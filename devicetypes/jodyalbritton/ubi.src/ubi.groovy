/**
 *  Ubi
 *
 *  Copyright 2015 Jody Albritton
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
	definition (name: "Ubi", namespace: "jodyalbritton", author: "jody albritton") {
	capability "Refresh"
   	capability "Polling"
    capability "Temperature Measurement"
    capability "Relative Humidity Measurement"
    capability "Sensor"
    capability "Illuminance Measurement"
    capability "Speech Synthesis"
      
    //attributes added because capabilities do not exist 
    
    attribute "soundlevel", "enum"
    attribute "airpressure", "enum"
	
    }

	simulator {
		// TODO: define status and reply messages here
	}
    
    preferences {
        section ("Use Fahrenheit for temps...") {
             input "tempPref", "bool", title: "Farenheit",
                  description: "Temp meausurements in Fahrenheit.", defaultValue: true,
                  required: false, displayDuringSetup: true
        }
	}
  
    
    
	tiles {
		valueTile("temperature", "device.temperature", width: 1, height: 1, canChangeIcon: false) {
        	
            	state("temperature", label: '${currentValue}°', unit:units, backgroundColors: [
                
                // Celsius Color Range 
                [value: 0, color: "#153591"],
				[value: 7, color: "#1e9cbb"],
				[value: 15, color: "#90d2a7"],
				[value: 23, color: "#44b621"],
				[value: 29, color: "#f1d801"],
				[value: 33, color: "#d04e00"],
				[value: 36, color: "#bc2323"],
				// Fahrenheit Color Range
				[value: 40, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 92, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
                
                
                ]
            )
        }
        valueTile("humidity", "device.humidity", inactiveLabel: false) {
            state "default", label:'${currentValue}%', unit:"Humidity"
        }
        valueTile("illuminance", "device.illuminance", inactiveLabel: false) {
			state "luminosity", label:'${currentValue}lux', unit:"Light"
		}
        valueTile("airpressure", "device.airpressure", inactiveLabel: false) {
        	state "default", label:'${currentValue}Kpa', unit:"Air Pressure"
        }
        valueTile("soundlevel", "device.soundlevel", inactiveLabel: false) {
        	state "default", label:'${currentValue}db', unit:"Sound"
        }
        standardTile("refresh", "device.poll", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
        
        main(["temperature", "humidity","illuminance", "soundlevel", "airpressure"])
        details(["temperature", "humidity", "illuminance", "soundlevel", "airpressure", "refresh"])
        
	}
}

// parse events into attributes
    def parse(String description) {
	log.debug "Parsing '${description}'"

}



// handle commands

// send speech command 


void speak(message) {
    log.debug "Executing 'speak' using parent SmartApp for ${device.deviceNetworkId}"

    parent.speakChild(device.deviceNetworkId, message)
}


//poll the device

void poll() {
	log.debug "Executing 'poll' using parent SmartApp for ${device.deviceNetworkId}"
    def results =  parent.pollChild(device.deviceNetworkId)
    
    parseEvents(results)	
}


//Parse the events 
def parseEvents(results) {
		def temp = results.temperature.toBigDecimal()
        def curTemp = cToF(temp)
		
	 	sendEvent(name: 'temperature', value: curTemp as Integer)
        sendEvent(name: 'humidity', value: results.humidity)
        sendEvent(name: 'illuminance', value: results.light)
        sendEvent(name: 'soundlevel', value: results.soundlevel)
        sendEvent(name: 'airpressure', value: results.airpressure)
        
        log.debug "Results: ${results}"
        
        
}


  
def units = ""        
def cToF(temp) {
	if (settings.tempPref){ 
		return temp * 1.8 + 32
        units = "F"
    }else{ 
    	return temp
        units = "C"
    }
}


// Refresh Command 
def refresh() {
	log.debug "Executing 'refresh'"
	poll()

}