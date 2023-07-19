/**
 *  Echo Speaks SmartApp
 *
 *  Copyright 2018, 2019 Anthony Santilli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

import groovy.json.*
import java.text.SimpleDateFormat
String appVersion()	 { return "2.5.0" }
String appModified() { return "2019-05-31" }
String appAuthor()   { return "Anthony S." }
Boolean isBeta()     { return false }
Boolean isST()       { return (getPlatform() == "SmartThings") }
Map minVersions()    { return [echoDevice: 250, server: 211] } //These values define the minimum versions of code this app will work with.

definition(
    name       : "Echo Speaks",
    namespace  : "tonesto7",
    author     : "Anthony Santilli",
    description: "Integrate your Amazon Echo devices into your Smart Home environment to create virtual Echo Devices. This allows you to speak text, make announcements, control media playback including volume, and many other Alexa features.",
    category   : "My Apps",
    iconUrl    : "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_speaks.1x${state?.updateAvailable ? "_update" : ""}.png",
    iconX2Url  : "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_speaks.2x${state?.updateAvailable ? "_update" : ""}.png",
    iconX3Url  : "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_speaks.3x${state?.updateAvailable ? "_update" : ""}.png",
    pausable   : true,
    oauth      : true
)

preferences {
    page(name: "startPage")
    page(name: "mainPage")
    page(name: "settingsPage")
    page(name: "devicePrefsPage")
    page(name: "newSetupPage")
    page(name: "groupsPage")
    page(name: "actionsPage")
    page(name: "devicePage")
    page(name: "deviceListPage")
    page(name: "unrecogDevicesPage")
    page(name: "changeLogPage")
    page(name: "notifPrefPage")
    page(name: "servPrefPage")
    page(name: "musicSearchTestPage")
    page(name: "searchTuneInResultsPage")
    page(name: "deviceTestPage")
    page(name: "broadcastPage")
    page(name: "announcePage")
    page(name: "sequencePage")
    page(name: "setNotificationTimePage")
    page(name: "uninstallPage")
}

def startPage() {
    state?.isParent = true
    checkVersionData(true)
    state?.childInstallOkFlag = false
    if(state?.resumeConfig || (state?.isInstalled && !state?.serviceConfigured)) {
        log.debug "resumeConfig: true"
        return servPrefPage()
    } else if(showChgLogOk()) {
        return changeLogPage()
    } else { return mainPage() }
}

def appInfoSect()	{
    Map codeVer = state?.codeVersions ?: null
    def str = ""//"Author: ${appAuthor()}"
    // if(isST() && state?.customerName) { str += "User: ${state?.customerName}" }
    if(codeVer && (codeVer?.server || codeVer?.echoDevice)) {
        str += bulletItem(str, "App: (v${appVersion()})")
        str += (codeVer && codeVer?.echoDevice) ? bulletItem(str, "Device: (v${codeVer?.echoDevice})") : ""
        str += (codeVer && codeVer?.server) ? bulletItem(str, "Server: (v${codeVer?.server})") : ""
    } else { str += "\nApp: v${appVersion()}" }
    section() {
        href "changeLogPage", title: pTS("${app?.name}", getAppImg("echo_speaks.2x", true)), description: str, image: getAppImg("echo_speaks.2x")
        if(!state?.isInstalled) { paragraph "--NEW Install--", state: "complete" }
    }
}

def mainPage() {
    def tokenOk = getAccessToken()
    Boolean newInstall = (state?.isInstalled != true)
    Boolean resumeConf = (state?.resumeConfig == true)
    if(state?.refreshDeviceData == true) { getEchoDevices() }
    return dynamicPage(name: "mainPage", uninstall: false, install: true) {
        appInfoSect()
        if(!tokenOk) {
            section() { paragraph title: "Uh OH!!!", "Oauth Has NOT BEEN ENABLED. Please Remove this app and try again after it after enabling OAUTH"; }; return;
        }
        if(newInstall) {
            deviceDetectOpts()
        } else {
            if(!resumeConfig && state?.authValid != true) {
                section() { paragraph title: "NOTICE:", "You are not currently logged in to Amazon.  Please complete the Authentication Process on the Server Login Page...", required: true, state: null }
            }
            section(sTS("Alexa Devices:")) {
                if(!newInstall) {
                    List devs = getDeviceList()?.collect { "${it?.value?.name}${it?.value?.online ? " (Online)" : ""}${it?.value?.supported == false ? " \u2639" : ""}" }?.sort()
                    Map skDevs = state?.skippedDevices?.findAll { (it?.value?.reason != "In Ignore Device Input") }
                    Map ignDevs = state?.skippedDevices?.findAll { (it?.value?.reason == "In Ignore Device Input") }
                    if(devs?.size()) {
                        href "deviceListPage", title: inTS("Installed Devices:"), description: "${devs?.join("\n")}\n\nTap to view details...", state: "complete"
                    } else { paragraph title: "Discovered Devices:", "No Devices Available", state: "complete" }
                    if(skDevs?.size()) {
                        String uDesc = "Unsupported: (${skDevs?.size()})"
                        uDesc += ignDevs?.size() ? "\nUser Ignored: (${ignDevs?.size()})" : ""
                        uDesc += settings?.bypassDeviceBlocks ? "\nBlock Bypass: (Active)" : ""
                        href "unrecogDevicesPage", title: inTS("Unused Devices:"), description: "${uDesc}\n\nTap to view details..."
                    }
                }
                def devPrefDesc = devicePrefsDesc()
                href "devicePrefsPage", title: inTS("Device Detection\nPreferences", getAppImg("devices", true)), description: "${devPrefDesc ? "${devPrefDesc}\n\n" : ""}Tap to configure...", state: "complete", image: getAppImg("devices")
            }

            // def t1 = getGroupsDesc()
            // def grpDesc = t1 ? "${t1}\n\nTap to modify" : null
            // section(sTS("Manage Groups:")) {
            //     href "groupsPage", title: inTS("Broadcast Groups", getAppImg("es_groups", true)), description: (grpDesc ?: "Tap to configure"), state: (grpDesc ? "complete" : null), image: getAppImg("es_groups")
            // }

            // def t2 = getActionsDesc()
            // def actDesc = t2 ? "${t2}\n\nTap to modify" : null
            // section(sTS("Manage Actions:")) {
            //     href "actionsPage", title: inTS("Actions", getAppImg("es_actions", true)), description: (actDesc ?: "Tap to configure"), state: (actDesc ? "complete" : null), image: getAppImg("es_actions")
            // }
            state?.childInstallOkFlag = true

            section(sTS("Experimental Functions:")) {
                href "deviceTestPage", title: inTS("Device Test Page", getAppImg("broadcast", true)), description: "Test Announcements, Broadcasts, and Sequences\n\nTap to proceed...", image: getAppImg("testing")
                href "musicSearchTestPage", title: inTS("Music Search Tests", getAppImg("music", true)), description: "Test music queries\n\nTap to proceed...", image: getAppImg("music")
            }
            section(sTS("Alexa Login Service:")) {
                def t0 = getServiceConfDesc()
                href "servPrefPage", title: inTS("Login Service\nSettings", getAppImg("settings", true)), description: (t0 ? "${t0}\n\nTap to modify" : "Tap to configure"), state: (t0 ? "complete" : null), image: getAppImg("settings")
            }
            if(!state?.shownDevSharePage) { showDevSharePrefs() }
            section(sTS("Notifications:")) {
                def t0 = getAppNotifConfDesc()
                href "notifPrefPage", title: inTS("App and Device\nNotifications", getAppImg("devices", true)), description: (t0 ? "${t0}\n\nTap to modify" : "Tap to configure"), state: (t0 ? "complete" : null), image: getAppImg("notification2")
            }
        }
        section(sTS("Documentation & Settings:")) {
            href url: documentationLink(), style: "external", required: false, title: inTS("View Documentation", getAppImg("documentation", true)), description: "Tap to proceed", state: "complete", image: getAppImg("documentation")
            href "settingsPage", title: inTS("Manage Logging, and Metrics", getAppImg("settings", true)), description: "Tap to modify...", image: getAppImg("settings")
        }
        if(!newInstall) {
            section(sTS("Donations:")) {
                href url: textDonateLink(), style: "external", required: false, title: inTS("Donations", getAppImg("donate", true)), description: "Tap to open browser", image: getAppImg("donate")
            }
            section(sTS("Remove Everything:")) {
                href "uninstallPage", title: inTS("Uninstall this App", getAppImg("uninstall", true)), description: "Tap to Remove...", image: getAppImg("uninstall")
            }
        } else {
            showDevSharePrefs()
            section(sTS("Important Step:")) {
                paragraph title: "Notice:", "Please complete the install and return to the Echo Speaks App to resume deployment and configuration of the server.", required: true, state: null
                state?.resumeConfig = true
            }
        }
    }
}

def groupsPage() {
    return dynamicPage(name: "groupsPage", uninstall: false, install: false) {
        def groupApp = findChildAppByName( grpChildName() )
        if(groupApp) { /*Nothing to add here yet*/ }
        else {
            section("") {
                paragraph "You haven't created any Broadcast Groups yet!\nTap Create New Group to get Started"
            }
        }
        section("") {
            app(name: "groupApp", appName: grpChildName(), namespace: "tonesto7", multiple: true, title: inTS("Create New Group", getAppImg("es_groups", true)), image: getAppImg("es_groups"))
        }
    }
}

def actionsPage() {
    return dynamicPage(name: "actionsPage", uninstall: false, install: false) {
        def actionApp = findChildAppByName( actChildName() )
        if(actionApp) { /*Nothing to add here yet*/ }
        else {
            section("") {
                paragraph "You haven't created any Actions yet!\nTap Create New Action to get Started"
            }
        }
        section("") {
            app(name: "actionApp", appName: actChildName(), namespace: "tonesto7", multiple: true, title: inTS("Create New Action", getAppImg("es_actions", true)), image: getAppImg("es_actions"))
        }
    }
}

def devicePrefsPage() {
    Boolean newInstall = (state?.isInstalled != true)
    Boolean resumeConf = (state?.resumeConfig == true)
    return dynamicPage(name: "devicePrefsPage", uninstall: false, install: false) {
        deviceDetectOpts()
        section(sTS("Detection Override:")) {
            paragraph "Device not detected?  Enabling this will allow you to override the developer block for unrecognized or uncontrollable devices.  This is useful for testing the device."
            input "bypassDeviceBlocks", "bool", title: inTS("Override Blocks and Create Ignored Devices?"), description: "WARNING: This will create devices for all remaining ignored devices", required: false, defaultValue: false, submitOnChange: true
        }
        devCleanupSect()
        if(!newInstall && !resumeConf) { state?.refreshDeviceData = true }
    }
}

private deviceDetectOpts() {
    Boolean newInstall = (state?.isInstalled != true)
    Boolean resumeConf = (state?.resumeConfig == true)
    section(sTS("Device Detection Preferences")) {
        input "autoCreateDevices", "bool", title: inTS("Auto Create New Devices?", getAppImg("devices", true)), description: "", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("devices")
        input "createTablets", "bool", title: inTS("Create Devices for Tablets?", getAppImg("amazon_tablet", true)), description: "", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("amazon_tablet")
        input "createWHA", "bool", title: inTS("Create Multiroom Devices?", getAppImg("echo_wha", true)), description: "", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("echo_wha")
        input "createOtherDevices", "bool", title: inTS("Create Other Alexa Enabled Devices?", getAppImg("devices", true)), description: "FireTV (Cube, Stick), Sonos, etc.", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("devices")
        input "autoRenameDevices", "bool", title: inTS("Rename Devices to Match Amazon Echo Name?", getAppImg("name_tag", true)), description: "", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("name_tag")
        Map devs = getAllDevices(true)
        if(devs?.size()) {
            input "echoDeviceFilter", "enum", title: inTS("Don't Use these Devices", getAppImg("exclude", true)), description: "Tap to select", options: (devs ? devs?.sort{it?.value} : []), multiple: true, required: false, submitOnChange: true, image: getAppImg("exclude")
            paragraph title:"Notice:", "To prevent unwanted devices from reinstalling after removal make sure to add it to the Don't use input before removing."
        }
    }
}

private devCleanupSect() {
    if(state?.isInstalled && !state?.resumeConfig) {
        section(sTS("Device Cleanup Options:")) {
            List remDevs = getRemovableDevs()
            if(remDevs?.size()) { paragraph "Removable Devices:\n${remDevs?.sort()?.join("\n")}", required: true, state: null }
            paragraph title:"Notice:", "Remember to add device to filter above to prevent recreation.  Also the cleanup process will fail if the devices are used in external apps/automations"
            input "cleanUpDevices", "bool", title: inTS("Cleanup Unused Devices?"), description: "", required: false, defaultValue: false, submitOnChange: true
            if(cleanUpDevices) { removeDevices() }
        }
    }
}

private List getRemovableDevs() {
    def childDevs = isST() ? app?.getChildDevices(true) : app?.getChildDevices()
    Map eDevs = state?.echoDeviceMap ?: [:]
    List remDevs = []
    childDevs?.each { cDev->
        def dni = cDev?.deviceNetworkId?.tokenize("|")
        if(!eDevs?.containsKey(dni[2])) { remDevs?.push(cDev?.getLabel() as String) }
    }
    return remDevs ?: []
}

private String devicePrefsDesc() {
    String str = ""
    str += "Auto Create (${(settings?.autoCreateDevices == false) ? "Disabled" : "Enabled"})"
    if(settings?.autoCreateDevices) {
        str += (settings?.createTablets == true) ? bulletItem(str, "Tablets") : ""
        str += (settings?.createWHA == true) ? bulletItem(str, "WHA") : ""
        str += (settings?.createOtherDevices == true) ? bulletItem(str, "Other Devices") : ""
    }
    str += settings?.autoRenameDevices != false ? bulletItem(str, "Auto Rename") : ""
    str += settings?.bypassDeviceBlocks == true ? "\nBlock Bypass: (Active)" : ""
    def remDevsSz = getRemovableDevs()?.size() ?: 0
    str += remDevsSz > 0 ? "\n\nRemovable Devices: (${remDevsSz})" : ""
    return str != "" ? str : null
}

def settingsPage() {
    return dynamicPage(name: "settingsPage", uninstall: false, install: false) {
        section(sTS("Logging:")) {
            input "appDebug", "bool", title: inTS("Show Debug Logs in the IDE?", getAppImg("debug", true)), description: "Only leave on when required", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("debug")
            if(settings?.appDebug) {
                input "appTrace", "bool", title: inTS("Show Detailed Trace Logs in the IDE?", getAppImg("debug", true)), description: "Only Enabled when asked by the developer", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("debug")
            }
        }
        showDevSharePrefs()
        section(sTS("App Change Details:")) {
            href "changeLogPage", title: inTS("View App Revision History", getAppImg("change_log", true)), description: "Tap to view", image: getAppImg("change_log")
        }
    }
}

def deviceListPage() {
    return dynamicPage(name: "deviceListPage", install: false) {
        Boolean onST = isST()
        section(sTS("Discovered Devices:")) {
            state?.echoDeviceMap?.sort { it?.value?.name }?.each { k,v->
                String str = "Status: (${v?.online ? "Online" : "Offline"})\nStyle: ${v?.style?.name}\nFamily: ${v?.family}\nType: ${v?.type}\nVolume Control: (${v?.volumeSupport?.toString()?.capitalize()})"
                str += "\nText-to-Speech: (${v?.ttsSupport?.toString()?.capitalize()})\nMusic Player: (${v?.mediaPlayer?.toString()?.capitalize()})"
                str += v?.supported != true ? "\nUnsupported Device: (True)" : ""
                str += (v?.mediaPlayer == true && v?.musicProviders) ? "\nMusic Providers: [${v?.musicProviders}]" : ""
                if(onST) {
                    paragraph title: pTS(v?.name, getAppImg(v?.style?.image, true)), str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.style?.image)
                } else { href "deviceListPage", title: pTS(v?.name, getAppImg(v?.style?.image, true)), description: str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.style?.image) }
            }
        }
    }
}

def unrecogDevicesPage() {
    return dynamicPage(name: "unrecogDevicesPage", install: false) {
        Boolean onST = isST()
        Map skDevMap = state?.skippedDevices ?: [:]
        Map ignDevs = skDevMap?.findAll { (it?.value?.reason == "In Ignore Device Input") }
        Map unDevs = skDevMap?.findAll { (it?.value?.reason != "In Ignore Device Input") }
        section(sTS("Unrecognized/Unsupported Devices:")) {
            if(unDevs?.size()) {
                unDevs?.sort { it?.value?.name }?.each { k,v->
                    String str = "Status: (${v?.online ? "Online" : "Offline"})\nStyle: ${v?.desc}\nFamily: ${v?.family}\nType: ${v?.type}\nVolume Control: (${v?.volume?.toString()?.capitalize()})"
                    str += "\nText-to-Speech: (${v?.tts?.toString()?.capitalize()})\nMusic Player: (${v?.mediaPlayer?.toString()?.capitalize()})\nReason Ignored: (${v?.reason})"
                    if(onST) {
                        paragraph title: pTS(v?.name, getAppImg(v?.image, true)), str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.image)
                    } else { href "unrecogDevicesPage", title: pTS(v?.name, getAppImg(v?.image, true)), description: str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.image) }
                }
                input "bypassDeviceBlocks", "bool", title: inTS("Override Blocks and Create Ignored Devices?"), description: "WARNING: This will create devices for all remaining ignored devices", required: false, defaultValue: false, submitOnChange: true
            } else {
                paragraph "No Uncognized Devices"
            }
        }
        if(ignDevs?.size()) {
            section(sTS("User Ignored Devices:")) {
                ignDevs?.sort { it?.value?.name }?.each { k,v->
                    String str = "Status: (${v?.online ? "Online" : "Offline"})\nStyle: ${v?.desc}\nFamily: ${v?.family}\nType: ${v?.type}\nVolume Control: (${v?.volume?.toString()?.capitalize()})"
                    str += "\nText-to-Speech: (${v?.tts?.toString()?.capitalize()})\nMusic Player: (${v?.mediaPlayer?.toString()?.capitalize()})\nReason Ignored: (${v?.reason})"
                    if(onST) {
                        paragraph title: pTS(v?.name, getAppImg(v?.image, true)), str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.image)
                    } else { href "unrecogDevicesPage", title: pTS(v?.name, getAppImg(v?.image, true)), description: str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.image) }
                }
            }
        }
    }
}

def showDevSharePrefs() {
    section(sTS("Share Data with Developer:")) {
        paragraph title: "What is this used for?", "These options send non-user identifiable information and error data to diagnose catch trending issues."
        input ("optOutMetrics", "bool", title: inTS("Do Not Share Data?", getAppImg("analytics", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("analytics"))
        if(settings?.optOutMetrics != true) {
            href url: getAppEndpointUrl("renderMetricData"), style: (isST() ? "embedded" : "external"), title: inTS("View the Data shared with Developer", getAppImg("view", true)), description: "Tap to view Data", required: false, image: getAppImg("view")
        }
    }
    if(optOutMetrics != true && state?.isInstalled && state?.serviceConfigured && !state?.resumeConfig) {
        section() { input "sendMetricsNow", "bool", title: inTS("Send Metrics Now?", getAppImg("reset", true)), description: "", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("reset") }
        if(sendMetricsNow) { sendInstallData() }
    }
    state?.shownDevSharePage = true
}

Map getDeviceList(isInputEnum=false, onlyTTS=false) {
    Map devMap = [:]
    Map availDevs = state?.echoDeviceMap ?: [:]
    availDevs?.each { key, val->
        if(onlyTTS && val?.ttsSupport != true) { return }
        devMap[key] = val
    }
    return isInputEnum ? (devMap?.size() ? devMap?.collectEntries { [(it?.key):it?.value?.name] } : devMap) : devMap
}

Map getAllDevices(isInputEnum=false) {
    Map devMap = [:]
    Map availDevs = state?.allEchoDevices ?: [:]
    availDevs?.each { key, val-> devMap[key] = val }
    return isInputEnum ? (devMap?.size() ? devMap?.collectEntries { [(it?.key):it?.value?.name] } : devMap) : devMap
}

def servPrefPage() {
    Boolean newInstall = (state?.isInstalled != true)
    Boolean resumeConf = (state?.resumeConfig == true)
    return dynamicPage(name: "servPrefPage", install: (newInstall || resumeConf), nextPage: (!(newInstall || resumeConf) ? "mainPage" : ""), uninstall: (state?.serviceConfigured != true)) {
        Boolean hasChild = ((isST() ? app?.getChildDevices(true) : getChildDevices())?.size())
        Boolean onHeroku = (isST() || settings?.useHeroku != false)

        if(state?.generatedHerokuName) { section() { paragraph title: "Heroku Name:", "${!isST() ? "Heroku Name:\n" : ""}${state?.generatedHerokuName}", state: "complete" }; }
        if(!isST() && settings?.useHeroku == null) settingUpdate("useHeroku", "true", "bool")
        if(settings?.amazonDomain == null) settingUpdate("amazonDomain", "amazon.com", "enum")
        if(settings?.regionLocale == null) settingUpdate("regionLocale", "en-US", "enum")

        if(!state?.serviceConfigured) {
            if(!isST()) {
                section(sTS("Server Deployment Option:")) {
                    input "useHeroku", "bool", title: inTS("Deploy server to Heroku?", getAppImg("heroku", true)), description: "Turn Off to allow local server deployment", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("heroku")
                    if(settings?.useHeroku == false) { paragraph """<p style="color: red;">Local Server deployments are only allowed on Hubitat and are something that can be very difficult for me to support.  I highly recommend Heroku deployments for most users.</p>""" }
                }
            }
            section("") { paragraph "Proceed with the server setup by tapping on Begin Server Setup", state: "complete" }
            srvcPrefOpts(true)
            section(sTS("Deploy the Server:")) {
                href (url: getAppEndpointUrl("config"), style: "external", title: inTS("Begin Server Setup", getAppImg("upload", true)), description: "Tap to proceed", required: false, state: "complete", image: getAppImg("upload"))
            }
        } else {
            if(state?.onHeroku) {
                section(sTS("Server Management:")) {
                    href url: "https://${getRandAppName()}.herokuapp.com/config", style: "external", required: false, title: inTS("Amazon Login Page", getAppImg("amazon_orange", true)), description: "Tap to proceed", image: getAppImg("amazon_orange")
                    href url: "https://dashboard.heroku.com/apps/${getRandAppName()}/settings", style: "external", required: false, title: inTS("Heroku App Settings", getAppImg("heroku", true)), description: "Tap to proceed", image: getAppImg("heroku")
                    href url: "https://dashboard.heroku.com/apps/${getRandAppName()}/logs", style: "external", required: false, title: inTS("Heroku App Logs", getAppImg("heroku", true)), description: "Tap to proceed", image: getAppImg("heroku")
                }
            }
            if(state?.isLocal) {
                section(sTS("Local Server Management:")) {
                    href url: "${getServerHostURL()}/config", style: "external", required: false, title: inTS("Amazon Login Page", getAppImg("amazon_orange", true)), description: "Tap to proceed", image: getAppImg("amazon_orange")
                }
            }
            if(state?.authValid) {
                section(sTS("Cookie Info:")) {
                    if(state?.lastCookieRefresh) { paragraph "Cookie Date: (${state?.lastCookieRefresh})", state: "complete" }
                    input "refreshCookie", "bool", title: inTS("Refresh Alexa Cookie?", getAppImg("reset", true)), description: "This will Refresh your Amazon Cookie.", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("reset")
                    if(refreshCookie) { runCookieRefresh() }
                }
            }
            srvcPrefOpts()
        }
        section(sTS("Reset Options (Tap to view):"), hideable:true, hidden: true) {
            input "resetService", "bool", title: inTS("Reset Service Data?", getAppImg("reset", true)), description: "This will clear all references to the current server and allow you to redeploy a new instance.\nLeave the page and come back after toggling.",
                required: false, defaultValue: false, submitOnChange: true, image: getAppImg("reset")
            input "resetCookies", "bool", title: inTS("Clear Stored Cookie Data?", getAppImg("reset", true)), description: "This will clear all stored cookie data.", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("reset")
            if(settings?.resetService) { clearCloudConfig() }
            if(settings?.resetCookies) { clearCookieData() }
        }
        state?.resumeConfig = false
    }
}

def srvcPrefOpts(pre=false) {
    section(sTS("${pre ? "Required " : ""}Amazon Region Settings${state?.serviceConfigured ? " (Tap to view)" : ""}"), hideable: state?.serviceConfigured, hidden: state?.serviceConfigured) {
        input "amazonDomain", "enum", title: inTS("Select your Amazon Domain?", getAppImg("amazon_orange", true)), description: "", required: true, defaultValue: "amazon.com", options: amazonDomainOpts(), submitOnChange: true, image: getAppImg("amazon_orange")
        input "regionLocale", "enum", title: inTS("Select your Locale?", getAppImg("web", true)), description: "", required: true, defaultValue: "en-US", options: localeOpts(), submitOnChange: true, image: getAppImg("web")
    }
}

def notifPrefPage() {
    dynamicPage(name: "notifPrefPage", install: false) {
        Integer pollWait = 900
        Integer pollMsgWait = 3600
        Integer updNotifyWait = 7200
        section("") {
            paragraph title: "Notice:", "The settings configure here are used by both the App and the Devices.", state: "complete"
        }
        section(sTS("Push Messages:")) {
            input "usePush", "bool", title: inTS("Send Push Notitifications\n(Optional)", getAppImg("notification", true)), required: false, submitOnChange: true, defaultValue: false, image: getAppImg("notification")
        }
        section(sTS("SMS Text Messaging:")) {
            paragraph "To send to multiple numbers separate the number by a comma\nE.g. 8045551122,8046663344"
            input "smsNumbers", "text", title: inTS("Send SMS to Text to...\n(Optional)", getAppImg("sms_phone", true)), required: false, submitOnChange: true, image: getAppImg("sms_phone")
        }
        section(sTS("Pushover Support:")) {
            input ("pushoverEnabled", "bool", title: inTS("Use Pushover Integration", getAppImg("pushover", true)), description: "requires Pushover Manager app.", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("pushover"))
            if(settings?.pushoverEnabled == true) {
                if(state?.isInstalled) {
                    if(!state?.pushoverManager) {
                        paragraph "If this is the first time enabling Pushover than leave this page and come back if the devices list is empty"
                        pushover_init()
                    } else {
                        input "pushoverDevices", "enum", title: inTS("Select Pushover Devices"), description: "Tap to select", groupedOptions: getPushoverDevices(), multiple: true, required: false, submitOnChange: true
                        if(settings?.pushoverDevices) {
                            def t0 = ["-2":"Lowest", "-1":"Low", "0":"Normal", "1":"High", "2":"Emergency"]
                            input "pushoverPriority", "enum", title: inTS("Notification Priority (Optional)"), description: "Tap to select", defaultValue: "0", required: false, multiple: false, submitOnChange: true, options: t0
                            input "pushoverSound", "enum", title: inTS("Notification Sound (Optional)"), description: "Tap to select", defaultValue: "pushover", required: false, multiple: false, submitOnChange: true, options: getPushoverSounds()
                        }
                    }
                } else { paragraph "New Install Detected!!!\n\n1. Press Done to Finish the Install.\n2. Goto the Automations Tab at the Bottom\n3. Tap on the SmartApps Tab above\n4. Select ${app?.getLabel()} and Resume configuration", state: "complete" }
            }
        }
        if(settings?.smsNumbers?.toString()?.length()>=10 || settings?.usePush || (settings?.pushoverEnabled && settings?.pushoverDevices)) {
            if((settings?.usePush || (settings?.pushoverEnabled && settings?.pushoverDevices)) && !state?.pushTested && state?.pushoverManager) {
                if(sendMsg("Info", "Push Notification Test Successful. Notifications Enabled for ${app?.label}", true)) {
                    state.pushTested = true
                }
            }
            section(sTS("Notification Restrictions:")) {
                def t1 = getNotifSchedDesc()
                href "setNotificationTimePage", title: inTS("Notification Restrictions", getAppImg("restriction", true)), description: (t1 ?: "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("restriction")
            }
            section(sTS("Missed Poll Alerts:")) {
                input (name: "sendMissedPollMsg", type: "bool", title: inTS("Send Missed Checkin Alerts?", getAppImg("late", true)), defaultValue: true, submitOnChange: true, image: getAppImg("late"))
                if(settings?.sendMissedPollMsg) {
                    def misPollNotifyWaitValDesc = settings?.misPollNotifyWaitVal ?: "Default: 45 Minutes"
                    input (name: "misPollNotifyWaitVal", type: "enum", title: inTS("Time Past the Missed Checkin?", getAppImg("delay_time", true)), required: false, defaultValue: 2700, options: notifValEnum(), submitOnChange: true, image: getAppImg("delay_time"))
                    if(settings?.misPollNotifyWaitVal) { pollWait = settings?.misPollNotifyWaitVal as Integer }

                    def misPollNotifyMsgWaitValDesc = settings?.misPollNotifyMsgWaitVal ?: "Default: 1 Hour"
                    input (name: "misPollNotifyMsgWaitVal", type: "enum", title: inTS("Send Reminder After?", getAppImg("reminder", true)), required: false, defaultValue: 3600, options: notifValEnum(), submitOnChange: true, image: getAppImg("reminder"))
                    if(settings?.misPollNotifyMsgWaitVal) { pollMsgWait = settings?.misPollNotifyMsgWaitVal as Integer }
                }
            }
            section(sTS("Cookie Refresh Alert:")) {
                input (name: "sendCookieRefreshMsg", type: "bool", title: inTS("Send on Refreshed Cookie?", getAppImg("cookie", true)), defaultValue: false, submitOnChange: true, image: getAppImg("cookie"))
            }
            section(sTS("Code Update Alerts:")) {
                input "sendAppUpdateMsg", "bool", title: inTS("Send for Updates...", getAppImg("update", true)), defaultValue: true, submitOnChange: true, image: getAppImg("update")
                if(settings?.sendAppUpdateMsg) {
                    def updNotifyWaitValDesc = settings?.updNotifyWaitVal ?: "Default: 12 Hours"
                    input (name: "updNotifyWaitVal", type: "enum", title: inTS("Send Reminders After?", getAppImg("reminder", true)), required: false, defaultValue: 43200, options: notifValEnum(), submitOnChange: true, image: getAppImg("reminder"))
                    if(settings?.updNotifyWaitVal) { updNotifyWait = settings?.updNotifyWaitVal as Integer }
                }
            }
        } else { state.pushTested = false }
        state.misPollNotifyWaitVal = pollWait
        state.misPollNotifyMsgWaitVal = pollMsgWait
        state.updNotifyWaitVal = updNotifyWait
    }
}

def setNotificationTimePage() {
    dynamicPage(name: "setNotificationTimePage", title: "Prevent Notifications\nDuring these Days, Times or Modes", uninstall: false) {
        Boolean timeReq = (settings["qStartTime"] || settings["qStopTime"]) ? true : false
        section() {
            input "qStartInput", "enum", title: inTS("Starting at", getAppImg("start_time", true)), options: ["A specific time", "Sunrise", "Sunset"], defaultValue: null, submitOnChange: true, required: false, image: getAppImg("start_time")
            if(settings["qStartInput"] == "A specific time") {
                input "qStartTime", "time", title: inTS("Start time", getAppImg("start_time", true)), required: timeReq, image: getAppImg("start_time")
            }
            input "qStopInput", "enum", title: inTS("Stopping at", getAppImg("stop_time", true)), options: ["A specific time", "Sunrise", "Sunset"], defaultValue: null, submitOnChange: true, required: false, image: getAppImg("stop_time")
            if(settings?."qStopInput" == "A specific time") {
                input "qStopTime", "time", title: inTS("Stop time", getAppImg("stop_time", true)), required: timeReq, image: getAppImg("stop_time")
            }
            input "quietDays", "enum", title: inTS("Only on these days of the week", getAppImg("day_calendar", true)), multiple: true, required: false, image: getAppImg("day_calendar"),
                    options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            input "quietModes", "mode", title: inTS("When these Modes are Active", getAppImg("mode", true)), multiple: true, submitOnChange: true, required: false, image: getAppImg("mode")
        }
    }
}

def uninstallPage() {
    dynamicPage(name: "uninstallPage", title: "Uninstall", uninstall: true) {
        section("") { paragraph "This will uninstall the App and All Child Devices.\n\nPlease make sure that any devices created by this app are removed from any routines/rules/smartapps before tapping Remove." }
        if(isST()) { remove("Remove ${app?.label} and Devices!", "WARNING!!!", "Last Chance to Stop!\nThis action is not reversible\n\nThis App and Devices will be removed") }
    }
}

String bulletItem(String inStr, String strVal) { return "${inStr == "" ? "" : "\n"} \u2022 ${strVal}" }
String dashItem(String inStr, String strVal, newLine=false) { return "${(inStr == "" && !newLine) ? "" : "\n"} - ${strVal}" }

def deviceTestPage() {
    return dynamicPage(name: "deviceTestPage", uninstall: false, install: false) {
        section("") {
            href "broadcastPage", title: inTS("Broadcast Test", getAppImg("broadcast", true)), description: (t1 ?: "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("broadcast")
            href "announcePage", title: inTS("Announcement Test", getAppImg("broadcast", true)), description: (t1 ?: "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("announcement")
            href "sequencePage", title: inTS("Sequence Creator Test", getAppImg("broadcast", true)), description: (t1 ?: "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("sequence")
        }
    }
}

def broadcastPage() {
    return dynamicPage(name: "broadcastPage", uninstall: false, install: false) {
        section("") {
            Map devs = getDeviceList(true, true)
            input "broadcastDevices", "enum", title: inTS("Select Devices to Test the Broadcast"), description: "Tap to select", options: (devs ? devs?.sort{it?.value} : []), multiple: true, required: false, submitOnChange: true
            input "broadcastVolume", "number", title: inTS("Broadcast at this volume"), description: "Enter number", range: "0..100", defaultValue: 30, required: false, submitOnChange: true
            input "broadcastRestVolume", "number", title: inTS("Restore to this volume after"), description: "Enter number", range: "0..100", defaultValue: null, required: false, submitOnChange: true
            input "broadcastMessage", "text", title: inTS("Message to broadcast"), defaultValue: "This is a test of the Echo speaks broadcast system!!!", required: true, submitOnChange: true
            input "broadcastParallel", "bool", title: inTS("Execute commands in Parallel?"), description: "", required: false, defaultValue: true, submitOnChange: true
        }
        if(settings?.broadcastDevices) {
            section() {
                input "broadcastRun", "bool", title: inTS("Perform the Broadcast?"), description: "", required: false, defaultValue: false, submitOnChange: true
                if(broadcastRun) { executeBroadcast() }
            }
        }
    }
}

def announcePage() {
    return dynamicPage(name: "announcePage", uninstall: false, install: false) {
        section("") {
            Map devs = getDeviceList(true, true)
            input "announceDevices", "enum", title: inTS("Select Devices to Test the Announcement"), description: "Tap to select", options: (devs ? devs?.sort{it?.value} : []), multiple: true, required: false, submitOnChange: true
            input "announceVolume", "number", title: inTS("Announce at this volume"), description: "Enter number", range: "0..100", defaultValue: 30, required: false, submitOnChange: true
            input "announceRestVolume", "number", title: inTS("Restore to this volume after"), description: "Enter number", range: "0..100", defaultValue: null, required: false, submitOnChange: true
            input "announceMessage", "text", title: inTS("Message to announce"), defaultValue: "This is a test of the Echo speaks broadcast system!!!", required: true, submitOnChange: true
        }
        if(settings?.announceDevices) {
            section() {
                input "announceRun", "bool", title: inTS("Perform the Announcement?"), description: "", required: false, defaultValue: false, submitOnChange: true
                if(announceRun) { executeAnnouncement() }
            }
        }
    }
}

Map seqItemsAvail() {
    return [
        other: [
            "weather":null, "traffic":null, "flashbriefing":null, "goodmorning":null, "goodnight":null, "cleanup":null,
            "singasong":null, "tellstory":null, "funfact":null, "joke":null, "playsearch":null, "calendartoday":null,
            "calendartomorrow":null, "calendarnext":null, "stop":null, "stopalldevices":null,
            "wait": "value (seconds)", "volume": "value (0-100)", "speak": "message", "announcement": "message",
            "announcementall": "message", "pushnotification": "message"
        ],
        // dnd: [
        //     "dnd_duration": "2H30M", "dnd_time": "00:30", "dnd_all_duration": "2H30M", "dnd_all_time": "00:30",
        //     "dnd_duration":"2H30M", "dnd_time":"00:30"
        // ],
        speech: [
            "cannedtts_random": ["goodbye", "confirmations", "goodmorning", "compliments", "birthday", "goodnight", "iamhome"]
        ],
        music: [
            "amazonmusic": "search term", "applemusic": "search term", "iheartradio": "search term", "pandora": "search term",
            "spotify": "search term", "tunein": "search term", "cloudplayer": "search term"
        ]
    ]
}

def sequencePage() {
    return dynamicPage(name: "sequencePage", uninstall: false, install: false) {
        section(sTS("Command Legend:"), hideable: true, hidden: true) {
            String str1 = "Sequence Options:"
            seqItemsAvail()?.other?.sort()?.each { k, v->
                str1 += "${bulletItem(str1, "${k}${v != null ? "::${v}" : ""}")}"
            }
            String str2 = "Music Options:"
            seqItemsAvail()?.music?.sort()?.each { k, v->
                str2 += "${bulletItem(str2, "${k}${v != null ? "::${v}" : ""}")}"
            }
            String str3 = "Canned TTS Options:"
            seqItemsAvail()?.speech?.sort()?.each { k, v->
                def newV = v
                if(v instanceof List) { newV = ""; v?.sort()?.each { newV += "     ${dashItem(newV, "${it}", true)}"; } }
                str3 += "${bulletItem(str3, "${k}${newV != null ? "::${newV}" : ""}")}"
            }
            paragraph str1, state: "complete"
            paragraph str2, state: "complete"
            paragraph str3, state: "complete"
            paragraph "Enter the command in a format exactly like this:\nvolume::40,, speak::this is so silly,, wait::60,, weather,, cannedtts_random::goodbye,, traffic,, amazonmusic::green day,, volume::30\n\nEach command needs to be separated by a double comma `,,` and the separator between the command and value must be command::value.", state: "complete"
        }
        section(sTS("Sequence Test Config:")) {
            input "sequenceDevice", "device.EchoSpeaksDevice", title: inTS("Select Devices to Test Sequence Command"), description: "Tap to select", multiple: false, required: false, submitOnChange: true
            input "sequenceString", "text", title: inTS("Sequence String to Use"), defaultValue: "", required: false, submitOnChange: true
        }
        if(settings?.sequenceDevice && settings?.sequenceString) {
            section() {
                input "sequenceRun", "bool", title: inTS("Perform the Sequence?"), description: "", required: false, defaultValue: false, submitOnChange: true
                if(sequenceRun) { executeSequence() }
            }
        }
    }
}

Integer getRecheckDelay(Integer msgLen=null, addRandom=false) {
    def random = new Random()
	Integer randomInt = random?.nextInt(5) //Was using 7
    if(!msgLen) { return 30 }
    def v = (msgLen <= 14 ? 1 : (msgLen / 14)) as Integer
    // logger("trace", "getRecheckDelay($msgLen) | delay: $v + $randomInt")
    return addRandom ? (v + randomInt) : (v < 5 ? 5 : v)
}

private executeBroadcast() {
    settingUpdate("broadcastRun", "false", "bool")
    String testMsg = settings?.broadcastMessage
    Map eDevs = state?.echoDeviceMap
    List seqItems = []
    def selectedDevs = settings?.broadcastDevices
    selectedDevs?.each { dev->
        seqItems?.push([command: "volume", value: settings?.broadcastVolume as Integer, serial: dev, type: eDevs[dev]?.type])
        seqItems?.push([command: "speak", value: testMsg, serial: dev, type: eDevs[dev]?.type])
    }
    sendMultiSequenceCommand(seqItems, "broadcastTest", settings?.broadcastParallel)
    // schedules volume restore
    runIn(getRecheckDelay(testMsg?.length()), "broadcastVolumeRestore")
}

private broadcastVolumeRestore() {
    Map eDevs = state?.echoDeviceMap
    def selectedDevs = settings?.broadcastDevices
    List seqItems = []
    selectedDevs?.each { dev-> seqItems?.push([command: "volume", value: (settings?.broadcastRestVolume ?: 30), serial: dev, type: eDevs[dev]?.type]) }
    sendMultiSequenceCommand(seqItems, "broadcastVolumeRestore", settings?.broadcastParallel)
}

private announcementVolumeRestore() {
    Map eDevs = state?.echoDeviceMap
    def selectedDevs = settings?.announceDevices
    List seqItems = []
    selectedDevs?.each { dev-> seqItems?.push([command: "volume", value: (settings?.announceRestVolume ?: 30), serial: dev, type: eDevs[dev]?.type]) }
    sendMultiSequenceCommand(seqItems, "announcementVolumeRestore", settings?.broadcastParallel)
}

private executeAnnouncement() {
    settingUpdate("announceRun", "false", "bool")
    String testMsg = settings?.announceMessage
    Map eDevs = state?.echoDeviceMap
    List seqItems = []
    def selectedDevs = settings?.announceDevices
    selectedDevs?.each { dev->
        seqItems?.push([command: "volume", value: settings?.announceRestVolume as Integer, serial: dev, type: eDevs[dev]?.type])
    }
    seqItems?.push([command: "announcementTest", value: testMsg, serial: null, type: null])
    sendMultiSequenceCommand(seqItems, "announcementTest", settings?.broadcastParallel)
    runIn(getRecheckDelay(testMsg?.length()), "announcementVolumeRestore")
}

private executeSequence() {
    settingUpdate("sequenceRun", "false", "bool")
    String seqStr = settings?.sequenceString
    if(settings?.sequenceDevice?.hasCommand("executeSequenceCommand")) {
        settings?.sequenceDevice?.executeSequenceCommand(seqStr as String)
    } else {
        log.warn "sequence test device doesn't support the executeSequenceCommand command..."
    }
}

private executeTuneInSearch() {
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/tunein/search",
        query: [ query: settings?.tuneinSearchQuery, mediaOwnerCustomerId: state?.deviceOwnerCustomerId ],
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json"
    ]
    Map results = makeSyncronousReq(params, "get", "tuneInSearch") ?: [:]
    return results
}

private executeMusicSearchTest() {
    settingUpdate("performMusicTest", "false", "bool")
    if(settings?.musicTestDevice && settings?.musicTestProvider && settings?.musicTestQuery) {
        if(settings?.musicTestDevice?.hasCommand("searchMusic")) {
            log.debug "Performing ${settings?.musicTestProvider} Search Test with Query: (${settings?.musicTestQuery}) on Device: (${settings?.musicTestDevice})"
            settings?.musicTestDevice?.searchMusic(settings?.musicTestQuery as String, settings?.musicTestProvider as String)
        } else { log.error "The Device ${settings?.musicTestDevice} does NOT support the searchMusic() command..." }
    }
}

def musicSearchTestPage() {
    return dynamicPage(name: "musicSearchTestPage", uninstall: false, install: false) {
        section(sTS("Test a Music Search on Device:")) {
            paragraph "Use this to test the search you discovered above directly on a device.", state: "complete"
            Map testEnum = ["CLOUDPLAYER": "My Library", "AMAZON_MUSIC": "Amazon Music", "I_HEART_RADIO": "iHeartRadio", "PANDORA": "Pandora", "APPLE_MUSIC": "Apple Music", "TUNEIN": "TuneIn", "SIRIUSXM": "siriusXm", "SPOTIFY": "Spotify"]
            input "musicTestProvider", "enum", title: inTS("Select Music Provider to perform test", getAppImg("music", true)), defaultValue: null, required: false, options: testEnum, submitOnChange: true, image: getAppImg("music")
            if(musicTestProvider) {
                input "musicTestQuery", "text", title: inTS("Music Search term to test on Device", getAppImg("search2", true)), defaultValue: null, required: false, submitOnChange: true, image: getAppImg("search2")
                if(settings?.musicTestQuery) {
                    input "musicTestDevice", "device.EchoSpeaksDevice", title: inTS("Select a Device to Test Music Search", getAppImg("echo_speaks.1x", true)), description: "Tap to select", multiple: false, required: false, submitOnChange: true, image: getAppImg("echo_speaks.1x")
                    if(musicTestDevice) {
                        input "performMusicTest", "bool", title: inTS("Perform the Music Search Test?", getAppImg("music", true)), description: "", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("music")
                        if(performMusicTest) { executeMusicSearchTest() }
                    }
                }
            }
        }
        section(sTS("TuneIn Search Results:")) {
            paragraph "Enter a search phrase to query TuneIn to help you find the right search term to use in searchTuneIn() command.", state: "complete"
            input "tuneinSearchQuery", "text", title: inTS("Enter search phrase for TuneIn", getAppImg("tunein", true)), defaultValue: null, required: false, submitOnChange: true, image: getAppImg("tunein")
            if(settings?.tuneinSearchQuery) {
                href "searchTuneInResultsPage", title: inTS("View search results!", getAppImg("search2", true)), description: "Tap to proceed...", image: getAppImg("search2")
            }
        }
    }
}

def searchTuneInResultsPage() {
    return dynamicPage(name: "searchTuneInResultsPage", uninstall: false, install: false) {
        def results = executeTuneInSearch()
        Boolean onST = isST()
        section(sTS("Search Results: (Query: ${settings?.tuneinSearchQuery})")) {
            if(results?.browseList && results?.browseList?.size()) {
                results?.browseList?.eachWithIndex { item, i->
                    if(i < 25) {
                        if(item?.browseList != null && item?.browseList?.size()) {
                            item?.browseList?.eachWithIndex { item2, i2->
                                String str = ""
                                str += "ContentType: (${item2?.contentType})"
                                str += "\nId: (${item2?.id})"
                                str += "\nDescription: ${item2?.description}"
                                if(onST) {
                                    paragraph title: pTS(item2?.name?.take(75), (onST ? null : item2?.image)), str, required: true, state: (!item2?.name?.contains("Not Supported") ? "complete" : null), image: item2?.image ?: ""
                                } else { href "searchTuneInResultsPage", title: pTS(item2?.name?.take(75), (onST ? null : item2?.image)), description: str, required: true, state: (!item2?.name?.contains("Not Supported") ? "complete" : null), image: onST && item2?.image ? item2?.image : null }
                            }
                        } else {
                            String str = ""
                            str += "ContentType: (${item?.contentType})"
                            str += "\nId: (${item?.id})"
                            str += "\nDescription: ${item?.description}"
                            if(onST) {
                                paragraph title: pTS(item?.name?.take(75), (onST ? null : item?.image)), str, required: true, state: (!item?.name?.contains("Not Supported") ? "complete" : null), image: item?.image ?: ""
                            } else { href "searchTuneInResultsPage", title: pTS(item?.name?.take(75), (onST ? null : item?.image)), description: str, required: true, state: (!item?.name?.contains("Not Supported") ? "complete" : null), image: onST && item?.image ? item?.image : null }
                        }
                    }
                }
            } else { paragraph "No Results found..." }
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    state?.installData = [initVer: appVersion(), dt: getDtNow().toString(), updatedDt: "Not Set", sentMetrics: false, shownChgLog: true]
    state?.isInstalled = true
    sendInstallData()
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    if(!state?.isInstalled) { state?.isInstalled = true }
    if(!state?.installData) { state?.installData = [initVer: appVersion(), dt: getDtNow().toString(), updatedDt: getDtNow().toString(), sentMetrics: false] }
    unschedule()
    initialize()
}

def initialize() {
    if(app?.getLabel() != "Echo Speaks") { app?.updateLabel("Echo Speaks") }
    if(settings?.optOutMetrics == true && state?.appGuid) { if(removeInstallData()) { state?.appGuid = null } }
    subscribe(app, onAppTouch)
    if(!state?.resumeConfig) {
        runEvery5Minutes("healthCheck") // This task checks for missed polls, app updates, code version changes, and cloud service health
        appCleanup()
        runEvery1Minute("getOtherData")
        runEvery10Minutes("getEchoDevices") //This will reload the device list from Amazon
        validateCookie(true)
        runIn(15, "reInitDevices")
        getOtherData()
        getEchoDevices()
    }
}

def uninstalled() {
    log.warn "uninstalling app and devices"
    unschedule()
    if(settings?.optOutMetrics != true) { if(removeInstallData()) { state?.appGuid = null } }
    clearCloudConfig()
    clearCookieData()
    removeDevices(true)
}

def getGroupApps() {
    return getChildApps()?.findAll { it?.name == grpChildName() }
}

def getActionApps() {
    return getChildApps()?.findAll { it?.name == actChildName() }
}

public getBroadcastGrps() {
    Map grps = [:]
    def groupApps = getChildApps()?.findAll { it?.name == grpChildName() }
    groupApps?.each { grp->
        grps[grp?.getId() as String] = grp?.getBroadcastGroupData(true)
    }
    return grps
}

def onAppTouch(evt) {
    // log.trace "appTouch..."
    updated()
}

void settingUpdate(name, value, type=null) {
    if(name && type) {
        app?.updateSetting("$name", [type: "$type", value: value])
    }
    else if (name && type == null){ app?.updateSetting(name.toString(), value) }
}

void settingRemove(String name) {
	logger("trace", "settingRemove($name)...")
	if(name && settings?.containsKey(name as String)) { isST() ? app?.deleteSetting(name as String) : app?.removeSetting(name as String) }
}

mappings {
    path("/renderMetricData")  { action: [GET: "renderMetricData"] }
    path("/receiveData")       { action: [POST: "processData"] }
    path("/config")            { action: [GET: "renderConfig"] }
    path("/cookie")            { action: [GET: "getCookieData", POST: "storeCookieData", DELETE: "clearCookieData"] }
}

String getCookieVal() { return (state?.cookieData && state?.cookieData?.localCookie) ? state?.cookieData?.localCookie as String : null }
String getCsrfVal() { return (state?.cookieData && state?.cookieData?.csrf) ? state?.cookieData?.csrf as String : null }

def clearCloudConfig() {
    log.trace "clearCloudConfig called..."
    settingUpdate("resetService", "false", "bool")
    unschedule("cloudServiceHeartbeat")
    List remItems = ["generatedHerokuName", "useHeroku", "onHeroku", "nodeServiceInfo", "serverHost", "isLocal"]
    remItems?.each { rem->
        state?.remove(rem as String)
    }
    // (isST() ? app?.getChildDevices(true) : getChildDevices())?.each { dev-> dev?.setAuthState(false) }
    state?.serviceConfigured = false
    state?.resumeConfig = true
}

String getEnvParamsStr() {
    Map envParams = [:]
    envParams["smartThingsUrl"] = "${getAppEndpointUrl("receiveData")}"
    envParams["appCallbackUrl"] = "${getAppEndpointUrl("receiveData")}"
    envParams["hubPlatform"] = "${getPlatform()}"
    envParams["useHeroku"] = (isST() || settings?.useHeroku != false)
    envParams["serviceDebug"] = (settings?.serviceDebug == true) ? "true" : "false"
    envParams["serviceTrace"] = (settings?.serviceTrace == true) ? "true" : "false"
    envParams["amazonDomain"] = settings?.amazonDomain as String ?: "amazon.com"
    envParams["regionLocale"] = settings?.regionLocale as String ?: "en-US"
    envParams["hostUrl"] = "${getRandAppName()}.herokuapp.com"
    String envs = ""
    envParams?.each { k, v-> envs += "&env[${k}]=${v}" }
    return envs
}

private checkIfCodeUpdated() {
    if(state?.codeVersions && state?.codeVersions?.mainApp != appVersion()) {
        checkVersionData(true)
        log.info "Code Version Change! Re-Initializing SmartApp in 5 seconds..."
        state?.pollBlocked = true
        updCodeVerMap("mainApp", appVersion())
        Map iData = atomicState?.installData ?: [:]
        iData["updatedDt"] = getDtNow().toString()
        iData["shownChgLog"] = false
        atomicState?.installData = iData
        runIn(5, "postCodeUpdated", [overwrite: false])
        return true
    }
    state?.pollBlocked = false
    return false
}

private postCodeUpdated() {
    updated()
    runIn(10, "sendInstallData", [overwrite: false])
}

private appCleanup() {
    List items = ["availableDevices", "lastMsgDt", "consecutiveCmdCnt", "isRateLimiting", "versionData", "heartbeatScheduled", "serviceAuthenticated", "cookie"]
    items?.each { si-> if(state?.containsKey(si as String)) { state?.remove(si)} }
    state?.pollBlocked = false
    state?.resumeConfig = false
    state?.deviceRefreshInProgress = false
    // Settings Cleanup

    List setItems = ["tuneinSearchQuery", "performBroadcast", "performMusicTest", "stHub"]
    settings?.each { si-> if(si?.key?.startsWith("broadcast") || si?.key?.startsWith("musicTest") || si?.key?.startsWith("announce") || si?.key?.startsWith("sequence")) { setItems?.push(si?.key as String) } }
    setItems?.each { sI->
        if(settings?.containsKey(sI as String)) { settingRemove(sI as String) }
    }
}

private resetQueues() {
    (isST() ? app?.getChildDevices(true) : getChildDevices())?.each { it?.resetQueue() }
}

private reInitDevices() {
    (isST() ? app?.getChildDevices(true) : getChildDevices())?.each { it?.triggerInitialize() }
}

private updCodeVerMap(key, val) {
    Map cv = atomicState?.codeVersions ?: [:]
    cv[key as String] = val
    atomicState?.codeVersions = cv
}

String getRandAppName() {
    if(!state?.generatedHerokuName && (!state?.isLocal && !state?.serverHost)) { state?.generatedHerokuName = "${app?.name?.toString().replaceAll(" ", "-")}-${randomString(8)}"?.toLowerCase() }
    return state?.generatedHerokuName as String
}

def processData() {
    // log.trace "processData() | Data: ${request.JSON}"
    Map data = request?.JSON as Map
    if(data) {
        if(data?.version) {
            state?.onHeroku = (isST() || data?.onHeroku == true || data?.onHeroku == null || (!data?.isLocal && settings?.useHeroku != false))
            state?.isLocal = (!isST() && data?.isLocal == true)
            state?.serverHost = (data?.serverUrl ?: null)
            log.trace "processData Received | Version: ${data?.version} | onHeroku: ${data?.onHeroku} | serverUrl: ${data?.serverUrl}"
            updCodeVerMap("server", data?.version)
        } else { log.debug "data: $data" }
    }
    def json = new groovy.json.JsonOutput().toJson([message: "success", version: appVersion()])
    render contentType: "application/json", data: json, status: 200
}

def getCookieData() {
    log.trace "getCookieData() Request Received..."
    Map resp = state?.cookieData ?: [:]
    def json = new groovy.json.JsonOutput().toJson(resp)
    incrementCntByKey("getCookieCnt")
    render contentType: "application/json", data: json
}

def storeCookieData() {
    log.trace "storeCookieData Request Received..."
    if(request?.JSON && request?.JSON?.cookieData) {
        log.trace "cookieData Received: ${request?.JSON?.cookieData?.keySet()}"
        logger("trace", "cookieData Received: ${request?.JSON?.cookieData?.keySet()}")
        Map obj = [:]
        request?.JSON?.cookieData?.each { k,v->
            obj[k as String] = v as String
        }
        state?.cookieData = obj
        state?.onHeroku = (isST() || data?.onHeroku == true || data?.onHeroku == null || (!data?.isLocal && settings?.useHeroku != false))
        state?.isLocal = (!isST() && data?.isLocal == true)
        state?.serverHost = request?.JSON?.serverUrl ?: null
        updCodeVerMap("server", request?.JSON?.version)
    }
    if(state?.cookieData?.localCookie && state?.cookieData?.csrf) {
        log.info "Cookie Data has been Updated... Re-Initializing SmartApp and to restart polling in 10 seconds..."
        validateCookie(true)
        state?.serviceConfigured = true
        state?.lastCookieRefresh = getDtNow()
        runIn(10, "initialize", [overwrite: true])
    }
}

def clearCookieData(src=null) {
    logger("trace", "clearCookieData(${src ?: ""})")
    settingUpdate("resetCookies", "false", "bool")
    state?.remove("cookie")
    state?.remove("cookieData")
    state?.remove("lastCookieRefresh")
    unschedule("getEchoDevices")
    unschedule("getOtherData")
    log.warn "Cookie Data has been cleared and Device Data Refreshes have been suspended..."
    updateChildAuth(false)
    // if(getServerHostURL()) { clearServerAuth() }
}

private updateChildAuth(Boolean isValid) {
    (isST() ? app?.getChildDevices(true) : getChildDevices())?.each { it?.setAuthState(isValid) }
}

private authEvtHandler(Boolean isAuth) {
    state?.authValid = (isAuth == true)
    if(isAuth == false && !state?.noAuthActive) {
        clearCookieData()
        noAuthReminder()
        sendMsg("${app.name} Amazon Login Issue", "Amazon Cookie Has Expired or is Missing!!! Please login again using the Heroku Web Config page...")
        runEvery1Hour("noAuthReminder")
        state?.noAuthActive = true
        updateChildAuth(isAuth)
    } else {
        if(state?.noAuthActive) {
            unschedule("noAuthReminder")
            state?.noAuthActive = false
            runIn(10, "initialize", [overwrite: true])
        }
    }
}

Boolean isAuthValid(methodName) {
    if(state?.authValid == false) {
        log.warn "Echo Speaks Authentication is no longer valid... Please login again and commands will be allowed again!!! | Method: (${methodName})"
        return false
    }
    return true
}

private validateCookie(frc=false) {
    if((!frc && getLastCookieChkSec() <= 1800) || !getCookieVal() || !getCsrfVal()) { return }
    try {
        def params = [uri: getAmazonUrl(), path: "/api/bootstrap", query: ["version": 0], headers: [cookie: getCookieVal(), csrf: getCsrfVal()], contentType: "application/json"]
        execAsyncCmd("get", "cookieValidResp", params, [execDt: now()])
    } catch(ex) {
        incrementCntByKey("err_app_cookieValidCnt")
        log.error "validateCookie() Exception:", ex
    }
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v?.toString(), "utf-8").replaceAll("\\+", "%20")}" }?.sort().join("&")
}

String getServerHostURL() {
    return (state?.isLocal && state?.serverHost) ? (state?.serverHost ? "${state?.serverHost}" : null) : "https://${getRandAppName()}.herokuapp.com"
}

Integer getLastCookieRefreshSec() { return !state?.lastCookieRefresh ? 100000 : GetTimeDiffSeconds(state?.lastCookieRefresh, "getLastCookieRrshSec").toInteger() }

def clearServerAuth() {
    log.debug "serverUrl: ${getServerHostURL()}"
    Map params = [ uri: getServerHostURL(), path: "/clearAuth" ]
    def execDt = now()
    httpGet(params) { resp->
        log.debug "resp: ${resp.status} | data: ${resp?.data}"
        if (resp?.status == 200) {
            log.debug "clearServerAuth Completed... | Process Time: (${execDt ? (now()-execDt) : 0}ms)"
        }
    }
}

private runCookieRefresh() {
    settingUpdate("refreshCookie", "false", "bool")
    Map params = [
        uri: getServerHostURL(),
        path: "/config",
        contentType: "text/html",
        requestContentType: "text/html"
    ]
    execAsyncCmd("get", "wakeUpServerResp", params, [execDt: now()])
}

def wakeUpServerResp(response, data) {
    log.trace "wakeUpServerResp..."
    try { } catch(ex) { log.error "wakeUpServerResp Error: ${response?.getErrorMessage() ?: null}" }
    def rData = response?.data ?: null
    if (rData) {
        // log.debug "rData: $rData"
        log.debug "wakeUpServer Completed... | Process Time: (${data?.execDt ? (now()-data?.execDt) : 0}ms)"
        Map cookieData = state?.cookieData ?: [:]
        if (!cookieData || !cookieData?.loginCookie || !cookieData?.refreshToken) {
            log.error("Required Registration data is missing for Cookie Refresh")
            return
        }
        Map params = [
            uri: getServerHostURL(),
            path: "/refreshCookie"
        ]
        execAsyncCmd("get", "cookieRefreshResp", params, [execDt: now()])
    }
}

def cookieRefreshResp(response, data) {
    log.trace "cookieRefreshResp..."
    try { } catch(ex) { log.error "cookieRefreshResp Error: ${response?.getErrorMessage() ?: null}" }
    Map rData = response?.json ?: [:]
    if (rData && rData?.result && rData?.result?.size()) {
        log.debug "refreshAlexaCookie Completed | Process Time: (${data?.execDt ? (now()-data?.execDt) : 0}ms)"
        if(settings?.sendCookieRefreshMsg == true) { sendMsg("${app.name} Cookie Refresh", "Amazon Cookie was Refreshed Successfully!!!") }
        // log.debug "refreshAlexaCookie Response: ${rData?.result}"
    }
}

private apiHealthCheck(frc=false) {
    // if(!frc || (getLastApiChkSec() <= 1800)) { return }
    try {
        Map params = [uri: getAmazonUrl(), path: "/api/ping", query: ["_": ""], headers: [cookie: getCookieVal(), csrf: getCsrfVal()], contentType: "plain/text"]
        httpGet(params) { resp->
            log.debug "API Health Check Resp: (${resp?.getData()})"
            return (resp?.getData().toString() == "healthy")
        }
    } catch(ex) {
        incrementCntByKey("err_app_apiHealthCnt")
        log.error "apiHealthCheck() Exception:", ex
    }
}

def cookieValidResp(response, data) {
    // log.trace "cookieValidResp..."
    if(response?.status == 401) {
        log.error "cookieValidResp Status: (${response.status})"
        authEvtHandler(false)
        state?.lastCookieChkDt = getDtNow()
        return
    }
    Map aData = response?.json?.authentication ?: [:]
    Boolean valid = false
    if (aData) {
        if(aData?.customerId) { state?.deviceOwnerCustomerId = aData?.customerId }
        if(aData?.customerName) { state?.customerName = aData?.customerName }
        valid = (resp?.data?.authentication?.authenticated != false)
    }
    state?.lastCookieChkDt = getDtNow()
    def execTime = data?.execDt ? (now()-data?.execDt) : 0
    log.debug "Cookie Validation: (${valid}) | Process Time: (${execTime}ms)"
    authEvtHandler(valid)
}

private respIsValid(statusCode, Boolean hasErr, errMsg=null, String methodName, Boolean falseOnErr=false) {
    statusCode = statusCode as Integer
    if(statusCode == 401) {
        setAuthState(false)
        return false
    } else { if(statusCode > 401 && statusCode < 500) { log.error "${methodName} Error: ${errMsg ?: null}" } }
    if(hasErr && falseOnErr) { return false }
    return true
}

private noAuthReminder() { log.warn "Amazon Cookie Has Expired or is Missing!!! Please login again using the Heroku Web Config page..." }

private makeSyncronousReq(params, method="get", src, showLogs=false) {
    try {
        "http${method?.toString()?.toLowerCase()?.capitalize()}"(params) { resp ->
            if(resp?.data) {
                // log.debug "status: ${resp?.status}"
                if(showLogs) { log.debug "makeSyncronousReq(Src: $src) | Status: ${resp?.status}: ${resp?.data}" }
                return resp?.data
            }
            return null
        }
    } catch (ex) {
        if(ex instanceof  groovyx.net.http.ResponseParseException) {
            log.error "There was an errow while parsing the response: ", ex
        } else { log.error "makeSyncronousReq(Method: ${method}, Src: ${src}) exception", ex }
        return null
    }
}

public childInitiatedRefresh() {
    Integer lastRfsh = getLastChildInitRefreshSec()
    if(state?.deviceRefreshInProgress != true && lastRfsh > 120) {
        log.debug "A Child Device is requesting a Device List Refresh..."
        state?.lastChildInitRefreshDt = getDtNow()
        getOtherData()
        runIn(3, "getEchoDevices")
    } else {
        log.warn "childInitiatedRefresh request ignored... Refresh already in progress or it's too soon to refresh again | Last Refresh: (${lastRfsh} seconds)"
    }
}

private getEchoDevices() {
    if(!isAuthValid("getEchoDevices")) { return }
    def params = [
        uri: getAmazonUrl(),
        path: "/api/devices-v2/device",
        query: [ cached: true, _: new Date().getTime() ],
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
    ]
    state?.deviceRefreshInProgress = true
    state?.refreshDeviceData = false
    execAsyncCmd("get", "echoDevicesResponse", params, [execDt: now()])
}

private getMusicProviders() {
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/behaviors/entities",
        query: [ skillId: "amzn1.ask.1p.music" ],
        headers: ["Routines-Version": "1.1.210292", cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json"
    ]
    Map items = [:]
    List musicResp = makeSyncronousReq(params, "get", "getMusicProviders") ?: [:]
    if(musicResp?.size()) {
        musicResp?.findAll { it?.availability == "AVAILABLE" }?.each { item->
            items[item?.id] = item?.displayName
        }
    }
    // log.debug "items: $items"
    return items
}

private getOtherData() {
    getBluetoothDevices()
    getDoNotDisturb()
}

private getBluetoothDevices() {
    // log.trace "getBluetoothDevices"
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/bluetooth",
        query: [cached: true, _: new Date().getTime()],
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json"
    ]
    def btResp = makeSyncronousReq(params, "get", "getBluetoothDevices") ?: null
    state?.bluetoothData = btResp ?: [:]
}

def getBluetoothData(serialNumber) {
    // log.trace "getBluetoothData: ${serialNumber}"
    String curConnName = null
    Map btObjs = [:]
    Map btData = state?.bluetoothData ?: [:]
    Map bluData = btData && btData?.bluetoothStates?.size() ? btData?.bluetoothStates?.find { it?.deviceSerialNumber == serialNumber } : [:]
    if(bluData?.size() && bluData?.pairedDeviceList && bluData?.pairedDeviceList?.size()) {
        def bData = bluData?.pairedDeviceList?.findAll { (it?.deviceClass != "GADGET") }
        bData?.each {
            btObjs[it?.address as String] = it
            if(it?.connected == true) { curConnName = it?.friendlyName as String }
        }
    }
    return [btObjs: btObjs, pairedNames: btObjs?.collect { it?.value?.friendlyName as String } ?: [], curConnName: curConnName]
}

private getDoNotDisturb() {
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/dnd/device-status-list",
        query: [_: new Date().getTime()],
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
    ]
    def dndResp = makeSyncronousReq(params, "get", "getDoNotDisturb") ?: null
    state?.dndData = dndResp ?: [:]
}

def getDndEnabled(serialNumber) {
    // log.trace "getBluetoothData: ${serialNumber}"
    Map sData = state?.dndData ?: [:]
    def dndData = sData?.doNotDisturbDeviceStatusList?.size() ? sData?.doNotDisturbDeviceStatusList?.find { it?.deviceSerialNumber == serialNumber } : [:]
    return (dndData && dndData?.enabled == true)
}

private getRoutines(autoId=null, limit=2000) {
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/behaviors/automations${autoId ? "/${autoId}" : ""}",
        query: [ limit: limit ],
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json"
    ]
    Map items = [:]
    def routineResp = makeSyncronousReq(params, "get", "getRoutinesHandler") ?: [:]
    // log.debug "routineResp: $routineResp"
    if(routineResp) {
        if(autoId) {
            return routineResp
        } else {
            if(routineResp?.size()) {
                routineResp?.findAll { it?.status == "ENABLED" }?.each { item->
                    items[item?.automationId] = item?.name
                }
            }
        }
    }
    // log.debug "routine items: $items"
    return items
}

def executeRoutineById(String routineId) {
    def execDt = now()
    Map routineData = getRoutines(routineId)
    if(routineData && routineData?.sequence) {
        sendSequenceCommand("ExecuteRoutine", routineData, null)
        // log.debug "Executed Alexa Routine | Process Time: (${(now()-execDt)}ms) | RoutineId: ${routineId}"
        return true
    } else {
        log.debug "No Routine Data Returned for ID: (${routineId})"
        return false
    }
}

Map isFamilyAllowed(String family) {
    Map famMap = getDeviceFamilyMap()
    if(family in famMap?.block) { return [ok: false, reason: "Family Blocked"] }
    if(family in famMap?.echo) { return [ok: true, reason: "Amazon Echos Allowed"] }
    if(family in famMap?.tablet) {
        if(settings?.createTablets == true) { return [ok: true, reason: "Tablets Enabled"] }
        return [ok: false, reason: "Tablets Not Enabled"]
    }
    if(family in famMap?.wha) {
        if(settings?.createWHA == true) { return [ok: true, reason: "WHA Enabled"] }
        return [ok: false, reason: "WHA Devices Not Enabled"]
    }
    if(settings?.createOtherDevices == true) {
        return [ok: true, reason: "Other Devices Enabled"]
    } else { return [ok: false, reason: "Other Devices Not Enabled"] }
    return [ok: false, reason: "Unknown Reason"]
}

def echoDevicesResponse(response, data) {
    List ignoreTypes = getDeviceTypesMap()?.ignore ?: ["A1DL2DVDQVK3Q", "A21Z3CGI8UIP0F", "A2825NDLA7WDZV", "A2IVLV5VM2W81", "A2TF17PFR55MTB", "A1X7HJX9QL16M5", "A2T0P32DY3F7VB", "A3H674413M2EKB", "AILBSA2LNTOYL"]
    List removeKeys = ["appDeviceList", "charging", "macAddress", "deviceTypeFriendlyName", "registrationId", "remainingBatteryLevel", "postalCode", "language"]
    if(response?.status == 401) {
        authEvtHandler(false)
        return
    }
    try {
        // log.debug "json response is: ${response.json}"
        state?.deviceRefreshInProgress=false
        List eDevData = response?.json?.devices ?: []
        Map echoDevices = [:]
        if(eDevData?.size()) {
            eDevData?.each { eDevice->
                String serialNumber = eDevice?.serialNumber;
                // if (!(eDevice?.deviceType in ignoreTypes) && !eDevice?.accountName?.contains("Alexa App") && !eDevice?.accountName?.startsWith("This Device")) {
                if (!(eDevice?.deviceType in ignoreTypes) && !eDevice?.accountName?.startsWith("This Device")) {
                    removeKeys?.each { rk-> eDevice?.remove(rk as String) }
                    if (eDevice?.deviceOwnerCustomerId != null) { state?.deviceOwnerCustomerId = eDevice?.deviceOwnerCustomerId }
                    echoDevices[serialNumber] = eDevice
                }
            }
        }
        // log.debug "echoDevices: ${echoDevices}"
        receiveEventData([echoDevices: echoDevices, musicProviders: getMusicProviders(), execDt: data?.execDt], "Groovy")
    } catch (ex) {
        log.error "echoDevicesResponse Exception", ex
    }
}

def receiveEventData(Map evtData, String src) {
    try {
        if(checkIfCodeUpdated()) {
            log.warn "Possible Code Version Change Detected... Device Updates will occur on next cycle."
            return
        }
        // log.debug "musicProviders: ${evtData?.musicProviders}"
        logger("trace", "evtData(Keys): ${evtData?.keySet()}", true)
        if (evtData?.keySet()?.size()) {
            List ignoreTheseDevs = settings?.echoDeviceFilter ?: []
            Boolean onHeroku = (state?.onHeroku == true && state?.isLocal == true)

            //Check for minimum versions before processing
            Boolean updRequired = false
            List updRequiredItems = []
            ["server":"Echo Speaks Server", "echoDevice":"Echo Speaks Device"]?.each { k,v->
                Map codeVers = state?.codeVersions
                if(codeVers && codeVers[k as String] && (versionStr2Int(codeVers[k as String]) < minVersions()[k as String])) {
                    updRequired = true
                    updRequiredItems?.push("$v")
                }
            }

            if (evtData?.echoDevices?.size()) {
                def execTime = evtData?.execDt ? (now()-evtData?.execDt) : 0
                Map echoDeviceMap = [:]
                Map allEchoDevices = [:]
                Map skippedDevices = [:]
                List curDevFamily = []
                Integer cnt = 0
                evtData?.echoDevices?.each { echoKey, echoValue->
                    logger("debug", "echoDevice | $echoKey | ${echoValue}", true)
                    logger("debug", "echoDevice | ${echoValue?.accountName}", false)
                    allEchoDevices[echoKey] = [name: echoValue?.accountName]
                    // log.debug "name: ${echoValue?.accountName}"
                    Map familyAllowed = isFamilyAllowed(echoValue?.deviceFamily as String)
                    Map deviceStyleData = getDeviceStyle(echoValue?.deviceFamily as String, echoValue?.deviceType as String)
                    // log.debug "deviceStyle: ${deviceStyleData}"
                    Boolean isBlocked = (deviceStyleData?.blocked || familyAllowed?.reason == "Family Blocked")
                    Boolean isInIgnoreInput = (echoValue?.serialNumber in settings?.echoDeviceFilter)
                    Boolean allowTTS = (deviceStyleData?.allowTTS == true)
                    Boolean isMediaPlayer = (echoValue?.capabilities?.contains("AUDIO_PLAYER") || echoValue?.capabilities?.contains("AMAZON_MUSIC") || echoValue?.capabilities?.contains("TUNE_IN") || echoValue?.capabilities?.contains("PANDORA") || echoValue?.capabilities?.contains("I_HEART_RADIO") || echoValue?.capabilities?.contains("SPOTIFY"))
                    Boolean volumeSupport = (echoValue?.capabilities.contains("VOLUME_SETTING"))
                    Boolean unsupportedDevice = ((familyAllowed?.ok == false && familyAllowed?.reason == "Unknown Reason") || isBlocked == true)
                    Boolean bypassBlock = (settings?.bypassDeviceBlocks == true && !isInIgnoreInput)

                    if(!bypassBlock && (familyAllowed?.ok == false || isBlocked == true || (!allowTTS && !isMediaPlayer) || isInIgnoreInput)) {
                        logger("debug", "familyAllowed(${echoValue?.deviceFamily}): ${familyAllowed?.ok} | Reason: ${familyAllowed?.reason} | isBlocked: ${isBlocked} | deviceType: ${echoValue?.deviceType} | tts: ${allowTTS} | volume: ${volumeSupport} | mediaPlayer: ${isMediaPlayer}")
                        if(!skippedDevices?.containsKey(echoValue?.serialNumber as String)) {
                            List reasons = []
                            if(deviceStyleData?.blocked) {
                                reasons?.push("Device Blocked by App Config")
                            } else if(familyAllowed?.reason == "Family Blocked") {
                                reasons?.push("Family Blocked by App Config")
                            } else if (!familyAllowed?.ok) {
                                reasons?.push(familyAllowed?.reason)
                            } else if(isInIgnoreInput) {
                                reasons?.push("In Ignore Device Input")
                                logger("warn", "skipping ${echoValue?.accountName} because it is in the do not use list...")
                            } else {
                                if(!allowTTS) { reasons?.push("No TTS") }
                                if(!isMediaPlayer) { reasons?.push("No Media Controls") }
                            }
                            skippedDevices[echoValue?.serialNumber as String] = [
                                name: echoValue?.accountName, desc: deviceStyleData?.name, image: deviceStyleData?.image, family: echoValue?.deviceFamily,
                                type: echoValue?.deviceType, tts: allowTTS, volume: volumeSupport, mediaPlayer: isMediaPlayer, reason: reasons?.join(", "),
                                online: echoValue?.online
                            ]
                        }
                        return
                    }

                    echoValue["unsupported"] = (unsupportedDevice == true)
                    echoValue["authValid"] = (state?.authValid == true)
                    echoValue["amazonDomain"] = (settings?.amazonDomain ?: "amazon.com")
                    echoValue["regionLocale"] = (settings?.regionLocale ?: "en-US")
                    echoValue["cookie"] = [cookie: getCookieVal(), csrf: getCsrfVal()]
                    echoValue["deviceAccountId"] = echoValue?.deviceAccountId as String ?: null
                    echoValue["deviceStyle"] = deviceStyleData
                    // log.debug "deviceStyle: ${echoValue?.deviceStyle}"

                    Map permissions = [:]
                    permissions["TTS"] = allowTTS
                    permissions["volumeControl"] = volumeSupport
                    permissions["mediaPlayer"] = isMediaPlayer
                    permissions["amazonMusic"] = (echoValue?.capabilities.contains("AMAZON_MUSIC"))
                    permissions["tuneInRadio"] = (echoValue?.capabilities.contains("TUNE_IN"))
                    permissions["iHeartRadio"] = (echoValue?.capabilities.contains("I_HEART_RADIO"))
                    permissions["pandoraRadio"] = (echoValue?.capabilities.contains("PANDORA"))
                    permissions["appleMusic"] = (evtData?.musicProviders.containsKey("APPLE_MUSIC"))
                    permissions["siriusXm"] = (evtData?.musicProviders?.containsKey("SIRIUSXM"))
                    // permissions["tidal"] = true
                    permissions["spotify"] = true //(echoValue?.capabilities.contains("SPOTIFY")) // Temporarily removed restriction check
                    permissions["isMultiroomDevice"] = (echoValue?.clusterMembers && echoValue?.clusterMembers?.size() > 0) ?: false;
                    permissions["isMultiroomMember"] = (echoValue?.parentClusters && echoValue?.parentClusters?.size() > 0) ?: false;
                    permissions["alarms"] = (echoValue?.capabilities.contains("TIMERS_AND_ALARMS"))
                    permissions["reminders"] = (echoValue?.capabilities.contains("REMINDERS"))
                    permissions["doNotDisturb"] = (echoValue?.capabilities?.contains("SLEEP"))
                    permissions["wakeWord"] = (echoValue?.capabilities?.contains("FAR_FIELD_WAKE_WORD"))
                    permissions["flashBriefing"] = (echoValue?.capabilities?.contains("FLASH_BRIEFING"))
                    permissions["microphone"] = (echoValue?.capabilities?.contains("MICROPHONE"))
                    permissions["followUpMode"] = (echoValue?.capabilities?.contains("GOLDFISH"))
                    permissions["connectedHome"] = (echoValue?.capabilities?.contains("SUPPORTS_CONNECTED_HOME"))
                    permissions["bluetoothControl"] = (echoValue?.capabilities.contains("PAIR_BT_SOURCE") || echoValue?.capabilities.contains("PAIR_BT_SINK"))
                    permissions["alexaGuardSupport"] = (echoValue?.capabilities?.contains("TUPLE"))
                    echoValue["musicProviders"] = evtData?.musicProviders
                    echoValue["permissionMap"] = permissions
                    echoValue["hasClusterMembers"] = (echoValue?.clusterMembers && echoValue?.clusterMembers?.size() > 0) ?: false
                    // log.warn "Device Permisions | Name: ${echoValue?.accountName} | $permissions"

                    echoDeviceMap[echoKey] = [
                        name: echoValue?.accountName, online: echoValue?.online, family: echoValue?.deviceFamily, serialNumber: echoKey,
                        style: echoValue?.deviceStyle, type: echoValue?.deviceType, mediaPlayer: isMediaPlayer,
                        ttsSupport: allowTTS, volumeSupport: volumeSupport, clusterMembers: echoValue?.clusterMembers,
                        musicProviders: evtData?.musicProviders?.collect{ it?.value }?.sort()?.join(", "), supported: (unsupportedDevice != true)
                    ]

                    String dni = [app?.id, "echoSpeaks", echoKey].join("|")
                    def childDevice = getChildDevice(dni)
                    String devLabel = "Echo - ${echoValue?.accountName}${echoValue?.deviceFamily == "WHA" ? " (WHA)" : ""}"
                    String childHandlerName = "Echo Speaks Device"
                    if (!childDevice) {
                        // log.debug "childDevice not found | autoCreateDevices: ${settings?.autoCreateDevices}"
                        if(settings?.autoCreateDevices != false) {
                            try{
                                log.debug "Creating NEW Echo Speaks Device!!! | Device Label: ($devLabel)${(settings?.bypassDeviceBlocks && unsupportedDevice) ? " | (UNSUPPORTED DEVICE)" : "" }"
                                childDevice = addChildDevice("tonesto7", childHandlerName, dni, null, [name: childHandlerName, label: devLabel, completedSetup: true])
                            } catch(ex) {
                                log.error "AddDevice Error! ", ex
                            }
                        }
                    } else {
                        //Check and see if name needs a refresh
                        if (settings?.autoRenameDevices != false && (childDevice?.name != childHandlerName || childDevice?.label != devLabel)) {
                            log.debug ("Amazon Device Name Change Detected... Updating Device Name to (${devLabel}) | Old Name: (${childDevice?.label})")
                            childDevice?.name = childHandlerName as String
                            childDevice?.setLabel(devLabel as String)
                        }
                        // logger("info", "Sending Device Data Update to ${devLabel} | Last Updated (${getLastDevicePollSec()}sec ago)")
                        childDevice?.updateDeviceStatus(echoValue)
                        // childDevice?.updateServiceInfo(getServiceHostInfo(), onHeroku)
                        updCodeVerMap("echoDevice", childDevice?.devVersion()) // Update device versions in codeVersions state Map
                    }
                    curDevFamily.push(echoValue?.deviceStyle?.name)
                }
                log.debug "Device Data Received and Updated for (${echoDeviceMap?.size()}) Alexa Devices | Took: (${execTime}ms) | Last Refreshed: (${(getLastDevicePollSec()/60).toFloat()?.round(1)} minutes)"
                state?.lastDevDataUpd = getDtNow()
                state?.echoDeviceMap = echoDeviceMap
                state?.allEchoDevices = allEchoDevices
                state?.skippedDevices = skippedDevices
                state?.deviceStyleCnts = curDevFamily?.countBy { it }
            } else {
                log.warn "No Echo Device Data Sent... This may be the first transmission from the service after it started up!"
            }
            if(updRequired) {
                log.warn "CODE UPDATES REQUIRED: Echo Speaks Integration may not function until the following items are ALL Updated ${updRequiredItems}..."
                appUpdateNotify()
            }
            if(state?.installData?.sentMetrics != true) { runIn(900, "sendInstallData", [overwrite: false]) }
        }
    } catch(ex) {
        log.error "receiveEventData Error:", ex
        incrementCntByKey("appErrorCnt")
    }
}

public getDeviceStyle(String family, String type) {
    if(!state?.appData || !state?.appData?.deviceSupport) { checkVersionData(true) }
    Map typeData = state?.appData?.deviceSupport ?: [:]
    if(typeData[type]) {
        return typeData[type]
    } else { return [name: "Echo Unknown $type", image: "unknown", allowTTS: false] }
}

public Map getDeviceFamilyMap() {
    if(!state?.appData || !state?.appData?.deviceFamilies) { checkVersionData(true) }
    return state?.appData?.deviceFamilies ?: [:]
}

public Map getDeviceTypesMap() {
    if(!state?.appData || !state?.appData?.deviceTypes) { checkVersionData(true) }
    return state?.appData?.deviceTypes ?: [:]
}

private getDevicesFromSerialList(serialNumberList) {
    //log.trace "getDevicesFromSerialList called with: ${ serialNumberList}"
    if (serialNumberList == null) {
       log.debug "SerialNumberList is null"
       return;
    }
    def devicesList = serialNumberList.findResults { echoKey ->
        String dni = [app?.id, "echoSpeaks", echoKey].join("|")
        getChildDevice(dni)
    }
    //log.debug "Device list: ${ devicesList}"
    return devicesList
}

// This is called by the device handler to send playback data to cluster members
public sendPlaybackStateToClusterMembers(whaKey, response, data) {
    //log.trace "sendPlaybackStateToClusterMembers: key: ${ whaKey}"
    def echoDeviceMap = state?.echoDeviceMap
    def whaMap = echoDeviceMap[whaKey]
    def clusterMembers = whaMap?.clusterMembers

    if (clusterMembers) {
        def clusterMemberDevices = getDevicesFromSerialList(clusterMembers)
        clusterMemberDevices?.each { it?.getPlaybackStateHandler(response, data, true) }
    } else {
        // The lookup will fail during initial refresh because echoDeviceMap isn't available yet
        //log.debug "sendPlaybackStateToClusterMembers: no data found for ${whaKey} (first refresh?)"
    }
}

public getServiceHostInfo() {
    return (state?.isLocal && state?.serverHost) ? state?.serverHost : null
}

private removeDevices(all=false) {
    try {
        settingUpdate("cleanUpDevices", "false", "bool")
        List devList = getDeviceList(true)?.collect { String dni = [app?.id, "echoSpeaks", it?.key].join("|") }
        def items = app.getChildDevices()?.findResults { (all || (!all && !devList?.contains(it?.deviceNetworkId as String))) ? it?.deviceNetworkId as String : null }
        log.warn "removeDevices(${all ? "all" : ""}) | In Use: (${all ? 0 : devList?.size()}) | Removing: (${items?.size()})"
        if(items?.size() > 0) {
            Boolean isST = isST()
            items?.each {  isST ? deleteChildDevice(it as String, true) : deleteChildDevice(it as String) }
        }
    } catch (ex) { log.error "Device Removal Failed: ", ex }
}

Map sequenceBuilder(cmd, val) {
    def seqJson = null
    if (cmd instanceof Map) {
        seqJson = cmd?.sequence ?: cmd
    } else { seqJson = ["@type": "com.amazon.alexa.behaviors.model.Sequence", "startNode": createSequenceNode(cmd, val)] }
    Map seqObj = [behaviorId: (seqJson?.sequenceId ? cmd?.automationId : "PREVIEW"), sequenceJson: new JsonOutput().toJson(seqJson) as String, status: "ENABLED"]
    return seqObj
}

Map multiSequenceBuilder(commands, parallel=false) {
    String seqType = parallel ? "ParallelNode" : "SerialNode"
    List nodeList = []
    commands?.each { cmdItem-> nodeList?.push(createSequenceNode(cmdItem?.command, cmdItem?.value, [serialNumber: cmdItem?.serial, deviceType:cmdItem?.type])) }
    Map seqJson = [ "sequence": [ "@type": "com.amazon.alexa.behaviors.model.Sequence", "startNode": [ "@type": "com.amazon.alexa.behaviors.model.${seqType}", "name": null, "nodesToExecute": nodeList ] ] ]
    Map seqObj = sequenceBuilder(seqJson, null)
    return seqObj
}

Map createSequenceNode(command, value, Map deviceData = [:]) {
    try {
        Map seqNode = [
            "@type": "com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode",
            "operationPayload": [
                "deviceType": deviceData?.deviceType,
                "deviceSerialNumber": deviceData?.serialNumber,
                "locale": (settings?.regionLocale ?: "en-US"),
                "customerId": state?.deviceOwnerCustomerId
            ]
        ]
        switch (command) {
            case "volume":
                seqNode?.type = "Alexa.DeviceControls.Volume"
                seqNode?.operationPayload?.value = value;
                break
            case "speak":
                seqNode?.type = "Alexa.Speak"
                seqNode?.operationPayload?.textToSpeak = value as String
                break
            case "announcementTest":
                log.debug "test"
                seqNode?.type = "AlexaAnnouncement"
                seqNode?.operationPayload?.remove('deviceType')
                seqNode?.operationPayload?.remove('deviceSerialNumber')
                seqNode?.operationPayload?.remove('locale')
                seqNode?.operationPayload?.expireAfter = "PT5S"
                List valObj = (value?.toString()?.contains("::")) ? value?.split("::") : ["Echo Speaks", value as String]
                seqNode?.operationPayload?.content = [[
                    locale: (state?.regionLocale ?: "en-US"),
                    display: [ title: valObj[0], body: valObj[1] as String ],
                    speak: [ type: "text", value: valObj[1] as String ],
                ]]
                List announceDevs = []
                if(settings?.announceDevices) {
                    Map eDevs = state?.echoDeviceMap
                    settings?.announceDevices?.each { dev->
                        announceDevs?.push([deviceTypeId: eDevs[dev]?.type, deviceSerialNumber: dev])
                    }
                }
                seqNode?.operationPayload?.target = [ customerId : state?.deviceOwnerCustomerId, devices: announceDevs ]
                break
            default:
                return
        }
        // log.debug "seqNode: $seqNode"
        return seqNode
    } catch (ex) {
        log.error "createSequenceNode Exception: $ex"
        return [:]
    }
}

private execAsyncCmd(String method, String callbackHandler, Map params, Map otherData = null) {
    if(method && callbackHandler && params) {
        String m = method?.toString()?.toLowerCase()
        if(isST()) {
            include 'asynchttp_v1'
            asynchttp_v1."${m}"(callbackHandler, params, otherData)
        } else { "asynchttp${m?.capitalize()}"("${callbackHandler}", params, otherData) }
    } else { log.error "execAsyncCmd Error | Missing a required parameter" }
}

private sendAmazonCommand(String method, Map params, Map otherData) {
    execAsyncCmd(method, "amazonCommandResp", params, otherData)
}

def amazonCommandResp(response, data) {
    Boolean hasErr = (response?.hasError() == true)
    String errMsg = (hasErr && response?.getErrorMessage()) ? response?.getErrorMessage() : null
    if(!respIsValid(response?.status, hasErr, errMsg, "amazonCommandResp", true)) {return}
    try {} catch (ex) { }
    def resp = response?.data ? response?.getJson() : null
    // logger("warn", "amazonCommandResp | Status: (${response?.status}) | Response: ${resp} | PassThru-Data: ${data}")
    if(response?.status == 200) {
        log.trace "amazonCommandResp | Status: (${response?.status})${resp != null ? " | Response: ${resp}" : ""} | ${data?.cmdDesc} was Successfully Sent!!!"
    }
}

private sendSequenceCommand(type, command, value) {
    // logger("trace", "sendSequenceCommand($type) | command: $command | value: $value")
    Map seqObj = sequenceBuilder(command, value)
    sendAmazonCommand("post", [
        uri: getAmazonUrl(),
        path: "/api/behaviors/preview",
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json",
        body: seqObj
    ], [cmdDesc: "SequenceCommand (${type})"])
}

private sendMultiSequenceCommand(commands, String srcDesc, Boolean parallel=false) {
    String seqType = parallel ? "ParallelNode" : "SerialNode"
    List nodeList = []
    commands?.each { cmdItem-> nodeList?.push(createSequenceNode(cmdItem?.command, cmdItem?.value, [serialNumber: cmdItem?.serial, deviceType: cmdItem?.type])) }
    Map seqJson = [ "sequence": [ "@type": "com.amazon.alexa.behaviors.model.Sequence", "startNode": [ "@type": "com.amazon.alexa.behaviors.model.${seqType}", "name": null, "nodesToExecute": nodeList ] ] ]
    sendSequenceCommand("${srcDesc} | MultiSequence: ${parallel ? "Parallel" : "Sequential"}", seqJson, null)
}

/******************************************
|    Notification Functions
*******************************************/
String getAmazonDomain() { return settings?.amazonDomain as String }
String getAmazonUrl() {return "https://alexa.${settings?.amazonDomain as String}"}

Map notifValEnum(allowCust = true) {
    Map items = [
        300:"5 Minutes", 600:"10 Minutes", 900:"15 Minutes", 1200:"20 Minutes", 1500:"25 Minutes",
        1800:"30 Minutes", 2700:"45 Minutes", 3600:"1 Hour", 7200:"2 Hours", 14400:"4 Hours", 21600:"6 Hours", 43200:"12 Hours", 86400:"24 Hours"
    ]
    if(allowCust) { items[100000] = "Custom" }
    return items
}

private healthCheck() {
    // logger("trace", "healthCheck")
    checkVersionData()
    if(checkIfCodeUpdated()) {
        log.warn "Code Version Change Detected... Health Check will occur on next cycle."
        return
    }
    validateCookie()
    if(getLastCookieRefreshSec() > 432000) { runCookieRefresh() }
    if(!getOk2Notify()) { return }
    missPollNotify((settings?.sendMissedPollMsg == true), (state?.misPollNotifyMsgWaitVal ?: 3600))
    appUpdateNotify()
    if(state?.isInstalled && getLastMetricUpdSec() > (3600*24)) { runIn(30, "sendInstallData", [overwrite: true]) }
}

private missPollNotify(Boolean on, Integer wait) {
    logger("debug", "missPollNotify() | on: ($on) | wait: ($wait) | getLastDevicePollSec: (${getLastDevicePollSec()}) | misPollNotifyWaitVal: (${state?.misPollNotifyWaitVal}) | getLastMisPollMsgSec: (${getLastMisPollMsgSec()})")
    if(!on || !wait || !(getLastDevicePollSec() > (state?.misPollNotifyWaitVal ?: 2700))) { return }
    if(!(getLastMisPollMsgSec() > wait.toInteger())) {
        state?.missPollRepair = false
        return
    } else {
        if(!state?.missPollRepair) {
            state?.missPollRepair = true
            initialize()
            return
        }
        state?.missPollRepair = true
        String msg = ""
        if(state?.authValid) {
            msg = "\nThe Echo Speaks app has NOT received any device data from Amazon in the last (${getLastDevicePollSec()}) seconds.\nThere maybe an issue with the scheduling.  Please open the app and press Done/Save."
        } else { msg = "\nThe Amazon login info has expired!\nPlease open the heroku amazon authentication page and login again to restore normal operation." }
        log.warn "${msg.toString().replaceAll("\n", " ")}"
        if(sendMsg("${app.name} ${state?.authValid ? "Data Refresh Issue" : "Amazon Login Issue"}", msg)) {
            state?.lastMisPollMsgDt = getDtNow()
        }
        if(state?.authValid) {
            (isST() ? app?.getChildDevices(true) : getChildDevices())?.each { cd-> cd?.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: true, isStateChange: true) }
        }
    }
}

private appUpdateNotify() {
    Boolean on = (settings?.sendAppUpdateMsg != false)
    Boolean appUpd = isAppUpdateAvail()
    Boolean actUpd = false//isActionAppUpdateAvail()
    Boolean grpUpd = false//isGroupAppUpdateAvail()
    Boolean echoDevUpd = isEchoDevUpdateAvail()
    Boolean servUpd = isServerUpdateAvail()
    logger("debug", "appUpdateNotify() | on: (${on}) | appUpd: (${appUpd}) | actUpd: (${appUpd}) | grpUpd: (${grpUpd}) | echoDevUpd: (${echoDevUpd}) | servUpd: (${servUpd}) | getLastUpdMsgSec: ${getLastUpdMsgSec()} | state?.updNotifyWaitVal: ${state?.updNotifyWaitVal}")
    if(getLastUpdMsgSec() > state?.updNotifyWaitVal.toInteger()) {
        if(on && (appUpd || actUpd || grpUpd || echoDevUpd || servUpd)) {
            state?.updateAvailable = true
            def str = ""
            str += !appUpd ? "" : "\nEcho Speaks App: v${state?.appData?.versions?.mainApp?.ver?.toString()}"
            str += !actUpd ? "" : "\nEcho Speaks - Actions: v${state?.appData?.versions?.actionApp?.ver?.toString()}"
            str += !grpUpd ? "" : "\nEcho Speaks - Groups: v${state?.appData?.versions?.groupApp?.ver?.toString()}"
            str += !echoDevUpd ? "" : "\nEcho Speaks Device: v${state?.appData?.versions?.echoDevice?.ver?.toString()}"
            str += !servUpd ? "" : "\n${state?.onHeroku ? "Heroku Service" : "Node Service"}: v${state?.appData?.versions?.server?.ver?.toString()}"
            sendMsg("Info", "Echo Speaks Update(s) are Available:${str}...\n\nPlease visit the IDE to Update your code...")
            state?.lastUpdMsgDt = getDtNow()
            return
        }
        state?.updateAvailable = false
    }
}

Boolean pushStatus() { return (settings?.smsNumbers?.toString()?.length()>=10 || settings?.usePush || settings?.pushoverEnabled) ? ((settings?.usePush || (settings?.pushoverEnabled && settings?.pushoverDevices)) ? "Push Enabled" : "Enabled") : null }
Integer getLastMsgSec() { return !state?.lastMsgDt ? 100000 : GetTimeDiffSeconds(state?.lastMsgDt, "getLastMsgSec").toInteger() }
Integer getLastUpdMsgSec() { return !state?.lastUpdMsgDt ? 100000 : GetTimeDiffSeconds(state?.lastUpdMsgDt, "getLastUpdMsgSec").toInteger() }
Integer getLastMisPollMsgSec() { return !state?.lastMisPollMsgDt ? 100000 : GetTimeDiffSeconds(state?.lastMisPollMsgDt, "getLastMisPollMsgSec").toInteger() }
Integer getLastVerUpdSec() { return !state?.lastVerUpdDt ? 100000 : GetTimeDiffSeconds(state?.lastVerUpdDt, "getLastVerUpdSec").toInteger() }
Integer getLastDevicePollSec() { return !state?.lastDevDataUpd ? 840 : GetTimeDiffSeconds(state?.lastDevDataUpd, "getLastDevicePollSec").toInteger() }
Integer getLastCookieChkSec() { return !state?.lastCookieChkDt ? 3600 : GetTimeDiffSeconds(state?.lastCookieChkDt, "getLastCookieChkSec").toInteger() }
Integer getLastChildInitRefreshSec() { return !state?.lastChildInitRefreshDt ? 3600 : GetTimeDiffSeconds(state?.lastChildInitRefreshDt, "getLastChildInitRefreshSec").toInteger() }
Boolean getOk2Notify() {
    Boolean smsOk = (settings?.smsNumbers?.toString()?.length()>=10)
    Boolean pushOk = settings?.usePush
    Boolean pushOver = (settings?.pushoverEnabled && settings?.pushoverDevices)
    Boolean daysOk = quietDaysOk(settings?.quietDays)
    Boolean timeOk = quietTimeOk()
    Boolean modesOk = quietModesOk(settings?.quietModes)
    logger("debug", "getOk2Notify() | smsOk: $smsOk | pushOk: $pushOk | pushOver: $pushOver || daysOk: $daysOk | timeOk: $timeOk | modesOk: $modesOk")
    if(!(smsOk || pushOk || pushOver)) { return false }
    if(!(daysOk && modesOk && timeOk)) { return false }
    return true
}
Boolean quietModesOk(List modes) { return (modes && location?.mode?.toString() in modes) ? false : true }
Boolean quietTimeOk() {
    def strtTime = null
    def stopTime = null
    def now = new Date()
    def sun = getSunriseAndSunset() // current based on geofence, previously was: def sun = getSunriseAndSunset(zipCode: zipCode)
    if(settings?.qStartTime && settings?.qStopTime) {
        if(settings?.qStartInput == "sunset") { strtTime = sun?.sunset }
        else if(settings?.qStartInput == "sunrise") { strtTime = sun?.sunrise }
        else if(settings?.qStartInput == "A specific time" && settings?.qStartTime) { strtTime = settings?.qStartTime }

        if(settings?.qStopInput == "sunset") { stopTime = sun?.sunset }
        else if(settings?.qStopInput == "sunrise") { stopTime = sun?.sunrise }
        else if(settings?.qStopInput == "A specific time" && settings?.qStopTime) { stopTime = settings?.qStopTime }
    } else { return true }
    if(strtTime && stopTime) {
        return timeOfDayIsBetween(strtTime, stopTime, new Date(), location.timeZone) ? false : true
    } else { return true }
}

Boolean quietDaysOk(days) {
    if(days) {
        def dayFmt = new SimpleDateFormat("EEEE")
        if(location?.timeZone) { dayFmt?.setTimeZone(location?.timeZone) }
        return days?.contains(dayFmt?.format(new Date())) ? false : true
    }
    return true
}

// Sends the notifications based on app settings
public sendMsg(String msgTitle, String msg, Boolean showEvt=true, Map pushoverMap=null, sms=null, push=null) {
    logger("trace", "sendMsg() | msgTitle: ${msgTitle}, msg: ${msg}, showEvt: ${showEvt}")
    String sentstr = "Push"
    Boolean sent = false
    try {
        String newMsg = "${msgTitle}: ${msg}"
        String flatMsg = newMsg.toString().replaceAll("\n", " ")
        if(!getOk2Notify()) {
            log.info "sendMsg: Message Skipped During Quiet Time ($flatMsg)"
            if(showEvt) { sendNotificationEvent(newMsg) }
        } else {
            if(push || settings?.usePush) {
                sentstr = "Push Message"
                if(showEvt) {
                    sendPush(newMsg)	// sends push and notification feed
                } else {
                    sendPushMessage(newMsg)	// sends push
                }
                sent = true
            }
            if(settings?.pushoverEnabled && settings?.pushoverDevices) {
                sentstr = "Pushover Message"
                Map msgObj = [:]
                msgObj = pushoverMap ?: [title: msgTitle, message: msg, priority: (settings?.pushoverPriority?:0)]
                if(settings?.pushoverSound) { msgObj?.sound = settings?.pushoverSound }
                buildPushMessage(settings?.pushoverDevices, msgObj, true)
                sent = true
            }
            String smsPhones = sms ? sms.toString() : (settings?.smsNumbers?.toString() ?: null)
            if(smsPhones) {
                List phones = smsPhones?.toString()?.split("\\,")
                for (phone in phones) {
                    String t0 = newMsg.take(140)
                    if(showEvt) {
                        sendSms(phone?.trim(), t0)	// send SMS and notification feed
                    } else {
                        sendSmsMessage(phone?.trim(), t0)	// send SMS
                    }
                }
                sentstr = "Text Message to Phone [${phones}]"
                sent = true
            }
            if(sent) {
                state?.lastMsg = flatMsg
                state?.lastMsgDt = getDtNow()
                logger("debug", "sendMsg: Sent ${sentstr} (${flatMsg})")
            }
        }
    } catch (ex) {
        incrementCntByKey("appErrorCnt")
        log.error "sendMsg $sentstr Exception:", ex
    }
    return sent
}

String getAppImg(String imgName, frc=false) { return (frc || state?.hubPlatform == "SmartThings") ? "https://raw.githubusercontent.com/tonesto7/echo-speaks/${isBeta() ? "beta" : "master"}/resources/icons/${imgName}.png" : "" }
String getPublicImg(String imgName) { return isST() ? "https://raw.githubusercontent.com/tonesto7/SmartThings-tonesto7-public/master/resources/icons/${imgName}.png" : "" }
String sTS(String t, String i = null) { return isST() ? t : """<h3>${i ? """<img src="${i}" width="42"> """ : ""} ${t?.replaceAll("\\n", " ")}</h3>""" }
String inTS(String t, String i = null) { return isST() ? t : """${i ? """<img src="${i}" width="42"> """ : ""} <u>${t?.replaceAll("\\n", " ")}</u>""" }
String pTS(String t, String i = null) { return isST() ? t : """<b>${i ? """<img src="${i}" width="42"> """ : ""} ${t?.replaceAll("\\n", " ")}</b>""" }

String actChildName(){ return "Echo Speaks - Actions" }
String grpChildName(){ return "Echo Speaks - Groups" }
String documentationLink() { return "https://tonesto7.github.io/echo-speaks-docs" }
String textDonateLink() { return "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=HWBN4LB9NMHZ4" }
String getAppEndpointUrl(subPath)   { return isST() ? "${apiServerUrl("/api/smartapps/installations/${app.id}${subPath ? "/${subPath}" : ""}?access_token=${state.accessToken}")}" : "${getApiServerUrl()}/${getHubUID()}/apps/${app?.id}${subPath ? "/${subPath}" : ""}?access_token=${state?.accessToken}" }
String getLocalEndpointUrl(subPath) { return "${getLocalApiServerUrl()}/apps/${app?.id}${subPath ? "/${subPath}" : ""}?access_token=${state?.accessToken}" }
//PushOver-Manager Input Generation Functions
private getPushoverSounds(){return (Map) state?.pushoverManager?.sounds?:[:]}
private getPushoverDevices(){List opts=[];Map pmd=state?.pushoverManager?:[:];pmd?.apps?.each{k,v->if(v&&v?.devices&&v?.appId){Map dm=[:];v?.devices?.sort{}?.each{i->dm["${i}_${v?.appId}"]=i};addInputGrp(opts,v?.appName,dm);}};return opts;}
private inputOptGrp(List groups,String title){def group=[values:[],order:groups?.size()];group?.title=title?:"";groups<<group;return groups;}
private addInputValues(List groups,String key,String value){def lg=groups[-1];lg["values"]<<[key:key,value:value,order:lg["values"]?.size()];return groups;}
private listToMap(List original){original.inject([:]){r,v->r[v]=v;return r;}}
private addInputGrp(List groups,String title,values){if(values instanceof List){values=listToMap(values)};values.inject(inputOptGrp(groups,title)){r,k,v->return addInputValues(r,k,v)};return groups;}
private addInputGrp(values){addInputGrp([],null,values)}
//PushOver-Manager Location Event Subscription Events, Polling, and Handlers
public pushover_init(){subscribe(location,"pushoverManager",pushover_handler);pushover_poll()}
public pushover_cleanup(){state?.remove("pushoverManager");unsubscribe("pushoverManager");}
public pushover_poll(){sendLocationEvent(name:"pushoverManagerCmd",value:"poll",data:[empty:true],isStateChange:true,descriptionText:"Sending Poll Event to Pushover-Manager")}
public pushover_msg(List devs,Map data){if(devs&&data){sendLocationEvent(name:"pushoverManagerMsg",value:"sendMsg",data:data,isStateChange:true,descriptionText:"Sending Message to Pushover Devices: ${devs}");}}
public pushover_handler(evt){Map pmd=state?.pushoverManager?:[:];switch(evt?.value){case"refresh":def ed = evt?.jsonData;String id = ed?.appId;Map pA = pmd?.apps?.size() ? pmd?.apps : [:];if(id){pA[id]=pA?."${id}"instanceof Map?pA[id]:[:];pA[id]?.devices=ed?.devices?:[];pA[id]?.appName=ed?.appName;pA[id]?.appId=id;pmd?.apps = pA;};pmd?.sounds=ed?.sounds;break;case "reset":pmd=[:];break;};state?.pushoverManager=pmd;}
//Builds Map Message object to send to Pushover Manager
private buildPushMessage(List devices,Map msgData,timeStamp=false){if(!devices||!msgData){return};Map data=[:];data?.appId=app?.getId();data.devices=devices;data?.msgData=msgData;if(timeStamp){data?.msgData?.timeStamp=new Date().getTime()};pushover_msg(devices,data);}

/******************************************
|       Changelog Logic
******************************************/
String changeLogData() { return getWebData([uri: "https://raw.githubusercontent.com/tonesto7/echo-speaks/${isBeta() ? "beta" : "master"}/resources/changelog.txt", contentType: "text/plain; charset=UTF-8"], "changelog") }
Boolean showChgLogOk() { return (state?.isInstalled && state?.installData?.shownChgLog != true) }
def changeLogPage() {
    def execTime = now()
    return dynamicPage(name: "changeLogPage", title: "", nextPage: "mainPage", install: false) {
        section() {
            paragraph title: "What's New in this Release...", "", state: "complete", image: getAppImg("whats_new")
            paragraph changeLogData()
        }
        Map iData = atomicState?.installData ?: [:]
        iData["shownChgLog"] = true
        atomicState?.installData = iData
    }
}

/******************************************
|    METRIC Logic
******************************************/
String getFbMetricsUrl() { return state?.appData?.settings?.database?.metricsUrl ?: "https://echo-speaks-metrics.firebaseio.com" }
Integer getLastMetricUpdSec() { return !state?.lastMetricUpdDt ? 100000 : GetTimeDiffSeconds(state?.lastMetricUpdDt, "getLastMetricUpdSec").toInteger() }
Boolean metricsOk() { (settings?.optOutMetrics != true && state?.appData?.settings?.sendMetrics != false) }
private generateGuid() { if(!state?.appGuid) { state?.appGuid = UUID?.randomUUID().toString() } }
private sendInstallData() { settingUpdate("sendMetricsNow", "false", "bool"); if(metricsOk()) { sendFirebaseData(getFbMetricsUrl(), "/clients/${state?.appGuid}.json", createMetricsDataJson(), "put", "heartbeat"); } }
private removeInstallData() { return removeFirebaseData("/clients/${state?.appGuid}.json") }
private sendFirebaseData(url, path, data, cmdType=null, type=null) {
    logger("trace", "sendFirebaseData(${path}, ${data}, $cmdType, $type", true)
    return queueFirebaseData(url, path, data, cmdType, type)
}
def queueFirebaseData(url, path, data, cmdType=null, type=null) {
    logger("trace", "queueFirebaseData(${path}, ${data}, $cmdType, $type", true)
    Boolean result = false
    def json = new groovy.json.JsonOutput().prettyPrint(data)
    Map params = [uri: url as String, path: path as String, requestContentType: "application/json", contentType: "application/json", body: json.toString()]
    String typeDesc = type ? type as String : "Data"
    try {
        if(!cmdType || cmdType == "put") {
            execAsyncCmd(cmdType, "processFirebaseResponse", params, [type: typeDesc])
            result = true
        } else if (cmdType == "post") {
            execAsyncCmd(cmdType, "processFirebaseResponse", params, [type: typeDesc])
            result = true
        } else { log.debug "queueFirebaseData UNKNOWN cmdType: ${cmdType}" }

    } catch(ex) { log.error "queueFirebaseData (type: $typeDesc) Exception:", ex }
    return result
}

def removeFirebaseData(pathVal) {
    logger("trace", "removeFirebaseData(${pathVal})", true)
    Boolean result = true
    try {
        httpDelete(uri: getFbMetricsUrl(), path: pathVal as String) { resp ->
            logger("debug", "Remove Firebase | resp: ${resp?.status}")
        }
    } catch (ex) {
        if(ex instanceof groovyx.net.http.ResponseParseException) {
            logger("error", "removeFirebaseData: Response: ${ex?.message}")
        } else {
            logger("error", "removeFirebaseData: Response: ${ex?.message}")
            result = false
        }
    }
    return result
}

def processFirebaseResponse(resp, data) {
    logger("trace", "processFirebaseResponse(${data?.type})", true)
    Boolean result = false
    String typeDesc = data?.type as String
    try {
        if(resp?.status == 200) {
            logger("info", "processFirebaseResponse: ${typeDesc} Data Sent SUCCESSFULLY")
            if(typeDesc?.toString() == "heartbeat") { state?.lastMetricUpdDt = getDtNow() }
            def iData = atomicState?.installData ?: [:]
            iData["sentMetrics"] = true
            atomicState?.installData = iData
            result = true
        } else if(resp?.status == 400) {
            log.error "processFirebaseResponse: 'Bad Request': ${resp?.status}"
        } else { log.warn "processFirebaseResponse: 'Unexpected' Response: ${resp?.status}" }
        if (isST() && resp?.hasError()) { log.error "processFirebaseResponse: errorData: ${resp?.errorData} | errorMessage: ${resp?.errorMessage}" }
    } catch(ex) {
        log.error "processFirebaseResponse (type: $typeDesc) Exception:", ex
    }
}

def renderMetricData() {
    try {
        def json = new groovy.json.JsonOutput().prettyPrint(createMetricsDataJson())
        render contentType: "application/json", data: json
    } catch (ex) { log.error "renderMetricData Exception:", ex }
}

private Map getSkippedDevsAnon() {
    Map res = [:]
    Map sDevs = state?.skippedDevices ?: [:]
    sDevs?.each { k, v-> if(!res?.containsKey(v?.type)) { res[v?.type] = v } }
    return res
}

private createMetricsDataJson(rendAsMap=false) {
    try {
        generateGuid()
        Map swVer = state?.codeVersions
        Map deviceUsageMap = [:]
        Map deviceErrorMap = [:]
        (isST() ? app?.getChildDevices(true) : getChildDevices())?.each { d->
            Map obj = d?.getDeviceMetrics()
            if(obj?.usage?.size()) { obj?.usage?.each { k,v-> deviceUsageMap[k as String] = (deviceUsageMap[k as String] ? deviceUsageMap[k as String] + v : v) } }
            if(obj?.errors?.size()) { obj?.errors?.each { k,v-> deviceErrorMap[k as String] = (deviceErrorMap[k as String] ? deviceErrorMap[k as String] + v : v) } }
        }
        def dataObj = [
            guid: state?.appGuid,
            datetime: getDtNow()?.toString(),
            installDt: state?.installData?.dt,
            updatedDt: state?.installData?.updatedDt,
            timeZone: location?.timeZone?.ID?.toString(),
            hubPlatform: getPlatform(),
            authValid: (state?.authValid == true),
            stateUsage: "${stateSizePerc()}%",
            amazonDomain: settings?.amazonDomain,
            serverPlatform: state?.onHeroku ? "Cloud" : "Local",
            versions: [app: appVersion(), server: swVer?.server ?: "N/A", device: swVer?.echoDevice ?: "N/A"],
            detections: [skippedDevices: getSkippedDevsAnon()],
            counts: [
                deviceStyleCnts: state?.deviceStyleCnts ?: [:],
                appHeartbeatCnt: state?.appHeartbeatCnt ?: 0,
                getCookieCnt: state?.getCookieCnt ?: 0,
                appErrorCnt: state?.appErrorCnt ?: 0,
                deviceErrors: deviceErrorMap ?: [:],
                deviceUsage: deviceUsageMap ?: [:]
            ]
        ]
        def json = new groovy.json.JsonOutput().toJson(dataObj)
        return json
    } catch (ex) {
        log.error "createMetricsDataJson: Exception:", ex
    }
}

private incrementCntByKey(String key) {
    long evtCnt = state?."${key}" ?: 0
    // evtCnt = evtCnt?.toLong()+1
    evtCnt++
    logger("trace", "${key?.toString()?.capitalize()}: $evtCnt", true)
    state?."${key}" = evtCnt?.toLong()
}

/******************************************
|    APP/DEVICE Version Functions
*******************************************/
Boolean isCodeUpdateAvailable(String newVer, String curVer, String type) {
    Boolean result = false
    def latestVer
    if(newVer && curVer) {
        List versions = [newVer, curVer]
        if(newVer != curVer) {
            latestVer = versions?.max { a, b ->
                List verA = a?.tokenize('.')
                List verB = b?.tokenize('.')
                Integer commonIndices = Math.min(verA?.size(), verB?.size())
                for (int i = 0; i < commonIndices; ++i) {
                    //log.debug "comparing $numA and $numB"
                    if(verA[i]?.toInteger() != verB[i]?.toInteger()) {
                        return verA[i]?.toInteger() <=> verB[i]?.toInteger()
                    }
                }
                verA?.size() <=> verB?.size()
            }
            result = (latestVer == newVer) ? true : false
        }
    }
    // logger("trace", "isCodeUpdateAvailable | type: $type | newVer: $newVer | curVer: $curVer | newestVersion: ${latestVer} | result: $result")
    return result
}

Boolean isAppUpdateAvail() {
    if(state?.appData?.versions && state?.codeVersions?.mainApp && isCodeUpdateAvailable(state?.appData?.versions?.mainApp?.ver, state?.codeVersions?.mainApp, "main_app")) { return true }
    return false
}

Boolean isActionAppUpdateAvail() {
    if(state?.appData?.versions && state?.codeVersions?.actionApp && isCodeUpdateAvailable(state?.appData?.versions?.actionApp?.ver, state?.codeVersions?.actionApp, "action_app")) { return true }
    return false
}

Boolean isGroupAppUpdateAvail() {
    if(state?.appData?.versions && state?.codeVersions?.groupApp && isCodeUpdateAvailable(state?.appData?.versions?.groupApp?.ver, state?.codeVersions?.groupApp, "group_app")) { return true }
    return false
}

Boolean isEchoDevUpdateAvail() {
    if(state?.appData?.versions && state?.codeVersions?.echoDevice && isCodeUpdateAvailable(state?.appData?.versions?.echoDevice?.ver, state?.codeVersions?.echoDevice, "dev")) { return true }
    return false
}

Boolean isServerUpdateAvail() {
    if(state?.appData?.versions && state?.codeVersions?.server && isCodeUpdateAvailable(state?.appData?.versions?.server?.ver, state?.codeVersions?.server, "server")) { return true }
    return false
}

Integer versionStr2Int(str) { return str ? str.toString()?.replaceAll("\\.", "")?.toInteger() : null }

private checkVersionData(now = false) { //This reads a JSON file from GitHub with version numbers
    if (now || !state?.appData || (getLastVerUpdSec() > (3600*6))) {
        if(now && (getLastVerUpdSec() < 300)) { return }
        getConfigData()
    }
}

private getConfigData() {
    def params = [
        uri: "https://raw.githubusercontent.com/tonesto7/echo-speaks/${isBeta() ? "beta" : "master"}/resources/appData.json",
        contentType: "application/json"
    ]
    def data = getWebData(params, "appData", false)
    if(data) {

        state?.appData = data
        state?.lastVerUpdDt = getDtNow()
        log.info "Successfully Retrieved (v${data?.appDataVer}) of AppData Content from GitHub Repo..."
    }
}

private getWebData(params, desc, text=true) {
    try {
        // log.trace("getWebData: ${desc} data")
        httpGet(params) { resp ->
            if(resp?.data) {
                if(text) { return resp?.data?.text.toString() }
                return resp?.data
            }
        }
    } catch (ex) {
        incrementCntByKey("appErrorCnt")
        if(ex instanceof groovyx.net.http.HttpResponseException) {
            log.warn("${desc} file not found")
        } else { log.error "getWebData(params: $params, desc: $desc, text: $text) Exception:", ex }
        return "${label} info not found"
    }
}

/******************************************
|    Time and Date Conversion Functions
*******************************************/
def formatDt(dt, tzChg=true) {
    def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
    if(tzChg) { if(location.timeZone) { tf.setTimeZone(location?.timeZone) } }
    return tf?.format(dt)
}

String strCapitalize(str) { return str ? str?.toString().capitalize() : null }
String isPluralString(obj) { return (obj?.size() > 1) ? "(s)" : "" }

def parseDt(pFormat, dt, tzFmt=true) {
    def result
    def newDt = Date.parse("$pFormat", dt)
    result = formatDt(newDt, tzFmt)
    //log.debug "parseDt Result: $result"
    return result
}

def getDtNow() {
    def now = new Date()
    return formatDt(now)
}

def epochToTime(tm) {
    def tf = new SimpleDateFormat("h:mm a")
    if(location?.timeZone) { tf?.setTimeZone(location?.timeZone) }
    return tf.format(tm)
}

def time2Str(time) {
    if(time) {
        def t = timeToday(time, location?.timeZone)
        def f = new java.text.SimpleDateFormat("h:mm a")
        f?.setTimeZone(location?.timeZone ?: timeZone(time))
        return f?.format(t)
    }
}

def GetTimeDiffSeconds(lastDate, sender=null) {
    try {
        if(lastDate?.contains("dtNow")) { return 10000 }
        def now = new Date()
        def lastDt = Date.parse("E MMM dd HH:mm:ss z yyyy", lastDate)
        def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(lastDt)).getTime()
        def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(now)).getTime()
        def diff = (int) (long) (stop - start) / 1000
        return diff
    }
    catch (ex) {
        log.error "GetTimeDiffSeconds Exception: (${sender ? "$sender | " : ""}lastDate: $lastDate):", ex
        return 10000
    }
}

/******************************************
|   App Input Description Functions
*******************************************/
String getAppNotifConfDesc() {
    String str = ""
    if(pushStatus()) {
        def ap = getAppNotifDesc()
        def nd = getNotifSchedDesc()
        str += (settings?.usePush) ? "${str != "" ? "\n" : ""}Sending via: (Push)" : ""
        str += (settings?.pushoverEnabled) ? "${str != "" ? "\n" : ""}Pushover: (Enabled)" : ""
        str += (settings?.pushoverEnabled && settings?.pushoverPriority) ? bulletItem(str, "Priority: (${settings?.pushoverPriority})") : ""
        str += (settings?.pushoverEnabled && settings?.pushoverSound) ? bulletItem(str, "Sound: (${settings?.pushoverSound})") : ""
        str += (settings?.phone) ? "${str != "" ? "\n" : ""}Sending via: (SMS)" : ""
        str += (ap) ? "${str != "" ? "\n\n" : ""}Enabled Alerts:\n${ap}" : ""
        str += (ap && nd) ? "${str != "" ? "\n" : ""}\nAlert Restrictions:\n${nd}" : ""
    }
    return str != "" ? str : null
}

String getNotifSchedDesc() {
    def sun = getSunriseAndSunset()
    def startInput = settings?.qStartInput
    def startTime = settings?.qStartTime
    def stopInput = settings?.qStopInput
    def stopTime = settings?.qStopTime
    def dayInput = settings?.quietDays
    def modeInput = settings?.quietModes
    def notifDesc = ""
    def getNotifTimeStartLbl = ( (startInput == "Sunrise" || startInput == "Sunset") ? ( (startInput == "Sunset") ? epochToTime(sun?.sunset?.time) : epochToTime(sun?.sunrise?.time) ) : (startTime ? time2Str(startTime) : "") )
    def getNotifTimeStopLbl = ( (stopInput == "Sunrise" || stopInput == "Sunset") ? ( (stopInput == "Sunset") ? epochToTime(sun?.sunset?.time) : epochToTime(sun?.sunrise?.time) ) : (stopTime ? time2Str(stopTime) : "") )
    notifDesc += (getNotifTimeStartLbl && getNotifTimeStopLbl) ? " • Silent Time: ${getNotifTimeStartLbl} - ${getNotifTimeStopLbl}" : ""
    def days = getInputToStringDesc(dayInput)
    def modes = getInputToStringDesc(modeInput)
    notifDesc += days ? "${(getNotifTimeStartLbl || getNotifTimeStopLbl) ? "\n" : ""} • Silent Day${isPluralString(dayInput)}: ${days}" : ""
    notifDesc += modes ? "${(getNotifTimeStartLbl || getNotifTimeStopLbl || days) ? "\n" : ""} • Silent Mode${isPluralString(modeInput)}: ${modes}" : ""
    return (notifDesc != "") ? "${notifDesc}" : null
}

String getServiceConfDesc() {
    String str = ""
    str += (state?.generatedHerokuName && state?.onHeroku) ? bulletItem(str, "Heroku: (Configured)") : ""
    str += (state?.serviceConfigured && state?.isLocal) ? bulletItem(str, "Local Server: (Configured)") : ""
    str += (settings?.amazonDomain) ? bulletItem(str, "Domain: (${settings?.amazonDomain})") : ""
    str += (state?.lastCookieRefresh) ? bulletItem(str, "Cookie Date: (${state?.lastCookieRefresh})") : ""
    return str != "" ? str : null
}

String getAppNotifDesc() {
    def str = ""
    str += settings?.sendMissedPollMsg != false ? bulletItem(str, "Missed Poll Alerts") : ""
    str += settings?.sendAppUpdateMsg != false ? bulletItem(str, "Code Updates") : ""
    str += settings?.sendCookieRefreshMsg == true ? bulletItem(str, "Cookie Refresh") : ""
    return str != "" ? str : null
}

String getGroupsDesc() {
    def grps = getGroupApps()
    return grps?.size() ? " • (${grps?.size()}) Groups Configured" : null
}

String getActionsDesc() {
    def acts = getActionApps()
    return acts?.size() ? " • (${acts?.size()}) Actions Configured" : null
}

String getServInfoDesc() {
    Map rData = state?.nodeServiceInfo
    String str = ""
    String dtstr = ""
    if(rData?.startupDt) {
        def dt = rData?.startupDt
        dtstr += dt?.y ? "${dt?.y}yr${dt?.y > 1 ? "s" : ""}, " : ""
        dtstr += dt?.mn ? "${dt?.mn}mon${dt?.mn > 1 ? "s" : ""}, " : ""
        dtstr += dt?.d ? "${dt?.d}day${dt?.d > 1 ? "s" : ""}, " : ""
        dtstr += dt?.h ? "${dt?.h}hr${dt?.h > 1 ? "s" : ""} " : ""
        dtstr += dt?.m ? "${dt?.m}min${dt?.m > 1 ? "s" : ""} " : ""
        dtstr += dt?.s ? "${dt?.s}sec" : ""
    }
    if(state?.onHeroku) {
        str += " ├ App Name: (${state?.generatedHerokuName})\n"
    }
    str += " ├ IP: (${rData?.ip})"
    str += "\n ├ Port: (${rData?.port})"
    str += "\n ├ Version: (v${rData?.version})"
    str += "\n ${dtstr != "" ? "├" : "└"} Session Events: (${rData?.sessionEvts})"
    str += dtstr != "" ? "\n └ Uptime: ${dtstr.length() > 20 ? "\n     └ ${dtstr}" : "${dtstr}"}" : ""
    return str != "" ? str : null
}

String getInputToStringDesc(inpt, addSpace = null) {
    Integer cnt = 0
    String str = ""
    if(inpt) {
        inpt.sort().each { item ->
            cnt = cnt+1
            str += item ? (((cnt < 1) || (inpt?.size() > 1)) ? "\n      ${item}" : "${addSpace ? "      " : ""}${item}") : ""
        }
    }
    //log.debug "str: $str"
    return (str != "") ? "${str}" : null
}

String randomString(Integer len) {
    def pool = ["a".."z",0..9].flatten()
    Random rand = new Random(new Date().getTime())
    def randChars = (0..len).collect { pool[rand.nextInt(pool.size())] }
    log.debug "randomString: ${randChars?.join()}"
    return randChars.join()
}

def getAccessToken() {
    try {
        if(!state?.accessToken) { state?.accessToken = createAccessToken() }
        else { return true }
    }
    catch (ex) {
        // sendPush("Error: OAuth is not Enabled for ${appName()}!. Please click remove and Enable Oauth under the SmartApp App Settings in the IDE")
        log.error "getAccessToken Exception", ex
        return false
    }
}

def renderConfig() {
    String title = "Echo Speaks"
    Boolean heroku = (isST() || (settings?.useHeroku == null || settings?.useHeroku != false))
    String oStr = !heroku ? """<div id="localServerDiv" class="w-100 mb-3">
                    <div class="my-2 text-left">
                        <p>Due to the complexity of node environments I will not be able to support local server setup</p>
                        <h5>1. Install the node server</h5>
                        <h5>2. Start the node server</h5>
                        <h5>3. Open the servers web config page</h5>
                        <h5>4. Copy the following URL and use it in the appCallbackUrl field of the Server Web Config Page</h5>
                    </div>
                    <div class="all-copy nameContainer mx-0 mb-2 p-1">
                        <p id="copyCallback" class="m-0 p-0">${getAppEndpointUrl("receiveData") as String}</p>
                    </div>
                </div>""" : """
        <div id="cloudServerDiv" class="w-100 mb-3">
            <div class="my-2 text-center">
                <h5>1. Copy the following Name and use it when asked by Heroku</h5>
                <div class="all-copy nameContainer mx-5 mb-2 p-1">
                    <p id="copyHeroku" class="m-0 p-0">${getRandAppName()?.toString().trim()}</p>
                </div>
            </div>
            <div class="my-2 text-center">
                <h5>2. Tap Button to deploy to Heroku</h5>
                <a href="https://heroku.com/deploy?template=https://github.com/tonesto7/echo-speaks-server/tree/${isBeta() ? "dev" : "master"}${getEnvParamsStr()}">
                    <img src="https://www.herokucdn.com/deploy/button.svg" alt="Deploy">
                </a>
            </div>
        </div>"""

    String html = """<head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <meta name="description" content="${title}">
        <meta name="author" content="Anthony S.">
        <meta http-equiv="cleartype" content="on">
        <meta name="MobileOptimized" content="320">
        <meta name="HandheldFriendly" content="True">
        <meta name="apple-mobile-web-app-capable" content="yes">
        <title>${title}</title>
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.1.3/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.5.13/css/mdb.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css" rel="stylesheet">
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.4/umd/popper.min.js"></script>
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.1.3/js/bootstrap.min.js"></script>
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/js/toastr.min.js"></script>
        <style>
            .btn-rounded {
                border-radius: 50px!important;
            }
            span img {
                width: 48px;
                height: auto;
            }
            span p {
                display: block;
            }
            .all-copy p {
                -webkit-user-select: all;
                -moz-user-select: all;
                -ms-user-select: all;
                user-select: all;
            }
            .nameContainer {
                border-radius: 18px;
                color: rgba(255,255,255,1);
                font-size: 1.5rem;
                background: #666;
                -webkit-box-shadow: 1px 1px 1px 0 rgba(0,0,0,0.3) ;
                box-shadow: 1px 1px 1px 0 rgba(0,0,0,0.3) ;
                text-shadow: 1px 1px 1px rgba(0,0,0,0.2) ;
            }
        </style>
    <head>
    <body>
        <div style="margin: 0 auto; max-width: 600px;">
            <form class="p-1">
                <div class="my-3 text-center">
                    <span>
                        <img src="${getAppImg("echo_speaks.1x", true)}"/>
                        <p class="h4 text-center">Echo Speaks</p>
                    </span>
                </div>
                <hr>
                ${oStr}

            </form>
        </div>
    </body>
    <script>
        \$("#copyHeroku").on("click", function () {
            console.log("copyHerokuName Click...")
            \$(this).select();
        });
        \$("#copyCallback").on("click", function () {
            console.log("copyCallback Click...")
            \$(this).select();
        });
    </script>
    """
    render contentType: "text/html", data: html
}

String getObjType(obj) {
	if(obj instanceof String) {return "String"}
	else if(obj instanceof GString) {return "GString"}
	else if(obj instanceof Map) {return "Map"}
	else if(obj instanceof List) {return "List"}
	else if(obj instanceof ArrayList) {return "ArrayList"}
	else if(obj instanceof Integer) {return "Integer"}
	else if(obj instanceof BigInteger) {return "BigInteger"}
	else if(obj instanceof Long) {return "Long"}
	else if(obj instanceof Boolean) {return "Boolean"}
	else if(obj instanceof BigDecimal) {return "BigDecimal"}
	else if(obj instanceof Float) {return "Float"}
	else if(obj instanceof Byte) {return "Byte"}
	else { return "unknown"}
}

private Map amazonDomainOpts() {
    return [
        "amazon.com":"Amazon.com",
        "amazon.ca":"Amazon.ca",
        "amazon.co.uk":"amazon.co.uk",
        "amazon.com.au":"amazon.com.au",
        "amazon.de":"Amazon.de",
        "amazon.it":"Amazon.it"
    ]
}
private List localeOpts() { return ["en-US", "en-CA", "de-DE", "en-GB", "it-IT", "en-AU"] }

private getPlatform() {
    def p = "SmartThings"
    if(state?.hubPlatform == null) {
        try { [dummy: "dummyVal"]?.encodeAsJson(); } catch (e) { p = "Hubitat" }
        // p = (location?.hubs[0]?.id?.toString()?.length() > 5) ? "SmartThings" : "Hubitat"
        state?.hubPlatform = p
        log.debug "hubPlatform: (${state?.hubPlatform})"
    }
    return state?.hubPlatform
}

Integer stateSize() {
    def j = new groovy.json.JsonOutput().toJson(state)
    return j?.toString().length()
}
Integer stateSizePerc() { return (int) ((stateSize() / 100000)*100).toDouble().round(0) }
String debugStatus() { return !settings?.appDebug ? "Off" : "On" }
String deviceDebugStatus() { return !settings?.childDebug ? "Off" : "On" }
Boolean isAppDebug() { return (settings?.appDebug == true) }
Boolean isChildDebug() { return (settings?.childDebug == true) }

String getAppDebugDesc() {
    def str = ""
    str += isAppDebug() ? "App Debug: (${debugStatus()})" : ""
    str += isChildDebug() && str != "" ? "\n" : ""
    str += isChildDebug() ? "Device Debug: (${deviceDebugStatus()})" : ""
    return (str != "") ? "${str}" : null
}

private logger(type, msg, traceOnly=false) {
    if (traceOnly && !settings?.appTrace) { return }
    if(type && msg && settings?.appDebug) {
        log."${type}" "${msg}"
    }
}