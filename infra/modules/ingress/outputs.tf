output "ingress_name" {
  description = "Ingress name"
  value       = kubernetes_ingress_v1.main.metadata[0].name
}