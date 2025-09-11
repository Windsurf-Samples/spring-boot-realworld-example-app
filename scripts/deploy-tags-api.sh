#!/bin/bash


set -e

STACK_NAME="tags-api-serverless"
ENVIRONMENT="dev"
REGION="us-east-1"
DB_USERNAME="admin"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_aws_cli() {
    print_status "Checking AWS CLI configuration..."
    
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI is not installed. Please install AWS CLI v2."
        exit 1
    fi
    
    if ! aws sts get-caller-identity &> /dev/null; then
        print_error "AWS CLI is not configured. Please run 'aws configure'."
        exit 1
    fi
    
    print_status "AWS CLI is configured correctly."
}

validate_template() {
    print_status "Validating CloudFormation template..."
    
    if aws cloudformation validate-template --template-body file://cloudformation/tags-api-serverless.yaml &> /dev/null; then
        print_status "CloudFormation template is valid."
    else
        print_error "CloudFormation template validation failed."
        exit 1
    fi
}

stack_exists() {
    aws cloudformation describe-stacks --stack-name "$1" &> /dev/null
}

get_stack_status() {
    aws cloudformation describe-stacks --stack-name "$1" --query 'Stacks[0].StackStatus' --output text 2>/dev/null || echo "DOES_NOT_EXIST"
}

deploy_stack() {
    local action="$1"
    local stack_name="$2"
    
    echo -n "Enter database password (minimum 8 characters): "
    read -s DB_PASSWORD
    echo
    
    if [ ${#DB_PASSWORD} -lt 8 ]; then
        print_error "Password must be at least 8 characters long."
        exit 1
    fi
    
    print_status "${action^}ing CloudFormation stack: $stack_name"
    
    aws cloudformation $action-stack \
        --stack-name "$stack_name" \
        --template-body file://cloudformation/tags-api-serverless.yaml \
        --parameters \
            ParameterKey=Environment,ParameterValue="$ENVIRONMENT" \
            ParameterKey=DatabaseUsername,ParameterValue="$DB_USERNAME" \
            ParameterKey=DatabasePassword,ParameterValue="$DB_PASSWORD" \
        --capabilities CAPABILITY_NAMED_IAM \
        --region "$REGION"
    
    print_status "Waiting for stack $action to complete..."
    aws cloudformation wait stack-${action}-complete --stack-name "$stack_name" --region "$REGION"
    
    local status=$(get_stack_status "$stack_name")
    if [[ "$status" == *"COMPLETE"* ]]; then
        print_status "Stack $action completed successfully."
    else
        print_error "Stack $action failed with status: $status"
        exit 1
    fi
}

update_lambda_function() {
    print_status "Packaging and updating Lambda function..."
    
    cd lambda/tags-api
    
    if [ -f package.json ]; then
        print_status "Installing Node.js dependencies..."
        npm install --production
    fi
    
    print_status "Creating deployment package..."
    zip -r tags-api.zip index.js node_modules/ > /dev/null
    
    print_status "Updating Lambda function code..."
    aws lambda update-function-code \
        --function-name "${ENVIRONMENT}-tags-api" \
        --zip-file fileb://tags-api.zip \
        --region "$REGION" > /dev/null
    
    rm tags-api.zip
    cd ../..
    
    print_status "Lambda function updated successfully."
}

initialize_database() {
    print_status "Initializing database schema..."
    
    local db_endpoint=$(aws cloudformation describe-stacks \
        --stack-name "$STACK_NAME-$ENVIRONMENT" \
        --query 'Stacks[0].Outputs[?OutputKey==`DatabaseEndpoint`].OutputValue' \
        --output text \
        --region "$REGION")
    
    if [ -z "$db_endpoint" ]; then
        print_error "Could not retrieve database endpoint from stack outputs."
        exit 1
    fi
    
    print_status "Database endpoint: $db_endpoint"
    print_warning "Please run the following command manually to initialize the database:"
    echo "mysql -h $db_endpoint -u $DB_USERNAME -p realworld < cloudformation/migrate-data.sql"
}

test_deployment() {
    print_status "Testing deployment..."
    
    local api_url=$(aws cloudformation describe-stacks \
        --stack-name "$STACK_NAME-$ENVIRONMENT" \
        --query 'Stacks[0].Outputs[?OutputKey==`ApiGatewayUrl`].OutputValue' \
        --output text \
        --region "$REGION")
    
    if [ -z "$api_url" ]; then
        print_error "Could not retrieve API Gateway URL from stack outputs."
        exit 1
    fi
    
    print_status "API Gateway URL: $api_url"
    
    print_status "Testing /tags endpoint..."
    local response=$(curl -s -w "%{http_code}" "$api_url")
    local http_code="${response: -3}"
    local body="${response%???}"
    
    if [ "$http_code" = "200" ]; then
        print_status "API test successful!"
        echo "Response: $body"
    else
        print_error "API test failed with HTTP code: $http_code"
        echo "Response: $body"
        exit 1
    fi
}

show_outputs() {
    print_status "Stack outputs:"
    aws cloudformation describe-stacks \
        --stack-name "$STACK_NAME-$ENVIRONMENT" \
        --query 'Stacks[0].Outputs[*].[OutputKey,OutputValue]' \
        --output table \
        --region "$REGION"
}

main() {
    print_status "Starting serverless Tags API deployment..."
    
    check_aws_cli
    validate_template
    
    local full_stack_name="$STACK_NAME-$ENVIRONMENT"
    local stack_status=$(get_stack_status "$full_stack_name")
    
    if [ "$stack_status" = "DOES_NOT_EXIST" ]; then
        deploy_stack "create" "$full_stack_name"
    elif [[ "$stack_status" == *"COMPLETE"* ]]; then
        print_warning "Stack already exists. Updating..."
        deploy_stack "update" "$full_stack_name"
    else
        print_error "Stack is in an invalid state: $stack_status"
        exit 1
    fi
    
    update_lambda_function
    
    initialize_database
    
    test_deployment
    
    show_outputs
    
    print_status "Deployment completed successfully!"
    print_warning "Don't forget to initialize the database using the command shown above."
}

case "${1:-deploy}" in
    deploy)
        main
        ;;
    test)
        test_deployment
        ;;
    outputs)
        show_outputs
        ;;
    delete)
        print_warning "This will delete the entire stack. Are you sure? (y/N)"
        read -r confirmation
        if [[ "$confirmation" =~ ^[Yy]$ ]]; then
            print_status "Deleting stack..."
            aws cloudformation delete-stack --stack-name "$STACK_NAME-$ENVIRONMENT" --region "$REGION"
            aws cloudformation wait stack-delete-complete --stack-name "$STACK_NAME-$ENVIRONMENT" --region "$REGION"
            print_status "Stack deleted successfully."
        else
            print_status "Deletion cancelled."
        fi
        ;;
    *)
        echo "Usage: $0 [deploy|test|outputs|delete]"
        echo "  deploy  - Deploy or update the stack (default)"
        echo "  test    - Test the deployed API"
        echo "  outputs - Show stack outputs"
        echo "  delete  - Delete the stack"
        exit 1
        ;;
esac
