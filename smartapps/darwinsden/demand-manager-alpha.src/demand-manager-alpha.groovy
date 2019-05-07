/**
 *  Demand Manager
 *
 *  Author: Darwin@DarwinsDen.com
 *  Copyright 2018, 2019 - All rights reserved
 *  
 *  ****** WARNING ******
 *  Installation and configuration of this software will grant this application control of your home thermostat and other devices. 
 *  Unexpectedly high and low home temperatures and unexpected high utility usage & costs may result due to both the planned 
 *  and unplanned nature of the algorthms and technologies involved, unreliability of devices and networks, and un-anticipated 
 *  software defects including those in this software application and its dependencies. By installing this software, you are accepting
 *  the risks to people, pets, and personal property and agree to not hold the developer liable.
 *  
 *  This software was developed in the hopes that it will be useful to others, however, 
 *  it is distributed on an "AS IS" BASIS, WITOUT WARRANTIES OR GUARANTEES OF ANY KIND, either express or implied. 
 * 
 *  The end user is free to modify this software for personal use. Re-distribution of this software in its original or 
 *  modified form requires explicit written consent from the developer. 
 * 
 *  The developer retains all rights, title, copyright, and interest, including all copyright, patent rights, and trade secrets 
 *  associated with the algorthms, and technologies used herein. 
 *
 */
 
def version() { return "v0.1.2e.20190507" }
/*
 *	07-May-2019 >>> v0.1.2e.20190507 - Added additional exception handling and watchdog processes, logic updates
 */

def warning() {
    return "WARNING: By enabling these features, you are granting permission of this application to control your thermostat and other devices. This software is in early beta stages. Normal operation and/or unexpected failures or defects in this software, dependent hardware, and/or network may result in unintended high or low temperatures and high utility usage & costs."
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
    page(name: "pageNotifications")
    page(name: "pageAdvancedSettings")
    page(name: "pagePeakDayHolidays")
    page(name: "pageDevicesToControl")
}

private pageMain() {
    dynamicPage(name: "pageMain", title: "", install: true, uninstall: false) {
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

        section("Proactively Manage Demand: manage demand by controlling devices and your thermostat") {
            href "pageDevicesToControl", title: "Manage demand by turning off devices during peak periods..", description: "", required: false,
                image: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png"
            href "pageThermostat", title: "Manage demand by automatically adjusting your thermostat cooling setpoint..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/thermostat.png"
        }

        section("Monitor your demand and solar generation") {
            href "pageDisplayIndicators", title: "Choose display indicator devices..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/dashboard.png"
        }

        section("Advanced Settings") {
            href "pageAdvancedSettings", title: "Advanced settings..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/cog.png"
        }

        section("About") {
            paragraph "Version: " + app.version() + "\nMemory used: " + memUsed() +
                "\n\nFor additional information visit: DarwinsDen.com/demand", title: "Demand Manager", required: false
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
            input "airCondWatts", "number", required: false, defaultValue: 5000, title: "Estimated Air Condioner Watts"
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
    dynamicPage(name: "pageDisplayIndicators", title: "Choose display indicator devices (optional)", install: false, uninstall: false) {
        section("Select a color indicator light (such as the EZ MultiPli/HomeSeer HSM200) to indicate when you're in a peak demand period") {
            input "colorIndicatorDevice1", "capability.colorControl", required: false, title: "Select your peak period color indicator device 1"
            input "colorIndicatorDevice2", "capability.colorControl", required: false, title: "Select your peak period color indicator device 2"
        }
        section("Select HomeSeer WD200+ dimmers to be used as demand warning indicators and solar inverter (if present) production level indicators... ") {
            input "WD200Dimmer1", "capability.indicator", required: false, title: "Select your HomeSeer WD200+ dimmer 1"
            input "WD200Dimmer2", "capability.indicator", required: false, title: "Select your HomeSeer WD200+ dimmer 2"
            input "solarSystemSizeWatts", "number", required: false, default: 6000, title: "Size of your Solar System in Watts"
        }
        section("Install Virtual Demand Meters (Current, Projected, Peak Today, and Peak Month) for use with dashboards (ActionTiles, etc..).") {
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
            input "notificationMethod", "enum", required: false, title: "Notification Method", options: ["none", "text", "push", "text and push"]
        }

        section("Phone number for text messages") {
            input "phoneNumber", "phone", title: "Phone number for text messages", description: "Phone Number", required: false
        }
    }
}

def pagePeakDayHolidays() {
    dynamicPage(name: "pagePeakDayHolidays", title: "Future Capability", install: false, uninstall: false) {}
}

def pageDevicesToControl() {
    dynamicPage(name: "pageDevicesToControl", title: "Enter devices that should be turned off during peak utility periods and/or when peak demand exceeds your goal demand",
        install: false, uninstall: false) {

        section("Enter devices that should be turned off during peak utility periods. Devices will be turned on again when the peak period ends... ") {
            input "deviceToTurnOffDuringPeak1", "capability.switch", required: false, title: "Device 1"
            input "deviceToTurnOffDuringPeak2", "capability.switch", required: false, title: "Device 2"
        }
        section("Enter devices that should be turned off when peak demand exceeds your goal demand during any 30 or 60 minute demand period. " +
            "Devices will be turned back on again at the beginning of the next 30 or 60 minute demand cycle... ") {
            input "deviceToTurnOffDuringPeakDemand1", "capability.switch", required: false, title: "Device 1"
            input "deviceToTurnOffDuringPeakDemand2", "capability.switch", required: false, title: "Device 2"
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

        section("Maximum thermostat set point temperature the program will reach. Once this set point temperature is reached, the Demand Manager will no longer raise the thermostat temperature to manage demand.") {
            input "maxTemperature", "number", required: false, defaultValue: 83, title: "Maximum Temperature"
        }

        section("Precool Home to a chosen temperature and return home temperature back when the peak period ends. When used in conjunction with Demand Manager thermostat commanding, " +
               "the pre-cool start time is typically 30 minutes or more before your peak period begins, and the pre-cool return time will typically be the same time " +
               "that your peak period ends. Note: You may wish to  program your smart thermostat to perform this function itself locally instead of using the precool functions here.") {
            input "precoolHome", "boolean", required: false, defaultValue: false, title: "Precool"
            input "precoolStartTime", "time", required: false, title: "Pre-cool start time"
            input "precoolStopTime", "time", required: false, title: "Pre-cool return time"
            input "precoolStartTemperature", "number", required: false, title: "Temperature to pre-cool to (°F)"
            input "precoolStopTemperature", "number", required: false, title: "Temperature to return to at pre-cool return time (°F)"
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
    }
}


def pageAdvancedSettings() {
    dynamicPage(name: "pageAdvancedSettings", title: "Advanced Settings", install: false, uninstall: false) {
        section("IDE Log Level (set log level in SmartThings IDE Live Logging Tob)") {
            input "logLevel", "enum", required: false, title: "IDE Log Level", options: ["none", "trace", "debug", "info","warn"]
        }
    
    }

}


def pageRemove() {
    dynamicPage(name: "pageRemove", title: "", install: false, uninstall: true) {
        section() {
            paragraph parent ? "CAUTION: You are about to remove the '${app.label}'. This action is irreversible. If you are sure you want to do this, please tap on the Remove button below." :
                "CAUTION: You are about to completely remove Demand Manager and all of its settings. This action is irreversible. If you are sure you want to do this, please tap on the Remove button below.",
                required: true, state: null
        }
    }
}


def installed() {
    log.debug("installed called")
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

def updated() {
    log.debug("updated called")

    def defaultDemandGoalWatts = 3000
    def defaultNominalUsageWatts = 1000
    def defaultAirConditionerWatts = 5000

    if (!goalDemandInWatts) {
        atomicState.goalDemandWatts = defaultDemandGoalWatts
    } else {
        atomicState.goalDemandWatts = goalDemandInWatts.toInteger()
    }

    if (!nomUsageWatts) {
        atomicState.nominalUsageWatts = defaultNominalUsageWatts
    } else {
        atomicState.nominalUsageWatts = nomUsageWatts.toInteger()
    }

    if (!airCondWatts) {
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

    unsubscribe()
    unschedule()
    initialize()

    if (getChildDevice("dashboardDevice") == null) {
        log.debug "adding virtual active peak period switch"
        def child = addChildDevice("darwinsden", "Demand Manager Dashboard", "dashboardDevice", null, [name: "dashboardDevice", label: "Demand Manager - Active Peak Period Switch", completedSetup: true])
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

}

def memUsed() {
    def numBytes = state.toString().length() + atomicState.toString().length()
    return "${numBytes} bytes (" + ((100.0 * numBytes / 100000.0).toInteger()).toString() + "%)"
}

def initialize() {
    log.debug "Initializing Demand Manager"
    atomicState.lastThrottleRunTime = now()
    atomicState.lastProcessCompletedTime = now()
    runEvery1Minute(throttleEvents)
    runEvery5Minutes(watchDog)
    subscribeDevices()
    schedulePrecooling()
    schedulePeakTimes()
}

def getSecondsIntoThisDay(def Date) {
           def hour = timeNow.format('h').toInteger()
            def min = timeNow.format('m').toInteger()
            def sec = timeNow.format('s').toInteger()
            def secondsIntoThisDay = hour*3600+min*60+sec
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
    if (precoolHome) {
        if (precoolStartTime && precoolStartTemperature && precoolStopTemperature &&
              precoolStartTemperature <= atomicState.maximumAllowedTemperature && precoolStopTemperature <= atomicState.maximumAllowedTemperature) 
        {
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
        if (deviceToTurnOffDuringPeak1) {
            deviceToTurnOffDuringPeak1.off()
        }
        if (deviceToTurnOffDuringPeak2) {
            deviceToTurnOffDuringPeak2.off()
        }
    }
}

def peakPeriodOffActions() {
    if (operationMode && operationMode.toString() == "fullControl") {
        if (deviceToTurnOffDuringPeak1) {
            deviceToTurnOffDuringPeak1.on()
        }
        if (deviceToTurnOffDuringPeak2) {
            deviceToTurnOffDuringPeak2.on()
        }
    }
}

def peakDemandOnActions() {
    if (operationMode && operationMode.toString() == "fullControl") {
        if (deviceToTurnOffDuringPeakDemand1) {
            deviceToTurnOffDuringPeakDemand1.off()
        }
        if (deviceToTurnOffDuringPeakDemand2) {
            deviceToTurnOffDuringPeakDemand2.off()
        }
    }
}

def peakDemandOffActions() {
    if (operationMode && operationMode.toString() == "fullControl") {
        if (deviceToTurnOffDuringPeakDemand1) {
            deviceToTurnOffDuringPeak1Demand.on()
        }
        if (deviceToTurnOffDuringPeakDemand2) {
            deviceToTurnOffDuringPeakDemand2.on()
        }
    }
}

def turnOnPeakPeriod() {
    def dashboardDevice = getChildDevice("dashboardDevice")
    if (dashboardDevice) {
        if (!monitorOnly || monitorOnly.toBoolean() == false) {
            dashboardDevice.on()
            peakPeriodOnActions()
            sendNotification("now entering peak demand period", "demandGeneral")
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
            log.debug "now ending peak demand period"
            sendNotification("now ending peak demand period", "demandGeneral")
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
        immediateEvent()
    }
}

def stopPeak1Schedule() {
    def month = getTheMonth()
    if (!monthsSchedule1 || monthsSchedule1.contains(month)) {
        atomicState.peak1ScheduleActive = false
        turnOffPeakPeriod()
        immediateEvent()
    }
}

def startPeak2Schedule() {
    def month = getTheMonth()
    if (!weekend() && (!monthsSchedule2 || monthsSchedule2.contains(month))) {
        atomicState.peak2ScheduleActive = true
        turnOnPeakPeriod()
        immediateEvent()
    }
}

def stopPeak2Schedule() {
    def month = getTheMonth()
    if (!monthsSchedule2 || monthsSchedule2.contains(month)) {
        atomicState.peak2ScheduleActive = false
        turnOffPeakPeriod()
        immediateEvent()
    }
}

def startPeak3Schedule() {
    def month = getTheMonth()
    if (!weekend() && (!monthsSchedule3 || monthsSchedule3.contains(month))) {
        atomicState.peak3ScheduleActive = true
        turnOnPeakPeriod()
        immediateEvent()
    }
}

def stopPeak3Schedule() {
    def month = getTheMonth()
    if (!monthsSchedule3 || monthsSchedule3.contains(month)) {
        atomicState.peak3ScheduleActive = false
        turnOffPeakPeriod()
        immediateEvent()
    }
}

def precoolingStart() {
    if (operationMode && operationMode.toString() == "fullControl" && precoolHome && precoolHome.toBoolean() == true &&
        atomicState.todayIsPeakUtilityDay) {
        if (precoolStartTime && precoolStartTemperature) {
            if (precoolStopTime && precoolStopTemperature) {
                if (homeThermostat.coolingSetpointState.integerValue > precoolStartTemperature) {
                    log.debug "commanding thermostat to precool"
                    runIn(15, commandThermostatHandler, [data: [coolingSetpoint: precoolStartTemperature, tryCount: 2]])
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
}

def precoolingStop() {
    if (operationMode && operationMode.toString() == "fullControl" && precoolHome && precoolHome.toBoolean() == true && precoolStopTemperature && atomicState.todayIsPeakUtilityDay) {
        log.debug "commanding thermostat to stop precool"
        runIn(15, commandThermostatHandler, [data: [coolingSetpoint: precoolStopTemperature, tryCount: 2]])
        def precoolStopNotes = ""
        if (atomicState.temperaturePriorToPrecool != precoolStopTemperature) {
            precoolStopNotes = " Note: Setpoint was ${atomicState.temperaturePriorToPrecool} prior to pre-cool."
        }
        sendNotificationMessage("Precooling period complete. Returning thermostat to ${precoolStopTemperature}°F." + precoolStopNotes, "thermostat")
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
    def dashboardDevice = getChildDevice("dashboardDevice")
    if (operationMode && operationMode.toString() == "fullControl") {
        if (dashboardDevice.switchState.stringValue == "on") {
            sendNotificationMessage("Entering utility peak period.", "demandGeneral")
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
    //log.debug "${secondsSinceLastRun}"
    if (secondsSinceLastRun > 300) {
        sendNotificationMessage("Warning: Demand Manager has not processed events in the last 5 minutes. Reinitializing", "anomaly")
        unsubscribe()
        unschedule()
        initialize()
    }
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
    if (min >= atomicState.cycleTimeMinutes) {
        secondsIntoThisCycle = (min - atomicState.cycleTimeMinutes) * 60 + sec + millisec / 1000.0
    } else {
        secondsIntoThisCycle = min * 60 + sec + millisec / 1000.0
    }
    secondsLeftInThisCycle = atomicState.cycleTimeMinutes * 60 - secondsIntoThisCycle
    def secondsSinceLastCheck = (now() - atomicState.lastCycleCheckTime) / 1000.0
    if (secondsSinceLastCheck > atomicState.cycleTimeMinutes * 60 || atomicState.lastMinute > min ||
        (atomicState.lastMinute < atomicState.cycleTimeMinutes && min >= atomicState.cycleTimeMinutes)) {
        log.debug "New Demand Cycle"
        atomicState.demandCurrentWatts = Math.max(wholeHomePowerMeter.powerState.integerValue, 0)
        atomicState.cycleDemandNotificationSent = false
        def demandPeakCurrent = getChildDevice("demandPeakCurrent")
        if (demandPeakCurrent) {
            demandPeakCurrent.setPower(atomicState.demandCurrentWatts)
        }
        secondsInThisInterval = secondsIntoThisCycle
        if (atomicState.processedDemandOnActions && atomicState.processedDemandOnActions.toBoolean() == true) {
            peakDemandOffActions()
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
        demandPeakCurrent.setPower(demandCurrent)
    }
    def dashboardDevice = getChildDevice("dashboardDevice")
    if (dashboardDevice) {
        dashboardDevice.setCurrentDemand(demandCurrent)
        dashboardDevice.setProjectedDemand(demandProjected)
    }
    def demandPeakProjected = getChildDevice("demandPeakProjected")
    if (demandPeakProjected) {
        demandPeakProjected.setPower(demandProjected)
    }
    log.debug "Projected Demand: ${demandProjected}W. Current Demand: ${demandCurrent}W."
    //***********************************************
    //* Set Current and Projected Demand Global Data
    //***********************************************   
    atomicState.demandCurrentWatts = demandCurrent
    atomicState.demandProjectedWatts = demandProjected
}

def recordPeakDemands() {
    def day = new Date().format('DD', location.timeZone).toInteger()
    def month = new Date().format('MM', location.timeZone).toInteger()
    def dashboardDevice = getChildDevice("dashboardDevice")
    def demandPeakToday = getChildDevice("demandPeakToday")
    def demandPeakThisMonth = getChildDevice("demandPeakMonth")
    def projectedDemand = atomicState.demandProjectedWatts.toInteger()

    if (!atomicState.lastDay || atomicState.lastDay != day) {
        atomicState.lastDay = day
        if (demandPeakToday) {
            demandPeakToday.setPower(0)
        }
        if (dashboardDevice) {
            dashboardDevice.setPeakDayDemand(0)
        }
    }
    if (!atomicState.lastMonth || atomicState.lastMonth != month) {
        atomicState.lastMonth = month
        if (demandPeakThisMonth) {
            demandPeakThisMonth.setPower(0)
        }
        if (dashboardDevice) {
            dashboardDevice.setPeakMonthDemand(0)
        }
    }
    if (atomicState.nowInPeakUtilityPeriod.toBoolean() == true && (now() - atomicState.peakPeriodStartTime > 2 * 60 * 1000)) {
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
            peakDemandOnActions()
            atomicState.processedDemandOnActions = true
        }

        if (atomicState.nowInPeakUtilityPeriod.toBoolean() == true && atomicState.secondsLeftInThisDemandCycle < 120) {
            if (dashboardDevice) {
                def peakDemandToday = dashboardDevice.currentValue("peakDayDemand")
                def peakDemandThisMonth = dashboardDevice.currentValue("peakMonthDemand")
                if (!peakDemandToday || projectedDemand > peakDemandToday) {
                    log.debug("setting today's peak demand")
                    dashboardDevice.setPeakDayDemand(projectedDemand)
                    if (demandPeakToday) {
                        demandPeakToday.setPower(projectedDemand)
                    }
                }
                if (!peakDemandThisMonth || projectedDemand > peakDemandThisMonth) {
                    log.debug("setting this month's peak demand")
                    dashboardDevice.setPeakMonthDemand(projectedDemand)
                    if (demandPeakThisMonth) {
                        demandPeakThisMonth.setPower(projectedDemand)
                    }
                    if (notifyWhenMonthlyDemandExceeded && notifyWhenMonthlyDemandExceeded.toBoolean() == true) {
                        sendNotificationMessage("New Peak Demand for ${getTheMonth()} is: ${projectedDemand}W", "demandMonth")
                    }
                }
            }
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
        log.debug "Weighted Temperature Departure: ${(weightedTemperatureDeparture*100).toInteger()/100.0}F. " +
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
    log.debug "*******  Shut 'Er Down!...   *********"
    def setPointToCommand = homeThermostat.coolingSetpointState.integerValue
    def tempBumpDegrees = 2
    if (thermoRiseSchedulingPlan && thermoRiseSchedulingPlan.toString() != "Off" && getSmartThermoWeightedDeparture(homeThermostat.coolingSetpointState.integerValue) < -1) {
        setPointToCommand = setPointToCommand + 1
        log.debug "Ahead of thermostat schedule. Upping commanded setpoint by 1 to: ${setPointToCommand}"
    }
    if ((setPointToCommand < homeThermostat.temperatureState.integerValue) &&
        (homeThermostat.coolingSetpointState.integerValue <= 82)) {
        setPointToCommand = setPointToCommand + 1
        tempBumpDegrees = tempBumpDegrees + 1
        log.debug "Current temperature is above planned setpoint value. Upping commanded setpoint by 1 to: ${setPointToCommand}"
    }

    if (setPointToCommand == homeThermostat.coolingSetpointState.integerValue) {
        sendNotificationMessage("Briefly adjusting thermostat & returning to ${setPointToCommand}F to halt AC. " + getTrsStatusString(setPointToCommand), "thermostat")
    } else {
        sendNotificationMessage("Raising thermost from ${homeThermostat.coolingSetpointState.integerValue} to ${setPointToCommand}F to manage demand." + getTrsStatusString(setPointToCommand), "thermostat")
    }
    atomicState.lastThermostatCommandTime = now()
    commandThermostatWithBump(setPointToCommand, tempBumpDegrees)
}

def turnOnThermostat() {
    log.debug "*******  Crank 'Er Up!... *********"
    def setPointToCommand = homeThermostat.coolingSetpointState.integerValue
    def tempBumpDegrees = -3
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
        def estimatedSecondsOfAcToPeak = atomicState.secondsIntoThisDemandCycle * (atomicState.demandCurrentWatts - demandToAttempt) /
            (demandToAttempt - demandWithAirConditioning)
        def thermostatIsBusy = atomicState.processingThermostatCommand && ((now() - atomicState.lastThermostatCommandTime) / 1000) < 210
        log.debug "Demand To Attempt: ${demandToAttempt.toInteger()}. Demand If AC Continues: ${demandAtEndOfCycleIfAcContinues.toInteger()}." +
            " WH To Next Cycle: ${wattHoursToNextCycleWithAc.toInteger()}. Seconds AC To Peak: ${estimatedSecondsOfAcToPeak.toInteger()}." +
            " AC State: ${homeThermostat.thermostatOperatingStateState.stringValue}. Busy: ${thermostatIsBusy}."

        if (atomicState.nowInPeakUtilityPeriod.toBoolean() == true) {
            if (atomicState.lastThermostatCommandTime == null) {
                atomicState.lastThermostatCommandTime = 0. toInteger()
            }
            def allowedToCommandThermo = operationMode && operationMode.toString() == "fullControl" && commandThermostat && commandThermostat.toBoolean() == true &&
                now() - atomicState.peakPeriodStartTime > 2 * 60 * 1000
            //log.debug ("allowed: ${allowedToCommandThermo}")
            if (allowedToCommandThermo && !thermostatIsBusy && ((now() - atomicState.lastThermostatCommandTime) / 1000 >= 60)) {
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
                        runIn(1, turnOnThermostat)
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
    log.debug "Setting Thermostat to ${setPoint}F degrees."
    atomicState.lastThermostatCommandTime = now()
    try {
        homeThermostat.setCoolingSetpoint(setPoint)
    } catch (Exception e) {
        log.debug "exception setting setpoint: ${e}"
        sendNotificationMessage("Warning: thermostat exception in handler when attempting to set cooling setpoint to: ${setPoint}. Exception is: ${e}", "anomaly")
        runIn(45, verifyThermostatCommand, [data: [coolingSetpoint: setPoint]])
        throw e
    }
    runIn(45, verifyThermostatCommand, [data: [coolingSetpoint: setPoint]])
}

def commandThermostatWithBump(setPoint, degreesBump) {
    if (!signedRelease || signedRelease != 'I Agree') {
        log.warn "Please sign consent setting in thermostat preferences to allow program to manage the thermostat."
        return
    }
    log.debug "Setting Thermostat to ${setPoint}F with bump of ${degreesBump} (${(setPoint + degreesBump)}F)."
    atomicState.lastThermostatCommandTime = now()
    try {
        homeThermostat.setCoolingSetpoint(setPoint + degreesBump)
    } catch (Exception e) {
        log.debug "exception setting setpoint: ${e}"
        sendNotificationMessage("Warning: thermostat exception in bump handler when attempting to set cooling setpoint to: ${setPoint}. Exception is: ${e}", "anomaly")
        throw e
    }
    atomicState.processingThermostatCommand = true
    // command final setpoint 
    runIn(15, commandThermostatHandler, [data: [coolingSetpoint: setPoint, tryCount: 2]])
}

def toggleColorIndicatorHandler(data) {
    def stateIsOn = data.stateOn.toBoolean()
    if (stateIsOn == true) {
        log.debug("turning on light!")
        if (colorIndicatorDevice1) {
           colorIndicatorDevice1.on()
        }
        if (colorIndicatorDevice2) {
           colorIndicatorDevice2.on()
        }
    } else {
        log.debug("turning off light!")
        if (colorIndicatorDevice1) {
           colorIndicatorDevice1.off()
        }
        if (colorIndicatorDevice2) {
           colorIndicatorDevice2.off()
        }
    }
}

def colorIndicatorOnPeakHandler() {
    def red = [level: 0, saturation: 0, hex: "#f0000"]
    if (colorIndicatorDevice1) {
        colorIndicatorDevice1.setColor(red)
    }
    if (colorIndicatorDevice2) {
        colorIndicatorDevice2.setColor(red)
    }
}

def colorIndicatorOffPeakHandler() {
    def green = [level: 0, saturation: 0, hex: "#00FF00"]
    if (colorIndicatorDevice1) {
       colorIndicatorDevice1.setColor(green)
    }
    if (colorIndicatorDevice2) {
       colorIndicatorDevice2.setColor(green)
    }
    runIn(5, toggleColorIndicatorHandler, [data: [stateOn: false]])
}

def setEachWd200Led(data) {
    def Dimmer
    def Id
    if (WD200Dimmer1.id == data.device) {
        Dimmer = WD200Dimmer1
        Id = 1
    } else if (WD200Dimmer2.id == data.device) {
        Dimmer = WD200Dimmer2
        Id = 2
    }
    if (atomicState.wd200ProcessLock[Id] == true) {
        log.debug "locked! led ${data.ledNumber}"
        for (int i = 0; i < 15; i++) {
            pause(500)
            if (atomicState?.wd200ProcessLock[Id] == false) {
                atomicState?.wd200ProcessLock[Id] = true
                log.debug "lock obtained! led ${data.ledNumber}"
                break
            }
            if (i == 14) {
                log.warn "Failed to get lock to set wd200 LED: ${data.ledNumber}."
                atomicState.lastLedLevel = 0
            }
        }
    }
    atomicState.wd200ProcessLock[Id] = true
    log.debug "Setting dimmer ${Id} LED: ${data.ledNumber} color: ${data.ledColor} blink: ${data.ledBlink}"
    Dimmer.setStatusLed(data.ledNumber, data.ledColor, data.ledBlink)
    atomicState.wd200ProcessLock[Id] = false
}

def setIndicatorDevices() {
    def nowInPeakUtilityPeriod = atomicState.nowInPeakUtilityPeriod.toBoolean()
    if (colorIndicatorDevice1 || colorIndicatorDevice2) {
        def stateChanged = false
        if (atomicState.lastPeakStateOn != null) {
            if ((nowInPeakUtilityPeriod == true & atomicState.lastPeakStateOn.toBoolean() == false) ||
                (nowInPeakUtilityPeriod == false & atomicState.lastPeakStateOn.toBoolean() == true)) {
                stateChanged = true
            }
        }
        if (stateChanged) {
            //log.debug "state changed!"
            if (nowInPeakUtilityPeriod == true) {
                runIn(5, colorIndicatorOnPeakHandler)
            } else {
                runIn(5, colorIndicatorOffPeakHandler)
            }
        }
    }
    atomicState.lastPeakStateOn = nowInPeakUtilityPeriod

    if (WD200Dimmer1 || WD200Dimmer2) {
        def ledRed = 1
        def ledGreen = 2
        def ledYellow = 5
        def ledMagenta = 3
        def ledLevel = 1;
        def blinkDuration = 0
        def blink = 0;
        def color = ledGreen;
        def scaleWattsPerLed 
        if (solarSystemSizeWatts) {
            scaleWattsPerLed = solarSystemSizeWatts.toDouble()/6
        } else
        { 
            scaleWattsPerLed = 1000
        }

        if (powerGenerator1) {
            ledLevel = ((powerGenerator1.powerState.integerValue + 500) / scaleWattsPerLed + 1).toInteger()
            if (ledLevel > 7) {
                ledLevel = 7
            } else {
                if (ledLevel < 1) {
                    ledLevel = 1
                }
            }
        }
        if (nowInPeakUtilityPeriod == true) {
                if (ledLevel == 1) {
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
            log.debug "setting led blink duration to: ${blinkDuration}"
            runIn(15, wd200LedBlinkHandler, [data: [duration: blinkDuration]])
        }
        //color=5
        //ledLevel = 3
        if (color != atomicState.lastLedColor || ledLevel != atomicState.lastLedLevel || blink != atomicState.lastLedBlink) {
            setWD200LEDs(ledLevel, color, blink)
        }
        atomicState.lastLedLevel = ledLevel
        atomicState.lastLedColor = color
        atomicState.lastLedBlink = blink
        atomicState.lastBlinkDuration = blinkDuration
    }
}

def wd200LedBlinkHandler(data) {
    def blinkDuration = data.duration
    if (WD200Dimmer1) {
        WD200Dimmer1.setBlinkDurationMilliseconds(blinkDuration)
    }
    if (WD200Dimmer2) {
        WD200Dimmer2.setBlinkDurationMilliseconds(blinkDuration)
    }
}

def setWD200LEDs(ledLevel, ledColor, ledBlink) {
    def locks = new Boolean[2]
    locks = [false, false]
    atomicState.wd200ProcessLock = locks
    for (int led = 1; led <= 7; led++) {
        def color = ledColor
        def blink = ledBlink
        if (led > ledLevel) {
            color = 0
            blink = 0
        }
        //log.debug "setting led: ${led} color: ${color} blink: ${blink}"
        def timeToRun1 = new Date(now() + 10000 + 2000 * led);
        def timeToRun2 = new Date(now() + 10000 + 2000 * led + 1000);
        if (WD200Dimmer1) {
            runOnce(timeToRun1, setEachWd200Led, [overwrite: false, data: [device: WD200Dimmer1.id, ledNumber: led, ledColor: color, ledBlink: blink]])
        }
        if (WD200Dimmer2) {
            runOnce(timeToRun2, setEachWd200Led, [overwrite: false, data: [device: WD200Dimmer2.id, ledNumber: led, ledColor: color, ledBlink: blink]])
        }
    }
}

def watchDog ()
{
    def secondsSinceLastProcessCompleted = (now() - atomicState.lastProcessCompletedTime) / 1000
    if (secondsSinceLastProcessCompleted > 290) {
        sendNotificationMessage("Warning: Demand Manager has not successfully run in the last 5 minutes. Reinitializing", "anomaly")
        unsubscribe()
        unschedule()
        initialize()
    }
}

def process() {
    setUtilityPeriodGlobalStatus()
    setCycleStatus()
    calcCurrentAndProjectedDemand()
    thermostatControls()
    recordPeakDemands()
    setIndicatorDevices()
    atomicState.lastProcessCompletedTime = now()
}