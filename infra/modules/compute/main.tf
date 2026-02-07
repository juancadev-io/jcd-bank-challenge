resource "aws_instance" "backend" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = [var.security_group_id]
  iam_instance_profile   = var.instance_profile_name

  user_data = templatefile("${path.module}/user_data.tftpl", {
    aws_region         = var.aws_region
    ecr_repository_url = var.ecr_repository_url
    encryption_key     = var.encryption_key
    log_group_name     = var.log_group_name
    metrics_namespace  = var.metrics_namespace
  })

  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
    encrypted   = true
  }

  tags = { Name = "${var.name_prefix}-backend" }
}

# --- Schedule: auto start/stop to save costs ---
# Monday-Friday 8am-5pm Colombia time (America/Bogota)

resource "aws_iam_role" "scheduler" {
  name = "${var.name_prefix}-ec2-scheduler"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect    = "Allow"
        Principal = { Service = "scheduler.amazonaws.com" }
        Action    = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy" "scheduler" {
  name = "ec2-start-stop"
  role = aws_iam_role.scheduler.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["ec2:StartInstances", "ec2:StopInstances"]
        Resource = aws_instance.backend.arn
      }
    ]
  })
}

resource "aws_scheduler_schedule" "start_ec2" {
  name = "${var.name_prefix}-start-ec2"

  flexible_time_window {
    mode = "OFF"
  }

  schedule_expression          = "cron(0 8 ? * MON-FRI *)"
  schedule_expression_timezone = "America/Bogota"

  target {
    arn      = "arn:aws:scheduler:::aws-sdk:ec2:startInstances"
    role_arn = aws_iam_role.scheduler.arn

    input = jsonencode({
      InstanceIds = [aws_instance.backend.id]
    })
  }
}

resource "aws_scheduler_schedule" "stop_ec2" {
  name = "${var.name_prefix}-stop-ec2"

  flexible_time_window {
    mode = "OFF"
  }

  schedule_expression          = "cron(0 17 ? * MON-FRI *)"
  schedule_expression_timezone = "America/Bogota"

  target {
    arn      = "arn:aws:scheduler:::aws-sdk:ec2:stopInstances"
    role_arn = aws_iam_role.scheduler.arn

    input = jsonencode({
      InstanceIds = [aws_instance.backend.id]
    })
  }
}
