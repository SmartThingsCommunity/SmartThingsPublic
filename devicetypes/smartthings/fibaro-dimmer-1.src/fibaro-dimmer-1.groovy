/**
 *  Device Type Definition File
 *
 *  Device Type:		Fibaro Dimmer
 *  File Name:			fibaro-dimmer.groovy
 *	Initial Release:	2015-06-00
 *	Author:				SmartThings
 *
 *  Copyright 2015 SmartThings
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
	// Automatically generated. Make future change here.
	definition (name: "Fibaro Dimmer 1", namespace: "smartthings", author: "SmartThings, Elnar Hajiyev") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"

		//Extending Fibaro Dimmer 1 devices with scene attribute
        attribute "scene", "number"
 
        command	  "configureParams"

        fingerprint deviceId: "0x1101", inClusters: "0x72,0x86,0x70,0x85,0x8E,0x26,0x7A,0x27,0x73,0xEF,0x26,0x2B"
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

	tiles {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("configureParams", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configureParams", icon:"st.secondary.configure"
		}

		main(["switch"])
		details(["switch", "refresh", 
           "levelSliderControl",
           "configureParams"
        ])
	}
    
    preferences {
	    input name: "param1", type: "enum", defaultValue: "255", required: true,
            options: ["0" : "0",
                      "1" : "1",
                      "2" : "2",
                      "255" : "255"],
            title: "1. Activate / deactivate functions ALL ON / ALL OFF.\n" +
                   "Available settings:\n" +
                   "0 = All ON not active, All OFF not active,\n" +
                   "1 = All ON not active, All OFF active,\n" +
                   "2 = All ON active, All OFF not active,\n" +
                   "255 = All ON active, All OFF active.\n" +
                   "Default value: 255."

        input name: "param6", type: "number", range: "0..2", defaultValue: "0", required: true,
            title: "6. Sending commands to control devices assigned to 1st association group (key no. 1).\n" +
                   "Available settings:\n" +
                   "0 = commands are sent when device is turned on and off.\n" +
                   "1 = commands are sent when device is turned off. Enabling device does not send control commands. Double-clicking key sends 'turn on' command, dimmers memorize the last saved state (e.g. 50% brightness).\n" +
                   "2 = commands are sent when device is turned off. Enabling device does not send control commands. Double-clicking key sends 'turn on' command and dimmers are set to 100% brightness.\n" +
                   "Default value: 1.\n\n" +
                   "NOTE: Parameter 15 value must be set to 1 to work properly. This activates the double-click functionality - dimmer/roller shutter control."

        input name: "param7", type: "number", range: "0..1", defaultValue: "1", required: true,
            title: "7. Checking the device status before sending a control frame from the key no. 2.\n" +
                   "Available settings:\n" +
                   "0 = Device status is not checked.\n" +
                   "1 = Device status is checked.\n" +
                   "Default value: 1.\n\n" +
                   "Info: Key no. 2 is not represented by any physical device expect of devices on association list. " +
                   "This functionality prevents of lack of reaction on pressing key no. 2 through polling devices from list one by one and checking thier actual states.\n" +
                   "It is not possible to check the device status before sending a control frame from the key no. 2 if roller blind switch is chosen in parameter 14 (value 2)\n" +
                   "If devices state is checked before sending asociation then parameter 19 should be set to value 0."

        input name: "param8", type: "number", range: "1..99", defaultValue: "1", required: true,
            title: "8. The percentage of a dimming step at automatic control.\n" +
                   "Available settings: 1-99\n" +
                   "Default value: 1."

        input name: "param9", type: "number", range: "1..255", defaultValue: "5", required: true,
            title: "9. Time of manually moving the Dimmer between the extreme dimming values.\n" +
                   "Available settings: 1-255 (10ms – 2.5s)\n" +
                   "Default value: 5."

        input name: "param10", type: "number", range: "0..255", defaultValue: "1", required: true,
            title: "10. Time of Automatic moving the Dimmer between the extreme dimming values.\n" +
                   "Available settings: 0-255 (0ms – 2.5s)\n" +
                   "0 - this value disables the smooth change in light intensity\n" +
                   "Default value: 1.\n\n" +
                   "NOTE value 0 is required for inductive and capacitive devices unsuitable for dimming, (e.g. fluorescent lamps , motors etc.)."

        input name: "param11", type: "number", range: "1..99", defaultValue: "1", required: true,
            title: "11. The percentage of a dimming step at manual control.\n" +
                   "Available settings: 1-99\n" +
                   "Default value: 1."

        input name: "param12", type: "number", range: "2..99", defaultValue: "99", required: true,
            title: "12. Maximum Dimmer level control.\n" +
                   "Available settings: 2-99\n" +
                   "Default value: 99."

        input name: "param13", type: "number", range: "1..98", defaultValue: "2", required: true,
            title: "13. Minimum Dimmer level control\n" +
                   "Available settings: 1-98\n" +
                   "Default value: 2.\n\n" +
                   "NOTE: The maximum level may not be lower than the minimum level.\n" +
                   "Recommended values of parameters 12 and 13 (max and min level) for controlling the devices are as follows:\n" +
                   "- AC motors [min 60%, max 99%]\n" +
                   "- fluorescent lamps, fluorescent tubes, LED [min 98%, max 99%] [parameter 10 set to 0]"

        input name: "param14", type: "number", range: "0..2", defaultValue: "0", required: true,
            title: "14. Switch type. Choose between momentary switch and toggle switch.\n" +
                   "Available settings:\n" +
                   "0 = momentary switch,\n" +
                   "1 = toggle switch,\n" +
                   "2 = Roller blind switch (UP / DOWN) - two switch keys operate the Dimmer.\n" +
                   "Default value: 0."

        input name: "param15", type: "number", range: "0..1", defaultValue: "1", required: true,
            title: "15. Double click option (set lightning at 100%).\n" +
                   "Available settings:\n" +
                   "0 = Double click disabled,\n" +
                   "1 = Double click enabled.\n" +
                   "Default value: 1."

        input name: "param16", type: "number", range: "0..1", defaultValue: "1", required: true,
            title: "16. Saving the state of the device after a power failure. The Dimmer will return to the last position before power failure.\n" +
                   "Available settings:\n" +
                   "0 = the Dimmer does not save the state after a power failure, it returns to 'off' position,\n" +
                   "1 = the Dimmer saves its state before power failure.\n" +
                   "Default value: 1."

        input name: "param17", type: "number", range: "0..1", defaultValue: "0", required: true,
            title: "17. The function of 3 - way switch, provides the option to double key no. 1. " +
                   "The Dimmer may control two toggle push-buttons or an infinite number of momentary push-buttons.\n" +
                   "Available settings:\n" +
                   "0 = the function of 3-way switch is disabled,\n" +
                   "1 = the function of 3-way switch is enabled.\n" +
                   "Default value: 0."

        input name: "param18", type: "number", range: "0..1", defaultValue: "0", required: true,
            title: "18. The function of synchronizing the light level for associated devices. The Dimmer communicates the position to the associated device.\n" +
                   "Available settings:\n" +
                   "0 = function disabled,\n" +
                   "1 = function enabled.\n" +
                   "Default value: 0."

        input name: "param19", type: "number", range: "0..1", defaultValue: "0", required: true,
            title: "19. Assigns bistable key status to the device status.\n" +
                   "Available settings:\n" +
                   "0 = [On / Off] device changes status on key status change.\n" +
                   "1 = Device status depends on key status: ON when the key is ON, OFF when the key is OFF.\n"
                   "Default value: 0.\n\n" +
                   "Info: Remote control from Fibaro System Is Still Possible. This function is useful When you want display status of external devices, e.g. Motion Sensor, in Fibaro System."

        input name: "param30", type: "number", range: "0..3", defaultValue: "3", required: true,
            title: "30. Alarm of any type (general alarm, water flooding alarm, smoke alarm: CO, CO2, temperature alarm).\n" +
                   "Available settings:\n" +
                   "0 = DEACTIVATION - the device does not respond to alarm data frames,\n" +
                   "1 = ALARM DIMMER ON - the device turns on after detecting an alarm,\n" +
                   "2 = ALARM DIMMER OFF - the device turns off after detecting an alarm,\n" +
                   "3 = ALARM FLASHING the device periodically changes its status to the opposite, when it detects an alarm within 10 min."
                   "Default value: 3."

        input name: "param39", type: "number", range: "1..65535", defaultValue: "600", required: true,
            title: "39. Active flashing alarm time.\n" +
                   "Available settings: [1-65535][ms]\n" +
                   "Default value: 600."

        input name: "param41", type: "number", range: "0..1", defaultValue: "0", required: true,
            title: "41. Scene activation functionality.\n" +
                   "The device offers the possibility of sending commands compatible with Command class scene activation. " +
                   "Information is sent to devices assigned to association group no. 3. " +
                   "Controllers such as Home Center 2 are able to interpret such commands and based on these commands they activate scenes, to which specific scene IDs have been assigned. " +
                   "The user may expand the functionality of the button connected to inputs S1 and S2 by distinguishing the actions of keys connected to those inputs. " +
                   "For example: double click would activate the scene “goodnight” and triple click would activate the scene “morning”.\n" +
                   "Available settings:\n" +
                   "0 = functionality deactivated,\n" +
                   "1 = functionality activated.\n" +
                   "Default value: 0.\n\n" +
                   "Scene ID is determined as follows:\n" +
                   "Momentary switch:\n" +
                   "Input S1: holding down ID 12 (option inactive in case of roller blind switch), " +
                   "releasing ID 13, double click ID 14 (depends on parameters 15 value - 1 = double click active), " +
                   "triple click ID 15, one click ID 16.\n" +
                   "Input S2: holding down ID 22 (option inactive in case of roller blind switch), " +
                   "releasing ID 23, double click ID 24 (depends on parameters 15 value - 1 = double click active) option inactive in case of roller blind switch, " +
                   "triple click ID 25, one click ID 26.\n\n" +
                   "Toggle switch:\n" +
                   "Input S1: holding down ID 12, releasing ID 13, double click ID 14 (depends on parameters 15 value - 1 = double click active), triple click ID 15. " +
                   "If parameter no. 19 is set to 0: single click ID16 is sent. If parameter no. 19 is set to 1 following IDs are sent: switch from “off” to “on” ID 10, switch from “on” to “off” ID 11.\n" +
                   "Input S2: holding down ID 22, releasing ID 23, double click ID 24, (depends on parameters 15 value - 1 = double click active), triple click ID 25. " +
                   "If parameter no.19 set to 0 (default), then one click ID 26 is sent. If parameter no.19 is set to 1 following IDs are sent: switch from “off” to “on” ID 20, switch from “on” to “off” ID 21.\n\n" +
                   "Roller blind switch:\n" +
                   "Input S1, Turning on the light: switch from “off” to “on” ID 10, double click ID 14 (depends of parameter 15 value 1 - double click functionality), " +
                   "triple click ID 15, brighten ID 17, releasing ID 13.\n" +
                   "Input S2, Turning off the light: switch from “on” to “off” ID 11, triple click ID 25, dim ID 18, releasing ID 13."

        input name: "param20", type: "number", range: "101..170", defaultValue: "110", required: true,
            title: "ADVANCED FUNCTION\n\n" +
                   "20. The function enabling the change of control impulse length. " +
                   "This function will enable decreasing the minimum level of the Dimmer by extending the control impulse. " +
                   "By changing the minimum level, the user may completely dim LED bulbs. Not all LED bulbs available on the market have the dimming option.\n" +
                   "Available settings:\n" +
                   "Default values: 110 for 50Hz (UK), 101 for 60Hz (USA).\n\n"+
                   "WARNING!\nWrong setting of the function may cause incorrect operation of the Dimmer."
                   
        input name: "paramAssociationGroup1", type: "bool", defaultValue: false, required: true,
             title: "The Dimmer 1 provides the association of three groups.\n\n" +
                    "1 = 1st group is assigned to key no. 1,\n" +
                    "Default value: true"

        input name: "paramAssociationGroup2", type: "bool", defaultValue: false, required: true,
             title: "2nd group is assigned to key no. 2\n" +
                    "Default value: true"

        input name: "paramAssociationGroup3", type: "bool", defaultValue: true, required: true,
             title: "3rd group for controllers such Home Center for state reporting,\n" +
                    "Default value: false"
    }
}

def parse(String description) {
	def item1 = [
		canBeCurrentState: false,
		linkText: getLinkText(device),
		isStateChange: false,
		displayed: false,
		descriptionText: description,
		value:  description
	]
	def result
	def cmd = zwave.parse(description, [0x26: 1, 0x70: 2, 072: 2])
    //log.debug "cmd: ${cmd}"
    
    if (cmd) {
        result = createEvent(cmd, item1)
	}
	else {
		item1.displayed = displayed(description, item1.isStateChange)
		result = [item1]
	}
    
    if(result?.descriptionText)
		log.debug "Parse returned ${result?.descriptionText}"
        
	result

}

def createEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd, Map map) {
	log.debug( "Scene ID: $cmd.sceneId")
    log.debug( "Dimming Duration: $cmd.dimmingDuration")

    sendEvent(name: "scene", value: "$cmd.sceneId", data: [switchType: "$settings.param20"], descriptionText: "Scene id $cmd.sceneId was activated", isStateChange: true)
    log.debug( "Scene id $cmd.sceneId was activated" )
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStartLevelChange cmd, Map item1) {
	[]
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd, Map item1) {
	[response(zwave.basicV1.basicGet())]
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	result[0].descriptionText = "${item1.linkText} is ${item1.value}"
	result[0].handlerName = cmd.value ? "statusOn" : "statusOff"
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "digital"
	}
	result
}

def doCreateEvent(physicalgraph.zwave.Command cmd, Map item1) {
	def result = [item1]

	item1.name = "switch"
	item1.value = cmd.value ? "on" : "off"
	item1.handlerName = item1.value
	item1.descriptionText = "${item1.linkText} was turned ${item1.value}"
	item1.canBeCurrentState = true
	item1.isStateChange = isStateChange(device, item1.name, item1.value)
	item1.displayed = item1.isStateChange

	if (cmd.value >= 5) {
		def item2 = new LinkedHashMap(item1)
		item2.name = "level"
		item2.value = cmd.value as String
		item2.unit = "%"
		item2.descriptionText = "${item1.linkText} dimmed ${item2.value} %"
		item2.canBeCurrentState = true
		item2.isStateChange = isStateChange(device, item2.name, item2.value)
		item2.displayed = false
		result << item2
	}
	result
}

def createEvent(physicalgraph.zwave.Command cmd,  Map map) {
	// Handles any Z-Wave commands we aren't interested in
	log.debug "UNHANDLED COMMAND $cmd"
}

def on() {
	log.info "on"
	delayBetween([zwave.basicV1.basicSet(value: 0xFF).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def off() {
	delayBetween ([zwave.basicV1.basicSet(value: 0x00).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value) {
    def level = Math.min(value as Integer, 99)
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
    def level = Math.min(value as Integer, 99)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format()
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def createEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd, Map item1) {

	log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"

}

 def configureParams() {
	log.debug "Configuring ${device.displayName} parameters"
	def cmds = []
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: param1.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1, scaledConfigurationValue: param6.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: param7.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: param8.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, scaledConfigurationValue: param9.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, scaledConfigurationValue: param10.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1, scaledConfigurationValue: param11.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 13, size: 1, scaledConfigurationValue: param13.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 15, size: 1, scaledConfigurationValue: param15.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 16, size: 1, scaledConfigurationValue: param16.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 19, size: 1, scaledConfigurationValue: param19.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 20, size: 1, scaledConfigurationValue: param20.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 30, size: 1, scaledConfigurationValue: param30.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 39, size: 1, scaledConfigurationValue: param39.toInteger()).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 41, size: 1, scaledConfigurationValue: param41.toInteger()).format()

        // Register for Group 1
        if(paramAssociationGroup1) {
        	cmds << zwave.associationV2.associationSet(groupingIdentifier:1, nodeId: [zwaveHubNodeId]).format()
        }
        else {
        	cmds << zwave.associationV2.associationRemove(groupingIdentifier:1, nodeId: [zwaveHubNodeId]).format()
        }
        // Register for Group 2
        if(paramAssociationGroup2) {
        	cmds << zwave.associationV2.associationSet(groupingIdentifier:2, nodeId: [zwaveHubNodeId]).format()
        }
        else {
        	cmds << zwave.associationV2.associationRemove(groupingIdentifier:2, nodeId: [zwaveHubNodeId]).format()
        }
        // Register for Group 3
        if(paramAssociationGroup3) {
        	cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId: [zwaveHubNodeId]).format()
        }
        else {
        	cmds << zwave.associationV2.associationRemove(groupingIdentifier:3, nodeId: [zwaveHubNodeId]).format()
        }
        
	delayBetween(cmds, 500)
}

def updated() {
	log.debug "updated()"
	response(["delay 2000"] + configureParams() + refresh())
}