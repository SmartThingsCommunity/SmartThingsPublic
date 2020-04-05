package com.smartthings.sdk.smartapp.core;

import com.smartthings.sdk.smartapp.core.models.*;


@SuppressWarnings({"PMD.UseUtilityClass", "PMD.ClassNamingConventions"})
public final class Response {
    public static ExecutionResponse ok() {
        return new ExecutionResponse()
            .statusCode(200);
    }

    public static ExecutionResponse ok(InstallResponseData data) {
        return new ExecutionResponse()
            .statusCode(200)
            .installData(data);
    }

    public static ExecutionResponse ok(UpdateResponseData data) {
        return new ExecutionResponse()
            .statusCode(200)
            .updateData(data);
    }

    public static ExecutionResponse ok(UninstallResponseData data) {
        return new ExecutionResponse()
            .statusCode(200)
            .uninstallData(data);
    }

    public static ExecutionResponse ok(EventResponseData data) {
        return new ExecutionResponse()
            .statusCode(200)
            .eventData(data);
    }

    public static ExecutionResponse ok(ConfigurationResponseData data) {
        return new ExecutionResponse()
            .statusCode(200)
            .configurationData(data);
    }

    public static ExecutionResponse ok(PingResponseData data) {
        return new ExecutionResponse()
            .statusCode(200)
            .pingData(data);
    }


    public static ExecutionResponse notFound() {
        return new ExecutionResponse()
            .statusCode(404);
    }

    public static ExecutionResponse status(int statusCode) {
        return new ExecutionResponse()
            .statusCode(statusCode);
    }
}
