/**
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Tesla Smart Heater
 *
 */

definition(
    name: "Tesla-Smart-Heater",
    namespace: "jonbur",
    author: "JB",
    description: "Tesla Smart Heater",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/tesla-app%402x.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/tesla-app%403x.png")


preferences {
	section {
		input "HVACswitch", "capability.Switch", title: "Select the Tesla HVAC switch", required: true, multiple: false    
		input "presencesensor", "capability.presenceSensor", title: "Select the presence sensor", required: true, multiple: false
        input "temperaturesensor", "capability.temperatureMeasurement", title: "Select the temperature sensor", required: true, multiple: false
        input "targettime", "time", title: "Time to execute every target day"
        input "minimumtemperature", "number", title: "Enter minimum temperature", defaultValue:10
        input "days", "enum", title: "Select Days of the Week", required: true, multiple: true, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}
 
def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
    schedule(targettime, eventHandler)
}

def eventHandler() {
    log.debug "event handler"
    log.debug temperaturesensor.currentValue("temperature")
    log.debug presencesensor.currentValue("presence")

    //If it is a target day and temperature less than or equal to target temperature and Tesla present then turn on heater
    
    def df = new java.text.SimpleDateFormat("EEEE")
    // Ensure the new date object is set to local time zone
    df.setTimeZone(location.timeZone)
    def day = df.format(new Date())
    //Does the preference input Days, i.e., days-of-week, contain today?
    def dayCheck = days.contains(day)
    if (dayCheck) {
        log.debug "Today is one of the target days"
        if (temperaturesensor.currentValue("temperature") <= minimumtemperature){
            log.debug "Temperature less than minimum"
            if (presencesensor.currentValue("presence") == "present"){
                log.debug "Car is present"
                log.debug "Turn on the HVAC"
                sendPush("Turning on Tesla HVAC")
                HVACswitch.on()
            } else
                log.debug "Car not present"
        } else {
            log.debug "Temperature more than minimum"
        }
    } else {
    	log.debug "Today is not one of the target days. Nothing to do."
    }
    

    
    
}