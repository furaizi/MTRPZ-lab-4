data "aws_caller_identity" "current" {}

provider "aws" {
  region = var.aws_region
}

##### 1) HTTP API Gateway #####
resource "aws_apigatewayv2_api" "shortener_api" {
  name          = "url-shortener-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["*"]
    allow_methods = ["GET", "POST", "OPTIONS", "DELETE"]
    allow_headers = ["*"]
  }
}

##### 2) Lambda Functions #####
resource "aws_lambda_function" "shorten" {
  function_name = "shorten-url"
  handler       = "com.example.ShortenHandler::handleRequest"
  runtime       = "java21"
  role          = var.lambda_exec_role_arn
  filename      = "../build/distributions/shortener.zip"
  memory_size   = 128
  timeout       = 10

  environment {
    variables = {
      TABLE_SHORTLINKS = var.shortlinks_table_name
      TABLE_ANALYTICS  = var.analytics_table_name
    }
  }

  tracing_config {
    mode = "Active"
  }
}

resource "aws_lambda_function" "redirect" {
  function_name = "redirect-url"
  handler       = "com.example.RedirectHandler::handleRequest"
  runtime       = "java21"
  role          = var.lambda_exec_role_arn
  filename      = "../build/distributions/shortener.zip"
  memory_size   = 128
  timeout       = 10

  environment {
    variables = {
      TABLE_SHORTLINKS = var.shortlinks_table_name
      TABLE_ANALYTICS  = var.analytics_table_name
    }
  }
}

resource "aws_lambda_function" "get_link" {
  function_name = "get-link"
  handler       = "com.example.GetLinkHandler::handleRequest"
  runtime       = "java21"
  role          = var.lambda_exec_role_arn
  filename      = "../build/distributions/shortener.zip"
  memory_size   = 128
  timeout       = 10

  environment {
    variables = {
      TABLE_SHORTLINKS = var.shortlinks_table_name
    }
  }
}

resource "aws_lambda_function" "get_link_stats" {
  function_name = "get-link-stats"
  handler       = "com.example.GetLinkStatsHandler::handleRequest"
  runtime       = "java21"
  role          = var.lambda_exec_role_arn
  filename      = "../build/distributions/shortener.zip"
  memory_size   = 128
  timeout       = 10

  environment {
    variables = {
      TABLE_SHORTLINKS = var.shortlinks_table_name
      TABLE_ANALYTICS  = var.analytics_table_name
    }
  }
}

resource "aws_lambda_function" "delete_link" {
  function_name = "delete-link"
  handler       = "com.example.DeleteLinkHandler::handleRequest"
  runtime       = "java21"
  role          = var.lambda_exec_role_arn
  filename      = "../build/distributions/shortener.zip"
  memory_size   = 128
  timeout       = 10

  environment {
    variables = {
      TABLE_SHORTLINKS = var.shortlinks_table_name
      TABLE_ANALYTICS  = var.analytics_table_name
    }
  }
}

##### 3) Integrations #####
resource "aws_apigatewayv2_integration" "shorten_integration" {
  api_id                 = aws_apigatewayv2_api.shortener_api.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.shorten.invoke_arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_integration" "redirect_integration" {
  api_id                 = aws_apigatewayv2_api.shortener_api.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.redirect.invoke_arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_integration" "get_link_integration" {
  api_id                 = aws_apigatewayv2_api.shortener_api.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.get_link.invoke_arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_integration" "get_link_stats_integration" {
  api_id                 = aws_apigatewayv2_api.shortener_api.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.get_link_stats.invoke_arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_integration" "delete_link_integration" {
  api_id                 = aws_apigatewayv2_api.shortener_api.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.delete_link.invoke_arn
  payload_format_version = "2.0"
}

##### 4) Routes #####
resource "aws_apigatewayv2_route" "post_links_route" {
  api_id    = aws_apigatewayv2_api.shortener_api.id
  route_key = "POST /links"
  target    = "integrations/${aws_apigatewayv2_integration.shorten_integration.id}"
}

resource "aws_apigatewayv2_route" "get_redirect_route" {
  api_id    = aws_apigatewayv2_api.shortener_api.id
  route_key = "GET /{code}"
  target    = "integrations/${aws_apigatewayv2_integration.redirect_integration.id}"
}

resource "aws_apigatewayv2_route" "get_link_route" {
  api_id    = aws_apigatewayv2_api.shortener_api.id
  route_key = "GET /links/{id}"
  target    = "integrations/${aws_apigatewayv2_integration.get_link_integration.id}"
}

resource "aws_apigatewayv2_route" "get_link_stats_route" {
  api_id    = aws_apigatewayv2_api.shortener_api.id
  route_key = "GET /links/{id}/stats"
  target    = "integrations/${aws_apigatewayv2_integration.get_link_stats_integration.id}"
}

resource "aws_apigatewayv2_route" "delete_link_route" {
  api_id    = aws_apigatewayv2_api.shortener_api.id
  route_key = "DELETE /links/{id}"
  target    = "integrations/${aws_apigatewayv2_integration.delete_link_integration.id}"
}

##### 5) Stage #####
resource "aws_apigatewayv2_stage" "dev" {
  api_id      = aws_apigatewayv2_api.shortener_api.id
  name        = "dev"
  auto_deploy = true

  default_route_settings {
    throttling_burst_limit = 100
    throttling_rate_limit  = 50
  }
}

##### 6) Permissions for API to invoke Lambda #####
resource "aws_lambda_permission" "allow_api_shorten" {
  statement_id  = "AllowAPIGatewayInvoke_Shorten"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.shorten.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.shortener_api.execution_arn}/*/*"
}

resource "aws_lambda_permission" "allow_api_redirect" {
  statement_id  = "AllowAPIGatewayInvoke_Redirect"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.redirect.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.shortener_api.execution_arn}/*/*"
}

resource "aws_lambda_permission" "allow_api_get_link" {
  statement_id  = "AllowAPIGatewayInvoke_GetLink"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.get_link.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.shortener_api.execution_arn}/*/*"
}

resource "aws_lambda_permission" "allow_api_get_link_stats" {
  statement_id  = "AllowAPIGatewayInvoke_GetLinkStats"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.get_link_stats.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.shortener_api.execution_arn}/*/*"
}

resource "aws_lambda_permission" "allow_api_delete_link" {
  statement_id  = "AllowAPIGatewayInvoke_DeleteLink"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.delete_link.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.shortener_api.execution_arn}/*/*"
}
