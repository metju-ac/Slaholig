resource "kubernetes_manifest" "database" {
  for_each = toset(var.databases)

  manifest = {
    "apiVersion" = "postgresql.cnpg.io/v1"
    "kind"       = "Database"
    "metadata" = {
      "name"      = each.value
      "namespace" = var.cnpg_namespace
    }
    "spec" = {
      "name"  = each.value
      "owner" = each.value
      "cluster" = {
        "name" = var.cluster_name
      }
    }
  }
}
