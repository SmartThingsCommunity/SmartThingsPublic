/**
 *  Demand Manager
 *
 *  Author: Darwin@DarwinsDen.com
 *  Copyright 2018, 2019 - All rights reserved
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
 *  the risks to people, pets, and personal property and agree to not hold the developer liable.
 *  
 *  This software was developed in the hopes that it will be useful to others, however, 
 *  it is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR GUARANTEES OF ANY KIND, either express or implied. 
 * 
 *  The end user is free to modify this software for personal use. Re-distribution of this software in its original or 
 *  modified form requires explicit written consent from the developer. 
 * 
 *  The developer retains all rights, title, and interest, including all copyright, patent rights, and trade secrets 
 *  associated with the algorithms and technologies used herein. 
 *
 */
 
def version() {
    return "v0.2.1e.20190705"
}
/*   
 *	05-Jul-2019 >>> v0.2.1e.20190705 - Added support for multiple peak control and display devices. Note: update requires these devices to be re-entered in app preferences. 
 *	03-Jul-2019 >>> v0.2.0e.20190703 - Added option to re-set set-point after each cycle. Resolve issue that could result in multiple thermostat commands. 
 *	04-Jun-2019 >>> v0.1.5e.20190604 - Fix issue with turning back on peak demand exceeded devices at start of new cycle. 
 *	30-May-2019 >>> v0.1.4e.20190530 - Resolve new install/init issue
 *	28-May-2019 >>> v0.1.3e.20190528 - Added option to persist off-peak indication display, improved threading, additional watchdog logic
 *	07-May-2019 >>> v0.1.2e.20190507 - Added additional exception handling and watchdog processes, logic updates
 */

def warning() {
    return "WARNING: By enabling these features, you are granting permission of this application to control your thermostat and other devices. This software is in the early beta stages. Normal operation and/or unexpected failures or defects in this software, dependent hardware, and/or network may result in unintended high or low temperatures and high utility usage & costs."
}

def release() {
    return "I understand the risks to people, pets, personal property, and utility usage and costs, and agree to not hold the developer liable for any related issues."
}

definition(
    name: "Demand Manager - Alpha", namespace: "darwinsden", author: "Darwin", description: "Control Demand Management.",
    category: "My Apps", iconUrl: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/meterColor.png",
    iconX2Url: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/meterColor.png"
)

preferences {
    page(name: "pageMain")
    page(name: "pageNominalData")
    page(name: "pageDisplayIndicators")
    page(name: "pageSettings")
    page(name: "pageRemove")
    page(name: "pageRelease")
    page(name: "pageThermostat")
    page(name: "pagePeakSchedule")
    page(name: "pagePeakSchedule1")
    page(name: "pagePeakSchedule2")
    page(name: "pagePeakSchedule3")
    page(name: "pagePeakDayHolidays")
    page(name: "pageNotifications")
    page(name: "pageAdvancedSettings")
    page(name: "pageDevicesToControl")
    page(name: "pagePrecoolSettings")
    page(name: "pageAdvancedThermostatCommandSettings")
}

private pageMain() {
    dynamicPage(name: "pageMain", title: "", install: true, uninstall: false) {
        section() {
          paragraph app.version() + "\n\nDarwinsDen.com/Demand", 
          title: "Demand Manager - Alpha", required: false, image: "https://darwinsden.com/download/ddlogo-for-demandmanager-0-png"
        }
        section("Required Information:") {
            input "goalDemandInWatts", "number", required: false, defaultValue: 3000, title: "Your Goal Demand Watts", image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/demand2-1.png"
            input "operationMode", "enum", required: true, title: "Operation Mode", options: ["monitorOnly": "MONITOR ONLY: Do not perform demand management actions",
                "notifyOnly": "NOTIFY: Monitor and send demand notifications", "fullControl": "FULL: Monitor, notify and manage devices & thermostat"
            ], image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/controls.png"
            input "cycleTime", "enum", options: ["30 minutes", "60 minutes"], required: true, title: "Demand Cycle Period", image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/timer.png"
            input "wholeHomePowerMeter", "capability.powerMeter", required: true, title: "Home Energy Meter", image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/energyMeter.png"
            href "pagePeakSchedule", title: "Peak Utility Schedules", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/calendar.png"
        }

        section("Choose how you get notified of demand events") {
            href "pageNotifications", title: "Notification preferences..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/notification.png"
        }
        section("Critical power generation and consumption devices. Setting devices here will help improve your projected demand estimates.") {
            input "homeThermostat", "capability.thermostat", required: false, title: "Thermostat"
            input "powerGenerator1", "capability.powerMeter", required: false, title: "Solar Inverter"
        }

        section("Tune your data. Refining estimates here will further improve your projected demand estimates.") {
            href "pageNominalData", title: "Enter your nominal home usage data...", description: "", required: false,
                image: "http://cdn.device-icons.smartthings.com/secondary/activity@2x.png"
        }

        section("Manage demand by controlling devices and your thermostat") {
            href "pageDevicesToControl", title: "Manage demand by turning off devices during peak periods..", description: "", required: false,
                image: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png"
            href "pageThermostat", title: "Manage demand by automatically adjusting your thermostat cooling setpoint..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/thermostat.png"
        }

        section("Monitor your demand and solar generation") {
            href "pageDisplayIndicators", title: "Choose display indicator devices..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/dashboard.png"
        }

        section("Advanced Settings (General)") {
            href "pageAdvancedSettings", title: "Advanced settings..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/cog.png"
        }

        section("About") {
            paragraph "Version: " + app.version() + "\nMemory used: " + memUsed(), title: "Demand Manager", required: false
        }
        section("For more information") {
          href(name: "Site", title: "For additional information, or to provide feedback, please visit: DarwinsDen.com/demand",
             description: "Tap open the demand manager web page on DarwinsDen.com",
             required: false,
             url: "https://darwinsden.com/demand/")
       }       
        section("Product Improvement") {
          href(name: "Survey", title: "Please help improve the Demand Manager smart app.",
             description: "Tap take the Demand Manager Survey",
             required: false,
             url: "https://www.surveymonkey.com/r/RDFFJQM")
       }
       section("Rename this smart app") {
            label name: "name", title: "Name", state: (name ? "complete" : null), defaultValue: app.name, required: false
        }
        section("Remove Demand Manager") {
            href "pageRemove", title: "", description: "Remove Demand Manager", required: false
        }
    }
}

def pageNominalData() {
    dynamicPage(name: "pageNominalData", title: "This information helps the demand management program predict usage needs into the future when estimating projected demands for each peak cycle",
        install: false, uninstall: false) {
        section("What is the estimated Watt usage of your air conditioner when it is in use? (default 5000W)... ") {
            input "airCondWatts", "number", required: false, defaultValue: 5000, title: "Estimated Air Conditioner Watts"
        }

        section("What is the estimated nominal Watt usage of your home when in normal use? This should not include your main air conditioner since that is accounted for by monitoring your thermostat, but should assume average typical occasional usage of your appliances such as microwave oven, televisions, etc. (default 1000W)...") {
            input "nomUsageWatts", "number", required: false, defaultValue: 1000, title: "Estimated Nominal Home Usage Watts"
        }
    }
}

def pagePeakSchedule() {
    dynamicPage(name: "pagePeakSchedule", title: "Enter utility peak hour schedule", install: false, uninstall: false) {
        section("Utility Peak Time Schedules. The program will run on weekdays for the hours chosen here unless the day is specified as a holiday below.") {
            href "pagePeakSchedule1", title: "Enter your Utility Peak Hour Schedule 1...", description: "", required: false,
                image: "http://cdn.device-icons.smartthings.com/secondary/activity@2x.png"
            href "pagePeakSchedule2", title: "Enter your Utility Peak Hour Schedule 2...", description: "", required: false,
                image: "http://cdn.device-icons.smartthings.com/secondary/activity@2x.png"
            href "pagePeakSchedule3", title: "Enter your Utility Peak Hour Schedule 3...", description: "", required: false,
                image: "http://cdn.device-icons.smartthings.com/secondary/activity@2x.png"
        }

        section("Utility Peak Day Holidays (Future Capability)") {
            href "pagePeakDayHolidays", title: "Enter holidays from peak utility periods here...", description: "", required: false,
                image: "http://cdn.device-icons.smartthings.com/secondary/activity@2x.png"
        }
    }
}

def pagePeakSchedule1() {
    dynamicPage(name: "pagePeakSchedule1", title: "Enter Your Utility Schedule 1 Peak Hours and Months.", install: false, uninstall: false) {
        section() {
            input "schedule1IsActive", "boolean", required: false, defaultValue: false, title: "Enable this schedule"
        }
        section("") {
            input "schedule1StartTime", "time", required: false, title: "Start Time (schedule 1)"
            input "schedule1StopTime", "time", required: false, title: "End Time (schedule 1)"
        }
        section("On Which Months") {
            input "monthsSchedule1", "enum", title: "Select which months the schedule applies", required: false, multiple: true,
                options: ["January": "January", "February": "February", "March": "March", "April": "April", "May": "May", "June": "June", "July": "July",
                    "August": "August", "September": "September", "October": "October", "November": "November", "December": "December"
                ]
        }
    }
}

def pagePeakSchedule2() {
    dynamicPage(name: "pagePeakSchedule2", title: "Enter Your Utility Schedule 2 Peak Hours and Months.", install: false, uninstall: false) {
        section() {
            input "schedule2IsActive", "boolean", required: false, defaultValue: false, title: "Enable this schedule"
        }
        section("") {
            input "schedule2StartTime", "time", required: false, title: "Start Time (schedule 2)"
            input "schedule2StopTime", "time", required: false, title: "End Time (schedule 2)"
        }
        section("On Which Months") {
            input "monthsSchedule2", "enum", title: "Select which months the schedule applies", required: false, multiple: true,
                options: ["January": "January", "February": "February", "March": "March", "April": "April", "May": "May", "June": "June", "July": "July",
                    "August": "August", "September": "September", "October": "October", "November": "November", "December": "December"
                ]
        }
    }
}

def pagePeakSchedule3() {
    dynamicPage(name: "pagePeakSchedule3", title: "Enter Your Utility Schedule 3 Peak Hours and Months.", install: false, uninstall: false) {
        section() {
            input "schedule3IsActive", "boolean", required: false, defaultValue: false, title: "Enable this schedule"
        }
        section("") {
            input "schedule3StartTime", "time", required: false, title: "Start Time (schedule 3)"
            input "schedule3StopTime", "time", required: false, title: "End Time (schedule 3)"
        }
        section("On Which Months") {
            input "monthsSchedule3", "enum", title: "Select which months the schedule applies", required: false, multiple: true,
                options: ["January": "January", "February": "February", "March": "March", "April": "April", "May": "May", "June": "June", "July": "July",
                    "August": "August", "September": "September", "October": "October", "November": "November", "December": "December"
                ]
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
		type:				"capability.indicator",
		title:				"Select your HomeSeer WD200+ dimmers",
		multiple:			true,
		required:			false
    ]
    
    dynamicPage(name: "pageDisplayIndicators", title: "Choose display indicator devices (optional)", install: false, uninstall: false) {
        section("Select a color indicator light (such as the EZ MultiPli/HomeSeer HSM200) to indicate when you're in a peak demand period. Note: " +
            "The indicator on green light will only be briefly displayed during off-peak unless the persist off-peak display is set below.") {
            input colorIndicatorDevices
            input "alwaysDisplayOffPeakIndicator", "boolean", required: false, defaultValue: false, title: "Persist off-peak indication display"
        }
        section("Select HomeSeer WD200+ dimmers to be used as demand warning indicators and solar inverter (if present) production level indicators. " +
            "The Demand Manager will use the colored LED's on the switch plates as graphing indicators (not the bulbs connected to the loads).") {
            input wD200Dimmers
            input "solarSystemSizeWatts", "number", required: false,
                default: 6000, title: "Size of your Solar System in Watts"
        }
        section("Install Virtual Demand Meters (Current, Projected, Peak Today, and Peak Month) for use with dashboards (ActionTiles, etc..). " +
            "These should be added to the dashboard smart app as Power Meters.") {
            input "installVirtualDemandMeters", "boolean", required: false, defaultValue: false, title: "Install Virtual Demand Meters"
        }
    }
}

def pageNotifications() {
    dynamicPage(name: "pageNotifications", title: "Notification Preferences", install: false, uninstall: false) {

        section("Send a notification if your demand goal was exceeded for any demand cycle (ie. 30 or 60 minute period).") {
            input "notifyWhenCycleDemandExceeded", "boolean", required: false, defaultValue: false, title: "Notify when cycle demand exceeded"
        }

        section("Send a notification if the monthly demand reaches a new high") {
            input "notifyWhenMonthlyDemandExceeded", "boolean", required: false, defaultValue: false, title: "Notify of new monthly high demand"
        }
        section("Send a notification for general demand status (demand cycle started/ended, etc..)") {
            input "notifyWithGeneralDemandStatus", "boolean", required: false, defaultValue: false, title: "Notify of general demand status"
        }
        section("Notify when thermostat is controlled - or when the Demand Manager recommends that the air conditioner should be turned off if thermostat control is disabled") {
            input "notifyWhenThermostatControlled", "boolean", required: false, defaultValue: false, title: "Notify of pending or recommended thermostat control"
        }

        section("Send a notification if anomalies are encountered in the Demand Manager") {
            input "notifyWhenAnomalies", "boolean", required: false, defaultValue: true, title: "Notify when anomalies are encountered"
        }

        section("Amount in Watts that the current projected cycle demand can exceed before a notification is sent (only applies when cycle demand notifications are on).") {
            input "notifyWhenDemandExceededBuffer", "number", required: false, title: "Cycle demand exceeded buffer (Watts)"
        }

        section("Notification method (push notifications are via ST app)") {
            input "notificationMethod", "enum", required: false, defaultValue: "push", title: "Notification Method", options: ["none", "text", "push", "text and push"]
        }

        section("Phone number for text messages") {
            input "phoneNumber", "phone", title: "Phone number for text messages", description: "Phone Number", required: false
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

    def devicesToTurnOffDuringPeak = [
		name:				"devicesToTurnOffDuringPeak",
		type:				"capability.switch",
		title:				"Devices that should be off during entire peak period",
		multiple:			true,
		required:			false
    ]

    def deviceToTurnOffDuringPeakDemand = [
		name:				"deviceToTurnOffDuringPeakDemand",
		type:				"capability.switch",
		title:				"Devices that should be off when demand goal is exceeded",
		multiple:			true,
		required:			false
    ]
    dynamicPage(name: "pageDevicesToControl", title: "Manage demand by turning off devices during peak periods",
        install: false, uninstall: false) {

        section("Enter devices that should be turned off during peak utility periods. Devices will be turned on again when the peak period ends... ") {
            input devicesToTurnOffDuringPeak
        }
        section("Enter devices that should be turned off when peak demand exceeds your goal demand during any 30 or 60 minute demand period. " +
            "Devices will be turned back on again at the beginning of the next 30 or 60 minute demand cycle... ") {
            input deviceToTurnOffDuringPeakDemand
        }
    }
}


def pageThermostat() {
    dynamicPage(name: "pageThermostat", title: "Manage your thermostat based on demand", install: false, uninstall: false) {
        section(warning()) {
            input "signedRelease", "enum", options: ["I Agree"], required: true, title: release()
        }
        section("Command Thermostat: Allow Demand Manager to control thermostat cooling set point to manage demand. " +
            "This must be turned on before any thermostat command controls below will be applied. By default, if this is turn on without Temperature Rise Scheduling (TRS) applied (see below Beta option), the demand manager will aggressively attempt to control the thermostat to limit demand and will increase the thermostat setpoint if the home temperature " +
            " increases higher than the setpoint. This can result in rapidly increasing home temperatures and/or relatively short air conditioner duty cycles. It is recommended that the home be pre-cooled " +
            "before peak utility hours if this option is set.") {
            input "commandThermostat", "boolean", required: false, defaultValue: false, title: "Command Thermostat"
        }

        section("Maximum thermostat set point temperature the program will reach. Once this set-point temperature is reached, the Demand Manager will no longer raise the thermostat temperature to manage demand.") {
            input "maxTemperature", "number", required: false, defaultValue: 83, title: "Maximum Temperature"
        }

        section("") {
            href "pagePrecoolSettings", title: "Pre-cool Settings", description: "Pre-cool your home before your peak period begins...", required: false,
                image: "http://cdn.device-icons.smartthings.com/secondary/activity@2x.png"
        }

        section("Conserve Energy: Allow air conditioner to continue through to the next demand cycle (ie 30 or 60 minute period) if it is close to the end of a demand cycle and demand overage will not be extreme. " +
            "Warning: May result in demand slightly exceeding your goal demand") {
            input "conserveAirConditioningEnergy", "boolean", required: false, defaultValue: false, title: "Conserve AC Energy"
        }

        section("Thermostat Temperature Rise Scheduling - TRS (Beta): attempts to compromise between your goal demand preference setting, maintaining home temperature comfort, and limiting short air conditioner duty cycles. When enabled, the demand manager will" +
            " attempt to maintain a slowly increasing cooling temperature setpoint from 1 PM to 8PM. This may result in demands slightly exceeding your demand goal if your goal is aggressive. This requires that the home is pre-cooled to be effective.") {
            input "thermoRiseSchedulingPlan", "enum", required: false, defaultValue: "Off", title: "Choose your TRS temperature rise goal:",
                options: ["Off": "Off: Do not perform Temperature Rise Scheduling",
                    "Goal78F": "78°F by 8PM (suggested precool to at least 74°/75°)",
                    "Goal79F": "79°F by 8PM (suggested precool to at least 75°/76°)",
                    "Goal80F": "80°F by 8PM (suggested precool to at least 76°/77°)",
                    "Goal81F": "81°F by 8PM (suggested precool to at least 77°/78°)",
                ]
        }
        section("") {
            href "pageAdvancedThermostatCommandSettings", title: "Advanced Thermostat Command Settings", description: "Additional options & overrides...", required: false,
                image: "http://cdn.device-icons.smartthings.com/secondary/activity@2x.png"
        }
    }
}

def pageAdvancedSettings() {
    dynamicPage(name: "pageAdvancedSettings", title: "Advanced Settings (General)", install: false, uninstall: false) {
        section("IDE Log Level (set log level in SmartThings IDE Live Logging Tob)") {
            input "logLevel", "enum", required: false, title: "IDE Log Level", options: ["none", "trace", "debug", "info", "warn"]
        }
    }
}

def pagePrecoolSettings() {
    dynamicPage(name: "pagePrecoolSettings", title: "Pre-cool Settings", install: false, uninstall: false) {
        section("Precool Home to a chosen temperature and return home temperature back when the peak period ends. When used in conjunction with Demand Manager thermostat commanding, " +
            "the pre-cool start time is typically 30 minutes or more before your peak period begins, and the pre-cool return time will typically be the same time " +
            "that your peak period ends. Note: You may wish to  program your smart thermostat to perform this function itself locally instead of using the precool functions here.") {
            input "precoolHome", "boolean", required: false, defaultValue: false, title: "Precool"
            input "precoolStartTime", "time", required: false, title: "Pre-cool start time"
            input "precoolStopTime", "time", required: false, title: "Pre-cool return time"
            input "precoolStartTemperature", "number", required: false, title: "Temperature to pre-cool to (°F)"
            input "precoolStopTemperature", "number", required: false, title: "Temperature to return to at pre-cool return time (°F)"
        }
    }
}

def pageAdvancedThermostatCommandSettings() {
    dynamicPage(name: "pageAdvancedThermostatCommandSettings", title: "Advanced Thermostat Command Settings", install: false, uninstall: false) {
        section("Return the thermostat back to pre-defined temperature setpoint after completing each (30 or 60 minute) cycle. Note: This will likely cause issues if TRS is enabled. " +
            "This may also result in inefficient and frequent air conditioner cycles with typical environments. It is recommended that this option is only used when demand goal " +
            "overage conditions are rarely expected, such as in mild/temperate environments or when supplemental home generators or batteries (eg PowerWalls) are being utilized") {
            input "returnSetPointAfterCycle", "boolean", required: false, defaultValue: false, title: "Return temperature after each cycle"
            input "returnCycleSetPoint", "number", required: false, title: "Temperature to return to after each (30 or 60 minute) cycle (°F)"
        }
        section("Minimum allowable time between thermostat commands. Default is 3 minutes") {
         input "minMinutesBetweenThermostatCommands", "enum", required: false, title: "Minimum time allowed between thermostat commands", defaultValue: "3",
          options: ["1": "1 Minute", "2":"2 Minutes", "3":"3 Minutes","4":"4 Minutes","5":"5 Minutes","7":"7 Minutes","10":"10 Minutes"]
        }
        
        
    }
}
def pageRemove() {
    dynamicPage(name: "pageRemove", title: "", install: false, uninstall: true) {
        section() {
            paragraph parent ? "CAUTION: You are about to remove the '${app.label}'. This action is irreversible. If you are sure you want to do this, please tap on the Remove button below." :
                "CAUTION: You are about to completely remove Demand Manager and all of its settings. This action is irreversible. If you are sure you want to do this, please tap on the Remove button below. " +
                "Note: If an error occurs during removal, you may need to first manually remove references to Demand Manager child devices from other smart apps if you have manually added them.",
                required: true, state: null
        }
    }
}

def installed() {
    log.debug("installed called")
    //initialize()
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
    log.debug("updated called")

    def defaultDemandGoalWatts = 3000
    def defaultNominalUsageWatts = 1000
    def defaultAirConditionerWatts = 5000

    if (goalDemandInWatts == null) {
        atomicState.goalDemandWatts = defaultDemandGoalWatts
    } else {
        atomicState.goalDemandWatts = goalDemandInWatts.toInteger()
    }

    if (nomUsageWatts == null) {
        atomicState.nominalUsageWatts = defaultNominalUsageWatts
    } else {
        atomicState.nominalUsageWatts = nomUsageWatts.toInteger()
    }

    if (airCondWatts == null) {
        atomicState.airConditionerWatts = defaultAirConditionerWatts
    } else {
        atomicState.airConditionerWatts = airCondWatts.toInteger()
    }

    if (maxTemperature) {
        atomicState.maximumAllowedTemperature = maxTemperature
    } else {
        atomicState.maximumAllowedTemperature = 83
    }

    if (cycleTime == "30 minutes") {
        atomicState.cycleTimeMinutes = 30
    } else {
        atomicState.cycleTimeMinutes = 60
    }
    log.debug "Cycle time is ${atomicState.cycleTimeMinutes}"
    
    if (minMinutesBetweenThermostatCommands == null) {
        atomicState.minSecsBetweenThermoCommands = 180. toInteger()
    } else {
        atomicState.minSecsBetweenThermoCommands = minMinutesBetweenThermostatCommands.toInteger()*60
    }
   
    if (getChildDevice("dashboardDevice") == null) {
        log.debug "adding virtual active peak period switch"
        def child = addChildDevice("darwinsden", "Demand Manager Dashboard", "dashboardDevice", null, [name: "dashboardDevice", label: "Demand Manager - Active Peak Period Switch", completedSetup: true])
        def dashboardDevice = getChildDevice("dashboardDevice")
        if (dashboardDevice) {
            dashboardDevice.off()
        }
    }
    if (installVirtualDemandMeters && installVirtualDemandMeters.toBoolean() == true) {
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
    def dashboardDevice = getChildDevice("dashboardDevice")
    if (dashboardDevice) {
        dashboardDevice.setGoalDemand(atomicState.goalDemandWatts)
        dashboardDevice.setMode(operationMode.toString())
        dashboardDevice.setCycleMinutes(atomicState.cycleTimeMinutes)
    }
    unsubscribe()
    unschedule()
    initialize()
}

def memUsed() {
    def numBytes = state.toString().length() + atomicState.toString().length()
    return "${numBytes} bytes (" + ((100.0 * numBytes / 100000.0).toInteger()).toString() + "%)"
}

def initialize() {
    log.debug "Initializing Demand Manager"
    atomicState.lastThrottleRunTime = now()
    atomicState.lastProcessCompletedTime = now()
    atomicState.lastProcessedTime = now()
    runEvery1Minute(throttleEvents)
    runEvery5Minutes(processWatchDog)
    runEvery1Hour(processWatchDog)
    runEvery1Hour(confirmDisplayIndications)
    subscribeDevices()
    schedulePrecooling()
    schedulePeakTimes()
}

def getSecondsIntoThisDay(def Date) {
    def hour = timeNow.format('h').toInteger()
    def min = timeNow.format('m').toInteger()
    def sec = timeNow.format('s').toInteger()
    def secondsIntoThisDay = hour * 3600 + min * 60 + sec
    return secondsIntoThisDay
}

def schedulePeakTimes() {
    if (schedule1IsActive && schedule1IsActive.toBoolean() == true) {
        if (schedule1StartTime && schedule1StopTime) {
            def onPeakNow = timeOfDayIsBetween(schedule1StartTime, schedule1StopTime, new Date(), location.timeZone)
            if (onPeakNow) {
                startPeak1Schedule()
            }
            schedule(schedule1StartTime.toString(), startPeak1Schedule)
            schedule(schedule1StopTime.toString(), stopPeak1Schedule)

        } else {
            message = "Schedule 1 enabled in preferences, but start and/or stop time was not specified. Peak schedule 1 could not be set."
            log.warn message
            sendNotificationMessage(message, "anomaly")
        }
    }
    if (schedule2IsActive && schedule2IsActive.toBoolean() == true) {
        if (schedule2StartTime && schedule2StopTime) {
            def onPeakNow = timeOfDayIsBetween(schedule2StartTime, schedule2StopTime, new Date(), location.timeZone)
            if (onPeakNow) {
                startPeak2Schedule()
            }
            schedule(schedule2StartTime.toString(), startPeak2Schedule)
            schedule(schedule2StopTime.toString(), stopPeak2Schedule)
        } else {
            message = "Schedule 2 enabled in preferences, but start and/or stop time was not specified. Peak schedule 2 could not be set."
            log.warn message
            sendNotificationMessage(message, "anomaly")
        }
    }
    if (schedule3IsActive && schedule3IsActive.toBoolean() == true) {
        if (schedule3StartTime && schedule3StopTime) {
            def onPeakNow = timeOfDayIsBetween(schedule3StartTime, schedule3StopTime, new Date(), location.timeZone)
            if (onPeakNow) {
                startPeak3Schedule()
            }
            schedule(schedule3StartTime.toString(), startPeak3Schedule)
            schedule(schedule3StopTime.toString(), stopPeak3Schedule)
        } else {
            message = "Schedule 3 enabled in preferences, but start and/or stop time was not specified. Peak schedule 3 could not be set."
            log.warn message
            sendNotificationMessage(message, "anomaly")
        }
    }
}

def schedulePrecooling() {
    if (precoolHome?.toBoolean()) {
        if (precoolStartTime && precoolStartTemperature && precoolStopTemperature &&
            precoolStartTemperature <= atomicState.maximumAllowedTemperature && precoolStopTemperature <= atomicState.maximumAllowedTemperature) {
            if (precoolStopTime) {
                log.debug "subscribing to precool start (${precoolStartTime.toString()}) and stop times (${precoolStopTime.toString()})"
                schedule(precoolStartTime.toString(), precoolingStart)
                schedule(precoolStopTime.toString(), precoolingStop)
            } else {
                log.warn "Not scheduling pre-cooling. No stop/return time has been set"
                sendNotificationMessage("Pre-cooling is enabled, but no stop/return time was specified. Pre-cooling was not scheduled.", "anomaly")
            }
        } else {
            log.warn "Not scheduling pre-cooling. No start time has been set or temperatures are outside of maximum set in preferences"
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
    if (operationMode && operationMode.toString() == "fullControl") {
        if (devicesToTurnOffDuringPeak?.size()) {
           devicesToTurnOffDuringPeak.off()
        }
    }
}

def peakPeriodOffActions() {
    if (operationMode && operationMode.toString() == "fullControl") {
        if (devicesToTurnOffDuringPeak?.size()) {
           devicesToTurnOffDuringPeak.on()
        }
    }
}

def peakDemandOnActions() {
    if (operationMode && operationMode.toString() == "fullControl") {
        if (devicesToTurnOffDuringPeakDemand?.size()) {
           devicesToTurnOffDuringPeakDemand.off()
        }
    }
}

def peakDemandOffActions() {
    if (operationMode && operationMode.toString() == "fullControl") {
        if (devicesToTurnOffDuringPeakDemand?.size()) {
           devicesToTurnOffDuringPeakDemand.on()
        }
    }
}

def turnOnPeakPeriod() {
    def dashboardDevice = getChildDevice("dashboardDevice")
    if (dashboardDevice) {
        if (!monitorOnly || monitorOnly.toBoolean() == false) {
            dashboardDevice.on()
            peakPeriodOnActions()
            atomicState.processNewCycleThermo = false //new cycle thermo controls should only be applied after a new/reset cycle in an existing demand period
            //sendNotificationMessage("now entering peak demand period", "demandGeneral")
        }
    } else {
        log.error "Can't turn on peak period switch. Switch not found"
    }
}

def turnOffPeakPeriod() {
    def dashboardDevice = getChildDevice("dashboardDevice")
    if (dashboardDevice) {
        if (!monitorOnly || monitorOnly.toBoolean() == false) {
            dashboardDevice.off()
            peakPeriodOffActions()
            logDebug "now ending peak demand period"
            //sendNotificationMessage("now ending peak demand period", "demandGeneral")
        }
    } else {
        log.error "Can't turn off peak period switch. Switch not found"
    }
}

def startPeak1Schedule() {
    def month = getTheMonth()
    if (!weekend() && (!monthsSchedule1 || monthsSchedule1.contains(month))) {
        atomicState.peak1ScheduleActive = true
        turnOnPeakPeriod()
        runIn(1, immediateEvent)
    }
}

def stopPeak1Schedule() {
    def month = getTheMonth()
    if (!monthsSchedule1 || monthsSchedule1.contains(month)) {
        atomicState.peak1ScheduleActive = false
        turnOffPeakPeriod()
        runIn(1, immediateEvent)
    }
}

def startPeak2Schedule() {
    def month = getTheMonth()
    if (!weekend() && (!monthsSchedule2 || monthsSchedule2.contains(month))) {
        atomicState.peak2ScheduleActive = true
        turnOnPeakPeriod()
        runIn(1, immediateEvent)
    }
}

def stopPeak2Schedule() {
    def month = getTheMonth()
    if (!monthsSchedule2 || monthsSchedule2.contains(month)) {
        atomicState.peak2ScheduleActive = false
        turnOffPeakPeriod()
        runIn(1, immediateEvent)
    }
}

def startPeak3Schedule() {
    def month = getTheMonth()
    if (!weekend() && (!monthsSchedule3 || monthsSchedule3.contains(month))) {
        atomicState.peak3ScheduleActive = true
        turnOnPeakPeriod()
        runIn(1, immediateEvent)
    }
}

def stopPeak3Schedule() {
    def month = getTheMonth()
    if (!monthsSchedule3 || monthsSchedule3.contains(month)) {
        atomicState.peak3ScheduleActive = false
        turnOffPeakPeriod()
        runIn(1, immediateEvent)
    }
}

def precoolingStart() {
    if (operationMode && operationMode.toString() == "fullControl" && precoolHome && precoolHome.toBoolean() == true &&
        atomicState.todayIsPeakUtilityDay) {
        if (precoolStartTime && precoolStartTemperature) {
            if (precoolStopTime && precoolStopTemperature) {
                if (homeThermostat.coolingSetpointState.integerValue > precoolStartTemperature) {
                    log.debug "commanding thermostat to precool"
                    atomicState.lastThermostatCommandTime = now()
                    atomicState.processingThermostatCommand = true
                    runIn(30, commandThermostatHandler, [data: [coolingSetpoint: precoolStartTemperature]])
                    sendNotificationMessage("Pre-cooling home to ${precoolStartTemperature} °F.", "thermostat")
                    atomicState.temperaturePriorToPrecool = homeThermostat.coolingSetpointState.integerValue
                } else {
                    sendNotificationMessage("Pre-cooling is not required. Current setpoint of ${coolingSetpointState.integerValue} is already " +
                        "at or below ${precoolStartTemperature} °F.", "thermostat")
                }
            } else {
                log.warn "Not performing pre-cooling. No stop/return time and/or temperature has been set"
                sendNotificationMessage("Pre-cooling is enabled, but no stop/return time. Pre-cooling was not started.", "anomaly")
            }
        } else {
            log.warn "Not performing pre-cooling. No start time and/or temperature was specified"
            sendNotificationMessage("Pre-cooling is enabled, but no start time was specified. Pre-cooling was not started.", "anomaly")
        }
    }
    processWatchDog()
}

def precoolingStop() {
    if (operationMode && operationMode.toString() == "fullControl" && precoolHome && precoolHome.toBoolean() == true && precoolStopTemperature && atomicState.todayIsPeakUtilityDay) {
        log.debug "commanding thermostat to stop precool"
        atomicState.lastThermostatCommandTime = now()
        atomicState.processingThermostatCommand = true
        runIn(30, commandThermostatHandler, [data: [coolingSetpoint: precoolStopTemperature]])
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
  
    if (atomicState.lastdebugTime == null) {
      atomicState.lastdebugTime = now()
    }
    if (atomicState.lastLogDelaySeconds == null) {
      atomicState.lastLogDelaySeconds = 1
    }
   
    //make sure the logger handler runIn calls are spaced out; overwrite: false apparently doesn't work well if the runIn calls are scheduled for the same second
    def secondsSinceLastLog = (now() - atomicState.lastdebugTime)/1000
    def delayTime = (atomicState.lastLogDelaySeconds - secondsSinceLastLog + 3).toInteger()
    if (delayTime  < 1 ) {
       delayTime = 1
    } else { 
       if (delayTime > 10) {
        delayTime = 10
      }
    }
    atomicState.lastLogDelaySeconds = delayTime
    atomicState.lastdebugTime = now()
    if (logLevel != null) {
        if (logLevel == "debug" | logLevel == "trace") {
            runIn(delayTime, logDebugHandler, [data: [message: msg], overwrite: false]) 
        }
    }
}

private sendNotificationMessage(message, msgType) {
    def sendNotification = false
    def warning = false
    if (operationMode && operationMode.toString() != "monitorOnly") {
        if (msgType == "anomaly" && (!notifyWhenAnomalies || notifyWhenAnomalies.toBoolean() == true)) {
            sendNotification = true
            warning = true
        } else if (msgType == "thermostat" && (notifyWhenThermostatControlled && notifyWhenThermostatControlled.toBoolean() == true)) {
            sendNotification = true
            warning = false
        } else if (msgType == "demandExceeded" && (notifyWhenCycleDemandExceeded && notifyWhenCycleDemandExceeded.toBoolean() == true)) {
            sendNotification = true
            warning = false
        } else if (msgType == "demandMonth" && (notifyWhenMonthlyDemandExceeded && notifyWhenMonthlyDemandExceeded.toBoolean() == true)) {
            sendNotification = true
            warning = false
        } else if (msgType == "demandGeneral" && (notifyWithGeneralDemandStatus && notifyWithGeneralDemandStatus.toBoolean() == true)) {
            sendNotification = true
            warning = false
        } else if (msgType == "any") {
            sendNotification = true
            warning = false
        }
        if (sendNotification.toBoolean() == true) {
            def sendPushMessage = (notificationMethod && (notificationMethod.toString() == "push" || notificationMethod.toString() == "text and push"))
            def sendTextMessage = (notificationMethod && (notificationMethod.toString() == "text" || notificationMethod.toString() == "text and push"))
            if (sendTextMessage == true) {
                if (phoneNumber) {
                    sendSmsMessage(phoneNumber.toString(), message)
                }
            }
            if (sendPushMessage == true) {
                sendPush(message)
            }
        }
    }
    if (warning.toBoolean() == true) {
        log.warn(message)
    } else {
        log.debug(message)
    }
    def dashboardDevice = getChildDevice("dashboardDevice")
    if (dashboardDevice) {
        dashboardDevice.setMessage(message)
    }
}

def subscribeDevices() {
    def dashboardDevice = getChildDevice("dashboardDevice")

    log.debug "subscribing to Devices"
    subscribe(homeThermostat, "thermostatOperatingState", immediateEvent)
    subscribe(homeThermostat, "coolingSetpoint", immediateEvent)
    subscribe(homeThermostat, "temperature", throttledEvent)
    subscribe(powerGenerator1, "power", immediateEvent)
    subscribe(wholeHomePowerMeter, "power", throttledEvent)
    subscribe(dashboardDevice, "switch.on", peakPeriodSwitchEvent)
    subscribe(dashboardDevice, "switch.off", peakPeriodSwitchEvent)
}

def peakPeriodSwitchEvent(evt) {
    //log.debug "Throttled Event Received: ${evt.device} ${evt.name} ${evt.value}"
    if (operationMode && operationMode.toString() == "fullControl") {
        def dashboardDevice = getChildDevice("dashboardDevice")
        if (dashboardDevice.switchState.stringValue == "on") {
            sendNotificationMessage("Entering utility peak period.", "demandGeneral")
            atomicState.processNewCycleThermo = false //new cycle thermo controls should only be applied after a new/reset cycle in an existing demand period
            peakPeriodOnActions()
        } else {
            sendNotificationMessage("Ending utility peak period.", "demandGeneral")
            peakPeriodOffActions()
        }
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
        process()
    }
}

def setUtilityPeriodGlobalStatus() {
    def peakUsagePeriod
    def peakUsageDay
    def dashboardDevice = getChildDevice("dashboardDevice")
    if (dashboardDevice && dashboardDevice.switchState && dashboardDevice.switchState.stringValue == "on") {
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
    def dashboardDevice = getChildDevice("dashboardDevice")
    if (dashboardDevice) {
         def peakDemand = atomicState.lastRecordedPeakDemand ? atomicState.lastRecordedPeakDemand.toInteger() : 0
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
                 if (notifyWhenMonthlyDemandExceeded && notifyWhenMonthlyDemandExceeded.toBoolean() == true &&
                        peakDemand > exceededBuffer) {
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
    def min = timeNow.format('m').toInteger()
    def sec = timeNow.format('s').toInteger()
    def millisec = timeNow.format('S').toInteger()

    //Protect against unexpected currupted atomic data
    if (atomicState.cycleTimeMinutes == null) {
        log.warn("handling null cycle minutes")
        atomicState.cycleTimeMinutes = 60
    }
    if (atomicState.nominalUsageWatts == null) {
        log.warn("handling null nominal usage watts")
        atomicState.nominalUsageWatts = 1000
    }

    if (min >= atomicState.cycleTimeMinutes) {
        secondsIntoThisCycle = (min - atomicState.cycleTimeMinutes) * 60 + sec + millisec / 1000.0
    } else {
        secondsIntoThisCycle = min * 60 + sec + millisec / 1000.0
    }
    secondsLeftInThisCycle = atomicState.cycleTimeMinutes * 60 - secondsIntoThisCycle
    def secondsSinceLastCheck = (now() - atomicState.lastCycleCheckTime) / 1000.0
    if (secondsSinceLastCheck > atomicState.cycleTimeMinutes * 60 || atomicState.lastMinute > min ||
        (atomicState.lastMinute < atomicState.cycleTimeMinutes && min >= atomicState.cycleTimeMinutes)) {
        logDebug "New Demand Cycle"
        runIn (1, recordFinalCyclePeaks)
        atomicState.demandCurrentWatts = Math.max(wholeHomePowerMeter.powerState.integerValue, 0)
        atomicState.cycleDemandNotificationSent = false
        def demandPeakCurrent = getChildDevice("demandPeakCurrent")
        if (atomicState.nowInPeakUtilityPeriod.toBoolean() == true) {
            atomicState.processNewCycleThermo = true
        }
        if (demandPeakCurrent) {
            demandPeakCurrent.setPower(atomicState.demandCurrentWatts)
        }
        secondsInThisInterval = secondsIntoThisCycle
        if (atomicState.processedDemandOnActions && atomicState.processedDemandOnActions.toBoolean() == true) {
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
    def dashboardDevice = getChildDevice("dashboardDevice")
    if (dashboardDevice) {
        dashboardDevice.setCurrentDemand(data.power)
    }
}

def setDashboardProjectedDemand(data) {
    def dashboardDevice = getChildDevice("dashboardDevice")
    if (dashboardDevice) {
        dashboardDevice.setProjectedDemand(data.power)
    }
}

def calcCurrentAndProjectedDemand() {
    def demandCurrent
    def demandProjected
    def demandPeak
    if (atomicState.demandCurrentWatts == null || atomicState.secondsIntoThisDemandCycle == 0) {
        atomicState.demandCurrentWatts = wholeHomePowerMeter.powerState.integerValue
    } else {
        // current demand
        demandCurrent = ((1.0 * atomicState.demandCurrentWatts * (atomicState.secondsIntoThisDemandCycle - atomicState.deltaIntervalSeconds) +
            Math.max(wholeHomePowerMeter.powerState.integerValue, 0) * atomicState.deltaIntervalSeconds) / atomicState.secondsIntoThisDemandCycle).toInteger()
    }
    // projected demand
    def generatedPower = powerGenerator1 ? powerGenerator1.powerState.integerValue : 0
    demandProjected = ((1.0 * demandCurrent * atomicState.secondsIntoThisDemandCycle + 1.0 * Math.max(atomicState.nominalUsageWatts.toInteger() - generatedPower, 0) *
        atomicState.secondsLeftInThisDemandCycle) / (atomicState.cycleTimeMinutes * 60.0)).toInteger()
    def demandPeakCurrent = getChildDevice("demandPeakCurrent")
    if (demandPeakCurrent) {
        runIn(1, setPeakCurrentDevice, [data: [power: demandCurrent]])
    }
    def dashboardDevice = getChildDevice("dashboardDevice")
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
    def day = new Date().format('DD', location.timeZone).toInteger()
    def month = new Date().format('MM', location.timeZone).toInteger()
    def projectedDemand = atomicState.demandProjectedWatts.toInteger()

    if (!atomicState.lastDay || atomicState.lastDay != day) {
        atomicState.lastDay = day
        def demandPeakToday = getChildDevice("demandPeakToday")
        if (demandPeakToday) {
            demandPeakToday.setPower(0)
        }
        def dashboardDevice = getChildDevice("dashboardDevice")
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
        def dashboardDevice = getChildDevice("dashboardDevice")
        if (dashboardDevice) {
            dashboardDevice.setPeakMonthDemand(0)
        }
    }
    if (atomicState.nowInPeakUtilityPeriod.toBoolean() == true && (now() - atomicState.peakPeriodStartTime > 3 * 60 * 1000)) {
        def exceededBuffer = 0
        if (!notifyWhenDemandExceededBuffer) {
            exceededBuffer = 0
        } else {
            exceededBuffer = notifyWhenDemandExceededBuffer.toInteger()
        }
        if (projectedDemand > atomicState.goalDemandWatts + exceededBuffer) {
            if (!atomicState.cycleDemandNotificationSent || atomicState.cycleDemandNotificationSent.toBoolean() == false) {
                atomicState.cycleDemandNotificationSent = true
                sendNotificationMessage("Projected ${projectedDemand}W cycle demand is estimated to exceed ${atomicState.goalDemandWatts}W goal.", "demandExceeded")
            }
        }

        if (projectedDemand > atomicState.goalDemandWatts &&
            (!atomicState.processedDemandOnActions || atomicState.processedDemandOnActions.toBoolean() == false)) {
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
    def min = timeNow2.format('m').toInteger()
    def hour24 = timeNow.toInteger() + min / 60
    def weightedTemperatureDeparture
    if (thermoRiseSchedulingPlan && thermoRiseSchedulingPlan.toString() != "Off" && hour24 > 13 && hour24 < 20) {
        def scheduledTemperature
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
                log.warn "Unexpected Rise Schedule Plan: ${thermoRiseSchedulingPlan.toString()}"
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

def getTrsStatusString(setPoint) {
    def statusString = ""

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

def turnOffThermostat() {
    logDebug "*******  Shut 'Er Down!...   *********"
    def currentSetPoint = homeThermostat.coolingSetpointState.integerValue
    def setPointToCommand = currentSetPoint
    def tempBumpDegrees = 2
    if (thermoRiseSchedulingPlan && thermoRiseSchedulingPlan.toString() != "Off" && getSmartThermoWeightedDeparture(currentSetPoint) < -1) {
        setPointToCommand = setPointToCommand + 1
        logDebug "Ahead of thermostat schedule. Upping commanded setpoint by 1 to: ${setPointToCommand}"
    }
    if ((setPointToCommand < homeThermostat.temperatureState.integerValue) &&
        (currentSetPoint <= 82)) {
        setPointToCommand = setPointToCommand + 1
        tempBumpDegrees = tempBumpDegrees + 1
        logDebug "Current temperature is above planned setpoint value. Upping commanded setpoint by 1 to: ${setPointToCommand}"
    }

    if (setPointToCommand <= atomicState.maximumAllowedTemperature) {
        if (setPointToCommand == currentSetPoint) {
            sendNotificationMessage("Briefly adjusting thermostat & returning to ${setPointToCommand}F to halt AC. " + getTrsStatusString(setPointToCommand), "thermostat")
        } else {
            sendNotificationMessage("Raising thermost from ${currentSetPoint} to ${setPointToCommand}F to manage demand." + getTrsStatusString(setPointToCommand), "thermostat")
        }
        atomicState.lastThermostatCommandTime = now()
        atomicState.processingThermostatCommand = true
        commandThermostatWithBump(setPointToCommand, tempBumpDegrees)
    } else {
        logDebug "Unexpected condition: Set point to command of ${setPointToCommand} is above max allowed: ${atomicState.maximumAllowedTemperature}. Cannot turn off thermostat."
    }

}

def turnOnThermostat() {
    logDebug "*******  Crank 'Er Up!... *********"
    def setPointToCommand = homeThermostat.coolingSetpointState.integerValue
    def tempBumpDegrees = -3
    atomicState.processingThermostatCommand = true
    atomicState.lastThermostatCommandTime = now()
    commandThermostatWithBump(setPointToCommand, tempBumpDegrees)
    sendNotificationMessage("Briefly initiating cooling to ensure home will remain comfortable per TRS preference." + getTrsStatusString(setPointToCommand), "thermostat")
}

def getTrsAdjustedTargetDemand() {
    def trsAdjustedTargetDemand
    if (homeThermostat.coolingSetpointState.stringValue != "") {
        try {
            def goalAdjustmentWatts = getSmartThermoWeightedDeparture(homeThermostat.coolingSetpointState.integerValue) * 200
            trsAdjustedTargetDemand = atomicState.goalDemandWatts + goalAdjustmentWatts
            if (demandPeakToday && trsAdjustedTargetDemand < demandPeakToday.powerState.integerValue) {
                if (demandPeakToday.powerState.integerValue < atomicState.goalDemandWatts) {
                    trsAdjustedTargetDemand = demandPeakToday.powerState.integerValue
                } else {
                    trsAdjustedTargetDemand = atomicState.goalDemandWatts
                }
            }
        } catch (Exception e) {
            log.warn "exception in getTrsAdjustedTargetDemand: ${e}"
            sendNotificationMessage("Issue processing temperature rise scheduling.", "anomaly")
            trsAdjustedTargetDemand = atomicState.goalDemandWatts
        }
    } else {
        trsAdjustedTargetDemand = atomicState.goalDemandWatts
    }
    return trsAdjustedTargetDemand
}

def thermostatControls() {
    if (homeThermostat) {
        def demandToAttempt = getTrsAdjustedTargetDemand()
        // reduce power generators by 1/3 as a conservative estimate since generation status may be lagging or may decrease in the future. 
        def generatedPower = powerGenerator1 ? powerGenerator1.powerState.integerValue : 0
        def demandWithoutAirConditioning = Math.max((atomicState.nominalUsageWatts - generatedPower * 2 / 3).toInteger(), 0)
        def demandWithAirConditioning = demandWithoutAirConditioning + atomicState.airConditionerWatts
        def demandAtEndOfCycleIfAcContinues = (atomicState.demandCurrentWatts * atomicState.secondsIntoThisDemandCycle +
            demandWithAirConditioning * atomicState.secondsNextInterval +
            demandWithoutAirConditioning * (atomicState.secondsLeftInThisDemandCycle - atomicState.secondsNextInterval)) / (atomicState.cycleTimeMinutes.toInteger() * 60)
        def wattHoursToNextCycleWithAc = atomicState.secondsLeftInThisDemandCycle * (demandWithAirConditioning) * 60 / atomicState.cycleTimeMinutes / 60 / 60
        def estimatedSecondsOfAcToPeak
        if (demandToAttempt - demandWithAirConditioning == 0) {
            estimatedSecondsOfAcToPeak = 100000
        } else {
            estimatedSecondsOfAcToPeak = atomicState.secondsIntoThisDemandCycle * (atomicState.demandCurrentWatts - demandToAttempt) /
                (demandToAttempt - demandWithAirConditioning)
        }
        def thermostatIsBusy = atomicState.processingThermostatCommand && ((now() - atomicState.lastThermostatCommandTime) / 1000) < 210
        logDebug "Demand To Attempt: ${demandToAttempt.toInteger()}. Demand If AC Continues: ${demandAtEndOfCycleIfAcContinues.toInteger()}." +
            " WH To Next Cycle: ${wattHoursToNextCycleWithAc.toInteger()}. Seconds AC To Peak: ${estimatedSecondsOfAcToPeak.toInteger()}." +
            " AC State: ${homeThermostat.thermostatOperatingStateState.stringValue}. Busy: ${thermostatIsBusy}."

        if (atomicState.nowInPeakUtilityPeriod.toBoolean() == true) {
            if (atomicState.lastThermostatCommandTime == null) {
                atomicState.lastThermostatCommandTime = 0. toInteger()
            }
            if (atomicState.minSecsBetweenThermoCommands == null) {
                atomicState.minSecsBetweenThermoCommands = 180. toInteger()
            }
            def allowedToCommandThermo = operationMode && operationMode.toString() == "fullControl" && commandThermostat && commandThermostat.toBoolean() == true &&
                now() - atomicState.peakPeriodStartTime > 2 * 60 * 1000 && atomicState.secondsIntoThisDemandCycle > 60
            //log.debug ("allowed: ${allowedToCommandThermo}")
            if (allowedToCommandThermo && !thermostatIsBusy && ((now() - atomicState.lastThermostatCommandTime) / 1000 > atomicState.minSecsBetweenThermoCommands)) {
                if (atomicState.processNewCycleThermo && atomicState.processNewCycleThermo.toBoolean() == true) {
                    atomicState.processNewCycleThermo = false
                    if (returnSetPointAfterCycle && returnSetPointAfterCycle.toBoolean() == true && returnCycleSetPoint && returnCycleSetPoint.toInteger() > 70 &&
                         returnCycleSetPoint.toInteger() <= atomicState.maximumAllowedTemperature && 
                             returnCycleSetPoint.toInteger() != homeThermostat.coolingSetpointState.integerValue) {                 
                        logDebug ("setting return temp!")
                        atomicState.lastThermostatCommandTime = now()
                        atomicState.processingThermostatCommand = true
                        runIn(15, commandThermostatHandler, [data: [coolingSetpoint: returnCycleSetPoint]])
                    }
                } else {
                    if (homeThermostat.thermostatOperatingStateState.stringValue == 'cooling') {
                        //log.debug ("cooling!")
                        def acWattsAllowedToContinue = 200
                        if (!atomicState.maximumAllowedTemperature) {
                            atomicState.maximumAllowedTemperature = 83
                        }
                        if ((homeThermostat.coolingSetpointState.integerValue <= atomicState.maximumAllowedTemperature) &&
                            (((demandAtEndOfCycleIfAcContinues >= demandToAttempt) &&
                                    (wattHoursToNextCycleWithAc > acWattsAllowedToContinue)) ||
                                (demandAtEndOfCycleIfAcContinues > demandToAttempt + acWattsAllowedToContinue + 100))) {
                            //************************
                            //** Turn Off Thermostat
                            //************************
                            atomicState.lastThermostatCommandTime = now()
                            atomicState.processingThermostatCommand = true
                            runIn(1, turnOffThermostat)
                        }
                    } else {
                        // Thermostat is not cooling, check if the AC should be turned on          
                        if (thermoRiseSchedulingPlan && thermoRiseSchedulingPlan.toString() != "Off" && getSmartThermoWeightedDeparture(homeThermostat.temperatureState.integerValue) > 1 &&
                            estimatedSecondsOfAcToPeak > atomicState.secondsLeftInThisDemandCycle * 1.25 &&
                            homeThermostat.coolingSetpointState.integerValue < homeThermostat.temperatureState.integerValue &&
                            atomicState.secondsLeftInThisDemandCycle > 60) {
                            //************************
                            //** Turn On Thermostat
                            //************************
                            atomicState.lastThermostatCommandTime = now()
                            atomicState.processingThermostatCommand = true
                            runIn(1, turnOnThermostat)
                        }
                    }
                }
            }
        }
    }
}

def verifyThermostatCommandFinal(data) {
    def setPoint = data.coolingSetpoint
    if (homeThermostat.coolingSetpointState.integerValue != setPoint) {
        log.debug("Cooling setpoint was not set to ${data.coolingSetpoint} as commanded (currently: ${homeThermostat.coolingSetpointState.integerValue}). Giving Up...")
        sendNotificationMessage("Warning: Cooling setpoint ${data.coolingSetpoint} could not be verified.", "anomaly")
    } else {
        log.debug("Confirmed cooling setpoint ${data.coolingSetpoint} is set after two tries.")
    }
    atomicState.processingThermostatCommand = false
}

def verifyThermostatCommand(data) {
    def setPoint = data.coolingSetpoint
    if (homeThermostat.coolingSetpointState.integerValue != setPoint) {
        log.debug("Cooling setpoint was not set to ${data.coolingSetpoint} as commanded (currently: ${homeThermostat.coolingSetpointState.integerValue}). Retrying...")
        atomicState.lastThermostatCommandTime = now()
        atomicState.processingThermostatCommand = true
        try {
            homeThermostat.setCoolingSetpoint(setPoint)
        } catch (Exception e) {
            log.debug "exception setting setpoint: ${e}"
            sendNotificationMessage("Warning: thermostat exception in verify when attempting to set cooling setpoint to: ${setPoint}. Exception is: ${e}", "anomaly")
            throw e
        }
        runIn(45, verifyThermostatCommandFinal, [data: [coolingSetpoint: setPoint]])
    } else {
        log.debug("Confirmed cooling setpoint ${data.coolingSetpoint} is set.")
        atomicState.processingThermostatCommand = false
    }
}

def commandThermostatHandler(data) {
    if (!signedRelease || signedRelease != 'I Agree') {
        log.warn "Please sign consent setting in thermostat preferences to allow program to manage the thermostat."
        return
    }
    def setPoint = data.coolingSetpoint
    logDebug "Setting Thermostat to ${setPoint}F degrees."
    atomicState.lastThermostatCommandTime = now()
    atomicState.processingThermostatCommand = true
    try {
        homeThermostat.setCoolingSetpoint(setPoint)
    } catch (Exception e) {
        log.debug "exception setting setpoint: ${e}"
        sendNotificationMessage("Warning: thermostat exception in handler when attempting to set cooling setpoint to: ${setPoint}. Exception is: ${e}", "anomaly")
        runIn(45, verifyThermostatCommand, [data: [coolingSetpoint: setPoint]])
        //throw e
    }
    runIn(45, verifyThermostatCommand, [data: [coolingSetpoint: setPoint]])
}

def commandThermostatWithBump(setPoint, degreesBump) {
    if (!signedRelease || signedRelease != 'I Agree') {
        log.warn "Please accept consent setting in thermostat preferences to allow program to manage the thermostat."
        return
    }
    if (setPoint > atomicState.maximumAllowedTemperature) {
        logDebug "Unexpected condition: Set point to command of ${setPoint} is above max allowed: ${atomicState.maximumAllowedTemperature}. Cannot command thermostat."
        return
    }

    logDebug "Setting Thermostat to ${setPoint}F with bump of ${degreesBump} (${(setPoint + degreesBump)}F)."
    atomicState.lastThermostatCommandTime = now()
    try {
        homeThermostat.setCoolingSetpoint(setPoint + degreesBump)
    } catch (Exception e) {
        log.debug "exception setting setpoint: ${e}"
        sendNotificationMessage("Warning: thermostat exception in bump handler when attempting to set cooling setpoint to: ${setPoint}. Exception is: ${e}", "anomaly")
        //throw e
    }
    atomicState.processingThermostatCommand = true
    // command final setpoint 
    runIn(15, commandThermostatHandler, [data: [coolingSetpoint: setPoint]])
}

def toggleColorIndicatorHandler(data) {
    def stateIsOn = data.stateOn.toBoolean()
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
    def nowInPeakUtilityPeriod = atomicState.nowInPeakUtilityPeriod.toBoolean()
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
   if (!atomicState.deprecatedDevicesLogged & (WD200Dimmer1 || WD200Dimmer2 || colorIndicatorDevice1 || colorIndicatorDevice2 ||
         deviceToTurnOffDuringPeak1 || deviceToTurnOffDuringPeak2 || deviceToTurnOffDuringPeakDemand1 || deviceToTurnOffDuringPeakDemand2)) {
       atomicState.deprecatedDevicesLogged = true
       sendPush ("Demand Manager smart app requires re-entry of display devices and peak period on/off devices in smart app preferences due software update") 
   }
}

def setIndicatorDevices() {
    def nowInPeakUtilityPeriod = atomicState.nowInPeakUtilityPeriod ? atomicState.nowInPeakUtilityPeriod.toBoolean() : false
    runIn (10, checkDeprecated)
    if (colorIndicatorDevices?.size()) {
        def stateChanged = false
        if (atomicState.lastPeakDisplayStateOn == null) {
            atomicState.lastPeakDisplayStateOn = !nowInPeakUtilityPeriod
        }
        if ((nowInPeakUtilityPeriod == true & atomicState.lastPeakDisplayStateOn.toBoolean() == false) ||
            (nowInPeakUtilityPeriod == false & atomicState.lastPeakDisplayStateOn.toBoolean() == true)) {
            //log.debug "state changed!"
            runIn(1, colorIndicatorHandler)
            def displayOffIndicator = alwaysDisplayOffPeakIndicator ? alwaysDisplayOffPeakIndicator.toBoolean() : false
            if (!nowInPeakUtilityPeriod && !displayOffIndicator) {
                runIn(20, toggleColorIndicatorHandler, [data: [stateOn: false]])
            }
        }
    }
    
    if (wD200Dimmers?.size()) {
        def ledRed = 1
        def ledGreen = 2
        def ledYellow = 5
        def ledMagenta = 3
        def level = 1;
        def blinkDuration = 0
        def blink = 0;
        def color = ledGreen;
        def scaleWattsPerLed
        if (solarSystemSizeWatts) {
            scaleWattsPerLed = solarSystemSizeWatts.toDouble() / 6
        } else {
            scaleWattsPerLed = 1000
        }

        if (powerGenerator1) {
            level = ((powerGenerator1.powerState.integerValue + 500) / scaleWattsPerLed + 1).toInteger()
            if (level > 7) {
                level = 7
            } else {
                if (level < 1) {
                    level = 1
                }
            }
        }
        if (nowInPeakUtilityPeriod == true) {
            if (level == 1) {
                color = ledRed
            } else {
                color = ledYellow
            }
        }
        if (nowInPeakUtilityPeriod == true && atomicState.demandProjectedWatts > atomicState.goalDemandWatts * 0.8) {
            blinkDuration = 1000
            color = ledRed
            blink = 1
            if (atomicState.demandProjectedWatts > atomicState.goalDemandWatts * 1.1) {
                blinkDuration = 400
            }
        }

        if (blinkDuration != atomicState.lastBlinkDuration) {
            logDebug "setting led blink duration to: ${blinkDuration}"
            runIn(3, wd200LedBlinkHandler, [data: [duration: blinkDuration]])
        }
        //color=4
        //level=6
        if (color != atomicState.lastLedColor || level != atomicState.lastLedLevel || blink != atomicState.lastLedBlink) {
            //log.debug "Sending dimmer LED: color: ${color} blink: ${blink} level: ${level}"       
            runIn(1, setWD200Leds, [data: [ledLevel: level, ledColor: color, ledBlink: blink]])
        }
    }
}

def wd200LedBlinkHandler(data) {
    def blinkDuration = data.duration
    wD200Dimmers.setBlinkDurationMilliseconds(blinkDuration)
    atomicState.lastBlinkDuration = blinkDuration
}

def setWD200LED(led, data) {
    def color = data.ledColor
    def blink = data.ledBlink
    if (led > data.ledLevel) {
        color = 0
        blink = 0
    }
    wD200Dimmers.setStatusLed(led, color, blink)
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
    def displayOffIndicator = alwaysDisplayOffPeakIndicator ? alwaysDisplayOffPeakIndicator.toBoolean() : false
    def nowInPeakUtilityPeriod = atomicState.nowInPeakUtilityPeriod ? atomicState.nowInPeakUtilityPeriod.toBoolean() : false
    if (nowInPeakUtilityPeriod | displayOffIndicator) {
        // Do not reconfirm color of indicator if it should be off, since setting the color can turn it back on
        runIn(5, colorIndicatorHandler)
    } else if (!nowInPeakUtilityPeriod && !displayOffIndicator) {
        // ensure it is off
        runIn(5, toggleColorIndicatorHandler, [data: [stateOn: false]])
    }
    //why not check the watchDog while we're here:
    processWatchDog()
}

def rescheduleAllEvents() {
    unsubscribe()
    unschedule()
    initialize()
}

def processWatchDog() {
    if (!atomicState.lastProcessedTime) {
        atomicState.lastProcessedTime = now()
    }
    if (!atomicState.lastProcessCompletedTime) {
        tomicState.lastProcessCompletedTime = now()
    }
    def secondsSinceLastProcessed = (now() - atomicState.lastProcessedTime) / 1000
    def secondsSinceLastProcessCompleted = (now() - atomicState.lastProcessCompletedTime) / 1000

    if (secondsSinceLastProcessed > 290) {
        sendNotificationMessage("Warning: Demand Manager has not processed in ${(secondsSinceLastProcessed/60).toInteger()} minutes. Reinitializing", "anomaly")
        runIn(30, rescheduleAllEvents)
    } else if (secondsSinceLastProcessCompleted > 290) {
        sendNotificationMessage("Warning: Demand Manager has not successfully run in ${(secondsSinceLastProcessCompleted/60).toInteger()} minutes. Reinitializing", "anomaly")
        runIn(30, rescheduleAllEvents)
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