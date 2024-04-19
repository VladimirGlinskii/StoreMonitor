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
