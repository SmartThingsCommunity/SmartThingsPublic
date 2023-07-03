/**
 *  Tuya Power Strip Child (v.0.0.1)
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
	definition (name: "Tuya Power Strip Child", namespace: "fison67", author: "fison67") {
        capability "Switch"			
        attribute "lastCheckin", "Date"
        command "setTimer", ["number"]
        command "stop"
	}

	simulator { }

	preferences { }
    
	tiles {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"off", icon:"https://github.com/fison67/mi_connector/blob/master/icons/powerStrip_on.png?raw=true", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"on", icon:"https://github.com/fison67/mi_connector/blob/master/icons/powerStrip_off.png?raw=true", backgroundColor:"#ffffff", nextState:"turningOn"
                
                attributeState "turningOn", label:'${name}', action:"off", icon:"https://github.com/fison67/mi_connector/blob/master/icons/powerStrip_on.png?raw=true", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"on", icon:"https://github.com/fison67/mi_connector/blob/master/icons/powerStrip_off.png?raw=true", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Updated: ${currentValue}',icon: "st.Health & Wellness.health9")
            }
		}
        valueTile("lastOn_label", "", decoration: "flat") {
            state "default", label:'Last\nOn'
        }
        valueTile("lastOn", "device.lastOn", decoration: "flat", width: 3, height: 1) {
            state "default", label:'${currentValue}'
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:""
        }
        valueTile("lastOff_label", "", decoration: "flat") {
            state "default", label:'Last\nOff'
        }
        valueTile("lastOff", "device.lastOff", decoration: "flat", width: 3, height: 1) {
            state "default", label:'${currentValue}'
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
		details(["switch", "lastOn_label", "lastOn", "refresh", "lastOff_label", "lastOff", "timer_label", "time", "tiemr0"])
       
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def setID(id){
	state.id = id
}

def setStatus(type, data){
	log.debug "Type[${type}], data >> ${data}"
    def now = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)
    sendEvent(name: "lastCheckin", value: now, displayed: false)
    
    switch(type){
    case "power":
        if(device.currentValue("switch") != (data ? "on" : "off")){
            sendEvent(name: (data ? "lastOn" : "lastOff"), value: now, displayed: false )
        }
        sendEvent(name: "switch", value: data ? "on" : "off")
    	break
    case "timer":
    	def timeStr = msToTime(data)
    	sendEvent(name:"leftTime", value: "${timeStr}", displayed: false)
    	sendEvent(name:"time", value: Math.round(data/60), displayed: false)
    	break
    }
}

def getID(){
	return state.id
}

def on(){
	parent.childOn(state.id)
}

def off(){
	parent.childOff(state.id)
}

def stop(){
	parent.childTimer(((state.id as int) + 8).toString(), 0)
}

def setTimer(second){
	parent.childTimer(((state.id as int) + 8).toString(), second * 60)
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