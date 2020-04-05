# smartapp-core

Create easily-readable SmartThings SmartApps with this DSL-like fluent Java library.

This library aims to make developing your own JVM-based SmartApps easy, intuitive, and flexible. You can write and distribute your SmartApps in any way you'd like.

## Prerequisites

* Java 1.8+
* [SmartThings developer](https://smartthings.developer.samsung.com/workspace/) account

## Adding the library to your build

Include the `smartapp-core` Maven dependency:

```xml
<dependency>
    <groupId>com.smartthings.sdk</groupId>
    <artifactId>smartapp-core</artifactId>
    <version>0.0.4-PREVIEW</version>
</dependency>
```

If you're using Gradle:

```gradle
dependencies {
    compile 'com.smartthings.sdk:smartapp-core:0.0.4-PREVIEW'
}
```

If you do not use Maven or Gradle, jars can be downloaded from the
[central Maven repository](https://search.maven.org/search?q=g:com.smartthings.sdk%20a:smartapp-core).

## Getting Started

> Note: Although you can easily use this library alongside any web framework of your choice, the example below uses Spring Boot.

1. Create a definition of your SmartApp (_fig. A_)
2. Listen for incoming HTTP requests and execute the SmartApp (_fig. B_)
3. Add your additional response logic.

For more information on the SmartApp API reference (request/response expectations) please [view the API reference documentation](https://smartthings.developer.samsung.com/develop/api-ref/smartapps-v1.html), or for additional insight, the [SmartApp guide](https://smartthings.developer.samsung.com/develop/guides/smartapps/basics.html).

_Fig. A_ Declare the SmartApp specification
```java
public class AppController {
    private final SmartApp smartApp = SmartApp.of(spec ->
        spec
            .install(request -> Response.ok())
            .update(request -> Response.ok(UpdateResponseData.newInstance()))
            .configuration(request -> Response.ok(ConfigurationResponseData.newInstance()))
            .event(request -> Response.ok(EventResponseData.newInstance()))
            .uninstall(request -> Response.ok(UninstallResponseData.newInstance()))
    );
}
```

_Fig. B_ Execute the SmartApp for an incoming response
```java
public class AppController {
    /* SmartApp declaration removed for brevity */

    @RequestMapping("/")
    public ExecutionResponse index(@RequestBody ExecutionRequest executionRequest) {
         return smartApp.execute(executionRequest);
    }
}
```

## Documentation

### Required handlers

By default, some lifecycle events are handled for you. If you wish, you may override them at any time by creating the definition.

| Lifecycle Handler             | Required | Has Default Implementation |
|:------------------------------|:--------:|:--------------------------:|
| `AppLifecycle.EVENT`          | ✅       | 🚫                         |
| `AppLifecycle.INSTALL`        | ✅       | 🚫                         |
| `AppLifecycle.UPDATE`         | ✅       | 🚫                         |
| `AppLifecycle.UNINSTALL`      | ✅       | ✅                         |
| `AppLifecycle.PING`           | 🚫       | ✅                         |
| `AppLifecycle.OAUTH_CALLBACK` | 🚫       | 🚫                         |


### Handling unsupported lifecycle events

With `SmartAppDefinitionSpec.when(Predicate<ExecutionRequest>, Handler)`, you can handle "custom" event lifecycles even if this library does not explicitly support it yet. For example, if the event `EXECUTE` is available in the API, but not in `smartapp-core`, you may use the predicate handler to check if the request matches `"EXECUTE"` and respond accordingly.

### Responses

#### `notFound():ExecutionResponse`

Signifies to the SmartThings platform that the app definition doesn't know about that specific lifecycle event.

#### `ok():ExecutionResponse`
Respond to request with an empty response object.

#### `ok(ConfigurationResponseData):ExecutionResponse`

Respond to initialization and page requests.

#### `ok(EventResponseData):ExecutionResponse`

Respond to event lifecycle requests.

#### `ok(InstallResponseData):ExecutionResponse`

Respond to installation update requests.

#### `ok(PingResponseData):ExecutionResponse`

Respond to ping lifecycle requests which are sent by the platform to ensure your application is online.

#### `ok(UninstallResponseDataok):ExecutionResponse`

Respond to uninstallation requests.

#### `ok(UpdateResponseData):ExecutionResponse`

Respond to configuration update requests.

#### `status(int):ExecutionResponse`

Response to request with an arbitrary HTTP status code.

### Request Preprocessors

If you need to have code called for all lifecycle events before they are
handled normally, you can implement the `RequestPreprocessor` interface and add
your implementation to the SmartApp using the `addRequestPreprocessor` method.

```java
    private final SmartApp smartApp = SmartApp.of(spec ->
        spec
            .install(request -> Response.ok())
            ...
            .addRequestPreprocessor(myPreprocessor)
    );
}
```

### Context Store

Support has been included for managing installed application information via a
"context store". The following implementations are currently available:

  - [DynamoDB](../smartapp-contextstore-dynamodb/README.md)

To create your own implementation, you will need to:

  1. Create a class which implements InstalledAppContextStore which is a DAO
     for managing your context. Consider using DefaultInstalledAppContextStore
     which also implements RequestPreprocessor, making it easy to hook into
     the appropriate SmartApp lifecycle events. DefaultInstalledAppContextStore
     also uses the DefaultAppContextStore which includes common data related
     to the SmartApp.
     1. If you're storing tokens, be sure your `get` method refreshes tokens
        if necessary before returning the context.
     2. Set up a RequestPreprocessor to call your DAO at the appropriate times.
        The easiest way to do this is to implement DefaultAppContextStore as
        mentioned above and pass that into your SmartApp instance on start-up
        using `addRequestPreprocessor`. (If you're using Spring and our Spring
        library, you can simply make the implementation available in the
        `ApplicationContext`.)
  3. Use the `get` method of your DAO to retrieve your context as needed.
  4. Since refresh tokens expire after 30 days, if you expect users to have
     periods of over 30 days with no out-of-band activity, you will need to
     refresh the tokens on a schedule.

### Reference Documentation

Reference documentation can be found here:

https://smartthingscommunity.github.io//smartapp-sdk-java/smartapp-core/javadoc/

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
