/********************************************************************************************
|    Application Name: NST Manager                                                          |
|    Copyright (C) 2017 - 2019 Anthony S.                                                   |
|    Authors: Anthony S. (@tonesto7), Eric S. (@E_sch)                                      |
|    Contributors: Ben W. (@desertblade)                                                    |
|    A few code methods are modeled from those in CoRE by Adrian Caramaliu                  |
|                                                                                           |
|    License Info: https://github.com/tonesto7/nest-manager/blob/master/app_license.txt     |
|                                                                                           |
|    NOTE: I really hope that we don't have a ton of forks being released to the community, |
|    and that we can collaborate to make smartapps and devices that will accommodate to     |
|    all use cases                                                                          |
*********************************************************************************************/

import groovy.json.*
import java.text.SimpleDateFormat
import java.security.MessageDigest
include 'asynchttp_v1'

definition(
	name: "${appName()}",
	namespace: "${appNamespace()}",
	author: "${appAuthor()}",
	description: "${textDesc()}",
	category: "Convenience",
	iconUrl: "https://raw.githubusercontent.com/${gitPath()}/Images/App/nst_manager_5.png",
	iconX2Url: "https://raw.githubusercontent.com/${gitPath()}/Images/App/nst_manager_5%402x.png",
	iconX3Url: "https://raw.githubusercontent.com/${gitPath()}/Images/App/nst_manager_5%403x.png",
	singleInstance: true)

{
	appSetting "clientId"
	appSetting "clientSecret"
	appSetting "devOpt"
}

def appVersion() { "5.6.2" }
def appVerDate() { "04-09-2019" }
def minVersions() {
	return [
		"automation":["val":550, "desc":"5.5.0"],
		"thermostat":["val":545, "desc":"5.4.5"],
		"protect":["val":543, "desc":"5.4.3"],
		"presence":["val":544, "desc":"5.4.4"],
		"weather":["val":551, "desc":"5.5.1"],
		"camera":["val":544, "desc":"5.4.4"],
		"stream":["val":201, "desc":"2.0.1"]
	]
}

preferences {
	//startPage
	page(name: "startPage")

	//Manager Pages
	page(name: "authPage")
	page(name: "mainPage")
	page(name: "deviceSelectPage")
	page(name: "devWebTiles")
	page(name: "donationPage")
	page(name: "reviewSetupPage")
	page(name: "voiceRprtPrefPage")
	page(name: "changeLogPage")
	page(name: "prefsPage")
	page(name: "infoPage")
	page(name: "helpPage")
	page(name: "pollPrefPage")
	page(name: "debugPrefPage")
	page(name: "notifPrefPage")
	page(name: "devNamePage")
	page(name: "alarmTestPage")
	page(name: "simulateTestEventPage")
//	page(name: "devNameResetPage")
	page(name: "resetDiagQueuePage")
	page(name: "devPrefPage")
	page(name: "camMotionZoneFltrPage")
	page(name: "nestLoginPrefPage")
	page(name: "nestTokenResetPage")
	page(name: "uninstallPage")
	page(name: "forceUninstallPage")
	page(name: "custWeatherPage")
	page(name: "automationsPage")
	page(name: "automationKickStartPage")
	page(name: "automationGlobalPrefsPage")
	page(name: "automationStatisticsPage")
	page(name: "automationSchedulePage")
	page(name: "feedbackPage")
	page(name: "sendFeedbackPage")
	page(name: "codeUpdatesPage")
	page(name: "setNotificationPage")
	page(name: "notifConfigPage")
	page(name: "setNotificationTimePage")
	page(name: "restSrvcDiscovery", content: "restSrvcDiscovery")
}

mappings {
	if(!parent) {
		//used during Oauth Authentication
		path("/oauth/initialize") 	{action: [GET: "oauthInitUrl"]}
		path("/oauth/callback") 	{action: [GET: "callback"]}

		path("/deviceTiles")	{action: [GET: "renderDeviceTiles"]}
		path("/tstatTiles")		{action: [GET: "getTstatTiles"]}
		path("/protectTiles")	{action: [GET: "getProtectTiles"]}
		path("/cameraTiles")	{action: [GET: "getCamTiles"]}
		path("/weatherTile")	{action: [GET: "getWeatherTile"]}
		path("/renderInstallData")	{action: [GET: "renderInstallData"]}
		if(!(settings?.restStreamLocal && settings?.restStreamLocalHub)) {
			path("/receiveEventData") 	{action: [POST: "receiveEventData"]}
			path("/streamStatus")		{action: [POST: "receiveStreamStatus"]}
		}
		//Web Diagnostics Pages
		if(settings?.enDiagWebPage == true || getDevOpt()) {
			path("/processCmd") 	{action: [POST: "procDiagCmd"]}
			path("/diagHome")		{action: [GET: "renderDiagHome"]}
			path("/getLogData")		{action: [GET: "renderLogData"]}
			//path("/getLogMap")	{action: [GET: "getLogMap"]}
			path("/getManagerData")	{action: [GET: "renderManagerData"]}
			path("/getAutoData")	{action: [GET: "renderAutomationData"]}
			path("/getDeviceData")	{action: [GET: "renderDeviceData"]}
			path("/getInstData")	{action: [GET: "renderInstData"]}
			path("/getAppData")		{action: [GET: "renderAppData"]}
		}
	}
}

/******************************************************************************
|					Application Pages						|
*******************************************************************************/
//This Page is used to load either parent or child app interface code
def startPage() {
	atomicState?.isParent = true
	authPage()
}

def authPage() {
	//LogTrace("authPage()")
	def execTime = now()
	generateInstallId()
	if(!atomicState?.accessToken) { getAccessToken() }
	atomicState.ok2InstallAutoFlag = false
	if(!atomicState?.usageMetricsStore) { initAppMetricStore() }
	if(atomicState?.notificationPrefs == null) { atomicState?.notificationPrefs = buildNotifPrefMap() }
	def preReqOk = (atomicState?.preReqTested == true) ? true : preReqCheck()
	def stateSz = getStateSizePerc()
	if(!atomicState?.devHandlersTested) { deviceHandlerTest() }

	if(!atomicState?.accessToken || !nestDevAccountCheckOk() || (!atomicState?.isInstalled && (!atomicState?.devHandlersTested || !preReqOk)) || (stateSz > 80)) {
		return dynamicPage(name: "authPage", title: "Status Page", nextPage: "", install: (atomicState?.isInstalled == true ? true : false), uninstall: false) {
			section () {
				def title = ""
				def desc = ""
				def showWiki = false
				def wikiDesc = "View Instruction Projects Wiki"
				def wikiObj = ""
				if(!atomicState?.accessToken) {
					title = "OAuth Error"
					desc = "OAuth is not Enabled for ${appName()} application. Please click remove and review the installation directions again"
					wikiObj = "#Enabling_OAuth"
					wikiDesc = "Enabling Oauth (Wiki)"
				}
				else if(!nestDevAccountCheckOk()) {
					title = "Nest Developer Data Missing"
					desc = "Client ID and Secret\nAre both missing!\n\nThe built-in Client ID and Secret can no longer be provided.\n\nPlease visit the Wiki at the link below to resolve the issue."
					showWiki = true
					wikiObj = "#Nest_Developer_Account"
					wikiDesc = "Configure Nest Dev Account (Wiki)"
				}
				else if(!atomicState?.devHandlersTested) {
					desc = "Device Handlers are Missing or Not Published. Please verify the installation instructions and device handlers are present before continuing."
					wikiObj = "#Installation_Instructions"
					wikiDesc = "Device Installation (Wiki)"
				}
				else if(!preReqOk) {
					desc = "SmartThings Location is not returning (TimeZone: ${location?.timeZone}) or (ZipCode: ${location?.zipCode}) Please edit these settings under the ST IDE or Mobile App"
				}
				else {
					desc = "Application Status has not received any messages to display"
				}
				if(stateSz > 80) {
					desc += "${desc != "" ? "\n\n" : ""}Your Manager State Usage is Greater than 80% full. This is not normal and you should notify the developer."
					settingUpdate("enDiagWebPage", "true", "bool")
					href url: getAppEndpointUrl("diagHome"), style:"external", title:"NST Diagnostic Web Page", description:"Tap to view", required: true,state: "complete", image: getAppImg("web_icon.png")
				}
				LogAction("Status Message: $desc", "warn", true)
				paragraph title: title, "$desc", required: true, state: null
				if(showWiki) {
					href url: getWikiPageUrl()+wikiObj, style:"embedded", required:false, title:wikiDesc, description:"Tap to open in browser", state: "complete", image: getAppImg("web_icon.png")
				}
			}
			devPageFooter("authErrLoadCnt", execTime)
		}
	}
	def val = atomicState?.authToken ? (3600*4) : 300
	if(getLastWebUpdSec() > val.toInteger()) {
		updateWebStuff(true)
		setStateVar(true)
	}
	if(atomicState?.newSetupComplete && (atomicState?.appData?.updater?.versions?.app?.ver.toString() == appVersion())) {
		def result = ((atomicState?.appData?.updater?.setupVersion && !atomicState?.setupVersion) || (atomicState?.setupVersion?.toInteger() < atomicState?.appData?.updater?.setupVersion?.toInteger())) ? true : false
		if (result) { atomicState?.newSetupComplete = null }
	}
	if((settings.restStreamLocal == true) && (atomicState?.appData?.settings?.streaming.allowLocal != true)) { settingUpdate("restStreamLocal", "true", "bool") }
	def description
	def oauthTokenProvided = false

	if(atomicState?.authToken) {
		description = "You are connected."
		oauthTokenProvided = true
	} else { description = "Click to enter Nest Credentials" }

	if(!oauthTokenProvided && atomicState?.accessToken) {
		def redirectUrl = buildRedirectUrl
		//LogTrace("RedirectUrl = ${redirectUrl}")

		LogAction("AuthToken not found: Directing to Login Page", "info", true)
		return dynamicPage(name: "authPage", title: "Login Page", nextPage: "mainPage", install: false, uninstall: false) {
			section("") {
				paragraph appInfoDesc(), image: getAppImg("nst_manager_5%402x.png", true)
			}
			section(""){
				paragraph "Tap 'Login to Nest' below to authorize SmartThings to your Nest Account.\n\nAfter login you will be taken to the 'Works with Nest' page. Read the info and if you 'Agree' press the 'Accept' button."
				paragraph "❖ FYI: Please use the parent Nest account, Nest Family member accounts will not work correctly", state: "complete"
				href url: redirectUrl, style:"embedded", required: true, title: "Login to Nest", description: description
			}
			devPageFooter("authLoadCnt", execTime)
		}
	}

	else if(showChgLogOk()) { return changeLogPage() }
	else if(showDonationOk()) { return donationPage() }
	else { return mainPage() }
}

def mainPage() {
	//LogTrace("mainPage")
	def execTime = now()
	def isInstalled = atomicState?.isInstalled
	def setupComplete = (!atomicState?.newSetupComplete || !isInstalled) ? false : true
	return dynamicPage(name: "mainPage", title: "", nextPage: (!setupComplete ? "reviewSetupPage" : null), install: setupComplete, uninstall: false) {
		section("") {
			href "changeLogPage", title: "", description: "${appInfoDesc()}", image: getAppImg("nst_manager_5%402x.png", true)
			if(settings?.restStreaming) {
				Boolean strOn = atomicState?.restStreamingOn
				href "pollPrefPage", title: "", state: (strOn ? "complete" : null), image: getAppImg("two_way_icon.png"), description: "Nest Streaming: (${!strOn ? "Inactive" : "Active"})"
			}
			if(atomicState?.appData && !appDevType()) {
				if(isAppUpdateAvail()) {
					href url: stIdeLink(), style:"external", required: false, title:"An Update is Available for ${appName()}!",
						description:"Current: v${appVersion()} | New: ${atomicState?.appData?.updater?.versions?.app?.ver}\n\nTap to Open the IDE in Browser", state: "complete", image: getAppImg("update_icon.png")
				}
				if(atomicState?.cltBlacklisted) {
					paragraph "This ID is blacklisted, please update software!\nIf software is up to date, contact developer", required: true, state: null
				}
			}
		}
		if(isInstalled) {
			if(settings?.structures && !atomicState?.structures) { atomicState.structures = settings?.structures }
			section("Devices & Location:") {
				paragraph "Home/Away Status: (${strCapitalize(getLocationPresence() ?: "Not Available Yet!")})", title: "Location: ${atomicState?.structName}", state: "complete", image: getAppImg("home_icon.png")
				def t1 = getDevicesDesc(false)
				def devDesc = t1 ? "${t1}\n\nTap to modify devices" : "Tap to configure"
				href "deviceSelectPage", title: "Manage/View Devices", description: devDesc, state: "complete", image: "blank_icon.png"
				def devSelected = (atomicState?.structures && (atomicState?.thermostats || atomicState?.protects || atomicState?.cameras || atomicState?.presDevice || atomicState?.weatherDevice))
				if(devSelected) {
					href "devWebTiles", title: "View Device Tiles", description:"Tap to view", required: true,state: "complete", image: getAppImg("web_icon.png")
					href "devPrefPage", title: "Device Customization", description: "Tap to configure", image: getAppImg("device_pref_icon.png")
				}
			}
			//getDevChgDesc()
		}
		if(!isInstalled) {
			devicesPage()
		}
		if(isInstalled && atomicState?.structures && (atomicState?.thermostats || atomicState?.protects || atomicState?.cameras) && settings?.liteAppMode != true) {
			def t1 = getInstAutoTypesDesc()
			def autoDesc = t1 ? "${t1}\n\nTap to modify" : null
			section("Manage Automations:") {
				href "automationsPage", title: "Automations", description: (autoDesc ? autoDesc : "Tap to configure"), state: (autoDesc ? "complete" : null), image: getAppImg("nst_automations_5.png")
			}
		}
		if(isInstalled) {
			section("Notifications Options:") {
				def t1 = getAppNotifConfDesc()
				href "notifPrefPage", title: "Notifications", description: (t1 ? "${t1}\n\nTap to modify" : "Tap to configure"), state: (t1 ? "complete" : null),image: getAppImg("notification_icon2.png")
			}
			section("Manage Polling, Logging, Nest Login, and More:") {
				def descStr = ""
				def sz = descStr.size()
				def t1 = getAppNotifConfDesc()
				descStr += t1 ?: ""
				if(descStr.size() != sz) { descStr += "\n\n"; sz = descStr.size() }
				t1 = getAppDebugDesc()
				descStr += t1 ?: ""
				if(descStr.size() != sz) { descStr += "\n\n"; sz = descStr.size() }
				t1 = getPollingConfDesc()
				descStr += t1 ?: ""
				if(descStr.size() != sz) { descStr += "\n\n"; sz = descStr.size() }
				def prefDesc = (descStr != "") ? "" : "Tap to configure"
				href "prefsPage", title: "Application\nPreferences", description: prefDesc, state: (descStr ? "complete" : ""), image: getAppImg("settings_icon.png")
			}
			section("Donate, Release and License Info") {
				href "infoPage", title: "Info and More", description: "", image: getAppImg("info_bubble.png")
			}
			section("Having Trouble?:") {
				href "helpPage", title: "Get Help | Diagnostics", description: "", image: getAppImg("help_ring_icon.png")
				if(settings?.enDiagWebPage) {
					String diagTime = (getTimestampVal("remDiagLogActivatedDt") != null) ? "\n• Will Disable in:\n  └ ${getDiagLogTimeRemaining()}" : ""
					String diagStr = (settings?.enRemDiagLogging) ? "Diagnostic Logs: (ACTIVE)${diagTime}\n\nTap to view" : "Tap to view"
					href url: getAppEndpointUrl("diagHome"), style:"external", title:"NST Diagnostic Web Page", description: diagStr, required: true, state: "complete", image: getAppImg("web_icon.png")
				}
			}
			section("Remove All Apps, Automations, and Devices:") {
				href "uninstallPage", title: "Uninstall this App", description: "", image: getAppImg("uninstall_icon.png")
			}
		}
		atomicState.ok2InstallAutoFlag = false
		// storageInfoSect()
		devPageFooter("mainLoadCnt", execTime)
	}
}

// NEW STORAGE SmartApp
def storageInfoSect() {
	//Integer stateSz = getStateSizePerc()
	//if(!atomicState?.isInstalled || isAppLiteMode() || (stateSz < 50)) { return "" }
	def storApp = getStorageApp(false)
	section("Storage App Info:") {
		if(storApp) {
			def str = ""
			str += "Version: V${storApp?.appVersion()}"
			str += "\nUsage: ${storApp?.getStateSizePerc()}%"
			paragraph str, state: "complete"
		} else {
			paragraph "Storage SmartApp Is Not Installed..."
		}
	}
}

def getStorageVal(val) {
	if(val) {
		def storApp = getStorageApp()
		def ret
		if(storApp) { ret = storApp?.getStateVal(val as String) } // avoid multiple calls to child
		return ret
	}
	return null
}

def updStorageVal(sKey, sValue) {
	if(sKey) {
		def storApp = getStorageApp()
		if(storApp) { return storApp?.stateUpdate(sKey as String, sValue) }
	}
}

def remStorageVal(String sKey) {
	if(sKey) {
		def storApp = getStorageApp()
		if(storApp) { return storApp?.stateRemove(sKey as String) }
	}
}

def findStateStorageVal(String val) {
	def storVal = getStorageVal(val)
	def stateVal = atomicState?."$val"
	if(storVal) {
		if(stateVal) { state.remove(val) }
		return storVal
	} else if(stateVal) { return stateVal }
	return null
}

public storageAppInst(Boolean available) {
	atomicState?.storageAppAvailable = (available == true)
}

private getStorageApp(honorState = true) {
	Integer stateSz = getStateSizePerc()
	if(honorState && stateSz < 26) { return null }
	if(honorState && isAppLiteMode()) { return null }
	def storApp = getChildApps()?.find { it?.getAutomationType() == "storage" && it?.name == autoAppName() }
	if(storApp) {
		if(storApp?.label != getStorageAppChildLabel()) { storApp?.updateLabel(getStorageAppChildLabel()) }
		storageAppInst(true)
		return storApp
	} else {
		//runIn(5, "initStorageApp", [overwrite: true])
		storageAppInst(false)
		return null
	}
}

private checkStorageApp() {
	def oldStorApp = getChildApps()?.find{ it?.getAutomationType() == "storage" && it?.name != autoAppName() }
	if(oldStorApp) {
		LogAction("checkStorageApp | Removing Old Storage App", "warn", false)
		deleteChildApp(oldStorApp)
		updTimestampMap("lastAnalyticUpdDt", null)
	}
}

def donationPage() {
	return dynamicPage(name: "donationPage", title: "", nextPage: "mainPage", install: false, uninstall: false) {
		section("") {
			def str = ""
			str += "Hello User, \n\nPlease forgive the interuption but it's been 30 days since you installed/updated this SmartApp and we wanted to present you with this reminder that we do accept donations (We do not require them)."
			str += "\n\nIf you have been enjoying our software and devices please remember that we have spent thousand's of hours of our spare time working on features and stability for those applications and devices."
			str += "\n\nIf you have already donated please ignore and thank you very much for your support!"

			str += "\n\nThanks again for using NST Manager"
			paragraph str, required: true, state: null
			href url: textDonateLink(), style:"external", required: false, title:"Donations",
				description:"Tap to open in browser", state: "complete", image: getAppImg("donate_icon.png")
		}
		def iData = atomicState?.installData
		iData["shownDonation"] = true
		atomicState?.installData = iData
	}
}

def deviceSelectPage() {
	def execTime = now()
	return dynamicPage(name: "deviceSelectPage", title: "Device Selection", nextPage: "startPage", install: false, uninstall: false) {
		devicesPage()
		devPageFooter("devLocLoadCnt", execTime)
	}
}

def devicesPage() {
	def structs = getNestStructures()
	def isInstalled = atomicState?.isInstalled
	def structDesc = !structs?.size() ? "No Locations Found" : "Found (${structs?.size()}) Locations"
	//LogAction("${structDesc} (${structs})", "info", false)
	if (atomicState?.thermostats || atomicState?.protects || atomicState?.vThermostats || atomicState?.cameras || atomicState?.presDevice || atomicState?.weatherDevice ) {  // if devices are configured, you cannot change the structure until they are removed
		section("Location:") {
			paragraph "Nest Location Name: ${structs[atomicState?.structures]}${(structs.size() > 1) ? "\n(Remove All Devices to Change!)" : ""}", image: getAppImg("nest_structure_icon.png")
		}
	} else {
		section("Select Location:") {
			input(name: "structures", title:"Nest Locations", type: "enum", required: true, multiple: false, submitOnChange: true, metadata: [values:structs],
					image: getAppImg("nest_structure_icon.png"))
		}
	}
	if (settings?.structures) {
		atomicState.structures = settings?.structures
		def newStrucName = structs && structs?."${atomicState?.structures}" ? "${structs[atomicState?.structures]}" : null
		atomicState.structName = newStrucName ?: atomicState?.structName
		//atomicState.structName = (settings?.structures && atomicState?.structures) ? "${structs[atomicState?.structures]}" : null

		def stats = getNestThermostats()
		def statDesc = stats.size() ? "Found (${stats.size()}) Thermostats" : "No Thermostats"
		//LogAction("${statDesc} (${stats})", "info", false)

		def str = "devicePage"
		def dData = atomicState?.deviceData

		def t0 = [:]
		t0 = dData?.thermostats?.findAll { it?.key?.toString() in settings?.thermostats }
		def t1 = []
		t0?.each { devItem ->
			LogAction("${str}: Found (${devItem?.value?.name})", "info", false)
			if(devItem?.key && devItem?.value?.name) {
				t1 << "${devItem.key.toString()}"
			}
		}
		LogAction("${str} | Thermostats(${t0?.size()}): ${t1}", "info", true)

		def t3 = settings?.thermostats?.size() ?: 0
		if(t1?.size() != t3) {
			LogAction("Thermostat Counts Wrong! | Current: (${t1?.size()}) | Expected: (${t3})", "error", true);
			settingUpdate("thermostats", t1, "enum")
		}

		def coSmokes = getNestProtects()
		def coDesc = coSmokes.size() ? "Found (${coSmokes.size()}) Protects" : "No Protects"
		//LogAction("${coDesc} (${coSmokes})", "info", false)

		t0 = [:]
		t0 = dData?.smoke_co_alarms?.findAll { it?.key?.toString() in settings?.protects }
		t1 = []
		t0?.each { devItem ->
			LogAction("${str}: Found (${devItem?.value?.name})", "info", false)
			if(devItem?.key && devItem?.value?.name) {
				t1 << "${devItem.key.toString()}"
			}
		}
		LogAction("${str} | Protects(${t0?.size()}): ${t1}", "info", true)

		t3 = settings?.protects?.size() ?: 0
		if(t1?.size() != t3) {
			LogAction("Protects Counts Wrong! | Current: (${t1?.size()}) | Expected: (${t3})", "error", true);
			settingUpdate("protects", t1, "enum")
		}

		def cams = getNestCameras()
		def camDesc = cams.size() ? "Found (${cams.size()}) Cameras" : "No Cameras"
		//LogAction("${camDesc} (${cams})", "info", false)

		t0 = [:]
		t0 = dData?.cameras?.findAll { it?.key?.toString() in settings?.cameras }
		t1 = []
		t0?.each { devItem ->
			LogAction("${str}: Found (${devItem?.value?.name})", "info", false)
			if(devItem?.key && devItem?.value?.name) {
				t1 << "${devItem.key.toString()}"
			}
		}
		LogAction("${str} | Cameras(${t0?.size()}): ${t1}", "info", true)

		t3 = settings?.cameras?.size() ?: 0
		if(t1?.size() != t3) {
			LogAction("Cameras Counts Wrong! | Current: (${t1?.size()}) | Expected: (${t3})", "error", true);
			settingUpdate("cameras", t1, "enum")
		}

		section("Select Devices:") {
			if(!stats?.size() && !coSmokes.size() && !cams?.size()) { paragraph "No Devices were found" }
			if(stats?.size() > 0) {
				input(name: "thermostats", title:"Nest Thermostats", type: "enum", required: false, multiple: true, submitOnChange: true, metadata: [values:stats],
						image: getAppImg("thermostat_icon.png"))
			}
			atomicState.thermostats = settings?.thermostats ? statState(settings?.thermostats) : null
			if(coSmokes.size() > 0) {
				input(name: "protects", title:"Nest Protects", type: "enum", required: false, multiple: true, submitOnChange: true, metadata: [values:coSmokes],
						image: getAppImg("protect_icon.png"))
			}
			atomicState.protects = settings?.protects ? coState(settings?.protects) : null
			if(cams.size() > 0) {
				input(name: "cameras", title:"Nest Cameras", type: "enum", required: false, multiple: true, submitOnChange: true, metadata: [values:cams],
						image: getAppImg("camera_icon.png"))
			}
			atomicState.cameras = settings?.cameras ? camState(settings?.cameras) : null
			input(name: "presDevice", title:"Add Presence Device?\n", type: "bool", defaultValue: false, required: false, submitOnChange: true, image: getAppImg("presence_icon.png"))
			atomicState.presDevice = settings?.presDevice ?: null
			input(name: "weatherDevice", title:"Add Weather Device?\n", type: "bool", defaultValue: false, required: false, submitOnChange: true, image: getAppImg("weather_icon.png"))
			atomicState.weatherDevice = settings?.weatherDevice ?: null
		}
		if(isInstalled) {
			section("Device Web Tiles:") {
				href "devWebTiles", title: "View Device Tiles", description:"Tap to view", required: true,state: "complete", image: getAppImg("web_icon.png")
			}
			if(atomicState?.protects) {
				section("Nest Protect Alarm Simulation:") {
					def dt = atomicState?.isAlarmCoTestActiveDt
					href "alarmTestPage", title: "Test Protect Automations\nBy Simulating Alarm Events", description: "${dt ? "Last Tested:\n$dt\n\n" : ""}Tap to Begin...", image: getAppImg("test_icon.png")
				}
			}
		}
	}
}

def devWebTiles() {
	dynamicPage(name: "devWebTiles", title: "Device Tiles", uninstall: false) {
		section("Device Web Tiles:") {
			href url: getAppEndpointUrl("deviceTiles"), style:"external", title:"All Device Tiles\n(Take Longer to Load)", description:"Tap to view", required: true,state: "complete", image: getAppImg("view_icon.png")
			if(atomicState?.thermostats) { href url: getAppEndpointUrl("tstatTiles"), style:"external", title:"Thermostat Tiles", description:"Tap to view", required: true,state: "complete", image: getAppImg("thermostat_icon.png") }
			if(atomicState?.protects) { href url: getAppEndpointUrl("protectTiles"), style:"external", title:"Protect Tiles", description:"Tap to view", required: true,state: "complete", image: getAppImg("protect_icon.png") }
			if(atomicState?.cameras) { href url: getAppEndpointUrl("cameraTiles"), style:"external", title:"Camera Tiles", description:"Tap to view", required: true,state: "complete", image: getAppImg("camera_icon.png") }
			if(atomicState?.weatherDevice) { href url: getAppEndpointUrl("weatherTile"), style:"external", title:"Weather Tile", description:"Tap to view", required: true,state: "complete", image: getAppImg("weather_icon.png") }
		}
	}
}


def devPrefPage() {
	def execTime = now()
	dynamicPage(name: "devPrefPage", title: "Device Options", uninstall: false) {
		def devSelected = (atomicState?.structures && (atomicState?.thermostats || atomicState?.protects || atomicState?.cameras || atomicState?.presDevice || atomicState?.weatherDevice))
		if(devSelected) {
			section("Customize Device Names:") {
				def devDesc = (atomicState?.devNameOverride && (atomicState?.custLabelUsed || atomicState?.useAltNames)) ? "Custom Labels Set\n\nTap to modify" : "Tap to configure"
				href "devNamePage", title: "Device Names", description: devDesc, state:(atomicState?.custLabelUsed || atomicState?.useAltNames) ? "complete" : "", image: getAppImg("device_name_icon.png")
			}
		}
		if(atomicState?.cameras) {
			section("Camera Devices:") {
			//	if(getDevOpt() || betaMarker()) {
					input "camTakeSnapOnEvt", "bool", title: "Take Snapshot on Motion Events?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("snapshot_icon.png")
					input "motionSndChgWaitVal", "enum", title: "Delay before Motion/Sound Events are marked Inactive?", required: false, defaultValue: 60, metadata: [values:waitValAltEnum(true)], submitOnChange: true, image: getAppImg("delay_time_icon.png")
					input "camEnMotionZoneFltr", "bool", title: "Allow filtering motion events by configured zones?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("motion_icon.png")
					if(settings?.camEnMotionZoneFltr) {
						def camZones = getCamActivityZones()
						def t0 = camMotionZoneDesc()
						href "camMotionZoneFltrPage", title: "Restrict Motion to Certain Zones?", description: t0, params: [devices: atomicState?.cameras.sort{it?.value}, camZones: camZones], image: getAppImg("zone_icon.png"), state: (t0 ? "complete" : "")
					}
					atomicState.needChildUpd = true
			//	} else {
			//		paragraph "No Camera Device Options Yet..."
			//	}
			}
		}
		if(atomicState?.protects) {
			section("Protect Devices:") {
				input "showProtActEvts", "bool", title: "Show Non-Alarm Events in Activity Feed?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("list_icon.png")
				atomicState.needChildUpd = true
			}
		}
		if(atomicState?.thermostats) {
			section("Thermostat Devices:") {
				input ("tstatShowHistoryGraph", "bool", title: "Show Graph with Setpoint, Humidity, Temp History?", description: "This disables history collection", required: false, defaultValue: true, submitOnChange: true,
						image: getAppImg("graph_icon2.png"))
				input ("tempChgWaitVal", "enum", title: "Manual Temp Change Delay", required: false, defaultValue: 4, metadata: [values:waitValEnum()], submitOnChange: true, image: getAppImg("temp_icon.png"))
				atomicState.needChildUpd = true
			}
		}
		if(atomicState?.weatherDevice) {
			section("Weather Device:") {
				def t1 = getWeatherConfDesc()
				href "custWeatherPage", title: "Customize Weather Location?", description: (t1 ? "${t1}\n\nTap to modify" : ""), state: (t1 ? "complete":""), image: getAppImg("weather_icon_grey.png")
				input ("weathAlertNotif", "bool", title: "Local Weather Alerts?", description: "", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("weather_alert_icon.png"))
				input ("weatherShowGraph", "bool", title: "Weather History Graph?", description: "", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("graph_icon2.png"))
				atomicState.needChildUpd = true
			}
		}
		if(atomicState?.presDevice) {
			section("Presence Device:") {
				paragraph "No Presence Device Options Yet..."
				//atomicState.needChildUpd = true
			}
		}
		devPageFooter("devCustLoadCnt", execTime)
	}
}

def getCamActivityZones() {
	def camZones = [:]
	atomicState?.cameras.sort{it?.value}.each { cam ->
		camZones[cam?.key] = [:]
		def actZones = atomicState?.deviceData?.cameras[cam?.key]?.activity_zones
		if(actZones.size()) {
			actZones?.each { zn ->
				camZones[cam?.key][zn?.id as String] = zn?.name as String
			}
		}
	}
	return camZones
}

def camMotionZoneFltrPage(params) {
	def cams = params?.devices
	def camZones = params?.camZones
	if(params?.devices && params?.camZones) {
		atomicState.camFilterPageData = params
	} else {
		cams = atomicState?.camFilterPageData?.devices
		camZones = atomicState?.camFilterPageData?.camZones
	}
	def execTime = now()
	dynamicPage(name: "camMotionZoneFltrPage", title: "", nextPage: "devPrefPage", install: false) {
		if(cams && camZones) {
			cams?.each { cm->
				def zones = camZones[cm?.key]?.sort { it?.value }
				section("(${cm?.value}) Zones") {
					if(!zones?.size()) {
						paragraph "Camera has NO Zones..."
					} else {
						input("camera_${cm?.key}_zones", "enum", title:"Available Zones", description: "Found (${zones?.size()}) Zones", required: false, multiple: true, submitOnChange: true, options: zones, image: getAppImg("zone_icon.png"))
					}
				}
			}
		} else { section() { paragraph "NO Camera Zones Found..." } }
		atomicState.needChildUpd = true
		devPageFooter("camZoneFltLoadCnt", execTime)
	}
}

def camMotionZoneDesc() {
	def desc = ""
	if(atomicState?.cameras) {
		atomicState?.cameras.sort{it?.value}.each { cam ->
			if(settings?."camera_${cam?.key}_zones"?.size()) {
				desc += "${desc == "" ? "" : "\n"}${cam?.value}: (${settings?."camera_${cam?.key}_zones"?.size()}) Zones"
			}
		}
	}
	return desc == "" ? null : desc
}

def custWeatherPage() {
	def execTime = now()
	dynamicPage(name: "custWeatherPage", title: "", nextPage: "", install: false) {
		def objs = [:]
		def defZip = getStZipCode() ? getStZipCode() : getNestZipCode()
		section("") {
			input ("useCustWeatherLoc", "bool", title: "Use Custom Location?", description: "", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("info_icon2.png"))
		}
		if(settings?.useCustWeatherLoc) {
//ERS todo no more search
/*
			section("Select the Search method:") {
				input ("custWeatherLocSrch", "bool", title: "Use semi-automated search?", description: "", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("info_icon2.png"))
			}
			if(settings?.custWeatherLocSrch) {
				section("Set Custom Weather Location") {
					input("custLocSearchStr", "text", title: "Enter a location to search\nZipcode/City are valid", description: "The results will be available in the input below...", required: false, defaultValue: defZip, submitOnChange: true, image: getAppImg("weather_icon_grey.png"))
					if(settings?.custLocSearchStr != null || settings?.custLocSearchStr != "") {
						objs = getWeatherQueryResults(settings?.custLocSearchStr ? settings?.custLocSearchStr.toString() : null)
						if(objs?.size() > 0) {
							input(name: "custWeatherResultItems", title:"Search Results (Found: ${objs?.size()})", type: "enum", required: false, multiple: true, submitOnChange: true, metadata: [values:objs],
									image: getAppImg("search_icon.png"))
						}
					}
				}
			} else {
*/
				section("Manually Enter a Location:") {
//ERS todo no more WU can set string...
/*
					href url:"https://www.wunderground.com/weatherstation/ListStations.asp", style:"embedded", required:false, title:"Weather Station ID Lookup",
							description: "Lookup Weather Station ID", image: getAppImg("search_icon.png")
*/
					input("custLocStr", "text", title: "Manually Set Weather Location?", required: false, defaultValue: defZip, submitOnChange: true, image: getAppImg("weather_icon_grey.png"))
//ERS todo no more PWS
					//def validEnt = "\n\nWeather Stations: [pws:station_id]\nZipCodes: [90250]\nZWM: [zwm:zwm_number]"
					def validEnt = "ZipCodes: [90250]"
					paragraph "Valid location entries are:${validEnt}", image: getAppImg("blank_icon.png")
				}
//			}
		}
		updTimestampMap("lastWeatherUpdDt", 0)
		updTimestampMap("lastForecastUpdDt", 0)
		devPageFooter("custWeathLoadCnt", execTime)
	}
}

/*
def getWeatherQueryResults(query) {
	LogTrace("Getting Weather Query Results for '$query'")
	def objMap = [:]
	if(query) {
		def params = [uri: "http://autocomplete.wunderground.com/aq?query=${query.toString().encodeAsURL()}", contentType:"application/json", requestContentType:"application/json"]
		def data = getWebData(params, "weather location query", false)
		data?.RESULTS?.each { res ->
			log.debug "item: ${res?.name} | Zip: ${res?.zmw}"
			objMap[["zmw:${res?.zmw}"].join('.')] = res?.name.toString()
		}
	}
	return objMap
}
*/

def codeUpdatesPage(){
	dynamicPage(name: "codeUpdatesPage", uninstall: false, install: false) {
		def theURL = "https://consigliere-regional.api.smartthings.com/?redirect=" + URLEncoder.encode(getAppEndpointUrl("stupdate"))
		section() {
			def desc = "\bSmartApps:"
			desc += atomicState?.swVer?.mgrVer != null ? "${desc != "" ? "\n":""}Manager Version: (${atomicState?.swVer?.mgrVer})" : ""
			desc += atomicState?.swVer?.autoSaVer != null ? "${desc != "" ? "\n":""}Automations Version: (${atomicState?.swVer?.autoSaVer})" : ""
			desc += "\n\n\bDevices:"
			desc += atomicState?.swVer?.tDevVer != null ? "${desc != "" ? "\n":""} • Thermostat Version: (${atomicState?.swVer?.tDevVer})" : ""
			desc += atomicState?.swVer?.pDevVer != null ? "${desc != "" ? "\n":""} • Protect Version: (${atomicState?.swVer?.pDevVer})" : ""
			desc += atomicState?.swVer?.camDevVer != null ? "${desc != "" ? "\n":""} • Camera Version: (${atomicState?.swVer?.camDevVer})" : ""
			desc += atomicState?.swVer?.presDevVer != null ? "${desc != "" ? "\n":""} • Presence Version: (${atomicState?.swVer?.presDevVer})" : ""
			desc += atomicState?.swVer?.weatDevVer != null ? "${desc != "" ? "\n":""} • Weather Version: (${atomicState?.swVer?.weatDevVer})" : ""
			paragraph desc, state: "complete"
		}
		section() {
			paragraph title: "What will this do?", "This process makes sure the following are up-to-date:\n • All SmartApps\n • All Devices\n\nAll you will need to do is sign in to the IDE and watch it go..."
			href url: theURL, title: "Tap to Update", description: null, image: getAppImg("update_icon.png")
		}
		section("") {
			href "changeLogPage", title: "View Change Log", description: "Tap to view", image: getAppImg("change_log_icon.png")
		}
	}
}

def reviewSetupPage() {
	return dynamicPage(name: "reviewSetupPage", title: "Setup Review", install: true, uninstall: atomicState?.isInstalled) {
		if(!atomicState?.newSetupComplete) { atomicState.newSetupComplete = true }
		atomicState?.setupVersion = atomicState?.appData?.updater?.setupVersion?.toInteger() ?: 0
		section("Device Summary:") {
			def str = getDevicesDesc() ?: ""
			paragraph title: (!atomicState?.isInstalled ? "Devices Pending Install:" : "Installed Devices:"), "${str}"//, state: (str ? "complete" : null)
			if(atomicState?.weatherDevice) {
				if(!getStZipCode() || getStZipCode() != getNestZipCode()) {
					def wDesc = getWeatherConfDesc()
					def wmsg = "Please update ST zip codes - Nest and ST do not match"
					href "custWeatherPage", title: "Customize Weather Location?", description: (wDesc ? "${wDesc}\n\nTap to modify" : "${wmsg}"), state: ((wDesc || !wmsg) ? "complete":""), image: getAppImg("weather_icon_grey.png")
				}
			}
			if(!atomicState?.isInstalled && (settings?.thermostats || settings?.protects || settings?.cameras || settings?.presDevice || settings?.weatherDevice)) {
				def t1 = devCustomizePageDesc()
				href "devPrefPage", title: "Device Customization", description: (t1 ? "${t1}\n\nTap to modify" : "Tap to configure"),
						state: (t1 ? "complete" : null), image: getAppImg("device_pref_icon.png")
			}
		}
		//getDevChgDesc()

		showVoiceRprtPrefs()
		section("App Mode: (Full or Lite)") {
			def lmDesc = "Lite Mode will remove alot of the advanced features and allow for a very basic install. This will basically integrate Nest into ST without the bells and whistles."
			input ("liteAppMode", "bool", title: "Lite App Mode?", description: lmDesc, required: false, defaultValue: false, submitOnChange: true,
					image: getAppImg("app_analytics_icon.png"))
		}
		section("Notifications:") {
			def t1 = getAppNotifConfDesc()
			href "notifPrefPage", title: "Notifications", description: (t1 ? "${t1}\n\nTap to modify" : "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("notification_icon2.png")
		}
		section("Polling:") {
			def pollDesc = getPollingConfDesc()
			href "pollPrefPage", title: "Device | Structure\nPolling Preferences", description: (pollDesc != "" ? "${pollDesc}\n\nTap to modify" : "Tap to configure"), state: (pollDesc != "" ? "complete" : null), image: getAppImg("timer_icon.png")
		}
		showDevSharePrefs()
		section("") {
			href "infoPage", title: "Donations and Info", description: "Tap to view", image: getAppImg("info.png")
		}
		if(!atomicState?.isInstalled) {
			section("") {
				href "uninstallPage", title: "Uninstall this App", description: "Tap to Remove", image: getAppImg("uninstall_icon.png")
			}
		}
	}
}

def isAppLiteMode() {
	return (settings?.liteAppMode == true)
}

def showDevSharePrefs() {
	section("Share Data with Developer:") {
		paragraph title: "What is this used for?", "These options send non-user identifiable information and error data to diagnose or catch trending issues."
		input ("optInAppAnalytics", "bool", title: "Send Install Data?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("app_analytics_icon.png"))
		input ("optInSendExceptions", "bool", title: "Send Error Data?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("diag_icon.png"))
		if(settings?.optInAppAnalytics != false) {
			input(name: "mobileClientType", title:"Primary Mobile Device?", type: "enum", required: true, submitOnChange: true, metadata: [values:["android":"Android", "ios":"iOS", "winphone":"Windows Phone"]],
							image: getAppImg("${(settings?.mobileClientType && settings?.mobileClientType != "decline") ? "${settings?.mobileClientType}_icon" : "mobile_device_icon"}.png"))
			// if(mobileClientType == "android") {
				// input ("showHtmlOnAndroid", "bool", title: "Show HTML Content in Android Device Handlers?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("app_analytics_icon.png"))
			// }
			href url: getAppEndpointUrl("renderInstallData"), style:"embedded", title:"View the Data shared with Developer", description: "Tap to view Data", required:false, image: getAppImg("view_icon.png")
		}
	}
}

def helpPage () {
	def execTime = now()
	dynamicPage(name: "helpPage", title: "Help and Diagnostics", install: false) {
		section("App Info") {
			paragraph "Current State Usage:\n${getStateSizePerc()}% (${getStateSize()} bytes)", required: true, state: (getStateSizePerc() <= 70 ? "complete" : null),
					image: getAppImg("progress_bar.png")
			if(atomicState?.isInstalled && atomicState?.structures && (atomicState?.thermostats || atomicState?.protects || atomicState?.cameras || atomicState?.weatherDevice)) {
				input "enDiagWebPage", "bool", title: "Enable Diagnostic Web Page?", description: "", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("diagnostic_icon.png")
				if(settings?.enDiagWebPage) {
					href url: getAppEndpointUrl("diagHome"), style:"external", title:"NST Diagnostic Web Page", description:"Tap to view", required: true,state: "complete", image: getAppImg("web_icon.png")
				}
			}
		}
		if(getDevOpt()) {
			settingUpdate("enDiagWebPage","true", "bool")
		}
		if(settings?.enDiagWebPage) {
			section("How's Does Log Collection Work:", hideable: true, hidden: true) {
				paragraph title: "How will the log collection work?", "When logs are enabled this SmartApp will create a child diagnostic app to store your logs which you can view under the diagnostics web page or share the url with the developer for remote troubleshooting.\n\n Turn off to remove the diag app and all data."
			}
			section("Log Collection:") {
/*
				def formatVal = settings?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
				def tf = new SimpleDateFormat(formatVal)
				if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
*/
				paragraph "Logging will automatically turn off in 48 hours and all logs will be purged."
				input (name: "enRemDiagLogging", type: "bool", title: "Enable Log Collection?", required: false, defaultValue: (atomicState?.enRemDiagLogging ?: false), submitOnChange: true, image: getAppImg("log.png"))
				if(atomicState?.enRemDiagLogging) {
					def str = "Press Done/Save all the way back to the main smartapp page to allow the Diagnostic App to Install"
					paragraph str, required: true, state: "complete"
				}
			}
		}
		diagLogProcChange((settings?.enDiagWebPage && settings?.enRemDiagLogging))
		section("Help and Feedback:") {
			href url: getWikiPageUrl(), style:"embedded", required:false, title:"View the Projects Wiki", description:"Tap to open in browser", state: "complete", image: getAppImg("web_icon.png")
			href url: getIssuePageUrl(), style:"embedded", required:false, title:"Report | View Issues", description:"Tap to open in browser", state: "complete", image: getAppImg("issue_icon.png")
			href "feedbackPage", title: "Send Developer Feedback", description: "", image: getAppImg("feedback_icon.png")
		}
		section("SmartApp Security") {
			paragraph title:"What does resetting do?", "If you share a url with someone and want to remove their access you can reset your token and this will invalidate any URL you shared and create a new one for you."
			input (name: "resetSTAccessToken", type: "bool", title: "Reset SmartThings Access Token?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("reset_icon.png"))
			resetSTAccessToken(settings?.resetSTAccessToken == true)
		}
		devPageFooter("helpLoadCnt", execTime)
	}
}

def infoPage () {
	def execTime = now()
	dynamicPage(name: "infoPage", title: "Info and Instructions", install: false) {
		section("About this App:") {
			paragraph appInfoDesc(), image: getAppImg("nst_manager_5%402x.png", true)
		}
		section("Donations:") {
			href url: textDonateLink(), style:"external", required: false, title:"Donations",
				description:"Tap to open in browser", state: "complete", image: getAppImg("donate_icon.png")
		}
		section("Credits:") {
			paragraph title: "Creator:", "Anthony S. (@tonesto7)", state: "complete"
			paragraph title: "Co-Author:", "Eric S. (@E_Sch)", state: "complete"
			paragraph title: "Collaborator:", "Ben W. (@desertblade)", state: "complete"
		}
		section("App Change Details:") {
			href "changeLogPage", title: "View App Revision History", description: "Tap to view", image: getAppImg("change_log_icon.png")
		}
		section("Licensing Info:") {
			paragraph "${textCopyright()}\n${textLicense()}"
		}
		devPageFooter("infoLoadCnt", execTime)
	}
}

//Defines the Preference Page
def prefsPage() {
	def execTime = now()
	dynamicPage(name: "prefsPage", title: "Application Preferences", nextPage: "", install: false, uninstall: false ) {
		section("Polling:") {
			def t1 = getPollingConfDesc()
			href "pollPrefPage", title: "Device | Structure\nPolling Preferences", description: "${t1}\n\nTap to modify", state: (t1 != "" ? "complete" : null), image: getAppImg("timer_icon.png")
		}
		showVoiceRprtPrefs()

		showDevSharePrefs()

		def devSelected = (atomicState?.structures && (atomicState?.thermostats || atomicState?.protects || atomicState?.cameras || atomicState?.presDevice || atomicState?.weatherDevice))
		if(devSelected) {
			section("Device Preferences:") {
				href "devPrefPage", title: "Device Customization", description: "Tap to configure", image: getAppImg("device_pref_icon.png")
			}
		}
		section("Manage Nest Login:") {
			href "nestLoginPrefPage", title: "Nest Login Preferences", description: "Tap to view", image: getAppImg("login_icon.png")
		}
		section("App and Device Logging:") {
			def t1 = getAppDebugDesc()
			href "debugPrefPage", title: "Logging", description: (t1 ? "${t1 ?: ""}\n\nTap to modify" : "Tap to configure"), state: (t1) ? "complete" : null,
					image: getAppImg("log.png")
		}
		section ("Misc. Options:") {
			input ("useMilitaryTime", "bool", title: "Use Military Time (HH:mm)?", defaultValue: false, submitOnChange: true, required: false, image: getAppImg("military_time_icon.png"))
			input ("disAppIcons", "bool", title: "Disable App Icons?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("no_icon.png"))
			input ("liteAppMode", "bool", title: "Lite App Mode?", description: "Removes Automations", required: false, defaultValue: false, submitOnChange: true,
					image: getAppImg("app_analytics_icon.png"))
			atomicState.needChildUpd = true
		}
		section("Customize Application Label:") {
			label title:"Application Label (optional)", required:false
		}
		devPageFooter("prefLoadCnt", execTime)
	}
}

def voiceRprtPrefPage() {
	def execTime = now()
	return dynamicPage(name: "voiceRprtPrefPage", title: "Voice Report Preferences", install: false, uninstall: false) {
		section("Report Customization:") {
			paragraph "These options allow you to configure how much info is included in the Thermostat voice reporting."
			if(!atomicState?.appData?.settings?.reports?.disVoiceZoneRprt) {
				input ("vRprtIncSchedInfo", "bool", title: "Include Automation Source Schedule Info?", required: false, defaultValue: true, submitOnChange: false, image: getAppImg("nst_automations_5.png"))
				input ("vRprtIncZoneInfo", "bool", title: "Include Current Zone Info?", required: false, defaultValue: true, submitOnChange: false, image: getAppImg("thermostat_icon.png"))
				input ("vRprtIncExtWeatInfo", "bool", title: "Include External Info?", required: false, defaultValue: true, submitOnChange: false, image: getAppImg("weather_icon.png"))
			}
			if(!atomicState?.appData?.settings?.reports?.disVoiceUsageRprt) {
				input ("vRprtIncUsageInfo", "bool", title: "Include Usage Info?", required: false, defaultValue: true, submitOnChange: false, image: getAppImg("usage_icon.png"))
			}
		}
		devPageFooter("vRprtPrefLoadCnt", execTime)
	}
}

def pollPrefPage() {
	def execTime = now()
	dynamicPage(name: "pollPrefPage", title: "Polling Preferences", install: false) {
		section("Rest Streaming (Experimental):") {
			input(name: "restStreaming", title:"Enable Rest Streaming?", type: "bool", defaultValue: false, required: false, submitOnChange: true, image: getAppImg("two_way_icon.png"))
			if(!settings?.restStreaming) {
				paragraph title: "Streaming is an Experimental Feature (Even though it's Stable)", "It requires the install of our local NodeJS streaming service running on your home network."
				href url: streamLink(), style:"external", required: false, title:"Setup Instructions", description:"Tap to open in browser", state: "complete", image: getAppImg("web_icon.png")
			}
		}
		if(settings?.restStreaming) {
			section("Configure Streaming Service:") {
				href "restSrvcDiscovery", title: "Auto-Discover Local Service", state: (settings?.selectedRestDevice ? "complete" : null), image: getAppImg("search_icon.png"),
						description: selectedRestDiscSrvcDesc() ? "Selected Service:\n${selectedRestDiscSrvcDesc()}" : "Discover NST Service on your local network"
				if(!settings?.selectedRestDevice) {
					input(name: "restStreamIp", title:"Rest Service Address", type: "text", required: true, submitOnChange: true, image: getAppImg("ip_icon.png"))
					input(name: "restStreamPort", title:"Rest Service Port", type: "number", defaultValue: 3000, required: true, submitOnChange: true, image: getAppImg("port_icon.png"))
				}
				if(atomicState?.appData?.settings?.streaming.allowLocal == true) {
					input(name: "restStreamLocal", title:"Use Local Network to Send Events?", type: "bool", defaultValue: false, required: false, submitOnChange: true, image: getAppImg("two_way_icon.png"))
					if(settings?.restStreamLocal == true) {
						input(name: "restStreamLocalHub", type: "hub", title: "Select Local Hub", description: "This is the hub Stream events will be sent to.", submitOnChange: true, image: getAppImg("hub_icon.png"))
						if(settings?.restStreamLocal && settings?.restStreamLocalHub) { subscribe(location, null, lanStreamEvtHandler, [filterEvents:false]) }
					}
				}
				getRestSrvcDesc()
				paragraph title: "Notice", "This is still an experimental feature. It's subject to your local network and internet connections. If communication is lost the Manager will default back to standard polling."
			}
		} else {
			restDiscoveryClean()
		}
		startStopStream()

		section("Polling:") {
			if(settings?.restStreaming && getRestHost()) {
				paragraph title: "NOTICE!", "These settings are only used when rest streaming is inactive or disabled", required: true, state: null, image: getAppImg("info_icon2.png")
			}
			input ("pollValue", "enum", title: "Device Poll Rate", required: false, defaultValue: 180, metadata: [values:pollValEnum(true)], submitOnChange: true, image: getAppImg("thermostat_icon.png"))
			input ("pollStrValue", "enum", title: "Location Poll Rate", required: false, defaultValue: 180, metadata: [values:pollValEnum()], submitOnChange: true, image: getAppImg("nest_structure_icon.png"))
		}
		if(atomicState?.weatherDevice) {
			section("Weather Updates:") {
				input ("pollWeatherValue", "enum", title: "Weather Refresh Rate", required: false, defaultValue: 900, metadata: [values:notifValEnum()], submitOnChange: true, image: getAppImg("weather_icon.png"))
			}
		}
		section("Wait Values:") {
			input ("pollWaitVal", "enum", title: "Forced Poll Refresh Limit", required: false, defaultValue: 10, metadata: [values:waitValEnum()], submitOnChange: true, image: getAppImg("delay_time_icon.png"))
		}

		devPageFooter("pollPrefLoadCnt", execTime)
	}
}

def getRestSrvcDesc() {
	def rData = atomicState?.restServiceData
	def str = null
	if(rData) {
		str = ""
		def dtstr = ""
		if(rData?.startupDt) {
			def dt = rData?.startupDt
			dtstr += dt?.y ? "${dt?.y}yr., " : ""
			dtstr += dt?.mn ? "${dt?.mn}mon, " : ""
			dtstr += dt?.d ? "${dt?.d}days, " : ""
			dtstr += dt?.h ? "${dt?.h}hrs " : ""
			dtstr += dt?.m ? "${dt?.m}min " : ""
			dtstr += dt?.s ? "${dt?.s}sec" : ""
		}
		str += "${str == "" ? "" : "\n"}Host: (${rData?.hostInfo?.hostname})"
		if(rData?.hostInfo?.osPlatform) {
			def pd = parseJson(rData?.hostInfo?.osPlatform.toString())
			str += "\n ├ OS: ${pd?.dist} ${pd?.release}"
			str += "\n │├ Codename: ${pd?.codename}"
			str += "\n │└ Kernel: ${pd?.os.toString().capitalize()} ${rData?.hostInfo?.osRelease}"
		} else {
			str += "\n ├ OS: ${rData?.hostInfo?.osType} ${rData?.hostInfo?.osRelease ? "(${rData?.hostInfo?.osRelease})": ""}"
		}
		str += "\n ├ Memory: ${rData?.hostInfo?.memTotal} (${rData?.hostInfo?.memFree} free)"
		str += "\n ├ IP: (${rData?.hostInfo?.ip})"
		str += "\n ├ Port: (${rData?.hostInfo?.port})"
		str += "\n ├ Node Service: (v${rData?.version})"
		str += "\n ├ Active Streaming: (${rData?.streaming.toString().capitalize()})"
		str += "\n ${dtstr != "" ? "├" : "└"} Session Events: (${rData?.sessionEvts})"
		str += dtstr != "" ? "\n └ Uptime: ${dtstr.length() > 20 ? "\n     └ ${dtstr}" : "${dtstr}"}" : ""
		paragraph title: "Running Service Info:", str, state: "complete"
	}
	return str
}

def getRestSrvcUrn() { return "urn:schemas-upnp-org:service:NST-Streaming:1" }

def selectedRestDiscSrvcDesc() {
	if(!settings?.selectedRestDevice) {
		return null
	} else {
		def res = selectedRestDevice?.toString().split(":")
		return "IP: ${res[0]}\nPort: ${res[1]}"
	}
}

def restDiscoveryClean() {
	LogAction("Cleaning Out Discovered Services...", "trace", false)
	atomicState.localNstSrvcs = [:]		// verified Nest services
	atomicState.localRestSrvcs = [:]	// services that broadcasted, will be verified then added to localNstSrvcs list
	atomicState.discRfshCnt = 0
	app.updateSetting("selectedRestDevice", "")
}

def restSrvcDiscovery(params=[:]) {
	def devices = discoveredSrvcs()
	def options = devices ?: []
	def objsFound = options.size() ?: 0

	def discRfshCnt = (int) atomicState.discRfshCnt ?: 0
	atomicState?.discRfshCnt = discRfshCnt+1

	if ((objsFound == 0 && atomicState?.discRfshCnt > 25) || params?.reset == "true") {
		restDiscoveryClean()
	}

	restSrvcSubscribe()

	if((discRfshCnt % 5) == 0) {
		sendNstSrvcDiscovery()
	}
	if(((discRfshCnt % 3) == 0) && ((discRfshCnt % 5) != 0)) {
		verifyDevices()
	}
	//LogAction("options: $options devices: $devices objsFound $objsFound", "debug", true)

	return dynamicPage(name:"restSrvcDiscovery", title:"", nextPage:"", refreshInterval:5) {
		section("Please wait while we discover your local NST Service devices. Discovery can take a couple minutes or more, so sit back and relax! Select your service below once discovered.") {
			input "selectedRestDevice", "enum", required:false, title: "Local NST Services\n(Discovered: ${objsFound})", multiple: false, options: options, submitOnChange: true, image: getAppImg("two_way_icon.png")
		}
		section("Options") {
			href "restSrvcDiscovery", title:"Clear Discovered Services...", description:"", params: ["reset": "true"], image: getAppImg("reset_icon.png")
		}
	}
}

Map discoveredSrvcs() {
	def objs = getVerifiedSrvcs()
	def res = [:]
	objs?.each { res[it?.value?.host] = it?.value?.name }
	return res
}

def getVerifiedSrvcs() { getSrvcs().findAll { it?.value?.verified == true } }

def getSrvcs() {
	if (!atomicState?.localNstSrvcs) { atomicState.localNstSrvcs = [:] }
	atomicState?.localNstSrvcs
}

void restSrvcSubscribe() {
	if(atomicState?.ssdpOn != true) {
		LogAction("Enabling SSDP client", "info", true)
		atomicState.ssdpOn = true
		atomicState.localNstSrvcs = [:]
		atomicState.localRestSrvcs = [:]
		subscribe(location, "ssdpTerm.${getRestSrvcUrn()}", srvcBrdCastHandler)
	}
}

void sendNstSrvcDiscovery() {
	LogAction("Sending SSDP discovery broadcast to find local NST Service...", "info", true)
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery ${getRestSrvcUrn()}", physicalgraph.device.Protocol.LAN))
}

def getRestSrvcs() {
	if(!atomicState?.localRestSrvcs) { atomicState.localRestSrvcs = [:] }
	atomicState?.localRestSrvcs
}

void verifyDevices() {
	def devices = getRestSrvcs()
	devices.each {
		int port = convertHexToInt(it?.value?.deviceAddress)
		String ip = convertHexToIP(it?.value?.networkAddress)
		String host = "${ip}:${port}"
		sendHubCommand(new physicalgraph.device.HubAction("""GET ${it?.value?.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n""", physicalgraph.device.Protocol.LAN, host, [callback: srvcDescRespHandler]))
	}
}

def srvcBrdCastHandler(evt) {
	LogTrace("srvcBrdCastHandler")
	def description = evt.description
	def hub = evt?.hubId
	def parsedEvent = parseLanMessage(description)
	parsedEvent << ["hub":hub]

	//LogAction("parsedEvent: ${parsedEvent}", "info", false)

	def devices = getRestSrvcs()
	String ssdpUSN = parsedEvent?.ssdpUSN.toString()
	if (devices?."${ssdpUSN}") {
		//This area can be used to update service status in ST
	} else {
		def t0 = atomicState?.localRestSrvcs
		t0 << ["${ssdpUSN}": parsedEvent]
		atomicState?.localRestSrvcs = t0
	}
}

void srvcDescRespHandler(physicalgraph.device.HubResponse hubResponse) {
	LogTrace("srvcDescRespHandler")
	def body = hubResponse.xml

	if(!atomicState?.localNstSrvcs) { atomicState.localNstSrvcs = [:] }
	def t1 = body?.device?.UDN?.toString().split(":")
	def t0 = atomicState?.localNstSrvcs
	t0["${t1[1]}"] = ["name":"${body?.device?.hostName} (${body?.device?.serviceIp})", "host": "${body?.device?.serviceIp}:${body?.device?.servicePort}", "verified": true]
	atomicState?.localNstSrvcs = t0
}

def automationsPage() {
	def execTime = now()
	return dynamicPage(name: "automationsPage", title: "Installed Automations", nextPage: !parent ? "" : "automationsPage", install: false) {
		def autoApp = findChildAppByName( autoAppName() )
		def autoAppInst = isAutoAppInst()
		if(autoApp) { /*Nothing to add here yet*/ }
		else {
			section("") {
				paragraph "You haven't created any Automations yet!\nTap Create New Automation to get Started"
			}
		}
		section("") {
			app(name: "autoApp", appName: autoAppName(), namespace: "tonesto7", multiple: true, title: "Create New Automation (NST)", image: getAppImg("nst_automations_5.png"))
		}
		if(autoAppInst) {
			section("Automation Details:") {
				def schEn = getChildApps()?.findAll { (!(it.getAutomationType() in ["nMode", "watchDog", "remDiag", "storage"]) && it?.getActiveScheduleState()) }
				if(schEn?.size()) {
					href "automationSchedulePage", title: "View Automation Schedule(s)", description: "", image: getAppImg("schedule_icon.png")
				}
				href "automationStatisticsPage", title: "View Automation Statistics", description: "", image: getAppImg("app_analytics_icon.png")
			}
			section("Advanced Options: (Tap + to Show)                 ", hideable: true, hidden: true) {
				def descStr = ""
				descStr += (settings?.locDesiredCoolTemp || settings?.locDesiredHeatTemp) ? "Comfort Settings:" : ""
				descStr += settings?.locDesiredHeatTemp ? "\n • Desired Heat Temp: (${settings?.locDesiredHeatTemp}${tUnitStr()})" : ""
				descStr += settings?.locDesiredCoolTemp ? "\n • Desired Cool Temp: (${settings?.locDesiredCoolTemp}${tUnitStr()})" : ""
				descStr += (settings?.locDesiredComfortDewpointMax) ? "${(settings?.locDesiredCoolTemp || settings?.locDesiredHeatTemp) ? "\n\n" : ""}Dew Point:" : ""
				descStr += settings?.locDesiredComfortDewpointMax ? "\n • Max Dew Point: (${settings?.locDesiredComfortDewpointMax}${tUnitStr()})" : ""
				descStr += "${(settings?.locDesiredCoolTemp || settings?.locDesiredHeatTemp) ? "\n\n" : ""}${getSafetyValuesDesc()}" ?: ""
				def prefDesc = (descStr != "") ? "${descStr}\n\nTap to modify" : "Tap to configure"
				href "automationGlobalPrefsPage", title: "Global Automation Preferences", description: prefDesc, state: (descStr != "" ? "complete" : null), image: getAppImg("global_prefs_icon.png")
				input "disableAllAutomations", "bool", title: "Disable All Automations?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("disable_icon2.png")
				if(atomicState?.disableAllAutomations == false && settings?.disableAllAutomations) {
					toggleAllAutomations(true)

				} else if (atomicState?.disableAllAutomations && !settings?.disableAllAutomations) {
					toggleAllAutomations(true)
				}
				atomicState?.disableAllAutomations = settings?.disableAllAutomations == true ? true : false
				//input "enTstatAutoSchedInfoReq", "bool", title: "Allow Other Smart Apps to Retrieve Thermostat automation Schedule info?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("info_icon2.png")
				href "automationKickStartPage", title: "Re-Initialize All Automations", description: "Tap to Update All Automations", image: getAppImg("reset_icon.png")
			}
		}
		atomicState.ok2InstallAutoFlag = true

		devPageFooter("autoLoadCnt", execTime)
	}
}

def automationSchedulePage() {
	def execTime = now()
	dynamicPage(name: "automationSchedulePage", title: "View Schedule Data..", uninstall: false) {
		section() {
			def str = ""
			def tz = TimeZone.getTimeZone(location.timeZone.ID)
			def sunsetT = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", location.currentValue('sunsetTime')).format('h:mm a', tz)
			def sunriseT = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", location.currentValue('sunriseTime')).format('h:mm a', tz)
			str += "Mode: (${location?.mode})"
			str += "\nSunrise: (${sunriseT})"
			str += "\nSunset: (${sunsetT})"
			paragraph title: "SmartThings Location Info:", "$str", state: "complete"
		}
		def schMap = []
		def schSize = 0
		getChildApps()?.each { capp ->
			if(capp?.getStateVal("newAutomationFile") == null) { return }
			def schedActMap = [:]
			def schInfo = capp?.getScheduleDesc()
			if (schInfo?.size()) {
				schSize = schSize+1
				def curSch = capp?.getCurrentSchedule()
				schInfo?.each { schItem ->
					section("${capp?.label}") {
						def schNum = schItem?.key
						def schDesc = schItem?.value
						def schInUse = (curSch?.toInteger() == schNum?.toInteger()) ? true : false
						if(schNum && schDesc) {
							paragraph "${schDesc}", state: schInUse ? "complete" : ""
						}
					}
				}
			}
		}
		if(schSize < 1) {
			section("") {
				paragraph "There is No Schedule Data to Display"
			}
		}

		devPageFooter("viewAutoSchedLoadCnt", execTime)
	}
}

void processAutoSchedChgs() {
	def sMap = atomicState?.schedActMap
	if(sMap.size()) {
		getChildApps()?.each { capp ->
			sMap.each { cd ->
				def sKey = cd?.key
				def sVal = cd?.value
				log.debug "cd: $cd"
				def sLbl = "schMot_${sKey}_SchedActive"
				if(sVal != capp?.getSettingVal(sLbl)) {
					capp?.updateSchedActiveState(sKey, sVal)
				}
			}
		}
	}
}

def automationStatisticsPage() {
	def execTime = now()
	dynamicPage(name: "automationStatisticsPage", title: "Installed Automations Stats\n(Auto-Refreshes every 20 sec.)", refreshInterval: 20, uninstall: false) {
		def cApps = getChildApps()
		def aSize = 0
		if(cApps) {
			cApps?.sort()?.each { chld ->
				if(chld?.getStateVal("newAutomationFile") == null) { return }
				aSize = aSize+1
				def autoType = chld?.getAutomationType()
				section(" ") {
					paragraph "${chld?.label}", state: "complete", image: getAutoIcon(autoType)
					def data = chld?.getAutomationStats()
					def fmt = "M/d/yyyy - h:mm a"
/*
					def tf = new SimpleDateFormat("M/d/yyyy - h:mm a")
						tf.setTimeZone(getTimeZone())
					//def lastModDt = data?.lastUpdatedDt ? tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", data?.lastUpdatedDt.toString())) : null
					//def lastEvtDt = data?.lastEvent?.date ? tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", data?.lastEvent?.date.toString())) : null
					//def lastActionDt = data?.lastActionData?.dt ? tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", data?.lastActionData?.dt.toString())) : null
					//def lastEvalDt = data?.lastEvalDt ? tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", data?.lastEvalDt.toString())) : null
					//def lastSchedDt = data?.lastSchedDt ? tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", data?.lastSchedDt.toString())) : null
*/

					def lastModDt = data?.lastUpdatedDt ? formatDt2(data?.lastUpdatedDt.toString(), fmt) : null
					def lastEvtDt = data?.lastEvent?.date ? formatdt2(data?.lastEvent?.date.toString(), fmt) : null
					def lastActionDt = data?.lastActionData?.dt ? formatDt2(data?.lastActionData?.dt.toString(), fmt) : null
					def lastEvalDt = data?.lastEvalDt ? formatDt2(data?.lastEvalDt.toString(), fmt) : null
					def lastSchedDt = data?.lastSchedDt ? formatDt2(data?.lastSchedDt.toString(), fmt) : null

					def lastExecVal = data?.lastExecVal ?: null
					def execAvgVal = data?.execAvgVal ?: null

					def str = ""
					str += lastModDt ? "• Last Modified:\n  └ (${lastModDt})" : "\n • Last Modified: (Not Available)"
					str += lastEvtDt ? "\n\n• Last Event:" : ""
					str += lastEvtDt ? "${(data?.lastEvent?.displayName.length() > 20) ? "\n  │ Device:\n  │└ " : "\n  ├ Device: "}${data?.lastEvent?.displayName}" : ""
					str += lastEvtDt ? "\n  ├ Type: (${strCapitalize(data?.lastEvent?.name)})" : ""
					str += lastEvtDt ? "\n  ├ Value: (${data?.lastEvent?.value}${data?.lastEvent?.unit ? "${data?.lastEvent?.unit}" : ""})" : ""
					str += lastEvtDt ? "\n  └ DateTime: (${lastEvtDt})" : "\n\n • Last Event: (Not Available)"
					str += lastEvalDt ? "\n\n• Last Evaluation:\n  └ (${lastEvalDt})" : "\n\n • Last Evaluation: (Not Available)"
					str += lastSchedDt ? "\n\n• Last Schedule:\n  └ (${lastSchedDt})" : "\n\n • Last Schedule: (Not Available)"
					str += lastActionDt ? "\n\n• Last Action:\n  ├ DateTime: (${lastActionDt})\n  └ Action: ${data?.lastActionData?.actionDesc}" : "\n\n • Last Action: (Not Available)"
					str += lastExecVal ? "\n\n• Execution Info:\n  ${execAvgVal ? "├" : "└"} Last Time: (${lastExecVal} ms)${execAvgVal ? "\n  └ Avg. Time: (${execAvgVal} ms)" : ""}" : "\n\n • Execution Info: (Not Available)"
					paragraph "${str}", state: "complete"
				}
			}
		}
		if(aSize < 1) {
			section("") {
				paragraph "There is No Statistic Data to Display"
			}
		}
		devPageFooter("viewAutoStatLoadCnt", execTime)
	}
}

def locDesiredClear() {
	LogTrace("locDesiredClear")
	def list = [ "locDesiredHeatTemp", "locDesiredCoolTemp","locDesiredComfortDewpointMax", "locDesiredTempScale", "locDesiredButton" ]
	list.each { item ->
		settingRemove(item.toString())
	}
	if(atomicState?.thermostats && settings?.clearLocDesired) {
		atomicState?.thermostats?.each { ts ->
			def dev = getChildDevice(ts?.key)
			def canHeat = dev?.currentState("canHeat")?.stringValue == "false" ? false : true
			def canCool = dev?.currentState("canCool")?.stringValue == "false" ? false : true
			if(canHeat) {
				settingRemove("${dev?.deviceNetworkId}_safety_temp_min")
			}
			if(canCool) {
				settingRemove("${dev?.deviceNetworkId}_safety_temp_max")
			}
			if(settings?."${dev?.deviceNetworkId}_comfort_dewpoint_max") {
				settingRemove("${dev?.deviceNetworkId}_comfort_dewpoint_max")
			}
			if(settings?."${dev?.deviceNetworkId}_comfort_humidity_max") {
				settingRemove("${dev?.deviceNetworkId}_comfort_humidity_max")
			}
		}
	}
	settingRemove("clearLocDesired")
}

def getGlobTitleStr(typ) {
	return "Desired Default ${typ} Temp (${tUnitStr()})"
}

def automationGlobalPrefsPage() {
	def execTime = now()
	dynamicPage(name: "automationGlobalPrefsPage", title: "", nextPage: "", install: false) {
		if(atomicState?.thermostats) {
			def descStr = "Range within ${tempRangeValues()}"
			section {
				paragraph "These settings are applied if individual thermostat settings are not present"
			}
			section(title: "Comfort Preferences 									", hideable: true, hidden: false) {
				input "locDesiredHeatTemp", "decimal", title: getGlobTitleStr("Heat"), description: descStr, range: tempRangeValues(), submitOnChange: true,
						required: false, image: getAppImg("heat_icon.png")
				input "locDesiredCoolTemp", "decimal", title: getGlobTitleStr("Cool"), description: descStr, range: tempRangeValues(), submitOnChange: true,
						required: false, image: getAppImg("cool_icon.png")
				def tRange = (getTemperatureScale() == "C") ? "15..19" : "60..66"
				def wDev = getChildDevice(getNestWeatherId())
				def curDewPnt = wDev ? "${wDev?.currentDewpoint}${tUnitStr()}" : 0
				input "locDesiredComfortDewpointMax", "decimal", title: "Default Dewpoint Threshold (${tRange} ${tUnitStr()})", required: false, range: trange, submitOnChange: true,
						image: getAppImg("dewpoint_icon.png")
				href url: "https://en.wikipedia.org/wiki/Dew_point#Relationship_to_human_comfort", style:"embedded", title: "What is Dew Point?",
						description:"Tap to view", image: getAppImg("instruct_icon.png")
			}
			section(title: "Safety Preferences 									", hideable:true, hidden: false) {
				if(atomicState?.thermostats) {
					atomicState?.thermostats?.each { ts ->
						def dev = getChildDevice(ts?.key)
						def canHeat = dev?.currentState("canHeat")?.stringValue == "false" ? false : true
						def canCool = dev?.currentState("canCool")?.stringValue == "false" ? false : true

						def defmin
						def defmax
						def safeTemp = getSafetyTemps(dev, false)
						if(safeTemp) {
							defmin = safeTemp.min
							defmax = safeTemp.max
						}
						def dew_max = getComfortDewpoint(dev,false)

						/*
						 TODO
							need to check / default to current setting in dth
							should have method in dth to set safety temps (today they are sent from nest manager polls..)
							should have method in dth to clear safety temps
							add global default
						*/

						def str = ""
						str += "Safety Values:"
						str += safeTemp ? "\n• Safety Temps:\n  └ Min: ${safeTemp.min}${tUnitStr()}/Max: ${safeTemp.max}${tUnitStr()}" : "\n• Safety Temps: (Not Set)"
						str += dew_max ? "\n• Comfort Max Dewpoint:\n  └ Max: ${dew_max}${tUnitStr()}" : "\n• Comfort Max Dewpoint: (Not Set)"
						paragraph "${str}", title:"${dev?.displayName}", state: "complete", image: getAppImg("instruct_icon.png")
						if(canHeat) {
							input "${dev?.deviceNetworkId}_safety_temp_min", "decimal", title: "Low Safety Temp (${tUnitStr()})", description: "Range within ${tempRangeValues()}",
									range: tempRangeValues(), submitOnChange: true, required: false, image: getAppImg("heat_icon.png")
						}
						if(canCool) {
							input "${dev?.deviceNetworkId}_safety_temp_max", "decimal", title: "High Safety Temp (${tUnitStr()})", description: "Range within ${tempRangeValues()}",
									range: tempRangeValues(), submitOnChange: true, required: false, image: getAppImg("cool_icon.png")
						}
						def tmin = settings?."${dev?.deviceNetworkId}_safety_temp_min"
						def tmax = settings?."${dev?.deviceNetworkId}_safety_temp_max"

						def comparelow = getTemperatureScale() == "C" ? 10 : 50
						def comparehigh = getTemperatureScale() == "C" ? 32 : 90
						tmin = (tmin != null) ? Math.min( Math.max( (tmin.toDouble() ?: comparelow.toDouble()),comparelow.toDouble() ),comparehigh.toDouble() ) : null
						tmax = (tmax != null) ? Math.max( Math.min((tmax.toDouble() ?: comparehigh.toDouble()),comparehigh.toDouble()),comparelow.toDouble() ) : null
						tmax = (tmax && tmin) ? tmax > tmin ? tmax : null : tmax  // minimum temp takes presedence

						atomicState?."${dev?.deviceNetworkId}_safety_temp_min" = tmin
						atomicState?."${dev?.deviceNetworkId}_safety_temp_max" = tmax

						def tRange = (getTemperatureScale() == "C") ? "15..19" : "60..66"
						input "${dev?.deviceNetworkId}_comfort_dewpoint_max", "decimal", title: "Dewpoint Threshold (${tRange} ${tUnitStr()})", required: false, range: trange,
									submitOnChange: true, image: getAppImg("dewpoint_icon.png")
						// def hrange = "10..80"
						// input "${dev?.deviceNetworkId}_comfort_humidity_max", "number", title: "Max. Humidity Desired (%)", description: "Range within ${hrange}", range: hrange,
						// 			required: false, image: getAppImg("humidity_icon.png")
					}
				}
			}
			section(title: "Reset All Comfort and Safety Temps 									", hideable: true, hidden: true) {
				input(name: "clearLocDesired", type: "bool", title: "Clear Comfort and Safety Temps?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("info_icon2.png"))
				if(clearLocDesired == true) {
					locDesiredClear()
				}
			}
		}

		devPageFooter("autoGlobPrefLoadCnt", execTime)
	}
}

def automationKickStartPage() {
	dynamicPage(name: "automationKickStartPage", title: "This Page runs Update() on all installed Nest Automations", nextPage: "automationsPage", install: false, uninstall: false) {
		def cApps = getChildApps()
		section("Running Update All Automations:") {
			if(cApps) {
				cApps?.sort()?.each { chld ->
					chld?.update()
					paragraph "${chld?.label}\n\nUpdate() Completed Successfully!", state: "complete"
				}
			} else {
				paragraph "No Automations Found"
			}
		}
	}
}

def notifPrefPage() {
	def execTime = now()
	dynamicPage(name: "notifPrefPage", install: false) {
		section("Enable Text Messaging:") {
			input "phone", "phone", title: "Send SMS to Number\n(Optional)", required: false, submitOnChange: true, image: getAppImg("notification_icon2.png")
		}
		section("Enable Push Messages:") {
			input "usePush", "bool", title: "Send Push Notifications\n(Optional)", required: false, submitOnChange: true, defaultValue: false, image: getAppImg("notification_icon.png")
		}
		section("Enable Pushover Support:") {
			input ("pushoverEnabled", "bool", title: "Use Pushover Integration", required: false, submitOnChange: true, image: getAppImg("pushover_icon.png"))
			if(settings?.pushoverEnabled == true) {
				if(atomicState?.isInstalled) {
					if(!atomicState?.pushoverManager) {
						paragraph "If this is the first time enabling Pushover than leave this page and come back if the devices list is empty"
						pushover_init()
					} else {
						input "pushoverDevices", "enum", title: "Select Pushover Devices", description: "Tap to select", groupedOptions: getPushoverDevices(), multiple: true, required: false, submitOnChange: true
						if(settings?.pushoverDevices) {
							def t0 = [(-2):"Lowest", (-1):"Low", 0:"Normal", 1:"High", 2:"Emergency"]
							input "pushoverPriority", "enum", title: "Notification Priority (Optional)", description: "Tap to select", defaultValue: 0, required: false, multiple: false, submitOnChange: true, options: t0
							input "pushoverSound", "enum", title: "Notification Sound (Optional)", description: "Tap to select", defaultValue: "pushover", required: false, multiple: false, submitOnChange: true, options: getPushoverSounds()
						}
					}
				} else { paragraph "New Install Detected!!!\n\n1. Press Done to Finish the Install.\n2. Goto the Automations Tab at the Bottom\n3. Tap on the SmartApps Tab above\n4. Select ${app?.getLabel()} and Resume configuration", state: "complete" }
			}
		}
		if(settings?.phone || settings?.usePush || (settings?.pushoverEnabled && settings?.pushoverDevices)) {
			section("Notification Restrictions:") {
				def t1 = getNotifSchedDesc()
				href "setNotificationTimePage", title: "Notification Restrictions", description: (t1 ?: "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("restriction_icon.png")
			}
			if((settings?.usePush || (settings?.pushoverEnabled && settings?.pushoverDevices)) && !atomicState?.pushTested && atomicState?.pushoverManager) {
				if(sendMsg("Info", "Push Notification Test Successful. Notifications Enabled for ${appName()}", true)) {
					atomicState.pushTested = true
				}
			}

			section("Location Alerts:") {
				paragraph "Get notified when the Location changes from Home/Away", state: "complete"
				input name: "locPresChangeMsg", type: "bool", title: "Notify on Home/Away changes?", defaultValue: true, submitOnChange: true, image: getAppImg("presence_icon.png")
			}
			section("Other Alerts:") {
				def t1 = getAppNotifDesc()
				def appDesc = t1 ? "${t1}\n\n" : ""
				href "notifConfigPage", title: "App Notifications", description: "${appDesc}Tap to configure", params: [pType:"app"], state: (appDesc != "" ? "complete" : null),
						image: getAppImg("nst_manager_5.png")
				t1 = getDevNotifDesc()
				def devDesc = t1 ? "${t1}\n\n" : ""
				href "notifConfigPage", title: "Device Notifications", description: "${devDesc}Tap to configure", params: [pType:"dev"], state: (devDesc != "" ? "complete" : null),
						image: getAppImg("thermostat_icon.png")
				t1 = getAutoNotifDesc()
				def autoDesc = t1 ? "${t1}\n\n" : ""
				href "notifConfigPage", title: "Automation Notifications", description: "${autoDesc}Tap to configure", params: [pType:"auto"], state: (autoDesc != "" ? "complete" : null),
						image: getAppImg("nst_automations_5.png")
				if(atomicState?.appData?.settings?.askAlexa?.enAaMsgQueue == true) {
					t1 = getAskAlexaDesc()
					def aaDesc = t1 ? "${t1}\n\n" : ""
					href "notifConfigPage", title: "AskAlexa Integration", description: "${aaDesc}Tap to configure", params: [pType:"askAlexa"], state: (aaDesc != "" ? "complete" : null),
							image: askAlexaImgUrl()
				}
			}
			section("Reminder Settings:") {
				input name: "notifyMsgWaitVal", type: "enum", title: "Default Reminder Wait?", required: false, defaultValue: 3600,
						metadata: [values:notifValEnum()], submitOnChange: true, image: getAppImg("reminder_icon.png")
			}
		} else { atomicState.pushTested = false }
		atomicState?.notificationPrefs = buildNotifPrefMap()

		devPageFooter("notifPrefLoadCnt", execTime)
	}
}

def notifConfigPage(params) {
	def pType = params.pType
	if(params?.pType) {
		atomicState.curNotifConfigPageData = params
	} else {
		pType = atomicState?.curNotifConfigPageData?.pType
	}
	def execTime = now()
	dynamicPage(name: "notifConfigPage", install: false) {
		switch(pType.toString()) {
			case "app":
				section("Code Update Notifications:") {
					paragraph "Receive notifications when App and Device updates are available", state: "complete"
					input name: "sendAppUpdateMsg", type: "bool", title: "Alert on Updates?", defaultValue: true, submitOnChange: true, image: getAppImg("update_icon.png")
					if(settings?.sendAppUpdateMsg == true || settings?.sendAppUpdateMsg == null) {
						input name: "updNotifyWaitVal", type: "enum", title: "Send Update Reminder Every?", required: false, defaultValue: 43200,
								metadata: [values:notifValEnum()], submitOnChange: true, image: getAppImg("reminder_icon.png")
					}
				}
				section("API Event Notifications:") {
					paragraph "Receive notifications when there are issues with the Nest API", state: "complete"
					input name: "appApiIssuesMsg", type: "bool", title: "Notify on API Issues?", defaultValue: true, submitOnChange: true, image: getAppImg("issue_icon.png")
					if(settings?.appApiIssuesMsg == true || settings?.appApiIssuesMsg == null) {
						input name: "appApiFailedCmdMsg", type: "bool", title: "Notify on Failed Commands?", defaultValue: true, submitOnChange: true, image: getAppImg("switch_on_icon.png")
						input name: "appApiRateLimitMsg", type: "bool", title: "Notify when being Rate-Limited?", defaultValue: true, submitOnChange: true, image: getAppImg("switch_on_icon.png")
						input name: "appApiIssuesWaitVal", type: "enum", title: "Send Issue Reminder Every?", required: false, defaultValue: 900,
								metadata: [values:notifValEnum()], submitOnChange: true, image: getAppImg("reminder_icon.png")
					}
				}

				section("Missed Poll Notifications") {
					paragraph "Receive notifications when the App hasn't updated data in a while", state: "complete"
					input name: "sendMissedPollMsg", type: "bool", title: "Send Missed Poll Messages?", defaultValue: true, submitOnChange: true, image: getAppImg("late_icon.png")
					if(settings?.sendMissedPollMsg == true || settings?.sendMissedPollMsg == null) {
						input name: "misPollNotifyWaitVal", type: "enum", title: "Delay After Missed Poll?", required: false, defaultValue: 1800,
								metadata: [values:notifValEnum()], submitOnChange: true, image: getAppImg("delay_time_icon.png")
					}
				}
				section("Debug Reminders:") {
					paragraph "Get notified when Debug Logging or Remote Diagnostic Logging has been enabled for more than 24 hours", state: "complete"
					input name: "appDbgDiagRemindMsg", type: "bool", title: "Remind me when debug logs are left on?", defaultValue: true, submitOnChange: true, image: getAppImg("reminder_icon.png")
				}
				break
			case "dev":
				if(atomicState?.thermostats || atomicState?.presDevice || atomicState?.cameras || atomicState?.protects || atomicState?.weatherDevice) {
					section("Health Alerts:") {
						paragraph "Get notified when devices go offline", state: "complete"
						input ("devHealthNotifyMsg", "bool", title: "Send Device Heatlth Alerts?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("health_icon.png"))
						if(settings?.devHealthNotifyMsg == true || settings?.devHealthNotifyMsg == null) {
							input name: "devHealthMsgWaitVal", type: "enum", title: "Send Health Reminder Every?", required: false, defaultValue: 3600,
									metadata: [values:notifValEnum()], submitOnChange: true, image: getAppImg("reminder_icon.png")
						}
					}
				}
				if(atomicState?.cameras) {
					section("Camera Alerts:") {
						paragraph "Get notified on Camera streaming changes", state: "complete"
						input ("camStreamNotifMsg", "bool", title: "Send Cam Streaming Alerts?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("camera_icon.png"))
					}
				}
				if(atomicState?.weatherDevice) {
					section("Weather Device:") {
						paragraph "Get local weather alert broadcast's", state: "complete"
						input ("weathAlertNotif", "bool", title: "Local Weather Alerts?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("weather_icon.png"))
					}
				}
				break
			case "auto":
				section("Automation Notifications:") {
					paragraph "Manage some of your automation app notifications...", state: "complete"
				}
				def cApps = getChildApps()
				def watchDog = cApps?.find { it?.getAutomationType() == "watchDog" }
				if(watchDog) {
					def watDogEcoNotifVal = (watchDog?.getSettingVal("watDogNotifMissedEco") == true) ? true : false
					section("WatchDog Eco Notification:") {
						input "watchDogNotifMissedEco", "bool", title: "Notify when Nest is Away and not in Eco mode?", required: false, defaultValue: watDogEcoNotifVal, submitOnChange: true, image: getAppImg("switch_on_icon.png")
					}
					watchDog?.settingUpdate("watDogNotifMissedEco", "${settings?.watchDogNotifMissedEco}", "bool")
				}
				break

			case "askAlexa":
				section() {
					paragraph "", title: "AskAlexa Prefences...", state: "complete"
				}
				section("AskAlexa Integration:") {
					input "allowAskAlexaMQ", "bool", title: "AskAlexa Message Queue?", required: false, defaultValue: true, submitOnChange: true, image: askAlexaImgUrl()
					if(getAskAlexaMultiQueueEn()) {
						input "askAlexaMQList", "enum", title: "Available Message Queues?", options: atomicState?.askAlexaMQList
					}
				}
				break
		}

		devPageFooter("appNotifPrefLoadCnt", execTime)
	}
}

def getAppNotifDesc() {
	def str = ""
	str += settings?.appApiIssuesMsg != false && settings?.appApiFailedCmdMsg != false ? "${str != "" ? "\n" : ""} • API CMD Failures: (${strCapitalize(settings?.appApiFailedCmdMsg ?: "True")})" : ""
	str += settings?.appApiIssuesMsg != false && settings?.appApiRateLimitMsg != false ? "${str != "" ? "\n" : ""} • API Rate-Limiting: (${strCapitalize(settings?.appApiRateLimitMsg ?: "True")})" : ""
	str += settings?.sendMissedPollMsg != false ? "${str != "" ? "\n" : ""} • Missed Poll Alerts: (${strCapitalize(settings?.sendMissedPollMsg ?: "True")})" : ""
	str += settings?.appDbgDiagRemindMsg != false ? "${str != "" ? "\n" : ""} • Debug Log Reminder: (${strCapitalize(settings?.appDbgDiagRemindMsg ?: "True")})" : ""
	str += settings?.sendAppUpdateMsg != false ? "${str != "" ? "\n" : ""} • Code Updates: (${strCapitalize(settings?.sendAppUpdateMsg ?: "True")})" : ""
	return str != "" ? str : null
}

def getDevNotifDesc() {
	def str = ""
	str += settings?.devHealthNotifyMsg != false ? "${str != "" ? "\n" : ""} • Health Alerts: (${strCapitalize(settings?.devHealthNotifyMsg ?: "True")})" : ""
	str += settings?.camStreamNotifMsg != false ? "${str != "" ? "\n" : ""} • Camera Stream Alerts: (${strCapitalize(settings?.camStreamNotifMsg ?: "True")})" : ""
	str += settings?.weathAlertNotif != false ? "${str != "" ? "\n" : ""} • Weather Alerts: (${strCapitalize(settings?.weathAlertNotif ?: "True")})" : ""
	return str != "" ? str : null
}

def getAutoNotifDesc() {
	def str = ""
	str += settings?.watchDogNotifMissedEco ? "${str != "" ? "\n" : ""} • WatchDog Eco Alerts: (${strCapitalize(settings?.watchDogNotifMissedEco)})" : ""
	return str != "" ? str : null
}

def getAskAlexaDesc() {
	def str = ""
	str += settings?.allowAskAlexaMQ ? "${str != "" ? "\n" : ""} • Ask Alexa Msg Queue: (${strCapitalize(settings?.allowAskAlexaMQ)})" : ""
	str += getAskAlexaMultiQueueEn() && atomicState?.askAlexaMQList ? "${str != "" ? "\n" : ""}Multiple Queues Available:\n• Queues: (${atomicState?.askAlexaMQList?.size()})" : ""
	return str != "" ? str : null
}

def addNewline(str) { return "${str != "" ? "\n" : ""}${str}"}

def getAppNotifConfDesc() {
	def str = ""
	if(pushStatus()) {
		def ap = getAppNotifDesc()
		def de = getDevNotifDesc()
		def au = getAutoNotifDesc()
		def nd = getNotifSchedDesc()
		str += (settings?.usePush) ? "${str != "" ? "\n" : ""}Sending via: (Push)" : ""
		str += (settings?.pushoverEnabled) ? "${str != "" ? "\n" : ""}Pushover: (Enabled)" : ""
		str += (settings?.pushoverEnabled && settings?.pushoverPriority) ? "${str != "" ? "\n" : ""} • Priority: (${settings?.pushoverPriority})" : ""
		str += (settings?.pushoverEnabled && settings?.pushoverSound) ? "${str != "" ? "\n" : ""} • Sound: (${settings?.pushoverSound})" : ""
		str += (settings?.phone) ? "${str != "" ? "\n" : ""}Sending via: (SMS)" : ""
		str += (ap || de || au) ? "${str != "" ? "\n" : ""}\nEnabled Alerts:" : ""
		str += (ap) ? "${str != "" ? "\n" : ""} • App Alerts (True)" : ""
		str += (de) ? "${str != "" ? "\n" : ""} • Device Alerts (True)" : ""
		str += (au) ? "${str != "" ? "\n" : ""} • Automation Alerts (True)" : ""
		str += (nd) ? "${str != "" ? "\n" : ""}\nAlert Restrictions:\n${nd}" : ""
	}
	return str != "" ? str : null
}

def buildNotifPrefMap() {
	def res = [:]
	res["app"] = [:]
	res?.app["api"] = [
		"issueMsg":(settings?.appApiIssuesMsg == false ? false : true),
		"issueMsgWait":(settings?.appApiIssuesWaitVal == null ? 900 : settings?.appApiIssuesWaitVal.toInteger()),
		"cmdFailMsg":(settings?.appApiFailedCmdMsg == false ? false : true),
		"rateLimitMsg":(settings?.appApiRateLimitMsg == false ? false : true)
	]
	res?.app["updates"] = [
		"updMsg":(settings?.sendAppUpdateMsg == false ? false : true),
		"updMsgWait":(settings?.updNotifyWaitVal == null ? 43200 : settings?.updNotifyWaitVal.toInteger())
	]
	res?.app["poll"] = ["missPollMsg":(settings?.sendMissedPollMsg == false ? false : true)]
	res?.app["remind"] = ["logRemindMsg":(settings?.appDbgDiagRemindMsg == false ? false : true)]
	res["dev"] = [:]
	res?.dev["devHealth"] = [
		"healthMsg":(settings?.devHealthNotifyMsg == false ? false : true),
		"healthMsgWait":(settings?.devHealthMsgWaitVal == null ? 3600 : settings?.devHealthMsgWaitVal.toInteger())
	]
	res?.dev["camera"] = ["streamMsg":(settings?.camStreamNotifMsg == false ? false : true)]
	res?.dev["weather"] = ["localAlertMsg":(settings?.weathAlertNotif == false ? false : true)]
	res?.dev["tstat"] = [:]
	res?.dev["presence"] = [:]
	res?.dev["protect"] = [:]
	res["locationChg"] = (settings?.locPresChangeMsg == false ? false : true)
	res["msgDefaultWait"] = (settings?.notifyMsgWaitVal == null ? 3600 : settings?.notifyMsgWaitVal.toInteger())
	return res
}

def toggleAllAutomations(upd = false) {
	def t0 = settings?.disableAllAutomations == true ? true : false
	atomicState?.disableAllAutomations = t0
	def disStr = !t0 ? "Returning control to" : "Disabling"
	def cApps = getChildApps()
	cApps.each { ca ->
		LogAction("toggleAllAutomations: ${disStr} automation ${ca?.label}", "info", true)
		ca?.setAutomationStatus(upd)
	}
}

def devNamePage() {
	def execTime = now()
	def pagelbl = atomicState?.isInstalled ? "Device Labels" : "Custom Device Labels"
	dynamicPage(name: "devNamePage", title: pageLbl, nextPage: "", install: false) {
		if(settings?.devNameOverride == null || atomicState?.devNameOverride == null) {
			atomicState?.devNameOverride = true;
			settingUpdate("devNameOverride", "true","bool")
		}
		def overrideName = (atomicState?.devNameOverride) ? true : false
		def altName = (atomicState?.useAltNames) ? true : false
		def custName = (atomicState?.custLabelUsed) ? true : false
		section("Settings:") {
			input (name: "devNameOverride", type: "bool", title: "NST Manager updates Device Names?", required: false, defaultValue: overrideName, submitOnChange: true, image: "" )
			if(devNameOverride && !useCustDevNames) {
				input (name: "useAltNames", type: "bool", title: "Use Location Name as Prefix?", required: false, defaultValue: altName, submitOnChange: true, image: "" )
			}
			if(devNameOverride && !useAltNames) {
				input (name: "useCustDevNames", type: "bool", title: "Assign Custom Names?", required: false, defaultValue: custName, submitOnChange: true, image: "" )
			}

			atomicState.devNameOverride = settings?.devNameOverride ? true : false
			if(atomicState?.devNameOverride) {
				atomicState.useAltNames = settings?.useAltNames ? true : false
				atomicState.custLabelUsed = settings?.useCustDevNames ? true : false
			} else {
				atomicState.useAltNames = false
				atomicState.custLabelUsed = false
			}

			if(atomicState?.custLabelUsed) {
				paragraph "Custom Labels Are Active", state: "complete"
			}
			if(atomicState?.useAltNames) {
				paragraph "Using Location Name as Prefix is Active", state: "complete"
			}
			//paragraph "Current Device Handler Names", image: ""
		}

		def found = false
		if(atomicState?.thermostats || atomicState?.vThermostats) {
			section ("Thermostat Device(s):") {
				atomicState?.thermostats?.each { t ->
					found = true
					def d = getChildDevice(getNestTstatDni(t))
					deviceNameFunc(d, getNestTstatLabel(t.value, t.key), "tstat_${t?.key}_lbl", "thermostat")
				}
				atomicState?.vThermostats?.each { t ->
					found = true
					def d = getChildDevice(getNestvStatDni(t))
					deviceNameFunc(d, getNestVtstatLabel(t.value, t.key), "vtstat_${t?.key}_lbl", "thermostat")
				}
			}
		}
		if(atomicState?.protects) {
			section ("Protect Device Names:") {
				atomicState?.protects?.each { p ->
					found = true
					def d = getChildDevice(getNestProtDni(p))
					deviceNameFunc(d, getNestProtLabel(p.value, p.key), "prot_${p?.key}_lbl", "protect")
				}
			}
		}
		if(atomicState?.cameras) {
			section ("Camera Device Names:") {
				atomicState?.cameras?.each { c ->
					found = true
					def d = getChildDevice(getNestCamDni(c))

					deviceNameFunc(d, getNestCamLabel(c.value, c.key), "cam_${c?.key}_lbl", "camera")
				}
			}
		}
		if(atomicState?.presDevice) {
			section ("Presence Device Name:") {
				found = true
				def pLbl = getNestPresLabel()
				def dni = getNestPresId()
				def d = getChildDevice(dni)
				deviceNameFunc(d, pLbl, "presDev_lbl", "presence")
			}
		}
		if(atomicState?.weatherDevice) {
			section ("Weather Device Name:") {
				found = true
				def wLbl = getNestWeatherLabel()
				def dni = getNestWeatherId()
				def d = getChildDevice(dni)
				deviceNameFunc(d, wLbl, "weathDev_lbl", "weather")
			}
		}
		if(!found) {
			paragraph "No Devices Selected"
		}
		atomicState.forceChildUpd = true

		devPageFooter("devCustNameLoadCnt", execTime)
	}
}

def deviceNameFunc(dev, label, inputStr, devType) {
	def dstr = ""
	if(dev) {
		dstr += "Found:\n${dev.displayName}"
		if(dev.displayName != label) {
			def str1 = "\n\nName is not set to default.\nDefault name is:"
			dstr += "$str1\n${label}"
		}
	} else {
		dstr += "New Name:\n${label}"
	}
	paragraph "${dstr}", state: "complete", image: (atomicState?.custLabelUsed) ? " " : getAppImg("${devType}_icon.png")
	if(atomicState.custLabelUsed) {
		input "${inputStr}", "text", title: "Custom name for ${label}", defaultValue: label, submitOnChange: true, image: getAppImg("${devType}_icon.png")
	}
}

// Parent only method
def setNotificationTimePage() {
	dynamicPage(name: "setNotificationTimePage", title: "Prevent Notifications\nDuring these Days, Times or Modes", uninstall: false) {
		def timeReq = (settings["qStartTime"] || settings["qStopTime"]) ? true : false
		section() {
			input "qStartInput", "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: null, submitOnChange: true, required: false, image: getAppImg("start_time_icon.png")
			if(settings["qStartInput"] == "A specific time") {
				input "qStartTime", "time", title: "Start time", required: timeReq, image: getAppImg("start_time_icon.png")
			}
			input "qStopInput", "enum", title: "Stopping at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: null, submitOnChange: true, required: false, image: getAppImg("stop_time_icon.png")
			if(settings?."qStopInput" == "A specific time") {
				input "qStopTime", "time", title: "Stop time", required: timeReq, image: getAppImg("stop_time_icon.png")
			}
			input "quietDays", "enum", title: "Prevent during these days of the week", multiple: true, required: false, image: getAppImg("day_calendar_icon.png"), options: timeDayOfWeekOptions()
			input "quietModes", "mode", title: "Prevent when these Modes are Active", multiple: true, submitOnChange: true, required: false, image: getAppImg("mode_icon.png")
		}
	}
}

def debugPrefPage() {
	def execTime = now()
	dynamicPage(name: "debugPrefPage", install: false) {
		section ("Application Logs") {
			input ("debugAppendAppName", "bool", title: "Show App/Device Name on all Log Entries?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("log.png"))
			input (name: "appDebug", type: "bool", title: "Show ${appName()} Logs in the IDE?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("log.png"))
			if(settings?.appDebug) {
				input (name: "advAppDebug", type: "bool", title: "Show Verbose Logs?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("list_icon.png"))
			} else {
				settingUpdate("advAppDebug", "false", "bool")
			}
			input ("showDataChgdLogs", "bool", title: "Show API Changes in Logs?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("switch_on_icon.png"))
		}
		section ("Child Device Logs") {
			input (name: "childDebug", type: "bool", title: "Show Device Logs in the IDE?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("log.png"))
		}
		section("Diagnostics:") {
			def t1 = getRemDiagDesc()
			href "helpPage", title: "View Diagnostic Info", description: (t1 ? "${t1 ?: ""}\n\nTap to view" : "Tap to view"), state: (t1) ? "complete" : null, image: getAppImg("diagnostic_icon.png")
		}
		section ("Reset Application Data") {
			input (name: "resetAllData", type: "bool", title: "Reset Application Data?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("reset_icon.png"))
			if(settings?.resetAllData) { LogAction("Reset Application Data Enabled", "info", true) }
			else { LogAction("Reset Application Data Disabled", "info", true) }
		}
		if(settings?.appDebug || settings?.childDebug) {
			if(getTimestampVal("debugEnableDt") == null) { updTimestampMap("debugEnableDt", getDtNow()) }
		} else { updTimestampMap("debugEnableDt", null) }
		atomicState.needChildUpd = true

		devPageFooter("logPrefLoadCnt", execTime)
	}
}

def getRemDiagApp() {
	def remDiagApp = getChildApps()?.find { it?.getAutomationType() == "remDiag" && it?.name == autoAppName() }
	if(remDiagApp) {
		if(remDiagApp?.label != getRemDiagAppChildLabel()) { remDiagApp?.updateLabel(getRemDiagAppChildLabel()) }
		return remDiagApp
	} else {
		return null
	}
}

private diagLogProcChange(setOn) {
	// log.trace "diagLogProcChange($setOn)"
	def doInit = false
	def msg = "Remote Diagnostic Logs "
	if(setOn) {
		if(!atomicState?.enRemDiagLogging && getTimestampVal("remDiagLogActivatedDt") == null) {
			msg += "activated"
			doInit = true
		}
	} else {
		if(getTimestampVal("remDiagLogActivatedDt") != null || atomicState?.enRemDiagLogging) {
			msg += "deactivated"
			settingUpdate("enRemDiagLogging", "false","bool")
			atomicState?.enRemDiagLogging = false
			updTimestampMap("remDiagLogActivatedDt", null)
			doInit = true
		}
	}
	if(doInit) {
		def kdata = getState()?.findAll { (it?.key in ["remDiagLogDataStore" /* , "remDiagDataSentDt"*/  ]) }
		kdata.each { kitem ->
			state.remove(kitem?.key.toString())
		}
		updTimestampMap("remDiagDataSentDt", getDtNow()) // allow us some time for child to start
		//atomicState?.remDiagDataSentDt = getDtNow() // allow us some time for child to start
		atomicState?.enRemDiagLogging = true
		updTimestampMap("remDiagLogActivatedDt", getDtNow())

		initRemDiagApp()
		LogAction(msg, "info", true)
		if(!atomicState?.enRemDiagLogging) { //when turning off, tell automations; turn on - user does done
			def cApps = getChildApps()?.findAll { !(it?.getAutomationType() == "remDiag") }
			if(cApps) {
				cApps?.sort()?.each { chld ->
					chld?.update()
				}
			}
		}
		atomicState.forceChildUpd = true
		updTimestampMap("lastAnalyticUpdDt", null)
	}
}

def getRemDiagActSec() { return getTimeSeconds("remDiagLogActivatedDt", 100000, "getRemDiagActSec").toInteger() }
def getLastRemDiagSentSec() { return getTimeSeconds("remDiagDataSentDt", 1000, "getLastRemDiagSentSec").toInteger() }

def changeLogPage() {
	def execTime = now()
	dynamicPage(name: "changeLogPage", title: "", nextPage: "mainPage", install: false) {
		section() {
			paragraph title: "What's New in this Release...", "", state: "complete", image: getAppImg("whats_new_icon.png")
			paragraph appVerInfo()
		}
		def iData = atomicState?.installData
		iData["shownChgLog"] = true
		atomicState?.installData = iData

		devPageFooter("chgLogLoadCnt", execTime)
	}
}

def uninstallPage() {
	dynamicPage(name: "uninstallPage", title: "Uninstall", install: false, uninstall: true) {
		section() {
			paragraph "This will uninstall the App, All Automation Apps and Child Devices.\n\nPlease make sure that any devices created by this app are removed from any routines/rules/smartapps before tapping Remove."
		}
		section("Did You Get an Error?") {
			href "forceUninstallPage", title: "Perform Some Cleanup Steps", description: ""
		}
		remove("Remove ${appName()} and Devices!", "WARNING!!!", "Last Chance to Stop!\nThis action is not reversible\n\nThis App, All Devices, and Automations will be removed")
	}
}

def forceUninstallPage() {
	dynamicPage(name: "forceUninstallPage", title: "Uninstall Pre Cleanup", install: false, uninstall: true) {
		section() {
			getChildApps()?.each {
				deleteChildApp(it)
				paragraph "Removed Child App: ${it?.label}"
			}
		}
		remove("Try Removing Again!!!", "WARNING!!!", "Last Chance to Stop!\nThis action is not reversible\n\nThis App, All Devices, and Automations will be removed")
	}
}

def getDevOpt() {
	appSettings?.devOpt.toString() == "true" ? true : false
}

def devPageFooter(var, eTime) {
	def res = []
	def data = atomicState?.usageMetricsStore ?: [:]
	data[var] = data[var] != null ? data[var.toString()].toInteger() + 1 : 1
	atomicState?.usageMetricsStore = data
	return res?.size() ? res : ""
}

/******************************************************************************
|						PAGE TEXT DESCRIPTION METHODS						|
*******************************************************************************/
def getSafetyValuesDesc() {
	def str = ""
	def ctr = 1
	def tstats = atomicState?.thermostats
	def siz = tstats?.size()
	if(tstats) {
		tstats?.each { ts ->
			def dev = getChildDevice(ts?.key)
			def defmin
			def defmax
			def safeTemp = getSafetyTemps(dev, false)
			if(safeTemp) {
				defmin = safeTemp.min
				defmax = safeTemp.max
			}
			def maxDew = getComfortDewpoint(dev, false)
			def minTemp = defmin
			def maxTemp = defmax

			if(minTemp == 0) { minTemp = null }
			if(maxTemp == 0) { maxTemp = null }
			if(maxDew == 0) { maxDew = null }

			str += (ts && (minTemp || maxTemp || maxDew)) ? "${dev?.displayName}" : ""

			str += (ts && (minTemp || maxTemp)) ? "\nSafety Values:" : ""
			str += minTemp ? "\n• Min. Temp: ${minTemp}${tUnitStr()}" : ""
			str += maxTemp ? "\n• Max. Temp: ${maxTemp}${tUnitStr()}" : ""
			//str += maxHum ? "\n• Max. Humidity: ${maxHum}%" : ""
			//str += (ts && (minTemp || maxTemp) && (maxDew)) ? "\n" : ""
			str += (ts && (maxDew)) ? "\nComfort Values:" : ""
			str += maxDew ? "\n• Max. Dewpnt: ${maxDew}${tUnitStr()}" : ""
			str += (str != "" && siz > ctr) ? "\n\n" : ""
			ctr = (str != "") ? ctr += 1 : ctr
			siz = (str == "") ? siz -= 1 : siz
		}
	}
	return str
}

def showVoiceRprtPrefs() {
	if(atomicState?.thermostats && (!atomicState?.appData?.settings?.reports?.disVoiceZoneRprt || !atomicState?.appData?.settings?.reports?.disVoiceUsageRprt)) {
		def rPrefs = getVoiceRprtPrefDesc()
		section("Voice Reports:") {
			href "voiceRprtPrefPage", title: "Voice Report Preferences", description: (rPrefs ? "${rPrefs}\n\nTap to modify" : "Tap to configure"), state: (rPrefs ? "complete" : ""), image: getAppImg("speech2_icon.png")
		}
	}
}

def getVoiceRprtPrefs() {
	return [
		"allowVoiceUsageRprt":(atomicState?.appData?.settings?.reports?.disVoiceUsageRprt == true) ? false : true,
		"allowVoiceZoneRprt":(atomicState?.appData?.settings?.reports?.disVoiceZoneRprt == true) ? false : true,
		"vRprtSched":(settings?.vRprtIncSchedInfo == false ? false : true),
		"vRprtZone":(settings?.vRprtIncZoneInfo == false ? false : true),
		"vRprtExtWeat":(settings?.vRprtIncExtWeatInfo == false ? false : true),
		"vRprtUsage":(settings?.vRprtIncUsageInfo == false ? false : true)
	]
}

def getVoiceRprtPrefDesc() {
	def rPref = getVoiceRprtPrefs()
	def str = ""
	str += rPref?.vRprtSched || rPref?.vRprtZone || rPref?.vRprtUsage || rPref?.vRprtExtWeat ? "Included in Reports (If Available):" : ""
	str += rPref?.vRprtZone ? "\n• Zone Info" : ""
	str += rPref?.vRprtSched ? "\n• Automation Schedule" : ""
	str += rPref?.vRprtExtWeat ? "\n• External Weather" : ""
	str += rPref?.vRprtUsage ? "\n• HVAC Usage Info" : ""
	return str != "" ? str : null
}

def getPollingConfDesc() {
	def pollValDesc = (!settings?.pollValue || settings?.pollValue == "180") ? "" : (!atomicState?.streamPolling ? " (Custom)" : " (Stream)")
	def pollStrValDesc = (!settings?.pollStrValue || settings?.pollStrValue == "180") ? "" : (!atomicState?.streamPolling ? " (Custom)" : " (Stream)")
	def pollWeatherValDesc = (!settings?.pollWeatherValue || settings?.pollWeatherValue == "900") ? "" : " (Custom)"
	def pollWaitValDesc = (!settings?.pollWaitVal || settings?.pollWaitVal == "10") ? "" : " (Custom)"
	def pStr = ""
	pStr += "Nest Stream: (${settings.restStreaming ? (!atomicState?.restStreamingOn ? "Not Active" : "Active") : "Off"})"
	pStr += "\nPolling: (${!atomicState?.pollingOn ? "Not Active" : "Active"})"
	pStr += "\n• Device: (${getInputEnumLabel((!atomicState?.streamPolling ? (pollValue ?: 180) : 300), pollValEnum(true))}) ${pollValDesc}"
	pStr += "\n• Structure: (${getInputEnumLabel((!atomicState?.streamPolling ? (pollStrValue?:180) : 300), pollValEnum())}) ${pollStrValDesc}"
	pStr += atomicState?.weatherDevice ? "\n• Weather Polling: (${getInputEnumLabel(pollWeatherValue?:900, notifValEnum())})${pollWeatherValDesc}" : ""
	pStr += "\n• Forced Poll Refresh Limit:\n  └ (${getInputEnumLabel(pollWaitVal ?: 10, waitValEnum())})${pollWaitValDesc}"
	return (pStr != "" ? pStr : "")
}

// Parent only method
def getNotifSchedDesc() {
	def sun = getSunriseAndSunset()
	def startInput = settings?.qStartInput
	def startTime = settings?.qStartTime
	def stopInput = settings?.qStopInput
	def stopTime = settings?.qStopTime
	def dayInput = settings?.quietDays
	def modeInput = settings?.quietModes
	def notifDesc = ""
	def getNotifTimeStartLbl = ( (startInput == "Sunrise" || startInput == "Sunset") ? ( (startInput == "Sunset") ? epochToTime(sun?.sunset.time) : epochToTime(sun?.sunrise.time) ) : (startTime ? time2Str(startTime) : "") )
	def getNotifTimeStopLbl = ( (stopInput == "Sunrise" || stopInput == "Sunset") ? ( (stopInput == "Sunset") ? epochToTime(sun?.sunset.time) : epochToTime(sun?.sunrise.time) ) : (stopTime ? time2Str(stopTime) : "") )
	notifDesc += (getNotifTimeStartLbl && getNotifTimeStopLbl) ? "• Silent Time: ${getNotifTimeStartLbl} - ${getNotifTimeStopLbl}" : ""
	def days = getInputToStringDesc(dayInput)
	def modes = getInputToStringDesc(modeInput)
	notifDesc += days ? "${(getNotifTimeStartLbl || getNotifTimeStopLbl) ? "\n" : ""}• Silent Day${isPluralString(dayInput)}: ${days}" : ""
	notifDesc += modes ? "${(getNotifTimeStartLbl || getNotifTimeStopLbl || days) ? "\n" : ""}• Silent Mode${isPluralString(modeInput)}: ${modes}" : ""
	return (notifDesc != "") ? "${notifDesc}" : null
}

def getWeatherConfDesc() {
	def str = ""
	def defZip = getStZipCode() ? getStZipCode() : getNestZipCode()
	str += "• Weather Location:\n   └ ${getCustWeatherLoc() ? "Custom (${getCustWeatherLoc(true)})" : "Hub Location (${defZip})"}"
	return (str != "") ? "${str}" : null
}

def getCustWeatherLoc(desc=false) {
	def res = null
	if(settings?.useCustWeatherLoc) {
/*
		if(settings?.custWeatherLocSrch == true) {
			if(settings?.custWeatherResultItems != null) {
				res = desc ? (settings?.custWeatherResultItems[0]?.split("\\:"))[1].split("\\.")[0] : settings?.custWeatherResultItems[0].toString()
			}
		} else
*/
		if(settings?.custLocStr != null) {
			res = settings?.custLocStr.toString()
		}
	}
	return res
}

def devCustomizePageDesc() {
	def tempChgWaitValDesc = (!settings?.tempChgWaitVal || settings?.tempChgWaitVal == 4) ? "" : settings?.tempChgWaitVal
	def wstr = settings?.weathAlertNotif ? "Enabled" : "Disabled"
	def str = "Device Customizations:"
	str += "\n• Man. Temp Change Delay:\n   └ (${getInputEnumLabel(settings?.tempChgWaitVal ?: 4, waitValEnum())})"
	str += "\n${getWeatherConfDesc()}"
	str += "\n• Weather Alerts: (${wstr})"
	return ((tempChgWaitValDesc || getCustWeatherLoc() || settings?.weathAlertNotif) ? str : "")
}

def getDevicesDesc(startNewLine=true) {
	def pDev = settings?.thermostats || settings?.protects || settings?.cameras
	def vDev = atomicState?.vThermostats || settings?.presDevice || settings?.weatherDevice
	def str = ""
	str += pDev ? "${startNewLine ? "\n" : ""}Physical Devices:" : ""
	str += settings?.thermostats ? "\n • [${settings?.thermostats?.size()}] Thermostat${(settings?.thermostats?.size() > 1) ? "s" : ""}" : ""
	str += settings?.protects ? "\n • [${settings?.protects?.size()}] Protect${(settings?.protects?.size() > 1) ? "s" : ""}" : ""
	str += settings?.cameras ? "\n • [${settings?.cameras?.size()}] Camera${(settings?.cameras?.size() > 1) ? "s" : ""}" : ""

	str += vDev ? "${pDev ? "\n" : ""}\nVirtual Devices:" : ""
	str += atomicState?.vThermostats ? "\n • [${atomicState?.vThermostats?.size()}] Virtual Thermostat${(atomicState?.vThermostats?.size() > 1) ? "s" : ""}" : ""
	str += settings?.presDevice ? "\n • Presence Device" : ""
	str += settings?.weatherDevice ? "\n • Weather Device" : ""
	str += (!settings?.thermostats && !settings?.protects && !settings?.cameras && !settings?.presDevice && !settings?.weatherDevice) ? "\n • No Devices Selected" : ""
	return (str != "") ? str : null
}

def getAppDebugDesc() {
	def str = ""
	str += isAppDebug() ? "App Debug: (${debugStatus()})${advAppDebug ? "(Trace)" : ""}" : ""
	str += isChildDebug() ? "${str ? "\n" : ""}Device Debug: (${deviceDebugStatus()})" : ""
	str += settings?.showDataChgdLogs ? "${str ? "\n" : ""}Log API Changes: (${settings?.showDataChgdLogs ? "True" : "False"})" : ""
	str += getRemDiagDesc() ? "${str ? "\n" : ""}${getRemDiagDesc()}" : ""
	return (str != "") ? "${str}" : null
}

def getRemDiagDesc() {
	def str = ""
	str += settings?.enDiagWebPage ? "Web Page: (${settings.enDiagWebPage})" : ""
	str += settings?.enRemDiagLogging ? "${str ? "\n" : ""}Log Collection: (${settings.enRemDiagLogging})" : ""
	return (str != "") ? "${str}" : null
}

/******************************************************************************
*								NEST LOGIN PAGES							*
*******************************************************************************/
def nestLoginPrefPage () {
	if(!atomicState?.authToken) {
		return authPage()
	} else {
		def execTime = now()
		return dynamicPage(name: "nestLoginPrefPage", nextPage: atomicState?.authToken ? "" : "authPage", install: false) {
			//def formatVal = settings?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
			//def tf = new SimpleDateFormat(formatVal)
			//if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
			updTimestampMap("authTokenCreatedDt", (getTimestampVal("authTokenCreatedDt") ?: getDtNow()))
			section() {
				paragraph title: "Authorization Info:", "Authorization Date:\n• ${getTimestampVal("authTokenCreatedDt")}", state: "complete"
				paragraph "Last Nest Connection:\n• ${getTimestampVal("lastDevDataUpd")}"
			}
			section("Revoke Authorization Reset:") {
				href "nestTokenResetPage", title: "Log Out and Reset Nest Token", description: "Tap to Reset Nest Token", required: true, state: null, image: getAppImg("reset_icon.png")

			}
			devPageFooter("nestLoginLoadCnt", execTime)
		}
	}
}

def nestTokenResetPage() {
	return dynamicPage(name: "nestTokenResetPage", install: false) {
		section ("Resetting Nest Token") {
			revokeNestToken()
			paragraph "Token Reset Complete...", state: "complete"
			paragraph "Press Done/Save to return to Login page"
		}
	}
}

/******************************************************************************
 *#########################	NATIVE ST APP METHODS ############################*
 ******************************************************************************/
def installed() {
	LogAction("Installed with settings: ${settings}", "debug", true)
	atomicState?.installData = ["initVer":appVersion(), "dt":getDtNow().toString(), "updatedDt":"Not Set", "freshInstall":true, "shownDonation":false, "shownFeedback":false, "shownChgLog":true, "usingNewAutoFile":true, "liteAppMode":isAppLiteMode()]
	sendInstallSlackNotif()
	initialize()
	sendNotificationEvent("${appName()} installed")
}

def updated() {
	LogAction("${app.label} Updated...with settings: ${settings}", "debug", true)
	atomicState?.pollBlocked = true
	atomicState?.pollBlockedReason = "Running updated"
	//restStreamHandler(true, false)   // stop the rest stream
	//atomicState?.restStreamingOn = false
	//atomicState.ssdpOn = false
	// if(atomicState?.migrationInProgress == true) { LogAction("Skipping updated() as migration in-progress", "warn", true); return }
	if(atomicState?.needToFinalize == true) { LogAction("Skipping updated() as auth change in-progress", "warn", true); return }
	initialize()
	sendNotificationEvent("${appName()} has updated settings")
	atomicState?.lastUpdatedDt = getDtNow()
}

def uninstalled() {
	//LogTrace("uninstalled")
	uninstManagerApp()
}

def initialize() {
	LogTrace("initialize")
	atomicState?.pollBlocked = true
	atomicState?.pollBlockedReason = "Running Initialize"
	restStreamHandler(true, false)   // stop the rest stream
	//atomicState?.restStreamingOn = false
	atomicState.ssdpOn = false
	if(!atomicState?.tsMigration) { timestampMigration() }
	if(atomicState?.resetAllData || settings?.resetAllData) {
		if(fixState()) { return }	// runIn of fixState will call initManagerApp()
		//settingUpdate("resetAllData", "false", "bool")
	}
	if(!isAppLiteMode()) {
		runIn(5, "reInitBuiltins", [overwrite: true])  // These are to have these apps release subscriptions to devices (in case of delete)
	}
	runIn(21, "initManagerApp", [overwrite: true])	// need to give time for watchdog updates before we try to delete devices.
}

def reInitBuiltins() {
	checkStorageApp()
	if(!isAppLiteMode()) {
		initWatchdogApp()
		initNestModeApp() // this just removes extras
		initStorageApp()
	}
	if(atomicState?.tsMigration) { initRemDiagApp() }
}

def initBuiltin(btype) {
	LogTrace("initBuiltin(${btype})")
	def keepApp = false
	def autoStr = ""
	switch (btype) {
		case "initNestModeApp":
			if(automationNestModeEnabled()) {
				keepApp = true
				autoStr = "nMode"
			}
			break
		case "initWatchdogApp":
			def t0 = settings?.thermostats?.size()
			def t1 = atomicState?.thermostats?.size()
			if(atomicState?.isInstalled && t0 && t1) { // only need watchDog if we have thermostats
				keepApp = true
			}
			autoStr = "watchDog"
			break
		case "initRemDiagApp":
			if(settings?.enRemDiagLogging) {
				keepApp = true
			} else {
				settingUpdate("enRemDiagLogging", "false","bool")
				atomicState?.enRemDiagLogging = false
				updTimestampMap("remDiagLogActivatedDt", null)
			}
			autoStr = "remDiag"
			break
		case "initStorageApp":
			autoStr = "storage"
			keepApp = true
			def stateSz = getStateSizePerc()
			if(stateSz < 58) { keepApp = false }
			else {
				def kdata = getState()?.findAll { (it?.key in [ "curWeather", "curForecast", "curAstronomy", "curAlerts" ]) }
				kdata.each { kitem ->
					state.remove(kitem?.key.toString())
				}
			}
			break
		default:
 			LogAction("initBuiltin BAD btype ${btype}", "warn", true)
			break
	}
	if(isAppLiteMode()) { keepApp = false }
	if(autoStr) {
		def mynestApp = getChildApps()?.findAll { it?.getAutomationType() == autoStr }
		if(keepApp && mynestApp?.size() < 1 && btype != "initNestModeApp") {
			LogAction("Installing ${autoStr}", "info", true)
			updTimestampMap("lastAnalyticUpdDt", null)
			try {
				if(btype == "initRemDiagApp") {
					addChildApp(appNamespace(), autoAppName(), getRemDiagAppChildLabel(), [settings:[remDiagFlag:["type":"bool", "value":true]]])
				}
				if(btype == "initWatchdogApp") {
					addChildApp(appNamespace(), autoAppName(), getWatDogAppChildLabel(), [settings:[watchDogFlag:["type":"bool", "value":true]]])
				}
				if(btype == "initStorageApp") {
					def storageApp = getStorageApp(false)
					if(!storageApp) {
						addChildApp(appNamespace(), autoAppName(), getStorageAppChildLabel(), [settings:[storageFlag:[type:"bool", value:true]]])
					}
					storageAppInst(true)
				}
			} catch (ex) {
				appUpdateNotify(true, "automation")
			}
		} else if(mynestApp?.size() >= 1) {
			def cnt = 1
			mynestApp?.each { chld ->
				if(keepApp && cnt == 1) {
					LogTrace("initBuiltin: Running Update Command on ${autoStr}")
					chld.update()
				} else if(!keepApp || cnt > 1) {
					def slbl = keepApp ? "warn" : "info"
					LogAction("initBuiltin: Deleting ${keepApp ? "Extra " : ""}${autoStr} (${chld?.id})", slbl, true)
					deleteChildApp(chld)
					updTimestampMap("lastAnalyticUpdDt", null)
				}
				cnt = cnt+1
			}
		}
	}
}

def initNestModeApp() {
	initBuiltin("initNestModeApp")
}

def initWatchdogApp() {
	initBuiltin("initWatchdogApp")
}

def initRemDiagApp() {
	initBuiltin("initRemDiagApp")
}

def initStorageApp() {
	initBuiltin("initStorageApp")
}

def initManagerApp() {
	LogTrace("initManagerApp (${atomicState?.pollBlocked}) (${atomicState?.pollBlockedReason})")
	setStateVar()
	//restStreamHandler(true, false)   // stop the rest stream
	unschedule()
	unsubscribe()
	atomicState?.pollingOn = false
	atomicState?.restStreamingOn = false
	atomicState?.streamPolling = false
	atomicState.ssdpOn = false
	stateCleanup()

	initStorageApp()
	def sData = atomicState?.swVer ?: [:]
	sData["mgrVer"] = appVersion()
	atomicState?.swVer = sData
	if(settings?.structures && atomicState?.structures && !atomicState.structName) {
		def structs = getNestStructures()
		if(structs && structs["${atomicState?.structures}"]) {
			atomicState.structName = structs[atomicState?.structures]?.toString()
		}
	}
	if(!addRemoveDevices()) { // if we changed any devices or had an error trying, reset queues and polling
		atomicState.cmdQlist = []
		if(!isAppLiteMode()) {
			runIn(15, "reInitBuiltins", [overwrite: true])  // need to have watchdog/nestmode check if we created devices
		}
	}
	if(settings?.thermostats || settings?.protects || settings?.cameras || settings?.presDevice || settings?.weatherDevice) {
		atomicState?.isInstalled = true
	} else { atomicState.isInstalled = false }
	subscriber()
	startStopStream()
	runIn(21, "finishInitManagerApp", [overwrite: true])
}

def finishInitManagerApp() {
	LogTrace("finishInitManagerApp (${atomicState?.pollBlocked}) (${atomicState?.pollBlockedReason})")
// polling is still blocked coming into this
	//atomicState?.appCodeIdData = [:]
	setPollingState() // polling is unblocked
	updTimestampMap("lastChildUpdDt", null) // force child update on next poll
	updTimestampMap("lastDevDataUpd", null)
	updTimestampMap("lastChildForceUpdDt", null)
	updTimestampMap("lastForcePoll", null)
	if(atomicState?.isInstalled && atomicState?.installData?.usingNewAutoFile) {
		createSavedNest()
		if(app.label == "Nest Manager") { app.updateLabel("NST Manager") }
		def badAutomation = false
		def storId = null
		if(!isAppLiteMode()) {
			getChildApps()?.sort()?.each { chld ->
				chld?.update()
			}
		} else {
			badAutomation = true
		}

		if (!badAutomation) {
			def tstatAutoApp = getChildApps()?.find {
				def aa = null
				try {
					aa = it?.getAutomationType()
					def bb = it?.getCurrentSchedule()
					def ai = it?.getAutomationsInstalled()
				} catch (Exception e) {
					LogAction("BAD Automation file ${it?.label?.toString()} (${it?.id}), please INSTALL proper automation file", "error", true)
					badAutomation = true
					appUpdateNotify(true, "automation")
				}
				if( !badAutomation && !(aa in ["nMode", "watchDog", "remDiag", "schMot", "storage"]) ) { badAutomation = true }
			}
		}
		if(badAutomation) {
			LogAction("Deleting BAD Automations in 10 mins", "warn", true)
			runIn(600, "removeBadAutomations", [overwrite: true])
		}
		if(resetAllData) { // This is to cleanup state for resetAllData - children are given 90 seconds to fixState()
			runIn(90, "cleanupResetSettings", [overwrite: true])
		}
	}
}

def cleanupResetSettings() {
	settingUpdate("resetAllData", "false", "bool")
	runIn(75, "initialize", [overwrite: true]) // cause cleanup to propagate to child apps
}

def removeBadAutomations() {
	def tstatAutoApp = getChildApps()?.find {
		def bad = false
		def aa = null
		try {
			aa = it?.getAutomationType()
			def bb = it?.getCurrentSchedule()
			def ai = it?.getAutomationsInstalled()
		} catch (Exception e) {
			LogAction("BAD Automation (${it?.id}) found", "warn", true)
			bad = true
		}
		if( !bad && !(aa in ["nMode", "watchDog", "remDiag", "schMot", "storage"]) ) { bad = true }
		if(bad || isAppLiteMode()) {
			try {
				LogAction("Calling uninstall on Automation (${it?.id})", "warn", true)
				it?.uninstAutomationApp()
			} catch (Exception e) {
				;
			}
			LogAction("Deleting Automation (${it?.id})", "warn", true)
			deleteChildApp(it)
			updTimestampMap("lastAnalyticUpdDt", null)
		}
	}
}

def remDiagAppAvail(available) {
	atomicState?.remDiagAppAvailable = (available == true)
}

def createSavedNest() {
	def str = "createSavedNest"
	LogTrace("${str}")
	if(atomicState?.isInstalled) {
		def bbb = [:]
		def bad = false
		if(settings?.structures && atomicState?.structures) {
			def structs = getNestStructures()
			def newStrucName = structs && structs?."${atomicState?.structures}" ? "${structs[atomicState?.structures]}" : null
			if(newStrucName) {
				bbb.a_structures_setting = settings.structures
				bbb.a_structures_as = atomicState.structures
				bbb.a_structure_name_as = atomicState?.structName

				def dData = atomicState?.deviceData
				def t0 = [:]

				t0 = dData?.thermostats?.findAll { it?.key?.toString() in settings?.thermostats }
				LogAction("${str} | Thermostats(${t0?.size()}): ${settings?.thermostats}", "info", true)
				def t1 = [:]
				t0?.each { devItem ->
					LogAction("${str}: Found (${devItem?.value?.name})", "info", false)
					if(devItem?.key && devItem?.value?.name) {
						t1?."${devItem.key.toString()}" = devItem?.value?.name
					}
				}
				def t3 = settings?.thermostats?.size() ?: 0
				if(t1?.size() != t3) { LogAction("Thermostat Counts Wrong! | Current: (${t1?.size()}) | Expected: (${t3})", "error", true); bad = true }
				bbb?.b_thermostats_as = settings?.thermostats && dData && atomicState?.thermostats ? t1 : [:]
				bbb?.b_thermostats_setting = settings?.thermostats ?: []

				dData = atomicState?.deviceData
				t0 = [:]
				t0 = dData?.smoke_co_alarms?.findAll { it?.key?.toString() in settings?.protects }
				LogAction("${str} | Protects(${t0?.size()}): ${settings?.protects}", "info", true)
				t1 = [:]
				t0?.each { devItem ->
					LogAction("${str}: Found (${devItem?.value?.name})", "info", false)
					if(devItem?.key && devItem?.value?.name) {
						t1."${devItem.key}" = devItem?.value?.name
					}
				}
				t3 = settings?.protects?.size() ?: 0
				if(t1?.size() != t3) { LogAction("Protect Counts Wrong! | Current: (${t1?.size()}) | Expected: (${t3})", "error", true); bad = true }
				bbb.c_protects_as = settings?.protects && dData && atomicState?.protects ? t1 : [:]
				bbb.c_protects_settings = settings?.protects ?: []

				dData = atomicState?.deviceData
				t0 = [:]
				t0 = dData?.cameras?.findAll { it?.key?.toString() in settings?.cameras }
				LogAction("${str} | Cameras(${t0?.size()}): ${settings?.cameras}", "info", true)
				t1 = [:]
				t0?.each { devItem ->
					LogAction("${str}: Found (${devItem?.value?.name})", "info", false)
					if(devItem?.key && devItem?.value?.name) {
						t1."${devItem?.key}" = devItem?.value?.name
					}
				}
				t3 = settings?.cameras?.size() ?: 0
				if(t1?.size() != t3) { LogAction("Camera Counts Wrong! | Current: (${t1?.size()}) | Expected: (${t3})", "error", true); bad = true }
				bbb.d_cameras_as = settings?.cameras && dData && atomicState?.cameras ? t1 : [:]
				bbb.d_cameras_setting = settings?.cameras ?: []
			} else { LogAction("${str}: No Structures Found!!!", "warn", true) }

			def t0 = atomicState?.savedNestSettings ?: null
			def t1 = t0 ? new groovy.json.JsonOutput().toJson(t0) : null
			def t2 = bbb != [:] ? new groovy.json.JsonOutput().toJson(bbb) : null
			if(bad) {
				atomicState.savedNestSettingsprev = atomicState?.savedNestSettings
				atomicState.savedNestSettingslastbuild = bbb
				state.remove("savedNestSettings")
			}
			if(!bad && t2 && (!t0 || t1 != t2)) {
				atomicState.savedNestSettings = bbb
				state.remove("savedNestSettingsprev")
				state.remove("savedNestSettingslastbuild")
				return true
			}
		} else { LogAction("${str}: No Structure Settings", "warn", true) }
	} else { LogAction("${str}: NOT Installed!!!", "warn", true) }
	return false
}

def mySettingUpdate(name, value, type=null) {
	if(getDevOpt()) {
		LogAction("Setting $name set to type:($type) $value", "warn", true)
		if(!atomicState?.ReallyChanged) { return }
	}
	if(atomicState?.ReallyChanged) {
		settingUpdate(name, value, type)
	}
}

def checkRemapping() {
	def str = "checkRemapping"
	LogTrace(str)
	def astr = ""
	atomicState.ReallyChanged = false
	def myRC = atomicState.ReallyChanged
	if(atomicState?.isInstalled && settings?.structures) {
		def aastr = getApiData("str")
		def aadev = getApiData("dev")
		//def aameta = getApiData("meta")
		def sData = atomicState?.structData
		def dData = atomicState?.deviceData
		//def mData = atomicState?.metaData
		def savedNest = atomicState?.savedNestSettings
		if(sData && dData /* && mData */ && savedNest) {
			def structs = getNestStructures()
			if(structs && !getDevOpt() ) {
				LogAction("${str}: nothing to do ${structs}", "info", true)
				return
			} else {
				astr += "${str}: found the mess..cleaning up ${structs}"
				atomicState?.pollBlocked = true
				atomicState?.pollBlockedReason = "Remapping"

				def newStructures_settings = ""
				def newThermostats_settings = []
				def newvThermostats = [:]
				def newProtects_settings = []
				def newCameras_settings = []
				def oldPresId = getNestPresId()
				def oldWeatId = getNestWeatherId()

				sData?.each { strucId ->
					def t0 = strucId.key
					def t1 = strucId.value
					if(t1?.name && t1?.name == savedNest?.a_structure_name_as) {
						newStructures_settings = [t1?.structure_id]?.join('.')
					}
				}

				if(settings?.structures && newStructures_settings) {
					if(settings.structures != newStructures_settings) {
						atomicState.ReallyChanged = true
						myRC = atomicState?.ReallyChanged
						astr += ", STRUCTURE CHANGED"
					} else {
						astr += ", NOTHING REALLY CHANGED (DEVELOPER MODE)"
					}
				} else { astr += ", no new structure found" }
				LogAction(astr, "warn", true)
				astr = ""
				if(myRC || (newStructures_setting && getDevOpt())) {
					mySettingUpdate("structures", newStructures_settings)
					if(myRC) { atomicState?.structures = newStructures_settings }
					def newStrucName = newStructures_settings ? atomicState?.structData[newStructures_settings]?.name : null
					astr = "${str}: newStructures ${newStructures_settings} | name: ${newStrucName} | to settings & as structures: ${settings?.structures}"

					astr += ", as.thermostats: ${atomicState?.thermostats}  |  saveNest: ${savedNest?.b_thermostats_as}"
					LogAction(astr, "info", true)
					savedNest?.b_thermostats_as.each { dni ->
						def t0 = dni?.key
						def dev = getChildDevice(t0)
						if(dev) {
							//LogAction("${str}: found dev oldId: ${t0}", "info", true)
							def gotIt = false
							dData?.thermostats?.each { devItem ->
								def t21 = devItem.key
								def t22 = devItem.value
								def newDevStructId = [t22?.structure_id].join('.')
								if(!gotIt && t22 && newDevStructId && newDevStructId == newStructures_settings && dni.value == t22?.name) {
									def t6 = [t22?.device_id].join('.')
									def t7 = [ "${t6}":dni.value ]
									def newDevId
									t7.collect { ba ->
										newDevId = getNestTstatDni(ba)
									}
									newThermostats_settings << newDevId
									gotIt = true

									def rstr = "found newDevId ${newDevId} to replace oldId: ${t0} ${t22?.name} |"

									if(settings?."${t0}_safety_temp_min") {
										mySettingUpdate("${newDevId}_safety_temp_min", settings?."${t0}_safety_temp_min", "decimal")
										mySettingUpdate("${t0}_safety_temp_min", "")
										rstr += ", safety min"
									}
									if(settings?."${t0}_safety_temp_max") {
										mySettingUpdate("${newDevId}_safety_temp_max", settings?."${t0}_safety_temp_max", "decimal")
										mySettingUpdate("${t0}_safety_temp_max", "")
										rstr += ", safety max"
									}
									if(settings?."${t0}_comfort_dewpoint_max") {
										mySettingUpdate("${newDevId}_comfort_dewpoint_max", settings?."${t0}_comfort_dewpoint_max", "decimal")
										mySettingUpdate("${t0}_comfort_dewpoint_max", "")
										rstr += ", comfort dew"
									}
									if(settings?."${t0}_comfort_humidity_max") {
										mySettingUpdate("${newDevId}_comfort_humidity_max", settings?."${t0}_comfort_humidity_max", "number")
										mySettingUpdate("${t0}_comfort_humidity_max", "")
										rstr += ", comfort hum"
									}
									if(settings?."tstat_${t0}_lbl") {
										if(atomicState?.devNameOverride && atomicState?.custLabelUsed) {
											mySettingUpdate("tstat_${newDevId}_lbl", settings?."tstat_${t0}_lbl", "text")
										}
										mySettingUpdate("tstat_${t0}_lbl", "")
										rstr += ", custom Label"
									}
									if(atomicState?.vThermostats && atomicState?."vThermostatv${t0}") {
										def physDevId = atomicState?."vThermostatMirrorIdv${t0}"
										def t1 = atomicState?.vThermostats
										def t5 = "v${newDevId}" as String

										if(t0 && t0 == physDevId && t1?."v${physDevId}") {
											def vdev = getChildDevice("v${t0}")
											if(vdev) {
												rstr += ", there are virtual devices that match"

												if(settings?."vtstat_v${t0}_lbl") {
													if(atomicState?.devNameOverride && atomicState?.custLabelUsed) {
														mySettingUpdate("vtstat_${t5}_lbl", settings?."tstat_v${t0}_lbl", "text")
													}
													mySettingUpdate("vtstat_v${t0}_lbl", "")
													rstr += ", custom vstat Label"
												}
												newvThermostats."${t5}" = t1."v${t0}"
												if(myRC) {
													atomicState."vThermostat${t5}" = atomicState?."vThermostatv${t0}"
													atomicState?."vThermostatMirrorId${t5}" = newDevId
													atomicState?."vThermostatChildAppId${t5}" = atomicState?."vThermostatChildAppIdv${t0}"
												}

												def automationChildApp = getChildApps().find{ it.id == atomicState?."vThermostatChildAppIdv${t0}" }
												if(automationChildApp != null) {
													if(myRC) { automationChildApp.setRemoteSenTstat(newDevId) }
													rstr += ", fixed atomicState.remSenTstat"
												} else { rstr += ", DID NOT FIND AUTOMATION APP" }

												// fix locks
												def t3 = ""
												if(atomicState?."remSenLock${t0}") {
													rstr += ", fixed locks"
													if(myRC) {
														atomicState."remSenLock${newDevId}" = atomicState."remSenLock${t0}"
														t3 = "remSenLock${t0}";		state.remove(t3.toString())
													}
												} else { rstr += ", DID NOT FIND LOCK" }
												// find the virtual device and reset its dni
												rstr += ", reset vDNI"
												if(myRC) {
													vdev.deviceNetworkId = t5

													t3 = "vThermostatv${t0}";		state.remove(t3.toString())
													t3 = "vThermostatMirrorIdv${t0}";	state.remove(t3.toString())
													t3 = "vThermostatChildAppIdv${t0}";	state.remove(t3.toString())
												}

											} else { rstr += ", DID NOT FIND VIRTUAL DEVICE" }
											def t11 = "oldvStatDatav${t0}"
											state.remove(t11.toString())
										} else { rstr += ", vstat formality check failed" }
									} else { rstr += ", no vstat" }

									if(myRC) { dev.deviceNetworkId = newDevId }
									if(rstr != "") { LogAction("${str}: resultStr: ${rstr}", "info", true) }
								}
							}
							if(!gotIt) { LogAction("${str}: NOT matched dev oldId: ${t0}", "warn", true) }
						} else { LogAction("${str}: NOT found dev oldId: ${t0}", "error", true) }
						def t10 = "oldTstatData${t0}"
						state.remove(t10.toString())
					}
					astr = ""
					if(settings?.thermostats) {
						def t0 = settings?.thermostats?.size()
						def t1 = savedNest?.b_thermostats_as?.size()
						def t2 = newThermostats_settings?.size()
						if(t0 == t1 && t1 == t2) {
							mySettingUpdate("thermostats", newThermostats_settings, "enum")
							astr += "${str}: newThermostats_settings: ${newThermostats_settings} settings.thermostats: ${settings?.thermostats}"

							//LogAction("as.thermostats: ${atomicState?.thermostats}", "warn", true)
							atomicState.thermostats = null
							def t4 = newvThermostats ? newvThermostats?.size() : 0
							def t5 = atomicState?.vThermostats ? atomicState?.vThermostats.size() : 0
							if(t4 || t5) {
								if(t4 == t5) {
									astr += ", AS vThermostats ${newvThermostats}"
									if(myRC) { atomicState.vThermostats = newvThermostats }
								} else { LogAction("vthermostat sizes don't match ${t4} ${t5}", "warn", true) }
							}
							LogAction(astr, "info", true)
						} else { LogAction("thermostat sizes don't match ${t0} ${t1} ${t2}", "warn", true) }
					}

					astr = ""
					savedNest?.c_protects_as.each { dni ->
						def t0 = dni.key
						def dev = getChildDevice(t0)
						if(dev) {
							def gotIt = false
							dData?.smoke_co_alarms?.each { devItem ->
								def t21 = devItem.key
								def t22 = devItem.value
								def newDevStructId = [t22?.structure_id].join('.')
								if(!gotIt && t22 && newDevStructId && newDevStructId == newStructures_settings && dni.value == t22?.name) {
									//def newDevId = [t22?.device_id].join('.')
									def t6 = [t22?.device_id].join('.')
									def t7 = [ "${t6}":dni.value ]
									def newDevId
									t7.collect { ba ->
										newDevId = getNestProtDni(ba)
									}
									newProtects_settings << newDevId
									gotIt = true
									astr += ", ${str}: found newDevId ${newDevId} to replace oldId: ${t0} ${t22?.name} "
									if(settings?."prot_${t0}_lbl") {
										if(atomicState?.devNameOverride && atomicState?.custLabelUsed) {
											mySettingUpdate("prot_${newDevId}_lbl", settings?."prot_${t0}_lbl", "text")
										}
										mySettingUpdate("prot_${t0}_lbl", "")
									}

									if(myRC) { dev.deviceNetworkId = newDevId }
								}
							}
							if(!gotIt) { LogAction("${str}: NOT matched dev oldId: ${t0}", "warn", true) }
						} else { LogAction("${str}: NOT found dev oldId: ${t0}", "error", true) }
						def t10 = "oldProtData${t0}"
						state.remove(t10.toString())
					}
					if(settings?.protects) {
						def t0 = settings?.protects?.size()
						def t1 = savedNest?.c_protects_as?.size()
						def t2 = newProtects_settings?.size()
						if(t0 == t1 && t1 == t2) {
							mySettingUpdate("protects", newProtects_settings, "enum")
							astr += "newProtects: ${newProtects_settings} settings.protects: ${settings?.protects} "
							//LogAction("as.protects: ${atomicState?.protects}", "warn", true)
							atomicState.protects = null
						} else { LogAction("protect sizes don't match ${t0} ${t1} ${t2}", "warn", true) }
						LogAction(astr, "info", true)
					}

					astr = ""
					savedNest?.d_cameras_as.each { dni ->
						def t0 = dni.key
						def dev = getChildDevice(t0)
						if(dev) {
							def gotIt = false
							dData?.cameras?.each { devItem ->
								def t21 = devItem.key
								def t22 = devItem.value
								def newDevStructId = [t22?.structure_id].join('.')
								if(!gotIt && t22 && newDevStructId && newDevStructId == newStructures_settings && dni.value == t22?.name) {
									//def newDevId = [t22?.device_id].join('.')
									def t6 = [t22?.device_id].join('.')
									def t7 = [ "${t6}":dni.value ]
									def newDevId
									t7.collect { ba ->
										newDevId = getNestCamDni(ba)
									}
									newCameras_settings << newDevId
									gotIt = true
									astr += "${str}: found newDevId ${newDevId} to replace oldId: ${t0} ${t22?.name} "
									if(settings?."cam_${t0}_lbl") {
										if(atomicState?.devNameOverride && atomicState?.custLabelUsed) {
											mySettingUpdate("cam_${newDevId}_lbl", settings?."cam_${t0}_lbl", "text")
										}
										mySettingUpdate("cam_${t0}_lbl", "")
									}

									if(myRC) { dev.deviceNetworkId = newDevId }
								}
							}
							if(!gotIt) { LogAction("${str}: NOT matched dev oldId: ${t0}", "warn", true) }
						} else { LogAction("${str}: NOT found dev oldId: ${t0}", "error", true) }
						def t10 = "oldCamData${t0}"
						state.remove(t10.toString())
					}
					if(settings?.cameras) {
						def t0 = settings?.cameras?.size()
						def t1 = savedNest?.d_cameras_as?.size()
						def t2 = newCameras_settings?.size()
						if(t0 == t1 && t1 == t2) {
							mySettingUpdate("cameras", newCameras_settings, "enum")
							astr += "${str}: newCameras_settings: ${newCameras_settings} settings.cameras: ${settings?.cameras}"
							//LogAction("as.cameras: ${atomicState?.cameras}", "warn", true)
							atomicState.cameras = null
						} else { LogAction("camera sizes don't match ${t0} ${t1} ${t2}", "warn", true) }
						LogAction(astr, "info", true)
					}

/*
	The Settings changes made above "do not take effect until a state re-load happens
					if(myRC) {
						fixDevAS()
					}
*/
					astr = "oldPresId $oldPresId "
					// fix presence
					if(settings?.presDevice) {
						if(oldPresId) {
							def dev = getChildDevice(oldPresId)
							def newId = getNestPresId()
							def ndev = getChildDevice(newId)
							astr += "| DEV ${dev?.deviceNetworkId} | NEWID $newId |  NDEV: ${ndev?.deviceNetworkId} "
							def t10 = "oldPresData${dev?.deviceNetworkId}"
							state.remove(t10.toString())
							if(dev && newId && ndev) { astr += " all good presence" }
							else if(!dev) { astr += "where is the pres device?" }
							else if(dev && newId && !ndev) {
								astr += "will fix presence "
								if(myRC) { dev.deviceNetworkId = newId }
							} else { LogAction("${dev?.label} $newId ${ndev?.label}", "error", true) }
						} else { LogAction("no oldPresId", "error", true) }
						LogAction(astr, "info", true)
					}
					// fix weather
					astr += "oldWeatId $oldWeatId "
					if(settings?.weatherDevice) {
						if(oldWeatId) {
							def dev = getChildDevice(oldWeatId)
							def newId = getNestWeatherId()
							def ndev = getChildDevice(newId)
							astr += "| DEV ${dev?.deviceNetworkId} | NEWID $newId |  NDEV: ${ndev?.deviceNetworkId} "
							def t10 = "oldWeatherData${dev?.deviceNetworkId}"
							state.remove(t10.toString())
							if(dev && newId && ndev) { astr += " all good weather " }
							else if(!dev) { LogAction("where is the weather device?", "warn", true) }
							else if(dev && newId && !ndev) {
								astr += "will fix weather"
								if(myRC) { dev.deviceNetworkId = newId }
							} else { LogAction("${dev?.label} $newId ${ndev?.label}", "error", true) }
						} else { LogAction("no oldWeatId", "error", true) }
					}
					LogAction(astr, "info", true)

				} else { LogAction("no changes or no data a:${settings?.structures} b: ${newStructures_settings}", "info", true) }

				atomicState?.pollBlocked = false
				atomicState?.pollBlockedReason = ""
				return
			}
		} else { LogAction("don't have our data", "warn", true) }
	} else { LogAction("not installed, no structure", "warn", true) }
}

def askAlexaMQHandler(evt) {
	if (!evt) { return }
	switch (evt.value) {
		case "refresh":
			atomicState?.askAlexaMQList = (evt.jsonData && evt.jsonData?.queues) ? evt.jsonData.queues : []
			break
	}
}

def startStopStream() {
	if((!settings?.restStreaming) && !atomicState?.restStreamingOn) {
		return
	}
	if(settings?.restStreaming && atomicState?.restStreamingOn) {
		runIn(45, "restStreamCheck", [overwrite: true])
		return
	}
	if(settings?.restStreaming && !atomicState?.restStreamingOn) {
		LogTrace("startStopStream: Stream not on, Sending restStreamHandler(Start) Event to local node service")
		restStreamHandler()
		runIn(45, "restStreamCheck", [overwrite: true])
	}
	else if (!settings?.restStreaming && atomicState?.restStreamingOn) {
		LogTrace("startStopStream: Streaming should not be running Sending restStreamHandler(Stop) Event to local node service")
		restStreamHandler(true)
		//atomicState?.restStreamingOn = false
		runIn(45, "restStreamCheck", [overwrite: true])
	}
}

def getApiURL() {
	return apiServerUrl("/api/token/${atomicState?.accessToken}/smartapps/installations/${app.id}") ?: null
}

def getRestHost() {
	def res = null
	def autoHost = settings?.selectedRestDevice ?: null
	def ip = settings?.restStreamIp ?: null
	def port = settings?.restStreamPort ?: 3000
	LogTrace("getRestHost: autoHost: ${autoHost} ip: ${ip} port: ${port}")
	if(autoHost) {
		res = autoHost
	} else {
		if(ip) {
			res = "${ip}:${port}"
		} else {
			// LogAction("No IP Address Configured for Rest Service", "warn", false)
			atomicState.restStreamingOn = false
		}
	}
	return res
}

def restStreamHandler(close = false, resetPoll=true) {
	LogTrace("restStreamHandler: close: ${close}, resetPoll: ${resetPoll}")
	def toClose = close
	def host = getRestHost()
	if(!host) {
		atomicState.restStreamingOn = false;
		host = atomicState?.lastRestHost ?: null
		atomicState.lastRestHost = null
		return
		//toClose = true
	} else {
		atomicState.lastRestHost = host
	}
	if(!close && !atomicState?.authToken) {
		LogAction("No authToken", "warn", false)
		atomicState.restStreamingOn = false
		return
	}
	LogTrace("restStreamHandler(close: ${close}) host: ${host} lastRestHost: ${atomicState?.lastRestHost}")
	def connStatus = toClose ? false : true
	LogAction("restStreamHandler(${connStatus ? "Start" : "Stop"}) Event to local node service", "debug", true)
	String hubIp = settings?.restStreamLocalHub?.getLocalIP()
	Boolean localStream = (settings?.restStreamLocal == true && hubIp)
	try {
		def hubAction = new physicalgraph.device.HubAction(
			method: "POST",
			headers: [
				"HOST": host,
				"nesttoken": "${atomicState?.authToken}",
				"stHubIp": "${hubIp}",
				"localStream": "${localStream}",
				"connStatus": "${connStatus}",
				"callback": "${getApiURL()}",
				"sttoken": "${atomicState?.accessToken}",
				"structure": "${atomicState?.structures}"
			],
			path: "/stream",
			body: ""
		)
		sendHubCommand(hubAction)
	} catch (Exception e) {
		log.error "restStreamHandler Exception $e on $hubAction"
		atomicState.restStreamingOn = false
	}
	if(toClose) {
		atomicState?.restStreamingOn = false
		if(atomicState?.streamPolling && resetPoll) {
			resetPolling()
		}
	}
}

def restStreamCheck() {
	LogTrace("restStreamCheck")
	def host = getRestHost()
	if(!host) { return }
	if(!atomicState?.authToken) {
		LogAction("restStreamCheck No authToken", "warn", false)
		return
	}
	LogTrace("restStreamCheck host: ${host}")
	String hubIp = settings?.restStreamLocalHub?.getLocalIP()
	Boolean localStream = (settings?.restStreamLocal == true && hubIp)
	try {
		atomicState.lastRestHost = host
		def hubAction = new physicalgraph.device.HubAction(
			method: "POST",
			headers: [
				"HOST": host,
				"callback": "${getApiURL()}",
				"stHubIp": "${hubIp}",
				"localStream": "${localStream}",
				"sttoken": "${atomicState?.accessToken}",
				"structure": "${atomicState?.structures}"
			],
			path: "/status",
			body: ""
		)
		sendHubCommand(hubAction)
	}
	catch (Exception e) {
		log.error "restStreamCheck Exception $e on $hubAction"
		atomicState.restStreamingOn = false
	}
}

def receiveStreamStatus(eventData=null) {
	def resp = eventData == null ? request?.JSON : eventData
	if(resp) {
		def t0 = resp?.streaming == true ? true : false
		def t1 = atomicState?.restStreamingOn
		if(t1 != t0) {		// report when changes
			LogAction("restStreamStatus: resp: ${resp}", "debug", true)
		}
		atomicState?.restStreamingOn = t0
		if(!settings?.restStreaming && t0) {		// suppose to be off
			//LogAction("receiveStreamStatus: Sending restStreamHandler(Stop) Event to local node service", "debug", true)
			restStreamHandler(true)
		} else if (settings?.restStreaming && !atomicState?.restStreamingOn) {		// suppose to be on
			runIn(45, "startStopStream", [overwrite: true])
		}
		if(settings?.restStreaming && t0) {		// All good
			updTimestampMap("lastHeardFromNestDt", getDtNow())
			if(atomicState?.ssdpOn == true) {
				unsubscribe()
				atomicState.ssdpOn = false
				subscriber()
			}
			def sData = atomicState?.swVer ?: [:]
			sData["streamDevVer"] = resp?.version ?: ""
			atomicState?.swVer = sData
			if(sData.streamDevVer != "" && (versionStr2Int(sData.streamDevVer) >= minVersions()?.stream?.val)) {
				;
			} else {
				LogAction("NST STREAM SERVICE UPDATE REQUIRED: Stream service (v${sData.streamDevVer}) | REQUIRED: (v${minVersions()?.stream?.desc}) | Update the Service to latest version", "error", true)
				appUpdateNotify()
			}
		}
		atomicState?.restServiceData = resp
		if(eventData) {
			return [data: "status received...ok", status: 200]
		} else {
			render contentType: 'text/html', data: "status received...ok", status: 200
		}
	}
}

def uninstManagerApp() {
	LogTrace("uninstManagerApp")
	try {
		restStreamHandler(true, false)   // stop the rest stream
		//Revokes Smartthings endpoint token
		revokeAccessToken()
		//Revokes Nest Auth Token
		revokeNestToken()
		if(addRemoveDevices(true)) { // if the removes were successful
			//removes analytic data from the server
			if(removeInstallData()) {
				atomicState?.installationId = null
			}
			//If any client related data exists on firebase it will be removed
			//clearRemDiagData(true)
			// clearAllAutomationBackupData()
			//sends notification of uninstall
			sendNotificationEvent("${appName()} is uninstalled")
		}
	} catch (ex) {
		log.error "uninstManagerApp Exception:", ex
		sendExceptionData(ex, "uninstManagerApp")
	}
}

def isAutoAppInst() {
	def chldCnt = 0
	childApps?.each { cApp ->
		chldCnt = chldCnt + 1
	}
	return (chldCnt > 0) ? true : false
}

def getInstAutoTypesDesc() {
	def dat = ["nestMode":0, "watchDog":0, "disabled":0, "remDiag":0, "storage":0, "schMot":["tSched":0, "remSen":0, "fanCtrl":0, "fanCirc":0, "conWat":0, "extTmp":0, "leakWat":0, "humCtrl":0 ]]
	def disItems = []
	def nItems = [:]
	def schMotItems = []
	//atomicState?.autoSaVer = minVersions()?.automation?.desc
	def sData = atomicState?.swVer ?: [:]
	sData["autoSaVer"] = null
	sData["storVer"] = null
	atomicState?.swVer = sData
	childApps?.each { a ->
		def type
		def ver
		def dis
		try {
			type = a?.getAutomationType()
			dis = a?.getIsAutomationDisabled()
			ver = a?.appVersion()
		}
		catch(ex) {
			dis = null
			ver = null
			type = "old"
		}
		if(ver) {
			def updVer = sData?.autoSaVer ?: ver
			if(versionStr2Int(ver) < versionStr2Int(updVer)) {
				updVer = ver
			}
			sData.autoSaVer = updVer
			atomicState?.swVer = sData
		}
		if(ver == null || (versionStr2Int(ver) < minVersions()?.automation?.val) || (versionStr2Int(ver) > minVersions()?.automation?.val && !getDevOpt())) {
			LogAction("NST AUTOMATIONS UPDATE REQUIRED: Automation ${a?.label} (v${ver}) | REQUIRED: (v${minVersions()?.automation?.desc}) | Please install the current NST Automations SmartApp Code in the IDE", "error", true)
			appUpdateNotify(true, "automation")
		}

		if(dis) {
			disItems.push(a?.label.toString())
			dat["disabled"] = dat["disabled"] ? dat["disabled"]+1 : 1
		} else {
			switch(type) {
				case "nMode":
					dat["nestMode"] = dat["nestMode"] ? dat["nestMode"]+1 : 1
 					if(dat.nestMode > 1) {
 						dat.nestMode = dat.nestMode - 1
 						LogAction("Deleting Extra Nest Mode (${a?.id})", "warn", true)
 						deleteChildApp(a)
						updTimestampMap("lastAnalyticUpdDt", null)
 					}
					break
				case "schMot":
					def ai
					try {
						ai = a?.getAutomationsInstalled()
						schMotItems += a?.getSchMotConfigDesc(true)
					}
					catch (Exception e) {
						log.error "BAD Automation file ${a?.label?.toString()}, please RE-INSTALL automation file"
						appUpdateNotify(true, "automation")
					}
					if(ai) {
						ai?.each { aut ->
							aut?.each { it2 ->
								if(it2?.key == "schMot") {
									it2?.value?.each {
										nItems[it] = nItems[it] ? nItems[it]+1 : 1
									}
								}
							}
						}
					}
					dat["schMot"] = nItems
					break
				case "watchDog":
					dat["watchDog"] = dat["watchDog"] ? dat["watchDog"]+1 : 1
 					if(dat.watchDog > 1) {
 						dat.watchDog = dat.watchDog - 1
 						LogAction("Deleting Extra Watchdog (${a?.id})", "warn", true)
 						deleteChildApp(a)
						updTimestampMap("lastAnalyticUpdDt", null)
 					}
 					break
				case "remDiag":
					dat["remDiag"] = dat["remDiag"] ? dat["remDiag"]+1 : 1
 					if(dat.remDiag > 1) {
 						dat.remDiag = dat.remDiag - 1
 						LogAction("Deleting Extra Remote Diagnostic (${a?.id})", "warn", true)
 						deleteChildApp(a)
						updTimestampMap("lastAnalyticUpdDt", null)
 					}
 					break
				case "storage":
					dat["storage"] = dat["storage"] ? dat["storage"]+1 : 1
 					if(dat.storage > 1) {
 						dat.storage = dat.storage - 1
 						LogAction("Deleting Extra Storage Child (${a?.id})", "warn", true)
 						deleteChildApp(a)
						updTimestampMap("lastAnalyticUpdDt", null)
 					} else if(a?.name == "NST Storage") {
						LogAction("Deleting Old Storage Child (${a?.id})", "warn", true)
 						deleteChildApp(a)
						updTimestampMap("lastAnalyticUpdDt", null)
					 }
					checkStorageApp()
					break
 				default:
 					LogAction("Deleting Unknown Automation (${a?.id})", "warn", true)
 					deleteChildApp(a)
					updTimestampMap("lastAnalyticUpdDt", null)
					break
			}
		}
	}
	atomicState?.installedAutomations = dat

	def str = ""
	str += (dat?.watchDog > 0 || dat?.nestMode > 0 || dat?.schMot || dat?.disabled > 0) ? "Installed Automations:" : ""
	str += (dat?.storage > 0) ? "\n• Storage (Active)" : ""
	str += (dat?.watchDog > 0) ? "\n• Watchdog (Active)" : ""
	str += (dat?.remDiag > 0) ? "\n• Diagnostic (Active)" : ""
	str += (dat?.nestMode > 0) ? ((dat?.nestMode > 1) ? "\n• Nest Home/Away (${dat?.nestMode})" : "\n• Nest Home/Away (Active)") : ""
	def sch = dat?.schMot.findAll { it?.value > 0}
	str += (sch?.size()) ? "\n• Thermostat (${sch?.size()})" : ""
	def scii = 1
	def newList = schMotItems?.unique()
	newList?.sort()?.each { sci ->
		str += "${scii == newList?.size() ? "\n  └" : "\n  ├"} $sci"
		scii = scii+1
	}
	str += (disItems?.size() > 0) ? "\n• Disabled: (${disItems?.size()})" : ""
	return (str != "") ? str : null
}

def subscriber() {
	subscribe(app, onAppTouch)
	if(atomicState.appData?.settings?.askAlexa?.enMultiQueue && settings?.allowAskAlexaMQ) {
		subscribe(location, "askAlexaMQ", askAlexaMQHandler) //Refreshes list of available AA queues
	}
	//Pushover Manager Init/cleanup
	if(settings?.pushoverEnabled == true) {
		pushover_init()
	} else { pushover_cleanup() }
	//Rest Stream Subriptions
	if(settings?.restStreaming && !getRestHost()) {
		restSrvcSubscribe()
	}
	if(settings?.restStreaming && settings?.restStreamLocal && settings?.restStreamLocalHub) {
		subscribe(location, null, lanStreamEvtHandler, [filterEvents:false])
	}
}

private adj_temp(tempF) {
	if(getObjType(tempF) in ["List", "ArrayList"]) {
		LogAction("adj_temp: error temp ${tempF} is list", "error", true)
	}
	if(getTemperatureScale() == "C") {
		return (tempF - 32) * (5 / 9) as Double
	} else {
		return tempF
	}
}

def setPollingState() {
	if(!atomicState?.thermostats && !atomicState?.protects && !atomicState?.weatherDevice && !atomicState?.cameras) {
		LogAction("No Devices Selected; Polling is OFF", "info", true)
		atomicState.pollingOn = false
		unschedule("poll")
		atomicState.streamPolling = false
	} else {
		if(!atomicState?.authToken) {
			atomicState.pollingOn = false
		}
		if(!atomicState?.pollingOn && atomicState?.authToken) {
			//LogAction("Polling is ACTIVE", "info", true)
			atomicState.pollingOn = true
			def pollTime = !settings?.pollValue ? 180 : settings?.pollValue.toInteger()
			def pollStrTime = !settings?.pollStrValue ? 180 : settings?.pollStrValue.toInteger()
			atomicState.streamPolling = false
			def theMax = 60
			if(settings?.restStreaming && atomicState?.restStreamingOn) {
				theMax = 300
				atomicState.streamPolling = true
			}
			pollTime = Math.max(pollTime, theMax)
			pollStrTime = Math.max(pollStrTime, theMax)
			def weatherTimer = pollTime
			if(atomicState?.weatherDevice) { weatherTimer = (settings?.pollWeatherValue ? settings?.pollWeatherValue.toInteger() : 900) }
			Integer timgcd = gcd([pollTime, pollStrTime, weatherTimer])
			def random = new Random()
			def random_int = random.nextInt(60)
			timgcd = (timgcd.div(60) < 1) ? 1 : (timgcd.div(60))
			def random_dint = random.nextInt(timgcd.toInteger())
			LogAction("POLL scheduled (${random_int} ${random_dint}/${timgcd} * * * ?)", "info", true)
			schedule("${random_int} ${random_dint}/${timgcd} * * * ?", poll)	// this runs every timgcd minutes
			atomicState?.pollBlocked = false
			atomicState?.pollBlockedReason = null
			def timChk = atomicState?.streamPolling ? 1200 : 240
			if(!getTimestampVal("lastDevDataUpd") || getLastDevPollSec() > timChk) {
				if(atomicState.streamPolling) {
					poll()
				} else { poll(true) }
			} else {
				runIn(30, "pollFollow", [overwrite: true])
			}
		}
	}
}

private gcd(a, b) {
	while (b > 0) {
		long temp = b;
		b = a % b;
		a = temp;
	}
	return a;
}

private gcd(input = []) {
	long result = input[0];
	for(int i = 1; i < input.size; i++) result = gcd(result, input[i]);
	return result;
}

def onAppTouch(event) {
	/*
	// ERS Debug to remove
			def sData = atomicState?.swVer ?: [:]
			sData["mgrVer"] = "tester"
			atomicState?.swVer = sData
			return
	*/
	stateCleanup()
	createSavedNest()
	poll(true)
}

def refresh(child = null) {
	def devId = !child?.device?.deviceNetworkId ? child?.toString() : child?.device?.deviceNetworkId.toString()
	LogAction("Refresh Received Device:${devId}", "debug", false)
	return sendNestApiCmd(atomicState?.structures, "poll", "poll", 0, devId)
}

/************************************************************************************************
|								API/Device Polling Methods										|
*************************************************************************************************/

def pollFollow() { poll() }

def checkIfSwupdated() {
	// if(checkMigrationRequired()) { return true }
	if(atomicState?.swVer?.mgrVer != appVersion()) {
		def sData = atomicState?.swVer ?: [:]
		sData["mgrVer"] = appVersion()
		atomicState?.swVer = sData
		updTimestampMap("lastAnalyticUpdDt", null)
		LogAction("checkIfSwupdated: new version ${appVersion()}", "info", true)
		def iData = atomicState?.installData
		iData["updatedDt"] = getDtNow().toString()
		iData["shownChgLog"] = false
		iData["shownFeedback"] = false
		iData["shownDonation"] = false
		atomicState?.installData = iData
// force full fixState
		settingUpdate("resetAllData", "true", "bool")
		atomicState.resetAllData = false
		atomicState.pollBlocked = true
		atomicState.pollBlockedReason = "Software Update pending"
		runIn(20, "updated", [overwrite: true])
		//updated()
		sendInstallSlackNotif(false)
		return true
	}
	return false
}

def poll(force = false, type = null) {
	if(isPollAllowed()) {
		if(checkIfSwupdated()) { return }
		if(force == true) {
			forcedPoll(type)
			finishPoll()
			return
		}
		def pollTime = !settings?.pollValue ? 180 : settings?.pollValue.toInteger()
		if(settings?.restStreaming && atomicState?.restStreamingOn) {
			pollTime = 60*5
		}
		def pollTimeout = pollTime + 85

		if(getLastHeardFromNestSec() > pollTimeout) {
			if(settings?.restStreaming && atomicState?.restStreamingOn) {
				LogAction("Have not heard from Rest Stream", "warn", true)
				restStreamHandler(true, false)   // close the stream if we have not heard from it in a while
				//atomicState?.restStreamingOn = false
			}
		}

		if(atomicState?.streamPolling && (!settings?.restStreaming || !atomicState?.restStreamingOn)) {	// return to normal polling
			resetPolling()
			return
		}

		if(settings?.restStreaming && atomicState?.restStreamingOn) {
			LogAction("Skipping Poll because Rest Streaming is ON", "info", false)
			if(!atomicState?.streamPolling) {	// set to stream polling
				resetPolling()
				return
			}
			restStreamCheck()
			finishPoll()
			return
		}
		startStopStream()

		def okStruct = ok2PollStruct()
		def okDevice = ok2PollDevice()
		def okMeta = ok2PollMetaData()
		def meta = false
		def dev = false
		def str = false
		if(!okDevice && !okStruct && !(getLastHeardFromNestSec() > pollTimeout*2)) {
			LogAction("No Device or Structure poll - Devices Last Updated: ${getLastDevPollSec()} seconds ago | Structures Last Updated ${getLastStrPollSec()} seconds ago", "info", true)
		} else {
			def sstr = ""
			def metstr = "async"
			if(okStruct) {
				sstr += "Updating Structure Data (Last Updated: ${getLastStrPollSec()} seconds ago)"
				str = queueGetApiData("str")
			}
			if(okDevice) {
				sstr += sstr != "" ? " | " : ""
				sstr += "Updating Device Data (Last Updated: ${getLastDevPollSec()} seconds ago)"
				dev = queueGetApiData("dev")
			}
			if(okMeta) {
				sstr += sstr != "" ? " | " : ""
				sstr += "Updating Meta Data(Last Updated: ${getLastMetaPollSec()} seconds ago)"
				meta = queueGetApiData("meta")
			}
			if(sstr != "") { LogAction("${sstr} (${metstr})", "info", true) }
			return
		}
		finishPoll(str, dev)
	} else if(atomicState?.cltBlacklisted) {
		LogAction("Client poll is BLACKLISTED. Please contact the Developer", "warn", true)
		finishPoll(false, true)
	}
}

def finishPoll(str=null, dev=null) {
	LogTrace("finishPoll($str, $dev) received")
	def lastDevUpd = getLastChildUpdSec()
	if(!atomicState?.pollingOn) {
		LogAction("finishPoll: Polling not ON", "warn", true);
		return
	}
	if(atomicState?.pollBlocked) {
		LogAction("finishPoll: Polling BLOCKED | Reason: (${atomicState?.pollBlockedReason})", "warn", true);
		if( (atomicState?.apiRateLimited && lastDevUpd > 35*60) || (lastDevUpd > 45*60 && (atomicState?.needChildUpd || atomicState?.forceChildUpd) ) ) {
			LogAction("finishPoll: ReRunning Updated() | Polling blocked | Last device update ${lastDevUpd} | Rate Limited: ${atomicState?.apiRateLimited}", "warn", true);
			runIn(5, "updated", [overwrite: false]) // ensure it does not keep delaying
			return
		}
		if(getLastAnyCmdSentSeconds() > 75) { // if poll is blocked and we have not sent a command recently, try to kick the queues
			schedNextWorkQ();
		}
		return
	}
	if(getLastChildForceUpdSec() > (15*60)-2) { // if nest goes silent (no changes coming back); force all devices to get an update so they can check health
		atomicState.forceChildUpd = true
	}
	if(dev || str || atomicState?.forceChildUpd || atomicState?.needChildUpd) { runIn(1, "updateChildData", [overwrite : true]) }
	updateWebStuff()
	notificationCheck() //Checks if a notification needs to be sent for a specific event
	broadcastCheck()
	if(atomicState?.enRemDiagLogging && settings?.enRemDiagLogging) {
		saveLogtoRemDiagStore("", "", "", true) // force flush of remote logs
	}
}

def resetPolling() {
	atomicState.pollingOn = false
	atomicState.streamPolling = false
	unschedule("poll")
	unschedule("finishPoll")
	unschedule("postCmd")
	unschedule("pollFollow")
	setPollingState()		// will call poll
}

def schedFinishPoll(devChg) {
	if(isPollAllowed()) {
		finishPoll(false, devChg)
	}
	return
}

def forcedPoll(type = null) {
	LogAction("forcedPoll($type) received", "warn", true)
	def lastFrcdPoll = getLastForcedPollSec()
	def pollWaitVal = !settings?.pollWaitVal ? 10 : settings?.pollWaitVal.toInteger()
	pollWaitVal = Math.max(pollWaitVal, 10)

	if(lastFrcdPoll > pollWaitVal) { // This limits manual forces to 10 seconds or more
		updTimestampMap("lastForcePoll", getDtNow())
		atomicState?.workQrunInActive = false
		atomicState?.pollBlocked = false
		atomicState?.pollBlockedReason = null
		cmdProcState(false)

		LogAction("Last Forced Update was ${lastFrcdPoll} seconds ago.", "info", true)
		if(type == "dev" || !type) {
			LogAction("Updating Device Data (forcedPoll)", "info", true)
			getApiData("dev")
		}
		if(type == "str" || !type) {
			LogAction("Updating Structure Data (forcedPoll)", "info", true)
			getApiData("str")
		}
		if(type == "meta" || !type) {
			LogAction("Updating Meta Data (forcedPoll)", "info", true)
			getApiData("meta")
		}
		updTimestampMap("lastWebUpdDt", null)
		updTimestampMap("lastWeatherUpdDt" , null)
		updTimestampMap("lastForecastUpdDt", null)
		schedNextWorkQ()
	} else {
		LogAction("Too Soon for Update; Elapsed (${lastFrcdPoll}) seconds; minimum (${settings?.pollWaitVal})", "debug", true)
		atomicState.needStrPoll = true
		atomicState.needDevPoll = true
	}
	atomicState.forceChildUpd = true
	//runIn(1, "updateChildData", [overwrite : true])
	//updateChildData()
}

def postCmd() {
	//LogTrace("postCmd()")
	poll()
}

def getApiData(type = null) {
	LogTrace("getApiData($type)")
	//LogAction("getApiData($type)", "info", false)
	def result = false
	if(!type || !atomicState?.authToken) { return result }

	switch(type) {
		case "str":
		case "dev":
		case "meta":
			break
		default:
			return result
	}
	def tPath = (type == "str") ? "/structures" : ((type == "dev") ? "/devices" : "/")
	def params = [
		uri: getNestApiUrl(),
		path: "$tPath",
		headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState?.authToken}"]
	]
	try {
		httpGet(params) { resp ->
			if(resp?.status == 200) {
				updTimestampMap("lastHeardFromNestDt", getDtNow())
				apiIssueEvent(false)
				//atomicState?.apiRateLimited = false
				//atomicState?.apiCmdFailData = null
				if(type == "str") {
					def t0 = resp?.data
					//LogTrace("API Structure Resp.Data: ${t0}")
					if(atomicState?.structData == null) { atomicState?.structData = t0 }
					def chg = didChange(atomicState?.structData, t0, "str", "poll")
					if(chg) {
						result = true
						def newStrucName = atomicState?.structData?.size() && atomicState?.structures ? atomicState?.structData[atomicState?.structures]?.name : null
						atomicState.structName = newStrucName ?: atomicState?.structName
						locationPresNotify(getLocationPresence())
					}
					incrementCntByKey("apiStrReqCnt")
				}
				else if(type == "dev") {
					def t0 = resp?.data
					//LogTrace("API Device Resp.Data: ${t0}")
					def chg = didChange(atomicState?.deviceData, t0, "dev", "poll")
					if(chg) { result = true }
					incrementCntByKey("apiDevReqCnt")
				}
				else if(type == "meta") {
					//LogTrace("API Metadata Resp.Data: ${resp?.data}")
					def nresp = resp?.data?.metadata
					def chg = didChange(atomicState?.metaData, nresp, "meta", "poll")
					if(chg) { result = true }
					incrementCntByKey("apiMetaReqCnt")
				}
			} else {
				LogAction("getApiData - ${type} Received: Resp (${resp?.status})", "error", true)
				apiRespHandler(resp?.status, resp?.data, "getApiData(${type})", "${type} Poll")
				apiIssueEvent(true)
				atomicState.forceChildUpd = true
			}
		}
	} catch (ex) {
		//atomicState?.apiRateLimited = false
		atomicState.forceChildUpd = true
		log.error "getApiData (type: $type) Exception:", ex
		if(ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
			apiRespHandler(ex?.response?.status, ex?.response?.data, "getApiData(ex catch)", "${type} Poll")
		} else {
			if(type == "str") { atomicState.needStrPoll = true }
			else if(type == "dev") { atomicState?.needDevPoll = true }
			else if(type == "meta") { atomicState?.needMetaPoll = true }
			sendExceptionData(ex, "getApiData")
		}
		apiIssueEvent(true)
	}
	return result
}

def queueGetApiData(type = null, newUrl = null) {
	LogTrace("queueGetApiData($type,$newUrl)")
	def result = false
	if(!type || !atomicState?.authToken) { return result }

	def tPath = (type == "str") ? "/structures" : ((type == "dev") ? "/devices" : "/")
	try {
		def theUrl = newUrl ?: getNestApiUrl()
		def params = [
			uri: theUrl,
			path: "$tPath",
			headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState?.authToken}"]
		]
		if(type == "str") {
			atomicState.qstrRequested = true
			asynchttp_v1.get(procNestResponse, params, [ type: "str"])
			result = true
		}
		else if(type == "dev") {
			atomicState.qdevRequested = true
			asynchttp_v1.get(procNestResponse, params, [ type: "dev"])
			result = true
		}
		else if(type == "meta") {
			asynchttp_v1.get(procNestResponse, params, [ type: "meta"])
			result = true
		}
	} catch(ex) {
		log.error "queueGetApiData (type: $type) Exception:", ex
		sendExceptionData(ex, "queueGetApiData")
	}
	return result
}

def procNestResponse(resp, data) {
	LogTrace("procNestResponse(${data?.type})")
	LogAction("procNestResponse | Status: ${resp?.getStatus()} | data: $data", "info", false)
	def str = false
	def dev = false
	def meta = false
	def type = data?.type
	try {
		if(!type) { return }

		if(resp?.status == 307) {
			//LogTrace("resp: ${resp.headers}")
			def newUrl = resp?.headers?.Location?.split("\\?")
			//LogTrace("NewUrl: ${newUrl[0]}")
			queueGetApiData(type, newUrl[0])
			return
		}

		if(resp?.status == 200) {
			updTimestampMap("lastHeardFromNestDt", getDtNow())
			apiIssueEvent(false)
			//atomicState?.apiRateLimited = false
			//atomicState?.apiCmdFailData = null
			if(type == "str") {
				def t0 = resp?.json
				//LogTrace("API Structure Resp.Data: ${t0}")
				//if(atomicState?.structData == null) { atomicState?.structData = t0 }
				def chg = didChange(atomicState?.structData, t0, "str", "poll(async)")
				if(chg) {
					str = true
					def newStrucName = atomicState?.structData && atomicState?.structures ? atomicState?.structData[atomicState?.structures]?.name : null
					atomicState.structName = newStrucName ?: atomicState?.structName
					locationPresNotify(getLocationPresence())
				}
				atomicState.qstrRequested = false
				incrementCntByKey("apiStrReqCnt")
			}
			if(type == "dev") {
				def t0 = resp?.json
				//LogTrace("API Device Resp.Data: ${t0}")
				def chg = didChange(atomicState?.deviceData, t0, "dev", "poll(async)")
				if(chg) {
					dev = true
				}
				atomicState.qdevRequested = false
				incrementCntByKey("apiDevReqCnt")
			}
			if(type == "meta") {
				def nresp = resp?.json?.metadata
				//LogTrace("API Meta Resp.Data: ${resp?.json}")
				def chg = didChange(atomicState?.metaData, nresp, "meta", "poll(async)")
				if(chg) {
					meta = true
				}
				incrementCntByKey("apiMetaReqCnt")
			}
		} else {
			def tstr = (type == "str") ? "Structure" : ((type == "dev") ? "Device" : "Metadata")
			tstr += " Poll async"
			//LogAction("procNestResponse - Received $tstr: Resp (${resp?.status})", "error", true)
			if(resp?.hasError()) {
				def rCode = resp?.getStatus() ?: null
				def errJson = resp?.getErrorJson() ?: null
				//log.debug "rCode: $rCode | errJson: $errJson"
				apiRespHandler(rCode, errJson, "procNestResponse($type)", tstr)
			}
			apiIssueEvent(true)
			atomicState.forceChildUpd = true
			atomicState.qstrRequested = false
			atomicState.qdevRequested = false
		}
		if((atomicState?.qdevRequested == false && atomicState?.qstrRequested == false) && (dev || atomicState?.forceChildUpd || atomicState?.needChildUpd)) {
			if(isPollAllowed()) {
				finishPoll(true, true)
			}
		}

	} catch (ex) {
		def tstr = (type == "str") ? "Structure" : ((type == "dev") ? "Device" : "Metadata")
		tstr += " Poll async"
		//LogAction("procNestResponse - Received $tstr: Resp (${resp?.status})", "error", true)
		if(ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
			apiRespHandler(ex?.response?.status, ex?.response?.data, "procNestResponse($type)", tstr)
		} else {
			if(resp?.hasError()) {
				def rCode = resp?.getStatus() ?: null
				def errJson = resp?.getErrorJson() ?: null
				//log.debug "rCode: $rCode | errJson: $errJson"
				apiRespHandler(rCode, errJson, "procNestResponse($type)", tstr)
			}
		}
		apiIssueEvent(true)
		//atomicState?.apiRateLimited = false
		atomicState.forceChildUpd = true
		atomicState.qstrRequested = false
		atomicState.qdevRequested = false
		if(type == "str") { atomicState.needStrPoll = true }
		else if(type == "dev") { atomicState?.needDevPoll = true }
		else if(type == "meta") { atomicState?.needMetaPoll = true }
		log.error "procNestResponse (type: $type) | Exception:", ex
		sendExceptionData("${ex}", "procNestResponse_${type}")
	}
}

def receiveEventData(eventData=null) {
	def status = [:]
	try {
		def evtData = eventData == null ? request?.JSON : eventData
		//LogAction("evtData: $evtData", "trace", true)
		def devChgd = false
		def gotSomething = false
		if(evtData?.data && settings?.restStreaming && atomicState?.restStreamingOn) {
			if(evtData?.data?.devices) {
				//LogTrace("API Device Resp.Data: ${evtData?.data?.devices}")
				gotSomething = true
				def chg = didChange(atomicState?.deviceData, evtData?.data?.devices, "dev", "stream")
				if(chg) {
					devChgd = true
				} else {
					LogTrace("got deviceData")
				}
			}
			if(evtData?.data?.structures) {
				//LogTrace("API Structure Resp.Data: ${evtData?.data?.structures}")
				gotSomething = true
				def chg = didChange(atomicState?.structData, evtData?.data?.structures, "str", "stream")
				if(chg) {
					def newStrucName = atomicState?.structData && atomicState?.structures ? atomicState?.structData[atomicState?.structures]?.name : null
					atomicState.structName = newStrucName ?: atomicState?.structName
					locationPresNotify(getLocationPresence())
				} else {
					LogTrace("got structData")
				}
			}
			if(evtData?.data?.metadata) {
				//LogTrace("API Metadata Resp.Data: ${evtData?.data?.metadata}")
				gotSomething = true
				def chg = didChange(atomicState?.metaData, evtData?.data?.metadata, "meta", "stream")
				if(!chg) { LogTrace("got metaData") }
			}
		} else {
			def forceStop = false
			if(!settings?.restStreaming) { forceStop = true }
			if(!forceStop && !atomicState?.restStreamingOn) {
				LogAction("receiveEventData: stream not on yet, ignoring", "debug", true)
			}
			if(forceStop) {
				LogAction("receiveEventData: Sending restStreamHandler(Stop)", "warn", true)
				restStreamHandler(true)
			}
		}
		if(gotSomething) {
			updTimestampMap("lastHeardFromNestDt", getDtNow())
			if(atomicState?.ssdpOn == true) {
				unsubscribe() //These were causing exceptions
				atomicState?.ssdpOn = false
				subscriber()
			}
			//apiIssueEvent(false)
			//atomicState?.apiRateLimited = false
			//atomicState?.apiCmdFailData = null
			incrementCntByKey("apiRestStrEvtCnt")
		}
		if(atomicState?.forceChildUpd || atomicState?.needChildUpd || devChgd) {
			schedFinishPoll(devChgd)
		}
		status = [data:"status received...ok", code:200]
	} catch (ex) {
		log.error "receiveEventData Exception:", ex
		LogAction("receiveEventData Exception: ${ex}", "error", true)
		status = [data:"${ex?.message}", code:500]
		//apiIssueEvent(true)
	}
	if(eventData) {
		return status
	} else {
		render contentType: 'text/html', data: status?.data, status: status?.code
	}
}

def lanStreamEvtHandler(evt) {
	// log.trace "lanStreamEvtHandler..."
	def status = [:]
	try {
		def msg = parseLanMessage(evt?.description)
		Map headerMap = msg?.headers
		// log.debug "lanStreamEvtHandler... | headers: ${headerMap}"
		Map msgData = [:]
		if (headerMap?.size()) {
			if (headerMap?.evtSource && headerMap?.evtSource == "NST_Stream") {
				if (msg?.body != null) {
					def slurper = new groovy.json.JsonSlurper()
					msgData = slurper.parseText(msg?.body)
					// log.debug "msgData: $msgData"
					if(headerMap?.evtType) {
						switch(headerMap?.evtType) {
							case "streamStatus":
								status = receiveStreamStatus(msgData)
								break
							case "sendEventData":
								status = receiveEventData(msgData)
								break
						}
					}
				}
			}
		}
	} catch (ex) {
		log.error "lanStreamEvtHandler Exception:", ex
		status = [data:"${ex?.message}", code: 500]
	}
	render contentType: 'text/html', data: status?.data, status: status?.code
}

def didChange(old, newer, type, src) {
	//LogTrace("didChange: type: $type src: $src")
	Boolean result = false
	String srcStr = src.toString().toUpperCase()
	if(newer != null) {
		if(type == "str") {
			updTimestampMap("lastStrDataUpd", getDtNow())
			atomicState.needStrPoll = false
			newer.each {
				if(it?.value) {
					def myId = it?.value?.structure_id
					if(myId) {
						newer[myId].wheres = [:]
					}
				}
			}
		}
		if(type == "dev") {
			updTimestampMap("lastDevDataUpd", getDtNow())
			atomicState?.needDevPoll = false
			newer.each { t ->		// This reduces stored state size
				def dtyp = t.key
				t.value.each {
					if(it?.value) {
						def myId = it?.value?.device_id
						if(myId) {
							newer."${dtyp}"[myId].where_id = ""
							if(newer."${dtyp}"[myId]?.app_url) {
								newer."${dtyp}"[myId].app_url = ""
							}
							if(newer."${dtyp}"[myId]?.last_event?.app_url) {
								newer."${dtyp}"[myId].last_event.app_url = ""
							}
							if(newer."${dtyp}"[myId]?.last_event?.image_url) {
								newer."${dtyp}"[myId].last_event.image_url = ""
							}
						}
					}
				}
			}
		}
		if(type == "meta") {
			updTimestampMap("lastMetaDataUpd", getDtNow())
			atomicState.needMetaPoll = false
		}
		if(old != newer) {
			if(type == "str") {
				def tt0 = atomicState?.structData?.size() ? atomicState?.structData : null
				// Null safe does not work on array references that miss
				def t0 = tt0 && atomicState?.structures && tt0?."${atomicState?.structures}" ? tt0[atomicState?.structures] : null
				def t1 = newer && atomicState?.structures && newer?."${atomicState?.structures}" ? newer[atomicState?.structures] : null

				if(t1 && t0 != t1) {
					result = true
					atomicState?.forceChildUpd = true
					LogTrace("structure old newer not the same ${atomicState?.structures}")
					// whatChanged(t0, t1, "/structures", "structure")
					if(settings?.showDataChgdLogs == true && atomicState?.enRemDiagLogging != true) {
						def chgs = getChanges(t0, t1, "/structures", "structure")
						if(chgs) { LogAction("STRUCTURE Changed ($srcStr): ${chgs}", "info", true) }
					} else {
						LogAction("API Structure Data HAS Changed ($srcStr)", "info", true)
					}
				}
				atomicState?.structData = newer
			}
			else if(type == "dev") {
				def devChg = false
				def tstats = atomicState?.thermostats.collect { dni ->
					def t1 = dni.key
					if(t1 && old && old?.thermostats && newer?.thermostats &&
						old?.thermostats[t1] && newer?.thermostats[t1] && old?.thermostats[t1] == newer?.thermostats[t1]) {
					} else {
						result = true
						atomicState.needChildUpd = true
						LogTrace("thermostat old newer not the same ${t1}")
						if(t1 && old && old?.thermostats && newer?.thermostats && old?.thermostats[t1] && newer?.thermostats[t1]) {
							// whatChanged(old?.thermostats[t1], newer?.thermostats[t1], "/devices/thermostats/${t1}", "thermostat")
							if(settings?.showDataChgdLogs == true && atomicState?.enRemDiagLogging != true) {
								def chgs = getChanges(old?.thermostats[t1], newer?.thermostats[t1], "/devices/thermostats/${t1}", "thermostat")
								if(chgs) {
									LogAction("THERMOSTAT Changed ($srcStr) | ${getChildDeviceLabel(t1)}: ${chgs}", "info", true)
								}
							} else { devChg = true }
						}
					}
				}

				def nProtects = atomicState?.protects.collect { dni ->
					def t1 = dni.key
					if(t1 && old && old?.smoke_co_alarms && newer?.smoke_co_alarms &&
						old?.smoke_co_alarms[t1] && newer?.smoke_co_alarms[t1] && old?.smoke_co_alarms[t1] == newer?.smoke_co_alarms[t1]) {
					} else {
						result = true
						atomicState.needChildUpd = true
						LogTrace("protect old newer not the same ${t1}")
						if(t1 && old && old?.smoke_co_alarms && newer?.smoke_co_alarms && old?.smoke_co_alarms[t1] && newer?.smoke_co_alarms[t1]) {
							// whatChanged(old?.smoke_co_alarms[t1], newer?.smoke_co_alarms[t1], "/devices/smoke_co_alarms/${t1}", "protect")
							if(settings?.showDataChgdLogs == true && atomicState?.enRemDiagLogging != true) {
								def chgs = getChanges(old?.smoke_co_alarms[t1], newer?.smoke_co_alarms[t1], "/devices/smoke_co_alarms/${t1}", "protect")
								if(chgs) {
									LogAction("PROTECT Changed ($srcStr) | ${getChildDeviceLabel(t1)}: ${chgs}", "info", true)
								}
							} else { devChg = true }
						}
					}
				}

				def nCameras = atomicState?.cameras.collect { dni ->
					def t1 = dni.key
					if(t1 && old && old?.cameras && newer?.cameras &&
						old?.cameras[t1] && newer?.cameras[t1] && old?.cameras[t1] == newer?.cameras[t1]) {
					} else {
						result = true
						atomicState.needChildUpd = true
						LogTrace("camera old newer not the same ${t1}")
						if(t1 && old && old?.cameras && newer?.cameras && old?.cameras[t1] && newer?.cameras[t1]) {
							//whatChanged(old?.cameras[t1], newer?.cameras[t1], "/devices/cameras/${t1}", "camera")
							if(settings?.showDataChgdLogs == true && atomicState?.enRemDiagLogging != true) {
								def chgs = getChanges(old?.cameras[t1], newer?.cameras[t1], "/devices/cameras/${t1}", "camera")
								if(chgs) {
									LogAction("CAMERA Changed ($srcStr) | ${getChildDeviceLabel(t1)}: ${chgs}", "info", true)
								}
							} else { devChg = true }
						}
					}
				}
				if(devChg && (atomicState?.enRemDiagLogging == true || settings?.showDataChgdLogs != true)) {
					LogAction("API Device Data HAS Changed ($srcStr)", "info", true)
				}
				atomicState?.deviceData = newer
			}
			else if(type == "meta") {
				result = true
				atomicState.needChildUpd = true
				atomicState.metaData = newer
				//whatChanged(old, newer, "/metadata", "metadata")
				if(atomicState?.enRemDiagLogging == true || settings?.showDataChgdLogs != true) {
					LogAction("API MetaData Data HAS Changed ($srcStr)", "info", true)
				} else {
					def chgs = getChanges(old, newer, "/metadata", "metadata")
					if(chgs) {
						LogAction("METADATA Changed ($srcStr): ${chgs}", "info", true)
					}
				}
			}
		}
	}
	//LogAction("didChange: type: $type src: $src result: $result", "info", true)
	return result
}

def getChanges(mapA, mapB, headstr, objType=null) {
	def t0 = mapA
	def t1 = mapB
	def left = t0
	def right = t1
	def itemsChgd = []
	if (left instanceof Map) {
		String[] leftKeys = left.keySet()
		String[] rightKeys = right.keySet()
		leftKeys.each {
			//if ( (left[it] instanceof List) || (left[it] instanceof ArrayList) || (left[it] instanceof Map)) {
			if ( left[it] instanceof Map ) {
				def chgs = getChanges( left[it], right[it], "${headstr}/${it}", objType )
				if(chgs && objType) {
					itemsChgd += chgs
				}
			} else {
				if (left[it].toString() != right[it].toString()) {
					if(objType) {
						itemsChgd.push(it.toString())
					}
				}
			}
		}
		if(itemsChgd.size()) { return itemsChgd }
	}
	return null
}

def whatChanged(mapA, mapB, headstr) {
	def t0 = mapA
	def t1 = mapB
	def left = t0
	def right = t1

	if(left == null || right == null) {
		LogAction("Object: $headstr NULL", "trace", true)
		return false
	}

	if (getObjType(left) != getObjType(right)) {
		LogAction("Object ${headstr} comparison failure: Mismatch object classes. ${getObjType(left)} ${getObjType(right)}", "trace", true)
		return false
	}

	if (left instanceof List || left instanceof ArrayList) {
		if (left.size() != right.size()) {
			LogAction("Array ${headstr} comparison failure: Object size mismatch.", "trace", true)
			LogAction("ORIG has " + left.size() + " items. NEW has " + right.size() + " items.", "trace", true)
			LogAction("ORIG Object: ${left} NEW Object: ${right}", "trace", true)
			return false
		}
		for(int i=0; i < left.size(); i++) {
			// May detect matching items here if sort of objects is problem
			whatChanged(left[i], right[i], "${headstr}/${i}")
		}
	} else if (left instanceof Map) {
		String[] leftKeys = left.keySet()
		String[] rightKeys = right.keySet()
		if (leftKeys.sort() != rightKeys.sort()) {
			LogAction("Map ${headstr} comparison failure: Orig keys do not match new keys.", "trace", true)
			LogAction("ORIG " + leftKeys.toString() + " NEW " + rightKeys.toString(), "trace", true)
			return false
		}
		def ret = true
		leftKeys.each {
			//if(!ret) { return }

			if ( (left[it] instanceof List) || (left[it] instanceof ArrayList) || (left[it] instanceof Map)) {
				// May detect matching items here if sort of objects is problem
				whatChanged( left[it], right[it], "${headstr}/${it}" )
			} else {
				if (left[it].toString() != right[it].toString()) {
					LogAction("String comparison ${headstr} failure: Orig " + it + " value does not match new value.", "trace", true)
					LogAction("\nORIG " + left[it] + "\nNEW " + right[it], "trace", true)
					ret = false
				}
			}
		}
	}
	return
}

def updateChildData(force = false) {
	LogTrace("updateChildData($force) ${atomicState?.forceChildUpd} ${atomicState?.needChildUpd}")
	if(atomicState?.pollBlocked) { return }
	def nforce = atomicState?.forceChildUpd
	atomicState.forceChildUpd = true
	try {
		updTimestampMap("lastChildUpdDt", getDtNow())
		if(force || nforce) {
			updTimestampMap("lastChildForceUpdDt", getDtNow())
		}
		def useMt = !useMilitaryTime ? false : true
		def dbg = !childDebug ? false : true
		def logNamePrefix = (settings?.debugAppendAppName || settings?.debugAppendAppName == null) ? true : false
		def remDiag = (atomicState?.enRemDiagLogging && settings?.enRemDiagLogging) ? true: false
		def nestTz = getNestTimeZone()?.toString()
		def api = apiIssueType()
		def mobClientType = settings?.mobileClientType
		def vRprtPrefs = getVoiceRprtPrefs()
		def clientBl = atomicState?.cltBlacklisted == true ? true : false
		def hcCamTimeout = atomicState?.appData?.settings?.healthcheck?.camTimeout ?: 120
		def hcProtWireTimeout = atomicState?.appData?.settings?.healthcheck?.protWireTimeout ?: 45
		def hcProtBattTimeout = atomicState?.appData?.settings?.healthcheck?.protBattTimeout ?: 1500
		def hcTstatTimeout = atomicState?.appData?.settings?.healthcheck?.tstatTimeout ?: 45
		def hcLongTimeout = atomicState?.appData?.settings?.healthcheck?.longTimeout ?: 120
		def hcRepairEnabled = atomicState?.appData?.settings?.healthcheck?.repairEnabled != false ? true : false
		def locPresence = getLocationPresence()
		def locSecurityState = getSecurityState()
		def locEtaBegin = getEtaBegin()
		if(atomicState?.notificationPrefs == null) { atomicState?.notificationPrefs = buildNotifPrefMap() }
		def nPrefs = atomicState?.notificationPrefs
		def devBannerData = atomicState?.devBannerData ?: null
		def streamingActive = atomicState?.restStreamingOn == true ? true : false
		def isBeta = betaMarker()
		def camTakeSnapOnEvt = settings?.camTakeSnapOnEvt == false ? false : true

		def curWeatherData = [:]
		if(atomicState?.thermostats && getWeatherDeviceInst()) {
			def cur = getWeatherData("curWeather")
			if(cur) {
				curWeatherData["temp"] = cur?.temperature ?: null
				curWeatherData["hum"] = cur?.relativeHumidity ?: 0
			}
		}
		def showGraphs = settings?.tstatShowHistoryGraph == false ? false : true

		if(settings?.devNameOverride == null || atomicState?.devNameOverride == null) { // Upgrade force to on
			atomicState?.devNameOverride = true;
			settingUpdate("devNameOverride", "true", "bool")
		}
		def overRideNames = (atomicState?.devNameOverride) ? true : false
		//def devCodeIds = atomicState?.devCodeIdData ?: [:]
		def devices = app.getChildDevices(true)
		devices?.each {
			if(atomicState?.pollBlocked) { return true }
			def devId = it?.deviceNetworkId
			if(devId && atomicState?.thermostats && atomicState?.deviceData?.thermostats && atomicState?.deviceData?.thermostats[devId]) {
				//devCodeIds["tstat"] = it?.getDevTypeId()
				def defmin = fixTempSetting(atomicState?."${devId}_safety_temp_min" ?: null)
				def defmax = fixTempSetting(atomicState?."${devId}_safety_temp_max" ?: null)
				def safetyTemps = [ "min":defmin, "max":defmax ]

				def comfortDewpoint = fixTempSetting(settings?."${devId}_comfort_dewpoint_max" ?: null)
				if(comfortDewpoint == 0) {
					comfortDewpoint = fixTempSetting(settings?.locDesiredComfortDewpointMax ?: null)
				}
				def comfortHumidity = settings?."${devId}_comfort_humidity_max" ?: 80
				if(nforce) {
					atomicState?."oldTstatSchedData${devId}" = null
				}
				def oldTstatSchedData = atomicState?."oldTstatSchedData${devId}"
				if(oldTstatSchedData == null) {
					oldTstatSchedData = reqSchedInfoRprt(it, false) as Map
					atomicState?."oldTstatSchedData${devId}" = oldTstatSchedData
				}
				def autoSchedData = oldTstatSchedData as Map
				def tData = ["data":atomicState?.deviceData?.thermostats[devId], "mt":useMt, "debug":dbg, "tz":nestTz, "apiIssues":api, "safetyTemps":safetyTemps, "comfortHumidity":comfortHumidity,
						"comfortDewpoint":comfortDewpoint, "pres":locPresence, "childWaitVal":getChildWaitVal().toInteger(), "allowDbException":allowDbException,
						"latestVer":latestTstatVer()?.ver?.toString(), "vReportPrefs":vRprtPrefs, "clientBl":clientBl, "curWeatherData":curWeatherData, "logPrefix":logNamePrefix, "hcTimeout":hcTstatTimeout,
						"mobileClientType":mobClientType, "enRemDiagLogging":remDiag, "autoSchedData":autoSchedData, "healthNotify":nPrefs?.dev?.devHealth, "showGraphs":showGraphs,
						"devBannerData":devBannerData, "restStreaming":streamingActive, "isBeta":isBeta, "hcRepairEnabled":hcRepairEnabled, "etaBegin":locEtaBegin ]
				def oldTstatData = atomicState?."oldTstatData${devId}"
				def tDataChecksum = generateMD5_A(tData.toString())
				atomicState."oldTstatData${devId}" = tDataChecksum
				tDataChecksum = atomicState."oldTstatData${devId}"
				if(force || nforce || (oldTstatData != tDataChecksum)) {
					physDevLblHandler("thermostat", devId, it?.label, "thermostats", tData?.data?.name.toString(), "tstat", overRideNames)
					def t1 = it?.currentState("devVer")?.value?.toString()
					def sData = atomicState?.swVer ?: [:]
					sData["tDevVer"] = t1 ?: ""
					atomicState?.swVer = sData
					if(sData?.tDevVer != "" && (versionStr2Int(sData?.tDevVer) >= minVersions()?.thermostat?.val)) {
						//LogTrace("UpdateChildData >> Thermostat id: ${devId} | data: ${tData}")
						LogTrace("updateChildData >> Thermostat id: ${devId} | oldTstatData: ${oldTstatData} tDataChecksum: ${tDataChecksum} force: $force nforce: $nforce")
						if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
					} else {
						if(atomicState?."lastUpdated${devId}Dt" == null) {
							atomicState."lastUpdated${devId}Dt" = getDtNow()
						} else {
							LogAction("NST THERMOSTAT DEVICE UPDATE REQUIRED: Thermostat ${devId} (v${sData?.tDevVer}) | REQUIRED: (v${minVersions()?.thermostat?.desc}) | Update the Device code to the latest software in the IDE", "error", true)
							appUpdateNotify()
						}
					}
					it?.generateEvent(tData)
				}
				return true
			}
			else if(devId && atomicState?.protects && atomicState?.deviceData?.smoke_co_alarms && atomicState?.deviceData?.smoke_co_alarms[devId]) {
				//devCodeIds["protect"] = it?.getDevTypeId()
				def pData = ["data":atomicState?.deviceData?.smoke_co_alarms[devId], "mt":useMt, "debug":dbg, "showProtActEvts":(!showProtActEvts ? false : true), "logPrefix":logNamePrefix,
						"tz":nestTz, "apiIssues":api, "allowDbException":allowDbException, "latestVer":latestProtVer()?.ver?.toString(), "clientBl":clientBl,
						"hcWireTimeout":hcProtWireTimeout, "hcBattTimeout":hcProtBattTimeout, "mobileClientType":mobClientType, "enRemDiagLogging":remDiag, "healthNotify":nPrefs?.dev?.devHealth,
						"devBannerData":devBannerData, "restStreaming":streamingActive, "isBeta":isBeta, "hcRepairEnabled":hcRepairEnabled ]
				def oldProtData = atomicState?."oldProtData${devId}"
				def pDataChecksum = generateMD5_A(pData.toString())
				atomicState."oldProtData${devId}" = pDataChecksum
				pDataChecksum = atomicState."oldProtData${devId}"
				if(force || nforce || (oldProtData != pDataChecksum)) {
					physDevLblHandler("protect", devId, it?.label, "protects", pData?.data?.name.toString(), "prot", overRideNames)
					def t1 = it?.currentState("devVer")?.value?.toString()
					def sData = atomicState?.swVer ?: [:]
					sData["pDevVer"] = t1 ?: ""
					atomicState?.swVer = sData
					if(sData?.pDevVer != "" && (versionStr2Int(sData?.pDevVer) >= minVersions()?.protect?.val)) {
						//LogTrace("UpdateChildData >> Protect id: ${devId} | data: ${pData}")
						LogTrace("UpdateChildData >> Protect id: ${devId} | oldProtData: ${oldProtData} pDataChecksum: ${pDataChecksum} force: $force nforce: $nforce")
						if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
					} else {
						if(atomicState?."lastUpdated${devId}Dt" == null) {
							atomicState."lastUpdated${devId}Dt" = getDtNow()
						} else {
							LogAction("NST PROTECT DEVICE UPDATE REQUIRED: Protect ${devId} (v${sData?.pDevVer}) | REQUIRED: (v${minVersions()?.protect?.desc}) | Update the Device code to the latest software in the IDE", "error", true)
							appUpdateNotify()
						}
					}
					it?.generateEvent(pData)
				}
				return true
			}
			else if(devId && atomicState?.cameras && atomicState?.deviceData?.cameras && atomicState?.deviceData?.cameras[devId]) {
				//devCodeIds["camera"] = it?.getDevTypeId()
				List camMotionZones = (settings?.camEnMotionZoneFltr && settings?."camera_${devId}_zones"?.size()) ? settings?."camera_${devId}_zones" : []
				def camData = ["data":atomicState?.deviceData?.cameras[devId], "mt":useMt, "debug":dbg, "logPrefix":logNamePrefix, "camMotionZones": camMotionZones,
						"tz":nestTz, "apiIssues":api, "allowDbException":allowDbException, "latestVer":latestCamVer()?.ver?.toString(), "clientBl":clientBl,
						"hcTimeout":hcCamTimeout, "mobileClientType":mobClientType, "enRemDiagLogging":remDiag, "healthNotify":nPrefs?.dev?.devHealth,
						"streamNotify":nPrefs?.dev?.camera?.streamMsg, "devBannerData":devBannerData, "restStreaming":streamingActive, "motionSndChgWaitVal":motionSndChgWaitVal,
						"isBeta":isBeta, "camTakeSnapOnEvt": camTakeSnapOnEvt, "hcRepairEnabled":hcRepairEnabled, "secState":locSecurityState ]
				def oldCamData = atomicState?."oldCamData${devId}"
				def cDataChecksum = generateMD5_A(camData.toString())
				atomicState."oldCamData${devId}" = cDataChecksum
				cDataChecksum = atomicState."oldCamData${devId}"
				if(force || nforce || (oldCamData != cDataChecksum)) {
					physDevLblHandler("camera", devId, it?.label, "cameras", camData?.data?.name.toString(), "cam", overRideNames)
					def t1 = it?.currentState("devVer")?.value?.toString()
					def sData = atomicState?.swVer ?: [:]
					sData["camDevVer"] = t1 ?: ""
					atomicState?.swVer = sData
					if(sData?.camDevVer != "" && (versionStr2Int(sData?.camDevVer) >= minVersions()?.camera?.val)) {
						// LogTrace("UpdateChildData >> Camera id: ${devId} | data: ${camData}")
						LogTrace("UpdateChildData >> Camera id: ${devId} | oldCamData: ${oldCamData} cDataChecksum: ${cDataChecksum} force: $force nforce: $nforce")
						if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
					} else {
						if(atomicState?."lastUpdated${devId}Dt" == null) {
							atomicState."lastUpdated${devId}Dt" = getDtNow()
						} else {
							LogAction("NST CAMERA DEVICE UPDATE REQUIRED: Camera ${devId} (v${sData?.camDevVer}) | REQUIRED: (v${minVersions()?.camera?.desc}) | Update the Device code to the latest software in the IDE", "error", true)
							appUpdateNotify()
						}
					}
					it?.generateEvent(camData)
				}
				return true
			}
			else if(devId && atomicState?.presDevice && devId == getNestPresId()) {
				//devCodeIds["presence"] = it?.getDevTypeId()
				def pData = ["debug":dbg, "logPrefix":logNamePrefix, "tz":nestTz, "mt":useMt, "pres":locPresence, "apiIssues":api, "allowDbException":allowDbException,
							"latestVer":latestPresVer()?.ver?.toString(), "clientBl":clientBl, "hcTimeout":hcLongTimeout, "mobileClientType":mobClientType, "hcRepairEnabled":hcRepairEnabled,
							"enRemDiagLogging":remDiag, "healthNotify":nPrefs?.dev?.devHealth, "lastStrDataUpd": getTimestampVal("lastStrDataUpd"), "isBeta":isBeta ]
				def oldPresData = atomicState?."oldPresData${devId}"
				def pDataChecksum = generateMD5_A(pData.toString())
				atomicState."oldPresData${devId}" = pDataChecksum
				pDataChecksum = atomicState."oldPresData${devId}"
				if(force || nforce || (oldPresData != pDataChecksum)) {
					virtDevLblHandler(devId, it?.label, "pres", "pres", overRideNames)
					def t1 = it?.currentState("devVer")?.value?.toString()
					def sData = atomicState?.swVer ?: [:]
					sData["presDevVer"] = t1 ?: ""
					atomicState?.swVer = sData
					if(sData?.presDevVer != "" && (versionStr2Int(sData?.presDevVer) >= minVersions()?.presence?.val)) {
						LogTrace("UpdateChildData >> Presence id: ${devId} | oldPresData: ${oldPresData} pDataChecksum: ${pDataChecksum} force: $force nforce: $nforce")
						if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
					} else {
						if(atomicState?."lastUpdated${devId}Dt" == null) {
							atomicState."lastUpdated${devId}Dt" = getDtNow()
						} else {
							LogAction("NST PRESENCE DEVICE UPDATE REQUIRED: Presence ${devId} (v${sData?.presDevVer}) | REQUIRED: (v${minVersions()?.presence?.desc}) | Update the Device code to the latest software in the IDE", "error", true)
							appUpdateNotify()
						}
					}
					it?.generateEvent(pData)
				}
				return true
			}
			else if(devId && atomicState?.weatherDevice && devId == getNestWeatherId()) {
				//devCodeIds["weather"] = it?.getDevTypeId()
				def wData1 = ["weatCond":getWeatherData("curWeather"), "weatForecast":getWeatherData("curForecast"), /* "weatAstronomy":getWeatherData("curAstronomy"),*/ "weatAlerts":getWeatherData("curAlerts"), weatLocation:getWeatherData("curLocation")]
				def wData = ["data":wData1, "tz":nestTz, "mt":useMt, "debug":dbg, "logPrefix":logNamePrefix, "apiIssues":api,
							"allowDbException":allowDbException, "weathAlertNotif":settings?.weathAlertNotif, "latestVer":latestWeathVer()?.ver?.toString(),
							"clientBl":clientBl, "hcTimeout":hcLongTimeout, "mobileClientType":mobClientType, "enRemDiagLogging":remDiag, "hcRepairEnabled":hcRepairEnabled,
							"healthNotify":nPrefs?.dev?.devHealth, "showGraphs":showGraphs, "devBannerData":devBannerData, "isBeta":isBeta ]
				def oldWeatherData = atomicState?."oldWeatherData${devId}"
				def wDataChecksum = generateMD5_A(wData.toString())
				def showWGraphs = settings?.weatherShowGraph == false ? false : true
				atomicState."oldWeatherData${devId}" = wDataChecksum
				wDataChecksum = atomicState."oldWeatherData${devId}"
				if(force || nforce || (oldWeatherData != wDataChecksum)) {
					virtDevLblHandler(devId, it?.label, "weather", "weath", overRideNames)
					def t1 = it?.currentState("devVer")?.value?.toString()
					def sData = atomicState?.swVer ?: [:]
					sData["weatDevVer"] = t1 ?: ""
					atomicState?.swVer = sData
					if(sData?.weatDevVer != "" && (versionStr2Int(sData?.weatDevVer) >= minVersions()?.weather?.val)) {
						LogTrace("UpdateChildData >> Weather id: ${devId} oldWeatherData: ${oldWeatherData} wDataChecksum: ${wDataChecksum} force: $force nforce: $nforce")
						if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
					} else {
						if(atomicState?."lastUpdated${devId}Dt" == null) {
							atomicState."lastUpdated${devId}Dt" = getDtNow()
						} else {
							LogAction("NST WEATHER DEVICE UPDATE REQUIRED: Weather ${devId} (v${sData?.weatDevVer}) | REQUIRED: (v${minVersions()?.weather?.desc}) | Update the Device code to the latest software in the IDE", "error", true)
							appUpdateNotify()
						}
					}
					it?.generateEvent(wData)
				}
				return true
			}
			else if(devId && atomicState?.vThermostats && atomicState?."vThermostat${devId}") {
				def physdevId = atomicState?."vThermostatMirrorId${devId}"
				if(physdevId && atomicState?.thermostats && atomicState?.deviceData?.thermostats && atomicState?.deviceData?.thermostats[physdevId]) {
					def tmp_data = atomicState?.deviceData?.thermostats[physdevId]
					def data = tmp_data
					def defmin = fixTempSetting(atomicState?."${physdevId}_safety_temp_min" ?: null)
					def defmax = fixTempSetting(atomicState?."${physdevId}_safety_temp_max" ?: null)
					def safetyTemps = [ "min":defmin, "max":defmax ]
					def comfortDewpoint = fixTempSetting(settings?."${physdevId}_comfort_dewpoint_max" ?: null)
					if(comfortDewpoint == 0) {
						comfortDewpoint = fixTempSetting(settings?.locDesiredComfortDewpointMax ?: null)
					}
					def comfortHumidity = settings?."${physdevId}_comfort_humidity_max" ?: 80
					def automationChildApp = getChildApps().find{ it.id == atomicState?."vThermostatChildAppId${devId}" }
					if(automationChildApp != null && !automationChildApp.getIsAutomationDisabled()) {
						//data = new JsonSlurper().parseText(JsonOutput.toJson(tmp_data))  // This is a deep clone as object is same reference
						data = [:] + tmp_data
						def tempC = 0.0
						def tempF = 0.0
						if(getTemperatureScale() == "C") {
							tempC = automationChildApp.getRemoteSenTemp()
							tempF = (tempC * (9 / 5) + 32.0)
						} else {
							tempF = automationChildApp.getRemoteSenTemp()
							tempC = (tempF - 32.0) * (5 / 9) as Double
						}
						data?.ambient_temperature_c = tempC
						data?.ambient_temperature_f = tempF

						def ctempC = 0.0
						def ctempF = 0
						if(getTemperatureScale() == "C") {
							ctempC = automationChildApp.getRemSenCoolSetTemp()
							ctempF = ctempC != null ? (ctempC * (9 / 5) + 32.0) as Integer : null
						} else {
							ctempF = automationChildApp.getRemSenCoolSetTemp()
							ctempC = ctempF != null ? (ctempF - 32.0) * (5 / 9) as Double : null
						}

						def htempC = 0.0
						def htempF = 0
						if(getTemperatureScale() == "C") {
							htempC = automationChildApp.getRemSenHeatSetTemp()
							htempF = htempC != null ? (htempC * (9 / 5) + 32.0) as Integer : null
						} else {
							htempF = automationChildApp.getRemSenHeatSetTemp()
							htempC = htempF != null ? (htempF - 32.0) * (5 / 9) as Double : null
						}

						if(data?.hvac_mode.toString() == "heat-cool") {
							data?.target_temperature_high_f = ctempF
							data?.target_temperature_low_f = htempF
							data?.target_temperature_high_c = ctempC
							data?.target_temperature_low_c = htempC
						} else if(data?.hvac_mode.toString() == "cool") {
							data?.target_temperature_f = ctempF
							data?.target_temperature_c = ctempC
						} else if(data?.hvac_mode.toString() == "heat") {
							data?.target_temperature_f = htempF
							data?.target_temperature_c = htempC
						}
					}

					if(nforce) {
						atomicState?."oldvTstatSchedData${devId}" = null
					}
					def oldTstatSchedData = atomicState?."oldvTstatSchedData${devId}"
					if(oldTstatSchedData == null) {
						oldTstatSchedData = reqSchedInfoRprt(it, false) as Map
						atomicState?."oldvTstatSchedData${devId}" = oldTstatSchedData
					}
					def autoSchedData = oldTstatSchedData as Map
					def tData = ["data":data, "mt":useMt, "debug":dbg, "tz":nestTz, "apiIssues":api, "safetyTemps":safetyTemps, "comfortHumidity":comfortHumidity,
						"comfortDewpoint":comfortDewpoint, "pres":locPresence, "childWaitVal":getChildWaitVal().toInteger(), "allowDbException":allowDbException,
						"latestVer":latestvStatVer()?.ver?.toString(), "vReportPrefs":vRprtPrefs, "clientBl":clientBl, "curWeatherData":curWeatherData, "logPrefix":logNamePrefix, "hcTimeout":hcTstatTimeout,
						"mobileClientType":mobClientType, "enRemDiagLogging":remDiag, "autoSchedData":autoSchedData, "healthNotify":nPrefs?.dev?.devHealth, "showGraphs":showGraphs,
						"devBannerData":devBannerData, "restStreaming":streamingActive, "isBeta":isBeta, "hcRepairEnabled":hcRepairEnabled, "etaBegin":locEtaBegin ]

					def oldTstatData = atomicState?."oldvStatData${devId}"
					def tDataChecksum = generateMD5_A(tData.toString())
					atomicState."oldvStatData${devId}" = tDataChecksum
					tDataChecksum = atomicState."oldvStatData${devId}"
					if(force || nforce || (oldTstatData != tDataChecksum)) {
						physDevLblHandler("vthermostat", devId, it?.label, "vThermostats", tData?.data?.name.toString(), "vtstat", overRideNames)
						def t1 = it?.currentState("devVer")?.value?.toString()
						def sData = atomicState?.swVer ?: [:]
						sData["vtDevVer"] = t1 ?: ""
						atomicState?.swVer = sData
						if(sData?.vtDevVer != "" && (versionStr2Int(sData?.vtDevVer) >= minVersions()?.thermostat?.val)) {
							LogTrace("UpdateChildData >> vThermostat id: ${devId} | oldvStatData: ${oldvStatData} tDataChecksum: ${tDataChecksum} force: $force nforce: $nforce")
							if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
						} else {
							if(atomicState?."lastUpdated${devId}Dt" == null) {
								atomicState."lastUpdated${devId}Dt" = getDtNow()
							} else {
								LogAction("NST THERMOSTAT DEVICE UPDATE REQUIRED: Thermostat ${devId} (v${sData?.vtDevVer}) | REQUIRED: (v${minVersions()?.thermostat?.desc}) | Update the Device code to the latest software in the IDE", "error", true)
							}
						}
						it?.generateEvent(tData)
					}
					return true
				}
			}

			else if(devId && devId == getNestPresId()) {
				return true
			}
			else if(devId && devId == getNestWeatherId()) {
				return true
			}
			// NOTE: This causes NP exceptions depending if child has not finished being deleted or if items are removed from Nest
			// else if(!atomicState?.deviceData?.thermostats[devId] && !atomicState?.deviceData?.smoke_co_alarms[devId] && !atomicState?.deviceData?.cameras[devId]) {
			// 	LogAction("Device found ${devId} and connection removed", "warn", true)
			// 	return null
			// }

			else {
				LogAction("updateChildData() Device ${devId} unclaimed", "warn", true)
				return true
			}
		}
		//atomicState?.devCodeIdData = devCodeIds
	}
	catch (ex) {
		log.error "updateChildData Exception:", ex
		sendExceptionData(ex, "updateChildData")
		updTimestampMap("lastChildUpdDt", null)
		updTimestampMap("lastChildForceUpdDt", null)
		return
	}
	if(atomicState?.pollBlocked) { return }
	atomicState.forceChildUpd = false
	atomicState.needChildUpd = false
}

def setNeedChildUpdate() {
	atomicState.needChildUpd = true
}

def tUnitStr() {
	return "\u00b0${getTemperatureScale()}"
}

def setDeviceLabel(devId, labelStr) {
	def dev = getChildDevice(devId)
	if(labelStr) { dev.label = labelStr.toString() }
}

void physDevLblHandler(devType, devId, devLbl, devStateName, apiName, abrevStr, ovrRideNames) {
	def nameIsDefault = false
	def deflbl
	def deflblval
	atomicState?."${devStateName}"?.each { t ->
		if(t.key == devId) {
			deflblval = t.value
			deflbl = getDefaultLabel("${devType}", t.value)
		}
	}
	def curlbl = devLbl?.toString()
	if(deflbl && deflbl == curlbl) { nameIsDefault = true }
	def newlbl = "getNest${abrevStr.capitalize()}Label"(apiName, devId)
	//LogTrace("physDevLblHandler | deflbl: ${deflbl} | curlbl: ${curlbl} | newlbl: ${newlbl} | deflblval: ${deflblval} || devId: ${devId}")
	if(ovrRideNames || (nameIsDefault && curlbl != newlbl)) {		// label change from nest
		if(curlbl != newlbl) {
			LogAction("Changing name from ${curlbl} to ${newlbl}", "info", true)
			setDeviceLabel(devId, newlbl?.toString())
			curlbl = newlbl?.toString()
		}
		def t0 = atomicState?."${devStateName}"
		t0[devId] = apiName.toString()
		atomicState?."${devStateName}" = t0
	}
	if(atomicState?.custLabelUsed && settings?."${abrevStr}_${devId}_lbl" != curlbl) {
		settingUpdate("${abrevStr}_${devId}_lbl", curlbl?.toString())
	}
	if(!atomicState?.custLabelUsed && settings?."${abrevStr}_${devId}_lbl") { settingRemove("${abrevStr}_${devId}_lbl") }
	if(settings?."${abrevStr}_${deflblval}_lbl") { settingRemove("${abrevStr}_${deflblval}_lbl") } // clean up old stuff
}

void virtDevLblHandler(devId, devLbl, devMethAbrev, abrevStr, ovrRideNames) {
	def curlbl = devLbl?.toString()
	def newlbl = "getNest${devMethAbrev.capitalize()}Label"()
	//LogTrace("virtDevLblHandler | curlbl: ${curlbl} | newlbl: ${newlbl} || devId: ${devId}")
	if(ovrRideNames && curlbl != newlbl) {
		LogAction("Changing name from ${curlbl} to ${newlbl}", "info", true)
		setDeviceLabel(devId, newlbl?.toString())
		curlbl = newlbl?.toString()
	}
	if(atomicState?.custLabelUsed && settings?."${abrevStr}Dev_lbl" != curlbl) {
		settingUpdate("${abrevStr}Dev_lbl", curlbl?.toString())
	}
	if(!atomicState?.custLabelUsed && settings?."${abrevStr}Dev_lbl") { settingRemove("${abrevStr}Dev_lbl") }
}

def apiIssues() {
	def t0 = atomicState?.apiIssuesList ?: [false, false, false, false, false, false, false]
	atomicState?.apiIssuesList = t0
	def result = t0[5..-1].every { it == true } ? true : false
	def dt = getTimestampVal("apiIssueDt")
	if(result) {
		def str = dt ? "may still be occurring. Status will clear when last updates are good (Last Updates: ${t0}) | Issues began at ($dt) " : "Detected (${getDtNow()})"
		LogAction("Nest API Issues ${str}", "warn", true)
	}
	return result
}

def apiIssueType() {
	def res = "Good"
	//this looks at the last 3 items added and determines whether issue is sporadic or outage
	def t0 = []
	t0 = atomicState?.apiIssuesList ?: [false, false, false, false, false, false, false]
	atomicState?.apiIssuesList = t0
	def items = t0[3..-1].findAll { it == true }
	if(items?.size() >= 1 && items?.size() <= 2) { res = "Sporadic" }
	else if(items?.size() >= 3) { res = "Outage" }
	//log.debug "apiIssueType: $res"
	return res
}

def issueListSize() { return 7 }

def apiIssueEvent(issue, cmd = null) {
	def list = atomicState?.apiIssuesList ?: [false, false, false, false, false, false, false]
	def listSize = issueListSize()
	if(list?.size() < listSize) {
		list.push(issue)
	}
	else if(list?.size() > listSize) {
		def nSz = (list?.size()-listSize) + 1
		def nList = list?.drop(nSz)
		nList?.push(issue)
		list = nList
	}
	else if(list?.size() == listSize) {
		def nList = list?.drop(1)
		nList?.push(issue)
		list = nList
	}
	atomicState?.apiIssuesList = list
	if(issue) {
		if(!getTimestampVal("apiIssueDt")) {
			updTimestampMap("apiIssueDt", getDtNow())
		}
	} else {
		def result = list[3..-1].every { it == false }
		def rateLimit = (atomicState?.apiRateLimited) ? true : false
		if(rateLimit) {
			def t0 = atomicState?.apiCmdFailData?.dt ? GetTimeDiffSeconds(atomicState?.apiCmdFailData?.dt, null, "apiIssueEvent").toInteger() : 200
			if((t0 > 120 && result) || t0 > 300) {
				atomicState?.apiRateLimited = false
				rateLimit = false
				LogAction("Clearing rate Limit", "info", true)
			}
		}
	}
}

def ok2PollMetaData() {
/*
	if(!atomicState?.authToken) { return false }
	if(!atomicState.metaData) { return true }
	if(atomicState?.pollBlocked) { return false }
	if(atomicState?.needMetaPoll) { return true }
	def pollTime = !settings?.pollMetaValue ? (3600 * 4) : settings?.pollMetaValue.toInteger()
	def val = pollTime / 3
	if(val > 60) { val = 50 }
	return ( ((getLastMetaPollSec() + val) > pollTime) ? true : false )
*/
	def pollTime = !settings?.pollMetaValue ? (3600 * 4) : settings?.pollMetaValue.toInteger()
	return (pollOk("Meta", pollTime, "metaData")) ? true : false
}

def ok2PollDevice() {
/*
	if(!atomicState?.authToken) { return false }
	if(!atomicState?.deviceData) { return true }
	if(atomicState?.pollBlocked) { return false }
	if(atomicState?.needDevPoll) { return true }
	def pollTime = !settings?.pollValue ? 180 : settings?.pollValue.toInteger()
	def val = pollTime / 3
	val = Math.max(Math.min(val.toInteger(), 50),25)
	//if(val > 60) { val = 50 }
	return ( ((getLastDevPollSec() + val) > pollTime) ? true : false )
*/
	def pollTime = !settings?.pollValue ? 180 : settings?.pollValue.toInteger()
	return (pollOk("Dev", pollTime, "deviceData")) ? true : false
}

def ok2PollStruct() {
/*
	if(!atomicState?.authToken) { return false }
	if(!atomicState?.structData) { return true }
	if(atomicState?.pollBlocked) { return false }
	if(atomicState?.needStrPoll) { return true }
	def pollStrTime = !settings?.pollStrValue ? 180 : settings?.pollStrValue.toInteger()
	def val = pollStrTime / 3
	val = Math.max(Math.min(val.toInteger(), 50),25)
	//if(val > 60) { val = 50 }
	return ( ((getLastStrPollSec() + val) > pollStrTime) ? true : false )
*/
	def pollStrTime = !settings?.pollStrValue ? 180 : settings?.pollStrValue.toInteger()
	return (pollOk("Str", pollStrTime, "structData")) ? true : false
}

def pollOk(typ, pTime, stVar) {
	if(!atomicState?.authToken) { return false }
	if(!atomicState?."${stVar}") { return true }
	if(atomicState?.pollBlocked) { return false }
	if(atomicState?."need${typ}Poll") { return true }
	def pollTime = pTime as Integer
	def val = pollTime / 3
	val = Math.max(Math.min(val.toInteger(), 50),25)
	return ( (("getLast${typ}PollSec"() + val) > pollTime) ? true : false )
}

def isPollAllowed() {
	return (atomicState?.pollingOn && atomicState?.authToken &&
		!atomicState?.cltBlacklisted &&
		(atomicState?.thermostats || atomicState?.protects || atomicState?.weatherDevice || atomicState?.cameras)) ? true : false
}

def getLastMetaPollSec() { return getTimeSeconds("lastMetaDataUpd", 100000, "getLastMetaPollSec").toInteger() }
def getLastDevPollSec() { return getTimeSeconds("lastDevDataUpd", 840, "getLastDevPollSec").toInteger() }
def getLastStrPollSec() { return getTimeSeconds("lastStrDataUpd", 1000, "getLastStrPollSec").toInteger() }
def getLastForcedPollSec() { return getTimeSeconds("lastForcePoll", 1000, "getLastForcedPollSec").toInteger() }
def getLastChildUpdSec() { return getTimeSeconds("lastChildUpdDt", 100000, "getLastChildUpdSec").toInteger() }
def getLastChildForceUpdSec() { return getTimeSeconds("lastChildForceUpdDt", 100000, "getLastChildForceUpdSec").toInteger() }
def getLastHeardFromNestSec() { return getTimeSeconds("lastHeardFromNestDt", 100000, "getLastHeardFromNestSec").toInteger() }

/************************************************************************************************
|										Nest API Commands										|
*************************************************************************************************/

private cmdProcState(Boolean value) { atomicState?.cmdIsProc = value }
private cmdIsProc() { return (!atomicState?.cmdIsProc) ? false : true }
private getLastProcSeconds() { return getTimeSeconds("cmdLastProcDt", 0, "getLastProcSeconds") }

def apiVar() {
	def api = [
		rootTypes: [
			struct:"structures", cos:"devices/smoke_co_alarms", tstat:"devices/thermostats", cam:"devices/cameras", meta:"metadata"
		],
		cmdObjs: [
			targetF:"target_temperature_f", targetC:"target_temperature_c", targetLowF:"target_temperature_low_f", setLabel:"label",
			targetLowC:"target_temperature_low_c", targetHighF:"target_temperature_high_f", targetHighC:"target_temperature_high_c",
			fanActive:"fan_timer_active", fanTimer:"fan_timer_timeout", fanDuration:"fan_timer_duration", hvacMode:"hvac_mode",
			away:"away", streaming:"is_streaming", setTscale:"temperature_scale", eta:"eta"
		]
	]
	return api
}

// There are 3 different return values
def getPdevId(Boolean virt, devId) {
	def pChild
	if(virt && atomicState?.vThermostats && devId) {
		if(atomicState?."vThermostat${devId}") {
			def pdevId = atomicState?."vThermostatMirrorId${devId}"
			if(pdevId) { pChild = getChildDevice(pdevId) }
			if(pChild) { return pChild }
			else { return "00000" }
		}
	}
	return pChild
}

def setEtaState(child, etaData, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? child?.toString() : child?.device?.deviceNetworkId.toString()

	def str1 = "setEtaState | "
	def strAction = "BAD data"
	def strArgs = " ${virtual ? "Virtual " : ""}Thermostat (${child?.device?.displayName} - ${devId}) | Trip_Id: ${etaData?.trip_id} | Begin: ${etaData?.estimated_arrival_window_begin} | End: ${etaData?.estimated_arrival_window_end}"

	if(etaData?.trip_id && etaData?.estimated_arrival_window_begin && etaData?.estimated_arrival_window_end) {
		def etaObj = [ "trip_id":"${etaData.trip_id}", "estimated_arrival_window_begin":"${etaData.estimated_arrival_window_begin}", "estimated_arrival_window_end":"${etaData.estimated_arrival_window_end}" ]
		// "trip_id":"sample-trip-id","estimated_arrival_window_begin":"2014-10-31T22:42:00.000Z","estimated_arrival_window_end":"2014-10-31T23:59:59.000Z"
		// new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))

		strAction = "Setting Eta"
		def pChild = getPdevId(virtual.toBoolean(), devId)
		if(pChild == null) {
			LogAction(str1+strAction+strArgs, "debug", true)
			return sendNestApiCmd(atomicState?.structures, apiVar().rootTypes.struct, apiVar().cmdObjs.eta, etaObj, devId)
		} else {
			if(pChild != "00000") {
				LogAction(str1+strAction+strArgs, "debug", true)
				pChild.setNestEta(etaData?.trip_id, etaData?.estimated_arrival_window_begin, etaData.estimated_arrival_window_end) {
				}
				return
			} else {
				strAction = "CANNOT Set Eta"
			}
		}
	}
	LogAction(str1+strAction+strArgs, "warn", true)
}

def cancelEtaState(child, trip_id, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? child?.toString() : child?.device?.deviceNetworkId.toString()

	def str1 = "cancelEtaState | "
	def strAction = "BAD data"
	def strArgs = " ${virtual ? "Virtual " : ""}Thermostat (${child?.device?.displayName} - ${devId}) | Trip_Id: ${trip_id}"

	if(trip_id) {
		def etaObj = [ "trip_id":"${trip_id}", "estimated_arrival_window_begin":0, "estimated_arrival_window_end":0 ]
		// "trip_id":"sample-trip-id","estimated_arrival_window_begin":"2014-10-31T22:42:00.000Z","estimated_arrival_window_end":"2014-10-31T23:59:59.000Z"
		// new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))

		strAction = "Cancel Eta"
		def pChild = getPdevId(virtual.toBoolean(), devId)
		if(pChild == null) {
			LogAction(str1+strAction+strArgs, "debug", true)
			return sendNestApiCmd(atomicState?.structures, apiVar().rootTypes.struct, apiVar().cmdObjs.eta, etaObj, devId)
		} else {
			if(pChild != "00000") {
				LogAction(str1+strAction+strArgs, "debug", true)
				pChild.cancelNestEta(trip_id) {
				}
				return
			} else {
				strAction = "CANNOT Cancel Eta"
			}
		}
	}
	LogAction(str1+strAction+strArgs, "warn", true)
}

def setCamStreaming(child, streamOn) {
	def devId = !child?.device?.deviceNetworkId ? child?.toString() : child?.device?.deviceNetworkId.toString()
	def val = streamOn.toBoolean() ? true : false
	LogAction("setCamStreaming | Setting Camera (${child?.device?.displayName} - ${devId}) Streaming to (${val ? "On" : "Off"})", "debug", true)
	return sendNestApiCmd(devId, apiVar().rootTypes.cam, apiVar().cmdObjs.streaming, val, devId)
}

def setCamLabel(child, label) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def val = label
// This is not used anywhere. A command to set label is not available in the dth for a callback
	LogAction("setCamLabel | Setting Camera (${child?.device?.displayName} - ${devId}) Label to (${val})", "debug", true)
	return sendNestApiCmd(devId, apiVar().rootTypes.cam, apiVar().cmdObjs.setLabel, val, devId)
}

def setProtLabel(child, label) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def val = label
// This is not used anywhere. A command to set label is not available in the dth for a callback
	LogAction("setProtLabel | Setting Protect (${child?.device?.displayName} - ${devId}) Label to (${val})", "debug", true)
	return sendNestApiCmd(devId, apiVar().rootTypes.cos, apiVar().cmdObjs.setLabel, val, devId)
}

def setStructureAway(child, value, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def val = value?.toBoolean()

	def str1 = "setStructureAway | "
	def strAction = ""
	strAction = "Setting Nest Location:"
	def strArgs = " (${child?.device?.displayName} ${!devId ? "" : "-  ${devId}"} to (${val ? "Away" : "Home"})"

	def pChild = getPdevId(virtual.toBoolean(), devId)
	if(pChild == null) {
		LogAction(str1+strAction+strArgs, "debug", true)
		if(val) {
			def ret = sendNestApiCmd(atomicState?.structures, apiVar().rootTypes.struct, apiVar().cmdObjs.away, "away", devId)
			// Below is to ensure automations read updated value even if queued
			if(ret && atomicState?.structData && atomicState?.structures && atomicState?.structData[atomicState?.structures]?.away) {
				def t0 = atomicState?.structData
				t0[atomicState?.structures].away = "away"
				atomicState?.structData = t0
				// locationPresNotify(getLocationPresence())
			}
			return ret
		}
		else {
			def ret = sendNestApiCmd(atomicState?.structures, apiVar().rootTypes.struct, apiVar().cmdObjs.away, "home", devId)
			if(ret && atomicState?.structData && atomicState?.structures && atomicState?.structData[atomicState?.structures]?.away) {
				def t0 = atomicState?.structData
				t0[atomicState?.structures].away = "home"
				atomicState?.structData = t0
				// locationPresNotify(getLocationPresence())
			}
			return ret
		}
	} else {
		if(pChild != "00000") {
			LogAction(str1+strAction+strArgs, "debug", true)
			if(val) {
				pChild.away()
			} else {
				pChild.present()
			}
			return
		} else {
			strAction = "CANNOT Set Location"
		}
	}
	LogAction(str1+strAction+strArgs, "warn", true)
}

def setTstatTempScale(child, tScale, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def tempScale = tScale.toString()

// INCOMPLETE: This is not used anywhere. A command to set Temp Scale is not available in the dth for a callback

	LogAction("setTstatTempScale: INCOMPLETE Thermostat${!devId ? "" : " ${devId}"} tempScale: (${tempScale})", "debug", true)
	return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.setTscale, tempScale, devId)
}

def setTstatLabel(child, label, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def val = label

// INCOMPLETE: This is not used anywhere. A command to set label is not available in the dth for a callback

	LogAction("setTstatLabel: INCOMPLETE Thermostat${!devId ? "" : " ${devId}"} Label: (${val})", "debug", true)
	return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.setLabel, val, devId)
}

def setFanMode(child, fanOn, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def val = fanOn.toBoolean()

	def str1 = "setFanMode | "
	def strAction = ""
	strAction = "Setting"
	def strArgs = " ${virtual ? "Virtual " : ""}Thermostat (${child?.device?.displayName} - ${devId}) Fan Mode to (${val ? "On" : "Auto"})"

	def pChild = getPdevId(virtual.toBoolean(), devId)
	if(pChild == null) {
		LogAction(str1+strAction+strArgs, "debug", true)
		return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.fanActive, val, devId)
	} else {
		if(pChild != "00000") {
			LogAction(str1+strAction+strArgs, "debug", true)
			if(val) {
				pChild.fanOn()
			} else {
				pChild.fanAuto()
			}
			return
		} else {
			strAction = "CANNOT Set"
		}
	}
	LogAction(str1+strAction+strArgs, "warn", true)
}

def setHvacMode(child, mode, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()

	def str1 = "setHvacMode | "
	def strAction = ""
	strAction = "Setting"
	def strArgs = " ${virtual ? "Virtual " : ""}Thermostat (${child?.device?.displayName} - ${devId}) HVAC Mode to (${mode})"

	def pChild = getPdevId(virtual.toBoolean(), devId)
	if(pChild == null) {
		LogAction(str1+strAction+strArgs, "debug", true)
		return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.hvacMode, mode.toString(), devId)
	} else {
		if(pChild != "00000") {
			LogAction(str1+strAction+strArgs, "debug", true)
			switch (mode) {
				case "heat-cool":
					pChild.auto()
					break
				case "heat":
					pChild.heat()
					break
				case "cool":
					pChild.cool()
					break
				case "eco":
					pChild.eco()
					break
				case "off":
					pChild.off()
					break
				case "emergency heat":
					pChild.emergencyHeat()
					break
				default:
					LogAction("setHvacMode: Invalid Request: ${mode}", "warn", true)
					break
			}
			return
		} else {
			strAction = "CANNOT Set "
		}
	}

	LogAction(str1+strAction+strArgs, "warn", true)
}

def setTargetTemp(child, unit, temp, mode, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()

	def str1 = "setTargetTemp | "
	def strAction = ""
	strAction = "Setting"
	def strArgs = " ${virtual ? "Virtual " : ""}Thermostat (${child?.device?.displayName} - ${devId}) Target Temp to (${temp}${tUnitStr()})"

	def pChild = getPdevId(virtual.toBoolean(), devId)
	if(pChild == null) {
		LogAction(str1+strAction+strArgs, "debug", true)
		if(unit == "C") {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetC, temp, devId)
		}
		else {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetF, temp, devId)
		}
	} else {
		LogAction(str1+strAction+strArgs, "debug", true)
		def appId = atomicState?."vThermostatChildAppId${devId}"
		def automationChildApp
		if(appId) { automationChildApp = getChildApps().find{ it?.id == appId } }
		if(automationChildApp) {
			def res = automationChildApp.remSenTempUpdate(temp,mode)
			if(res) { return }
		}
		if(pChild != "00000") {
			if(mode == 'cool') {
				pChild.setCoolingSetpoint(temp)
			} else if(mode == 'heat') {
				pChild.setHeatingSetpoint(temp)
			} else { LogAction("setTargetTemp - UNKNOWN MODE (${mode}) child ${pChild}", "warn", true) }
			return
		} else {
			strAction = "CANNOT Set"
		}
	}

	LogAction(str1+strAction+strArgs, "warn", true)
}

def setTargetTempLow(child, unit, temp, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()

	def str1 = "setTargetTempLow | "
	def strAction = ""
	strAction = "Setting"
	def strArgs = " ${virtual ? "Virtual " : ""}Thermostat (${child?.device?.displayName} - ${devId}) Target Temp Low to (${temp}${tUnitStr()})"

	def pChild = getPdevId(virtual.toBoolean(), devId)
	if(pChild == null) {
		LogAction(str1+strAction+strArgs, "debug", true)
		if(unit == "C") {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetLowC, temp, devId)
		}
		else {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetLowF, temp, devId)
		}
	} else {
		LogAction(str1+strAction+strArgs, "debug", true)
		def appId = atomicState?."vThermostatChildAppId${devId}"
		def automationChildApp
		if(appId) { automationChildApp = getChildApps().find{ it?.id == appId } }

		if(automationChildApp) {
			def res = automationChildApp.remSenTempUpdate(temp,"heat")
			if(res) { return }
		}
		if(pChild != "00000") {
			pChild.setHeatingSetpoint(temp)
			return
		} else {
			strAction = "CANNOT Set"
		}
	}

	LogAction(str1+strAction+strArgs, "warn", true)
}

def setTargetTempHigh(child, unit, temp, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()

	def str1 = "setTargetTempHigh | "
	def strAction = ""
	strAction = "Setting"
	def strArgs = " ${virtual ? "Virtual " : ""}Thermostat (${child?.device?.displayName} - ${devId}) Target Temp High to (${temp}${tUnitStr()})"

	def pChild = getPdevId(virtual.toBoolean(), devId)
	if(pChild == null) {
		LogAction(str1+strAction+strArgs, "debug", true)
		if(unit == "C") {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetHighC, temp, devId)
		}
		else {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetHighF, temp, devId)
		}
	} else {
		LogAction(str1+strAction+strArgs, "debug", true)
		def appId = atomicState?."vThermostatChildAppId${devId}"
		def automationChildApp
		if(appId) { automationChildApp = getChildApps().find{ it?.id == appId } }

		if(automationChildApp) {
			def res = automationChildApp.remSenTempUpdate(temp,"cool")
			if(res) { return }
		}
		if(pChild != "00000") {
			pChild.setCoolingSetpoint(temp)
			return
		} else {
			strAction = "CANNOT Set"
		}
	}

	LogAction(str1+strAction+strArgs, "warn", true)
}

def sendNestApiCmd(cmdTypeId, cmdType, cmdObj, cmdObjVal, childId) {
	LogAction("sendNestApiCmd $cmdTypeId, $cmdType, $cmdObj, $cmdObjVal, $childId", "info", false)
	if(!atomicState?.authToken) {
		LogAction("sendNestApiCmd no auth token", "warn", true)
		return false
	}

	try {
		if(cmdTypeId) {
			def qnum = getQueueNumber(cmdTypeId)
			if(qnum == -1 ) { return false }

			atomicState?.pollBlocked = true
			atomicState?.pollBlockedReason = "Sending Cmd"
			def now = new Date()
			def cmdData = [cmdTypeId?.toString(), cmdType?.toString(), cmdObj?.toString(), cmdObjVal, now]

			def tempQueue = []
			def newCmd = []
			def replaced = false
			def skipped = false
			def schedQ = false

			if(!atomicState?."cmdQ${qnum}" ) { atomicState."cmdQ${qnum}" = [] }
			def cmdQueue = atomicState?."cmdQ${qnum}"
			cmdQueue.each { cmd ->
				if(newCmd != []) {
					tempQueue << newCmd
				}
				newCmd = [cmd[0], cmd[1], cmd[2], cmd[3], cmd[4]]
			}

			if(newCmd != []) {		// newCmd is last command in queue
				if(newCmd[1] == cmdType?.toString() && newCmd[2] == cmdObj?.toString() && newCmd[3] == cmdObjVal) {	// Exact same command; leave it and skip
					skipped = true
					tempQueue << newCmd
				} else if(newCmd[1] == cmdType?.toString() && newCmd[2] == cmdObj?.toString() &&
						newCmd[2] != apiVar().cmdObjs.away && newCmd[2] != apiVar().cmdObjs.fanActive && newCmd[2] != apiVar().cmdObjs.fanTimer && newCmd[2] != apiVar().cmdObjs.eta) {
					// if we are changing the same setting again use latest - this is Temp settings, hvac
					replaced = true
					tempQueue << cmdData
				} else {
					tempQueue << newCmd
					tempQueue << cmdData
				}
			} else {
				tempQueue << cmdData
			}
			atomicState."cmdQ${qnum}" = tempQueue

			def str = "Adding"
			if(replaced) { str = "Replacing" }
			if(skipped) { str = "Skipping" }

			if(replaced || skipped) {
				LogAction("Command Matches last in queue ${qnum} - ${str}", "warn", true)
			}

			LogAction("${str} Cmd to Queue ${qnum} (qsize: ${tempQueue?.size()}): $cmdTypeId, $cmdType, $cmdObj, $cmdObjVal, $childId", "info", true)
			atomicState?.lastQcmd = cmdData
			schedNextWorkQ()
			return true

		} else {
			LogAction("sendNestApiCmd null cmdTypeId $cmdTypeId, $cmdType, $cmdObj, $cmdObjVal, $childId", "warn", true)
			return false
		}
	}
	catch (ex) {
		log.error "sendNestApiCmd Exception:", ex
		sendExceptionData(ex, "sendNestApiCmd")
		return false
	}
}

/*
 * Each nest device has its own queue (as does the nest structure itself)
 *   Queues are "assigned" dynamically as they are needed
 * Each queue has it own "free" command counts, then commands are limited to 1 per minute.
 */

private getQueueNumber(cmdTypeId) {
	if(!atomicState?.cmdQlist) { atomicState.cmdQlist = [] }
	def cmdQueueList = atomicState?.cmdQlist
	def qnum = cmdQueueList.indexOf(cmdTypeId)
	if(qnum == -1) {
		cmdQueueList = atomicState?.cmdQlist
		cmdQueueList << cmdTypeId
		atomicState.cmdQlist = cmdQueueList
		qnum = cmdQueueList.indexOf(cmdTypeId)
		atomicState?."cmdQ${qnum}" = null
		setLastCmdSentSeconds(qnum, null)
		//setRecentSendCmd(qnum, null)
	}
	qnum = cmdQueueList.indexOf(cmdTypeId)
	if(qnum == -1 || qnum == null) { LogAction("getQueueNumber: NOT FOUND", "warn", true ) }
	else {
		if(getLastCmdSentSeconds(qnum) > 3600) { setRecentSendCmd(qnum, cmdMaxVal()) } // if nothing sent in last hour, reset command limit
	}
	return qnum
}

/*
 * Queues are processed in the order in which commands were sent (across all queues)
 * This maintains proper state ordering for changes, as commands can have dependencies in order
 */

def getQueueToWork() {
	def qnum
	def savedtim
	if(!atomicState?.cmdQlist) { atomicState.cmdQlist = [] }
	def cmdQueueList = atomicState?.cmdQlist
	cmdQueueList.eachWithIndex { val, idx ->
		def cmdQueue = atomicState?."cmdQ${idx}"
		if(cmdQueue?.size() > 0) {
			def cmdData = cmdQueue[0]
			def timVal = cmdData[4]
			if(savedtim == null || timVal < savedtim) {
				savedtim = timVal
				qnum = idx
			}
		}
	}
	LogTrace("getQueueToWork queue: ${qnum}")
	if(qnum != -1 && qnum != null) {
		if(getLastCmdSentSeconds(qnum) > 3600) { setRecentSendCmd(qnum, cmdMaxVal()) } // if nothing sent in last hour, reset command limit
	}
	return qnum
}

private cmdMaxVal() { return 2 }

void schedNextWorkQ(useShort=false) {
	def cmdDelay = getChildWaitVal()
	if(useShort) { cmdDelay = 0 }
	//
	// This is throttling the rate of commands to the Nest service for this access token.
	// If too many commands are sent Nest throttling could shut all write commands down for 1 hour to the device or structure
	// This allows up to 3 commands if none sent in the last hour, then only 1 per 60 seconds. Nest could still
	// throttle this if the battery state on device is low.
	// https://nestdevelopers.io/t/user-receiving-exceeded-rate-limit-on-requests-please-try-again-later/354
	//

	def qnum = getQueueToWork()
	def timeVal = cmdDelay
	def str = ""
	def queueItemsAvail
	def lastCommandSent
	if(qnum != null) {
		queueItemsAvail = getRecentSendCmd(qnum)
		lastCommandSent = getLastCmdSentSeconds(qnum)
		if( (queueItemsAvailable == 0 && lastCommandSent > 60) ) { queueItemsAvail = 1 }
		if( queueItemsAvail <= 0 || atomicState?.apiRateLimited) {
			timeVal = 60 + cmdDelay
			//atomicState?.workQrunInActive = false
		} else if(lastCommandSent < 60) {
			timeVal = (60 - lastCommandSent + cmdDelay)
			if(queueItemsAvail > 0) { timeVal = 0 }
		}
		str = timeVal > cmdDelay || atomicState?.apiRateLimited ? "*RATE LIMITING ON* " : ""
		//LogAction("schedNextWorkQ │ ${str}queue: ${qnum} │ schedTime: ${timeVal} │ recentSendCmd: ${queueItemsAvail} │ last seconds: ${lastCommandSent} │ cmdDelay: ${cmdDelay} | runInActive: ${atomicState?.workQrunInActive} | Api Limited: ${atomicState?.apiRateLimited}", "info", true)
	} else {
		timeVal = 0
	}
	def actStr = "ALREADY PENDING "
	if(cmdIsProc()) { actStr = "COMMAND RUNNING " }
	if(!atomicState?.workQrunInActive && !cmdIsProc() ) {
		atomicState?.workQrunInActive = true
		if(timeVal != 0) {
			actStr = "RUNIN "
			runIn(timeVal, "workQueue", [overwrite: true])
		} else {
			actStr = "DIRECT CALL "
			workQueue()
		}
	}
	LogAction("schedNextWorkQ ${actStr} │ ${str}queue: ${qnum} │ schedTime: ${timeVal} │ recentSendCmd: ${queueItemsAvail} │ last seconds: ${lastCommandSent} │ cmdDelay: ${cmdDelay} | runInActive: ${atomicState?.workQrunInActive} | command proc: ${cmdIsProc()} | Api Limited: ${atomicState?.apiRateLimited}", "info", true)
}

private getRecentSendCmd(qnum) {
	return atomicState?."recentSendCmd${qnum}"
}

private setRecentSendCmd(qnum, val) {
	if(qnum != null) {
		atomicState."recentSendCmd${qnum}" = val
	} else {
		LogAction("setRecentSendCmd qnum $qnum is null", "error", true)
	}
	return
}

def sendEcoActionDescToDevice(dev, desc) {
	if(dev && desc) {
		dev?.ecoDesc(desc)
	}
}

private getLastAnyCmdSentSeconds() { return getTimeSeconds("lastCmdSentDt", 3601, "getLastAnyCmdSentSeconds") }
private getLastCmdSentSeconds(qnum) { return getTimeSeconds("lastCmdSentDt${qnum}", 3601, "getLastCmdSentSeconds") }

private setLastCmdSentSeconds(qnum, val) {
	updTimestampMap("lastCmdSentDt${qnum}", val)
	if(val != null) { updTimestampMap("lastCmdSentDt", val) }
}

def storeLastCmdData(cmd, qnum) {
	if(cmd) {
		def newVal = ["qnum":qnum, "obj":cmd[2], "value":cmd[3], "date":getDtNow()]

		def list = atomicState?.cmdDetailHistory ?: []
		def listSize = 20
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
		if(list) { atomicState?.cmdDetailHistory = list }
	}
}

void workQueue() {
	LogTrace("workQueue")
	atomicState?.workQrunInActive = false
	//def cmdDelay = getChildWaitVal()
	if(!atomicState?.cmdQlist) { atomicState?.cmdQlist = [] }
	def cmdQueueList = atomicState?.cmdQlist

	def qnum = getQueueToWork()
	if(qnum == null) { qnum = 0 }
	if(!atomicState?."cmdQ${qnum}") { atomicState."cmdQ${qnum}" = [] }

	def metstr = "async"

	def cmdQueue = atomicState?."cmdQ${qnum}"
	try {
		if(cmdQueue?.size() > 0) {
			LogAction("workQueue │ Run Queue: ${qnum} | ($metstr)", "trace", true)
			runIn(90, "workQueue", [overwrite: true])  // lost schedule catchall
			if(!cmdIsProc()) {
				cmdProcState(true)
				atomicState?.pollBlocked = true
				atomicState?.pollBlockedReason = "Processing Queue"
				cmdQueue = atomicState?."cmdQ${qnum}"
				// log.trace "cmdQueue(workqueue): $cmdQueue"
				def cmd = cmdQueue?.remove(0)
				// log.trace "cmdQueue(workqueue-after): $cmdQueue"
				// log.debug "cmd: $cmd"
				atomicState?."cmdQ${qnum}" = cmdQueue
				def cmdres

				if(getLastCmdSentSeconds(qnum) > 3600) { setRecentSendCmd(qnum, cmdMaxVal()) } // if nothing sent in last hour, reset command limit

				storeLastCmdData(cmd, qnum)

				if(cmd[1] == "poll") {
					atomicState.needStrPoll = true
					atomicState.needDevPoll = true
					atomicState.forceChildUpd = true
					cmdres = true
				} else {
					cmdres = queueProcNestApiCmd(getNestApiUrl(), cmd[0], cmd[1], cmd[2], cmd[3], qnum, cmd)
					return
				}
				finishWorkQ(cmd, cmdres)
			} else { LogAction("workQueue: busy processing command", "warn", true) }
		} else { atomicState.pollBlocked = false; atomicState?.pollBlockedReason = null; cmdProcState(false); }
	}
	catch (ex) {
		log.error "workQueue Exception Error:", ex
		sendExceptionData(ex, "workQueue")
		cmdProcState(false)
		atomicState.needDevPoll = true
		atomicState.needStrPoll = true
		atomicState.forceChildUpd = true
		atomicState?.pollBlocked = false
		atomicState?.pollBlockedReason = null
		atomicState?.workQrunInActive = true
		runIn(60, "workQueue", [overwrite: true])
		runIn((60 + 4), "postCmd", [overwrite: true])
		return
	}
}

def finishWorkQ(cmd, result) {
	LogTrace("finishWorkQ cmd: $cmd result: $result")
	def cmdDelay = getChildWaitVal()

	if( !result ) {
		atomicState.forceChildUpd = true
		atomicState.pollBlocked = false
		atomicState?.pollBlockedReason = null
		runIn((cmdDelay * 3), "postCmd", [overwrite: true])
	}

	atomicState.needDevPoll = true
	if(cmd && cmd[1] == apiVar().rootTypes.struct.toString()) {
		atomicState.needStrPoll = true
		atomicState.forceChildUpd = true
	}

	updTimestampMap("cmdLastProcDt", getDtNow())
	cmdProcState(false)

	def qnum = getQueueToWork()
	if(qnum == null) { qnum = 0 }
	if(!atomicState?."cmdQ${qnum}") { atomicState."cmdQ${qnum}" = [] }

	def cmdQueue = atomicState?."cmdQ${qnum}"
	if(cmdQueue?.size() == 0) {
		atomicState.pollBlocked = false
		atomicState.pollBlockedReason = null
		atomicState.needChildUpd = true
		runIn(cmdDelay, "postCmd", [overwrite: true])
	}
	else { schedNextWorkQ(true) }

	if(cmdQueue?.size() > 10) {
		sendMsg("Warning", "There is now ${cmdQueue?.size()} events in the Command Queue. Something must be wrong", true)
		LogAction("${cmdQueue?.size()} events in the Command Queue", "warn", true)
	}
	return
}

def queueProcNestApiCmd(uri, typeId, type, obj, objVal, qnum, cmd, redir = false) {
	def myStr = "queueProcNestApiCmd"
	LogTrace("${myStr}: typeId: ${typeId}, type: ${type}, obj: ${obj}, objVal: ${objVal}, qnum: ${qnum}, isRedirUri: ${redir}")
	def result = false
	if(!atomicState?.authToken) { return result }

	try {
		def urlPath = "/${type}/${typeId}"
		def data = new JsonBuilder("${obj}":objVal)
		def params = [
			uri: uri,
			path: urlPath,
			requestContentType: "application/json",
			headers: ["Content-Type": "application/json", "Authorization": "Bearer ${atomicState?.authToken}"],
			body: data.toString()
		]
		LogTrace("${myStr} Url: $uri | params: ${params}")
		LogAction("Processing Queued Cmd: [ObjId: ${typeId} | ObjType: ${type} | ObjKey: ${obj} | ObjVal: ${objVal} | QueueNum: ${qnum} | Redirect: ${redir}]", "trace", true)
		atomicState?.lastCmdSent = "$type: (${obj}: ${objVal})"

		adjThrottle(qnum, redir, myStr)

		def asyncargs = [
			typeId: typeId,
			type: type,
			obj: obj,
			objVal: objVal,
			qnum: qnum,
			cmd: cmd ]

		asynchttp_v1.put(nestCmdResponse, params, asyncargs)

	} catch(ex) {
		log.error "${myStr} (command: $cmd) Exception:", ex
		sendExceptionData(ex, myStr)
	}
}

def adjThrottle(qnum, redir, callerStr) {
	if(!redir) {
		def t0 = getRecentSendCmd(qnum)
		def val = t0
		if(t0 > 0 /* && (getLastCmdSentSeconds(qnum) < 60) */ ) {
			val -= 1
		}
		def t1 = getLastCmdSentSeconds(qnum)
		if(t1 > 120 && t1 < 60*45 && val < (cmdMaxVal() - 1) ) {
			val += 1
		}
		if(t1 > 60*30 && t1 < 60*45 && val < cmdMaxVal() ) {
			val += 1
		}
		LogTrace("${callerStr} adjThrottle orig recentSendCmd: ${t0} | new: ${val} | last seconds: ${t1} queue: ${qnum}")
		setRecentSendCmd(qnum, val)
	}
	setLastCmdSentSeconds(qnum, getDtNow())
}

def nestCmdResponse(resp, data) {
	LogAction("nestCmdResponse(${data?.cmd})", "info", false)
	def typeId = data?.typeId
	def type = data?.type
	def obj = data?.obj
	def objVal = data?.objVal
	def qnum = data?.qnum
	def command = data?.cmd
	def result = false
	try {
		if(!command) { cmdProcState(false); return }

		if(resp?.status == 307) {
			//LogTrace("resp: ${resp.headers}")
			def newUrl = resp?.headers?.Location?.split("\\?")
			//LogTrace("NewUrl: ${newUrl[0]}")
			queueProcNestApiCmd(newUrl[0], typeId, type, obj, objVal, qnum, command, true)
			return
		}
		if(resp?.status == 200) {
			LogAction("nestCmdResponse | Processed Queue: ${qnum} | Obj: ($type{$obj:$objVal}) SUCCESSFULLY!", "info", true)
			apiIssueEvent(false)
			incrementCntByKey("apiCommandCnt")
			atomicState?.lastCmdSentStatus = "ok"
			//atomicState?.apiRateLimited = false
			//atomicState?.apiCmdFailData = null
			result = true
		}
/*
		if(resp?.status == 429) {
			// requeue command
			def newCmd = [command[0], command[1], command[2], command[3], command[4]]
			def tempQueue = []
			tempQueue << newCmd
			if(!atomicState?."cmdQ${qnum}" ) { atomicState."cmdQ${qnum}" = [] }
			def cmdQueue = atomicState?."cmdQ${qnum}"
			cmdQueue.each { cmd ->
				newCmd = [cmd[0], cmd[1], cmd[2], cmd[3], cmd[4]]
				tempQueue << newCmd
			}
			atomicState."cmdQ${qnum}" = tempQueue
		}
*/
		if(resp?.status != 200) {
			atomicState?.lastCmdSentStatus = "failed"
			if(resp?.hasError()) {
				apiRespHandler((resp?.getStatus() ?: null), (resp?.getErrorJson() ?: null), "nestCmdResponse", "nestCmdResponse ${qnum} ($type{$obj:$objVal})", true)
			}
			apiIssueEvent(true)
		}
/*
		if(resp?.status == 429) {
			result = true // we requeued the command
		}
*/
		finishWorkQ(command, result)

	} catch (ex) {
		atomicState?.lastCmdSentStatus = "failed"
		cmdProcState(false)
		if(resp?.hasError()) {
			apiRespHandler((resp?.getStatus() ?: null), (resp?.getErrorJson() ?: null), "nestCmdResponse", "nestCmdResponse ${qnum} ($type{$obj:$objVal})", true)
		}
		apiIssueEvent(true)
		log.error "nestCmdResponse (command: $command) Exception:", ex
		sendExceptionData(ex, "nestCmdResponse")
	}
}

def procNestApiCmd(uri, typeId, type, obj, objVal, qnum, origcmd, redir = false) {
	def myStr = "procNestApiCmd"
	LogTrace("${myStr}: typeId: ${typeId}, type: ${type}, obj: ${obj}, objVal: ${objVal}, qnum: ${qnum}, isRedirUri: ${redir}")
	def result = false
	if(!atomicState?.authToken) { return result }

	try {
		def urlPath = redir ? "" : "/${type}/${typeId}"
		def data = new JsonBuilder("${obj}":objVal)
		def params = [
			uri: uri,
			path: urlPath,
			contentType: "application/json",
			query: [ "auth": atomicState?.authToken ],
			body: data.toString()
		]
		LogAction("${myStr} Url: $uri | params: ${params}", "trace", true)
		atomicState?.lastCmdSent = "$type: (${obj}: ${objVal})"

		adjThrottle(qnum, redir, myStr)

		httpPutJson(params) { resp ->
			def rCode = resp?.status ?: null
			if(resp?.status == 307) {
				def newUrl = resp?.headers?.location?.split("\\?")
				LogTrace("NewUrl: ${newUrl[0]}")
				if( procNestApiCmd(newUrl[0], typeId, type, obj, objVal, qnum, origcmd, true) ) {
					result = true
				}
				return result
			}
			else if(resp?.status == 200) {
				LogAction("${myStr} Processed queue: ${qnum} ($type{$obj:$objVal}) SUCCESSFULLY!", "info", true)
				apiIssueEvent(false)
				incrementCntByKey("apiCommandCnt")
				atomicState?.lastCmdSentStatus = "ok"
				//atomicState?.apiRateLimited = false
				//atomicState?.apiCmdFailData = null
				result = true
				return result
			}
/*
			if(resp?.status == 429) {
				// requeue command
				def newCmd = [origcmd[0], origcmd[1], origcmd[2], origcmd[3], origcmd[4]]
				def tempQueue = []
				tempQueue << newCmd
				if(!atomicState?."cmdQ${qnum}" ) { atomicState."cmdQ${qnum}" = [] }
				def cmdQueue = atomicState?."cmdQ${qnum}"
				cmdQueue.each { cmd ->
					newCmd = [cmd[0], cmd[1], cmd[2], cmd[3], cmd[4]]
					tempQueue << newCmd
				}
				atomicState."cmdQ${qnum}" = tempQueue
			}
*/
			atomicState?.lastCmdSentStatus = "failed"
			result = false
			apiRespHandler(resp?.status, resp?.data, myStr, "${myStr} ${qnum} ($type{$obj:$objVal})", true)
			apiIssueEvent(true)
/*
			if(resp?.status == 429) {
				result = true // we requeued the command
			}
*/
		}
	} catch (ex) {
		atomicState?.lastCmdSentStatus = "failed"
		cmdProcState(false)
		if (ex instanceof groovyx.net.http.HttpResponseException && ex?.response) {
			apiRespHandler(ex?.response?.status, ex?.response?.data, "procNestApiCmd", "procNestApiCmd ${qnum} ($type{$obj:$objVal})", true)
		} else {
			sendExceptionData(ex, "procNestApiCmd")
		}
		apiIssueEvent(true)
		log.error "procNestApiCmd Exception: ($type | $obj:$objVal)", ex
	}
	return result
}

def apiRespHandler(code, errJson, methodName, tstr=null, isCmd=false) {
	LogAction("[$methodName] | Status: (${code}) | Error Message: ${errJson}", "warn", true)
	if (!(code?.toInteger() in [200, 307])) {
		def result = ""
		def notif = true
		def errMsg = errJson?.message != null ? errJson?.message : null
		switch(code) {
			case 400:
				result = !errMsg ? "A Bad Request was made to the API..." : errMsg
				break
			case 401:
				result = !errMsg ? "Authentication ERROR, Please try refreshing your login under Authentication settings..." : errMsg
				revokeNestToken()
				break
			case 403:
				result = !errMsg ? "Forbidden: Your Login Credentials are Invalid..." : errMsg
				revokeNestToken()
				break
			case 429:
				result = !errMsg ? "Requests are currently being blocked because of API Rate Limiting..." : errMsg
				atomicState?.apiRateLimited = true
				break
			case 500:
				result = !errMsg ? "Internal Nest Error:" : errMsg
				notif = false
				break
			case 503:
				result = !errMsg ? "There is currently a Nest Service Issue..." : errMsg
				notif = false
				break
			default:
				result = !errMsg ? "Received Response..." : errMsg
				notif = false
				break
		}
		def failData = ["code":code, "msg":result, "method":methodName, "dt":getDtNow(), isCmd: isCmd]
		atomicState?.apiCmdFailData = failData
		if(notif || isCmd) {
			failedCmdNotify(failData, tstr)
		}
		LogAction("$methodName error - (Status: $code - $result) - [ErrorLink: ${errJson?.type}]", "error", true)
	}
}

private incrementCntByKey(String key) {
	long evtCnt = atomicState?."${key}" ?: 0
	// evtCnt = evtCnt?.toLong()+1
	evtCnt++
	LogTrace("${key?.toString()?.capitalize()}: $evtCnt")
	atomicState?."${key}" = evtCnt?.toLong()
}

/************************************************************************************************
|								Push Notification Functions										|
*************************************************************************************************/
def pushStatus() { return (settings?.phone || settings?.usePush || settings?.pushoverEnabled) ? ((settings?.usePush || (settings?.pushoverEnabled && settings?.pushoverDevices)) ? "Push Enabled" : "Enabled") : null }
//def getLastMsgSec() { return !atomicState?.lastMsgDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastMsgDt, null, "getLastMsgSec").toInteger() }

def getLastUpdMsgSec() { return getTimeSeconds("lastUpdMsgDt", 100000, "getLastUpdMsgSec").toInteger() }
def getLastMissPollMsgSec() { return getTimeSeconds("lastMisPollMsgDt", 100000, "getLastMissPollMsgSec").toInteger() }
def getLastApiIssueMsgSec() { return getTimeSeconds("lastApiIssueMsgDt", 100000, "getLastApiIssueMsgSec").toInteger() }
def getLastLogRemindMsgSec() { return getTimeSeconds("lastLogRemindMsgDt", 100000, "getLastLogRemindMsgSec").toInteger() }
def getLastFailedCmdMsgSec() { return getTimeSeconds("lastFailedCmdMsgDt", 100000, "getLastFailedCmdMsgSec").toInteger() }
def getDebugLogsOnSec() { return getTimeSeconds("debugEnableDt", 0, "getDebugLogsOnSec").toInteger() }

//PushOver-Manager Input Generation Functions
private getPushoverSounds(){return (Map) atomicState?.pushoverManager?.sounds?:[:]}
private getPushoverDevices(){List opts=[];Map pmd=atomicState?.pushoverManager?:[:];pmd?.apps?.each{k,v->if(v&&v?.devices&&v?.appId){Map dm=[:];v?.devices?.sort{}?.each{i->dm["${i}_${v?.appId}"]=i};addInputGrp(opts,v?.appName,dm);}};return opts;}
private inputOptGrp(List groups,String title){def group=[values:[],order:groups?.size()];group?.title=title?:"";groups<<group;return groups;}
private addInputValues(List groups,String key,String value){def lg=groups[-1];lg["values"]<<[key:key,value:value,order:lg["values"]?.size()];return groups;}
private listToMap(List original){original.inject([:]){r,v->r[v]=v;return r;}}
private addInputGrp(List groups,String title,values){if(values instanceof List){values=listToMap(values)};values.inject(inputOptGrp(groups,title)){r,k,v->return addInputValues(r,k,v)};return groups;}
private addInputGrp(values){addInputGrp([],null,values)}
//PushOver-Manager Location Event Subscription Events, Polling, and Handlers
public pushover_init(){subscribe(location,"pushoverManager",pushover_handler);pushover_poll()}
public pushover_cleanup(){state?.remove("pushoverManager");unsubscribe("pushoverManager");}
public pushover_poll(){sendLocationEvent(name:"pushoverManagerCmd",value:"poll",data:[empty:true],isStateChange:true,descriptionText:"Sending Poll Event to Pushover-Manager")}
public pushover_msg(List devs,Map data){if(devs&&data){sendLocationEvent(name:"pushoverManagerMsg",value:"sendMsg",data:data,isStateChange:true,descriptionText:"Sending Message to Pushover Devices: ${devs}");}}
public pushover_handler(evt){Map pmd=atomicState?.pushoverManager?:[:];switch(evt?.value){case"refresh":def ed = evt?.jsonData;String id = ed?.appId;Map pA = pmd?.apps?.size() ? pmd?.apps : [:];if(id){pA[id]=pA?."${id}"instanceof Map?pA[id]:[:];pA[id]?.devices=ed?.devices?:[];pA[id]?.appName=ed?.appName;pA[id]?.appId=id;pmd?.apps = pA;};pmd?.sounds=ed?.sounds;break;case "reset":pmd=[:];break;};atomicState?.pushoverManager=pmd;}
//Builds Map Message object to send to Pushover Manager
private buildPushMessage(List devices,Map msgData,timeStamp=false){if(!devices||!msgData){return};Map data=[:];data?.appId=app?.getId();data.devices=devices;data?.msgData=msgData;if(timeStamp){data?.msgData?.timeStamp=new Date().getTime()};pushover_msg(devices,data);}

def notificationCheck() {
	LogTrace("notificationCheck")
	if(atomicState?.notificationPrefs == null) { atomicState?.notificationPrefs = buildNotifPrefMap() }
	def nPrefs = atomicState?.notificationPrefs
	if(!getOk2Notify()) { return }
	apiIssueNotify(nPrefs?.app?.api?.issueMsg, nPrefs?.app?.api?.rateLimitMsg, nPrefs?.app?.api?.issueMsgWait)
	missPollNotify(nPrefs?.app?.poll?.missPollMsg)
	loggingRemindNotify(nPrefs?.app?.remind?.logRemindMsg)
	if(!appDevType()) { appUpdateNotify() }
}

def cameraStreamNotify(child, Boolean streaming) {
	if(atomicState?.notificationPrefs == null) { atomicState?.notificationPrefs = buildNotifPrefMap() }
	if(streaming == null || atomicState?.notificationPrefs?.dev?.camera?.streamMsg != true) { return }
	sendMsg("${child?.device?.displayName} Info", "Streaming is now '${streaming ? "ON" : "OFF"}'", false)
}

def getLastDevHealthMsgSec(timeVal) { return !timeVal ? 100000 : GetTimeDiffSeconds(timeVal, null, "getLastDevHealthMsgSec").toInteger() }

def deviceHealthNotify(child, Boolean isHealthy) {
	// log.trace "deviceHealthNotify(${child?.device?.displayName}, $isHealthy)"
	if(atomicState?.notificationPrefs == null) { atomicState?.notificationPrefs = buildNotifPrefMap() }
	def nPrefs = atomicState?.notificationPrefs?.dev?.devHealth
	if(isHealthy == true || nPrefs?.healthMsg != true) {
		return
	}
	def devLbl = child?.device?.displayName
	def devId = !child?.device?.deviceNetworkId ? child?.toString() : child?.device?.deviceNetworkId.toString()
	def t0 = atomicState?.lastDevHealthMsgMap ?: [:]
	def lastTime = t0 && devId && t0?."${devId}" ? t0["${devId}"].dt : null
	if(!devId || getLastDevHealthMsgSec(lastTime) <= nPrefs?.healthMsgWait?.toInteger() ) {
		return
	}
	sendMsg("$devLbl Health Warning", "\nDevice is OFFLINE. Please check your logs for possible issues.")
	t0["${devId}"] = ["device":"$devLbl", "dt":getDtNow()]
	atomicState.lastDevHealthMsgMap = t0
}

def getNestZipCode() {
	def tt = getStrucVal("postal_code")
	return tt ?: ""
}

def getNestTimeZone() {
	return getStrucVal("time_zone")
}

def getEtaBegin() {
	return getStrucVal("eta_begin")
}

def getStrucVal(svariable) {
	def sData = atomicState?.structData
	def sKey = atomicState?.structures
	def asStruc = sData && sKey && sData[sKey] ? sData[sKey] : null
	def retVal = asStruc ? asStruc[svariable] ?: null : null
	return (retVal != null) ? retVal as String : null
}

def getSecurityState() {
	return getStrucVal("wwn_security_state")
}

def getLocationPresence() {
	return getStrucVal("away")
}

def locationPresNotify(pres) {
	log.trace "locationPresNotify($pres)"
	if(pres == null) { return }
	if(atomicState?.notificationPrefs == null) { atomicState?.notificationPrefs = buildNotifPrefMap() }
	if(atomicState?.notificationPrefs?.locationChg == true) {
		def lastStatus = atomicState?.curNestLocStatus
		if(lastStatus != null && lastStatus?.toString() != pres?.toString()) {
			sendMsg("${app?.label} Nest Location Info", "\nNest (${atomicState?.structName}) Location has been changed to [${pres.toString().capitalize()}]")
		}
	}
	atomicState?.curNestLocStatus = pres
}

def getApiIssueSec() { return getTimeSeconds("apiIssueDt", 100000, "getApiIssueSec").toInteger() }

def apiIssueNotify(msgOn, rateOn, wait) {
	if( (getApiIssueSec() > 600) && (getLastAnyCmdSentSeconds() > 600)) {
		updTimestampMap("apiIssueDt", null)
		atomicState.apiIssuesList = []
		if(atomicState?.apiRateLimited) {
			atomicState.apiRateLimited = false
			LogAction("Clearing rate Limit", "info", true)
		}
	}
	if(!msgOn || !wait || !(getLastApiIssueMsgSec() > wait.toInteger())) { return }
	def apiIssue = apiIssues() ? true : false
	def rateLimit = (rateOn && atomicState?.apiRateLimited) ? true : false
	if(apiIssue || rateLimit) {
		def msg = ""
		msg += apiIssue ? "\nThe Nest API appears to be having issues. This will effect the updating of device and location data.\nThe issues started at (${getTimestampVal("apiIssueDt")})" : ""
		msg += rateLimit ? "${apiIssue ? "\n\n" : "\n"}Your API connection is currently being Rate-limited for excessive commands." : ""
		if(sendMsg("${app?.label} API Issue Warning", msg, true)) {
			updTimestampMap("lastApiIssueMsgDt", getDtNow())
		}
	}
}

def failedCmdNotify(failData, tstr) {
	if(!(getLastFailedCmdMsgSec() > 300)) { return }
	if(atomicState?.notificationPrefs == null) { atomicState?.notificationPrefs = buildNotifPrefMap() }
	def nPrefs = atomicState?.notificationPrefs
	def cmdFail = (nPrefs?.app?.api?.cmdFailMsg && failData?.msg != null) ? true : false
	def cmdstr = tstr ?: atomicState?.lastCmdSent
	def msg = "\nThe (${cmdstr}) CMD sent to the API has failed.\nStatus Code: ${failData?.code}\nErrorMsg: ${failData?.msg}\nDT: ${failData?.dt}"
	if(cmdFail) {
		if(sendMsg("${app?.label} API CMD Failed", msg)) {
			updTimestampMap("lastFailedCmdMsgDt", getDtNow())
		}
	}
	LogAction(msg, (cmdFail ? "error" : "warn"), true)
}

def loggingRemindNotify(msgOn) {
	if(  !(settings?.appDebug || settings?.childDebug) || !msgOn || !(getLastLogRemindMsgSec() > 86400)) { return }
	if(getTimestampVal("debugEnableDt") == null) { updTimestampMap("debugEnableDt", getDtNow()) }
	def dbgAlert = (getDebugLogsOnSec() > 86400)
	if(dbgAlert) {
		def msg = "Your debug logging has remained enabled for more than 24 hours please disable them to reduce resource usage on ST platform."
		if(sendMsg(("${app?.label} Debug Logging Reminder"), msg, true)) {
			updTimestampMap("lastLogRemindMsgDt", getDtNow())
		}
	}
}

def missPollNotify(on) {
	def theWait = settings?.misPollNotifyWaitVal ?: 1800
	if(getLastDevPollSec() < theWait.toInteger()) {
		if(!getTimestampVal("lastDevDataUpd")) {
			def now = new Date()
			def val = new Date(now.time - ( (theWait.toInteger()+1) * 60 * 1000) ) // if uninitialized, set 31 mins in past
 			updTimestampMap("lastDevDataUpd", formatDt(val))
		}
		return
	} else {
		def msg = "\nThe app has not refreshed data in the last (${getLastDevPollSec()}) seconds.\nPlease try refreshing data using device refresh button."
		LogAction(msg, "error", true)
/* poll handles this
		if(settings?.restStreaming && atomicState?.restStreamingOn) {
			restStreamHandler(true)   // close the stream if we have not heard from it in a while
			//atomicState?.restStreamingOn = false
		}
*/
		if(atomicState?.notificationPrefs == null) { atomicState?.notificationPrefs = buildNotifPrefMap() }
		def msgWait = atomicState?.notificationPrefs?.msgDefaultWait ?: 3600
		if(on && getLastMissPollMsgSec() > msgWait.toInteger()) {
			if(sendMsg("${app.label} Nest Data update Issue", msg)) {
				updTimestampMap("lastMisPollMsgDt", getDtNow())
			}
		}
	}
}

def minVersionsOk() {
	def files = [tDevVer:"thermostat", weatDevVer:"weather", vtDevVer:"thermostat", presDevVer:"presence", camDevVer:"camera", pDevVer:"protect", autoSaVer:"automation"]
	def swData = atomicState?.swVer
	if(swData) {
		files?.each { fi->
			if(swData[fi?.key] && !(versionStr2Int(swData[fi?.key]) >= minVersions()[fi?.value]?.val)) { return false }
		}
	}
	return true
}

def appUpdateNotify(badFile=false, badType=null) {
	if(atomicState?.notificationPrefs == null) { atomicState?.notificationPrefs = buildNotifPrefMap() }
	def on = atomicState?.notificationPrefs?.app?.updates?.updMsg
	def wait = atomicState?.notificationPrefs?.app?.updates?.updMsgWait
	if(!badFile && (!on || !wait || !minVersionsOk())) { return }
	if(getLastUpdMsgSec() > wait.toInteger()) {
		def appUpd = isAppUpdateAvail() == true ? true : false
		def autoappUpd = isAutoAppUpdateAvail() == true ? true : false
		def protUpd = atomicState?.protects ? isProtUpdateAvail() : false
		def presUpd = atomicState?.presDevice ? isPresUpdateAvail() : false
		def tstatUpd = atomicState?.thermostats ? isTstatUpdateAvail() : false
		def weatherUpd = atomicState?.weatherDevice ? isWeatherUpdateAvail() : false
		def camUpd = atomicState?.cameras ? isCamUpdateAvail() : false
		def streamUpd = atomicState?.restStreamingOn ? isStreamUpdateAvail() : false
		def blackListed = (atomicState?.appData && !appDevType() && atomicState?.cltBlacklisted) ? true : false
		//log.debug "appUpd: $appUpd || protUpd: $protUpd || presUpd: $presUpd || tstatUpd: $tstatUpd || weatherUpd: $weatherUpd || camUpd: $camUpd || blackListed: $blackListed || badFile: $badFile"
		if(appUpd || autoappUpd || protUpd || presUpd || tstatUpd || weatherUpd || camUpd || streamUpd || blackListed || badFile) {
			def str = ""
			str += !blackListed ? "" : "\nBlack Listed, please ensure software is up to date then contact developer"
			if(badFile && badType) {
				str += !badType ? "" : "\nInvalid or Missing ${badType} Code, please Reinstall the correct ${badType} SmartApp Code"
			}
			str += !appUpd ? "" : "\nManager App: v${atomicState?.appData?.updater?.versions?.app?.ver?.toString()}${betaMarker() ? " Beta" : ""}"
			str += (!autoappUpd && !badFile) ? "" : "\nAutomation App: v${atomicState?.appData?.updater?.versions?.autoapp?.ver?.toString()}${betaMarker() ? " Beta" : ""}"
			str += !protUpd ? "" : "\nProtect: v${atomicState?.appData?.updater?.versions?.protect?.ver?.toString()}"
			str += !camUpd ? "" : "\nCamera: v${atomicState?.appData?.updater?.versions?.camera?.ver?.toString()}"
			str += !presUpd ? "" : "\nPresence: v${atomicState?.appData?.updater?.versions?.presence?.ver?.toString()}"
			str += !tstatUpd ? "" : "\nThermostat: v${atomicState?.appData?.updater?.versions?.thermostat?.ver?.toString()}"
			// str += !vtstatUpd ? "" : "\nVirtual Thermostat: v${atomicState?.appData?.updater?.versions?.thermostat?.ver?.toString()}"
			str += !weatherUpd ? "" : "\nWeather App: v${atomicState?.appData?.updater?.versions?.weather?.ver?.toString()}"
			str += !streamUpd ? "" : "\nStream Service: v${atomicState?.appData?.updater?.versions?.stream?.ver?.toString()}"
			def t0 = badFile ? "Warn" : "Info"
			if(sendMsg(t0, "${appName()} Update(s) are Available:${str} \n\nPlease visit the IDE to Update code", true)) {
				updTimestampMap("lastUpdMsgDt", getDtNow())
			}
		}
	}
}

def updateHandler() {
	LogTrace("updateHandler")
	if(atomicState?.isInstalled) {
		if(atomicState?.appData?.updater?.updateType.toString() == "critical" && atomicState?.lastCritUpdateInfo?.ver?.toInteger() != atomicState?.appData?.updater?.updateVer?.toInteger()) {
			sendMsg("Critical", "There are Critical Updates available for ${appName()}! Please visit the IDE and make sure to update the App and Devices Code")
			atomicState?.lastCritUpdateInfo = ["dt":getDtNow(), "ver":atomicState?.appData?.updater?.updateVer?.toInteger()]
		}
		def t0 = atomicState?.appData?.updater?.updateMsg
		// LogAction("updateHandler: t0 is ${t0}", "info", true)
		if(t0 != null && t0 != "" && t0 != atomicState?.lastUpdateMsg) {
			if(getLastUpdateMsgSec() > 86400) {
				if(sendMsg("Info", "${t0}")) {
					updTimestampMap("lastUpdateMsgDt", getDtNow())
					atomicState.lastUpdateMsg = t0
				}
			}
		}
	}
}

def getOk2Notify() { return (daysOk(settings?.quietDays) && notificationTimeOk() && modesOk(settings?.quietModes)) }

def sendMsg(String msgType, String msg, Boolean showEvt=true, Map pushoverMap=null, sms=null, push=null, brdcast=null) {
	//LogAction("sendMsg: msgType: ${msgType}, msg: ${msg}, showEvt: ${showEvt}", "warn", true)
	LogTrace("sendMsg")
	def sentstr = "Push"
	def sent = false
	try {
		def newMsg = "${msgType}: ${msg}" as String
		def flatMsg = newMsg.toString().replaceAll("\n", " ")
		if(!getOk2Notify()) {
			LogAction("sendMsg: Message Skipped During Quiet Time ($flatMsg)", "info", true)
			if(showEvt) { sendNotificationEvent(newMsg) }
		} else {
			if(!brdcast) {
				if(push || settings?.usePush) {
					sentstr = "Push Message"
					if(showEvt) {
						sendPush(newMsg)	// sends push and notification feed
					} else {
						sendPushMessage(newMsg)	// sends push
					}
					sent = true
				}
				if(settings?.pushoverEnabled && settings?.pushoverDevices) {
					sentstr = "Pushover Message"
					Map msgObj = [:]
					msgObj = pushoverMap ?: [title: msgType, message: msg, priority: (settings?.pushoverPriority?:0)]
					if(settings?.pushoverSound) { msgObj?.sound = settings?.pushoverSound }
					buildPushMessage(settings?.pushoverDevices, msgObj, true)
					sent = true
				}
				def thephone = sms ? sms.toString() : settings?.phone ? settings?.phone?.toString() : ""
				if(thephone) {
					sentstr = "Text Message to Phone [${thephone}]"
					def t0 = newMsg.take(140)
					if(showEvt) {
						sendSms(thephone as String, t0 as String)	// send SMS and notification feed
					} else {
						sendSmsMessage(thephone as String, t0 as String)	// send SMS
					}
					sent = true
				}
			} else {
				sentstr = "Broadcast Message"
				sendPush(newMsg) // sends push and notification feed was sendPushMessage(newMsg)  // push but no notification feed
				sent = true
			}
			if(sent) {
				//atomicState?.lastMsg = flatMsg
				//atomicState?.lastMsgDt = getDtNow()
				LogAction("sendMsg: Sent ${sentstr} (${flatMsg})", "debug", true)
				incrementCntByKey("appNotifSentCnt")
			}
		}
	} catch (ex) {
		log.error "sendMsg $sentstr Exception:", ex
		sendExceptionData(ex, "sendMsg")
	}
	return sent
}

def getLastWebUpdSec() { return getTimeSeconds("lastWebUpdDt", 100000, "getLastWebUpdSec").toInteger() }
def getLastWeatherUpdSec() { return getTimeSeconds("lastWeatherUpdDt", 100000, "getLastWeatherUpdSec").toInteger() }
def getLastForecastUpdSec() { return getTimeSeconds("lastForecastUpdDt", 100000, "getLastForecastUpdSec").toInteger() }
def getLastAnalyticUpdSec() { return getTimeSeconds("lastAnalyticUpdDt", 100000, "getLastAnalyticUpdSec").toInteger() }
def getLastUpdateMsgSec() { return getTimeSeconds("lastUpdateMsgDt", 100000, "getLastUpdateMsgSec").toInteger() }

def getStZipCode() { return location?.zipCode?.toString() }

def updateWebStuff(now=false) {
	//LogTrace("updateWebStuff $now")
	def nnow = now
	if(!atomicState?.appData) { nnow = true }
	LogTrace("updateWebStuff $now $nnow")
	if(nnow || (getLastWebUpdSec() > (3600*4))) {
		if(nnow) {
			getWebFileData()
		} else { getWebFileData(false) }
	}
	def wValue = Math.max( (settings?.pollWeatherValue ? settings?.pollWeatherValue.toInteger() : 900), 900)
	if(atomicState?.weatherDevice && getLastWeatherUpdSec() > wValue) {
		if(now) {
			getWeatherConditions(now)
		} else {
			runIn(20, "getWeatherConditions", [overwrite: true])
		}
	}
	if(atomicState?.isInstalled) {
		if(getLastAnalyticUpdSec() > (3600*24)) { runIn(105, "sendInstallData", [overwrite: true]) }
	}
	if(atomicState?.feedbackPending) { sendFeedbackData() }
}

def getWeatherConditions(force = false) {
	LogTrace("getWeatherConditions $force")
	if(atomicState?.weatherDevice) {
		def storageApp = getStorageApp()
		try {
			LogAction("Retrieving Local Weather Conditions", "info", false)
			def loc = ""
			def curWeather = ""
			def curForecast = ""
			def curLocation = ""
			def curAlerts = ""
			def curAlertdetail = ""
			def err = false
			def chgd = false
			def custLoc = getCustWeatherLoc().toString()

			if(custLoc) {
				loc = custLoc.toString()
				curWeather = getTwcConditions(/*loc*/)	//getWeatherFeature("conditions", loc)	// getTwcConditions(loc)
				curLocation = getTwcLocation(/*loc*/)
			} else {
				curWeather = getTwcConditions()		//getWeatherFeature("conditions")	//getTwcConditions()
				curLocation = getTwcLocation()
			}
			String myLoc = curLocation?.location?.latitude.toString() + "," + curLocation?.location?.longitude.toString()
			curAlerts = getTwcAlerts(myLoc)		// does not support loc    //getWeatherFeature("alerts", loc)	// getTwcAlerts(loc)
				//	atomicState?.curWeather = curWeather
				//	atomicState?.curLocation = curLocation
				//	atomicState?.curAlerts = curAlerts
			if( getLastForecastUpdSec() > (1800) ||
			    (storageApp && (!getStorageVal("curForecast") /* || !getStorageVal("curAstronomy") */ )) ||
			    (!storageApp && (!atomicState?.curForecast /* || !atomicState?.curAstronomy */ ))) {
				if(custLoc) {
					loc = custLoc.toString()
					curForecast = getTwcForecast(/*loc*/)			//getWeatherFeature("forecast", loc)	// getTwcForecast(loc)
					//curAstronomy = getWeatherFeature("astronomy", loc)	// getTwcForecast(loc)
				} else {
					curForecast = getTwcForecast()				//getWeatherFeature("forecast")		// getTwcForecast()
					//curAstronomy = getWeatherFeature("astronomy")		// getTwcForecast()
				}
				if(curForecast /*&& curAstronomy*/) {
					if(storageApp) {
						updStorageVal("curForecast", curForecast)
						//updStorageVal("curAstronomy", curAstronomy)
					} else {
						atomicState?.curForecast = curForecast
						//atomicState?.curAstronomy = curAstronomy
					}
					chgd = true
					updTimestampMap("lastForecastUpdDt", getDtNow())
				} else {
					LogAction("Could Not Retrieve Local Forecast or astronomy Conditions... This issue is likely caused by Weather Underground API issues...", "warn", true)
					atomicState.forceChildUpd = true
					err = true
				}
			}
			//if(curWeather && curAlerts) {
			if(curWeather) {
				if(storageApp) {
					updStorageVal("curWeather", curWeather)
					updStorageVal("curLocation", curLocation)
				} else {
					atomicState?.curWeather = curWeather
					atomicState?.curLocation = curLocation
				}
				chgd = true
/*
	Try to reduce size of alerts if they are big to save state space
*/
/*
				def alrt = curAlerts?.alerts
				def cntr = 0
				alrt.each { al ->
					if(al?.message) {
						curAlerts.alerts[cntr].message = al.message.take(800)
					}
					cntr++
				}
ERS todo				curAlertdetail = ""
				curAlertdetail = getTwcAlertDetail(alertId)
*/
				if(storageApp) {
					updStorageVal("curAlerts", curAlerts)
				} else {
					atomicState?.curAlerts = curAlerts
				}

				if(!err) { updTimestampMap("lastWeatherUpdDt", getDtNow()) }
			} else {
				LogAction("Could Not Retrieve Local Weather Conditions or alerts... This issue is likely caused by Weather Underground API issues...", "warn", true)
				atomicState.forceChildUpd = true
				return false
			}
			if(chgd) {
				atomicState.needChildUpd = true
				if(!force) { runIn(21, "finishPoll", [overwrite: true]) }
				return true
			}
		}
		catch (ex) {
			log.error "getWeatherConditions Exception:", ex
			sendExceptionData(ex, "getWeatherConditions")
			return false
		}
	} else { return false }
}

def getWeatherData(dataName) {
	def storageApp = getStorageApp(false)
	def stateSz = getStateSizePerc()
	def t1 = isAppLiteMode()
	if(storageApp && (stateSz < 33 || t1)) {
		initStorageApp() // should delete storageapp
		storageApp = null
	}
	//storageApp = getStorageApp()
	if(storageApp && !t1) {
		def t0 = findStateStorageVal(dataName)
		if(t0) {
			return t0
		} else { if(getWeatherConditions(true)) { return getStorageVal(dataName) } }
	} else {
		if(stateSz > 62) {
			LogAction("storageApp not found getWeatherData ${stateSz}%", "warn", true)
		}
		if(!t1) {
			if(stateSz > 62) {
				initStorageApp() // should create storage App
			}
		}
		if(atomicState?."$dataName") {
			return atomicState?."$dataName"
		} else { if(getWeatherConditions(true)) { return atomicState?."$dataName" }	}
	}
	return null
}

def getWLocation() {
	return getWeatherData("curLocation") // getTwcConditions()
}

def getWData() {
	return getWeatherData("curWeather") // getTwcConditions()
}

def getWForecastData() {
	return getWeatherData("curForecast") // getTwcForecast()
}

/*
def getWAstronomyData() {
	return getWeatherData("curAstronomy")
}
*/

def getWAlertsData() {
	return getWeatherData("curAlerts") // getTwcAlerts
}

def getWeatherDeviceInst() {
	return atomicState?.weatherDevice ? true : false
}

def getWebFileData(now = true) {
	LogTrace("getWebFileData $now")
	def params = [ uri: getAppSettingsUrl(), contentType: 'application/json' ]
	def result = false
	try {
		def allowAsync = false
		def metstr = "sync"
		if(!now) {
			allowAsync = true
			metstr = "async"
		}

		LogTrace("getWebFileData: Getting appConfig.json File(${metstr})")

		if(now || !allowAsync) {
			httpGet(params) { resp ->
				result = webResponse(resp, [type:metstr])
			}
		} else {
			asynchttp_v1.get(webResponse, params, [type:metstr])
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.HttpResponseException) {
			LogAction("appConfig.json file not found", "warn", true)
		} else {
			log.error "getWebFileData Exception:", ex
		}
		sendExceptionData(ex, "getWebFileData")
	}
	return result
}

def webResponse(resp, data) {
	LogTrace("webResponse(${data?.type})")
	def result = false
	if(resp?.status == 200) {
		def newdata = resp?.data
		if(data?.type == "async") { newdata = resp?.json }
		def t0 = atomicState?.appData
		def t1 = t0?.token
		newdata["token"] = t1
		//LogTrace("webResponse Resp: ${newdata}")
		LogTrace("webResponse appData: ${t0}")
		if(newdata && t0 != newdata) {
			LogAction("appConfig.json File HAS Changed", "info", true)
			atomicState?.appData = newdata
			clientBlacklisted()
			clientMetricBlacklisted()
			clientExceptionsBlacklisted()
			updateHandler()
			setStateVar(true)
		} else { LogAction("appConfig.json did not change", "info", false) }
		if(atomicState?.appData && !appDevType() && atomicState?.cltBlacklisted) {
			appUpdateNotify()
		}
		if(atomicState?.appData?.settings?.database?.pullSettingsFromFB == true) {
			getFbAppSettings(data?.type == "async" ? false : true )
		}
		updTimestampMap("lastWebUpdDt", getDtNow())
		result = true
	} else {
		LogAction("Get failed appConfig.json status: ${resp?.status}", "warn", true)
	}
	return result
}

def getFbAppSettings(now = true) {
	def params = [ uri: getAppSettingsFBUrl(), contentType: 'application/json' ]
	def result = false
	try {
		def allowAsync = false
		def metstr = "sync"
		if(!now) {
			allowAsync = true
			metstr = "async"
		}
		LogAction("Getting appSettings.json File(${metstr})", "info", false)

		if(now || !allowAsync) {
			httpGet(params) { resp ->
				result = webFbResponse(resp, [type:metstr])
			}
		} else {
			asynchttp_v1.get(webFbResponse, params, [type:metstr])
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.HttpResponseException) {
			 //log.warn  "clientData.json file not found..."
		} else {
			LogAction("getFbAppSettings Exception: ${ex}", "error", true)
		}
	}
	return result
}

def webFbResponse(resp, data) {
	LogAction("webFbResponse(${data?.type})", "info", false)
	def result = false
	if(resp?.status == 200) {
		def newdata = resp?.data
		if(data?.type == "async") { newdata = resp?.json }
		LogTrace("webFbResponse Resp: ${newdata}")
		LogAction("appSetttings.json File", "info", false)
		def adata = atomicState?.appData
		adata["token"] = newdata?.token
		atomicState?.appData = adata
		result = true
	} else {
		LogAction("Get failed appSettings.json status: ${resp?.status}", "warn", true)
	}
	return result
}

def getWebData(params, desc, text=true) {
	try {
		LogAction("getWebData: ${desc} data", "info", true)
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
			LogAction("${desc} file not found", "warn", true)
		} else {
			log.error "getWebData(params: $params, desc: $desc, text: $text) Exception:", ex
		}
		//sendExceptionData(ex, "getWebData")
		return "${label} info not found"
	}
}

private clientBlacklisted() {
	Boolean isBl = false
	List clList = atomicState?.appData?.blacklists?.clients ?: []
	if(atomicState?.isInstalled && atomicState?.installationId && clList?.size()) {
		isBl = (atomicState?.installationId in clList)
	}
	atomicState?.cltBlacklisted = isBl
}

private clientMetricBlacklisted() {
	Boolean isBl = false
	List clList = atomicState?.appData?.blacklists?.metrics ?: []
	if(atomicState?.isInstalled && clList?.size()) {
		isBl = (atomicState?.installationId in clList)
	}
	atomicState?.cltMetBlacklisted = isBl
}

private clientExceptionsBlacklisted() {
	Boolean isBl = false
	List clList = atomicState?.appData?.blacklists?.exceptions ?: []
	if(atomicState?.isInstalled && clList?.size()) {
		isBl = (atomicState?.installationId in clList)
	}
	atomicState?.cltExcBlacklisted = isBl
}

private broadcastCheck() {
	LogTrace("broadcastCheck")
	Map bCastData = atomicState?.appData?.broadcast
	if(atomicState?.isInstalled && bCastData) {
		if(bCastData?.msgId != "" && bCastData?.message != "" && atomicState?.lastBroadcastId != bCastData?.msgId && (bCastData?.minVer == "" || bCastData?.minVer != appVersion())) {
			if(sendMsg(strCapitalize(bCastData?.type), bCastData?.message.toString(), true, null, null, null, true)) {
				atomicState?.lastBroadcastId = bCastData?.msgId
			}
		}
		if(bCastData?.devBannerMsg != null && atomicState?.devBannerData?.msgId != bCastData?.devBannerMsg?.msgId) {
			if(bCastData?.devBannerMsg?.msgId && bCastData?.devBannerMsg?.message && bCastData?.devBannerMsg?.type && bCastData?.devBannerMsg?.expireDt && (bCastData?.devBannerMsg?.minVer == "" || bCastData?.devBannerMsg?.minVer != appVersion())) {
				def curDt = Date.parse("E MMM dd HH:mm:ss z yyyy", getDtNow())
				def expDt = Date.parse("E MMM dd HH:mm:ss z yyyy", bCastData?.devBannerMsg?.expireDt.toString())
				//log.debug "curDt: $curDt | expDt: $expDt | isExpired: ${(curDt > expDt)}"
				if(curDt && expDt && (curDt < expDt)) {
					atomicState?.devBannerData = bCastData?.devBannerMsg
				} else { atomicState?.devBannerData = null }
			} else { atomicState?.devBannerData = null }
		} else { atomicState?.devBannerData = null }
	}
}

def allowDbException() {
	if(atomicState?.appData?.settings?.database?.sendExceptions == null) { getWebFileData() }
	return (atomicState?.appData?.settings?.database?.sendExceptions == true)
}

def ver2IntArray(val) {
	def ver = val?.split("\\.")
	return [maj:"${ver[0]?.toInteger()}",min:"${ver[1]?.toInteger()}",rev:"${ver[2]?.toInteger()}"]
}

Integer versionStr2Int(str) { return str ? str.toString()?.replaceAll("\\.", "")?.toInteger() : null }

Integer getChildWaitVal() { return settings?.tempChgWaitVal ? settings?.tempChgWaitVal.toInteger() : 4 }

def getAskAlexaMQEn() {
	if(atomicState?.appData?.settings?.askAlexa?.enAaMsgQueue == true) {
		return settings?.allowAskAlexaMQ == null ? true : setting?.allowAskAlexaMQ
	} else { return false }
}

def getAskAlexaMultiQueueEn() {
	return atomicState?.appData?.settings?.askAlexa?.enMultiQueue == true ? true : false
}

def initAppMetricStore() {
	def items = ["mainLoadCnt", "devLocLoadCnt", "diagLoadCnt", "prefLoadCnt", "autoLoadCnt", "protTestLoadCnt", "helpLoadCnt", "infoLoadCnt", "chgLogLoadCnt", "nestLoginLoadCnt", "pollPrefLoadCnt", "devCustLoadCnt",
		"vRprtPrefLoadCnt", "notifPrefLoadCnt", "logPrefLoadCnt", "camZoneFltLoadCnt", "viewAutoSchedLoadCnt", "viewAutoStatLoadCnt", "autoGlobPrefLoadCnt", "devCustNameLoadCnt", "custWeathLoadCnt", "appNotifPrefLoadCnt"]
	def data = atomicState?.usageMetricsStore ?: [:]
	items?.each { if(!data[it]) { data[it] = 0 } }
	atomicState?.usageMetricsStore = data
}

def incMetricCntVal(item) {
	def data = atomicState?.usageMetricsStore ?: [:]
	data[item] = (data[item] == null) ? 1 : data[item].toInteger()+1
	atomicState?.usageMetricsStore = data
}

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
	//LogTrace("isCodeUpdateAvailable: type: $type | newVer: $newVer | curVer: $curVer | newestVersion: ${latestVer} | result: $result")
	return result
}

def isAppUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.app?.ver, appVersion(), "manager")) { return true }
	return false
}

def isAutoAppUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.autoapp?.ver, atomicState?.swVer?.autoSaVer, "automation")) { return true }
	if(atomicState?.swVer?.autoSaVer != "" && (versionStr2Int(atomicState?.swVer?.autoSaVer) > minVersions()?.automation?.val) && !getDevOpt()) { return true } // check if too high
	return false
}

def isPresUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.presence?.ver, atomicState?.swVer?.presDevVer, "presence")) { return true }
	return false
}

def isProtUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.protect?.ver, atomicState?.swVer?.pDevVer, "protect")) { return true }
	return false
}

def isCamUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.camera?.ver, atomicState?.swVer?.camDevVer, "camera")) { return true }
	return false
}

def isTstatUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.thermostat?.ver, atomicState?.swVer?.tDevVer, "thermostat")) { return true }
	return false
}

def isWeatherUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.weather?.ver, atomicState?.swVer?.weatDevVer, "weather")) { return true }
	return false
}

def isStreamUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.stream?.ver, atomicState?.swVer?.streamDevVer, "stream")) { return true }
	return false
}

def reqSchedInfoRprt(child, report=true) {
	LogTrace("reqSchedInfoRprt: (${child.device.label}, $report)")
	def result = null

	def tstat = getChildDevice(child.device.deviceNetworkId)
	if (tstat) {
		def str = ""
		def tstatAutoApp = getChildApps()?.find {
			(it?.getAutomationType() == "schMot" && it?.getTstatAutoDevId() == tstat?.deviceNetworkId)
		}
		//LogTrace("tstatAutoApp: ${tstatAutoApp}")
		def actSchedNum
		try {
			actSchedNum = tstatAutoApp ? tstatAutoApp?.getCurrentSchedule() : null
		} catch (ex) {
			log.error "BAD AUTOMATION FILE, FIX IN IDE; Likely put Nest Manager code in automation file", ex
		}

		if(actSchedNum) {
			def reqSenHeatSetPoint = tstatAutoApp?.getRemSenHeatSetTemp()
			def reqSenCoolSetPoint = tstatAutoApp?.getRemSenCoolSetTemp()
			def curZoneTemp = tstatAutoApp?.getRemoteSenTemp()
			def tempSrc = tstatAutoApp?.getRemSenTempSrc()

			def tempSrcStr = tempSrc
			def schedData = tstatAutoApp?.getSchedData(actSchedNum)
			def schedMotionActive = schedData?.m0 ? tstatAutoApp?.checkOnMotion(actSchedNum) : null
			tempSrcStr = (tempSrc == "Schedule") ? "Schedule ${actSchedNum} Sensor" : tempSrc

			if(!report) {
				def useMot = (schedMotionActive && (schedData?.mctemp || schedData?.mhtemp)) ? true : false
				tempSrcStr = useMot ? "Schedule Motion Trigger" : tempSrcStr

				return ["scdNum":actSchedNum, "schedName":schedData?.lbl, "reqSenHeatSetPoint":reqSenHeatSetPoint, "reqSenCoolSetPoint":reqSenCoolSetPoint, "curZoneTemp":curZoneTemp, "tempSrc":tempSrc, "tempSrcDesc":tempSrcStr]

			} else {
				def tempScaleStr = " degrees"
				def canHeat = tstat?.currentCanHeat?.toString() == "true" ? true : false
				def canCool = tstat?.currentCanCool?.toString() == "true" ? true : false
				def curMode = tstat?.currentnestThermostatMode?.toString()
				def curOper = tstat?.currentThermostatOperatingState?.toString()
				def curHum = tstat?.currentHumidity?.toString()
				def schedDesc = schedVoiceDesc(actSchedNum, schedData, schedMotionActive)
				str += schedDesc ?: " There are No Schedules currently Active. "

				if(getVoiceRprtPrefs()?.vRprtZone == true) {
					if(tempSrcStr && curZoneTemp) {
						def zTmp = curZoneTemp.toDouble()
						str += "The ${tempSrcStr} has an ambient temperature of "
						if(zTmp > adj_temp(90.0)) { str += "a scorching " }
						else if(zTmp > adj_temp(84.0) && zTmp <= adj_temp(90.0)) { str += "a uncomfortable " }
						else if(zTmp > adj_temp(78.0) && zTmp <= adj_temp(84.0)) { str += "a balmy " }
						else if(zTmp > adj_temp(74.0) && zTmp <= adj_temp(78.0)) { str += "a tolerable " }
						else if(zTmp >= adj_temp(68.0) && zTmp <= adj_temp(74.0)) { str += "a comfortable " }
						else if(zTmp >= adj_temp(64.0) && zTmp <= adj_temp(68.0)) { str += "a breezy " }
						else if(zTmp >= adj_temp(60.0) && zTmp < adj_temp(64.0)) { str += "a chilly " }
						else if(zTmp < adj_temp(60.0)) { str += "a freezing " }
						str += "${curZoneTemp}${tempScaleStr}"
						str += curHum ? " with a humidity of ${curHum}%. " : ". "
						if(zTmp < adj_temp(60.0)) { str += " (Please remember to dress warmly)." }
					}
				}

				if(curMode in ["eco"]) {
			//if in eco mode, should read temps from thermostat vs. the automation
					reqSenHeatSetPoint = tstat?.currentHeatingSetpoint
					reqSenCoolSetPoint = tstat?.currentCoolingSetpoint
				}
				str += " The HVAC is currently "
				str += curOper == "idle" ? " sitting idle " : " ${curOper} "
				str += " in ${curMode} mode"
				str += curMode in ["auto", "heat", "cool", "eco"] ? " with " : ". "
				str += canHeat && curMode in ["auto", "heat", "eco"] ? "the Heat set to ${reqSenHeatSetPoint}${tempScaleStr}" : ""
				str += canHeat && canCool && curMode == "auto" ? " and " : " "
				str += canCool && curMode in ["auto", "cool", "eco"] ? "the cool set to ${reqSenCoolSetPoint}${tempScaleStr}" : ""
				str += "."

				if (str != "") {
					LogAction("reqSchedInfoRprt: Sending voice report for [$str] to (${tstat})", "info", false)
					result = str
				}
			}
		} else {
			LogTrace("reqSchedInfoRprt: No Automation Schedules were found for ${tstat} device")
			if(report) {
				result = "No Thermostat Automation Schedules were found for ${tstat} device"
			}
			result = [:]
		}
	} else {
		LogAction("reqSchedInfoRprt: Thermostat device not found", "error", true)
		result = "Thermostat device not found"
	}
	return result
}

def getVoiceRprtCnt() {
	def cnt = 0
	def devs = app.getChildDevices(true)
	if(devs?.size() >= 1) {
		devs?.each { dev ->
			def rCnt = dev?.getDataByName("voiceRprtCnt")
			if(rCnt != null) {
				//log.debug "rCnt: ${rCnt}"
				cnt = cnt + rCnt.toInteger()
			}
		}
	}
	atomicState?.voiceRprtCnt = cnt
	return cnt
}

def schedVoiceDesc(num, data, motion) {
	def str = ""
	str += data?.lbl ? " The automation schedule slot ${num} labeled ${data?.lbl} is currently active. " : ""
	str += (!motion && (data?.ctemp || data?.htemp)) ? "The schedules desired temps" : ""
	str += (motion && (data?.mctemp || data?.mhtemp)) ? "The schedules desired motion triggered temps" : ""
	str += ((motion && data?.mhtemp) || (!motion && data?.htemp)) ? " are set to a heat temp of ${!motion ? fixTempSetting(data?.htemp) : fixTempSetting(data?.mhtemp)} degrees" : ""
	str += ((motion && data?.mctemp) || (!motion && data?.ctemp)) ? " and " : ". "
	str += ((motion && data?.mctemp) || (!motion && data?.ctemp)) ? " ${((!motion && !data?.htemp) || (motion && !data?.mhtemp)) ? "are" : ""} a cool temp of ${!motion ? fixTempSetting(data?.ctemp) : fixTempSetting(data?.mctemp)} degrees. " : ""
	return str != "" ? str : null
}

/************************************************************************************************
|	This Section Discovers all structures and devices on your Nest Account.			|
|	It also Adds Removes Devices from ST							|
*************************************************************************************************/

def getNestStructures(force = false) {
	LogTrace("Getting Nest Structures")
	def struct = [:]
	def thisstruct = [:]
	try {
		if(ok2PollStruct() || force) { getApiData("str") }
		if(atomicState?.structData) {
			def structs = atomicState?.structData
			structs?.eachWithIndex { struc, index ->
				def strucId = struc?.key
				def strucData = struc?.value

				def dni = [strucData?.structure_id].join('.')
				struct[dni] = strucData?.name.toString()

				if(strucData?.structure_id.toString() == settings?.structures.toString()) {
					thisstruct[dni] = strucData?.name.toString()
				} else {
					if(atomicState?.structures) {
						if(strucData?.structure_id?.toString() == atomicState?.structures?.toString()) {
							thisstruct[dni] = strucData?.name.toString()
						}
					} else {
						if(!settings?.structures) {
							thisstruct[dni] = strucData?.name.toString()
						}
					}
				}
			}
			if(atomicState?.thermostats || atomicState?.protects || atomicState?.cameras || atomicState?.vThermostats || atomicState?.presDevice || atomicState?.weatherDevice || isAutoAppInst() ) {  // if devices are configured, you cannot change the structure until they are removed
				struct = thisstruct
			}
			if(ok2PollDevice() || force) { getApiData("dev") }
		} else { LogAction("Missing: structData ${atomicState?.structData}", "warn", true) }

	} catch (ex) {
		log.error "getNestStructures Exception:", ex
		sendExceptionData(ex, "getNestStructures")
	}
	return struct
}

def getNestThermostats() {
	LogTrace("Getting Thermostat list")
	def stats = [:]
	def tstats = atomicState?.deviceData?.thermostats
	LogTrace("Found ${tstats?.size()} Thermostats")
	tstats.each { stat ->
		def statId = stat?.key
		def statData = stat?.value

		def adni = [statData?.device_id].join('.')
		if(statData?.structure_id == settings?.structures) {
			stats[adni] = getThermostatDisplayName(statData)
		}
	}
	return stats
}

def getNestProtects() {
	LogTrace("Getting Nest Protect List")
	def protects = [:]
	def nProtects = atomicState?.deviceData?.smoke_co_alarms
	LogTrace("Found ${nProtects?.size()} Nest Protects")
	nProtects.each { dev ->
		def devId = dev?.key
		def devData = dev?.value

		def bdni = [devData?.device_id].join('.')
		if(devData?.structure_id == settings?.structures) {
			protects[bdni] = getProtectDisplayName(devData)
		}
	}
	return protects
}

def getNestCameras() {
	LogTrace("Getting Nest Camera List")
	def cameras = [:]
	def nCameras = atomicState?.deviceData?.cameras
	LogTrace("Found ${nCameras?.size()} Nest Cameras")
	nCameras.each { dev ->
		def devId = dev?.key
		def devData = dev?.value

		def bdni = [devData?.device_id].join('.')
		if(devData?.structure_id == settings?.structures) {
			cameras[bdni] = getCameraDisplayName(devData)
		}
	}
	return cameras
}

def statState(val) {
	def stats = [:]
	def tstats = getNestThermostats()
	tstats.each { stat ->
		def statId = stat?.key
		def statData = stat?.value
		val.each { st ->
			if(statId == st) {
				def adni = [statId].join('.')
				stats[adni] = statData
			}
		}
	}
	return stats
}

def coState(val) {
	def protects = [:]
	def nProtects = getNestProtects()
	nProtects.each { dev ->
		val.each { pt ->
		if(dev?.key == pt) {
			def bdni = [dev?.key].join('.')
				protects[bdni] = dev?.value
			}
		}
	}
	return protects
}

def camState(val) {
	def cams = [:]
	def nCameras = getNestCameras()
	nCameras.each { dev ->
		val.each { cm ->
		if(dev?.key == cm) {
			def bdni = [dev?.key].join('.')
				cams[bdni] = dev?.value
			}
		}
	}
	return cams
}

def getThermostatDisplayName(stat) {
	if(stat?.name) { return stat.name.toString() }
	else if(stat?.name_long) { return stat?.name_long.toString() }
		else { return "Thermostatnamenotfound" }
}

def getProtectDisplayName(prot) {
	if(prot?.name) { return prot.name.toString() }
	else if(prot?.name_long) { return prot?.name_long.toString() }
		else { return "Protectnamenotfound" }
}

def getCameraDisplayName(cam) {
	if(cam?.name) { return cam.name.toString() }
	else if(cam?.name_long) { return cam?.name_long.toString() }
		else { return "Cameranamenotfound" }
}

def getNestDeviceDni(dni, type) {
	//LogTrace("getNestDeviceDni: $dni | $type")
	//LogAction("getNestDeviceDni: $dni ${dni.key} | $type", "trace", true)
	def retVal = ""
	def d1 = getChildDevice(dni?.key.toString())
	if(d1) { retVal = dni?.key.toString() }
	else {
		def t0 = "Nest${type}-${dni?.value.toString()}${appDevName()} | ${dni?.key.toString()}"
		d1 = getChildDevice(t0)
		if(d1) { retVal = t0.toString() }
		retVal = dni?.key.toString()
	}
	//LogAction("getNestDeviceDni ($type) Issue", "warn", true)
	//LogAction("getNestDeviceDni: retVal: $retVal", "trace", true)
	return retVal
}

def getNestTstatDni(dni) { return getNestDeviceDni(dni, "Thermostat") }

def getNestProtDni(dni) { return getNestDeviceDni(dni, "Protect") }

def getNestCamDni(dni) { return getNestDeviceDni(dni, "Cam") }

def getNestvStatDni(dni) { return getNestDeviceDni(dni, "vThermostat") }

def getNestPresId() {
	def dni = "Nest Presence Device" // old name 1
	def d3 = getChildDevice(dni)
	if(d3) { return dni }
	else {
		if(atomicState?.structures) {
			dni = "NestPres${atomicState.structures}" // old name 2
			d3 = getChildDevice(dni)
			if(d3) { return dni }
		}
		def retVal = ""
		def devt = appDevName()
		if(atomicState?.structures) { retVal = "NestPres${devt} | ${atomicState?.structures}" }
		else if(settings?.structures) { retVal = "NestPres${devt} | ${settings?.structures}" }
		else {
			LogAction("getNestPresID No structures ${atomicState?.structures}", "warn", true)
			return ""
		}
		return retVal
	}
}

def getNestWeatherId() {
	def dni = "Nest Weather Device (${location?.zipCode})"
	def d4 = getChildDevice(dni)
	if(d4) { return dni }
	else {
		if(atomicState?.structures) {
			dni = "NestWeather${atomicState.structures}"
			d4 = getChildDevice(dni)
			if(d4) { return dni }
		}
		def retVal = ""
		def devt = appDevName()
		if(atomicState?.structures) { retVal = "NestWeather${devt} | ${atomicState?.structures}" }
		else if(settings?.structures) { retVal = "NestWeather${devt} | ${settings?.structures}" }
		else {
			LogAction("getNestWeatherId No structures ${atomicState?.structures}", "warn", true)
			return ""
		}
		return retVal
	}
}

def getDefaultLabel(ttype, name) {
	//LogTrace("getDefaultLabel: ${ttype} ${name}")
	if(name == null || name == "") {
		LogAction("BAD CALL getDefaultLabel: ${ttype}, ${name}", "error", true)
		return ""
	}
	def devt = appDevName()
	def defName
	switch (ttype) {
		case "thermostat":
			defName = "Nest Thermostat${devt} - ${name}"
			if(atomicState?.devNameOverride && atomicState?.useAltNames) { defName = "${location.name}${devt} - ${name}" }
			break
		case "protect":
			defName = "Nest Protect${devt} - ${name}"
			if(atomicState?.devNameOverride && atomicState?.useAltNames) { defName = "${location.name}${devt} - ${name}" }
			break
		case "camera":
			defName = "Nest Camera${devt} - ${name}"
			if(atomicState?.devNameOverride && atomicState?.useAltNames) { defName = "${location.name}${devt} - ${name}" }
			break
		case "vthermostat":
			defName = "Nest vThermostat${devt} - ${name}"
			if(atomicState?.devNameOverride && atomicState?.useAltNames) { defName = "${location.name}${devt} - Virtual ${name}" }
			break
		case "presence":
			defName = "Nest Presence Device${devt}"
			if(atomicState?.devNameOverride && atomicState?.useAltNames) { defName = "${location.name}${devt} - Nest Presence Device" }
			break
		case "weather":
			def defZip = getStZipCode() ? getStZipCode() : getNestZipCode()
			def wLbl = getCustWeatherLoc() ? getCustWeatherLoc().toString() : "${defZip}"
			defName = "Nest Weather${devt} (${wLbl})"
			if(atomicState?.devNameOverride && atomicState?.useAltNames) { defName = "${location.name}${devt} - Nest Weather Device" }
			break
		default:
			LogAction("BAD CALL getDefaultLabel: ${ttype}, ${name}", "error", true)
			return ""
			break
	}
	return defName
}

def getNestTstatLabel(name, key) {
	//LogTrace("getNestTstatLabel: ${name}")
	def defName = getDefaultLabel("thermostat", name)
	if(atomicState?.devNameOverride && atomicState?.custLabelUsed) {
		return settings?."tstat_${key}_lbl" ?: defName
	}
	else { return defName }
}

def getNestProtLabel(name, key) {
	def defName = getDefaultLabel("protect", name)
	if(atomicState?.devNameOverride && atomicState?.custLabelUsed) {
		return settings?."prot_${key}_lbl" ?: defName
	}
	else { return defName }
}

def getNestCamLabel(name, key) {
	def defName = getDefaultLabel("camera", name)
	if(atomicState?.devNameOverride && atomicState?.custLabelUsed) {
		return settings?."cam_${key}_lbl" ?: defName
	}
	else { return defName }
}

def getNestVtstatLabel(name, key) {
	def defName = getDefaultLabel("vthermostat", name)
	if(atomicState?.devNameOverride && atomicState?.custLabelUsed) {
		return settings?."vtstat_${key}_lbl" ?: defName
	}
	else { return defName }
}

def getNestPresLabel() {
	def defName = getDefaultLabel("presence", "name")
	if(atomicState?.devNameOverride && atomicState?.custLabelUsed) {
		return settings?.presDev_lbl ? settings?.presDev_lbl.toString() : defName
	}
	else { return defName }
}

def getNestWeatherLabel() {
	def defName = getDefaultLabel("weather", "name")
	if(atomicState?.devNameOverride && atomicState?.custLabelUsed) {
		return settings?.weathDev_lbl ? settings?.weathDev_lbl.toString() : defName
	}
	else { return defName }
}

def getChildDeviceLabel(dni) {
	if(!dni) { return null }
	return getChildDevice(dni.toString())?.getLabel() ?: null
}

def getTstats() {
	return atomicState?.thermostats
}

def getCams() {
	return atomicState?.cameras
}

def getThermostatDevice(dni) {
	def d = getChildDevice(getNestTstatDni(dni))
	if(d) { return d }
	return null
}

def getCameraDevice(dni) {
	def d = getChildDevice(getNestCamDni(dni))
	if(d) { return d }
	return null
}

def getWeatherDevice() {
	def res = null
	def d = getChildDevice(getNestWeatherId())
	if(d) { return d }
	return res
}

def getLocHubId() {
	def hubs = location?.hubs*.id.findAll { it }
	return hubs[0] != null ? hubs[0].toString() : null
}

def getLocHubIp() {
	def hub = location?.hubs[0]
	return hub != null ? hub?.localIp.toString() : null
}

def getLocHub() {
	def hubs = location?.hubs*.id.findAll { it }
	return hubs[0] != null ? hubs[0] : null
}

def addRemoveDevices(uninst = null) {
	LogTrace("addRemoveDevices")
	def retVal = false
	if( /* atomicState?.resetAllData ||*/ settings?.resetAllData) {
		LogAction("addRemoveDevices: found fixState in progress", "debug", true)
		return true
	}
	try {
		def devsInUse = []
		def tstats
		def nProtects
		def nCameras
		def nVstats
		def devsCrt = 0
		def noCreates = true
		def noDeletes = true

		if(!uninst) {
			//LogAction("addRemoveDevices() Nest Thermostats ${atomicState?.thermostats}", "debug", false)
			if(atomicState?.thermostats) {
				tstats = atomicState?.thermostats.collect { dni ->
					def d1 = getChildDevice(getNestTstatDni(dni))
					if(!d1) {
						def d1Label = getNestTstatLabel("${dni?.value}", "${dni.key}")
						d1 = addChildDevice(app.namespace, getThermostatChildName(), dni?.key, null, [label: "${d1Label}"])
						//d1.take()
						devsCrt = devsCrt + 1
						LogAction("Created: ${d1?.displayName} with (Id: ${dni?.key})", "debug", true)
					} else {
						LogAction("Found: ${d1?.displayName} with (Id: ${dni?.key}) exists", "debug", true)
					}
					devsInUse += dni.key
					return d1
				}
			}
			//LogAction("addRemoveDevices Nest Protects ${atomicState?.protects}", "debug", false)
			if(atomicState?.protects) {
				nProtects = atomicState?.protects.collect { dni ->
					def d2 = getChildDevice(getNestProtDni(dni).toString())
					if(!d2) {
						def d2Label = getNestProtLabel("${dni.value}", "${dni.key}")
						d2 = addChildDevice(app.namespace, getProtectChildName(), dni.key, null, [label: "${d2Label}"])
						//d2.take()
						devsCrt = devsCrt + 1
						LogAction("Created: ${d2?.displayName} with (Id: ${dni?.key})", "debug", true)
					} else {
						LogAction("Found: ${d2?.displayName} with (Id: ${dni?.key}) exists", "debug", true)
					}
					devsInUse += dni.key
					return d2
				}
			}

			if(atomicState?.presDevice) {
				try {
					def dni = getNestPresId()
					def d3 = getChildDevice(dni)
					if(!d3) {
						def d3Label = getNestPresLabel()
						d3 = addChildDevice(app.namespace, getPresenceChildName(), dni, null, [label: "${d3Label}"])
						//d3.take()
						devsCrt = devsCrt + 1
						LogAction("Created: ${d3.displayName} with (Id: ${dni})", "debug", true)
					} else {
						LogAction("Found: ${d3.displayName} with (Id: ${dni}) exists", "debug", true)
					}
					devsInUse += dni
				} catch (ex) {
					LogAction("Nest Presence Device Handler may not be installed/published", "warn", true)
					noCreates = false
				}
			}

			if(atomicState?.weatherDevice) {
				try {
					def dni = getNestWeatherId()
					def d4 = getChildDevice(dni)
					if(!d4) {
						def d4Label = getNestWeatherLabel()
						d4 = addChildDevice(app.namespace, getWeatherChildName(), dni, null, [label: "${d4Label}"])
						//d4.take()
						updTimestampMap("lastWeatherUpdDt", null)
						updTimestampMap("lastForecastUpdDt", null)
						devsCrt = devsCrt + 1
						LogAction("Created: ${d4.displayName} with (Id: ${dni})", "debug", true)
					} else {
						LogAction("Found: ${d4.displayName} with (Id: ${dni}) exists", "debug", true)
					}
					devsInUse += dni
				} catch (ex) {
					LogAction("Nest Weather Device Handler may not be installed/published", "warn", true)
					noCreates = false
				}
			}
			if(atomicState?.cameras) {
				nCameras = atomicState?.cameras.collect { dni ->
					def d5 = getChildDevice(getNestCamDni(dni).toString())
					if(!d5) {
						def d5Label = getNestCamLabel("${dni.value}", "${dni.key}")
						d5 = addChildDevice(app.namespace, getCameraChildName(), dni.key, null, [label: "${d5Label}"])
						//d5.take()
						devsCrt = devsCrt + 1
						LogAction("Created: ${d5?.displayName} with (Id: ${dni?.key})", "debug", true)
					} else {
						LogAction("Found: ${d5?.displayName} with (Id: ${dni?.key}) exists", "debug", true)
					}
					devsInUse += dni.key
					return d5
				}
			}
			if(atomicState?.vThermostats) {
				nVstats = atomicState?.vThermostats.collect { dni ->
					LogAction("atomicState.vThermostats: ${atomicState.vThermostats} dni: ${dni} dni.key: ${dni.key.toString()} dni.value: ${dni.value.toString()}", "debug", true)
					def d6 = getChildDevice(getNestvStatDni(dni).toString())
					if(!d6) {
						def d6Label = getNestVtstatLabel("${dni.value}", "${dni.key}")
						LogAction("CREATED: ${d6Label} with (Id: ${dni.key})", "debug", true)
						d6 = addChildDevice(app.namespace, getThermostatChildName(), dni.key, null, [label: "${d6Label}", "data":["isVirtual":"true"]])
						//d6.take()
						devsCrt = devsCrt + 1
						LogAction("Created: ${d6?.displayName} with (Id: ${dni?.key})", "debug", true)
					} else {
						LogAction("Found: ${d6?.displayName} with (Id: ${dni?.key}) exists", "debug", true)
					}
					devsInUse += dni.key
					return d6
				}
			}

			def presCnt = 0
			def weathCnt = 0
			if(atomicState?.presDevice) { presCnt = 1 }
			if(atomicState?.weatherDevice) { weathCnt = 1 }
			if(devsCrt > 0) {
				noCreates = false
				LogAction("Created Devices; Current Devices: (${tstats?.size()}) Thermostat(s), (${nVstats?.size() ?: 0}) Virtual Thermostat(s), (${nProtects?.size() ?: 0}) Protect(s), (${nCameras?.size() ?: 0}) Cameras(s), ${presCnt} Presence Device and ${weathCnt} Weather Device", "debug", true)
				updTimestampMap("lastAnalyticUpdDt", null)
			}
		}

		if(uninst) {
			atomicState.thermostats = []
			atomicState.vThermostats = []
			atomicState.protects = []
			atomicState.cameras = []
			atomicState.presDevice = false
			atomicState.weatherDevice = false
		}

		if(!atomicState?.weatherDevice) {
			runIn(5, "cleanStorage", [overwrite: true]) // calling the child truncates logs
			atomicState?.curWeather = null
			atomicState?.curForecast = null
			//atomicState?.curAstronomy = null
			atomicState?.curAlerts = null
		}

		def noDeleteErr = true
		def delete
		LogTrace("addRemoveDevices devicesInUse: ${devsInUse}")
		delete = app.getChildDevices(true).findAll { !devsInUse?.toString()?.contains(it?.deviceNetworkId) }

		if(delete?.size() > 0) {
			noDeletes = false
			noDeleteErr = false
			updTimestampMap("lastAnalyticUpdDt", null)
			LogAction("Removing ${delete.size()} devices: ${delete}", "debug", true)
			delete.each { deleteChildDevice(it.deviceNetworkId, true) }
			noDeleteErr = true
		}
		retVal = ((uninst && noDeleteErr) || (!uninst && (noCreates && noDeletes))) ? true : false // it worked = no delete errors on uninstall; or no creates or deletes done
		//retVal = true
		//currentDevMap(true)
	} catch (ex) {
		if(ex instanceof physicalgraph.exception.ConflictException) {
			def msg = "Error: Can't Remove Device. One or more of them are still in use by other SmartApps or Routines. Please remove them and try again!"
			sendPush(msg)
			LogAction("addRemoveDevices Exception | $msg", "warn", true)
		}
		else if(ex instanceof physicalgraph.app.exception.UnknownDeviceTypeException) {
			def msg = "Error: Device Handlers are Missing or Not Published. Please verify all device handlers are present before continuing."
			appUpdateNotify()
			sendPush(msg)
			LogAction("addRemoveDevices Exception | $msg", "warn", true)
		}
		else {
			log.error "addRemoveDevices Exception:", ex
			sendExceptionData(ex, "addRemoveDevices")
		}
		retVal = false
	}
	return retVal
}

def addRemoveVthermostat(tstatdni, tval, myID) {
	def odevId = tstatdni
	LogAction("addRemoveVthermostat() tstat: ${tstatdni}  devid: ${odevId}  tval: ${tval}  myID: ${myID} vThermostats: ${atomicState?.vThermostats} ", "trace", true)

	if(parent || !myID || tval == null) {
		LogAction("got called BADLY ${parent}  ${myID}  ${tval}", "warn", true)
		return false
	}
	def tstat = tstatdni
	def tStatPhys

	def d1 = getChildDevice(odevId.toString())
	if(!d1) {
		LogAction("addRemoveVthermostat: Cannot find thermostat device child", "error", true)
		if(tval) { return false }  // if deleting (false), let it try to proceed
	} else {
		tstat = d1
		tStatPhys = tstat?.currentNestType == "physical" ? true : false
		if(!tStatPhys && tval) { LogAction("addRemoveVthermostat: Cannot create a virtual thermostat on a virtual thermostat device child", "error", true) }
	}

	def devId = "v${odevId}"

	// def migrate = migrationInProgress()

	// if(!migrate && atomicState?."vThermostat${devId}" && myID != atomicState?."vThermostatChildAppId${devId}") {
	if(atomicState?."vThermostat${devId}" && myID != atomicState?."vThermostatChildAppId${devId}") {
		LogAction("addRemoveVthermostat() not ours ${myID} ${atomicState?."vThermostat${devId}"} ${atomicState?."vThermostatChildAppId${devId}"}", "trace", true)
		//atomicState?."vThermostat${devId}" = false
		//atomicState?."vThermostatChildAppId${devId}" = null
		//atomicState?."vThermostatMirrorId${devId}" = null
		//atomicState?.vThermostats = null
		return false

	} else if(tval && atomicState?."vThermostat${devId}" && myID == atomicState?."vThermostatChildAppId${devId}") {
		LogAction("addRemoveVthermostat() already created ${myID} ${atomicState?."vThermostat${devId}"} ${atomicState?."vThermostatChildAppId${devId}"}", "trace", true)
		return true

	} else if(!tval && !atomicState?."vThermostat${devId}") {
		LogAction("addRemoveVthermostat() already removed ${myID} ${atomicState?."vThermostat${devId}"} ${atomicState?."vThermostatChildAppId${devId}"}", "trace", true)
		return true

	} else {
		atomicState."vThermostat${devId}" = tval
		if(tval && !atomicState?."vThermostatChildAppId${devId}") {
			LogAction("addRemoveVthermostat() marking for create virtual thermostat tracking ${tstat}", "trace", true)
			atomicState."vThermostatChildAppId${devId}" = myID
			atomicState?."vThermostatMirrorId${devId}" = odevId
			def vtlist = atomicState?.vThermostats ?: [:]
			vtlist[devId] = "${tstat.label.toString()}"
			atomicState.vThermostats = vtlist
			if(!settings?.resetAllData) { runIn(120, "updated", [overwrite: true]) }  // create what is needed

		} else if(!tval && atomicState?."vThermostatChildAppId${devId}") {
			LogAction("addRemoveVthermostat() marking for remove virtual thermostat tracking ${tstat}", "trace", true)
			atomicState."vThermostatChildAppId${devId}" = null
			atomicState?."vThermostatMirrorId${devId}" = null

			state.remove("vThermostat${devId}" as String)
			state.remove("vThermostatChildAppId${devId}" as String)
			state.remove("vThermostatMirrorId${devId}" as String)
			state.remove("oldvStatData${devId}" as String)

			def vtlist = atomicState?.vThermostats
			def newlist = [:]
			def vtstat
			vtstat = vtlist.collect { dni ->
				//LogAction("vThermostats: ${atomicState.vThermostats}  dni: ${dni}  dni.key: ${dni.key.toString()}  dni.value: ${dni.value.toString()} devId: ${devId}", "debug", true)
				def ttkey = dni.key.toString()
				if(ttkey == devId) { ; /*log.trace "skipping $dni"*/ }
				else { newlist[ttkey] = dni.value }
				return true
			}
			vtlist = newlist
			atomicState.vThermostats = vtlist
			if(!settings?.resetAllData) { runIn(120, "updated", [overwrite: true]) }  // create what is needed
		} else {
			LogAction("addRemoveVthermostat() unexpected operation state ${myID} ${atomicState?."vThermostat${devId}"} ${atomicState?."vThermostatChildAppId${devId}"}", "warn", true)
			return false
		}
		return true
	}
}

def deviceHandlerTest() {
	//LogTrace("deviceHandlerTest()")
	atomicState.devHandlersTested = true
	return true

	if(atomicState?.devHandlersTested || atomicState?.isInstalled || (atomicState?.thermostats && atomicState?.protects && atomicState?.cameras && atomicState?.vThermostats && atomicState?.presDevice && atomicState?.weatherDevice)) {
		atomicState.devHandlersTested = true
		return true
	}
}

def preReqCheck() {
	//LogTrace("preReqCheckTest()")
	if(!atomicState?.installData) { atomicState?.installData = ["initVer":appVersion(), "dt":getDtNow().toString(), "updatedDt":"Not Set", "freshInstall":true, "shownDonation":false, "shownFeedback":false, "shownChgLog":true, "usingNewAutoFile":true, "liteAppMode":isAppLiteMode()] }
	if(!location?.timeZone || !location?.zipCode) {
		atomicState.preReqTested = false
		LogAction("SmartThings Location not returning (TimeZone: ${location?.timeZone}) or (ZipCode: ${location?.zipCode}) Please edit these settings under the IDE", "warn", true)
		return false
	}
	else {
		atomicState.preReqTested = true
		return true
	}
}

//This code really does nothing at the moment but return the dynamic url of the app's endpoints
def getEndpointUrl() {
	def params = [
		uri: "https://graph.api.smartthings.com/api/smartapps/endpoints",
		query: ["access_token": atomicState?.accessToken],
		contentType: 'application/json'
	]
	try {
		httpGet(params) { resp ->
			LogAction("EndPoint URL: ${resp?.data?.uri}", "trace", false)
			return resp?.data?.uri
		}
	} catch (ex) {
		log.error "getEndpointUrl Exception:", ex
		sendExceptionData(ex, "getEndpointUrl")
	}
}

def getAccessToken() {
	try {
		if(!atomicState?.accessToken) { atomicState?.accessToken = createAccessToken() }
		else { return true }
	}
	catch (ex) {
		def msg = "Error: OAuth is not Enabled for ${appName()}!. Please click remove and Enable Oauth under the SmartApp App Settings in the IDE"
		sendPush(msg)
		log.error "getAccessToken Exception", ex
		LogAction("getAccessToken Exception | $msg", "warn", true)
		//sendExceptionData(ex, "getAccessToken")
		return false
	}
}

void resetSTAccessToken(reset) {
	if(reset != true) { return }
	LogAction("Resetting SmartApp Access Token....", "info", true)
	atomicState?.pollBlocked = true
	atomicState?.pollBlockedReason = "reseting Access Token"
	restStreamHandler(true, false)
	//atomicState?.restStreamingOn = false
	revokeAccessToken()
	atomicState?.accessToken = null
	atomicState?.pollBlocked = false
	atomicState?.pollBlockedReason = ""

	if(getAccessToken()) {
		LogAction("Reset SmartApp Access Token... Successful", "info", true)
		settingUpdate("resetSTAccessToken", "false", "bool")
	}
	startStopStream()
}

def generateInstallId() {
	if(!atomicState?.installationId) { atomicState?.installationId = UUID?.randomUUID().toString() }
}

/************************************************************************************************
|					Below This line handle SmartThings >> Nest Token Authentication				|
*************************************************************************************************/

//These are the Nest OAUTH Methods to aquire the auth code and then Access Token.
def oauthInitUrl() {
	//log.debug "oauthInitUrl with callback: ${callbackUrl}"
	atomicState.oauthInitState = UUID?.randomUUID().toString()
	def oauthParams = [
		response_type: "code",
		client_id: clientId(),
		state: atomicState?.oauthInitState,
		redirect_uri: callbackUrl //"https://graph.api.smartthings.com/oauth/callback"
	]
	redirect(location: "https://home.nest.com/login/oauth2?${toQueryString(oauthParams)}")
}

def callback() {
	try {
		LogTrace("callback()>> params: $params, params.code ${params.code}")
		def code = params.code
		LogTrace("Callback Code: ${code}")
		def oauthState = params.state
		LogTrace("Callback State: ${oauthState}")

		if(oauthState == atomicState?.oauthInitState){
			def tokenParams = [
				code: code.toString(),
				client_id: clientId(),
				client_secret: clientSecret(),
				grant_type: "authorization_code",
			]
			def tokenUrl = "https://api.home.nest.com/oauth2/access_token?${toQueryString(tokenParams)}"
			httpPost(uri: tokenUrl) { resp ->
				atomicState.authToken = resp?.data.access_token
				if(atomicState?.authToken) {
					updTimestampMap("authTokenCreatedDt", getDtNow())
					atomicState.authTokenExpires = resp?.data.expires_in
					// atomicState.authTokenNum = clientToken()
					atomicState.oauthInitState = UUID?.randomUUID().toString()
				}
			}

			if(atomicState?.authToken) {
				LogAction("Nest AuthToken Generated SUCCESSFULLY", "info", true)
				atomicState.needStrPoll = true
				atomicState?.needDevPoll = true
				atomicState?.needMetaPoll = true
				runIn(5, "finishRemap", [overwrite: true])
				success()

			} else {
				LogAction("Failure Generating Nest AuthToken", "error", true)
				fail()
			}
		} else { LogAction("callback() oauthState != atomicState.oauthInitState", "error", true) }
	}
	catch (ex) {
		log.error "Oauth Callback Exception:", ex
		sendExceptionData(ex, "callback")
	}
}

def finishRemap() {
	LogTrace("finishRemap (${atomicState?.pollBlocked}) (${atomicState?.pollBlockedReason})")
 	checkRemapping()
 	atomicState.needToFinalize = true
 	runIn(21, "finalizeRemap", [overwrite: true])
}

def finalizeRemap() {
	LogTrace("finalizeRemap (${atomicState?.pollBlocked}) (${atomicState?.pollBlockedReason})")
 	fixDevAS()
 	//sendInstallSlackNotif(false)
 	atomicState.needToFinalize = false
 	initManagerApp()
 	state.remove("needToFinalize")
}

def revokeNestToken() {
	if(atomicState?.authToken) {
		LogAction("revokeNestToken()", "info", true)
		restStreamHandler(true, false)
		//atomicState?.restStreamingOn = false
		def params = [
			uri: "https://api.home.nest.com",
			path: "/oauth2/access_tokens/${atomicState?.authToken}",
			contentType: 'application/json'
		]
		try {
			httpDelete(params) { resp ->
				if(resp?.status == 204) {
					LogAction("Nest Token revoked", "warn", true)
					revokeCleanState()
					return true
				}
			}
		}
		catch (ex) {
			if(ex?.message?.toString() == "Not Found") {
				revokeCleanState()
				return true
			} else {
				log.error "revokeNestToken Exception:", ex
				revokeCleanState()
				sendExceptionData(ex, "revokeNestToken")
				return false
			}
		}
	} else { revokeCleanState() }
}

def revokeCleanState() {
	unschedule()
	atomicState.authToken = null
	updTimestampMap("authTokenCreatedDt", null)
	atomicState.authTokenExpires = null
	atomicState.structData = null
	atomicState.deviceData = null
	atomicState.metaData = null
	updTimestampMap("lastStrDataUpd", null)
	updTimestampMap("lastDevDataUpd", null)
	updTimestampMap("lastMetaDataUpd", null)
	atomicState?.pollingOn = false
	atomicState.streamPolling = false
	atomicState?.pollBlocked = false
	atomicState?.workQrunInActive = false
	atomicState?.pollBlockedReason = "No Auth Token"
}

//HTML Connections Pages
def success() {
	def message = """
	<p>Your SmartThings Account is now connected to Nest!</p>
	<p>Click <b>Done</b> or <b>Next</b> to proceed with the rest of the setup.</p>
	"""
	connectionStatus(message)
}

def fail() {
	def message = """
	<p>The connection could not be established!</p>
	<p>Click <b>Done</b> or <b>Next</b> to return to the menu.</p>
	"""
	connectionStatus(message)
}

def connectionStatus(message, redirectUrl = null) {
	def redirectHtml = ""
	if(redirectUrl) { redirectHtml = """<meta http-equiv="refresh" content="3; url=${redirectUrl}" />""" }
	def html = """
		<!DOCTYPE html>
		<html>
		<head>
			<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
			<title>SmartThings & Nest connection</title>
			<link rel="stylesheet" href="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/css/authresults.css">
		</head>
		<body>
			<div class="container" style="margin: auto; position: relative;">
				<img class="stlogo" src="https://github.com/tonesto7/nest-manager/raw/master/Images/App/st-standalone-logo.png" alt="SmartThings logo" /></br></br>
				<img class="linklogo" src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" /></br></br>
				<img class="nestlogo" src="https://github.com/tonesto7/nest-manager/raw/master/Images/App/nst_manager_5.png" alt="nest icon" />
				${message}
			</div>
		</body>
		</html>
		"""
/* """ */
	render contentType: 'text/html', data: html
}

def toJson(Map m) {
	return new org.json.JSONObject(m).toString()
}

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def clientId() {
	if(appSettings?.clientId && appSettings?.clientId != "blank") {
		return appSettings?.clientId?.toString().trim()
	} else {
		if(atomicState?.appData?.token?.id) {
			return atomicState?.appData?.token?.id
		} else {
			LogAction("clientId is missing and is required to generate your Nest Auth token. Please verify you are running the latest software version", "error", true)
		}
		return null
	}
}

def clientSecret() {
	if(appSettings?.clientSecret && appSettings?.clientSecret != "blank") {
		return appSettings?.clientSecret?.toString().trim()
	} else {
		if(atomicState?.appData?.token?.secret) {
			return atomicState?.appData?.token?.secret
		} else {
			LogAction("clientSecret is missing and is required to generate your Nest Auth token. Please verify you are running the latest software version", "error", true)
		}
		return null
	}
}

def nestDevAccountCheckOk() {
	if(atomicState?.authToken == null && (clientId() == null || clientSecret() == null) ) { return false }
	else { return true }
}

/************************************************************************************************
|									LOGGING AND Diagnostic										|
*************************************************************************************************/
def LogTrace(msg, logSrc=null) {
	def trOn = (settings?.appDebug && settings?.advAppDebug && !settings?.enRemDiagLogging) ? true : false
	if(trOn) {
		def logOn = (settings?.enRemDiagLogging && atomicState?.enRemDiagLogging) ? true : false
		def theLogSrc = (logSrc == null) ? (parent ? "Automation" : "Manager") : logSrc
		Logger(msg, "trace", theLogSrc, logOn)
	}
}

def LogAction(msg, type="debug", showAlways=false, logSrc=null) {
	def isDbg = (settings?.appDebug && !settings?.enRemDiagLogging) ? true : false
	def theLogSrc = (logSrc == null) ? (parent ? "Automation" : "Manager") : logSrc
	if(showAlways) { Logger(msg, type, theLogSrc) }
	else if(isDbg && !showAlways) { Logger(msg, type, theLogSrc) }
}

def tokenStrScrubber(str) {
	def regex1 = /(Bearer c.{1}\w+)/
	def regex2 = /(auth=c.{1}\w+)/
	def newStr = str.replaceAll(regex1, "Bearer 'token code redacted'")
	newStr = newStr.replaceAll(regex2, "auth='token code redacted'")
	//log.debug "newStr: $newStr"
	return newStr
}

def Logger(msg, type, logSrc=null, noSTlogger=false) {
	def labelstr = ""
	def logOut = true
	if(settings?.debugAppendAppName != false) { labelstr = "${app.label} (v${appVersion()}) | " }
	if(msg && type) {
		def themsg = tokenStrScrubber("${labelstr}${msg}")

		if(atomicState?.enRemDiagLogging && settings?.enRemDiagLogging && atomicState?.remDiagAppAvailable == true) {
			if(saveLogtoRemDiagStore(themsg, type, logSrc) == true) {
				logOut = false
			}
		}
		// log.debug "logOut: $logOut | noSTlogger: $noSTlogger"
		if(logOut == true && noSTlogger == false) {
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
	}
	else { log.error "${labelstr}Logger Error - type: ${type} | msg: ${msg} | logSrc: ${logSrc}" }
}

def getDiagLogTimeRemaining() {
	return sec2PrettyTime((3600*48) - Math.abs((getRemDiagActSec() ?: 0)))
}

String sec2PrettyTime(Integer timeSec) {
    Integer years = Math.floor(timeSec / 31536000); timeSec -= years * 31536000;
    Integer months = Math.floor(timeSec / 31536000); timeSec -= months * 2592000;
    Integer days = Math.floor(timeSec / 86400); timeSec -= days * 86400;
    Integer hours = Math.floor(timeSec / 3600); timeSec -= hours * 3600;
    Integer minutes = Math.floor(timeSec / 60); timeSec -= minutes * 60;
    Integer seconds = Integer.parseInt((timeSec % 60) as String, 10);
    Map dt = [y: years, mn: months, d: days, h: hours, m: minutes, s: seconds]
	String dtStr = ""
	// dtStr += dt?.y ? "${dt?.y}yr${dt?.y>1?"s":""}, " : ""
	// dtStr += dt?.mn ? "${dt?.mn}mon${dt?.mn>1?"s":""}, " : ""
	// dtStr += dt?.d ? "${dt?.d}day${dt?.d>1?"s":""}, " : ""
	// dtStr += dt?.h ? "${dt?.h}hr${dt?.h>1?"s":""} " : ""
	// dtStr += dt?.m ? "${dt?.m}min${dt?.m>1?"s":""} " : ""
	// dtStr += dt?.s ? "${dt?.s}sec" : ""
	dtStr += dt?.d ? "${dt?.d}d " : ""
	dtStr += dt?.h ? "${dt?.h}h " : ""
	dtStr += dt?.m ? "${dt?.m}m " : ""
	dtStr += dt?.s ? "${dt?.s}s" : ""
	return dtStr
}

def saveLogtoRemDiagStore(String msg, String type, String logSrcType=null, frc=false) {
	def retVal = false
	// log.trace "saveLogtoRemDiagStore($msg, $type, $logSrcType)"
	if(atomicState?.enRemDiagLogging && settings?.enRemDiagLogging) {
		def turnOff = false
		def reasonStr = ""
		if(frc == false) {
			if(getRemDiagActSec() > (3600 * 48)) {
				turnOff = true
				reasonStr += "was active for last 48 hours "
			}
		}
		if(turnOff) {
			saveLogtoRemDiagStore("Diagnostics disabled due to ${reasonStr}", "info", "Manager", true)
			diagLogProcChange(false)
			log.info "Remote Diagnostics disabled ${reasonStr}"
		} else {
			if(getStateSizePerc() >= 65) {
				log.warn "saveLogtoRemDiagStore: remoteDiag log storage suspended state size is ${getStateSizePerc()}%"
			} else {
				if(msg) {
					def data = atomicState?.remDiagLogDataStore ?: []
					def dt = new Date().getTime()
					def item = ["dt":dt, "type":type, "src":(logSrcType ?: "Not Set"), "msg":msg]
					data << item
					atomicState?.remDiagLogDataStore = data
					retVal = true
				}
			}

			def data = atomicState?.remDiagLogDataStore ?: []
			def t0 = data?.size()
			if(t0 && (t0 > 30 || frc || getLastRemDiagSentSec() > 120 || getStateSizePerc() >= 65)) {
				def remDiagApp = getRemDiagApp()
				if(remDiagApp) {
					remDiagApp?.savetoRemDiagChild(data)
					updTimestampMap("remDiagDataSentDt", getDtNow())
					//atomicState?.remDiagDataSentDt = getDtNow()
				} else {
					log.warn "Remote Diagnostics Child app not found"
					if(getRemDiagActSec() > 20) {	// avoid race that child did not start yet
						diagLogProcChange(false)
					}
					retVal = false
				}
				atomicState?.remDiagLogDataStore = []
			}
		}
	}
	return retVal
}

def fixState() {
	def result = false
	LogTrace("fixState (${atomicState?.pollBlocked}) (${atomicState?.pollBlockedReason})")
	def before = getStateSizePerc()
	if(!parent) {
		if(!atomicState?.resetAllData && resetAllData) {
			def data = getState()?.findAll { !(it?.key in ["accessToken", "authToken", "authTokenExpires", "timestampDtMap", "authTokenNum", "enRemDiagLogging",
				"installationId", "installData", "remDiagLogDataStore", "remDiagDataSentDt", "resetAllData", "pollingOn", "pollBlocked", "ssdpOn",
				"pollBLockedReason", "resetAllData", "autoMigrationComplete", "tsMigration", "savedNestSettings",
				 "apiCommandCnt", "apiStrReqCnt", "apiDevReqCnt", "apiMetaReqCnt", "apiRestStrEvtCnt", "appNotifSentCnt", "structData", "deviceData",
				"disableAllAutomations", "automationNestModeEnabled", "automationNestModeEcoActive" ]) }
			data.each { item ->
				state.remove(item?.key.toString())
			}
			unschedule()
			unsubscribe()
			atomicState.ssdpOn = false
			atomicState.pollingOn = false
			atomicState?.pollBlocked = true
			atomicState?.pollBlockedReason = "Repairing State"
			toggleAllAutomations()
			updTimestampMap("lastChildUpdDt", getDtNow()) // make sure we don't try to force child update too soon
			updTimestampMap("lastDevDataUpd", getDtNow())
			result = true
		} else if(atomicState?.resetAllData && !resetAllData) {
			LogAction("fixState: resetting ALL toggle", "info", true)
			atomicState.resetAllData = false
		}

	}
	if(result) {
		atomicState.resetAllData = true
		LogAction("fixState: State Data: before: $before  after: ${getStateSizePerc()}", "info", true)
		runIn(22, "finishFixState", [overwrite: true])
	}
	return result
}

void finishFixState() {
	LogTrace("finishFixState (${atomicState?.pollBlocked}) (${atomicState?.pollBlockedReason})")
	atomicState?.pollBlockedReason = "finishFixState"
	if(!parent) {
		if(atomicState?.resetAllData) {
			if(atomicState?.notificationPrefs == null) { atomicState?.notificationPrefs = buildNotifPrefMap() }
			atomicState.devNameOverride = settings?.devNameOverride ? true : false
			atomicState.useAltNames = settings?.useAltNames ? true : false
			atomicState.custLabelUsed = settings?.useCustDevNames ? true : false
			if(!atomicState?.installData) {
				atomicState?.installData = ["initVer":appVersion(), "dt":getDtNow().toString(), "updatedDt":"Not Set", "freshInstall":true, "shownDonation":false, "shownFeedback":false, "shownChgLog":true, "usingNewAutoFile":true, "liteAppMode":isAppLiteMode()]
			}

			getWebFileData() // get the appData and calls setStateVar

			atomicState.needStrPoll = true
			atomicState?.needDevPoll = true
			atomicState?.needMetaPoll = true

			atomicState.structures = settings?.structures ?: null

			def structs = getNestStructures(true)

			fixDevAS()

			if(settings?.thermostats || settings?.protects || settings?.cameras || settings?.presDevice || settings?.weatherDevice) {
				atomicState.isInstalled = true
				atomicState.newSetupComplete = true
				atomicState?.setupVersion = atomicState?.appData?.updater?.setupVersion?.toInteger() ?: 0
			} else { atomicState.isInstalled = false }

			runIn(21, "initManagerApp", [overwrite: true])	// need to give time for watchdog updates before we try to delete devices.
			//initManagerApp()
		}
	} else {
		LogAction("finishFixState called as CHILD", "error", true)
	}
}

def fixDevAS() {
	LogTrace("fixDevAS")
	if(settings?.thermostats && !atomicState?.thermostats) { atomicState.thermostats = settings?.thermostats ? statState(settings?.thermostats) : null }
	if(settings?.protects && !atomicState?.protects) { atomicState.protects = settings?.protects ? coState(settings?.protects) : null }
	if(settings?.cameras && !atomicState?.cameras) { atomicState.cameras = settings?.cameras ? camState(settings?.cameras) : null }
	atomicState.presDevice = settings?.presDevice ?: null
	atomicState.weatherDevice = settings?.weatherDevice ?: null
}

void settingUpdate(name, value, type=null) {
	LogAction("settingUpdate($name, $value, $type)...", "trace", false)
	if(name) {
		if(value == "" || value == null || value == []) {
			settingRemove(name)
			return
		}
	}
	if(name && type) {
		app?.updateSetting("$name", [type: "$type", value: value])
	}
	else if (name && type == null){ app?.updateSetting(name.toString(), value) }
}

void settingRemove(name) {
	LogAction("settingRemove($name)...", "trace", false)
	if(name) { app?.deleteSetting("$name") }
}

def stateUpdate(key, value) {
	if(key) { atomicState?."${key}" = value }
	else { LogAction("stateUpdate: null key $key $value", "error", true) }
}

def setStateVar(frc = false) {
	LogTrace("setStateVar")
	//If the developer changes the version in the web appData JSON it will trigger
	//the app to create any new state values that might not exist or reset those that do to prevent errors
	def stateVer = 3
	def stateVar = !atomicState?.stateVarVer ? 0 : atomicState?.stateVarVer.toInteger()
	if(!atomicState?.stateVarUpd || frc || (stateVer < atomicState?.appData?.settings?.maintenance?.stateVer?.toInteger())) {
		if(!atomicState?.newSetupComplete)		{ atomicState.newSetupComplete = false }
		if(!atomicState?.setupVersion)			{ atomicState?.setupVersion = 0 }
		if(!atomicState?.custLabelUsed)			{ atomicState?.custLabelUsed = false }
		if(!atomicState?.useAltNames)			{ atomicState.useAltNames = false }
		if(!atomicState?.apiCommandCnt)			{ atomicState?.apiCommandCnt = 0L }
		atomicState?.stateVarUpd = true
		atomicState?.stateVarVer = atomicState?.appData?.settings?.maintenance?.stateVer ? atomicState?.appData?.settings?.maintenance?.stateVer?.toInteger() : 0
	}
}

def timestampMigration() {
	def items = ["apiIssueDt","authTokenCreatedDt", "cmdLastProcDt", "debugEnableDt", "lastApiIssueMsgDt", "lastChildUpdDt", "lastDevDataUpd", "lastFailedCmdMsgDt", "lastForcePoll", "lastForecastUpdDt",
			"lastHeardFromNestDt", "lastMetaDataUpd", "lastMisPollMsgDt", "lastStrDataUpd", "lastUpdMsgDt", "lastUpdateMsgDt", "lastWeatherUpdDt", "lastWebUpdDt", "remDiagLogActivatedDt"
	]
	def sData = atomicState?.timestampDtMap ?: [:]
	items?.each { item->
		if(sData[item] == null) { sData[item] = state[item] }
		state.remove(item)
	}
	atomicState?.tsMigration = true
}

//Things that need to clear up on updates go here
def stateCleanup() {
	LogAction("stateCleanup", "trace", true)

	def data = [ "exLogs", "pollValue", "pollStrValue", "pollWaitVal", "tempChgWaitVal", "cmdDelayVal", "testedDhInst", "missedPollNotif", "updateMsgNotif", "updChildOnNewOnly", "disAppIcons",
		"showProtAlarmStateEvts", "showAwayAsAuto", "cmdQlist", "cmdQ", "recentSendCmd", "cmdIsProc", "currentWeather", "altNames", "locstr", "custLocStr", "autoAppInstalled", "nestStructures", "lastSentExceptionDataDt",
		"swVersion", "dashSetup", "dashboardUrl", "apiIssues", "stateSize", "haveRun", "lastStMode", "lastPresSenAway", "devCodeIdData", "appCodeIdData", "clientBlacklisted",
		"automationsActive", "temperatures", "powers", "energies", "use24Time", "useMilitaryTime", "advAppDebug", "appDebug", "awayModes", "homeModes", "childDebug", "updNotifyWaitVal",
		"appApiIssuesWaitVal", "misPollNotifyWaitVal", "misPollNotifyMsgWaitVal", "devHealthMsgWaitVal", "nestLocAway", "heardFromRestDt", "autoSaVer", "lastHeardFromRestDt",
		"remDiagApp", "remDiagClientId", "restorationInProgress", "diagManagAppStateFilters", "diagChildAppStateFilters", "lastFinishedPoll","tDevVer", "pDevVer", "camDevVer", "presDevVer", "weatDevVer", "vtDevVer", "streamDevVer",
		/* "curAlerts", */ "curAstronomy", /* "curForecast", "curWeather", */ "detailEventHistory", "detailExecutionHistory", "evalExecutionHistory", "lastForecastUpdDt", "lastWeatherUpdDt",
		"lastMsg", "lastMsgDt", "qFirebaseRequested", "qmetaRequested", "debugAppendAppName", "ReallyChanged", "tsMigrationDone", "pushTested", "lastDevHealthMsgData"
 	]

	["oldTstat", "oldvTstat", "oldvStat", "oldCamData", "oldProt", "oldPres", "oldWeather", "lastCmdSentDt", "recentSendCmd" ]?.each { oi->
		def oiRem = state?.findAll { it?.key?.toString().startsWith(oi) }.collect { it?.key }
		data = data+oiRem
	}
	data?.each { item ->
		//if(state?.containsKey(item)) { state.remove(item?.toString()) }
		state.remove(item?.toString())
	}

	atomicState.authTokenExpires = atomicState?.tokenExpires ?: atomicState?.authTokenExpires
	state.remove("tokenExpires")
	updTimestampMap("authTokenCreatedDt", (atomicState?.tokenCreatedDt ?: getTimestampVal("authTokenCreatedDt")))
	state.remove("tokenCreatedDt")

	if(!atomicState?.cmdQlist) {
		data = [ "cmdQ0", "cmdQ1", "cmdQ2", "cmdQ3", "cmdQ4", "cmdQ5", "cmdQ6", "cmdQ7", "cmdQ8", "cmdQ9", "cmdQ10", "cmdQ11", "cmdQ12", "cmdQ13", "cmdQ14", "cmdQ15" ]
		data.each { item ->
			//if(state?.containsKey(item)) { state.remove(item?.toString()) }
			state.remove(item?.toString())
		}
	}
	atomicState?.workQrunInActive = false
	atomicState.forceChildUpd = true
	def remSettings = [ "showAwayAsAuto", "temperatures", "powers", "energies", "childDevDataPageDev", "childDevPageRfsh", "childDevDataRfshVal", "childDevDataStateFilter", "childDevPageShowAttr",
		"childDevPageShowCapab", "childDevPageShowCmds", "childDevPageShowState", "managAppPageRfsh", "managAppPageShowMeta", "managAppPageShowSet", "managAppPageShowState", "updChildOnNewOnly",
		"locDesiredButton", "locDesiredTempScale", "recipients", "enableDashboard"
	]
	List camMotionSets = settings?.keySet()?.findAll { it?.toString()?.startsWith("camera_") && it?.toString()?.endsWith("_zones") }?.collect { it as String }
	if(camMotionSets?.size()) {
		if(settings?.camEnMotionZoneFltr) {
			atomicState?.cameras?.keySet()?.each { cam-> if(camMotionSets?.find {"camera_${cam}_zones"}) { camMotionSets = camMotionSets - "camera_${cam}_zones" } }
		}
		remSettings = remSettings+camMotionSets
	}
	remSettings.each { item ->
		//if(settings?.containsKey(item)) {
			settingRemove(item.toString())	// removes settings
		//}
	}
	runIn(25, "cleanStorage", [overwrite: true]) // calling the child truncates logs
}

def cleanStorage() {
	remStorageVal("curForecast")
	remStorageVal("curAstronomy")
	remStorageVal("curWeather")
	remStorageVal("curAlerts")
}

/******************************************************************************
*								STATIC METHODS								*
*******************************************************************************/
def getThermostatChildName()	{ return getChildName("Nest Thermostat") }
def getProtectChildName()	{ return getChildName("Nest Protect") }
def getPresenceChildName()	{ return getChildName("Nest Presence") }
def getWeatherChildName()	{ return getChildName("Nest Weather") }
def getCameraChildName()	{ return getChildName("Nest Camera") }

def getAutoAppChildName()	{ return getChildName(autoAppName()) }
def getWatDogAppChildLabel()	{ return getChildName("Nest Location ${location.name} Watchdog") }
def getRemDiagAppChildLabel()	{ return getChildName("NST Location ${location.name} Diagnostics") }
def getStorageAppChildLabel()	{ return getChildName("NST Location ${location.name} Storage") }

def getChildName(str)		{ return "${str}${appDevName()}" }

def getServerUrl()			{ return "https://graph.api.smartthings.com" }
def getShardUrl()			{ return getApiServerUrl() }
def getCallbackUrl()		{ return "https://graph.api.smartthings.com/oauth/callback" }
def getBuildRedirectUrl()	{ return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState?.accessToken}&apiServerUrl=${shardUrl}" }
def getNestApiUrl()			{ return "https://developer-api.nest.com" }
def getAppEndpointUrl(subPath)	{ return "${apiServerUrl("/api/smartapps/installations/${app.id}${subPath ? "/${subPath}" : ""}?access_token=${atomicState.accessToken}")}" }
def getWikiPageUrl()		{ return "http://thingsthataresmart.wiki/index.php?title=NST_Manager" }
def getIssuePageUrl()		{ return "https://github.com/tonesto7/nest-manager/issues" }
def slackMsgWebHookUrl()	{ return "https://hooks.slack.com/services/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" }
def getAutoHelpPageUrl()	{ return "http://thingsthataresmart.wiki/index.php?title=NST_Manager#Nest_Automations" }
def weatherApiKey()			{ return "b82aba1bb9a9d7f1" }
def getFbLegacyAppUrl() 	{ return "https://st-nest-manager.firebaseio.com" }
def getFbMetricsUrl() 		{ return atomicState?.appData?.settings?.database?.metricsUrl ?: "https://nst-manager-metrics.firebaseio.com" }
def getFbExceptionsUrl() 	{ return atomicState?.appData?.settings?.database?.exceptionUrl ?: "https://nst-manager-exceptions.firebaseio.com" }
def getAppSettingsUrl() 	{ return "https://raw.githubusercontent.com/${gitPath()}/Data/appConfig.json" }
def getAppSettingsFBUrl() 	{ return "https://st-nest-manager.firebaseio.com/appSettings.json" }
def getAppImg(imgName, on = null)	{ return (!disAppIcons || on) ? "https://raw.githubusercontent.com/tonesto7/nest-manager/${gitBranch()}/Images/App/$imgName" : "" }
def getDevImg(imgName, on = null)	{ return (!disAppIcons || on) ? "https://raw.githubusercontent.com/tonesto7/nest-manager/${gitBranch()}/Images/Devices/$imgName" : "" }
private Integer convertHexToInt(hex) { Integer.parseInt(hex,16) }
private String convertHexToIP(hex) { [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".") }

def latestTstatVer()		{ return atomicState?.appData?.updater?.versions?.thermostat ?: "unknown" }
def latestProtVer()		{ return atomicState?.appData?.updater?.versions?.protect ?: "unknown" }
def latestPresVer()		{ return atomicState?.appData?.updater?.versions?.presence ?: "unknown" }
def latestWeathVer()		{ return atomicState?.appData?.updater?.versions?.weather ?: "unknown" }
def latestCamVer()		{ return atomicState?.appData?.updater?.versions?.camera ?: "unknown" }
def latestvStatVer()		{ return atomicState?.appData?.updater?.versions?.thermostat ?: "unknown" }
def getChildAppVer(appName)	{ return appName?.appVersion() ? "v${appName?.appVersion()}" : "" }
def getUse24Time()		{ return useMilitaryTime ? true : false }

//Returns app State Info
def getStateSize() {
	def resultJson = new groovy.json.JsonOutput().toJson(state)
	return resultJson?.toString().length()
	//return state?.toString().length()
}
def getStateSizePerc() { return (int) ((stateSize / 100000)*100).toDouble().round(0) }

def debugStatus() { return !settings?.appDebug ? "Off" : "On" }
def deviceDebugStatus() { return !settings?.childDebug ? "Off" : "On" }
def isAppDebug() { return !settings?.appDebug ? false : true }
def isChildDebug() { return !settings?.childDebug ? false : true }

def getLocationModes() {
	def result = []
	location?.modes.sort().each {
		if(it) { result.push("${it}") }
	}
	return result
}

def showDonationOk() {
	return (!atomicState?.installData?.shownDonation && getDaysSinceUpdated() >= 30) ? true : false
}

def showFeedbackOk() {
	return (!atomicState?.installData?.shownFeedback && getDaysSinceUpdated() >= 7) ? true : false
}

def showChgLogOk() {
	return (!atomicState?.installData?.shownChgLog && atomicState?.isInstalled) ? true : false
}

def getDaysSinceInstall() {
	def instDt = atomicState?.installData?.dt
	if(instDt == null || instDt == "Not Set") {
		def iData = atomicState?.installData
		iData["dt"] = getDtNow().toString()
		atomicState?.installData = iData
		return 0
	}
	def start = Date.parse("E MMM dd HH:mm:ss z yyyy", instDt)
	def stop = new Date()
	if(start && stop) {
		return (stop - start)
	}
	return 0
}

def getDaysSinceUpdated() {
	def updDt = atomicState?.installData?.updatedDt
	if(updDt == null || updDt == "Not Set") {
		def iData = atomicState?.installData
		iData["updatedDt"] = getDtNow().toString()
		atomicState?.installData = iData
		return 0
	}
	def start = Date.parse("E MMM dd HH:mm:ss z yyyy", updDt)
	def stop = new Date()
	if(start && stop) {
		return (stop - start)
	}
	return 0
}

def getObjType(obj, retType=false) {
	if(obj instanceof String) {return "String"}
	else if(obj instanceof GString) {return "GString"}
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

def getShowHelp() { return true }

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

def formatDt2(tm, fmt=null) {
	def formatVal = fmt == null ? (settings?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a") : fmt
	def tf = new SimpleDateFormat(formatVal)
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	return tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", tm.toString()))
}

private getTimeSeconds(timeKey, defVal, meth) {
	def t0 = getTimestampVal(timeKey)
	return !t0 ? defVal : GetTimeDiffSeconds(t0, null, meth).toInteger()
}

void updTimestampMap(keyName, dt=null) {
	def data = atomicState?.timestampDtMap ?: [:]
	if(keyName) { data[keyName] = dt }
	atomicState?.timestampDtMap = data
}

def getTimestampVal(val) {
	def tsData = atomicState?.timestampDtMap
	if(val && tsData && tsData[val]) { return tsData[val] }
	return null
}

def GetTimeDiffSeconds(strtDate, stpDate=null, methName=null) {
	//LogTrace("[GetTimeDiffSeconds] StartDate: $strtDate | StopDate: ${stpDate ?: "Not Sent"} | MethodName: ${methName ?: "Not Sent"})")
	if((strtDate && !stpDate) || (strtDate && stpDate)) {
		//if(strtDate?.contains("dtNow")) { return 10000 }
		def stopVal = stpDate ? stpDate.toString() : getDtNow() //formatDt(now)
/*
		def startDt = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate)
		def stopDt = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal)
		def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(startDt)).getTime()
*/
		def start = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate).getTime()
		def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal).getTime()
		def diff = (int) (long) (stop - start) / 1000
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

// parent only Method
def notificationTimeOk() {
	def strtTime = null
	def stopTime = null
	def now = new Date()
	def sun = getSunriseAndSunset() // current based on geofence, previously was: def sun = getSunriseAndSunset(zipCode: zipCode)
	if(settings?.qStartTime && settings?.qStopTime) {
		if(settings?.qStartInput == "sunset") { strtTime = sun.sunset }
		else if(settings?.qStartInput == "sunrise") { strtTime = sun.sunrise }
		else if(settings?.qStartInput == "A specific time" && settings?.qStartTime) { strtTime = settings?.qStartTime }

		if(settings?.qStopInput == "sunset") { stopTime = sun.sunset }
		else if(settings?.qStopInput == "sunrise") { stopTime = sun.sunrise }
		else if(settings?.qStopInput == "A specific time" && settings?.qStopTime) { stopTime = settings?.qStopTime }
	} else { return true }
	if(strtTime && stopTime) {
		return timeOfDayIsBetween(strtTime, stopTime, new Date(), getTimeZone()) ? false : true
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
		tf.setTimeZone(getTimeZone())
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

def notifValEnum(allowCust = false) {
	def vals = [
		60:"1 Minute", 300:"5 Minutes", 600:"10 Minutes", 900:"15 Minutes", 1200:"20 Minutes", 1500:"25 Minutes", 1800:"30 Minutes",
		3600:"1 Hour", 7200:"2 Hours", 14400:"4 Hours", 21600:"6 Hours", 43200:"12 Hours", 86400:"24 Hours"
	]
	if(allowCust) { vals << [ 1000000:"Custom" ] }
	return vals
}

def pollValEnum(device=false) {
	def vals = [:]
	// if(device && inReview()) { vals = [ 30:"30 Seconds" ] }
	vals << [
		60:"1 Minute", 120:"2 Minutes", 180:"3 Minutes", 240:"4 Minutes", 300:"5 Minutes",
		600:"10 Minutes", 900:"15 Minutes", 1200:"20 Minutes", 1500:"25 Minutes",
		1800:"30 Minutes", 2700:"45 Minutes", 3600:"60 Minutes"
	]
	return vals
}

def waitValAltEnum(allow30=false) {
	def vals = [:]
	if(allow30) { vals << [ 30:"30 Seconds" ] }
	vals << [
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
	if(inputName && enumName) {
		enumName.each { item ->
			if(item?.key.toString() == inputName?.toString()) {
				result = item?.value
			}
		}
	}
	return result
}

def generateMD5_A(String s) {
	MessageDigest digest = MessageDigest.getInstance("MD5")
	digest.update(s.bytes)
	return digest.digest().toString()
}

def minDevVer2Str(val) {
	def str = ""
	def list = []
	str += "v"
	val?.each {
		list.add(it)
	}
}

/******************************************************************************
*					 	DIAGNOSTIC & NEST API INFO PAGES					*
*******************************************************************************/
def alarmTestPage() {
	def execTime = now()
	dynamicPage(name: "alarmTestPage", install: false, uninstall: false) {
		if(atomicState?.protects) {
			section("Select Carbon/Smoke Device to Test:") {
				input name: "alarmCoTestDevice", title:"Select the Protect to Test", type: "enum", required: false, multiple: false, submitOnChange: true, metadata: [values:atomicState?.protects], image: getAppImg("protect_icon.png")
			}
			if(settings?.alarmCoTestDevice) {
				section("Select the Event to Generate:") {
					input "alarmCoTestDeviceSimSmoke", "bool", title: "Simulate a Smoke Event?", defaultValue: false, submitOnChange: true, image: getDevImg("smoke_emergency.png")
					input "alarmCoTestDeviceSimCo", "bool", title: "Simulate a Carbon Event?", defaultValue: false, submitOnChange: true, image: getDevImg("co_emergency.png")
					input "alarmCoTestDeviceSimLowBatt", "bool", title: "Simulate a Low Battery Event?", defaultValue: false, submitOnChange: true, image: getDevImg("battery_low.png")
				}
				if(settings?.alarmCoTestDeviceSimLowBatt || settings?.alarmCoTestDeviceSimCo || settings?.alarmCoTestDeviceSimSmoke) {
					section("Execute Selected Tests from Above:") {
						if(!atomicState?.isAlarmCoTestActive) {
							paragraph "WARNING:\nIf protect devices are used by Smart Home Monitor (SHM) it will not be seen as a test and will trigger any actions and/or alarms you have configured.",
									required: true, state: null
						}
						if(settings?.alarmCoTestDeviceSimSmoke && !settings?.alarmCoTestDeviceSimCo && !settings?.alarmCoTestDeviceSimLowBatt) {
							href "simulateTestEventPage", title: "Simulate Smoke Event", params: ["testType":"smoke"], description: "Tap to Execute Test", required: true, state: null
						}

						if(settings?.alarmCoTestDeviceSimCo && !settings?.alarmCoTestDeviceSimSmoke && !settings?.alarmCoTestDeviceSimLowBatt) {
							href "simulateTestEventPage", title: "Simulate Carbon Event", params: ["testType":"co"], description: "Tap to Execute Test", required: true, state: null
						}

						if(settings?.alarmCoTestDeviceSimLowBatt && !settings?.alarmCoTestDeviceSimCo && !settings?.alarmCoTestDeviceSimSmoke) {
							href "simulateTestEventPage", title: "Simulate Battery Event", params: ["testType":"battery"], description: "Tap to Execute Test", required: true, state: null
						}
					}
				}

				if(atomicState?.isAlarmCoTestActive && (settings?.alarmCoTestDeviceSimLowBatt || settings?.alarmCoTestDeviceSimCo || settings?.alarmCoTestDeviceSimSmoke)) {
					section("Instructions") {
						paragraph "FYI: Clear ALL Selected Tests to Reset for New Alarm Test", required: true, state: null
					}
				}
				if(!settings?.alarmCoTestDeviceSimLowBatt && !settings?.alarmCoTestDeviceSimCo && !settings?.alarmCoTestDeviceSimSmoke) {
					atomicState?.isAlarmCoTestActive = false
					atomicState?.curProtTestPageData = null
				}
			}
		}
		devPageFooter("protTestLoadCnt", execTime)
	}
}

void resetAlarmTest() {
	LogAction("Resetting Protect Alarm Test back to the default.", "info", true)
	settingRemove("alarmCoTestDevice")
	settingRemove("alarmCoTestDeviceSimSmoke")
	settingRemove("alarmCoTestDeviceSimCo")
	settingRemove("alarmCoTestDeviceSimLowBatt")
	atomicState?.isAlarmCoTestActive = false
	atomicState?.curProtTestPageData = null
}

def getLastAlarmTestDtSec() { return !atomicState?.isAlarmCoTestActiveDt ? 100000 : GetTimeDiffSeconds(atomicState?.isAlarmCoTestActiveDt, null, "getLastAlarmTestDtSec").toInteger() }

def simulateTestEventPage(params) {
	//def pName = getAutoType()
	def testType
	if(params?.testType) {
		atomicState.curProtTestType = params?.testType
		testType = params?.testType
	} else {
		testType = atomicState?.curProtTestType
	}
	dynamicPage(name: "simulateTestEventPage", refreshInterval: (atomicState?.isAlarmCoTestActive ? null : 10), install: false, uninstall: false) {
		if(settings?.alarmCoTestDevice) {
			def dev = getChildDevice(settings?.alarmCoTestDevice)
			def testText
			if(dev) {
				section("Testing ${dev}") {
					def isRun = false
					if(!atomicState?.isAlarmCoTestActive) {
						atomicState?.isAlarmCoTestActive = true
						atomicState?.isAlarmCoTestActiveDt = getDtNow()
						runIn(60, "resetAlarmTest", [overwrite: true])
						if(testType == "co") {
							testText = "Carbon 'Detected'"
							dev?.runCoTest()
						}
						else if(testType == "smoke") {
							testText = "Smoke 'Detected'"
							dev?.runSmokeTest()
						}
						else if(testType == "battery") {
							testText = "Battery 'Replace'"
							dev?.runBatteryTest()
						}
						def mystr = "Sending ${testText} Event to '$dev'"
						LogAction("${mystr}", "info", true)
						paragraph "${mystr}", state: "complete"
					} else {
						paragraph "Skipping; Test is already Running", required: true, state: null
					}
				}
			}
		}
	}
}

def getChildStateKeys(type) {
	def data = []
	def objs
	switch (type) {
		case "device":
			objs = app.getChildDevices(true)
			break
		case "childapp":
			objs = getAllChildApps()
			break
		default:
			objs = app
			break
	}
	if(objs) {
		objs?.each { obj ->
			def	items = obj?.getState().findAll { it }
			items?.each { item ->
				if(!data?.contains(item?.key.toString())) {
					data?.push(item?.key.toString())
				}
			}
		}
	}
	data = data?.sort()
	//log.debug "data: $data"
	return data
}

def buildDevInputMap() {
	def devMap = [:]
	def devices = app.getChildDevices(true)
	devices?.each {
		devMap[[it?.deviceNetworkId].join('.')] = it?.label
	}
	return devMap
}

def buildChildAppInputMap() {
	def appMap = [:]
	getAllChildApps()?.each {
		appMap[[it?.id]?.join('.')] = it?.getLabel()
	}
	return appMap
}

def feedbackPage() {
	def fbData = atomicState?.lastFeedbackData
	def fbNoDup = (settings?.feedbackMsg && fbData?.lastMsg == settings?.feedbackMsg) ? false : true
	def fbLenOk = (settings?.feedbackMsg && settings?.feedbackMsg?.toString().length() > 20) ? true : false
	def msgOk = (settings?.feedbackMsg && fbLenOk && fbNoDup) ? true : false
	//log.debug "msgOk: ($msgOk) | [fbNoDup: $fbNoDup, fbLenOk: $fbLenOk]"
	dynamicPage(name: "feedbackPage", install: false, nextPage: (msgOk ? "sendFeedbackPage" : ""), uninstall: false) {
		section {
			paragraph "Submit feedback to the developer"
			input "feedbackMsg", "text", title: "Enter Feedback here", submitOnChange: true, defaultValue: null
			if (settings?.feedbackMsg != null || settings?.feedbackMsg != "") {
				if (!fbLenOk) {
					paragraph "The current feedback message is too short.\n\n(minimum 20 Char.)", required: true, state: null
				}
				if (!fbNoDup) {
					paragraph "You've already sent the same feedback on (${fbData?.lastMsgDt}).\n\nPlease edit before trying again", required: true, state: null
				}
			}
		}
		if (fbData) {
			section {
				paragraph "You Last Sent Feedback on:\n${fbData?.lastMsgDt}\nFor App Version: ${fbData?.lastAppVer}"
			}
		}
		if (msgOk) {
			section {
				paragraph "Tap Next to Send", state: "complete"
			}
		}
	}
}

def sendFeedbackPage() {
	dynamicPage(name: "sendFeedbackPage", install: false, nextPage: "mainPage", uninstall: false) {
		def fbData = atomicState?.lastFeedbackData
		section("Feedback Status:") {
			if (settings?.feedbackMsg) {
				def ok2send = true
				if (fbData) {
					if (fbData?.lastMsg == settings?.feedbackMsg) {
						ok2send = false
						paragraph "SKIPPING\nYou've already sent the same feedback on (${fbData?.lastMsgDt}).\n\nPlease go back and edit before trying again", required: true, state: null
					}
				}
				if(ok2send) {
					atomicState?.feedbackPending = true
					runIn(5, "sendFeedbackData", [overwrite: true])
					paragraph "Feedback submitted", title: "Thank You", state: "complete"
				}
			} else {
				paragraph "Feedback text is missing from the previous page", required: true, state: null
			}
		}
	}
}

def procDiagCmd() {
	def status = [:]
	def rData = request?.JSON
	// log.trace "procDiagCmd($rData)"
	if(rData) {
		status["code"] = 200
		if(rData?.cmd) {
			switch(rData?.cmd) {
				case "stateCleanup":
					LogAction("Web Diagnostic Command (${rData?.cmd} received... Running stateCleanup() in 3 seconds", "debug", true)
					runIn(3, "stateCleanup", [overwrite: true])
					break
				case "runUpdated":
					LogAction("Web Diagnostic Command (${rData?.cmd} received... Running update() Method in 3 seconds", "debug", true)
					runIn(3, "updated", [overwrite: true])
					break
				case "sendFirebaseData":
					LogAction("Web Diagnostic Command (${rData?.cmd} received... Running sendInstallData() Method in 3 seconds", "debug", true)
					runIn(3, "sendInstallData", [overwrite: true])
					break
			}
		}
	}
	return [contentType: 'application/json', gotData: (rData != null), status: status?.code]
}

def getDeviceMetricCnts() {
	def data = [:]
	def devs = app.getChildDevices(true)
	if(devs?.size() >= 1) {
		try {
			devs?.each { dev ->
				def mData = dev?.getMetricCntData()
				if(mData != null) {
					//log.debug "mData: ${mData}"
					mData?.each { md ->
						def objKey = md?.key.toString()
						def objVal = md?.value?.toInteger() ?: 0
						if(data?.containsKey("${objKey}")) {
							def newVal = 0
							def prevVal = data?.get("${objKey}") ?: 0
							newVal = prevVal?.toInteger()+objVal
							//log.debug "$objKey Data: [prevVal: $prevVal | objVal: $objVal | newVal: $newVal]"
							data << ["${objKey}":newVal]
						} else {
							data << ["${objKey}":objVal]
						}
					}
				}
			}
		} catch (ex) {
			log.error "getDeviceMetricCnts", ex
			sendExceptionData(ex, "getDeviceMetricCnts")
		}
	}
	//log.debug "data: ${data}"
	return data
}
/******************************************************************************
*					Firebase Analytics Functions			*
*******************************************************************************/
def createInstallDataJson(returnMap=false) {
	try {
		generateInstallId()
		def autoDesc = getInstAutoTypesDesc()			// This is a hack to get installedAutomations data updated without waiting for user to hit done
		def tsVer = !atomicState?.swVer?.tDevVer ? "Not Installed" : atomicState?.swVer?.tDevVer
		def ptVer = !atomicState?.swVer?.pDevVer ? "Not Installed" : atomicState?.swVer?.pDevVer
		def cdVer = !atomicState?.swVer?.camDevVer ? "Not Installed" : atomicState?.swVer?.camDevVer
		def pdVer = !atomicState?.swVer?.presDevVer ? "Not Installed" : atomicState?.swVer?.presDevVer
		def wdVer = !atomicState?.swVer?.weatDevVer ? "Not Installed" : atomicState?.swVer?.weatDevVer
		def vtsVer = !atomicState?.swVer?.vtDevVer ? "Not Installed" : atomicState?.swVer?.vtDevVer
		def autoVer = !atomicState?.swVer?.autoSaVer ? "Not Installed" : atomicState?.swVer?.autoSaVer
		def restVer = !atomicState?.swVer?.streamDevVer ? "Not Installed" : atomicState?.swVer?.streamDevVer

		def versions = [
			"apps":["manager":appVersion()?.toString(), "automation":autoVer, "service":restVer],
			"devices":["thermostat":tsVer, "vthermostat":vtsVer, "protect":ptVer, "camera":cdVer, "presence":pdVer, "weather":wdVer]
		]

		def tstatCnt = atomicState?.thermostats?.size() ?: 0
		def protCnt = atomicState?.protects?.size() ?: 0
		def camCnt = atomicState?.cameras?.size() ?: 0
		def vstatCnt = atomicState?.vThermostats?.size() ?: 0

		def automations = !atomicState?.installedAutomations ? "No Automations Installed" : atomicState?.installedAutomations

		def tz = getTimeZone()?.ID?.toString()
		def apiCmdCnt = !atomicState?.apiCommandCnt ? 0 : atomicState?.apiCommandCnt
		def apiStrReqCnt = !atomicState?.apiStrReqCnt ? 0 : atomicState?.apiStrReqCnt
		def apiDevReqCnt = !atomicState?.apiDevReqCnt ? 0 : atomicState?.apiDevReqCnt
		def apiMetaReqCnt = !atomicState?.apiMetaReqCnt ? 0 : atomicState?.apiMetaReqCnt
		def apiRestStrEvtCnt = !atomicState?.apiRestStrEvtCnt ? 0 : atomicState?.apiRestStrEvtCnt
		def appNotifSentCnt = !atomicState?.appNotifSentCnt ? 0 : atomicState?.appNotifSentCnt
		def cltType = !settings?.mobileClientType ? "Not Configured" : settings?.mobileClientType?.toString()
		def appErrCnt = !atomicState?.appExceptionCnt ? 0 : atomicState?.appExceptionCnt
		def devErrCnt = !atomicState?.childExceptionCnt ? 0 : atomicState?.childExceptionCnt
		def devUseMetCnt = getDeviceMetricCnts()
		def appUseMetCnt = atomicState?.usageMetricsStore
		def data = []
		if(settings?.optInAppAnalytics || settings?.optInAppAnalytics == null) {
			data =	[
				"guid":atomicState?.installationId, "beta":betaMarker(), "versions":versions, "thermostats":tstatCnt, "protects":protCnt, "vthermostats":vstatCnt, "cameras":camCnt, "appErrorCnt":appErrCnt, "devErrorCnt":devErrCnt,
				"installDt": atomicState?.installData?.dt, "updatedDt": atomicState?.installData?.updatedDt, "automations":automations, "timeZone":tz, "apiCmdCnt":apiCmdCnt, "apiStrReqCnt":apiStrReqCnt, "apiDevReqCnt":apiDevReqCnt,
				"apiMetaReqCnt":apiMetaReqCnt, "appNotifSentCnt":appNotifSentCnt, "apiRestStrEvtCnt":apiRestStrEvtCnt, "appUseMetCnt":appUseMetCnt, "devUseMetCnt":devUseMetCnt,"stateUsage":"${getStateSizePerc()}%", "mobileClient":cltType,
				"liteAppMode": isAppLiteMode(), "datetime":getDtNow()?.toString(), "optOut":false
			]
		} else {
			data = [
				"guid":atomicState?.installationId, "beta":betaMarker(), "versions":versions, "thermostats":tstatCnt, "protects":protCnt, "vthermostats":vstatCnt, "cameras":camCnt, "appErrorCnt":appErrCnt, "devErrorCnt":devErrCnt,
				"apiStrReqCnt":apiStrReqCnt, "apiDevReqCnt":apiDevReqCnt, "apiMetaReqCnt":apiMetaReqCnt, "installDt": atomicState?.installData?.dt, "updatedDt": atomicState?.installData?.updatedDt,"automations":automations,
				"liteAppMode": isAppLiteMode(), "timeZone":tz, "apiCmdCnt":apiCmdCnt, "apiRestStrEvtCnt":apiRestStrEvtCnt, "stateUsage":"${getStateSizePerc()}%", "datetime":getDtNow()?.toString(), "optOut":true
			]
		}
		if(returnMap == true) {
			return data
		} else {
			def resultJson = new groovy.json.JsonOutput().toJson(data)
			return resultJson
		}
	} catch (ex) {
		log.error "createInstallDataJson: Exception:", ex
		sendExceptionData(ex, "createInstallDataJson")
	}
}

def renderInstallData() {
	try {
		def resultJson = createInstallDataJson()
		def resultString = new groovy.json.JsonOutput().prettyPrint(resultJson)
		render contentType: "application/json", data: resultString
	} catch (ex) { log.error "renderInstallData Exception:", ex }
}

def renderLogData() {
	try {
		def remDiagApp = getRemDiagApp()
		def resultStr = "There are no logs to show... Is logging turned on?"

		def logData = remDiagApp?.getRemLogData()
		if(logData) {
			resultStr = logData
		}
		render contentType: "text/html", data: resultStr
	} catch (ex) { log.error "renderLogData Exception:", ex }
}

def getDiagHomeUrl() { getAppEndpointUrl("diagHome") }

def getLogMap() {
	try {
		def remDiagApp = getRemDiagApp()
		def resultJson = new groovy.json.JsonOutput().toJson(remDiagApp?.getStateVal("remDiagLogDataStore"))
		render contentType: "application/json", data: resultJson
	} catch (ex) { log.error "getLogMap Exception:", ex }
}

def getSetData() {
	def res = null
	def par = params?.value
	log.debug "getSetData param: $par"
 	res = par ? getSettingVal("$par") : getSettings()
	def resultJson = new groovy.json.JsonOutput().toJson(res)
	render contentType: "application/json", data: resultJson
}

def getStateData() {
	def res = null
	def par = params.value
	log.debug "getStateData param: $par"
 // 	res = par ? getStateVal("$par") : getState()
	def resultJson = new groovy.json.JsonOutput().toJson(res)
	render contentType: "application/json", data: resultJson
}

def lastCmdDesc() {
	def cmdDesc = ""
	def map = [:]
	map["DateTime"] = getTimestampVal("lastCmdSentDt") ?: "Nothing found"
	map["Cmd Sent"] = atomicState?.lastCmdSent ?: "Nothing found"
	map["Cmd Result"] = atomicState?.lastCmdSentStatus ? "(${atomicState?.lastCmdSentStatus})" : "(Nothing found)"
	cmdDesc += getMapDescStr(map)
	return cmdDesc
}

def getWebHeaderHtml(title, clipboard=true, vex=false, swiper=false, charts=false) {
	def html = """
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
		<meta name="description" content="NST Diagnostics">
		<meta name="author" content="Anthony S.">
		<meta http-equiv="cleartype" content="on">
		<meta name="MobileOptimized" content="320">
		<meta name="HandheldFriendly" content="True">
		<meta name="apple-mobile-web-app-capable" content="yes">

		<title>NST Diagnostics (${atomicState?.structName}) - ${title}</title>

		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
		<link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet">
		<script src="https://use.fontawesome.com/fbe6a4efc7.js"></script>
		<script src="https://fastcdn.org/FlowType.JS/1.1/flowtype.js"></script>
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/7.0.0/normalize.min.css">
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/hamburgers/0.9.1/hamburgers.min.css">
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
		<script type="text/javascript">
			const serverUrl = '${apiServerUrl('')}';
			const cmdUrl = '${getAppEndpointUrl('processCmd')}';
		</script>
	"""
	html += clipboard ? """<script src="https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/1.7.1/clipboard.min.js"></script>""" : ""
	html += vex ? """<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.0/js/vex.combined.min.js"></script>""" : ""
	html += swiper ? """<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/Swiper/4.3.3/css/swiper.min.css" />""" : ""
	html += vex ? """<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.0/css/vex.min.css" />""" : ""
	html += vex ? """<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/vex-js/3.1.0/css/vex-theme-top.min.css" />""" : ""
	html += swiper ? """<script src="https://cdnjs.cloudflare.com/ajax/libs/Swiper/4.3.3/js/swiper.min.js"></script>""" : ""
	html += charts ? """<script src="https://www.gstatic.com/charts/loader.js"></script>""" : ""
	html += vex ? """<script>vex.defaultOptions.className = 'vex-theme-default'</script>""" : ""

	return html
}

def renderDiagHome() {
	try {
		def remDiagUrl = getAppEndpointUrl("diagHome")
		def logUrl = getAppEndpointUrl("getLogData")
		def managerUrl = getAppEndpointUrl("getManagerData")
		def autoUrl = getAppEndpointUrl("getAutoData")
		def deviceUrl = getAppEndpointUrl("getDeviceData")
		def appDataUrl = getAppEndpointUrl("getAppData")
		def instDataUrl = getAppEndpointUrl("getInstData")
		def devTilesUrl = getAppEndpointUrl("deviceTiles")
		def tstatTilesUrl = getAppEndpointUrl("tstatTiles")
		def protTilesUrl = getAppEndpointUrl("protectTiles")
		def camTilesUrl = getAppEndpointUrl("cameraTiles")
		def weatherTilesUrl = getAppEndpointUrl("weatherTile")
		def sPerc = getStateSizePerc() ?: 0
		def instData = atomicState?.installData
		def cmdDesc = lastCmdDesc().toString().replaceAll("\n", "<br>")
		//def newHtml = getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/Documents/html/diagHome.html", contentType: "text/plain; charset=UTF-8"], "newHtml").toString()
		//log.debug "newHtml: $newHtml"
		def html = """
			<head>
				${getWebHeaderHtml("Location")}
				<link rel="stylesheet" href="https://cdn.rawgit.com/toubou91/percircle/master/dist/css/percircle.css">
				<script src="https://cdn.rawgit.com/toubou91/percircle/master/dist/js/percircle.js"></script>
				<link rel="stylesheet" href="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/css/diaghome.min.css">
				<style>
				</style>
			</head>
			<body>
				<button onclick="topFunction()" id="scrollTopBtn" title="Go to top"><i class="fa fa-arrow-up centerText" aria-hidden="true"></i></button>

				<!-- Your Content -->
				<div id="container">
					<div id="top-hdr" class="navbar navbar-default navbar-fixed-top">
						<div class="centerText">
							<div class="row">
								<div class="col-xs-2"></div>
								<div class="col-xs-8 centerText">
									<h4 class="title-text"><img class="logoIcn" src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/nst_manager_5.png"> Diagnostics Home</img></h4>
								</div>
								<div class="col-xs-2 right-head-col pull-right">
									<button id="rfrshBtn" type="button" class="btn refresh-btn pull-right" title="Refresh Page Content"><i id="rfrshBtnIcn" class="fa fa-refresh" aria-hidden="true"></i></button>
								</div>
							</div>
						</div>
					</div>

					<!-- Page Content -->
					<div id="page-content-wrapper">
						<div class="container">
						 	<!--First Panel Section -->
						 	<div class="panel panel-primary">
								<!--First Panel Section Heading-->
								<div class="panel-heading">
									<div class="row">
										<div class="col-xs-12">
											<h1 class="panel-title panel-title-text">Install Details:</h1>
										</div>
									</div>
								</div>

								<!--First Panel Section Body -->
								<div class="panel-body" style="overflow-y: auto;">
									<div class="container-fluid">
										<!--First Panel Section Body Row 1-->
										<div class="row" style="min-height: 100px;">

											<!--First Panel Section Body Row 1 - Col1 -->
											<div class=" col-xs-12 col-sm-8">
												<div id="instContDiv" style="padding: 0 10px;">
													<div class="row panel-border centerText">
														<div class="col-xs-12 col-sm-6 install-content">
															<span><b>Version:</b></br><small>${appVersion()}</small></span>
														</div>
														<div class="col-xs-12 col-sm-6 install-content">
															<span><b>Install ID:</b></br><small>${atomicState?.installationId}</small></span>
														</div>

													<div class="col-xs-12 col-sm-6 install-content">
														<span><b>Install Date:</b></br><small>${instData?.dt}</small></span>
													</div>
													<div class="col-xs-12 col-sm-6 install-content">
														<span><b>Last Updated:</b></br><small>${instData?.updatedDt}</small></span>
													</div>
													<div class="col-xs-12 col-sm-6 install-content">
														<span><b>Init. Version:</b></br><small>${instData?.initVer}</small></span>
													</div>
													<div class="col-xs-12 col-sm-6 install-content">
														<span><b>Fresh Install:</b></br><small>${instData?.freshInstall}</small></span>
													</div>
												</div>
											</div>
										</div>
										<!--First Panel Section Body Row 1 - Col2 -->
										<div class="col-xs-12 col-sm-4" style="padding: 25px;">
									 			<div style="pull-right">
													<div class="stateUseTitleText">State Usage</div>
													<div id="stateUseCirc" data-percent="${sPerc}" data-text="<p class='stateUseCircText'>${sPerc}%</p>" class="small blue2 center"></div>
												</div>
											</div>
										</div>
										<hr/>
										<!--First Panel Section Body Row 2 -->
										<div class="row" style="min-height: 100px;">
											<!--First Panel Section Body Row 2 - Col 1 -->
											<div id="instContDiv" style="padding: 0 10px;">
												<div class="panel panel-default">
													<div id="item${appNum}-settings" class="panel-heading">
														<h1 class="panel-title subpanel-title-text">Last Command Info:</h1>
													</div>
													<div class="panel-body">
														<div><pre class="mapDataFmt">${lastCmdDesc().toString().replaceAll("\n", "<br>")}</pre></div>
													</div>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>

							<!--Second Panel Section -->
							<div class="panel panel-info">
					 			<div class="panel-heading">
									<h1 class="panel-title">Shortcuts</h1>
					 			</div>
					 			<div class="panel-body">
									<div class="col-xs-6 centerText">
								 		<p><a class="btn btn-primary btn-md shortcutBtns" href="${logUrl}" role="button">View Logs</a></p>
									 	<p><a class="btn btn-primary btn-md shortcutBtns" href="${managerUrl}" role="button">Manager Data</a></p>
									 	<p><a class="btn btn-primary btn-md shortcutBtns" href="${autoUrl}" role="button">Automation Data</a></p>
									</div>
									<div class="col-xs-6 centerText">
									 	<p><a class="btn btn-primary btn-md shortcutBtns" href="${deviceUrl}" role="button">Device Data</a></p>
										<p><a class="btn btn-primary btn-md shortcutBtns" href="${instDataUrl}" role="button">Install Data</a></p>
										<p><a class="btn btn-primary btn-md shortcutBtns" href="${appDataUrl}" role="button">AppData File</a></p>
									</div>
								</div>
						 	</div>
							<!--Third Panel Section -->
							<div class="panel panel-warning">
					 			<div class="panel-heading">
									<h1 class="panel-title">Diagnostic Commands</h1>
					 			</div>
					 			<div class="panel-body">
									<div class="col-xs-6 centerText">
										<p><a class="btn btn-primary btn-md shortcutBtns" id="updateMethodBtn" role="button">Run Update()</a></p>
										<p><a class="btn btn-primary btn-md shortcutBtns" id="stateCleanupBtn" role="button">Run StateCleanup()</a></p>
										<p><a class="btn btn-primary btn-md shortcutBtns" id="sendInstallDataBtn" role="button">Run SendInstallData()</a></p>
									</div>
								</div>
						 	</div>
							<!--Fourth Panel Section -->
							<div class="panel panel-success">
					 			<div class="panel-heading">
									<h1 class="panel-title">Device Tiles</h1>
					 			</div>
					 			<div class="panel-body">
									<div class="col-xs-6 centerText">
									 	<p><a class="btn btn-primary btn-md shortcutBtns" href="${devTilesUrl}" role="button">All Devices</a></p>
										${atomicState?.thermostats ? """<p><a class="btn btn-primary btn-md shortcutBtns" href="${tstatTilesUrl}" role="button">Thermostat Devices</a></p>""" : ""}
										${atomicState?.protects ? """<p><a class="btn btn-primary btn-md shortcutBtns" href="${protTilesUrl}" role="button">Protect Devices</a></p>""" : ""}
									</div>
									<div class="col-xs-6 centerText">
										${atomicState?.cameras ? """<p><a class="btn btn-primary btn-md shortcutBtns" href="${camTilesUrl}" role="button">Camera Devices</a></p>""" : ""}
										${atomicState?.weatherDevice ? """<p><a class="btn btn-primary btn-md shortcutBtns" href="${weatherTilesUrl}" role="button">Weather Device</a></p>""" : ""}
									</div>
								</div>
						 	</div>
							<footer class="footer">
								<div class="container">
				 					<div class="well well-sm footerText">
				 						<span>External Access URL: <button id="copyUrlBtn" class="btn" title="Copy URL to Clipboard" type="button" data-clipboard-action="copy" data-clipboard-text="${remDiagUrl}"><i class="fa fa-clipboard" aria-hidden="true"></i></button></span>
									</div>
								</div>
							</footer>
						</div>
					</div>
				</div>
				<script src="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/js/diaghome.min.js"></script>
			</body>
		"""
/* """ */
		render contentType: "text/html", data: html
	} catch (ex) { log.error "renderDiagUrl Exception:", ex }
}

def dumpListDesc(data, level, List lastLevel, listLabel, html=false) {
	def str = ""
	def cnt = 1
	def newLevel = lastLevel

	def list1 = data?.collect {it}
	list1?.each { par ->
		def t0 = cnt - 1
		if(par instanceof Map) {
			def newmap = [:]
			newmap["${listLabel}[${t0}]"] = par
			def t1 = (cnt == list1.size()) ? true : false
			newLevel[level] = t1
			str += dumpMapDesc(newmap, level, newLevel, !t1)
		} else if(par instanceof List || par instanceof ArrayList) {
			def newmap = [:]
			newmap["${listLabel}[${t0}]"] = par
			def t1 = (cnt == list1.size()) ? true : false
			newLevel[level] = t1
			str += dumpMapDesc(newmap, level, newLevel, !t1)
		} else {
			def lineStrt = "\n"
			for(int i=0; i < level; i++) {
				lineStrt += (i+1 < level) ? (!lastLevel[i] ? "   │" : "    " ) : "   "
			}
			lineStrt += (cnt == 1 && list1.size() > 1) ? "┌── " : (cnt < list1?.size() ? "├── " : "└── ")
			str += "${lineStrt}${listLabel}[${t0}]: ${par} (${getObjType(par)})"
		}
		cnt = cnt+1
	}
	return str
}

def dumpMapDesc(data, level, List lastLevel, listCall=false, html=false) {
	def str = ""
	def cnt = 1
	data?.sort()?.each { par ->
		def lineStrt = ""
		def newLevel = lastLevel
		def thisIsLast = (cnt == data?.size() && !listCall) ? true : false
		if(level > 0) {
			newLevel[(level-1)] = thisIsLast
		}
		def theLast = thisIsLast
		if(level == 0) {
			lineStrt = "\n\n • "
		} else {
			theLast == (last && thisIsLast) ? true : false
			lineStrt = "\n"
			for(int i=0; i < level; i++) {
				lineStrt += (i+1 < level) ? (!newLevel[i] ? "   │" : "    " ) : "   "
			}
			lineStrt += ((cnt < data?.size() || listCall) && !thisIsLast) ? "├── " : "└── "
		}
		if(par?.value instanceof Map) {
			str += "${lineStrt}${par?.key.toString()}: (Map)"
			newLevel[(level+1)] = theLast
			str += dumpMapDesc(par?.value, level+1, newLevel)
		}
		else if(par?.value instanceof List || par?.value instanceof ArrayList) {
			str += "${lineStrt}${par?.key.toString()}: [List]"
			newLevel[(level+1)] = theLast

			str += dumpListDesc(par?.value, level+1, newLevel, par?.key.toString())
		}
		else {
			def objType = getObjType(par?.value)
			if(html) {
				def cls = mapDescValHtmlCls(par?.value)
				str += "<span>${lineStrt}${par?.key.toString()}: (${par?.value}) (${objType})</span>"
			} else {
				str += "${lineStrt}${par?.key.toString()}: (${par?.value}) (${objType})"
			}
		}
		cnt = cnt + 1
	}
	return str
}

def mapDescValHtmlCls(value) {
	if(!value) { return "" }
}

def preSymObj() { [1:"•", 2:"│", 3:"├", 4:"└", 5:"    ", 6:"┌", 7:"├──", 8:"└── "] }

def getMapDescStr(data) {
	def str = ""
	def lastLevel = [true]
	str = dumpMapDesc(data, 0, lastLevel)
	//log.debug "str: $str"
	return str != "" ? str : "No Data was returned"
}

def renderManagerData() {
	try {
		def appHtml = ""
		def navHtml = ""
		def scrStr = ""
		def appNum = 1
		def setDesc = getMapDescStr(getSettings())
		def noShow = ["authToken", "accessToken", "cssData"]
		def stData = getState()?.sort()?.findAll { !(it.key in noShow) }
		def stateData = [:]
		stData?.sort().each { item ->
			stateData[item?.key] = item?.value
		}

		def navMap = [:]
		navMap = ["key":app?.getLabel(), "items":["Settings", "State", "MetaData"]]
		def navItems = navHtmlBuilder(navMap, appNum)
		if(navItems?.html) { navHtml += navItems?.html }
		if(navItems?.js) { scrStr += navItems?.js }
		def stateDesc = getMapDescStr(stateData)
		def metaDesc = getMapDescStr(getMetadata())
		def html = """
			<head>
				${getWebHeaderHtml("Manager Data")}
				<link rel="stylesheet" href="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/css/diagpages.min.css">
				<style>
					.pushy {
					    position: fixed;
					    width: 250px;
					    height: 100%;
					    top: 0;
					    z-index: 9999;
					    background: #191918;
					    opacity: 0.6;
					    overflow: auto;
					    -webkit-overflow-scrolling: touch;
					    /* enables momentum scrolling in iOS overflow elements */
					}
					.nav-home-btn {
					    padding: 20px 10px 0 10px;
					    font-size: 22px;
					    -webkit-text-stroke: white;
					    -webkit-text-stroke-width: thin;
					}
				</style>
			</head>
			<body>
				<button onclick="topFunction()" id="scrollTopBtn" title="Go to top"><i class="fa fa-arrow-up centerText" aria-hidden="true"></i></button>
				<nav id="menu-page" class="pushy pushy-left" data-focus="#nav-key-item1">
					<div class="nav-home-btn centerText"><button id="goHomeBtn" class="btn-link" title="Go Back to Home Page"><i class="fa fa-home centerText" aria-hidden="true"></i> Go Home</button></div>
					<!--Include your navigation here-->
					${navHtml}
				</nav>
				<!-- Site Overlay -->
				<div class="site-overlay"></div>

 				<!-- Your Content -->
				<div id="container">
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
									<h3 class="title-text"><img class="logoIcn" src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/nst_manager_5.png"> Manager Data</img></h3>
						 		</div>
						 		<div class="col-xs-2 right-head-col pull-right">
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
								<div id="key-item1" class="panel panel-primary">
									<div class="panel-heading">
							 			<div class="row">
										<div class="col-xs-10">
							 				<h1 class="panel-title panel-title-text">NST Manager:</h1>
										</div>
										<div class="col-xs-2" style="padding: 10px;">
							 				<button id="exportPdfBtn" type="button" title="Export Content as PDF" class="btn export-pdf-btn pull-right"><i id="exportPdfBtnIcn" class="fa fa-file-pdf-o" aria-hidden="true"></i> PDF</button>
										</div>
							 			</div>
									</div>

									<div class="panel-body">
										<div>
											<div class="panel panel-default">
										 		<div id="item${appNum}-settings" class="panel-heading">
													<h1 class="panel-title subpanel-title-text">Setting Data:</h1>
										 		</div>
										 		<div class="panel-body">
													<div><pre class="pre-scroll mapDataFmt">${setDesc.toString().replaceAll("\n", "<br>")}</pre></div>
										 		</div>
											</div>

											<div class="panel panel-default">
										 		<div id="item${appNum}-state" class="panel-heading">
													<h1 class="panel-title subpanel-title-text">State Data:</h1>
										 		</div>
										 		<div class="panel-body">
													<div><pre class="pre-scroll mapDataFmt">${stateDesc.toString().replaceAll("\n", "<br>")}</pre></div>
										 		</div>
											</div>

											<div class="panel panel-default">
										 		<div id="item${appNum}-metadata" class="panel-heading">
													<h1 class="panel-title subpanel-title-text">Meta Data:</h1>
										 		</div>
										 		<div class="panel-body">
													<div><pre class="pre-scroll mapDataFmt">${metaDesc.toString().replaceAll("\n", "<br>")}</pre></div>
										 		</div>
										 	</div>
										</div>
									</div>

								</div>
							</div>
				 		</div>
					</div>
				</div>
				<script>
					\$("body").flowtype({
						minFont: 7,
						maxFont: 10,
						fontRatio: 30
					});
				</script>
 			 	<script src="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/js/diagpages.min.js"></script>
 			 	<script>
					\$(document).ready(function() {
						${scrStr}
					});
					\$("#goHomeBtn").click(function() {
					    closeNavMenu();
					    toggleMenuBtn();
					    window.location.replace('${getAppEndpointUrl("diagHome")}');
					});
 			   	</script>
			</body>
		"""
/* """ */
		render contentType: "text/html", data: html
	} catch (ex) { log.error "renderManagerData Exception:", ex }
}

def renderAutomationData() {
	try {
		def appHtml = ""
		def navHtml = ""
		def scrStr = ""
		def appNum = 1
		getAllChildApps()?.sort {it?.getLabel()}?.each { cApp ->
			def navMap = [:]
			navMap = ["key":cApp?.getLabel(), "items":["Settings", "State", "MetaData"]]
			def navItems = navHtmlBuilder(navMap, appNum)
			if(navItems?.html) { navHtml += navItems?.html }
			if(navItems?.js) { scrStr += navItems?.js }
			def setDesc = getMapDescStr(cApp?.getSettings())
			def stateDesc = getMapDescStr(cApp?.getState()?.findAll { !(it?.key in ["remDiagLogDataStore", "cssData"]) })
			def metaDesc = getMapDescStr(cApp?.getMetadata())
			appHtml += """
			<div class="panel panel-primary">
			 	<div id="key-item${appNum}" class="panel-heading">
					<h1 class="panel-title panel-title-text">${cApp?.getLabel()}:</h1>
			 	</div>
		 		<div class="panel-body">
					<div>
						<div class="panel panel-default">
						 	<div id="item${appNum}-settings" class="panel-heading">
								<h1 class="panel-title subpanel-title-text">Setting Data:</h1>
						 	</div>
					 		<div class="panel-body">
								<div><pre class="mapDataFmt">${setDesc.toString().replaceAll("\n", "<br>")}</pre></div>
					 		</div>
						</div>

						<div class="panel panel-default">
					 		<div id="item${appNum}-state" class="panel-heading">
								<h1 class="panel-title subpanel-title-text">State Data:</h1>
					 		</div>
					 		<div class="panel-body">
								<div><pre class="mapDataFmt">${stateDesc.toString().replaceAll("\n", "<br>")}</pre></div>
					 		</div>
						</div>

						<div class="panel panel-default">
					 		<div id="item${appNum}-metadata" class="panel-heading">
								<h1 class="panel-title subpanel-title-text">Meta Data:</h1>
					 		</div>
					 		<div class="panel-body">
								<div><pre class="mapDataFmt">${metaDesc.toString().replaceAll("\n", "<br>")}</pre></div>
					 		</div>
					 	</div>
					</div>
			 	</div>
			</div>
			"""
			appNum = appNum+1
		}
		def html = """
			<head>
				${getWebHeaderHtml("Automation Data")}
				<link rel="stylesheet" href="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/css/diagpages.min.css">
				<style>
				</style>
			</head>
			<body>
				<button onclick="topFunction()" id="scrollTopBtn" title="Go to top"><i class="fa fa-arrow-up centerText" aria-hidden="true"></i></button>
				<nav id="menu-page" class="pushy pushy-left" data-focus="#nav-key-item1">
					<div class="nav-home-btn centerText"><button id="goHomeBtn" class="btn-link" title="Go Back to Home Page"><i class="fa fa-home centerText" aria-hidden="true"></i> Go Home</button></div>
					<!--Include your navigation here-->
					${navHtml}
				</nav>
				<!-- Site Overlay -->
				<div class="site-overlay"></div>

				<!-- Your Content -->
				<div id="container">
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
									<h3 class="title-text"><img class="logoIcn" src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/nst_manager_5.png"> Automation Data</img></h3>
								</div>
								<div class="col-xs-2 right-head-col pull-right">
									<button id="rfrshBtn" type="button" class="btn refresh-btn pull-right" title="Refresh Page Content"><i id="rfrshBtnIcn" class="fa fa-refresh" aria-hidden="true"></i></button>
								</div>
							</div>
						</div>
					</div>
					<!-- Page Content -->
				 	<div id="page-content-wrapper">
						<div class="container">
							<div id="main" class="panel-body">
								${appHtml}
							</div>
						</div>
					</div>
				</div>
				<script>
					\$("body").flowtype({
						minFont: 7,
						maxFont: 10,
						fontRatio: 30
					});
				</script>
 				<script src="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/js/diagpages.min.js"></script>
 				<script>
					\$(document).ready(function() {
						${scrStr}
					});
					\$("#goHomeBtn").click(function() {
					    closeNavMenu();
					    toggleMenuBtn();
					    window.location.replace('${getAppEndpointUrl("diagHome")}');
					});
				</script>
		</body>
		"""
	/* """ */
		render contentType: "text/html", data: html
	} catch (ex) { log.error "renderAutomationData Exception:", ex }
}

def navHtmlBuilder(navMap, idNum) {
	def res = [:]
	def htmlStr = ""
	def jsStr = ""
	if(navMap?.key) {
		htmlStr += """
			<div class="nav-cont-bord-div nav-menu">
			  <div class="nav-cont-div">
				<li class="nav-key-item"><a id="nav-key-item${idNum}">${navMap?.key}<span class="icon"></span></a></li>"""
		jsStr += navJsBuilder("nav-key-item${idNum}", "key-item${idNum}")
	}
	if(navMap?.items) {
		def nItems = navMap?.items
		nItems?.each {
			htmlStr += """\n<li class="nav-subkey-item"><a id="nav-subitem${idNum}-${it?.toString().toLowerCase()}">${it}<span class="icon"></span></a></li>"""
			jsStr += navJsBuilder("nav-subitem${idNum}-${it?.toString().toLowerCase()}", "item${idNum}-${it?.toString().toLowerCase()}")
		}
	}
	htmlStr += """\n		</div>
						</div>"""
	res["html"] = htmlStr
	res["js"] = jsStr
	return res
}

def navJsBuilder(btnId, divId) {
	def res = """
			\$("#${btnId}").click(function() {
				\$("html, body").animate({scrollTop: \$("#${divId}").offset().top - hdrHeight - 20},500);
				closeNavMenu();
				toggleMenuBtn();
			});
	"""
	return "\n${res}"
}

def renderDeviceData() {
	try {
		def devHtml = ""
		def navHtml = ""
		def scrStr = ""
		def devices = app.getChildDevices(true)
		def devNum = 1
		devices?.sort {it?.getLabel()}.each { dev ->
			def navMap = [:]
			navMap = ["key":dev?.getLabel(), "items":["Settings", "State", "Attributes", "Commands", "Capabilities"]]
			def navItems = navHtmlBuilder(navMap, devNum)
			if(navItems?.html) { navHtml += navItems?.html }
			if(navItems?.js) { scrStr += navItems?.js }
			def setDesc = getMapDescStr(dev?.getSettings())
			def stateDesc = getMapDescStr(dev?.getState()?.findAll { !(it?.key in ["cssData"]) })

			def attrDesc = ""; def cnt = 1
			def devData = dev?.supportedAttributes.collect { it as String }
			devData?.sort().each {
				attrDesc += "${cnt>1 ? "\n\n" : "\n"} • ${"$it" as String}: (${dev.currentValue("$it")})"
				cnt = cnt+1
			}

			def commDesc = ""; cnt = 1
			dev?.supportedCommands?.sort()?.each { cmd ->
				commDesc += "${cnt>1 ? "\n\n" : "\n"} • ${cmd.name}(${!cmd?.arguments ? "" : cmd?.arguments.toString().toLowerCase().replaceAll("\\[|\\]", "")})"
				cnt = cnt+1
			}
			def data = dev?.capabilities?.sort()?.collect {it as String}
			def t0 = [ "capabilities":data ]
			def capDesc = getMapDescStr(t0)
			devHtml += """
			<div class="panel panel-primary">
			 	<div id="key-item${devNum}" class="panel-heading">
					<h1 class="panel-title panel-title-text">${dev?.getLabel()}:</h1>
			 	</div>
			 	<div class="panel-body">
					<div>
						<div id="item${devNum}-settings" class="panel panel-default">
					 		<div class="panel-heading">
								<h1 class="panel-title subpanel-title-text">Setting Data:</h1>
					 		</div>
					 		<div class="panel-body">
								<div><pre class="mapDataFmt">${setDesc.toString().replaceAll("\n", "<br>")}</pre></div>
					 		</div>
						</div>
						<div id="item${devNum}-state" class="panel panel-default">
					 		<div class="panel-heading">
								<h1 class="panel-title subpanel-title-text">State Data:</h1>
					 		</div>
					 		<div class="panel-body">
								<div><pre class="mapDataFmt">${stateDesc.toString().replaceAll("\n", "<br>")}</pre></div>
					 		</div>
						</div>
						<div id="item${devNum}-attributes" class="panel panel-default">
					 		<div class="panel-heading">
								<h1 class="panel-title subpanel-title-text">Attribute Data:</h1>
					 		</div>
					 		<div class="panel-body">
								<div><pre class="mapDataFmt">${attrDesc.toString().replaceAll("\n", "<br>")}</pre></div>
					 		</div>
						</div>
						<div id="item${devNum}-commands" class="panel panel-default">
							<div class="panel-heading">
								<h1 class="panel-title subpanel-title-text">Command Data:</h1>
							</div>
							<div class="panel-body">
						 		<div><pre class="mapDataFmt">${commDesc.toString().replaceAll("\n", "<br>")}</pre></div>
							</div>
						</div>
						<div id="item${devNum}-capabilities" class="panel panel-default">
					 		<div class="panel-heading">
								<h1 class="panel-title panel-title-text">Capability Data:</h1>
					 		</div>
					 		<div class="panel-body">
								<div><pre class="mapDataFmt">${capDesc.toString().replaceAll("\n", "<br>")}</pre></div>
					 		</div>
						</div>
					</div>
				</div>
			</div>
			"""
			devNum = devNum+1
		}
		def html = """
			<head>
				${getWebHeaderHtml("Device Data", true, true, true, true)}
				<link rel="stylesheet" href="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/css/diagpages_new.css">
				<style>
					h1, h2, h3, h4, h5, h6 {
						padding: 20px;
						margin: 4px;
					}
				</style>
			</head>
			<body>
				<button onclick="topFunction()" id="scrollTopBtn" title="Go to top"><i class="fa fa-arrow-up centerText" aria-hidden="true"></i></button>
				<nav id="menu-page" class="pushy pushy-left" data-focus="#nav-key-item1">
					<div class="nav-home-btn centerText"><button id="goHomeBtn" class="btn-link" title="Go Back to Home Page"><i class="fa fa-home centerText" aria-hidden="true"></i> Go Home</button></div>
					<!--Include your navigation here-->
					${navHtml}
				</nav>
				<!-- Site Overlay -->
				<div class="site-overlay"></div>

				<!-- Your Content -->
				<div id="container">
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
									<h3 class="title-text"><img class="logoIcn" src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/nst_manager_5.png"> Device Data</img></h3>
								</div>
								<div class="col-xs-2 right-head-col pull-right">
									<button id="rfrshBtn" type="button" class="btn refresh-btn pull-right" title="Refresh Page Content"><i id="rfrshBtnIcn" class="fa fa-refresh" aria-hidden="true"></i></button>
								</div>
							</div>
						</div>
					</div>
					<!-- Page Content -->
				 	<div id="page-content-wrapper">
						<div class="container">
							<div id="main" class="panel-body">
								${devHtml}
							</div>
						</div>
	 			   </div>
	 			</div>
				<script>
					\$("body").flowtype({
						minFont: 7,
						maxFont: 10,
						fontRatio: 30
					});
				</script>
				<script src="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/js/diagpages.min.js"></script>
				<script>
					\$(document).ready(function() {
						${scrStr}
					});
					\$("#goHomeBtn").click(function() {
					    closeNavMenu();
					    toggleMenuBtn();
					    window.location.replace('${getAppEndpointUrl("diagHome")}');
					});
				</script>
			</body>
		"""
	/* """ */
		log.debug apiServerUrl("/api/rooms")
		render contentType: "text/html", data: html
	} catch (ex) { log.error "renderDeviceData Exception:", ex }
}

def getTstatTiles() {
	return renderDeviceTiles("Nest Thermostat")
}

def getProtectTiles() {
	return renderDeviceTiles("Nest Protect")
}

def getCamTiles() {
	return renderDeviceTiles("Nest Camera")
}

def getWeatherTile() {
	return renderDeviceTiles("Nest Weather")
}

def renderDeviceTiles(type=null) {
	try {
		def devHtml = ""
		def navHtml = ""
		def scrStr = ""
		def devices = app.getChildDevices(true)
		def devNum = 1
		devices?.sort {it?.getLabel()}.each { dev ->
			def navMap = [:]
			def hasHtml = (dev?.hasHtml() == true)
			if((hasHtml && !type) || (hasHtml && type && dev?.name == type)) {
				navMap = ["key":dev?.getLabel(), "items":[]]
				def navItems = navHtmlBuilder(navMap, devNum)
				if(navItems?.html) { navHtml += navItems?.html }
				if(navItems?.js) { scrStr += navItems?.js }
				devHtml += """
				<div class="panel panel-primary" style="max-width: 600px; margin: 30 auto; position: relative;">
				 	<div id="key-item${devNum}" class="panel-heading">
						<h1 class="panel-title panel-title-text">${dev?.getLabel()}: (v${dev?.devVer()})</h1>
				 	</div>
				 	<div class="panel-body">
						<div style="margin: auto; position: relative;">
							<div>${dev?.getDeviceTile(devNum)}</div>
						</div>
					</div>
				</div>
				"""
			}
			devNum = devNum+1
		}

		def html = """
			<head>
				${getWebHeaderHtml(type, true, true, true, true)}
				<link rel="stylesheet" href="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/css/diagpages_new.css">
				<style>
					h1, h2, h3, h4, h5, h6 {
						padding: 20px;
						margin: 4px;
					}
				</style>
			</head>
			<body>
				<button onclick="topFunction()" id="scrollTopBtn" title="Go to top"><i class="fa fa-arrow-up centerText" aria-hidden="true"></i></button>
				<nav id="menu-page" class="pushy pushy-left" data-focus="#nav-key-item1">
					<div class="nav-home-btn centerText"><button id="goHomeBtn" class="btn-link" title="Go Back to Home Page"><i class="fa fa-home centerText" aria-hidden="true"></i> Go Home</button></div>
					<!--Include your navigation here-->
					${navHtml}
				</nav>
				<!-- Site Overlay -->
				<div class="site-overlay"></div>

				<!-- Your Content -->
				<div id="container">
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
									<h3 class="title-text"><img class="logoIcn" src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/nst_manager_5.png"> ${type ?: "All Device"}s</img></h3>
								</div>
								<div class="col-xs-2 right-head-col pull-right">
									<button id="rfrshBtn" type="button" class="btn refresh-btn pull-right" title="Refresh Page Content"><i id="rfrshBtnIcn" class="fa fa-refresh" aria-hidden="true"></i></button>
								</div>
							</div>
						</div>
					</div>
					<!-- Page Content -->
				 	<div id="page-content-wrapper">
						<div class="container">
							<div id="main" class="panel-body">
								${devHtml}
							</div>
						</div>
	 				</div>
	 			</div>
				<script>
					\$("body").flowtype({
						minFont: 7,
						maxFont: 10,
						fontRatio: 30
					});
				</script>
				<script src="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/js/diagpages.min.js"></script>
				<script>
					\$(document).ready(function() {
						${scrStr}
					});
					\$("#goHomeBtn").click(function() {
					    closeNavMenu();
					    toggleMenuBtn();
					    window.location.replace('${getAppEndpointUrl("diagHome")}');
					});
				</script>
			</body>
		"""
/* """ */
		render contentType: "text/html", data: html
	} catch (ex) { log.error "renderDeviceData Exception:", ex }
}

def renderAppData() {
	renderHtmlMapDesc("AppFile Data", "AppFile Data", getMapDescStr(atomicState?.appData))
}

def renderInstData() {
	renderHtmlMapDesc("Install Data", "Installation Data", getMapDescStr(createInstallDataJson(true)))
}

def renderHtmlMapDesc(title, heading, datamap) {
	try {
		def navHtml = ""
		def html = """
			<head>
				${getWebHeaderHtml(title)}
				<link rel="stylesheet" href="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/css/diagpages.css">
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
									<h3 class="title-text"><img class="logoIcn" src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/nst_manager_5.png"> ${heading}</img></h3>
								</div>
								<div class="col-xs-2 right-head-col pull-right">
									<button id="rfrshBtn" type="button" class="btn refresh-btn pull-right" title="Refresh Page Content"><i id="rfrshBtnIcn" class="fa fa-refresh" aria-hidden="true"></i></button>
								</div>
							</div>
						</div>
					</div>
					<!-- Page Content -->
				 	<div id="page-content-wrapper">
						<div class="container">
							<div id="main" class="panel-body">
								<div class="panel panel-primary">
						 			<div class="panel-heading">
										<h1 class="panel-title panel-title-text">${heading}:</h1>
						 			</div>
						 			<div class="panel-body">
										<div><pre class="mapDataFmt">${datamap.toString().replaceAll("\n", "<br>")}</pre></div>
						 			</div>
						 		</div>
						 	</div>
	 					</div>
	 	 			</div>
	 	 		</div>
			 	<script src="https://cdn.rawgit.com/tonesto7/nest-manager/master/Documents/js/diagpages.min.js"></script>
				<script>
					\$("#goHomeBtn").click(function() {
						closeNavMenu();
						toggleMenuBtn();
						window.location.replace('${getAppEndpointUrl("diagHome")}');
					});
				</script>
			</body>
		"""
	/* """ */
		render contentType: "text/html", data: html
	} catch (ex) { log.error "getAppDataFile Exception:", ex }
}

def sendInstallData() {
	if(atomicState?.cltMetBlacklisted) {
		LogAction("Metrics Upload has been BLACKLISTED for this client.", "warn", true)
	} else { sendFirebaseData(getFbMetricsUrl(), createInstallDataJson(), "clients/${atomicState?.installationId}.json", null, "heartbeat") }
}

def removeInstallData() {
	return removeFirebaseData("clients/${atomicState?.installationId}.json")
}

def sendInstallSlackNotif(inst=true) {
	def cltType = !settings?.mobileClientType ? "Not Configured" : settings?.mobileClientType?.toString()
	def str = ""
	def typeStr = ""
	if(inst) {
		typeStr = "New Client Installed"
	} else {
		typeStr = "Client Updated"
	}
	str += "${typeStr}:"
	str += "\n • DateTime: ${getDtNow()} TimeZone: ${getTimeZone()?.ID?.toString()}"
	str += "\n • App Version: v${appVersion()}"
	str += "\n • Mobile Client: ${cltType}"
	str += atomicState?.authToken && atomicState?.authTokenNum ? "\n • TokenNum: ${atomicState?.authTokenNum}" : ""
	str += atomicState?.authToken && getTimestampVal("authTokenCreatedDt") ? "\n • TokenCreated: ${getTimestampVal("authTokenCreatedDt")}" : ""
	def tf = new SimpleDateFormat("M/d/yyyy - h:mm a")
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	str += atomicState?.authToken && atomicState?.authTokenExpires ? "\n • TokenExpires: ${tf.format(atomicState?.authTokenExpires)}" : ""
	def res = [:]
	res << ["username":"New User Notification"]
	res << ["icon_emoji":":spock-hand:"]
	if(inst) {
		res << ["channel": "#new_clients"]
	} else {
		res << ["channel": "#updated-clients"]
	}
	res << ["text":str]
	def json = new groovy.json.JsonOutput().toJson(res)
	sendDataToSlack(json, "", "post", "${typeStr} Slack Notif")
}

def getDbExceptPath() { return atomicState?.appData?.settings?.database?.exceptionKey ?: "exceptions" }

def ok2SendException(ex) {
	def retVal = true
	if(allowDbException() != true) {
		retVal = false
		// Nothing to see here!
	} else if(atomicState?.cltExcBlacklisted) {
		LogAction("Exception Data Upload has been BLACKLISTED for this client.", "warn", true)
		retVal = false
	} else if(!(settings?.optInSendExceptions || settings?.optInSendExceptions == null)) {
		retVal = false
	} else if (ex instanceof java.util.concurrent.TimeoutException) {
		retVal = false
	}
	return retVal
}

def sendExceptionData(ex, methodName, isChild = false, autoType = null) {
	try {
		def showErrLog = (atomicState?.enRemDiagLogging && settings?.enRemDiagLogging) ? true : false
		def labelstr = (settings?.debugAppendAppName || settings?.debugAppendAppName == null) ? "${app.label} | " : ""
		//LogAction("${labelstr}sendExceptionData(method: $methodName, isChild: $isChild, autoType: $autoType)", "info", false)
		LogAction("${labelstr}sendExceptionData(method: $methodName, isChild: $isChild, autoType: $autoType, ex: ${ex})", "error", showErrLog)
		def exCnt = atomicState?.appExceptionCnt ?: 1
		atomicState?.appExceptionCnt = exCnt?.toInteger() + 1
		if(ok2SendException(ex)) {
			def exString = "${ex}"
			generateInstallId()
			def appType = isChild && autoType ? "automationApp/${autoType}" : "managerApp"
			def exData =[:]
			if(isChild) {
				exData = ["methodName":methodName, "automationType":autoType, "appVersion":(appVersion() ?: "Not Available"),"errorMsg":exString, "errorDt":getDtNow().toString()]
			} else {
				exData = ["methodName":methodName, "appVersion":(appVersion() ?: "Not Available"),"errorMsg":exString, "errorDt":getDtNow().toString()]
			}
			def results = new groovy.json.JsonOutput().toJson(exData)
			sendFirebaseData(getFbExceptionsUrl(), results, "${getDbExceptPath()}/${appType}/${methodName}/${atomicState?.installationId}.json", "post", "Exception")
		}
		if(ex instanceof physicalgraph.exception.StateCharacterLimitExceededException) {
			state.remove("remDiagLogDataStore")
			settingUpdate("resetAllData", "true", "bool")
			atomicState?.resetAllData = false
			runIn(20, "updated", [overwrite: true])
		}
	} catch (e) {
		log.debug "sendExceptionData: other exception caught"
	}
}

def sendChildExceptionData(devType, devVer, ex, methodName) {
	def showErrLog = (atomicState?.enRemDiagLogging && settings?.enRemDiagLogging) ? true : false
	LogAction("sendChildExceptionData(device: $deviceType, devVer: $devVer, method: $methodName, ex: ${ex}", "error", showErrLog)
	def exCnt = atomicState?.childExceptionCnt ?: 1
	atomicState?.childExceptionCnt = exCnt.toInteger() + 1
	if(ok2SendException(ex)) {
		generateInstallId()
		def exString = "${ex}"
		def exData = ["deviceType":devType, "devVersion":(devVer ?: "Not Available"), "methodName":methodName, "errorMsg":exString, "errorDt":getDtNow().toString()]
		def results = new groovy.json.JsonOutput().toJson(exData)
		sendFirebaseData(getFbExceptionsUrl(), results, "${getDbExceptPath()}/${devType}/${methodName}/${atomicState?.installationId}.json", "post", "Exception")
	}
}

def sendFeedbackData(msg) {
	def cltId = atomicState?.installationId
	def exData = ["guid":atomicState?.installationId, "version":appVersion(), "feedbackMsg":(msg ? msg : (settings?.feedbackMsg ?: "No Text")), "msgDt":getDtNow().toString()]
	def results = new groovy.json.JsonOutput().toJson(exData)
	if(sendFirebaseData(getFbLegacyAppUrl(), results, "feedback/data.json", "post", "Feedback")) {
		atomicState?.feedbackPending = false
		if(!msg) { atomicState?.lastFeedbackData = ["lastMsg":settings?.feedbackMsg, "lastMsgDt":getDtNow().toString(), "lastAppVer":appVersion()] }
	}
}

def sendFirebaseData(url, data, pathVal, cmdType=null, type=null) {
	LogAction("sendFirebaseData(${data}, ${pathVal}, $cmdType, $type", "info", true)
	LogTrace("sendFirebaseData(${data}, ${pathVal}, $cmdType, $type")
	return queueFirebaseData(url, data, pathVal, cmdType, type)
}

def queueFirebaseData(url, data, pathVal, cmdType=null, type=null) {
	LogTrace("queueFirebaseData(${data}, ${pathVal}, $cmdType, $type")
	def result = false
	def json = new groovy.json.JsonOutput().prettyPrint(data)
	def params = [ uri: "${url}/${pathVal}", body: json.toString() ]
	def typeDesc = type ? "${type}" : "Data"
	try {
		if(!cmdType || cmdType == "put") {
			asynchttp_v1.put(processFirebaseSlackResponse, params, [ type: "${typeDesc}"])
			result = true
		} else if (cmdType == "post") {
			asynchttp_v1.post(processFirebaseSlackResponse, params, [ type: "${typeDesc}"])
			result = true
		} else { LogAction("queueFirebaseData UNKNOWN cmdType: ${cmdType}", warn, true) }

	} catch(ex) {
		log.error "queueFirebaseData (type: $typeDesc) Exception:", ex
		//sendExceptionData(ex, "queueFirebaseData")
	}
	return result
}

def processFirebaseSlackResponse(resp, data) {
	LogTrace("processFirebaseSlackResponse(${data?.type})")
	def result = false
	def typeDesc = data?.type
	try {
		if(resp?.status == 200) {
			LogAction("processFirebaseSlackResponse: ${typeDesc} Data Sent SUCCESSFULLY", "info", false)
			if(typeDesc?.toString() == "Remote Diag Logs") {

			} else {
				if(typeDesc?.toString() == "heartbeat") { updTimestampMap("lastAnalyticUpdDt", getDtNow()) }
			}
			result = true
		}
		else if(resp?.status == 400) {
			LogAction("processFirebaseSlackResponse: 'Bad Request': ${resp?.status}", "error", true)
		}
		else {
			LogAction("processFirebaseSlackResponse: 'Unexpected' Response: ${resp?.status}", "warn", true)
		}
		if(resp?.hasError()) {
			LogAction("processFirebaseSlackResponse: errorData: $resp.errorData errorMessage: $resp.errorMessage", "error", true)
		}
	} catch(ex) {
		log.error "processFirebaseSlackResponse (type: $typeDesc) Exception:", ex
		sendExceptionData(ex, "processFirebaseSlackResponse")
	}
}

def sendDataToSlack(data, pathVal, cmdType=null, type=null) {
	LogAction("sendDataToSlack(${data}, ${pathVal}, $cmdType, $type", "trace", false)
	def result = false
	def json = new groovy.json.JsonOutput().prettyPrint(data)
	def params = [ uri: "${slackMsgWebHookUrl()}", body: json.toString() ]
	def typeDesc = type ? "${type}" : "Slack Data"
	def respData
	try {
		if(!cmdType || cmdType == "post") {
			asynchttp_v1.post(processFirebaseSlackResponse, params, [ type: "${typeDesc}"])
			result = true
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.HttpResponseException) {
			LogAction("sendDataToSlack: 'HttpResponseException': ${ex?.message}", "error", true)
		}
		else { log.error "sendDataToSlack: ([$data, $pathVal, $cmdType, $type]) Exception:", ex }
		sendExceptionData(ex, "sendDataToSlack")
	}
	return result
}

def removeFirebaseData(pathVal) {
	LogAction("removeFirebaseData(${pathVal})", "trace", false)
	def result = true
	try {
		httpDelete(uri: "${getFbMetricsUrl()}/${pathVal}") { resp ->
			LogAction("cur FB resp: ${resp?.status}", "info", true)
		}
		httpDelete(uri: "${getFbLegacyAppUrl()}/installData/${pathVal}") { resp ->
			LogAction("old FB resp: ${resp?.status}", "info", true)
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.ResponseParseException) {
			LogAction("removeFirebaseData: Response: ${ex?.message}", "info", true)
		} else {
			LogAction("removeFirebaseData: Exception: ${ex?.message}", "error", true)
			sendExceptionData(ex, "removeFirebaseData")
			result = false
		}
	}
	return result
}

/////////////////////////////////////////////////////////////////////////////////////////////
/************************************************************************************************
|				Application Name: Nest Automations				|
|				Author: Anthony S. (@tonesto7) | Eric S. (@E_Sch)		|
|************************************************************************************************/
/////////////////////////////////////////////////////////////////////////////////////////////

// Calls by Automation children
// parent only method

def remSenLock(val, myId) {
	def res = false
	def k = "remSenLock${val}"
	if(val && myId && !parent) {
		def lval = atomicState?."${k}"
		if(!lval) {
			atomicState?."${k}" = myId
			res = true
		} else if(lval == myId) { res = true }
	}
	return res
}

def remSenUnlock(val, myId) {
	def res = false
	if(val && myId && !parent) {
		def k = "remSenLock${val}"
		def lval = atomicState?."${k}"
		if(lval) {
			if(lval == myId) {
				atomicState?."${k}" = null
				state.remove("${k}" as String)
				res = true
			}
		} else { res = true }
	}
	return res
}

def automationNestModeEnabled(val=null) {
	LogTrace("automationNestModeEnabled: $val")
	return getSetVal("automationNestModeEnabled", val)
/*
	if(val == null) {
		return atomicState?.automationNestModeEnabled ?: false
	} else {
		atomicState.automationNestModeEnabled = val.toBoolean()
	}
	return atomicState?.automationNestModeEnabled ?: false
*/
}

def setNModeActive(val=null) {
	LogTrace("setNModeActive: $val")
	def myKey = "automationNestModeEcoActive"
	def retVal
	if(!automationNestModeEnabled(null)) {
		retVal = getSetVal(myKey, false)
/*
	if(automationNestModeEnabled(null)) {
		if(val == null) {
			return atomicState?.automationNestModeEcoActive ?: false
		} else {
			atomicState.automationNestModeEcoActive = val.toBoolean()
		}
	} else { atomicState.automationNestModeEcoActive = false }
	return atomicState?.automationNestModeEcoActive ?: false
*/
	} else { retVal = getSetVal(myKey, val) }
	return retVal
}

def getSetVal(k, val=null) {
	if(val == null) {
		return atomicState?."${k}" ?: false
	} else {
		atomicState."${k}" = val.toBoolean()
	}
	return atomicState?."${k}" ?: false
}

// Most of this is obsolete after upgrade to V5 is complete

def initAutoApp() {
	def appLbl = getCurAppLbl()
	LogAction("initAutoApp(): called by ${appLbl}; May need REINSTALL", "warn", true)
/*
	if (settings["automationTypeFlag"] && settings["restoreCompleted"] != true) {
		log.debug "automationType: ${settings?.automationTypeFlag}"
		parent?.callRestoreState(app, settings?.restoreId?.toString())
	}
*/

	unschedule()
	unsubscribe()
	def autoDisabled = getIsAutomationDisabled()

	app.updateLabel(getAutoTypeLabel())
	atomicState?.lastAutomationSchedDt = null

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
	state.remove("lastaway")
	state.remove("debugAppendAppName")   // cause Automations to re-check with parent for value
	state.remove("enRemDiagLogging")   // cause Automations to re-check with parent for value after updated is called
}

def uninstAutomationApp() {
	LogTrace("uninstAutomationApp")
	def autoType = getAutoType()
	if(autoType == "schMot") {
		def myID = getMyLockId()
		//if(schMotTstat && myID && parent && !migrate) {
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
	//LogAction("getAutoTypeLabel:","trace", true)
	def type = atomicState?.automationType
	def appLbl = getCurAppLbl()
	def newName = appName() == "${appLabel()}" ? "Nest Automations" : "${appName()}"
	def typeLabel = ""
	def newLbl
	def dis = (atomicState?.disableAutomation == true) ? "\n(Disabled)" : ""

	if(type == "nMode")	{ typeLabel = "${newName} (NestMode)" }
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

def getAutoType() { return !parent ? "" : atomicState?.automationType }

def getAutoIcon(type) {
	if(type) {
		switch(type) {
			case "nMode":
				return getAppImg("mode_automation_icon.png")
				break
			case "schMot":
				return getAppImg("thermostat_automation_icon.png")
				break
			case "watchDog":
				return getAppImg("watchdog_icon.png")
				break
			case "storage":
				return getAppImg("storage_icon.png")
				break
			case "remDiag":
				return getAppImg("diag_icon.png")
				break
		}
	}
}

// /********************************************************************************
// |		SCHEDULE, MODE, or MOTION CHANGES ADJUST THERMOSTAT SETPOINTS	|
// |		(AND THERMOSTAT MODE) AUTOMATION CODE				|
// *********************************************************************************/
//
def getTstatAutoDevId() {
	if(settings?.schMotTstat) { return settings?.schMotTstat.deviceNetworkId.toString() }
	return null
}

def isSchMotConfigured() {
	return settings?.schMotTstat ? true : false
}

def getAutomationType() {
	//return atomicState?.automationType ?: null
	return null
}

def getIsAutomationDisabled() {
	def dis = atomicState?.disableAutomation
	return (dis != null && dis == true) ? true : false
}

def fixTempSetting(Double temp) {
	if(getObjType(temp) in ["List", "ArrayList"]) {
		LogAction("fixTempSetting: error temp ${temp} is list", "error", true)
	}
	def newtemp = temp
	if(temp != null) {
		if(getTemperatureScale() == "C") {
			if(temp > 35) {		// setting was done in F
				newtemp = roundTemp( (newtemp - 32.0) * (5 / 9) as Double)
			}
		} else if(getTemperatureScale() == "F") {
			if(temp < 40) {		// setting was done in C
				newtemp = roundTemp( ((newtemp * (9 / 5) as Double) + 32.0) ).toInteger()
			}
		}
	}
	return newtemp
}

private tempRangeValues() {
	return (getTemperatureScale() == "C") ? "10..32" : "50..90"
}

private timeComparisonOptionValues() {
	return ["custom time", "midnight", "sunrise", "noon", "sunset"]
}

private timeDayOfWeekOptions() {
	return ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
}

def roundTemp(Double temp) {
	if(temp == null) { return null }
	def newtemp
	if( getTemperatureScale() == "C") {
		newtemp = Math.round(temp.round(1) * 2) / 2.0f
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
		} else if(getObjType(temp) in ["List", "ArrayList"]) {
			LogAction("roundTemp: error temp ${temp} is list", "error", true)
		}
	}
	return newtemp
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


/************************************************************************************************
|					GLOBAL Code | Logging AND Diagnostic							|
*************************************************************************************************/

def addEventToAskAlexaQueue(vMsg, msgId, queue=null) {
	if(getAskAlexaMQEn()) {
		if(getAskAlexaMultiQueueEn()) {
			LogAction("sendEventToAskAlexaQueue: Adding this Message to the Ask Alexa Queue ($queue): ($vMsg)|${msgId}", "info", true)
			sendLocationEvent(name: "AskAlexaMsgQueue", value: "${app?.label}", isStateChange: true, descriptionText: "${vMsg}", unit: "${msgId}", data: queue)
		} else {
			LogAction("sendEventToAskAlexaQueue: Adding this Message to the Ask Alexa Queue: ($vMsg)|${msgId}", "info", true)
			sendLocationEvent(name: "AskAlexaMsgQueue", value: "${app?.label}", isStateChange: true, descriptionText: "${vMsg}", unit: "${msgId}")
		}
	}
}

def removeAskAlexaQueueMsg(msgId, queue=null) {
	if(getAskAlexaMQEn()) {
		if(getAskAlexaMultiQueueEn()) {
			LogAction("removeAskAlexaQueueMsg: Removing Message ID (${msgId}) from the Ask Alexa Queue ($queue)", "info", true)
			sendLocationEvent(name: "AskAlexaMsgQueueDelete", value: "${app?.label}", isStateChange: true, unit: msgId, data: queue)
		} else {
			LogAction("removeAskAlexaQueueMsg: Removing Message ID (${msgId}) from the Ask Alexa Queue", "info", true)
			sendLocationEvent(name: "AskAlexaMsgQueueDelete", value: "${app?.label}", isStateChange: true, unit: msgId)
		}
	}
}

private getDeviceSupportedCommands(dev) {
	return dev?.supportedCommands.findAll { it as String }
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

def askAlexaImgUrl() { return "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/AskAlexa512.png" }

///////////////////////////////////////////////////////////////////////////////
/******************************************************************************
|				Application Help and License Info Variables					|
*******************************************************************************/
///////////////////////////////////////////////////////////////////////////////
def appName()		{ return "${parent ? "${autoAppName()}" : "${appLabel()}"}${appDevName()}" }
def appLabel()		{ return "Nest Manager" }
def appAuthor()		{ return "Anthony S." }
def appNamespace()	{ return "tonesto7" }
def autoAppName()	{ return "NST Automations" }
def gitRepo()		{ return "tonesto7/nest-manager"}
def gitBranch()		{ return betaMarker() ? "beta" : "master" }
def gitPath()		{ return "${gitRepo()}/${gitBranch()}"}
def developerVer()	{ return false }
def betaMarker()	{ return false }
def appDevType()	{ return false }
def appDevName()	{ return appDevType() ? " (Dev)" : "" }
def appInfoDesc()	{
	def cur = atomicState?.appData?.updater?.versions?.app?.ver.toString()
	def beta = betaMarker() ? " Beta" : ""
	def str = ""
	str += "${appName()}"
	str += isAppUpdateAvail() ? "\n• ${textVersion()} (Latest: v${cur})${beta}" : "\n• ${textVersion()}${beta}"
	str += "\n• ${textModified()}"
	return str
}
def textVersion()	{ return "Version: ${appVersion()}" }
def textModified()	{ return "Updated: ${appVerDate()}" }
def textVerInfo()	{ return "${appVerInfo()}" }
def appVerInfo()	{ return getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/Data/changelog.txt", contentType: "text/plain; charset=UTF-8"], "changelog") }
def textLicense()	{ return getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/app_license.txt", contentType: "text/plain; charset=UTF-8"], "license") }
def textDonateLink(){ return "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2CJEVN439EAWS" }
def streamLink() 	{ return "https://community.smartthings.com/t/nst-manager-real-time-events/89198" }
def stIdeLink()		{ return "https://graph.api.smartthings.com" }
def textCopyright()	{ return "Copyright© 2017, 2018 - Anthony S." }
def textDesc()		{ return "This SmartApp is used to integrate your Nest devices with SmartThings and to enable built-in automations" }