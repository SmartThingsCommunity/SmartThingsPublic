package com.smartthings.sdk.smartapp.extensions.contextstore.dynamodb;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.PostConstruct;

import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartthings.sdk.smartapp.core.extensions.contextstore.DefaultInstalledAppContext;
import com.smartthings.sdk.smartapp.core.extensions.contextstore.DefaultInstalledAppContextStore;
import com.smartthings.sdk.smartapp.core.models.ConfigMap;
import com.smartthings.sdk.smartapp.core.models.InstalledApp;
import com.smartthings.sdk.smartapp.core.models.Permissions;
import com.smartthings.sdk.smartapp.core.service.Token;
import com.smartthings.sdk.smartapp.core.service.TokenRefreshService;

public class DynamoDBInstalledAppContextStore implements DefaultInstalledAppContextStore {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DynamoDB dynamoDB;
    private final TokenRefreshService tokenRefreshService;
    private final Clock clock;

    private final String tableName;

    private static final ObjectMapper mapper = new ObjectMapper();

    public DynamoDBInstalledAppContextStore(DynamoDB dynamoDB, TokenRefreshService tokenRefreshService) {
        this.dynamoDB = dynamoDB;
        this.tokenRefreshService = tokenRefreshService;
        clock = Clock.systemUTC();

        tableName = "installedAppContext";
    }

    public DynamoDBInstalledAppContextStore(DynamoDB dynamoDB, TokenRefreshService tokenRefreshService, Clock clock,
            String tableName) {
        this.dynamoDB = dynamoDB;
        this.tokenRefreshService = tokenRefreshService;
        this.clock = clock;
        this.tableName = tableName;
    }

    @PostConstruct
    public void init() {
        try {
            dynamoDB.createTable(tableName, Arrays.asList(new KeySchemaElement("installedAppId", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("installedAppId", "S")), new ProvisionedThroughput(10L, 10L));
        } catch (ResourceInUseException e) {
            log.info("InstalledAppContext table already exists");
        }
    }

    @Override
    public void add(DefaultInstalledAppContext context) {
        Table table = dynamoDB.getTable(tableName);
        InstalledApp installedApp = context.getInstalledApp();
        Token token = context.getToken();
        String configJson;
        try {
            configJson = mapper.writeValueAsString(installedApp.getConfig());
        } catch (JsonProcessingException e) {
            log.error("problem mapping config as JSON", e);
            configJson = null;
        }
        Item item = new Item().withPrimaryKey("installedAppId", context.getInstalledAppId())
                .with("authToken", token.getAccessToken()) // named to match NodeSDK version
                .with("authTokenExpiration", token.getAccessTokenExpiration().toEpochMilli())
                .with("refreshToken", token.getRefreshToken())
                .with("refreshTokenExpiration", token.getRefreshTokenExpiration().toEpochMilli())
                .with("locationId", installedApp.getLocationId()).with("config", configJson)
                .with("permissions", installedApp.getPermissions());
        PutItemOutcome outcome = table.putItem(item);
        if (log.isDebugEnabled()) {
            log.debug("put item outcome = " + outcome);
            log.debug("put item outcome.getItem() = " + outcome.getItem());
            log.debug("put item outcome.getPutItemResult() = " + outcome.getPutItemResult());
        }
    }

    @Override
    public void remove(String installedAppId) {
        Table table = dynamoDB.getTable(tableName);
        DeleteItemOutcome outcome = table.deleteItem("installedAppId", installedAppId);
        if (log.isDebugEnabled()) {
            log.debug("deleted item outcome: " + outcome);
        }
    }

    private DefaultInstalledAppContext contextFromItem(Item item) {
        Permissions permissions = new Permissions();
        List<?> permissionsList = item.getList("permissions");
        if (permissionsList != null) {
            for (Object permission : permissionsList) {
                permissions.add((String) permission);
            }
        }

        ConfigMap config;
        try {
            config = mapper.readValue(item.getString("config"), ConfigMap.class);
        } catch (IOException e) {
            log.error("problem converting stored config JSON ("
                + item.getString("config") + ") back to config", e);
            config = new ConfigMap();
        }
        InstalledApp installedApp = new InstalledApp()
            .installedAppId(item.getString("installedAppId"))
            .locationId(item.getString("locationId"))
            .config(config)
            .permissions(permissions);
        Token token = new Token()
            .accessToken(item.getString("authToken")) // named to match NodeSDK version
            .accessTokenExpiration(Instant.ofEpochMilli(item.getLong("authTokenExpiration")))
            .refreshToken(item.getString("refreshToken"))
            .refreshTokenExpiration(Instant.ofEpochMilli(item.getLong("refreshTokenExpiration")));

        return new DefaultInstalledAppContext()
            .installedApp(installedApp)
            .token(token);
    }

    private DefaultInstalledAppContext readFromDb(String installedAppId) {
        Table table = dynamoDB.getTable(tableName);
        Item item = table.getItem("installedAppId", installedAppId);

        if (item == null) {
            if (log.isInfoEnabled()) {
                log.info("could not find installed app " + installedAppId);
            }
            return null;
        }

        return contextFromItem(item);
    }

    private DefaultInstalledAppContext refreshed(DefaultInstalledAppContext context) {
        if (context == null) {
            log.info("null context passed to refreshed");
            return null;
        }

        Instant now = clock.instant();

        if (context.getToken().getAccessTokenExpiration().isAfter(now.plus(Duration.ofHours(1)))) {
            // We still have at least an hour left on the current token.
            return context;
        }

        Token refreshedToken = tokenRefreshService.refresh(context.getToken());
        DefaultInstalledAppContext updatedContext = context.token(refreshedToken);
        update(updatedContext);
        return updatedContext;
    }

    @Override
    public DefaultInstalledAppContext get(String installedAppId) {
        return refreshed(readFromDb(installedAppId));
    }

    @Override
    public Stream<DefaultInstalledAppContext> get() {
        Table table = dynamoDB.getTable(tableName);
        ItemCollection<ScanOutcome> scanItems = table.scan();
        Iterator<Item> iter = scanItems.iterator();
        Iterable<Item> iterable = () -> iter;
        return StreamSupport.stream(iterable.spliterator(), false)
            .map(item -> refreshed(contextFromItem(item)));
    }
}
