package com.smartthings.sdk.smartapp.guice.internal;

import com.google.inject.Module;
import com.smartthings.sdk.smartapp.guice.BindingsSpec;

import java.util.List;

public class DefaultBindingsSpec implements BindingsSpec {

    private final List<Module> modules;

    public DefaultBindingsSpec(List<Module> modules) {
        this.modules = modules;
    }

    @Override
    public BindingsSpec module(Module module) {
        this.modules.add(module);
        return this;
    }

    @Override
    public BindingsSpec module(Class<? extends Module> moduleClass) {
        return module(createModule(moduleClass));
    }

    private <T extends Module> T createModule(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Module " + clazz.getName() + " is not reflectively instantiable", e);
        }
    }
}
