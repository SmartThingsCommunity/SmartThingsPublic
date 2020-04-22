/**
 *  3400-X Keypad Manager
 *
 *  Copyright 2015 Mitch Pond
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
    name: "3400-X Keypad Manager",
    namespace: "mitchpond",
    author: "Mitch Pond",
    description: "Service manager for Centralite 3400-X security keypad. Keeps keypad state in sync with Smart Home Monitor",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    singleInstance: false)


preferences {
	page(name: "setupPage")
}

def setupPage() {
	dynamicPage(name: "setupPage",title: "3400-X Keypad Manager", install: true, uninstall: true) {
        section("Settings") {
            input(name: "keypad", title: "Keypad", type: "capability.lockCodes", multiple: false, required: true)
            input(name: "pin"	, title: "PIN code", type: "number", range: "0000..9999", required: true)
            paragraph "PIN should be four digits. Shorter PINs will be padded with leading zeroes. (42 becomes 0042)"
            label(title: "Assign a name", required: false)
        }
        def routines = location.helloHome?.getPhrases()*.label
        routines?.sort()
        section("Routines", hideable: true, hidden: true) {
        	input(name: "armRoutine", title: "Arm/Away routine", type: "enum", options: routines, required: false)
            input(name: "disarmRoutine", title: "Disarm routine", type: "enum", options: routines, required: false)
            input(name: "stayRoutine", title: "Arm/Stay routine", type: "enum", options: routines, required: false)
            //input(name: "nightRoutine", title: "Arm/Night routine", type: "enum", options: routines, required: false)
        }
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
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(location,"alarmSystemStatus",alarmStatusHandler)
    subscribe(keypad,"codeEntered",codeEntryHandler)   

    //initialize keypad to correct state
    def event = [name:"alarmSystemStatus", value: location.currentState("alarmSystemStatus").value, 
    			displayed: true, description: "System Status is ${shmState}"]
    alarmStatusHandler(event)
}

//Returns the PIN padded with zeroes to 4 digits
private String getPIN(){
	return settings.pin.value.toString().padLeft(4,'0')
}

// TODO: implement event handlers
def alarmStatusHandler(event) {
	log.debug "Keypad manager caught alarm status change: "+event.value
    if (event.value == "off") keypad?.setDisarmed()
    else if (event.value == "away") keypad?.setArmedAway()
    else if (event.value == "stay") keypad?.setArmedStay()
}

private sendSHMEvent(String shmState){
	def event = [name:"alarmSystemStatus", value: shmState, 
    			displayed: true, description: "System Status is ${shmState}"]
    sendLocationEvent(event)
}

private execRoutine(armMode) {
	if (armMode == 'away') location.helloHome?.execute(settings.armRoutine)
    else if (armMode == 'stay') location.helloHome?.execute(settings.stayRoutine)
    else if (armMode == 'off') location.helloHome?.execute(settings.disarmRoutine)    
}

def codeEntryHandler(evt){
	//do stuff
    log.debug "Caught code entry event! ${evt.value.value}"
    
    def codeEntered = evt.value as String
    def correctCode = getPIN()
    def data = evt.data as String
    def armMode = ''
    
    if (data == '0') armMode = 'off'
    else if (data == '3') armMode = 'away'
    else if (data == '1') armMode = 'stay'
    else if (data == '2') armMode = 'stay'	//Currently no separate night mode for SHM, set to 'stay'
    else {
    	log.error "${app.label}: Unexpected arm mode sent by keypad!: "+data
        return []
        }
    
    if (codeEntered == correctCode) {
    	log.debug "Correct PIN entered. Change SHM state to ${armMode}"
        keypad.acknowledgeArmRequest(data)
        sendSHMEvent(armMode)
		execRoutine(armMode)
    }
    else {
    	log.debug "Invalid PIN"
        //Could also call acknowledgeArmRequest() with a parameter of 4 to report invalid code. Opportunity to simplify code?
    	keypad.sendInvalidKeycodeResponse()
    }
}