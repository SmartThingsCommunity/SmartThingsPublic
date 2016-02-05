/**
 *  DSC Away Panel
 *
 *  Author: Jordan <jordan@xeron.cc
 *  Original Code By: Rob Fisher <robfish@att.net>, Carlos Santiago <carloss66@gmail.com>, JTT <aesystems@gmail.com>
 *  Date: 2016-02-03
 */
 // for the UI

preferences {
    input("ip", "text", title: "IP", description: "The IP of your alarmserver")
    input("port", "text", title: "Port", description: "The port")
} 
 
metadata {
    // Automatically generated. Make future change here.
    definition (name: "DSC Away Panel", author: "Jordan <jordan@xeron.cc>") {
        capability "Switch"
        
        command "away"
        command "disarm"
        command "partition"
        command "stay"
    }

    // simulator metadata
    simulator {
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "alarm", label:'Alarm', action: 'disarm', icon:"st.security.alarm.off", backgroundColor:"#ff0000"
                attributeState "away", label:'Armed Away', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#800000"
                attributeState "disarm", label:'Disarmed', icon:"st.security.alarm.off", backgroundColor:"#79b821"
                attributeState "entrydelay", label:'Entry Delay', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#ff9900"
                attributeState "exitdelay", label:'Exit Delay', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#ff9900"
                attributeState "notready", label:'Not Ready', icon:"st.security.alarm.off", backgroundColor:"#ffcc00"
                attributeState "ready", label:'Ready', action: 'away', icon:"st.security.alarm.off", backgroundColor:"#79b821"
                attributeState "forceready", label:'Force Ready', action: 'away', icon:"st.security.alarm.off", backgroundColor:"#79b821"
                attributeState "stay", label:'Armed Stay', action: 'away', icon:"st.security.alarm.on", backgroundColor:"#008CC1"
                attributeState "instantaway", label:'Armed Instant Away', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#800000"
                attributeState "instantstay", label:'Armed Instant Stay', action: 'away', icon:"st.security.alarm.on", backgroundColor:"#008CC1"
            }
        }
        standardTile("disarm", "capability.momentary", width: 2, height: 2, title: "Disarm", required: true, multiple: false){
            state "disarm", label: 'Disarm', action: "disarm", icon: "st.Home.home4", backgroundColor: "#79b821"
        }
        standardTile("away", "capability.momentary", width: 2, height: 2, title: "Armed Away", required: true, multiple: false){
            state "away", label: 'Arm Away', action: "away", icon: "st.Home.home4", backgroundColor: "#800000"
        }
        standardTile("stay", "capability.momentary", width: 2, height: 2, title: "Armed Stay", required: true, multiple: false){
            state "stay", label: 'Arm Stay', action: "stay", icon: "st.Home.home4", backgroundColor: "#008CC1"
        }

        main (["status", "away", "stay", "disarm"])
        details(["status", "away", "stay", "disarm"])

    }
}

def parse(String description) {
}

def partition(String state, String partition) {
    // state will be a valid state for the panel (ready, notready, armed, etc)
    // partition will be a partition number, for most users this will always be 1

    log.debug "Partition: ${state} for partition: ${partition}"
    sendEvent (name: "switch", value: "${state}")
}

def stay() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/alarm/stayarm",
        headers: [
            HOST: "$ip:$port"
            //HOST: getHostAddress()
        ]
    )
    log.debug "response" : "Request to stay arm received"
    //log.debug "stay"
    //sendEvent (name: "switch", value: "stay")
    return result
}

def away() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/alarm/arm",
        headers: [
            HOST: "$ip:$port"
            //HOST: getHostAddress()
        ]
    )
    log.debug "response" : "Request to away arm received"
    //log.debug "away"
    //sendEvent (name: "switch", value: "away")
    return result
}


def on() {
    away()
}

def off() {
    disarm()
}

def disarm() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/alarm/disarm",
        headers: [
            HOST: "$ip:$port"
            //HOST: getHostAddress()
        ]
    )
    log.debug "response" : "Request to disarm received"
    //log.debug "disarm"
    //sendEvent (name: "switch", value: "disarm")
    return result
}
