package com.myorg;

import software.amazon.awscdk.App;

public final class FinanceBackendApp {
    public static void main(final String[] args) {
        App app = new App();

        new FinanceBackendStack(app, "FinanceBackendStack");

        app.synth();
    }
}
