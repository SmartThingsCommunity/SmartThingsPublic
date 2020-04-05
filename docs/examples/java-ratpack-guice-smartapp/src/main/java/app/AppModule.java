package app;

import app.handlers.AppConfigurationHandler;
import app.handlers.AppEventHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.smartthings.sdk.smartapp.core.Response;
import com.smartthings.sdk.smartapp.core.extensions.*;
import com.smartthings.sdk.smartapp.core.internal.handlers.DefaultPingHandler;
import com.smartthings.sdk.smartapp.core.models.*;


public class AppModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(AppModule.class);

    @Override
    protected void configure() {
        // Here are a couple of examples of using separate classes to implement lifecycle events.
        bind(EventHandler.class).to(AppEventHandler.class).in(Scopes.SINGLETON);
        bind(ConfigurationHandler.class).to(AppConfigurationHandler.class).in(Scopes.SINGLETON);
    }

    // We don't need to include a PingHandler because the default is sufficient. This is included
    // here to help with debugging a new SmartApp.
    @Provides
    @Singleton
    public PingHandler pingHandler() {
        return new DefaultPingHandler() {
            @Override
            public ExecutionResponse handle(ExecutionRequest executionRequest) throws Exception {
                LOG.debug("PING: executionRequest = " + executionRequest);
                return super.handle(executionRequest);
            }
        };
    }

    // In a real application (and perhaps even a future version of this example), some of these simple
    // handlers might be more complicated and it might make sense to move them out into their own classes
    // (and then bind them in configure above).
    @Provides
    @Singleton
    public InstallHandler installHandler() {
        return executionRequest -> {
            LOG.debug("INSTALL: executionRequest = " + executionRequest);
            return Response.ok(new InstallResponseData());
        };
    }

    @Provides
    @Singleton
    public UpdateHandler updateHandler() {
        // The update lifecycle event is called when the user updates configuration options previously set via
        // the install lifecycle event so this should be similar to that handler.
        return executionRequest -> {
            LOG.debug("UPDATE: executionRequest = " + executionRequest);
            return Response.ok(new UpdateResponseData());
        };
    }

    // For simple things like uninstall, we can just implement them in-place like this.
    @Provides
    @Singleton
    public UninstallHandler uninstallHandler() {
        return executionRequest -> {
            LOG.debug("UNINSTALL: executionRequest = " + executionRequest);
            return Response.ok(new UninstallResponseData());
        };
    }
}
