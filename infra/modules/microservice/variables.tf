variable "namespace" {
  description = "Kubernetes namespace"
  type        = string
}

variable "service_name" {
  description = "Service name"
  type        = string
}

variable "image" {
  description = "Docker image"
  type        = string
}

variable "image_pull_policy" {
  description = "Image pull policy"
  type        = string
  default     = "Always"
}

variable "image_pull_secret" {
  description = "Image pull secret name"
  type        = string
  default     = "docker-registry-secret"
}

variable "replicas" {
  description = "Number of replicas"
  type        = number
  default     = 1
}

variable "database_url" {
  description = "Database URL"
  type        = string
}

variable "database_secret_name" {
  description = "Database secret name"
  type        = string
}

variable "config_map_name" {
  description = "Config map name"
  type        = string
}

variable "axon_server_url" {
  description = "Axon server URL"
  type        = string
}

variable "cpu_request" {
  description = "CPU request"
  type        = string
  default     = "250m"
}

variable "memory_request" {
  description = "Memory request"
  type        = string
  default     = "512Mi"
}

variable "cpu_limit" {
  description = "CPU limit"
  type        = string
  default     = "500m"
}

variable "memory_limit" {
  description = "Memory limit"
  type        = string
  default     = "1024Mi"
}