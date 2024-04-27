variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "folder_id" {
  description = "Folder id that contains the cloud function"
  type        = string
}

variable "service_account_id" {
  description = "Service account ID for the cloud function"
  type        = string
}

variable "name" {
  description = "Name of the cloud function that should be equal to folder name inside functions-parent"
  type        = string
}

variable "entrypoint" {
  description = "Full name of the Handler class"
  type        = string
}

variable "functions_build_target_folder" {
  description = "Path to folder with functions' zip archives"
  type        = string
}

variable "execution_timeout" {
  description = "Execution timeout of function in seconds"
  type        = number
  default     = 10
}

variable "db_url" {
  description = "Database url for the cloud function"
  type        = string
}

variable "functions_bucket" {
  description = "Bucket which contains builded cloud function"
  type = object({
    access_key = string
    secret_key = string
    bucket     = string
  })
}

variable "lockbox_id" {
  description = "ID of the lockbox which contains DB_USERNAME and DB_PASSWORD secrets"
  type        = string
}

variable "lockbox_version_id" {
  description = "Version ID for the lockbox"
  type        = string
}

variable "environment" {
  description = "Additional env variables for the cloud dunction"
  type        = map(string)
  default     = {}
}

variable "secrets" {
  description = "Additional secrets for the cloud dunction"
  type = list(object({
    id                   = string
    version_id           = string
    key                  = string
    environment_variable = string
  }))
  default = []
}
