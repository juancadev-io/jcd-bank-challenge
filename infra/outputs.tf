output "frontend_url" {
  description = "URL for the frontend application"
  value       = "https://${var.domain_name}"
}

output "api_url" {
  description = "URL for the backend API (via CloudFront)"
  value       = "https://${var.domain_name}/api"
}

output "api_gateway_url" {
  description = "Direct API Gateway URL (for testing)"
  value       = module.api_gateway.api_endpoint
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID (for cache invalidation)"
  value       = module.cdn.distribution_id
}

output "s3_bucket_name" {
  description = "S3 bucket name for frontend files"
  value       = module.storage.bucket_id
}

output "ecr_repository_url" {
  description = "ECR repository URL for backend Docker images"
  value       = module.ecr.repository_url
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = module.compute.cluster_name
}

output "ecs_service_name" {
  description = "ECS service name"
  value       = module.compute.service_name
}

output "vpc_id" {
  description = "VPC ID"
  value       = module.networking.vpc_id
}

output "acm_certificate_arn" {
  description = "ACM certificate ARN"
  value       = module.dns.certificate_arn
}
