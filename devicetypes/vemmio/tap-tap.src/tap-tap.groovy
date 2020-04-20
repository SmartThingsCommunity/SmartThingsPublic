 /**
 *  Copyright 2019 Rafał Dobrakowski (vemmio)
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
 *  Rafał Dobrakowski (vemmio)
 *  Date: 2019-03-25
 *
 */
 
metadata {
	definition (name: "Tap-Tap", namespace: "vemmio", author: "Rafał Dobrakowski", vid:"generic-buttons") {
		capability "Actuator"
		capability "Button"
        capability "Holdable Button"
		capability "Configuration"
		capability "Sensor"
        capability "Battery"
        capability "Health Check"
       
        attribute "numberOfButtons", "number"
        attribute "needUpdate", "string"
 
        fingerprint mfr: "030f", prod: "0001", model: "0020", deviceJoinName: "Rafał Dobrakowski"
	}
    
    preferences {
        // input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		// generate_preferences(configuration_model())
    }
	
    simulator {
        
	}
	
    tiles (scale: 2) {
    	
        multiAttributeTile(name:"TapTap", type:"generic", width:6, height:6) {
        
        	tileAttribute("device.level", key: "PRIMARY_CONTROL"){
            	attributeState "signal", label:'${currentValue}',backgroundColor:"#e86d13"
            }
            
            tileAttribute("battery", key:"SECONDARY_CONTROL"){
            	attributeState "battery", label: '${currentValue} %', action:""
            }
            
            
            tileAttribute("sequenceNumber", key:"SLIDER_CONTROL"){
            	attributeState "sequence", label: '${currentValue}', action:"", icon:"st.samsung.da.RC_ic_charge"
            }
            
        } 
       
		main "TapTap"
        
		
	} 
}

def installed() 
{
	state.enableDebugging = "true"
}

def parse(String description) {
    log.debug "description > $description"
	def results = []
	if (description.startsWith("Err")) {
	    results = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [0x2B: 1, 0x80: 1, 0x84: 1])
        
		if(cmd) {
        	results += zwaveEvent(cmd)
        }
        
		if(!results) {
        	results = [ descriptionText: cmd, displayed: false ]
        }
	}
    
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange cmd) {
        
        switch (cmd.upDown) {
        
           case 0: // Up
              buttonEvent(device.currentValue("numberOfButtons"), "pushed")
           break

           default:
              logging("Unhandled SwitchMultilevelStartLevelChange: ${cmd}")
           break
           
        }
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
        logging("keyAttributes: $cmd.keyAttributes")
        logging("sceneNumber: $cmd.sceneNumber")
        logging("sequenceNumber: $cmd.sequenceNumber")

        sendEvent(name: "sequenceNumber", value: cmd.sequenceNumber, displayed:false)
        sendEvent(name: "level", value: cmd.sceneNumber, displayed:false)
        
        switch (cmd.keyAttributes) {
           case 0:
              buttonEvent(cmd.sceneNumber, "pushed")
           break 
           case 1: // released
              if (!settings.holdMode || settings.holdMode == "2") buttonEvent(cmd.sceneNumber, "held")
           break
           case 2: // held
              if (settings.holdMode == "1") buttonEvent(cmd.sceneNumber, "held")
           break 
           default:
              logging("Unhandled CentralSceneNotification: ${cmd}")
           break
        }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x5B: 1, 0x20: 1, 0x31: 5, 0x30: 2, 0x84: 1, 0x70: 1])
	state.sec = 1
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) 
{
	response(configure())
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd)
{
	logging("WakeUpIntervalReport ${cmd.toString()}")
    state.wakeInterval = cmd.seconds
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
    logging("Device > ${device.displayName} < woke up > ${cmd}")
    
    def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

    // Only ask for battery if we haven't had a BatteryReport in a while
    if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
        result << response(zwave.batteryV1.batteryGet())
        result << response("delay 1200")  // leave time for device to respond to batteryGet
    }
    result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
    result
}

def buttonEvent(button, value) {
	createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    logging("Battery Report: $cmd")
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

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logging("AssociationReport $cmd")
    state."association${cmd.groupingIdentifier}" = cmd.nodeId[0]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
     update_current_properties(cmd)
     logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
     update_current_properties(cmd)
     logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'")
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	if (state.MSR == "003B-6341-5044") {
		updateDataValue("ver", "${cmd.applicationVersion >> 4}.${cmd.applicationVersion & 0xF}")
	}
	def text = "$device.displayName: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
	createEvent(descriptionText: text, isStateChange: false)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logging("Unhandled zwaveEvent: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
    updateDataValue("MSR", msr)
}

/**
* Triggered when Done button is pushed on Preference Pane
*/
def updated()
{
    logging("updated() is being called")
    state.wakeCount = 1
    def cmds = update_needed_settings()
    sendEvent(name: "checkInterval",   value: 2 * 60 * 12 * 60 + 5 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "numberOfButtons", value: 8, displayed: true)
    sendEvent(name: "needUpdate",      value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    
    if (cmds != []) response(commands(cmds))
}

def configure() {
	state.enableDebugging = settings.enableDebugging
    
    logging("Configuring Device For SmartThings Use")
    
    def cmds = []
    cmds = update_needed_settings()
    sendEvent(name: "numberOfButtons", value: 8, displayed: true)
    
    if (cmds != []) commands(cmds)
}

def ping() {
    logging("ping()")
	logging("Battery Device - Not sending ping commands")
}

def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
  
  	logging(">>> '${configuration}'")
  
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
    
    if(state.wakeInterval == null || state.wakeInterval != 86400){
        logging("Setting Wake Interval to 86400")
        cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds: 86400, nodeid:zwaveHubNodeId)
        cmds << zwave.wakeUpV1.wakeUpIntervalGet()
    }
    
    if(settings."3" == "1"){
       if(!state.association3 || state.association3 == "" || state.association3 == "1"){
          logging("Setting association group 3")
          cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:zwaveHubNodeId)
          cmds << zwave.associationV2.associationGet(groupingIdentifier:3)
       }
       if(!state.association5 || state.association5 == "" || state.association5 == "1"){
          logging("Setting association group 5")
          cmds << zwave.associationV2.associationSet(groupingIdentifier:5, nodeId:zwaveHubNodeId)
          cmds << zwave.associationV2.associationGet(groupingIdentifier:5)
       }
       if(!state.association7 || state.association7 == "" || state.association7 == "1"){
          logging("Setting association group 7")
          cmds << zwave.associationV2.associationSet(groupingIdentifier:7, nodeId:zwaveHubNodeId)
          cmds << zwave.associationV2.associationGet(groupingIdentifier:7)
       }
       if(!state.association9 || state.association9 == "" || state.association9 == "1"){
          logging("Setting association group 9")
          cmds << zwave.associationV2.associationSet(groupingIdentifier:9, nodeId:zwaveHubNodeId)
          cmds << zwave.associationV2.associationGet(groupingIdentifier:9)
       }
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
               if (it.@setonly == "true"){
                  if (it.@index == 5) {
                      if (state.wakeCount <= 3) {
                          logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"))
                          def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}")
                          cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                          cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                      } else {
                        logging ("Parameter has already sent. Will not send again until updated() gets called")
                    }
                  } else {
                      logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"))
                      def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}")
                      cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                      cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                  }
               } else {
                  isUpdateNeeded = "YES"
                  logging("Current value of parameter ${it.@index} is unknown")
                  cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
               }
            }
            else if (settings."${it.@index}" != null && cmd2Integer(currentProperties."${it.@index}") != convertParam(it.@index.toInteger(), settings."${it.@index}"))
            { 
                isUpdateNeeded = "YES"
                
                if (it.@index == 5) {
                    if (state.wakeCount <= 3) {
                        logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"))
                        def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}")
                        cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                        cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                    } else {
                        logging ("Parameter has already sent. Will not send again until updated() gets called")
                    }
                } else {
                    logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"))
                    def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}")
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
    long parValue
	switch (number){
    	case 5:
            switch (value) {
                case "1": 
                parValue = 4278190080
                break
                case "2": 
                parValue = 16711680
                break
                case "3": 
                parValue = 65280
                break
                default:
                parValue = value
                break
            }
        break
        default:
        	parValue = value.toLong()
        break
    }
    return parValue
}

private def logging(message) {
    if (state.enableDebugging == null || state.enableDebugging == "true") log.debug "$message"
}

/**
* Convert 1 and 2 bytes values to integer
*/
def cmd2Integer(array) { 
long value
    if (array != [255, 0, 0, 0]){
        switch(array.size()) {    
            case 1:
                value = array[0]
            break
            case 2:
                value = ((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
            break
            case 3:
                value = ((array[0] & 0xFF) << 16) | ((array[1] & 0xFF) << 8) | (array[2] & 0xFF)
            break
            case 4:
                value = ((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
            break
        }
    } else {
         value = 4278190080
    }
    return value
}

def integer2Cmd(value, size) {
	switch(size) {
	case 1:
		[value.toInteger()]
    break
	case 2:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        [value2.toInteger(), value1.toInteger()]
    break
    case 3:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        [value3.toInteger(), value2.toInteger(), value1.toInteger()]
    break
	case 4:
    	def short value1 = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        def short value4 = (value >> 24) & 0xFF
		[value4.toInteger(), value3.toInteger(), value2.toInteger(), value1.toInteger()]
	break
	}
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=1000) {
	delayBetween(commands.collect{ command(it) }, delay)
}

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
