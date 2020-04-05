package app.handlers

import com.smartthings.sdk.smartapp.core.Response
import com.smartthings.sdk.smartapp.core.extensions.ConfigurationHandler
import com.smartthings.sdk.smartapp.core.models.*

// Create SectionSetting blocks for user later
// With "apply", we can create and configure the SectionSetting() in one statement
private val hideTipText = SectionSetting().apply {
    type = SettingType.PARAGRAPH
    name = "You can also hide/collapse the section by default with isHidden=true"
}

private val doneText = SectionSetting().apply {
    type = SettingType.PARAGRAPH
    name = "You are done!"
    description = "Description text"
}

private enum class Pages(val pageId: String) {
    INTRO("intro"), FINISH("finish")
}

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
class Configuration : ConfigurationHandler {

    override fun handle(request: ExecutionRequest): ExecutionResponse {
        val response = ConfigurationResponseData()
        when (request.configurationData?.phase) {
            // The first phase is INITIALIZE, where we define the
            // basics of your app configuration.
            ConfigurationPhase.INITIALIZE -> {
                // Although not required, we are invoking InitializeSetting and
                // using method-chaining to ultimately return the required type
                // of InitializeSetting. See below for an example of how you can
                // mix chaining with setting property accessors of a different type.
                response.initialize = InitializeSetting()
                    .firstPageId("intro")
                    .disableCustomDisplayName(false)
                    .disableRemoveApp(false)
                    .permissions(listOf("r:devices:*", "x:devices:*"))
                    .apply {
                        // You can use the "apply" scope function to create and
                        // configure our InitializeSetting in one statement.
                        id = "init"
                        name = "Kotlin SmartApp Example"
                        description = "Create a SmartApp using Kotlin and Ktor"
                    }
                return Response.ok(response)
            }
            // The subsequent phase is PAGE, and you will need to handle
            // the requested pageId, returning the appropriate response
            // object.
            // For more information on how pages are structured and what
            // settings are available, please see the documentation:
            // https://smartthings.developer.samsung.com/develop/guides/smartapps/configuration.html
            ConfigurationPhase.PAGE -> {
                when (request.configurationData.pageId) {
                    Pages.INTRO.pageId -> {
                        response.page = Page()
                            .pageId(Pages.INTRO.pageId)
                            .nextPageId("finish")
                            .name("This is the first configuration page")
                            .addSectionsItem(Section()
                                .addSettingsItem(DeviceSetting().apply {
                                    id = "selectedSwitches"
                                    name = "Select a device"
                                    description = "Tap to select"
                                    type = SettingType.DEVICE
                                    isMultiple = true
                                    isPreselect = true
                                    capabilities = listOf("switch")
                                    permissions = listOf(DeviceSetting.PermissionsEnum.R, DeviceSetting.PermissionsEnum.X)
                                })
                            )
                    }
                    Pages.FINISH.pageId -> {
                        response.page = Page()
                            .pageId(Pages.FINISH.pageId)
                            .previousPageId("intro")
                            .complete(true)
                            .name("This is the last configuration page")
                            .addSectionsItem(Section()
                                .name("This section can be hidden by tapping here")
                                .apply {
                                    isHideable = true
                                    isHidden = false
                                    settings = listOf(hideTipText)
                                }
                            )
                            .addSectionsItem(Section().settings(listOf(doneText)))
                    }
                    else -> {
                        return Response.notFound()
                    }
                }
                return Response.ok(response)
            }
            else -> {
                return Response.notFound()
            }
        }
    }
}
