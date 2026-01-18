variable "namespace" {
  description = "Kubernetes namespace"
  type        = string
}

variable "domain" {
  description = "Domain name"
  type        = string
}

variable "services" {
  description = "Map of service names"
  type = object({
    courses    = string
    enrollment = string
    forum      = string
    assignment = string
  })
}