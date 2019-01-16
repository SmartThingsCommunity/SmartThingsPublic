import groovy.json.JsonOutput

/**
 *  Copyright 2018 SmartThings
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
    definition (name: "Aeotec Wallmote", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "SmartThings-smartthings-Aeon_Key_Fob_Components") {
        capability "Actuator"
        capability "Button"
        capability "Battery"
        capability "Holdable Button"
        capability "Configuration"
        capability "Sensor"
        capability "Health Check"

        fingerprint mfr: "0086", model: "0082", deviceJoinName: "Aeotec Wallmote Quad"
        fingerprint mfr: "0086", model: "0081", deviceJoinName: "Aeotec Wallmote"
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "rich-control", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.button", key: "PRIMARY_CONTROL") {
                attributeState "default", label: ' ', action: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
            }
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "battery", label:'${currentValue}% battery', unit:""
        }

        main("rich-control")
        details(["rich-control", childDeviceTiles("endpoints"), "battery"])
    }
}

def getNumberOfButtons() {
    def modelToButtons = ["0082" : 4, "0081": 2]
    return modelToButtons[zwaveInfo.model] ?: 1
}

def installed() {
    sendEvent(name: "numberOfButtons", value: numberOfButtons)
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "zwave", scheme:"untracked"].encodeAsJson(), displayed: false)
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1])
    createChildDevices()
    response([
            secure(zwave.batteryV1.batteryGet()),
            "delay 2000",
            secure(zwave.wakeUpV2.wakeUpNoMoreInformation())
    ])
}

def updated() {
    if (!childDevices) {
        createChildDevices()
    }
    else if (device.label != state.oldLabel) {
        childDevices.each {
            def segs = it.deviceNetworkId.split(":")
            def newLabel = "${device.displayName} button ${segs[-1]}"
            it.setLabel(newLabel)
        }
        state.oldLabel = device.label
    }
}

def configure() {

}

def parse(String description) {
    def results = []
    if (description.startsWith("Err")) {
        results = createEvent(descriptionText:description, displayed:true)
    } else {
        def cmd = zwave.parse(description)
        if(cmd) results += zwaveEvent(cmd)
        if(!results) results = [ descriptionText: cmd, displayed: false ]
    }
    return results
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    def button = cmd.sceneNumber
    def value
    // 0 = pushed, 1 = held down, 2 = released
    if (cmd.keyAttributes < 2) {
        value = cmd.keyAttributes == 2 ? "held" : "pushed"
    } else {
        // we can't do anything with the held down event yet
        return []
    }
    def child = getChildDevice(button)
    child?.sendEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$child.displayName was $value", isStateChange: true)
    createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand()
    if (encapsulatedCommand) {
        return zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
        createEvent(descriptionText: cmd.toString())
    }
}

/*
The device supports multichannel
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep) {
    def encapsulatedCommand = cmd.encapsulatedCommand()
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
    }
}
*/

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    def results = []
    results += createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
    if (!state.lastbatt || (now() - state.lastbatt) >= 56*60*60*1000) {
        results += response([
                secure(zwave.batteryV1.batteryGet()),
                "delay 2000",
                secure(zwave.wakeUpV2.wakeUpNoMoreInformation())
        ])
    } else {
        results += response(secure(zwave.wakeUpV2.wakeUpNoMoreInformation()))
    }
    results
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%", isStateChange: true ]
    state.lastbatt = now()
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "$device.displayName battery is low!"
    } else {
        map.value = cmd.batteryLevel
    }
    createEvent(map)
}

def createChildDevices() {
    state.oldLabel = device.label
    def child
    for (i in 1..numberOfButtons) {
        child = addChildDevice("Child Button", "${device.deviceNetworkId}:${i}", device.hubId,
                [completedSetup: true, label: "${device.displayName} button ${i}",
                 isComponent: true, componentName: "button$i", componentLabel: "Button $i"])
        child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: i], descriptionText: "$child.displayName was pushed", isStateChange: true)
    }
}

def secure(cmd) {
    if(zwaveInfo.zw.endsWith("s")) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

def getChildDevice(button) {
    String childDni = "${device.deviceNetworkId}:${button}"
    def child = childDevices.find{it.deviceNetworkId == childDni}
    if (!child) {
        log.error "Child device $childDni not found"
    }
    return child
}