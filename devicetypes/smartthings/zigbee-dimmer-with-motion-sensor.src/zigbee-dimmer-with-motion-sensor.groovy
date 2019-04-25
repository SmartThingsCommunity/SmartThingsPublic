/**
 *  Copyright 2018 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus

metadata {
	definition (name: "ZigBee Dimmer with Motion Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true) {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Light"
		capability "Switch"
		capability "Switch Level"
		capability "Motion Sensor"
		capability "Health Check"

		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0500, 0702, 0B05, FC01", outClusters: "0019", manufacturer: "sengled", model: "E13-N11", deviceJoinName: "Sengled Element Classic PAR38 Motion Sensor"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0500, 0702, 0B05, FC01", outClusters: "0019", manufacturer: "sengled", model: "E13-A21", deviceJoinName: "Sengled Smart LED Motion Sensor PAR38 Bulb"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
		standardTile("motion", "device.motion", decoration: "flat", width: 2, height: 2) {
			state "active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
			state "inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#cccccc"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "switch"
		details(["switch", "motion", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description: $description"

	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status')) {
			map = parseIasMessage(description)
		} else {
			def descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap && descMap.clusterInt == 0x0006 && descMap.commandInt == 0x07) {
				if (descMap.data[0] == "00") {
					log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
					sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
				} else {
					log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
				}
			} else if (device.getDataValue("manufacturer") == "sengled" && descMap && descMap.clusterInt == 0x0008 && descMap.attrInt == 0x0000) {
				// This is being done because the sengled element touch/classic incorrectly uses the value 0xFF for the max level.
				// Per the ZCL spec for the UINT8 data type 0xFF is an invalid value, and 0xFE should be the max.  Here we
				// manually handle the invalid attribute value since it will be ignored by getEvent as an invalid value.
				// We also set the level of the bulb to 0xFE so future level reports will be 0xFE until it is changed by
				// something else.
				if (descMap.value.toUpperCase() == "FF") {
					descMap.value = "FE"
				}
				sendHubCommand(zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, 0x00, "FE0000").collect { new physicalgraph.device.HubAction(it) }, 0)
				map = zigbee.getEventFromAttrData(descMap.clusterInt, descMap.attrInt, descMap.encoding, descMap.value)
			} else if (descMap?.clusterInt == 0x0500 && descMap.attrInt == 0x0002) {
				def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
				map = translateZoneStatus(zs)
			} else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS && descMap?.value) {
				map = translateZoneStatus(new ZoneStatus(zigbee.convertToInt(descMap?.value)))
			} else {
				log.warn "DID NOT PARSE MESSAGE for description : $description"
				log.debug "${descMap}"
			}
		}
	} else if (map.name == "level" && map.value == 0) {
		map = [:]
	}

	log.debug "Parse returned $map"
	def result = map ? createEvent(map) : [:]

	return result
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)

	return translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
	// Some sensor models that use this DTH use alarm1 and some use alarm2 to signify motion
	return getMotionResult(zs.isAlarm1Set() || zs.isAlarm2Set())
}

private Map getMotionResult(value) {
	def descriptionText = value ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
	return [
			name			: 'motion',
			value			: value ? 'active' : 'inactive',
			descriptionText : descriptionText,
			translatable	: true
	]
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def setLevel(value, rate = null) {
	zigbee.setLevel(value)
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.onOffRefresh() + zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def refresh() {
	zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def setupHealthCheck() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def installed() {
	setupHealthCheck()
}

def configure() {
	log.debug "Configuring Reporting and Bindings."
	setupHealthCheck()

	// OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
	zigbee.onOffConfig(0, 300) + zigbee.levelConfig() + zigbee.enrollResponse() + refresh()
}
