variable "cloud_id" {
  description = "ID of the project cloud"
  type        = string
  default     = "b1gnf2g79ev07fe5a60r"
}

variable "project_name" {
  description = "Name of the project in kebab-case"
  type        = string
  default     = "store-monitor"
}

variable "reviewers" {
  description = "Users who will be granted viewer access to the project folder"
  type        = list(string)
  default = [
    "userAccount:ajeljdedlbt6jjbuc38n",
    "userAccount:ajedvg94gct2uubqb9rh"
  ]
}

variable "db_user" {
  description = "Database user"
  type        = string
  default     = "vglinskii"
}

variable "db_password" {
  description = "Database password"
  type        = string
  default     = "adminadmin"
}

variable "environment" {
  description = "App environment"
  type        = string
  default     = "development"
}

variable "base_api_image_digest" {
  description = "Hash of Base API docker image"
  type        = string
  default     = "sha256:07fcd89382b860e25f604d921b7ad33110648ee43ae426348559a824e978d3a9"
}

variable "functions_code_folder" {
  description = "Path to folder with functions' zip archives"
  type        = string
  default     = "../functions-build/"
}
