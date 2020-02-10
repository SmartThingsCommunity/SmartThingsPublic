/*
 *  Copyright 2016 SmartThings
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

metadata {
	definition(name: "Ecolink ZigBee Door/Window Sensor", namespace: "Ecolink", author: "Ecolink") {
		capability "Contact Sensor"
		capability "Configuration"
		capability "Battery"
		capability "Temperature Measurement"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"

		command "enrollResponse"
        
        attribute "tamper", "enum", ["detected", "clear"]

		//fingerprints
        fingerprint inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019", manufacturer: "Ecolink", model: "DWZB1-ECO"//, deviceJoinName: "Ecolink Door/Window"

	}

	simulator {
		status "active": "zone report :: type: 19 value: 0031"
		status "inactive": "zone report :: type: 19 value: 0030"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "contact", type: "generic", width: 6, height: 4) {
			tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", label: 'open', icon: "st.contact.contact.open", backgroundColor: "#FB902C"
				attributeState "closed", label: 'closed', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
			}
		}
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label: '${currentValue}°', unit: "F",
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
		valueTile("battery", "device.battery", decoration: "ring", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: "",
            backgroundColors: [
							[value: 25, color: "#FF0000"],
							[value: 74, color: "#FBFB09"],
							[value: 96, color: "#44b621"]
					]
		}       
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main(["contact", "temperature"])
		details(["contact", "temperature", "battery", "refresh"])
	}
}

def parse(String description) {
	log.debug "contact sensor description: $description"
	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status')) {
			map = parseIasMessage(description)
		} 
        else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == 0x0001 && descMap.commandInt != 0x07 && descMap?.value) {
				map = getBatteryResult(Integer.parseInt(descMap.value, 16))
			} 
            else if (descMap?.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER && descMap.commandInt == 0x07) {
				if (descMap.data[0] == "00") {
					//log.debug "TEMP REPORTING CONFIG RESPONSE: $descMap"
                    sendEvent(name: "checkInterval", value: 60 * 60, displayed: true, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
				} 
                else {
					log.warn "TEMP REPORTING CONFIG FAILED- error code: ${descMap.data[0]}"
				}
			} 
            else if (descMap.clusterInt == 0x0406 && descMap.attrInt == 0x0000) {
				def value = descMap.value.endsWith("01") ? "open" : "closed"
				map = getContactResult(value)
			}
            else if(descMap.clusterInt == 0x0020 && descMap.commandInt == 0x00)
            {
            	log.debug "Check in interval command received!"
                //sendEvent(name: "checkInterval", value: 60 * 60, displayed: true, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
                // Read the Battery Level and temperature
                List cmd =  zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) +
                            zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000)
                log.debug "read attributes: ${cmd}"
                return cmd?.collect { new physicalgraph.device.HubAction(it) }
            }
		}
	} else if (map.name == "temperature") {
		if (tempOffset) {
			map.value = (int) map.value + (int) tempOffset
		}
		map.descriptionText = temperatureScale == 'C' ? '{{ device.displayName }} was {{ value }}°C' : '{{ device.displayName }} was {{ value }}°F'
		map.translatable = true
	}

	log.debug "Parse returned $map"
	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
		List cmds = zigbee.enrollResponse()
		log.debug "enroll response: ${cmds}"
        configure()
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	return result
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)

	//check for tamper bit, send message to log if tampered/tamper cleared
    if(zs.tamper != 0)
    {
    	sendEvent(name: "tamper", value: "detected")
    }
    else if(zs.tamper != 1)
    {
    	sendEvent(name: "tamper", value: "clear")
    }
    
    // Some sensor models that use this DTH use alarm1 and some use alarm2 to signify motion
	return (zs.isAlarm1Set() || zs.isAlarm2Set()) ? getContactResult('open') : getContactResult('closed')
}

private Map getBatteryResult(rawValue) {
	log.debug "Battery rawValue = ${rawValue}"
	def linkText = getLinkText(device)

	def result = [:]

	def volts = rawValue / 10

	if (!(rawValue == 0 || rawValue == 255)) {
		result.name = 'battery'
		result.translatable = true
		result.descriptionText = "{{ device.displayName }} battery was {{ value }}%"

        def minVolts = 2.1
        def maxVolts = 3.0
        def pct = (volts - minVolts) / (maxVolts - minVolts)
        def roundedPct = Math.round(pct * 100)
        if (roundedPct <= 0)
        roundedPct = 1
        result.value = Math.min(100, roundedPct)
        
        //alert end user of low battery if batt < ~2.4V
        if(result.value <= 25)
        {
        	result.descriptionText = "$device.displayName low battery alert!"
        }
	}
    //if we get 0 its because the sensor didnt read the voltage prior to learning in. Assume battery is 100%
    else if(rawValue == 0)
    {
    	result.name = 'battery'
		result.translatable = true
		result.descriptionText = "{{ device.displayName }} battery was {{ value }}%"
        result.value = 100
    }

	return result
}

private Map getContactResult(value) {
	log.debug 'contact'
	String descriptionText = value == 'open' ? "{{ device.displayName }} contact opened" : "{{ device.displayName }} contact closed"
	return [
			name           : 'contact',
			value          : value,
			descriptionText: descriptionText,
			translatable   : true
	]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    
    log.debug "ping called"
    
    // Read the Battery Level and temperature
    return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) + 
    		zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000)
            
}

def refresh() {
	log.debug "refresh called"

	//read battery voltage and temperature
	def refreshCmds = zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) +
			zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000) +
            zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, 0x0002)
    
    return refreshCmds
}

def configure() {
	log.debug "Configure Function Called"
    
    // Device-Watch allows 2 check-in misses from device
	sendEvent(name: "checkInterval", value: 60 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    
    //create binding for poll control cluster
    def createPollingBinding = ["zdo bind 0x${device.deviceNetworkId} 1 1 0x0020 {${device.zigbeeId}} {}", "delay 1500",
        						"send 0x${device.deviceNetworkId} 1 1", "delay 1500"]
                                
    //5 minute long poll and 30 minute check in intervals
    def enrollCmds = (zigbee.writeAttribute(0x0020, 0x0000, 0x23, 0x00001C20) + zigbee.command(0x0020, 0x03, "0200") + 
    				  zigbee.writeAttribute(0x0020, 0x0003, 0x21, 0x0028) + zigbee.command(0x0020, 0x02, "B1040000"))
                      
    //send enroll response, create binding for polling cluster, configure polling of the device, and read device status attributes
    //return zigbee.enrollResponse() + createPollingBinding + refresh() + enrollCmds
	return createPollingBinding + refresh() + enrollCmds
}