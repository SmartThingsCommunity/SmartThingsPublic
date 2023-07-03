/**
 *  RealApplication_CA
 *
 *  Copyright 2017 Venkata Kishore Chilakala
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
    name: "RealApplication_CA",
    namespace: "CA",
    author: "Venkata Kishore Chilakala",
    description: "myApp",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Allow External Service to Control These Things...") 
    {
    	
    	input "motions", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
        input "contacts", "capability.contactSensor",title: "Which Contact Sensor?", multiple: true, required: false
        input "presences", "capability.presenceSensor",title: "Which Presence Sensor?", multiple: true, required: false
        input "switches", "capability.switchLevel", title: "Which Switches?", multiple: true, required: false   
     	input "levelSwitches","capability.switchLevel",title: "Which Level Switch?",multiple: true, required:false  
       	input "relativeHumidity","capability.relativeHumidityMeasurement",title: "Humidity",multiple:true,required:false
      	input "temperature","capability.temperatureMeasurement",title: "Temperature Measurement",multiple:true,required:false
        input "lock","capability.lock",title: "Door Lock", multiple:true,required:false
        input "hubs",title: "Hubs",multiple:false,required:false
        }
    
}

def installed() {
	initialize()
}

def updated() {  
	initialize()
}

mappings {

path("/devices")
{
	action:[
		GET:"listDevices"
	]
}

path("/hubs") {
	log.debug "list action"
		action: [      	
			GET: "listAll"
		]
	}

 path("/switches") {
    action: [
      GET:"listSwitches",
      PUT:"updateSwitches"
    ]
  } 
  path("/switches/:id") {
	action: [
		GET:"showSwitch",
		PUT:"updateSwitch"
	]
  }
   path("/levelSwitches") {
    action: [
      GET:"listLevelSwitches",
      PUT:"updateLevelSwitches"
    ]
  } 
  path("/levelSwitches/:id") {
	action: [
		GET:"showLevelSwitch",
		PUT:"updateLevelSwitch"
	]
  }
   path("/motions") {
   
    action: [
      GET:"listMotions",
      PUT:"updateMotions"
   	]
  }
  path("/motions/:id"){
    action:[
    	GET:"showMotion",
        PUT:"updateMotion"
    ]
   }
   path("/presences")
 		{
 	 action:[
    	GET:"listPresenceSensor",
        PUT:"updatePresenceSensor"
        ]
 }
  path("/presences/:id")
       {
     action:[
    	GET:"showPresence",
        PUT:"updatePresence"
       ]
  }
  path("/locks") {
    action: [
      GET:"listLocks",
      PUT:"updateLocks"
    ]
  }
  
  path("/locks/:id") {
	action: [
		GET:"showLock",
		PUT:"updateLock"
	]
  }
 path("/contacts")
 	{
    action: [
      GET: "listContacts",
      PUP: "updateContacts"
    	]
   }
 path ("/contacts/:id")
 {
	 action:[
 		GET:"showContact",
        PUT:"updateContact"
    ]
 }
 }
 
 def listAll()
 {
   [location_id :location.id,hub_id:location.hubs*.id,hub_type:location.hubs*.type,hub_name:location.hubs*.name,hub_firm_ver:location.hubs*.firmwareVersionString,hub_ip:location.hubs*.localIP,hub_port:location.hubs*.localSrvPortTCP]
   
 }
 
 
 
def listSwitches() {
	switches.collect { device(it,"switch") }
}
void updateSwitches() {
	updateAll(switches)
}
def showSwitch() {
	show(switches, "switch")
}
void updateSwitch() {
	update(switches)
}
def listLevelSwitches() {

	levelSwitches.collect { device(it,"level") }
   // motions.collect { device(it,"motion") }
}
void updateLevelSwitches() {
	updateAll(levelSwitches)
}
def showLevelSwitch() {
	show(levelSwitches, "level")
}
void updateLevelSwitch() {
	update(levelSwitches)
}

def listLocks() {
	lock.collect { device(it,"lock") }
}
void updateLocks() {
	updateAll(lock)
}
def showLock() {
	show(lock, "lock")
}
void updateLock() {
	update(lock)
}

def listMotions() {
	motions.collect { device(it,"motion") }
}
void updateMotions()
{
	updateAll(motions)
}
def showMotion()
{
	show(motions, "motion")
}
void updateMotion()
{
	update(motions)
}

def listPresenceSensor()
{
	presences.collect{device(it,"presence")}
}
void updatePresenceSensor()
{	
	updateAll(presences)
}
def showPresence()
{
	show(presences,"presence")
}
void updatePresence()
{
	update(presences)
}

def listContacts(){

	contacts.collect{device(it,"contact")}
}
void updateContacts()
{
	updateAll(contacts)
}
def showContact()
{
	show(contacts,"contact")
}
void updateContact()
{
	update(contacts)
}



private device(it, name) {
	if (it) {
		def s = it.currentState(name)
		[
  id: it.id,
  name: it.displayName,
  label: it.displayName,
  hub_name: it.hub.name,
  hub_id: it.hub.id,
  hub_ip: it.hub.localIP,
  hub_port: it.hub.localSrvPortTCP,
  state: s
]
    }
}
private void updateAll(devices) {
	def command = request.JSON?.command
	if (command) {
		devices."$command"()
	}
}
private show(devices, name) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
    	log.debug " state s :: ${s}"
        log.debug "devices :: ${devices}"
        log.debug "name :: ${name}"
		def s = device.currentState(name)
		[id: device.id, label: device.displayName, name: device.displayName,state:s]
	}
}
private void update(devices) {
	log.debug "update, request: ${request.JSON}, params: ${params}, devices: $devices.id"
	def command = request.JSON?.command
    def value = request.JSON?.value
    
    log.debug "$value" 
    log.debug "$command"
    
	if (command) {
		def device = devices.find { it.id == params.id }
		if (!device) {
			httpError(404, "Device not found")
		} else {
       
        	if("$command"=="on")
            {
               device."$command"()
            }else if("$command"=="off")
            {
               device."$command"()
            }else if("$command"=="lock")
            {
               device."$command"()
            }else if("$command"=="unlock")
            {
               device."$command"()
            }else
            {
           	   device.setLevel("$command")
            }
		}
	}
}
def initialize() {
	
}