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
                .name("itemId")
                .type(AttributeType.STRING)
                .build();
        tableProps = TableProps.builder()
                .tableName("items")
                .partitionKey(partitionKey)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
        Table dynamodbTable = new Table(this, "items", tableProps);


        Map<String, String> lambdaEnvMap = new HashMap<>();
        lambdaEnvMap.put("TABLE_NAME", dynamodbTable.getTableName());
        lambdaEnvMap.put("PRIMARY_KEY", "itemId");


        Function getOneItemFunction = new Function(this, "getOneItemFunction",
                getLambdaFunctionProps(lambdaEnvMap, "get-one.handler"));
        Function getAllItemsFunction = new Function(this, "getAllItemsFunction",
                getLambdaFunctionProps(lambdaEnvMap, "get-all.handler"));
        Function createItemFunction = new Function(this, "createItemFunction",
                getLambdaFunctionProps(lambdaEnvMap, "create.handler"));
        Function updateItemFunction = new Function(this, "updateItemFunction",
                getLambdaFunctionProps(lambdaEnvMap, "update-one.handler"));
        Function deleteItemFunction = new Function(this, "deleteItemFunction",
                getLambdaFunctionProps(lambdaEnvMap, "delete-one.handler"));


        dynamodbTable.grantReadWriteData(getOneItemFunction);
        dynamodbTable.grantReadWriteData(getAllItemsFunction);
        dynamodbTable.grantReadWriteData(createItemFunction);
        dynamodbTable.grantReadWriteData(updateItemFunction);
        dynamodbTable.grantReadWriteData(deleteItemFunction);

        RestApi api = new RestApi(this, "itemsApi",
                RestApiProps.builder().restApiName("Items Service").build());

        IResource items = api.getRoot().addResource("items");

        Integration getAllIntegration = new LambdaIntegration(getAllItemsFunction);
        items.addMethod("GET", getAllIntegration);

        Integration createOneIntegration = new LambdaIntegration(createItemFunction);
        items.addMethod("POST", createOneIntegration);
        addCorsOptions(items);


        IResource singleItem = items.addResource("{id}");
        Integration getOneIntegration = new LambdaIntegration(getOneItemFunction);
        singleItem.addMethod("GET", getOneIntegration);

        Integration updateOneIntegration = new LambdaIntegration(updateItemFunction);
        singleItem.addMethod("PATCH", updateOneIntegration);

        Integration deleteOneIntegration = new LambdaIntegration(deleteItemFunction);
        singleItem.addMethod("DELETE", deleteOneIntegration);
        addCorsOptions(singleItem);
    }


    private void addCorsOptions(IResource item) {
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

        item.addMethod("OPTIONS", methodIntegration, methodOptions);
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
