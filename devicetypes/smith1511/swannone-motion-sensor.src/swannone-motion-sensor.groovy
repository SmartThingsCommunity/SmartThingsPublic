/**
 *  Copyright 2016 Christian Smith (smith1511)
 *
 *  Name: SwannOne Motion Sensor
 *
 *  Author: Christian Smith (smith1511)
 *
 *  Date: 2017-11-01
 *
 *  Version: 1.00
 *
 *  Description:
 *   - This device handler is written specifically for the Aeon Home Energy Meter Gen2 UK version, with a single clamp.
 *   - Supports live reporting of energy, power, current, and voltage. Press the 'Now' tile to refresh.
 *      (voltage tile is not shown by default, but you can enable it below).
 *   - Supports reporting of energy usage and cost over an ad hoc period, based on the 'energy' figure reported by 
 *     the device. Press the 'Since...' tile to reset.
 *   - Supports additional reporting of energy usage and cost over multiple pre-defined periods:
 *       'Today', 'Last 24 Hours', 'Last 7 Days', 'This Month', 'This Year', and 'Lifetime'
 *     These can be cycled through by pressing the 'statsMode' tile. 
 *   - There's a tile that will reset all Energy Stats periods, but it's hidden by default.
 *   - Key device parameters can be set from the device settings. Refer to the Aeon HEMv2 instruction 
 *     manual for full details.
 *   - If you are re-using this device, please use your own hosting for the icons.
 *
 *  Version History:
 *
 *   2017-11-01: v1.00:
 *    - Initial release
 * 
 *  To Do:
 *   - Verify that temperature measurement works correctly.
 *
 *  License:
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *   for the specific language governing permissions and limitations under the License.
 *
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus


metadata {
	definition(name: "SwannOne Motion Sensor", namespace: "smith1511", author: "Christian Smith") {
		capability "Motion Sensor"
		capability "Configuration"
		capability "Battery"
		capability "Temperature Measurement"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"

		command "enrollResponse"

		fingerprint inClusters: "0000,0001,0002,0003,0500,FC00,0402", outClusters: "0003", manufacturer: "SwannOne", model: "SWO-MOS1PA", deviceJoinName: "Motion Sensor"
	}

	simulator {
		status "active": "zone status 0x0031 -- extended status 0x00"
		status "inactive": "zone status 0x0030 -- extended status 0x00"
        status "temp1": "catchall: 0104 0402 01 01 0100 00 481F 00 00 0000 01 01 000086"
        status "temp2": "catchall: 0104 0500 01 01 0140 00 481F 00 00 0000 04 01 00"
        status "temp3": "catchall: 0104 0500 01 01 0140 00 481F 00 00 0000 0B 01 0000"
	}

	preferences {
		section {
			image(name: 'educationalcontent', multiple: true, images: [
					"http://cdn.device-gse.smartthings.com/Motion/Motion1.jpg",
					"http://cdn.device-gse.smartthings.com/Motion/Motion2.jpg",
					"http://cdn.device-gse.smartthings.com/Motion/Motion3.jpg"
			])
		}
		section {
			input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter '-5'. If 3 degrees too cold, enter '+3'.", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
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
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main(["motion", "temperature"])
		details(["motion", "temperature", "battery", "refresh"])
	}
}

def parse(String description) {

	log.debug "description: $description"
    
	def event = zigbee.getEvent(description)
    if (event) {
        log.debug "Event: $event"
        return event
    }
    
	if (description?.startsWith('zone status')) {
    
        // Zone Status
        event = getMotionEvent(description)
        
    } else if ((description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))) {
        def descMap = zigbee.parseDescriptionAsMap(description)
        
        log.debug "descMap: $descMap"
        
        if (descMap.clusterInt == 0x0001) {
            
            // Power Configuration
            log.debug "Power Configuraton"
            
        } else if (descMap.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER) {
            
            // Temp. Measurement
            log.debug "Temperature Measurement"
            event = getTemperatureEvent(descMap.resultCode)
            
        } else if (descMap.clusterInt == 0x0500) {
        
            // IAS Zone
            log.debug "IAS Zone"
        }
    }
    
    log.debug "result: $event"

	return event
}

private Map getMotionEvent(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	def map = zs.isAlarm1Set() ? getMotionMap('active') : getMotionMap('inactive')
    return createEvent(map)
}

private Map getMotionMap(value) {
	String descriptionText = value == 'active' ? "{{ device.displayName }} detected motion" : "{{ device.displayName }} motion has stopped"
	return [
        name           : 'motion',
        value          : value,
        descriptionText: descriptionText,
        translatable   : true
	]
}

private Map getTemperatureEvent(value) {
	String descriptionText = temperatureScale == 'C' ? '{{ device.displayName }} was {{ value }}°C' : '{{ device.displayName }} was {{ value }}°F'
    def temperature = tempOffset ? (int) value + (int) tempOffset : value
	def map = [
        name           : 'temperature',
        value          : temperature,
        descriptionText: descriptionText,
        translatable   : true
	]
    return createEvent(map)
}

def ping() {
    log.debug "Ping"
	return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) // Read the Battery Level
}

def refresh() {
	log.debug "Refresh"
	return zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000) + zigbee.enrollResponse()
}

def configure() {
    log.debug "Configure"
	sendEvent(name: "checkInterval", value: 12 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    return refresh() + zigbee.batteryConfig() + zigbee.temperatureConfig(30, 120)
}
