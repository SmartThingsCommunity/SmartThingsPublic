/**
 *
 *  zooZ Z-Wave Smart Plug
 *   
 *	github: Eric Maycock (erocm123)
 *	Date: 2016-12-01
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
	definition (name: "zooZ Z-Wave Smart Plug", namespace: "erocm123", author: "Eric Maycock", vid:"generic-switch-power-energy") {
		capability "Energy Meter"
        capability "Voltage Measurement"
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"
        capability "Health Check"

		command "reset"
        
        attribute   "needUpdate", "string"
        attribute   "amperage", "number"
        
        fingerprint mfr: "027A", prod: "0101", model: "000A"

        fingerprint deviceId: "0x1001", inClusters: "0x5E,0x25,0x32,0x27,0x2C,0x2B,0x70,0x85,0x59,0x72,0x86,0x7A,0x73,0x5A"
                                                           
	}
    
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())  
    }

	simulator {
	}

	tiles(scale: 2){
        multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
           		attributeState "statusText", label:'${currentValue}'       		
            }
	    }
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("power", "device.power", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
        valueTile("voltage", "device.voltage", width: 2, height: 2) {
			state "default", label:'${currentValue} V'
		}
		valueTile("amperage", "device.amperage", width: 2, height: 2) {
			state "default", label:'${currentValue} A'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset\r\nkWh', action:"reset"
		}
		standardTile("energy", "device.energy", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }

		main "switch"
		details (["switch", "power", "amperage", "voltage", "energy", "refresh", "configure", "reset"])
	}
}

def updated()
{
    state.enableDebugging = settings.enableDebugging
    logging("updated() is being called")
    sendEvent(name: "checkInterval", value: 2 * 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    def cmds = []
    
    cmds = update_needed_settings()
    
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
    
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
    logging("MeterReport $cmd")
    def event
	if (cmd.scale == 0) {
    	if (cmd.meterType == 161) {
		    event = createEvent(name: "voltage", value: cmd.scaledMeterValue, unit: "V")
        } else if (cmd.meterType == 33) {
        	event = createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
        }
	} else if (cmd.scale == 1) {
		event = createEvent(name: "amperage", value: cmd.scaledMeterValue, unit: "A")
	} else if (cmd.scale == 2) {
		event = createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
	}
    runIn(1, "updateStatus")
    return event
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

    String statusText = ""

    if(device.currentValue('power') != null)
        statusText = "${device.currentValue('power')} W - "
    
    if(device.currentValue('amperage') != null)
        statusText = statusText + "${device.currentValue('amperage')} A - "
    
    if(device.currentValue('voltage') != null)
        statusText = statusText + "${device.currentValue('voltage')} V - "
    
    if(device.currentValue('energy') != null)
        statusText = statusText + "${device.currentValue('energy')} kWh - "
        
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
        zwave.meterV2.meterGet(scale: 1).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def refresh() {
    logging("refresh()")
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 1).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def ping() {
    logging("ping()")
	refresh()
}

def configure() {
    state.enableDebugging = settings.enableDebugging
    logging("Configuring Device For SmartThings Use")
    def cmds = []

    cmds = update_needed_settings()
    
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

 /*  Code has elements from other community source @CyrilPeponnet (Z-Wave Parameter Sync). */

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
                isUpdateNeeded = "YES"
                logging("Current value of parameter ${it.@index} is unknown")
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
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
<Value type="byte" byteSize="2" index="151" label="Power Report Value Threshold" min="1" max="65535" value="50" setting_type="zwave" fw="">
 <Help>
Number of Watts the appliance needs to go over for the change to be reported
Range: 1 to 65535
Default: 50
</Help>
</Value>
<Value type="byte" byteSize="1" index="152" label="Power Report Percentage Threshold" min="1" max="255" value="10" setting_type="zwave" fw="">
 <Help>
Percentage in power usage change the appliance needs to go over for the event to be reported
Range: 1 to 255
Default: 10
</Help>
</Value>
<Value type="byte" byteSize="4" index="171" label="Power Report Frequency" min="0" max="2678400" value="30" setting_type="zwave" fw="">
 <Help>
Number of seconds for the interval the Smart Plug will report power consumption
Range: 0,5 to 2678400
Default: 30
</Help>
</Value>
<Value type="byte" byteSize="4" index="172" label="Energy Report Frequency" min="0" max="2678400" value="300" setting_type="zwave" fw="">
 <Help>
Number of seconds for the interval the Smart Plug will report energy usage
Range: 0,5 to 2678400
Default: 300
</Help>
</Value>
<Value type="byte" byteSize="4" index="173" label="Voltage Report Frequency" min="0" max="2678400" value="60" setting_type="zwave" fw="">
 <Help>
Number of seconds for the interval the Smart Plug will report voltage
Range: 0,5 to 2678400
Default: 0 (Disabled)
</Help>
</Value>
<Value type="byte" byteSize="4" index="174" label="Electricity Report Frequency" min="0" max="2678400" value="60" setting_type="zwave" fw="">
 <Help>
Number of seconds for the interval the Smart Plug will report energy current
Range: 0,5 to 2678400
Default: 0 (Disabled)
</Help>
</Value>
<Value type="list" byteSize="1" index="20" label="Overload Protection" min="0" max="1" value="1" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: Enabled
</Help>
        <Item label="Disable" value="0" />
        <Item label="Enable" value="1" />
</Value>
<Value type="list" byteSize="1" index="21" label="On/Off Status Recovery After Power Failure" min="0" max="2" value="0" setting_type="zwave" fw="">
 <Help>
Smart Plug remembers the status prior to power outage and turns back to it (default)
Range: 0 to 2
Default: Previous
</Help>
        <Item label="Previous" value="0" />
        <Item label="On" value="1" />
        <Item label="Off" value="2" />
</Value>
<Value type="list" byteSize="1" index="24" label="On/Off Status Change Notifications" min="0" max="2" value="1" setting_type="zwave" fw="">
 <Help>
On, Off, or Manual Only. When set to "Manual Only", notifications are only sent when physically pressing the button on the plug
Range: 0 to 2
Default: On
</Help>
        <Item label="Off" value="0" />
        <Item label="On" value="1" />
        <Item label="Manual Only" value="2" />
</Value>
<Value type="list" byteSize="1" index="27" label="LED Indicator Control" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
LED indicator will display power consumption whenever the device is plugged in (LED stays on at all times) or for 5 Seconds after it is turned on or off 
Range: 0 to 1
Default: Always
</Help>
        <Item label="Always" value="0" />
        <Item label="For 5 Seconds" value="1" />
</Value>
  <Value type="boolean" index="enableDebugging" label="Enable Debug Logging?" value="true" setting_type="preference" fw="">
    <Help>
    </Help>
  </Value>
</configuration>
'''
}
