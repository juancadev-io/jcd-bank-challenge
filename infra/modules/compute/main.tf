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
