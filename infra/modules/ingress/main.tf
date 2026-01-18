resource "kubernetes_ingress_v1" "main" {
  metadata {
    name      = "kotlin-seminar-ingress"
    namespace = var.namespace
    annotations = {
      "kubernetes.io/ingress.class"                  = "traefik"
      "traefik.ingress.kubernetes.io/rewrite-target" = "/"
      "traefik.ingress.kubernetes.io/rule-type"      = "PathPrefixStrip"
      "cert-manager.io/cluster-issuer"               = "letsencrypt-production"
    }
  }

  spec {
    tls {
      hosts       = [var.domain]
      secret_name = "kotlin-seminar-tls"
    }

    rule {
      host = var.domain

      http {
        path {
          path      = "/courses"
          path_type = "Prefix"

          backend {
            service {
              name = var.services.courses
              port {
                number = 8080
              }
            }
          }
        }

        path {
          path      = "/enrollment"
          path_type = "Prefix"

          backend {
            service {
              name = var.services.enrollment
              port {
                number = 8080
              }
            }
          }
        }

        path {
          path      = "/forums"
          path_type = "Prefix"

          backend {
            service {
              name = var.services.forum
              port {
                number = 8080
              }
            }
          }
        }

        path {
          path      = "/assignments"
          path_type = "Prefix"

          backend {
            service {
              name = var.services.assignment
              port {
                number = 8080
              }
            }
          }
        }
      }
    }
  }
}