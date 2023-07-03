/**
 *  Tuya Plug Color (v.0.0.1)
 *
 * MIT License
 *
 * Copyright (c) 2019 fison67@nate.com
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
*/

import groovy.json.JsonSlurper

metadata {
	definition (name: "Tuya Plug Color", namespace: "fison67", author: "fison67") {
        capability "Switch"		
        capability "Outlet"
		capability "Color Control"
        capability "Power Meter"
        capability "Energy Meter"
        capability "Refresh"
        
        attribute "lastCheckin", "Date"
        attribute "led", "string"
        
        command "ledOn"
        command "ledOff"
        
        command "setTimer", ["number"]
        command "stop"
	}

	simulator { }

	preferences {
        input name: "powerIDX", title:"Power Index" , type: "number", required: false, defaultValue: 1
        input name: "colorIDX", title:"Color Index" , type: "number", required: false, defaultValue: 5
        input name: "meterIDX", title:"Meter Index" , type: "number", required: false
        input name: "energyIDX", title:"Energy Index" , type: "number", required: false
        input name: "ledIDX", title:"LED Index" , type: "number", required: false
        input name: "timerIDX", title:"Timer Index" , type: "number", required: false
	}
    
	tiles {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"off", icon:"https://github.com/fison67/DW-Connector/blob/master/icons/dawon-on.png?raw=true", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"on", icon:"https://github.com/fison67/DW-Connector/blob/master/icons/dawon-off.png?raw=true", backgroundColor:"#ffffff", nextState:"turningOn"
                
                attributeState "turningOn", label:'${name}', action:"off", icon:"https://github.com/fison67/DW-Connector/blob/master/icons/dawon-on.png?raw=true", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"on", icon:"https://github.com/fison67/DW-Connector/blob/master/icons/dawon-off.png?raw=true", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Updated: ${currentValue}',icon: "st.Health & Wellness.health9")
            }
            
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action:"setColor"
            }
		}
        
        standardTile("led", "device.led", inactiveLabel: false, width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"ledOff", backgroundColor:"#00a0dc", nextState:"turningOff"
            state "off", label:'${name}', action:"ledOn", backgroundColor:"#ffffff", nextState:"turningOn"
             
        	state "turningOn", label:'....', action:"ledOff", backgroundColor:"#00a0dc", nextState:"turningOff"
            state "turningOff", label:'....', action:"ledOn", backgroundColor:"#ffffff", nextState:"turningOn"
        }
        valueTile("power", "device.power", width:2, height:2, inactiveLabel: false, decoration: "flat" ) {
        	state "power", label: 'Current\n${currentValue} w', defaultState: true
		}    
        valueTile("energy", "device.energy", width:2, height:2, inactiveLabel: false, decoration: "flat" ) {
        	state "energy", label: 'Total\n${currentValue}kWh', defaultState: true
        }
        valueTile("timer_label", "device.leftTime", decoration: "flat", width: 4, height: 1) {
            state "default", label:'Set a Timer\n${currentValue}'
        }
        controlTile("time", "device.time", "slider", height: 1, width: 1, range:"(0..120)") {
	    	state "time", action:"setTimer"
		}
        standardTile("tiemr0", "device.timeRemaining") {
			state "default", label: "OFF", action: "stop", icon:"st.Health & Wellness.health7", backgroundColor:"#c7bbc9"
		}
        main(["switch"])
  		details(["switch", "led", "power", "energy", "timer_label", "time", "tiemr0"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def setInfo(String app_url, String id) {
	log.debug "${app_url}, ${id}"
	state.app_url = app_url
    state.id = id
}

def setStatus(data){
	log.debug data
    
    sendEvent(name: "switch", value: (data[powerIDX.toString()] ? "on" : "off"))
    
    if(meterIDX > 0){
    	sendEvent(name:"power", value: data[meterIDX.toString()] / 10)
    }
    if(energyIDX > 0){
    	sendEvent(name:"energy", value: data[energyIDX.toString()] / 10)
    }
    if(ledIDX > 0){
    	sendEvent(name:"led", value: (data[ledIDX.toString()] ? "on" : "off"))
    }
    if(timerIDX > 0){
    	def timeStr = msToTime(data[timerIDX.toString()])
    	sendEvent(name:"leftTime", value: "${timeStr}", displayed: false)
    	sendEvent(name:"time", value: Math.round(data/60), displayed: false)
    }
  
    def now = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)
    sendEvent(name: "lastCheckin", value: now, displayed: false)
}

def setColor(color){
	log.debug "setColor >> ${color.hex}"
    processCommand("color", color.hex, colorIDX.toString())
}

def setLevel(brightness){
	log.debug "setLevel >> ${brightness}"
    processCommand("brightness", brightness, colorIDX.toString())
}

def on(){
	log.debug "on"
    processCommand("power", "on", powerIDX.toString())
}

def off(){
	log.debug "off"
    processCommand("power", "off", powerIDX.toString())
}

def ledOn(){
	log.debug "ledOn"
    processCommand("power", "on", ledIDX.toString())
}

def ledOff(){
	log.debug "ledOff"
    processCommand("power", "off", ledIDX.toString())
}

def setTimer(second){
	log.debug "setTimer >> ${second} second"
    processCommand("timer", second, timerIDX.toString())
}

def stop(){
	processCommand("timer", 0, timerIDX.toString())
}

def processCommand(cmd, data, idx){
	def body = [
        "id": state.id,
        "cmd": cmd,
        "data": data,
        "idx": idx
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def callback(physicalgraph.device.HubResponse hubResponse){
	def msg
    try {
        msg = parseLanMessage(hubResponse.description)
		def jsonObj = new JsonSlurper().parseText(msg.body)
    } catch (e) {
        log.error "Exception caught while parsing data: "+e;
    }
}

def refresh(){}

def updated(){}

def sendCommand(options, _callback){
	def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: _callback])
    sendHubCommand(myhubAction)
}

def makeCommand(body){
	def options = [
     	"method": "POST",
        "path": "/api/control",
        "headers": [
        	"HOST": state.app_url,
            "Content-Type": "application/json"
        ],
        "body":body
    ]
    return options
}

def msToTime(duration) {
    def seconds = (duration%60).intValue()
    def minutes = ((duration/60).intValue() % 60).intValue()
    def hours = ( (duration/(60*60)).intValue() %24).intValue()

    hours = (hours < 10) ? "0" + hours : hours
    minutes = (minutes < 10) ? "0" + minutes : minutes
    seconds = (seconds < 10) ? "0" + seconds : seconds

    return hours + ":" + minutes + ":" + seconds
}