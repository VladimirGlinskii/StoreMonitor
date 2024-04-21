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
    text_value = var.db_user
  }
  entries {
    key        = "DB_PASSWORD"
    text_value = var.db_password
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
  name       = var.db_user
  password   = var.db_password
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
    environment = var.environment
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
  name = "${yandex_container_registry.container_registry.id}/${var.project_name}-base-api"
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
    url    = "cr.yandex/${yandex_container_repository.base_api_repository.name}:${var.environment}"
    digest = var.base_api_image_digest
    environment = {
      DB_URL = local.dbUrl
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
  source      = "${var.functions_code_folder}auth-function.zip"
  source_hash = filemd5("${var.functions_code_folder}auth-function.zip")
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
    DB_URL = local.dbUrl
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
  /api/sensors/{proxy+}:
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
  /api/decommissioned-reports:
    get:
      x-yc-apigateway-integration:
        type: serverless_containers
        container_id: ${yandex_serverless_container.base-api.id}
        service_account_id: ${yandex_iam_service_account.sa.id}
      x-yc-apigateway-validator:
        $ref: "#/components/x-yc-apigateway-validators/request-params-validator"
      parameters:
        - in: query
          name: from
          required: true
          schema:
            type: string
            format: date-time
        - in: query
          name: to
          required: true
          schema:
            type: string
            format: date-time
  /api/incidents:
    post:
      x-yc-apigateway-integration:
        type: cloud_functions
        function_id: ${yandex_function.create-incident-function.id}
        service_account_id: ${yandex_iam_service_account.sa.id}
    get:
      x-yc-apigateway-integration:
        type: cloud_functions
        function_id: ${yandex_function.incidents-report-function.id}
        service_account_id: ${yandex_iam_service_account.sa.id}
      x-yc-apigateway-validator:
        $ref: "#/components/x-yc-apigateway-validators/request-params-validator"
      parameters:
        - in: query
          name: from
          required: true
          schema:
            type: string
            format: date-time
        - in: query
          name: to
          required: true
          schema:
            type: string
            format: date-time
            
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
  x-yc-apigateway-validators:
    request-params-validator:
      validateRequestParameters: true
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
  source      = "${var.functions_code_folder}cashier-simulator.zip"
  source_hash = filemd5("${var.functions_code_folder}cashier-simulator.zip")
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
    DB_URL       = local.dbUrl
    BASE_API_URL = local.baseApiUrl
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

resource "yandex_logging_group" "update-sensor-value-function_log" {
  name      = "${var.project_name}-update-sensor-value-function"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_storage_object" "update-sensor-value-function-package" {
  access_key  = yandex_storage_bucket.functions-bucket.access_key
  secret_key  = yandex_storage_bucket.functions-bucket.secret_key
  bucket      = yandex_storage_bucket.functions-bucket.bucket
  key         = "update-sensor-value.zip"
  source      = "${var.functions_code_folder}update-sensor-value.zip"
  source_hash = filemd5("${var.functions_code_folder}update-sensor-value.zip")
}

resource "yandex_function" "update-sensor-value-function" {
  folder_id          = yandex_resourcemanager_folder.folder.id
  name               = "${var.project_name}-update-sensor-value-function"
  user_hash          = "${yandex_storage_object.update-sensor-value-function-package.source_hash}@1"
  runtime            = "java21"
  entrypoint         = "ru.vglinskii.storemonitor.updatesensorvalue.Handler"
  memory             = "256"
  service_account_id = yandex_iam_service_account.sa.id
  execution_timeout  = 600
  package {
    bucket_name = yandex_storage_object.update-sensor-value-function-package.bucket
    object_name = yandex_storage_object.update-sensor-value-function-package.key
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
    DB_URL = local.dbUrl
  }
  log_options {
    log_group_id = yandex_logging_group.update-sensor-value-function_log.id
    min_level    = "INFO"
  }
}

resource "yandex_api_gateway" "devices-api-gateway" {
  folder_id   = yandex_resourcemanager_folder.folder.id
  name        = "${var.project_name}-devices-api-gateway"
  description = "API Gateway for devices"
  spec        = <<-EOT
openapi: "3.0.0"
info:
  version: 0.5.0
  title: Devices API
paths:
  /api/sensors/values:
    post:
      x-yc-apigateway-integration:
        type: cloud_functions
        function_id: ${yandex_function.update-sensor-value-function.id}
        service_account_id: ${yandex_iam_service_account.sa.id}
EOT
}

resource "yandex_logging_group" "sensor-simulator_log" {
  name      = "${var.project_name}-sensor-simulator"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_storage_object" "sensor-simulator-package" {
  access_key  = yandex_storage_bucket.functions-bucket.access_key
  secret_key  = yandex_storage_bucket.functions-bucket.secret_key
  bucket      = yandex_storage_bucket.functions-bucket.bucket
  key         = "sensor-simulator.zip"
  source      = "${var.functions_code_folder}sensor-simulator.zip"
  source_hash = filemd5("${var.functions_code_folder}sensor-simulator.zip")
}

resource "yandex_function" "sensor-simulator-function" {
  folder_id          = yandex_resourcemanager_folder.folder.id
  name               = "${var.project_name}-sensor-simulator"
  user_hash          = "${yandex_storage_object.sensor-simulator-package.source_hash}@1"
  runtime            = "java21"
  entrypoint         = "ru.vglinskii.storemonitor.sensorsimulator.Handler"
  memory             = "256"
  service_account_id = yandex_iam_service_account.sa.id
  execution_timeout  = 600
  package {
    bucket_name = yandex_storage_object.sensor-simulator-package.bucket
    object_name = yandex_storage_object.sensor-simulator-package.key
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
    DB_URL                                  = local.dbUrl
    DEVICES_API_URL                         = local.devicesApiUrl
    SENSOR_VALUE_CELSIUS_MEAN               = "-3"
    SENSOR_VALUE_CELSIUS_STANDARD_DEVIATION = "6"
  }
  log_options {
    log_group_id = yandex_logging_group.sensor-simulator_log.id
    min_level    = "INFO"
  }
}

resource "yandex_function_trigger" "sensor-simulator_trigger" {
  folder_id = yandex_resourcemanager_folder.folder.id
  name      = "${var.project_name}-sensor-simulator-trigger"
  timer {
    cron_expression = "0/10 * ? * * *"
  }
  function {
    id                 = yandex_function.sensor-simulator-function.id
    service_account_id = yandex_iam_service_account.sa.id
  }
}

resource "yandex_logging_group" "create-incident-function_log" {
  name      = "${var.project_name}-create-incident-function"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_storage_object" "create-incident-function-package" {
  access_key  = yandex_storage_bucket.functions-bucket.access_key
  secret_key  = yandex_storage_bucket.functions-bucket.secret_key
  bucket      = yandex_storage_bucket.functions-bucket.bucket
  key         = "create-incident.zip"
  source      = "${var.functions_code_folder}create-incident.zip"
  source_hash = filemd5("${var.functions_code_folder}create-incident.zip")
}

resource "yandex_function" "create-incident-function" {
  folder_id          = yandex_resourcemanager_folder.folder.id
  name               = "${var.project_name}-create-incident-function"
  user_hash          = "${yandex_storage_object.create-incident-function-package.source_hash}@1"
  runtime            = "java21"
  entrypoint         = "ru.vglinskii.storemonitor.createincident.Handler"
  memory             = "256"
  service_account_id = yandex_iam_service_account.sa.id
  execution_timeout  = 600
  package {
    bucket_name = yandex_storage_object.create-incident-function-package.bucket
    object_name = yandex_storage_object.create-incident-function-package.key
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
    DB_URL = local.dbUrl
  }
  log_options {
    log_group_id = yandex_logging_group.create-incident-function_log.id
    min_level    = "INFO"
  }
}

resource "yandex_logging_group" "incidents-report-function_log" {
  name      = "${var.project_name}-incidents-report-function"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_storage_object" "incidents-report-function-package" {
  access_key  = yandex_storage_bucket.functions-bucket.access_key
  secret_key  = yandex_storage_bucket.functions-bucket.secret_key
  bucket      = yandex_storage_bucket.functions-bucket.bucket
  key         = "incidents-report-function.zip"
  source      = "${var.functions_code_folder}incidents-report-function.zip"
  source_hash = filemd5("${var.functions_code_folder}incidents-report-function.zip")
}

resource "yandex_function" "incidents-report-function" {
  folder_id          = yandex_resourcemanager_folder.folder.id
  name               = "${var.project_name}-incidents-report-function"
  user_hash          = "${yandex_storage_object.incidents-report-function-package.source_hash}@1"
  runtime            = "java21"
  entrypoint         = "ru.vglinskii.storemonitor.incidentsreport.Handler"
  memory             = "256"
  service_account_id = yandex_iam_service_account.sa.id
  execution_timeout  = 600
  package {
    bucket_name = yandex_storage_object.incidents-report-function-package.bucket
    object_name = yandex_storage_object.incidents-report-function-package.key
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
    DB_URL = local.dbUrl
  }
  log_options {
    log_group_id = yandex_logging_group.incidents-report-function_log.id
    min_level    = "INFO"
  }
}

resource "yandex_storage_bucket" "decommissioned-reports-bucket" {
  access_key = yandex_iam_service_account_static_access_key.sa-static-key.access_key
  secret_key = yandex_iam_service_account_static_access_key.sa-static-key.secret_key
  bucket     = "${var.project_name}-decommissioned-reports-bucket"
  folder_id  = yandex_resourcemanager_folder.folder.id
  max_size   = 10737418240
}

resource "yandex_logging_group" "decommissioned-report-simulator_log" {
  name      = "${var.project_name}-decommissioned-report-simulator"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_storage_object" "decommissioned-report-simulator-package" {
  access_key  = yandex_storage_bucket.functions-bucket.access_key
  secret_key  = yandex_storage_bucket.functions-bucket.secret_key
  bucket      = yandex_storage_bucket.functions-bucket.bucket
  key         = "decommissioned-report-simulator.zip"
  source      = "${var.functions_code_folder}decommissioned-report-simulator.zip"
  source_hash = filemd5("${var.functions_code_folder}decommissioned-report-simulator.zip")
}

resource "yandex_function" "decommissioned-report-simulator-function" {
  folder_id          = yandex_resourcemanager_folder.folder.id
  name               = "${var.project_name}-decommissioned-report-simulator"
  user_hash          = "${yandex_storage_object.decommissioned-report-simulator-package.source_hash}@1"
  runtime            = "java21"
  entrypoint         = "ru.vglinskii.storemonitor.decommissionedreportsimulator.Handler"
  memory             = "256"
  service_account_id = yandex_iam_service_account.sa.id
  execution_timeout  = 600
  package {
    bucket_name = yandex_storage_object.decommissioned-report-simulator-package.bucket
    object_name = yandex_storage_object.decommissioned-report-simulator-package.key
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
    DB_URL                                 = local.dbUrl
    BUCKET_NAME                            = yandex_storage_bucket.decommissioned-reports-bucket.bucket
    SA_ACCESS_KEY                          = yandex_iam_service_account_static_access_key.sa-static-key.access_key
    SA_SECRET_KEY                          = yandex_iam_service_account_static_access_key.sa-static-key.secret_key
    MAX_COMMODITIES_FOR_DECOMMISSION_COUNT = "20"
  }
  log_options {
    log_group_id = yandex_logging_group.decommissioned-report-simulator_log.id
    min_level    = "INFO"
  }
}

resource "yandex_function_trigger" "decommissioned-report-simulator_trigger" {
  folder_id = yandex_resourcemanager_folder.folder.id
  name      = "${var.project_name}-decommissioned-report-simulator-trigger"
  timer {
    cron_expression = "0 0 ? * * *"
  }
  function {
    id                 = yandex_function.decommissioned-report-simulator-function.id
    service_account_id = yandex_iam_service_account.sa.id
  }
}
