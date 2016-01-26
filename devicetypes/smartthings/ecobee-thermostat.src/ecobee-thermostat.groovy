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
 *	Ecobee Thermostat
 *
 *	Author: SmartThings
 *	Date: 2013-06-13
 *
 * 	Updates by Sean Kendall Schneyer <smartthings@linuxbox.org>
 * 	Date: 2015-12-23
 * 	Incorporate additional device capabilities, some based on code by Yves Racine
 *
 *
 *  Current Version: 0.8.0-RC
 *  Release Date: 2016-01-26
 *  See separate Changelog for change history 
 *
 */
metadata {
	definition (name: "Ecobee Thermostat", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Thermostat"
		capability "Polling"
        capability "Sensor"
		capability "Refresh"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		// capability "Presence Sensor"
        capability "Motion Sensor"
            

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
        

		// Capability "Thermostat"
        attribute "temperatureScale", "string"
		attribute "thermostatSetpoint","number"
		attribute "thermostatStatus","string"
        attribute "apiConnected","string"
        attribute "averagedTemperature","number"

		attribute "currentProgram","string"
        attribute "currentProgramId","string"
		
        attribute "weatherSymbol", "string"
        
        attribute "debugEventFromParent","string"
        
        
	/*
		attribute "thermostatName", "string"
		attribute "temperatureDisplay", "string"
		attribute "coolingSetpointDisplay", "string"
		attribute "heatingSetpointDisplay", "string"
		attribute "heatLevelUp", "string"
		attribute "heatLevelDown", "string"
		attribute "coolLevelUp", "string"
		attribute "coolLevelDown", "string"
		attribute "verboseTrace", "string"
		attribute "fanMinOnTime", "string"
		attribute "humidifierMode", "string"
		attribute "dehumidifierMode", "string"
		attribute "humidifierLevel", "string"
		attribute "dehumidifierLevel", "string"
		attribute "condensationAvoid", "string"
		attribute "groups", "string"
		attribute "equipmentStatus", "string"
		attribute "alerts", "string"
		attribute "programScheduleName", "string"
		attribute "programFanMode", "string"
		attribute "programType", "string"
		attribute "programCoolTemp", "string"
		attribute "programHeatTemp", "string"
		attribute "programCoolTempDisplay", "string"
		attribute "programHeatTempDisplay", "string"
		attribute "programEndTimeMsg", "string"
        
		attribute "weatherDateTime", "string"
		attribute "weatherSymbol", "string"
		attribute "weatherStation", "string"
		attribute "weatherCondition", "string"
		attribute "weatherTemperatureDisplay", "string"
		attribute "weatherPressure", "string"
		attribute "weatherRelativeHumidity", "string"
		attribute "weatherWindSpeed", "string"
		attribute "weatherWindDirection", "string"
		attribute "weatherPop", "string"
		attribute "weatherTempHigh", "string"
		attribute "weatherTempLow", "string"
		attribute "weatherTempHighDisplay", "string"
		attribute "weatherTempLowDisplay", "string"
        
		attribute "plugName", "string"
		attribute "plugState", "string"
		attribute "plugSettings", "string"
		attribute "hasHumidifier", "string"
		attribute "hasDehumidifier", "string"
		attribute "hasErv", "string"
		attribute "hasHrv", "string"
		attribute "ventilatorMinOnTime", "string"
		attribute "ventilatorMode", "string"
		attribute "programNameForUI", "string"
		// Passed in via the SmartApp
        // attribute "thermostatOperatingState", "string"
		attribute "climateList", "string"
		attribute "modelNumber", "string"
		attribute "followMeComfort", "string"
		attribute "autoAway", "string"
		attribute "intervalRevision", "string"
		attribute "runtimeRevision", "string"
		attribute "thermostatRevision", "string"
		attribute "heatStages", "string"
		attribute "coolStages", "string"
		attribute "climateName", "string"
		attribute "setClimate", "string"

		// Report Runtime events
		attribute "auxHeat1RuntimeInPeriod", "string"
		attribute "auxHeat2RuntimeInPeriod", "string"
		attribute "auxHeat3RuntimeInPeriod", "string"
		attribute "compCool1RuntimeInPeriod", "string"
		attribute "compCool2RuntimeInPeriod", "string"
		attribute "dehumidifierRuntimeInPeriod", "string"
		attribute "humidifierRuntimeInPeriod", "string"
		attribute "ventilatorRuntimeInPeriod", "string"
		attribute "fanRuntimeInPeriod", "string"

		attribute "auxHeat1RuntimeDaily", "string"
		attribute "auxHeat2RuntimeDaily", "string"
		attribute "auxHeat3RuntimeDaily", "string"
		attribute "compCool1RuntimeDaily", "string"
		attribute "compCool2RuntimeDaily", "string"
		attribute "dehumidifierRuntimeDaily", "string"
		attribute "humidifierRuntimeDaily", "string"
		attribute "ventilatorRuntimeDaily", "string"
		attribute "fanRuntimeDaily", "string"
		attribute "reportData", "string"

		// Report Sensor Data & Stats
		attribute "reportSensorMetadata", "string"
		attribute "reportSensorData", "string"
		attribute "reportSensorAvgInPeriod", "string"
		attribute "reportSensorMinInPeriod", "string"
		attribute "reportSensorMaxInPeriod", "string"
		attribute "reportSensorTotalInPeriod", "string"

		// Remote Sensor Data & Stats
		attribute "remoteSensorData", "string"
		attribute "remoteSensorTmpData", "string"
		attribute "remoteSensorHumData", "string"
		attribute "remoteSensorOccData", "string"
		attribute "remoteSensorAvgTemp", "string"
		attribute "remoteSensorAvgHumidity", "string"
		attribute "remoteSensorMinTemp", "string"
		attribute "remoteSensorMinHumidity", "string"
		attribute "remoteSensorMaxTemp", "string"
		attribute "remoteSensorMaxHumidity", "string"
		*/


      

        /*
		command "setFanMinOnTime"
		command "setCondensationAvoid"
		command "createVacation"
		command "deleteVacation"
		command "getEcobeePinAndAuth"
		command "getThermostatInfo"
		command "getThermostatSummary"
		command "iterateCreateVacation"
		command "iterateDeleteVacation"
		command "iterateResumeProgram"
		command "iterateSetHold"
		command "resumeProgram"
		command "resumeThisTstat"
		command "setAuthTokens"
		command "setHold"
		command "setHoldExtraParams"
		command "heatLevelUp"
		command "heatLevelDown"
		command "coolLevelUp"
		command "coolLevelDown"
		command "auxHeatOnly"
		command "setThermostatFanMode"
		command "dehumidifierOff"
		command "dehumidifierOn"
		command "humidifierOff"
		command "humidifierAuto"
		command "humidifierManual"
		command "setHumidifierLevel"
		command "setDehumidifierLevel"
		command "updateGroup"
		command "getGroups"
		command "iterateUpdateGroup"
		command "createGroup"
		command "deleteGroup"
		command "updateClimate"
		command "iterateUpdateClimate"
		command "createClimate"
		command "deleteClimate"
		command "setClimate"
		command "iterateSetClimate"
		command "controlPlug"
		command "ventilatorOn"
		command "ventilatorAuto"
		command "ventilatorOff"
		command "ventilatorAuto"
		command "setVentilatorMinOnTime"
		command "awake"
		command "away"
		command "present"
		command "home"
		command "asleep"
		command "quickSave"
		command "setThisTstatClimate"
		command "setThermostatSettings"
		command "iterateSetThermostatSettings"
		command "getEquipmentStatus"
		command "refreshChildTokens"
		command "autoAway"
		command "followMeComfort"
		command "getReportData"
		command "generateReportRuntimeEvents"
		command "generateReportSensorStatsEvents"
		command "getThermostatRevision"
		command "generateRemoteSensorEvents"
        */


	}

	simulator { }

    	tiles(scale: 2) {

      
		multiAttributeTile(name:"iOSsummary", type:"thermostat", width:6, height:4) {
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
            	// TODO: Change this to a preference so the use can select green over grey from within the app
            	// Uncomment the below if you prefer green for idle
				attributeState("idle", backgroundColor:"#44b621")
				// Or uncomment this one if you prefer grey for idle
				// attributeState("idle", backgroundColor:"#C0C0C0")
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
            	attributeState("default", label:'${currentValue}', unit:"F")
            }
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"F")
			}

        } // End multiAttributeTile
        

        // Workaround until they fix the Thermostat tile. Only use this one OR the above one, not both
        multiAttributeTile(name:"summary", type: "lighting", width: 6, height: 4) {
        	tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°', unit:"F",
				backgroundColors: getTempColors())
			}

			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
                attributeState("default", action: "setTemperature")
			}

            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue}%', unit:"%")
			}

			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
            	// TODO: Change this to a preference so the use can select green over grey from within the app
            	// Uncomment the below if you prefer green for idle
				attributeState("idle", backgroundColor:"#44b621")
				// Or uncomment this one if you prefer grey for idle
				// attributeState("idle", backgroundColor:"#C0C0C0")
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
            	attributeState("default", label:'${currentValue}', unit:"F")
            }
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"F")
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
			state "off", action:"switchMode", nextState: "updating", icon: "st.thermostat.heating-cooling-off"
			state "heat", action:"switchMode",  nextState: "updating", icon: "st.thermostat.heat"
			state "cool", action:"switchMode",  nextState: "updating", icon: "st.thermostat.cool"
			state "auto", action:"switchMode",  nextState: "updating", icon: "st.thermostat.auto"
			state "auxHeatOnly", action:"switchMode", icon: "st.thermostat.emergency-heat"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
		standardTile("fanMode", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "auto", label:'Fan: ${currentValue}', action:"thermostat.fanOn", nextState: "updating", icon: "st.Appliances.appliances11"
			state "on", label:'Fan: ${currentValue}', action:"thermostat.fanAuto", nextState: "updating", icon: "st.Appliances.appliances11"
			// state "off", label:'Fan: ${currentValue}', action:"switchFanMode", nextState: "updating", icon: "st.Appliances.appliances11"
			// state "circulate", label:'Fan: ${currentValue}', action:"switchFanMode", nextState: "updating", icon: "st.Appliances.appliances11"
            state "updating", label:"Working", icon: "st.secondary.secondary"
		}
		standardTile("upButtonControl", "device.thermostatSetpoint", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"raiseSetpoint", icon:"st.thermostat.thermostat-up"
		}
		valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 2, height: 2, decoration: "flat") {
			state "thermostatSetpoint", label:'${currentValue}°'
		}
		valueTile("currentStatus", "device.thermostatStatus", height: 2, width: 4, decoration: "flat") {
			state "thermostatStatus", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		standardTile("downButtonControl", "device.thermostatSetpoint", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"lowerSetpoint", icon:"st.thermostat.thermostat-down"
		}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 4, inactiveLabel: false, range:"(15..85") {
			state "setHeatingSetpoint", action:"thermostat.setHeatingSetpoint", backgroundColor:"#d04e00", unit: '${getTemperatureScale()}'
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}°\nHeat', unit:"dF", backgroundColor:"#d04e00"
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 4, inactiveLabel: false, range:"(15..85") {
			state "setCoolingSetpoint", action:"thermostat.setCoolingSetpoint", backgroundColor: "#1e9cbb", unit: '${getTemperatureScale()}'
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue}°\nCool', unit:"dF", backgroundColor: "#1e9cbb"
		}
		standardTile("refresh", "device.thermostatMode", width: 2, height: 2,inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("resumeProgram", "device.resumeProgram", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "resume", action:"resumeProgram", nextState: "updating", label:'Resume Schedule', icon:"st.Office.office7"
			state "updating", label:"Working", icon: "st.samsung.da.oven_ic_send"
		}
        valueTile("currentProgram", "device.currentProgramName", height: 2, width: 4, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Comfort Setting:\n${currentValue}' 
		}
        
		standardTile("setHome", "device.setHome", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "home", action:"home", nextState: "updating", label:'Set Home', icon:"st.Home.home4"
			state "updating", label:"Working...", icon: "st.samsung.da.oven_ic_send"
		}
        
        standardTile("setAway", "device.setAway", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "away", action:"away", nextState: "updating", label:'Set Away', icon:"st.Home.home1"
			state "updating", label:"Working...", icon: "st.samsung.da.oven_ic_send"
		}

        standardTile("setSleep", "device.setSleep", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "sleep", action:"sleep", nextState: "updating", label:'Set Sleep', icon:"st.Bedroom.bedroom2"
			state "updating", label:"Working...", icon: "st.samsung.da.oven_ic_send"
		}

        standardTile("operatingState", "device.thermostatOperatingState", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			//state "idle", label: "Idle", backgroundColor:"#44b621", icon: "st.nest.empty"
            state "idle", label: "Idle", backgroundColor:"#c0c0c0", icon: "st.nest.empty"
            state "fan only", label: "Fan Only", backgroundColor:"#87ceeb", icon: "st.Appliances.appliances11"
			state "heating", backgroundColor:"#ffa81e", icon: "st.thermostat.heat"
			state "cooling", backgroundColor:"#269bd2", icon: "st.thermostat.cool"
            // Issue reported that the label overlaps. Need to remove the icon
            state "default", label: '${currentValue}', backgroundColor:"c0c0c0", icon: "st.nest.empty"
		}

        valueTile("humidity", "device.humidity", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: 'Humidity\n${currentValue}%', unit: "humidity" // Add a blue background signifying water?
		}


		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
		}


        // Additional tiles based on Yves Racine's device type
        // Weather Tiles and other Forecast related tiles
		standardTile("weatherIcon", "device.weatherSymbol", inactiveLabel: false, width: 2, height: 2,
			decoration: "flat") {
			state "-2",			label: 'updating...',	icon: "st.unknown.unknown.unknown"
			state "0",			label: 'Sunny',			icon: "st.Weather.weather14"
			state "1",			label: 'Few Clouds',	icon: "st.Weather.weather11"
			state "2",			label: 'Partly Cloudy',	icon: "st.Weather.weather11"
			state "3",			label: 'Mostly Cloudy',	icon: "st.Weather.weather13"
			state "4",			label: 'Overcast',		icon: "st.Weather.weather13"
			state "5",			label: 'Drizzle',		icon: "st.Weather.weather10"
			state "6",			label: 'Rain',			icon: "st.Weather.weather10"
			state "7",			label: 'Freezing Rain',	icon: "st.Weather.weather6"
			state "8",			label: 'Showers',		icon: "st.Weather.weather10"
			state "9",			label: 'Hail',			icon: "st.custom.wuk.sleet"
			state "10",			label: 'Snow',			icon: "st.Weather.weather6"
			state "11",			label: 'Flurries',		icon: "st.Weather.weather6"
			state "12",			label: 'Sleet',			icon: "st.Weather.weather6"
			state "13",			label: 'Blizzard',		icon: "st.Weather.weather7"
			state "14",			label: 'Pellets',		icon: "st.custom.wuk.sleet"
			state "15",			label: 'Thunder Storms',icon: "st.custom.wuk.tstorms"
			state "16",			label: 'Windy',			icon: "st.Transportation.transportation5"
			state "17",			label: 'Tornado',		icon: "st.Weather.weather1"
			state "18",			label: 'Fog',			icon: "st.Weather.weather13"
			state "19",			label: 'Hazy',			icon: "st.Weather.weather13"
			state "20",			label: 'Smoke',			icon: "st.Weather.weather13"
			state "21",			label: 'Dust',			icon: "st.Weather.weather13"
		}
		valueTile("weatherDateTime", "device.weatherDateTime", inactiveLabel: false,
			width: 3, height: 2, decoration: "flat") {
			state "default", label: '${currentValue}'
		}
		valueTile("weatherConditions", "device.weatherCondition",
			inactiveLabel: false, width: 3, height: 2, decoration: "flat") {
			state "default", label: 'Forecast\n${currentValue}'
		}
		standardTile("weatherTemperature", "device.weatherTemperature", inactiveLabel:
			false, width: 2, height: 2, decoration: "flat") {
			state "default", label: 'Outside: ${currentValue}°', unit: "dF", icon: "st.Weather.weather2"
		}
		valueTile("weatherRelativeHumidity", "device.weatherRelativeHumidity",
			inactiveLabel: false, width: 2, height: 2,decoration: "flat") {
			state "default", label: 'Out Hum\n${currentValue}%', unit: "humidity"
		}
		valueTile("weatherTempHigh", "device.weatherTempHigh", inactiveLabel: false,
			width: 2, height: 2, decoration: "flat") {
			state "default", label: 'ForecastH\n${currentValue}°', unit: "dF"
		}
		valueTile("weatherTempLow", "device.weatherTempLow", inactiveLabel: false,
			width: 2, height: 2, decoration: "flat") {
			state "default", label: 'ForecastL\n${currentValue}°', unit: "dF"
		}
		valueTile("weatherPressure", "device.weatherPressure", inactiveLabel: false,
			width: 2, height: 2, decoration: "flat") {
			state "default", label: 'Pressure\n${currentValue}', unit: "hpa"
		}
		valueTile("weatherWindDirection", "device.weatherWindDirection",
			inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "default", label: 'W.Dir\n${currentValue}'
		}
		valueTile("weatherWindSpeed", "device.weatherWindSpeed", inactiveLabel: false,
			width: 2, height: 2, decoration: "flat") {
			state "default", label: 'W.Speed\n${currentValue}'
		}
		valueTile("weatherPop", "device.weatherPop", inactiveLabel: false, width: 2,
			height: 2, decoration: "flat") {
			state "default", label: 'PoP\n${currentValue}%', unit: "%"
		}



		main(["temperature", "summary"])
        // details(["summary","temperature", "upButtonControl", "thermostatSetpoint", "currentStatus", "downButtonControl", "mode", "weatherIcon", "resumeProgram", "refresh"])
        // details(["summary","apiStatus", "upButtonControl", "thermostatSetpoint", "currentStatus", "downButtonControl", "mode", "weatherIcon", "resumeProgram", "refresh"])
        details(["summary",
        	"operatingState", "weatherIcon", "weatherTemperature",
            "motion", "resumeProgram", "mode",
            "coolSliderControl", "coolingSetpoint",
            "heatSliderControl", "heatingSetpoint",
            "currentStatus", "apiStatus",
            "currentProgram", "fanMode",
            "setHome", "setAway", "setSleep",
            "refresh"
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
    LOG("Debug level of parent: ${parent.settings?.debugLevel}")
	def linkText = getLinkText(device)

	if(results) {
		results.each { name, value ->
			LOG("generateEvent() - In each loop: name: ${name}  value: ${value}", 4)
			def isChange = false
			def isDisplayed = true
			def event = [name: name, linkText: linkText, descriptionText: getThermostatDescriptionText(name, value, linkText),
			handlerName: name]

			if (name=="temperature" || name=="heatingSetpoint" || name=="coolingSetpoint" || name=="weatherTemperature" ) {
				def sendValue = value // ? convertTemperatureIfNeeded(value.toDouble(), "F", 1): value //API return temperature value in F
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
                isChange = true;
                isDisplayed = isChange
                event << [value: value.toString(), isStateChange: isChange, displayed: isDisplayed]
            } else{
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

	if (parent.setHold (this, heatingSetpoint,  coolingSetpoint, deviceId, sendHoldType)) {
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
	if (parent.setHold (this, heatingSetpoint,  coolingSetpoint, deviceId, sendHoldType)) {
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
	["on", "auto", "circulate"]
}


// TODO Add a delay (like in the setTemperature case) to capture multiple clicks on the UI
def switchMode() {
	LOG("in switchMode()", 5)
	def currentMode = device.currentState("thermostatMode")?.value
	def lastTriedMode = state.lastTriedMode ?: currentMode ?: "off"
	def modeOrder = modes()
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	switchToMode(nextMode)
}

def switchToMode(nextMode) {
	LOG("In switchToMode = ${nextMode}", 5)
	if (nextMode in modes()) {
		state.lastTriedMode = nextMode
		"$nextMode"()
	} else {
		LOG("switchToMode(): No mode method: ${nextMode}", 1, null, "warn")
	}
}

def switchFanMode() {
	LOG("switchFanMode()", 5)
	def currentFanMode = device.currentState("thermostatFanMode")?.value
	LOG("switching fan from current mode: $currentFanMode", 4)
	def returnCommand

	switch (currentFanMode) {
		case "fanAuto":
			returnCommand = switchToFanMode("fanOn")
			break
		case "fanOn":
			returnCommand = switchToFanMode("fanCirculate")
			break
		case "fanCirculate":
			returnCommand = switchToFanMode("fanAuto")
			break
	}
	if(!currentFanMode) { returnCommand = switchToFanMode("fanOn") }
	returnCommand
}

def switchToFanMode(nextMode) {

	LOG("switching to fan mode: $nextMode", 4)
	def returnCommand

	if(nextMode == "fanAuto") {
		if(!fanModes.contains("fanAuto")) {
			returnCommand = fanAuto()
		} else {
			returnCommand = switchToFanMode("fanOn")
		}
	} else if(nextMode == "fanOn") {
		if(!fanModes.contains("fanOn")) {
			returnCommand = fanOn()
		} else {
			returnCommand = switchToFanMode("fanCirculate")
		}
	} else if(nextMode == "fanCirculate") {
		if(!fanModes.contains("fanCirculate")) {
			returnCommand = fanCirculate()
		} else {
			returnCommand = switchToFanMode("fanAuto")
		}
	}
	returnCommand
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}



def generateQuickEvent(name, value, pollIn=0) {
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


def setThermostatMode(String value, holdType=null) {
	// 	"emergencyHeat" "heat" "cool" "off" "auto"
    
    if (value=="emergency" || value=="emergencyHeat") { value = "auxHeatOnly" }    
	LOG("setThermostatMode(${value})", 5)
    
    def deviceId = getDeviceId()
	if (parent.setMode(this, value, deviceId, holdType))
		generateQuickEvent("thermostatMode", value, 15)
	else {
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
    setThermostatProgram("home")
}

def away() {
	// Change the Comfort Setting to Away
    LOG("away()", 5)
    setThermostatProgram("away")
}

def sleep() {
	// Change the Comfort Setting to Sleep    
    LOG("sleep()", 5)
    setThermostatProgram("sleep")
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
	if ( parent.setFanMode(this, value, getDeviceId()) ) {
    	generateQuickEvent("thermostatFanMode", value, 15)
    } else {
    	generateQuickEvent("thermostatFanMode", device.currentValue("thermostatFanMode"))
    }
    
	generateSetpointEvent()
	generateStatusEvent()    
}

def fanOn() {
	LOG("fanON()", 5)
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

	LOG("Generate SetPoint Event", 5)

	def mode = device.currentValue("thermostatMode")    
    def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
    
    	LOG("Current Mode = ${mode}")
		LOG("Heating Setpoint = ${heatingSetpoint}")
		LOG("Cooling Setpoint = ${coolingSetpoint}")
    

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

		runIn(4, "alterSetpoint", [data: [value:targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
}

//called by tile when user hit raise temperature button on UI
void lowerSetpoint() {
	def mode = device.currentValue("thermostatMode")
	def targetvalue

	if (mode == "off" || (mode == "auto" && !usingSmartAuto() )) {
		log.warn "lowerSetpoint(): this mode: $mode does not allow lowerSetpoint"
    } else {

    	def heatingSetpoint = device.currentValue("heatingSetpoint")
		def coolingSetpoint = device.currentValue("coolingSetpoint")
		def thermostatSetpoint = device.currentValue("thermostatSetpoint").toDouble()
		log.debug "lowerSetpoint() mode = ${mode}, heatingSetpoint: ${heatingSetpoint}, coolingSetpoint:${coolingSetpoint}, thermostatSetpoint:${thermostatSetpoint}"

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
		log.debug "alterSetpoint in mode $mode succeed change setpoint to= ${temp.value}"
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

	
	LOG("Generate Status Event for Mode = ${mode}")
	LOG("Temperature = ${temperature}")
	LOG("Heating set point = ${heatingSetpoint}")
	LOG("Cooling set point = ${coolingSetpoint}")
	LOG("HVAC Mode = ${mode}")
	

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


// Built in functions from SmartThings?
// getTemperatureScale()
// fahrenheitToCelsius()
// celsiusToFahrenheit()

def wantMetric() {
	return (getTemperatureScale() == "C")
}

private def cToF(temp) {
	// return (temp * 1.8 + 32)
    return celsiusToFahrenheit(temp)
}
private def fToC(temp) {
	// return (temp - 32) / 1.8
    return fahrenheitToCelsius(temp)
}
private def milesToKm(distance) {
	return (distance * 1.609344)
}
private def get_URI_ROOT() {
	return "https://api.ecobee.com"
}
// Maximum tstat batch size (25 thermostats max may be processed in batch)
private def get_MAX_TSTAT_BATCH() {
	return 25
}


private def getDeviceId() {
	return device.deviceNetworkId.split(/\./).last()	
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