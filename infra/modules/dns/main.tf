terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
    cloudflare = {
      source = "cloudflare/cloudflare"
    }
  }
}

locals {
  domain_parts  = split(".", var.domain_name)
  parent_domain = join(".", slice(local.domain_parts, 1, length(local.domain_parts)))
}

data "cloudflare_zone" "main" {
  name = local.parent_domain
}

# --- ACM Certificate (must be in us-east-1 for CloudFront) ---

resource "aws_acm_certificate" "main" {
  domain_name       = var.domain_name
  validation_method = "DNS"

  tags = { Name = var.domain_name }

  lifecycle {
    create_before_destroy = true
  }
}

# --- Cloudflare DNS records for ACM validation ---

resource "cloudflare_record" "acm_validation" {
  for_each = {
    for dvo in aws_acm_certificate.main.domain_validation_options : dvo.domain_name => {
      name  = dvo.resource_record_name
      type  = dvo.resource_record_type
      value = dvo.resource_record_value
    }
  }

  zone_id = data.cloudflare_zone.main.id
  name    = each.value.name
  content = trimsuffix(each.value.value, ".")
  type    = each.value.type
  proxied = false
  comment = "ACM validation for ${var.domain_name}"
}

# --- Wait for certificate validation ---

resource "aws_acm_certificate_validation" "main" {
  certificate_arn         = aws_acm_certificate.main.arn
  validation_record_fqdns = [for dvo in aws_acm_certificate.main.domain_validation_options : dvo.resource_record_name]
}
