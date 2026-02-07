variable "name_prefix" {
  type = string
}

variable "instance_type" {
  type = string
}

variable "ami_id" {
  type = string
}

variable "subnet_id" {
  type = string
}

variable "security_group_id" {
  type = string
}

variable "instance_profile_name" {
  type = string
}

variable "ecr_repository_url" {
  type = string
}

variable "aws_region" {
  type = string
}

variable "encryption_key" {
  type      = string
  sensitive = true
}

variable "log_group_name" {
  type = string
}

variable "metrics_namespace" {
  type = string
}
