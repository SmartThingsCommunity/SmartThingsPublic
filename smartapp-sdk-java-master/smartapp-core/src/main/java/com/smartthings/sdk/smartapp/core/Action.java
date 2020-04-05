package com.smartthings.sdk.smartapp.core;

@FunctionalInterface
public interface Action<T> {
    void execute(T t) throws Exception;
}
