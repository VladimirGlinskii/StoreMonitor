resource "yandex_resourcemanager_folder" "folder" {
  name     = var.project_name
  cloud_id = var.cloud_id
}

resource "yandex_resourcemanager_folder_iam_binding" "viewer" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "viewer"
  members   = var.reviewers
}

resource "yandex_iam_service_account" "sa" {
  name      = "${var.project_name}-service-account"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_resourcemanager_folder_iam_member" "sa-storage-editor" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "storage.editor"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_iam_service_account_static_access_key" "sa-static-key" {
  service_account_id = yandex_iam_service_account.sa.id
}

resource "yandex_storage_bucket" "functions-bucket" {
  access_key = yandex_iam_service_account_static_access_key.sa-static-key.access_key
  secret_key = yandex_iam_service_account_static_access_key.sa-static-key.secret_key
  bucket     = "${var.project_name}-functions-bucket"
  folder_id  = yandex_resourcemanager_folder.folder.id
}

resource "yandex_resourcemanager_folder_iam_member" "sa-storage-uploader" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "storage.uploader"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "sa-storage-viewer" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "storage.viewer"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_lockbox_secret" "default_lockbox" {
  name      = "${var.project_name}-lockbox"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_resourcemanager_folder_iam_member" "sa-lockbox-payloadViewer" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "lockbox.payloadViewer"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "sa-lockbox-viewer" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "lockbox.viewer"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_lockbox_secret_version" "default_lockbox_version" {
  secret_id   = yandex_lockbox_secret.default_lockbox.id
  description = "main"
  entries {
    key        = "DB_USERNAME"
    text_value = "vglinskii"
  }
  entries {
    key        = "DB_PASSWORD"
    text_value = "adminadmin"
  }
}

resource "yandex_vpc_security_group" "mysql-sg" {
  name       = "MySQL Security Group"
  network_id = module.vpc.vpc_id
  folder_id  = yandex_resourcemanager_folder.folder.id

  ingress {
    protocol       = "TCP"
    port           = 3306
    v4_cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    protocol       = "ANY"
    description    = "To internet"
    v4_cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "yandex_mdb_mysql_cluster" "db-cluster" {
  name        = "${var.project_name}-db"
  environment = "PRODUCTION"
  network_id  = module.vpc.vpc_id
  version     = "8.0"
  folder_id   = yandex_resourcemanager_folder.folder.id
  security_group_ids = [
    yandex_vpc_security_group.mysql-sg.id
  ]

  resources {
    resource_preset_id = "b1.medium"
    disk_type_id       = "network-ssd"
    disk_size          = 10
  }

  host {
    zone             = "ru-central1-a"
    assign_public_ip = true
  }

  access {
    web_sql = true
  }
}

resource "yandex_mdb_mysql_database" "base-api-db" {
  cluster_id = yandex_mdb_mysql_cluster.db-cluster.id
  name       = "base-api"
}

resource "yandex_mdb_mysql_user" "base-api-db-admin" {
  cluster_id = yandex_mdb_mysql_cluster.db-cluster.id
  name       = "vglinskii"
  password   = "adminadmin"
  permission {
    database_name = yandex_mdb_mysql_database.base-api-db.name
    roles         = ["ALL"]
  }

  connection_limits {
    max_user_connections = 50
  }
}

resource "yandex_container_registry" "container_registry" {
  name      = "${var.project_name}-registry"
  folder_id = yandex_resourcemanager_folder.folder.id
  labels = {
    environment = "development"
  }
}

resource "yandex_resourcemanager_folder_iam_member" "sa-container-registry-puller" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "container-registry.images.puller"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "sa-serverless-containers-invoker" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "serverless-containers.containerInvoker"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_container_repository" "base_api_repository" {
  name = "${yandex_container_registry.container_registry.id}/store-monitor-base-api"
}

resource "yandex_container_repository_lifecycle_policy" "container_repository_lifecycle_policy" {
  name          = "${var.project_name}-base-api-repository-lifecycle"
  status        = "active"
  repository_id = yandex_container_repository.base_api_repository.id
  rule {
    untagged     = true
    retained_top = 1
  }
}

resource "yandex_logging_group" "base_api_log" {
  name      = "${var.project_name}-base-api-logs"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_serverless_container" "base-api" {
  name               = "${var.project_name}-base-api"
  folder_id          = yandex_resourcemanager_folder.folder.id
  description        = "Base API"
  memory             = 512
  execution_timeout  = "15s"
  cores              = 1
  core_fraction      = 100
  service_account_id = yandex_iam_service_account.sa.id
  secrets {
    id                   = yandex_lockbox_secret.default_lockbox.id
    version_id           = yandex_lockbox_secret_version.default_lockbox_version.id
    key                  = "DB_USERNAME"
    environment_variable = "DB_USERNAME"
  }
  secrets {
    id                   = yandex_lockbox_secret.default_lockbox.id
    version_id           = yandex_lockbox_secret_version.default_lockbox_version.id
    key                  = "DB_PASSWORD"
    environment_variable = "DB_PASSWORD"
  }
  image {
    url    = "cr.yandex/${yandex_container_repository.base_api_repository.name}:development"
    digest = "sha256:10b46ca716889ac697da4632c7b3fcf1c2a19aae9e6ce3a84d80deb1bf93621b"
    environment = {
      DB_URL = "jdbc:mysql://${yandex_mdb_mysql_cluster.db-cluster.host[0].fqdn}:3306/base-api?useSSL=true"
    }
  }
  log_options {
    log_group_id = yandex_logging_group.base_api_log.id
    min_level    = "INFO"
  }
}

resource "yandex_resourcemanager_folder_iam_member" "sa-functionInvoker" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "functions.functionInvoker"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_logging_group" "auth-function_log" {
  name      = "${var.project_name}-auth-function-logs"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_storage_object" "auth-function-package" {
  access_key  = yandex_storage_bucket.functions-bucket.access_key
  secret_key  = yandex_storage_bucket.functions-bucket.secret_key
  bucket      = yandex_storage_bucket.functions-bucket.bucket
  key         = "auth-function.zip"
  source      = "../functions-build/auth-function.zip"
  source_hash = filemd5("../functions-build/auth-function.zip")
}

resource "yandex_function" "auth-function" {
  folder_id          = yandex_resourcemanager_folder.folder.id
  name               = "${var.project_name}-auth-function"
  user_hash          = "${yandex_storage_object.auth-function-package.source_hash}@1"
  runtime            = "java21"
  entrypoint         = "ru.vglinskii.storemonitor.authfunction.Handler"
  memory             = "256"
  execution_timeout  = "10"
  service_account_id = yandex_iam_service_account.sa.id
  package {
    bucket_name = yandex_storage_object.auth-function-package.bucket
    object_name = yandex_storage_object.auth-function-package.key
  }
  secrets {
    id                   = yandex_lockbox_secret.default_lockbox.id
    version_id           = yandex_lockbox_secret_version.default_lockbox_version.id
    key                  = "DB_USERNAME"
    environment_variable = "DB_USERNAME"
  }
  secrets {
    id                   = yandex_lockbox_secret.default_lockbox.id
    version_id           = yandex_lockbox_secret_version.default_lockbox_version.id
    key                  = "DB_PASSWORD"
    environment_variable = "DB_PASSWORD"
  }
  environment = {
    DB_URL = "jdbc:mysql://${yandex_mdb_mysql_cluster.db-cluster.host[0].fqdn}:3306/base-api"
  }
  log_options {
    log_group_id = yandex_logging_group.auth-function_log.id
    min_level    = "INFO"
  }
}

resource "yandex_api_gateway" "pa-api-gateway" {
  folder_id   = yandex_resourcemanager_folder.folder.id
  name        = "${var.project_name}-pa-api-gateway"
  description = "API Gateway for director's personal account"
  spec        = <<-EOT
openapi: "3.0.0"
info:
  version: 0.5.0
  title: Personal Account API
paths:
  /api/cash-registers/{proxy+}:
    x-yc-apigateway-any-method:
      x-yc-apigateway-integration:
        type: serverless_containers
        container_id: ${yandex_serverless_container.base-api.id}
        service_account_id: ${yandex_iam_service_account.sa.id}
      parameters:
        - name: proxy
          in: path
          explode: false
          required: false
          schema:
            default: '-'
            type: string
          style: simple
components:
  securitySchemes:
    apiKeyAuth:
      type: apiKey
      in: header
      name: X-SECRET-KEY
      x-yc-apigateway-authorizer:
        type: function
        function_id: ${yandex_function.auth-function.id}
        service_account_id: ${yandex_iam_service_account.sa.id}
security:
  - apiKeyAuth: []
EOT
}

resource "yandex_logging_group" "cashier-simulator_log" {
  name      = "${var.project_name}-cashier-simulator-logs"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_storage_object" "cashier-simulator-package" {
  access_key  = yandex_storage_bucket.functions-bucket.access_key
  secret_key  = yandex_storage_bucket.functions-bucket.secret_key
  bucket      = yandex_storage_bucket.functions-bucket.bucket
  key         = "cashier-simulator.zip"
  source      = "../functions-build/cashier-simulator.zip"
  source_hash = filemd5("../functions-build/cashier-simulator.zip")
}

resource "yandex_function" "cashier-simulator-function" {
  folder_id          = yandex_resourcemanager_folder.folder.id
  name               = "${var.project_name}-cashier-simulator"
  user_hash          = "${yandex_storage_object.cashier-simulator-package.source_hash}@1"
  runtime            = "java21"
  entrypoint         = "ru.vglinskii.storemonitor.cashiersimulator.Handler"
  memory             = "256"
  service_account_id = yandex_iam_service_account.sa.id
  execution_timeout  = 600
  package {
    bucket_name = yandex_storage_object.cashier-simulator-package.bucket
    object_name = yandex_storage_object.cashier-simulator-package.key
  }
  secrets {
    id                   = yandex_lockbox_secret.default_lockbox.id
    version_id           = yandex_lockbox_secret_version.default_lockbox_version.id
    key                  = "DB_USERNAME"
    environment_variable = "DB_USERNAME"
  }
  secrets {
    id                   = yandex_lockbox_secret.default_lockbox.id
    version_id           = yandex_lockbox_secret_version.default_lockbox_version.id
    key                  = "DB_PASSWORD"
    environment_variable = "DB_PASSWORD"
  }
  environment = {
    DB_URL       = "jdbc:mysql://${yandex_mdb_mysql_cluster.db-cluster.host[0].fqdn}:3306/base-api"
    BASE_API_URL = "https://${yandex_api_gateway.pa-api-gateway.domain}/api"
  }
  log_options {
    log_group_id = yandex_logging_group.cashier-simulator_log.id
    min_level    = "INFO"
  }
}

resource "yandex_function_trigger" "cashier-simulator_trigger" {
  folder_id = yandex_resourcemanager_folder.folder.id
  name      = "${var.project_name}-cashier-simulator-trigger"
  timer {
    cron_expression = "0/20 * ? * * *"
  }
  function {
    id                 = yandex_function.cashier-simulator-function.id
    service_account_id = yandex_iam_service_account.sa.id
  }
}
