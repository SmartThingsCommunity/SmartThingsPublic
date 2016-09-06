/**
 *  Based on original version Copyright 2015 SmartThings
 *  Additions Copyright 2016 Sean Kendall Schneyer
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
 *	Ecobee Thermostat
 *
 *	Author: SmartThings
 *	Date: 2013-06-13
 *
 * 	Updates by Sean Kendall Schneyer <smartthings@linuxbox.org>
 * 	Date: 2015-12-23
 *
 *
 *  See Changelog for change history 
 *
 * 	0.9.12 - Fix for setting custom Thermostat Programs (Comfort Settings)
 *	0.9.13 - Add attributes to indicate custom program names to child thermostats (smart1, smart2, etc)
 *
 */

def getVersionNum() { return "0.9.13" }
private def getVersionLabel() { return "Ecobee Thermostat Version ${getVersionNum()}" }

 
metadata {
	definition (name: "Ecobee Thermostat", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Thermostat"
        capability "Sensor"
		capability "Refresh"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		// capability "Presence Sensor"
        capability "Motion Sensor"
        
        // Extended Set of Thermostat Capabilities
        capability "Thermostat Cooling Setpoint"
		capability "Thermostat Fan Mode"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Thermostat Setpoint"
            

		command "setTemperature"
        command "auxHeatOnly"

		command "generateEvent"
		command "raiseSetpoint"
		command "lowerSetpoint"
		command "resumeProgram"
		command "switchMode"
        
        command "setThermostatProgram"
        command "home"
        command "sleep"
        command "away"
        
        command "fanOff"  // Missing from the Thermostat standard capability set
        command "noOp" // Workaround for formatting issues 
        command "setStateVariable"
        
        

		// Capability "Thermostat"
        attribute "temperatureScale", "string"
		attribute "thermostatSetpoint","number"
		attribute "thermostatStatus","string"
        attribute "apiConnected","string"
        
		attribute "currentProgram","string"
        attribute "currentProgramId","string"		
        attribute "weatherSymbol", "string"        
        attribute "debugEventFromParent","string"
        attribute "logo", "string"
        attribute "timeOfDate", "enum", ["day", "night"]
        attribute "lastPoll", "string"
        
        attribute "smart1", "string"
        attribute "smart2", "string"
        attribute "smart3", "string"
        attribute "smart4", "string"
        attribute "smart5", "string"
        attribute "smart6", "string"
        attribute "smart7", "string"
        attribute "smart8", "string"
        attribute "smart9", "string"
        attribute "smart10", "string"
	}

	simulator { }

    	tiles(scale: 2) {      
              
		multiAttributeTile(name:"tempSummary", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}

			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
                attributeState("default", action: "setTemperature")
			}
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue}%', unit:"%")
			}

			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#44b621")
				attributeState("heating", backgroundColor:"#ffa81e")
				attributeState("cooling", backgroundColor:"#269bd2")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
				attributeState("cool", label:'${name}')
                attributeState("auto", label:'${name}')
			}
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
            	attributeState("default", label:'${currentValue}', unit:"dF")
            }
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}

        } // End multiAttributeTile
        

        // Workaround until they fix the Thermostat multiAttributeTile. Only use this one OR the above one, not both
        multiAttributeTile(name:"summary", type: "lighting", width: 6, height: 4) {
        	tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°', unit:"dF",
				backgroundColors: getTempColors())
			}

			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
                attributeState("default", action: "setTemperature")
			}

            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue}%', unit:"%")
			}

			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#44b621")
				attributeState("heating", backgroundColor:"#ffa81e")
				attributeState("cooling", backgroundColor:"#269bd2")
			}

			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
				attributeState("cool", label:'${name}')
                attributeState("auto", label:'${name}')
			}
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
            	attributeState("default", label:'${currentValue}', unit:"dF")
            }
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}

        }

        // Show status of the API Connection for the Thermostat
		standardTile("apiStatus", "device.apiConnected", width: 2, height: 2) {
        	state "full", label: "API", backgroundColor: "#44b621", icon: "st.contact.contact.closed"
            state "warn", label: "API ", backgroundColor: "#FFFF33", icon: "st.contact.contact.open"
            state "lost", label: "API ", backgroundColor: "#ffa81e", icon: "st.contact.contact.open"
		}

		valueTile("temperature", "device.temperature", width: 2, height: 2, canChangeIcon: true, icon: "st.Home.home1") {
			state("temperature", label:'${currentValue}°', unit:"F",
				backgroundColors: getTempColors()
			)
		}
        
		standardTile("mode", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "off", action:"thermostat.heat", label: "Set Mode", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_off.png"
			state "heat", action:"thermostat.cool",  label: "Set Mode", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_heat.png"
			state "cool", action:"thermostat.auto",  label: "Set Mode", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_cool.png"
			state "auto", action:"thermostat.off",  label: "Set Mode", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_auto.png"
            // Not included in the button loop, but if already in "auxHeatOnly" pressing button will go to "auto"
			state "auxHeatOnly", action:"thermostat.auto", icon: "st.thermostat.emergency-heat"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
        
        standardTile("modeShow", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "off", action:"noOp", label: "Off", nextState: "off", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_off.png"
			state "heat", action:"noOp",  label: "Heat", nextState: "heat", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_heat.png"
			state "cool", action:"noOp",  label: "Cool", nextState: "cool", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_cool.png"
			state "auto", action:"noOp",  label: "Auto", nextState: "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_auto.png"
            // Not included in the button loop, but if already in "auxHeatOnly" pressing button will go to "auto"
			state "auxHeatOnly", action:"noOp", icon: "st.thermostat.emergency-heat"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
        
        // TODO Use a different color for the one that is active
		standardTile("setModeHeat", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {			
			state "heat", action:"thermostat.heat",  label: "Heat", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_heat.png"
			state "updating", label:"Working...", icon: "st.secondary.secondary"
		}
		standardTile("setModeCool", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {			
			state "cool", action:"thermostat.cool",  label: "Cool", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_cool.png"
			state "updating", label:"Working...", icon: "st.secondary.secondary"
		}        
		standardTile("setModeAuto", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {			
			state "auto", action:"thermostat.auto",  label: "Auto", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_auto.png"
			state "updating", label:"Working...", icon: "st.secondary.secondary"
		}
		standardTile("setModeOff", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {			
			state "off", action:"thermostat.off", label: "Off", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_off.png"
			state "updating", label:"Working...", icon: "st.secondary.secondary"
		}
        

		standardTile("fanModeLabeled", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "on", label:'On', action:"noOp", nextState: "on", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan.png"
            state "auto", label:'Auto', action:"noOp", nextState: "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan.png"
            state "off", label:'Off', action:"noOp", nextState: "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan.png"
			state "circulate", label:'Circulate', action:"noOp", nextState: "circulate", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan.png"
            state "updating", label:"Working", icon: "st.secondary.secondary"
		}
        
        standardTile("fanOffButton", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "off", label:"Fan Off", action:"fanOff", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
            state "updating", label:"Working", icon: "st.secondary.secondary"
		}

		standardTile("fanCirculate", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "circulate", label:"Fan Cicrulate", action:"thermostat.fanCirculate", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
            state "updating", label:"Working", icon: "st.secondary.secondary"
		}
        
		standardTile("fanMode", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "on", action:"thermostat.fanAuto", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
            state "auto", action:"thermostat.fanOn", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
            state "off", action:"thermostat.fanAuto", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
			state "circulate", action:"thermostat.fanAuto", nextState: "updating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_fan_big_nolabel.png"
            state "updating", label:"Working", icon: "st.secondary.secondary"
		}
        standardTile("fanModeAutoSlider", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "on", action:"thermostat.fanAuto", nextState: "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/fanmode_auto_slider_off.png"
            state "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/fanmode_auto_slider_on.png"
        }
		standardTile("fanModeOnSlider", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "auto", action:"thermostat.fanOn", nextState: "auto", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/fanmode_on_slider_off.png"
            state "on", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/fanmode_on_slider_on.png"
        }

        
		standardTile("upButtonControl", "device.thermostatSetpoint", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"raiseSetpoint", icon:"st.thermostat.thermostat-up"
		}
		valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 2, height: 2, decoration: "flat") {
			state "thermostatSetpoint", label:'${currentValue}°',
				backgroundColors: getTempColors()
		}
		valueTile("currentStatus", "device.thermostatStatus", height: 2, width: 4, decoration: "flat") {
			state "thermostatStatus", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		standardTile("downButtonControl", "device.thermostatSetpoint", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"lowerSetpoint", icon:"st.thermostat.thermostat-down"
		}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 4, inactiveLabel: false, range:"(15..85)") {
			state "setHeatingSetpoint", action:"thermostat.setHeatingSetpoint", backgroundColor:"#d04e00", unit: 'C'
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}°\nHeat', unit:"dF", backgroundColor:"#d04e00"
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 4, inactiveLabel: false, range:"(15..85)") {
			state "setCoolingSetpoint", action:"thermostat.setCoolingSetpoint", backgroundColor: "#1e9cbb", unit: 'C'
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue}°\nCool', unit:"dF", backgroundColor: "#1e9cbb"
		}
		standardTile("refresh", "device.thermostatMode", width: 2, height: 2,inactiveLabel: false, decoration: "flat") {
            state "default", action:"refresh.refresh", label: "Refresh", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/header_ecobeeicon_blk.png"
		}
        
        
        standardTile("resumeProgram", "device.resumeProgram", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "resume", action:"resumeProgram", nextState: "updating", label:'Resume', icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/action_resume_program.png"
			state "updating", label:"Working", icon: "st.samsung.da.oven_ic_send"
		}
        
        // TODO: Add icons and handling for Ecobee Comfort Settings
        standardTile("currentProgramIcon", "device.currentProgramName", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "Home", action:"noOp", label: 'Home', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_home_blue.png"
			state "Away", action:"noOp", label: 'Away', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_away_blue.png"
            state "Sleep", action:"noOp", label: 'Sleep', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_asleep_blue.png"
            state "Auto Away", action:"noOp", label: 'Auto Away', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_away_blue.png" // Fix to auto version
            state "Auto Home", action:"noOp", label: 'Auto Home', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_home_blue.png" // Fix to auto
            state "Hold", action:"noOp", label: "Hold Activated", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_generic_chair_blue.png"
            state "Hold: Home", action:"noOp", label: 'Hold: Home', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_home_blue.png"
            state "Hold: Away", action:"noOp", label: 'Hold: Away',  icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_away_blue.png"
            state "Hold: Sleep", action:"noOp", label: 'Hold: Sleep',  icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_asleep_blue.png"
            state "default:", action:"noOp", label: 'Other: ${currentValue}', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_asleep_blue.png"
            
		}        
        
        valueTile("currentProgram", "device.currentProgramName", height: 2, width: 4, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Comfort Setting:\n${currentValue}' 
		}
        
		standardTile("setHome", "device.setHome", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "home", action:"home", nextState: "updating", label:'Home', icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_home_blue.png"
			state "updating", label:"Working...", icon: "st.samsung.da.oven_ic_send"
		}
        
        standardTile("setAway", "device.setAway", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "away", action:"away", nextState: "updating", label:'Away', icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_away_blue.png"
			state "updating", label:"Working...", icon: "st.samsung.da.oven_ic_send"
		}

        standardTile("setSleep", "device.setSleep", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			// state "sleep", action:"sleep", nextState: "updating", label:'Set Sleep', icon:"st.Bedroom.bedroom2"
            state "sleep", action:"sleep", nextState: "updating", label:'Sleep', icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/schedule_asleep_blue.png"
			state "updating", label:"Working...", icon: "st.samsung.da.oven_ic_send"
		}

        standardTile("operatingState", "device.thermostatOperatingState", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			//state "idle", label: "Idle", backgroundColor:"#44b621", icon: "st.nest.empty"
            state "idle", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_idle.png"
            state "fan only", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_fan.png"
			state "heating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_heat.png"
			state "cooling", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_cool.png"
            // Issue reported that the label overlaps. Need to remove the icon
            state "default", label: '${currentValue}', icon: "st.nest.empty"
		}

        valueTile("humidity", "device.humidity", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: 'Humidity\n${currentValue}%', unit: "humidity" // Add a blue background signifying water?
		}
        
        standardTile("motionState", "device.motion", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "active", action:"noOp", nextState: "active", label:"Motion", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/motion_sensor_motion.png"
			state "inactive", action: "noOp", nextState: "inactive", label:"No Motion", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/motion_sensor_nomotion.png"
            state "not supported", action: "noOp", nextState: "not supported", label: "N/A", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/notsupported_x.png"
		}

        // Weather Tiles and other Forecast related tiles
		standardTile("weatherIcon", "device.weatherSymbol", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "-2",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_updating_-2.png" // label: 'updating...',	
			state "0",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_sunny_00.png" // label: 'Sunny',			
			state "1",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_mostlysunny_01.png" // label: 'Few Clouds',	
			state "2",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_partly_cloudy_02.png"
			state "3",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_mostly_cloudy_03.png"
			state "4",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_cloudy_04.png"
			state "5",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_drizzle_05.png"
			state "6",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_rain_06.png"
			state "7",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_freezing_rain_07.png"
			state "8",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_rain_06.png"
			state "9",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_freezing_rain_07.png"
			state "10",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_snow_10.png"
			state "11",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_flurries_11.png"
			state "12",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_freezing_rain_07.png"
			state "13",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_snow_10.png"
			state "14",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_freezing_rain_07.png"
			state "15",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_thunderstorms_15.png"
			state "16",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_windy_16.png"
			state "17",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_tornado_17.png"
			state "18",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_fog_18.png"
			state "19",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_fog_18.png" // Hazy
			state "20",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_fog_18.png" // Smoke
			state "21",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_fog_18.png" // Dust
            
            // Night Time Icons (Day time Value + 100)
			state "100",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_clear_night_100.png" // label: 'Sunny',			
			state "101",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_partly_cloudy_101.png" // label: 'Few Clouds',	
			state "102",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_partly_cloudy_101.png"
			state "103",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_mostly_cloudy_103.png"
			state "104",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_cloudy_04.png"
			state "105",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_drizzle_105.png"
			state "106",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_rain_106.png"
			state "107",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_freezing_rain_107.png"
			state "108",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_rain_106.png"
			state "109",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_freezing_rain_107.png"
			state "110",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_snow_110.png"
			state "111",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_flurries_111.png"
			state "112",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_freezing_rain_107.png"
			state "113",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_snow_110.png"
			state "114",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_freezing_rain_107.png"
			state "115",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_night_thunderstorms_115.png"
			state "116",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_windy_16.png"
			state "117",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_tornado_17.png"
			state "118",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_fog_18.png"
			state "119",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_fog_18.png" // Hazy
			state "120",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_fog_18.png" // Smoke
			state "121",			icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/weather_fog_18.png" // Dust
		}
        standardTile("weatherTemperature", "device.weatherTemperature", width: 2, height: 2, decoration: "flat") {
			state "default", action: "noOp", nextState: "default", label: 'Out: ${currentValue}°', icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/thermometer.png"
		}
        
        valueTile("lastPoll", "device.lastPoll", height: 2, width: 4, decoration: "flat") {
			state "thermostatStatus", label:'Last Poll:\n${currentValue}', backgroundColor:"#ffffff"
		}
        
        standardTile("ecoLogo", "device.logo", inactiveLabel: false, width: 2, height: 2) {
			state "default",  icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/header_ecobeeicon_blk.png"			
		}

        standardTile("oneBuffer", "device.logo", inactiveLabel: false, width: 1, height: 1, decoration: "flat") {
        	state "default"
        }
        
        standardTile("commandDivider", "device.logo", inactiveLabel: false, width: 4, height: 1, decoration: "flat") {
        	state "default", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/command_divider.png"			
        }        
    
		main(["temperature", "tempSummary"])
		details([
        	// Use this if you are on a fully operational device OS (such as iOS or Android)
        	"tempSummary",
            // Use the lines below if you can't (or don't want to) use the multiAttributeTile version
            // To use, uncomment these lines below, and comment out the line above
            // "temperature", "humidity",  "upButtonControl", "thermostatSetpoint", 
            // "currentStatus", "downButtonControl",
            
        	"operatingState", "weatherIcon", "refresh", 
            "currentProgramIcon", "weatherTemperature", "motionState", 
            "modeShow", "fanModeLabeled", "resumeProgram",
            
            "oneBuffer", "commandDivider", "oneBuffer",
            "coolSliderControl", "coolingSetpoint",
            "heatSliderControl", "heatingSetpoint",            
            "fanMode", "fanModeAutoSlider", "fanModeOnSlider", 
            // "currentProgram", "apiStatus",
            "setHome", "setAway", "setSleep",
            "setModeHeat", "setModeCool", "setModeAuto",
            "apiStatus", "lastPoll"
            // "fanOffButton", "fanCirculate", "setVariable"
            ])            
	}

	preferences {
    	section () {
			input "holdType", "enum", title: "Hold Type", description: "When changing temperature, use Temporary or Permanent hold (default)", required: false, options:["Temporary", "Permanent"]
        	// TODO: Add a preference for the background color for "idle"
        	// TODO: Allow for a "smart" Setpoint change in "Auto" mode. Why won't the paragraph show up in the Edit Device screen?
        	paragraph "The Smart Auto Temp Adjust flag allows for the temperature to be adjusted manually even when the thermostat is in Auto mode. An attempt to determine if the heat or cool setting should be changed will be made automatically."
            input "smartAuto", "bool", title: "Smart Auto Temp Adjust", description: true, required: false
            // input "detailedTracing", "bool", title: "Enable Detailed Tracing", description: true, required: false
       }
	}

}

// parse events into attributes
def parse(String description) {
	LOG( "parse() --> Parsing '${description}'" )
	// Not needed for cloud connected devices

}

def refresh() {
	LOG("refresh() called", 4)
	poll()
}

void poll() {
	LOG("Executing 'poll' using parent SmartApp")
    parent.pollChildren(this)
}


def generateEvent(Map results) {
	LOG("generateEvent(): parsing data $results", 4)
    LOG("Debug level of parent: ${parent.settings?.debugLevel}", 4, null, "debug")
	def linkText = getLinkText(device)

	if(results) {
		results.each { name, value ->
			LOG("generateEvent() - In each loop: name: ${name}  value: ${value}", 4)
			def isChange = false
			def isDisplayed = true
			def event = [name: name, linkText: linkText, descriptionText: getThermostatDescriptionText(name, value, linkText), handlerName: name]

			if (name=="temperature" || name=="heatingSetpoint" || name=="coolingSetpoint" || name=="weatherTemperature" ) {
				def sendValue = value // ? convertTemperatureIfNeeded(value.toDouble(), "F", 1): value //API return temperature value in F
                LOG("generateEvent(): Temperature value: ${sendValue}", 5, this, "trace")
				isChange = isTemperatureStateChange(device, name, value.toString())
				isDisplayed = isChange
				event << [value: sendValue, isStateChange: isChange, displayed: isDisplayed]
			} else if (name=="heatMode" || name=="coolMode" || name=="autoMode" || name=="auxHeatMode") {
				isChange = isStateChange(device, name, value.toString())
				event << [value: value.toString(), isStateChange: isChange, displayed: false]
			} else if (name=="thermostatOperatingState") {
            	generateOperatingStateEvent(value.toString())
                return
            } else if (name=="apiConnected") {
            	// Treat as if always changed to ensure an updated value is shown on mobile device and in feed
                isChange = isStateChange(device,name,value.toString());
                isDisplayed = isChange
                event << [value: value.toString(), isStateChange: isChange, displayed: isDisplayed]
            } else if (name=="weatherSymbol" && device.currentValue("timeOfDay") == "night") {
            	// Check to see if it is night time, if so change to a night symbol
                def symbolNum = value.toInteger() + 100
                isChange = isStateChange(device, name, symbolNum.toString())
                isDisplayed = isChange
				event << [value: symbolNum.toString(), isStateChange: isChange, displayed: isDisplayed]            
            } else {
				isChange = isStateChange(device, name, value.toString())
				isDisplayed = isChange
				event << [value: value.toString(), isStateChange: isChange, displayed: isDisplayed]
			}
			LOG("Out of loop, calling sendevent(${event})", 5)
			sendEvent(event)
		}
		generateSetpointEvent()
		generateStatusEvent()
	}
}

//return descriptionText to be shown on mobile activity feed
private getThermostatDescriptionText(name, value, linkText) {
	if(name == "temperature") {
		return "$linkText temperature is ${value}°"

	} else if(name == "heatingSetpoint") {
		return "heating setpoint is ${value}°"

	} else if(name == "coolingSetpoint"){
		return "cooling setpoint is ${value}°"

	} else if (name == "thermostatMode") {
		return "thermostat mode is ${value}"

	} else if (name == "thermostatFanMode") {
		return "thermostat fan mode is ${value}"

	} else {
		return "${name} = ${value}"
	}
}

// Does not set in absolute values, sets in increments either up or down
def setTemperature(setpoint) {
	LOG("setTemperature() called with setpoint ${setpoint}. Current temperature: ${device.currentValue("temperature")}. Heat Setpoint: ${device.currentValue("heatingSetpoint")}. Cool Setpoint: ${device.currentValue("coolingSetpoint")}. Thermo Setpoint: ${device.currentValue("thermostatSetpoint")}", 4)

    def mode = device.currentValue("thermostatMode")
    def midpoint
	def targetvalue

	if (mode == "off" || (mode == "auto" && !usingSmartAuto() )) {
		LOG("setTemperature(): this mode: $mode does not allow raiseSetpoint", 2, null, "warn")
        return
    }

	def currentTemp = device.currentValue("temperature")
    def deltaTemp = 0

	if (setpoint == 0) { // down arrow pressed
    	deltaTemp = -1
    } else if (setpoint == 1) { // up arrow pressed
    	deltaTemp = 1
    } else {
    	deltaTemp = ( (setpoint - currentTemp) < 0) ? -1 : 1
    }
    

    LOG("deltaTemp = ${deltaTemp}")

    if (mode == "auto") {
    	// In Smart Auto Mode
		LOG("setTemperature(): In Smart Auto Mode", 4)

        if (deltaTemp < 0) {
        	// Decrement the temp for cooling
            LOG("Smart Auto: lowerSetpoint being called", 4)
            lowerSetpoint()
        } else if (deltaTemp > 0) {
        	// Increment the temp for heating
            LOG("Smart Auto: raiseSetpoint being called", 4)
            raiseSetpoint()
        } // Otherwise they are equal and the setpoint does not change

    } else if (mode == "heat") {
    	// Change the heat
        LOG("setTemperature(): change the heat temp", 4)
        // setHeatingSetpoint(setpoint)
        if (deltaTemp < 0) {
        	// Decrement the temp for cooling
            LOG("Heat: lowerSetpoint being called", 4)
            lowerSetpoint()
        } else if (deltaTemp > 0) {
        	// Increment the temp for heating
            LOG("Heat: raiseSetpoint being called", 4)
            raiseSetpoint()
        } // Otherwise they are equal and the setpoint does not change

    } else if (mode == "cool") {
    	// Change the cool
        LOG("setTemperature(): change the cool temp", 4)
        // setCoolingSetpoint(setpoint)
        if (deltaTemp < 0) {
        	// Decrement the temp for cooling
            LOG("Cool: lowerSetpoint being called", 4)
            lowerSetpoint()
        } else if (deltaTemp > 0) {
        	// Increment the temp for heating
            LOG("Cool: raiseSetpoint being called", 4)
            raiseSetpoint()
        } // Otherwise they are equal and the setpoint does not change

    }
}

void setHeatingSetpoint(setpoint) {
	LOG("setHeatingSetpoint() request with setpoint value = ${setpoint} before toDouble()", 4)
	setHeatingSetpoint(setpoint.toDouble())
}

void setHeatingSetpoint(Double setpoint) {
//    def mode = device.currentValue("thermostatMode")
	LOG("setHeatingSetpoint() request with setpoint value = ${setpoint}", 4)

	def heatingSetpoint = setpoint
	def coolingSetpoint = device.currentValue("coolingSetpoint").toDouble()
	def deviceId = getDeviceId()


	LOG("setHeatingSetpoint() before compare: heatingSetpoint == ${heatingSetpoint}   coolingSetpoint == ${coolingSetpoint}", 4)
	//enforce limits of heatingSetpoint vs coolingSetpoint
	if (heatingSetpoint > coolingSetpoint) {
		coolingSetpoint = heatingSetpoint
	}

	LOG("Sending setHeatingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}", 4)


	def sendHoldType = whatHoldType()

	if (parent.setHold(this, heatingSetpoint,  coolingSetpoint, deviceId, sendHoldType)) {
		sendEvent("name":"heatingSetpoint", "value": wantMetric() ? heatingSetpoint : heatingSetpoint.round(0).toInteger() )
		sendEvent("name":"coolingSetpoint", "value": wantMetric() ? coolingSetpoint : coolingSetpoint.round(0).toInteger() )
		LOG("Done setHeatingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}", 4)
		generateSetpointEvent()
		generateStatusEvent()
	} else {
		LOG("Error setHeatingSetpoint(${setpoint})", 2, null, "error") //This error is handled by the connect app
        
	}
}

void setCoolingSetpoint(setpoint) {
	LOG("setCoolingSetpoint() request with setpoint value = ${setpoint} (before toDouble)", 4)

	setCoolingSetpoint(setpoint.toDouble())
}

void setCoolingSetpoint(Double setpoint) {
	LOG("setCoolingSetpoint() request with setpoint value = ${setpoint}", 4)
//    def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint").toDouble()
	def coolingSetpoint = setpoint
	def deviceId = getDeviceId()


	LOG("setCoolingSetpoint() before compare: heatingSetpoint == ${heatingSetpoint}   coolingSetpoint == ${coolingSetpoint}")

	//enforce limits of heatingSetpoint vs coolingSetpoint
	if (heatingSetpoint > coolingSetpoint) {
		heatingSetpoint = coolingSetpoint
	}

	LOG("Sending setCoolingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}")
	def sendHoldType = whatHoldType()
    LOG("sendHoldType == ${sendHoldType}", 5)

    // Convert temp to F from C if needed
	if (parent.setHold(this, heatingSetpoint,  coolingSetpoint, deviceId, sendHoldType)) {
		sendEvent("name":"heatingSetpoint", "value": wantMetric() ? heatingSetpoint : heatingSetpoint.round(0).toInteger() )
		sendEvent("name":"coolingSetpoint", "value": wantMetric() ? coolingSetpoint : coolingSetpoint.round(0).toInteger() )
		LOG("Done setCoolingSetpoint>> coolingSetpoint = ${coolingSetpoint}, heatingSetpoint = ${heatingSetpoint}", 4)
		generateSetpointEvent()
		generateStatusEvent()
	} else {
		LOG("Error setCoolingSetpoint(setpoint)", 2, null, "error") //This error is handled by the connect app
	}
}

void resumeProgram() {
	// TODO: Put a check in place to see if we are already running the program. If there is nothing to resume, then save the calls upstream
	LOG("resumeProgram() is called", 5)
	sendEvent("name":"thermostatStatus", "value":"Resuming schedule...", "description":statusText, displayed: false)
	def deviceId = getDeviceId()
	if (parent.resumeProgram(this, deviceId)) {
		sendEvent("name":"thermostatStatus", "value":"Setpoint updating...", "description":statusText, displayed: false)
		runIn(15, "poll")
		LOG("resumeProgram() is done", 5)
		sendEvent("name":"resumeProgram", "value":"resume", descriptionText: "resumeProgram is done", displayed: false, isStateChange: true)
	} else {
		sendEvent("name":"thermostatStatus", "value":"failed resume click refresh", "description":statusText, displayed: false)
		LOG("Error resumeProgram() check parent.resumeProgram(this, deviceId)", 2, null, "error")
	}

	generateSetpointEvent()
	generateStatusEvent()    
}

/*
def modes() {
	if (state.modes) {
		LOG("Modes = ${state.modes}", 5)
		return state.modes
	}
	else {
		state.modes = parent.availableModes(this)
		LOG("Modes = ${state.modes}", 5)
		return state.modes
	}
}

def fanModes() {
	["off", "on", "auto", "circulate"]
}
*/



def generateQuickEvent(name, value) {
	generateQuickEvent(name, value, 0)
}

def generateQuickEvent(name, value, pollIn) {
	sendEvent(name: name, value: value, displayed: true)
    if (pollIn > 0) { runIn(pollIn, "poll") }
}

def generateFanModeEvent(fanMode) {
	sendEvent(name: "thermostatFanMode", value: fanMode, descriptionText: "$device.displayName fan is in ${mode} mode", displayed: true)
}

def generateOperatingStateEvent(operatingState) {
	LOG("generateOperatingStateEvent with state: ${operatingState}", 4)
	sendEvent(name: "thermostatOperatingState", value: operatingState, descriptionText: "$device.displayName is ${operatingState}", displayed: true)
}


def setThermostatMode(String value) {
	// 	"emergencyHeat" "heat" "cool" "off" "auto"
    
    if (value=="emergency" || value=="emergencyHeat") { value = "auxHeatOnly" }    
	LOG("setThermostatMode(${value})", 5)
	generateQuickEvent("thermostatMode", value)


    def deviceId = getDeviceId()
	if (parent.setMode(this, value, deviceId)) {
		// generateQuickEvent("thermostatMode", value, 15)
	} else {
		LOG("Error setting new mode to ${value}.", 1, null, "error")
		def currentMode = device.currentState("thermostatMode")?.value
		generateQuickEvent("thermostatMode", currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}


def off() {
	LOG("off()", 5)
    setThermostatMode("off")    
}

def heat() {
	LOG("heat()", 5)
    setThermostatMode("heat")    
}

def auxHeatOnly() {
	LOG("auxHeatOnly()", 5)
    setThermostatMode("auxHeatOnly")
}

def emergency() {
	auxHeatOnly()
}

// This is the proper definition for the capability
def emergencyHeat() {
	auxHeatOnly()
}

def cool() {
	LOG("cool()", 5)
    setThermostatMode("cool")    
}

def auto() {
	LOG("auto()", 5)
    setThermostatMode("auto")    
}


// Handle Comfort Settings
def setThermostatProgram(program, holdType=null) {
	// Change the Comfort Setting to Home
    LOG("setThermostatProgram: program: ${program}  holdType: ${holdType}", 4)
	def deviceId = getDeviceId()    

	LOG("Before calling parent.setProgram()", 5)
	
    def sendHoldType = holdType ?: whatHoldType()
    
  
    if ( parent.setProgram(this, program, deviceId, sendHoldType) ) {
		generateProgramEvent(program)
        runIn(15, "poll")        
	} else {
    	LOG("Error setting new comfort setting ${program}.", 2, null, "warn")
		def currentProgram = device.currentState("currentProgramId")?.value
		generateProgramEvent(currentProgram, program) // reset the tile back
	}
 
 	LOG("After calling parent.setProgram()", 5)
    
	generateSetpointEvent()
	generateStatusEvent()    
}



def home() {
	// Change the Comfort Setting to Home
    LOG("home()", 5)
    setThermostatProgram("Home")
}

def away() {
	// Change the Comfort Setting to Away
    LOG("away()", 5)
    setThermostatProgram("Away")
}

def sleep() {
	// Change the Comfort Setting to Sleep    
    LOG("sleep()", 5)
    setThermostatProgram("Sleep")
}

def generateProgramEvent(program, failedProgram=null) {
	LOG("Generate generateProgramEvent Event: program ${program}", 4)

	sendEvent("name":"thermostatStatus", "value":"Setpoint updating...", "description":statusText, displayed: false)
	sendEvent("name":"currentProgramName", "value":program.capitalize())
    sendEvent("name":"currentProgramId", "value":program)
    
    def tileName = ""
    
    if (!failedProgram) {
    	tileName = "set" + program.capitalize()    	
    } else {
    	tileName = "set" + failedProgram.capitalize()    	
    }
    sendEvent("name":"${tileName}", "value":"${program}", descriptionText: "${tileName} is done", displayed: false, isStateChange: true)
}


def setThermostatFanMode(value, holdType=null) {
	LOG("setThermostatFanMode(${value})", 4)
	// "auto" "on" "circulate" "off"       
    
    // This is to work around a bug in some SmartApps that are using fanOn and fanAuto as inputs here, which is wrong
    if (value == "fanOn" || value == "on" ) { value = "on" }
    else if (value == "fanAuto" || value == "auto" ) { value = "auto" }
    else if (value == "fanCirculate" || value == "circulate")  { value == "circulate" }
    else if (value == "fanOff" || value == "off") { value = "off" }
	else {
    	LOG("setThermostatFanMode() - Unrecognized Fan Mode: ${value}. Setting to 'auto'", 1, null, "error")
        value = "auto"
    }
    
    // Change the state now to quickly refresh the UI
    generateQuickEvent("thermostatFanMode", value, 0)
    
    def results = parent.setFanMode(this, value, getDeviceId())
    
	if ( results ) {
    	LOG("parent.setFanMode() returned successfully!", 5)
    } else {
    	generateQuickEvent("thermostatFanMode", device.currentValue("thermostatFanMode"))
    }
    
	generateSetpointEvent()
	generateStatusEvent()    
}

def fanOn() {
	LOG("fanOn()", 5)
    setThermostatFanMode("on")

}

def fanAuto() {
	LOG("fanAuto()", 5)
	setThermostatFanMode("auto")
}

def fanCirculate() {
	LOG("fanCirculate()", 5)
    setThermostatFanMode("circulate")
}

def fanOff() {
	LOG("fanOff()", 5)
	setThermostatFanMode("off")
}

def generateSetpointEvent() {
	LOG("Generate SetPoint Event", 5, null, "trace")

	def mode = device.currentValue("thermostatMode")    
    def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
    
	LOG("Current Mode = ${mode}", 4, null, "debug")
	LOG("Heating Setpoint = ${heatingSetpoint}", 4, null, "debug")
	LOG("Cooling Setpoint = ${coolingSetpoint}", 4, null, "debug")

	if (mode == "heat") {
		sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint.toString())
	}
	else if (mode == "cool") {
		sendEvent("name":"thermostatSetpoint", "value":coolingSetpoint.toString())
	} else if (mode == "auto" && !usingSmartAuto() ) {
		// No Smart Auto, just regular auto
		sendEvent("name":"thermostatSetpoint", "value":"Auto")
	} else if (mode == "auto" && usingSmartAuto() ) {
    	// Smart Auto Enabled
        sendEvent("name":"thermostatSetpoint", "value":device.currentValue("temperature").toString())
    } else if (mode == "off") {
		sendEvent("name":"thermostatSetpoint", "value":"Off")
	} else if (mode == "emergencyHeat") {
		sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint.toString())
	}

}

void raiseSetpoint() {
	def mode = device.currentValue("thermostatMode")
	def targetvalue

	if (mode == "off" || (mode == "auto" && !usingSmartAuto() )) {
		LOG("raiseSetpoint(): this mode: $mode does not allow raiseSetpoint")
        return
	}

   	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def thermostatSetpoint = device.currentValue("thermostatSetpoint").toDouble()
	LOG("raiseSetpoint() mode = ${mode}, heatingSetpoint: ${heatingSetpoint}, coolingSetpoint:${coolingSetpoint}, thermostatSetpoint:${thermostatSetpoint}", 4)

   	if (thermostatSetpoint) {
		targetvalue = thermostatSetpoint
	} else {
		targetvalue = 0.0
	}

       if (getTemperatureScale() == "C" ) {
       	targetvalue = targetvalue.toDouble() + 0.5
       } else {
		targetvalue = targetvalue.toDouble() + 1.0
       }

	sendEvent("name":"thermostatSetpoint", "value":( wantMetric() ? targetvalue : targetvalue.round(0).toInteger() ), displayed: true)
	LOG("In mode $mode raiseSetpoint() to $targetvalue", 4)

	def runWhen = parent.settings?.arrowPause ?: 4		
	runIn(runWhen, "alterSetpoint", [data: [value:targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
}

//called by tile when user hit raise temperature button on UI
void lowerSetpoint() {
	def mode = device.currentValue("thermostatMode")
	def targetvalue

	if (mode == "off" || (mode == "auto" && !usingSmartAuto() )) {
		LOG("lowerSetpoint(): this mode: $mode does not allow lowerSetpoint", 2, null, "warn")
    } else {
    	def heatingSetpoint = device.currentValue("heatingSetpoint")
		def coolingSetpoint = device.currentValue("coolingSetpoint")
		def thermostatSetpoint = device.currentValue("thermostatSetpoint").toDouble()
		LOG("lowerSetpoint() mode = ${mode}, heatingSetpoint: ${heatingSetpoint}, coolingSetpoint:${coolingSetpoint}, thermostatSetpoint:${thermostatSetpoint}", 4)

        if (thermostatSetpoint) {
			targetvalue = thermostatSetpoint
		} else {
			targetvalue = 0.0
		}

        if (getTemperatureScale() == "C" ) {
        	targetvalue = targetvalue.toDouble() - 0.5
        } else {
			targetvalue = targetvalue.toDouble() - 1.0
        }

		sendEvent("name":"thermostatSetpoint", "value":( wantMetric() ? targetvalue : targetvalue.round(0).toInteger() ), displayed: true)
		LOG("In mode $mode lowerSetpoint() to $targetvalue", 5, null, "info")

		// Wait 4 seconds before sending in case we hit the buttons again
		runIn(4, "alterSetpoint", [data: [value:targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
	}

}

//called by raiseSetpoint() and lowerSetpoint()
void alterSetpoint(temp) {
	def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
    def currentTemp = device.currentValue("temperature")
    def saveThermostatSetpoint = device.currentValue("thermostatSetpoint")
	def deviceId = getDeviceId()

	def targetHeatingSetpoint = heatingSetpoint
	def targetCoolingSetpoint = coolingSetpoing

	LOG("alterSetpoint - temp.value is ${temp.value}", 4)

	//step1: check thermostatMode
	if (mode == "heat"){
		if (temp.value > coolingSetpoint){
			targetHeatingSetpoint = temp.value
			targetCoolingSetpoint = temp.value
		} else {
			targetHeatingSetpoint = temp.value
			targetCoolingSetpoint = coolingSetpoint
		}
	} else if (mode == "cool") {
		//enforce limits before sending request to cloud
		if (temp.value < heatingSetpoint){
			targetHeatingSetpoint = temp.value
			targetCoolingSetpoint = temp.value
		} else {
			targetHeatingSetpoint = heatingSetpoint
			targetCoolingSetpoint = temp.value
		}
	} else if (mode == "auto" && usingSmartAuto() ) {
    	// Make changes based on our Smart Auto mode
        if (temp.value > currentTemp) {
        	// Change the heat settings to the new setpoint
            LOG("alterSetpoint() - Smart Auto setting setpoint: ${temp.value}. Updating heat target")
            targetHeatingSetpoint = temp.value
            targetCoolingSetpoint = (temp.value > coolingSetpoint) ? temp.value : coolingSetpoint
		} else {
        	// Change the cool settings to the new setpoint
			LOG("alterSetpoint() - Smart Auto setting setpoint: ${temp.value}. Updating cool target")
            targetCoolingSetpoint = temp.value

            LOG("targetHeatingSetpoint before ${targetHeatingSetpoint}")
            targetHeatingSetpoint = (temp.value < heatingSetpoint) ? temp.value : heatingSetpoint
            LOG("targetHeatingSetpoint after ${targetHeatingSetpoint}")

        }
    } else {
    	LOG("alterSetpoint() called with unsupported mode: ${mode}", 2, null, "warn")
        // return without changing settings on thermostat
        return
    }

	LOG("alterSetpoint >> in mode ${mode} trying to change heatingSetpoint to ${targetHeatingSetpoint} " +
			"coolingSetpoint to ${targetCoolingSetpoint} with holdType : ${whatHoldType()}")

	def sendHoldType = whatHoldType()
	//step2: call parent.setHold to send http request to 3rd party cloud    
	if (parent.setHold(this, targetHeatingSetpoint, targetCoolingSetpoint, deviceId, sendHoldType)) {
		sendEvent("name": "thermostatSetpoint", "value": temp.value.toString(), displayed: false)
		sendEvent("name": "heatingSetpoint", "value": targetHeatingSetpoint)
		sendEvent("name": "coolingSetpoint", "value": targetCoolingSetpoint)
		LOG("alterSetpoint in mode $mode succeed change setpoint to= ${temp.value}", 4)
	} else {
		LOG("WARN: alterSetpoint() - setHold failed. Could be an intermittent problem.", 1, null, "error")
        sendEvent("name": "thermostatSetpoint", "value": saveThermostatSetpoint.toString(), displayed: false)
	}
    // generateSetpointEvent()
	generateStatusEvent()
    // refresh data
    runIn(15, "poll")
}

def generateStatusEvent() {
	def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def temperature = device.currentValue("temperature")

	def statusText	
	LOG("Generate Status Event for Mode = ${mode}", 4)
	LOG("Temperature = ${temperature}", 4)
	LOG("Heating setpoint = ${heatingSetpoint}", 4)
	LOG("Cooling setpoint = ${coolingSetpoint}", 4)
	LOG("HVAC Mode = ${mode}", 4)	

	if (mode == "heat") {
		if (temperature >= heatingSetpoint) {
			statusText = "Right Now: Idle"
		} else {
			statusText = "Heating to ${heatingSetpoint}°"
		}
	} else if (mode == "cool") {
		if (temperature <= coolingSetpoint) {
			statusText = "Right Now: Idle"
		} else {
			statusText = "Cooling to ${coolingSetpoint}°"
		}
	} else if (mode == "auto") {
		statusText = "Right Now: Auto (Heat: ${heatingSetpoint}/Cool: ${coolingSetpoint})"
	} else if (mode == "off") {
		statusText = "Right Now: Off"
	} else if (mode == "emergencyHeat" || mode == "emergency heat" || mode == "emergency") {
		statusText = "Emergency Heat"
	} else {
		statusText = "${mode}?"
	}
	LOG("Generate Status Event = ${statusText}", 4)
	sendEvent("name":"thermostatStatus", "value":statusText, "description":statusText, displayed: true)
}

//generate custom mobile activity feeds event
def generateActivityFeedsEvent(notificationMessage) {
	sendEvent(name: "notificationMessage", value: "$device.displayName $notificationMessage", descriptionText: "$device.displayName $notificationMessage", displayed: true)
}

def noOp() {
	// Doesn't do anything. Here due to a formatting issue on the Tiles!
}

def getSliderRange() {
	return wantMetric ? "(15..30)" : "(50..90)"
}

// Built in functions from SmartThings
// getTemperatureScale()
// fahrenheitToCelsius()
// celsiusToFahrenheit()

def wantMetric() {
	return (getTemperatureScale() == "C")
}

private def cToF(temp) {
    return celsiusToFahrenheit(temp)
}
private def fToC(temp) {
    return fahrenheitToCelsius(temp)
}


private def getImageURLRoot() {
	return "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/dark/"
}

private def getDeviceId() {
	def deviceId = device.deviceNetworkId.split(/\./).last()	
    LOG("getDeviceId() returning ${deviceId}", 4)
    return deviceId
}

private def usingSmartAuto() {
	LOG("Entered usingSmartAuto() ", 5)
	if (settings.smartAuto) { return settings.smartAuto }
    if (parent.settings.smartAuto) { return parent.settings.smartAuto }
    return false
}

private def whatHoldType() {
	def sendHoldType = parent.settings.holdType ? (parent.settings.holdType=="Temporary" || parent.settings.holdType=="Until Next Program")? "nextTransition" : (parent.settings.holdType=="Permanent" || parent.settings.holdType=="Until I Change")? "indefinite" : "indefinite" : "indefinite"
	LOG("Entered whatHoldType() with ${sendHoldType}  settings.holdType == ${settings.holdType}")
	if (settings.holdType && settings.holdType != "") { return  holdType ? (settings.holdType=="Temporary" || settings.holdType=="Until Next Program")? "nextTransition" : (settings.holdType=="Permanent" || settings.holdType=="Until I Change")? "indefinite" : "indefinite" : "indefinite" }   
   
    return sendHoldType
}

private debugLevel(level=3) {
	def debugLvlNum = parent.settings.debugLevel?.toInteger() ?: 3
    def wantedLvl = level?.toInteger()
    
    return ( debugLvlNum >= wantedLvl )
}


private def LOG(message, level=3, child=null, logType="debug", event=false, displayEvent=false) {
	def prefix = ""
	if ( parent.settings.debugLevel?.toInteger() == 5 ) { prefix = "LOG: " }
	if ( debugLevel(level) ) { 
    	log."${logType}" "${prefix}${message}"
        // log.debug message
        if (event) { debugEvent(message, displayEvent) }        
	}    
}


private def debugEvent(message, displayEvent = false) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	if ( debugLevel(4) ) { log.debug "Generating AppDebug Event: ${results}" }
	sendEvent (results)
}



def getTempColors() {
	def colorMap

	colorMap = [
		// Celsius Color Range
		[value: 0, color: "#1e9cbb"],
		[value: 15, color: "#1e9cbb"],
		[value: 19, color: "#1e9cbb"],

		[value: 21, color: "#44b621"],
		[value: 22, color: "#44b621"],
		[value: 24, color: "#44b621"],

		[value: 21, color: "#d04e00"],
		[value: 35, color: "#d04e00"],
		[value: 37, color: "#d04e00"],
		// Fahrenheit Color Range
		[value: 40, color: "#1e9cbb"],
		[value: 59, color: "#1e9cbb"],
		[value: 67, color: "#1e9cbb"],

		[value: 69, color: "#44b621"],
		[value: 72, color: "#44b621"],
		[value: 74, color: "#44b621"],

		[value: 76, color: "#d04e00"],
		[value: 95, color: "#d04e00"],
		[value: 99, color: "#d04e00"]
	]
}