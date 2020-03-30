package app.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartthings.sdk.smartapp.core.Response;
import com.smartthings.sdk.smartapp.core.extensions.ConfigurationHandler;
import com.smartthings.sdk.smartapp.core.models.*;


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
@Component
public class AppConfigurationHandler implements ConfigurationHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private enum PageType {
        INTRO {
            public ExecutionResponse buildResponse() {
                SectionSetting deviceSetting = new DeviceSetting()
                    .multiple(true)
                    .preselect(true)
                    .addPermissionsItem(DeviceSetting.PermissionsEnum.R)
                    .addPermissionsItem(DeviceSetting.PermissionsEnum.X)
                    .addCapabilitiesItem("switch")
                    .id("selectedSwitches")
                    .name("Device(s) to turn on at 7 a.m.")
                    .description("Tap to select")
                    .type(SettingType.DEVICE);
                Section deviceSection = new Section().name("Device Settings Section")
                    .addSettingsItem(deviceSetting);

                SectionSetting textSetting = new TextSetting()
                    .minLength(3)
                    .postMessage("Post Message")
                    .id("favoriteQuote")
                    .name("Favorite Quote")
                    .description("Enter something, anything you want")
                    .type(SettingType.TEXT);
                SectionSetting passwordSetting = new PasswordSetting()
                    .id("passwordTest")
                    .name("Do NOT enter real password")
                    .description("Enter something random or fun")
                    .type(SettingType.PASSWORD);
                SectionSetting booleanSetting = new BooleanSetting()
                    .id("booleanTest")
                    .name("Yes or no, 1 or 0, true or false, set or not set")
                    .description("To choose or not to choose, that is the question")
                    .type(SettingType.BOOLEAN);
                Section settingsTestSection = new Section()
                    .name("Settings Test Section")
                    .addSettingsItem(textSetting)
                    .addSettingsItem(passwordSetting)
                    .addSettingsItem(booleanSetting);

                Page page = new Page()
                    .pageId(INTRO.pageId)
                    .nextPageId(FINISH.pageId)
                    .name("This is the first configuration page")
                    .addSectionsItem(deviceSection)
                    .addSectionsItem(settingsTestSection);
                return Response.ok(new ConfigurationResponseData().page(page));
            }
        },
        FINISH {
            @Override
            public ExecutionResponse buildResponse() {
                SectionSetting enumSetting = new EnumSetting()
                    .addOptionsItem(new Option().id("mk3s").name("Original Prusa i3 MK3S"))
                    .addOptionsItem(new Option().id("taz6").name("LulzBot TAZ 6"))
                    .addOptionsItem(new Option().id("m3").name("MakerGear M3"))
                    .id("enumTest")
                    .name("Favorite Printer")
                    .description("Pick your favorite")
                    .type(SettingType.ENUM);
                SectionSetting complexEnumSetting = new EnumSetting()
                    .addGroupedOptionsItem(new GroupedOption().name("Warm")
                        .addOptionsItem(new Option().id("red").name("Red"))
                        .addOptionsItem(new Option().id("orange").name("Orange"))
                        .addOptionsItem(new Option().id("yellow").name("Yellow")))
                    .addGroupedOptionsItem(new GroupedOption().name("Cool")
                        .addOptionsItem(new Option().id("green").name("Green"))
                        .addOptionsItem(new Option().id("blue").name("Blue"))
                        .addOptionsItem(new Option().id("purple").name("Purple")))
                    .addGroupedOptionsItem(new GroupedOption().name("Neutral")
                        .addOptionsItem(new Option().id("black").name("Black"))
                        .addOptionsItem(new Option().id("white").name("White"))
                        .addOptionsItem(new Option().id("gray").name("Gray")))
                    .id("complexEnumTest")
                    .name("Favorite Color")
                    .description("Pick your favorite")
                    .type(SettingType.ENUM);
                SectionSetting modeSetting = new ModeSetting()
                    .style(StyleType.DEFAULT)
                    .id("modeTest")
                    .name("Select Mode")
                    .description("This is a mode setting")
                    .type(SettingType.MODE);
                Section justForFunSection = new Section()
                    .name("Another Fun Section")
                    .addSettingsItem(enumSetting)
                    .addSettingsItem(complexEnumSetting)
                    .addSettingsItem(modeSetting);

                SectionSetting numberSetting = new NumberSetting()
                    .min(3).max(13)
                    .postMessage("2 < n < 14")
                    .id("numberTest")
                    .name("A Number")
                    .description("between 3 and 13, inclusive")
                    .type(SettingType.NUMBER);
                Section unnamedSection = new Section()
                    .addSettingsItem(numberSetting);

                SectionSetting hideTipText = new SectionSetting()
                    .type(SettingType.PARAGRAPH)
                    .name("You can also hide/collapse the section by default with isHidden=true");
                Section collapsibleSection = new Section()
                    .name("This section can be hidden by tapping here")
                    .hideable(true)
                    .hidden(false)
                    .addSettingsItem(hideTipText);
                SectionSetting doneText = new SectionSetting()
                    .type(SettingType.PARAGRAPH)
                    .name("You are done!")
                    .description("Description text");

                Section doneTextSection = new Section()
                    .addSettingsItem(doneText);

                Page page = new Page()
                    .pageId(FINISH.pageId)
                    .previousPageId(INTRO.pageId)
                    .name("This is the last configuration page")
                    .complete(true)
                    .addSectionsItem(justForFunSection)
                    .addSectionsItem(unnamedSection)
                    .addSectionsItem(collapsibleSection)
                    .addSectionsItem(doneTextSection);
                return Response.ok(new ConfigurationResponseData().page(page));
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
        log.debug("CONFIGURATION: executionRequest = " + executionRequest);

        ConfigurationData configData = executionRequest.getConfigurationData();
        if (configData == null) {
            log.error("configurationData required in ExecutionRequest for ConfigurationHandler");
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
                log.error("missing pageId in configurationData");
                return Response.notFound();
            }
            try {
                PageType pageType = PageType.fromPageId(configData.getPageId());
                return pageType.buildResponse();
            } catch (IllegalArgumentException exception) {
                log.error("invalid pageId " + configData.getPageId() + "in configurationData");
                return Response.notFound();
            }
        }

        log.error("missing phase in configurationData");
        return Response.notFound();
    }

    private ExecutionResponse buildInitializeResponse() {
        // Although not required, we are invoking InitializeSetting and
        // using method-chaining to ultimately return the required type
        // of InitializeSetting. See the handle method and PageType for
        // an example of how you can mix chaining with setting property
        // accessors of a different type.
        InitializeSetting initialize = new InitializeSetting()
            .id("init")
            .name("Java Spring Boot SmartApp Example")
            .description("Create a SmartApp using Java and Spring Boot")
            .firstPageId("intro")
            .disableCustomDisplayName(false)
            .disableRemoveApp(false)
            .addPermissionsItem("r:devices:*")
            .addPermissionsItem("x:devices:*");
        return Response.ok(new ConfigurationResponseData().initialize(initialize));
    }
}
