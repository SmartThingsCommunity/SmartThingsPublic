/**
 *  Copyright 2015 SmartThings
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
	definition (name: "iblinds", namespace: "iblinds", author: "HAB") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
        capability "Window Shade"   
		capability "Refresh"
        capability "Battery"
        capability "Configuration"
	

	//	fingerprint inClusters: "0x26"
   fingerprint type: "1106", cc: "5E,85,59,86,72,5A,73,26,25,80"
    
	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"blind", type: "lighting", width: 6, height: 4, canChangeIcon: true, canChangeBackground: true){
			tileAttribute ("device.windowShade", key: "PRIMARY_CONTROL") {
				attributeState "open", label:'${name}', action:"switch.off", icon:"http://cdn.device-icons.smartthings.com/Home/home9-icn@2x.png", backgroundColor:"#00B200", nextState:"closing"
				attributeState "closed", label:'${name}', action:"switch.on", icon:"http://cdn.device-icons.smartthings.com/Home/home9-icn@2x.png", backgroundColor:"#ffffff", nextState:"opening"
				attributeState "opening", label:'${name}', action:"switch.off", icon:"http://cdn.device-icons.smartthings.com/Home/home9-icn@2x.png", backgroundColor:"#00B200", nextState:"closing"
				attributeState "closing", label:'${name}', action:"switch.on", icon:"http://cdn.device-icons.smartthings.com/Home/home9-icn@2x.png", backgroundColor:"#ffffff", nextState:"opening" 
			}
            
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
			attributeState "level", action:"switch level.setLevel"
		}
            tileAttribute("device.battery", key: "SECONDARY_CONTROL") {
            attributeState "battery", label:'Battery Level: ${currentValue}%', unit:"%"    
            }
	
		}

		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {	state "battery", label:'${currentValue}% Battery Level', unit:""
		}
        
        valueTile("levelval", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {	state "Level", label:'${currentValue}% Tilt Angle', unit:""
		}
        
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("config", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Calibrate', action:"configuration.configure" , icon:"st.custom.buttons.add-icon"
		}

		main(["blind"])
       	details(["blind", "levelval", "battery", "levelSliderControl",  "refresh"])

	}
    
      preferences {
        
        input name: "time", type: "time", title: "Check battery level every day at: ", description: "Enter time", defaultValue: "2019-01-01T12:00:00.000-0600", required: true, displayDuringSetup: true
        input name: "reverse", type: "bool", title: "Reverse", description: "Reverse Blind Direction", required: true
 }
    
}

def parse(String description) {
	def result = null
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}



def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "ConfigurationReport $cmd"
	def value = "when off"
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	createEvent([name: "indicatorStatus", value: value])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	
    
           sendEvent(name: "level", value: 50, unit: "%")
	      
           sendEvent(name: "switch", value: "on")
           sendEvent(name: "windowShade", value: "open")
          
           zwave.switchMultilevelV2.switchMultilevelSet(value: 0x32).format()
           //zwave.basicV1.basicSet(value: 0x32).format()
}

def off() {
				
           if(reverse)
           {
           	sendEvent(name: "switch", value: "off")
            sendEvent(name: "windowShade", value: "closed")
            sendEvent(name: "level", value: 99, unit: "%")
            zwave.switchMultilevelV2.switchMultilevelSet(value: 0x63).format()
           }
           else
           {
    		sendEvent(name: "switch", value: "off")
            sendEvent(name: "windowShade", value: "closed")
            sendEvent(name: "level", value: 0, unit: "%")
            zwave.switchMultilevelV2.switchMultilevelSet(value: 0x00).format()
           } 
}

def setLevel(value) {
  

	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	
      if(reverse)
     {
       level = 99 - level
     }
    
    if (level <= 0) {
    	 sendEvent(name: "switch", value: "off")
         sendEvent(name: "windowShade", value: "closed")
    }
    if (level > 0 && level < 99) {
    	sendEvent(name: "switch", value: "on")
        sendEvent(name: "windowShade", value: "open")
    }
    if (level >= 99) {
    	 sendEvent(name: "switch", value: "off")
         sendEvent(name: "windowShade", value: "closed")
    } 
 
	sendEvent(name: "level", value: level, unit: "%")
    zwave.switchMultilevelV2.switchMultilevelSet(value: level).format()
    //zwave.basicV1.basicSet(value: level).format()
    
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
	zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format()
				
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}


def installed () {
    // When device is installed get battery level and set daily schedule for battery refresh
    log.debug "Installed, Set Get Battery Schedule"
    runIn(15,getBattery) 
    schedule("$time",getBattery)
    
}

def updated () {
    // When device is updated get battery level and set daily schedule for battery refresh
    log.debug "Updated , Set Get Battery Schedule"
    runIn(15,getBattery) 
    schedule("$time",getBattery)
     
}

def refresh() {
    log.debug "Refresh Tile Pushed"
    delayBetween([
        zwave.switchMultilevelV1.switchMultilevelGet().format(),
        zwave.batteryV1.batteryGet().format(),
    ], 3000)
}

def getBattery() {
    log.debug  "get battery level"
    // Use sendHubCommand to get battery level 
    def cmd = []
    cmd << new physicalgraph.device.HubAction(zwave.batteryV1.batteryGet().format())
    sendHubCommand(cmd)
    
}


/* The configure method is an advanced feature to launch calibration from the SmartThings App.
**** USE AT YOUR OWN RISK ****
Changing the configurationValue will allow you to change the inital calibration torque 
Reducing the torque helps improve calibration for small blinds 
Increasing the torque helps imporve calibration for large blinds

Note: You must add "config" to the details above to expose the config button in the App

-- details(["blind", "levelval", "battery", "levelSliderControl",  "refresh", "config"]) --

Update configurationValue[N] as follows: 

1 - Calibrate at default torque values
2 - Reduce calibration torque by 1 factor
3 - Reduce calibration torque by 2 factor
4 - Increase calibration torque by .5 factor
5. - Increase calibration torque by 1 factor 

--  zwave.configurationV2.configurationSet(parameterNumber: 1, size: 1, configurationValue: [N]).format()  

*/


/*
def configure() {

     // Run Calibration 
       log.debug "Configuration tile pushed"
       zwave.configurationV2.configurationSet(parameterNumber: 1, size: 1, configurationValue: [1]).format()      
}
*/