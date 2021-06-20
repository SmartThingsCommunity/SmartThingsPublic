/**
 *  Demand Manager
 *
 *  Author: eric@darwinsden.com
 *  Copyright 2018, 2019, 2020, 2021 - All rights reserved
 *
 *  For questions, more information, or to provide feedback on this smart app, please visit: 
 *
 *    darwinsden.com/demand/
 *  
 *  ****** WARNING ******
 *  Installation and configuration of this software will grant this application control of your home thermostat and other devices. 
 *  Unexpectedly high and low home temperatures and unexpected utility usage & costs may result due to both the planned 
 *  and unplanned nature of the algorithms and technologies involved, the unreliability of devices and networks, and unanticipated 
 *  software defects including those in this software application and its dependencies. By installing this software, you are accepting
 *  the risks to people, pets, and personal property.
 *  
 *  This software was developed in the hopes that it will be useful to others, however, 
 *  it is distributed on as "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, 
 *  any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are solely responsible for 
 *  determining the appropriateness of using this work and assume any associated risks. In no event and under no legal theory, whether in tort 
 *  (including negligence), contract, or otherwise, shall any contributor be liable to you for damages, including any direct, indirect, special, 
 *  incidental, or consequential damages of any character arising as a result of the use or inability to use this work (including but not limited 
 *  to damages for loss of goodwill, work stoppage, computer failure or malfunction, or any and all other damages or losses), even if such
 *  contributor has been advised of the possibility of such damages.
 * 
 *  The end user is free to modify this software for personal use. Re-distribution of this software in its original or 
 *  modified form requires explicit written consent from the developer. 
 * 
 *  The developer retains all rights, title, and interest, including all copyright, patent rights, and trade secrets 
 *  associated with the algorithms and technologies used herein. 
 *
 */

def version() {
    return "v0.3.30.20210620"
}

/*   
 *	17-Jun-2021 >>> v0.3.30.20210620 - UI updates.
 *	27-May-2020 >>> v0.3.2e.20200527 - Additional Hubitat compatibility updates.
 *	10-Jan-2020 >>> v0.3.1e.20200111 - Initial basic cross-platform support for Hubitat.
 *	16-Oct-2019 >>> v0.2.4e.20191016 - Added support for multiple thermostats and updated watchdog to only notify once on issue occurrence and resolution.
 *	28-Jul-2019 >>> v0.2.2e.20190728 - Added support for Griddy and ComEd utility pricing peak period triggers. Added support for Powerwall as a solar and grid meter. 
 *	05-Jul-2019 >>> v0.2.1e.20190705 - Added support for multiple peak control and display devices. Note: update requires these devices to be re-entered in app preferences. 
 *	03-Jul-2019 >>> v0.2.0e.20190703 - Added option to re-set set-point after each cycle. Resolve issue that could result in multiple thermostat commands. 
 *	04-Jun-2019 >>> v0.1.5e.20190604 - Fix issue with turning back on peak demand exceeded devices at start of new cycle. 
 *	30-May-2019 >>> v0.1.4e.20190530 - Resolve new install/init issue
 *	28-May-2019 >>> v0.1.3e.20190528 - Added option to persist off-peak indication display, improved threading, additional watchdog logic
 *	07-May-2019 >>> v0.1.2e.20190507 - Added additional exception handling and watchdog processes, logic updates
 */

import groovy.transform.Field

def warning() {
    return "WARNING: By enabling this feature, you are granting permission of this application to actively control your thermostat, " +
        "which may carry risks to people, pets and personal property. Normal operation and/or unexpected failures or defects in this software, " +
        "dependent hardware, and/or network may result in unexpected temperatures extremes."
}

definition(
    name: "Demand Manager Alpha", namespace: "darwinsden", author: "eedwards", description: "Electric Energy Demand Management.",
    category: "My Apps", iconUrl: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/meterColor.png",
    iconX2Url: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/meterColor.png"
)

preferences {
    page(name: "pageMain")
    page(name: "pageNominalData")
    page(name: "pageDisplayIndicators")
    page(name: "pageSettings")
    page(name: "pageRequiredInfo")
    page(name: "pageThermostat")
    page(name: "pagePeakSchedule")
    page(name: "pagePeakSchedule1")
    page(name: "pagePeakSchedule2")
    page(name: "pagePeakSchedule3")
    page(name: "pagePeakDayHolidays")
    page(name: "pageNotifications")
    page(name: "pageEnergyConsumers")
    page(name: "pageEnergyGenerators")
    page(name: "pageManageDemand")
    page(name: "pageMonitorDemand")
    page(name: "pageAdvancedSettings")
    page(name: "pageDevicesToControl")
    page(name: "pagePrecoolSettings")
    page(name: "pageAdvancedThermostatCommandSettings")
    page(name: "pageComEdPricing")
    page(name: "pageGriddyPricing")
}

String requiredInfoStatus() {
    String statusStr
    if (settings.goalDemandInWatts != null && settings.cycleTime) {
        String opMode 
        switch (mode()) {
            case "monitorOnly" :
                opMode = "Monitor Only"
                break
            case "notifyOnly" :
                opMode = "Notify Only"
                break
            case "fullControl" :
                opMode = "Full Control"
                break
            default :
                break 
                opMode = "Full Control"
        }
        statusStr = "Goal Demand: ${goalDemandInWatts} Watts\n" + "Mode: " + opMode + "\nDemand Cycle Time: ${cycleTime}"
    }
    return statusStr
}

String appendOnNewLine(message, textToAdd) {
    if (textToAdd) {
       if (message) {
           message = message + "\n" + textToAdd
       } else {
           message = textToAdd
       }
    }
    return message
}

String scheduleStatus() {
    String status
    if (atomicState.scheduleCount > 0) {
        status = appendOnNewLine(status, "${atomicState.scheduleCount} Active Schedule")
        if (atomicState.scheduleCount > 1) {
            status = status + 's.'
        } else {
            status = status + '.'
        }
    }
    if (peakHoursFromGriddyPricing) {
        status = appendOnNewLine(status, "Peak Hours from Griddy Pricing enabled.")
    }
    if (peakHoursFromComEdPricing) {
        status = appendOnNewLine(status, "Peak Hours from ComEd Pricing enabled.")
    }
    return status
}

private pageMain() {
    dynamicPage(name: "pageMain", title: "", install: true, uninstall: true) {
        section() {
            if (hubIsSt()) {
                  paragraph app.versionDetails(), title: "Demand Manager", required: false, image: demandIcon
            } else {
                  paragraph "<img src ='${demandIcon}' width = '60' align='left' style = 'margin-top: -6px; padding-right: 15px'>Demand Manager\n${app.versionDetails()}"
            }               
        }
        section("Required Set Up:") {
            String status = requiredInfoStatus()
            Boolean valid = (status != '' && status != null)
            if (!valid) {
                status = "Required configuration information has not been set."
            }
            hrefMenuPage (pageRequiredInfo, "Required: Goal Demand, Mode, and Demand Cycle Time..", status, controlsIcon, null, valid ? "complete" : null)
            status = scheduleStatus() 
            valid = (status != '' && status != null)
            if (!valid) {
                status = "No schedules or utility price triggers are active. These are required to trigger demand actions, unless you set the Demand Manager device On/Off state directly."
            }
            hrefMenuPage (pagePeakSchedule, "Peak Utility Schedules or Utility Price Triggers", status, schedIcon, null, valid ? "complete" : null)
            status = consumersStatus() 
            hrefMenuPage (pageEnergyConsumers, "Energy Consumers and Nominal Usage..", status, eMeterIcon, null, atomicState.consumerStatusOk ? "complete" : null)
            if (powerGenerator1) {
                status = "Solar Power: ${powerGenerator1}"
                valid = true
            } else {
                status = "No optional solar power meter selected"
                valid = false
            }
            hrefMenuPage (pageEnergyGenerators, "Energy Generation..", status, solarIcon, null, valid ? "complete" : null)
        }
        
        section("Manage and Monitor Demand:") {
            String status = manageDemandStatus()
            hrefMenuPage (pageManageDemand, "Actively Manage Your Demand..", status, levelsIcon, null, atomicState.managingDemand ? "complete" : null)
            hrefMenuPage (pageMonitorDemand, "Monitor Your Demand..","Display devices and notifications", notifyIcon, null, null ? "complete" : null)
        }

        section("") {
           hrefMenuPage (pageAdvancedSettings, "Advanced settings..", "",cogIcon, null)
           href(name: "Survey", title: "Please help improve the Demand Manager app.",
             description: "Take the Demand Manager Survey",
             required: false,
             url: "https://www.surveymonkey.com/r/RDFFJQM")
        }
        section() {
            String freeMsg = "This is free software. Donations are very much appreciated, but are not required or expected."
            if (hubIsSt()) {
                href(name: "Site", title: "For more information, questions, or to provide feedback, please visit: ${ddUrl}",
                  description: "Tap to open the Demand Manager web page on DarwinsDen.com",
                  required: false,
                  image: ddLogoSt,
                  url: ddUrl)
                href(name: "", title: "",
                  description: freeMsg,
                  required: false,
                  image: ppBtn,
                  url: "https://www.paypal.com/paypalme/darwinsden")
            } else {             
                String ddMsg = "For more information, questions, or to provide feedback, please visit: <a href='${ddUrl}'>${ddUrl}</a>"
                String ddDiv = "<div style='display:inline-block;margin-right: 20px'>" + "<a href='${ddUrl}'><img src='${ddLogoHubitat}' height='40'></a></div>"
                String ppDiv = "<div style='display:inline-block'>" + "<a href='https://www.paypal.com/paypalme/darwinsden'><img src='${ppBtn}'></a></div>" 
                paragraph "<div style='text-align:center'>" + freeMsg + " " + ddMsg + "</div>"
                paragraph "<div style='text-align:center'>" + ddDiv + ppDiv + "</div>" 
         } 
        }
    }
}

String manageDemandStatus() {
    String status 
    atomicState.managingDemand = false
    if (commandThermostat) {
        if (homeThermostats) {
            status = "Managing AC thermostat."
            atomicState.managingDemand = true 
        } else {
            status = "No AC thermostats have been selected on the 'Energy Consumer' page."
        }   
    }
    Boolean commandingDevices
    if (devicesToTurnOffDuringPeak || devicesToTurnOffOnlyDuringPeak) {
        status = appendOnNewLine(status, "Turning off devices during peak periods.")
        commandingDevices = true
        atomicState.managingDemand= true
    }
    if (deviceToTurnOffDuringPeakDemand || deviceToTurnOffOnlyDuringPeakDemand) {
        status = appendOnNewLine(status, "Turning off devices if projected demand exceeds goal.")
        commandingDevices = true
        atomicState.managingDemand
    }
    if (!atomicState.managingDemand) {
          status = appendOnNewLine(status, "No devices are being controlled by the Demand Manager.")
    }
    return status
}                     

def pageManageDemand() {
    dynamicPage(name: "pageManageDemand", title: "", install: false, uninstall: false) {
        section("Actively manage demand by controlling devices and adjusting your air conditioning thermostat") {
            hrefMenuPage ("pageDevicesToControl", "Manage demand by turning off devices during peak periods..", "",
                                   "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png", null)
            hrefMenuPage ("pageThermostat", "Manage demand by automatically adjusting your thermostat cooling setpoint..", "",
                                   "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/thermostat120.png", null)
        }
    }
}

def pageMonitorDemand() {
    dynamicPage(name: "pageMonitorDemand", title: "Monitor your demand and solar generation", install: false, uninstall: false) {
        section() {
            hrefMenuPage ("pageDisplayIndicators", "Choose display indicator devices..", "",
                                   "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/dashboard120.png", null)
            hrefMenuPage ("pageNotifications", "Notification preferences..", "Choose how you get notified of demand events", 
                                     "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/notification40.png", null)
        }
    }
}

String nominalDataStatus() {
    String status
    if (airCondWatts != null) {
        status = "AC Watts (average): ${airCondWatts} W"
    }
    if (nomUsageWatts != null) {
        status = appendOnNewLine(status, "Nominal Home consumption during peak hours: ${nomUsageWatts} W")
    }
    return status
}

String consumersStatus() {
    String status
    atomicState.consumerStatusOk = false
    if (wholeHomePowerMeter) {
        status = "Power meter: ${wholeHomePowerMeter}"
    } else {
        status = "A home power meter is required for accurate demand calculations."
    }
    if (homeThermostats) {
        status = appendOnNewLine(status, "Thermostat(s): ${homeThermostats}")
    } else {
        status = appendOnNewLine(status, "Selecting a thermostat can improve your demand projections and allow you to actively manage demand.")
    }
    atomicState.consumerStatusOk = wholeHomePowerMeter && homeThermostats
    status = appendOnNewLine(status, nominalDataStatus() )
    return status
}

def pageEnergyConsumers() {
    dynamicPage(name: "pageEnergyConsumers", title: "Enter your major energy consumers and nominal home usage data", install: false, uninstall: false) {
        section ("A Whole home Power Meter is required for accurate demand calculations. If you select the DarwinsDen Powerwall device here, the Demand Manager will use the Powerwall's grid power value") {
            input "wholeHomePowerMeter", "capability.powerMeter", required: false, title: "Home Power Meter", image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/energyMeter.png"
        }
        String msg = "Projected demand estimates are important for determining if your current usage trends are likely to exceed your demand goals during a 30/60 minute demand period." +
            " Monitoring major energy consumers such as your air conditioning thermostat and entering in nominal home usage helps to refine these projections."
        section (msg) {
            String status = nominalDataStatus()
            Boolean complete = status != null
            if (!complete) {
                status = "Tune your data. Refining estimates here will further improve your projected demand estimates."
            }
            hrefMenuPage ("pageNominalData", "Nominal home usage data...", status, null, null, complete ? "complete" : null)
            paragraph "Thermostats will be monitored to project future demand needs during each demand cycle. " +
                "Thermostat(s) selected here can also be controlled to manage demand if the option is enabled in the 'Manage Demand' section."
            input "homeThermostats", "capability.thermostat", required: false, multiple: true, title: "Thermostat(s)"
        }
    }
}

def pageEnergyGenerators() {
    dynamicPage(name: "pageEnergyConsumers", title: "", install: false, uninstall: false) {        
        String msg = "Projected demand estimates are important for determining if your current usage trends are likely to exceed your demand goals during a 30/60 minute demand period." +
            " If you are generating solar power, monitoring solar power generation helps to accurately project your demand."
        section (msg) {
            paragraph "If you have a Powerwall, you may select the DarwinsDen Powerwall device here as the solar power meter, and the Demand Manager will use the Powerwall's solar power value"
            input "powerGenerator1", "capability.powerMeter", required: false, title: "Solar Power Meter"
        }
    }
}

def pageRequiredInfo() {
    dynamicPage(name: "pageRequiredInfo", title: "", install: false, uninstall: false) {
        section (){
            input "goalDemandInWatts", "number", required: false, defaultValue: 3000, title: "Your Goal Demand Watts"
            input "operationMode", "enum", required: true, title: "Operation Mode", options: ["monitorOnly": "MONITOR ONLY: Do not perform demand management actions",
                "notifyOnly": "NOTIFY: Monitor and send demand notifications", "fullControl": "FULL: Monitor, notify and manage devices and/or thermostat"
                ], image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/controls.png"
            input "cycleTime", "enum", options: ["30 minutes", "60 minutes"], required: true, title: "Demand Cycle Period", image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/timer.png"
        }
    }
}

def pageNominalData() {
    dynamicPage(name: "pageNominalData", title: "This information helps the Demand Manager project future demand needs during each 30/60 minute demand period.",
        install: false, uninstall: false) {
        section("What is the estimated Watt usage of your air conditioner when it is in use? Use an average value of a single unit if more than one thermostat/AC unit is being used (default 5000W)... ") {
            input "airCondWatts", "number", required: false, defaultValue: 5000, title: "Estimated Air Conditioner Watts"
        }
        section("What is the estimated typical Watt usage of your home when in normal use? This should assume average typical occasional usage of your appliances such as microwave oven, televisions, etc. " +
                "This should not include your main air conditioner " +
                "since your AC Watt usage can be directly known by monitoring your thermostat status (if a thermostat is specified in this app). " +
               "If a whole home battery such as a Powerwall is installed and configured to keep your usage nominally at 0 during demand periods, your nominal usage should be set to '0'. (default 1000W)") {
            input "nomUsageWatts", "number", required: false, defaultValue: 1000, title: "Estimated Nominal Home Usage Watts"
        }
    }
}

String formatTimeString(timeSetting) {
    def timeFormat = new java.text.SimpleDateFormat("hh:mm a")
    def isoDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    def isoTime = new java.text.SimpleDateFormat(isoDatePattern).parse(timeSetting.toString())
    return timeFormat.format(isoTime).toString()
}

String getScheduleString(timeSetting1, timeSetting2, monthSetting) {
    String str
    if (timeSetting1 && timeSetting2) {
        str = "Peak Start: " + formatTimeString(timeSetting1) + "\nPeak End: " + formatTimeString(timeSetting2)
        if (monthSetting && monthSetting != '' & monthSetting != "N/A" ) {
            str = str + "\nMonths: " + monthSetting.toString()
        }
    } else {
        str = "Requires stop and start times. Select to add.."
    }
    return str
}

String getScheduleName (String schedName, Boolean schedActive, Integer schedNumber) {
    String name = schedName ?: "Schedule ${schedNumber}"
    if (!schedActive) {
        name = name + " (Disabled)"
    }
    return name
}
    
def pagePeakSchedule() {
    dynamicPage(name: "pagePeakSchedule", title: "Enter utility peak hour schedule and utility price triggers", install: false, uninstall: false) {
        section("Peak hours will occur on weekdays for the hours chosen here unless the day is specified as a holiday below.") {
            atomicState.scheduleCount = 0
            //Schedule 1
            String msgStr = getScheduleString(schedule1StartTime, schedule1StopTime, monthsSchedule1)
            Boolean valid = schedule1StartTime && schedule1StopTime
            String icon
            if (!valid) {
                icon = schedEditIcon
            } else {
                icon = schedOkIcon
            }   
            Boolean scheduleActive = valid && schedule1IsActive
            if (scheduleActive) {
                 atomicState.scheduleCount = atomicState.scheduleCount + 1
            }
            String schedName = getScheduleName (schedule1Name, schedule1IsActive, 1)
            hrefMenuPage ("pagePeakSchedule1", schedName, msgStr, icon, null, scheduleActive ? "complete" : null)
            
            //Schedule 2
            msgStr = getScheduleString(schedule2StartTime, schedule2StopTime, monthsSchedule2)
            valid = schedule2StartTime && schedule2StopTime
            if (!valid) {
                icon = schedEditIcon
            } else {
                icon = schedOkIcon
            }   
            scheduleActive = valid && schedule2IsActive
            if (scheduleActive) {
                 atomicState.scheduleCount = atomicState.scheduleCount + 1
            }
            schedName = getScheduleName(schedule2Name, schedule2IsActive, 2)
            hrefMenuPage ("pagePeakSchedule2", schedName, msgStr, icon, null, scheduleActive ? "complete" : null)
            
            //Schedule 3
            msgStr = getScheduleString(schedule3StartTime, schedule3StopTime, monthsSchedule3)
            valid = schedule3StartTime && schedule3StopTime
            if (!valid) {
                icon = schedEditIcon
            } else {
                icon = schedOkIcon
            }   
            scheduleActive = valid && schedule3IsActive
            if (scheduleActive) {
                 atomicState.scheduleCount = atomicState.scheduleCount + 1
            }
            schedName = getScheduleName(schedule3Name, schedule3IsActive, 3)
            hrefMenuPage ("pagePeakSchedule3", schedName, msgStr, icon, null, scheduleActive ? "complete" : null)
        }
        section("Utility Peak Day Holidays (Future Capability)") {
            hrefMenuPage (pagePeakDayHolidays, "Enter holidays from peak utility periods...", "", null, null)
        }
        section("Peak Hour utility pricing triggers") {
            hrefMenuPage (pageComEdPricing, "Set Peak Period based on ComEd real-time prices", "", activityIcon, null)
            hrefMenuPage (pageGriddyPricing, "Set Peak Period based on Griddy real-time prices", "", activityIcon, null)
        }
    }
}

def pagePeakSchedule1() {
    dynamicPage(name: "pagePeakSchedule1", title: "Enter Your Utility Schedule 1 Peak Hours and Months.", install: false, uninstall: false) {
        section() {
            input "schedule1IsActive", "bool", required: false, defaultValue: false, title: "Enable this schedule"
            input "schedule1StartTime", "time", required: false, title: "Start Time (schedule 1)"
            input "schedule1StopTime", "time", required: false, title: "End Time (schedule 1)"
            input "monthsSchedule1", "enum", title: "In which months? (optional - if no months are selected, the schedule will execute for all months)", required: false, multiple: true,
                options: ["January": "January", "February": "February", "March": "March", "April": "April", "May": "May", "June": "June", "July": "July",
                    "August": "August", "September": "September", "October": "October", "November": "November", "December": "December"]
            input "schedule1Name", "text", required: false, title: "Name this schedule (optional)"
        }
    }
}

def pagePeakSchedule2() {
    dynamicPage(name: "pagePeakSchedule2", title: "Enter Your Utility Schedule 2 Peak Hours and Months.", install: false, uninstall: false) {
        section() {
            input "schedule2IsActive", "bool", required: false, defaultValue: false, title: "Enable this schedule"
            input "schedule2StartTime", "time", required: false, title: "Start Time (schedule 2)"
            input "schedule2StopTime", "time", required: false, title: "End Time (schedule 2)"
            input "monthsSchedule2", "enum", title: "In which months? (optional - if no months are selected, the schedule will execute for all months)", required: false, multiple: true,
                options: ["January": "January", "February": "February", "March": "March", "April": "April", "May": "May", "June": "June", "July": "July",
                    "August": "August", "September": "September", "October": "October", "November": "November", "December": "December"]
            input "schedule2Name", "text", required: false, title: "Name this schedule (optional)"
        }
    }
}

def pagePeakSchedule3() {
    dynamicPage(name: "pagePeakSchedule3", title: "Enter Your Utility Schedule 3 Peak Hours and Months.", install: false, uninstall: false) {
        section() {
            input "schedule3IsActive", "bool", required: false, defaultValue: false, title: "Enable this schedule"
            input "schedule3StartTime", "time", required: false, title: "Start Time (schedule 3)"
            input "schedule3StopTime", "time", required: false, title: "End Time (schedule 3)"
            input "monthsSchedule3", "enum", title: "In which months? (optional - if no months are selected, the schedule will execute for all months)", required: false, multiple: true,
                options: ["January": "January", "February": "February", "March": "March", "April": "April", "May": "May", "June": "June", "July": "July",
                    "August": "August", "September": "September", "October": "October", "November": "November", "December": "December"]
            input "schedule3Name", "text", required: false, title: "Name this schedule (optional)"
        }
    }
}

def pageComEdPricing() {
    dynamicPage(name: "pageComEdPricing", title: "Set Peak Period based on ComEd real-time pricing.", install: false, uninstall: false) {
        section() {
            input "peakHoursFromComEdPricing", "bool", required: false, defaultValue: false, title: "Set peak hours based on real-time ComEd pricing"
            input "comEdPeakPriceTrigger", "decimal", required: false, defaultValue: false, title: "Enter the value in cents per kW that should trigger a peak period (for example: 4.1)"
        }
    }
}

def pageGriddyPricing() {
    dynamicPage(name: "pageGriddyPricing", title: "Set Peak Period based on Griddy real-time pricing.", install: false, uninstall: false) {
        section() {
            input "peakHoursFromGriddyPricing", "bool", required: false, defaultValue: false, title: "Set peak hours based on real-time Griddy pricing"
            input "griddyPeakPriceTrigger", "decimal", required: false, defaultValue: false, title: "Enter the value in cents per kW that should trigger a peak period (for example: 4.1)"
            input "griddyLoadZone", "text", required: false, defaultValue: false, title: "Griddy load zone/settlement point (eg: LZ_HOUSTON)"
        }
    }
}

def pageDisplayIndicators() {

    def colorIndicatorDevices = [
		name:				"colorIndicatorDevices",
		type:				"capability.colorControl",
		title:				"Select your peak period color indicator devices",
		multiple:			true,
		required:			false
    ]

    def wD200Dimmers = [
		name:				"wD200Dimmers",
		type:				"capability.switchLevel",
		title:				"Select your HomeSeer WD200+ dimmers",
		multiple:			true,
		required:			false
    ]
    
    dynamicPage(name: "pageDisplayIndicators", title: "Choose display indicator devices (optional)", install: false, uninstall: false) {
        section("Select a color indicator light (such as the EZ MultiPli/HomeSeer HSM200) to indicate when you're in a peak demand period. Note: " +
            "The indicator on green light will only be briefly displayed during off-peak unless the persist off-peak display is set below.") {
            input colorIndicatorDevices
            input "alwaysDisplayOffPeakIndicator", "bool", required: false, defaultValue: false, title: "Persist off-peak indication display"
        }
        section("Select HomeSeer WD200+ dimmers to be used as demand warning indicators and solar (if present) production level indicators. " +
            "The Demand Manager will use the colored LED's on the switch plates as graphing indicators (not the bulbs connected to the loads).") {
            input wD200Dimmers
            input "solarSystemSizeWatts", "number", required: false,
                default: 6000, title: "Size of your Solar System in Watts"
        }
        section("Install Virtual Demand Meters (Current, Projected, Peak Today, and Peak Month) for use with dashboards (ActionTiles, etc..). " +
            "These should be added to the dashboard smart app as Power Meters.") {
            input "installVirtualDemandMeters", "bool", required: false, defaultValue: false, title: "Install Virtual Demand Meters"
        }
    }
}

def pageNotifications() {
    dynamicPage(name: "pageNotifications", title: "Notification Preferences", install: false, uninstall: false) {
        section("Notification Triggers:") {
            input "notifyWhenCycleDemandExceeded", "bool", required: false, defaultValue: false, title: "Demand goal has been exceeded for 30/60 minute cycle"
            input "notifyWhenMonthlyDemandExceeded", "bool", required: false, defaultValue: false, title: "New monthly high demand"
            input "notifyWithGeneralDemandStatus", "bool", required: false, defaultValue: false, title: "General demand status (peak period starts/stops, etc.)"
            input "notifyWhenAnomalies", "bool", required: false, defaultValue: true, title: "When Demand Manager nomalies are encountered"
        }
        section("Notify when thermostat is controlled - or when the Demand Manager recommends that the air conditioner should be turned off if thermostat control is disabled") {
            input "notifyWhenThermostatControlled", "bool", required: false, defaultValue: false, title: "Pending or suggested thermostat control"
        }

        section("Amount in Watts that the projected cycle demand can exceed before a notification is sent (only applies when cycle demand notifications are on).") {
            input "notifyWhenDemandExceededBuffer", "number", required: false, defaultValue: 0, title: "Cycle demand exceeded buffer (Watts)"
        }

        if (hubIsSt()) {
            section("Notification method (push notifications are via mobile app)") {
                input "notificationMethod", "enum", required: false, defaultValue: "push", title: "Notification Method", options: ["none", "text", "push", "text and push"]
            }
            section("Phone number for text messages") {
                input "phoneNumber", "phone", title: "Phone number for text messages", description: "Phone Number", required: false
            }
        } else {
            //Hubitat
            section() {
               input(name: "notifyDevices", type: "capability.notification", title: "Send to these notification devices", required: false, multiple: true, submitOnChange: true)
            }
        }
    }
}

def pagePeakDayHolidays() {
    def holidays = [
		name:				"holidays",
		type:				"date",
		title:				"Enter holidays from peak periods",
		multiple:			false,
		required:			false
    ]
    dynamicPage(name: "pagePeakDayHolidays", title: "Future Capability. Not currently implemented", install: false, uninstall: false) {
        section("Fixed Date Holidays") {
            input "fixedDateHolidays", "enum", title: "Select fixed date holidays", required: false, multiple: true,
                options: ["Christmas", "New Year's Day", "Independence Day (US)","Thanksgiving (US)"]
        }
       section("Other Holiday 1") {
          input name: "holiday1Month", type: "number", title: "Month", required: false
          input name: "holiday1Day", type: "number", title: "Day", required: false
       }
       section("Other Holiday 2") {
          input name: "holiday2Month", type: "number", title: "Month", required: false
          input name: "holiday2Day", type: "number", title: "Day", required: false
       }      
    }
}

def pageDevicesToControl() {
    dynamicPage(name: "pageDevicesToControl", title: "Manage demand by turning off devices during peak periods or demand events", install: false, uninstall: false) {
        section("Enter devices to turn off during peak utility periods:") {
            input name: "devicesToTurnOffDuringPeak", type:	"capability.switch", title:	"Devices to turn off when the peak period starts. These devices will " +
                "be turned on again after the peak period is over (regardless of " +
                "their state prior to the peak period starting).", multiple: true, required: false
            input name: "devicesToTurnOffOnlyDuringPeak", type:	"capability.switch", title:	"Devices to turn off when the peak period starts. These devices will " +
                "NOT be turned on again after the peak period is over.", multiple: true, required: false
        }
        section("Enter devices to turn off when peak demand is projected to exceed your goal demand during any 30 or 60 minute demand period:") {
            input name:	"deviceToTurnOffDuringPeakDemand", type: "capability.switch", title: "Devices to turn off when demand goal is exceeded. " +
                "These devices will be turned on again at the beginning of the next 30 or 60 minute demand cycle (regardless of their state prior to the demand exceeded event).", 
                    multiple: true, required: false
            input name:	"deviceToTurnOffOnlyDuringPeakDemand", type: "capability.switch", title: "Devices to turn off when demand goal is exceeded. " +
                "These devices will Not be turned on again at the beginning of the next 30 or 60 minute demand cycle.", multiple: true, required: false
        }
    }
}

String formatText (String text, String beginTag, String endTag) {
    String output 
    if (!hubIsSt()) {
        output = beginTag + text + endTag
    } else {
        output = text
    }
}
                
def pageThermostat() {
    dynamicPage(name: "pageThermostat", title: "Manage your thermostat based on demand", install: false, uninstall: false) {
        section() {
            String msg = "When 'Command Thermostat' is enabled, the Demand Manager will " +
                 "control the thermostat to temporarily halt your air conditioner during peak periods if it projects that your 30/60 minute demand goal will otherwise not be met. " +
                 "This can result in relatively short air conditioner duty cycles in high demand situations. \n\n" +
                 "Halting of your AC is accomplished by temporarily bumping up your " +
                 "thermostat cooling set-point to trigger it's hysteresis check, before returning the set-point back down again. If the Demand Manager " +
                 "determines that it is unable to adequately halt your AC when returning your thermostat to its previous temperature, " +
                 "it will leave the setpoint at a higher value than was previously set. This can potentially result in rapidly increasing home temperatures in high demand and high heat situations. "  +
                 "The maximum temperature that the Demand Manager will be permitted to raise your thermostat to can be adjusted below.\n\n" +
                 "If multiple thermostats have been selected, the Demand Manager will sequentially alternate control of each for a given 30 or 60 minute demand period. \n\n" +
                 "It is recommended that your home be pre-cooled prior to your peak utility hours if this option is enabled. You may wish to program your " +
                 "smart thermostat(s) to return your home to your preferred temperature when peak hours are over."
            paragraph formatText(msg, "<p style ='background-color: #FCF5E5; border: 1px; border-style:solid; padding: 1em; font-size:95%'>", "</p>")
            paragraph warning()
            input "commandThermostat", "bool", required: false, defaultValue: false, title: "Command Thermostat", submitOnChange : true
            if (commandThermostat && !homeThermostats) {
                paragraph "<p style='color: red; font-weight: bold; font-size:100%'>No AC thermostats have been selected on the 'Energy Consumers' page.</p>"
            }
           input "maxTemperature", "number", required: false, defaultValue: 83, title: "Maximum Temperature the Demand Manager is permitted to raise the thermostat temperature to:"
        }

        section("") {
            hrefMenuPage (pagePrecoolSettings, "Pre-cool Settings", "Pre-cool your home before your peak period begins...", activityIcon, null)
            hrefMenuPage (pageAdvancedThermostatCommandSettings, "Advanced Thermostat Command Settings", "Additional options & overrides...", activityIcon, null)
        }
    }
}

def pageAdvancedSettings() {
    dynamicPage(name: "pageAdvancedSettings", title: "Advanced Settings (General)", install: false, uninstall: false) {
      section("") {
            input "logLevel", "enum", required: false, title: "Log level (default: info)", defaultValue: "info", options: ["none", "trace", "debug", "info", "warn", "error"]
      }
      section ("Perform consumption device refreshes during peak periods prior to checking usage data. This may improve usage data accuracy, but could result in network congestion. " +
         "Currently applies to selected Home Power Meter and Thermostat devices.") {
            input "refreshDevices", "bool", required: false, title: "Refresh Devices during peak periods"
       }
   }
}

def pagePrecoolSettings() {
    dynamicPage(name: "pagePrecoolSettings", title: "Pre-cool Settings", install: false, uninstall: false) {
        section("Precool Home to a chosen temperature and return home temperature back when the peak period ends. When used in conjunction with Demand Manager thermostat commanding, " +
            "the pre-cool start time is typically 30 minutes or more before your peak period begins, and the pre-cool return time will typically be the same time " +
            "that your peak period ends. Note: You may wish to  program your smart thermostat to perform this function itself locally instead of using the precool functions here. " +
            "Pre-cooling currently only applies to the first thermostat chosen if multiple thermostats are selected.") {
            input "precoolHome", "bool", required: false, defaultValue: false, title: "Precool"
            input "precoolStartTime", "time", required: false, title: "Pre-cool start time"
            input "precoolStopTime", "time", required: false, title: "Pre-cool return time"
            input "precoolStartTemperature", "number", required: false, title: "Temperature to pre-cool to (°F)"
            input "precoolStopTemperature", "number", required: false, title: "Temperature to return to at pre-cool return time (°F)"
        }
    }
}

def pageAdvancedThermostatCommandSettings() {
    dynamicPage(name: "pageAdvancedThermostatCommandSettings", title: "Advanced Thermostat Command Settings", install: false, uninstall: false) {
        section() {
            input "minMinutesBetweenThermostatCommands", "enum", required: false, title: "Minimum time allowed between thermostat commands (default: 3 minutes)", defaultValue: "3",
               options: ["1": "1 Minute", "2":"2 Minutes", "3":"3 Minutes","4":"4 Minutes","5":"5 Minutes","7":"7 Minutes","10":"10 Minutes"]
            input "thermoHysteresisBumpF", "number", required: false, defaultValue: 2, title: "Thermostat hysteresis bump in Fahrenheit (default 2)"
            input "thermoHysteresisBumpSeconds", "number", required: false, defaultValue: 15, title: "Thermostat hysteresis bump return time in seconds (default 15)"
        }
        section("Return the thermostat back to pre-defined temperature setpoint after completing each (30 or 60 minute) cycle. Note: This will likely cause issues if TRS is enabled. " +
            "This may also result in inefficient and frequent air conditioner cycles with typical environments. It is recommended that this option is only used when demand goal " +
            "overage conditions are rarely expected, such as in mild/temperate environments or when supplemental home generators or batteries (eg PowerWalls) are being utilized") {
            input "returnSetPointAfterCycle", "bool", required: false, defaultValue: false, title: "Return temperature after each cycle"
            input "returnCycleSetPoint", "number", required: false, title: "Temperature to return to after each (30 or 60 minute) cycle (°F)"
        }
        section("Thermostat Temperature Rise Scheduling - TRS (Beta): If enabled, TRS adds additional logic to the default (more aggressive) thermostat shutoff behavior in response to demand events." + 
            "TRS attempts to maintain home temperature comfort, while also considering your goal demand preference setting and limiting short air conditioner duty cycles. When enabled, the demand manager will" +
            " attempt to maintain a slowly increasing cooling temperature setpoint from 1 PM to 8 PM. This may result in demands slightly exceeding your demand goal if your goal is aggressive. This requires that the home is pre-cooled to be effective.") {
            input "thermoRiseSchedulingPlan", "enum", required: false, defaultValue: "Off", title: "Choose your TRS temperature rise goal:",
                options: ["Off": "Off: Do not perform Temperature Rise Scheduling",
                    "Goal78F": "78°F by 8PM (suggested precool to at least 74°/75°)",
                    "Goal79F": "79°F by 8PM (suggested precool to at least 75°/76°)",
                    "Goal80F": "80°F by 8PM (suggested precool to at least 76°/77°)",
                    "Goal81F": "81°F by 8PM (suggested precool to at least 77°/78°)",
                ]
        }        
    }
}

def installed() {
    log.debug("${app.label} installed.")
    runIn (1, initialize)
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def updated() {
   logger ("${app.label} updated","debug")
   runIn (10, initialize)
}

def dashboardDeviceId() {
   def deviceIdStr = null
   if (atomicState.dashboardDeviceId) {
      deviceIdStr = atomicState.dashboardDeviceId
   } else {
       def deviceId = getChildDevice("dashboardDevice")
       if (deviceId) {
           deviceIdStr = "dashboardDevice"
       } else {
           deviceIdStr = "dashboardDevice" + app.id.toString()
       }
       atomicState.dashboardDeviceId = deviceIdStr
   }
   return deviceIdStr 
}

String mode() {
    return settings.operationMode ?: "fullControl"
}

Integer goalDemandW() {
    return settings.goalDemandInWatts != null ? settings.goalDemandInWatts.toInteger() : 3000
}

Integer nominalWatts() {
    return settings.nomUsageWatts != null ? settings.nomUsageWatts.toInteger() : 1000
}

Integer airConditionerWatts() {
    return settings.airCondWatts != null ? settings.airCondWatts.toInteger() : 5000
}

Integer cycleTimeMinutes() {
    Integer cycleTimeMins
    if (settings.cycleTime == "30 minutes") {
        cycleTimeMins = 30
    } else {
        cycleTimeMins = 60
    }
    return cycleTimeMins
}

Integer maximumAllowedTemperature() {
    return settings.maxTemperature ?: 83
}

Integer secondssBetweenThermostatCommands() {
    return settings.minMinutesBetweenThermostatCommands ? settings.minMinutesBetweenThermostatCommands.toInteger() * 60 : 180
}

def initialize() {
    logger ("initializing Demand Manager","debug")
   
    if (getChildDevice(dashboardDeviceId()) == null) {
        log.debug "adding virtual active peak period switch" 
        def child = addChildDevice("darwinsden", "Demand Manager Dashboard", dashboardDeviceId(), null, [name: "dashboardDevice", label: "Demand Manager Device", completedSetup: true])
        def dashboardDevice = getChildDevice(dashboardDeviceId())
        if (dashboardDevice) {
            dashboardDevice.off()
        }
    }
    if (installVirtualDemandMeters?.toBoolean()) {
        if (getChildDevice("demandPeakCurrent") == null) {
            log.debug "adding current peak meter"
            def child = addChildDevice("darwinsden", "Demand Manager Virtual Energy Meter", "demandPeakCurrent", null, [name: "demandPeakCurrent", label: "Demand-Current", completedSetup: true])
        }
        if (getChildDevice("demandPeakProjected") == null) {
            log.debug "adding projected peak meter"
            def child = addChildDevice("darwinsden", "Demand Manager Virtual Energy Meter", "demandPeakProjected", null, [name: "demandPeakProjected", label: "Demand-Projected", completedSetup: true])
        }
        if (getChildDevice("demandPeakToday") == null) {
            log.debug "adding today peak meter"
            def child = addChildDevice("darwinsden", "Demand Manager Virtual Energy Meter", "demandPeakToday", null, [name: "demandPeakToday", label: "Demand-Peak Today", completedSetup: true])
        }
        if (getChildDevice("demandPeakMonth") == null) {
            log.debug "adding month peak meter"
            def child = addChildDevice("darwinsden", "Demand Manager Virtual Energy Meter", "demandPeakMonth", null, [name: "demandPeakMonth", label: "Demand-Peak This Month", completedSetup: true])
        }
    }
    def dashboardDevice = getChildDevice(dashboardDeviceId())
    if (dashboardDevice) {
        dashboardDevice.setGoalDemand(goalDemandW())
        dashboardDevice.setMode(mode())
        dashboardDevice.setCycleMinutes(cycleTimeMinutes())
    }
    unsubscribe()
    unschedule()
    runIn(1,setSchedules)
}

Boolean hubIsSt() { 
    return (getHubType() == "SmartThings") 
}

private getHubType() {
    def hubType = "SmartThings"
    if(atomicState.hubType == null) {
        try { 
             include 'asynchttp_v1'
           } 
           catch (e) { 
              hubType = "Hubitat"  
            }
        atomicState.hubType = hubType
    }
    return atomicState.hubType
}

String versionDetails () {
    String vers = app.version() 
    if (newerVersionExists(atomicState.latestStableVersion, app.version())) {
        String latestVersion
        if (!hubIsSt()) {
            latestVersion = "<a href='${ddUrl}'>${atomicState.latestStableVersion}</a>"
        } else {
            latestVersion = atomicState.latestStableVersion
        }
        vers = vers + " (${latestVersion} is available)"
    }
    return vers
}
        
String stripVerPrefix(String ver) {
    if (ver && (ver.substring(0,1) == 'v' || ver.substring(0,1) == 'V')) {
        ver = ver.substring(1,ver.size() - 1)
    }
    return ver
}
       
Boolean newerVersionExists(String latest, String current) {
    Boolean isNewer = false
    if (latest && current) {
        List latV = stripVerPrefix(latest).tokenize('.')
        List curV = stripVerPrefix(current).tokenize('.')
        if (latV.size() >= 3 && curV.size() >= 3) {
            isNewer = !(curV[0] >= latV[0] && curV[1] >= latV[1] && curV[2] >= latV[2]) 
        }
    }
    return isNewer
}
    
def versionCb (resp, callData) {
    if (resp.status == 200) {
        if (resp.getJson().latestStableVersion) {
            atomicState.latestStableVersion = resp.getJson().latestStableVersion
            if (newerVersionExists(atomicState.latestStableVersion, app.version())) {
                if (!atomicState.newVerLogged) {
                   logger ("${app.label} new version ${atomicState.latestStableVersion} is available.","info")
                   atomicState.newVerLogged = true
                }
            } else {
                atomicState.newVerLogged = false
            }
        }
    }
}

def versionCheck() {
    atomicState.latestStableVersion = null
    httpAsyncGet('versionCb',versionUrl,null,null)
}

def setSchedules() {
    logger ("Setting schedules","debug")
    atomicState.lastThrottleRunTime = now()
    runEvery1Minute(throttleEvents)
    runEvery5Minutes(processWatchDog)
    runEvery1Hour(processWatchDog)
    runEvery1Hour(confirmDisplayIndications)
    subscribeDevices()
    schedulePrecooling()
    schedulePeakTimes()
    schedule(new Date(), versionCheck)
}

//Hubitat compatibility
private timeOfDayIsBetween(fromDate, toDate, checkDate, timeZone)     {
     return (!checkDate.before(toDateTime(fromDate)) && !checkDate.after(toDateTime(toDate)))
}

def startPeakScheduleNow (startTime, stopTime) {
    def onPeakNow = timeOfDayIsBetween(startTime, stopTime, new Date(), location.timeZone)
    return onPeakNow && !atomicState.nowInPeakUtilityPeriod?.toBoolean()
}
   
def schedulePeakTimes() {
    if (schedule1IsActive?.toBoolean()) {
        if (schedule1StartTime && schedule1StopTime) {
            schedule(schedule1StartTime.toString(), startPeak1Schedule)
            schedule(schedule1StopTime.toString(), stopPeak1Schedule)
            if (startPeakScheduleNow(schedule1StartTime, schedule1StopTime)) {
                startPeak1Schedule()
            }
        } else {
            String message = "Schedule 1 enabled in preferences, but start and/or stop time was not specified. Peak schedule 1 could not be set."
            sendNotificationMessage(message, "anomaly")
        }
    }
    if (schedule2IsActive?.toBoolean()) {
        if (schedule2StartTime && schedule2StopTime) {
            schedule(schedule2StartTime.toString(), startPeak2Schedule)
            schedule(schedule2StopTime.toString(), stopPeak2Schedule)
            if (startPeakScheduleNow(schedule2StartTime, schedule2StopTime)) {
                startPeak2Schedule()
            }
        } else {
            String message = "Schedule 2 enabled in preferences, but start and/or stop time was not specified. Peak schedule 2 could not be set."
            sendNotificationMessage(message, "anomaly")
        }
    }
    if (schedule3IsActive?.toBoolean()) {
        if (schedule3StartTime && schedule3StopTime) {
            schedule(schedule3StartTime.toString(), startPeak3Schedule)
            schedule(schedule3StopTime.toString(), stopPeak3Schedule)
            if (startPeakScheduleNow(schedule3StartTime, schedule3StopTime)) {
                startPeak3Schedule()
            }
        } else {
            String message = "Schedule 3 enabled in preferences, but start and/or stop time was not specified. Peak schedule 3 could not be set."
            sendNotificationMessage(message, "anomaly")
        }
    }
}

def schedulePrecooling() {
    if (precoolHome?.toBoolean()) {
        if (precoolStartTime && precoolStartTemperature && precoolStopTemperature &&
            precoolStartTemperature <= maximumAllowedTemperature() && precoolStopTemperature <= maximumAllowedTemperature()) {
            if (precoolStopTime) {
                logger ("subscribing to precool start (${precoolStartTime.toString()}) and stop times (${precoolStopTime.toString()})","debug")
                schedule(precoolStartTime.toString(), precoolingStart)
                schedule(precoolStopTime.toString(), precoolingStop)
            } else {
                sendNotificationMessage("Pre-cooling is enabled, but no stop/return time was specified. Pre-cooling was not scheduled.", "anomaly")
            }
        } else {
            sendNotificationMessage("Pre-cooling is enabled, but no start time was specified or temperatures are outside of maximum set in preferences. Pre-cooling was not scheduled.", "anomaly")
        }
    }
}

def weekend() {
    def df = new java.text.SimpleDateFormat("EEEE")
    // Ensure the new date object is set to local time zone
    df.setTimeZone(location.timeZone)
    def day = df.format(new Date())
    //log.debug "Today is: ${day}"
    return (day == "Saturday" || day == "Sunday")
}

def getTheMonth() {
    def mf = new java.text.SimpleDateFormat("MMMM")
    mf.setTimeZone(location.timeZone)
    def month = mf.format(new Date())
    //log.debug "Month is: ${month}"
    return month
}

def peakPeriodOnActions() {
    if (mode() == "fullControl") {
        if (devicesToTurnOffDuringPeak?.size()) {
           devicesToTurnOffDuringPeak.off()
        }
        if (devicesToTurnOffOnlyDuringPeak?.size()) {
           devicesToTurnOffOnlyDuringPeak.off()
        }
    }
    atomicState.processNewCycleThermo = false //new cycle thermo controls should only be applied after a new/reset cycle in an existing demand period
    atomicState.thermostatIdToReturn = 0
    atomicState.thermostatSetLastCycle = 0
    atomicState.thermostatToControlThisCycle = 0
    sendNotificationMessage("Entering utility peak period.", "demandGeneral")
    runIn(1, immediateEvent)
}

def peakPeriodOffActions() {
    if (mode() == "fullControl") {
        if (devicesToTurnOffDuringPeak?.size()) {
           devicesToTurnOffDuringPeak.on()
        }
    }
    sendNotificationMessage("Ending utility peak period.", "demandGeneral")
    runIn(1, immediateEvent)
}

def peakDemandOnActions() {
    if (mode() == "fullControl") {
        if (deviceToTurnOffDuringPeakDemand?.size()) {
           deviceToTurnOffDuringPeakDemand.off()
        }
        if (deviceToTurnOffOnlyDuringPeakDemand?.size()) {
           deviceToTurnOffOnlyDuringPeakDemand.off()
        }
    }
}

def peakDemandOffActions() {
    if (mode() == "fullControl") {
        if (deviceToTurnOffDuringPeakDemand?.size()) {
           deviceToTurnOffDuringPeakDemand.on()
        }
    }
}

def turnOnPeakPeriod() {
    def dashboardDevice = getChildDevice(dashboardDeviceId())
    if (dashboardDevice) {
        dashboardDevice.on()
    } else {
        log.error "Can't turn on peak period switch. Switch not found"
    }
    runIn (2, peakPeriodOnActions)
    logger ("Starting peak demand period","debug")
}

def turnOffPeakPeriod() {
    def dashboardDevice = getChildDevice(dashboardDeviceId())
    if (dashboardDevice) {
        dashboardDevice.off()
    } else {
        log.error "Can't turn off peak period switch. Switch not found"
    }
    runIn(2,peakPeriodOffActions)
    logger ("Ending peak demand period","debug")
}

def startPeak1Schedule() {
    def month = getTheMonth()
    if (!weekend() && (!monthsSchedule1 || monthsSchedule1.contains(month))) {
        atomicState.peak1ScheduleActive = true
        turnOnPeakPeriod()
    }
}

def stopPeak1Schedule() {
    def month = getTheMonth()
    if (!weekend() && (!monthsSchedule1 || monthsSchedule1.contains(month))) {
        atomicState.peak1ScheduleActive = false
        turnOffPeakPeriod()
    }
}

def startPeak2Schedule() {
    def month = getTheMonth()
    if (!weekend() && (!monthsSchedule2 || monthsSchedule2.contains(month))) {
        atomicState.peak2ScheduleActive = true
        turnOnPeakPeriod()
    }
}

def stopPeak2Schedule() {
    def month = getTheMonth()
    if (!weekend() && (!monthsSchedule2 || monthsSchedule2.contains(month))) {
        atomicState.peak2ScheduleActive = false
        turnOffPeakPeriod()
    }
}

def startPeak3Schedule() {
    def month = getTheMonth()
    if (!weekend() && (!monthsSchedule3 || monthsSchedule3.contains(month))) {
        atomicState.peak3ScheduleActive = true
        turnOnPeakPeriod()
    }
}

def stopPeak3Schedule() {
    def month = getTheMonth()
    if (!monthsSchedule3 || monthsSchedule3.contains(month)) {
        atomicState.peak3ScheduleActive = false
        turnOffPeakPeriod()
    }
}

def precoolingStart() {
    def thermostat = homeThermostats.get(0)
    if (mode() == "fullControl" && precoolHome?.toBoolean() && atomicState.todayIsPeakUtilityDay.toBoolean()) {
        if (precoolStartTime && precoolStartTemperature) {
            if (precoolStopTime && precoolStopTemperature) {
                atomicState.temperaturePriorToPrecool = thermostat.currentValue("coolingSetpoint")
                if (thermostat.currentValue("coolingSetpoint") > precoolStartTemperature) {
                    logger ("commanding thermostat to precool","debug")
                    atomicState.lastThermostatCommandTime = now()
                    atomicState.processingThermostatCommand = true
                    runIn(30, commandThermostatHandler, [data: [coolingSetpoint: precoolStartTemperature, whichThermostat: thermostat.deviceNetworkId]])
                    sendNotificationMessage("Pre-cooling home to ${precoolStartTemperature} °F.", "thermostat")
                } else {                    
                    sendNotificationMessage("Pre-cooling is not required. Current setpoint of ${thermostat.currentValue("coolingSetpoint")} is already " +               
                        "at or below ${precoolStartTemperature} °F.", "thermostat")
                }
            } else {
                sendNotificationMessage("Pre-cooling is enabled, but no stop/return time. Pre-cooling was not started.", "anomaly")
            }
        } else {
            sendNotificationMessage("Pre-cooling is enabled, but no start time was specified. Pre-cooling was not started.", "anomaly")
        }
    }
    processWatchDog()
}

def precoolingStop() {
    def thermostat = homeThermostats.get(0)
    if (mode() == "fullControl" && precoolHome?.toBoolean() && precoolStopTemperature && atomicState.todayIsPeakUtilityDay) {
        logger ("commanding thermostat to stop precool","debug")
        atomicState.lastThermostatCommandTime = now()
        atomicState.processingThermostatCommand = true
        runIn(30, commandThermostatHandler, [data: [coolingSetpoint: precoolStopTemperature, whichThermostat: thermostat.deviceNetworkId]])
        def precoolStopNotes = ""
        if (atomicState.temperaturePriorToPrecool != precoolStopTemperature) {
            precoolStopNotes = " Note: Setpoint was ${atomicState.temperaturePriorToPrecool} prior to pre-cool."
        }
        sendNotificationMessage("Precooling period complete. Returning thermostat to ${precoolStopTemperature}°F." + precoolStopNotes, "thermostat")
    }
    processWatchDog()
}

def logDebugHandler(data) {
    log.debug(data.message)
}

def logDebug(msg) {
    if (logLevel == "debug" | logLevel == "trace") {
        //thread out logs, as these appear to be occasionally causing ST to exceed max duration for scheduled events
        if (atomicState.lastDebugTime == null) {
            atomicState.lastDebugTime = now()
        }
        if (atomicState.lastLogDelaySeconds == null) {
          atomicState.lastLogDelaySeconds = 1
        }
        //make sure the logger handler runIn calls are spaced out; overwrite: false apparently doesn't work well in ST if the runIn calls are scheduled for the same second
        def secondsSinceLastLog = (now() - atomicState.lastDebugTime)/1000
        Integer delayTime = (atomicState.lastLogDelaySeconds - secondsSinceLastLog + 3).toInteger()
        if (delayTime  < 1 ) {
           delayTime = 1
        } else { 
           if (delayTime > 10) {
            delayTime = 10
          }
        }
        atomicState.lastLogDelaySeconds = delayTime
        atomicState.lastDebugTime = now()
        runIn(delayTime, logDebugHandler, [data: [message: msg], overwrite: false]) 
    }
}

private sendNotificationMessage(message, msgType) {
    Boolean sendNotification = false
    Boolean warning = msgType == "anomaly"
    if (mode() != "monitorOnly") {
        if (msgType == "anomaly" && notifyWhenAnomalies?.toBoolean()) {
            sendNotification = true
        } else if (msgType == "thermostat" && (notifyWhenThermostatControlled?.toBoolean())) {
            sendNotification = true
        } else if (msgType == "demandExceeded" && (notifyWhenCycleDemandExceeded?.toBoolean())) {
            sendNotification = true
        } else if (msgType == "demandMonth" && (notifyWhenMonthlyDemandExceeded?.toBoolean())) {
            sendNotification = true
        } else if (msgType == "demandGeneral" && (notifyWithGeneralDemandStatus?.toBoolean())) {
            sendNotification = true
        } else if (msgType == "any") {
            sendNotification = true
        }
        if (sendNotification) {
            Boolean sendPushMessage = (notificationMethod && (notificationMethod.toString() == "push" || notificationMethod.toString() == "text and push"))
            Boolean sendTextMessage = (notificationMethod && (notificationMethod.toString() == "text" || notificationMethod.toString() == "text and push"))
            if (sendTextMessage) {
                if (phoneNumber) {
                    sendSmsMessage(phoneNumber.toString(), message)
                }
            }
            if (sendPushMessage) {
                if (hubIsSt()) { 
                    sendPush(message)
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
    }
    if (warning) {
        logger(message,"warn")
    } else {
        logger(message,"debug")
    }
    def dashboardDevice = getChildDevice(dashboardDeviceId())
    if (dashboardDevice) {
        dashboardDevice.setMessage(message)
    }
}

def subscribeDevices() {
    def dashboardDevice = getChildDevice(dashboardDeviceId())
    logger ("subscribing to Devices","debug")
    subscribe(homeThermostats, "thermostatOperatingState", immediateEvent)
    subscribe(homeThermostats, "coolingSetpoint", immediateEvent)
    subscribe(homeThermostats, "temperature", throttledEvent)
    subscribe(powerGenerator1, "power", immediateEvent)
    subscribe(wholeHomePowerMeter, "power", throttledEvent)
    subscribe(dashboardDevice, "switch.on", peakPeriodSwitchEvent)
    subscribe(dashboardDevice, "switch.off", peakPeriodSwitchEvent)
}

def peakPeriodSwitchEvent(evt) {
    //log.debug "Throttled Event Received: ${evt.device} ${evt.name} ${evt.value}"
    def dashboardDevice = getChildDevice(dashboardDeviceId())
    if (dashboardDevice?.currentValue("switch") == "on") {
          runIn(2,peakPeriodOnActions)
    } else {
          runIn(2,peakPeriodOffActions)
    }
    process()
}

def throttledEvent(evt) {
    //log.debug "Throttled Event Received: ${evt.device} ${evt.name} ${evt.value}"
    throttleEvents()
}  

def immediateEvent(evt) {
    //log.debug "Immediate Event Received: ${evt.device} ${evt.name} ${evt.value} "
    atomicState.lastThrottleRunTime = now()
    process()
}

def throttleEvents() {
    if (atomicState.lastThrottleRunTime == null) {
        atomicState.lastThrottleRunTime = 0
    }
    def secondsSinceLastRun = (now() - atomicState.lastThrottleRunTime) / 1000
    if (secondsSinceLastRun > 40) {
        atomicState.lastThrottleRunTime = now()
        runIn (1, checkDynamicPricingTriggers)
        runIn (1, refreshDevicesDuringPeak)
        process()
    }
}

private httpAsyncGet (handlerMethod, String url, String path, query=null) {
    try {
        def requestParameters = [uri: url, path: path, query: query, contentType: 'application/json']
        if(hubIsSt()) {
            include 'asynchttp_v1'
            asynchttp_v1.get(handlerMethod, requestParameters)
            log.debug "requesting: ${requestParameters}"
        } else { 
            asynchttpGet(handlerMethod, requestParameters)
        }
    } 
    catch (e) {
       log.error "Http Get failed: ${e}"
    }
}

private httpAsyncPost (handlerMethod, Map body = [:], String url, String path) {
    try {
        logger ("Async post to: ${path}","trace")
        def requestParameters = [uri: url, path: path, body: body, contentType: 'application/json']
        if(hubIsSt()) {
            include 'asynchttp_v1'
            asynchttp_v1.post(handlerMethod, requestParameters)
        } else { 
            asynchttpPost(handlerMethod, requestParameters)
        }
    } 
    catch (e) {
       logger ("Http Post failed: ${e}","error")
    }
}

def checkForPriceThresholdTrigger (def price, def threshold, def source) {
    if (price && threshold != null) {
       if (!atomicState.nowInPeakUtilityPeriod?.toBoolean()) {
           if (price >= threshold.toFloat()) {
              sendNotificationMessage("${source} pricing of ${price} cents/kwH exceeded trigger threshold of ${threshold.toFloat()} cents/kwH. Entering peak period", "demandGeneral")
              turnOnPeakPeriod()
           }
       } else {
           if (price < threshold.toFloat()) {
              sendNotificationMessage("${source} Pricing of ${price} cents/kwH dropped below trigger threshold of ${threshold.toFloat()} cents/kwH. Ending peak period", "demandGeneral")
              turnOffPeakPeriod()
           }
       }
   }
}

def processGriddyResponse(response, callData) {
    logger ("processing Griddy response","debug")
    logger ("${response.json}","trace")
    Float price = response.json.now.price_ckwh.toFloat()
    checkForPriceThresholdTrigger (price, griddyPeakPriceTrigger, 'Griddy')
    logger ("Griddy Hourly price is: ${price}","debug")
}

def processComEdResponse(response, callData) {
    logger ("processing ComEd response","debug")
    logger ("${response.json}","trace")
    Float price = response.json.price[0].toFloat()
    logger ("ComEd Hourly price is: ${price}","debug")
    checkForPriceThresholdTrigger (price, comEdPeakPriceTrigger, 'ComEd')
}

def checkDynamicPricingTriggers() {
    if (peakHoursFromComEdPricing?.toBoolean()){
        httpAsyncGet('processComEdResponse',"https://hourlypricing.comed.com","/api",[type:'currenthouraverage'])
    }
    if (peakHoursFromGriddyPricing?.toBoolean()){
        httpAsyncPost('processGriddyResponse',[settlement_point: griddyLoadZone.toString()], "https://app.gogriddy.com","/api/v1/insights/getnow")
    }
}

def refreshDevicesDuringPeak() {
   if (atomicState.nowInPeakUtilityPeriod?.toBoolean()) {
      // Refresh Home Meter if set in preferences
      if (wholeHomePowerMeter !=null && refreshDevices?.toBoolean()) {
          wholeHomePowerMeter.refresh()
      } 
      //Refresh thermostat if set in preferences & in demand relevant situation (attempt to minimize thermostat device processing)
      if (refreshDevices?.toBoolean() && homeThermostats != null && (atomicState.demandProjectedWatts > goalDemandW() * 0.9)) {
          homeThermostats.refresh()
      }
   }
}

def setUtilityPeriodGlobalStatus() {
    Boolean peakUsagePeriod
    Boolean peakUsageDay
    def dashboardDevice = getChildDevice(dashboardDeviceId())
    if (dashboardDevice && dashboardDevice.currentValue("switch") == "on") {
        peakUsagePeriod = true
        if (!atomicState.lastStateWasPeakUtilityPeriod) {
            atomicState.peakPeriodStartTime = now()
            atomicState.lastStateWasPeakUtilityPeriod = true
        }
    } else {
        peakUsagePeriod = false
        atomicState.lastStateWasPeakUtilityPeriod = false
    }
    if (!weekend()) {
        peakUsageDay = true
    } else {
        peakUsageDay = false
    }
    //*************************
    //* Set Utility Period Global Status
    //**************************
    atomicState.todayIsPeakUtilityDay = peakUsageDay
    atomicState.nowInPeakUtilityPeriod = peakUsagePeriod
}

def recordFinalCyclePeaks () {
    def dashboardDevice = getChildDevice(dashboardDeviceId())
    if (dashboardDevice) {
         Integer peakDemand = atomicState.lastRecordedPeakDemand ? atomicState.lastRecordedPeakDemand.toInteger() : 0
         if (peakDemand > 1) {
            def peakDemandToday = dashboardDevice.currentValue("peakDayDemand")
            if (!peakDemandToday || peakDemand > peakDemandToday) {
                logDebug("setting today's peak demand")
                dashboardDevice.setPeakDayDemand(peakDemand)
                def demandPeakToday = getChildDevice("demandPeakToday")
                if (demandPeakToday) {
                    demandPeakToday.setPower(peakDemand)
                }
            }
            def peakDemandThisMonth = dashboardDevice.currentValue("peakMonthDemand")
            if (!peakDemandThisMonth || peakDemand > peakDemandThisMonth) {
                 logDebug("setting this month's peak demand")
                 dashboardDevice.setPeakMonthDemand(peakDemand)
                 def demandPeakThisMonth = getChildDevice("demandPeakMonth")
                 if (demandPeakThisMonth) {
                     demandPeakThisMonth.setPower(peakDemand)
                 }
                 if (notifyWhenMonthlyDemandExceeded?.toBoolean() && peakDemand > exceededBuffer) {
                     sendNotificationMessage("New Peak Demand for ${getTheMonth()} is: ${peakDemand}W", "demandMonth")
                 }
            }
         }
     }
     atomicState.lastRecordedPeakDemand = 0
}

def setCycleStatus() {
    def secondsIntoThisCycle
    def secondsLeftInThisCycle
    def secondsInThisInterval
    if (atomicState.lastCycleCheckTime == null) {
        atomicState.lastCycleCheckTime = 0
    }
    def timeNow = new Date()
    Integer min = timeNow.format('m').toInteger()
    Integer sec = timeNow.format('s').toInteger()
    Integer millisec = timeNow.format('S').toInteger()

    if (min >= cycleTimeMinutes()) {
        secondsIntoThisCycle = (min - cycleTimeMinutes()) * 60 + sec + millisec / 1000.0
    } else {
        secondsIntoThisCycle = min * 60 + sec + millisec / 1000.0
    }
    secondsLeftInThisCycle = cycleTimeMinutes() * 60 - secondsIntoThisCycle
    def secondsSinceLastCheck = (now() - atomicState.lastCycleCheckTime) / 1000.0
    if (secondsSinceLastCheck > cycleTimeMinutes() * 60 || atomicState.lastMinute > min ||
        (atomicState.lastMinute < cycleTimeMinutes() && min >= cycleTimeMinutes())) {
        logDebug "New Demand Cycle"
        runIn (1, recordFinalCyclePeaks)
        atomicState.demandCurrentWatts = atomicState.demandCurrentWatts = Math.max(getCurrentHomePowerWatts() , 0)
        atomicState.cycleDemandNotificationSent = false
        def demandPeakCurrent = getChildDevice("demandPeakCurrent")
        if (atomicState.nowInPeakUtilityPeriod?.toBoolean()) {
            atomicState.processNewCycleThermo = true
            atomicState.thermostatToControlThisCycle = 0
        }
        if (demandPeakCurrent) {
            demandPeakCurrent.setPower(atomicState.demandCurrentWatts)
        }
        secondsInThisInterval = secondsIntoThisCycle
        if (atomicState.processedDemandOnActions?.toBoolean()) {
            runIn(1, peakDemandOffActions)
            atomicState.processedDemandOnActions = false
        }
    } else {
        secondsInThisInterval = secondsSinceLastCheck
    }
    if (secondsLeftInThisCycle < 60) {
        atomicState.secondsNextInterval = secondsLeftInThisCycle
    } else {
        atomicState.secondsNextInterval = 60
    }
    atomicState.lastMinute = min
    atomicState.lastCycleCheckTime = now()
    //log.debug "Into cycle: ${secondsIntoThisCycle}s. " + "Left in cycle: ${secondsLeftInThisCycle}s. Interval: ${secondsInThisInterval}s."
    //********************************
    //* Set Cycle Status Global Data
    //********************************    
    atomicState.secondsIntoThisDemandCycle = secondsIntoThisCycle
    atomicState.secondsLeftInThisDemandCycle = secondsLeftInThisCycle
    atomicState.deltaIntervalSeconds = secondsInThisInterval
}

def setPeakCurrentDevice(data) {
    def demandPeakCurrent = getChildDevice("demandPeakCurrent")
    if (demandPeakCurrent) {
        demandPeakCurrent.setPower(data.power)
    }
}

def setPeakProjectedDevice(data) {
    def demandPeakProjected = getChildDevice("demandPeakProjected")
    if (demandPeakProjected) {
        demandPeakProjected.setPower(data.power)
    }
}

def setDashboardCurrentDemand(data) {
    def dashboardDevice = getChildDevice(dashboardDeviceId())
    if (dashboardDevice) {
        dashboardDevice.setCurrentDemand(data.power)
    }
}

def setDashboardProjectedDemand(data) {
    def dashboardDevice = getChildDevice(dashboardDeviceId())
    if (dashboardDevice) {
        dashboardDevice.setProjectedDemand(data.power)
    }
}

def getProjectedPowerWithoutAc() {
  //reduce solar production estimate by 33% for conservative future production estimate
  return (nominalWatts() - getSolarPower() * 2 / 3).toInteger()
 } 

def getNumberOfActiveCoolingAcUnits()
{
   Integer numberOfActiveUnits = 0
   if (homeThermostats) {
     for (i in 0 .. homeThermostats.size() - 1) {
      def thermostat = homeThermostats.get(i)
      if (thermostat?.currentValue("thermostatOperatingState") == 'cooling') {   
         numberOfActiveUnits = numberOfActiveUnits + 1
      }
     }
   }
   return numberOfActiveUnits
}
  
Integer estimateHomePower()
{
  // Estimate based on thermostat operation + solar, and nominal home use
  def homePower = getProjectedPowerWithoutAc() + getNumberOfActiveCoolingAcUnits() * airConditionerWatts()
  logger ("estimated home power is: ${homePower.toInteger()}","debug")
  return homePower.toInteger()
}

def getCurrentHomePowerWatts() 
{
    Integer currentHomePowerWatts = 0
    if (wholeHomePowerMeter != null) {
        currentHomePowerWatts = wholeHomePowerMeter.currentValue("power")
    } else {
      // Estimate...
      currentHomePowerWatts = estimateHomePower().toInteger()
    }
    return currentHomePowerWatts.toInteger()
}

def calcCurrentAndProjectedDemand() {
    def demandCurrent
    def demandProjected
    def demandPeak
    if (atomicState.demandCurrentWatts == null || atomicState.secondsIntoThisDemandCycle == 0) {
           atomicState.demandCurrentWatts = Math.max(getCurentHomePowerWatts,0)
    } else {
           // current demand
           demandCurrent = ((1.0 * atomicState.demandCurrentWatts * (atomicState.secondsIntoThisDemandCycle - atomicState.deltaIntervalSeconds) +
               Math.max(getCurrentHomePowerWatts().toInteger(), 0) * atomicState.deltaIntervalSeconds) / atomicState.secondsIntoThisDemandCycle).toInteger()
    }
    // projected demand
    demandProjected = ((1.0 * demandCurrent * atomicState.secondsIntoThisDemandCycle + 1.0 * Math.max(getProjectedPowerWithoutAc(), 0) *
        atomicState.secondsLeftInThisDemandCycle) / (cycleTimeMinutes() * 60.0)).toInteger()
    def demandPeakCurrent = getChildDevice("demandPeakCurrent")
    if (demandPeakCurrent) {
        runIn(1, setPeakCurrentDevice, [data: [power: demandCurrent]])
    }
    def dashboardDevice = getChildDevice(dashboardDeviceId())
    if (dashboardDevice) {
        runIn(1, setDashboardCurrentDemand, [data: [power: demandCurrent]])
        runIn(1, setDashboardProjectedDemand, [data: [power: demandProjected]])
    }
    def demandPeakProjected = getChildDevice("demandPeakProjected")
    if (demandPeakProjected) {
        runIn(1, setPeakProjectedDevice, [data: [power: demandProjected]])
    }
    logDebug("Projected Demand: ${demandProjected}W. Current Demand: ${demandCurrent}W.")
    //***********************************************
    //* Set Current and Projected Demand Global Data
    //***********************************************   
    atomicState.demandCurrentWatts = demandCurrent
    atomicState.demandProjectedWatts = demandProjected
}

def recordPeakDemands() {
    Integer day = new Date().format('DD', location.timeZone).toInteger()
    Integer month = new Date().format('MM', location.timeZone).toInteger()
    Integer projectedDemand = atomicState.demandProjectedWatts.toInteger()

    if (!atomicState.lastDay || atomicState.lastDay != day) {
        atomicState.lastDay = day
        def demandPeakToday = getChildDevice("demandPeakToday")
        if (demandPeakToday) {
            demandPeakToday.setPower(0)
        }
        def dashboardDevice = getChildDevice(dashboardDeviceId())
        if (dashboardDevice) {
            dashboardDevice.setPeakDayDemand(0)
        }
    }
    if (!atomicState.lastMonth || atomicState.lastMonth != month) {
        atomicState.lastMonth = month
        def demandPeakThisMonth = getChildDevice("demandPeakMonth")
        if (demandPeakThisMonth) {
            demandPeakThisMonth.setPower(0)
        }
        def dashboardDevice = getChildDevice(dashboardDeviceId())
        if (dashboardDevice) {
            dashboardDevice.setPeakMonthDemand(0)
        }
    }
    if (atomicState.nowInPeakUtilityPeriod?.toBoolean() && (now() - atomicState.peakPeriodStartTime > 3 * 60 * 1000)) {
        Integer exceededBuffer = notifyWhenDemandExceededBuffer ?: 0
        if (projectedDemand > goalDemandW() + exceededBuffer) {
            if (!atomicState.cycleDemandNotificationSent || !atomicState.cycleDemandNotificationSent.toBoolean()) {
                atomicState.cycleDemandNotificationSent = true
                sendNotificationMessage("Projected ${projectedDemand}W cycle demand is estimated to exceed ${goalDemandW()}W goal.", "demandExceeded")
            }
        }

        if (projectedDemand > goalDemandW() &&
            (!atomicState.processedDemandOnActions || !atomicState.processedDemandOnActions.toBoolean())) {
            runIn(1, peakDemandOnActions)
            atomicState.processedDemandOnActions = true
        }
        if (atomicState.secondsLeftInThisDemandCycle?.toInteger() < 600) {
           atomicState.lastRecordedPeakDemand = projectedDemand
        }
    }
}

def getSmartThermoWeightedDeparture(currentSetPoint) {
    def timeNow = new Date().format('HH', location.timeZone)
    def timeNow2 = new Date()
    Integer min = timeNow2.format('m').toInteger()
    def hour24 = timeNow.toInteger() + min / 60
    def weightedTemperatureDeparture
    if (thermoRiseSchedulingPlan && thermoRiseSchedulingPlan.toString() != "Off" && hour24 > 13 && hour24 < 20) {
        Integer scheduledTemperature
        switch (thermoRiseSchedulingPlan.toString()) {
            case "Goal78F":
                scheduledTemperature = 59 + hour24
                break
            case "Goal79F":
                scheduledTemperature = 60 + hour24
                break
            case "Goal80F":
                scheduledTemperature = 61 + hour24
                break
            case "Goal81F":
                scheduledTemperature = 62 + hour24
                break
            default:
                logger ("Unexpected Rise Schedule Plan: ${thermoRiseSchedulingPlan.toString()}","warn")
                scheduledTemperature = 61 + hour24
                break
        }
        def timeIntoCycle = hour24 - 12
        def temperatureScheduleDeparture = currentSetPoint - scheduledTemperature
        // The temperature delta between the scheduled and actual setpoint is of higher importance the later it is in the demand period
        // Apply a higher weighting the later it is in the demand period
        def timeWeight = 0.23 * timeIntoCycle
        weightedTemperatureDeparture = timeWeight * temperatureScheduleDeparture
        logDebug "Weighted Temperature Departure: ${(weightedTemperatureDeparture*100).toInteger()/100.0}F. " +
            " current setpoint: ${currentSetPoint}F. Scheduled Temperature: ${(scheduledTemperature*100).toInteger()/100.0}F. " +
            " Time Weight: ${(timeWeight*100).toInteger()/100.0}."
        // adjust our goal Watts for this cycle depending on whether we're ahead or behind the plan temperature schedule   
    } else {
        weightedTemperatureDeparture = 0
    }
    return weightedTemperatureDeparture
}

String getTrsStatusString(setPoint) {
    String statusString = ""
    if (thermoRiseSchedulingPlan && thermoRiseSchedulingPlan.toString() != "Off") {
        def weightedTempDeparture = getSmartThermoWeightedDeparture(setPoint)
        if (weightedTempDeparture > -1.0 && weightedTempDeparture < 1.0) {
            statusString = " TRS is on plan."
        } else {
            if (weightedTempDeparture < -1.0) {
                statusString = " TRS is ahead of plan."
            } else if (weightedTempDeparture > 1.0) {
                statusString = " TRS is behind plan."
            }
        }
    }
    return statusString
}
     
def turnOffThermostat(data) {
    def thermostat = getThermostat(data.whichThermostat)
    logDebug "*******  Turn Off " + thermostat.displayName + " *********"
    def currentSetPoint = thermostat.currentValue("coolingSetpoint")
    def setPointToCommand = currentSetPoint
    Integer tempBumpDegrees = thermoHysteresisBumpF ?: 2
    if (thermoRiseSchedulingPlan && thermoRiseSchedulingPlan.toString() != "Off" && getSmartThermoWeightedDeparture(currentSetPoint) < -1) {
        setPointToCommand = setPointToCommand + 1
        logDebug "Ahead of thermostat schedule. Upping " + thermostat.displayName + " commanded setpoint by 1 to: ${setPointToCommand}"
    }
    if ((setPointToCommand < thermostat.currentValue("temperature")) &&
        (currentSetPoint <= 82)) {
        setPointToCommand = setPointToCommand + 1
        tempBumpDegrees = tempBumpDegrees + 1
        logDebug "Current temperature is above planned setpoint value. Upping " + thermostat.displayName + " commanded setpoint by 1 to: ${setPointToCommand}"
    }

    if (setPointToCommand <= maximumAllowedTemperature()) {
        if (setPointToCommand == currentSetPoint) {
            sendNotificationMessage("Briefly adjusting " + thermostat.displayName + " thermostat & returning to ${setPointToCommand}F to halt AC. " + getTrsStatusString(setPointToCommand), "thermostat")
        } else {
            sendNotificationMessage("Raising " + thermostat.displayName + " thermost from ${currentSetPoint} to ${setPointToCommand}F to manage demand." + getTrsStatusString(setPointToCommand), "thermostat")
        }
        atomicState.lastThermostatCommandTime = now()
        atomicState.processingThermostatCommand = true
        commandThermostatWithBump(setPointToCommand, tempBumpDegrees, thermostat)
    } else {
        logDebug "Unexpected condition: " + thermostat.displayName + " set point to command of ${setPointToCommand} is above max allowed: ${maximumAllowedTemperature()}. Cannot turn off thermostat."
    }

}

def turnOnThermostat(data) {
    logDebug "*******  Crank 'Er Up!... *********"
    def thermostat = getThermostat(data.whichThermostat)
    def setPointToCommand = thermostat.currentValue("coolingSetpoint")
    Integer tempBumpDegrees = -3
    atomicState.processingThermostatCommand = true
    atomicState.lastThermostatCommandTime = now()
    commandThermostatWithBump(setPointToCommand, tempBumpDegrees, thermostat)
    sendNotificationMessage("Briefly initiating " + thermostat.displayName + " cooling to ensure home will remain comfortable per TRS preference." + getTrsStatusString(setPointToCommand), "thermostat")
}

def getTrsAdjustedTargetDemand(thermostat) {
    def trsAdjustedTargetDemand
    if (getThermostat(deviceId).currentValue("coolingSetpoint") != "") {
        try {
            def goalAdjustmentWatts = getSmartThermoWeightedDeparture(thermostat.currentValue("coolingSetpoint")) * 200
            trsAdjustedTargetDemand = goalDemandW() + goalAdjustmentWatts
            if (demandPeakToday && trsAdjustedTargetDemand < demandPeakToday.currentValue("power")) {
                if (demandPeakToday.currentValue("power") < goalDemandW()) {
                    trsAdjustedTargetDemand = demandPeakToday.currentValue("power")
                } else {
                    trsAdjustedTargetDemand = goalDemandW()
                }
            }
        } catch (Exception e) {
            logger ("exception in getTrsAdjustedTargetDemand: ${e}","warn")
            sendNotificationMessage("Issue processing temperature rise scheduling.", "anomaly")
            trsAdjustedTargetDemand = goalDemandW()
        }
    } else {
        trsAdjustedTargetDemand = goalDemandW()
    }
    return trsAdjustedTargetDemand
}

def returnThermostatSetPoint() {
  if (atomicState.thermostatIdToReturn  != 0) {
        def thermostatToReturn = getThermostat(atomicState.thermostatIdToReturn)
        atomicState.thermostatIdToReturn = 0
        if (returnSetPointAfterCycle?.toBoolean() && returnCycleSetPoint?.toInteger() > 70 &&
                         returnCycleSetPoint.toInteger() <= maximumAllowedTemperature() && 
                             returnCycleSetPoint.toInteger() != thermostatToReturn.currentValue("coolingSetpoint")) {  
           sendNotificationMessage("Returning " + thermostatToReturn.displayName + " to ${returnCycleSetPoint}F at start of new demand cycle.", "thermostat")
           atomicState.lastThermostatCommandTime = now()
           atomicState.processingThermostatCommand = true
           runIn(15, commandThermostatHandler, [data: [coolingSetpoint: returnCycleSetPoint, whichThermostat: thermostatToReturn.deviceNetworkId]])
        }
   }
}
 
def incrementThermostat(startingThermo) {
     def deviceId 
     if (homeThermostats) {
        if (homeThermostats.size() < 2 || startingThermo == null || startingThermo == 0 || 
            homeThermostats.get(homeThermostats.size()-1).deviceNetworkId == startingThermo) {
           deviceId = homeThermostats.get(0).deviceNetworkId 
        } else {
           for (i in 0 .. homeThermostats.size() - 2) {
              if (homeThermostats.get(i).deviceNetworkId == startingThermo)  {
                 deviceId = homeThermostats.get(i + 1).deviceNetworkId   
              }
           }
        }
     }
     return deviceId
}
  
def getThermostat (thermostat) {
    def deviceId
    if (homeThermostats) {
       deviceId = homeThermostats.get(0)
       homeThermostats.each { object ->
          if (object.deviceNetworkId == thermostat)  {
            deviceId = object 
          }
       }
    }
    return deviceId
}
 
def nextThermostatToControl() {
    // find a thermostat to control that's ideally currently cooling and is not the thermostat controlled in the last cycle. 
    def deviceId = incrementThermostat(atomicState.thermostatSetLastCycle)
    if (homeThermostats) {
       for (i in 1 .. homeThermostats.size()) {     
         if (getThermostat(deviceId).currentValue("thermostatOperatingState") == 'cooling') {
           //log.debug "found cooling: ${getThermostat(deviceId)}"
           exit
         } else {
           //log.debug "incrementing: ${getThermostat(deviceId)}"
           deviceId = incrementThermostat(deviceId)
         }
       }
    }
    return deviceId
}   
            
def thermostatControls() {
    def thermostat 
    if (atomicState.thermostatToControlThisCycle) {
       // Once a thermostat has been controlled for a cycle, stick with it.
       thermostat = getThermostat(atomicState.thermostatToControlThisCycle)
    } else {
       // find the best candidate next thermostat to control
       thermostat = getThermostat(nextThermostatToControl())
    }
    
    if (thermostat) {
        def demandToAttempt = getTrsAdjustedTargetDemand(thermostat)
        // reduce power generators by 1/3 as a conservative estimate since generation status may be lagging or may decrease in the future. 
        def demandWithoutAirConditioning = Math.max(getProjectedPowerWithoutAc(),0)
        def demandWithAirConditioning = demandWithoutAirConditioning + airConditionerWatts()*getNumberOfActiveCoolingAcUnits()
        def demandAtEndOfCycleIfAcContinues = (atomicState.demandCurrentWatts * atomicState.secondsIntoThisDemandCycle +
            demandWithAirConditioning * atomicState.secondsNextInterval +
            demandWithoutAirConditioning * (atomicState.secondsLeftInThisDemandCycle - atomicState.secondsNextInterval)) / (cycleTimeMinutes() * 60)
        //wattHoursToNextCycleWithAc: Power expected to be consumed before the demand cycle is over
        def wattHoursToNextCycleWithAc = atomicState.secondsLeftInThisDemandCycle * (demandWithAirConditioning) * 60 / cycleTimeMinutes() / 60 / 60
        def estimatedSecondsOfAcToPeak
        if (demandToAttempt - demandWithAirConditioning == 0) {
            estimatedSecondsOfAcToPeak = 100000
        } else {
            estimatedSecondsOfAcToPeak = atomicState.secondsIntoThisDemandCycle * (atomicState.demandCurrentWatts - demandToAttempt) /
                (demandToAttempt - demandWithAirConditioning)
        }
        Boolean thermostatIsBusy = atomicState.processingThermostatCommand && ((now() - atomicState.lastThermostatCommandTime) / 1000) < 210
 
        logDebug "Demand To Attempt: ${demandToAttempt.toInteger()}. Demand If AC Continues: ${demandAtEndOfCycleIfAcContinues.toInteger()}." +
            " WH To Next Cycle: ${wattHoursToNextCycleWithAc.toInteger()}. Seconds AC To Peak: ${estimatedSecondsOfAcToPeak.toInteger()}. " +
            thermostat.displayName + " AC State: ${thermostat.currentValue("thermostatOperatingState")}. Busy: ${thermostatIsBusy}."

        if (atomicState.nowInPeakUtilityPeriod?.toBoolean()) {
            if (atomicState.lastThermostatCommandTime == null) {
                atomicState.lastThermostatCommandTime = 0.toInteger()
            }
            Boolean allowedToCommandThermo = mode() == "fullControl" && commandThermostat?.toBoolean() &&
                now() - atomicState.peakPeriodStartTime > 2 * 60 * 1000 && atomicState.secondsIntoThisDemandCycle > 60
            //log.debug ("allowed: ${allowedToCommandThermo}")
            if (allowedToCommandThermo && !thermostatIsBusy && ((now() - atomicState.lastThermostatCommandTime) / 1000 > secondssBetweenThermostatCommands())) {
                if (atomicState.processNewCycleThermo?.toBoolean()) {
                    atomicState.processNewCycleThermo = false
                    //*************************
                    //check to see if thermostat should return to default setpoint at start of new cycle 
                    //*************************
                    runIn (1, returnThermostatSetPoint)
                } else {
                    if (thermostat.currentValue("thermostatOperatingState") == 'cooling') {
                         //log.debug ("cooling!")
                        Integer acWattsAllowedToContinue = 200
                        
                        Boolean demandWouldExceedGoal = demandAtEndOfCycleIfAcContinues >= demandToAttempt
                        Boolean wattsRemainingInCycleAreExcessive = wattHoursToNextCycleWithAc > acWattsAllowedToContinue 
                        Boolean demandOverageExcessive = demandAtEndOfCycleIfAcContinues > demandToAttempt + acWattsAllowedToContinue + 100
                        Boolean canRaiseTemp = thermostat.currentValue("coolingSetpoint") <= maximumAllowedTemperature()
                        
                        // Let the AC continue if demand goal won't exceeded by much and not much more energy is expected to be consumed before the demand cycle is over
                        // So..
                        // Shut down AC if the demand goal would exceeded by a lot -or-
                        // Shut down AC if demand goal would be exceeded, and a lot of power is still expected to be consumed before the demand cycle is over anyway
                        
                       
                        if (canRaiseTemp && ((demandWouldExceedGoal && wattsRemainingInCycleAreExcessive) || demandOverageExcessive)) {                       
                            //************************
                            //** Turn Off Thermostat
                            //************************
                            atomicState.lastThermostatCommandTime = now()
                            atomicState.processingThermostatCommand = true
                            atomicState.thermostatIdToReturn = thermostat.deviceNetworkId
                            atomicState.thermostatSetLastCycle = thermostat.deviceNetworkId
                            atomicState.thermostatToControlThisCycle = thermostat.deviceNetworkId
                            runIn(1, turnOffThermostat, [data: [whichThermostat: thermostat.deviceNetworkId]])
                        }
                    } else {
                        // Thermostat is not cooling, check if the AC should be turned on          
                        if (thermoRiseSchedulingPlan && thermoRiseSchedulingPlan.toString() != "Off" && 
                            getSmartThermoWeightedDeparture(thermostat.currentValue("temperature")) > 1 &&
                            estimatedSecondsOfAcToPeak > atomicState.secondsLeftInThisDemandCycle * 1.25 &&
                            thermostat.currentValue("coolingSetpoint") < thermostat.currentValue("temperature") &&
                            atomicState.secondsLeftInThisDemandCycle > 60) {
                            //************************
                            //** Turn On Thermostat
                            //************************
                            atomicState.lastThermostatCommandTime = now()
                            atomicState.processingThermostatCommand = true
                            runIn(1, turnOnThermostat, [data: [whichThermostat: thermostat.deviceNetworkId]])
                        }
                    }
                }
            }
        }
   }
}

def verifyThermostatCommandFinal(data) {
    def setPoint = data.coolingSetpoint
    def thermostat = getThermostat(data.whichThermostat)
    if (thermostat.currentValue("coolingSetpoint") != setPoint) {
        logger ("Cooling setpoint was not set to ${data.coolingSetpoint} as commanded (currently: ${thermostat.currentValue("coolingSetpoint")}). Giving Up...","warn")
        sendNotificationMessage("Warning: " + thermostat.displayName + " cooling setpoint ${data.coolingSetpoint} could not be verified.", "anomaly")
    } else {
        logger ("Confirmed " + thermostat.displayName + " cooling setpoint ${data.coolingSetpoint} is set after two tries.","debug")
    }
    atomicState.processingThermostatCommand = false
}

def verifyThermostatCommand(data) {
    def setPoint = data.coolingSetpoint
    def thermostat = getThermostat(data.whichThermostat)
    if (thermostat.currentValue("coolingSetpoint") != setPoint) {
        logger ("Cooling setpoint was not set to ${data.coolingSetpoint} as commanded (currently: ${thermostat.currentValue("coolingSetpoint")}). Retrying...","info")
        atomicState.lastThermostatCommandTime = now()
        atomicState.processingThermostatCommand = true
        try {
            thermostat.setCoolingSetpoint(setPoint)
        } catch (Exception e) {
            logger ("exception setting " + thermostat.displayName + " setpoint: ${e}","warn")
            sendNotificationMessage("Warning: thermostat exception in verify when attempting to set " + thermostat.displayName + " cooling setpoint to: ${setPoint}. Exception is: ${e}", "anomaly")
            throw e
        }
        runIn(45, verifyThermostatCommandFinal, [data: [coolingSetpoint: setPoint, whichThermostat: thermostat.deviceNetworkId]])
    } else {
        logger ("Confirmed " + thermostat.displayName + " cooling setpoint ${data.coolingSetpoint} is set.","debug")
        atomicState.processingThermostatCommand = false
    }
}

def commandThermostatHandler(data) {
    def setPoint = data.coolingSetpoint
    def thermostat = getThermostat(data.whichThermostat)
    logDebug "Setting " + thermostat.displayName + " thermostat to ${setPoint}F degrees."
    atomicState.lastThermostatCommandTime = now()
    atomicState.processingThermostatCommand = true
    try {
        thermostat.setCoolingSetpoint(setPoint)
    } catch (Exception e) {
        logger ("exception setting setpoint: ${e}","warn")
        sendNotificationMessage("Warning: thermostat exception in handler when attempting to set " + thermostat.displayName + " cooling setpoint to: ${setPoint}. Exception is: ${e}", "anomaly")
        runIn(45, verifyThermostatCommand, [data: [coolingSetpoint: setPoint, whichThermostat: thermostat.deviceNetworkId]])
    }
    runIn(45, verifyThermostatCommand, [data: [coolingSetpoint: setPoint, whichThermostat: thermostat.deviceNetworkId]])
}

def commandThermostatWithBump(setPoint, degreesBump, thermostat) {
    if (setPoint > maximumAllowedTemperature()) {
        logger ("Unexpected condition: " + thermostat.displayName + " set point to command ${setPoint} is above max allowed: ${maximumAllowedTemperature()}. Cannot command thermostat.","warn")
        return
    }
    logDebug "Setting " + thermostat.displayName + " thermostat to ${setPoint}F with bump of ${degreesBump} (${(setPoint + degreesBump)}F)."
    atomicState.lastThermostatCommandTime = now()
    try {
        thermostat.setCoolingSetpoint(setPoint + degreesBump)
    } catch (Exception e) {
        logger ("exception setting setpoint with bump: ${e}","warn")
        sendNotificationMessage("Warning: thermostat exception in bump handler when attempting to set " + thermostat.displayName + 
              " cooling setpoint to: ${setPoint}. Exception is: ${e}", "anomaly")
    }
    atomicState.processingThermostatCommand = true
    // command final setpoint 
    Integer bumpReturnDelaySeconds = thermoHysteresisBumpSeconds ?: 15
    runIn(bumpReturnDelaySeconds, commandThermostatHandler, [data: [coolingSetpoint: setPoint, whichThermostat: thermostat.deviceNetworkId]])
}

def toggleColorIndicatorHandler(data) {
    Boolean stateIsOn = data.stateOn.toBoolean()
    if (stateIsOn) {
        logDebug("turning on peak indicator light!")
        colorIndicatorDevices.on()
    } else {
        logDebug("turning off peak indicator light!")
        colorIndicatorDevices.off()
    }
}

def colorIndicatorHandler() {
    def red = [level: 0, saturation: 0, hex: "#f0000"]
    def green = [level: 0, saturation: 0, hex: "#00FF00"]
    Boolean nowInPeakUtilityPeriod = atomicState.nowInPeakUtilityPeriod?.toBoolean()
    def color
    if (nowInPeakUtilityPeriod) {
        color = red
    } else {
        color = green
    }
    colorIndicatorDevices.setColor(color)
    atomicState.lastPeakDisplayStateOn = nowInPeakUtilityPeriod
}

def checkDeprecated() {
   if (!atomicState?.deprecatedDevicesLogged & (WD200Dimmer1 || WD200Dimmer2 || colorIndicatorDevice1 || colorIndicatorDevice2 ||
         deviceToTurnOffDuringPeak1 || deviceToTurnOffDuringPeak2 || deviceToTurnOffDuringPeakDemand1 || deviceToTurnOffDuringPeakDemand2)) {
       atomicState.deprecatedDevicesLogged = true
       sendPush ("Demand Manager smart app requires re-entry of display devices and peak period on/off devices in smart app preferences due to software update") 
   }
    if (!atomicState.deprecatedThermostatLogged & !(homeThermostat == null)) {
       atomicState.deprecatedThermostatLogged = true
       sendPush ("Demand Manager smart app requires re-entry of thermostat devices in smart app preferences due to software update adding multiple thermostat support") 
   }
}

Boolean solarMeterIsPowerwall() {
   Boolean isPowerwall = false
   if (powerGenerator1 != null) {
      def theAtts = powerGenerator1.supportedAttributes
      theAtts.each {att ->
           //log.debug "Supported Attribute: ${att.name}" 
           if (att.name == "solarPower") {
              isPowerwall = true
           }
      }
   }
   //log.debug "solar meter is a powerwall: ${isPowerwall}"
   return isPowerwall
}
     
def getSolarPower ()
{
   def solarPower = 0
   if (powerGenerator1 != null) {
      if (solarMeterIsPowerwall()) {
          solarPower = powerGenerator1.currentValue("solarPower")
          //log.debug "power from pW is: ${solarPower}"
      } else {
         solarPower = powerGenerator1.currentValue("power")
         //log.debug "power from Inverter is: ${solarPower}"
      }
   }
   return solarPower
}
      
def setIndicatorDevices() {
    Boolean nowInPeakUtilityPeriod = atomicState.nowInPeakUtilityPeriod?.toBoolean()
    runIn (10, checkDeprecated)
    if (colorIndicatorDevices?.size()) {
        def stateChanged = false
        if (atomicState.lastPeakDisplayStateOn == null) {
            atomicState.lastPeakDisplayStateOn = !nowInPeakUtilityPeriod
        }
        if ((nowInPeakUtilityPeriod && !atomicState.lastPeakDisplayStateOn?.toBoolean()) ||
            (!nowInPeakUtilityPeriod && atomicState.lastPeakDisplayStateOn?.toBoolean())) {
            //log.debug "state changed!"
            runIn(1, colorIndicatorHandler)
            Boolean displayOffIndicator = alwaysDisplayOffPeakIndicator?.toBoolean()
            if (!nowInPeakUtilityPeriod && !displayOffIndicator) {
                runIn(20, toggleColorIndicatorHandler, [data: [stateOn: false]])
            }
        }
    }
    
    if (wD200Dimmers?.size()) {
        Integer level = 1;
        Integer blinkDuration = 0
        Integer blink = 0;
        Integer color = ledGreen;
        def scaleWattsPerLed
        if (solarSystemSizeWatts) {
            scaleWattsPerLed = solarSystemSizeWatts.toDouble() / 6
        } else {
            scaleWattsPerLed = 1000
        }
        if (powerGenerator1) {
            def solarPower = getSolarPower()
            level = ((solarPower + 500) / scaleWattsPerLed + 1).toInteger()
            if (level > 7) {
                level = 7
            } else {
                if (level < 1) {
                    level = 1
                }
            }
        }
        if (nowInPeakUtilityPeriod) {
            if (level == 1) {
                color = ledRed
            } else {
                color = ledYellow
            }
        }
        if (nowInPeakUtilityPeriod && atomicState.demandProjectedWatts > goalDemandW() * 0.8) {
            blinkDuration = 1000
            color = ledRed
            blink = 1
            if (atomicState.demandProjectedWatts > goalDemandW() * 1.1) {
                blinkDuration = 400
            }
            if (blinkDuration != atomicState.lastBlinkDuration) {
               logDebug "setting led blink duration to: ${blinkDuration}"
               runIn(3, wd200LedBlinkHandler, [data: [duration: blinkDuration]])
            }
        }
        if (color != atomicState.lastLedColor || level != atomicState.lastLedLevel || blink != atomicState.lastLedBlink) {
            runIn(1, setWD200Leds, [data: [ledLevel: level, ledColor: color, ledBlink: blink]])
        }
    }
}

def wd200LedBlinkHandler(data) {
    def blinkDuration = data.duration
    if (wD200Dimmers) {
		wD200Dimmers.each {						
            if (it.hasCommand("setBlinkDurationMilliseconds")) {
                it.setBlinkDurationMilliseconds(blinkDuration)
            } else if (it.hasCommand("setBlinkDurationMS")) {
                it.setBlinkDurationMS(blinkDuration)
            }
         }
    }
    atomicState.lastBlinkDuration = blinkDuration
}

def setWD200LED(led, data) {
    Integer color = data.ledColor
    Integer blink = data.ledBlink
    if (led > data.ledLevel) {
        color = 0
        blink = 0
    }
    if (wD200Dimmers) {
		wD200Dimmers.each {						
            if (it.hasCommand("setStatusLed")) {
                it.setStatusLed(led, color, blink)
            } else if (it.hasCommand("setStatusLED")) {
                it.setStatusLED(led.toString(), color.toString(), blink.toString())
            }
         }
    }
}

def setWD200Led7(dataIn) {
    setWD200LED(7, dataIn)
    atomicState.lastLedLevel = dataIn.ledLevel
    atomicState.lastLedColor = dataIn.ledColor
    atomicState.lastLedBlink = dataIn.ledBlink
}

def setWD200Led6(dataIn) {
    setWD200LED(6, dataIn)
    runIn(1, setWD200Led7, [data: dataIn])
}

def setWD200Led5(dataIn) {
    setWD200LED(5, dataIn)
    runIn(1, setWD200Led6, [data: dataIn])
}

def setWD200Led4(dataIn) {
    setWD200LED(4, dataIn)
    runIn(1, setWD200Led5, [data: dataIn])
}

def setWD200Led3(dataIn) {
    setWD200LED(3, dataIn)
    runIn(1, setWD200Led4, [data: dataIn])
}

def setWD200Led2(dataIn) {
    setWD200LED(2, dataIn)
    runIn(1, setWD200Led3, [data: dataIn])
}

def setWD200Leds(dataIn) {
    setWD200LED(1, dataIn)
    runIn(1, setWD200Led2, [data: dataIn])
    logDebug "Setting dimmer LEDs: color: ${dataIn.ledColor} blink: ${dataIn.ledBlink} level: ${dataIn.ledLevel}"
}

def confirmDisplayIndications() {
    //Periodically ensure display indicator devices are correctly set
    //trigger update of WD200 display devices on next planned processing cycle
    atomicState.lastBlinkDuration = -1
    atomicState.lastLedLevel = -1
    if (colorIndicatorDevices?.size()) {
       def displayOffIndicator = alwaysDisplayOffPeakIndicator?.toBoolean()
       def nowInPeakUtilityPeriod = atomicState.nowInPeakUtilityPeriod?.toBoolean()
       if (nowInPeakUtilityPeriod | displayOffIndicator) {
           // Do not reconfirm color of indicator if it should be off, since setting the color can turn it back on
           runIn(5, colorIndicatorHandler)
       } else if (!nowInPeakUtilityPeriod && !displayOffIndicator) {
           // ensure it is off
           runIn(5, toggleColorIndicatorHandler, [data: [stateOn: false]])
       }
    }
    //why not check the watchDog while we're here:
    processWatchDog()
}

def rescheduleAllEvents() {
    initialize()
}

def processWatchDog() {
    if (!atomicState.lastProcessedTime) {
        atomicState.lastProcessedTime = now()
    }
    if (!atomicState.lastProcessCompletedTime) {
        atomicState.lastProcessCompletedTime = now()
    }
    def secondsSinceLastProcessed = (now() - atomicState.lastProcessedTime) / 1000
    def secondsSinceLastProcessCompleted = (now() - atomicState.lastProcessCompletedTime) / 1000

    if (secondsSinceLastProcessed > 290) {
        if (!atomicState?.processedWarningSent) {
           sendNotificationMessage("Warning: Demand Manager has not executed in ${(secondsSinceLastProcessed/60).toInteger()} minutes. Reinitializing", "anomaly")
           atomicState?.processedWarningSent = true
       }
        runIn(30, rescheduleAllEvents)
    } else {
        if (secondsSinceLastProcessCompleted > 290) {
           if (!atomicState?.completedWarningSent) {
              sendNotificationMessage("Warning: Demand Manager has not successfully completed in ${(secondsSinceLastProcessCompleted/60).toInteger()} minutes. Reinitializing", "anomaly")
              atomicState.completedWarningSent = true
           }
           runIn(30, rescheduleAllEvents)
        } else {
          if (atomicState?.completedWarningSent || atomicState?.processedWarningSent) {
             sendNotificationMessage("Info: Demand Manager has successfully resumed operation","anomaly")
             atomicState.completedWarningSent = false 
             atomicState.processedWarningSent = false
          }      
        }
    }
}

def chainProcessing3() {
    calcCurrentAndProjectedDemand()
    //the following can run concurrently
    runIn(1, thermostatControls)
    runIn(2, recordPeakDemands)
    runIn(3, setIndicatorDevices)
    atomicState.lastProcessCompletedTime = now()
}

def chainProcessing2() {
    setCycleStatus()
    runIn(1, chainProcessing3)
}

def chainProcessing1() {
    setUtilityPeriodGlobalStatus()
    runIn(1, chainProcessing2)
}
    
def process() {
    runIn(1, processWatchDog)
    atomicState.lastProcessedTime = now()
    runIn (2, chainProcessing1)
}

void logger (String message, String msgLevel="debug") {
    Integer prefLevelInt = settings.logLevel != null ? logLevels[settings.logLevel] : 3
    Integer msgLevelInt = logLevels[msgLevel]
    if (msgLevelInt >= prefLevelInt && prefLevelInt) {
        log."${msgLevel}" message
    } else if (!msgLevelInt) {
        log.info "${message} logged with invalid level: ${msgLevel}"
    }
}

def hrefMenuPage (String page, String titleStr, String descStr, String image, params, state = null) {
    if (hubIsSt()) {
        href page, title: titleStr, description: descStr, required: false, image: image
    } else {
        String imgFloat = ""
        String imgElement = ""
        if (descStr) {imgFloat = "float: left;"} //Center title} if no description
        if (image) {imgElement = "<img src='${image}' width='40' style='${imgFloat} width: 40px; padding: 0 16px 0 0'>"}
        String titleDiv = imgElement + titleStr
        String descDiv = "<div style='float :left; width: 90%'>" + descStr + "</div>"
        href page, description: descDiv, title: titleDiv, required: false, params : params, state : state
    }
}

// Constants
@Field static final Map logLevels = ["none":0, "trace":1,"debug":2,"info":3, "warn":4,"error":5]
@Field static final String ddUrl = "https://darwinsden.com/demand/"
@Field static final String versionUrl = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/metadata/demandManagerVersion.json"
//WD 200 LEDs
@Field static final Integer ledRed = 1
@Field static final Integer ledGreen = 2
@Field static final Integer ledYellow = 5
// Icons
@Field static final String notifyIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/notification40.png"
@Field static final String batteryIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/battery40.png"
@Field static final String cogIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/cogD40.png"
@Field static final String controlsIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/controls.png"
@Field static final String dashIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/dashboard40.png"
@Field static final String schedIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/schedClock40.png"
@Field static final String schedOkIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/schedOk40.png"
@Field static final String schedEditIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/schedEdit40.png"
@Field static final String addIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/add40.png"
@Field static final String solarIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/solar.png"
@Field static final String eMeterIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/energyMeter.png"
@Field static final String schedIncomplIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/schedIncompl40.png"
@Field static final String ddLogoHubitat = "https://darwinsden.com/download/ddlogo-for-hubitat-demandManagerV3-png"
@Field static final String ddLogoSt = "https://darwinsden.com/download/ddlogo-for-st-demandManagerV3-png"
@Field static final String ppBtn = "https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif"
@Field static final String activityIcon = "http://cdn.device-icons.smartthings.com/secondary/activity@2x.png"
@Field static final String demandIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/meterColor100.png"
@Field static final String levelsIcon = "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/levels.png"