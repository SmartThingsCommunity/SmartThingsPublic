 /**
 *  Inovelli Switch NZW30/NZW30T w/Scene
 *  Author: Eric Maycock (erocm123)
 *  Date: 2018-12-04
 *
 *  Copyright 2018 Eric Maycock
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
 *  2018-12-04: Added option to "Disable Remote Control" and to send button events 1,pushed / 1,held for on / off.
 *
 *  2018-08-03: Added the ability to change the label on scenes.
 *
 *  2018-06-20: Modified tile layout. Update firmware version reporting. Bug Fix.
 * 
 *  2018-06-08: Remove communication method check from updated().
 * 
 *  2018-04-11: No longer deleting child devices when user toggles the option off. SmartThings was throwing errors.
 *              User will have to manually delete them.
 *
 *  2018-03-08: Added support for local protection to disable local control. Requires firmware 1.03+.
 *              Also merging handler from NZW30T as they are identical other than the LED indicator.
 *              Child device creation option added for local control setting. Child device must be installed:
 *              https://github.com/erocm123/SmartThingsPublic/blob/master/devicetypes/erocm123/switch-level-child-device.src
 *       
 *  2018-02-26: Added support for Z-Wave Association Tool SmartApp. Associations require firmware 1.02+.
 *              https://github.com/erocm123/SmartThingsPublic/tree/master/smartapps/erocm123/z-waveat
 */
 
metadata {
	definition (name: "Inovelli Switch NZW30 w/Scene", namespace: "erocm123", author: "Eric Maycock", vid: "generic-switch") {
		capability "Switch"
		capability "Refresh"
		capability "Polling"
		capability "Actuator"
		capability "Sensor"
        //capability "Health Check"
        capability "Button"
        capability "Holdable Button"
        capability "Configuration"
        
        attribute "lastActivity", "String"
        attribute "lastEvent", "String"
        attribute "firmware", "String"
        
        command "pressUpX1"
        command "pressDownX1"
        command "pressUpX2"
        command "pressDownX2"
        command "pressUpX3"
        command "pressDownX3"
        command "pressUpX4"
        command "pressDownX4"
        command "pressUpX5"
        command "pressDownX5"
        command "holdUp"
        command "holdDown"
        
        command "setAssociationGroup", ["number", "enum", "number", "number"] // group number, nodes, action (0 - remove, 1 - add), multi-channel endpoint (optional)

		fingerprint mfr: "015D", prod: "B111", model: "1E1C", deviceJoinName: "Inovelli Switch"
        fingerprint mfr: "015D", prod: "1E00", model: "1E00", deviceJoinName: "Inovelli Switch"
        fingerprint mfr: "0312", prod: "1E00", model: "1E00", deviceJoinName: "Inovelli Switch"
        fingerprint mfr: "0312", prod: "1E02", model: "1E02", deviceJoinName: "Inovelli Switch" // Toggle version NZW30T
        fingerprint deviceId: "0x1001", inClusters: "0x5E,0x86,0x72,0x5A,0x85,0x59,0x73,0x25,0x27,0x70,0x5B,0x75,0x22,0x8E,0x55,0x6C,0x7A"
	}

	simulator {
	}
    
    preferences {
        input "autoOff", "number", title: "Auto Off\n\nAutomatically turn switch off after this number of seconds\nRange: 0 to 32767", description: "Tap to set", required: false, range: "0..32767", defaultValue: "0"
        input "ledIndicator", "enum", title: "LED Indicator\n\nTurn LED indicator on when light is: (Paddle Switch Only)\n", description: "Tap to set", required: false, options:[["1": "On"], ["0": "Off"], ["2": "Disable"], ["3": "Always On"]], defaultValue: "0"
        input "invert", "enum", title: "Invert Switch\n\nInvert on & off on the physical switch", description: "Tap to set", required: false, options:[["0": "No"], ["1": "Yes"]], defaultValue: "0"
        input "disableLocal", "enum", title: "Disable Local Control\n\nDisable ability to control switch from the wall\n(Firmware 1.02+)", description: "Tap to set", required: false, options:[["2": "Yes"], ["0": "No"]], defaultValue: "0"
        input "disableRemote", "enum", title: "Disable Remote Control\n\nDisable ability to control switch from inside SmartThings", description: "Tap to set", required: false, options:[["2": "Yes"], ["0": "No"]], defaultValue: "0"
        input "buttonOn", "enum", title: "Send Button Event On\n\nSend the button 1 pushed event when switch turned on from inside SmartThings", description: "Tap to set", required: false, options:[["1": "Yes"], ["0": "No"]], defaultValue: "0"
        input "buttonOff", "enum", title: "Send Button Event Off\n\nSend the button 1 held event when switch turned off from inside SmartThings", description: "Tap to set", required: false, options:[["1": "Yes"], ["0": "No"]], defaultValue: "0"
        input description: "Use the below options to enable child devices for the specified settings. This will allow you to adjust these settings using SmartApps such as Smart Lighting. If any of the options are enabled, make sure you have the appropriate child device handlers installed.\n(Firmware 1.02+)", title: "Child Devices", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input "enableDisableLocalChild", "bool", title: "Disable Local Control", description: "", required: false
        input description: "1 pushed - Up 1x click\n2 pushed - Up 2x click\n3 pushed - Up 3x click\n4 pushed - Up 4x click\n5 pushed - Up 5x click\n6 pushed - Up held\n\n1 held - Down 1x click\n2 held - Down 2x click\n3 held - Down 3x click\n4 held - Down 4x click\n5 held - Down 5x click\n6 held - Down held", title: "Button Mappings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input description: "Use the \"Z-Wave Association Tool\" SmartApp to set device associations. (Firmware 1.02+)\n\nGroup 2: Sends on/off commands to associated devices when switch is pressed (BASIC_SET).", title: "Associations", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input description: "Below you can change the labels that appear for the various tap sequences.", title: "Scene Labels", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input "pressUpX1Label", "text", title: "Label for \"Tap ▲\"", description: "Tap to set", required: false
        input "pressDownX1Label", "text", title: "Label for \"Tap ▼\"", description: "Tap to set", required: false
        input "pressUpX2Label", "text", title: "Label for \"Tap ▲▲\"", description: "Tap to set", required: false
        input "pressDownX2Label", "text", title: "Label for \"Tap ▼▼\"", description: "Tap to set", required: false
        input "pressUpX3Label", "text", title: "Label for \"Tap ▲▲▲\"", description: "Tap to set", required: false
        input "pressDownX3Label", "text", title: "Label for \"Tap ▼▼▼\"", description: "Tap to set", required: false
        input "pressUpX4Label", "text", title: "Label for \"Tap ▲▲▲▲\"", description: "Tap to set", required: false
        input "pressDownX4Label", "text", title: "Label for \"Tap ▼▼▼▼\"", description: "Tap to set", required: false
        input "pressUpX5Label", "text", title: "Label for \"Tap ▲▲▲▲▲\"", description: "Tap to set", required: false
        input "pressDownX5Label", "text", title: "Label for \"Tap ▼▼▼▼▼\"", description: "Tap to set", required: false
        input "pressHoldUpLabel", "text", title: "Label for \"Hold ▲\"", description: "Tap to set", required: false
        input "pressHoldDownLabel", "text", title: "Label for \"Hold ▼\"", description: "Tap to set", required: false
    }
    
    tiles {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
            }
            tileAttribute("device.lastEvent", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'${currentValue}',icon: "st.unknown.zwave.remote-controller")
            }
        }
                
        valueTile("lastActivity", "device.lastActivity", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label: 'Last Activity: ${currentValue}',icon: "st.Health & Wellness.health9"
        }
        
        valueTile("firmware", "device.firmware", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: 'fw: ${currentValue}', icon: ""
        }
        
        valueTile("info", "device.info", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", label: 'Tap on the buttons below to test scenes (ie: Tap ▲ 1x, ▲▲ 2x, etc depending on the button)'
        }
        
        valueTile("icon", "device.icon", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: '', icon: "https://inovelli.com/wp-content/uploads/Device-Handler/Inovelli-Device-Handler-Logo.png"
        }
        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        
        standardTile("pressUpX1", "device.pressUpX1", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "pressUpX1"
        }
        
        standardTile("pressUpX2", "device.pressUpX2", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "pressUpX2"
        }
        
        standardTile("pressUpX3", "device.pressUpX3", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "pressUpX3"
        }
        
        standardTile("pressDownX1", "device.pressDownX1", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "pressDownX1"
        }
        
        standardTile("pressDownX2", "device.pressDownX2", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "pressDownX2"
        }
        
        standardTile("pressDownX3", "device.pressDownX3", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "pressDownX3"
        }
        
        standardTile("pressUpX4", "device.pressUpX4", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "pressUpX4"
        }
        
        standardTile("pressUpX5", "device.pressUpX5", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "pressUpX5"
        }
        
        standardTile("holdUp", "device.holdUp", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "holdUp"
		}
        
        standardTile("pressDownX4", "device.pressDownX4", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "pressDownX4"
        }
        
        standardTile("pressDownX5", "device.pressDownX5", width: 2, height: 1, decoration: "flat") {
            state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "pressDownX5"
        }
        
        standardTile("holdDown", "device.holdDown", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "holdDown"
		}
    }
}

private channelNumber(String dni) {
    dni.split("-ep")[-1] as Integer
}

private sendAlert(data) {
    sendEvent(
        descriptionText: data.message,
        eventType: "ALERT",
        name: "failedOperation",
        value: "failed",
        displayed: true,
    )
}

void childSetLevel(String dni, value) {
    def valueaux = value as Integer
    def level = Math.max(Math.min(valueaux, 99), 0)    
    def cmds = []
    switch (channelNumber(dni)) {
        case 101:
            cmds << new physicalgraph.device.HubAction(command(zwave.protectionV2.protectionSet(localProtectionState : level > 0 ? 2 : 0, rfProtectionState: 0) ))
            cmds << new physicalgraph.device.HubAction(command(zwave.protectionV2.protectionGet() ))
        break
    }
	sendHubCommand(cmds, 1000)
}

void childOn(String dni) {
    log.debug "childOn($dni)"
    childSetLevel(dni, 99)
}

void childOff(String dni) {
    log.debug "childOff($dni)"
    childSetLevel(dni, 0)
}

void childRefresh(String dni) {
    log.debug "childRefresh($dni)"
}

def childExists(ep) {
    def children = childDevices
    def childDevice = children.find{it.deviceNetworkId.endsWith(ep)}
    if (childDevice) 
        return true
    else
        return false
}

def installed() {
    log.debug "installed()"
    refresh()
}

def configure() {
    log.debug "configure()"
    def cmds = initialize()
    commands(cmds)
}

def updated() {
    if (!state.lastRan || now() >= state.lastRan + 2000) {
        log.debug "updated()"
        state.lastRan = now()
        def cmds = initialize()
        response(commands(cmds))
    } else {
        log.debug "updated() ran within the last 2 seconds. Skipping execution."
    }
}


def initialize() {
    sendEvent(name: "checkInterval", value: 3 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    sendEvent(name: "numberOfButtons", value: 6, displayed: true)
    if (enableDisableLocalChild && !childExists("ep101")) {
    try {
        addChildDevice("Switch Level Child Device", "${device.deviceNetworkId}-ep101", null,
                [completedSetup: true, label: "${device.displayName} (Disable Local Control)",
                isComponent: true, componentName: "ep101", componentLabel: "Disable Local Control"])
    } catch (e) {
        runIn(3, "sendAlert", [data: [message: "Child device creation failed. Make sure the device handler for \"Switch Level Child Device\" is installed"]])
    }
    } else if (!enableDisableLocalChild && childExists("ep101")) {
        log.debug "Trying to delete child device ep101. If this fails it is likely that there is a SmartApp using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
        try {
            log.debug "SmartThings has issues trying to delete the child device when it is in use. Need to manually delete them."
            //if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
        } catch (e) {
            runIn(3, "sendAlert", [data: [message: "Failed to delete child device. Make sure the device is not in use by any SmartApp."]])
        }
    }
    if (device.label != state.oldLabel) {
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("e101")}
        if (childDevice)
        childDevice.setLabel("${device.displayName} (Disable Local Control)")
        state.oldLabel = device.label
    }
    
    sendEvent([name:"pressUpX1", value:pressUpX1Label? "${pressUpX1Label} ▲" : "Tap ▲", displayed: false])
    sendEvent([name:"pressDownX1", value:pressDownX1Label? "${pressDownX1Label} ▼" : "Tap ▼", displayed: false])
    sendEvent([name:"pressUpX2", value:pressUpX2Label? "${pressUpX2Label} ▲▲" : "Tap ▲▲", displayed: false])
    sendEvent([name:"pressDownX2", value:pressDownX2Label? "${pressDownX2Label} ▼▼" : "Tap ▼▼", displayed: false])
    sendEvent([name:"pressUpX3", value:pressUpX3Label? "${pressUpX3Label} ▲▲▲" : "Tap ▲▲▲", displayed: false])
    sendEvent([name:"pressDownX3", value:pressDownX3Label? "${pressDownX3Label} ▼▼▼" : "Tap ▼▼▼", displayed: false])
    sendEvent([name:"pressUpX4", value:pressUpX4Label? "${pressUpX4Label} ▲▲▲▲" : "Tap ▲▲▲▲", displayed: false])
    sendEvent([name:"pressDownX4", value:pressDownX4Label? "${pressDownX4Label} ▼▼▼▼" : "Tap ▼▼▼▼", displayed: false])
    sendEvent([name:"pressUpX5", value:pressUpX5Label? "${pressUpX5Label} ▲▲▲▲▲" : "Tap ▲▲▲▲▲", displayed: false])
    sendEvent([name:"pressDownX5", value:pressDownX5Label? "${pressDownX5Label} ▼▼▼▼▼" : "Tap ▼▼▼▼▼", displayed: false])
    sendEvent([name:"holdUp", value:pressHoldUpLabel? "${pressHoldUpLabel} ▲" : "Hold ▲", displayed: false])
    sendEvent([name:"holdDown", value:pressHoldDownLabel? "${pressHoldDownLabel} ▼" : "Hold ▼", displayed: false])
    
    def cmds = processAssociations()
    cmds << zwave.versionV1.versionGet()
    cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: ledIndicator!=null? ledIndicator.toInteger() : 0, parameterNumber: 3, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 3)
    cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: invert!=null? invert.toInteger() : 0, parameterNumber: 4, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4)
    cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: autoOff!=null? autoOff.toInteger() : 0, parameterNumber: 5, size: 2)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 5)

	if (state.disableLocal != settings.disableLocal) {
        cmds << zwave.protectionV2.protectionSet(localProtectionState : disableLocal!=null? disableLocal.toInteger() : 0, rfProtectionState: 0)
        cmds << zwave.protectionV2.protectionGet()
    }
    
    state.disableLocal = settings.disableLocal
    return cmds
}

def parse(description) {
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent(descriptionText: description, isStateChange: true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x70: 1, 0x98: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
			//log.debug("'$description' parsed to $result")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
    def now
    if(location.timeZone)
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    else
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a")
    sendEvent(name: "lastActivity", value: now, displayed:false)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 1])
	if (encapsulatedCommand) {
		state.sec = 1
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    switch (cmd.keyAttributes) {
       case 0:
       createEvent(buttonEvent(cmd.keyAttributes + 1, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
       case 1:
       createEvent(buttonEvent(6, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
       case 2:
       null
       break
       default:
       createEvent(buttonEvent(cmd.keyAttributes - 1, (cmd.sceneNumber == 2? "pushed" : "held"), "physical"))
       break
    }
}

def buttonEvent(button, value, type = "digital") {
    if(button != 6)
        sendEvent(name:"lastEvent", value: "${value != 'pushed'?' Tap '.padRight(button+5, '▼'):' Tap '.padRight(button+5, '▲')}", displayed:false)
    else
        sendEvent(name:"lastEvent", value: "${value != 'pushed'?' Hold ▼':' Hold ▲'}", displayed:false)
	[name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true, type: type]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
}

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

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled: $cmd"
	null
}

def on() {
    if (buttonOn == "1") {
        sendEvent([name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$device.displayName button 1 was pushed", isStateChange: true, type: "digital"])
    }
    if (disableRemote != "2") {
    commands([
		zwave.basicV1.basicSet(value: 0xFF),
		zwave.basicV1.basicGet()
	])
    } else {
    commands([
        zwave.basicV1.basicGet()
    ])
    }
}

def off() {
    if (buttonOff == "1") {
        sendEvent([name: "button", value: "held", data: [buttonNumber: 1], descriptionText: "$device.displayName button 1 was held", isStateChange: true, type: "digital"])
    }
    if (disableRemote != "2") {
    commands([
		zwave.basicV1.basicSet(value: 0x00),
		zwave.basicV1.basicGet()
	])
    } else {
    commands([
        zwave.basicV1.basicGet()
    ])
    }
}


def ping() {
    refresh()
}

def poll() {
	refresh()
}

def refresh() {
	commands(zwave.basicV1.basicGet())
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=500) {
	delayBetween(commands.collect{ command(it) }, delay)
}

def pressUpX1() {
	sendEvent(buttonEvent(1, "pushed"))
}

def pressDownX1() {
	sendEvent(buttonEvent(1, "held"))
}

def pressUpX2() {
	sendEvent(buttonEvent(2, "pushed"))
}

def pressDownX2() {
	sendEvent(buttonEvent(2, "held"))
}

def pressUpX3() {
	sendEvent(buttonEvent(3, "pushed"))
}

def pressDownX3() {
	sendEvent(buttonEvent(3, "held"))
}

def pressUpX4() {
	sendEvent(buttonEvent(4, "pushed"))
}

def pressDownX4() {
	sendEvent(buttonEvent(4, "held"))
}

def pressUpX5() {
	sendEvent(buttonEvent(5, "pushed"))
}

def pressDownX5() {
	sendEvent(buttonEvent(5, "held"))
}

def holdUp() {
	sendEvent(buttonEvent(6, "pushed"))
}

def holdDown() {
	sendEvent(buttonEvent(6, "held"))
}

def setDefaultAssociations() {
    def smartThingsHubID = (zwaveHubNodeId.toString().format( '%02x', zwaveHubNodeId )).toUpperCase()
    state.defaultG1 = [smartThingsHubID]
    state.defaultG2 = []
}

def setAssociationGroup(group, nodes, action, endpoint = null){
    if (!state."desiredAssociation${group}") {
        state."desiredAssociation${group}" = nodes
    } else {
        switch (action) {
            case 0:
                state."desiredAssociation${group}" = state."desiredAssociation${group}" - nodes
            break
            case 1:
                state."desiredAssociation${group}" = state."desiredAssociation${group}" + nodes
            break
        }
    }
}

def processAssociations(){
   def cmds = []
   setDefaultAssociations()
   def associationGroups = 5
   if (state.associationGroups) {
       associationGroups = state.associationGroups
   } else {
       log.debug "Getting supported association groups from device"
       cmds <<  zwave.associationV2.associationGroupingsGet()
   }
   for (int i = 1; i <= associationGroups; i++){
      if(state."actualAssociation${i}" != null){
         if(state."desiredAssociation${i}" != null || state."defaultG${i}") {
            def refreshGroup = false
            ((state."desiredAssociation${i}"? state."desiredAssociation${i}" : [] + state."defaultG${i}") - state."actualAssociation${i}").each {
                log.debug "Adding node $it to group $i"
                cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                refreshGroup = true
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                log.debug "Removing node $it from group $i"
                cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                refreshGroup = true
            }
            if (refreshGroup == true) cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
            else log.debug "There are no association actions to complete for group $i"
         }
      } else {
         log.debug "Association info not known for group $i. Requesting info from device."
         cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
      }
   }
   return cmds
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    def temp = []
    if (cmd.nodeId != []) {
       cmd.nodeId.each {
          temp += it.toString().format( '%02x', it.toInteger() ).toUpperCase()
       }
    } 
    state."actualAssociation${cmd.groupingIdentifier}" = temp
    log.debug "Associations for Group ${cmd.groupingIdentifier}: ${temp}"
    updateDataValue("associationGroup${cmd.groupingIdentifier}", "$temp")
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    sendEvent(name: "groups", value: cmd.supportedGroupings)
    log.debug "Supported association groups: ${cmd.supportedGroupings}"
    state.associationGroups = cmd.supportedGroupings
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.debug cmd
    if(cmd.applicationVersion && cmd.applicationSubVersion) {
	    def firmware = "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2,'0')}"
        state.needfwUpdate = "false"
        createEvent(name: "firmware", value: "${firmware}")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.protectionv2.ProtectionReport cmd) {
    log.debug cmd
    def integerValue = cmd.localProtectionState
    def children = childDevices
    def childDevice = children.find{it.deviceNetworkId.endsWith("ep101")}
    if (childDevice) {
        childDevice.sendEvent(name: "switch", value: integerValue > 0 ? "on" : "off")        
    }
}
