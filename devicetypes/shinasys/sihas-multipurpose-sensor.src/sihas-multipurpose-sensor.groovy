/*
<<<<<<< HEAD
 *  Copyright 2021 SmartThings
=======
 *  Copyright 2018 SmartThings
>>>>>>> * first release 
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
        
        fingerprint inClusters: "0000,0001,0003,0020,0400,0402,0405,0406,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "USM-300Z", deviceJoinName: "SiHAS MultiPurpose Sensor", mnmn: "SmartThings", vid: "generic-motion-6"
<<<<<<< HEAD
        fingerprint inClusters: "0000,0001,0003,0020,0406,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "OSM-300Z", deviceJoinName: "SiHAS Motion Sensor", mnmn: "SmartThings", vid: "generic-motion-2"
        fingerprint inClusters: "0000,0003,0402,0001,0405", outClusters: "0004,0003,0019", manufacturer: "ShinaSystem", model: "TSM-300Z", deviceJoinName: "SiHAS Temperature/Humidity Sensor", mnmn: "SmartThings", vid: "generic-humidity"
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
private getPOWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE() { 0x0020 }
private getTEMPERATURE_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE() { 0x0000 }
private getRALATIVE_HUMIDITY_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE() { 0x0000 }
private getILLUMINANCE_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE() { 0x0000 }
private getOCCUPANCY_SENSING_OCCUPANCY_ATTRIBUTE() { 0x0000 }

private List<Map> collectAttributes(Map descMap) {
    List<Map> descMaps = new ArrayList<Map>()
    descMaps.add(descMap)
    if (descMap.additionalAttrs) {
        descMaps.addAll(descMap.additionalAttrs)
    }
=======
        fingerprint inClusters: "0000,0001,0003,0020,0406,0500",                outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "OSM-300Z", deviceJoinName: "SiHAS Motion Sensor", mnmn: "SmartThings", vid: "generic-motion-2"
        fingerprint inClusters: "0000,0003,0402,0001,0405",                     outClusters: "0004,0003,0019", manufacturer: "ShinaSystem", model: "TSM-300Z", deviceJoinName: "SiHAS Temperature/Humidity Sensor", mnmn: "SmartThings", vid: "generic-humidity"
    }

    simulator {
        status "active": "zone report :: type: 19 value: 0031"
        status "inactive": "zone report :: type: 19 value: 0030"
    }

    preferences {
        
        section {
            input "tempOffset"    , "number", title: "Temperature offset", description: "Select how many degrees to adjust the temperature.", range: "-100..100", displayDuringSetup: false
            input "humidityOffset", "number", title: "Humidity offset"   , description: "Enter a percentage to adjust the humidity.", range: "*..*", displayDuringSetup: false            
        }     	
    }
	
    tiles(scale: 2) {
        multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
            tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
                attributeState "active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
                attributeState "inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#cccccc"
            }
        }
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state("temperature", label: '${currentValue}°', unit: "C",
                    backgroundColors: [
                            [value: 31, color: "#153591"],
                            [value: 44, color: "#1e9cbb"],
                            [value: 59, color: "#90d2a7"],
                            [value: 74, color: "#44b621"],
                            [value: 84, color: "#f1d801"],
                            [value: 95, color: "#d04e00"],
                            [value: 96, color: "#bc2323"]
                    ]
            )
        }
        valueTile("humidity", "device.humidity", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "humidity", label: '${currentValue}% humidity', unit: ""
        }
        
        valueTile("illuminance", "device.illuminance", width: 2, height: 2) {
            state "luminosity", label:'${currentValue} ${unit}', unit:"lux"
        }
        
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label: '${currentValue}% battery', unit: ""
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
    
        main(["motion", "temperature"])
        details(["motion", "temperature", "humidity", "illuminance", "battery", "refresh"])
    }
}

private List<Map> collectAttributes(Map descMap) {
    List<Map> descMaps = new ArrayList<Map>()

    descMaps.add(descMap)

    if (descMap.additionalAttrs) {
        descMaps.addAll(descMap.additionalAttrs)
    }

>>>>>>> * first release 
    return  descMaps
}

def parse(String description) {
<<<<<<< HEAD
    Map map = zigbee.getEvent(description)
    
=======
    log.debug "description: $description"
    Map map = zigbee.getEvent(description)
    //log.debug "map: $map"
>>>>>>> * first release 
    if (!map) {
        if (description?.startsWith('zone status')) {
            map = parseIasMessage(description)
        } else if (description?.startsWith('read attr')) {
            Map descMap = zigbee.parseDescriptionAsMap(description)
<<<<<<< HEAD
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
            }
        } else if (description?.startsWith('illuminance:')) { //parse illuminance
            map = parseCustomMessage(description)
        }
=======

            if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
                //log.info "BATT METRICS - attr: ${descMap?.attrInt}, value: ${descMap?.value}, decValue: ${Integer.parseInt(descMap.value, 16)}, currPercent: ${device.currentState("battery")?.value}, device: ${device.getDataValue("manufacturer")} ${device.getDataValue("model")}"
                List<Map> descMaps = collectAttributes(descMap)
                def battMap = descMaps.find { it.attrInt == 0x0020 }

                if (battMap) {
                    map = getBatteryResult(Integer.parseInt(battMap.value, 16))
                }
            } else if (descMap?.clusterInt == 0x0500 && descMap.attrInt == 0x0002 && descMap.commandInt != 0x07) {
                def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
                map = translateZoneStatus(zs)
            } else if (descMap?.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER && descMap.commandInt == 0x07) {
                if (descMap.data[0] == "00") {
                    //log.debug "TEMP REPORTING CONFIG RESPONSE: $descMap"
                    sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
                } else {
                    log.warn "TEMP REPORTING CONFIG FAILED- error code: ${descMap.data[0]}"
                }
            } else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS && descMap?.value) {
                map = translateZoneStatus(new ZoneStatus(zigbee.convertToInt(descMap?.value)))
            } else if (descMap?.clusterInt == 0x0406 && descMap.attrInt == 0 /*occupancy*/ && descMap?.value) {
                map = getMotionResult(descMap.value == "01" ? "active" : "inactive")
            }
        }
        if (description?.startsWith('illuminance:')) { //parse illuminance
            map = parseCustomMessage(description)            
        }    
>>>>>>> * first release 
    } else if (map.name == "temperature") {
        if (tempOffset) {
            map.value = new BigDecimal((map.value as float) + (tempOffset as float)).setScale(1, BigDecimal.ROUND_HALF_UP)
        }
        map.descriptionText = temperatureScale == 'C' ? "${device.displayName} temperature was ${map.value}°C" : "${device.displayName} temperature was ${map.value}°F"
        map.translatable = true
    } else if (map.name == "humidity") {
        if (humidityOffset) {
            map.value = (int) map.value + (int) humidityOffset
        }
        map.descriptionText = "${device.displayName} humidity was ${map.value}%"
        map.translatable = true
    }
<<<<<<< HEAD
    
    def result = map ? createEvent(map) : [:]
    
    if (description?.startsWith('enroll request')) {
        List cmds = zigbee.enrollResponse()
=======
   
    log.debug "Parse returned $map"
    def result = map ? createEvent(map) : [:]

    if (description?.startsWith('enroll request')) {
        List cmds = zigbee.enrollResponse()
        log.debug "enroll response: ${cmds}"
>>>>>>> * first release 
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    return result
}

<<<<<<< HEAD
private def parseCustomMessage(String description) {
=======
private def parseCustomMessage(String description) 
{
>>>>>>> * first release 
    return [
        name           : description.split(": ")[0],
        value          : description.split(": ")[1],
        translatable   : true
    ]
}

private Map parseIasMessage(String description) {
    ZoneStatus zs = zigbee.parseZoneStatus(description)
<<<<<<< HEAD
=======

>>>>>>> * first release 
    translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
    // Some sensor models that use this DTH use alarm1 and some use alarm2 to signify motion
    return (zs.isAlarm1Set() || zs.isAlarm2Set()) ? getMotionResult('active') : getMotionResult('inactive')
}

private Map getBatteryResult(rawValue) {
<<<<<<< HEAD
    def linkText = getLinkText(device)
    def result = [:]
    def volts = rawValue / 10
    
=======
    //log.debug "Battery rawValue = ${rawValue}"
    def linkText = getLinkText(device)

    def result = [:]

    def volts = rawValue / 10

>>>>>>> * first release 
    if (!(rawValue == 0 || rawValue == 255)) {
        result.name = 'battery'
        result.translatable = true
        def minVolts =  2.3
        def maxVolts =  3.2
<<<<<<< HEAD
        
=======
>>>>>>> * first release 
        // Get the current battery percentage as a multiplier 0 - 1
        def curValVolts = Integer.parseInt(device.currentState("battery")?.value ?: "100") / 100.0
        // Find the corresponding voltage from our range
        curValVolts = curValVolts * (maxVolts - minVolts) + minVolts
        // Round to the nearest 10th of a volt
        curValVolts = Math.round(10 * curValVolts) / 10.0
<<<<<<< HEAD
        
=======
>>>>>>> * first release 
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
<<<<<<< HEAD
        
        result.descriptionText = "${device.displayName} battery was ${result.value}%"
        state.lastVolts = volts
    }
=======
        result.descriptionText = "${device.displayName} battery was ${result.value}%"
        state.lastVolts = volts
    }

>>>>>>> * first release 
    return result
}

private Map getMotionResult(value) {
<<<<<<< HEAD
=======
    //log.debug 'motion'
>>>>>>> * first release 
    String descriptionText = value == 'active' ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
    return [
        name           : 'motion',
        value          : value,
        descriptionText: descriptionText,
        translatable   : true
    ]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
<<<<<<< HEAD
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
        refreshCmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)        
        refreshCmds +=  zigbee.enrollResponse()
    }
    
=======
    zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020)
}

def refresh() {
    log.debug "Refreshing Values"
    def refreshCmds = []

    refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) 
    if(isUSM300() || isTSM300())
    {
        refreshCmds += zigbee.readAttribute(zigbee.RELATIVE_HUMIDITY_CLUSTER, 0x0000)
        refreshCmds += zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000) 
    }   
    if(isUSM300())
    {
        refreshCmds += zigbee.readAttribute(0x0400/*illuminance*/, 0x0000) 
    }
    if(isUSM300() || isOSM300())
    {
        refreshCmds += zigbee.readAttribute(0x0406/*occupancy sensing*/, 0x0000)
        
        refreshCmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
        
        refreshCmds +=  zigbee.enrollResponse()
    }
    

>>>>>>> * first release 
    return refreshCmds
}

def configure() {
<<<<<<< HEAD
    def configCmds = []    
    
    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
=======
    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

    log.debug "Configuring Reporting"
>>>>>>> * first release 
    
    // temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
    // battery minReport 30 seconds, maxReportTime 6 hrs by default
    // humidity minReportTime 30 seconds, maxReportTime 60 min
    // illuminance minReportTime 30 seconds, maxReportTime 60 min
<<<<<<< HEAD
    // occupancy sensing minReportTime 10 seconds, maxReportTime 60 min
    // ex) zigbee.configureReporting(0x0001, 0x0020, DataType.UINT8, 600, 21600, 0x01)
    // This is for cluster 0x0001 (power cluster), attribute 0x0021 (battery level), whose type is UINT8, 
    // the minimum time between reports is 10 minutes (600 seconds) and the maximum time between reports is 6 hours (21600 seconds), 
    // and the amount of change needed to trigger a report is 1 unit (0x01).    
    configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE, DataType.UINT8, 30, 21600, 0x01/*100mv*1*/)
    
    if (isUSM300() || isTSM300()) {
        configCmds += zigbee.configureReporting(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, TEMPERATURE_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE, DataType.INT16, 30, 300, 30/*30/100=0.3도*/)
        configCmds += zigbee.configureReporting(zigbee.RELATIVE_HUMIDITY_CLUSTER, RALATIVE_HUMIDITY_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE, DataType.UINT16, 30, 3600, 50/*50/100=0.5%*/)
    }
    
    if (isUSM300()) {
        configCmds += zigbee.configureReporting(ILLUMINANCE_MEASUREMENT_CLUSTER, ILLUMINANCE_MEASUREMENT_MEASURED_VALUE_ATTRIBUTE, DataType.UINT16, 30, 3600, 20/*20 lux*/)
    }
    
    if (isUSM300() || isOSM300()) {
        configCmds += zigbee.configureReporting(OCCUPANCY_SENSING_CLUSTER, OCCUPANCY_SENSING_OCCUPANCY_ATTRIBUTE, DataType.BITMAP8, 1, 600, 1)
    }
    
=======
     // occupancy sensing minReportTime 10 seconds, maxReportTime 60 min
    def configCmds = []
    
    //ex) zigbee.configureReporting(0x0001, 0x0020, DataType.UINT8, 600, 21600, 0x01)
    //This is for cluster 0x0001 (power cluster), attribute 0x0021 (battery level), whose type is UINT8, 
    // the minimum time between reports is 10 minutes (600 seconds) and the maximum time between reports is 6 hours (21600 seconds), 
    // and the amount of change needed to trigger a report is 1 unit (0x01).
    
    configCmds += zigbee.configureReporting(0x0001/*power*/, 0x0020, DataType.UINT8, 30, 21600, 0x01/*100mv*1*/) 
    if(isUSM300() || isTSM300())
    {
        configCmds += zigbee.configureReporting(0x0402/*temperature*/, 0x0000, DataType.INT16, 30, 300, 30/*30/100=0.3도*/) 
        configCmds += zigbee.configureReporting(zigbee.RELATIVE_HUMIDITY_CLUSTER/*0x0405*/, 0x0000, DataType.UINT16, 30, 3600, 50/*50/100=0.5%*/) 
    }
    if(isUSM300())  
    {
        configCmds += zigbee.configureReporting(0x0400/*illuminance*/, 0x0000, DataType.UINT16, 30, 3600, 20/*20 lux*/) 
    }
    if(isUSM300() || isOSM300())    
    {
        configCmds += zigbee.configureReporting(0x0406/*occupancy sensing*/, 0x0000, 0x18/*bitmap8*/, 1, 600, 1)
    }
>>>>>>> * first release 
    return refresh() + configCmds
}

private Boolean isUSM300() {
    device.getDataValue("model") == "USM-300Z"
}
<<<<<<< HEAD

private Boolean isTSM300() {
    device.getDataValue("model") == "TSM-300Z"
}

=======
private Boolean isTSM300() {
    device.getDataValue("model") == "TSM-300Z"
}
>>>>>>> * first release 
private Boolean isOSM300() {
    device.getDataValue("model") == "OSM-300Z"
}