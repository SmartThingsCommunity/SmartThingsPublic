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
		input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
		input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
        input "phone", "phone", title: "Warn with text message",description: "Phone Number", required: false
	}
	}
}

mappings {

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
    subscribe(switches, "switch", lockDeviceHandler)    
}

def deviceHandler(evt) {
    //locks.lock()
    logField(evt) { it.toString() }    
}

def lockDeviceHandler(evt) {
    //locks.lock()
    log.debug "lock status changed to ${evt.value}"    
}


private logField(evt, Closure c) {
	if(evt.value=='on')
    	locks.unlock()
    else
    	locks.lock()
        
	//httpPut("http://vps49294.vps.ovh.ca/sample_charts/api-event-handler.php?data=mydata", "data") { resp ->
    //httpPostJson(uri: "http://vps49294.vps.ovh.ca/sample_charts/api-event-handler.php?data=mydata",   body:[device: evt.deviceId, name: evt.name, value: evt.value, date: evt.isoDate, unit: evt.unit]) {resp ->
    httpPost(uri: "http://vps49294.vps.ovh.ca/sample_charts/api-event-handler.php?deviceName=${evt.name}&deviceId=${evt.id}&deviceValue=${evt.value}") {resp ->
log.debug "response data: ${resp.data}"
        log.debug evt.name+" Event data successfully posted"
    }
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
