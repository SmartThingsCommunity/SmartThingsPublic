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
 *	Tado Thermostat
 *
 * 	Author: Stuart Buchanan, Based on original work by Ian M with thanks. also source for icons was from @tonesto7's excellent Nest Manager.
* 	Date: 2016-11-28 v1.6 Moved all data collection functions into Tado (Connect) SmartApp, huge changes to device handler, existing devices and handler will need to be uninstalled before installing this version
 *	Date: 2016-07-13 v1.5 Quick dirty workaround to control zones with a single account.
 * 	Date: 2016-04-25 v1.4 Tado Hot water does not actually return the current water temps, it only returns the Current set point temp. to get around this when the power is on for the hot water botht the temp and setpoint will both display the setpoint value, otherwise will display --
 * 	Date: 2016-04-25 v1.3 Finally found time to update this with the lessons learnt from the Tado Cooling Device Type. will bring better support for RM and Thermostat Director
 * 	Date: 2016-04-08 v1.2 added setThermostatMode(mode) function to work better with Rule Machine and Thermostat Mode Director
 *	Date: 2016-04-05 v1.1 change of default Water Heating Temps can now be defined in device preferences (default Value is 90C).
 *	Date: 2016-04-05 v1.0 Initial release
 */

preferences {
	input("username", "text", title: "Username", description: "Your Tado username")
	input("password", "password", title: "Password", description: "Your Tado password")
	input("tadoZoneId", "number", title: "Enter Tado Zone ID?", required: true)
    input("manualmode", "enum", title: "Default Manual Overide Method", options: ["TADO_MODE","MANUAL"], required: false, defaultValue:"TADO_MODE")
	input("defWaterTemp", "number", title: "Default Water Heating Temperature", required: false, defaultValue: 90)
}

metadata {
	definition (name: "Tado Hot Water Control", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Actuator"
    capability "Temperature Measurement"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Thermostat"
		capability "Polling"
		capability "Refresh"
    attribute "tadoMode", "string"
		command "temperatureUp"
    command "temperatureDown"
    command "heatingSetpointUp"
    command "heatingSetpointDown"
		command "on"
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
            	attributeState "default", label:'${currentValue}°', backgroundColor:"#fab907", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/tap_icon.png"
            }
			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
    			attributeState("VALUE_UP", action: "temperatureUp")
    			attributeState("VALUE_DOWN", action: "temperatureDown")
  			}
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
    			attributeState("idle", backgroundColor:"#666666")
    			attributeState("heating", backgroundColor:"#ff471a")
                attributeState("emergency heat", backgroundColor:"#ff471a")
  			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
    			attributeState("off", label:'${name}')
    			attributeState("heat", label:'${name}')
  			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
    			attributeState("default", label:'${currentValue}', unit:"dF")
  			}
		}

        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Set Point\r\n\${currentValue}°'
		}

        standardTile("tadoMode", "device.tadoMode", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state("SLEEP", label:'${name}', backgroundColor:"#0164a8", icon:"st.Bedroom.bedroom2")
            state("HOME", label:'${name}', backgroundColor:"#fab907", icon:"st.Home.home2")
            state("AWAY", label:'${name}', backgroundColor:"#62aa12", icon:"st.Outdoor.outdoor18")
            state("OFF", label:'', backgroundColor:"#ffffff", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/hvac_off.png", defaultState: true)
            state("MANUAL", label:'${name}', backgroundColor:"#804000", icon:"st.Weather.weather1")
		}

		standardTile("thermostatMode", "device.thermostatMode", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
        	state("heat", label:'HEAT', backgroundColor:"#ea2a2a", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/heat_mode_icon.png")
            state("off", label:'', backgroundColor:"#ffffff", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/hvac_off.png", defaultState: true)
		}

        standardTile("refresh", "device.switch", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("Off", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.off", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/hvac_off.png"
		}
		standardTile("emergencyHeat", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.emergencyHeat", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/emergencyHeat.png"
		}
		valueTile("outsidetemperature", "device.outsidetemperature", width: 2, height: 1, decoration: "flat") {
			state "outsidetemperature", label: 'Outside Temp\r\n${currentValue}°'
		}
		standardTile("heat", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.heat", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/hvac_heat.png"
		}
		standardTile("heatingSetpointUp", "device.heatingSetpoint", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointUp", label:'', action:"heatingSetpointUp", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/heat_arrow_up.png"
        }
        standardTile("heatingSetpointDown", "device.heatingSetpoint", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointDown", label:'', action:"heatingSetpointDown", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/heat_arrow_down.png"
        }
		standardTile("endManualControl", "device.thermostat", width: 2, height: 1, canChangeIcon: false, canChangeBackground: true, decoration: "flat") {
            state("default", label:'', action:"endManualControl", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/endManual.png")
		}
		main "thermostat"
		details (["thermostat","thermostatMode","outsidetemperature","heatingSetpoint","refresh","heatingSetpointUp","heatingSetpointDown","tadoMode","emergencyHeat","heat","Off","endManualControl"])
	}
}

def getWeather(){
	parent.weatherStatusCommand(this)
}

def setCapabilitySupportsWater(value){
  state.supportsWater = value
  log.debug("state.supportsWater = ${state.supportsWater}")
}

def getCapabilitySupportsWater() {
  def map = null
  map = [name: "capabilitySupportsWater", value: state.supportsWater]
  return map
}

def setCapabilitySupportsWaterTempControl(value){
  state.supportsWaterTempControl = value
  log.debug("state.supportsWaterTempControl = ${state.supportsWaterTempControl}")
}

def getCapabilitySupportsWaterTempControl() {
  def map = null
  map = [name: "capabilitySupportsWaterTempControl", value: state.supportsWaterTempControl]
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

def auto() {
	log.debug "Executing 'auto'"
	parent.autoCommand(this)
  parent.statusCommand(this)
}

def on() {
	log.debug "Executing 'on'"
	onCommand()
    statusCommand()
}

def off() {
	log.debug "Executing 'off'"
	offCommand()
    statusCommand()
}

def setHeatingSetpoint(targetTemperature) {
	log.debug "Executing 'setHeatingSetpoint'"
    log.debug "Target Temperature ${targetTemperature}"
    setHeatingTempCommand(targetTemperature)
	statusCommand()
}

def temperatureUp(){
	if (device.currentValue("thermostatMode") == "heat") {
    	heatingSetpointUp()
    } else {
    	log.debug ("temperature setpoint not supported in the current thermostat mode")
    }
}

def temperatureDown(){
	if (device.currentValue("thermostatMode") == "heat") {
    	heatingSetpointDown()
    } else {
    	log.debug ("temperature setpoint not supported in the current thermostat mode")
    }
}

def heatingSetpointUp(){
	log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
	if(state.supportsWaterTempControl == "true"){
		if ((device.currentValue("thermostatSetpoint").toInteger() - 1 ) < state.MinHeatTemp){
			log.debug("cannot decrease heat setpoint, its already at the minimum level of " + state.MinHeatTemp)
		} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() + 1
			log.debug "Setting heatingSetpoint up to: ${newSetpoint}"
			setHeatingSetpoint(newSetpoint)
			statusCommand()
		}
	} else {
		log.debug "Hot Water Temperature Capability Not Supported"
	}
}

def heatingSetpointDown(){
	log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
	if(state.supportsWaterTempControl == "true"){
		if ((device.currentValue("thermostatSetpoint").toInteger() + 1 ) > state.MaxHeatTemp){
			log.debug("cannot increase heat setpoint, its already at the maximum level of " + state.MaxHeatTemp)
		} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() - 1
			log.debug "Setting heatingSetpoint down to: ${newSetpoint}"
			setHeatingSetpoint(newSetpoint)
			statusCommand()
		}
	} else {
		log.debug "Hot Water Temperature Capability Not Supported"
	}
}

// Commands to device


def setThermostatMode(requiredMode){
	switch (requiredMode) {
    	case "heat":
        	heat()
        break
        case "auto":
        	auto()
        break
		case "off":
        	off()
        break
		case "emergency heat":
        	emergencyHeat()
        break
     }
}

def heat(){
  parent.heatCommand(this)
  parent.statusCommand(this)
}

def emergencyHeat(){
  parent.emergencyHeat(this)
}

def endManualControl(){
	parent.endManualControl(this)
}
