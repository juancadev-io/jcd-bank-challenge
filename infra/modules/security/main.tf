resource "aws_security_group" "ecs" {
  name        = "${var.name_prefix}-ecs-sg"
  description = "Security group for backend ECS tasks"
  vpc_id      = var.vpc_id

  tags = { Name = "${var.name_prefix}-ecs-sg" }
}

resource "aws_security_group" "vpc_link" {
  name        = "${var.name_prefix}-vpc-link-sg"
  description = "Security group for API Gateway VPC Link"
  vpc_id      = var.vpc_id

  tags = { Name = "${var.name_prefix}-vpc-link-sg" }
}

# ECS: allow inbound 8080 from VPC Link SG only
resource "aws_vpc_security_group_ingress_rule" "ecs_from_vpc_link" {
  security_group_id            = aws_security_group.ecs.id
  referenced_security_group_id = aws_security_group.vpc_link.id
  from_port                    = 8080
  to_port                      = 8080
  ip_protocol                  = "tcp"
}

# ECS: allow all outbound (NAT GW for ECR, CloudWatch)
resource "aws_vpc_security_group_egress_rule" "ecs_all_out" {
  security_group_id = aws_security_group.ecs.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

# VPC Link: allow inbound 8080 (API Gateway routes through this)
resource "aws_vpc_security_group_ingress_rule" "vpc_link_inbound" {
  security_group_id = aws_security_group.vpc_link.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 8080
  to_port           = 8080
  ip_protocol       = "tcp"
}

# VPC Link: allow outbound to ECS SG on 8080
resource "aws_vpc_security_group_egress_rule" "vpc_link_to_ecs" {
  security_group_id            = aws_security_group.vpc_link.id
  referenced_security_group_id = aws_security_group.ecs.id
  from_port                    = 8080
  to_port                      = 8080
  ip_protocol                  = "tcp"
}
