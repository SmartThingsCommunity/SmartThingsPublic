/**
 *  Foobot Air Quality Monitor DTH
 *
 *  Copyright 2018 Michael Struck
 *  Precision code additions and other UI-Barry Burke
 * 
 *  Version 3.0.1 1/24/18
 *
 *  Version 2.0.0 (6/2/17) AdamV Release: Updated Region so it works in UK & US
 *  Version 3.0.0 (8/1/17) Re-engineered release by Michael Struck. Added C/F temperature units, cleaned up code and interface, adding a repoll timer, removed username
 *  used the standard 'carbonDioxide' variable instead of CO2, GPIstate instead of GPIState (for the activity log), set colors for Foobot recommended levels of attributes.
 *  Version 3.0.1 (1/24/18) Precision code additions and other UI-Barry Burke(@storageanarchy)
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
        input "uuid", "text", title: "UUID", description: "The UUID of the Foobot device", required: true
        input "region", "enum", title: "Select your region (For API access)", defaultValue: "US", required: true, options: ["EU", "US"], displayDuringSetup: true
        input "CF", "enum", title: "Temperature units", defaultValue: "°F", options: ["°C","°F"], displayDuringSetup: true
        input "refreshRate", "enum", title: "Data refresh rate", defaultValue: 0, options:[0: "Never" ,10: "Every 10 Minutes", 30: "Every 1/2 hour", 60 : "Every Hour", 240 :"Every 4 hours",
        	360: "Every 6 hours", 720: "Every 12 hours", 1440: "Once a day"], displayDuringSetup: true
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
        attribute "GPIstate", "String"
        attribute "carbonDioxide", "number"       
	}
	simulator {
		// TODO: define status and reply messages here
	}
	tiles (scale: 2){   
        multiAttributeTile(name:"pollution", type:"generic", width:6, height:4) {
            tileAttribute("device.pollution", key: "PRIMARY_CONTROL") {
    			attributeState("pollution", label:'${currentValue} GPI', unit:"%", icon:"st.Weather.weather13", backgroundColors:[
                    [value: 24, color: "#1c71ff"],
                    [value: 49, color: "#5c93ee"],
                    [value: 74, color: "#ff4040"],
                    [value: 100, color: "#d62d20"]
                ])
  			}
            tileAttribute("device.GPIupdated", key: "SECONDARY_CONTROL") {
           		attributeState("GPIupdated", label:'${currentValue}')
            }
		}
        valueTile("carbonDioxide", "device.carbonDioxide", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
        	state "carbonDioxide", label:'${currentValue}\nCO₂ ppm', unit:"ppm",backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 625, color: "#44b621"],
                    [value: 1300, color: "#f1d801"],
                    [value: 1925, color: "#bc2323"]
                ]
        }
        valueTile("voc", "device.voc", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "voc", label:'${currentValue}\nVOC ppb', unit:"ppb",backgroundColors:[
                    [value: 0, color: "#90d2a7"],
                    [value: 150, color: "#44b621"],
                    [value: 300, color: "#f1d801"],
                    [value: 450, color: "#bc2323"]
                ]
        }
        valueTile("particle", "device.particle", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "particle", label:'${currentValue}\nµg/m³', unit:"µg/m³ PM2.5",backgroundColors:[
                     [value: 0, color: "#90d2a7"],
                    [value: 12, color: "#44b621"],
                    [value: 25, color: "#f1d801"],
                    [value: 37, color: "#bc2323"]
                ]
        }
         valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "temperature", label:'${currentValue}°', unit:"°", backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"],
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]
		}
        
        valueTile("humidity", "device.humidity", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false, canChangeBackground: false) {
			state "humidity", label:'${currentValue}%', unit:"%",
            	backgroundColors:[
					[value:  0, color: "#0033cc"],
                    [value: 100, color: "#ff66ff"]
				]
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "refresh", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        valueTile("refreshes", "device.refreshes", inactiveLable: false, decoration: "flat", width: 4, height: 1) {
			state "refreshes", label:'Refreshes Remaining Today: ${currentValue}'
		}
        standardTile("spacerlastUpdatedLeft", "spacerTile", decoration: "flat", width: 1, height: 1) {
 		}
        standardTile("spacerlastUpdatedRight", "spacerTile", decoration: "flat", width: 1, height: 1) {
 		}
        main "pollution"
        details(["pollution","carbonDioxide","voc","particle","temperature", "humidity", "refresh","spacerlastUpdatedLeft", "refreshes","spacerlastUpdatedRight"])
	}
}
private getAPIKey() {
    return "ENTER YOUR API KEY HERE (KEEP THE QUOTATION MARKS)"
}
def parse(String description) {
	log.debug "Parsing '${description}'"
}
def refresh() { 
	poll()
}
// handle commands
def poll() {
    if (uuid){
        def refreshTime =  refreshRate ? (refreshRate as int) * 60 : 0
        if (refreshTime > 0) {
            runIn (refreshTime, poll)
            log.debug "Data will repoll every ${refreshRate} minutes"   
        }
        else log.debug "Data will never repoll" 
        def accessToken = getAPIKey()  
        def params = region == "EU" ? "https://api.foobot.io/v2/device/${uuid}/datapoint/0/last/0/?api_key=${accessToken}" : "https://api-us-east-1.foobot.io/v2/device/${uuid}/datapoint/0/last/0/?api_key=${accessToken}"
        try {
            httpGet(params) {resp ->
                resp.headers.each {
                    log.debug "${it.name} : ${it.value}"
                    if (it.name=="X-API-KEY-LIMIT-REMAINING") sendEvent(name: "refreshes", value:it.value, isStateChange: true)	
                }
                // get the contentType of the response
                log.debug "response contentType: ${resp.contentType}"
                // get the status code of the response
                log.debug "response status code: ${resp.status}"
                if (resp.status==200){
                    // get the data from the response body
                    log.debug "response data: ${resp.data}"
                    
                    def parts = resp.data.datapoints[-1][1].toDouble().round(2)
                    log.debug "Particle: ${parts}"
                    sendEvent(name: "particle", value: sprintf("%.2f",parts), unit: "µg/m³ PM2.5", isStateChange: true)
                     
                    def tmp = resp.data.datapoints[-1][2].toDouble()
                    def temp = ((CF == "°F") ? celsiusToFahrenheit(tmp) : tmp ).toDouble().round(1)
                    log.debug "Temperature: ${temp}${CF}"
                    sendEvent(name: "temperature", value: temp as Double, unit: "°", isStateChange: true)
                    
                    def hum = resp.data.datapoints[-1][3].toDouble().round(0)
                    log.debug "Humidity: ${hum}%"
                    sendEvent(name: "humidity", value: hum, unit: "%", isStateChange: true)
                    
                    log.debug "Carbon dioxide: ${resp.data.datapoints[-1][4]}"
                    sendEvent(name: "carbonDioxide", value: resp.data.datapoints[-1][4] as Integer, unit: "ppm", isStateChange: true)
                    
                    log.debug "Volatile Organic Compounds: ${resp.data.datapoints[-1][5]}"
                    sendEvent(name: "voc", value: resp.data.datapoints[-1][5] as Integer, unit: "ppb", isStateChange: true)
                    
                    def allpollu = resp.data.datapoints[-1][6].toDouble().round(0)
                    log.debug "Pollution: ${allpollu}"
                    sendEvent(name: "pollution", value: allpollu, unit: "GPI", isStateChange: true)
                    
                    def GPItext 
                    if (allpollu < 25) GPItext="GREAT"
                    else if (allpollu < 50) GPItext="GOOD"
                    else if (allpollu < 75) GPItext="FAIR"
                    else if (allpollu > 75) GPItext="POOR"
                    sendEvent(name: "GPIstate", value: GPItext, isStateChange: true)
                    def now = new Date().format("EEE, d MMM yyyy HH:mm:ss",location.timeZone)
                    sendEvent(name:"lastUpdated", value: now, displayed: false, isStateChange: true)
                    sendEvent(name:"GPIupdated", value:GPItext +' - Last Updated: ' + now, isStateChange: true)
          		}
            	else if (resp.status==429) log.debug "You have exceeded the maximum number of refreshes today"	
                else if (resp.status==500) log.debug "Foobot internal server error"
            }
        } catch (e) {
            log.error "error: $e"
        }
	}
    else log.debug "The device indentifier (UUID) is missing from the device settings"
}
