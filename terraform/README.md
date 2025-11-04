# RealWorld API - Tags API Terraform Infrastructure

This directory contains Terraform templates for deploying the Tags API endpoint to AWS API Gateway + Lambda.

## Architecture

The infrastructure consists of:

- **API Gateway REST API**: Main API Gateway resource
- **API Gateway Resource**: `/tags` endpoint path
- **API Gateway Methods**: 
  - `GET /tags` - Retrieves all tags (no authorization required)
  - `OPTIONS /tags` - CORS preflight support
- **Lambda Function**: Handles the Tags API logic
- **IAM Role & Policies**: Permissions for Lambda execution and API Gateway invocation
- **CloudWatch Log Group**: Lambda function logs
- **API Gateway Deployment & Stage**: Makes the API accessible

## Configuration

### CORS Settings

The API Gateway is configured with CORS to match the Spring Boot application settings:
- **Allowed Origins**: `*` (all origins)
- **Allowed Methods**: `HEAD`, `GET`, `POST`, `PUT`, `DELETE`, `PATCH`
- **Allowed Headers**: `Authorization`, `Cache-Control`, `Content-Type`
- **Allow Credentials**: `false`

### Authentication

The `/tags` endpoint is configured as a public endpoint with no authentication required (`authorization = "NONE"`), matching the Spring Boot security configuration.

## Prerequisites

- Terraform >= 1.0
- AWS CLI configured with appropriate credentials
- AWS account with permissions to create:
  - API Gateway resources
  - Lambda functions
  - IAM roles and policies
  - CloudWatch log groups

## Usage

### 1. Initialize Terraform

```bash
cd terraform
terraform init
```

### 2. Configure Variables

Copy the example variables file and update with your values:

```bash
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` and set:
- `aws_region`: Your AWS region (default: us-east-1)
- `project_name`: Project name for resource naming (default: realworld-api)
- `environment`: Environment name (default: dev)
- `database_connection_string`: Your database connection string (REQUIRED)

### 3. Review Plan

```bash
terraform plan
```

### 4. Deploy Infrastructure

```bash
terraform apply
```

### 5. Access the API

After deployment, Terraform will output the API Gateway URL:

```bash
terraform output api_gateway_url
```

Test the endpoint:

```bash
curl $(terraform output -raw api_gateway_url)
```

## Outputs

The following outputs are available after deployment:

- `api_gateway_url`: Full URL of the Tags API endpoint
- `api_gateway_id`: ID of the API Gateway REST API
- `lambda_function_name`: Name of the Lambda function
- `lambda_function_arn`: ARN of the Lambda function
- `lambda_execution_role_arn`: ARN of the Lambda execution IAM role
- `cloudwatch_log_group`: CloudWatch log group name for Lambda logs

## Current Implementation Status

⚠️ **IMPORTANT**: The current Lambda handler (`lambda_function/index.py`) is a **STUB IMPLEMENTATION** that returns mock data. See `FOLLOW_UP_WORK.md` for details on completing the integration.

## Cleanup

To destroy all created resources:

```bash
terraform destroy
```

## File Structure

```
terraform/
├── main.tf                 # Provider and general configuration
├── variables.tf            # Input variables
├── outputs.tf              # Output values
├── iam.tf                  # IAM roles and policies
├── api_gateway.tf          # API Gateway resources
├── lambda.tf               # Lambda function configuration
├── lambda_function/        # Lambda handler code
│   └── index.py            # Python Lambda handler (STUB)
├── terraform.tfvars.example # Example variables file
├── README.md               # This file
└── FOLLOW_UP_WORK.md       # Documentation for next steps
```

## Next Steps

See `FOLLOW_UP_WORK.md` for detailed information about:
- Completing the Lambda handler implementation
- Database connectivity changes
- Packaging and deployment process
- Testing approach
