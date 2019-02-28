/**
 *
 *  Fibaro Dimmer 2 (US)
 *   
 *	github: Eric Maycock (erocm123)
 *	email: erocmail@gmail.com
 *	Date: 2016-07-31 8:03PM
 *	Copyright Eric Maycock
 *
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
 
metadata {
	definition (name: "Fibaro Dimmer 2", namespace: "erocm123", author: "Eric Maycock", vid:"generic-switch-power-energy") {
		capability "Actuator"
		capability "Switch"
		capability "Switch Level"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"
        capability "Polling"
        capability "Energy Meter"
        capability "Power Meter"
        capability "Button"
        capability "Health Check"
        
        attribute   "needUpdate", "string"

        fingerprint mfr: "010F", prod: "0102", model: "2000", deviceJoinName: "Fibaro Dimmer 2"

		fingerprint deviceId: "0x1101", inClusters: "0x72,0x86,0x70,0x85,0x8E,0x26,0x7A,0x27,0x73,0xEF,0x26,0x2B"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x20,0x86,0x72,0x26,0x5A,0x59,0x85,0x73,0x98,0x7A,0x56,0x70,0x31,0x32,0x8E,0x60,0x75,0x71,0x27"
        
	}
    
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())
    }

	simulator {

	}

	tiles(scale: 2){
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
           		attributeState "statusText", label:'${currentValue}'       		
            }
	    }
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }


		main "switch"
		details (["switch", "power", "energy", "refresh", "configure", "reset"])
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x98: 1, 0x56: 1, 0x60: 3])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
    
    def statusTextmsg = ""
    if (device.currentState('power') && device.currentState('energy')) statusTextmsg = "${device.currentState('power').value} W ${device.currentState('energy').value} kWh"
    sendEvent(name:"statusText", value:statusTextmsg, displayed:false)
    
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    logging("BasicReport: $cmd")
    def events = []
	if (cmd.value == 0) {
		events << createEvent(name: "switch", value: "off")
	} else if (cmd.value == 255) {
		events << createEvent(name: "switch", value: "on")
	} else {
		events << createEvent(name: "switch", value: "on")
        events << createEvent(name: "switchLevel", value: cmd.value)
	}
    
    def request = update_needed_settings()
    
    if(request != []){
        return [response(commands(request)), events]
    } else {
        return events
    }
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
    logging("SceneActivationSet: $cmd")
    logging("sceneId: $cmd.sceneId")
    logging("dimmingDuration: $cmd.dimmingDuration")
    logging("Configuration for preference \"Switch Type\" is set to ${settings."20"}")
    
    if (settings."20" == "2") {
        logging("Switch configured as Roller blinds")
        switch (cmd.sceneId) {
            // Roller blinds S1
            case 10: // Turn On (1x click)
                buttonEvent(1, "pushed")
            break
            case 13: // Release
                buttonEvent(1, "held")
            break
            case 14: // 2x click
                buttonEvent(2, "pushed")
            break
            case 17: // Brightening
                buttonEvent(2, "held")
            break
            // Roller blinds S2
            case 11: // Turn Off
                buttonEvent(3, "pushed")
            break
            case 13: // Release
                buttonEvent(3, "held")
            break
            case 14: // 2x click
                buttonEvent(4, "pushed")
            break
            case 15: // 3x click
                buttonEvent(5, "pushed")
            break
            case 18: // Dimming
                buttonEvent(4, "held")
            break
            default:
                logging("Unhandled SceneActivationSet: ${cmd}")
            break
        }
    } else if (settings."20" == "1") {
        logging("Switch configured as Toggle")
        switch (cmd.sceneId) {
            // Toggle S1
            case 10: // Off to On
                buttonEvent(1, "pushed")
            break
            case 11: // On to Off
                buttonEvent(1, "held")
            break
            case 14: // 2x click
                buttonEvent(2, "pushed")
            break
            // Toggle S2
            case 20: // Off to On
                buttonEvent(3, "pushed")
            break
            case 21: // On to Off
                buttonEvent(3, "held")
            break
            case 24: // 2x click
                buttonEvent(4, "pushed")
            break
            case 25: // 3x click
                buttonEvent(5, "pushed")
            break
            default:
                logging("Unhandled SceneActivationSet: ${cmd}")
            break
        
        }
    } else {
        if (settings."20" == "0") logging("Switch configured as Momentary") else logging("Switch type not configured") 
        switch (cmd.sceneId) {
            // Momentary S1
            case 16: // 1x click
                buttonEvent(1, "pushed")
            break
            case 14: // 2x click
                buttonEvent(2, "pushed")
            break
            case 12: // held
                buttonEvent(1, "held")
            break
            case 13: // release
                buttonEvent(2, "held")
            break
            // Momentary S2
            case 26: // 1x click
                buttonEvent(3, "pushed")
            break
            case 24: // 2x click
                buttonEvent(4, "pushed")
            break
            case 25: // 3x click
                buttonEvent(5, "pushed")
            break
            case 22: // held
                buttonEvent(3, "held")
            break
            case 23: // release
                buttonEvent(4, "held")
            break
            default:
                logging("Unhandled SceneActivationSet: ${cmd}")
            break
        }
    }  
}

def buttonEvent(button, value) {
    logging("buttonEvent() Button:$button, Value:$value")
	createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	logging(cmd)
	dimmerEvents(cmd)
}

def dimmerEvents(physicalgraph.zwave.Command cmd) {
	logging(cmd)
	def result = []
	def value = (cmd.value ? "on" : "off")
	def switchEvent = createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
	result << switchEvent
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	if (switchEvent.isStateChange) {
		result << response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logging("AssociationReport $cmd")
    state."association${cmd.groupingIdentifier}" = cmd.nodeId[0]
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x84: 1])
	if (encapsulatedCommand) {
		state.sec = 1
		def result = zwaveEvent(encapsulatedCommand)
		result = result.collect {
			if (it instanceof physicalgraph.device.HubAction && !it.toString().startsWith("9881")) {
				response(cmd.CMD + "00" + it.toString())
			} else {
				it
			}
		}
		result
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def versions = [0x31: 5, 0x30: 1, 0x9C: 1, 0x70: 2, 0x85: 2]
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    logging("Unhandled Z-Wave Event: $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	logging(cmd)
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
		} else if (cmd.scale == 1) {
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
		} else if (cmd.scale == 2) {
			return createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		} else {
			return createEvent(name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3])
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd){
    logging("SensorMultilevelReport: $cmd")
	def map = [:]
	switch (cmd.sensorType) {
		case 4:
			map.name = "power"
            map.value = cmd.scaledSensorValue.toInteger().toString()
            map.unit = cmd.scale == 1 ? "Btu/h" : "W"
            break
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}

def on() {
	commands([zwave.basicV1.basicSet(value: 0xFF), zwave.basicV1.basicGet()])
}

def off() {
	commands([zwave.basicV1.basicSet(value: 0x00), zwave.basicV1.basicGet()])
}

def refresh() {
   	logging("$device.displayName refresh()")

    def cmds = []
    if (state.lastRefresh != null && now() - state.lastRefresh < 5000) {
        logging("Refresh Double Press")
        def configuration = parseXml(configuration_model())
        configuration.Value.each
        {
            if ( "${it.@setting_type}" == "zwave" ) {
                cmds << zwave.configurationV1.configurationGet(parameterNumber: "${it.@index}".toInteger())
            }
        } 
        cmds << zwave.firmwareUpdateMdV2.firmwareMdGet()
    } else {
        cmds << zwave.meterV2.meterGet(scale: 0)
        cmds << zwave.meterV2.meterGet(scale: 2)
	    cmds << zwave.basicV1.basicGet()
    }

    state.lastRefresh = now()
    
    commands(cmds)
}

def ping() {
   	logging("$device.displayName ping()")

    def cmds = []

    cmds << zwave.meterV2.meterGet(scale: 0)
    cmds << zwave.meterV2.meterGet(scale: 2)
	cmds << zwave.basicV1.basicGet()

    commands(cmds)
}

def setLevel(level) {
	if(level > 99) level = 99
    if(level < 1) level = 1
    def cmds = []
    cmds << zwave.basicV1.basicSet(value: level)
    cmds << zwave.switchMultilevelV1.switchMultilevelGet()
    
	commands(cmds)
}

def updated()
{
    state.enableDebugging = settings.enableDebugging
    logging("updated() is being called")
    sendEvent(name: "checkInterval", value: 2 * 30 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    state.needfwUpdate = ""
    
    def cmds = update_needed_settings()
    
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    
    response(commands(cmds))
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=1500) {
	delayBetween(commands.collect{ command(it) }, delay)
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


def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    
    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue

    if (settings."${cmd.parameterNumber}" != null)
    {
        if (settings."${cmd.parameterNumber}".toInteger() == convertParam("${cmd.parameterNumber}".toInteger(), cmd2Integer(cmd.configurationValue)))
        {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
        }
    }

    state.currentProperties = currentProperties
}

def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
     
    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    
    if(!state.needfwUpdate || state.needfwUpdate == ""){
       logging("Requesting device firmware version")
       cmds << zwave.firmwareUpdateMdV2.firmwareMdGet()
    }   
    if(!state.association1 || state.association1 == "" || state.association1 == "1"){
       logging("Setting association group 1")
       cmds << zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
       cmds << zwave.associationV2.associationGet(groupingIdentifier:1)
    }
    if(!state.association2 || state.association2 == "" || state.association1 == "2"){
       logging("Setting association group 2")
       cmds << zwave.associationV2.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId)
       cmds << zwave.associationV2.associationGet(groupingIdentifier:2)
    }
   
    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave"){
            if (currentProperties."${it.@index}" == null)
            {
                if (device.currentValue("currentFirmware") == null || "${it.@fw}".indexOf(device.currentValue("currentFirmware")) >= 0){
                    isUpdateNeeded = "YES"
                    logging("Current value of parameter ${it.@index} is unknown")
                    cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                }
            }
            else if (settings."${it.@index}" != null && convertParam(it.@index.toInteger(), cmd2Integer(currentProperties."${it.@index}")) != settings."${it.@index}".toInteger())
            { 
                if (device.currentValue("currentFirmware") == null || "${it.@fw}".indexOf(device.currentValue("currentFirmware")) >= 0){
                    isUpdateNeeded = "YES"

                    logging("Parameter ${it.@index} will be updated to " + settings."${it.@index}")
                    def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger())
                    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                    cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                }
            } 
        }
    }
    
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

/**
* Convert 1 and 2 bytes values to integer
*/
def cmd2Integer(array) { 

switch(array.size()) {
	case 1:
		array[0]
    break
	case 2:
    	((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
    break
    case 3:
    	((array[0] & 0xFF) << 16) | ((array[1] & 0xFF) << 8) | (array[2] & 0xFF)
    break
	case 4:
    	((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
	break
    }
}

def integer2Cmd(value, size) {
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
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
     update_current_properties(cmd)
     logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'")
}

def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd){
    logging("Firmware Report ${cmd.toString()}")
    def firmwareVersion
    switch(cmd.checksum){
       case "3281":
          firmwareVersion = "3.08"
       break;
       default:
          firmwareVersion = cmd.checksum
    }
    state.needfwUpdate = "false"
    updateDataValue("firmware", firmwareVersion.toString())
    createEvent(name: "currentFirmware", value: firmwareVersion)
}

def configure() {
    state.enableDebugging = settings.enableDebugging
    logging("Configuring Device For SmartThings Use")
    def cmds = []

    cmds = update_needed_settings()
    
    if (cmds != []) commands(cmds)
}

def convertParam(number, value) {
	switch (number){
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

private def logging(message) {
    if (state.enableDebugging == null || state.enableDebugging == "true") log.debug "$message"
}


def configuration_model()
{
'''
<configuration>
  <Value type="byte" byteSize="1" index="1" label="Minimum brightness level" min="1" max="98" value="1" setting_type="zwave" fw="3.08">
    <Help>
(parameter is set automatically during the calibration process)
The parameter can be changed manually after the calibration.
Range: 1~98
Default: 1
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="2" label="Maximum brightness level" min="2" max="99" value="99" setting_type="zwave" fw="3.08">
    <Help>
(parameter is set automatically during the calibration process)
The parameter can be changed manually after the calibration.
Range: 2~99
Default: 99
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="5" label="Automatic control - dimming step size" min="1" max="99" value="1" setting_type="zwave" fw="3.08">
    <Help>
This parameter defines the percentage value of dimming step during the automatic control.
Range: 1~99
Default: 1
    </Help>
  </Value>
    <Value type="short" byteSize="2" index="6" label="Automatic control - time of a dimming step" min="0" max="255" value="1" setting_type="zwave" fw="3.08">
    <Help>
This parameter defines the time of single dimming step set in parameter 5 during the automatic control.
Range: 0~255
Default: 1
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="7" label="Manual control - dimming step size" min="1" max="99" value="1" setting_type="zwave" fw="3.08">
    <Help>
This parameter defines the percentage value of dimming step during the manual control.
Range: 1~99
Default: 1
    </Help>
  </Value>
    <Value type="short" byteSize="2" index="8" label="Manual control - time of a dimming step" min="0" max="255" value="5" setting_type="zwave" fw="3.08">
    <Help>
This parameter defines the time of single dimming step set in parameter 7 during the manual control.
Range: 0~255
Default: 5
    </Help>
  </Value>
    <Value type="list" byteSize="1" index="9" label="State of the device after a power failure" min="0" max="1" value="1" setting_type="zwave" fw="3.08">
    <Help>
The Dimmer 2 will return to the last state before power failure.
0 - the Dimmer 2 does not save the state before a power failure, it returns to the "off" position
1 - the Dimmer 2 restores its state before power failure
Range: 0~1
Default: 1
    </Help>
        <Item label="Off" value="0" />
        <Item label="Previous State" value="1" />
  </Value>
  <Value type="short" byteSize="2" index="10" label="Timer functionality (auto - off)" min="0" max="32767" value="0" setting_type="zwave" fw="3.08">
    <Help>
This parameter allows to automatically switch off the device after specified time (seconds) from switching on the light source.
Range: 1~32767
Default: 0
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="19" label="Forced switch on brightness level" min="0" max="99" value="0" setting_type="zwave" fw="3.08">
    <Help>
If the parameter is active, switching on the Dimmer 2 (S1 single click) will always set this brightness level.
Range: 0~99
Default: 0
    </Help>
  </Value>
    <Value type="list" byteSize="1" index="20" label="Switch type" min="0" max="2" value="0" setting_type="zwave" fw="3.08">
    <Help>
Choose between momentary, toggle and roller blind switch.
Range: 0~2
Default: 0
    </Help>
    <Item label="Momentary" value="0" />
    <Item label="Toggle" value="1" />
    <Item label="Roller Blind" value="2" />
  </Value>
      <Value type="list" byteSize="1" index="22" label="Assign toggle switch status to the device status " min="0" max="1" value="0" setting_type="zwave" fw="3.08">
    <Help>
By default each change of toggle switch position results in action of Dimmer 2 (switch on/off) regardless the physical connection of contacts.
0 - device changes status on switch status change
1 - device status is synchronized with switch status 
Range: 0~1
Default: 0
    </Help>
    <Item label="Default" value="0" />
    <Item label="Synchronized" value="1" />
  </Value>
  <Value type="list" byteSize="1" index="23" label="Double click option" min="0" max="1" value="1" setting_type="zwave" fw="3.08">
    <Help>
set the brightness level to MAX
Range: 0~1
Default: 1
    </Help>
    <Item label="Disabled" value="0" />
    <Item label="Enabled" value="1" />
  </Value>
    <Value type="list" byteSize="1" index="26" label="The function of 3-way switch" min="0" max="1" value="0" setting_type="zwave" fw="3.08">
    <Help>
Switch no. 2 controls the Dimmer 2 additionally (in 3-way switch mode). Function disabled for parameter 20 set to 2 (roller blind switch). 
Range: 0~1
Default: 0
    </Help>
    <Item label="Disabled" value="0" />
    <Item label="Enabled" value="1" />
  </Value>
  <Value type="list" byteSize="1" index="28" label="Scene activation functionality" min="0" max="1" value="1" setting_type="zwave" fw="3.08">
    <Help>
SCENE ID depends on the switch type configurations. 
Range: 0~1
Default: 0
    </Help>
    <Item label="Disabled" value="0" />
    <Item label="Enabled" value="1" />
  </Value>
  <Value type="list" byteSize="1" index="29" label="Switch functionality of S1 and S2" min="0" max="1" value="0" setting_type="zwave" fw="3.08">
    <Help>
This parameter allows for switching the role of keys connected to S1 and S2 without changes in connection. 
Range: 0~1
Default: 0
    </Help>
    <Item label="Standard" value="0" />
    <Item label="Switched" value="1" />
  </Value>
    <Value type="list" byteSize="1" index="35" label="Auto-calibration after power on" min="0" max="4" value="1" setting_type="zwave" fw="3.08">
    <Help>
This parameter determines the trigger of auto-calibration procedure, e.g. power on, load error, etc.
0 - No auto-calibration of the load after power on
1 - Auto-calibration performed after first power on
2 - Auto-calibration performed after each power on
3 - Auto-calibration performed after first power on or after each LOAD ERROR alarm (no load, load failure, burnt out bulb), if parameter 37 is set to 1 also after alarms: SURGE (Dimmer 2 output overvoltage) and OVERCURRENT (Dimmer 2 output overcurrent)
4 - Auto-calibration performed after each power on or after each LOAD ERROR alarm (no load, load failure, burnt out bulb), if parameter 37 is set to 1 also after alarms: SURGE (Dimmer 2 output overvoltage) and OVERCURRENT (Dimmer 2 output overcurrent) 
Range: 0~4
Default: 1
    </Help>
    <Item label="0" value="0" />
    <Item label="1" value="1" />
    <Item label="2" value="2" />
    <Item label="3" value="3" />
    <Item label="4" value="4" />
  </Value>
<Value type="short" byteSize="2" index="39" label="Max Power load" min="0" max="350" value="250" setting_type="zwave" fw="3.08">
    <Help>
This parameter defines the maximum load for a dimmer.
Range: 0~350
Default: 250
    </Help>
  </Value>
      <Value type="list" byteSize="1" index="40" label="Reaction to General Alarm" min="0" max="3" value="3" setting_type="zwave" fw="">
    <Help>
This parameter determines how the device will react to General Alarm frame.
Range: 0~3
Default: 3 (Flash)
    </Help>
    <Item label="Alarm frame is ignored" value="0" />
    <Item label="Turn ON after receiving the alarm frame" value="1" />
    <Item label="Turn OFF after receiving the alarm frame" value="2" />
	<Item label="Flash after receiving the alarm frame" value="3" />
  </Value>
      <Value type="list" byteSize="1" index="41" label="Reaction to Flood Alarm" min="0" max="3" value="2" setting_type="zwave" fw="">
    <Help>
This parameter determines how the device will react to Flood Alarm frame.
Range: 0~3
Default: 2 (OFF)
    </Help>
    <Item label="Alarm frame is ignored" value="0" />
    <Item label="Turn ON after receiving the alarm frame" value="1" />
    <Item label="Turn OFF after receiving the alarm frame" value="2" />
	<Item label="Flash after receiving the alarm frame" value="3" />
  </Value>
      <Value type="list" byteSize="1" index="42" label="Reaction to CO/CO2/Smoke Alarm" min="0" max="3" value="3" setting_type="zwave" fw="">
    <Help>
This parameter determines how the device will react to CO, CO2 or Smoke frame. 
Range: 0~3
Default: 3 (Flash)
    </Help>
    <Item label="Alarm frame is ignored" value="0" />
    <Item label="Turn ON after receiving the alarm frame" value="1" />
    <Item label="Turn OFF after receiving the alarm frame" value="2" />
	<Item label="Flash after receiving the alarm frame" value="3" />
  </Value>
      <Value type="list" byteSize="1" index="43" label="Reaction to Heat Alarm" min="0" max="3" value="1" setting_type="zwave" fw="">
    <Help>
This parameter determines how the device will react to Heat Alarm frame.
Range: 0~3
Default: 1 (ON)
    </Help>
    <Item label="Alarm frame is ignored" value="0" />
    <Item label="Turn ON after receiving the alarm frame" value="1" />
    <Item label="Turn OFF after receiving the alarm frame" value="2" />
	<Item label="Flash after receiving the alarm frame" value="3" />
  </Value>
        <Value type="byte" byteSize="2" index="44" label="Flashing alarm duration" min="1" max="32000" value="600" setting_type="zwave" fw="">
    <Help>
This parameter allows to set duration of flashing alarm mode. 
Range: 1~32000 (1s-32000s)
Default: 600 (10 min)
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="50" label="Active power reports" min="0" max="100" value="10" setting_type="zwave" fw="3.08">
    <Help>
The parameter defines the power level change that will result in a new power report being sent. The value is a percentage of the previous report.
Range: 0~100
Default: 10
    </Help>
  </Value>
    <Value type="short" byteSize="2" index="52" label="Periodic active power and energy reports" min="0" max="32767" value="3600" setting_type="zwave" fw="3.08">
    <Help>
Parameter 52 defines a time period between consecutive reports. Timer is reset and counted from zero after each report. 
Range: 0~32767
Default: 3600
    </Help>
  </Value>
    <Value type="short" byteSize="2" index="53" label="Energy reports" min="0" max="255" value="10" setting_type="zwave" fw="3.08">
    <Help>
Energy level change which will result in sending a new energy report.
Range: 0~255
Default: 10
    </Help>
  </Value>
    <Value type="list" byteSize="1" index="54" label="Self-measurement" min="0" max="1" value="0" setting_type="zwave" fw="3.08">
    <Help>
The Dimmer 2 may include active power and energy consumed by itself in reports sent to the main controller.
Range: 0~1
Default: 0
    </Help>
    <Item label="Disabled" value="0" />
    <Item label="Enabled" value="1" />
  </Value>
    <Value type="boolean" index="enableDebugging" label="Enable Debug Logging?" value="true" setting_type="preference" fw="3.08">
    <Help>

    </Help>
  </Value>
</configuration>
'''
}
