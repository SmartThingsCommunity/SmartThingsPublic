/**
 *  Computer Power Control with Wake on LAN
 *
 *  Copyright 2016 Matt Sutton
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
definition(
    name: "Computer Power Control with Wake on LAN",
    namespace: "freethewhat",
    author: "Matt Sutton",
    description: "Powers a computer using wake on LAN and eventGhost to power of computer.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {    
	// simulated switch to control wake on lan and eventghost methods. 
    // if using amazon echo or google home name it after the device you would like 
    // to power on to allow voice control. (ie. Desktop PC, Media PC, Computer, etc.)
    section("Simulated Switch") {
    	input "theswitch", "capability.switch", required: true, title: "Switch"
    }
    
    section("Computer Information") {
    	input "computerIP","text", required:true, title: "Computer IP Address"
        input "computerPort","number", required: true, title: "Webserver port"
        input "macaddress", "text", required: true, title: "Computer MAC Address without"
        input "secureonpassword", "text", required: false, title: "SecureOn Password (Optional)"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	//switch.on sends WOL. Switch.off sends eventghost
	subscribe(theswitch, "switch.on", theswitchOnHandler)
    subscribe(theswitch, "switch.off", theswitchOffHandler)
}

def theswitchOnHandler(evt) {
	//sends myWOL results as hub command
	log.debug "theswitchOnHandler: Running"
    sendHubCommand(myWOL())
}

def theswitchOffHandler(evt) {
	//send myEventGhostShutdown as hub command
	log.debug "theswitchOffHandler: Running"
    sendHubCommand(myEventGhostShutdown())
}

def myWOL(evt) {
	//Determines if there is a secure password used for WOL
	if(secureonpassword){
    	//if a secure password exists
        //creates a new physicalgraph.device.hubaction
        def result = new physicalgraph.device.HubAction (
        	"wake on lan $macaddress",
        	physicalgraph.device.Protocol.LAN,
        	null,
        	[secureCode: "$secureonpassword"]
        )
        //returns the result
    	return result
    } else {
    	//if no secure password exists
        //creates a new physicalgraph.device.hubaction
    	log.debug "myWOL: SecureOn Password False"
        log.debug "myWOL: MAC Address $macaddress"
        def result = new physicalgraph.device.HubAction (
        	"wake on lan $macaddress",
        	physicalgraph.device.Protocol.LAN,
        	null
        )    
        //returns the result
        return result
    }
}

def myEventGhostShutdown(evt) {
	//concatenate computer IP and MAC
    //set even ghost command
    //encodes the eventghost command
	//creates new physical.device.graph from given values
	log.debug "eventghostPowerOff: Running"
	def egHost = computerIP + ":" + computerPort
	def egRawCommand = "ST.PCPower.Shutdown"
	def egRestCommand = java.net.URLEncoder.encode(egRawCommand)
	def result = new physicalgraph.device.HubAction("""GET /?$egRestCommand HTTP/1.1\r\nHOST: $egHost\r\n\r\n""",
    	physicalgraph.device.Protocol.LAN
    )
    //returns result
    return result
}
// TODO: implement event handlers