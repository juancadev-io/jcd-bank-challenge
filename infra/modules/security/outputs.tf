output "ecs_security_group_id" {
  value = aws_security_group.ecs.id
}

output "vpc_link_security_group_id" {
  value = aws_security_group.vpc_link.id
}
