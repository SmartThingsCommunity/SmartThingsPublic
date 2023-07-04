metadata {
	definition(name: "Sonoff-Tasmota", namespace: "BrettSheleski", author: "Brett Sheleski", ocfDeviceType: "oic.d.smartplug") {
		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Polling"
		capability "Refresh"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "momentary.push", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				attributeState "off", label: '${name}', action: "momentary.push", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","refresh"])
	}

	preferences {
		input(name: "ipAddress", type: "string", title: "IP Address", description: "IP Address of Sonoff", displayDuringSetup: true, required: true)
		input(name: "port", type: "number", title: "Port", description: "Port", displayDuringSetup: true, required: true, defaultValue: 80)
		
		section("Sonoff Host") {
			
		}

		section("Authentication") {
			input(name: "username", type: "string", title: "Username", description: "Username", displayDuringSetup: false, required: false)
			input(name: "password", type: "password", title: "Password (sent cleartext)", description: "Caution: password is sent cleartext", displayDuringSetup: false, required: false)
		}
	}

	simulator {
        // status declarations specify messages that result from a person physically actuating the device
		// this is the message that the device will send to the Device Handler’s parse(message) method
		status "switch reports off": "index:15, mac:5CCF7FBD413C, ip:C0A80206, port:0050, requestId:f1f55fbf-b2c8-470b-bc47-0bb4cb938f25, headers:SFRUUC8xLjEgMjAwIE9LDQpDb250ZW50LVR5cGU6IGFwcGxpY2F0aW9uL2pzb24NCkNvbnRlbnQtTGVuZ3RoOiAxNQ0KQ29ubmVjdGlvbjogY2xvc2U=, body:eyJQT1dFUiI6Ik9GRiJ9"
		status "switch reports on": "index:15, mac:5CCF7FBD413C, ip:C0A80206, port:0050, requestId:dc77bc98-ddb3-409b-844a-0b8864082f39, headers:SFRUUC8xLjEgMjAwIE9LDQpDb250ZW50LVR5cGU6IGFwcGxpY2F0aW9uL2pzb24NCkNvbnRlbnQtTGVuZ3RoOiAxNA0KQ29ubmVjdGlvbjogY2xvc2U=, body:eyJQT1dFUiI6Ik9OIn0="
		status "poll status off": "index:15, mac:5CCF7FBD413C, ip:C0A80206, port:0050, requestId:eee15c07-a24a-4fc6-8d8e-b1911ed6764f, headers:SFRUUC8xLjEgMjAwIE9LDQpDb250ZW50LVR5cGU6IGFwcGxpY2F0aW9uL2pzb24NCkNvbnRlbnQtTGVuZ3RoOiAxODkNCkNvbm5lY3Rpb246IGNsb3Nl, body:eyJTdGF0dXMiOnsiTW9kdWxlIjoxLCJGcmllbmRseU5hbWUiOiJTb25vZmYiLCJUb3BpYyI6ImRlZmF1bHQtdG9waWMiLCJCdXR0b25Ub3BpYyI6IjAiLCJQb3dlciI6MCwiUG93ZXJPblN0YXRlIjoxLCJMZWRTdGF0ZSI6MSwiU2F2ZURhdGEiOjEsIlNhdmVTdGF0ZSI6MCwiQnV0dG9uUmV0YWluIjowLCJQb3dlclJldGFpbiI6MH19"
		status "poll status on": "index:15, mac:5CCF7FBD413C, ip:C0A80206, port:0050, requestId:b813e07c-4984-40c8-97bd-ba7603804ad0, headers:SFRUUC8xLjEgMjAwIE9LDQpDb250ZW50LVR5cGU6IGFwcGxpY2F0aW9uL2pzb24NCkNvbnRlbnQtTGVuZ3RoOiAxODkNCkNvbm5lY3Rpb246IGNsb3Nl, body:eyJTdGF0dXMiOnsiTW9kdWxlIjoxLCJGcmllbmRseU5hbWUiOiJTb25vZmYiLCJUb3BpYyI6ImRlZmF1bHQtdG9waWMiLCJCdXR0b25Ub3BpYyI6IjAiLCJQb3dlciI6MSwiUG93ZXJPblN0YXRlIjoxLCJMZWRTdGF0ZSI6MSwiU2F2ZURhdGEiOjEsIlNhdmVTdGF0ZSI6MCwiQnV0dG9uUmV0YWluIjowLCJQb3dlclJldGFpbiI6MH19"
		status "legacy switch reports off": "index:15, mac:5CCF7FBD413C, ip:C0A80206, port:0050, requestId:9f55a327-be15-43fb-91ce-c04a035a3217, headers:SFRUUC8xLjEgMjAwIE9LCkNvbnRlbnQtVHlwZTogdGV4dC9wbGFpbgpDb250ZW50LUxlbmd0aDogMzYKQ29ubmVjdGlvbjogY2xvc2U=, body:UkVTVUxUID0geyJQT1dFUiI6Ik9GRiJ9ClBPV0VSID0gT0ZG"
		status "legacy switch reports on": "index:15, mac:5CCF7FBD413C, ip:C0A80206, port:0050, requestId:9f55a327-be15-43fb-91ce-c04a035a3217, headers:SFRUUC8xLjEgMjAwIE9LCkNvbnRlbnQtVHlwZTogdGV4dC9wbGFpbgpDb250ZW50LUxlbmd0aDogMzQKQ29ubmVjdGlvbjogY2xvc2U=, body:UkVTVUxUID0geyJQT1dFUiI6Ik9OIn0KUE9XRVIgPSBPTg=="
		status "legacy poll status off": "index:15, mac:5CCF7FBD413C, ip:C0A80206, port:0050, requestId:9f55a327-be15-43fb-91ce-c04a035a3217, headers:SFRUUC8xLjEgMjAwIE9LCkNvbnRlbnQtVHlwZTogdGV4dC9wbGFpbgpDb250ZW50LUxlbmd0aDogMjE5CkNvbm5lY3Rpb246IGNsb3Nl, body:U1RBVFVTID0geyJTdGF0dXMiOnsiTW9kdWxlIjoxLCAiRnJpZW5kbHlOYW1lIjoiQmFzZW1lbnQgTGlnaHRzIiwgIlRvcGljIjoiYmFzZW1lbnQtbGlnaHRzIiwgIkJ1dHRvblRvcGljIjoiMCIsICJQb3dlciI6MCwgIlBvd2VyT25TdGF0ZSI6MywgIkxlZFN0YXRlIjoxLCAiU2F2ZURhdGEiOjEsICJTYXZlU3RhdGUiOjEsICJCdXR0b25SZXRhaW4iOjAsICJQb3dlclJldGFpbiI6MH19"
		status "legacy poll status on": "index:15, mac:5CCF7FBD413C, ip:C0A80206, port:0050, requestId:9f55a327-be15-43fb-91ce-c04a035a3217, headers:SFRUUC8xLjEgMjAwIE9LCkNvbnRlbnQtVHlwZTogdGV4dC9wbGFpbgpDb250ZW50LUxlbmd0aDogMjE5CkNvbm5lY3Rpb246IGNsb3Nl, body:U1RBVFVTID0geyJTdGF0dXMiOnsiTW9kdWxlIjoxLCAiRnJpZW5kbHlOYW1lIjoiQmFzZW1lbnQgTGlnaHRzIiwgIlRvcGljIjoiYmFzZW1lbnQtbGlnaHRzIiwgIkJ1dHRvblRvcGljIjoiMCIsICJQb3dlciI6MSwgIlBvd2VyT25TdGF0ZSI6MywgIkxlZFN0YXRlIjoxLCAiU2F2ZURhdGEiOjEsICJTYXZlU3RhdGUiOjEsICJCdXR0b25SZXRhaW4iOjAsICJQb3dlclJldGFpbiI6MH19"

		// reply declarations specify responses that the physical device will send to the Device Handler
		// when it receives a certain message from the Hub
		// reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
    }
}

String testLegacyInput() {
	String prefix = 'index:15, mac:5CCF7FBD413C, ip:C0A80206, port:0050, requestId:9f55a327-be15-43fb-91ce-c04a035a3217'
	//String multiBody = '''STATUS = {"Status":{"Module":1, "FriendlyName":"Basement Lights", "Topic":"basement-lights", "ButtonTopic":"0", "Power":0, "PowerOnState":3, "LedState":1, "SaveData":1, "SaveState":1, "ButtonRetain":0, "PowerRetain":0}}'''
	String multiBody = '''RESULT = {"POWER":"ON"}
POWER = ON'''
	def contentLength = multiBody.length()
	String multiHeaders = """HTTP/1.1 200 OK
Content-Type: text/plain
Content-Length: $contentLength
Connection: close"""	
	String multilineString = prefix + ', headers:' + multiHeaders.bytes.encodeBase64() + ', body:' + multiBody.bytes.encodeBase64()
	return multilineString
}

def parse(String description) {
	def message = parseLanMessage(description)	//def message = parseLanMessage(testLegacyInput())

	// parse result from current and legacy formats
	def resultJson = {}
	if (message?.json) {
		// current json data format
		resultJson = message.json
	}
	else {
		// legacy Content-Type: text/plain
		// with json embedded in body text
		def STATUS_PREFIX = "STATUS = "
		def RESULT_PREFIX = "RESULT = "
		if (message?.body?.startsWith(STATUS_PREFIX)) {
			resultJson = new groovy.json.JsonSlurper().parseText(message.body.substring(STATUS_PREFIX.length()))
		}
		else if (message?.body?.startsWith(RESULT_PREFIX)) {
			resultJson = new groovy.json.JsonSlurper().parseText(message.body.substring(RESULT_PREFIX.length()))
		}
	}

	// consume and set switch state
	if ((resultJson?.POWER in ["ON", 1, "1"]) || (resultJson?.Status?.Power in [1, "1"])) {
		setSwitchState(true)
	}
	else if ((resultJson?.POWER in ["OFF", 0, "0"]) || (resultJson?.Status?.Power in [0, "0"])) {
		setSwitchState(false)
	}
	else {
		log.error "can not parse result with header: $message.header"
		log.error "...and raw body: $message.body"
	}
}

def setSwitchState(Boolean on) {
	log.info "switch is " + (on ? "ON" : "OFF")
	sendEvent(name: "switch", value: on ? "on" : "off")
}

def push() {
	log.debug "PUSH"
	sendCommand("Power", "Toggle")
}

def on() {
	log.debug "ON"
	sendCommand("Power", "On")
}

def off() {
	log.debug "OFF"
	sendCommand("Power", "Off")
}

def poll() {
	log.debug "POLL"
	sendCommand("Status", null)
}

def refresh() {
	log.debug "REFRESH"
	sendCommand("Status", null)
}

private def sendCommand(String command, String payload) {
	log.debug "sendCommand(${command}:${payload}) to device at $ipAddress:$port"

	if (!ipAddress || !port) {
		log.warn "aborting. ip address or port of device not set"
		return null;
	}
	def hosthex = convertIPtoHex(ipAddress)
	def porthex = convertPortToHex(port)
	device.deviceNetworkId = "$hosthex:$porthex"

	def path = "/cm"
	if (payload){
		path += "?cmnd=${command}%20${payload}"
	}
	else{
		path += "?cmnd=${command}"
	}

	if (username){
		path += "&user=${username}"
		if (password){
			path += "&password=${password}"
		}
	}

	def result = new physicalgraph.device.HubAction(
		method: "GET",
		path: path,
		headers: [
			HOST: "${ipAddress}:${port}"
		]
	)
	return result
}

private String convertIPtoHex(ipAddress) { 
	String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format('%04x', port.toInteger())
	return hexport
}