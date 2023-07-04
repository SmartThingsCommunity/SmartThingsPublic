/**
 *  Echo Speaks SmartApp
 *
 *  Copyright 2018, 2019, 2020 Anthony Santilli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

String appVersion()   { return "3.6.2.0" }
String appModified()  { return "2020-04-22" }
String appAuthor()    { return "Anthony S." }
Boolean isBeta()      { return false }
Boolean isST()        { return (getPlatform() == "SmartThings") }
Map minVersions()     { return [echoDevice: 3620, wsDevice: 3300, actionApp: 3620, zoneApp: 3620, server: 240] } //These values define the minimum versions of code this app will work with.

definition(
    name        : "Echo Speaks",
    namespace   : "tonesto7",
    author      : "Anthony Santilli",
    description : "Integrate your Amazon Echo devices into your Smart Home environment to create virtual Echo Devices. This allows you to speak text, make announcements, control media playback including volume, and many other Alexa features.",
    category    : "My Apps",
    iconUrl     : "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_speaks_3.1x${state?.updateAvailable ? "_update" : ""}.png",
    iconX2Url   : "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_speaks_3.2x${state?.updateAvailable ? "_update" : ""}.png",
    iconX3Url   : "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/echo_speaks_3.3x${state?.updateAvailable ? "_update" : ""}.png",
    importUrl   : "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/smartapps/tonesto7/echo-speaks.src/echo-speaks.groovy",
    oauth       : true,
    pausable    : true
)

preferences {
    page(name: "startPage")
    page(name: "mainPage")
    page(name: "settingsPage")
    page(name: "devicePrefsPage")
    page(name: "deviceManagePage")
    page(name: "devCleanupPage")
    page(name: "newSetupPage")
    page(name: "authStatusPage")
    page(name: "actionsPage")
    page(name: "zonesPage")
    page(name: "devicePage")
    page(name: "deviceListPage")
    page(name: "unrecogDevicesPage")
    page(name: "changeLogPage")
    page(name: "notifPrefPage")
    page(name: "alexaGuardPage")
    page(name: "alexaGuardAutoPage")
    page(name: "servPrefPage")
    page(name: "musicSearchTestPage")
    page(name: "searchTuneInResultsPage")
    page(name: "deviceTestPage")
    page(name: "donationPage")
    page(name: "speechPage")
    page(name: "announcePage")
    page(name: "sequencePage")
    page(name: "viewZoneHistory")
    page(name: "viewActionHistory")
    page(name: "setNotificationTimePage")
    page(name: "actionDuplicationPage")
    page(name: "zoneDuplicationPage")
    page(name: "uninstallPage")
}

def startPage() {
    state?.isParent = true
    checkVersionData(true)
    state?.childInstallOkFlag = false
    if(!state?.resumeConfig && state?.isInstalled) { stateMigrationChk(); checkGuardSupport(); }
    if(state?.resumeConfig || (state?.isInstalled && !state?.serviceConfigured)) { return servPrefPage() }
    else if(showChgLogOk()) { return changeLogPage() }
    else if(showDonationOk()) { return donationPage() }
    else { return mainPage() }
}

def mainPage() {
    Boolean tokenOk = getAccessToken()
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
            section(sTS("Alexa Guard:")) {
                if(state?.alexaGuardSupported == true) {
                    String gState = state?.alexaGuardState ? (state?.alexaGuardState =="ARMED_AWAY" ? "Away" : "Home") : "Unknown"
                    String gStateIcon = gState == "Unknown" ? "alarm_disarm" : (gState == "Away" ? "alarm_away" : "alarm_home")
                    href "alexaGuardPage", title: inTS("Alexa Guard Control", getAppImg(gStateIcon, true)), image: getAppImg(gStateIcon), state: guardAutoConfigured() ? "complete" : null,
                            description: "Current Status: ${gState}${guardAutoConfigured() ? "\nAutomation: Enabled" : ""}\n\nTap to proceed..."
                } else if (state?.guardDataOverMaxSize == true) {
                    paragraph pTS("Because you have a lot of devices attached to your Alexa account the response size is larger than ST allows.  The request is being made using your server... On next page load you should see the status.\n\nPlease make sure you are running server version 2.3 or higher.", null, false, "gray")
                } else { paragraph pTS("Alexa Guard is not enabled or supported by any of your Echo Devices", null, false, "gray") }
            }

            section(sTS("Alexa Devices:")) {
                if(!newInstall) {
                    List devs = getDeviceList()?.collect { "${it?.value?.name}${it?.value?.online ? " (Online)" : ""}${it?.value?.supported == false ? " \u2639" : ""}" }
                    Map skDevs = state?.skippedDevices?.findAll { (it?.value?.reason != "In Ignore Device Input") }
                    Map ignDevs = state?.skippedDevices?.findAll { (it?.value?.reason == "In Ignore Device Input") }
                    List remDevs = getRemovableDevs()
                    if(remDevs?.size()) {
                        href "devCleanupPage", title: inTS("Removable Devices:"), description: "${remDevs?.sort()?.join("\n")}", required: true, state: null
                    }
                    href "deviceManagePage", title: inTS("Manage Devices:", getAppImg("devices", true)), description: "(${devs?.size()}) Installed\n\nTap to manage...", state: "complete", image: getAppImg("devices")
                } else { paragraph "Device Management will be displayed after install is complete" }
            }

            section(sTS("Companion Apps:")) {
                def zones = getZoneApps()
                href "zonesPage", title: inTS("Manage Zones${zones?.size() ? " (${zones?.size()} ${zones?.size() > 1 ? "Zones" : "Zone"})" : ""}", getAppImg("es_groups", true)), description: getZoneDesc(), state: (zones?.size() ? "complete" : null), image: getAppImg("es_groups")
                href "actionsPage", title: inTS("Manage Actions", getAppImg("es_actions", true)), description: getActionsDesc(), state: (getActionApps()?.size() ? "complete" : null), image: getAppImg("es_actions")
            }

            section(sTS("Alexa Login Service:")) {
                def ls = getLoginStatusDesc()
                href "authStatusPage", title: inTS("Login Status | Service Management", getAppImg("settings", true)), description: (ls ? "${ls}\n\nTap to modify" : "Tap to configure"), state: (ls ? "complete" : null), image: getAppImg("settings")
            }
            if(!state?.shownDevSharePage) { showDevSharePrefs() }
            section(sTS("Notifications:")) {
                def t0 = getAppNotifConfDesc()
                href "notifPrefPage", title: inTS("Manage Notifications", getAppImg("notification2", true)), description: (t0 ? "${t0}\n\nTap to modify" : "Tap to configure"), state: (t0 ? "complete" : null), image: getAppImg("notification2")
            }

            section(sTS("Experimental Functions:")) {
                href "deviceTestPage", title: inTS("Device Testing", getAppImg("testing", true)), description: "Test Speech, Announcements, and Sequences Builder\n\nTap to proceed...", image: getAppImg("testing")
                href "musicSearchTestPage", title: inTS("Music Search Tests", getAppImg("music", true)), description: "Test music queries\n\nTap to proceed...", image: getAppImg("music")
            }
        }
        section(sTS("Documentation & Settings:")) {
            href url: documentationLink(), style: "external", required: false, title: inTS("View Documentation", getAppImg("documentation", true)), description: "Tap to proceed", image: getAppImg("documentation")
            href "settingsPage", title: inTS("Manage Logging, and Metrics", getAppImg("settings", true)), description: "Tap to modify...", image: getAppImg("settings")
        }
        if(!newInstall) {
            section(sTS("Donations:")) {
                href url: textDonateLink(), style: "external", required: false, title: inTS("Donations", getAppImg("donate", true)), description: "Tap to open browser", image: getAppImg("donate")
            }
            section(sTS("Remove Everything:")) {
                href "uninstallPage", title: inTS("Uninstall this App", getAppImg("uninstall", true)), description: "Tap to Remove...", image: getAppImg("uninstall")
            }
            section(sTS("Feature Requests/Issue Reporting"), hideable: true, hidden: true) {
                def issueUrl = "https://github.com/tonesto7/echo-speaks/issues/new?assignees=tonesto7&labels=bug&template=bug_report.md&title=%28BUG%29+&projects=echo-speaks%2F6"
                def featUrl = "https://github.com/tonesto7/echo-speaks/issues/new?assignees=tonesto7&labels=enhancement&template=feature_request.md&title=%5BFeature+Request%5D&projects=echo-speaks%2F6"
                href url: featUrl, style: "external", required: false, title: inTS("New Feature Request", getAppImg("www", true)), description: "Tap to open browser", image: getAppImg("www")
                href url: issueUrl, style: "external", required: false, title: inTS("Report an Issue", getAppImg("www", true)), description: "Tap to open browser", image: getAppImg("www")
            }
        } else {
            showDevSharePrefs()
            section(sTS("Important Step:")) {
                paragraph title: "Notice:", pTS("Please complete the install and return to the Echo Speaks App to resume deployment and configuration of the server.", null, true, "red"), required: true, state: null
                state?.resumeConfig = true
            }
        }
        state.ok2InstallActionFlag = false
        clearDuplicationItems()
    }
}

def authStatusPage() {
    Boolean newInstall = (state?.isInstalled != true)
    Boolean resumeConf = (state?.resumeConfig == true)
    return dynamicPage(name: "authStatusPage", install: false, nextPage: "mainPage", uninstall: false) {
        if(state?.authValid) {
            Integer lastChkSec = getLastTsValSecs("lastCookieRrshDt")
            Boolean pastDayChkOk = (lastChkSec > 86400)
            section(sTS("Cookie Status:")) {
                Boolean cookieValid = (validateCookie(true) == true)
                Boolean chk1 = (state?.cookieData && state?.cookieData?.localCookie)
                Boolean chk2 = (state?.cookieData && state?.cookieData?.csrf)
                Boolean chk3 = (lastChkSec < 432000)
                Boolean chk4 = (cookieValid == true)
                String nextRfsh = nextCookieRefreshDur()
                // log.debug "cookieValid: ${cookieValid} | chk1: $chk1 | chk2: $chl2 | chk3: $chk3 | chk4: $chk4"
                String stat = "Auth Status: (${(chk1 && chk2) ? "OK": "Invalid"})"
                stat += "\n \u2022 Cookie: (${chk1 ? okSym() : notOkSym()})"
                stat += "\n \u2022 CSRF Value: (${chk2 ? okSym() : notOkSym()})"
                paragraph pTS(stat, null, false, (chk1 && chk2) ? "#2784D9" : "red"), state: ((chk1 && chk2) ? "complete" : null), required: true
                paragraph pTS("Last Refresh: (${chk3 ? "OK" : "Issue"})\n(${seconds2Duration(getLastTsValSecs("lastCookieRrshDt"))})", null, false, chk3 ? "#2784D9" : "red"), state: (chk3 ? "complete" : null), required: true
                paragraph pTS("Next Refresh:\n(${nextCookieRefreshDur()})", null, false, "#2784D9"), state: "complete", required: true
            }

            section(sTS("Cookie Tools: (Tap to show)"), hideable: true, hidden: true) {
                String ckDesc = pastDayChkOk ? "This will Refresh your Amazon Cookie." : "It's too soon to refresh your cookie.\nMinimum wait is 24 hours!!"
                input "refreshCookieDays", "number", title: inTS("Auto refresh cookie every?\n(in days)", getAppImg("day_calendar", true)), description: "in Days (1-5 max)", required: true, defaultValue: 5, submitOnChange: true, image: getAppImg("day_calendar")
                if(refreshCookieDays != null && refreshCookieDays < 1) { settingUpdate("refreshCookieDays", 1, "number") }
                if(refreshCookieDays != null && refreshCookieDays > 5) { settingUpdate("refreshCookieDays", 5, "number") }
                if(!isST()) { paragraph pTS("in Days (1-5 max)", null, false, "gray") }
                // Refreshes the cookie
                input "refreshCookie", "bool", title: inTS("Manually refresh cookie?", getAppImg("reset", true)), description: ckDesc, required: true, defaultValue: false, submitOnChange: true, image: getAppImg("reset"), state: (pastDayChkOk ? "" : null)
                if(!isST()) { paragraph pTS(ckDesc, null, false, pastDayChkOk ? null : "red") }
                paragraph pTS("Notice:\nAfter manually refreshing the cookie leave this page and come back before the date will change.", null, false, "#2784D9"), state: "complete"
                // Clears cookies for app and devices
                input "resetCookies", "bool", title: inTS("Remove All Cookie Data?", getAppImg("reset", true)), description: "Clear all stored cookie data from the app and devices.", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("reset")
                if(!isST()) { paragraph pTS("Clear all stored cookie data from the app and devices.", null, false, "gray") }
                input "refreshDevCookies", "bool", title: inTS("Resend Cookies to Devices?", getAppImg("reset", true)), description: "Force devices to synchronize their stored cookies.", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("reset")
                if(!isST()) { paragraph pTS("Force devices to synchronize their stored cookies.", null, false, "gray") }
                if(settings?.refreshCookie) { settingUpdate("refreshCookie", "false", "bool"); runIn(2, "runCookieRefresh"); }
                if(settings?.resetCookies) { clearCookieData("resetCookieToggle") }
                if(settings?.refreshDevCookies) { refreshDevCookies() }
            }
        }

        section(sTS("Service Management")) {
            def t0 = getServiceConfDesc()
            href "servPrefPage", title: inTS("Manage Login Service", getAppImg("settings", true)), description: (t0 ? "${t0}\n\nTap to modify" : "Tap to configure"), state: (t0 ? "complete" : null), image: getAppImg("settings")
        }
    }
}

def servPrefPage() {
    Boolean newInstall = (state?.isInstalled != true)
    Boolean resumeConf = (state?.resumeConfig == true)
    return dynamicPage(name: "servPrefPage", install: (newInstall || resumeConf), nextPage: (!(newInstall || resumeConf) ? "mainPage" : ""), uninstall: (state?.serviceConfigured != true)) {
        Boolean hasChild = ((isST() ? app?.getChildDevices(true) : getChildDevices())?.size())
        Boolean onHeroku = (isST() || settings?.useHeroku != false)
        Boolean authValid = (state?.authValid == true)

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
            section() { paragraph pTS("To proceed with the server setup.\nTap on 'Begin Server Setup' below", null, true, "#2784D9"), state: "complete" }
            srvcPrefOpts(true)
            section(sTS("Deploy the Server:")) {
                href (url: getAppEndpointUrl("config"), style: "external", title: inTS("Begin Server Setup", getAppImg("upload", true)), description: "Tap to proceed", required: false, state: "complete", image: getAppImg("upload"))
            }
        } else {
            if(!authValid) {
                section(sTS("Authentication:")) {
                    paragraph pTS("You still need to Login to Amazon to complete the setup", null, true, "red"), required: true, state: null
                    if(getServerItem("onHeroku")) {
                        href url: "https://${getRandAppName()}.herokuapp.com/config", style: "external", required: false, title: inTS("Amazon Login Page", getAppImg("amazon_orange", true)), description: "Tap to proceed", image: getAppImg("amazon_orange")
                    } else if (getServerItem("isLocal")) {
                        href url: "${getServerHostURL()}/config", style: "external", required: false, title: inTS("Amazon Login Page", getAppImg("amazon_orange", true)), description: "Tap to proceed", image: getAppImg("amazon_orange")
                    }
                }
            } else {
                if(getServerItem("onHeroku")) {
                    section(sTS("Server Management:")) {
                        if(state?.herokuName) { paragraph pTS("Heroku Name:\n \u2022 ${state?.herokuName}", null, true, "#2784D9"), state: "complete" }
                        href url: "https://${getRandAppName()}.herokuapp.com/config", style: "external", required: false, title: inTS("Amazon Login Page", getAppImg("amazon_orange", true)), description: "Tap to proceed", image: getAppImg("amazon_orange")
                        href url: "https://dashboard.heroku.com/apps/${getRandAppName()}/settings", style: "external", required: false, title: inTS("Heroku App Settings", getAppImg("heroku", true)), description: "Tap to proceed", image: getAppImg("heroku")
                        href url: "https://dashboard.heroku.com/apps/${getRandAppName()}/logs", style: "external", required: false, title: inTS("Heroku App Logs", getAppImg("heroku", true)), description: "Tap to proceed", image: getAppImg("heroku")
                    }
                }
                if(getServerItem("isLocal")) {
                    section(sTS("Local Server Management:")) {
                        href url: "${getServerHostURL()}/config", style: "external", required: false, title: inTS("Amazon Login Page", getAppImg("amazon_orange", true)), description: "Tap to proceed", image: getAppImg("amazon_orange")
                    }
                }
            }
            srvcPrefOpts()
        }
        section(sTS("Reset Options (Tap to show):"), hideable: true, hidden: true) {
            input "resetService", "bool", title: inTS("Reset Service Data?", getAppImg("reset", true)), description: "This will clear all references to the current server and allow you to redeploy a new instance.\nLeave the page and come back after toggling.",
                required: false, defaultValue: false, submitOnChange: true, image: getAppImg("reset")
            if(!isST()) { paragraph pTS("This will clear all references to the current server and allow you to redeploy a new instance.\nLeave the page and come back after toggling.", null, false, "gray") }
            if(settings?.resetService) { clearCloudConfig() }
        }
        state?.resumeConfig = false
    }
}

def srvcPrefOpts(req=false) {
    section(sTS("${req ? "Required " : ""}Amazon Locale Settings"), hideable: false, hidden: false) {
        if(req) {
            input "amazonDomain", "enum", title: inTS("Select your Amazon Domain?", getAppImg("amazon_orange", true)), description: "", required: true, defaultValue: "amazon.com", options: amazonDomainOpts(), submitOnChange: true, image: getAppImg("amazon_orange")
            input "regionLocale", "enum", title: inTS("Select your Locale?", getAppImg("www", true)), description: "", required: true, defaultValue: "en-US", options: localeOpts(), submitOnChange: true, image: getAppImg("www")
        } else {
            def s = ""
            s += settings?.amazonDomain ? "Amazon Domain: (${settings?.amazonDomain})" : ""
            s += settings?.regionLocale ? "\nLocale Region: (${settings?.regionLocale})" : ""
            paragraph pTS(s, null, false, "#2784D9"), state: "complete", image: getAppImg("amazon_orange")
        }
    }
}

def deviceManagePage() {
    return dynamicPage(name: "deviceManagePage", uninstall: false, install: false) {
        Boolean newInstall = (state?.isInstalled != true)
        section(sTS("Alexa Devices:")) {
            if(!newInstall) {
                List devs = getDeviceList()?.collect { "${it?.value?.name}${it?.value?.online ? " (Online)" : ""}${it?.value?.supported == false ? " \u2639" : ""}" }?.sort()
                Map skDevs = state?.skippedDevices?.findAll { (it?.value?.reason != "In Ignore Device Input") }
                Map ignDevs = state?.skippedDevices?.findAll { (it?.value?.reason == "In Ignore Device Input") }
                if(devs?.size()) {
                    href "deviceListPage", title: inTS("Installed Devices:"), description: "${devs?.join("\n")}\n\nTap to view details...", state: "complete"
                } else { paragraph title: "Discovered Devices:", "No Devices Available", state: "complete" }
                List remDevs = getRemovableDevs()
                if(remDevs?.size()) {
                    href "devCleanupPage", title: inTS("Removable Devices:"), description: "${remDevs?.sort()?.join("\n")}", required: true, state: null
                }
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
    }
}

def alexaGuardPage() {
    return dynamicPage(name: "alexaGuardPage", uninstall: false, install: false) {
        String gState = state?.alexaGuardState ? (state?.alexaGuardState =="ARMED_AWAY" ? "Away" : "Home") : "Unknown"
        String gStateIcon = gState == "Unknown" ? "alarm_disarm" : (gState == "Away" ? "alarm_away" : "alarm_home")
        String gStateTitle = (gState == "Unknown" || gState == "Home") ? "Set Guard to Armed?" : "Set Guard to Home?"
        section(sTS("Alexa Guard Control")) {
            input "alexaGuardAwayToggle", "bool", title: inTS(gStateTitle, getAppImg(gStateIcon, true)), description: "Current Status: ${gState}", defaultValue: false, submitOnChange: true, image: getAppImg(gStateIcon)
        }
        if(settings?.alexaGuardAwayToggle != state?.alexaGuardAwayToggle) {
            setGuardState(settings?.alexaGuardAwayToggle == true ? "ARMED_AWAY" : "ARMED_STAY")
        }
        state?.alexaGuardAwayToggle = settings?.alexaGuardAwayToggle
        section(sTS("Automate Guard Control")) {
            href "alexaGuardAutoPage", title: inTS("Automate Guard Changes", getAppImg("alarm_disarm", true)), description: guardAutoDesc(), image: getAppImg("alarm_disarm"), state: (guardAutoDesc() =="Tap to configure..." ? null : "complete")
        }
    }
}

def alexaGuardAutoPage() {
    return dynamicPage(name: "alexaGuardAutoPage", uninstall: false, install: false) {
        String asn = getAlarmSystemName(true)
        List amo = getAlarmModes()
        Boolean alarmReq = (settings?.guardAwayAlarm || settings?.guardHomeAlarm)
        Boolean modeReq = (settings?.guardAwayModes || settings?.guardHomeModes)
        // Boolean swReq = (settings?.guardAwaySw || settings?.guardHomeSw)
        section(sTS("Set Guard Using ${asn}")) {
            input "guardHomeAlarm", "enum", title: inTS("Home in ${asn} modes.", getAppImg("alarm_home", true)), description: "Tap to select...", options: amo, required: alarmReq, multiple: true, submitOnChange: true, image: getAppImg("alarm_home")
            input "guardAwayAlarm", "enum", title: inTS("Away in ${asn} modes.", getAppImg("alarm_away", true)), description: "Tap to select...", options: amo, required: alarmReq, multiple: true, submitOnChange: true, image: getAppImg("alarm_away")
        }

        section(sTS("Set Guard Using Modes")) {
            input "guardHomeModes", "mode", title: inTS("Home in these Modes?", getPublicImg("mode", true)), description: "Tap to select...", required: modeReq, multiple: true, submitOnChange: true, image: getAppImg("mode")
            input "guardAwayModes", "mode", title: inTS("Away in these Modes?", getPublicImg("mode", true)), description: "Tap to select...", required: modeReq, multiple: true, submitOnChange: true, image: getAppImg("mode")
        }

        section(sTS("Set Guard Using Switches:")) {
            input "guardHomeSwitch", "capability.switch", title: inTS("Home when any of these are On?", getAppImg("switch", true)), description: "Tap to select...", multiple: true, required: false, submitOnChange: true, image: getAppImg("switch")
            input "guardAwaySwitch", "capability.switch", title: inTS("Away when any of these are On?", getAppImg("switch", true)), description: "Tap to select...", multiple: true, required: false, submitOnChange: true, image: getAppImg("switch")
        }

        section(sTS("Set Guard using Presence")) {
            input "guardAwayPresence", "capability.presenceSensor", title: inTS("Away when these devices are All away?", getAppImg("presence", true)), description: "Tap to select...", multiple: true, required: false, submitOnChange: true, image: getAppImg("presence")
        }
        if(guardAutoConfigured()) {
            section(sTS("Delay:")) {
                input "guardAwayDelay", "number", title: inTS("Delay before arming Away?\n(in seconds)", getAppImg("delay_time", true)), description: "Enter number in seconds", required: false, defaultValue: 30, submitOnChange: true, image: getAppImg("delay_time")
            }
        }
        section(sTS("Restrict Guard Changes (Optional):")) {
            input "guardRestrictOnSwitch", "capability.switch", title: inTS("Only when these are On?", getAppImg("switch", true)), description: "Tap to select...", multiple: true, required: false, submitOnChange: true, image: getAppImg("switch")
            input "guardRestrictOffSwitch", "capability.switch", title: inTS("Only when these are Off?", getAppImg("switch", true)), description: "Tap to select...", multiple: true, required: false, submitOnChange: true, image: getAppImg("switch")
        }
    }
}

Boolean guardAutoConfigured() {
    return ((settings?.guardAwayAlarm && settings?.guardHomeAlarm) || (settings?.guardAwayModes && settings?.guardHomeModes) || (settings?.guardAwaySwitch && settings?.guardHomeSwitch) || settings?.guardAwayPresence)
}

String guardAutoDesc() {
    String str = ""
    if(guardAutoConfigured()) {
        str += "Guard Triggers:"
        str += (settings?.guardAwayAlarm && settings?.guardHomeAlarm) ? "\n \u2022 Using ${getAlarmSystemName()}" : ""
        str += settings?.guardHomeModes ? "\n \u2022 Home Modes: (${settings?.guardHomeModes?.size()})" : ""
        str += settings?.guardAwayModes ? "\n \u2022 Away Modes: (${settings?.guardAwayModes?.size()})" : ""
        str += settings?.guardHomeSwitch ? "\n \u2022 Home Switches: (${settings?.guardHomeSwitch?.size()})" : ""
        str += settings?.guardAwaySwitch ? "\n \u2022 Away Switches: (${settings?.guardAwaySwitch?.size()})" : ""
        str += settings?.guardAwayPresence ? "\n \u2022 Presence Home: (${settings?.guardAwayPresence?.size()})" : ""
    }
    return str == "" ? "Tap to configure..." : "${str}\n\nTap to configure..."
}

def guardTriggerEvtHandler(evt) {
    def evtDelay = now() - evt?.date?.getTime()
    logDebug("${evt?.name.toUpperCase()} Event | Device: ${evt?.displayName} | Value: (${strCapitalize(evt?.value)}) with a delay of ${evtDelay}ms")
    if(!guardRestrictOk()) {
        logDebug("guardTriggerEvtHandler | Skipping Guard Changes because Restriction are Active.")
        return
    }
    String newState = null
    String curState = state?.alexaGuardState ?: null
    switch(evt?.name as String) {
        case "mode":
            Boolean inAwayMode = isInMode(settings?.guardAwayModes)
            Boolean inHomeMode = isInMode(settings?.guardHomeModes)
            if(inAwayMode && inHomeMode) { logError("Guard Control Trigger can't act because same mode is in both Home and Away input"); return; }
            if(inAwayMode && !inHomeMode) { newState = "ARMED_AWAY" }
            if(!inAwayMode && inHomeMode) { newState = "ARMED_STAY" }
            break
        case "switch":
            Boolean inAwaySw = isSwitchOn(settings?.guardAwaySwitch)
            Boolean inHomeSw = isSwitchOn(settings?.guardHomeSwitch)
            if(inAwaySw && inHomeSw) { logError("Guard Control Trigger can't act because both switch groups are in both Home and Away input"); return; }
            if(inAwaySw && !inHomeSw) { newState = "ARMED_AWAY" }
            if(!inAwaySw && inHomeSw) { newState = "ARMED_STAY" }
            break
        case "presence":
            newState = isSomebodyHome(settings?.guardAwayPresence) ? "ARMED_STAY" : "ARMED_AWAY"
            break
        case "alarmSystemStatus":
        case "hsmStatus":
            Boolean inAlarmHome = isInAlarmMode(settings?.guardHomeAlarm)
            Boolean inAlarmAway = isInAlarmMode(settings?.guardAwayAlarm)
            if(inAlarmAway && !inAlarmHome) { newState = "ARMED_AWAY" }
            if(!inAlarmAway && inAlarmHome) { newState = "ARMED_STAY" }
            break
    }
    if(curState == newState) { logInfo("Skipping Guard Change... New Guard State is the same as current state: ($curState)") }
    if(newState && curState != newState) {
        if (newState == "ARMED_STAY") {
            unschedule("setGuardAway")
            logInfo("Setting Alexa Guard Mode to Home...")
            setGuardHome()
        }
        if(newState == "ARMED_AWAY") {
            if(settings?.guardAwayDelay) { logWarn("Setting Alexa Guard Mode to Away in (${settings?.guardAwayDelay} seconds)", true); runIn(settings?.guardAwayDelay, "setGuardAway"); }
            else { setGuardAway(); logWarn("Setting Alexa Guard Mode to Away...", true); }
        }
    }
}

Boolean guardRestrictOk() {
    Boolean onSwOk = settings?.guardRestrictOnSwitch ? isSwitchOn(settings?.guardRestrictOnSwitch) : true
    Boolean offSwOk = settings?.guardRestrictOffSwitch ? !isSwitchOn(settings?.guardRestrictOffSwitch) : true
    return (onSwOk && offSwOk)
}

def actionsPage() {
    return dynamicPage(name: "actionsPage", nextPage: "mainPage", uninstall: false, install: false) {
        List actApps = getActionApps()
        List activeActions = actApps?.findAll { it?.isPaused() != true }
        List pausedActions = actApps?.findAll { it?.isPaused() == true }
        if(actApps) { /*Nothing to add here yet*/ }
        else { section("") { paragraph pTS("You haven't created any Actions yet!\nTap Create New Action to get Started") } }
        section() {
            app(name: "actionApp", appName: actChildName(), namespace: "tonesto7", multiple: true, title: inTS("Create New Action", getAppImg("es_actions", true)), image: getAppImg("es_actions"))
            if(actApps?.size() && isST()) {
                input "actionDuplicateSelect", "enum", title: inTS("Duplicate Existing Action", getAppImg("es_actions", true)), description: "Tap to select...", options: actApps?.collectEntries { [(it?.id):it?.getLabel()] }, required: false, multiple: false, submitOnChange: true, image: getAppImg("es_actions")
                if(settings?.actionDuplicateSelect) {
                    href "actionDuplicationPage", title: inTS("Create Duplicate Action?", getAppImg("question", true)), description: "Tap to proceed...", image: getAppImg("question")
                }
            }
        }
        if(actApps?.size()) {
            section (sTS("Action History:")) {
                href "viewActionHistory", title: inTS("View Action History", getAppImg("tasks", true)), description: "(Grouped by Action)", image: getAppImg("tasks"), state: "complete"
            }

            section (sTS("Global Actions Management:"), hideable: true, hidden: true) {
                if(activeActions?.size()) {
                    input "pauseChildActions", "bool", title: inTS("Pause all actions?", getAppImg("pause_orange", true)), description: "When pausing all Actions you can either restore all or open each action and manually unpause it.",
                            defaultValue: false, submitOnChange: true, image: getAppImg("pause_orange")
                    if(settings?.pauseChildActions) { settingUpdate("pauseChildActions", "false", "bool"); runIn(3, "executeActionPause"); }
                    if(!isST()) { paragraph pTS("When pausing all Actions you can either restore all or open each action and manually unpause it.", null, false, "gray") }
                }
                if(pausedActions?.size()) {
                    input "unpauseChildActions", "bool", title: inTS("Restore all actions?", getAppImg("pause_orange", true)), defaultValue: false, submitOnChange: true, image: getAppImg("pause_orange")
                    if(settings?.unpauseChildActions) { settingUpdate("unpauseChildActions", "false", "bool"); runIn(3, "executeActionUnpause"); }
                }
                input "reinitChildActions", "bool", title: inTS("Force Refresh all actions?", getAppImg("reset", true)), defaultValue: false, submitOnChange: true, image: getAppImg("reset")
                if(settings?.reinitChildActions) { settingUpdate("reinitChildActions", "false", "bool"); runIn(3, "executeActionUpdate"); }
            }
        }
        state.childInstallOkFlag = true
        state?.actionDuplicated = false
    }
}

def actionDuplicationPage() {
    return dynamicPage(name: "actionDuplicationPage", nextPage: "actionsPage", uninstall: false, install: false) {
        section() {
            if(state?.actionDuplicated) {
                paragraph pTS("Action already duplicated...\n\nReturn to action page and select it", null, true, "red"), required: true, state: null
            } else {
                def act = getActionApps()?.find { it?.id?.toString() == settings?.actionDuplicateSelect?.toString() }
                if(act) {
                    Map actData = act?.getDuplSettingData()
                    actData?.settings["duplicateFlag"] = [type: "bool", value: true]
                    addChildApp("tonesto7", actChildName(), "${actData?.label} (Dup)", [settings: actData?.settings])
                    paragraph pTS("Action Duplicated...\n\nReturn to Action Page and look for the App with '(Dup)' in the name...", null, true, "#2784D9"), state: "complete"
                } else { paragraph pTS("Action not Found", null, true, "red"), required: true, state: null }
                state?.actionDuplicated = true
            }
        }
    }
}

def zoneDuplicationPage() {
    return dynamicPage(name: "zoneDuplicationPage", nextPage: "zonesPage", uninstall: false, install: false) {
        section() {
            if(state?.zoneDuplicated) {
                paragraph pTS("Zone already duplicated...\n\nReturn to zone page and select it", null, true, "red"), required: true, state: null
            } else {
                def zn = getZoneApps()?.find { it?.id?.toString() == settings?.zoneDuplicateSelect?.toString() }
                if(zn) {
                    Map znData = zn?.getDuplSettingData()
                    znData?.settings["duplicateFlag"] = [type: "bool", value: true]
                    addChildApp("tonesto7", zoneChildName(), "${znData?.label} (Dup)", [settings: znData?.settings])
                    paragraph pTS("Zone Duplicated...\n\nReturn to Zone Page and look for the App with '(Dup)' in the name...", null, true, "#2784D9"), state: "complete"
                } else { paragraph pTS("Zone not Found", null, true, "red"), required: true, state: null }
                state?.zoneDuplicated = true
            }
        }
    }
}

public clearDuplicationItems() {
    state?.actionDuplicated = false
    state?.zoneDuplicated = false
    settingRemove("actionDuplicateSelect")
    settingRemove("zoneDuplicateSelect")
}

public getDupActionStateData() {
    def act = getActionApps()?.find { it?.id == settings?.actionDuplicateSelect }
    return act?.getDuplStateData() ?: null
}
public getDupZoneStateData() {
    def act = getActionApps()?.find { it?.id == settings?.actionDuplicateSelect }
    return act?.getDuplStateData() ?: null
}

def zonesPage() {
    return dynamicPage(name: "zonesPage", nextPage: "mainPage", uninstall: false, install: false) {
        List zApps = getZoneApps()
        List activeZones = zApps?.findAll { it?.isPaused() != true }
        List pausedZones = zApps?.findAll { it?.isPaused() == true }
        if(zApps) { /*Nothing to add here yet*/ }
        else {
            section("") { paragraph pTS("You haven't created any Zones yet!\nTap Create New Zone to get Started") }
        }
        section() {
            app(name: "zoneApp", appName: zoneChildName(), namespace: "tonesto7", multiple: true, title: inTS("Create New Zone", getAppImg("es_groups", true)), image: getAppImg("es_groups"))
            if(zApps?.size() && isST()) {
                input "zoneDuplicateSelect", "enum", title: inTS("Duplicate Existing Zone", getAppImg("es_groups", true)), description: "Tap to select...", options: zApps?.collectEntries { [(it?.id):it?.getLabel()] }, required: false, multiple: false, submitOnChange: true, image: getAppImg("es_groups")
                if(settings?.zoneDuplicateSelect) {
                    href "zoneDuplicationPage", title: inTS("Create Duplicate Zone?", getAppImg("question", true)), description: "Tap to proceed...", image: getAppImg("question")
                }
            }
        }
        if(zApps?.size()) {
            section (sTS("Zone History:")) {
                href "viewZoneHistory", title: inTS("View Zone History", getAppImg("tasks", true)), description: "(Grouped by Zone)", image: getAppImg("tasks"), state: "complete"
            }
        }
        section (sTS("Zone Management:"), hideable: true, hidden: true) {
            if(activeZones?.size()) {
                input "pauseChildZones", "bool", title: inTS("Pause all Zones?", getAppImg("pause_orange", true)), description: "When pausing all Zones you can either restore all or open each zones and manually unpause it.",
                        defaultValue: false, submitOnChange: true, image: getAppImg("pause_orange")
                if(settings?.pauseChildZones) { settingUpdate("pauseChildZones", "false", "bool"); runIn(3, "executeZonePause"); }
                if(!isST()) { paragraph pTS("When pausing all zones you can either restore all or open each zone and manually unpause it.", null, false, "gray") }
            }
            if(pausedZones?.size()) {
                input "unpauseChildZone", "bool", title: inTS("Restore all actions?", getAppImg("pause_orange", true)), defaultValue: false, submitOnChange: true, image: getAppImg("pause_orange")
                if(settings?.unpauseChildZones) { settingUpdate("unpauseChildZones", "false", "bool"); runIn(3, "executeZoneUnpause"); }
            }
            input "reinitChildZones", "bool", title: inTS("Clear Zones Status and force a full status refresh for all zones?", getAppImg("reset", true)), defaultValue: false, submitOnChange: true, image: getAppImg("reset")
            if(settings?.reinitChildZones) { settingUpdate("reinitChildZones", "false", "bool"); runIn(3, "executeZoneUpdate"); }
        }
        state?.childInstallOkFlag = true
        state?.zoneDuplicated = false
        updateZoneSubscriptions()
    }
}

def viewZoneHistory() {
    return dynamicPage(name: "viewZoneHistory", uninstall: false, install: false) {
        List zApps = getZoneApps()
        zApps?.each { z->
            section(z?.getLabel()) {
                Map items = z?.getZoneHistory(true) ?: [:]
                items?.each { k, v->
                    paragraph title: k, pTS(v)
                }
            }
        }
    }
}

def viewActionHistory() {
    return dynamicPage(name: "viewActionHistory", uninstall: false, install: false) {
        List actApps = getActionApps()
        actApps?.each { a->
            section(a?.getLabel()) {
                Map items = a?.getActionHistory(true) ?: [:]
                items?.each { k, v->
                    paragraph title: k, pTS(v)
                }
            }
        }
    }
}

private executeActionPause() {
    getActionApps()?.findAll { it?.isPaused() != true }?.each { it?.updatePauseState(true) }
}
private executeActionUnpause() {
    getActionApps()?.findAll { it?.isPaused() == true }?.each { it?.updatePauseState(false) }
}
private executeActionUpdate() {
    getActionApps()?.each { it?.updated() }
}
private executeZonePause() {
    getZoneApps()?.findAll { it?.isPaused() != true }?.each { it?.updatePauseState(true) }
}
private executeZoneUnpause() {
    getZoneApps()?.findAll { it?.isPaused() == true }?.each { it?.updatePauseState(false) }
}
private executeZoneUpdate() {
    atomicState?.zoneStatusMap = [:]
    getZoneApps()?.each { it?.updated() }
}

def devicePrefsPage() {
    Boolean newInstall = (state?.isInstalled != true)
    Boolean resumeConf = (state?.resumeConfig == true)
    return dynamicPage(name: "devicePrefsPage", uninstall: false, install: false) {
        deviceDetectOpts()
        section(sTS("Detection Override:")) {
            paragraph pTS("Device not detected?  Enabling this will allow you to override the developer block for unrecognized or uncontrollable devices.  This is useful for testing the device.", getAppImg("info", true), false)
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
        input "addEchoNamePrefix", "bool", title: inTS("Add 'Echo - ' Prefix to label?", getAppImg("name_tag")), description: "", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("name_tag")
        Map devs = getAllDevices(true)
        if(devs?.size()) {
            input "echoDeviceFilter", "enum", title: inTS("Don't Use these Devices", getAppImg("exclude", true)), description: "Tap to select", options: (devs ? devs?.sort{it?.value} : []), multiple: true, required: false, submitOnChange: true, image: getAppImg("exclude")
            paragraph title:"Notice:", pTS("To prevent unwanted devices from reinstalling after removal make sure to add it to the Don't use these devices input above before removing.", getAppImg("info", true), false)
        }
    }
}

private devCleanupPage() {
    return dynamicPage(name: "devCleanupPage", uninstall: false, install: false) {
        devCleanupSect()
    }
}

private devCleanupSect() {
    if(state?.isInstalled && !state?.resumeConfig) {
        section(sTS("Device Cleanup Options:")) {
            List remDevs = getRemovableDevs()
            if(remDevs?.size()) { paragraph "Removable Devices:\n${remDevs?.sort()?.join("\n")}", required: true, state: null }
            paragraph title:"Notice:", pTS("Remember to add device to filter above to prevent recreation.  Also the cleanup process will fail if the devices are used in external apps/automations", getAppImg("info", true), true, "#2784D9")
            input "cleanUpDevices", "bool", title: inTS("Cleanup Unused Devices?"), description: "", required: false, defaultValue: false, submitOnChange: true
            if(cleanUpDevices) { removeDevices() }
        }
    }
}

private List getRemovableDevs() {
    Map eDevs = state?.echoDeviceMap ?: [:]
    List cDevs = (isST() ? app?.getChildDevices(true) : app?.getChildDevices())
    List remDevs = []
    cDevs?.each { cDev->
        if(cDev?.deviceNetworkId?.toString() == "echoSpeaks_websocket") { return }
        def dni = cDev?.deviceNetworkId?.tokenize("|")
        if(eDevs?.size() && dni[2] && !eDevs?.containsKey(dni[2])) { remDevs?.push(cDev?.getLabel() as String) }
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
    return str != "" ? str : null
}

def settingsPage() {
    return dynamicPage(name: "settingsPage", uninstall: false, install: false) {
        section(sTS("App Change Details:")) {
            href "changeLogPage", title: inTS("View App Revision History", getAppImg("change_log", true)), description: "Tap to view", image: getAppImg("change_log")
        }
        section(sTS("Logging:")) {
            input "logInfo", "bool", title: inTS("Show Info Logs?", getAppImg("debug", true)), required: false, defaultValue: true, submitOnChange: true, image: getAppImg("debug")
            input "logWarn", "bool", title: inTS("Show Warning Logs?", getAppImg("debug", true)), required: false, defaultValue: true, submitOnChange: true, image: getAppImg("debug")
            input "logError", "bool", title: inTS("Show Error Logs?", getAppImg("debug", true)), required: false, defaultValue: true, submitOnChange: true, image: getAppImg("debug")
            input "logDebug", "bool", title: inTS("Show Debug Logs?", getAppImg("debug", true)), description: "Auto disables after 6 hours", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("debug")
            input "logTrace", "bool", title: inTS("Show Detailed Logs?", getAppImg("debug", true)), description: "Only enabled when asked to.\n(Auto disables after 6 hours)", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("debug")
        }
        // section(sTS("GrayLog Device"), hideWhenEmpty: true) {
        //     input "logDevice", "device.GrayLogDevice", title: inTS("Gray Log Devices?", getAppImg("debug", true)), required: false, submitOnChange: true, image: getAppImg("debug")
        // }
        if(advLogsActive()) { logsEnabled() }
        showDevSharePrefs()
        section(sTS("Diagnostic Data:")) {
            paragraph pTS("If you are having trouble send a private message to the developer with a link to this page that is shown below.", null, false, "gray")
            input "diagShareSensitveData", "bool", title: inTS("Share Cookie Data?", getAppImg("question", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("question")
            href url: getAppEndpointUrl("diagData"), style: "external", title: inTS("Diagnostic Data"), description: "Tap to view"
        }
    }
}

def deviceListPage() {
    return dynamicPage(name: "deviceListPage", install: false) {
        Boolean onST = isST()
        section(sTS("Discovered Devices:")) {
            state?.echoDeviceMap?.sort { it?.value?.name }?.each { k,v->
                String str = "Status: (${v?.online ? "Online" : "Offline"})"
                str += "\nStyle: ${v?.style?.name}"
                str += "\nFamily: ${v?.family}"
                str += "\nType: ${v?.type}"
                str += "\nVolume Control: (${v?.volumeSupport?.toString()?.capitalize()})"
                str += "\nAnnouncements: (${v?.announceSupport?.toString()?.capitalize()})"
                str += "\nText-to-Speech: (${v?.ttsSupport?.toString()?.capitalize()})"
                str += "\nMusic Player: (${v?.mediaPlayer?.toString()?.capitalize()})"
                str += v?.supported != true ? "\nUnsupported Device: (True)" : ""
                str += (v?.mediaPlayer == true && v?.musicProviders) ? "\nMusic Providers: [${v?.musicProviders}]" : ""
                if(onST) {
                    paragraph title: pTS(v?.name, getAppImg(v?.style?.image, true), false, "#2784D9"), str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.style?.image)
                } else { href "deviceListPage", title: inTS(v?.name, getAppImg(v?.style?.image, true)), description: str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.style?.image) }
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
                        paragraph title: pTS(v?.name, getAppImg(v?.image, true), false), str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.image)
                    } else { href "unrecogDevicesPage", title: inTS(v?.name, getAppImg(v?.image, true)), description: str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.image) }
                }
                input "bypassDeviceBlocks", "bool", title: inTS("Override Blocks and Create Ignored Devices?"), description: "WARNING: This will create devices for all remaining ignored devices", required: false, defaultValue: false, submitOnChange: true
            } else {
                paragraph pTS("No Uncognized Devices", null, true)
            }
        }
        if(ignDevs?.size()) {
            section(sTS("User Ignored Devices:")) {
                ignDevs?.sort { it?.value?.name }?.each { k,v->
                    String str = "Status: (${v?.online ? "Online" : "Offline"})\nStyle: ${v?.desc}\nFamily: ${v?.family}\nType: ${v?.type}\nVolume Control: (${v?.volume?.toString()?.capitalize()})"
                    str += "\nText-to-Speech: (${v?.tts?.toString()?.capitalize()})\nMusic Player: (${v?.mediaPlayer?.toString()?.capitalize()})\nReason Ignored: (${v?.reason})"
                    if(onST) {
                        paragraph title: pTS(v?.name, getAppImg(v?.image, true), false, "#2784D9"), str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.image)
                    } else { href "unrecogDevicesPage", title: inTS(v?.name, getAppImg(v?.image, true)), description: str, required: true, state: (v?.online ? "complete" : null), image: getAppImg(v?.image) }
                }
            }
        }
    }
}

def showDevSharePrefs() {
    section(sTS("Share Data with Developer:")) {
        paragraph title: "What is this used for?", pTS("These options send non-user identifiable information and error data to diagnose catch trending issues.", null, false)
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

Map getDeviceList(isInputEnum=false, filters=[]) {
    Map devMap = [:]
    Map availDevs = state?.echoDeviceMap ?: [:]
    availDevs?.each { key, val->
        if(filters?.size()) {
            if(filters?.contains('tts') && val?.ttsSupport != true) { return }
            if(filters?.contains('announce') && val?.ttsSupport != true && val?.announceSupport != true) { return }
        }
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

def notifPrefPage() {
    dynamicPage(name: "notifPrefPage", install: false) {
        section("") {
            paragraph title: "Notice:", pTS("The settings configure here are used by both the App and the Devices.", getAppImg("info", true), true, "#2784D9"), state: "complete"
        }
        section(sTS("Push Messages:")) {
            input "usePush", "bool", title: inTS("Send Push Notitifications\n(Optional)", getAppImg("notification", true)), required: false, submitOnChange: true, defaultValue: false, image: getAppImg("notification")
        }
        section(sTS("SMS Text Messaging:")) {
            paragraph pTS("To send to multiple numbers separate the number by a comma\nE.g. 8045551122,8046663344", null, false)
            input "smsNumbers", "text", title: inTS("Send SMS Text to...\n(Optional)", getAppImg("sms_phone", true)), required: false, submitOnChange: true, image: getAppImg("sms_phone")
        }
        section (sTS("Notification Devices:")) {
            input "notif_devs", "capability.notification", title: inTS("Send to Notification devices?", getAppImg("notification", true)), required: false, multiple: true, submitOnChange: true, image: getAppImg("notification")
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
                } else { paragraph pTS("New Install Detected!!!\n\n1. Press Done to Finish the Install.\n2. Goto the Automations Tab at the Bottom\n3. Tap on the SmartApps Tab above\n4. Select ${app?.getLabel()} and Resume configuration", getAppImg("info", true), false, "#2784D9"), state: "complete" }
            }
        }
        if(settings?.smsNumbers?.toString()?.length()>=10 || settings?.notif_devs || settings?.usePush || (settings?.pushoverEnabled && settings?.pushoverDevices)) {
            if((settings?.usePush || settings?.notif_devs || (settings?.pushoverEnabled && settings?.pushoverDevices)) && !state?.pushTested && state?.pushoverManager) {
                if(sendMsg("Info", "Push Notification Test Successful. Notifications Enabled for ${app?.label}", true)) {
                    state.pushTested = true
                }
            }
            section(sTS("Notification Restrictions:")) {
                def t1 = getNotifSchedDesc()
                href "setNotificationTimePage", title: inTS("Quiet Restrictions", getAppImg("restriction", true)), description: (t1 ?: "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("restriction")
            }
            section(sTS("Missed Poll Alerts:")) {
                input (name: "sendMissedPollMsg", type: "bool", title: inTS("Send Missed Checkin Alerts?", getAppImg("late", true)), defaultValue: true, submitOnChange: true, image: getAppImg("late"))
                if(settings?.sendMissedPollMsg) {
                    input (name: "misPollNotifyWaitVal", type: "enum", title: inTS("Time Past the Missed Checkin?", getAppImg("delay_time", true)), description: "Default: 45 Minutes", required: false, defaultValue: 2700, options: notifValEnum(), submitOnChange: true, image: getAppImg("delay_time"))
                    input (name: "misPollNotifyMsgWaitVal", type: "enum", title: inTS("Send Reminder After?", getAppImg("reminder", true)), description: "Default: 1 Hour", required: false, defaultValue: 3600, options: notifValEnum(), submitOnChange: true, image: getAppImg("reminder"))
                }
            }
            section(sTS("Cookie Alerts:")) {
                input (name: "sendCookieRefreshMsg", type: "bool", title: inTS("Send on Refreshed Cookie?", getAppImg("cookie", true)), defaultValue: false, submitOnChange: true, image: getAppImg("cookie"))
                input (name: "sendCookieInvalidMsg", type: "bool", title: inTS("Send on Invalid Cookie?", getAppImg("cookie", true)), defaultValue: true, submitOnChange: true, image: getAppImg("cookie"))
            }
            section(sTS("Code Update Alerts:")) {
                input "sendAppUpdateMsg", "bool", title: inTS("Send for Updates...", getAppImg("update", true)), defaultValue: true, submitOnChange: true, image: getAppImg("update")
                if(settings?.sendAppUpdateMsg) {
                    input (name: "updNotifyWaitVal", type: "enum", title: inTS("Send Reminders After?", getAppImg("reminder", true)), description: "Default: 12 Hours", required: false, defaultValue: 43200, options: notifValEnum(), submitOnChange: true, image: getAppImg("reminder"))
                }
            }
        } else { state.pushTested = false }
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
            input "quietDays", "enum", title: inTS("Only on these week days", getAppImg("day_calendar", true)), multiple: true, required: false, image: getAppImg("day_calendar"), options: weekDaysEnum()
            input "quietModes", "mode", title: inTS("When these modes are Active", getAppImg("mode", true)), multiple: true, submitOnChange: true, required: false, image: getAppImg("mode")
        }
    }
}

def uninstallPage() {
    dynamicPage(name: "uninstallPage", title: "Uninstall", uninstall: true) {
        section("") { paragraph "This will remove the app, all devices, all actions, all zones.\n\nPlease make sure that any devices created by this app are removed from any routines/rules/smartapps before tapping Remove." }
        if(isST()) { remove("Remove App, Devices, Actions, and Zones!", "WARNING!!!", "Last Chance to Stop!\nThis action is not reversible\n\nAll items will be removed") }
    }
}

String bulletItem(String inStr, String strVal) { return "${inStr == "" ? "" : "\n"} \u2022 ${strVal}" }
String dashItem(String inStr, String strVal, newLine=false) { return "${(inStr == "" && !newLine) ? "" : "\n"} - ${strVal}" }

def deviceTestPage() {
    return dynamicPage(name: "deviceTestPage", uninstall: false, install: false) {
        section("") {
            href "speechPage", title: inTS("Speech Test", getAppImg("broadcast", true)), description: (t1 ?: "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("broadcast")
            href "announcePage", title: inTS("Announcement Test", getAppImg("announcement", true)), description: (t1 ?: "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("announcement")
            href "sequencePage", title: inTS("Sequence Creator Test", getAppImg("sequence", true)), description: (t1 ?: "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("sequence")
        }
    }
}

def speechPage() {
    return dynamicPage(name: "speechPage", uninstall: false, install: false) {
        section("") {
            paragraph pTS("This feature has been known to have issues and may not work because it's not supported by all Alexa devices.  To test each device individually I suggest using the device interface and press Test Speech or Test Announcement")
            Map devs = getDeviceList(true, [tts])
            input "test_speechDevices", "enum", title: inTS("Select Devices to Test the Speech"), description: "Tap to select", options: (devs ? devs?.sort{it?.value} : []), multiple: true, required: false, submitOnChange: true
            if(test_speechDevices?.size() >= 3) { paragraph "Amazon will Rate Limit more than 3 device commands at a time.  There will be a delay in the other devices but they should play the test after a few seconds", state: null}
            input "test_speechVolume", "number", title: inTS("Speak at this volume"), description: "Enter number", range: "0..100", defaultValue: 30, required: false, submitOnChange: true
            input "test_speechRestVolume", "number", title: inTS("Restore to this volume after"), description: "Enter number", range: "0..100", defaultValue: null, required: false, submitOnChange: true
            input "test_speechMessage", "text", title: inTS("Message to Speak"), defaultValue: "This is a speach test for your Echo speaks device!!!", required: true, submitOnChange: true
        }
        if(settings?.test_speechDevices) {
            section() {
                input "test_speechRun", "bool", title: inTS("Perform the Speech Test?"), description: "", required: false, defaultValue: false, submitOnChange: true
                if(test_speechRun) { executeSpeechTest() }
            }
        }
    }
}

def announcePage() {
    return dynamicPage(name: "announcePage", uninstall: false, install: false) {
        section("") {
            paragraph pTS("This feature has known to have issues and may not work because it's not supported by all Alexa devices.  To test each device individually I suggest using the device interface and press Test Speech or Test Announcement")
            if(!settings?.test_announceDevices) {
                input "test_announceAllDevices", "bool", title: inTS("Test Announcement using All Supported Devices"), defaultValue: false, required: false, submitOnChange: true
            }
            if(!test_announceAllDevices) {
                def devs = getChildDevicesByCap("announce") ?: []
                input "test_announceDevices", "enum", title: inTS("Select Devices to Test the Announcement"), description: "Tap to select", options: (devs?.collectEntries { [(it?.getId()): it?.getLabel() as String] }), multiple: true, required: false, submitOnChange: true
            }
            if(test_announceAllDevices || test_announceDevices) {
                input "test_announceVolume", "number", title: inTS("Announce at this volume"), description: "Enter number", range: "0..100", defaultValue: 30, required: false, submitOnChange: true
                input "test_announceRestVolume", "number", title: inTS("Restore to this volume after"), description: "Enter number", range: "0..100", defaultValue: null, required: false, submitOnChange: true
                input "test_announceMessage", "text", title: inTS("Message to announce"), defaultValue: "This is a test of the Echo speaks announcement system!!!", required: true, submitOnChange: true
            }
        }
        if(settings?.test_announceDevices || settings?.test_announceAllDevices) {
            section() {
                input "test_announceRun", "bool", title: inTS("Perform the Announcement?"), description: "", required: false, defaultValue: false, submitOnChange: true
                if(test_announceRun) { executeAnnouncement() }
            }
        }
    }
}

Map seqItemsAvail() {
    return [
        other: [
            "weather":null, "traffic":null, "flashbriefing":null, "goodnews":null, "goodmorning":null, "goodnight":null, "cleanup":null,
            "singasong":null, "tellstory":null, "funfact":null, "joke":null, "playsearch":null, "calendartoday":null,
            "calendartomorrow":null, "calendarnext":null, "stop":null, "stopalldevices":null,
            "wait": "value (seconds)", "volume": "value (0-100)", "speak": "message", "announcement": "message",
            "announcementall": "message", "pushnotification": "message", "email": null
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
            String str4 = "DoNotDisturb Options:"
            seqItemsAvail()?.dnd?.sort()?.each { k, v->
                str4 += "${bulletItem(str4, "${k}${v != null ? "::${v}" : ""}")}"
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
            // paragraph str4, state: "complete"
            paragraph str2, state: "complete"
            paragraph str3, state: "complete"
            paragraph "Enter the command in a format exactly like this:\nvolume::40,, speak::this is so silly,, wait::60,, weather,, cannedtts_random::goodbye,, traffic,, amazonmusic::green day,, volume::30\n\nEach command needs to be separated by a double comma `,,` and the separator between the command and value must be command::value.", state: "complete"
        }
        section(sTS("Sequence Test Config:")) {
            input "test_sequenceDevice", "device.EchoSpeaksDevice", title: inTS("Select Devices to Test Sequence Command"), description: "Tap to select", multiple: false, required: (settings?.test_sequenceString != null), submitOnChange: true
            input "test_sequenceString", "text", title: inTS("Sequence String to Use"), required: (settings?.test_sequenceDevice != null), submitOnChange: true
        }
        if(settings?.test_sequenceDevice && settings?.test_sequenceString) {
            section() {
                input "test_sequenceRun", "bool", title: inTS("Perform the Sequence?"), description: "", required: false, defaultValue: false, submitOnChange: true
                if(test_sequenceRun) { executeSequence() }
            }
        }
    }
}

Integer getRecheckDelay(Integer msgLen=null, addRandom=false) {
    def random = new Random()
    Integer randomInt = random?.nextInt(5) //Was using 7
    if(!msgLen) { return 30 }
    def v = (msgLen <= 14 ? 1 : (msgLen / 14)) as Integer
    // logTrace("getRecheckDelay($msgLen) | delay: $v + $randomInt")
    return addRandom ? (v + randomInt) : (v < 5 ? 5 : v)
}

private executeSpeechTest() {
    settingUpdate("test_speechRun", "false", "bool")
    String testMsg = settings?.test_speechMessage
    List selectedDevs = settings?.test_speechDevices
    selectedDevs?.each { devSerial->
        def childDev = getChildDeviceBySerial(devSerial)
        if(childDev && childDev?.hasCommand('setVolumeSpeakAndRestore')) {
            childDev?.setVolumeSpeakAndRestore(settings?.test_speechVolume as Integer, testMsg, (settings?.test_speechRestVolume ?: 30))
        } else {
            logError("Speech Test device with serial# (${devSerial} was not located!!!")
        }
    }
}

private executeAnnouncement() {
    settingUpdate("test_announceRun", "false", "bool")
    String testMsg = settings?.test_announceMessage
    List sDevs = settings?.test_announceAllDevices ? getChildDevicesByCap("announce") : getDevicesFromList(settings?.test_announceDevices)
    if(sDevs?.size()) {
        if(sDevs?.size() > 1) {
            List devObj = []
            sDevs?.each { devObj?.push([deviceTypeId: it?.getEchoDeviceType() as String, deviceSerialNumber: it?.getEchoSerial() as String]) }
            def devJson = new groovy.json.JsonOutput().toJson(devObj)
            sDevs[0]?.sendAnnouncementToDevices(testMsg, "Echo Speaks Test", devObj, settings?.test_announceVolume ?: null, settings?.test_announceRestVolume ?: null)
        } else {
            sDevs[0]?.playAnnouncement(testMsg, "Echo Speaks Test", settings?.test_announceVolume ?: null, settings?.test_announceRestVolume ?: null)
        }
    }
}

private executeSequence() {
    settingUpdate("test_sequenceRun", "false", "bool")
    String seqStr = settings?.test_sequenceString
    if(settings?.test_sequenceDevice?.hasCommand("executeSequenceCommand")) {
        settings?.test_sequenceDevice?.executeSequenceCommand(seqStr as String)
    } else {
        logWarn("sequence test device doesn't support the executeSequenceCommand command...", true)
    }
}

private executeTuneInSearch() {
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/tunein/search",
        query: [ query: settings?.test_tuneinSearchQuery, mediaOwnerCustomerId: state?.deviceOwnerCustomerId ],
        headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
        requestContentType: "application/json",
        contentType: "application/json"
    ]
    Map results = [:]
    try {
        httpGet(params) { response ->
            results = response?.data ?: [:]
        }
    } catch (ex) {
        respExceptionHandler(ex, "executeTuneInSearch")
    }
    return results
}

private executeMusicSearchTest() {
    settingUpdate("test_musicSearchRun", "false", "bool")
    if(settings?.test_musicDevice && settings?.test_musicProvider && settings?.test_musicQuery) {
        if(settings?.test_musicDevice?.hasCommand("searchMusic")) {
            logDebug("Performing ${settings?.test_musicProvider} Search Test with Query: (${settings?.test_musicQuery}) on Device: (${settings?.test_musicDevice})")
            settings?.test_musicDevice?.searchMusic(settings?.test_musicQuery as String, settings?.test_musicProvider as String)
        } else { logError("The Device ${settings?.test_musicDevice} does NOT support the searchMusic() command...") }
    }
}

def musicSearchTestPage() {
    return dynamicPage(name: "musicSearchTestPage", uninstall: false, install: false) {
        section("Test a Music Search on Device:") {
            paragraph "Use this to test the search you discovered above directly on a device.", state: "complete"
            Map testEnum = ["CLOUDPLAYER": "My Library", "AMAZON_MUSIC": "Amazon Music", "I_HEART_RADIO": "iHeartRadio", "PANDORA": "Pandora", "APPLE_MUSIC": "Apple Music", "TUNEIN": "TuneIn", "SIRIUSXM": "siriusXm", "SPOTIFY": "Spotify"]
            input "test_musicProvider", "enum", title: inTS("Select Music Provider to perform test", getAppImg("music", true)), defaultValue: null, required: false, options: testEnum, submitOnChange: true, image: getAppImg("music")
            if(test_musicProvider) {
                input "test_musicQuery", "text", title: inTS("Music Search term to test on Device", getAppImg("search2", true)), defaultValue: null, required: false, submitOnChange: true, image: getAppImg("search2")
                if(settings?.test_musicQuery) {
                    input "test_musicDevice", "device.EchoSpeaksDevice", title: inTS("Select a Device to Test Music Search", getAppImg("echo_speaks_3.1x", true)), description: "Tap to select", multiple: false, required: false, submitOnChange: true, image: getAppImg("echo_speaks_3.1x")
                    if(test_musicDevice) {
                        input "test_musicSearchRun", "bool", title: inTS("Perform the Music Search Test?", getAppImg("music", true)), description: "", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("music")
                        if(test_musicSearchRun) { executeMusicSearchTest() }
                    }
                }
            }
        }
        section(sTS("TuneIn Search Results:")) {
            paragraph "Enter a search phrase to query TuneIn to help you find the right search term to use in searchTuneIn() command.", state: "complete"
            input "test_tuneinSearchQuery", "text", title: inTS("Enter search phrase for TuneIn", getAppImg("tunein", true)), defaultValue: null, required: false, submitOnChange: true, image: getAppImg("tunein")
            if(settings?.test_tuneinSearchQuery) {
                href "searchTuneInResultsPage", title: inTS("View search results!", getAppImg("search2", true)), description: "Tap to proceed...", image: getAppImg("search2")
            }
        }
    }
}

def searchTuneInResultsPage() {
    return dynamicPage(name: "searchTuneInResultsPage", uninstall: false, install: false) {
        def results = executeTuneInSearch()
        Boolean onST = isST()
        section(sTS("Search Results: (Query: ${settings?.test_tuneinSearchQuery})")) {
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
                                    paragraph title: pTS(item2?.name?.take(75), (onST ? null : item2?.image), false), str, required: true, state: (!item2?.name?.contains("Not Supported") ? "complete" : null), image: item2?.image ?: ""
                                } else { href "searchTuneInResultsPage", title: pTS(item2?.name?.take(75), (onST ? null : item2?.image), false), description: str, required: true, state: (!item2?.name?.contains("Not Supported") ? "complete" : null), image: onST && item2?.image ? item2?.image : null }
                            }
                        } else {
                            String str = ""
                            str += "ContentType: (${item?.contentType})"
                            str += "\nId: (${item?.id})"
                            str += "\nDescription: ${item?.description}"
                            if(onST) {
                                paragraph title: pTS(item?.name?.take(75), (onST ? null : item?.image), false), str, required: true, state: (!item?.name?.contains("Not Supported") ? "complete" : null), image: item?.image ?: ""
                            } else { href "searchTuneInResultsPage", title: pTS(item?.name?.take(75), (onST ? null : item?.image), false), description: str, required: true, state: (!item?.name?.contains("Not Supported") ? "complete" : null), image: onST && item?.image ? item?.image : null }
                        }
                    }
                }
            } else { paragraph "No Results found..." }
        }
    }
}

private getChildDeviceBySerial(String serial) {
    def childDevs = isST() ? app?.getChildDevices(true) : app?.getChildDevices()
    return childDevs?.find { it?.deviceNetworkId?.tokenize("|")?.contains(serial) } ?: null
}

public getChildDeviceByCap(String cap) {
    def childDevs = isST() ? app?.getChildDevices(true) : app?.getChildDevices()
    return childDevs?.find { it?.currentValue("permissions") && it?.currentValue("permissions")?.toString()?.contains(cap) } ?: null
}

public getDevicesFromList(List ids) {
    def cDevs = isST() ? app?.getChildDevices(true) : app?.getChildDevices()
    return cDevs?.findAll { it?.id in ids } ?: null
}

public getDeviceFromId(String id) {
    def cDevs = isST() ? app?.getChildDevices(true) : app?.getChildDevices()
    return cDevs?.find { it?.id == id } ?: null
}

public getChildDevicesByCap(String cap) {
    def childDevs = isST() ? app?.getChildDevices(true) : app?.getChildDevices()
    return childDevs?.findAll { it?.currentValue("permissions") && it?.currentValue("permissions")?.toString()?.contains(cap) } ?: null
}

def donationPage() {
    return dynamicPage(name: "donationPage", title: "", nextPage: "mainPage", install: false, uninstall: false) {
        section("") {
            def str = ""
            str += "Hello User, \n\nPlease forgive the interuption but it's been 30 days since you installed/updated this SmartApp and I wanted to present you with this one time reminder that donations are accepted (We do not require them)."
            str += "\n\nIf you have been enjoying the software and devices please remember that we have spent thousand's of hours of our spare time working on features and stability for those applications and devices."
            str += "\n\nIf you have already donated, thank you very much for your support!"
            str += "\n\nIf you are just not interested in donating please ignore this message"

            str += "\n\nThanks again for using Echo Speaks"
            paragraph str, required: true, state: null
            href url: textDonateLink(), style: "external", required: false, title: "Donations", description: "Tap to open in browser", state: "complete", image: getAppImg("donate")
        }
        updInstData("shownDonation", true)
    }
}

def installed() {
    logInfo("Installed Event Received...")
    state?.installData = [initVer: appVersion(), dt: getDtNow().toString(), updatedDt: "Not Set", showDonation: false, sentMetrics: false, shownChgLog: true]
    state?.isInstalled = true
    sendInstallData()
    initialize()
}

def updated() {
    logInfo("Updated Event Received...")
    if(!state?.isInstalled) { state?.isInstalled = true }
    if(!state?.installData) { state?.installData = [initVer: appVersion(), dt: getDtNow().toString(), updatedDt: getDtNow().toString(), shownDonation: false, sentMetrics: false] }
    unsubscribe()
    state?.zoneEvtsActive = false
    unschedule()
    stateMigrationChk()
    initialize()
}

def initialize() {
    if(app?.getLabel() != "Echo Speaks") { app?.updateLabel("Echo Speaks") }
    if(settings?.optOutMetrics == true && state?.appGuid) { if(removeInstallData()) { state?.appGuid = null } }
    if(!state?.resumeConfig) { subscribe(app, onAppTouch) }
    if(guardAutoConfigured()) {
        if(settings?.guardAwayAlarm && settings?.guardHomeAlarm) {
            subscribe(location, "${!isST() ? "hsmStatus" : "alarmSystemStatus"}", guardTriggerEvtHandler)
        }
        if(settings?.guardAwayModes && settings?.guardHomeModes) {
            subscribe(location, "mode", guardTriggerEvtHandler)
        }
        if(settings?.guardAwaySwitch && settings?.guardHomeSwitch) {
            if(settings?.guardHomeSwitch) subscribe(settings?.guardHomeSwitch, "switch", guardTriggerEvtHandler)
            if(settings?.guardAwaySwitch) subscribe(settings?.guardAwaySwitch, "switch", guardTriggerEvtHandler)
        }
        if(settings?.guardAwayPresence) {
            subscribe(settings?.guardAwayPresence, "presence", guardTriggerEvtHandler)
        }
    }
    if(!state?.resumeConfig) {
        updateZoneSubscriptions()
        validateCookie(true)
        runEvery1Minute("getOtherData")
        runEvery10Minutes("getEchoDevices") //This will reload the device list from Amazon
        // runEvery1Minute("getEchoDevices") //This will reload the device list from Amazon
        runIn(11, "postInitialize")
        getOtherData()
        getEchoDevices()
    }
}

private stateMigrationChk() {
    if(!getAppFlag("stateMapConverted")) { stateMapMigration() }
    return
}


def updateZoneSubscriptions() {
    if(state?.zoneEvtsActive != true) {
        subscribe(location, "es3ZoneState", zoneStateHandler)
        subscribe(location, "es3ZoneRemoved", zoneRemovedHandler)
        state?.zoneEvtsActive = true
    }
}

def postInitialize() {
    runEvery5Minutes("healthCheck") // This task checks for missed polls, app updates, code version changes, and cloud service health
    appCleanup()
    reInitChildDevices()
}

def uninstalled() {
    log.warn "uninstalling app and devices"
    unschedule()
    if(settings?.optOutMetrics != true) { if(removeInstallData()) { state?.appGuid = null } }
    clearCloudConfig()
    clearCookieData("App Uninstalled")
    removeDevices(true)
}

private appCleanup() {
    List items = [
        "availableDevices", "consecutiveCmdCnt", "isRateLimiting", "versionData", "heartbeatScheduled", "serviceAuthenticated", "cookie", "misPollNotifyWaitVal", "misPollNotifyMsgWaitVal",
        "updNotifyWaitVal", "lastDevActivity", "devSupMap", "tempDevSupData", "devTypeIgnoreData"
    ]
    items?.each { si-> if(state?.containsKey(si as String)) { state?.remove(si)} }
    state?.pollBlocked = false
    state?.resumeConfig = false
    state?.missPollRepair = false
    state?.deviceRefreshInProgress = false
    // state?.zoneStatusMap = [:]

    // Settings Cleanup
    List setItems = ["performBroadcast", "stHub", "cookieRefreshDays"]
    settings?.each { si-> ["music", "tunein", "announce", "perform", "broadcast", "sequence", "speech", "test_"]?.each { swi-> if(si?.key?.startsWith(swi as String)) { setItems?.push(si?.key as String) } } }
    setItems?.unique()?.sort()?.each { sI-> if(settings?.containsKey(sI as String)) { settingRemove(sI as String) } }
    cleanUpdVerMap()
}

void wsEvtHandler(evt) {
    // log.debug "evt: ${evt}"
    if(evt && evt?.id && (evt?.attributes?.size() || evt?.triggers?.size())) {
        if("bluetooth" in evt?.triggers) { getBluetoothData() }
        if("activity" in evt?.triggers) { getDeviceActivity(null, true) }
        if(evt?.all == true) {
            getEsDevices()?.each { eDev->
                if(evt?.attributes?.size()) { evt?.attributes?.each { k,v-> eDev?.sendEvent(name: k as String, value: v) } }
                if(evt?.triggers?.size()) { eDev?.websocketUpdEvt(evt?.triggers) }
            }
        } else {
            def eDev = findEchoDevice(evt?.id as String)
            if(eDev) {
                evt?.attributes?.each { k,v-> eDev?.sendEvent(name: k as String, value: v) }
                if(evt?.triggers?.size()) { eDev?.websocketUpdEvt(evt?.triggers) }
            }
        }
    }
}

private findEchoDevice(serial) {
    return getEsDevices()?.find { it?.getEchoSerial()?.toString() == serial as String } ?: null
}

void webSocketStatus(Boolean active) {
    state?.websocketActive = active
    runIn(3, "updChildSocketStatus")
}

private updChildSocketStatus() {
    def active = (state?.websocketActive == true)
    getEsDevices()?.each { it?.updSocketStatus(active) }
    updTsVal("lastWebsocketUpdDt")
}

def zoneStateHandler(evt) {
    String id = evt?.value?.toString()
    Map data = evt?.jsonData;
    // log.trace "zone: ${id} | Data: $data"
    if(data && id) {
        Map zoneMap = atomicState?.zoneStatusMap ?: [:]
        zoneMap[id as String] = [name: data?.name, active: data?.active]
        atomicState?.zoneStatusMap = zoneMap
    }
}

def zoneRemovedHandler(evt) {
    String id = evt?.value?.toString()
    Map data = evt?.jsonData;
    log.trace "zone removed: ${id} | Data: $data"
    if(data && id) {
        Map zoneMap = atomicState?.zoneStatusMap ?: [:]
        if(zoneMap?.containsKey(id as String)) { zoneMap?.remove(id as String) }
        atomicState?.zoneStatusMap = zoneMap
    }
}

private requestZoneRefresh() {
    atomicState?.zoneStatusMap = [:]
    sendLocationEvent(name: "es3ZoneRefresh", value: "sendStatus", data: [sendStatus: true], isStateChange: true, display: false, displayed: false)
}

public Map getZones() {
    return atomicState?.zoneStatusMap ?: [:]
}

public Map getActiveZones() {
    Map zones = atomicState?.zoneStatusMap ?: [:]
    return zones?.size() ? zones?.findAll { it?.value?.active == true } : [:]
}

public List getActiveZoneNames() {
    Map zones = atomicState?.zoneStatusMap ?: [:]
    return zones?.size() ? zones?.findAll { it?.value?.active == true }?.collect { it?.value?.name as String } : []
}

def getActionApps() {
    return getAllChildApps()?.findAll { it?.name == actChildName() }
}

def getEsDevices() {
    return (isST() ? app?.getChildDevices(true) : getChildDevices())?.findAll { it?.isWS() == false }
}

def getSocketDevice() {
    return (isST() ? app?.getChildDevices(true) : getChildDevices())?.find { it?.isWS() == true }
}

def getZoneApps() {
    return getAllChildApps()?.findAll { it?.name == zoneChildName() }
}

def getZoneById(String id) {
    return getZoneApps()?.find { it?.id?.toString() == id }
}

def onAppTouch(evt) {
    // logTrace("appTouch...")
    updated()
}

mappings {
    path("/renderMetricData")           { action: [GET: "renderMetricData"] }
    path("/receiveData")                { action: [POST: "processData"] }
    path("/config")                     { action: [GET: "renderConfig"] }
    path("/textEditor/:cId/:inName")    { action: [GET: "renderTextEditPage", POST: "textEditProcessing"] }
    path("/cookie")                     { action: [GET: "getCookieData", POST: "storeCookieData", DELETE: "clearCookieData"] }
    path("/diagData")                   { action: [GET: "getDiagData"] }
    path("/diagCmds/:cmd")              { action: [GET: "execDiagCmds"] }
    path("/diagDataJson")               { action: [GET: "getDiagDataJson"] }
    path("/diagDataText")               { action: [GET: "getDiagDataText"] }
}

String getCookieVal() { return (state?.cookieData && state?.cookieData?.localCookie) ? state?.cookieData?.localCookie as String : null }
String getCsrfVal() { return (state?.cookieData && state?.cookieData?.csrf) ? state?.cookieData?.csrf as String : null }

def clearCloudConfig() {
    logTrace("clearCloudConfig called...")
    settingUpdate("resetService", "false", "bool")
    unschedule("cloudServiceHeartbeat")
    remServerItem(["useHeroku", "onHeroku", "serverHost", "isLocal"])
    state?.remove("herokuName")
    state?.serviceConfigured = false
    state?.resumeConfig = true
    if(!state?.authValid) { clearCookieData("clearCloudConfig") }
}

String getEnvParamsStr() {
    Map envParams = [:]
    envParams["smartThingsUrl"] = "${getAppEndpointUrl("receiveData")}"
    envParams["appCallbackUrl"] = "${getAppEndpointUrl("receiveData")}"
    envParams["hubPlatform"] = "${getPlatform()}"
    envParams["useHeroku"] = (isST() || settings?.useHeroku != false)
    envParams["serviceDebug"] = "false"
    envParams["serviceTrace"] = "false"
    envParams["amazonDomain"] = settings?.amazonDomain as String ?: "amazon.com"
    envParams["regionLocale"] = settings?.regionLocale as String ?: "en-US"
    envParams["hostUrl"] = "${getRandAppName()}.herokuapp.com"
    String envs = ""
    envParams?.each { k, v-> envs += "&env[${k}]=${v}" }
    return envs
}

private checkIfCodeUpdated() {
    Boolean codeUpdated = false
    List chgs = []
    // updChildVers()
    logDebug("Code versions: ${state?.codeVersions}")
    if(state?.codeVersions) {
        if(state?.codeVersions?.mainApp != appVersion()) {
            checkVersionData(true)
            chgs?.push("mainApp")
            state?.pollBlocked = true
            updCodeVerMap("mainApp", appVersion())
            Map iData = atomicState?.installData ?: [:]
            iData["updatedDt"] = getDtNow().toString()
            iData["shownChgLog"] = false
            if(iData?.shownDonation == null) {
                iData["shownDonation"] = false
            }
            atomicState?.installData = iData
            codeUpdated = true
        }
        def cDevs = (isST() ? app?.getChildDevices(true) : getChildDevices())
        def echoDev = cDevs?.find { !it?.isWS() }
        if(echoDev && codeVersions?.echoDevice && state?.codeVersions?.echoDevice != echoDev?.devVersion()) {
            chgs?.push("echoDevice")
            state?.pollBlocked = true
            updCodeVerMap("echoDevice", echoDev?.devVersion())
            codeUpdated = true
        }
        if(!isST()) {
            def wsDev = cDevs?.find { it?.isWS() }
            if(wsDev && state?.codeVersions?.wsDevice && state?.codeVersions?.wsDevice != wsDev?.devVersion()) {
                chgs?.push("wsDevice")
                updCodeVerMap("wsDevice", wsDev?.devVersion())
                codeUpdated = true
            }
        }
        def cApps = getActionApps()
        if(cApps?.size() && state?.codeVersions?.actionApp && state?.codeVersions?.actionApp != cApps[0]?.appVersion()) {
            chgs?.push("actionApp")
            state?.pollBlocked = true
            updCodeVerMap("actionApp", cApps[0]?.appVersion())
            codeUpdated = true
        }
        def zApps = getZoneApps()
        if(zApps?.size() && state?.codeVersions?.zoneApp && state?.codeVersions?.zoneApp != zApps[0]?.appVersion()) {
            chgs?.push("zoneApp")
            state?.pollBlocked = true
            // log.debug "zoneVer: ${zApps[0]?.appVersion()}"
            updCodeVerMap("zoneApp", zApps[0]?.appVersion())
            codeUpdated = true
        }
    }
    if(codeUpdated) {
        logInfo("Code Version Change Detected... | Re-Initializing SmartApp in 5 seconds | Changes: ${chgs}")
        runIn(5, "postCodeUpdated", [overwrite: false])
        return true
    } else {
        state?.pollBlocked = false
        return false
    }
}

private postCodeUpdated() {
    updated()
    runIn(10, "sendInstallData", [overwrite: false])
}

private resetQueues() {
    getEsDevices()?.findAll { it?.isWS() != true }?.each { it?.resetQueue() }
}

private reInitChildDevices() {
    getEsDevices()?.each { it?.triggerInitialize() }
    updChildVers()
    reInitChildActions()
}

private reInitChildZones() {
    getZoneApps()?.each { it?.triggerInitialize() }
}

private reInitChildActions() {
    getActionApps()?.each { it?.triggerInitialize() }
    runIn(3, "reInitChildZones")
}

def processData() {
    // logTrace("processData() | Data: ${request.JSON}")
    Map data = request?.JSON as Map
    if(data) {
        if(data?.version) {
            updServerItem("onHeroku", (isST() || data?.onHeroku != false || (!data?.isLocal && settings?.useHeroku != false)))
            updServerItem("isLocal", (!isST() && data?.isLocal == true))
            updServerItem("serverHost", (data?.serverUrl ?: null))
            logTrace("processData Received | Version: ${data?.version} | onHeroku: ${data?.onHeroku} | serverUrl: ${data?.serverUrl}")
            updCodeVerMap("server", data?.version)
            state?.serviceConfigured = true
        } else { log.debug "data: $data" }
    }
    def json = new groovy.json.JsonOutput().toJson([message: "success", version: appVersion()])
    render contentType: "application/json", data: json, status: 200
}

Boolean serverConfigured() {
    return (getServerItem("onHeroku") || getServerItem("isLocal"))
}

def getCookieData() {
    Map resp = state?.cookieData ?: [:]
    resp["refreshDt"] = getTsVal("lastCookieRrshDt") ?: null
    def json = new groovy.json.JsonOutput().toJson(resp)
    incrementCntByKey("getCookieCnt")
    render contentType: "application/json", data: json
}

def storeCookieData() {
    logTrace("storeCookieData Request Received...")
    Map data = request?.JSON as Map
    if(data && data?.cookieData) {
        logTrace("cookieData Received: ${request?.JSON?.cookieData?.keySet()}")
        Map cookieItems = [:]
        data?.cookieData?.each { k,v-> cookieItems[k as String] = v as String }
        state?.cookieData = cookieItems
        updServerItem("onHeroku", (isST() || data?.onHeroku != false || (!data?.isLocal && settings?.useHeroku != false)))
        updServerItem("isLocal", (!isST() && data?.isLocal == true))
        updServerItem("serverHost", (data?.serverUrl ?: null))
        updCodeVerMap("server", data?.version)
    }
    // log.debug "csrf: ${state?.cookieData?.csrf}"
    if(state?.cookieData?.localCookie && state?.cookieData?.csrf != null) {
        logInfo("Cookie data was updated | Reinitializing App... | Polling should restart in 10 seconds...")
        validateCookie(true)
        state?.serviceConfigured = true
        updTsVal("lastCookieRrshDt")
        checkGuardSupport()
        runIn(10, "initialize", [overwrite: true])
    }
}

def clearCookieData(src=null) {
    logTrace("clearCookieData(${src ?: ""})")
    settingUpdate("resetCookies", "false", "bool")
    state?.authValid = false
    state?.remove("cookie")
    state?.remove("cookieData")
    remTsVal(["lastCookieChkDt", "lastCookieRrshDt"])
    unschedule("getEchoDevices")
    unschedule("getOtherData")
    logWarn("Cookie Data has been cleared and Device Data Refreshes have been suspended...")
    updateChildAuth(false)
}

private refreshDevCookies() {
    logTrace("refreshDevCookies()")
    settingUpdate("refreshDevCookies", "false", "bool")
    logDebug("Re-Syncing Cookie Data with Devices")
    Boolean isValid = (state?.authValid && getCookieVal() != null && getCsrfVal() != null)
    updateChildAuth(isValid)
    return isValid
}

private updateChildAuth(Boolean isValid) {
    (isST() ? app?.getChildDevices(true) : getChildDevices())?.each { (isValid) ? it?.updateCookies([cookie: getCookieVal(), csrf: getCsrfVal()]) : it?.removeCookies(true); }
}

private authEvtHandler(Boolean isAuth, String src=null) {
    // log.debug "authEvtHandler(${isAuth})"
    state?.authValid = (isAuth == true)
    if(isAuth == false && !state?.noAuthActive) {
        clearCookieData()
        noAuthReminder()
        if(settings?.sendCookieInvalidMsg != false && getLastTsValSecs("lastCookieInvalidMsgDt") > 28800) {
            sendMsg("${app.name} Amazon Login Issue", "Amazon Cookie Has Expired or is Missing!!! Please login again using the Heroku Web Config page...")
            updTsVal("lastCookieInvalidMsgDt")
        }
        runEvery1Hour("noAuthReminder")
        state?.noAuthActive = true
        state?.authEvtClearReason = [dt: getDtNow(), src: src]
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
        logWarn("Echo Speaks Authentication is no longer valid... Please login again and commands will be allowed again!!! | Method: (${methodName})", true)
        return false
    }
    return true
}

String toQueryString(Map m) {
    return m.collect { k, v -> "${k}=${URLEncoder.encode(v?.toString(), "utf-8").replaceAll("\\+", "%20")}" }?.sort().join("&")
}

String getServerHostURL() {
    def srvHost = getServerItem("serverHost")
    return (getServerItem("isLocal") && srvHost) ? (srvHost as String ?: null) : "https://${getRandAppName()}.herokuapp.com"
}

Integer cookieRefreshSeconds() { return (settings?.refreshCookieDays ?: 5)*86400 as Integer }

def clearServerAuth() {
    logDebug("serverUrl: ${getServerHostURL()}")
    Map params = [ uri: getServerHostURL(), path: "/clearAuth" ]
    def execDt = now()
    httpGet(params) { resp->
        // log.debug "resp: ${resp.status} | data: ${resp?.data}"
        if (resp?.status == 200) {
            logInfo("Clear Server Auth Completed... | Process Time: (${execDt ? (now()-execDt) : 0}ms)")
        }
    }
}

private wakeupServer(c=false, g=false, src) {
    Map params = [
        uri: getServerHostURL(),
        path: "/wakeup",
        headers: [wakesrc: src],
        contentType: "text/plain",
        requestContentType: "text/plain"
    ]
    if(!getCookieVal() || !getCsrfVal()) { logWarn("wakeupServer | Cookie or CSRF Missing... Skipping Wakeup"); return; }
    execAsyncCmd("post", "wakeupServerResp", params, [execDt: now(), refreshCookie: c, updateGuard: g, wakesrc: src])
}

private runCookieRefresh() {
    settingUpdate("refreshCookie", "false", "bool")
    if(getLastTsValSecs("lastCookieRrshDt", 500000) < 86400) { logError("Cookie Refresh is blocked... | Last refresh was less than 24 hours ago.", true); return; }
    wakeupServer(true, false, "runCookieRefresh")
}

def wakeupServerResp(response, data) {
    def rData = null
    try { rData = response?.data ?: null }
    catch(ex) { logError("wakeupServerResp Exception: ${ex}") }
    updTsVal("lastServerWakeDt")
    if (rData && rData == "OK") {
        logInfo("wakeupServer Completed... | Process Time: (${data?.execDt ? (now()-data?.execDt) : 0}ms) | Source: (${data?.wakesrc})")
        if(data?.refreshCookie == true) { runIn(2, "cookieRefresh") }
        if(data?.updateGuard == true) { runIn(2, "checkGuardSupportFromServer") }
    }
}

private cookieRefresh() {
    Map cookieData = state?.cookieData ?: [:]
    if (!cookieData || !cookieData?.loginCookie || !cookieData?.refreshToken) {
        logError("Required Registration data is missing for Cookie Refresh")
        return
    }
    Map params = [
        uri: getServerHostURL(),
        path: "/refreshCookie",
        contentType: "application/json"
    ]
    execAsyncCmd("get", "cookieRefreshResp", params, [execDt: now()])
}

def cookieRefreshResp(response, data) {
    Map rData = null
    try {
        rData = response?.data ? parseJson(response?.data?.toString()) : null
        // log.debug "rData: $rData"
        if (rData && rData?.result && rData?.result?.size()) {
            logInfo("Amazon Cookie Refresh Completed | Process Time: (${data?.execDt ? (now()-data?.execDt) : 0}ms)")
            if(settings?.sendCookieRefreshMsg == true && getLastTsValSecs("lastCookieRfshMsgDt") > 15) {
                sendMsg("${app.name} Cookie Refresh", "Amazon Cookie was Refreshed Successfully!!!")
                updTsVal("lastCookieRfshMsgDt")
            }
            // log.debug "refreshAlexaCookie Response: ${rData?.result}"
        }
    } catch(ex) {
        if(ex instanceof groovyx.net.http.HttpResponseException ) {
            logError("cookieRefreshResp Response Exception | Status: (${ex?.getResponse()?.getStatus()}) | Msg: ${ex?.getMessage()}")
        } else if(ex instanceof java.net.SocketTimeoutException) {
            logError("cookieRefreshResp Response Socket Timeout | Msg: ${ex?.getMessage()}")
        } else if(ex instanceof java.net.UnknownHostException) {
            logError("cookieRefreshResp HostName Not Found | Msg: ${ex?.getMessage()}")
        } else if(ex instanceof org.apache.http.conn.ConnectTimeoutException) {
            logError("cookieRefreshResp Request Timeout | Msg: ${ex?.getMessage()}")
        } else { logError("cookieRefreshResp Exception: ${ex}") }
    }
}

private apiHealthCheck(frc=false) {
    try {
        Map params = [
            uri: getAmazonUrl(),
            path: "/api/ping",
            query: ["_": ""],
            headers: [ Cookie: getCookieVal(), csrf: getCsrfVal()],
            contentType: "plain/text"
        ]
        httpGet(params) { resp->
            logDebug("API Health Check Resp: (${resp?.getData()})")
            return (resp?.getData().toString() == "healthy")
        }
    } catch(ex) {
        respExceptionHandler(ex, "apiHealthCheck")
    }
}

Boolean validateCookie(frc=false) {
    Boolean valid = false
    try {
        if((!frc && getLastTsValSecs("lastCookieChkDt", 3600) <= 900) || !getCookieVal() || !getCsrfVal()) { return }
        def execDt = now()
        Map params = [
            uri: getAmazonUrl(),
            path: "/api/bootstrap",
            query: ["version": 0],
            headers: [ Cookie: getCookieVal(), csrf: getCsrfVal()],
            contentType: "application/json"
        ]
        httpGet(params) { resp->
            Map aData = resp?.data?.authentication ?: null
            if (aData) {
                // log.debug "aData: $aData"
                if(aData?.customerId) { state?.deviceOwnerCustomerId = aData?.customerId }
                if(aData?.customerName) { state?.customerName = aData?.customerName }
                valid = (resp?.data?.authentication?.authenticated != false)
            }
            logDebug("Cookie Validation: (${valid}) | Process Time: (${(now()-execDt)}ms)")
            authValidationEvent(valid, "validateCookie")
        }
    } catch(ex) {
        respExceptionHandler(ex, "validateCookie", true)
        incrementCntByKey("err_app_cookieValidCnt")
    }
    updTsVal("lastCookieChkDt")
    return valid
}

private getCustomerData(frc=false) {
    try {
        if(!frc && state?.amazonCustomerData && getLastTsValSecs("lastCustDataUpdDt") < 3600) { return state?.amazonCustomerData }
        def execDt = now()
        Map params = [
            uri: getAmazonUrl(),
            path: "/api/get-customer-pfm",
            query: [_: now()],
            headers: [ Cookie: getCookieVal(), csrf: getCsrfVal()],
            contentType: "application/json"
        ]
        httpGet(params) { resp->
            Map pData = resp?.data ?: null
            if (pData) {
                Map d = [:]
                if(pData?.marketPlaceLocale) { d["marketPlaceLocale"] = pData?.marketPlaceLocale }
                if(pData?.marketPlaceId) { d["marketPlaceId"] = pData?.marketPlaceId }
                state?.amazonCustomerData = d
            }
        }
    } catch(ex) {
        respExceptionHandler(ex, "getCustomerData", true)
        updTsVal("lastCustDataUpdDt")
    }
}

private userCommIds() {
    try {
        Map params = [
            uri: "https://alexa-comms-mobile-service.${getAmazonDomain()}",
            path: "/accounts",
            headers: [ Cookie: getCookieVal(), csrf: getCsrfVal()],
            contentType: "application/json"
        ]
        httpGet(params) { response->
            List resp = response?.data ?: []
            Map accItems = (resp?.size()) ? resp?.findAll { it?.signedInUser?.toString() == "true" }?.collectEntries { [(it?.commsId as String): [firstName: it?.firstName as String, signedInUser: it?.signedInUser, isChild: it?.isChild]]} : [:]
            state?.accountCommIds = accItems
            logDebug("Amazon User CommId's: (${accItems})")
        }
    } catch(ex) {
        respExceptionHandler(ex, "userCommIds")
    }
}

private authValidationEvent(Boolean valid, String src=null) {
    Integer listSize = 3
    List eList = atomicState?.authValidHistory ?: [true, true, true]
    eList.push(valid)
    if(eList?.size() > listSize) { eList = eList?.drop( eList?.size()-listSize ) }
    atomicState?.authValidHistory = eList
    if(eList?.every { it == false }) {
        logError("The last 3 Authentication Validations have failed | Clearing Stored Auth Data | Please login again using the Echo Speaks service...")
        authEvtHandler(false, src)
        return
    } else { authEvtHandler(true) }
}

private noAuthReminder() { logWarn("Amazon Cookie Has Expired or is Missing!!! Please login again using the Heroku Web Config page...") }

public childInitiatedRefresh() {
    Integer lastRfsh = getLastTsValSecs("lastChildInitRefreshDt", 3600)?.abs()
    if(state?.deviceRefreshInProgress == false && lastRfsh > 120) {
        logDebug("A Child Device is requesting a Device List Refresh...")
        updTsVal("lastChildInitRefreshDt")
        getOtherData()
        runIn(3, "getEchoDevices")
    } else {
        logWarn("childInitiatedRefresh request ignored... Refresh already in progress or it's too soon to refresh again | Last Refresh: (${lastRfsh} seconds)")
    }
}

public updChildVers() {
    def cApps = getActionApps()
    def zApps = getZoneApps()
    def cDevs = (isST() ? app?.getChildDevices(true) : getChildDevices())
    def eDevs = cDevs?.findAll { it?.isWS() != true }
    updCodeVerMap("actionApp", cApps?.size() ? cApps[0]?.appVersion() : null)
    updCodeVerMap("zoneApp", zApps?.size() ? zApps[0]?.appVersion() : null)
    updCodeVerMap("echoDevice", eDevs?.size() ? eDevs[0]?.devVersion() : null)
    if(!isST()) {
        def wDevs = cDevs?.findAll { it?.isWS() == true }
        updCodeVerMap("wsDevice", wDevs?.size() ? wDevs[0]?.devVersion() : null)
    }
}

private getMusicProviders() {
    if(state?.musicProviders && getLastTsValSecs("musicProviderUpdDt") < 3600) { return state?.musicProviders }
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/behaviors/entities",
        query: [ skillId: "amzn1.ask.1p.music" ],
        headers: [ Cookie: getCookieVal(), csrf: getCsrfVal(), Connection: "keep-alive", DNT: "1", "Routines-Version": "1.1.210292" ],
        contentType: "application/json"
    ]
    Map items = [:]
    try {
        httpGet(params) { response ->
            List rData = response?.data ?: []
            if(rData?.size()) {
                rData?.findAll { it?.availability == "AVAILABLE" }?.each { item->
                    items[item?.id] = item?.displayName
                }
            }
            // log.debug "Music Providers: ${items}"
            if(!state?.musicProviders || items != state?.musicProviders) { state?.musicProviders = items }
            updTsVal("musicProviderUpdDt")
        }
    } catch (ex) {
        respExceptionHandler(ex, "getMusicProviders", true)
    }
    return items
}

private getOtherData() {
    stateMigrationChk()
    getDoNotDisturb()
    getBluetoothDevices()
    getMusicProviders()
    // getCustomerData()
    // getAlexaSkills()
}

private getBluetoothDevices() {
    if(state?.websocketActive && state?.bluetoothData && getLastTsValSecs("bluetoothUpdDt") < 3600) { return }
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/bluetooth",
        query: [cached: true, _: new Date()?.getTime()],
        headers: [ Cookie: getCookieVal(), csrf: getCsrfVal()],
        contentType: "application/json"
    ]
    Map btResp = [:]
    try {
        httpGet(params) { response ->
            btResp = response?.data ?: [:]
            // log.debug "Bluetooth Items: ${btResp}"
            state?.bluetoothData = btResp
            updTsVal("bluetoothUpdDt")
        }
    } catch (ex) {
        respExceptionHandler(ex, "getBluetoothDevices", true)
        if(!state?.bluetoothData) { state?.bluetoothData = [:] }
    }
}

def getBluetoothData(serialNumber) {
    // logTrace("getBluetoothData: ${serialNumber}")
    String curConnName = null
    Map btObjs = [:]
    Map btData = state?.bluetoothData ?: [:]
    Map bluData = btData && btData?.bluetoothStates?.size() ? btData?.bluetoothStates?.find { it?.deviceSerialNumber == serialNumber } : [:]
    if(bluData?.size() && bluData?.pairedDeviceList && bluData?.pairedDeviceList?.size()) {
        def bData = bluData?.pairedDeviceList?.findAll { (it?.deviceClass != "GADGET") }
        bData?.findAll { it?.address != null }?.each {
            btObjs[it?.address as String] = it
            if(it?.connected == true) { curConnName = it?.friendlyName as String }
        }
    }
    return [btObjs: btObjs, pairedNames: btObjs?.findAll { it?.value?.friendlyName != null }?.collect { it?.value?.friendlyName?.toString()?.replaceAll("\ufffd", "") } ?: [], curConnName: curConnName?.replaceAll("\ufffd", "")]
}

def getDeviceActivity(serialNum, frc=false) {
    try {
        Map params = [
            uri: getAmazonUrl(),
            path: "/api/activities",
            query: [ size: 5, offset: 1 ],
            headers: [ Cookie: getCookieVal(), csrf: getCsrfVal()],
            contentType: "application/json"
        ]
        Map lastActData = atomicState?.lastDevActivity ?: null
        // log.debug "activityData(IN): $lastActData"
        Integer lastUpdSec = getLastTsValSecs("lastDevActChk")
        // log.debug "lastUpdSec: $lastUpdSec"
        if(frc || lastUpdSec >= 5) {
            updTsVal("lastDevActChk")
            httpGet(params) { response->
                if (response?.data && response?.data?.activities != null) {
                    def lastCommand = response?.data?.activities?.find {
                        (it?.domainAttributes == null || it?.domainAttributes?.startsWith("{")) &&
                        it?.activityStatus?.equals("SUCCESS") &&
                        (it?.utteranceId?.startsWith(it?.sourceDeviceIds?.deviceType) || it?.utteranceId?.startsWith("Vox:")) &&
                        it?.utteranceId?.contains(it?.sourceDeviceIds?.serialNumber)
                    }
                    if (lastCommand) {
                        def lastDescription = new groovy.json.JsonSlurper().parseText(lastCommand?.description)
                        def lastDevice = lastCommand?.sourceDeviceIds?.get(0)
                        lastActData = [ serialNumber: lastDevice?.serialNumber, spokenText: lastDescription?.summary, lastSpokenDt: lastCommand?.creationTimestamp ]
                        atomicState?.lastDevActivity = lastActData
                    }
                }
            }
        }
        if(lastActData && lastActData?.size() && lastActData?.serialNumber == serialNum) {
            // log.debug "activityData(OUT): $lastActData"
            return lastActData
        } else { return null }
    } catch (ex) {
        if(!ex?.message == "Bad Request") {
            respExceptionHandler(ex, "getDeviceActivity")
        }
        // log.error "getDeviceActivity error: ${ex.message}"
    }
}

private getDoNotDisturb() {
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/dnd/device-status-list",
        query: [_: now()],
        headers: [ Cookie: getCookieVal(), csrf: getCsrfVal() ],
        contentType: "application/json",
    ]
    Map dndResp = [:]
    try {
        httpGet(params) { response ->
            dndResp = response?.data ?: [:]
            // log.debug "DoNotDisturb Data: ${dndResp}"
            state?.dndData = dndResp
        }
    } catch (ex) {
        respExceptionHandler(ex, "getDoNotDisturb", true)
        if(!state?.dndData) { state?.dndData = dndResp }
    }

}

def getDndEnabled(serialNumber) {
    // logTrace("getBluetoothData: ${serialNumber}")
    Map sData = state?.dndData ?: [:]
    def dndData = sData?.doNotDisturbDeviceStatusList?.size() ? sData?.doNotDisturbDeviceStatusList?.find { it?.deviceSerialNumber == serialNumber } : [:]
    return (dndData && dndData?.enabled == true)
}

public getAlexaRoutines(autoId=null, utterOnly=false) {
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/behaviors/automations${autoId ? "/${autoId}" : ""}",
        query: [ limit: 100 ],
        headers: [ Cookie: getCookieVal(), csrf: getCsrfVal() ],
        contentType: "application/json"
    ]

    def rtResp = [:]
    try {
        httpGet(params) { response ->
            rtResp = response?.data ?: [:]
            // log.debug "alexaRoutines: $rtResp"
            if(rtResp) {
                if(autoId) {
                    return rtResp
                } else {
                    Map items = [:]
                    Integer cnt = 1
                    if(rtResp?.size()) {
                        rtResp?.findAll { it?.status == "ENABLED" }?.each { item->
                            if(item?.name != null) {
                                items[item?.automationId] = item?.name
                            } else {
                                if(item?.triggers?.size()) {
                                    item?.triggers?.each { trg->
                                        if(trg?.payload?.containsKey("utterance") && trg?.payload?.utterance != null) {
                                            items[item?.automationId] = trg?.payload?.utterance as String
                                        } else {
                                            items[item?.automationId] = "Unlabeled Routine ($cnt)"
                                            cnt++
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // log.debug "routine items: $items"
                    return items
                }
            }
        }
    } catch (ex) {
        respExceptionHandler(ex, "getAlexaRoutines", true)
        return rtResp
    }
}

def executeRoutineById(String routineId) {
    def execDt = now()
    Map routineData = getAlexaRoutines(routineId)
    if(routineData && routineData?.sequence) {
        sendSequenceCommand("ExecuteRoutine", routineData, null)
        // log.debug "Executed Alexa Routine | Process Time: (${(now()-execDt)}ms) | RoutineId: ${routineId}"
        return true
    } else {
        logError("No Routine Data Returned for ID: (${routineId})")
        return false
    }
}

def checkGuardSupport() {
    def execDt = now()
    if(!isAuthValid("checkGuardSupport")) { return }
    def params = [
        uri: getAmazonUrl(),
        path: "/api/phoenix",
        query: [ cached: true, _: new Date().getTime() ],
        headers: [ Cookie: getCookieVal(), csrf: getCsrfVal()],
        contentType: "application/json",
    ]
    execAsyncCmd("get", "checkGuardSupportResponse", params, [execDt: now()])
}

def checkGuardSupportResponse(response, data) {
    // log.debug "checkGuardSupportResponse Resp Size(${response?.data?.toString()?.size()})"
    Boolean guardSupported = false
    try {
        def respLen = response?.data?.toString()?.length() ?: null
        if(isST() && response?.data && respLen && respLen > 485000) {
            logInfo("GuardSupport Response Length: ${respLen}")
            Map minUpdMap = getMinVerUpdsRequired()
            if(!minUpdMap?.updRequired || (minUpdMap?.updItems && !minUpdMap?.updItems?.contains("Echo Speaks Server"))) {
                wakeupServer(false, true, "checkGuardSupport")
                logDebug("Guard Support Check Response is too large for ST... Checking for Guard Support using the Server")
            } else {
                logWarn("Can't check for Guard Support because server version is out of date...  Please update to the latest version...")
            }
            state?.guardDataOverMaxSize = true
            return
        }
        def resp = response?.data ? parseJson(response?.data?.toString()) : null
        if(resp && resp?.networkDetail) {
            def details = parseJson(resp?.networkDetail as String)
            def locDetails = details?.locationDetails?.locationDetails?.Default_Location?.amazonBridgeDetails?.amazonBridgeDetails["LambdaBridge_AAA/OnGuardSmartHomeBridgeService"] ?: null
            if(locDetails && locDetails?.applianceDetails && locDetails?.applianceDetails?.applianceDetails) {
                def guardKey = locDetails?.applianceDetails?.applianceDetails?.find { it?.key?.startsWith("AAA_OnGuardSmartHomeBridgeService_") }
                def guardData = locDetails?.applianceDetails?.applianceDetails[guardKey?.key]
                // log.debug "Guard: ${guardData}"
                if(guardData?.modelName == "REDROCK_GUARD_PANEL") {
                    state?.guardData = [
                        entityId: guardData?.entityId,
                        applianceId: guardData?.applianceId,
                        friendlyName: guardData?.friendlyName,
                    ]
                    guardSupported = true
                } else { logError("checkGuardSupportResponse Error | No data received...") }
            }
        } else { logError("checkGuardSupportResponse Error | No data received...") }
    } catch (ex) {
        if(ex instanceof groovyx.net.http.HttpResponseException ) {
            logError("checkGuardSupportResponse Response Exception | Status: (${ex?.getResponse()?.getStatus()}) | Msg: ${ex?.getMessage()}")
        } else if(ex instanceof java.net.SocketTimeoutException) {
            logError("checkGuardSupportResponse Response Socket Timeout | Msg: ${ex?.getMessage()}")
        } else if(ex instanceof java.net.UnknownHostException) {
            logError("checkGuardSupportResponse HostName Not Found | Msg: ${ex?.getMessage()}")
        } else if(ex instanceof org.apache.http.conn.ConnectTimeoutException) {
            logError("checkGuardSupportResponse Request Timeout | Msg: ${ex?.getMessage()}")
        } else { logError("checkGuardSupportResponse Exception: ${ex}") }
    }
    state?.alexaGuardSupported = guardSupported
    updTsVal("lastGuardSupChkDt")
    state?.guardDataSrc = "app"
    if(guardSupported) getGuardState()
}

def checkGuardSupportFromServer() {
    if(!isAuthValid("checkGuardSupportFromServer")) { return }
    def params = [
        uri: getServerHostURL(),
        path: "/agsData",
        requestContentType: "application/json",
        contentType: "application/json",
    ]
    execAsyncCmd("get", "checkGuardSupportServerResponse", params, [execDt: now()])
}

def checkGuardSupportServerResponse(response, data) {
    Boolean guardSupported = false
    try {
        def resp = response?.data ? parseJson(response?.data?.toString()) : null
        // log.debug "GuardSupport Server Response: ${resp}"
        if(resp && resp?.guardData) {
            // log.debug "AGS Server Resp: ${resp?.guardData}"
            state?.guardData = resp?.guardData
            guardSupported = true
        } else { logError("checkGuardSupportServerResponse Error | No data received...") }
    } catch (ex) {
        if(ex instanceof groovyx.net.http.HttpResponseException ) {
            logError("checkGuardSupportServerResponse Response Exception | Status: (${ex?.getResponse()?.getStatus()}) | Msg: ${ex?.getMessage()}")
        } else if(ex instanceof java.net.SocketTimeoutException) {
            logError("checkGuardSupportServerResponse Response Socket Timeout | Msg: ${ex?.getMessage()}")
        } else if(ex instanceof java.net.UnknownHostException) {
            logError("checkGuardSupportServerResponse HostName Not Found | Msg: ${ex?.getMessage()}")
        } else if(ex instanceof org.apache.http.conn.ConnectTimeoutException) {
            logError("checkGuardSupportServerResponse Request Timeout | Msg: ${ex?.getMessage()}")
        } else { logError("checkGuardSupportServerResponse Exception: ${ex}") }
    }
    state?.alexaGuardSupported = guardSupported
    state?.guardDataOverMaxSize = guardSupported
    state?.guardDataSrc = "server"
    updTsVal("lastGuardSupChkDt")
    if(guardSupported) getGuardState()
}

private getGuardState() {
    if(!isAuthValid("getGuardState")) { return }
    if(!state?.alexaGuardSupported) { logError("Alexa Guard is either not enabled. or not supported by any of your devices"); return; }
    Map params = [
        uri: getAmazonUrl(),
        path: "/api/phoenix/state",
        headers: [ Cookie: getCookieVal(), csrf: getCsrfVal() ],
        contentType: "application/json",
        body: [ stateRequests: [ [entityId: state?.guardData?.applianceId, entityType: "APPLIANCE" ] ] ]
    ]
    try {
        httpPostJson(params) { resp ->
            Map respData = resp?.data ?: null
            if(respData && respData?.deviceStates && respData?.deviceStates[0] && respData?.deviceStates[0]?.capabilityStates) {
                def guardStateData = parseJson(respData?.deviceStates[0]?.capabilityStates as String)
                state?.alexaGuardState = guardStateData?.value[0] ? guardStateData?.value[0] : guardStateData?.value
                settingUpdate("alexaGuardAwayToggle", ((state?.alexaGuardState == "ARMED_AWAY") ? "true" : "false"), "bool")
                logDebug("Alexa Guard State: (${state?.alexaGuardState})")
                updTsVal("lastGuardStateChkDt")
            }
            // log.debug "GuardState resp: ${respData}"
        }
    } catch (ex) {
        respExceptionHandler(ex, "getGuardState", true)
    }
}

private setGuardState(guardState) {
    def execTime = now()
    if(!isAuthValid("setGuardState")) { return }
    if(!state?.alexaGuardSupported) { logError("Alexa Guard is either not enabled. or not supported by any of your devices"); return; }
    guardState = guardStateConv(guardState)
    logDebug("setAlexaGuard($guardState)")
    try {
        def body = new groovy.json.JsonOutput()?.toJson([ controlRequests: [ [ entityId: state?.guardData?.applianceId as String, entityType: "APPLIANCE", parameters: [action: "controlSecurityPanel", armState: guardState as String ] ] ] ])
        Map params = [
            uri: getAmazonUrl(),
            path: "/api/phoenix/state",
            headers: [cookie: getCookieVal(), csrf: getCsrfVal()],
            contentType: "application/json",
            body: body?.toString()
        ]
        httpPutJson(params) { response ->
            def resp = response?.data ?: null
            if(resp && !resp?.errors?.size() && resp?.controlResponses && resp?.controlResponses[0] && resp?.controlResponses[0]?.code && resp?.controlResponses[0]?.code == "SUCCESS") {
                logInfo("Alexa Guard set to (${guardState}) Successfully | (${(now()-execTime)}ms)")
                state?.alexaGuardState = guardState
                updTsVal("lastGuardStateUpdDt")
                updGuardActionTrig()
            } else { logError("Failed to set Alexa Guard to (${guardState}) | Reason: ${resp?.errors ?: null}") }
        }
    } catch (ex) {
        respExceptionHandler(ex, "setGuardState", true)
    }
}

private getAlexaSkills() {
    def execDt = now()
    log.debug "getAlexaSkills"
    if(!isAuthValid("getAlexaSkills") || state?.amazonCustomerData) { return }
    if(state?.skillDataMap && getLastTsValSecs("skillDataUpdDt") < 3600) { return }
    Map params = [
        uri: "https://skills-store.${getAmazonDomain()}",
        path: "/app/secure/your-skills-page?deviceType=app&ref-suffix=evt_sv_ub&pfm=${state?.amazonCustomerData?.marketPlaceId}&cor=US&lang=en-us&_=${now()}",
        headers: [
            Accept: "application/vnd+amazon.uitoolkit+json;ns=1;fl=0",
            Origin: getAmazonUrl(),
            cookie: getCookieVal(),
            csrf: getCsrfVal()
        ],
        contentType: "application/json",
    ]
    try {
        httpGet(params) { response->
            def respData = response?.data ?: null
            log.debug "respData: $respData"
            // log.debug respData[3]?.contents[3]?.contents?.products

            // updTsVal("skillDataUpdDt")
        }
    } catch (ex) {
        log.error "getAlexaSkills Exception: ${ex}"
        // respExceptionHandler(ex, "getAlexaSkills", true)
    }
}

def respExceptionHandler(ex, String mName, ignOn401=false, ignNullMsg=false) {
    if(ex instanceof groovyx.net.http.HttpResponseException ) {
        Integer sCode = ex?.getResponse()?.getStatus()
        def rData = ex?.getResponse()?.getData()
        def errMsg = ex?.getMessage()
        if(sCode == 401) {
            if(ignOn401) authValidationEvent(false, "${mName}_${status}")
        } else if (sCode == 400) {
            switch(errMsg) {
                case "Bad Request":
                    logError("${mName} | Improperly formatted request sent to Amazon | Msg: ${errMsg}")
                    break
                case "Rate Exceeded":
                    logError("${mName} | Amazon is currently rate-limiting your requests | Msg: ${errMsg}")
                    break
                default:
                    logError("${mName} | 400 Error | Msg: ${errMsg}")
                    break
            }
        } else if(sCode == 429) {
            logWarn("${mName} | Too Many Requests Made to Amazon | Msg: ${errMsg}")
        } else {
            logError("${mName} | Response Exception | Status: (${sCode}) | Msg: ${errMsg}")
        }
    } else if(ex instanceof java.net.SocketTimeoutException) {
        logError("${mName} | Response Socket Timeout (Possibly an Amazon Issue) | Msg: ${ex?.getMessage()}")
    } else if(ex instanceof org.apache.http.conn.ConnectTimeoutException) {
        logError("${mName} | Request Timeout | Msg: ${ex?.getMessage()}")
    } else if(ex instanceof java.net.UnknownHostException) {
        logError("${mName} | HostName Not Found (Possibly an Amazon/Internet Issue) | Msg: ${ex?.getMessage()}")
    } else if(ex instanceof java.net.NoRouteToHostException) {
        logError("${mName} | No Route to Connection (Possibly a Local Internet Issue) | Msg: ${ex?.getMessage()}")
    } else { logError("${mName} Exception: ${ex}") }
}

private guardStateConv(gState) {
    switch(gState) {
        case "disarm":
        case "off":
        case "stay":
        case "home":
        case "ARMED_STAY":
            return "ARMED_STAY"
        case "away":
        case "ARMED_AWAY":
            return "ARMED_AWAY"
        default:
            return "ARMED_STAY"
    }
}

String getAlexaGuardStatus() {
    return state?.alexaGuardState ?: null
}

Boolean getAlexaGuardSupported() {
    return (state?.alexaGuardSupported == true) ? true : false
}

public updGuardActionTrig() {
    def acts = getActionApps()
    if(acts?.size()) { acts?.each { aa-> aa?.guardEventHandler(state?.alexaGuardState) } }
}

public setGuardHome() {
    setGuardState("ARMED_STAY")
}

public setGuardAway() {
    setGuardState("ARMED_AWAY")
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

private getEchoDevices() {
    stateMigrationChk()
    if(!isAuthValid("getEchoDevices")) { return }
    def params = [
        uri: getAmazonUrl(),
        path: "/api/devices-v2/device",
        query: [ cached: true, _: new Date().getTime() ],
        headers: [ Cookie: getCookieVal(), csrf: getCsrfVal() ],
        contentType: "application/json"
    ]
    state?.deviceRefreshInProgress = true
    state?.refreshDeviceData = false
    execAsyncCmd("get", "echoDevicesResponse", params, [execDt: now()])
}

def echoDevicesResponse(response, data) {
    List ignoreTypes = getDeviceIgnoreData() ?: ["A1DL2DVDQVK3Q", "A21Z3CGI8UIP0F", "A2825NDLA7WDZV", "A2IVLV5VM2W81", "A2TF17PFR55MTB", "A1X7HJX9QL16M5", "A2T0P32DY3F7VB", "A3H674413M2EKB", "AILBSA2LNTOYL"]
    List removeKeys = ["appDeviceList", "charging", "macAddress", "deviceTypeFriendlyName", "registrationId", "remainingBatteryLevel", "postalCode", "language"]
    List removeCaps = [
        "SUPPORTS_CONNECTED_HOME", "SUPPORTS_CONNECTED_HOME_ALL", "SUPPORTS_CONNECTED_HOME_CLOUD_ONLY", "ALLOW_LOG_UPLOAD", "FACTORY_RESET_DEVICE", "DIALOG_INTERFACE_VERSION",
        "SUPPORTS_SOFTWARE_VERSION", "REQUIRES_OOBE_FOR_SETUP", "DEREGISTER DEVICE", "PAIR_REMOTE", "SET_LOCALE", "DEREGISTER_FACTORY_RESET"
    ]
    try {
        // log.debug "json response is: ${response.json}"
        state?.deviceRefreshInProgress=false
        List eDevData = response?.json?.devices ?: []
        Map echoDevices = [:]
        if(eDevData?.size()) {
            eDevData?.each { eDevice->
                if (!(eDevice?.deviceType in ignoreTypes) && !eDevice?.accountName?.startsWith("This Device")) {
                    removeKeys?.each { rk-> eDevice?.remove(rk as String) }
                    eDevice?.capabilities = eDevice?.capabilities?.findAll { !(it in removeCaps) }?.collect { it as String }
                    if (eDevice?.deviceOwnerCustomerId != null) { state?.deviceOwnerCustomerId = eDevice?.deviceOwnerCustomerId }
                    echoDevices[eDevice?.serialNumber] = eDevice
                }
            }
        }
        // log.debug "echoDevices: ${echoDevices}"
        def musicProvs = state?.musicProviders ?: getMusicProviders(true)
        receiveEventData([echoDevices: echoDevices, musicProviders: musicProvs, execDt: data?.execDt], "Groovy")
    } catch (ex) {
        respExceptionHandler(ex, "echoDevicesResponse")
    }
}

private getDeviceIgnoreData() {
    Map dData = deviceSupportMap()?.types ?: [:]
    if(dData?.size()) {
        List o = dData?.findAll { it?.value?.ignore == true }?.collect { it?.key as String }
        // log.debug "devTypeIgnoreData: ${o}"
        return o
    }
    return null
}

def getUnknownDevices() {
    List items = []
    state?.unknownDevices?.each {
        it?.description = "What kind of device/model?(PLEASE UPDATE THIS)"
        if(items?.size() < 5) items?.push(it)
    }
    return items
}

def receiveEventData(Map evtData, String src) {
    try {
        if(checkIfCodeUpdated()) {
            logWarn("Possible Code Version Change Detected... Device Updates will occur on next cycle.")
            return
        }
        // log.debug "musicProviders: ${evtData?.musicProviders}"
        logTrace("evtData(Keys): ${evtData?.keySet()}")
        if (evtData?.keySet()?.size()) {
            List ignoreTheseDevs = settings?.echoDeviceFilter ?: []
            Boolean onHeroku = (getServerItem("onHeroku") == true && !getServerItem("isLocal") == true)

            //Check for minimum versions before processing
            Map updReqMap = getMinVerUpdsRequired()
            Boolean updRequired = updReqMap?.updRequired
            List updRequiredItems = updReqMap?.updItems

            if (evtData?.echoDevices?.size()) {
                def execTime = evtData?.execDt ? (now()-evtData?.execDt) : 0
                Map echoDeviceMap = [:]
                Map allEchoDevices = [:]
                Map skippedDevices = [:]
                List unknownDevices = []
                List curDevFamily = []
                Integer cnt = 0
                String devAcctId = null
                evtData?.echoDevices?.each { echoKey, echoValue->
                    devAcctId = echoValue?.deviceAccountId
                    logTrace("echoDevice | $echoKey | ${echoValue}")
                    // logDebug("echoDevice | ${echoValue?.accountName}", false)
                    allEchoDevices[echoKey] = [name: echoValue?.accountName]
                    // log.debug "name: ${echoValue?.accountName}"
                    Map familyAllowed = isFamilyAllowed(echoValue?.deviceFamily as String)
                    Map deviceStyleData = getDeviceStyle(echoValue?.deviceFamily as String, echoValue?.deviceType as String)
                    // log.debug "deviceStyle: ${deviceStyleData}"
                    Boolean isBlocked = (deviceStyleData?.blocked || familyAllowed?.reason == "Family Blocked")
                    Boolean isInIgnoreInput = (echoValue?.serialNumber in settings?.echoDeviceFilter)
                    Boolean allowTTS = (deviceStyleData?.caps && deviceStyleData?.caps?.contains("t"))
                    Boolean isMediaPlayer = (echoValue?.capabilities?.contains("AUDIO_PLAYER") || echoValue?.capabilities?.contains("AMAZON_MUSIC") || echoValue?.capabilities?.contains("TUNE_IN") || echoValue?.capabilities?.contains("PANDORA") || echoValue?.capabilities?.contains("I_HEART_RADIO") || echoValue?.capabilities?.contains("SPOTIFY"))
                    Boolean volumeSupport = (echoValue?.capabilities.contains("VOLUME_SETTING"))
                    Boolean unsupportedDevice = ((familyAllowed?.ok == false && familyAllowed?.reason == "Unknown Reason") || isBlocked == true)
                    Boolean bypassBlock = (settings?.bypassDeviceBlocks == true && !isInIgnoreInput)

                    if(!bypassBlock && (familyAllowed?.ok == false || isBlocked == true || (!allowTTS && !isMediaPlayer) || isInIgnoreInput)) {
                        logDebug("familyAllowed(${echoValue?.deviceFamily}): ${familyAllowed?.ok} | Reason: ${familyAllowed?.reason} | isBlocked: ${isBlocked} | deviceType: ${echoValue?.deviceType} | tts: ${allowTTS} | volume: ${volumeSupport} | mediaPlayer: ${isMediaPlayer}")
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
                                logDebug("skipping ${echoValue?.accountName} because it is in the do not use list...")
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
                    // if(isBypassBlock && familyAllowed?.reason == "Family Blocked" || isBlocked == true) { return }

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
                    permissions["announce"] = (deviceStyleData?.caps && deviceStyleData?.caps?.contains("a"))
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
                    permissions["guardSupported"] = (echoValue?.capabilities?.contains("TUPLE"))
                    permissions["isEchoDevice"] = (echoValue?.deviceFamily in ["KNIGHT", "ROOK", "ECHO"])
                    echoValue["guardStatus"] = (state?.alexaGuardSupported && state?.alexaGuardState) ? state?.alexaGuardState as String : (permissions?.guardSupported ? "Unknown" : "Not Supported")
                    echoValue["musicProviders"] = evtData?.musicProviders
                    echoValue["permissionMap"] = permissions
                    echoValue["hasClusterMembers"] = (echoValue?.clusterMembers && echoValue?.clusterMembers?.size() > 0) ?: false

                    if(deviceStyleData?.name?.toString()?.toLowerCase()?.contains("unknown")) {
                        unknownDevices?.push([
                            name: echoValue?.accountName,
                            family: echoValue?.deviceFamily,
                            type: echoValue?.deviceType,
                            permissions: permissions?.findAll {it?.value == true}?.collect {it?.key as String}?.join(", ")?.toString()
                        ])
                    }
                    // echoValue["mainAccountCommsId"] = state?.accountCommIds?.find { it?.value?.signedInUser == true && it?.value?.isChild == false }?.key as String ?: null
                    // logWarn("Device Permisions | Name: ${echoValue?.accountName} | $permissions")

                    echoDeviceMap[echoKey] = [
                        name: echoValue?.accountName, online: echoValue?.online, family: echoValue?.deviceFamily, serialNumber: echoKey,
                        style: echoValue?.deviceStyle, type: echoValue?.deviceType, mediaPlayer: isMediaPlayer, announceSupport: permissions?.announce,
                        ttsSupport: allowTTS, volumeSupport: volumeSupport, clusterMembers: echoValue?.clusterMembers,
                        musicProviders: evtData?.musicProviders?.collect{ it?.value }?.sort()?.join(", "), supported: (unsupportedDevice != true)
                    ]

                    String dni = [app?.id, "echoSpeaks", echoKey].join("|")
                    def childDevice = getChildDevice(dni)
                    String devLabel = "${settings?.addEchoNamePrefix != false ? "Echo - " : ""}${echoValue?.accountName}${echoValue?.deviceFamily == "WHA" ? " (WHA)" : ""}"
                    String childHandlerName = "Echo Speaks Device"
                    Boolean autoRename = (settings?.autoRenameDevices != false)
                    if (!childDevice) {
                        // log.debug "childDevice not found | autoCreateDevices: ${settings?.autoCreateDevices}"
                        if(settings?.autoCreateDevices != false) {
                            try{
                                logInfo("Creating NEW Echo Speaks Device!!! | Device Label: ($devLabel)${(settings?.bypassDeviceBlocks && unsupportedDevice) ? " | (UNSUPPORTED DEVICE)" : "" }")
                                childDevice = addChildDevice("tonesto7", childHandlerName, dni, null, [name: childHandlerName, label: devLabel, completedSetup: true])
                            } catch(ex) {
                                logError("AddDevice Error! | ${ex}")
                            }
                        }
                    }
                    if(childDevice) {
                        //Check and see if name needs a refresh
                        String curLbl = childDevice?.getLabel()
                        if(autoRename && childDevice?.name as String != childHandlerName) { childDevice?.name = childHandlerName as String }
                        // log.debug "curLbl: ${curLbl} | newLbl: ${devLabel} | autoRename: ${autoRename}"
                        if(autoRename && (curLbl != devLabel)) {
                            logDebug("Amazon Device Name Change Detected... Updating Device Name to (${devLabel}) | Old Name: (${curLbl})")
                            childDevice?.setLabel(devLabel as String)
                        }
                        // logInfo("Sending Device Data Update to ${devLabel} | Last Updated (${getLastTsValSecs("lastDevDataUpdDt")}sec ago)")
                        childDevice?.updateDeviceStatus(echoValue)
                        updCodeVerMap("echoDevice", childDevice?.devVersion()) // Update device versions in codeVersions state Map
                    }
                    curDevFamily?.push(echoValue?.deviceStyle?.name)
                }
                if(!isST()) {
                    String wsChildHandlerName = "Echo Speaks WS"
                    def oldWsDev = getChildDevice("echoSpeaks_websocket")
                    if(oldWsDev) { isST() ? deleteChildDevice("echoSpeaks_websocket", true) : deleteChildDevice("echoSpeaks_websocket") }
                    def wsDevice = getChildDevice("${app?.getId()}|echoSpeaks_websocket")
                    if(!wsDevice) { addChildDevice("tonesto7", wsChildHandlerName, "${app?.getId()}|echoSpeaks_websocket", null, [name: wsChildHandlerName, label: "Echo Speaks - WebSocket", completedSetup: true]) }
                    updCodeVerMap("echoDeviceWs", wsDevice?.devVersion())
                }
                logDebug("Device Data Received and Updated for (${echoDeviceMap?.size()}) Alexa Devices | Took: (${execTime}ms) | Last Refreshed: (${(getLastTsValSecs("lastDevDataUpdDt")/60).toFloat()?.round(1)} minutes)")
                updTsVal("lastDevDataUpdDt")
                state?.echoDeviceMap = echoDeviceMap
                state?.allEchoDevices = allEchoDevices
                state?.skippedDevices = skippedDevices
                state?.deviceStyleCnts = curDevFamily?.countBy { it }
                state?.unknownDevices = unknownDevices
            } else {
                log.warn "No Echo Device Data Sent... This may be the first transmission from the service after it started up!"
            }
            state?.remove("tempDevSupData")
            if(updRequired) {
                logWarn("CODE UPDATES REQUIRED: Echo Speaks Integration may not function until the following items are ALL Updated ${updRequiredItems}...")
                appUpdateNotify()
            }
            if(state?.installData?.sentMetrics != true) { runIn(900, "sendInstallData", [overwrite: false]) }
        }
    } catch(ex) {
        logError("receiveEventData Error: ${ex}")
        incrementCntByKey("appErrorCnt")
    }
}

private Map getMinVerUpdsRequired() {
    Boolean updRequired = false
    List updItems = []
    Map codeItems = [server: "Echo Speaks Server", echoDevice: "Echo Speaks Device", actionApp: "Echo Speaks Actions", zoneApp: "Echo Speaks Zones"]
    if(!isST()) { codeItems?.wsDevice = "Echo Speaks Websocket" }
    Map codeVers = state?.codeVersions ?: [:]
    codeVers?.each { k,v->
        if(codeItems?.containsKey(k as String) && v != null && (versionStr2Int(v) < minVersions()[k as String])) { updRequired = true; updItems?.push(codeItems[k]); }
    }
    return [updRequired: updRequired, updItems: updItems]
}

public getDeviceStyle(String family, String type) {
    Map typeData = deviceSupportMap()?.types[type] ?: [:]
    if(typeData) {
        return typeData
    } else { return [name: "Echo Unknown $type", image: "unknown", allowTTS: false] }
}

public Map getDeviceFamilyMap() {
    if(!state?.appData || !state?.appData?.deviceFamilies) { checkVersionData(true) }
    return state?.appData?.deviceFamilies ?: [:]
}

private getDevicesFromSerialList(serialList) {
    //logTrace("getDevicesFromSerialList called with: ${serialList}")
    if (serialList == null) {
       logDebug("SerialNumberList is null")
       return null;
    }
    List devs = []
    serialList?.each { ser ->
        def d = findEchoDevice(ser)
        if(d) devs?.push(d)
    }
    //log.debug "Device list: ${devs}"
    return devs
}

// This is called by the device handler to send playback data to cluster members
public sendPlaybackStateToClusterMembers(whaKey, data) {
    //logTrace("sendPlaybackStateToClusterMembers: key: ${ whaKey}")
    try {
        Map echoDeviceMap = state?.echoDeviceMap
        def whaMap = echoDeviceMap[whaKey]
        def clusterMembers = whaMap?.clusterMembers

        if (clusterMembers) {
            def clusterMemberDevices = getDevicesFromSerialList(clusterMembers)
            if(clusterMemberDevices) {
                clusterMemberDevices?.each { it?.playbackStateHandler(data, true) }
            }
        } else {
            // The lookup will fail during initial refresh because echoDeviceMap isn't available yet
            //log.debug "sendPlaybackStateToClusterMembers: no data found for ${whaKey} (first refresh?)"
        }
    } catch (ex) {
        log.error "sendPlaybackStateToClusterMembers Error: ${ex}"
    }
}

private removeDevices(all=false) {
    try {
        settingUpdate("cleanUpDevices", "false", "bool")
        List devList = getDeviceList(true)?.collect { String dni = [app?.id, "echoSpeaks", it?.key].join("|") }
        def items = app.getChildDevices()?.findResults { (all || (!all && !devList?.contains(it?.deviceNetworkId as String))) ? it?.deviceNetworkId as String : null }
        logWarn("removeDevices(${all ? "all" : ""}) | In Use: (${all ? 0 : devList?.size()}) | Removing: (${items?.size()})", true)
        if(items?.size() > 0) {
            Boolean isST = isST()
            items?.each {  isST ? deleteChildDevice(it as String, true) : deleteChildDevice(it as String) }
        }
    } catch (ex) { logError("Device Removal Failed: ${ex}") }
}

Map sequenceBuilder(cmd, val) {
    def seqJson = null
    if (cmd instanceof Map) {
        seqJson = cmd?.sequence ?: cmd
    } else { seqJson = ["@type": "com.amazon.alexa.behaviors.model.Sequence", "startNode": createSequenceNode(cmd, val)] }
    Map seqObj = [behaviorId: (seqJson?.sequenceId ? cmd?.automationId : "PREVIEW"), sequenceJson: new groovy.json.JsonOutput().toJson(seqJson) as String, status: "ENABLED"]
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
        Boolean remDevSpecifics = false
        Map seqNode = [
            "@type": "com.amazon.alexa.behaviors.model.OpaquePayloadOperationNode",
            operationPayload: [
                deviceType: deviceData?.deviceType ?: null,
                deviceSerialNumber: deviceData?.serialNumber ?: null,
                locale: (settings?.regionLocale ?: "en-US"),
                customerId: state?.deviceOwnerCustomerId
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
                if(settings?.test_announceDevices) {
                    Map eDevs = state?.echoDeviceMap
                    settings?.test_announceDevices?.each { dev->
                        announceDevs?.push([deviceTypeId: eDevs[dev]?.type, deviceSerialNumber: dev])
                    }
                }
                seqNode?.operationPayload?.target = [ customerId : state?.deviceOwnerCustomerId, devices: announceDevs ]
                break
            default:
                return
        }
        if(remDevSpecifics) {
            seqNode?.operationPayload?.remove('deviceType')
            seqNode?.operationPayload?.remove('deviceSerialNumber')
            seqNode?.operationPayload?.remove('locale')
        }
        // log.debug "seqNode: $seqNode"
        return seqNode
    } catch (ex) {
        logError("createSequenceNode Exception: ${ex}")
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
    } else { logError("execAsyncCmd Error | Missing a required parameter") }
}

private sendAmazonCommand(String method, Map params, Map otherData=null) {
    try {
        def rData = null
        def rStatus = null
        switch(method) {
            case "POST":
                httpPostJson(params) { response->
                    rData = response?.data ?: null
                    rStatus = response?.status
                }
                break
            case "PUT":
                if(params?.body) { params?.body = new groovy.json.JsonOutput().toJson(params?.body) }
                httpPutJson(params) { response->
                    rData = response?.data ?: null
                    rStatus = response?.status
                }
                break
            case "DELETE":
                httpDelete(params) { response->
                    rData = response?.data ?: null
                    rStatus = response?.status
                }
                break
        }
        logDebug("sendAmazonCommand | Status: (${rStatus})${rData != null ? " | Response: ${rData}" : ""} | ${otherData?.cmdDesc} was Successfully Sent!!!")
    } catch (ex) {
        respExceptionHandler(ex, "${otherData?.cmdDesc}", true)
    }
}

private sendSequenceCommand(type, command, value) {
    // logTrace("sendSequenceCommand($type) | command: $command | value: $value", true)
    Map seqObj = sequenceBuilder(command, value)
    sendAmazonCommand("POST", [
        uri: getAmazonUrl(),
        path: "/api/behaviors/preview",
        headers: [ Cookie: getCookieVal(), csrf: getCsrfVal() ],
        contentType: "application/json",
        body: new groovy.json.JsonOutput().toJson(seqObj)
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
    // logTrace("healthCheck", true)
    checkVersionData()
    if(checkIfCodeUpdated()) {
        logWarn("Code Version Change Detected... Health Check will occur on next cycle.")
        return
    }
    validateCookie()
    if(getLastTsValSecs("lastCookieRrshDt") > cookieRefreshSeconds()) {
        runCookieRefresh()
    } else if (getLastTsValSecs("lastGuardSupChkDt") > 43200) {
        checkGuardSupport()
    } else if(getLastTsValSecs("lastServerWakeDt") > 86400 && serverConfigured()) { wakeupServer(false, false, "healthCheck") }
    if(!isST() && getSocketDevice()?.isSocketActive() != true) { getSocketDevice()?.triggerInitialize() }
    if(state?.isInstalled && getLastTsValSecs("lastMetricUpdDt") > (3600*24)) { runIn(30, "sendInstallData", [overwrite: true]) }
    if(!getOk2Notify()) { return }
    missPollNotify((settings?.sendMissedPollMsg == true), (settings?.misPollNotifyMsgWaitVal as Integer ?: 3600))
    appUpdateNotify()
    if(advLogsActive()) { logsDisable() }
}

Boolean advLogsActive() { return (settings?.logDebug || settings?.logTrace) }
public logsEnabled() { if(advLogsActive() && getTsVal("logsEnabled")) { updTsVal("logsEnabled") } }
public logsDisable() { Integer dtSec = getLastTsValSecs("logsEnabled", null); if(dtSec && (dtSec > 3600*6) && advLogsActive()) { settingUpdate("logDebug", "false", "bool"); settingUpdate("logTrace", "false", "bool"); remTsVal("logsEnabled"); } }

private missPollNotify(Boolean on, Integer wait) {
    logTrace("missPollNotify() | on: ($on) | wait: ($wait) | getLastDevicePollSec: (${getLastTsValSecs("lastDevDataUpdDt")}) | misPollNotifyWaitVal: (${settings?.misPollNotifyWaitVal}) | getLastMisPollMsgSec: (${getLastTsValSecs("lastMissedPollMsgDt")})")
    if(!on || !wait) { return; }
    if(getLastTsValSecs("lastDevDataUpdDt", 840) <= (settings?.misPollNotifyWaitVal as Integer ?: 2700)) {
        state?.missPollRepair = false
        return
    } else {
        if(state?.missPollRepair == false) {
            state?.missPollRepair = true
            initialize()
            return
        }
        if(!(getLastTsValSecs("lastMissedPollMsgDt") > wait?.toInteger())) { return; }
        String msg = ""
        if(state?.authValid) {
            msg = "\nThe Echo Speaks app has NOT received any device data from Amazon in the last (${getLastTsValSecs("lastDevDataUpdDt")}) seconds.\nThere maybe an issue with the scheduling.  Please open the app and press Done/Save."
        } else { msg = "\nThe Amazon login info has expired!\nPlease open the heroku amazon authentication page and login again to restore normal operation." }
        logWarn("${msg.toString().replaceAll("\n", " ")}")
        if(sendMsg("${app.name} ${state?.authValid ? "Data Refresh Issue" : "Amazon Login Issue"}", msg)) {
            updTsVal("lastMissedPollMsgDt")
        }
        if(state?.authValid) {
            (isST() ? app?.getChildDevices(true) : getChildDevices())?.each { cd-> cd?.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: true, isStateChange: true) }
        }
    }
}

private appUpdateNotify() {
    Boolean on = (settings?.sendAppUpdateMsg != false)
    Boolean appUpd = appUpdAvail()
    Boolean actUpd = actionUpdAvail()
    Boolean zoneUpd = zoneUpdAvail()
    Boolean echoDevUpd = echoDevUpdAvail()
    Boolean socketUpd = socketUpdAvail()
    Boolean servUpd = serverUpdAvail()
    logDebug("appUpdateNotify() | on: (${on}) | appUpd: (${appUpd}) | actUpd: (${appUpd}) | zoneUpd: (${zoneUpd}) | echoDevUpd: (${echoDevUpd}) | servUpd: (${servUpd}) | getLastUpdMsgSec: ${getLastTsValSecs("lastUpdMsgDt")} | updNotifyWaitVal: ${settings?.updNotifyWaitVal}")
    if(settings?.updNotifyWaitVal && getLastTsValSecs("lastUpdMsgDt") > settings?.updNotifyWaitVal?.toInteger()) {
        if(on && (appUpd || actUpd || zoneUpd || echoDevUpd || socketUpd || servUpd)) {
            state?.updateAvailable = true
            def str = ""
            str += !appUpd ? "" : "\nEcho Speaks App: v${state?.appData?.versions?.mainApp?.ver?.toString()}"
            str += !actUpd ? "" : "\nEcho Speaks Actions: v${state?.appData?.versions?.actionApp?.ver?.toString()}"
            str += !zoneUpd ? "" : "\nEcho Speaks Zones: v${state?.appData?.versions?.zoneApp?.ver?.toString()}"
            str += !echoDevUpd ? "" : "\nEcho Speaks Device: v${state?.appData?.versions?.echoDevice?.ver?.toString()}"
            str += !socketUpd ? "" : "\nEcho Speaks Socket: v${state?.appData?.versions?.wsDevice?.ver?.toString()}"
            str += !servUpd ? "" : "\n${(getServerItem("onHeroku") == true) ? "Heroku Service" : "Node Service"}: v${state?.appData?.versions?.server?.ver?.toString()}"
            sendMsg("Info", "Echo Speaks Update(s) are Available:${str}...\n\nPlease visit the IDE to Update your code...")
            updTsVal("lastUpdMsgDt")
            return
        }
        state?.updateAvailable = false
    }
}

private List codeUpdateItems(shrt=false) {
    Boolean appUpd = appUpdAvail()
    Boolean actUpd = actionUpdAvail()
    Boolean zoneUpd = zoneUpdAvail()
    Boolean devUpd = echoDevUpdAvail()
    Boolean socketUpd = socketUpdAvail()
    Boolean servUpd = serverUpdAvail()
    List updItems = []
    if(appUpd || actUpd || zoneUpd || devUpd || socketUpd || servUpd) {
        if(appUpd) updItems.push("${!shrt ? "\nEcho Speaks " : ""}App: (v${state?.appData?.versions?.mainApp?.ver?.toString()})")
        if(actUpd) updItems.push("${!shrt ? "\nEcho Speaks " : ""}Actions: (v${state?.appData?.versions?.actionApp?.ver?.toString()})")
        if(zoneUpd) updItems.push("${!shrt ? "\nEcho Speaks " : ""}Zones: (v${state?.appData?.versions?.zoneApp?.ver?.toString()})")
        if(devUpd) updItems.push("${!shrt ? "\nEcho Speaks " : "ES "}Device: (v${state?.appData?.versions?.echoDevice?.ver?.toString()})")
        if(socketUpd) updItems.push("${!shrt ? "\nEcho Speaks " : ""}Websocket: (v${state?.appData?.versions?.wsDevice?.ver?.toString()})")
        if(servUpd) updItems.push("${!shrt ? "\n" : ""}Server: (v${state?.appData?.versions?.server?.ver?.toString()})")
    }
    return updItems
}

Boolean pushStatus() { return (settings?.smsNumbers?.toString()?.length()>=10 || settings?.usePush || settings?.pushoverEnabled) ? ((settings?.usePush || (settings?.pushoverEnabled && settings?.pushoverDevices)) ? "Push Enabled" : "Enabled") : null }
Boolean getOk2Notify() {
    Boolean smsOk = (settings?.smsNumbers?.toString()?.length()>=10)
    Boolean pushOk = settings?.usePush
    Boolean notifDevs = (settings?.notif_devs?.size())
    Boolean pushOver = (settings?.pushoverEnabled && settings?.pushoverDevices)
    Boolean daysOk = quietDaysOk(settings?.quietDays)
    Boolean timeOk = quietTimeOk()
    Boolean modesOk = quietModesOk(settings?.quietModes)
    logDebug("getOk2Notify() | smsOk: $smsOk | pushOk: $pushOk | pushOver: $pushOver || daysOk: $daysOk | timeOk: $timeOk | modesOk: $modesOk")
    if(!(smsOk || pushOk || notifDevs || pushOver)) { return false }
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
        // log.debug "quietTimeOk | Start: ${strtTime} | Stop: ${stopTime}"
        if(!isST()) {
            strtTime = toDateTime(strtTime)
            stopTime = toDateTime(stopTime)
        }
        return timeOfDayIsBetween(strtTime, stopTime, new Date(), location?.timeZone) ? false : true
    } else { return true }
}

Boolean quietDaysOk(days) {
    if(days) {
        def dayFmt = new java.text.SimpleDateFormat("EEEE")
        if(location?.timeZone) { dayFmt?.setTimeZone(location?.timeZone) }
        return days?.contains(dayFmt?.format(new Date())) ? false : true
    }
    return true
}

// Sends the notifications based on app settings
public sendMsg(String msgTitle, String msg, Boolean showEvt=true, Map pushoverMap=null, sms=null, push=null) {
    logTrace("sendMsg() | msgTitle: ${msgTitle}, msg: ${msg}, showEvt: ${showEvt}")
    String sentstr = "Push"
    Boolean sent = false
    try {
        String newMsg = "${msgTitle}: ${msg}"
        String flatMsg = newMsg.toString().replaceAll("\n", " ")
        if(!getOk2Notify()) {
            logInfo("sendMsg: Message Skipped During Quiet Time ($flatMsg)")
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
            if(settings?.notif_devs) {
                sentstr = "Notification Devices"
                settings?.notif_devs?.each { it?.deviceNotification(msg as String) }
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
                updTsVal("lastMsgDt")
                logDebug("sendMsg: Sent ${sentstr} (${flatMsg})")
            }
        }
    } catch (ex) {
        incrementCntByKey("appErrorCnt")
        logError("sendMsg $sentstr Exception: ${ex}")
    }
    return sent
}

Boolean childInstallOk() { return (state?.childInstallOkFlag == true) }
String getAppImg(String imgName, frc=false) { return (frc || isST()) ? "https://raw.githubusercontent.com/tonesto7/echo-speaks/${isBeta() ? "beta" : "master"}/resources/icons/${imgName}.png" : "" }
String getPublicImg(String imgName, frc=false) { return (frc || isST()) ? "https://raw.githubusercontent.com/tonesto7/SmartThings-tonesto7-public/master/resources/icons/${imgName}.png" : "" }
String sTS(String t, String i = null) { return isST() ? t : """<h3>${i ? """<img src="${i}" width="42"> """ : ""} ${t?.replaceAll("\\n", "<br>")}</h3>""" }
String pTS(String t, String i = null, bold=true, color=null) { return isST() ? t : "${color ? """<div style="color: $color;">""" : ""}${bold ? "<b>" : ""}${i ? """<img src="${i}" width="42"> """ : ""}${t?.replaceAll("\\n", "<br>")}${bold ? "</b>" : ""}${color ? "</div>" : ""}" }
String inTS(String t, String i = null, color=null, under=true) { return isST() ? t : """${color ? """<div style="color: $color;">""" : ""}${i ? """<img src="${i}" width="42"> """ : ""} ${under ? "<u>" : ""}${t?.replaceAll("\\n", " ")}${under ? "</u>" : ""}${color ? "</div>" : ""}""" }

String actChildName(){ return "Echo Speaks - Actions" }
String zoneChildName(){ return "Echo Speaks - Zones" }
String documentationLink() { return "https://tonesto7.github.io/echo-speaks-docs" }
String textDonateLink() { return "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=HWBN4LB9NMHZ4" }
def updateDocsInput() { href url: documentationLink(), style: "external", required: false, title: inTS("View Documentation", getAppImg("documentation", true)), description: "Tap to proceed", state: "complete", image: getAppImg("documentation")}

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
public pushover_poll(){sendLocationEvent(name:"pushoverManagerCmd",value:"poll",data:[empty:true],isStateChange:true,descriptionText:"Sending Poll Event to Pushover-Manager", display: false, displayed: false)}
public pushover_msg(List devs,Map data){if(devs&&data){sendLocationEvent(name:"pushoverManagerMsg",value:"sendMsg",data:data,isStateChange:true,descriptionText:"Sending Message to Pushover Devices: ${devs}", display: false, displayed: false);}}
public pushover_handler(evt){Map pmd=state?.pushoverManager?:[:];switch(evt?.value){case"refresh":def ed = evt?.jsonData;String id = ed?.appId;Map pA = pmd?.apps?.size() ? pmd?.apps : [:];if(id){pA[id]=pA?."${id}"instanceof Map?pA[id]:[:];pA[id]?.devices=ed?.devices?:[];pA[id]?.appName=ed?.appName;pA[id]?.appId=id;pmd?.apps = pA;};pmd?.sounds=ed?.sounds;break;case "reset":pmd=[:];break;};state?.pushoverManager=pmd;}
//Builds Map Message object to send to Pushover Manager
private buildPushMessage(List devices,Map msgData,timeStamp=false){if(!devices||!msgData){return};Map data=[:];data?.appId=app?.getId();data.devices=devices;data?.msgData=msgData;if(timeStamp){data?.msgData?.timeStamp=new Date().getTime()};pushover_msg(devices,data);}

/******************************************
|       Changelog Logic
******************************************/
Boolean showDonationOk() { return (state?.isInstalled && !atomicState?.installData?.shownDonation && getDaysSinceUpdated() >= 30) ? true : false }

Integer getDaysSinceUpdated() {
    def updDt = atomicState?.installData?.updatedDt ?: null
    if(updDt == null || updDt == "Not Set") {
        updInstData("updatedDt", getDtNow().toString())
        return 0
    } else {
        def start = Date.parse("E MMM dd HH:mm:ss z yyyy", updDt)
        def stop = new Date()
        if(start && stop) {	return (stop - start) }
        return 0
    }
}

String changeLogData() { return getWebData([uri: "https://raw.githubusercontent.com/tonesto7/echo-speaks/${isBeta() ? "beta" : "master"}/resources/changelog.txt", contentType: "text/plain; charset=UTF-8"], "changelog") }
Boolean showChgLogOk() { return (state?.isInstalled && (state?.curAppVer != appVersion() || state?.installData?.shownChgLog != true)) }
def changeLogPage() {
    def execTime = now()
    return dynamicPage(name: "changeLogPage", title: "", nextPage: "mainPage", install: false) {
        section() {
            paragraph title: "Release Notes", pTS(isST() ? "" : "Release Notes", getAppImg("whats_new", true), true), state: "complete", image: getAppImg("whats_new")
            paragraph pTS(changeLogData(), null, false, "gray")
        }
        state?.curAppVer = appVersion()
        updInstData("shownChgLog", true)
    }
}

/******************************************
|    METRIC Logic
******************************************/
String getFbMetricsUrl() { return state?.appData?.settings?.database?.metricsUrl ?: "https://echo-speaks-metrics.firebaseio.com/" }
String getFbConfigUrl() { return state?.appData?.settings?.database?.configUrl ?: "https://echospeaks-config.firebaseio.com/" }
Boolean metricsOk() { (settings?.optOutMetrics != true && state?.appData?.settings?.sendMetrics != false) }
private generateGuid() { if(!state?.appGuid) { state?.appGuid = UUID?.randomUUID().toString() } }
private sendInstallData() { settingUpdate("sendMetricsNow", "false", "bool"); if(metricsOk()) { sendFirebaseData(getFbMetricsUrl(), "/clients/${state?.appGuid}.json", createMetricsDataJson(), "put", "heartbeat"); } }
private removeInstallData() { return removeFirebaseData("/clients/${state?.appGuid}.json") }
private sendFirebaseData(url, path, data, cmdType=null, type=null) { logTrace("sendFirebaseData(${path}, ${data}, $cmdType, $type"); return queueFirebaseData(url, path, data, cmdType, type); }

def queueFirebaseData(url, path, data, cmdType=null, type=null) {
    logTrace("queueFirebaseData(${path}, ${data}, $cmdType, $type")
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
        } else { logWarn("queueFirebaseData UNKNOWN cmdType: ${cmdType}") }

    } catch(ex) { logError("queueFirebaseData (type: $typeDesc) Exception: ${ex}") }
    return result
}

def removeFirebaseData(pathVal) {
    logTrace("removeFirebaseData(${pathVal})")
    Boolean result = true
    try {
        httpDelete(uri: getFbMetricsUrl(), path: pathVal as String) { resp ->
            logDebug("Remove Firebase | resp: ${resp?.status}")
        }
    } catch (ex) {
        if(ex instanceof groovyx.net.http.HttpResponseException ) {
            logError("removeFirebaseData Response Exception: ${ex}")
        } else {
            logError("removeFirebaseData Exception: ${ex}")
            result = false
        }
    }
    return result
}

def processFirebaseResponse(resp, data) {
    logTrace("processFirebaseResponse(${data?.type})")
    Boolean result = false
    String typeDesc = data?.type as String
    try {
        if(resp?.status == 200) {
            logDebug("processFirebaseResponse: ${typeDesc} Data Sent SUCCESSFULLY")
            if(typeDesc?.toString() == "heartbeat") { updTsVal("lastMetricUpdDt") }
            updInstData("sentMetrics", true)
            result = true
        } else if(resp?.status == 400) {
            logError("processFirebaseResponse: 'Bad Request': ${resp?.status}")
        } else { logWarn("processFirebaseResponse: 'Unexpected' Response: ${resp?.status}") }
        if (isST() && resp?.hasError()) { logError("processFirebaseResponse: errorData: ${resp?.errorData} | errorMessage: ${resp?.errorMessage}") }
    } catch(ex) {
        logError("processFirebaseResponse (type: $typeDesc) Exception: ${ex}")
    }
}

def renderMetricData() {
    try {
        def json = new groovy.json.JsonOutput().prettyPrint(createMetricsDataJson())
        render contentType: "application/json", data: json
    } catch (ex) { logError("renderMetricData Exception: ${ex}") }
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
        Map actData = [:]
        def actCnt = 0
        getActionApps()?.each { a-> actData[actCnt] = a?.getActionMetrics(); actCnt++ }
        Map zoneData = [:]
        def zoneCnt = 0
        getZoneApps()?.each { a-> zoneData[zoneCnt] = a?.getZoneMetrics(); zoneCnt++ }
        Map dataObj = [
            guid: state?.appGuid,
            datetime: getDtNow()?.toString(),
            installDt: state?.installData?.dt,
            updatedDt: state?.installData?.updatedDt,
            timeZone: location?.timeZone?.ID?.toString(),
            hubPlatform: getPlatform(),
            authValid: (state?.authValid == true),
            stateUsage: "${stateSizePerc()}%",
            amazonDomain: settings?.amazonDomain,
            serverPlatform: (getServerItem("onHeroku") == true) ? "Cloud" : "Local",
            versions: [app: appVersion(), server: swVer?.server ?: "N/A", actions: swVer?.actionApp ?: "N/A", zones: swVer?.zoneApp ?: "N/A", device: swVer?.echoDevice ?: "N/A", socket: swVer?.wsDevice ?: "N/A"],
            detections: [skippedDevices: getSkippedDevsAnon()],
            actions: actData,
            zones: zoneData,
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
        logError("createMetricsDataJson: Exception: ${ex}")
    }
}

private incrementCntByKey(String key) {
    long evtCnt = state?."${key}" ?: 0
    // evtCnt = evtCnt?.toLong()+1
    evtCnt++
    // logTrace("${key?.toString()?.capitalize()}: $evtCnt", true)
    state?."${key}" = evtCnt?.toLong()
}

// ******************************************
//      APP/DEVICE Version Functions
// ******************************************
Boolean codeUpdIsAvail(String newVer, String curVer, String type) {
    Boolean result = false
    def latestVer
    if(newVer && curVer) {
        List versions = [newVer, curVer]
        if(newVer != curVer) {
            latestVer = versions?.max { a, b ->
                List verA = a?.tokenize('.'); List verB = b?.tokenize('.'); Integer commonIndices = Math.min(verA?.size(), verB?.size());
                for (int i = 0; i < commonIndices; ++i) { if(verA[i]?.toInteger() != verB[i]?.toInteger()) { return verA[i]?.toInteger() <=> verB[i]?.toInteger() }; }
                verA?.size() <=> verB?.size()
            }
            result = (latestVer == newVer) ? true : false
        }
    }
    return result
}

Boolean appUpdAvail() { return (state?.appData?.versions && state?.codeVersions?.mainApp && codeUpdIsAvail(state?.appData?.versions?.mainApp?.ver, state?.codeVersions?.mainApp, "main_app")) }
Boolean actionUpdAvail() { return (state?.appData?.versions && state?.codeVersions?.actionApp && codeUpdIsAvail(state?.appData?.versions?.actionApp?.ver, state?.codeVersions?.actionApp, "action_app")) }
Boolean zoneUpdAvail() { return (state?.appData?.versions && state?.codeVersions?.zoneApp && codeUpdIsAvail(state?.appData?.versions?.zoneApp?.ver, state?.codeVersions?.zoneApp, "zone_app")) }
Boolean echoDevUpdAvail() { return (state?.appData?.versions && state?.codeVersions?.echoDevice && codeUpdIsAvail(state?.appData?.versions?.echoDevice?.ver, state?.codeVersions?.echoDevice, "dev")) }
Boolean socketUpdAvail() { return (!isST() && state?.appData?.versions && state?.codeVersions?.wsDevice && codeUpdIsAvail(state?.appData?.versions?.wsDevice?.ver, state?.codeVersions?.wsDevice, "socket")) }
Boolean serverUpdAvail() { return (state?.appData?.versions && state?.codeVersions?.server && codeUpdIsAvail(state?.appData?.versions?.server?.ver, state?.codeVersions?.server, "server")) }
Integer versionStr2Int(str) { return str ? str.toString()?.replaceAll("\\.", "")?.toInteger() : null }

private checkVersionData(now = false) { //This reads a JSON file from GitHub with version numbers
    def lastUpd = getLastTsValSecs("lastAppDataUpdDt")
    if (now || !state?.appData || (lastUpd > (3600*6))) {
        if(now && (lastUpd < 300)) { return }
        getConfigData()
        getNoticeData()
    }
}

private getConfigData() {
    Map params = [
        uri: "https://raw.githubusercontent.com/tonesto7/echo-speaks/${isBeta() ? "beta" : "master"}/resources/appData.json",
        contentType: "application/json"
    ]
    def data = getWebData(params, "appData", false)
    if(data) {
        state?.appData = data
        updTsVal("lastAppDataUpdDt")
        logDebug("Successfully Retrieved (v${data?.appDataVer}) of AppData Content from GitHub Repo...")
    }
}

private getNoticeData() {
    Map params = [
        uri: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/notices.json",
        contentType: "application/json"
    ]
    def data = getWebData(params, "noticeData", false)
    if(data) {
        state?.noticeData = data
        logDebug("Successfully Retrieved Developer Notices from GitHub Repo...")
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
            logWarn("${desc} file not found")
        } else { logError("getWebData(params: $params, desc: $desc, text: $text) Exception: ${ex}") }
        return "${desc} info not found"
    }
}


// TODO: https://m.media-amazon.com/images/G/01/mobile-apps/dex/ask-tech-docs/ask-soundlibrary._TTH_.json
Map getAvailableSounds() {
    return [
        // Bells and Buzzer
        bells: "bell_02",
        buzzer: "buzzers_pistols_01",
        church_bell: "amzn_sfx_church_bell_1x_02",
        doorbell1: "amzn_sfx_doorbell_01",
        doorbell2: "amzn_sfx_doorbell_chime_01",
        doorbell3: "amzn_sfx_doorbell_chime_02",
        // Holidays
        xmas_bells: "christmas_05",
        halloween_door: "horror_10",
        // Misc
        air_horn: "air_horn_03",
        boing1: "boing_01",
        boing2: "boing_03",
        camera: "camera_01",
        squeaky_door: "squeaky_12",
        ticking_clock: "clock_01",
        trumpet: "amzn_sfx_trumpet_bugle_04",
        // Animals
        cat_meow: "amzn_sfx_cat_meow_1x_01",
        dog_bark: "amzn_sfx_dog_med_bark_1x_02",
        lion_roar: "amzn_sfx_lion_roar_02",
        rooster: "amzn_sfx_rooster_crow_01",
        wolf_howl: "amzn_sfx_wolf_howl_02",
        // Scifi
        aircraft: "futuristic_10",
        engines: "amzn_sfx_scifi_engines_on_02",
        red_alert: "amzn_sfx_scifi_alarm_04",
        shields: "amzn_sfx_scifi_sheilds_up_01",
        sirens: "amzn_sfx_scifi_alarm_01",
        zap: "zap_01",
        // Crowds
        applause: "amzn_sfx_crowd_applause_01",
        cheer: "amzn_sfx_large_crowd_cheer_01"
    ]
}

/******************************************
|    Diagnostic Data
*******************************************/

private getDiagDataJson(asObj = false) {
    try {
        updChildVers()
        def echoDevs = getEsDevices()
        def actApps = getActionApps()
        def zoneApps = getZoneApps()
        def wsDev = getSocketDevice()
        List appWarnings = []
        List appErrors = []
        List devWarnings = []
        List devErrors = []
        List sockWarnings = []
        List sockErrors = []
        List devSpeech = []
        List actWarnings = []
        List actErrors = []
        List zoneWarnings = []
        List zoneErrors = []
        def ah = getLogHistory()
        if(ah?.warnings?.size()) { appWarnings = appWarnings + ah?.warnings }
        if(ah?.errors?.size()) { appErrors = appErrors + ah?.errors }
        echoDevs?.each { dev->
            def h = dev?.getLogHistory()
            if(h?.warnings?.size()) { devWarnings = devWarnings + h?.warnings }
            if(h?.errors?.size()) { devErrors = devErrors + h?.errors }
            if(h?.speech?.size()) { devSpeech = devSpeech + h?.speech }
        }
        if(wsDev) {
            def h = dev?.getLogHistory()
            if(h?.warnings?.size()) { sockWarnings = sockWarnings + h?.warnings }
            if(h?.errors?.size()) { sockErrors = sockErrors + h?.errors }
        }
        actApps?.each { act->
            def h = act?.getLogHistory()
            if(h?.warnings?.size()) { actWarnings = actWarnings + h?.warnings }
            if(h?.errors?.size()) { actErrors = actErrors + h?.errors }
        }
        zoneApps?.each { zn->
            def h = zn?.getLogHistory()
            if(h?.warnings?.size()) { zoneWarnings = zoneWarnings + h?.warnings }
            if(h?.errors?.size()) { zoneErrors = zoneErrors + h?.errors }
        }
        Map output = [
            diagDt: getDtNow()?.toString(),
            app: [
                version: appVersion(),
                installed: state?.installData?.dt,
                updated: state?.installData?.updatedDt,
                timeZone: location?.timeZone?.ID?.toString(),
                lastVersionUpdDt: getTsVal("lastAppDataUpdDt"),
                config: state?.appData?.appDataVer ?: null,
                flags: [
                    pollBlocked: (state?.pollBlocked == true),
                    resumeConfig: state?.resumeConfig,
                    serviceConfigured: state?.serviceConfigured,
                    refreshDeviceData: state?.refreshDeviceData,
                    deviceRefreshInProgress: state?.deviceRefreshInProgress,
                    noAuthActive: state?.noAuthActive,
                    missPollRepair: state?.missPollRepair,
                    pushTested: state?.pushTested,
                    updateAvailable: state?.updateAvailable,
                    devices: [
                        addEchoNamePrefix: settings?.addEchoNamePrefix,
                        autoCreateDevices: settings?.autoCreateDevices,
                        autoRenameDevices: settings?.autoRenameDevices,
                        bypassDeviceBlocks: settings?.bypassDeviceBlocks,
                        createOtherDevices: settings?.createOtherDevices,
                        createTablets: settings?.createTablets,
                        createWHA: settings?.createWHA,
                        echoDeviceFilters: settings?.echoDeviceFilter?.size() ?: 0
                    ]
                ],
                stateUsage: "${stateSizePerc()}%",
                warnings: appWarnings ?: [],
                errors: appErrors ?: []
            ],
            actions: [
                version: state?.codeVersions?.actionApp ?: null,
                count: actApps?.size() ?: 0,
                warnings: actWarnings ?: [],
                errors: actErrors ?: []
            ],
            zones: [
                version: state?.codeVersions?.zoneApp ?: null,
                count: zoneApps?.size() ?: 0,
                warnings: zoneWarnings ?: [],
                errors: zoneErrors ?: []
            ],
            devices: [
                version: state?.codeVersions?.echoDevice ?: null,
                count: echoDevs?.size() ?: 0,
                lastDataUpdDt: getTsVal("lastDevDataUpdDt"),
                models: state?.deviceStyleCnts ?: [:],
                warnings: devWarnings ?: [],
                errors: devErrors ?: [],
                speech: devSpeech
            ],
            socket: [
                version: state?.codeVersions?.wsDevice ?: null,
                warnings: sockWarnings ?: [],
                errors: sockErrors ?: [],
                active: state?.websocketActive,
                lastStatusUpdDt: getTsVal("lastWebsocketUpdDt")
            ],
            hub: [
                platform: getPlatform(),
                firmware: location?.hubs[0]?.getFirmwareVersionString() ?: null,
                type: location?.hubs[0]?.getType() ?: null
            ],
            authStatus: [
                cookieValidationState: (state?.authValid == true),
                cookieValidDate: getTsVal("lastCookieChkDt") ?: null,
                cookieValidDur: getTsVal("lastCookieChkDt") ? seconds2Duration(getLastTsValSecs("lastCookieChkDt")) : null,
                cookieValidHistory: state?.authValidHistory,
                cookieLastRefreshDate: getTsVal("lastCookieRrshDt") ?: null,
                cookieLastRefreshDur: getTsVal("lastCookieRrshDt") ? seconds2Duration(getLastTsValSecs("lastCookieRrshDt")) : null,
                cookieInvalidReason: (state?.authValid != true && state.authEvtClearReason) ? state?.authEvtClearReason : null,
                cookieRefreshDays: settings?.refreshCookieDays,
                cookieItems: [
                    hasLocalCookie: (state?.cookieData && state?.cookieData?.localCookie),
                    hasCSRF: (state?.cookieData && state?.cookieData?.csrf),
                    hasDeviceId: (state?.cookieData && state?.cookieData?.deviceId),
                    hasDeviceSerial: (state?.cookieData && state?.cookieData?.deviceSerial),
                    hasLoginCookie: (state?.cookieData && state?.cookieData?.loginCookie),
                    hasRefreshToken: (state?.cookieData && state?.cookieData?.refreshToken),
                    hasFrc: (state?.cookieData && state?.cookieData?.frc),
                    amazonPage: (state?.cookieData && state?.cookieData?.amazonPage) ? state?.cookieData?.amazonPage : null,
                    refreshDt: (state?.cookieData && state?.cookieData?.refreshDt) ? state?.cookieData?.refreshDt : null,
                    tokenDate: (state?.cookieData && state?.cookieData?.tokenDate) ? state?.cookieData?.tokenDate : null,
                ],
                cookieData: (settings?.diagShareSensitveData == true) ? state?.cookieData ?: null : "Not Shared"
            ],
            alexaGuard: [
                supported: state?.alexaGuardSupported,
                status: state?.alexaGuardState,
                dataSrc: state?.guardDataSrc,
                lastSupportCheck: getTsVal("lastGuardSupChkDt"),
                lastStateCheck: getTsVal("lastGuardStateChkDt"),
                lastStateUpd: getTsVal("lastGuardStateUpdDt"),
                stRespLimit: (state?.guardDataOverMaxSize == true)
            ],
            server: [
                version: state?.codeVersions?.server ?: null,
                amazonDomain: settings?.amazonDomain,
                amazonLocale: settings?.regionLocale,
                lastServerWakeDt: getTsVal("lastServerWakeDt"),
                lastServerWakeDur: getTsVal("lastServerWakeDt") ? seconds2Duration(getLastTsValSecs("lastServerWakeDt")) : null,
                serverPlatform: getServerItem("onHeroku") ? "Cloud" : "Local",
                hostUrl: getServerHostURL(),
                randomName: getRandAppName()
            ],
            versionChecks: [
                minVersionUpdates: getMinVerUpdsRequired(),
                updateItemsOther: codeUpdateItems()
            ]
        ]
        def json = new groovy.json.JsonOutput().toJson(output)
        if(asObj) {
            return json
        }
        render contentType: "application/json", data: json, status: 200
    } catch (ex) {
        logError("getDiagData: Exception: ${ex}")
        if(asObj) { return null }
        render contentType: "application/json", data: [status: "failed", error: ex], status: 500
    }
}

private getDiagDataText() {
    def jsonIn = getDiagDataJson(true)
    def txtOut = null
    if(jsonIn) {
        def o = new groovy.json.JsonOutput().prettyPrint(createMetricsDataJson())
        render contentType: "text/plain", data: o, status: 200
    }
}

def getDiagData() {
    def ema = new String("dG9uZXN0bzdAZ21haWwuY29t"?.decodeBase64())
    String html = """
        <!DOCTYPE html>
        <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta http-equiv="x-ua-compatible" content="ie=edge">
                <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                <meta name="description" content="${title}">
                <meta name="author" content="Anthony S.">
                <meta http-equiv="cleartype" content="on">
                <meta name="MobileOptimized" content="320">
                <meta name="HandheldFriendly" content="True">
                <title>Echo Speak Diagnostics</title>
                <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.2/css/all.css">
                <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700&display=swap">
                <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.4.1/css/bootstrap.min.css" rel="stylesheet">
                <link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.14.0/css/mdb.min.css" rel="stylesheet">
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.4/umd/popper.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.4.1/js/bootstrap.min.js"></script>
                <script>
                    let cmdUrl = '${getAppEndpointUrl("diagCmds")}';
                </script>
                <style>
                    .bg-less-dark { background-color: #373c40 !important; color: #fff !important;}
                    .rounded-15 { border-radius: 15px !important; }
                    .rounded-5 { border-radius: 5px !important; }
                    .btn-matrix button { width: 135px; }
                    .btn-matrix > .btn:nth-child(Xn+X+1) { clear: left; margin-left: 0; }
                    .btn-matrix > .btn:nth-child(n+X+1) { margin-top: -1px; }
                    .btn-matrix > .btn:first-child { border-bottom-left-radius: 0; }
                    .btn-matrix > .btn:nth-child(X) { border-top-right-radius: 4px !important; }
                    .btn-matrix > .btn:nth-last-child(X) { border-bottom-left-radius: 4px !important; }
                    .btn-matrix > .btn:last-child { border-top-right-radius: 0; }
                    .btn-matrix { margin: 20px; flex-wrap: wrap;}
                    .valign-center { display: grid; vertical-align: middle; align-items: center;}
                </style>
            </head>
            <body class="bg-less-dark">
                <div class="container-fluid">
                    <div class="text-center">
                        <h3 class="mt-4 mb-0">Echo Speaks Diagnostics</h3>
                        <p>(v${appVersion()})</p>
                    </div>
                    <div class="text-center">
                        <h5 class="mt-4 mb-0">Diagnostic Data</h5>
                    </div>
                    <div class="px-0">
                        <div class="d-flex justify-content-center">
                            <div class="btn-group btn-matrix mt-1">
                                <button id="emailBtn" onclick="location.href='mailto:${ema?.toString()}?subject=Echo%20Speaks%20Diagnostics&body=${getAppEndpointUrl("diagData")}'" class="btn btn-sm btn-success rounded-15 p-2 my-2 mx-3" type="button"><div class="valign-center"><i class="fas fa-envelope fa-2x m-1"></i><span class="">Share with Developer</span></div></button>
                                <button id="asJsonBtn" onclick="location.href='${getAppEndpointUrl("diagDataJson")}'" class="btn btn-sm btn-info rounded-15 p-2 my-2 mx-3" type="button"><div class="valign-center"><i class="fas fa-code fa-2x m-1"></i><span>View Data as JSON</span></div></button>
                                <button id="asTextBtn" onclick="location.href='${getAppEndpointUrl("diagDataText")}'" class="btn btn-sm btn-dark rounded-15 p-2 my-2 mx-3" type="button"><div class="valign-center"><i class="fas fa-file-alt fa-2x m-1"></i><span>View Data as Text</span></div></button>
                            </div>
                        </div>
                    </div>
                    <div class="text-center">
                        <h5 class="mt-4 mb-0">Remote Commands</h5>
                    </div>
                    <div class="px-0">
                        <div class="d-flex justify-content-center">
                            <section class="btn-group btn-matrix mt-1">
                                <button id="wakeupServer" data-cmdtype="wakeupServer" class="btn btn-sm btn-outline-light rounded-5 p-2 my-2 mx-3 cmd_btn" type="button"><div class="valign-center"><i class="fas fa-server fa-2x m-1"></i><span>Wakeup Server</span></div></button>
                                <button id="forceDeviceSync" data-cmdtype="forceDeviceSync" class="btn btn-sm btn-outline-light rounded-5 p-2 my-2 mx-3 cmd_btn" type="button"><div class="valign-center"><i class="fas fa-sync fa-2x m-1"></i><span>Device Auth Sync</span></div></button>
                                <button id="execUpdate" data-cmdtype="execUpdate" class="btn btn-sm btn-outline-light rounded-5 p-2 my-2 mx-3 cmd_btn" type="button"><div class="valign-center"><i class="fas fa-arrow-circle-up fa-2x m-1"></i><span>Execute Update()</span></div></button>
                                <button id="validateAuth" data-cmdtype="validateAuth" class="btn btn-sm btn-outline-warning rounded-5 p-2 my-2 mx-3 cmd_btn" type="button"><div class="valign-center"><i class="fas fa-check fa-2x m-1"></i><span>Validate Auth</span></div></button>
                                <button id="clearLogs" data-cmdtype="clearLogs" class="btn btn-sm btn-outline-warning rounded-5 p-2 my-2 mx-3 cmd_btn" type="button"><div class="valign-center"><i class="fas fa-broom fa-2x m-1"></i><span>Clear Logs</span></div></button>
                                <button id="cookieRefresh" data-cmdtype="cookieRefresh" class="btn btn-sm btn-outline-danger rounded-5 p-2 my-2 mx-3 cmd_btn" type="button"><div class="valign-center"><i class="fas fa-cookie-bite fa-2x m-1"></i><span>Refresh Cookie</span></div></button>
                            </section>
                        </div>
                    </div>
                    <div class="w-100" style="position: fixed; bottom: 0;">
                        <div class="form-group ml-0 mr-4">
                            <label for="exampleFormControlTextarea1">External URL (Click/Tap to Select)</label>
                            <textarea class="form-control z-depth-1" id="exampleFormControlTextarea1" onclick="this.focus();this.select()" rows="3" readonly>${getAppEndpointUrl("diagData")}</textarea>
                        </div>
                    </div>
                </div>
            </body>
            <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.14.0/js/mdb.min.js"></script>
            <script>
                \$('.cmd_btn').click(function() { console.log('cmd_btn type: ', \$(this).attr("data-cmdtype")); execCmd(\$(this).attr("data-cmdtype")); });
                function execCmd(cmd) { if(!cmd) return; \$.getJSON(cmdUrl.replace('diagCmds', `diagCmds/\${cmd}`), function(result){ console.log(result); }); }
            </script>
        </html>
    """
    render contentType: "text/html", data: html, status: 200
}

def execDiagCmds() {
    String dcmd = params?.cmd
    Boolean status = false
    // log.debug "dcmd: ${dcmd}"
    if(dcmd) {
        switch(dcmd) {
            case "clearLogs":
                status = clearDiagLogs()
                break
            case "validateAuth":
                status = validateCookie(true);
                break
            case "wakeupServer":
                wakeupServer(false, false, "Diagnostic Command")
                status = true
                break
            case "cookieRefresh":
                status = runCookieRefresh()
                break
            case "forceDeviceSync":
                status = refreshDevCookies()
                break
            case "execUpdate":
                updated()
                status = true
                break
        }
    }
    def json = new groovy.json.JsonOutput().toJson([message: (status ? "ok" : "failed"), command: dcmd, version: appVersion()])
    render contentType: "application/json", data: json, status: 200
}


/******************************************
|    Time and Date Conversion Functions
*******************************************/
def formatDt(dt, tzChg=true) {
    def tf = new java.text.SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
    if(tzChg) { if(location.timeZone) { tf.setTimeZone(location?.timeZone) } }
    return tf?.format(dt)
}

String strCapitalize(str) { return str ? str?.toString().capitalize() : null }
String pluralizeStr(obj, para=true) { return (obj?.size() > 1) ? "${para ? "(s)": "s"}" : "" }
String pluralize(itemVal, str) { return (itemVal?.toInteger() > 1) ? "${str}s" : str }

def parseDt(pFormat, dt, tzFmt=true) {
    def result
    def newDt = Date.parse("$pFormat", dt)
    return formatDt(newDt, tzFmt)
}

def parseFmtDt(parseFmt, newFmt, dt) {
    def newDt = Date.parse(parseFmt, dt?.toString())
    def tf = new java.text.SimpleDateFormat(newFmt)
    if(location.timeZone) { tf.setTimeZone(location?.timeZone) }
    return tf?.format(newDt)
}

def getDtNow() {
    def now = new Date()
    return formatDt(now)
}

def epochToTime(tm) {
    def tf = new java.text.SimpleDateFormat("h:mm a")
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
        return diff?.abs()
    } catch (ex) {
        logError("GetTimeDiffSeconds Exception: (${sender ? "$sender | " : ""}lastDate: $lastDate): ${ex}")
        return 10000
    }
}

private seconds2Duration(Integer timeSec, postfix=true, tk=2, asMap=false) {
    Integer years = Math.floor(timeSec / 31536000); timeSec -= years * 31536000;
    Integer months = Math.floor(timeSec / 31536000); timeSec -= months * 2592000;
    Integer days = Math.floor(timeSec / 86400); timeSec -= days * 86400;
    Integer hours = Math.floor(timeSec / 3600); timeSec -= hours * 3600;
    Integer minutes = Math.floor(timeSec / 60); timeSec -= minutes * 60;
    Integer seconds = Integer.parseInt((timeSec % 60) as String, 10);
    Map d = [y: years, mn: months, d: days, h: hours, m: minutes, s: seconds]
    if(asMap) { return d }
    List l = []
    if(d?.d > 0) { l?.push("${d?.d} ${pluralize(d?.d, "day")}") }
    if(d?.h > 0) { l?.push("${d?.h} ${pluralize(d?.h, "hour")}") }
    if(d?.m > 0) { l?.push("${d?.m} ${pluralize(d?.m, "min")}") }
    if(d?.s > 0) { l?.push("${d?.s} ${pluralize(d?.s, "sec")}") }
    return l?.size() ? "${l?.take(tk ?: 2)?.join(", ")}${postfix ? " ago" : ""}" : "Not Sure"
}

private nextCookieRefreshDur() {
    Integer days = settings?.refreshCookieDays ?: 5
    def lastCookieRfsh = getTsVal("lastCookieRrshDt")
    if(!lastCookieRfsh) { return "Not Sure"}
    Date now = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(Date.parse("E MMM dd HH:mm:ss z yyyy", getDtNow())))
    Date lastDt = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(Date.parse("E MMM dd HH:mm:ss z yyyy", lastCookieRfsh)))
    Date nextDt = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(lastDt + days))
    def diff = ((long) (nextDt?.getTime() - now?.getTime()) / 1000) as Integer
    def dur = seconds2Duration(diff, false, 3)
    // log.debug "now: ${now} | lastDt: ${lastDt} | nextDt: ${nextDt} | Days: $days | Wait: $diff | Dur: ${dur}"
    return dur
}
List weekDaysEnum() {
    return ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
}

List monthEnum() {
    return ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
}

/******************************************
|   App Helper Utilites
*******************************************/

private updInstData(key, val) {
    Map iData = atomicState?.installData
    iData[key] = val
    atomicState?.installData = iData
}

private getInstData(key) {
    def iMap = atomicState?.installData
    if(val && iMap && iMap[val]) { return iMap[val] }
    return null
}

private updTsVal(key, dt=null) {
    def data = atomicState?.tsDtMap ?: [:]
    if(key) { data[key] = dt ?: getDtNow() }
    atomicState?.tsDtMap = data
}

private remTsVal(key) {
    def data = atomicState?.tsDtMap ?: [:]
    if(key) {
        if(key instanceof List) {
            key?.each { k-> if(data?.containsKey(k)) { data?.remove(k) } }
        } else { if(data?.containsKey(key)) { data?.remove(key) } }
        atomicState?.tsDtMap = data
    }
}

def getTsVal(val) {
    def tsMap = atomicState?.tsDtMap
    if(val && tsMap && tsMap[val]) { return tsMap[val] }
    return null
}

private updDevSupVal(key, val) {
    def data = atomicState?.devSupMap ?: [:]
    if(key) { data[key] = val }
    atomicState?.devSupMap = data
}

private remDevSupVal(key) {
    def data = atomicState?.devSupMap ?: [:]
    if(key) {
        if(key instanceof List) {
            key?.each { k-> if(data?.containsKey(k)) { data?.remove(k) } }
        } else { if(data?.containsKey(key)) { data?.remove(key) } }
        atomicState?.devSupMap = data
    }
}

private getDevSupVal(val) {
    def dsMap = atomicState?.devSupMap
    if(val && dsMap && dsMap[val]) { return dsMap[val] }
    return null
}

private updServerItem(key, val) {
    def data = atomicState?.serverDataMap ?: [:]
    if(key) { data[key] = val }
    atomicState?.serverDataMap = data
}

private remServerItem(key) {
    def data = atomicState?.serverDataMap ?: [:]
    if(key) {
        if(key instanceof List) {
            key?.each { k-> if(data?.containsKey(k)) { data?.remove(k) } }
        } else { if(data?.containsKey(key)) { data?.remove(key) } }
        atomicState?.serverDataMap = data
    }
}

def getServerItem(val) {
    def sMap = atomicState?.serverDataMap
    if(val && sMap && sMap[val]) { return sMap[val] }
    return null
}

private updAppFlag(key, val) {
    def data = atomicState?.appFlagsMap ?: [:]
    if(key) { data[key] = val }
    atomicState?.appFlagsMap = data
}

private remAppFlag(key) {
    def data = atomicState?.appFlagsMap ?: [:]
    if(key) {
        if(key instanceof List) {
            key?.each { k-> if(data?.containsKey(k)) { data?.remove(k) } }
        } else { if(data?.containsKey(key)) { data?.remove(key) } }
        atomicState?.appFlagsMap = data
    }
}

Boolean getAppFlag(val) {
    def aMap = atomicState?.appFlagsMap
    if(val && aMap && aMap[val]) { return aMap[val] }
    return false
}

private stateMapMigration() {
    //Timestamp State Migrations
    Map tsItems = [
        "musicProviderUpdDt":"musicProviderUpdDt", "lastCookieChkDt":"lastCookieChkDt", "lastServerWakeDt":"lastServerWakeDt", "lastChildInitRefreshDt":"lastChildInitRefreshDt",
        "lastCookieRefresh":"lastCookieRrshDt", "lastVerUpdDt":"lastAppDataUpdDt", "lastGuardSupportCheck":"lastGuardSupChkDt", "lastGuardStateUpd":"lastGuardStateUpdDt",
        "lastGuardStateCheck":"lastGuardStateChkDt", "lastDevDataUpd":"lastDevDataUpdDt", "lastMetricUpdDt":"lastMetricUpdDt", "lastMisPollMsgDt":"lastMissedPollMsgDt",
        "lastUpdMsgDt":"lastUpdMsgDt", "lastMsgDt":"lastMsgDt"
    ]
    tsItems?.each { k, v-> if(state?.containsKey(k)) { updTsVal(v as String, state[k as String]); state?.remove(k as String); } }

    //App Flag Migrations
    Map flagItems = [:]
    flagItems?.each { k, v-> if(state?.containsKey(k)) { updAppFlag(v as String, state[k as String]); state?.remove(k as String); } }

    //Server Data Migrations
    Map servItems = ["onHeroku":"onHeroku", "serverHost":"serverHost", "isLocal":"isLocal"]
    servItems?.each { k, v-> if(state?.containsKey(k)) { updServerItem(v as String, state[k as String]); state?.remove(k as String); } }
    if(state?.generatedHerokuName) { state?.herokuName = state?.generatedHerokuName; state?.remove("generatedHerokuName") }
    updAppFlag("stateMapConverted", true)
}

Integer getLastTsValSecs(val, nullVal=1000000) {
    def tsMap = atomicState?.tsDtMap
    return (val && tsMap && tsMap[val]) ? GetTimeDiffSeconds(tsMap[val]).toInteger() : nullVal
}

void settingUpdate(name, value, type=null) {
    if(name && type) {
        app?.updateSetting("$name", [type: "$type", value: value])
    }
    else if (name && type == null){ app?.updateSetting(name.toString(), value) }
}

void settingRemove(String name) {
    logTrace("settingRemove($name)...")
    if(name && settings?.containsKey(name as String)) { isST() ? app?.deleteSetting(name as String) : app?.removeSetting(name as String) }
}

private updCodeVerMap(key, val) {
    Map cv = atomicState?.codeVersions ?: [:]
    if(val && (!cv.containsKey(key) || (cv?.containsKey(key) && cv[key] != val))) { cv[key as String] = val }
    if (cv?.containsKey(key) && val == null) { cv?.remove(key) }
    atomicState?.codeVersions = cv
}

private cleanUpdVerMap() {
    Map cv = atomicState?.codeVersions ?: [:]
    def ri = ["groupApp"]
    cv?.each { k, v-> if(v == null) ri?.push(k) }
    ri?.each { cv?.remove(it) }
    atomicState?.codeVersions = cv
}

String getRandAppName() {
    if(!state?.herokuName && (!getServerItem("isLocal") && !getServerItem("serverHost"))) { state?.herokuName = "${app?.name?.toString().replaceAll(" ", "-")}-${randomString(8)}"?.toLowerCase() }
    return state?.herokuName as String
}

/******************************************
|   App Input Description Functions
*******************************************/
String getAppNotifConfDesc() {
    String str = ""
    if(pushStatus()) {
        def ap = getAppNotifDesc()
        def nd = getNotifSchedDesc(true)
        str += (settings?.usePush) ? bulletItem(str, "Sending via: (Push)") : ""
        str += (settings?.pushoverEnabled) ? bulletItem(str, "Pushover: (Enabled)") : ""
        // str += (settings?.pushoverEnabled && settings?.pushoverPriority) ? bulletItem(str, "Priority: (${settings?.pushoverPriority})") : ""
        // str += (settings?.pushoverEnabled && settings?.pushoverSound) ? bulletItem(str, "Sound: (${settings?.pushoverSound})") : ""
        str += (settings?.phone) ? bulletItem(str, "Sending via: (SMS)") : ""
        str += (ap) ? "${str != "" ? "\n\n" : ""}Enabled Alerts:\n${ap}" : ""
        str += (ap && nd) ? "${str != "" ? "\n" : ""}\nQuiet Restrictions:\n${nd}" : ""
    }
    return str != "" ? str : null
}
List getQuietDays() {
    List allDays = weekDaysEnum()
    List curDays = settings?.quietDays ?: []
    return allDays?.findAll { (!curDays?.contains(it as String)) }
}

String getNotifSchedDesc(min=false) {
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
    notifDesc += (getNotifTimeStartLbl && getNotifTimeStopLbl) ? " • Time: ${getNotifTimeStartLbl} - ${getNotifTimeStopLbl}" : ""
    def days = getInputToStringDesc(dayInput)
    def modes = getInputToStringDesc(modeInput)
    def qDays = getQuietDays()
    notifDesc += dayInput && qDays ? "${(getNotifTimeStartLbl || getNotifTimeStopLbl) ? "\n" : ""} • Day${pluralizeStr(dayInput, false)}:${min ? " (${qDays?.size()} selected)" : "\n    - ${qDays?.join("\n    - ")}"}" : ""
    notifDesc += modes ? "${(getNotifTimeStartLbl || getNotifTimeStopLbl || (dayInput && qDays)) ? "\n" : ""} • Mode${pluralizeStr(modeInput, false)}:${min ? " (${modes?.size()} selected)" : "\n    - ${modes?.join("\n    - ")}"}" : ""
    return (notifDesc != "") ? "${notifDesc}" : null
}

String getServiceConfDesc() {
    String str = ""
    str += (state?.herokuName && getServerItem("onHeroku")) ? "Heroku: (Configured)\n" : ""
    str += (state?.serviceConfigured && getServerItem("isLocal")) ? "Local Server: (Configured)\n" : ""
    str += (settings?.amazonDomain) ? "Domain: (${settings?.amazonDomain})" : ""
    return str != "" ? str : null
}

String getLoginStatusDesc() {
    def s = "Login Status: (${state?.authValid ? "Valid" : "Invalid"})"
    s += (getTsVal("lastCookieRrshDt")) ? "\nCookie Updated:\n(${seconds2Duration(getLastTsValSecs("lastCookieRrshDt"))})" : ""
    return s
}

String getAppNotifDesc() {
    def str = ""
    str += settings?.sendMissedPollMsg != false ? bulletItem(str, "Missed Polls") : ""
    str += settings?.sendAppUpdateMsg != false ? bulletItem(str, "Code Updates") : ""
    str += settings?.sendCookieRefreshMsg == true ? bulletItem(str, "Cookie Refresh") : ""
    return str != "" ? str : null
}

String getActionsDesc() {
    def acts = getActionApps()
    def paused = acts?.findAll { it?.isPaused() == true }
    def active = acts?.findAll { it?.isPaused() != true }
    String str = ""
    str += active?.size() ? "(${active?.size()}) Active\n" : ""
    str += paused?.size() ? "(${paused?.size()}) Paused\n" : ""
    str += active?.size() || paused?.size() ? "\nTap to modify" : "Tap to create actions using device/location events to perform advanced actions using your Alexa devices."
    return str
}

String getZoneDesc() {
    def zones = getZoneApps()
    def actZones = getActiveZoneNames()?.sort()?.collect { "\u2022 ${it}" }
    // log.debug "actZones: $actZones"
    def paused = zones?.findAll { it?.isPaused() == true }
    def active = zones?.findAll { it?.isPaused() != true }
    String str = ""
    str += actZones?.size() ? "Active Zones:\n${actZones?.join("\n")}\n" : "No Active Zones...\n"
    str += paused?.size() ? "(${paused?.size()}) Paused\n" : ""
    str += active?.size() || paused?.size() ? "\nTap to modify" : "Tap to create alexa device zones based on motion, presence, and other criteria."
    return str
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

def appInfoSect()	{
    Map codeVer = state?.codeVersions ?: null
    String str = ""
    Boolean isNote = false
    if(codeVer && (codeVer?.server || codeVer?.actionApp || codeVer?.echoDevice)) {
        str += (codeVer && codeVer?.actionApp) ? bulletItem(str, "Action: (v${codeVer?.actionApp})") : ""
        str += (codeVer && codeVer?.zoneApp) ? bulletItem(str, "Zone: (v${codeVer?.zoneApp})") : ""
        str += (codeVer && codeVer?.echoDevice) ? bulletItem(str, "Device: (v${codeVer?.echoDevice})") : ""
        str += (!isST() && codeVer && codeVer?.wsDevice) ? bulletItem(str, "Socket: (v${codeVer?.wsDevice})") : ""
        str += (codeVer && codeVer?.server) ? bulletItem(str, "Server: (v${codeVer?.server})") : ""
    }
    section() {
        href "changeLogPage", title: inTS("${app?.name} (v${appVersion()})", getAppImg("echo_speaks_3.2x", true), null, false), description: str, image: getAppImg("echo_speaks_3.2x")
        if(!state?.isInstalled) {
            paragraph pTS("--NEW Install--", null, true, "#2784D9"), state: "complete"
        } else {
            if(!state?.noticeData) { getNoticeData() }
            Boolean showDocs = false
            Map minUpdMap = getMinVerUpdsRequired()
            List codeUpdItems = codeUpdateItems(true)
            List remDevs = getRemovableDevs()
            if(minUpdMap?.updRequired && minUpdMap?.updItems?.size()) {
                isNote=true
                String str3 = "Updates Required for:"
                minUpdMap?.updItems?.each { item-> str3 += bulletItem(str3, item)  }
                paragraph pTS(str3, null, true, "red"), required: true, state: null
                paragraph pTS("If you just updated the code please press Done/Save to let the app process the changes.", null, true, "red"), required: true, state: null
                showDocs = true
            } else if(codeUpdItems?.size()) {
                isNote=true
                String str2 = "Code Updates Available for:"
                codeUpdItems?.each { item-> str2 += bulletItem(str2, item) }
                paragraph pTS(str2, null, false, "#2784D9"), required: true, state: null
                showDocs = true
            }
            if(showDocs) { updateDocsInput() }
            if(!state?.authValid && !state?.resumeConfig) { isNote = true; paragraph pTS("You are no longer logged in to Amazon.  Please complete the Authentication Process on the Server Login Page!", null, false, "red"), required: true, state: null }
            if(state?.noticeData && state?.noticeData?.notices && state?.noticeData?.notices?.size()) {
                isNote = true; state?.noticeData?.notices?.each { item-> paragraph pTS(bulletItem(str, item), null, false, "red"), required: true, state: null; };
            }
            if(remDevs?.size()) {
                isNote = true
                paragraph pTS("Device Removal:\n(${remDevs?.size()}) devices can be removed", null, false), required: true, state: null
            }
            if(!isNote) { paragraph pTS("No Issues to Report", null, true) }
        }
    }
    List unkDevs = getUnknownDevices()
    if(unkDevs?.size()) {
        section() {
            Map params = [ assignees: "tonesto7", labels: "add_device_support", title: "[ADD DEVICE SUPPORT] (${unkDevs?.size()}) Devices", body: "Requesting device support from the following device(s):\n" + unkDevs?.collect { d-> d?.collect { k,v-> "${k}: ${v}" }?.join("\n") }?.join("\n\n")?.toString() ]
            def featUrl = "https://github.com/tonesto7/echo-speaks/issues/new?${UrlParamBuilder(params)}"
            href url: featUrl, style: "external", required: false, title: inTS("Unknown Devices Found\n\nSend device info to the Developer on GitHub?", getAppImg("info", true)), description: "Tap to open browser", image: getAppImg("info")
        }
    }
}

String UrlParamBuilder(items) {
    return items?.collect { k,v -> "${k}=${URLEncoder.encode(v?.toString())}" }?.join("&") as String
}

def getRandomItem(items) {
    def list = new ArrayList<String>();
    items?.each { list?.add(it) }
    return list?.get(new Random().nextInt(list?.size()));
}

String randomString(Integer len) {
    def pool = ["a".."z",0..9].flatten()
    Random rand = new Random(new Date().getTime())
    def randChars = (0..len).collect { pool[rand.nextInt(pool.size())] }
    logDebug("randomString: ${randChars?.join()}")
    return randChars.join()
}

Boolean getAccessToken() {
    try {
        if(!state?.accessToken) { state?.accessToken = createAccessToken() }
        else { return true }
    } catch (ex) {
        logError("getAccessToken Exception: ${ex}")
        return false
    }
}

private getTextEditChild(id) {
    if(!isST()) {
        Long longId = id as Long
        return getChildAppById(longId) ?: null
    } else { return getActionApps()?.find { it?.id == id } ?: null }
}

def renderConfig() {
    String title = "Echo Speaks"
    Boolean heroku = (isST() || (settings?.useHeroku == null || settings?.useHeroku != false))
    String oStr = !heroku ? """
        <div id="localServerDiv" class="w-100 mb-3">
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
            .btn-rounded { border-radius: 50px!important; }
            span img { width: 48px; height: auto; }
            span p { display: block; }
            .all-copy p { -webkit-user-select: all; -moz-user-select: all; -ms-user-select: all; user-select: all; }
            .nameContainer { border-radius: 18px; color: rgba(255,255,255,1); font-size: 1.5rem; background: #666; -webkit-box-shadow: 1px 1px 1px 0 rgba(0,0,0,0.3); box-shadow: 1px 1px 1px 0 rgba(0,0,0,0.3); text-shadow: 1px 1px 1px rgba(0,0,0,0.2); }
        </style>
    <head>
    <body>
        <div style="margin: 0 auto; max-width: 600px;">
            <form class="p-1">
                <div class="my-3 text-center"><span><img src="${getAppImg("echo_speaks_3.1x", true)}"/><p class="h4 text-center">Echo Speaks</p></span></div>
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

def renderTextEditPage() {
    String actId = params?.cId
    String inName = params?.inName
    Map inData = [:]
    // log.debug "actId: $actId | inName: $inName"
    if(actId && inName) {
        def actApp = getTextEditChild(actId)
        if(actApp) { inData = actApp?.getInputData(inName) }
    }
    String html = """
        <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="x-ua-compatible" content="ie=edge">
                <title>Echo Speak Response Designer</title>
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.9.0/css/all.min.css">
                <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet">
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/mdbootstrap@4.8.10/css/mdb.min.css" integrity="sha256-iNGvtX88EOpA/u8LtFgUkDiIsoBSwI+GbErXYrSzpuA=" crossorigin="anonymous">
                <link href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.48.4/codemirror.min.css" rel="stylesheet">
                <link href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.48.4/theme/material.min.css" rel="stylesheet">
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/vanillatoasts@1.3.0/vanillatoasts.css" integrity="sha256-U06o/6s4HELYo2A3Gd7KPGQMojQiAxY9B8oE/hnM3KU=" crossorigin="anonymous">
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.4/umd/popper.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/js/bootstrap.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.48.4/codemirror.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.48.4/addon/mode/simple.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.48.4/mode/xml/xml.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.48.4/addon/hint/show-hint.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.48.4/addon/hint/xml-hint.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.48.4/addon/edit/closetag.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.48.4/addon/edit/matchtags.min.js"></script>
                <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.48.4/mode/htmlmixed/htmlmixed.min.js"></script>
                <style>
                    form div { margin-bottom: 0.5em; margin: 0 auto; }
                    .form-control { font-size: 0.7rem; }
                    button.btn.btn-info.btn-block.my-4 { max-width: 200px; text-align: center; }
                    .ssml-buttons { font-size: .8rem; margin-bottom: 5px; text-decoration: none; }
                    .ssml-buttons, h3 { font-size: 0.9rem; text-decoration: underline; }
                    .ssml-buttons, input { font-size: 0.9rem; text-decoration: none; }
                    .ssml-button { font-weight: normal; margin-left: 5px; margin-bottom: 5px; cursor: pointer; border: 0.5px solid #b1a8a8; border-radius: 7px; padding: 3px; background-color: #EEE; -webkit-appearance: none; -moz-appearance: none }
                    .ssml-button:hover { background-color: #AAA }
                    .ssml-button:first-child { margin-left: 0 }
                    .no-submit { cursor: text }
                </style>
            </head>
            <body class="m-2">
                <div class="p-3"><button type="button" class="close" aria-label="Close" onclick="window.open('','_parent',''); window.close();"><span aria-hidden="true">×</span></button></div>
                <div style="float: left; clear:both;position: fixed; top: 0px;"><small>v${inData?.version}</small></div>
                <div class="w-100 pt-0">
                    <form>
                        <div class="container px-0">
                            <div class="text-center">
                                <p id="inputTitle" class="h5 mb-0">Text Field Entry</p>
                                <p class="mt-1 mb-2 text-center">Response Designer</p>
                            </div>
                            <div class="px-0">
                                <textarea id="editor"></textarea>
                                <div class="text-center blue-text"><small>Each line item represents a single response. Multiple lines will trigger a random selection.</small></div>
                                <div class="d-flex justify-content-center">
                                    <button id="clearBtn" style="border-radius: 50px !important;" class="btn btn-sm btn-outline-warning px-1 my-2 mx-3" type="button"><i class="fas fa-times-circle mr-1"></i>Clear All</button>
                                    <button id="newLineBtn" style="border-radius: 50px !important;" class="btn btn-sm btn-outline-info px-1 my-2 mx-3" type="button"><i class="fas fa-plus mr-1"></i>New Response</button>
                                    <button id="submitBtn" style="border-radius: 50px !important;" class="btn btn-sm btn-outline-success my-2" type="submit"><i class="fa fa-save mr-1"></i>Save Responses</button>
                                </div>
                            </div>
                            <div class="row mt-2 mx-auto">
                                <div class="px-2 col-12">
                                    <div class="card my-3 mx-0">
                                        <h5 class="card-header px-2 py-0"><i class="fas fa-info px-2 my-auto"></i>Builder Items</h5>
                                        <div class="card-body py-0 px-2 mx-0">
                                            <div class="text-center orange-text mt-0 mb-2">
                                                <small>Select a line item and tap on an item to insert or replace the item in the text.</small>
                                            </div>
                                            <div class="p-1 mx-auto">
                                                <div class="shortcuts-wrap" style="display: block;">
                                                    <div class="row">
                                                        <div class="col-12">
                                                            <div class="ssml-buttons">
                                                                <h3>Event Variables</h3>
                                                                <input class="ssml-button" type="button" unselectable="on" value="Type" data-ssml="evttype">
                                                                <input class="ssml-button" type="button" unselectable="on" value="Value" data-ssml="evtvalue">
                                                                <input class="ssml-button" type="button" unselectable="on" value="DeviceName" data-ssml="evtname">
                                                                <input class="ssml-button" type="button" unselectable="on" value="Unit" data-ssml="evtunit">
                                                                <input class="ssml-button" type="button" unselectable="on" value="Date" data-ssml="evtdate">
                                                                <input class="ssml-button" type="button" unselectable="on" value="Time" data-ssml="evttime">
                                                                <input class="ssml-button" type="button" unselectable="on" value="Date/Time" data-ssml="evtdatetime">
                                                                <input class="ssml-button" type="button" unselectable="on" value="Duration (Seconds)" data-ssml="evtduration">
                                                                <input class="ssml-button" type="button" unselectable="on" value="Duration (Minutes)" data-ssml="evtdurationmin">
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="my-3">
                                                    <small id="inputDesc">Description goes here.</small>
                                                </div>
                                            </div>
                                            <hr class="mb-1 mx-auto" style="background-color: #696969; width: 100%;">
                                            <div class="p-1 mx-auto">
                                                <h4>SSML Markup Items</h4>
                                                <div class="shortcuts-wrap" style="display: block;">
                                                    <div class="row">
                                                        <div class="col-6">
                                                            <div class="ssml-buttons">
                                                                <h3>BREAK</h3><input class="ssml-button" type="button" unselectable="on" value="50ms" data-ssml="break"><input class="ssml-button" type="button" unselectable="on" value="200ms" data-ssml="break"><input class="ssml-button"
                                                                    type="button" unselectable="on" value="500ms" data-ssml="break"><input class="ssml-button" type="button" unselectable="on" value="800ms" data-ssml="break"><input class="ssml-button" type="button" unselectable="on"
                                                                    value="1s" data-ssml="break"><input class="ssml-button" type="button" unselectable="on" value="2s" data-ssml="break">
                                                            </div>
                                                        </div>
                                                        <div class="col-6">
                                                            <div class="ssml-buttons">
                                                                <h3>EMPHASIS</h3><input class="ssml-button" type="button" unselectable="on" value="strong" data-ssml="emphasis"><input class="ssml-button" type="button" unselectable="on" value="reduced" data-ssml="emphasis">
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div class="row">
                                                        <div class="col-6">
                                                            <div class="ssml-buttons">
                                                                <h3>PITCH</h3><input class="ssml-button" type="button" unselectable="on" value="x-low" data-ssml="pitch"><input class="ssml-button" type="button" unselectable="on" value="low" data-ssml="pitch"><input class="ssml-button"
                                                                    type="button" unselectable="on" value="medium" data-ssml="pitch"><input class="ssml-button" type="button" unselectable="on" value="high" data-ssml="pitch"><input class="ssml-button" type="button" unselectable="on"
                                                                    value="x-high" data-ssml="pitch">
                                                            </div>
                                                        </div>
                                                        <div class="col-6">
                                                            <div class="ssml-buttons">
                                                                <h3>RATE</h3><input class="ssml-button" type="button" unselectable="on" value="x-slow" data-ssml="rate"><input class="ssml-button" type="button" unselectable="on" value="slow" data-ssml="rate"><input class="ssml-button"
                                                                    type="button" unselectable="on" value="medium" data-ssml="rate"><input class="ssml-button" type="button" unselectable="on" value="fast" data-ssml="rate"><input class="ssml-button" type="button" unselectable="on"
                                                                    value="x-fast" data-ssml="rate">
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div class="row">
                                                        <div class="col-6">
                                                            <div class="ssml-buttons">
                                                                <h3>VOLUME</h3><input class="ssml-button" type="button" unselectable="on" value="silent" data-ssml="volume"><input class="ssml-button" type="button" unselectable="on" value="x-soft" data-ssml="volume">
                                                                <input class="ssml-button" type="button" unselectable="on" value="soft" data-ssml="volume"><input class="ssml-button" type="button" unselectable="on" value="medium" data-ssml="volume"><input class="ssml-button"
                                                                    type="button" unselectable="on" value="loud" data-ssml="volume"><input class="ssml-button" type="button" unselectable="on" value="x-loud" data-ssml="volume">
                                                            </div>
                                                        </div>
                                                        <div class="col-6">
                                                            <div class="ssml-buttons">
                                                                <h3>WHISPER</h3><input class="ssml-button" type="button" unselectable="on" value="whisper" data-ssml="whisper">
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div class="row">
                                                        <div class="col-6">
                                                            <div class="ssml-buttons">
                                                                <h3>VOICE</h3>
                                                                <select class="browser-default custom-select custom-select-sm mb-2" id="voices">
                                                                    <option value="Naja" class="x-option ember-view">Danish (F) - Naja</option>
                                                                    <option value="Mads" class="x-option ember-view">Danish (M) - Mads</option>
                                                                    <option value="Lotte" class="x-option ember-view">Dutch (F) - Lotte</option>
                                                                    <option value="Ruben" class="x-option ember-view">Dutch (M) - Ruben</option>
                                                                    <option value="Nicole" class="x-option ember-view">English, Australian (F) - Nicole</option>
                                                                    <option value="Russell" class="x-option ember-view">English, Australian (M) - Russell</option>
                                                                    <option value="Amy" class="x-option ember-view">English, British (F) - Amy</option>
                                                                    <option value="Emma" class="x-option ember-view">English, British (F) - Emma</option>
                                                                    <option value="Brian" class="x-option ember-view">English, British (M) - Brian</option>
                                                                    <option value="Raveena" class="x-option ember-view">English, Indian (F) - Raveena</option>
                                                                    <option value="Aditi" class="x-option ember-view">English, Indian (F) - Aditi</option>
                                                                    <option value="Ivy" class="x-option ember-view">English, US (F) - Ivy</option>
                                                                    <option value="Joanna" class="x-option ember-view">English, US (F) - Joanna</option>
                                                                    <option value="Kendra" class="x-option ember-view">English, US (F) - Kendra</option>
                                                                    <option value="Kimberly" class="x-option ember-view">English, US (F) - Kimberly</option>
                                                                    <option value="Salli" class="x-option ember-view">English, US (F) - Salli</option>
                                                                    <option value="Joey" class="x-option ember-view">English, US (M) - Joey</option>
                                                                    <option value="Justin" class="x-option ember-view">English, US (M) - Justin</option>
                                                                    <option value="Matthew" class="x-option ember-view">English, US (M) - Matthew</option>
                                                                    <option value="Geraint" class="x-option ember-view">English, Welsh (M) - Geraint</option>
                                                                    <option value="Celine" class="x-option ember-view">French (F) - Céline</option>
                                                                    <option value="Lea" class="x-option ember-view">French (F) - Léa</option>
                                                                    <option value="Mathieu" class="x-option ember-view">French (M) - Mathieu</option>
                                                                    <option value="Chantal" class="x-option ember-view">French, Canadian (F) - Chantal</option>
                                                                    <option value="Marlene" class="x-option ember-view">German (F) - Marlene</option>
                                                                    <option value="Vicki" class="x-option ember-view">German (F) - Vicki</option>
                                                                    <option value="Hans" class="x-option ember-view">German (M) - Hans</option>
                                                                    <option value="Aditi" class="x-option ember-view">Hindi (F) - Aditi</option>
                                                                    <option value="Dóra" class="x-option ember-view">Icelandic (F) - Dóra</option>
                                                                    <option value="Karl" class="x-option ember-view">Icelandic (M) - Karl</option>
                                                                    <option value="Carla" class="x-option ember-view">Italian (F) - Carla</option>
                                                                    <option value="Giorgio" class="x-option ember-view">Italian (M) - Giorgio</option>
                                                                    <option value="Takumi" class="x-option ember-view">Japanese (M) - Takumi</option>
                                                                    <option value="Mizuki" class="x-option ember-view">Japanese (F) - Mizuki</option>
                                                                    <option value="Seoyeon" class="x-option ember-view">Korean (F) - Seoyeon</option>
                                                                    <option value="Liv" class="x-option ember-view">Norwegian (F) - Liv</option>
                                                                    <option value="Ewa" class="x-option ember-view">Polish (F) - Ewa</option>
                                                                    <option value="Maja" class="x-option ember-view">Polish (F) - Maja</option>
                                                                    <option value="Jacek" class="x-option ember-view">Polish (M) - Jacek</option>
                                                                    <option value="Jan" class="x-option ember-view">Polish (M) - Jan</option>
                                                                    <option value="Vitoria" class="x-option ember-view">Portugese, Brazilian (F) - Vitória</option>
                                                                    <option value="Ricardo" class="x-option ember-view">Portugese, Brazilian (M) - Ricardo</option>
                                                                    <option value="Ines" class="x-option ember-view">Portugese, European (F) - Inês</option>
                                                                    <option value="Cristiano" class="x-option ember-view">Portugese, European (M) - Cristiano</option>
                                                                    <option value="Carmen" class="x-option ember-view">Romanian (F) - Carmen</option>
                                                                    <option value="Tatyana" class="x-option ember-view">Russian (F) - Tatyana</option>
                                                                    <option value="Maxim" class="x-option ember-view">Russian (M) - Maxim</option>
                                                                    <option value="Conchita" class="x-option ember-view">Spanish, European (F) - Conchita</option>
                                                                    <option value="Enrique" class="x-option ember-view">Spanish, European (M) - Enrique</option>
                                                                    <option value="Penélope" class="x-option ember-view">Spanish, US (F) - Penélope</option>
                                                                    <option value="Miguel" class="x-option ember-view">Spanish, US (M) - Miguel</option>
                                                                    <option value="Astrid" class="x-option ember-view">Swedish (F) - Astrid</option>
                                                                    <option value="Filiz" class="x-option ember-view">Turkish (F) - Filiz</option>
                                                                    <option value="Gwyneth" class="x-option ember-view">Welsh (F) - Gwyneth</option>
                                                                </select>
                                                                <input class="ssml-button" type="button" unselectable="on" value="Add Voice" data-ssml="voice">
                                                            </div>
                                                        </div>
                                                        <div class="col-6">
                                                            <div class="ssml-buttons">
                                                                <h3>SAY-AS</h3><input class="ssml-button" type="button" unselectable="on" value="number" data-ssml="say-as"><input class="ssml-button" type="button" unselectable="on" value="spell-out" data-ssml="say-as">
                                                                <input class="ssml-button" type="button" unselectable="on" value="ordinal" data-ssml="say-as"><input class="ssml-button" type="button" unselectable="on" value="digits" data-ssml="say-as"><input class="ssml-button"
                                                                    type="button" unselectable="on" value="date" data-ssml="say-as"><input class="ssml-button" type="button" unselectable="on" value="time" data-ssml="say-as"><input class="ssml-button" type="button" unselectable="on"
                                                                    value="speechcon" data-ssml="say-as">
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            <hr class="mb-1 mx-auto" style="background-color: #696969; width: 100%;">
                                            <div class="text-center align-text-top blue-text"><small>Speak tags will automatically be added to lines where SSML is inserted.</small></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
                <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/mdbootstrap@4.8.10/js/mdb.min.js" integrity="sha256-wH71T2mMsoF6NEYmAPxpPvUbgALoVRlZRHlMlCQpOnk=" crossorigin="anonymous"></script>
                <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/vanillatoasts@1.3.0/vanillatoasts.min.js"></script>
                <script>
                    let inName = '${inName}';
                    let actId = '${actId}'
                    let rootUrl = '${getAppEndpointUrl("textEditor/${actId}/${inName}")}';
                    let curText = '${inData?.val}';
                    curText = (curText == null || curText == 'null' || curText == '') ? '${inData?.template}' : curText
                    let curTitle = '${inData?.title}';
                    let curDesc = '${inData?.desc}';
                    let selectedLineNum = undefined;

                    // SSML Links
                    let ssmlTestUrl = "https://topvoiceapps.com/ssml"
                    let ssmlDocsUrl = "https://developer.amazon.com/docs/custom-skills/speech-synthesis-markup-language-ssml-reference.html"
                    let ssmlSoundsUrl = "https://developer.amazon.com/docs/custom-skills/ask-soundlibrary.html"
                    let ssmlSpeechConsUrl = "https://developer.amazon.com/docs/custom-skills/speechcon-reference-interjections-english-us.html"

                    function cleanEditorText(txt) {
                        txt = txt.split(';').filter(t => t.trim().length > 0).map(t => t.trim()).join(';');
                        txt = txt.endsWith(';') ? txt.replace(/;([^;]*)\$/, '\$1') : txt;
                        txt = txt.replace('%duration_min%', '%durationmin%');
                        return txt.replace(/  +/g, ' ').replace('> <', '><');//.replace("\'", '');
                    }

                    \$(document).ready(function() {
                        \$('#inputTitle').text(curTitle);
                        \$('#inputDesc').html(curDesc);
                        \$('#editor').val(cleanEditorText(curText));
                        CodeMirror.defineSimpleMode("simplemode", {
                            start: [{
                                regex: /<speak>|<\\/speak>/,
                                token: 'tag'
                            }, {
                                regex: /<voice[^>]+>|<\\/voice>/,
                                token: 'attribute'
                            }, {
                                regex: /<say-as[^>]+>|<\\/say-as>|<emphasis[^>]+>|<\\/emphasis>/,
                                token: 'string'
                            }, {
                                regex: /<prosody[^>]+>|<\\/prosody>/,
                                token: 'keyword'
                            }, {
                                regex: /%[a-z]+%/,
                                token: "variable-2"
                            }, {
                                regex: /<[^>]+>/,
                                token: 'variable'
                            }, {
                                regex: /=["']?((?:.(?!["']?\\s+(?:\\S+)=|[>"']))+.)["']?/,
                                token: 'value'
                            }, {
                                regex: /REPLACE_THIS_TEXT/,
                                token: "error"
                            }, {
                                regex: /0x[a-f\\d]+|[-+]?(?:\\.\\d+|\\d+\\.?\\d*)(?:e[-+]?\\d+)?/i,
                                token: "number"
                            }]
                        });

                        let editor = CodeMirror.fromTextArea(document.getElementById("editor"), {
                            theme: 'material',
                            mode: 'simplemode',
                            lineNumbers: true,
                            lineSeparator: ';',
                            styleActiveLine: true,
                            showCursorWhenSelecting: true,
                            autoCloseTags: true,
                            lineWrapping: false,
                            autocorrect: false,
                            autocapitalize: false,
                            spellcheck: true,
                            styleActiveLine: {
                                nonEmpty: true
                            }
                        });
                        editor.markClean();

                        \$('#newLineBtn').click((e) => {
                            let doc = editor.getDoc();
                            let cursor = doc.getCursor();
                            let lineCnt = editor.lineCount();
                            doc.replaceRange(';', CodeMirror.Pos(lineCnt - 1));
                        });

                        \$('#clearBtn').click((e) => {
                            editor.setValue('');
                        });

                        function updateInfo(instance, changeObj) {
                            selectedLineNum = changeObj.to.line;
                            if (!editor.isClean()) {
                                \$('#submitBtn').removeClass('btn-outline-success').addClass('btn-success');
                            } else {
                                \$('#submitBtn').removeClass('btn-success').addClass('btn-outline-success');
                            }
                            // \$('#lineCnt').text('Response Cnt: ' + changeObj.to.line);
                        }

                        editor.on("change", updateInfo);
                        editor.on('beforeSelectionChange', (e) => {
                            //Handles selection changes
                        })

                        \$('form').submit(function(e) {
                            console.log('form submit...')
                            e.preventDefault();
                            let xmlhttp = new XMLHttpRequest();
                            xmlhttp.open("POST", rootUrl);
                            xmlhttp.onreadystatechange = () => {
                                if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                                    // console.log(xmlhttp.responseText);
                                    let toast = VanillaToasts.create({
                                        title: 'Echo Speaks Actions',
                                        text: 'Responses saved successfully!',
                                        type: 'success',
                                        icon: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEsAAABLCAYAAAA4TnrqAAAbSUlEQVR42u2cd5QVRdrGf1XdfePcyYkZYchBgigICoqoi+gChjWjYMRFRdQ1x9U1oLK6qKBgFlE/I4iIrq6uYlgRUaJkmGGGyXluvt1d3x99h6CIJFe/7/Cc02fuubfi02+99dRbVQMHcAAHcAAHcAAHcAAH8GtA/NYN2AG6Cz09D+lPR7i8KCuBCjeRaKiCWOi3bt1/lyzhSeGcSAuzDxmGd/B5GHmdkKlZaCnpSF8a0u9H84HQQUjnQYGKgYpEUZFm7HATdrAOs2YzkSUf0zDvKYy2PUiUrvo/TpYvBf9RFxD+7HnSL3seo/Bg9DbdCJxqUHXl312uzkekSX9qtnR7c4ThzhKGkSE0mSJ0wyN0Q0OZtrATcbCCWGYTVrxOJaI1KtpSm6jY0HDipPMiC7+AeMl64mVraPnkf/D0GEjV07dhh5v/b5Dl++PNHPXeJBbd8AFGYU+8g9rS/PyLPv2gXu2lP6O39Ab6Cpenp9D19kKTOULKgJDSLaTQhQSEcCxLOC0UQiFQFqi4wA6BVYeySjFjq1QstMQONS4za0rWD7x5VOOK+XXESn6g7qUHCBx1MmWTx/8OyZKSwOjHSaz7Av9JN+Lq2pfIwtl+I797b5mSeZxw+44Vuqun0PUcIaUupIAkIdtIcYjZ+lkmv5diW7rt8ghhA0phmw3YibUkIp/bocZ/mbVl3+aNO7a+4Z11tHw1H1e7rpTedQFmS81vT1bKWQ8TXT6PtLOnkHlTH+ruXdhWyy46UXpTzhC663Ch6xlIuY0UFGDHBWYjyqpGWVUCuxpBvcBuRoqI0DRLYEuU7RaaDAhBhpAyR2gyX+hartS0TKFpXqTYRiYKlBnGSqxQ8fA7Zl35nLVjD1l98Oxiu/nzObjyiyi+9yLs5sb/Plme468m87opRL5cRPolh9P0zNcdtOwOo4U3dbQw3N2FlFLI1tRWAitehhVbjh3/FjOyVEWbN9rB2mqrZmNL02s3x46sqbFr1oNZA8RBukHPhtTO8LUQImf8wy53u55+PSM3W0tJayc9vt7C5eovDXdf4XJ1kJrmRUsSpxRY8TI7Gpxj1lU8v+b8Pt/3eGO1Wn1Wdw66/nFKJ1/13yMrdfxs7GANnoHnY25emKXndhsjfWl/Fi5Pd6RMFm4rrHixMiOfqVjL+3bjlm9jS94ty/nr/fHY0hasxi3YjeVYdaVYleuJbV6OHaxD1ZSSaKxE8wbQ2nRG86fi7tAbV2EXjLx26DmFGFkFuDtns+GUblruxCdyXXlFfbRA+gnS4x0mXe7uQtMMx5IVmPEKO9LycqK6dHrmHw/bUPv6Jxi5B1F885mE1y/79cjSux5N2vi5WJWraXzoSJF538Y/aIHcW4XLMwRNk0IAykooM/otifCrdkPZ/PoHhm7Ke6LWNkuXEF//Nc2zrsZ31AWEFryw11YdOPwEWhZ9SM6Fd+E/ZAieTodw5qhMXp66IN/VtvNQzRc4T7o9xwhdDwgpEMpGmdFVVrBpcnDhh//j7zs4ctypnXnjvGupfGXK/ifLfcwVFH06jdpJpZhblqUZRf2vFr6MiUJ3ZQkBYFumFfsqFgs9GWoMzZ8Wf68po3wxJ1UvIF3F9pqYn7RYgfigdOtXmj+VgutmEFr8MekjL8bXcyDNH7/s9fUaNERPyxovvf7hUte9CMC2Yna4+dVY6dq7U/ocUVz28DV4u/Sl+O6L9h9Z3j/eTmT+vWQ/HscsX95By+n4oPSkno6UEhRY8WJ/S+mM6d9e+93whi8i6D4docntirABaydFy+RjOTTssq0hBCuAKG2yEc99/5NEGSeeT/sHX6Lxw3n4+w6h6ZPXfIEBJ4zU07Ovlx7v4UiBQKGi4W8SdeV/yRrR9cuSe54GTaPk7kv2nSz3kMuJLXiS7OkKc8vKQ7TMomnS7R+MEKBsSyWC79SGgn9V/+7bGc39NIgK4NvtOq+AXkCfHxEigM3AUuBYwP8LTYkD1wAvUrBzsloR6H8cRfe9Tmj5V6QfP4rQ4q8L3UVdrtP8qeOErqcIASoRKzYbqq8+7o/t5s6542mElBT/7dKfLVPyCzC6H8+Rnz1B1jSFWbayr5bZ/jnhShnsyMREsx2s/lt83ecXPb3x4RVoni4gsoEvkXIciBtA3AziRuBTwAv4tnu8wHIQtwLRH/22sycd6AFAKLHLdrd8+wkrhmcTXb2YM/qC9Pm3lE+9/sZEXcV4FY+VAEiXu72RmTvjk3c3/qn/PeOwmuo56JoH95Isw0fqxH+x9MFqzLKlXWVm0XTh8h+GAKx4td1cNbHu1rb3Sn9286Wlr7VaEICFsjNAzQK1wHlodQrrgJeBf7N1WCobWAW8DqxJprOBb4CXgO/Y0UrB3tWI3YaKZ+7mDsON1VxP2pDTzKyR7V6OVxSPtaPh5SgQhjvfyCl4bO3cDScUXnsDZVNuIm3wSXtOVuDydzFLvsMsXZIlM9v/Xbj8AwEw41V2c9WV+bcc9GLqpW/YdqguKbd3gAVsAYqBMsDAGW5nAZOT5E12Oi/qgftAXAdcDNQCrwLnAo8CZwOzd4udncA246wbPxSroZq1F92Cr1v3BfHKzRfasfASAOFyFxq5Bf8Ifr+m96Gf1dPl0bl7Rpar37moYA3B2ddJ/aBDrxPuwCicoddkNVfckHlr2zc3j3sNZZs0Tx3x4+wCKZoQ3IngMhATgB+SVuIDPgD+ClQDYUADbgc1BcF6YBnwJHAy8AkwBHgCSLAP2rDk3nFIt4clg9Lwdu7xXby6bLwdj64DkC7PwUZW3n3B7z9PDy1fSMd7Z+4+WRkPvYL70LMIjH52mPCmXy6EdJx5uGFyw+3tX6667E0EkpanztxZdoWtslDMRPEpqH8ChwBrgZ5APjAWuB/wgErF8UXtAReOZdUA/YHU5N8KILQvZAGUz7iL3LMnsunGswkc3m1hoqb8RpVINCBA+lJG+A7uf8nACwcTWb8Co7Dol8lyHT6ayHtfEF/7UbpMybleaEY6KFSs5d1E2eLH02782kZ30/LMWbtql8IZihbOLBYCcoESIAK8CdyAY1lJ9bTVL6UAaTj+jeTfDJwJYfec1a4Ie/ZeAv2GUPX8TFac0vkdK9g4VdlKCalLPTVzwqLXl/XMOeNy2l//6C+Tde03L+PqfRRabo9RwvAdA6CseIXVVDFJb3t4s9amJy1PnryzrGJruULWIzgfOBohjgKmAWOAjcApwG1AlkNM6xJ7a/7MZNoXgREI3gYuBNzbavrFiXyXKHlgAlpKOj1mfauim1ZNs6Ohr0EhDFd7I6fNxctGdhDR4tU75NF3VtC0y2aj4sGAu9fJY5GagVKoSNOspnt6fOM9bTLxVR/8bBuAIHA0tv0UrVaglA0cDAwAnsGREefh+KQ6UPfjaKwCFA8B7XB0WT6wAsU4YBiOFa5PSIOiEd+QP2LfgiZBoO7TTznspuFVkfWbp0qPt5+QmkvzB07vNXvlDKG71kY2/kDtuzN3sISt8J07HXfvU1DhhqEyu/M7QjNSlRktt6p+OEm4A8usqjW0TB/1k4rVie3A8T+H4eihH796O/lo29VrYsUUmltPSEPZQgpNWVK3LTNJdGtalcwbVEJ8JxpKQifdvevRKAVUBqEyGBdSCAYUGir8I2mmgLowVDeV4rfWpnt7HDFPenyDUQqzseaalEPyHv0iextFPyFLP+RU2rw2m+DsmnulP/s2ENjRhldCr4+7QO9wtBl+63qwzZ8ji911KSHdh3/eGlJmqbMM2/6DEqJaIZoEKksolbWzggQK3TY1gdpdk1JAC45s8f5MGgFUm+H4o+26hM7WM3MfQUisUPP8+nkvnK7506Ib77xo52QFxr+LsuJ+V7fhc4Xbfxy2bdvBqotlRpsXVaiO+huyd7Odu0buCxFwHP6/gCXAFKAJyAFuBE7bD9VEgXHA0cBlu2ZVjMv3rfzGVdTzE2G4s1Q8Uhpet+x4oRnrfhjdD9iJg5eZRWjZnXLR9E4ASpl1Ktb8vVWzkaYZ+6P9O8CF4/BvwZEH03CG8M1AabKzcRwp0fCjvM1AJbC9mceA8mR6gDiCZTi+FByf17qnFsIRzc0AAvzWhv9sVIn4BgA0PUcPZHR2FWyTDz9x8NKXiUpE2iBkJgC2VWG3VJajFNb6z/c3WQJoEYigQnmBbjiqfi2Oj7o8Scam5N/bgFHA/GS6BmAo8DccgXsbzmrBwlH9F6DQcIbhomS6S4G2wJ1AY/KF3Q5Kc918ZVB9Vr8B1AAhpEd6fO3dbbKQvlTscPNOpIPLD5qRhZAehyyzxqpZF7Lqivc3USQ71UuhzhdCPA8cAVyXJNEHLATW46j9dsBdwGIcSzw82eF3gVfYFuWYgiOApwN1OJPESuBqoANwQjL9CuCOZJ1LASu2CpRtlaOUs8Ok6bneruDr0M0xpJ+8at2N0D0+sVXIqJBVs85UwVp+JUjgNqXUO8AVQAWCPyeJMnA02TAcnVUJfIQzrJYDb+NY3Hc4suQwnCXVIraJYil17W0hxBLgJBxHPwhHs92KszIYAAjVBAhatko+Xfe7ACO7DfAzOgu1/Uz0q+7DasC85PMkjqUMEIgzFKokWXlLMm2rr/GBcIH6A3AojvNuh2NRnwEP4Qytucn8lm1axyVJfRg4EuiS/BwE/g7cDnSWBogItE7EP+75T8kyYygrGkmGTTTAr2V31hH6rgNIeweFsyZ8EWeteDSwWCnVDuibfFlvgNKA93CG1ymg5uIssIPJ729LkmniWMoinInBSn43CDhWCHGaUur55At4B7gSCOCsP3WRCqqZlFZppywzHAcStRU7J0vFQ2Al6pSyowLNQGo5Wm4XP0pFfgWybKA3jg95ESdq2g+4AOjo/K6OwJmxBuE4/CJgKvA0zkQwHmd4dcKJTCzCCQO14KwvLwa6A0cppSYlybsQZ4h/kqzvUuApowuICq3AidUrlGlWRdZBpGTtzsmyww2g7EqUXQ8EkHobGcgvQHPVap0GYW34an+S1Trc+yUfxTbrb0iS2RO46Uf5uuMMo+3RK0nWjzFxu8/b7+XfsmMyYcfufyzFe8xFSclkx+xYuDheXocVbPoZsuqLUVaiSsvqtBHdXSSkniU8aX1lWptlqRe9TsPtB+0nngQCGhXqY5y3/GPFHsGZ/jvgOPR9jjb8bEOgUSnxjdbl6E7CcHUCAZZZbTU3rLfDwa0Jf0JWdOEL5L36Zig0u+Yr4fIdC1IK3TO8+emTXzE6HWduW6rtKxQKIgLxF1D3JaROvVe38kMRvX1ws1HpyTWL0zKUPw4piSjq15xoBJF+F7hqKj7Ku1ZoejaASsRXhFctKpO+1B1Y3QH+C1/C1W2Ys5DO6jRXaEZAmdFyq3LlicIdWG5Vr93pQnpPUTgzSsJWhHUPYza8wRMvnWXQMe84hNYP8AAWSpWQaH7/4X53Vt3X5zqMnaxJ9we8VOJJbEj39xz4rvT4jkoupK9NOSxvyhcZu1hIAwQum42KhwKuniNnC0/a8SiFHap5qOGWvJt8f/o78ZXzMNd8us+NVCcVgVIAblDX4gzHuThiMg0nnNwfmASsRwjE+5v3G0nS7eHMaITiNRDZVHqOkd3mBaFpbpWIbY6VbzxBGq41FU/dQ81cJ0Sz0wjapBmn4h16XouKNs/EthIIgfCmnZ92x6rD3QPGknrJW/upua3BUTUMyEaIB3DUdBmO6n4S+BgYQyK+30hqRYe/vcSyWYtoXPBprhZInyA0zY1SWKHg2ytO67m28d9ztxL1s2RdO2AMsaWfY1b9MFclwgsA0NwFMrXNzWbxVwGzejWBK+buXot+CdkZ4Kzv3kepBCS35sVWo/8KyMHl9uxPotrdNBUr2MSqMQOEp0P3K6THNwhAJWIliZotz/Z5b6PyJJc5uyQrsWgWKeccjavH8EY7WPuwshJNApCelJON9kdOaP77YKHiUVIumPnLrdolBNQ16jgxdieG6/ahRnbYPlEOjuDcb6I4d/Q1NH46h7yxl9B77oaRWiDjKiGlwLZts7nxiVPOOmRFzZtPUXLflb9MFkDNhPOJff8Woffu/FBFm6YrZYPQdOHLuDHjnuJzcx8/E5RFykWz9r7Vzsk+EyGeQcoteD0QDUI83vprJ2A08AFKWdjW3teVRN4FN1D9yhS6zviI4PI1h+vZbSYLw5WJAiscfD+y5vunZ8/8D56OPYhVlOyQd5fzceo1CxDuFOxgTbbefsAL0ps2QggBVqzSaq64Mvu29m9XXT4b4fLS9NhJ7CnUyPaOg7edo5HJXeY84ByccE0h8AKo2c4hUw0xf9NeE1V0x3NE1i2l3a1TCP+wvK+7sMOz0uvssKtYZHW8suRsIyt/mZ6WwaK+P6Vml1skzdNOQO94KHq7frV2fcl1Kh5eBIDuztfSCqbVP7jl/Obpf5LCk0rWw3VoHQbsWetNCyyb1pBIa7U4kYWXcdZ9Q0D0dEjdO+mgZ+fT+fEPkb4AnadPIbxu7WBXYcfnpcd/GIBKxCsStRV/ST2qx7Lvj8lkw7Wn7rScXe8nJaI0/+MEht2YhV7YZ43dVDZeJcJLUCA0V75MyZ2a9VDlLXZTZQArhuZLIeWcR/butZtbhW4EZ/f6S+ApHClxKQjPdoTuNnLPuYYxNRVo6dnUvPqI1vx52TmuvHazpMfXFwEqEa9K1FZe3WdUp/fLHp7GQVc/SP2/39kLsoDE6o+YO/wm6iZI9Pxu31m1Gy9W8eB/FAo0I02m5Nzt6n7csypU27vwg49xdRxE9v3r2IOOFQCnIhEIgfigNHlQrfVIMl8COqi2SU22W0jpfzy9/tVIysATWLIE7FBLm/YPzXlAz8x9Srjc7QHseGxzorbi8rNHFL3xn/tmoadlUfboTT9b5m7tVEY/fAjvqZOovVygF/T83qpeM0ZFm99G2QopNeFNO1PL6Tyn5bXKq5QZS1HxMK7+f9rdfgWAESQ3UNWo5C6R1kq2sHAW1K7dKcx/6BD6b1BknfxnUIJEXYUnXlp2urfbYW/raVnXo7sCKLCj4cXxys1jcka2n/3sXc+Bstl467m7LHu3t3Ujc27BM3QChZcLZGreBrPsu0vsUN09mPF6EAjD21EGcv6h5Xa+0DuoDyrUsLtFlwEaSvVCKTCTyt5Wji9TqmOS0PKf7URKGgXXPEbakNPJu/ReYpVg1lV4zIaaP6QN+dNLWmb+TOnxH4GQYJlxK9j4Umzz2rPc7bouWD3uDkCy8Y4LfrGhe7QHHv10Kiu7H4v0ZyK9GY11N+TebdUXX4gZczoipCakliVTYeuQcXvInPgm6eOeI/P690i/5GkA0i+cinKkQAjn3MMEnLCwQYofnKVPL+B64H2gAbltaKcc4cy+2WPupGjyh6QOG8uwz95EWWZuvKL4zPQTx7xq5LV9S/pTzxBS86EUdiyyJlFXMaFl4T/Ha2lZG0f0A6G72LQb50lhb2PGXi+pY2aiF/ZE2WZPPbfrR0J3t8FONFr1xSOFN/VLW0aJvnU3RQueo/b+5dTe0UdmXDc/NbH2i1DWzfcl4qtasBu3UFS+iIVZY1CftTseZzjGkgT6ceLlHyLFBx+b6fbZBefgLuqBq6AjelYBng7ZrOgnZIcnv842sgv6SLd3mPR4hkmXp6fQNNfWo92WWW1HWl5JVBY/mTe239rKGZ9i5LWl+M7zCC1fuNvd3qe4x0GfKyJfld8gU/MfElKiYs0fxVa+d5pwpYSkS8NuqMB3/CXEV33ZRm/TdaJ0+/6AbW7GjH6rrOhyoi0b7KbymuCmxcHQrIuiyDYGA8lD130kzDALqXrsyXmJz8eP4E0hRMZpEw3vIcf5jLx2WZo/vUh6PL2k291Pur2HSsPoJKTmQ2P7SwPldiz0jtVc//ymG09Z3OmRefama04i64wrKHvkmj3u796RZXhJvfRlVCKa5u41Yq7wpA4R2Mpq2jLR3aPt1OxRUH7fSmpv7yVyH1o/TKbl3ik8/sFCyu2vo8TAbBC2WYVtVqLMKpRVZwualBQRoWxbF0qTUrkRMiB0LVNoeo7U9XxhGPlC07KkJn1bb1RsLdeKKDO2UoVb3rWaamZvuuqYlV1fXWOHlizAVdCBDROO3xr5/K+QlTphPkZRP1S48Vgtu8M76EYAM1pqlS8/ASFXGx0OI7FpYa6e2+kq6Uu7QuiuTCQIbBuUElJqO7/opHb83HpDLHnpSSRvi+1wKQobgd2Iba5TsdAXdrjpX2Z16TcdrxpaW/bmBoKLPsLdrhvFd4/GqqncK5Jaoe9NpsSmrzjv8ZN4/RF9FFIPoBQkop/VPzJoLRGT7EnrjtULet0p3b6hztUUBYnYBjvS8Ci2WSdcnkOFy32w0I0iNC0HKVKElG6k0JzX13pUa7vzbbayFSqObYfBqlO2WYoZW40ZW6rM2FKrqWbtaeMHNrz/TxdmXQUL+wwmZfDJVD57FyoR3SeS9tqytKJDCZz3DFhmgV7Y+yPh8h6MsuJ2c8U51uZFnxidjpwo/JlXCd2Vs/VqSqx5jt245d7AyF7Lokstcs7XKBk72e3qMihd86flCMOdK3Q9W2haBpoMCE1zC4EmJDbKjgtlBXGspw4rXq2iwerEltUNZZPGhnt/DYnyCszaMho/eJHUY05n8y2nYkd+B5czc19UqAhY9ZvP1tIKX0LTDBUPrbAbN9+tZRw0Trr9w9CSc7wV26zC9ZMT6794wdX9mGDjU2PxDjyLyNev4T/yDPTCHmjpuWj+NKQ/DZnqQc8AmQKaF0iAHQXVAnaLiR1qREWasFvqSFSsJ7z4Ixr/+QKuwq7Et6zd7+TsM1kp58/A3LxI9586eab0pp+LABLhcjRNCt2d79xrtiwVC86zW6ruSb+46+KGRxegt+1D/eRhJDZ+u+sKXB5w+zB0F5adwI5HIBpN7vn+ttgjn+UadDFGl6EYHY7sIgzvEMBxKYavoHUpqBKxLSpc90ii5JtnXF2HNpePvQajfX/q7j9m9yqJRyEe3X+Rvv2IPVLwVtVaPAO6InxZw5FGIbDNNpVtq2jzfKuu5PT8vxQ8Iv3ZzcLwYlb8QNOMMb91P/cL9mgY+kfPQMVavN5Bl8yW3vThWyctM1apwnWPxTd8PqPH9LPrFwoh2ryqVMW5v69/G7Gv2KNhKDQd3H4NhNuZ2W1lhxo+M0sXT2p6dPhXgLXwqdEGYP5/IwqcUzK7D8PD0f98MF42Z2kDqIBdV/xWaM4tD4Rm37IBZ0jHQSX49bbaf1PszesXQBZSz8Y2TZxjPS04h2d/j355v2FvFLwCEthm64mXRhyifvu5/XcKgXMeYbeilwdwAAdwAAdwAL9r/C/e3US8+Q+buQAAAABJRU5ErkJggg==",
                                        timeout: 4500,
                                        callback: function() { this.hide() }
                                    });
                                }
                            }
                            xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
                            // console.log(\$('#editor').val());
                            flattenSsml();
                            console.log('editor: ', editor.getValue());
                            xmlhttp.send(JSON.stringify({
                                val: editor.getValue(),
                                name: inName,
                                type: 'text'
                            }));
                            editor.markClean();
                            \$('#submitBtn').removeClass('btn-success').addClass('btn-outline-info');
                        });

                        function insertSsml(editor, str, sTags = true) {
                            let doc = editor.getDoc();
                            let cursor = doc.getCursor();
                            let line = cursor.line;
                            // console.log(`lineTxt (\${editor.getLine(line).length}): `, editor.getLine(line))
                            if (editor.getSelection().length > 0) {
                                editor.replaceSelection(cleanEditorText(` \${str}`));
                            } else {
                                doc.replaceRange(cleanEditorText(` \${str}`), {
                                    line: line,
                                    ch: cursor.ch
                                });
                            }
                            if (sTags) {
                                doc.replaceRange(editor.getLine(line).replace('<speak>', '').replace('</speak>', ''), {
                                    line: line,
                                    ch: 0
                                }, {
                                    line: line,
                                    ch: editor.getLine(line).length
                                });
                                doc.replaceRange('<speak>', {
                                    line: line,
                                    ch: 0
                                });
                                doc.replaceRange('</speak>', {
                                    line: line,
                                    ch: editor.getLine(line).length
                                });
                            }
                        }

                        function flattenSsml() {
                            let doc = editor.getDoc();
                            let mlInd = 0
                            let mlItems = [];
                            let ln = 0
                            doc.eachLine(line => {
                                if (line.text.trim().startsWith('<speak>') && !line.text.trim().endsWith('</speak>')) {
                                    mlItems[mlInd] = {
                                        s: ln,
                                        str: line.text.replace(';', '').trim()
                                    };
                                } else if (!line.text.trim().startsWith('<speak>') && line.text.trim().endsWith('</speak>')) {
                                    if (mlItems[mlInd] !== undefined && mlItems[mlInd].s !== undefined) {
                                        mlItems[mlInd].e = ln;
                                        mlItems[mlInd].str += line.text.replace(';', '').trim();
                                        mlItems[mlInd].el = line.text.length;
                                        mlInd++;
                                    }
                                } else {
                                    if (mlItems[mlInd] !== undefined) {
                                        mlItems[mlInd].str += line.text.replace(';', '').trim();
                                    }
                                }
                                ln++;
                            });
                            // console.log(mlItems);
                            if (mlItems.length) {
                                doc.replaceRange(mlItems[0].str, {
                                    line: mlItems[0].s,
                                    ch: 0
                                }, {
                                    line: mlItems[0].e,
                                    ch: mlItems[0].el
                                });
                                flattenSsml();
                            }
                            editor.setValue(cleanEditorText(editor.getValue()));
                        }

                        \$('.ssml-buttons input:not(.no-submit)').click(function() {
                            let ssml = \$(this).data('ssml');
                            let value = \$(this).val();
                            var selected = editor.getSelection();
                            var replace = (selected == '') ? 'REPLACE_THIS_TEXT' : selected;
                            switch (ssml) {
                                case 'break':
                                    insertSsml(editor, `<break time="\${value}"/>`);
                                    break;
                                case 'emphasis':
                                    insertSsml(editor, `<emphasis level="\${value}">\${replace}</emphasis>`);
                                    break;
                                case 'pitch':
                                case 'rate':
                                case 'volume':
                                    insertSsml(editor, `<prosody \${ssml}="\${value}">\${replace}</prosody>`);
                                    break;
                                case 'voice':
                                    insertSsml(editor, `<voice name="\${(\$('#voices').val() != '') ? \$('#voices').val() : 'Ivy'}">\${replace}</voice>`);
                                    break;
                                case 'audio':
                                    insertSsml(editor, `<audio src="\${(\$('#audio').val() != '') ? \$('#audio').val() : 'https://s3.amazonaws.com/ask-soundlibrary/transportation/amzn_sfx_car_accelerate_01.mp3'}"/>`);
                                    break;
                                case 'sub':
                                    insertSsml(editor, `<sub alias="\${(\$('#alias').val() != '') ? \$('#alias').val() : 'magnesium'}">\${replace}</sub>`);
                                    break;
                                case 'say-as':
                                    if (selected == '') {
                                        switch (value) {
                                            case 'number':
                                                replace = '1234';
                                                break;
                                            case 'characters':
                                                replace = 'TEST';
                                                break;
                                            case 'spell-out':
                                                replace = 'TEST';
                                                break;
                                            case 'cardinal':
                                                replace = '1234';
                                                break;
                                            case 'ordinal':
                                                replace = '5';
                                                break;
                                            case 'digits':
                                                replace = '1234';
                                                break;
                                            case 'fraction':
                                                replace = '1/5';
                                                break;
                                            case 'unit':
                                                replace = 'USD10';
                                                break;
                                            case 'date':
                                                replace = '01012019';
                                                break;
                                            case 'time':
                                                // replace = '1'21"';
                                                break;
                                            case 'telephone':
                                                replace = '(541) 754-3010';
                                                break;
                                            case 'address':
                                                replace = '711-2880 Nulla St., Mankato, Mississippi, 96522';
                                                break;
                                            case 'expletive':
                                                replace = 'bad word';
                                                break;
                                            case 'interjection':
                                                replace = 'boing';
                                                break;
                                            case 'speechcon':
                                                replace = 'boing';
                                                break;
                                            default:
                                                replace = 'REPLACE THIS TEXT';
                                        }
                                    }
                                    insertSsml(editor, `<say-as interpret-as="\${value}">\${replace}</say-as>`)
                                    break;
                                case 'whisper':
                                    insertSsml(editor, `<amazon:effect name="whispered">\${replace}</amazon:effect>`);
                                    break;
                                case 'evttype':
                                    insertSsml(editor, '%type%', false);
                                    break;
                                case 'evtvalue':
                                    insertSsml(editor, '%value%', false);
                                    break;
                                case 'evtname':
                                    insertSsml(editor, '%name%', false);
                                    break;
                                case 'evtdate':
                                    insertSsml(editor, '%date%', false);
                                    break;
                                case 'evtunit':
                                    insertSsml(editor, '%unit%', false);
                                    break;
                                case 'evttime':
                                    insertSsml(editor, '%time%', false);
                                    break;
                                case 'evtdatetime':
                                    insertSsml(editor, '%datetime%', false);
                                    break;
                                case 'evtduration':
                                    insertSsml(editor, '%duration%', false);
                                    break;
                                case 'evtdurationmin':
                                    insertSsml(editor, '%durationmin%', false);
                                    break;
                                default:
                                    break;
                            }
                            return false
                        });
                    });
                </script>
            </body>
        </html>
    """
    render contentType: "text/html", data: html
}

def textEditProcessing() {
    String actId = params?.cId
    String inName = params?.inName
    // log.debug "POST | actId: $actId | inName: $inName"
    def resp = request?.JSON ?: null
    def actApp = getTextEditChild(actId)
    Boolean status = (actApp && actApp?.updateTxtEntry(resp))
    def json = new groovy.json.JsonOutput().toJson([message: (status ? "success" : "failed"), version: appVersion()])
    render contentType: "application/json", data: json, status: 200
}

def getSettingVal(inName) {
    String actId = params?.cId
    // log.debug "GetSettingVals | actId: $actId"
    def actApp = getTextEditChild(actId)
    def value = null
    if(actApp) { value = actApp?.getSettingInputVal(inName) }
    return value
}

String getTextEditorPath(cId, inName) {
    return getAppEndpointUrl("textEditor/${cId}/${inName}") as String
}

def okSym() {
    return "\u2713"
}
def notOkSym() {
    return "\u2715"
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
    else if(obj instanceof Date) {return "Date"}
    else { return "unknown"}
}

private List amazonDomainOpts() { return (state?.appData && state?.appData?.amazonDomains?.size()) ? state?.appData?.amazonDomains : ["amazon.com", "amazon.ca", "amazon.co.uk", "amazon.com.au", "amazon.de", "amazon.it", "amazon.com.br", "amazon.com.mx"] }
private List localeOpts() { return (state?.appData && state?.appData?.locales?.size()) ? state?.appData?.locales : ["en-US", "en-CA", "de-DE", "en-GB", "it-IT", "en-AU", "pt-BR", "es-MX", "es-UY"] }

private getPlatform() {
    def p = "SmartThings"
    if(state?.hubPlatform == null) {
        try { [dummy: "dummyVal"]?.encodeAsJson(); } catch (e) { p = "Hubitat" }
        // p = (location?.hubs[0]?.id?.toString()?.length() > 5) ? "SmartThings" : "Hubitat"
        state?.hubPlatform = p
    }
    // log.debug "hubPlatform: (${state?.hubPlatform})"
    return state?.hubPlatform
}

Boolean isContactOpen(sensors) {
    if(sensors) { sensors?.each { if(sensors?.currentSwitch == "open") { return true } } }
    return false
}

Boolean isSwitchOn(devs) {
    if(devs) { devs?.each { if(it?.currentSwitch == "on") { return true } } }
    return false
}

Boolean isSensorPresent(sensors) {
    if(sensors) { sensors?.each { if(it?.currentPresence == "present") { return true } } }
    return false
}

Boolean isSomebodyHome(sensors) {
    if(sensors) { return (sensors?.findAll { it?.currentPresence == "present" }?.size() > 0) }
    return false
}

Boolean isInMode(modes) {
    return (location?.mode?.toString() in modes)
}

Boolean isInAlarmMode(modes) {
    if(!modes) return false
    return (getAlarmSystemStatus() in modes)
}

String getAlarmSystemName(abbr=false) {
    return isST() ? (abbr ? "SHM" : "Smart Home Monitor") : (abbr ? "HSM" : "Hubitat Safety Monitor")
}

List getAlarmModes() {
    return isST() ? ["off", "stay", "away"] : ["disarm", "armNight", "armHome", "armAway"]
}

String getAlarmSystemStatus() {
    if(isST()) {
        def cur = location?.currentState("alarmSystemStatus")?.value
        def inc = getShmIncidents()
        if(inc != null && inc?.size()) { cur = 'alarm_active' }
        return cur ?: "disarmed"
    } else { return location?.hsmStatus ?: "disarmed" }
}

def getShmIncidents() {
    def incidentThreshold = now() - 604800000
    return location?.activeIncidents?.collect{[date: it?.date?.time, title: it?.getTitle(), message: it?.getMessage(), args: it?.getMessageArgs(), sourceType: it?.getSourceType()]}.findAll{ it?.date >= incidentThreshold } ?: null
}

public setAlarmSystemMode(mode) {
    if(!isST()) {
        switch(mode) {
            case "armAway":
            case "away":
                mode = "armAway"
                break
            case "armHome":
            case "night":
            case "stay":
                mode = "armHome"
                break
            case "disarm":
            case "off":
                mode = "disarm"
                break
        }
    }
    logInfo("Setting the ${getAlarmSystemName()} Mode to (${mode})...")
    sendLocationEvent(name: (isST() ? 'alarmSystemStatus' : 'hsmSetArm'), value: mode.toString())
}

public JsonElementsParser(root) {
    if (root instanceof List) {
        root.collect {
            if (it instanceof Map) { JsonElementsParser(it) }
            else if (it instanceof List) { JsonElementsParser(it) }
            else if (it == null) { null }
            else {
                if(it?.toString()?.startsWith("{") && it?.toString()?.endsWith("}")) { it = JsonElementsParser(parseJson(it?.toString())) }
                else { it }
            }
        }
    } else if (root instanceof Map) {
        root.each {
            if (it.value instanceof Map) { JsonElementsParser(it.value) }
            else if (it.value instanceof List) { it.value = JsonElementsParser(it.value) }
            else if (it.value == null) { it.value }
        }
    }
}

Integer stateSize() { def j = new groovy.json.JsonOutput().toJson(state); return j?.toString().length(); }
Integer stateSizePerc() { return (int) ((stateSize() / 100000)*100).toDouble().round(0); }

List logLevels() {
    List lItems = ["logInfo", "logWarn", "logDebug", "logError", "logTrace"]
    return settings?.findAll { it?.key in lItems && it?.value == true }?.collect { it?.key }
}

String getAppDebugDesc() {
    def ll = logLevels()
    def str = ""
    str += ll?.size() ? "App Log Levels: (${ll?.join(", ")})" : ""
    return (str != "") ? "${str}" : null
}

private addToLogHistory(String logKey, msg, Integer max=10) {
    Boolean ssOk = (stateSizePerc() <= 70)
    List eData = atomicState[logKey as String] ?: []
    if(eData?.find { it?.message == msg }) { return; }
    eData?.push([dt: getDtNow(), message: msg])
    if(!ssOk || eData?.size() > max) { eData = eData?.drop( (eData?.size()-max) ) }
    atomicState[logKey as String] = eData
}
private logDebug(msg) { if(settings?.logDebug == true) { log.debug "EchoApp (v${appVersion()}) | ${msg}" } }
private logInfo(msg) { if(settings?.logInfo != false) { log.info " EchoApp (v${appVersion()}) | ${msg}" } }
private logTrace(msg) { if(settings?.logTrace == true) { log.trace "EchoApp (v${appVersion()}) | ${msg}" } }
private logWarn(msg, noHist=false) { if(settings?.logWarn != false) { log.warn " EchoApp (v${appVersion()}) | ${msg}"; }; if(!noHist) { addToLogHistory("warnHistory", msg, 15); } }
private logError(msg, noHist=false) { if(settings?.logError != false) { log.error "EchoApp (v${appVersion()}) | ${msg}"; }; if(!noHist) { addToLogHistory("errorHistory", msg, 15); } }

// public hasLogDevice() { return (settings?.logDevice != null) }
// public sendLog(msg, lvl) {
//     if(settings?.logDevice) {
//         parent?.logToDevice(app?.getLabel(), "app", msg, appVersion(), lvl)
//     }
// }

public logToDevice(src, srcType, msg, ver, lvl) {
    if(settings?.logDevice) {
        settings?.logDevice?.sendLog(src, srcType, msg, ver, lvl)
    }
}

def clearDiagLogs(type="all") {
    // log.debug "clearDiagLogs($type)"
    if(type=="all") {
        clearLogHistory()
        getActionApps()?.each { ca-> ca?.clearLogHistory() }
        (isST() ? app?.getChildDevices(true) : getChildDevices())?.each { cd-> cd?.clearLogHistory() }
        return true
    }
    return false
}

Map getLogHistory() {
    return [ warnings: atomicState?.warnHistory ?: [], errors: atomicState?.errorHistory ?: [] ]
}
void clearLogHistory() {
    atomicState?.warnHistory = []
    atomicState?.errorHistory = []
}

private Map deviceSupportMap() {
    return [
        types: [
            "A10A33FOX2NUBK": [ caps: [ "a", "t" ], image: "echo_spot_gen1", name: "Echo Spot" ],
            "A10L5JEZTKKCZ8": [ caps: [ "a", "t" ], image: "vobot_bunny", name: "Vobot Bunny" ],
            "A112LJ20W14H95": [ ignore: true ],
            "A12GXV8XMS007S": [ caps: [ "a", "t" ], image: "firetv_gen1", name: "Fire TV (Gen1)" ],
            "A15ERDAKK5HQQG": [ image: "sonos_generic", name: "Sonos" ],
            "A16MZVIFVHX6P6": [ caps: [ "a", "t" ], image: "unknown", name: "Generic Echo" ],
            "A17LGWINFBUTZZ": [ caps: [ "t", "a" ], image: "roav_viva", name: "Anker Roav Viva" ],
            "A18BI6KPKDOEI4": [ caps: [ "a", "t" ], image: "ecobee4", name: "Ecobee4" ],
            "A18O6U1UQFJ0XK": [ caps: [ "a", "t" ], image: "echo_plus_gen2", name: "Echo Plus (Gen2)" ],
            "A1C66CX2XD756O": [ caps: [ "a", "t" ], image: "amazon_tablet", name: "Fire Tablet HD" ],
            "A1DL2DVDQVK3Q" :  [ blocked: true, ignore: true, name: "Mobile App" ],
            "A1F8D55J0FWDTN": [ caps: [ "a", "t" ], image: "toshiba_firetv", name: "Fire TV (Toshiba)" ],
            "A1GC6GEE1XF1G9": [ ignore: true ],
            "A1H0CMF1XM0ZP4": [ blocked: true, name: "Bose SoundTouch 30" ],
            "A1J16TEDOYCZTN": [ caps: [ "a", "t" ], image: "amazon_tablet", name: "Fire Tablet" ],
            "A1JJ0KFC4ZPNJ3": [ caps: [ "a", "t" ], image: "echo_input", name: "Echo Input" ],
            "A1M0A9L9HDBID3": [ caps: [ "t" ], image: "one-link", name: "One-Link Safe and Sound" ],
            "A1MPSLFC7L5AFK": [ ignore: true ],
            "A1N9SW0I0LUX5Y": [ blocked: false, caps: [ "a", "t" ], image: "unknown", name: "Ford/Lincoln Alexa App" ],
            "A1NL4BVLQ4L3N3": [ caps: [ "a", "t" ], image: "echo_show_gen1", name: "Echo Show (Gen1)" ],
            "A1ORT4KZ23OY88": [ ignore: true ],
            "A1P31Q3MOWSHOD": [ caps: [ "t", "a" ], image: "halo_speaker", name: "Zolo Halo Speaker" ],
            "A1Q7QCGNMXAKYW": [ blocked: true, image: "amazon_tablet", name: "Generic Tablet" ],
            "A1RABVCI4QCIKC": [ caps: [ "a", "t" ], image: "echo_dot_gen3", name: "Echo Dot (Gen3)" ],
            "A1RTAM01W29CUP": [ caps: [ "a", "t" ], image: "alexa_windows", name: "Windows App" ],
            "A1VS6XVTGTLC00": [ ignore: true ],
            "A1VZJGJYCRI78V": [ ignore: true ],
            "A1W2YILXTG9HA7": [ caps: [ "t", "a" ], image: "unknown", name: "Nextbase 522GW Dashcam" ],
            "A1X7HJX9QL16M5": [ blocked: true, ignore: true, name: "Bespoken.io" ],
            "A1Z88NGR2BK6A2": [ caps: [ "a", "t" ], image: "echo_show_gen2", name: "Echo Show 8" ],
            "A1ZB65LA390I4K": [ ignore: true ],
            "A21X6I4DKINIZU": [ ignore: true ],
            "A21Z3CGI8UIP0F": [ ignore: true ],
            "A25EC4GIHFOCSG": [ blocked: true, name: "Unrecognized Media Player" ],
            "A27VEYGQBW3YR5": [ caps: [ "a", "t" ], image: "echo_link", name: "Echo Link" ],
            "A2825NDLA7WDZV": [ ignore: true ],
            "A29L394LN0I8HN": [ ignore: true ],
            "A2C8J6UHV0KFCV": [ ignore: true ],
            "A2E0SNTXJVT7WK": [ caps: [ "a", "t" ], image: "firetv_gen1", name: "Fire TV (Gen2)" ],
            "A2GFL5ZMWNE0PX": [ caps: [ "a", "t" ], image: "firetv_gen1", name: "Fire TV (Gen3)" ],
            "A2HZENIFNYTXZD": [ caps: [ "a", "t" ], image: "facebook_portal", name: "Facebook Portal" ],
            "A2IVLV5VM2W81": [  ignore: true ],
            "A2J0R2SD7G9LPA": [ caps: [ "a", "t" ], image: "lenovo_smarttab_m10", name: "Lenovo SmartTab M10" ],
            "A2JKHJ0PX4J3L3": [ caps: [ "a", "t" ], image: "firetv_cube", name: "Fire TV Cube (Gen2)" ],
            "A2LH725P8DQR2A": [ caps: [ "a", "t" ], image: "fabriq_riff", name: "Fabriq Riff" ],
            "A2LWARUGJLBYEW": [ caps: [ "a", "t" ], image: "firetv_stick_gen1", name: "Fire TV Stick (Gen2)" ],
            "A2M35JJZWCQOMZ": [ caps: [ "a", "t" ], image: "echo_plus_gen1", name: "Echo Plus (Gen1)" ],
            "A2M4YX06LWP8WI": [ caps: [ "a", "t" ], image: "amazon_tablet", name: "Fire Tablet" ],
            "A2OSP3UA4VC85F": [ image: "sonos_generic", name: "Sonos" ],
            "A2R2GLZH1DFYQO": [ caps: [ "t", "a" ], image: "halo_speaker", name: "Zolo Halo Speaker" ],
            "A2RJLFEH0UEKI9": [ ignore: true ],
            "A2T0P32DY3F7VB": [ ignore: true ],
            "A2TF17PFR55MTB": [ ignore: true ],
            "A2TOXM6L8SFS8A": [ ignore: true ],
            "A2V3E2XUH5Z7M8": [ ignore: true ],
            "A2WN1FJ2HG09UN": [ ignore: true ],
            "A18TCD9FP10WJ9": [ ignore: true ],
            "A1FWRGKHME4LXH": [ ignore: true ],
            "A3L2K717GERE73": [ ignore: true, image: "unknown", name: "Voice in a Can (iOS)" ],
            "A222D4HGE48EOR": [ ignore: true, image: "unknown", name: "Voice in a Can (Apple Watch)" ],
            "A19JK51Y4N50K5": [ ignore: true, image: "unknown", name: "Jabra(?)" ],
            "A2X8WT9JELC577": [ caps: [ "a", "t" ], image: "ecobee4", name: "Ecobee5" ],
            "A2XPGY5LRKB9BE": [ caps: [ "a", "t" ], image: "unknown", name: "Fitbit Versa 2" ],
            "A2Y04QPFCANLPQ": [ caps: [ "a", "t" ], image: "unknown", name: "Bose QuietComfort 35 II" ],
            "A2ZOTUOF1IBEYI": [ ignore: true ],
            "A303PJF6ISQ7IC": [ caps: [ "a", "t" ], image: "echo_auto", name: "Echo Auto" ],
            "A195TXHV1M5D4A": [ caps: [ "a", "t" ], image: "echo_auto", name: "Echo Auto" ],
            "A30YDR2MK8HMRV": [ caps: [ "a", "t" ], image: "echo_dot_clock", name: "Echo Dot Clock" ],
            "A32DDESGESSHZA": [ caps: [ "a", "t" ], image: "echo_dot_gen3",  "name" : "Echo Dot (Gen3)" ],
            "A32DOYMUN6DTXA": [ caps: [ "a", "t" ],  image: "echo_dot_gen3",  "name" : "Echo Dot (Gen3)" ],
            "A347G2JC8I4HC7": [ caps: [ "a", "t" ], image: "unknown", name: "Roav Car Charger Pro" ],
            "A37CFAHI1O0CXT": [ image: "logitech_blast", name: "Logitech Blast" ],
            "A37M7RU8Z6ZFB": [ ignore: true ],
            "A37SHHQ3NUL7B5": [ blocked: true, name: "Bose Home Speaker 500" ],
            "A38949IHXHRQ5P": [ caps: [ "a", "t" ], image: "echo_tap", name: "Echo Tap" ],
            "A38BPK7OW001EX": [ blocked: true, name: "Raspberry Alexa" ],
            "A38EHHIB10L47V": [ caps: [ "a", "t" ], image: "tablet_hd10", name: "Fire Tablet HD 8" ],
            "A3B50IC5QPZPWP": [ caps: [ "a", "t" ], image: "unknown", name: "Polk Command Bar" ],
            "A3B5K1G3EITBIF": [ caps: [ "a", "t" ], image: "facebook_portal", name: "Facebook Portal" ],
            "A3CY98NH016S5F": [ caps: [ "a", "t" ], image: "unknown", name: "Facebook Portal Mini" ],
            "A3BRT6REMPQWA8": [ caps: [ "a", "t" ], image: "sonos_generic", name: "Bose Home Speaker 450" ],
            "A3C9PE6TNYLTCH": [ image: "echo_wha", name: "Multiroom" ],
            "A3F1S88NTZZXS9": [ blocked: true, image: "dash_wand", name: "Dash Wand" ],
            "A3FX4UWTP28V1P": [ caps: [ "a", "t" ], image: "echo_plus_gen2", name: "Echo (Gen3)" ],
            "A3H674413M2EKB": [ ignore: true ],
            "A3HF4YRA2L7XGC": [ caps: [ "a", "t" ], image: "firetv_cube", name: "Fire TV Cube" ],
            "A3L0T0VL9A921N": [ caps: [ "a", "t" ], image: "tablet_hd10", name: "Fire Tablet HD 8" ],
            "A3NPD82ABCPIDP": [ caps: [ "t" ], image: "sonos_beam", name: "Sonos Beam" ],
            "A3NVKTZUPX1J3X": [ blocked: true, name: "Unknown Device" ],
            "A3NWHXTQ4EBCZS": [ ignore: true ],
            "A3QPPX1R9W5RJV": [ caps: [ "a", "t" ], image: "fabriq_chorus", name: "Fabriq Chorus" ],
            "A3R9S4ZZECZ6YL": [ caps: [ "a", "t" ], image: "tablet_hd10", name: "Fire Tablet HD 10" ],
            "A3RBAYBE7VM004": [ caps: [ "a", "t" ], image: "echo_studio", name: "Echo Studio" ],
            "A3S5BH2HU6VAYF": [ caps: [ "a", "t" ], image: "echo_dot_gen2", name: "Echo Dot (Gen2)" ],
            "A3SSG6GR8UU7SN": [ caps: [ "a", "t" ], image: "echo_sub_gen1", name: "Echo Sub" ],
            "A3SSWQ04XYPXBH": [ blocked: true, image: "amazon_tablet", name: "Generic Tablet" ],
            "A3TCJ8RTT3NVI7": [ ignore: true ],
            "A3VRME03NAXFUB": [ caps: [ "a", "t" ], image: "echo_flex", name: "Echo Flex" ],
            "A4ZP7ZC4PI6TO": [ caps: [ "a", "t" ], image: "echo_show_5", name: "Echo Show 5 (Gen1)" ],
            "A7WXQPH584YP":  [ caps: [ "a", "t" ], image: "echo_gen2", name: "Echo (Gen2)" ],
            "A81PNL0A63P93": [ caps: [ "a", "t" ], image: "unknown", name: "Home Remote" ],
            "AB72C64C86AW2": [ caps: [ "a", "t" ], image: "echo_gen1", name: "Echo (Gen1)" ],
            "ABP0V5EHO8A4U": [ ignore: true ],
            "AD2YUJTRVBNOF": [ ignore: true ],
            "ADQRVG6LYK4LQ": [ ignore: true ],
            "ADVBD696BHNV5": [ caps: [ "a", "t" ], image: "firetv_stick_gen1", name: "Fire TV Stick (Gen1)" ],
            "AE7X7Z227NFNS": [ caps: [ "a", "t" ], image: "unknown", name: "HiMirror Mini" ],
            "AF473ZSOIRKFJ": [ caps: [ "a", "t" ], image: "unknown", name: "Onkyo VC-PX30" ],
            "AFF50AL5E3DIU": [ caps: [ "a", "t" ], image: "insignia_firetv",  "name" : "Fire TV (Insignia)" ],
            "AGZWSPR7FLP9E": [ ignore: true ],
            "AILBSA2LNTOYL": [ ignore: true ],
            "AKKLQD9FZWWQS": [ blocked: true, caps: [ "a", "t" ], image: "unknown", name: "Jabra Elite" ],
            "AKNO1N0KSFN8L": [ caps: [ "a", "t" ], image: "echo_dot_gen1", name: "Echo Dot (Gen1)" ],
            "AKPGW064GI9HE": [ caps: [ "a", "t" ], image: "firetv_stick_gen1", name: "Fire TV Stick 4K (Gen3)" ],
            "AO6HHP9UE6EOF": [ caps: [ "a", "t" ], image: "unknown", name: "Unknown Media Device" ],
            "AP1F6KUH00XPV": [ blocked: true, name: "Stereo/Subwoofer Pair" ],
            "AP4RS91ZQ0OOI": [ caps: [ "a", "t" ], image: "toshiba_firetv", name: "Fire TV (Toshiba)" ],
            "ATH4K2BAIXVHQ": [ ignore: true ],
            "AUPUQSVCVHXP0": [ ignore: true ],
            "AVD3HM0HOJAAL": [ image: "sonos_generic", name: "Sonos" ],
            "AVE5HX13UR5NO": [ caps: [ "a", "t" ], image: "logitech_zero_touch", name: "Logitech Zero Touch" ],
            "AVN2TMX8MU2YM": [ blocked: true, name: "Bose Home Speaker 500" ],
            "AWZZ5CVHX2CD":  [ caps: [ "a", "t" ], image: "echo_show_gen2", name: "Echo Show (Gen2)" ],
            "A2C8J6UHV0KFCV": [ caps: [ "a", "t" ], image: "unknown", name: "Alexa Gear" ],
            "AUPUQSVCVHXP0": [ caps: [ "a", "t" ], image: "unknown", name: "Ecobee Switch+" ],
            "A2RJLFEH0UEKI9": [ ignore: true ],
            "AKOAGQTKAS9YB": [ ignore: true ],
            "A37M7RU8Z6ZFB": [ caps: [ "a", "t" ], image: "unknown", name: "Garmin Speak" ],
            "A2WN1FJ2HG09UN": [ caps: [ "a", "t" ], image: "unknown", name: "Ultimate Alexa App" ],
            "A2BRQDVMSZD13S": [ caps: [ "a", "t" ], image: "unknown", name: "SURE Universal Remote" ],
            "A3TCJ8RTT3NVI7": [ caps: [ "a", "t" ], image: "unknown", name: "Alexa Listens" ]
        ],
        families: [
            block: [ "AMAZONMOBILEMUSIC_ANDROID", "AMAZONMOBILEMUSIC_IOS", "TBIRD_IOS", "TBIRD_ANDROID", "VOX", "MSHOP" ],
            echo: [ "ROOK", "KNIGHT", "ECHO" ],
            other: [ "REAVER", "FIRE_TV", "FIRE_TV_CUBE", "ALEXA_AUTO", "MMSDK" ],
            tablet: [ "TABLET" ],
            wha: [ "WHA" ]
        ]
    ]
}
