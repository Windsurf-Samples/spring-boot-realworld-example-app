const mysql = require('mysql2/promise');
const AWS = require('aws-sdk');

const secretsManager = new AWS.SecretsManager();

let connection = null;

async function getDbCredentials() {
  try {
    const secret = await secretsManager.getSecretValue({
      SecretId: process.env.DB_SECRET_ARN
    }).promise();
    return JSON.parse(secret.SecretString);
  } catch (error) {
    console.error('Error retrieving database credentials:', error);
    throw error;
  }
}

async function getDbConnection() {
  if (connection) {
    try {
      await connection.ping();
      return connection;
    } catch (error) {
      console.log('Connection lost, creating new connection');
      connection = null;
    }
  }
  
  const credentials = await getDbCredentials();
  
  connection = await mysql.createConnection({
    host: process.env.DB_HOST,
    port: process.env.DB_PORT || 3306,
    user: credentials.username,
    password: credentials.password,
    database: process.env.DB_NAME || 'realworld',
    connectTimeout: 10000,
    acquireTimeout: 10000,
    timeout: 10000
  });
  
  return connection;
}

exports.handler = async (event) => {
  console.log('Event:', JSON.stringify(event, null, 2));
  
  try {
    const db = await getDbConnection();
    const [rows] = await db.execute('SELECT name FROM tags ORDER BY name');
    
    const tags = rows.map(row => row.name);
    
    console.log(`Retrieved ${tags.length} tags from database`);
    
    return {
      statusCode: 200,
      headers: {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': 'Content-Type',
        'Access-Control-Allow-Methods': 'GET, OPTIONS'
      },
      body: JSON.stringify({ tags })
    };
  } catch (error) {
    console.error('Error:', error);
    return {
      statusCode: 500,
      headers: {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*'
      },
      body: JSON.stringify({ 
        error: 'Internal server error',
        message: error.message 
      })
    };
  }
};
