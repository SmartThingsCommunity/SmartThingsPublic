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
                .name('Java Spring Boot SmartApp Example')
                .description('Create a SmartApp using Java and Spring Boot')
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

        when:
        ExecutionResponse executionResponse = configurationHandler.handle(executionRequest)

        then:
        executionResponse.statusCode == 200

        ConfigurationResponseData config = executionResponse.getConfigurationData();
        config.getInitialize() == null
        Page page = config.getPage()
        page.getName() == "This is the first configuration page"
        page.pageId == 'intro'
        page.nextPageId == 'finish'
        page.previousPageId == null
        page.complete == false
        page.sections.size == 2
        page.sections.name == ['Device Settings Section', 'Settings Test Section']
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

        when:
        ExecutionResponse executionResponse = configurationHandler.handle(executionRequest)

        then:
        executionResponse.statusCode == 200

        ConfigurationResponseData config = executionResponse.getConfigurationData();
        config.getInitialize() == null
        Page page = config.getPage()
        page.getName() == "This is the last configuration page"
        page.pageId == 'finish'
        page.nextPageId == null
        page.previousPageId == 'intro'
        page.complete == true
        page.sections.size == 4
        page.sections.name == ['Another Fun Section',
            null,
            'This section can be hidden by tapping here',
            null]
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
