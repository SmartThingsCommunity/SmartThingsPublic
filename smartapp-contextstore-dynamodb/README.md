# smartapp-contextstore-dynamodb

This library adds support for a context store using DynamoDB. This will help
keep track of tokens, configuration and other application related data stored
in a `DefaultInstalledAppContext` instance.

## Prerequisites

* Java 1.8+
* [SmartThings developer](https://smartthings.developer.samsung.com/workspace/) account
* [Amazon Web Services](https://aws.amazon.com/) account
* [smartapp-core](../smartapp-core)

## Adding the library to your build

Include the `smartapp-core` Maven dependency:

```xml
<dependency>
    <groupId>com.smartthings.sdk</groupId>
    <artifactId>smartapp-contextstore-dynamodb</artifactId>
    <version>0.0.4-PREVIEW</version>
</dependency>
```

If you're using Gradle:

```gradle
dependencies {
    compile 'com.smartthings.sdk:smartapp-contextstore-dynamodb:0.0.4-PREVIEW'
}
```

If you do not use Maven or Gradle, jars can be downloaded from the
[central Maven repository](https://search.maven.org/search?q=g:com.smartthings.sdk%20a:smartapp-contextstore-dynamodb).

## Getting Started

To use this library, you'll need to create an instance of
`DynamoDBInstalledAppContextStore` and wire it into your SmartApp instance.
`DynamoDBInstalledAppContextStore` is a `RequestPreprocessor` that handles
the appropriate lifecycle events to keep the backing store up-to-date with
application installs, updates and uninstalls.

```java
AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
DynamoDB dynamoDB = new DynamoDB(client);
TokenRefreshService tokenRefreshService = new TokenRefreshServiceImpl(clientId, clientSecret);
DynamoDBInstalledAppContextStore contextStore =
    new DynamoDBInstalledAppContextStore(dynamoDB, tokenRefreshService);

...

SmartApp smartApp = SmartApp.of(spec ->
    spec
        .install(request -> Response.ok())
        .update(request -> Response.ok(UpdateResponseData.newInstance()))
        .configuration(request -> Response.ok(ConfigurationResponseData.newInstance()))
        .event(request -> Response.ok(EventResponseData.newInstance()))
        .uninstall(request -> Response.ok(UninstallResponseData.newInstance()))
        .addRequestPreprocessor(contextStore)
);
```

`DynamoDBInstalledAppContextStore` also implements
`DefaultInstalledAppContextStore` so you can use it directly to retrieve the
context for a given installed app id.

```java
// later you can refer to the context store to get the context for a given
// installedAppId
DefaultInstalledAppContext context = contextStore.get(installedAppId);
```

For a complete example, see [Java Spring Boot example SmartApp](/examples/java-springboot-smartapp).

## More about SmartThings

If you are not familiar with SmartThings, we have
[extensive on-line documentation](https://smartthings.developer.samsung.com/develop/index.html).

To create and manage your services and devices on SmartThings, create an account in the
[developer workspace](https://devworkspace.developer.samsung.com/).

The [SmartThings Community](https://community.smartthings.com/c/developers/) is a good place share and
ask questions.

There is also a [SmartThings reddit community](https://www.reddit.com/r/SmartThings/) where you
can read and share information.

## License and Copyright

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

Copyright 2019 SmartThings, Inc.
