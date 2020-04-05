package app.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartthings.sdk.smartapp.core.Response;
import com.smartthings.sdk.smartapp.core.extensions.ConfigurationHandler;
import com.smartthings.sdk.smartapp.core.models.*;

import java.util.ArrayList;
import java.util.List;


/**
 * This class extends ConfigurationHandler to handle incoming requests.
 * The Configuration page defines not only the look and feel of your
 * app's presence within the SmartThings app, but it defines what
 * your app will have access to once installed.
 *
 * Below you will find examples of how you can create interesting and
 * engaging pages with instructions, images, collapsible sections, and
 * selectors (modes, devices, etc.)
 */
public class AppConfigurationHandler implements ConfigurationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AppConfigurationHandler.class);

    private static final SectionSetting doneText = new SectionSetting()
        .type(SettingType.PARAGRAPH)
        .name("You are done!")
        .description("Description text");
    private static final SectionSetting hideTipText = new SectionSetting()
        .type(SettingType.PARAGRAPH)
        .name("You can also hide/collapse the section by default with isHidden=true");

    private enum PageType {
        INTRO {
            public ExecutionResponse buildResponse() {
                Page page = new Page().pageId(INTRO.pageId);
                ConfigurationResponseData response = new ConfigurationResponseData();
                List<String> capabilities = new ArrayList<>();
                capabilities.add("switch");
                List<DeviceSetting.PermissionsEnum> devicePermissions = new ArrayList<>();
                devicePermissions.add(DeviceSetting.PermissionsEnum.R);
                devicePermissions.add(DeviceSetting.PermissionsEnum.X);
                page.setNextPageId(FINISH.pageId);
                page.setName("This is the first configuration page");
                DeviceSetting deviceSetting = new DeviceSetting();
                deviceSetting.setId("selectedSwitches");
                deviceSetting.setName("Select a device");
                deviceSetting.setDescription("Tap to select");
                deviceSetting.setType(SettingType.DEVICE);
                deviceSetting.setMultiple(true);
                deviceSetting.setPreselect(true);
                deviceSetting.setCapabilities(capabilities);
                deviceSetting.setPermissions(devicePermissions);
                Section deviceSection = new Section().addSettingsItem(deviceSetting);
                page.addSectionsItem(deviceSection);
                response.setPage(page);
                return Response.ok(response);
            }
        },
        FINISH {
            @Override
            public ExecutionResponse buildResponse() {
                Page page = new Page().pageId(FINISH.pageId);
                ConfigurationResponseData response = new ConfigurationResponseData();
                page.setPreviousPageId(INTRO.pageId);
                page.setName("This is the last configuration page");
                page.setComplete(true);
                Section section = new Section().name("This section can be hidden by tapping here");
                section.setHideable(true);
                section.setHidden(false);
                List<SectionSetting> settings = new ArrayList<>();
                settings.add(hideTipText);
                section.setSettings(settings);
                page.addSectionsItem(section);
                List<SectionSetting> doneTextSettings = new ArrayList<>();
                doneTextSettings.add(doneText);
                page.addSectionsItem(new Section().settings(doneTextSettings));
                response.setPage(page);
                return Response.ok(response);
            }
        };

        private final String pageId;

        public static PageType fromPageId(String pageId) {
            return valueOf(pageId.toUpperCase());
        }

        PageType() {
            pageId = name().toLowerCase();
        }

        public abstract ExecutionResponse buildResponse();
    }

    @Override
    public ExecutionResponse handle(ExecutionRequest executionRequest) {
        LOG.debug("CONFIGURATION: executionRequest = " + executionRequest);

        ConfigurationData configData = executionRequest.getConfigurationData();
        if (configData == null) {
            LOG.error("configurationData required in ExecutionRequest for ConfigurationHandler");
            return Response.notFound();
        }

        ConfigurationPhase phase = configData.getPhase();

        if (phase == ConfigurationPhase.INITIALIZE) {
            // The first phase is INITIALIZE, where we define the
            // basics of your app configuration.
            return buildInitializeResponse();
        }

        if (phase == ConfigurationPhase.PAGE) {
            // The subsequent phase is PAGE, and you will need to handle
            // the requested pageId, returning the appropriate response
            // object.
            // For more information on how pages are structured and what
            // settings are available, please see the documentation:
            // https://smartthings.developer.samsung.com/develop/guides/smartapps/configuration.html
            if (configData.getPageId() == null) {
                LOG.error("missing pageId in configurationData");
                return Response.notFound();
            }
            try {
                PageType pageType = PageType.fromPageId(configData.getPageId());
                return pageType.buildResponse();
            } catch (IllegalArgumentException exception) {
                LOG.error("invalid pageId " + configData.getPageId() + "in configurationData");
                return Response.notFound();
            }
        }

        LOG.error("missing phase in configurationData");
        return Response.notFound();
    }

    private ExecutionResponse buildInitializeResponse() {
        ConfigurationResponseData response = new ConfigurationResponseData();
        // Although not required, we are invoking InitializeSetting and
        // using method-chaining to ultimately return the required type
        // of InitializeSetting. See the handle method and PageType for
        // an example of how you can mix chaining with setting property
        // accessors of a different type.
        List<String> permissions = new ArrayList<>();
        permissions.add("r:devices:*");
        permissions.add("x:devices:*");
        InitializeSetting initialize = new InitializeSetting()
            .firstPageId("intro")
            .disableCustomDisplayName(false)
            .disableRemoveApp(false)
            .permissions(permissions);
        initialize.setId("init");
        initialize.setName("Java Ratpack Guice SmartApp Example");
        initialize.setDescription("Create a SmartApp using Java and Ratpack with Guice");
        response.setInitialize(initialize);
        return Response.ok(response);
    }
}
