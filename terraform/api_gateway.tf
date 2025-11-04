resource "aws_api_gateway_rest_api" "tags_api" {
  name        = "${local.resource_prefix}-tags-api"
  description = "API Gateway for RealWorld Tags API"

  endpoint_configuration {
    types = ["REGIONAL"]
  }

  tags = local.tags
}

resource "aws_api_gateway_resource" "tags" {
  rest_api_id = aws_api_gateway_rest_api.tags_api.id
  parent_id   = aws_api_gateway_rest_api.tags_api.root_resource_id
  path_part   = "tags"
}

resource "aws_api_gateway_method" "tags_get" {
  rest_api_id   = aws_api_gateway_rest_api.tags_api.id
  resource_id   = aws_api_gateway_resource.tags.id
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "tags_get_200" {
  rest_api_id = aws_api_gateway_rest_api.tags_api.id
  resource_id = aws_api_gateway_resource.tags.id
  http_method = aws_api_gateway_method.tags_get.http_method
  status_code = "200"

  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin"  = true
    "method.response.header.Access-Control-Allow-Headers" = true
    "method.response.header.Access-Control-Allow-Methods" = true
  }

  response_models = {
    "application/json" = "Empty"
  }
}

resource "aws_api_gateway_integration" "tags_lambda" {
  rest_api_id             = aws_api_gateway_rest_api.tags_api.id
  resource_id             = aws_api_gateway_resource.tags.id
  http_method             = aws_api_gateway_method.tags_get.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.tags_api.invoke_arn
}

resource "aws_api_gateway_integration_response" "tags_lambda_response" {
  rest_api_id = aws_api_gateway_rest_api.tags_api.id
  resource_id = aws_api_gateway_resource.tags.id
  http_method = aws_api_gateway_method.tags_get.http_method
  status_code = aws_api_gateway_method_response.tags_get_200.status_code

  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin"  = "'*'"
    "method.response.header.Access-Control-Allow-Headers" = "'Authorization,Cache-Control,Content-Type'"
    "method.response.header.Access-Control-Allow-Methods" = "'HEAD,GET,POST,PUT,DELETE,PATCH'"
  }

  depends_on = [
    aws_api_gateway_integration.tags_lambda
  ]
}

resource "aws_api_gateway_method" "tags_options" {
  rest_api_id   = aws_api_gateway_rest_api.tags_api.id
  resource_id   = aws_api_gateway_resource.tags.id
  http_method   = "OPTIONS"
  authorization = "NONE"
}

resource "aws_api_gateway_method_response" "tags_options_200" {
  rest_api_id = aws_api_gateway_rest_api.tags_api.id
  resource_id = aws_api_gateway_resource.tags.id
  http_method = aws_api_gateway_method.tags_options.http_method
  status_code = "200"

  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin"  = true
    "method.response.header.Access-Control-Allow-Headers" = true
    "method.response.header.Access-Control-Allow-Methods" = true
  }

  response_models = {
    "application/json" = "Empty"
  }
}

resource "aws_api_gateway_integration" "tags_options" {
  rest_api_id = aws_api_gateway_rest_api.tags_api.id
  resource_id = aws_api_gateway_resource.tags.id
  http_method = aws_api_gateway_method.tags_options.http_method
  type        = "MOCK"

  request_templates = {
    "application/json" = "{\"statusCode\": 200}"
  }
}

resource "aws_api_gateway_integration_response" "tags_options_response" {
  rest_api_id = aws_api_gateway_rest_api.tags_api.id
  resource_id = aws_api_gateway_resource.tags.id
  http_method = aws_api_gateway_method.tags_options.http_method
  status_code = aws_api_gateway_method_response.tags_options_200.status_code

  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin"  = "'*'"
    "method.response.header.Access-Control-Allow-Headers" = "'Authorization,Cache-Control,Content-Type'"
    "method.response.header.Access-Control-Allow-Methods" = "'HEAD,GET,POST,PUT,DELETE,PATCH'"
  }

  depends_on = [
    aws_api_gateway_integration.tags_options
  ]
}

resource "aws_api_gateway_deployment" "tags_api" {
  rest_api_id = aws_api_gateway_rest_api.tags_api.id

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_resource.tags.id,
      aws_api_gateway_method.tags_get.id,
      aws_api_gateway_method.tags_options.id,
      aws_api_gateway_integration.tags_lambda.id,
      aws_api_gateway_integration.tags_options.id,
    ]))
  }

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [
    aws_api_gateway_integration.tags_lambda,
    aws_api_gateway_integration.tags_options
  ]
}

resource "aws_api_gateway_stage" "tags_api" {
  deployment_id = aws_api_gateway_deployment.tags_api.id
  rest_api_id   = aws_api_gateway_rest_api.tags_api.id
  stage_name    = var.environment

  tags = local.tags
}
