# Serverless Tags API Deployment Guide

This guide provides step-by-step instructions for deploying the Tags API as a serverless function using AWS Lambda, API Gateway, and Aurora Serverless v2.

## Overview

The serverless Tags API migration replaces the Spring Boot `/tags` endpoint with:
- **AWS Lambda** function containing the business logic
- **API Gateway** for HTTP endpoint management
- **Aurora Serverless v2** for scalable database access
- **VPC** configuration for secure database connectivity
- **IAM roles** with least privilege permissions

## Prerequisites

### AWS CLI Setup
1. Install AWS CLI v2 or later
2. Configure AWS credentials with appropriate permissions:
   ```bash
   aws configure
   ```
3. Verify authentication:
   ```bash
   aws sts get-caller-identity
   ```

### Required AWS Permissions
Your AWS user/role needs the following permissions:
- CloudFormation: Full access
- Lambda: Full access
- API Gateway: Full access
- RDS: Full access
- EC2: VPC and Security Group management
- IAM: Role and policy management
- Secrets Manager: Secret management

### Tools Required
- AWS CLI v2+
- Node.js 18+ (for local testing)
- MySQL client (for database setup)

## Deployment Steps

### 1. Validate CloudFormation Template
```bash
cd ~/repos/spring-boot-realworld-example-app
aws cloudformation validate-template --template-body file://cloudformation/tags-api-serverless.yaml
```

### 2. Deploy the CloudFormation Stack
```bash
# Deploy with default parameters
aws cloudformation create-stack \
  --stack-name tags-api-serverless-dev \
  --template-body file://cloudformation/tags-api-serverless.yaml \
  --parameters ParameterKey=Environment,ParameterValue=dev \
               ParameterKey=DatabaseUsername,ParameterValue=admin \
               ParameterKey=DatabasePassword,ParameterValue=YourSecurePassword123! \
  --capabilities CAPABILITY_NAMED_IAM

# Monitor deployment progress
aws cloudformation describe-stacks --stack-name tags-api-serverless-dev --query 'Stacks[0].StackStatus'
```

### 3. Wait for Stack Creation
The deployment typically takes 10-15 minutes due to Aurora Serverless cluster creation:
```bash
aws cloudformation wait stack-create-complete --stack-name tags-api-serverless-dev
```

### 4. Get Stack Outputs
```bash
aws cloudformation describe-stacks \
  --stack-name tags-api-serverless-dev \
  --query 'Stacks[0].Outputs'
```

### 5. Initialize Database Schema
```bash
# Get database endpoint from stack outputs
DB_ENDPOINT=$(aws cloudformation describe-stacks \
  --stack-name tags-api-serverless-dev \
  --query 'Stacks[0].Outputs[?OutputKey==`DatabaseEndpoint`].OutputValue' \
  --output text)

# Connect to database and run migration script
mysql -h $DB_ENDPOINT -u admin -p realworld < cloudformation/migrate-data.sql
```

### 6. Update Lambda Function Code
The CloudFormation template includes inline Lambda code, but for production deployments, you should package and deploy the code separately:

```bash
# Package Lambda function
cd lambda/tags-api
npm install
zip -r tags-api.zip index.js node_modules/

# Update Lambda function
aws lambda update-function-code \
  --function-name dev-tags-api \
  --zip-file fileb://tags-api.zip
```

## Testing the Deployment

### 1. Get API Gateway URL
```bash
API_URL=$(aws cloudformation describe-stacks \
  --stack-name tags-api-serverless-dev \
  --query 'Stacks[0].Outputs[?OutputKey==`ApiGatewayUrl`].OutputValue' \
  --output text)

echo "API Gateway URL: $API_URL"
```

### 2. Test the Tags Endpoint
```bash
# Test GET /tags endpoint
curl -X GET "$API_URL" | jq .

# Expected response format:
# {
#   "tags": ["javascript", "nodejs", "aws", "serverless", ...]
# }
```

### 3. Test CORS (if needed for frontend)
```bash
curl -X OPTIONS "$API_URL" -H "Origin: http://localhost:3000" -v
```

### 4. Check Lambda Logs
```bash
aws logs describe-log-groups --log-group-name-prefix "/aws/lambda/dev-tags-api"

# View recent logs
aws logs tail /aws/lambda/dev-tags-api --follow
```

## Performance and Cost Considerations

### Aurora Serverless v2 Scaling
- **Min Capacity**: 0.5 ACU (Aurora Capacity Units)
- **Max Capacity**: 1 ACU
- **Auto-pause**: Automatically pauses after inactivity
- **Cost**: Pay only for actual usage

### Lambda Configuration
- **Memory**: 128 MB (sufficient for simple database queries)
- **Timeout**: 30 seconds
- **Cold Start**: ~1-2 seconds for first invocation
- **Cost**: Pay per request and execution time

### Expected Costs (US East 1)
- **Aurora Serverless v2**: ~$0.12/hour when active, $0 when paused
- **Lambda**: ~$0.20 per 1M requests + $0.0000166667 per GB-second
- **API Gateway**: ~$3.50 per 1M requests
- **Data Transfer**: Minimal for this use case

## Monitoring and Troubleshooting

### CloudWatch Metrics
Monitor these key metrics:
- Lambda: Duration, Errors, Throttles
- API Gateway: Count, Latency, 4XXError, 5XXError
- Aurora: DatabaseConnections, CPUUtilization

### Common Issues

**1. Lambda Timeout**
- Increase timeout in CloudFormation template
- Check database connection pooling

**2. Database Connection Errors**
- Verify VPC configuration
- Check security group rules
- Ensure Aurora cluster is in available state

**3. API Gateway 502 Errors**
- Check Lambda function logs
- Verify Lambda response format
- Ensure proper IAM permissions

**4. CORS Issues**
- Verify OPTIONS method configuration
- Check response headers in Lambda function

### Debugging Commands
```bash
# Check Lambda function status
aws lambda get-function --function-name dev-tags-api

# Check Aurora cluster status
aws rds describe-db-clusters --db-cluster-identifier dev-tags-api-cluster

# View CloudFormation events
aws cloudformation describe-stack-events --stack-name tags-api-serverless-dev
```

## Rollback Procedures

### 1. Quick Rollback (API Gateway)
```bash
# Revert to previous deployment
aws apigateway create-deployment \
  --rest-api-id <API_ID> \
  --stage-name dev \
  --description "Rollback deployment"
```

### 2. Full Stack Rollback
```bash
# Delete the entire stack
aws cloudformation delete-stack --stack-name tags-api-serverless-dev

# Wait for deletion to complete
aws cloudformation wait stack-delete-complete --stack-name tags-api-serverless-dev
```

### 3. Database Backup Recovery
```bash
# Restore from automated backup (if needed)
aws rds restore-db-cluster-to-point-in-time \
  --source-db-cluster-identifier dev-tags-api-cluster \
  --db-cluster-identifier dev-tags-api-cluster-restored \
  --restore-to-time 2023-01-01T12:00:00Z
```

## Security Best Practices

### 1. Database Security
- Database is in private subnets only
- Security groups restrict access to Lambda only
- Credentials stored in AWS Secrets Manager
- Encryption at rest enabled by default

### 2. Lambda Security
- Minimal IAM permissions (least privilege)
- VPC configuration for database access
- Environment variables for configuration
- CloudWatch logging enabled

### 3. API Gateway Security
- CORS configured for specific origins (update as needed)
- Rate limiting can be added if needed
- API keys can be added for additional security

## Next Steps

1. **Add Monitoring**: Set up CloudWatch alarms for error rates and latency
2. **Add Caching**: Implement API Gateway caching for better performance
3. **Add Authentication**: Integrate with existing JWT authentication if needed
4. **Add Rate Limiting**: Configure API Gateway throttling
5. **Add CI/CD**: Automate deployments using GitHub Actions or AWS CodePipeline

## Support

For issues or questions:
1. Check CloudWatch logs for Lambda and API Gateway
2. Review CloudFormation stack events
3. Verify AWS service limits and quotas
4. Contact AWS support for infrastructure issues
