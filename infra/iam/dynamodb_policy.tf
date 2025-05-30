data "aws_iam_policy_document" "lambda_dynamodb" {
  statement {
    effect = "Allow"

    # права на ShortLinks: читання та запис
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem",
      "dynamodb:Query"
    ]

    resources = [
      var.shortlinks_table_arn
    ]
  }

  statement {
    effect = "Allow"

    # права на Analytics: тільки запис (Write-only)
    actions = [
      "dynamodb:PutItem",
      "dynamodb:UpdateItem"
    ]

    resources = [
      var.analytics_table_arn
    ]
  }
}

resource "aws_iam_policy" "lambda_dynamodb_policy" {
  name   = "lambda-dynamodb-policy"
  policy = data.aws_iam_policy_document.lambda_dynamodb.json
}

resource "aws_iam_role_policy_attachment" "attach_lambda_dynamodb" {
  role       = aws_iam_role.lambda_exec_role.name
  policy_arn = aws_iam_policy.lambda_dynamodb_policy.arn
}
