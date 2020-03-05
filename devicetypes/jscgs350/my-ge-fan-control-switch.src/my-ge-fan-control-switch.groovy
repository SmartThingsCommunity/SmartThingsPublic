/**
 *  A better functional Device Type for Z-Wave Smart Fan Control Switches, particularly the GE 12730 device.
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
 *  Updates:
 *  -------
 *  02-18-2016 : Initial commit
 *  03-11-2016 : Due to ST's v2.1.0 app totally hosing up SECONDARY_CONTROL, implemented a workaround to display that info in a separate tile.
 *  08-14-2016 : Completely changed the code to use ST's updated DH for "dimmer switch".  Did not reimplement "adjusting" state.
 *  08-28-2016 : Made some cosmetic changes, and fixed the low/med/high reporting to properly reflect any physical adjustment at the switch.
 *  01-08-2017 : Added code for Health Check capabilities/functions, and cleaned up code.
 *  03-11-2017 : Changed from valueTile to standardTile for a few tiles since ST's mobile app v2.3.x changed something between the two.
 *  08-31-2017 : Changed to ST's color scheme.
 *  09-01-2017 : Updated to use ST's latest version of the dimmer switch code.
 *  11-03-2017 : Cleaned up code for fan speed.
 *  11-30-2017 : Turned off Health Check for now.
 *  01-26-2018 : Added dimmer level to the main tile (this may get removed later)
 *
 */
 
metadata {
	definition (name: "My GE Fan Control Switch", namespace: "jscgs350", author: "jscgs350", ocfDeviceType: "oic.d.switch", vid:"generic-dimmer") {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Health Check"

		command "lowSpeed"
		command "medSpeed"
		command "highSpeed"

		attribute "currentState", "string"

	}

	preferences {
		input "ledIndicator", "enum", title: "LED Indicator", description: "Turn LED indicator... ", required: false, options:["on": "When On", "off": "When Off", "never": "Never"], defaultValue: "off"
        section("Fan Thresholds") {
			input "lowThreshold", "number", title: "Low Threshold (typical is 1-33)", range: "1..99", defaultValue: 33
			input "medThreshold", "number", title: "Medium Threshold (typical is 34-67)", range: "1..99", defaultValue: 67
			input "highThreshold", "number", title: "High Threshold (typical is 68-99)", range: "1..99", defaultValue: 99
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {    
				attributeState "on", action:"switch.off", label:'ON', icon:"st.Lighting.light24", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", action:"switch.on", label:'OFF', icon:"st.Lighting.light24", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'TURNINGON', icon:"st.Lighting.light24", backgroundColor:"#f0b823", nextState: "turningOn"
				attributeState "turningOff", label:'TURNINGOFF', icon:"st.Lighting.light24", backgroundColor:"#f0b823", nextState: "turningOff"
			}   
            tileAttribute ("device.currentState", key: "SECONDARY_CONTROL") {
           		attributeState "LOW", label:'Fan speed set to LOW', icon:"st.Lighting.light24"
                attributeState "MED", label:'Fan speed set to MEDIUM', icon:"st.Lighting.light24"
                attributeState "HIGH", label:'Fan speed set to HIGH', icon:"st.Lighting.light24"
            }
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
		standardTile("lowSpeed", "device.currentState", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "LOW", label:'LOW', action: "lowSpeed", icon:"st.Home.home30"
  		}
		standardTile("medSpeed", "device.currentState", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "MED", label: 'MED', action: "medSpeed", icon:"st.Home.home30"
		}
		standardTile("highSpeed", "device.currentState", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "HIGH", label: 'HIGH', action: "highSpeed", icon:"st.Home.home30"
		}
		standardTile("indicator", "device.indicatorStatus", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
			state "never", action:"indicator.indicatorWhenOff", icon:"st.indicators.never-lit"
		}
		standardTile("refresh", "device.switch", width: 6, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}
		main(["switch"])
		details(["switch", "lowSpeed", "medSpeed", "highSpeed", "refresh"])
	}
}

def installed() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def updated(){
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
  	switch (ledIndicator) {
            case "on":
                indicatorWhenOn()
                break
            case "off":
                indicatorWhenOff()
                break
            case "never":
                indicatorNever()
                break
            default:
                indicatorWhenOn()
                break
    }
}

def getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x26: 1,  // SwitchMultilevel
		0x56: 1,  // Crc16Encap
		0x70: 1,  // Configuration
	]
}

def parse(String description) {
	def result = null
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
//		def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
        def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
            if (cmd.value > 0) {
                if (cmd.value <= lowThreshold) {
                    sendEvent(name: "currentState", value: "LOW" as String)
                }
                if (cmd.value > lowThreshold && cmd.value <= medThreshold) {
                    sendEvent(name: "currentState", value: "MED" as String)
                }
                if (cmd.value > medThreshold) {
                    sendEvent(name: "currentState", value: "HIGH" as String)
                }
            }
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "ConfigurationReport $cmd"
	def value = "when off"
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	createEvent([name: "indicatorStatus", value: value])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def versions = commandClassVersions
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	sendEvent(name: "switch", value: "on", isStateChange: true)
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	sendEvent(name: "switch", value: "off", isStateChange: true)
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def setLevel(value) {
//	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on", isStateChange: true)
	} else {
		sendEvent(name: "switch", value: "off", isStateChange: true)
	}
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
//	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
	delayBetween ([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				   zwave.switchMultilevelV1.switchMultilevelGet().format()], getStatusDelay)
}

def lowSpeed() {
    sendEvent(name: "currentState", value: "LOW" as String)
	setLevel(lowThreshold)
}

def medSpeed() {
    sendEvent(name: "currentState", value: "MED" as String)
	setLevel(medThreshold)
}

def highSpeed() {
    sendEvent(name: "currentState", value: "HIGH" as String)
	setLevel(highThreshold)
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

// PING is used by Device-Watch in attempt to reach the Device
def ping() {
	refresh()
}

def refresh() {
	log.debug "refresh() is called"
	def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	delayBetween(commands,100)
}

void indicatorWhenOn() {
	sendEvent(name: "indicatorStatus", value: "when on", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()))
}

void indicatorWhenOff() {
	sendEvent(name: "indicatorStatus", value: "when off", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()))
}

void indicatorNever() {
	sendEvent(name: "indicatorStatus", value: "never", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 3, size: 1).format()))
}

def invertSwitch(invert=true) {
	if (invert) {
		zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
	}
	else {
		zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
	}
}