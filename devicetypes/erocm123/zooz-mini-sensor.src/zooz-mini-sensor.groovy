/**
 *
 *  zooZ Mini Sensor
 *   
 *	github: Eric Maycock (erocm123)
 *	Date: 2016-10-05
 *	Copyright Eric Maycock
 *
 *  Code has elements from other community source @CyrilPeponnet (Z-Wave Parameter Sync). Includes all 
 *  configuration parameters and ease of advanced configuration. Added software based light offsets.
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
	definition (name: "zooZ Mini Sensor", namespace: "erocm123", author: "Eric Maycock", vid:"generic-motion-4") {
		capability "Motion Sensor"
		capability "Illuminance Measurement"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
        capability "Acceleration Sensor"
        capability "Tamper Alert"
        capability "Health Check"
        
        attribute   "needUpdate", "string"
        
        fingerprint mfr: "027A", prod: "0003", model: "0083"
        fingerprint deviceId: "0x0701", inClusters: "0x5E,0x86,0x72,0x5A,0x73,0x80,0x31,0x71,0x30,0x70,0x85,0x59,0x84"
        
	}
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())  
    }
    
	simulator {
	}
    
	tiles (scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
			}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
				attributeState "statusText", label:'${currentValue}'
			}
		}
		valueTile(
        	"illuminance","device.illuminance", width: 2, height: 2) {
            	state "luminosity",label:'LUX ${currentValue}', unit:"lux", backgroundColors:[
                	[value: 0, color: "#000000"],
                    [value: 1, color: "#060053"],
                    [value: 12, color: "#3E3900"],
                    [value: 24, color: "#8E8400"],
					[value: 48, color: "#C5C08B"],
					[value: 60, color: "#DAD7B6"],
					[value: 84, color: "#F3F2E9"],
                    [value: 100, color: "#FFFFFF"]
				]
		}
		valueTile("battery", "device.battery", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }
        
		main([
        	"motion"
            ])
		details([
            "motion",
            "illuminance", "battery", "configure", 
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
        	logging("Update is hit when the device is paired.")
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
    logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'")
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd)
{
	logging("WakeUpIntervalReport ${cmd.toString()}")
    state.wakeInterval = cmd.seconds
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    logging("Battery Report: $cmd")
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel <= 100? cmd.batteryLevel : 100
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
    logging("SensorBinaryReport: $cmd")
	motionEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    logging("BasicSet: $cmd")
	motionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    logging("NotificationReport: $cmd")
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
    logging("Device ${device.displayName} woke up")

    def request = sync_properties()
    
    if (!state.lastBatteryReport || (now() - state.lastBatteryReport) / 60000 >= 60 * 24)
    {
        logging("Over 24hr since last battery report. Requesting report")
        request << zwave.batteryV1.batteryGet()
    }

    if(request != []){
       response(commands(request) + ["delay 5000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()])
    } else {
       logging("No commands to send")
       response([zwave.wakeUpV1.wakeUpNoMoreInformation().format()])
    }
}

def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd){
    logging("Firmware Report ${cmd.toString()}")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    logging("Unknown Z-Wave Command: ${cmd.toString()}")
}

def configure() {
    logging("Configuring Device For SmartThings Use")
    def cmds = []
    cmds = update_needed_settings()
    if (cmds != []) commands(cmds)
}

def updated()
{
    logging("updated() is being called")
    sendEvent(name: "checkInterval", value: 2 * 12 * 60 * 60 + 5 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    if (state.realLuminance != null) sendEvent(name:"illuminance", value: getAdjustedLuminance(state.realLuminance))
    updateStatus()
    def cmds = []
    cmds = update_needed_settings()
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    if (cmds != []) response(commands(cmds))
}

def sync_properties()
{   
    def currentProperties = state.currentProperties ?: [:]
    def configuration = parseXml(configuration_model())

    def cmds = []
    
    if(state.wakeInterval == null || state.wakeInterval != getRoundedInterval(settings.wake)){
        logging("Setting Wake Interval to ${getRoundedInterval(settings.wake)}")
        cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds: getRoundedInterval(settings.wake), nodeid:zwaveHubNodeId)
        cmds << zwave.wakeUpV1.wakeUpIntervalGet()
    }
    
    configuration.Value.each
    {
        if ( "${it.@setting_type}" == "zwave" ) {
            if (! currentProperties."${it.@index}" || currentProperties."${it.@index}" == null)
            { 
                logging("Looking for current value of parameter ${it.@index}")
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
            }
        }
    }
    
    if (device.currentValue("needUpdate") == "YES") { cmds += update_needed_settings() }
    return cmds
}

def convertParam(number, value) {
	switch (number){
    	case 201:
            value
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



def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
     
    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
   
    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave"){
            if (currentProperties."${it.@index}" == null)
            {
                logging("Current value of parameter ${it.@index} is unknown")
                isUpdateNeeded = "YES"
            }
            else if (settings."${it.@index}" != null && convertParam(it.@index.toInteger(), cmd2Integer(currentProperties."${it.@index}")) != settings."${it.@index}".toInteger())
            { 
                isUpdateNeeded = "YES"

                logging("Parameter ${it.@index} will be updated to " + settings."${it.@index}")
                def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger())
                cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
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

def ping() {
    logging("ping()")
	logging("Battery Device - Not sending ping commands")
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

private getAdjustedLuminance(value) {
    
    value = Math.round((value as Double) * 100) / 100

	if (settings."304") {
	   return value =  value + Math.round(settings."304" * 100) /100
	} else {
       return value
    }
    
}

private updateStatus(){
    def result = []

    String statusText = ""
    if(device.currentValue('illuminance') != null)
        statusText = statusText + "LUX ${device.currentValue('illuminance')} - "
        
    if (statusText != ""){
        statusText = statusText.substring(0, statusText.length() - 2)
        sendEvent(name:"statusText", value: statusText, displayed:false)
    }
}

private def logging(message) {
    if (state.enableDebugging == null || state.enableDebugging == "true") log.debug "$message"
}

def configuration_model()
{
'''
<configuration>
  <Value type="byte" index="1" label="Motion Sensitivity" min="8" max="255" value="12" byteSize="1" setting_type="zwave">
    <Help>
Motion detection sensitivity
Range: 8~255
Default: 12
    </Help>
  </Value>
  <Value type="byte" index="2" label="Trigger On Duration" min="5" max="600" value="30" byteSize="2" setting_type="zwave">
    <Help>
Number of seconds the associated device to stay ON for after being triggered by the sensor before it automatically turns OFF
Range: 5~600
Default: 30
    </Help>
  </Value>
    <Value type="byte" index="3" label="Trigger Action" min="1" max="255" value="255" byteSize="1" setting_type="zwave">
    <Help>
Associated device will turn ON when triggered by the sensor (255) or Brightness level (percentage) the associated device will turn ON to when triggered by the sensor (1-99).
Range: 1~99,255
Default: 255
    </Help>
  </Value>
  <Value type="list" byteSize="1" index="4" label="Motion detection" min="0" max="255" value="255" setting_type="zwave">
    <Help>
Enable or disable motion detection.
Default: Enabled
   </Help>
        <Item label="Disable" value="0" />
        <Item label="Enable" value="255" />
  </Value>
    <Value type="byte" byteSize="2" index="5" label="Light Trigger Level" min="0" max="1000" value="100" setting_type="zwave">
    <Help>
Light level change (in LUX) to set off light trigger
Range: 0~1000
Default: 100
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="6" label="Motion Trigger Interval" min="1" max="8" value="8" setting_type="zwave">
    <Help>
Number of seconds for motion trigger interval
Range: 1~8
Default: 8
    </Help>
  </Value>
  <Value type="byte" byteSize="2" index="7" label="Light Polling Interval" min="60" max="360000" value="180" setting_type="zwave">
    <Help>
Light polling interval in seconds
Range: 60~360000
Default: 180
    </Help>
  </Value>
    <Value type="list" byteSize="1" index="8" label="Light Trigger" min="0" max="1" value="0" setting_type="zwave">
    <Help>
Enable or disable the light trigger
Default: Disabled
</Help>
        <Item label="Disable" value="0" />
        <Item label="Enable" value="1" />
  </Value>
    <Value type="byte" byteSize="1" index="9" label="Light Report" min="0" max="255" value="20" setting_type="zwave">
    <Help>
Light level change (in LUX) to be reported by the sensor to the controller
Range: 0~255
Default: 20
    </Help>
  </Value>
      <Value type="list" byteSize="1" index="10" label="LED Notifications" min="0" max="1" value="1" setting_type="zwave">
    <Help>
Enable or disable LED notifications
Default: Enabled
</Help>
        <Item label="Disable" value="0" />
        <Item label="Enable" value="1" />
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
      <Value type="byte" byteSize="2" index="wake" label="Wake Interval" min="240" max="65536" value="43200" setting_type="wake">
    <Help>
Set the wake interval for the device in seconds. Decreasing this value will reduce battery life. 
Range: 240~65536
Default: 43200 (12 Hours)
    </Help>
  </Value>
  <Value type="boolean" index="enableDebugging" label="Enable Debug Logging?" value="true" setting_type="preference" fw="">
    <Help>
    </Help>
  </Value>
</configuration>
'''
}
