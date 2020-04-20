/**
 *  Trend Setter - Colorful Light Group Device
 *
 *  Copyright 2015 Chris Kitch
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
	definition (name: "Colorful Light Group Device", namespace: "kriskit.trendSetter", author: "Chris Kitch") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
        capability "Color Control"
        
        command "adjustLevel"
        command "adjustSaturation"
        command "adjustHue"
        
        attribute "onPercentage", "number"
        attribute "levelSync", "string"
        attribute "colorSync", "string"
        attribute "saturationSync", "string"
        attribute "hueSync", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "half", label: '${name}', action: "switch.on", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#a3d164", nextState: "turningOn"
                attributeState "mostlyOn", label: 'Onish', action: "switch.on", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#79b821", nextState: "turningOn"
                attributeState "mostlyOff", label: 'Offish', action: "switch.off", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#d1e5b5", nextState: "turninOff"
			}
            
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
            	attributeState "color", action: "color control.setColor"
            }
            
			tileAttribute ("device.onPercentage", key: "SECONDARY_CONTROL") {
				attributeState "onPercentage", label:'${currentValue}% On'
                attributeState "100", label:'All On'
                attributeState "0", label:'All Off'
			}
            
            tileAttribute("device.level", key: "SLIDER_CONTROL") {
              attributeState "default", label: '', action: "switch level.setLevel"
    		}
		}
        
        standardTile("levelLabel", "levelLable", height:1, width:1, decoration: "flat", inactiveLabel: true) {
            state "default", label:"Level", unit:"", icon: "st.illuminance.illuminance.light"
        }
        
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
            state "level", action:"switch level.setLevel"
        }
        
        valueTile("levelValue", "device.level", inactiveLabel: true, height:1, width:1, decoration: "flat") {
            state "default", label:'${currentValue}%', unit:""
        }
        
        valueTile("levelSync", "device.levelSync", height:1, width:1) {
            state "default", label:' Sync ', unit:"", action: "adjustLevel", backgroundColor: "#ff9900"
            state "ok", label:'', unit:"", backgroundColor: "#00b509"
        }
        
        standardTile("saturationLabel", "saturationLabel", height:1, width:1, decoration: "flat", inactiveLabel: true) {
            state "default", label:"Sat", unit:"", icon: "st.Kids.kids2"
        }
        
        controlTile("saturationSliderControl", "device.saturation", "slider", height: 1, width: 3, inactiveLabel: false) {
            state "saturation", action:"color control.setSaturation"
        }
        
        valueTile("saturationValue", "device.saturation", inactiveLabel: true, height:1, width:1, decoration: "flat") {
            state "default", label:'${currentValue}%', unit:""
        }
        
        valueTile("saturationSync", "device.saturationSync", height:1, width:1) {
            state "default", label:' Sync ', unit:"", action: "adjustSaturation", backgroundColor: "#ff9900"
            state "ok", label:'', unit:"", backgroundColor: "#00b509"
        }
        
        standardTile("hueLabel", "hueLabel", height:1, width:1, decoration: "flat", inactiveLabel: true) {
            state "default", label:"Hue", unit:"", icon: "st.Kids.kids2"
        }
        
        controlTile("hueSliderControl", "device.hue", "slider", height: 1, width: 3, inactiveLabel: false) {
            state "hue", action:"color control.setHue"
        }
        
        valueTile("hueValue", "device.hue", inactiveLabel: true, height:1, width:1, decoration: "flat") {
            state "default", label:'${currentValue}%', unit:""
        }
        
        valueTile("hueSync", "device.hueSync", height:1, width:1) {
            state "default", label:' Sync ', unit:"", action: "adjustHue", backgroundColor: "#ff9900"
            state "ok", label:'', unit:"", backgroundColor: "#00b509"
        }
	}
    
    main "switch"
    details([
    	"switch", 
        "levelLabel", 
        "levelSliderControl", 
        "levelValue", 
        "levelSync", 
        "saturationLabel", 
        "saturationSliderControl", 
        "saturationValue", 
        "saturationSync",
        "hueLabel",
        "hueSliderControl",
        "hueValue",
        "hueSync"])
}

def parse(String description) {
}

def groupSync(name, values) {
	try {
    	"sync${name.capitalize()}"(values)	
    } catch(ex) {
    	log.error "Error executing 'sync${name.capitalize()}' method: $ex"
    }
}

// SWITCH
def on() {
	on(true)
}

def on(triggerGroup) {
	sendEvent(name: "switch", value: "on")
    sendEvent(name: "onPercentage", value: 100, displayed: false)
    
    if (triggerGroup)
    	parent.performGroupCommand("on")
}

def off() {
	off(true)
}

def off(triggerGroup) {
	sendEvent(name: "switch", value: "off")
    sendEvent(name: "onPercentage", value: 0, displayed: false)
    
    if (triggerGroup)
    	parent.performGroupCommand("off")
}

def syncSwitch(values) {
	log.debug "syncSwitch(): $values"
    
    def onCount = values?.count { it == "on" }
    def percentOn = (int)Math.floor((onCount / values?.size()) * 100)
    
    log.debug "Percent On: $percentOn"
    
    if (percentOn == 0 || percentOn == 100) {
    	if (percentOn == 0)
        	off(false)
        else
        	on(false)            
        return
    }
    
    def value = null
    
    if (percentOn == 50)
    	value = "half"
    else if (percentOn > 0 && percentOn < 50)
		value = "mostlyOff"
    else if (percentOn > 50 && percentOn < 100)
		value = "mostlyOn"
        
	sendEvent(name: "switch", value: value)
	sendEvent(name: "onPercentage", value: percentOn, displayed: false)
}

// LEVEL
def setLevel(val) {
	setLevel(val, true)
}

def setLevel(val, triggerGroup) {
	log.debug "Setting level to $val"

    if (val < 0)
    	val = 0
    
    if( val > 100)
    	val = 100
    
    if (triggerGroup) {
       if (val == 0)
    	   off()
       else
    	   on()
    }
        
    sendEvent(name: "level", value: val, isStateChange: true)
    sendEvent(name: "switch.setLevel", value: val, isStateChange: true)
    
    if (triggerGroup)
    	parent.performGroupCommand("setLevel", [val])
}

def syncLevel(values) {
	log.debug "syncLevel(): $values"
    
    def valueCount = values?.size()
    def valueCountBy = values?.countBy { it }
    def matchValue = "bad"
    def level = device.currentValue("level")
    
    valueCountBy.each { value, count -> 
    	if (count == valueCount) {
        	level = value
            matchValue = "ok"
        	return true
        }
    }
    
    if (matchValue == "bad")
    	level = getAdjustmentLevel(values)
    
    setLevel(level, false)
    sendEvent(name: "levelSync", value: matchValue, displayed: false)
}

def adjustLevel() {
	def values = parent.getGroupCurrentValues("level")
    
    if (!values)
    	return

    def level = getAdjustmentLevel(values)    
    setLevel(level)
}

def getAdjustmentLevel(values) {
    if (!values)
    	return
        
    def valueCountBy = values?.countBy { it }
    valueCountBy = valueCountBy?.sort { a, b -> b.value <=> a.value }
    
    def level = device.currentValue("level")
    
    if (valueCountBy.size() > 1) {        
        if (valueCountBy.size() == values.size()) {
        	log.debug "Values are all different - making average"
            level = Math.round(values.sum() / values.size())
        } else {
			log.debug "Some values are the same, choosing most popular"
            def firstItem = valueCountBy.find { true }
            level = firstItem.key
        }
    }
    
    return level
}

// COLOR
def setColor(value) {
	setColor(value, true)
}

def setColor(value, triggerGroup) {
	value.level = null
    
    def hex = value.hex
    
    if (!hex && value.hue && value.saturation)
		hex = colorUtil.hslToHex(value.hue, value.saturation)
        
	sendEvent(name: "color", value: value.hex, displayed:false)
    
    if (triggerGroup)
    	parent.performGroupCommand("setColor", [value])

	if (value.saturation)
		setSaturation(value.saturation, triggerGroup, false)
        
    if (value.hue)
    	setHue(value.hue, triggerGroup, false)
}

def syncColor(values) {
	log.debug "syncColor(): $values"
}

// SATURATION
def setSaturation(value) {
	setSaturation(value, true, true)
}

def setSaturation(value, triggerGroup, sendColor) {
	on(triggerGroup)

	sendEvent(name: "saturation", value: (int)value, displayed:false)
    
    if (triggerGroup)
    	parent.performGroupCommand("setSaturation", [value])
    
    if (sendColor) {
    	def hex = colorUtil.hslToHex((int)device.currentValue("hue"), value)
    	sendEvent(name: "color", value: hex, displayed:false)	
    }
}

def syncSaturation(values) {
	log.debug "syncSaturation(): $values"
    
    def valueCount = values?.size()
    def valueCountBy = values?.countBy { it }
    def matchValue = "bad"
    
    valueCountBy.each { value, count -> 
    	if (count == valueCount) {
            matchValue = "ok"
        	return true
        }
    }
    
    sendEvent(name: "saturationSync", value: matchValue, displayed: false)
}

def adjustSaturation() {
    def saturation = (int)device.currentValue("saturation")    
	log.debug "adjustSaturation $saturation"
    setSaturation(saturation)
}

// HUE
def setHue(value) {
	setHue(value, true, true)
}

def setHue(value, triggerGroup, sendColor) {
	on(triggerGroup)
	sendEvent(name: "hue", value: (int)value, displayed: false)
    
    if (triggerGroup)
    	parent.performGroupCommand("setHue", [value])
    
    if (sendColor) {
    	def hex = colorUtil.hslToHex(value, (int)device.currentValue("saturation"))
    	sendEvent(name: "color", value: hex, displayed:false)	
    }
}

def syncHue(values) {
	log.debug "syncHue(): $values"
    
    def valueCount = values?.size()
    def valueCountBy = values?.countBy { it }
    def matchValue = "bad"
    
    valueCountBy.each { value, count -> 
    	if (count == valueCount) {
            matchValue = "ok"
        	return true
        }
    }
    
    sendEvent(name: "hueSync", value: matchValue, displayed: false)
}

def adjustHue() {
    def hue = (int)device.currentValue("hue")    
	log.debug "adjustHue: $hue"
    setHue(hue)
}