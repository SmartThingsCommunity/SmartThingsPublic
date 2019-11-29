/*
Blue Iris Fusion  (parent app, child app is Blue Iris Camera Triggers - Trigger)

Created by FLYJMZ (flyjmz230@gmail.com)

Based on work by:
Tony Gutierrez in "Blue Iris Profile Integration"
jpark40 at https://community.smartthings.com/t/blue-iris-profile-trigger/17522/76
luma at https://community.smartthings.com/t/blue-iris-camera-trigger-from-smart-things/25147/9

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
for the specific language governing permissions and limitations under the License.
*/


//////////////////////////////////////////////////////////////////////////////////////////////
///                                     App Info                                            //
//////////////////////////////////////////////////////////////////////////////////////////////
/*
SmartThings Community Thread: 
NEW:  https://community.smartthings.com/t/release-bi-fusion-v3-0-adds-blue-iris-device-type-handler-blue-iris-camera-dth-motion-sensing/103032
OLD:  https://community.smartthings.com/t/release-blue-iris-fusion-integrate-smartthings-and-blue-iris/54226

Github Code: 
https://github.com/flyjmz/jmzSmartThings/tree/master/smartapps/flyjmz/blue-iris-fusion.src

Child app can be found on Github: 
https://github.com/flyjmz/jmzSmartThings/tree/master/smartapps/flyjmz/blue-iris-fusion-trigger.src

Blue Iris Server Device Type Handler on Github: 
https://github.com/flyjmz/jmzSmartThings/tree/master/devicetypes/flyjmz/blue-iris-server.src

Server DTH SmartThings Community Thread:  
https://community.smartthings.com/t/release-blue-iris-device-handler/

Blue Iris Camera Device Type Handler on Github: 
https://github.com/flyjmz/jmzSmartThings/tree/master/devicetypes/flyjmz/blue-iris-camera.src

Version 1.0 - 30July2016    Initial release
Version 1.1 - 3August2016   Cleaned up Code
Version 2.0 - 16Oct2016     Added Profile integration.  Also set up option for local connections, but doesn't work.  Standby for updates to make it work.
Version 2.1 - 14Dec2016     Got local connection to work!  If you have issues, try external.  External is very stable.
Version 2.2 - 2Jan2017      Found out the local connection issue, "Local Only" setting in Blue Iris Webserver Settings cannot be checked.
Version 2.3 - 17Jan2017     Added preference to turn debug logging on or off.
Version 2.4 - 22Jan2017     Fixed error in profile change notifications (they all said temporary even if it was a hold change)
Version 2.5 - 23Jan2017     Slight tweak to notifications.
Version 2.6 - 17Jun2017     Fixed Profile names when using in LAN (localAction was throwing NULL). Thanks Zaxxon!
Version 3.0 - 26Oct2017     Added Blue Iris Server and Camera Device Type Integration with motion, profile integration, manual triggering and manual profile switching.
"                           Also added App Update Notifications, cleaned up notifications, added OAuth for motion alerts
Version 3.0.1 - 28Oct2017   Fixed bug where server device map was would fail to initialize when user wasn't using it (so now it doesn't generate unless desired) - it would make install fail even if not using the Server DTH 
"                           Fixed bug that would only only allow one camera device to install.
"                           Enabled full Camera Device DTH support even without using Server DTH.
"                           Changed Software Update input (now it asks if you want to disable vs ask if you want to enable...so it defaults to enabled).
Version 3.0.2 - 1Nov2017    Code updated to allow user to change Camera Device Names after installation (can change in devices' settings, the change in BI Fusion preferences is irrelevant unless the shortname changes as well).                             
Version 3.0.3 - 26Nov2017   Code cleanup; added Live Logging display of Motion URLs; updated "secure only" terminology since Blue Iris changed it.
Version 3.0.4 - 29Nov2017   Added a method to rename camera devices to bicamera[i] without also having the shortname, which will now let people rename shortnames too.
"                           Added an option to have it not auto-delete old camera devices, hopefully this will let people get out of the loop of changing something but not knowing how to change it back in order to continue.
Version 3.0.5 - 8Dec17      Fixed Error when user ties a ST mode to BI's Inactive profile (the 0 was being treated as false and not switching modes for automatic mode integration)
"                           Improved settings and operation when not using profile<>mode integration.
"                           Added step in DNI fix method to prevent renaming already renamed devices.
Version 3.0.6 - 24Dec17     Cleaned up log.info verbage
"                           Fixed new install flow so that OAUTH tokens are created if they haven't already been (so you don't have to hit the switch first)
"                           Added ability to add custom polling interval for server DTH
Version 3.1 - 5Mar18        Added handling for "cameradevice.moveToPreset" command w/error checking //NOTE: I need folks to test this for me!
Version 3.2 - 17Apr18       Hopefully fixed external profile switch error
Version 3.2.1 - 18Apr18     Cleaned up some of the logs, fixed the external command lock code (1 & 2 are opposite in external vs local commands)
Version 3.2.2 - 6Jul2018    Updated notes after comfirming v3.2.1's fixes worked & did some logging cleanup. Updated language in preferences.  Custom polling code redone to be more robust. Added Periodic notifications for server offline.
Version 3.2.3 - 23Jul2018  	Updated Nofications to support SmartThings' depricating the Contact Book feature (which was a dumb move on their part).
Version 3.2.4 - 6Feb2019	Added timestamps to notifications. Added cameradisplayName to cameraSettings.

TODO:
- make this work with the smartapp installer app:  https://community.smartthings.com/t/beta-community-smartapp-installer/118348
- Make this all work with MQTT or using session keys so that we don't have to uncheck that secure login box in blue iris?
- Delete "Fix for having DNI tied to shortname" code chunk from v3.0.5.
- Change timing of software update notifications (so user doesn't get spammed every day...give them some options)
- make this work for more profiles than modes (or 2 profiles on the same mode)?  in the integration setup, list all the profiles and have them type in the mode name?
--Allow user to enter own name.  So have them select mode, and then below where they can give names to the rest of the profiles, just show all the profiles and let them overwrite the defaulted-in ST mode
--------I have a todo down the way with a suggestion for 2 profiles to one mode, but can i create fake modes for profiles that don't have an equivalent mode?
--------Or first ask if they have more than one so that normal folks can just number the modes.  
--------Or maybe better yet is list each profile and then click on it to go to another setup page to set it up.  You'd select a mode from a list, give it a different name if desired, or create a fake ST mode.
- See todos throughout.
- getting "error physicalgraph.app.exception.smartAppException: Method Not Allowed" 3-5 times in log 5-10 seconds after mode switches to away
    >>this isn't happening in any other mode switches. ?????
    >>need to check if this is still happening after all the other updates first
- there is a todo for adding call back to local mode when not using bi server dth...

-Add ability to enter both LAN and WAN address for: failover, camera live feed
-Try to get motion alerts from BI to Camera Devices without using OAuth.  Some example code in here already (lanEventHandler), and look at:
https://community.smartthings.com/t/smartthings-labs-and-lan-devices/1100/11
https://community.smartthings.com/t/poll-or-subscribe-example-to-network-events/72862/15
and maybe:
https://community.smartthings.com/t/help-receiving-http-events-from-raspberry-pi/3629/14
https://community.smartthings.com/t/tutorial-creating-a-rest-smartapp-endpoint/4331
*/

def appVersion() {"3.2.4"}

mappings {
    path("/active/:camera") {
        action: [GET: "cameraActiveHandler"]
    }
    path("/inactive/:camera") {
        action: [GET: "cameraInactiveHandler"]
    }
}

definition(
    name: "Blue Iris Fusion",
    namespace: "flyjmz",
    author: "flyjmz230@gmail.com",
    description: "Full SmartThings mode integration with Blue Iris Profiles, plus SmartThings can trigger Blue Iris Camera recording, and use Blue Iris Cameras as motion sensors.",
    category: "Safety & Security",
    iconUrl: "https://raw.githubusercontent.com/flyjmz/jmzSmartThings/master/resources/BlueIris_logo.png",
    iconX2Url: "https://raw.githubusercontent.com/flyjmz/jmzSmartThings/master/resources/BlueIris_logo%402x.png",
    singleInstance: true
)

preferences {
    page(name:"BIFusionSetup")
    page(name:"BIServerSetup")
    page(name:"integrationSetup")
    page(name:"cameraDeviceSetup")
    page(name:"oauthSetup")
    page(name:"oauthView")
}

def BIFusionSetup() {
    dynamicPage(name:"BIFusionSetup", title: "BI Fusion Setup", install: true, uninstall: true, submitOnChange: true) {
        section("Blue Iris Server Settings") {
            href(name: "BIServerSetup", title: "Blue Iris Server Settings", required: false, page: "BIServerSetup")
        }
        section("Blue Iris Profile <=> SmartThings Mode Integration") {
            href(name: "integrationSetup", title: "Blue Iris Profile <=> SmartThings Mode Integration", required: false, page: "integrationSetup")
        }
        section("Blue Iris Camera Installation") {
            href(name: "cameraDeviceSetup", title: "Blue Iris Camera Installation", required: false, page: "cameraDeviceSetup")
        }
        section("Blue Iris Camera Triggers") {
            app(name: "Blue Iris Fusion - Trigger", appName: "Blue Iris Fusion - Trigger", namespace: "flyjmz", title: "Add Camera Trigger", multiple: true)
        }
        section("Notification Delivery Settings", hidden: false, hideable: true) {
        	input "useTimeStamp", "bool", title: "Add timestamp to messages?", required: false
            input("recipients", "contact", title: "Send notifications to") {
                input "wantsPush", "bool", title: "Send Push Notification? (pushes to all this location's users)", required: false
                paragraph "If you want SMS Notifications, enter phone numbers including the country code (1 for USA), otherwise leave blank. Separate multiple numbers with a semi-colon (;). Only enter the numbers, no spaces or special characters."
                input "phoneNumbers", "string", title: "Enter Phone Numbers for SMS Notifications:", required: false
            }
        }
        section("Advanced", hidden: true, hideable: true){
            paragraph "You can turn on debug logging, viewed in Live Logging on the API website."
            def loggingOn = false
            input "loggingOn", "bool", title: "Debug Logging On?"
            paragraph "New software version notifications are sent automatically, but can be disabled."
            input "updateAlertsOff", "bool", title: "Disable software update alerts?", required:false
        }
    }
}

def BIServerSetup() {
    dynamicPage(name:"BIServerSetup", title: "BI Server Setup", submitOnChange: true) {
        section("Blue Iris Server Device Type") {
            paragraph "Blue Iris Server provides more complete BI control through ST.  See https://community.smartthings.com/t/release-blue-iris-device-handler/101765"
            input "usingBIServer", "bool", title: "Use/Install Blue Iris Server?", required: true, submitOnChange: true
            paragraph "NOTE: If you want to remove the device but keep BI Fusion installed, just turn this off."
            paragraph "NOTE: The Blue Iris Server Device Type requires a 'local' connection between the ST hub and BI server.  BI Fusion can handle external connections, but not when using the Server Device."
            if(usingBIServer) {
                paragraph "NOTE: Ensure the Blue Iris Server Device Type Handler is already added to your account on the SmartThings API."
                paragraph "NOTE: Once installed, do not edit the server device settings from within the device's preferences page. Make all changes within this BI Fusion app."
            }
        }
        section("Blue Iris Server Login Settings") {
            paragraph "Note: Username, Password, and camera Shortnames cannot contain special characters or spaces."
            input "username", "text", title: "Blue Iris Username", required: true
            input "password", "password", title: "Blue Iris Password", required: true
            paragraph "Note: Blue Iris only allows Admin Users to toggle profiles."
            if(usingBIServer) {
                input "host", "text", title: "Blue Iris Server IP (only include the IP)", description: "e.g. 192.168.0.14", required:true
                input "port", "number", title: "Blue Iris Server Port", description: "e.g. 81", required:true
                paragraph "NOTE: Ensure 'Use secure session keys and login page' is not checked in Blue Iris Webserver - Advanced settings."
                double waitThreshold = 5
                input "waitThreshold", "number", title: "Custom Server Health Monitor, max server response time: (sec)", description: "Default: 5 seconds", required:false
                input "pollingInterval", "enum", title: "Custom polling interval? (min)", description: "Default: 15 minutes", options: ["1", "5", "10", "15", "30", "60"], required: false
                input "periodicNotifications", "bool", title: "Receive Periodic Notifications for Errors?", required: false, submitOnChange: true 
                if (periodicNotifications) {
                    input "periodicNotificationsTiming", "number", title: "Periodic Notification Interval (minutes between messages):", description: "Defaults to 15 min", required: false
                }
            } else {
                paragraph "Local or External Connection to Blue Iris Server (i.e. LAN vs WAN)?"
                paragraph "(External requires port forwarding/VPN/etc so the SmartThings Cloud can reach your BI server.)"
                paragraph "(Local does not support notifications confirming the changes were actually made.)"
                input "localOnly", "bool", title: "Local connection?", required: true, submitOnChange: true
                if (localOnly) {
                    paragraph "NOTE: When using a local connection, you need to ensure 'Use secure session keys and login page' is not checked in Blue Iris Webserver - Advanced settings."
                    paragraph "Use the local IP address for Host, do not include http:// or anything but the IP address."
                    input "host", "text", title: "BI Webserver IP Address", description: "e.g. 192.168.0.14", required:true
                } else {
                    paragraph "Since you're using an external connection, use the external IP address for Webserver Host, and be sure to include the full address (i.e. include http:// or https://, .com, etc)."
                    paragraph "If you are using Stunnel, ensure the SSL certificate is from a Certificate Authority (CA), it cannot be self-signed. You can create a free CA signed certificate at www.letsencrypt.org"
                    input "host", "text", title: "BI Webserver Host (include http(s)://)", required:true
                }
                input "port", "number", title: "BI Webserver Port (e.g. 81)", required:true
            }
        }
    }
}

def integrationSetup() {
    dynamicPage(name:"integrationSetup", title: "Blue Iris Profile <=> SmartThings Mode Integration", submitOnChange: true) {
        section("Blue Iris Profile/SmartThings Mode Integration") {
            paragraph "You can have BI Fusion update Blue Iris Profiles when SmartThings Modes Change (e.g. SmartThings goes to away, set Blue Iris to away)."
            input "autoModeProfileSync", "bool", title: "Auto Sync BI Profile to ST Mode?", required: true, submitOnChange: true
            if (autoModeProfileSync) {
                paragraph "Enter your Blue Iris Profile Number (1-7, use 0 for Inactive) for the matching SmartThings mode. To ignore a mode leave it blank.  Each Blue Iris Profile can only be used once (e.g. BI Profile 1 cannot be used for ST modes Home and Away, it has to be only one or the other)."
                location.modes.each { mode ->
                    def modeId = mode.id.toString()  
                    input "mode-${modeId}", "number", title: "Mode ${mode}", required: false, submitOnChange: true
                }
            }
            if (usingBIServer) {
                paragraph "You can optionally add the Blue Iris Profile Names (leave blank to ignore a profile or use the default)."
                def takenProfiles = []
                location.modes.each { mode ->
                    def checkMode = "mode-${mode.id.toString()}"
                    if (settings[checkMode] != null) {
                        takenProfiles += settings[checkMode].toInteger()
                    }
                }
                if (loggingOn) log.debug "takenProfiles is ${takenProfiles}"
                if (!takenProfiles.contains(0)  || !autoModeProfileSync) input "profile0", "text", title: "BI Inactive", description: "Default: Inactive", required:false
                if (!takenProfiles.contains(1) || !autoModeProfileSync) input "profile1", "text", title: "BI Profile #1", description: "Default: Profile 1", required:false
                if (!takenProfiles.contains(2) || !autoModeProfileSync) input "profile2", "text", title: "BI Profile #2", description: "Default: Profile 2", required:false
                if (!takenProfiles.contains(3) || !autoModeProfileSync) input "profile3", "text", title: "BI Profile #3", description: "Default: Profile 3", required:false
                if (!takenProfiles.contains(4) || !autoModeProfileSync) input "profile4", "text", title: "BI Profile #4", description: "Default: Profile 4", required:false
                if (!takenProfiles.contains(5) || !autoModeProfileSync) input "profile5", "text", title: "BI Profile #5", description: "Default: Profile 5", required:false
                if (!takenProfiles.contains(6) || !autoModeProfileSync) input "profile6", "text", title: "BI Profile #6", description: "Default: Profile 6", required:false
                if (!takenProfiles.contains(7) || !autoModeProfileSync) input "profile7", "text", title: "BI Profile #7", description: "Default: Profile 7", required:false
            }
            if (autoModeProfileSync) {
                paragraph "You can make the automatic profile changes either 'Hold' or 'Temporary' changes."
                paragraph "Hold changes remain until the next change is made, even through computer/server restart.  Temporary changes will only be in effect for the 'Temp Time' duration set for each profile in Blue Iris Settings > Profiles. At the end of that time, Blue Iris will change profiles according to your schedule."
                paragraph "Note: if Blue Iris restarts while a temporary profile is set, it will set the profile according to it's schedule."
                input "holdChanges", "bool", title: "Make Hold changes?", required: true
                paragraph "Profile changes will display in SmartThings Notifications Feed.  Do you also want to receive PUSH/SMS notifications?"
                input "receiveAlerts", "enum", title: "Receive PUSH/SMS on Profile Change?", options: ["Yes", "Errors Only", "No"], required: true
            } 
        }
    }
}

def cameraDeviceSetup() {
    dynamicPage(name:"cameraDeviceSetup", title: "Blue Iris Camera Installation", submitOnChange: true) {
        section("Blue Iris Camera Device Creation") {
            paragraph "You can install devices for each of your cameras to act as motion sensor devices in SmartThings and to allow SmartThings to trigger them to record."
            input "installCamaraDevices", "bool", title: "Install Cameras?", required: true, submitOnChange: true 
            paragraph "NOTE: To uninstall the camera devices but keep BI Fusion installed, just set cameras to install to '0' and turn off this switch"
            if (installCamaraDevices) {
                paragraph "Ensure the Blue Iris Camera Device Type Handler is already added to your account on the SmartThings API."
                input "howManyCameras", "number", title: "How many cameras do you want to install?", required: true, submitOnChange: true 
                paragraph "Create a new device for each camera by entering the Blue Iris short name (case-sensitive, do not use special characters)."
                paragraph "Display Names are optional. They default to 'BI Cam - [short name]'.  To change a Display Name after device creation, edit the name in the device's own settings page in the SmartThings App (it won't do anything if you change it here)."
                paragraph "NOTE: You have to click 'Done' to complete BI Fusion setup prior to re-entering settings to create any any triggers."
                for (int i = 0; i < howManyCameras; i++) {
                    input "camera${i}shortName", "text", title: "Camera ${i} Short Name", description: "e.g. frontporch", required: true
                    input "camera${i}displayName", "text", title: "Camera ${i} Display Name", description: "e.g. Front Porch", required: false
                }
                paragraph "If you're having trouble changing settings, you can turn off auto-deletion for old camera devices. ADVANCED USERS ONLY"
                input "skipDeletion", "bool", title: "Do not delete old camera devices?", required: false
            }

        }
        if (installCamaraDevices) {
            section ("Blue Iris Motion Alert Setup") {
                paragraph "You will need to copy the addresses and change the camera names for each camera you want to get motion from. The addresses will display after clicking to open the page below."
                def createNewAddresses = false
                input "createNewAddresses", "bool", title: "Do you want to (re)create the URLs?", required: false, submitOnChange: true
                paragraph "(Leave Off in Order to VIEW ONLY...Not Change)"
                input "debugDisplaysURLs", "bool", title: "Display URLs in API Logs?", required: false
                paragraph "If turned on, the URLs will populate in SmartThings API's Live Logging when you initialize this smartapp.  If you open the API on the computer running Blue Iris, you can then copy and paste the URLs."
            }
            if (createNewAddresses) {
                section("CHANGE & View Motion Alert URLs") {
                    href(name: "oauthSetup", title: "CHANGE & View Motion Alert URLs", required: false, page: "oauthSetup")
                }
            } else {
                section("View Only Motion Alert URLs") {
                    href(name: "oauthView", title: "View Only Motion Alert URLs", required: false, page: "oauthView")
                }
            }
        }
    }
}

def oauthSetup() {
    dynamicPage(name:"oauthSetup", title: "CHANGED Blue Iris Alert URLs") {
        createBIFusionToken()
        section("") {
            paragraph "Take a screenshot of this page, then enter the addresses following the directions on the previous page."
            paragraph "WARNING: THESE ADDRESSES ARE NEW!"
        }
        section("Motion Active URL") {
            def activeURL = apiServerUrl("/api/smartapps/installations/${app.id}/active/cameraShortNameHere?access_token=${state.accessToken}")
            paragraph "Motion Active URL is: ${activeURL}"
        }
        section("Motion Inactive URL") {
            def inactiveURL = apiServerUrl("/api/smartapps/installations/${app.id}/inactive/cameraShortNameHere?access_token=${state.accessToken}")
            paragraph "Motion Inactive URL is: ${inactiveURL}"
        }
    }
}

def oauthView() {
    dynamicPage(name:"oauthView", title: "CURRENT Blue Iris Alert URLs") {
        if (state.accessToken == null) createBIFusionToken()
        section("") {
            paragraph "Take a screenshot of this page, then enter the addresses following the directions on the previous page."
        }
        section("Motion Active URL") {
            def activeURL = apiServerUrl("/api/smartapps/installations/${app.id}/active/cameraShortNameHere?access_token=${state.accessToken}")
            paragraph "Motion Active URL is: ${activeURL}"
        }
        section("Motion Inactive URL") {
            def inactiveURL = apiServerUrl("/api/smartapps/installations/${app.id}/inactive/cameraShortNameHere?access_token=${state.accessToken}")
            paragraph "Motion Inactive URL is: ${inactiveURL}"
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    unschedule()
    initialize()
}

def uninstalled() {
    removeChildDevices(getChildDevices(true))  //todo - make this optional?  Folks are having trouble deleting the app because they didn't un-associate the child devices from other smartapps yet.  but if we make it optional, they'd end up with broken children all over.
    unschedule()
    revokeAccessToken()
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def initialize() {
    log.info "initialized"
    createInfoMaps()
    if (autoModeProfileSync) subscribe(location, modeChange)
    if (loggingOn) log.debug "Initialized with settings: ${settings}"
    if (debugDisplaysURLs) {
        log.info "PLEASE REMEMBER TO SELECT HTTPS:// FROM THE DROPDOWN AND DON'T TYPE IT IN"
        for (int i = 0; i < howManyCameras; i++) {
            def cameraShortNameInput = "camera${i}shortName"
            def cameraShortName = settings[cameraShortNameInput].toString()
            def activeURL = apiServerUrl("/api/smartapps/installations/${app.id}/active/${cameraShortName}?access_token=${state.accessToken}")
            def inactiveURL = apiServerUrl("/api/smartapps/installations/${app.id}/inactive/${cameraShortName}?access_token=${state.accessToken}")
            log.info "${cameraShortName} Active URL:" + "\n" + "${activeURL}" - "https://" + "\n" +"${cameraShortName} Inactive URL:" + "\n" + "${inactiveURL}" - "https://"
        }
    }
    //if (installCamaraDevices) subscribe(location, null, lanEventHandler, [filterEvents:false])  //for new motion...todo - test
    schedule(now(), checkForUpdates)
    checkForUpdates()
}

///////////////////////////////////////////////////////////////////////////
//                  BI FUSION 3.X Code (Uses Device Type Handlers)      ///
///////////////////////////////////////////////////////////////////////////
def createInfoMaps() {
    //First create Profile:
    if (state.profileModeMap != null) state.profileModeMap.clear()  //wipes it clean to prevent weird carryover
    state.profileModeMap = [[modeName: "Inactive"],
                            [modeName: "Profile 1"], 
                            [modeName: "Profile 2"],
                            [modeName: "Profile 3"],
                            [modeName: "Profile 4"],
                            [modeName: "Profile 5"],
                            [modeName: "Profile 6"],
                            [modeName: "Profile 7"]]  //Don't need the numbers, to get Profile 1's Mode's Name use: state.profileModeMap[1].modeName
    if (profile0 != null) state.profileModeMap[0].modeName = profile0
    if (profile1 != null) state.profileModeMap[1].modeName = profile1
    if (profile2 != null) state.profileModeMap[2].modeName = profile2
    if (profile3 != null) state.profileModeMap[3].modeName = profile3
    if (profile4 != null) state.profileModeMap[4].modeName = profile4
    if (profile5 != null) state.profileModeMap[5].modeName = profile5
    if (profile6 != null) state.profileModeMap[6].modeName = profile6
    if (profile7 != null) state.profileModeMap[7].modeName = profile7
    if (autoModeProfileSync) {
        location.modes.each { mode ->                               //todo- this section prevents users from using the same BI profile number for multiple ST Modes (ie home and night are both profile 2).  
            //Probably need to run a script to see if multiple modes have the same number and combine the name, eg "Home/Night".  Then in this and in the server DTH,
            //instead of comparing the actual ST mode to the result of getProfileName(), have it compare the number from BI's return to getProfileNumber()
            def checkMode = "mode-${mode.id.toString()}"
            if (settings[checkMode] != null) {
                state.profileModeMap[settings[checkMode].toInteger()].modeName = "${mode.name}" //For each ST mode, it determines if the user made profile number for it in settings, then uses that profile number as the map value number and fills the name.
            }
        }
    }
    if (loggingOn) log.debug "state.profileModeMap map: ${state.profileModeMap}"

    //Second create BI Server Settings Map:
    state.blueIrisServerSettings = [:]
    if (usingBIServer) { 
        state.blueIrisServerSettings.host = host
        state.blueIrisServerSettings.port = port
        state.blueIrisServerSettings.username = username
        state.blueIrisServerSettings.password = password
        state.blueIrisServerSettings.autoModeProfileSync = autoModeProfileSync
        state.blueIrisServerSettings.profileModeMap = state.profileModeMap
        //the network ID needs to be the hex ip:port or mac address for the BI Server Computer (I use IP because it's easier for user, but mac would be easier to code):
        def hosthex = convertIPtoHex(host).toUpperCase()  //Note: it needs to be set to uppercase for the new deviceNetworkId to work in SmartThings
        def porthex = convertPortToHex(port).toUpperCase()
        state.blueIrisServerSettings.DNI = "$hosthex:$porthex"   //Change: this was: if (usingBIServer) state.blueIrisServerSettings.DNI = "$hosthex:$porthex"
        //else state.blueIrisServerSettings.DNI = null
        state.blueIrisServerSettings.waitThreshold = waitThreshold
        state.blueIrisServerSettings.pollingInterval = pollingInterval
        state.blueIrisServerSettings.holdChanges = holdChanges
        state.blueIrisServerSettings.loggingOn = loggingOn
        state.blueIrisServerSettings.periodicNotifications = periodicNotifications
        state.blueIrisServerSettings.periodicNotificationsTiming = (periodicNotificationsTiming != null) ? periodicNotificationsTiming : 15
        if (loggingOn) log.debug "state.blueIrisServerSettings map: ${state.blueIrisServerSettings}"
    }

    //Third create the Camera Devices Map:

    //Fix for having DNI tied to shortname:////  todo - only need this for a while to make sure everyone had updated names, then can delete.
    if (installCamaraDevices) {
        def previousChildDevices = getChildDevices(true)
        if (loggingOn) log.debug "DNI fix step 1a.  Found these devices: ${previousChildDevices}"
        previousChildDevices.each {
            if (it.deviceNetworkId.toString().startsWith("bicamera")) {
                if (loggingOn) log.debug "DNI fix step 1b.  Device '${it.deviceNetworkId.toString()}' is a camera"
                if (it.deviceNetworkId.toString().length() > 11) {
                    if (loggingOn) log.debug "DNI fix step 1c.  Device '${it.deviceNetworkId.toString()}' has an old name, proceeding to step 2"
                    if (howManyCameras > 10 && howManyCameras < 99) {  //anything higher than bicamera9 (which started at 0, so bicamera9 is camera #10)
                        if (loggingOn) log.debug "DNI fix step 2.  Device '${it.deviceNetworkId}' is changing to '${it.deviceNetworkId.toString().take(10)}'"
                        it.deviceNetworkId = it.deviceNetworkId.toString().take(10)
                    } else if (howManyCameras < 11) {   //bicamera0-bicamera9
                        if (loggingOn) log.debug "DNI fix step 2.  Device '${it.deviceNetworkId}' is changing to '${it.deviceNetworkId.toString().take(9)}'"
                        it.deviceNetworkId = it.deviceNetworkId.toString().take(9)
                    } else log.error "Cannot rename cameras if more than 99 are installed"
                }  //else, not a camera device, skip it
            }
        }
    }
    ///////////////////////////////////////////

    state.cameradeviceDNI = []
    state.camerashortName = []
    state.cameradisplayName = []
    if (installCamaraDevices) {
        for (int i = 0; i < howManyCameras; i++) {
            def cameraShortNameInput = "camera${i}shortName"
            def cameraDisplayNameInput = "camera${i}displayName"
            state.cameradeviceDNI[i] = "bicamera" + i
            state.camerashortName[i] = settings[cameraShortNameInput].toString()
            if (settings[cameraDisplayNameInput]?.toString() == null) state.cameradisplayName[i] = "BI Cam - " + settings[cameraShortNameInput].toString()
            else state.cameradisplayName[i] = settings[cameraDisplayNameInput].toString()
        }
        if (loggingOn) log.debug "state.cameradeviceDNI: ${state.cameradeviceDNI}, state.camerashortName: ${state.camerashortName}, state.cameradisplayName: ${state.cameradisplayName}"
    }
    state.cameraSettings = [:]
    state.cameraSettings.host = host
    state.cameraSettings.port = port
    state.cameraSettings.username = username
    state.cameraSettings.password = password
    state.cameraSettings.shortName = ""
    state.cameraSettings.displayName = ""

    //Finally, go make the devices:
    makeDevices()
}

def makeDevices() {
    //First, delete any old devices not in the settings any more:
    def installedChildDevices = getChildDevices(true)
    def wantedChildDevices = []
    wantedChildDevices.clear()
    if (usingBIServer) {
        def serverDNI = state.blueIrisServerSettings.DNI.toString()
        wantedChildDevices += serverDNI
    }
    if (installCamaraDevices) {
        state.cameradeviceDNI.each {
            if (it != null) {
                wantedChildDevices += it
            }
        }
    }
    if (loggingOn) log.debug "installedChildDevices found: $installedChildDevices, and wantedChildDevices are: $wantedChildDevices"
    installedChildDevices.each {
        def childDNI = it.deviceNetworkId
        if (it != null && !wantedChildDevices.contains(childDNI)) {
            if (!it.deviceNetworkId.toString().startsWith("bicamera")) {  //if not a camera, then it's a server, and we always delete and rebuild
                deleteChildDevice(it.deviceNetworkId) 
            } else {  //it is a camera
                if (!skipDeletion)  deleteChildDevice(it.deviceNetworkId) //and we want to delete them 
            }
        } //else not deleting since we want it
    }
    //Then install devices if user wants:
    if (usingBIServer) createBlueIrisServerDevice()
    if (installCamaraDevices) createCameraDevice()   
}


//////////////////////      Server Device Creation      ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////

def createBlueIrisServerDevice() {
    def serverDevice = getChildDevice(state.blueIrisServerSettings.DNI)       
    try {
        if (!serverDevice) { //double check that it isn't already installed
            serverDevice = addChildDevice("flyjmz", "Blue Iris Server", state.blueIrisServerSettings.DNI, location.hubs[0].id, [name: "Blue Iris Server", label: "Blue Iris Server", completedSetup: true])
            log.info "Blue Iris Server Created"
        } else {
            log.info "Blue Iris Server already created"
        }
    } catch (e) {
        log.error "Error creating Blue Iris Server device: ${e}"
    }

    //Update the Server Settings regardless of whether it was just created or not, and subscribe to it's events:
    serverDevice.initializeServer(state.blueIrisServerSettings)
    subscribe(serverDevice, "cameraPresetOk", cameraPresetOkHandler)
    if (receiveAlerts == "Errors Only") {
        subscribe(serverDevice, "errorMessage", serverDeviceErrorMessageHandler)  //returns a message string
    } else if (receiveAlerts == "Yes") {
        subscribe(serverDevice, "errorMessage", serverDeviceErrorMessageHandler)  //returns a message string
        subscribe(serverDevice, "blueIrisProfile", serverDeviceProfileHandler) //returns profile number
        subscribe(serverDevice, "stoplight", serverDeviceStopLightHandler)  //returns ["red", "green", "yellow"]
    } else if (receiveAlerts == "No") {
        //no server events to subscribe to, because we don't receive any that are just for messaging
    }

    //Code for motion active/inactive from BI Server Device.  OAuth setup overrode this, but I'd like to go back (todo):
    //subscribe(serverDevice, "cameraMotionActive", cameraActiveHandler)    
    //subscribe(serverDevice, "cameraMotionInactive", cameraInactiveHandler)
}                   

def serverDeviceProfileHandler(evt) {
    if (loggingOn) log.debug "serverDeviceProfileHandler() received {$evt}"
    send("Blue Iris Profile set to ${evt.value}")
}

def serverDeviceStopLightHandler(evt) {
    if (loggingOn) log.debug "serverDeviceStopLightHandler() received {$evt}"
    send("Blue Iris Stoplight set to ${evt.value}")
}

def serverDeviceErrorMessageHandler(evt) {
    log.error "serverDeviceErrorMessageHandler() received {$evt.descriptionText}"
    send("${evt.descriptionText}")
}

/*      //Code for motion active/inactive from BI Server Device.  OAuth setup overrode this, but I'd like to go back (todo).
def cameraActiveHandler(evt) {  //receives triggered status from BI through BI Server Device, and sends it to the Camera device
if (loggingOn) log.debug "cameraActiveHandler() got event: '${evt.displayName}'. Camera '${evt.value}' is active."
log.trace "cameraActiveHandler() got event: '${evt.displayName}'. Camera '${evt.value}' is active."
def shortName = evt.device.name
def cameraDNI = ""
def biCameraSize = state.biCamera.size()
for (int i = 0; i < biCameraSize; i++) {
if (state.biCamera[i].shortName == shortName) cameraDNI = state.biCamera[i].deviceDNI
}
log.trace "cameraDNI is $cameraDNI"
def cameraDevice = getChildDevice(cameraDNI)
cameraDevice.active()
}

def cameraInactiveHandler(evt) {  //receives triggered status from BI through BI Server Device, and sends it to the Camera device
if (loggingOn) log.debug "cameraInactiveHandler() got event: '${evt.displayName}'. Camera '${evt.value}' is inactive."
def shortName = evt.device.name
def cameraDNI = ""
def biCameraSize = state.biCamera.size()
for (int i = 0; i < biCameraSize; i++) {
if (state.biCamera[i].shortName == shortName) cameraDNI = state.biCamera[i].deviceDNI
}
log.trace "cameraDNI is $cameraDNI"
def cameraDevice = getChildDevice(cameraDNI)
cameraDevice.inactive()
}
*/

//////////////////////      Camera Device Creation      ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
def createCameraDevice() {
    for (int i = 0; i < howManyCameras; i++) {  
        def cameraDevice = getChildDevice(state.cameradeviceDNI[i])
        if (!cameraDevice) {    //double check that it isn't already installed
            try {
                cameraDevice = addChildDevice("flyjmz", "Blue Iris Camera", state.cameradeviceDNI[i], location.hubs[0].id, [name: "${state.camerashortName[i]}", label: "${state.cameradisplayName[i]}", completedSetup: true])
                if (loggingOn) log.debug "'${state.cameradisplayName[i]}' Device Created"
                subscribe(cameraDevice, "switch.on", cameraTriggerHandler)
                subscribe(cameraDevice, "cameraPreset", cameraPresetHandler)
                state.cameraSettings.shortName = state.camerashortName[i]
                state.cameraSettings.displayName = state.cameradisplayName[i]
                cameraDevice.initializeCamera(state.cameraSettings)  //pass settings
            } catch (e) {
                log.error "Error creating '${state.cameradisplayName[i]}' Device: ${e}"
            }
        } else {
            if (loggingOn) log.debug "Camera with dni of '${state.cameradeviceDNI[i]}' already exists."
            subscribe(cameraDevice, "switch.on", cameraTriggerHandler) //still have to subscribe (intialize() wiped old subscriptions)
            subscribe(cameraDevice, "cameraPreset", cameraPresetHandler)
            state.cameraSettings.shortName = state.camerashortName[i]
            state.cameraSettings.displayName = state.cameradisplayName[i]
            cameraDevice.initializeCamera(state.cameraSettings)  //pass/update settings
        }
    }
}

def cameraTriggerHandler(evt) {  //sends command to camera to start recording whenever the camera device is 'turned on' (which is done by the Camera DTH and/or other apps controlling the Camera DTH).
    //WARNING - you don't want to have a camera triggered event also run this, because you'll end up in a loop.
    if (loggingOn) log.debug "cameraTriggerHandler() got event ${evt.displayName} is ${evt.value}"
    def shortName = evt.device.name
    def cameraMapSize = state.cameradeviceDNI.size()
    if (usingBIServer) {
        def serverDevice = getChildDevice(state.blueIrisServerSettings.DNI)
        serverDevice.triggerCamera(shortName)       //sends command through the BI Server Device
    } else {
        if(localOnly) {
            def triggerCameraCommand = "/admin?camera=${shortName}&trigger&user=${username}&pw=${password}"
            localAction(triggerCameraCommand)      //sends command through local action if not using the BI Server Device
        } else {
            externalAction("trigger",shortName)     //sends command through external action if not using the BI Server Device
        }
    }
}

def cameraPresetHandler(evt) {
    if (loggingOn) log.debug "cameraPresetHandler() got event ${evt.displayName} is preset ${evt.value}"
    def shortName = evt.device.name
    def preset = evt.value.toInteger()
    if (usingBIServer || localOnly) {
        def localPreset = 7 + preset //1-7 are other actions, presets start at number 8.  //todo - no idea if evt.value will give the preset name...
        log.info "Moving ${shortName} to preset ${preset} via local command"
        def presetCommand = "/cam/${shortName}/pos=${localPreset}&user=${username}&pw=${password}"
        localAction(presetCommand)
        runIn(waitThreshold,cameraPresetErrorChecker)
    } else {
        def externalPreset = preset + 100  //Blue Iris JSON command for preset is 101...120 for preset 1...20
        externalAction("preset",["shortName":shortName,"preset":externalPreset])
    }
}

def cameraPresetOkHandler(evt) {
    state.cameraPresetOk = true
}

def cameraPresetErrorChecker() {
    if (!state.cameraPresetOk && (receiveAlerts == "Errors Only" || receiveAlerts == "Yes")) {
        log.error "BI Camera did not move to commanded preset"
        send("BI Camera did not move to commanded preset")
    }
    state.cameraPresetOk = false
}


/////                   Camera Motion Code (Using OAuth)                           /////
////////////////////////////////////////////////////////////////////////////////////////

def createBIFusionToken() {
    try {
        if (state.accessToken) revokeAccessToken()
        createAccessToken()
        log.info "created new token"
    } catch (Exception e) {
        log.error "Error: Can't create access token, is OAuth enabled in the SmartApp Settings? Error: $e"
        send("Error: Can't create access token, is OAuth enabled in the SmartApp Settings?")
        return
    }
}

/*
def lanEventHandler(evt) {  //todo -- see if i can make this work
def msg = parseLanMessage(evt.value)
def body = msg.body
log.debug "lanEventHandler() got msg $msg and body $body"
//def headerString = new String(parsedEvent.headers.decodeBase64())     
//def bodyString = new String(parsedEvent.body.decodeBase64())
}
*/

def cameraActiveHandler() {
    def cameraShortName = params.camera
    log.info "'$cameraShortName' is active."
    try {
        def cameraDNI = ""
        for (int i = 0; i < state.cameradeviceDNI.size(); i++) {
            if (state.camerashortName[i] == cameraShortName) cameraDNI = state.cameradeviceDNI[i]
        }
        if (cameraDNI != "") {
            def cameraDevice = getChildDevice(cameraDNI)
            cameraDevice.active()
        } else {
            log.error "error 30a: Camera Motion Received but received camera shortname '$cameraShortName' not in list.  Check Blue Iris Alert settings for camera."
            sendEvent(name: "errorMessage", value: "Camera Motion Received but received camera shortname '$cameraShortName' not in list.  Check Blue Iris Alert settings for camera.", descriptionText: "Camera Motion Received but received camera shortname not in list.  Check Blue Iris Alert settings for camera.", displayed: true)
        }
    } catch (Exception e) {
        log.error "error 30: Camera Motion Received but failed to send motion to ST device. Error: $e"
        sendEvent(name: "errorMessage", value: "Active Camera Motion Received but failed to send motion to ST device, check settings", descriptionText: "Active Camera Motion Received but failed to send motion to ST device, check settings", displayed: true)
    }
}

def cameraInactiveHandler() {
    def cameraShortName = params.camera
    log.info "'$cameraShortName' is inactive."
    try {
        def cameraDNI = ""
        for (int i = 0; i < state.cameradeviceDNI.size(); i++) {
            if (state.camerashortName[i] == cameraShortName) cameraDNI = state.cameradeviceDNI[i]
        }
        if (cameraDNI != "") {
            def cameraDevice = getChildDevice(cameraDNI)
            cameraDevice.inactive()
        } else {
            log.error "error 31a: Camera Motion stopped but received camera shortname '$cameraShortName' not in list.  Check Blue Iris Alert settings for camera."
            sendEvent(name: "errorMessage", value: "Camera Motion stopped but received camera shortname '$cameraShortName' not in list.  Check Blue Iris Alert settings for camera.", descriptionText: "Camera Motion stopped but received camera shortname not in list.  Check Blue Iris Alert settings for camera.", displayed: true)
        }
    } catch (Exception e) {
        log.error "error 31: Camera Motion Stopped but failed to update ST device. Error: $e"
        sendEvent(name: "errorMessage", value: "Camera Motion Stopped but failed to update ST device, check settings", descriptionText: "Camera Motion Stopped but failed to update ST device, check settings", displayed: true)
    }
}

private String convertIPtoHex(ipAddress) {
    try {
        String hex = ipAddress.tokenize('.').collect {String.format('%02x', it.toInteger())}.join()
        return hex
    } catch (Exception e) {
        log.error "error 12: Invalid IP Address $ipAddress, check settings. Error: $e"
        sendEvent(name: "errorMessage", value: "Invalid IP Address $ipAddress, check settings", descriptionText: "Invalid Blue Iris Server IP Address $ipAddress, check settings", displayed: true)
    }
}

private String convertPortToHex(port) {
    if (!port || (port == 0)) {
        log.error "error 13: Invalid port $port, check settings."
    }
    try {
        String hexport = port.toString().format('%04x', port.toInteger())
        return hexport
    } catch (Exception e) {
        log.error "error 14: Invalid port $port, check settings. Error: $e"
    }
}


///////////////////////////////////////////////////////////////////////////
//                  BI FUSION 2.X Code (No devices required)
///////////////////////////////////////////////////////////////////////////
def modeChange(evt) {
    if (evt.name != "mode") {return;}
    log.info "mode change detected, mode now: " + evt.value
    def checkMode = ""

    location.modes.each { mode ->
        if (mode.name == evt.value){
            checkMode = "mode-" + mode.id
            if (loggingOn) log.debug "BI_modeChange matched to " + mode.name
        }
    }

    if (checkMode != "" && settings[checkMode] != null) {
        def profile = settings[checkMode].toInteger()
        if (usingBIServer) {
            def device = getChildDevice(state.blueIrisServerSettings.DNI)
            device.setProfile(profile)  //sends profile change through device
        } else {
            if(localOnly){
                log.info "Changing Blue Iris Profile to ${profile} via local command"
                def lock = 2  
                //Blue Iris Param "&lock=0/1/2" makes profile changes as: run/temp/hold, not sure what 'run' means...
                //NOTE: Local commands use this, whereas external commands have the 1 & 2 switched! CAO 18Apr2018
                if(holdChanges) {
                    if(receiveAlerts == "No" || receiveAlerts == "Errors Only") sendNotificationEvent("Blue Iris Fusion hold changed Blue Iris to profile ${profile}")
                    if(receiveAlerts == "Yes") send("Blue Iris Fusion hold changed Blue Iris to profile ${profile}")
                } else {
                    lock = 1
                    if(receiveAlerts == "No" || receiveAlerts == "Errors Only") sendNotificationEvent("Temporarily changed Blue Iris to profile ${profile}")
                    if(receiveAlerts == "Yes") send("Temporarily changed Blue Iris to profile ${profile}")
                }
                def profileChangeCommand = "/admin?profile=${profile}&lock=${lock}&user=${username}&pw=${password}"
                localAction(profileChangeCommand)   //sends profile change through local lan (like device) except through the app
            } else externalAction("profile",profile)  //sends profile change from SmartThings cloud to BI server
        }  
    }
}

def localAction(command) {
    def biHost = "${host}:${port}"
    def httpMethod = "GET"
    def httpRequest = [
        method:     httpMethod,
        path:       command,
        headers:    [
            HOST:       biHost,
            Accept:     "*/*",
        ]
    ]
    def hubAction = new physicalgraph.device.HubAction(httpRequest)
    sendHubCommand(hubAction)  //todo - add callback function for error checking
    if (loggingOn) log.debug hubAction
}

def externalAction(commandType,stringCommand) {  //can accept string of either: number for profile change or shortname for camera trigger
    def lock = 2
    if (holdChanges) lock = 1  //note, help file says 1 = hold, 2 = temp, which is backwards from local commands.
    try {
        httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"login"]) { response ->
            if (loggingOn) log.debug "response 1: " + response.data
            if (response.data.result == "fail") {
                if (loggingOn) log.debug "BI_Inside initial call fail, proceeding to login"  //todo - does this do anything?  shouldn't it include the last session (as a state variable) and then if it is logged in, then skip to the task? (which would need to be a separate method then)
                def session = response.data.session
                def hash = username + ":" + response.data.session + ":" + password
                hash = hash.encodeAsMD5()
                httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"login","session":session,"response":hash]) { response2 ->
                    if (loggingOn) log.debug "response 2: " + response2.data
                    if (response2.data.result == "success") {
                        def BIprofileNames = response2.data.data.profiles
                        if (loggingOn) log.debug ("BI_Logged In")
                        httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"status","session":session]) { response3 ->
                            if (loggingOn) log.debug "response 3: " + response3.data
                            if (response3.data.result == "success") {
                                if (loggingOn) log.debug ("BI_Retrieved Status")
                                //Begin Profile Change Code
                                if (commandType == "profile") {
                                    def profile = stringCommand.toInteger()
                                    def newProfile = null
                                    log.info "Changing Blue Iris Profile to ${profile} via external command"
                                    if (response3.data.data.profile != profile) {        
                                        httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"status","profile":profile, "session":session]) { response4 -> //note: cannot set "lock" in JSON command, the only way to do a hold is to send the "profile" command twice
                                            if (loggingOn) log.debug "response 4: " + response4.data
                                            def lockStatus = response4.data.data.lock.toInteger()
                                            def profileChangedTo = response4.data.data.profile.toInteger()
                                            //////send command again to make it a hold change (because you can't send "lock" in a JSON command)/////
                                            if (holdChanges) {
                                                httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"status","profile":profile, "session":session]) { response6 ->
                                                    if (loggingOn) log.debug "response 15: " + response6.data
                                                    lockStatus = response6.data.data.lock.toInteger()
                                                    profileChangedTo = response6.data.data.profile.toInteger()
                                                }
                                            }
                                            //////////
                                            log.info ("Blue Iris is now in profile ${profileName(BIprofileNames,profile)}, and lock is ${lockStatus}!")
                                            if (profileChangedTo == profile && lockStatus == lock) {
                                                log.info "BI Fusion synced profile successfully"
                                                if (receiveAlerts == "No" || receiveAlerts == "Errors Only") sendNotificationEvent("BI Fusion changed Blue Iris to profile ${profileName(BIprofileNames,profile)} successfully")
                                                if (receiveAlerts == "Yes") send("BI Fusion changed Blue Iris to profile ${profileName(BIprofileNames,profile)} successfully")
                                            } else if (profileChangedTo == profile && lockStatus != lock) {
                                                log.error "BI Fusion changed Blue Iris to profile ${profileName(BIprofileNames,profile)} successfully, but Hold/Temp type is incorrectly '${externalLockName(lockStatus)}'"
                                                if (receiveAlerts == "No") sendNotificationEvent("BI Fusion changed Blue Iris to profile ${profileName(BIprofileNames,profile)} successfully, but Hold/Temp type is incorrectly '${externalLockName(lockStatus)}.'")
                                                if (receiveAlerts == "Yes" || receiveAlerts == "Errors Only") send("BI Fusion changed Blue Iris to profile ${profileName(BIprofileNames,profile)} successfully, but Hold/Temp type is incorrectly '${externalLockName(lockStatus)}.'")
                                            } else if (profileChangedTo != profile) {
                                                log.error "BI Fusion failed to change profiles, Blue Iris profile is '${profileName(BIprofileNames,response4.data.data.profile)}.'"
                                                if (receiveAlerts == "No") sendNotificationEvent("BI Fusion failed to change profiles, Blue Iris profile is '${profileName(BIprofileNames,response4.data.data.profile)}.'")
                                                if (receiveAlerts == "Yes" || receiveAlerts == "Errors Only") send("BI Fusion failed to change profiles, Blue Iris profile is '${profileName(BIprofileNames,response4.data.data.profile)}.'")
                                            }   
                                            httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"logout","session":session]) { response5 ->
                                                if (loggingOn) log.debug "Logged Out, response 5: " + response5.data
                                            }
                                        }
                                    } else {
                                        log.info ("Blue Iris is already at profile ${profileName(BIprofileNames,profile)}.")
                                        sendNotificationEvent("Blue Iris is already in profile ${profileName(BIprofileNames,profile)}.")
                                    }
                                    //End Profile Change Code
                                } else if (commandType == "trigger") {
                                    //Begin Trigger code
                                    def shortName = stringCommand
                                    log.info "Triggering $shortName via external command"
                                    httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"trigger","camera":shortName,"session":session]) { response4 ->
                                        if (loggingOn) log.debug "response 7: " + response4.data
                                        if (response4.data.result == "success") {
                                            log.info "${shortName} triggered"
                                            if (receiveAlerts == "No") sendNotificationEvent("Blue Iris Fusion triggered camera '${shortName}'")
                                            if (receiveAlerts == "Yes" || receiveAlerts == "Errors Only") send("Blue Iris Fusion triggered camera '${shortName}'")
                                            httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"logout","session":session]) { response5 ->
                                                if (loggingOn) log.debug "logged out, response 8: " + response5.data
                                            }
                                        } else {
                                            log.error "BI Fusion Error: ${shortName} not triggered"
                                            if (loggingOn) log.debug "response 9: " + response4.data.data.reason
                                            if (receiveAlerts == "No") sendNotificationEvent("BI Fusion Error: ${shortName} not triggered")
                                            if (receiveAlerts == "Yes" || receiveAlerts == "Errors Only") send("BI Fusion Error: ${shortName} not triggered")
                                        }
                                    }
                                    //End Trigger code
                                } else if (commandType == "preset") {
                                    def shortName = stringCommand.shortName
                                    def preset = stringCommand.preset.toInteger()
                                    log.info "Moving ${state.shortNameList} to preset ${preset} via external command"
                                    def presetNumber = state.presetList[i] + 100  //Blue Iris JSON command for preset is 101...120 for preset 1...20
                                    httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"ptz","camera":shortName,"button":presetNumber, "session":session]) { response4 -> 
                                        if (parent.loggingOn) log.debug "response 10: " + response4.data
                                        if (response4.data.result == "success") {
                                            log.info "BI Fusion moved $shortName to preset $preset"
                                        } else {
                                            log.error "BI Fusion Failure: preset $preset not sent to $shortName"
                                            if (parent.loggingOn) log.debug "response 11: " + response4.data.data.reason
                                            if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure: preset $preset not sent to $shortName")
                                            if (receiveNotifications) parent.send("BI Fusion Failure: preset $preset not sent to $shortName")
                                        }
                                    }
                                } else {log.error "externalAction() commandType is ${commandType}, not supported"}
                            } else {
                                log.error "BI Fusion Error: Could not retrieve current status"
                                if (loggingOn) log.debug "response 12: " + response3.data.data.reason
                                if (receiveAlerts == "No") sendNotificationEvent("BI Fusion Error: Could not retrieve current status")
                                if (receiveAlerts == "Yes" || receiveAlerts == "Errors Only") send("BI Fusion Error: Could not retrieve current status")
                            }
                        }
                    } else {
                        log.error "BI Fusion Error: Could not login"
                        if (loggingOn) log.debug "response 13: " + response2.data.data.reason
                        if (receiveAlerts == "No") sendNotificationEvent("BI Fusion Error: Could not login")
                        if (receiveAlerts == "Yes" || receiveAlerts == "Errors Only") send("BI Fusion Error: Could not login")
                    }
                }
            } else {
                log.error "BI Fusion Error: Could not login"
                if (loggingOn) log.debug "response 14: " + response.data.data.reason
                if (receiveAlerts == "No") sendNotificationEvent("BI Fusion Error: Could not login")
                if (receiveAlerts == "Yes" || receiveAlerts == "Errors Only") send("BI Fusion Error: Could not login")
            }
        }
    } catch(Exception e) {
        log.error "BI Fusion Error: External Connection to Blue Iris Failed. Error: $e"
        if (receiveAlerts == "No") sendNotificationEvent("BI Fusion Error: External Connection to Blue Iris Failed. Error: $e")
        if (receiveAlerts == "Yes" || receiveAlerts == "Errors Only") send("BI Fusion Error: External Connection to Blue Iris Failed. Error: $e")
    }
}

def profileName(names, num) {
    if (names[num.toInteger()]) {
        names[num.toInteger()] + " (#${num})"
    } else {
        '#' + num
    }
}
def externalLockName(num) {  //note, this only works for external commands, local commands have the 1/2 switched.
    if (num == 0)  return "Run"
    if (num == 1)  return "Hold"
    if (num == 2)  return "Temporary"
}

def checkForTriggerUpdates() {
    log.info "Checking for Trigger app updates"
    def childApps = getChildApps()
    def installedVersion = "0.0"
    def installed = false
    if (childApps[0] != null) {
        installedVersion = childApps[0].appVersion()
        installed = true
    } else {}  //no triggers installed
    if (loggingOn) log.debug "Trigger child app installedVersion is $installedVersion"
    def name = "BI Fusion - Trigger"
    def website = "https://raw.githubusercontent.com/flyjmz/jmzSmartThings/master/smartapps/flyjmz/blue-iris-fusion-trigger.src/version.txt"
    if (installed) checkUpdates(name, installedVersion, website)
}

def checkForFusionUpdates() {
    log.info "Checking for BI Fusion app updates"
    def installedVersion = appVersion()
    def name = "BI Fusion"
    def website = "https://raw.githubusercontent.com/flyjmz/jmzSmartThings/master/smartapps/flyjmz/blue-iris-fusion.src/version.txt"
    checkUpdates(name, installedVersion, website)
}

def checkForCameraUpdates() {
    log.info "Checking for Camera device code updates"
    def name = "Blue Iris Camera Device Type Handler"
    def website = "https://raw.githubusercontent.com/flyjmz/jmzSmartThings/master/devicetypes/flyjmz/blue-iris-camera.src/version.txt"
    def installedVersion = getChildDevice(state.cameradeviceDNI[0]).appVersion()
    if (installed) checkUpdates(name, installedVersion, website)
}

def checkForServerUpdates() {
    log.info "Checking for Server device code updates"
    def name = "Blue Iris Server Device Type Handler"
    def website = "https://raw.githubusercontent.com/flyjmz/jmzSmartThings/master/devicetypes/flyjmz/blue-iris-server.src/version.txt"
    def installedVersion = getChildDevice(state.blueIrisServerSettings.DNI).appVersion()
    if (installedVersion) checkUpdates(name, installedVersion, website)
}

def checkForUpdates() {
    checkForFusionUpdates()
    checkForTriggerUpdates()
    if (installCamaraDevices) checkForCameraUpdates()
    if (usingBIServer) checkForServerUpdates()
}

def checkUpdates(name, installedVersion, website) {
    if (loggingOn) log.debug "${name} running checkForUpdates() with an installedVersion of $installedVersion, at website $website"
    def publishedVersion = "0.0"
    try {
        httpGet([uri: website, contentType: "text/plain; charset=UTF-8"]) { resp ->
            if(resp.data) {
                publishedVersion= resp?.data?.text.toString()   //For some reason I couldn't add .trim() to this line, or make another variable and add it there.  The rest of the code would run, but notifications failed. Just having .trim() in the notification below failed a bunch, then started working again...
                if (loggingOn) log.debug "publishedVersion found is $publishedVersion"
                return publishedVersion
            } else  log.error "checkUpdates retrievePublishedVersion response from httpGet was ${resp} for ${name}"
        }
    }
    catch (Exception e) {
        log.error "checkForUpdates() couldn't get the current ${name} code verison. Error:$e"
        publishedVersion = "0.0"
    }
    if (loggingOn) log.debug "${name} publishedVersion from web is ${publishedVersion}, installedVersion is ${installedVersion}"
    def instVerNum = 0              
    def webVerNum = 0
    if (publishedVersion && installedVersion) {  //make sure no null
        def instVerMap = installedVersion.tokenize('.')  //makes a map of each level of the version
        def webVerMap = publishedVersion.tokenize('.')
        def instVerMapSize = instVerMap.size()
        def webVerMapSize = webVerMap.size()
        if (instVerMapSize > webVerMapSize) {   //handles mismatched sizes (e.g. they have v1.3 installed but v2 is out and didn't write it as v2.0)
            def sizeMismatch = instVerMapSize - webVerMapSize
            for (int i = 0; i < sizeMismatch; i++) {
                def newMapPosition = webVerMapSize + i  //maps' first postion is [0], but size would count it, so the next map position is i in the loop because i starts with 0
                webVerMap[newMapPosition] = 0  //just make it a zero, the actual goal is to increase the size of the map
            }
        } else if (instVerMapSize < webVerMapSize) {
            def sizeMismatch = webVerMapSize - instVerMapSize
            for (int i = 0; i < sizeMismatch; i++) {
                def newMapPosition = instVerMapSize + i  
                instVerMap[newMapPosition] = 0
            }
        }
        instVerMapSize = instVerMap.size() //update the sizes incase we just changed the maps
        webVerMapSize = webVerMap.size()
        if (loggingOn) log.debug "${name} instVerMapSize is $instVerMapSize and the map is $instVerMap and webVerMapSize is $webVerMapSize and the map is $webVerMap"
        for (int i = 0; i < instVerMapSize; i++) {
            instVerNum = instVerNum + instVerMap[i]?.toInteger() *  Math.pow(10, (instVerMapSize - i))  //turns the version segments into one big additive number: 1.2.3 becomes 100+20+3 = 123
        }
        for (int i = 0; i < webVerMapSize; i++) {
            webVerNum = webVerNum + webVerMap[i]?.toInteger() * Math.pow(10, (webVerMapSize - i)) 
        }
        if (loggingOn) log.debug "${name} publishedVersion is now ${webVerNum}, installedVersion is now ${instVerNum}"
        if (webVerNum > instVerNum) {
            def msg = "${name} Update Available. Update in IDE. v${installedVersion} installed, v${publishedVersion.trim()} available."
            if (updateAlertsOff) sendNotificationEvent(msg)  //Message is only displayed in Smartthings app notifications feed
            if (!updateAlertsOff) send(msg) //Message sent to push/SMS per user, plus the Smartthings app notifications feed
        } else if (webVerNum == instVerNum) {
            log.info "${name} is current."
        } else if (webVerNum < instVerNum) {
            log.error "Your installed version of ${name} seems to be higher than the published version."
        }
    } else if (!publishedVersion) {log.error "Cannot get published app version for ${name} from the web."}
}

private send(msg) {
    if (useTimeStamp) {
    	def stamp = new Date().format('yyyy-M-d HH:mm:ss',location.timeZone)
        msg = msg + " (" + stamp + ")"
    }
    
	//First try to use Contact Book (Depricated 30July2018)
    if (location.contactBookEnabled) {
        if (loggingOn) log.debug("sending '$msg' notification to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    
    //Otherwise use old school Push/SMS notifcations
    else {
        if (loggingOn) log.debug("sending message to app notifications tab: '$msg'")
        sendNotificationEvent(msg)  //First send to app notifications (because of the loop we're about to do, we need to use this version to avoid multiple instances) 
        if (wantsPush) {
            sendPushMessage(msg)  //Second, send the push notification if user wanted it
            if (loggingOn) log.debug("sending push message")
        }

        if (phoneNumbers) {	//Third, send SMS messages if desired
            if (phoneNumbers.indexOf(";") > 1) {	//Code block for multiple phone numbers
                def phones = phoneNumbers.split(";")
                for (int i = 0; i < phones.size(); i++) {
                    if (loggingOn) log.debug("sending SMS to ${phones[i]}")
                    sendSmsMessage(phones[i], msg)
                }
            } else {	//Code block for single phone number
                if (loggingOn) log.debug("sending SMS to ${phoneNumbers}")
                sendSmsMessage(phoneNumbers, msg)
            }
        }
    }
}