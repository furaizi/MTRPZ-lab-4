provider "aws" {
  region = var.aws_region
}

data "aws_caller_identity" "current" {}

module "iam" {
  source        = "./iam"
  aws_region    = var.aws_region
  aws_account_id = data.aws_caller_identity.current.account_id
}