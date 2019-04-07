/**
 *  Remotec ZRC-90 Scene Master
 *  Copyright 2015 Eric Maycock
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
	definition (name: "Remotec ZRC-90 Scene Master", namespace: "erocm123", author: "Eric Maycock", vid:"generic-button") {
		capability "Actuator"
		capability "Button"
        capability "Holdable Button"
		capability "Configuration"
		capability "Sensor"
        capability "Battery"
        capability "Health Check"
        
        attribute "sequenceNumber", "number"
        attribute "numberOfButtons", "number"
        
        fingerprint mfr: "5254", prod: "0000", model: "8510"

		fingerprint deviceId: "0x0106", inClusters: "0x5E,0x85,0x72,0x21,0x84,0x86,0x80,0x73,0x59,0x5A,0x5B,0xEF,0x5B,0x84"
	}

	simulator {
		status "button 1 pushed":  "command: 5B03, payload: 23 00 01"
		status "button 1 held":  "command: 5B03, payload: 2B 02 01"
        status "button 1 released":  "command: 5B03, payload: 2C 01 01"
        status "button 1 double":  "command: 5B03, payload: 2F 03 01"
		status "button 2 pushed":  "command: 5B03, payload: 23 00 02"
		status "button 2 held":  "command: 5B03, payload: 2B 02 02"
        status "button 2 released":  "command: 5B03, payload: 2C 01 02"
        status "button 2 double":  "command: 5B03, payload: 2F 03 02"
		status "button 3 pushed":  "command: 5B03, payload: 23 00 03"
		status "button 3 held":  "command: 5B03, payload: 2B 02 03"
        status "button 3 released":  "command: 5B03, payload: 2C 01 03"
        status "button 3 double":  "command: 5B03, payload: 2F 03 03"
		status "button 4 pushed":  "command: 5B03, payload: 23 00 04"
		status "button 4 held":  "command: 5B03, payload: 2B 02 04"
        status "button 4 released":  "command: 5B03, payload: 2C 01 04"
        status "button 4 double":  "command: 5B03, payload: 2F 03 04"
        status "button 5 pushed":  "command: 5B03, payload: 23 00 05"
		status "button 5 held":  "command: 5B03, payload: 2B 02 05"
        status "button 5 released":  "command: 5B03, payload: 2C 01 05"
        status "button 5 double":  "command: 5B03, payload: 2F 03 05"
		status "button 6 pushed":  "command: 5B03, payload: 23 00 06"
		status "button 6 held":  "command: 5B03, payload: 2B 02 06"
        status "button 6 released":  "command: 5B03, payload: 2C 01 06"
        status "button 6 double":  "command: 5B03, payload: 2F 03 06"
		status "button 7 pushed":  "command: 5B03, payload: 23 00 07"
		status "button 7 held":  "command: 5B03, payload: 2B 02 07"
        status "button 7 released":  "command: 5B03, payload: 2C 01 07"
        status "button 7 double":  "command: 5B03, payload: 2F 03 07"
		status "button 8 pushed":  "command: 5B03, payload: 23 00 08"
		status "button 8 held":  "command: 5B03, payload: 2B 02 08"
        status "button 8 released":  "command: 5B03, payload: 2C 01 08"
        status "button 8 double":  "command: 5B03, payload: 2F 03 08"
        status "battery 100%": "command: 8003, payload: 64"
		status "wakeup":  "command: 8407, payload:"
        
	}
	tiles (scale: 2) {
		multiAttributeTile(name:"button", type:"generic", width:6, height:4) {
  			tileAttribute("device.button", key: "PRIMARY_CONTROL"){
    		attributeState "default", label:'', backgroundColor:"#ffffff", icon: "st.unknown.zwave.remote-controller"
  			}
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
			attributeState "battery", label:'${currentValue} % battery'
            }
            
        }
        valueTile(
			"battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        valueTile(
			"sequenceNumber", "device.sequenceNumber", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}', unit:""
		}
        standardTile("configure", "device.configure", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		main "button"
		details(["button", "battery", "sequenceNumber", "configure"])
	}
    
    preferences {
       input name: "holdMode", type: "enum", title: "Multiple \"held\" events on botton hold? With this option, the controller will send a \"held\" event about every second while holding down a button. If set to No it will send a \"held\" event a single time when the button is released.", defaultValue: "2", displayDuringSetup: true, required: false, options: [
                   "1":"Yes",
                   "2":"No"]
       input name: "debug", type: "boolean", title: "Enable Debug?", defaultValue: false, displayDuringSetup: false, required: false
    }
}

def parse(String description) {
	def results = []
     logging("${description}")
	if (description.startsWith("Err")) {
	    results = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [0x2B: 1, 0x80: 1, 0x84: 1])
		if(cmd) results += zwaveEvent(cmd)
		if(!results) results = [ descriptionText: cmd, displayed: false ]
	}
    
    if(state.isConfigured != "true") configure()
    
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
        logging(cmd)
        logging("keyAttributes: $cmd.keyAttributes")
        logging("sceneNumber: $cmd.sceneNumber")
        logging("sequenceNumber: $cmd.sequenceNumber")

        sendEvent(name: "sequenceNumber", value: cmd.sequenceNumber, displayed:false)
        switch (cmd.keyAttributes) {
           case 0:
              buttonEvent(cmd.sceneNumber, "pushed")
           break
           case 1:
              if (settings.holdMode == "2") buttonEvent(cmd.sceneNumber, "held")
           break
           case 2:
              if (!settings.holdMode || settings.holdMode == "1") buttonEvent(cmd.sceneNumber, "held")
           break
           case 3:
              buttonEvent(cmd.sceneNumber + 8, "pushed")
           break
           default:
               logging("Unhandled CentralSceneNotification: ${cmd}")
           break
        }
}

private def logging(message) {
    if (settings.debug == "true") log.debug "$message"
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)]
	results << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	return results
}

def buttonEvent(button, value) {
	createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logging("Unhandled zwaveEvent: ${cmd}")
}

def installed() {
    logging("installed()")
    configure()
}

def updated() {
    logging("updated()")
    configure()
}

def configure() {
	logging("configure()")
    sendEvent(name: "checkInterval", value: 2 * 60 * 12 * 60 + 5 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "numberOfButtons", value: 16, displayed: true)
    state.isConfigured = "true"
}

def ping() {
    logging("ping()")
	logging("Battery Device - Not sending ping commands")
}
