data "aws_caller_identity" "current" {}

provider "aws" {
  region = var.aws_region
}

# 1) Таблиця ShortLinks
resource "aws_dynamodb_table" "shortlinks" {
  name           = "ShortLinks"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "shortCode"

  attribute {
    name = "shortCode"
    type = "S"
  }

  # Point-in-time recovery
  point_in_time_recovery {
    enabled = true
  }
}

# 2) Таблиця Analytics
resource "aws_dynamodb_table" "analytics" {
  name           = "Analytics"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "shortCode"
  range_key      = "timestamp"

  attribute {
    name = "shortCode"
    type = "S"
  }
  attribute {
    name = "timestamp"
    type = "S"
  }

  # TTL для автоматичного видалення старих записів
  ttl {
    attribute_name = "expiresAt"
    enabled        = true
  }

  point_in_time_recovery {
    enabled = true
  }
}
