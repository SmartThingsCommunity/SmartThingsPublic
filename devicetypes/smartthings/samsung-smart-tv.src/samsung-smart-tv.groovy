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
 *  Samsung TV
 *
 *  Author: SmartThings (juano23@gmail.com)
 *  Date: 2015-01-08
 */
 
metadata {
	definition (name: "Samsung Smart TV", namespace: "smartthings", author: "SmartThings") {
    		capability "switch" 
            
			command "mute" 
			command "source"
			command "menu"    
            command "tools"           
			command "HDMI"    
            command "Sleep"
            command "Up"
            command "Down"
            command "Left"
            command "Right" 
			command "chup" 
 			command "chdown"               
			command "prech"
			command "volup"    
            command "voldown"           
            command "Enter"
            command "Return"
            command "Exit"
            command "Info"            
            command "Size"
	}

    standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
        state "default", label:'TV', action:"switch.off", icon:"st.Electronics.electronics15", backgroundColor:"#ffffff"
    }
    standardTile("power", "device.switch", width: 1, height: 1, canChangeIcon: false) {
        state "default", label:'', action:"switch.off", decoration: "flat", icon:"st.thermostat.heating-cooling-off", backgroundColor:"#ffffff"
    }    
    standardTile("mute", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Mute', action:"mute", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff"
    }    
	standardTile("source", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Source', action:"source", icon:"st.Electronics.electronics15"
    }
	standardTile("tools", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Tools', action:"tools", icon:"st.secondary.tools"
    }
	standardTile("menu", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Menu', action:"menu", icon:"st.vents.vent"
    }
	standardTile("HDMI", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Source', action:"HDMI", icon:"st.Electronics.electronics15"
    }
    standardTile("Sleep", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Sleep', action:"Sleep", icon:"st.Bedroom.bedroom10"
    }
    standardTile("Up", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Up', action:"Up", icon:"st.thermostat.thermostat-up"
    }
    standardTile("Down", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Down', action:"Down", icon:"st.thermostat.thermostat-down"
    }
    standardTile("Left", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Left', action:"Left", icon:"st.thermostat.thermostat-left"
    }
    standardTile("Right", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Right', action:"Right", icon:"st.thermostat.thermostat-right"
    }  
	standardTile("chup", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'CH Up', action:"chup", icon:"st.thermostat.thermostat-up"
    }
	standardTile("chdown", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'CH Down', action:"chdown", icon:"st.thermostat.thermostat-down"
    }
	standardTile("prech", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Pre CH', action:"prech", icon:"st.secondary.refresh-icon"
    }
    standardTile("volup", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Vol Up', action:"volup", icon:"st.thermostat.thermostat-up"
    }
    standardTile("voldown", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Vol Down', action:"voldown", icon:"st.thermostat.thermostat-down"
    }
    standardTile("Enter", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Enter', action:"Enter", icon:"st.illuminance.illuminance.dark"
    }
    standardTile("Return", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Return', action:"Return", icon:"st.secondary.refresh-icon"
    }
    standardTile("Exit", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Exit', action:"Exit", icon:"st.locks.lock.unlocked"
    }    
    standardTile("Info", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Info', action:"Info", icon:"st.motion.acceleration.active"
    }    
    standardTile("Size", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Picture Size', action:"Size", icon:"st.contact.contact.open"
    }      
    main "switch"
    details (["power","HDMI","Sleep","chup","prech","volup","chdown","mute","voldown", "menu", "Up", "tools", "Left", "Enter", "Right", "Return", "Down", "Exit", "Info","Size"])	
}

def parse(String description) {
	return null
}

def off() {
	log.debug "Turning TV OFF"
    parent.tvAction("POWEROFF",device.deviceNetworkId) 
    sendEvent(name:"Command", value: "Power Off", displayed: true) 
}

def mute() {
	log.trace "MUTE pressed"
    parent.tvAction("MUTE",device.deviceNetworkId) 
    sendEvent(name:"Command", value: "Mute", displayed: true) 
}

def source() {
	log.debug "SOURCE pressed"
    parent.tvAction("SOURCE",device.deviceNetworkId) 
    sendEvent(name:"Command", value: "Source", displayed: true) 
}

def menu() {
	log.debug "MENU pressed"
    parent.tvAction("MENU",device.deviceNetworkId) 
}

def tools() {
	log.debug "TOOLS pressed"
    parent.tvAction("TOOLS",device.deviceNetworkId) 
    sendEvent(name:"Command", value: "Tools", displayed: true)     
}

def HDMI() {
	log.debug "HDMI pressed"
    parent.tvAction("HDMI",device.deviceNetworkId) 
    sendEvent(name:"Command sent", value: "Source", displayed: true)
}

def Sleep() {
	log.debug "SLEEP pressed"
    parent.tvAction("SLEEP",device.deviceNetworkId) 
    sendEvent(name:"Command", value: "Sleep", displayed: true)
}

def Up() {
	log.debug "UP pressed"
    parent.tvAction("UP",device.deviceNetworkId)
}

def Down() {
	log.debug "DOWN pressed"
    parent.tvAction("DOWN",device.deviceNetworkId) 
}

def Left() {
	log.debug "LEFT pressed"
    parent.tvAction("LEFT",device.deviceNetworkId) 
}

def Right() {
	log.debug "RIGHT pressed"
    parent.tvAction("RIGHT",device.deviceNetworkId) 
}

def chup() {
	log.debug "CHUP pressed"
    parent.tvAction("CHUP",device.deviceNetworkId)
    sendEvent(name:"Command", value: "Channel Up", displayed: true)         
}

def chdown() {
	log.debug "CHDOWN pressed"
    parent.tvAction("CHDOWN",device.deviceNetworkId) 
    sendEvent(name:"Command", value: "Channel Down", displayed: true)     
}

def prech() {
	log.debug "PRECH pressed"
    parent.tvAction("PRECH",device.deviceNetworkId)
    sendEvent(name:"Command", value: "Prev Channel", displayed: true)       
}

def Exit() {
	log.debug "EXIT pressed"
    parent.tvAction("EXIT",device.deviceNetworkId) 
}

def volup() {
	log.debug "VOLUP pressed"
    parent.tvAction("VOLUP",device.deviceNetworkId)
    sendEvent(name:"Command", value: "Volume Up", displayed: true)         
}

def voldown() {
	log.debug "VOLDOWN pressed"
    parent.tvAction("VOLDOWN",device.deviceNetworkId) 
    sendEvent(name:"Command", value: "Volume Down", displayed: true)         
}

def Enter() {
	log.debug "ENTER pressed"
    parent.tvAction("ENTER",device.deviceNetworkId) 
}

def Return() {
	log.debug "RETURN pressed"
    parent.tvAction("RETURN",device.deviceNetworkId) 
}

def Info() {
	log.debug "INFO pressed"
    parent.tvAction("INFO",device.deviceNetworkId) 
	sendEvent(name:"Command", value: "Info", displayed: true)    
}

def Size() {
	log.debug "PICTURE_SIZE pressed"
    parent.tvAction("PICTURE_SIZE",device.deviceNetworkId) 
    sendEvent(name:"Command", value: "Picture Size", displayed: true)
}