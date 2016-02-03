/**
 *  Alarm Control Center
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
    definition (name: "Alarm Control Center", author: "Jordan <jordan@xeron.cc>") {
        capability "Switch"
        
        command "awayswitch"
        command "disarmswitch"
        command "stayswitch"
        command "alarm"
        command "away"
        command "disarm"
        command "entrydelay"
        command "exitdelay"
        command "notready"
        command "ready"
        command "stay"
    }

    // simulator metadata
    simulator {
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "alarm", label:'Alarm', icon:"st.security.alarm.off", backgroundColor:"#ff0000"
                attributeState "arm", label:'Armed Away', icon:"st.security.alarm.on", backgroundColor:"#800000"
                attributeState "disarm", label:'Disarmed', icon:"st.security.alarm.off", backgroundColor:"#79b821"
                attributeState "entrydelay", label:'Entry Delay', icon:"st.security.alarm.on", backgroundColor:"#ff9900"
                attributeState "exitdelay", label:'Exit Delay', icon:"st.security.alarm.on", backgroundColor:"#ff9900"
                attributeState "notready", label:'Open', icon:"st.security.alarm.off", backgroundColor:"#ffcc00"
                attributeState "ready", label:'Ready', icon:"st.security.alarm.off", backgroundColor:"#79b821"
                attributeState "stay", label:'Armed Stay', icon:"st.security.alarm.on", backgroundColor:"#008CC1"
            }
        }
        standardTile("disarm", "capability.momentary", width: 2, height: 2, title: "Disarm", required: true, multiple: false){
            state "disarm", label: 'Disarm', action: "disarmswitch", icon: "st.Home.home4", backgroundColor: "#79b821"
        }
        standardTile("away", "capability.momentary", width: 2, height: 2, title: "Armed Away", required: true, multiple: false){
            state "away", label: 'Arm Away', action: "awayswitch", icon: "st.Home.home4", backgroundColor: "#800000"
        }
        standardTile("stay", "capability.momentary", width: 2, height: 2, title: "Armed Stay", required: true, multiple: false){
            state "stay", label: 'Arm Stay', action: "stayswitch", icon: "st.Home.home4", backgroundColor: "#008CC1"
        }

        main (["status", "away", "stay", "disarm"])
        details(["status", "away", "stay", "disarm"])

    }
}

def parse(String description) {
}

def stayswitch() {
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

def awayswitch() {
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
}

def off() {
}

def disarmswitch() {
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

def away() {
    sendEvent (name: "switch", value: "away")
}

def stay() {
    sendEvent (name: "switch", value: "stay")
}

def disarm() {
    sendEvent (name: "switch", value: "disarm")
}

def exitdelay() {
    sendEvent (name: "switch", value: "exitdelay")
}

def entrydelay() {
    sendEvent (name: "switch", value: "entrydelay")
}

def notready() {
    sendEvent (name: "switch", value: "notready")
}

def ready() {
    sendEvent (name: "switch", value: "ready")
}

def alarm() {
    sendEvent (name: "switch", value: "alarm")
}
