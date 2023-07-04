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

import physicalgraph.zigbee.zcl.DataType

metadata {
    definition (name: "Curtain", namespace: "Rooms Beautiful", author: "Alex Feng", ocfDeviceType: "oic.d.switch", vid:"generic-rgbw-color-bulb") {	// vid is for Samsung Connect App
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Health Check"
        capability "Battery"
        capability "Switch Level"
        
        attribute("replay", "enum")
        attribute("battLife", "enum")
        
        command "pause"
        command "cont"
        
        fingerprint profileId: "0104", inClusters: "0000, 0001, 0003, 0006, FC00, DC00, 0102,", deviceJoinName: "Curtain", manufacturer: "Rooms Beautiful",  model: "C001"
    }
    
    preferences {
        input name: "invert", type: "bool", title: "Invert Direction", description: "Invert Curtain Direction", defaultValue: false, displayDuringSetup: false, required: true
    }

    // simulator metadata
    simulator {
        // status messages
        status "on": "on/off: 1"
        status "off": "on/off: 0"

        // reply messages
        reply "zcl on-off on": "on/off: 1"
        reply "zcl on-off off": "on/off: 0"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
            	attributeState "on", label:'Open', action:"switch.off", icon:"st.shades.shade-open", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'Closed', action:"switch.on", icon:"st.shades.shade-closed", backgroundColor:"#bc2323", nextState:"turningOn"
                attributeState "turningOn", label:'Opening', action:"switch.off", icon:"st.shades.shade-opening", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'Closing', action:"switch.on", icon:"st.shades.shade-closing", backgroundColor:"#bc2323", nextState:"turningOn"
            }
            
            tileAttribute ("device.battLife", key: "SECONDARY_CONTROL") {
                attributeState "full", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/full.png"
                attributeState "medium", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/medium.png"
                attributeState "low", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/low.png"
                attributeState "dead", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/dead.png"
            }
            
            /*tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }*/
        }        
        standardTile("contPause", "device.replay", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "pause", label: "Pause", icon:'https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/pause.png', action:'pause', backgroundColor:"#e86d13", nextState: "cont"//, label: '${currentValue}'
            state "cont", label: "Cont.", icon:'https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/play.png', action:'cont', backgroundColor:"#90d2a7", nextState: "pause"//, label: '${currentValue}'
        }        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration:"flat", width:1, height:1) {		            
        	state "battery", defaultState: true, label: 'Batt: ${currentValue}%'
				backgroundColors: [
                    [value: "50", color: "#e86d13"],
                    [value: "100", color: "#90d2a7"]
            	]
        }
        
        main "switch"
        details(["switch", "contPause", /*"battery",*/ "refresh"])
    }
}

def setLevel(value) {
	if (value >= 70){
      	log.debug "Battery Full: " + value
        sendEvent(name: "battLife", value: "full")
    }
	else if (value >= 60 && value < 75){
       	log.debug "Battery Medium: " + value
      	sendEvent(name: "battLife", value: "medium")
    }
    else if (value >= 50 && value < 60){
      	log.debug "Battery Low: " + value
       	sendEvent(name: "battLife", value: "low")
    }
    else if (value < 50){
      	log.debug "Battery Dead: " + value
      	sendEvent(name: "battLife", value: "dead")
    }
                
	sendEvent(name: "level", value: value)
}

// Parse incoming device messages to generate events
def parse(String description) {
	// FYI = event.name refers to attribute name & not the tile's name

	def linkText = getLinkText(device)
    def event = zigbee.getEvent(description)
    def descMap = zigbee.parseDescriptionAsMap(description)
    def value
    def attrId
    
    if (event) {
    	if(!descMap.attrId){
        	sendEvent(name: "replay", value: "pause")
            //log.warn "${linkText} - Replay set to: PAUSE"
        }        	
        
        sendEvent(event)
        log.debug "${linkText} - On/Off: ${event.value}"
    }
    else {
       	if(descMap.attrId) {
        	if(descMap.clusterInt != 0xDC00){
                value = Integer.parseInt(descMap.value, 16)
                attrId = Integer.parseInt(descMap.attrId, 16)
            }
        }
            
        switch(descMap.clusterInt) {
        	case 0x0001:
            	if(attrId == 0x0020)
                	handleBatteryEvent(value)                    
                break;
            case 0x0102:
            	log.debug "${linkText} - Replay: ${device.currentState("replay").value}"
                break;
            case 0xFC00:
            	if (description?.startsWith('read attr -'))
                	log.info "${linkText} - Inverted: ${value}"
            	else
                	log.debug "${linkText} - Inverted set to: ${invert}"                
                break;
            case 0xDC00:
				value = descMap.value
            	def shortAddr = value.substring(4)
                def lqi = zigbee.convertHexToInt(value.substring(2, 4))
            	def rssi = (byte)zigbee.convertHexToInt(value.substring(0, 2))
            	log.info "${linkText} - Parent Addr: ${shortAddr} **** LQI: ${lqi} **** RSSI: ${rssi}"                
                break;
            default:
                log.warn "${linkText} - DID NOT PARSE MESSAGE for description: $description"
                log.debug descMap
            	break;
        }
    }
}

def off() {
    zigbee.off()
}

def on() {
    zigbee.on()
}

def pause() {
    zigbee.command(0x0102, 0x02) +
    sendEvent(name: "replay", value: "cont")
}

def cont() {
    zigbee.command(0x0102, 0x02) +
    sendEvent(name: "replay", value: "pause")
}

private handleBatteryEvent(volts) {
	def linkText = getLinkText(device)

    if (volts > 30 || volts < 20) {
        log.debug "${linkText} - Ignoring invalid value for voltage (${volts/10}V)"
    }
    else {
        def batteryMap = [30:"full", 29:"full", 28:"full", 27:"medium", 26:"medium", 25:"medium",
                          24:"low", 23:"low", 22:"dead", 21:"dead", 20:"dead"]
		
        def value = batteryMap[volts]
        if(value != null){
            def minVolts = 20
			def maxVolts = 30
			def pct = (volts - minVolts) / (maxVolts - minVolts)
			def roundedPct = Math.round(pct * 100)
			def percent = Math.min(100, roundedPct)
            
        	sendEvent(name: "battery", value: percent)
        	sendEvent(name: "battLife", value: value)
        	log.debug "${linkText} - Batt: ${value} **** Volts: ${volts/10}v **** Percent: ${percent}%"
        } 
    }
}

def refresh() {
    zigbee.onOffRefresh() +
    zigbee.readAttribute(0x0001, 0x0020) +
    
    // For Diagnostics
    zigbee.readAttribute(0xFC00, 0x0000) +
    zigbee.readAttribute(0xDC00, 0x0000)
}

def ping() {
    return refresh()
}

//	Don't do Device-Watch to prevent 20-30 min read attribute
def configure() {
    return refresh()
}

def installed() {
	sendEvent(name: "battLife", value: "full")
	response(refresh())
}

def updated() {
	// Needed because updated() is being called twice
    def time
    if(state.updatedDate == null){
    	time = now()
    }
    else{
        time = now() - state.updatedDate
    }
    state.updatedDate = now()
    
    log.trace ("Time: ${time}")
    
    //	Updated [Tested on 12/27/18] - Don't need if statement
    //	Doesn't occur twice anymore
    
    //if (time < 100 ){	// Smaller value (e.g. 100) means less likely to occur twice
        if (invert.value == false)
            response(normal())
        else if(invert.value == true)
            response(reverse())
    //}
}

def normal() {
    if(device.currentState("switch").value == "on"){
    	sendEvent(name: "switch", value: "off")
        log.warn ("normal-off")
        zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x00)
    }
    else{
    	sendEvent(name: "switch", value: "on")
        log.warn ("normal-on")
        zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x00)
    }   
}

def reverse() {
	if(device.currentState("switch").value == "on"){    	
    	sendEvent(name: "switch", value: "off")
        log.warn ("reverse-off")
        zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x01)
    }
    else{
    	sendEvent(name: "switch", value: "on")
        log.warn ("reverse-on")
        zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x01)
    }
}