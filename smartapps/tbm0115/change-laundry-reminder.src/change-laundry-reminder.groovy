/**
 *  Change Laundry Reminder
 *
 *  Copyright 2019 Trais McAllister
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
    name: "Change Laundry Reminder",
    namespace: "tbm0115",
    author: "Trais McAllister",
    description: "Receive a reminder when a washer/dryer machine cycle is complete.",
    category: "Family",
    iconUrl: "http://cdn.device-icons.smartthings.com/Appliances/appliances1-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances1-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances1-icn@2x.png")

preferences {
    page(name: "pgWasher", title: "Washer Setup", nextPage: "pgDryer", uninstall: true){
        section("Sensor(s)"){
            input "mpSensorW", "capability.sensor", title: "Sensor", description: "ex. Multi-Purpose Sensor"
        }
        section("Settings"){
            input "intThresW", "number", title: "Inactivity Threshold (minutes)", defaultValue: 10
        }
        section("Reminder"){
            input "sendPushW", "bool", title: "Send Push Notification when Complete?", defaultValue: true
            input "blnAlertWithDryer", "bool", title: "Send Notification Even When Dryer Running?", defaultValue: false
        }
	}
    page(name: "pgDryer", title: "Dryer Setup", install: true, uninstall: true){
        section("Sensor"){
            input "mpSensorD", "capability.sensor", title: "Sensor", description: "ex. Multi-Purpose Sensor"
        }
        section("Settings"){
            input "intThresD", "number", title: "Inactivity Threshold (minutes)", defaultValue: 10
        }
        section("Reminder"){
            input "sendPushD", "bool", title: "Send Push Notification when Complete?", defaultValue: true
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
    //initializeDevice(mpSensorW, "washer")
    //initializeDevice(mpSensorD, "dryer")
    
    def dev = null
    for(strType in ["dryer", "washer"]){
    	if (strType == "dryer"){
        	dev = mpSensorD
        }else if (strType == "washer"){
        	dev = mpSensorW
		}
        if (dev != null){
            subscribe(dev, "acceleration.active", accelerationDetectedHandler)
            subscribe(dev, "acceleration.inactive", accelerationDetectedHandler)
            subscribe(dev, "contact.open", contactDetectedHandler)
            subscribe(dev, "contact.closed", contactDetectedHandler)

            state[strType + "_isClosed"] = dev.currentState("contact").value == "closed"
            state[strType + "_sentNotification"] = false
            state[strType + "_closedDoorRecently"] = false
            def accState = dev.currentState("acceleration")
            if (accState == "active"){
                state[strType + "_lastActive"] = now()
            }else{
                state[strType + "_lastActive"] = null
            }
            if (state[strType + "_isClosed"]){
                state[strType + "_lastOpened"] = null
            }else{
                state[strType + "_lastOpened"] = now()
            }
        }
	}
}

def accelerationDetectedHandler(evt){
	def strType = ""
    if (mpSensorD != null && mpSensorD.id == evt.deviceId){
    	strType = "dryer"
	}else if (mpSensorW != null && mpSensorW.id == evt.deviceId){
    	strType = "washer"
	}
	def stateCurrent = evt.value
    def isClosed = state[strType + "_isClosed"]
    def closedDoorRecently = state[strType + "_closedDoorRecently"]
    def lastActive = state[strType + "_lastActive"]
    log.debug "accelerationDetectedHandler evt.value = $stateCurrent"
    if (isClosed && !closedDoorRecently){
        if (stateCurrent == "active"){
        	state[strType + "_lastActive"] = now()
        }else if(lastActive != null){
        	if (strType == "dryer"){
            	runIn(intThresD * 60, remindD)
			}else if (strType == "washer"){
            	runIn(intThresW * 60, remindW)
            }
        }
	}
	log.debug "accelerationDetectedHandler called:\r\n\tstate: $stateCurrent.value\r\n\tlastActive: $lastActive\r\n\tisClosed: $isClosed\r\n\tclosedRecently: $closedDoorRecently"
}
def contactDetectedHandler(evt){
	def strType = ""
    if (mpSensorD != null && mpSensorD.id == evt.deviceId){
    	strType = "dryer"
	}else if (mpSensorW != null && mpSensorW.id == evt.deviceId){
    	strType = "washer"
	}
	def stateCurrent = evt.value
    log.debug "contactDetectedHandler called: $stateCurrent"
    if (stateCurrent == "open"){
    	state[strType + "_isClosed"] = false
        state[strType + "_lastActive"] = null
        state[strType + "_lastOpened"] = now()
        if (state[strType + "_sentNotification"]){
        	state[strType + "_sentNotification"] = false
		}
    }else{
    	state[strType + "_isClosed"] = true
        state[strType + "_closedDoorRecently"] = true
        if (strType == "dryer"){
        	runIn(1 * 60, postClosedDoorD)
        }else if(strType == "washer"){
        	runIn(1 * 60, postClosedDoorW)
        }
    }
    def isClosed = state[strType + "_isClosed"]
    def closedDoorRecently = state[strType + "_closedDoorRecently"]
    def sentNotification = state[strType + "_sentNotification"]
    log.debug "contactDetectedHandler called:\r\n\t isClosed: $isClosed\r\n\tsentNotification: $sentNotification\r\n\tclosedDoorRecently: $closedDoorRecently"
}
def postClosedDoorD(){
	state["dryer_closedDoorRecently"] = false
    log.debug "Now monitoring dryer"
}
def postClosedDoorW(){
	state["washer_closedDoorRecently"] = false
    log.debug "Now monitoring washer"
}
def remindD(){
	remind(mpSensorD, "dryer")
}
def remindW(){
	if (!blnAlertWithDryer || (mpSensorD == null && isTimedOut("dryer"))){
		remind(mpSensorW, "washer")
    }
}
Boolean isTimedOut(strType){
	def timeOut = now()
    def lastActive = state[strType + "_lastActive"]
    if (strType == "dryer"){
    	timeOut = timeOut - (intThresD * 60)
    }else if (strType == "washer"){
    	timeOut = timeOut - (intThresW * 60)
    }
    return lastActive <= timeOut
}
def remind(dev, strType){
 	// Send reminder notification to change wash
    def timeOut = now()
    def lastActive = state[strType + "_lastActive"]
    def lastOpened = state[strType + "_lastOpened"]
    //if (strType == "dryer"){
    //	timeOut = timeOut - (intThresD * 60)
	//}else if (strType == "washer"){
    ///	timeOut = timeOut - (intThresW * 60)
	//}
    
    def blnTimedOut = isTimedOut(dev, strType)// lastActive <= timeOut
    if (lastActive > lastOpened){
        if (state[strType + "_isClosed"] && !state[strType + "_sentNotification"] && blnTimedOut){
            state[strType + "_sentNotification"] = true;
            sendPush("Change $strType!")
        }else{
            log.debug "remind called:\r\n\tCouldn't send notification.\r\n\tlastActive: $lastActive\r\n\ttimeOut: $timeOut\r\n\tlastActive <= timeOut: $blnTimedOut"
        }
	}
}

// TODO: implement event handlers