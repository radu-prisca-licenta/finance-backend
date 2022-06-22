const { DynamoDB, Lambda } = require('aws-sdk');

const TABLE_NAME = process.env.TABLE_NAME || '';

const db = new DynamoDB.DocumentClient();

exports.handler = async () => {

  const params = {
    TableName: TABLE_NAME
  };

  try {
    const response = await db.scan(params).promise();
    return { statusCode: 200, body: JSON.stringify(response.Items) };
  } catch (dbError) {
    return { statusCode: 500, body: JSON.stringify(dbError) };
  }
};
