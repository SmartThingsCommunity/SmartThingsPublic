# smartapp-spring

This is a simple library to make declaring Smart App lifecycle handlers as Spring components easy.

## Prerequisites

* Java 1.8+
* Spring 4+
* [smartapp-core](../smartapp-core)
* [SmartThings developer](https://smartthings.developer.samsung.com/workspace/) account

## Adding the library to your build

Include the `smartapp-core` Maven dependency:

```xml
<dependency>
    <groupId>com.smartthings.sdk</groupId>
    <artifactId>smartapp-spring</artifactId>
    <version>0.0.4-PREVIEW</version>
</dependency>
```

If you're using Gradle:

```gradle
dependencies {
    compile 'com.smartthings.sdk:smartapp-spring:0.0.4-PREVIEW'
}
```

If you do not use Maven or Gradle, jars can be downloaded from the
[central Maven repository](https://search.maven.org/search?q=g:com.smartthings.sdk%20a:smartapp-spring).

## Getting Started

This simple library adds to smartapp-core by giving you the ability to create a `SmartAppDefinition`
using handlers defined as components. To do this, you simply call `SpringSmartAppDefinition.of`
and give it your `ApplicationContext`.

```java
SmartAppDefinition smartAppDefinition = SpringSmartAppDefinition.of(applicationContext);
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
