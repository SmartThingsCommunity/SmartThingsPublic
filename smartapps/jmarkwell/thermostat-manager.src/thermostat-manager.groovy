/*
 *  Thermostat Manager
 *  Build 2021022104
 *
 *  Copyright 2021 Jordan Markwell
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  ChangeLog:
 *      
 *      20210221
 *          01: tempHandler() will no longer set "heat" or "off" mode in the case that Externally Controlled Emergency Heat is enabled and
 *              the currentOutdoorTemp is below the emergencyHeatThreshold.
 *          02: "off" mode can no longer be conditionally set when the currentTemp reaches the heatingThreshold in "emergency heat" mode.
 *          03: Added currentOutdoorTemp to tempHandler() debug logging.
 *          04: Updates to code comments.
 *
 *      20210218
 *          01: Discovered that Zen Thermostat's device handler doesn't have the emergencyHeat() command. Revised set commands to use
 *              parameter based setThermostatMode() and setThermostatFanMode() instead.
 *
 *      20210207
 *          01: Improvements to the error handling of the new dual capability system.
 *          02: Users will now be notified when Thermostat Manager fails to make an automated change due to a missing device capability.
 *          03: openContactPause() will now only set state.lastThermostatMode on the first time run per pause.
 *          04: Updates to code comments.
 *
 *      20210202
 *          01: Selecting a capability.thermostat device is no longer a required preference item. Thanks to SmartThings Community member,
 *              hsbarrett for pointing this out.
 *
 *      20210131
 *          01: Adding support for thermostats that do not have capability.thermostat. Special thanks to SmartThings Community member,
 *              orangebucket for helping me iron out some of the kinks.
 *          02: Updates to code comments.
 *
 *      20201114
 *          01: Redesigned Smart Home Monitor based setPoint enforcement into mode based setPoint enforcement.
 *          02: Updates to preference text and code comments.
 *
 *      20200114
 *          01: Corrected an issue discovered by SmartThings Community member, gspitman, that causes a null pointer exception to be thrown
 *              in the case that a user has not disabled Energy Saver but has not selected a contact to monitor either.
 *          02: Minor code optimizations.
 *
 *      20191106
 *          01: Added ability to use remote temperature sensor.
 *          02: contactOpenHandler() can no longer schedule openContactPause() while the thermostat is in a paused state.
 *          03: When esConflictResolver() executes ahead of state.pauseTime, rescheduled executions that follow will now execute one second
 *              after state.pauseTime.
 *          04: Updated esConflictResolver() log output.
 *          05: Corrected a mistake in outdoorTempHandler() log output.
 *          06: Removed some dead code from the contactOpenHandler() function.
 *
 *      20190809
 *          01: Corrected an issue that caused the thermostat to be placed in, "off" mode for users who had intended for Thermostat Manager
 *              not to be setting modes.
 *          02: The tempHandler() function will no longer be allowed to set, "heat" mode while the system is in, "emergency heat" mode.
 *          03: Updated preference text, log output and code comments.
 *
 *      20190417
 *          01: Added a "hold-after" timer option to Energy Saver. If set, the hold-after timer will hold the thermostat in a paused status
 *              for a specified number of minutes after all contacts have been closed.
 *          02: The default value of openContactMinutes is now 2.
 *          03: Rearranged the condition checks in esConflictResolver() to reduce unnecessary processing.
 *
 *      20190405
 *          01: Energy Saver can now initiate paused status when the thermostat is in, "off" mode. This was done in order to cover a corner
 *              case in which the thermostat might be kicked on in the case that Energy Saver is enabled and a contact is opened after the
 *              thermostat has been manually turned off.
 *
 *      20190329
 *          01: Updated help text and code comments.
 *          02: Temperature thresholds will no longer be rounded and the currentTemp and currentOutdoorTemp variables will be stored
 *              rounded.
 *
 *      20190327
 *          01: Added ability to enforce setPoints when the Smart Home Monitor security system is in an armed status.
 *          02: Added ability to always enforce temperature setPoints.
 *
 *      20190308
 *          01: Adjusted the conditions that allow the tempHandler() function to initiate, "off" mode.
 *          02: Modified code comments.
 *
 *      20190307
 *          01: Added slider to disable "heat" mode.
 *          02: Added condition that the indoor temperature must be below the heatingThreshold (if it exists) in order to switch to
 *              "emergency heat" mode based on an outdoor temperature sensor.
 *          03: Added slider to disable "cool" mode.
 *          04: "Allow Manual Thermostat Off to Override Thermostat Manager" is now enabled by default.
 *          05: Rearranged the code to prioritize heating mode.
 *          06: Added heatingThreshold/coolingThreshold values to debug output.
 *          07: Extended the descriptions of Heating/Cooling Threshold settings.
 *
 *      20181123
 *          01: Adding a feature requested by SmartThings Community member, richardjroy: A hold-down timer for Energy Saver.
 *          02: Updated the debugging log output of the verifyAndEnforce() function.
 *          03: Added new getSHMSetPoint() function.
 *          04: Added verifyAndEnforce() functionality to Energy Saver functions.
 *          05: Added, "auto" and "off" mode handling capabilities to the verifyAndEnforce() function.
 *          06: contactOpenHandler() can no longer initiate thermostat pause countdown without the user having set a value for
 *              openContactMinutes.
 *          07: openContactMinutes now has a default value.
 *
 *      20181120
 *          01: Added some conditions to the verify portion of the verifyAndEnforce() function.
 *
 *      20181109
 *          01: Changed verifyAndEnforce() function's thermostat mode change retry logging type from logNNotify() to debug.
 *          02: Thermostat Manager will no longer set temperature setPoints unnecessarily following a mode change.
 *          03: Correcting a spelling mistake in the changelog text.
 *          04: Added more debug output to the verifyAndEnforce() function.
 *          05: Modified some code comments.
 *
 *      20181108
 *          01: Consolidated the enforceCoolingSetPoint() and enforceHeatingSetPoint() functions into the new verifyAndEnforce() function.
 *              verifyAndEnforce() adds the capability to verify that a requested thermostat mode change has taken place and takes
 *              corrective action if it has not.
 *
 *      20181018
 *          01: Simplified contactClosedHandler() function.
 *
 *      20181017
 *          01: Disabled thermostat capability test in *empHandler() functions.
 *
 *      20181016
 *          01: Created esConflictResolver(), to resolve a race condition that can cause Energy Saver to permanently switch off the
 *              thermostat if manualOverride is enabled.
 *          02: Changed debug log output.
 *          03: Updated code comments.
 *          04: Modified conditions for mode change commands called by esConflictResolver().
 *
 *      20181012
 *          01: Added a toggle to disable externally controlled emergency heat.
 *          02: Added capability for externally controlled emergency heat system to re-engage, "heat" mode if the temperature rises above
 *              the emergencyHeatThreshold.
 *          03: Renamed extTempHandler() to outdoorTempHandler().
 *          04: Edited the wording of emergency heat menu items.
 *
 *      20181011
 *          01: Created a new menu page for emergency heat settings and moved the useEmergencyHeat toggle into it.
 *          02: Renamed disableSHMSPEnforce to disableSHMSetPointEnforce.
 *          03: Added capability to engage, "emergency heat" mode based on temperature readings from an outdoor thermometer.
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
 *          03: Logging and notifications will now continue to function even if a service is disabled (with the exception of the
 *              notification service itself).
 *          04: General code cleanup.
 *
 *      20180326
 *          01: Now accounting for all possible thermostat modes in contactClosedHandler().
 *          02: Adding (thermostatMode != "off") condition to openContactPause().
 *
 *      20180307
 *          01: If notifications are not configured or are disabled, quietly record qualifying events in the notification log.
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
 *          01: tempHandler() will now check to ensure that Energy Saver states do not contradict the status of the contacts being
 *              monitored.
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
 *          01: Added capability to automatically set thermostat to "off" mode in the case that user selected contact sensors have remained
 *              open for longer than a user specified number of minutes.
 *          02: Added ability to override Thermostat Manager by manually setting the thermostat to "off" mode.
 *          03: Added push notification capability.
 *          04: Modified logging behavior. Rearranged menus. General code cleanup.
 *          05: Added ability to disable Smart Home Monitor based setPoint enforcement without having to remove user defined values.
 *          06: Added ability to disable notifications without having to remove contacts.
 *          07: Missed a comma.
 *          08: Modifying notification messages.
 *          09: Converting tempHandler's event.value to integer.
 *          10: Returned to using thermostat.currentValue("temperature") instead of event.value.toInteger() for the currentTemp variable in
 *              the tempHandler() function.
 *
 *      20171213
 *          01: Standardized optional Smart Home Monitor based setPoint enforcement with corresponding preference settings.
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
 *          03: Added the ability to have Simple Thermostat Manager ignore a temperature threshold by manually setting it to 0.
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
    page(name: "altThermostatConfig")
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
            input "thermostat", "capability.thermostat", title: "Thermostat", multiple: false, required: false
            paragraph "If you are unable to select your thermostat, try Alternative Thermostat Configuration."
            href "altThermostatConfig", title: "Alternative Thermostat Configuration"
            input "tempSensor", "capability.temperatureMeasurement", title: "Temperature Sensor", multiple: false, required: true
            paragraph "When the temperature falls below the heating threshold, Thermostat Manager will set heating mode. This value must always be lower than the Cooling Threshold. Recommended value: 70F (21C)"
            input name: "heatingThreshold", title: "Heating Threshold", type: "number", required: false
            input name: "disableHeat", title: "Don't Set Heat Mode", type: "bool", defaultValue: false, required: true
            paragraph "When the temperature rises higher than the cooling threshold, Thermostat Manager will set cooling mode. This value must always be higher than the Heating Threshold. Recommended value: 75F (24C)"
            input name: "coolingThreshold", title: "Cooling Threshold", type: "number", required: false
            input name: "disableCool", title: "Don't Set Cool Mode", type: "bool", defaultValue: false, required: true
        }
        section("Tips") {
            paragraph "If you set the cooling threshold at the lowest setting you use in your modes and you set the heating threshold at the highest setting you use in your modes, you will not need to create multiple instances of Thermostat Manager."
        }
        section("Optional Settings") {
            input name: "setFan", title: "Maintain Auto Fan Mode", type: "bool", defaultValue: true, required: true
            input name: "manualOverride", title: "Allow Manual Thermostat Off to Override Thermostat Manager", type: "bool", defaultValue: true, required: true
            input name: "debug", title: "Debug Logging", type: "bool", defaultValue: false, required: true
            input name: "disable", title: "Disable Thermostat Manager", type: "bool", defaultValue: false, required: true
            
            href "setPointPage", title: "Mode Based SetPoint Enforcement"
            href "energySaverPage", title: "Energy Saver"
            href "emergencyHeatPage", title: "Emergency Heat Settings"
            href "notificationPage", title: "Notification Settings"
            
            label(title: "Assign a name", required: false)
        }
    }
}

def altThermostatConfig() {
    dynamicPage(name: "altThermostatConfig", title: "Alternative Thermostat Configuration") {
        section() {
            input "tstatMode", "capability.thermostatMode", title: "Thermostat Mode Controller", multiple: false, required: false
            input "thermostatHeatingSetpoint", "capability.thermostatHeatingSetpoint", title: "Thermostat Heating SetPoint Controller", multiple: false, required: false
            input "thermostatCoolingSetpoint", "capability.thermostatCoolingSetpoint", title: "Thermostat Cooling SetPoint Controller", multiple: false, required: false
            input "thermostatFanMode", "capability.thermostatFanMode", title: "Thermostat Fan Mode Controller", multiple: false, required: false
        }
        section() {
            input name: "useAltThermostatConfig", title: "Use Alternative Thermostat Configuration", type: "bool", defaultValue: false, required: true
        }
    }
}

def setPointPage() {
    dynamicPage(name: "setPointPage", title: "Mode Based SetPoint Enforcement") {
        section() {
            paragraph "These optional settings allow you use Thermostat Manager to set your thermostat's cooling and heating setPoints based on your location's Hello Home mode. SetPoints will be set only when a thermostat mode change occurs (e.g. heating to cooling) and only the setPoint for the incoming mode will be set (e.g. A change from heating mode to cooling mode would prompt the cooling setPoint to be set)."
        }
        section("Tips") {
            paragraph "Each mode configuration must be unique. Do not select the same mode in multiple configurations."
        }
        section("Mode Configuration 1") {
            input "modeConfig1", title: "Modes", "mode", multiple: true, required: false
            input name: "offHeatingSetPoint", title: "Heating SetPoint", type: "number", required: false
            input name: "offCoolingSetPoint", title: "Cooling SetPoint", type: "number", required: false
        }
        section("Mode Configuration 2") {
            input "modeConfig2", title: "Modes", "mode", multiple: true, required: false
            input name: "stayHeatingSetPoint", title: "Heating SetPoint", type: "number", required: false
            input name: "stayCoolingSetPoint", title: "Cooling SetPoint", type: "number", required: false
        }
        section("Mode Configuration 3") {
            input "modeConfig3", title: "Modes", "mode", multiple: true, required: false
            input name: "awayHeatingSetPoint", title: "Heating SetPoint", type: "number", required: false
            input name: "awayCoolingSetPoint", title: "Cooling SetPoint", type: "number", required: false
        }
        section() {
            input "armedModes", title: "Select all modes in which SmartThings Home Monitor is ARMED", "mode", multiple: true, required: true
            input name: "enforceArmedSetPoints", title: "Enforce SetPoints in Armed Statuses", type: "bool", defaultValue: false, required: true
            input name: "enforceSetPoints", title: "Always Enforce SetPoints", type: "bool", defaultValue: false, required: true
            input name: "disableSHMSetPointEnforce", title: "Disable Mode Based SetPoint Enforcement", type: "bool", defaultValue: false, required: true
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
            paragraph "Open Contact Time must be set to a value of 1 or greater."
            input name: "openContactMinutes", title: "Open Contact Time (minutes)", type: "number", defaultValue: 2, required: false
            input name: "minPauseMinutes", title: "Minimum Pause Time (minutes)", type: "number", defaultValue: 2, required: false
            paragraph "If Hold-After Time is specified/non-zero, once the thermostat enters a paused state it will remain paused for the specified number of minutes after all selected contacts have been closed."
            input name: "holdAfterMinutes", title: "Hold-After Time (minutes)", type: "number", defaultValue: 2, required: false
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
    
    // if (disableHeat && manualOverride) { app.updateSetting("manualOverride", [type: "bool", value: false]) }
    log.debug "Thermostat_Manager.updated(): ${settings}"
    
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(tempSensor, "temperature", tempHandler)
    subscribe(contact, "contact.open", contactOpenHandler)
    subscribe(contact, "contact.closed", contactClosedHandler)
    subscribe(outdoorTempSensor, "temperature", outdoorTempHandler)
}

def tempHandler(event) {
    def openContact             = contact?.currentValue("contact")?.contains("open")
    def currentTemp             = Math.round( tempSensor.currentValue("temperature") )
    def currentOutdoorTemp      = Math.round( outdoorTempSensor?.currentValue("temperature") )
    def heatingSetpoint         = getHeatingSetpoint()
    def coolingSetpoint         = getCoolingSetpoint()
    def currentThermostatMode   = getThermostatMode()
    def fanMode                 = getFanMode()
    def homeMode                = location.mode
    
    def SHMSetPoint = getSHMSetPoint(currentThermostatMode)
    
    esConflictResolver()
    
    if (debug) {
        // if (event?.device?.hasCapability("Thermostat")) { log.debug "Thermostat_Manager.tempHandler(): (${event.device.typeName}) IS A THERMOSTAT" }
        // else { log.debug "Thermostat_Manager.tempHandler(): (${event.device.typeName}) IS NOT A THERMOSTAT" }
        if (!disableEnergySaver && contact) {
            log.debug "Thermostat_Manager.tempHandler(): At least one contact is open: ${openContact}"
            if (state.lastThermostatMode) { log.debug "Thermostat_Manager.tempHandler(): Thermostat Manager is currently paused." }
        }
        log.debug "Thermostat_Manager.tempHandler(): Hello Home Mode: ${homeMode}"
        log.debug "Thermostat_Manager.tempHandler(): Fan Mode: ${fanMode}"
        log.debug "Thermostat_Manager.tempHandler(): Mode: ${currentThermostatMode}"
        log.debug "Thermostat_Manager.tempHandler(): Heating Threshold: ${heatingThreshold} | Cooling Threshold: ${coolingThreshold}"
        if (SHMSetPoint) { log.debug "Thermostat_Manager.tempHandler(): Mode Configuration SetPoint: ${SHMSetPoint}" }
        log.debug "Thermostat_Manager.tempHandler(): Heating Setpoint: ${heatingSetpoint} | Cooling Setpoint: ${coolingSetpoint}"
        if (currentOutdoorTemp) { log.debug "Thermostat_Manager.tempHandler(): Outdoor Temperature: ${currentOutdoorTemp}" }
        log.debug "Thermostat_Manager.tempHandler(): Indoor Temperature: ${currentTemp}"
    }
   
    if ( (!disable) && (setFan) && (fanMode != "auto") ) {
        logNNotify("Thermostat Manager setting fan mode auto.")
        setFanAuto()
    }
    
    if (
                !disable && !disableHeat &&
                (disableEnergySaver || !state.lastThermostatMode) &&
                ( !manualOverride || ( manualOverride && ( (currentThermostatMode != "off") || state.ignoreOverride ) ) ) &&
                !useEmergencyHeat && (currentThermostatMode != "heat") && (currentThermostatMode != "emergency heat") &&
                heatingThreshold && (currentTemp < heatingThreshold)
    ) {
        
        logNNotify("Thermostat Manager - The temperature has fallen to ${currentTemp}. Setting heat mode.")
        setHeatMode()
        
        def setSetPoint = getSHMSetPoint("heat")
        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "heat", count: 1] ] )
    }
    else if (
                !disable && !disableCool &&
                (disableEnergySaver || !state.lastThermostatMode) &&
                ( !manualOverride || ( manualOverride && ( (currentThermostatMode != "off") || state.ignoreOverride ) ) ) &&
                (currentThermostatMode != "cool") &&
                coolingThreshold && (currentTemp > coolingThreshold)
    ) {
        
        logNNotify("Thermostat Manager - The temperature has risen to ${currentTemp}. Setting cooling mode.")
        setCoolMode()
        
        def setSetPoint = getSHMSetPoint("cool")
        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "cool", count: 1] ] )
    }
    else if (
                !disable &&
                (disableEnergySaver || !state.lastThermostatMode) &&
                ( !manualOverride || ( manualOverride && ( (currentThermostatMode != "off") || state.ignoreOverride ) ) ) &&
                useEmergencyHeat && (currentThermostatMode != "emergency heat") &&
                heatingThreshold && (currentTemp < heatingThreshold)
    ) {
        
        logNNotify("Thermostat Manager - The temperature has fallen to ${currentTemp}. Setting emergency heat mode.")
        setEmergencyHeatMode()
        
        def setSetPoint = getSHMSetPoint("emergency heat")
        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "emergency heat", count: 1] ] )
    }
    else if (
                !disable && !disableHeat &&
                // If the thermostat is in, "emergency heat" mode, then it can't be (paused, or) in, "off" mode.
                !useEmergencyHeat && (currentThermostatMode == "emergency heat") &&
                // Either cool mode is disabled or the temperature is between the heating and cooling thresholds.
                ( (!coolingThreshold || disableCool) || ( coolingThreshold && (currentTemp < coolingThreshold) ) ) &&
                heatingThreshold && (currentTemp > heatingThreshold) &&
                (   // Either Externally Controlled Emergency Heat is disabled or the outdoor temperature is above the emergencyHeatThreshold.
                    disableExtEmergencyHeat ||
                    ( !disableExtEmergencyHeat && (currentOutdoorTemp && emergencyHeatThreshold && (currentOutdoorTemp > emergencyHeatThreshold) ) )
                )
    ) {
        
        logNNotify("Thermostat Manager - The temperature has risen to ${currentTemp}. Setting heat mode.")
        setHeatMode()
        
        def setSetPoint = getSHMSetPoint("heat")
        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "heat", count: 1] ] )
    }
    else if (   // If disableSHMSetPointEnforce is enabled, or if the thermostat is (paused, or) in, "off" mode, SHMSetPoint will be null.
                !disable && SHMSetPoint &&
                ( enforceSetPoints || ( enforceArmedSetPoints && armedModes.contains(location.mode) ) ) &&
                (
                    ( ( (currentThermostatMode == "heat") || (currentThermostatMode == "emergency heat") ) && (heatingSetpoint != SHMSetPoint) ) ||
                    ( (currentThermostatMode == "cool") && (coolingSetpoint != SHMSetPoint) )
                )
    ) {
        
        runIn( 60, verifyAndEnforce, [data: [setPoint: SHMSetPoint, mode: currentThermostatMode, count: 1] ] )
    }
    else if (debug) {
        log.debug "Thermostat_Manager.tempHandler(): Thermostat Manager standing by."
    }
}

def outdoorTempHandler(event) {
    def openContact             = contact?.currentValue("contact")?.contains("open")
    def currentTemp             = Math.round( tempSensor.currentValue("temperature") )
    def currentOutdoorTemp      = Math.round( outdoorTempSensor.currentValue("temperature") )
    def heatingSetpoint         = getHeatingSetpoint()
    def coolingSetpoint         = getCoolingSetpoint()
    def currentThermostatMode   = getThermostatMode()
    def fanMode                 = getFanMode()
    def homeMode                = location.mode
    
    def SHMSetPoint = getSHMSetPoint(currentThermostatMode)
    
    esConflictResolver()
    
    if (debug) {
        // if (event?.device?.hasCapability("Thermostat")) { log.debug "Thermostat_Manager.outdoorTempHandler(): (${event.device.typeName}) IS A THERMOSTAT" }
        // else { log.debug "Thermostat_Manager.outdoorTempHandler(): (${event.device.typeName}) IS NOT A THERMOSTAT" }
        if (!disableEnergySaver && contact) {
            log.debug "Thermostat_Manager.outdoorTempHandler(): At least one contact is open: ${openContact}"
            if (state.lastThermostatMode) { log.debug "Thermostat_Manager.outdoorTempHandler(): Thermostat Manager is currently paused." }
        }
        log.debug "Thermostat_Manager.outdoorTempHandler(): Hello Home Mode: ${homeMode}"
        log.debug "Thermostat_Manager.outdoorTempHandler(): Fan Mode: ${fanMode}"
        log.debug "Thermostat_Manager.outdoorTempHandler(): Mode: ${currentThermostatMode}"
        log.debug "Thermostat_Manager.outdoorTempHandler(): Heating Threshold: ${heatingThreshold} | Cooling Threshold: ${coolingThreshold}"
        if (SHMSetPoint) { log.debug "Thermostat_Manager.outdoorTempHandler(): Mode Configuration SetPoint: ${SHMSetPoint}" }
        log.debug "Thermostat_Manager.outdoorTempHandler(): Heating Setpoint: ${heatingSetpoint} | Cooling Setpoint: ${coolingSetpoint}"
        log.debug "Thermostat_Manager.outdoorTempHandler(): Outdoor Temperature: ${currentOutdoorTemp}"
        log.debug "Thermostat_Manager.outdoorTempHandler(): Indoor Temperature: ${currentTemp}"
    }
    
    if ( (!disable) && (setFan) && (fanMode != "auto") ) {
        logNNotify("Thermostat Manager setting fan mode auto.")
        setFanAuto()
    }
    
    if (
            !disable && !disableExtEmergencyHeat &&
            (disableEnergySaver || !state.lastThermostatMode) &&
            ( !manualOverride || ( manualOverride && ( (currentThermostatMode != "off") || state.ignoreOverride ) ) ) &&
            (currentThermostatMode != "emergency heat") &&
            // If the indoor temperature is below the heatingThreshold and the outdoor temperature falls below the emergencyHeatThreshold.
            ( !heatingThreshold || (heatingThreshold && (currentTemp < heatingThreshold) ) ) &&
            emergencyHeatThreshold && (currentOutdoorTemp < emergencyHeatThreshold)
    ) {
        
        logNNotify("Thermostat Manager - Outdoor temperature has fallen to ${currentOutdoorTemp}. Setting emergency heat mode.")
        setEmergencyHeatMode()
        
        def setSetPoint = getSHMSetPoint("emergency heat")
        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "emergency heat", count: 1] ] )
    }
    else if (
                !disable && !disableExtEmergencyHeat && !disableHeat &&
                // If the thermostat is in, "emergency heat" mode, then it can't be (paused, or) in, "off" mode.
                !useEmergencyHeat && (currentThermostatMode == "emergency heat") &&
                // Either cool mode is disabled or the temperature is between the heating and cooling thresholds.
                ( (!coolingThreshold || disableCool) || ( coolingThreshold && (currentTemp < coolingThreshold) ) ) &&
                (heatingThreshold && (currentTemp < heatingThreshold) ) &&
                // If the outdoor temperature rises above the emergencyHeatThreshold.
                emergencyHeatThreshold && (currentOutdoorTemp > emergencyHeatThreshold)
    ) {
        
        logNNotify("Thermostat Manager - Outdoor temperature has risen to ${currentOutdoorTemp}. Setting heat mode.")
        setHeatMode()
        
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
    def currentThermostatMode = getThermostatMode()
    
    if (currentThermostatMode == inMap.mode) { // If the thermostat has properly changed over to the requested mode.
        if (debug) {
            log.debug "Thermostat_Manager.verifyAndEnforce(): Thermostat has successfully entered ${inMap.mode} mode. (${inMap.count}/3)"
        }
        
        if ( (currentThermostatMode == "heat") || (currentThermostatMode == "cool") ) {
            state.ignoreOverride = false
        }
        
        if (inMap.setPoint) { // If mode based setPoint enforcement is in use.
            def heatingSetpoint = getHeatingSetpoint()
            def coolingSetpoint = getCoolingSetpoint()
            
            if ( ( (currentThermostatMode == "heat") || (currentThermostatMode == "emergency heat") ) && (heatingSetpoint != inMap.setPoint) ) {
                logNNotify("Thermostat Manager is setting the heating setPoint to ${inMap.setPoint}.")
                setHeatSetPoint(inMap.setPoint)
            }
            else if ( (currentThermostatMode == "cool") && (coolingSetpoint != inMap.setPoint) ) {
                logNNotify("Thermostat Manager is setting the cooling setPoint to ${inMap.setPoint}.")
                setCoolSetPoint(inMap.setPoint)
            }
            else if (debug) { // If setPoints do not need to be set.
                log.debug "Thermostat_Manager.verifyAndEnforce(): Existing setPoints match user defined settings."
            }
        }
    }
    else if (   // If the thermostat has failed to change over to the requested mode and has not been subsequently paused or otherwise disabled.
                !disable &&
                (disableEnergySaver || !state.lastThermostatMode) &&
                ( !manualOverride || ( manualOverride && ( (currentThermostatMode != "off") || state.ignoreOverride ) ) )
    ) {
        
        if (inMap.count <= 3) { // Retry 2 times for a maximum of 3 total tries.
            log.debug "Thermostat_Manager.verifyAndEnforce(): Thermostat has failed to initiate ${inMap.mode} mode. (${inMap.count}/3) Trying again."
            
            switch (inMap.mode) {
                case "heat":
                    setHeatMode()
                    break
                case "cool":
                    setCoolMode()
                    break
                case "emergency heat":
                    setEmergencyHeatMode()
                    break
                case "auto":
                    setAutoMode()
                    break
                case "off":
                    setOffMode()
                    break
            }
            
            // SetPoints can only be set for the thermostat's currently active mode.
            runIn( 60, verifyAndEnforce, [data: [setPoint: inMap.setPoint, mode: inMap.mode, count: ++inMap.count] ] )
        }
        else {
            logNNotify("Thermostat Manager - Thermostat failed to change to ${inMap.mode} mode.")
        }
    }
}

def contactOpenHandler(event) {
    if (debug) {
        log.debug "Thermostat_Manager.contactOpenHandler(): A contact has been opened."
    }
    
    if (!disable && !disableEnergySaver && !state.openContactReported) {
        state.openContactReported = true
        
        if (openContactMinutes && !state.lastThermostatMode) {
            runIn( (openContactMinutes * 60), openContactPause )
            log.debug "Thermostat_Manager.contactOpenHandler(): Initiating countdown to thermostat pause."
        }
    }
}

def contactClosedHandler(event) {
    if (debug) {
        log.debug "Thermostat_Manager.contactClosedHandler(): A contact has been closed."
    }
    
    esConflictResolver()
}

def esConflictResolver() { // Remember that state values are not changed until the application has finished running.
    if (    // If all monitored contacts are currently closed.
            !disable && !disableEnergySaver && contact && !contact?.currentValue("contact")?.contains("open") &&
            // Don't waste time on this function if none of the following conditions are met.
            (state.openContactReported || state.lastThermostatMode)
    ) {
        
        def nowTime = now()
        def pauseTime = state.pauseTime
        
        // If an open contact has been reported, discontinue any existing countdown.
        if (state.openContactReported) {
            log.debug "Thermostat_Manager.esConflictResolver(): All contacts have been closed. Discontinuing any existing thermostat pause countdown."
            unschedule(openContactPause)
            state.openContactReported = false
            
            if (state.lastThermostatMode && holdAfterMinutes) {
                pauseTime = nowTime + (60000 * holdAfterMinutes)
                if (pauseTime > state.pauseTime) {
                    state.pauseTime = pauseTime
                }
                else {
                    pauseTime = state.pauseTime
                }
            }
        }
        
        if (state.lastThermostatMode) {
            // If this block can be entered, the thermostat is paused and tempHandler() condition checks will properly fail.
            if ( !pauseTime || (nowTime >= pauseTime) ) {
                def currentThermostatMode = getThermostatMode()
                
                // If the thermostat is currently paused, restore it to its previous state.
                if (currentThermostatMode == "off") {
                    if (state.lastThermostatMode == "heat") {
                        logNNotify("Thermostat Manager - All contacts have been closed. Restoring heat mode.")
                        setHeatMode()
                        
                        def setSetPoint = getSHMSetPoint("heat")
                        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "heat", count: 1] ] )
                    }
                    else if (state.lastThermostatMode == "cool") {
                        logNNotify("Thermostat Manager - All contacts have been closed. Restoring cooling mode.")
                        setCoolMode()
                        
                        def setSetPoint = getSHMSetPoint("cool")
                        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "cool", count: 1] ] )
                    }
                    else if (state.lastThermostatMode == "emergency heat") {
                        logNNotify("Thermostat Manager - All contacts have been closed. Restoring emergency heat mode.")
                        setEmergencyHeatMode()
                        
                        def setSetPoint = getSHMSetPoint("emergency heat")
                        runIn( 60, verifyAndEnforce, [data: [setPoint: setSetPoint, mode: "emergency heat", count: 1] ] )
                    }
                    else if (state.lastThermostatMode == "auto") {
                        logNNotify("Thermostat Manager - All contacts have been closed. Restoring auto mode.")
                        setAutoMode()
                        
                        runIn( 60, verifyAndEnforce, [data: [setPoint: null, mode: "auto", count: 1] ] )
                    }
                }
                state.lastThermostatMode = null
            }
            else if (pauseTime && (pauseTime > nowTime) ) {
                def reRunTime = Math.round( ( (pauseTime + 1000) - nowTime) / 1000 )
                if (debug) { log.debug "Thermostat_Manager.esConflictResolver(): esConflictResolver() will be run again in ${reRunTime} seconds."}
                runIn(reRunTime, esConflictResolver)
            }
        }
    }
}

def openContactPause() {
    if ( contact?.currentValue("contact")?.contains("open") ) { // If any monitored contact is open.
        def currentThermostatMode = getThermostatMode()
        
        if (!state.lastThermostatMode) { state.lastThermostatMode = currentThermostatMode }
        if (minPauseMinutes) { state.pauseTime = now() + (60000 * minPauseMinutes) }
        
        if (currentThermostatMode != "off") {
            logNNotify("Thermostat Manager is turning the thermostat off temporarily due to an open contact.")
            setOffMode()
            runIn( 60, verifyAndEnforce, [data: [setPoint: null, mode: "off", count: 1] ] )
        }
    }
    else { // If no monitored contacts remain open.
        state.openContactReported = false
    }
}

def getSHMSetPoint(newMode) {
    def setSetPoint = null
    
    if (!disableSHMSetPointEnforce) {
        if ( (newMode == "heat") || (newMode == "emergency heat") ) {
            if ( modeConfig1.contains(location.mode) && (offHeatingSetPoint) ) {
                setSetPoint = offHeatingSetPoint
            }
            else if ( modeConfig2.contains(location.mode) && (stayHeatingSetPoint) ) {
                setSetPoint = stayHeatingSetPoint
            }
            else if ( modeConfig3.contains(location.mode) && (awayHeatingSetPoint) ) {
                setSetPoint = awayHeatingSetPoint
            }
        }
        else if (newMode == "cool") {
            if ( modeConfig1.contains(location.mode) && (offCoolingSetPoint) ) {
                setSetPoint = offCoolingSetPoint
            }
            else if ( modeConfig2.contains(location.mode) && (stayCoolingSetPoint) ) {
                setSetPoint = stayCoolingSetPoint
            }
            else if ( modeConfig3.contains(location.mode) && (awayCoolingSetPoint) ) {
                setSetPoint = awayCoolingSetPoint
            }
        }
    }
    
    return(setSetPoint)
}

def getThermostatMode() {
    def currentThermostatMode = null
    
    if (!useAltThermostatConfig && thermostat) {
        currentThermostatMode = thermostat.currentValue("thermostatMode")
    }
    else if (useAltThermostatConfig && tstatMode) {
        currentThermostatMode = tstatMode.currentValue("thermostatMode")
    }
    
    return(currentThermostatMode)
}

def getHeatingSetpoint() {
    def heatingSetpoint = null
    
    if (!useAltThermostatConfig && thermostat) {
        heatingSetpoint = thermostat.currentValue("heatingSetpoint")
    }
    else if (useAltThermostatConfig && thermostatHeatingSetpoint) {
        heatingSetpoint = thermostatHeatingSetpoint.currentValue("heatingSetpoint")
    }
    
    return(heatingSetpoint)
}

def getCoolingSetpoint() {
    def coolingSetpoint = null
    
    if (!useAltThermostatConfig && thermostat) {
        coolingSetpoint = thermostat.currentValue("coolingSetpoint")
    }
    else if (useAltThermostatConfig && thermostatCoolingSetpoint) {
        coolingSetpoint = thermostatCoolingSetpoint.currentValue("coolingSetpoint")
    }
    
    return(coolingSetpoint)
}

def getFanMode() {
    def fanMode = null
    
    if (!useAltThermostatConfig && thermostat) {
        fanMode = thermostat.currentValue("thermostatFanMode")
    }
    else if (useAltThermostatConfig && thermostatFanMode) {
        fanMode = thermostatFanMode.currentValue("thermostatFanMode")
    }
    
    return(fanMode)
}

def setHeatMode() {
    if (!useAltThermostatConfig && thermostat) {
        thermostat.setThermostatMode("heat")
    }
    else if (useAltThermostatConfig && tstatMode) {
        tstatMode.setThermostatMode("heat")
    }
    else {
        logNNotify("Thermostat Manager - Cannot set thermostat mode. No thermostat or thermostatMode devices have been configured.")
    }
}

def setCoolMode() {
    if (!useAltThermostatConfig && thermostat) {
        thermostat.setThermostatMode("cool")
    }
    else if (useAltThermostatConfig && tstatMode) {
        tstatMode.setThermostatMode("cool")
    }
    else {
        logNNotify("Thermostat Manager - Cannot set thermostat mode. No thermostat or thermostatMode devices have been configured.")
    }
}

def setEmergencyHeatMode() {
    if (!useAltThermostatConfig && thermostat) {
        thermostat.setThermostatMode("emergency heat")
    }
    else if (useAltThermostatConfig && tstatMode) {
        tstatMode.setThermostatMode("emergency heat")
    }
    else {
        logNNotify("Thermostat Manager - Cannot set thermostat mode. No thermostat or thermostatMode devices have been configured.")
    }
}

def setAutoMode() {
    if (!useAltThermostatConfig && thermostat) {
        thermostat.setThermostatMode("auto")
    }
    else if (useAltThermostatConfig && tstatMode) {
        tstatMode.setThermostatMode("auto")
    }
    else {
        logNNotify("Thermostat Manager - Cannot set thermostat mode. No thermostat or thermostatMode devices have been configured.")
    }
}

def setOffMode() {
    if (!useAltThermostatConfig && thermostat) {
        thermostat.setThermostatMode("off")
    }
    else if (useAltThermostatConfig && tstatMode) {
        tstatMode.setThermostatMode("off")
    }
    else {
        logNNotify("Thermostat Manager - Cannot set thermostat mode. No thermostat or thermostatMode devices have been configured.")
    }
}

def setHeatSetPoint(setPoint) {
    if (!useAltThermostatConfig && thermostat) {
        thermostat.setHeatingSetpoint(setPoint)
    }
    else if (useAltThermostatConfig && thermostatHeatingSetpoint) {
        thermostatHeatingSetpoint.setHeatingSetpoint(setPoint)
    }
    else {
        logNNotify("Thermostat Manager - Cannot set heating setPoint. No thermostat or thermostatHeatingSetpoint devices have been configured. Select one or disable \"Mode Based SetPoint Enforcement\".")
    }
}

def setCoolSetPoint(setPoint) {
    if (!useAltThermostatConfig && thermostat) {
        thermostat.setCoolingSetpoint(setPoint)
    }
    else if (useAltThermostatConfig && thermostatCoolingSetpoint) {
        thermostatCoolingSetpoint.setCoolingSetpoint(setPoint)
    }
    else {
        logNNotify("Thermostat Manager - Cannot set cooling setPoint. No thermostat or thermostatCoolingSetpoint devices have been configured. Select one or disable \"Mode Based SetPoint Enforcement\".")
    }
}

def setFanAuto() {
    if (!useAltThermostatConfig && thermostat) {
        thermostat.setThermostatFanMode("auto")
    }
    else if (useAltThermostatConfig && thermostatFanMode) {
        thermostatFanMode.setThermostatFanMode("auto")
    }
    else {
        logNNotify("Thermostat Manager - Cannot set fan mode. No thermostat or thermostatFanMode devices have been configured. Select one or disable \"Maintain Auto Fan Mode\".")
    }
}