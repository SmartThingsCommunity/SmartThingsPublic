/**
 *  Thermo-based Humidistat
 *
 *  Copyright 2017 Bill Bondy
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
    name: "Thermo-based Humidistat",
    namespace: "Bondys",
    author: "Bill Bondy",
    description: "Use your thermostat himidity sensor to automatically turn on your A/C when at or above a specific himidity level.",
    category: "Convenience",
   iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather2-icn",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather2-icn?displaySize=2x"
)


preferences { 
  page(name: "page1", title: "Set humidity sensor and thermostat", install:true, uninstall: true){ 
	 section("Give this app a name?") {
     label(name: "label",
     title: "Give this app a name?",
     required: false,
     multiple: false)
	}
    
    section("Humidistat On/Off:") {
    	input(name: "humState", type: "enum", defaultValue: "Off", title: "On/Off", options: ["On","Off"])
    }
	section("Humidity measurement supplied by:") {
		input ("humidityInput", "capability.relativeHumidityMeasurement", required: true)
	}   
    section("Thermostat to control:") {
    	input ("thermo", "capability.thermostat", required: true)
    }
    section("Max allowed humidity") {
        input ("humMax", "number", range: "50..70", type: "decimal", defaultValue: "60", title: "Desired maxiumum humidity,  default is 60%.", required: false)
    }
    section("Humidity On/Off offset") {
        input ("humrange", "number", range: "1..10", type: "decimal", defaultValue: "1", title: "Delta from max allowed humidity where A/C will be turned On/Off, default is 1%.", required: false)
    }
    section("Thermostat A/C Temperature") {
        input ("thermoTemp", "number", range: "72..88", type: "decimal", defaultValue: "80", title: "A/C Temperature, default is 80.", required: false)
    }
  }
	log.debug("preferences")
}

        
def installed() {
    log.debug("installed")
}

def updated() {
    log.debug("updated")
	unsubscribe()
    if (settings.humState == "On") {
    	runEvery5Minutes(humidityActivate)
   		humidityActivate()
    } else {
      log.debug("Humidistat Off")
 	  log.debug("Set AC Mode to: off")
  	  thermo.setThermostatMode("off")
    }
}    

     
def humidityActivate(evt) {  
  log.debug("humidityActivate")
    
    def currentHumidity = settings.humidityInput?.latestValue("humidity")
    if (currentHumidity) {
    }
    else {
    currentHumidity = (10).toDouble() // should never get here, this would essentially turn the humidistat off
    }
  
    log.debug("currentHumidity: "+currentHumidity+" humrange: "+settings.humrange+" humMax: "+settings.humMax+" thermostatMode: "+thermo.currentThermostatMode+" setThermoTemp: "+settings.thermoTemp)
	if ((currentHumidity >= (settings.humrange+settings.humMax)) && (thermo.currentThermostatMode=="cool")) {
    	if (thermo.currentCoolingSetpoing != settings.thermoTemp) {
            log.debug("Set AC Temm to: "+settings.thermoTemp)
    		thermo.setCoolingSetpoint(settings.thermoTemp)
        	pause(3000)	
        }
    }
	else if ((currentHumidity >= (settings.humrange+settings.humMax)) && (thermo.currentThermostatMode=="off")) {
    	log.debug("Set AC Temm to: "+settings.thermoTemp)
    	thermo.setCoolingSetpoint(settings.thermoTemp)
        pause(3000)
        log.debug("Set AC Mode to: cool")
        thermo.setThermostatMode("cool")
        pause(3000)
    }
    else if ((currentHumidity <= (settings.humMax-settings.humrange)) && (thermo.currentThermostatMode=="cool")) {
        log.debug("Set AC Mode to: off")
    	thermo.setThermostatMode("off")
        pause(3000)
    }
}
 
 