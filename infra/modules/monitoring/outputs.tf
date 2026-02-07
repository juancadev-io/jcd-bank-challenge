output "log_group_name" {
  value = aws_cloudwatch_log_group.backend.name
}

output "log_group_arn" {
  value = aws_cloudwatch_log_group.backend.arn
}

output "api_gateway_log_group_arn" {
  value = aws_cloudwatch_log_group.api_gateway.arn
}

output "sns_topic_arn" {
  value = aws_sns_topic.alarms.arn
}
