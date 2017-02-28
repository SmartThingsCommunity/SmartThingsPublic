/**
 *  Copyright 2016 SmartThings
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
 *  Author: SmartThings
 *  Date: 2016-01-19
 *
 *  This DTH should serve as the generic DTH to handle RGB ZigBee HA devices (For color bulbs with no color temperature)
 */
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "ZigBee RGB Bulb", namespace: "smartthings", author: "SmartThings") {

		capability "Actuator"
		capability "Color Control"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Health Check"
		capability "Light"

		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Gardenspot RGB", deviceJoinName: "SYLVANIA Smart Gardenspot mini RGB"
		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY Gardenspot RGB", deviceJoinName: "SYLVANIA Smart Gardenspot mini RGB"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"color control.setColor"
			}
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		details(["switch", "refresh"])
	}
}

//Globals
private getATTRIBUTE_HUE() { 0x0000 }
private getATTRIBUTE_SATURATION() { 0x0001 }
private getHUE_COMMAND() { 0x00 }
private getSATURATION_COMMAND() { 0x03 }
private getCOLOR_CONTROL_CLUSTER() { 0x0300 }

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	def event = zigbee.getEvent(description)
	if (event) {
		log.debug event
		if (event.name=="level" && event.value==0) {}
		else {
			sendEvent(event)
		}
	}
	else {
		def zigbeeMap = zigbee.parseDescriptionAsMap(description)
		def cluster = zigbee.parse(description)

		if (zigbeeMap?.clusterInt == COLOR_CONTROL_CLUSTER) {
			if(zigbeeMap.attrInt == ATTRIBUTE_HUE){  //Hue Attribute
				def hueValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 255 * 100)
				sendEvent(name: "hue", value: hueValue, descriptionText: "Color has changed")
			}
			else if(zigbeeMap.attrInt == ATTRIBUTE_SATURATION){ //Saturation Attribute
				def saturationValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 255 * 100)
				sendEvent(name: "saturation", value: saturationValue, descriptionText: "Color has changed", displayed: false)
			}
		}
		else if (cluster && cluster.clusterId == 0x0006 && cluster.command == 0x07) {
			if (cluster.data[0] == 0x00){
				log.debug "ON/OFF REPORTING CONFIG RESPONSE: $cluster"
				sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
			}
			else {
				log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
			}
		}
		else {
			log.info "DID NOT PARSE MESSAGE for description : $description"
			log.debug zigbeeMap
		}
	}
}

def on() {
	zigbee.on()
}

def off() {
	zigbee.off()
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.onOffRefresh()
}

def refresh() {
	zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE) + zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION) + zigbee.onOffConfig(0, 300) + zigbee.levelConfig() + zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE, DataType.UINT8, 1, 3600, 0x01) + zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION, DataType.UINT8, 1, 3600, 0x01)
}

def configure() {
	log.debug "Configuring Reporting and Bindings."
	// Device-Watch allows 3 check-in misses from device (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 3 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	// OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
	zigbee.onOffConfig(0, 300) + zigbee.levelConfig() + zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE, DataType.UINT8, 1, 3600, 0x01) + zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION, DataType.UINT8, 1, 3600, 0x01) + zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE) + zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
}

def setLevel(value) {
	zigbee.setLevel(value)
}

def setColor(value){
	log.trace "setColor($value)"
	zigbee.on() + setHue(value.hue) + "delay 500" + setSaturation(value.saturation)
}

def setHue(value) {
	def scaledHueValue = zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
	zigbee.command(COLOR_CONTROL_CLUSTER, HUE_COMMAND, scaledHueValue, "00", "0500")       //payload-> hue value, direction (00-> shortest distance), transition time (1/10th second) (0500 in U16 reads 5)
}

def setSaturation(value) {
	def scaledSatValue = zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
	zigbee.command(COLOR_CONTROL_CLUSTER, SATURATION_COMMAND, scaledSatValue, "0500") + "delay 1000" + zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
}