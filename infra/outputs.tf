output "lambda_exec_role_arn" {
  description = "The ARN of the IAM role for Lambda execution"
  value       = module.iam.lambda_exec_role_arn
}

output "lambda_exec_role_name" {
  description = "The name of the IAM role for Lambda execution"
  value       = module.iam.lambda_exec_role_name
}