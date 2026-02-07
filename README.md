# Bank Onboarding Portal

Portal interno de onboarding bancario para registro de clientes nuevos y apertura de cuentas básicas.

## Stack Técnico

- **Backend**: Java 21 con Spring Boot 4.0.2
- **Frontend**: Angular
- **Base de datos**: PostgreSQL 16
- **Containerización**: Docker & Docker Compose

## Estructura del Proyecto

```
├── apps/
│   ├── backend/          # Spring Boot REST API
│   └── frontend/         # Angular SPA
├── database/
│   └── init/            # Scripts SQL de inicialización
├── infra/               # Terraform (AWS)
├── docs/                # Documentación
├── docker-compose.yml   # Orquestación de servicios
└── .env                 # Variables de entorno locales
```

## Configuración Inicial

### 1. Variables de Entorno

Copia el archivo de ejemplo y configura tus variables:

```bash
cp .env.example .env
```

Edita `.env` con tus configuraciones (valores por defecto ya incluidos):

```env
DB_NAME=bank_onboarding
DB_USER=bankuser
DB_PASSWORD=bankpass123
DB_PORT=5432
SERVER_PORT=8080
```

### 2. Iniciar la Base de Datos

La base de datos PostgreSQL se iniciará automáticamente con Docker Compose y ejecutará los scripts de inicialización:

```bash
# Iniciar solo PostgreSQL
docker-compose up -d postgres

# Ver logs de la base de datos
docker-compose logs -f postgres
```

Los scripts SQL en `database/init/` crearán:
- Tablas `customers` y `accounts`
- Índices para mejor rendimiento
- Constraints para reglas de negocio (un cliente = una cuenta)
- Datos de prueba (4 clientes, 2 con cuentas)

### 3. Iniciar el Backend

#### Opción A: Con Docker (Recomendado)

```bash
# Iniciar todos los servicios
docker-compose up -d

# Ver logs del backend
docker-compose logs -f backend
```

#### Opción B: Desarrollo Local

```bash
cd apps/backend

# Asegúrate que PostgreSQL esté corriendo
docker-compose up -d postgres

# Ejecutar la aplicación
./mvnw spring-boot:run

# O ejecutar tests
./mvnw test
```

El backend estará disponible en `http://localhost:8080`

## API Endpoints

### Customers

- `POST /api/customers` - Crear cliente
- `GET /api/customers` - Listar todos los clientes

### Accounts

- `POST /api/accounts` - Crear cuenta
- `GET /api/accounts?customerId={id}` - Consultar cuentas por cliente

## Reglas de Negocio

1. ✅ Un cliente puede tener **una sola cuenta**
2. ✅ `documentNumber` y `email` son **obligatorios y únicos**
3. ✅ NO permitir crear cuenta si el cliente no existe → `404`
4. ✅ `accountNumber` se **autogenera** (formato: `ACC-{timestamp}-{random}`)
5. ✅ Validaciones retornan `400` con mensajes claros en JSON

## Base de Datos

### Esquema

**Tabla: customers**
```sql
- id: BIGSERIAL PRIMARY KEY
- document_type: VARCHAR(3) CHECK (CC/CE/PAS)
- document_number: VARCHAR(50) UNIQUE NOT NULL
- full_name: VARCHAR(255) NOT NULL
- email: VARCHAR(255) UNIQUE NOT NULL
- created_at: TIMESTAMP
- updated_at: TIMESTAMP
```

**Tabla: accounts**
```sql
- id: BIGSERIAL PRIMARY KEY
- customer_id: BIGINT UNIQUE FK -> customers(id)
- account_number: VARCHAR(50) UNIQUE NOT NULL
- status: VARCHAR(20) CHECK (ACTIVE/INACTIVE)
- balance: DECIMAL(15,2) DEFAULT 0.00
- created_at: TIMESTAMP
- updated_at: TIMESTAMP
```

### Conectarse a PostgreSQL

```bash
# Desde el host
docker exec -it bank-postgres psql -U bankuser -d bank_onboarding

# Comandos útiles de psql
\dt              # Listar tablas
\d customers     # Describir tabla customers
\d accounts      # Describir tabla accounts
SELECT * FROM customers;
SELECT * FROM accounts;
```

## Comandos Docker Útiles

```bash
# Iniciar todos los servicios
docker-compose up -d

# Detener todos los servicios
docker-compose down

# Ver logs
docker-compose logs -f

# Reconstruir las imágenes
docker-compose build --no-cache

# Reiniciar un servicio específico
docker-compose restart backend

# Eliminar volúmenes (¡CUIDADO! Borra datos)
docker-compose down -v
```

## Desarrollo

### Backend (Spring Boot)

```bash
cd apps/backend

# Compilar
./mvnw clean install

# Ejecutar tests
./mvnw test

# Ejecutar con perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Generar JAR
./mvnw package -DskipTests
```

### Frontend (Angular)

```bash
cd apps/frontend

# Instalar dependencias
npm install

# Desarrollo
ng serve

# Tests
ng test

# Build producción
ng build --prod
```

## Health Check

Una vez iniciado el backend, verifica su estado:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Info de la aplicación
curl http://localhost:8080/actuator/info
```

## Troubleshooting

### El backend no se conecta a la base de datos

1. Verifica que PostgreSQL esté corriendo: `docker-compose ps`
2. Revisa los logs: `docker-compose logs postgres`
3. Verifica las variables de entorno en `.env`
4. Asegúrate que el puerto 5432 no esté ocupado: `lsof -i :5432`

### Error de permisos en mvnw

```bash
chmod +x apps/backend/mvnw
```

### Resetear la base de datos

```bash
# Detener servicios y eliminar volúmenes
docker-compose down -v

# Reiniciar (los scripts SQL se ejecutarán nuevamente)
docker-compose up -d
```

## Próximos Pasos

1. ✅ Implementar entidades JPA (Customer, Account)
2. ✅ Crear DTOs con validaciones
3. ✅ Implementar Services con lógica de negocio
4. ✅ Crear Controllers con manejo de errores
5. ✅ Agregar tests unitarios e integración
6. 🔄 Desarrollar frontend Angular
7. 🔄 Configurar CI/CD
8. 🔄 Deploy a AWS

## Licencia

Este es un proyecto de práctica/kata para evaluaciones técnicas.
