/**
 *  Device Type Definition File
 *
 *  Device Type:        Fibaro Flood Sensor
 *  File Name:          fibaro-flood-sensor.groovy
 *  Initial Release:    2014-12-10
 *  @author:            Todd Wackford
 *  Email:              todd@wackford.net
 *  @version:           1.0
 *
 *  Copyright 2014 SmartThings
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
 
 /**
 * Sets up metadata, simulator info and tile definition. The tamper tile is setup, but 
 * not displayed to the user. We do this so we can receive events and display on device 
 * activity. If the user wants to display the tamper tile, adjust the tile display lines
 * with the following:
 *		main(["water", "temperature"])
 *		details(["water", "temperature", "tamper", "battery", "configure"])
 *
 * @param none
 *
 * @return none
 */
metadata {
	definition (name: "Fibaro Flood Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Water Sensor"
		capability "Temperature Measurement"
		capability "Configuration"
		capability "Battery"
		capability "Health Check"
    		capability "Sensor"
    
		command    "resetParams2StDefaults"
		command    "listCurrentParams"
		command    "updateZwaveParam"
		command    "test"

		fingerprint deviceId: "0xA102", inClusters: "0x30,0x9C,0x60,0x85,0x8E,0x72,0x70,0x86,0x80,0x84"
		fingerprint mfr:"010F", prod:"0000", model:"2002"
		fingerprint mfr:"010F", prod:"0000", model:"1002"
		fingerprint mfr:"010F", prod:"0B00", model:"1001"
	}

	simulator {
		// messages the device returns in response to commands it receives
		status "motion (basic)"     : "command: 2001, payload: FF"
		status "no motion (basic)"  : "command: 2001, payload: 00"

		for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1).incomingMessage()
		}

		for (int i = 200; i <= 1000; i += 200) {
			status "luminance ${i} lux": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 0, sensorType: 3).incomingMessage()
		}

		for (int i = 0; i <= 100; i += 20) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
				batteryLevel: i).incomingMessage()
		}
	}

	tiles(scale:2) {
		multiAttributeTile(name:"water", type: "generic", width: 6, height: 4){
			tileAttribute("device.water", key: "PRIMARY_CONTROL") {
				attributeState("dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff")
				attributeState("wet", icon:"st.alarm.water.wet", backgroundColor:"#00A0DC")
 			}
 		}
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
		}
		standardTile("tamper", "device.tamper", width: 2, height: 2) {
			state("secure", label:"secure", icon:"st.locks.lock.locked",   backgroundColor:"#ffffff")
			state("tampered", label:"tampered", icon:"st.locks.lock.unlocked", backgroundColor:"#00a0dc")
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main(["water", "temperature"])
		details(["water", "temperature", "battery", "configure"])
	}
}

// Parse incoming device messages to generate events
def parse(String description)
{
	def result = []

	def cmd = zwave.parse(description, [0x31: 2, 0x30: 1, 0x70: 2, 0x71: 1, 0x84: 1, 0x80: 1, 0x9C: 1, 0x72: 2, 0x56: 2, 0x60: 3])

	if (cmd) {
		result += zwaveEvent(cmd) //createEvent(zwaveEvent(cmd))   
	}

	if ( result[0] != null ) {
		log.debug "Parse returned ${result}"
		result
	}
}


def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 2, 0x31: 2]) // can specify command class versions here like in zwave.parse
	log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	}
}

// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
	if (!isConfigured()) {
		// we're still in the process of configuring a newly joined device
		result << lateConfigure(true)
	} else {
		result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	def map = [:]

	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = "temperature"
			break;
		case 0:
			// here's our tamper alarm = acceleration
			map.value = cmd.sensorState == 255 ? "active" : "inactive"
			map.name = "acceleration"
			break;
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
	map.unit = "%"
	map.displayed = false
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	def map = [:]
	map.value = cmd.sensorValue ? "active" : "inactive"
	map.name = "acceleration"

	if (map.value == "active") {
		map.descriptionText = "$device.displayName detected vibration"
	}
	else {
		map.descriptionText = "$device.displayName vibration has stopped"
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.debug "BasicSet with CMD = ${cmd}"

	if (!isConfigured()) {
		def result = []
		def map = [:]

		map.name = "water"
		map.value = cmd.value ? "wet" : "dry"
		map.descriptionText = "${device.displayName} is ${map.value}"

		// If we are getting a BasicSet, and isConfigured == false, then we are likely NOT properly configured.
		result += lateConfigure(true)

		result << createEvent(map)

		result
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	def map = [:]

	if (cmd.sensorType == 0x05) {
		map.name = "water"
		map.value = cmd.sensorState ? "wet" : "dry"
		map.descriptionText = "${device.displayName} is ${map.value}"

		log.debug "CMD = SensorAlarmReport: ${cmd}"
		setConfigured()
	} else if ( cmd.sensorType == 0) {
		map.name = "tamper"
		map.isStateChange = true
		map.value = cmd.sensorState ? "tampered" : "secure"
		map.descriptionText = "${device.displayName} has been tampered with"
		runIn(30, "resetTamper") //device does not send alarm cancelation

	} else if ( cmd.sensorType == 1) {
		map.name = "tamper"
		map.value = cmd.sensorState ? "tampered" : "secure"
		map.descriptionText = "${device.displayName} has been tampered with"
		runIn(30, "resetTamper") //device does not send alarm cancelation

	} else {
		map.descriptionText = "${device.displayName}: ${cmd}"
	}
	createEvent(map)
}

def resetTamper() {
	def map = [:]
	map.name = "tamper"
	map.value = "secure"
	map.descriptionText = "$device.displayName is secure"
	sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Catchall reached for cmd: ${cmd.toString()}}"
	[:]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	device.updateDataValue(["MSR", msr])

	if ( msr == "010F-0B00-2001" ) { //this is the msr and device type for the fibaro flood sensor
		result += lateConfigure(true)
	}

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}

def setConfigured() {
	device.updateDataValue("configured", "true")
}

def isConfigured() {
	Boolean configured = device.getDataValue(["configured"]) as Boolean

	return configured
}

def lateConfigure(setConf = False) {
	def res = response(configure())

	if (setConf)
		setConfigured()

	return res
}

 /**
 * Configures the device to settings needed by SmarthThings at device discovery time.
 *
 * @param none
 *
 * @return none
 */
def configure() {
	log.debug "Configuring Device..."
	// Device wakes up every 4 hours, this interval allows us to miss one wakeup notification before marking offline
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	// default initial state
	sendEvent(name: "water", value: "dry")

	def cmds = []

	// send associate to group 2 to get alarm data
	cmds << zwave.associationV2.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()

	cmds << zwave.configurationV1.configurationSet(configurationValue: [255], parameterNumber: 5, size: 1).format()

	// send associate to group 3 to get sensor data reported only to hub
	cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()

	// reporting frequency of temps and battery set to one hour
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,60*60], parameterNumber: 10, size: 2).format()
	// cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format()

	// temp hysteresis set to .5 degrees celcius
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,50], parameterNumber: 12, size: 2).format()
	// cmds << zwave.configurationV1.configurationGet(parameterNumber: 12).format()

	cmds << zwave.batteryV1.batteryGet().format()

	cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()

	delayBetween(cmds, 100)
}


//used to add "test" button for simulation of user changes to parameters
def test() {
	def params = [paramNumber:12,value:4,size:1]
	updateZwaveParam(params)
}

 /**
 * This method will allow the user to update device parameters (behavior) from an app.
 * A "Zwave Tweaker" app will be developed as an interface to do this. Or the user can
 * write his/her own app to envoke this method. No type or value checking is done to
 * compare to what device capability or reaction. It is up to user to read OEM
 * documentation prio to envoking this method.
 *
 * <p>THIS IS AN ADVANCED OPERATION. USE AT YOUR OWN RISK! READ OEM DOCUMENTATION!
 *
 * @param List[paramNumber:80,value:10,size:1]
 *
 *
 * @return none
 */
def updateZwaveParam(params) {
	if ( params ) {
		def pNumber = params.paramNumber
		def pSize	= params.size
		def pValue	= [params.value]
		log.debug "Make sure device is awake and in recieve mode (triple-click?)"
		log.debug "Updating ${device.displayName} parameter number '${pNumber}' with value '${pValue}' with size of '${pSize}'"

		def cmds = []
		cmds << zwave.configurationV1.configurationSet(configurationValue: pValue, parameterNumber: pNumber, size: pSize).format()
		cmds << zwave.configurationV1.configurationGet(parameterNumber: pNumber).format()
		delayBetween(cmds, 1000)
	}
}

 /**
 * Sets all of available Fibaro parameters back to the device defaults except for what
 * SmartThings needs to support the stock functionality as released. This will be
 * called from the "Fibaro Tweaker" or user's app.
 *
 * <p>THIS IS AN ADVANCED OPERATION. USE AT YOUR OWN RISK! READ OEM DOCUMENTATION!
 *
 * @param none
 *
 * @return none
 */
def resetParams2StDefaults() {
	log.debug "Resetting ${device.displayName} parameters to SmartThings compatible defaults"
	def cmds = []
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0],			parameterNumber: 1,	 size: 2).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [3],				parameterNumber: 2,	 size: 1).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [255],			parameterNumber: 5,	 size: 1).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [255],			parameterNumber: 7,	 size: 1).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [1],				parameterNumber: 9,	 size: 1).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,60*60],		parameterNumber: 10, size: 2).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,50],			parameterNumber: 12, size: 2).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0],				parameterNumber: 13, size: 1).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [5,220],			parameterNumber: 50, size: 2).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [13,172],		parameterNumber: 51, size: 2).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0,0,225],		parameterNumber: 61, size: 4).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,255,0,0],		parameterNumber: 62, size: 4).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [2],				parameterNumber: 63, size: 1).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0],			parameterNumber: 73, size: 2).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [2],				parameterNumber: 74, size: 1).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0],			parameterNumber: 75, size: 2).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0],			parameterNumber: 76, size: 2).format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0],				parameterNumber: 77, size: 1).format()

	delayBetween(cmds, 1200)
}

 /**
 * Lists all of available Fibaro parameters and thier current settings out to the 
 * logging window in the IDE This will be called from the "Fibaro Tweaker" or 
 * user's own app.
 *
 * <p>THIS IS AN ADVANCED OPERATION. USE AT YOUR OWN RISK! READ OEM DOCUMENTATION!
 *
 * @param none
 *
 * @return none
 */
def listCurrentParams() {
	log.debug "Listing of current parameter settings of ${device.displayName}"
	def cmds = []
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 1).format() 
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 2).format() 
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 5).format() 
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 7).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 12).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 13).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 50).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 51).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 61).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 62).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 63).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 73).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 74).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 75).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 76).format()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 77).format()

	delayBetween(cmds, 1200)
}

