package com.smartthings.sdk.smartapp.core.extensions;

import com.smartthings.sdk.smartapp.core.EventSpec;
import com.smartthings.sdk.smartapp.core.Handler;
import com.smartthings.sdk.smartapp.core.internal.handlers.DefaultEventHandler;
import java.util.function.Consumer;

/**
 * Marker interface for delineating a handler for Event lifecycle.
 */
public interface EventHandler extends Handler {

    static EventHandler of (Consumer<? super EventSpec> consumer) {
        DefaultEventHandler.EventSpecImpl spec = new DefaultEventHandler.EventSpecImpl();
        consumer.accept(spec);
        return spec.build();
    }
}
