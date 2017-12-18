/**
 *  Inovelli Dimmer Smart Plug NZW39 w/Scene
 *  Author: Eric Maycock (erocm123)
 *  Date: 2017-10-18
 *
 *  Copyright 2017 Eric Maycock
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
    definition (name: "Inovelli Dimmer Smart Plug NZW39 w/Scene", namespace: "erocm123", author: "Eric Maycock") {
        capability "Switch"
        capability "Refresh"
        capability "Polling"
        capability "Actuator"
        capability "Sensor"
        capability "Health Check"
        capability "Button"
        capability "Indicator"
        capability "Switch Level"
        
        attribute "lastActivity", "String"
        attribute "lastEvent", "String"

        command "pressUpX2"

        fingerprint mfr: "015D", prod: "2700", model: "2700", deviceJoinName: "Inovelli Dimmer Smart Plug"
        fingerprint mfr: "0312", prod: "2700", model: "2700", deviceJoinName: "Inovelli Dimmer Smart Plug"
    }

    simulator {
    }
    
    preferences {
        input "autoOff", "number", title: "Auto Off\n\nAutomatically turn switch off after this number of seconds\nRange: 0 to 32767", description: "", required: false, range: "0..32767"
        input "ledIndicator", "enum", title: "LED Indicator\n\nTurn LED indicator on when light is:\n", description: "Tap to select", required: false, options:[1: "On", 0: "Off", 2: "Disable"], defaultValue: 1     
        input description: "1 pushed - Button 2x click", title: "Button Mappings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    }
    
    tiles {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute("device.lastEvent", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}',icon: "st.unknown.zwave.remote-controller")
            }
        }
        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        
        standardTile("pressUpX2", "device.button", width: 4, height: 1, decoration: "flat") {
            state "default", label: "Tap ▲▲", backgroundColor: "#ffffff", action: "pressUpX2"
        }
        
        valueTile("lastActivity", "device.lastActivity", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label: 'Last Activity: ${currentValue}',icon: "st.Health & Wellness.health9"
        }
        
        valueTile("info", "device.info", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", label: 'Tap on the ▲▲ button above to test your scene'
        }
        
        valueTile("icon", "device.icon", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", label: '', icon: "https://inovelli.com/wp-content/uploads/Device-Handler/Inovelli-Device-Handler-Logo.png"
        }
    }
}

def installed() {
    refresh()
}

def updated() {
    sendEvent(name: "checkInterval", value: 3 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    sendEvent(name: "numberOfButtons", value: 6, displayed: true)
    def cmds = []
    cmds << zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId)
    cmds << zwave.associationV2.associationGet(groupingIdentifier: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [ledIndicator? ledIndicator.toInteger() : 1], parameterNumber: 1, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 1)
    cmds << zwave.configurationV1.configurationSet(scaledConfigurationValue: autoOff? autoOff.toInteger() : 0, parameterNumber: 2, size: 2)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 2)
    response(commands(cmds))
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
            log.debug("'$description' parsed to $result")
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
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
    def value = (cmd.value ? "on" : "off")
    def result = [createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")]
    if (cmd.value) {
        result << createEvent(name: "level", value: cmd.value, unit: "%")
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 1])
    if (encapsulatedCommand) {
        state.sec = 1
        zwaveEvent(encapsulatedCommand)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    createEvent(buttonEvent(cmd.sceneNumber, (cmd.sceneNumber == 2? "held" : "pushed"), "physical"))
}

def buttonEvent(button, value, type = "digital") {
    sendEvent(name:"lastEvent", value: "${value != 'pushed'?' Tap '.padRight(button+1+5, '▼'):' Tap '.padRight(button+1+5, '▲')}", displayed:false)
    [name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true, type: type]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug "Unhandled: $cmd"
    null
}

def on() {
    commands([
        zwave.basicV1.basicSet(value: 0xFF),
        zwave.switchMultilevelV1.switchMultilevelGet()
    ])
}

def off() {
    commands([
        zwave.basicV1.basicSet(value: 0x00),
        zwave.switchMultilevelV1.switchMultilevelGet()
    ])
}

def setLevel(value) {
    commands([
        zwave.basicV1.basicSet(value: value < 100 ? value : 99),
        zwave.switchMultilevelV1.switchMultilevelGet()
    ])
}

def setLevel(value, duration) {
    def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
        commands([
            zwave.switchMultilevelV2.switchMultilevelSet(value: value < 100 ? value : 99, dimmingDuration: dimmingDuration),
            zwave.switchMultilevelV1.switchMultilevelGet()
    ])
}

def ping() {
    log.debug "ping()"
    refresh()
}

def poll() {
    log.debug "poll()"
    refresh()
}

def refresh() {
    log.debug "refresh()"
    commands([zwave.switchBinaryV1.switchBinaryGet(),
              zwave.switchMultilevelV1.switchMultilevelGet()
    ])
}

private command(physicalgraph.zwave.Command cmd) {
    if (state.sec != 0) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay=500) {
    delayBetween(commands.collect{ command(it) }, delay)
}

def pressUpX2() {
    sendEvent(buttonEvent(2, "pushed"))
}
