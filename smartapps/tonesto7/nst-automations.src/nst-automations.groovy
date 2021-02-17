/********************************************************************************************
|    Application Name: NST Automations                                                      |
|        Copyright (C) 2017 Anthony S.                                                      |
|    Authors: Anthony S. (@tonesto7), Eric S. (@E_sch)                                      |
|    Contributors: Ben W. (@desertblade)                                                    |
|    A few code methods are modeled from those in CoRE by Adrian Caramaliu                  |
|                                                                                           |
|    License Info: https://github.com/tonesto7/nest-manager/blob/master/app_license.txt     |
|********************************************************************************************/

import groovy.json.*
import java.text.SimpleDateFormat

definition(
	name: "${appName()}",
	namespace: "${appNamespace()}",
	author: "${appAuthor()}",
	parent: "${appNamespace()}:${appParentName()}",
	description: "${appDesc()}",
	category: "Convenience",
	iconUrl: "https://raw.githubusercontent.com/${gitPath()}/Images/App/automation_icon.png",
	iconX2Url: "https://raw.githubusercontent.com/${gitPath()}/Images/App/automation_icon.png",
	iconX3Url: "https://raw.githubusercontent.com/${gitPath()}/Images/App/automation_icon.png"
)

{
	appSetting "devOpt"
}

def appVersion() { "5.1.7" }
def appVerDate() { "8-2-2017" }

preferences {
	//startPage
	page(name: "startPage")

	page(name: "changeLogPage")
	page(name: "uninstallPage")

	//Automation Pages
	page(name: "notAllowedPage")
	page(name: "selectAutoPage")
	page(name: "mainAutoPage")
	//page(name: "remSenTstatFanSwitchPage")
	page(name: "remSenShowTempsPage")
	page(name: "nestModePresPage")
	//page(name: "tstatModePage")
	page(name: "schMotModePage")
	page(name: "setDayModeTimePage")
	page(name: "watchDogPage")
	page(name: "diagnosticsPage")
	page(name: "schMotSchedulePage")
	page(name: "scheduleConfigPage")
	page(name: "tstatConfigAutoPage")

	//shared pages
	page(name: "setNotificationPage")
	page(name: "setNotificationTimePage")
}

/******************************************************************************
|					Application Pages						  |
*******************************************************************************/
//This Page is used to load either parent or child app interface code
def startPage() {
	if(parent) {
		if(!atomicState?.isInstalled && parent?.state?.ok2InstallAutoFlag != true) {
			notAllowedPage()
		} else {
			atomicState?.isParent = false
			selectAutoPage()
		}
	}
}

def notAllowedPage () {
	dynamicPage(name: "notAllowedPage", title: "This install Method is Not Allowed", install: false, uninstall: true) {
		section() {
			paragraph "HOUSTON WE HAVE A PROBLEM!\n\nNST Automations can't be directly installed from the Marketplace.\n\nPlease use the NST Manager SmartApp to configure them.", required: true,
			state: null, image: getAppImg("disable_icon2.png")
		}
	}
}

def changeLogPage () {
	def execTime = now()
	dynamicPage(name: "changeLogPage", title: "App Revision History:", install: false) {
		section() {
			paragraph appVerInfo()
		}
		incChgLogLoadCnt()
		devPageFooter("chgLogLoadCnt", execTime)
	}
}

def uninstallPage() {
	dynamicPage(name: "uninstallPage", title: "Uninstall", uninstall: true) {
		section("") {
			if(parent) {
				paragraph "This will uninstall the ${app?.label} Automation!"
			}
		}
		remove("Remove ${appName()} and Devices!", "WARNING!!!", "Last Chance to Stop!\nThis action is not reversible\n\nThis App, All Devices, and Automations will be removed")
	}
}


/******************************************************************************
 *#########################	NATIVE ST APP METHODS ############################*
 ******************************************************************************/
def installed() {
	log.debug "${app.label} Installed with settings: ${settings}"		// MUST BE log.debug
	atomicState?.installData = ["initVer":appVersion(), "dt":getDtNow().toString()]
	initialize()
	sendNotificationEvent("${appName()} installed")
}

def updated() {
	LogAction("${app.label} Updated...with settings: ${settings}", "debug", true)
	initialize()
	sendNotificationEvent("${appName()} has updated settings")
	atomicState?.lastUpdatedDt = getDtNow()
}

def uninstalled() {
	//LogTrace("uninstalled")
	uninstAutomationApp()
}

def initialize() {
	//log.debug "${app.label} Initialize..."			// Must be log.debug
	if(!atomicState?.newAutomationFile) { atomicState?.newAutomationFile = true }
	if(!atomicState?.installData) { atomicState?.installData = ["initVer":appVersion(), "dt":getDtNow().toString()] }
	def settingsReset = parent?.settings?.resetAllData
	if(atomicState?.resetAllData || settingsReset) {
		if(fixState()) { return }	// runIn of fixState will call initAutoApp()
	}
	runIn(6, "initAutoApp", [overwrite: true])
}

def subscriber() {

}

private adj_temp(tempF) {
	if(getTemperatureScale() == "C") {
		return ((tempF - 32) * (5 / 9)) as Double //
	} else {
		return tempF
	}
}

def getStZipCode() { return location?.zipCode?.toString() }
def getNestZipCode() {
	if(atomicState?.structures && atomicState?.structData) {
		return atomicState?.structData[atomicState?.structures]?.postal_code ? atomicState?.structData[atomicState?.structures]?.postal_code.toString() : ""
	} else { return "" }
}
def getNestTimeZone() {
	if(atomicState?.structures && atomicState?.structData) {
		return atomicState?.structData[atomicState?.structures]?.time_zone ?: null
	} else { return null }
}

def helpHandler() {
	def help = parent ? parent?.state?.appData?.help : null
	if(help) {
		atomicState.showHelp = (help?.showHelp == false) ? false : true
	}
}

def incMetricCntVal(item) {
	def data = atomicState?.usageMetricsStore ?: [:]
	data[item] = (data[item] == null) ? 1 : data[item].toInteger()+1
	atomicState?.usageMetricsStore = data
}

def incAutoGlobPrefLoadCnt() { incMetricCntVal("autoGlobPrefLoadCnt") }

def isCodeUpdateAvailable(newVer, curVer, type) {
	def result = false
	def latestVer
	if(newVer && curVer) {
		def versions = [newVer, curVer]
		if(newVer != curVer) {
			latestVer = versions?.max { a, b ->
				def verA = a?.tokenize('.')
				def verB = b?.tokenize('.')
				def commonIndices = Math.min(verA?.size(), verB?.size())
				for (int i = 0; i < commonIndices; ++i) {
					//log.debug "comparing $numA and $numB"
					if(verA[i]?.toInteger() != verB[i]?.toInteger()) {
						return verA[i]?.toInteger() <=> verB[i]?.toInteger()
					}
				}
				verA?.size() <=> verB?.size()
			}
			result = (latestVer == newVer) ? true : false
		}
	}
	//log.debug "type: $type | newVer: $newVer | curVer: $curVer | newestVersion: ${latestVer} | result: $result"
	return result
}

def isAppUpdateAvail() {
	def ver = parent ? parent?.state?.appData?.updater?.versions?.autoapp : null
	if(isCodeUpdateAvailable(ver?.ver, appVersion(), "automation")) { return true }
	return false
}

def setMyLockId(val) {
	if(atomicState?.myID == null && parent && val) {
		atomicState.myID = val
	}
}

def getMyLockId() {
	if(parent) { return atomicState?.myID } else { return null }
}

def fixState() {
	def result = false
	LogAction("fixState", "info", false)
	def before = getStateSizePerc()
	if(!parent) {
/*
		if(!atomicState?.resetAllData && resetAllData) {
			def data = getState()?.findAll { !(it?.key in ["accessToken", "authToken", "enRemDiagLogging", "installationId", "remDiagLogActivatedDt", "remDiagLogDataStore", "remDiagDataSentDt", "remDiagLogSentCnt", "resetAllData", "pollingOn", "apiCommandCnt"]) }
			data.each { item ->
				state.remove(item?.key.toString())
			}
			unschedule()
			unsubscribe()
			atomicState.pollingOn = false
			result = true
		} else if(atomicState?.resetAllData && !resetAllData) {
			LogAction("fixState: resetting ALL toggle", "info", true)
			atomicState.resetAllData = false
		}
*/
	} else {
		if(!atomicState?.resetAllData && parent?.settings?.resetAllData) { // automation cleanup called from update() -> initAutoApp()
			def data = getState()?.findAll { !(it?.key in [ "automationType", "disableAutomation", "lastScheduleList", "oldremSenTstat", "leakWatRestoreMode", "conWatRestoreMode", "extTmpRestoreMode", "extTmpTstatOffRequested", "conWatTstatOffRequested", "leakWatTstatOffRequested", "resetAllData", "extTmpLastDesiredTemp", "restoreId", "restoredFromBackup", "restoreCompleted", "automationTypeFlag", "newAutomationFile", "installData", "remDiagLogDataStore" ]) }
//  "watchDogAlarmActive", "extTmpAlarmActive", "conWatAlarmActive", "leakWatAlarmActive",
			data.each { item ->
				state.remove(item?.key.toString())
			}
			unschedule()
			unsubscribe()
			result = true
		} else if(atomicState?.resetAllData && !parent?.settings?.resetAllData) {
			LogAction("fixState: resetting ALL toggle", "info", true)
			atomicState.resetAllData = false
		}
	}
	if(result) {
		atomicState.resetAllData = true
		LogAction("fixState: State Data: before: $before after: ${getStateSizePerc()}", "info", true)
		runIn(20, "finishFixState", [overwrite: true])
	}
	return result
}

void finishFixState(migrate=false) {
	LogAction("finishFixState", "info", false)
	if(!parent) {
/*
		if(atomicState?.resetAllData) {
			atomicState.misPollNotifyWaitVal = !misPollNotifyWaitVal ? 900 : misPollNotifyWaitVal.toInteger()
			atomicState.misPollNotifyMsgWaitVal = !misPollNotifyMsgWaitVal ? 3600 : misPollNotifyMsgWaitVal.toInteger()
			atomicState.updNotifyWaitVal = !updNotifyWaitVal ? 43200 : updNotifyWaitVal.toInteger()
			atomicState.useAltNames = settings?.useAltNames ? true : false
			atomicState.custLabelUsed = settings?.useCustDevNames ? true : false
			if(!atomicState?.installData) { atomicState?.installData = ["initVer":appVersion(), "dt":getDtNow().toString(), "freshInstall":false, "shownDonation":false, "shownFeedback":false] }

			getWebFileData() // get the appData and calls setStateVar

			atomicState.needStrPoll = true
			atomicState?.needDevPoll = true
			atomicState?.needMetaPoll = true

			atomicState.structures = settings?.structures ?: null
			if(settings?.structures && atomicState?.structures && !atomicState.structName) {
				def structs = getNestStructures()
				if(structs) {
					atomicState.structName = "${structs[atomicState?.structures]}"
				}
			}
			//def str = getApiData("str")
			//def dev = getApiData("dev")
			//def meta = getApiData("meta")

			if(settings?.thermostats && !atomicState?.thermostats) { atomicState.thermostats = settings?.thermostats ? statState(settings?.thermostats) : null }
			if(settings?.protects && !atomicState?.protects) { atomicState.protects = settings?.protects ? coState(settings?.protects) : null }
			if(settings?.cameras && !atomicState?.cameras) { atomicState.cameras = settings?.cameras ? camState(settings?.cameras) : null }
			atomicState.presDevice = settings?.presDevice ?: null
			atomicState.weatherDevice = settings?.weatherDevice ?: null
			if(settings?.thermostats || settings?.protects || settings?.cameras || settings?.presDevice || settings?.weatherDevice) {
				atomicState.isInstalled = true
				atomicState.newSetupComplete = true
				atomicState?.setupVersion = atomicState?.appData?.updater?.setupVersion?.toInteger() ?: 0
			} else { atomicState.isInstalled = false }

			//updated()
			initManagerApp()

			def cApps = getChildApps()
			if(cApps) {
				cApps?.sort()?.each { chld ->
					chld?.update()
				}
			}
		}
*/
	} else {
		if(atomicState?.resetAllData || migrate) {
			def tstat = settings?.schMotTstat
			if(tstat) {
				LogAction("finishFixState found tstat", "info", true)
				getTstatCapabilities(tstat, schMotPrefix())
				if(!getMyLockId()) {
					setMyLockId(app.id)
				}
				if(settings?.schMotRemoteSensor) {
					LogAction("finishFixState found remote sensor", "info", true)
					if( parent?.remSenLock(tstat?.deviceNetworkId, getMyLockId()) ) {  // lock new ID
						atomicState?.remSenTstat = tstat?.deviceNetworkId
					}
					if(isRemSenConfigured() && settings?.remSensorDay) {
						LogAction("finishFixState found remote sensor configured", "info", true)
						if(settings?.vthermostat != null) { parent?.addRemoveVthermostat(tstat.deviceNetworkId, vthermostat, getMyLockId()) }
					}
				}
			}
			if(!migrate) { initAutoApp() }
			//updated()
		}
	}
}

def selectAutoPage() {
	//LogTrace("selectAutoPage()")
	if(!atomicState?.automationType) {
		return dynamicPage(name: "selectAutoPage", title: "Choose an Automation Type", uninstall: false, install: false, nextPage: null) {
			def thereIsChoice = !parent.automationNestModeEnabled(null)
			if(thereIsChoice) {
				section("Set Nest Presence Based on ST Modes, Presence Sensor, or Switches:") {
					href "mainAutoPage", title: "Nest Mode Automations", description: "", params: [autoType: "nMode"], image: getAppImg("mode_automation_icon.png")
				}
			}
			section("Thermostat Automations: Setpoints, Remote Sensor, External Temp, Humidifier, Contact Sensor, Leak Sensor, Fan Control") {
				href "mainAutoPage", title: "Thermostat Automations", description: "", params: [autoType: "schMot"], image: getAppImg("thermostat_automation_icon.png")
			}
		}
	}
	else { return mainAutoPage( [autoType: atomicState?.automationType]) }
}

def mainAutoPage(params) {
	//LogTrace("mainAutoPage()")
	if(!atomicState?.tempUnit) { atomicState?.tempUnit = getTemperatureScale()?.toString() }
	if(!atomicState?.disableAutomation) { atomicState.disableAutomation = false }
	def t0 = parent?.getShowHelp()
	atomicState?.showHelp = (t0 != null) ? t0 : true
	def autoType = null
	//If params.autoType is not null then save to atomicState.
	if(!params?.autoType) { autoType = atomicState?.automationType }
	else { atomicState.automationType = params?.autoType; autoType = params?.autoType }

	// If the selected automation has not been configured take directly to the config page.  Else show main page
	if(autoType == "nMode" && !isNestModesConfigured())		{ return nestModePresPage() }
	else if(autoType == "watchDog" && !isWatchdogConfigured())	{ return watchDogPage() }
	else if(autoType == "remDiag" && !isDiagnosticsConfigured())	{ return diagnosticsPage() }
	else if(autoType == "schMot" && !isSchMotConfigured())		{ return schMotModePage() }

	else {
		// Main Page Entries
		//return dynamicPage(name: "mainAutoPage", title: "Automation Configuration", uninstall: false, install: false, nextPage: "nameAutoPage" ) {
		return dynamicPage(name: "mainAutoPage", title: "Automation Configuration", uninstall: false, install: true, nextPage:null ) {
			section() {
				if(disableAutomationreq) {
					paragraph "This Automation is currently disabled!\nTurn it back on to to make changes or resume operation", required: true, state: null, image: getAppImg("instruct_icon.png")
				} else {
					if(atomicState?.disableAutomation) { paragraph "This Automation is still disabled!\nPress Next and Done to Activate this Automation Again", state: "complete", image: getAppImg("instruct_icon.png") }
				}
				if(!atomicState?.disableAutomation) {
					if(autoType == "nMode") {
						//paragraph title:"Set Nest Presence Based on ST Modes, Presence Sensor, or Switches:", ""
						def nDesc = ""
						nDesc += isNestModesConfigured() ? "Nest Mode:\n • Status: (${strCapitalize(getNestLocPres())})" : ""
						if(((!nModePresSensor && !nModeSwitch) && (nModeAwayModes && nModeHomeModes))) {
							nDesc += nModeHomeModes ? "\n • Home Modes: (${nModeHomeModes.size()})" : ""
							nDesc += nModeAwayModes ? "\n • Away Modes: (${nModeAwayModes.size()})" : ""
						}
						nDesc += (nModePresSensor && !nModeSwitch) ? "\n\n${nModePresenceDesc()}" : ""
						nDesc += (nModeSwitch && !nModePresSensor) ? "\n • Using Switch: (State: ${isSwitchOn(nModeSwitch) ? "ON" : "OFF"})" : ""
						nDesc += (nModeDelay && nModeDelayVal) ? "\n • Change Delay: (${getEnumValue(longTimeSecEnum(), nModeDelayVal)})" : ""
						nDesc += (isNestModesConfigured() ) ? "\n • Restrictions Active: (${autoScheduleOk(getAutoType()) ? "NO" : "YES"})" : ""
						if(isNestModesConfigured()) {
							nDesc += "\n • Set Thermostats to ECO: (${nModeSetEco ? "On" : "Off"})"
							if(parent?.settings?.cameras) {
								nDesc += "\n • Turn Cams On when Away: (${nModeCamOnAway ? "On" : "Off"})"
								nDesc += "\n • Turn Cams Off when Home: (${nModeCamOffHome ? "On" : "Off"})"
								if(settings?.nModeCamsSel) {
									nDesc += "\n • Nest Cams Selected: (${nModeCamsSel.size()})"
								}
							}
						}
						nDesc += (nModePresSensor || nModeSwitch) || (!nModePresSensor && !nModeSwitch && (nModeAwayModes && nModeHomeModes)) ? "\n\nTap to modify" : ""
						def nModeDesc = isNestModesConfigured() ? "${nDesc}" : null
						href "nestModePresPage", title: "Nest Mode Automation Config", description: nModeDesc ?: "Tap to configure", state: (nModeDesc ? "complete" : null), image: getAppImg("mode_automation_icon.png")
					}

					if(autoType == "schMot") {
						def sModeDesc = getSchMotConfigDesc()
						href "schMotModePage", title: "Thermostat Automation Config", description: sModeDesc ?: "Tap to configure", state: (sModeDesc ? "complete" : null), image: getAppImg("thermostat_automation_icon.png")
					}

					if(autoType == "watchDog") {
						//paragraph title:"Watch your Nest Location for Events:", ""
						def watDesc = ""
						def t1 = getVoiceNotifConfigDesc("watchDog")
						watDesc += (settings["${getAutoType()}AllowSpeechNotif"] && (settings["${getAutoType()}SpeechDevices"] || settings["${getAutoType()}SpeechMediaPlayer"]) && t1) ?
								"\n\nVoice Notifications:${t1}" : ""
						def watDogDesc = isWatchdogConfigured() ? "${watDesc}" : null
						href "watchDogPage", title: "Nest Location Watchdog", description: watDogDesc ?: "Tap to configure", state: (watDogDesc ? "complete" : null), image: getAppImg("watchdog_icon.png")
					}
					if(autoType == "remDiag") {
						//paragraph title:"Watch your Nest Location for Events:", ""
						def diagDesc = ""
						def remDiagDesc = isDiagnosticsConfigured() ? "${diagDesc}" : null
						href "diagnosticsPage", title: "NST Diagnostics", description: remDiagDesc ?: "Tap to configure", state: (remDiagDesc ? "complete" : null), image: getAppImg("diag_icon.png")
					}
				}
			}
			section("Automation Options:") {
				if(atomicState?.isInstalled && (isNestModesConfigured() || isWatchdogConfigured() || isSchMotConfigured())) {
					//paragraph title:"Enable/Disable this Automation", ""
					input "disableAutomationreq", "bool", title: "Disable this Automation?", required: false, defaultValue: atomicState?.disableAutomation, submitOnChange: true, image: getAppImg("disable_icon2.png")
					setAutomationStatus(settings?.disableAutomationreq)
				}
				input ("showDebug", "bool", title: "Debug Option", description: "Show Automation Logs in the IDE?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("debug_icon.png"))
				if(showDebug) {
					input (name: "advAppDebug", type: "bool", title: "Show Verbose Logs?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("list_icon.png"))
				} else {
					settingUpdate("advAppDebug", "false", "bool")
				}
			}
			section("Automation Name:") {
//				if(autoType == "watchDog") {
//					paragraph "${app?.label}"
//				} else {
					def newName = getAutoTypeLabel()
					label title: "Label this Automation:", description: "Suggested Name: ${newName}", defaultValue: newName, required: true, wordWrap: true, image: getAppImg("name_tag_icon.png")
					if(!atomicState?.isInstalled) {
						paragraph "Make sure to name it something that you can easily recgonize."
					}
//				}
			}
			remove("Remove this Automation!", "WARNING!!!", "Last Chance to Stop!!!\nThis action is not reversible\n\nThis Automation will be removed completely")
		}
	}
}

def getSchMotConfigDesc(retAsList=false) {
	def list = []
	if(settings?.schMotWaterOff) { list.push("Turn Off if Leak Detected") }
	if(settings?.schMotContactOff) { list.push("Set ECO if Contact Open") }
	if(settings?.schMotExternalTempOff) { list.push("Set ECO based on External Temp") }
	if(settings?.schMotRemoteSensor) { list.push("Use Remote Temp Sensors") }
	if(isTstatSchedConfigured()) { list.push("Setpoint Schedules Created") }
	if(settings?.schMotOperateFan) { list.push("Control Fans with HVAC") }
	if(settings?.schMotHumidityControl) { list.push("Control Humidifier") }

	if(retAsList) {
		return isSchMotConfigured() ? list : null
	} else {
		def sDesc = ""
		sDesc += settings?.schMotTstat ? "${settings?.schMotTstat?.label}" : ""
		list?.each { ls ->
			sDesc += "\n • ${ls}"
		}
		sDesc += settings?.schMotTstat ? "\n\nTap to modify" : ""
		return isSchMotConfigured() ? "${sDesc}" : null
	}
}

def setAutomationStatus(disabled, upd=false) {
	if(!atomicState?.disableAutomation && disabled) {
		LogAction("Automation Disabled at (${getDtNow()})", "info", true)
		atomicState?.disableAutomationDt = getDtNow()
	} else if(atomicState?.disableAutomation && !disabled) {
		LogAction("Automation Enabled at (${getDtNow()})", "info", true)
		atomicState?.disableAutomationDt = null
	}
	atomicState?.disableAutomation = disabled
	if(upd) { app.update() }
}

def buildSettingsMap() {
	def inputData = parent?.getWebData("https://st-nest-manager.firebaseio.com/restoreInputData.json", "application/json", "inputType", false)
	def settingsMap = [:]
	def setData = getSettings()?.sort()?.findAll { it }
	setData?.sort().each { item ->
		def itemVal = item?.value
		def itemType = inputData?.inputs?.find { item?.key.toString().contains(it?.key.toString()) }
		settingsMap[item?.key] = ["type":itemType?.value, "value":itemVal]
	}
	//log.debug "buildSettingsMap: $settingsMap"
	return settingsMap
}

def createAutoBackupJson() {
	//log.trace "createAutoBackupJson..."
	def noShow = ["curAlerts", "curAstronomy", "curForecast", "curWeather", "detailEventHistory", "detailExecutionHistory", "evalExecutionHistory", "activeSchedData", "resetAllData"]
	for(def i=1; i <= 8; i++) { noShow.push("schMot_${i}_MotionActiveDt"); noShow.push("schMot_${i}_MotionInActiveDt"); noShow.push("schMot_${i}_oldMotionActive"); }
	def stData = getState()?.sort()?.findAll { !(it.key in noShow) }
	def stateData = [:]
	stData?.sort().each { item ->
		stateData[item?.key] = item?.value
	}
	def setData = buildSettingsMap()
	setData?.sort().each { item ->
		def itemVal = item?.value?.value
		def itemType = item?.value?.type
		def tmpList = []
		def getIds4These = ["phone", "contact"]
		def setObj = null
		if(itemType?.contains("capability") || itemType in getIds4These) {
			if(itemVal instanceof List) { setObj = settings[item?.key].collect { it?.getId() } }
			else { setObj = settings[item?.key].getId() }
		}
		else {
			if(itemType == "mode" || itemVal instanceof Integer || itemVal instanceof Double || itemVal instanceof Boolean || itemVal instanceof Float || itemVal instanceof Long || itemVal instanceof BigDecimal) {
				setObj = itemVal
			}
			else { setObj = itemVal.toString() }
		}
		//log.debug "setting item ${item?.key}: ${getObjType(itemVal)} | result: $setObj"
		setData[item?.key].value = setObj
	}
	setData["automationTypeFlag"] = getAutoType().toString()
	def data = [:]
	data["appLabel"] = app.label
	data["stateData"] = stateData
	data["settingsData"] = setData
	data["backupDt"] = getDtNow()
	def resultJson = new groovy.json.JsonOutput().toJson(data)
	//log.debug "resultJson: $resultJson"
	return resultJson
}

def backupConfigToFirebase() {
	unschedule()
	unsubscribe()
	uninstAutomationApp()			// Cleanup any parent state this cFhild owns
	def data = createAutoBackupJson()
	return parent?.sendAutomationBackupData(data, app.id)
}

void settingUpdate(name, value, type=null) {
	LogTrace("settingUpdate($name, $value, $type)...")
	try {
		//if(name && value && type) {
		if(name && type) {
			app?.updateSetting("$name", [type: "$type", value: value])
		}
		//else if (name && value && type == null) { app?.updateSetting(name.toString(), value) }
		else if (name && type == null) { app?.updateSetting(name.toString(), value) }
	} catch(e) {
		log.error "settingUpdate Exception:", ex
	}
}

def stateUpdate(key, value) {
	if(key) { atomicState?."${key}" = value }
	else { LogAction("stateUpdate: null key $key $value", "error", true) }
}

def initAutoApp() {
	//log.debug "${app.label} initAutoApp..."			// Must be log.debug
	def restoreId = settings["restoreId"]
	def restoreComplete = settings["restoreCompleted"] == true ? true : false
	if(settings["watchDogFlag"]) {
		atomicState?.automationType = "watchDog"
	} else if(settings["remDiagFlag"]) {
		atomicState?.automationType = "remDiag"
	}
	else if (restoreId != null && restoreComplete == false) {
		LogAction("Restored AutomationType: (${settings?.automationTypeFlag})", "info", true)
		if(parent?.callRestoreState(app, restoreId)) {
			finishFixState(true)
			parent?.postChildRestore(restoreId)
			if(parent?.keepBackups() != true) { parent?.removeAutomationBackupData(restoreId) }
			settingUpdate("restoreCompleted", true, "bool")
		}
	}

	def autoType = getAutoType()
	if(autoType == "nMode") {
		parent.automationNestModeEnabled(true)
	}
	unschedule()
	unsubscribe()
	def autoDisabled = getIsAutomationDisabled()

	if(!autoDisabled && (restoreId && restoreComplete == false ? false : true)) {
		automationsInst()

		if(autoType == "schMot" && isSchMotConfigured()) {
			updateScheduleStateMap()
			def schedList = getScheduleList()
			def timersActive = false
			def sLbl
			def cnt = 1
			def numact = 0
			schedList?.each { scd ->
				sLbl = "schMot_${scd}_"
				atomicState."schedule${cnt}SwEnabled" = null
				atomicState."schedule${cnt}PresEnabled" = null
				atomicState."schedule${cnt}MotionEnabled" = null
				atomicState."schedule${cnt}SensorEnabled" = null

				def newscd = []
				def act = settings["${sLbl}SchedActive"]
				if(act) {
					newscd = cleanUpMap([
						m: settings["${sLbl}restrictionMode"],
						tf: settings["${sLbl}restrictionTimeFrom"],
						tfc: settings["${sLbl}restrictionTimeFromCustom"],
						tfo: settings["${sLbl}restrictionTimeFromOffset"],
						tt: settings["${sLbl}restrictionTimeTo"],
						ttc: settings["${sLbl}restrictionTimeToCustom"],
						tto: settings["${sLbl}restrictionTimeToOffset"],
						w: settings["${sLbl}restrictionDOW"],
						p1: buildDeviceNameList(settings["${sLbl}restrictionPresHome"], "and"),
						p0: buildDeviceNameList(settings["${sLbl}restrictionPresAway"], "and"),
						s1: buildDeviceNameList(settings["${sLbl}restrictionSwitchOn"], "and"),
						s0: buildDeviceNameList(settings["${sLbl}restrictionSwitchOff"], "and"),
						ctemp: roundTemp(settings["${sLbl}CoolTemp"]),
						htemp: roundTemp(settings["${sLbl}HeatTemp"]),
						hvacm: settings["${sLbl}HvacMode"],
						sen0: settings["schMotRemoteSensor"] ? buildDeviceNameList(settings["${sLbl}remSensor"], "and") : null,
						thres: settings["schMotRemoteSensor"] ? settings["${sLbl}remSenThreshold"] : null,
						m0: buildDeviceNameList(settings["${sLbl}Motion"], "and"),
						mctemp: settings["${sLbl}Motion"] ? roundTemp(settings["${sLbl}MCoolTemp"]) : null,
						mhtemp: settings["${sLbl}Motion"] ? roundTemp(settings["${sLbl}MHeatTemp"]) : null,
						mhvacm: settings["${sLbl}Motion"] ? settings["${sLbl}MHvacMode"] : null,
//						mpresHome: settings["${sLbl}Motion"] ? settings["${sLbl}MPresHome"] : null,
//						mpresAway: settings["${sLbl}Motion"] ? settings["${sLbl}MPresAway"] : null,
						mdelayOn: settings["${sLbl}Motion"] ? settings["${sLbl}MDelayValOn"] : null,
						mdelayOff: settings["${sLbl}Motion"] ? settings["${sLbl}MDelayValOff"] : null
					])
					numact += 1
				}
				LogTrace("initAutoApp: [Schedule: $scd | sLbl: $sLbl | act: $act | newscd: $newscd]")
				atomicState."sched${cnt}restrictions" = newscd
				atomicState."schedule${cnt}SwEnabled" = (newscd?.s1 || newscd?.s0) ? true : false
				atomicState."schedule${cnt}PresEnabled" = (newscd?.p1 || newscd?.p0) ? true : false
				atomicState."schedule${cnt}MotionEnabled" = (newscd?.m0) ? true : false
				atomicState."schedule${cnt}SensorEnabled" = (newscd?.sen0) ? true : false
				//atomicState."schedule${cnt}FanCtrlEnabled" = (newscd?.fan0) ? true : false
				atomicState."schedule${cnt}TimeActive" = (newscd?.tf || newscd?.tfc || newscd?.tfo || newscd?.tt || newscd?.ttc || newscd?.tto || newscd?.w) ? true : false

				atomicState."${sLbl}MotionActiveDt" = null
				atomicState."${sLbl}MotionInActiveDt" = null

				def newact = isMotionActive(settings["${sLbl}Motion"])
				if(newact) { atomicState."${sLbl}MotionActiveDt" = getDtNow() }
				else { atomicState."${sLbl}MotionInActiveDt" = getDtNow() }

				atomicState."${sLbl}oldMotionActive" = newact
				atomicState?."motion${cnt}UseMotionSettings" = null 		// clear automation state of schedule in use motion state
				atomicState?."motion${cnt}LastisBtwn" = false

				timersActive = (timersActive || atomicState."schedule${cnt}TimeActive") ? true : false

				cnt += 1
			}
			atomicState.scheduleTimersActive = timersActive
			atomicState.lastSched = null	// clear automation state of schedule in use
			atomicState.scheduleSchedActiveCount = numact
		}

		subscribeToEvents()
		scheduler()
	}
	app.updateLabel(getAutoTypeLabel())
	LogAction("Automation Label: ${getAutoTypeLabel()}", "info", true)

	//if(settings["backedUpData"] && atomicState?.restoreCompleted) { }

	state.remove("motionnullLastisBtwn")
	state.remove("motion1InBtwn")
	state.remove("motion2InBtwn")
	state.remove("motion3InBtwn")
	state.remove("motion4InBtwn")
	state.remove("TstatTurnedOff")
	state.remove("schedule{1}TimeActive")
	state.remove("schedule{2}TimeActive")
	state.remove("schedule{3}TimeActive")
	state.remove("schedule{4}TimeActive")
	state.remove("schedule{5}TimeActive")
	state.remove("schedule{6}TimeActive")
	state.remove("schedule{7}TimeActive")
	state.remove("schedule{8}TimeActive")
	state.remove("lastaway")

	state.remove("evalSched")
	state.remove("debugAppendAppName")   // cause Automations to re-check with parent for value
	state.remove("enRemDiagLogging")   // cause Automations to re-check with parent for value after updated is called
	state.remove("weatherDeviceInst")   // cause Automations to re-check with parent for value after updated is called

	scheduleAutomationEval(30)
}

def uninstAutomationApp() {
	//LogTrace("uninstAutomationApp")
	def autoType = getAutoType()
	if(autoType == "schMot") {
		def myID = getMyLockId()
		if(schMotTstat && myID && parent) {
			if(parent?.addRemoveVthermostat(schMotTstat.deviceNetworkId, false, myID)) {
				LogAction("uninstAutomationApp: cleanup virtual thermostat", "debug", true)
			}
		}
		if(schMotTstat && myID && parent) {
			if( parent?.remSenUnlock(atomicState?.remSenTstat, myID) ) { // attempt unlock old ID
				LogAction("uninstAutomationApp: Released remote sensor lock", "debug", true)
			}
		}
	}
	if(autoType == "nMode") {
		parent?.automationNestModeEnabled(false)
	}
}

def getCurAppLbl() { return app?.label?.toString() }

def getAutoTypeLabel() {
	LogTrace("getAutoTypeLabel()")
	def type = atomicState?.automationType
	def appLbl = getCurAppLbl()
	def newName = appName() == "${appLabel()}" ? "NST Automations" : "${appName()}"
	def typeLabel = ""
	def newLbl
	def dis = (atomicState?.disableAutomation == true) ? "\n(Disabled)" : ""

	if(type == "nMode")		{ typeLabel = "${newName} (NestMode)" }
	else if(type == "watchDog")	{ typeLabel = "Nest Location ${location.name} Watchdog"}
	else if(type == "remDiag")	{ typeLabel = "NST Diagnostics"}
	else if(type == "schMot")	{ typeLabel = "${newName} (${schMotTstat?.label})" }

	if(appLbl != "Nest Manager" && appLbl != "${appLabel()}") {
		if(appLbl.contains("\n(Disabled)")) {
			newLbl = appLbl.replaceAll('\\\n\\(Disabled\\)', '')
		} else {
			newLbl = appLbl
		}
	} else {
		newLbl = typeLabel
	}
	return "${newLbl}${dis}"
}

def getAppStateData() {
	return getState()
}

def getSettingsData() {
	def sets = []
	settings?.sort().each { st ->
		sets << st
	}
	return sets
}

def getSettingVal(var) {
	return settings[var] ?: null
}

def getStateVal(var) {
	return state[var] ?: null
}

/*
def getAutoIcon(type) {
	if(type) {
		switch(type) {
			case "remSen":
				return getAppImg("remote_sensor_icon.png")
				break
			case "fanCtrl":
				return getAppImg("fan_control_icon.png")
				break
			case "conWat":
				return getAppImg("open_window.png")
				break
			case "leakWat":
				return getAppImg("leak_icon.png")
				break
			case "humCtrl":
				return getAppImg("humidity_automation_icon.png")
				break
			case "extTmp":
				return getAppImg("external_temp_icon.png")
				break
			case "nMode":
				return getAppImg("mode_automation_icon.png")
				break
			case "schMot":
				return getAppImg("thermostat_automation_icon.png")
				break
			case "tMode":
				return getAppImg("mode_setpoints_icon.png")
				break
			case "watchDog":
				return getAppImg("watchdog_icon.png")
				break
		}
	}
}
*/

def automationsInst() {
	atomicState.isNestModesConfigured = 	isNestModesConfigured() ? true : false
	atomicState.isWatchdogConfigured = 	isWatchdogConfigured() ? true : false
	atomicState.isDiagnosticsConfigured = 	isDiagnosticsConfigured() ? true : false
	atomicState.isSchMotConfigured = 	isSchMotConfigured() ? true : false

	atomicState.isLeakWatConfigured = 	isLeakWatConfigured() ? true : false
	atomicState.isConWatConfigured = 	isConWatConfigured() ? true : false
	atomicState.isHumCtrlConfigured = 	isHumCtrlConfigured() ? true : false
	atomicState.isExtTmpConfigured = 	isExtTmpConfigured() ? true : false
	atomicState.isRemSenConfigured =	isRemSenConfigured() ? true : false
	atomicState.isTstatSchedConfigured = 	isTstatSchedConfigured() ? true : false
	atomicState.isFanCtrlConfigured = 	isFanCtrlSwConfigured() ? true : false
	atomicState.isFanCircConfigured = 	isFanCircConfigured() ? true : false
	atomicState?.isInstalled = true
}

def getAutomationsInstalled() {
	def list = []
	def aType = atomicState?.automationType
	switch(aType) {
		case "nMode":
			list.push(aType)
			break
		case "schMot":
			def tmp = [:]
			tmp[aType] = []
			if(isLeakWatConfigured()) 		{ tmp[aType].push("leakWat") }
			if(isConWatConfigured()) 		{ tmp[aType].push("conWat") }
			if(isHumCtrlConfigured()) 		{ tmp[aType].push("humCtrl") }
			if(isExtTmpConfigured()) 		{ tmp[aType].push("extTmp") }
			if(isRemSenConfigured())		{ tmp[aType].push("remSen") }
			if(isTstatSchedConfigured()) 		{ tmp[aType].push("tSched") }
			if(isFanCtrlSwConfigured()) 		{ tmp[aType].push("fanCtrl") }
			if(isFanCircConfigured()) 		{ tmp[aType].push("fanCirc") }
			if(tmp?.size()) { list.push(tmp) }
			break
		case "watchDog":
			list.push(aType)
			break
		case "remDiag":
			list.push(aType)
			break
	}
	LogTrace("getAutomationsInstalled List: $list")
	return list
}

def getAutomationType() {
	return atomicState?.automationType ?: null
}

def getAutoType() { return !parent ? "" : atomicState?.automationType }

def getIsAutomationDisabled() {
	def dis = atomicState?.disableAutomation
	return (dis != null && dis == true) ? true : false
}

def subscribeToEvents() {
	//Remote Sensor Subscriptions
	def autoType = getAutoType()
	def swlist = []

	//Nest Mode Subscriptions
	if(autoType == "nMode") {
		if(isNestModesConfigured()) {
			if(!settings?.nModePresSensor && !settings?.nModeSwitch && (settings?.nModeHomeModes || settings?.nModeAwayModes)) { subscribe(location, "mode", nModeGenericEvt) }
			if(settings?.nModePresSensor && !settings?.nModeSwitch) { subscribe(nModePresSensor, "presence", nModeGenericEvt) }
			if(settings?.nModeSwitch && !settings?.nModePresSensor) { subscribe(nModeSwitch, "switch", nModeGenericEvt) }

			def tstats = parent?.getTstats()
			def foundTstats
			if(tstats) {
				foundTstats = tstats?.collect { dni ->
					def d1 = parent.getThermostatDevice(dni)
					if(d1) {
						LogAction("Found: ${d1?.displayName} with (Id: ${dni?.key})", "debug", false)

						subscribe(d1, "nestThermostatMode", automationGenericEvt)
						subscribe(d1, "presence", automationGenericEvt)
					}
					return d1
				}
			}
			def t0 = []
			if(settings["nModerestrictionSwitchOn"]) { t0 = t0 + settings["nModerestrictionSwitchOn"] }
			if(settings["nModerestrictionSwitchOff"]) { t0 = t0 + settings["nModerestrictionSwitchOff"] }
			for(sw in t0) {
				if(swlist?.contains(sw)) {
					//log.trace "found $sw"
				} else {
					swlist.push(sw)
					subscribe(sw, "switch", automationGenericEvt)
				}
			}
		}
	}

	//ST Thermostat Motion
	if(autoType == "schMot") {
		def needThermTemp
		def needThermMode
		def needThermPres

		if(isSchMotConfigured()) {
			if(settings?.schMotWaterOff) {
				if(isLeakWatConfigured()) { subscribe(leakWatSensors, "water", leakWatSensorEvt) }
			}
			if(settings?.schMotContactOff) {
				if(isConWatConfigured()) {
					subscribe(conWatContacts, "contact", conWatContactEvt)
					def t0 = []
					if(settings["conWatrestrictionSwitchOn"]) { t0 = t0 + settings["conWatrestrictionSwitchOn"] }
					if(settings["conWatrestrictionSwitchOff"]) { t0 = t0 + settings["conWatrestrictionSwitchOff"] }
					for(sw in t0) {
						if(swlist?.contains(sw)) {
							//log.trace "found $sw"
						} else {
							swlist.push(sw)
							subscribe(sw, "switch", automationGenericEvt)
						}
					}
				}
			}
			if(settings?.schMotHumidityControl) {
				if(isHumCtrlConfigured()) {
					subscribe(humCtrlSwitches, "switch", automationGenericEvt)
					subscribe(humCtrlHumidity, "humidity", automationGenericEvt)
					if(!settings?.humCtrlUseWeather && settings?.humCtrlTempSensor) { subscribe(humCtrlTempSensor, "temperature", automationGenericEvt) }
					if(settings?.humCtrlUseWeather) {
						atomicState.NeedwUpd = true
						if(parent?.getWeatherDeviceInst()) {
							def weather = parent?.getWeatherDevice()
							if(weather) {
								subscribe(weather, "temperature", automationGenericEvt)
							}
						} else { LogAction("No weather device found", "error", true) }
					}
					def t0 = []
					if(settings["humCtrlrestrictionSwitchOn"]) { t0 = t0 + settings["humCtrlrestrictionSwitchOn"] }
					if(settings["humCtrlrestrictionSwitchOff"]) { t0 = t0 + settings["humCtrlrestrictionSwitchOff"] }
					for(sw in t0) {
						if(swlist?.contains(sw)) {
							//log.trace "found $sw"
						} else {
							swlist.push(sw)
							subscribe(sw, "switch", automationGenericEvt)
						}
					}
				}
			}

			if(settings?.schMotExternalTempOff) {
				if(isExtTmpConfigured()) {
					if(settings?.extTmpUseWeather) {
						atomicState.NeedwUpd = true
						if(parent?.getWeatherDeviceInst()) {
							def weather = parent?.getWeatherDevice()
							if(weather) {
								subscribe(weather, "temperature", extTmpGenericEvt)
								subscribe(weather, "dewpoint", extTmpGenericEvt)
							}
						} else { LogAction("No weather device found", "error", true) }
					}
					def t0 = []
					if(settings["extTmprestrictionSwitchOn"]) { t0 = t0 + settings["extTmprestrictionSwitchOn"] }
					if(settings["extTmprestrictionSwitchOff"]) { t0 = t0 + settings["extTmprestrictionSwitchOff"] }
					for(sw in t0) {
						if(swlist?.contains(sw)) {
							//log.trace "found $sw"
						} else {
							swlist.push(sw)
							subscribe(sw, "switch", automationGenericEvt)
						}
					}
					if(!settings?.extTmpUseWeather && settings?.extTmpTempSensor) { subscribe(extTmpTempSensor, "temperature", extTmpGenericEvt) }
					atomicState.extTmpChgWhileOnDt = getDtNow()
					atomicState.extTmpChgWhileOffDt = getDtNow()
				}
			}
			def senlist = []
			if(settings?.schMotRemoteSensor) {
				if(isRemSenConfigured()) {
					if(settings?.remSensorDay) {
						for(sen in settings?.remSensorDay) {
							if(senlist?.contains(sen)) {
								//log.trace "found $sen"
							} else {
								senlist.push(sen)
								subscribe(sen, "temperature", automationGenericEvt)
								subscribe(sen, "humidity", automationGenericEvt)
							}
						}
					}
				}
			}
			if(isTstatSchedConfigured()) {
			}
			if(settings?.schMotOperateFan) {
				if(isFanCtrlSwConfigured() && fanCtrlFanSwitches) {
					subscribe(fanCtrlFanSwitches, "switch", automationGenericEvt)
					subscribe(fanCtrlFanSwitches, "level", automationGenericEvt)
				}
				def t0 = []
				if(settings["fanCtrlrestrictionSwitchOn"]) { t0 = t0 + settings["fanCtrlrestrictionSwitchOn"] }
				if(settings["fanCtrlrestrictionSwitchOff"]) { t0 = t0 + settings["fanCtrlrestrictionSwitchOff"] }
				for(sw in t0) {
					if(swlist?.contains(sw)) {
						//log.trace "found $sw"
					} else {
						swlist.push(sw)
						subscribe(sw, "switch", automationGenericEvt)
					}
				}
			}
			if(settings?.schMotOperateFan || settings?.schMotRemoteSensor || settings?.schMotHumidityControl) {
				subscribe(schMotTstat, "thermostatFanMode", automationGenericEvt)
			}

			def schedList = getScheduleList()
			def sLbl
			def cnt = 1
			def prlist = []
			def mtlist = []
			schedList?.each { scd ->
				sLbl = "schMot_${scd}_"
				def restrict = atomicState?."sched${cnt}restrictions"
				def act = settings["${sLbl}SchedActive"]
				if(act) {
					if(atomicState?."schedule${cnt}SwEnabled") {
						if(restrict?.s1) {
							for(sw in settings["${sLbl}restrictionSwitchOn"]) {
								if(swlist?.contains(sw)) {
									//log.trace "found $sw"
								} else {
									swlist.push(sw)
									subscribe(sw, "switch", automationGenericEvt)
								}
							}
						}
						if(restrict?.s0) {
							for(sw in settings["${sLbl}restrictionSwitchOff"]) {
								if(swlist?.contains(sw)) {
									//log.trace "found $sw"
								} else {
									swlist.push(sw)
									subscribe(sw, "switch", automationGenericEvt)
								}
							}
						}
					}
					if(atomicState?."schedule${cnt}PresEnabled") {
						if(restrict?.p1) {
							for(pr in settings["${sLbl}restrictionPresHome"]) {
								if(prlist?.contains(pr)) {
									//log.trace "found $pr"
								} else {
									prlist.push(pr)
									subscribe(pr, "presence", automationGenericEvt)
								}
							}
						}
						if(restrict?.p0) {
							for(pr in settings["${sLbl}restrictionPresAway"]) {
								if(prlist?.contains(pr)) {
									//log.trace "found $pr"
								} else {
									prlist.push(pr)
									subscribe(pr, "presence", automationGenericEvt)
								}
							}
						}
					}
					if(atomicState?."schedule${cnt}MotionEnabled") {
						if(restrict?.m0) {
							for(mt in settings["${sLbl}Motion"]) {
								if(mtlist?.contains(mt)) {
									//log.trace "found $mt"
								} else {
									mtlist.push(mt)
									subscribe(mt, "motion", automationMotionEvt)
								}
							}
						}
					}
					if(atomicState?."schedule${cnt}SensorEnabled") {
						if(restrict?.sen0) {
							for(sen in settings["${sLbl}remSensor"]) {
								if(senlist?.contains(sen)) {
									//log.trace "found $sen"
								} else {
									senlist.push(sen)
									subscribe(sen, "temperature", automationGenericEvt)
								}
							}
						}
					}
				}
				cnt += 1
			}
			subscribe(schMotTstat, "thermostatMode", automationGenericEvt)
			subscribe(schMotTstat, "nestThermostatMode", automationGenericEvt)
			subscribe(schMotTstat, "thermostatOperatingState", automationGenericEvt)
			subscribe(schMotTstat, "temperature", automationGenericEvt)
			subscribe(schMotTstat, "presence", automationGenericEvt)
			subscribe(schMotTstat, "coolingSetpoint", automationGenericEvt)
			subscribe(schMotTstat, "heatingSetpoint", automationGenericEvt)
			subscribe(schMotTstat, "safetyTempExceeded", automationSafetyTempEvt)
			subscribe(location, "sunset", automationGenericEvt)
			subscribe(location, "sunrise", automationGenericEvt)
			subscribe(location, "mode", automationGenericEvt)
		}
	}
	//watchDog Subscriptions
	if(autoType == "watchDog") {
		// if(isWatchdogConfigured()) {
		def tstats = parent?.getTstats()
		def foundTstats

		if(tstats) {
			foundTstats = tstats?.collect { dni ->
				def d1 = parent.getThermostatDevice(dni)
				if(d1) {
					LogAction("Found: ${d1?.displayName} with (Id: ${dni?.key})", "debug", false)

					subscribe(d1, "temperature", automationGenericEvt)
					subscribe(d1, "safetyTempExceeded", automationSafetyTempEvt)
					subscribe(d1, "nestThermostatMode", automationGenericEvt)
					subscribe(d1, "thermostatMode", automationGenericEvt)
					subscribe(d1, "presence", automationGenericEvt)
					subscribe(location, "mode", automationGenericEvt)
				}
				return d1
			}
		}
		//Alarm status monitoring
		if(settings["${autoType}AlarmDevices"] && settings?."${pName}AllowAlarmNotif") {
			if(settings["${autoType}_Alert_1_Use_Alarm"] || settings["${autoType}_Alert_2_Use_Alarm"]) {
				subscribe(settings["${autoType}AlarmDevices"], "alarm", alarmAlertEvt)
			}
		}
	}

	//remDiag Subscriptions
	if(autoType == "remDiag") {

	}
}

def scheduler() {
	def random = new Random()
	def random_int = random.nextInt(60)
	def random_dint = random.nextInt(9)

	def autoType = getAutoType()
	if(autoType == "schMot" && atomicState?.scheduleSchedActiveCount && atomicState?.scheduleTimersActive) {
		LogAction("${autoType} scheduled (${random_int} ${random_dint}/5 * * * ?)", "info", true)
		schedule("${random_int} ${random_dint}/5 * * * ?", heartbeatAutomation)
	} else if(autoType != "remDiag") {
		LogAction("${autoType} scheduled (${random_int} ${random_dint}/30 * * * ?)", "info", true)
		schedule("${random_int} ${random_dint}/30 * * * ?", heartbeatAutomation)
	}
}

def heartbeatAutomation() {
	def autoType = getAutoType()
	LogTrace("Heartbeat ${autoType}: heartbeatAutomation()")
	def val = 900
	if(autoType == "schMot") {
		val = 220
	}
	if(getLastAutomationSchedSec() > val) {
		LogAction("${autoType} Heartbeat run", "trace", false)
		runAutomationEval()
	}
}

def defaultAutomationTime() {
	return 20
}

def scheduleAutomationEval(schedtime = defaultAutomationTime()) {
	def theTime = schedtime
	if(theTime < defaultAutomationTime()) { theTime = defaultAutomationTime() }
	def autoType = getAutoType()
	def random = new Random()
	def random_int = random.nextInt(6)  // this randomizes a bunch of automations firing at same time off same event
	def waitOverride = false
	switch(autoType) {
		case "nMode":
			if(theTime == defaultAutomationTime()) {
				theTime = 14 + random_int  // this has nMode fire first as it may change the Nest Mode
			}
			break
		case "schMot":
			if(theTime == defaultAutomationTime()) {
				theTime += random_int
			}
			def schWaitVal = settings?.schMotWaitVal?.toInteger() ?: 60
			if(schWaitVal > 120) { schWaitVal = 120 }
			def t0 = getLastschMotEvalSec()
			if((schWaitVal - t0) >= theTime ) {
				theTime = (schWaitVal - t0)
				waitOverride = true
			}
			//theTime = Math.min( Math.max(theTime,defaultAutomationTime()), 120)
			break
		case "watchDog":
			if(theTime == defaultAutomationTime()) {
				theTime = 35 + random_int  // this has watchdog fire last so other automations can finish changes
			}
			break
	}
	if(!atomicState?.evalSched) {
		runIn(theTime, "runAutomationEval", [overwrite: true])
		atomicState?.lastAutomationSchedDt = getDtNow()
		atomicState.evalSched = true
		atomicState.evalSchedLastTime = theTime
	} else {
		def t0 = atomicState?.evalSchedLastTime
		if(t0 == null) { t0 = 0 }
		def timeLeftPrev = t0 - getLastAutomationSchedSec()
		if(timeLeftPrev > (theTime + 5) || waitOverride) {
			if(Math.abs(timeLeftPrev - theTime) > 3) {
				runIn(theTime, "runAutomationEval", [overwrite: true])
				LogAction("scheduleAutomationEval: changed time ${timeLeftPrev} to ${theTime}", "debug", true)
			}
		} else { LogAction("scheduleAutomationEval: skipped time ${theTime} because ${timeLeftPrev}", "debug", true) }
	}
}

def getLastAutomationSchedSec() { return !atomicState?.lastAutomationSchedDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastAutomationSchedDt, null, "getLastAutomationSchedSec").toInteger() }

def runAutomationEval() {
	LogTrace("runAutomationEval")
	def autoType = getAutoType()
	atomicState.evalSched = false
	switch(autoType) {
		case "nMode":
			if(isNestModesConfigured()) {
				checkNestMode()
			}
			break
		case "schMot":
			if(atomicState?.needChildUpdate) {
				atomicState?.needChildUpdate = false
				parent?.setNeedChildUpdate()
			}
			if(isSchMotConfigured()) {
				schMotCheck()
			}
			break
		case "watchDog":
			if(isWatchdogConfigured()) {
				watchDogCheck()
			}
			break
		case "remDiag":
			if(isDiagnosticsConfigured()) {
				//remDiagCheck()
			}
			break
		default:
			LogAction("runAutomationEval: Invalid Option Received ${autoType}", "warn", true)
			break
	}
}

void sendAutoChgToDevice(dev, autoType, chgDesc) {
	if(dev && autoType && chgDesc) {
		try {
			dev?.whoMadeChanges(autoType?.toString(), chgDesc?.toString(), getDtNow().toString())
		} catch (ex) {
			log.error "sendAutoChgToDevice Exception:", ex
		}
	}
}

def sendEcoActionDescToDevice(dev, desc) {
	if(dev && desc) {
		try {
			dev?.ecoDesc(desc)		// THIS ONLY WORKS ON NEST THERMOSTATS
		} catch (ex) {
			log.error "sendEcoActionDescToDevice Exception:", ex
		}
	}
}

def getAutomationStats() {
	return [
		"lastUpdatedDt":atomicState?.lastUpdatedDt,
		"lastEvalDt":atomicState?.lastEvalDt,
		"lastEvent":atomicState?.lastEventData,
		"lastActionData":getAutoActionData(),
		"lastSchedDt":atomicState?.lastAutomationSchedDt,
		"lastExecVal":atomicState?.lastExecutionTime,
		"execAvgVal":(atomicState?.evalExecutionHistory != [] ? getAverageValue(atomicState?.evalExecutionHistory) : null)
	]
}

def storeLastAction(actionDesc, actionDt, autoType=null, dev=null) {
	if(actionDesc && actionDt) {

		def newVal = ["actionDesc":actionDesc, "dt":actionDt, "autoType":autoType]
		atomicState?.lastAutoActionData = newVal

		def list = atomicState?.detailActionHistory ?: []
		def listSize = 30
		if(list?.size() < listSize) {
			list.push(newVal)
		}
		else if(list?.size() > listSize) {
			def nSz = (list?.size()-listSize) + 1
			def nList = list?.drop(nSz)
			nList?.push(newVal)
			list = nList
		}
		else if(list?.size() == listSize) {
			def nList = list?.drop(1)
			nList?.push(newVal)
			list = nList
		}
		if(list) { atomicState?.detailActionHistory = list }

		if(dev) {
			sendAutoChgToDevice(dev, autoType, actionDesc)		// THIS ONLY WORKS ON NEST THERMOSTATS
		}
	}
}

def getAutoActionData() {
	if(atomicState?.lastAutoActionData) {
		return atomicState?.lastAutoActionData
	}
}

def automationGenericEvt(evt) {
	def startTime = now()
	def eventDelay = startTime - evt.date.getTime()
	LogAction("${evt?.name.toUpperCase()} Event | Device: ${evt?.displayName} | Value: (${strCapitalize(evt?.value)}) with a delay of ${eventDelay}ms", "trace", true)
	if(isRemSenConfigured() && settings?.vthermostat) {
		atomicState.needChildUpdate = true
	}
	if(settings?.humCtrlUseWeather && isHumCtrlConfigured()) {
		atomicState.NeedwUpd = true
	}
	doTheEvent(evt)
}

def doTheEvent(evt) {
	if(atomicState?.disableAutomation) { return }
	else {
		scheduleAutomationEval()
		storeLastEventData(evt)
	}
}

/******************************************************************************
|						WATCHDOG AUTOMATION LOGIC CODE						  |
*******************************************************************************/
def watchDogPrefix() { return "watchDog" }

def watchDogPage() {
	def pName = watchDogPrefix()
	dynamicPage(name: "watchDogPage", title: "Nest Location Watchdog", uninstall: false, install: true) {
		section("Notifications:") {
			def pageDesc = getNotifConfigDesc(pName)
			href "setNotificationPage", title: "Configured Alerts", description: pageDesc, params: ["pName":"${pName}", "allowSpeech":true, "allowAlarm":true, "showSchedule":true],
					state: (pageDesc ? "complete" : null), image: getAppImg("notification_icon.png")
			input "watDogNotifMissedEco", "bool", title: "Notify When Away and Not in Eco Mode?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("switch_on_icon.png")
		}
		//remove("Remove ${app?.label}!", "Last Chance!!!", "Warning!!! This action is not reversible\n\nThis Automation will be removed completely")
	}
}

def automationSafetyTempEvt(evt) {
	def startTime = now()
	def eventDelay = startTime - evt.date.getTime()
	LogAction("Event | Thermostat Safety Temp Exceeded: '${evt.displayName}' (${evt.value}) with a delay of ${eventDelay}ms", "trace", true)
	if(atomicState?.disableAutomation) { return }
	else {
		if(evt?.value == "true") {
			scheduleAutomationEval()
		}
	}
	storeLastEventData(evt)
}

// Alarms will repeat every watDogRepeatMsgDelay (1 hr default) ALL thermostats
def watchDogCheck() {
	if(atomicState?.disableAutomation) { return }
	else {
		def execTime = now()
		atomicState?.lastEvalDt = getDtNow()
		def tstats = parent?.getTstats()
		def foundTstats
		if(tstats) {
			foundTstats = tstats?.collect { dni ->
				def d1 = parent.getThermostatDevice(dni)
				if(d1) {
					def exceeded = d1?.currentValue("safetyTempExceeded")?.toString()
					if(exceeded == "true") {
						watchDogAlarmActions(d1.displayName, dni, "temp")
						LogAction("watchDogCheck: | Thermostat: ${d1?.displayName} Safety Temp Exceeded: ${exceeded}", "warn", true)
					} else {
						// This is allowing for warning if Nest has problem of system coming out of ECO while away
						def nestModeAway = (d1?.currentpresence?.toString() == "not present") ? true : false
						//def nestModeAway = (getNestLocPres() == "home") ? false : true
						if(nestModeAway) {
							def curMode = d1?.currentnestThermostatMode?.toString()
							if(!(curMode in ["eco", "off" ])) {
								watchDogAlarmActions(d1.displayName, dni, "eco")
								def pres = d1?.currentPresence?.toString()
								LogAction("watchDogCheck: | Thermostat: ${d1?.displayName} is Away and Mode Is Not in ECO | CurMode: (${curMode}) | CurrentPresence: (${pres})", "warn", true)
							}
						}
					}
					return d1
				}
			}
		}
		storeExecutionHistory((now()-execTime), "watchDogCheck")
	}
}

def watchDogAlarmActions(dev, dni, actType) {
	def pName = watchDogPrefix()
	//def allowNotif = (settings["${pName}NotificationsOn"] && (settings["${pName}NotifRecips"] || settings["${pName}NotifPhones"] || settings["${pName}UsePush"])) ? true : false
	def allowNotif = settings["${pName}NotificationsOn"] ? true : false
	def allowSpeech = allowNotif && settings?."${pName}AllowSpeechNotif" ? true : false
	def allowAlarm = allowNotif && settings?."${pName}AllowAlarmNotif" ? true : false
	def evtNotifMsg = ""
	def evtVoiceMsg = ""
	switch(actType) {
		case "temp":
			evtNotifMsg = "Safety Temp exceeded on ${dev}."
			evtVoiceMsg = evtNotifMsg
			break
		case "eco":
			if(settings["watDogNotifMissedEco"] == true) {
				evtNotifMsg = "Nest Location Home/Away Mode is 'Away' and thermostat [${dev}] is not in ECO."
				evtVoiceMsg = evtNotifMsg
			} else {return}
			break
	}
	if(getLastWatDogSafetyAlertDtSec(dni) > getWatDogRepeatMsgDelayVal()) {
		LogAction("watchDogAlarmActions() | ${evtNotifMsg}", "warn", true)

		if(allowNotif) {
			sendEventPushNotifications(evtNotifMsg, "Warning", pName)
			if(allowSpeech) {
				sendEventVoiceNotifications(voiceNotifString(evtVoiceMsg, pName), pName, "nmWatDogEvt_${app?.id}", true, "nmWatDogEvt_${app?.id}")
			}
			if(allowAlarm) {
				scheduleAlarmOn(pName)
			}
		} else {
			sendNofificationMsg("Warning", evtNotifMsg)
		}
		atomicState?."lastWatDogSafetyAlertDt${dni}" = getDtNow()
	}
}

def getLastWatDogSafetyAlertDtSec(dni) { return !atomicState?."lastWatDogSafetyAlertDt{$dni}" ? 10000 : GetTimeDiffSeconds(atomicState?."lastWatDogSafetyAlertDt${dni}", null, "getLastWatDogSafetyAlertDtSec").toInteger() }
def getWatDogRepeatMsgDelayVal() { return !watDogRepeatMsgDelay ? 3600 : watDogRepeatMsgDelay.toInteger() }

def isWatchdogConfigured() {
	return (atomicState?.automationType == "watchDog") ? true : false
}

/******************************************************************************
|						REMOTE DIAG AUTOMATION LOGIC CODE						  |
*******************************************************************************/
def remDiagPrefix() { return "remDiag" }

def diagnosticsPage() {
	def pName = remDiagPrefix()
	dynamicPage(name: "diagnosticsPage", title: "NST Manager Diagnostics", uninstall: false, install: true) {
		section("Status") {
			paragraph "There is nothing to show yet"
		}
	}
}

def isDiagnosticsConfigured() {
	return (atomicState?.automationType == "remDiag") ? true : false
}


/////////////////////THERMOSTAT AUTOMATION CODE LOGIC ///////////////////////

/****************************************************************************
|					REMOTE SENSOR AUTOMATION CODE			  				|
*****************************************************************************/

def remSenPrefix() { return "remSen" }

/*
def remSenLock(val, myId) {
	def res = false
	if(val && myId && !parent) {
		def lval = atomicState?."remSenLock${val}"
		if(!lval) {
			atomicState?."remSenLock${val}" = myId
			res = true
		} else if(lval == myId) { res = true }
	}
	return res
}

def remSenUnlock(val, myId) {
	def res = false
	if(val && myId && !parent) {
		def lval = atomicState?."remSenLock${val}"
		if(lval) {
			if(lval == myId) {
				atomicState?."remSenLock${val}" = null
				state.remove("remSenLock${val}" as String)
				res = true
			}
		} else { res = true }
	}
	return res
}
*/

//Requirements Section
def remSenCoolTempsReq()	{ return (settings?.remSenRuleType in [ "Cool", "Heat_Cool", "Cool_Circ", "Heat_Cool_Circ" ]) ? true : false }
def remSenHeatTempsReq()	{ return (settings?.remSenRuleType in [ "Heat", "Heat_Cool", "Heat_Circ", "Heat_Cool_Circ" ]) ? true : false }
def remSenDayHeatTempOk()	{ return (!remSenHeatTempsReq() || (remSenHeatTempsReq() && remSenDayHeatTemp)) ? true : false }
def remSenDayCoolTempOk()	{ return (!remSenCoolTempsReq() || (remSenCoolTempsReq() && remSenDayCoolTemp)) ? true : false }

def isRemSenConfigured() {
	def devOk = (settings?.remSensorDay) ? true : false
	return (devOk && settings?.remSenRuleType && remSenDayHeatTempOk() && remSenDayCoolTempOk() ) ? true : false
}

def getLastMotionActiveSec(mySched) {
	def sLbl = "schMot_${mySched}_"
	return !atomicState?."${sLbl}MotionActiveDt" ? 0 : GetTimeDiffSeconds(atomicState?."${sLbl}MotionActiveDt", null, "getLastMotionActiveSec").toInteger()
}

def getLastMotionInActiveSec(mySched) {
	def sLbl = "schMot_${mySched}_"
	return !atomicState?."${sLbl}MotionInActiveDt" ? 0 : GetTimeDiffSeconds(atomicState?."${sLbl}MotionInActiveDt", null, "getLastMotionInActiveSec").toInteger()
}

def automationMotionEvt(evt) {
	def startTime = now()
	def eventDelay = startTime - evt.date.getTime()
	LogAction("${evt?.name.toUpperCase()} Event | Device: '${evt?.displayName}' | Motion: (${strCapitalize(evt?.value)}) with a delay of ${eventDelay}ms", "trace", true)
	if(atomicState?.disableAutomation) { return }
	else {
		storeLastEventData(evt)
		def dorunIn = false
		def delay = 120
		def sLbl

		def mySched = getCurrentSchedule()
		def schedList = getScheduleList()
		def schName = ""
		for (cnt in schedList) {
			sLbl = "schMot_${cnt}_"
			def act = settings["${sLbl}SchedActive"]

			if(act && settings["${sLbl}Motion"]) {
				def str = settings["${sLbl}Motion"].toString()
				if(str.contains( evt.displayName)) {
					def oldActive = atomicState?."${sLbl}oldMotionActive"
					def newActive = isMotionActive(settings["${sLbl}Motion"])
					atomicState."${sLbl}oldMotionActive" = newActive
					if(oldActive != newActive) {
						if(newActive) {
							if(cnt == mySched) { delay = settings."${sLbl}MDelayValOn"?.toInteger() ?: 60 }
							atomicState."${sLbl}MotionActiveDt" = getDtNow()
						} else {
							if(cnt == mySched) { delay = settings."${sLbl}MDelayValOff"?.toInteger() ?: 30*60 }
							atomicState."${sLbl}MotionInActiveDt" = getDtNow()
						}
					}
					LogAction("Updating Schedule Motion Sensor State | Schedule: (${cnt} - ${getSchedLbl(cnt)}) | Previous Active: (${oldActive}) | Current Status: ($newActive)", "trace", true)
					if(cnt == mySched) { dorunIn = true }
				}
			}
		}
/*
		if(settings["${sLbl}MPresHome"] || settings["${sLbl}MPresAway"]) {
			if(settings["${sLbl}MPresHome"]) { if(!isSomebodyHome(settings["${sLbl}MPresHome"])) { dorunIn = false } }
			if(settings["${sLbl}MPresAway"]) { if(isSomebodyHome(settings["${sLbl}MPresAway"])) { dorunIn = false } }
		}
*/
		if(dorunIn) {
			LogAction("Automation Schedule Motion | Scheduling Delay Check: ($delay sec) | Schedule: ($mySched - ${getSchedLbl(mySched)})", "trace", true)
			def val = Math.min( Math.max(delay,defaultAutomationTime()), 60)
			scheduleAutomationEval(val)
		} else {
			def str = "Motion Event | Skipping Motion Check: "
			if(mySched) {
				str += "Motion Sensor is Not Used in Active Schedule (#${mySched} - ${getSchedLbl(getCurrentSchedule())})"
			} else {
				str += "No Active Schedule"
			}
			LogAction(str, "info", true)
		}
	}
}

def isMotionActive(sensors) {
	def result
	sensors?.each { sen ->
		if(sen) {
			def sval = sen?.currentState("motion").value
			if(sval == "active") { result = true }
		}
	}
	return result
	//return sensors?.currentState("motion")?.value.equals("active") ? true : false
}

def getDeviceTempAvg(items) {
	def tmpAvg = []
	def tempVal = 0
	if(!items) { return tempVal }
	else if(items?.size() > 1) {
		tmpAvg = items*.currentTemperature
		if(tmpAvg && tmpAvg?.size() > 1) { tempVal = (tmpAvg?.sum().toDouble() / tmpAvg?.size().toDouble()).round(1) } //
	}
	else { tempVal = getDeviceTemp(items) }
	return tempVal.toDouble()
}

def remSenShowTempsPage() {
	dynamicPage(name: "remSenShowTempsPage", uninstall: false) {
		if(settings?.remSensorDay) {
			section("Default Sensor Temps: (Schedules can override)") {
				def cnt = 0
				def rCnt = settings?.remSensorDay?.size()
				def str = ""
				str += "Sensor Temp (average): (${getDeviceTempAvg(settings?.remSensorDay)}°${getTemperatureScale()})\n│"
				settings?.remSensorDay?.each { t ->
					cnt = cnt+1
					str += "${(cnt >= 1) ? "${(cnt == rCnt) ? "\n└" : "\n├"}" : "\n└"} ${t?.label}: ${(t?.label?.toString()?.length() > 10) ? "\n${(rCnt == 1 || cnt == rCnt) ? "    " : "│"}└ " : ""}(${getDeviceTemp(t)}°${getTemperatureScale()})"
				}
				paragraph "${str}", state: "complete", image: getAppImg("temperature_icon.png")
			}
		}
	}
}

def remSendoSetCool(chgval, onTemp, offTemp) {
	def remSenTstat = settings?.schMotTstat
	def remSenTstatMir = settings?.schMotTstatMir

	try {
		def hvacMode = remSenTstat ? remSenTstat?.currentThermostatMode.toString() : null
		def curCoolSetpoint = getTstatSetpoint(remSenTstat, "cool")
		def curHeatSetpoint = getTstatSetpoint(remSenTstat, "heat")
		def tempChangeVal = !remSenTstatTempChgVal ? 5.0 : Math.min(Math.max(remSenTstatTempChgVal.toDouble(), 2.0), 5.0)
		def maxTempChangeVal = tempChangeVal * 3

		chgval = (chgval > (onTemp + maxTempChangeVal)) ? onTemp + maxTempChangeVal : chgval
		chgval = (chgval < (offTemp - maxTempChangeVal)) ? offTemp - maxTempChangeVal : chgval
		if(chgval != curCoolSetpoint) {
			scheduleAutomationEval(60)
			def cHeat = null
			if(hvacMode in ["auto"]) {
				if(curHeatSetpoint >= (offTemp-tempChangeVal)) {
					cHeat = offTemp - tempChangeVal
					LogAction("Remote Sensor: HEAT - Adjusting HeatSetpoint to (${cHeat}°${getTemperatureScale()}) to allow COOL setting", "info", true)
					if(remSenTstatMir) { remSenTstatMir*.setHeatingSetpoint(cHeat) }
				}
			}
			if(setTstatAutoTemps(remSenTstat, chgval, cHeat, "remSen")) {
				//LogAction("Remote Sensor: COOL - Adjusting CoolSetpoint to (${chgval}°${getTemperatureScale()}) ", "info", true)
				//storeLastAction("Adjusted Cool Setpoint to (${chgval}°${getTemperatureScale()}) Heat Setpoint to (${cHeat}°${getTemperatureScale()})", getDtNow(), "remSen")
				if(remSenTstatMir) { remSenTstatMir*.setCoolingSetpoint(chgval) }
			}
			return true // let all this take effect
		} else {
			LogAction("Remote Sensor: COOL - CoolSetpoint is already (${chgval}°${getTemperatureScale()}) ", "info", true)
		}

	} catch (ex) {
		log.error "remSendoSetCool Exception:", ex
		parent?.sendExceptionData(ex, "remSendoSetCool", true, getAutoType())
	}
	return false
}

def remSendoSetHeat(chgval, onTemp, offTemp) {
	def remSenTstat = schMotTstat
	def remSenTstatMir = schMotTstatMir

	try {
		def hvacMode = remSenTstat ? remSenTstat?.currentThermostatMode.toString() : null
		def curCoolSetpoint = getTstatSetpoint(remSenTstat, "cool")
		def curHeatSetpoint = getTstatSetpoint(remSenTstat, "heat")
		def tempChangeVal = !remSenTstatTempChgVal ? 5.0 : Math.min(Math.max(remSenTstatTempChgVal.toDouble(), 2.0), 5.0)
		def maxTempChangeVal = tempChangeVal * 3

		chgval = (chgval < (onTemp - maxTempChangeVal)) ? onTemp - maxTempChangeVal : chgval
		chgval = (chgval > (offTemp + maxTempChangeVal)) ? offTemp + maxTempChangeVal : chgval
		if(chgval != curHeatSetpoint) {
			scheduleAutomationEval(60)
			def cCool = null
			if(hvacMode in ["auto"]) {
				if(curCoolSetpoint <= (offTemp+tempChangeVal)) {
					cCool = offTemp + tempChangeVal
					LogAction("Remote Sensor: COOL - Adjusting CoolSetpoint to (${cCool}°${getTemperatureScale()}) to allow HEAT setting", "info", true)
					if(remSenTstatMir) { remSenTstatMir*.setCoolingSetpoint(cCool) }
				}
			}
			if(setTstatAutoTemps(remSenTstat, cCool, chgval, "remSen")) {
				//LogAction("Remote Sensor: HEAT - Adjusting HeatSetpoint to (${chgval}°${getTemperatureScale()})", "info", true)
				//storeLastAction("Adjusted Heat Setpoint to (${chgval}°${getTemperatureScale()}) Cool Setpoint to (${cCool}°${getTemperatureScale()})", getDtNow(), "remSen")
				if(remSenTstatMir) { remSenTstatMir*.setHeatingSetpoint(chgval) }
			}
			return true // let all this take effect
		} else {
			LogAction("Remote Sensor: HEAT - HeatSetpoint is already (${chgval}°${getTemperatureScale()})", "info", true)
		}

	} catch (ex) {
		log.error "remSendoSetHeat Exception:", ex
		parent?.sendExceptionData(ex, "remSendoSetHeat", true, getAutoType())
	}
	return false
}

/*
def getRemSenModeOk() {
	def result = false
	if(settings?.remSensorDay ) { result = true }
	//log.debug "getRemSenModeOk: $result"
	return result
}
*/

private remSenCheck() {
	LogTrace("remSenCheck")
	if(atomicState?.disableAutomation) { return }
	try {
		def remSenTstat = settings?.schMotTstat
		def remSenTstatMir = settings?.schMotTstatMir

		def execTime = now()
		//atomicState?.lastEvalDt = getDtNow()

		def noGoDesc = ""
		if( !settings?.remSensorDay || !remSenTstat) {
			noGoDesc += !settings?.remSensorDay ? "Missing Required Sensor Selections" : ""
			noGoDesc += !remSenTstat ? "Missing Required Thermostat device" : ""
			LogAction("Remote Sensor NOT Evaluating Status: ${noGoDesc}", "warn", true)
		} else {
			//log.info "remSenCheck: Evaluating Event"

			def hvacMode = remSenTstat ? remSenTstat?.currentnestThermostatMode.toString() : null
			if(hvacMode in [ "off", "eco"] ) {
				LogAction("Remote Sensor: Skipping Evaluation; The Current Thermostat Mode is '${strCapitalize(hvacMode)}'", "info", true)
				disableOverrideTemps()
				storeExecutionHistory((now() - execTime), "remSenCheck")
				return
			}

			def reqSenHeatSetPoint = getRemSenHeatSetTemp(hvacMode)
			def reqSenCoolSetPoint = getRemSenCoolSetTemp(hvacMode)
			def threshold = getRemoteSenThreshold()

			if(hvacMode in ["auto"]) {
				// check that requested setpoints make sense & notify
				def coolheatDiff = Math.abs(reqSenCoolSetPoint - reqSenHeatSetPoint)
				if( !((reqSenCoolSetPoint > reqSenHeatSetPoint) && (coolheatDiff >= 2)) ) {
					LogAction("remSenCheck: Invalid Setpoints with auto mode: (${reqSenCoolSetPoint})/(${reqSenHeatSetPoint}, ${threshold})", "warn", true)
					storeExecutionHistory((now() - execTime), "remSenCheck")
					return
				}
			}

			def tempChangeVal = !remSenTstatTempChgVal ? 5.0 : Math.min(Math.max(remSenTstatTempChgVal.toDouble(), 2.0), 5.0)
			def maxTempChangeVal = tempChangeVal * 3
			def curTstatTemp = getDeviceTemp(remSenTstat).toDouble()
			def curSenTemp = (settings?.remSensorDay) ? getRemoteSenTemp().toDouble() : null

			def curTstatOperState = remSenTstat?.currentThermostatOperatingState.toString()
			def curTstatFanMode = remSenTstat?.currentThermostatFanMode.toString()
			def fanOn = (curTstatFanMode == "on" || curTstatFanMode == "circulate") ? true : false
			def curCoolSetpoint = getTstatSetpoint(remSenTstat, "cool")
			def curHeatSetpoint = getTstatSetpoint(remSenTstat, "heat")
			def acRunning = (curTstatOperState == "cooling") ? true : false
			def heatRunning = (curTstatOperState == "heating") ? true : false

			LogAction("remSenCheck: Rule Type: ${getEnumValue(remSenRuleEnum("heatcool"), settings?.remSenRuleType)}", "info", false)
			LogAction("remSenCheck: Sensor Temp: ${curSenTemp}", "info", false)
			LogAction("remSenCheck: Thermostat Info - ( Temperature: (${curTstatTemp}) | HeatSetpoint: (${curHeatSetpoint}) | CoolSetpoint: (${curCoolSetpoint}) | HvacMode: (${hvacMode}) | OperatingState: (${curTstatOperState}) | FanMode: (${curTstatFanMode}) )", "info", false)
			LogAction("remSenCheck: Desired Temps - Heat: ${reqSenHeatSetPoint} | Cool: ${reqSenCoolSetPoint}", "info", false)
			LogAction("remSenCheck: Threshold Temp: ${threshold} | Change Temp Increments: ${tempChangeVal}", "info", false)

/*

// This does not use mode filters, as the automation could have a bunch of settings in place, and suddenly stopping
// is indeterminate as to what settings to leave or change

			def modeOk = true
			if(!modeOk || !getRemSenModeOk()) {
				noGoDesc = ""
				noGoDesc += (!modeOk && getRemSenModeOk()) ? "Mode Filters are set; the current mode is not selected for Evaluation" : ""
				noGoDesc += (!getRemSenModeOk() && modeOk) ? "This mode is not selected for evaluation" : ""

// if we have heat on, ac on, or fan on, turn them off once

				if(atomicState?.haveRun) {
					if(settings?.remSenRuleType in ["Cool", "Heat_Cool", "Heat_Cool_Circ"]
						&& atomicState?.remSenCoolOn != null && !atomicState.remSenCoolOn
						&& (hvacMode in ["cool","auto"])
						&& acRunning) {

						def onTemp = reqSenCoolSetPoint + threshold
						def offTemp = reqSenCoolSetPoint
						chgval = curTstatTemp + tempChangeVal
						if(remSendoSetCool(chgval, onTemp, offTemp)) {
							noGoDesc += "   Turning off COOL due to mode change"
						}
						atomicState?.remSenCoolOn = false
					}

					if(settings?.remSenRuleType in ["Heat", "Heat_Cool", "Heat_Cool_Circ"]
						&& atomicState?.remSenHeatOn != null && !atomicState.remSenHeatOn
						&& (hvacMode in ["heat", "emergency heat", "auto"])
						&& heatRunning) {

						def onTemp = reqSenHeatSetPoint - threshold
						def offTemp = reqSenHeatSetPoint
						chgval = curTstatTemp - tempChangeVal
						if(remSendoSetHeat(chgval, onTemp, offTemp)) {
							noGoDesc += "   Turning off HEAT due to mode change"
						}
						atomicState?.remSenHeatOn = false
					}

					atomicState.haveRun = false
				}
				LogAction("Remote Sensor: Skipping Evaluation; Evaluation Status: ${noGoDesc}", "info", true)
				storeExecutionHistory((now() - execTime), "remSenCheck")
				return
			}
			atomicState.haveRun = true
*/

			def chg = false
			def chgval = 0
			if(hvacMode in ["cool","auto"]) {
				//Changes Cool Setpoints
				if(settings?.remSenRuleType in ["Cool", "Heat_Cool", "Heat_Cool_Circ"]) {
					def onTemp = reqSenCoolSetPoint + threshold
					def offTemp = reqSenCoolSetPoint
					def turnOn = false
					def turnOff = false

					LogAction("Remote Sensor: COOL - (Sensor Temp: ${curSenTemp} - CoolSetpoint: ${reqSenCoolSetPoint})", "trace", true)
					if(curSenTemp <= offTemp) {
						turnOff = true
					} else if(curSenTemp >= onTemp) {
						turnOn = true
					}

					if(turnOff && acRunning) {
						chgval = curTstatTemp + tempChangeVal
						chg = true
						LogAction("Remote Sensor: COOL - Adjusting CoolSetpoint to Turn Off Thermostat", "info", true)
						acRunning = false
						atomicState?.remSenCoolOn = false
					} else if(turnOn && !acRunning) {
						chgval = curTstatTemp - tempChangeVal
						chg = true
						acRunning = true
						atomicState.remSenCoolOn = true
						LogAction("Remote Sensor: COOL - Adjusting CoolSetpoint to Turn On Thermostat", "info", true)
					} else {
						// logic to decide if we need to nudge thermostat to keep it on or off
						if(acRunning) {
							chgval = curTstatTemp - tempChangeVal
							atomicState.remSenCoolOn = true
						} else {
							chgval = curTstatTemp + tempChangeVal
							atomicState?.remSenCoolOn = false
						}
						def coolDiff1 = Math.abs(curTstatTemp - curCoolSetpoint)
						LogAction("Remote Sensor: COOL - coolDiff1: ${coolDiff1} tempChangeVal: ${tempChangeVal}", "trace", false)
						if(coolDiff1 < (tempChangeVal / 2)) { //
							chg = true
							LogAction("Remote Sensor: COOL - Adjusting CoolSetpoint to maintain state", "info", true)
						}
					}
					if(chg) {
						if(remSendoSetCool(chgval, onTemp, offTemp)) {
							storeExecutionHistory((now() - execTime), "remSenCheck")
							return // let all this take effect
						}

					} else {
						LogAction("Remote Sensor: NO CHANGE TO COOL - CoolSetpoint is (${curCoolSetpoint}°${getTemperatureScale()}) ", "info", false)
					}
				}
			}

			chg = false
			chgval = 0

			LogAction("remSenCheck: Thermostat Info - ( Temperature: (${curTstatTemp}) | HeatSetpoint: (${curHeatSetpoint}) | CoolSetpoint: (${curCoolSetpoint}) | HvacMode: (${hvacMode}) | OperatingState: (${curTstatOperState}) | FanMode: (${curTstatFanMode}) )", "info", false)

			//Heat Functions.
			if(hvacMode in ["heat", "emergency heat", "auto"]) {
				if(settings?.remSenRuleType in ["Heat", "Heat_Cool", "Heat_Cool_Circ"]) {
					def onTemp = reqSenHeatSetPoint - threshold
					def offTemp = reqSenHeatSetPoint
					def turnOn = false
					def turnOff = false

					LogAction("Remote Sensor: HEAT - (Sensor Temp: ${curSenTemp} - HeatSetpoint: ${reqSenHeatSetPoint})", "trace", false)
					if(curSenTemp <= onTemp) {
						turnOn = true
					} else if(curSenTemp >= offTemp) {
						turnOff = true
					}

					if(turnOff && heatRunning) {
						chgval = curTstatTemp - tempChangeVal
						chg = true
						LogAction("Remote Sensor: HEAT - Adjusting HeatSetpoint to Turn Off Thermostat", "info", true)
						heatRunning = false
						atomicState.remSenHeatOn = false
					} else if(turnOn && !heatRunning) {
						chgval = curTstatTemp + tempChangeVal
						chg = true
						LogAction("Remote Sensor: HEAT - Adjusting HeatSetpoint to Turn On Thermostat", "info", true)
						atomicState.remSenHeatOn = true
						heatRunning = true
					} else {
						// logic to decide if we need to nudge thermostat to keep it on or off
						if(heatRunning) {
							chgval = curTstatTemp + tempChangeVal
							atomicState.remSenHeatOn = true
						} else {
							chgval = curTstatTemp - tempChangeVal
							atomicState.remSenHeatOn = false
						}
						def heatDiff1 = Math.abs(curTstatTemp - curHeatSetpoint)
						LogAction("Remote Sensor: HEAT - heatDiff1: ${heatDiff1} tempChangeVal: ${tempChangeVal}", "trace", false)
						if(heatDiff1 < (tempChangeVal / 2)) {
							chg = true
							LogAction("Remote Sensor: HEAT - Adjusting HeatSetpoint to maintain state", "info", true)
						}
					}
					if(chg) {
						if(remSendoSetHeat(chgval, onTemp, offTemp)) {
							storeExecutionHistory((now() - execTime), "remSenCheck")
							return // let all this take effect
						}
					} else {
						LogAction("Remote Sensor: NO CHANGE TO HEAT - HeatSetpoint is already (${curHeatSetpoint}°${getTemperatureScale()})", "info", false)
					}
				}
			}
		}
/*
			//
			// if all thermostats (primary and mirrors) are Nest, then AC/HEAT & fan may be off (or set back) with away mode. (depends on user's home/away assist settings in Nest)
			// if thermostats were not all Nest, then non Nest units could still be on for AC/HEAT or FAN
			// current presumption in this implementation is:
			//	they are all nests or integrated with Nest (Works with Nest) as we don't have away/home temps for each mirror thermostats.  (They could be mirrored from primary)
			//	all thermostats in an automation are in the same Nest structure, so that all share home/away settings
			//
*/
		storeExecutionHistory((now() - execTime), "remSenCheck")
	} catch (ex) {
		log.error "remSenCheck Exception:", ex
		parent?.sendExceptionData(ex, "remSenCheck", true, getAutoType())
	}
}

def getRemSenTempsToList() {
	def mySched = getCurrentSchedule()
	def sensors
	if(mySched) {
		def sLbl = "schMot_${mySched}_"
		if(settings["${sLbl}remSensor"]) {
			sensors = settings["${sLbl}remSensor"]
		}
	}
	if(!sensors) { sensors = settings?.remSensorDay }
	if(sensors?.size() >= 1) {
		def info = []
		sensors?.sort().each {
			info.push("${it?.displayName}": " ${it?.currentTemperature.toString()}°${getTemperatureScale()}")
		}
		return info
	}
}

def getDeviceTemp(dev) {
	return dev ? dev?.currentValue("temperature")?.toString().replaceAll("\\[|\\]", "").toDouble() : 0
}

def getTstatSetpoint(tstat, type) {
	if(tstat) {
		if(type == "cool") {
			def coolSp = tstat?.currentCoolingSetpoint
			//log.debug "getTstatSetpoint(cool): $coolSp"
			return coolSp ? coolSp.toDouble() : 0
		} else {
			def heatSp = tstat?.currentHeatingSetpoint
			//log.debug "getTstatSetpoint(heat): $heatSp"
			return heatSp ? heatSp.toDouble() : 0
		}
	}
	else { return 0 }
}

def getRemoteSenThreshold() {
	def threshold = settings?.remSenTempDiffDegrees
	def mySched = getCurrentSchedule()
	if(mySched) {
		def sLbl = "schMot_${mySched}_"
		if(settings["${sLbl}remSenThreshold"]) {
			threshold = settings["${sLbl}remSenThreshold"]
		}
	}
	def theMin = getTemperatureScale() == "C" ? 0.3 : 0.6
	threshold = !threshold ? 2.0 : Math.min(Math.max(threshold.toDouble(),theMin), 4.0)
	return threshold.toDouble()
}

def getRemoteSenTemp() {
	def mySched = getCurrentSchedule()
	if(!atomicState.remoteTempSourceStr) { atomicState.remoteTempSourceStr = null }
	if(!atomicState.currentSchedNum) { atomicState.currentSchedNum = null }
	def sens
	if(mySched) {
		def sLbl = "schMot_${mySched}_"
		if(settings["${sLbl}remSensor"]) {
			atomicState.remoteTempSourceStr = "Schedule"
			atomicState.currentSchedNum = mySched
			sens = settings["${sLbl}remSensor"]
			return getDeviceTempAvg(sens).toDouble()
		}
	}
	if(settings?.remSensorDay) {
		atomicState.remoteTempSourceStr = "Remote Sensor"
		atomicState.currentSchedNum = null
		return getDeviceTempAvg(settings?.remSensorDay).toDouble()
	}
	else {
		atomicState.remoteTempSourceStr = "Thermostat"
		atomicState.currentSchedNum = null
		return getDeviceTemp(schMotTstat).toDouble()
/*
	else {
		LogAction("getRemoteSenTemp: No Temperature Found!", "warn", true)
		return 0.0
*/
	}
}

def fixTempSetting(Double temp) {
	def newtemp = temp
	if(temp != null) {
		if(getTemperatureScale() == "C") {
			if(temp > 35) {    // setting was done in F
				newtemp = roundTemp( ((newtemp - 32.0) * (5 / 9)) as Double) //
			}
		} else if(getTemperatureScale() == "F") {
			if(temp < 40) {    // setting was done in C
				newtemp = roundTemp( (((newtemp * (9 / 5)) as Double) + 32.0) ).toInteger() //
			}
		}
	}
	return newtemp
}

def setRemoteSenTstat(val) {
	LogAction("setRemoteSenTstat $val", "info", true)
	atomicState.remSenTstat = val
}

def getRemSenCoolSetTemp(curMode=null, useCurrent=true) {
	def coolTemp
	def theMode = curMode != null ? curMode : null
	if(theMode == null) {
		def tstat = schMotTstat
		theMode = tstat ? tstat?.currentnestThermostatMode.toString() : null
	}
	if(theMode != "eco") {
		if(getLastOverrideCoolSec() < (3600 * 4)) {
			if(atomicState?.coolOverride != null) {
				coolTemp = fixTempSetting(atomicState?.coolOverride.toDouble())
			}
		} else { atomicState?.coolOverride = null }

		if(coolTemp == null) {
			def mySched = getCurrentSchedule()
			if(mySched) {
				def useMotion = atomicState?."motion${mySched}UseMotionSettings"
				def hvacSettings = atomicState?."sched${mySched}restrictions"
				coolTemp = !useMotion ? hvacSettings?.ctemp : hvacSettings?.mctemp ?: hvacSettings?.ctemp
			}
			if(coolTemp == null && remSenDayCoolTemp) {
				coolTemp = remSenDayCoolTemp.toDouble()
			}

			if(coolTemp == null) {
				def desiredCoolTemp = getGlobalDesiredCoolTemp()
				if(desiredCoolTemp) { coolTemp = desiredCoolTemp.toDouble() }
			}

			if(coolTemp) {
				coolTemp = fixTempSetting(coolTemp)
			}
		}
	}
	if(coolTemp == null && useCurrent) {
		coolTemp = schMotTstat ? getTstatSetpoint(schMotTstat, "cool") : coolTemp
	}
	return coolTemp
}

def getRemSenHeatSetTemp(curMode=null, useCurrent=true) {
	def heatTemp
	def theMode = curMode != null ? curMode : null
	if(theMode == null) {
		def tstat = schMotTstat
		theMode = tstat ? tstat?.currentnestThermostatMode.toString() : null
	}
	if(theMode != "eco") {
		if(getLastOverrideHeatSec() < (3600 * 4)) {
			if(atomicState?.heatOverride != null) {
				heatTemp = fixTempSetting(atomicState.heatOverride.toDouble())
			}
		} else { atomicState?.heatOverride = null }

		if(heatTemp == null) {
			def mySched = getCurrentSchedule()
			if(mySched) {
				def useMotion = atomicState?."motion${mySched}UseMotionSettings"
				def hvacSettings = atomicState?."sched${mySched}restrictions"
				heatTemp = !useMotion ? hvacSettings?.htemp : hvacSettings?.mhtemp ?: hvacSettings?.htemp
			}
			if(heatTemp == null && remSenDayHeatTemp) {
				heatTemp = remSenDayHeatTemp.toDouble()
			}

			if(heatTemp == null) {
				def desiredHeatTemp = getGlobalDesiredHeatTemp()
				if(desiredHeatTemp) { heatTemp = desiredHeatTemp.toDouble() }
			}

			if(heatTemp) {
				heatTemp = fixTempSetting(heatTemp)
			}
		}
	}

	if(heatTemp == null && useCurrent) {
		heatTemp = schMotTstat ? getTstatSetpoint(schMotTstat, "heat") : heatTemp
	}
	return heatTemp
}


// TODO When a temp change is sent to virtual device, it lasts for 4 hours, next turn off, or next schedule change, then we return to automation settings
// Other choices could be to change the schedule setpoint permanently if one is active, or allow folks to set timer

def getLastOverrideCoolSec() { return !atomicState?.lastOverrideCoolDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastOverrideCoolDt, null, "getLastOverrideCoolSec").toInteger() }
def getLastOverrideHeatSec() { return !atomicState?.lastOverrideHeatDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastOverrideHeatDt, null, "getLastOverrideHeatSec").toInteger() }

def disableOverrideTemps() {
	if(atomicState?.heatOverride || atomicState?.coolOverride) {
		atomicState?.coolOverride = null
		atomicState?.heatOverride = null
		atomicState?.lastOverrideCoolDt = null
		atomicState?.lastOverrideHeatDt = null
		LogAction("disableOverrideTemps: Disabling Override temps", "trace", true)
	}
}

def remSenTempUpdate(temp, mode) {
	LogAction("remSenTempUpdate(${temp}, ${mode})", "trace", false)

	def res = false
	if(atomicState?.disableAutomation) { return res }
	switch(mode) {
		case "heat":
			if(remSenHeatTempsReq()) {
				LogAction("remSenTempUpdate Set Heat Override to: ${temp} for 4 hours", "trace", true)
				atomicState?.heatOverride = temp.toDouble()
				atomicState?.lastOverrideHeatDt = getDtNow()
				scheduleAutomationEval()
				res = true
			}
			break
		case "cool":
			if(remSenCoolTempsReq()) {
				LogAction("remSenTempUpdate Set Cool Override to: ${temp} for 4 hours", "trace", true)
				atomicState?.coolOverride = temp.toDouble()
				atomicState?.lastOverrideCoolDt = getDtNow()
				scheduleAutomationEval()
				res = true
			}
			break
		default:
			LogAction("remSenTempUpdate Invalid Request: ${mode}", "warn", true)
			break
	}
	return res
}

def remSenRuleEnum(type=null) {
	// Determines that available rules to display based on the selected thermostats capabilites.
	def canCool = atomicState?.schMotTstatCanCool ? true : false
	def canHeat = atomicState?.schMotTstatCanHeat ? true : false
	def hasFan = atomicState?.schMotTstatHasFan ? true : false

	//log.debug "remSenRuleEnum -- hasFan: $hasFan (${atomicState?.schMotTstatHasFan} | canCool: $canCool (${atomicState?.schMotTstatCanCool} | canHeat: $canHeat (${atomicState?.schMotTstatCanHeat}"
	def vals = []
	if (type) {
		if (type == "fan") {
			if (canCool && !canHeat && hasFan) { vals = ["Circ":"Circulate(Fan)", "Cool_Circ":"Cool/Circulate(Fan)"] }
			else if (!canCool && canHeat && hasFan) { vals = ["Circ":"Circulate(Fan)", "Heat_Circ":"Heat/Circulate(Fan)"] }
			else if (!canCool && !canHeat && hasFan) { vals = ["Circ":"Circulate(Fan)"] }
			else { vals = [ "Circ":"Circulate(Fan)", "Heat_Cool_Circ":"Auto/Circulate(Fan)", "Heat_Circ":"Heat/Circulate(Fan)", "Cool_Circ":"Cool/Circulate(Fan)" ] }
		}
		else if (type == "heatcool") {
			if (!canCool && canHeat) { vals = ["Heat":"Heat"] }
			else if (canCool && !canHeat) { vals = ["Cool":"Cool"] }
			else { vals = ["Heat_Cool":"Auto", "Heat":"Heat", "Cool":"Cool"] }
		}
		else { LogAction("remSenRuleEnum: Invalid Type ($type)", "error", true) }
	}
	else {
		if (canCool && !canHeat && hasFan) { vals = ["Cool":"Cool", "Circ":"Circulate(Fan)", "Cool_Circ":"Cool/Circulate(Fan)"] }
		else if (canCool && !canHeat && !hasFan) { vals = ["Cool":"Cool"] }
		else if (!canCool && canHeat && hasFan) { vals = ["Circ":"Circulate(Fan)", "Heat":"Heat", "Heat_Circ":"Heat/Circulate(Fan)"] }
		else if (!canCool && canHeat && !hasFan) { vals = ["Heat":"Heat"] }
		else if (!canCool && !canHeat && hasFan) { vals = ["Circ":"Circulate(Fan)"] }
		else if (canCool && canHeat && !hasFan) { vals = ["Heat_Cool":"Auto", "Heat":"Heat", "Cool":"Cool"] }
		else { vals = [ "Heat_Cool":"Auto", "Heat":"Heat", "Cool":"Cool", "Circ":"Circulate(Fan)", "Heat_Cool_Circ":"Auto/Circulate(Fan)", "Heat_Circ":"Heat/Circulate(Fan)", "Cool_Circ":"Cool/Circulate(Fan)" ] }
	}
	//log.debug "remSenRuleEnum vals: $vals"
	return vals
}

/************************************************************************
|				    FAN CONTROL AUTOMATION CODE	     				    |
*************************************************************************/

def fanCtrlPrefix() { return "fanCtrl" }

def isFanCtrlConfigured() {
	return ( settings?.schMotOperateFan && (isFanCtrlSwConfigured() || isFanCircConfigured())) ? true : false
}

def isFanCtrlSwConfigured() {
	return (settings?.schMotOperateFan && settings?.fanCtrlFanSwitches && settings?.fanCtrlFanSwitchTriggerType && settings?.fanCtrlFanSwitchHvacModeFilter) ? true : false
}

def isFanCircConfigured() {
	return (settings?.schMotOperateFan && settings?.schMotCirculateTstatFan && settings?.schMotFanRuleType) ? true : false
}

def getTempScaleStr() {
	return "°${getTemperatureScale()}"
}

def getFanSwitchDesc(showOpt = true) {
	def swDesc = ""
	def swCnt = 0
	def pName = fanCtrlPrefix()
	if(showOpt) {
		swDesc += (settings?."${pName}FanSwitches" && (settings?."${pName}FanSwitchSpeedCtrl" || settings?."${pName}FanSwitchTriggerType" || settings?."${pName}FanSwitchHvacModeFilter")) ? "Fan Switch Config:" : ""
	}
	swDesc += settings?."${pName}FanSwitches" ? "${showOpt ? "\n" : ""}• Fan Switches:" : ""
	def rmSwCnt = settings?."${pName}FanSwitches"?.size() ?: 0
	settings?."${pName}FanSwitches"?.sort { it?.displayName }?.each { sw ->
		swCnt = swCnt+1
		swDesc += "${swCnt >= 1 ? "${swCnt == rmSwCnt ? "\n   └" : "\n   ├"}" : "\n   └"} ${sw?.label}: (${strCapitalize(sw?.currentSwitch)})"
		swDesc += checkFanSpeedSupport(sw) ? "\n	 └ Current Spd: (${sw?.currentValue("currentState").toString()})" : ""
	}
	if(showOpt) {
		if (settings?."${pName}FanSwitches") {
			swDesc += (settings?."${pName}FanSwitchSpeedCtrl" || settings?."${pName}FanSwitchTriggerType" || settings?."${pName}FanSwitchHvacModeFilter") ? "\n\nFan Triggers:" : ""
			swDesc += (settings?."${pName}FanSwitchSpeedCtrl") ? "\n • Fan Speed Support: (Active)" : ""
			swDesc += (settings?."${pName}FanSwitchTriggerType") ? "\n • Fan Trigger:\n   └(${getEnumValue(switchRunEnum(), settings?."${pName}FanSwitchTriggerType")})" : ""
			swDesc += (settings?."${pName}FanSwitchHvacModeFilter") ? "\n • Hvac Mode Filter:\n   └(${getEnumValue(fanModeTrigEnum(), settings?."${pName}FanSwitchHvacModeFilter")})" : ""
		}
	}

	swDesc += (settings?.schMotCirculateTstatFan) ? "\n • Fan Circulation Enabled" : ""
	swDesc += (settings?.schMotCirculateTstatFan) ? "\n • Fan Circulation Rule:\n   └(${getEnumValue(remSenRuleEnum("fan"), settings?.schMotFanRuleType)})" : ""
	swDesc += (settings?.schMotCirculateTstatFan && settings?.fanCtrlTempDiffDegrees) ? ("\n • Threshold: (${settings?.fanCtrlTempDiffDegrees}${getTempScaleStr()})") : ""
	swDesc += (settings?.schMotCirculateTstatFan && settings?.fanCtrlOnTime) ? ("\n • Circulate Time: (${getEnumValue(fanTimeSecEnum(), settings?.fanCtrlOnTime)})") : ""
	swDesc += (settings?.schMotCirculateTstatFan && settings?.fanCtrlTimeBetweenRuns) ? ("\n • Time Between Cycles:\n   └ (${getEnumValue(longTimeSecEnum(), settings?.fanCtrlTimeBetweenRuns)})") : ""

	swDesc += (settings?."${pName}FanSwitches" || settings?.schMotCirculateTstatFan) ? "\n\n • Restrictions Active: (${autoScheduleOk(fanCtrlPrefix()) ? "No" : "Yes"})" : ""

	return (swDesc == "") ? null : "${swDesc}"
}

def getFanSwitchesSpdChk() {
	def devCnt = 0
	def pName = fanCtrlPrefix()
	if(settings?."${pName}FanSwitches") {
		settings?."${pName}FanSwitches"?.each { sw ->
			if(checkFanSpeedSupport(sw)) { devCnt = devCnt+1 }
		}
	}
	return (devCnt >= 1) ? true : false
}

def fanCtrlScheduleOk() { return autoScheduleOk(fanCtrlPrefix()) }

def fanCtrlCheck() {
	LogAction("FanControl Event | Fan Switch Check", "trace", false)
	try {
		def fanCtrlTstat = schMotTstat

		if(atomicState?.disableAutomation) { return }
		if( !isFanCtrlConfigured()) { return }

		def execTime = now()
		//atomicState?.lastEvalDt = getDtNow()

		def reqHeatSetPoint = getRemSenHeatSetTemp()
		reqHeatSetPoint = reqHeatSetPoint ?: 0

		def reqCoolSetPoint = getRemSenCoolSetTemp()
		reqCoolSetPoint = reqCoolSetPoint ?: 0

		def curTstatTemp = getRemoteSenTemp().toDouble()

		def t0 = getReqSetpointTemp(curTstatTemp, reqHeatSetPoint, reqCoolSetPoint).req
		def curSetPoint = t0 ? t0.toDouble() : 0

		def tempDiff = Math.abs(curSetPoint - curTstatTemp)
		LogAction("fanCtrlCheck: Desired Temps - Heat: ${reqHeatSetPoint} | Cool: ${reqCoolSetPoint}", "info", false)
		LogAction("fanCtrlCheck: Current Thermostat Sensor Temp: ${curTstatTemp} Temp Difference: (${tempDiff})", "info", false)

		if(isFanCircConfigured()) {
			def adjust = (getTemperatureScale() == "C") ? 0.5 : 1.0
			def threshold = !fanCtrlTempDiffDegrees ? adjust : fanCtrlTempDiffDegrees.toDouble()
			def hvacMode = schMotTstat ? schMotTstat?.currentnestThermostatMode.toString() : null
/*
			def curTstatFanMode = schMotTstat?.currentThermostatFanMode.toString()
			def fanOn = (curTstatFanMode == "on" || curTstatFanMode == "circulate") ? true : false
			if(atomicState?.haveRunFan) {
				if(schMotFanRuleType in ["Circ", "Cool_Circ", "Heat_Circ", "Heat_Cool_Circ"]) {
					if(fanOn) {
						LogAction("fantCtrlCheck: Turning OFF '${schMotTstat?.displayName}' Fan; Modes do not match evaluation", "info", true)
						storeLastAction("Turned ${schMotTstat} Fan to (Auto)", getDtNow(), "fanCtrl", schMotTstat)
						schMotTstat?.fanAuto()
						if(schMotTstatMir) { schMotTstatMir*.fanAuto() }
					}
				}
				atomicState.haveRunFan = false
			}
*/
			def sTemp = getReqSetpointTemp(curTstatTemp, reqHeatSetPoint, reqCoolSetPoint)
			def resultMode = sTemp?.type?.toString()
			def can_Circ = false
			if(
				!(hvacMode in ["off"]) && (
					( hvacMode in ["cool"] && schMotFanRuleType in ["Cool_Circ"]) ||
					( resultMode in ["cool"] && schMotFanRuleType in ["Cool_Circ", "Heat_Cool_Circ"]) ||
					( hvacMode in ["heat"] && schMotFanRuleType in ["Heat_Circ"]) ||
					( resultMode in ["heat"] && schMotFanRuleType in ["Heat_Circ", "Heat_Cool_Circ"]) ||
					( hvacMode in ["auto"] && schMotFanRuleType in ["Heat_Cool_Circ"]) ||
					( hvacMode in ["eco"] && schMotFanRuleType in ["Circ"])
				)
			) {

				can_Circ = true
			}
			circulateFanControl(resultMode, curTstatTemp, sTemp?.req?.toDouble(), threshold, can_Circ)
		}

		if(isFanCtrlSwConfigured()) {
			doFanOperation(tempDiff, curTstatTemp, reqHeatSetPoint, reqCoolSetPoint)
		}

		storeExecutionHistory((now()-execTime), "fanCtrlCheck")

	} catch (ex) {
		log.error "fanCtrlCheck Exception:", ex
		parent?.sendExceptionData(ex, "fanCtrlCheck", true, getAutoType())
	}
}

def getReqSetpointTemp(curTemp, reqHeatSetPoint, reqCoolSetPoint) {
	LogAction("getReqSetpointTemp: Current Temp: ${curTemp} Req Heat: ${reqHeatSetPoint} Req Cool: ${reqCoolSetPoint}", "info", false)
	def tstat = schMotTstat

	def hvacMode = tstat ? tstat?.currentThermostatMode.toString() : null
	def operState = tstat ? tstat?.currentThermostatOperatingState.toString() : null
	def opType = hvacMode.toString()

	if(hvacMode == "off") {
		return ["req":null, "type":"off"]
	}
	if((hvacMode == "cool") || (operState == "cooling")) {
		opType = "cool"
	} else if((hvacMode == "heat") || (operState == "heating")) {
		opType = "heat"
	} else if(hvacMode == "auto") {
		def coolDiff = Math.abs(curTemp - reqCoolSetPoint)
		def heatDiff = Math.abs(curTemp - reqHeatSetPoint)
		opType = coolDiff < heatDiff ? "cool" : "heat"
	}
	def temp = (opType == "cool") ? reqCoolSetPoint?.toDouble() : reqHeatSetPoint?.toDouble()
	return ["req":temp, "type":opType]
}

def doFanOperation(tempDiff, curTstatTemp, curHeatSetpoint, curCoolSetpoint) {
	def pName = fanCtrlPrefix()
	LogAction("doFanOperation: Temp Difference: (${tempDiff})", "info", false)
	try {
		def tstat = schMotTstat

/*		def curTstatTemp = tstat ? getRemoteSenTemp().toDouble() : null
		def curCoolSetpoint = getRemSenCoolSetTemp()
		def curHeatSetpoint = getRemSenHeatSetTemp()
*/
		def hvacMode = tstat ? tstat?.currentnestThermostatMode.toString() : null
		def curTstatOperState = tstat?.currentThermostatOperatingState.toString()
		def curTstatFanMode = tstat?.currentThermostatFanMode.toString()
		LogAction("doFanOperation: Thermostat Info - ( Temperature: (${curTstatTemp}) | HeatSetpoint: (${curHeatSetpoint}) | CoolSetpoint: (${curCoolSetpoint}) | HvacMode: (${hvacMode}) | OperatingState: (${curTstatOperState}) | FanMode: (${curTstatFanMode}) )", "info", false)

		if(atomicState?.haveRunFan == null) { atomicState.haveRunFan = false }
		def savedHaveRun = atomicState.haveRunFan

		def hvacFanOn = false
//	1:"Heating/Cooling", 2:"With Fan Only", 3:"Heating", 4:"Cooling"

		def validOperModes = []
		switch ( settings?."${pName}FanSwitchTriggerType".toInteger() ) {
			case 1:
				validOperModes = ["heating", "cooling"]
				hvacFanOn = (curTstatOperState in validOperModes) ? true : false
				break
			case 2:
				hvacFanOn = (curTstatFanMode in ["on", "circulate"]) ? true : false
				break
			case 3:
				validOperModes = ["heating"]
				hvacFanOn = (curTstatOperState in validOperModes) ? true : false
				break
			case 4:
				validOperModes = ["cooling"]
				hvacFanOn = (curTstatOperState in validOperModes) ? true : false
				break
			default:
				break
		}
/*
		if( settings?."${pName}FanSwitchTriggerType".toInteger() == 1) {
			def validOperModes = ["heating", "cooling"]
			hvacFanOn = (curTstatOperState in ["heating", "cooling"]) ? true : false
		}
		if( settings?."${pName}FanSwitchTriggerType".toInteger() == 2) {
			hvacFanOn = (curTstatFanMode in ["on", "circulate"]) ? true : false
		}
		//if(settings?."${pName}FanSwitchHvacModeFilter" != "any" && (settings?."${pName}FanSwitchHvacModeFilter" != hvacMode)) {
*/
		if( !( ("any" in settings?."${pName}FanSwitchHvacModeFilter") || (hvacMode in settings?."${pName}FanSwitchHvacModeFilter") ) ){
			if(savedHaveRun) {
				LogAction("doFanOperation: Evaluating turn fans off; Thermostat Mode does not Match the required Mode", "info", true)
			}
			hvacFanOn = false  // force off of fans
		}

		def schedOk = fanCtrlScheduleOk()
		if(!schedOk) {
			if(savedHaveRun) {
				LogAction("doFanOperation: Evaluating turn fans off; Schedule is restricted", "info", true)
			}
			hvacFanOn = false  // force off of fans
		}

		settings?."${pName}FanSwitches"?.each { sw ->
			def swOn = (sw?.currentSwitch.toString() == "on") ? true : false
			if(hvacFanOn) {
				if(!swOn && !savedHaveRun) {
					LogAction("doFanOperation: Fan Switch (${sw?.displayName}) is (${swOn ? "ON" : "OFF"}) | Turning '${sw}' Switch (ON)", "info", true)
					sw.on()
					swOn = true
					atomicState.haveRunFan = true
					storeLastAction("Turned On $sw)", getDtNow(), pName)
				} else {
					if(!swOn && savedHaveRun) {
						LogAction("doFanOperation: savedHaveRun state shows switch ${sw} turned OFF outside of automation requests", "info", true)
					}
				}
				if(swOn && atomicState?.haveRunFan && checkFanSpeedSupport(sw)) {
					def speed = sw?.currentValue("currentState") ?: null
					if(settings?."${pName}FanSwitchSpeedCtrl" && settings?."${pName}FanSwitchHighSpeed" && settings?."${pName}FanSwitchMedSpeed" && settings?."${pName}FanSwitchLowSpeed") {
						if(tempDiff < settings?."${pName}FanSwitchMedSpeed".toDouble()) {
							if(speed != "LOW") {
								sw.lowSpeed()
								LogAction("doFanOperation: Temp Difference (${tempDiff}°${getTemperatureScale()}) is BELOW the Medium Speed Threshold of (${settings?."${pName}FanSwitchMedSpeed"}) | Turning '${sw}' Fan Switch on (LOW SPEED)", "info", true)
								storeLastAction("Set Fan $sw to Low Speed", getDtNow(), pName)
							}
						}
						else if(tempDiff >= settings?."${pName}FanSwitchMedSpeed".toDouble() && tempDiff < settings?."${pName}FanSwitchHighSpeed".toDouble()) {
							if(speed != "MED") {
								sw.medSpeed()
								LogAction("doFanOperation: Temp Difference (${tempDiff}°${getTemperatureScale()}) is ABOVE the Medium Speed Threshold of (${settings?."${pName}FanSwitchMedSpeed"}) | Turning '${sw}' Fan Switch on (MEDIUM SPEED)", "info", true)
								storeLastAction("Set Fan $sw to Medium Speed", getDtNow(), pName)
							}
						}
						else if(tempDiff >= settings?."${pName}FanSwitchHighSpeed".toDouble()) {
							if(speed != "HIGH") {
								sw.highSpeed()
								LogAction("doFanOperation: Temp Difference (${tempDiff}°${getTemperatureScale()}) is ABOVE the High Speed Threshold of (${settings?."${pName}FanSwitchHighSpeed"}) | Turning '${sw}' Fan Switch on (HIGH SPEED)", "info", true)
								storeLastAction("Set Fan $sw to High Speed", getDtNow(), pName)
							}
						}
					} else {
						if(speed != "HIGH") {
							sw.highSpeed()
							LogAction("doFanOperation: Fan supports multiple speeds, with speed control disabled | Turning '${sw}' Fan Switch on (HIGH SPEED)", "info", true)
							storeLastAction("Set Fan $sw to High Speed", getDtNow(), pName)
						}
					}
				}
			} else {
				if(swOn && savedHaveRun) {
					LogAction("doFanOperation: Fan Switch (${sw?.displayName}) is (${swOn ? "ON" : "OFF"}) | Turning '${sw}' Switch (OFF)", "info", true)
					storeLastAction("Turned Off (${sw})", getDtNow(), pName)
					sw.off()
					atomicState.haveRunFan = false
				} else {
					if(swOn && !savedHaveRun) {
						LogAction("doFanOperation: Saved have run state shows switch ${sw} turned ON outside of automation requests", "info", true)
					}
				}
			}
		}
	} catch (ex) {
		log.error "doFanOperation Exception:", ex
		parent?.sendExceptionData(ex, "doFanOperation", true, getAutoType())
	}
}

def getLastFanCtrlFanRunDtSec() { return !atomicState?.lastfanCtrlRunDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastfanCtrlRunDt, null, "getLastFanCtrlFanRunDtSec").toInteger() }
def getLastFanCtrlFanOffDtSec() { return !atomicState?.lastfanCtrlFanOffDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastfanCtrlFanOffDt, null, "getLastFanCtrlFanOffDtSec").toInteger() }


// CONTROLS THE THERMOSTAT FAN
def circulateFanControl(operType, Double curSenTemp, Double reqSetpointTemp, Double threshold, can_Circ) {
	def tstat = schMotTstat
	def tstatsMir = schMotTstatMir

	def theFanIsOn = false
	def hvacMode = tstat ? tstat?.currentnestThermostatMode.toString() : null
	def curTstatFanMode = tstat?.currentThermostatFanMode.toString()
	def fanOn = (curTstatFanMode == "on" || curTstatFanMode == "circulate") ? true : false

	def returnToAuto = can_Circ ? false : true
	if(hvacMode in ["off", "eco"]) { returnToAuto = true }

	// Track approximate fan on / off times
	if( !fanOn && atomicState?.lastfanCtrlRunDt > atomicState?.lastfanCtrlFanOffDt ) {
		atomicState?.lastfanCtrlFanOffDt = getDtNow()
		returnToAuto = true
	}

	if( fanOn && atomicState?.lastfanCtrlRunDt < atomicState?.lastfanCtrlFanOffDt ) {
		atomicState?.lastfanCtrlFanRunDt = getDtNow()
	}

	def schedOk = fanCtrlScheduleOk()
	if(!schedOk) {
		returnToAuto = true
	}

	def curOperState = tstat?.currentnestThermostatOperatingState.toString()

	def tstatOperStateOk = (curOperState == "idle") ? true : false
	// if ac or heat is on, we should put fan back to auto
	if(!tstatOperStateOk) {
		if( atomicState?.lastfanCtrlFanOffDt > atomicState?.lastfanCtrlRunDt) { return }
		LogAction("Circulate Fan Run: The Thermostat OperatingState is Currently (${strCapitalize(curOperState)}) Skipping", "info", true)
		atomicState?.lastfanCtrlFanOffDt = getDtNow()
		returnToAuto = true
	}
	def fanTempOk = getCirculateFanTempOk(curSenTemp, reqSetpointTemp, threshold, fanOn, operType)

	if(hvacMode in ["heat", "auto", "cool", "eco"] && fanTempOk && !returnToAuto) {
		if(!fanOn) {
			def waitTimeVal = fanCtrlTimeBetweenRuns?.toInteger() ?: 1200
			def timeSinceLastOffOk = (getLastFanCtrlFanOffDtSec() > waitTimeVal) ? true : false
			if(!timeSinceLastOffOk) {
				def remaining = waitTimeVal - getLastFanCtrlFanOffDtSec()
				LogAction("Circulate Fan: Want to RUN Fan | Delaying for wait period ${waitTimeVal}, remaining ${remaining} seconds", "info", true)
				def val = Math.min( Math.max(remaining,defaultAutomationTime()), 60)
				scheduleAutomationEval(val)
				return
			}
			LogAction("Circulate Fan: Activating '${tstat?.displayName}'' Fan for ${strCapitalize(operType)}ING Circulation", "debug", true)
			tstat?.fanOn()
			storeLastAction("Turned ${tstat} Fan 'On'", getDtNow(), "fanCtrl", tstat)
			if(tstatsMir) {
				tstatsMir?.each { mt ->
					LogAction("Circulate Fan: Mirroring Primary Thermostat: Activating '${mt?.displayName}' Fan", "debug", true)
					mt?.fanOn()
					storeLastAction("Turned ${mt.displayName} Fan 'On'", getDtNow(), "fanCtrl", mt)
				}
			}
			atomicState?.lastfanCtrlRunDt = getDtNow()
		}
		theFanIsOn = true

	} else {
		if(returnToAuto || !fanTempOk) {
			if(fanOn && !returnToAuto) {
				def fanOnTimeVal = fanCtrlOnTime?.toInteger() ?: 240
				def timeSinceLastRunOk = (getLastFanCtrlFanRunDtSec() > fanOnTimeVal) ? true : false // fan left on for minimum
				if(!timeSinceLastRunOk) {
					def remaining = fanOnTimeVal - getLastFanCtrlFanRunDtSec()
					LogAction("Circulate Fan Run: Want to STOP Fan | Delaying for run period ${fanOnTimeVal}, remaining ${remaining} seconds", "info", true)
					def val = Math.min( Math.max(remaining,defaultAutomationTime()), 60)
					scheduleAutomationEval(val)
					return
				}
			}
			if(fanOn) {
				LogAction("Circulate Fan: Turning OFF '${tstat?.displayName}' Fan that was used for ${strCapitalize(operType)}ING Circulation", "info", true)
				tstat?.fanAuto()
				storeLastAction("Turned ${tstat} Fan to 'Auto'", getDtNow(), "fanCtrl", tstat)
				if(tstatsMir) {
					tstatsMir?.each { mt ->
						LogAction("Circulate Fan: Mirroring Primary Thermostat: Turning OFF '${mt?.displayName}' Fan", "info", true)
						mt?.fanAuto()
						storeLastAction("Turned ${mt.displayName} Fan 'Off'", getDtNow(), "fanCtrl", mt)
					}
				}
				atomicState?.lastfanCtrlFanOffDt = getDtNow()
			}
		}
		theFanIsOn = false
	}
	if(theFanIsOn) {
		scheduleAutomationEval(120)
	}
}

def getCirculateFanTempOk(Double senTemp, Double reqsetTemp, Double threshold, Boolean fanOn, operType) {

	def turnOn = false
/*
	def adjust = (getTemperatureScale() == "C") ? 0.5 : 1.0
	if(threshold > (adjust * 2.0)) {
		adjust = adjust * 2.0
	}

	if(adjust >= threshold) {
		LogAction("getCirculateFanTempOk: Bad threshold setting ${threshold} <= ${adjust}", "warn", true)
		return false
	}

	LogAction(" ├ adjust: ${adjust}}°${getTemperatureScale()}", "debug", false)
*/

	LogAction(" ├ operType: (${strCapitalize(operType)}) | Temp Threshold: ${threshold}°${getTemperatureScale()} |  FanAlreadyOn: (${strCapitalize(fanOn)})", "debug", false)
	LogAction(" ├ Sensor Temp: ${senTemp}°${getTemperatureScale()} | Requested Setpoint Temp: ${reqsetTemp}°${getTemperatureScale()}", "debug", false)

	if(!reqsetTemp) {
		LogAction("getCirculateFanTempOk: Bad reqsetTemp ${reqsetTemp}", "warn", false)
		LogAction("getCirculateFanTempOk:", "debug", false)
		return false
	}

//	def ontemp
	def offtemp

	if(operType == "cool") {
//		ontemp = reqsetTemp + threshold
		offtemp = reqsetTemp
		if(senTemp >= (offtemp + threshold)) { turnOn = true }
//		if((senTemp > offtemp) && (senTemp <= (ontemp - adjust))) { turnOn = true }
	}
	if(operType == "heat") {
//		ontemp = reqsetTemp - threshold
		offtemp = reqsetTemp
		if(senTemp <= (offtemp - threshold)) { turnOn = true }
//		if((senTemp < offtemp) && (senTemp >= (ontemp + adjust))) { turnOn = true }
	}

//	LogAction(" ├ onTemp: ${ontemp} | offTemp: ${offtemp}}°${getTemperatureScale()}", "debug", false)
	LogAction(" ├ offTemp: ${offtemp}°${getTemperatureScale()} | Temp Threshold: ${threshold}°${getTemperatureScale()}", "debug", false)
	LogAction(" ┌ Final Result: (${strCapitalize(turnOn)})", "debug", false)
//	LogAction("getCirculateFanTempOk: ", "debug", false)

	def resultStr = "getCirculateFanTempOk: The Temperature Difference is "
	if(turnOn) {
		resultStr += " within "
	} else {
		resultStr += " Outside "
	}
	def disp = false
	resultStr += "of Threshold Limits | "
	if(!turnOn && fanOn) {
		resultStr += "Turning Thermostat Fan OFF"
		disp = true
	} else if(turnOn && !fanOn) {
		resultStr += "Turning Thermostat Fan ON"
		disp = true
	} else if(turnOn && fanOn) {
		resultStr += "Fan is ON"
	} else if(!turnOn && !fanOn) {
		resultStr += "Fan is OFF"
	}
	LogAction("${resultStr}", "info", disp)

	return turnOn
}


/********************************************************************************
|					HUMIDITY CONTROL AUTOMATION CODE	     				|
*********************************************************************************/
def humCtrlPrefix() { return "humCtrl" }

def isHumCtrlConfigured() {
	return ((settings?.humCtrlUseWeather || settings?.humCtrlTempSensor) && settings?.humCtrlHumidity && settings?.humCtrlSwitches) ? true : false
}

def getDeviceVarAvg(items, var) {
	def tmpAvg = []
	def tempVal = 0
	if(!items) { return tempVal }
	else {
		tmpAvg = items*."${var}"
		if(tmpAvg && tmpAvg?.size() > 0) { tempVal = (tmpAvg?.sum().toDouble() / tmpAvg?.size().toDouble()).round(1) } //
	}
	return tempVal.toDouble()
}

def humCtrlSwitchDesc(showOpt = true) {
	if(settings?.humCtrlSwitches) {
		def cCnt = settings?.humCtrlSwitches?.size() ?: 0
		def str = ""
		def cnt = 0
		str += "Switch Status:"
		settings?.humCtrlSwitches?.sort { it?.displayName }?.each { dev ->
			cnt = cnt+1
			def val = strCapitalize(dev?.currentSwitch) ?: "Not Set"
			str += "${(cnt >= 1) ? "${(cnt == cCnt) ? "\n└" : "\n├"}" : "\n└"} ${dev?.label}: (${val})"
		}

		if(showOpt) {
			str += (settings?.humCtrlSwitchTriggerType || settings?.humCtrlSwitchHvacModeFilter) ? "\n\nSwitch Triggers:" : ""
			str += (settings?.humCtrlSwitchTriggerType) ? "\n  • Switch Trigger: (${getEnumValue(switchRunEnum(true), settings?.humCtrlSwitchTriggerType)})" : ""
			str += (settings?.humCtrlSwitchHvacModeFilter) ? "\n  • Hvac Mode Filter: (${getEnumValue(fanModeTrigEnum(), settings?.humCtrlSwitchHvacModeFilter).toString().replaceAll("\\[|\\]", "")})" : ""
		}

		return str
	}
	return null
}

def humCtrlHumidityDesc() {
	if(settings?.humCtrlHumidity) {
		def cCnt = settings?.humCtrlHumidity?.size() ?: 0
		def str = ""
		def cnt = 0
		str += "Sensor Humidity (average): (${getDeviceVarAvg(settings.humCtrlHumidity, "currentHumidity")}%)"
		settings?.humCtrlHumidity?.sort { it?.displayName }?.each { dev ->
			cnt = cnt+1
			def val = strCapitalize(dev?.currentHumidity) ?: "Not Set"
			str += "${(cnt >= 1) ? "${(cnt == cCnt) ? "\n└" : "\n├"}" : "\n└"} ${dev?.label}: ${(dev?.label?.toString()?.length() > 10) ? "\n${(cCnt == 1 || cnt == cCnt) ? "    " : "│"}└ " : ""}(${val}%)"
		}
		return str
	}
	return null
}

def getHumCtrlTemperature() {
	def extTemp = 0.0
	if(!settings?.humCtrlUseWeather && settings?.humCtrlTempSensor) {
		extTemp = getDeviceTemp(settings?.humCtrlTempSensor)
	} else {
		if(settings?.humCtrlUseWeather && (atomicState?.curWeatherTemp_f || atomicState?.curWeatherTemp_c)) {
			if(getTemperatureScale() == "C") { extTemp = atomicState?.curWeatherTemp_c.toDouble() }
			else { extTemp = atomicState?.curWeatherTemp_f.toDouble() }
		}
	}
	return extTemp
}

def getMaxHumidity(curExtTemp) {
	def maxhum = 15
	if(curExtTemp != null) {
		if(curExtTemp >= adj_temp(40)) {
			maxhum = 45
		} else if(curExtTemp >= adj_temp(32)) {
			maxhum = 40
		} else if(curExtTemp >= adj_temp(20)) {
			maxhum = 35
		} else if(curExtTemp >= adj_temp(10)) {
			maxhum = 30
		} else if(curExtTemp >= adj_temp(0)) {
			maxhum = 25
		} else if(curExtTemp >= adj_temp(-10)) {
			maxhum = 20
		} else if(curExtTemp >= adj_temp(-20)) {
			maxhum = 15
		}
	}
	return maxhum
}

def humCtrlScheduleOk() { return autoScheduleOk(humCtrlPrefix()) }

def humCtrlCheck() {
	LogAction("humCtrlCheck", "trace", false)
	def pName = humCtrlPrefix()
	if(atomicState?.disableAutomation) { return }
	try {
		def execTime = now()

		def tstat = schMotTstat
		def hvacMode = tstat ? tstat?.currentnestThermostatMode.toString() : null
		def curTstatOperState = tstat?.currentThermostatOperatingState.toString()
		def curTstatFanMode = tstat?.currentThermostatFanMode.toString()
		//def curHum = humCtrlHumidity?.currentHumidity
		def curHum = getDeviceVarAvg(settings.humCtrlHumidity, "currentHumidity")
		def curExtTemp = getHumCtrlTemperature()
		def maxHum = getMaxHumidity(curExtTemp)
		def schedOk = humCtrlScheduleOk()

		LogAction("humCtrlCheck: ( Humidity: (${curHum}) | External Temp: (${curExtTemp}) | Max Humidity: (${maxHum}) | HvacMode: (${hvacMode}) | OperatingState: (${curTstatOperState}) )", "info", false)

		if(atomicState?.haveRunHumidifier == null) { atomicState.haveRunHumidifier = false }
		def savedHaveRun = atomicState?.haveRunHumidifier

		def humOn = false

		if(curHum < maxHum) {
			humOn = true
		}

//	1:"Heating/Cooling", 2:"With Fan Only", 3:"Heating", 4:"Cooling" 5:"All Operating Modes"

		def validOperModes = []
		def validOperating = true
		switch ( settings?.humCtrlSwitchTriggerType?.toInteger() ) {
			case 1:
				validOperModes = ["heating", "cooling"]
				validOperating = (curTstatOperState in validOperModes) ? true : false
				break
			case 2:
				validOperating = (curTstatFanMode in ["on", "circulate"]) ? true : false
				break
			case 3:
				validOperModes = ["heating"]
				validOperating = (curTstatOperState in validOperModes) ? true : false
				break
			case 4:
				validOperModes = ["cooling"]
				validOperating = (curTstatOperState in validOperModes) ? true : false
				break
			case 5:
				break
			default:
				break
		}

		def validHvac = true
		if( !( ("any" in settings?.humCtrlSwitchHvacModeFilter) || (hvacMode in settings?.humCtrlSwitchHvacModeFilter) ) ){
			LogAction("humCtrlCheck: Evaluating turn humidifier off; Thermostat Mode does not Match the required Mode", "info", false)
			validHvac = false  // force off
		}

		def turnOn = (humOn && validOperating && validHvac && schedOk) ?: false
		LogAction("humCtrlCheck: turnOn: ${turnOn} | humOn: ${humOn} | validOperating: ${validOperating} | validHvac: ${validHvac} | schedOk: ${schedOk} | savedHaveRun: ${savedHaveRun}", "info", false)

		settings?.humCtrlSwitches?.each { sw ->
			def swOn = (sw?.currentSwitch.toString() == "on") ? true : false
			if(turnOn) {
				//if(!swOn && !savedHaveRun) {
				if(!swOn) {
					LogAction("humCtrlCheck: Fan Switch (${sw?.displayName}) is (${swOn ? "ON" : "OFF"}) | Turning '${sw}' Switch (ON)", "info", true)
					sw.on()
					swOn = true
					atomicState.haveRunHumidifier = true
					storeLastAction("Turned On $sw)", getDtNow(), pName)
				} else {
					if(!swOn && savedHaveRun) {
						LogAction("humCtrlCheck: savedHaveRun state shows switch ${sw} turned OFF outside of automation requests", "info", true)
					}
				}
			} else {
				//if(swOn && savedHaveRun) {
				if(swOn) {
					LogAction("humCtrlCheck: Fan Switch (${sw?.displayName}) is (${swOn ? "ON" : "OFF"}) | Turning '${sw}' Switch (OFF)", "info", true)
					storeLastAction("Turned Off (${sw})", getDtNow(), pName)
					sw.off()
					atomicState.haveRunHumidifier = false
				} else {
					if(swOn && !savedHaveRun) {
						LogAction("humCtrlCheck: Saved have run state shows switch ${sw} turned ON outside of automation requests", "info", true)
					}
					atomicState.haveRunHumidifier = false
				}
			}
		}
		storeExecutionHistory((now()-execTime), "humCtrlCheck")

	} catch (ex) {
		log.error "humCtrlCheck Exception:", ex
		parent?.sendExceptionData(ex, "humCtrlCheck", true, getAutoType())
	}
}


/********************************************************************************
|					EXTERNAL TEMP AUTOMATION CODE	     				|
*********************************************************************************/
def extTmpPrefix() { return "extTmp" }

def isExtTmpConfigured() {
	return ((settings?.extTmpUseWeather || settings?.extTmpTempSensor) && settings?.extTmpDiffVal) ? true : false
}

def getExtConditions( doEvent = false ) {
	LogTrace("getExtConditions")
	if(atomicState?.weatherDeviceInst == null) {
		atomicState?.weatherDeviceInst = parent?.getWeatherDeviceInst()
		if(atomicState?.weatherDeviceInst == null) {
			atomicState?.weatherDeviceInst = false
		}
		//log.debug "set weatherDeviceInst to ${atomicState?.weatherDeviceInst}"
	}
	if(atomicState?.NeedwUpd && atomicState?.weatherDeviceInst) {
		try {
			def cur = parent?.getWData()
			def weather = parent.getWeatherDevice()

			if(cur && weather && cur?.current_observation) {
				atomicState?.curWeather = cur?.current_observation
				atomicState?.curWeatherTemp_f = Math.round(cur?.current_observation?.temp_f) as Integer
				atomicState?.curWeatherTemp_c = Math.round(cur?.current_observation?.temp_c.toDouble())
				atomicState?.curWeatherLoc = cur?.current_observation?.display_location?.full.toString()  // This is not available as attribute in dth
				//atomicState?.curWeatherHum = cur?.current_observation?.relative_humidity?.toString().replaceAll("\\%", "")

				def dp = 0.0
				if(weather) {  // Dewpoint is calculated in dth
					dp = weather?.currentValue("dewpoint")?.toString().replaceAll("\\[|\\]", "").toDouble()
				}
				def c_temp = 0.0
				def f_temp = 0 as Integer
				if(getTemperatureScale() == "C") {
					c_temp = dp as Double
					f_temp = ((c_temp * (9 / 5)) + 32) as Integer //
				} else {
					f_temp = dp as Integer
					c_temp = ((f_temp - 32) * (5 / 9)) as Double //
				}
				atomicState?.curWeatherDewpointTemp_c = Math.round(c_temp.round(1) * 2) / 2.0f //
				atomicState?.curWeatherDewpointTemp_f = Math.round(f_temp) as Integer

				atomicState.NeedwUpd = false
			}
		} catch (ex) {
			log.error "getExtConditions Exception:", ex
			parent?.sendExceptionData(ex, "getExtConditions", true, getAutoType())
		}
	}
}

def getExtTmpTemperature() {
	def extTemp = 0.0
	if(!settings?.extTmpUseWeather && settings?.extTmpTempSensor) {
		extTemp = getDeviceTemp(settings?.extTmpTempSensor)
	} else {
		if(settings?.extTmpUseWeather && (atomicState?.curWeatherTemp_f || atomicState?.curWeatherTemp_c)) {
			if(getTemperatureScale() == "C") { extTemp = atomicState?.curWeatherTemp_c.toDouble() }
			else { extTemp = atomicState?.curWeatherTemp_f.toDouble() }
		}
	}
	return extTemp
}

def getExtTmpDewPoint() {
	def extDp = 0.0
	if(settings?.extTmpUseWeather && (atomicState?.curWeatherDewpointTemp_f || atomicState?.curWeatherDewpointTemp_c)) {
		if(getTemperatureScale() == "C") { extDp = roundTemp(atomicState?.curWeatherDewpointTemp_c.toDouble()) }
		else { extDp = roundTemp(atomicState?.curWeatherDewpointTemp_f.toDouble()) }
	}
//TODO if an external sensor, if it has temp and humidity, we can calculate DP
	return extDp
}

def getDesiredTemp() {
	def extTmpTstat = settings?.schMotTstat
	def curMode = extTmpTstat.currentnestThermostatMode.toString()
	def modeOff = (curMode in ["off"]) ? true : false
	def modeEco = (curMode in ["eco"]) ? true : false
	def modeCool = (curMode == "cool") ? true : false
	def modeHeat = (curMode == "heat") ? true : false
	def modeAuto = (curMode == "auto") ? true : false

	def desiredHeatTemp = getRemSenHeatSetTemp(curMode)
	def desiredCoolTemp = getRemSenCoolSetTemp(curMode)
	def lastMode = extTmpTstat?.currentpreviousthermostatMode?.toString()
	if(modeEco) {
		if( !lastMode && atomicState?.extTmpTstatOffRequested && atomicState?.extTmplastMode) {
			lastMode = atomicState?.extTmplastMode
			//atomicState?.extTmpLastDesiredTemp
		}
		if(lastMode) {
			desiredHeatTemp = getRemSenHeatSetTemp(lastMode, false)
			desiredCoolTemp = getRemSenCoolSetTemp(lastMode, false)
			LogAction("getDesiredTemp: Using lastMode: ${lastMode} | extTmpTstatOffRequested: ${atomicState?.extTmpTstatOffRequested} | curMode: ${curMode}", "debug", false)
			modeOff = (lastMode in ["off"]) ? true : false
			modeCool = (lastMode == "cool") ? true : false
			modeHeat = (lastMode == "heat") ? true : false
			modeAuto = (lastMode == "auto") ? true : false
		}
	}

	def desiredTemp = 0
	if(!modeOff) {
		if(desiredHeatTemp && modeHeat)		{ desiredTemp = desiredHeatTemp }
		else if(desiredCoolTemp && modeCool)	{ desiredTemp = desiredCoolTemp }
		else if(desiredHeatTemp && desiredCoolTemp && (desiredHeatTemp < desiredCoolTemp) && modeAuto ) {
			desiredTemp = (desiredCoolTemp + desiredHeatTemp) / 2.0 //
		}
		//else if(desiredHeatTemp && modeEco)	{ desiredTemp = desiredHeatTemp }
		//else if(desiredCoolTemp && modeEco)	{ desiredTemp = desiredCoolTemp }
		else if(!desiredTemp && atomicState?.extTmpLastDesiredTemp) { desiredTemp = atomicState?.extTmpLastDesiredTemp }

		LogAction("getDesiredTemp: curMode: ${curMode} | lastMode: ${lastMode} | Desired Temp: ${desiredTemp} | Desired Heat Temp: ${desiredHeatTemp} | Desired Cool Temp: ${desiredCoolTemp} extTmpLastDesiredTemp: ${atomicState?.extTmpLastDesiredTemp}", "info", false)
	}

	return desiredTemp
}

def extTmpTempOk(disp=false, last=false) {
	//LogTrace("extTmpTempOk")
	def pName = extTmpPrefix()
	try {
		def execTime = now()
		def extTmpTstat = settings?.schMotTstat
		def extTmpTstatMir = settings?.schMotTstatMir

		def intTemp = extTmpTstat ? getRemoteSenTemp().toDouble() : null
		def extTemp = getExtTmpTemperature()

		def dpLimit = getComfortDewpoint(extTmpTstat)
		def curDp = getExtTmpDewPoint()
		def diffThresh = Math.abs(getExtTmpTempDiffVal())

		def curMode = extTmpTstat.currentnestThermostatMode.toString()
		def modeOff = (curMode == "off") ? true : false
		def modeCool = (curMode == "cool") ? true : false
		def modeHeat = (curMode == "heat") ? true : false
		def modeEco = (curMode == "eco") ? true : false
		def modeAuto = (curMode == "auto") ? true : false

		def canHeat = atomicState?.schMotTstatCanHeat
		def canCool = atomicState?.schMotTstatCanCool

		LogAction("extTmpTempOk: Inside Temp: ${intTemp} | curMode: ${curMode} | modeOff: ${modeOff} | modeEco: ${modeEco} | modeAuto: ${modeAuto} || extTmpTstatOffRequested: ${atomicState?.extTmpTstatOffRequested}", "debug", false)

		def retval = true
		def tempOk = true

		def dpOk = (curDp < dpLimit || !canCool) ? true : false
		if(!dpOk) { retval = false }

		def str

/*
		def modeEco = (curMode in ["eco"]) ? true : false
		def home = false
		def away = false
		if(extTmpTstat && getTstatPresence(extTmpTstat) == "present") { home = true }
		else { away = true }
		if(away && modeEco) {			// we won't pull system out of ECO mode if we are away
			retval = false
			str = "Nest is away AND in ECO mode"
		}
*/

		if(!getSafetyTempsOk(extTmpTstat)) {
			retval = false
			tempOk = false
			str = "within safety Temperatures "
			LogAction("extTmpTempOk: Safety Temps not OK", "warn", true)
		}

		def desiredHeatTemp
		def desiredCoolTemp
		if(modeAuto && retval) {
			desiredHeatTemp = getRemSenHeatSetTemp(curMode)
			desiredCoolTemp = getRemSenCoolSetTemp(curMode)
		}
		def lastMode = extTmpTstat?.currentpreviousthermostatMode?.toString()
		if(curMode == "eco") {
			if(!lastMode && atomicState?.extTmpTstatOffRequested && atomicState?.extTmplastMode) {
				lastMode = atomicState?.extTmplastMode
				//atomicState?.extTmpLastDesiredTemp
			}
			if(lastMode) {
				LogAction("extTmpTempOk: Resetting mode curMode: ${curMode} | to previous mode lastMode: ${lastMode} | extTmpTstatOffRequested: ${atomicState?.extTmpTstatOffRequested}", "debug", false)
				desiredHeatTemp = getRemSenHeatSetTemp(lastMode, false)
				desiredCoolTemp = getRemSenCoolSetTemp(lastMode, false)
				if(!desiredHeatTemp) { desiredHeatTemp = atomicState?.extTmpLastDesiredHTemp }
				if(!desiredCoolTemp) { desiredCoolTemp = atomicState?.extTmpLastDesiredCTemp }
				//modeOff = (lastMode == "off") ? true : false
				modeCool = (lastMode == "cool") ? true : false
				modeHeat = (lastMode == "heat") ? true : false
				modeEco = (lastMode == "eco") ? true : false
				modeAuto = (lastMode == "auto") ? true : false
			}
		}

		if(modeAuto && retval && desiredHeatTemp && desiredCoolTemp) {
			if( !(extTemp >= (desiredHeatTemp+diffThresh) && extTemp <= (desiredCoolTemp-diffThresh)) ) {
				retval = false
				tempOk = false
				str = "within range (${desiredHeatTemp} ${desiredCoolTemp})"
			}
			atomicState?.extTmpLastDesiredHTemp = desiredHeatTemp
			atomicState?.extTmpLastDesiredCTemp = desiredCoolTemp
		}

		def tempDiff
		def desiredTemp
		def insideThresh

		if(!modeAuto && retval) {
			desiredTemp = getDesiredTemp()
			if(!desiredTemp) {
				desiredTemp = intTemp
				if(!modeOff) {
					LogAction("extTmpTempOk: No Desired Temp found, using interior Temp", "warn", true)
				}
				retval = false
			} else {
				tempDiff = Math.abs(extTemp - desiredTemp)
				str = "enough different (${tempDiff})"
				insideThresh = getExtTmpInsideTempDiffVal()
				LogAction("extTmpTempOk: Outside Temp: ${extTemp} | Desired Temp: ${desiredTemp} | Inside Temp Threshold: ${insideThresh} | Outside Temp Threshold: ${diffThresh} | Actual Difference: ${tempDiff} | Outside Dew point: ${curDp} | Dew point Limit: ${dpLimit}", "debug", false)

				if(diffThresh && tempDiff < diffThresh) {
					retval = false
					tempOk = false
				}
				def extTempHigh = (extTemp >= desiredTemp) ? true : false
				def extTempLow = (extTemp <= desiredTemp) ? true : false
				def oldMode = atomicState?.extTmpRestoreMode
				if(modeCool || oldMode == "cool" || (!canHeat && canCool)) {
					str = "greater than"
					if(extTempHigh) { retval = false; tempOk = false }
					else if (intTemp > desiredTemp+insideThresh) { retval = false; tempOk = false } // too hot inside
				}
				if(modeHeat || oldMode == "heat" || (!canCool && canHeat)) {
					str = "less than"
					if(extTempLow) { retval = false; tempOk = false }
					else if (intTemp < desiredTemp-insideThresh) { retval = false; tempOk = false } // too cold inside
				}
				LogAction("extTmpTempOk: extTempHigh: ${extTempHigh} | extTempLow: ${extTempLow}", "debug", false)
			}
		}
		def showRes = disp ? (retval != last ? true : false) : false
		if(!dpOk) {
			LogAction("extTmpTempOk: ${retval} Dewpoint: (${curDp}°${getTemperatureScale()}) is ${dpOk ? "ok" : "TOO HIGH"}", "info", showRes)
		} else {
			if(!modeAuto) {
				LogAction("extTmpTempOk: ${retval} Desired Inside Temp: (${desiredTemp}°${getTemperatureScale()}) is ${tempOk ? "" : "Not"} ${str} $diffThresh° of Outside Temp: (${extTemp}°${getTemperatureScale()}) or Inside Temp: (${intTemp}) is ${tempOk ? "" : "Not"} within Inside Threshold: ${insideThresh} of desired (${desiredTemp})", "info", showRes)
			} else {
				LogAction("extTmpTempOk: ${retval} Exterior Temperature (${extTemp}°${getTemperatureScale()}) is ${tempOk ? "" : "Not"} ${str} using $diffThresh° offset |  Inside Temp: (${intTemp})", "info", showRes)

			}
		}
		storeExecutionHistory((now() - execTime), "extTmpTempOk")
		return retval
	} catch (ex) {
		log.error "extTmpTempOk Exception:", ex
		parent?.sendExceptionData(ex, "extTmpTempOk", true, getAutoType())
	}
}

def extTmpScheduleOk() { return autoScheduleOk(extTmpPrefix()) }
def getExtTmpTempDiffVal() { return !settings?.extTmpDiffVal ? 1.0 : settings?.extTmpDiffVal.toDouble() }
def getExtTmpInsideTempDiffVal() { return !settings?.extTmpInsideDiffVal ? (getTemperatureScale() == "C" ? 2 : 4) : settings?.extTmpInsideDiffVal.toDouble() }
def getExtTmpWhileOnDtSec() { return !atomicState?.extTmpChgWhileOnDt ? 100000 : GetTimeDiffSeconds(atomicState?.extTmpChgWhileOnDt, null, "getExtTmpWhileOnDtSec").toInteger() }
def getExtTmpWhileOffDtSec() { return !atomicState?.extTmpChgWhileOffDt ? 100000 : GetTimeDiffSeconds(atomicState?.extTmpChgWhileOffDt, null, "getExtTmpWhileOffDtSec").toInteger() }

// TODO allow override from schedule?
def getExtTmpOffDelayVal() { return !settings?.extTmpOffDelay ? 300 : settings?.extTmpOffDelay.toInteger() }
def getExtTmpOnDelayVal() { return !settings?.extTmpOnDelay ? 300 : settings?.extTmpOnDelay.toInteger() }

def extTmpTempCheck(cTimeOut = false) {
	LogAction("extTmpTempCheck", "trace", false)
	def pName = extTmpPrefix()

	try {
		if(atomicState?.disableAutomation) { return }
		else {

			def extTmpTstat = settings?.schMotTstat
			def extTmpTstatMir = settings?.schMotTstatMir

			def execTime = now()
			//atomicState?.lastEvalDt = getDtNow()

			if(!atomicState?."${pName}timeOutOn") { atomicState."${pName}timeOutOn" = false }
			if(cTimeOut) { atomicState."${pName}timeOutOn" = true }
			def timeOut = atomicState."${pName}timeOutOn" ?: false

			def curMode = extTmpTstat?.currentnestThermostatMode?.toString()
			def modeOff = (curMode in ["off", "eco"]) ? true : false
			def modeEco = (curMode in ["eco"]) ? true : false
			def allowNotif = settings?."${pName}NotificationsOn" ? true : false
			def allowSpeech = allowNotif && settings?."${pName}AllowSpeechNotif" ? true : false
			def allowAlarm = allowNotif && settings?."${pName}AllowAlarmNotif" ? true : false
			def speakOnRestore = allowSpeech && settings?."${pName}SpeechOnRestore" ? true : false

			if(!modeOff) { atomicState."${pName}timeOutOn" = false; timeOut = false }
// if we requested off; and someone switched us on or nMode took over...
			if( atomicState?.extTmpTstatOffRequested && (!modeEco || (modeEco && parent.setNModeActive(null))) ) {  // reset timer and states
				LogAction("extTmpTempCheck: | ${!modeEco ? "HVAC turned on when automation had OFF" : "Automation overridden by nMODE"}, resetting state to match", "warn", true)
				atomicState.extTmpChgWhileOnDt = getDtNow()
				atomicState.extTmpTstatOffRequested = false
				atomicState.extTmpChgWhileOffDt = getDtNow()
				atomicState?.extTmpRestoreMode = null
				atomicState."${pName}timeOutOn" = false
				unschedTimeoutRestore(pName)
			}

			def mylastMode = atomicState?."${pName}lastMode"  // when we state change that could change desired Temp ensure delays happen before off can happen again
			def lastDesired = atomicState?.extTmpLastDesiredTemp   // this catches scheduled temp or hvac mode changes
			def desiredTemp = getDesiredTemp()

			if( (mylastMode != curMode) || (desiredTemp && desiredTemp != lastDesired)) {
				if(!modeOff) {
					atomicState?."${pName}lastMode" = curMode
					if(desiredTemp) { atomicState?.extTmpLastDesiredTemp = desiredTemp }
					atomicState.extTmpChgWhileOnDt = getDtNow()
				} else {
					//atomicState.extTmpChgWhileOffDt = getDtNow()
				}
			}

			def safetyOk = getSafetyTempsOk(extTmpTstat)
			def schedOk = extTmpScheduleOk()
			def okToRestore = (modeEco && atomicState?.extTmpTstatOffRequested && atomicState?.extTmpRestoreMode) ? true : false
			def tempWithinThreshold = extTmpTempOk( ((modeEco && okToRestore) || (!modeEco && !okToRestore)), okToRestore)

			if(!tempWithinThreshold || timeOut || !safetyOk || !schedOk) {
				if(allowAlarm) { alarmEvtSchedCleanup(extTmpPrefix()) }
				def rmsg = ""
				if(okToRestore) {
					if(getExtTmpWhileOffDtSec() >= (getExtTmpOnDelayVal() - 5) || timeOut || !safetyOk) {
						def lastMode = null
						if(atomicState?.extTmpRestoreMode) {
							lastMode = extTmpTstat?.currentpreviousthermostatMode?.toString()
							if(!lastMode) { lastMode = atomicState?.extTmpRestoreMode }
						}
						if(lastMode && (lastMode != curMode || timeOut || !safetyOk || !schedOk)) {
							scheduleAutomationEval(60)
							if(setTstatMode(extTmpTstat, lastMode, pName)) {
								storeLastAction("Restored Mode ($lastMode)", getDtNow(), pName, extTmpTstat)
								atomicState?.extTmpRestoreMode = null
								atomicState?.extTmpTstatOffRequested = false
								atomicState?.extTmpRestoredDt = getDtNow()
								atomicState.extTmpChgWhileOnDt = getDtNow()
								atomicState."${pName}timeOutOn" = false
								unschedTimeoutRestore(pName)

								if(extTmpTstatMir) {
									if(setMultipleTstatMode(extTmpTstatMir, lastMode, pName)) {
										LogAction("Mirroring (${lastMode}) Restore to ${extTmpTstatMir}", "info", true)
									}
								}

								rmsg = "extTmpTempCheck: Restoring '${extTmpTstat?.label}' to '${strCapitalize(lastMode)}' mode: "
								def needAlarm = false
								if(!safetyOk) {
									rmsg += "External Temp Safety Temps reached"
									needAlarm = true
								} else if(!schedOk) {
									rmsg += "the schedule does not allow automation control"
								} else if(timeOut) {
									rmsg += "the (${getEnumValue(longTimeSecEnum(), extTmpOffTimeout)}) Timeout reached"
								} else {
									rmsg += "External Temp above the Threshold for (${getEnumValue(longTimeSecEnum(), extTmpOnDelay)})"
								}
								LogAction(rmsg, (needAlarm ? "warn" : "info"), true)
								if(allowNotif) {
									if(!timeOut && safetyOk) {
										sendEventPushNotifications(rmsg, "Info", pName)  // this uses parent and honors quiet times others do NOT
										if(speakOnRestore) { sendEventVoiceNotifications(voiceNotifString(atomicState?."${pName}OnVoiceMsg", pName), pName, "nmExtTmpOn_${app?.id}", true, "nmExtTmpOff_${app?.id}") }
									} else if(needAlarm) {
										sendEventPushNotifications(rmsg, "Warning", pName)
										if(allowAlarm) { scheduleAlarmOn(pName) }
									}
								}
								storeExecutionHistory((now() - execTime), "extTmpTempCheck")
								return

							} else { LogAction("extTmpTempCheck: | There was problem restoring the last mode to '", "error", true) }
						} else {
							if(!lastMode) {
								LogAction("extTmpTempCheck: | Unable to restore settings: previous mode not found. Likely other automation operation", "warn", true)
								atomicState?.extTmpTstatOffRequested = false
							} else if(!timeOut && safetyOk) { LogAction("extTmpTstatCheck: | Skipping Restore: the Mode to Restore is same as Current Mode ${curMode}", "info", true) }
							if(!safetyOk) { LogAction("extTmpTempCheck: | Unable to restore mode and safety temperatures are exceeded", "warn", true) }
							// TODO check if timeout quickly cycles back
						}
					} else {
						if(safetyOk) {
							def remaining = getExtTmpOnDelayVal() - getExtTmpWhileOffDtSec()
							LogAction("extTmpTempCheck: Delaying restore for wait period ${getExtTmpOnDelayVal()}, remaining ${remaining}", "info", true)
							def val = Math.min( Math.max(remaining,defaultAutomationTime()), 60)
							scheduleAutomationEval(val)
						}
					}
				} else {
					if(modeOff) {
						if(timeout || !safetyOk) {
							LogAction("extTmpTempCheck: | Timeout or Safety temps exceeded and Unable to restore settings okToRestore is false", "warn", true)
							atomicState."${pName}timeOutOn" = false
						}
						else if( (!atomicState?.extTmpRestoreMode && atomicState?.extTmpTstatOffRequested) ||
								(atomicState?.extTmpRestoreMode && !atomicState?.extTmpTstatOffRequested) ) {
							LogAction("extTmpTempCheck: | Unable to restore settings: previous mode not found.", "warn", true)
							atomicState?.extTmpRestoreMode = null
							atomicState?.extTmpTstatOffRequested = false
						}
					}
				}
			}

			if(tempWithinThreshold && !timeOut && safetyOk && schedOk && !modeEco) {
				def rmsg = ""
				if(!modeOff) {
					if(getExtTmpWhileOnDtSec() >= (getExtTmpOffDelayVal() - 2)) {
						atomicState."${pName}timeOutOn" = false
						atomicState?.extTmpRestoreMode = curMode
						LogAction("extTmpTempCheck: Saving ${extTmpTstat?.label} (${strCapitalize(atomicState?.extTmpRestoreMode)}) mode", "info", true)
						scheduleAutomationEval(60)
						if(setTstatMode(extTmpTstat, "eco", pName)) {
							storeLastAction("Set Thermostat ${extTmpTstat?.displayName} to ECO", getDtNow(), pName, extTmpTstat)
							atomicState?.extTmpTstatOffRequested = true
							atomicState.extTmpChgWhileOffDt = getDtNow()
							scheduleTimeoutRestore(pName)
							modeOff = true
							modeEco = true
							rmsg = "${extTmpTstat.label} turned 'ECO': External Temp is at the temp threshold for (${getEnumValue(longTimeSecEnum(), extTmpOffDelay)})"
							if(extTmpTstatMir) {
								if(setMultipleTstatMode(extTmpTstatMir, "eco", pName)) {
									LogAction("Mirroring (ECO) Mode to ${extTmpTstatMir}", "info", true)
								}
							}
							LogAction(rmsg, "info", true)
							if(allowNotif) {
								sendEventPushNotifications(rmsg, "Info", pName) // this uses parent and honors quiet times, others do NOT
								if(allowSpeech) { sendEventVoiceNotifications(voiceNotifString(atomicState?."${pName}OffVoiceMsg",pName), pName, "nmExtTmpOff_${app?.id}", true, "nmExtTmpOn_${app?.id}") }
								if(allowAlarm) { scheduleAlarmOn(pName) }
							}
						} else { LogAction("extTmpTempCheck: Error turning themostat to Eco", "warn", true) }
					} else {
						def remaining = getExtTmpOffDelayVal() - getExtTmpWhileOnDtSec()
						LogAction("extTmpTempCheck: Delaying ECO for wait period ${getExtTmpOffDelayVal()} seconds | Wait time remaining: ${remaining} seconds", "info", true)
						def val = Math.min( Math.max(remaining,defaultAutomationTime()), 60)
						scheduleAutomationEval(val)
					}
				} else {
					LogAction("extTmpTempCheck: | Skipping: Exterior temperatures in range and '${extTmpTstat?.label}' mode is 'OFF or ECO'", "info", true)
				}
			} else {
				if(timeOut) { LogAction("extTmpTempCheck: Skipping: active timeout", "info", true) }
				else if(!safetyOk) { LogAction("extTmpTempCheck: Skipping: Safety Temps Exceeded", "info", true) }
				else if(!schedOk) { LogAction("extTmpTempCheck: Skipping: Schedule Restrictions", "info", true) }
				else if(!tempWithinThreshold) { LogAction("extTmpTempCheck: Exterior temperatures not in range", "info", false) }
				else if(modeEco) { LogAction("extTmpTempCheck: Skipping: in ECO mode extTmpTstatOffRequested: (${atomicState?.extTmpTstatOffRequested})", "info", false) }
			}
			storeExecutionHistory((now() - execTime), "extTmpTempCheck")
		}
	} catch (ex) {
		log.error "extTmpTempCheck Exception:", ex
		parent?.sendExceptionData(ex, "extTmpTempCheck", true, getAutoType())
	}
}

def extTmpGenericEvt(evt) {
	def startTime = now()
	def eventDelay = startTime - evt.date.getTime()
	LogAction("${evt?.name.toUpperCase()} Event | Device: ${evt?.displayName} | Value: (${strCapitalize(evt?.value)}) with a delay of ${eventDelay}ms", "trace", true)
	storeLastEventData(evt)
	extTmpDpOrTempEvt("${evt?.name}")
}

def extTmpDpOrTempEvt(type) {
	if(atomicState?.disableAutomation) { return }
	else {
		atomicState.NeedwUpd = true
		if(settings?.extTmpUseWeather) { getExtConditions() }

		def lastTempWithinThreshold = atomicState?.extTmpLastWithinThreshold
		def tempWithinThreshold = extTmpTempOk(false,false)
		atomicState?.extTmpLastWithinThreshold = tempWithinThreshold

		if(lastTempWithinThreshold == null || tempWithinThreshold != lastTempWithinThreshold) {

			def extTmpTstat = settings?.schMotTstat
			def curMode = extTmpTstat?.currentnestThermostatMode.toString()
			def modeOff = (curMode in ["off", "eco"]) ? true : false
			def offVal = getExtTmpOffDelayVal()
			def onVal = getExtTmpOnDelayVal()
			def timeVal

			if(!modeOff) {
				atomicState.extTmpChgWhileOnDt = getDtNow()
				timeVal = ["valNum":offVal, "valLabel":getEnumValue(longTimeSecEnum(), offVal)]
			} else {
				atomicState.extTmpChgWhileOffDt = getDtNow()
				timeVal = ["valNum":onVal, "valLabel":getEnumValue(longTimeSecEnum(), onVal)]
			}
			def val = Math.min( Math.max(timeVal?.valNum,defaultAutomationTime()), 60)
			LogAction("${type} | External Temp Check scheduled for (${timeVal.valLabel}) HVAC mode: ${curMode}", "info", true)
			scheduleAutomationEval(val)
		} else {
			LogAction("${type}: Skipping no state change | tempWithinThreshold: ${tempWithinThreshold}", "info", false)
		}
	}
}

/******************************************************************************
|						WATCH CONTACTS AUTOMATION CODE			  			  |
*******************************************************************************/
def conWatPrefix() { return "conWat" }

def autoStateDesc(autotype) {
	def str = ""
	def t0 = atomicState?."${autotype}RestoreMode"
	def t1 = atomicState?."${autotype}TstatOffRequested"
	str += "ECO State:"
	str += "\n • Mode Adjusted: (${t0 != null ? "TRUE" : "FALSE"})"
	str += "\n •   Last Mode: (${strCapitalize(t0) ?: "Not Set"})"
	str += t1 ? "\n •   Last Eco Requested: (${t1})" : ""
	return str != "" ? str : null
}

def conWatContactDesc() {
	if(settings?.conWatContacts) {
		def cCnt = settings?.conWatContacts?.size() ?: 0
		def str = ""
		def cnt = 0
		str += "Contact Status:"
		settings?.conWatContacts?.sort { it?.displayName }?.each { dev ->
			cnt = cnt+1
			def val = strCapitalize(dev?.currentContact) ?: "Not Set"
			str += "${(cnt >= 1) ? "${(cnt == cCnt) ? "\n└" : "\n├"}" : "\n└"} ${dev?.label}: (${val})"
		}
		return str
	}
	return null
}

def isConWatConfigured() {
	return (settings?.conWatContacts && settings?.conWatOffDelay) ? true : false
}

def getConWatContactsOk() { return settings?.conWatContacts?.currentState("contact")?.value.contains("open") ? false : true }
//def conWatContactOk() { return (!settings?.conWatContacts) ? false : true }
def conWatScheduleOk() { return autoScheduleOk(conWatPrefix()) }
def getConWatOpenDtSec() { return !atomicState?.conWatOpenDt ? 100000 : GetTimeDiffSeconds(atomicState?.conWatOpenDt, null, "getConWatOpenDtSec").toInteger() }
def getConWatCloseDtSec() { return !atomicState?.conWatCloseDt ? 100000 : GetTimeDiffSeconds(atomicState?.conWatCloseDt, null, "getConWatCloseDtSec").toInteger() }
def getConWatRestoreDelayBetweenDtSec() { return !atomicState?.conWatRestoredDt ? 100000 : GetTimeDiffSeconds(atomicState?.conWatRestoredDt, null, "getConWatRestoreDelayBetweenDtSec").toInteger() }

// TODO allow override from schedule?
def getConWatOffDelayVal() { return !settings?.conWatOffDelay ? 300 : (settings?.conWatOffDelay.toInteger()) }
def getConWatOnDelayVal() { return !settings?.conWatOnDelay ? 300 : (settings?.conWatOnDelay.toInteger()) }
def getConWatRestoreDelayBetweenVal() { return !settings?.conWatRestoreDelayBetween ? 600 : settings?.conWatRestoreDelayBetween.toInteger() }

def conWatCheck(cTimeOut = false) {
	LogTrace("conWatCheck $cTimeOut")
	//
	// There should be monitoring of actual temps for min and max warnings given on/off automations
	//
	// Should have some check for stuck contacts
	//
	def pName = conWatPrefix()

	def conWatTstat = settings?.schMotTstat
	def conWatTstatMir = settings?.schMotTstatMir

	try {
		if(atomicState?.disableAutomation) { return }
		else {
			def execTime = now()
			//atomicState?.lastEvalDt = getDtNow()

			if(!atomicState?."${pName}timeOutOn") { atomicState."${pName}timeOutOn" = false }
			if(cTimeOut) { atomicState."${pName}timeOutOn" = true }
			def timeOut = atomicState."${pName}timeOutOn" ?: false
			def curMode = conWatTstat ? conWatTstat?.currentnestThermostatMode.toString() : null
			def modeEco = (curMode in ["eco"]) ? true : false
			//def curNestPres = getTstatPresence(conWatTstat)
			def modeOff = (curMode in ["off", "eco"]) ? true : false
			def openCtDesc = getOpenContacts(conWatContacts) ? " '${getOpenContacts(conWatContacts)?.join(", ")}' " : " a selected contact "
			def allowNotif = settings?."${pName}NotificationsOn" ? true : false
			def allowSpeech = allowNotif && settings?."${pName}AllowSpeechNotif" ? true : false
			def allowAlarm = allowNotif && settings?."${pName}AllowAlarmNotif" ? true : false
			def speakOnRestore = allowSpeech && settings?."${pName}SpeechOnRestore" ? true : false

			//log.debug "curMode: $curMode | modeOff: $modeOff | conWatRestoreOnClose: $conWatRestoreOnClose | lastMode: $lastMode"
			//log.debug "conWatTstatOffRequested: ${atomicState?.conWatTstatOffRequested} | getConWatCloseDtSec(): ${getConWatCloseDtSec()}"

			if(!modeEco) { atomicState."${pName}timeOutOn" = false; timeOut = false }

// if we requested off; and someone switched us on or nMode took over...
			if( atomicState?.conWatTstatOffRequested && (!modeEco || (modeEco && parent.setNModeActive(null))) ) {  // so reset timer and states
				LogAction("conWatCheck: | ${!modeEco ? "HVAC turned on when automation had OFF" : "Automation overridden by nMODE"}, resetting state to match", "warn", true)
				atomicState?.conWatRestoreMode = null
				atomicState?.conWatTstatOffRequested = false
				atomicState?.conWatOpenDt = getDtNow()
				atomicState."${pName}timeOutOn" = false
				unschedTimeoutRestore(pName)
			}

			def mylastMode = atomicState?."${pName}lastMode"  // when we state change modes, ensure delays happen before off can happen again
			atomicState?."${pName}lastMode" = curMode
			if(!modeOff && (mylastMode != curMode)) { atomicState?.conWatOpenDt = getDtNow() }

			def safetyOk = getSafetyTempsOk(conWatTstat)
			def schedOk = conWatScheduleOk()
			def okToRestore = (modeEco && atomicState?.conWatTstatOffRequested) ? true : false
			def contactsOk = getConWatContactsOk()

			if(contactsOk || timeOut || !safetyOk || !schedOk) {
				if(allowAlarm) { alarmEvtSchedCleanup(conWatPrefix()) }
				def rmsg = ""
				if(okToRestore) {
					if(getConWatCloseDtSec() >= (getConWatOnDelayVal() - 5) || timeOut || !safetyOk) {
						def lastMode = null
						if(atomicState?.conWatRestoreMode) {
							lastMode = conWatTstat?.currentpreviousthermostatMode?.toString()
							if(!lastMode) { lastMode = atomicState?.conWatRestoreMode }
						}
						if(lastMode && (lastMode != curMode || timeOut || !safetyOk || !schedOk)) {
							scheduleAutomationEval(60)
							if(setTstatMode(conWatTstat, lastMode, pName)) {
								storeLastAction("Restored Mode ($lastMode) to $conWatTstat", getDtNow(), pName, conWatTstat)
								atomicState?.conWatRestoreMode = null
								atomicState?.conWatTstatOffRequested = false
								atomicState?.conWatRestoredDt = getDtNow()
								atomicState?.conWatOpenDt = getDtNow()
								atomicState."${pName}timeOutOn" = false
								unschedTimeoutRestore(pName)
								modeEco = false
								modeOff = false

								if(conWatTstatMir) {
									if(setMultipleTstatMode(conWatTstatMir, lastMode, pName)) {
										LogAction("Mirroring (${lastMode}) Restore to ${conWatTstatMir}", "info", true)
									}
								}
								rmsg = "Restoring '${conWatTstat?.label}' to '${strCapitalize(lastMode)}' mode: "
								def needAlarm = false
								if(!safetyOk) {
									rmsg += "Global Safety Values reached"
									needAlarm = true
								} else if(timeOut) {
									rmsg += "(${getEnumValue(longTimeSecEnum(), conWatOffTimeout)}) Timeout reached"
								} else if(!schedOk) {
									rmsg += "of Schedule restrictions"
								} else {
									rmsg += "ALL contacts 'Closed' for (${getEnumValue(longTimeSecEnum(), conWatOnDelay)})"
								}

								LogAction(rmsg, (needAlarm ? "warn" : "info"), true)
//ERS
								if(allowNotif) {
									if(!timeOut && safetyOk) {
										sendEventPushNotifications(rmsg, "Info", pName) // this uses parent and honors quiet times, others do NOT
										if(speakOnRestore) { sendEventVoiceNotifications(voiceNotifString(atomicState?."${pName}OnVoiceMsg",pName), pName, "nmConWatOn_${app?.id}", true, "nmConWatOff_${app?.id}") }
									} else if(needAlarm) {
										sendEventPushNotifications(rmsg, "Warning", pName)
										if(allowAlarm) { scheduleAlarmOn(pName) }
									}
								}
								storeExecutionHistory((now() - execTime), "conWatCheck")
								return

							} else { LogAction("conWatCheck: | There was Problem Restoring the Last Mode to ($lastMode)", "error", true) }
						} else {
							if(!lastMode) {
								LogAction("conWatCheck: | Unable to restore settings: previous mode not found. Likely other automation operation", "warn", true)
								atomicState?.conWatTstatOffRequested = false
							} else if(!timeOut && safetyOk) { LogAction("conWatCheck: | Skipping Restore: the Mode to Restore is same as Current Mode ${curMode}", "info", true) }
							if(!safetyOk) { LogAction("conWatCheck: | Unable to restore mode and safety temperatures are exceeded", "warn", true) }
						}
					} else {
						if(safetyOk) {
							def remaining = getConWatOnDelayVal() - getConWatCloseDtSec()
							LogAction("conWatCheck: Delaying restore for wait period ${getConWatOnDelayVal()}, remaining ${remaining}", "info", true)
							def val = Math.min( Math.max(remaining,defaultAutomationTime()), 60)
							scheduleAutomationEval(val)
						}
					}
				} else {
					if(modeOff) {
						if(timeOut || !safetyOk) {
							LogAction("conWatCheck: | Timeout or Safety temps exceeded and Unable to restore settings okToRestore is false", "warn", true)
							atomicState."${pName}timeOutOn" = false
						}
						else if(!atomicState?.conWatRestoreMode && atomicState?.conWatTstatOffRequested) {
							LogAction("conWatCheck: | Unable to restore settings: previous mode not found. Likely other automation operation", "warn", true)
							atomicState?.conWatTstatOffRequested = false
						}
					}
				}
			}

			if(!contactsOk && safetyOk && !timeOut && schedOk && !modeEco) {
				def rmsg = ""
				if(!modeOff) {
					if((getConWatOpenDtSec() >= (getConWatOffDelayVal() - 2)) && (getConWatRestoreDelayBetweenDtSec() >= (getConWatRestoreDelayBetweenVal() - 2))) {
						atomicState."${pName}timeOutOn" = false
						atomicState?.conWatRestoreMode = curMode
						LogAction("conWatCheck: Saving ${conWatTstat?.label} mode (${strCapitalize(atomicState?.conWatRestoreMode)})", "info", true)
						LogAction("conWatCheck: ${openCtDesc}${getOpenContacts(conWatContacts).size() > 1 ? "are" : "is"} still Open: Turning 'OFF' '${conWatTstat?.label}'", "debug", true)
						scheduleAutomationEval(60)
						if(setTstatMode(conWatTstat, "eco", pName)) {
							storeLastAction("Set $conWatTstat to 'ECO'", getDtNow(), pName, conWatTstat)
							atomicState?.conWatTstatOffRequested = true
							atomicState?.conWatCloseDt = getDtNow()
							scheduleTimeoutRestore(pName)
							if(conWatTstatMir) {
								if(setMultipleTstatMode(conWatTstatMir, "eco", pName)) {
									LogAction("Mirroring (ECO) Mode to ${conWatTstatMir}", "info", true)
								}
							}
							rmsg = "${conWatTstat.label} turned to 'ECO': ${openCtDesc}Opened for (${getEnumValue(longTimeSecEnum(), conWatOffDelay)})"
							LogAction(rmsg, "info", true)
							if(allowNotif) {
								sendEventPushNotifications(rmsg, "Info", pName) // this uses parent and honors quiet times, others do NOT
								if(allowSpeech) { sendEventVoiceNotifications(voiceNotifString(atomicState?."${pName}OffVoiceMsg",pName), pName, "nmConWatOff_${app?.id}", true, "nmConWatOn_${app?.id}") }
								if(allowAlarm) { scheduleAlarmOn(pName) }
							}
						} else { LogAction("conWatCheck: Error turning themostat to ECO", "warn", true) }
					} else {
						if(getConWatRestoreDelayBetweenDtSec() < (getConWatRestoreDelayBetweenVal() - 2)) {
							def remaining = getConWatRestoreDelayBetweenVal() - getConWatRestoreDelayBetweenDtSec()
							LogAction("conWatCheck: | Skipping ECO change: delay since last restore not met (${getEnumValue(longTimeSecEnum(), conWatRestoreDelayBetween)})", "info", false)
							def val = Math.min( Math.max(remaining,defaultAutomationTime()), 60)
							scheduleAutomationEval(val)
						} else {
							def remaining = getConWatOffDelayVal() - getConWatOpenDtSec()
							LogAction("conWatCheck: Delaying ECO for wait period ${getConWatOffDelayVal()} seconds | Wait time remaining: ${remaining} seconds", "info", true)
							def val = Math.min( Math.max(remaining,defaultAutomationTime()), 60)
							scheduleAutomationEval(val)
						}
					}
				} else {
					LogAction("conWatCheck: | Skipping ECO change: '${conWatTstat?.label}' mode is '${curMode}'", "info", true)
				}
			} else {
				if(timeOut) { LogAction("conWatCheck: Skipping: active timeout", "info", true) }
				else if(!schedOk) { LogAction("conWatCheck: Skipping: Schedule Restrictions", "info", true) }
				else if(!safetyOk) { LogAction("conWatCheck: Skipping: Safety Temps Exceeded", "warn", true) }
				else if(contactsOk) { LogAction("conWatCheck: Contacts are closed", "info", true) }
				else if(modeEco) { LogAction("conWatTempCheck: Skipping: in ECO mode conWatTstatOffRequested: (${atomicState?.conWatTstatOffRequested})", "info", false) }
			}
			storeExecutionHistory((now() - execTime), "conWatCheck")
		}
	} catch (ex) {
		log.error "conWatCheck Exception:", ex
		parent?.sendExceptionData(ex, "conWatCheck", true, getAutoType())
	}
}

def conWatContactEvt(evt) {
	def startTime = now()
	def eventDelay = startTime - evt.date.getTime()
	LogAction("${evt?.name.toUpperCase()} Event | Device: ${evt?.displayName} | Value: (${strCapitalize(evt?.value)}) with a delay of ${eventDelay}ms", "trace", true)
	if(atomicState?.disableAutomation) { return }
	else {
		def conWatTstat = settings?.schMotTstat
		def curMode = conWatTstat?.currentnestThermostatMode.toString()
		def isModeOff = (curMode in ["eco"]) ? true : false
		def conOpen = (evt?.value == "open") ? true : false
		def canSched = false
		def timeVal
		if(conOpen) {
			atomicState?.conWatOpenDt = getDtNow()
			timeVal = ["valNum":getConWatOffDelayVal(), "valLabel":getEnumValue(longTimeSecEnum(), getConWatOffDelayVal())]
			canSched = true
		}
		else if(!conOpen && getConWatContactsOk()) {
			atomicState.conWatCloseDt = getDtNow()
			if(isModeOff) {
				timeVal = ["valNum":getConWatOnDelayVal(), "valLabel":getEnumValue(longTimeSecEnum(), getConWatOnDelayVal())]
				canSched = true
			}
		}
		storeLastEventData(evt)
		if(canSched) {
			LogAction("conWatContactEvt: Contact Check scheduled for (${timeVal?.valLabel})", "info", false)
			def val = Math.min( Math.max(timeVal?.valNum,defaultAutomationTime()), 60)
			scheduleAutomationEval(val)
		} else {
			LogAction("conWatContactEvt: Skipping Event", "info", false)
		}
	}
}

/******************************************************************************
|					WATCH FOR LEAKS AUTOMATION LOGIC CODE			  	  	  |
******************************************************************************/
def leakWatPrefix() { return "leakWat" }

def leakWatSensorsDesc() {
	if(settings?.leakWatSensors) {
		def cCnt = settings?.leakWatSensors?.size() ?: 0
		def str = ""
		def cnt = 0
		str += "Leak Sensors:"
		settings?.leakWatSensors?.sort { it?.displayName }?.each { dev ->
			cnt = cnt+1
			def val = strCapitalize(dev?.currentWater) ?: "Not Set"
			str += "${(cnt >= 1) ? "${(cnt == cCnt) ? "\n└" : "\n├"}" : "\n└"} ${dev?.label}: (${val})"
		}
		return str
	}
	return null
}

def isLeakWatConfigured() {
	return (settings?.leakWatSensors) ? true : false
}

def getLeakWatSensorsOk() { return settings?.leakWatSensors?.currentState("water")?.value.contains("wet") ? false : true }
def leakWatSensorsOk() { return (!settings?.leakWatSensors) ? false : true }
//def leakWatScheduleOk() { return autoScheduleOk(leakWatPrefix()) }

// TODO allow override from schedule?
def getLeakWatOnDelayVal() { return !settings?.leakWatOnDelay ? 300 : settings?.leakWatOnDelay.toInteger() }
def getLeakWatDryDtSec() { return !atomicState?.leakWatDryDt ? 100000 : GetTimeDiffSeconds(atomicState?.leakWatDryDt, null, "getLeakWatDryDtSec").toInteger() }

def leakWatCheck() {
	//LogTrace("leakWatCheck")
//
// TODO Should have some check for stuck contacts
//    if we cannot save/restore settings, don't bother turning things off
//
	def pName = leakWatPrefix()
	try {
		if(atomicState?.disableAutomation) { return }
		else {
			def leakWatTstat = settings?.schMotTstat
			def leakWatTstatMir = settings?.schMotTstatMir

			def execTime = now()
			//atomicState?.lastEvalDt = getDtNow()

			def curMode = leakWatTstat?.currentThermostatMode.toString()
			//def curNestPres = getTstatPresence(leakWatTstat)
			def modeOff = (curMode == "off") ? true : false
			def wetCtDesc = getWetWaterSensors(leakWatSensors) ? " '${getWetWaterSensors(leakWatSensors)?.join(", ")}' " : " a selected leak sensor "
			def allowNotif = settings?."${pName}NotificationsOn" ? true : false
			def allowSpeech = allowNotif && settings?."${pName}AllowSpeechNotif" ? true : false
			def allowAlarm = allowNotif && settings?."${pName}AllowAlarmNotif" ? true : false
			def speakOnRestore = allowSpeech && settings?."${pName}SpeechOnRestore" ? true : false

			if(!modeOff && atomicState?.leakWatTstatOffRequested) {  // someone switched us on when we had turned things off, so reset timer and states
				LogAction("leakWatCheck: | System turned on when automation had OFF, resetting state to match", "warn", true)
				atomicState?.leakWatRestoreMode = null
				atomicState?.leakWatTstatOffRequested = false
			}

			def safetyOk = getSafetyTempsOk(leakWatTstat)
			//def schedOk = leakWatScheduleOk()
			def okToRestore = (modeOff && atomicState?.leakWatTstatOffRequested) ? true : false
			def sensorsOk = getLeakWatSensorsOk()

			if(sensorsOk || !safetyOk) {
				if(allowAlarm) { alarmEvtSchedCleanup(leakWatPrefix()) }
				def rmsg = ""

				if(okToRestore) {
					if(getLeakWatDryDtSec() >= (getLeakWatOnDelayVal() - 5) || !safetyOk) {
						def lastMode = null
						if(atomicState?.leakWatRestoreMode) { lastMode = atomicState?.leakWatRestoreMode }
						if(lastMode && (lastMode != curMode || !safetyOk)) {
							scheduleAutomationEval(60)
							if(setTstatMode(leakWatTstat, lastMode, pName)) {
								storeLastAction("Restored Mode ($lastMode) to $leakWatTstat", getDtNow(), pName, leakWatTstat)
								atomicState?.leakWatTstatOffRequested = false
								atomicState?.leakWatRestoreMode = null
								atomicState?.leakWatRestoredDt = getDtNow()

								if(leakWatTstatMir) {
									if(setMultipleTstatMode(leakWatTstatMir, lastmode, pName)) {
										LogAction("leakWatCheck: Mirroring Restoring Mode (${lastMode}) to ${leakWatTstatMir}", "info", true)
									}
								}
								rmsg = "Restoring '${leakWatTstat?.label}' to '${strCapitalize(lastMode)}' mode: "
								def needAlarm = false
								if(!safetyOk) {
									rmsg += "External Temp Safety Temps reached"
									needAlarm = true
								} else {
									rmsg += "ALL leak sensors 'Dry' for (${getEnumValue(longTimeSecEnum(), leakWatOnDelay)})"
								}

								LogAction(rmsg, needAlarm ? "warn" : "info", true)
								if(allowNotif) {
									if(safetyOk) {
										sendEventPushNotifications(rmsg, "Info", pName) // this uses parent and honors quiet times, others do NOT
										if(speakOnRestore) { sendEventVoiceNotifications(voiceNotifString(atomicState?."${pName}OnVoiceMsg", pName), pName, "nmLeakWatOn_${app?.id}", true, "nmLeakWatOff_${app?.id}") }
									} else if(needAlarm) {
										sendEventPushNotifications(rmsg, "Warning", pName)
										if(allowAlarm) { scheduleAlarmOn(pName) }
									}
								}
								storeExecutionHistory((now() - execTime), "leakWatCheck")
								return

							} else { LogAction("leakWatCheck: | There was problem restoring the last mode to ${lastMode}", "error", true) }
						} else {
							if(!safetyOk) {
								LogAction("leakWatCheck: | Unable to restore mode and safety temperatures are exceeded", "warn", true)
							} else {
								LogAction("leakWatCheck: | Skipping Restore: Mode to Restore (${lastMode}) is same as Current Mode ${curMode}", "info", true)
							}
						}
					} else {
						if(safetyOk) {
							def remaining = getLeakWatOnDelayVal() - getLeakWatDryDtSec()
							LogAction("leakWatCheck: Delaying restore for wait period ${getLeakWatOnDelayVal()}, remaining ${remaining}", "info", true)
							def val = Math.min( Math.max(remaining,defaultAutomationTime()), 60)
							scheduleAutomationEval(val)
						}
					}
				} else {
					if(modeOff) {
						if(!safetyOk) {
							LogAction("leakWatCheck: | Safety temps exceeded and Unable to restore settings okToRestore is false", "warn", true)
						}
						else if(!atomicState?.leakWatRestoreMode && atomicState?.leakWatTstatOffRequested) {
							LogAction("leakWatCheck: | Unable to restore settings: previous mode not found. Likely other automation operation", "warn", true)
							atomicState?.leakWatTstatOffRequested = false
						}
					}
				}
			}

// tough decision here:  there is a leak, do we care about schedule ?
//		if(!getLeakWatSensorsOk() && safetyOk && schedOk) {
			if(!sensorsOk && safetyOk) {
				def rmsg = ""
				if(!modeOff) {
					atomicState?.leakWatRestoreMode = curMode
					LogAction("leakWatCheck: Saving ${leakWatTstat?.label} mode (${strCapitalize(atomicState?.leakWatRestoreMode)})", "info", true)
					LogAction("leakWatCheck: ${wetCtDesc}${getWetWaterSensors(leakWatSensors).size() > 1 ? "are" : "is"} Wet: Turning 'OFF' '${leakWatTstat?.label}'", "debug", true)
					scheduleAutomationEval(60)
					if(setTstatMode(leakWatTstat, "off", pName)) {
						storeLastAction("Turned Off $leakWatTstat", getDtNow(), pName, leakWatTstat)
						atomicState?.leakWatTstatOffRequested = true
						atomicState?.leakWatDryDt = getDtNow()

						if(leakWatTstatMir) {
							if(setMultipleTstatMode(leakWatTstatMir, "off", pName)) {
								LogAction("leakWatCheck: Mirroring (Off) Mode to ${leakWatTstatMir}", "info", true)
							}
						}
						rmsg = "${leakWatTstat.label} turned 'OFF': ${wetCtDesc}has reported it's WET"
						LogAction(rmsg, "warn", true)
						if(allowNotif) {
							sendEventPushNotifications(rmsg, "Warning", pName) // this uses parent and honors quiet times, others do NOT
							if(allowSpeech) { sendEventVoiceNotifications(voiceNotifString(atomicState?."${pName}OffVoiceMsg",pName), pName, "nmLeakWatOff_${app?.id}", true, "nmLeakWatOn_${app?.id}") }
							if(allowAlarm) { scheduleAlarmOn(pName) }
						}
					} else { LogAction("leakWatCheck: Error turning themostat Off", "warn", true) }
				} else {
					LogAction("leakWatCheck: | Skipping change: '${leakWatTstat?.label}' mode is already 'OFF'", "info", true)
				}
			} else {
				//if(!schedOk) { LogAction("leakWatCheck: Skipping: Schedule Restrictions", "warn", true) }
				if(!safetyOk) { LogAction("leakWatCheck: Skipping: Safety Temps Exceeded", "warn", true) }
				if(sensorsOk) { LogAction("leakWatCheck: Sensors are ok", "info", true) }
			}
			storeExecutionHistory((now() - execTime), "leakWatCheck")
		}
	} catch (ex) {
		log.error "leakWatCheck Exception:", ex
		parent?.sendExceptionData(ex, "leakWatCheck", true, getAutoType())
	}
}

def leakWatSensorEvt(evt) {
	def startTime = now()
	def eventDelay = startTime - evt.date.getTime()
	LogAction("${evt?.name.toUpperCase()} Event | Device: ${evt?.displayName} | Value: (${strCapitalize(evt?.value)}) with a delay of ${eventDelay}ms", "trace", true)
	if(atomicState?.disableAutomation) { return }
	else {
		def curMode = leakWatTstat?.currentThermostatMode.toString()
		def isModeOff = (curMode == "off") ? true : false
		def leakWet = (evt?.value == "wet") ? true : false

		def canSched = false
		def timeVal
		if(leakWet) {
			canSched = true
		}
		else if(!leakWet && getLeakWatSensorsOk()) {
			if(isModeOff) {
				atomicState?.leakWatDryDt = getDtNow()
				timeVal = ["valNum":getLeakWatOnDelayVal(), "valLabel":getEnumValue(longTimeSecEnum(), getLeakWatOnDelayVal())]
				canSched = true
			}
		}

		storeLastEventData(evt)
		if(canSched) {
			LogAction("leakWatSensorEvt: Leak Check scheduled (${timeVal?.valLabel})", "info", false)
			def val = Math.min( Math.max(timeVal?.valNum,defaultAutomationTime()), 60)
			scheduleAutomationEval(val)
		} else {
			LogAction("leakWatSensorEvt: Skipping Event", "info", true)
		}
	}
}

/********************************************************************************
|					MODE AUTOMATION CODE	     						|
*********************************************************************************/
def nModePrefix() { return "nMode" }

def nestModePresPage() {
	def pName = nModePrefix()
	dynamicPage(name: "nestModePresPage", title: "Nest Mode - Nest Home/Away Automation", uninstall: false, install: false) {
		if(!nModePresSensor && !nModeSwitch) {
			def modeReq = (!nModePresSensor && (nModeHomeModes || nModeAwayModes))
			section("Set Nest Presence with ST Modes:") {
				input "nModeHomeModes", "mode", title: "Modes to Set Nest Location 'Home'", multiple: true, submitOnChange: true, required: modeReq,
						image: getAppImg("mode_home_icon.png")
				if(checkModeDuplication(nModeHomeModes, nModeAwayModes)) {
					paragraph "ERROR:\nDuplicate Mode(s) were found under both the Home and Away Modes.\nPlease Correct to Proceed", required: true, state: null, image: getAppImg("error_icon.png")
				}
				input "nModeAwayModes", "mode", title: "Modes to Set Nest Location 'Away'", multiple: true, submitOnChange: true, required: modeReq,
						image: getAppImg("mode_away_icon.png")
				if(nModeHomeModes && nModeAwayModes) {
					def str = ""
					def locPres = getNestLocPres()
					str += location?.mode || locPres ? "Location Mode Status:" : ""
					str += location?.mode ? "\n${locPres ? "├" : "└"} SmartThings: (${location?.mode})" : ""
					str += locPres ? "\n└ Nest Location: (${locPres == "away" ? "Away" : "Home"})" : ""
					paragraph "${str}", state: (str != "" ? "complete" : null), image: getAppImg("instruct_icon.png")
				}
			}
		}
		if(!nModeHomeModes && !nModeAwayModes && !nModeSwitch) {
			section("(Optional) Set Nest Presence using Presence Sensor:") {
				//paragraph "Choose a Presence Sensor(s) to use to set your Nest to Home/Away", image: getAppImg("instruct_icon")
				def t0 = nModePresenceDesc()
				def presDesc = t0 ? "\n\n${t0}\n\nTap to modify" : "Tap to configure"
				input "nModePresSensor", "capability.presenceSensor", title: "Select Presence Sensor(s)", description: presDesc, multiple: true, submitOnChange: true, required: false,
						image: getAppImg("presence_icon.png")
				if(nModePresSensor) {
					if(nModePresSensor.size() > 1) {
						paragraph "Nest Location will be set to 'Away' when all Presence sensors leave and will return to 'Home' when someone arrives", title: "How this Works!", image: getAppImg("instruct_icon.png")
					}
					paragraph "${t0}", state: "complete", image: getAppImg("instruct_icon.png")
				}
			}
		}
		if(!nModePresSensor && !nModeHomeModes && !nModeAwayModes) {
			section("(Optional) Set Nest Presence based on the state of a Switch:") {
				input "nModeSwitch", "capability.switch", title: "Select a Switch", required: false, multiple: false, submitOnChange: true, image: getAppImg("switch_on_icon.png")
				if(nModeSwitch) {
					input "nModeSwitchOpt", "enum", title: "Switch State to Trigger 'Away'?", required: true, defaultValue: "On", options: ["On", "Off"], submitOnChange: true, image: getAppImg("settings_icon.png")
				}
			}
		}
		if(parent?.settings?.cameras) {
			section("Nest Cam Options:") {
				input (name: "nModeCamOnAway", type: "bool", title: "Turn On Nest Cams when Away?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("camera_green_icon.png"))
				input (name: "nModeCamOffHome", type: "bool", title: "Turn Off Nest Cams when Home?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("camera_gray_icon.png"))
				if(settings?.nModeCamOffHome || settings?.nModeCamOnAway) {
					paragraph title: "Optional" , "You can choose which cameras are changed when Home/Away.  If you don't select any devices all will be changed."
					input (name: "nModeCamsSel", type: "device.nestCamera", title: "Select your Nest Cams?", required: false, multiple: true, submitOnChange: true, image: getAppImg("camera_blue_icon.png"))
				}
			}
		}
		if((nModeHomeModes && nModeAwayModes) || nModePresSensor || nModeSwitch) {
			section("Additional Settings:") {
				input (name: "nModeSetEco", type: "bool", title: "Set ECO mode when away?", required: false, defaultValue: false, submitOnChange: true, image: getDevImg("eco_icon.png"))
				input (name: "nModeDelay", type: "bool", title: "Delay Changes?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("delay_time_icon.png"))
				if(nModeDelay) {
					input "nModeDelayVal", "enum", title: "Delay before change?", required: false, defaultValue: 60, metadata: [values:longTimeSecEnum()],
							submitOnChange: true, image: getAppImg("configure_icon.png")
				}
			}
		}
		if(((nModeHomeModes && nModeAwayModes) && !nModePresSensor) || nModePresSensor) {
			section(getDmtSectionDesc(nModePrefix())) {
				def pageDesc = getDayModeTimeDesc(pName)
				href "setDayModeTimePage", title: "Configured Restrictions", description: pageDesc, params: ["pName": "${pName}"], state: (pageDesc ? "complete" : null),
						image: getAppImg("cal_filter_icon.png")
			}
			section("Notifications:") {
				def pageDesc = getNotifConfigDesc(pName)
				href "setNotificationPage", title: "Configured Alerts", description: pageDesc, params: ["pName":"${pName}", "allowSpeech":false, "allowAlarm":false, "showSchedule":true],
						state: (pageDesc ? "complete" : null), image: getAppImg("notification_icon.png")
			}
		}
		if(atomicState?.showHelp) {
			section("Help:") {
				href url:"${getAutoHelpPageUrl()}", style:"embedded", required:false, title:"Help and Instructions", description:"", image: getAppImg("info.png")
			}
		}
	}
}

def nModePresenceDesc() {
	if(settings?.nModePresSensor) {
		def cCnt = nModePresSensor?.size() ?: 0
		def str = ""
		def cnt = 0
		str += "Presence Status:"
		settings?.nModePresSensor?.sort { it?.displayName }?.each { dev ->
			cnt = cnt+1
			def presState = strCapitalize(dev?.currentPresence) ?: "No State"
			str += "${(cnt >= 1) ? "${(cnt == cCnt) ? "\n└" : "\n├"}" : "\n└"} ${dev?.label}: ${(dev?.label?.toString()?.length() > 10) ? "\n${(cCnt == 1 || cnt == cCnt) ? "    " : "│"}└ " : ""}(${presState})"
		}
		return str
	}
	return null
}

def isNestModesConfigured() {
	def devOk = ((!nModePresSensor && !nModeSwitch && (nModeHomeModes && nModeAwayModes)) || (nModePresSensor && !nModeSwitch) || (!nModePresSensor && nModeSwitch)) ? true : false
	return devOk
}

def nModeGenericEvt(evt) {
	def startTime = now()
	def eventDelay = startTime - evt.date.getTime()
	LogAction("${evt?.name.toUpperCase()} Event | Device: ${evt?.displayName} | Value: (${strCapitalize(evt?.value)}) with a delay of ${eventDelay}ms", "trace", true)
	if(atomicState?.disableAutomation) { return }
	storeLastEventData(evt)
	if(nModeDelay) {
		def delay = nModeDelayVal.toInteger() ?: 60
		if(delay > defaultAutomationTime()) {
			LogAction("Event | A Check is scheduled (${getEnumValue(longTimeSecEnum(), nModeDelayVal)})", "info", false)
			scheduleAutomationEval(delay)
		} else { scheduleAutomationEval() }
	} else {
		scheduleAutomationEval()
	}
}

def adjustCameras(on, sendAutoType=null) {
	def cams = settings?.nModeCamsSel ?: parent?.getCams()
	def foundCams
	if(cams) {
		if(settings?.nModeCamsSel) {
			foundCams = cams
		} else {
			foundCams = cams?.collect { parent.getCameraDevice(it) }
		}
		foundCams.each { dev ->
			if(dev) {
				def didstr = "On"
				try {
					if(on) {
						dev?.on()
					} else {
						dev?.off()
						didstr = "Off"
					}
					LogAction("adjustCameras: Turning Streaming ${didstr} for (${dev?.displayName})", "info", true)
					storeLastAction("Turned ${didstr} Streaming ${dev?.displayName}", getDtNow(), sendAutoType)
				}
				catch (ex) {
					log.error "adjustCameras() Exception: ${dev?.label} does not support commands on / off", ex
					sendNofificationMsg("Warning", "Camera commands not found, check IDE logs and installation instructions")
					parent?.sendExceptionData(ex, "adjustCameras", true, getAutoType())
				}
				return dev
			}
		}
	}
}

def adjustEco(on, senderAutoType=null) {
	def tstats = parent?.getTstats()
	def foundTstats
	if(tstats) {
		foundTstats = tstats?.collect { dni ->
			def d1 = parent.getThermostatDevice(dni)
			if(d1) {
				def didstr = null
				def curMode = d1?.currentnestThermostatMode?.toString()
				if(on && (curMode in ["eco"])) {
					if(senderAutoType) { sendEcoActionDescToDevice(d1, senderAutoType) } // THIS ONLY WORKS ON NEST THERMOSTATS
				}
				if(on && !(curMode in ["eco", "off"])) {
					didstr = "ECO"
					setTstatMode(d1, "eco", senderAutoType)
				}
				def prevMode = d1?.currentpreviousthermostatMode?.toString()
				LogAction("adjustEco: CURMODE: ${curMode} ON: ${on} PREVMODE: ${prevMode}", "trace", false)
				if(!on && curMode in ["eco"]) {
					if(prevMode && prevMode != curMode) {
						didstr = "$prevMode"
						setTstatMode(d1, prevMode, senderAutoType)
					}
				}
				if(didstr) {
					LogAction("adjustEco($on): | Thermostat: ${d1?.displayName} setting to HVAC mode $didstr was $curMode", "trace", true)
					storeLastAction("Set ${d1?.displayName} to $didstr", getDtNow(), senderAutoType, d1)
				}
				return d1
			} else { LogAction("adjustEco NO D1", "warn", true) }
		}
	}
}

def setAway(away) {
	def tstats = parent?.getTstats()
	def didstr = away ? "AWAY" : "HOME"
	def foundTstats
	if(tstats) {
		foundTstats = tstats?.collect { dni ->
			def d1 = parent.getThermostatDevice(dni)
			if(d1) {
				if(away) {
					d1?.away()
				} else {
					d1?.present()
				}
				LogAction("setAway($away): | Thermostat: ${d1?.displayName} setting to $didstr", "trace", true)
				storeLastAction("Set ${d1?.displayName} to $didstr", getDtNow(), "nMode", d1)
				return d1
			} else { LogAction("setaway NO D1", "warn", true) }
		}
	} else {
		if(away) {
			parent?.setStructureAway(null, true)
		} else {
			parent?.setStructureAway(null, false)
		}
		LogAction("setAway($away): | Setting structure to $didstr", "trace", true)
		storeLastAction("Set structure to $didstr", getDtNow(), "nMode")
	}
}

def nModeScheduleOk() { return autoScheduleOk(nModePrefix()) }

def checkNestMode() {
	LogAction("checkNestMode", "trace", false)
//
// This automation only works with Nest as it toggles non-ST standard home/away
//
	def pName = nModePrefix()
	try {
		if(atomicState?.disableAutomation) { return }
		else if(!nModeScheduleOk()) {
			LogAction("checkNestMode: Skipping: Schedule Restrictions", "info", true)
		} else {
			def execTime = now()
			atomicState?.lastEvalDt = getDtNow()

			def curStMode = location?.mode
			def allowNotif = settings?."${nModePrefix()}NotificationsOn" ? true : false
			def nestModeAway = (getNestLocPres() == "home") ? false : true
			def awayPresDesc = (nModePresSensor && !nModeSwitch) ? "All Presence device(s) have left setting " : ""
			def homePresDesc = (nModePresSensor && !nModeSwitch) ? "A Presence Device is Now Present setting " : ""
			def awaySwitDesc = (nModeSwitch && !nModePresSensor) ? "${nModeSwitch} State is 'Away' setting " : ""
			def homeSwitDesc = (nModeSwitch && !nModePresSensor) ? "${nModeSwitch} State is 'Home' setting " : ""
			def modeDesc = ((!nModeSwitch && !nModePresSensor) && nModeHomeModes && nModeAwayModes) ? "The mode (${curStMode}) has triggered " : ""
			def awayDesc = "${awayPresDesc}${awaySwitDesc}${modeDesc}"
			def homeDesc = "${homePresDesc}${homeSwitDesc}${modeDesc}"

			def away = false
			def home = false

			if(nModePresSensor && !nModeSwitch) {
				if(!isPresenceHome(nModePresSensor)) {
					away = true
				} else {
					home = true
				}
			} else if(nModeSwitch && !nModePresSensor) {
				def swOptAwayOn = (nModeSwitchOpt == "On") ? true : false
				if(swOptAwayOn) {
					!isSwitchOn(nModeSwitch) ? (home = true) : (away = true)
				} else {
					!isSwitchOn(nModeSwitch) ? (away = true) : (home = true)
				}
			} else if(nModeHomeModes && nModeAwayModes) {
				if(isInMode(nModeHomeModes)) {
					home = true
				} else {
					if(isInMode(nModeAwayModes)) { away = true }
				}
			} else {
				LogAction("checkNestMode: Nothing Matched", "info", true)
			}

			def didsomething = false
			//if(away && !nestModeAway && !modeMatch) {
			if(away && !nestModeAway) {
				LogAction("checkNestMode: ${awayDesc} Nest 'Away'", "info", true)
				didsomething = true
				setAway(true)
				atomicState?.nModeTstatLocAway = true
				if(nModeSetEco) {
					parent.setNModeActive(true) // set nMode has it in manager
					adjustEco(true, pName)
				}
				if(allowNotif) {
					sendEventPushNotifications("${awayDesc} Nest 'Away'", "Info", pName)
				}
				if(nModeCamOnAway) { adjustCameras(true, pName) }
			}
			//else if(home && nestModeAway && !modeMatch) {
			else if(home && nestModeAway) {
				LogAction("checkNestMode: ${homeDesc} Nest 'Home'", "info", true)
				didsomething = true
				setAway(false)
				parent.setNModeActive(false)		// clear nMode has it in manager
				atomicState?.nModeTstatLocAway = false
				if(nModeSetEco) { adjustEco(false, pName) }
				if(allowNotif) {
					sendEventPushNotifications("${homeDesc} Nest 'Home'", "Info", pName)
				}
				if(nModeCamOffHome) { adjustCameras(false, pName) }
			}
			else {
				LogAction("checkNestMode: No Changes | ${nModePresSensor ? "isPresenceHome: ${isPresenceHome(nModePresSensor)} | " : ""}ST-Mode: ($curStMode) | NestModeAway: ($nestModeAway) | Away: ($away) | Home: ($home)", "info", false)
			}
			if(didsomething) {
				scheduleAutomationEval(90)
			}
			storeExecutionHistory((now() - execTime), "checkNestMode")
		}
	} catch (ex) {
		log.error "checkNestMode Exception:", ex
		parent?.sendExceptionData(ex, "checkNestMode", true, getAutoType())
	}
}

def getNestLocPres() {
	if(atomicState?.disableAutomation) { return }
	else {
		def plocationPresence = parent?.getLocationPresence()
		if(!plocationPresence) { return null }
		else {
			return plocationPresence
		}
	}
}

/********************************************************************************
|		SCHEDULE, MODE, or MOTION CHANGES ADJUST THERMOSTAT SETPOINTS			|
|		(AND THERMOSTAT MODE) AUTOMATION CODE									|
*********************************************************************************/

def getTstatAutoDevId() {
	if(settings?.schMotTstat) { return settings?.schMotTstat.deviceNetworkId.toString() }
	return null
}

private tempRangeValues() {
	return (getTemperatureScale() == "C") ? "10..32" : "50..90"
}

private timeComparisonOptionValues() {
	return ["custom time", "midnight", "sunrise", "noon", "sunset"]
}

private timeDayOfWeekOptions() {
	return ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]
}

private getDayOfWeekName(date = null) {
	if (!date) {
		date = adjustTime()
	}
	def theDay = date.day.toInteger()
	def list = []
	list = timeDayOfWeekOptions()
	//LogAction("theDay: $theDay date.date: ${date.day}")
	return(list[theDay].toString())
/*
	switch (date.day) {
		case 0: return "Sunday"
		case 1: return "Monday"
		case 2: return "Tuesday"
		case 3: return "Wednesday"
		case 4: return "Thursday"
		case 5: return "Friday"
		case 6: return "Saturday"
	}
*/
	return null
}

/*
private getDayOfWeekNumber(date = null) {
	if (!date) {
		date = adjustTime(now())
	}
	if (date instanceof Date) {
		return date.day
	}
	switch (date) {
		case "Sunday": return 0
		case "Monday": return 1
		case "Tuesday": return 2
		case "Wednesday": return 3
		case "Thursday": return 4
		case "Friday": return 5
		case "Saturday": return 6
	}
	return null
}
*/

//adjusts the time to local timezone
private adjustTime(time = null) {
	if (time instanceof String) {
		//get UTC time
		time = timeToday(time, location.timeZone).getTime()
	}
	if (time instanceof Date) {
		//get unix time
		time = time.getTime()
	}
	if (!time) {
		time = now()
	}
	if (time) {
		return new Date(time + location.timeZone.getOffset(time))
	}
	return null
}

private formatLocalTime(time, format = "EEE, MMM d yyyy @ h:mm a z") {
	if (time instanceof Long) {
		time = new Date(time)
	}
	if (time instanceof String) {
		//get UTC time
		time = timeToday(time, location.timeZone)
	}
	if (!(time instanceof Date)) {
		return null
	}
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(time)
}

private convertDateToUnixTime(date) {
	if (!date) {
		return null
	}
	if (!(date instanceof Date)) {
		date = new Date(date)
	}
	return date.time - location.timeZone.getOffset(date.time)
}

private convertTimeToUnixTime(time) {
	if (!time) {
		return null
	}
	return time - location.timeZone.getOffset(time)
}

private formatTime(time, zone = null) {
	//we accept both a Date or a settings' Time
	return formatLocalTime(time, "h:mm a${zone ? " z" : ""}")
}

private formatHour(h) {
	return (h == 0 ? "midnight" : (h < 12 ? "${h} AM" : (h == 12 ? "noon" : "${h-12} PM"))).toString()
}

private cleanUpMap(map) {
	def washer = []
	//find dirty laundry
	for (item in map) {
		if (item.value == null) washer.push(item.key)
	}
	//clean it
	for (item in washer) {
		map.remove(item)
	}
	washer = null
	return map
}

private buildDeviceNameList(devices, suffix) {
	def cnt = 1
	def result = ""
	for (device in devices) {
		def label = getDeviceLabel(device)
		result += "$label" + (cnt < devices.size() ? (cnt == devices.size() - 1 ? " $suffix " : ", ") : "")
		cnt++
	}
	if(result == "") { result = null }
	return result
}

private getDeviceLabel(device) {
	return device instanceof String ? device : (device ? ( device.label ? device.label : (device.name ? device.name : "$device")) : "Unknown device")
}

def getCurrentSchedule() {
	def noSched = false
	def mySched

	def schedList = getScheduleList()
	def res1
	def ccnt = 1
	for (cnt in schedList) {
		res1 = checkRestriction(cnt)
		if(res1 == null) { break }
		ccnt += 1
	}
	if(ccnt > schedList?.size()) { noSched = true }
	else { mySched = ccnt }
	LogTrace("getCurrentSchedule: mySched: $mySched noSched: $noSched ccnt: $ccnt res1: $res1")
	return mySched
}

private checkRestriction(cnt) {
	//LogTrace("checkRestriction:( $cnt )")
	def sLbl = "schMot_${cnt}_"
	def restriction
	def act = settings["${sLbl}SchedActive"]
	if(act) {
		def apprestrict = atomicState?."sched${cnt}restrictions"

		if (apprestrict?.m && apprestrict?.m.size() && !(location.mode in apprestrict?.m)) {
			restriction = "a SmartThings MODE mismatch"
		} else if (apprestrict?.w && apprestrict?.w.size() && !(getDayOfWeekName() in apprestrict?.w)) {
			restriction = "a day of week mismatch"
		} else if (apprestrict?.tf && apprestrict?.tt && !(checkTimeCondition(apprestrict?.tf, apprestrict?.tfc, apprestrict?.tfo, apprestrict?.tt, apprestrict?.ttc, apprestrict?.tto))) {
			restriction = "a time of day mismatch"
		} else {
			if (settings["${sLbl}restrictionSwitchOn"]) {
				for(sw in settings["${sLbl}restrictionSwitchOn"]) {
					if (sw.currentValue("switch") != "on") {
						restriction = "switch ${sw} being ${sw.currentValue("switch")}"
						break
					}
				}
			}
			if (!restriction && settings["${sLbl}restrictionSwitchOff"]) {
				for(sw in settings["${sLbl}restrictionSwitchOff"]) {
					if (sw.currentValue("switch") != "off") {
						restriction = "switch ${sw} being ${sw.currentValue("switch")}"
						break
					}
				}
			}
			if (!restriction && settings["${sLbl}restrictionPresHome"] && !isSomebodyHome(settings["${sLbl}restrictionPresHome"])) {
				for(pr in settings["${sLbl}restrictionPresHome"]) {
					if (!isPresenceHome(pr)) {
						restriction = "presence ${pr} being ${pr.currentValue("presence")}"
						break
					}
				}
			}
			if (!restriction && settings["${sLbl}restrictionPresAway"] && isSomebodyHome(settings["${sLbl}restrictionPresAway"])) {
				for(pr in settings["${sLbl}restrictionPresAway"]) {
					if (isPresenceHome(pr)) {
						restriction = "presence ${pr} being ${pr.currentValue("presence")}"
						break
					}
				}
			}
		}
		LogTrace("checkRestriction:( $cnt ) restriction: $restriction")
	} else {
		restriction = "an inactive schedule"
	}
	return restriction
}

def getActiveScheduleState() {
	return atomicState?.activeSchedData ?: null
}

def getSchRestrictDoWOk(cnt) {
	def apprestrict = atomicState?.activeSchedData
	def result = true
	apprestrict?.each { sch ->
		if(sch?.key.toInteger() == cnt.toInteger()) {
			if (!(getDayOfWeekName().toString() in sch?.value?.w)) {
				result = false
			}
		}
	}
	return result
}

private checkTimeCondition(timeFrom, timeFromCustom, timeFromOffset, timeTo, timeToCustom, timeToOffset) {
	def time = adjustTime()
	//convert to minutes since midnight
	def tc = time.hours * 60 + time.minutes
	def tf
	def tt
	def i = 0
	while (i < 2) {
		def t = null
		def h = null
		def m = null
		switch(i == 0 ? timeFrom : timeTo) {
			case "custom time":
				t = adjustTime(i == 0 ? timeFromCustom : timeToCustom)
				if (i == 0) {
					timeFromOffset = 0
				} else {
					timeToOffset = 0
				}
				break
			case "sunrise":
				t = getSunrise()
				break
			case "sunset":
				t = getSunset()
				break
			case "noon":
				h = 12
				break
			case "midnight":
				h = (i == 0 ? 0 : 24)
			break
		}
		if (h != null) {
			m = 0
		} else {
			h = t.hours
			m = t.minutes
		}
		switch (i) {
			case 0:
				tf = h * 60 + m + cast(timeFromOffset, "number")
				break
			case 1:
				tt = h * 60 + m + cast(timeFromOffset, "number")
				break
		}
		i += 1
	}
	//due to offsets, let's make sure all times are within 0-1440 minutes
	while (tf < 0) tf += 1440
	while (tf > 1440) tf -= 1440
	while (tt < 0) tt += 1440
	while (tt > 1440) tt -= 1440
	if (tf < tt) {
		return (tc >= tf) && (tc < tt)
	} else {
		return (tc < tt) || (tc >= tf)
	}
}

private cast(value, dataType) {
	def trueStrings = ["1", "on", "open", "locked", "active", "wet", "detected", "present", "occupied", "muted", "sleeping"]
	def falseStrings = ["0", "false", "off", "closed", "unlocked", "inactive", "dry", "clear", "not detected", "not present", "not occupied", "unmuted", "not sleeping"]
	switch (dataType) {
		case "string":
		case "text":
			if (value instanceof Boolean) {
				return value ? "true" : "false"
			}
			return value ? "$value" : ""
		case "number":
			if (value == null) return (int) 0
			if (value instanceof String) {
				if (value.isInteger())
					return value.toInteger()
				if (value.isFloat())
					return (int) Math.floor(value.toFloat())
				if (value in trueStrings)
					return (int) 1
			}
			def result = (int) 0
			try {
				result = (int) value
			} catch(all) {
				result = (int) 0
			}
			return result ? result : (int) 0
		case "long":
			if (value == null) return (long) 0
			if (value instanceof String) {
				if (value.isInteger())
					return (long) value.toInteger()
				if (value.isFloat())
					return (long) Math.round(value.toFloat())
				if (value in trueStrings)
					return (long) 1
			}
			def result = (long) 0
			try {
				result = (long) value
			} catch(all) {
			}
			return result ? result : (long) 0
		case "decimal":
			if (value == null) return (float) 0
			if (value instanceof String) {
				if (value.isFloat())
					return (float) value.toFloat()
				if (value.isInteger())
					return (float) value.toInteger()
				if (value in trueStrings)
					return (float) 1
			}
			def result = (float) 0
			try {
				result = (float) value
			} catch(all) {
			}
			return result ? result : (float) 0
		case "boolean":
			if (value instanceof String) {
				if (!value || (value in falseStrings))
					return false
				return true
			}
			return !!value
		case "time":
			return value instanceof String ? adjustTime(value).time : cast(value, "long")
		case "vector3":
			return value instanceof String ? adjustTime(value).time : cast(value, "long")
	}
	return value
}

//TODO is this expensive in ST?
private getSunrise() {
	def sunTimes = getSunriseAndSunset()
	return adjustTime(sunTimes.sunrise)
}

private getSunset() {
	def sunTimes = getSunriseAndSunset()
	return adjustTime(sunTimes.sunset)
}

def isTstatSchedConfigured() {
	//return (settings?.schMotSetTstatTemp && atomicState?.activeSchedData?.size())
	return (atomicState.scheduleSchedActiveCount)
}

/* //NOT IN USE ANYMORE (Maybe we should keep for future use)
def isTimeBetween(start, end, now, tz) {
	def startDt = Date.parse("E MMM dd HH:mm:ss z yyyy", start).getTime()
	def endDt = Date.parse("E MMM dd HH:mm:ss z yyyy", end).getTime()
	def nowDt = Date.parse("E MMM dd HH:mm:ss z yyyy", now).getTime()
	def result = false
	if(nowDt > startDt && nowDt < endDt) {
		result = true
	}
	//def result = timeOfDayIsBetween(startDt, endDt, nowDt, tz) ? true : false
	return result
}
*/

def checkOnMotion(mySched) {
	LogTrace("checkOnMotion($mySched)")
	def sLbl = "schMot_${mySched}_"

	if(settings["${sLbl}Motion"] && atomicState?."${sLbl}MotionActiveDt") {
		def motionOn = isMotionActive(settings["${sLbl}Motion"])

		def lastActiveMotionDt = Date.parse("E MMM dd HH:mm:ss z yyyy", atomicState?."${sLbl}MotionActiveDt").getTime()
		def lastActiveMotionSec = getLastMotionActiveSec(mySched)

		def lastInactiveMotionDt = 1
		def lastInactiveMotionSec

		if(atomicState?."${sLbl}MotionInActiveDt") {
			lastInactiveMotionDt = Date.parse("E MMM dd HH:mm:ss z yyyy", atomicState?."${sLbl}MotionInActiveDt").getTime()
			lastInactiveMotionSec = getLastMotionInActiveSec(mySched)
		}

		LogAction("checkOnMotion: [ActiveDt: ${lastActiveMotionDt} (${lastActiveMotionSec} sec) | InActiveDt: ${lastInactiveMotionDt} (${lastInactiveMotionSec} sec) | MotionOn: ($motionOn)", "trace", false)

		def ontimedelay = (settings."${sLbl}MDelayValOn"?.toInteger() ?: 60) * 1000 		// default to 60s
		def offtimedelay = (settings."${sLbl}MDelayValOff"?.toInteger() ?: 30*60) * 1000	// default to 30 min

		def ontimeNum = lastActiveMotionDt + ontimedelay
		def offtimeNum = lastInactiveMotionDt + offtimedelay

		def nowDt = Date.parse("E MMM dd HH:mm:ss z yyyy", getDtNow()).getTime()
		if(ontimeNum > offtimeNum) {  // means motion is on now, so ensure offtime is in future
			offtimeNum = nowDt + offtimedelay
		}

		def lastOnTime			// if we are on now, backup ontime to not oscillate
		if(atomicState?."motion${mySched}UseMotionSettings" && atomicState?."motion${mySched}TurnedOnDt") {
			lastOnTime = Date.parse("E MMM dd HH:mm:ss z yyyy", atomicState?."motion${mySched}TurnedOnDt").getTime()
			if(ontimeNum > lastOnTime) {
				ontimeNum = lastOnTime - ontimedelay
			}
		}

		def ontime = formatDt( ontimeNum )
		def offtime = formatDt( offtimeNum )

		LogAction("checkOnMotion: [ActiveDt: (${atomicState."${sLbl}MotionActiveDt"}) | OnTime: ($ontime) | InActiveDt: (${atomicState?."${sLbl}MotionInActiveDt"}) | OffTime: ($offtime)]", "info", true)
		def result = false
		if(nowDt >= ontimeNum && nowDt <= offtimeNum) {
			result = true
		}
		if(nowDt < ontimeNum || (result && !motionOn)) {
			LogAction("checkOnMotion: (Schedule $mySched - ${getSchedLbl(mySched)}) Scheduling Motion Check (60 sec)", "trace", true)
			scheduleAutomationEval(60)
		}
		return result
	}
	return false
}

def setTstatTempCheck() {
	LogAction("setTstatTempCheck", "trace", false)
	/* NOTE:
		// This automation only works with Nest as it checks non-ST presence & thermostat capabilities
		// Presumes: That all thermostats in an automation are in the same Nest structure, so that all share home/away settings and tStat modes
	*/
	try {

		if(atomicState?.disableAutomation) { return }
		def execTime = now()

		def tstat = settings?.schMotTstat
		def tstatMir = settings?.schMotTstatMir

		def pName = schMotPrefix()

		def curMode = tstat ? tstat?.currentnestThermostatMode.toString() : null

		def lastMode = atomicState?."${pName}lastMode"
		def samemode = lastMode == curMode ? true : false

		def mySched = getCurrentSchedule()
		LogAction("setTstatTempCheck | Current Schedule: (${mySched ? ("${mySched} - ${getSchedLbl(mySched)}") : "None Active"})", "debug", false)
		def noSched = (mySched == null) ? true : false

		def previousSched = atomicState?.lastSched
		def samesched = previousSched == mySched ? true : false

		if((!samesched || !samemode) && previousSched) {		// schedule change - set old schedule to not use motion
			if(atomicState?."motion${previousSched}UseMotionSettings") {
				LogAction("setTstatTempCheck: Disabled Motion Settings Used for Previous Schedule (${previousSched} - ${getSchedLbl(previousSched)}", "info", true)
			}
			atomicState?."motion${previousSched}UseMotionSettings" = false
			atomicState?."motion${previousSched}LastisBtwn" = false
		}

		if(!samesched || !samemode ) {			// schedule change, clear out overrides
			disableOverrideTemps()
		}

		LogAction("setTstatTempCheck: [Current Schedule: (${getSchedLbl(mySched)}) | Previous Schedule: (${previousSched} - ${getSchedLbl(previousSched)}) | None: ($noSched)]", "trace", false)

		if(noSched) {
			LogAction("setTstatTempCheck: Skipping check [No matching Schedule]", "info", true)
		} else {
			def isBtwn = checkOnMotion(mySched)
			def previousBtwn = atomicState?."motion${mySched}LastisBtwn"
			atomicState?."motion${mySched}LastisBtwn" = isBtwn

			if(!isBtwn) {
				if(atomicState?."motion${mySched}UseMotionSettings") {
					LogAction("setTstatTempCheck: Disabled Use of Motion Settings for Schedule (${mySched} - ${getSchedLbl(mySched)})", "info", true)
				}
				atomicState?."motion${mySched}UseMotionSettings" = false
			}

			def sLbl = "schMot_${mySched}_"
			def motionOn = isMotionActive(settings["${sLbl}Motion"])

			if(!atomicState?."motion${mySched}UseMotionSettings" && isBtwn && !previousBtwn) {   // transitioned to use Motion
				if(motionOn) {	// if motion is on use motion now
					atomicState?."motion${mySched}UseMotionSettings" = true
					atomicState?."motion${mySched}TurnedOnDt" = getDtNow()
					disableOverrideTemps()
					LogAction("setTstatTempCheck: Enabled Use of Motion Settings for schedule ${mySched}", "info", true)
				} else {
					atomicState."${sLbl}MotionActiveDt" = null		// this will clear isBtwn
					atomicState?."motion${mySched}LastisBtwn" = false
					LogAction("setTstatTempCheck: Motion Sensors were NOT Active at Transition Time to Motion ON for Schedule (${mySched} - ${getSchedLbl(mySched)})", "info", true)
				}
			}

			def samemotion = previousBtwn == isBtwn ? true : false
			def schedMatch = (samesched && samemotion) ? true : false

			def strv = "Using "
			if(schedMatch) { strv = "" }
			LogAction("setTstatTempCheck: ${strv}Schedule ${mySched} (${previousSched}) use Motion settings: ${atomicState?."motion${mySched}UseMotionSettings"} | isBtwn: $isBtwn | previousBtwn: $previousBtwn | motionOn $motionOn", "trace", true)

			if(tstat && !schedMatch) {
				def hvacSettings = atomicState?."sched${mySched}restrictions"
				def useMotion = atomicState?."motion${mySched}UseMotionSettings"

				def newHvacMode = (!useMotion ? hvacSettings?.hvacm : (hvacSettings?.mhvacm ?: hvacSettings?.hvacm))
				if(newHvacMode && (newHvacMode.toString() != curMode)) {

					if(newHvacMode == "rtnFromEco") {
						if(curMode == "eco") {
							def t0 = tstat?.currentpreviousthermostatMode?.toString()
							if(t0) {
								newHvacMode = t0
							}
						} else {
							newHvacMode = curMode
						}
						LogAction("setTstatTempCheck: New Mode is rtnFromEco; Setting Thermostat Mode to (${strCapitalize(newHvacMode)})", "info", true)
					}

					if(newHvacMode && (newHvacMode.toString() != curMode)) {
						if(setTstatMode(schMotTstat, newHvacMode, pName)) {
							storeLastAction("Set ${tstat} Mode to ${strCapitalize(newHvacMode)}", getDtNow(), pName, tstat)
							LogAction("setTstatTempCheck: Setting ${tstat} Thermostat Mode to (${strCapitalize(newHvacMode)})", "info", true)
						} else { LogAction("setTstatTempCheck: Error Setting ${tstat} Thermostat Mode to (${strCapitalize(newHvacMode)})", "warn", true) }
						if(tstatMir) {
							if(setMultipleTstatMode(tstatMir, newHvacMode, pName)) {
							LogAction("Mirroring (${newHvacMode}) to ${tstatMir}", "info", true)
							}
						}
					}
				}

				curMode = tstat?.currentnestThermostatMode?.toString()

				// if remote sensor is on, let it handle temp changes (above took care of a mode change)
				if(settings?.schMotRemoteSensor && isRemSenConfigured()) {
					atomicState.lastSched = mySched
					atomicState?."${pName}lastMode" = curMode
					storeExecutionHistory((now() - execTime), "setTstatTempCheck")
					return
				}

				def isModeOff = (curMode in ["off","eco"]) ? true : false
				def tstatHvacMode = curMode

				def heatTemp = null
				def coolTemp = null
				def needChg = false

				if(!isModeOff && atomicState?.schMotTstatCanHeat) {
					def oldHeat = getTstatSetpoint(tstat, "heat")
					heatTemp = getRemSenHeatSetTemp(curMode)
					if(heatTemp && oldHeat != heatTemp) {
						needChg = true
						LogAction("setTstatTempCheck: Schedule Heat Setpoint '${heatTemp}${tUnitStr()}' on (${tstat}) | Old Setpoint: '${oldHeat}${tUnitStr()}'", "info", false)
						//storeLastAction("Set ${settings?.schMotTstat} Heat Setpoint to ${heatTemp}", getDtNow(), pName, tstat)
					} else { heatTemp = null }
				}

				if(!isModeOff && atomicState?.schMotTstatCanCool) {
					def oldCool = getTstatSetpoint(tstat, "cool")
					coolTemp = getRemSenCoolSetTemp(curMode)
					if(coolTemp && oldCool != coolTemp) {
						needChg = true
						LogAction("setTstatTempCheck: Schedule Cool Setpoint '${coolTemp}${tUnitStr()}' on (${tstat}) | Old Setpoint: '${oldCool}${tUnitStr()}'", "info", false)
						//storeLastAction("Set ${settings?.schMotTstat} Cool Setpoint to ${coolTemp}", getDtNow(), pName, tstat)
					} else { coolTemp = null }
				}
				if(needChg) {
					if(setTstatAutoTemps(settings?.schMotTstat, coolTemp?.toDouble(), heatTemp?.toDouble(), pName, tstatMir)) {
						//LogAction("setTstatTempCheck: [Temp Change | newHvacMode: $newHvacMode | tstatHvacMode: $tstatHvacMode | heatTemp: $heatTemp | coolTemp: $coolTemp ]", "info", true)
						//storeLastAction("Set ${tstat} Cool Setpoint ${coolTemp} Heat Setpoint ${heatTemp}", getDtNow(), pName, tstat)
					} else {
						LogAction("setTstatTempCheck: Thermostat Set ERROR [ newHvacMode: $newHvacMode | tstatHvacMode: $tstatHvacMode | heatTemp: ${heatTemp}${tUnitStr()} | coolTemp: ${coolTemp}${tUnitStr()} ]", "info", true)
					}
				}
			}
		}
		atomicState.lastSched = mySched
		atomicState?."${pName}lastMode" = curMode
		storeExecutionHistory((now() - execTime), "setTstatTempCheck")
	} catch (ex) {
		log.error "setTstatTempCheck Exception:", ex
		parent?.sendExceptionData(ex, "setTstatTempCheck", true, getAutoType())
	}
}

/********************************************************************************
|       				MASTER AUTOMATION FOR THERMOSTATS						|
*********************************************************************************/
def schMotPrefix() { return "schMot" }

def schMotModePage() {
	//def pName = schMotPrefix()
	dynamicPage(name: "schMotModePage", title: "Thermostat Automation", uninstall: false) {
		def dupTstat
		def dupTstat1
		def dupTstat2
		def dupTstat3
		def tStatPhys
		def tempScale = getTemperatureScale()
		def tempScaleStr = "°${tempScale}"
		section("Configure Thermostat") {
			input name: "schMotTstat", type: "capability.thermostat", title: "Select Thermostat?", multiple: false, submitOnChange: true, required: true, image: getAppImg("thermostat_icon.png")
			//log.debug "schMotTstat: ${schMotTstat}"
			def tstat = settings?.schMotTstat
			def tstatMir = settings?.schMotTstatMir
			if(tstat) {
				getTstatCapabilities(tstat, schMotPrefix())
				def canHeat = atomicState?.schMotTstatCanHeat
				def canCool = atomicState?.schMotTstatCanCool
				tStatPhys = tstat?.currentNestType == "physical" ? true : false

				def str = ""
				def reqSenHeatSetPoint = getRemSenHeatSetTemp()
				def reqSenCoolSetPoint = getRemSenCoolSetTemp()
				def curZoneTemp = getRemoteSenTemp()
				def tempSrcStr = (getCurrentSchedule() && atomicState?.remoteTempSourceStr == "Schedule") ? "Schedule ${getCurrentSchedule()} (${"${getSchedLbl(getCurrentSchedule())}" ?: "Not Found"})" : "(${atomicState?.remoteTempSourceStr})"

				str += tempSrcStr ? "Zone Status:\n• Temp Source:${tempSrcStr?.toString().length() > 15 ? "\n  └" : ""} ${tempSrcStr}" : ""
				str += curZoneTemp ? "\n• Temperature: (${curZoneTemp}°${getTemperatureScale()})" : ""

				def hstr = canHeat ? "H: ${reqSenHeatSetPoint}°${getTemperatureScale()}" : ""
				def cstr = canHeat && canCool ? "/" : ""
				cstr += canCool ? "C: ${reqSenCoolSetPoint}°${getTemperatureScale()}" : ""
				str += "\n• Setpoints: (${hstr}${cstr})\n"

				str += "\nThermostat Status:\n• Temperature: (${getDeviceTemp(tstat)}${tempScaleStr})"
				hstr = canHeat ? "H: ${getTstatSetpoint(tstat, "heat")}${tempScaleStr}" : ""
				cstr = canHeat && canCool ? "/" : ""
				cstr += canCool ? "C: ${getTstatSetpoint(tstat, "cool")}${tempScaleStr}" : ""
				str += "\n• Setpoints: (${hstr}${cstr})"

				str += "\n• Mode: (${tstat ? ("${strCapitalize(tstat?.currentThermostatOperatingState)}/${strCapitalize(tstat?.currentnestThermostatMode)}") : "unknown"})"
				str += (atomicState?.schMotTstatHasFan) ? "\n• FanMode: (${strCapitalize(tstat?.currentThermostatFanMode)})" : "\n• No Fan on HVAC system"
				str += "\n• Presence: (${strCapitalize(getTstatPresence(tstat))})"
				def safetyTemps = getSafetyTemps(tstat)
					str += safetyTemps ? "\n• Safety Temps:\n  └ Min: ${safetyTemps.min}°${getTemperatureScale()}/Max: ${safetyTemps.max}${tempScaleStr}" : ""
					str += "\n• Virtual: (${tstat?.currentNestType.toString() == "virtual" ? "True" : "False"})"
				paragraph "${str}", title: "${tstat.displayName} Zone Status", state: (str != "" ? "complete" : null), image: getAppImg("info_icon2.png")

				if(!tStatPhys) {      // if virtual thermostat, check if physical thermostat is in mirror list
					def mylist = [ deviceNetworkId:"${tstat.deviceNetworkId.toString().replaceFirst("v", "")}" ]
					dupTstat1 = checkThermostatDupe(mylist, tstatMir)
					if(dupTstat1) {
						paragraph "ERROR:\nThe Virtual version of the Primary Thermostat was found in Mirror Thermostat List.\nPlease Correct to Proceed", required: true, state: null, image: getAppImg("error_icon.png")
					}
				} else {	      // if physcial thermostat, see if virtual version is in mirror list
					def mylist = [ deviceNetworkId:"v${tstat.deviceNetworkId.toString()}" ]
					dupTstat2 = checkThermostatDupe(mylist, tstatMir)
					if(dupTstat2) {
						paragraph "ERROR:\nThe Virtual version of the Primary Thermostat was found in Mirror Thermostat List.\nPlease Correct to Proceed", required: true, state: null, image: getAppImg("error_icon.png")
					}
				}
				dupTstat3 = checkThermostatDupe(tstat, tstatMir)  // make sure thermostat is not in mirror list
				dupTstat = dupTstat1 || dupTstat2 || dupTstat3

				if(dupTstat) {
					paragraph "ERROR:\nThe Primary Thermostat was also found in the Mirror Thermostat List.\nPlease Correct to Proceed", required: true, state: null, image: getAppImg("error_icon.png")
				}
				if(!tStatPhys) {
				}
				input "schMotTstatMir", "capability.thermostat", title: "Mirror Changes to these Thermostats", multiple: true, submitOnChange: true, required: false, image: getAppImg("thermostat_icon.png")
				if(tstatMir && !dupTstat) {
					tstatMir?.each { t ->
						paragraph "Thermostat Temp: ${getDeviceTemp(t)}${tempScaleStr}", image: " "
					}
				}
			}
		}

		if(settings?.schMotTstat && !dupTstat) {
			updateScheduleStateMap()
			section {
				paragraph "The options below allow you to configure a thermostat with automations that will help save energy and maintain comfort", title: "Choose Automations:", required: false
			}

			section("Schedule Automation:") {
				def actSch = atomicState?.activeSchedData?.size()
				def tDesc = (isTstatSchedConfigured() || atomicState?.activeSchedData?.size()) ? "Tap to modify Schedules" : null
				href "tstatConfigAutoPage", title: "Use Schedules to adjust Temp Setpoints and HVAC mode?", description: (tDesc != null ? tDesc : ""), params: ["configType":"tstatSch"], state: (tDesc != null ? "complete" : ""), image: getAppImg("schedule_icon.png")
				if (actSch) {
					def schInfo = getScheduleDesc()
					def curSch = getCurrentSchedule()
					if (schInfo?.size()) {
						schInfo?.each { schItem ->
							def schNum = schItem?.key
							def schDesc = schItem?.value
							def schInUse = (curSch?.toInteger() == schNum?.toInteger()) ? true : false
							if(schNum && schDesc) {
								href "schMotSchedulePage", title: "", description: "${schDesc}\n\nTap to modify this Schedule", params: ["sNum":schNum], state: (schInUse ? "complete" : "")
							}
						}
					}
				}
			}

			section("Fan Control:") {
				if(tStatPhys || settings?.schMotOperateFan) {
					def desc = ""
					def titStr = ""
					if(atomicState?.schMotTstatHasFan) { titStr += "Use HVAC Fan for Circulation\nor\n" }
					titStr += "Run External Fan while HVAC is Operating"
					input (name: "schMotOperateFan", type: "bool", title: "${titStr}?", description: desc, required: false, defaultValue: false, submitOnChange: true, image: getAppImg("fan_control_icon.png"))
					def fanCtrlDescStr = ""
					def t0 = getFanSwitchDesc()
					if(settings?.schMotOperateFan) {
						fanCtrlDescStr += t0 ? "${t0}" : ""
						def fanCtrlDesc = isFanCtrlConfigured() ? "${fanCtrlDescStr}\n\nTap to modify" : null
						href "tstatConfigAutoPage", title: "Fan Control Config", description: fanCtrlDesc ?: "Not Configured", params: ["configType":"fanCtrl"], state: (fanCtrlDesc ? "complete" : null),
								required: true, image: getAppImg("configure_icon.png")
					}
				} else if(!tStatPhys) {
					paragraph "Fan Control is not available on a VIRTUAL Thermostat", state: "complete", image: getAppImg("info_icon2.png")
				}
				if(!tStatPhys && settings?.schMotOperateFan) { paragraph "ERROR:\nThe Primary Thermostat is VIRTUAL and UNSUPPORTED for Fan Control.\nPlease Correct to Proceed", required: true, state: null, image: getAppImg("error_icon.png") }
			}

			section("Remote Sensor:") {
				if(tStatPhys || settings?.schMotRemoteSensor) {
					def desc = ""
					input (name: "schMotRemoteSensor", type: "bool", title: "Use Alternate Temp Sensors Control Zone temperature?", description: desc, required: false, defaultValue: false, submitOnChange: true,
							image: getAppImg("remote_sensor_icon.png"))
					if(settings?.schMotRemoteSensor) {
						def remSenDescStr = ""
						remSenDescStr += settings?.remSenRuleType ? "Rule-Type: ${getEnumValue(remSenRuleEnum("heatcool"), settings?.remSenRuleType)}" : ""
						remSenDescStr += settings?.remSenTempDiffDegrees ? ("\n • Threshold: (${settings?.remSenTempDiffDegrees}${tempScaleStr})") : ""
						remSenDescStr += settings?.remSenTstatTempChgVal ? ("\n • Adjust Temp: (${settings?.remSenTstatTempChgVal}${tempScaleStr})") : ""

						def hstr = remSenHeatTempsReq() ? "H: ${fixTempSetting(settings?.remSenDayHeatTemp) ?: 0}${tempScaleStr}" : ""
						def cstr = remSenHeatTempsReq() && remSenCoolTempsReq() ? "/" : ""
						cstr += remSenCoolTempsReq() ? "C: ${fixTempSetting(settings?.remSenDayCoolTemp) ?: 0}${tempScaleStr}" : ""
						remSenDescStr += (settings?.remSensorDay && (settings?.remSenDayHeatTemp || settings?.remSenDayCoolTemp)) ? "\n • Default Temps:\n   └ (${hstr}${cstr})" : ""


						remSenDescStr += (settings?.vthermostat) ? "\n\nVirtual Thermostat:" : ""
						remSenDescStr += (settings?.vthermostat) ? "\n• Enabled" : ""

						//remote sensor/Day
						def dayModeDesc = ""
						dayModeDesc += settings?.remSensorDay ? "\n\nDefault Sensor${settings?.remSensorDay?.size() > 1 ? "s" : ""}:" : ""
						def rCnt = settings?.remSensorDay?.size()
						settings?.remSensorDay?.each { t ->
							dayModeDesc += "\n ├ ${t?.label}: ${(t?.label?.toString()?.length() > 10) ? "\n │ └ " : ""}(${getDeviceTemp(t)}${tempScaleStr})"
						}
						dayModeDesc += settings?.remSensorDay ? "\n └ Temp${(settings?.remSensorDay?.size() > 1) ? " (avg):" : ":"} (${getDeviceTempAvg(settings?.remSensorDay)}${tempScaleStr})" : ""
						remSenDescStr += settings?.remSensorDay ? "${dayModeDesc}" : ""

						def remSenDesc = isRemSenConfigured() ? "${remSenDescStr}\n\nTap to modify" : null
						href "tstatConfigAutoPage", title: "Remote Sensor Config", description: remSenDesc ?: "Not Configured", params: ["configType":"remSen"], required: true, state: (remSenDesc ? "complete" : null),
								image: getAppImg("configure_icon.png")
					}
				} else if(!tStatPhys) {	paragraph "Remote Sensor is not available on a VIRTUAL Thermostat", state: "complete", image: getAppImg("info_icon2.png") }

				if(!tStatPhys && settings?.schMotRemoteSensor) {
					paragraph "ERROR:\nThe Primary Thermostat is VIRTUAL and UNSUPPORTED for REMOTE Sensor.\nPlease Correct to Proceed", required: true, state: null, image: getAppImg("error_icon.png")
				}
			}

			section("Leak Detection:") {
				if(tStatPhys || settings?.schMotWaterOff) {
					def desc = ""
					input (name: "schMotWaterOff", type: "bool", title: "Turn Off if Water Leak is detected?", description: desc, required: false, defaultValue: false, submitOnChange: true, image: getAppImg("leak_icon.png"))
					if(settings?.schMotWaterOff) {
						def leakDesc = ""
						def t0 = leakWatSensorsDesc()
						leakDesc += (settings?.leakWatSensors && t0) ? "${t0}" : ""
						leakDesc += settings?.leakWatSensors ? "\n\n${autoStateDesc("leakWat")}" : ""
						leakDesc += (settings?.leakWatSensors) ? "\n\nSettings:" : ""
						leakDesc += settings?.leakWatOnDelay ? "\n • On Delay: (${getEnumValue(longTimeSecEnum(), settings?.leakWatOnDelay)})" : ""
						//leakDesc += (settings?.leakWatModes || settings?.leakWatDays || (settings?.leakWatStartTime && settings?.leakWatStopTime)) ?
							//"\n • Restrictions Active: (${autoScheduleOk(leakWatPrefix()) ? "NO" : "YES"})" : ""
						def t1 = getNotifConfigDesc(leakWatPrefix())
						leakDesc += t1 ? "\n\n${t1}" : ""
						leakDesc += (settings?.leakWatSensors) ? "\n\nTap to modify" : ""
						def leakWatDesc = isLeakWatConfigured() ? "${leakDesc}" : null
						href "tstatConfigAutoPage", title: "Leak Sensor Automation", description: leakWatDesc ?: "Tap to configure", params: ["configType":"leakWat"], required: true, state: (leakWatDesc ? "complete" : null),
								image: getAppImg("configure_icon.png")
					}
				} else if(!tStatPhys) {
					paragraph "Leak Detection is not available on a VIRTUAL Thermostat", state: "complete", image: getAppImg("info_icon2.png")
				}
				if(!tStatPhys && settings?.schMotWaterOff) { paragraph "ERROR:\nThe Primary Thermostat is VIRTUAL and UNSUPPORTED for Leak Detection.\nPlease Correct to Proceed", required: true, state: null, image: getAppImg("error_icon.png") }
			}
			section("Contact Automation:") {
				if(tStatPhys || settings?.schMotContactOff) {
					def desc = ""
					input (name: "schMotContactOff", type: "bool", title: "Set ECO if Door/Window Contact Open?", description: desc, required: false, defaultValue: false, submitOnChange: true, image: getAppImg("open_window.png"))
					if(settings?.schMotContactOff) {
						def conDesc = ""
						def t0 = conWatContactDesc()
						conDesc += (settings?.conWatContacts && t0) ? "${t0}" : ""
						conDesc += settings?.conWatContacts ? "\n\n${autoStateDesc("conWat")}" : ""
						conDesc += settings?.conWatContacts ? "\n\nSettings:" : ""
						conDesc += settings?.conWatOffDelay ? "\n • Eco Delay: (${getEnumValue(longTimeSecEnum(), settings?.conWatOffDelay)})" : ""
						conDesc += settings?.conWatOnDelay ? "\n • On Delay: (${getEnumValue(longTimeSecEnum(), settings?.conWatOnDelay)})" : ""
						conDesc += settings?.conWatRestoreDelayBetween ? "\n • Delay Between Restores:\n   └ (${getEnumValue(longTimeSecEnum(), settings?.conWatRestoreDelayBetween)})" : ""
						conDesc += (settings?.conWatContacts) ? "\n • Restrictions Active: (${autoScheduleOk(conWatPrefix()) ? "NO" : "YES"})" : ""
						def t1 = getNotifConfigDesc(conWatPrefix())
						conDesc += t1 ? "\n\n${t1}" : ""
						conDesc += (settings?.conWatContacts) ? "\n\nTap to modify" : ""
						def conWatDesc = isConWatConfigured() ? "${conDesc}" : null
						href "tstatConfigAutoPage", title: "Contact Sensors Config", description: conWatDesc ?: "Tap to configure", params: ["configType":"conWat"], required: true, state: (conWatDesc ? "complete" : null),
								image: getAppImg("configure_icon.png")
					}
				} else if(!tStatPhys) {
					paragraph "Contact automation is not available on a VIRTUAL Thermostat", state: "complete", image: getAppImg("info_icon2.png")
				}
				if(!tStatPhys && settings?.schMotContactOff) {
					paragraph "ERROR:\nThe Primary Thermostat is VIRTUAL and UNSUPPORTED for Contact automation.\nPlease Correct to Proceed", required: true, state: null, image: getAppImg("error_icon.png")
				}
			}
			section("Humidity Control:") {
				def desc = ""
				input (name: "schMotHumidityControl", type: "bool", title: "Turn Humidifier On / Off?", description: desc, required: false, defaultValue: false, submitOnChange: true, image: getAppImg("humidity_automation_icon.png"))
				if(settings?.schMotHumidityControl) {
					def humDesc = ""
					humDesc += (settings?.humCtrlSwitches) ? "${humCtrlSwitchDesc()}" : ""
					humDesc += (settings?.humCtrlHumidity) ? "${settings?.humCtrlSwitches ? "\n\n" : ""}${humCtrlHumidityDesc()}" : ""
					humDesc += (settings?.humCtrlUseWeather || settings?.humCtrlTempSensor) ? "\n\nSettings:" : ""
					humDesc += (!settings?.humCtrlUseWeather && settings?.humCtrlTempSensor) ? "\n • Temp Sensor: (${getHumCtrlTemperature()}${tempScaleStr})" : ""
					humDesc += (settings?.humCtrlUseWeather && !settings?.humCtrlTempSensor) ? "\n • Weather: (${getHumCtrlTemperature()}${tempScaleStr})" : ""
					humDesc += (settings?.humCtrlSwitches) ?  "\n • Restrictions Active: (${autoScheduleOk(humCtrlPrefix()) ? "NO" : "YES"})" : ""
			//TODO need this in schedule
					humDesc += ((settings?.humCtrlTempSensor || settings?.humCtrlUseWeather) ) ? "\n\nTap to modify" : ""
					def humCtrlDesc = isHumCtrlConfigured() ? "${humDesc}" : null
					href "tstatConfigAutoPage", title: "Humidifier Config", description: humCtrlDesc ?: "Tap to configure", params: ["configType":"humCtrl"], required: true, state: (humCtrlDesc ? "complete" : null),
							image: getAppImg("configure_icon.png")
				}
			}
			section("External Temp:") {
				if(tStatPhys || settings?.schMotExternalTempOff) {
					def desc = ""
					input (name: "schMotExternalTempOff", type: "bool", title: "Set ECO if External Temp is near comfort settings?", description: desc, required: false, defaultValue: false, submitOnChange: true, image: getAppImg("external_temp_icon.png"))
					if(settings?.schMotExternalTempOff) {
						def extDesc = ""
						extDesc += (settings?.extTmpUseWeather || settings?.extTmpTempSensor) ? "${autoStateDesc("extTmp")}\n\n" : ""
						extDesc += (settings?.extTmpUseWeather || settings?.extTmpTempSensor) ? "Settings:" : ""
						extDesc += (!settings?.extTmpUseWeather && settings?.extTmpTempSensor) ? "\n • Sensor: (${getExtTmpTemperature()}${tempScaleStr})" : ""
						extDesc += (settings?.extTmpUseWeather && !settings?.extTmpTempSensor) ? "\n • Weather: (${getExtTmpTemperature()}${tempScaleStr})" : ""
					//TODO need this in schedule
						extDesc += settings?.extTmpDiffVal ? "\n • Outside Threshold: (${settings?.extTmpDiffVal}${tempScaleStr})" : ""
						extDesc += settings?.extTmpInsideDiffVal ? "\n • Inside Threshold: (${settings?.extTmpInsideDiffVal}${tempScaleStr})" : ""
						extDesc += settings?.extTmpOffDelay ? "\n • ECO Delay: (${getEnumValue(longTimeSecEnum(), settings?.extTmpOffDelay)})" : ""
						extDesc += settings?.extTmpOnDelay ? "\n • On Delay: (${getEnumValue(longTimeSecEnum(), settings?.extTmpOnDelay)})" : ""
						extDesc += (settings?.extTmpTempSensor || settings?.extTmpUseWeather) ? "\n • Restrictions Active: (${autoScheduleOk(extTmpPrefix()) ? "NO" : "YES"})" : ""
						def t0 = getNotifConfigDesc(extTmpPrefix())
						extDesc += t0 ? "\n\n${t0}" : ""
						extDesc += ((settings?.extTmpTempSensor || settings?.extTmpUseWeather) ) ? "\n\nTap to modify" : ""
						def extTmpDesc = isExtTmpConfigured() ? "${extDesc}" : null
						href "tstatConfigAutoPage", title: "External Temps Config", description: extTmpDesc ?: "Tap to configure", params: ["configType":"extTmp"], required: true, state: (extTmpDesc ? "complete" : null),
								image: getAppImg("configure_icon.png")
					}
				} else if(!tStatPhys) {
					paragraph "External Temp automation is not available on a VIRTUAL Thermostat", state: "complete", image: getAppImg("info_icon2.png")
				}
				if(!tStatPhys && settings?.schMotExternalTempOff) {
					paragraph "ERROR:\nThe Primary Thermostat is VIRTUAL and UNSUPPORTED for External Temp automation.\nPlease Correct to Proceed", required: true, state: null, image: getAppImg("error_icon.png")
				}
			}

			section("Settings:") {
				input "schMotWaitVal", "enum", title: "Minimum Wait Time between Evaluations?", required: false, defaultValue: 60, metadata: [values:[30:"30 Seconds", 60:"60 Seconds",90:"90 Seconds",120:"120 Seconds"]], image: getAppImg("delay_time_icon.png")
			}
		}
		if(atomicState?.showHelp) {
			section("Help:") {
				href url:"${getAutoHelpPageUrl()}", style:"embedded", required:false, title:"Help and Instructions", description:"", image: getAppImg("info.png")
			}
		}
	}
}

def getSchedLbl(num) {
	def result = ""
	if(num) {
		def schData = atomicState?.activeSchedData
		schData?.each { sch ->
			if(num?.toInteger() == sch?.key.toInteger()) {
				//log.debug "Label:(${sch?.value?.lbl})"
				result = sch?.value?.lbl
			}
		}
	}
	return result
}

def getSchedData(num) {
	if(!num) { return null }
	def resData = [:]
	def schData = atomicState?.activeSchedData
	schData?.each { sch ->
		//log.debug "sch: $sch"
		if(sch?.key != null && num?.toInteger() == sch?.key.toInteger()) {
			// log.debug "Data:(${sch?.value})"
			resData = sch?.value
		}
	}
	return resData != [:] ? resData : null
}

/* NOTE
	Schedule Rules:
	You ALWAYS HAVE TEMPS in A SCHEDULE
	• You ALWAYS OFFER OPTION OF MOTION TEMPS in A SCHEDULE
	• If Motion is ENABLED, it MUST HAVE MOTION TEMPS
	• You ALWAYS OFFER RESTRICTION OPTIONS in A SCHEDULE
	• If REMSEN is ON, you offer remote sensors options
*/

def tstatConfigAutoPage(params) {
	def configType = params.configType
	if(params?.configType) {
		atomicState.tempTstatConfigPageData = params; configType = params?.configType;
	} else { configType = atomicState?.tempTstatConfigPageData?.configType }
	def pName = ""
	def pTitle = ""
	def pDesc = null
	switch(configType) {
		case "tstatSch":
			pName = schMotPrefix()
			pTitle = "Thermostat Schedule Automation"
			pDesc = "Configure Schedules and Setpoints"
			break
		case "fanCtrl":
			pName = fanCtrlPrefix()
			pTitle = "Fan Automation"
			break
		case "remSen":
			pName = remSenPrefix()
			pTitle = "Remote Sensor Automation"
			break
		case "leakWat":
			pName = leakWatPrefix()
			pTitle = "Thermostat/Leak Automation"
			break
		case "conWat":
			pName = conWatPrefix()
			pTitle = "Thermostat/Contact Automation"
			break
		case "humCtrl":
			pName = humCtrlPrefix()
			pTitle = "Humidifier Automation"
			break
		case "extTmp":
			pName = extTmpPrefix()
			pTitle = "Thermostat/External Temps Automation"
			break
	}
	dynamicPage(name: "tstatConfigAutoPage", title: pTitle, description: pDesc, uninstall: false) {
		def tstat = settings?.schMotTstat
		if (tstat) {
			def tempScale = getTemperatureScale()
			def tempScaleStr = "°${tempScale}"
			def tStatName = tstat?.displayName.toString()
			def tStatHeatSp = getTstatSetpoint(tstat, "heat")
			def tStatCoolSp = getTstatSetpoint(tstat, "cool")
			def tStatMode = tstat?.currentThermostatMode
			def tStatTemp = "${getDeviceTemp(tstat)}${tempScaleStr}"
			def canHeat = atomicState?.schMotTstatCanHeat
			def canCool = atomicState?.schMotTstatCanCool
			def locMode = location?.mode

			def hidestr = null
			hidestr = ["fanCtrl"]   // fan schedule is turned off
			if(!settings?.schMotRemoteSensor) { // no remote sensors requested or used
				hidestr = ["fanCtrl", "remSen"]
			}
			if(!settings?.schMotOperateFan) {

			}
			//if(!settings?.schMotSetTstatTemp) {   //motSen means no motion sensors offered   restrict means no restrictions offered  tstatTemp says no tstat temps offered
				//"tstatTemp", "motSen" "restrict"
			//}
			if(!settings?.schMotExternalTempOff) {
			}

			if(configType == "tstatSch") {
				section {
					def str = ""
					str += "• Temperature: (${tStatTemp})"
					str += "\n• Setpoints: (H: ${canHeat ? "${tStatHeatSp}${tempScaleStr}" : "NA"}/C: ${canCool ? "${tStatCoolSp}${tempScaleStr}" : "NA"})" //
					paragraph title: "${tStatName}\nSchedules and Setpoints:", "${str}", state: "complete", image: getAppImg("info_icon2.png")
				}
				showUpdateSchedule(null, hidestr)
			}

			if(configType == "fanCtrl") {
				def reqinp = !(settings["schMotCirculateTstatFan"] || settings["${pName}FanSwitches"])
				section("Control Fans/Switches based on Thermostat\n(3-Speed Fans Supported)") {
					input "${pName}FanSwitches", "capability.switch", title: "Select Fan Switches?", required: reqinp, submitOnChange: true, multiple: true,
							image: getAppImg("fan_ventilation_icon.png")
					if(settings?."${pName}FanSwitches") {
						def t0 = getFanSwitchDesc(false)
						paragraph "${t0}", state: t0 ? "complete" : null, image: getAppImg("blank_icon.png")
					}
				}
				if(settings["${pName}FanSwitches"]) {
					section("Fan Event Triggers") {
						paragraph "Triggers are evaluated when Thermostat sends an operating event.  Poll time may take 1 minute or more for fan to switch on.",
								title: "What are these triggers?", image: getAppImg("instruct_icon.png")
						input "${pName}FanSwitchTriggerType", "enum", title: "Control Switches When?", defaultValue: 1, metadata: [values:switchRunEnum()],
							submitOnChange: true, image: getAppImg("${settings?."${pName}FanSwitchTriggerType" == 1 ? "thermostat" : "home_fan"}_icon.png")
						input "${pName}FanSwitchHvacModeFilter", "enum", title: "Thermostat Mode Triggers?", defaultValue: "any", metadata: [values:fanModeTrigEnum()],
								submitOnChange: true, multiple: true, image: getAppImg("mode_icon.png")
					}
					if(getFanSwitchesSpdChk()) {
						section("Fan Speed Options") {
							input "${pName}FanSwitchSpeedCtrl", "bool", title: "Enable Speed Control?", defaultValue: true, submitOnChange: true, image: getAppImg("speed_knob_icon.png")
							if(settings["${pName}FanSwitchSpeedCtrl"]) {
								paragraph "These threshold settings allow you to configure the speed of the fan based on it's closeness to the desired temp", title: "What do these values mean?"
								input "${pName}FanSwitchLowSpeed", "decimal", title: "Low Speed Threshold (${tempScaleStr})", required: true, defaultValue: 1.0, submitOnChange: true, image: getAppImg("fan_low_speed.png")
								input "${pName}FanSwitchMedSpeed", "decimal", title: "Medium Speed Threshold (${tempScaleStr})", required: true, defaultValue: 2.0, submitOnChange: true, image: getAppImg("fan_med_speed.png")
								input "${pName}FanSwitchHighSpeed", "decimal", title: "High Speed Threshold (${tempScaleStr})", required: true, defaultValue: 4.0, submitOnChange: true, image: getAppImg("fan_high_speed.png")
							}
						}
					}
				}
				if(atomicState?.schMotTstatHasFan) {
					section("Fan Circulation:") {
						def desc = ""
						input (name: "schMotCirculateTstatFan", type: "bool", title: "Run HVAC Fan for Circulation?", description: desc, required: reqinp, defaultValue: false, submitOnChange: true, image: getAppImg("fan_circulation_icon.png"))
						if(settings?.schMotCirculateTstatFan) {
							input("schMotFanRuleType", "enum", title: "(Rule) Action Type", options: remSenRuleEnum("fan"), required: true, image: getAppImg("rule_icon.png"))
							paragraph "Temp difference to trigger Action Type.", title: "What is the Action Threshold Temp?", image: getAppImg("instruct_icon.png")
							def adjust = (getTemperatureScale() == "C") ? 0.5 : 1.0
							input "fanCtrlTempDiffDegrees", "decimal", title: "Action Threshold Temp (${tempScaleStr})", required: true, defaultValue: adjust, image: getAppImg("temp_icon.png")
							input name: "fanCtrlOnTime", type: "enum", title: "Minimum circulate Time\n(Optional)", defaultValue: 240, metadata: [values:fanTimeSecEnum()], required: true, submitOnChange: true, image: getAppImg("timer_icon.png")
							input name: "fanCtrlTimeBetweenRuns", type: "enum", title: "Delay Between On/Off Cycles\n(Optional)", defaultValue: 1200, metadata: [values:longTimeSecEnum()], required: true, submitOnChange: true, image: getAppImg("delay_time_icon.png")
						}
					}
				}
				section(getDmtSectionDesc(fanCtrlPrefix())) {
					def pageDesc = getDayModeTimeDesc(pName)
					href "setDayModeTimePage", title: "Configured Restrictions", description: pageDesc, params: ["pName": "${pName}"], state: (pageDesc ? "complete" : null),
							image: getAppImg("cal_filter_icon.png")
				}

				if(settings?."${pName}FanSwitches") {
					def schTitle
					if(!atomicState?.activeSchedData?.size()) {
						schTitle = "Optionally create schedules to set temperatures based on schedule"
					} else {
						schTitle = "Temperature settings based on schedule"
					}
					section("${schTitle}") { // FANS USE TEMPS IN LOGIC
						href "scheduleConfigPage", title: "Enable/Modify Schedules", description: pageDesc, params: ["sData":["hideStr":"${hideStr}"]], state: (pageDesc ? "complete" : null), image: getAppImg("schedule_icon.png")
					}
				}
			}

			def cannotLock
			def defHeat
			def defCool
			if(!getMyLockId()) {
				setMyLockId(app.id)
			}
			if(atomicState?.remSenTstat) {
				if(tstat.deviceNetworkId != atomicState?.remSenTstat) {
					parent?.addRemoveVthermostat(atomicState.remSenTstat, false, getMyLockId())
					if( parent?.remSenUnlock(atomicState.remSenTstat, getMyLockId()) ) { // attempt unlock old ID
						atomicState.oldremSenTstat = atomicState?.remSenTstat
						atomicState?.remSenTstat = null
					}
				}
			}
			if(settings?.schMotRemoteSensor) {
				if( parent?.remSenLock(tstat?.deviceNetworkId, getMyLockId()) ) {  // lock new ID
					atomicState?.remSenTstat = tstat?.deviceNetworkId
					cannotLock = false
				} else { cannotLock = true }
			}

			if(configType == "remSen") {
				//   can check if any vthermostat is owned by us, and delete it
				//   have issue request for vthermostat is still on as input below

				if(cannotLock) {
					section("") {
						paragraph "Cannot Lock thermostat for remote sensor - thermostat may already be in use.  Please Correct", required: true, state: null, image: getAppImg("error_icon.png")
					}
				}

				if(!cannotLock) {
					section("Select the Allowed (Rule) Action Type:") {
						if(!settings?.remSenRuleType) {
							paragraph "They determine the actions taken when the temperature threshold is reached, to balance temperatures", title: "What are Rule Actions?", image: getAppImg("instruct_icon.png")
						}
						input(name: "remSenRuleType", type: "enum", title: "(Rule) Action Type", options: remSenRuleEnum("heatcool"), required: true, submitOnChange: true, image: getAppImg("rule_icon.png"))
					}
					if(settings?.remSenRuleType) {
						def senLblStr = "Default"
						section("Choose Temperature Sensor(s) to use:") {
							def daySenReq = (!settings?.remSensorDay) ? true : false
							input "remSensorDay", "capability.temperatureMeasurement", title: "${senLblStr} Temp Sensor(s)", submitOnChange: true, required: daySenReq,
									multiple: true, image: getAppImg("temperature_icon.png")
							if(settings?.remSensorDay) {
								def tmpVal = "Temp${(settings?.remSensorDay?.size() > 1) ? " (avg):" : ":"} (${getDeviceTempAvg(settings?.remSensorDay)}${tempScaleStr})"
								if(settings?.remSensorDay.size() > 1) {
									href "remSenShowTempsPage", title: "View ${senLblStr} Sensor Temps", description: "${tmpVal}", state: "complete", image: getAppImg("blank_icon.png")
									//paragraph "Multiple temp sensors will return the average of those sensors.", image: getAppImg("i_icon.png")
								} else { paragraph "${tmpVal}", title: "Remote Sensor Temp", state: "complete", image: getAppImg("instruct_icon.png") }
							}
						}
						if(settings?.remSensorDay) {
							section("Desired Setpoints") {
								paragraph "These temps are used when remote sensors are enabled and no schedules are created or active", title: "What are these temps for?", image: getAppImg("info_icon2.png")
								if(isTstatSchedConfigured()) {
//								if(settings?.schMotSetTstatTemp) {
									paragraph "If schedules are enabled and that schedule is in use it's setpoints will take precendence over the setpoints below", required: true, state: null
								}
								def tempStr = "Default "
								if(remSenHeatTempsReq()) {
									defHeat = fixTempSetting(getGlobalDesiredHeatTemp())
									defHeat = defHeat ?: tStatHeatSp
									input "remSenDayHeatTemp", "decimal", title: "Desired ${tempStr}Heat Temp (${tempScaleStr})", description: "Range within ${tempRangeValues()}", range: tempRangeValues(),
											required: true, defaultValue: defHeat, image: getAppImg("heat_icon.png")
								}
								if(remSenCoolTempsReq()) {
									defCool = fixTempSetting(getGlobalDesiredCoolTemp())
									defCool = defCool ?: tStatCoolSp
									input "remSenDayCoolTemp", "decimal", title: "Desired ${tempStr}Cool Temp (${tempScaleStr})", description: "Range within ${tempRangeValues()}", range: tempRangeValues(),
											required: true, defaultValue: defCool, image: getAppImg("cool_icon.png")
								}
							}
							section("Remote Sensor Settings") {
								paragraph "Temp difference to trigger Action Type.", title: "What is the Action Threshold Temp?", image: getAppImg("instruct_icon.png")
								input "remSenTempDiffDegrees", "decimal", title: "Action Threshold Temp (${tempScaleStr})", required: true, defaultValue: 2.0, image: getAppImg("temp_icon.png")
								if(settings?.remSenRuleType != "Circ") {
									paragraph "Is the amount the thermostat temp is adjusted +/- to enable the HVAC system.", title: "What are Temp Increments?", image: getAppImg("instruct_icon.png")
									input "remSenTstatTempChgVal", "decimal", title: "Change Temp Increments (${tempScaleStr})", required: true, defaultValue: 5.0, image: getAppImg("temp_icon.png")
								}
							}

							section("(Optional) Create a Virtual Nest Thermostat:") {
								input(name: "vthermostat", type: "bool", title:"Create Virtual Nest Thermostat", required: false, submitOnChange: true, image: getAppImg("thermostat_icon.png"))
								if(settings?.vthermostat != null && !parent?.addRemoveVthermostat(tstat.deviceNetworkId, vthermostat, getMyLockId())) {
									paragraph "Unable to ${(vthermostat ? "enable" : "disable")} Virtual Thermostat!. Please Correct", image: getAppImg("error_icon.png")
								}
							}

							def schTitle
							if(!atomicState?.activeSchedData?.size()) {
								schTitle = "Optionally create schedules to set temperatures, alternate sensors based on schedule"
							} else {
								schTitle = "Temperature settings and optionally alternate sensors based on schedule"
							}
							section("${schTitle}") {
								href "scheduleConfigPage", title: "Enable/Modify Schedules", description: pageDesc, params: ["sData":["hideStr":"${hideStr}"]], state: (pageDesc ? "complete" : null), image: getAppImg("schedule_icon.png")
							}
						}
					}
				}
			}

			if(configType == "leakWat") {
				section("When Leak is Detected, Turn Off this Thermostat") {
					def req = (settings?.leakWatSensors || settings?.schMotTstat) ? true : false
					input name: "leakWatSensors", type: "capability.waterSensor", title: "Which Leak Sensor(s)?", multiple: true, submitOnChange: true, required: req,
							image: getAppImg("water_icon.png")
					if(settings?.leakWatSensors) {
						paragraph "${leakWatSensorsDesc()}", state: "complete", image: getAppImg("instruct_icon.png")
					}
				}
				if(settings?.leakWatSensors) {
					section("Restore On when Dry:") {
						input name: "leakWatOnDelay", type: "enum", title: "Delay Restore (in minutes)", defaultValue: 300, metadata: [values:longTimeSecEnum()], required: false, submitOnChange: true,
								image: getAppImg("delay_time_icon.png")
					}
					section("Notifications:") {
						def pageDesc = getNotifConfigDesc(pName)
						href "setNotificationPage", title: "Configured Alerts", description: pageDesc, params: ["pName":"${pName}", "allowSpeech":true, "allowAlarm":true, "showSchedule":true],
								state: (pageDesc ? "complete" : null), image: getAppImg("notification_icon.png")
					}
				}
			}

			if(configType == "conWat") {
				section("When these Contacts are open, Set this Thermostat to ECO") {
					def req = !settings?.conWatContacts ? true : false
					input name: "conWatContacts", type: "capability.contactSensor", title: "Which Contact(s)?", multiple: true, submitOnChange: true, required: req,
							image: getAppImg("contact_icon.png")
					if(settings?.conWatContacts) {
						def str = ""
						str += settings?.conWatContacts ? "${conWatContactDesc()}\n" : ""
						paragraph "${str}", state: (str != "" ? "complete" : null), image: getAppImg("instruct_icon.png")
					}
				}
				if(settings?.conWatContacts) {
					section("Trigger Actions:") {
						input name: "conWatOffDelay", type: "enum", title: "Delay to set ECO (in Minutes)", defaultValue: 300, metadata: [values:longTimeSecEnum()], required: false, submitOnChange: true,
								image: getAppImg("delay_time_icon.png")

						input name: "conWatOnDelay", type: "enum", title: "Delay Restore (in Minutes)", defaultValue: 300, metadata: [values:longTimeSecEnum()], required: false, submitOnChange: true,
								image: getAppImg("delay_time_icon.png")
						input name: "conWatRestoreDelayBetween", type: "enum", title: "Delay Between On/ECO Cycles\n(Optional)", defaultValue: 600, metadata: [values:longTimeSecEnum()], required: false, submitOnChange: true,
								image: getAppImg("delay_time_icon.png")
					}
					section("Restoration Preferences (Optional):") {
						input "${pName}OffTimeout", "enum", title: "Auto Restore after", defaultValue: 0, metadata: [values:longTimeSecEnum()], required: false, submitOnChange: true,
								image: getAppImg("delay_time_icon.png")
						if(!settings?."${pName}OffTimeout") { atomicState."${pName}timeOutScheduled" = false }
					}

					section(getDmtSectionDesc(conWatPrefix())) {
						def pageDesc = getDayModeTimeDesc(pName)
						href "setDayModeTimePage", title: "Configured Restrictions", description: pageDesc, params: ["pName": "${pName}"], state: (pageDesc ? "complete" : null),
								image: getAppImg("cal_filter_icon.png")
					}
					section("Notifications:") {
						def pageDesc = getNotifConfigDesc(pName)
						href "setNotificationPage", title: "Configured Alerts", description: pageDesc, params: ["pName":"${pName}", "allowSpeech":true, "allowAlarm":true, "showSchedule":true],
								state: (pageDesc ? "complete" : null), image: getAppImg("notification_icon.png")
					}
				}
			}

			if(configType == "humCtrl") {
				section("Switch for Humidifier") {
					def reqinp = !(settings?.humCtrlSwitches)
// TODO needs new icon
					input "humCtrlSwitches", "capability.switch", title: "Select Switches?", required: reqinp, submitOnChange: true, multiple: true,
							image: getAppImg("fan_ventilation_icon.png")

/*
this does not work...
*/
					def t00 = settings[humCtrlSwitches].collect { it?.getId() }
					def t1 = atomicState?.oldhumCtrlSwitches
					def t2 = t1.collect { it?.getId() }
					if(t2 != t00) {
						atomicState.haveRunHumidifier = false
						if(settings?.humCtrlSwitches) { humCtrlSwitches*.off() }
						if(t1) { t1*.off() }
						atomicState?.oldhumCtrlSwitches = settings?.humCtrlSwitches
						log.warn "found different oldhum vs. humctrlSwitches"
					}

					if(settings?.humCtrlSwitches) {
						def t0 = humCtrlSwitchDesc(false)
						paragraph "${t0}", state: t0 ? "complete" : null, image: getAppImg("blank_icon.png")
					}
				}
				if(settings?.humCtrlSwitches) {
					section("Humidifier Triggers") {
						paragraph "Triggers are evaluated when Thermostat sends an operating event.  Poll time may take 1 minute or more for fan to switch on.",
								title: "What are these triggers?", image: getAppImg("instruct_icon.png")
// TODO needs to fix icon
						input "humCtrlSwitchTriggerType", "enum", title: "Control Switches When?", defaultValue: 5, metadata: [values:switchRunEnum(true)],
								submitOnChange: true, image: getAppImg("${settings?.humCtrlSwitchTriggerType == 1 ? "thermostat" : "home_fan"}_icon.png")
						input "humCtrlSwitchHvacModeFilter", "enum", title: "Thermostat Mode Triggers?", defaultValue: "any", metadata: [values:fanModeTrigEnum()],
								submitOnChange: true, multiple: true, image: getAppImg("mode_icon.png")
					}
					section("Indoor Humidity Measurement") {
						def req = !settings?.humCtrlHumidity ? true : false
						input name: "humCtrlHumidity", type: "capability.relativeHumidityMeasurement", title: "Which Humidity Sensor(s)?", multiple: true, submitOnChange: true, required: req,
								image: getAppImg("humidity_icon.png")
						if(settings?.humCtrlHumidity) {
							def str = ""
							str += settings?.humCtrlHumidity ? "${humCtrlHumidityDesc()}\n" : ""
							paragraph "${str}", state: (str != "" ? "complete" : null), image: getAppImg("instruct_icon.png")
						}
					}
					section("Select the External Temp Sensor to Use:") {
						if(!parent?.getWeatherDeviceInst()) {
							paragraph "Please Enable the Weather Device under the Manager App before trying to use External Weather as the External Temperature Sensor!", required: true, state: null
						} else {
							if(!settings?.humCtrlTempSensor) {
								input "humCtrlUseWeather", "bool", title: "Use Local Weather as External Sensor?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("weather_icon.png")
								atomicState.NeedwUpd = true
								if(settings?.humCtrlUseWeather) {
									if(atomicState?.curWeather == null) {
										getExtConditions()
									}
									def tmpVal = (tempScale == "C") ? atomicState?.curWeatherTemp_c : atomicState?.curWeatherTemp_f
									paragraph "Local Weather:\n• ${atomicState?.curWeatherLoc}\n• Temp: (${tmpVal}${tempScaleStr})", state: "complete", image: getAppImg("instruct_icon.png")
								}
							}
						}
						if(!settings?.humCtrlUseWeather) {
							atomicState.curWeather = null  // force refresh of weather if toggled
							def senReq = (!settings?.humCtrlUseWeather && !settings?.humCtrlTempSensor) ? true : false
							input "humCtrlTempSensor", "capability.temperatureMeasurement", title: "Select a Temp Sensor?", submitOnChange: true, multiple: false, required: senReq, image: getAppImg("temperature_icon.png")
							if(settings?.humCtrlTempSensor) {
								def str = ""
								str += settings?.humCtrlTempSensor ? "Sensor Status:" : ""
								str += settings?.humCtrlTempSensor ? "\n└ Temp: (${settings?.humCtrlTempSensor?.currentTemperature}${tempScaleStr})" : ""
								paragraph "${str}", state: (str != "" ? "complete" : null), image: getAppImg("instruct_icon.png")
							}
						}
					}
					section(getDmtSectionDesc(humCtrlPrefix())) {
						def pageDesc = getDayModeTimeDesc(pName)
						href "setDayModeTimePage", title: "Configured Restrictions", description: pageDesc, params: ["pName": "${pName}"], state: (pageDesc ? "complete" : null),
								image: getAppImg("cal_filter_icon.png")
					}
				}
			}

			if(configType == "extTmp") {
				section("Select the External Temps to Use:") {
					if(!parent?.getWeatherDeviceInst()) {
						paragraph "Please Enable the Weather Device under the Manager App before trying to use External Weather as an External Sensor!", required: true, state: null
					} else {
						if(!settings?.extTmpTempSensor) {
							input "extTmpUseWeather", "bool", title: "Use Local Weather as External Sensor?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("weather_icon.png")
							atomicState.NeedwUpd = true
							if(settings?.extTmpUseWeather) {
								if(atomicState?.curWeather == null) {
									getExtConditions()
								}
								def tmpVal = (tempScale == "C") ? atomicState?.curWeatherTemp_c : atomicState?.curWeatherTemp_f
								def curDp = getExtTmpDewPoint()
								paragraph "Local Weather:\n• ${atomicState?.curWeatherLoc}\n• Temp: (${tmpVal}${tempScaleStr})\n• Dewpoint: (${curDp}${tempScaleStr})", state: "complete", image: getAppImg("instruct_icon.png")
							}
						}
					}
					if(!settings?.extTmpUseWeather) {
						atomicState.curWeather = null  // force refresh of weather if toggled
						def senReq = (!settings?.extTmpUseWeather && !settings?.extTmpTempSensor) ? true : false
						input "extTmpTempSensor", "capability.temperatureMeasurement", title: "Select a Temp Sensor?", submitOnChange: true, multiple: false, required: senReq, image: getAppImg("temperature_icon.png")
						if(settings?.extTmpTempSensor) {
							def str = ""
							str += settings?.extTmpTempSensor ? "Sensor Status:" : ""
							str += settings?.extTmpTempSensor ? "\n└ Temp: (${settings?.extTmpTempSensor?.currentTemperature}${tempScaleStr})" : ""
							paragraph "${str}", state: (str != "" ? "complete" : null), image: getAppImg("instruct_icon.png")
						}
					}
				}
				if(settings?.extTmpUseWeather || settings?.extTmpTempSensor) {
					section("When the threshold Temps are Reached\nSet the Thermostat to ECO") {
						input name: "extTmpDiffVal", type: "decimal", title: "When desired and external temp difference is at least this many degrees (${tempScaleStr})?", defaultValue: 1.0, submitOnChange: true, required: true,
								image: getAppImg("temp_icon.png")
						input name: "extTmpInsideDiffVal", type: "decimal", title: "AND When desired and internal temp difference is within this many degrees (${tempScaleStr})?", defaultValue: getTemperatureScale() == "C" ? 2.0 : 4.0, submitOnChange: true, required: true,
								image: getAppImg("temp_icon.png")
					}
					section("Delay Values:") {
						input name: "extTmpOffDelay", type: "enum", title: "Delay to set ECO (in minutes)", defaultValue: 300, metadata: [values:longTimeSecEnum()], required: false, submitOnChange: true,
								image: getAppImg("delay_time_icon.png")
						input name: "extTmpOnDelay", type: "enum", title: "Delay Restore (in minutes)", defaultValue: 300, metadata: [values:longTimeSecEnum()], required: false, submitOnChange: true,
								image: getAppImg("delay_time_icon.png")
					}
					section("Restoration Preferences (Optional):") {
						input "${pName}OffTimeout", "enum", title: "Auto Restore after (Optional)", defaultValue: 0, metadata: [values:longTimeSecEnum()], required: false, submitOnChange: true,
								image: getAppImg("delay_time_icon.png")
						if(!settings?."${pName}OffTimeout") { atomicState."${pName}timeOutScheduled" = false }
					}
					section(getDmtSectionDesc(extTmpPrefix())) {
						def pageDesc = getDayModeTimeDesc(pName)
						href "setDayModeTimePage", title: "Configured Restrictions", description: pageDesc, params: ["pName": "${pName}"], state: (pageDesc ? "complete" : null),
								image: getAppImg("cal_filter_icon.png")
					}
					section("Notifications:") {
						def pageDesc = getNotifConfigDesc(pName)
						href "setNotificationPage", title: "Configured Alerts", description: pageDesc, params: ["pName":"${pName}", "allowSpeech":true, "allowAlarm":true, "showSchedule":true],
								state: (pageDesc ? "complete" : null), image: getAppImg("notification_icon.png")
					}
					def schTitle
					if(!atomicState?.activeSchedData?.size()) {
						schTitle = "Optionally create schedules to set temperatures based on schedule"
					} else {
						schTitle = "Temperature settings based on schedule"
					}
					section("${schTitle}") { // EXTERNAL TEMPERATURE has TEMP Setting
						href "scheduleConfigPage", title: "Enable/Modify Schedules", description: pageDesc, params: ["sData":["hideStr":"${hideStr}"]], state: (pageDesc ? "complete" : null), image: getAppImg("schedule_icon.png")
					}
				}
			}
		}
	}
}

def scheduleConfigPage(params) {
	//LogTrace("scheduleConfigPage ($params)")
	def sData = params?.sData
	if(params?.sData) {
		atomicState.tempSchPageData = params
		sData = params?.sData
	} else {
		sData = atomicState?.tempSchPageData?.sData
	}
	dynamicPage(name: "scheduleConfigPage", title: "Thermostat Schedule Page", description: "Configure/View Schedules", uninstall: false) {
		if(settings?.schMotTstat) {
			def tstat = settings?.schMotTstat
			def canHeat = atomicState?.schMotTstatCanHeat
			def canCool = atomicState?.schMotTstatCanCool
			def str = ""
			def reqSenHeatSetPoint = getRemSenHeatSetTemp()
			def reqSenCoolSetPoint = getRemSenCoolSetTemp()
			def curZoneTemp = getRemoteSenTemp()
			def tempSrcStr = atomicState?.remoteTempSourceStr
			section {
				str += "Zone Status:\n• Temp Source: (${tempSrcStr})\n• Temperature: (${curZoneTemp}°${getTemperatureScale()})"

				def hstr = canHeat ? "H: ${reqSenHeatSetPoint}°${getTemperatureScale()}" : ""
				def cstr = canHeat && canCool ? "/" : ""
				cstr += canCool ? "C: ${reqSenCoolSetPoint}°${getTemperatureScale()}" : ""
				str += "\n• Setpoints: (${hstr}${cstr})\n"

				str += "\nThermostat Status:\n• Temperature: (${getDeviceTemp(tstat)}°${getTemperatureScale()})"
				hstr = canHeat ? "H: ${getTstatSetpoint(tstat, "heat")}°${getTemperatureScale()}" : ""
				cstr = canHeat && canCool ? "/" : ""
				cstr += canCool ? "C: ${getTstatSetpoint(tstat, "cool")}°${getTemperatureScale()}" : ""
				str += "\n• Setpoints: (${hstr}${cstr})"

				str += "\n• Mode: (${tstat ? ("${strCapitalize(tstat?.currentThermostatOperatingState)}/${strCapitalize(tstat?.currentThermostatMode)}") : "unknown"})"
				str += (atomicState?.schMotTstatHasFan) ? "\n• FanMode: (${strCapitalize(tstat?.currentThermostatFanMode)})" : "\n• No Fan on HVAC system"
				str += "\n• Presence: (${strCapitalize(getTstatPresence(tstat))})"
				paragraph title: "${tstat?.displayName}\nSchedules and Setpoints:", "${str}", state: "complete", image: getAppImg("info_icon2.png")
			}
			showUpdateSchedule(null,sData?.hideStr)
		}
	}
}

def schMotSchedulePage(params) {
	//LogTrace("schMotSchedulePage($params)")
	def sNum = params?.sNum
	if(params?.sNum) {
		atomicState.tempMotSchPageData = params
		sNum = params?.sNum
	} else {
		sNum = atomicState?.tempMotSchPageData?.sNum
	}
	dynamicPage(name: "schMotSchedulePage", title: "Edit Schedule Page", description: "Modify Schedules", uninstall: false) {
		if(sNum) {
			showUpdateSchedule(sNum)
		}
	}
}

def getScheduleList() {
	def cnt = parent ? parent?.state?.appData?.schedules?.count : null
	def maxCnt = cnt ? cnt.toInteger() : 4
	maxCnt = Math.min( Math.max(cnt,4), 8)
	if(maxCnt < atomicState?.lastScheduleList?.size()) {
		maxCnt = atomicState?.lastScheduleList?.size()
		LogAction("A schedule size issue has occurred. The configured schedule size is smaller than the previous configuration restoring previous schedule size.", "warn", true)
	}
	def list = 1..maxCnt
	atomicState?.lastScheduleList = list
	return list
}

def showUpdateSchedule(sNum=null,hideStr=null) {
	updateScheduleStateMap()
	def schedList = getScheduleList()  // setting in initAutoApp adjust # of schedule slots
	def lact
	def act = 1
	def sLbl
	def cnt = 1
	schedList?.each { scd ->
		sLbl = "schMot_${scd}_"
		if(sNum != null) {
			if(sNum?.toInteger() == scd?.toInteger()) {
				lact = act
				act = settings["${sLbl}SchedActive"]
				def schName = settings["${sLbl}name"]
				editSchedule("secData":["scd":scd, "schName":schName, "hideable":(sNum ? false : true), "hidden":(act || (!act && scd == 1)) ? true : false, "hideStr":hideStr])
			}
		} else {
			lact = act
			act = settings["${sLbl}SchedActive"]
			if (lact || act) {
				def schName = settings["${sLbl}name"]
				editSchedule("secData":["scd":scd, "schName":schName, "hideable":true, "hidden":(act || (!act && scd == 1)) ? true : false, "hideStr":hideStr])
			}
		}
	}
}

def editSchedule(schedData) {
	def cnt = schedData?.secData?.scd
	LogTrace("editSchedule (${schedData?.secData})")

	def sLbl = "schMot_${cnt}_"
	def canHeat = atomicState?.schMotTstatCanHeat
	def canCool = atomicState?.schMotTstatCanCool
	def tempScaleStr = "°${getTemperatureScale()}"
	def act = settings["${sLbl}SchedActive"]
	def actIcon = act ? "active" : "inactive"
	def sectStr = schedData?.secData?.schName ? (act ? "Enabled" : "Disabled") : "Tap to Enable"
	def titleStr = "Schedule ${schedData?.secData?.scd} (${sectStr})"
	section(title: "${titleStr}                                                            ") {//, hideable:schedData?.secData?.hideable, hidden: schedData?.secData?.hidden) {
		input "${sLbl}SchedActive", "bool", title: "Schedule Enabled", description: (cnt == 1 && !settings?."${sLbl}SchedActive" ? "Enable to Edit Schedule" : null), required: true,
				defaultValue: false, submitOnChange: true, image: getAppImg("${actIcon}_icon.png")
		if(act) {
			input "${sLbl}name", "text", title: "Schedule Name", required: true, defaultValue: "Schedule ${cnt}", multiple: false, submitOnChange: true, image: getAppImg("name_tag_icon.png")
		}
	}
	if(act) {
		section("(${schedData?.secData?.schName ?: "Schedule ${cnt}"}) Setpoint Configuration:                                     ", hideable: true, hidden: (settings["${sLbl}HeatTemp"] != null && settings["${sLbl}CoolTemp"] != null) ) {
			paragraph "Configure Setpoints and HVAC modes that will be set when this Schedule is in use", title: "Setpoints and Mode"
			if(canHeat) {
				input "${sLbl}HeatTemp", "decimal", title: "Heat Set Point (${tempScaleStr})", description: "Range within ${tempRangeValues()}", required: true, range: tempRangeValues(),
						submitOnChange: true, image: getAppImg("heat_icon.png")
			}
			if(canCool) {
				input "${sLbl}CoolTemp", "decimal", title: "Cool Set Point (${tempScaleStr})", description: "Range within ${tempRangeValues()}", required: true, range: tempRangeValues(),
						submitOnChange: true, image: getAppImg("cool_icon.png")
			}
			input "${sLbl}HvacMode", "enum", title: "Set Hvac Mode:", required: false, description: "No change set", metadata: [values:tModeHvacEnum(canHeat,canCool, true)], multiple: false, image: getAppImg("hvac_mode_icon.png")
		}
		if(settings?.schMotRemoteSensor && !("remSen" in hideStr)) {
			section("(${schedData?.secData?.schName ?: "Schedule ${cnt}"}) Remote Sensor Options:                                           ", hideable: true, hidden: (settings["${sLbl}remSensor"] == null && settings["${sLbl}remSenThreshold"] == null)) {
				paragraph "Configure alternate Remote Temp sensors that are active with this schedule", title: "Alternate Remote Sensors\n(Optional)"
				input "${sLbl}remSensor", "capability.temperatureMeasurement", title: "Alternate Temp Sensors", description: "For Remote Sensor Automation", submitOnChange: true, required: false, multiple: true, image: getAppImg("temperature_icon.png")
				if(settings?."${sLbl}remSensor" != null) {
					def tmpVal = "Temp${(settings["${sLbl}remSensor"]?.size() > 1) ? " (avg):" : ":"} (${getDeviceTempAvg(settings["${sLbl}remSensor"])}${tempScaleStr})"
					paragraph "${tmpVal}", state: "complete", image: getAppImg("instruct_icon.png")
				}

				paragraph "Temp difference to trigger HVAC operations used with this schedule", title: "Alternate Action Threshold Temp\n(Optional)?", image: getAppImg("instruct_icon.png")
				input "${sLbl}remSenThreshold", "decimal", title: "Action Threshold Temp (${tempScaleStr})", required: false, defaultValue: 2.0, image: getAppImg("temp_icon.png")
			}
		}
		section("(${schedData?.secData?.schName ?: "Schedule ${cnt}"}) Motion Sensor Setpoints:                                        ", hideable: true, hidden:(settings["${sLbl}Motion"] == null) ) {
			paragraph "Activate alternate HVAC settings with Motion", title: "(Optional)"
			def mmot = settings["${sLbl}Motion"]
			input "${sLbl}Motion", "capability.motionSensor", title: "Motion Sensors", description: "Select Sensors to Use", required: false, multiple: true, submitOnChange: true, image: getAppImg("motion_icon.png")
			if(settings["${sLbl}Motion"]) {
				paragraph " • Motion State: (${isMotionActive(mmot) ? "Active" : "Not Active"})", state: "complete", image: getAppImg("instruct_icon.png")
				if(canHeat) {
					input "${sLbl}MHeatTemp", "decimal", title: "Heat Setpoint with Motion(${tempScaleStr})", description: "Range within ${tempRangeValues()}", required: true, range: tempRangeValues(), image: getAppImg("heat_icon.png")
				}
				if(canCool) {
					input "${sLbl}MCoolTemp", "decimal", title: "Cool Setpoint with Motion (${tempScaleStr})", description: "Range within ${tempRangeValues()}", required: true, range: tempRangeValues(), image: getAppImg("cool_icon.png")
				}
				input "${sLbl}MHvacMode", "enum", title: "Set Hvac Mode with Motion:", required: false, description: "No change set", metadata: [values:tModeHvacEnum(canHeat,canCool,true)], multiple: false, image: getAppImg("hvac_mode_icon.png")
				//input "${sLbl}MRestrictionMode", "mode", title: "Ignore in these modes", description: "Any location mode", required: false, multiple: true, image: getAppImg("mode_icon.png")
//				input "${sLbl}MPresHome", "capability.presenceSensor", title: "Only act when these people are home", description: "Always", required: false, multiple: true, image: getAppImg("nest_dev_pres_icon.png")
//				input "${sLbl}MPresAway", "capability.presenceSensor", title: "Only act when these people are away", description: "Always", required: false, multiple: true, image: getAppImg("nest_dev_away_icon.png")
				input "${sLbl}MDelayValOn", "enum", title: "Delay Motion Setting Changes", required: false, defaultValue: 60, metadata: [values:longTimeSecEnum()], multiple: false, image: getAppImg("delay_time_icon.png")
				input "${sLbl}MDelayValOff", "enum", title: "Delay disabling Motion Settings", required: false, defaultValue: 1800, metadata: [values:longTimeSecEnum()], multiple: false, image: getAppImg("delay_time_icon.png")
			}
		}

		def timeFrom = settings["${sLbl}restrictionTimeFrom"]
		def timeTo = settings["${sLbl}restrictionTimeTo"]
		def showTime = (timeFrom || timeTo || settings?."${sLbl}restrictionTimeFromCustom" || settings?."${sLbl}restrictionTimeToCustom") ? true : false
		def myShow = !(settings["${sLbl}restrictionMode"] || settings["${sLbl}restrictionDOW"] || showTime || settings["${sLbl}restrictionSwitchOn"] || settings["${sLbl}restrictionSwitchOff"] || settings["${sLbl}restrictionPresHome"] || settings["${sLbl}restrictionPresAway"] )
		section("(${schedData?.secData?.schName ?: "Schedule ${cnt}"}) Schedule Restrictions:                                          ", hideable: true, hidden: myShow) {
			paragraph "Restrict when this Schedule is in use", title: "(Optional)"
			input "${sLbl}restrictionMode", "mode", title: "Only execute in these modes", description: "Any location mode", required: false, multiple: true, image: getAppImg("mode_icon.png")
			input "${sLbl}restrictionDOW", "enum", options: timeDayOfWeekOptions(), title: "Only execute on these days", description: "Any week day", required: false, multiple: true, image: getAppImg("day_calendar_icon2.png")
			input "${sLbl}restrictionTimeFrom", "enum", title: (timeFrom ? "Only execute if time is between" : "Only execute during this time"), options: timeComparisonOptionValues(), required: showTime, multiple: false, submitOnChange: true, image: getAppImg("start_time_icon.png")
			if (showTime) {
				if ((timeFrom && timeFrom.contains("custom")) || settings?."${sLbl}restrictionTimeFromCustom" != null) {
					input "${sLbl}restrictionTimeFromCustom", "time", title: "Custom time", required: true, multiple: false
				} else {
					input "${sLbl}restrictionTimeFromOffset", "number", title: "Offset (+/- minutes)", range: "*..*", required: true, multiple: false, defaultValue: 0, image: getAppImg("offset_icon.png")
				}
				input "${sLbl}restrictionTimeTo", "enum", title: "And", options: timeComparisonOptionValues(), required: true, multiple: false, submitOnChange: true, image: getAppImg("stop_time_icon.png")
				if ((timeTo && timeTo.contains("custom")) || settings?."${sLbl}restrictionTimeToCustom" != null) {
					input "${sLbl}restrictionTimeToCustom", "time", title: "Custom time", required: true, multiple: false
				} else {
					input "${sLbl}restrictionTimeToOffset", "number", title: "Offset (+/- minutes)", range: "*..*", required: true, multiple: false, defaultValue: 0, image: getAppImg("offset_icon.png")
				}
			}
			input "${sLbl}restrictionPresHome", "capability.presenceSensor", title: "Only execute when one or more of these People are home", description: "Always", required: false, multiple: true, image: getAppImg("nest_dev_pres_icon.png")
			input "${sLbl}restrictionPresAway", "capability.presenceSensor", title: "Only execute when all these People are away", description: "Always", required: false, multiple: true, image: getAppImg("nest_dev_away_icon.png")
			input "${sLbl}restrictionSwitchOn", "capability.switch", title: "Only execute when these switches are all on", description: "Always", required: false, multiple: true, image: getAppImg("switch_on_icon.png")
			input "${sLbl}restrictionSwitchOff", "capability.switch", title: "Only execute when these switches are all off", description: "Always", required: false, multiple: true, image: getAppImg("switch_off_icon.png")
		}
	}
}

def getScheduleDesc(num = null) {
	def result = [:]
	def schedData = atomicState?.activeSchedData
	def actSchedNum = getCurrentSchedule()
	def tempScaleStr = "°${getTemperatureScale()}"
	def schNum
	def schData

	def sCnt = 1
	def sData = schedData
	if(num) {
		sData = schedData?.find { it?.key.toInteger() == num.toInteger() }
	}
	if(sData?.size()) {
		sData?.sort().each { scd ->
			def str = ""
			schNum = scd?.key
			schData = scd?.value
			def sLbl = "schMot_${schNum}_"
			def isRestrict = (schData?.m || schData?.tf || schData?.tfc || schData?.tfo || schData?.tt || schData?.ttc || schData?.tto || schData?.w || schData?.s1 || schData?.s0 || schData?.p1 || schData?.p0)
			def isTimeRes = (schData?.tf || schData?.tfc || schData?.tfo || schData?.tt || schData?.ttc || schData?.tto)
			def isDayRes = schData?.w
			def isTemp = (schData?.ctemp || schData?.htemp || schData?.hvacm)
			def isSw = (schData?.s1 || schData?.s0)
			def isPres = (schData?.p1 || schData?.p0)
			def isMot = schData?.m0
			def isRemSen = (schData?.sen0 || schData?.thres)
			def isFanEn = schData?.fan0
			def resPreBar = isSw || isPres || isTemp ? "│" : " "
			def tempPreBar = isMot || isRemSen ? "│" : "   "
			def motPreBar = isRemSen

			str += schData?.lbl ? " • ${schData?.lbl}${(actSchedNum?.toInteger() == schNum?.toInteger()) ? " (In Use)" : " (Not In Use)"}" : ""

			//restriction section
			str += isRestrict ? "\n ${isSw || isPres || isTemp ? "├" : "└"} Restrictions:" : ""
			def mLen = schData?.m ? schData?.m?.toString().length() : 0
			def mStr = ""
			def mdSize = 1
			schData?.m?.each { md ->
				mStr += md ? "\n ${isSw || isPres || isTemp ? "│ ${(isDayRes || isTimeRes || isPres || isSw) ? "│" : "    "}" : "   "} ${mdSize < schData?.m.size() ? "├" : "└"} ${md.toString()}" : ""
				mdSize = mdSize+1
			}
			str += schData?.m ? "\n ${resPreBar} ${(isTimeRes || schData?.w) ? "├" : "└"} Mode${schData?.m?.size() > 1 ? "s" : ""}:${isInMode(schData?.m) ? " (${okSym()})" : " (${notOkSym()})"}" : ""
			str += schData?.m ? "$mStr" : ""

			def dayStr = getAbrevDay(schData?.w)
			def timeDesc = getScheduleTimeDesc(schData?.tf, schData?.tfc, schData?.tfo, schData?.tt, schData?.ttc, schData?.tto, (isSw || isPres || isDayRes))
			str += isTimeRes ?	"\n │ ${isDayRes || isPres || isSw ? "├" : "└"} ${timeDesc}" : ""
			str += isDayRes ?	"\n │ ${isSw || isPres ? "├" : "└"} Days:${getSchRestrictDoWOk(schNum) ? " (${okSym()})" : " (${notOkSym()})"}" : ""
			str += isDayRes ?	"\n │ ${isSw || isPres ? "│" :"    "} └ ${dayStr}" : ""

			// def p1Len = schData?.p1 ? schData?.p1?.toString().length() : 0
			// def p1Str = ""
			// def p1dSize = 1
			// settings["${sLbl}restrictionPresAway"]?.each { ps1 ->
			// 	p1Str += ps1 ? "\n ${isSw || isPres || isTemp ? "│     " : "     "} ${p1dSize < settings["${sLbl}restrictionPresAway"].size() ? "├" : "└"} ${ps1.toString()}${!isPresenceHome(ps1) ? " (${okSym()})" : " (${notOkSym()})"}" : ""
			// 	p1dSize = p1dSize+1
			// }
			// def p0Len = schData?.p0 ? schData?.p0?.toString().length() : 0
			// def p0Str = ""
			// def p0dSize = 1
			// settings["${sLbl}restrictionPresHome"]?.each { ps0 ->
			// 	p0Str += ps0 ? "\n ${isSw || isPres || isTemp ? "│     " : "     "} ${p0dSize < settings["${sLbl}restrictionPresHome"].size() ? "├" : "└"} ${ps0.toString()}" : ""
			// 	p0dSize = p0dSize+1
			// }
			str += schData?.p1 ?	"\n │ ${(schData?.p0 || isSw) ? "├" : "└"} Presence Home:${isSomebodyHome(settings["${sLbl}restrictionPresHome"]) ? " (${okSym()})" : " (${notOkSym()})"}" : ""
			//str += schData?.p1 ? "$p1Str" : ""
			str += schData?.p1 ?	"\n │ ${(schData?.p0 || isSw) ? "│" : "   "} └ (${schData?.p1.size()} Selected)" : ""
			str += schData?.p0 ?	"\n │ ${isSw ? "├" : "└"} Presence Away:${!isSomebodyHome(settings["${sLbl}restrictionPresAway"]) ? " (${okSym()})" : " (${notOkSym()})"}" : ""
			//str += schData?.p0 ? "$p0Str" : ""
			str += schData?.p0 ? 	"\n │ ${isSw ? "│" : "   "} └ (${schData?.p0.size()} Selected)" : ""

			str += schData?.s1 ?	"\n │ ${schData?.s0 ? "├" : "└"} Switches On:${isSwitchOn(settings["${sLbl}restrictionSwitchOn"]) ? " (${okSym()})" : " (${notOkSym()})"}" : ""
			str += schData?.s1 ?	"\n │ ${schData?.s0 ? "│" : "   "} └ (${schData?.s1.size()} Selected)" : ""
			str += schData?.s0 ?	"\n │ └ Switches Off:${!isSwitchOn(settings["${sLbl}restrictionSwitchOff"]) ? " (${okSym()})" : " (${notOkSym()})"}" : ""
			str += schData?.s0 ? 	"\n │      └ (${schData?.s0.size()} Selected)" : ""

			//Temp Setpoints
			str += isTemp  ? 	"${isRestrict ? "\n │\n" : "\n"} ${(isMot || isRemSen) ? "├" : "└"} Temp Setpoints:" : ""
			str += schData?.ctemp ? "\n ${tempPreBar}  ${schData?.htemp ? "├" : "└"} Cool Setpoint: (${fixTempSetting(schData?.ctemp)}${tempScaleStr})" : ""
			str += schData?.htemp ? "\n ${tempPreBar}  ${schData?.hvacm ? "├" : "└"} Heat Setpoint: (${fixTempSetting(schData?.htemp)}${tempScaleStr})" : ""
			str += schData?.hvacm ? "\n ${tempPreBar}  └ HVAC Mode: (${strCapitalize(schData?.hvacm)})" : ""

			//Motion Info
			// def m0Len = schData?.p0 ? schData?.p0?.toString().length() : 0
			// def m0Str = ""
			// def m0dSize = 1
			// schData?.m0?.each { ms0 ->
			// 	m0Str += ms0 ? "\n     ${isTemp || isFanEn || isRemSen || isRestrict ? "│" : " "} ${m0dSize < schData?.m0.size() ? "├" : "└"} ${ms0.toString()}" : ""
			// 	m0dSize = m0dSize+1
			// }
			str += isMot ?				"${isTemp || isFanEn || isRemSen || isRestrict ? "\n │\n" : "\n"} ${isRemSen ? "├" : "└"} Motion Settings:" : ""
			str += isMot ?		 		"\n ${motPreBar ? "│" : "   "} ${(schData?.mctemp || schData?.mhtemp) ? "├" : "└"} Motion Sensors: (${schData?.m0.size()})" : ""
			//str += schData?.m0 ? "$m0Str" : ""
			//str += isMot ?				"\n ${motPreBar ? "│" : "   "} ${schData?.mctemp || schData?.mhtemp ? "│" : ""} └ (${isMotionActive(settings["${sLbl}Motion"]) ? "Active" : "None Active"})" : ""
			str += isMot && schData?.mctemp ? 	"\n ${motPreBar ? "│" : "   "} ${(schData?.mctemp || schData?.mhtemp) ? "├" : "└"} Mot. Cool Setpoint: (${fixTempSetting(schData?.mctemp)}${tempScaleStr})" : ""
			str += isMot && schData?.mhtemp ? 	"\n ${motPreBar ? "│" : "   "} ${schData?.mdelayOn || schData?.mdelayOff ? "├" : "└"} Mot. Heat Setpoint: (${fixTempSetting(schData?.mhtemp)}${tempScaleStr})" : ""
			str += isMot && schData?.mhvacm ? 	"\n ${motPreBar ? "│" : "   "} ${(schData?.mdelayOn || schData?.mdelayOff) ? "├" : "└"} Mot. HVAC Mode: (${strCapitalize(schData?.mhvacm)})" : ""
			str += isMot && schData?.mdelayOn ? 	"\n ${motPreBar ? "│" : "   "} ${schData?.mdelayOff ? "├" : "└"} Mot. On Delay: (${getEnumValue(longTimeSecEnum(), schData?.mdelayOn)})" : ""
			str += isMot && schData?.mdelayOff ? 	"\n ${motPreBar ? "│" : "   "} └ Mot. Off Delay: (${getEnumValue(longTimeSecEnum(), schData?.mdelayOff)})" : ""

			//Remote Sensor Info
			str += isRemSen && schData?.sen0 ?	"${isRemSen || isRestrict ? "\n │\n" : "\n"} └ Alternate Remote Sensor:" : ""
			//str += isRemSen && schData?.sen0 ? 	"\n      ├ Temp Sensors: (${schData?.sen0.size()})" : ""
			settings["${sLbl}remSensor"]?.each { t ->
				str += "\n      ├ ${t?.label}: ${(t?.label?.toString()?.length() > 10) ? "\n      │ └ " : ""}(${getDeviceTemp(t)}°${getTemperatureScale()})"
			}
			str += isRemSen && schData?.sen0 ? 	"\n      └ Temp${(settings["${sLbl}remSensor"]?.size() > 1) ? " (avg):" : ":"} (${getDeviceTempAvg(settings["${sLbl}remSensor"])}${tempScaleStr})" : ""
			str += isRemSen && schData?.thres ? 	"\n  └ Threshold: (${settings["${sLbl}remSenThreshold"]}${tempScaleStr})" : ""
			//log.debug "str: \n$str"
			if(str != "") { result[schNum] = str }
		}
	}
	return (result?.size() >= 1) ? result : null
}

def getScheduleTimeDesc(timeFrom, timeFromCustom, timeFromOffset, timeTo, timeToCustom, timeToOffset, showPreLine=false) {
	def tf = new SimpleDateFormat("h:mm a")
		tf.setTimeZone(location?.timeZone)
	def spl = showPreLine == true ? "│" : ""
	def timeToVal = null
	def timeFromVal = null
	def i = 0
	if(timeFrom && timeTo) {
		while (i < 2) {
			switch(i == 0 ? timeFrom : timeTo) {
				case "custom time":
					if(i == 0) { timeFromVal = tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", timeFromCustom)) }
					else { timeToVal = tf.format(Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", timeToCustom)) }
					break
				case "sunrise":
					def sunTime = ((timeFromOffset > 0 || timeToOffset > 0) ? getSunriseAndSunset(zipCode: location.zipCode, sunriseOffset: "00:${i == 0 ? timeFromOffset : timeToOffset}") : getSunriseAndSunset(zipCode: location.zipCode))
					if(i == 0) { timeFromVal = "Sunrise: (" + tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", sunTime?.sunrise.toString())) + ")" }
					else { timeToVal = "Sunrise: (" + tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", sunTime?.sunrise.toString())) + ")" }
					break
				case "sunset":
					def sunTime = ((timeFromOffset > 0 || timeToOffset > 0) ? getSunriseAndSunset(zipCode: location.zipCode, sunriseOffset: "00:${i == 0 ? timeFromOffset : timeToOffset}") : getSunriseAndSunset(zipCode: location.zipCode))
					if(i == 0) { timeFromVal = "Sunset: (" + tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", sunTime?.sunset.toString())) + ")" }
					else { timeToVal = "Sunset: (" + tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", sunTime?.sunset.toString())) + ")" }
					break
				case "noon":
					def rightNow = adjustTime().time
					def offSet = (timeFromOffset != null || timeToOffset != null) ? (i == 0 ? (timeFromOffset * 60 * 1000) : (timeToOffset * 60 * 1000)) : 0
					def res = "Noon: " + formatTime(convertDateToUnixTime((rightNow - rightNow.mod(86400000) + 43200000) + offSet))
					if(i == 0) { timeFromVal = res }
					else { timeToVal = res }
					break
				case "midnight":
					def rightNow = adjustTime().time
					def offSet = (timeFromOffset != null || timeToOffset != null) ? (i == 0 ? (timeFromOffset * 60 * 1000) : (timeToOffset * 60 * 1000)) : 0
					def res = "Midnight: " + formatTime(convertDateToUnixTime((rightNow - rightNow.mod(86400000)) + offSet))
					if(i == 0) { timeFromVal = res }
					else { timeToVal = res }
				break
			}
			i += 1
		}
	}
	def timeOk = ((timeFrom && (timeFromCustom || timeFromOffset) && timeTo && (timeToCustom || timeToOffset)) && checkTimeCondition(timeFrom, timeFromCustom, timeFromOffset, timeTo, timeToCustom, timeToOffset)) ? true : false
	def out = ""
	out += (timeFromVal && timeToVal) ? "Time:${timeOk ? " (${okSym()})" : " (${notOkSym()})"}\n │ ${spl}     ├ $timeFromVal\n │ ${spl}     ├   to\n │ ${spl}     └ $timeToVal" : ""
	return out
}

void updSchedActiveState(String schNum, String active) {
	LogAction("updSchedActiveState(schNum: $schNum, active: $active)", "trace", true)
	if(schNum && active) {
		def sLbl = "schMot_${schNum}_SchedActive"
		def curAct = settings["${sLbl}"]
		if(curAct.toString() == active.toString()) { return }
		LogAction("updateSchedActiveState | Setting Schedule (${schNum} - ${getSchedLbl(schNum)}) Active to ($active)", "info", true)
		settingUpdate("${sLbl}", "${active}")
	} else { return }
}

def okSym() {
	return "✓"// ☑"
}
def notOkSym() {
	return "✘"
}

def getRemSenTempSrc() {
	return atomicState?.remoteTempSourceStr ?: null
}

def getAbrevDay(vals) {
	def list = []
	if(vals) {
		//log.debug "days: $vals | (${vals?.size()})"
		def len = (vals?.toString().length() < 7) ? 3 : 2
		vals?.each { d ->
			list.push(d?.toString().substring(0, len))
		}
	}
	return list
}

def roundTemp(Double temp) {
	if(temp == null) { return null }
	def newtemp
	if( getTemperatureScale() == "C") {
		newtemp = Math.round(temp.round(1) * 2) / 2.0f //
	} else {
		if(temp instanceof Integer) {
			//log.debug "roundTemp: ($temp) is Integer"
			newTemp = temp.toInteger()
		}
		else if(temp instanceof Double) {
			//log.debug "roundTemp: ($temp) is Double"
			newtemp = temp.round(0).toInteger()
		}
		else if(temp instanceof BigDecimal) {
			//log.debug "roundTemp: ($temp) is BigDecimal"
			newtemp = temp.toInteger()
		}
	}
	return newtemp
}

def updateScheduleStateMap() {
	if(autoType == "schMot" && isSchMotConfigured()) {
		def actSchedules = null
		def numAct = 0
		actSchedules = [:]
		getScheduleList()?.each { scdNum ->
			def sLbl = "schMot_${scdNum}_"
			def newScd = []
			def schActive = settings["${sLbl}SchedActive"]

			if(schActive) {
				actSchedules?."${scdNum}" = [:]
				newScd = cleanUpMap([
					lbl: settings["${sLbl}name"],
					m: settings["${sLbl}restrictionMode"],
					tf: settings["${sLbl}restrictionTimeFrom"],
					tfc: settings["${sLbl}restrictionTimeFromCustom"],
					tfo: settings["${sLbl}restrictionTimeFromOffset"],
					tt: settings["${sLbl}restrictionTimeTo"],
					ttc: settings["${sLbl}restrictionTimeToCustom"],
					tto: settings["${sLbl}restrictionTimeToOffset"],
					w: settings["${sLbl}restrictionDOW"],
					p1: deviceInputToList(settings["${sLbl}restrictionPresHome"]),
					p0: deviceInputToList(settings["${sLbl}restrictionPresAway"]),
					s1: deviceInputToList(settings["${sLbl}restrictionSwitchOn"]),
					s0: deviceInputToList(settings["${sLbl}restrictionSwitchOff"]),
					ctemp: roundTemp(settings["${sLbl}CoolTemp"]),
					htemp: roundTemp(settings["${sLbl}HeatTemp"]),
					hvacm: settings["${sLbl}HvacMode"],
					sen0: settings["schMotRemoteSensor"] ? deviceInputToList(settings["${sLbl}remSensor"]) : null,
					thres: settings["schMotRemoteSensor"] ? settings["${sLbl}remSenThreshold"] : null,
					m0: deviceInputToList(settings["${sLbl}Motion"]),
					mctemp: settings["${sLbl}Motion"] ? roundTemp(settings["${sLbl}MCoolTemp"]) : null,
					mhtemp: settings["${sLbl}Motion"] ? roundTemp(settings["${sLbl}MHeatTemp"]) : null,
					mhvacm: settings["${sLbl}Motion"] ? settings["${sLbl}MHvacMode"] : null,
//					mpresHome: settings["${sLbl}Motion"] ? settings["${sLbl}MPresHome"] : null,
//					mpresAway: settings["${sLbl}Motion"] ? settings["${sLbl}MPresAway"] : null,
					mdelayOn: settings["${sLbl}Motion"] ? settings["${sLbl}MDelayValOn"] : null,
					mdelayOff: settings["${sLbl}Motion"] ? settings["${sLbl}MDelayValOff"] : null
				])
				numAct += 1
				actSchedules?."${scdNum}" = newScd
				//LogAction("updateScheduleMap [ ScheduleNum: $scdNum | PrefixLbl: $sLbl | SchedActive: $schActive | NewSchedData: $newScd ]", "info", true)
			}
		}
		atomicState.activeSchedData = actSchedules
	}
}

def deviceInputToList(items) {
	def list = []
	if(items) {
		items?.sort().each { d ->
			list.push(d?.displayName.toString())
		}
		return list
	}
	return null
}

/*
def inputItemsToList(items) {
	def list = []
	if(items) {
		items?.each { d ->
			list.push(d)
		}
		return list
	}
	return null
}
*/

def isSchMotConfigured() {
	return settings?.schMotTstat ? true : false
}

def getLastschMotEvalSec() { return !atomicState?.lastschMotEval ? 100000 : GetTimeDiffSeconds(atomicState?.lastschMotEval, null, "getLastschMotEvalSec").toInteger() }

def schMotCheck() {
	LogAction("schMotCheck", "trace", false)
	try {
		if(atomicState?.disableAutomation) { return }
		def schWaitVal = settings?.schMotWaitVal?.toInteger() ?: 60
		if(schWaitVal > 120) { schWaitVal = 120 }
		def t0 = getLastschMotEvalSec()
		if(t0 < schWaitVal) {
			def schChkVal = ((schWaitVal - t0) < 30) ? 30 : (schWaitVal - t0)
			scheduleAutomationEval(schChkVal)
			LogAction("Too Soon to Evaluate Actions; Re-Evaluation in (${schChkVal} seconds)", "info", true)
			return
		}

		def execTime = now()
		atomicState?.lastEvalDt = getDtNow()
		atomicState?.lastschMotEval = getDtNow()

		// This order is important
		// turn system on/off, then update schedule mode/temps, then remote sensors, then update fans

		if(settings?.schMotWaterOff) {
			if(isLeakWatConfigured()) { leakWatCheck() }
		}
		if(settings?.schMotContactOff) {
			if(isConWatConfigured()) { conWatCheck() }
		}
		if(settings?.schMotExternalTempOff) {
			if(isExtTmpConfigured()) {
				if(settings?.extTmpUseWeather) { getExtConditions() }
				extTmpTempCheck()
			}
		}
//		if(settings?.schMotSetTstatTemp) {
			if(isTstatSchedConfigured()) { setTstatTempCheck() }
//		}
		if(settings?.schMotRemoteSensor) {
			if(isRemSenConfigured()) {
				remSenCheck()
			}
		}
		if(settings?.schMotHumidityControl) {
			if(isHumCtrlConfigured()) {
				if(settings?.humCtrlUseWeather) { getExtConditions() }
				humCtrlCheck()
			}
		}
		if(settings?.schMotOperateFan) {
			if(isFanCtrlConfigured()) {
				fanCtrlCheck()
			}
		}

		atomicState?.lastschMotEval = getDtNow()
		storeExecutionHistory((now() - execTime), "schMotCheck")
	} catch (ex) {
		log.error "schMotCheck Exception:", ex
		parent?.sendExceptionData(ex, "schMotCheck", true, getAutoType())
	}
}

def storeLastEventData(evt) {
	if(evt) {
		def newVal = ["name":evt.name, "displayName":evt.displayName, "value":evt.value, "date":formatDt(evt.date), "unit":evt.unit]
		atomicState?.lastEventData = newVal
		//log.debug "LastEvent: ${atomicState?.lastEventData}"

		def list = atomicState?.detailEventHistory ?: []
		def listSize = 30
		if(list?.size() < listSize) {
			list.push(newVal)
		}
		else if(list?.size() > listSize) {
			def nSz = (list?.size()-listSize) + 1
			def nList = list?.drop(nSz)
			nList?.push(newVal)
			list = nList
		}
		else if(list?.size() == listSize) {
			def nList = list?.drop(1)
			nList?.push(newVal)
			list = nList
		}
		if(list) { atomicState?.detailEventHistory = list }
	}
}

def storeExecutionHistory(val, method = null) {
	//log.debug "storeExecutionHistory($val, $method)"
	try {
		if(method) {
			LogAction("${method} Execution Time: (${val} milliseconds)", "trace", false)
		}
		if(method in ["watchDogCheck", "checkNestMode", "schMotCheck"]) {
			atomicState?.lastExecutionTime = val ?: null
			def list = atomicState?.evalExecutionHistory ?: []
			def listSize = 30
			list = addToList(val, list, listSize)
			if(list) { atomicState?.evalExecutionHistory = list }
		}
		if(!(method in ["watchDogCheck", "checkNestMode"])) {
			def list = atomicState?.detailExecutionHistory ?: []
			def listSize = 30
			list = addToList([val, method], list, listSize)
			if(list) { atomicState?.detailExecutionHistory = list }
		}
	} catch (ex) {
		log.error "storeExecutionHistory Exception:", ex
		parent?.sendExceptionData(ex, "storeExecutionHistory", true, getAutoType())
	}
}

def addToList(val, list, listSize) {
	if(list?.size() < listSize) {
		list.push(val)
	}
	else if(list?.size() > listSize) {
		def nSz = (list?.size()-listSize) + 1
		def nList = list?.drop(nSz)
		nList?.push(val)
		list = nList
	}
	else if(list?.size() == listSize) {
		def nList = list?.drop(1)
		nList?.push(val)
		list = nList
	}
	return list
}

def getAverageValue(items) {
	def tmpAvg = []
	def val = 0
	if(!items) { return val }
	else if(items?.size() > 1) {
		tmpAvg = items
		if(tmpAvg && tmpAvg?.size() > 1) { val = (tmpAvg?.sum().toDouble() / tmpAvg?.size().toDouble()).round(0) }
	} else { val = item }
	return val.toInteger()
}

/************************************************************************************************
|								DYNAMIC NOTIFICATION PAGES								|
*************************************************************************************************/

def setNotificationPage(params) {
	def pName = params.pName
	def allowSpeech = false
	def allowAlarm = false
	def showSched = false
	if(params?.pName) {
		atomicState.curNotifPageData = params
		allowSpeech = params?.allowSpeech?.toBoolean(); showSched = params?.showSchedule?.toBoolean(); allowAlarm = params?.allowAlarm?.toBoolean()
	} else {
		pName = atomicState?.curNotifPageData?.pName; allowSpeech = atomicState?.curNotifPageData?.allowSpeech; showSched = atomicState?.curNotifPageData?.showSchedule; allowAlarm = atomicState?.curNotifPageData?.allowAlarm
	}
	dynamicPage(name: "setNotificationPage", title: "Configure Notification Options", uninstall: false) {
		section("Notification Preferences:") {
			input "${pName}NotificationsOn", "bool", title: "Enable Notifications?", description: (!settings["${pName}NotificationsOn"] ? "Enable Text, Voice, Ask Alexa, or Alarm Notifications" : ""), required: false, defaultValue: false, submitOnChange: true,
						image: getAppImg("notification_icon.png")
		}
		if(settings["${pName}NotificationsOn"]) {
			def notifDesc = !location.contactBookEnabled ? "Enable Push Messages Below" : "(Manager App Recipients are used by default)"
			section("${notifDesc}") {
				if(!location.contactBookEnabled) {
					input "${pName}UsePush", "bool", title: "Send Push Notitifications\n(Optional)", required: false, submitOnChange: true, defaultValue: false, image: getAppImg("notification_icon.png")
				} else {
					input("${pName}NotifRecips", "contact", title: "Select Recipients\n(Optional)", required: false, multiple: true, submitOnChange: true, image: getAppImg("recipient_icon.png")) {
						input ("${pName}NotifPhones", "phone", title: "Phone Number to Send SMS to\n(Optional)", submitOnChange: true, required: false)
					}
				}
			}
		}
		if(allowSpeech && settings?."${pName}NotificationsOn") {
			section("Voice Notification Preferences:") {
				input "${pName}AllowSpeechNotif", "bool", title: "Enable Voice Notifications?", description: "Media players, Speech Devices, or Ask Alexa", required: false, defaultValue: (settings?."${pName}AllowSpeechNotif" ? true : false), submitOnChange: true, image: getAppImg("speech_icon.png")
				if(settings["${pName}AllowSpeechNotif"]) {
					if(pName == "leakWat") {
						if(!atomicState?."${pName}OffVoiceMsg" || !settings["${pName}UseCustomSpeechNotifMsg"]) { atomicState?."${pName}OffVoiceMsg" = "ATTENTION: %devicename% has been turned OFF because %wetsensor% has reported it is WET" }
						if(!atomicState?."${pName}OnVoiceMsg" || !settings["${pName}UseCustomSpeechNotifMsg"]) { atomicState?."${pName}OnVoiceMsg" = "Restoring %devicename% to %lastmode% Mode because ALL water sensors have been Dry again for (%ondelay%)" }
					}
					if(pName == "conWat") {
						if(!atomicState?."${pName}OffVoiceMsg" || !settings["${pName}UseCustomSpeechNotifMsg"]) { atomicState?."${pName}OffVoiceMsg" = "ATTENTION: %devicename% has been turned OFF because %opencontact% has been Opened for (%offdelay%)" }
						if(!atomicState?."${pName}OnVoiceMsg" || !settings["${pName}UseCustomSpeechNotifMsg"]) { atomicState?."${pName}OnVoiceMsg" = "Restoring %devicename% to %lastmode% Mode because ALL contacts have been Closed again for (%ondelay%)" }
					}
					if(pName == "extTmp") {
						if(!atomicState?."${pName}OffVoiceMsg" || !settings["${pName}UseCustomSpeechNotifMsg"]) { atomicState?."${pName}OffVoiceMsg" = "ATTENTION: %devicename% has been turned to ECO because External Temp is above the temp threshold for (%offdelay%)" }
						if(!atomicState?."${pName}OnVoiceMsg" || !settings["${pName}UseCustomSpeechNotifMsg"]) { atomicState?."${pName}OnVoiceMsg" = "Restoring %devicename% to %lastmode% Mode because External Temp has been above the temp threshold for (%ondelay%)" }
					}
					input "${pName}SendToAskAlexaQueue", "bool", title: "Send to Ask Alexa Message Queue?", required: false, defaultValue: (settings?."${pName}AllowSpeechNotif" ? false : true), submitOnChange: true,
							image: askAlexaImgUrl()
					input "${pName}SpeechMediaPlayer", "capability.musicPlayer", title: "Select Media Player(s)", hideWhenEmpty: true, multiple: true, required: false, submitOnChange: true, image: getAppImg("media_player.png")
					input "${pName}SpeechDevices", "capability.speechSynthesis", title: "Select Speech Synthesizer(s)", hideWhenEmpty: true, multiple: true, required: false, submitOnChange: true, image: getAppImg("speech2_icon.png")
					if(settings["${pName}SpeechMediaPlayer"]) {
						input "${pName}SpeechVolumeLevel", "number", title: "Default Volume Level?", required: false, defaultValue: 30, range: "0::100", submitOnChange: true, image: getAppImg("volume_icon.png")
						input "${pName}SpeechAllowResume", "bool", title: "Can Resume Playing Media?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("resume_icon.png")
					}
					def desc = ""
					if(pName in ["conWat", "extTmp", "leakWat"]) {
						if( (settings["${pName}SpeechMediaPlayer"] || settings["${pName}SpeechDevices"] || settings["${pName}SendToAskAlexaQueue"]) ) {
							switch(pName) {
								case "conWat":
									desc = "Contact Close"
									break
								case "extTmp":
									desc = "External Temperature Threshold"
									break
								case "leakWat":
									desc = "Water Dried"
									break
							}

							input "${pName}SpeechOnRestore", "bool", title: "Speak when restoring HVAC on (${desc})?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("speech_icon.png")
		// TODO There are more messages and errors than ON / OFF
							input "${pName}UseCustomSpeechNotifMsg", "bool", title: "Customize Notitification Message?", required: false, defaultValue: (settings?."${pName}AllowSpeechNotif" ? false : true), submitOnChange: true,
								image: getAppImg("speech_icon.png")
							if(settings["${pName}UseCustomSpeechNotifMsg"]) {
								getNotifVariables(pName)
								input "${pName}CustomOffSpeechMessage", "text", title: "Turn Off Message?", required: false, defaultValue: atomicState?."${pName}OffVoiceMsg" , submitOnChange: true, image: getAppImg("speech_icon.png")
								atomicState?."${pName}OffVoiceMsg" = settings?."${pName}CustomOffSpeechMessage"
								if(settings?."${pName}CustomOffSpeechMessage") {
									paragraph "Off Msg:\n" + voiceNotifString(atomicState?."${pName}OffVoiceMsg",pName)
								}
								input "${pName}CustomOnSpeechMessage", "text", title: "Restore On Message?", required: false, defaultValue: atomicState?."${pName}OnVoiceMsg", submitOnChange: true, image: getAppImg("speech_icon.png")
								atomicState?."${pName}OnVoiceMsg" = settings?."${pName}CustomOnSpeechMessage"
								if(settings?."${pName}CustomOnSpeechMessage") {
									paragraph "Restore On Msg:\n" + voiceNotifString(atomicState?."${pName}OnVoiceMsg",pName)
								}
							} else {
								atomicState?."${pName}OffVoiceMsg" = ""
								atomicState?."${pName}OnVoiceMsg" = ""
							}
						}
					}
				}
			}
		}
		if(allowAlarm && settings?."${pName}NotificationsOn") {
			section("Alarm/Siren Device Preferences:") {
				input "${pName}AllowAlarmNotif", "bool", title: "Enable Alarm|Siren?", required: false, defaultValue: (settings?."${pName}AllowAlarmNotif" ? true : false), submitOnChange: true,
						image: getAppImg("alarm_icon.png")
				if(settings["${pName}AllowAlarmNotif"]) {
					input "${pName}AlarmDevices", "capability.alarm", title: "Select Alarm/Siren(s)", multiple: true, required: settings["${pName}AllowAlarmNotif"], submitOnChange: true, image: getAppImg("alarm_icon.png")
				}
			}
		}
		if(pName in ["conWat", "leakWat", "extTmp", "watchDog"] && settings["${pName}NotificationsOn"] && settings["${pName}AllowAlarmNotif"] && settings["${pName}AlarmDevices"]) {
			section("Notification Alert Options (1):") {
				input "${pName}_Alert_1_Delay", "enum", title: "First Alert Delay (in minutes)", defaultValue: null, required: true, submitOnChange: true, metadata: [values:longTimeSecEnum()],
						image: getAppImg("alert_icon2.png")
				if(settings?."${pName}_Alert_1_Delay") {
					input "${pName}_Alert_1_AlarmType", "enum", title: "Alarm Type to use?", metadata: [values:alarmActionsEnum()], defaultValue: null, submitOnChange: true, required: true, image: getAppImg("alarm_icon.png")
					if(settings?."${pName}_Alert_1_AlarmType") {
						input "${pName}_Alert_1_Alarm_Runtime", "enum", title: "Turn off Alarm After (in seconds)?", metadata: [values:shortTimeEnum()], defaultValue: 10, required: true, submitOnChange: true,
								image: getAppImg("delay_time_icon.png")
					}
				}
			}
			if(settings["${pName}_Alert_1_Delay"]) {
				section("Notification Alert Options (2):") {
					input "${pName}_Alert_2_Delay", "enum", title: "Second Alert Delay (in minutes)", defaultValue: null, metadata: [values:longTimeSecEnum()], required: false, submitOnChange: true, image: getAppImg("alert_icon2.png")
					if(settings?."${pName}_Alert_2_Delay") {
						input "${pName}_Alert_2_AlarmType", "enum", title: "Alarm Type to use?", metadata: [values:alarmActionsEnum()], defaultValue: null, submitOnChange: true, required: true, image: getAppImg("alarm_icon.png")
						if(settings?."${pName}_Alert_2_AlarmType") {
							input "${pName}_Alert_2_Alarm_Runtime", "enum", title: "Turn off Alarm After (in minutes)?", metadata: [values:shortTimeEnum()], defaultValue: 10, required: true, submitOnChange: true,
									image: getAppImg("delay_time_icon.png")
						}
					}
				}
			}
		}
	}
}

def getNotifVariables(pName) {
	def str = ""
	str += "\n • DeviceName: %devicename%"
	str += "\n • Last Mode: %lastmode%"
	str += (pName == "leakWat") ? "\n • Wet Water Sensor: %wetsensor%" : ""
	str += (pName == "conWat") ? "\n • Open Contact: %opencontact%" : ""
	str += (pName in ["conWat", "extTmp"]) ? "\n • Off Delay: %offdelay%" : ""
	str += "\n • On Delay: %ondelay%"
	str += (pName == "extTmp") ? "\n • Temp Threshold: %tempthreshold%" : ""
	paragraph "These Variables are accepted: ${str}"
}

//process custom tokens to generate final voice message (Copied from BigTalker)
def voiceNotifString(phrase, pName) {
	//LogTrace("conWatVoiceNotifString")
	try {
		if(phrase?.toLowerCase().contains("%devicename%")) { phrase = phrase?.toLowerCase().replace('%devicename%', (settings?."schMotTstat"?.displayName.toString() ?: "unknown")) }
		if(phrase?.toLowerCase().contains("%lastmode%")) { phrase = phrase?.toLowerCase().replace('%lastmode%', (atomicState?."${pName}RestoreMode".toString() ?: "unknown")) }
		if(pName == "leakWat" && phrase?.toLowerCase().contains("%wetsensor%")) {
			phrase = phrase?.toLowerCase().replace('%wetsensor%', (getWetWaterSensors(leakWatSensors) ? getWetWaterSensors(leakWatSensors)?.join(", ").toString() : "a selected leak sensor")) }
		if(pName == "conWat" && phrase?.toLowerCase().contains("%opencontact%")) {
			phrase = phrase?.toLowerCase().replace('%opencontact%', (getOpenContacts(conWatContacts) ? getOpenContacts(conWatContacts)?.join(", ").toString() : "a selected contact")) }
		if(pName == "extTmp" && phrase?.toLowerCase().contains("%tempthreshold%")) {
			phrase = phrase?.toLowerCase().replace('%tempthreshold%', "${extTmpDiffVal.toString()}(°${getTemperatureScale()})") }
		if(phrase?.toLowerCase().contains("%offdelay%")) { phrase = phrase?.toLowerCase().replace('%offdelay%', getEnumValue(longTimeSecEnum(), settings?."${pName}OffDelay").toString()) }
		if(phrase?.toLowerCase().contains("%ondelay%")) { phrase = phrase?.toLowerCase().replace('%ondelay%', getEnumValue(longTimeSecEnum(), settings?."${pName}OnDelay").toString()) }
	} catch (ex) {
		log.error "voiceNotifString Exception:", ex
		parent?.sendExceptionData(ex, "voiceNotifString", true, getAutoType())
	}
	return phrase
}

def getNotificationOptionsConf(pName) {
	LogTrace("getNotificationOptionsConf pName: $pName")
	def res = (settings?."${pName}NotificationsOn" &&
			(getRecipientDesc(pName) ||
			(settings?."${pName}AllowSpeechNotif" && (settings?."${pName}SpeechDevices" || settings?."${pName}SpeechMediaPlayer")) ||
			(settings?."${pName}AllowAlarmNofif" && settings?."${pName}AlarmDevices")
		) ) ? true : false
	return res
}

def getNotifConfigDesc(pName) {
	LogTrace("getNotifConfigDesc pName: $pName")
	def str = ""
	if(settings?."${pName}NotificationsOn") {
		str += ( getRecipientDesc(pName) || (settings?."${pName}AllowSpeechNotif" && (settings?."${pName}SpeechDevices" || settings?."${pName}SpeechMediaPlayer"))) ?
			"Notification Status:" : ""
		str += (settings?."${pName}NotifRecips") ? "${str != "" ? "\n" : ""} • Contacts: (${settings?."${pName}NotifRecips"?.size()})" : ""
		str += (settings?."${pName}UsePush") ? "\n • Push Messages: Enabled" : ""
		str += (settings?."${pName}NotifPhones") ? "\n • SMS: (${settings?."${pName}NotifPhones"?.size()})" : ""
		def t0 = getVoiceNotifConfigDesc(pName)
		str += t0 ? ("${(str != "") ? "\n\n" : "\n"}Voice Status:${t0}") : ""
		def t1 = getAlarmNotifConfigDesc(pName)
		str += t1 ? ("${(str != "") ? "\n\n" : "\n"}Alarm Status:${t1}") : ""
		def t2 = getAlertNotifConfigDesc(pName)
		str += t2 ? "\n${t2}" : ""
	}
	return (str != "") ? "${str}" : null
}

def getVoiceNotifConfigDesc(pName) {
	def str = ""
	if(settings?."${pName}NotificationsOn" && settings["${pName}AllowSpeechNotif"]) {
		def speaks = settings?."${pName}SpeechDevices"
		def medias = settings?."${pName}SpeechMediaPlayer"
		str += settings["${pName}SendToAskAlexaQueue"] ? "\n• Send to Ask Alexa: (True)" : ""
		str += speaks ? "\n• Speech Devices:" : ""
		if(speaks) {
			def cnt = 1
			speaks?.each { str += it ? "\n ${cnt < speaks.size() ? "├" : "└"} $it" : ""; cnt = cnt+1; }
		}
		str += medias ? "${speaks ? "\n\n" : "\n"}• Media Players:" : ""
		if(medias) {
			def cnt = 1
			medias?.sort { it?.displayName }?.each { str += it ? "\n│${cnt < medias.size() ? "├" : "└"} $it" : ""; cnt = cnt+1; }
		}
		str += (medias && settings?."${pName}SpeechVolumeLevel") ? "\n├ Volume: (${settings?."${pName}SpeechVolumeLevel"})" : ""
		str += (medias && settings?."${pName}SpeechAllowResume") ? "\n└ Resume: (${strCapitalize(settings?."${pName}SpeechAllowResume")})" : ""
		str += (settings?."${pName}UseCustomSpeechNotifMsg" && (medias || speaks)) ? "\n• Custom Message: (${strCapitalize(settings?."${pName}UseCustomSpeechNotifMsg")})" : ""
	}
	return (str != "") ? "${str}" : null
}

def getAlarmNotifConfigDesc(pName) {
	def str = ""
	if(settings?."${pName}NotificationsOn" && settings["${pName}AllowAlarmNotif"]) {
		def alarms = getInputToStringDesc(settings["${pName}AlarmDevices"], true)
		str += alarms ? "\n • Alarm Devices:${alarms.size() > 1 ? "\n" : ""}${alarms}" : ""
	}
	return (str != "") ? "${str}" : null
}

def getAlertNotifConfigDesc(pName) {
	def str = ""
//TODO not sure we do all these
	if(settings?."${pName}NotificationsOn" && (settings["${pName}_Alert_1_Delay"] || settings["${pName}_Alert_2_Delay"]) && (settings["${pName}AllowSpeechNotif"] || settings["${pName}AllowAlarmNotif"])) {
		str += settings["${pName}_Alert_1_Delay"] ? "\nAlert (1) Status:\n  • Delay: (${getEnumValue(longTimeSecEnum(), settings["${pName}_Alert_1_Delay"])})" : ""
		str += settings["${pName}_Alert_1_Send_Push"] ? "\n  • Send Push: (${settings["${pName}_Alert_1_Send_Push"]})" : ""
		str += settings["${pName}_Alert_1_Use_Speech"] ? "\n  • Use Speech: (${settings["${pName}_Alert_1_Use_Speech"]})" : ""
		str += settings["${pName}_Alert_1_Use_Alarm"] ? "\n  • Use Alarm: (${settings["${pName}_Alert_1_Use_Alarm"]})" : ""
		str += (settings["${pName}_Alert_1_Use_Alarm"] && settings["${pName}_Alert_1_AlarmType"]) ? "\n ├ Alarm Type: (${getEnumValue(alarmActionsEnum(), settings["${pName}_Alert_1_AlarmType"])})" : ""
		str += (settings["${pName}_Alert_1_Use_Alarm"] && settings["${pName}_Alert_1_Alarm_Runtime"]) ? "\n └ Alarm Runtime: (${getEnumValue(shortTimeEnum(), settings["${pName}_Alert_1_Alarm_Runtime"])})" : ""
		str += settings["${pName}_Alert_2_Delay"] ? "${settings["${pName}_Alert_1_Delay"] ? "\n" : ""}\nAlert (2) Status:\n  • Delay: (${getEnumValue(longTimeSecEnum(), settings["${pName}_Alert_2_Delay"])})" : ""
		str += settings["${pName}_Alert_2_Send_Push"] ? "\n  • Send Push: (${settings["${pName}_Alert_2_Send_Push"]})" : ""
		str += settings["${pName}_Alert_2_Use_Speech"] ? "\n  • Use Speech: (${settings["${pName}_Alert_2_Use_Speech"]})" : ""
		str += settings["${pName}_Alert_2_Use_Alarm"] ? "\n  • Use Alarm: (${settings["${pName}_Alert_2_Use_Alarm"]})" : ""
		str += (settings["${pName}_Alert_2_Use_Alarm"] && settings["${pName}_Alert_2_AlarmType"]) ? "\n ├ Alarm Type: (${getEnumValue(alarmActionsEnum(), settings["${pName}_Alert_2_AlarmType"])})" : ""
		str += (settings["${pName}_Alert_2_Use_Alarm"] && settings["${pName}_Alert_2_Alarm_Runtime"]) ? "\n └ Alarm Runtime: (${getEnumValue(shortTimeEnum(), settings["${pName}_Alert_2_Alarm_Runtime"])})" : ""
	}
	return (str != "") ? "${str}" : null
}

def getInputToStringDesc(inpt, addSpace = null) {
	def cnt = 0
	def str = ""
	if(inpt) {
		inpt.sort().each { item ->
			cnt = cnt+1
			str += item ? (((cnt < 1) || (inpt?.size() > 1)) ? "\n    ${item}" : "${addSpace ? "    " : ""}${item}") : ""
		}
	}
	//log.debug "str: $str"
	return (str != "") ? "${str}" : null
}

def isPluralString(obj) {
	return (obj?.size() > 1) ? "(s)" : ""
}

def getRecipientsNames(val) {
	def n = ""
	def i = 0
	if(val) {
		//def valLabel =
		//log.debug "val: $val"
		val?.each { r ->
			i = i + 1
			n += i == val?.size() ? "${r}" : "${r},"
		}
	}
	return n?.toString().replaceAll("\\,", "\n")
}

def getRecipientDesc(pName) {
	return ((settings?."${pName}NotifRecips") || (settings?."${pName}NotifPhones" || settings?."${pName}NotifUsePush")) ? getRecipientsNames(settings?."${pName}NotifRecips") : null
}

def setDayModeTimePage(params) {
	def pName = params.pName
	if(params?.pName) {
		atomicState.cursetDayModeTimePageData = params
	} else {
		pName = atomicState?.cursetDayModeTimePageData?.pName
	}
	dynamicPage(name: "setDayModeTimePage", title: "Select Days, Times or Modes", uninstall: false) {
		def secDesc = settings["${pName}DmtInvert"] ? "Not" : "Only"
		def inverted = settings["${pName}DmtInvert"] ? true : false
		section("") {
			input "${pName}DmtInvert", "bool", title: "When Not in Any of These?", defaultValue: false, submitOnChange: true, image: getAppImg("switch_icon.png")
		}
		section("${secDesc} During these Days, Times, or Modes:") {
			def timeReq = (settings?."${pName}StartTime" || settings."${pName}StopTime") ? true : false
			input "${pName}StartTime", "time", title: "Start time", required: timeReq, image: getAppImg("start_time_icon.png")
			input "${pName}StopTime", "time", title: "Stop time", required: timeReq, image: getAppImg("stop_time_icon.png")
			input "${pName}Days", "enum", title: "${inverted ? "Not": "Only"} These Days", multiple: true, required: false, options: timeDayOfWeekOptions(), image: getAppImg("day_calendar_icon2.png")
			input "${pName}Modes", "mode", title: "${inverted ? "Not": "Only"} in These Modes", multiple: true, required: false, image: getAppImg("mode_icon.png")
		}
		section("Switches:") {
			input "${pName}restrictionSwitchOn", "capability.switch", title: "Only execute when these switches are all ON", multiple: true, required: false, image: getAppImg("switch_on_icon.png")
			input "${pName}restrictionSwitchOff", "capability.switch", title: "Only execute when these switches are all OFF", multiple: true, required: false, image: getAppImg("switch_off_icon.png")
		}
	}
}

def getDayModeTimeDesc(pName) {
	def startTime = settings?."${pName}StartTime"
	def stopTime = settings?."${pName}StopTime"
	def dayInput = settings?."${pName}Days"
	def modeInput = settings?."${pName}Modes"
	def inverted = settings?."${pName}DmtInvert" ?: null
	def swOnInput = settings?."${pName}restrictionSwitchOn"
	def swOffInput = settings?."${pName}restrictionSwitchOff"
	def str = ""
	def days = getInputToStringDesc(dayInput)
	def modes = getInputToStringDesc(modeInput)
	def swOn = getInputToStringDesc(swOnInput)
	def swOff = getInputToStringDesc(swOffInput)
	str += ((startTime && stopTime) || modes || days) ? "${!inverted ? "When" : "When Not"}:" : ""
	str += (startTime && stopTime) ? "\n • Time: ${time2Str(settings?."${pName}StartTime")} - ${time2Str(settings?."${pName}StopTime")}" : ""
	str += days ? "${(startTime && stopTime) ? "\n" : ""}\n • Day${isPluralString(dayInput)}: ${days}" : ""
	str += modes ? "${((startTime && stopTime) || days) ? "\n" : ""}\n • Mode${isPluralString(modeInput)}: ${modes}" : ""
	str += swOn ? "${((startTime && stopTime) || days || modes) ? "\n" : ""}\n • Switch${isPluralString(swOnInput)} that must be on: ${getRestSwitch(swOnInput)}" : ""
	str += swOff ? "${((startTime && stopTime) || days || modes || swOn) ? "\n" : ""}\n • Switch${isPluralString(swOffInput)} that must be off: ${getRestSwitch(swOffInput)}" : ""
	str += (str != "") ? "\n\nTap to modify" : ""
	return str
}

def getRestSwitch(swlist) {
	def swDesc = ""
	def swCnt = 0
	def rmSwCnt = swlist?.size() ?: 0
	swlist?.sort { it?.displayName }?.each { sw ->
		swCnt = swCnt+1
		swDesc += "${swCnt >= 1 ? "${swCnt == rmSwCnt ? "\n   └" : "\n   ├"}" : "\n   └"} ${sw?.label}: (${strCapitalize(sw?.currentSwitch)})"
	}
	return (swDesc == "") ? null : "${swDesc}"
}

def getDmtSectionDesc(autoType) {
	return settings["${autoType}DmtInvert"] ? "Do Not Act During these Days, Times, or Modes:" : "Only Act During these Days, Times, or Modes:"
//TODO add switches to adjust schedule
}

/************************************************************************************************
|   					      AUTOMATION SCHEDULE CHECK 								|
*************************************************************************************************/

def autoScheduleOk(autoType) {
	try {
		def inverted = settings?."${autoType}DmtInvert" ? true : false
		def modeOk = true
		modeOk = (!settings?."${autoType}Modes" || ((isInMode(settings?."${autoType}Modes") && !inverted) || (!isInMode(settings?."${autoType}Modes") && inverted))) ? true : false

		//dayOk
		def dayOk = true
		def dayFmt = new SimpleDateFormat("EEEE")
		dayFmt.setTimeZone(getTimeZone())
		def today = dayFmt.format(new Date())
		def inDay = (today in settings?."${autoType}Days") ? true : false
		dayOk = (!settings?."${autoType}Days" || ((inDay && !inverted) || (!inDay && inverted))) ? true : false

		//scheduleTimeOk
		def timeOk = true
		if(settings?."${autoType}StartTime" && settings?."${autoType}StopTime") {
			def inTime = (timeOfDayIsBetween(settings?."${autoType}StartTime", settings?."${autoType}StopTime", new Date(), getTimeZone())) ? true : false
			timeOk = ((inTime && !inverted) || (!inTime && inverted)) ? true : false
		}

		def soFarOk = (modeOk && dayOk && timeOk) ? true : false
		def swOk = true
		if(soFarOk && settings?."${autoType}restrictionSwitchOn") {
			for(sw in settings["${autoType}restrictionSwitchOn"]) {
				if (sw.currentValue("switch") != "on") {
					swOk = false
					break
				}
			}
		}
		soFarOk = (modeOk && dayOk && timeOk && swOk) ? true : false
		if(soFarOk && settings?."${autoType}restrictionSwitchOff") {
			for(sw in settings["${autoType}restrictionSwitchOff"]) {
				if (sw.currentValue("switch") != "off") {
					swOk = false
					break
				}
			}
		}

		LogAction("autoScheduleOk( dayOk: $dayOk | modeOk: $modeOk | dayOk: ${dayOk} | timeOk: $timeOk | swOk: $swOk | inverted: ${inverted})", "info", false)
		return (modeOk && dayOk && timeOk && swOk) ? true : false
	} catch (ex) {
		log.error "${autoType}-autoScheduleOk Exception:", ex
		parent?.sendExceptionData(ex, "autoScheduleOk", true, getAutoType())
	}
}

/************************************************************************************************
|					      SEND NOTIFICATIONS VIA PARENT APP								|
*************************************************************************************************/
def sendNofificationMsg(msg, msgType, recips = null, sms = null, push = null) {
	LogAction("sendNofificationMsg($msg, $msgType, $recips, $sms, $push)", "trace", false)
	if(recips || sms || push) {
		parent?.sendMsg(msgType, msg, true, recips, sms, push)
		//LogAction("Send Push Notification to $recips", "info", true)
	} else {
		parent?.sendMsg(msgType, msg, true)
	}
}

/************************************************************************************************
|							GLOBAL Code | Logging AND Diagnostic							    |
*************************************************************************************************/

def sendEventPushNotifications(message, type, pName) {
	LogTrace("sendEventPushNotifications($message, $type, $pName)")
	if(settings["${pName}_Alert_1_Send_Push"] || settings["${pName}_Alert_2_Send_Push"]) {
//TODO this portion is never reached
		if(settings["${pName}_Alert_1_CustomPushMessage"]) {
			sendNofificationMsg(settings["${pName}_Alert_1_CustomPushMessage"].toString(), type, settings?."${pName}NotifRecips", settings?."${pName}NotifPhones", settings?."${pName}UsePush")
		} else {
			sendNofificationMsg(message, type, settings?."${pName}NotifRecips", settings?."${pName}NotifPhones", settings?."${pName}UsePush")
		}
	} else {
		sendNofificationMsg(message, type, settings?."${pName}NotifRecips", settings?."${pName}NotifPhones", settings?."${pName}UsePush")
	}
}

def sendEventVoiceNotifications(vMsg, pName, msgId, rmAAMsg=false, rmMsgId) {
	def allowNotif = settings?."${pName}NotificationsOn" ? true : false
	def allowSpeech = allowNotif && settings?."${pName}AllowSpeechNotif" ? true : false
	def ok2Notify = parent.getOk2Notify()

	LogAction("sendEventVoiceNotifications($vMsg, $pName) ok2Notify: $ok2Notify", "trace", false)
	if(allowNotif && allowSpeech) {
		if(ok2Notify && (settings["${pName}SpeechDevices"] || settings["${pName}SpeechMediaPlayer"])) {
			sendTTS(vMsg, pName)
		}
		if(settings["${pName}SendToAskAlexaQueue"]) {		// we queue to Alexa regardless of quiet times
			if(rmMsgId != null && rmAAMsg == true) {
				removeAskAlexaQueueMsg(rmMsgId)
			}
			if (vMsg && msgId != null) {
				addEventToAskAlexaQueue(vMsg, msgId)
			}
		}
	}
}

def addEventToAskAlexaQueue(vMsg, msgId, queue=null) {
	if(parent?.getAskAlexaMQEn() == true) {
		if(parent.getAskAlexaMultiQueueEn()) {
			LogAction("sendEventToAskAlexaQueue: Adding this Message to the Ask Alexa Queue ($queues): ($vMsg)|${msgId}", "info", true)
			sendLocationEvent(name: "AskAlexaMsgQueue", value: "${app?.label}", isStateChange: true, descriptionText: "${vMsg}", unit: "${msgId}", data:queues)
		} else {
			LogAction("sendEventToAskAlexaQueue: Adding this Message to the Ask Alexa Queue: ($vMsg)|${msgId}", "info", true)
			sendLocationEvent(name: "AskAlexaMsgQueue", value: "${app?.label}", isStateChange: true, descriptionText: "${vMsg}", unit: "${msgId}")
		}
	}
}

def removeAskAlexaQueueMsg(msgId, queue=null) {
	if(parent?.getAskAlexaMQEn() == true) {
		if(parent.getAskAlexaMultiQueueEn()) {
			LogAction("removeAskAlexaQueueMsg: Removing Message ID (${msgId}) from the Ask Alexa Queue ($queues)", "info", true)
			sendLocationEvent(name: "AskAlexaMsgQueueDelete", value: "${app?.label}", isStateChange: true, unit: msgId, data: queues)
		} else {
			LogAction("removeAskAlexaQueueMsg: Removing Message ID (${msgId}) from the Ask Alexa Queue", "info", true)
			sendLocationEvent(name: "AskAlexaMsgQueueDelete", value: "${app?.label}", isStateChange: true, unit: msgId)
		}
	}
}


def scheduleAlarmOn(autoType) {
	LogAction("scheduleAlarmOn: autoType: $autoType a1DelayVal: ${getAlert1DelayVal(autoType)}", "debug", true)
	def timeVal = getAlert1DelayVal(autoType).toInteger()
	def ok2Notify = parent.getOk2Notify()

	LogAction("scheduleAlarmOn timeVal: $timeVal ok2Notify: $ok2Notify", "info", true)
	if(canSchedule() && ok2Notify) {
		if(timeVal > 0) {
			runIn(timeVal, "alarm0FollowUp", [data: [autoType: autoType]])
			LogAction("scheduleAlarmOn: Scheduling Alarm Followup 0 in timeVal: $timeVal", "info", true)
			atomicState."${autoType}AlarmActive" = true
		} else { LogAction("scheduleAlarmOn: Did not schedule ANY operation timeVal: $timeVal", "error", true) }
	} else { LogAction("scheduleAlarmOn: Could not schedule operation timeVal: $timeVal", "error", true) }
}

def alarm0FollowUp(val) {
	def autoType = val.autoType
	LogAction("alarm0FollowUp: autoType: $autoType 1 OffVal: ${getAlert1AlarmEvtOffVal(autoType)}", "debug", true)
	def timeVal = getAlert1AlarmEvtOffVal(autoType).toInteger()
	LogAction("alarm0FollowUp timeVal: $timeVal", "info", true)
	if(canSchedule() && timeVal > 0 && sendEventAlarmAction(1, autoType)) {
		runIn(timeVal, "alarm1FollowUp", [data: [autoType: autoType]])
		LogAction("alarm0FollowUp: Scheduling Alarm Followup 1 in timeVal: $timeVal", "info", true)
	} else { LogAction ("alarm0FollowUp: Could not schedule operation timeVal: $timeVal", "error", true) }
}

def alarm1FollowUp(val) {
	def autoType = val.autoType
	LogAction("alarm1FollowUp autoType: $autoType a2DelayVal: ${getAlert2DelayVal(autoType)}", "debug", true)
	def aDev = settings["${autoType}AlarmDevices"]
	if(aDev) {
		aDev?.off()
		storeLastAction("Set Alarm OFF", getDtNow())
		LogAction("alarm1FollowUp: Turning OFF ${aDev}", "info", true)
	}
	def timeVal = getAlert2DelayVal(autoType).toInteger()
	//if(canSchedule() && (settings["${autoType}_Alert_2_Use_Alarm"] && timeVal > 0)) {
	if(canSchedule() && timeVal > 0) {
		runIn(timeVal, "alarm2FollowUp", [data: [autoType: autoType]])
		LogAction("alarm1FollowUp: Scheduling Alarm Followup 2 in timeVal: $timeVal", "info", true)
	} else { LogAction ("alarm1FollowUp: Could not schedule operation timeVal: $timeVal", "error", true) }
}

def alarm2FollowUp(val) {
	def autoType = val.autoType
	LogAction("alarm2FollowUp: autoType: $autoType 2 OffVal: ${getAlert2AlarmEvtOffVal(autoType)}", "debug", true)
	def timeVal = getAlert2AlarmEvtOffVal(autoType)
	if(canSchedule() && timeVal > 0 && sendEventAlarmAction(2, autoType)) {
		runIn(timeVal, "alarm3FollowUp", [data: [autoType: autoType]])
		LogAction("alarm2FollowUp: Scheduling Alarm Followup 3 in timeVal: $timeVal", "info", true)
	} else { LogAction ("alarm2FollowUp: Could not schedule operation timeVal: $timeVal", "error", true) }
}

def alarm3FollowUp(val) {
	def autoType = val.autoType
	LogAction("alarm3FollowUp: autoType: $autoType", "debug", true)
	def aDev = settings["${autoType}AlarmDevices"]
	if(aDev) {
		aDev?.off()
		storeLastAction("Set Alarm OFF", getDtNow())
		LogAction("alarm3FollowUp: Turning OFF ${aDev}", "info", true)
	}
	atomicState."${autoType}AlarmActive" = false
}

def alarmEvtSchedCleanup(autoType) {
	if(atomicState?."${autoType}AlarmActive") {
		LogAction("Cleaning Up Alarm Event Schedules autoType: $autoType", "info", true)
		def items = ["alarm0FollowUp","alarm1FollowUp", "alarm2FollowUp", "alarm3FollowUp"]
		items.each {
			unschedule("$it")
		}
		def val = [ autoType: autoType ]
		alarm3FollowUp(val)
	}
}

def sendEventAlarmAction(evtNum, autoType) {
	LogAction("sendEventAlarmAction evtNum: $evtNum autoType: $autoType", "info", true)
	try {
		def resval = false
		def allowNotif = settings?."${autoType}NotificationsOn" ? true : false
		def allowAlarm = allowNotif && settings?."${autoType}AllowAlarmNotif" ? true : false
		def aDev = settings["${autoType}AlarmDevices"]
		if(allowNotif && allowAlarm && aDev) {
			//if(settings["${autoType}_Alert_${evtNum}_Use_Alarm"]) {
				resval = true
				def alarmType = settings["${autoType}_Alert_${evtNum}_AlarmType"].toString()
				switch (alarmType) {
					case "both":
						atomicState?."${autoType}alarmEvt${evtNum}StartDt" = getDtNow()
						aDev?.both()
						storeLastAction("Set Alarm BOTH ON", getDtNow(), autoType)
						break
					case "siren":
						atomicState?."${autoType}alarmEvt${evtNum}StartDt" = getDtNow()
						aDev?.siren()
						storeLastAction("Set Alarm SIREN ON", getDtNow(), autoType)
						break
					case "strobe":
						atomicState?."${autoType}alarmEvt${evtNum}StartDt" = getDtNow()
						aDev?.strobe()
						storeLastAction("Set Alarm STROBE ON", getDtNow(), autoType)
						break
					default:
						resval = false
						break
				}
			//}
		}
	} catch (ex) {
		log.error "sendEventAlarmAction Exception: ($evtNum) - ", ex
		parent?.sendExceptionData(ex, "sendEventAlarmAction", true, getAutoType())
	}
	return resval
}

def alarmAlertEvt(evt) {
	LogAction("alarmAlertEvt: ${evt.displayName} Alarm State is Now (${evt.value})", "trace", true)
}

def getAlert1DelayVal(autoType) { return !settings["${autoType}_Alert_1_Delay"] ? 300 : (settings["${autoType}_Alert_1_Delay"].toInteger()) }
def getAlert2DelayVal(autoType) { return !settings["${autoType}_Alert_2_Delay"] ? 300 : (settings["${autoType}_Alert_2_Delay"].toInteger()) }

def getAlert1AlarmEvtOffVal(autoType) { return !settings["${autoType}_Alert_1_Alarm_Runtime"] ? 10 : (settings["${autoType}_Alert_1_Alarm_Runtime"].toInteger()) }
def getAlert2AlarmEvtOffVal(autoType) { return !settings["${autoType}_Alert_2_Alarm_Runtime"] ? 10 : (settings["${autoType}_Alert_2_Alarm_Runtime"].toInteger()) }

/*
def getAlarmEvt1RuntimeDtSec() { return !atomicState?.alarmEvt1StartDt ? 100000 : GetTimeDiffSeconds(atomicState?.alarmEvt1StartDt).toInteger() }
def getAlarmEvt2RuntimeDtSec() { return !atomicState?.alarmEvt2StartDt ? 100000 : GetTimeDiffSeconds(atomicState?.alarmEvt2StartDt).toInteger() }
*/

void sendTTS(txt, pName) {
	LogAction("sendTTS(data: ${txt})", "trace", true)
	try {
		def msg = txt.toString().replaceAll("\\[|\\]|\\(|\\)|\\'|\\_", "")
		def spks = settings?."${pName}SpeechDevices"
		def meds = settings?."${pName}SpeechMediaPlayer"
		def res = settings?."${pName}SpeechAllowResume"
		def vol = settings?."${pName}SpeechVolumeLevel"
		LogAction("sendTTS msg: $msg | speaks: $spks | medias: $meds | resume: $res | volume: $vol", "debug", true)
		if(settings?."${pName}AllowSpeechNotif") {
			if(spks) {
				spks*.speak(msg)
			}
			if(meds) {
				meds?.each {
					if(res) {
						def currentStatus = it.latestValue('status')
						def currentTrack = it.latestState("trackData")?.jsonValue
						def currentVolume = it.latestState("level")?.integerValue ? it.currentState("level")?.integerValue : 0
						if(vol) {
							it?.playTextAndResume(msg, vol?.toInteger())
						} else {
							it?.playTextAndResume(msg)
						}
					}
					else {
						it?.playText(msg)
					}
				}
			}
		}
	} catch (ex) {
		log.error "sendTTS Exception:", ex
		parent?.sendExceptionData(ex, "sendTTS", true, getAutoType())
	}
}

def scheduleTimeoutRestore(pName) {
	def timeOutVal = settings["${pName}OffTimeout"]?.toInteger()
	if(timeOutVal && !atomicState?."${pName}timeOutScheduled") {
		runIn(timeOutVal.toInteger(), "restoreAfterTimeOut", [data: [pName:pName]])
		LogAction("Mode Restoration Timeout Scheduled ${pName} (${getEnumValue(longTimeSecEnum(), settings?."${pName}OffTimeout")})", "info", true)
		atomicState."${pName}timeOutScheduled" = true
	}
}

def unschedTimeoutRestore(pName) {
	def timeOutVal = settings["${pName}OffTimeout"]?.toInteger()
	if(timeOutVal && atomicState?."${pName}timeOutScheduled") {
		unschedule("restoreAfterTimeOut")
		LogAction("Cancelled Scheduled Mode Restoration Timeout ${pName}", "info", true)
	}
	atomicState."${pName}timeOutScheduled" = false
}

def restoreAfterTimeOut(val) {
	def pName = val?.pName.value
	if(pName && settings?."${pName}OffTimeout") {
		switch(pName) {
			case "conWat":
				atomicState."${pName}timeOutScheduled" = false
				conWatCheck(true)
				break
			//case "leakWat":
				//leakWatCheck(true)
				//break
			case "extTmp":
				atomicState."${pName}timeOutScheduled" = false
				extTmpTempCheck(true)
				break
			default:
				LogAction("restoreAfterTimeOut no pName match ${pName}", "error", true)
				break
		}
	}
}

def checkThermostatDupe(tstatOne, tstatTwo) {
	def result = false
	if(tstatOne && tstatTwo) {
		def pTstat = tstatOne?.deviceNetworkId.toString()
		def mTstatAr = []
		tstatTwo?.each { ts ->
			mTstatAr << ts?.deviceNetworkId.toString()
		}
		if(pTstat in mTstatAr) { return true }
	}
	return result
}

def checkModeDuplication(modeOne, modeTwo) {
	def result = false
	if(modeOne && modeTwo) {
		modeOne?.each { dm ->
			if(dm in modeTwo) {
				result = true
			}
		}
	}
	return result
}

private getDeviceSupportedCommands(dev) {
	return dev?.supportedCommands.findAll { it as String }
}

def checkFanSpeedSupport(dev) {
	def req = ["lowSpeed", "medSpeed", "highSpeed"]
	def devCnt = 0
	def devData = getDeviceSupportedCommands(dev)
	devData.each { cmd ->
		if(cmd.name in req) { devCnt = devCnt+1 }
	}
	def speed = dev?.currentValue("currentState") ?: null
	//log.debug "checkFanSpeedSupport (speed: $speed | devCnt: $devCnt)"
	return (speed && devCnt == 3) ? true : false
}

def getTstatCapabilities(tstat, autoType, dyn = false) {
	try {
		def canCool = true
		def canHeat = true
		def hasFan = true
		if(tstat?.currentCanCool) { canCool = tstat?.currentCanCool.toBoolean() }
		if(tstat?.currentCanHeat) { canHeat = tstat?.currentCanHeat.toBoolean() }
		if(tstat?.currentHasFan) { hasFan = tstat?.currentHasFan.toBoolean() }

		atomicState?."${autoType}${dyn ? "_${tstat?.deviceNetworkId}_" : ""}TstatCanCool" = canCool
		atomicState?."${autoType}${dyn ? "_${tstat?.deviceNetworkId}_" : ""}TstatCanHeat" = canHeat
		atomicState?."${autoType}${dyn ? "_${tstat?.deviceNetworkId}_" : ""}TstatHasFan" = hasFan
	} catch (ex) {
		log.error "getTstatCapabilities Exception:", ex
		parent?.sendExceptionData(ex, "getTstatCapabilities", true, getAutoType())
	}
}

def getSafetyTemps(tstat, usedefault=true) {
	def minTemp = tstat?.currentState("safetyTempMin")?.doubleValue
	def maxTemp = tstat?.currentState("safetyTempMax")?.doubleValue
	if(minTemp == 0) {
		if(usedefault) { minTemp = (getTemperatureScale() == "C") ? 7 : 45 }
		else { minTemp = null }
	}
	if(maxTemp == 0) { maxTemp = null }
	if(minTemp || maxTemp) {
		return ["min":minTemp, "max":maxTemp]
	}
	return null
}

/*
def getComfortHumidity(tstat) {
	def maxHum = tstat?.currentValue("comfortHumidityMax") ?: 0
	if(maxHum) {
		//return ["min":minHumidity, "max":maxHumidity]
		return maxHum
	}
	return null
}
*/

def getComfortDewpoint(tstat, usedefault=true) {
	def maxDew = tstat?.currentState("comfortDewpointMax")?.doubleValue
	maxDew = maxDew ?: 0.0
	if(maxDew == 0.0) {
		if(usedefault) {
			maxDew = (getTemperatureScale() == "C") ? 19 : 66
			return maxDew.toDouble()
		}
		return null
	}
	return maxDew
}

def getSafetyTempsOk(tstat) {
	def sTemps = getSafetyTemps(tstat)
	//log.debug "sTempsOk: $sTemps"
	if(sTemps) {
		def curTemp = tstat?.currentTemperature?.toDouble()
		//log.debug "curTemp: ${curTemp}"
		if( ((sTemps?.min != null && sTemps?.min.toDouble() != 0) && (curTemp < sTemps?.min.toDouble())) || ((sTemps?.max != null && sTemps?.max?.toDouble() != 0) && (curTemp > sTemps?.max?.toDouble())) ) {
			return false
		}
	} // else { log.debug "getSafetyTempsOk: no safety Temps" }
	return true
}

def getGlobalDesiredHeatTemp() {
	def t0 = parent?.settings?.locDesiredHeatTemp?.toDouble()
	return t0 ?: null
}

def getGlobalDesiredCoolTemp() {
	def t0 = parent?.settings?.locDesiredCoolTemp?.toDouble()
	return t0 ?: null
}

def getClosedContacts(contacts) {
	if(contacts) {
		def cnts = contacts?.findAll { it?.currentContact == "closed" }
		return cnts ?: null
	}
	return null
}

def getOpenContacts(contacts) {
	if(contacts) {
		def cnts = contacts?.findAll { it?.currentContact == "open" }
		return cnts ?: null
	}
	return null
}

def getDryWaterSensors(sensors) {
	if(sensors) {
		def cnts = sensors?.findAll { it?.currentWater == "dry" }
		return cnts ?: null
	}
	return null
}

def getWetWaterSensors(sensors) {
	if(sensors) {
		def cnts = sensors?.findAll { it?.currentWater == "wet" }
		return cnts ?: null
	}
	return null
}

def isContactOpen(con) {
	def res = false
	if(con) {
		if(con?.currentSwitch == "on") { res = true }
	}
	return res
}

def isSwitchOn(dev) {
	def res = false
	if(dev) {
		dev?.each { d ->
			if(d?.currentSwitch == "on") { res = true }
		}
	}
	return res
}

def isPresenceHome(presSensor) {
	def res = false
	if(presSensor) {
		presSensor?.each { d ->
			if(d?.currentPresence == "present") { res = true }
		}
	}
	return res
}

def isSomebodyHome(sensors) {
	if(sensors) {
		def cnts = sensors?.findAll { it?.currentPresence == "present" }
		return cnts ? true : false
	}
	return false
}

def getTstatPresence(tstat) {
	def pres = "not present"
	if(tstat) { pres = tstat?.currentPresence }
	return pres
}

def setTstatMode(tstat, mode, autoType=null) {
	def result = false
	if(mode) {
		def curMode = tstat?.currentnestThermostatMode?.toString()
		if (curMode != mode) {
			try {
				if(mode == "auto") { tstat.auto(); result = true }
				else if(mode == "heat") { tstat.heat(); result = true }
				else if(mode == "cool") { tstat.cool(); result = true }
				else if(mode == "off") { tstat.off(); result = true }
				else {
					if(mode == "eco") {
						tstat.eco(); result = true
						LogTrace("setTstatMode mode action | type: $autoType")
						if(autoType) { sendEcoActionDescToDevice(tstat, autoType) } // THIS ONLY WORKS ON NEST THERMOSTATS
					}
				}
			}
			catch (ex) {
				log.error "setTstatMode() Exception: ${tstat?.label} does not support mode ${mode}; check IDE and install instructions", ex
				parent?.sendExceptionData(ex, "setTstatMode", true, getAutoType())
			}
		}

		if(result) { LogAction("setTstatMode: '${tstat?.label}' Mode set to (${strCapitalize(mode)})", "info", false) }
		else { LogAction("setTstatMode() | No Mode change: ${mode}", "info", true) }
	} else {
		LogAction("setTstatMode() | Invalid or Missing Mode received: ${mode}", "warn", true)
	}
	return result
}

def setMultipleTstatMode(tstats, mode, autoType=null) {
	def result = false
	if(tstats && mode) {
		tstats?.each { ts ->
			def retval
//			try {
				retval = setTstatMode(ts, mode, autoType)   // THERE IS A PROBLEM HERE IF MIRROR THERMOSTATS ARE NOT NEST
//			} catch (ex) {
//				log.error "setMultipleTstatMode() Exception:", ex
//				parent?.sendExceptionData(ex, "setMultipleTstatMode", true, getAutoType())
//			}

			if(retval) {
				LogAction("Setting ${ts?.displayName} Mode to (${mode})", "info", true)
				storeLastAction("Set ${ts?.displayName} to (${mode})", getDtNow(), autoType)
				result = true
			} else {
				LogAction("Failed Setting ${ts} Mode to (${mode})", "warn", true)
				return false
			}
		}
	} else {
		LogAction("setMultipleTstatMode(${tstats}, $mode, $autoType) | Invalid or Missing tstats or Mode received: ${mode}", "warn", true)
	}
	return result
}

def setTstatAutoTemps(tstat, coolSetpoint, heatSetpoint, pName, mir=null) {

	def retVal = false
	def setStr = "No thermostat device"
	def heatFirst
	def setHeat
	def setCool
	def hvacMode = "unknown"
	def reqCool
	def reqHeat
	def curCoolSetpoint
	def curHeatSetpoint

	if(tstat) {
		hvacMode = tstat?.currentnestThermostatMode.toString()
		LogAction("setTstatAutoTemps: [tstat: ${tstat?.displayName} | Mode: ${hvacMode} | coolSetpoint: ${coolSetpoint}${tUnitStr()} | heatSetpoint: ${heatSetpoint}${tUnitStr()}]", "info", true)

		retVal = true
		setStr = ""

		curCoolSetpoint = getTstatSetpoint(tstat, "cool")
		curHeatSetpoint = getTstatSetpoint(tstat, "heat")
		def diff = getTemperatureScale() == "C" ? 2.0 : 3.0
		reqCool = coolSetpoint?.toDouble() ?: null
		reqHeat = heatSetpoint?.toDouble() ?: null

		if(!reqCool && !reqHeat) { retVal = false; setStr += "Missing COOL and HEAT Setpoints" }

		if(hvacMode in ["auto"]) {
			if(!reqCool && reqHeat) { reqCool = (double) (curCoolSetpoint > (reqHeat + diff)) ? curCoolSetpoint : (reqHeat + diff) }
			if(!reqHeat && reqCool) { reqHeat = (double) (curHeatSetpoint < (reqCool - diff)) ? curHeatSetpoint : (reqCool - diff) }
			if((reqCool && reqHeat) && (reqCool >= (reqHeat + diff))) {
				if(reqHeat <= curHeatSetpoint) { heatFirst = true }
					else if(reqCool >= curCoolSetpoint) { heatFirst = false }
					else if(reqHeat > curHeatSetpoint) { heatFirst = false }
					else { heatFirst = true }
				if(heatFirst) {
					if(reqHeat != curHeatSetpoint) { setHeat = true }
					if(reqCool != curCoolSetpoint) { setCool = true }
				} else {
					if(reqCool != curCoolSetpoint) { setCool = true }
					if(reqHeat != curHeatSetpoint) { setHeat = true }
				}
			} else {
				setStr += " or COOL/HEAT is not separated by ${diff}"
				retVal = false
			}

		} else if(hvacMode in ["cool"] && reqCool) {
			if(reqCool != curCoolSetpoint) { setCool = true }

		} else if(hvacMode in ["heat"] && reqHeat) {
			if(reqHeat != curHeatSetpoint) { setHeat = true }

		} else {
			setStr += "incorrect HVAC Mode (${hvacMode}"
			retVal = false
		}
	}
	if(retVal) {
		if(heatFirst && setHeat) {
			setStr += "heatSetpoint: (${reqHeat}${tUnitStr()}) "
			if(reqHeat != curHeatSetpoint) {
				tstat?.setHeatingSetpoint(reqHeat)
				storeLastAction("Set ${tstat} Heat Setpoint ${reqHeat}${tUnitStr()}", getDtNow(), pName, tstat)
				if(mir) { mir*.setHeatingSetpoint(reqHeat) }
			}
		}
		if(setCool) {
			setStr += "coolSetpoint: (${reqCool}${tUnitStr()}) "
			if(reqCool != curCoolSetpoint) {
				tstat?.setCoolingSetpoint(reqCool)
				storeLastAction("Set ${tstat} Cool Setpoint ${reqCool}", getDtNow(), pName, tstat)
				if(mir) { mir*.setCoolingSetpoint(reqCool) }
			}
		}
		if(!heatFirst && setHeat) {
			setStr += "heatSetpoint: (${reqHeat}${tUnitStr()})"
			if(reqHeat != curHeatSetpoint) {
				tstat?.setHeatingSetpoint(reqHeat)
				storeLastAction("Set ${tstat} Heat Setpoint ${reqHeat}${tUnitStr()}", getDtNow(), pName, tstat)
				if(mir) { mir*.setHeatingSetpoint(reqHeat) }
			}
		}
		LogAction("setTstatAutoTemps() | Setting tstat [${tstat?.displayName} | mode: (${hvacMode}) | ${setStr}]", "info", true)
	} else {
		LogAction("setTstatAutoTemps() | Setting tstat [${tstat?.displayName} | mode: (${hvacMode}) | ${setStr}]", "warn", true)
	}
	return retVal
}


/******************************************************************************
*					Keep These Methods						  *
*******************************************************************************/
/*
def switchEnumVals() { return [0:"Off", 1:"On", 2:"On/Off"] }

def longTimeMinEnum() {
	def vals = [
		1:"1 Minute", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 10:"10 Minutes", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes",
		45:"45 Minutes", 60:"1 Hour", 120:"2 Hours", 240:"4 Hours", 360:"6 Hours", 720:"12 Hours", 1440:"24 Hours"
	]
	return vals
}
*/

def fanTimeSecEnum() {
	def vals = [
		60:"1 Minute", 120:"2 Minutes", 180:"3 Minutes", 240:"4 Minutes", 300:"5 Minutes", 600:"10 Minutes", 900:"15 Minutes", 1200:"20 Minutes"
	]
	return vals
}

def longTimeSecEnum() {
	def vals = [
		0:"Off", 60:"1 Minute", 120:"2 Minutes", 180:"3 Minutes", 240:"4 Minutes", 300:"5 Minutes", 600:"10 Minutes", 900:"15 Minutes", 1200:"20 Minutes", 1500:"25 Minutes",
		1800:"30 Minutes", 2700:"45 Minutes", 3600:"1 Hour", 7200:"2 Hours", 14400:"4 Hours", 21600:"6 Hours", 43200:"12 Hours", 86400:"24 Hours", 10:"10 Seconds(Testing)"
	]
	return vals
}

def shortTimeEnum() {
	def vals = [
		1:"1 Second", 2:"2 Seconds", 3:"3 Seconds", 4:"4 Seconds", 5:"5 Seconds", 6:"6 Seconds", 7:"7 Seconds",
		8:"8 Seconds", 9:"9 Seconds", 10:"10 Seconds", 15:"15 Seconds", 30:"30 Seconds", 60:"60 Seconds"
	]
	return vals
}

/*
def smallTempEnum() {
	def tempUnit = getTemperatureScale()
	def vals = [
		1:"1°${tempUnit}", 2:"2°${tempUnit}", 3:"3°${tempUnit}", 4:"4°${tempUnit}", 5:"5°${tempUnit}", 6:"6°${tempUnit}", 7:"7°${tempUnit}",
		8:"8°${tempUnit}", 9:"9°${tempUnit}", 10:"10°${tempUnit}"
	]
	return vals
}
*/

def switchRunEnum(addAlways = false) {
	def pName = schMotPrefix()
	def hasFan = atomicState?."${pName}TstatHasFan" ? true : false
	def vals = [
		1:"Heating and Cooling", 2:"With Fan Only", 3:"Heating", 4:"Cooling"
	]
	if(!hasFan) {
		vals = [
			1:"Heating and Cooling", 3:"Heating", 4:"Cooling"
		]
	}
	if(addAlways) {
		vals << [5:"Any Operating State"]
	}
	return vals
}

def fanModeTrigEnum() {
	def pName = schMotPrefix()
	def canCool = atomicState?."${pName}TstatCanCool" ? true : false
	def canHeat = atomicState?."${pName}TstatCanHeat" ? true : false
	def hasFan = atomicState?."${pName}TstatHasFan" ? true : false
	def vals = ["auto":"Auto", "cool":"Cool", "heat":"Heat", "eco":"Eco", "any":"Any Mode"]
	if(!canHeat) {
		vals = ["cool":"Cool", "eco":"Eco", "any":"Any Mode"]
	}
	if(!canCool) {
		vals = ["heat":"Heat", "eco":"Eco", "any":"Any Mode"]
	}
	return vals
}

def tModeHvacEnum(canHeat, canCool, canRtn=null) {
	def vals = ["auto":"Auto", "cool":"Cool", "heat":"Heat", "eco":"Eco"]
	if(!canHeat) {
		vals = ["cool":"Cool", "eco":"Eco"]
	}
	if(!canCool) {
		vals = ["heat":"Heat", "eco":"Eco"]
	}
	if(canRtn) {
		vals << ["rtnFromEco":"Return from ECO if in ECO"]
	}
	return vals
}

def alarmActionsEnum() {
	def vals = ["siren":"Siren", "strobe":"Strobe", "both":"Both (Siren/Strobe)"]
	return vals
}

def getEnumValue(enumName, inputName) {
	def result = "unknown"
	def resultList = []
	def inputIsList = getObjType(inputName) == "List" ? true : false
	if(enumName) {
		enumName?.each { item ->
			if(inputIsList) {
				inputName?.each { inp ->
					if(item?.key.toString() == inp?.toString()) {
						resultList.push(item?.value)
					}
				}
			} else
			if(item?.key.toString() == inputName?.toString()) {
				result = item?.value
			}
		}
	}
	if(inputIsList) {
		return resultList
	} else {
		return result
	}
}

def getSunTimeState() {
	def tz = TimeZone.getTimeZone(location.timeZone.ID)
	def sunsetTm = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", location?.currentValue('sunsetTime')).format('h:mm a', tz)
	def sunriseTm = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", location?.currentValue('sunriseTime')).format('h:mm a', tz)
	atomicState.sunsetTm = sunsetTm
	atomicState.sunriseTm = sunriseTm
}

def parseDt(format, dt) {
	def result
	def newDt = Date.parse("$format", dt)
	result = formatDt(newDt)
	//log.debug "result: $result"
	return result
}

def getDevOpt() {
	appSettings?.devOpt.toString() == "true" ? true : false
}

def devPageFooter(var, eTime) {
	def res = []
	if(getDevOpt()) {
		res += 	section() {
					paragraph "       Page Loads: (${atomicState?.usageMetricsStore["${var}"] ?: 0}) | LoadTime: (${eTime ? (now()-eTime) : 0}ms)"
				}
	}
	return res?.size() ? res : ""
}


def askAlexaImgUrl() { return "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/AskAlexa512.png" }

def savetoRemDiagChild(List newdata) {
	//LogTrace("savetoRemDiagChild($msg, $type, $logSrcType)")
	if(atomicState?.automationType == "remDiag") {
		def stateSz = getStateSizePerc()
		if(stateSz >= 75) {
			// this is log.xxxx to avoid looping/recursion
			log.warn "savetoRemDiagChild: log storage trimming state size is ${getStateSizePerc()}%"
		}
		if(newdata?.size() > 0) {
			def data = atomicState?.remDiagLogDataStore ?: []
			def cnt = 0
			while(data && stateSz >= 70 && cnt < 50) {
				data.remove(0)
				atomicState?.remDiagLogDataStore = data
				stateSz = getStateSizePerc()
				cnt += 1
			}
			newdata?.each { logItem ->
				data << logItem
				cnt -= 1
				//log.debug "item: $logItem"
				//def item = ["dt":getDtNow(), "type":type, "src":(logSrcType ?: "Not Set"), "msg":msg]
			}
			atomicState?.remDiagLogDataStore = data
			stateSz = getStateSizePerc()
			while(data && stateSz >= 75 && cnt < 50) {
				data.remove(0)
				atomicState?.remDiagLogDataStore = data
				stateSz = getStateSizePerc()
				cnt += 1
			}
			log.debug "(${data?.size()} | State: ${stateSz}%)"
		} else { log.error "bad call to savetoRemDiagChild - no data" }
	} else { Logger("bad call to savetoRemDiagChild - wrong automation") }
}

def getRemLogData() {
	try {
		def appHtml = ""
		def navHtml = ""
		def scrStr = ""
		def logData = atomicState?.remDiagLogDataStore
		def resultStr = ""
		def tf = new SimpleDateFormat("h:mm:ss a")
		tf.setTimeZone(getTimeZone())
		def logSz = logData?.size() ?: 0
		def cnt = 1
		// def navMap = [:]
		// navMap = ["key":cApp?.getLabel(), "items":["Settings", "State", "MetaData"]]
		// def navItems = navHtmlBuilder(navMap, appNum)
		// if(navItems?.html) { navHtml += navItems?.html }
		// if(navItems?.js) { scrStr += navItems?.js }
		if(logSz > 0) {
			logData?.sort { it?.dt }.reverse()?.each { logItem ->
				def tCls = ""
				switch(logItem?.type) {
					case "info":
						tCls = "label-info"
						break
					case "warn":
						tCls = "label-warning"
						break
					case "error":
						tCls = "label-danger"
						break
					case "trace":
						tCls = "label-default"
						break
					case "debug":
						tCls = "label-primary"
						break
					default:
						tCls = "label-primary"
						break
				}
				def srcCls = "defsrc-bg"
				if(logItem?.src.toString().startsWith("Manager")) {
					srcCls = "mansrc-bg"
				} else if(logItem?.src.toString().startsWith("Camera")) {
					srcCls = "camsrc-bg"
				} else if(logItem?.src.toString().startsWith("Protect")) {
					srcCls = "protsrc-bg"
				} else if(logItem?.src.toString().startsWith("Thermostat")) {
					srcCls = "tstatsrc-bg"
				} else if(logItem?.src.toString().startsWith("weather")) {
					srcCls = "weatsrc-bg"
				} else if(logItem?.src.toString().startsWith("Presence")) {
					srcCls = "pressrc-bg"
				} else if(logItem?.src.toString().startsWith("Automation")) {
					srcCls = "autosrc-bg"
				}
				resultStr += """
					${cnt > 1 ? "<br>" : ""}
					<div class="log-line">
						<span class="log-time">${tf?.format(logItem?.dt)}</span>:
						<span class="log-type $tCls">${logItem?.type}</span> |
						<span class="log-source ${srcCls}"> ${logItem?.src}</span>:
						<span class="log-msg"> ${logItem?.msg}</span>
					</div>
				"""
				cnt = cnt+1
			}
		} else {
			resultStr = "There are NO log entries available."
		}

		return """
			<head>
				<meta charset="utf-8">
				<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
				<meta name="description" content="NST - Logs">
				<meta name="author" content="Anthony S.">
				<meta http-equiv="cleartype" content="on">
				<meta name="MobileOptimized" content="320">
				<meta name="HandheldFriendly" content="True">
				<meta name="apple-mobile-web-app-capable" content="yes">

				<title>NST Diagnostics - Logs</title>

				<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
				<link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet">
				<script src="https://use.fontawesome.com/fbe6a4efc7.js"></script>
				<script src="https://fastcdn.org/FlowType.JS/1.1/flowtype.js"></script>
				<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/7.0.0/normalize.min.css">
				<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
				<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
				<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/hamburgers/0.9.1/hamburgers.min.css">
				<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
				<script src="https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/1.7.1/clipboard.min.js"></script>
				<script src="https://cdn.rawgit.com/eKoopmans/html2pdf/master/vendor/jspdf.min.js"></script>
				<script src="https://cdn.rawgit.com/eKoopmans/html2canvas/develop/dist/html2canvas.min.js"></script>
				<script src="https://cdn.rawgit.com/eKoopmans/html2pdf/master/src/html2pdf.js"></script>
				<link rel="stylesheet" href="https://rawgit.com/tonesto7/nest-manager/master/Documents/css/diagpages.css">
				<style>
				</style>
			</head>
			<body>
				<button onclick="topFunction()" id="scrollTopBtn" title="Go to top"><i class="fa fa-arrow-up centerText" aria-hidden="true"></i> Back to Top</button>
				<nav id="menu-page" class="pushy pushy-left" data-focus="#nav-key-item1">
					<div class="nav-home-btn centerText"><button id="goHomeBtn" class="btn-link" title="Go Back to Home Page"><i class="fa fa-home centerText" aria-hidden="true"></i> Go Home</button></div>
					<!--Include your navigation here-->
					${navHtml}
				</nav>
				<!-- Site Overlay -->
				<div class="site-overlay"></div>

				<!-- Your Content -->
				<div id="container">
					<!--Page Header Section -->
					<div id="top-hdr" class="navbar navbar-default navbar-fixed-top">
						<div class="centerText">
							<div class="row">
						  		<div class="col-xs-2">
							   		<div class="left-head-col pull-left">
								   		<div class="menu-btn-div">
									   		<div class="hamburger-wrap">
										   		<button id="menu-button" class="menu-btn hamburger hamburger--collapse hamburger--accessible" title="Menu" type="button">
											   		<span class="hamburger-box">
												   		<span class="hamburger-inner"></span>
											   		</span>
											   		<!--<span class="hamburger-label">Menu</span>-->
										   		</button>
									   		</div>
								   		</div>
							   		</div>
						   		</div>
							   	<div class="col-xs-8 centerText">
									<h3 class="title-text"><img class="logoIcn" src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/nst_manager_icon.png"> Logs</img></h3>
									<h6 style="font-size: 0.9em;">This Includes Automations, Device, Manager Logs</h6>
							   	</div>
						   		<div class="col-xs-2 right-head-col">
									<button id="rfrshBtn" type="button" class="btn refresh-btn pull-right" title="Refresh Page Content"><i id="rfrshBtnIcn" class="fa fa-refresh" aria-hidden="true"></i></button>
						   		</div>
						  	</div>
						</div>
					</div>
					<!-- Page Content -->
					<div id="page-content-wrapper">
						<div class="container">
						   <!--First Panel Section -->
						   <div id="main" class="panel-body">
								<div class="panel panel-primary">
									<div class="panel-heading">
									 	<div class="row">
											<div class="col-xs-10" style="padding-left: 25px;">
										   		<div class="row">
											   		<h1 class="panel-title pnl-head-title pull-left">Log Stream</h1>
										   		</div>
										   		<div class="row">
											   		<small class="pull-left" style="text-decoration: underline;">${logSz} Items</small>
										   		</div>
									   		</div>
											<div class="col-xs-2" style="padding: 10px;">
											   	<button id="exportLogPdfBtn" type="button" title="Export Content as PDF" class="btn export-pdf-btn pull-right"><i id="exportPdfBtnIcn" class="fa fa-file-pdf-o" aria-hidden="true"></i> PDF</button>
									  		</div>
									 	</div>
									</div>
									<div class="panel-body" style="background-color: #DEDEDE;">
										<div id="logBody" class="logs-div">
											<div>${resultStr}</div>
								      	</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
				<script src="https://rawgit.com/tonesto7/nest-manager/master/Documents/js/diagpages.js"></script>
			</body>
		"""
/* """ */
	}  catch (ex) { log.error "getRemLogData Exception:", ex }
	return null
}

/*
def navHtmlBuilder(navMap, idNum) {
	def res = [:]
	def htmlStr = ""
	def jsStr = ""
	if(navMap?.key) {
		htmlStr += """\n<li><a id="nav-key-item${idNum}">${navMap?.key}<span class="icon"></span></a></li>"""
		jsStr += navJsBuilder("nav-key-item${idNum}", "key-item${idNum}")
	}
	if(navMap?.items) {
		def nItems = navMap?.items
		htmlStr += """\n<ul style="list-style-type: disc;">"""
		nItems?.each {
			htmlStr += """\n<li><a id="nav-subitem${idNum}-${it?.toString().toLowerCase()}">${it}<span class="icon"></span></a></li>"""
			jsStr += navJsBuilder("nav-subitem${idNum}-${it?.toString().toLowerCase()}", "item${idNum}-${it?.toString().toLowerCase()}")
		}
		htmlStr += """\n</ul>"""
	}
	htmlStr += """\n</br>"""
	res["html"] = htmlStr
	res["js"] = jsStr
	return res
}

def navJsBuilder(btnId, divId) {
	def res = """
		\$("#${btnId}").click(function() {
			\$('html, body').animate({ scrollTop: \$("#${divId}").offset().top - hdrHeight-20 }, 500);
		});
	"""
	return "\n${res}"
}

def clearRemDiagData(force=false) {
	atomicState?.remDiagLogDataStore = null
	//atomicState?.remDiagLogActivatedDt = null	// NOT done to have force off then on to re-enable
	LogAction("Cleared Diag data", "info", true)
}
*/

/*

//Things that I need to clear up on updates go here
//IMPORTANT: This must be run in it's own thread, and exit after running as the cleanup occurs on exit
def stateCleanup() {
	LogAction("stateCleanup", "trace", true)

	def data = [ "exLogs", "pollValue", "pollStrValue", "pollWaitVal", "tempChgWaitVal", "cmdDelayVal", "testedDhInst", "missedPollNotif", "updateMsgNotif", "updChildOnNewOnly", "disAppIcons",
		"showProtAlarmStateEvts", "showAwayAsAuto", "cmdQ", "recentSendCmd", "currentWeather", "altNames", "locstr", "custLocStr", "autoAppInstalled", "nestStructures", "lastSentExceptionDataDt",
		"tDevVer", "pDevVer", "camDevVer", "presDevVer", "weatDevVer", "vtDevVer", "dashSetup", "dashboardUrl", "apiIssues", "stateSize", "haveRun", "lastStMode", "lastPresSenAway", "automationsActive",
		"temperatures", "powers", "energies", "use24Time", "useMilitaryTime", "advAppDebug", "appDebug", "awayModes", "homeModes", "childDebug" ]
	data.each { item ->
		state.remove(item?.toString())
	}

	if(!atomicState?.cmdQlist) {
		data = [ "cmdQ2", "cmdQ3", "cmdQ4", "cmdQ5", "cmdQ6", "cmdQ7", "cmdQ8", "cmdQ9", "cmdQ10", "cmdQ11", "cmdQ12", "cmdQ13", "cmdQ14", "cmdQ15", "lastCmdSentDt2", "lastCmdSentDt3",
			"lastCmdSentDt4", "lastCmdSentDt5", "lastCmdSentDt6", "lastCmdSentDt7", "lastCmdSentDt8", "lastCmdSentDt9", "lastCmdSentDt10", "lastCmdSentDt11", "lastCmdSentDt12", "lastCmdSentDt13",
			"lastCmdSentDt14", "lastCmdSentDt15", "recentSendCmd2", "recentSendCmd3", "recentSendCmd4", "recentSendCmd5", "recentSendCmd6", "recentSendCmd7", "recentSendCmd8", "recentSendCmd9",
			"recentSendCmd10", "recentSendCmd11", "recentSendCmd12", "recentSendCmd13", "recentSendCmd14", "recentSendCmd15" ]
		data.each { item ->
			state.remove(item?.toString())
		}
	}
	def sdata = [ "showAwayAsAuto", "temperatures", "powers", "energies", "childDevDataPageDev", "childDevDataRfsh", "childDevDataStateFilter", "childDevPageShowAttr", "childDevPageShowCapab", "childDevPageShowCmds" ]
	sdata.each { item ->
		if(settings?."${item}" != null) {
			app.updateSetting("${item.toString()}", "")   // clear settings
		}
	}
}
*/

/******************************************************************************
*								STATIC METHODS								  *
*******************************************************************************/

def getAutoAppChildName()	{ return getChildName("Nest Automations") }
def getWatDogAppChildName()	{ return getChildName("Nest Location ${location.name} Watchdog") }

def getChildName(str)		{ return "${str}${appDevName()}" }

def getServerUrl()			{ return "https://graph.api.smartthings.com" }
def getShardUrl()			{ return getApiServerUrl() }
def getHelpPageUrl()			{ return "http://thingsthataresmart.wiki/index.php?title=Nest_Manager" }
def getAutoHelpPageUrl()		{ return "http://thingsthataresmart.wiki/index.php?title=Nest_Manager#Nest_Automations" }
def getAppImg(imgName, on = null)	{ return (!disAppIcons || on) ? "https://raw.githubusercontent.com/${gitPath()}/Images/App/$imgName" : "" }
def getDevImg(imgName, on = null)	{ return (!disAppIcons || on) ? "https://raw.githubusercontent.com/${gitPath()}/Images/Devices/$imgName" : "" }
def getChildAppVer(appName) { return appName?.appVersion() ? "v${appName?.appVersion()}" : "" }
def getUse24Time()			{ return useMilitaryTime ? true : false }

//Returns app State Info
def getStateSize() {
	def resultJson = new groovy.json.JsonOutput().toJson(state)
	return resultJson?.toString().length()
        //return state?.toString().length()
}
def getStateSizePerc()		{ return (int) ((stateSize / 100000)*100).toDouble().round(0) } //

def getLocationModes() {
	def result = []
	location?.modes.sort().each {
		if(it) { result.push("${it}") }
	}
	return result
}

def getDaysSinceInstall() {
	def start = Date.parse("E MMM dd HH:mm:ss z yyyy", atomicState?.installData.dt)
	def stop = new Date()
	if(start && stop) {
		return (stop - start)
	}
	return 0
}

def getObjType(obj) {
	if(obj instanceof String) {return "String"}
	else if(obj instanceof Map) {return "Map"}
	else if(obj instanceof List) {return "List"}
	else if(obj instanceof ArrayList) {return "ArrayList"}
	else if(obj instanceof Integer) {return "Integer"}
	else if(obj instanceof BigInteger) {return "BigInteger"}
	else if(obj instanceof Long) {return "Long"}
	else if(obj instanceof Boolean) {return "Boolean"}
	else if(obj instanceof BigDecimal) {return "BigDecimal"}
	else if(obj instanceof Float) {return "Float"}
	else if(obj instanceof Byte) {return "Byte"}
	else { return "unknown"}
}

def preStrObj() { [1:"•", 2:"│", 3:"├", 4:"└", 5:"    "] }

def getShowHelp() { return atomicState?.showHelp == false ? false : true }

def getTimeZone() {
	def tz = null
	if(location?.timeZone) { tz = location?.timeZone }
	else { tz = getNestTimeZone() ? TimeZone.getTimeZone(getNestTimeZone()) : null }
	if(!tz) { LogAction("getTimeZone: Hub or Nest TimeZone not found", "warn", true) }
	return tz
}

def formatDt(dt) {
	def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	else {
		LogAction("SmartThings TimeZone is not set; Please open your ST location and Press Save", "warn", true)
	}
	return tf.format(dt)
}

def getGlobTitleStr(typ) {
	return "Desired Default ${typ} Temp (°${getTemperatureScale()})"
}

def formatDt2(tm) {
	def formatVal = settings?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	return tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", tm.toString()))
}
/*
def genRandId(int length){
	String alphabet = new String("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz")
	int n = alphabet.length()
	String result = new String()
	Random r = new Random()
	for (int i=0; i<length; i++) { result = result + alphabet.charAt(r.nextInt(n)) }
	return result
}
*/

def tUnitStr() {
	return "°${getTemperatureScale()}"
}

def GetTimeDiffSeconds(strtDate, stpDate=null, methName=null) {
	//LogTrace("[GetTimeDiffSeconds] StartDate: $strtDate | StopDate: ${stpDate ?: "Not Sent"} | MethodName: ${methName ?: "Not Sent"})")
	if((strtDate && !stpDate) || (strtDate && stpDate)) {
		//if(strtDate?.contains("dtNow")) { return 10000 }
		def now = new Date()
		def stopVal = stpDate ? stpDate.toString() : formatDt(now)
/*
		def startDt = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate)
		def stopDt = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal)
		def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(startDt)).getTime()
*/
		def start = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate).getTime()
		def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal).getTime()
		def diff = (int) (long) (stop - start) / 1000 //
		LogTrace("[GetTimeDiffSeconds] Results for '$methName': ($diff seconds)")
		return diff
	} else { return null }
}

def daysOk(days) {
	if(days) {
		def dayFmt = new SimpleDateFormat("EEEE")
		if(getTimeZone()) { dayFmt.setTimeZone(getTimeZone()) }
		return days.contains(dayFmt.format(new Date())) ? false : true
	} else { return true }
}

def time2Str(time) {
	if(time) {
		def t = timeToday(time, getTimeZone())
		def f = new java.text.SimpleDateFormat("h:mm a")
		f.setTimeZone(getTimeZone() ?: timeZone(time))
		f.format(t)
	}
}

def epochToTime(tm) {
	def tf = new SimpleDateFormat("h:mm a")
		tf?.setTimeZone(getTimeZone())
	return tf.format(tm)
}

def getDtNow() {
	def now = new Date()
	return formatDt(now)
}

def modesOk(modeEntry) {
	def res = true
	if(modeEntry) {
		modeEntry?.each { m ->
			if(m.toString() == location?.mode.toString()) { res = false }
		}
	}
	return res
}

def isInMode(modeList) {
	if(modeList) {
		//log.debug "mode (${location.mode}) in list: ${modeList} | result: (${location?.mode in modeList})"
		return location.mode.toString() in modeList
	}
	return false
}

def notifValEnum(allowCust = true) {
	def valsC = [
		60:"1 Minute", 300:"5 Minutes", 600:"10 Minutes", 900:"15 Minutes", 1200:"20 Minutes", 1500:"25 Minutes", 1800:"30 Minutes",
		3600:"1 Hour", 7200:"2 Hours", 14400:"4 Hours", 21600:"6 Hours", 43200:"12 Hours", 86400:"24 Hours", 1000000:"Custom"
	]
	def vals = [
		60:"1 Minute", 300:"5 Minutes", 600:"10 Minutes", 900:"15 Minutes", 1200:"20 Minutes", 1500:"25 Minutes",
		1800:"30 Minutes", 3600:"1 Hour", 7200:"2 Hours", 14400:"4 Hours", 21600:"6 Hours", 43200:"12 Hours", 86400:"24 Hours"
	]
	return allowCust ? valsC : vals
}

def pollValEnum() {
	def vals = [
		60:"1 Minute", 120:"2 Minutes", 180:"3 Minutes", 240:"4 Minutes", 300:"5 Minutes",
		600:"10 Minutes", 900:"15 Minutes", 1200:"20 Minutes", 1500:"25 Minutes",
		1800:"30 Minutes", 2700:"45 Minutes", 3600:"60 Minutes"
	]
	return vals
}

def waitValEnum() {
	def vals = [
		1:"1 Second", 2:"2 Seconds", 3:"3 Seconds", 4:"4 Seconds", 5:"5 Seconds", 6:"6 Seconds", 7:"7 Seconds",
		8:"8 Seconds", 9:"9 Seconds", 10:"10 Seconds", 15:"15 Seconds", 30:"30 Seconds"
	]
	return vals
}

def strCapitalize(str) {
	return str ? str?.toString().capitalize() : null
}

def getInputEnumLabel(inputName, enumName) {
	def result = "Not Set"
	if(input && enumName) {
		enumName.each { item ->
			if(item?.key.toString() == inputName?.toString()) {
				result = item?.value
			}
		}
	}
	return result
}

def toJson(Map m) {
	return new org.json.JSONObject(m).toString()
}

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

/************************************************************************************************
|									LOGGING AND Diagnostic										|
*************************************************************************************************/
def lastN(String input, n) {
	return n > input?.size() ? null : n ? input[-n..-1] : ''
}
def LogTrace(msg, logSrc=null) {
	def trOn = (showDebug && advAppDebug) ? true : false
	if(trOn) {
		def theId = lastN(getId().toString(),5)
		def theLogSrc = (logSrc == null) ? (parent ? "Automation-${theId}" : "NestManager") : logSrc
		Logger(msg, "trace", theLogSrc, atomicState?.enRemDiagLogging)
	}
}

def LogAction(msg, type="debug", showAlways=false, logSrc=null) {
	def isDbg = showDebug ? true : false
	def theId = lastN(app.getId().toString(),5)
	def theLogSrc = (logSrc == null) ? (parent ? "Automation-${theId}" : "NestManager") : logSrc
	if(showAlways) { Logger(msg, type, theLogSrc) }
	else if(isDbg && !showAlways) { Logger(msg, type, theLogSrc) }
}

def Logger(msg, type, logSrc=null, noSTlogger=false) {
	if(msg && type) {
		def labelstr = ""
		if(atomicState?.debugAppendAppName == null) {
			def tval = parent ? parent?.settings?.debugAppendAppName : settings?.debugAppendAppName
			atomicState?.debugAppendAppName = (tval || tval == null) ? true : false
		}
		if(atomicState?.debugAppendAppName) { labelstr = "${app.label} | " }
		def themsg = "${labelstr}${msg}"
		if(!noSTlogger) {
			switch(type) {
				case "debug":
					log.debug "${themsg}"
					break
				case "info":
					log.info "||| ${themsg}"
					break
				case "trace":
					log.trace "| ${themsg}"
					break
				case "error":
					log.error "| ${themsg}"
					break
				case "warn":
					log.warn "|| ${themsg}"
					break
				default:
					log.debug "${themsg}"
					break
			}
		}
		//log.debug "Logger remDiagTest: $msg | $type | $logSrc"
		if(parent) {
			if(atomicState?.enRemDiagLogging == null) {
				atomicState?.enRemDiagLogging = parent?.state?.enRemDiagLogging
				if(atomicState?.enRemDiagLogging == null) {
					atomicState?.enRemDiagLogging = false
				}
				//log.debug "set enRemDiagLogging to ${atomicState?.enRemDiagLogging}"
			}
			if(atomicState?.enRemDiagLogging) {
				parent?.saveLogtoRemDiagStore(themsg, type, logSrc)
			}
		}
	}
	else { log.error "${labelstr}Logger Error - type: ${type} | msg: ${msg} | logSrc: ${logSrc}" }
}

///////////////////////////////////////////////////////////////////////////////
/******************************************************************************
|				Application Help and License Info Variables		  			  |
*******************************************************************************/
///////////////////////////////////////////////////////////////////////////////
def appName()		{ return "${appLabel()}" }
def appAuthor()		{ return "Anthony S." }
def appNamespace()	{ return "tonesto7" }
def appLabel()		{ return "NST Automations" }
def appParentName()	{ return "Nest Manager" }
def gitRepo()		{ return "tonesto7/nest-manager"}
def gitBranch()		{ return betaMarker() ? "beta" : "master" }
def gitPath()		{ return "${gitRepo()}/${gitBranch()}"}
def betaMarker()	{ return false }
def appDevType()	{ return false }
def appDevName()	{ return appDevType() ? " (Dev)" : "" }
def appInfoDesc()	{
	def cur = parent ? parent?.state?.appData?.updater?.versions?.autoapp?.ver.toString() : null
	def beta = betaMarker() ? " Beta" : ""
	def str = ""
	str += "${appName()}"
	str += isAppUpdateAvail() ? "\n• ${textVersion()} (Latest: v${cur})${beta}" : "\n• ${textVersion()}${beta}"
	str += "\n• ${textModified()}"
	return str
}
def textVersion()	{ return "Version: ${appVersion()}" }
def textModified()	{ return "Updated: ${appVerDate()}" }
def stIdeLink()		{ return "https://graph.api.smartthings.com" }
def appDesc()		{ return "This SmartApp is used to integrate your Nest devices with SmartThings and to enable built-in automations" }