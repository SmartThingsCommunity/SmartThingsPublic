/**
 *  Foobot Air Quality Monitor DTH
 *
 *  Copyright 2017 Michael Struck
 *  Version 2.0.0 7/28/17
 *
 *  Version 2.0.0 Re-engineered release by Michael Struck to work with Ask Alexa. Added C/F adjustment, cleaned up code, used the standard 'carbonDioxide' variable instead of CO2
 *
 *  Uses code from Adam K (V2. Updated 6/2/2017 - Updated Region so it works in UK & US)
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
        input("username", "text", title: "Username", description: "Your Foobot username (usually an email address)")
        input("uuid", "text", title: "UUID", description: "The UUID of the exact Foobot that you would like information for")
        def myOptions = ["EU", "US"]
        input "region", "enum", title: "Select your region (For API access)", defaultValue: "EU", required: true, options: myOptions, displayDuringSetup: true
        input "CF", "enum", title: "Select your temperature type", defaultValue: "°C", required: true, options: ["°C","°F"], displayDuringSetup: true
}
metadata {
	definition (name: "Foobot Air Quality Monitor", namespace: "MichaelStruck", author: "Michael Struck") {
		capability "Polling"
        capability "Refresh"
        capability "Sensor"
		capability "Relative Humidity Measurement"
        capability "Temperature Measurement"
        capability "Carbon Dioxide Measurement"
     
     	attribute "pollution", "number"
        attribute "particle", "number"
        attribute "voc", "number"
        attribute "GPIState", "String"
        attribute "carbonDioxide", "number"       
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){   
        multiAttributeTile(name:"pollution", type:"generic", width:6, height:4) {
            tileAttribute("device.pollution", key: "PRIMARY_CONTROL") {
    			attributeState("pollution", label:'${currentValue}% GPI', unit:"%", icon:"st.Weather.weather13", backgroundColors:[
                    [value: 24, color: "#1c71ff"],
                    [value: 49, color: "#5c93ee"],
                    [value: 74, color: "#ff4040"],
                    [value: 100, color: "#d62d20"]
                ])
  			}
            tileAttribute("device.GPIState", key: "SECONDARY_CONTROL") {
           		attributeState("GPIState", label:'${currentValue}')
            }
       }
        valueTile("carbonDioxide", "device.carbonDioxide", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
        	state "carbonDioxide", label:'${currentValue} CO2 ppm', unit:"ppm"
        }
        valueTile("voc", "device.voc", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "voc", label:'${currentValue} VOC ppb', unit:"ppb"
        }
        valueTile("particle", "device.particle", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "particle", label:'${currentValue} µg/m³', unit:"µg/m³ PM2.5"
        }
        valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "humidity", label:'${currentValue}% humidty', unit:"%"
        }
        valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "temperature", label:'${currentValue}°', unit:"°"
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "refresh", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("spacerlastUpdatedLeft", "spacerTile", decoration: "flat", width: 1, height: 1) {
 		}
        valueTile("lastUpdated", "device.lastUpdated", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
			state "lastUpdated", label:'Last updated:\n${currentValue}'
		}
        standardTile("spacerlastUpdatedRight", "spacerTile", decoration: "flat", width: 1, height: 1) {
 		}
        main "pollution"
        details(["pollution","carbonDioxide","voc","particle","humidity", "temperature", "refresh", "spacerlastUpdatedLeft", "lastUpdated", "spacerlastUpdatedRight"])
	}
}
private getAPIKey() {
    return "ADD YOUR API KEY HERE";
}
def parse(String description) {
	log.debug "Parsing '${description}'"
}
def refresh() { 
	poll()
}
// handle commands
def poll() {   
    def start = new Date(Calendar.instance.time.time-1800000).format("yyyy-MM-dd'T'HH:MM':00'");
    def stop = new Date(Calendar.instance.time.time+1800000).format("yyyy-MM-dd'T'HH:MM':00'");
    
    def accessToken = getAPIKey()  
    def regionVar = ""
    def params = "" 
    if (region){
    	regionVar = region
    	if (regionVar == "EU")params = "https://api.foobot.io/v2/device/${settings.uuid}/datapoint/0/last/0/?api_key=${accessToken}"
 		if (regionVar == "US")params = "https://api-us-east-1.foobot.io/v2/device/${settings.uuid}/datapoint/0/last/0/?api_key=${accessToken}"
    } 
    try {
        httpGet(params) {resp ->
			resp.headers.each {
           		log.debug "${it.name} : ${it.value}"
        	}
            // get an array of all headers with the specified key
        	def theHeaders = resp.getHeaders("Content-Length")

        	// get the contentType of the response
        	log.debug "response contentType: ${resp.contentType}"

        	// get the status code of the response
       		log.debug "response status code: ${resp.status}"

        	// get the data from the response body
        	log.debug "response data: ${resp.data}"
            log.debug "particle: ${resp.data.datapoints[-1][1]}"
            sendEvent(name: "particle", value: sprintf("%.2f",resp.data.datapoints[-1][1]), unit: "µg/m³ PM2.5")
            log.debug "tmp: ${resp.data.datapoints[-1][2]}"
            BigDecimal tmp = resp.data.datapoints[-1][2]
           	def tmpround = String.format("%5.2f",tmp)
            def temp = tmpround
            if (CF == "°F") temp = celsiusToFahrenheit(tmp) as Integer
            sendEvent(name: "temperature", value: temp, unit: "°")
            log.debug "hum: ${resp.data.datapoints[-1][3]}"
            sendEvent(name: "humidity", value: resp.data.datapoints[-1][3] as Integer, unit: "%")
            log.debug "Carbon dioxide: ${resp.data.datapoints[-1][4]}"
            sendEvent(name: "carbonDioxide", value: resp.data.datapoints[-1][4] as Integer, unit: "ppm")
            log.debug "voc: ${resp.data.datapoints[-1][5]}"
            sendEvent(name: "voc", value: resp.data.datapoints[-1][5] as Integer, unit: "ppb")
            log.debug "allpollu: ${resp.data.datapoints[-1][6]}"
            def allpollu = resp.data.datapoints[-1][6]
            sendEvent(name: "pollution", value: resp.data.datapoints[-1][6] as Integer, unit: "%")
            if (allpollu < 25) sendEvent(name: "GPIState", value: "Great", isStateChange: true)
            else if (allpollu < 50) sendEvent(name: "GPIState", value: "Good", isStateChange: true)
            else if (allpollu < 75) sendEvent(name: "GPIState", value: "Fair", isStateChange: true)
            else if (allpollu > 75) sendEvent(name: "GPIState", value: "Poor", isStateChange: true)
            def now = new Date().format("EEE, d MMM yyyy HH:mm:ss",location.timeZone)
            sendEvent(name:"lastUpdated", value: now, displayed: false)
        }
    } catch (e) {
        log.error "error: $e"
    }
}
