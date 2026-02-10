# --- Log Groups ---

resource "aws_cloudwatch_log_group" "backend" {
  name              = "/${var.name_prefix}/backend"
  retention_in_days = 14

  tags = { Name = "${var.name_prefix}-backend-logs" }
}

resource "aws_cloudwatch_log_group" "api_gateway" {
  name              = "/${var.name_prefix}/api-gateway"
  retention_in_days = 7

  tags = { Name = "${var.name_prefix}-api-gateway-logs" }
}

# --- SNS Topic for Alarms ---

resource "aws_sns_topic" "alarms" {
  name = "${var.name_prefix}-alarms"

  tags = { Name = "${var.name_prefix}-alarms" }
}

resource "aws_sns_topic_subscription" "email" {
  count = var.alarm_email != "" ? 1 : 0

  topic_arn = aws_sns_topic.alarms.arn
  protocol  = "email"
  endpoint  = var.alarm_email
}

# --- CloudWatch Alarms ---

resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  alarm_name          = "${var.name_prefix}-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "cpu_usage_active"
  namespace           = var.metrics_namespace
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "EC2 CPU usage above 80% for 10 minutes"
  alarm_actions       = [aws_sns_topic.alarms.arn]

  dimensions = {
    InstanceId = var.instance_id
  }

  tags = { Name = "${var.name_prefix}-cpu-alarm" }
}

resource "aws_cloudwatch_metric_alarm" "memory_high" {
  alarm_name          = "${var.name_prefix}-memory-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "mem_used_percent"
  namespace           = var.metrics_namespace
  period              = 300
  statistic           = "Average"
  threshold           = 85
  alarm_description   = "EC2 memory usage above 85% for 10 minutes"
  alarm_actions       = [aws_sns_topic.alarms.arn]

  dimensions = {
    InstanceId = var.instance_id
  }

  tags = { Name = "${var.name_prefix}-memory-alarm" }
}

resource "aws_cloudwatch_metric_alarm" "status_check" {
  alarm_name          = "${var.name_prefix}-status-check"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "StatusCheckFailed"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Maximum"
  threshold           = 1
  alarm_description   = "EC2 instance status check failed"
  alarm_actions       = [aws_sns_topic.alarms.arn]

  dimensions = {
    InstanceId = var.instance_id
  }

  tags = { Name = "${var.name_prefix}-status-alarm" }
}
