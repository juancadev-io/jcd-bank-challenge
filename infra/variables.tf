variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

variable "domain_name" {
  description = "Subdomain for the application"
  type        = string
  default     = "bank.juancamilofarfan.com"
}

variable "cloudflare_api_token" {
  description = "Cloudflare API token with DNS edit permissions"
  type        = string
  sensitive   = true
}

variable "fargate_cpu" {
  description = "CPU units for the Fargate task (512 = 0.5 vCPU)"
  type        = number
  default     = 512
}

variable "fargate_memory" {
  description = "Memory (MiB) for the Fargate task"
  type        = number
  default     = 1024
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "encryption_key" {
  description = "Encryption key for the backend application (AES)"
  type        = string
  sensitive   = true
}

variable "alarm_email" {
  description = "Email for CloudWatch alarm notifications (leave empty to skip)"
  type        = string
  default     = ""
}
