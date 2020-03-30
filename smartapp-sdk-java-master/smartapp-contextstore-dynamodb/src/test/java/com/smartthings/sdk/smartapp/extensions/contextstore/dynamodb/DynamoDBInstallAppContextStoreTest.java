package com.smartthings.sdk.smartapp.extensions.contextstore.dynamodb;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartthings.sdk.smartapp.core.extensions.contextstore.DefaultInstalledAppContext;
import com.smartthings.sdk.smartapp.core.models.ConfigMap;
import com.smartthings.sdk.smartapp.core.models.InstalledApp;
import com.smartthings.sdk.smartapp.core.models.Permissions;
import com.smartthings.sdk.smartapp.core.service.Token;
import com.smartthings.sdk.smartapp.core.service.TokenRefreshService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBInstallAppContextStoreTest {
    Instant now = Instant.now();
    Clock clock = Clock.fixed(now, ZoneId.systemDefault());

    DynamoDB dynamoDB = mock(DynamoDB.class);
    TokenRefreshService tokenRefreshService = mock(TokenRefreshService.class);
    Table table = mock(Table.class);

    DynamoDBInstalledAppContextStore tester = new DynamoDBInstalledAppContextStore(dynamoDB, tokenRefreshService, clock,
            "installedAppContext");

    ConfigMap config = new ConfigMap();
    Permissions permissions = new Permissions();
    InstalledApp installedApp = new InstalledApp().installedAppId("sample installed app id").locationId("location id")
            .config(config).permissions(permissions);
    Token token = new Token()
        .accessToken("access token").accessTokenExpiration(now.plus(Duration.ofDays(1)))
        .refreshToken("refresh token").refreshTokenExpiration(now.plus(Duration.ofDays(30)));
    DefaultInstalledAppContext context = new DefaultInstalledAppContext().installedApp(installedApp).token(token);

    ObjectMapper mapper = new ObjectMapper();

    public DynamoDBInstallAppContextStoreTest() {
        when(dynamoDB.getTable("installedAppContext")).thenReturn(table);
        permissions.add("perm1");
        permissions.add("perm2");
    }

    @Test
    public void initCreatesTable() {
        when(dynamoDB.createTable(eq("installedAppContext"),
                eq(Arrays.asList(new KeySchemaElement("installedAppId", KeyType.HASH))),
                eq(Arrays.asList(new AttributeDefinition("installedAppId", "S"))), any())).thenReturn(table);

        tester.init();

        verify(dynamoDB).createTable(eq("installedAppContext"),
                eq(Arrays.asList(new KeySchemaElement("installedAppId", KeyType.HASH))),
                eq(Arrays.asList(new AttributeDefinition("installedAppId", "S"))), any());
    }

    @Test
    public void initIsFineWithTableAlreadyExisting() {
        when(dynamoDB.createTable(eq("installedAppContext"),
                eq(Arrays.asList(new KeySchemaElement("installedAppId", KeyType.HASH))),
                eq(Arrays.asList(new AttributeDefinition("installedAppId", "S"))), any()))
                        .thenThrow(new ResourceInUseException("table already exists"));

        tester.init();

        verify(dynamoDB).createTable(eq("installedAppContext"),
                eq(Arrays.asList(new KeySchemaElement("installedAppId", KeyType.HASH))),
                eq(Arrays.asList(new AttributeDefinition("installedAppId", "S"))), any());
    }

    @Test
    public void addCallsPutItemProperly() throws JsonProcessingException {
        tester.add(context);

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(table).putItem(itemCaptor.capture());
        Item captured = itemCaptor.getValue();

        assertEquals("sample installed app id", captured.get("installedAppId"));
        assertEquals("location id", captured.get("locationId"));
        assertEquals(mapper.writeValueAsString(config), captured.get("config"));
        Permissions permissionResults = new Permissions();
        for (Object permission : captured.getList("permissions")) {
            permissionResults.add((String) permission);
        }
        assertEquals(permissions, permissionResults);

        assertEquals("access token", captured.get("authToken"));
        assertEquals(token.getAccessTokenExpiration().toEpochMilli(),
            captured.getLong("authTokenExpiration"));
        assertEquals("refresh token", captured.get("refreshToken"));
        assertEquals(token.getRefreshTokenExpiration().toEpochMilli(),
            captured.getLong("refreshTokenExpiration"));
    }

    @Test
    public void getReconstitutesProperly() throws JsonProcessingException {
        Item item = new Item()
            .withPrimaryKey("installedAppId", context.getInstalledAppId())
            .with("authToken", token.getAccessToken())
            .with("authTokenExpiration", token.getAccessTokenExpiration().toEpochMilli())
            .with("refreshToken", token.getRefreshToken())
            .with("refreshTokenExpiration", token.getRefreshTokenExpiration().toEpochMilli())
            .with("locationId", installedApp.getLocationId())
            .with("config", mapper.writeValueAsString(installedApp.getConfig()))
            .with("permissions", installedApp.getPermissions());
        when(table.getItem("installedAppId", (Object) "installed app id")).thenReturn(item);

        DefaultInstalledAppContext result = tester.get("installed app id");
        assertEquals(context, result);
    }

    @Test
    public void getDoesNotRefreshNewToken() throws JsonProcessingException {
        token.accessTokenExpiration(now.plus(Duration.ofMinutes(61)));
        Item item = new Item()
            .withPrimaryKey("installedAppId", context.getInstalledAppId())
            .with("authToken", token.getAccessToken())
            .with("authTokenExpiration", token.getAccessTokenExpiration().toEpochMilli())
            .with("refreshToken", token.getRefreshToken())
            .with("refreshTokenExpiration", token.getRefreshTokenExpiration().toEpochMilli())
            .with("locationId", installedApp.getLocationId())
            .with("config", mapper.writeValueAsString(installedApp.getConfig()))
            .with("permissions", installedApp.getPermissions());

        when(table.getItem("installedAppId", (Object) "installed app id")).thenReturn(item);

        DefaultInstalledAppContext result = tester.get("installed app id");
        assertEquals(context, result);

        verify(tokenRefreshService, never()).refresh(any());
    }

    @Test
    public void getRefreshesOldToken() throws JsonProcessingException {
        token.accessTokenExpiration(now.plus(Duration.ofMinutes(59)));
        Token newToken = new Token()
            .accessToken("updated access token")
            .accessTokenExpiration(now.plus(Duration.ofDays(1)))
            .refreshToken("updated refresh token")
            .refreshTokenExpiration(now.plus(Duration.ofDays(30)));
        when(tokenRefreshService.refresh(token)).thenReturn(newToken);

        Item item = new Item()
            .withPrimaryKey("installedAppId", context.getInstalledAppId())
            .with("authToken", token.getAccessToken())
            .with("authTokenExpiration", token.getAccessTokenExpiration().toEpochMilli())
            .with("refreshToken", token.getRefreshToken())
            .with("refreshTokenExpiration", token.getRefreshTokenExpiration().toEpochMilli())
            .with("locationId", installedApp.getLocationId())
            .with("config", mapper.writeValueAsString(installedApp.getConfig()))
            .with("permissions", installedApp.getPermissions());

        when(table.getItem("installedAppId", (Object) "installed app id")).thenReturn(item);

        DefaultInstalledAppContext result = tester.get("installed app id");
        DefaultInstalledAppContext expected = context.token(newToken);
        assertEquals(expected, result);

        verify(tokenRefreshService).refresh(token);
    }
}
