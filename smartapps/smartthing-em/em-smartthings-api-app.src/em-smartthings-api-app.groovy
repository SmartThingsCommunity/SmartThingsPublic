/**
 *  My SmartThings API App
 *
 *  Copyright 2016 EM
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
    name: "EM SmartThings API App",
    namespace: "smartthing-em",
    author: "Keval Parekh",
    description: "This app will show the status of door sensors. ",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences(oauthPage: "deviceAuthorization") {
page(name: "deviceAuthorization"){
	section("Allow Endpoint to Control These Things...") {    
        input "doorsensors", "capability.contactSensor", title: "Which Door?", multiple: true, required: false 
		}
	}
}

mappings {
//Doors
 path("/doorsensors") {
		action: [
			GET: "listDoorSensors"
		]
	}
    path("/doorsensors/:id") {
		action: [
			GET: "showDoorSensor"
		]
	}
     path("/doorsensors/:id/:events") {
		action: [
			GET: "showDoorSensorEvents"
		]
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
    subscribe(doorsensors, "contact.closed", DoorSensorDeviceHandler)    
    subscribe(doorsensors, "contact.open", DoorSensorDeviceHandler)    
}

//Handler for door sensor 
def DoorSensorDeviceHandler(evt){
	log.debug "Door status changed to ${evt.value}"
    //This URL will notify user for door sensor change event
    httpPost(uri: "http://vps49294.vps.ovh.ca/fcall/webservice/smart_home/smart_home_user_notifications.php?deviceId=${evt.deviceId}&deviceValue=${evt.value}&isoDate=${evt.isoDate}") 
    {
    	resp -> log.debug "response data: ${resp.data}"
	    log.debug evt.name+" Event data successfully posted"
    }
}

//Door Sensor Methods

//This method will get all door sensors
def listDoorSensors() {
     doorsensors?.collect{[type: "door", id: it.id, name: it.displayName, status: it.currentValue('contact')]}?.sort{it.name}
}

//This method will get detail for specified door sensor
def showDoorSensor() {
	show(doorsensors, "contact")    
}

//This method will fetch the event for specified door sensor
def showDoorSensorEvents() {
	log.debug "called....."
    getEvents(doorsensors, "status")
}

private show(devices, type) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
		def attributeName = type == "motionSensor" ? "motion" : type
		def s = device.currentState(attributeName)
		[id: device.id, label: device.displayName, value: s?.value, unitTime: s?.date?.time, type: type]
	}
}

private getEvents(devices, type) {
	log.debug " events data requested.."
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
        def events = device.eventsSince(new Date() - 364)
        events = events.findAll{it.name == type}
        def result = events.collect{item(device, it)}
	    result
    }	
}

private item(device, s) {
	device && s ? [uid: s.id, device_id: device.id, label: device.displayName, name: s.name, value: s.value, date: s.date] : null
}

private device(it, type) {
	it ? [id: it.id, label: it.label, type: type] : null
}