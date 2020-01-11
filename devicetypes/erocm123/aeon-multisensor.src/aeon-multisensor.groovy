/*
 * 
 * Uses some original code from @Duncan Aeon Multisensor 6 code for secure configuration, Copyright 2015 SmartThings, modified for setting
 * preferences around configuration and the reporting of tampering and ultraviolet index, and reconfiguration after pairing
 *
 *  Eric Maycock
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
	definition (name: "Aeon Multisensor", namespace: "erocm123", author: "Eric Maycock", vid:"generic-motion-7") {
		capability "Motion Sensor"
		capability "Acceleration Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Illuminance Measurement"
		capability "Ultraviolet Index" 
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
        capability "Refresh"
		
		// CCs supported - 94, 134, 114, 132, 89, 133, 115, 113, 128, 48, 49, 112, 152, 122
		attribute "tamper", "enum", ["detected", "clear"]
        attribute   "needUpdate", "string"
        
		fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x7A", outClusters: "0x5A"
		}
        preferences {
        input description: "Once you change values on this page, the \"Synced\" Status will become \"Pending\" status.\
You can then force the sync by triple clicking the device button or just wait for the\
next WakeUp (60 minutes).",

        displayDuringSetup: false, type: "paragraph", element: "paragraph"
        
		generate_preferences(configuration_model())
              
        input "tempOffset", "decimal", title: "Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
    }
	simulator {
		status "no motion" : "command: 9881, payload: 00300300"
		status "motion"    : "command: 9881, payload: 003003FF"
        status "clear" : " command: 9881, payload: 0071050000000007030000"
        status "tamper" : "command: 9881, payload: 007105000000FF07030000"
        
        for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                	scaledSensorValue: i,
                    precision: 1,
                    sensorType: 1,
                    scale: 1
				)
			).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 20) {
			status "RH ${i}%": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                	scaledSensorValue: i,
                    sensorType: 5
            	)
			).incomingMessage()
		}
		for (int i in [0, 1, 2, 8, 12, 16, 20, 24, 30, 64, 82, 100, 200, 500, 1000]) {
			status "illuminance ${i} lux": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                scaledSensorValue: i,
                sensorType: 3
                )
			).incomingMessage()
		}
		for (int i = 0; i <= 11; i += 1) {
			status "ultravioletultravioletIndex ${i}": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                scaledSensorValue: i,
                sensorType: 27
                )
			).incomingMessage()
		}
		for (int i in [0, 5, 10, 15, 50, 99, 100]) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
                batteryLevel: i
                )
			).incomingMessage()
		}
		status "low battery alert": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
            	batteryLevel: 255
            	)
			).incomingMessage()
		status "wake up": "command: 8407, payload:"
	}
	tiles (scale: 2) {
		multiAttributeTile(name:"main", type:"generic", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
            	attributeState "temperature",label:'${currentValue}°', icon:"st.motion.motion.inactive", backgroundColors:[
                	[value: 32, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
				]
            }
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
                attributeState "humidity",label:'RH ${currentValue} %',unit:""
            }
		}
        standardTile("motion","device.motion", width: 2, height: 2) {
            	state "active",label:'motion',icon:"st.motion.motion.active",backgroundColor:"#53a7c0"
                state "inactive",label:'no motion',icon:"st.motion.motion.inactive",backgroundColor:"#ffffff"
			}
		valueTile("temperature","device.temperature", width: 2, height: 2) {
            	state "temperature",label:'${currentValue}°',backgroundColors:[
                	[value: 32, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
				]
			}
		valueTile("humidity","device.humidity", width: 2, height: 2) {
           	state "humidity",label:'RH ${currentValue} %',unit:""
			}
		valueTile(
        	"illuminance","device.illuminance", width: 2, height: 2) {
            	state "luminosity",label:'${currentValue} ${unit}', unit:"lux", backgroundColors:[
                	[value: 0, color: "#000000"],
                    [value: 1, color: "#060053"],
                    [value: 3, color: "#3E3900"],
                    [value: 12, color: "#8E8400"],
					[value: 24, color: "#C5C08B"],
					[value: 36, color: "#DAD7B6"],
					[value: 128, color: "#F3F2E9"],
                    [value: 1000, color: "#FFFFFF"]
				]
			}
		valueTile(
        	"ultravioletIndex","device.ultravioletIndex", width: 2, height: 2) {
				state "ultravioletIndex",label:'${currentValue} UV INDEX',unit:""
			}
		standardTile("acceleration", "device.acceleration", width: 2, height: 2) {
			state("active", label:'tamper', icon:"st.motion.acceleration.active", backgroundColor:"#f39c12")
			state("inactive", label:'clear', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
		}
		/*standardTile(
        	"tamper","device.tamper", width: 2, height: 2) {
				state "tamper",label:'tamper',icon:"st.motion.motion.active",backgroundColor:"#ff0000"
                state "clear",label:'clear',icon:"st.motion.motion.inactive",backgroundColor:"#00ff00"
			}*/
		valueTile(
			"battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        standardTile("refresh", "device.switch", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}
        standardTile("configure", "device.needUpdate", width: 2, height: 2) {
            state("NO" , label:'Synced', action:"configuration.configure", icon:"st.motion.active", backgroundColor:"#8acb47")
            state("YES", label:'Pending', action:"configuration.configure", icon:"st.motion.inactive", backgroundColor:"#f39c12")
        }
		main([
        	"main", "motion"
            ])
		details([
        	"main","humidity","illuminance","ultravioletIndex","motion","acceleration","battery", "refresh", "configure"
            ])
	}
	
}

def parse(String description)
{
	def result = []
    switch(description){
        case ~/Err 106.*/:
			state.sec = 0
			result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
			descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
        break
		case "updated":
        	log.debug "Update is hit when the device is paired."
            result << response(zwave.wakeUpV1.wakeUpIntervalSet(seconds: 900, nodeid:zwaveHubNodeId).format())
            result << response(zwave.batteryV1.batteryGet().format())
            result << response(zwave.versionV1.versionGet().format())
            result << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())
            result << response(zwave.firmwareUpdateMdV2.firmwareMdGet().format())
            result << response(configure())
        break
        default:
			def cmd = zwave.parse(description, [0x31: 5, 0x30: 2, 0x84: 1])
			if (cmd) {
				result += zwaveEvent(cmd)
			}
        break
	}
	//log.debug "Parsed '${description}' to ${result.inspect()}"
	if ( result[0] != null ) { result }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	//log.debug "${cmd}"
	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x30: 2, 0x84: 1])
	state.sec = 1
	//log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	response(configure())
}

/**
* This will be called each time we update a paramter. Use it to fill our currents parameters as a callback
*/
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    update_current_properties(cmd)
    log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug "---CONFIGURATION REPORT V1--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = now()
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
            state.realTemperature = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.value = getAdjustedTemp(state.realTemperature)
			map.unit = getTemperatureScale()
			break;
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "lux"
			break;
        case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break;
		case 27:
        	map.name = "ultravioletIndex"
            map.value = cmd.scaledSensorValue.toInteger()
            map.unit = ""
            break;
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}

def motionEvent(value) {
	def map = [name: "motion"]
	if (value) {
		map.value = "active"
		map.descriptionText = "$device.displayName detected motion"
	} else {
		map.value = "inactive"
		map.descriptionText = "$device.displayName motion has stopped"
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	setConfigured()
	motionEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	motionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				result << motionEvent(0)
				result << createEvent(name: "acceleration", value: "inactive", descriptionText: "$device.displayName tamper cleared")
				break
			case 3:
				result << createEvent(name: "acceleration", value: "active", descriptionText: "$device.displayName was moved")
				break
			case 7:
				result << motionEvent(1)
				break
		}
	} else {
		result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
	}
	result
}

/**
* This is called each time your device will wake up.
*/
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	//return zwave.configurationV1.configurationGet(parameterNumber: 3).format()
	//zwave.wakeUpV1.wakeUpIntervalGet().format()
    log.debug "Device ${device.displayName} woke up"
    //zwave.wakeUpV2.wakeUpIntervalCapabilitiesGet().format()
    def request = sync_properties()
    //def commands = []
    request << zwave.wakeUpV2.wakeUpIntervalSet(seconds: 900, nodeid:zwaveHubNodeId)
    //commands << zwave.wakeUpV2.wakeUpIntervalGet().format()
    sendEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
    // check if we need to request battery level (every 48h)
    /*if (!state.lastBatteryReport || (now() - state.lastBatteryReport)/60000 >= 60 * 48)
    {
        commands << zwave.batteryV1.batteryGet().format()
    }*/
    // Adding No More infomration needed at the end
    //commands << zwave.wakeUpV1.wakeUpNoMoreInformation()
    response(commands(request) + ["delay 15000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()])
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd)
{
	log.debug "${cmd.toString()}"
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	log.debug "Device ${device.displayName} woke up v2"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
    log.debug "Unknown Z-Wave Command"
}

def refresh() {
   	log.debug "Aeon Multisensor 6 refresh()"
    def request = [
    	zwave.configurationV1.configurationGet(parameterNumber: 3),
    	zwave.configurationV1.configurationGet(parameterNumber: 4),
    	zwave.configurationV1.configurationGet(parameterNumber: 111),
    	zwave.configurationV1.configurationGet(parameterNumber: 201),
        zwave.configurationV1.configurationGet(parameterNumber: 202),
        zwave.configurationV1.configurationGet(parameterNumber: 203),
        zwave.configurationV1.configurationGet(parameterNumber: 204),
        zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1),
        zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:1),
        zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5, scale:1),
    ]
    log.debug "${request}"
    commands(request)

}

/**
* Configures the device to settings needed by SmarthThings at device discovery time.
* Need a triple click on B-button to zwave commands to pass
*/
def configure() {
    log.debug "Configuring Device For SmartThings Use"
    def cmds = []

    // Associate Group 3 Device Status (Group 1 is for Basic direct action -switches-, Group 2 for Tamper Alerts System -alarm-)
    // Hub need to be Associate to group 3
    cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId])
    cmds += sync_properties()
    commands(cmds)
}

/**
* Triggered when Done button is pushed on Preference Pane
*/
def updated()
{
    log.debug "updated() is being called"
    // Only used to toggle the status if update is needed
    update_needed_settings()
    //if (state.realTemperature) log.debug "Real Temperature: ${state.realTemperature} Adjusted Temperature: ${getAdjustedTemp(state.realTemperature)}"
    if (state.realTemperature) sendEvent(name:"temperature", value: getAdjustedTemp(state.realTemperature))
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
}

/**
* Try to sync properties with the device
*/
def sync_properties()
{
    def currentProperties = state.currentProperties ?: [:]
    def configuration = parseXml(configuration_model())

    def cmds = []
    configuration.Value.each
    {
        if (! currentProperties."${it.@index}" || currentProperties."${it.@index}" == null)
        {
            log.debug "Looking for current value of parameter ${it.@index}"
            cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
        }
    }

    if (device.currentValue("needUpdate") == "YES") { cmds += update_needed_settings() }
    return cmds
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

/**
* Update current cache properties
*/
def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    //log.debug "${cmd.configurationValue}"
    def convertedConfigurationValue = convertParam("${cmd.parameterNumber}".toInteger(), cmd2Integer(cmd.configurationValue))
    //log.debug "${convertedConfigurationValue}"
    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue

    if (settings."${cmd.parameterNumber}" != null)
    {
        if (settings."${cmd.parameterNumber}".toInteger() == convertedConfigurationValue)
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

/**
* Update needed settings
*/
def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    configuration.Value.each
    {
        if (currentProperties."${it.@index}" == null)
        {
            log.debug "Current value of parameter ${it.@index} is unknown"
            isUpdateNeeded = "YES"
        }
        else if (settings."${it.@index}" != null && convertParam(it.@index.toInteger(), cmd2Integer(currentProperties."${it.@index}")) != settings."${it.@index}".toInteger())
        { 
            isUpdateNeeded = "YES"
            
            log.debug "Parameter ${it.@index} will be updated to " + settings."${it.@index}"
            def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger())
            switch(it.@byteSize)
            {
                case "1":
                    cmds << zwave.configurationV1.configurationSet(configurationValue: [convertedConfigurationValue], parameterNumber: it.@index.toInteger(), size: 1)
                break
                case "2":
                    def short valueLow   = convertedConfigurationValue & 0xFF
                    def short valueHigh = (convertedConfigurationValue >> 8) & 0xFF
                    def value = [valueHigh, valueLow]
                    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, 2), parameterNumber: it.@index.toInteger(), size: 2)
                break
                case "4":
                    def short value1 = convertedConfigurationValue & 0xFF
                    def short value2 = (convertedConfigurationValue >> 8) & 0xFF
                    def short value3 = (convertedConfigurationValue >> 16) & 0xFF
                    def short value4 = (convertedConfigurationValue >> 24) & 0xFF
                    def value = [value4, value3, value2, value1]
                	cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, 4), parameterNumber: it.@index.toInteger(), size: 4)
                break
            }
            cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
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
	case 4:
    	def short value1 = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        def short value4 = (value >> 24) & 0xFF
		[value4, value3, value2, value1]
	break
	}
}

private setConfigured() {
	updateDataValue("configured", "true")
}

private isConfigured() {
	getDataValue("configured") == "true"
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=200) {
	delayBetween(commands.collect{ command(it) }, delay)
}

/**
* This function generate the preferences menu from the XML file
* each input will be accessible from settings map object.
*/
def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
    configuration.Value.each
    {
        switch(it.@type)
        {
            case ["byte","short","four"]:
                input "${it.@index}", "number",
                    title:"${it.@index} - ${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"${it.@index} - ${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    options: items
            break
        }
    }
}

/**
* Define the Aeon motion sensor model used to generate preference pane.
*/
def configuration_model()
{
'''
<configuration>
  <Value type="short" byteSize="2" index="3" label="PIR reset time" min="10" max="3600" value="20">
    <Help>
Number of seconds to wait to report motion cleared after a motion event if there is no motion detected.
Range: 10~3600.
Default: 240 (4 minutes)
Note:
(1), The time unit is seconds if the value range is in 10 to 255.
(2), If the value range is in 256 to 3600, the time unit will be minute and its value should follow the below rules:
a), Interval time =Value/60, if the interval time can be divided by 60 and without remainder.
b), Interval time= (Value/60) +1, if the interval time can be divided by 60 and has remainder.
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="4" label="PIR motion sensitivity" min="0" max="5" value="">
    <Help>
A value from 0-5, from disabled to high sensitivity
Range: 0~5
Default: 5
    </Help>
  </Value>
    <Value type="byte" byteSize="4" index="111" label="Reporting Interval" min="5" max="2678400" value="">
    <Help>
The interval time of sending reports in Report group 1
Range: 5~
Default: 3600 seconds
Note:
1. The unit of interval time is second if USB power.
2. If battery power, the minimum interval time is 60 minutes by default, for example, if the value is set to be more than 5 and less than 3600, the interval time is 60 minutes, if the value is set to be more than 3600 and less than 7200, the interval time is 120 minutes. You can also change the minimum interval time to 4 minutes via setting the interval value(3 bytes) to 240 in Wake Up Interval Set CC
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="201" label="Temperature offset" min="-10" max="10" value="">
    <Help>
Range: -100~100
Default: 0
Note: 
1. The value contains one decimal point. E.g. if the value is set to 20, the calibration value is 2.0 F (EU/AU version) or 2.0 ℉(US version)
2. The calibration value = standard value - measure value.
E.g. If measure value =25.3℃ and the standard value = 23.2℃, so the calibration value = 23.2℃ - 25.3℃= -2.1℃.
If the measure value =30.1℃ and the standard value = 33.2℃, so the calibration value = 33.2℃ - 30.1℃=3.1℃. 
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="202" label="Humidity offset" min="-50" max="50" value="">
    <Help>
Range: -50~50
Default: 0
Note:
The calibration value = standard value - measure value.
E.g. If measure value = 80RH and the standard value = 75RH, so the calibration value = 75RH – 80RH= -5RH.
If the measure value = 85RH and the standard value = 90RH, so the calibration value = 90RH – 85RH = 5RH. 
    </Help>
  </Value>
    <Value type="byte" byteSize="2" index="203" label="Luminance offset" min="-1000" max="1000" value="">
    <Help>
Range: -1000~1000
Default: 0
Note:
The calibration value = standard value - measure value.
E.g. If measure value = 800Lux and the standard value = 750Lux, so the calibration value = 750 – 800 = -50.
If the measure value = 850Lux and the standard value = 900Lux, so the calibration value = 900 – 850 = 50.
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="204" label="Ultraviolet offset" min="-10" max="10" value="">
    <Help>
Range: -10~10
Default: 0
Note:
The calibration value = standard value - measure value.
E.g. If measure value = 9 and the standard value = 8, so the calibration value = 8 – 9 = -1.
If the measure value = 7 and the standard value = 9, so the calibration value = 9 – 7 = 2. 
    </Help>
  </Value>
</configuration>
'''
}

private getAdjustedTemp(value) {
    
    value = Math.round((value as Double) * 100) / 100
    //log.debug "Adjusted Temp: ${value}"

	if (tempOffset) {
       //log.debug "Offset: ${Math.round(tempOffset * 100) /100}"
	   return value =  value + Math.round(tempOffset * 100) /100
	} else {
       return value
    }
    
}
