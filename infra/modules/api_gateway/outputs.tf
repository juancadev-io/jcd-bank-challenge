output "api_endpoint" {
  value = aws_apigatewayv2_api.main.api_endpoint
}

output "api_id" {
  value = aws_apigatewayv2_api.main.id
}

output "cloud_map_service_arn" {
  value = aws_service_discovery_service.backend.arn
}
