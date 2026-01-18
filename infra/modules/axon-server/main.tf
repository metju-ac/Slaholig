resource "kubernetes_persistent_volume_claim" "eventdata" {
  metadata {
    name      = "axonserver-eventdata"
    namespace = var.namespace
  }

  spec {
    storage_class_name = var.storage_class
    access_modes       = ["ReadWriteOnce"]
    resources {
      requests = {
        storage = var.event_storage_size
      }
    }
  }
}

resource "kubernetes_persistent_volume_claim" "data" {
  metadata {
    name      = "axonserver-data"
    namespace = var.namespace
  }

  spec {
    storage_class_name = var.storage_class
    access_modes       = ["ReadWriteOnce"]
    resources {
      requests = {
        storage = var.data_storage_size
      }
    }
  }
}

resource "kubernetes_deployment_v1" "axonserver" {
  metadata {
    name      = "axonserver"
    namespace = var.namespace
    labels = {
      app = "axonserver"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "axonserver"
      }
    }

    template {
      metadata {
        labels = {
          app = "axonserver"
        }
        annotations = {
          "k8s.grafana.com/scrape"              = "true"
          "k8s.grafana.com/metrics.portNumber"  = "8024"
          "k8s.grafana.com/metrics.path"        = "/actuator/prometheus"
        }
      }

      spec {
        container {
          name  = "axonserver"
          image = "axoniq/axonserver:latest"

          port {
            container_port = 8024
          }

          port {
            container_port = 8124
          }

          env {
            name  = "AXONIQ_AXONSERVER_STANDALONE"
            value = "true"
          }

          volume_mount {
            name       = "config"
            mount_path = "/config"
            read_only  = true
          }

          volume_mount {
            name       = "eventdata"
            mount_path = "/eventdata"
          }

          volume_mount {
            name       = "data"
            mount_path = "/data"
          }
        }

        volume {
          name = "config"
          config_map {
            name = var.config_map_name
          }
        }

        volume {
          name = "eventdata"
          persistent_volume_claim {
            claim_name = kubernetes_persistent_volume_claim.eventdata.metadata[0].name
          }
        }

        volume {
          name = "data"
          persistent_volume_claim {
            claim_name = kubernetes_persistent_volume_claim.data.metadata[0].name
          }
        }
      }
    }
  }
}

resource "kubernetes_service_v1" "axonserver" {
  metadata {
    name      = "axon-server"
    namespace = var.namespace
  }

  spec {
    selector = {
      app = "axonserver"
    }

    port {
      name        = "gui"
      port        = 8024
      target_port = 8024
    }

    port {
      name        = "grpc"
      port        = 8124
      target_port = 8124
    }

    type = "ClusterIP"
  }
}