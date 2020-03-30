package com.smartthings.sdk.smartapp.guice;

import com.google.inject.Module;

public interface BindingsSpec {

    /**
     * Adds the bindings from the given module.
     *
     * @param module module whose bindings should be added
     * @return this
     */
    BindingsSpec module(Module module);

    /**
     * Adds the bindings from the given module.
     *
     * @param moduleClass type of the module whose bindings should be added
     * @return this
     */
    @SuppressWarnings("unchecked")
    BindingsSpec module(Class<? extends Module> moduleClass);
}
