/**
 *  My SmartThings API App
 *
 *  Copyright 2016 Ravi Dubey
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
    name: "My SmartThings API App",
    namespace: "Ravi-em",
    author: "Ravi Dubey",
    description: "This will handle list & switches. ",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences(oauthPage: "deviceAuthorization") {
page(name: "deviceAuthorization"){
	section("Allow Endpoint to Control These Things...") {    
		input "watersensors", "capability.waterSensor", title: "Which Water Sensor?", multiple: true, required: false
        input "doorsensors", "capability.contactSensor", title: "Which Door?", multiple: true, required: false 
        //input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
		//input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false        
		//input "temperatures", "capability.temperatureMeasurement", title: "Which Temperatures?", multiple: true, required: false 
        //input "thermostats", "capability.thermostat", title: "Which Themostats?", multiple: true, required: false
        input "motions", "capability.motionSensor", title: "Which Motions?", multiple: true, required: false
		}
	}
}

mappings {

//Water Sensors
    path("/watersensors") {
		action: [
			GET: "listWaterSensors"
		]
	}
    path("/watersensors/:id") {
		action: [
			GET: "showWaterSensor"
		]
	}
     path("/watersensors/:id/events") {
		action: [
			GET: "showWaterSensorEvents"
		]
	}
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
     path("/doorsensors/:id/events") {
		action: [
			GET: "showDoorSensorEvents"
		]
	}

//Swiches      
    path("/switches") {
		action: [
			GET: "listSwitches"
		]
	}
	path("/switches/:id") {
		action: [
			GET: "showSwitch"
		]
	}
	path("/switches/:id/:command") {
		action: [
			GET: "updateSwitch"
		]
	}
//locks    
    path("/locks") {
		action: [
			GET: "listLocks"
		]
	}
	path("/locks/:id") {
		action: [
			GET: "showLock"
		]
	}
	path("/locks/:id/:command") {
		action: [
			GET: "updateLock"
		]
	}  
	
// Sensors
 
    path("/temperatures") {
		action: [
			GET: "listTemperatures"
		]
	}
	path("/temperatures/:id") {
		action: [
			GET: "showTemperature"
		]
	}
    path("/temperatures/:id/:command") {
		action: [
			GET: "updateTemperatures"
		]
	}
    

// Thermostats

	path("/thermostats") {
		action: [
			GET: "listThermostats"
		]
	}
	path("/thermostats/:id") {
		action: [
			GET: "showThermostat"
		]
	}  

	path("/thermostats/:id/:command/:temp") {
		action: [
			GET: "updateThermostat"
		]
	}
	
//Motions

path("/motions") {
		action: [
			GET: "listMotions"
		]
	}
	path("/motions/:id") {
		action: [
			GET: "showMotion"
		]
	}
     path("/motions/:id/events") {
		action: [
			GET: "showMotionEvents"
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
    subscribe(switches, "switch", deviceHandler)    
    subscribe(locks, "lock", lockDeviceHandler)    
    subscribe(doorsensors, "contact.closed", DoorSensorDeviceHandler)    
    subscribe(doorsensors, "contact.open", DoorSensorDeviceHandler)    
}

def deviceHandler(evt) {
    //locks.lock()
    
    logField(evt) { it.toString() }  
    
}

def lockDeviceHandler(evt) {
    //locks.lock()
    log.debug "lock status changed to ${evt.value}"
    
    httpPost(uri: "http://vps49294.vps.ovh.ca/sample_charts/api-event-handler.php?deviceName=${evt.name}&deviceId=${evt.id}&deviceValue=${evt.value}") {resp ->
log.debug "response data: ${resp.data}"
    log.debug evt.name+" Event data successfully posted"
    }
}



def DoorSensorDeviceHandler(evt){
	log.debug "Door status changed to ${evt.value}"
    
    httpPost(uri: "http://vps49294.vps.ovh.ca/sample_charts/api-event-handler.php?deviceName=${evt.name}&deviceId=${evt.id}&deviceValue=${evt.value}") {resp ->
log.debug "response data: ${resp.data}"
    log.debug evt.name+" Event data successfully posted"
    }
}

def WaterSensorDeviceHandler(evt)
{
	log.debug "Water Sensor Status Cchanged to ${evt.value}"
    
}

private logField(evt, Closure c) {
	if(evt.value=='on')
    	locks.unlock()
    else
    	locks.lock()
    
    log.debug "http://vps49294.vps.ovh.ca/sample_charts/api-event-handler.php?deviceName=${evt.displayName}&deviceId=${evt.deviceId}&deviceValue=${evt.value}"
    
	//httpPut("http://vps49294.vps.ovh.ca/sample_charts/api-event-handler.php?data=mydata", "data") { resp ->
    //httpPostJson(uri: "http://vps49294.vps.ovh.ca/sample_charts/api-event-handler.php?data=mydata",   body:[device: evt.deviceId, name: evt.name, value: evt.value, date: evt.isoDate, unit: evt.unit]) {resp ->
    httpPost(uri: "http://vps49294.vps.ovh.ca/sample_charts/api-event-handler.php?deviceName=${evt.displayName}&deviceId=${evt.deviceId}&deviceValue=${evt.value}") {resp ->
log.debug "response data: ${resp.data}"
        log.debug evt.name+" Event data successfully posted"
    }
}
//Door Sensor Methods

def listDoorSensors() {
     doorsensors?.collect{[type: "door", id: it.id, name: it.displayName, status: it.currentValue('contact')]}?.sort{it.name}
}

def showDoorSensor() {
	show(doorsensors, "door")    
}

def showDoorSensorEvents() {
    getEvents(doorsensors, "door")
}

//Water Sensor Methods
def listWaterSensors() {
     watersensors?.collect{[type: "water", id: it.id, name: it.displayName, status: it.currentValue('water')]}?.sort{it.name}
}

def showWaterSensor() {
	show(watersensors, "water")    
}

def showWaterSensorEvents() {
    getEvents(watersensors, "water")
}

//switches
def listSwitches() {
	switches.collect{device(it,"switch")}
}

def showSwitch() {
	show(switches, "switch")
}
void updateSwitch() {
	update(switches)
}

//locks
def listLocks() {
	locks.collect{device(it,"lock")}
}

def showLock() {
	show(locks, "lock")
}

void updateLock() {
	update(locks)
}


//Temp sensors
def listTemperatures() {
	temperatures?.collect{[type: "temperatureMeasurement", id: it.id, name: it.displayName, status: it.currentValue('temperature')]}?.sort{it.name}

}
def showTemperature() {
	show(temperatures, "temperature")
}



//thermostats

def listThermostats() {
	thermostats.collect{device(it,"thermostat")}
}

def showThermostat() {
	show(thermostats, "thermostat")
}

void updateThermostat() {

	def device = thermostats.find { it.id == params.id }
	def command = params.command
	def temp = params.temp

    log.debug "$command ${params.id} at $temp"

	if(command == 'heat')
	{
		device.setHeatingSetpoint(temp)
	}
	else if(command == 'cool')
	{
	  device.setCoolingSetpoint(temp)	
	}
}

// Motions methods

def listMotions() {
     motions?.collect{[type: "motion", id: it.id, name: it.displayName, status: it.currentValue('motion')]}?.sort{it.name}

}
def showMotion() {
	show(motions, "motion")
    
}

def showMotionEvents() {
    getEvents(motions, "motion")
}

private getEvents(devices, type) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
        def events = device.events(max: 40)
         events = events.findAll{it.name == type}
        def result = events.collect{item(device, it)}
	    result
    }	
}


private void update(devices) {
	log.debug "update, request: params: ${params}, devices: $devices.id"
    
    
	//def command = request.JSON?.command
    def command = params.command
    //let's create a toggle option here as well
	if (command) 
    {
		def device = devices.find { it.id == params.id }
		if (!device) {
			httpError(404, "Device not found")
		} else {
        	if(command == "toggle")
       		{
            	if(device.currentValue('switch') == "on")
                  device.off();
                else
                  device.on();
       		}
       		else
       		{
				device."$command"()
            }
		}
	}
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


private device(it, type) {
	it ? [id: it.id, label: it.label, type: type] : null
}