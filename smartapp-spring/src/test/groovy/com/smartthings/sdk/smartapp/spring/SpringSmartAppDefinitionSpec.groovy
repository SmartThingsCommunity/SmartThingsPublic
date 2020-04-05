package com.smartthings.sdk.smartapp.spring

import java.util.function.Predicate

import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import spock.lang.Specification

import com.smartthings.sdk.smartapp.core.Handler
import com.smartthings.sdk.smartapp.core.PredicateHandler
import com.smartthings.sdk.smartapp.core.RequestPreprocessor
import com.smartthings.sdk.smartapp.core.SmartAppDefinition
import com.smartthings.sdk.smartapp.core.extensions.ConfigurationHandler
import com.smartthings.sdk.smartapp.core.extensions.EventHandler
import com.smartthings.sdk.smartapp.core.extensions.InstallHandler
import com.smartthings.sdk.smartapp.core.extensions.OAuthCallbackHandler
import com.smartthings.sdk.smartapp.core.extensions.PingHandler
import com.smartthings.sdk.smartapp.core.extensions.UninstallHandler
import com.smartthings.sdk.smartapp.core.extensions.UpdateHandler
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest


class SpringSmartAppDefinitionSpec extends Specification {
    ApplicationContext applicationContext = Mock()
    PingHandler pingHandler = Mock()
    ConfigurationHandler configurationHandler = Mock()
    InstallHandler installHandler = Mock()
    UpdateHandler updateHandler = Mock()
    OAuthCallbackHandler oauthCallbackHandler = Mock()
    EventHandler eventHandler = Mock()
    UninstallHandler uninstallHandler = Mock()
    PredicateHandler predicateHandler = PredicateHandler.of(Mock(Predicate.class), Mock(Handler.class))
    Map<String, PredicateHandler> predicateHandlers = ["ph": predicateHandler]
    RequestPreprocessor requestPreprocessor = Mock(RequestPreprocessor.class);
    Map<String, RequestPreprocessor> requestPreprocessors = ["rh": requestPreprocessor];

    void "handles all handlers"() {
        given:
        applicationContext.getBean(PingHandler.class) >> pingHandler
        applicationContext.getBean(ConfigurationHandler.class) >> configurationHandler
        applicationContext.getBean(InstallHandler.class) >> installHandler
        applicationContext.getBean(UpdateHandler.class) >> updateHandler
        applicationContext.getBean(OAuthCallbackHandler.class) >> oauthCallbackHandler
        applicationContext.getBean(EventHandler.class) >> eventHandler
        applicationContext.getBean(UninstallHandler.class) >> uninstallHandler
        applicationContext.getBeansOfType(PredicateHandler.class) >> predicateHandlers
        applicationContext.getBeansOfType(RequestPreprocessor.class) >> requestPreprocessors

        when:
        SmartAppDefinition smartAppDefinition = SpringSmartAppDefinition.of(applicationContext)

        then:
        smartAppDefinition.pingHandler.is(pingHandler)
        smartAppDefinition.configurationHandler.is(configurationHandler)
        smartAppDefinition.installHandler.is(installHandler)
        smartAppDefinition.updateHandler.is(updateHandler)
        smartAppDefinition.oauthCallbackHandler.is(oauthCallbackHandler)
        smartAppDefinition.eventHandler.is(eventHandler)
        smartAppDefinition.uninstallHandler.is(uninstallHandler)
        smartAppDefinition.predicateHandlers == [predicateHandler]
        smartAppDefinition.requestPreprocessors == [requestPreprocessor]
    }

    void "not require ping handler"() {
        given:
        applicationContext.getBean((Class<? extends PingHandler>) PingHandler.class) >> { throw new NoSuchBeanDefinitionException("no bean") }
        applicationContext.getBean(ConfigurationHandler.class) >> configurationHandler
        applicationContext.getBean(InstallHandler.class) >> installHandler
        applicationContext.getBean(UpdateHandler.class) >> updateHandler
        applicationContext.getBean(OAuthCallbackHandler.class) >> oauthCallbackHandler
        applicationContext.getBean(EventHandler.class) >> eventHandler
        applicationContext.getBean(UninstallHandler.class) >> uninstallHandler
        applicationContext.getBeansOfType(PredicateHandler.class) >> predicateHandlers
        applicationContext.getBeansOfType(RequestPreprocessor.class) >> requestPreprocessors

        when:
        SmartAppDefinition smartAppDefinition = SpringSmartAppDefinition.of(applicationContext)

        then:
        smartAppDefinition.pingHandler == null
        smartAppDefinition.configurationHandler.is(configurationHandler)
        smartAppDefinition.installHandler.is(installHandler)
        smartAppDefinition.updateHandler.is(updateHandler)
        smartAppDefinition.oauthCallbackHandler.is(oauthCallbackHandler)
        smartAppDefinition.eventHandler.is(eventHandler)
        smartAppDefinition.uninstallHandler.is(uninstallHandler)
        smartAppDefinition.predicateHandlers == [predicateHandler]
        smartAppDefinition.requestPreprocessors == [requestPreprocessor]
    }

    void "require configuration handler"() {
        given:
        applicationContext.getBean(PingHandler.class) >> pingHandler
        applicationContext.getBean(ConfigurationHandler.class) >> { throw new NoSuchBeanDefinitionException("no such bean") }

        when:
        SpringSmartAppDefinition.of(applicationContext)

        then:
        thrown NoSuchBeanDefinitionException
    }

    void "require install handler"() {
        given:
        applicationContext.getBean(PingHandler.class) >> pingHandler
        applicationContext.getBean(ConfigurationHandler.class) >> configurationHandler
        applicationContext.getBean(InstallHandler.class) >> { throw new NoSuchBeanDefinitionException("no such bean") }

        when:
        SpringSmartAppDefinition.of(applicationContext)

        then:
        thrown NoSuchBeanDefinitionException
    }

    void "require update handler"() {
        given:
        applicationContext.getBean(PingHandler.class) >> pingHandler
        applicationContext.getBean(ConfigurationHandler.class) >> configurationHandler
        applicationContext.getBean(InstallHandler.class) >> installHandler
        applicationContext.getBean(UpdateHandler.class) >> { throw new NoSuchBeanDefinitionException("no such bean") }

        when:
        SpringSmartAppDefinition.of(applicationContext)

        then:
        thrown NoSuchBeanDefinitionException
    }

    void "not require oauth handler"() {
        given:
        applicationContext.getBean((Class<? extends PingHandler>) PingHandler.class) >> pingHandler
        applicationContext.getBean(ConfigurationHandler.class) >> configurationHandler
        applicationContext.getBean(InstallHandler.class) >> installHandler
        applicationContext.getBean(UpdateHandler.class) >> updateHandler
        applicationContext.getBean(OAuthCallbackHandler.class) >> { throw new NoSuchBeanDefinitionException("no bean") }
        applicationContext.getBean(EventHandler.class) >> eventHandler
        applicationContext.getBean(UninstallHandler.class) >> uninstallHandler
        applicationContext.getBeansOfType(PredicateHandler.class) >> predicateHandlers
        applicationContext.getBeansOfType(RequestPreprocessor.class) >> requestPreprocessors

        when:
        SmartAppDefinition smartAppDefinition = SpringSmartAppDefinition.of(applicationContext)

        then:
        smartAppDefinition.pingHandler.is(pingHandler)
        smartAppDefinition.configurationHandler.is(configurationHandler)
        smartAppDefinition.installHandler.is(installHandler)
        smartAppDefinition.updateHandler.is(updateHandler)
        smartAppDefinition.oauthCallbackHandler == null
        smartAppDefinition.eventHandler.is(eventHandler)
        smartAppDefinition.uninstallHandler.is(uninstallHandler)
        smartAppDefinition.requestPreprocessors == [requestPreprocessor]
    }
}
