/**
 *
 *  zooZ 4-in-1 Sensor (Advanced)
 *   
 *	github: Eric Maycock (erocm123)
 *	Date: 2016-07-09
 *	Copyright Eric Maycock
 *
 *  Code has elements from other community source @CyrilPeponnet (Z-Wave Parameter Sync). Includes all 
 *  configuration parameters and ease of advanced configuration. Added software based temp, humidity,
 *  and light offsets.
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
	definition (name: "zooZ 4-in-1 Sensor (Advanced)", namespace: "erocm123", author: "Eric Maycock", vid:"generic-motion-7") {
		capability "Motion Sensor"
		capability "Acceleration Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Illuminance Measurement"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
        capability "Refresh"
        capability "Tamper Alert"
        capability "Health Check"
        
        command "resetBatteryRuntime"
		
        attribute   "needUpdate", "string"
        
        fingerprint mfr: "0109", prod: "2021", model: "2101", deviceJoinName: "zooZ 4-in-1 Sensor"
        fingerprint deviceId: "0x0701", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x31,0x70,0x5A,0x98,0x7A"
	}
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model()) 
    }
	simulator {
	}
	tiles (scale: 2) {
		multiAttributeTile(name:"main", type:"generic", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
            	attributeState "temperature",label:'${currentValue}°', icon:"st.motion.motion.inactive", backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
				    [value: 74, color: "#44b621"],
				    [value: 84, color: "#f1d801"],
				    [value: 95, color: "#d04e00"],
				    [value: 96, color: "#bc2323"]
			    ]
            }
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
				attributeState "statusText", label:'${currentValue}'
			}
		}
       standardTile("motion","device.motion", width: 2, height: 2) {
            	state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
                state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
		}
		valueTile("humidity","device.humidity", width: 2, height: 2) {
           	state "humidity",label:'RH ${currentValue}%',unit:"%"
		}
		valueTile(
        	"illuminance","device.illuminance", width: 2, height: 2) {
            	state "luminosity", label:'LUX ${currentValue}%', unit:"%", backgroundColors:[
                	[value: 0, color: "#000000"],
                    [value: 1, color: "#060053"],
                    [value: 12, color: "#3E3900"],
                    [value: 24, color: "#8E8400"],
					[value: 48, color: "#C5C08B"],
					[value: 60, color: "#DAD7B6"],
					[value: 84, color: "#F3F2E9"],
                    [value: 100, color: "#F3F2E9"]
				]
		}
		standardTile("acceleration", "device.acceleration", width: 2, height: 2) {
			state("active", label:'tamper', icon:"st.motion.acceleration.active", backgroundColor:"#f39c12")
			state("inactive", label:'clear', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
		}
        standardTile("tamper", "device.tamper", width: 2, height: 2) {
			state("detected", label:'tamper\nactive', icon:"st.contact.contact.open", backgroundColor:"#e86d13")
			state("clear", label:'tamper\nclear', icon:"st.contact.contact.closed", backgroundColor:"#cccccc")
		}
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        valueTile("currentFirmware", "device.currentFirmware", width: 2, height: 2) {
			state "currentFirmware", label:'Firmware: v${currentValue}', unit:""
		}
        standardTile("refresh", "device.switch", decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }
        valueTile(
			"batteryRuntime", "device.batteryRuntime", decoration: "flat", width: 2, height: 2) {
			state "batteryRuntime", label:'Battery: ${currentValue} Double tap to reset counter', unit:"", action:"resetBatteryRuntime"
		}
        standardTile(
			"statusText2", "device.statusText2", decoration: "flat", width: 2, height: 2) {
			state "statusText2", label:'${currentValue}', unit:"", action:"resetBatteryRuntime"
		}
        
		main([
        	"main", "motion"
            ])
		details([
        	"main",
            "humidity","illuminance", "battery",
            "motion","tamper", "refresh",
             "statusText2", "configure", 
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
            result << response(zwave.wakeUpV1.wakeUpIntervalSet(seconds: 43200, nodeid:zwaveHubNodeId).format())
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
    
    //log.debug "${description} parsed to ${result}"
    
    updateStatus()

	if ( result[0] != null ) { result }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x30: 2, 0x84: 1])
	state.sec = 1
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

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    update_current_properties(cmd)
    log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd)
{
	log.debug "WakeUpIntervalReport ${cmd.toString()}"
    state.wakeInterval = cmd.seconds
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    log.debug cmd
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastBatteryReport = now()
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
            state.realLuminance = cmd.scaledSensorValue.toInteger()
			map.value = getAdjustedLuminance(cmd.scaledSensorValue.toInteger())
			map.unit = "lux"
			break;
        case 5:
			map.name = "humidity"
            state.realHumidity = cmd.scaledSensorValue.toInteger()
			map.value = getAdjustedHumidity(cmd.scaledSensorValue.toInteger())
			map.unit = "%"
			break;
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}

def motionEvent(value) {
	def map = [name: "motion"]
	if (value != 0) {
		map.value = "active"
		map.descriptionText = "$device.displayName detected motion"
	} else {
		map.value = "inactive"
		map.descriptionText = "$device.displayName motion has stopped"
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
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
				result << createEvent(name: "tamper", value: "clear", descriptionText: "$device.displayName tamper cleared")
                result << createEvent(name: "acceleration", value: "inactive", descriptionText: "$device.displayName tamper cleared")
				break
			case 3:
				result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName was moved")
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

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
    log.debug "Device ${device.displayName} woke up"

    def request = update_needed_settings()
    
    if (!state.lastBatteryReport || (now() - state.lastBatteryReport) / 60000 >= 60 * 24)
    {
        log.debug "Over 24hr since last battery report. Requesting report"
        request << zwave.batteryV1.batteryGet()
    }

    if(request != []){
       response(commands(request) + ["delay 5000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()])
    } else {
       log.debug "No commands to send"
       response([zwave.wakeUpV1.wakeUpNoMoreInformation().format()])
    }
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	log.debug "AssociationReport $cmd"
    if (zwaveHubNodeId in cmd.nodeId) state."association${cmd.groupingIdentifier}" = true
    else state."association${cmd.groupingIdentifier}" = false
}

def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd){
    log.debug "Firmware Report ${cmd.toString()}"
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.debug cmd
    if(cmd.applicationVersion && cmd.applicationSubVersion) {
	    def firmware = "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2,'0')}"
        state.needfwUpdate = "false"
        updateDataValue("firmware", firmware)
        createEvent(name: "currentFirmware", value: firmware)
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug "Unknown Z-Wave Command: ${cmd.toString()}"
}

def refresh() {
   	log.debug "$device.displayName - refresh()"

    def request = []
    if (state.lastRefresh != null && now() - state.lastRefresh < 5000) {
        log.debug "Refresh Double Press"
        def configuration = parseXml(configuration_model())
        configuration.Value.each
        {
            if ( "${it.@setting_type}" == "zwave" ) {
                request << zwave.configurationV1.configurationGet(parameterNumber: "${it.@index}".toInteger())
            }
        } 
        request << zwave.wakeUpV1.wakeUpIntervalGet()
    }
    state.lastRefresh = now()
    request << zwave.batteryV1.batteryGet()
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:1)
    request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5, scale:1)
    commands(request)
}

def ping() {
   	log.debug "$device.displayName - ping()"
    return command(zwave.batteryV1.batteryGet())
}

def configure() {
    log.debug "Configuring Device For SmartThings Use"
    def cmds = []

    cmds += update_needed_settings()
    commands(cmds)
}

def updated()
{
    log.debug "updated() is being called"
    sendEvent(name: "checkInterval", value: 2 * 12 * 60 * 60 + 5 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    if (state.realTemperature != null) sendEvent(name:"temperature", value: getAdjustedTemp(state.realTemperature))
    if (state.realHumidity != null) sendEvent(name:"humidity", value: getAdjustedHumidity(state.realHumidity))
    if (state.realLuminance != null) sendEvent(name:"illuminance", value: getAdjustedLuminance(state.realLuminance))
    
    updateStatus()
    
    state.needfwUpdate = ""
    
    def cmds = update_needed_settings()
    
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    
    response(commands(cmds))
}

def update_needed_settings()
{   
    def currentProperties = state.currentProperties ?: [:]
    def configuration = parseXml(configuration_model())
    
    def isUpdateNeeded = "NO"

    def cmds = []
    
    if(!state.needfwUpdate || state.needfwUpdate == "") {
       log.debug "Requesting device firmware version"
       cmds << zwave.versionV1.versionGet()
    }
    
    if(!state.association1){
       log.debug "Setting association group 1"
       cmds << zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
       cmds << zwave.associationV2.associationGet(groupingIdentifier:1)
    }
    
    if(state.wakeInterval == null || state.wakeInterval != 43200){
        log.debug "Setting Wake Interval to 43200"
        cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds: 43200, nodeid:zwaveHubNodeId)
        cmds << zwave.wakeUpV1.wakeUpIntervalGet()
    }
    
    if (device.currentValue("temperature") == null) {
        log.debug "Temperature report not yet received. Sending request"
        cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
    }
    if (device.currentValue("humidity") == null) {
        log.debug "Humidity report not yet received. Sending request"
        cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5, scale:1)
    }
    if (device.currentValue("illuminance") == null) {
        log.debug "Illuminance report not yet received. Sending request"
        cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3, scale:1)
    }
    
    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave"){
            if (currentProperties."${it.@index}" == null)
            {
                log.debug "Current value of parameter ${it.@index} is unknown"
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                isUpdateNeeded = "YES"
            }
            else if (settings."${it.@index}" != null && convertParam(it.@index.toInteger(), cmd2Integer(currentProperties."${it.@index}")) != settings."${it.@index}".toInteger())
            { 
                isUpdateNeeded = "YES"

                log.debug "Parameter ${it.@index} will be updated to " + settings."${it.@index}"
                def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger())
                cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
            }
        }
    }
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
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

def update_current_properties(cmd)
{

    def currentProperties = state.currentProperties ?: [:]
    def convertedConfigurationValue = convertParam("${cmd.parameterNumber}".toInteger(), cmd2Integer(cmd.configurationValue))
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
    
	if (state.sec && cmd.toString()) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=1000) {
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
                    defaultValue: "${it.@value}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    options: items
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"${it.@label}\n" + "${it.Help}",
                    //range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title:"${it.@label}\n" + "${it.Help}",
                    //range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}"
            break
        }
    }
}

private getBatteryRuntime() {
   def currentmillis = now() - state.batteryRuntimeStart
   def days=0
   def hours=0
   def mins=0
   def secs=0
   secs = (currentmillis/1000).toInteger() 
   mins=(secs/60).toInteger() 
   hours=(mins/60).toInteger() 
   days=(hours/24).toInteger() 
   secs=(secs-(mins*60)).toString().padLeft(2, '0') 
   mins=(mins-(hours*60)).toString().padLeft(2, '0') 
   hours=(hours-(days*24)).toString().padLeft(2, '0') 
 

  if (days>0) { 
      return "$days days and $hours:$mins:$secs"
  } else {
      return "$hours:$mins:$secs"
  }
}

private getRoundedInterval(number) {
    double tempDouble = (number / 60)
    if (tempDouble == tempDouble.round())
       return (tempDouble * 60).toInteger()
    else 
       return ((tempDouble.round() + 1) * 60).toInteger()
}

private getAdjustedTemp(value) {
    
    value = Math.round((value as Double) * 100) / 100

	if (settings."302") {
	   return value =  value + Math.round(settings."302" * 100) /100
	} else {
       return value
    }
    
}

private getAdjustedHumidity(value) {
    
    value = Math.round((value as Double) * 100) / 100

	if (settings."303") {
	   return value =  value + Math.round(settings."303" * 100) /100
	} else {
       return value
    }
    
}

private getAdjustedLuminance(value) {
    
    value = Math.round((value as Double) * 100) / 100

	if (settings."304") {
	   return value =  value + Math.round(settings."304" * 100) /100
	} else {
       return value
    }
    
}

def resetBatteryRuntime() {
    if (state.lastReset != null && now() - state.lastReset < 5000) {
        log.debug "Reset Double Press"
        state.batteryRuntimeStart = now()
        updateStatus()
    }
    state.lastReset = now()
}

private updateStatus(){
   def result = []
   if(state.batteryRuntimeStart != null){
        sendEvent(name:"batteryRuntime", value:getBatteryRuntime(), displayed:false)
        if (device.currentValue('currentFirmware') != null){
            sendEvent(name:"statusText2", value: "Firmware: v${device.currentValue('currentFirmware')} - Battery: ${getBatteryRuntime()} Double tap to reset", displayed:false)
        } else {
            sendEvent(name:"statusText2", value: "Battery: ${getBatteryRuntime()} Double tap to reset", displayed:false)
        }
    } else {
        state.batteryRuntimeStart = now()
    }

    String statusText = ""
    if(device.currentValue('humidity') != null)
        statusText = "RH ${device.currentValue('humidity')}% - "
    if(device.currentValue('illuminance') != null)
        statusText = statusText + "LUX ${device.currentValue('illuminance')} - "
        
    if (statusText != ""){
        statusText = statusText.substring(0, statusText.length() - 2)
        sendEvent(name:"statusText", value: statusText, displayed:false)
    }
}

def configuration_model()
{
'''
<configuration>
  <Value type="list" index="1" label="Temperature Scale (C or F)" min="0" max="1" value="1" byteSize="1" setting_type="zwave">
    <Help>

    </Help>
        <Item label="F" value="1" />
        <Item label="C" value="0" />
  </Value>
  <Value type="list" index="7" label="Led Behavior" min="1" max="4" value="4" byteSize="1" setting_type="zwave">
    <Help>
Default: Off for Temp, On with Motion
    </Help>
        <Item label="LED Off" value="1" />
        <Item label="Breathing" value="2" />
        <Item label="Quick Blink on Temp/PIR" value="3" />
        <Item label="Off for Temp, On with Motion" value="4" />
  </Value>
  <Value type="short" byteSize="1" index="5" label="PIR reset time" min="1" max="255" setting_type="zwave">
    <Help>
Number of MINUTES to wait to report motion cleared after a motion event if there is no motion detected.
Range: 1~255.
Default: 3 Minutes
Firmware 17.09+: Number of SECONDS to wait to report motion cleared after a motion event if there is no motion detected.
Range: 15~60.
Default: 15 Seconds
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="6" label="PIR motion sensitivity" min="1" max="7" value="3" setting_type="zwave">
    <Help>
A value from 1-7, from low to high sensitivity
Range: 1~7
Default: 3
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="2" label="Temperature Change Report" min="1" max="50" value="10" setting_type="zwave">
    <Help>
Range: 1~50
Default: 10
Note: 
The amount by which the temperature must change in order for the sensor to send a report. Values are in .10 (tenths) so 1 = .1, 5 = .5, etc.
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="3" label="Humidity Change Report" min="1" max="50" value="10" setting_type="zwave">
    <Help>
Range: 1~50
Default: 10
Note:
The amount by which the humidity must change in order for the sensor to send a report. Value is in %.
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="4" label="Luminance Change Report" min="0" max="50" value="10" setting_type="zwave">
    <Help>
Range: 0,5~50
Default: 10
Note:
The amount by which the luminance must change in order for the sensor to send a report. Value is in %.
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="302" label="Temperature offset" min="*" max="*" value="">
    <Help>
Range: None
Default: 0
Note: 
1. The calibration value = standard value - measure value.
E.g. If measure value = 85.3F and the standard value = 83.2F, so the calibration value = 83.2F - 85.3F = -2.1F.
If the measure value = 60.1F and the standard value = 63.2F, so the calibration value = 63.2F - 60.1℃ = 3.1F. 
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="303" label="Humidity offset" min="*" max="*" value="">
    <Help>
Range: None
Default: 0
Note:
The calibration value = standard value - measure value.
E.g. If measure value = 80RH and the standard value = 75RH, so the calibration value = 75RH – 80RH = -5RH.
If the measure value = 85RH and the standard value = 90RH, so the calibration value = 90RH – 85RH = 5RH. 
    </Help>
  </Value>
    <Value type="byte" byteSize="2" index="304" label="Luminance offset" min="*" max="*" value="">
    <Help>
Range: None
Default: 0
Note:
The calibration value = standard value - measure value.
E.g. If measure value = 80% Lux and the standard value = 75% Lux, so the calibration value = 75 – 80 = -5.
If the measure value = 85% Lux and the standard value = 90% Lux, so the calibration value = 90 – 85 = 5.
    </Help>
  </Value>
</configuration>
'''
}
