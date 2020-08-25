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
 */
metadata {
	definition (name: "broadlinkswitch", namespace: "smartthings", author: "BeckyR") {
		capability "Switch"
        attribute "onCodeID","string"
        attribute "BLmac","string"
        attribute "BLURL","string"
        attribute "offCodeID","string"
        command "changedata"
	}


	// tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}

		main "switch"
		details "switch"
	}
}
 
def on() {
	def codeID = device.currentValue("onCodeID")
	put("$codeID")
   
}

def off() {
	def codeID = device.currentValue("offCodeID")
	put("$codeID")
}

def changedata(att,newstate){
	sendEvent(name:"${att}",value: "${newstate}")
 }
 
private put(toggle) {
	def url1=device.currentValue("BLURL")
   	def url2=device.currentValue("BLmac")
 	def hubaction = new physicalgraph.device.HubAction(
				method: "GET",
               path: "/send?deviceMac=${url2}&codeId=$toggle",
               headers: [HOST: "${url1}"],
               )
   			return hubaction
   } 