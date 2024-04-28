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

module "mysql" {
  source             = "github.com/terraform-yc-modules/terraform-yc-mysql"
  folder_id          = yandex_resourcemanager_folder.folder.id
  name               = "${var.project_name}-db"
  mysql_version      = "8.0"
  environment        = "PRODUCTION"
  resource_preset_id = "b1.medium"
  disk_size          = 10
  disk_type          = "network-hdd"
  network_id         = module.vpc.vpc_id
  security_groups_ids_list = [
    yandex_vpc_security_group.mysql_sg.id
  ]
  access_policy = {
    web_sql = true
  }
  databases = [{
    name = "base-api"
  }]
  hosts_definition = [{
    zone             = "ru-central1-a"
    assign_public_ip = true
  }]
  users = [{
    name     = var.db_user
    password = var.db_password
    permissions = [{
      database_name = "base-api"
    }]
  }]
}

resource "yandex_container_repository" "base_api_repository" {
  name = "${yandex_container_registry.container_registry.id}/${var.project_name}-base-api"
}

resource "yandex_container_repository_lifecycle_policy" "base_api_repository_lifecycle_policy" {
  name          = "${var.project_name}-base-api-repository-lifecycle"
  status        = "active"
  repository_id = yandex_container_repository.base_api_repository.id
  rule {
    untagged     = true
    retained_top = 1
  }
}

resource "yandex_logging_group" "base_api_logs" {
  name      = "${var.project_name}-base-api-logs"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_serverless_container" "base_api" {
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
    log_group_id = yandex_logging_group.base_api_logs.id
    min_level    = "INFO"
  }
}

module "auth_function" {
  source                        = "./tf-modules/function"
  folder_id                     = yandex_resourcemanager_folder.folder.id
  service_account_id            = yandex_iam_service_account.sa.id
  project_name                  = var.project_name
  name                          = "auth-function"
  entrypoint                    = "ru.vglinskii.storemonitor.authfunction.Handler"
  functions_build_target_folder = var.functions_build_target_folder
  functions_bucket              = yandex_storage_bucket.functions_bucket
  db_url                        = local.dbUrl
  lockbox_id                    = yandex_lockbox_secret.default_lockbox.id
  lockbox_version_id            = yandex_lockbox_secret_version.default_lockbox_version.id
}

module "create_incident_function" {
  source                        = "./tf-modules/function"
  folder_id                     = yandex_resourcemanager_folder.folder.id
  service_account_id            = yandex_iam_service_account.sa.id
  project_name                  = var.project_name
  name                          = "create-incident"
  entrypoint                    = "ru.vglinskii.storemonitor.createincident.Handler"
  functions_build_target_folder = var.functions_build_target_folder
  functions_bucket              = yandex_storage_bucket.functions_bucket
  db_url                        = local.dbUrl
  lockbox_id                    = yandex_lockbox_secret.default_lockbox.id
  lockbox_version_id            = yandex_lockbox_secret_version.default_lockbox_version.id
  execution_timeout             = 10
}

module "incidents_report_function" {
  source                        = "./tf-modules/function"
  folder_id                     = yandex_resourcemanager_folder.folder.id
  service_account_id            = yandex_iam_service_account.sa.id
  project_name                  = var.project_name
  name                          = "incidents-report-function"
  entrypoint                    = "ru.vglinskii.storemonitor.incidentsreport.Handler"
  functions_build_target_folder = var.functions_build_target_folder
  functions_bucket              = yandex_storage_bucket.functions_bucket
  db_url                        = local.dbUrl
  lockbox_id                    = yandex_lockbox_secret.default_lockbox.id
  lockbox_version_id            = yandex_lockbox_secret_version.default_lockbox_version.id
  execution_timeout             = 600
}

resource "yandex_api_gateway" "pa_api_gateway" {
  folder_id   = yandex_resourcemanager_folder.folder.id
  name        = "${var.project_name}-pa-api-gateway"
  description = "API Gateway for director's personal account"
  spec = templatefile("./pa-openapi.yaml", {
    service_account_id           = yandex_iam_service_account.sa.id,
    base_api_container_id        = yandex_serverless_container.base_api.id,
    create_incident_function_id  = module.create_incident_function.id,
    incidents_report_function_id = module.incidents_report_function.id,
    auth_function_id             = module.auth_function.id
  })
}

module "cashier_simulator" {
  source                        = "./tf-modules/function"
  folder_id                     = yandex_resourcemanager_folder.folder.id
  service_account_id            = yandex_iam_service_account.sa.id
  project_name                  = var.project_name
  name                          = "cashier-simulator"
  entrypoint                    = "ru.vglinskii.storemonitor.cashiersimulator.Handler"
  functions_build_target_folder = var.functions_build_target_folder
  functions_bucket              = yandex_storage_bucket.functions_bucket
  db_url                        = local.dbUrl
  lockbox_id                    = yandex_lockbox_secret.default_lockbox.id
  lockbox_version_id            = yandex_lockbox_secret_version.default_lockbox_version.id
  execution_timeout             = 600
  environment = {
    BASE_API_URL = local.baseApiUrl
  }
}

resource "yandex_function_trigger" "cashier_simulator_trigger" {
  folder_id = yandex_resourcemanager_folder.folder.id
  name      = "${var.project_name}-cashier-simulator-trigger"
  timer {
    cron_expression = "0/20 * ? * * *"
  }
  function {
    id                 = module.cashier_simulator.id
    service_account_id = yandex_iam_service_account.sa.id
  }
}

module "update_sensor_value_function" {
  source                        = "./tf-modules/function"
  folder_id                     = yandex_resourcemanager_folder.folder.id
  service_account_id            = yandex_iam_service_account.sa.id
  project_name                  = var.project_name
  name                          = "update-sensor-value"
  entrypoint                    = "ru.vglinskii.storemonitor.updatesensorvalue.Handler"
  functions_build_target_folder = var.functions_build_target_folder
  functions_bucket              = yandex_storage_bucket.functions_bucket
  db_url                        = local.dbUrl
  lockbox_id                    = yandex_lockbox_secret.default_lockbox.id
  lockbox_version_id            = yandex_lockbox_secret_version.default_lockbox_version.id
  execution_timeout             = 600
}

resource "yandex_api_gateway" "devices_api_gateway" {
  folder_id   = yandex_resourcemanager_folder.folder.id
  name        = "${var.project_name}-devices-api-gateway"
  description = "API Gateway for devices"
  spec = templatefile("./devices-openapi.yaml", {
    service_account_id              = yandex_iam_service_account.sa.id,
    update_sensor_value_function_id = module.update_sensor_value_function.id
  })
}

module "sensor_simulator" {
  source                        = "./tf-modules/function"
  folder_id                     = yandex_resourcemanager_folder.folder.id
  service_account_id            = yandex_iam_service_account.sa.id
  project_name                  = var.project_name
  name                          = "sensor-simulator"
  entrypoint                    = "ru.vglinskii.storemonitor.sensorsimulator.Handler"
  functions_build_target_folder = var.functions_build_target_folder
  functions_bucket              = yandex_storage_bucket.functions_bucket
  db_url                        = local.dbUrl
  lockbox_id                    = yandex_lockbox_secret.default_lockbox.id
  lockbox_version_id            = yandex_lockbox_secret_version.default_lockbox_version.id
  execution_timeout             = 600
  environment = {
    DEVICES_API_URL                         = local.devicesApiUrl
    SENSOR_VALUE_CELSIUS_MEAN               = "-3"
    SENSOR_VALUE_CELSIUS_STANDARD_DEVIATION = "3"
  }
}

resource "yandex_function_trigger" "sensor_simulator_trigger" {
  folder_id = yandex_resourcemanager_folder.folder.id
  name      = "${var.project_name}-sensor-simulator-trigger"
  timer {
    cron_expression = "0/10 * ? * * *"
  }
  function {
    id                 = module.sensor_simulator.id
    service_account_id = yandex_iam_service_account.sa.id
  }
}

resource "yandex_storage_bucket" "decommissioned_reports_files_bucket" {
  access_key = yandex_iam_service_account_static_access_key.sa_static_key.access_key
  secret_key = yandex_iam_service_account_static_access_key.sa_static_key.secret_key
  bucket     = "${var.project_name}-decommissioned-reports-bucket"
  folder_id  = yandex_resourcemanager_folder.folder.id
  max_size   = 10737418240
}

module "decommissioned_report_simulator" {
  source                        = "./tf-modules/function"
  folder_id                     = yandex_resourcemanager_folder.folder.id
  service_account_id            = yandex_iam_service_account.sa.id
  project_name                  = var.project_name
  name                          = "decommissioned-report-simulator"
  entrypoint                    = "ru.vglinskii.storemonitor.decommissionedreportsimulator.Handler"
  functions_build_target_folder = var.functions_build_target_folder
  functions_bucket              = yandex_storage_bucket.functions_bucket
  db_url                        = local.dbUrl
  lockbox_id                    = yandex_lockbox_secret.default_lockbox.id
  lockbox_version_id            = yandex_lockbox_secret_version.default_lockbox_version.id
  execution_timeout             = 600
  environment = {
    BUCKET_NAME                            = yandex_storage_bucket.decommissioned_reports_files_bucket.bucket
    SA_ACCESS_KEY                          = yandex_iam_service_account_static_access_key.sa_static_key.access_key
    SA_SECRET_KEY                          = yandex_iam_service_account_static_access_key.sa_static_key.secret_key
    MAX_COMMODITIES_FOR_DECOMMISSION_COUNT = "20"
  }
}

resource "yandex_function_trigger" "decommissioned_report_simulator_trigger" {
  folder_id = yandex_resourcemanager_folder.folder.id
  name      = "${var.project_name}-decommissioned-report-simulator-trigger"
  timer {
    cron_expression = "0 0 ? * * *"
  }
  function {
    id                 = module.decommissioned_report_simulator.id
    service_account_id = yandex_iam_service_account.sa.id
  }
}
