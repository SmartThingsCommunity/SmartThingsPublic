/**
 *  Thermostat Manager
 *  Build 2018112307
 *
 *  Copyright 2018 Jordan Markwell
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
 *  ChangeLog:
 *      
 *      20181123
 *          01: Adding a feature requested by SmartThings Community member, richardjroy: A hold-down timer for Energy
 *              Saver.
 *          02: Updated the debugging log output of the verifyAndEnforce() function.
 *          03: Added new getSHMSetPoint() function.
 *          04: Added verifyAndEnforce() functionality to Energy Saver functions.
 *          05: Added, "auto" and "off" mode handling capabilities to the verifyAndEnforce() function.
 *          06: contactOpenHandler() can no longer initiate thermostat pause countdown without the user having set a
 *              value for openContactMinutes.
 *          07: openContactMinutes now has a default value.
 *
 *      20181120
 *          01: Added some conditions to the verify portion of the verifyAndEnforce() function.
 *
 *      20181109
 *          01: Changed verifyAndEnforce() function's thermostat mode change retry logging type from logNNotify() to
 *              debug.
 *          02: Thermostat Manager will no longer set temperature setPoints unnecessarily following a mode change.
 *          03: Correcting a spelling mistake in the changelog text.
 *          04: Added more debug output to the verifyAndEnforce() function.
 *          05: Modified some code comments.
 *
 *      20181108
 *          01: Consolidated the enforceCoolingSetPoint() and enforceHeatingSetPoint() functions into the new
 *              verifyAndEnforce() function. verifyAndEnforce() adds the capability to verify that a requested
 *              thermostat mode change has taken place and takes corrective action if it has not.
 *
 *      20181018
 *          01: Simplified contactClosedHandler() function.
 *
 *      20181017
 *          01: Disabled thermostat capability test in *empHandler() functions.
 *
 *      20181016
 *          01: Created esConflictResolver(), to resolve a race condition that can cause Energy Saver to permanently
 *              switch off the thermostat if manualOverride is enabled.
 *          02: Changed debug log output.
 *          03: Updated code comments.
 *          04: Modified conditions for mode change commands called by esConflictResolver().
 *
 *      20181012
 *          01: Added a toggle to disable externally controlled emergency heat.
 *          02: Added capability for externally controlled emergency heat system to re-engage, "heat" mode if the
 *              temperature rises above the emergencyHeatThreshold.
 *          03: Renamed extTempHandler() to outdoorTempHandler().
 *          04: Edited the wording of emergency heat menu items.
 *
 *      20181011
 *          01: Created a new menu page for emergency heat settings and moved the useEmergencyHeat toggle into it.
 *          02: Renamed disableSHMSPEnforce to disableSHMSetPointEnforce.
 *          03: Added capability to engage, "emergency heat" mode based on temperature readings from an outdoor
 *              thermometer.
 *
 *      20181010
 *          01: Added, "emergency heat" mode to contactClosedHandler().
 *          02: Added option to use, "emergency heat" mode in place of heat mode.
 *          03: Set, "pausable" to true.
 *          04: Added, "emergency heat" to heating modes condition check.
 *
 *      20180412
 *          01: A bit of code cleanup.
 *
 *      20180401
 *          01: Correcting a typo in logNNotify() that D_Gjorgjievski from the SmartThings Community forum discovered.
 *          02: Corrected a problem with the openContact variable inside of the tempHandler() function.
 *
 *      20180327
 *          01: Now accounting for all possible thermostat modes in tempHandler().
 *          02: Disabling Thermostat Manager will now disable Energy Saver.
 *          03: Logging and notifications will now continue to function even if a service is disabled (with the
 *              exception of the notification service itself).
 *          04: General code cleanup.
 *
 *      20180326
 *          01: Now accounting for all possible thermostat modes in contactClosedHandler().
 *          02: Adding (thermostatMode != "off") condition to openContactPause().
 *
 *      20180307
 *          01: If notifications are not configured or are disabled, quietly record qualifying events in the
 *              notification log.
 *          02: Changed logNNotify() log level to, "info".
 *
 *      20180306
 *          01: Added temperature threshold recommendations for Celsius.
 *          02: Correction to iconX2Url.
 *          03: Adding pausable setting to definition.
 *
 *      20180109
 *          01: Verify that a monitored contact remains open before allowing Energy Saver to pause the thermostat.
 *
 *      20180102
 *          01: tempHandler() will now check to ensure that Energy Saver states do not contradict the status of the
 *              contacts being monitored.
 *          02: Deleting a misplaced quotation mark.
 *
 *      20171218
 *          01: Don't set modes if Energy Saver has paused the thermostat due to open contacts.
 *
 *      20171216
 *          01: Apparently handler functions don't work without a parameter variable.
 *          02: Turned setPoint enforcement into scheduled functions.
 *
 *      20171215
 *          01: Added capability to automatically set thermostat to "off" mode in the case that user selected contact
 *              sensors have remained open for longer than a user specified number of minutes.
 *          02: Added ability to override Thermostat Manager by manually setting the thermostat to "off" mode.
 *          03: Added push notification capability.
 *          04: Modified logging behavior. Rearranged menus. General code cleanup.
 *          05: Added ability to disable Smart Home Monitor based setPoint enforcement without having to remove user
 *              defined values.
 *          06: Added ability to disable notifications without having to remove contacts.
 *          07: Missed a comma.
 *          08: Modifying notification messages.
 *          09: Converting tempHandler's event.value to integer.
 *          10: Returned to using thermostat.currentValue("temperature") instead of event.value.toInteger() for the
 *              currentTemp variable in the tempHandler() function.
 *
 *      20171213
 *          01: Standardized optional Smart Home Monitor based setPoint enforcement with corresponding preference
 *              settings.
 *          02: Added notification capabilities.
 *          03: Renamed from, "Simple Thermostat Manager" to, "Thermostat Manager".
 *          04: Corrected an incorrect setPoint preference variable.
 *          05: Edited the text of the text notification preference setting.
 *          06: Menu cleanup.
 *
 *      20171212
 *          01: Added Hello Home mode value and Smart Home Monitor status value to debug logging.
 *          02: Added a preliminary form of setPoint enforcement.
 *
 *      20171210
 *          01: Corrected a mistake in the help paragraph.
 *          02: Reconfigured the placement of the help text.
 *          03: Added the ability to have Simple Thermostat Manager ignore a temperature threshold by manually setting
 *              it to 0.
 *
 *      20171125
 *          01: Reverted system back to using user defined boundaries.
 *          02: Changed fanMode state check to check for "auto" instead of "fanAuto".
 *
 *      Earlier:
 *          Creation
 *          Modified to use established thermostat setPoints rather than user defined boundaries.
 */
 
definition(
    name: "Thermostat Manager",
    namespace: "jmarkwell",
    author: "Jordan Markwell",
    description: "Automatically changes thermostat mode in response to changes in temperature that exceed user defined thresholds.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png",
    pausable: true
)

preferences {
    page(name: "mainPage")
    page(name: "setPointPage")
    page(name: "notificationPage")
    page(name: "energySaverPage")
    page(name: "emergencyHeatPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Thermostat Manager", install: true, uninstall: true) {
        section() {
            paragraph "Automatically changes the thermostat mode in response to changes in temperature that exceed user defined thresholds."
        }
        section("Main Configuration") {
            input "thermostat", "capability.thermostat", title: "Thermostat", multiple: false, required: true
            paragraph "When the temperature rises higher than the cooling threshold, Thermostat Manager will set cooling mode. Recommended value: 75F (24C)"
            input name: "coolingThreshold", title: "Cooling Threshold", type: "number", required: false
            paragraph "When the temperature falls below the heating threshold, Thermostat Manager will set heating mode. Recommended value: 70F (21C)"
            input name: "heatingThreshold", title: "Heating Threshold", type: "number", required: false
        }
        section("Tips") {
            paragraph "If you set the cooling threshold at the lowest setting you use in your modes and you set the heating threshold at the highest setting you use in your modes, you will not need to create multiple instances of Thermostat Manager."
            paragraph "If you want to use Thermostat Manager to set cooling mode only or to set heating mode only, remove the value for the threshold that you want to be ignored or set it to 0."
        }
        section("Optional Settings") {
            input name: "setFan", title: "Maintain Auto Fan Mode", type: "bool", defaultValue: true, required: true
            input name: "manualOverride", title: "Allow Manual Thermostat Off to Override Thermostat Manager", type: "bool", defaultValue: false, required: true
            input name: "debug", title: "Debug Logging", type: "bool", defaultValue: false, required: true
            input name: "disable", title: "Disable Thermostat Manager", type: "bool", defaultValue: false, required: true
            
            href "setPointPage", title: "Smart Home Monitor Based SetPoint Enforcement"
            href "energySaverPage", title: "Energy Saver"
            href "emergencyHeatPage", title: "Emergency Heat Settings"
            href "notificationPage", title: "Notification Settings"
            
            label(title: "Assign a name", required: false)
            mode(title: "Set for specific mode(s)")
        }
    }
}

def setPointPage() {
    dynamicPage(name: "setPointPage", title: "Smart Home Monitor Based SetPoint Enforcement") {
        section() {
            paragraph "These optional settings allow you use Thermostat Manager to set your thermostat's cooling and heating setPoints based on the status of Smart Home Monitor; SmartThings' built-in security system. SetPoints will be set only when a thermostat mode change occurs (e.g. heating to cooling) and only the setPoint for the incoming mode will be set (e.g. A change from heating mode to cooling mode would prompt the cooling setPoint to be set)."
        }
        section("Disarmed Status") {
            input name: "offCoolingSetPoint", title: "Cooling SetPoint", type: "number", required: false
            input name: "offHeatingSetPoint", title: "Heating SetPoint", type: "number", required: false
        }
        section("Armed (stay) Status") {
            input name: "stayCoolingSetPoint", title: "Cooling SetPoint", type: "number", required: false
            input name: "stayHeatingSetPoint", title: "Heating SetPoint", type: "number", required: false
        }
        section("Armed (away) Status") {
            input name: "awayCoolingSetPoint", title: "Cooling SetPoint", type: "number", required: false
            input name: "awayHeatingSetPoint", title: "Heating SetPoint", type: "number", required: false
        }
        section() {
            input name: "disableSHMSetPointEnforce", title: "Disable Smart Home Monitor Based SetPoint Enforcement", type: "bool", defaultValue: false, required: true
        }
    }
}

def notificationPage() {
    dynamicPage(name: "notificationPage", title: "Notification Settings") {
        section() {
            input(name: "recipients", title: "Select Notification Recipients", type: "contact", required: false) {
                input name: "phone", title: "Enter Phone Number of Text Message Notification Recipient", type: "phone", required: false
            }
            input name: "pushNotify", title: "Send Push Notifications", type: "bool", defaultValue: false, required: true
        }
        section() {
            input name: "disableNotifications", title: "Disable Notifications", type: "bool", defaultValue: false, required: true
        }
    }
}

def energySaverPage() {
    dynamicPage(name: "energySaverPage", title: "Energy Saver") {
        section() {
            paragraph "Energy Saver will temporarily pause the thermostat (by placing it in \"off\" mode) for a specified minimal amount of minutes in the case that any selected contact sensors are left open for a specified number of minutes."
            input name: "contact", title: "Contact Sensors", type: "capability.contactSensor", multiple: true, required: false
            input name: "openContactMinutes", title: "Open Contact Time (minutes)", type: "number", defaultValue: 5, required: false
            input name: "minPauseMinutes", title: "Minimum Pause Time (minutes)", type: "number", defaultValue: 2, required: false
        }
        section() {
            input name: "disableEnergySaver", title: "Disable Energy Saver", type: "bool", defaultValue: false, required: true
        }
    }
}

def emergencyHeatPage() {
    dynamicPage(name: "emergencyHeatPage", title: "Emergency Heat Settings") {
        section() {
            paragraph "If you would like to have Thermostat Manager enable emergency heat mode based on the temperature outside, select a temperature sensor below."
            input name: "outdoorTempSensor", title: "Outdoor Temperature Sensor", type: "capability.temperatureMeasurement", multiple: false, required: false
            paragraph "When an outdoor temperature sensor reports a temperature lower than the emergency heat threshold, Thermostat Manager will set emergency heat mode. Set the emergency heat threshold at some value lower than the heating threshold."
            input name: "emergencyHeatThreshold", title: "Emergency Heat Threshold", type: "number", required: false
            input name: "disableExtEmergencyHeat", title: "Disable Externally Controlled Emergency Heat", type: "bool", defaultValue: false, required: true
        }
        section() {
            input name: "useEmergencyHeat", title: "Always Use Emergency Heat Mode Instead of Heat Mode", type: "bool", defaultValue: false, required: true
        }
    }
}

def installed() {
    log.debug "Thermostat_Manager.installed(): ${settings}"
    initialize()
}

def updated() {
    state.clear()
    
    log.debug "Thermostat_Manager.updated(): ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(thermostat, "temperature", tempHandler)
    subscribe(contact, "contact.open", contactOpenHandler)
    subscribe(contact, "contact.closed", contactClosedHandler)
    subscribe(outdoorTempSensor, "temperature", outdoorTempHandler)
}

def tempHandler(event) {
    def openContact     = contact?.currentValue("contact")?.contains("open")
    def currentTemp     = thermostat.currentValue("temperature")
    def coolingSetpoint = thermostat.currentValue("coolingSetpoint")
    def heatingSetpoint = thermostat.currentValue("heatingSetpoint")
    def thermostatMode  = thermostat.currentValue("thermostatMode")
    def fanMode         = thermostat.currentValue("thermostatFanMode")
    def homeMode        = location.mode
    def securityStatus  = location.currentValue("alarmSystemStatus")
    
    esConflictResolver()
    
    if (debug) {
        // if (event?.device?.hasCapability("Thermostat")) { log.debug "Thermostat_Manager.tempHandler(): (${event.device.typeName}) IS A THERMOSTAT" }
        // else { log.debug "Thermostat_Manager.tempHandler(): (${event.device.typeName}) IS NOT A THERMOSTAT" }
        if (!disableEnergySaver && contact) {
            log.debug "Thermostat_Manager.tempHandler(): At least one contact is open: ${openContact}"
            if (state.lastThermostatMode) { log.debug "Thermostat_Manager.tempHandler(): Thermostat Manager is currently paused." }
        }
        log.debug "Thermostat_Manager.tempHandler(): Smart Home Monitor Status: ${securityStatus}"
        log.debug "Thermostat_Manager.tempHandler(): Hello Home Mode: ${homeMode}"
        log.debug "Thermostat_Manager.tempHandler(): Fan Mode: ${fanMode}"
        log.debug "Thermostat_Manager.tempHandler(): Mode: ${thermostatMode}"
        log.debug "Thermostat_Manager.tempHandler(): Cooling Setpoint: ${coolingSetpoint} | Heating Setpoint: ${heatingSetpoint}"
        log.debug "Thermostat_Manager.tempHandler(): Indoor Temperature: ${currentTemp}"
    }
   
    if ( (!disable) && (setFan) && (fanMode != "auto") ) {
        logNNotify("Thermostat Manager setting fan mode auto.")
        thermostat.fanAuto()
    }
    
    if ( // Temperature setPoints only stick for the active mode.
            !disable && (disableEnergySaver || !state.lastThermostatMode) &&
            (thermostatMode != "cool") && ( !manualOverride || (manualOverride && (thermostatMode != "off") ) ) &&
            coolingThreshold && ( Math.round(currentTemp) > Math.round(coolingThreshold) )
       ) {
        
        logNNotify("Thermostat Manager - The temperature has risen to ${currentTemp}. Setting cooling mode.")
        thermostat.cool()
        
        // SetPoints won't be changed unless the thermostat is already in the required mode.
        def setSetPoint = getSHMSetPoint("cool")
        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "cool", count: 1] ] )
    }
    else if ( // Temperature setPoints only stick for the active mode.
            !disable && (disableEnergySaver || !state.lastThermostatMode) &&
            (thermostatMode != "heat") && (thermostatMode != "emergency heat") && ( !manualOverride || (manualOverride && (thermostatMode != "off") ) ) &&
            heatingThreshold && ( Math.round(currentTemp) < Math.round(heatingThreshold) )
        ) {
        
        def newMode = "heat"
        if (!useEmergencyHeat) {
            logNNotify("Thermostat Manager - The temperature has fallen to ${currentTemp}. Setting heat mode.")
            thermostat.heat()
        } else {
            newMode = "emergency heat"
            logNNotify("Thermostat Manager - The temperature has fallen to ${currentTemp}. Setting emergency heat mode.")
            thermostat.emergencyHeat()
        }
        
        // SetPoints won't be changed unless the thermostat is already in the required mode.
        def setSetPoint = getSHMSetPoint(newMode)
        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: newMode, count: 1] ] )
    }
    else if (debug) {
        log.debug "Thermostat_Manager.tempHandler(): Thermostat Manager standing by."
    }
}

def outdoorTempHandler(event) {
    def openContact         = contact?.currentValue("contact")?.contains("open")
    def currentTemp         = thermostat.currentValue("temperature")
    def currentOutdoorTemp  = outdoorTempSensor.currentValue("temperature")
    def coolingSetpoint     = thermostat.currentValue("coolingSetpoint")
    def heatingSetpoint     = thermostat.currentValue("heatingSetpoint")
    def thermostatMode      = thermostat.currentValue("thermostatMode")
    def fanMode             = thermostat.currentValue("thermostatFanMode")
    def homeMode            = location.mode
    def securityStatus      = location.currentValue("alarmSystemStatus")
    
    esConflictResolver()
    
    if (debug) {
        // if (event?.device?.hasCapability("Thermostat")) { log.debug "Thermostat_Manager.outdoorTempHandler(): (${event.device.typeName}) IS A THERMOSTAT" }
        // else { log.debug "Thermostat_Manager.outdoorTempHandler(): (${event.device.typeName}) IS NOT A THERMOSTAT" }
        if (!disableEnergySaver && contact) {
            log.debug "Thermostat_Manager.outdoorTempHandler(): At least one contact is open: ${openContact}"
            if (state.lastThermostatMode) { log.debug "Thermostat_Manager.outdoorTempHandler(): Thermostat Manager is currently paused." }
        }
        log.debug "Thermostat_Manager.outdoorTempHandler(): Smart Home Monitor Status: ${securityStatus}"
        log.debug "Thermostat_Manager.outdoorTempHandler(): Hello Home Mode: ${homeMode}"
        log.debug "Thermostat_Manager.outdoorTempHandler(): Fan Mode: ${fanMode}"
        log.debug "Thermostat_Manager.outdoorTempHandler(): Mode: ${thermostatMode}"
        log.debug "Thermostat_Manager.outdoorTempHandler(): Cooling Setpoint: ${coolingSetpoint} | Heating Setpoint: ${heatingSetpoint}"
        log.debug "Thermostat_Manager.outdoorTempHandler(): Outdoor Temperature: ${currentOutdoorTemp}"
        log.debug "Thermostat_Manager.outdoorTempHandler(): Indoor Temperature: ${currentTemp}"
    }
    
    if ( (!disable) && (setFan) && (fanMode != "auto") ) {
        logNNotify("Thermostat Manager setting fan mode auto.")
        thermostat.fanAuto()
    }
    
    if ( // Temperature setPoints only stick for the active mode.
            !disable && !disableExtEmergencyHeat && (disableEnergySaver || !state.lastThermostatMode) &&
            (thermostatMode != "emergency heat") && ( !manualOverride || (manualOverride && (thermostatMode != "off") ) ) &&
            emergencyHeatThreshold && ( Math.round(currentOutdoorTemp) < Math.round(emergencyHeatThreshold) )
        ) {
        
        logNNotify("Thermostat Manager - Outdoor temperature has fallen to ${currentOutdoorTemp}. Setting emergency heat mode.")
        thermostat.emergencyHeat()
        
        // SetPoints won't be changed unless the thermostat is already in the required mode.
        def setSetPoint = getSHMSetPoint("emergency heat")
        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "emergency heat", count: 1] ] )
    }
    else if ( // Temperature setPoints only stick for the active mode.
            !disable && !disableExtEmergencyHeat && !useEmergencyHeat && (thermostatMode == "emergency heat") &&
            emergencyHeatThreshold && ( Math.round(currentOutdoorTemp) > Math.round(emergencyHeatThreshold) ) &&
            heatingThreshold && ( Math.round(currentTemp) < Math.round(heatingThreshold) )
        ) {
        
        logNNotify("Thermostat Manager - Outdoor temperature has risen to ${currentOutdoorTemp}. Setting heat mode.")
        thermostat.heat()
        
        // SetPoints won't be changed unless the thermostat is already in the required mode.
        def setSetPoint = getSHMSetPoint("heat")
        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "heat", count: 1] ] )
    }
    else if (debug) {
        log.debug "Thermostat_Manager.outdoorTempHandler(): Thermostat Manager standing by."
    }
}

def logNNotify(message) {
    log.info message
    if ( (!disableNotifications) && ( (location.contactBookEnabled && recipients) || phone || pushNotify ) ) {
        if (location.contactBookEnabled && recipients) {
            sendNotificationToContacts(message, recipients)
        }
        else if (phone) {
            sendSms(phone, message)
        }
        
        if (pushNotify) {
            sendPush(message)
        }
    }
    else {
        sendNotificationEvent(message)
    }
}

def verifyAndEnforce(inMap) {
    def thermostatMode  = thermostat.currentValue("thermostatMode")
    
    if (thermostatMode == inMap.mode) { // If the thermostat has properly changed over to the requested mode.
        if (debug) {
            log.debug "Thermostat_Manager.verifyAndEnforce(): Thermostat has successfully entered ${inMap.mode} mode. (${inMap.count}/3)"
        }
        
        if (inMap.setPoint) { // If Smart Home Monitor based setPoint enforcement is in use.
            if ( (thermostatMode == "cool") && (thermostat.currentValue("coolingSetpoint") != inMap.setPoint) ) {
                logNNotify("Thermostat Manager is setting the cooling setPoint to ${inMap.setPoint}.")
                thermostat.setCoolingSetpoint(inMap.setPoint)
            }
            else if ( ( (thermostatMode == "heat") || (thermostatMode == "emergency heat") ) && (thermostat.currentValue("heatingSetpoint") != inMap.setPoint) ) {
                logNNotify("Thermostat Manager is setting the heating setPoint to ${inMap.setPoint}.")
                thermostat.setHeatingSetpoint(inMap.setPoint)
            }
            else if (debug) { // If setPoints do not need to be set.
                log.debug "Thermostat_Manager.verifyAndEnforce(): Existing setPoints match user defined settings."
            }
        }
    }  // If the thermostat has failed to change over to the requested mode and has not been subsequently paused or otherwise disabled.
    else if ( !disable && (disableEnergySaver || !state.lastThermostatMode) && ( !manualOverride || (manualOverride && (thermostatMode != "off") ) ) ) {
        if (inMap.count <= 3) { // Retry 2 times for a maximum of 3 total tries.
            log.debug "Thermostat_Manager.verifyAndEnforce(): Thermostat has failed to initiate ${inMap.mode} mode. (${inMap.count}/3) Trying again."
            
            switch (inMap.mode) {
                case "cool":
                    thermostat.cool()
                    break
                case "heat":
                    thermostat.heat()
                    break
                case "emergency heat":
                    thermostat.emergencyHeat()
                    break
                case "auto":
                    thermostat.auto()
                    break
                case "off":
                    thermostat.off()
                    break
            }
            
            runIn( 60, verifyAndEnforce, [data: [setPoint: inMap.setPoint, mode: inMap.mode, count: ++inMap.count] ] )
        }
        else {
            logNNotify("Thermostat Manager - Thermostat failed to change to ${inMap.mode} mode.")
        }
    }
}

def getSHMSetPoint(newMode) {
    def securityStatus = location.currentValue("alarmSystemStatus")
    def setSetPoint = null
    
    if (!disableSHMSetPointEnforce) {
        if (newMode == "cool") {
            if ( (securityStatus == "off") && (offCoolingSetPoint) ) {
                setSetPoint = offCoolingSetPoint
            }
            else if ( (securityStatus == "stay") && (stayCoolingSetPoint) ) {
                setSetPoint = stayCoolingSetPoint
            }
            else if ( (securityStatus == "away") && (awayCoolingSetPoint) ) {
                setSetPoint = awayCoolingSetPoint
            }
        }
        else if ( (newMode == "heat") || (newMode == "emergency heat") ) {
            if ( (securityStatus == "off") && (offHeatingSetPoint) ) {
                setSetPoint = offHeatingSetPoint
            }
            else if ( (securityStatus == "stay") && (stayHeatingSetPoint) ) {
                setSetPoint = stayHeatingSetPoint
            }
            else if ( (securityStatus == "away") && (awayHeatingSetPoint) ) {
                setSetPoint = awayHeatingSetPoint
            }
        }
    }
    
    return(setSetPoint)
}

def contactOpenHandler(event) {
    def thermostatMode = thermostat.currentValue("thermostatMode")
    
    // If the thermostat is not off and all of the contacts were closed previously.
    if (!disable && !disableEnergySaver && openContactMinutes && (thermostatMode != "off") && !state.openContactReported) {
        state.openContactReported = true
        runIn( (openContactMinutes * 60), openContactPause )
        log.debug "Thermostat_Manager.contactOpenHandler(): A contact has been opened. Initiating countdown to thermostat pause."
    }
    else if (debug) {
        log.debug "Thermostat_Manager.contactOpenHandler(): A contact has been opened."
    }
}

def contactClosedHandler(event) {
    if (debug) {
        log.debug "Thermostat_Manager.contactClosedHandler(): A contact has been closed."
    }
    
    esConflictResolver()
}

def esConflictResolver() { // Remember that state values are not changed until the application has finished running.
    // If all monitored contacts are currently closed.
    if ( !disable && !disableEnergySaver && !contact.currentValue("contact").contains("open") ) {
        def nowTime = now()
        
        // If an open contact has been reported, discontinue any existing countdown.
        if (state.openContactReported) {
            log.debug "Thermostat_Manager.esConflictResolver(): All contacts have been closed. Discontinuing any existing thermostat pause countdown."
            unschedule(openContactPause)
            state.openContactReported = false
        }
        
        if ( !minPauseMinutes || (nowTime > state.pauseTime) ) {
            // If the thermostat is currently paused, restore it to its previous state.
            if (state.lastThermostatMode) {
                if (thermostat.currentValue("thermostatMode") == "off") {
                    if (state.lastThermostatMode == "auto") {
                        logNNotify("Thermostat Manager - All contacts have been closed. Restoring auto mode.")
                        thermostat.auto()
                        
                        // SetPoints won't be changed unless the thermostat is already in the required mode.
                        runIn( 60, verifyAndEnforce, [data: [setPoint: null, mode: "auto", count: 1] ] )
                    }
                    else if (state.lastThermostatMode == "emergency heat") {
                        logNNotify("Thermostat Manager - All contacts have been closed. Restoring emergency heat mode.")
                        thermostat.emergencyHeat()
                        
                        // SetPoints won't be changed unless the thermostat is already in the required mode.
                        def setSetPoint = getSHMSetPoint("emergency heat")
                        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "emergency heat", count: 1] ] )
                    }
                    else if (state.lastThermostatMode == "cool") {
                        logNNotify("Thermostat Manager - All contacts have been closed. Restoring cooling mode.")
                        thermostat.cool()
                        
                        // SetPoints won't be changed unless the thermostat is already in the required mode.
                        def setSetPoint = getSHMSetPoint("cool")
                        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "cool", count: 1] ] )
                    }
                    else {
                        logNNotify("Thermostat Manager - All contacts have been closed. Setting heat mode.")
                        thermostat.heat()
                        
                        // SetPoints won't be changed unless the thermostat is already in the required mode.
                        def setSetPoint = getSHMSetPoint("heat")
                        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "heat", count: 1] ] )
                    }
                }
                state.lastThermostatMode = null
            }
        }
        else if (minPauseMinutes) {
            def reRunTime = Math.round( (state.pauseTime - nowTime) / 1000 )
            if (debug) { log.debug "Thermostat_Manager.esConflictResolver(): esConflictResolver() will be run again in ${reRunTime} seconds."}
            runIn(reRunTime, esConflictResolver)
        }
    }
}

def openContactPause() {
    def thermostatMode = thermostat.currentValue("thermostatMode")
    
    // If the thermostat is not in, "off" mode and any monitored contact is open.
    if ( (thermostatMode != "off") && contact.currentValue("contact").contains("open") ) {
        state.lastThermostatMode = thermostat.currentValue("thermostatMode")
        logNNotify("Thermostat Manager is turning the thermostat off temporarily due to an open contact.")
        thermostat.off()
        
        if (minPauseMinutes) { state.pauseTime = now() + (60000 * minPauseMinutes) }
        
        // SetPoints won't be changed unless the thermostat is already in the required mode.
        runIn( 60, verifyAndEnforce, [data: [setPoint: null, mode: "off", count: 1] ] )
    }
    else { // If the thermostat was turned off after an open contact was reported or no monitored contacts remain open.
        state.openContactReported = false
    }
}
