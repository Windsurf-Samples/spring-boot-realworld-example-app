import json
import os


def lambda_handler(event, context):
    """
    Lambda handler for Tags API endpoint.
    
    This is a STUB implementation that returns mock data.
    For production use, this needs to be replaced with actual database queries.
    
    Expected behavior:
    - Query the database to retrieve all tag names from the 'tags' table
    - Return JSON response: {"tags": ["tag1", "tag2", ...]}
    """
    
    print(f"Received event: {json.dumps(event)}")
    
    response_body = {
        "tags": [
            "reactjs",
            "angularjs", 
            "dragons",
            "training",
            "implementations"
        ]
    }
    
    response = {
        "statusCode": 200,
        "headers": {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Headers": "Authorization,Cache-Control,Content-Type",
            "Access-Control-Allow-Methods": "HEAD,GET,POST,PUT,DELETE,PATCH"
        },
        "body": json.dumps(response_body)
    }
    
    return response
