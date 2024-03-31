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
