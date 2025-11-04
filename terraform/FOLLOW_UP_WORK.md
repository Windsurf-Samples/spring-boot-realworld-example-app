# Follow-Up Work Required for Complete Integration

This document outlines the additional work needed to complete the migration of the Tags API from Spring Boot to AWS API Gateway + Lambda.

## Current Status

✅ **Completed:**
- Terraform infrastructure templates for API Gateway
- API Gateway REST API with `/tags` endpoint
- Lambda function stub with proper IAM roles
- CORS configuration matching Spring Boot settings
- Public endpoint configuration (no authentication)
- CloudWatch logging setup

⚠️ **Incomplete (STUB Implementation):**
- Lambda handler currently returns mock data
- No actual database connectivity
- Database queries not implemented

## 1. Lambda Handler Implementation

### Current Implementation
The Lambda handler in `lambda_function/index.py` is a stub that returns hardcoded mock data:

```python
response_body = {
    "tags": ["reactjs", "angularjs", "dragons", "training", "implementations"]
}
```

### Required Changes

#### Option A: Python Lambda Handler (Recommended for simplicity)

**Pros:**
- Simpler deployment (no JVM cold start)
- Smaller package size
- Faster execution
- Easier to maintain for this simple endpoint

**Implementation Steps:**

1. **Add database connectivity library**
   - For SQLite: Add `sqlite3` (built-in to Python)
   - For PostgreSQL/MySQL: Add `psycopg2` or `pymysql` to `requirements.txt`

2. **Update Lambda handler to query database**
   ```python
   import json
   import os
   import sqlite3  # or your database library
   
   def lambda_handler(event, context):
       # Connect to database
       db_connection_string = os.environ['DATABASE_CONNECTION_STRING']
       conn = connect_to_database(db_connection_string)
       
       # Query tags from database (matching MyBatis query: SELECT name FROM tags)
       cursor = conn.cursor()
       cursor.execute("SELECT name FROM tags")
       tags = [row[0] for row in cursor.fetchall()]
       
       # Return response matching Spring Boot format
       response_body = {"tags": tags}
       
       return {
           "statusCode": 200,
           "headers": {
               "Content-Type": "application/json",
               "Access-Control-Allow-Origin": "*",
               "Access-Control-Allow-Headers": "Authorization,Cache-Control,Content-Type",
               "Access-Control-Allow-Methods": "HEAD,GET,POST,PUT,DELETE,PATCH"
           },
           "body": json.dumps(response_body)
       }
   ```

3. **Add requirements.txt for Lambda dependencies**
   ```
   # For SQLite (built-in, no external deps needed)
   
   # For PostgreSQL
   # psycopg2-binary==2.9.9
   
   # For MySQL
   # pymysql==1.1.0
   ```

4. **Update Terraform to package dependencies**
   - Modify `lambda.tf` to install dependencies before zipping
   - Use `null_resource` with `local-exec` provisioner or external build script

#### Option B: Java Lambda Handler (Maintains consistency with existing codebase)

**Pros:**
- Reuse existing Spring Boot code and dependencies
- Type safety with Java
- Can leverage existing MyBatis configuration

**Cons:**
- Larger package size (~50-100 MB with Spring dependencies)
- Longer cold start times (2-10 seconds)
- More complex build and deployment process

**Implementation Steps:**

1. **Create new module in Spring Boot project**
   ```
   src/main/java/io/spring/lambda/
   └── TagsLambdaHandler.java
   ```

2. **Implement AWS Lambda RequestHandler**
   ```java
   package io.spring.lambda;
   
   import com.amazonaws.services.lambda.runtime.Context;
   import com.amazonaws.services.lambda.runtime.RequestHandler;
   import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
   import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
   import io.spring.application.TagsQueryService;
   import java.util.HashMap;
   import java.util.Map;
   
   public class TagsLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
       private TagsQueryService tagsQueryService;
       
       public TagsLambdaHandler() {
           // Initialize Spring context or manually wire dependencies
           this.tagsQueryService = initializeTagsQueryService();
       }
       
       @Override
       public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
           Map<String, Object> response = new HashMap<>();
           response.put("tags", tagsQueryService.allTags());
           
           APIGatewayProxyResponseEvent apiResponse = new APIGatewayProxyResponseEvent();
           apiResponse.setStatusCode(200);
           apiResponse.setBody(gson.toJson(response));
           
           Map<String, String> headers = new HashMap<>();
           headers.put("Content-Type", "application/json");
           headers.put("Access-Control-Allow-Origin", "*");
           apiResponse.setHeaders(headers);
           
           return apiResponse;
       }
   }
   ```

3. **Add Lambda dependencies to build.gradle**
   ```gradle
   dependencies {
       implementation 'com.amazonaws:aws-lambda-java-core:1.2.3'
       implementation 'com.amazonaws:aws-lambda-java-events:3.11.3'
   }
   ```

4. **Create Gradle task to build Lambda JAR**
   ```gradle
   task buildLambda(type: Jar) {
       archiveBaseName = 'tags-lambda'
       from sourceSets.main.output
       into('lib') {
           from configurations.runtimeClasspath
       }
       manifest {
           attributes 'Main-Class': 'io.spring.lambda.TagsLambdaHandler'
       }
   }
   ```

5. **Update Terraform to use Java runtime**
   ```hcl
   runtime = "java17"
   handler = "io.spring.lambda.TagsLambdaHandler::handleRequest"
   ```

## 2. Database Connectivity Changes

### Current Database: SQLite

The Spring Boot application currently uses SQLite with MyBatis:
- Database file: `dev.db`
- Query: `SELECT name FROM tags` (from `mapper/TagReadService.xml`)

### Lambda Database Options

#### Option 1: Continue with SQLite (Not Recommended)
- **Pros**: No code changes needed for queries
- **Cons**: 
  - SQLite files don't work well in Lambda (read-only filesystem except /tmp)
  - No shared state between Lambda invocations
  - Cannot scale horizontally

#### Option 2: Migrate to RDS (PostgreSQL or MySQL) - RECOMMENDED
- **Pros**: 
  - Proper managed database service
  - Scales with Lambda
  - Shared state across invocations
  - Better for production workloads
- **Cons**: 
  - Additional AWS costs
  - Need to migrate data
  - Update connection strings

**Migration Steps for RDS:**

1. **Provision RDS database**
   ```hcl
   # Add to Terraform
   resource "aws_db_instance" "realworld" {
     identifier        = "realworld-db"
     engine            = "postgres"
     engine_version    = "15.4"
     instance_class    = "db.t3.micro"
     allocated_storage = 20
     
     db_name  = "realworld"
     username = "admin"
     password = var.db_password  # Use AWS Secrets Manager in production
     
     vpc_security_group_ids = [aws_security_group.db.id]
     db_subnet_group_name   = aws_db_subnet_group.main.name
     
     skip_final_snapshot = true
   }
   ```

2. **Update Lambda VPC configuration**
   - Place Lambda in VPC to access RDS
   - Configure security groups for database access
   - Update IAM role with VPC execution policy (already included in `iam.tf`)

3. **Migrate database schema**
   - Export SQLite data: `sqlite3 dev.db .dump > schema.sql`
   - Convert to PostgreSQL/MySQL syntax
   - Import into RDS database

4. **Update connection string format**
   - SQLite: `jdbc:sqlite:dev.db`
   - PostgreSQL: `postgresql://host:5432/dbname?user=admin&password=xxx`
   - MySQL: `mysql://host:3306/dbname?user=admin&password=xxx`

#### Option 3: DynamoDB (Alternative NoSQL approach)
- Requires restructuring data model
- Good for key-value access patterns
- May not fit current relational model well

### Connection String Security

**Current:** Connection string is a Terraform variable

**Production Best Practice:**
1. Store in AWS Secrets Manager
2. Grant Lambda permission to read secret
3. Retrieve at runtime in Lambda handler

Example Terraform addition:
```hcl
resource "aws_secretsmanager_secret" "db_connection" {
  name = "${local.resource_prefix}-db-connection"
}

resource "aws_secretsmanager_secret_version" "db_connection" {
  secret_id     = aws_secretsmanager_secret.db_connection.id
  secret_string = var.database_connection_string
}

# Add to Lambda IAM policy
{
  "Effect": "Allow",
  "Action": [
    "secretsmanager:GetSecretValue"
  ],
  "Resource": aws_secretsmanager_secret.db_connection.arn
}
```

## 3. Packaging and Deployment Process

### Python Lambda Deployment

1. **Create deployment package**
   ```bash
   cd terraform/lambda_function
   pip install -r requirements.txt -t .
   zip -r ../lambda_function.zip .
   ```

2. **Update via Terraform**
   ```bash
   terraform apply
   ```

### Java Lambda Deployment

1. **Build Lambda JAR**
   ```bash
   ./gradlew buildLambda
   ```

2. **Update Terraform source**
   ```hcl
   resource "aws_lambda_function" "tags_api" {
     filename         = "../build/libs/tags-lambda.jar"
     # ... rest of configuration
   }
   ```

3. **Deploy**
   ```bash
   terraform apply
   ```

### CI/CD Integration

Consider adding GitHub Actions workflow:

```yaml
name: Deploy Tags Lambda

on:
  push:
    branches: [main]
    paths:
      - 'terraform/**'
      - 'src/main/java/io/spring/lambda/**'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v2
        
      - name: Terraform Init
        run: terraform init
        working-directory: terraform
        
      - name: Terraform Apply
        run: terraform apply -auto-approve
        working-directory: terraform
        env:
          AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
          AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
```

## 4. Spring Boot Configuration Changes

### If Using Java Lambda with Spring

1. **Minimize Spring Boot overhead**
   - Consider Spring Cloud Function for Lambda optimization
   - Or use plain Java without full Spring Boot context

2. **Environment-specific configuration**
   - Keep `application.properties` for local development
   - Use Lambda environment variables for deployed version

3. **Database connection pooling**
   - Lambda best practice: Use connection pooling carefully
   - Consider AWS RDS Proxy for connection management

## 5. Testing Approach

### Local Testing

**Before deployment:**

1. **Test Spring Boot application locally**
   ```bash
   ./gradlew bootRun
   curl http://localhost:8080/tags
   ```

2. **Test Lambda handler locally** (Python)
   ```bash
   cd terraform/lambda_function
   python -c "from index import lambda_handler; print(lambda_handler({}, {}))"
   ```

3. **Test Lambda with SAM CLI** (if using Java)
   ```bash
   sam local invoke TagsApi --event event.json
   ```

### Post-Deployment Testing

1. **Get API Gateway URL**
   ```bash
   terraform output api_gateway_url
   ```

2. **Test endpoint**
   ```bash
   curl $(terraform output -raw api_gateway_url)
   ```

3. **Verify CORS headers**
   ```bash
   curl -i -X OPTIONS $(terraform output -raw api_gateway_url) \
     -H "Origin: http://example.com" \
     -H "Access-Control-Request-Method: GET"
   ```

4. **Check Lambda logs**
   ```bash
   aws logs tail /aws/lambda/$(terraform output -raw lambda_function_name) --follow
   ```

### Integration Testing

Create automated tests comparing responses:

```python
import requests

# Test against Spring Boot
spring_response = requests.get("http://localhost:8080/tags")

# Test against API Gateway
api_gateway_url = "https://xxx.execute-api.us-east-1.amazonaws.com/dev/tags"
lambda_response = requests.get(api_gateway_url)

# Verify responses match
assert spring_response.json() == lambda_response.json()
assert spring_response.status_code == lambda_response.status_code
```

## 6. Performance Considerations

### Lambda Cold Starts
- **Python**: ~100-500ms cold start
- **Java with Spring**: ~2-10s cold start
- **Mitigation**: 
  - Use provisioned concurrency for consistent performance
  - Consider Lambda SnapStart for Java (reduces cold start to ~1s)
  - Minimize dependencies and package size

### Database Connection Management
- Lambda instances are reused for multiple invocations
- Reuse database connections across invocations
- Implement connection pooling in Lambda handler
- Consider AWS RDS Proxy for efficient connection management

### Estimated Costs (us-east-1, approximate)
- **API Gateway**: $3.50 per million requests
- **Lambda**: 
  - $0.20 per 1M requests
  - $0.0000166667 per GB-second
- **RDS db.t3.micro**: ~$15/month
- **CloudWatch Logs**: ~$0.50 per GB stored

## 7. Migration Strategy

### Recommended Phased Approach

**Phase 1: Infrastructure Setup (Current)**
- ✅ Terraform templates created
- ✅ API Gateway configured
- ✅ Lambda stub deployed

**Phase 2: Database Migration**
- [ ] Choose database (recommend RDS PostgreSQL)
- [ ] Provision RDS instance
- [ ] Migrate schema and data
- [ ] Test connectivity from Lambda

**Phase 3: Lambda Implementation**
- [ ] Implement database connectivity in Lambda handler
- [ ] Test locally with new database
- [ ] Deploy and test in AWS

**Phase 4: Production Cutover**
- [ ] Run both systems in parallel
- [ ] Compare responses and performance
- [ ] Gradually shift traffic to Lambda version
- [ ] Monitor for errors and rollback if needed

**Phase 5: Cleanup**
- [ ] Remove Tags API from Spring Boot application
- [ ] Decommission old infrastructure

## 8. Monitoring and Observability

### CloudWatch Metrics to Monitor
- Lambda invocations
- Lambda errors
- Lambda duration
- API Gateway 4xx/5xx errors
- API Gateway latency

### CloudWatch Alarms to Create
```hcl
resource "aws_cloudwatch_metric_alarm" "lambda_errors" {
  alarm_name          = "${local.resource_prefix}-tags-lambda-errors"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "Errors"
  namespace           = "AWS/Lambda"
  period              = "300"
  statistic           = "Sum"
  threshold           = "5"
  alarm_description   = "Tags Lambda function errors"
  
  dimensions = {
    FunctionName = aws_lambda_function.tags_api.function_name
  }
}
```

### Logging Best Practices
- Use structured logging (JSON format)
- Include request IDs for tracing
- Log database query performance
- Implement distributed tracing with AWS X-Ray

## 9. Security Considerations

### Current Security Posture
- ✅ Public endpoint (no auth) matches Spring Boot configuration
- ✅ CORS properly configured
- ✅ IAM role with least privilege for Lambda

### Additional Security Recommendations
- [ ] Enable AWS WAF for API Gateway (DDoS protection, rate limiting)
- [ ] Implement AWS Secrets Manager for database credentials
- [ ] Enable VPC for Lambda if accessing RDS
- [ ] Use security groups to restrict database access
- [ ] Enable AWS CloudTrail for audit logging
- [ ] Implement AWS Shield for DDoS protection

### Rate Limiting
Consider adding API Gateway usage plans:
```hcl
resource "aws_api_gateway_usage_plan" "tags_api" {
  name = "${local.resource_prefix}-tags-usage-plan"

  api_stages {
    api_id = aws_api_gateway_rest_api.tags_api.id
    stage  = aws_api_gateway_stage.tags_api.stage_name
  }

  throttle_settings {
    burst_limit = 100
    rate_limit  = 50
  }
}
```

## 10. Documentation Updates Needed

- [ ] Update API documentation with new Gateway URLs
- [ ] Document environment variables required for Lambda
- [ ] Create runbook for common operational tasks
- [ ] Update architecture diagrams
- [ ] Document rollback procedures

## Summary

The Terraform infrastructure is complete and ready for deployment. The main work remaining is:

1. **Critical**: Implement actual database connectivity in Lambda handler
2. **Critical**: Migrate from SQLite to a Lambda-compatible database (RDS recommended)
3. **Important**: Set up proper CI/CD pipeline for Lambda deployments
4. **Important**: Implement comprehensive testing strategy
5. **Recommended**: Add monitoring, alerting, and security enhancements

Estimated time to complete: 2-4 days for basic implementation, 1-2 weeks for production-ready solution with proper testing and monitoring.
