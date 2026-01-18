variable "namespace" {
  description = "Application namespace"
  type        = string
}

variable "cnpg_namespace" {
  description = "CNPG namespace"
  type        = string
}

variable "axon_config_path" {
  description = "Path to Axon Server config"
  type        = string
}

variable "service_configs" {
  description = "Map of service configurations"
  type = map(object({
    config_path = string
  }))
}