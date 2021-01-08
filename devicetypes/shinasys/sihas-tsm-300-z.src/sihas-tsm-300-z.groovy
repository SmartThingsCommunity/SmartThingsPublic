/*
 *  Copyright 2018 SmartThings
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
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition (name: "SiHAS TSM-300-Z", namespace: "shinasys", author: "Shina System Co., Ltd", vid: "generic-humidity") {
        capability "Configuration"
        capability "Battery"
        capability "Refresh" 
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Health Check"
        capability "Sensor"
        
        fingerprint inClusters: "0000,0001,0003,0004,0402,0405", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "TSM-300Z", deviceJoinName: "SiHAS Temperature/Humidity Sensor"
    }

    // simulator metadata
    simulator {
   
    }

    preferences {
        
        section {
            input "tempOffset", "number", title: "Temperature offset", description: "Select how many degrees to adjust the temperature.", range: "-100..100", displayDuringSetup: false
            input "humidityOffset", "number", title: "Humidity offset", description: "Enter a percentage to adjust the humidity.", range: "*..*", displayDuringSetup: false
        }
    }

    tiles(scale: 2) {
   
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state("temperature", label:'${currentValue}°',
                backgroundColors:[
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
        valueTile("humidity", "device.humidity") {
            state "humidity", label:'${currentValue}%', unit:"%"
        }
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label: '${currentValue}% battery', unit: "%"
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main(["temperature", "humidity"])
        details(["temperature", "humidity", "battery", "refresh"])
        
    }
}

private List<Map> collectAttributes(Map descMap) {
    List<Map> descMaps = new ArrayList<Map>()

    descMaps.add(descMap)

    if (descMap.additionalAttrs) {
        descMaps.addAll(descMap.additionalAttrs)
    }

    return  descMaps
}

def parse(String description) {
    log.debug "description: $description"
    Map map = zigbee.getEvent(description)
    if (!map) {
        if (description?.startsWith('read attr')) {
            Map descMap = zigbee.parseDescriptionAsMap(description)

            if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
                //log.info "BATT METRICS - attr: ${descMap?.attrInt}, value: ${descMap?.value}, decValue: ${Integer.parseInt(descMap.value, 16)}, currPercent: ${device.currentState("battery")?.value}, device: ${device.getDataValue("manufacturer")} ${device.getDataValue("model")}"
                List<Map> descMaps = collectAttributes(descMap)
                def battMap = descMaps.find { it.attrInt == 0x0020 }

                if (battMap) {
                    map = getBatteryResult(Integer.parseInt(battMap.value, 16))
                }
            }
            else if (descMap?.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER && descMap.commandInt == 0x07) {
                if (descMap.data[0] == "00") {
                    //log.debug "TEMP REPORTING CONFIG RESPONSE: $descMap"
                    sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
                } else {
                    log.warn "TEMP REPORTING CONFIG FAILED- error code: ${descMap.data[0]}"
                }
            }
        }
    }    
    else if (map.name == "temperature") {
	    if (tempOffset) {
	        map.value = new BigDecimal((map.value as float) + (tempOffset as float)).setScale(1, BigDecimal.ROUND_HALF_UP)
	    }
        map.unit = "C"
        map.descriptionText = "${device.displayName} temperature was ${map.value}°C"
        map.translatable = true        
    } else if (map.name == "humidity") {
        if (humidityOffset) {
            map.value = (int) map.value + (int) humidityOffset
        }
        map.descriptionText = "${device.displayName} humidity was ${map.value}%"
        map.translatable = true
    }
    
    log.debug "Parse returned $map"
    def result = map ? createEvent(map) : [:]

    
    return result
}

private def parseCustomMessage(String description) 
{
    return [
        name           : description.split(": ")[0],
        value          : description.split(": ")[1],
        translatable   : true
    ]
}


private Map getBatteryResult(rawValue) {
    //log.debug "Battery rawValue = ${rawValue}"
    def linkText = getLinkText(device)

    def result = [:]

    def volts = rawValue / 10

    if (!(rawValue == 0 || rawValue == 255)) {
        result.name = 'battery'
        result.translatable = true
        result.unit = "%"
        def minVolts =  2.3
        def maxVolts =  3.2
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



/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    zigbee.readAttribute(0x0402/*temperature measurement*/, 0x0000)
}

def refresh() {
    log.debug "Refreshing Values"
    def refreshCmds = []

    refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) +
        zigbee.readAttribute(zigbee.RELATIVE_HUMIDITY_CLUSTER, 0x0000) +
        zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000) 

    return refreshCmds
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

    log.debug "Configuring Reporting"
    
    // temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
    // battery minReport 30 seconds, maxReportTime 6 hrs by default
    // humidity minReportTime 30 seconds, maxReportTime 60 min
    def configCmds = []
    
    //ex) zigbee.configureReporting(0x0001, 0x0020, DataType.UINT8, 600, 21600, 0x01)
    //This is for cluster 0x0001 (power cluster), attribute 0x0021 (battery level), whose type is UINT8, 
    // the minimum time between reports is 10 minutes (600 seconds) and the maximum time between reports is 6 hours (21600 seconds), 
    // and the amount of change needed to trigger a report is 1 unit (0x01).
    
    configCmds += 
        zigbee.configureReporting(0x0001/*power*/, 0x0020, DataType.UINT8, 30, 21600, 0x01/*100mv*1*/) +
        zigbee.configureReporting(0x0402/*temperature*/, 0x0000, DataType.INT16, 30, 300, 30/*30/100=0.3 degree*/) +
        zigbee.configureReporting(zigbee.RELATIVE_HUMIDITY_CLUSTER/*0x0405*/, 0x0000, DataType.UINT16, 30, 3600, 50/*50/100=0.5%*/) 
    
    return refresh() + configCmds
}