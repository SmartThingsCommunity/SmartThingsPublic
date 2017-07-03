/**
 *  Intermatic PE653 Pool Control System
 *
 *  Copyright 2014 bigpunk6
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
 *  Don't use Cooper Lee's code (vTile_ms, ms_w_vts) he was working on a different implementation than me.
 *
 *  Install my device type then use the Multi-Channel Controll App by SmartThings from the Marketplace under the More section.
 *
 */
metadata {
	definition (name: "Intermatic PE653 Pool Control System", author: "bigpunk6", namespace:  "bigpunk6") {
        capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Configuration"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Zw Multichannel"
        capability "Thermostat"
        
        attribute "operationMode", "string"
        attribute "firemanTimeout", "string"
        attribute "temperatureOffsets", "string"
        attribute "poolspaConfig", "string"
        attribute "poolSetpoint", "string"
        attribute "spaSetpoint", "string"
        attribute "setPoolSetpoint", "string"
        attribute "pool", "string"
        attribute "setSpaSetpoint", "string"
        attribute "spa", "string"
        
        command "quickSetPool"
        command "quickSetSpa"
		
		fingerprint deviceId: "0x1001", inClusters: "0x91,0x73,0x72,0x86,0x81,0x60,0x70,0x85,0x25,0x27,0x43,0x31", outClusters: "0x82"
	}
    
    preferences {
        input "operationMode1", "enum", title: "Boster/Cleaner Pump",
            options:[1:"No",
                     2:"Uses Circuit-1",
                     3:"Variable Speed pump Speed-1",
                     4:"Variable Speed pump Speed-2",
                     5:"Variable Speed pump Speed-3",
                     6:"Variable Speed pump Speed-4"]
        input "operationMode2", "enum", title: "Pump Type", 
            options:[0:"1 Speed Pump without Booster/Cleaner",
                     1:"1 Speed Pump with Booster/Cleaner",
                     2:"2 Speed Pump without Booster/Cleaner",
                     3:"2 Speed Pump with Booster/Cleaner"]
        input "poolSpa1", "enum", title: "Pool or Spa", options:[0:"Pool",1:"Spa",2:"Both"]
	    input "fireman", "enum", title: "Fireman Timeout",
            options:["255":"No heater installed",
                     "0":"No cool down period",
                     "1":"1 minute",
                     "2":"2 minute",
                     "3":"3 minute",
                     "4":"4 minute",
                     "5":"5 minute",
                     "6":"6 minute",
                     "7":"7 minute",
                     "8":"8 minute",
                     "9":"9 minute",
                     "10":"10 minute",
                     "11":"11 minute",
                     "12":"12 minute",
                     "13":"13 minute",
                     "14":"14 minute",
                     "15":"15 minute"]
        input "tempOffsetwater", "number", title: "Water temperature offset", defaultValue: 0, required: true
        input "tempOffsetair", "number",
            title: "Air temperature offset - Sets the Offset of the air temerature for the add-on Thermometer in degrees Fahrenheit -20F to +20F", defaultValue: 0, required: true
    }

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		reply "8E010101,delay 800,6007": "command: 6008, payload: 4004"
		reply "8505": "command: 8506, payload: 02"
		reply "59034002": "command: 5904, payload: 8102003101000000"
		reply "6007":  "command: 6008, payload: 0002"
		reply "600901": "command: 600A, payload: 10002532"
		reply "600902": "command: 600A, payload: 210031"
	}
    
	// tile definitions
	tiles(scale: 2) {
        multiAttributeTile(name:"temperature", type: "thermostat", width: 6, height: 4){
			tileAttribute ("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState "temperature", label:'${currentValue}°',
					backgroundColors:[
						[value: 32, color: "#153591"],
					    [value: 54, color: "#1e9cbb"],
				    	[value: 64, color: "#90d2a7"],
				    	[value: 74, color: "#44b621"],
				    	[value: 90, color: "#f1d801"],
				    	[value: 98, color: "#d04e00"],
				    	[value: 110, color: "#bc2323"]
					]
			}
            tileAttribute ("device.poolSetpoint", key: "VALUE_CONTROL") {
				attributeState "poolSetpoint", action:"quickSetPool"
			}
			tileAttribute ("device.spaSetpoint", key: "SECONDARY_CONTROL") {
				attributeState "spaSetpoint", label:'Spa set to ${currentValue}°F'
			}
            
		}
        controlTile("poolSliderControl", "device.poolSetpoint", "slider", height: 2, width: 4, inactiveLabel: false, range:"(40..104)") {
			state "setPoolSetpoint", action:"quickSetPool", backgroundColor:"#d04e00"
		}
		valueTile("poolSetpoint", "device.poolSetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "pool", label:'${currentValue}° pool', backgroundColor:"#ffffff"
		}
		controlTile("spaSliderControl", "device.spaSetpoint", "slider", height: 2, width: 4, inactiveLabel: false, range:"(40..104)") {
			state "setSpaSetpoint", action:"quickSetSpa", backgroundColor: "#1e9cbb"
		}
		valueTile("spaSetpoint", "device.spaSetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "spa", label:'${currentValue}° spa', backgroundColor:"#ffffff"
		}
        /*valueTile("temperature", "device.temperature") {
			state("temperature", label:'${currentValue}°', unit:"F",
				backgroundColors:[
					[value: 32, color: "#153591"],
					[value: 54, color: "#1e9cbb"],
					[value: 64, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 90, color: "#f1d801"],
					[value: 98, color: "#d04e00"],
					[value: 110, color: "#bc2323"]
				]
			)
		}*/
        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("configure", "device.configure", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        
		main "temperature"
        details(["temperature", "poolSliderControl", "poolSetpoint", "spaSliderControl", "spaSetpoint", "configure", "refresh"])
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
        log.warn "Error in Parse"
	    result = createEvent(descriptionText:description, isStateChange:true)
	} else {
		def cmd = zwave.parse(description, [0x20: 1, 0x25:1, 0x27:1, 0x31:1, 0x43:1, 0x60:3, 0x70:2, 0x81:1, 0x85:1, 0x86: 1, 0x73:1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug("'$description' parsed to $result")
	return result
}

    //Thermostat
def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	log.debug "ThermostatModeReport $cmd"
    def map = [:]
	switch (cmd.mode) {
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			map.value = "pool"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_FURNACE:
			map.value = "spa"
			break
	}
	map.name = "thermostatMode"
	map
}

def quickSetPool(degrees) {
    log.debug "quickSetPool $degrees"
	setPoolSetpoint(degrees, 1000)
}

def setPoolSetpoint(degrees, delay = 30000) {
	setPoolSetpoint(degrees.toDouble(), delay)
}

def setPoolSetpoint(Double degrees, Integer delay = 30000) {
	log.trace "setPoolSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
    	convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
    	convertedDegrees = degrees
    }

	delayBetween([
		zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()
	], delay)
}

def quickSetSpa(degrees) {
	setSpaSetpoint(degrees, 1000)
}

def setSpaSetpoint(degrees, delay = 30000) {
	setSpaSetpoint(degrees.toDouble(), delay)
}

def setSpaSetpoint(Double degrees, Integer delay = 30000) {
    log.trace "setSpaSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
    	convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
    	convertedDegrees = degrees
    }

	delayBetween([
		zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 7, scale: deviceScale, precision: p,  scaledValue: convertedDegrees).format(),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 7).format()
	], delay)
}

//Reports

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.debug "configuration: $cmd"
    def map = [:]
	map.value = cmd.configurationValue
	map.displayed = false
    switch (cmd.parameterNumber) {
        case 1:
			map.name = "operationMode"
			break;
        case 2:
			map.name = "firemanTimeout"
			break;
        case 3:
			map.name = "temperatureOffsets"
			break;
        case 19:
			map.name = "poolspaConfig"
			break;
		default:
			return [:]
	}
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv1.SensorMultilevelReport cmd) {
    log.debug "Sensor: $cmd"
    def map = [:]
    map.value = cmd.scaledSensorValue.toString()
    map.unit = cmd.scale == 1 ? "F" : "C"
    map.name = "temperature"
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointReport cmd) {
	def map = [:]
	map.value = cmd.scaledValue.toString()
	map.unit = cmd.scale == 1 ? "F" : "C"
	map.displayed = false
	switch (cmd.setpointType) {
		case 1:
			map.name = "poolSetpoint"
			break;
		case 7:
			map.name = "spaSetpoint"
			break;
		default:
			return [:]
	}
	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "$cmd"
    if (cmd.value == 0) {
		createEvent(name: "switch", value: "off")
	} else if (cmd.value == 255) {
		createEvent(name: "switch", value: "on")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiInstanceReport cmd) {
    log.debug "MultiInstanceReport $cmd"
}

private List loadEndpointInfo() {
	if (state.endpointInfo) {
		state.endpointInfo
	} else if (device.currentValue("epInfo")) {
		fromJson(device.currentValue("epInfo"))
	} else {
		[]
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd) {
	log.debug "$cmd"
    updateDataValue("endpoints", cmd.endPoints.toString())
	if (!state.endpointInfo) {
		state.endpointInfo = loadEndpointInfo()
	}
	if (state.endpointInfo.size() > cmd.endPoints) {
		cmd.endpointInfo
	}
	state.endpointInfo = [null] * cmd.endPoints
	//response(zwave.associationV2.associationGroupingsGet())
	[ createEvent(name: "epInfo", value: util.toJson(state.endpointInfo), displayed: false, descriptionText:""),
	  response(zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: 1)) ]
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) {
	log.debug "$cmd"
    def result = []
	def cmds = []
	if(!state.endpointInfo) state.endpointInfo = []
	state.endpointInfo[cmd.endPoint - 1] = cmd.format()[6..-1]
	if (cmd.endPoint < getDataValue("endpoints").toInteger()) {
		cmds = zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: cmd.endPoint + 1).format()
	} else {
		log.debug "endpointInfo: ${state.endpointInfo.inspect()}"
	}
	result << createEvent(name: "epInfo", value: util.toJson(state.endpointInfo), displayed: false, descriptionText:"")
	if(cmds) result << response(cmds)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
	log.debug "$cmd"
    state.groups = cmd.supportedGroupings
	if (cmd.supportedGroupings > 1) {
		[response(zwave.associationGrpInfoV1.associationGroupInfoGet(groupingIdentifier:2, listMode:1))]
	}
}

def zwaveEvent(physicalgraph.zwave.commands.associationgrpinfov1.AssociationGroupInfoReport cmd) {
	log.debug "$cmd"
    def cmds = []
	for (def i = 2; i <= state.groups; i++) {
		cmds << response(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:i, nodeId:zwaveHubNodeId))
	}
	cmds
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	log.debug "$cmd"
    def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
	if (encapsulatedCommand) {
		if (state.enabledEndpoints.find { it == cmd.sourceEndPoint }) {
			def formatCmd = ([cmd.commandClass, cmd.command] + cmd.parameter).collect{ String.format("%02X", it) }.join()
            createEvent(name: "epEvent", value: "$cmd.sourceEndPoint:$formatCmd", isStateChange: true, displayed: false, descriptionText: "(fwd to ep $cmd.sourceEndPoint)")
        } else {
			zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.warn "Captured zwave command $cmd"
	createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
}

//Commands

def epCmd(Integer ep, String cmds) {
    log.debug "epCmd: $ep $cmds"
    if (cmds.contains('2001FF')){
        log.debug "contained 2001FF"
        delayBetween([
            encap(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF), ep),
            encap(zwave.switchBinaryV1.switchBinaryGet(), ep)
	    ], 2300)
    } else if (cmds.contains('200100')) {
        log.debug "contained 2001FF"
        delayBetween([
            encap(zwave.switchBinaryV1.switchBinarySet(switchValue: 0), ep),
            encap(zwave.switchBinaryV1.switchBinaryGet(), ep)
	    ], 2300)
    } else if (cmds.contains('2002')) {
        encap(zwave.switchBinaryV1.switchBinaryGet(), ep)
    } else {
        log.warn "No CMD found"
    }
}

def enableEpEvents(enabledEndpoints) {
	state.enabledEndpoints = enabledEndpoints.split(",").findAll()*.toInteger()
	null
}

private command(physicalgraph.zwave.Command cmd) {
	log.debug "command: $cmd"
    cmd.format()
}

private encap(cmd, endpoint) {
	if (endpoint) {
        command(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint, sourceEndPoint: endpoint).encapsulate(cmd))
	} else {
		command(cmd)
	}
}

def poll() {
    zwave.sensorMultilevelV1.sensorMultilevelGet().format()
}

def refresh() {
	delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:3, destinationEndPoint:3, commandClass:37, command:2).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:4, destinationEndPoint:4, commandClass:37, command:2).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:5, destinationEndPoint:5, commandClass:37, command:2).format(),
    zwave.sensorMultilevelV1.sensorMultilevelGet().format(),
    zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format(),
    zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 7).format(),
    zwave.configurationV2.configurationGet(parameterNumber: 1).format(),
    zwave.configurationV2.configurationGet(parameterNumber: 2).format(),
    zwave.configurationV2.configurationGet(parameterNumber: 3).format(),
    zwave.configurationV2.configurationGet(parameterNumber: 19).format()
    ], 3000)
}

def configure() {
    def cmds = []
        cmds << zwave.configurationV2.configurationSet(configurationValue: [operationMode1.toInteger(), operationMode2.toInteger()], parameterNumber: 1, size: 2).format()
        cmds << zwave.configurationV2.configurationSet(configurationValue: [tempOffsetwater.toInteger(), tempOffsetair.toInteger(), 0, 0], parameterNumber: 3, size: 4).format()
        cmds << zwave.configurationV2.configurationSet(configurationValue: [poolSpa1.toInteger()], parameterNumber: 19, size: 1).format()
        cmds << zwave.configurationV2.configurationSet(configurationValue: [fireman.toInteger()], parameterNumber: 2, size: 1).format()
	delayBetween(cmds, 2500)
}