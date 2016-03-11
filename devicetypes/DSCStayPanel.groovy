/**
 *  DSC Stay Panel
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
    definition (name: "DSC Stay Panel", author: "Jordan <jordan@xeron.cc>") {
        capability "Switch"
        
        command "away"
        command "chime"
        command "disarm"
        command "instant"
        command "night"
        command "partition"
        command "reset"
        command "stay"
    }

    // simulator metadata
    simulator {
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"status", type: "lighting", width: 6, height: 4){
            tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
                attributeState "alarm", label:'Alarm', action: 'disarm', icon:"st.security.alarm.off", backgroundColor:"#ff0000"
                attributeState "away", label:'Armed Away', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#800000"
                attributeState "disarm", label:'Disarmed', icon:"st.security.alarm.off", backgroundColor:"#79b821"
                attributeState "entrydelay", label:'Entry Delay', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#ff9900"
                attributeState "exitdelay", label:'Exit Delay', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#ff9900"
                attributeState "notready", label:'Not Ready', icon:"st.security.alarm.off", backgroundColor:"#ffcc00"
                attributeState "ready", label:'Ready', action: 'stay', icon:"st.security.alarm.off", backgroundColor:"#79b821"
                attributeState "forceready", label:'Ready - F', action: 'stay', icon:"st.security.alarm.off", backgroundColor:"#79b821"
                attributeState "stay", label:'Armed Stay', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#008CC1"
                attributeState "instantaway", label:'Armed Instant Away', action: 'stay', icon:"st.security.alarm.on", backgroundColor:"#800000"
                attributeState "instantstay", label:'Armed Instant Stay', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#008CC1"
            }
            tileAttribute("device.trouble", key: "SECONDARY_CONTROL") {
              attributeState("detected", label:'Trouble')
              attributeState("clear", label:'No Trouble')
            }

        }
        standardTile("disarm", "capability.momentary", width: 2, height: 2, title: "Disarm"){
            state "disarm", label: 'Disarm', action: "disarm", icon: "st.Home.home4", backgroundColor: "#79b821"
        }
        standardTile("away", "capability.momentary", width: 2, height: 2, title: "Armed Away"){
            state "away", label: 'Arm Away', action: "away", icon: "st.Home.home4", backgroundColor: "#800000"
        }
        standardTile("stay", "capability.momentary", width: 2, height: 2, title: "Armed Stay"){
            state "stay", label: 'Arm Stay', action: "stay", icon: "st.Home.home4", backgroundColor: "#008CC1"
        }
        standardTile("instant", "capability.momentary", width: 2, height: 2, title: "Instant"){
            state "instant", label: 'Instant', action: "instant", icon: "st.Home.home4", backgroundColor: "#00FF00"
        }
        standardTile("night", "capability.momentary", width: 2, height: 2, title: "Night"){
            state "night", label: 'Night', action: "night", icon: "st.Home.home4", backgroundColor: "#AA00FF"
        }
        standardTile("chime", "capability.momentary", width: 2, height: 2, title: "Chime"){
            state "chime", label: 'Chime', action: "chime", icon: "st.Home.home4", backgroundColor: "#EE9D00"
        }
        standardTile("reset", "capability.momentary", width: 2, height: 2, title: "Sensor Reset"){
            state "reset", label: 'Sensor Reset', action: "reset", icon: "st.Home.home4", backgroundColor: "#FF3000"
        }

        main "status"
        details(["status", "away", "stay", "disarm", "instant", "night", "chime", "reset"])

    }
}

def parse(String description) {
}

def partition(String state, String partition) {
    // state will be a valid state for the panel (ready, notready, armed, etc)
    // partition will be a partition number, for most users this will always be 1

    log.debug "Partition: ${state} for partition: ${partition}"

    def troubleMap = [
      'trouble':"detected",
      'restore':"clear",
    ]

    def onList = ['alarm','entrydelay','exitdelay','instantstay','stay']

    if (onList.contains(state)) {
      sendEvent (name: "switch", value: "on")
    } else {
      sendEvent (name: "switch", value: "off")
    }

    if (troubleMap[state]) {
        def troubleState = troubleMap."${state}"
        // Send final event
        sendEvent (name: "trouble", value: "${troubleState}")
    } else {
        sendEvent (name: "status", value: "${state}")
    }
}


def away() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/alarm/arm",
        headers: [
            HOST: "$ip:$port"
        ]
    )
    log.debug "response" : "Request to away arm received"
    return result
}

def chime() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/alarm/togglechime",
        headers: [
            HOST: "$ip:$port"
        ]
    )
    log.debug "response" : "Request to toggle chime received"
    return result
}

def disarm() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/alarm/disarm",
        headers: [
            HOST: "$ip:$port"
        ]
    )
    log.debug "response" : "Request to disarm received"
    return result
}

def instant() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/alarm/toggleinstant",
        headers: [
            HOST: "$ip:$port"
        ]
    )
    log.debug "response" : "Request to toggle instant mode received"
    return result
}

def night() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/alarm/togglenight",
        headers: [
            HOST: "$ip:$port"
        ]
    )
    log.debug "response" : "Request to toggle night mode received"
    return result
}

def on() {
    stay()
}

def off() {
    disarm()
}

def reset() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/alarm/reset",
        headers: [
            HOST: "$ip:$port"
        ]
    )
    log.debug "response" : "Request to sensor reset received"
    return result
}

def stay() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/alarm/stayarm",
        headers: [
            HOST: "$ip:$port"
        ]
    )
    log.debug "response" : "Request to stay arm received"
    return result
}
