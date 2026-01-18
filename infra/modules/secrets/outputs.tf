output "db_secret_names" {
  description = "Database secret names"
  value = {
    for k, v in kubernetes_secret.db_secrets : k => v.metadata[0].name
  }
}

output "service_config_names" {
  description = "Service config map names"
  value = {
    for k, v in kubernetes_config_map.service_configs : k => v.metadata[0].name
  }
}

output "axon_config_name" {
  description = "Axon Server config map name"
  value       = kubernetes_config_map.axon_config.metadata[0].name
}