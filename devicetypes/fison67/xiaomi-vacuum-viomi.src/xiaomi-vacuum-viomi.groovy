/**
 *  Xiaomi Vacuum Viomi (v.0.0.1)
 *
 * MIT License
 *
 * Copyright (c) 2020 fison67@nate.com
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
	definition (name: "Xiaomi Vacuum Viomi", namespace: "fison67", author: "fison67", vid: "generic-switch", ocfDeviceType: "oic.d.switch") {
    	capability "Robot Cleaner Cleaning Mode"
        capability "Switch"		
		capability "Fan Speed"
        capability "Battery"	
        capability "Refresh"	
        
        attribute "mode", "string"
        attribute "fanSpeed", "string"
        attribute "cleanTime", "string"
        attribute "lastCheckin", "Date"
         
        command "clean"
        command "charge"
        command "paused"
        command "fanSpeed"
        
        command "silent"
        command "standard"
        command "medium"
        command "turbo"
	}

    preferences {
        input name: "offOption", title:"OFF Option" , type: "enum", required: true, options: ["off", "return"], defaultValue: "off"
	}
    
	simulator {}

	tiles {
		multiAttributeTile(name:"mode", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.mode", key: "PRIMARY_CONTROL") {
                attributeState "idleNotDocked", label:'Idle Not Docked', backgroundColor:"#00a0dc", icon:"https://github.com/fison67/mi_connector/blob/master/icons/vacuum_off.png?raw=true", action:"off"
                attributeState "idle", label:'Idle', backgroundColor:"#000000", icon:"https://github.com/fison67/mi_connector/blob/master/icons/vacuum-ready.png?raw=true", action:"on"
                attributeState "idle2", label:'Idle2',  backgroundColor:"#00a0dc", icon:"https://github.com/fison67/mi_connector/blob/master/icons/vacuum_off.png?raw=true", action:"charge"
                attributeState "cleaning", label:'Cleaning', backgroundColor:"#4286f4", icon:"https://github.com/fison67/mi_connector/blob/master/icons/vacuum_on.png?raw=true", action:"off"
                attributeState "returning", label:'Returning', backgroundColor:"#4e25a8", icon:"https://github.com/fison67/mi_connector/blob/master/icons/vacuum_turning_off.png?raw=true", action:"on"
                attributeState "docked", label:'Docked',   backgroundColor:"#25a896", icon:"https://github.com/fison67/mi_connector/blob/master/icons/vacuum_off.png?raw=true", action:"on"
                
			}
            
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Updated: ${currentValue}',icon: "st.Health & Wellness.health9")
            }
		}
        
        standardTile("switch", "device.switch", inactiveLabel: false, width: 1, height: 1, canChangeIcon: true) {
            state "on", label:'${name}', action:"off", backgroundColor:"#00a0dc", nextState:"turningOff"
            state "off", label:'${name}', action:"on", backgroundColor:"#ffffff", nextState:"turningOn"
             
        	state "turningOn", label:'....', action:"off", backgroundColor:"#00a0dc", nextState:"turningOff"
            state "turningOff", label:'....', action:"on", backgroundColor:"#ffffff", nextState:"turningOn"
        }
        
        valueTile("empty1_label", "", decoration: "flat", width: 2, height: 1) {
            state "default", label:''
        }
        standardTile("silent", "device.silent") {
			state "default", label: "Silent", action: "silent", icon:"https://postfiles.pstatic.net/MjAxODAzMjlfMTAy/MDAxNTIyMzIzNjE4NjE2.2N1NVfE2fmK85H1EhwK_gqEs0FK0qSaJ1KCimGnxZFcg.CAcpOhL3yJXAlvS-JoBcGz1Uf2UnjuICzGs4hBwwK8kg.PNG.shin4299/Fan_20.png?type=w580", backgroundColor:"#b1d6de"
		}
        standardTile("standard", "device.standard") {
			state "default", label: "Standard", action: "standard", icon:"https://postfiles.pstatic.net/MjAxODAzMjlfMzIg/MDAxNTIyMzIzNjE4NjE2.8HySZX7X1Lb821PxhP96mahNs7dxuYcmDYqy-8bczT8g.wMnYS-sYxbbqXBFrK06w7fT_I6sBb1IcmznRVMOrjjEg.PNG.shin4299/Fan_60.png?type=w580", backgroundColor:"#b1d6de"
		}
        standardTile("medium", "device.medium") {
			state "default", label: "Medium", action: "medium", icon:"https://postfiles.pstatic.net/MjAxODAzMjlfNTgg/MDAxNTIyMzIzNDI2NjE2.86i1P_l290aYfdzh9fATsl3VA-dCVAba9ir_1Ym3mlIg.gyZmaDisBZAbtzzSg-55iwk2ie1ijd64x4ZTo5Jbu4Eg.PNG.shin4299/Fan_30.png?type=w580", backgroundColor:"#b1d6de"
		}
        standardTile("turbo", "device.turbo") {
			state "default", label: "Turbo", action: "turbo", icon:"https://postfiles.pstatic.net/MjAxODAzMjlfMjIw/MDAxNTIyMzIzNjE4NjIx.t6DneqY6JyAZAicutP3NtV9Vf0wWGNAXWnVDIxnL_0gg.-5LlfL2aVTqW3ziuAXWOHFQ6C436d5-XZc_NVHxgS9Mg.PNG.shin4299/Fan_120.png?type=w580", backgroundColor:"#b1d6de"
		}
        valueTile("fanSpeed", "device.fanSpeed", decoration: "flat", width: 2, height: 1) {
            state "default", label:'Fan Speed: ${currentValue}'
        }
       
        standardTile("paused", "device.paused", width: 1, height: 1) {
            state "paused", label:'Pause', action:"paused", backgroundColor:"#00a0dc"
        }
        standardTile("restart", "device.restart", width: 1, height: 1) {
            state "restart", label:'Restart', action:"on", backgroundColor:"#09540d"
        }
            
        standardTile("charge", "device.charge", width: 1, height: 1 ) {
            state "charge", label:'Charge', action:"charge",  backgroundColor:"#25a896"
        }
        
        valueTile("battery", "device.battery",  height: 1, width: 1) {
            state "val", label:'${currentValue}%', defaultState: true,
            	backgroundColors:[
                    [value: 10, color: "#ff002a"],
                    [value: 20, color: "#f4425f"],
                    [value: 30, color: "#ef7085"],
                    [value: 40, color: "#ea8f9e"],
                    [value: 50, color: "#edadb7"],
                    [value: 60, color: "#a9aee8"],
                    [value: 70, color: "#7f87e0"],
                    [value: 80, color: "#505bd3"],
                    [value: 90, color: "#2131e0"]
                ]
        }
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "default", label:"", action:"refresh", icon:"st.secondary.refresh"
        }
        
        valueTile("empty2_label", "", decoration: "flat", width: 4, height: 1) {
            state "default", label:''
        }
        valueTile("label_clean_time", "", decoration: "flat", width: 2, height: 1) {
            state "default", label:'Clean Time'
        }
        valueTile("cleanTime", "device.cleanTime", decoration: "flat", width: 4, height: 1) {
            state "default", label:'${currentValue}'
        }
        main (["mode"])
      	details(["mode", "switch", "paused", "restart", "charge", "empty1_label", "silent", "standard", "medium", "turbo", "fanSpeed", "battery", "refresh", "empty2_label", "label_clean_time", "cleanTime", ])
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

def setStatus(params){
	log.debug "${params.key} >> ${params.data}"
    
 	switch(params.key){
    case "state":
    	sendEvent(name:"mode", value: params.data )
        if(params.data == "cleaning"){
        	sendEvent(name:"switch", value: "on", displayed: false)
        	sendEvent(name:"robotCleanerCleaningMode", value: "auto", displayed: false)
        }else{
        	sendEvent(name:"switch", value: "off", displayed: false)
        	sendEvent(name:"robotCleanerCleaningMode", value: "stop", displayed: false)
        }
    	break;
    case "batteryLevel":
    	sendEvent(name:"battery", value: params.data, displayed: false)
    	break;
    case "fanSpeed":
    	def _value = "Unknown"
        switch(params.data.toInteger()){
        case 0:
        	_value = "Silent"
        	break;
        case 1:
        	_value = "Standard"
        	break;
        case 2:
        	_value = "Medium"
        	break;
        case 3:
        	_value = "Turbo"
        	break;
        }
    	sendEvent(name:"fanSpeed", value: _value )
    	break;
    case "cleanTime":
    	sendEvent(name:"cleanTime", value: formatSeconds(params.data as int), displayed: false)
    	break    
    }
    
    
    updateLastTime()
}

def setRobotCleanerCleaningMode(mode){
	switch(mode){
    case "auto":
    	on()
    	break
    case "stop":
    	off()
    	break
    }
}

public String formatSeconds(int timeInSeconds){
    int secondsLeft = timeInSeconds % 3600 % 60;
    int minutes = Math.floor(timeInSeconds % 3600 / 60);
    int hours = Math.floor(timeInSeconds / 3600);

    String HH = hours < 10 ? "0" + hours : hours;
    String MM = minutes < 10 ? "0" + minutes : minutes;
    String SS = secondsLeft < 10 ? "0" + secondsLeft : secondsLeft;

    return HH + ":" + MM + ":" + SS;
}

def updateLastTime(){
	def now = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)
    sendEvent(name: "lastCheckin", value: now, displayed: false)
}

def refresh(){
	log.debug "Refresh"
    def options = [
     	"method": "GET",
        "path": "/devices/get/${state.id}",
        "headers": [
        	"HOST": parent._getServerURL(),
            "Content-Type": "application/json"
        ]
    ]
    sendCommand(options, callback)
}

def silent(){
    def body = [
        "id": state.id,
        "cmd": "fanSpeed",
        "data": 0
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def standard(){
    def body = [
        "id": state.id,
        "cmd": "fanSpeed",
        "data": 1
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def medium(){
    def body = [
        "id": state.id,
        "cmd": "fanSpeed",
        "data": 2
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def turbo(){
    def body = [
        "id": state.id,
        "cmd": "fanSpeed",
        "data": 3
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def charge(){
    def body = [
        "id": state.id,
        "cmd": "charge"
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def paused(){
    def body = [
        "id": state.id,
        "cmd": "pause"
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def start(){
    def body = [
        "id": state.id,
        "cmd": "start"
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def clean(){
    def body = [
        "id": state.id,
        "cmd": "clean"
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def on(){
    clean()
}

def off(){
	if(offOption == "off"){
        log.debug "Off >> ${state.id}"
        def body = [
            "id": state.id,
            "cmd": "stop"
        ]
        def options = makeCommand(body)
        sendCommand(options, null)
    }else{
    	charge()
    }
}

def callback(physicalgraph.device.HubResponse hubResponse){
	def msg
    try {
        msg = parseLanMessage(hubResponse.description)
		def jsonObj = new JsonSlurper().parseText(msg.body)
        
        sendEvent(name:"battery", value: jsonObj.properties.batteryLevel)
        sendEvent(name:"mode", value: jsonObj.state.state)
        sendEvent(name:"switch", value: (jsonObj.properties.cleaning ? "on" : "off") )
       	sendEvent(name:"paused", value: jsonObj.properties.cleaning ? "paused" : "restart" )  
        
        sendEvent(name:"cleanTime", value: formatSeconds(jsonObj.properties.cleanTime), displayed: false)
        
        def fanSpeed;
        switch(jsonObj.state.fanSpeed){
        case 0:
        	_value = "Silent"
        	break;
        case 1:
        	_value = "Standard"
        	break;
        case 2:
        	_value = "Medium"
        	break;
        case 3:
        	_value = "Turbo"
        	break;
        }
    	sendEvent(name:"fanSpeed", value: fanSpeed )
        
        updateLastTime()
    } catch (e) {
        log.error "Exception caught while parsing data: "+e;
    }
}

def updated() {
}

def sendCommand(options, _callback){
	def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: _callback])
    sendHubCommand(myhubAction)
}

def makeCommand(body){
	def options = [
     	"method": "POST",
        "path": "/control",
        "headers": [
        	"HOST": parent._getServerURL(),
            "Content-Type": "application/json"
        ],
        "body":body
    ]
    return options
}