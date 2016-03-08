/**
 *  Xiaomi Smart Button
 *
 *  Copyright 2015 Eric Maycock
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
	definition (name: "Xiaomi Smart Button", namespace: "erocm123", author: "Eric Maycock") {
		capability "Button"
        capability "Configuration"
		capability "Sensor"
        capability "Refresh"
        
        attribute "lastPress", "string"

	}
    
    simulator {
   	  status "button 1 pressed": "on/off: 0"
      status "button 1 released": "on/off: 1"
    }
    
    preferences{
    	input ("holdTime", "number", title: "Minimum time in seconds for a press to count as \"held\"",
        		defaultValue: 4, displayDuringSetup: false)
    }

	tiles(scale: 2) {
    	standardTile("button", "device.button", decoration: "flat", width: 2, height: 2) {
        	state "default", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
	}

		main (["button"])
		details(["button","refresh"])
	}
}

def parse(String description) {
	log.debug "Parsing '${description}'"
  def descMap = zigbee.parseDescriptionAsMap(description)
	def results = []
  if (description?.startsWith('on/off: '))
		results = parseCustomMessage(description)
	return results;
}

def configure(){
	refresh()
}

def refresh(){
}

private Map parseCustomMessage(String description) {
	if (description?.startsWith('on/off: ')) {
    	if (description == 'on/off: 0') 		//button pressed
    		createPressEvent(1)
    	else if (description == 'on/off: 1') 	//button released
    		createButtonEvent(1)
	}
}

//this method determines if a press should count as a push or a hold and returns the relevant event type
private createButtonEvent(button) {
	def currentTime = now()
    def startOfPress = device.latestState('lastPress').date.getTime()
    def timeDif = currentTime - startOfPress
    def holdTimeMillisec = (settings.holdTime?:3).toInteger() * 1000
    
    if (timeDif < 0) 
    	return []	//likely a message sequence issue. Drop this press and wait for another. Probably won't happen...
    else if (timeDif < holdTimeMillisec) 
    	return createButtonPushedEvent(button)
    else 
    	return createButtonHeldEvent(button)
}

private createPressEvent(button) {
	return createEvent([name: 'lastPress', value: now(), data:[buttonNumber: button], displayed: false])
}

private createButtonPushedEvent(button) {
	log.debug "Button ${button} pushed"
	return createEvent([
    	name: "button",
        value: "pushed", 
        data:[buttonNumber: button], 
        descriptionText: "${device.displayName} button ${button} was pushed",
        isStateChange: true, 
        displayed: true])
}

private createButtonHeldEvent(button) {
	log.debug "Button ${button} held"
	return createEvent([
    	name: "button",
        value: "held", 
        data:[buttonNumber: button], 
        descriptionText: "${device.displayName} button ${button} was held",
        isStateChange: true])
}