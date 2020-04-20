/**
 *  ControlAny-Support
 *
 *  Copyright 2017 ControlAny 
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
    name: "ControlAny-Support",
    namespace: "controlany-support",
    author: "developer@controlany.com",
    description: "ControlAny-Support",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")



preferences {
    section("Allow External Service to Control These Things...") 
    {
       	input "motions", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false
        input "contacts", "capability.contactSensor",title: "Contact Sensors", multiple: true, required: false
        input "presences", "capability.presenceSensor",title: "Presence Sensors", multiple: true, required: false
        input "switches", "capability.switch", title: "Switches ", multiple: true, required: false   
      	input "levelSwitches","capability.switchLevel",title: "Level Switches",multiple: true, required:false  
       	input "relativeHumidity","capability.relativeHumidityMeasurement",title: "Humidity",multiple:true,required:false
      	input "temperatures","capability.temperatureMeasurement",title: "Temperature Measurement",multiple:true,required:false
        input "batteries","capability.battery",title:"Battery",multiple:true,required:false 
        input "waters","capability.waterSensor",title:"Water Sensors",multiple:true,required:false
        input "locks","capability.lock",title: "Door Lock", multiple:true,required:false
        input "hubs",title: "Hubs",multiple:false,required:false
     }
    
}

def installed() {
	initialize()
  	subscribeToEvents()
}

def updated() {  
	//unsubscribe()
	initialize()
  	unsubscribe()
  	subscribeToEvents()
}


def subscribeToEvents() {
  subscribe(contacts, "contact", sendEvent)
  subscribe(motions, "motion", sendEvent)
  subscribe(switches, "switch", sendEvent)
  subscribe(presences, "presence", sendEvent)
  subscribe(temperatures, "temperature", sendEvent)
  subscribe(batteries, "battery", sendEvent)
  subscribe(waters, "water", sendEvent)
 
}

String toQueryString(Map m) {
        return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def sendEvent(evt)
{
     log.debug "Notify got evt ${evt.device}"  
     log.debug "Device Id is $evt.deviceId"
     log.debug "Device displayName is $evt.displayName"
     log.debug "Device Name is $evt.name"
     log.debug "Device Value is $evt.value"
    // log.debug "The jsonValue of this event is $evt.jsonValue"
  	 def postParams = [
           	deviceId: evt.deviceId,
            value : evt.value,
            displayName :evt.displayName,
            isStateChange:evt.isStateChange,
            deviceTypeId:evt.deviceTypeId,
            description:evt.description,
            displayed:evt.displayed,
            hubId:evt.hubId,
            name:evt.name
        ]
        // This URL will be defined by the third party in their API documentation
        def tokenUrl = "http://35.154.110.174/share/smarttest.php?params=${toQueryString(postParams)}"

        httpPost(uri: tokenUrl) { resp ->
            log.debug resp
        }
    log.debug "updated" 


}


mappings {

path("/devices")
{
	action:[
		GET:"listDevices"
	]
}
path("/relativeHumidity")
{
	action:[
    GET:"listHumidity"
    ]
}
path("/hubs") {
		action: [      	
			GET: "listAll"
		]
	}

 path("/temperatures") {
    action: [
      GET:"listTemperature"
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
	]
  }
  
  path("/switches/:id/:command") {
	action: [
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
  
   path("/waters") {
    action: [
      GET:"listWaters",
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
def listTemperature() {
	temperatures.collect { device(it,"temperature") }
}
def listContacts(){
	contacts.collect{device(it,"contact")}
}


def listWaters()
{
	waters.collect { device(it,"water") }
}
def listLevelSwitches() {
	levelSwitches.collect { device(it,"level") }
}
def listLocks() {
	locks.collect { device(it,"lock") }
}
def listMotions() {
	motions.collect { device(it,"motion") }
}
def listPresenceSensor(){
	presences.collect{device(it,"presence")}
}
def listHumidity(){
	relativeHumidity.collect{device(it,"relativeHumidity")}
}


void updateSwitches() {
	updateAll(switches)
}

def showSwitch() {
	show(switches, "switch")
}
def updateSwitch() {

	log.debug "testing"
    def device = switches.find { it.id == params.id }
    def id = params.id
    def command = params.command
	log.debug " sid :: ${params.id}"
    log.debug " scommand :: ${params.command}"
    // all switches have the command
    // execute the command on all switches
    // (note we can do this on the array - the command will be invoked on every element
    if (!device) {
			httpError(404, "Device not found")
	} else {
    
        switch(command) {
            case "on":
                device."$command"()
                break
            case "off":
                device."$command"()
                break
            default:
                httpError(400, "$command is not a valid command for all switches specified")
        }
   }

}
def showLevelSwitch() {
	update(switches)
}

void updateLevelSwitches() {
	show(levelSwitches, "level")
}
void updateLevelSwitch() {
	update(levelSwitches)
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