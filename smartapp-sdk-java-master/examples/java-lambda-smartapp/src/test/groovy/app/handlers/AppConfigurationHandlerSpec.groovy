package app.handlers

import app.handlers.AppConfigurationHandler
import com.smartthings.sdk.smartapp.core.models.*
import spock.lang.Specification


class AppConfigurationHandlerSpec extends Specification {
    private final AppConfigurationHandler configurationHandler = new AppConfigurationHandler()

    void 'properly responds to initialization phase'() {
        given:
        ConfigurationData configurationData = new ConfigurationData()
            .installedAppId(UUID.randomUUID().toString())
            .phase(ConfigurationPhase.INITIALIZE)
            .config(new ConfigMap())
        ExecutionRequest executionRequest = new ExecutionRequest()
            .lifecycle(AppLifecycle.CONFIGURATION)
            .executionId(UUID.randomUUID().toString())
            .locale('en')
            .version('0.1.0')
            .configurationData(configurationData)
            .settings([:])
        ConfigurationResponseData configurationResponseData = new ConfigurationResponseData()
            .initialize(new InitializeSetting()
                .id('init')
                .name('Java Ratpack Guice SmartApp Example')
                .description('Create a SmartApp using Java and Ratpack with Guice')
                .firstPageId('intro')
                .permissions(['r:devices:*', 'x:devices:*']))
        ExecutionResponse expectedResponse = new ExecutionResponse()
            .statusCode(200)
            .configurationData(configurationResponseData)

        when:
        ExecutionResponse executionResponse = configurationHandler.handle(executionRequest)

        then:
        executionResponse == expectedResponse
    }

    void 'properly responds to intro page request'() {
        given:
        ConfigMap configMap = new ConfigMap()
        ConfigEntries configEntries = new ConfigEntries()
        configEntries.add(new ConfigEntry().valueType(ConfigEntry.ValueTypeEnum.STRING))
        configMap['init'] = configEntries
        ConfigurationData configurationData = new ConfigurationData()
            .installedAppId(UUID.randomUUID().toString())
            .phase(ConfigurationPhase.PAGE)
            .pageId('intro')
            .config(configMap)
        ExecutionRequest executionRequest = new ExecutionRequest()
            .lifecycle(AppLifecycle.CONFIGURATION)
            .executionId(UUID.randomUUID().toString())
            .locale('en')
            .version('0.1.0')
            .configurationData(configurationData)
            .settings([:])
        ConfigurationResponseData configurationResponseData = new ConfigurationResponseData()
            .page(new Page()
                .name('This is the first configuration page')
                .pageId('intro')
                .nextPageId('finish')
            .sections([new Section()
                .settings([new DeviceSetting()
                    .id("selectedSwitches")
                    .name('Select a device')
                    .description('Tap to select')
                    .type(SettingType.DEVICE)
                    .multiple(true)
                    .preselect(true)
                    .capabilities(['switch'])
                    .permissions([DeviceSetting.PermissionsEnum.R, DeviceSetting.PermissionsEnum.X])])])
        )
        ExecutionResponse expectedResponse = new ExecutionResponse()
            .statusCode(200)
            .configurationData(configurationResponseData)

        when:
        ExecutionResponse executionResponse = configurationHandler.handle(executionRequest)

        then:
        executionResponse == expectedResponse
    }

    void 'properly responds to finish page request'() {
        given:
        ConfigMap configMap = new ConfigMap()
        ConfigEntries configEntries = new ConfigEntries()
        configEntries.add(new ConfigEntry().valueType(ConfigEntry.ValueTypeEnum.STRING))
        configMap['init'] = configEntries
        ConfigurationData configurationData = new ConfigurationData()
            .installedAppId(UUID.randomUUID().toString())
            .phase(ConfigurationPhase.PAGE)
            .pageId('finish')
            .previousPageId('intro')
            .config(configMap)
        ExecutionRequest executionRequest = new ExecutionRequest()
            .lifecycle(AppLifecycle.CONFIGURATION)
            .executionId(UUID.randomUUID().toString())
            .locale('en')
            .version('0.1.0')
            .configurationData(configurationData)
            .settings([:])
        SectionSetting doneTextSetting = new SectionSetting()
            .name('You are done!')
            .description('Description text')
            .type(SettingType.PARAGRAPH)
        SectionSetting hideTipSetting = new SectionSetting()
            .name("You can also hide/collapse the section by default with isHidden=true")
            .type(SettingType.PARAGRAPH)
        ConfigurationResponseData configurationResponseData = new ConfigurationResponseData()
            .page(new Page()
                .name('This is the last configuration page')
                .complete(true)
                .pageId('finish')
                .previousPageId('intro')
                .sections([
                    new Section()
                        .settings([hideTipSetting])
                        .name('This section can be hidden by tapping here')
                        .hideable(true),
                    new Section().settings([doneTextSetting])
            ])
        )
        ExecutionResponse expectedResponse = new ExecutionResponse()
            .statusCode(200)
            .configurationData(configurationResponseData)

        when:
        ExecutionResponse executionResponse = configurationHandler.handle(executionRequest)

        then:
        executionResponse == expectedResponse
    }

    void 'returns NOT_FOUND when missing configurationData'() {
        when:
        ExecutionResponse executionResponse = configurationHandler.handle(new ExecutionRequest())

        then:
        executionResponse == new ExecutionResponse().statusCode(404)
    }

    void 'returns NOT_FOUND when missing phase'() {
        given:
        ConfigurationData configurationData = new ConfigurationData().phase(null)
        ExecutionRequest executionRequest = new ExecutionRequest().configurationData(configurationData)

        when:
        ExecutionResponse executionResponse = configurationHandler.handle(executionRequest)

        then:
        executionResponse == new ExecutionResponse().statusCode(404)
    }

    void 'returns NOT_FOUND when missing pageId'() {
        given:
        ConfigurationData configurationData = new ConfigurationData()
            .phase(ConfigurationPhase.PAGE)
        ExecutionRequest executionRequest = new ExecutionRequest().configurationData(configurationData)

        when:
        ExecutionResponse executionResponse = configurationHandler.handle(executionRequest)

        then:
        executionResponse == new ExecutionResponse().statusCode(404)
    }

    void 'returns NOT_FOUND when bad pageId'() {
        given:
        ConfigurationData configurationData = new ConfigurationData()
            .phase(ConfigurationPhase.PAGE)
            .pageId('invalid')
        ExecutionRequest executionRequest = new ExecutionRequest().configurationData(configurationData)

        when:
        ExecutionResponse executionResponse = configurationHandler.handle(executionRequest)

        then:
        executionResponse == new ExecutionResponse().statusCode(404)
    }
}
