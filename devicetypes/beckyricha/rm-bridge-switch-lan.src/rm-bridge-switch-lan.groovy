/**
 *  Broadlink Switch
 *
 *  Copyright 2016 BeckyR
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
 *  1/7/17 - updated with better switch displays and use of device ID from user itsamti
 */
 
 
// 09/01/2016 - itsamti - Added new switch definition below 
metadata {
	
    definition (name: "RM Bridge Switch LAN", namespace: "beckyricha", author: "BeckyR") {
		capability "Switch"
		command "onPhysical"
		command "offPhysical"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
		standardTile("on", "device.switch", decoration: "flat") {
			state "default", label: 'On', action: "onPhysical", backgroundColor: "#ffffff"
		}
		standardTile("off", "device.switch", decoration: "flat") {
			state "default", label: 'Off', action: "offPhysical", backgroundColor: "#ffffff"
		}
        main "switch"
		details(["switch","on","off"])
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def on() {
	sendEvent(name: "switch", value: "on")
	put('on')
}

def off() {
	sendEvent(name: "switch", value: "off")
	put('off')
}

def onPhysical() {
	sendEvent(name: "switch", value: "on", type: "physical")
	put('on')
}

def offPhysical() {
	sendEvent(name: "switch", value: "off", type: "physical")
	put('off')
}

private put(toggle) {
    def url1="192.168.1.105:9876"
    def userpassascii= "yourusername:yourpassword"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def toReplace = device.deviceNetworkId
	def replaced = toReplace.replaceAll(' ', '%20')
 	def hubaction = new physicalgraph.device.HubAction(
				method: "GET",
               path: "/code/$replaced%20$toggle",
               headers: [HOST: "${url1}", AUTHORIZATION: "${userpass}"],
               )
   			return hubaction
   }