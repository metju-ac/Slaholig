output "service_name" {
  description = "Axon Server service name"
  value       = kubernetes_service_v1.axonserver.metadata[0].name
}

output "deployment_name" {
  description = "Axon Server deployment name"
  value       = kubernetes_deployment_v1.axonserver.metadata[0].name
}