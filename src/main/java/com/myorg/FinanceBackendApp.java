package com.myorg;

import software.amazon.awscdk.App;

/**
 *entrypoint for the CDK application it will load the stack defined in ~/CdkWorkshopStack.java
 */
public final class FinanceBackendApp {
    public static void main(final String[] args) {
        App app = new App();

        new FinanceBackendStack(app, "FinanceBackendStack");

        app.synth();
    }
}
