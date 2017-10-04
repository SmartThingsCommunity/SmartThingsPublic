/**
 *  Device Type Definition File
 *
 *  Device Type:		Fibaro Motion Sensor
 *  File Name:			fibaro-motion-sensor.groovy
 *	Initial Release:	2014-12-10
 *	Author:				Todd Wackford
 *  Email:				todd@wackford.net
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
 ***************************************************************************************
 *
 * Change Log:
 *
 * 1.	20150125	Todd Wackford
 *		Incorporated Crc16Encap function to support core code changes. Duncan figured it
 *		out as usual.
 *
 * 2.	20150125	Todd Wackford
 *		Leaned out parse and moved most device info getting into configuration method.
 */

 /**
 * Sets up metadata, simulator info and tile definition.
 *
 * @param none
 *
 * @return none
 */
 metadata {
	definition (name: "Fibaro Motion Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.motion") {
		capability 	"Motion Sensor"
		capability 	"Temperature Measurement"
		capability 	"Acceleration Sensor"
		capability 	"Configuration"
		capability 	"Illuminance Measurement"
		capability 	"Sensor"
		capability 	"Battery"
		capability  	"Health Check"

        	command		"resetParams2StDefaults"
        	command		"listCurrentParams"
        	command		"updateZwaveParam"
        	command		"test"
		command		"configure"

		fingerprint deviceId: "0x2001", inClusters: "0x30,0x84,0x85,0x80,0x8F,0x56,0x72,0x86,0x70,0x8E,0x31,0x9C,0xEF,0x30,0x31,0x9C"
	}

	simulator {
		// messages the device returns in response to commands it receives
		status "motion (basic)"     : "command: 2001, payload: FF"
		status "no motion (basic)"  : "command: 2001, payload: 00"
		status "motion (binary)"    : "command: 3003, payload: FF"
		status "no motion (binary)" : "command: 3003, payload: 00"

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

	 tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC")
				attributeState("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#CCCCCC")
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
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "luminosity", label:'${currentValue} ${unit}', unit:"lux"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        	standardTile("acceleration", "device.acceleration", width: 2, height: 2) {
			state("active", label:'vibration', icon:"st.motion.acceleration.active", backgroundColor:"#00a0dc")
			state("inactive", label:'still', icon:"st.motion.acceleration.inactive", backgroundColor:"#cccccc")
		}

		main(["motion", "temperature", "acceleration", "illuminance"])
		details(["motion", "temperature", "acceleration", "battery", "illuminance", "configure"])
	}
}

 /**
 * Configures the device to settings needed by SmarthThings at device discovery time.
 *
 * @param none
 *
 * @return none
 */
def configure() {
	log.debug "Configuring Device For SmartThings Use"
	// Device-Watch simply pings if no device events received for 8 hrs & 2 minutes
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

    def cmds = []

    // send associate to group 3 to get sensor data reported only to hub
    cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()

	// turn on tamper sensor with active/inactive reports (use it as an acceleration sensor) default is 0, or off
	cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 24, size: 1).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 24).format()

    // temperature change report threshold (0-255 = 0.1 to 25.5C) default is 1.0 Celcius, setting to .5 Celcius
    cmds << zwave.configurationV1.configurationSet(configurationValue: [5], parameterNumber: 60, size: 1).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 60).format()

    cmds << response(zwave.batteryV1.batteryGet())
    cmds << response(zwave.versionV1.versionGet().format())
    cmds << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())

	delayBetween(cmds, 500)
}

// Parse incoming device messages to generate events
def parse(String description)
{
	def result = []
	def cmd = zwave.parse(description, [0x72: 2, 0x31: 2, 0x30: 1, 0x84: 1, 0x9C: 1, 0x70: 2, 0x80: 1, 0x86: 1, 0x7A: 1, 0x56: 1])

    if (description == "updated") {
        result << response(zwave.wakeUpV1.wakeUpIntervalSet(seconds: 7200, nodeid:zwaveHubNodeId))
		result << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
	}

	if (cmd) {
		if( cmd.CMD == "8407" ) {
            result << response(zwave.batteryV1.batteryGet().format())
        	result << new physicalgraph.device.HubAction(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
        }
		result << createEvent(zwaveEvent(cmd))
	}

    if ( result[0] != null ) {
		log.debug "Parse returned ${result}"
		result
    }
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd)
{
	def versions = [0x31: 2, 0x30: 1, 0x84: 1, 0x9C: 1, 0x70: 2]
	// def encapsulatedCommand = cmd.encapsulatedCommand(versions)
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		log.debug "Could not extract command from $cmd"
	} else {
		zwaveEvent(encapsulatedCommand)
	}
}

def createEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, Map item1) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
}

def createEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd, Map item1) {
    updateDataValue("applicationVersion", "${cmd.applicationVersion}")
    log.debug "applicationVersion:      ${cmd.applicationVersion}"
    log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}

def createEvent(physicalgraph.zwave.commands.firmwareupdatemdv1.FirmwareMdReport cmd, Map item1) {
    log.debug "checksum:       ${cmd.checksum}"
    log.debug "firmwareId:     ${cmd.firmwareId}"
    log.debug "manufacturerId: ${cmd.manufacturerId}"
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	def map = [:]
    map.name = "acceleration"

	map.value = cmd.sensorState ? "active" : "inactive"
	if (map.value == "active") {
		map.descriptionText = "$device.displayName detected vibration"
	}
	else {
		map.descriptionText = "$device.displayName vibration has stopped"
	}
    map
}

// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	[descriptionText: "${device.displayName} woke up", isStateChange: false]
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
		case 3:
			// luminance
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			map.name = "illuminance"
			break;
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
log.debug cmd
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
	map.unit = "%"
	map.displayed = false
	map
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	def map = [:]
	map.value = cmd.sensorValue ? "active" : "inactive"
	map.name = "motion"
	if (map.value == "active") {
		map.descriptionText = "$device.displayName detected motion"
	}
	else {
		map.descriptionText = "$device.displayName motion has stopped"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	def map = [:]
	map.value = cmd.value ? "active" : "inactive"
	map.name = "motion"
	if (map.value == "active") {
		map.descriptionText = "$device.displayName detected motion"
	}
	else {
		map.descriptionText = "$device.displayName motion has stopped"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Catchall reached for cmd: ${cmd.toString()}}"
	[:]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
    updateDataValue("MSR", msr)

    if ( msr == "010F-0800-2001" ) { //this is the msr and device type for the fibaro motion sensor
    	configure()
    }

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}

//used to add "test" button for simulation of user changes to parameters
def test() {
	def params = [paramNumber:80,value:10,size:1]
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
        log.debug "Make sure device is awake and in recieve mode"
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
	log.debug "Resetting Sensor Parameters to SmartThings Compatible Defaults"
	def cmds = []
	cmds << zwave.configurationV1.configurationSet(configurationValue: [10], parameterNumber: 1, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [15], parameterNumber: 2, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 4, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,30], parameterNumber: 6, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 8, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,200], parameterNumber: 9, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 12, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 16, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [15], parameterNumber: 20, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,30], parameterNumber: 22, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 24, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 26, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,200], parameterNumber: 40, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0], parameterNumber: 42, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [5], parameterNumber: 60, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [3,132], parameterNumber: 62, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0], parameterNumber: 64, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0], parameterNumber: 66, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [10], parameterNumber: 80, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [50], parameterNumber: 81, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,100], parameterNumber: 82, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [3,232], parameterNumber: 83, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [18], parameterNumber: 86, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [28], parameterNumber: 87, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 89, size: 1).format()

    delayBetween(cmds, 500)
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
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 6).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 8).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 12).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 14).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 16).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 20).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 22).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 24).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 26).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 40).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 42).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 60).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 62).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 64).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 66).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 80).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 81).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 82).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 83).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 86).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 87).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 89).format()

	delayBetween(cmds, 500)
}
