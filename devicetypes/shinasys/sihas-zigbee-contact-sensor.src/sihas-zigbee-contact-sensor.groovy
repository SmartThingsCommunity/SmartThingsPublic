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
    definition (name: "SiHAS Zigbee Contact Sensor", namespace: "shinasys", author: "SHINA SYSTEM", mnmn: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: true ) {
        capability "Battery"
	    capability "Configuration"
        capability "Contact Sensor"
        capability "Refresh"
        capability "Health Check"
        capability "Sensor"
        
        fingerprint inClusters: "0000,0001,0003,0020,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "DSM-300Z", deviceJoinName: "SiHAS Contact Sensor", mnmn: "SmartThings", vid: "generic-contact-3", ocfDeviceType: "x.com.st.d.sensor.contact"
    }
}

private getPOWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE() { 0x0020 }

private List<Map> collectAttributes(Map descMap) {
    List<Map> descMaps = new ArrayList<Map>()
    descMaps.add(descMap)
    if (descMap.additionalAttrs) {
        descMaps.addAll(descMap.additionalAttrs)
    }
    return  descMaps
}

def parse(String description) {
	//log.debug "description: $description"
    
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
            }
        }
    }
    
    def result = map ? createEvent(map) : [:]
    
    if (description?.startsWith('enroll request')) {
        List cmds = zigbee.enrollResponse()
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    
    return result
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
    return (zs.isAlarm1Set() || zs.isAlarm2Set()) ? getContactResult('open') : getContactResult('closed')    
}

private Map getBatteryResult(rawValue) {
    def linkText = getLinkText(device)
    def result = [:]
    def volts = rawValue / 10
    if (!(rawValue == 0 || rawValue == 255)) {
        result.name = 'battery'
        result.translatable = true
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

private Map getContactResult(value) {
	def linkText = getLinkText(device)
	def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
	return [
		name: 'contact',
		value: value,
		descriptionText: descriptionText
	]
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
    refreshCmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)        
    refreshCmds +=  zigbee.enrollResponse()
    
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
    configCmds += zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 0, 60 * 60, null)
    
    return refresh() + configCmds
}