resource "aws_security_group" "ec2" {
  name        = "${var.name_prefix}-ec2-sg"
  description = "Security group for backend EC2 instance"
  vpc_id      = var.vpc_id

  tags = { Name = "${var.name_prefix}-ec2-sg" }
}

resource "aws_security_group" "vpc_link" {
  name        = "${var.name_prefix}-vpc-link-sg"
  description = "Security group for API Gateway VPC Link"
  vpc_id      = var.vpc_id

  tags = { Name = "${var.name_prefix}-vpc-link-sg" }
}

# EC2: allow inbound 8080 from VPC Link SG only
resource "aws_vpc_security_group_ingress_rule" "ec2_from_vpc_link" {
  security_group_id            = aws_security_group.ec2.id
  referenced_security_group_id = aws_security_group.vpc_link.id
  from_port                    = 8080
  to_port                      = 8080
  ip_protocol                  = "tcp"
}

# EC2: allow all outbound (NAT GW for ECR, SSM, CloudWatch)
resource "aws_vpc_security_group_egress_rule" "ec2_all_out" {
  security_group_id = aws_security_group.ec2.id
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

# VPC Link: allow outbound to EC2 SG on 8080
resource "aws_vpc_security_group_egress_rule" "vpc_link_to_ec2" {
  security_group_id            = aws_security_group.vpc_link.id
  referenced_security_group_id = aws_security_group.ec2.id
  from_port                    = 8080
  to_port                      = 8080
  ip_protocol                  = "tcp"
}
