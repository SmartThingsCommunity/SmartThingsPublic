package app;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.smartthings.sdk.client.ApiClient;
import com.smartthings.sdk.smartapp.core.Response;
import com.smartthings.sdk.smartapp.core.SmartAppDefinition;
import com.smartthings.sdk.smartapp.core.extensions.HttpVerificationService;
import com.smartthings.sdk.smartapp.core.extensions.InstallHandler;
import com.smartthings.sdk.smartapp.core.extensions.PingHandler;
import com.smartthings.sdk.smartapp.core.extensions.UninstallHandler;
import com.smartthings.sdk.smartapp.core.extensions.UpdateHandler;
import com.smartthings.sdk.smartapp.core.extensions.contextstore.DefaultInstalledAppContextStore;
import com.smartthings.sdk.smartapp.core.internal.handlers.DefaultPingHandler;
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest;
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse;
import com.smartthings.sdk.smartapp.core.models.InstallResponseData;
import com.smartthings.sdk.smartapp.core.models.UninstallResponseData;
import com.smartthings.sdk.smartapp.core.models.UpdateResponseData;
import com.smartthings.sdk.smartapp.core.service.TokenRefreshService;
import com.smartthings.sdk.smartapp.core.service.TokenRefreshServiceImpl;
import com.smartthings.sdk.smartapp.extensions.contextstore.dynamodb.DynamoDBInstalledAppContextStore;
import com.smartthings.sdk.smartapp.spring.SpringSmartAppDefinition;


@Configuration
public class AppConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(AppConfiguration.class);

    // We don't need to include a PingHandler because the default is sufficient.
    // This is included here to help with debugging a new SmartApp.
    @Bean
    public PingHandler pingHandler() {
        return new DefaultPingHandler() {
            @Override
            public ExecutionResponse handle(ExecutionRequest executionRequest) throws Exception {
                LOG.debug("PING: executionRequest = " + executionRequest);
                return super.handle(executionRequest);
            }
        };
    }

    // In a real application (and perhaps even a future version of this
    // example), some of these simple handlers might be more complicated and it
    // might make sense to move them out into their own classes tagged with
    // @Component.
    @Bean
    public InstallHandler installHandler() {
        return executionRequest -> {
            LOG.debug("INSTALL: executionRequest = " + executionRequest);
            return Response.ok(new InstallResponseData());
        };
    }

    @Bean
    public UpdateHandler updateHandler() {
        // The update lifecycle event is called when the user updates
        // configuration options previously set via the install lifecycle
        // event so this should be similar to that handler.
        return executionRequest -> {
            LOG.debug("UPDATE: executionRequest = " + executionRequest);
            return Response.ok(new UpdateResponseData());
        };
    }

    // For simple things like uninstall, we can just implement them in-place
    // like this.
    @Bean
    public UninstallHandler uninstallHandler() {
        return executionRequest -> {
            LOG.debug("UNINSTALL: executionRequest = " + executionRequest);
            return Response.ok(new UninstallResponseData());
        };
    }

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    public ApiClient apiClient() {
        return new ApiClient();
    }

    @Bean
    public HttpVerificationService httpVerificationService() {
        return new HttpVerificationService(httpClient());
    }

    @Bean
    TokenRefreshService tokenRefreshService(@Value("${smartThingsOAuthClientId}") String clientId,
            @Value("${smartThingsOAuthClientSecret}") String clientSecret) {
        return new TokenRefreshServiceImpl(clientId, clientSecret, httpClient());
    }

    @Bean
    public DefaultInstalledAppContextStore installedAppContextStore(
            TokenRefreshService tokenRefreshService) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        return new DynamoDBInstalledAppContextStore(dynamoDB, tokenRefreshService);
    }

    @Bean
    public SmartAppDefinition smartAppDefinition(ApplicationContext applicationContext) {
        // SpringSmartAppDefinition can find all of the necessary components in
        // the given ApplicationContext.
        return SpringSmartAppDefinition.of(applicationContext);

        // Alternatively, you could get all of the components yourself and build the definition.
//        return new SpringSmartAppDefinition(pingHandler(), configurationHandler, installHandler(),
//            updateHandler(), null, eventHandler, null, Collections.emptyList(), Collections.emptyList());
    }
}
