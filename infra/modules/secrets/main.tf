data "kubernetes_secret" "db_secrets" {
  for_each = var.service_configs

  metadata {
    name      = "${each.key}-service-db-password"
    namespace = var.cnpg_namespace
  }
}

# Copy secrets to application namespace
resource "kubernetes_secret" "db_secrets" {
  for_each = var.service_configs

  metadata {
    name      = "${each.key}-service-db-password"
    namespace = var.namespace
  }

  data = {
    password = data.kubernetes_secret.db_secrets[each.key].data.password
  }
}

# Axon Server config
resource "kubernetes_config_map" "axon_config" {
  metadata {
    name      = "axonserver-config"
    namespace = var.namespace
  }

  data = {
    "axonserver.properties" = file(var.axon_config_path)
  }
}

# Service configs
resource "kubernetes_config_map" "service_configs" {
  for_each = var.service_configs

  metadata {
    name      = "${each.key}-service-config"
    namespace = var.namespace
  }

  data = {
    "application.yml" = file(each.value.config_path)
  }
}