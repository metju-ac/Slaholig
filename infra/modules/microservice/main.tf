resource "kubernetes_deployment" "service" {
  metadata {
    name      = var.service_name
    namespace = var.namespace
    labels = {
      app = var.service_name
    }
  }

  spec {
    replicas = var.replicas

    selector {
      match_labels = {
        app = var.service_name
      }
    }

    template {
      metadata {
        labels = {
          app = var.service_name
        }
        annotations = {
          "k8s.grafana.com/scrape"             = "true"
          "k8s.grafana.com/metrics.portNumber" = "8080"
          "k8s.grafana.com/metrics.path"       = "/actuator/prometheus"
        }
      }

      spec {
        image_pull_secrets {
          name = var.image_pull_secret
        }

        container {
          name              = var.service_name
          image             = var.image
          image_pull_policy = var.image_pull_policy

          port {
            container_port = 8080
          }

          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }

          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-Djdk.containerized=false"
          }

          env {
            name  = "SPRING_DATASOURCE_URL"
            value = var.database_url
          }

          env {
            name  = "AXON_AXONSERVER_SERVERS"
            value = var.axon_server_url
          }

          env {
            name = "SPRING_DATASOURCE_PASSWORD"
            value_from {
              secret_key_ref {
                name = var.database_secret_name
                key  = "password"
              }
            }
          }

          resources {
            requests = {
              cpu    = var.cpu_request
              memory = var.memory_request
            }
            limits = {
              cpu    = var.cpu_limit
              memory = var.memory_limit
            }
          }

          volume_mount {
            name       = "config"
            mount_path = "/config"
            read_only  = true
          }
        }

        volume {
          name = "config"
          config_map {
            name = var.config_map_name
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "service" {
  metadata {
    name      = var.service_name
    namespace = var.namespace
  }

  spec {
    selector = {
      app = var.service_name
    }

    port {
      port        = 8080
      target_port = 8080
    }

    type = "ClusterIP"
  }
}