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

resource "yandex_resourcemanager_folder_iam_member" "sa_storage_editor" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "storage.editor"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "sa_storage_uploader" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "storage.uploader"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "sa_storage_viewer" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "storage.viewer"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_iam_service_account_static_access_key" "sa_static_key" {
  service_account_id = yandex_iam_service_account.sa.id
}

resource "yandex_storage_bucket" "functions_bucket" {
  access_key = yandex_iam_service_account_static_access_key.sa_static_key.access_key
  secret_key = yandex_iam_service_account_static_access_key.sa_static_key.secret_key
  bucket     = "${var.project_name}-functions-bucket"
  folder_id  = yandex_resourcemanager_folder.folder.id
}

resource "yandex_lockbox_secret" "default_lockbox" {
  name      = "${var.project_name}-lockbox"
  folder_id = yandex_resourcemanager_folder.folder.id
}

resource "yandex_resourcemanager_folder_iam_member" "sa_lockbox_payloadViewer" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "lockbox.payloadViewer"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "sa_lockbox_viewer" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "lockbox.viewer"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

module "vpc" {
  source        = "github.com/terraform-yc-modules/terraform-yc-vpc.git"
  folder_id     = yandex_resourcemanager_folder.folder.id
  network_name  = "${var.project_name}-network"
  create_nat_gw = false
  private_subnets = [
    {
      name           = "ru-central1-a"
      zone           = "ru-central1-a"
      v4_cidr_blocks = ["10.10.0.0/24"]
    }
  ]
}

resource "yandex_vpc_security_group" "mysql_sg" {
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

resource "yandex_container_registry" "container_registry" {
  name      = "${var.project_name}-registry"
  folder_id = yandex_resourcemanager_folder.folder.id
  labels = {
    environment = var.environment
  }
}

resource "yandex_resourcemanager_folder_iam_member" "sa_container_registry_puller" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "container-registry.images.puller"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "sa_serverless_containerInvoker" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "serverless-containers.containerInvoker"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "sa_functionInvoker" {
  folder_id = yandex_resourcemanager_folder.folder.id
  role      = "functions.functionInvoker"
  member    = "serviceAccount:${yandex_iam_service_account.sa.id}"
}
