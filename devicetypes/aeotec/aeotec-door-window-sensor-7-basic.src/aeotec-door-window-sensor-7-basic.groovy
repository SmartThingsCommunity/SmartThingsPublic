/**
 *  Copyright 2015 SmartThings
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
 *  Z-Wave Door/Window Sensor
 *
 *  Original Author: SmartThings
 *  Date: 2013-11-3
 *  Modified Author: Aeotec
 *  Date: 2019-8-23
 *
 *  Special thanks to Erocm123 for his configuration code.
 *
 *  Version 1.0
 *  * Added Tilt Sensor to SmartThings Classic interface.
 *  * Added Configuration settings Parameter #1, #2
 */

metadata {
	definition(name: "Aeotec Door/Window Sensor 7 Basic", namespace: "Aeotec", author: "Chris Cheng", ocfDeviceType: "x.com.st.d.sensor.contact", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, genericHandler: "Z-Wave") {
		capability "Contact Sensor"
		capability "Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Health Check"
        
        command "update_needed_settings"

		fingerprint deviceId: "0x2001", inClusters: "0x30,0x80,0x84,0x85,0x86,0x72"
		fingerprint deviceId: "0x07", inClusters: "0x30"
		fingerprint deviceId: "0x0701", inClusters: "0x5E,0x98"
		fingerprint deviceId: "0x0701", inClusters: "0x5E,0x86,0x72,0x98", outClusters: "0x5A,0x82"
		fingerprint deviceId: "0x0701", inClusters: "0x5E,0x80,0x71,0x85,0x70,0x72,0x86,0x30,0x31,0x84,0x59,0x73,0x5A,0x8F,0x98,0x7A", outClusters: "0x20"
		fingerprint mfr: "0371", prod: "0102", model: "0007", deviceJoinName: "Aeotec Door/Window Sensor 7" //EU
		fingerprint mfr: "0371", prod: "0002", model: "0007", deviceJoinName: "Aeotec Door/Window Sensor 7" //US
	}
    
    preferences {
        input description: "All Parameter settings must be set before you press the 'configure' button on the main sensor page.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())
    }

	// simulator metadata
	simulator {
		// status messages
		status "open": "command: 2001, payload: FF"
		status "closed": "command: 2001, payload: 00"
        //status "tilted": "command: 3003, payload: FF"
        //status "closed": "command: 3003, payload: 00"
		status "wake up": "command: 8407, payload: "
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "contact", type: "generic", width: 6, height: 4) {
			tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
				attributeState("open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
				attributeState("closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC")
			}
            tileAttribute("device.tilt", key: "SECONDARY_CONTROL"){
            	attributeState("closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC")
                attributeState("tilted", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
            }
		}     
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
        
        standardTile("update_needed_settings", "device.button", inactivelabel: false, width: 2, height: 2){
        	state "default", label: "configure", action: "update_needed_settings"
        }

		main "contact"
		details(["contact", "tilt", "battery", "update_needed_settings"])
	}
}

private getCommandClassVersions() {
	[0x20: 1, 0x25: 1, 0x30: 1, 0x31: 5, 0x70: 2, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1]
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err 106")) {
		if ((zwaveInfo.zw == null && state.sec != 0) || zwaveInfo?.zw?.endsWith("s")) {
			log.debug description
		} else {
			result = createEvent(
				descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				isStateChange: true,
			)
		}
	} else if (description != "updated") {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "parsed '$description' to $result"
	return result
}

def installed() {
	// Device-Watch simply pings if no device events received for 482min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// this is the nuclear option because the device often goes to sleep before we can poll it
	sendEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
	sendEvent(name: "battery", unit: "%", value: 100)
	response(initialPoll())
}

def updated() {
	// Device-Watch simply pings if no device events received for 482min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def sensorValueEvent(value) {
	if (value) {
		createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
	} else {
		createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
	}
}

def tiltValueEvent(value) {
	if (value) {
		createEvent(name: "tilt", value: "tilted", descriptionText: "$device.displayName is tilted")
        //log.debug "Tilted binary report received"
	} 
    else {
		createEvent(name: "tilt", value: "closed", descriptionText: "$device.displayName is closed")
        //log.debug "Closed binary report received"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
    tiltValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd) {
	sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
    //log.debug "result open"
	if (cmd.notificationType == 0x06 && cmd.event == 0x16) {
		result << sensorValueEvent(1)
	} else if (cmd.notificationType == 0x06 && cmd.event == 0x17) {
		result << sensorValueEvent(0)
	} else if (cmd.notificationType == 0x07) {
		if (cmd.v1AlarmType == 0x07) {  // special case for nonstandard messages from Monoprice door/window sensors
			result << sensorValueEvent(cmd.v1AlarmLevel)
		} else if (cmd.event == 0x01 || cmd.event == 0x02) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x03) {
			result << createEvent(descriptionText: "$device.displayName covering was removed", isStateChange: true)
			if (!state.MSR) result << response(command(zwave.manufacturerSpecificV2.manufacturerSpecificGet()))
		} else if (cmd.event == 0x05 || cmd.event == 0x06) {
			result << createEvent(descriptionText: "$device.displayName detected glass breakage", isStateChange: true)
		} else if (cmd.event == 0x07) {
			if (!state.MSR) result << response(command(zwave.manufacturerSpecificV2.manufacturerSpecificGet()))
			result << createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
		}
	} else if (cmd.notificationType) {
		def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
		result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, displayed: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def event = createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
	def cmds = []
	if (!state.MSR) {
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	}

	if (device.currentValue("contact") == null) {
		// In case our initial request didn't make it, initial state check no. 3
		cmds << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_DOOR_WINDOW)
	}

	if (!state.lastbat || now() - state.lastbat > 53 * 60 * 60 * 1000) {
		cmds << zwave.batteryV1.batteryGet()
	}

	def request = []
	if (cmds.size() > 0) {
		request = commands(cmds, 1000)
		request << "delay 20000"
	}
	request << zwave.wakeUpV1.wakeUpNoMoreInformation().format()

	[event, response(request)]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [name: "battery", unit: "%"]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbat = now()
	[createEvent(map)]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)

	// change DTH if required based on MSR
	if (!retypeBasedOnMSR()) {
		if (msr == "011A-0601-0901") {
			// Enerwave motion doesn't always get the associationSet that the hub sends on join
			result << response(zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId))
		}
	} else {
		// if this is door/window sensor check initial contact state no.2
		if (!device.currentState("contact")) {
			result << response(command(zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_DOOR_WINDOW)))
		}
	}

	// every battery device can miss initial battery check. check initial battery state no.2
	if (!device.currentState("battery")) {
		result << response(command(zwave.batteryV1.batteryGet()))
	}

	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def version = commandClassVersions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def result = null
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	log.debug "Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		result = zwaveEvent(encapsulatedCommand)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.multicmdv1.MultiCmdEncap cmd) {
	log.debug "MultiCmd with $numberOfCommands inner commands"
	cmd.encapsulatedCommands(commandClassVersions).collect { encapsulatedCommand ->
		zwaveEvent(encapsulatedCommand)
	}.flatten()
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

def initialPoll() {
	def request = []
	if (isEnerwave()) { // Enerwave motion doesn't always get the associationSet that the hub sends on join
		request << zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId)
	}

	// check initial battery and contact state no.1
	request << zwave.batteryV1.batteryGet()
	request << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_DOOR_WINDOW)
	request << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	commands(request, 500) + ["delay 6000", command(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

private command(physicalgraph.zwave.Command cmd) {
	if ((zwaveInfo.zw == null && state.sec != 0) || zwaveInfo?.zw?.endsWith("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay = 200) {
	delayBetween(commands.collect { command(it) }, delay)
}

def retypeBasedOnMSR() {
	def dthChanged = true
	switch (state.MSR) {
		case "0086-0002-002D":
			log.debug "Changing device type to Z-Wave Water Sensor"
			setDeviceType("Z-Wave Water Sensor")
			break
		case "011F-0001-0001":  // Schlage motion
		case "014A-0001-0001":  // Ecolink motion
		case "014A-0004-0001":  // Ecolink motion +
		case "0060-0001-0002":  // Everspring SP814
		case "0060-0001-0003":  // Everspring HSP02
		case "011A-0601-0901":  // Enerwave ZWN-BPC
			log.debug "Changing device type to Z-Wave Motion Sensor"
			setDeviceType("Z-Wave Motion Sensor")
			break
		case "013C-0002-000D":  // Philio multi +
			log.debug "Changing device type to 3-in-1 Multisensor Plus (SG)"
			setDeviceType("3-in-1 Multisensor Plus (SG)")
			break
		case "0109-2001-0106":  // Vision door/window
			log.debug "Changing device type to Z-Wave Plus Door/Window Sensor"
			setDeviceType("Z-Wave Plus Door/Window Sensor")
			break
		case "0109-2002-0205": // Vision Motion
			log.debug "Changing device type to Z-Wave Plus Motion/Temp Sensor"
			setDeviceType("Z-Wave Plus Motion/Temp Sensor")
			break
		default:
			dthChanged = false
			break
	}
	dthChanged
}

// this is present in zwave-motion-sensor.groovy DTH too
private isEnerwave() {
	zwaveInfo?.mfr?.equals("011A") && zwaveInfo?.prod?.equals("0601") && zwaveInfo?.model?.equals("0901")
}

/*
CONFIGURATION GET/REPORT
*/

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	 
	log.debug "${cmd.configurationValue}"	
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.debug "${cmd.configurationValue}"
}

/*def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    
    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue

    if (settings."${cmd.parameterNumber}" != null)
    {   
            if (convertParam("${cmd.parameterNumber}".toInteger(), settings."${cmd.parameterNumber}".toInteger()) == cmd2Integer(cmd.configurationValue))
            {
                sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
            }
            else
            {
                sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
            }
    }

    state.currentProperties = currentProperties
}*/

/*
CONFIGURATION SET
*/

def convertParam(number, value) {
	switch (number){
        case 41:
            //Parameter difference between firmware versions
        	if (settings."41".toInteger() != null && device.currentValue("currentFirmware") != null) {
                if (device.currentValue("currentFirmware") == "1.07" || device.currentValue("currentFirmware") == "1.08") {
                    (value * 256) + 2
                } else if (device.currentValue("currentFirmware") == "1.07EU" || device.currentValue("currentFirmware") == "1.08EU") {
                    (value * 256) + 1
                } else {
                    value
                }	
            } else {
                value
            }
        break
        case 45:
            //Parameter difference between firmware versions
        	if (settings."45".toInteger() != null && device.currentValue("currentFirmware") != null && device.currentValue("currentFirmware") != "1.08")
            	2
            else
                value
        break
        case 101:
        	if (settings."40".toInteger() != null) {
                if (settings."40".toInteger() == 1) {
                   0
                } else {
                   value
                }	
            } else {
                241
            }
        break
    	case 201:
        	if (value < 0)
            	256 + value
        	else if (value > 100)
            	value - 256
            else
            	value
        break
        case 202:
        	if (value < 0)
            	256 + value
        	else if (value > 100)
            	value - 256
            else
            	value
        break
        case 203:
            if (value < 0)
            	65536 + value
        	else if (value > 1000)
            	value - 65536
            else
            	value
        break
        case 204:
        	if (value < 0)
            	256 + value
        	else if (value > 100)
            	value - 256
            else
            	value
        break
        default:
        	value
        break
    }
}

def integer2Cmd(value, size) {
    try{
	switch(size) {
	case 1:
		[value]
    break
	case 2:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        [value2, value1]
    break
    case 3:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        [value3, value2, value1]
    break
	case 4:
    	def short value1 = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        def short value4 = (value >> 24) & 0xFF
		[value4, value3, value2, value1]
	break
	}
    } catch (e) {
        log.debug "Error: integer2Cmd $e Value: $value"
    }
}

def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
    def configuration = parseXml(configuration_model())
    
    if(!state.needfwUpdate || state.needfwUpdate == ""){
       //cmds << zwave.versionV1.versionGet()
    }    

    cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds: 86400, nodeid:zwaveHubNodeId)
    cmds << zwave.wakeUpV1.wakeUpIntervalGet()
    cmds << zwave.batteryV1.batteryGet()
   
    configuration.Value.each
    {   
    	log.debug ("Current value of parameter we are sending are parameter ${it.@index}, size:${it.@byteSize.toInteger()}, value:${convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger())}")
    	cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger()).toInteger(), it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
        //log.debug ("Getting Configuration Report for parameter ${it.@index}")
        //cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
    }
    //def value = zwave.configurationV1.configurationGet(parameterNumber: 1)
    //log.debug ("$value")
    commands(cmds)
}

def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
   
    configuration.Value.each
    {
        switch(it.@type)
        {   
            case ["byte","short","four"]:
                input "${it.@index}", "number",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
        }  
    }
}

def configuration_model()
{
'''
    <configuration>
      <Value type="list" index="1" label="Dry Contact Enabled/Disabled" min="0" max="1" value="0" byteSize="1" setting_type="zwave" displayDuringSetup="true">
            <Help>
            Enable or Disable Dry Contact Sensor, if enabled, magnet sensor is disabled.
            </Help>
            <Item label="Disabled" value="0" />
            <Item label="Enabled" value="1" />
      </Value>
      <Value type="list" index="2" label="Sensor State" min="0" max="1" value="0" byteSize="1" setting_type="zwave" displayDuringSetup="true">
            <Help>
            Change the Open or Close output.
            </Help>
            <Item label="Closed state when magnet is near" value="0" />
            <Item label="Open state when magnet is near" value="1" />
      </Value>
    </configuration>
'''
}