variable "name_prefix" {
  type = string
}

variable "environment" {
  type = string
}

variable "instance_id" {
  type = string
}

variable "alarm_email" {
  type    = string
  default = ""
}

variable "metrics_namespace" {
  type = string
}
