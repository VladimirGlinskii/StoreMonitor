resource "yandex_logging_group" "log_group" {
  name      = "${var.project_name}-${var.name}-logs"
  folder_id = var.folder_id
}

resource "yandex_storage_object" "package" {
  access_key  = var.functions_bucket.access_key
  secret_key  = var.functions_bucket.secret_key
  bucket      = var.functions_bucket.bucket
  key         = "${var.name}.zip"
  source      = "${var.functions_build_target_folder}${var.name}.zip"
  source_hash = filemd5("${var.functions_build_target_folder}${var.name}.zip")
}

resource "yandex_function" "function" {
  folder_id          = var.folder_id
  name               = "${var.project_name}-${var.name}"
  user_hash          = "${yandex_storage_object.package.source_hash}@1"
  runtime            = "java21"
  entrypoint         = var.entrypoint
  memory             = "256"
  execution_timeout  = var.execution_timeout
  service_account_id = var.service_account_id
  package {
    bucket_name = yandex_storage_object.package.bucket
    object_name = yandex_storage_object.package.key
  }
  secrets {
    id                   = var.lockbox_id
    version_id           = var.lockbox_version_id
    key                  = "DB_USERNAME"
    environment_variable = "DB_USERNAME"
  }
  secrets {
    id                   = var.lockbox_id
    version_id           = var.lockbox_version_id
    key                  = "DB_PASSWORD"
    environment_variable = "DB_PASSWORD"
  }
  dynamic "secrets" {
    for_each = var.secrets
    content {
      id                   = secrets.value["id"]
      version_id           = secrets.value["version_id"]
      key                  = secrets.value["key"]
      environment_variable = secrets.value["environment_variable"]
    }
  }
  environment = merge(
    {
      DB_URL = var.db_url
    },
    var.environment
  )
  log_options {
    log_group_id = yandex_logging_group.log_group.id
    min_level    = "INFO"
  }
}
