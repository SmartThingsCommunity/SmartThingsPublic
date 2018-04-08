/**
 *  Broadlink Virtual Switch for RM Plugin, RM Bridge & Home Assistant
 *
 *  Copyright 2017 af950833
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
preferences {    
	section("Internal Access"){
		input "id_pw", "text", title: "ID & PW for RM Plugin or RM Bridge(Optional)", required: false
		input "internal_ip", "text", title: "IP for RM Plugin or RM Bridge or HA(Required for all)", required: true
		input "internal_port", "text", title: "Port(Required for all)", required: true
		input "internal_on_path", "text", title: "On Path(Required for all)", required: true
		input "internal_off_path", "text", title: "Off Path(Optional)", required: false
        input "body_data_for_ha", "text", title: "Entity ID of Body Data for HA(Required for HA)", required: false
	}
}




metadata {
	definition (name: "Broadlink Virtaul Switch", namespace: "smartthings", author: "af950833") {
			capability "Switch"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.Home.home30", backgroundColor: "#ffffff", nextState: "on"
				state "on", label: 'On', action: "switch.off", icon: "st.Home.home30", backgroundColor: "#79b821", nextState: "off"
		}
		standardTile("offButton", "device.button", width: 1, height: 1, canChangeIcon: true) {
			state "default", label: 'Force Off', action: "switch.off", icon: "st.Home.home30", backgroundColor: "#ffffff"
		}
		standardTile("onButton", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "default", label: 'Force On', action: "switch.on", icon: "st.Home.home30", backgroundColor: "#79b821"
		}
		main "button"
			details (["button","onButton","offButton"])
	}
}

def parse(String description) {
	log.debug(description)
}

def on() {
	if (internal_on_path){
    	def userpass = "Basic " + id_pw.encodeAsBase64().toString()
		def result = new physicalgraph.device.HubAction(
				method: "POST",		/* If you want to use the RM Bridge, change the method from "POST" to "Get" */
				path: "${internal_on_path}",
				headers: [HOST: "${internal_ip}:${internal_port}", AUTHORIZATION: "${userpass}"],
                body: ["entity_id":"${body_data_for_ha}"]
				)
			sendHubCommand(result)
			sendEvent(name: "switch", value: "on") 
			log.debug "Executing ON" 
			log.debug result
	}
}

def off() {
	if (internal_off_path){
    	def userpass = "Basic " + id_pw.encodeAsBase64().toString()
		def result = new physicalgraph.device.HubAction(
				method: "POST",		/* If you want to use the RM Bridge, change the method from "POST" to "Get" */
				path: "${internal_off_path}",
				headers: [HOST: "${internal_ip}:${internal_port}", AUTHORIZATION: "${userpass}"],
                body: ["entity_id":"${body_data_for_ha}"]
				)

			sendHubCommand(result)
			sendEvent(name: "switch", value: "off")
			log.debug "Executing OFF" 
			log.debug result
	}
}