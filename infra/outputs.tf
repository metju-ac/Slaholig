output "axon_server_service" {
  description = "Axon Server service name"
  value       = module.axon_server.service_name
}

output "services" {
  description = "Deployed services"
  value = {
    courses    = module.courses_service.service_name
    enrollment = module.enrollment_service.service_name
    forum      = module.forum_service.service_name
    assignment = module.assignment_service.service_name
  }
}

output "ingress_domain" {
  description = "Ingress domain"
  value       = var.domain
}