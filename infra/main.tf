terraform {
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "3.0.1"
    }
  }
}

# Database module - creates CNPG databases
module "databases" {
  source = "./modules/database"

  cnpg_namespace = "cnpg-system"
  cluster_name   = "main"
  databases = [
    "assignment-service",
    "courses-service",
    "forum-service",
    "enrollment-service"
  ]
}

# Secrets module - manages all secrets and configmaps
module "secrets" {
  source = "./modules/secrets"

  namespace           = var.namespace
  cnpg_namespace      = "cnpg-system"
  axon_config_path    = "${path.module}/../axon/config/axonserver.properties"

  service_configs = {
    courses = {
      config_path = "${path.module}/../spring/config/courses-service/application-prod.yml"
    }
    enrollment = {
      config_path = "${path.module}/../spring/config/enrollment-service/application-prod.yml"
    }
    forum = {
      config_path = "${path.module}/../spring/config/forum-service/application-prod.yml"
    }
    assignment = {
      config_path = "${path.module}/../spring/config/assignment-service/application-prod.yml"
    }
  }

  depends_on = [module.databases]
}

# Axon Server module
module "axon_server" {
  source = "./modules/axon-server"

  namespace           = var.namespace
  config_map_name     = module.secrets.axon_config_name
  storage_class       = "longhorn-local"
  event_storage_size  = "1Gi"
  data_storage_size   = "1Gi"
}

# Microservices
module "courses_service" {
  source = "./modules/microservice"

  namespace            = var.namespace
  service_name         = "courses-service"
  image                = "mmisik/kotlin-seminar:courses-service"
  database_url         = "jdbc:postgresql://main-rw.cnpg-system.svc.cluster.local:5432/courses-service"
  database_secret_name = module.secrets.db_secret_names["courses"]
  config_map_name      = module.secrets.service_config_names["courses"]
  axon_server_url      = "${module.axon_server.service_name}:8124"

  depends_on = [module.axon_server]
}

module "enrollment_service" {
  source = "./modules/microservice"

  namespace            = var.namespace
  service_name         = "enrollment-service"
  image                = "mmisik/kotlin-seminar:enrollment-service"
  database_url         = "jdbc:postgresql://main-rw.cnpg-system.svc.cluster.local:5432/enrollment-service"
  database_secret_name = module.secrets.db_secret_names["enrollment"]
  config_map_name      = module.secrets.service_config_names["enrollment"]
  axon_server_url      = "${module.axon_server.service_name}:8124"

  depends_on = [module.axon_server]
}

module "forum_service" {
  source = "./modules/microservice"

  namespace            = var.namespace
  service_name         = "forum-service"
  image                = "mmisik/kotlin-seminar:forum-service"
  database_url         = "jdbc:postgresql://main-rw.cnpg-system.svc.cluster.local:5432/forum-service"
  database_secret_name = module.secrets.db_secret_names["forum"]
  config_map_name      = module.secrets.service_config_names["forum"]
  axon_server_url      = "${module.axon_server.service_name}:8124"

  depends_on = [module.axon_server]
}

module "assignment_service" {
  source = "./modules/microservice"

  namespace            = var.namespace
  service_name         = "assignment-service"
  image                = "mmisik/kotlin-seminar:assignment-service"
  database_url         = "jdbc:postgresql://main-rw.cnpg-system.svc.cluster.local:5432/assignment-service"
  database_secret_name = module.secrets.db_secret_names["assignment"]
  config_map_name      = module.secrets.service_config_names["assignment"]
  axon_server_url      = "${module.axon_server.service_name}:8124"

  depends_on = [module.axon_server]
}

# Ingress
module "ingress" {
  source = "./modules/ingress"

  namespace = var.namespace
  domain    = var.domain

  services = {
    courses    = module.courses_service.service_name
    enrollment = module.enrollment_service.service_name
    forum      = module.forum_service.service_name
    assignment = module.assignment_service.service_name
  }
}