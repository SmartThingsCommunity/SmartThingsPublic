/**
 *  Zigbee Garage Door Control
 *
 *  Copyright 2018 George Richards
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
	// Automatically generated. Make future change here.

	definition (name: "Zigbee Garage Door Control", namespace: "falcogeorgeGDC", author: "George Richards") {
		capability "Garage Door Control"
        capability "Actuator"
        capability "Switch"
        capability "Refresh"
   		capability "Sensor"
   		capability "Contact Sensor"

	

		command "on"
		command "off"
        command "toggle"

		fingerprint  profileId: "0104", inClusters: "0000,0003,0006", outClusters: "0000,000F", manufacturer: "Richards Labs", model: "Garage Pop 1"

	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		standardTile("switch", "device.switch", width: 6, height: 4, canChangeIcon: true) {
			state "closed", label: 'Closed', action: "switch.on", icon: "st.Transportation.transportation12", backgroundColor: "#53a7c0", nextState:"opening"
			state "open", label: 'Open', action: "switch.off", icon: "st.Transportation.transportation12", backgroundColor: "#ff2d00", nextState:"closing"             
			state "closing", label: 'Closing', action: "switch.on", icon: "st.Transportation.transportation12", backgroundColor: "#ffb600"
			state "opening", label: 'Opening', action: "switch.on", icon: "st.Transportation.transportation12", backgroundColor: "#ffb600" }            

		standardTile("refresh", "device.refresh", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		
		main "switch"
		details(["switch","refresh"])
	}

}

// Globals
private getCLUSTER_BASIC() { 0x0000 }
private getCLUSTER_ON_OFF() { 0x006 }
private getCLUSTER_BINARY_INPUT() { 0x000F }
private getON_OFF_ATTR_BASIO() {0x0055 }


// parse events into attributes
def parse(String description) {
//    log.debug zigbee.parseDescriptionAsMap(description)  // full raw message
	log.debug "Parsing '${description}'" // deconstructed message
	// TODO: handle 'door' attribute
   Map map = [:]

//   log.debug "${resultMap}"
   if (description?.startsWith('read attr')) {
      map = parseCustomMessage(description) 
      sendEvent(name: "lastOpened", value: now)
	}
   if (description?.startsWith('catchall:')) 
      map = parseCatchAllMessage(description)
//   log.debug "Parse returned $map"
   def results = map ? createEvent(map) : null

   return results;


}

// handle commands
def on() { // Open the Door
	def doorstat = DoorStatus()
	log.debug "Opening the Garage door control"
    zigbee.command(0x0006,0x01)
}


def off() {
	def doorstat = DoorStatus()
	log.debug "Executing 'off' Closing Garage Door"
    zigbee.command(0x0006,0x00)
} 

def DoorStatus() {   
        def  String doorResult =
        	 zigbee.readAttribute(CLUSTER_BINARY_INPUT, ON_OFF_ATTR_BASIO) 
//             doorResult << {"st rattr 0x${device.deviceNetworkId} 5 0x000F 0x0055"}
        log.debug "${doorResult}"
   		def result
   		if (doorResult?.startsWith('read attr')) {
        	log.debug "Read Attribute"
     		if (doorResult?.endsWith('value: 00')) {		//contact closed
         	result = getContactResult("closed")
         	log.debug "New Garage Door Closed"
        }
     	else if (doorResult?.endsWith('value: 01')) 	{//contact opened
         	result = getContactResult("open")
         	log.debug "New Garage Door Open"
        }

        
   }
      	return result
        
}



def refresh() {  // just used for development to use.. uncomment capability above.
    def cmds =   
        zigbee.readAttribute(CLUSTER_BINARY_INPUT, ON_OFF_ATTR_BASIO) 
//    	 log.debug "refresh() --- cmds: $cmds"
    return cmds
}




private Map getContactResult(value) {
   def linkText = getLinkText(device)
   def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
   return [
      name: 'switch',
      value: value,
      descriptionText: descriptionText
	]
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
	log.debug cluster  

	return resultMap
}


private Map parseCustomMessage(String description) {
   def result
   if (description?.startsWith('read attr')) {
      if (description?.endsWith('value: 00')) {		//contact closed
         result = getContactResult("closed")
         	log.debug "Garage Door Closed"
            }
      else if (description?.endsWith('value: 01')) 	{//contact opened
         result = getContactResult("open")
         	log.debug "Garage Door Open"
            }

      return result
   }
}