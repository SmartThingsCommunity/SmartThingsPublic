/**
 *  Curb Control
 *
 *  Author: Curb
 */

definition(
    name: "Curb Control",
    namespace: "Curb",
    author: "Curb",
    description: "This SmartApp allows you to interact with the switches in your physical graph through Curb.",
    category: "Convenience",
    iconUrl: "http://energycurb.com/images/logo.png",
    iconX2Url: "http://energycurb.com/images/logo.png",
    oauth: [displayName: "SmartThings Curb Control", displayLink: "energycurb.com"]
)

preferences {
  section("Allow Curb to Control These Things...") {
  input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
  }
}

mappings {
    path("/") {
        action: [
            GET: "index"
        ]
    }
  path("/switches") {
  action: [
    GET: "listSwitches",
    PUT: "updateSwitches"
    ]
  }
  path("/switches/:id") {
  action: [
    GET: "showSwitch",
    PUT: "updateSwitch"
    ]
  }
}

def installed() {}

def updated() {}

def index(){
    [[url: "/switches"]]
}

def listSwitches() {
  switches.collect { device(it,"switch") }
}
void updateSwitches() {
  updateAll(switches)
}
def showSwitch() {
  show(switches, "switch")
}
void updateSwitch() {
  update(switches)
}

private void updateAll(devices) {
  def command = request.JSON?.command
  if (command) {
    switch(command) {
      case "on":
      devices*.on()
      break
      case "off":
      devices*.off()
      break
      default:
      httpError(403, "Access denied. This command is not supported by current capability.")
    }
  }
}

private void update(devices) {
  log.debug "update, request: ${request.JSON}, params: ${params}, devices: $devices.id"
  def command = request.JSON?.command
  if (command) {
  def device = devices.find { it.id == params.id }
  if (!device) {
  httpError(404, "Device not found")
  } else {
  switch(command) {
  case "on":
  device.on()
  break
  case "off":
  device.off()
  break
  default:
  httpError(403, "Access denied. This command is not supported by current capability.")
  }
  }
  }
}

private show(devices, name) {
  def d = devices.find { it.id == params.id }
  if (!d) {
  httpError(404, "Device not found")
  }
  else {
        device(d, name)
  }
}

private device(it, name){
    if(it) {
  def s = it.currentState(name)
  [id: it.id, label: it.displayName, name: it.displayName, state: s]
    }
}
