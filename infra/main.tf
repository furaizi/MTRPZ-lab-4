provider "aws" {
  region = var.aws_region
}

data "aws_caller_identity" "current" {}

module "dynamodb" {
  aws_region = var.aws_region
  aws_account_id = data.aws_caller_identity.current.account_id
  source = "./dynamodb"
}

module "iam" {
  source              = "./iam"
  aws_region         = var.aws_region
  aws_account_id      = data.aws_caller_identity.current.account_id
  shortlinks_table_arn = module.dynamodb.shortlinks_table_arn
  analytics_table_arn  = module.dynamodb.analytics_table_arn
}

module "api" {
  source              = "./api"
  aws_region         = var.aws_region
  shortlinks_table_name = module.dynamodb.shortlinks_table_name
  analytics_table_name  = module.dynamodb.analytics_table_name
  lambda_exec_role_arn = module.iam.lambda_exec_role_arn
}