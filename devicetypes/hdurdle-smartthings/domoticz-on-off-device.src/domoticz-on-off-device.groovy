import java.security.MessageDigest

preferences {
 input("domoticzIP", "text", title: "Domoticz IP Address", description: "IP Address of the Server")
 input("deviceID", "text", title: "Device ID", description: "The device id")
}

metadata {
 definition(name: "Domoticz On Off Device", namespace: "hdurdle-smartthings", author: "Howard Durdle") {
  capability "Switch"
  command "register"
 }

 simulator {}

 tiles(scale: 2) {
  multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
   tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
    attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
    attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "On"
   }
  }

  standardTile("register", "device.status", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
   state "default", label: "Register", icon: "http://www.mocet.com/pic/link-icon.png", action: "register"
  }

  main "switch"
  details(["switch", "register"])
 }
}

// parse events into attributes
def parse(String description) {
 log.debug "Parsing '${description}'"
  // TODO: handle 'switch' attribute
}

// handle commands
def on() {
 sendEvent(name: "switch", value: 'on')
 apiGet('On')
}

def off() {
 sendEvent(name: "switch", value: 'off')
 apiGet('Off')
}

private apiGet(state) {

 log.debug settings.domoticzIP + ':8080'

 def httpRequest = [
  method: 'GET',
  path: '/json.htm',
  headers: [
   HOST: settings.domoticzIP + ':8080',
   Accept: "*/*"
  ],
  query: [
   type: 'command', param: 'switchlight',
   switchcmd: state,
   idx: settings.deviceID
  ]
 ]

 log.debug httpRequest.query

 return new physicalgraph.device.HubAction(httpRequest)
}