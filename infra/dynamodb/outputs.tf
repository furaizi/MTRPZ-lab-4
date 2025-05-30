output "shortlinks_table_arn" {
  description = "The ARN of the ShortLinks DynamoDB table"
  value       = aws_dynamodb_table.shortlinks.arn
}

output "analytics_table_arn" {
  description = "The ARN of the Analytics DynamoDB table"
  value       = aws_dynamodb_table.analytics.arn
}

output "shortlinks_table_name" {
  description = "The name of the ShortLinks DynamoDB table"
  value       = aws_dynamodb_table.shortlinks.name
}

output "analytics_table_name" {
  description = "The name of the Analytics DynamoDB table"
  value       = aws_dynamodb_table.analytics.name
}