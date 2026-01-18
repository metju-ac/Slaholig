variable "namespace" {
  description = "Kubernetes namespace"
  type        = string
  default     = "kotlin-seminar"
}

variable "domain" {
  description = "Domain for ingress"
  type        = string
  default     = "kotlin-seminar.bez-sance.cz"
}