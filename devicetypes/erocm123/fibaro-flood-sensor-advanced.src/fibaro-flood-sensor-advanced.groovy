/**
 *  Fibaro Flood Sensor ZW5
 *
 *  Copyright 2016 Fibar Group S.A.
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
	definition (name: "Fibaro Flood Sensor (Advanced)", namespace: "erocm123", author: "Fibar Group S.A.", vid:"generic-leak") {
		capability "Battery"
		capability "Configuration"
		capability "Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Water Sensor"
        capability "Health Check"
        
        attribute "needUpdate", "string"
        
        fingerprint mfr: "010F", prod: "0B01", model: "2002", deviceJoinName: "Fibaro Flood Sensor"
        
        // Wall Powered
        fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x22, 0x85, 0x59, 0x20, 0x70, 0x56, 0x5A, 0x7A, 0x72, 0x8E, 0x71, 0x73, 0x98, 0x9C, 0x31, 0x86"
        // Battery Powered
        fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x22, 0x85, 0x59, 0x20, 0x80, 0x70, 0x56, 0x5A, 0x7A, 0x72, 0x8E, 0x71, 0x73, 0x98, 0x9C, 0x31, 0x86", outClusters: ""
	}
    
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())
    }

	simulator {
    
	}
    
    tiles(scale: 2) {
    	multiAttributeTile(name:"FGFS", type:"lighting", width:6, height:4) {//with generic type secondary control text is not displayed in Android app
        	tileAttribute("device.water", key:"PRIMARY_CONTROL") {
            	attributeState("dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff")
            	attributeState("wet", icon:"st.alarm.water.wet", backgroundColor:"#00a0dc")
            }
            
            tileAttribute("device.tamper", key:"SECONDARY_CONTROL") {
				attributeState("active", label:'tamper active', backgroundColor:"#e86d13")
				attributeState("inactive", label:'tamper inactive', backgroundColor:"#ffffff")
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
        
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }
       	        
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "battery", label:'${currentValue}% battery', unit:""
        }
        
        main "FGFS"
        details(["FGFS","battery", "temperature", "configure"])
    }
}

// parse events into attributes
def parse(String description) {
	//logging("Parsing '${description}'")
	def result = []
    
    if (description.startsWith("Err 106")) {
		if (state.sec) {
			result = createEvent(descriptionText:description, displayed:false)
		} else {
            state.sec = 0
			result = createEvent(
				descriptionText: "FGFS failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
			)
		}
	} else if (description == "updated") {
		return null
	} else {
        def cmd = zwave.parse(description, [0x31: 5, 0x56: 1, 0x71: 3, 0x72:2, 0x80: 1, 0x84: 2, 0x85: 2, 0x86: 1, 0x98: 1])

        if (cmd) {
            //logging("Parsed '${cmd}'")
            zwaveEvent(cmd)
        }
    }
}

//security
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x71: 3, 0x84: 2, 0x85: 2, 0x86: 1, 0x98: 1])
	if (encapsulatedCommand) {
        state.sec = 1
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

//crc16
def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd)
{
    def versions = [0x31: 5, 0x30: 1, 0x9C: 1, 0x70: 2, 0x85: 2]
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		logging("Could not extract command from $cmd")
	} else {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd)
{
	logging("WakeUpIntervalReport ${cmd.toString()}")
    state.wakeInterval = cmd.seconds
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
    logging("Device ${device.displayName} woke up")
    
    def request = update_needed_settings()
    
    if (!state.lastBatteryReport || (now() - state.lastBatteryReport) / 60000 >= 60 * 24)
    {
        logging("Over 24hr since last battery report. Requesting report")
        request << zwave.batteryV1.batteryGet()
    }
    
    if (!state.lastTempReport || (now() - state.lastTempReport) / 60000 >= 60 * 3)
    {
        logging("Over 3hr since last temperature report. Requesting report")
        request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)
    }

    if(request != []){
       response(encapSequence(request, 500) + ["delay 5000", encap(zwave.wakeUpV1.wakeUpNoMoreInformation())])
    } else {
       logging("No commands to send")
       response(encap(zwave.wakeUpV1.wakeUpNoMoreInformation()))
    }
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    state.MSR = 1
	logging("manufacturerId:   ${cmd.manufacturerId}")
    logging("manufacturerName: ${cmd.manufacturerName}")
    logging("productId:        ${cmd.productId}")
    logging("productTypeId:    ${cmd.productTypeId}")
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) { 
	logging("deviceIdData:                ${cmd.deviceIdData}")
    logging("deviceIdDataFormat:          ${cmd.deviceIdDataFormat}")
    logging("deviceIdDataLengthIndicator: ${cmd.deviceIdDataLengthIndicator}")
    logging("deviceIdType:                ${cmd.deviceIdType}")
    
    if (cmd.deviceIdType == 1 && cmd.deviceIdDataFormat == 1) {//serial number in binary format
		String serialNumber = "h'"
        
        cmd.deviceIdData.each{ data ->
        	serialNumber += "${String.format("%02X", data)}"
        }
        
        updateDataValue("serialNumber", serialNumber)
        logging("${device.displayName} - serial number: ${serialNumber}")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
    updateDataValue("version", "${cmd.applicationVersion}.${cmd.applicationSubVersion}")
    logging("applicationVersion:      ${cmd.applicationVersion}")
    logging("applicationSubVersion:   ${cmd.applicationSubVersion}")
    logging("zWaveLibraryType:        ${cmd.zWaveLibraryType}")
    logging("zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}")
    logging("zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}")
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel == 255 ? 1 : cmd.batteryLevel.toString()
	map.unit = "%"
	map.displayed = true
    state.lastBatteryReport = now()
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def map = [:]
    if (cmd.notificationType == 5) {
    	switch (cmd.event) {                
        	case 2:
            	map.name = "water"
                map.value = "wet"
                map.descriptionText = "${device.displayName} is ${map.value}"
            	break
                
            case 0:
            	map.name = "water"
                map.value = "dry"
                map.descriptionText = "${device.displayName} is ${map.value}"
            	break
        }
    } else if (cmd.notificationType == 7) {
    	switch (cmd.event) {
        	case 0:
            	map.name = "tamper"
                map.value = "inactive"
                map.descriptionText = "${device.displayName}: tamper alarm has been deactivated"
				break
                
        	case 3:
            	map.name = "tamper"
                map.value = "active"
                map.descriptionText = "${device.displayName}: tamper alarm activated"
            	break
        }
    }
    
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	if (cmd.sensorType == 1) {
        // temperature
        def cmdScale = cmd.scale == 1 ? "F" : "C"
        map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
        map.unit = getTemperatureScale()
        map.name = "temperature"
        map.displayed = true
	}
	state.lastTempReport = now()
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
     update_current_properties(cmd)
     logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'")
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
	log.info "${device.displayName}: received command: $cmd - device has reset itself"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    logging("Unhandled event $cmd")
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

/**
* Triggered when Done button is pushed on Preference Pane
*/
def updated()
{
    logging("updated() is being called")
    sendEvent(name: "checkInterval", value: 2 * 12 * 60 * 60 + 5 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID]) 
    def cmds = update_needed_settings()
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    if (cmds != []) response(encapSequence(cmds, 500))
}

def ping() {
    logging("ping()")
	logging("Battery Device - Not sending ping commands")
}

def configure() {
	state.enableDebugging = settings.enableDebugging
    logging("Configuring Device For SmartThings Use")
    def cmds = []
    cmds = update_needed_settings()
    cmds += zwave.batteryV1.batteryGet()
    cmds += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)
    cmds += zwave.wakeUpV2.wakeUpNoMoreInformation()
    if (cmds != []) encapSequence(cmds, 500)
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crc16(physicalgraph.zwave.Command cmd) {
	//zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
    "5601${cmd.format()}0000"
}

private encapSequence(commands, delay=500) {
	delayBetween(commands.collect{ encap(it) }, delay)
}

private encap(physicalgraph.zwave.Command cmd) {
	def secureClasses = [0x20, 0x5A, 0x71, 0x85, 0x8E, 0x9C, 0x70]

	//todo: check if secure inclusion was successful
    //if not do not send security-encapsulated command
	if (state.sec != 0) {
        logging("Sending Secure $cmd")
    	secure(cmd)
    } else {
        logging("Sending crc16 $cmd")
    	crc16(cmd)
    }
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
                    title: it.@label != "" ? "${it.@label}\n" + "${it.Help}" : "" + "${it.Help}",
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
        if (convertParam(cmd.parameterNumber, settings."${cmd.parameterNumber}") == cmd2Integer(cmd.configurationValue))
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
    
    if(state.wakeInterval == null || state.wakeInterval != 21600){
        logging("Setting Wake Interval to 21600")
        cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds: 21600, nodeid:zwaveHubNodeId)
        cmds << zwave.wakeUpV1.wakeUpIntervalGet()
    }
    
    if(state.MSR == null){
        logging("Getting Manufacturer Specific Info")
        cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
    }

    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave"){
            if (currentProperties."${it.@index}" == null)
            {
                isUpdateNeeded = "YES"
                logging("Current value of parameter ${it.@index} is unknown")
                cmds << zwave.configurationV2.configurationGet(parameterNumber: it.@index.toInteger())
            }
            else if (settings."${it.@index}" != null && cmd2Integer(currentProperties."${it.@index}") != convertParam(it.@index.toInteger(), settings."${it.@index}"))
            { 
                isUpdateNeeded = "YES"

                logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"))
                def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}")
                cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue.toInteger(), it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
            } 
        }
    }
    
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

def convertParam(number, value) {
    return value.toInteger()
}

private def logging(message) {
    if (state.enableDebugging == null || state.enableDebugging == "true") log.debug "$message"
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

def configuration_model()
{
'''
<configuration>
<Value type="byte" byteSize="2" index="1" label="Alarm cancellation delay" min="0" max="3600" value="0" setting_type="zwave" fw="">
 <Help>
in seconds
Range: 0 to 3600
Default: 0 (No Delay)
</Help>
</Value>
<Value type="byte" byteSize="4" index="10" label="Temperature measurement interval" min="1" max="65535" value="300" setting_type="zwave" fw="">
 <Help>
In seconds
Range: 1 to 65535
Default: 300
</Help>
</Value>
<Value type="byte" byteSize="2" index="12" label="Temperature measurement hysteresis" min="1" max="1000" value="50" setting_type="zwave" fw="">
 <Help>
Each .01
Determines a minimum temperature change value (insensitivity level), resulting in a temperature report being sent to the main controller.
Range: 1 to 1000
Default: 50
</Help>
</Value>
<Value type="list" byteSize="1" index="2" label="Acoustic and visual signals On / Off in case of flooding" min="0" max="3" value="3" setting_type="zwave" fw="">
 <Help>
Range: 0 to 3
Default: 3 (Acoustic and visual alarms active)
</Help>
        <Item label="Acoustic and visual alarms inactive" value="0" />
        <Item label="Acoustic alarm inactive, visual alarm active" value="1" />
        <Item label="Acoustic alarm active, visual alarm inactive" value="2" />
        <Item label="Acoustic and visual alarms active" value="3" />
</Value>
<Value type="byte" byteSize="2" index="50" label="Low temperature alarm threshold" min="-1000" max="10000" value="1500" setting_type="zwave" fw="">
 <Help>
each 0.01
Range: -1000 to 10000
Default: 1500
</Help>
</Value>
<Value type="byte" byteSize="2" index="51" label="High temperature alarm threshold" min="-10000" max="10000" value="3500" setting_type="zwave" fw="">
 <Help>
each 0.01
Range: -10000 to 10000
Default: 3500
</Help>
</Value>
<Value type="list" byteSize="4" index="61" label="Low temperature alarm indicator color" min="0" max="16777215" value="255" setting_type="zwave" fw="">
 <Help>
Range: 0 to 16777215
Default: Blue
</Help>
        <Item label="Red" value="16711680" />
        <Item label="Green" value="65280" />
        <Item label="Blue" value="255" />
        <Item label="Yellow" value="16776960" />
        <Item label="Turquoise" value="65535" />
        <Item label="Orange" value="16750848" />
        <Item label="White" value="16777215" />
        <Item label="Off" value="0" />
</Value>
<Value type="list" byteSize="4" index="62" label="High temperature alarm indicator color" min="0" max="16777215" value="16711680" setting_type="zwave" fw="">
 <Help>
Range: 0 to 16777215
Default: Red
</Help>
        <Item label="Red" value="16711680" />
        <Item label="Green" value="65280" />
        <Item label="Blue" value="255" />
        <Item label="Yellow" value="16776960" />
        <Item label="Turquoise" value="65535" />
        <Item label="Orange" value="16750848" />
        <Item label="White" value="16777215" />
        <Item label="Off" value="0" />
</Value>
<Value type="list" byteSize="1" index="63" label="Temperature indication using LED visual indicator" min="0" max="2" value="2" setting_type="zwave" fw="">
 <Help>
Visual indicator indicates the temperature (blink) every Temperature Measurement Interval
Range: 0 to 2
Default:
</Help>
        <Item label="Off" value="0" />
        <Item label="Blink (every Temperature Measurement Interval)" value="1" />
        <Item label="Continuously (only in constant power mode)" value="2" />
</Value>
<Value type="byte" byteSize="2" index="73" label="Temperature measurement compensation" min="-10000" max="10000" value="0" setting_type="zwave" fw="">
 <Help>
Parameter stores a temperature value to be added to or deducted from the current temperature measured by internal temperature sensor in order to compensate the difference between air temperature and temperature at the floor level.
Range: -10000 to 10000(Default)
Default: 0
</Help>
</Value>
<Value type="byte" byteSize="4" index="75" label="Visual and audible alarms duration" min="0" max="65535" value="" setting_type="zwave" fw="">
 <Help>
The user can silence the Flood Sensor. Because the Sensor’s alarm may last for a long time, it’s possible to turn off visual and audible alarm signaling to save battery.
Range: 0 to 65535
Default: 0
</Help>
</Value>
<Value type="list" byteSize="1" index="77" label="Flood sensor functionality" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: On
</Help>
        <Item label="On" value="0" />
        <Item label="Off" value="1" />
</Value>
<Value type="boolean" index="enableDebugging" label="Enable Debug Logging?" value="true" setting_type="preference" fw="3.08">
    <Help>

    </Help>
  </Value>
</configuration>
'''
}
