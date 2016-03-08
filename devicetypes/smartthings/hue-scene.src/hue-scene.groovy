/**
 *  Hue Scene
 *
 *  Author: CyrilPeponnet
 */
// for the UI
metadata {
    // Automatically generated. Make future change here.
    definition (name: "Hue Scene", namespace: "smartthings", author: "CyrilPeponnet") {
        capability "Actuator"
        capability "Switch"
        capability "Momentary"
        capability "Sensor"

        attribute "lights", "string"
        attribute "group", "string"
        attribute "offStateId", "string"

    }

    // simulator metadata
    simulator {
    }

    tiles (scale: 2){
        multiAttributeTile(name:"push", type: "momentary", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on",  label:'Push', action:"momentary.push", icon:"st.lights.philips.hue-multi", backgroundColor:"#07A4D2"
            }
            tileAttribute ("lights", key: "SECONDARY_CONTROL") {
                attributeState "default", label:'${currentValue}'
            }
        }
        standardTile("switch", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
            state "off", label:"", action:"switch.off", icon:"st.secondary.off"
        }

        main "push"
        details "push", "switch"
    }
}

def parse(String description) {
}

def push() {
    parent.pushScene(this, device.currentValue("group")?: 0)
    sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}

def on() {
    parent.pushScene(this, device.currentValue("group")?: 0)
    sendEvent(name: "switch", value: "on", isStateChange: true)
}

def off() {
    parent.pushScene(this, device.currentValue("group")?: 0, device.currentValue("offStateId")?: null)
    sendEvent(name: "switch", value: "off", isStateChange: true)
}