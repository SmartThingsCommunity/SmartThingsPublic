/**
 *
 *  Qubino Flush Dimmer
 *   
 *	github: Eric Maycock (erocm123)
 *	Date: 2017-02-21
 *	Copyright Eric Maycock
 *
 *  Includes all configuration parameters and ease of advanced configuration. 
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

	definition (name: "Qubino Flush Dimmer", namespace: "erocm123", author: "Eric Maycock", vid:"generic-dimmer-power-energy") {
		capability "Actuator"
		capability "Switch"
        capability "Switch Level"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Relay Switch"
        capability "Configuration"
        capability "Energy Meter"
        capability "Power Meter"
        capability "Temperature Measurement"
        capability "Health Check"
        
        command "reset"

        fingerprint mfr: "0159", prod: "0001", model: "0051"
		fingerprint deviceId: "0x1101", inClusters: "0x5E,0x86,0x72,0x5A,0x73,0x20,0x27,0x25,0x26,0x32,0x85,0x8E,0x59,0x70", outClusters: "0x20,0x26"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x86,0x72,0x5A,0x73,0x20,0x27,0x25,0x26,0x30,0x32,0x60,0x85,0x8E,0x59,0x70", outClusters: "0x20,0x26"
        
	}

	simulator {
	}
    
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())  
    }

	tiles{
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
			   attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			   attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
			   attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			   attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
			}
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
           		attributeState "statusText", label:'${currentValue}'       		
            }
	    }
        childDeviceTiles("all")
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		standardTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
        valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
            state "temperature", label:'${currentValue}°',
            backgroundColors:
             [
                [value: 31, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
        }
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		    state "default", label:'reset kWh', action:"reset"
	    }
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }
	}
}

def installed() {
    logging("installed()", 1)
	command(zwave.manufacturerSpecificV1.manufacturerSpecificGet())
	createChildDevices()
}

def parse(String description) {
	def result = []
    if (description != "updated") {
	   logging("description: $description", 1)
	   def cmd = zwave.parse(description, [0x20: 1, 0x70: 1])
	   if (cmd) {
		   result += zwaveEvent(cmd)
	   }
    }

    def statusTextmsg = ""
    result.each {
        if ((it instanceof Map) == true && it.find{ it.key == "name" }?.value == "power") {
            statusTextmsg = "${it.value} W ${device.currentValue('energy')? device.currentValue('energy') : "0"} kWh"
        }
        if ((it instanceof Map) == true && it.find{ it.key == "name" }?.value == "energy") {
            statusTextmsg = "${device.currentValue('power')? device.currentValue('power') : "0"} W ${it.value} kWh"
        }
    }
    if (statusTextmsg != "") sendEvent(name:"statusText", value:statusTextmsg, displayed:false)
    
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    logging("BasicReport: $cmd", 2)
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    logging("MeterReport $cmd", 2)
    def event
	if (cmd.scale == 0) {
       event = createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
	} else if (cmd.scale == 2) {
		event = createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
	}
    return event
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    logging("SwitchBinaryReport: $cmd", 2)
    if (cmd.value != 254) {  
       createEvent(name: "switch", value: cmd.value? "on" : "off", type: "digital")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	logging("SwitchMultilevelReport: $cmd", 2)
	dimmerEvents(cmd)
}

def dimmerEvents(physicalgraph.zwave.Command cmd) {
	logging("dimmerEvents: $cmd", 1)
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

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
    logging("SensorMultilevelReport: $cmd", 2)
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
            logging("Temperature Report: $map.value", 2)
			break;
		default:
			map.descriptionText = cmd.toString()
	}
    
    return createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    logging("ManufacturerSpecificReport: $cmd", 2)
	if (state.manufacturer != cmd.manufacturerName) {
		updateDataValue("manufacturer", cmd.manufacturerName)
	}

	createEvent(name: "manufacturer", value: cmd.manufacturerName)
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logging("AssociationReport $cmd", 2)
    state."association${cmd.groupingIdentifier}" = cmd.nodeId[0]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
    logging("SensorBinaryReport: $cmd", 2)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    logging("NotificationReport: $cmd", 2)
    def result
	
	
	if (cmd.notificationType == 2) {
    def children = childDevices
	def childDevice = children.find{it.deviceNetworkId.endsWith("-i2")}
		switch (cmd.event) {
			case 0:
                switch(settings."i2")
                {
                    case "Motion Sensor Child Device":
                        childDevice.sendEvent(name: "motion", value: "active")
                    break
                    case "Carbon Monoxide Detector Child Device":
                        childDevice.sendEvent(name: "carbonMonoxide", value: "detected")
                    break
                    case "Carbon Dioxide Detector Child Device":
                        childDevice.sendEvent(name: "carbonDioxide", value: "detected")
                    break
                    case "Water Sensor Child Device":
                        childDevice.sendEvent(name: "water", value: "wet")
                    break
                    case "Smoke Detector Child Device":
                        childDevice.sendEvent(name: "smoke", value: "detected")
                    break
                    case "Contact Sensor Child Device":
                        childDevice.sendEvent(name: "contact", value: "open")
                    break
                }
			break
			case 2:
                switch(settings."i2")
                {
			        case "Motion Sensor Child Device":
                        childDevice.sendEvent(name: "motion", value: "inactive")
                    break
                    case "Carbon Monoxide Detector Child Device":
                        childDevice.sendEvent(name: "carbonMonoxide", value: "clear")
                    break
                    case "Carbon Dioxide Detector Child Device":
                        childDevice.sendEvent(name: "carbonDioxide", value: "clear")
                    break
                    case "Water Sensor Child Device":
                        childDevice.sendEvent(name: "water", value: "dry")
                    break
                    case "Smoke Detector Child Device":
                        childDevice.sendEvent(name: "smoke", value: "clear")
                    break
                    case "Contact Sensor Child Device":
                        childDevice.sendEvent(name: "contact", value: "closed")
                    break
                }
			break
		}
	} 
    else if (cmd.notificationType == 5) {
    def children = childDevices
	def childDevice = children.find{it.deviceNetworkId.endsWith("-i3")}
		switch (cmd.event) {
			case 0:
                switch(settings."i3")
                {
                    case "Motion Sensor Child Device":
                        childDevice.sendEvent(name: "motion", value: "active")
                    break
                    case "Carbon Monoxide Detector Child Device":
                        childDevice.sendEvent(name: "carbonMonoxide", value: "detected")
                    break
                    case "Carbon Dioxide Detector Child Device":
                        childDevice.sendEvent(name: "carbonDioxide", value: "detected")
                    break
                    case "Water Sensor Child Device":
                        childDevice.sendEvent(name: "water", value: "wet")
                    break
                    case "Smoke Detector Child Device":
                        childDevice.sendEvent(name: "smoke", value: "detected")
                    break
                    case "Contact Sensor Child Device":
                        childDevice.sendEvent(name: "contact", value: "open")
                    break
                }
			break
			case 2:
                switch(settings."i3")
                {
			        case "Motion Sensor Child Device":
                        childDevice.sendEvent(name: "motion", value: "inactive")
                    break
                    case "Carbon Monoxide Detector Child Device":
                        childDevice.sendEvent(name: "carbonMonoxide", value: "clear")
                    break
                    case "Carbon Dioxide Detector Child Device":
                        childDevice.sendEvent(name: "carbonDioxide", value: "clear")
                    break
                    case "Water Sensor Child Device":
                        childDevice.sendEvent(name: "water", value: "dry")
                    break
                    case "Smoke Detector Child Device":
                        childDevice.sendEvent(name: "smoke", value: "clear")
                    break
                    case "Contact Sensor Child Device":
                        childDevice.sendEvent(name: "contact", value: "closed")
                    break
                }
			break
		}
	}else {
        logging("Need to handle this cmd.notificationType: ${cmd.notificationType}", 2)
		result = createEvent(descriptionText: cmd.toString(), isStateChange: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand([0x98: 1, 0x20: 1, 0x70: 1, 0x25: 1])
    if (encapsulatedCommand) {
        state.sec = 1
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logging("$device.displayName: Unhandled: $cmd", 2)
	[:]
}

def on() {
    logging("on()", 1)
	commands([
		zwave.basicV1.basicSet(value: 0xFF),
		zwave.switchBinaryV1.switchBinaryGet()
	])
}

def off() {
    logging("off()", 1)
	commands([
		zwave.basicV1.basicSet(value: 0x00),
		zwave.switchBinaryV1.switchBinaryGet()
	])
}

def setLevel(level) {
    logging("setLevel($level)", 1)
	if(level > 99) level = 99
    if(level < 1) level = 1
    commands([
        zwave.basicV1.basicSet(value: level),
    ])
}

def poll() {
    logging("poll()", 1)
	command(zwave.switchBinaryV1.switchBinaryGet())
}

def refresh() {
    logging("refresh()", 1)
    commands([
		zwave.switchBinaryV1.switchBinaryGet(),
		zwave.meterV2.meterGet(scale: 0),
		zwave.meterV2.meterGet(scale: 2),
        zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
	])
}

def reset() {
    logging("reset()", 1)
    commands([
        zwave.meterV2.meterReset(),
        zwave.meterV2.meterGet()
    ])
}

def ping() {
    logging("ping()", 1)
	refresh()
}

def configure() {
    logging("configure()", 1)
    def cmds = []
    cmds = update_needed_settings()
    if (cmds != []) commands(cmds)
}

def updated()
{
    logging("updated()", 1)
    if (!childDevices) {
		createChildDevices()
	}
	else if (device.label != state.oldLabel) {
		childDevices.each {
			def newLabel = "${device.displayName} (i${channelNumber(it.deviceNetworkId)})"
			it.setLabel(newLabel)
		}
		state.oldLabel = device.label
	}
    if (childDevices) {
        def childDevice = childDevices.find{it.deviceNetworkId.endsWith("-i2")}
        if (childDevice && settings."i2" && settings."i2" != "Disabled"  && childDevice.typeName != settings."i2") {
            childDevice.setDeviceType(settings."i2")
        }
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-i3")}
        if (childDevice && settings."i3" && settings."i3" != "Disabled" && childDevice.typeName != settings."i3") {   
            childDevice.setDeviceType(settings."i3")
        }
    }
    def cmds = [] 
    cmds = update_needed_settings()
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    if (cmds != []) response(commands(cmds))
}

def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
   
    configuration.Value.each
    {
        if(it.@hidden != "true" && it.@disabled != "true"){
        switch(it.@type)
        {   
            case ["number"]:
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
}

 /*  Code has elements from other community source @CyrilPeponnet (Z-Wave Parameter Sync). */

def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    
    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue
    
    def parameterSettings = parseXml(configuration_model()).Value.find{it.@index == "${cmd.parameterNumber}"}

    if (settings."${cmd.parameterNumber}" != null || parameterSettings.@hidden == "true")
    {
        if (convertParam(cmd.parameterNumber, parameterSettings.@hidden != "true"? settings."${cmd.parameterNumber}" : parameterSettings.@value) == cmd2Integer(cmd.configurationValue))
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
    
    if(!state.association9 || state.association9 == "" || state.association9 != 1){
       logging("Setting association group 9", 1)
       cmds << zwave.associationV2.associationSet(groupingIdentifier:9, nodeId:zwaveHubNodeId)
       cmds << zwave.associationV2.associationGet(groupingIdentifier:9)
    }
    if(!state.association6 || state.association6 == "" || state.association6 != 1){
       logging("Setting association group 6", 1)
       cmds << zwave.associationV2.associationSet(groupingIdentifier:6, nodeId:zwaveHubNodeId)
       cmds << zwave.associationV2.associationGet(groupingIdentifier:6)
    }

    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave" && it.@disabled != "true"){
            if (currentProperties."${it.@index}" == null)
            {
               if (it.@setonly == "true"){
                  logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"), 2)
                  def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}")
                  cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
               } else {
                  isUpdateNeeded = "YES"
                  logging("Current value of parameter ${it.@index} is unknown", 2)
                  cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
               }
            }
            else if ((settings."${it.@index}" != null || "${it.@hidden}" == "true") && cmd2Integer(currentProperties."${it.@index}") != convertParam(it.@index.toInteger(), "${it.@hidden}" != "true"? settings."${it.@index}" : "${it.@value}"))
            { 
                isUpdateNeeded = "YES"
                logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), "${it.@hidden}" != "true"? settings."${it.@index}" : "${it.@value}"), 2)
                def convertedConfigurationValue = convertParam(it.@index.toInteger(), "${it.@hidden}" != "true"? settings."${it.@index}" : "${it.@value}")
                cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
            } 
        }
    }
    
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

def convertParam(number, value) {
   def parValue
   switch (number){
   case 110:
      if (value < 0)
         parValue = value * -1 + 1000
      else 
         parValue = value
   break
   default:
      parValue = value
   break
   }
   return parValue.toInteger()
}

private def logging(message, level) {
    if (logLevel != "0"){
    switch (logLevel) {
       case "1":
          if (level > 1)
             log.debug "$message"
       break
       case "99":
          log.debug "$message"
       break
    }
    }
}

/**
* Convert byte values to integer
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

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
     update_current_properties(cmd)
     logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'", 2)
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec || zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=500) {
	delayBetween(commands.collect{ command(it) }, delay)
}

private channelNumber(String dni) {
	dni.split("-i")[-1] as Integer
}

private void createChildDevices() {
	state.oldLabel = device.label
    try {
	   for (i in 2..3) {
          addChildDevice("erocm123", "Contact Sensor Child Device", "${device.deviceNetworkId}-i${i}", null,
			 [completedSetup: true, label: "${device.displayName} (i${i})",
		     isComponent: true, componentName: "i$i", componentLabel: "Input $i"])
	   }
    } catch (e) {
       runIn(2, "sendAlert")
    }
}

private sendAlert() {
   sendEvent(
      descriptionText: "Child device creation failed. Please make sure that the \"Contact Sensor Child Device\" is installed and published.",
	  eventType: "ALERT",
	  name: "childDeviceCreation",
	  value: "failed",
	  displayed: true,
   )
}

def configuration_model()
{
'''
<configuration>
<Value type="list" byteSize="1" index="1" label="Input 1 switch type" min="0" max="1" value="1" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: Bi-stable switch type (Toggle)
</Help>
        <Item label="Mono-stable switch type (Push button)" value="0" />
        <Item label="Bi-stable switch type (Toggle)" value="1" />
</Value>
<Value type="list" byteSize="1" index="2" label="Input 2 switch type" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: Bi-stable switch type (Toggle)
</Help>
        <Item label="Mono-stable switch type (Push button)" value="0" />
        <Item label="Bi-stable switch type (Toggle)" value="1" />
</Value>
<Value type="list" byteSize="1" index="3" label="Input 2 contact type" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: 0 (NO - Normally Open)
</Help>
        <Item label="NO (normally open) input type" value="0" />
        <Item label="NC (normally closed) input type" value="1" />
</Value>
<Value type="list" byteSize="1" index="4" label="Input 3 contact type" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: 0 (NO - Normally Open)
</Help>
        <Item label="NO (normally open) input type" value="0" />
        <Item label="NC (normally closed) input type" value="1" />
</Value>
<Value type="list" byteSize="2" index="10" label="Activate / deactivate functions ALL ON/ALL OFF" min="0" max="255" value="255" setting_type="zwave" fw="">
 <Help>
ALL ON active
Range: 0, 1, 2, 255
Default: ALL ON active, ALL OFF active
</Help>
        <Item label="ALL ON active, ALL OFF active" value="255" />
        <Item label="ALL ON is not active, ALL OFF is not active" value="0" />
        <Item label="ALL ON is not active, ALL OFF active" value="1" />
        <Item label="ALL ON active, ALL OFF is not active" value="2" />
</Value>
<Value type="number" byteSize="2" index="11" label="Automatic turning off output after set time " min="0" max="32535" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 32535
Default: 0 (Disabled)
</Help>
</Value>
<Value type="number" byteSize="2" index="12" label="Automatic turning on output after set time " min="0" max="32535" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 32535
Default: 0 (Disabled)
</Help>
</Value>
<Value type="list" byteSize="1" index="20" label="Enable/Disable 3way/Additional Switch" min="0" max="2" value="0" setting_type="zwave" fw="">
 <Help>
Dimming is done by push button or switch connected to I1 (by  default).  Enabling  3way  switch,  dimming  can  be controlled by push button or switch connected to I1 and I2.
Range: 0 to 2
Default: Disabled
</Help>
        <Item label="Disabled" value="0" />
        <Item label="3-way" value="1" />
        <Item label="Additional" value="2" />
</Value>
<Value type="list" byteSize="1" index="21" label="Enable/Disable Double click function" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
If Double click function is enabled, a fast double click on the push  button  will  set  dimming  power  at  maximum  dimming value.
Range: 0 to 1
Default: Disabled
</Help>
        <Item label="Disabled" value="0" />
        <Item label="Enabled" value="1" />
</Value>
<Value type="list" byteSize="1" index="30" label="State of the relay after a power failure" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: 0 (Previous State)
</Help>
        <Item label="Previous" value="0" />
        <Item label="Off" value="1" />
</Value>
<Value type="number" byteSize="1" index="40" label="Power reporting in Watts on power change %" min="0" max="100" value="5" setting_type="zwave" fw="">
 <Help>
Range: 0 (Disabled) to 100
Default: 5 (%)
</Help>
</Value>
<Value type="number" byteSize="2" index="42" label="Power reporting in Watts by time interval" min="0" max="32767" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 32767
Default: 0 (Disabled)
</Help>
</Value>
<Value type="number" byteSize="1" index="60" label="Minimum dimming value" min="1" max="98" value="1" setting_type="zwave" fw="">
 <Help>
Range: 1 to 98
Default: 1 (%)
</Help>
</Value>
<Value type="number" byteSize="1" index="61" label="Maximum dimming value" min="2" max="99" value="99" setting_type="zwave" fw="">
 <Help>
Range: 2 to 99
Default: 99 (%)
</Help>
</Value>
<Value type="number" byteSize="2" index="65" label="Dimming time (soft on/off)" min="50" max="255" value="100" setting_type="zwave" fw="">
 <Help>
In 10 ms
Range: 50 to 255
Default: 100 (1000 ms = 1 seconds)
</Help>
</Value>
<Value type="number" byteSize="2" index="66" label="Dimming time when key pressed" min="1" max="255" value="3" setting_type="zwave" fw="">
 <Help>
Range: 1 to 255 
Default: 3 seconds
</Help>
</Value>
<Value type="list" byteSize="1" index="67" label="Ignore start level" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: Disabled
</Help>
        <Item label="Disabled" value="0" />
        <Item label="Enabled" value="1" />
</Value>
<Value type="number" byteSize="1" index="68" label="Dimming duration" min="0" max="127" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 127
Default: 0 (Dimming duration according to parameter 66)
</Help>
</Value>
<Value type="list" byteSize="1" index="100" label="Enable / Disable Endpoint I2 or select Notification Type and Event" min="0" max="9" value="2" setting_type="zwave" fw="" hidden="true">
 <Help>
Range: 0 to 6, 9
Default: Home Security; Motion Detection, unknown location
</Help>
        <Item label="Disabled" value="0" />
        <Item label="Home Security; Motion Detection" value="1" />
        <Item label="Carbon Monoxide; Carbon Monoxide detected" value="2" />
        <Item label="Carbon Dioxide; Carbon Dioxide detected" value="3" />
        <Item label="Water Alarm; Water Leak detected" value="4" />
        <Item label="Heat Alarm; Overheat detected" value="5" />
        <Item label="Smoke Alarm; Smoke detected" value="6" />
        <Item label="Sensor Binary" value="9" />
</Value>
<Value type="list" byteSize="1" index="101" label="Enable / Disable Endpoint I3 or select Notification Type and Event" min="0" max="9" value="4" setting_type="zwave" fw="" hidden="true">
 <Help>
Range: 0 to 6, 9
Default: Home Security; Motion Detection, unknown location
</Help>
        <Item label="Disabled" value="0" />
        <Item label="Home Security; Motion Detection" value="1" />
        <Item label="Carbon Monoxide; Carbon Monoxide detected" value="2" />
        <Item label="Carbon Dioxide; Carbon Dioxide detected" value="3" />
        <Item label="Water Alarm; Water Leak detected" value="4" />
        <Item label="Heat Alarm; Overheat detected" value="5" />
        <Item label="Smoke Alarm; Smoke detected" value="6" />
        <Item label="Sensor Binary" value="9" />
</Value>
<Value type="list" byteSize="1" index="i2" label="Enable / Disable Endpoint I2 or select Notification Type and Event" min="0" max="9" value="Contact Sensor Child Device" setting_type="preference" fw="">
 <Help>
Range: 0 to 6, 9
Default: Home Security; Motion Detection, unknown location
</Help>
        <Item label="Disabled" value="Disabled" />
        <Item label="Home Security; Motion Detection" value="Motion Sensor Child Device" />
        <Item label="Carbon Monoxide; Carbon Monoxide detected" value="Carbon Monoxide Detector Child Device" />
        <Item label="Carbon Dioxide; Carbon Dioxide detected" value="Carbon Dioxide Detector Child Device" />
        <Item label="Water Alarm; Water Leak detected" value="Water Sensor Child Device" />
        <Item label="Smoke Alarm; Smoke detected" value="Smoke Detector Child Device" />
        <Item label="Sensor Binary" value="Contact Sensor Child Device" />
</Value>
<Value type="list" byteSize="1" index="i3" label="Enable / Disable Endpoint I3 or select Notification Type and Event" min="0" max="9" value="Contact Sensor Child Device" setting_type="preference" fw="">
 <Help>
Range: 0 to 6, 9
Default: Home Security; Motion Detection, unknown location
</Help>
        <Item label="Disabled" value="Disabled" />
        <Item label="Home Security; Motion Detection" value="Motion Sensor Child Device" />
        <Item label="Carbon Monoxide; Carbon Monoxide detected" value="Carbon Monoxide Detector Child Device" />
        <Item label="Carbon Dioxide; Carbon Dioxide detected" value="Carbon Dioxide Detector Child Device" />
        <Item label="Water Alarm; Water Leak detected" value="Water Sensor Child Device" />
        <Item label="Smoke Alarm; Smoke detected" value="Smoke Detector Child Device" />
        <Item label="Sensor Binary" value="Contact Sensor Child Device" />
</Value>
<Value type="number" byteSize="2" index="110" label="Temperature sensor offset settings" min="-100" max="100" value="0" setting_type="zwave" fw="">
 <Help>
In tenths. i.e. 1 = 0.1C, -15 = -1.5C
Range: -100 to 100
Default: 0
</Help>
</Value>
<Value type="number" byteSize="1" index="120" label="Digital temperature sensor reporting" min="0" max="127" value="5" setting_type="zwave" fw="">
 <Help>
Range: 0 to 127
Default: 5 (0.5°C change)
</Help>
</Value>
<Value type="list" byteSize="1" index="250" label="Secure Inclusion" min="0" max="1" value="0" setting_type="zwave" fw="" disabled="true">
 <Help>
Range: 0 to 1
Default: Disabled
</Help>
        <Item label="Disabled" value="0" />
        <Item label="Enabled" value="1" />
</Value>
  <Value type="list" index="logLevel" label="Debug Logging Level?" value="0" setting_type="preference" fw="">
    <Help>
    </Help>
        <Item label="None" value="0" />
        <Item label="Reports" value="1" />
        <Item label="All" value="99" />
  </Value>
</configuration>
'''
}
