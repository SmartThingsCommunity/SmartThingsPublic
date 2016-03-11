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
        command "disarm"
        command "instant"
        command "night"
        command "partition"
        command "reset"
        command "stay"
        command "togglechime"
    }

    // simulator metadata
    simulator {
    }

    // UI tile definitions
    tiles(scale: 2) {
        standardTile ("status", "device.status", width: 4, height: 4, title: "Status") {
            state "alarm", label:'Alarm', action: 'disarm', icon:"st.security.alarm.off", backgroundColor:"#ff0000"
            state "away", label:'Armed Away', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#800000"
            state "disarm", label:'Disarmed', icon:"st.security.alarm.off", backgroundColor:"#79b821"
            state "entrydelay", label:'Entry Delay', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#ff9900"
            state "exitdelay", label:'Exit Delay', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#ff9900"
            state "notready", label:'Not Ready', icon:"st.security.alarm.off", backgroundColor:"#ffcc00"
            state "ready", label:'Ready', action: 'stay', icon:"st.security.alarm.off", backgroundColor:"#79b821"
            state "forceready", label:'Ready - F', action: 'stay', icon:"st.security.alarm.off", backgroundColor:"#79b821"
            state "stay", label:'Armed Stay', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#008CC1"
            state "instantaway", label:'Armed Instant Away', action: 'stay', icon:"st.security.alarm.on", backgroundColor:"#800000"
            state "instantstay", label:'Armed Instant Stay', action: 'disarm', icon:"st.security.alarm.on", backgroundColor:"#008CC1"
        }
        standardTile("trouble", "device.trouble", width: 2, height: 2, title: "Trouble") {
          state "detected", label: 'Trouble', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
          state "clear", label: 'No Trouble', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
        }
        standardTile("chime", "device.chime", width: 2, height: 2, title: "Chime"){
            state "togglechime", label: 'Toggling Chime', action: "togglechime", icon: "st.Home.home4", backgroundColor: "#fbd48a"
            state "chime", label: 'Chime', action: "togglechime", icon: "st.Home.home4", backgroundColor: "#EE9D00"
            state "nochime", label: 'No Chime', action: "togglechime", icon: "st.Home.home4", backgroundColor: "#796338"
        }
        standardTile("disarm", "capability.momentary", width: 2, height: 2, title: "Disarm"){
            state "disarm", label: 'Disarm', action: "disarm", icon: "st.Home.home4", backgroundColor: "#79b821"
        }
        standardTile("away", "capability.momentary", width: 2, height: 2, title: "Away Arm"){
            state "away", label: 'Away Arm', action: "away", icon: "st.Home.home4", backgroundColor: "#800000"
        }
        standardTile("stay", "capability.momentary", width: 2, height: 2, title: "Stay Arm"){
            state "stay", label: 'Stay Arm', action: "stay", icon: "st.Home.home4", backgroundColor: "#008CC1"
        }
        standardTile("instant", "capability.momentary", width: 2, height: 2, title: "Instant"){
            state "instant", label: 'Instant', action: "instant", icon: "st.Home.home4", backgroundColor: "#00FF00"
        }
        standardTile("night", "capability.momentary", width: 2, height: 2, title: "Night"){
            state "night", label: 'Night', action: "night", icon: "st.Home.home4", backgroundColor: "#AA00FF"
        }
        standardTile("reset", "capability.momentary", width: 2, height: 2, title: "Sensor Reset"){
            state "reset", label: 'Sensor Reset', action: "reset", icon: "st.Home.home4", backgroundColor: "#FF3000"
        }

        main "status"
        details(["status", "trouble", "chime", "away", "stay", "disarm", "instant", "night", "reset"])

    }
}

def parse(String description) {
}

def partition(String state, String partition) {
    // state will be a valid state for the panel (ready, notready, armed, etc)
    // partition will be a partition number, for most users this will always be 1

    log.debug "Partition: ${state} for partition: ${partition}"

    def onList = ['alarm','entrydelay','exitdelay','instantstay','stay']

    def chimeList = ['chime','nochime']

    def troubleMap = [
      'trouble':"detected",
      'restore':"clear",
    ]

    if (onList.contains(state)) {
      sendEvent (name: "switch", value: "on")
    } else if (!(chimeList.contains(state) || troubleMap[state])) {
      sendEvent (name: "switch", value: "off")
    }

    if (troubleMap[state]) {
        def troubleState = troubleMap."${state}"
        // Send trouble event
        sendEvent (name: "trouble", value: "${troubleState}")
    } else if (chimeList.contains(state)) {
        // Send chime event
        sendEvent (name: "chime", value: "${state}")
    } else {
        // Send final event
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

def togglechime() {
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
