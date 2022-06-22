package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinanceBackendStack extends Stack {
    public FinanceBackendStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public FinanceBackendStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        TableProps tableProps;
        Attribute partitionKey = Attribute.builder()
                .name("userId")
                .type(AttributeType.STRING)
                .build();
        tableProps = TableProps.builder()
                .tableName("users")
                .partitionKey(partitionKey)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
        Table dynamodbTable = new Table(this, "users", tableProps);


        Map<String, String> lambdaEnvMap = new HashMap<>();
        lambdaEnvMap.put("TABLE_NAME", dynamodbTable.getTableName());
        lambdaEnvMap.put("PRIMARY_KEY", "userId");


        Function getOneUserFunction = new Function(this, "getOneUserFunction",
                getLambdaFunctionProps(lambdaEnvMap, "get-one.handler"));
        Function getAllUsersFunction = new Function(this, "getAllUsersFunction",
                getLambdaFunctionProps(lambdaEnvMap, "get-all.handler"));
        Function createUserFunction = new Function(this, "createUserFunction",
                getLambdaFunctionProps(lambdaEnvMap, "create.handler"));
        Function updateUserFunction = new Function(this, "updateUserFunction",
                getLambdaFunctionProps(lambdaEnvMap, "update-one.handler"));
        Function deleteUserFunction = new Function(this, "deleteUserFunction",
                getLambdaFunctionProps(lambdaEnvMap, "delete-one.handler"));


        dynamodbTable.grantReadWriteData(getOneUserFunction);
        dynamodbTable.grantReadWriteData(getAllUsersFunction);
        dynamodbTable.grantReadWriteData(createUserFunction);
        dynamodbTable.grantReadWriteData(updateUserFunction);
        dynamodbTable.grantReadWriteData(deleteUserFunction);

        RestApi api = new RestApi(this, "usersApi",
                RestApiProps.builder().restApiName("Users Service").build());

        IResource users = api.getRoot().addResource("users");

        Integration getAllIntegration = new LambdaIntegration(getAllUsersFunction);
        users.addMethod("GET", getAllIntegration);

        Integration createOneIntegration = new LambdaIntegration(createUserFunction);
        users.addMethod("POST", createOneIntegration);
        addCorsOptions(users);


        IResource singleUser = users.addResource("{id}");
        Integration getOneIntegration = new LambdaIntegration(getOneUserFunction);
        singleUser.addMethod("GET", getOneIntegration);

        Integration updateOneIntegration = new LambdaIntegration(updateUserFunction);
        singleUser.addMethod("PATCH", updateOneIntegration);

        Integration deleteOneIntegration = new LambdaIntegration(deleteUserFunction);
        singleUser.addMethod("DELETE", deleteOneIntegration);

        addCorsOptions(singleUser);

        // Transactions

        TableProps tableTransactionProps;
        Attribute partitionTransactionKey = Attribute.builder()
                .name("transactionId")
                .type(AttributeType.STRING)
                .build();
        tableTransactionProps = TableProps.builder()
                .tableName("transactions")
                .partitionKey(partitionTransactionKey)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
        Table dynamodbTransactionTable = new Table(this, "transactions", tableTransactionProps);


        Map<String, String> lambdaEnvTransactionMap = new HashMap<>();
        lambdaEnvTransactionMap.put("TABLE_NAME", dynamodbTransactionTable.getTableName());
        lambdaEnvTransactionMap.put("PRIMARY_KEY", "transactionId");


        Function getOneTransactionFunction = new Function(this, "getOneTransactionFunction",
                getLambdaFunctionProps(lambdaEnvTransactionMap, "get-one.handler"));
        Function getAllTransactionsFunction = new Function(this, "getAllTransactionsFunction",
                getLambdaFunctionProps(lambdaEnvTransactionMap, "get-all.handler"));
        Function createTransactionFunction = new Function(this, "createTransactionFunction",
                getLambdaFunctionProps(lambdaEnvTransactionMap, "create.handler"));
        Function updateTransactionFunction = new Function(this, "updateTransactionFunction",
                getLambdaFunctionProps(lambdaEnvTransactionMap, "update-one.handler"));
        Function deleteTransactionFunction = new Function(this, "deleteTransactionFunction",
                getLambdaFunctionProps(lambdaEnvTransactionMap, "delete-one.handler"));


        dynamodbTransactionTable.grantReadWriteData(getOneTransactionFunction);
        dynamodbTransactionTable.grantReadWriteData(getAllTransactionsFunction);
        dynamodbTransactionTable.grantReadWriteData(createTransactionFunction);
        dynamodbTransactionTable.grantReadWriteData(updateTransactionFunction);
        dynamodbTransactionTable.grantReadWriteData(deleteTransactionFunction);

        RestApi transactionApi = new RestApi(this, "transactionsApi",
                RestApiProps.builder().restApiName("Transactions Service").build());

        IResource transactions = transactionApi.getRoot().addResource("transactions");

        Integration getAllTransactionIntegration = new LambdaIntegration(getAllTransactionsFunction);
        transactions.addMethod("GET", getAllTransactionIntegration);

        Integration createOneTransactionIntegration = new LambdaIntegration(createTransactionFunction);
        transactions.addMethod("POST", createOneTransactionIntegration);
        addCorsOptions(transactions);


        IResource singleTransaction = transactions.addResource("{id}");
        Integration getOneTransactionIntegration = new LambdaIntegration(getOneTransactionFunction);
        singleTransaction.addMethod("GET", getOneTransactionIntegration);

        Integration updateOneTransactionIntegration = new LambdaIntegration(updateTransactionFunction);
        singleTransaction.addMethod("PATCH", updateOneTransactionIntegration);

        Integration deleteOneTransactionIntegration = new LambdaIntegration(deleteTransactionFunction);
        singleTransaction.addMethod("DELETE", deleteOneTransactionIntegration);

        addCorsOptions(singleTransaction);
    }


    private void addCorsOptions(IResource resource) {
        List<MethodResponse> methodResponses = new ArrayList<>();

        Map<String, Boolean> responseParameters = new HashMap<>();
        responseParameters.put("method.response.header.Access-Control-Allow-Headers", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Methods", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Credentials", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Origin", Boolean.TRUE);
        methodResponses.add(MethodResponse.builder()
                .responseParameters(responseParameters)
                .statusCode("200")
                .build());
        MethodOptions methodOptions = MethodOptions.builder()
                .methodResponses(methodResponses)
                .build();

        Map<String, String> requestTemplate = new HashMap<>();
        requestTemplate.put("application/json", "{\"statusCode\": 200}");
        List<IntegrationResponse> integrationResponses = new ArrayList<>();

        Map<String, String> integrationResponseParameters = new HashMap<>();
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Headers", "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,X-Amz-User-Agent'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Origin", "'*'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Credentials", "'false'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Methods", "'OPTIONS,GET,PUT,POST,DELETE'");
        integrationResponses.add(IntegrationResponse.builder()
                .responseParameters(integrationResponseParameters)
                .statusCode("200")
                .build());
        Integration methodIntegration = MockIntegration.Builder.create()
                .integrationResponses(integrationResponses)
                .passthroughBehavior(PassthroughBehavior.NEVER)
                .requestTemplates(requestTemplate)
                .build();

        resource.addMethod("OPTIONS", methodIntegration, methodOptions);
    }

    private FunctionProps getLambdaFunctionProps(Map<String, String> lambdaEnvMap, String handler) {
        return FunctionProps.builder()
                .code(Code.fromAsset("lambda"))
                .handler(handler)
                .runtime(Runtime.NODEJS_14_X)
                .environment(lambdaEnvMap)
                .timeout(Duration.seconds(30))
                .memorySize(512)
                .build();
    }


}
