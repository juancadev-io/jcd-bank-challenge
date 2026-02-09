variable "name_prefix" {
  type = string
}

variable "environment" {
  type = string
}

variable "cluster_name" {
  type = string
}

variable "service_name" {
  type = string
}

variable "alarm_email" {
  type    = string
  default = ""
}
