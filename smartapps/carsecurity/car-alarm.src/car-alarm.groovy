/**
 *  Copyright 2017 Greg & SmartThings
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
 *  Ridiculously Automated Garage Door
 *
 *  Author: Greg
 *  Date: 2017-06-10
 *
 * Monitors departure of the car and notifies if the driver didn't depart with it ;)
 *
 */

definition(
    name: "Car Alarm",
    namespace: "carsecurity",
    author: "Greg",
    description: "Monitors departure of the car and notifies if the driver didn't depart with it ;)",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Transportation/transportation8-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation8-icn@2x.png"
)

preferences {

	section("Car and Driver") {
		input "car", "capability.presenceSensor", title: "Car Presence sensor", description: "Which car?", multiple: false, required: false
		input "driver", "capability.presenceSensor", title: "Driver Presence sensor", description: "Which Driver?", multiple: false, required: false
		
	}
	section("False alarm threshold (optional, defaults to 130 seconds)") {
		input "falseAlarmThreshold", "number", title: "Number of seconds", required: false
	}
	section("Minimum time between messages (optional, defaults to 10 minutes)") {
		input "frequency", "decimal", title: "Minutes", required: false
	}
}

def installed() {
	log.trace "installed()"
	subscribe()
}

def updated() {
	log.trace "updated()"
	unsubscribe()
	subscribe()
}

def subscribe() {
	subscribe(car, "presence", carPresence)
	//subscribe(drivers, "presence.present", driverPresence)
	
}

def carPresence(evt)
{
	//log.info "$evt.name: $evt.value"
	// time in which there must be no "not present" events in order to open the door
	
	

	final carAlarmInterval = falseAlarmThreshold ? falseAlarmThreshold : 130

	if (evt.value == "present") {
		log.trace("present");	
	}
	else {
		log.trace("car not present");
		if(driver.currentPresence  == "present"){
        
			def car = getCar(evt)

			if (frequency) {
				def lastTime = state[evt.deviceId]
				if (lastTime == null || now() - lastTime >= frequency * 60000) {
					sendMessage(car.displayName)
				}
			}
			else {
				sendMessage(car.displayName)
			}

		}
		
	}
}

private sendMessage(carName)
{	
	Map options = [:]
	String msg  = "${carName} HAS DEPARTED WITHOUT THE DRIVER!"
	log.trace "sendMessage()"
	
	options.method = 'push'
	sendNotification(msg, options)
}

private getCar(evt)
{
	car.find{it.id == evt.deviceId}
}