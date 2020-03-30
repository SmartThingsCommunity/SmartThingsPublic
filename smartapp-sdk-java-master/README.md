# SmartThings SmartApp SDK Java (Preview) ![SmartThings](docs/smartthings-logo.png) ![Java](docs/java-logo.png)

<p align="center">
<a href="https://search.maven.org/search?q=g:%22com.smartthings.sdk%22"><img src="https://img.shields.io/maven-central/v/com.smartthings.sdk/smartapp-core.svg?label=Maven%20Central" /></a>
<a href="https://circleci.com/gh/SmartThingsCommunity/smartapp-sdk-java/tree/master"><img src="https://circleci.com/gh/SmartThingsCommunity/smartapp-sdk-java/tree/master.svg?style=svg&circle-token=21da1691c64a0b734e55ada5d591b477347a2936" /></a>
<a href="https://codecov.io/gh/SmartThingsCommunity/smartapp-sdk-java"><img src="https://codecov.io/gh/SmartThingsCommunity/smartapp-sdk-java/branch/master/graph/badge.svg?token=Zy5sgPRzLd"></a>
<a href="https://snyk.io/test/github/SmartThingsCommunity/smartapp-sdk-java"><img src="https://snyk.io/test/github/SmartThingsCommunity/smartapp-sdk-java/badge.svg" alt="Known Vulnerabilities" data-canonical-src="https://snyk.io/test/github/SmartThingsCommunity/smartapp-sdk-java" style="max-width:100%;"></a>
    <a href="https://smartthingsdev.slack.com/messages/CG8D8PS6B"><img src="https://badgen.net/badge//smartthingsdev?icon=slack" /></a>
</p>

> **Welcome!**  This SDK is currently in _**preview**_ and is not recommended for use in production applications at this time. We welcome your bug reports, feature requests, and contributions.

This SDK includes a set of JVM libraries for building **webhook** and **AWS Lambda** SmartApps, and interacting with
the public SmartThings REST API.

## Prerequisites

* Java 1.8+
* [SmartThings developer](https://smartthings.developer.samsung.com/workspace/) account

## Adding the SDK to your build

Several artifacts are published to the Maven central repository under the `com.smartthings.sdk` group.

* `smartapp-core` - Core SmartApp Framework
  * `smartapp-guice` - Extension library for use with Google Guice
  * `smartapp-spring` - Extension library for use with Spring Dependency Injection
  * `smartapp-contextstore-dynamodb` - Extension library to use DynamoDB to
    store installed application context data.
* `smartthings-client` - Library for working with SmartThings APIs

Import the library dependencies as needed:

<details>
    <summary>Apache Maven</summary>

```xml
<dependency>
  <groupId>com.smartthings.sdk</groupId>
  <artifactId>smartapp-core</artifactId>
  <version>0.0.4-PREVIEW</version>
  <type>pom</type>
</dependency>
```

</details>

<details>
    <summary>Gradle Groovy DSL</summary>

```groovy
implementation 'com.smartthings.sdk:smartapp-core:0.0.4-PREVIEW'
```

</details>
<br>

If you prefer, the artifacts can be downloaded directly from [Maven Central](https://search.maven.org/search?q=g:com.smarrthings.sdk).

## Getting Started

### What is a SmartApp?

SmartApps are an example of a
[SmartThings Automation](https://smartthings.developer.samsung.com/develop/getting-started/automation.html).
Automations allow users to control the SmartThings ecosystem without manual intervention. Creating a SmartApp allows
you to control and get status notifications from SmartThings devices using the
[SmartThings REST API](https://smartthings.developer.samsung.com/develop/api-ref/st-api.html).

**Webhook** SmartApps are any publicly-accessible web server that will receive a POST request payload.

**AWS Lambda** SmartApps are hosted in the Amazon Web Services cloud and are invoked by **ARN** instead of a
public-DNS address.

![automation smartapp](https://smartthings.developer.samsung.com/docs/smartapps/img/automation_smartapp.png)

To learn more about what a SmartApp is and how you can create interesting automations, please visit the [developer portal documentation](https://smartthings.developer.samsung.com/develop/guides/smartapps/basics.html).

### Basics

Take a quick look at how SmartApps are declared in various languages.

<details>
<summary>Kotlin (click to toggle)</summary>

```kotlin
package app

val smartApp: SmartApp = SmartApp.of { spec ->
    spec
        .configuration(Configuration())
        .install {
            Response.ok(InstallResponseData())
        }
        .update {
            Response.ok(UpdateResponseData())
        }
        .event {
            Response.ok(EventResponseData())
        }
        .uninstall {
            Response.ok(UninstallResponseData())
        }
}

fun Application.main() {
    install(Routing) {
        post("/smartapp") {
            call.respond(smartApp.execute(call.receive()))
        }
    }
}

```

</details>

<details>
<summary>Groovy (click to toggle)</summary>

```groovy
    SmartApp smartApp = SmartApp.of { spec ->
        spec
            .install({ req ->
                // create subscriptions
                Response.ok()
            })
            .update({ req ->
                // delete subscriptions
                // create subscriptions
                Response.ok()
            })
            .configuration({ req ->
                ConfigurationResponseData data = ...// build config
                Response.ok(data)
            })
            .event(EventHandler.of { eventSpec ->
                eventSpec
                    .onSubscription("switch", { event ->
                       // do something
                    })
                    .onSchedule("nightly", { event ->
                       // do something
                    })
                    .onEvent(
                        { event ->
                            // test event
                            true
                        },
                        { event ->
                            // do something
                        }
                    )
            })
    }
```

</details>

<details>
<summary>Java (click to toggle)</summary>

```java
    private final SmartApp smartApp = SmartApp.of(spec ->
        spec
            .install(request -> {
                return Response.ok();
            })
            .update(request -> {
                return Response.ok(UpdateResponseData.newInstance());
            })
            .configuration(request -> {
                return Response.ok(ConfigurationReponseData.newInstance());
            })
            .event(request -> {
                EventData eventData = request.getEventData();
                EventHandler.of(eventSpec ->
                        eventSpec
                                .onEvent(event -> {
                                    // when this predicate is true...
                                    return true;
                                }, event -> {
                                    // ...do something with event
                                })
                                .onSchedule("nightly", event -> {
                                    // do something
                                })
                                .onSubscription("switch", event -> {
                                    // do something
                                })
                );
                return Response.ok(EventResponseData.newInstance());
            })
        );
```

</details>

### Runnable Examples

Several simple examples of using the sdk are included in the examples directory.

#### [kotlin-smartapp](examples/kotlin-smartapp) ([Documentation](examples/kotlin-smartapp/README.md)) ![kotlin-logo](docs/kotlin-logo.png) ![ktor-logo](docs/ktor-logo.png)
This Kotlin example implements the Java `smartapp-core` library with a simple [Ktor](https://ktor.io/) server.

#### [java-ratpack-guice-smartapp](examples/java-ratpack-guice-smartapp) ([Documentation](examples/java-ratpack-guice-smartapp/README.md)) ![java-logo](docs/java-logo.png) ![ratpack-logo](docs/ratpack-logo.png)
This Java example implements the Java `smartapp-core` library with a Ratpack server and uses Guice for dependency management.

#### [java-springboot-smartapp](examples/java-springboot-smartapp) ([Documentation](examples/java-springboot-smartapp/README.md)) ![java-logo](docs/java-logo.png) ![spring-logo](docs/spring-logo.png)
This Java example implements the Java `smartapp-core` library using Spring Boot.

#### [java-lambda-smartapp](examples/java-lambda-smartapp) ([Documentation](examples/java-lambda-smartapp/README.md)) ![java-logo](docs/java-logo.png) ![aws-logo](docs/aws-logo.png)
This Java example implements the Java `smartapp-core` library as an AWS Lambda.

## Documentation

### Modules

#### [smartapp-core](/smartapp-core) ([Documentation](smartapp-core/README.md))
Core SmartApp framework. Provides abilities for defining a SmartApp that could be used in many environments - AWS Lambda / Dropwizard / Ratpack / etc

#### [smartthings-client](/smartthings-client) ([Documentation](smartthings-client/README.md))
An API library that provides useful utilities for working with the Subscription / Schedules / Device APIs

### Extension Libraries

#### [smartapp-guice](/smartapp-guice) ([Documentation](smartapp-guice/README.md))
An extension library that provides support for building a SmartApp with Guice dependency injection.

#### [smartapp-spring](/smartapp-spring) ([Documentation](smartapp-spring/README.md))
An extension library that provides support for building a SmartApp with Spring dependency injection.

#### [smartapp-contextstore-dynamodb](/smartapp-contextstore-dynamodb) ([Documentation](smartapp-contextstore-dynamodb/README.md))
An extension library that implements a [context store](smartapp-core/README.md#context-store) using DynamoDB.

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
