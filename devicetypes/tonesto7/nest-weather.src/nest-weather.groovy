/**
 *  Nest Weather
 *  Author: Anthony S. (@tonesto7)
 *  Co-Authors: Ben W. (@desertBlade)  Eric S. (@E_sch)
 *  Graphing Modeled on code from Andreas Amann (@ahndee)
 *
 *	Copyright (C) 2017, 2018, 2019 Anthony S., Ben W.
 * 	Licensing Info: Located at https://raw.githubusercontent.com/tonesto7/nest-manager/master/LICENSE.md
 */

import java.text.SimpleDateFormat

preferences {  }

def devVer() { return "5.5.2" }

metadata {
	definition (name: "${textDevName()}", namespace: "tonesto7", author: "Anthony S.") {

		capability "Illuminance Measurement"
		capability "Sensor"
		capability "Refresh"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Ultraviolet Index"
		capability "Health Check"

		command "refresh"
		command "log"

		attribute "devVer", "string"
//		attribute "apiStatus", "string"
		attribute "debugOn", "string"
		attribute "devTypeVer", "string"
		attribute "lastUpdatedDt", "string"
		attribute "onlineStatus", "string"

// from original smartweather tile
		attribute "localSunrise", "string"
		attribute "localSunset", "string"
		attribute "city", "string"
		attribute "timeZoneOffset", "string"
		attribute "weather", "string"
		attribute "wind", "string"
		attribute "weatherIcon", "string"
		attribute "forecastIcon", "string"
		attribute "feelsLike", "string"
		attribute "percentPrecip", "string"

// original, not used
//		attribute "sunriseDate", "string"
//		attribute "sunsetDate", "string"

// smartweather tile 2.0
		attribute "winddirection", "string"
/*
		attribute "wind_gust", "string"
		attribute "winddirection_deg", "string"
		attribute "windinfo", "string"
		attribute "uv_index", "string"
		attribute "water", "string"
		attribute "percentPrecipToday", "string"
		attribute "percentPrecipLastHour", "string"
		attribute "pressure", "string"
*/
		//attribute "solarradiation", "string"
		attribute "visibility", "string"
	//	attribute "pressure_trend", "string"

		attribute "dewpoint", "string"
		attribute "wind_degrees", "string"

// nst manager data
		attribute "windgust", "string"
		attribute "pressure_mb", "string"
		attribute "pressure_in", "string"
		attribute "pressure_trend", "string"
		attribute "weatherObservedDt", "string"
		attribute "precip_today", "string"
		attribute "precip_lasthour", "string"

		attribute "alertKeys", "string"
		attribute "alert", "string"
		attribute "alert2", "string"
		attribute "alert3", "string"
		attribute "alert4", "string"

// old versions compatibility
		//attribute "uvindex", "string"
		//attribute "windDir", "string"
	}

	simulator { }

	tiles(scale: 2) {
		valueTile("temp2", "device.temperature", width: 2, height: 2, decoration: "flat") {
			state("default", label:'${currentValue}°', icon:"https://cdn.rawgit.com/tonesto7/nest-manager/master/Images/App/weather_icon.png",
					backgroundColors: getTempColors() )
		}
		valueTile("lastUpdatedDt", "device.lastUpdatedDt", width: 4, height: 1, decoration: "flat", wordWrap: true) {
			state("default", label: 'Data Last Received:\n${currentValue}')
		}
//		valueTile("apiStatus", "device.apiStatus", width: 2, height: 1, decoration: "flat", wordWrap: true) {
//                        state "Good", label: "API Status:\nOK"
//                        state "Sporadic", label: "API Status:\nISSUE ", backgroundColor: "#FFFF33"
//                        state "Outage", label: "API Status:\nISSUE ", backgroundColor: "#FFFF33"
//		}
		valueTile("onlineStatus", "device.onlineStatus", width: 2, height: 1, wordWrap: true, decoration: "flat") {
			state("default", label: 'Network Status:\n${currentValue}')
		}
		standardTile("refresh", "device.refresh", width:2, height:2, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/refresh_icon.png"
		}
		valueTile("devTypeVer", "device.devTypeVer", width: 2, height: 1, decoration: "flat") {
			state("default", label: 'Device Type:\nv${currentValue}')
		}
		valueTile("remind", "device.blah", inactiveLabel: false, width: 6, height: 2, decoration: "flat", wordWrap: true) {
			state("default", label: 'Reminder:\nHTML Content is Available in SmartApp')
		}
		htmlTile(name:"weatherHTML", action: "getWeatherHTML", width: 6, height: 16, whitelist: ["www.gstatic.com", "raw.githubusercontent.com", "cdn.rawgit.com"])
		valueTile("remind", "device.blah", inactiveLabel: false, width: 6, height: 2, decoration: "flat", wordWrap: true) {
			state("default", label: 'Reminder:\nHTML Content is Available in SmartApp', defaultState: true)
		}
		main ("temp2")
		details ("weatherHTML", "remind", "refresh")
	}
	preferences {
		input "resetHistoryOnly", "bool", title: "Reset History Data", description: "", displayDuringSetup: false
		input "resetAllData", "bool", title: "Reset All Stored Event Data", description: "", displayDuringSetup: false
		input "weatherAlertFilters", "text", title: "Block Weather Alerts containing the following", description: "Seperate each item with a comma", displayDuringSetup: false
	}
}

mappings {
	path("/getWeatherHTML") {action: [GET: "getWeatherHTML"]}
}

void checkStateClear() {
	//Logger("checkStateClear...")
	def before = getStateSizePerc()
	if(!state?.resetAllData && resetAllData) {
		Logger("checkStateClear...Clearing ALL")
		def data = getState()?.findAll { !(it?.key in ["eric"]) }
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
			(it?.key in ["today", "temperatureTable", "dewpointTable", "humidityTable", "temperatureTableYesterday", "dewpointTableYesterday", "humidityTableYesterday"])
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

def getWAlertFilters() {
	def waf = settings?.weatherAlertFilters ?: []
	def res = waf != [] ? waf?.toString().tokenize(";,") : []
	state?.weatherAlertFilters = res
}

def initialize() {
	Logger("initialized...")
	state?.healthInRepair = false
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 2000) {
		state.updatedLastRanAt = now()
		verifyHC()
		state.isInstalled = true
		getWAlertFilters()
		if(state?.shortcutAppId) { parent?.updShortcutAppId(atomicState?.shortcutAppId) }
	} else {
		log.trace "initialize(): Ran within last 2 seconds - SKIPPING"
	}
}

void installed() {
	Logger("installed...")
	runIn(5, "initialize", [overwrite: true] )
}

void updated() {
	Logger("updated...")
	runIn(5, "initialize", [overwrite: true] )
}

def useTrackedHealth() { return state?.useTrackedHealth ?: false }

def getHcTimeout() {
	def to = state?.hcTimeout
	return ((to instanceof Integer) ? to.toInteger() : 60)*60
}

void verifyHC() {
	if(useTrackedHealth()) {
		def timeOut = getHcTimeout()
		if(!val || val.toInteger() != timeOut) {
			Logger("verifyHC: Updating Device Health Check Interval to $timeOut")
			sendEvent(name: "checkInterval", value: timeOut, data: [protocol: "cloud"], displayed: false)
		}
	} else {
		sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson(["protocol":"cloud", "scheme":"untracked"]), displayed: false)
	}
	repairHealthStatus(null)
}

def modifyDeviceStatus(status) {
	if(status == null) { return }
	def val = status.toString() == "offline" ? "offline" : "online"
	if(val != getHealthStatus(true)) {
		sendEvent(name: "DeviceWatch-DeviceStatus", value: val.toString(), displayed: false, isStateChange: true)
		Logger("UPDATED: DeviceStatus Event: '$val'")
	}

	def prevOnlineStat = device.currentState("onlineStatus")?.value
	def onlineStat = val
	state?.onlineStatus = onlineStat
	if(isStateChange(device, "onlineStatus", onlineStat?.toString())) {
		Logger("UPDATED | Online Status is: (${onlineStat}) | Original State: (${prevOnlineStat})")
		sendEvent(name: "onlineStatus", value: onlineStat, descriptionText: "Online Status is: ${onlineStat}", displayed: true, isStateChange: true, state: onlineStat)
	} else { LogAction("Online Status is: (${onlineStat}) | Original State: (${prevOnlineStat})") }
}

def ping() {
	LogAction("Ping", "info", true)
	keepAwakeEvent()
}

def keepAwakeEvent() {
	def lastDt = state?.lastUpdatedDtFmt
	if(lastDt) {
		def ldtSec = getTimeDiffSeconds(lastDt)
		if(ldtSec < 3600) {
			LogAction("keepAwakeEvent: ldtSec: $ldtSec", "debug", true)
			poll()
		}
	}
}

void repairHealthStatus(data) {
	Logger("repairHealthStatus($data)")
	if(state?.hcRepairEnabled != false) {
		if(data?.flag) {
			sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
			state?.healthInRepair = false
		} else {
			state.healthInRepair = true
			sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
			runIn(7, repairHealthStatus, [data: [flag: true]])
		}
	}
}

def parse(String description) {
	LogAction("Parsing '${description}'")
}

def configure() { }

def compileForC() {
	def retVal = false   // if using C mode, set this to true so that enums and colors are correct (due to ST issue of compile time evaluation)
	return retVal
}

def getTempColors() {
	def colorMap
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

def poll() {
	Logger("Polling parent...")
	parent.refresh(this)
}

void refresh() {
	poll()
}

// parent calls this method to queue data.
// goal is to return to parent asap to avoid execution timeouts

def generateEvent(Map eventData) {
	//LogAction("generateEvent Parsing data ${eventData}", "trace")
	state.eventData = eventData  // this data size is much larger than 2500 bytes
	runIn(21, "processEvent", [overwrite: true] )
}

void processEvent() {

	if(state?.swVersion != devVer()) {
		initialize()
		state.swVersion = devVer()
		state?.shownChgLog = false
		state.androidDisclaimerShown = false
	}
	def eventData = state?.eventData
	//LogAction("processEvent Parsing data ${eventData}", "trace")
	checkStateClear()
//	try {
		LogAction("------------START OF API RESULTS DATA------------", "warn")
		if(eventData) {
			state.isBeta = eventData?.isBeta == true ? true : false
			state.hcRepairEnabled = eventData?.hcRepairEnabled == true ? true : false
			state.useMilitaryTime = eventData?.mt ? true : false
			state.showLogNamePrefix = eventData?.logPrefix == true ? true : false
			state.enRemDiagLogging = eventData?.enRemDiagLogging == true ? true : false
			state.healthMsg = eventData?.healthNotify?.healthMsg == true ? true : false
			state.healthMsgWait = eventData?.healthNotify?.healthMsgWait
			state.showGraphs = eventData?.showGraphs == true ? true : false
			if(eventData?.allowDbException) { state?.allowDbException = eventData?.allowDbException == false ? false : true }
			debugOnEvent(eventData?.debug ? true : false)
			deviceVerEvent(eventData?.latestVer.toString())
			state.tempUnit = getTemperatureScale()
			if(eventData.hcTimeout && (state?.hcTimeout != eventData?.hcTimeout || !state?.hcTimeout)) {
				state.hcTimeout = eventData?.hcTimeout
				verifyHC()
			}
			state.clientBl = eventData?.clientBl == true ? true : false
			state.mobileClientType = eventData?.mobileClientType
			state.nestTimeZone = eventData?.tz ?: null
			state.weatherAlertNotify = !eventData?.weathAlertNotif ? false : true
			//apiStatusEvent(eventData?.apiIssues)

			def curWeather = eventData?.data?.weatCond ?: null
			if(curWeather == null ) {
				Logger("There is an issue getting the weather condition data", "warn")
				return
			} else { state.curWeather = curWeather }

			if(getWeatherAstronomy(curWeather)) {
				getWeatherConditions(curWeather, eventData?.data?.weatLocation)
				getWeatherForecast(eventData?.data?.weatForecast)
				getWeatherAlerts(eventData?.data?.weatAlerts)
			}

			checkHealth()
			state?.devBannerData = eventData?.devBannerData ?: null
			lastUpdatedEvent(true)
		}
		//LogAction("Device State Data: ${getState()}")
		//return null
/*
	}
	catch (ex) {
		log.error "generateEvent Exception:", ex
		exceptionDataHandler(ex?.message, "generateEvent")
	}
*/
	state.eventData = null
}

def getStateSize()	{ return state?.toString().length() }
def getStateSizePerc()	{ return (int) ((stateSize/100000)*100).toDouble().round(0) }
def getDevTypeId() { return device?.getTypeId() }

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def getDeviceStateData() {
	return getState()
}

def getTimeZone() {
	def tz = null
	if (!state?.nestTimeZone) { tz = location?.timeZone }
	else { tz = TimeZone.getTimeZone(state?.nestTimeZone) }
	if(!tz) { Logger("getTimeZone: Hub or Nest TimeZone is not found ...", "warn", true) }
	return tz
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
				//LogAction("comparing $numA and $numB")
				if (verA[i]?.toInteger() != verB[i]?.toInteger()) {
					return verA[i]?.toInteger() <=> verB[i]?.toInteger()
				}
			}
			verA?.size() <=> verB?.size()
		}
		result = (latestVer == newVer) ? true : false
	}
	LogAction("newVer: $newVer | curVer: $curVer | newestVersion: ${latestVer} | result: $result")
	return result
}

def deviceVerEvent(ver) {
	def curData = device.currentState("devTypeVer")?.value.toString()
	def pubVer = ver ?: null
	def dVer = devVer() ?: null
	state?.updateAvailable = isCodeUpdateAvailable(pubVer, dVer)
	def newData = state.updateAvailable ? "${dVer}(New: v${pubVer})" : "${dVer}" as String
	state?.devTypeVer = newData
	if(isStateChange(device, "devVer", dVer.toString())) {
		sendEvent(name: 'devVer', value: dVer, displayed: false)
	}
	if(isStateChange(device, "devTypeVer", newData.toString())) {
		Logger("UPDATED | Device Type Version is: (${newData}) | Original State: (${curData})")
		sendEvent(name: 'devTypeVer', value: newData, displayed: false)
	} else { LogAction("Device Type Version is: (${newData}) | Original State: (${curData})") }
}

def debugOnEvent(debug) {
	def val = device.currentState("debugOn")?.value
	def dVal = debug ? "on" : "off"
	state?.debugStatus = dVal
	state?.debug = debug.toBoolean() ? true : false
	if(isStateChange(device, "debugOn", dVal.toString())) {
		Logger("UPDATED | Device Debug Logging is: (${dVal}) | Original State: (${val})")
		sendEvent(name: 'debugOn', value: dVal, displayed: false)
	} else { LogAction("Device Debug Logging is: (${dVal}) | Original State: (${val})") }
}

def lastUpdatedEvent(sendEvt=false) {
	def now = new Date()
	def formatVal = state.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	tf.setTimeZone(getTimeZone())
	def lastDt = "${tf?.format(now)}"
	def lastUpd = device.currentState("lastUpdatedDt")?.value
	state?.lastUpdatedDt = lastDt?.toString()
	state?.lastUpdatedDtFmt = getDtNow()
	if(sendEvt) {
		LogAction("Last Parent Refresh time: (${lastDt}) | Previous Time: (${lastUpd})")
		sendEvent(name: 'lastUpdatedDt', value: lastDt?.toString(), displayed: false, isStateChange: true)
	}
}

/*
def apiStatusEvent(issue) {
	def curStat = device.currentState("apiStatus")?.value
	def newStat = issue
	if(isStateChange(device, "apiStatus", newStat.toString())) {
		Logger("UPDATED | API Status is: (${newStat}) | Original State: (${curStat})")
		sendEvent(name: "apiStatus", value: newStat, descriptionText: "API Status is: ${newStat}", displayed: true, isStateChange: true, state: newStat)
	} else { LogAction("API Status is: (${newStat}) | Original State: (${curStat})") }
}
*/

def humidityEvent(humidity) {
	def hum = device.currentState("humidity")?.value
	if(isStateChange(device, "humidity", humidity.toString())) {
		LogAction("UPDATED | Humidity is (${humidity}) | Original State: (${hum})")
		sendEvent(name:'humidity', value: humidity, unit: "%", descriptionText: "Humidity is ${humidity}" , displayed: false, isStateChange: true)
	} else { LogAction("Humidity is (${humidity}) | Original State: (${hum})") }
}

def illuminanceEvent(illum) {
	if(illum != null) {
		def cur = device.currentState("illuminance")?.value.toString()
		if(isStateChange(device, "illuminance", illum.toString())) {
			LogAction("UPDATED | Illuminance is (${illum}) | Original State: (${cur})")
			sendEvent(name:'illuminance', value: illum, unit: "lux", descriptionText: "Illuminance is ${illum}" , displayed: false, isStateChange: true)
		} else { LogAction("Illuminance is (${illum}) | Original State: (${cur})") }
	}
}

def dewpointEvent(Double tempVal) {
	def temp = device.currentState("dewpoint")?.value.toString()
	def rTempVal = wantMetric() ? tempVal.round(1) : tempVal.round(0).toInteger()
	if(isStateChange(device, "dewpoint", rTempVal.toString())) {
		LogAction("UPDATED | DewPoint Temperature is (${rTempVal}) | Original Temp: (${temp})")
		sendEvent(name:'dewpoint', value: rTempVal, unit: state?.tempUnit, descriptionText: "Dew point Temperature is ${rTempVal}" , displayed: false, isStateChange: true)
	} else { LogAction("DewPoint Temperature is (${rTempVal}) | Original Temp: (${temp})") }
}

def temperatureEvent(Double tempVal, Double feelsVal) {
	def temp = device.currentState("temperature")?.value.toString()
	def rTempVal = wantMetric() ? tempVal.round(1) : tempVal.round(0).toInteger()
	def rFeelsVal = wantMetric() ? feelsVal.round(1) : feelsVal.round(0).toInteger()
	if(isStateChange(device, "temperature", rTempVal.toString())) {
		LogAction("UPDATED | Temperature is (${rTempVal}) | Original Temp: (${temp})")
		sendEvent(name:'temperature', value: rTempVal, unit: state?.tempUnit, descriptionText: "Ambient Temperature is ${rTempVal}", displayed: true)
	} else { LogAction("Temperature is (${rTempVal}) | Original Temp: (${temp})") }
	if(isStateChange(device, "feelsLike", rFeelsVal.toString())) {
		sendEvent(name:'feelsLike', value: rFeelsVal, unit: state?.tempUnit, descriptionText: "Feels Like Temperature is ${rFeelsVal}" , displayed: false)
	}
}

def getTemp() {
	if ( wantMetric() ) {
		return "${state?.curWeatherTemp_c}°C"
	} else {
		return "${state?.curWeatherTemp_f}°F"
	}
	return 0
}

def getDewpoint() {
	if ( wantMetric() ) {
		return "${state?.curWeatherDewPoint_c}°C"
	} else {
		return "${state?.curWeatherDewPoint_f}°F"
	}
	return 0
}

def getCurWeather() {
	return state.curWeather ?: 0
}

def getHumidity() {
	return device.currentValue("humidity") ?: 0
}

def wantMetric() { return (state?.tempUnit == "C") }

def getHealthStatus(lower=false) {
	def res = device?.getStatus()
	if(lower) { return res.toString().toLowerCase() }
	return res.toString()
}

def healthNotifyOk() {
	def lastDt = state?.lastHealthNotifyDt
	if(lastDt) {
		def ldtSec = getTimeDiffSeconds(lastDt)
		def t0 = state.healthMsgWait ?: 3600
		LogAction("healtNotifyOk: ldtSec: $ldtSec", "debug", true)
		if(ldtSec < t0) {
			return false
		}
	}
	return true
}

def checkHealth() {
	def isOnline = (getHealthStatus() == "ONLINE") ? true : false
	if(isOnline || state?.healthMsg != true || state?.healthInRepair == true) { return }
	if(healthNotifyOk()) {
		def now = new Date()
		parent?.deviceHealthNotify(this, isOnline)
		state.lastHealthNotifyDt = getDtNow()
	}
}

/************************************************************************************************
|									Weather Info for Tiles										|
*************************************************************************************************/

				//getWeatherConditions(curWeather, eventData?.data?.weatLocation))
def getWeatherConditions(weatData, weatLocation) {
//	try {
		if(!weatData || !weatLocation) {
			Logger("There is an issue getting the weather condition data", "warn")
			return
		} else {
			def cur = weatData
			if(cur) {
				state.curWeatherTemp_f = !wantMetric() ? Math.round(cur?.temperature).toInteger() : Math.round( (cur?.temperature * 9/5 + 32) ).toInteger()
				state.curWeatherTemp_c = wantMetric() ? Math.round(cur?.temperature.toDouble()) : Math.round( ( (cur?.temperature - 32.0) * (5/9) )).toDouble()

				state.curFeelsTemp_f = !wantMetric() ? Math.round(cur?.temperatureFeelsLike).toInteger() : Math.round( (cur?.temperatureFeelsLike * 9/5 + 32) ).toInteger()
				state.curFeelsTemp_c = wantMetric() ? Math.round(cur?.temperatureFeelsLike.toDouble()) : Math.round( ( (cur?.temperatureFeelsLike - 32.0) * (5/9) )).toDouble()

				state.curWeatherHum = cur?.relativeHumidity
				def t0 = weatLocation?.location?.neighborhood != null ?  weatLocation?.location?.neighborhood.toString() + " " : ""
				def t1 = weatLocation?.location?.adminDistrictCode != null ? weatLocation?.location?.adminDistrictCode.toString() : (weatLocation?.location.countryCode ? weatLocation?.location.countryCode.toString() : "")
				state.curWeatherLoc = t0.toString() + weatLocation?.location?.city.toString() + ", " + t1.toString()
				state.curWeatherCond = cur?.wxPhraseMedium.toString()
				state.curWeatherIcon = cur?.iconCode
				state.zipCode = weatLocation?.location?.postalCode.toString()

				temperatureEvent( (wantMetric() ? state?.curWeatherTemp_c : state?.curWeatherTemp_f), (wantMetric() ? state?.curFeelsTemp_c : state?.curFeelsTemp_f) )

				humidityEvent(state?.curWeatherHum)
				illuminanceEvent(estimateLux(state?.curWeatherIcon))

/*
				def hum =
				def Tc =
				state.curWeatherDewPoint_c = estimateDewPoint(hum,Tc)
*/

				state.curWeatherDewPoint_c = wantMetric() ? Math.round(cur?.temperatureDewPoint.toDouble()) : Math.round( ( (cur?.temperatureDewPoint - 32.0) * (5/9) )).toDouble()
				if(state.curWeatherTemp_c < state.curWeatherDewPoint_c) { state.curWeatherDewPoint_c = state.curWeatherTemp_c }
				state.curWeatherDewPoint_f = Math.round(state.curWeatherDewPoint_c * 9.0/5.0 + 32.0)
				dewpointEvent((wantMetric() ? state?.curWeatherDewPoint_c : state?.curWeatherDewPoint_f))

				getSomeData(true)
				sendEvent(name: "weather", value: state.curWeatherCond)
				sendEvent(name: "weatherIcon", value: state?.curWeatherIcon, displayed:false)
				def wspeed = 0.0
				def wgust = 0.0
				def precip = 0.0
				def precip1hr = 0.0

				wspeed = Math.round(cur?.windSpeed)
				wgust = Math.round(cur?.windGust ?: 0)
				precip = Math.round(cur?.precip24Hour)
				precip1hr = Math.round(cur?.precip1Hour)
				sendEvent(name: "visibility", value: Math.round(cur?.visibility), unit: wantMetric() ? "km" : "miles", displayed:false)
				sendEvent(name: "wind", value: wspeed as String, unit: wantMetric() ? "KPH" : "MPH")
				sendEvent(name: "windgust", value: wgust as String, unit: wantMetric() ? "KPH" : "MPH")
				sendEvent(name: "precip_today", value: precip as String, unit: wantMetric() ? "mm" : "in")
				sendEvent(name: "precip_lasthour", value: precip1hr as String, unit: wantMetric() ? "mm" : "in")

				def wgustStr = wgust.toString()
				if(wantMetric()) {
					wspeed += " KPH"
					wgustStr = " KPH"
				} else {
					wspeed += " MPH"
					wgustStr += " MPH"
				}
				def wdir = cur?.windDirectionCardinal
				//sendEvent(name: "windDir", value: wdir) // obsolete;  for transition time
				sendEvent(name: "winddirection", value: wdir)

				sendEvent(name: "wind_degrees", value: cur?.windDirection)
				state.windStr = "From the ${wdir} at ${wspeed} ${ wgust != 0 ? "Gusting to ${wgustStr}" : ""}"

				sendEvent(name: "pressure_mb", value: cur?.pressureMeanSeaLevel, displayed:false) // MB
				t0 = cur?.pressureMeanSeaLevel.toDouble() * 0.0295301
				sendEvent(name: "pressure_in", value: t0.round(1), displayed:false)
				sendEvent(name: "pressure_trend", value: cur?.pressureTendencyTrend, displayed:false)

				sendEvent(name: "timeZoneOffset", value: weatLocation?.location?.ianaTimeZone)
				def cityValue = state?.curWeatherLoc
				sendEvent(name: "city", value: cityValue)

				//sendEvent(name: "uvindex", value: cur?.uvIndex) // obsolete
				sendEvent(name: "ultravioletIndex", value: cur?.uvIndex, displayed:false)
				//sendEvent(name: "solarradiation", value: cur?.current_observation?.solarradiation) // not available from twc

				def obsrDt = cur?.validTimeLocal
				if(obsrDt) {
					def newDt = formatDt(cur?.validTimeUtc.toLong()*1000l as long)
					if(isStateChange(device, "weatherObservedDt", newDt.toString())) {
						sendEvent(name: "weatherObservedDt", value: newDt)
					}
					//log.debug "newDt: $newDt"
					def curDt = Date.parse("E MMM dd HH:mm:ss z yyyy", getDtNow())
					def lastDt = Date.parse("E MMM dd HH:mm:ss z yyyy", newDt?.toString())
					if((lastDt + 60*60*1000) < curDt) {
						modifyDeviceStatus("offline")
					} else {
						modifyDeviceStatus("online")
					}
				} else {
					modifyDeviceStatus("offline")
				}

				LogAction("${state?.curWeatherLoc} Weather | humidity: ${state?.curWeatherHum} | temp_f: ${state?.curWeatherTemp_f} | temp_c: ${state?.curWeatherTemp_c} | Current Conditions: ${state?.curWeatherCond}")
			}
		}
/*
	}
	catch (ex) {
		log.error("getWeatherConditions Exception:", ex)
		exceptionDataHandler(ex?.message, "getWeatherConditions")
	}
*/
}

				//getWeatherForecast(eventData?.data?.weatForecast)
def getWeatherForecast(Map weatForecast) {
//	try {
		if(!weatForecast) {
			Logger("There is an issue getting the weather forecast", "warn")
			return
		} else {
			def cur = weatForecast
			if(cur) {
				state.curForecast = cur
				//LogAction("cur: $cur")
				def icon = cur?.daypart[0].iconCode[1]  //f1[0].icon
				def value = cur?.daypart[0].precipChance[1]  //f1[0].pop as String // as String because of bug in determining state change of 0 numbers
				sendEvent(name: "percentPrecip", value: value, unit: "%")
				sendEvent(name: "forecastIcon", value: icon, displayed: false)
			}
		}
/*
	}
	catch (ex) {
		log.error("getWeatherForecast Exception:", ex)
		exceptionDataHandler(ex?.message, "getWeatherForecast")
	}
*/
}

def getWeatherAstronomy(weatData) {
	if(!weatData) {
		Logger("There is an issue getting the weather astronomy data", "warn")
	} else {
		state.remove("curAstronomy")
		if(getSunriseSunset(weatData)) {
			sendEvent(name: "localSunrise", value: state.localSunrise, descriptionText: "Sunrise today is at ${state.localSunrise}")
			sendEvent(name: "localSunset", value: state.localSunset, descriptionText: "Sunset today at is ${state.localSunset}")
			return true
		}
	}
	return false
}

def getSunriseSunset(weatData) {
	// Sunrise / sunset

	long a = weatData?.sunriseTimeUtc
	long b = weatData?.sunsetTimeUtc
	if(a == null || b == null) { Logger("observation issue") ; return false }

	//def sunriseDate = Date.parse("yyyy-MM-dd'T'HH:mm'Z'", a)
	//def sunsetDate = Date.parse("yyyy-MM-dd'T'HH:mm'Z'", b)
	long sunriseDate = a * 1000
	state.sunriseDate = sunriseDate

	long sunsetDate = b * 1000
	state.sunsetDate = sunsetDate

	def tf = new java.text.SimpleDateFormat("h:mm a")
	tf.setTimeZone( getTimeZone() )
	def localSunrise = "${tf.format(sunriseDate)}"
	def localSunset = "${tf.format(sunsetDate)}"
	state.localSunrise = localSunrise
	state.localSunset = localSunset

	return true
}

def clearAlerts() {
	def newKeys = []
	sendEvent(name: "alertKeys", value: newKeys.encodeAsJSON(), displayed: false)

	def noneString = ""
	def cntr = 1
	def aname = "alert"
	while (cntr <= 4) {
		sendEvent(name: "${aname}", value: noneString, descriptionText: "${device.displayName} has no current weather alerts")

		state.remove("walert${cntr}")	// = noneString
		state.remove("walertMessage${cntr}")  // = null

		cntr += 1
		aname = "alert${cntr}"
	}
	state.lastWeatherAlertNotif = []
	state.walertCount = 0

	// below are old variables from prior releases
	state.remove("walert")
	state.remove("walertMessage")
}

def getWeatherAlerts(weatData) {
	try {
		if(!weatData) {
			//Logger("There is an issue getting the weather alert data", "warn")
			clearAlerts()
			return
		} else {
			def cur = weatData
			if(cur) {
				state.curAlerts = cur
				//LogAction("cur: $cur")
				def alerts = cur  // ?.alerts
				def newKeys = alerts?.collect{it.messageType + it.eventTrackingNumber.toString() + it.expireTimeUTC.toString()} ?: []
				//LogAction("${device.displayName}: newKeys: $newKeys")
				//LogAction("${device.currentState("alertKeys")}", "trace")
				def oldKeys = device.currentState("alertKeys")?.jsonValue
				//LogAction("${device.displayName}: oldKeys: $oldKeys")

				def noneString = ""

				if (oldKeys == null) { oldKeys = [] }

				if(state?.walert != null) { oldKeys = []; state.remove("walert") }	// this is code for this upgrade

				if(newkeys == [] && !(oldKeys == [])) {
					clearAlerts()
				}
				else if (newKeys != oldKeys) {
					clearAlerts()

					sendEvent(name: "alertKeys", value: newKeys.encodeAsJSON(), displayed: false)

					def totalAlerts = newKeys.size()
					def cntr = 1
					def newAlerts = false
					def newWalertNotif = []

					getWAlertFilters()
					alerts.each { alert ->
						def thisKey = alert.messageType + alert.eventTrackingNumber.toString() + alert.expireTimeUTC.toString()
						if(alert?.eventDescription == null) {
							Logger("null alert.eventDescription")
							return true
						}
						if(alert?.headlineText == null) {
							Logger("null alert.headlineText")
							return true
						}
						def msg = alert.headlineText // "${alert.description} from ${alert.date} until ${alert.expires}"
						def aname = "alert"
						if(cntr > 1) {
							aname = "alert${cntr}"
						}
						def statechange = oldKeys.contains(thisKey) ? false : true
						sendEvent(name: "${aname}", value: pad(alert.eventDescription), descriptionText: msg, displayed: true)

						if(statechange) { newAlerts = true }

						def curAlertDetail = getTwcAlertDetail(alert.detailKey.toString())
						def walert = pad(alert.eventDescription) // description
						def walertMessage = pad(curAlertDetail.alertDetail.texts[0].description) // message

						// Try to format message some
						walertMessage = walertMessage.replaceAll(/\.\.\. \.\.\./, '\n ')
						walertMessage = walertMessage.replaceAll(/\.\.\./, ' ')
						walertMessage = walertMessage.replaceAll(/\*/, '\n *')
						walertMessage = walertMessage.replaceAll(/\n\n\n/, '\n\n')
						walertMessage = walertMessage.replaceAll(/\n\n\n/, '\n\n')
						walertMessage = walertMessage.replaceAll(/\n\n\n/, '\n\n')
						walertMessage = walertMessage.replaceAll(/\n\n/, '<br> ')
						walertMessage = walertMessage.replaceAll(/\n/, '<br> ')

						state."walert${cntr}" = walert
						state."walertMessage${cntr}" = walertMessage.take(700)

						def t0 = pad(curAlertDetail.alertDetail.texts[0].description) // message

						if(state?.weatherAlertNotify) {
							if(statechange && !(thisKey in state?.lastWeatherAlertNotif)) {
								def waf = state?.weatherAlertFilters?.findAll { t0.contains(it) }
								if(!waf) {
									sendNofificationMsg("Warn", "WEATHER ALERT: ${t0}")
								}
							}
							newWalertNotif << thisKey
						}
						state.walertCount = cntr

						if(cntr <= 4) { cntr += 1 } else { log.error "Many Alerts"; return true }
					}
					state?.lastWeatherAlertNotif = newWalertNotif

					if(totalAlerts == 0 && device.currentValue("alert") != noneString) {
						log.error "clearing alerts again"
						clearAlerts()
					}
				}
			}
		}
	}
	catch (ex) {
		log.error("getWeatherAlerts Exception:", ex)
		exceptionDataHandler(ex?.message, "getWeatherAlerts")
	}
}

private pad(String s, size = 25) {
	try {
		def n = (size - s.size()) / 2
		if (n > 0) {
			def sb = ""
			n.times {sb += " "}
			sb += s
			n.times {sb += " "}
			return sb
		}
		else {
			return s
		}
	}
	catch (ex) {
		log.error("pad Exception:", ex)
		exceptionDataHandler(ex?.message, "pad")
	}
}

private estimateDewPoint(double rh,double t) {
	def L = Math.log(rh/100)
	def M = 17.27 * t
	def N = 237.3 + t
	def B = (L + (M/N)) / 17.27
	def dp = (237.3 * B) / (1 - B)

	def dp1 = 243.04 * ( Math.log(rh / 100) + ( (17.625 * t) / (243.04 + t) ) ) / (17.625 - Math.log(rh / 100) - ( (17.625 * t) / (243.04 + t) ) )
	def ave = (dp + dp1)/2
	//LogAction("dp: ${dp.round(1)} dp1: ${dp1.round(1)} ave: ${ave.round(1)}")
	ave = dp1
	return ave.round(1)
}

def luxUpdate() {
	LogAction("luxUpdate", "trace")
	poll()
}

private estimateLux(weatherIcon) {
	//LogAction("estimateLux ( ${weatherIcon} )", "trace")
	try {
		if(!weatherIcon || !state?.sunriseDate || !state?.sunsetDate ) {
			Logger("estimateLux: Weather Data missing...", "warn")
			Logger("state.sunriseDate: ${state?.sunriseDate} state.sunsetDate: ${state?.sunsetDate}")
			return null
		} else {
			def lux = 0
			def twilight = 20 * 60 * 1000 // 20 minutes
			def now = new Date().time
			if(now == null) { Logger("got null for new Date()") }
			def sunriseDate = (long) state?.sunriseDate
			def sunsetDate = (long) state?.sunsetDate
			if(sunriseDate == null || sunsetDate == null) { Logger("got null for sunriseDate or sunsetDate") }
			sunriseDate -= twilight
			sunsetDate += twilight
			def oneHour = 1000 * 60 * 60
			def fiveMin = 1000 * 60 * 5
			if (now > sunriseDate && now < sunsetDate) {
				//day
				switch(getWeatCondFromUrl(weatherIcon)) {
					case 'tstorms':
						lux = 200
						break
					case ['cloudy', 'fog', 'rain', 'sleet', 'snow', 'flurries',
						 'chanceflurries', 'chancerain', 'chancesleet',
						'chancesnow', 'chancetstorms']:
						lux = 1000
						break
					case 'mostlycloudy':
						lux = 2500
						break
					case ['partlysunny', 'partlycloudy', 'hazy']:
						lux = 7500
						break
					default:
						//sunny, clear
						lux = 10000
				}

				//adjust for dusk/dawn
				def afterSunrise = now - sunriseDate
				def beforeSunset = sunsetDate - now

				//LogAction("now: $now afterSunrise: $afterSunrise beforeSunset: $beforeSunset oneHour: $oneHour")
				if(afterSunrise < oneHour) {
					//dawn
					lux = (long)(lux * (afterSunrise/oneHour))
					runIn(5*60, "luxUpdate", [overwrite: true])
				} else if (beforeSunset < oneHour) {
					//dusk
					//LogAction("dusk", "trace")
					lux = (long)(lux * (beforeSunset/oneHour))
					runIn(5*60, "luxUpdate", [overwrite: true])
				} else if (beforeSunset < (oneHour*2)) {
					//LogAction("before dusk", "trace")
					def newTim = (beforeSunset - oneHour)/1000 /// seconds
					if(newTim > 0 && newTim < 3600) {
						runIn(newTim, "luxUpdate", [overwrite: true])
					}
				}
			} else {
				if( (now > (sunriseDate-oneHour)) && now < sunsetDate) {
					def newTim = (sunriseDate - now)/1000 /// seconds
					if(newTim > 0 && newTim < 3600) {
						runIn(newTim, "luxUpdate", [overwrite: true])
					}
				}
				//night - always set to 10 for now
				//could do calculations for dusk/dawn too
				lux = 10
			}
			return lux
		}
	}
	catch (ex) {
		log.warn "state.sunriseDate: ${state?.sunriseDate}"
		log.warn "state.sunsetDate: ${state?.sunsetDate}"
		log.error "estimateLux Exception:", ex
		exceptionDataHandler(ex?.message, "estimateLux")
	}
	return null
}

def sendNofificationMsg(msgType, msg, recips = null, sms = null, push = null) {
	if(msg && msgType) {
		parent?.sendMsg(msgType, msg, true, recips, sms, push)
		//LogAction("Send Push Notification to $recips...", "info", true)
	}
}

def getDtNow() {
	def now = new Date()
	return formatDt(now)
}

def formatDt(dt) {
	def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	else {
		Logger("SmartThings TimeZone is not found or is not set... Please Try to open your ST location and Press Save...", "warn", true)
	}
	return tf.format(dt)
}

def convertRfc822toDt(dt) {
	if(dt) {
		def tf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a")
		if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
		def result = tf.format(Date.parse("EEE, dd MMM yyyy HH:mm:ss Z", dt))
		return result
	}
	return null
}
/************************************************************************************************
|										LOGGING FUNCTIONS										|
*************************************************************************************************/
void Logger(msg, logType = "debug") {
	def smsg = state?.showLogNamePrefix ? "${device.displayName} (${devVer()}) | ${msg}" : "${msg}"
	if(state?.enRemDiagLogging) {
		parent.saveLogtoRemDiagStore(smsg, logType, "Weather")
	} else {
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
	}
}

// Local Application Logging
void LogAction(msg, logType = "debug", frc=false) {
	if(state?.debug || frc) {
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
			parent?.sendChildExceptionData("weather", devVer(), msgString, methodName)
		}
	}
}

def getFileBase64(url, preType, fileType) {
	try {
		def params = [uri: url, contentType: "$preType/$fileType"]
		httpGet(params) { resp ->
			if(resp?.status == 200) {
				if(resp.data) {
					def respData = resp?.data
					byte[] byteData = resp?.data?.getBytes()
					String enc = byteData?.encodeBase64()
					// log.debug "enc: ${enc}"
					return enc ? "data:${preType}/${fileType};base64,${enc?.toString()}" : null
				}
			} else {
				LogAction("getFileBase64 Resp: ${resp?.status} ${url}", "error")
				exceptionDataHandler("resp ${ex?.response?.status} ${url}", "getFileBase64")
				return null
			}
		}
	} catch (ex) {
		if(ex instanceof groovyx.net.http.ResponseParseException) {
			if(ex?.statusCode != 200) {
				LogAction("getFileBase64 Resp: ${ex?.statusCode} ${url}", "error")
				log.error "getFileBase64 Exception:", ex
			}
		} else if(ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
			LogAction("getFileBase64 Resp: ${ex?.response?.status} ${url}", "error")
			exceptionDataHandler("${ex?.response?.status} ${url}", "getFileBase64")
		} else {
			log.error "getFileBase64 Exception:", ex
			exceptionDataHandler(ex, "getFileBase64")
		}
		return null
    }
}

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
def gitBranch()		{ return state?.isBeta ? "beta" : "master" }
def gitPath()		{ return "${gitRepo()}/${gitBranch()}"}
def devVerInfo()	{ return getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/Data/changelog_weath.txt", contentType: "text/plain; charset=UTF-8"], "changelog") }

/*
def getWeatherIcon(b64=true) {
	try {
				//state.curWeatherIcon = cur?.iconCode
		return b64 ? getFileBase64(state?.curWeather?.current_observation?.icon_url, "image", "gif") : state?.curWeather?.current_observation?.icon_url
	}
	catch (ex) {
		log.error "getWeatherIcon Exception:", ex
		exceptionDataHandler(ex?.message, "getWeatherIcon")
	}
}
*/

def getWeatCondFromUrl(url) {
	def ilist = [
		"tstorms",
		"tstorms",
		"tstorms",
		"tstorms",
		"tstorms", // 4
		"chancesnow",
		"chancesleet",
		"chancesnow",
		"chancesleet",
		"chancerain",	// 9
		"chancesleet",
		"chancerain",
		"rain",
		"flurries",
		"snow",		// 14
		"snow",
		"snow",
		"tstorms",
		"sleet",
		"hazy",		// 19
		"fog",
		"hazy",
		"hazy",
		"clear",
		"clear",	// 24
		"chancesleet",
		"cloudy",
		"mostlycloudy",
		"mostlycloudy",
		"partlycloudy",  // 29
		"partlycloudy",
		"clear",
		"sunny",
		"clear",
		"mostlysunny",   // 34
		"rain",
		"sunny",
		"chancetstorms",
		"tstorms",
		"chancerain",	// 39
		"rain",
		"chancesnow",
		"snow",
		"snow",
		"unknown",	// 44
		"chancerain",
		"chanceflurries",
		"chancetstorms",
		"unknown",
		"unknown" ]

	return ilist[url]
}

def getWeatherImg(cond) {
//	try {
		//def newCond = getWeatCondFromUrl(cond)
		//def url = "https://cdn.rawgit.com/tonesto7/nest-manager/master/Images/Weather/icons/black/${getWeatCondFromUrl(cond) ?: "unknown"}.svg"
// https://console.bluemix.net/docs/api/content/services/Weather/images/30.png
		def url = "https://cdn.rawgit.com/tonesto7/nest-manager/master/Images/Weather/icons/black/${getWeatCondFromUrl(cond) ?: "unknown"}.svg"
		return url
/*
	}
	catch (ex) {
		log.error "getWeatherImg Exception:", ex
		exceptionDataHandler(ex?.message, "getWeatherImg")
	}
*/
}

def getFavIcon() {
//	try {
		return "https://cdn.rawgit.com/tonesto7/nest-manager/master/Images/App/weather_icon.ico"
/*
	}
	catch (ex) {
		log.error "getFavIcon Exception:", ex
		exceptionDataHandler(ex?.message, "getFavIcon")
	}
*/
}

def getFeelslike() {
	if ( wantMetric() ) {
		return "${state?.curFeelsTemp_c}°C"
	} else {
		return "${state?.curFeelsTemp_f}°F"
	}
}

def getPrecip() {
	def tstr = device.currentState("precip_today")?.value.toString()
	if(wantMetric()) {
		return "${tstr} mm"
	} else {
		return "${tstr} in"
	}
}

def getPressure() {
	def tstr = " " + device.currentState("pressure_trend")?.value.toString()
	def tstr1
	if(wantMetric()) {
		tstr1 = device.currentState("pressure_mb")?.value.toString()
		return "${tstr1} mb ${tstr}"
	} else {
		tstr1 = device.currentState("pressure_in")?.value.toString()
		return "${tstr1} in ${tstr}"
	}
}

def getVisibility() {
	def tstr = device.currentState("visibility")?.value.toString()
	if(wantMetric()) {
		return "${tstr} km"
	} else {
		return "${tstr} Miles"
	}
}

def getLux() {
	def cur = device.currentState("illuminance")?.value.toString()
	return cur
}

private localDate(timeZone) {
	//try {
		def df = new SimpleDateFormat("yyyy-MM-dd")
		df.setTimeZone(TimeZone.getTimeZone(timeZone))
		df.format(new Date())
/*
	}
	catch (ex) {
		log.error "localDate Exception:"
		exceptionDataHandler(ex?.message, "localDate")
	}
*/
}

String getDataString(Integer seriesIndex) {
	def dataString = ""
	def dataTable = []
	switch (seriesIndex) {
		case 1:
			dataTable = state?.temperatureTableYesterday
			break
		case 2:
			dataTable = state?.dewpointTableYesterday
			break
		case 3:
			dataTable = state?.temperatureTable
			break
		case 4:
			dataTable = state?.dewpointTable
			break
		case 5:
			dataTable = state?.humidityTableYesterday
			break
		case 6:
			dataTable = state?.humidityTable
			break
	}
	dataTable.each() {
		def dataArray = [[it[0],it[1],0],null,null,null,null,null,null]
		dataArray[seriesIndex] = it[2]
		dataString += dataArray?.toString() + ","
	}
	return dataString
}

def getSomeOldData(devpoll = false) {
	def dewpointTable = state?.dewpointTable
	def temperatureTable = state?.temperatureTable

	if (devpoll) {
		runIn( 66, "getSomeOldData", [overwrite: true])
		return
	}

	//def startOfToday = timeToday("00:00", location.timeZone)
	def startOfToday = timeToday("00:00", getTimeZone())
	def newValues
	def dataTable = []

	if (state.dewpointTableYesterday == null) {
		LogAction("Querying DB for yesterday's dewpoint data…", "trace")
		def dewpointData = device.statesBetween("dewpoint", startOfToday - 1, startOfToday, [max: 100]) // 24h in 15min intervals should be more than sufficient…
		LogAction("got ${dewpointData.size()}")

		// work around a bug where the platform would return less than the requested number of events (as June 2016, only 50 events are returned)
		while ((newValues = device.statesBetween("dewpoint", startOfToday - 1, dewpointData.last().date, [max: 100])).size()) {
			LogAction("got ${newValues.size()}")
			dewpointData += newValues
		}

		dataTable = []
		dewpointData.reverse().each() {
			//dataTable.add([it.date.format("H", location.timeZone),it.date.format("m", location.timeZone),it.floatValue])
			dataTable.add([it.date.format("H", getTimeZone()),it.date.format("m", getTimeZone()),it.floatValue])
		}
		runIn( 80, "getSomeOldData", [overwrite: true])
		state.dewpointTableYesterday = dataTable
		LogAction("finished")
		return
	}

	if (state.temperatureTableYesterday == null) {
		LogAction("Querying DB for yesterday's temperature data…", "trace")
		def temperatureData = device.statesBetween("temperature", startOfToday - 1, startOfToday, [max: 100])
		LogAction("got ${temperatureData.size()}")
		while ((newValues = device.statesBetween("temperature", startOfToday - 1, temperatureData.last().date, [max: 100])).size()) {
			LogAction("got ${newValues.size()}")
			temperatureData += newValues
		}

		dataTable = []
		temperatureData.reverse().each() {
			//dataTable.add([it.date.format("H", location.timeZone),it.date.format("m", location.timeZone),it.floatValue])
			dataTable.add([it.date.format("H", getTimeZone()),it.date.format("m", getTimeZone()),it.floatValue])
		}
		runIn( 80, "getSomeOldData", [overwrite: true])
		state.temperatureTableYesterday = dataTable
		LogAction("finished")
		return
	}

/*
	if (dewpointTable == null) {
		dewpointTable = []
		temperatureTable = []
	}
*/
	if (dewpointTable == null) {
		LogAction("Querying DB for today's dewpoint data…", "trace")
		def dewpointData = device.statesSince("dewpoint", startOfToday, [max: 100])
		LogAction("got ${dewpointData.size()}")
		while ((newValues = device.statesBetween("dewpoint", startOfToday, dewpointData.last().date, [max: 100])).size()) {
			LogAction("got ${newValues.size()}")
			dewpointData += newValues
		}
		dewpointTable = []
		dewpointData.reverse().each() {
			//dewpointTable?.add([it.date.format("H", location.timeZone),it.date.format("m", location.timeZone),it.floatValue])
			dewpointTable.add([it.date.format("H", getTimeZone()),it.date.format("m", getTimeZone()),it.floatValue])
		}
		runIn( 33, "getSomeOldData", [overwrite: true])
		state.dewpointTable = dewpointTable
		LogAction("finished")
		return
	}

	if (temperatureTable == null) {
		LogAction("Querying DB for today's temperature data…", "trace")
		def temperatureData = device.statesSince("temperature", startOfToday, [max: 100])
		LogAction("got ${temperatureData.size()}")
		while ((newValues = device.statesBetween("temperature", startOfToday, temperatureData.last().date, [max: 100])).size()) {
			temperatureData += newValues
			LogAction("got ${newValues.size()}")
		}
		temperatureTable = []
		//temperatureData.reverse().drop(1).each() {
		temperatureData.reverse().each() {
			//temperatureTable.add([it.date.format("H", location.timeZone),it.date.format("m", location.timeZone),it.floatValue])
			temperatureTable.add([it.date.format("H", getTimeZone()),it.date.format("m", getTimeZone()),it.floatValue])
		}
		runIn( 30, "getSomeOldData", [overwrite: true])
		state.temperatureTable = temperatureTable
		LogAction("finished")
		return
	}
}

def getSomeData(devpoll = false) {
	//LogAction("getSomeData ${state.curWeatherLoc}", "trace")

// hackery to test getting old data
	def tryNum = 1
	if (state.eric != tryNum ) {
		state.dewpointTableYesterday = null
		state.temperatureTableYesterday = null
		state.dewpointTable = null
		state.temperatureTable = null
		state.humidityTableYesterday = null
		state.humidityTable = null
		state.remove("dewpointTableYesterday")
		state.remove("temperatureTableYesterday")
		state.remove("dewpointTable")
		state.remove("temperatureTable")
		state.remove("today")

		state.eric = tryNum
		runIn( 33, "getSomeData", [overwrite: true])
		return
	}

	def todayDay = new Date().format("dd",getTimeZone())
	//def todayDay = new Date().format("dd",location.timeZone)

	if (state?.temperatureTable == null) {
		//getSomeOldData(devpoll)

		state.temperatureTable = []
		state.dewpointTable = []
		state.humidityTable = []
		addNewData()
	}

// hack for folks that upgrade
	if (state?.humidityTable == null) {
		state.humidityTable = []
		state.humidityTableYesterday = []
		addNewData()
		state.humidityTableYesterday = state.humidityTable
	}

	def temperatureTable = state?.temperatureTable
	def dewpointTable = state?.dewpointTable
	def humidityTable = state?.humidityTable

	if (state?.temperatureTableYesterday?.size() == 0) {
		state.temperatureTableYesterday = temperatureTable
		state.dewpointTableYesterday = dewpointTable
		state.humidityTableYesterday = humidityTable
	}

	if (!state.today || state.today != todayDay) {
		state.today = todayDay
		state.dewpointTableYesterday = dewpointTable
		state.temperatureTableYesterday = temperatureTable
		state.humidityTableYesterday = humidityTable

		state.temperatureTable = []
		state.dewpointTable = []
		state.humidityTable = []
	}
	addNewData()
}

def addNewData() {
	def currentTemperature = wantMetric() ? state?.curWeatherTemp_c : state?.curWeatherTemp_f
	def currentDewpoint = wantMetric() ? state?.curWeatherDewPoint_c : state?.curWeatherDewPoint_f
	def currentHumidity = state?.curWeatherHum

	def temperatureTable = state?.temperatureTable
	def dewpointTable = state?.dewpointTable
	def humidityTable = state?.humidityTable

	// add latest dewpoint & temperature readings for the graph
	def newDate = new Date()
	if(newDate == null) { Logger("got null for new Date()") }
	def hr = newDate.format("H", location.timeZone) as Integer
	def mins = newDate.format("m", location.timeZone) as Integer
	state.temperatureTable = addValue(temperatureTable, hr, mins, currentTemperature)
	state.dewpointTable = addValue(dewpointTable, hr, mins, currentDewpoint)
	state?.humidityTable = addValue(humidityTable, hr, mins, currentHumidity)

	//temperatureTable.add([newDate.format("H", location.timeZone),newDate.format("m", location.timeZone),currentTemperature])
	//dewpointTable.add([newDate.format("H", location.timeZone),newDate.format("m", location.timeZone),currentDewpoint])
	//humidityTable.add([newDate.format("H", location.timeZone),newDate.format("m", location.timeZone),currentHumidity])

	//state.temperatureTable = temperatureTable
	//state.dewpointTable = dewpointTable
	//state?.humidityTable = humidityTable
}

def addValue(table, hr, mins, val) {
	def newTable = table
	if(table?.size() > 2) {
		def last = table?.last()[2]
		def secondtolast = table[-2][2]
		if(val == last && val == secondtolast) {
			newTable = table?.take(table.size() - 1)
		}
	}
	newTable?.add([hr, mins, val])
	return newTable
}

def getStartTime() {
	def startTime = 24
	if (state?.dewpointTable?.size()) {
		startTime = state.dewpointTable.min{it[0].toInteger()}[0].toInteger()
	}
	if (state?.dewpointTableYesterday?.size()) {
		startTime = Math.min(startTime, state.dewpointTableYesterday.min{it[0].toInteger()}[0].toInteger())
	}
	return startTime
}

def getMinTemp() {
	def list = []
	if (state?.temperatureTableYesterday?.size() > 0) { list.add(state?.temperatureTableYesterday?.min { it[2] }[2].toInteger()) }
	if (state?.temperatureTable?.size() > 0) { list.add(state?.temperatureTable.min { it[2] }[2].toInteger()) }
	if (state?.dewpointTableYesterday?.size() > 0) { list.add(state?.dewpointTableYesterday.min { it[2] }[2].toInteger()) }
	if (state?.dewpointTable?.size() > 0) { list.add(state?.dewpointTable.min { it[2] }[2].toInteger()) }
	//LogAction("getMinTemp: ${list.min()} result: ${list}", "trace")
	return list?.min()
}

def getMaxTemp() {
	def list = []
	if (state?.temperatureTableYesterday?.size() > 0) { list.add(state?.temperatureTableYesterday.max { it[2] }[2].toInteger()) }
	if (state?.temperatureTable?.size() > 0) { list.add(state?.temperatureTable.max { it[2] }[2].toInteger()) }
	if (state?.dewpointTableYesterday?.size() > 0) { list.add(state?.dewpointTableYesterday.max { it[2] }[2].toInteger()) }
	if (state?.dewpointTable?.size() > 0) { list.add(state?.dewpointTable.max { it[2] }[2].toInteger()) }
	//LogAction("getMaxTemp: ${list.max()} result: ${list}", "trace")
	return list?.max()
}

def getTempUnitStr() {
	def tempStr = "°F"
	if ( wantMetric() ) {
		tempStr = "°C"
	}
	return tempStr
}

def incHtmlLoadCnt() 		{ state?.htmlLoadCnt = (state?.htmlLoadCnt ? state?.htmlLoadCnt.toInteger()+1 : 1) }
def incForecastBtnTapCnt() 	{ state?.forecastBtnTapCnt = (state?.forecastBtnTapCnt ? state?.forecastBtnTapCnt.toInteger()+1 : 1); return ""; }
def getMetricCntData() {
	return [weatHtmlLoadCnt:(state?.htmlLoadCnt ?: 0)]//, forecastBtnTapCnt:(state?.forecastBtnTapCnt ?: 0)]
}

def getWeatherAlertHtml() {
	def wAlertHtml = ""
	def alertCnt = state?.walertCount as Integer
	//log.debug "Weather Alert Count: ${state.walertCount}"   // count of current alerts

	if(alertCnt > 0) {
		for(int i=1; i < alertCnt.toInteger()+1; i++) {
			if(state?."walert${i}" && state?."walertMessage${i}") {
				wAlertHtml += """
					<div class="redAlertBanner"><a class=\"alert-modal${i}\">${alertCnt > 1 ? "Alert ${i}: " : ""}${state?."walert${i}"}</a></div>
					<script>
						\$('.alert-modal${i}').click(function(){
							vex.dialog.alert({ unsafeMessage: `
								<h2 class="alertModalTitle">${alertCnt > 1 ? "#${i}: " : ""}${state?."walert${i}"}</h2>
								<p>${state?."walertMessage${i}"}</p>
							`, className: 'vex-theme-top' })
						});
					</script>
				"""
				//log.debug "Alert $i Description: ${state."walert${i}"}"   // description  1,2,3
				//log.debug "Alert $i Message: ${state."walertMessage${i}"}"  // full message
			}
		}
	}
	return wAlertHtml
}

def forecastDay(day) {
	if(!state?.curForecast) { return }
	def dayName = "<b>${state.curForecast.daypart[0].daypartName[day]} </b><br>"
	def foreImgB64 = getWeatherImg(state.curForecast.daypart[0].iconCode[day])
	def forecastImageLink = """<a class=\"${day}-modal\"><img src="${foreImgB64}" style="width:64px;height:64px;"></a><br>"""
	def forecastTxt = ""

	def modalHead = "<script> \$('.${day}-modal').click(function(){vex.dialog.alert({unsafeMessage: ' "
	def modalTitle = " <h2>${state.curForecast.daypart[0].daypartName[day]}</h2>"
 	def forecastImage = """<div class=\"centerText\"><img src="${foreImgB64}" style="width:64px;height:64px;"></div>"""
	forecastTxt = "<p>${state.curForecast.daypart[0].narrative[day]}</p>"
	def modalClose = "' }); }); </script>"

	return dayName + forecastImageLink + modalHead + modalTitle + forecastImage + forecastTxt + modalClose
}

def androidDisclaimerMsg() {
	if(state?.mobileClientType == "android" && !state?.androidDisclaimerShown) {
		state.androidDisclaimerShown = true
		return """<div class="androidAlertBanner">FYI... The Android Client has a bug with reloading the HTML a second time.\nIt will only load once!\nYou will be required to completely close the client and reload to view the content again!!!</div>"""
	} else { return "" }
}

def getChgLogHtml() {
	def chgStr = ""
	if(!state?.shownChgLog == true) {
		chgStr = """
			<script>
				\$(document).ready(function() {
				    vex.dialog.alert({
						unsafeMessage: `<h3 style="background-color: transparent;">What\'s New with Weather</h3>
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

def hasHtml() { return true }

def getWeatherHTML() {
	try {
		if(!state?.curWeather || !state?.curForecast) { return hideWeatherHtml() }
		def updateAvail = !state.updateAvailable ? "" : """<div class="greenAlertBanner">Device Update Available!</div>"""
		def clientBl = state?.clientBl ? """<div class="brightRedAlertBanner">Your Manager client has been blacklisted!\nPlease contact the Nest Manager developer to get the issue resolved!!!</div>""" : ""
		def obsrvTime = "Last Updated:\n${state?.curWeather?.validTimeLocal}"
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

		def mainHtml = """
			<!DOCTYPE html>
			<html>
				<head>
					<meta http-equiv="cache-control" content="max-age=0"/>
					<meta http-equiv="cache-control" content="no-cache"/>
					<meta http-equiv="expires" content="0"/>
					<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT"/>
					<meta http-equiv="pragma" content="no-cache"/>
					<meta name="viewport" content="width = device-width, user-scalable=no, initial-scale=1.0">

				 	<link rel="stylesheet" type="text/css" href="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/css/ST-HTML.min.css"/>
					<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.1/css/vex.min.css" async/>
					<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.1/css/vex-theme-top.min.css" async />
					<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.1/css/vex-theme-default.min.css" async/>

					<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
					<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.1/js/vex.combined.min.js"></script>
					<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
					<script>vex.defaultOptions.className = 'vex-theme-default';</script>
					<style>
						.vex.vex-theme-default .vex-content { width: 95%; padding: 3px;	}
					</style>
				</head>
				<body>
					${getChgLogHtml()}
					${androidDisclaimerMsg()}
					${devBrdCastHtml}
					${clientBl}
					${updateAvail}
					${getWeatherAlertHtml()}
					<div class="container" style="max-width: 100%; overflow: hidden;">
						<h4>Current Weather Conditions</h4>
						<h1 class="bottomBorder"> ${state?.curWeatherLoc} </h1>
						<div class="row">
							<div class="six columns">
								<b>Feels Like:</b> ${getFeelslike()} <br>
								<b>Precip %: </b> ${device.currentState("percentPrecip")?.value}% <br>
								<b>Precip: </b> ${getPrecip()} <br>
								<b>Humidity:</b> ${state?.curWeatherHum}<br>
								<b>Dew Point: </b>${getDewpoint()}<br>
								<b>Pressure: </b> ${getPressure()} <br>
								<b>UV Index: </b>${device.currentState("ultravioletIndex")?.value}<br>
								<b>Visibility:</b> ${getVisibility()} <br>
								<b>Lux:</b> ${getLux()}<br>
								<b>Sunrise:</b> ${state?.localSunrise} <br> <b>Sunset: </b> ${state?.localSunset} <br>
								<b>Wind:</b> ${state?.windStr} <br>
							</div>
							<div class="six columns">
								<img class="offset-by-two eight columns" src="${getWeatherImg(state?.curWeatherIcon)}"> <br>
								<h2>${getTemp()}</h2>
								<h1 class ="offset-by-two topBorder">${state.curWeatherCond}</h1>
							</div>
						</div>
						<div class="row topBorder">
							<div class="centerText four columns">${forecastDay(1)}</div>
							<div class="centerText four columns">${forecastDay(2)}</div>
							<div class="centerText four columns">${forecastDay(3)}</div>
						</div>
						<div class="row">
							<div class="centerText four columns">${forecastDay(4)}</div>
							<div class="centerText four columns">${forecastDay(5)}</div>
							<div class="centerText four columns">${forecastDay(6)}</div>
						</div>
						<div class="row">
							<div class="centerText offset-by-two four columns">${forecastDay(7)}</div>
							<div class="centerText four columns">${forecastDay(8)}</div>
						</div>
						<p style="font-size: 12px; font-weight: normal; text-align: center;">Tap Icon to View Forecast</p>

						${historyGraphHtml()}

						<div class="row topBorder">
							<div class="centerText offset-by-three six columns">
								<b class="wStation">${state?.curWeather?.validTimeLocal}</b>
							</div>
						</div>
					</div>
					<script>
						function reloadPage() {
							window.location.reload();
						}
					</script>
					<div class="pageFooterBtn">
					    <button type="button" class="btn btn-info pageFooterBtn" onclick="reloadPage()">
						  <span>&#10227;</span> Refresh
					    </button>
					</div>

				</body>
			</html>
		"""
/* """ */
		incHtmlLoadCnt()
		render contentType: "text/html", data: mainHtml, status: 200
	}
	catch (ex) {
		log.error "getWeatherHTML Exception:", ex
		exceptionDataHandler(ex?.message, "getWeatherHTML")
	}
}

def getDeviceTile(devNum="") {
	try {
		if(!state?.curWeather || !state?.curForecast) {
			return hideWeatherHtml()
		}
		def updateAvail = !state.updateAvailable ? "" : """<div class="greenAlertBanner">Device Update Available!</div>"""
		def clientBl = state?.clientBl ? """<div class="brightRedAlertBanner">Your Manager client has been blacklisted!\nPlease contact the Nest Manager developer to get the issue resolved!!!</div>""" : ""
		def obsrvTime = "Last Updated:\n${state?.curWeather?.validTimeLocal}"

		def mainHtml = """
			${clientBl}
			${updateAvail}
			${getWeatherAlertHtml()}
			<div class="device">
				<div class="container">
					<h4>Current Weather Conditions</h4>
					<h1 class="bottomBorder"> ${state?.curWeatherLoc} </h1>
					<div class="row">
						<div class="six columns">
							<b>Feels Like:</b> ${getFeelslike()} <br>
							<b>Precip %: </b> ${device.currentState("percentPrecip")?.value}% <br>
							<b>Precip: </b> ${getPrecip()} <br>
							<b>Humidity:</b> ${state?.curWeatherHum}<br>
							<b>Dew Point: </b>${getDewpoint()}<br>
							<b>Pressure: </b> ${getPressure()} <br>
							<b>UV Index: </b>${device.currentState("ultravioletIndex")?.value}<br>
							<b>Visibility:</b> ${getVisibility()} <br>
							<b>Lux:</b> ${getLux()}<br>
							<b>Sunrise:</b> ${state?.localSunrise} <br> <b>Sunset: </b> ${state?.localSunset} <br>
							<b>Wind:</b> ${state?.windStr} <br>
						</div>
						<div class="six columns">
							<img class="offset-by-two eight columns" src="${getWeatherImg(state?.curWeatherIcon)}"> <br>
							<h2>${getTemp()}</h2>
							<h1 class ="offset-by-two topBorder">${state.curWeatherCond}</h1>
						</div>
					</div>
					<div class="row topBorder">
						<div class="centerText four columns">${forecastDay(1)}</div>
						<div class="centerText four columns">${forecastDay(2)}</div>
						<div class="centerText four columns">${forecastDay(3)}</div>
					</div>
					<div class="row">
						<div class="centerText four columns">${forecastDay(4)}</div>
						<div class="centerText four columns">${forecastDay(5)}</div>
						<div class="centerText four columns">${forecastDay(6)}</div>
					</div>
					<div class="row">
						<div class="centerText offset-by-two four columns">${forecastDay(7)}</div>
						<div class="centerText four columns">${forecastDay(8)}</div>
					</div>
					<p style="font-size: 12px; font-weight: normal; text-align: center;">Tap Icon to View Forecast</p>

					${historyGraphHtml(devNum)}

					<div class="row topBorder">
						<div class="centerText offset-by-three six columns">
							<b class="wStation">${state?.curWeather?.validTimeLocal}</b>
						</div>
					</div>
				</div>
			</div>

		"""
/* """ */
		render contentType: "text/html", data: mainHtml, status: 200
	}
	catch (ex) {
		log.error "getDeviceTile Exception:", ex
		exceptionDataHandler(ex?.message, "getDeviceTile")
	}
}

def historyGraphHtml(devNum="") {
	def html = ""
	if(state?.showGraphs) {
		if (state?.temperatureTable?.size() > 0 && state?.dewpointTable?.size() > 0) {
			def tempStr = getTempUnitStr()
			def minval = getMinTemp()
			def minstr = "minValue: ${minval},"

			def maxval = getMaxTemp()
			def maxstr = "maxValue: ${maxval},"

			def differ = maxval - minval
			//LogAction("differ ${differ}", "trace")
			minstr = "minValue: ${(minval - (wantMetric() ? 2:5))},"
			maxstr = "maxValue: ${(maxval + (wantMetric() ? 2:5))},"

			html = """
			  <script type="text/javascript">
				google.charts.load('current', {packages: ['corechart']});
				google.charts.setOnLoadCallback(drawWeatherGraph);
				function drawWeatherGraph() {
					var data = new google.visualization.DataTable();
					data.addColumn('timeofday', 'time');
					data.addColumn('number', 'Temp (Yesterday)');
					data.addColumn('number', 'Dew (Yesterday)');
					data.addColumn('number', 'Temp (Today)');
					data.addColumn('number', 'Dew (Today)');
					data.addColumn('number', 'Humidity (Yest)');
					data.addColumn('number', 'Humidity (Today)');
					data.addRows([
						${getDataString(1)}
						${getDataString(2)}
						${getDataString(3)}
						${getDataString(4)}
						${getDataString(5)}
						${getDataString(6)}
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
							0: {targetAxisIndex: 1, color: '#FFC2C2', lineWidth: 1},
							1: {targetAxisIndex: 1, color: '#D1DFFF', lineWidth: 1},
							2: {targetAxisIndex: 1, color: '#FF0000'},
							3: {targetAxisIndex: 1, color: '#004CFF'},
							4: {targetAxisIndex: 0, color: '#D2D2D2', lineWidth: 1},
							5: {targetAxisIndex: 0, color: '#B8B8B8'}
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
							bottom: '20%',
							height: '85%',
							width: '100%'
						}
					};
					var chart = new google.visualization.AreaChart(document.getElementById('chart_div${devNum}'));
					chart.draw(data, options);
				}
			</script>
			<h4 style="font-size: 22px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Event History</h4>
			<div id="chart_div${devNum}" style="width: 100%; height: 225px;"></div>
			"""
		} else {
			html = """
				<h4 style="font-size: 22px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Event History</h4>
				<br></br>
				<div class="centerText">
				<p>Waiting for more data to be collected</p>
				<p>This may take at a couple hours</p>
				</div>
			"""
		}
	}
}

def hideWeatherHtml() {
	def data = """
		<br></br><br></br>
		<h3 style="font-size: 22px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">The Required Weather data is not available yet...</h3>
		<br></br><h3 style="font-size: 22px; font-weight: bold; text-align: center; background: #00a1db; color: #f5f5f5;">Please refresh this page after a couple minutes...</h3>
		<br></br><br></br>"""
	render contentType: "text/html", data: data, status: 200
}

private def textDevName()	{ return "Nest Weather${appDevName()}" }
private def appDevType()	{ return false }
private def appDevName()	{ return appDevType() ? " (Dev)" : "" }