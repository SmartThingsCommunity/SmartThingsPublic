/*
 *  Copyright 2021 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition (name: "SiHAS Multipurpose Sensor", namespace: "shinasys", author: "SHINA SYSTEM") {
        capability "Motion Sensor"
        capability "Configuration"
        capability "Battery"
        capability "Temperature Measurement"
        capability "Illuminance Measurement"
        capability "Relative Humidity Measurement"
        capability "Refresh"
        capability "Health Check"
        capability "Sensor"
        capability "Contact Sensor"
        capability "afterguide46998.peopleCounter"
        
        fingerprint inClusters: "0000,0001,0003,0020,0400,0402,0405,0406,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "USM-300Z", deviceJoinName: "SiHAS MultiPurpose Sensor", mnmn: "SmartThings", vid: "generic-motion-6"
        fingerprint inClusters: "0000,0001,0003,0020,0406,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "OSM-300Z", deviceJoinName: "SiHAS Motion Sensor", mnmn: "SmartThings", vid: "generic-motion-2", ocfDeviceType: "x.com.st.d.sensor.motion"
        fingerprint inClusters: "0000,0003,0402,0001,0405", outClusters: "0004,0003,0019", manufacturer: "ShinaSystem", model: "TSM-300Z", deviceJoinName: "SiHAS Temperature/Humidity Sensor", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Temp/Humidity_Sensor", ocfDeviceType: "oic.d.thermostat"
        fingerprint inClusters: "0000,0001,0003,0020,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "DSM-300Z", deviceJoinName: "SiHAS Contact Sensor", mnmn: "SmartThings", vid: "generic-contact-3", ocfDeviceType: "x.com.st.d.sensor.contact"
        fingerprint inClusters: "0000,0001,0003,000C,0020,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "CSM-300Z", deviceJoinName: "SiHAS People Counter", mnmn: "SmartThingsCommunity", vid: "15962fd0-22b8-352e-9641-de640d672bb6", ocfDeviceType: "x.com.st.d.sensor.motion"
    }
    preferences {
        section {
            input "tempOffset"    , "number", title: "Temperature offset", description: "Select how many degrees to adjust the temperature.", range: "-100..100", displayDuringSetup: false
            input "humidityOffset", "number", title: "Humidity offset"   , description: "Enter a percentage to adjust the humidity.", range: "*..*", displayDuringSetup: false
        }
    }
}

private getILLUMINANCE_MEASUREMENT_CLUSTER() { 0x0400 }
private getOCCUPANCY_SENSING_CLUSTER() { 0x0406 }
private getANALOG_INPUT_BASIC_CLUSTER() { 0x000C }
private getPOWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE() { 0x0020 }
private getTEMPERATURE_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE() { 0x0000 }
private getRALATIVE_HUMIDITY_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE() { 0x0000 }
private getILLUMINANCE_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE() { 0x0000 }
private getOCCUPANCY_SENSING_OCCUPANCY_ATTRIBUTE() { 0x0000 }
private getANALOG_INPUT_BASIC_PRESENT_VALUE_ATTRIBUTE() { 0x0055 }

private List<Map> collectAttributes(Map descMap) {
    List<Map> descMaps = new ArrayList<Map>()
    descMaps.add(descMap)
    if (descMap.additionalAttrs) {
        descMaps.addAll(descMap.additionalAttrs)
    }
    return  descMaps
}

def parse(String description) {
    log.debug "Parsing message from device: $description"

    Map map = zigbee.getEvent(description)
    if (!map) {
        if (description?.startsWith('zone status')) {
            map = parseIasMessage(description)
        } else if (description?.startsWith('read attr')) {
            Map descMap = zigbee.parseDescriptionAsMap(description)
            if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
                List<Map> descMaps = collectAttributes(descMap)
                def battMap = descMaps.find { it.attrInt == POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE }
                if (battMap) {
                    map = getBatteryResult(Integer.parseInt(battMap.value, 16))
                }
            } else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS && descMap.commandInt != 0x07) {
                def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 10))
                map = translateZoneStatus(zs)
            } else if (descMap?.clusterInt == OCCUPANCY_SENSING_CLUSTER && descMap.attrInt == OCCUPANCY_SENSING_OCCUPANCY_ATTRIBUTE && descMap?.value) {
                map = getMotionResult(descMap.value == "01" ? "active" : "inactive")
            } else if (descMap?.clusterInt == ANALOG_INPUT_BASIC_CLUSTER && descMap.attrInt == ANALOG_INPUT_BASIC_PRESENT_VALUE_ATTRIBUTE && descMap?.value) {
                map = getAnalogInputResult(Integer.parseInt(descMap.value,16))
            }
        } else if (description?.startsWith('illuminance:')) { //parse illuminance
            map = parseCustomMessage(description)
        }
    } else if (map.name == "temperature") {
        if (tempOffset) {
            map.value = new BigDecimal((map.value as float) + (tempOffset as float)).setScale(1, BigDecimal.ROUND_HALF_UP)
        }
        map.descriptionText = temperatureScale == 'C' ? "${device.displayName} temperature was ${map.value}°C" : "${device.displayName} temperature was ${map.value}°F"
        map.translatable = true
    } else if (map.name == "humidity") {
        if (humidityOffset) {
            map.value = map.value + (int) humidityOffset
        }
        map.descriptionText = "${device.displayName} humidity was ${map.value}%"
        map.unit = "%"
        map.translatable = true
    }

    def result = map ? createEvent(map) : [:]

    if (description?.startsWith('enroll request')) {
        List cmds = zigbee.enrollResponse()
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    log.debug "result: $result"
    return result
}

private def parseCustomMessage(String description) {
    return [
        name           : description.split(": ")[0],
        value          : description.split(": ")[1],
        translatable   : true
    ]
}

private Map parseIasMessage(String description) {
    ZoneStatus zs = zigbee.parseZoneStatus(description)
    translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
    // Some sensor models that use this DTH use alarm1 and some use alarm2 to signify motion
    if (isDSM300()) {
    	return (zs.isAlarm1Set() || zs.isAlarm2Set()) ? getContactResult('open') : getContactResult('closed')
    } else {    
    	return (zs.isAlarm1Set() || zs.isAlarm2Set()) ? getMotionResult('active') : getMotionResult('inactive')
    } 
}

private Map getBatteryResult(rawValue) {
    def linkText = getLinkText(device)
    def result = [:]
    def volts = rawValue / 10

    if (!(rawValue == 0 || rawValue == 255)) {
        result.name = 'battery'
        result.translatable = true
        def minVolts = 2.3
        def maxVolts = 3.2

        if (isDSM300()) maxVolts = 3.1

        // Get the current battery percentage as a multiplier 0 - 1
        def curValVolts = Integer.parseInt(device.currentState("battery")?.value ?: "100") / 100.0
        // Find the corresponding voltage from our range
        curValVolts = curValVolts * (maxVolts - minVolts) + minVolts
        // Round to the nearest 10th of a volt
        curValVolts = Math.round(10 * curValVolts) / 10.0

        // Only update the battery reading if we don't have a last reading,
        // OR we have received the same reading twice in a row
        // OR we don't currently have a battery reading
        // OR the value we just received is at least 2 steps off from the last reported value
        if (state?.lastVolts == null || state?.lastVolts == volts || device.currentState("battery")?.value == null || Math.abs(curValVolts - volts) > 0.1) {
            def pct = (volts - minVolts) / (maxVolts - minVolts)
            def roundedPct = Math.round(pct * 100)
            if (roundedPct <= 0)
                roundedPct = 1
            result.value = Math.min(100, roundedPct)
        } else {
            // Don't update as we want to smooth the battery values, but do report the last battery state for record keeping purposes
            result.value = device.currentState("battery").value
        }

        result.descriptionText = "${device.displayName} battery was ${result.value}%"
        state.lastVolts = volts
    }
    return result
}

private Map getMotionResult(value) {
    String descriptionText = value == 'active' ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
    return [
        name           : 'motion',
        value          : value,
        descriptionText: descriptionText,
        translatable   : true
    ]
}

private Map getContactResult(value) {
	def linkText = getLinkText(device)
	def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
	return [
		name: 'contact',
		value: value,
		descriptionText: descriptionText
	]
}

private Map getAnalogInputResult(value) {
    Float f = Float.intBitsToFloat(value.intValue())
    int pc = f.round(0)
    String descriptionText = "${device.displayName} : $pc" 
    return [
        name           : 'peopleCounter',
        value          : pc,
        descriptionText: descriptionText,
        translatable   : true
    ]
}

def setPeopleCounter(peoplecounter) {
    int pc =  Float.floatToIntBits(peoplecounter);
    log.debug "SetPeopleCounter = $peoplecounter"
    zigbee.writeAttribute(ANALOG_INPUT_BASIC_CLUSTER, ANALOG_INPUT_BASIC_PRESENT_VALUE_ATTRIBUTE, DataType.FLOAT4, pc)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE)
}

def refresh() {
    def refreshCmds = []

    refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE)

    if (isUSM300() || isTSM300()) {
        refreshCmds += zigbee.readAttribute(zigbee.RELATIVE_HUMIDITY_CLUSTER, RALATIVE_HUMIDITY_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE)
        refreshCmds += zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, TEMPERATURE_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE)
    }

    if (isUSM300()) {
        refreshCmds += zigbee.readAttribute(ILLUMINANCE_MEASUREMENT_CLUSTER, ILLUMINANCE_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE)
    }

    if (isUSM300() || isOSM300()) {
        refreshCmds += zigbee.readAttribute(OCCUPANCY_SENSING_CLUSTER, OCCUPANCY_SENSING_OCCUPANCY_ATTRIBUTE)
        refreshCmds += zigbee.enrollResponse()
    }

    if (isDSM300()) {
        refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE)
        refreshCmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)        
        refreshCmds += zigbee.enrollResponse()
    }
    
    if (isCSM300()) {
        refreshCmds += zigbee.readAttribute(ANALOG_INPUT_BASIC_CLUSTER, ANALOG_INPUT_BASIC_PRESENT_VALUE_ATTRIBUTE)
    }
    
    return refreshCmds
}

def configure() {
    def configCmds = []

    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    // temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
    // battery minReport 30 seconds, maxReportTime 6 hrs by default
    // humidity minReportTime 30 seconds, maxReportTime 60 min
    // illuminance minReportTime 30 seconds, maxReportTime 60 min
    // occupancy sensing minReportTime 10 seconds, maxReportTime 60 min
    // ex) zigbee.configureReporting(0x0001, 0x0020, DataType.UINT8, 600, 21600, 0x01)
    // This is for cluster 0x0001 (power cluster), attribute 0x0021 (battery level), whose type is UINT8,
    // the minimum time between reports is 10 minutes (600 seconds) and the maximum time between reports is 6 hours (21600 seconds),
    // and the amount of change needed to trigger a report is 1 unit (0x01).
    configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE, DataType.UINT8, 30, 21600, 0x01/*100mv*1*/)

    if (isUSM300() || isTSM300()) {
        configCmds += zigbee.configureReporting(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, TEMPERATURE_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE, DataType.INT16, 20, 300, 10/*10/100=0.1도*/)
        configCmds += zigbee.configureReporting(zigbee.RELATIVE_HUMIDITY_CLUSTER, RALATIVE_HUMIDITY_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE, DataType.UINT16, 20, 300, 40/*10/100=0.4%*/)
    }

    if (isUSM300()) {
        configCmds += zigbee.configureReporting(ILLUMINANCE_MEASUREMENT_CLUSTER, ILLUMINANCE_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE, DataType.UINT16, 20, 3600, 10/*10 lux*/)
    }

    if (isUSM300() || isOSM300()) {
        configCmds += zigbee.configureReporting(OCCUPANCY_SENSING_CLUSTER, OCCUPANCY_SENSING_OCCUPANCY_ATTRIBUTE, DataType.BITMAP8, 1, 600, 1)
    }

    if (isDSM300()) {
        configCmds += zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 0, 0xffff, null)
    }
    
    if (isCSM300()) {
        configCmds += zigbee.configureReporting(ANALOG_INPUT_BASIC_CLUSTER, ANALOG_INPUT_BASIC_PRESENT_VALUE_ATTRIBUTE, DataType.FLOAT4, 1, 600, 1)
    }

    return configCmds + refresh()
}

private Boolean isUSM300() {
    device.getDataValue("model") == "USM-300Z"
}

private Boolean isTSM300() {
    device.getDataValue("model") == "TSM-300Z"
}

private Boolean isOSM300() {
    device.getDataValue("model") == "OSM-300Z"
}

private Boolean isDSM300() {
    device.getDataValue("model") == "DSM-300Z"
}

private Boolean isCSM300() {
    device.getDataValue("model") == "CSM-300Z"
}
