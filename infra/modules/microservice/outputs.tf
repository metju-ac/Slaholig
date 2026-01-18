output "service_name" {
  description = "Service name"
  value       = kubernetes_service.service.metadata[0].name
}

output "deployment_name" {
  description = "Deployment name"
  value       = kubernetes_deployment.service.metadata[0].name
}