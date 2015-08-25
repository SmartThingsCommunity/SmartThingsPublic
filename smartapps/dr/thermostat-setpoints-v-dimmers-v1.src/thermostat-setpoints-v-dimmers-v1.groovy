/**
 *  Thermostat Setpoints  using Virtual Dimmers
 *
 *  Copyright 2015 Darc Ranger
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
 *	Adapted/Inspired from App created by @mattjfrank
 *
 *	Date: 2015-08-22 Version 1.0
 */
definition(
    name: "Thermostat Setpoints: V-Dimmers v1",
    namespace: "DR",
    author: "DarcRanger",
    description: "This app uses two virtual dimmer switches to control Setpoint temperatures on a Thermostat.  This was develop to use with the Amazon Echo.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
    
    )
    
    
preferences {
section("Select Heating SetPoint Dimmer") { 
	input "Heating", "capability.switchLevel", 
		multiple: false, 
		title: "Heating Dimmer Switch...", 
		required: true
}

section("Select Cooling SetPoint Dimmer") { 
	input "Cooling", "capability.switchLevel", 
		multiple: false, 
		title: "Cooling Dimmer Switch...", 
		required: true
}

section("This thermostat will be updated") {
	input "thermostat", "capability.thermostat", 
		multiple: false, 
		title: "Thermostat", 
		required: true
}

section("Setpoint Default Limits: Minimum/Maximum [50/80]") {
	input "setMinimum", "number", 
		multiple: false, 
		title: "Minimum Temperature", 
		required: true,
        defaultValue: "50"

	input "setMaximum", "number", 
		multiple: false, 
		title: "Maximum Temperature", 
		required: true,
        defaultValue: "80"
}

/*section("App for Cooling or Heating") {
	input "TempMode", "enum", 
		multiple: false, 
		title: "Thermostat Setpoint", 
		required: true,
        options: ["Heating", "Cooling"]
}*/

    section("Notify me...") {
     input "pushNotification_Setpoint", "bool", title: "Thermostat Mode change with Push Notification", required: false, defaultValue: "false"
}
}

def installed()
{

    subscribe(Heating, "switch.setLevel", switchSetLevelHandlerH)
    subscribe(Heating, "switch", switchSetLevelHandlerH)
    subscribe(Cooling, "switch.setLevel", switchSetLevelHandlerC)
    subscribe(Cooling, "switch", switchSetLevelHandlerC)
        subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler)
        subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler)
	log.debug "Installed with settings: ${settings}"
}

def updated()
{
unsubscribe()
    subscribe(Heating, "switch.setLevel", switchSetLevelHandlerH)
    subscribe(Heating, "switch", switchSetLevelHandlerH)
    subscribe(Cooling, "switch.setLevel", switchSetLevelHandlerC)
    subscribe(Cooling, "switch", switchSetLevelHandlerC)
        subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler)
        subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler)
	log.info "subscribed to all of switches events"
	log.debug "Updated with settings: ${settings}"
}

def TempMode = null

//------------------Level-Heat---------------------
def switchSetLevelHandlerH(evt){
def TempMode = "Heating"
state.TempMode = TempMode
if ((evt.value == "on") || (evt.value == "off" ))
	return
def level = evt.value.toFloat()
//log.debug "level: $level"
level = level.toInteger()
state.level = level

if(state.level<setMinimum){
state.level=setMinimum
log.debug "Thermostat SetLevel below limit: $level.  Reset to minimum Temperature: $setMinimum"
}else if(state.level>setMaximum){
state.level=setMaximum
log.debug "Thermostat SetLevel below limit: $level.  Reset to maximum Temperature: $setMaximum"
}

log.debug "switchSetLevelHandler Event: ${state.level} | TempMode: $TempMode"
thermostat.setHeatingSetpoint(state.level)
//Heating.setLevel(state.level)
Heating.off(delay: 5000)
//def dimmerValue = Heating.latestValue("level") //can be turned on by setting the level
//log.debug "dimmerValues: ($TempMode) $dimmerValue"
}

//------------------Level-Cool---------------------
def switchSetLevelHandlerC(evt){
def TempMode = "Cooling"
state.TempMode = TempMode
if ((evt.value == "on") || (evt.value == "off" ))
	return
def level = evt.value.toFloat()
//log.debug "level: $level"
level = level.toInteger()
state.level = level

if(state.level<setMinimum){
state.level=setMinimum
log.debug "Thermostat SetLevel below limit: $level.  Reset to minimum Temperature: $setMinimum"
}else if(state.level>setMaximum){
state.level=setMaximum
log.debug "Thermostat SetLevel below limit: $level.  Reset to maximum Temperature: $setMaximum"
}

log.debug "switchSetLevelHandler Event: ${state.level} | TempMode: $TempMode"
thermostat.setCoolingSetpoint(state.level)
//Cooling.setLevel(state.level)
Cooling.off(delay: 5000)
//def dimmerValue = Cooling.latestValue("level") //can be turned on by setting the level def dimmerValue = masters.latestValue("level")
//log.debug "dimmerValues: ($TempMode) $dimmerValue"
}

//------------------SetPoint-Cooling---------------
	def coolingSetpoint(evt) {
	def TempMode = "Cooling"
    state.TempMode = TempMode
		log.debug ""
		//log.debug "coolingSetpoint: $evt, $settings"
	log.debug "coolingSetpoint Event Value: ${evt.value}" //  which event fired is here log.info
	log.debug "coolingSetpoint Event Name: ${evt.name}"   //  name of device firing it here log.info

//if (TempMode == "Cooling"){
  	def ThermoPoint = thermostat.currentValue("coolingSetpoint")
    state.ThermoPoint = ThermoPoint
     // log.debug "ThermoPoint: $ThermoPoint " + TempMode
    def ThermoCoolPoint = evt.value.toFloat()
	ThermoCoolPoint = ThermoCoolPoint.toInteger()
	log.debug("current coolingsetpoint is $ThermoCoolPoint")
		Cooling.setLevel(ThermoCoolPoint)
        Cooling.off(delay: 5000)
		Notification()
   //}
   }
//------------------SetPoint-Heating---------------   
   def heatingSetpointHandler(evt) {
   def TempMode = "Heating"
   state.TempMode = TempMode
        log.debug ""
       // log.debug "heatingSetpoint: $evt, $settings"
    log.debug "heatingSetpoint Event Value: ${evt.value}" //  which event fired is here
	log.debug "heatingSetpoint Event Name: ${evt.name}"   //  name of device firing it here

//if (TempMode == "Heating"){
  	def ThermoPoint = thermostat.currentValue("heatingSetpoint")
    state.ThermoPoint = ThermoPoint
      //log.debug "ThermoPoint: $ThermoPoint " + TempMode
    def ThermoHeatPoint = evt.value.toFloat()
	ThermoHeatPoint = ThermoHeatPoint.toInteger()
	log.debug("current heatingsetpoint is $ThermoHeatPoint") 
		Heating.setLevel(ThermoHeatPoint)
        Heating.off(delay: 5000)
		Notification()
   //}
}

def Notification(){
// log.debug "Test ThermoMode-Status: " + thermostat.currentValue("thermostatMode")
          if (pushNotification_Setpoint) {
            log.debug "Notify --> Thermostat SetPoint: $state.ThermoPoint ($state.TempMode)"
            sendPush("Thermostat SetPoint: $state.ThermoPoint " + state.TempMode)
            log.debug "NOTIFY---------------------------------------------"
        	}
}