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
	definition (name: "Particle Garage", namespace: "PARTICLE", author: "MikeD") {
		capability "switch"
        capability "Refresh"
        capability "Momentary"        
        capability "Polling"
        capability "Contact Sensor"
        capability "Temperature Measurement"

        
		attribute "contact1", "string"
		attribute "temp", "string"
	}
    preferences {
    input("deviceId", "text", title: "Device ID")
    input("token", "text", title: "Access Token")
	}

	tiles {
		standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: 'Push', action: "momentary.push", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Push', action: "momentary.push", backgroundColor: "#53a7c0"
		}
		standardTile("contact1", "device.contact1", width: 1, height: 1) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
		} 
        standardTile("refresh", "device.Refresh", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
		valueTile("temperature", "device.temperature", width: 1, height: 1){
            state "temperature", label: '${currentValue}°F', unit:"",
            	backgroundColors: [
                    [value: 25, color: "#202040"],
                    [value: 30, color: "#202080"]
                ]
		}
        
        valueTile("humidity", "device.humidity", width: 1, height: 1){
            state "humidity", label: '${currentValue}%', unit:"",
            	backgroundColors: [
                    [value: 50, color: "#202040"],
                    [value: 80, color: "#202080"]
                ]
		}
		main "refresh"
		details([ "switch","refresh","contact1","temperature","humidity"])
	}

	
}

def poll() {
	log.debug "Executing 'poll'"
    getContact()
    getTemperature()
}
def parse(String description) {
	log.error "This device does not support incoming events"
	return null
}

def push() {
    sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
	sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
    put 'garage'

}


def put(action) {
	 httpPost(
  			uri: "https://api.particle.io/v1/devices/${deviceId}/relay",
        	body: [access_token: token, command: action],  
 	) {response -> log.debug (response.data)}

}


def getContact() {
    int currentState;
    def ContactValue = { response ->  
        currentState = response.data.return_value
        log.debug "Current state, $currentState"        

	}

	def ReturnContact = [ uri: "https://api.particle.io/v1/devices/${deviceId}/contact",
        body: [access_token: token],
        success: ContactValue	
    ]
    httpPost(ReturnContact)
    if(currentState == 1){
		sendEvent(name: "contact1", value: "open")
        log.debug "Setting contact to open"        
    }
    else{
		sendEvent(name: "contact1", value: "closed")
        log.debug "Setting contact to closed"
    }

}

private getTemperature() {
    //Spark Core API Call
    def temperatureClosure = { response ->
	  	log.debug "Temeprature Request was successful, $response.data"
      
      	sendEvent(name: "temperature", value: response.data.return_value)
	}
    
    def temperatureParams = [
  		uri: "https://api.spark.io/v1/devices/${deviceId}/getTemp",
        body: [access_token: token],  
        success: temperatureClosure
	]

	httpPost(temperatureParams)
    
    def humidityClosure = { response ->
	  	log.debug "Humidity Request was successful, $response.data"
      
      	sendEvent(name: "humidity", value: response.data.return_value)
	}
    
    def humidityParams = [
  		uri: "https://api.spark.io/v1/devices/${deviceId}/getHum",
        body: [access_token: token],  
        success: humidityClosure
	]

	httpPost(humidityParams)
}