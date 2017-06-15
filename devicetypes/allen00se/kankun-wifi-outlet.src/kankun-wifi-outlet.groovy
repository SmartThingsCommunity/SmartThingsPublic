/**
 *  Kankun Wifi Outlet (Outlet Must Be "Hacked" according to instructions Below)
 *
 *  Copyright 2015 Krys Allen
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
 *  The Kankun Wifi outlet ships with some smartphone app control, however someone smarter than me realized it was running
 *	openWRT and put out a hack to make it very easily joined to your WAN and controlled via HTTP.
 *  Follow the document here to get the outlet joined your WAN https://drive.google.com/file/d/0B3zmQ6SwYdyJeEdlc2V2S2VoRzg/view
 *  Then use this guys JSON code to set up the outlet for HTTP. https://github.com/homedash/kankun-json
 *	Then this device type should work.
 */
preferences {
        input("ip", "string", title:"IP Address", description: "10.1.1.185", defaultValue: "10.1.1.185" ,required: true, displayDuringSetup: true)
        input("port", "string", title:"Port", description: "80", defaultValue: "80" , required: true, displayDuringSetup: true)
}

metadata {
	definition (name: "Kankun Wifi Outlet", namespace: "allen00se", author: "Krys Allen") {
		capability "Switch"
        capability "Polling"
		capability "Refresh"
	}

	simulator {
		// TODO: define status and reply messages here
	}
    
    tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","refresh"])
	}

	//tiles {
 	//	standardTile("button", "device.switch", width: 1, height: 1, canChangeIcon: true) {
	//		state "off", label: 'Off', icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
	//		state "on", label: 'On', icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		// TODO: define your main and details tiles here
	//}
    //main "button"
    //    details(["button"])
	//}
}


// ------------------------------------------------------------------

// parse events into attributes
def parse(String description) {
	//log.debug "Starting LAN Parse"
	def msg = parseLanMessage(description)
    //log.debug "msg: ${msg}"
    def json = msg.json
    log.debug "JSON: ${json}"
    
    //if (json['ok'] == 'true'){
    //	log.debug "Set command was Successful!"
    //} else {
    //	log.debud "Set command failed!"
    //}

	if (json['state'] == 'on'){
    	log.debug "Outlet is On"
   		sendEvent(name: "switch", value: "on")
    } else if (json['state'] == 'off'){
    	log.debug "Outlet is Off"
   		sendEvent(name: "switch", value: "off")
    } else if (json['ok'] == true){
    	log.debug "Set command was Successful!"
    } else {
    	log.debug "Command Failed! Result: ${json['ok']}"
    }
}


def poll() {
	log.debug "Executing 'poll'"
    sendEvent(name: "switch", value: "off")
    myCommand("get=state")
}

def refresh() {
	sendEvent(name: "switch", value: "off")
	log.debug "Executing 'refresh'"
    myCommand("get=state")
}

private setDeviceNetworkId(ip,port){
	log.debug "IP:${ip} Port:${port}"
  	def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(port)
    def hexVal = "$iphex:$porthex"
  	device.deviceNetworkId = "${hexVal}"
  	log.debug "Device Network Id set to ${hexVal}" //:${porthex}"
    return hexVal
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

def myCommand(value) {
	def hexValues = setDeviceNetworkId(ip,port)
	log.debug "Running myCommand with values:${value}"
    def myCommandAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/cgi-bin/json.cgi?${value}",
        headers: [
            HOST:"${hexValues}"
        ]
    )
    log.debug("Executing hubAction on ${hexValues}") // + getOutletAddress())
    //log.debug "Result: ${myCommandAction}"
    myCommandAction 
}

def on() {
	log.debug "Executing 'on'"
    sendEvent(name: "switch", value: "on")
    myCommand("set=on")
    // TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
    sendEvent(name: "switch", value: "off")
    myCommand("set=off")
	// TODO: handle 'off' command
}
