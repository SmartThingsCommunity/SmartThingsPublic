/**
 *
 *  zooZ Mini Plug
 *   
 *	github: Eric Maycock (erocm123)
 *	email: erocmail@gmail.com
 *	Date: 2016-10-05
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
	definition (name: "zooZ Mini Plug", namespace: "erocm123", author: "Eric Maycock") {
		capability "Energy Meter"
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"

		command "reset"
        
        attribute   "needUpdate", "string"

        fingerprint deviceId: "0x1001", inClusters: "0x5E,0x72,0x86,0x85,0x59,0x5A,0x73,0x70,0x25,0x27,0x71,0x32,0x20"
                                                           
	}
    
    preferences {
        
        input description: "Once you change values on this page, the \"Synced\" Status will become \"Pending\" status. When the parameters have been succesfully changed, the status will change back to \"Synced\"", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        
		generate_preferences(configuration_model())
        
    }

	simulator {
	}

	tiles(scale: 2){
        multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
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
        valueTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state("NO" , label:'Synced', action:"configuration.configure", backgroundColor:"#8acb47")
            state("YES", label:'Pending', action:"configuration.configure", backgroundColor:"#f39c12")
        }

		main "switch"
		details (["switch", "power", "energy", "refresh", "configure", "reset"])
	}
}

def updated()
{
    state.enableDebugging = settings.enableDebugging
    logging("updated() is being called")
    
    def cmds = update_needed_settings()
    
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    
    if (cmds != []) response(commands(cmds))
}

def parse(String description) {
	def result = null
	if(description == "updated") return 
	def cmd = zwave.parse(description, [0x20: 1, 0x32: 1, 0x72: 2])
	if (cmd) {
		result = zwaveEvent(cmd)
	}
    
    updateStatus()
    
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
	if (cmd.scale == 0) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
	} else if (cmd.scale == 1) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
	} else if (cmd.scale == 2) {
		createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	def evt = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
	if (evt.isStateChange) {
		[evt, response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])]
	} else {
		evt
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	logging("msr: $msr")
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)

	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logging("$device.displayName: Unhandled: $cmd")
	[:]
}

private updateStatus(){
    def result = []

    String statusText = ""
    if(device.currentValue('power') != null)
        statusText = "${device.currentValue('power')} W - "
    if(device.currentValue('energy') != null)
        statusText = statusText + "${device.currentValue('energy')} KWH - "
        
    if (statusText != ""){
        statusText = statusText.substring(0, statusText.length() - 2)
        sendEvent(name:"statusText", value: statusText, displayed:false)
    }
}

def on() {
	[
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000",
		zwave.meterV2.meterGet(scale: 2).format()
	]
}

def off() {
	[
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000",
		zwave.meterV2.meterGet(scale: 2).format()
	]
}

def poll() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def refresh() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def configure() {
    state.enableDebugging = settings.enableDebugging
    logging("Configuring Device For SmartThings Use")
    def cmds = []

    cmds = update_needed_settings()
    
    cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
    
    if (cmds != []) commands(cmds)
}

def reset() {
	return [
		zwave.meterV2.meterReset().format(),
		zwave.meterV2.meterGet(scale: 0).format()
	]
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

def convertParam(number, value) {
	switch (number){
    	case 201:
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

def configuration_model()
{
'''
<configuration>
  <Value type="list" byteSize="1" index="1" label="Metering Report" min="0" max="1" value="1" setting_type="zwave" fw="">
    <Help>
Enable or disable the sending of meter reports.
Default: Enable
   </Help>
        <Item label="Disable" value="0" />
        <Item label="Enable" value="1" />
  </Value>
  <Value type="byte" byteSize="2" index="2" label="Metering Report Frequency" min="1" max="65536" value="300" setting_type="zwave" fw="">
    <Help>
Number of seconds metering report is sent to controller
Range: 1~65536
Default: 300
    </Help>
  </Value>
  <Value type="byte" byteSize="1" index="3" label="Overload Protection" min="1" max="16" value="13" setting_type="zwave" fw="">
    <Help>
This parameter defines the percentage value of dimming step during the automatic control.
Range: 1~16
Default: 13
    </Help>
  </Value>
    <Value type="short" byteSize="1" index="4" label="Overload Notification" min="1" max="13" value="12" setting_type="zwave" fw="">
    <Help>
This parameter defines the time of single dimming step set in parameter 5 during the automatic control.
Range: 1~13
Default: 12
    </Help>
  </Value>
  <Value type="list" byteSize="1" index="5" label="LED Notifications" min="0" max="1" value="1" setting_type="zwave" fw="">
    <Help>
Enable or disable LED Notifications
Default: Enable
    </Help>
        <Item label="Disable" value="0" />
        <Item label="Enable" value="1" />
  </Value>
    <Value type="byte" byteSize="1" index="6" label="Power Report Percentage Threshold" min="1" max="100" value="5" setting_type="zwave" fw="">
    <Help>
Percentage of change in power consumption, voltage, electricity, or energy usage to be reported to controller by the Mini Plug
Range: 1~100
Default: 5
    </Help>
  </Value>
    <Value type="list" byteSize="1" index="7" label="On/Off Status Recovery After Power Failure" min="0" max="1" value="1" setting_type="zwave" fw="">
    <Help>
Mini Plug automatically turns OFF once power is restored (it does not remember the status prior to power outage)
Default: Previous State
    </Help>
        <Item label="Off" value="0" />
        <Item label="Previous State" value="1" />
  </Value>
  <Value type="list" byteSize="1" index="8" label="Auto Turn-Off Timer" min="0" max="1" value="0" setting_type="zwave" fw="">
    <Help>
Enable or disable the auto turn-off timer
Default: Disabled
    </Help>
        <Item label="Disable" value="0" />
        <Item label="Enable" value="1" />
  </Value>
  <Value type="byte" byteSize="2" index="9" label="Auto Turn-Off Timer Settings" min="0" max="65535" value="150" setting_type="zwave" fw="">
    <Help>
Number of minutes the Mini Plug will turn off after once the auto turn-off timer is enabled
Range: 0~65535
Default: 0
    </Help>
  </Value>
    <Value type="list" byteSize="1" index="10" label="Manual Control" min="0" max="1" value="1" setting_type="zwave" fw="">
    <Help>
Enable or disable manual control
Default: Enabled
    </Help>
        <Item label="Disable" value="0" />
        <Item label="Enable" value="1" />
  </Value>
  <Value type="boolean" index="enableDebugging" label="Enable Debug Logging?" value="true" setting_type="preference" fw="">
    <Help>
    </Help>
  </Value>
</configuration>
'''
}