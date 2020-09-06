metadata {
	definition (name: "mySwitch", namespace: "smartthings", author: "Baranauskas",
               // vid: "c2c-switch", 
                runLocally: true, 
                minHubCoreVersion: '000.031.00004' //, 
         //       executeCommandsLocally: true
                ) {
		capability "Actuator"
//        capability "Refresh"
		capability "Switch"
		capability "Sensor"
	}
    
	// simulator metadata
	simulator {
    }

//	preferences {
//	}
/*
    tiles(scale: 2) {
        // standard tile with actions named
        standardTile("switch", "device.switch", width: 3, height: 2) {
           state "on", label: '${name}', icon: "st.switches.switch.on", backgroundColor: "#00a0dc", action: "switch.off", nextState: "off"
           state "off", label: '${name}', icon: "st.switches.switch.off", backgroundColor: "#ff0000", action: "switch.on", nextState: "on"
        }
        // the "switch" tile will appear in the Things view
        main("switch")

        // the "switch" and "power" tiles will appear in the Device Details
        // view (order is left-to-right, top-to-bottom)
//        details(["switch"])
    }
*/
}

def installed() {
  log.debug "installed begin"
  log.debug "switch deviceNetworkId: ${device.deviceNetworkId}"
  log.debug "switch attribute name: ${device.name}"
  log.debug "switch attribute label: ${device.label}"
  log.debug "switch attribute type: ${device.type}"
  log.debug "switch attribute hub: ${device.hub}"
  log.debug "switch attribute lastUpdated: ${device.lastUpdated}"
  log.debug "switch attribute currentState A: ${device.currentState('switch')?.value}"
  
  sendEvent(name: "switch", value: "off")
  log.debug "switch attribute currentState B: ${device.currentState('switch')?.value}"

  on()
  log.debug "switch attribute currentState C: ${device.currentState('switch')?.value}"
//  sendEvent(name: "switch", value: "on")
  log.debug "installed end"
  
}

//def updated(){
//	// Device-Watch simply pings if no device events received for 32min(checkInterval)
//}

def on() {
  log.debug "on:"
  
  def result = new physicalgraph.device.HubAction(
    [method: "POST",
     path: "/dispositivos/727623301/acionar/1",
     headers: [
       HOST: "192.168.1.201:3000",
       "Content-Type": "text/plain; charset=utf-8",
       "Authorization": "Basic dG9rZW46YTc5NTMxOWFkMzViNjQ2YzBiYjJmY2RjZDdjNjQ3MWQ="
      ]
    ]//,
    //null,
    //[callback:parse]
  )
  result.options = [outputMsgToS3:true]
//  result
  log.debug "result: "+result

  def name = device.deviceNetworkId
  log.debug "switch deviceNetworkId: ${name}"
  def switchAttr = device
  log.debug "switch attribute name: ${switchAttr.name}"
//  sendEvent(name: "myTest", value: "off")
  sendEvent(name: "switch", value: "on")
  sendHubCommand(result)
  return result
}

def off() {
  log.debug "off:"
  
  def result = new physicalgraph.device.HubAction(
    [method: "POST",
     path: "/dispositivos/727623301/acionar/0",
     headers: [
       HOST: "192.168.1.201:3000",
       "Content-Type": "text/plain; charset=utf-8",
       "Authorization": "Basic dG9rZW46YTc5NTMxOWFkMzViNjQ2YzBiYjJmY2RjZDdjNjQ3MWQ="
      ]
    ]//,
    //null,
    //[callback:parse]
  )
  result.options = [outputMsgToS3:true]
//  result
  log.debug "result: "+result

  def name = device.deviceNetworkId
  log.debug "switch deviceNetworkId: ${name}"
  def switchAttr = device
  log.debug "switch attribute name: ${switchAttr.name}"
//  sendEvent(name: "myTest", value: "off")
  sendEvent(name: "switch", value: "off")
  sendHubCommand(result)
  return result
}

def parse(description) {
  log.debug "in parse: ${description}"
  def msg = parseLanMessage(description)

  def headerString = msg.header
	def status = msg.status
	log.debug "header ${headerString}"
	log.debug "status ${status}"

	if (!headerString) {
		//log.debug "headerstring was null for some reason :("
    }

	def bodyString = msg.body
	if (bodyString) {
      log.debug "Parsing body: (inicio)"
      log.debug "Parsing body: ${bodyString}"
      log.debug "Parsing body: (fim)"
   }
}
