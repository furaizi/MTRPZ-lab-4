variable "aws_account_id" {
  description = "The AWS Account ID"
  type        = string
}

variable "aws_region" {
  description = "The AWS Region"
  type        = string
}

variable "shortlinks_table_arn" {
  description = "The ARN of the ShortLinks DynamoDB table"
  type        = string
}

variable "analytics_table_arn" {
  description = "The ARN of the Analytics DynamoDB table"
  type        = string
}