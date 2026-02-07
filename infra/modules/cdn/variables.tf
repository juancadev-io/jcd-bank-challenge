variable "name_prefix" {
  type = string
}

variable "domain_name" {
  type        = string
  description = "Domain alias for the distribution (e.g. bank.juancamilofarfan.com)"
}

variable "s3_bucket_id" {
  type        = string
  description = "S3 bucket ID for frontend origin"
}

variable "s3_bucket_arn" {
  type        = string
  description = "S3 bucket ARN for bucket policy"
}

variable "s3_bucket_regional_domain" {
  type        = string
  description = "S3 bucket regional domain name for origin"
}

variable "api_gateway_endpoint" {
  type        = string
  description = "API Gateway endpoint URL (https://...)"
}

variable "acm_certificate_arn" {
  type        = string
  description = "ACM certificate ARN (must be in us-east-1)"
}
