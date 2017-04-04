/**
 *  Nest Thermostat
 *	Author: Anthony S. (@tonesto7)
 *	Co-Author: Eric S. (@E_Sch)
 *	Contributor: Ben W. (@desertBlade)
 *  Graphing Modeled on code from Andreas Amann (@ahndee)
 *
 * Modeled after the EcoBee thermostat under Templates in the IDE
 * Copyright (C) 2017 Anthony S.
 * Licensing Info: Located at https://raw.githubusercontent.com/tonesto7/nest-manager/master/LICENSE.md
 */

import java.text.SimpleDateFormat
import groovy.time.*

def devVer() { return "5.0.0" }

// for the UI
metadata {
	definition (name: "${textDevName()}", namespace: "tonesto7", author: "Anthony S.") {
		capability "Actuator"
		capability "Relative Humidity Measurement"
		capability "Refresh"
		capability "Sensor"
		capability "Thermostat"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Fan Mode"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Thermostat Setpoint"
		capability "Temperature Measurement"
		capability "Health Check"

		command "refresh"
		command "poll"

		command "away"
		command "present"
		command "eco"
		//command "setAway"
		//command "setHome"
		command "setPresence"
		//command "setFanMode"
		//command "setTemperature"
		command "setThermostatMode"
		command "levelUpDown"
		command "levelUp"
		command "levelDown"
		command "log"
		command "heatingSetpointUp"
		command "heatingSetpointDown"
		command "coolingSetpointUp"
		command "coolingSetpointDown"
		command "changeMode"
		command "changeFanMode"
		command "updateNestReportData"
		command "ecoDesc", ["string"]
		command "whoMadeChanges", ["string", "string", "string"]

		attribute "devVer", "string"
		attribute "temperatureUnit", "string"
		attribute "targetTemp", "string"
		attribute "softwareVer", "string"
		attribute "lastConnection", "string"
		attribute "apiStatus", "string"
		attribute "hasLeaf", "string"
		attribute "debugOn", "string"
		attribute "safetyTempMin", "string"
		attribute "safetyTempMax", "string"
		attribute "safetyTempExceeded", "string"
		attribute "comfortHumidityMax", "string"
		attribute "comfortHumidtyExceeded", "string"
		//attribute "safetyHumidityMin", "string"
		attribute "comfortDewpointMax", "string"
		attribute "comfortDewpointExceeded", "string"
		attribute "tempLockOn", "string"
		attribute "lockedTempMin", "string"
		attribute "lockedTempMax", "string"
		attribute "devTypeVer", "string"
		attribute "onlineStatus", "string"
		attribute "nestPresence", "string"
		attribute "nestThermostatMode", "string"
		attribute "presence", "string"
		attribute "canHeat", "string"
		attribute "canCool", "string"
		attribute "hasAuto", "string"
		attribute "hasFan", "string"
		attribute "sunlightCorrectionEnabled", "string"
		attribute "sunlightCorrectionActive", "string"
		attribute "timeToTarget", "string"
		attribute "nestType", "string"
		attribute "pauseUpdates", "string"
		attribute "nestReportData", "string"
		attribute "previousthermostatMode", "string"
		attribute "whoMadeChanges", "string"
		attribute "whoMadeChangesDesc", "string"
		attribute "whoMadeChangesDescDt", "string"
		attribute "whoSetEcoMode", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}°')
			}
			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
				attributeState("default", action: "levelUpDown")
				attributeState("VALUE_UP", action: "levelUp")
				attributeState("VALUE_DOWN", action: "levelDown")
			}
			tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue}%', unit:"%")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle",			backgroundColor:"#44B621")
				attributeState("heating",		 backgroundColor:"#FFA81E")
				attributeState("cooling",		 backgroundColor:"#2ABBF0")
				attributeState("fan only",		  backgroundColor:"#145D78")
				attributeState("pending heat",	  backgroundColor:"#B27515")
				attributeState("pending cool",	  backgroundColor:"#197090")
				attributeState("vent economizer", backgroundColor:"#8000FF")
			}
			tileAttribute("device.nestThermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
				attributeState("cool", label:'${name}')
				attributeState("auto", label:'${name}')
				attributeState("eco", label:'${name}')
				attributeState("emergency heat", label:'${name}')
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label:'${currentValue}')
			}
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}')
			}
		}
		valueTile("temp2", "device.temperature", width: 2, height: 2, decoration: "flat") {
			state("default", label:'${currentValue}°', icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/nest_like.png",
					backgroundColors: getTempColors())
		}
		standardTile("thermostatMode", "device.nestThermostatMode", width:2, height:2, decoration: "flat") {
			state("off", 	action:"changeMode", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/off_btn_icon.png")
			state("heat", 	action:"changeMode", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_btn_icon.png")
			state("cool", 	action:"changeMode", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_btn_icon.png")
			state("auto", 	action:"changeMode", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_cool_btn_icon.png")
			state("eco", 	action:"changeMode", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/eco_icon.png")
			state("emergency heat", action:"changeMode", nextState: "updating", icon: "st.thermostat.emergency")
			state("updating", label:"", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png")
		}


		standardTile("offBtn", "device.off", width:1, height:1, decoration: "flat") {
			state("default", action: "off", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/off_btn_icon.png")
		}
		standardTile("ecoBtn", "device.eco", width:1, height:1, decoration: "flat") {
			state("default", action: "eco", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/eco_icon.png")
		}
		standardTile("heatBtn", "device.canHeat", width:1, height:1, decoration: "flat") {
			state("true", action: "heat", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_btn_icon.png")
			state "false", label: ''
		}
		standardTile("coolBtn", "device.canCool", width:1, height:1, decoration: "flat") {
			state("true", action: "cool", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_btn_icon.png")
			state "false", label: ''
		}
		standardTile("autoBtn", "device.hasAuto", width:1, height:1, decoration: "flat") {
			state("true", action: "auto", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_cool_btn_icon.png")
			state "false", label: ''
		}

		standardTile("thermostatFanMode", "device.thermostatFanMode", width:2, height:2, decoration: "flat") {
			state("auto", action: "changeFanMode", 	nextState: "updating",	icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/fan_auto_icon.png")
			state("on",	action: "changeFanMode",	nextState: "updating", 	icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/fan_on_icon.png")
			state("updating", label:"", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png")
			state "disabled", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/fan_disabled_icon.png"
		}
		standardTile("nestPresence", "device.nestPresence", width:2, height:2, decoration: "flat") {
			state "home", 		action: "setPresence",	icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/pres_home_icon.png"
			state "away", 		action: "setPresence", 	icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/pres_away_icon.png"
			state "auto-away", 	action: "setPresence", 	icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/pres_autoaway_icon.png"
			state "unknown",	action: "setPresence", 	icon: "st.unknown.unknown.unknown"
		}
		standardTile("refresh", "device.refresh", width:2, height:2, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/refresh_icon.png"
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 1, height: 1) {
			state("heatingSetpoint", label:'${currentValue}', unit: "Heat", foregroundColor: "#FFFFFF",
				backgroundColors: [ [value: 0, color: "#FFFFFF"], [value: 7, color: "#FF3300"], [value: 15, color: "#FF3300"] ])
			state("disabled" , label: '', foregroundColor: "#FFFFFF", backgroundColor: "#FFFFFF")
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", width: 1, height: 1) {
			state("coolingSetpoint", label: '${currentValue}', unit: "Cool", foregroundColor: "#FFFFFF",
				backgroundColors: [ [value: 0, color: "#FFFFFF"], [value: 7, color: "#0099FF"], [value: 15, color: "#0099FF"] ])
			state("disabled", label: '', foregroundColor: "#FFFFFF", backgroundColor: "#FFFFFF")
		}
		standardTile("heatingSetpointUp", "device.heatingSetpoint", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
			state "default", label: '', action:"heatingSetpointUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_up.png"
			state "", label: ''
		}
		standardTile("heatingSetpointDown", "device.heatingSetpoint",  width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
			state "default", label:'', action:"heatingSetpointDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_down.png"
			state "", label: ''
		}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 3, range: getRange(), inactiveLabel: false) {
			state "default", action:"setHeatingSetpoint", backgroundColor:"#FF3300"
			state "", label: ''
		}
		standardTile("coolingSetpointUp", "device.coolingSetpoint", width: 1, height: 1,canChangeIcon: false, decoration: "flat") {
			state "default", label:'', action:"coolingSetpointUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_up.png"
			state "", label: ''
		}
		standardTile("coolingSetpointDown", "device.coolingSetpoint", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
			state "default", label:'', action:"coolingSetpointDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_down.png"
			state "", label: ''
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 3, range: getRange(), inactiveLabel: false) {
			state "setCoolingSetpoint", action:"setCoolingSetpoint", backgroundColor:"#0099FF"
			state "", label: ''
		}

		standardTile("blank", "device.heatingSetpoint", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
			state "default", label: ''
		}
		htmlTile(name:"graphHTML", action: "graphHTML", width: 6, height: 13, whitelist: ["www.gstatic.com", "raw.githubusercontent.com", "cdn.rawgit.com"])

		main("temp2")
		details( ["temperature", "thermostatMode", "nestPresence", "thermostatFanMode",
				"heatingSetpointDown", "heatingSetpoint", "heatingSetpointUp", "coolingSetpointDown", "coolingSetpoint", "coolingSetpointUp",
				"heatSliderControl", "coolSliderControl", "graphHTML", "offBtn", "ecoBtn", "heatBtn", "coolBtn", "autoBtn", "blank", "refresh"] )
	}
	preferences {
		input "resetHistoryOnly", "bool", title: "Reset History Data", description: "", displayDuringSetup: false
		input "resetAllData", "bool", title: "Reset All Stored Event Data", description: "", displayDuringSetup: false
	}
}

def compileForC() {
	def retVal = false   // if using C mode, set this to true so that enums and colors are correct (due to ST issue of compile time evaluation)
	return retVal
}

def getTempColors() {
	def colorMap
//getTemperatureScale() == "C"   wantMetric()
	if (compileForC()) {
		colorMap = [
			// Celsius Color Range
			[value: 0, color: "#153591"],
			[value: 7, color: "#1e9cbb"],
			[value: 15, color: "#90d2a7"],
			[value: 23, color: "#44b621"],
			[value: 29, color: "#f1d801"],
			[value: 33, color: "#d04e00"],
			[value: 36, color: "#bc2323"]
			]
	} else {
		colorMap = [
			// Fahrenheit Color Range
			[value: 40, color: "#153591"],
			[value: 44, color: "#1e9cbb"],
			[value: 59, color: "#90d2a7"],
			[value: 74, color: "#44b621"],
			[value: 84, color: "#f1d801"],
			[value: 92, color: "#d04e00"],
			[value: 96, color: "#bc2323"]
		]
	}
}

def lowRange() { return compileForC() ? 9 : 50 }
def highRange() { return compileForC() ? 32 : 90 }
def getRange() { return "${lowRange()}..${highRange()}" }

mappings {
	path("/graphHTML") { action: [GET: "getGraphHTML"] }
}

void checkStateClear() {
	//Logger("checkStateClear...")
	def before = getStateSizePerc()
	if(!state?.resetAllData && resetAllData) {
		Logger("checkStateClear...Clearing ALL")
		def data = getState()?.findAll { !(it?.key in ["eric", "virtual"]) }
		data.each { item ->
			state.remove(item?.key.toString())
		}
		state.resetAllData = true
		state.resetHistoryOnly = true
		Logger("Device State Data: before: $before  after: ${getStateSizePerc()}")
	} else if(state?.resetAllData && !resetAllData) {
		Logger("checkStateClear...resetting ALL toggle")
		state.resetAllData = false
	}
	if(!state?.resetHistoryOnly && resetHistoryOnly) {
		Logger("checkStateClear...Clearing HISTORY")
		def data = getState()?.findAll {
			(it?.key in ["today", "temperatureTable", "operatingStateTable", "humidityTable", "historyStoreMap", "temperatureTableYesterday", "operatingStateTableYesterday", "humidityTableYesterday"])
		}
		data.each { item ->
			state.remove(item?.key.toString())
		}
		state.resetHistoryOnly = true
		Logger("Device State Data: before: $before  after: ${getStateSizePerc()}")
	} else if(state?.resetHistoryOnly && !resetHistoryOnly) {
		Logger("checkStateClear...resetting HISTORY toggle")
		state.resetHistoryOnly = false
	}
	//LogAction("Device State Data: ${getState()}")
}

def initialize() {
	Logger("initialized...")
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 2000) {
		state.updatedLastRanAt = now()
		checkVirtualStatus()
		verifyHC()
	} else {
		log.trace "initialize(): Ran within last 2 seconds - SKIPPING"
	}
}

void installed() {
	Logger("installed...")
	initialize()
	state.isInstalled = true
}

void updated() {
	Logger("Device Updated...")
	initialize()
}

void checkVirtualStatus() {
	if(getDataValue("isVirtual") == null && state?.virtual != null) {
		def res = (state?.virtual instanceof Boolean) ? state?.virtual : false
		Logger("Updating the device's 'isVirtual' data value to (${res})")
		updateDataValue("isVirtual", "${res}")
	} else {
		def dVal = getDataValue("isVirtual").toString() == "true" ? true : false
		if(dVal != state?.virtual || state?.virtual == null) {
			state?.virtual = dVal
			Logger("Setting virtual to ${dVal?.toString()?.toUpperCase()}")
		}
	}
}

def getHcTimeout() {
	def to = state?.hcTimeout
	return ((to instanceof Integer) ? to.toInteger() : 35)*60
}

void verifyHC() {
	def val = device.currentValue("checkInterval")
	def timeOut = getHcTimeout()
	if(!val || val.toInteger() != timeOut) {
		Logger("verifyHC: Updating Device Health Check Interval to $timeOut")
		sendEvent(name: "checkInterval", value: timeOut, data: [protocol: "cloud"], displayed: false)
	}
	sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"CLOUD\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${hub?.hub?.hardwareID}\"}")
}

def ping() {
	Logger("ping...")
	keepAwakeEvent()
	//refresh()
}

def parse(String description) {
	LogAction("Parsing '${description}'")
}

def poll() {
	Logger("Polling parent...")
	poll()
}

def refresh() {
	pauseEvent("false")
	parent.refresh(this)
}

// parent calls this method to queue data.
// goal is to return to parent asap to avoid execution timeouts

def generateEvent(Map eventData) {
	//LogAction("generateEvent Parsing data ${eventData}", "trace")
	def eventDR = [evt:eventData]
	runIn(8, "processEvent", [overwrite: true, data: eventDR] )
}

void processEvent(data) {
	def pauseUpd = !device.currentValue("pauseUpdates") ? false : device.currentValue("pauseUpdates").value
	if(pauseUpd == "true") { LogAction("pausing", "warn"); return }

	def eventData = data?.evt
	checkStateClear()

	//LogAction("processEvent Parsing data ${eventData}", "trace")
	try {
		LogAction("------------START OF API RESULTS DATA------------", "warn")
		if(eventData) {
			state.restStreaming = eventData?.restStreaming == true ? true : false
			state.useMilitaryTime = eventData?.mt ? true : false
			state.showLogNamePrefix = eventData?.logPrefix == true ? true : false
			state.enRemDiagLogging = eventData?.enRemDiagLogging == true ? true : false
			state.healthMsg = eventData?.healthNotify == true ? true : false
			state.showGraphs = eventData?.showGraphs != null ? eventData?.showGraphs : true
			if(eventData?.allowDbException) { state?.allowDbException = eventData?.allowDbException = false ? false : true }
			debugOnEvent(eventData?.debug ? true : false)
			deviceVerEvent(eventData?.latestVer.toString())
			if(virtType()) { nestTypeEvent("virtual") } else { nestTypeEvent("physical") }
			if(eventData.hcTimeout && (state?.hcTimeout != eventData?.hcTimeout || !state?.hcTimeout)) {
				state.hcTimeout = eventData?.hcTimeout
				verifyHC()
			}
			if(state?.swVersion != devVer()) {
				initialize()
				state.swVersion = devVer()
				state?.shownChgLog = false
			}
			state?.childWaitVal = eventData?.childWaitVal.toInteger()
			state.clientBl = eventData?.clientBl == true ? true : false
			state.mobileClientType = eventData?.mobileClientType
			state.curExtTemp = eventData?.curExtTemp
			state.nestTimeZone = eventData.tz ?: null
			tempUnitEvent(getTemperatureScale())
			if(eventData?.data?.is_locked != null) { tempLockOnEvent(eventData?.data?.is_locked.toString() == "true" ? true : false) }
			canHeatCool(eventData?.data?.can_heat, eventData?.data?.can_cool)
			hasFan(eventData?.data?.has_fan.toString())
			presenceEvent(eventData?.pres.toString())

			def curMode = device?.currentState("nestThermostatMode")?.value.toString()
			hvacModeEvent(eventData?.data?.hvac_mode.toString())
			def newMode = device?.currentState("nestThermostatMode")?.value.toString()
			if(newMode == "eco" && curMode != newMode) {
				ecoDescEvent("Set Outside of this DTH")
			} else { ecoDescEvent(null, true) }

			hvacPreviousModeEvent(eventData?.data?.previous_hvac_mode.toString())
			hasLeafEvent(eventData?.data?.has_leaf)
			humidityEvent(eventData?.data?.humidity.toString())
			operatingStateEvent(eventData?.data?.hvac_state.toString())
			fanModeEvent(eventData?.data?.fan_timer_active.toString())
			if(!eventData?.data?.last_connection) { lastCheckinEvent(null,null) }
			else { lastCheckinEvent(eventData?.data?.last_connection, eventData?.data?.is_online.toString()) }
			sunlightCorrectionEnabledEvent(eventData?.data?.sunlight_correction_enabled)
			sunlightCorrectionActiveEvent(eventData?.data?.sunlight_correction_active)
			timeToTargetEvent(eventData?.data?.time_to_target, eventData?.data?.time_to_target_training)
			softwareVerEvent(eventData?.data?.software_version.toString())
			//onlineStatusEvent(eventData?.data?.is_online.toString())
			apiStatusEvent(eventData?.apiIssues)
			if(eventData?.htmlInfo) { state?.htmlInfo = eventData?.htmlInfo }
			safetyTempsEvent(eventData?.safetyTemps)
			comfortHumidityEvent(eventData?.comfortHumidity)
			comfortDewpointEvent(eventData?.comfortDewpoint)
			state.voiceReportPrefs = eventData?.vReportPrefs
			autoSchedDataEvent(eventData?.autoSchedData)
			state?.devBannerData = eventData?.devBannerData ?: null

			def hvacMode = state?.nestHvac_mode
			def tempUnit = state?.tempUnit
			switch (tempUnit) {
				case "C":
					def heatingSetpoint = 0.0
					def coolingSetpoint = 0.0
					def temp = eventData?.data?.ambient_temperature_c.toDouble()
					def targetTemp = eventData?.data?.target_temperature_c.toDouble()

					if (hvacMode == "cool") {
						coolingSetpoint = targetTemp
					}
					else if (hvacMode == "heat") {
						heatingSetpoint = targetTemp
					}
					else if (hvacMode == "auto") {
						coolingSetpoint = Math.round(eventData?.data?.target_temperature_high_c.toDouble())
						heatingSetpoint = Math.round(eventData?.data?.target_temperature_low_c.toDouble())
					}
					if(hvacMode == "eco") {
						if(eventData?.data?.eco_temperature_high_c) { coolingSetpoint = eventData?.data?.eco_temperature_high_c.toDouble() }
						else if(eventData?.data?.away_temperature_high_c) { coolingSetpoint = eventData?.data?.away_temperature_high_c.toDouble() }
						if(eventData?.data?.eco_temperature_low_c) { heatingSetpoint = eventData?.data?.eco_temperature_low_c.toDouble() }
						else if(eventData?.data?.away_temperature_low_c) { heatingSetpoint = eventData?.data?.away_temperature_low_c.toDouble() }
					}
					temperatureEvent(temp)
					if(hvacMode in ["cool", "heat"]) {
						thermostatSetpointEvent(targetTemp)
					} else {
						sendEvent(name:'thermostatSetpoint', value: "",  descriptionText: "Clear Thermostat Setpoint" , display: false, displayed: true)
					}
					if(hvacMode in ["cool", "auto", "eco"] && state?.can_cool) {
						coolingSetpointEvent(coolingSetpoint)
					} else {
						clearCoolingSetpoint()
					}
					if(hvacMode in ["heat", "auto", "eco"] && state?.can_heat) {
						heatingSetpointEvent(heatingSetpoint)
					} else {
						clearHeatingSetpoint()
					}
					if(eventData?.data?.locked_temp_min_c && eventData?.data?.locked_temp_max_c) { lockedTempEvent(eventData?.data?.locked_temp_min_c, eventData?.data?.locked_temp_max_c) }
					break

				case "F":
					def heatingSetpoint = 0
					def coolingSetpoint = 0
					def temp = eventData?.data?.ambient_temperature_f
					def targetTemp = eventData?.data?.target_temperature_f

					if (hvacMode == "cool") {
						coolingSetpoint = targetTemp
					}
					else if (hvacMode == "heat") {
						heatingSetpoint = targetTemp
					}
					else if (hvacMode == "auto") {
						coolingSetpoint = eventData?.data?.target_temperature_high_f
						heatingSetpoint = eventData?.data?.target_temperature_low_f
					}
					else if (hvacMode == "eco") {
						if(eventData?.data?.eco_temperature_high_f) { coolingSetpoint = eventData?.data?.eco_temperature_high_f }
						else if(eventData?.data?.away_temperature_high_f) { coolingSetpoint = eventData?.data?.away_temperature_high_f }
						if(eventData?.data?.eco_temperature_low_f)  { heatingSetpoint = eventData?.data?.eco_temperature_low_f }
						else if(eventData?.data?.away_temperature_low_f)  { heatingSetpoint = eventData?.data?.away_temperature_low_f }
					}
					temperatureEvent(temp)
					if(hvacMode in ["cool", "heat"]) {
						thermostatSetpointEvent(targetTemp)
					} else {
						sendEvent(name:'thermostatSetpoint', value: "",  descriptionText: "Clear Thermostat Setpoint" , display: false, displayed: true)
					}
					if(hvacMode in ["cool", "auto", "eco"] && state?.can_cool) {
						coolingSetpointEvent(coolingSetpoint)
					} else {
						clearCoolingSetpoint()
					}
					if(hvacMode in ["heat", "auto", "eco"] && state?.can_heat) {
						heatingSetpointEvent(heatingSetpoint)
					} else {
						clearHeatingSetpoint()
					}
					if(eventData?.data?.locked_temp_min_f && eventData?.data?.locked_temp_max_f) { lockedTempEvent(eventData?.data?.locked_temp_min_f, eventData?.data?.locked_temp_max_f) }
					break

				default:
					Logger("no Temperature data $tempUnit")
					break
			}
			getSomeData(true)
			lastUpdatedEvent()
			checkHealth()
		}
		//This will return all of the devices state data to the logs.
		//LogAction("Device State Data: ${getState()}")
		return null
	}
	catch (ex) {
		log.error "generateEvent Exception:", ex
		exceptionDataHandler(ex.message, "generateEvent")
	}
}

def getStateSize()	{ return state?.toString().length() }
def getStateSizePerc()  { return (int) ((stateSize/100000)*100).toDouble().round(0) }

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def getDeviceStateData() {
	return getState()
}

def getTimeZone() {
	def tz = null
	if (location?.timeZone) { tz = location?.timeZone }
	else { tz = state?.nestTimeZone ? TimeZone.getTimeZone(state?.nestTimeZone) : null }
	if(!tz) { Logger("getTimeZone: Hub or Nest TimeZone is not found ...", "warn") }
	return tz
}

def tUnitStr() {
	return "°${state?.tempUnit}"
}

def isCodeUpdateAvailable(newVer, curVer) {
	def result = false
	def latestVer
	def versions = [newVer, curVer]
	if(newVer != curVer) {
		latestVer = versions?.max { a, b ->
			def verA = a?.tokenize('.')
			def verB = b?.tokenize('.')
			def commonIndices = Math.min(verA?.size(), verB?.size())
			for (int i = 0; i < commonIndices; ++i) {
				if (verA[i]?.toInteger() != verB[i]?.toInteger()) {
					return verA[i]?.toInteger() <=> verB[i]?.toInteger()
				}
			}
			verA?.size() <=> verB?.size()
		}
		result = (latestVer == newVer) ? true : false
	}
	LogAction("isCodeUpdateAvailable(): newVer: $newVer | curVer: $curVer | newestVersion: ${latestVer} | result: $result")
	return result
}

def ecoDesc(val) {
	ecoDescEvent(val)
}

def pauseEvent(val) {
	def curData = device.currentState("pauseUpdates")?.value
	if(isStateChange(device, "pauseUpdates", val.toString())) {
		Logger("UPDATED | Pause Updates is: (${val}) | Original State: (${curData})")
		sendEvent(name: 'pauseUpdates', value: val, displayed: false)
	} else { LogAction("Pause Updates is: (${val}) | Original State: (${curData})") }
}

def deviceVerEvent(ver) {
	def curData = device.currentState("devTypeVer")?.value.toString()
	def pubVer = ver ?: null
	def dVer = devVer() ?: null
	state.updateAvailable = isCodeUpdateAvailable(pubVer, dVer)
	def newData = state.updateAvailable ? "${dVer}(New: v${pubVer})" : "${dVer}" as String
	state.devTypeVer = newData
		//log.info "curData: ${curData.getProperties().toString()},  newData: ${newData.getProperties().toString()}"
	if(isStateChange(device, "devVer", dVer.toString())) {
		sendEvent(name: 'devVer', value: dVer, displayed: false)
	}
	if(isStateChange(device, "devTypeVer", newData.toString())) {
		Logger("UPDATED | Device Type Version is: (${newData}) | Original State: (${curData})")
		sendEvent(name: 'devTypeVer', value: newData, displayed: false)
	} else { LogAction("Device Type Version is: (${newData}) | Original State: (${curData})") }
}

def nestTypeEvent(type) {
	def val = device.currentState("nestType")?.value
	state?.nestType=type
	if(!val.equals(type)) {
		Logger("UPDATED | nestType: (${type}) | Original State: (${val})")
		sendEvent(name: 'nestType', value: type, displayed: true)
	} else { LogAction("nestType: (${type}) | Original State: (${val})") }
}

def sunlightCorrectionEnabledEvent(sunEn) {
	def val = device.currentState("sunlightCorrectionEnabled")?.value
	def newVal = sunEn.toString() == "true" ? true : false
	state?.sunCorrectEnabled = newVal
	if(isStateChange(device, "sunlightCorrectionEnabled", newVal.toString())) {
		Logger("UPDATED | SunLight Correction Enabled: (${newVal}) | Original State: (${val.toString().capitalize()})")
		sendEvent(name: 'sunlightCorrectionEnabled', value: newVal, displayed: false)
	} else { LogAction("SunLight Correction Enabled: (${newVal}) | Original State: (${val})") }
}

def sunlightCorrectionActiveEvent(sunAct) {
	def val = device.currentState("sunlightCorrectionActive")?.value
	def newVal = sunAct.toString() == "true" ? true : false
	state?.sunCorrectActive = newVal
	if(isStateChange(device, "sunlightCorrectionActive", newVal.toString())) {
		Logger("UPDATED | SunLight Correction Active: (${newVal}) | Original State: (${val.toString().capitalize()})")
		sendEvent(name: 'sunlightCorrectionActive', value: newVal, displayed: false)
	} else { LogAction("SunLight Correction Active: (${newVal}) | Original State: (${val})") }
}

def timeToTargetEvent(ttt, tttTr) {
	//log.debug "timeToTargetEvent($ttt, $tttTr)"
	def val = device.currentState("timeToTarget")?.stringValue
	def opIdle = device.currentState("thermostatOperatingState").stringValue == "idle" ? true : false
	//log.debug "opIdle: $opIdle"
	def nVal
	if(ttt) {
		nVal = ttt.toString().replaceAll("\\~", "").toString()
		nVal = nVal.toString().replaceAll("\\>", "").toString()
		nVal = nVal.toString().replaceAll("\\<", "").toInteger()
	}
	//log.debug "nVal: $nVal"
	def trStr
	if(tttTr) {
		trStr = tttTr.toString() == "training" ? "\n(Still Training)" : ""
	}
	def newVal = ttt ? (nVal == 0 || opIdle ? "System is Idle" : "${nVal} Minutes${trStr}") : "Not Available"
	if(isStateChange(device, "timeToTarget", newVal.toString())) {
		Logger("UPDATED | Time to Target: (${newVal}) | Original State: (${val.toString().capitalize()})")
		sendEvent(name: 'timeToTarget', value: newVal, displayed: false)
	} else { LogAction("Time to Target: (${newVal}) | Original State: (${val})") }
}

def debugOnEvent(debug) {
	def val = device.currentState("debugOn")?.value
	def dVal = debug ? "On" : "Off"
	state?.debugStatus = dVal
	state?.debug = debug.toBoolean() ? true : false
	if(isStateChange(device, "debugOn", dVal.toString())) {
		Logger("UPDATED | Device Debug Logging is: (${dVal}) | Original State: (${val.toString().capitalize()})")
		sendEvent(name: 'debugOn', value: dVal, displayed: false)
	} else { LogAction("Device Debug Logging is: (${dVal}) | Original State: (${val})") }
}

def lastCheckinEvent(checkin, isOnline) {
	//log.trace("lastCheckinEvent($checkin, $isOnline)")
	def formatVal = state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	tf.setTimeZone(getTimeZone())
	def lastChk = device.currentState("lastConnection")?.value

	def prevOnlineStat = device.currentState("onlineStatus")?.value

	def onlineStat = isOnline.toString() == "true" ? "online" : "offline"

	def hcTimeout = getHcTimeout()
	def lastConn = checkin ? "${tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", checkin))}" : "Not Available"
	def lastConnFmt = checkin ? "${formatDt(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", checkin))}" : "Not Available"
	def lastConnSeconds = checkin ? getTimeDiffSeconds(lastChk) : 3000

	state?.lastConnection = lastConn?.toString()
	if(isStateChange(device, "lastConnection", lastConnFmt.toString())) {
		Logger("UPDATED | Last Nest Check-in was: (${lastConnFmt}) | Previous Check-in: (${lastChk})")
		sendEvent(name: 'lastConnection', value: lastConnFmt?.toString(), displayed: state?.showProtActEvts, isStateChange: true)
		if(hcTimeout && lastConnSeconds >= 0 && isOnline.toString() == "true") { onlineStat = lastConnSeconds < hcTimeout ? "online" : "offline" }
		//Logger("lastConnSeconds: $lastConnSeconds")
	} else { LogAction("Last Nest Check-in was: (${lastConnFmt}) | Original State: (${lastChk})") }

	state?.onlineStatus = onlineStat
	//log.debug "onlineStatus: $onlineStat"
	if(device?.getStatus().toString().toLowerCase() != onlineStat) {
		sendEvent(name: "DeviceWatch-DeviceStatusUpdate", value: onlineStat.toString(), displayed: false)
		sendEvent(name: "DeviceWatch-DeviceStatus", value: onlineStat.toString(), displayed: false)
		Logger("Device Health Status: ${device.getStatus()}")
	}
	if(isStateChange(device, "onlineStatus", onlineStat?.toString())) {
		Logger("UPDATED | Online Status is: (${onlineStat}) | Original State: (${prevOnlineStat})")
		sendEvent(name: "onlineStatus", value: onlineStat, descriptionText: "Online Status is: ${onlineStat}", displayed: state?.showProtActEvts, isStateChange: true, state: onlineStat)
	} else { LogAction("Online Status is: (${onlineStat}) | Original State: (${prevOnlineStat})") }
}

def lastUpdatedEvent(sendEvt=false) {
	def now = new Date()
	def formatVal = state.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
		tf.setTimeZone(getTimeZone())
	def lastDt = "${tf?.format(now)}"
	state?.lastUpdatedDt = lastDt?.toString()
	state?.lastUpdatedDtFmt = formatDt(now)
	if(sendEvt) {
		LogAction("Last Parent Refresh time: (${lastDt}) | Previous Time: (${lastUpd})")
		sendEvent(name: 'lastUpdatedDt', value: formatDt(now)?.toString(), displayed: false, isStateChange: true)
	}
}

def softwareVerEvent(ver) {
	def verVal = device.currentState("softwareVer")?.value
	state?.softwareVer = ver
	if(isStateChange(device, "softwareVer", ver.toString())) {
		Logger("UPDATED | Firmware Version: (${ver}) | Original State: (${verVal})")
		sendEvent(name: 'softwareVer', value: ver, descriptionText: "Firmware Version is now ${ver}", displayed: false, isStateChange: true)
	} else { LogAction("Firmware Version: (${ver}) | Original State: (${verVal})") }
}

def tempUnitEvent(unit) {
	def tmpUnit = device.currentState("temperatureUnit")?.value
	state?.tempUnit = unit
	if(isStateChange(device, "temperatureUnit", unit.toString())) {
		Logger("UPDATED | Temperature Unit: (${unit}) | Original State: (${tmpUnit})")
		sendEvent(name:'temperatureUnit', value: unit, descriptionText: "Temperature Unit is now: '${unit}'", displayed: true, isStateChange: true)
	} else { LogAction("Temperature Unit: (${unit}) | Original State: (${tmpUnit})") }
}

// TODO NOT USED
def targetTempEvent(Double targetTemp) {
	def temp = device.currentState("targetTemperature")?.value.toString()
	def rTargetTemp = wantMetric() ? targetTemp.round(1) : targetTemp.round(0).toInteger()
	if(isStateChange(device, "targetTemperature", rTargetTemp.toString())) {
		Logger("UPDATED | targetTemperature is (${rTargetTemp}${tUnitStr()}) | Original Temp: (${temp}${tUnitStr()})")
		sendEvent(name:'targetTemperature', value: rTargetTemp, unit: state?.tempUnit, descriptionText: "Target Temperature is ${rTargetTemp}${tUnitStr()}", displayed: false, isStateChange: true)
	} else { LogAction("targetTemperature is (${rTargetTemp}${tUnitStr()}) | Original Temp: (${temp}${tUnitStr()})") }
}

def thermostatSetpointEvent(Double targetTemp) {
	def temp = device.currentState("thermostatSetpoint")?.value.toString()
	def rTargetTemp = wantMetric() ? targetTemp.round(1) : targetTemp.round(0).toInteger()
	//if(isStateChange(device, "thermostatSetPoint", rTargetTemp.toString())) {
	if(!temp.equals(rTargetTemp.toString())) {
		Logger("UPDATED | thermostatSetPoint Temperature is (${rTargetTemp}${tUnitStr()}) | Original Temp: (${temp}${tUnitStr()})")
		sendEvent(name:'thermostatSetpoint', value: rTargetTemp, unit: state?.tempUnit, descriptionText: "thermostatSetpoint Temperature is ${rTargetTemp}${tUnitStr()}", displayed: false, isStateChange: true)
	} else { LogAction("thermostatSetpoint is (${rTargetTemp}${tUnitStr()}) | Original Temp: (${temp}${tUnitStr()})") }
}

def temperatureEvent(Double tempVal) {
	def temp = device.currentState("temperature")?.value.toString()
	def rTempVal = wantMetric() ? tempVal.round(1) : tempVal.round(0).toInteger()
	if(isStateChange(device, "temperature", rTempVal.toString())) {
		Logger("UPDATED | Temperature is (${rTempVal}${tUnitStr()}) | Original Temp: (${temp}${tUnitStr()})")
		sendEvent(name:'temperature', value: rTempVal, unit: state?.tempUnit, descriptionText: "Ambient Temperature is ${rTempVal}${tUnitStr()}" , displayed: true, isStateChange: true)
	} else { LogAction("Temperature is (${rTempVal}${tUnitStr()}) | Original Temp: (${temp})${tUnitStr()}") }
	checkSafetyTemps()
}

def heatingSetpointEvent(Double tempVal) {
	def temp = device.currentState("heatingSetpoint")?.value.toString()
	if(tempVal.toInteger() == 0 || !state?.can_heat || (getHvacMode == "off")) {
		if(temp != "") { clearHeatingSetpoint() }
	} else {
		def rTempVal = wantMetric() ? tempVal.round(1) : tempVal.round(0).toInteger()
		if(isStateChange(device, "heatingSetpoint", rTempVal.toString())) {
			Logger("UPDATED | Heat Setpoint is (${rTempVal}${tUnitStr()}) | Original Temp: (${temp}${tUnitStr()})")
			def disp = false
			def hvacMode = getHvacMode()
			if (hvacMode in ["auto", "heat"]) { disp = true }
			sendEvent(name:'heatingSetpoint', value: rTempVal, unit: state?.tempUnit, descriptionText: "Heat Setpoint is ${rTempVal}${tUnitStr()}" , displayed: disp, isStateChange: true, state: "heat")
			state?.allowHeat = true
		} else { LogAction("Heat Setpoint is (${rTempVal}${tUnitStr()}) | Original Temp: (${temp}${tUnitStr()})") }
	}
}

def coolingSetpointEvent(Double tempVal) {
	def temp = device.currentState("coolingSetpoint")?.value.toString()
	if(tempVal.toInteger() == 0 || !state?.can_cool || (getHvacMode == "off")) {
		if(temp != "") { clearCoolingSetpoint() }
	} else {
		def rTempVal = wantMetric() ? tempVal.round(1) : tempVal.round(0).toInteger()
		if(isStateChange(device, "coolingSetpoint", rTempVal.toString())) {
			Logger("UPDATED | Cool Setpoint is (${rTempVal}${tUnitStr()}) | Original Temp: (${temp}${tUnitStr()})")
			def disp = false
			def hvacMode = getHvacMode()
			if (hvacMode in ["auto", "cool"]) { disp = true }
			sendEvent(name:'coolingSetpoint', value: rTempVal, unit: state?.tempUnit, descriptionText: "Cool Setpoint is ${rTempVal}${tUnitStr()}" , displayed: disp, isStateChange: true, state: "cool")
			state?.allowCool = true
		} else { LogAction("Cool Setpoint is (${rTempVal}${tUnitStr()}) | Original Temp: (${temp}${tUnitStr()})") }
	}
}

def hasLeafEvent(Boolean hasLeaf) {
	def leaf = device.currentState("hasLeaf")?.value
	def lf = hasLeaf ? "On" : "Off"
	state?.hasLeaf = hasLeaf
	if(isStateChange(device, "hasLeaf", lf.toString())) {
		Logger("UPDATED | Leaf is set to (${lf}) | Original State: (${leaf})")
		sendEvent(name:'hasLeaf', value: lf,  descriptionText: "Leaf: ${lf}" , displayed: false, isStateChange: true, state: lf)
	} else { LogAction("Leaf is set to (${lf}) | Original State: (${leaf})") }
}

def humidityEvent(humidity) {
	def hum = device.currentState("humidity")?.value
	if(isStateChange(device, "humidity", humidity.toString())) {
		Logger("UPDATED | Humidity is (${humidity}) | Original State: (${hum})")
		sendEvent(name:'humidity', value: humidity, unit: "%", descriptionText: "Humidity is ${humidity}" , displayed: false, isStateChange: true)
	} else { LogAction("Humidity is (${humidity}) | Original State: (${hum})") }
}

def presenceEvent(presence) {
	def val = getPresence()
	def pres = (presence == "home") ? "present" : "not present"
	def nestPres = state?.nestPresence
	def newNestPres = (presence == "home") ? "home" : ((presence == "auto-away") ? "auto-away" : "away")
	def statePres = state?.present
	state?.present = (pres == "present") ? true : false
	state?.nestPresence = newNestPres
	if(!val.equals(pres) || !nestPres.equals(newNestPres) || !nestPres) {
		Logger("UPDATED | Presence: ${pres.toString().capitalize()} | Original State: ${val.toString().capitalize()} | State Variable: ${statePres}")
		sendEvent(name: 'presence', value: pres, descriptionText: "Device is: ${pres}", displayed: false, isStateChange: true, state: pres )
		sendEvent(name: 'nestPresence', value: newNestPres, descriptionText: "Nest Presence is: ${newNestPres}", displayed: true, isStateChange: true )
	} else { LogAction("Presence - Present: (${pres}) | Original State: (${val}) | State Variable: ${state?.present}") }
}

def whoMadeChanges(autoType, desc, dt) {
	// log.debug "whoMadeChanges: $autoType: $desc | dt: $dt"
	def curType = device?.currentState("whoMadeChanges")?.value
	def curDesc = device?.currentState("whoMadeChangesDesc")?.value
	def curDt = device?.currentState("whoMadeChangesDescDt")?.value
	def newChgType = autoType ?: "Not Set"
	def newChgDesc = desc ?: "Not Set"
	def formatVal = state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	tf.setTimeZone(getTimeZone())
	def newChgDt = tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", dt)) ?: "Not Set"
	if(isStateChange(device, "whoMadeChanges", newChgType.toString()) || isStateChange(device, "whoMadeChangesDesc", newChgDesc.toString()) || isStateChange(device, "whoMadeChangesDescDt", newChgDt.toString())) {
		Logger("UPDATED | Device Changes Made by (${newChgType}: ${newChgDesc}) at (${newChgDt})")
		sendEvent(name: "whoMadeChanges", value: newChgType)
		sendEvent(name: "whoMadeChangesDesc", value: newChgDesc)
		sendEvent(name: "whoMadeChangesDescDt", value: newChgDt)
	} else { LogAction("Device Changes Made by (${newChgType}: ${newChgDesc}) at (${newChgDt})") }
}

def ecoDescEvent(val, updChk=false) {
	//log.debug "ecoDescEvent($val)"
	def curMode = device?.currentState("nestThermostatMode")?.value.toString()
	def curEcoDesc = device?.currentState("whoSetEcoMode")?.value ?: null

	def newVal = updChk ? curEcoDesc : val
	def newEcoDesc = (curMode == "eco") ? (newVal == null ? "Set Outside of this DTH" : newVal) : "Not in Eco Mode"

	//log.debug "cur: $curEcoDesc | new: $newEcoDesc | curMode: $curMode | val: $val"
	if(isStateChange(device, "whoSetEcoMode", newEcoDesc.toString())) {
		Logger("UPDATED | whoSetEcoMode is (${newEcoDesc}) | Original State: (${curEcoDesc})")
		sendEvent(name: "whoSetEcoMode", value: newEcoDesc)
		def formatVal = state?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
		def tf = new SimpleDateFormat(formatVal)
		tf.setTimeZone(getTimeZone())
		state?.ecoDescDt = (newEcoDesc in ["Set Outside of this DTH", "Not in Eco Mode"]) ? null : tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", getDtNow()))
	} else { LogAction("whoSetEcoMode is (${newEcoDesc}) | Original State: (${curEcoDesc})") }
}

def hvacModeEvent(mode) {
	def hvacMode = !state?.hvac_mode ? device.currentState("thermostatMode")?.value.toString() : state.hvac_mode
	def newMode = (mode == "heat-cool") ? "auto" : mode
	if(mode == "eco") {
		if(state?.can_cool && state?.can_heat) { newMode = "auto" }
		else if(state?.can_heat) { newMode = "heat" }
		else if(state?.can_cool) { newMode = "cool" }
	}
	state?.hvac_mode = newMode
	if(!hvacMode.equals(newMode)) {
		Logger("UPDATED | Hvac Mode is (${newMode.toString().capitalize()}) | Original State: (${hvacMode.toString().capitalize()})")
		sendEvent(name: "thermostatMode", value: newMode, descriptionText: "HVAC mode is ${newMode} mode", displayed: true, isStateChange: true)
	}

	def oldnestmode = state?.nestHvac_mode
	newMode = (mode == "heat-cool") ? "auto" : mode
	state?.nestHvac_mode = newMode
	if(!oldnestmode.equals(newMode)) {
		Logger("UPDATED | NEST Hvac Mode is (${newMode.toString().capitalize()}) | Original State: (${oldnestmode.toString().capitalize()})")
		sendEvent(name: "nestThermostatMode", value: newMode, descriptionText: "Nest HVAC mode is ${newMode} mode", displayed: true, isStateChange: true)
	} else { LogAction("NEST Hvac Mode is (${newMode}) | Original State: (${oldnestmode})") }
}

def hvacPreviousModeEvent(mode) {
	def hvacMode = !state?.previous_hvac_mode ? device.currentState("previousthermostatMode")?.value.toString() : state.previous_hvac_mode
	def newMode = (mode == "heat-cool") ? "auto" : mode
	state?.previous_hvac_mode = newMode
	if(!hvacMode.equals(newMode)) {
		Logger("UPDATED | Hvac Previous Mode is (${newMode.toString().capitalize()}) | Original State: (${hvacMode.toString().capitalize()})")
		sendEvent(name: "previousthermostatMode", value: newMode, descriptionText: "HVAC Previous mode is ${newMode} mode", displayed: true, isStateChange: true)
	} else { LogAction("Hvac Previous Mode is (${newMode}) | Original State: (${hvacMode})") }
}

def fanModeEvent(fanActive) {
	def val = state?.has_fan ? ((fanActive == "true") ? "on" : "auto") : "disabled"
	def fanMode = device.currentState("thermostatFanMode")?.value
	if(isStateChange(device, "thermostatFanMode", val.toString())) {
		Logger("UPDATED | Fan Mode: (${val.toString().capitalize()}) | Original State: (${fanMode.toString().capitalize()})")
		sendEvent(name: "thermostatFanMode", value: val, descriptionText: "Fan Mode is: ${val}", displayed: true, isStateChange: true, state: val)
	} else { LogAction("Fan Active: (${val}) | Original State: (${fanMode})") }
}

def operatingStateEvent(operatingState) {
	def hvacState = device.currentState("thermostatOperatingState")?.value
	def operState = (operatingState == "off") ? "idle" : operatingState
	if(isStateChange(device, "thermostatOperatingState", operState.toString())) {
		Logger("UPDATED | OperatingState is (${operState.toString().capitalize()}) | Original State: (${hvacState.toString().capitalize()})")
		sendEvent(name: 'thermostatOperatingState', value: operState, descriptionText: "Device is ${operState}", displayed: true, isStateChange: true)
	} else { LogAction("OperatingState is (${operState}) | Original State: (${hvacState})") }
}

def tempLockOnEvent(isLocked) {
	def curState = device.currentState("tempLockOn")?.value.toString()
	def newState = isLocked?.toString()
	state?.tempLockOn = newState
	if(isStateChange(device, "tempLockOn", newState.toString())) {
		Logger("UPDATED | Temperature Lock is set to (${newState}) | Original State: (${curState})")
		sendEvent(name:'tempLockOn', value: newState,  descriptionText: "Temperature Lock: ${newState}" , displayed: false, isStateChange: true, state: newState)
	} else { LogAction("Temperature Lock is set to (${newState}) | Original State: (${curState})") }
}

def lockedTempEvent(Double minTemp, Double maxTemp) {
	def curMinTemp = device.currentState("lockedTempMin")?.doubleValue
	def curMaxTemp = device.currentState("lockedTempMax")?.doubleValue
	//def rTempVal = wantMetric() ? tempVal.round(1) : tempVal.round(0).toInteger()
	if(curMinTemp != minTemp || curMaxTemp != maxTemp) {
		Logger("UPDATED | Temperature Lock Minimum is (${minTemp}) | Original Temp: (${curMinTemp})")
		Logger("UPDATED | Temperature Lock Maximum is (${maxTemp}) | Original Temp: (${curMaxTemp})")
		sendEvent(name:'lockedTempMin', value: minTemp, unit: state?.tempUnit, descriptionText: "Temperature Lock Minimum is ${minTemp}${state?.tempUnit}" , displayed: true, isStateChange: true)
		sendEvent(name:'lockedTempMax', value: maxTemp, unit: state?.tempUnit, descriptionText: "Temperature Lock Maximum is ${maxTemp}${state?.tempUnit}" , displayed: true, isStateChange: true)
	} else {
		LogAction("Temperature Lock Minimum is (${minTemp}${state?.tempUnit}) | Original Minimum Temp: (${curMinTemp}${state?.tempUnit})")
		LogAction("Temperature Lock Maximum is (${maxTemp}${state?.tempUnit}) | Original Maximum Temp: (${curMaxTemp}${state?.tempUnit})")
	}
}

def safetyTempsEvent(safetyTemps) {
	def curMinTemp = device.currentState("safetyTempMin")?.doubleValue
	def curMaxTemp = device.currentState("safetyTempMax")?.doubleValue
	def newMinTemp = safetyTemps && safetyTemps?.min ? safetyTemps?.min.toDouble() : 0
	def newMaxTemp = safetyTemps && safetyTemps?.max ? safetyTemps?.max.toDouble() : 0

	//def rTempVal = wantMetric() ? tempVal.round(1) : tempVal.round(0).toInteger()
	if(curMinTemp != newMinTemp || curMaxTemp != newMaxTemp) {
		Logger("UPDATED | Safety Temperature Minimum is (${newMinTemp}${state?.tempUnit}) | Original Temp: (${curMinTemp}${state?.tempUnit})")
		Logger("UPDATED | Safety Temperature Maximum is (${newMaxTemp}${state?.tempUnit}) | Original Temp: (${curMaxTemp}${state?.tempUnit})")
		sendEvent(name:'safetyTempMin', value: newMinTemp, unit: state?.tempUnit, descriptionText: "Safety Temperature Minimum is ${newMinTemp}${state?.tempUnit}" , displayed: true, isStateChange: true)
		sendEvent(name:'safetyTempMax', value: newMaxTemp, unit: state?.tempUnit, descriptionText: "Safety Temperature Maximum is ${newMaxTemp}${state?.tempUnit}" , displayed: true, isStateChange: true)
		checkSafetyTemps()
	} else {
		LogAction("Safety Temperature Minimum is (${newMinTemp}${state?.tempUnit}) | Original Minimum Temp: (${curMinTemp}${state?.tempUnit})")
		LogAction("Safety Temperature Maximum is (${newMaxTemp}${state?.tempUnit}) | Original Maximum Temp: (${curMaxTemp}${state?.tempUnit})")
	}
}

def checkSafetyTemps() {
	def curMinTemp = device.currentState("safetyTempMin")?.doubleValue
	def curMaxTemp = device.currentState("safetyTempMax")?.doubleValue
	def curTemp = device.currentState("temperature")?.doubleValue
	def curRangeStr = device.currentState("safetyTempExceeded")?.toString()
	def curInRange = !curRangeStr?.toBoolean()
	def inRange = true
	if(curMinTemp && curMinTemp > curTemp) { inRange = false }
	if(curMaxTemp && curMaxTemp < curTemp) { inRange = false }
	def t0 = !(inRange.toBoolean())
	LogAction("checkSafetyTemps: (curMinTemp: ${curMinTemp} | curMaxTemp: ${curMaxTemp} | curTemp: ${curTemp} | exceeded: ${t0.toBoolean()} | curInRange: ${curInRange} | inRange: ${inRange})")
	if(curRangeStr == null || inRange != curInRange) {
		sendEvent(name:'safetyTempExceeded', value: t0.toBoolean(),  descriptionText: "Safety Temperature ${inRange ? "OK" : "Exceeded"} ${curTemp}${state?.tempUnit}", displayed: true, isStateChange: true)
		Logger("UPDATED | Safety Temperature Exceeded is (${t0.toBoolean()}) | Current Temp: (${curTemp}${state?.tempUnit})")
	} else {
		LogAction("Safety Temperature Exceeded is (${t0.toBoolean()}) | Current Temp: (${curTemp}${state?.tempUnit})")
	}
}

def comfortHumidityEvent(comfortHum) {
	//def curMinHum = device.currentState("comfortHumidityMin")?.integerValue
	def curMaxHum = device.currentState("comfortHumidityMax")?.integerValue
	//def newMinHum = comfortHum?.min.toInteger() ?: 0
	def newMaxHum = comfortHum ? comfortHum?.toInteger() : 0
	//if(isStateChange(device, "comfortHumidityMin", newMinHum.toString()) || isStateChange(device, "comfortHumidityMax", newMaxHum.toString())) {
	if(isStateChange(device, "comfortHumidityMax", newMaxHum.toString())) {
		//LogAction("UPDATED | Comfort Humidity Minimum is (${newMinHum}) | Original Temp: (${curMinHum})")
		Logger("UPDATED | Comfort Humidity Maximum is (${newMaxHum}%) | Original Humidity: (${curMaxHum}%)")
		sendEvent(name:'comfortHumidityMax', value: newMaxHum, unit: "%", descriptionText: "Safety Humidity Maximum is ${newMaxHum}%" , displayed: true, isStateChange: true)
	} else {
		//LogAction("Comfort Humidity Minimum is (${newMinHum}) | Original Minimum Humidity: (${curMinHum})")
		LogAction("Comfort Humidity Maximum is (${newMaxHum}%) | Original Maximum Humidity: (${curMaxHum}%)")
	}
}

def comfortDewpointEvent(comfortDew) {
	//def curMinDew = device.currentState("comfortDewpointMin")?.integerValue
	def curMaxDew = device.currentState("comfortDewpointMax")?.doubleValue
	//def newMinDew = comfortDew?.min.toInteger() ?: 0
	def newMaxDew = comfortDew ? comfortDew?.toDouble() : 0.0
	//if(isStateChange(device, "comfortDewpointMax", newMaxDew.toString()) || isStateChange(device, "comfortDewpointMin", newMinDew.toString())) {
	if(isStateChange(device, "comfortDewpointMax", newMaxDew.toString())) {
		//LogAction("UPDATED | Comfort Dewpoint Minimum is (${newMinDew}) | Original Temp: (${curMinDew})")
		Logger("UPDATED | Comfort Dewpoint Maximum is (${newMaxDew}) | Original Dewpoint: (${curMaxDew})")
		//sendEvent(name:'comfortDewpointMin', value: newMinDew, unit: "%", descriptionText: "Comfort Dewpoint Minimum is ${newMinDew}" , displayed: true, isStateChange: true)
		sendEvent(name:'comfortDewpointMax', value: newMaxDew, unit: state?.tempUnit, descriptionText: "Comfort Dewpoint Maximum is ${newMaxDew}" , displayed: true, isStateChange: true)
	} else {
		//LogAction("Comfort Dewpoint is (${newMinDew}) | Original Minimum Dewpoint: (${curMinDew})")
		LogAction("Comfort Dewpoint Maximum is (${newMaxDew}) | Original Maximum Dewpoint: (${curMaxDew})")
	}
}

def onlineStatusEvent(online) {
	def isOn = device.currentState("onlineStatus")?.value
	def val = online ? "Online" : "Offline"
	state?.onlineStatus = val
	if(isStateChange(device, "onlineStatus", val.toString())) {
		Logger("UPDATED | Online Status is: (${val}) | Original State: (${isOn})")
		sendEvent(name: "onlineStatus", value: val, descriptionText: "Online Status is: ${val}", displayed: true, isStateChange: true, state: val)
		sendEvent(name: "DeviceWatch-DeviceStatus", value: (val == "Online" ? "online" : "offline"), displayed: false)
	} else { LogAction("Online Status is: (${val}) | Original State: (${isOn})") }
}

def apiStatusEvent(issue) {
	def curStat = device.currentState("apiStatus")?.value
	def newStat = issue ? "Has Issue" : "Good"
	state?.apiStatus = newStat
	if(isStateChange(device, "apiStatus", newStat.toString())) {
		Logger("UPDATED | API Status is: (${newStat.toString().capitalize()}) | Original State: (${curStat.toString().capitalize()})")
		sendEvent(name: "apiStatus", value: newStat, descriptionText: "API Status is: ${newStat}", displayed: true, isStateChange: true, state: newStat)
	} else { LogAction("API Status is: (${newStat}) | Original State: (${curStat})") }
}

def nestReportStatusEvent() {
	def rprtData = getNestMgrReport()?.toString()
	if(rprtData && isStateChange(device, "nestReportData", rprtData.toString())) {
		Logger("UPDATED | Nest Voice Report Data has been Updated", "info")
		sendEvent(name: 'nestReportData', value: rprtData, descriptionText: "Nest Voice Report Data has been updated...", display: false, displayed: false)
	}
}

def keepAwakeEvent() {
	def lastDt = state?.lastUpdatedDtFmt
	if(lastDt) {
		def ldtSec = getTimeDiffSeconds(lastDt)
		log.debug "ldtSec: $ldtSec"
		if(ldtSec < 1900) {
			lastUpdatedEvent(true)
		} else { refresh() }
	} else { refresh() }
}

def autoSchedDataEvent(schedData) {
	def s0 = [:]
	s0 = schedData != null ? schedData : s0
	def t0 = state?.curAutoSchedData
	//log.debug "s0: $s0 | t0: $t0"
	if(s0 && t0 != s0) {
		Logger("UPDATED | Automation Schedule Data for this device has been Updated", "info")
	}
	state?.curAutoSchedData = schedData
}

def canHeatCool(canHeat, canCool) {
	state?.can_heat = !canHeat ? false : true
	state?.can_cool = !canCool ? false : true
	state?.has_auto = (canCool && canHeat) ? true : false
	if(isStateChange(device, "canHeat", state?.can_heat.toString())) {
		sendEvent(name: "canHeat", value: state?.can_heat.toString())
	}
	if(isStateChange(device, "canCool", state?.can_cool.toString())) {
		sendEvent(name: "canCool", value: state?.can_cool.toString())
	}
	if(isStateChange(device, "hasAuto", state?.has_auto.toString())) {
		sendEvent(name: "hasAuto", value: state?.has_auto.toString())
	}
}

def hasFan(hasFan) {
	state?.has_fan = (hasFan == "true") ? true : false
	if(isStateChange(device, "hasFan", hasFan.toString())) {
		sendEvent(name: "hasFan", value: hasFan.toString())
	}
}

def isEmergencyHeat(val) {
	state?.is_using_emergency_heat = !val ? false : true
}

def clearHeatingSetpoint() {
	sendEvent(name:'heatingSetpoint', value: "",  descriptionText: "Clear Heating Setpoint" , display: false, displayed: true )
	state?.allowHeat = false
}

def clearCoolingSetpoint() {
	sendEvent(name:'coolingSetpoint', value: "",  descriptionText: "Clear Cooling Setpoint" , display: false, displayed: true)
	state?.allowCool = false
}

def getCoolTemp() {
	return !device.currentValue("coolingSetpoint") ? 0 : device.currentValue("coolingSetpoint")
}

def getHeatTemp() {
	return !device.currentValue("heatingSetpoint") ? 0 : device.currentValue("heatingSetpoint")
}

def getFanMode() {
	return !device.currentState("thermostatFanMode")?.value ? "unknown" : device.currentState("thermostatFanMode")?.value.toString()
}

def getHvacMode() {
	return !state?.nestHvac_mode ? device.currentState("nestThermostatMode")?.value.toString() : state.nestHvac_mode
	//return !device.currentState("thermostatMode") ? "unknown" : device.currentState("thermostatMode")?.value.toString()
}

def getHvacState() {
	return !device.currentState("thermostatOperatingState") ? "unknown" : device.currentState("thermostatOperatingState")?.value.toString()
}

def getNestPresence() {
	return !state?.nestPresence ? device.currentState("nestPresence")?.value.toString() : state.nestPresence
	//return !device.currentState("nestPresence") ? "home" : device.currentState("nestPresence")?.value.toString()
}

def getPresence() {
	return !device.currentState("presence") ? "present" : device.currentState("presence").value.toString()
}

def getTargetTemp() {
	return !device.currentValue("targetTemperature") ? 0 : device.currentValue("targetTemperature")
}

def getThermostatSetpoint() {
	return !device.currentValue("thermostatSetpoint") ? 0 : device.currentValue("thermostatSetpoint")
}

def getTemp() {
	return !device.currentValue("temperature") ? 0 : device.currentValue("temperature")
}

def getHumidity() {
	return !device.currentValue("humidity") ? 0 : device.currentValue("humidity")
}

def getTempWaitVal() {
	return state?.childWaitVal ? state?.childWaitVal.toInteger() : 4
}

def wantMetric() { return (state?.tempUnit == "C") }

def getHealthStatus() {
	return device?.getStatus()
}

def checkHealth() {
	def isOnline = (getHealthStatus() == "ONLINE") ? true : false
	if(isOnline || state?.healthMsg != true) { return }
	parent?.deviceHealthNotify(this, isOnline)
}

/************************************************************************************************
|							Temperature Setpoint Functions for Buttons							|
*************************************************************************************************/
void heatingSetpointUp() {
	//LogAction("heatingSetpointUp()...", "trace")
	def operMode = getHvacMode()
	if ( operMode in ["heat", "eco", "auto"] ) {
		levelUpDown(1,"heat")
	}
}

void heatingSetpointDown() {
	//LogAction("heatingSetpointDown()...", "trace")
	def operMode = getHvacMode()
	if ( operMode in ["heat","eco", "auto"] ) {
		levelUpDown(-1, "heat")
	}
}

void coolingSetpointUp() {
	//LogAction("coolingSetpointUp()...", "trace")
	def operMode = getHvacMode()
	if ( operMode in ["cool","eco", "auto"] ) {
		levelUpDown(1, "cool")
	}
}

void coolingSetpointDown() {
	//LogAction("coolingSetpointDown()...", "trace")
	def operMode = getHvacMode()
	if ( operMode in ["cool", "eco", "auto"] ) {
		levelUpDown(-1, "cool")
	}
}

void levelUp() {
	levelUpDown(1)
}

void levelDown() {
	levelUpDown(-1)
}

void levelUpDown(tempVal, chgType = null) {
	//LogAction("levelUpDown()...($tempVal | $chgType)", "trace")
	def hvacMode = getHvacMode()

	if (canChangeTemp()) {
	// From RBOY https://community.smartthings.com/t/multiattributetile-value-control/41651/23
	// Determine OS intended behaviors based on value behaviors (urrgghhh.....ST!)
		def upLevel

		if (!state?.lastLevelUpDown) { state.lastLevelUpDown = 0 } // If it isn't defined lets baseline it

		if ((state.lastLevelUpDown == 1) && (tempVal == 1)) { upLevel = true } //Last time it was 1 and again it's 1 its increase

		else if ((state.lastLevelUpDown == 0) && (tempVal == 0)) { upLevel = false } //Last time it was 0 and again it's 0 then it's decrease

		else if ((state.lastLevelUpDown == -1) && (tempVal == -1)) { upLevel = false } //Last time it was -1 and again it's -1 then it's decrease

		else if ((tempVal - state.lastLevelUpDown) > 0) { upLevel = true } //If it's increasing then it's up

		else if ((tempVal - state.lastLevelUpDown) < 0) { upLevel = false } //If it's decreasing then it's down

		else { log.error "UNDEFINED STATE, CONTACT DEVELOPER. Last level $state.lastLevelUpDown, Current level, $value" }

		state.lastLevelUpDown = tempVal // Save it

		def targetVal = 0.0
		def tempUnit = device.currentValue('temperatureUnit')
		def curHeatpoint = device.currentValue("heatingSetpoint")
		def curCoolpoint = device.currentValue("coolingSetpoint")
		def curThermSetpoint = device.latestValue("thermostatSetpoint")
		targetVal = curThermSetpoint ?: 0.0
		if (hvacMode == "auto") {
			if (chgType == "cool") {
				targetVal = curCoolpoint
				curThermSetpoint = targetVal
			}
			if (chgType == "heat") {
				targetVal = curHeatpoint
				curThermSetpoint = targetVal
			}
		}
		def locked = state?.tempLockOn.toBoolean()
		def curMinTemp
		def curMaxTemp = 100.0

		if(locked) {
			curMinTemp = device.currentState("lockedTempMin")?.doubleValue
			curMaxTemp = device.currentState("lockedTempMax")?.doubleValue
		}
		if (tempUnit == "C" ) {
			if(curMinTemp < 9.0) { curMinTemp = 9.0 }
			if(curMaxTemp > 32.0) { curMaxTemp = 32.0 }
		} else {
			if(curMinTemp < 50) { curMinTemp = 50 }
			if(curMaxTemp > 90) { curMaxTemp = 90 }
		}
		if (upLevel) {
			//LogAction("Increasing by 1 increment")
			if (tempUnit == "C" ) {
				targetVal = targetVal.toDouble() + 0.5
				if(targetVal < curMinTemp) { targetVal = curMinTemp }
				if(targetVal > curMaxTemp) { targetVal = curMaxTemp }
			} else {
				targetVal = targetVal.toDouble() + 1.0
				if(targetVal < curMinTemp) { targetVal = curMinTemp }
				if(targetVal > curMaxTemp) { targetVal = curMaxTemp }
			}
		} else {
			//LogAction("Reducing by 1 increment")
			if (tempUnit == "C" ) {
				targetVal = targetVal.toDouble() - 0.5
				if(targetVal < curMinTemp) { targetVal = curMinTemp }
				if(targetVal > curMaxTemp) { targetVal = curMaxTemp }
			} else {
				targetVal = targetVal.toDouble() - 1.0
				if(targetVal < curMinTemp) { targetVal = curMinTemp }
				if(targetVal > curMaxTemp) { targetVal = curMaxTemp }
			}
		}

		if (targetVal != curThermSetpoint ) {
			pauseEvent("true")
			switch (hvacMode) {
				case "heat":
					if (state?.oldHeat == null) { state.oldHeat = curHeatpoint}
					thermostatSetpointEvent(targetVal)
					heatingSetpointEvent(targetVal)
					if (!chgType) { chgType = "" }
					scheduleChangeSetpoint()
					Logger("Sending changeSetpoint(Temp: ${targetVal})")
					break
				case "cool":
					if (state?.oldCool == null) { state.oldCool = curCoolpoint}
					thermostatSetpointEvent(targetVal)
					coolingSetpointEvent(targetVal)
					if (!chgType) { chgType = "" }
					scheduleChangeSetpoint()
					Logger("Sending changeSetpoint(Temp: ${targetVal})")
					break
				case "auto":
					if (chgType) {
						switch (chgType) {
							case "cool":
								if (state?.oldCool == null) { state.oldCool = curCoolpoint}
								coolingSetpointEvent(targetVal)
								scheduleChangeSetpoint()
								Logger("Sending changeSetpoint(Temp: ${targetVal})")
								break
							case "heat":
								if (state?.oldHeat == null) { state.oldHeat = curHeatpoint}
								heatingSetpointEvent(targetVal)
								scheduleChangeSetpoint()
								Logger("Sending changeSetpoint(Temp: ${targetVal})")
								break
							default:
								Logger("Can not change temp while in this mode ($chgType}!!!", "warn")
								break
						}
					} else { Logger("Temp Change without a chgType is not supported!!!", "warn") }
					break
				default:
					pauseEvent("false")
					Logger("Unsupported Mode Received: ($hvacMode}!!!", "warn")
					break
			}
		}
	} else { Logger("levelUpDown: Cannot adjust temperature due to hvacMode ${hvacMode}") }
}

def scheduleChangeSetpoint() {
	if (getLastChangeSetpointSec() > 7) {
		state?.lastChangeSetpointDt = getDtNow()
		runIn( 11, "changeSetpoint", [overwrite: true] )
	}
}

def getLastChangeSetpointSec() { return !state?.lastChangeSetpointDt ? 100000 : GetTimeDiffSeconds(state?.lastChangeSetpointDt).toInteger() }

def getDtNow() {
	def now = new Date()
	return formatDt(now)
}

def formatDt(dt) {
	def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	else {
		Logger("SmartThings TimeZone is not found or is not set... Please Try to open your ST location and Press Save...", "warn")
	}
	return tf.format(dt)
}

//Returns time differences is seconds
def GetTimeDiffSeconds(lastDate) {
	if(lastDate?.contains("dtNow")) { return 10000 }
	def now = new Date()
	def lastDt = Date.parse("E MMM dd HH:mm:ss z yyyy", lastDate)
	def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(lastDt)).getTime()
	def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(now)).getTime()
	def diff = (int) (long) (stop - start) / 1000
	return diff
}

def getTimeDiffSeconds(strtDate, stpDate=null, methName=null) {
	//LogTrace("[GetTimeDiffSeconds] StartDate: $strtDate | StopDate: ${stpDate ?: "Not Sent"} | MethodName: ${methName ?: "Not Sent"})")
	if(strtDate) {
		//if(strtDate?.contains("dtNow")) { return 10000 }
		def now = new Date()
		def stopVal = stpDate ? stpDate.toString() : formatDt(now)
		def startDt = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate)
		def stopDt = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal)
		def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(startDt)).getTime()
		def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal).getTime()
		def diff = (int) (long) (stop - start) / 1000
		//LogTrace("[GetTimeDiffSeconds] Results for '$methName': ($diff seconds)")
		return diff
	} else { return null }
}
// Nest does not allow temp changes in away modes
def canChangeTemp() {
	//LogAction("canChangeTemp()...", "trace")
	def curPres = getNestPresence()
	//if (curPres == "home" && state?.nestHvac_mode != "eco") {
	if(state?.nestHvac_mode != "eco") {
		def hvacMode = getHvacMode()
		switch (hvacMode) {
			case "heat":
				return true
				break
			case "cool":
				return true
				break
			case "auto":
				return true
				break
			default:
				return false
				break
		}
	} else { return false }
}

void changeSetpoint() {
	//LogAction("changeSetpoint()... ($val)", "trace")
	if ( canChangeTemp() ) {
		def md
		def hvacMode = getHvacMode()
		def curHeatpoint = getHeatTemp()
		def curCoolpoint = getCoolTemp()

		LogAction("changeSetpoint()... hvacMode: ${hvacMode} curHeatpoint: ${curHeatpoint}  curCoolpoint: ${curCoolpoint} oldCool: ${state?.oldCool} oldHeat: ${state?.oldHeat}", "trace")

		switch (hvacMode) {
			case "heat":
				state.oldHeat = null
				setHeatingSetpoint(curHeatpoint,true)
				break
			case "cool":
				state.oldCool = null
				setCoolingSetpoint(curCoolpoint,true)
				break
			case "auto":
				if ( (state?.oldCool != null) && (state?.oldHeat == null) ) { md = "cool"}
				if ( (state?.oldCool == null) && (state?.oldHeat != null) ) { md = "heat"}
				if ( (state?.oldCool != null) && (state?.oldHeat != null) ) { md = "both"}

				def heatFirst
				if(md) {
					if (curHeatpoint >= curCoolpoint) {
						Logger("changeSetpoint: Invalid Temp Type received in auto mode... ${curHeatpoint} ${curCoolpoint}", "warn")
					} else {
						if("${md}" == "heat") { state.oldHeat = null; setHeatingSetpoint(curHeatpoint) }
						else if ("${md}" == "cool") { state.oldCool = null; setCoolingSetpoint(curCoolpoint) }
						else if ("${md}" == "both") {
							if (curHeatpoint <= state.oldHeat) { heatfirst = true }
							else if (curCoolpoint >= state.oldCool) { heatFirst = false }
							else if (curHeatpoint > state.oldHeat) { heatFirst = false }
							else { heatFirst = true }
							if (heatFirst) {
								state.oldHeat = null
								setHeatingSetpoint(curHeatpoint,true)
								state.oldCool = null
								setCoolingSetpoint(curCoolpoint,true)
							} else {
								state.oldCool = null
								setCoolingSetpoint(curCoolpoint,true)
								state.oldHeat = null
								setHeatingSetpoint(curHeatpoint,true)
							}
						}
					}
				} else {
					Logger("changeSetpoint: Invalid Temp Type received... ${md}", "warn")
					state.oldCool = null
					state.oldHeat = null
				}
				break
			default:
				if (curHeatpoint > curCoolpoint) {
					Logger("changeSetpoint: Invalid Temp Type received in auto mode... ${curHeatpoint} ${curCoolpoint} ${val}", "warn")
				}
				//thermostatSetpointEvent(temp)
				break
		}
	}
	pauseEvent("false")
}

// Nest Only allows F temperatures as #.0  and C temperatures as either #.0 or #.5
void setHeatingSetpoint(temp, manChg=false) {
	setHeatingSetpoint(temp.toDouble(), manChg)
}

void setHeatingSetpoint(Double reqtemp, manChg=false) {
	LogAction("setHeatingSetpoint()... ($reqtemp)", "trace")
	def hvacMode = getHvacMode()
	def tempUnit = state?.tempUnit
	def temp = 0.0
	def canHeat = state?.can_heat.toBoolean()
	def result = false
	def locked = state?.tempLockOn.toBoolean()
	def curMinTemp
	def curMaxTemp = 100.0

	if(locked) {
		curMinTemp = device.currentState("lockedTempMin")?.doubleValue
		curMaxTemp = device.currentState("lockedTempMax")?.doubleValue
	}
	LogAction("Heat Temp Received: ${reqtemp} (${tempUnit}) Locked: ${locked}")
	if(canHeat && state?.nestHvac_mode != "eco") {
		switch (tempUnit) {
			case "C":
				temp = Math.round(reqtemp.round(1) * 2) / 2.0f
				if(curMinTemp < 9.0) { curMinTemp = 9.0 }
				if(curMaxTemp > 32.0) { curMaxTemp = 32.0 }
				if (temp) {
					if(temp < curMinTemp) { temp = curMinTemp }
					if(temp > curMaxTemp) { temp = curMaxTemp }
					LogAction("Sending Heat Temp ($temp)")
					if (hvacMode == 'auto') {
						parent.setTargetTempLow(this, tempUnit, temp, virtType())
						heatingSetpointEvent(temp)
					}
					if (hvacMode == 'heat') {
						parent.setTargetTemp(this, tempUnit, temp, hvacMode, virtType())
						thermostatSetpointEvent(temp)
						heatingSetpointEvent(temp)
					}
				}
				result = true
				break
			case "F":
				temp = reqtemp.round(0).toInteger()
				if(curMinTemp < 50) { curMinTemp = 50 }
				if(curMaxTemp > 90) { curMaxTemp = 90 }
				if (temp) {
					if(temp < curMinTemp) { temp = curMinTemp }
					if(temp > curMaxTemp) { temp = curMaxTemp }
					LogAction("Sending Heat Temp ($temp)")
					if (hvacMode == 'auto') {
						parent.setTargetTempLow(this, tempUnit, temp, virtType())
						heatingSetpointEvent(temp)
					}
					if (hvacMode == 'heat') {
						parent.setTargetTemp(this, tempUnit, temp, hvacMode, virtType())
						thermostatSetpointEvent(temp)
						heatingSetpointEvent(temp)
					}
				}
				result = true
				break
			default:
				Logger("no Temperature data $tempUnit")
			break
		}
	} else {
		Logger("Skipping heat change")
		result = false
	}
	if(result) {
		if(manChg) { incManTmpChgCnt() } else { incProgTmpChgCnt() }
	}
}

void setCoolingSetpoint(temp, manChg=false) {
	setCoolingSetpoint( temp.toDouble(), manChg)
}

void setCoolingSetpoint(Double reqtemp, manChg=false) {
	LogAction("setCoolingSetpoint()... ($reqtemp)", "trace")
	def hvacMode = getHvacMode()
	def temp = 0.0
	def tempUnit = state?.tempUnit
	def canCool = state?.can_cool.toBoolean()
	def result = false
	def locked = state?.tempLockOn.toBoolean()
	def curMinTemp
	def curMaxTemp = 100.0

	if(locked) {
		curMinTemp = device.currentState("lockedTempMin")?.doubleValue
		curMaxTemp = device.currentState("lockedTempMax")?.doubleValue
	}
	LogAction("Cool Temp Received: ${reqtemp} (${tempUnit}) Locked: ${locked}")
	if(canCool && state?.nestHvac_mode != "eco") {
		switch (tempUnit) {
			case "C":
				temp = Math.round(reqtemp.round(1) * 2) / 2.0f
				if(curMinTemp < 9.0) { curMinTemp = 9.0 }
				if(curMaxTemp > 32.0) { curMaxTemp = 32.0 }
				if (temp) {
					if(temp < curMinTemp) { temp = curMinTemp }
					if(temp > curMaxTemp) { temp = curMaxTemp }
					LogAction("Sending Cool Temp ($temp)")
					if (hvacMode == 'auto') {
						parent.setTargetTempHigh(this, tempUnit, temp, virtType())
						coolingSetpointEvent(temp)
					}
					if (hvacMode == 'cool') {
						parent.setTargetTemp(this, tempUnit, temp, hvacMode, virtType())
						thermostatSetpointEvent(temp)
						coolingSetpointEvent(temp)
					}
				}
				result = true
				break

			case "F":
				temp = reqtemp.round(0).toInteger()
				if(curMinTemp < 50) { curMinTemp = 50 }
				if(curMaxTemp > 90) { curMaxTemp = 90 }
				if (temp) {
					if(temp < curMinTemp) { temp = curMinTemp }
					if(temp > curMaxTemp) { temp = curMaxTemp }
					LogAction("Sending Cool Temp ($temp)")
					if (hvacMode == 'auto') {
						parent.setTargetTempHigh(this, tempUnit, temp, virtType())
						coolingSetpointEvent(temp)
					}
					if (hvacMode == 'cool') {
						parent.setTargetTemp(this, tempUnit, temp, hvacMode, virtType())
						thermostatSetpointEvent(temp)
						coolingSetpointEvent(temp)
					}
				}
				result = true
				break
			default:
					Logger("no Temperature data $tempUnit")
				break
		}
	} else {
		Logger("Skipping cool change")
		result = false
	}
	if(result) {
		if(manChg) { incManTmpChgCnt() } else { incProgTmpChgCnt() }
	}
}

/************************************************************************************************
|									NEST PRESENCE FUNCTIONS										|
*************************************************************************************************/
void setPresence() {
	LogAction("setPresence()...", "trace")
	def pres = getNestPresence()
	LogAction("Current Nest Presence: ${pres}", "trace")
	if(pres == "auto-away" || pres == "away") {
		if (parent.setStructureAway(this, "false", virtType())) { presenceEvent("home") }
	}
	else if (pres == "home") {
		if (parent.setStructureAway(this, "true", virtType())) { presenceEvent("away") }
	}
}

// backward compatibility for previous nest thermostat (and rule machine)
void away() {
	LogAction("away()...", "trace")
	setAway()
}

// backward compatibility for previous nest thermostat (and rule machine)
void present() {
	LogAction("present()...", "trace")
	setHome()
}

def setAway() {
	LogAction("setAway()...", "trace")
	if (parent.setStructureAway(this, "true", virtType())) { presenceEvent("away") }
}

def setHome() {
	LogAction("setHome()...", "trace")
	if (parent.setStructureAway(this, "false", virtType()) ) { presenceEvent("home") }
}

/************************************************************************************************
|										HVAC MODE FUNCTIONS										|
************************************************************************************************/

def getHvacModes() {
	//LogAction("Building Modes list")
	def modesList = ['off']
	if( state?.can_heat == true ) { modesList.push('heat') }
	if( state?.can_cool == true ) { modesList.push('cool') }
	if( state?.can_heat == true && state?.can_cool == true ) { modesList.push('auto') }
	modesList.push('eco')
	LogAction("Modes = ${modesList}")
	return modesList
}

def changeMode() {
	//LogAction("changeMode..")
	def currentMode = getHvacMode()
	def lastTriedMode = currentMode ?: "off"
	def modeOrder = getHvacModes()
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	LogAction("changeMode() currentMode: ${currentMode}   lastTriedMode:  ${lastTriedMode}  modeOrder:  ${modeOrder}   nextMode: ${nextMode}", "trace")
	setHvacMode(nextMode)
	//ecoDescEvent((nextMode == "eco" ? "User Changed (ST)" : null))
}

def setHvacMode(nextMode) {
	LogAction("setHvacMode(${nextMode})")
	if (nextMode in getHvacModes()) {
		state.lastTriedMode = nextMode
		"$nextMode"(true)
	} else {
		Logger("Invalid Mode '$nextMode'")
	}
}

def doChangeMode(manChg=false) {
	def currentMode = device.currentState("nestThermostatMode")?.value
	LogAction("doChangeMode()  currentMode:  ${currentMode}")
	def errflag = true
	switch(currentMode) {
		case "auto":
			if (parent.setHvacMode(this, "heat-cool", virtType())) {
				errflag = false
				ecoDescEvent(null)
			}
			break
		case "heat":
			if (parent.setHvacMode(this, "heat", virtType())) {
				errflag = false
				ecoDescEvent(null)
			}
			break
		case "cool":
			if (parent.setHvacMode(this, "cool", virtType())) {
				errflag = false
				ecoDescEvent(null)
			}
			break
		case "off":
			if (parent.setHvacMode(this, "off", virtType())) {
				errflag = false
				ecoDescEvent(null)
			}
			break
		case "eco":
			if (parent.setHvacMode(this, "eco", virtType())) {
				errflag = false
				if(manChg) { ecoDescEvent("User Changed (ST)") }
				else { ecoDescEvent("A ST Automation") }
			}
			break
		default:
			Logger("doChangeMode Received an Invalid Request: ${currentMode}", "warn")
			break
	}
	if (errflag) {
		Logger("doChangeMode call to change mode failed: ${currentMode}", "warn")
		refresh()
	}
}

void off(manChg=false) {
	LogAction("off()...", "trace")
	hvacModeEvent("off")
	doChangeMode(manChg)
	if(manChg) { incManModeChgCnt() } else { incProgModeChgCnt() }
}

void heat(manChg=false) {
	LogAction("heat()...", "trace")
	hvacModeEvent("heat")
	doChangeMode(manChg)
	if(manChg) { incManModeChgCnt() } else { incProgModeChgCnt() }
}

void emergencyHeat(manChg=false) {
	LogAction("emergencyHeat()...", "trace")
	Logger("Emergency Heat setting not allowed", "warn")
}

void cool(manChg=false) {
	LogAction("cool()...", "trace")
	hvacModeEvent("cool")
	doChangeMode(manChg)
	if(manChg) { incManModeChgCnt() } else { incProgModeChgCnt() }
}

void auto(manChg=false) {
	LogAction("auto()...", "trace")
	hvacModeEvent("auto")
	doChangeMode(manChg)
	if(manChg) { incManModeChgCnt() } else { incProgModeChgCnt() }
}

void eco(manChg=false) {
	LogAction("eco()...", "trace")
	hvacModeEvent("eco")
	doChangeMode(manChg)
	if(manChg) { incManModeChgCnt() } else { incProgModeChgCnt() }
}

void setThermostatMode(modeStr) {
	LogAction("setThermostatMode()...", "trace")
	switch(modeStr) {
		case "auto":
			auto()
			break
		case "heat":
			heat()
			break
		case "cool":
			cool()
			break
		case "eco":
			eco()
			break
		case "off":
			off()
			break
		case "emergency heat":
			emergencyHeat()
			break
		default:
			Logger("setThermostatMode Received an Invalid Request: ${modeStr}", "warn")
			break
	}
}


/************************************************************************************************
|										FAN MODE FUNCTIONS										|
*************************************************************************************************/
void changeFanMode() {
	def cur = device.currentState("thermostatFanMode")?.value
	if(cur == "on" || !cur) {
		setThermostatFanMode("auto", true)
	} else {
		setThermostatFanMode("on", true)
	}
}

void fanOn() {
	try {
		LogAction("fanOn()...", "trace")
		if (state?.has_fan.toBoolean()) {
			if (parent.setFanMode(this, true, virtType()) ) { fanModeEvent("true") }
		} else { Logger("Error setting fanOn", "error") }
	}
	catch (ex) {
		log.error "fanOn Exception:", ex
		exceptionDataHandler(ex.message, "fanOn")
	}
}

// non standard by ST Capabilities Thermostat Fan Mode
void fanOff() {
	LogAction("fanOff()...", "trace")
	fanAuto()
}

void fanCirculate() {
	LogAction("fanCirculate()...", "trace")
	fanOn()
}

void fanAuto() {
	try {
		LogAction("fanAuto()...", "trace")
		if (state?.has_fan.toBoolean()) {
			if (parent.setFanMode(this,false, virtType()) ) { fanModeEvent("false") }
		} else { Logger("Error setting fanAuto", "error") }
	}
	catch (ex) {
		log.error "fanAuto Exception:", ex
		exceptionDataHandler(ex.message, "fanAuto")
	}
}

void setThermostatFanMode(fanModeStr, manChg=false) {
	LogAction("setThermostatFanMode()... ($fanModeStr)", "trace")
	switch(fanModeStr) {
		case "auto":
			fanAuto()
			break
		case "on":
			fanOn()
			break
		case "circulate":
			fanCirculate()
			break
		case "off":   // non standard by ST Capabilities Thermostat Fan Mode
			fanOff()
			break
		default:
			Logger("setThermostatFanMode Received an Invalid Request: ${fanModeStr}", "warn")
			break
	}
	if(manChg) { incManFanChgCnt() } else { incProgFanChgCnt() }
}


/**************************************************************************
|												LOGGING FUNCTIONS												  |
***************************************************************************/

void Logger(msg, logType = "debug") {
	def smsg = state?.showLogNamePrefix ? "${device.displayName}: ${msg}" : "${msg}"
	switch (logType) {
		case "trace":
			log.trace "${smsg}"
			break
		case "debug":
			log.debug "${smsg}"
			break
		case "info":
			log.info "${smsg}"
			break
		case "warn":
			log.warn "${smsg}"
			break
		case "error":
			log.error "${smsg}"
			break
		default:
			log.debug "${smsg}"
			break
	}
	if(state?.enRemDiagLogging) {
		parent.saveLogtoRemDiagStore(smsg, logType, "Thermostat DTH")
	}
}

// Local Application Logging
void LogAction(msg, logType = "debug") {
	if(state?.debug) {
		Logger(msg, logType)
	}
}

//This will Print logs from the parent app when added to parent method that the child calls
def log(message, level = "trace") {
	def smsg = "PARENT_Log>> " + message
	LogAction(smsg, level)
	return null // always child interface call with a return value
}

def exceptionDataHandler(msg, methodName) {
	if(state?.allowDbException == false) {
		return
	} else {
		if(msg && methodName) {
			def msgString = "${msg}"
			def ttype = "thermostat"
			if(virtType()) { ttype = "vthermostat" }
			parent?.sendChildExceptionData(ttype, devVer(), msgString, methodName)
		}
	}
}

/**************************************************************************
|					  HTML TILE RENDER FUNCTIONS	  					  |
***************************************************************************/

def getFileBase64(url, preType, fileType) {
	try {
		def params = [
			uri: url,
			contentType: '$preType/$fileType'
		]
		httpGet(params) { resp ->
			if(resp.data) {
				def respData = resp?.data
				ByteArrayOutputStream bos = new ByteArrayOutputStream()
				int len
				int size = 4096
				byte[] buf = new byte[size]
				while ((len = respData.read(buf, 0, size)) != -1)
					bos.write(buf, 0, len)
				buf = bos.toByteArray()
				//LogAction("buf: $buf")
				String s = buf?.encodeBase64()
				//LogAction("resp: ${s}")
				return s ? "data:${preType}/${fileType};base64,${s.toString()}" : null
			}
		}
	}
	catch (ex) {
		log.error "getFileBase64 Exception:", ex
		exceptionDataHandler(ex.message, "getFileBase64")
	}
}

def getCssData() {
	def cssData = null
	def htmlInfo = state?.htmlInfo
	if(htmlInfo?.cssUrl && htmlInfo?.cssVer) {
		cssData = getFileBase64(htmlInfo.cssUrl, "text", "css")
		state?.cssVer = htmlInfo?.cssVer
	} else {
		cssData = getFileBase64(cssUrl(), "text", "css")
	}
	return cssData
}

def getChartJsData() {
	def chartJsData = null
	//def htmlInfo = state?.htmlInfo
	def htmlInfo
	state.chartJsData = null

	if(htmlInfo?.chartJsUrl && htmlInfo?.chartJsVer) {
		if(state?.chartJsData) {
			if (state?.chartJsVer?.toInteger() == htmlInfo?.chartJsVer?.toInteger()) {
				//LogAction("getChartJsData: Chart Javascript Data is Current | Loading Data from State...")
				chartJsData = state?.chartJsData
			} else if (state?.chartJsVer?.toInteger() < htmlInfo?.chartJsVer?.toInteger()) {
				//LogAction("getChartJsData: Chart Javascript Data is Outdated | Loading Data from Source...")
				//chartJsData = getFileBase64(htmlInfo.chartJsUrl, "text", "javascript")
				state.chartJsData = chartJsData
				state?.chartJsVer = htmlInfo?.chartJsVer
			}
		} else {
			//LogAction("getChartJsData: Chart Javascript Data is Missing | Loading Data from Source...")
			chartJsData = getFileBase64(htmlInfo.chartJsUrl, "text", "javascript")
			state?.chartJsData = chartJsData
			state?.chartJsVer = htmlInfo?.chartJsVer
		}
	} else {
		//LogAction("getChartJsData: No Stored Chart Javascript Data Found for Device... Loading for Static URL...")
		chartJsData = getFileBase64(chartJsUrl(), "text", "javascript")
	}
	return chartJsData
}

def cssUrl()	 { return "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Documents/css/ST-HTML.min.css" }
def chartJsUrl() { return "https://www.gstatic.com/charts/loader.js" }

def getImg(imgName) { return imgName ? "https://cdn.rawgit.com/tonesto7/nest-manager/master/Images/Devices/$imgName" : "" }

def getWebData(params, desc, text=true) {
	try {
		Logger("getWebData: ${desc} data", "info")
		httpGet(params) { resp ->
			if(resp.data) {
				if(text) {
					return resp?.data?.text.toString()
				} else { return resp?.data }
			}
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.HttpResponseException) {
			Logger("${desc} file not found", "warn")
		} else {
			log.error "getWebData(params: $params, desc: $desc, text: $text) Exception:", ex
		}
		//sendExceptionData(ex, "getWebData")
		return "${label} info not found"
	}
}
def gitRepo()		{ return "tonesto7/nest-manager"}
def gitBranch()		{ return "master" }
def gitPath()		{ return "${gitRepo()}/${gitBranch()}"}
def devVerInfo()	{ return getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/Data/changelog_tstat.txt", contentType: "text/plain; charset=UTF-8"], "changelog") }


/*
	 variable	  attribute for history	   getRoutine			 variable is present

   temperature		   "temperature"		getTemp				   true						 #
   coolSetpoint		 "coolingSetpoint"		getCoolTemp		   state.can_cool				   #
   heatSetpoint		 "heatingSetpoint"		getHeatTemp		   state.can_heat				   #
   operatingState	 "thermostatOperatingState"	getHvacState			true				 idle cooling heating
   operatingMode	"thermostatMode"		getHvacMode			true				 heat cool off auto
	presence	   "presence"			getPresence			true				 present  not present
*/

String getDataString(Integer seriesIndex) {
	//LogAction("getDataString ${seriesIndex}", "trace")
	def dataTable = []
	switch (seriesIndex) {
		case 1:
			dataTable = state?.temperatureTableYesterday
			break
		case 2:
			dataTable = state?.temperatureTable
			break
		case 3:
			dataTable = state?.operatingStateTable
			break
		case 4:
			dataTable = state?.humidityTable
			break
		case 5:
			dataTable = state?.coolSetpointTable
			break
		case 6:
			dataTable = state?.heatSetpointTable
			break
		case 7:
			dataTable = state?.extTempTable
			break
		case 8:
			dataTable = state?.fanModeTable
			break
	}

	def lastVal = 200

	//LogAction("getDataString ${seriesIndex} ${dataTable}")
	//LogAction("getDataString ${seriesIndex}")

	def lastAdded = false
	def dataArray
	def myval
	def myindex
	def lastdataArray = null
	def dataString = ""

	if (seriesIndex == 5) {
	  // state.can_cool
	}
	if (seriesIndex == 6) {
	   // state.can_heat
	}
	if (seriesIndex == 8) {
		//state?.has_fan
	}
	def myhas_fan = state?.has_fan && false ? true : false    // false because not graphing fan operation now

	def has_weather = false
	if( !(state?.curExtTemp == null || state?.curExtTemp == [:])) { has_weather = true }

	def datacolumns

	myindex = seriesIndex
//ERSERS
	datacolumns = 8
	//if (state?.can_heat && state?.can_cool && myhas_fan && has_weather) { datacolumns = 8 }
	if (!myhas_fan) {
		datacolumns -= 1
	}
	if (!has_weather) {
		datacolumns -= 1
		if (myindex == 8) { myindex = 7 }
	}
	if ((!state?.can_heat && state?.can_cool) || (state?.can_heat && !state?.can_cool)) {
		datacolumns -= 1
		if (myindex >= 6) { myindex -= 1 }
	}
	switch (datacolumns) {
		case 8:
			dataArray = [[0,0,0],null,null,null,null,null,null,null,null]
			break
		case 7:
			dataArray = [[0,0,0],null,null,null,null,null,null,null]
			break
		case 6:
			dataArray = [[0,0,0],null,null,null,null,null,null]
			break
		case 5:
			dataArray = [[0,0,0],null,null,null,null,null]
			break
		default:
			LogAction("getDataString: bad column result", "error")
	}

	//dataTable.each() {

	dataTable.any { it ->
		myval = it[2]

		//convert idle / non-idle to numeric value
		if (myindex == 3) {
			if(myval == "idle") { myval = 0 }
			if(myval == "cooling") { myval = 8 }
			if(myval == "heating") { myval = 16 }
			//else { }
		}
/*
		if(myhas_fan && seriesIndex == 8) {
			if(myval == "auto") { myval = 0 }
			if(myval == "on") { myval = 8 }
			//if (myval == "circulate") { myval = 8 }
		}
*/
		if (seriesIndex == 5) {
			if(myval == 0) { return false }
		// state.can_cool
		}
		if (seriesIndex == 6) {
			if(myval == 0) { return false }
		// state.can_heat
		}

		dataArray[myindex] = myval
		dataArray[0] = [it[0],it[1],0]

/*
		//reduce # of points to graph
		if (lastVal != myval) {
			lastAdded = true
			if (lastdataArray) {   //controls curves
				dataString += lastdataArray?.toString() + ","
			}
			lastdataArray = null
			lastVal = myval
			dataString += dataArray?.toString() + ","
		} else { lastAdded = false; lastdataArray = dataArray }
*/
		dataString += dataArray?.toString() + ","
		return false
	}

/*
	if (!lastAdded && dataString) {
		dataArray[myindex] = myval
		dataString += dataArray?.toString() + ","
	}
*/

	if (dataString == "") {
		dataArray[0] = [0,0,0]
		//dataArray[myindex] = 0
		dataString += dataArray?.toString() + ","
	}

	//LogAction("getDataString ${seriesIndex} datacolumns: ${datacolumns}  myindex: ${myindex} datastring: ${dataString}")
	return dataString
}

def tgetSomeOldData(val) {
	LogAction("tgetSomeOldData ${val}", "trace")
	def type = val?.type?.value
	def attributestr  = val?.attributestr?.value
	def gfloat = val?.gfloat?.value
	def devpoll = val?.devpoll?.value
	LogAction("calling getSomeOldData ( ${type}, ${attributestr}, ${gfloat}, ${devpoll})", "trace")
	getSomeOldData(type, attributestr, gfloat, devpoll)
}

def getSomeOldData(type, attributestr, gfloat, devpoll = false, nostate = true) {
	LogAction("getSomeOldData ( ${type}, ${attributestr}, ${gfloat}, ${devpoll})", "trace")

//	if (devpoll && (!state?."${type}TableYesterday" || !state?."${type}Table")) {
//		runIn( 66, "tgetSomeOldData", [data: [type:type, attributestr:attributestr, gfloat:gfloat, devpoll:false]])
//		return
//	}

	def startOfToday = timeToday("00:00", location.timeZone)
	def newValues
	def dataTable = []

	if (( nostate || state?."${type}TableYesterday" == null) && attributestr ) {
		LogAction("Querying DB for yesterday's ${type} data…", "trace")
		def yesterdayData = device.statesBetween("${attributestr}", startOfToday - 1, startOfToday, [max: 100])
		LogAction("got ${yesterdayData.size()}")
		if (yesterdayData.size() > 0) {
			while ((newValues = device.statesBetween("${attributestr}", startOfToday - 1, yesterdayData.last().date, [max: 100])).size()) {
				LogAction("got ${newValues.size()}")
				yesterdayData += newValues
			}
		}
		LogAction("got ${yesterdayData.size()}")
		dataTable = []
		yesterdayData.reverse().each() {
			if (gfloat) { dataTable.add([it.date.format("H", location.timeZone),it.date.format("m", location.timeZone),it.floatValue]) }
			else { dataTable.add([it.date.format("H", location.timeZone),it.date.format("m", location.timeZone),it.stringValue]) }
		}
		LogAction("finished ${dataTable}")
		if (!nostate) {
			state."${type}TableYesterday" = dataTable
		}
	}

	if ( nostate || state?."${type}Table" == null) {
		LogAction("Querying DB for today's ${type} data…", "trace")
		def todayData = device.statesSince("${attributestr}", startOfToday, [max: 100])
		LogAction("got ${todayData.size()}")
		if (todayData.size() > 0) {
			while ((newValues = device.statesBetween("${attributestr}", startOfToday, todayData.last().date, [max: 100])).size()) {
				LogAction("got ${newValues.size()}")
				todayData += newValues
			}
		}
		LogAction("got ${todayData.size()}")
		dataTable = []
		todayData.reverse().each() {
			if (gfloat) { dataTable.add([it.date.format("H", location.timeZone),it.date.format("m", location.timeZone),it.floatValue]) }
			else { dataTable.add([it.date.format("H", location.timeZone),it.date.format("m", location.timeZone),it.stringValue]) }
		}
		LogAction("finished ${dataTable}")
		if (!nostate) {
			state."${type}Table" = dataTable
		}
	}
}

void getSomeData(devpoll = false) {
	//LogAction("getSomeData ${app}", "trace")

// hackery to test getting old data
	def tryNum = 2
	if (state.eric != tryNum ) {
		if (devpoll) {
			runIn( 33, "getSomeData", [overwrite: true])
			return
		}

		runIn( 33, "getSomeData", [overwrite: true])
		state.eric = tryNum

		state.temperatureTableYesterday = null
		state.operatingStateTableYesterday = null
		state.humidityTableYesterday = null
		state.coolSetpointTableYesterday = null
		state.heatSetpointTableYesterday = null
		state.extTempTableYesterday = null
		state.fanModeTableYesterday = null

		state.temperatureTable = null
		state.operatingStateTable = null
		state.humidityTable = null
		state.coolSetpointTable = null
		state.heatSetpointTable = null
		state.extTempTable = null
		state.fanModeTable = null

		state.remove("temperatureTableYesterday")
		state.remove("operatingStateTableYesterday")
		state.remove("humidityTableYesterday")
		state.remove("coolSetpointTableYesterday")
		state.remove("heatSetpointTableYesterday")
		state.remove("extTempTableYesterday")
		state.remove("fanModeTableYesterday")

		state.remove("today")
		state.remove("temperatureTable")
		state.remove("operatingStateTable")
		state.remove("humidityTable")
		state.remove("coolSetpointTable")
		state.remove("heatSetpointTable")
		state.remove("extTempTable")
		state.remove("fanModeTable")

		state.remove("historyStoreMap")

		return
	} else {
		//getSomeOldData("temperature", "temperature", true, devpoll)
		//getSomeOldData("operatingState", "thermostatOperatingState", false, devpoll)
		//getSomeOldData("humidity", "humidity", false, devpoll)
		//if (state?.can_cool) { getSomeOldData("coolSetpoint", "coolingSetpoint", true, devpoll) }
		//if (state?.can_heat) { getSomeOldData("heatSetpoint", "heatingSetpoint", true, devpoll) }
	}

	def today = new Date()
	def todayDay = today.format("dd",location.timeZone)

	if (state?.temperatureTable == null) {

	// these are commented out as the platform continuously times out
		//getSomeOldData("temperature", "temperature", true, devpoll)
		//getSomeOldData("operatingState", "thermostatOperatingState", false, devpoll)
		//getSomeOldData("humidity", "humidity", false, devpoll)
		//if (state?.can_cool) { getSomeOldData("coolSetpoint", "coolingSetpoint", true, devpoll) }
		//if (state?.can_heat) { getSomeOldData("heatSetpoint", "heatingSetpoint", true, devpoll) }

		state.temperatureTable = []
		state.operatingStateTable = []
		state.humidityTable = []
		state.coolSetpointTable = []
		state.heatSetpointTable = []
		state.extTempTable = []
		state.fanModeTable = []
		addNewData()
	}

	def temperatureTable = state?.temperatureTable
	def operatingStateTable = state?.operatingStateTable
	def humidityTable = state?.humidityTable
	def coolSetpointTable = state?.coolSetpointTable
	def heatSetpointTable = state?.heatSetpointTable
	def extTempTable = state?.extTempTable
	def fanModeTable = state?.fanModeTable

	if(fanModeTable == null) {		// upgrade cleanup TODO
		state.fanModeTable = []; fanModeTable = state.fanModeTable; state.fanModeTableYesterday = fanModeTable
	}
	if(extTempTable == null) {		// upgrade cleanup TODO
		state.extTempTable = []; extTempTable = state.extTempTable; state.extTempTableYesterday = extTempTable
	}
	def hm = state?.historyStoreMap
	if(hm == null) {
		initHistoryStore()
	}

	if (state?.temperatureTableYesterday?.size() == 0) {
		state.temperatureTableYesterday = temperatureTable
		state.operatingStateTableYesterday = operatingStateTable
		state.humidityTableYesterday = humidityTable
		state.coolSetpointTableYesterday = coolSetpointTable
		state.heatSetpointTableYesterday = heatSetpointTable
		state.extTempTableYesterday = extTempTable
		state.fanModeTableYesterday = fanModeTable
	}

// DAY CHANGE
	if (!state?.today || state.today != todayDay) {
		state.today = todayDay
		state.temperatureTableYesterday = temperatureTable
		state.operatingStateTableYesterday = operatingStateTable
		state.humidityTableYesterday = humidityTable
		state.coolSetpointTableYesterday = coolSetpointTable
		state.heatSetpointTableYesterday = heatSetpointTable
		state.extTempTableYesterday = extTempTable
		state.fanModeTableYesterday = fanModeTable

		state.temperatureTable = []
		state.operatingStateTable = []
		state.humidityTable = []
		state.coolSetpointTable = []
		state.heatSetpointTable = []
		state.extTempTable = []
		state.fanModeTable = []
		updateOperatingHistory(today)

	}
	//initHistoryStore() 	// TODO DEBUGGING
	//updateOperatingHistory(today) // TODO DEBUGGING
	addNewData()
	//def bb = getHistoryStore()   // TODO DEBUGGING
}

def updateOperatingHistory(today) {
	Logger("updateOperatingHistory(${today})...", "trace")

	def dayChange = false
	def monthChange = false
	def yearChange = false

	def hm = state?.historyStoreMap
	if(hm == null) {
		log.error "hm is null"
		return
	}
	def dayNum = today.format("u", location.timeZone).toInteger() // 1 = Monday,... 7 = Sunday
	def monthNum = today.format("MM", location.timeZone).toInteger()
	def yearNum = today.format("YYYY", location.timeZone).toInteger()

	if(hm?.currentDay == null || hm?.currentDay < 1 || hm?.currentDay > 7) {
		Logger("hm.currentDay is invalid (${hm?.currentDay})", "error")
		return
	}

	if(dayNum == null || dayNum < 1 || dayNum > 7) {
		Logger("dayNum is invalid (${dayNum})", "error")
		return
	}

	if(monthNum == null || monthNum < 1 || monthNum > 12) {
		Logger("monthNum is invalid (${monthNum})", "error")
		return
	}

	Logger("dayNum: ${dayNum} currentDay ${hm.currentDay} | monthNum: ${monthNum} currentMonth ${hm.currentMonth}  | yearNum: ${yearNum} currentYear: ${hm.currentYear}")

	if(dayNum != hm.currentDay) {
		dayChange = true
	}
	if(monthNum != hm.currentMonth) {
		monthChange = true
	}
	if(yearNum != hm.currentYear) {
		yearChange = true
	}

	if(dayChange) {
		def Op_coolingusage = getSumUsage(state.operatingStateTableYesterday, "cooling").toInteger()
		def Op_heatingusage = getSumUsage(state.operatingStateTableYesterday, "heating").toInteger()
		def Op_idle = getSumUsage(state.operatingStateTableYesterday, "idle").toInteger()
		def fan_on = getSumUsage(state.fanModeTableYesterday, "on").toInteger()
		def fan_auto = getSumUsage(state.fanModeTableYesterday, "auto").toInteger()

		log.info "fanon ${fan_on}  fanauto: ${fan_auto} opidle: ${Op_idle}  cool: ${Op_coolingusage} heat: ${Op_heatingusage}"

		hm."OperatingState_Day${hm.currentDay}_cooling" = Op_coolingusage
		hm."OperatingState_Day${hm.currentDay}_heating" = Op_heatingusage
		hm."OperatingState_Day${hm.currentDay}_idle" = Op_idle
		hm."FanMode_Day${hm.currentDay}_On" = fan_on
		hm."FanMode_Day${hm.currentDay}_auto" = fan_auto

		hm.currentDay = dayNum
		hm.OperatingState_DayWeekago_cooling = hm."OperatingState_Day${hm.currentDay}_cooling"
		hm.OperatingState_DayWeekago_heating = hm."OperatingState_Day${hm.currentDay}_heating"
		hm.OperatingState_DayWeekago_idle = hm."OperatingState_Day${hm.currentDay}_idle"
		hm.FanMode_DayWeekago_On = hm."FanMode_Day${hm.currentDay}_On"
		hm.FanMode_DayWeekago_auto = hm."FanMode_Day${hm.currentDay}_auto"
		hm."OperatingState_Day${hm.currentDay}_cooling" = 0L
		hm."OperatingState_Day${hm.currentDay}_heating" = 0L
		hm."OperatingState_Day${hm.currentDay}_idle" = 0L
		hm."FanMode_Day${hm.currentDay}_On" = 0L
		hm."FanMode_Day${hm.currentDay}_auto" = 0L

		def t1 = hm["OperatingState_Month${hm.currentMonth}_cooling"]?.toInteger() ?: 0L
		hm."OperatingState_Month${hm.currentMonth}_cooling" = t1 + Op_coolingusage
		t1 = hm["OperatingState_Month${hm.currentMonth}_heating"]?.toInteger() ?: 0L
		hm."OperatingState_Month${hm.currentMonth}_heating" = t1 + Op_heatingusage
		t1 = hm["OperatingState_Month${hm.currentMonth}_idle"]?.toInteger() ?: 0L
		hm."OperatingState_Month${hm.currentMonth}_idle" = t1 + Op_idle
		t1 = hm["FanMode_Month${hm.currentMonth}_On"]?.toInteger() ?: 0L
		hm."FanMode_Month${hm.currentMonth}_On" = t1 + fan_on
		t1 = hm["FanMode_Month${hm.currentMonth}_auto"]?.toInteger() ?: 0L
		hm."FanMode_Month${hm.currentMonth}_auto" = t1 + fan_auto

		if(monthChange) {
			hm.currentMonth = monthNum
			hm.OperatingState_MonthYearago_cooling = hm."OperatingState_Month${hm.currentMonth}_cooling"
			hm.OperatingState_MonthYearago_heating = hm."OperatingState_Month${hm.currentMonth}_heating"
			hm.OperatingState_MonthYearago_idle = hm."OperatingState_Month${hm.currentMonth}_idle"
			hm.FanMode_MonthYearago_On = hm."FanMode_Month${hm.currentMonth}_On"
			hm.FanMode_MonthYearago_auto = hm."FanMode_Month${hm.currentMonth}_auto"
			hm."OperatingState_Month${hm.currentMonth}_cooling" = 0L
			hm."OperatingState_Month${hm.currentMonth}_heating" = 0L
			hm."OperatingState_Month${hm.currentMonth}_idle" = 0L
			hm."FanMode_Month${hm.currentMonth}_On" = 0L
			hm."FanMode_Month${hm.currentMonth}_auto" = 0L
		}

		t1 = hm[OperatingState_thisYear_cooling]?.toInteger() ?: 0L
		hm.OperatingState_thisYear_cooling = t1 + Op_coolingusage
		t1 = hm[OperatingState_thisYear_heating]?.toInteger() ?: 0L
		hm.OperatingState_thisYear_heating = t1 + Op_heatingusage
		t1 = hm[OperatingState_thisYear_idle]?.toInteger() ?: 0L
		hm.OperatingState_thisYear_idle = t1 + Op_idle
		t1 = hm[FanMode_thisYear_On]?.toInteger() ?: 0L
		hm.FanMode_thisYear_On = t1 + fan_on
		t1 = hm[FanMode_thisYear_auto]?.toInteger() ?: 0L
		hm.FanMode_thisYear_auto = t1 + fan_auto

		if(yearChange) {
			hm.currentYear = yearNum
			hm.OperatingState_lastYear_cooling = hm.OperatingState_thisYear_cooling
			hm.OperatingState_lastYear_heating = hm.OperatingState_thisYear_heating
			hm.OperatingState_lastYear_idle = hm.OperatingState_thisYear_idle
			hm.FanMode_lastYear_On = hm.FanMode_thisYear_On
			hm.FanMode_lastYear_auto = hm.FanMode_thisYear_auto

			hm.OperatingState_thisYear_cooling = 0L
			hm.OperatingState_thisYear_heating = 0L
			hm.OperatingState_thisYear_idle = 0L
			hm.FanMode_thisYear_On = 0L
			hm.FanMode_thisYear_auto = 0L
		}
		state.historyStoreMap = hm
	}
}

def getSumUsage(table, String strtyp) {
	//log.trace "getSumUsage...$strtyp Table size: ${table?.size()}"
	def totseconds = 0L
	def newseconds = 0L

	def hr
	def mins
	def myval
	def lasthr = 0
	def lastmins = 0
	def counting = false
	def firsttime = true
	def strthr
	def strtmin
	table.sort { a, b ->
		a[0] as Integer  <=> b[0] as Integer ?: a[1] as Integer <=> b[1] as Integer ?: a[2] <=> b[2]
	}
	//log.trace "$table"
	table.each() {
		hr = it[0].toInteger()
		mins = it[1].toInteger()
		myval = it[2].toString()
		//log.debug "${it[0]} ${it[1]} ${it[2]}"
		if(myval == strtyp) {
			if(!counting) {
				strthr = firstime ? lasthr : hr
				strtmin = firsttime ? lastmins : mins
				counting = true
			}
		} else if(counting) {
			newseconds = ((hr * 60 + mins) - (strthr * 60 + strtmin)) * 60
			totseconds += newseconds
			counting = false
			//log.debug "found $strtyp   starthr: $strthr  startmin: $strtmin  newseconds: $newseconds   totalseconds: $totseconds"
		}
		firsttime = false
	}
	if(counting) {
		def newDate = new Date()
		lasthr = newDate.format("H", location.timeZone).toInteger()
		lastmins = newDate.format("m", location.timeZone).toInteger()
		if( (hr*60+mins > lasthr*60+lastmins) ) {
			lasthr = 24
			lastmins = 0
		}
		newseconds = ((lasthr * 60 + lastmins) - (strthr * 60 + strtmin)) * 60
		totseconds += newseconds
		//log.debug "still counting found $strtyp  lasthr: $lasthr   lastmins: $lastmins  starthr: $strthr  startmin: $strtmin  newseconds: $newseconds   totalseconds: $totseconds"
	}
	//log.info "$strtyp totseconds: $totseconds"

	return totseconds.toInteger()
}

def initHistoryStore() {
	Logger("initHistoryStore()...", "trace")

	def historyStoreMap = [:]
	def today = new Date()
	def dayNum = today.format("u", location.timeZone) as Integer // 1 = Monday,... 7 = Sunday
	def monthNum = today.format("MM", location.timeZone) as Integer
	def yearNum = today.format("YYYY", location.timeZone) as Integer

	//dayNum = 6   // TODO DEBUGGING

	historyStoreMap = [
		currentDay: dayNum,
		currentMonth: monthNum,
		currentYear: yearNum,
		OperatingState_DayWeekago_cooling: 0L, OperatingState_DayWeekago_heating: 0L, OperatingState_DayWeekago_idle: 0L,
		OperatingState_MonthYearago_cooling: 0L, OperatingState_MonthYearago_heating: 0L, OperatingState_MonthYearago_idle: 0L,
		OperatingState_thisYear_cooling: 0L, OperatingState_thisYear_heating: 0L, OperatingState_thisYear_idle: 0L,
		OperatingState_lastYear_cooling: 0L, OperatingState_lastYear_heating: 0L, OperatingState_lastYear_idle: 0L,
		FanMode_DayWeekago_On: 0L, FanMode_DayWeekago_auto: 0L,
		FanMode_MonthYearago_On: 0L, FanMode_MonthYearago_auto: 0L,
		FanMode_thisYear_On: 0L, FanMode_thisYear_auto: 0L,
		FanMode_lastYear_On: 0L, FanMode_lastYear_auto: 0L
	]

	for(int i = 1; i <= 7; i++) {
		historyStoreMap << ["OperatingState_Day${i}_cooling": 0L, "OperatingState_Day${i}_heating": 0L, "OperatingState_Day${i}_idle": 0L]
		historyStoreMap << ["FanMode_Day${i}_On": 0L, "FanMode_Day${i}_auto": 0L]
	}

	for(int i = 1; i <= 12; i++) {
		historyStoreMap << ["OperatingState_Month${i}_cooling": 0L, "OperatingState_Month${i}_heating": 0L, "OperatingState_Month${i}_idle": 0L]
		historyStoreMap << ["FanMode_Month${i}_On": 0L, "FanMode_Month${i}_auto": 0L]
	}

	//log.debug "historyStoreMap: $historyStoreMap"
	state.historyStoreMap = historyStoreMap
}

def getTodaysUsage() {
	def hm = getHistoryStore()
	def timeMap = [:]
	timeMap << ["cooling":["tData":secToTimeMap(hm?."OperatingState_Day${hm?.currentDay}_cooling"), "tSec":hm?."OperatingState_Day${hm?.currentDay}_cooling"]]
	timeMap << ["heating":["tData":secToTimeMap(hm?."OperatingState_Day${hm?.currentDay}_heating"), "tSec":hm?."OperatingState_Day${hm?.currentDay}_heating"]]
	timeMap << ["idle":["tData":secToTimeMap(hm?."OperatingState_Day${hm?.currentDay}_idle"), "tSec":hm?."OperatingState_Day${hm?.currentDay}_idle"]]
	timeMap << ["fanOn":["tData":secToTimeMap(hm?."FanMode_Day${hm?.currentDay}_On"), "tSec":hm?."FanMode_Day${hm?.currentDay}_On"]]
	timeMap << ["fanAuto":["tData":secToTimeMap(hm?."FanMode_Day${hm?.currentDay}_auto"), "tSec":hm?."FanMode_Day${hm?.currentDay}_auto"]]
	return timeMap
}

def getWeeksUsage() {
	def hm = getHistoryStore()
	def timeMap = [:]
	def coolVal = 0L
	def heatVal = 0L
	def idleVal = 0L
	def fanOnVal = 0L
	def fanAutoVal = 0L
	for(int i = 1; i <= 7; i++) {
		coolVal = coolVal + hm?."OperatingState_Day${i}_cooling"?.toInteger()
		heatVal = heatVal + hm?."OperatingState_Day${i}_heating"?.toInteger()
		idleVal = idleVal + hm?."OperatingState_Day${i}_idle"?.toInteger()
		fanOnVal = fanOnVal + hm?."FanMode_Day${i}_On"?.toInteger()
		fanAutoVal = fanAutoVal + hm?."FanMode_Day${i}_auto"?.toInteger()
	}
	timeMap << ["cooling":["tData":secToTimeMap(coolVal), "tSec":coolVal]]
	timeMap << ["heating":["tData":secToTimeMap(heatVal), "tSec":heatVal]]
	timeMap << ["idle":["tData":secToTimeMap(idleVal), "tSec":idleVal]]
	timeMap << ["fanOn":["tData":secToTimeMap(fanOnVal), "tSec":fanOnVal]]
	timeMap << ["fanAuto":["tData":secToTimeMap(fanAutoVal), "tSec":fanAutoVal]]
	//log.debug "weeksUsage: ${timeMap}"
	return timeMap
}

def getMonthsUsage(monNum) {
	//Logger("getMonthsUsage ${monNum}")
	def hm = getHistoryStore()
	def timeMap = [:]
	def mVal = (monNum >= 1 && monNum <= 12) ? monNum : hm?.currentMonth
	timeMap << ["cooling":["tData":secToTimeMap(hm?."OperatingState_Month${mVal}_cooling"), "tSec":hm?."OperatingState_Month${mVal}_cooling"]]
	timeMap << ["heating":["tData":secToTimeMap(hm?."OperatingState_Month${mVal}_heating"), "tSec":hm?."OperatingState_Month${mVal}_heating"]]
	timeMap << ["idle":["tData":secToTimeMap(hm?."OperatingState_Month${mVal}_idle"), "tSec":hm?."OperatingState_Month${mVal}_idle"]]
	timeMap << ["fanOn":["tData":secToTimeMap(hm?."FanMode_Month${mVal}_On"), "tSec":hm?."FanMode_Month${mVal}_On"]]
	timeMap << ["fanAuto":["tData":secToTimeMap(hm?."FanMode_Month${mVal}_auto"), "tSec":hm?."FanMode_Month${mVal}_auto"]]
	//log.debug "monthsUsage: $mVal ${timeMap}"
	return timeMap
}

def getLast3MonthsUsageMap() {
	def hm = getHistoryStore()
	def timeMap = [:]
	def cnt = 1
	def mVal = (int) hm?.currentMonth
	if(mVal) {
		for(int i=1; i<=3; i++) {
			def newMap = [:]
			def mName = getMonthNumToStr(mVal)
			//log.debug "$mName Usage - Idle: (${hm?."OperatingState_Month${mVal}_idle"}) | Heat: (${hm?."OperatingState_Month${mVal}_heating"}) | Cool: (${hm?."OperatingState_Month${mVal}_cooling"})"
			newMap << ["cooling":["tSec":(hm?."OperatingState_Month${mVal}_cooling" ?: 0L), "iNum":cnt, "mName":mName]]
			newMap << ["heating":["tSec":(hm?."OperatingState_Month${mVal}_heating" ?: 0L), "iNum":cnt, "mName":mName]]
			newMap << ["idle":["tSec":(hm?."OperatingState_Month${mVal}_idle" ?: 0L), "iNum":cnt, "mName":mName]]
			newMap << ["fanOn":["tSec":(hm?."FanMode_Month${mVal}_On" ?: 0L), "iNum":cnt, "mName":mName]]
			newMap << ["fanAuto":["tSec":(hm?."FanMode_Month${mVal}_auto" ?: 0L), "iNum":cnt, "mName":mName]]
			timeMap << [(mVal):newMap]
			mVal = ((mVal==1) ? 12 : mVal-1)
			cnt = cnt+1
		}
	}
	return timeMap
}

def getMonthNumToStr(val) {
	def mons = [1:"Jan", 2:"Feb", 3:"Mar", 4:"Apr", 5:"May", 6:"June", 7:"July", 8:"Aug", 9:"Sept", 10:"Oct", 11:"Nov", 12:"Dec"]
	def res = mons?.find { key, value -> key.toInteger() == val?.toInteger() }
	return res ? res?.value : "unknown"
}

def getYearsUsage() {
	def hm = getHistoryStore()
	def timeMap = [:]
	def coolVal = 0L
	def heatVal = 0L
	def idleVal = 0L
	def fanOnVal = 0L
	def fanAutoVal = 0L
	for(int i = 1; i <= 12; i++) {
		coolVal = coolVal + hm?."OperatingState_Month${i}_cooling"?.toInteger()
		heatVal = heatVal + hm?."OperatingState_Month${i}_heating"?.toInteger()
		idleVal = idleVal + hm?."OperatingState_Month${i}_idle"?.toInteger()
		fanOnVal = fanOnVal + hm?."FanMode_Month${i}_On"?.toInteger()
		fanAutoVal = fanAutoVal + hm?."FanMode_Month${i}_auto"?.toInteger()
	}
	timeMap << ["cooling":["tData":secToTimeMap(coolVal), "tSec":coolVal]]
	timeMap << ["heating":["tData":secToTimeMap(heatVal), "tSec":heatVal]]
	timeMap << ["idle":["tData":secToTimeMap(idleVal), "tSec":idleVal]]
	timeMap << ["fanOn":["tData":secToTimeMap(fanOnVal), "tSec":fanOnVal]]
	timeMap << ["fanAuto":["tData":secToTimeMap(fanAutoVal), "tSec":fanAutoVal]]
	//log.debug "yearsUsage: ${timeMap}"
	return timeMap
}

def doSomething() {
	getNestMgrReport()
	//getTodaysUsage()
	//getWeeksUsage()
	//getMonthsUsage()
	//getYearsUsage()
}

def getHistoryStore() {
	//log.trace "getHistoryStore()..."
	def thm = state?.historyStoreMap
	if(thm == null) {
		log.error "thm is null"
		return
	}
	def hm = thm.clone()

	def Op_coolingusage = getSumUsage(state.operatingStateTable, "cooling").toInteger()
	def Op_heatingusage = getSumUsage(state.operatingStateTable, "heating").toInteger()
	def Op_idle = getSumUsage(state.operatingStateTable, "idle").toInteger()
	def fan_on = getSumUsage(state.fanModeTable, "on").toInteger()
	def fan_auto = getSumUsage(state.fanModeTable, "auto").toInteger()

	//log.info "fanon ${fan_on}  fanauto: ${fan_auto} opidle: ${Op_idle}  cool: ${Op_coolingusage} heat: ${Op_heatingusage}"
	//log.debug "currentDay ${hm.currentDay} | currentMonth ${hm.currentMonth}  | currentYear: ${hm.currentYear}"

	hm."OperatingState_Day${hm.currentDay}_cooling" = Op_coolingusage
	hm."OperatingState_Day${hm.currentDay}_heating" = Op_heatingusage
	hm."OperatingState_Day${hm.currentDay}_idle" = Op_idle
	hm."FanMode_Day${hm.currentDay}_On" = fan_on
	hm."FanMode_Day${hm.currentDay}_auto" = fan_auto

	def t1 = hm["OperatingState_Month${hm.currentMonth}_cooling"]?.toInteger() ?: 0L
	hm."OperatingState_Month${hm.currentMonth}_cooling" = t1 + Op_coolingusage
	t1 = hm["OperatingState_Month${hm.currentMonth}_heating"]?.toInteger() ?: 0L
	hm."OperatingState_Month${hm.currentMonth}_heating" = t1 + Op_heatingusage
	t1 = hm["OperatingState_Month${hm.currentMonth}_idle"]?.toInteger() ?: 0L
	hm."OperatingState_Month${hm.currentMonth}_idle" = t1 + Op_idle
	t1 = hm["FanMode_Month${hm.currentMonth}_On"]?.toInteger() ?: 0L
	hm."FanMode_Month${hm.currentMonth}_On" = t1 + fan_on
	t1 = hm["FanMode_Month${hm.currentMonth}_auto"]?.toInteger() ?: 0L
	hm."FanMode_Month${hm.currentMonth}_auto" = t1 + fan_auto

	t1 = hm[OperatingState_thisYear_cooling]?.toInteger() ?: 0L
	hm.OperatingState_thisYear_cooling = t1 + Op_coolingusage
	t1 = hm[OperatingState_thisYear_heating]?.toInteger() ?: 0L
	hm.OperatingState_thisYear_heating = t1 + Op_heatingusage
	t1 = hm[OperatingState_thisYear_idle]?.toInteger() ?: 0L
	hm.OperatingState_thisYear_idle = t1 + Op_idle
	t1 = hm[FanMode_thisYear_On]?.toInteger() ?: 0L
	hm.FanMode_thisYear_On = t1 + fan_on
	t1 = hm[FanMode_thisYear_auto]?.toInteger() ?: 0L
	hm.FanMode_thisYear_auto = t1 + fan_auto

	return hm
}

def addNewData() {
	def currentTemperature = getTemp()
	def currentcoolSetPoint = getCoolTemp()
	def currentheatSetPoint = getHeatTemp()
	def currentoperatingState = getHvacState()
	def currenthumidity = getHumidity()
	def currentfanMode = getFanMode()
	def currentExternal = null
	if( !(state?.curExtTemp == null || state?.curExtTemp == [:])) { currentExternal = state?.curExtTemp }

	def temperatureTable = state?.temperatureTable
	def operatingStateTable = state?.operatingStateTable
	def humidityTable = state?.humidityTable
	def coolSetpointTable = state?.coolSetpointTable
	def heatSetpointTable = state?.heatSetpointTable
	def extTempTable = state?.extTempTable
	def fanModeTable = state?.fanModeTable

	// add latest coolSetpoint & temperature readings for the graph
	def newDate = new Date()
	def hr = newDate.format("H", location.timeZone) as Integer
	def mins = newDate.format("m", location.timeZone) as Integer

	state.temperatureTable = addValue(temperatureTable, hr, mins, currentTemperature)
	state.operatingStateTable = addValue(operatingStateTable, hr, mins, currentoperatingState)
	state.humidityTable = addValue(humidityTable, hr, mins, currenthumidity)
	state.coolSetpointTable = addValue(coolSetpointTable, hr, mins, currentcoolSetPoint)
	state.heatSetpointTable = addValue(heatSetpointTable, hr, mins, currentheatSetPoint)
	if(!(currentExternal instanceof Map)) { state.extTempTable = addValue(extTempTable, hr, mins, currentExternal) }
	state.fanModeTable = addValue(fanModeTable, hr, mins, currentfanMode)
}

def addValue(table, hr, mins, val) {
	def newval = null
	if(val == [:] || val == null || val == [] || val == "") {
		Logger("bad value ${val}", "error");
		return table
	} else {
		newval = val
	}

	def newTable = []
	table.each() {
		def t_hr = it[0]
		def t_mins = it[1]
		def myval = it[2]
		//log.debug "${it[0]} ${it[1]} ${it[2]}"
		if(!(myval == null || myval == [:] || myval == [])) {
			newTable.add([t_hr, t_mins, myval])
		}
	}

	if(newTable?.size() > 2) {
		def last = newTable?.last()[2]
		def secondtolast = newTable[-2][2]
		if(newval == last && newval == secondtolast) {
			def tempTable = newTable
			newTable = tempTable?.take(tempTable.size() - 1)
		}
	}
	newTable?.add([hr, mins, newval])
	return newTable
}

def getIntListAvg(itemList) {
	//log.debug "itemList: ${itemList}"
	def avgRes = 0
	def iCnt = itemList?.size()
	if(iCnt >= 1) {
		if(iCnt > 1) {
			avgRes = (itemList?.sum().toDouble() / iCnt.toDouble()).round(0)
		} else { itemList?.each { avgRes = avgRes + it.toInteger() } }
	}
	//log.debug "[getIntListAvg] avgRes: $avgRes"
	return avgRes.toInteger()
}

def secToTimeMap(long seconds) {
	long sec = (seconds % 60) ?: 0L
	long minutes = ((seconds % 3600) / 60) ?: 0L
	long hours = ((seconds % 86400) / 3600) ?: 0L
	long days = (seconds / 86400) ?: 0L
	long years = (days / 365) ?: 0L
	def res = ["m":minutes, "h":hours, "d":days, "y":years]
	return res
}

def getStartTime() {
	def startTime = 24
	if (state?.temperatureTable?.size()) { startTime = state?.temperatureTable?.min{it[0].toInteger()}[0].toInteger() }
	if (state?.temperatureTableYesterday?.size()) { startTime = Math.min(startTime, state?.temperatureTableYesterday?.min{it[0].toInteger()}[0].toInteger()) }
	//LogAction("startTime ${startTime}", "trace")
	return startTime
}

def getMinTemp() {
	def has_weather = false
	if( !(state?.curExtTemp == null || state?.curExtTemp == [:])) { has_weather = true }

	def list = []
	if (state?.temperatureTableYesterday?.size() > 0) { list.add(state?.temperatureTableYesterday?.min { it[2] }[2].toInteger()) }
	if (state?.temperatureTable?.size() > 0) { list.add(state?.temperatureTable.min { it[2] }[2].toInteger()) }
	//if (state?.can_cool && state?.coolSetpointTable?.size() > 0) { list.add(state?.coolSetpointTable.min { it[2] }[2].toInteger()) }
	//if (state?.can_heat && state?.heatSetpointTable?.size() > 0) { list.add(state?.heatSetpointTable.min { it[2] }[2].toInteger()) }
	if (has_weather && state?.extTempTable?.size() > 0) { list.add(state?.extTempTable.min { it[2] }[2].toInteger()) }
	//LogAction("getMinTemp: ${list.min()} result: ${list}", "trace")
	return list?.min()
}

def getMaxTemp() {
	def has_weather = false
	if( !(state?.curExtTemp == null || state?.curExtTemp == [:])) { has_weather = true }

	def list = []
	if (state?.temperatureTableYesterday?.size() > 0) { list.add(state?.temperatureTableYesterday.max { it[2] }[2].toInteger()) }
	if (state?.temperatureTable?.size() > 0) { list.add(state?.temperatureTable.max { it[2] }[2].toInteger()) }
	//if (state?.can_cool && state?.coolSetpointTable?.size() > 0) { list.add(state?.coolSetpointTable.max { it[2] }[2].toInteger()) }
	//if (state?.can_heat && state?.heatSetpointTable?.size() > 0) { list.add(state?.heatSetpointTable.max { it[2] }[2].toInteger()) }
	if (has_weather && state?.extTempTable?.size() > 0) { list.add(state?.extTempTable.max { it[2] }[2].toInteger()) }
	//LogAction("getMaxTemp: ${list.max()} result: ${list}", "trace")
	return list?.max()
}

def getAutoChgType(type) {
	if(!type) { type = "nothing" }
	switch(type) {
		case "conWat":
			return "Contact Watcher"
		break
		case "extTmp":
			return "External Temp"
		break
		case "leakWat":
			return "Leak Watcher"
		break
		case "fanCtrl":
			return "Fan Control"
		break
		case "schMot":
			return "Thermostat Schedule"
		break
		case "watchDog":
			return "Watchdog"
		break
		case "nMode":
			return "Nest Mode"
		break
		case "fanCirc":
			return "Fan Circulation"
		break
		default:
			return "Unknown Type"
		break
	}
}

def getChgLogHtml() {
	def chgStr = ""
	log.debug "shownChgLog: ${state?.shownChgLog}"
	if(!state?.shownChgLog == true) {
		chgStr = """
			<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
			<script src="https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.0/js/vex.combined.min.js"></script>
			<script>
				\$(document).ready(function() {
				    vex.dialog.alert({
						unsafeMessage: `<h3 style="background-color: transparent;">What\'s New with the Thermostat</h3>
						<div style="padding: 0 5px 0 5px; text-align: left;">
							${devVerInfo()}
						</div>`
				    , className: 'vex-theme-top'})
				});
			</script>
		"""
		state?.shownChgLog = true
	}
	return chgStr
}

def getGraphHTML() {
	try {
		def tempStr = "°F"
		if ( wantMetric() ) {
			tempStr = "°C"
		}
		checkVirtualStatus()
		//LogAction("State Size: ${getStateSize()} (${getStateSizePerc()}%)")
		def canHeat = state?.can_heat == true ? true : false
		def canCool = state?.can_cool == true ? true : false
		def hasFan = state?.has_fan == true ? true : false
		def leafImg = state?.hasLeaf ? getFileBase64(getImg("nest_leaf_on.gif"), "image", "gif") : getFileBase64(getImg("nest_leaf_off.gif"), "image","gif")
		def updateAvail = !state.updateAvailable ? "" : """<div class="greenAlertBanner">Device Update Available!</div>"""
		def clientBl = state?.clientBl ? """<div class="brightRedAlertBanner">Your Manager client has been blacklisted!\nPlease contact the Nest Manager developer to get the issue resolved!!!</div>""" : ""

		def devBrdCastData = state?.devBannerData ?: null
		def devBrdCastHtml = ""
		if(devBrdCastData) {
			def curDt = Date.parse("E MMM dd HH:mm:ss z yyyy", getDtNow())
			def expDt = Date.parse("E MMM dd HH:mm:ss z yyyy", devBrdCastData?.expireDt.toString())
			if(curDt < expDt) {
				devBrdCastHtml = """
					<div class="orangeAlertBanner">
						<div>Message from the Developer:</div>
						<div style="font-size: 4.6vw;">${devBrdCastData?.message}</div>
					</div>
				"""
			}
		}

		def timeToTarget = device.currentState("timeToTarget").stringValue
		def sunCorrectStr = state?.sunCorrectEnabled ? "Enabled (${state?.sunCorrectActive == true ? "Active" : "Inactive"})" : "Disabled"
		def refreshBtnHtml = state.mobileClientType == "ios" ?
				"""<div class="pageFooterBtn"><button type="button" class="btn btn-info pageFooterBtn" onclick="reloadTstatPage()"><span>&#10227;</span> Refresh</button></div>""" : ""
		def chartHtml = (
				state?.showGraphs &&
				state?.temperatureTable?.size() > 0 &&
				state?.operatingStateTable?.size() > 0 &&
				state?.temperatureTableYesterday?.size() > 0 &&
				state?.humidityTable?.size() > 0 &&
				state?.coolSetpointTable?.size() > 0 &&
				state?.heatSetpointTable?.size() > 0) ? showChartHtml() : (state?.showGraphs ? hideChartHtml() : "")

		def whoSetEco = device?.currentValue("whoSetEcoMode")
		def whoSetEcoDt = state?.ecoDescDt
		def ecoDesc = whoSetEco && !(whoSetEco in ["Not in Eco Mode", "Unknown", "Not Set", "Set Outside of this DTH", "A ST Automation"]) ? "Eco Set By: ${getAutoChgType(whoSetEco)}" : "${whoSetEco}"

		def ecoDescDt = whoSetEcoDt != null ? """<tr><td class="dateTimeTextSmall">${whoSetEcoDt ?: ""}</td></tr>""" : ""
		def schedData = state?.curAutoSchedData
		def schedHtml = ""
		if(schedData) {
			schedHtml = """
				<section class="sectionBg">
					<h3>Automation Schedule</h3>
					<table class="sched">
						<col width="90%">
						<thead class="devInfo">
							<th>Active Schedule</th>
						</thead>
						<tbody>
							<tr><td>#${schedData?.scdNum} - ${schedData?.schedName}</td></tr>
						</tbody>
					</table>
					<h3>Zone Status</h3>

					<table class="sched">
						<col width="50%">
						<col width="50%">
						<thead class="devInfo">
							<th>Temp Source:</th>
							<th>Zone Temp:</th>
						</thead>
						<tbody class="sched">
							<tr>
								<td>${schedData?.tempSrcDesc}</td>
								<td>${schedData?.curZoneTemp}&deg;${state?.tempUnit}</td>
							</tr>
						</tbody>
					</table>
					<table class="sched">
						<col width="45%">
						<col width="45%">
						<thead class="devInfo">
							<th>Desired Heat Temp</th>
							<th>Desired Cool Temp</th>
						</thead>
						<tbody>
							<tr>
								<td>${schedData?.reqSenHeatSetPoint ? "${schedData?.reqSenHeatSetPoint}&deg;${state?.tempUnit}": "Not Available"}</td>
								<td>${schedData?.reqSenCoolSetPoint ? "${schedData?.reqSenCoolSetPoint}&deg;${state?.tempUnit}": "Not Available"}</td>
							</tr>
						</tbody>
					</table>
				</section>
				<br>
			"""
		}

		def chgDescHtml = """
			${schedHtml == "" ? "" : """<div class="swiper-slide">"""}
				<section class="sectionBg">
					<h3>Last Automation Event</h3>
					<table class="devInfo">
						<col width="90%">
						<thead>
							<th>${getAutoChgType(device?.currentValue("whoMadeChanges"))}</th>
						</thead>
						<tbody>
							<tr><td>${device?.currentValue("whoMadeChangesDesc") ?: "Unknown"}</td></tr>
							<tr><td class="dateTimeTextSmall">${device?.currentValue("whoMadeChangesDescDt") ?: ""}</td></tr>
						</tbody>
					</table>
				</section>
				<br>
				<section class="sectionBg">
					<h3>Eco Set By</h3>
					<table class="devInfo">
						<tbody>
							<tr><td>${ecoDesc}</td></tr>
							${ecoDescDt}
						</tbody>
					</table>
				</section>
			${schedHtml == "" ? "" : """</div>"""}
		"""

		def html = """
		<!DOCTYPE html>
		<html>
			<head>
				<meta http-equiv="cache-control" content="max-age=0"/>
				<meta http-equiv="cache-control" content="no-cache"/>
				<meta http-equiv="expires" content="0"/>
				<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT"/>
				<meta http-equiv="pragma" content="no-cache"/>
				<meta name="viewport" content="width = device-width, user-scalable=no, initial-scale=1.0">

				<link rel="stylesheet prefetch" href="${getCssData()}"/>
				<link rel="stylesheet" href="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/Swiper/3.4.1/css/swiper.min.css", "text", "css")}" />
				<link rel="stylesheet" href="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.0/css/vex.min.css", "text", "css")}" />
				<link rel="stylesheet" href="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.0/css/vex-theme-top.min.css", "text", "css")}" />

				<script type="text/javascript" src="${getChartJsData()}"></script>
				<script src="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/Swiper/3.4.1/js/swiper.min.js", "text", "javascript")}"></script>
				<script src="${getFileBase64("https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js", "text", "javascript")}"></script>
				<style>
				</style>
			</head>
			<body>
				${getChgLogHtml()}
				${devBrdCastHtml}
				${clientBl}
		  		${updateAvail}
				<div class="swiper-container">
					<!-- Additional required wrapper -->
					<div class="swiper-wrapper">
						<!-- Slides -->
						<div class="swiper-slide">
							${schedHtml == "" ? "" : "${schedHtml}"}
							<section class="sectionBg">
								<h3>Device Info</h3>
								<table class="devInfo">
								  <col width="50%">
								  <col width="50%">
								  <thead>
									<th>Time to Target</th>
									<th>Sun Correction</th>
								  </thead>
								  <tbody>
									<tr>
									  <td>${timeToTarget}</td>
									  <td>${sunCorrectStr}</td>
									</tr>
								  </tbody>
								</table>
								<table class="devInfo">
								<col width="40%">
								<col width="20%">
								<col width="40%">
								<thead>
								  <th>Network Status</th>
								  <th>Leaf</th>
								  <th>API Status</th>
								</thead>
								<tbody>
								  <tr>
									<td${state?.onlineStatus != "online" ? """ class="redText" """ : ""}>${state?.onlineStatus.toString().capitalize()}</td>
									<td><img src="${leafImg}" class="leafImg"></img></td>
								  	<td${state?.apiStatus != "Good" ? """ class="orangeText" """ : ""}>${state?.apiStatus}</td>
								  </tr>
								</tbody>
							  </table>
							  <table class="devInfo">
								<col width="40%">
								<col width="20%">
								<col width="40%">
								  <thead>
								    <th>Firmware Version</th>
								    <th>Debug</th>
								    <th>Device Type</th>
								  </thead>
								<tbody>
								  <tr>
									<td>${state?.softwareVer.toString()}</td>
									<td>${state?.debugStatus}</td>
									<td>${state?.devTypeVer.toString()}</td>
								  </tr>
								</tbody>
							  </table>
							  <table class="devInfo">
								<thead>
								  <th>Nest Checked-In</th>
								  <th>Data Last Received</th>
								</thead>
								<tbody>
								  <tr>
									<td class="dateTimeText">${state?.lastConnection.toString()}</td>
									<td class="dateTimeText">${state?.lastUpdatedDt.toString()}</td>
								  </tr>
								</tbody>
							  </table>
						   </section>
						   ${schedHtml == "" ? """<br>${chgDescHtml}""" : ""}
						</div>
						${schedHtml == "" ? "" : """${chgDescHtml}"""}
						${chartHtml}
					</div>
					<!-- If we need pagination -->
					<div class="swiper-pagination"></div>

					<div style="text-align: center;">
						<p class="slideFooterMsg">Swipe/Tap to Change Slide</p>
					</div>
				</div>
				<script>
					var mySwiper = new Swiper ('.swiper-container', {
						direction: 'horizontal',
						initialSlide: 0,
						lazyLoading: true,
						loop: false,
						slidesPerView: '1',
						centeredSlides: true,
						spaceBetween: 100,
						autoHeight: true,
						keyboardControl: true,
            			mousewheelControl: true,
						iOSEdgeSwipeDetection: true,
						iOSEdgeSwipeThreshold: 20,
						parallax: true,
						slideToClickedSlide: true,

						effect: 'coverflow',
						coverflow: {
						  rotate: 50,
						  stretch: 0,
						  depth: 100,
						  modifier: 1,
						  slideShadows : true
						},
						onTap: function(s, e) {
							s.slideNext(false);
							if (s.clickedIndex >= s.slides.length) {
								s.slideTo(0, 400, false)
							}
						},
						pagination: '.swiper-pagination',
						paginationHide: false,
						paginationClickable: true
					})
					function reloadTstatPage() {
						var url = "https://" + window.location.host + "/api/devices/${device?.getId()}/graphHTML"
						window.location = url;
					}
				</script>
				${refreshBtnHtml}
			</body>
		</html>
		"""
		incHtmlLoadCnt()
		render contentType: "text/html", data: html, status: 200
	} catch (ex) {
		log.error "graphHTML Exception:", ex
		exceptionDataHandler(ex.message, "graphHTML")
	}
}

def showChartHtml() {
	def tempStr = "°F"
	if ( wantMetric() ) {
		tempStr = "°C"
	}
	def canHeat = state?.can_heat == true ? true : false
	def canCool = state?.can_cool == true ? true : false
	def hasFan = state?.has_fan == true ? true : false
	def has_weather = false
	def commastr = ""
	if( !(state?.curExtTemp == null || state?.curExtTemp == [:])) { has_weather = true; commastr = "," }

	def coolstr1
	def coolstr2
	def coolstr3
	if(canCool) {
		coolstr1 = "data.addColumn('number', 'CoolSP');"
		coolstr2 =  getDataString(5)
		coolstr3 = "4: {targetAxisIndex: 1, type: 'line', color: '#85AAFF', lineWidth: 1},"
	}

	def heatstr1
	def heatstr2
	def heatstr3
	if(canHeat) {
		heatstr1 = "data.addColumn('number', 'HeatSP');"
		heatstr2 = getDataString(6)
		heatstr3 = "5: {targetAxisIndex: 1, type: 'line', color: '#FF4900', lineWidth: 1}${commastr}"
	}

	def weathstr1 = "data.addColumn('number', 'ExtTmp');"
	def weathstr2 = getDataString(7)
	def weathstr3 = "6: {targetAxisIndex: 1, type: 'line', color: '#000000', lineWidth: 1}"
	if(state?.has_weather) {
		weathstr1 = "data.addColumn('number', 'ExtTmp');"
		weathstr2 = getDataString(7)
		weathstr3 = "6: {targetAxisIndex: 1, type: 'line', color: '#000000', lineWidth: 1}"
	}

	if (canCool && !canHeat) { coolstr3 = "4: {targetAxisIndex: 1, type: 'line', color: '#85AAFF', lineWidth: 1}${commastr}" }

	if (!canCool && canHeat) { heatstr3 = "4: {targetAxisIndex: 1, type: 'line', color: '#FF4900', lineWidth: 1}${commastr}" }

	if (!canCool) {
		coolstr1 = ""
		coolstr2 = ""
		coolstr3 = ""
		weathstr3 = "5: {targetAxisIndex: 1, type: 'line', color: '#000000', lineWidth: 1}"
	}

	if(!canHeat) {
		heatstr1 = ""
		heatstr2 = ""
		heatstr3 = ""
		weathstr3 = "5: {targetAxisIndex: 1, type: 'line', color: '#000000', lineWidth: 1}"
	}

	if(!has_weather) {
		weathstr1 = ""
		weathstr2 = ""
		weathstr3 = ""
	}

	def minval = getMinTemp()
	def minstr = "minValue: ${minval},"

	def maxval = getMaxTemp()
	def maxstr = "maxValue: ${maxval},"

	def differ = maxval - minval
	minstr = "minValue: ${(minval - (wantMetric() ? 2:5))},"
	maxstr = "maxValue: ${(maxval + (wantMetric() ? 2:5))},"

	def uData = getTodaysUsage()
	def thData = (uData?.heating?.tSec.toLong()/3600).toDouble().round(0)
	def tcData = (uData?.cooling?.tSec.toLong()/3600).toDouble().round(0)
	def tiData = (uData?.idle?.tSec.toLong()/3600).toDouble().round(0)
	def tfoData = (uData?.fanOn?.tSec.toLong()/3600).toDouble().round(0)
	def tfaData = (uData?.fanAuto?.tSec.toLong()/3600).toDouble().round(0)

	//Month Chart Section
	uData = getMonthsUsage()
	def mhData = (uData?.heating?.tSec.toLong()/3600).toDouble().round(0)
	def mcData = (uData?.cooling?.tSec.toLong()/3600).toDouble().round(0)
	def miData = (uData?.idle?.tSec.toLong()/3600).toDouble().round(0)
	def mfoData = (uData?.fanOn?.tSec.toLong()/3600).toDouble().round(0)
	def mfaData = (uData?.fanAuto?.tSec.toLong()/3600).toDouble().round(0)

	def useTabListSize = 0
	if(canHeat) { useTabListSize = useTabListSize+1 }
	if(canCool) { useTabListSize = useTabListSize+1 }
	if(hasFan) { useTabListSize = useTabListSize+1 }
	def lStr = ""
	//Last 3 Months and Today Section
	def grpUseData = getLast3MonthsUsageMap()
	def m1Data = []
	def m2Data = []
	def m3Data = []
	grpUseData?.each { mon ->
		def data = mon?.value
		def heat = data?.heating ? (data?.heating?.tSec.toLong()/3600).toDouble().round(0) : 0
		def cool = data?.cooling ? (data?.cooling?.tSec.toLong()/3600).toDouble().round(0) : 0
		def idle = data?.idle ? (data?.idle?.tSec.toLong()/3600).toDouble().round(0) : 0
		def fanOn = data?.fanOn ? (data?.fanOn?.tSec.toLong()/3600).toDouble().round(0) : 0
		def fanAuto = data?.fanAuto ? (data?.fanAuto?.tSec.toLong()/3600).toDouble().round(0) : 0
		def mName = getMonthNumToStr(mon?.key)
		lStr += "\n$mName Usage - Idle: ($idle) | Heat: ($heat) | Cool: ($cool) | FanOn: ($fanOn) | FanAuto: ($fanAuto)"
		def iNum = 1
		if(data?.idle?.iNum) { iNum = data?.idle?.iNum.toInteger()	}
		else if(data?.heating?.iNum) {iNum = data?.heating?.iNum.toInteger() }
		else if(data?.cooling?.iNum == 1) { iNum = data?.cooling?.iNum.toInteger() }
		else if(data?.fanOn?.iNum == 1) { iNum = data?.fanOn?.iNum.toInteger() }

		if(iNum == 1) {
			m1Data.push("'$mName'")
			if(canHeat) { m1Data.push("${heat}") }
			if(canCool) { m1Data.push("${cool}") }
			if(hasFan) { m1Data.push("${fanOn}") }
		 }
		if(iNum == 2) {
			m2Data.push("'$mName'")
			if(canHeat) { m2Data.push("${heat}") }
			if(canCool) { m2Data.push("${cool}") }
			if(hasFan) { m2Data.push("${fanOn}") }
		}
		if(iNum == 3) {
			m3Data.push("'$mName'")
			if(canHeat) { m3Data.push("${heat}") }
			if(canCool) { m3Data.push("${cool}") }
			if(hasFan) { m3Data.push("${fanOn}") }
		}
	}
	lStr += "\nToday's Usage - Idle: ($tiData) | Heat: ($thData) | Cool: ($tcData) | FanOn: ($tfoData) | FanAuto: ($tfaData)"
	def mUseHeadStr = ["'Month'"]
	if(canHeat) { mUseHeadStr.push("'Heat'") }
	if(canCool) { mUseHeadStr.push("'Cool'") }
	if(hasFan) { mUseHeadStr.push("'FanOn'") }

	def tdData = ["'Today'"]
	if(canHeat) { tdData.push("${thData}") }
	if(canCool) { tdData.push("${tcData}") }
	if(hasFan) { tdData.push("${tfoData}") }
	lStr += "\nToday Data List: $tdData\n\n"

	//log.debug lStr

	def data = """
		<script type="text/javascript">
			google.charts.load('current', {packages: ['corechart']});
			google.charts.setOnLoadCallback(drawHistoryGraph);
			google.charts.setOnLoadCallback(drawUseGraph);

			function drawHistoryGraph() {
				var data = new google.visualization.DataTable();
				data.addColumn('timeofday', 'time');
				data.addColumn('number', 'Temp (Y)');
				data.addColumn('number', 'Temp (T)');
				data.addColumn('number', 'Operating');
				data.addColumn('number', 'Humidity');
				${coolstr1}
				${heatstr1}
				${weathstr1}
				data.addRows([
					${getDataString(1)}
					${getDataString(2)}
					${getDataString(3)}
					${getDataString(4)}
					${coolstr2}
					${heatstr2}
					${weathstr2}
				]);
				var options = {
					width: '100%',
					height: '100%',
					animation: {
						duration: 1500,
						startup: true
					},
					hAxis: {
						format: 'H:mm',
						minValue: [${getStartTime()},0,0],
						slantedText: true,
						slantedTextAngle: 30
					},
					series: {
						0: {targetAxisIndex: 1, type: 'area', color: '#FFC2C2', lineWidth: 1},
						1: {targetAxisIndex: 1, type: 'area', color: '#FF0000'},
						2: {targetAxisIndex: 0, type: 'area', color: '#ffdc89'},
						3: {targetAxisIndex: 0, type: 'area', color: '#B8B8B8'},
						${coolstr3}
						${heatstr3}
						${weathstr3}
					},
					vAxes: {
						0: {
							title: 'Humidity (%)',
							format: 'decimal',
							minValue: 0,
							maxValue: 100,
							textStyle: {color: '#B8B8B8'},
							titleTextStyle: {color: '#B8B8B8'}
						},
						1: {
							title: 'Temperature (${tempStr})',
							format: 'decimal',
							${minstr}
							${maxstr}
							textStyle: {color: '#FF0000'},
							titleTextStyle: {color: '#FF0000'}
						}
					},
					legend: {
						position: 'bottom',
						maxLines: 4,
						textStyle: {color: '#000000'}
					},
					chartArea: {
						left: '12%',
						right: '18%',
						top: '3%',
						bottom: '27%',
						height: '80%',
						width: '100%'
					}
				};
				var chart = new google.visualization.ComboChart(document.getElementById('main_graph'));
				chart.draw(data, options);
			}

			function drawUseGraph() {
				var data = google.visualization.arrayToDataTable([
				  ${mUseHeadStr},
				  ${tdData?.size() ? "${tdData}," : ""}
				  ${m3Data?.size() ? "${m3Data}${(m2Data?.size() || m1Data?.size() || tdData?.size()) ? "," : ""}" : ""}
				  ${m2Data?.size() ? "${m2Data}${(m1Data?.size() || tdData?.size())  ? "," : ""}" : ""}
				  ${m1Data?.size() ? "${m1Data}" : ""}
				]);

				var view = new google.visualization.DataView(data);
				view.setColumns([
					${(useTabListSize >= 1) ? "0," : ""}
					${(useTabListSize >= 1) ? "1, { calc: 'stringify', sourceColumn: 1, type: 'string', role: 'annotation' }${(useTabListSize > 1) ? "," : ""} // Heat Column": ""}
					${(useTabListSize > 1) ? "2, { calc: 'stringify', sourceColumn: 2, type: 'string', role: 'annotation' }${(useTabListSize > 2) ? "," : ""} // Cool column" : ""}
					${(useTabListSize > 2) ? "3, { calc: 'stringify', sourceColumn: 3, type: 'string', role: 'annotation' } // FanOn Column" : ""}
				]);
				var options = {
					vAxis: {
					  title: 'Hours'
					},
					seriesType: 'bars',
					colors: ['#FF9900', '#0066FF', '#884ae5'],
					chartArea: {
					  left: '15%',
					  right: '23%',
					  top: '7%',
					  bottom: '10%',
					  height: '100%',
					  width: '90%'
					}
				};

				var columnWrapper = new google.visualization.ChartWrapper({
					chartType: 'ComboChart',
					containerId: 'use_graph',
					dataTable: view,
					options: options
				});
				columnWrapper.draw()
			}
		  </script>
		  <div class="swiper-slide">
		  	<section class="sectionBg">
			  <h3>Event History</h3>
	  		  <div id="main_graph" style="width: 100%; height: 425px;"></div>
			</section>
  		  </div>
  		  <div class="swiper-slide">
		  	<section class="sectionBg">
				<h3>Usage History</h3>
  		    	<div id="use_graph" style="width: 100%; height: 425px;"></div>
			</section>
  		  </div>
	  """
	return data
}

def hideChartHtml() {
	def data = """
		<div class="swiper-slide">
			<section class="sectionBg" style="min-height: 250px;">
			  <h3>Event History</h3>
			  <br>
			  <div class="centerText">
				<p>Waiting for more data to be collected...</p>
				<p>This may take a few hours</p>
			  </div>
			</section>
		</div>
	"""
	return data
}

def getMonthUseChartData(mNum=null) {
	def today = new Date()
	def monthNum = mNum ?: today.format("MM", location.timeZone).toInteger()
	def hData = null; def cData = null;	def iData = null; def f1Data = null; def f0Data = null;
	def uData = getMonthsUsage(monthNum)
	log.debug "uData: $uData"
	uData?.each { item ->
		log.debug "item: $item"
		def type = item?.key
		def tData = item?.value?.tData
		def h = tData?.h.toInteger()
		def m = tData?.m.toInteger()
		def d = tData?.d.toInteger()
		def y = tData?.y.toInteger()
		if(h>0 || m>0 || d>0) {
			if(type == "heating") 	{ hData = item }
			if(type == "cooling") 	{ cData = item }
			if(type == "idle")	  	{ iData = item }
			//if(type == "fanOn")   	{ f1Data = item }
			//if(type == "fanAuto")	{ f0Data = item }
		}
	}
}

void updateNestReportData() {
	nestReportStatusEvent()
}

def cleanDevLabel() {
	return device.label.toString().replaceAll("-", "")
}

def getDayTimePerc(val,data) {
	//log.debug "getDayTimePerc($val, $data)"
	//log.debug "getDayElapSec: ${getDayElapSec()}"
	if(!data) { return null }
	return (int) ((val.toInteger()/getDayElapSec())*100).toDouble().round(0)
}

def getDayElapSec() {
	Calendar c = Calendar.getInstance();
	long now = c.getTimeInMillis();
	c.set(Calendar.HOUR_OF_DAY, 0);
	c.set(Calendar.MINUTE, 0);
	c.set(Calendar.SECOND, 0);
	c.set(Calendar.MILLISECOND, 0);
	long passed = now - c.getTimeInMillis();
	return (long) passed / 1000;
}

def getTimeMapString(data) {
	if(!data) { return null }
	def str = ""
	def d = data?.d
	def h = data?.h
	def m = data?.m
	if(h>0 || m>0 || d>0) {
		if(d>0) {
			str += "$d day${d>1 ? "s" : ""}"
			str += d>0 || m>0 ? " and " : ""
		}
		if (h>0) {
			str += h>0 ? "$h Hour${h>1 ? "s" : ""} " : ""
			str += m>0 ? "and " : ""
		}
		if (m>0) {
			str += m>0 ? "$m minute${m>1 ? "s" : ""}" : ""
		}
		return str
	} else {
		return null
	}
}

def getNestMgrReport() {
	//log.trace "getNestMgrReport()..."
	def str = ""
	if(state?.voiceReportPrefs?.allowVoiceZoneRprt || state?.voiceReportPrefs?.allowVoiceUsageRprt) {
		str += "Here is the up to date ${cleanDevLabel()} Report. "

		if(state?.voiceReportPrefs?.vRprtSched == true) {
			if(state?.voiceReportPrefs?.allowVoiceZoneRprt == false) {
				Logger("getNestMgrReport: Zone status voice reports have been disabled by Nest manager app preferences", "info")
				str += " automation schedule voice reports have been disabled by Nest manager app preferences. Please open your manager app and change the preferences and try again. "
			}
			else {
				def schRprtDesc = parent?.reqSchedInfoRprt(this)
				if(schRprtDesc) {
					str += schRprtDesc.toString() + "  "
					str += getExtTempVoiceDesc()
					str += " Now let's move on to usage.  "
				}
			}
		}

		if(state?.voiceReportPrefs?.vRprtUsage == true) {
			if(state?.voiceReportPrefs?.allowVoiceUsageRprt == false) {
				Logger("getNestMgrReport: Zone status voice reports have been disabled by Nest manager app preferences", "info")
				str += "Zone status voice reports have been disabled by Nest manager app preferences. Please open your manager app and change the preferences and try again. "
			} else {
				str += getUsageVoiceReport("runtimeToday")
			}
		}
	} else {
		str += "All voice reports have been disabled by Nest Manager app preferences. Please open your manager app and change the preferences and try again. "
		return str
	}
	log.trace "NestMgrReport Response: ${str}"
	incVoiceRprtCnt()
	return str
}

def incVoiceRprtCnt() 	{ state?.voiceRprtCnt = (state?.voiceRprtCnt ? state?.voiceRprtCnt.toInteger()+1 : 1) }
def incManTmpChgCnt() 	{ state?.manTmpChgCnt = (state?.manTmpChgCnt ? state?.manTmpChgCnt.toInteger()+1 : 1) }
def incProgTmpChgCnt() 	{ state?.progTmpChgCnt = (state?.progTmpChgCnt ? state?.progTmpChgCnt.toInteger()+1 : 1) }
def incManModeChgCnt() 	{ state?.manModeChgCnt = (state?.manModeChgCnt ? state?.manModeChgCnt.toInteger()+1 : 1) }
def incProgModeChgCnt() { state?.progModeChgCnt = (state?.progModeChgCnt ? state?.progModeChgCnt.toInteger()+1 : 1) }
def incManFanChgCnt() 	{ state?.manFanChgCnt = (state?.manFanChgCnt ? state?.manFanChgCnt.toInteger()+1 : 1) }
def incProgFanChgCnt() 	{ state?.progFanChgCnt = (state?.progFanChgCnt ? state?.progFanChgCnt.toInteger()+1 : 1) }
def incHtmlLoadCnt() 	{ state?.htmlLoadCnt = (state?.htmlLoadCnt ? state?.htmlLoadCnt.toInteger()+1 : 1) }
def incInfoBtnTapCnt()	{ state?.infoBtnTapCnt = (state?.infoBtnTapCnt ? state?.infoBtnTapCnt.toInteger()+1 : 1); return ""; }

def getMetricCntData() {
	def ttype = ""
	if(virtType()) { ttype = "v" }
	return 	[
			"${ttype}tstatVoiceRptCnt":(state?.voiceRprtCnt ?: 0), "${ttype}tstatManTmpChgCnt":(state?.manTmpChgCnt ?: 0), "${ttype}tstatProgTmpChgCnt":(state?.progTmpChgCnt ?: 0), "${ttype}tstatManModeChgCnt":(state?.manModeChgCnt ?: 0),
			"${ttype}tstatProgModeChgCnt":(state?.progModeChgCnt ?: 0), "${ttype}tstatManFanChgCnt":(state?.manFanChgCnt ?: 0),	"${ttype}tstatProgFanChgCnt":(state?.progFanChgCnt ?: 0), "${ttype}tstatHtmlLoadCnt":(state?.htmlLoadCnt ?: 0),
			//"${ttype}tstatInfoBtnTapCnt":(state?.infoBtnTapCnt ?: 0)
			]
}

def getExtTempVoiceDesc() {
	def str = ""
	if(state?.voiceReportPrefs?.vRprtExtWeat != true || state?.voiceReportPrefs?.vRprtExtWeat == null) { return str }
	def extTmp = !(state?.curExtTemp == null || state?.curExtTemp == [:]) ? state?.curExtTemp.toDouble() : null
	if(extTmp) {
		str += "Looking Outside the current external temp is "
		if(extTmp > adj_temp(78.0) && extTmp <= adj_temp(85.0)) { str += "a scorching " }
		else if(extTmp > adj_temp(76.0) && extTmp <= adj_temp(80.0)) { str += "a roasting " }
		else if(extTmp > adj_temp(74.0) && extTmp <= adj_temp(76.0)) { str += "a balmy " }
		else if(extTmp >= adj_temp(68.0) && extTmp <= adj_temp(74.0)) { str += "a comfortable " }
		else if(extTmp >= adj_temp(64.0) && extTmp <= adj_temp(68.0)) { str += "a breezy " }
		else if(extTmp >= adj_temp(50.0) && extTmp < adj_temp(64.0)) { str += "a chilly " }
		else if(extTmp < adj_temp(50.0)) { str += "a freezing " }
		str += "${extTmp} degrees. "
	}
	return str
}

private adj_temp(tempF) {
	if(getTemperatureScale() == "C") {
		return (tempF - 32) * 5/9 as Double
	} else {
		return tempF
	}
}

def getUsageVoiceReport(type) {
	switch(type) {
		case "runtimeToday":
			return generateUsageText("today" ,getTodaysUsage())
			break
		case "runtimeWeek":
			return generateUsageText("week" ,getWeeksUsage())
			break
		case "runtimeMonth":
			return generateUsageText("month" ,getMonthsUsage())
			 break
		default:
			return "I'm sorry but the report type received was not valid"
			break
	}
}

def generateUsageText(timeType, timeMap) {
	def str = ""
	if(timeType && timeMap) {
		def hData = null; def cData = null;	def iData = null; def f1Data = null; def f0Data = null;

		timeMap?.each { item ->
			def type = item?.key
			def tData = item?.value?.tData
			def h = tData?.h.toInteger()
			def m = tData?.m.toInteger()
			def d = tData?.d.toInteger()
			def y = tData?.y.toInteger()
			//if(h>0 || m>0 || d>0) {
				if(type == "heating") 	{ hData = item }
				if(type == "cooling") 	{ cData = item }
				if(type == "idle")	  	{ iData = item }
				//if(type == "fanOn")   	{ f1Data = item }
				//if(type == "fanAuto")	{ f0Data = item }
			//}
		}
		if(hData || cData || iData) {// || f1Data || f0Data) {
			str += " Based on the devices activity so far today. "
			def showAnd = hData || cData //|| f0Data || f1Data
			def iTime = 0; def hTime = 0; def cTime = 0;
			def iTmStr; def hTmStr; def cTmStr;

			//Fills Idle Usage Data
			if(iData?.key == "idle") {
				iTmStr = getTimeMapString(iData?.value?.tData)
				iTime = getDayTimePerc(iData?.value?.tSec.toInteger(),iData?.value?.tData)
			}
			//Fills Heating Usage Data
			if(hData?.key == "heating") {
				hTmStr = getTimeMapString(hData?.value?.tData)
				hTime = getDayTimePerc(hData?.value?.tSec.toInteger(),hData?.value?.tData)
			}

			//Fills Cooling Usage Data
			if(cData?.key == "cooling") {
				cTmStr = getTimeMapString(cData?.value?.tData)
				cTime = getDayTimePerc(cData?.value?.tSec.toInteger(),cData?.value?.tData)
			}

			def tmMap = new TreeMap<Integer, String>(["${hTime}":"heating", "${cTime}":"cooling", "${iTime}":"idle"])
			def mSz = tmMap?.size()
			def last = null
			tmMap?.reverseEach {
				if(it?.key.toInteger() > 0) {
					switch(it?.value.toString()) {
						case "idle":
							def lastOk = (last in ["cooling", "heating"])
							str += lastOk ? " and" : ""
							str += getIdleUsageDesc(iTime,, iTmStr, timeType)
							last = it?.value.toString()
							break
						case "heating":
							def lastOk = (last in ["idle", "cooling"])
							str += lastOk ? " and" : ""
							str += getHeatUsageDesc(hTime, hTmStr, timeType)
							last = it?.value.toString()
							break
						case "cooling":
							def lastOk = (last in ["idle", "heating"])
							str += lastOk ? " and" : ""
							str += getCoolUsageDesc(cTime, cTmStr, timeType)
							last = it?.value.toString()
							break
					}
					str += ". "
				}
			}

			/*if(type in ["fanAuto", "fanOn"]) {
				//not sure how to format the fan strings yet

			}*/
			//log.debug "idle: $iTime%"
			//log.debug "heating: $hTime%"
			//log.debug "cooling: $cTime%"
		} else {
			str += " There doesn't appear to have been any usage data collected yet.  "
		}
	}
	str += " That is all for the current nest report. Check back later and have a wonderful day..."
	//log.debug "generateUsageText: $str"
	return str
}

def getIdleUsageDesc(perc, tmStr, timeType) {
	def str = ""
	if(timeType == "today") {
		if (perc>0 && perc <=100) {
			str += " The device has been idle ${perc} percent of the day at "
			str += tmStr
		}
	}
}

def getHeatUsageDesc(perc, tmStr, timeType) {
	def str = ""
	if(timeType == "today") {
		if (perc>=86) {
			str += " spent way too much time "
			str += tmStr
			str += " heating your home ${timeType != "today" ? "this " : ""}${timeType} "
		}
		else if(perc>=66 && perc<=85) {
			str += " it must have been freezing today because it was heating your home for "
			str += tmStr
		}
		else if (perc>=34 && perc<66) {
			str += " it looks like the weather was a bit chilly today because your device spent "
			str += tmStr
			str += " trying to keep your home cozy "
		}
		else if (perc>0 && perc<34) {
			str += " It only spent ${tmStr} heating up the home"
		}
	}
	return str
}

def getCoolUsageDesc(perc, tmStr, timeType) {
	def str = ""
	if(timeType == "today") {
		if(perc>=66 && perc<=100) {
			str += " it must have been scorching outside because is was cooling for "
			str += tmStr
		}
		else if (perc>=34 && perc<66) {
			str += " it must have been a beautiful day because your device only cooled for "
			str += tmStr
		}
		else if (perc>0 && perc<34) {
			str += " it must have been a beautiful day because your device only cooled for "
			str += tmStr
		}
	}
	return str
}

private def textDevName()  	{ return "Nest Thermostat${appDevName()}" }
private def appDevType()   	{ return false }
private def appDevName()   	{ return appDevType() ? " (Dev)" : "" }
private def virtType()		{ return state?.virtual == true ? true : false }
