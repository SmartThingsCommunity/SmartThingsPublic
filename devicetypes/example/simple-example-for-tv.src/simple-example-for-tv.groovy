    /*
This example is provided in response to a query by Chadbaldwi

This is a small portion of a Device Handler I am writing for
a wifi speaker set.  I have modified it to accommodate the
simpler design and HMI needed in this specific use case.
*/

metadata {
	definition (name: "Simple Example for TV", namespace: "example", author: "example") {
		capability "Switch"
		capability "refresh"
		capability "Sensor"
		capability "Actuator"
	}
	tiles(scale: 2) {
//	The below is from a switch DH I authored.  Should work as-is.
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc",
				nextState:"waiting"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff",
				nextState:"waiting"
				attributeState "waiting", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#15EE10",
				nextState:"waiting"
				attributeState "commsError", label:'Comms Error', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#e86d13",
				nextState:"waiting"
			}
 			tileAttribute ("deviceError", key: "SECONDARY_CONTROL") {
				attributeState "deviceError", label: '${currentValue}'
			}
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 2,  decoration: "flat") {
			state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh")
		}		 
		
		main("switch")
		details("switch", "refresh")
	}
//	I add preferences (accessed in device details in the smartpone app
//	via the gear icon in upper right corner.
	preferences {
		input("deviceIP", "text", title: "Device IP", required: true, displayDuringSetup: true)
		input("devicePort", "text", title: "Device Port", required: true, displayDuringSetup: true)
    }
	main "switch"
	details(["switch", "refresh"])
}
def installed() {
	updated()
}
def updated() {
	unschedule()
}

//	----- ON/OFF COMMANDS -----
//	The on command.  Here I created your CURL command with a variable
//	on-off inserted.  This may take some playing with in your example.
def on() {
	def onOff = "1"		//	I assume "1" is the keycode for ON.
/*	
	Generating.  Starting with the Curl Command you provided
	{\"KEYLIST\": [{\"CODESET\": 11,\"CODE\": 1,\"ACTION\":\"KEYPRESS\"}]}
	I replaced the Code of "1" with KEY_CODE and converted to standard 
    by removing the "\" characters.
    {"KEYLIST": [{"CODESET": 11,"CODE": KEY_CODE,"ACTION":"KEYPRESS"}]}
    I then convert to URL format from site https://www.urlencoder.org/
    %7B%22KEYLIST%22%3A%20%5B%7B%22CODESET%22%3A%2011%2C%22CODE%22%3A%20KEY_CODE%2C%22ACTION%22%3A%22KEYPRESS%22%7D%5D%7D
    this is placed in a command to send to the unit.
    
    The KEY_CODE is replaced by the variable onOff as '${onOff}'
    
    I then add a specific parse method, used later.
*/
    sendCommand("/key_command/%7B%22KEYLIST%22%3A%20%5B%7B%22CODESET" +
                "%22%3A%2011%2C%22CODE%22%3A%20${onOff}%2C%22ACTION%22" +
                "%3A%22KEYPRESS%22%7D%5D%7D", "parseResponse")
//	Finally, I fire an event (will publish that this occurred to any 
//	handlers or apps that subscribe.
	sendEvent(name: "switch", value: "on")
    log.info "${device.label}: On/Off state is ${device.currentValue("switch")}."
}
def off() {
	def onOff = "0"		//	I assume "0" is the keycode for OFF.
    sendCommand("/key_command/%7B%22KEYLIST%22%3A%20%5B%7B%22CODESET" +
                "%22%3A%2011%2C%22CODE%22%3A%20${onOff}%2C%22ACTION%22" +
                "%3A%22KEYPRESS%22%7D%5D%7D", "parseResponse")
	sendEvent(name: "switch", value: "off")
    log.info "${device.label}: On/Off state is ${device.currentValue("switch")}."
}

//	----- SEND COMMAND  -----
private sendCommand(command, action){
	def cmdStr = new physicalgraph.device.HubAction([
		method: "PUT",	// I use 'GET' here.
		path: command,		//  The command I generated goes here.
		headers: [
//	deviceIP and devicePort were entered by you.  You can also hard
//	code these parameters.
//	I assumed the AUTH would go here.
			AUTH: "blahblahblah",
			HOST: "${deviceIP}:${devicePort}"
		]],
		null,
		[callback: action]
	)
//	for initial testing, I log the command string.  Assures I have reached
//	the send.  I remove this when I have verified.
    log.debug cmdStr
	sendHubCommand(cmdStr)
}
//	----- PARSE RESPONSE DATA BASED ON METHOD -----
def parseResponse(resp) {
//	for initial testing, I log the parse response received from the
//	device.
	log.debug "RAW RESPONSE:  ${resp}"
//	The below is an example for my device.  Yours may not return
//	anything (key press interface) or may return a success of some
//	sort.  The below may result in a clear text response that could
//	be readily read.
	def responseBody = (new XmlSlurper().parseText(resp.body))
    log.debug "BODY:  ${responseBody}"
}