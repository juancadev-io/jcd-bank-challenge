terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "~> 4.0"
    }
  }

  backend "s3" {
    bucket         = "bank-onboarding-terraform-state"
    key            = "dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "bank-onboarding-terraform-lock"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = local.common_tags
  }
}

provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"

  default_tags {
    tags = local.common_tags
  }
}

provider "cloudflare" {
  api_token = var.cloudflare_api_token
}

# --- Modules ---

module "networking" {
  source = "./modules/networking"

  name_prefix = local.name_prefix
  vpc_cidr    = var.vpc_cidr
  azs         = local.azs
}

module "security" {
  source = "./modules/security"

  name_prefix = local.name_prefix
  vpc_id      = module.networking.vpc_id
}

module "ecr" {
  source = "./modules/ecr"

  name_prefix = local.name_prefix
}

module "iam" {
  source = "./modules/iam"

  name_prefix = local.name_prefix
}

module "api_gateway" {
  source = "./modules/api_gateway"

  name_prefix                = local.name_prefix
  private_subnet_ids         = module.networking.private_subnet_ids
  vpc_link_security_group_id = module.security.vpc_link_security_group_id
  log_group_arn              = module.monitoring.api_gateway_log_group_arn
}

module "monitoring" {
  source = "./modules/monitoring"

  name_prefix  = local.name_prefix
  environment  = var.environment
  cluster_name = module.compute.cluster_name
  service_name = module.compute.service_name
  alarm_email  = var.alarm_email
}

module "compute" {
  source = "./modules/compute"

  name_prefix           = local.name_prefix
  cpu                   = var.fargate_cpu
  memory                = var.fargate_memory
  private_subnet_ids    = module.networking.private_subnet_ids
  security_group_id     = module.security.ecs_security_group_id
  execution_role_arn    = module.iam.execution_role_arn
  task_role_arn         = module.iam.task_role_arn
  ecr_repository_url    = module.ecr.repository_url
  aws_region            = var.aws_region
  encryption_key        = var.encryption_key
  log_group_name        = module.monitoring.log_group_name
  cloud_map_service_arn = module.api_gateway.cloud_map_service_arn
}

module "storage" {
  source = "./modules/storage"

  name_prefix = local.name_prefix
}

module "dns" {
  source = "./modules/dns"

  providers = {
    aws        = aws.us_east_1
    cloudflare = cloudflare
  }

  domain_name = var.domain_name
}

# --- Cloudflare CNAME record (bank → CloudFront) ---
# Placed here to break circular dependency between dns and cdn modules.
# proxied = false (DNS only) because CloudFront handles SSL/CDN.

resource "cloudflare_record" "app" {
  zone_id = module.dns.zone_id
  name    = split(".", var.domain_name)[0]
  content = module.cdn.distribution_domain_name
  type    = "CNAME"
  proxied = false
  comment = "Points ${var.domain_name} to CloudFront"
}

module "cdn" {
  source = "./modules/cdn"

  name_prefix               = local.name_prefix
  domain_name               = var.domain_name
  s3_bucket_id              = module.storage.bucket_id
  s3_bucket_arn             = module.storage.bucket_arn
  s3_bucket_regional_domain = module.storage.bucket_regional_domain_name
  api_gateway_endpoint      = module.api_gateway.api_endpoint
  acm_certificate_arn       = module.dns.certificate_arn
}
