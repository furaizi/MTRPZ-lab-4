variable "aws_region" {
  description = "The AWS region where resources will be created"
  type        = string
}

variable "lambda_exec_role_arn" {
  description = "The ARN of the Lambda execution role"
  type        = string
}


variable "shortlinks_table_name" {
  description = "The name of the ShortLinks DynamoDB table"
  type        = string
}

variable "analytics_table_name" {
  description = "The name of the Analytics DynamoDB table"
  type        = string
}