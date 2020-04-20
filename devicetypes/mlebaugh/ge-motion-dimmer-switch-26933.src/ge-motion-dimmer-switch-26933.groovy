/**
 *  GE Motion Dimmer Switch
 *	Author: Matt lebaugh (@mlebaugh)
 *
 * Based off of the Dimmer Switch under Templates in the IDE 
 * Copyright (C) Matt LeBaugh
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
	definition (name: "GE Motion Dimmer Switch 26933", namespace: "mlebaugh", author: "Matt LeBaugh") {
		capability "Motion Sensor"
        capability "Actuator"
 		capability "Switch"
        capability "Switch Level"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
		capability "Light"
        capability "Button"
        
		command "toggleMode"
        command "Occupancy"
        command "Vacancy"
        command "Manual"
        command "SetDefaultLevel"
        command "LightSenseOn"
		command "LightSenseOff"
        
        attribute "operatingMode", "enum", ["Manual", "Vacancy", "Occupancy"]
        attribute "defaultLevel", "number"
        
        fingerprint mfr:"0063", prod:"494D", model: "3034", deviceJoinName: "GE Z-Wave Plus Motion Wall Dimmer"
	}

		preferences {
        	input title: "", description: "Select your prefrences here, they will be sent to the device once updated.\n\nTo verify the current settings of the device, they will be shown in the 'recently' page once any setting is updated", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			//param 3
            input ("operationmode","enum",title: "Operating Mode",
                description: "Occupancy: Automatically turn on and off the light with motion\nVacancy: Manually turn on, automatically turn off light with no motion.",
                options: [
                    "1" : "Manual",
                    "2" : "Vacancy (auto-off)",
                    "3" : "Occupancy (auto-on/off)"
                ],
                
            )
            //param 1
            input ("timeoutduration","enum", title: "Timeout Duration",
                description: "Length of time after no motion for the light to shut off in Occupancy/Vacancy modes",
                options: [
                    "0" : "Test (5s)",
                    "1" : "1 minute",
                    "5" : "5 minutes (default)",
                    "15" : "15 minutes",
                    "30" : "30 minutes",
                    "255" : "disabled"
                ]
            )
            //param 13
			input ("motionsensitivity","enum",title: "Motion Sensitivity",
            	description: "Motion Sensitivity",
                options: [
                    "1" : "High",
                    "2" : "Medium (default)",
                    "3" : "Low"
                ]
            )
            //param 14
			input ("lightsense","enum",title: "Light Sensing",
                description: "If enabled, Occupancy mode will only turn light on if it is dark",
                options: [
                    "0" : "Disabled",
                    "1" : "Enabled",
                ]
            )
			//param 6
			input ("motion","enum",title: "Motion Sensor",
                description: "Enable/Disable Motion Sensor.",
                options: [
                    "0" : "Disable",
                    "1" : "Enable",
                ]
            )
            //param 5
            input ("invertSwitch","enum",title: "Orientation",
                options: [
                    "0" : "Normal",
                    "1" : "Inverted",
                ],
            )
            //param 15
            input ("resetcycle","enum",title: "Reset Cycle",
                options: [
                    "0" : "Disabled",
                    "1" : "10 sec",
                    "2" : "20 sec (default)",
                    "3" : "30 sec",
                    "4" : "45 sec",
                    "110" : "27 mins",
                ],
            )
            
            //dimmersettings
            
            //descrip
            input title: "", description: "Z-Wave Dimmer Ramp rate settings\nDefaults: Step Percent: 1, Duration: 3\n", type: "paragraph", element: "paragraph"
            //param 9
            input "stepSize", "number", title: "Z-Wave Size of Steps in Percent", range: "1..99"
       		//param 10
            input "stepDuration", "number", title: "Z-Wave Steps Intervals each 10 ms", range: "1..255"
            
            //descrip
            input title: "", description: "Single tap ramp rate settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
            //param 18
            input ("dimrate", "enum", title: "Default Dim Rate", required: false,
                options: [
                	"0" : "Dim up the light to the default level quickly (Default)",
                    "1" : "Dim up the light to the default level slowly",
                ]
            )
            //param 17
            input "switchlevel","number", title: "Default Dim level\nDefault 0: Return to last state", range: "0..99"
            
            //descrip
            input title: "", description: "Manual Ramp rate settings for push and hold\nDefaults: Step Percent: 1, Duration: 3", type: "paragraph", element: "paragraph"
            //param 7
            input "manualStepSize", "number", title: "Manual Size of Steps in Percent", range: "1..99"
       		//param 8
            input "manualStepDuration", "number", title: "Manual Steps Intervals Each 10 ms", range: "1..255"
            //param 16
            input ("switchmode","enum", title: "Enable Switch mode",
                type: "enum",
                options: [
                    "0" : "Disabled (Default)",
                    "1" : "Enable\n The Dimmer will act as a switch.",
                ]
            )
            
                      
            //association groups
        	input (
            type: "paragraph",
            element: "paragraph",
            title: "Configure Association Groups:",
            description: "Devices in association group 2 will receive Basic Set commands directly from the switch when it is turned on or off. Use this to control another device as if it was connected to this switch.\n\n" +
                         "Devices in association group 3 Same as Group 2 for this device\n\n" +
                         "Devices are entered as a comma delimited list of IDs in hexadecimal format."
        	)
			           
        	input (
            	name: "requestedGroup2",
            	title: "Association Group 2 Members (Max of 5):",
            	type: "text",
            	required: false
        	)

        	input (
            	name: "requestedGroup3",
            	title: "Association Group 3 Members (Max of 4):",
            	type: "text",
            	required: false
        	)
            
             //descrip
            input title: "", description: "SetLevel default function (advanced)", type: "paragraph", element: "paragraph"
            input ("setlevelmode","enum", title: "Setlevel command mode",
                type: "enum",
                options: [
                    "0" : "Turn on the light when the setlevel command is called (Default)",
                    "1" : "Setlevel will not turn on the light only set the default. Only use this if you cannot use the setdefaultlevel command",
                ]
            )
    }



	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
		tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
			attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
			attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
        
		standardTile("motion","device.motion", inactiveLabel: false, width: 2, height: 2) {
                state "inactive",label:'no motion',icon:"st.motion.motion.inactive",backgroundColor:"#ffffff"
                state "active",label:'motion',icon:"st.motion.motion.active",backgroundColor:"#53a7c0"
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
              
		standardTile("operatingMode", "device.operatingMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'Mode toggle: ${currentValue}', unit:"", action:"toggleMode"
		}

		main(["switch"])
		details(["switch", "motion", "operatingMode", "refresh"])

	}
}


def parse(String description) {
    def result = null
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x56: 1, 0x70: 2, 0x72: 2, 0x85: 2, 0x71: 3, 0x56: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
        }
	}
    if (!result) { log.warn "Parse returned ${result} for $description" }
    else {log.debug "Parse returned ${result}"}
	return result
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	log.debug("zwaveEvent(): CRC-16 Encapsulation Command received: ${cmd}")
	def encapsulatedCommand = zwave.commandClass(cmd.commandClass)?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		log.debug("zwaveEvent(): Could not extract command from ${cmd}")
	} else {
		return zwaveEvent(encapsulatedCommand)
	}
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	def result = []
    if (cmd.value == 255) {
    	result << createEvent([name: "button", value: "pushed", data: [buttonNumber: "1"], descriptionText: "On/Up on (button 1) $device.displayName was pushed", isStateChange: true, type: "physical"])
    }
	else if (cmd.value == 0) {
    	result << createEvent([name: "button", value: "pushed", data: [buttonNumber: "2"], descriptionText: "Off/Down (button 2) on $device.displayName was pushed", isStateChange: true, type: "physical"])
    }
    return result
}


def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	log.debug "---ASSOCIATION REPORT V2--- ${device.displayName} sent groupingIdentifier: ${cmd.groupingIdentifier} maxNodesSupported: ${cmd.maxNodesSupported} nodeId: ${cmd.nodeId} reportsToFollow: ${cmd.reportsToFollow}"
    state.group3 = "1,2"
    if (cmd.groupingIdentifier == 3) {
    	if (cmd.nodeId.contains(zwaveHubNodeId)) {
        	sendEvent(name: "numberOfButtons", value: 2, displayed: false)
        }
        else {
        	sendEvent(name: "numberOfButtons", value: 0, displayed: false)
			sendHubCommand(new physicalgraph.device.HubAction(zwave.associationV2.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()))
			sendHubCommand(new physicalgraph.device.HubAction(zwave.associationV2.associationGet(groupingIdentifier: 3).format()))
        }
    }
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "---CONFIGURATION REPORT V2--- ${device.displayName} sent ${cmd}"
    def config = cmd.scaledConfigurationValue
    def result = []
    if (cmd.parameterNumber == 1) {
		def value = config == 0 ? "Test 5s" : config == 1 ? "1 minute" : config == 5 ? "5 minute" : config == 15 ? "15 minute" : config == 30 ? "30 minute" : "disabled" 
    	result << createEvent([name:"TimeoutDuration", value: value, displayed:true, isStateChange:true])
    } else if (cmd.parameterNumber == 13) {
		def value = config == 1 ? "High" : config == 2 ? "Medium" : "Low"
    	result << createEvent([name:"MotionSensitivity", value: value, displayed:true, isStateChange:true])
	} else if (cmd.parameterNumber == 14) {
		def value = config == 0 ? "Disabled" : "Enabled"
    	result << createEvent([name:"LightSense", value: value, displayed:true, isStateChange:true])
    } else if (cmd.parameterNumber == 15) {
    	def value = config == 0 ? "Disabled" : config == 1 ? "10 sec" : config == 2 ? "20 sec" : config == 3 ? "30 sec" : config == 4 ? "45 sec" : "27 minute" 
    	result << createEvent([name:"ResetCycle", value: value, displayed:true, isStateChange:true])
    } else if (cmd.parameterNumber == 3) {
    	if (config == 1 ) {
        	result << createEvent([name:"operatingMode", value: "Manual", displayed:true, isStateChange:true])
         } else if (config == 2 ) {
        	result << createEvent([name:"operatingMode", value: "Vacancy", displayed:true, isStateChange:true])
        } else if (config == 3 ) {
        	result << createEvent([name:"operatingMode", value: "Occupancy", displayed:true, isStateChange:true])
        }
    } else if (cmd.parameterNumber == 6) {
    	def value = config == 0 ? "Disabled" : "Enabled"
    	result << createEvent([name:"MotionSensor", value: value, displayed:true, isStateChange:true])
    } else if (cmd.parameterNumber == 5) {
    	def value = config == 0 ? "Normal" : "Inverted"
    	result << createEvent([name:"SwitchOrientation", value: value, displayed:true, isStateChange:true])
    } 
    //dimmer settings
      else if (cmd.parameterNumber == 7) {
    	result << createEvent([name:"StepSize", value: config, displayed:true, isStateChange:true])
    } else if (cmd.parameterNumber == 8) {
    	result << createEvent([name:"StepDuration", value: config, displayed:true, isStateChange:true])
    } else if (cmd.parameterNumber == 9) {
    	result << createEvent([name:"ManualStepSize", value: config, displayed:true, isStateChange:true])
    } else if (cmd.parameterNumber == 10) {
    	result << createEvent([name:"ManualStepDuration", value: config, displayed:true, isStateChange:true])
    } else if (cmd.parameterNumber == 16) {
    	result << createEvent([name:"SwitchMode", value: config, displayed:true, isStateChange:true])
    } else if (cmd.parameterNumber == 17) {
    	result << createEvent([name:"defaultLevel", value: config, displayed:true, isStateChange:true])
    } else if (cmd.parameterNumber == 18) {
    	result << createEvent([name:"DimRate", value: config, displayed:true, isStateChange:true])
    } else {
     log.warn "Parameter  ${cmd.parameterNumber} ${config}"}
    //
   return result
}


def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    log.debug "---BINARY SWITCH REPORT V1--- ${device.displayName} sent ${cmd}"
    sendEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}


def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.debug "---MANUFACTURER SPECIFIC REPORT V2--- ${device.displayName} sent ${cmd}"
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
    state.manufacturer=cmd.manufacturerName
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)	
    sendEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	log.debug "---VERSION REPORT V1--- ${device.displayName} is running firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
}


def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
	log.debug "---NOTIFICATION REPORT V3--- ${device.displayName} sent ${cmd}"
	def result = []
    def cmds = []
	if (cmd.notificationType == 0x07) {
		if ((cmd.event == 0x00)) { 
           	result << createEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion has stopped")
         } else if (cmd.event == 0x08) {
            result << createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")	  
        }
    }
	result  
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.warn "${device.displayName} received unhandled command: ${cmd}"
}


def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	log.debug "---SWITCH MULTILEVEL REPORT V3--- ${device.displayName} sent ${cmd}"
	dimmerEvents(cmd)
}


def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd) {
	log.debug "---SWITCH MULTILEVEL SET V3--- ${device.displayName} sent ${cmd}"
	dimmerEvents(cmd)
}


def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange cmd) {
	log.debug "---SWITCH MULTILEVEL Stop Level Change--- ${device.displayName} sent ${cmd}"
	dimmerEvents(cmd)
}


private dimmerEvents(physicalgraph.zwave.Command cmd) {
    def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}


def on() {
    delayBetween([zwave.basicV1.basicSet(value: 0xFF).format(), zwave.switchMultilevelV3.switchMultilevelGet().format()], 5000) 
}


def off() {
	delayBetween ([zwave.basicV1.basicSet(value: 0x00).format(), zwave.switchMultilevelV3.switchMultilevelGet().format()], 5000)
}

/*
def setLevel(value) {
	def valueaux = value as Integer
	def level = Math.min(valueaux, 99)
    sendEvent(name: "level", value: level, unit: "%")
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV3.switchMultilevelGet().format()], 500)
}
*/


def setLevel(value) {
	def valueaux = value as Integer
	def level = Math.min(valueaux, 99)
    def cmds = []
    	
    if (settings.setlevelmode == "1"){ 
    	log.debug "setlevel with advanced mode"
    	if (device.currentValue("switch") == "on") {
            sendEvent(name: "level", value: level, unit: "%")
            cmds << zwave.configurationV1.configurationSet(configurationValue: [level] , parameterNumber: 17, size: 1)
            cmds << zwave.basicV1.basicSet(value: level)
            cmds << zwave.configurationV1.configurationGet(parameterNumber: 17)
            cmds << zwave.switchMultilevelV3.switchMultilevelGet()
        } else if (device.currentValue("switch") == "off") {
            cmds << zwave.configurationV1.configurationSet(configurationValue: [level] , parameterNumber: 17, size: 1)
            cmds << zwave.configurationV1.configurationGet(parameterNumber: 17)
        }        
        sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 500)
    } else {
    	log.debug "setlevel with basic mode ${settings.setlevelmode}"
    	sendEvent(name: "level", value: level, unit: "%")
		delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV3.switchMultilevelGet().format()], 500)
    }
}

def setLevel(value, duration) {
	def valueaux = value as Integer
	def level = Math.min(valueaux, 99)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
    sendEvent(name: "level", value: level, unit: "%")
	zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format()
}


def poll() {
	zwave.switchMultilevelV3.switchMultilevelGet().format()
}


def ping() {
	refresh()
}


def refresh() {
	log.debug "refresh() is called"
    delayBetween([
    	zwave.notificationV3.notificationGet(notificationType: 7).format(),
        zwave.switchMultilevelV3.switchMultilevelGet().format()
	],100)
}


def toggleMode() {
	log.debug("Toggling Mode") 
    def cmds = []
    if (device.currentValue("operatingMode") == "Manual") { 
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [2] , parameterNumber: 3, size: 1)
    }
    else if (device.currentValue("operatingMode") == "Vacancy") {
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [3], parameterNumber: 3, size: 1)
    }
    else if (device.currentValue("operatingMode") == "Occupancy") {
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1)
    }
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 3)
        sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 1000)

}


def SetModeNumber(value) {
	log.debug("Setting mode by number: ${value}") 
    def cmds = []
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [value] , parameterNumber: 3, size: 1)
  		cmds << zwave.configurationV1.configurationGet(parameterNumber: 3)
    sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 1000)

}


def Occupancy() {
	def cmds = []
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [3] , parameterNumber: 3, size: 1)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: 3)
    sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 1000)

}


def Vacancy() {
	def cmds = []
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [2] , parameterNumber: 3, size: 1)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: 3)
    sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 1000)

}

def Manual() {
	def cmds = []
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [1] , parameterNumber: 3, size: 1)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: 3)
    sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 1000)

}

def SetDefaultLevel(value) {
	log.debug("Setting default level: ${value}") 
    def cmds = []
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [value] , parameterNumber: 17, size: 1)
  		cmds << zwave.configurationV1.configurationGet(parameterNumber: 17)
    sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 1000)
}

def LightSenseOn() {
	log.debug("Setting Light Sense On") 
    def cmds = []
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 14, size: 1)
    sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 1000)
}

def LightSenseOff() {
	log.debug("Setting Light Sense Off") 
    def cmds = []
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 14, size: 1)
    sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 1000)
}

def installed() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def updated() {
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    if (state.lastUpdated && now() <= state.lastUpdated + 3000) return
    state.lastUpdated = now()

	def cmds = []
	//switch and dimmer settings
        //param 1 timeout duration
        if (settings.timeoutduration) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [settings.timeoutduration.toInteger()], parameterNumber: 1, size: 1)
        	cmds << zwave.configurationV1.configurationGet(parameterNumber: 1)
        }
        //param 13 motion sensitivity
        if (settings.motionsensitivity) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [settings.motionsensitivity.toInteger()], parameterNumber: 13, size: 1)
        	cmds << zwave.configurationV1.configurationGet(parameterNumber: 13)
        }
        //param 14 lightsense
        if (settings.lightsense) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [settings.lightsense.toInteger()], parameterNumber: 14, size: 1)
        	cmds << zwave.configurationV1.configurationGet(parameterNumber: 14)
    	}
        //param 15 reset cycle
        if (settings.resetcycle) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,settings.resetcycle.toInteger()], parameterNumber: 15, size: 1)
        	cmds << zwave.configurationV1.configurationGet(parameterNumber: 15)
        }
        //param 3 operating mode
        if (settings.operationmode) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [settings.operationmode.toInteger()], parameterNumber: 3, size: 1)
        	cmds << zwave.configurationV1.configurationGet(parameterNumber: 3)
        }
        //param 6 motion
        if (settings.motion) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [settings.motion.toInteger()], parameterNumber: 6, size: 1)
        	cmds << zwave.configurationV1.configurationGet(parameterNumber: 6)
        }
        //param 5 invert switch
        if (settings.invertSwitch) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [settings.invertSwitch.toInteger()], parameterNumber: 5, size: 1)
        	cmds << zwave.configurationV1.configurationGet(parameterNumber: 5)
		}
        //association groups
			cmds << zwave.associationV1.associationSet(groupingIdentifier:0, nodeId:zwaveHubNodeId)
        	cmds << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
		//don't think we need this - only one group needs to be associated for buttons
        	cmds << zwave.associationV1.associationRemove(groupingIdentifier:2, nodeId:zwaveHubNodeId)
        //needed for button presses
        	cmds << zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:zwaveHubNodeId)
		//add endpoints to groups
		def nodes = []
		if (settings.requestedGroup2 != state.currentGroup2) {
    	    nodes = parseAssocGroupList(settings.requestedGroup2, 2)
        	cmds << zwave.associationV2.associationRemove(groupingIdentifier: 2, nodeId: [])
        	cmds << zwave.associationV2.associationSet(groupingIdentifier: 2, nodeId: nodes)
        	cmds << zwave.associationV2.associationGet(groupingIdentifier: 2)
        	state.currentGroup2 = settings.requestedGroup2
    	}

    	if (settings.requestedGroup3 != state.currentGroup3) {
        	nodes = parseAssocGroupList(settings.requestedGroup3, 3)
        	cmds << zwave.associationV2.associationRemove(groupingIdentifier: 3, nodeId: [])
        	cmds << zwave.associationV2.associationSet(groupingIdentifier: 3, nodeId: nodes)
        	cmds << zwave.associationV2.associationGet(groupingIdentifier: 3)
        	state.currentGroup3 = settings.requestedGroup3
    	}
	// end switch and dimmer settings
		        
    // dimmer specific settings
        //param 7 zwave step
        if (settings.stepSize) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [settings.stepSize.toInteger()], parameterNumber: 7, size: 1)    
        	cmds << zwave.configurationV1.configurationGet(parameterNumber: 7)
        }
        //param 8 zwave duration
        if (settings.stepDuration) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,settings.stepDuration.toInteger()], parameterNumber: 8, size: 2)
        	cmds << zwave.configurationV1.configurationGet(parameterNumber: 8)
        }
        //param 9 manual step
        if (settings.manualStepSize) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [settings.manualStepSize.toInteger()], parameterNumber: 9, size: 1)
        	cmds << zwave.configurationV1.configurationGet(parameterNumber: 9)
        }
        //param 10 manual duration
        if (settings.manualStepDuration) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,settings.manualStepDuration.toInteger()], parameterNumber: 10, size: 2)
        	cmds << zwave.configurationV1.configurationGet(parameterNumber: 10)  
		}
        //switch mode param 16
        if (settings.switchmode == 0) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 16, size: 1)
        } else if (settings.switchmode) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [settings.switchmode.toInteger()], parameterNumber: 16, size: 1)
        }
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 16)
        //switch level param 17
        if (settings.switchlevel == 0) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 17, size: 1)
        } else if (settings.switchlevel) {
        	cmds << zwave.configurationV1.configurationSet(configurationValue: [settings.switchlevel.toInteger()], parameterNumber: 17, size: 1)
        }
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 17)
        //dim rate param 18
        if (settings.dimrate) {cmds << zwave.configurationV1.configurationSet(configurationValue: [settings.dimrate.toInteger()], parameterNumber: 18, size: 1)}
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 18)
	//end of dimmer specific params
        
    sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 500)
}

def Up() {
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: "1"], descriptionText: "On/Up (button 1) on $device.displayName was pushed", isStateChange: true, type: "digital")
    on()
}

def Down() {
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: "2"], descriptionText: "Off/Down (button 2) on $device.displayName was pushed", isStateChange: true, type: "digital")
    off()
}

def configure() {
	def cmds = []
		// association groups
		cmds << zwave.associationV1.associationSet(groupingIdentifier:0, nodeId:zwaveHubNodeId)
        cmds << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
		//don't think we need this
        cmds << zwave.associationV1.associationRemove(groupingIdentifier:2, nodeId:zwaveHubNodeId)
        //needed for button presses
        cmds << zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:zwaveHubNodeId)
	sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 1000)
}

private parseAssocGroupList(list, group) {
    def nodes = group == 2 ? [] : [zwaveHubNodeId]
    if (list) {
        def nodeList = list.split(',')
        def max = group == 2 ? 5 : 4
        def count = 0

        nodeList.each { node ->
            node = node.trim()
            if ( count >= max) {
                log.warn "Association Group ${group}: Number of members is greater than ${max}! The following member was discarded: ${node}"
            }
            else if (node.matches("\\p{XDigit}+")) {
                def nodeId = Integer.parseInt(node,16)
                if (nodeId == zwaveHubNodeId) {
                	log.warn "Association Group ${group}: Adding the hub as an association is not allowed (it would break double-tap)."
                }
                else if ( (nodeId > 0) & (nodeId < 256) ) {
                    nodes << nodeId
                    count++
                }
                else {
                    log.warn "Association Group ${group}: Invalid member: ${node}"
                }
            }
            else {
                log.warn "Association Group ${group}: Invalid member: ${node}"
            }
        }
    }
    
    return nodes
}