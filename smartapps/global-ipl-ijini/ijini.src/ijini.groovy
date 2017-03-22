/**
 *  IJINI
 *
 *  Copyright 2017 IPL_DevAccount
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
    name: "IJINI",
    namespace: "global.ipl.IJINI",
    author: "IPL_DevAccount",
    description: "IJINI SUPPORTING APP",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Allow Endpoint to Control These Things...") {
		input "switches", "capability.switch", title: "Which Switches?", multiple: true
        //input "locks", "capability.lock", title: "Which Locks?", multiple: true
        input "themotion", "capability.motionSensor", title: "Which Motions?", multiple: true
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
    
	path("/SwitchState/:id") {
		action: [
			GET: "SwitchState"
		]
	}  
}

def SwitchState()
{
	GetSwitchState(switches,"switch");
}

def GetSwitchState(devices, type)
{
	
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found ${devices}")
	}
	else 
    {    
    	def attributeName = (type == "motionSensor") ? "motion" : type
		def s = device.currentState(attributeName)
		[value: s?.value]
	}
}

def installed() {
	initialize()
}

def updated() 
{
	unsubscribe()
}

def initialize()
{
	subscribe(themotion, "motion.active", motionDetectedHandler)
    subscribe(themotion, "motion.inactive", motionStoppedHandler)
}

def motionDetectedHandler(evt) {
    log.debug "motionDetectedHandler called: ${evt}"
    httpGet(String uri, Closure closure)
}

def motionStoppedHandler(evt) {
    log.debug "motionStoppedHandler called: ${evt}"    
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

//Motions
def listMotion() {
	log.debug "listMotions is activated...."
	motion.collect{device(it,"motion")}
}

def showMotion() {
	show(motion, "motionSensor")
}

void updateMotion() {
	update(motion)
}


def deviceHandler(evt) {
log.debug "deviceHandler"
}

private void update(devices) 
{
	log.debug "update, request: params: ${params}, devices: $devices.id"
    
	//def command = request.JSON?.command
    def command = params.command
    //let's create a toggle option here
	if (command) 
    {
		def device = devices.find { it.id == params.id }//it, params 상에서 같은것을 찾아내서 저장...하는듯하다.
		if (!device) {
			httpError(404, "Device not found")
		} else {
        	if(command == "toggle")
       		{
            	if(device.currentValue('switch') == "on")
                {
                  device.off();
                }
                else
                {
                  device.on();
                }
       		}
       		else
       		{
				device."$command"()
            }
		}
	}
}

private show(devices, type) {
	log.debug "device  = ${devices},type = ${type} "
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
		def attributeName = (type == "motionSensor") ? "motion" : type
		def s = device.currentState(attributeName)
        
		[id: device.id, label: device.displayName, value: s?.value, unitTime: s?.date?.time, type: type]
	}
}


private device(it, type) {
	log.debug "device ctrl.. ${it}, ${type}"
	it ? [id: it.id, label: it.label, type: type] : null
}