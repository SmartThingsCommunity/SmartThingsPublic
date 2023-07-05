/**
 *  Bticino Shutter
 *
 *  Copyright 2018 Bogdan Ripa
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

metadata {
	definition (name: "Bticino Shutter", namespace: "bogdanripa", author: "Bogdan Ripa") {
		capability "Window Shade"
		capability "Refresh"
        capability "Switch Level"   // until we get a Window Shade Level capability
        command "stop"
		attribute "status", "string"
	}
    
	tiles (scale: 2) {
        multiAttributeTile(name:"windowShade", type: "lighting", width: 6, height: 4){
            tileAttribute ("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", label:'${name}', action:"close", icon:"st.shades.shade-open", backgroundColor:"#79b821", nextState:"closing"
                attributeState "closed", label:'${name}', action:"open", icon:"st.shades.shade-closed", backgroundColor:"#ffffff", nextState:"opening"
                attributeState "partially open", label:'partial', action:"close", icon:"st.shades.shade-open", backgroundColor:"#79b821", nextState:"closing"
                attributeState "opening", label:'${name}', action:"stop", icon:"st.shades.shade-opening", backgroundColor:"#79b821", nextState:"partially open"
                attributeState "closing", label:'${name}', action:"stop", icon:"st.shades.shade-closing", backgroundColor:"#ffffff", nextState:"partially open"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"setLevel"
            }
        }
        
        standardTile("home", "device.level", width: 2, height: 2, decoration: "flat") {
            state "default", label: "home", action:"presetPosition", icon:"st.Home.home2"
        }

        standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh", nextState: "disabled"
            state "disabled", label:'', action:"", icon:"st.secondary.refresh"
        }
        
        preferences {
            input "preset", "number", title: "Default half-open position (1-100)", defaultValue: 50, required: false, displayDuringSetup: false
        }


        main(["windowShade"])
        details(["windowShade", "home", "refresh"])
    }
    
	preferences {
        input "sw_id", "text", title: "Identifier", required: false
	}
}

def initialSetup(gw, device_id, level) {
    state.sw_id = device_id
    state.gw = gw
    switch(level) {
    	case 0:
		    sendEvent(name: "windowShade", value: "closed")
            break
        case 100:
		    sendEvent(name: "windowShade", value: "open")
            break
        default:
		    sendEvent(name: "windowShade", value: "partially open")
    }
    sendEvent(name: "level", value: level)

    log.debug "Initial level: " + level + " for " + state.sw_id + " calling " + state.gw
}

def updateLevel(level) {
	def status = "partially open"
    switch(level) {
        case 0:
        	status = "closed"
            break
        case 100:
        	status = "open"
            break
    }
    
    sendEvent(name: "windowShade", value: status)
    sendEvent(name: "level", value: level)

    log.debug "Update level: " + level + " for " + state.sw_id + " calling " + state.gw
}

def setGW(gw) {
    state.gw = gw
}

def installed() {
    return initialize()
}

def updated() {
    //unsubscribe()
    return initialize()
}

def initialize() {
	log.debug "Initialize Device Handler"
}

def sendCommand(level) {
    def id = state.sw_id

    log.debug "Set level " + level + " on " + state.gw + " for " + id

    if (id && state.gw != '') {
    	log.debug("Sending request")
        def result = new physicalgraph.device.HubAction([
            method: "POST",
            path: "/shutters/" + id.replaceAll(/#/, '%23'),
            headers: [
                "HOST": state.gw,
                "Content-Type": "application/x-www-form-urlencoded"
            ],
            body: ["level": '' + level]], null, [callback: processShutterResponse]
        )

        return result
    }
}

def processShutterResponse(response) {
	log.debug("Request completed: " + response.status)
    if (response.status != 200) {
    	log.debug("Error: " + error)
        sendEvent(name: "windowShade", value: "error")
        sendEvent(name: "status", value: "Response has error: ${response.body} with status: ${response.status}")
    }
}

// handle commands

def closed() {
	log.debug "Executed 'close'"
    unschedule()
    sendEvent(name: "windowShade", value: "closed")
    sendEvent(name: "level", value: 0)
}

def doClose() {
	log.debug "Executing 'close'"
    sendEvent(name: "status", value: "Executing 'close'")
    unschedule()
    runIn(18, closed)
    return sendCommand(0)
}

def close() {
	return doClose()
}

def opened() {
	log.debug "Executed 'open'"
    unschedule()
    sendEvent(name: "windowShade", value: "open")
    sendEvent(name: "level", value: 100)
}

def doOpen() {
	log.debug "Executing 'open'"
    sendEvent(name: "status", value: "Executing 'open'")
    unschedule()
    runIn(18, opened)
    return sendCommand(100)
}

def open() {
	return doOpen()
}

def stop() {
	log.debug "Executing 'stop'"
    sendEvent(name: "status", value: "Executing 'stop'")
    unschedule()
    return sendCommand(-1)
}

def loading() {
	log.debug "Loading..."
}

def setLevel(userLevel) {
// 18 seconds
	log.debug "Executing 'userSetLevel' to " + userLevel

	sendEvent(name: "status", value: "Executing 'userSetLevel' to " + userLevel)    
    sendEvent(name: "level", value: userLevel)
    
    if (userLevel == 100 || userLevel == "100") {
    	log.debug "doOpen"

    	return doOpen()
    }
    
    if (userLevel == 0 || userLevel == "0") {
    	log.debug "doClose"

		return doClose()
    }
    
    sendEvent(name: "windowShade", value: "partially open")
    return sendCommand(userLevel)
}

def presetPosition() {
    return setLevel(preset ?: state.preset ?: 50)
}