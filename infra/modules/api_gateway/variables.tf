variable "name_prefix" {
  type = string
}

variable "ec2_private_ip" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "vpc_link_security_group_id" {
  type = string
}

variable "log_group_arn" {
  type = string
}
