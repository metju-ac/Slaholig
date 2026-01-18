variable "cnpg_namespace" {
  description = "CNPG namespace"
  type        = string
}

variable "cluster_name" {
  description = "CNPG cluster name"
  type        = string
}

variable "databases" {
  description = "List of databases to create"
  type        = list(string)
}