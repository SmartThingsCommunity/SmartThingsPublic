/**
 *  Copyright 2017 SmartThings
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
 */
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "ZLL RGB Bulb", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", runLocally: true, minHubCoreVersion: '000.021.00001', executeCommandsLocally: true) {

		capability "Actuator"
		capability "Color Control"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Health Check"
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
private getMOVE_TO_HUE_AND_SATURATION_COMMAND() { 0x06 }
private getCOLOR_CONTROL_CLUSTER() { 0x0300 }

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	def finalResult = zigbee.getEvent(description)
	if (finalResult) {
		log.debug finalResult
		sendEvent(finalResult)
	}
	else {
		def zigbeeMap = zigbee.parseDescriptionAsMap(description)
		log.trace "zigbeeMap : $zigbeeMap"

		if (zigbeeMap?.clusterInt == COLOR_CONTROL_CLUSTER) {
			if(zigbeeMap.attrInt == ATTRIBUTE_HUE){  //Hue Attribute
				def hueValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 0xfe * 100)
				sendEvent(name: "hue", value: hueValue, displayed:false)
			}
			else if(zigbeeMap.attrInt == ATTRIBUTE_SATURATION){ //Saturation Attribute
				def saturationValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 0xfe * 100)
				sendEvent(name: "saturation", value: saturationValue, displayed:false)
			}
		}
		else {
			log.info "DID NOT PARSE MESSAGE for description : $description"
		}
	}
}

def on() {
	zigbee.on() + ["delay 1500"] + zigbee.onOffRefresh()
}

def off() {
	zigbee.off() + ["delay 1500"] + zigbee.onOffRefresh()
}

def refresh() {
	refreshAttributes() + configureAttributes()
}

def poll() {
	configureHealthCheck()

	refreshAttributes()
}

def configure() {
	log.debug "Configuring Reporting and Bindings."
	configureAttributes() + refreshAttributes()
}

def ping() {
	refreshAttributes()
}

def healthPoll() {
	log.debug "healthPoll()"
	def cmds = refreshAttributes()
	cmds.each{ sendHubCommand(new physicalgraph.device.HubAction(it))}
}

def configureHealthCheck() {
	if (!state.hasConfiguredHealthCheck) {
		log.debug "Configuring Health Check, Reporting"
		unschedule("healthPoll", [forceForLocallyExecuting: true])
		runEvery5Minutes("healthPoll", [forceForLocallyExecuting: true])
		state.hasConfiguredHealthCheck = true
	}
}

def configureAttributes() {
	zigbee.onOffConfig() +
	zigbee.levelConfig()
}

def refreshAttributes() {
	zigbee.onOffRefresh() +
	zigbee.levelRefresh() +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE) +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
}

def updated() {
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	configureHealthCheck()
}

def installed() {
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	configureHealthCheck()
}

def setLevel(value) {
	zigbee.setLevel(value) + zigbee.onOffRefresh() + zigbee.levelRefresh()         //adding refresh because of ZLL bulb not conforming to send-me-a-report
}

private getScaledHue(value) {
	zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
}

private getScaledSaturation(value) {
	zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
}

def setColor(value){
	log.trace "setColor($value)"
	zigbee.on() +
	zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_HUE_AND_SATURATION_COMMAND,
		getScaledHue(value.hue), getScaledSaturation(value.saturation), "0000") +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE) +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
}

def setHue(value) {
	//payload-> hue value, direction (00-> shortest distance), transition time (1/10th second)
	zigbee.command(COLOR_CONTROL_CLUSTER, HUE_COMMAND, getScaledHue(value), "00", "0000") +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE)
}

def setSaturation(value) {
	//payload-> sat value, transition time
	zigbee.command(COLOR_CONTROL_CLUSTER, SATURATION_COMMAND, getScaledSaturation(value), "0000") +
	zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
}
