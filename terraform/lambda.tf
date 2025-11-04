data "archive_file" "lambda_zip" {
  type        = "zip"
  source_dir  = "${path.module}/lambda_function"
  output_path = "${path.module}/lambda_function.zip"
}

resource "aws_lambda_function" "tags_api" {
  filename         = data.archive_file.lambda_zip.output_path
  function_name    = "${local.resource_prefix}-tags-api"
  role            = aws_iam_role.lambda_execution_role.arn
  handler         = "index.lambda_handler"
  source_code_hash = data.archive_file.lambda_zip.output_base64sha256
  runtime         = var.lambda_runtime
  timeout         = 30
  memory_size     = 512

  environment {
    variables = {
      DATABASE_CONNECTION_STRING = var.database_connection_string
      ENVIRONMENT                = var.environment
    }
  }

  tags = local.tags
}

resource "aws_cloudwatch_log_group" "lambda_logs" {
  name              = "/aws/lambda/${aws_lambda_function.tags_api.function_name}"
  retention_in_days = 7

  tags = local.tags
}

resource "aws_lambda_permission" "api_gateway_invoke" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.tags_api.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.tags_api.execution_arn}/*/*"
}
