output "cluster_name" {
  value = aws_ecs_cluster.main.name
}

output "service_name" {
  value = aws_ecs_service.backend.name
}

output "cluster_arn" {
  value = aws_ecs_cluster.main.arn
}
