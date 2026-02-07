output "ec2_security_group_id" {
  value = aws_security_group.ec2.id
}

output "vpc_link_security_group_id" {
  value = aws_security_group.vpc_link.id
}
