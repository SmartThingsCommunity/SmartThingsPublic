/**
 *  Copyright 2015 Stuart Buchanan
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
 *	Tado AC Thermostat
 *
 *	Author: Stuart Buchanan, Based on original work by Ian M with thanks. also source for icons was from @tonesto7's excellent Nest Manager.
 *	Date: 2016-11-28 v3.0 Moved all data collection functions into Tado (Connect) SmartApp, huge changes to device handler, existing devices and handler will need to be uninstalled before installing this version
 *	Date: 2016-07-13 v2.9 Quick dirty workaround to control zones with a single account.
 *	Date: 2016-05-07 v2.8 Corrected issue with Fan Speed commands not working.
 *	Date: 2016-04-25 v2.7 Minor changes to thermostatOperatingState to Show "idle" and "fan only" state
 *	Date: 2016-04-25 v2.6 Minor bug fix to correct issue with reading existing set point value.
 *	Date: 2016-04-09 v2.5 Major bug fix exercise, found lots and lots and lots.....now 100% conforms to ST Thermostat capability. main panel now shows colour of operating state. new attributes tadoMode and tadoFanSpeed created.
 *	Date: 2016-04-05 v2.4 Performed Testing with Thermostat Mode Director and found some deficiencies where this would not work correctly. i have now corrected, this now works fine and has been tested.
 *	Date: 2016-04-05 v2.3 added device preference for default temps for some commands as requested by @mitchell_lu66, also added some additional refreshes and error control for unsupported capabilities
 *	Date: 2016-04-05 v2.2 Added Fan Speed & Emergency Heat (1 Hour) Controls and also a manual Mode End function to fall back to Tado Control.
 						  Also added preference for how long manual mode runs for either ends at Tado Mode Change (TADO_MODE) or User Control (MANUAL),
                          please ensure the default method is Set in the device properties
 *	Date: 2016-04-05 v2.1 Minor Bug Fixes & improved Icons
 *	Date: 2016-04-05 v2.0 Further Changes to MultiAttribute Tile
 *	Date: 2016-04-05 v1.9 Amended Device Handler Name
 *	Date: 2016-04-05 v1.8 Added all thermostat related capabilities
 *  Date: 2016-04-05 v1.7 Amended device to be capable of both Fahrenheit and celsius and amended the Device multiattribute tile
 *  Date: 2016-04-05 v1.6 switched API calls to new v2 calls as the old ones had been deprecated.
 *  Date: 2016-02-21 v1.5 switched around thermostatOperatingState & thermostatMode to get better compatibility with Home Remote
 *  Date: 2016-02-21 v1.4 added HeatingSetPoint & CoolingSetPoint to make compatible with SmartTiles
 *  Date: 2016-02-21 v1.3 amended the read thermostat properties to match the ST Thermostat Capability
 *  Date: 2016-02-14 v1.2 amended the thermostat properties to match the ST Capability.Thermostat values
 *  Date: 2016-01-23 v1.1 fixed error in Tado Mode detection
 *	Date: 2016-01-22 v1.1 Add Heating & Cooling Controls (initial offering, will need to look into adding all possible commands)
 *	Date: 2015-12-04 v1.0 Initial Release With Temperatures & Relative Humidity
 */

import groovy.json.JsonOutput

preferences {
}

metadata {
	definition (name: "Tado Cooling Thermostat", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Actuator"
    capability "Temperature Measurement"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Fan Mode"
		capability "Thermostat Setpoint"
		capability "Thermostat Operating State"
		capability "Thermostat"
		capability "Relative Humidity Measurement"
		capability "Polling"
		capability "Refresh"

    attribute "tadoMode", "string"
    attribute "tadoFanSpeed", "string"
    command "temperatureUp"
    command "temperatureDown"
    command "heatingSetpointUp"
    command "heatingSetpointDown"
    command "coolingSetpointUp"
    command "coolingSetpointDown"
    command "cmdFanSpeedAuto"
    command "cmdFanSpeedHigh"
    command "cmdFanSpeedMid"
    command "cmdFanSpeedLow"
    command "dry"
    command "on"
		command "fan"
    command "endManualControl"
    command "emergencyHeat"
	}

	// simulator metadata
	simulator {
		// status messages

		// reply messages
	}

	tiles(scale: 2){
    multiAttributeTile(name: "thermostat", type:"thermostat", width:6, height:4) {
      tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeIcon: true, canChangeBackground: true){
        attributeState "default", label:'${currentValue}°', backgroundColor:"#fab907", icon:"st.Home.home1"
      }
      tileAttribute("device.temperature", key: "VALUE_CONTROL") {
        attributeState("VALUE_UP", action: "temperatureUp")
        attributeState("VALUE_DOWN", action: "temperatureDown")
      }
      tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
        attributeState("default", label:'${currentValue}%', unit:"%")
      }
      tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
        attributeState("idle", backgroundColor:"#666666")
        attributeState("heating", backgroundColor:"#ff471a")
        attributeState("cooling", backgroundColor:"#1a75ff")
        attributeState("emergency heat", backgroundColor:"#ff471a")
        attributeState("drying", backgroundColor:"#c68c53")
        attributeState("fan only", backgroundColor:"#39e600")
        attributeState("heating|cooling", backgroundColor:"#ff9900")
      }
      tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
        attributeState("off", label:'${name}')
        attributeState("heat", label:'${name}')
        attributeState("cool", label:'${name}')
        attributeState("auto", label:'${name}')
        attributeState("fan", label:'${name}')
        attributeState("dry", label:'${name}')
      }
      tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
        attributeState("default", label:'${currentValue}', unit:"dF")
      }
      tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
        attributeState("default", label:'${currentValue}', unit:"dF")
      }
    }

    standardTile("tadoMode", "device.tadoMode", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
      state("SLEEP", label:'${name}', backgroundColor:"#0164a8", icon:"st.Bedroom.bedroom2")
      state("HOME", label:'${name}', backgroundColor:"#fab907", icon:"st.Home.home2")
      state("AWAY", label:'${name}', backgroundColor:"#62aa12", icon:"st.Outdoor.outdoor18")
      state("OFF", label:'${name}', backgroundColor:"#ffffff", icon:"st.switches.switch.off", defaultState: true)
      state("MANUAL", label:'${name}', backgroundColor:"#804000", icon:"st.Weather.weather1")
    }

    standardTile("refresh", "device.switch", inactiveLabel: false, width: 2, height: 1, decoration: "flat") {
      state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }

    standardTile("thermostatMode", "device.thermostatMode", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
      state("heat", label:'HEAT', backgroundColor:"#ea2a2a", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/heat_mode_icon.png")
      state("emergency heat", label:'HEAT', backgroundColor:"#ea2a2a", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/heat_mode_icon.png")
      state("cool", label:'COOL', backgroundColor:"#089afb", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/cool_mode_icon.png")
      state("dry", label:'DRY', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/dry_mode_icon.png")
      state("fan", label:'FAN', backgroundColor:"#ffffff", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_mode_icononly.png")
      state("auto", label:'AUTO', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/auto_mode_icon.png")
      state("off", label:'', backgroundColor:"#ffffff", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_off.png", defaultState: true)
		}

		valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Set Point\r\n\${currentValue}°'
		}

    valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Set Point\r\n\${currentValue}°'
		}

    valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Set Point\r\n\${currentValue}°'
		}

		valueTile("outsidetemperature", "device.outsidetemperature", width: 2, height: 1, decoration: "flat") {
			state "outsidetemperature", label: 'Outside Temp\r\n${currentValue}°'
		}

		standardTile("tadoFanSpeed", "device.tadoFanSpeed", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true, decoration: "flat") {
      state("OFF", label:'', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_off_icon.png", defaultState: true)
      state("AUTO", label:'', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_auto_icon.png")
      state("HIGH", label:'', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_high_icon.png")
      state("MIDDLE", label:'', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_med_icon.png")
      state("LOW", label:'', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_low_icon.png")
		}

		standardTile("setAuto", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.auto", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_auto.png"
		}
    standardTile("setDry", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"dry", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_dry.png"
		}
    standardTile("setOn", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"on", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_on.png"
		}
    standardTile("setOff", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.off", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_off.png"
		}
    standardTile("cool", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.cool", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_cool.png"
		}
    standardTile("heat", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.heat", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_heat.png"
		}
    standardTile("emergencyHeat", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.emergencyHeat", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/emergencyHeat.png"
		}
    standardTile("fan", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.fan", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_mode_icon.png"
		}
    standardTile("coolingSetpointUp", "device.coolingSetpoint", canChangeIcon: false, decoration: "flat") {
      state "coolingSetpointUp", label:'  ', action:"coolingSetpointUp", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/cool_arrow_up.png"
    }
		standardTile("coolingSetpointDown", "device.coolingSetpoint", canChangeIcon: false, decoration: "flat") {
      state "coolingSetpointDown", label:'  ', action:"coolingSetpointDown", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/cool_arrow_down.png"
    }
		standardTile("heatingSetpointUp", "device.heatingSetpoint", canChangeIcon: false, decoration: "flat") {
      state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/heat_arrow_up.png"
    }
    standardTile("heatingSetpointDown", "device.heatingSetpoint", canChangeIcon: false, decoration: "flat") {
      state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/heat_arrow_down.png"
    }
		standardTile("cmdFanSpeedAuto", "device.thermostat", width: 2, height: 1, canChangeIcon: false, canChangeBackground: true, decoration: "flat") {
      state("default", label:'', action:"cmdFanSpeedAuto",  icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_auto_icon.png")
    }
    standardTile("cmdFanSpeedHigh", "device.thermostat", width: 2, height: 1, canChangeIcon: false, canChangeBackground: true, decoration: "flat") {
      state("default", label:'', action:"cmdFanSpeedHigh",  icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_high_icon.png")
    }
    standardTile("cmdFanSpeedMid", "device.thermostat", width: 2, height: 1, canChangeIcon: false, canChangeBackground: true, decoration: "flat") {
      state("default", label:'', action:"cmdFanSpeedMid",  icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_med_icon.png")
    }
    standardTile("cmdFanSpeedLow", "device.thermostat", width: 2, height: 1, canChangeIcon: false, canChangeBackground: true, decoration: "flat") {
      state("default", label:'', action:"cmdFanSpeedLow",  icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_low_icon.png")
    }
		standardTile("endManualControl", "device.thermostat", width: 2, height: 1, canChangeIcon: false, canChangeBackground: true, decoration: "flat") {
      state("default", label:'', action:"endManualControl", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/endManual.png")
		}

		main(["thermostat"])
		details(["thermostat","thermostatMode","coolingSetpointUp","coolingSetpointDown","autoOperation","heatingSetpointUp","heatingSetpointDown","outsidetemperature","thermostatSetpoint","tadoMode","refresh","tadoFanSpeed","setAuto","setOn","setOff","fan","cool","heat","setDry","cmdFanSpeedAuto","emergencyHeat","endManualControl","cmdFanSpeedLow","cmdFanSpeedMid","cmdFanSpeedHigh"])
  }
}

def setCapabilitytadoType(value){
  state.tadoType = value
  log.debug("state.tadoType = ${state.tadoType}")
}

def getCapabilitytadoType() {
  def map = null
  map = [name: "capabilityTadoType", value: state.tadoType]
  return map
}

def setCapabilitySupportsAuto(value){
  state.supportsAuto = value
  log.debug("state.supportsAuto = ${state.supportsAuto}")
}

def getCapabilitySupportsAuto() {
  def map = null
  map = [name: "capabilitySupportsAuto", value: state.supportsAuto]
  return map
}

def setCapabilitySupportsCool(value){
  state.supportsCool = value
  log.debug("state.supportsCool = ${state.supportsCool}")
}

def getCapabilitySupportsCool() {
  def map = null
  map = [name: "capabilitySupportsCool", value: state.supportsCool]
  return map
}

def setCapabilitySupportsCoolAutoFanSpeed(value){
  state.SupportsCoolAutoFanSpeed = value
  log.debug("state.SupportsCoolAutoFanSpeed = ${state.SupportsCoolAutoFanSpeed}")
}

def getCapabilitySupportsCoolAutoFanSpeed() {
  def map = null
  map = [name: "capabilitySupportsCoolAutoFanSpeed", value: state.supportsCoolAutoFanSpeed]
  return map
}

def setCapabilitySupportsDry(value){
  state.supportsDry = value
  log.debug("state.supportsDry = ${state.supportsDry}")
}

def getCapabilitySupportsDry() {
  def map = null
  map = [name: "capabilitySupportsDry", value: state.supportsDry]
  return map
}

def setCapabilitySupportsFan(value){
  state.supportsFan = value
  log.debug("state.supportsFan = ${state.supportsFan}")
}

def getCapabilitySupportsFan() {
  def map = null
  map = [name: "capabilitySupportsFan", value: state.supportsFan]
  return map
}

def setCapabilitySupportsHeat(value){
  state.supportsHeat = value
  log.debug("state.supportsHeat = ${state.supportsHeat}")
}

def getCapabilitySupportsHeat() {
  def map = null
  map = [name: "capabilitySupportsHeat", value: state.supportsHeat]
  return map
}

def setCapabilitySupportsHeatAutoFanSpeed(value){
  state.SupportsHeatAutoFanSpeed = value
  log.debug("state.SupportsHeatAutoFanSpeed = ${state.SupportsHeatAutoFanSpeed}")
}

def getCapabilitySupportsHeatAutoFanSpeed() {
  def map = null
  map = [name: "capabilitySupportsHeatAutoFanSpeed", value: state.SupportsHeatAutoFanSpeed]
  return map
}

def setCapabilityMaxCoolTemp(value){
  state.MaxCoolTemp = value
  log.debug("set state.MaxCoolTemp to : " + state.MaxCoolTemp)
}

def getCapabilityMaxCoolTemp() {
  def map = null
  map = [name: "capabilityMaxCoolTemp", value: state.MaxCoolTemp]
  return map
}

def setCapabilityMinCoolTemp(value){
  state.MinCoolTemp = value
  log.debug("set state.MinCoolTemp to : " + state.MinCoolTemp)
}

def getCapabilityMinCoolTemp() {
  def map = null
  map = [name: "capabilityMinCoolTemp", value: state.MinCoolTemp]
  return map
}

def setCapabilityMaxHeatTemp(value){
  state.MaxHeatTemp = value
  log.debug("set state.MaxHeatTemp to : " + state.MaxHeatTemp)
}

def getCapabilityMaxHeatTemp() {
  def map = null
  map = [name: "capabilityMaxHeatTemp", value: state.MaxHeatTemp]
  return map
}

def setCapabilityMinHeatTemp(value){
  state.MinHeatTemp = value
  log.debug("set state.MinHeatTemp to : " + state.MinHeatTemp)
}

def getCapabilityMinHeatTemp() {
  def map = null
  map = [name: "capabilityMinHeatTemp", value: state.MinHeatTemp]
  return map
}

def updated(){
	refresh()
}

def installed(){
  refresh()
}

def poll() {
	log.debug "Executing 'poll'"
	refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    parent.statusCommand(this)
    getWeather()
}

def getWeather(){
	parent.weatherStatusCommand(this)
}

def auto() {
	log.debug "Executing 'auto'"
	parent.autoCommand(this)
  parent.statusCommand(this)
}

def on() {
	log.debug "Executing 'on'"
	parent.onCommand(this)
  parent.statusCommand(this)
}

def off() {
	log.debug "Executing 'off'"
	parent.offCommand(this)
  parent.statusCommand(this)
}

def dry() {
	log.debug "Executing 'dry'"
	parent.dryCommand(this)
  parent.statusCommand(this)
}

def setThermostatMode(requiredMode){
	switch (requiredMode) {
    	case "dry":
        	dry()
        break
    	case "heat":
        	heat()
        break
        case "cool":
        	cool()
        break
        case "auto":
        	auto()
        break
        case "fan":
        	fan()
        break
		case "off":
        	off()
        break
		case "emergency heat":
        	emergencyHeat()
        break
     }
}

def thermostatFanMode(requiredMode){
	switch (requiredMode) {
    	case "auto":
        	fan()
        break
    	case "on":
        	fan()
        break
        case "circulate":
        	fan()
        break
     }
}

def setHeatingSetpoint(targetTemperature) {
	log.debug "Executing 'setHeatingSetpoint'"
  log.debug "Target Temperature ${targetTemperature}"
  parent.setHeatingTempCommand(this,targetTemperature)
	refresh()
}

def temperatureUp(){
	if (device.currentValue("thermostatMode") == "heat") {
    	heatingSetpointUp()
    } else if (device.currentValue("thermostatMode") == "cool") {
    	coolingSetpointUp()
    } else {
    	log.debug ("temperature setpoint not supported in the current thermostat mode")
    }
}

def temperatureDown(){
	if (device.currentValue("thermostatMode") == "heat") {
    	heatingSetpointDown()
    } else if (device.currentValue("thermostatMode") == "cool") {
    	coolingSetpointDown()
    } else {
    	log.debug ("temperature setpoint not supported in the current thermostat mode")
    }
}

def heatingSetpointUp(){
	def capabilitysupported = state.supportsHeat
    if (capabilitysupported == "true"){
		log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
    	if ((device.currentValue("thermostatSetpoint").toInteger() - 1 ) < state.MinHeatTemp){
    		log.debug("cannot decrease heat setpoint, its already at the minimum level of " + state.MinHeatTemp)
    	} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() + 1
			log.debug "Setting heatingSetpoint up to: ${newSetpoint}"
			setHeatingSetpoint(newSetpoint)
    	}
    } else {
    	log.debug("Sorry Heat Capability not supported by your HVAC Device")
    }
}

def heatingSetpointDown(){
	def capabilitysupported = state.supportsHeat
    if (capabilitysupported == "true"){
		log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
    	if ((device.currentValue("thermostatSetpoint").toInteger() + 1 ) > state.MaxHeatTemp){
    		log.debug("cannot increase heat setpoint, its already at the maximum level of " + state.MaxHeatTemp)
    	} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() - 1
			log.debug "Setting heatingSetpoint down to: ${newSetpoint}"
			setHeatingSetpoint(newSetpoint)
    	}
    } else {
    	log.debug("Sorry Heat Capability not supported by your HVAC Device")
    }
}

def setCoolingSetpoint(targetTemperature) {
	log.debug "Executing 'setCoolingSetpoint'"
  log.debug "Target Temperature ${targetTemperature}"
  parent.setCoolingTempCommand(this,targetTemperature)
	refresh()
}

def coolingSetpointUp(){
	def capabilitysupported = state.supportsCool
    if (capabilitysupported == "true"){
		log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
    	if ((device.currentValue("thermostatSetpoint").toInteger() + 1 ) > state.MaxCoolTemp){
    		log.debug("cannot increase cool setpoint, its already at the maximum level of " + state.MaxCoolTemp)
    	} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() + 1
			log.debug "Setting coolingSetpoint up to: ${newSetpoint}"
			setCoolingSetpoint(newSetpoint)
    	}
    } else {
    	log.debug("Sorry Cool Capability not supported by your HVAC Device")
    }
}

def coolingSetpointDown(){
	def capabilitysupported = state.supportsCool
    if (capabilitysupported == "true"){
		log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
    	if ((device.currentValue("thermostatSetpoint").toInteger() - 1 ) < state.MinCoolTemp){
    		log.debug("cannot decrease cool setpoint, its already at the minimum level of " + state.MinCoolTemp)
    	} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() - 1
			log.debug "Setting coolingSetpoint down to: ${newSetpoint}"
			setCoolingSetpoint(newSetpoint)
    	}
    } else {
    	log.debug("Sorry Cool Capability not supported by your HVAC Device")
    }
}

def fanOn(){
	fan()
}

def fanCirculate(){
	fan()
}

def cool(){
	def capabilitysupported = state.supportsCool
  if (capabilitysupported == "true"){
		parent.coolCommand(this)
    parent.statusCommand(this)
	} else {
    	log.debug("Sorry Cool Capability not supported by your HVAC Device")
    }
}

def heat(){
	def capabilitysupported = state.supportsHeat
	if (capabilitysupported == "true"){
	parent.heatCommand(this)
  parent.statusCommand(this)
  } else {
  	log.debug("Sorry Heat Capability not supported by your HVAC Device")
  }
}

def fan(){
	parent.fanAuto(this)
	refresh()
}

def emergencyHeat(){
	parent.emergencyHeat(this)
}

def cmdFanSpeedAuto(){
	parent.cmdFanSpeedAuto(this)
}

def cmdFanSpeedHigh(){
	parent.cmdFanSpeedHigh(this)
}

def cmdFanSpeedMed(){
	parent.cmdFanSpeedMed(this)
}

def cmdFanSpeedLow(){
	parent.cmdFanSpeedLow(this)
}

def endManualControl(){
	parent.endManualControl(this)
}
