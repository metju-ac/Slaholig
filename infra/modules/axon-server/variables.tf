variable "namespace" {
  description = "Kubernetes namespace"
  type        = string
}

variable "config_map_name" {
  description = "Config map name"
  type        = string
}

variable "storage_class" {
  description = "Storage class name"
  type        = string
  default     = "longhorn-local"
}

variable "event_storage_size" {
  description = "Event storage size"
  type        = string
  default     = "1Gi"
}

variable "data_storage_size" {
  description = "Data storage size"
  type        = string
  default     = "1Gi"
}