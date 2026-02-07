output "certificate_arn" {
  description = "Validated ACM certificate ARN"
  value       = aws_acm_certificate_validation.main.certificate_arn
}

output "zone_id" {
  description = "Cloudflare zone ID for the parent domain"
  value       = data.cloudflare_zone.main.id
}
