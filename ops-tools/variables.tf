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
  default     = "sha256:02064538ab7544c039fd1311bd4bcf6c9e6b87bed0e64050666d91180b272018"
}

variable "functions_code_folder" {
  description = "Path to folder with functions' zip archives"
  type        = string
  default     = "../functions-build/"
}
