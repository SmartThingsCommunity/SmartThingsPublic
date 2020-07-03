/**
 *  Tesla Powerwall Manager 
 * 
 *  Copyright 2019, 2020 DarwinsDen.com
 *  
 *  ****** WARNING ****** USE AT YOUR OWN RISK!
 *  This software was developed in the hopes that it will be useful to others, however, 
 *  it is beta software and may have unforeseen side effects to your equipment and related accounts.
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
 *  Access Token initialization code is derived from Trent Foley's excellent Tesla Connect SmartThings Smart App:
 *     https://github.com/trentfoley/SmartThingsPublic/blob/master/smartapps/trentfoley/tesla-connect.src/tesla-connect.groovy 
 *
 */
def version() {
    return "v0.2.8e.20200702"
}

/* 
 *	02-Jul-2020 >>> v0.2.8e.20200702 - Added dashboard tile display from local gateway iframe for Hubitat. 
 *	27-May-2020 >>> v0.2.7e.20200527 - Handle extra null battery site info from Tesla. Handle no time zone set. 
 *	02-Mar-2020 >>> v0.2.6e.20200302 - Correct mobile notifications
 *	29-Feb-2020 >>> v0.2.5e.20200229 - Additional http command and query error checks. Added option to pause automations.
 *	19-Feb-2020 >>> v0.2.4e.20200219 - Added battery charge % triggers time and day restriction options.
 *	31-Jan-2020 >>> v0.2.3e.20200131 - Added battery charge % triggers & TBC Strategy scheduling.
 *	22-Jan-2020 >>> v0.2.2e.20200122 - Added Stormwatch on/off scheduling.
 *	16-Jan-2020 >>> v0.2.1e.20200116 - Additional command retry/error checking logic. Hubitat battery% compatibility update.
 *	10-Jan-2020 >>> v0.2.0e.20200110 - Push notification support for Hubitat
 *	04-Jan-2020 >>> v0.1.8e.20200104 - Updated async http call for cross-platform support with Hubitat & SmartThings
 *	03-Jan-2020 >>> v0.1.7e.20200103 - Added access token refresh & command post retry logic 
 *	30-Dec-2019 >>> v0.1.6e.20191230 - Increased reserve percentage value options 
 *	06-Sep-2019 >>> v0.1.5e.20190906 - Updated watchdog to only notify once when issue first occurs and when resolved 
 *	13-Aug-2019 >>> v0.1.4e.20190813 - Added grid/outage status display, notifications, and device on/off controls 
 *	09-Aug-2019 >>> v0.1.3e.20190809 - Added reserve% scheduling & polling interval preferences
 *	29-Jul-2019 >>> v0.1.2e.20190729 - Set reserve percent to 100% in backup-only mode. Added mode scheduling.
 *	23-Jul-2019 >>> v0.1.1e.20190723 - Initial beta release
 */

definition(
    name: "Tesla Powerwall Manager", namespace: "darwinsden", author: "Darwin", description: "Monitor and control your Tesla Powerwall",
    importUrl:
    "https://raw.githubusercontent.com/DarwinsDen/SmartThingsPublic/master/smartapps/darwinsden/tesla-powerwall-manager.src/tesla-powerwall-manager.groovy",
    category: "My Apps",
    iconUrl: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/pwLogoAlphaCentered.png",
    iconX2Url: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/pwLogoAlphaCentered.png"
)

preferences {
    page(name: "pageMain")
    page(name: "pageConnections")
    page(name: "pageConnectionMethod")
    page(name: "teslaAccountInfo")
    page(name: "gatewayAccountInfo")
    page(name: "pageNotifications")
    page(name: "pageSchedules")
    page(name: "pageTriggers")
    page(name: "aboveTriggerOptions")
    page(name: "belowTriggerOptions")
    page(name: "pageReserveSchedule")
    page(name: "pageRemove")
    page(name: "schedule1Options")
    page(name: "schedule2Options")
    page(name: "schedule3Options")
    page(name: "schedule4Options")
    page(name: "schedule5Options")
    page(name: "schedule6Options")
    page(name: "schedule7Options")
    page(name: "pagePwPreferences")
    page(name: "pageDevicesToControl")
    page(name: "triggerRestrictions")
    page(name: "pageCustomizeGwTile")
}

private pageMain() {
    return dynamicPage(name: "pageMain", title: "", install: true) {
        section() {
            if (hubIsSt()) {
                paragraph app.version(),
                    title: "PowerWall Manager", required: false, image:
                    "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/pwLogoAlphaCentered.png"
             } else {
                def imgLink = "<img src='https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/pwLogoAlphaCentered.png' height=100 width=75>"
				paragraph "<div style='height: 75px; float: left; margin-top:-22px; padding:0; text-align:left; overflow: hidden'>${imgLink}</div>" +
                    "<div style='float: left; margin-top: 6px; margin-left: 16px'>Powerwall Manager\n ${app.version()}</div>"
            }               
        }
        
         section("Powerwall Connections") {
            if (hubIsSt()) {
                href "pageConnections", title: "Account Information..", description: "", required: false, image:
                 "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/Tesla-Icon50.png"
            } else {  
                href "pageConnections", title: "Tesla Account and/or Powerwall Gateway Information..", description: "", required: false, image:
                 "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/Tesla-Icon50.png"
            }
        }

        section("Preferences") {
            href "pageNotifications", title: "Notification Preferences..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/notification50.png"
            href "pageSchedules", title: "Schedule Powerwall setting changes..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/calendar50.png"
            href "pageTriggers", title: "Perform actions based on Powerwall battery charge level %..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/battery50.png"
            href "pageDevicesToControl", title: "Turn off devices when a grid outage occurs..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/outage50.png"
            href "pagePwPreferences", title: "Powerwall Manager Preferences..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/cog50.png"
        }
        section("For more information") {
            if (hubIsSt()) {
               href(name: "Site", title: "For more information, questions, or to provide feedback, please visit: DarwinsDen.com/powerwall",
               description: "Tap to open the Powerwall Manager web page on DarwinsDen.com",
               required: false,
               image: "https://darwinsden.com/download/ddlogo-for-pwmanager-0-png",
               url: "https://darwinsden.com/powerwall/")
            } else {
               def tag="https://darwinsden.com/powerwall/"
               def imgLink="<img src=https://darwinsden.com/download/ddlogo-for-pwmanager-0-png height=50 width=70>"
               def msg="For more information, questions, or to provide feedback, please visit: DarwinsDen.com/powerwall/\n" +
                    "Tap to open the Powerwall Manager web page on DarwinsDen.com"
               paragraph "<div style='float: left; margin-right: 8px; max-width: 200px><a href='${tag}'>${imgLink}</a></div><a href='${tag}'>${msg}</a>"
            }
        }
        section("Remove this app") {
            href "pageRemove", title: "Remove Powerwall Manager", description: "", required: false
        }
    }
}

def pageRemove() {
    dynamicPage(name: "pageRemove", title: "", install: false, uninstall: true) {
        section() {
            paragraph parent ? "CAUTION: You are about to remove the '${app.label}'." : "If an error occurs during removal, " +
                "you may need to first manually remove references to the Powerwall Manager child device from other smart apps if you have manually added them.",
                required: true, state: null
        }
    }
}

private teslaAccountInfo() {
    return dynamicPage(name: "teslaAccountInfo", title: "", install: false) {
        resetAccountAccess()
        section("Tesla Account Information: ") {
            input "userEmail", "text", title: "Email", autoCorrect: false, required: false
            input "userPw", "password", title: "Password", autoCorrect: false, required: false
        }
    }
}

private gatewayAccountInfo() {
    return dynamicPage(name: "gatewayAccountInfo", title: "", install: false) {
           section("Local Gateway Information. If provided, the Powerwall Manager will also (or only if no Tesla Account information is provided) " +
                  "obtain real-time meter and battery level data from the gateway itself. The Powerwall Manager does not currently support mode and state status " +
                   "or command capability directly from the local gateway.") {
               input("gatewayAddress", "string", title: "Powerwall Gateway IP local address (eg. 192.168.1.200)", required: false )
            //input "installerEmail", "text", title: "Gateway  Unstaller Email", autoCorrect: false, required: false
            //input "installerPw", "password", title: "Gateway Installer Password", autoCorrect: false, required: false
           }
      }
}

def getConnectionMethodStatus() {
    def statusStr
    if (!connectionMethod) {
        statusStr = "Use Remote Tesla Account Server Only"
    } else {
        if (connectionMethod == "Use Local Gateway Only") {
            statusStr = connectionMethod.toString() + ".\n Note: A Tesla Server connection is required for full Powerwall Manager capabilities."
        } else {
           statusStr = connectionMethod.toString()
        }
    }
    return statusStr
}       

def pageConnectionMethod() {
    dynamicPage(name: "pageConnectionMethod", title:"Choose how to connect to the Powerwall.", install: false, uninstall: false) {
        section("Connection Method") {
             input "connectionMethod", "enum", required: false, defaultValue: "Use Remote Tesla Account Server Only", title: "Connection Method", 
                 options: ["Use Remote Tesla Account Server Only", "Use Local Gateway Only", "Use Both Tesla Server and Local Gateway"]
         }
    }
}
        
def pageConnections() {
    dynamicPage(name: "pageConnections", title:"Choose how to connect to the Powerwall.", install: false, uninstall: false) {
        String statusString
        if (!hubIsSt()) {
           section("Connection Method:") {
               statusString = getConnectionMethodStatus()
               href "pageConnectionMethod", title: "${statusString}", description: ""
            }
        }
        if (!connectionMethod || connectionMethod != "Use Local Gateway Only") {
            statusString = getTeslaServerStatus()
            section("Enter Tesla Server Account Information:") {
               href "teslaAccountInfo", title: "${statusString}", description: ""
            }
        }
        if (connectionMethod && connectionMethod != "Use Remote Tesla Account Server Only") {
            statusString = getLocalGwStatus()
            section("Enter Gateway Address:") {
               href "gatewayAccountInfo", title: "${statusString}", description: ""
            }
            if (statusString.take(22)=="Local Gateway Verified") {
               section("Customize Gateway Dashboard Tile:") {
                   statusString = "Height: ${tileHeight?.toLong() ?: 517} (default 517 pixels)\n" +                
                                      "Width:  ${tileWidth?.toLong() ?: 460} (default 460 pixels)\n" + 
                                      "Scale:  ${tileScale?.toFloat() ?: 0.81} (default 0.81)\n\n"  + 
                                       getTileStr(0.5) +   
                   "\n&#8226To view this attribute tile on your dashboard, you may need to first visit the gateway URL in your dashboard browser " +
                   "and accept the self-signed certificate exception." +
                   "\n&#8226Add to .css to remove extra tile padding: #tile-XX .tile-contents, #tile-XX .tile-primary {padding: 0;}"
                    section("Customize Gateway Dashboard Tile:") {
                        href "pageCustomizeGwTile", title: "${statusString}", description: ""

                    }
               }
            }
        }
    }
}

def pageCustomizeGwTile() {
    dynamicPage(name: "pageCustomizeGwTile", title:"Customize Gateway Dashboard Tile:", install: false, uninstall: false) {
         section("") {
                  input("tileHeight", "number", title: "Tile Height (default 517 pixels)", defaultValue: 517, required: false )
                  input("tileWidth", "number", title: "Tile Width (default 460 pixels)", defaultValue: 460, required: false )
                  input("tileScale", "decimal", title: "Tile Scale (default 0.81)", defaultValue: 0.81, required: false )   
              }
    }
}
        
def getTeslaServerStatus() {
    try {
        def messageStr
        if (!userEmail || !userPw) {
            messageStr = "Enter your Tesla Account credentials"
        } else {
           getPowerwalls() 
           if (!state.accessTokenValid) {
               messageStr = "Error Verifying Tesla/Powerwall Account\n" +
                   "Please verify your Tesla Account username and password."
           } else if (state.foundPowerwalls) {
            messageStr = "Tesla Account Verified:\n" +
                        "Connection to Tesla server established.\n" +
                        "Site Name: ${state.siteName}\n" +
                        "Id: ${state.pwId}\n" +
                        "Site Id: ${state.energySiteId}"
           } else {
              messageStr = "Error: No Powerwalls found\n" +
                   "Please verify your Tesla Account username and password."
           }
        }
        return messageStr
    } catch (Exception e) {
        log.error e
        return "Error accessing Powerwall account\n" + 
            "Please verify your Tesla account username and password." 
    }
}

def getLocalGwStatus() {
    try {
        def messageStr
        if (gatewayAddress == null) {
                messageStr = "Enter your local gateway IP address" 
        } else {
                log.debug "Attempting to connect to local gateway"
                messageStr = "Local Gateway Status:\nCould not connect to local gateway at ${gatewayAddress}" 
                def requestParameters = [
                   uri: "https://${gatewayAddress}",
                   path: "/api/site_info/site_name",
                   contentType: 'application/json',
                   ignoreSSLIssues: true 
                ]
                httpGet(requestParameters) {
                  resp -> 
                   log.debug "response data was ${resp.data} "       
                   messageStr = "Local Gateway Verified:\n" +
                       "Connected at ${gatewayAddress}\n"+
                       "Site Name: ${resp.data.site_name.toString()}\n" +
                       "Gateway time zone: ${resp.data.timezone.toString()}\n"
                   state.foundGateway = true
                }
                //log.debug "${messageStr}"
        }
       return messageStr

    } catch (Exception e) {
        log.error e
        return "Error accessing local gateway:\n${e}"
    }
}

def pageNotifications() {
    dynamicPage(name: "pageNotifications", title: "Notification Preferences", install: false, uninstall: false) {
        section("Powerwall Notification Triggers:") {
            input "notifyWhenVersionChanges", "bool", required: false, defaultValue: false, title: "Notify when Powerwall software version changes"
            input "notifyWhenModesChange", "bool", required: false, defaultValue: false, title:
                "Notify when Powerwall configuration (modes/schedules) change"
            input "notifyWhenGridStatusChanges", "bool", required: false, defaultValue: false, title: "Notify of grid status changes/power failures"
            input "notifyWhenReserveApproached", "bool", required: false, defaultValue: false, title:
                "Notify when Powerwall charge level % drops to reserve percentage"
            input "notifyOfSchedules", "bool", required: false, defaultValue: true, title:
                "Notify when schedules or battery charge level % actions are being executed by the Powerwall Manager"
            input "notifyWhenAnomalies", "bool", required: false, defaultValue: true, title:
                "Notify when anomalies are encountered in the Powerwall Manager SmartApp"
        }
        section("Notification method (push notifications are via mobile app)") {
            if (hubIsSt()) {
                input "notificationMethod", "enum", required: false, defaultValue: "push", title: "Notification Method", options: ["none", "text", "push", "text and push"]
                input "phoneNumber", "phone", title: "Phone number for text messages", description: "Phone Number for text/SMS messages", required: false
            } else {
                //Hubitat
                input(name: "notifyDevices", type: "capability.notification", title: "Send to these notification devices", required: false, multiple: true, submitOnChange: true)
            }
        }
    }
}

def schedule1Options() {
    dynamicPage(name: "schedule1Options", title: "Schedule 1", install: false, uninstall: false) {
        section("") {
            input "schedule1Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only", "Self-Powered",
                "Time-Based Control"
            ]
            input "schedule1Reserve", "enum", required: false, title: "Reserve % to set (Self-Powered & TBC modes)",
                options: ["No Action": "No Action", "0": "0%", "5": "5%", "10": "10%", "15": "15%", "20": "20%", "25": "25%", "30": "30%", "35":
                    "35%", "40": "40%", "45": "45%", "50": "50%",
                    "55": "55%", "60": "60%", "65": "65%", "70": "70%", "75": "75%", "80": "80%", "85": "85%", "90": "90%", "95": "95%", "100":
                    "100%"
                ]
            input "schedule1Strategy", "enum", required: false, title: "Time-Based Control Strategy to set", options: ["No Action", "Cost Saving",
                "Balanced"
            ]
            input "schedule1Stormwatch", "enum", required: false, title: "Stormwatch enable/disable", options: ["No Action", "Enable Stormwatch",
                "Disable Stormwatch"
            ]
            input "schedule1Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            input "schedule1Time", "time", required: false, title: "At what time?"
        }
    }
}

def schedule2Options() {
    dynamicPage(name: "schedule2Options", title: "Schedule 2", install: false, uninstall: false) {
        section("Reserve % setting only applies when in Self-Powered and Time-Based Control modes") {
            input "schedule2Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only", "Self-Powered",
                "Time-Based Control"
            ]
            input "schedule2Reserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action": "No Action", "0": "0%", "5": "5%", "10": "10%", "15": "15%", "20": "20%", "25": "25%", "30": "30%", "35":
                    "35%", "40": "40%", "45": "45%", "50": "50%",
                    "55": "55%", "60": "60%", "65": "65%", "70": "70%", "75": "75%", "80": "80%", "85": "85%", "90": "90%", "95": "95%", "100":
                    "100%"
                ]
            input "schedule2Stormwatch", "enum", required: false, title: "Stormwatch enable/disable", options: ["No Action", "Enable Stormwatch",
                "Disable Stormwatch"
            ]
            input "schedule2Strategy", "enum", required: false, title: "Time-Based Control Strategy", options: ["No Action", "Cost Saving",
                "Balanced"
            ]
            input "schedule2Time", "time", required: false, title: "At what time?"
            input "schedule2Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        }
    }
}

def schedule3Options() {
    dynamicPage(name: "schedule3Options", title: "Schedule 3", install: false, uninstall: false) {
        section("Reserve % setting only applies when in Self-Powered and Time-Based Control modes") {
            input "schedule3Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only", "Self-Powered",
                "Time-Based Control"
            ]
            input "schedule3Reserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action": "No Action", "0": "0%", "5": "5%", "10": "10%", "15": "15%", "20": "20%", "25": "25%", "30": "30%", "35":
                    "35%", "40": "40%", "45": "45%", "50": "50%",
                    "55": "55%", "60": "60%", "65": "65%", "70": "70%", "75": "75%", "80": "80%", "85": "85%", "90": "90%", "95": "95%", "100":
                    "100%"
                ]
            input "schedule3Stormwatch", "enum", required: false, title: "Stormwatch enable/disable", options: ["No Action", "Enable Stormwatch",
                "Disable Stormwatch"
            ]
            input "schedule3Strategy", "enum", required: false, title: "Time-Based Control Strategy", options: ["No Action", "Cost Saving",
                "Balanced"
            ]
            input "schedule3Time", "time", required: false, title: "At what time?"
            input "schedule3Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        }
    }
}

def schedule4Options() {
    dynamicPage(name: "schedule4Options", title: "Schedule 4", install: false, uninstall: false) {
        section("Reserve % setting only applies when in Self-Powered and Time-Based Control modes") {
            input "schedule4Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only", "Self-Powered",
                "Time-Based Control"
            ]
            input "schedule4Reserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action": "No Action", "0": "0%", "5": "5%", "10": "10%", "15": "15%", "20": "20%", "25": "25%", "30": "30%", "35":
                    "35%", "40": "40%", "45": "45%", "50": "50%",
                    "55": "55%", "60": "60%", "65": "65%", "70": "70%", "75": "75%", "80": "80%", "85": "85%", "90": "90%", "95": "95%", "100":
                    "100%"
                ]
            input "schedule4Stormwatch", "enum", required: false, title: "Stormwatch enable/disable", options: ["No Action", "Enable Stormwatch",
                "Disable Stormwatch"
            ]
            input "schedule4Time", "time", required: false, title: "At what time?"
            input "schedule4Strategy", "enum", required: false, title: "Time-Based Control Strategy", options: ["No Action", "Cost Saving",
                "Balanced"
            ]
            input "schedule4Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        }
    }
}

def schedule5Options() {
    dynamicPage(name: "schedule5Options", title: "Schedule 5", install: false, uninstall: false) {
        section("Reserve % setting only applies when in Self-Powered and Time-Based Control modes") {
            input "schedule5Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only", "Self-Powered",
                "Time-Based Control"
            ]
            input "schedule5Reserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action": "No Action", "0": "0%", "5": "5%", "10": "10%", "15": "15%", "20": "20%", "25": "25%", "30": "30%", "35":
                    "35%", "40": "40%", "45": "45%", "50": "50%",
                    "55": "55%", "60": "60%", "65": "65%", "70": "70%", "75": "75%", "80": "80%", "85": "85%", "90": "90%", "95": "95%", "100":
                    "100%"
                ]
            input "schedule5Stormwatch", "enum", required: false, title: "Stormwatch enable/disable", options: ["No Action", "Enable Stormwatch",
                "Disable Stormwatch"
            ]
            input "schedule5Strategy", "enum", required: false, title: "Time-Based Control Strategy", options: ["No Action", "Cost Saving",
                "Balanced"
            ]
            input "schedule5Time", "time", required: false, title: "At what time?"
            input "schedule5Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        }
    }
}

def schedule6Options() {
    dynamicPage(name: "schedule6Options", title: "Schedule 6", install: false, uninstall: false) {
        section("Reserve % setting only applies when in Self-Powered and Time-Based Control modes") {
            input "schedule6Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only", "Self-Powered",
                "Time-Based Control"
            ]
            input "schedule6Reserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action": "No Action", "0": "0%", "5": "5%", "10": "10%", "15": "15%", "20": "20%", "25": "25%", "30": "30%", "35":
                    "35%", "40": "40%", "45": "45%", "50": "50%",
                    "55": "55%", "60": "60%", "65": "65%", "70": "70%", "75": "75%", "80": "80%", "85": "85%", "90": "90%", "95": "95%", "100":
                    "100%"
                ]
            input "schedule6Stormwatch", "enum", required: false, title: "Stormwatch enable/disable", options: ["No Action", "Enable Stormwatch",
                "Disable Stormwatch"
            ]
            input "schedule6Strategy", "enum", required: false, title: "Time-Based Control Strategy", options: ["No Action", "Cost Saving",
                "Balanced"
            ]
            input "schedule6Time", "time", required: false, title: "At what time?"
            input "schedule6Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        }
    }
}

def schedule7Options() {
    dynamicPage(name: "schedule7Options", title: "Schedule 7", install: false, uninstall: false) {
        section("Reserve % setting only applies when in Self-Powered and Time-Based Control modes") {
            input "schedule7Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only", "Self-Powered",
                "Time-Based Control"
            ]
            input "schedule7Reserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action": "No Action", "0": "0%", "5": "5%", "10": "10%", "15": "15%", "20": "20%", "25": "25%", "30": "30%", "35":
                    "35%", "40": "40%", "45": "45%", "50": "50%",
                    "55": "55%", "60": "60%", "65": "65%", "70": "70%", "75": "75%", "80": "80%", "85": "85%", "90": "90%", "95": "95%", "100":
                    "100%"
                ]
            input "schedule7Stormwatch", "enum", required: false, title: "Stormwatch enable/disable", options: ["No Action", "Enable Stormwatch",
                "Disable Stormwatch"
            ]
            input "schedule7Strategy", "enum", required: false, title: "Time-Based Control Strategy", options: ["No Action", "Cost Saving",
                "Balanced"
            ]
            input "schedule7Time", "time", required: false, title: "At what time?"
            input "schedule7Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        }
    }
}

def pagePwPreferences() {
    dynamicPage(name: "pagePwPreferences", title: "Powerwall Manager Preferences", install: false, uninstall: false) {
        section("") {
            input "pollingPeriod", "enum", required: false, title: "Powerwall polling interval", defaultValue: "10 minutes",
                options: ["Do not poll", "5 minutes", "10 minutes", "30 minutes", "1 hour"]
              //if ((connectionMethod && connectionMethod != "Use Remote Tesla Account Server Only")) {
              //   input "gatewayPollingPeriod", "enum", required: false, title: "Local Gateway Powerwall polling interval", defaultValue: "10 minutes",
              //      options: ["Do not poll", "1 minute", "5 minutes", "10 minutes", "1 hour"]
              //} 
        }
        section("") {
            input "logLevel", "enum", required: false, title: "IDE Log Level (sets log level in web IDE live logging tab)", options: ["none",
                "trace", "debug", "info", "warn"
            ]
        }
        section("") {
            input "pauseAutomations", "bool", required: false, defaultValue: false, title: "Pause all schedules and automated actions"
        }
    }
}

def pageDevicesToControl() {
    dynamicPage(name: "pageDevicesToControl", title: "Turn off devices when a grid outage is detected", install: false, uninstall: false) {
        section("") {
            input "devicesToOffDuringOutage", "capability.switch", title: "Devices that should be turned off during a grid outage", required: false,
                multiple: true
            input "turnDevicesBackOnAfterOutage", "bool", required: false, defaultValue: false,
                title: "Turn the above selected devices back on after grid outage is over?"
        }
    }
}

def appendOnNewLine(message, textToAdd) {
    def newMessage = ''
    if (message != '') {
        newMessage = message + "\n"
    }
    return newMessage + textToAdd
}

def pageTriggers() {
    dynamicPage(name: "pageTriggers", title: "Powerwall battery charge % level above/below actions.", install: false, uninstall: false) {
        def timeSetting = "N/A"
        def enableTriggers = "N/A"
        def triggerDaysNA = "N/A"
        //state.timeOfLastBelowTrigger = null
        //state.timeOfLastAboveTrigger = null

        def message = ""
        if (aboveTriggerValue && aboveTriggerEnabled?.toBoolean()) {
            def optionsString = getOptionsString(aboveTriggerMode, aboveTriggerReserve, aboveTriggerStormwatch, aboveTriggerStrategy,
                aboveTriggerDevicesToOn, timeSetting, triggerDaysNA)
            message = "Execute these actions when Powerwall charge level rises above ${aboveTriggerValue?.toString()}%:\n" + optionsString
            if (!actionsValid(aboveTriggerMode, aboveTriggerReserve, aboveTriggerStormwatch, aboveTriggerStrategy, aboveTriggerDevicesToOn,
                    enableTriggers)) {
                message = message + "\nNotification will be sent if enabled in preferences."
            }
        } else {
            message = "No charge level % upper trigger enabled.."
        }
        section("Choose actions to execute when the Powerwall battery charge % rises above a pre-defined level:") {
            href "aboveTriggerOptions", title: "${message}", description: ""
        }
        if (belowTriggerValue && belowTriggerEnabled?.toBoolean()) {
            def optionsString = getOptionsString(belowTriggerMode, belowTriggerReserve, belowTriggerStormwatch, belowTriggerStrategy,
                belowTriggerDevicesToOff, timeSetting, triggerDaysNA)
            message = "Execute these actions when Powerwall charge level drops below ${belowTriggerValue?.toString()}%:\n" + optionsString
            if (!actionsValid(belowTriggerMode, belowTriggerReserve, belowTriggerStormwatch, belowTriggerStrategy, belowTriggerDevicesToOff,
                    enableTriggers)) {
                message = message + "\nNotification will be sent if enabled in preferences."
            }
        } else {
            message = "No charge level % lower trigger enabled.."
        }
        section("Choose actions to execute when the Powerwall battery charge % drops below a pre-defined level:") {
            href "belowTriggerOptions", title: "${message}", description: ""
        }
        def restrictMessage = ''
        if (triggerRestrictPeriod1?.toBoolean() && triggerStartTime1 && triggerStopTime1) {
            restrictMessage = appendOnNewLine(restrictMessage, "Trigger Period 1: " + formatTimeString(triggerStartTime1) + " to " + formatTimeString(
                triggerStopTime1))
        }
        if (triggerRestrictPeriod2?.toBoolean() && triggerStartTime2 && triggerStopTime2) {
            restrictMessage = appendOnNewLine(restrictMessage, "Trigger Period 2: " + formatTimeString(triggerStartTime2) + " to " + formatTimeString(
                triggerStopTime2))
        }
        if (triggerRestrictDays?.toBoolean() && triggerDays?.size() > 0) {
            restrictMessage = appendOnNewLine(restrictMessage, triggerDays.toString())
        }
        if (restrictMessage == '') {
            restrictMessage = "No optional schedule restrictions defined.."
        }
        section("Restrict these triggers to specific times/days (optional):") {
            href "triggerRestrictions", title: "${restrictMessage}", description: ""
        }
    }
}

def triggerRestrictions() {
    dynamicPage(name: "triggerRestrictions", title: "Battery Charge % Level Trigger period restrictions", install: false, uninstall: false) {
        section("") {
            input "triggerRestrictDays", "bool", required: false, defaultValue: false, title: "Restrict % battery trigger actions to only occur on specified days"
            input "triggerDays", "enum", required: false, title: "Only on these days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

        }
        section("Restrict % battery trigger actions to only occur during specified time periods:") {
            input "triggerRestrictPeriod1", "bool", required: false, defaultValue: false, title: "Enable Time Period 1"
            input "triggerStartTime1", "time", required: false, title: "Time Period 1 Start Time"
            input "triggerStopTime1", "time", required: false, title: "Time Period 1 End Time"
            input "triggerRestrictPeriod2", "bool", required: false, defaultValue: false, title: "Enable Time Period 2"
            input "triggerStartTime2", "time", required: false, title: "Time Period 2 Start Time"
            input "triggerStopTime2", "time", required: false, title: "Time Period 2 Stop Time"
        }
    }
}

def aboveTriggerOptions() {
    dynamicPage(name: "aboveTriggerOptions", title: "Above Battery Charge % Level Trigger Options", install: false, uninstall: false) {
        section("") {
            input "aboveTriggerEnabled", "bool", required: false, defaultValue: false, title: "Enable these actions"
            input "aboveTriggerValue", "number", required: false, title: "Actions will trigger when charge level % rises above this value"
            input "aboveTriggerMode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only", "Self-Powered",
                "Time-Based Control"
            ]
            input "aboveTriggerReserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action": "No Action", "0": "0%", "5": "5%", "10": "10%", "15": "15%", "20": "20%", "25": "25%", "30": "30%", "35":
                    "35%", "40": "40%", "45": "45%", "50": "50%",
                    "55": "55%", "60": "60%", "65": "65%", "70": "70%", "75": "75%", "80": "80%", "85": "85%", "90": "90%", "95": "95%", "100":
                    "100%"
                ]
            input "aboveTriggerStormwatch", "enum", required: false, title: "Stormwatch enable/disable", options: ["No Action", "Enable Stormwatch",
                "Disable Stormwatch"
            ]
            input "aboveTriggerStrategy", "enum", required: false, title: "Time-Based Control Strategy", options: ["No Action", "Cost Saving",
                "Balanced"
            ]
            input "aboveTriggerDevicesToOn", "capability.switch", title:
                "Devices that should be turned on when charge level % rises above defined trigger", required: false, multiple: true
        }
    }
}

def belowTriggerOptions() {
    dynamicPage(name: "belowTriggerOptions", title: "Below Battery Charge % Level Trigger Options", install: false, uninstall: false) {
        section("") {
            input "belowTriggerEnabled", "bool", required: false, defaultValue: false, title: "Enable these actions"
            input "belowTriggerValue", "number", required: false, title: "Actions will trigger when charge level % drops below this value"
            input "belowTriggerMode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only", "Self-Powered",
                "Time-Based Control"
            ]
            input "belowTriggerReserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action": "No Action", "0": "0%", "5": "5%", "10": "10%", "15": "15%", "20": "20%", "25": "25%", "30": "30%", "35":
                    "35%", "40": "40%", "45": "45%", "50": "50%",
                    "55": "55%", "60": "60%", "65": "65%", "70": "70%", "75": "75%", "80": "80%", "85": "85%", "90": "90%", "95": "95%", "100":
                    "100%"
                ]
            input "belowTriggerStormwatch", "enum", required: false, title: "Stormwatch enable/disable", options: ["No Action", "Enable Stormwatch",
                "Disable Stormwatch"
            ]
            input "belowTriggerStrategy", "enum", required: false, title: "Time-Based Control Strategy", options: ["No Action", "Cost Saving",
                "Balanced"
            ]
            input "belowTriggerDevicesToOff", "capability.switch", title:
                "Devices that should be turned off when charge level % drops below defined trigger", required: false, multiple: true
        }
    }
}

Boolean hubIsSt() {
    return (getHubType() == "SmartThings")
}

def getPwDevice() {
   def deviceIdStr = null
   if (state.childDeviceId) {
      deviceIdStr = state.childDeviceId
   } else {
      def devices = getChildDevices()
      if (devices.size() > 0) {
          deviceIdStr = getChildDevices().first().getDeviceNetworkId()
          state.childDeviceId = deviceIdStr
      }
   } 
   return getChildDevice(deviceIdStr)
}

private getHubType() {
    def hubType = "SmartThings"
    if (state.hubType == null) {
        try {
            include 'asynchttp_v1'
        } catch (e) {
            hubType = "Hubitat"
        }
        state.hubType = hubType
    }
    return state.hubType
}

def actionsValid(modeSetting, reserveSetting, stormwatchSetting, strategySetting, devicesToControl, enableTriggers, peakSwitch = null) {
    return ((modeSetting && modeSetting.toString() != "No Action") ||
        (reserveSetting && reserveSetting.toString() != "No Action") ||
        (stormwatchSetting && stormwatchSetting.toString() != "No Action") ||
        (strategySetting && strategySetting.toString() != "No Action") ||
        (devicesToControl && devicesToControl.toString() != "N/A" && devicesToControl.size() > 0) ||
        (enableTriggers && enableTriggers.toString() != "No Action") ||
        (peakSwitch && peakSwitch.toString() != "No Action"))
}

def scheduleValid(timeSetting, daysSetting) {
    return timeSetting != null && daysSetting != null && (daysSetting.size() > 0 || daysSetting.toString() == "N/A")
}

def formatTimeString(timeSetting) {
    def timeFormat = new java.text.SimpleDateFormat("hh:mm a")
    def isoDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    def isoTime = new java.text.SimpleDateFormat(isoDatePattern).parse(timeSetting.toString())
    return timeFormat.format(isoTime).toString()
}

def getOptionsString(modeSetting, reserveSetting, stormwatchSetting, strategySetting, controlDevices, timeSetting, daysSetting) {
    def optionsString = ''
    if (actionsValid(modeSetting, reserveSetting, stormwatchSetting, strategySetting, controlDevices, enableTriggers)) {
        if (scheduleValid(timeSetting, daysSetting)) {
            if (modeSetting && modeSetting.toString() != "No Action") {
                optionsString = "Mode: " + modeSetting.toString()
            }
            if (reserveSetting && reserveSetting.toString() != "No Action") {
                optionsString = appendOnNewLine(optionsString, "Reserve: " + reserveSetting.toString() + '%')
            }
            if (stormwatchSetting && stormwatchSetting.toString() != "No Action") {
                if (stormwatchSetting.toString() == "Enable Stormwatch") {
                    optionsString = appendOnNewLine(optionsString, "Stormwatch: Enable")
                } else if (stormwatchSetting.toString() == "Disable Stormwatch") {
                    optionsString = appendOnNewLine(optionsString, "Stormwatch: Disable")
                }
            }
            if (strategySetting && strategySetting.toString() != "No Action") {
                optionsString = appendOnNewLine(optionsString, "Time-Based Control Strategy: " + strategySetting.toString())
            }
            if (controlDevices && controlDevices.size() > 0) {
                optionsString = appendOnNewLine(optionsString, "Control Devices: ${controlDevices}")               
            }
            if (enableTriggers && enableTriggers.toString() != "No Action") {
                if (optionsString != '') {
                    optionsString = optionsString + ',\n'
                }
                optionsString = optionsString + "Battery Charge % actions: " + enableTriggers.toString()
            }
            def timeString = ''
            if (timeSetting != "N/A") {
                timeString = formatTimeString(timeSetting) + ' '
            }
            def dayString = ''
            if (daysSetting != "N/A") {
                dayString = daysSetting.toString()
            }
            if (timeString != '' || dayString != '') {
                optionsString = optionsString + '\n' + timeString + dayString
            }
        } else {
            optionsString = "No time or days scheduled"
        }
    } else {
        optionsString = "No actions scheduled"
    }
    return optionsString
}

def pageSchedules() {
    dynamicPage(name: "pageSchedules", title:
        "Powerwall setting changes are subject to Powerwall processing rules and may not immediately take effect at the time they are commanded.",
        install: false, uninstall: false) {
        def optionsString
        def devicesToControl = null
        optionsString = getOptionsString(schedule1Mode, schedule1Reserve, schedule1Stormwatch, schedule1Strategy, devicesToControl, schedule1Time, schedule1Days)
        section("Schedule 1") {
            href "schedule1Options", title: "${optionsString}", description: ""
        }
        optionsString = getOptionsString(schedule2Mode, schedule2Reserve, schedule2Stormwatch, schedule2Strategy, devicesToControl, schedule2Time, schedule2Days)
        section("Schedule 2") {
            href "schedule2Options", title: "${optionsString}", description: ""
        }
        optionsString = getOptionsString(schedule3Mode, schedule3Reserve, schedule3Stormwatch, schedule3Strategy, devicesToControl, schedule3Time, schedule3Days)
        section("Schedule 3") {
            href "schedule3Options", title: "${optionsString}", description: ""
        }
        optionsString = getOptionsString(schedule4Mode, schedule4Reserve, schedule4Stormwatch, schedule4Strategy, devicesToControl, schedule4Time, schedule4Days)
        section("Schedule 4") {
            href "schedule4Options", title: "${optionsString}", description: ""
        }
        optionsString = getOptionsString(schedule5Mode, schedule5Reserve, schedule5Stormwatch, schedule5Strategy, devicesToControl, schedule5Time, schedule5Days)
        section("Schedule 5") {
            href "schedule5Options", title: "${optionsString}", description: ""
        }
        optionsString = getOptionsString(schedule6Mode, schedule6Reserve, schedule6Stormwatch, schedule6Strategy, devicesToControl, schedule6Time, schedule6Days)
        section("Schedule 6") {
            href "schedule6Options", title: "${optionsString}", description: ""
        }
        optionsString = getOptionsString(schedule7Mode, schedule7Reserve, schedule7Stormwatch, schedule7Strategy, devicesToControl, schedule7Time, schedule7Days)
        section("Schedule 7") {
            href "schedule7Options", title: "${optionsString}", description: ""
        }
    }
}

def instantiatSchedule(mode, reserve, stormwatch, strategy, peakSwitch, time, days, callback) {
    def devices = "N/A"
    if (actionsValid(mode, reserve, stormwatch, strategy, devices, enableTriggers, peakSwitch)) {
        if (scheduleValid(time, days)) {
            log.debug "scheduling ${callback.toString()}"
            schedule(time.toString(), callback)
        } else {
            def message = "${callback.toString()} actions are enabled in preferences, but schedule time and/or days were not specified. Schedule could not be set."
            sendNotificationMessage(message, "anomaly")
        }
    }
}

def setSchedules() {
    instantiatSchedule(schedule1Mode, schedule1Reserve, schedule1Stormwatch, schedule1Strategy, schedule1PeakSwitch, schedule1Time, schedule1Days, processSchedule1)
    instantiatSchedule(schedule2Mode, schedule2Reserve, schedule2Stormwatch, schedule2Strategy, schedule2PeakSwitch, schedule2Time, schedule2Days, processSchedule2)
    instantiatSchedule(schedule3Mode, schedule3Reserve, schedule3Stormwatch, schedule3Strategy, schedule3PeakSwitch, schedule3Time, schedule3Days, processSchedule3)
    instantiatSchedule(schedule4Mode, schedule4Reserve, schedule4Stormwatch, schedule4Strategy, schedule4PeakSwitch, schedule4Time, schedule4Days, processSchedule4)
    instantiatSchedule(schedule5Mode, schedule5Reserve, schedule5Stormwatch, schedule5Strategy, schedule5PeakSwitch, schedule5Time, schedule5Days, processSchedule5)
    instantiatSchedule(schedule6Mode, schedule6Reserve, schedule6Stormwatch, schedule6Strategy, schedule6PeakSwitch, schedule6Time, schedule6Days, processSchedule6)
    instantiatSchedule(schedule7Mode, schedule7Reserve, schedule7Stormwatch, schedule7Strategy, schedule7PeakSwitch, schedule7Time, schedule7Days, processSchedule7)
}

def getTheDay() {
    def df = new java.text.SimpleDateFormat("EEEE")
    // Ensure the new date object is set to local time zone
    if (location.timeZone != null) {
        df.setTimeZone(location.timeZone)
    } else {
        log.info "no time zone found for schedule processing"
    }
    def day = df.format(new Date())
    //log.debug "Today is: ${day}"
    return day
}

def commandPwFromSchedule(mode, reserve, stormwatch, strategy, scheduledDays) {
    def day = getTheDay()
    if (!pauseAutomations && scheduledDays?.contains(day)) {
        def message = commandPwActions(mode, reserve, stormwatch, strategy, chargeTriggers)
        if (notifyOfSchedules?.toBoolean()) {
            sendNotificationMessage("Performing scheduled Powerwall actions. " + message)
        }
    }
}

//Hubitat compatibility
private timeOfDayIsBetween(fromDate, toDate, checkDate, timeZone) {
    return (!checkDate.before(toDateTime(fromDate)) && !checkDate.after(toDateTime(toDate)))
}

def triggerPeriodActive() {
    def day = getTheDay()
    def daysAreSet = triggerRestrictDays?.toBoolean() && triggerDays?.size() > 0
    def dayIsActive = daysAreSet && triggerDays?.contains(day)
    def aPeriodIsSet = (triggerRestrictPeriod1?.toBoolean() || triggerRestrictPeriod2?.toBoolean())
    def aPeriodIsActive = (triggerRestrictPeriod1?.toBoolean() && timeOfDayIsBetween(triggerStartTime1, triggerStopTime1, new Date(), location
        .timeZone)) ||
        (triggerRestrictPeriod2?.toBoolean() && timeOfDayIsBetween(triggerStartTime2, triggerStopTime2, new Date(), location.timeZone))
    //Valid conditions:
    // 1) day matches & period active, 2) day matches & no periods declared, 3) no day is set & period active, 4) no day is set & no periods declared 
    return ((dayIsActive && (aPeriodIsActive || !aPeriodIsSet)) || (!daysAreSet && (aPeriodIsActive || !aPeriodIsSet)))
}

def commandPwActions(mode, reserve, stormwatch, strategy, enableChargeTriggers) {
    def pwDevice = getPwDevice()
    def message = ""
    if (mode && mode.toString() != "No Action") {
        message = message + " Mode: ${mode.toString()}."
        if (mode.toString() == "Backup-Only") {
            setBackupOnlyMode(pwDevice)
        } else if (mode.toString() == "Self-Powered") {
            setSelfPoweredMode(pwDevice)
        } else if (mode.toString() == "Time-Based Control") {
            setTimeBasedControlMode(pwDevice)
        } else {
            def errMessage = "Unexpected condition processing scheduled mode change: ${mode.toString()}"
            sendNotificationMessage(errMessage, "anomaly")
        }
    }
    if (reserve && reserve.toString() != "No Action") {
        message = message + " Reserve: ${reserve}%."
        if (reserve.toInteger() >= 0 && reserve.toInteger() <= 100) {
            runIn(10, commandBackupReservePercent, [data: [reservePercent: reserve.toInteger()]])
        } else {
            def errMessage = "Unexpected condition processing scheduled reserve % change: ${reserve}}"
            sendNotificationMessage(errMessage, "anomaly")
        }
    }
    if (stormwatch && stormwatch.toString() != "No Action") {
        if (stormwatch.toString() == "Enable Stormwatch") {
            runIn(15, commandStormwatchEnable)
            message = message + " Stormwatch: Enabled."
        } else if (stormwatch.toString() == "Disable Stormwatch") {
            message = message + " Stormwatch: Disabled."
            runIn(15, commandStormwatchDisable)
        }
    }
    if (strategy && strategy.toString() != "No Action") {
        message = message + " TBC Strategy: ${strategy.toString()}."
        if (strategy.toString() == "Cost Saving") {
            runIn(20, commandTouStrategy, [data: [strategy: "economics"]])
        } else if (strategy.toString() == "Balanced") {
            runIn(20, commandTouStrategy, [data: [strategy: "balanced"]])
        } else {
            def errMessage = "Unexpected condition processing scheduled strategy change: ${strategy.toString()}"
            sendNotificationMessage(errMessage, "anomaly")
        }
    }
    if (enableChargeTriggers && enableChargeTriggers.toString() != "No Action") {
        if (enableChargeTriggers.toString() == "Turn On Peak") {
            message = message + " Virtual Peak Switch: On."
        } else if (stormwatch.toString() == "Disable Triggers") {
            message = message + " Virtual Peak Switch: Off."
        }
    }
    return message
}

def processSchedule1() {
    commandPwFromSchedule(schedule1Mode, schedule1Reserve, schedule1Stormwatch, schedule1Strategy, schedule1Days)
}

def processSchedule2() {
    commandPwFromSchedule(schedule2Mode, schedule2Reserve, schedule2Stormwatch, schedule2Strategy, schedule2Days)
}
def processSchedule3() {
    commandPwFromSchedule(schedule3Mode, schedule3Reserve, schedule3Stormwatch, schedule3Strategy, schedule3Days)
}
def processSchedule4() {
    commandPwFromSchedule(schedule4Mode, schedule4Reserve, schedule4Stormwatch, schedule4Strategy, schedule4Days)
}

def processSchedule5() {
    commandPwFromSchedule(schedule5Mode, schedule5Reserve, schedule5Stormwatch, schedule5Strategy, schedule5Days)
}

def processSchedule6() {
    commandPwFromSchedule(schedule6Mode, schedule6Reserve, schedule6Stormwatch, schedule6Strategy, schedule6Days)
}

def processSchedule7() {
    commandPwFromSchedule(schedule7Mode, schedule7Reserve, schedule7Stormwatch, schedule7Strategy, schedule7Days)
}

private getUrl() {
    "https://owner-api.teslamotors.com"
}
private getId() {
    "81527cff06843c8634fdc09e8ac0abefb46ac849f38fe1e431c2ef2106796384"
}
private getSecret() {
    "c7257eb71a564034f9419ee651c7d0e5f7aa6bfbd18bafb5c5c033b093bb2fa3"
}
private getAgent() {
    "darwinsden"
}

def getToken() {
    if (!state.access_token) {
        refreshToken()
    }
    state.access_token
}

def refreshToken() {
    def tokenExpirationSeconds
    try {
        if (state.refresh_token) {
            try {
                httpPostJson([
                    uri: url,
                    path: "/oauth/token",
                    headers: ['User-Agent': agent],
                    body: [
                        grant_type: "refresh_token",
                        client_id: id,
                        client_secret: secret,
                        refresh_token: state.refresh_token
                    ]
                ]) {
                    resp ->
                        log.debug "Refresh token data: created at ${resp.data.created_at} and expires in ${resp.data.expires_in}"
                    state.access_token = resp.data.access_token
                    state.refresh_token = resp.data.refresh_token
                    def Long tokenExpiresOn = resp.data.created_at + resp.data.expires_in
                    state.token_expires_on = tokenExpiresOn
                    state.schedule_refresh_token = true
                }
            } catch (groovyx.net.http.HttpResponseException e) {
                log.warn e
                state.access_token = null
                if (e.response?.data?.status?.code == 14) {
                    state.refresh_token = null
                }
            }
        }

        if (!state.access_token) {
            httpPostJson([
                uri: url,
                path: "/oauth/token",
                headers: ['User-Agent': agent],
                body: [
                    grant_type: "password",
                    client_id: id,
                    client_secret: secret,
                    email: userEmail,
                    password: userPw
                ]
            ]) {
                resp ->
                    log.debug "Access token data: created at ${resp.data.created_at} and expires in ${resp.data.expires_in}"
                state.accessTokenValid = true
                state.access_token = resp.data.access_token
                state.refresh_token = resp.data.refresh_token
                def Long tokenExpiresOn = resp.data.created_at + resp.data.expires_in
                state.token_expires_on = tokenExpiresOn
                state.schedule_refresh_token = true
            }
        }
    } catch (Exception e) {
        state.accessTokenValid = false
        log.error "Unhandled exception getting token: $e"
    }
}

private resetAccountAccess() {
    log.debug "resetting account tokens"
    state.refresh_token = null
    state.access_token = null
    state.accessTokenValid = false
}

private httpAuthAsyncGet(handlerMethod, String path, Integer attempt = 1) {
    try {
        if (logLevel == "debug" | logLevel == "trace") {
           log.debug "Async requesting: ${path}"
        }
        def requestParameters = [
            uri: url,
            path: path,
            headers: ['User-Agent': agent, Authorization: "Bearer ${token}"]
        ]
        if (hubIsSt()) {
            include 'asynchttp_v1'
            asynchttp_v1.get(handlerMethod, requestParameters, [attempt: attempt])
        } else {
            asynchttpGet(handlerMethod, requestParameters, [attempt: attempt])
        }
    } catch (e) {
        log.error "Http Async Get failed: ${e}"
    }
}

private httpAuthGet(String path, Closure closure) {
    log.debug "requesting: ${path}"
    try {
        def requestParameters = [
            uri: url,
            path: path,
            headers: [
                'User-Agent': agent,
                Authorization: "Bearer ${token}"
            ]
        ]
        httpGet(requestParameters) {
            resp -> closure(resp)
        }
    } catch (e) {
        log.error "Http Get failed: ${e}"
    }
}

/*
private httpAuthGetL(String path, Closure closure) {
    log.debug "requesting: ${path}"
    try {
        def requestParameters = [
            uri: "https://${gatewayAddress}",
            path: path,
            headers: [
                'User-Agent': agent,
                Authorization: "Bearer ${token}"
            ]
        ]
        httpGet(requestParameters) {
            resp -> closure(resp)
        }
    } catch (e) {
        log.error "Http Get failed: ${e}"
    }
}
*/


private httpAsyncGet(address, handlerMethod, String path, Integer attempt = 1) {
    try {
        if (logLevel == "debug" | logLevel == "trace") {
           log.debug "Async requesting: ${path}"
        }
        //log.debug "https://${gatewayAddress}"
        def requestParameters = [
            uri: "https://${address}",
            path: path,
            contentType: 'application/json',
            ignoreSSLIssues: true 
            //,
            //headers: ['User-Agent': agent, Authorization: "Bearer ${token}"]
        ]
        if (hubIsSt()) {
            include 'asynchttp_v1'
            asynchttp_v1.get(handlerMethod, requestParameters, [attempt: attempt])
        } else {
            asynchttpGet(handlerMethod, requestParameters, [attempt: attempt])
        }
    } catch (e) {
        log.error "Http Async Get failed: ${e}"
    }
}

private httpAuthPost(Map params = [: ], String cmdName, String path, Closure closure, Integer attempt = null) {
    //cmdName is descriptive name for logging/notification
    def tryCount = attempt ?: 1
    def attemptStr = ""
    if (tryCount > 1) {
        attemptStr = ", Attempt: ${tryCount}"
    }
    log.debug "Command: ${cmdName} ${params?.body}" + attemptStr
    try {
        def requestParameters = [
            uri: url,
            path: path,
            headers: [
                'User-Agent': agent,
                Authorization: "Bearer ${token}"
            ]
        ]

        if (params.body) {
            requestParameters["body"] = params.body
            httpPostJson(requestParameters) {
                resp -> closure(resp)
            }
        } else {
            httpPost(requestParameters) {
                resp -> closure(resp)
            }
        }
        state.cmdFailedSent = false

    } catch (groovyx.net.http.HttpResponseException e) {
        log.warn "Request failed attempt ${tryCount} for path: ${path}. HTTP status code: ${e?.response?.getStatus()}"
        if (tryCount < 3) {
            log.debug "retrying Post"
            //debug notification message below. comment out for production
            //sendNotificationMessage("Failed HTTP command: ${params} after ${tryCount} tries. Retrying..")
            if (e.response.getStatus() == 401) {
                log.debug "Refreshing token"
                refreshToken()
            }
            pause(1000)
            httpAuthPost(params, cmdName, path, closure, tryCount + 1)
        } else {
            if (!state.cmdFailedSent) {
                sendNotificationMessage("Powerwall Manager: Failed HTTP command: ${cmdName} after ${tryCount} tries.")
                state.cmdFailedSent = true
            }
        }
    } catch (Exception e) {
        log.warn "Request failed attempt ${tryCount} for path: ${path}. General Exception: ${e}"
        if (tryCount < 3) {
            log.debug "retrying Post"
            //debug notification message below. comment out for production
            //sendNotificationMessage("Failed command: ${params} after ${tryCount} tries. Retrying..")
            pause(1000)
            httpAuthPost(params, cmdName, path, closure, tryCount + 1)
        } else {
            if (!state.cmdFailedSent) {
                sendNotificationMessage("Powerwall Manager: Failed command: ${cmdName} after ${tryCount} tries.")
                state.cmdFailedSent = true
            }
        }
    }
}

private sendNotificationMessage(message, msgType = null) {
    log.debug "notification message: ${message}"
    if (msgType == null || msgType != "anomaly" || notifyWhenAnomalies?.toBoolean()) {
        if (hubIsSt()) {
           def sendPushMessage = (!notificationMethod || (notificationMethod.toString() == "push" || notificationMethod.toString() == "text and push"))
           def sendTextMessage = (notificationMethod?.toString() == "text" || notificationMethod?.toString() == "text and push")
           if (sendTextMessage == true) {
               if (phoneNumber) {
                  sendSmsMessage(phoneNumber.toString(), message)
               }
           }
           if (sendPushMessage) {
               sendPush(message)
           }
        } else {
           // Hubitat
           if (notifyDevices != null) {
               notifyDevices.each {
                   it.deviceNotification(message)
               }
           }
      }
    }
}

private getPowerwalls() {
    state.foundPowerwalls = false
    def foundPowerwall = false
    httpAuthGet("/api/1/products", {
        resp ->
        //log.debug "response data for products was ${resp.data} "
        resp.data.response.each {
            product ->
                if (product.resource_type == "battery") {
                    //do not consider battery site if its site_name is null and a battery has previously been found (possibly a bad second site in the database)
                    if (product.site_name != null || !foundPowerwall) {
                        foundPowerwall = true
                        log.debug "battery found: ${product.id} site_name: ${product.site_name} energy_site_id: ${product.energy_site_id}"
                        state.energySiteId = product.energy_site_id
                        state.pwId = product.id
                        state.siteName = product.site_name
                    }
                }
        }
    })
    state.foundPowerwalls = foundPowerwall
}

def installed() {
    log.debug "Installed"
    initialize()
}

def updated() {
    log.debug "Updated"
    initialize()
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def initialize() {

    createDeviceForPowerwall()

    unsubscribe()
    unschedule()
    setSchedules()

    if (state.foundGateway) {
        //log.debug "calling tile update..."
        runIn (10, createDashboardTile)
    } else {
        log.debug "Gateway not connected"
    }
    
    if (pollingPeriod == "5 minutes") {
        runEvery5Minutes(processMain)
    } else if (pollingPeriod == "30 minutes") {
        runEvery30Minutes(processMain)
    } else if (pollingPeriod == "1 hour") {
        runEvery1Hour(processMain)
    } else if (pollingPeriod != "Do not poll") {
        runEvery10Minutes(processMain) //default
    } else {
        log.debug "not polling Powerwall"
    }

    runEvery1Hour(processWatchdog)
    runEvery3Hours(processWatchdog)
    runIn(5, processMain)

    if (state.refresh_token != null && state.token_expires_on != null) {
        state.schedule_refresh_token = true
    }
}

private createDeviceForPowerwall() {
    def pwDevice = getPwDevice()
    if (!pwDevice) {
        def device = addChildDevice("darwinsden", "Tesla Powerwall", "Powerwall" + now().toString(), null, 
                            [name: "Tesla Powerwall", label: "Tesla Powerwall",
            completedSetup: true
        ])
        log.debug "created powerwall device"
    } else {
        log.debug "device for Powerwall exists"
        pwDevice.initialize()
    }
}

def createDashboardTile() {
   def pwDevice = getPwDevice()
   log.debug "creating/updating tile..."
   if (pwDevice) {
      String tileStr = getTileStr(tileScale?.toFloat()) 
      pwDevice.sendEvent(name: "pwTile", value: tileStr)
   } else {
       log.warn "Unable to update Dashboard tile. Powerwall device does not exist."
   }
}

def updateIfChanged(device, attr, value, delta = null) {
    def currentValue = null
    if (state.currentAttrValue == null) {
        state.currentAttrValue = [: ]
    }
    if (state.currentAttrValue[attr] != null) {
        currentValue = state.currentAttrValue[attr].toString()
    }
    //log.debug "new value: ${value} old value: ${currentValue} attribute: ${attr} delta: ${delta} "
    def deltaMet = (currentValue == null || value != null && delta != null && Math.abs((value.toInteger() - currentValue.toInteger()).toInteger()) > delta
        .toInteger())
    def changed = value != null && value != '' && currentValue != null && currentValue != '' && value != currentValue && (!delta || deltaMet)
    state.currentAttrValue[attr] = value.toString()
    def heartBeatUpdateDue = false

    if (state.lastHeartbeatUpdateTime != null) {
        if (state.lastHeartbeatUpdateTime[attr] != null) {
            if ((now() - state.lastHeartbeatUpdateTime[attr]) > 60000) {
                heartBeatUpdateDue = true
            }
        }
    } else {
        state.lastHeartbeatUpdateTime = [: ]
    }
    if (device) {
        if (changed || heartBeatUpdateDue || (currentValue == null && (value != null && value != ''))) {
            device.sendEvent(name: attr, value: value)
            state.lastHeartbeatUpdateTime[attr] = now()
            //if (changed) {log.debug "changed ${attr} from ${currentValue} to ${value}"}
        }
    } else {
        log.debug("No Powerwall device to update ${attr} to ${value}")
    }
    return changed
}

def processAboveTriggerDeviceActions() {
    if (aboveTriggerDevicesToOn?.size()) {
        aboveTriggerDevicesToOn.on()
    }
}

def processBelowTriggerDeviceActions() {
    if (belowTriggerDevicesToOff?.size()) {
        belowTriggerDevicesToOff.off()
    }
}

def checkBatteryNotifications(data) {
    if (notifyWhenReserveApproached?.toBoolean() && data.reservePercent != null) {
        if (data.batteryPercent - data.reservePercent < 5) {
            def status
            if (data.batteryPercent <= data.reservePercent) {
                status = "is at or below"
            } else {
                status = "is approaching"
            }
            if (state.timeOfLastReserveNotification == null) {
                state.timeOfLastReserveNotification = now()
                sendNotificationMessage(
                    "Powerwall battery level of ${Math.round(data.batteryPercent*10)/10}% ${status} ${data.reservePercent}% reserve level.")
            }
        } else if (state.timeOfLastReserveNotification != null && now() - state.timeOfLastReserveNotification >= 30 * 60 * 1000) {
            //reset for new notification if alert condition no longer exists and it's been at least 30 minutes since last notification
            state.timeOfLastReserveNotification = null
        }
    }

    if (aboveTriggerValue) {
        if (data.batteryPercent >= aboveTriggerValue.toFloat()) {
            if (state.timeOfLastAboveTrigger == null) {
                if (!pauseAutomations && triggerPeriodActive() && aboveTriggerEnabled) {
                    state.timeOfLastAboveTrigger = now()
                    def enableTriggers = "N/A"
                    def triggerMessage = "Powerwall ${Math.round(data.batteryPercent*10)/10}% battery level is at or above ${aboveTriggerValue}% trigger."
                    if (actionsValid(aboveTriggerMode, aboveTriggerReserve, aboveTriggerStormwatch, aboveTriggerStrategy, aboveTriggerDevicesToOn,
                            enableTriggers)) {
                        def message = commandPwActions(aboveTriggerMode, aboveTriggerReserve, aboveTriggerStormwatch, aboveTriggerStrategy, enableTriggers)
                        if (aboveTriggerDevicesToOn?.size() > 0) {
                            message = message + " Turning on devices."
                            runIn(1, processAboveTriggerDeviceActions)
                        }
                        triggerMessage = triggerMessage + " Performing actions. " + message
                    }
                    if (notifyOfSchedules?.toBoolean()) {
                        sendNotificationMessage(triggerMessage)
                    }
                }
            }
        } else if (state.timeOfLastAboveTrigger != null && now() - state.timeOfLastAboveTrigger >= 30 * 60 * 1000) {
            //reset for new trigger if condition no longer exists and it's been at least 30 minutes since last trigger
            state.timeOfLastAboveTrigger = null
        }
    }

    if (belowTriggerValue) {
        if (data.batteryPercent <= belowTriggerValue.toFloat()) {
            if (state.timeOfLastBelowTrigger == null) {
                if (!pauseAutomations && triggerPeriodActive() && belowTriggerEnabled) {
                    state.timeOfLastBelowTrigger = now()
                    def enableTriggers = "N/A"
                    def triggerMessage = "Powerwall ${Math.round(data.batteryPercent*10)/10}% battery level is at or below ${belowTriggerValue}% trigger."
                    if (actionsValid(belowTriggerMode, belowTriggerReserve, belowTriggerStormwatch, belowTriggerStrategy, belowTriggerDevicesToOff,
                            enableTriggers)) {
                        def message = commandPwActions(belowTriggerMode, belowTriggerReserve, belowTriggerStormwatch, belowTriggerStrategy, enableTriggers)
                        if (belowTriggerDevicesToOff?.size() > 0) {
                            message = message + " Turning off devices."
                            runIn(1, processBelowTriggerDeviceActions)
                        }
                        triggerMessage = triggerMessage + " Performing actions. " + message
                    }
                    if (notifyOfSchedules?.toBoolean()) {
                        sendNotificationMessage(triggerMessage)
                    }
                }
            }
        } else if (state.timeOfLastBelowTrigger != null && now() - state.timeOfLastBelowTrigger >= 30 * 60 * 1000) {
            //reset for new trigger if condition no longer exists and it's been at least 30 minutes since last trigger
            state.timeOfLastBelowTrigger = null
        }
    }
}

def processSiteResponse(response, callData) {
    // log.debug "${callData}"
    if (logLevel == "debug" | logLevel == "trace") {
        log.debug "processing site data response"
    }
    if (!response.hasError()) {
        def data = response.json.response
        if (logLevel == "trace") {
          log.debug "${data}"   
        }
        def strategy = data.tou_settings.optimization_strategy
        def strategyUi
        if (strategy == "economics") {
            strategyUi = "Cost-Saving"
        } else if (strategy == "balanced") {
            strategyUi = "Balanced"
        } else {
            strategyUi = strategy
        }
        state.strategy = strategyUi.toString()
        def pwDevice = getPwDevice()
        def changed = updateIfChanged(pwDevice, "currentStrategy", strategyUi)
        if (changed && notifyWhenModesChange?.toBoolean()) {
            sendNotificationMessage("Powerwall ATC optimization strategy changed to ${strategyUi}")
        }
        if (notifyWhenModesChange?.toBoolean() && state?.lastSchedule && data.tou_settings.schedule != state.lastSchedule) {
            sendNotificationMessage("Powerwall Advanced Time Controls schedule has changed")
        }
        state.lastSchedule = data.tou_settings.schedule
        //log.debug "sched: ${data.tou_settings.schedule}"
    } else {
        if (response.getStatus() == 401) {
            log.warn "Site resp error: ${response.getErrorMessage()}. Refreshing token"
            runIn (1, refreshToken)
        }
        if (callData?.attempt && callData.attempt < 2) {
            log.warn "Site resp error: ${response.getErrorMessage()}. Retrying."
            runIn(20, requestSiteData, [data: [attempt: callData.attempt + 1]])
        } else {
            log.error "Site resp error: ${response.getErrorMessage()} on attempt ${callData?.attempt}."
        }
    }
}

def getTileStr(def zoomLevel) {
    long width = tileWidth?.toLong() ?: 460
    long height = tileHeight?.toLong() ?: 517  
    float frameScale = zoomLevel?.toFloat() ?: 0.81
    String innerDivStyle = "overflow: hidden; transform: scale(${frameScale}); transform-origin: 0 0; border: none; padding: 0; margin: 0;" 
    String outerDivStyle = "height: ${(height*frameScale).toLong()}px; width: ${width-16}px; overflow: hidden; border: none; padding: 0; margin: 0;"     
    String iframeStyle   = "height: ${height}px; width: ${width}px; border: none; scrollbar-width: none; overflow: hidden; border: none; padding: 0; margin: 0;"  
    //log.debug "inner: ${innerDivStyle} + outer: ${outerDivStyle} iframe: ${iframeStyle}"
    return "<div style = '$outerDivStyle'><div style = '$innerDivStyle'><iframe style='${iframeStyle}' scrolling='no' src='http://${gatewayAddress}'></iframe></div></div>"  
}

def processGwAggregatesResponse(response, callData) {
    //log.debug "${callData}"
    if (logLevel == "debug" | logLevel == "trace") {
       log.debug "processing gateway aggregates response"
    }
    if (!response.hasError()) {
        def data = response.json
        if (logLevel == "trace") {
          log.debug "${data}" 
        }
        def child = getPwDevice()
        updateIfChanged(child, "loadPower", data.load.instant_power.toInteger(), 100)
        updateIfChanged(child, "gridPower", data.site.instant_power.toInteger(), 100)
        updateIfChanged(child, "power", data.site.instant_power.toInteger(), 100)
        updateIfChanged(child, "solarPower", data.solar.instant_power.toInteger(), 100)
        updateIfChanged(child, "powerwallPower", data.battery.instant_power.toInteger(), 100)
    } else {
        log.debug "Error procesing gateway aggregate data. Data is ${data}"
    }       
}

def processGwSoeResponse(response, callData) {
    //log.debug "${callData}"
    if (logLevel == "debug" | logLevel == "trace") {
      log.debug "processing gateway SOE response"
    }
    if (!response.hasError()) {
        def data = response.json
        if (logLevel == "trace") {
           log.debug "${data}" 
        }
        def child = getPwDevice()
        //def batteryPercent = (data.percentage.toFloat() - 5.0)/0.95 //adjust TEG to match Tesla Server API
        def batteryPercent = (data.percentage.toFloat() - 6)/0.94 //adjust TEG to match Tesla Server API. Remove 5% and rescale 0 - 100%
        float bpRounded = Math.round(batteryPercent * 10)/10 //rounded to one decimal place 
        //log.debug "percent: ${bpRounded}"
        updateIfChanged(child, "battery", (bpRounded + 0.5).toInteger())
        updateIfChanged(child, "batteryPercent", bpRounded)
        //runIn(1, checkBatteryNotifications, [data: [batteryPercent: bpRounded, reservePercent: data.backup.backup_reserve_percent.toInteger()]])
        runIn(1, checkBatteryNotifications, [data: [batteryPercent: bpRounded, reservePercent: null]])
    } else { 
        log.debug "Error procesing gateway soe data. Data is: ${data}"
    }  
}

def processGwOperationResponse(response, callData) {
    //log.debug "${callData}"
    log.debug "processing gateway operation response"
    if (!response.hasError()) {
        def data = response.json
        log.debug "${data}" 
        def child = getPwDevice()
    } else { 
        log.debug "Error procesing gateway operation data. Data is: ${data}"
    }     
}

def processGwSiteNameResponse(response, callData) {
    //log.debug "${callData}"
    if (logLevel == "debug" | logLevel == "trace") {
       log.debug "processing gateway sitename response"
    }
    if (!response.hasError()) {
        def data = response.json
        //log.debug "${data}" 
        def child = getPwDevice()
        //updateIfChanged(child, "sitenameAndVers", data.site_name.toString() + ' ' + versionString + '\n' + gridStatusString)
        updateIfChanged(child, "siteName", data.site_name.toString())

    } else {
        log.debug "Error procesing gateway sitename data. Data is: ${data}"
    }      
}

def processPowerwallResponse(response, callData) {
    //     log.debug "${callData}"
    if (logLevel == "debug" | logLevel == "trace") {
        log.debug "processing powerwall response"
    }
    if (!response.hasError()) {
        def data = response.json.response
        if (logLevel == "trace") {
          log.debug "${data}"   
        }
        def child = getPwDevice()
        def reservePercent
        if (data.operation == "backup") {
            reservePercent = 100
        } else {
            reservePercent = data.backup.backup_reserve_percent.toInteger()
        }
        updateIfChanged(child, "reservePercent", reservePercent.toInteger())
        updateIfChanged(child, "reserve_pending", reservePercent.toInteger())
        if (data.total_pack_energy > 1) //sometimes data appears invalid
        {
            def batteryPercent = data.energy_left.toFloat() / data.total_pack_energy.toFloat() * 100.0
            
            def bpRounded = Math.round(batteryPercent * 10)/10 //rounded to one decimal place 
            //log.debug "percent: ${bpRounded}"
            updateIfChanged(child, "battery", (bpRounded + 0.5).toInteger())
            updateIfChanged(child, "batteryPercent", bpRounded)
            
            //updateIfChanged(child, "battery", Math.round(batteryPercent * 10) / 10).toInteger()
            //updateIfChanged(child, "batteryPercent", Math.round(batteryPercent * 10) / 10)
            runIn(1, checkBatteryNotifications, [data: [batteryPercent: bpRounded, reservePercent: data.backup.backup_reserve_percent.toInteger()]])
        }

        updateIfChanged(child, "loadPower", data.power_reading.load_power[0].toInteger(), 100)
        updateIfChanged(child, "gridPower", data.power_reading.grid_power[0].toInteger(), 100)
        updateIfChanged(child, "power", data.power_reading.grid_power[0].toInteger(), 100)
        updateIfChanged(child, "solarPower", data.power_reading.solar_power[0].toInteger(), 100)
        updateIfChanged(child, "powerwallPower", data.power_reading.battery_power[0].toInteger(), 100)
        def versionString = ''
        if (data.version != null) {
            versionString = 'V' + data.version.toString()
        }
        //Grid Status
        def gridStatusString
        def gridStatusEnum
        if (data.grid_status == "Inactive") {
            gridStatusString = "Status: Off-Grid"
            gridStatusEnum = "offGrid"
        } else {
            gridStatusString = "Status: On-Grid"
            gridStatusEnum = "onGrid"
        }
        def changed = updateIfChanged(child, "gridStatus", gridStatusEnum)
        if (changed) {
            if (gridStatusEnum == "offGrid") {
                runIn(1, processOffGridActions)
            } else {
                runIn(1, processOnGridActions)
            }
        }

        updateIfChanged(child, "sitenameAndVers", data.site_name.toString() + ' ' + versionString + '\n' + gridStatusString)
        updateIfChanged(child, "siteName", data.site_name.toString())

        changed = updateIfChanged(child, "pwVersion", versionString)
        if (changed && notifyWhenVersionChanges?.toBoolean()) {
            sendNotificationMessage("Powerwall software version changed to ${versionString}")
        }

        if (data.user_settings.storm_mode_enabled.toBoolean()) {
            updateIfChanged(child, "stormwatch", "Stormwatch: Enabled")
        } else {
            updateIfChanged(child, "stormwatch", "Stormwatch: Disabled")
        }

        def opMode = "Unknown"
        if (data.operation == "autonomous") {
            opMode = "Time-Based Control"
        } else if (data.operation == "self_consumption") {
            opMode = "Self-Powered"
        } else if (data.operation == "backup") {
            opMode = "Backup-Only"
        }
        changed = updateIfChanged(child, "currentOpState", opMode)
        if (changed && notifyWhenModesChange?.toBoolean()) {
            sendNotificationMessage("Powerwall op mode changed to ${opMode}")
        }
        //log.debug "grid status is: ${data.grid_status}"         
        state.lastCompletedTime = now()
    } else {
        if (response.getStatus() == 401) {
            log.warn "Powerwall resp error: ${response.getErrorMessage()}. Refreshing token"
        }
        if (callData?.attempt && callData.attempt < 2) {
            log.warn "Powerwall resp error: ${response.getErrorMessage()}. Retrying."
            runIn(30, requestPwData, [data: [attempt: callData.attempt + 1]])
        } else {
            log.error "Powerwall resp error: ${response.getErrorMessage()} on attempt ${callData?.attempt}."
        }
    }
}

def processOffGridActions() {
    log.debug "processing off grid actions"
    def child = getPwDevice()
    updateIfChanged(child, "switch", "off")
    if (notifyWhenGridStatusChanges?.toBoolean()) {
        sendNotificationMessage("Powerwall status changed to: Off Grid")
    }
    if (devicesToOffDuringOutage?.size()) {
        devicesToOffDuringOutage.off()
    }
}

def processOnGridActions() {
    log.debug "processing on grid actions"
    def child = getPwDevice()
    updateIfChanged(child, "switch", "on")
    if (notifyWhenGridStatusChanges?.toBoolean()) {
        sendNotificationMessage("Powerwall status changed to: On Grid")
    }
    if (devicesToOffDuringOutage?.size() && turnDevicesBackOnAfterOutage?.toBoolean()) {
        devicesToOffDuringOutage.on()
    }
}

def requestSiteData(data) {
    if (!state?.lastSiteRequestTime || now() - state.lastSiteRequestTime > 1000) {
        def tryCount = data?.attempt ?: 1
        //log.debug "requesting site info"
        if ((connectionMethod == null || connectionMethod != "Use Local Gateway Only") && state.foundPowerwalls) {
            httpAuthAsyncGet('processSiteResponse', "/api/1/energy_sites/${state.energySiteId}/site_info", tryCount)
        }
        state.lastSiteRequestTime = now()
    }
}

def requestLocalGwData() {
    httpAsyncGet(gatewayAddress, 'processGwAggregatesResponse', "/api/meters/aggregates", tryCount)
    httpAsyncGet(gatewayAddress, 'processGwSoeResponse', "/api/system_status/soe", tryCount)
    httpAsyncGet(gatewayAddress, 'processGwSiteNameResponse', "/api/site_info/site_name", tryCount)
            
    // Authenticate...
    //   httpAsyncGet(gatewayAddress, 'processGwOperationResponse', "/api/operation", tryCount)
    //   /api/system/update/status - version...
    //   /api/operation
    //   /api/system_status/grid_status   
}
def requestPwData(data) {
    if (!state?.lastPwRequestTime || now() - state.lastPwRequestTime > 1000) {
        def tryCount = data?.attempt ?: 1
        //log.debug "requesting powerwall data"
        if ((connectionMethod == null || connectionMethod != "Use Local Gateway Only") && state.foundPowerwalls) {
            httpAuthAsyncGet('processPowerwallResponse', "/api/1/powerwalls/${state.pwId}", tryCount)
        }
        if ((connectionMethod && connectionMethod != "Use Remote Tesla Account Server Only") && state.foundGateway && gatewayAddress) {
            if (connectionMethod == "Use Local Gateway Only" ) {
               requestLocalGwData()
            } else {
               runIn (60, requestLocalGwData) //stagger data
            }
        } 
        state.lastPwRequestTime = now()
    }
}

def commandOpMode(data) {
    //log.debug "commanding opMode to ${data.mode}"
    httpAuthPost(body: [default_real_mode: data.mode], "${data.mode} mode", "/api/1/energy_sites/${state.energySiteId}/operation", {
        resp ->
        //log.debug "${resp.data}"
    })
    runIn(2, requestPwData)
    runIn(30, processWatchdog)
}

def setSelfPoweredMode(child) {
    if (child) {    
        child.sendEvent(name: "currentOpState", value: "Pending Self-Powered", displayed: false)
    }
    runIn(1, commandOpMode, [data: [mode: "self_consumption"]])
}

def setTimeBasedControlMode(child) {
    if (child) {
        child.sendEvent(name: "currentOpState", value: "Pending Time-Based", displayed: false)
    }
    runIn(1, commandOpMode, [data: [mode: "autonomous"]])
}

def setBackupOnlyMode(child) {
    if (child) {
        child.sendEvent(name: "currentOpState", value: "Pending Backup-Only", displayed: false)
    }
    runIn(1, commandOpMode, [data: [mode: "backup"]])
}

def commandTouStrategy(data) {
    log.debug "commanding TOU strategy to ${data.strategy}"
    //request Site Data to get a current tbc schedule. Schedule needs to be sent on tou strategy command or else schedule will be re-set to default
    def latestSchedule
    try {
        httpAuthGet("/api/1/energy_sites/${state.energySiteId}/site_info", {
            resp ->
            //log.debug "${resp.data}"
            if (resp?.data?.response?.tou_settings?.schedule) {
                latestSchedule = resp.data.response.tou_settings.schedule
                //log.debug "got schedule ${latestSchedule}"
            }
        })
    } catch (Exception e) {
        log.debug "Exception ${e} getting latest schedule"
    }
    if (latestSchedule == null) {
        //log.debug "setting latest schedule to last known state"
        latestSchedule = state.lastSchedule
    }

    def commands = [tou_settings: [optimization_strategy: data.strategy, schedule: latestSchedule]]
    httpAuthPost(body: commands, "${data.strategy}", "/api/1/energy_sites/${state.energySiteId}/time_of_use_settings", {
        resp -> //log.debug "${resp.data}"
        //log.debug "TOU strategy command sent"
    })
    runIn(2, requestSiteData)
    runIn(30, processWatchdog)
}

def setTbcBalanced(child) {
    //log.debug "commanding TBC Balanced"
    if (child) {
        child.sendEvent(name: "currentStrategy", value: "Pending Balanced", displayed: false)
    }
    runIn(2, commandTouStrategy, [data: [strategy: "balanced"]])
}

def setTbcCostSaving(child) {
    //log.debug "commanding TBC CostSaving"
    if (child) {
        child.sendEvent(name: "currentStrategy", value: "Pending Cost-Saving", displayed: false)
    }
    runIn(2, commandTouStrategy, [data: [strategy: "economics"]])
}

def commandBackupReservePercent(data) {
    //log.debug "commanding reserve to ${data.reservePercent}%"
    httpAuthPost(body: [backup_reserve_percent: data.reservePercent], "reserve ${data.reservePercent}%",
    "/api/1/energy_sites/${state.energySiteId}/backup", {
        resp ->
    })
    runIn(2, requestPwData)
    runIn(30, processWatchdog)
}

def setBackupReservePercent(child, value) {
    //log.debug "commanding reserve to ${value}%"
    if (value >= 0 && value <= 100) {
        runIn(2, commandBackupReservePercent, [data: [reservePercent: value]])
    } else {
        log.debug "Backup reserve percent of: ${value} not sent. Must be between 0 and 100"
    }
}

def commandStormwatchEnable() {
    httpAuthPost(body: [enabled: true], "stormwatch mode enable", "/api/1/energy_sites/${state.energySiteId}/storm_mode", {
        resp -> //log.debug "${resp.data}"
        //log.debug "Stormwatch enable command sent"
    })
    runIn(3, requestPwData)
    runIn(30, processWatchdog)
}

def commandStormwatchDisable() {
    //log.debug "commanding stormwatch disable"
    httpAuthPost(body: [enabled: false], "stormwatch mode enable", "/api/1/energy_sites/${state.energySiteId}/storm_mode", {
        resp -> //log.debug "${resp.data}"
        //log.debug "Stormwatch disable command sent"
    })
    runIn(2, requestPwData)
    runIn(30, processWatchdog)
}

def enableStormwatch(child) {
    log.debug "commanding stormwatch on"
    runIn(2, commandStormwatchEnable)
}

def disableStormwatch(child) {
    log.debug "commanding stormwatch off"
    runIn(2, commandStormwatchDisable)
}

def refresh(child) {
    if (logLevel == "debug" | logLevel == "trace") {
        log.debug "refresh requested"
    }
    runIn(1, processMain)
    runIn(30, processWatchdog)
}

def processWatchdog() {
    def lastTimeProcessed
    def lastTimeCompleted
    if (!state.lastProcessedTime | !state.lastCompletedTime) {
        lastTimeProcessed = now()
        lastTimeCompleted = now()
    } else {
        lastTimeProcessed = state.lastProcessedTime
        lastTimeCompleted = state.lastCompletedTime
    }

    def secondsSinceLastProcessed = (now() - lastTimeProcessed) / 1000
    def secondsSinceLastProcessCompleted = (now() - lastTimeCompleted) / 1000
    def maxDownTime = 1800
    if (pollingPeriod) {
       if (pollingPeriod == "30 minutes") {
          maxDownTime = 6000
       } else if (pollingPeriod == "1 hour") {
          maxDownTime = 8000
       } else if (pollingPeriod == "Do not poll") {
          maxDownTime = 700000
       } 
    }

    if (secondsSinceLastProcessed > maxDownTime) {
        if (!state?.processedWarningSent) {
            sendNotificationMessage("Warning: Powerwall Manager has not executed in ${(secondsSinceLastProcessed/60).toInteger()} minutes. Reinitializing",
                "anomaly")
            state.processedWarningSent = true
            refreshToken()
        }
        runIn(30, initialize)
    } else {
        if (secondsSinceLastProcessCompleted > maxDownTime) {
            if (!state?.completedWarningSent) {
                sendNotificationMessage(
                    "Warning: Powerwall Manager has not successfully received and processed data in ${(secondsSinceLastProcessCompleted/60).toInteger()} minutes. Reinitializing",
                    "anomaly")
                state.completedWarningSent = true
                refreshToken()
            }
            runIn(30, initialize)
        } else {
            if (state?.completedWarningSent || state?.processedWarningSent) {
                sendNotificationMessage("Info: Powerwall Manager has successfully resumed operation", "anomaly")
                state.completedWarningSent = false
                state.processedWarningSent = false
            }
        }
    }
}

def processMain() {
    state.lastProcessedTime = now()
    def lastStateProcessTime
    if (state.lastStateRunTime == null) {
            lastStateProcessTime = 0
    } else {
            lastStateProcessTime = state.lastStateRunTime
    }
    def secondsSinceLastRun = (now() - lastStateProcessTime) / 1000
    //log.debug "${location.timeZone}"
    if (secondsSinceLastRun > 60) {
        state.lastStateRunTime = now()
        runIn(1, requestPwData)
        runIn(10, requestSiteData)
        if (state?.schedule_refresh_token && state.refresh_token != null && state.token_expires_on != null) {
            Long refreshDateEpoch = state.token_expires_on.toLong() * 1000
            //log.debug "Token refresh date is ${refreshDateEpoch}"
            def refreshDate = new Date(refreshDateEpoch) - 2 // Two days before due
            log.debug "Scheduling Token refresh on ${refreshDate}."
            runOnce(refreshDate, refreshToken)
            state.schedule_refresh_token = false
        }
    }
}