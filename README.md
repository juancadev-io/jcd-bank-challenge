# Bank Onboarding Portal

Portal interno de onboarding bancario para registro de clientes y apertura de cuentas.

**Live:** [bank.juancamilofarfan.com](https://bank.juancamilofarfan.com)

## Stack

| Capa | Tecnologia |
|------|-----------|
| Backend | Java 21 + Spring Boot 4.0.2 |
| Frontend | Angular 21 (standalone components) |
| Base de datos | H2 en memoria |
| Infraestructura | Terraform + AWS (EC2, ECR, API Gateway, S3, CloudFront) |
| DNS/CDN | CloudFront + Cloudflare |
| CI/CD | GitHub Actions (7 workflows) |
| Contenedores | Docker multi-stage builds |

## Estructura del monorepo

```
apps/
  backend/        Spring Boot REST API
  frontend/       Angular SPA
infra/
  modules/        10 modulos Terraform (networking, security, compute, ecr,
                  api_gateway, storage, cdn, iam, dns, monitoring)
.github/
  workflows/      CI/CD pipelines (validate + deploy + destroy)
```

## Arquitectura del backend

Arquitectura en capas estricta:

```
Controller  -->  Service  -->  Repository  -->  DB
    |               |
   DTO          Entity
```

- **Controller**: recibe HTTP, valida con `@Valid`, delega al service
- **Service**: logica de negocio, validaciones de duplicados, generacion de account number
- **Repository**: Spring Data JPA, queries derivadas
- **DTO**: objetos de transferencia separados de las entidades
- **Entity**: mapeo JPA con `@CryptoConverter` para cifrado de datos sensibles

### Manejo de errores

`GlobalExceptionHandler` (`@ControllerAdvice`) captura:

| Excepcion | HTTP | Cuando |
|-----------|------|--------|
| `MethodArgumentNotValidException` | 400 | Campos invalidos (`@NotBlank`, `@Email`) |
| `BusinessException` | 400 | Regla de negocio violada (duplicado, cuenta ya existe) |
| `ResourceNotFoundException` | 404 | Cliente o cuenta no encontrada |
| `Exception` | 500 | Error inesperado |

Todas las respuestas de error incluyen: `timestamp`, `status`, `message`, `path`.

### Seguridad de datos

Los campos sensibles (`documentNumber`, `fullName`, `email`, `accountNumber`) se cifran en base de datos usando AES-256-CBC mediante un `AttributeConverter` de JPA:

```
Customer.email  --(CryptoConverter)--> Base64(AES(email))  --> DB
DB              --(CryptoConverter)--> decrypt              --> Customer.email
```

La clave de cifrado se inyecta por variable de entorno (`ENCRYPTION_KEY`), nunca hardcodeada en produccion.

### Observabilidad

- `RequestIdFilter`: genera un UUID corto por request y lo inyecta en MDC + header `X-Request-Id`
- Todos los logs incluyen el requestId para trazabilidad
- Spring Actuator expone `/actuator/health`, `/actuator/info`, `/actuator/metrics`

## Reglas de negocio

| # | Regla | Implementacion |
|---|-------|---------------|
| 1 | Un cliente = una sola cuenta | `UNIQUE(customer_id)` en tabla + validacion en `AccountService` |
| 2 | `documentNumber` y `email` obligatorios y unicos | `@NotBlank` en DTO + `UNIQUE` en entidad + chequeo en service |
| 3 | No crear cuenta sin cliente existente | `CustomerRepository.existsById()` -> `ResourceNotFoundException` (404) |
| 4 | `accountNumber` autogenerado | Formato `ACC-{timestamp}-{random4}` en `AccountService` |
| 5 | Validaciones retornan 400 con JSON claro | `GlobalExceptionHandler` con estructura estandar |

## API

### Customers

```
POST /api/customers          Crear cliente
GET  /api/customers          Listar todos
GET  /api/customers/{id}     Obtener por ID
```

### Accounts

```
POST  /api/accounts                    Crear cuenta (body: { customerId })
GET   /api/accounts                    Listar todas
GET   /api/accounts?customerId={id}    Filtrar por cliente
PATCH /api/accounts/{id}/status        Cambiar estado (ACTIVE/INACTIVE)
```

## Frontend

SPA con dos paginas:

- **`/customers`** — Formulario reactivo (`ReactiveFormsModule`) para crear clientes + tabla de listado. Checkbox opcional para crear cuenta automaticamente.
- **`/accounts`** — Vista consolidada cliente-cuenta con acciones de crear cuenta y cambiar estado (activar/bloquear).

Patron de servicios: `CustomerService` y `AccountService` encapsulan las llamadas HTTP. Los componentes usan signals de Angular para estado reactivo.

**Responsive**: las tablas usan el patron de cards apiladas en pantallas <= 768px con `data-label` + CSS `::before` (sin JavaScript).

## Infraestructura (AWS + Terraform)

```
                    Cloudflare DNS
                         |
                    CloudFront CDN
                    /          \
            S3 (frontend)    API Gateway
                                |
                           VPC Private Subnet
                                |
                         EC2 (Docker + backend)
                                |
                           H2 in-memory
```

10 modulos Terraform:

| Modulo | Recursos |
|--------|----------|
| `networking` | VPC, subnets publica/privada, IGW, NAT, route tables |
| `security` | Security groups (EC2, VPC Link) |
| `compute` | EC2 con user_data para Docker + CloudWatch Agent |
| `ecr` | Repositorio de imagenes Docker |
| `api_gateway` | REST API con VPC Link hacia EC2 |
| `storage` | S3 bucket para frontend |
| `cdn` | CloudFront con origins S3 + API Gateway |
| `iam` | Roles y policies (EC2, ECR pull, SSM, CloudWatch) |
| `dns` | Route53 hosted zone + ACM certificate + Cloudflare validation |
| `monitoring` | CloudWatch log groups, alarms, SNS |

Estado remoto en S3 + DynamoDB lock.

## CI/CD

7 workflows en GitHub Actions:

| Workflow | Trigger | Que hace |
|----------|---------|----------|
| `validate-backend` | PR en `apps/backend/` | Maven test + build |
| `validate-frontend` | PR en `apps/frontend/` | npm install + build |
| `validate-infra` | PR en `infra/` | terraform fmt + init + validate + plan |
| `deploy-backend` | Push a main en `apps/backend/` | Build -> ECR push -> SSM deploy a EC2 -> health check |
| `deploy-frontend` | Push a main en `apps/frontend/` | Build -> S3 sync -> CloudFront invalidation |
| `deploy-infra` | Push a main en `infra/` | Terraform apply |
| `destroy` | Manual (workflow_dispatch) | Terraform destroy con confirmacion |

Todos los secretos (AWS keys, Cloudflare token, encryption key) se manejan via GitHub Secrets.

## Como ejecutar en local

### Prerrequisitos

- Java 21
- Node.js 20+
- npm 11+

### Backend

```bash
cd apps/backend
./mvnw spring-boot:run
```

Usa H2 en memoria por defecto (perfil default). No requiere base de datos externa.
API disponible en `http://localhost:8080`.

### Frontend

```bash
cd apps/frontend
npm install
npx ng serve
```

Disponible en `http://localhost:4200`. El proxy redirige `/api/*` al backend en `:8080`.

### Tests

```bash
# Backend
cd apps/backend && ./mvnw test

# Frontend
cd apps/frontend && npx ng test
```

## Variables de entorno

```bash
cp .env.example .env
# Editar .env con valores reales
```

Ver `.env.example` para la lista completa de variables. Nunca commitear credenciales reales.

## Decisiones de diseno

| Decision | Razon |
|----------|-------|
| H2 en dev, PostgreSQL en prod | Desarrollo sin dependencias externas, produccion con persistencia real |
| Cifrado AES en campos sensibles | Datos bancarios (documento, email, nombre) protegidos at-rest |
| `@ControllerAdvice` global | Respuestas de error consistentes en toda la API |
| RequestId por request | Trazabilidad end-to-end en logs |
| DTO separados de entidades | Desacoplar representacion HTTP del modelo de persistencia |
| Angular standalone components | Patron moderno de Angular, sin NgModules innecesarios |
| CSS responsive con data-label | Tablas legibles en movil sin JavaScript adicional |
| Terraform modular (10 modulos) | Separacion de concerns en IaC, reutilizable |
| EC2 + Docker (no Fargate) | Costo menor para una kata, con deploy automatizado via SSM |
| CloudFront + S3 para frontend | CDN global, HTTPS, cache automatico |
| Cloudflare para DNS | Gestion de dominio + validacion de certificado ACM |
