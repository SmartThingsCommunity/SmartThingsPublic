/**
 *  Copyright 2015 SmartThings
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
	definition (name: "esp8266 Outlets", namespace: "esp8266", author: "MikeD") {
		capability "Switch"
		capability "Actuator"
        
		attribute "switch1", "string"
		attribute "switch2", "string"
		attribute "switch3", "string"
		attribute "switch4", "string"
		attribute "switch5", "string"
		attribute "switch6", "string"
		attribute "switch7", "string"
        
		command "switch1on"
		command "switch1off"
		command "switch2on"
		command "switch2off"
		command "switch3on"
		command "switch3off"
		command "switch4on"
		command "switch4off"
		command "switch5on"
		command "switch5off"
		command "switch6on"
		command "switch6off"
		command "switch7on"
		command "switch7off"        
	}
	preferences {
        input("ip", "string", title:"IP Address", description: "192.168.1.241", required: true, displayDuringSetup: true)
        input("port", "string", title:"Port", description: "8000", defaultValue: 8000 , required: true, displayDuringSetup: true)
        input("username", "string", title:"Username", description: "username", required: false, displayDuringSetup: true)
        input("password", "password", title:"Password", description: "Password", required: false, displayDuringSetup: true)        
	}
 
	

	// tile definitions
	tiles {

        standardTile("switch1", "device.switch1", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch1on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: '${name}', action: "switch1off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}

        standardTile("switch2", "device.switch2", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch2on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: '${name}', action: "switch2off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}
        
        standardTile("switch3", "device.switch3", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch3on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: '${name}', action: "switch3off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}
        
        standardTile("switch4", "device.switch4", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch4on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: '${name}', action: "switch4off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}

        standardTile("switch5", "device.switch5", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch5on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: '${name}', action: "switch5off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}

        standardTile("switch6", "device.switch6", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch6on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: '${name}', action: "switch6off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}

        standardTile("switch7", "device.switch7", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch7on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: '${name}', action: "switch7off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}

        main (["switch1"])
        details (["switch1","switch2","switch3","switch4","switch5","switch6","switch7"])
	}
}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"
    // TODO: handle 'switch' attribute

}


def switch1on() {
	log.debug "Turn $device on"
	def uri = "/socket?parms=s1on"
    postAction(uri)
}

def switch1off() {
	log.debug "Turn $device on"
	def uri = "/socket?parms=s1off"
    postAction(uri)
}

def switch2on() {
	log.debug "Turn $device on"
	def uri = "/socket?parms=s2on"
    postAction(uri)
}

def switch2off() {
	log.debug "Turn $device on"
	def uri = "/socket?parms=s2off"
    postAction(uri)
}

def switch3on() {
	log.debug "Turn socket3On"
	def uri = "/socket?parms=s3on"
    postAction(uri)
}

def switch3off() {
	log.debug "Turn socket3Off"
	def uri = "/socket?parms=s3off"
    postAction(uri)
}

def switch4on() {
	log.debug "Turn socket4On"
	def uri = "/socket?parms=s4on"
    postAction(uri)
}

def switch4off() {
	log.debug "Turn socket4Off"
	def uri = "/socket?parms=s4off"
    postAction(uri)
}

def switch5on() {
	log.debug "Turn socket5On"
	def uri = "/socket?parms=s5on"
    postAction(uri)
}

def switch5off() {
	log.debug "Turn socket5Off"
	def uri = "/socket?parms=s5off"
    postAction(uri)
}

def switch6on() {
	log.debug "Turn socket6On"
	def uri = "/socket?parms=s6on"
    postAction(uri)
}

def switch6off() {
	log.debug "Turn socket6Off"
	def uri = "/socket?parms=s6off"
    postAction(uri)
}

def switch7on() {
	log.debug "Turn socket7On"
	def uri = "/socket?parms=s7on"
    postAction(uri)
}

def switch7off() {
	log.debug "Turn socket7Off"
	def uri = "/socket?parms=s7off"
    postAction(uri)
}





// ------------------------------------------------------------------

private postAction(uri){
  setDeviceNetworkId(ip,port)  
  def headers = getHeader()
  def hubAction = new physicalgraph.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers
  )//,delayAction(1000), refresh()]
  log.debug("Executing hubAction on " + getHostAddress())
  log.debug hubAction
  hubAction    
}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private encodeCredentials(username, password){
	log.debug "Encoding credentials"
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    log.debug "ASCII credentials are ${userpassascii}"
    log.debug "Credentials are ${userpass}"
    return userpass
}

private getHeader(userpass){
	log.debug "Getting headers"
    def headers = [:]
    headers.put("HOST", getHostAddress())
    headers.put("Authorization", userpass)
    log.debug "Headers are ${headers}"
    return headers
}

private delayAction(long time) {
	new physicalgraph.device.HubAction("delay $time")
}

private setDeviceNetworkId(ip,port){
  	def iphex = convertIPtoHex(ip)
  	def porthex = convertPortToHex(port)
  	device.deviceNetworkId = "$iphex:$porthex"
  	log.debug "Device Network Id set to ${iphex}:${porthex}"
}

private getHostAddress() {
	return "${ip}:${port}"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}