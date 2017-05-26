/********************************************************************************************
|    Application Name: NST Manager                                                          |
|        Copyright (C) 2017 Anthony S.                                                      |
|    Authors: Anthony S. (@tonesto7), Eric S. (@E_sch)                                      |
|    Contributors: Ben W. (@desertblade)                                                    |
|    A few code methods are modeled from those in CoRE by Adrian Caramaliu                  |
|                                                                                           |
|    License Info: https://github.com/tonesto7/nest-manager/blob/master/app_license.txt     |
|                                                                                           |
|    NOTE: I really hope that we don't have a ton of forks being released to the community, |
|    and that we can collaborate to make the smartapp and devices that will accommodate     |
|    every use case                                                                         |
*********************************************************************************************/

import groovy.json.*
import java.text.SimpleDateFormat
import java.security.MessageDigest

definition(
	name: "${appName()}",
	namespace: "${appNamespace()}",
	author: "${appAuthor()}",
	description: "${textDesc()}",
	category: "Convenience",
	iconUrl: "https://raw.githubusercontent.com/${gitPath()}/Images/App/nst_manager_icon.png",
	iconX2Url: "https://raw.githubusercontent.com/${gitPath()}/Images/App/nst_manager_icon%402x.png",
	iconX3Url: "https://raw.githubusercontent.com/${gitPath()}/Images/App/nst_manager_icon%403x.png",
	singleInstance: true,
	oauth: true )

{
	appSetting "clientId"
	appSetting "clientSecret"
	appSetting "devOpt"
}

include 'asynchttp_v1'

def appVersion() { "5.0.8" }
def appVerDate() { "4-29-2017" }
def minVersions() {
	return [
		"automation":["val":504, "desc":"5.0.4"],
		"thermostat":["val":502, "desc":"5.0.2"],
		"protect":["val":502, "desc":"5.0.2"],
		"presence":["val":501, "desc":"5.0.1"],
		"weather":["val":504, "desc":"5.0.4"],
		"camera":["val":504 , "desc":"5.0.4"],
	]
}

preferences {
	//startPage
	page(name: "startPage")

	//Manager Pages
	page(name: "authPage")
	page(name: "mainPage")
	page(name: "deviceSelectPage")
	page(name: "donationPage")
	page(name: "reviewSetupPage")
	page(name: "voiceRprtPrefPage")
	page(name: "changeLogPage")
	page(name: "prefsPage")
	page(name: "infoPage")
	page(name: "nestInfoPage")
	page(name: "structInfoPage")
	page(name: "tstatInfoPage")
	page(name: "protInfoPage")
	page(name: "camInfoPage")
	page(name: "pollPrefPage")
	page(name: "debugPrefPage")
	page(name: "notifPrefPage")
	page(name: "diagPage")
	page(name: "appParamsDataPage")
	page(name: "devNamePage")
	page(name: "childAppDataPage")
	page(name: "childDevDataPage")
	page(name: "managAppDataPage")
	page(name: "alarmTestPage")
	page(name: "simulateTestEventPage")
	page(name: "devNameResetPage")
	page(name: "resetDiagQueuePage")
	page(name: "devPrefPage")
	page(name: "nestLoginPrefPage")
	page(name: "nestTokenResetPage")
	page(name: "uninstallPage")
	page(name: "remoteDiagPage")
	page(name: "custWeatherPage")
	page(name: "automationsPage")
	page(name: "automationKickStartPage")
	page(name: "automationGlobalPrefsPage")
	page(name: "automationStatisticsPage")
	page(name: "automationSchedulePage")
	page(name: "feedbackPage")
	page(name: "sendFeedbackPage")

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
		//Renders Json Data
		path("/renderInstallId")	{action: [GET: "renderInstallId"]}
		path("/renderInstallData")	{action: [GET: "renderInstallData"]}
		path("/receiveEventData") 	{action: [POST: "receiveEventData"]}
		path("/streamStatus")		{action: [POST: "receiveStreamStatus"]}
	}
}

/******************************************************************************
|					Application Pages						  |
*******************************************************************************/
//This Page is used to load either parent or child app interface code
def startPage() {
	if(parent) {
		atomicState?.isParent = false
	} else {
		atomicState?.isParent = true
		authPage()
	}
}

def authPage() {
	//LogTrace("authPage()")
	generateInstallId()
	if(!atomicState?.accessToken) { getAccessToken() }
	atomicState.ok2InstallAutoFlag = false
	if(!atomicState?.usageMetricsStore) { initAppMetricStore() }
	if(atomicState?.notificationPrefs == null) { atomicState?.notificationPrefs = buildNotifPrefMap() }
	def preReqOk = (atomicState?.preReqTested == true) ? true : preReqCheck()
	if(!atomicState?.devHandlersTested) { deviceHandlerTest() }

	if(!atomicState?.accessToken || (!atomicState?.isInstalled && (!atomicState?.devHandlersTested || !preReqOk))) {
		return dynamicPage(name: "authPage", title: "Status Page", nextPage: "", install: false, uninstall: false) {
			section ("Status Page:") {
				def desc
				if(!atomicState?.accessToken) {
					desc = "OAuth is not Enabled for ${appName()} application.  Please click remove and review the installation directions again"
				}
				else if(!atomicState?.devHandlersTested) {
					desc = "Device Handlers are Missing or Not Published.  Please verify the installation instructions and device handlers are present before continuing."
				}
				else if(!preReqOk) {
					desc = "SmartThings Location is not returning (TimeZone: ${location?.timeZone}) or (ZipCode: ${location?.zipCode}) Please edit these settings under the ST IDE or Mobile App"
				}
				else {
					desc = "Application Status has not received any messages to display"
				}
				LogAction("Status Message: $desc", "warn", true)
				paragraph "$desc", required: true, state: null
			}
		}
	}
	updateWebStuff(true)
	setStateVar(true)
	if(atomicState?.newSetupComplete && (atomicState?.appData?.updater?.versions?.app?.ver.toString() == appVersion())) {
		def result = ((atomicState?.appData?.updater?.setupVersion && !atomicState?.setupVersion) || (atomicState?.setupVersion?.toInteger() < atomicState?.appData?.updater?.setupVersion?.toInteger())) ? true : false
		if (result) { atomicState?.newSetupComplete = null }
	}

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
				paragraph appInfoDesc(), image: getAppImg("nst_manager_icon%402x.png", true)
			}
			section(""){
				paragraph "Tap 'Login to Nest' below to authorize SmartThings to your Nest Account.\n\nAfter login you will be taken to the 'Works with Nest' page. Read the info and if you 'Agree' press the 'Accept' button."
				paragraph "❖ FYI: Please use the parent Nest account, Nest Family member accounts will not work correctly", state: "complete"
				href url: redirectUrl, style:"embedded", required: true, title: "Login to Nest", description: description
			}
		}
	}
	else if(showChgLogOk()) { return changeLogPage() }
	else if(showDonationOk()) { return donationPage() }
	else { return mainPage() }
}

def mainPage() {
	//LogTrace("mainPage")
	def execTime = now()
	def setupComplete = (!atomicState?.newSetupComplete || !atomicState.isInstalled) ? false : true
	return dynamicPage(name: "mainPage", title: "", nextPage: (!setupComplete ? "reviewSetupPage" : null), install: setupComplete, uninstall: false) {
		section("") {
			href "changeLogPage", title: "", description: "${appInfoDesc()}", image: getAppImg("nst_manager_icon%402x.png", true)
			if(settings?.restStreaming) {
				def rStrEn = (atomicState?.appData?.eventStreaming?.enabled || getDevOpt())
				href "pollPrefPage", title: "", state: ((atomicState?.restStreamingOn && rStrEn) ? "complete" : null), image: getAppImg("two_way_icon.png"),
						description: "Rest Streaming: (${(settings.restStreaming && rStrEn) ? "On" : "Off"}) (${(!atomicState?.restStreamingOn || !rStrEn) ? "Not Active" : "Active"})"
			}
			if(atomicState?.appData && !appDevType() && isAppUpdateAvail()) {
				href url: stIdeLink(), style:"external", required: false, title:"An Update is Available for ${appName()}!",
						description:"Current: v${appVersion()} | New: ${atomicState?.appData?.updater?.versions?.app?.ver}\n\nTap to Open the IDE in Browser", state: "complete", image: getAppImg("update_icon.png")
			}
			if(atomicState?.appData && !appDevType() && atomicState?.clientBlacklisted) {
				paragraph "This ID is blacklisted, please update software!\nIf software is up to date, contact developer", required: true, state: null
			}
		}
		if(atomicState?.isInstalled) {
			if(settings?.structures && !atomicState?.structures) { atomicState.structures = settings?.structures }
			section("Devices & Location:") {
				paragraph "Home/Away Status: (${strCapitalize(getLocationPresence())})", title: "Location: ${atomicState?.structName}", state: "complete",  image: getAppImg("thermostat_icon.png")
				def t1 = getDevicesDesc(false)
				def devDesc = t1 ? "${t1}\n\nTap to modify devices" : "Tap to configure"
				href "deviceSelectPage", title: "Manage Devices", description: devDesc, state: "complete", image: "blank_icon.png"
			}
			//getDevChgDesc()
		}
		if(!atomicState?.isInstalled) {
			devicesPage()
		}
		if(atomicState?.isInstalled && atomicState?.structures && (atomicState?.thermostats || atomicState?.protects || atomicState?.cameras)) {
			def t1 = getInstAutoTypesDesc()
			def autoDesc = t1 ? "${t1}\n\nTap to modify" : null
			section("Manage Automations:") {
				href "automationsPage", title: "Automations", description: (autoDesc ? autoDesc : "Tap to configure"), state: (autoDesc ? "complete" : null), image: getAppImg("automation_icon.png")
			}
		}
		if(atomicState?.isInstalled) {
			section("Notifications Options:") {
				def t1 = getAppNotifConfDesc()
				href "notifPrefPage", title: "Notifications", description: (t1 ? "${t1}\n\nTap to modify" : "Tap to configure"), state: (t1 ? "complete" : null),
						image: getAppImg("notification_icon2.png")
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
			section("Diagnostics, Donate, Release and License Info") { //, and Leave Feedback:") {
				href "infoPage", title: "Help, Info, and More", description: "", image: getAppImg("info.png")
			}
			section("Remove All Apps, Automations, and Devices:") {
				href "uninstallPage", title: "Uninstall this App", description: "", image: getAppImg("uninstall_icon.png")
			}
		}
		atomicState.ok2InstallAutoFlag = false
		incMainLoadCnt()
		devPageFooter("mainLoadCnt", execTime)
	}
}

def donationPage() {
	return dynamicPage(name: "donationPage", title: "", nextPage: "mainPage", install: false, uninstall: false) {
		section("") {
			def str = ""
			str += "Hello sorry to interupt but it has been 30 days since you installed this SmartApp.  We wanted to present this page as a one time reminder that we accept donations but do not require them."
			str += "If you enjoy our software please remember that we have spent thousand's of hours of our spare time working on features and stability for this application and devices."
			str += "If you have already donated please ignore and thank you very much for your support!"

			str += "\n\nThanks again for using ${appName()}"
			paragraph title: "Donation Reminder", str, required: true, state: null
			href url: textDonateLink(), style:"external", required: false, title:"Donations",
				description:"Tap to open in browser", state: "complete", image: getAppImg("donate_icon.png")
			//href "feedbackPage", title: "Send Us Some Feedback", description: "", image: getAppImg("feedback_icon.png")
			paragraph "This is message will not be shown again", state: "complete"
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
		incDevLocLoadCnt()
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
		atomicState.structures = settings?.structures ?: null
		atomicState.structName = (settings?.structures && atomicState?.structures) ?  "${structs[atomicState?.structures]}" : null

		def stats = getNestThermostats()
		def statDesc = stats.size() ? "Found (${stats.size()}) Thermostats" : "No Thermostats"
		//LogAction("${statDesc} (${stats})", "info", false)

		def coSmokes = getNestProtects()
		def coDesc = coSmokes.size() ? "Found (${coSmokes.size()}) Protects" : "No Protects"
		//LogAction("${coDesc} (${coSmokes})", "info", false)

		def cams = getNestCameras()
		def camDesc = cams.size() ? "Found (${cams.size()}) Cameras" : "No Cameras"
		//LogAction("${camDesc} (${cams})", "info", false)

		section("Select Devices:") {
			if(!stats?.size() && !coSmokes.size() && !cams?.size()) { paragraph "No Devices were found" }
			if(stats?.size() > 0) {
				input(name: "thermostats", title:"Nest Thermostats", type: "enum", required: false, multiple: true, submitOnChange: true, metadata: [values:stats],
						image: getAppImg("thermostat_icon.png"))
			}
			atomicState.thermostats =  settings?.thermostats ? statState(settings?.thermostats) : null
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
			def devSelected = (atomicState?.structures && (atomicState?.thermostats || atomicState?.protects || atomicState?.cameras || atomicState?.presDevice || atomicState?.weatherDevice))
			if(devSelected) {
				section("Device Preferences:") {
					href "devPrefPage", title: "Device Customization", description: "Tap to configure", image: getAppImg("device_pref_icon.png")
				}
			}
			if(atomicState?.protects) {
				section("Nest Protect Alarm Simulation:") {
					if(atomicState?.protects) {
						def dt = atomicState?.isAlarmCoTestActiveDt
						href "alarmTestPage", title: "Test Protect Automations\nBy Simulating Alarm Events", description: "${dt ? "Last Tested:\n$dt\n\n" : ""}Tap to Begin...", image: getAppImg("test_icon.png")
					}
				}
			}
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
				if(atomicState?.appData?.eventStreaming?.enabled == true || getDevOpt()) {
					input ("motionSndChgWaitVal", "enum", title: "Wait before Camera Motion and Sound is marked Inactive?", required: false, defaultValue: 60, metadata: [values:waitValAltEnum(true)], submitOnChange: true, image: getAppImg("motion_icon.png"))
					atomicState.needChildUpd = true
				} else {
					paragraph "No Camera Device Options Yet..."
				}
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
				if(!inReview())	{
					input ("tstatShowHistoryGraph", "bool", title: "Show Graph with Setpoint, Humidity, Temp History?", description: "This disables history collection", required: false, defaultValue: true, submitOnChange: true,
								image: getAppImg("graph_icon2.png"))
				}
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
			}
		}
		if(atomicState?.presDevice) {
			section("Presence Device:") {
				paragraph "No Presence Device Options Yet..."
				//atomicState.needChildUpd = true
			}
		}
		incDevCustLoadCnt()
		devPageFooter("devCustLoadCnt", execTime)
	}
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
				section("Manually Enter a Location:") {
					href url:"https://www.wunderground.com/weatherstation/ListStations.asp", style:"embedded", required:false, title:"Weather Station ID Lookup",
							description: "Lookup Weather Station ID", image: getAppImg("search_icon.png")
					input("custLocStr", "text", title: "Manaually Set Weather Location?", required: false, defaultValue: defZip, submitOnChange: true, image: getAppImg("weather_icon_grey.png"))
					def validEnt = "\n\nWeather Stations: [pws:station_id]\nZipCodes: [90250]\nZWM: [zwm:zwm_number]"
					paragraph "Valid location entries are:${validEnt}", image: getAppImg("blank_icon.png")
				}
			}
		}
		atomicState.lastWeatherUpdDt = 0
		atomicState?.lastForecastUpdDt = 0
		incCustWeathLoadCnt()
		devPageFooter("custWeathLoadCnt", execTime)
	}
}

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

		section("Notifications:") {
			def t1 = getAppNotifConfDesc()
			href "notifPrefPage", title: "Notifications", description: (t1 ? "${t1}\n\nTap to modify" : "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("notification_icon2.png")
		}
		section("Polling:") {
			def pollDesc = getPollingConfDesc()
			href "pollPrefPage", title: "Device | Structure\nPolling Preferences", description: (pollDesc != "" ? "${pollDesc}\n\nTap to modify" : "Tap to configure"), state: (pollDesc != "" ? "complete" : null), image: getAppImg("timer_icon.png")
		}
		showDevSharePrefs()
		if(atomicState?.showHelp) {
			section("") {
				href "infoPage", title: "Help and Info", description: "Tap to view", image: getAppImg("info.png")
			}
		}
		if(!atomicState?.isInstalled) {
			section("") {
				href "uninstallPage", title: "Uninstall this App", description: "Tap to Remove", image: getAppImg("uninstall_icon.png")
			}
		}
	}
}

def showDevSharePrefs() {
	section("Share Data with Developer:") {
		paragraph title: "What is this used for?", "These options send non-user identifiable information and error data to diagnose catch trending issues."
		input ("optInAppAnalytics", "bool", title: "Send Install Data?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("app_analytics_icon.png"))
		input ("optInSendExceptions", "bool", title: "Send Error Data?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("diag_icon.png"))
		if(settings?.optInAppAnalytics != false) {
			input(name: "mobileClientType", title:"Primary Mobile Device?", type: "enum", required: true, submitOnChange: true, metadata: [values:["android":"Android", "ios":"iOS", "winphone":"Windows Phone"]],
							image: getAppImg("${(settings?.mobileClientType && settings?.mobileClientType != "decline") ? "${settings?.mobileClientType}_icon" : "mobile_device_icon"}.png"))
			href url: getAppEndpointUrl("renderInstallData"), style:"embedded", title:"View the Data shared with Developer", description: "Tap to view Data", required:false, image: getAppImg("view_icon.png")
		}
	}
}

def infoPage () {
	def execTime = now()
	dynamicPage(name: "infoPage", title: "Help, Info and Instructions", install: false) {
		section("About this App:") {
			paragraph appInfoDesc(), image: getAppImg("nst_manager_icon%402x.png", true)
		}
		section("Donations:") {
			href url: textDonateLink(), style:"external", required: false, title:"Donations",
				description:"Tap to open in browser", state: "complete", image: getAppImg("donate_icon.png")
		}
		section("Help and Feedback:") {
			href url: getHelpPageUrl(), style:"embedded", required:false, title:"View the Projects Wiki",
				description:"Tap to open in browser", state: "complete", image: getAppImg("info.png")
			href url: getIssuePageUrl(), style:"embedded", required:false, title:"View | Report Issues",
				description:"Tap to open in browser", state: "complete", image: getAppImg("issue_icon.png")
			//href "feedbackPage", title: "Send Developer Feedback", description: "", image: getAppImg("feedback_icon.png")
			href "remoteDiagPage", title: "Send Logs to Developer", description: "", image: getAppImg("diagnostic_icon.png")
		}
		section("Credits:") {
			paragraph title: "Creator:", "Anthony S. (@tonesto7)", state: "complete"
			paragraph title: "Co-Author:", "Eric S. (@E_Sch)", state: "complete"
			paragraph title: "Collaborator:", "Ben W. (@desertblade)", state: "complete"
		}
		section("App Info:") {
			href "changeLogPage", title: "View App Revision History", description: "Tap to view", image: getAppImg("change_log_icon.png")
			paragraph "Current State Usage:\n${getStateSizePerc()}% (${getStateSize()} bytes)", required: true, state: (getStateSizePerc() <= 70 ? "complete" : null),
					image: getAppImg("progress_bar.png")
			if(atomicState?.installationId) {
				paragraph "InstallationID:\n${atomicState?.installationId}"
			}
		}
		if(atomicState?.isInstalled && atomicState?.structures && (atomicState?.thermostats || atomicState?.protects || atomicState?.weatherDevice)) {
			section("View App and Device Data, and Perform Device Tests:") {
				href "nestInfoPage", title: "API | Diagnostics | Testing", description: "", image: getAppImg("api_diag_icon.png")
			}
		}
		section("Licensing Info:") {
			paragraph "${textCopyright()}\n${textLicense()}"
		}
		incInfoLoadCnt()
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
			href "debugPrefPage", title: "Logging", description: (t1 ? "${t1 ?: ""}\n\nTap to modify" : "Tap to configure"), state: ((isAppDebug() || isChildDebug()) ? "complete" : null),
					image: getAppImg("log.png")
		}
		section ("Misc. Options:") {
			input ("useMilitaryTime", "bool", title: "Use Military Time (HH:mm)?", defaultValue: false, submitOnChange: true, required: false, image: getAppImg("military_time_icon.png"))
			input ("disAppIcons", "bool", title: "Disable App Icons?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("no_icon.png"))
			input ("debugAppendAppName", "bool", title: "Show App/Device Name on all Log Entries?", required: false, defaultValue: true, submitOnChange: true, image: getAppImg("log.png"))
			atomicState.needChildUpd = true
		}
		section("Customize Application Label:") {
			label title:"Application Label (optional)", required:false
		}
		incPrefLoadCnt()
		devPageFooter("prefLoadCnt", execTime)
	}
}

def voiceRprtPrefPage() {
	return dynamicPage(name: "voiceRprtPrefPage", title: "Voice Report Preferences", install: false, uninstall: false) {
		section("Report Customization:") {
			paragraph "These options allow you to configure how much info is included in the Thermostat voice reporting."
			if(!atomicState?.appData?.reportPrefs?.disVoiceZoneRprt) {
				input ("vRprtIncSchedInfo", "bool", title: "Include Automation Source Schedule Info?", required: false, defaultValue: true, submitOnChange: false, image: getAppImg("automation_icon.png"))
				input ("vRprtIncZoneInfo", "bool", title: "Include Current Zone Info?", required: false, defaultValue: true, submitOnChange: false, image: getAppImg("thermostat_icon.png"))
				input ("vRprtIncExtWeatInfo", "bool", title: "Include External Info?", required: false, defaultValue: true, submitOnChange: false, image: getAppImg("weather_icon.png"))
			}
			if(!atomicState?.appData?.reportPrefs?.disVoiceUsageRprt) {
				input ("vRprtIncUsageInfo", "bool", title: "Include Usage Info?", required: false, defaultValue: true, submitOnChange: false, image: getAppImg("usage_icon.png"))
			}
		}
		incVrprtPrefLoadCnt()
	}
}

def pollPrefPage() {
	def execTime = now()
	dynamicPage(name: "pollPrefPage", install: false) {
		section("") {
			paragraph "Polling Preferences", image: getAppImg("timer_icon.png")
		}
		if(atomicState?.appData?.eventStreaming?.enabled == true || getDevOpt()) {
			section("Rest Streaming (Experimental):") {
				input(name: "restStreaming", title:"Enable Rest Streaming?", type: "bool", defaultValue: false, required: false, submitOnChange: true, image: getAppImg("two_way_icon.png"))
			}
			if(settings?.restStreaming) {
				section("Configure Streaming Service:") {
					href "restSrvcDiscovery", title: "Auto-Discover Local Service", state: (settings?.selectedRestDevice ? "complete" : null),
							description: selectedRestDiscSrvcDesc() ? "Selected Service:\n${selectedRestDiscSrvcDesc()}" : "Discover NST Service on your local network"
					if(!settings?.selectedRestDevice) {
						input(name: "restStreamIp", title:"Rest Service Address", type: "text", required: true, submitOnChange: true, image: getAppImg("ip_icon.png"))
						input(name: "restStreamPort", title:"Rest Service Port", type: "number", defaultValue: 3000, required: true, submitOnChange: true, image: getAppImg("port_icon.png"))
					}
					getRestSrvcDesc()
					paragraph title: "Notice", "This is still an experimental feature.  It's subject to your local network and internet connections.  If communication is lost it will default back to standard polling."
				}
			} else {
				restDiscoveryClean()
			}
			startStopStream()
		}
		section("Polling:") {
			if(settings?.restStreaming && getRestHost()) {
				paragraph "These settings are only used when rest streaming is inactive or disabled", required: true, state: null, image: getAppImg("info_icon2.png")
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

		incPollPrefLoadCnt()
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
	atomicState.localNstSrvcs = [:]
	atomicState.localRestSrvcs = [:]
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
	//LogAction("options: $options   devices: $devices  objsFound $objsFound", "debug", true)

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
			app(name: "autoApp", appName: autoAppName(), namespace: "tonesto7", multiple: true, title: "Create New Automation (NST)", image: getAppImg("automation_icon.png"))
		}
		if(autoAppInst) {
			section("Automation Details:") {
				def schEn = getChildApps()?.findAll { (!(it.getAutomationType() in ["nMode", "watchDog"]) && it?.getActiveScheduleState()) }
				if(schEn?.size()) {
					href "automationSchedulePage", title: "View Automation Schedule(s)", description: "", image: getAppImg("schedule_icon.png")
				}
				href "automationStatisticsPage", title: "View Automation Statistics", description: "", image: getAppImg("app_analytics_icon.png")
			}
			section("Advanced Options: (Tap + to Show)                                                          ", hideable: true, hidden: true) {
				def descStr = ""
				descStr += (settings?.locDesiredCoolTemp || settings?.locDesiredHeatTemp) ? "Comfort Settings:" : ""
				descStr += settings?.locDesiredHeatTemp ? "\n • Desired Heat Temp: (${settings?.locDesiredHeatTemp}°${getTemperatureScale()})" : ""
				descStr += settings?.locDesiredCoolTemp ? "\n • Desired Cool Temp: (${settings?.locDesiredCoolTemp}°${getTemperatureScale()})" : ""
				descStr += (settings?.locDesiredComfortDewpointMax) ? "${(settings?.locDesiredCoolTemp || settings?.locDesiredHeatTemp) ? "\n\n" : ""}Dew Point:" : ""
				descStr += settings?.locDesiredComfortDewpointMax ? "\n • Max Dew Point: (${settings?.locDesiredComfortDewpointMax}${getTemperatureScale()})" : ""
				descStr += "${(settings?.locDesiredCoolTemp || settings?.locDesiredHeatTemp) ? "\n\n" : ""}${getSafetyValuesDesc()}" ?: ""
				def prefDesc = (descStr != "") ? "${descStr}\n\nTap to modify" : "Tap to configure"
				href "automationGlobalPrefsPage", title: "Global Automation Preferences", description: prefDesc, state: (descStr != "" ? "complete" : null), image: getAppImg("global_prefs_icon.png")
				input "disableAllAutomations", "bool", title: "Disable All Automations?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("disable_icon2.png")
				if(atomicState?.disableAllAutomations == false && settings?.disableAllAutomations) {
					toggleAllAutomations(settings?.disableAllAutomations)

				} else if (atomicState?.disableAllAutomations && !settings?.disableAllAutomations) {
					toggleAllAutomations(settings?.disableAllAutomations)
				}
				atomicState?.disableAllAutomations = settings?.disableAllAutomations
				//input "enTstatAutoSchedInfoReq", "bool", title: "Allow Other Smart Apps to Retrieve Thermostat automation Schedule info?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("info_icon2.png")
				href "automationKickStartPage", title: "Re-Initialize All Automations", description: "Tap to Update All Automations", image: getAppImg("reset_icon.png")
			}
		}
		atomicState.ok2InstallAutoFlag = true
		incAutoLoadCnt()
		devPageFooter("autoLoadCnt", execTime)
	}
}

def automationSchedulePage() {
	def execTime = now()
	dynamicPage(name: "automationSchedulePage", title: "View Schedule Data..", uninstall: false) {
		section("SmartThings Location:") {
			def str = ""
			def tz = TimeZone.getTimeZone(location.timeZone.ID)
			def sunsetT = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", location.currentValue('sunsetTime')).format('h:mm a', tz)
			def sunriseT = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", location.currentValue('sunriseTime')).format('h:mm a', tz)
			str += "Current Mode: ${location?.mode}"
			str += "\nSunrise: ${sunriseT}"
			str += "\nSunset: ${sunsetT}"
			paragraph "$str", state: "complete"
		}
		def schMap = []
		def schSize = 0
		getChildApps()?.each {
			if(it?.getStateVal("newAutomationFile") == null) { return }
			def schInfo = it?.getScheduleDesc()
			if (schInfo?.size()) {
				schSize = schSize+1
				def curSch = it?.getCurrentSchedule()
				section("${it?.label}") {
					schInfo?.each { schItem ->
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
		incViewAutoSchedLoadCnt()
		devPageFooter("viewAutoSchedLoadCnt", execTime)
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
					def tf = new SimpleDateFormat("M/d/yyyy - h:mm a")
						tf.setTimeZone(getTimeZone())
					def lastModDt = data?.lastUpdatedDt ? tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", data?.lastUpdatedDt.toString())) : null
					def lastEvtDt = data?.lastEvent?.date ? tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", data?.lastEvent?.date.toString())) : null
					def lastActionDt = data?.lastActionData?.dt ? tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", data?.lastActionData?.dt.toString())) : null
					def lastEvalDt = data?.lastEvalDt ? tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", data?.lastEvalDt.toString())) : null
					def lastSchedDt = data?.lastSchedDt ? tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", data?.lastSchedDt.toString())) : null
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
		incViewAutoStatLoadCnt()
		devPageFooter("viewAutoStatLoadCnt", execTime)
	}
}

def locDesiredClear() {
	LogTrace("locDesiredClear")
	def list = [ "locDesiredHeatTemp", "locDesiredCoolTemp","locDesiredComfortDewpointMax", "locDesiredTempScale", "locDesiredButton" ]
	list.each { item ->
		settingUpdate(item.toString(), "")
	}
	if(atomicState?.thermostats && settings?.clearLocDesired) {
		atomicState?.thermostats?.each { ts ->
			def dev = getChildDevice(ts?.key)
			def canHeat = dev?.currentState("canHeat")?.stringValue == "false" ? false : true
			def canCool = dev?.currentState("canCool")?.stringValue == "false" ? false : true
			if(canHeat) {
				settingUpdate("${dev?.deviceNetworkId}_safety_temp_min", "")
			}
			if(canCool) {
				settingUpdate("${dev?.deviceNetworkId}_safety_temp_max", "")
			}
			settingUpdate("${dev?.deviceNetworkId}_comfort_dewpoint_max", "")
		}
	}
	settingUpdate("clearLocDesired", false)
}

def getGlobTitleStr(typ) {
	return "Desired Default ${typ} Temp (°${getTemperatureScale()})"
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
				def curDewPnt = wDev ? "${wDev?.currentDewpoint}°${getTemperatureScale()}" : 0
				input "locDesiredComfortDewpointMax", "decimal", title: "Default Dewpoint Threshold (${tRange} °${getTemperatureScale()})", required: false,  range: trange, submitOnChange: true,
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
						str += safeTemp ? "\n• Safety Temps:\n  └ Min: ${safeTemp.min}°${getTemperatureScale()}/Max: ${safeTemp.max}°${getTemperatureScale()}" : "\n• Safety Temps: (Not Set)"
						str += dew_max ? "\n• Comfort Max Dewpoint:\n  └ Max: ${dew_max}°${getTemperatureScale()}" : "\n• Comfort Max Dewpoint: (Not Set)"
						paragraph "${str}", title:"${dev?.displayName}", state: "complete", image: getAppImg("instruct_icon.png")
						if(canHeat) {
							input "${dev?.deviceNetworkId}_safety_temp_min", "decimal", title: "Low Safety Temp °(${getTemperatureScale()})", description: "Range within ${tempRangeValues()}",
									range: tempRangeValues(), submitOnChange: true, required: false, image: getAppImg("heat_icon.png")
						}
						if(canCool) {
							input "${dev?.deviceNetworkId}_safety_temp_max", "decimal", title: "High Safety Temp °(${getTemperatureScale()})", description: "Range within ${tempRangeValues()}",
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
						input "${dev?.deviceNetworkId}_comfort_dewpoint_max", "decimal", title: "Dewpoint Threshold (${tRange} °${getTemperatureScale()})", required: false, range: trange,
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
		incAutoGlobPrefLoadCnt()
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
		def sectDesc = !location.contactBookEnabled ? "Enable push notifications below" : "Select People or Devices to Receive Notifications"
		section(sectDesc) {
			if(!location.contactBookEnabled) {
				input(name: "usePush", type: "bool", title: "Send Push Notitifications", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("notification_icon.png"))
			} else {
				input(name: "recipients", type: "contact", title: "Select Default Contacts", required: false, submitOnChange: true, image: getAppImg("recipient_icon.png")) {
					input ("phone", "phone", title: "Phone Number to send SMS to", required: false, submitOnChange: true, image: getAppImg("notification_icon2.png"))
				}
			}
			if(settings?.recipients || settings?.phone || settings?.usePush) {
				def t1 = getNotifSchedDesc()
				href "setNotificationTimePage", title: "Notification Restrictions", description: (t1 ?: "Tap to configure"), state: (t1 ? "complete" : null), image: getAppImg("restriction_icon.png")
			}
		}
		if(settings?.recipients || settings?.phone || settings?.usePush) {
			if(settings?.recipients && !atomicState?.pushTested) {
				sendMsg("Info", "Push Notification Test Successful. Notifications Enabled for ${appName()}", false)
				atomicState.pushTested = true
			} else { atomicState.pushTested = true }

			section("Location Notifications:") {
				paragraph "Get notified when the Location changes from Home/Away", state: "complete"
				input name: "locPresChangeMsg", type: "bool", title: "Notify on Home/Away changes?", defaultValue: true, submitOnChange: true, image: getAppImg("presence_icon.png")
			}
			section("Alert Configurations:") {
				def t1 = getAppNotifDesc()
				def appDesc = t1 ? "${t1}\n\n" : ""
				href "notifConfigPage", title: "App Notifications", description: "${appDesc}Tap to configure", params: [pType:"app"], state: (appDesc != "" ? "complete" : null),
						image: getAppImg("nst_manager_icon.png")
				t1 = getDevNotifDesc()
				def devDesc = t1 ? "${t1}\n\n" : ""
				href "notifConfigPage", title: "Device Notifications", description: "${devDesc}Tap to configure", params: [pType:"dev"], state: (devDesc != "" ? "complete" : null),
						image: getAppImg("thermostat_icon.png")
				t1 = getAutoNotifDesc()
				def autoDesc = t1 ? "${t1}\n\n" : ""
				href "notifConfigPage", title: "Automation Notifications", description: "${autoDesc}Tap to configure", params: [pType:"auto"], state: (autoDesc != "" ? "complete" : null),
						image: getAppImg("automation_icon.png")
				if(atomicState?.appData?.aaPrefs?.enAaMsgQueue == true) {
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
		incNotifPrefLoadCnt()
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
						input name: "misPollNotifyWaitVal", type: "enum", title: "Delay After Missed Poll?", required: false, defaultValue: 900,
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
		incAppNotifPrefLoadCnt()
		devPageFooter("appNotifPrefLoadCnt", execTime)
	}
}

def getAppNotifDesc() {
	def str = ""
	str += settings?.appApiIssuesMsg != false && settings?.appApiFailedCmdMsg != false ? "\n• API CMD Failures: (${strCapitalize(settings?.appApiFailedCmdMsg ?: "True")})" : ""
	str += settings?.appApiIssuesMsg != false && settings?.appApiRateLimitMsg != false ? "\n• API Rate-Limiting: (${strCapitalize(settings?.appApiRateLimitMsg ?: "True")})" : ""
	str += settings?.sendMissedPollMsg != false ? "\n• Missed Poll Alerts: (${strCapitalize(settings?.sendMissedPollMsg ?: "True")})" : ""
	str += settings?.appDbgDiagRemindMsg != false ? "\n• Debug Log Reminder: (${strCapitalize(settings?.appDbgDiagRemindMsg ?: "True")})" : ""
	str += settings?.sendAppUpdateMsg != false ? "\n• Code Updates: (${strCapitalize(settings?.sendAppUpdateMsg ?: "True")})" : ""
	return str != "" ? str : null
}

def getDevNotifDesc() {
	def str = ""
	str += settings?.devHealthNotifyMsg != false ? "\n• Health Alerts: (${strCapitalize(settings?.devHealthNotifyMsg ?: "True")})" : ""
	str += settings?.camStreamNotifMsg != false ? "\n• Camera Stream Alerts: (${strCapitalize(settings?.camStreamNotifMsg ?: "True")})" : ""
	str += settings?.weathAlertNotif != false ? "\n• Weather Alerts: (${strCapitalize(settings?.weathAlertNotif ?: "True")})" : ""
	return str != "" ? str : null
}

def getAutoNotifDesc() {
	def str = ""
	str += settings?.watchDogNotifMissedEco ? "\n• WatchDog Eco Alerts: (${strCapitalize(settings?.watchDogNotifMissedEco)})" : ""
	return str != "" ? str : null
}

def getAskAlexaDesc() {
	def str = ""
	str += settings?.allowAskAlexaMQ ? "\n• Ask Alexa Msg Queue: (${strCapitalize(settings?.allowAskAlexaMQ)})" : ""
	str += getAskAlexaMultiQueueEn() && atomicState?.askAlexaMQList ? "\nMultiple Queues Available:\n• Queues: (${atomicState?.askAlexaMQList?.size()})" : ""
	return str != "" ? str : null
}

def getAppNotifConfDesc() {
	def str = ""
	if(pushStatus()) {
		def ap = getAppNotifDesc()
		def de = getDevNotifDesc()
		def au = getAutoNotifDesc()
		def nd = getNotifSchedDesc()
		str += (settings?.recipients) ? "Sending via Contact Book (True)" : ""
		str += (settings?.usePush) ? "Sending via Push: (True)" : ""
		str += (settings?.phone) ? "\nSending via SMS: (True)" : ""
		str += (ap || de || au) ? "\nEnabled Alerts:" : ""
		str += (ap) ? "\n• App Alerts (True)" : ""
		str += (de) ? "\n• Device Alerts (True)" : ""
		str += (au) ? "\n• Automation Alerts (True)" : ""
		str += (nd) ? "\n\nAlert Restrictions:\n${nd}" : ""
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

def toggleAllAutomations(disable=false) {
	def dis = disable == null ? false : disable
	def cApps = getChildApps()
	cApps.each { ca ->
		ca?.setAutomationStatus(dis, true)
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
		incDevCustNameLoadCnt()
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
			input "quietDays", "enum", title: "Only on these days of the week", multiple: true, required: false, image: getAppImg("day_calendar_icon.png"), options: timeDayOfWeekOptions()
			input "quietModes", "mode", title: "When these Modes are Active", multiple: true, submitOnChange: true, required: false, image: getAppImg("mode_icon.png")
		}
	}
}

def debugPrefPage() {
	def execTime = now()
	dynamicPage(name: "debugPrefPage", install: false) {
		section ("Application Logs") {
			input (name: "appDebug", type: "bool", title: "Show ${appName()} Logs in the IDE?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("log.png"))
			if(appDebug) {
				input (name: "advAppDebug", type: "bool", title: "Show Verbose Logs?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("list_icon.png"))
			}
		}
		section ("Child Device Logs") {
			input (name: "childDebug", type: "bool", title: "Show Device Logs in the IDE?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("log.png"))
		}
		section("Remote Diagnostics:") {
			href "remoteDiagPage", title: "Stream Logs to Developer?", description: "", image: getAppImg("diagnostic_icon.png")
		}
		section ("Reset Application Data") {
			input (name: "resetAllData", type: "bool", title: "Reset Application Data?", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("log.png"))
			if(settings?.resetAllData) { LogAction("Reset Application Data Enabled", "info", true) }
			else { LogAction("Reset Application Data Disabled", "info", true) }
		}
		if(settings?.appDebug || settings?.childDebug) {
			if(atomicState?.debugEnableDt == null) { atomicState?.debugEnableDt = getDtNow() }
		} else { atomicState?.debugEnableDt = null }
		atomicState.needChildUpd = true
		incLogPrefLoadCnt()
		devPageFooter("logPrefLoadCnt", execTime)
	}
}

def remoteDiagPage () {
	def execTime = now()
	dynamicPage(name: "remoteDiagPage", title: "Send Logs to the Developer", refreshInterval: (atomicState?.enRemDiagLogging ? 30 : 0), install: false) {
		def diagAllowed = atomicState?.appData?.database?.allowRemoteDiag == true ? true : false
		def diagDevAuth = (atomicState?.remDiagClientId in atomicState?.appData?.clientRemDiagAuth?.clients) ? true : false
		//log.debug "diagAllowed: $diagAllowed | diagDevAuth: $diagDevAuth"
		section() {
			def formatVal = settings?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
			def tf = new SimpleDateFormat(formatVal)
			if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
			paragraph title: "How will this work?", "Once enabled this SmartApp will send manager and automation logs the developers Firebase database for review.  Turn off to remove all data from the remote site."
			paragraph "This will automatically turn off 2 hours"
			input (name: "enRemDiagLogging", type: "bool", title: "Enable Remote Diag?", required: false, defaultValue: (atomicState?.enRemDiagLogging ?: false), submitOnChange: true, image: getAppImg("diagnostic_icon.png"))
		}
		remDiagProcChange(diagAllowed,settings?.enRemDiagLogging)
		section() {
			if(atomicState?.enRemDiagLogging) {
				href url: getAppEndpointUrl("renderInstallId"), style:"embedded", title:"Provide this ID to Developer", description:"${atomicState?.remDiagClientId}\nTap to Allow Sharing",
						required: true,state: null
				def str = diagDevAuth ? "This ClientId is Authorized by Developer to stream logs to the remote server." : "This client is not authorized to stream logs. Please contact the developer if you have issues"
				paragraph str, required: true, state: (diagDevAuth ? "complete" : null)
			}
		}
		section() {
			if(atomicState?.remDiagLogDataStore?.size() >= 0) {
				def str = ""
				str += "Current Logs in the Data Store: (${atomicState?.remDiagLogDataStore?.size()})"
				if(atomicState?.remDiagDataSentDt) { str += "\n\nLast Sent Data to DB:\n${formatDt2(atomicState?.remDiagDataSentDt)} | (${getLastRemDiagSentSec()} sec ago)" }
				if(atomicState?.remDiagLogSentCnt) { str += "\n\nLogs sent to DB: (${atomicState?.remDiagLogSentCnt})" }
				paragraph str, state: "complete"
			}
		}
		incRemDiagLoadCnt()
		devPageFooter("remDiagLoadCnt", execTime)
	}
}

void remDiagProcChange(diagAllowed, setOn) {
	if(diagAllowed && setOn) {
		if(!atomicState?.enRemDiagLogging && atomicState?.remDiagLogActivatedDt == null) {
			LogAction("Remote Diagnostic Logs activated", "info", true)
			clearRemDiagData()
			chkRemDiagClientId()
			atomicState?.enRemDiagLogging = true
			atomicState?.remDiagLogActivatedDt = getDtNow()
			sendSetAndStateToFirebase()
		}
	} else {
		if(atomicState?.remDiagLogActivatedDt != null && (!diagAllowed || !setOn)) {
			LogAction("Remote Diagnostic Logs deactivated", "info", true)
			atomicState?.enRemDiagLogging = false
			clearRemDiagData()
			atomicState?.remDiagLogActivatedDt = null	// require toggle off then on again to force back on after timeout
		}
	}
}

void chkRemDiagClientId() {
	if(!atomicState?.remDiagClientId || atomicState?.remDiagClientId != atomicState?.installationId) { atomicState?.remDiagClientId = atomicState?.installationId	}
}

def clearRemDiagData(force=false) {
	if(!settings?.enRemDiagLogging || force) {
		if(atomicState?.remDiagClientId && removeRemDiagData()) { atomicState?.remDiagClientId = null }
	}
	atomicState?.remDiagLogDataStore = null
	//atomicState?.remDiagLogActivatedDt = null	// NOT done to have force off then on to re-enable
	atomicState?.remDiagDataSentDt = null
	atomicState?.remDiagLogSentCnt = null
	LogAction("Cleared Diag data", "info", true)
}

def saveLogtoRemDiagStore(String msg, String type, String logSrcType=null) {
	//LogTrace("saveLogtoRemDiagStore($msg, $type, $logSrcType)")
	if(parent) { return }
	if(atomicState?.appData?.database?.allowRemoteDiag && (atomicState?.remDiagClientId in atomicState?.appData?.clientRemDiagAuth?.clients)) {
		if(atomicState?.enRemDiagLogging) {
			if(getStateSizePerc() >= 90) {
				// this is log.xxxx to avoid looping/recursion
				log.warn "saveLogtoRemDiagStore: remoteDiag log storage suspended state size is ${getStateSizePerc()}%"
				return
			}
			def data = atomicState?.remDiagLogDataStore ?: []
			def item = ["dt":getDtNow().toString(), "type":type, "src":(logSrcType ?: "Not Set"), "msg":msg]
			data << item
			atomicState?.remDiagLogDataStore = data
			if(atomicState?.remDiagLogDataStore?.size() > 20 || getLastRemDiagSentSec() > 600 || getStateSizePerc() >= 75) {
				sendRemDiagData()
				atomicState?.remDiagLogDataStore = []
			}
		}
	}
	if(atomicState?.enRemDiagLogging) {
		def turnOff = false
		def reasonStr = ""
		if(getRemDiagActSec() > (3600 * 2)) {
			turnOff = true
			reasonStr += "was active for last 2 hours "
		}
		if(!atomicState?.appData?.database?.allowRemoteDiag || !(atomicState?.remDiagClientId in atomicState?.appData?.clientRemDiagAuth?.clients)) {
			turnOff = true
			reasonStr += "appData does not allow"
		}
		if(turnOff) {
			settingUpdate("enRemDiagLogging", "false","bool")
			atomicState?.enRemDiagLogging = false
			LogAction("Remote Diagnostics disabled ${reasonStr}", "info", true)
			def cApps = getChildApps()
			if(cApps) {
				cApps?.sort()?.each { chld ->
					chld?.update()
				}
			}
			clearRemDiagData()
		}
	}
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

def sendRemDiagData() {
	def data = atomicState?.remDiagLogDataStore
	if(data?.size()) {
		chkRemDiagClientId()
		if(atomicState?.remDiagClientId) {
			def json = new groovy.json.JsonOutput().toJson(data)
			sendFirebaseData(json, "${getDbRemDiagPath()}/clients/${atomicState?.remDiagClientId}.json", "post", "Remote Diag Logs")
			def lsCnt = !atomicState?.remDiagLogSentCnt ? data?.size() : atomicState?.remDiagLogSentCnt+data?.size()
			atomicState?.remDiagLogSentCnt = lsCnt
			atomicState?.remDiagDataSentDt = getDtNow()
		}
	}
}

def sendSetAndStateToFirebase() {
	chkRemDiagClientId()
	if(atomicState?.remDiagClientId) {
		sendFirebaseData(createManagerBackupDataJson(), "${getDbRemDiagPath()}/clients/${atomicState?.remDiagClientId}/setandstate.json", "put", "Remote Diag Logs")
		atomicState?.remDiagDataSentDt = getDtNow()
	}
}

def getRemDiagActSec() { return !atomicState?.remDiagLogActivatedDt ? 100000 : GetTimeDiffSeconds(atomicState?.remDiagLogActivatedDt, null, "getRemDiagActSec").toInteger() }
def getLastRemDiagSentSec() { return !atomicState?.remDiagDataSentDt ? 1000 : GetTimeDiffSeconds(atomicState?.remDiagDataSentDt, null, "getLastRemDiagSentSec").toInteger() }

def changeLogPage () {
	def execTime = now()
	dynamicPage(name: "changeLogPage", title: "", nextPage: "mainPage", install: false) {
		section() {
			paragraph title: "What's New in this Release...", "", state: "complete", image: getAppImg("whats_new_icon.png")
			paragraph appVerInfo()
		}
		def iData = atomicState?.installData
		iData["shownChgLog"] = true
		atomicState?.installData = iData
		incChgLogLoadCnt()
		devPageFooter("chgLogLoadCnt", execTime)
	}
}

def uninstallPage() {
	dynamicPage(name: "uninstallPage", title: "Uninstall", uninstall: true) {
		section("") {
			if(parent) {
				paragraph "This will uninstall the ${app?.label} Automation!"
			} else {
				paragraph "This will uninstall the App, All Automation Apps and Child Devices.\n\nPlease make sure that any devices created by this app are removed from any routines/rules/smartapps before tapping Remove."
			}
		}
		remove("Remove ${appName()} and Devices!", "WARNING!!!", "Last Chance to Stop!\nThis action is not reversible\n\nThis App, All Devices, and Automations will be removed")
	}
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

/******************************************************************************
|						PAGE TEXT DESCRIPTION METHODS						  |
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
			str += minTemp ? "\n• Min. Temp: ${minTemp}°${getTemperatureScale()}" : ""
			str += maxTemp ? "\n• Max. Temp: ${maxTemp}°${getTemperatureScale()}" : ""
			//str += maxHum ? "\n• Max. Humidity: ${maxHum}%" : ""
			//str += (ts && (minTemp || maxTemp) && (maxDew)) ? "\n" : ""
			str += (ts && (maxDew)) ? "\nComfort Values:" : ""
			str += maxDew ? "\n• Max. Dewpnt: ${maxDew}°${getTemperatureScale()}" : ""
			str += (str != "" && siz > ctr) ? "\n\n" : ""
			ctr = (str != "") ? ctr += 1 : ctr
			siz = (str == "") ? siz -= 1 : siz
		}
	}
	return str
}

def showVoiceRprtPrefs() {
	if(atomicState?.thermostats && (!atomicState?.appData?.reportPrefs?.disVoiceZoneRprt || !atomicState?.appData?.reportPrefs?.disVoiceUsageRprt)) {
		def rPrefs = getVoiceRprtPrefDesc()
		section("Voice Reports:") {
			href "voiceRprtPrefPage", title: "Voice Report Preferences", description: (rPrefs ? "${rPrefs}\n\nTap to modify" : "Tap to configure"), state: (rPrefs ? "complete" : ""), image: getAppImg("speech2_icon.png")
		}
	}
}

def getVoiceRprtPrefs() {
	return [
		"allowVoiceUsageRprt":(atomicState?.appData?.reportPrefs?.disVoiceUsageRprt == true) ? false : true,
		"allowVoiceZoneRprt":(atomicState?.appData?.reportPrefs?.disVoiceZoneRprt == true) ? false : true,
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
	def rStrEn = (atomicState?.appData?.eventStreaming?.enabled || getDevOpt())
	def pollValDesc = (!settings?.pollValue || settings?.pollValue == "180") ? "" : (!atomicState?.streamPolling ? " (Custom)" : " (Stream)")
	def pollStrValDesc = (!settings?.pollStrValue || settings?.pollStrValue == "180") ? "" : (!atomicState?.streamPolling ? " (Custom)" : " (Stream)")
	def pollWeatherValDesc = (!settings?.pollWeatherValue || settings?.pollWeatherValue == "900") ? "" : " (Custom)"
	def pollWaitValDesc = (!settings?.pollWaitVal || settings?.pollWaitVal == "10") ? "" : " (Custom)"
	def pStr = ""
	pStr += rStrEn ? "Nest Stream: (${(settings.restStreaming && rStrEn) ? "On" : "Off"}) (${(!atomicState?.restStreamingOn) ? "Not Active" : "Active"})" : ""
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
	//def schedInverted = settings?.DmtInvert
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
	if(settings?.useCustWeatherLoc == false || settings?.useCustWeatherLoc == null) { return res }
	if(settings?.custWeatherLocSrch == true && settings?.custWeatherResultItems != null) {
		res = desc ? (settings?.custWeatherResultItems[0]?.split("\\:"))[1].split("\\.")[0] : settings?.custWeatherResultItems[0].toString()
	} else if(settings?.useCustWeatherLoc == false && settings?.custLocStr != null) {
		res = settings?.custLocStr
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
	str += settings?.presDevice ? "\n • [1] Presence Device" : ""
	str += settings?.weatherDevice ? "\n • [1] Weather Device" : ""
	str += (!settings?.thermostats && !settings?.protects && !settings?.cameras && !settings?.presDevice && !settings?.weatherDevice) ? "\n • No Devices Selected" : ""
	return (str != "") ? str : null
}

def getAppDebugDesc() {
	def str = ""
	str += isAppDebug() ? "App Debug: (${debugStatus()})${advAppDebug ? "(Trace)" : ""}" : ""
	str += isChildDebug() ? "${isAppDebug() ? "\n" : ""}Device Debug: (${deviceDebugStatus()})" : ""
	return (str != "") ? "${str}" : null
}

/*
def getDevChgDesc() {
	if(!atomicState?.currentDevMap) { currentDevMap(true) }
	def added = [:]
	def deleted = [:]
	def result = compareDevMap(atomicState?.currentDevMap?.instDevicesMap, currentDevMap()?.instDevicesMap, added, deleted)
	log.debug "getDevChgDesc | result: $result"
	def res = []
	def opts = ["added", "removed"]
	def keys = ["thermostats", "vThermostats", "protects", "cameras", "presDevice", "weatherDevice"]
	opts?.each { t ->
		def str = ""
		if(result?."${t}"?.size()) {
			def cnt = 1
			result?."${t}"?.each {
				if(it?.key in ["presDevice", "weatherDevice"]) {
					str += it?.key ? "${cnt>1 ? "\n" : ""}Virtual Devices" : ""
					if(it?.key == "presDevice") {
						str += it?.key ? "\n • Presence Device" : ""
					} else {
						str += it?.key ? "\n • Weather Device" : ""
					}
				} else {
					str += it?.key ? "${cnt>1 ? "\n\n" : ""}${strCapitalize(it?.key)}:" : ""
					if(it?.value?.size()) {
						it?.value?.each { val ->
							str += val ? "\n • $val" : ""
						}
					}
				}
				cnt = cnt+1
			}
			//log.debug "str: $str"
			if(str != "") {
				res += section("Pending Device Changes:") {
					if(t == "added") {
						paragraph title: "Installing", str, state: "complete"
					} else if(t=="removed") {
						paragraph title: "Removing", str, required: true, state: null
					}
				}
			}
		}
	}
	return disp?.size() ? disp : null
}

def compareDevMap(map1, map2, added, deleted, lastkey=null) {
	//LogTrace("compareDevMap(map1, map2, $added, $deleted, $lastkey)")
	def keys = ["thermostats", "vThermostats", "protects", "cameras", "presDevice", "weatherDevice"]
	for(m1 in map1) {
		def keyVal = m1?.key.toString()
		def m1Key = map1?."${keyVal}"
		def m2Key = map2?."${keyVal}"
		if ((m1Key != null) && (m2Key == null)) {
			log.debug "Map1 Key${keyVal ? " (2nd Lvl.)" : ""}: ($keyVal) | M1Key: $m1Key | M2Key: $m2Key | M1Data: $m1"
			def val = lastkey ?: keyVal
			if(val in keys) {
				log.debug "val: $val"
				if(deleted[val] == null) { deleted[val] = [] } //if the key is invalid then create the map entry
				deleted[val].push(m1Key)
				log.debug "($val) Devices Pending Removal: ${m1Key}"
			}
		} else {
			if ((m1Key instanceof Map) && (m2Key instanceof Map)) {
				compareDevMap(m1Key, m2Key, added, deleted, keyVal)
			}
		}
	}
	for(m2 in map2) {
		def keyVal = m2?.key.toString()
		def m1Key = map1?."${keyVal}"
		def m2Key = map2?."${keyVal}"
		if ((m2Key != null) && (m1Key == null)) {
			log.debug "Map2 Key${keyVal ? " (2nd Lvl.)" : ""}: ($keyVal) | M2Key: $m2Key | M1Key: $m1Key | M2Data: $m2"
			def val = lastkey ?: m2Key
			if(val in keys) {
				if(added[val] == null) { added[val] = [] }
				added[val].push(m2Key)
				log.debug "($val) Devices Pending Install: ${m2Key}"
			}
		}
	}
	return ["added":added, "removed":deleted]
}

def currentDevMap(update=false) {
	def res = [:]
	def keys = ["thermostats", "vThermostats", "protects", "cameras", "presDevice", "weatherDevice"]
	try {
		keys?.each { key ->
			def items = [:]
			def var = atomicState?."${key}"
			if(var) {
				if(res[key] == null) { res[key] = [:] }
				var?.each { item ->
					if(key == "presDevice") {
						def val = [(getNestPresId().toString()):getNestPresLabel().toString()]
						res[key] << val
						//log.debug "val: ${val}"
					}
					else if(key == "weatherDevice") {
						def val = [(getNestWeatherId().toString()):getNestWeatherLabel().toString()]
						res[key] << val
						//log.debug "val: ${val}"
					} else {
						if(item?.key) {
							res[key] << [(item?.key):item?.value]
						}
					}
				}
			}
		}
		res = ["instDevicesMap":res]
		//log.debug "res: ${res}"
		if(update) {
			atomicState?.currentDevMap = res
		} else { return res }
	} catch (ex) {
		log.error "currentDevMap Exception:", ex
		sendExceptionData(ex, "currentDevMap")
	}
	return ""
}
*/

/******************************************************************************
*					  			NEST LOGIN PAGES		  	  		  		  *
*******************************************************************************/
def nestLoginPrefPage () {
	if(!atomicState?.authToken) {
		return authPage()
	} else {
		return dynamicPage(name: "nestLoginPrefPage", nextPage: atomicState?.authToken ? "" : "authPage", install: false) {
			def formatVal = settings?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
			def tf = new SimpleDateFormat(formatVal)
			if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
			atomicState.tokenCreatedDt = atomicState?.tokenCreatedDt ?: getDtNow()
			section() {
				paragraph title: "Authorization Info:", "Authorization Date:\n• ${tf?.format(Date.parse("E MMM dd HH:mm:ss z yyyy", atomicState?.tokenCreatedDt))}", state: "complete"
				paragraph "Last Nest Connection:\n• ${tf?.format(Date.parse("E MMM dd HH:mm:ss z yyyy", atomicState.lastDevDataUpd))}"
			}
			section("Revoke Authorization Reset:") {
				href "nestTokenResetPage", title: "Log Out and Reset Nest Token", description: "Tap to Reset Nest Token", required: true, state: null, image: getAppImg("reset_icon.png")

			}
		}
	}
	incNestLoginLoadCnt()
}

def nestTokenResetPage() {
	return dynamicPage(name: "nestTokenResetPage", install: false) {
		section ("Resetting Nest Token") {
			revokeNestToken()
			atomicState.authToken = null
			paragraph "Token reset\nPress Done to return to Login page"
		}
	}
}

/******************************************************************************
 *#########################	NATIVE ST APP METHODS ############################*
 ******************************************************************************/
def installed() {
	LogAction("Installed with settings: ${settings}", "debug", true)
	if(!parent) {
		atomicState?.installData = ["initVer":appVersion(), "dt":getDtNow().toString(), "updatedDt":"Not Set", "freshInstall":true, "shownDonation":false, "shownFeedback":false, "shownChgLog":true, "usingNewAutoFile":true]
		sendInstallSlackNotif()
	}
	initialize()
	sendNotificationEvent("${appName()} installed")
}

def updated() {
	LogAction("${app.label} Updated...with settings: ${settings}", "debug", true)
	if(atomicState?.migrationInProgress == true) { LogAction("Skipping updated() as migration inprogress", "warn", true); return }
	initialize()
	sendNotificationEvent("${appName()} has updated settings")
	if(parent) {
		atomicState?.lastUpdatedDt = getDtNow()
	}
}

def uninstalled() {
	//LogTrace("uninstalled")
	if(parent) {
		uninstAutomationApp()
	} else {
		uninstManagerApp()
	}
	//sendNotificationEvent("${appName()} is uninstalled")
}

def initialize() {
	//LogTrace("initialize")

	if(atomicState?.resetAllData || settings?.resetAllData) {
		if(fixState()) { return }	// runIn of fixState will call initAutoApp() or initManagerApp()
		if (!parent) { settingUpdate("resetAllData", "false", "bool") }
	}
	if(parent) {
		runIn(6, "initAutoApp", [overwrite: true])
	}
	else {
		if(checkMigrationRequired()) { return true }	// This will call updated later
		reInitBuiltins()	// These are to have these apps release subscriptions to devices (in case of delete)
		runIn(14, "initManagerApp", [overwrite: true])	// need to give time for watchdog updates before we try to delete devices.
		runIn(34, "reInitBuiltins", [overwrite: true])	// need to have watchdog/nestmode check if we created devices
	}
}

def reInitBuiltins() {
	initWatchdogApp()
	initNestModeApp()
}

def initNestModeApp() {
	if(automationNestModeEnabled()) {
		def nestModeApp = getChildApps()?.findAll { it?.getAutomationType() == "nMode" }
		if(nestModeApp?.size() >= 1) {
			def cnt = 1
			nestModeApp?.each { chld ->
				if(cnt == 1) {
					//LogAction("Running Update Command on Nest Mode", "warn", true)
					chld.update()
				} else if(cnt > 1) {
					LogAction("Deleting Extra nMode (${chld?.id})", "warn", true)
					deleteChildApp(chld)
				}
				cnt = cnt+1
			}
		}
	}
}

def initManagerApp() {
	setStateVar()
	restStreamHandler(true)   // stop the rest stream
	atomicState?.restStreamingOn = false
	atomicState.ssdpOn = false
	unschedule()
	unsubscribe()
	atomicState.pollingOn = false
	atomicState.lastChildUpdDt = null // force child update on next poll
	atomicState.lastForcePoll = null
	atomicState.swVersion = appVersion()
	if(settings?.structures && atomicState?.structures && !atomicState.structName) {
		def structs = getNestStructures()
		if(structs) {
			atomicState.structName = "${structs[atomicState?.structures]}"
		}
	}
	if(addRemoveDevices()) { // if we changed devices, reset queues and polling
		atomicState.cmdQlist = []
	}
	if(settings?.thermostats || settings?.protects || settings?.cameras || settings?.presDevice || settings?.weatherDevice) {
		atomicState?.isInstalled = true
	} else { atomicState.isInstalled = false }
	if(atomicState?.autoMigrationComplete == true) { // fix for bug that removed this setting - temporary
		def iData = atomicState?.installData
		iData["usingNewAutoFile"] = true
		atomicState?.installData = iData
	}
	if(atomicState?.installData?.usingNewAutoFile) {
		stateCleanup()
		def tstatAutoApp = getChildApps()?.find {
			try {
				def aa = it.getAutomationType()
				def bb = it.getCurrentSchedule()
			}
			catch (Exception e) {
				log.error "BAD Automation file ${app?.label?.toString()}, please RE-INSTALL automation file"
				appUpdateNotify(true)
			}
		}
	}
	subscriber()
	setPollingState()
	def appInstData = atomicState?.installData
	if(atomicState?.isInstalled && appInstData?.usingNewAutoFile) {
		if(app.label == "Nest Manager") { app.updateLabel("NST Manager") }
	}
	startStopStream()
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
	def strEn = (atomicState?.appData?.eventStreaming?.enabled == true || getDevOpt()) ? true : false
	if((!strEn || !settings?.restStreaming) && !atomicState?.restStreamingOn) {
		return
	}
	if(strEn && settings?.restStreaming && atomicState?.restStreamingOn) {
		runIn(5, "restStreamCheck", [overwrite: true])
		return
	}
	if(strEn && settings?.restStreaming && !atomicState?.restStreamingOn) {
		LogAction("Sending restStreamHandler(Start) Event to local node service", "debug", true)
		restStreamHandler()
		runIn(5, "restStreamCheck", [overwrite: true])
	}
	else if ((!settings?.restStreaming || !strEn) && atomicState?.restStreamingOn) {
		LogAction("Sending restStreamHandler(Stop) Event to local node service", "debug", true)
		restStreamHandler(true)
		atomicState?.restStreamingOn = false
		runIn(5, "restStreamCheck", [overwrite: true])
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
	LogTrace("getRestHost: autoHost: ${autoHost}  ip: ${ip}  port: ${port}")
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

def restStreamHandler(close = false) {
	def toClose = close
	def host = getRestHost()
	if(!host) {
		atomicState.restStreamingOn = false; 
		host = atomicState?.lastRestHost ?: null
		atomicState.lastRestHost = null
		if(!host) { return }
		toClose = true
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
	try {
		def hubAction = new physicalgraph.device.HubAction(
			method: "POST",
			headers: [
				"HOST": host,
				"nesttoken": "${atomicState?.authToken}",
				"connStatus": "${connStatus}",
				"callback": "${getApiURL()}",
				"sttoken": "${atomicState?.accessToken}"
			],
			path: "/stream",
			body: ""
		)
		sendHubCommand(hubAction)
	}
	catch (Exception e) {
		log.error "restStreamHandler Exception $e on $hubAction"
		atomicState.restStreamingOn = false
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
	try {
		atomicState.lastRestHost = host
		def hubAction = new physicalgraph.device.HubAction(
			method: "POST",
			headers: [
				"HOST": host,
				"callback": "${getApiURL()}",
				"sttoken": "${atomicState?.accessToken}"
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

def receiveStreamStatus() {
	def resp = request?.JSON
	if(resp) {
		def t0 = resp?.streaming == true ? true : false
		def t1 = atomicState?.restStreamingOn
		if(t1 != t0) {		// report when changes
			LogAction("restStreamStatus: resp: ${resp}", "debug", true)
		}
		atomicState?.restStreamingOn = t0
		if(!settings?.restStreaming && t0) {		// suppose to be off
			LogAction("Sending restStreamHandler(Stop) Event to local node service", "debug", false)
			restStreamHandler(true)
		} else if (settings?.restStreaming && !atomicState?.restStreamingOn) {		// suppose to be on
			runIn(21, "startStopStream", [overwrite: true])
		}
		if(settings?.restStreaming && t0) {		// All good
			atomicState?.lastHeardFromNestDt = getDtNow()
			if(atomicState?.ssdpOn == true) {
				unsubscribe()
				atomicState.ssdpOn = false
				subscriber()
			}
		}
		atomicState?.restServiceData = resp

		render contentType: 'text/html', data: "status received...ok", status: 200
	}
}

def uninstManagerApp() {
	LogTrace("uninstManagerApp")
	try {
		if(addRemoveDevices(true)) {
			restStreamHandler(true)   // stop the rest stream
			//removes analytic data from the server
			if(removeInstallData()) {
				atomicState?.installationId = null
			}
			//If any client related data exists on firebase it will be removed
			clearRemDiagData(true)
			clearAllAutomationBackupData()
			//Revokes Smartthings endpoint token
			revokeAccessToken()
			//Revokes Nest Auth Token
			revokeNestToken()
			//sends notification of uninstall
			sendNotificationEvent("${appName()} is uninstalled")
		}
	} catch (ex) {
		log.error "uninstManagerApp Exception:", ex
		sendExceptionData(ex, "uninstManagerApp")
	}
}

def initWatchdogApp() {
	LogTrace("initWatchdogApp")
	def watDogApp = getChildApps()?.findAll { it?.getAutomationType() == "watchDog" }
	if(watDogApp?.size() < 1) {
		LogAction("Installing Watchdog App", "info", true)
		addChildApp(appNamespace(), autoAppName(), getWatDogAppChildName(), [settings:[watchDogFlag:["type":"bool", "value":true]]])
	} else if(watDogApp?.size() >= 1) {
		def cnt = 1
		watDogApp?.each { chld ->
			if(cnt == 1) {
				//LogAction("Running Update Command on Watchdog", "warn", true)
				chld.update()
			} else if(cnt > 1) {
				LogAction("Deleting Extra Watchdog (${chld?.id})", "warn", true)
				deleteChildApp(chld)
			}
			cnt = cnt+1
		}
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
	def dat = ["nestMode":0,"watchDog":0, "disabled":0, "schMot":["tSched":0, "remSen":0, "fanCtrl":0, "fanCirc":0, "conWat":0, "extTmp":0, "leakWat":0, "humCtrl":0]]
	def disItems = []
	def nItems = [:]
	def schMotItems = []
	atomicState?.autoSaVer = minVersions()?.automation?.desc
	childApps?.each { a ->
		def type = a?.getAutomationType()
		def ver
		def dis
		try {
			dis = a?.getIsAutomationDisabled()
			ver = a?.appVersion()
		}
		catch(ex) {
			dis = null
			ver = null
			type = "old"
		}
		if(ver) {
			def updVer = atomicState?.autoSaVer ?: ver
			if(versionStr2Int(ver) < versionStr2Int(updVer)) {
				updVer = ver
			}
			atomicState.autoSaVer = updVer
		}

		if(versionStr2Int(ver) < minVersions()?.automation?.val) {
			LogAction("NEED SOFTWARE UPDATE: Automation ${a?.label} (v${ver}) REQUIRED: (v${minVersions()?.automation?.desc}) Update the NST automation to latest", "error", true)
			appUpdateNotify()
		}

		if(dis) {
			disItems.push(a?.label.toString())
			dat["disabled"] = dat["disabled"] ? dat["disabled"]+1 : 1
		} else {
			switch(type) {
				case "nMode":
					dat["nestMode"] = dat["nestMode"] ? dat["nestMode"]+1 : 1
					break
				case "schMot":
					def ai = a?.getAutomationsInstalled()
					schMotItems += a?.getSchMotConfigDesc(true)
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
 					}
 					break
 				default:
 					LogAction("Deleting Unknown Automation (${a?.id})", "warn", true)
 					deleteChildApp(a)
					break
			}
		}
	}
	atomicState?.installedAutomations = dat

	def str = ""
	str += (dat?.watchDog > 0 || dat?.nestMode > 0 || dat?.schMot || dat?.disabled > 0) ? "Installed Automations:" : ""
	str += (dat?.watchDog > 0) ? "\n• Watchdog (Active)" : ""
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
	if(atomicState.appData?.aaPrefs?.enMultiQueue && settings?.allowAskAlexaMQ) {
		subscribe(location, "askAlexaMQ", askAlexaMQHandler) //Refreshes list of available AA queues
	}
	if(settings?.restStreaming && !getRestHost()) {
		restSrvcSubscribe()
	}
}

private adj_temp(tempF) {
	if(getTemperatureScale() == "C") {
		return (tempF - 32) * 5/9 as Double
	} else {
		return tempF
	}
}

def setPollingState() {
	if(!atomicState?.thermostats && !atomicState?.protects && !atomicState?.weatherDevice && !atomicState?.cameras) {
		LogAction("No Devices Selected; Polling is OFF", "info", true)
		unschedule("poll")
		atomicState.pollingOn = false
		atomicState.streamPolling = false
	} else {
		if(!atomicState?.pollingOn) {
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
			def timgcd = gcd([pollTime, pollStrTime, weatherTimer])
			def random = new Random()
			def random_int = random.nextInt(60)
			timgcd = (timgcd.toInteger() / 60) < 1 ? 1 : timgcd.toInteger()/60
			def random_dint = random.nextInt(timgcd.toInteger())
			LogAction("POLL scheduled (${random_int} ${random_dint}/${timgcd} * * * ?)", "info", true)
			schedule("${random_int} ${random_dint}/${timgcd} * * * ?", poll)	// this runs every timgcd minutes
			poll(true)
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
	fixStuckMigration()
	poll(true)
	/*
		NOTE:
		This runin is used strictly for testing as it calls the cleanRestAutomationTest() method
		which will remove any New migrated automations and restore the originals back to active
		and clear the flags that marked the migration complete.
		FYI: If allowMigration() is set to true it will attempt to run a migration

	runIn(3, "cleanRestAutomationTest",[overwrite: true])
	*/
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

def fixStuckMigration() {
	if(atomicState?.migrationInProgress == true) {
		atomicState?.migrationInProgress = false
		atomicState?.pollBlocked = false
		atomicState?.autoMigrationComplete = true
		def t0 = atomicState?.installData
		t0["usingNewAutoFile"] = true
		atomicState?.installData = t0
	}
}

def cleanRestAutomationTest() {
	/*
		NOTE:
		This is only here to allow testing.
		It will be removed after testing is complete
	*/
	//log.trace "cleanRestAutomationTest..."
	def cApps = getChildApps()
	atomicState?.pollBlocked = true
	atomicState?.migrationInProgress = true
	atomicState?.autoMigrationComplete = true
	def foundAll = true
	if(keepBackups() == false) {
		cApps.each { ca ->
			def restId = ca?.getSettingVal("restoreId")
			if(restId == null) {
 				foundAll = false
			}
		}
		if(foundAll) {
			cApps.each { ca ->
				def restId = ca?.getSettingVal("restoreId")
				if(restId != null) {
					ca?.settingUpdate("restoreId", null)
				}
			}
		}
	}
	cApps.each { ca ->
		def restId = ca?.getSettingVal("restoreId")
		if(restId != null) {
			LogAction("CleanRestAutomationTest: removing ${ca?.label} ${restId}", "warn", true)
			deleteChildApp(ca)
		}
		else {
			LogAction("CleanRestAutomationTest: enabling ${ca?.label} ${restId}", "warn", true)
			ca?.settingUpdate("disableAutomationreq", "false", "bool")
			ca?.stateUpdate("disableAutomation", false)
			ca?.stateUpdate("disableAutomationDt", null)
			ca?.update()
		}
	}
	atomicState?.migrationInProgress = false
	atomicState?.pollBlocked = false
	atomicState?.autoMigrationComplete = false
	def t0 = atomicState?.installData
	t0["usingNewAutoFile"] = false
	atomicState?.installData = t0
}

def checkIfSwupdated() {
	if(checkMigrationRequired()) { return true }
	if(atomicState?.swVersion != appVersion()) {
		LogAction("checkIfSwupdated: new version ${appVersion()}", "info", true)
		def iData = atomicState?.installData
		iData["updatedDt"] = getDtNow().toString()
		iData["shownChgLog"] = false
		atomicState?.installData = iData
		def cApps = getChildApps()
		if(cApps) {
			cApps?.sort()?.each { chld ->
				chld?.update()
			}
		}
		updated()
		return true
	}
	return false
}

/* NOTE:
	MIGRATION Pre-Check
	This will be called as part of the version change logic.
	It looks to see if the file version is greater than a set number and that the atomicState.autoMigrationComplete is false or null,
	If the to are ok it schedules the "doAutoMigrationProcess" method for 5 seconds.
*/
def checkMigrationRequired() {
	if(atomicState?.migrationInProgress == true) { return true }
	else if(atomicState?.installData?.usingNewAutoFile == true) { return false }
	if(allowMigration()) {
		if((versionStr2Int(appVersion()) >= 454 && !atomicState?.autoMigrationComplete == true)) {
			LogAction("checkMigrationRequired: Scheduled Migration Process to New Automation File...(5 seconds)", "info", true)
			//atomicState?.migrationInProgress == true
			runIn(5, "doAutoMigrationProcess", [overwrite: true])
			return true
		}
	}
	return false
}

/* NOTE:
	This is method creates the settings map with these items [type, value] so it can be easily restored.
	The input reference data used is stored on our firebase
*/
def buildSettingsMap() {
	def inputData = getWebData([uri: "https://st-nest-manager.firebaseio.com/restoreInputData.json", contentType:"application/json"], "inputType", false)
	def settingsMap = [:]
	def setData = getSettings()?.sort()?.findAll { it }
	setData?.sort().each { item ->
		def itemType = inputData?.inputs?.find { item?.key.toString().contains(it?.key.toString()) }
		settingsMap[item?.key] = ["type":itemType?.value, "value":item?.value]
	}
	return settingsMap
}

/* NOTE:
	This is method creates the JSON that is sent to Firebase with the Settings and State data
*/
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
		//log.debug "item: $item"
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
	//setData["backedUpData"] = true
	def data = [:]
	data["appLabel"] = app.label
	data["stateData"] = stateData
	data["settingsData"] = setData
	data["backupDt"] = getDtNow()
	def resultJson = new groovy.json.JsonOutput().toJson(data)
	//log.debug "resultJson: $resultJson"
	return resultJson
}

// Child Method
// This is only necessary in the manager code to allow the backup to be created for migration to the new automation file
def backupConfigToFirebase() {
	//log.trace "backupConfigToFirebase..."
	unschedule()
	unsubscribe()
	uninstAutomationApp() 			// Cleanup any parent state this child owns
	def data = createAutoBackupJson()
	return parent?.sendAutomationBackupData(data, app.id)
}

//Manager only
def sendAutomationBackupData(data, appId) {
	try {
		sendFirebaseData(data, "backupData/clients/${atomicState?.installationId}/automationApps/${appId}.json", null, "Automation ($appId) Backup", true)
	} catch (ex) {
		LogAction("sendAutomationBackupData Exception: ${ex}", "error", true)
		return false
	}
}

def removeAutomationBackupData(childId, lbl=null) {
	LogAction("removeAutomationBackupData(${lbl ? "$lbl" : "$childId"})", "info", true)
	return removeFirebaseData("backupData/clients/${atomicState?.installationId}/automationApps/${childId}.json")
}

def clearAllAutomationBackupData() {
	LogAction("cleanAllAutomationBackupData()...", "trace", true)
	return removeFirebaseData("backupData/clients/${atomicState?.installationId}.json")
}

def getAutomationBackupData() {
	return getWebData([uri: "https://st-nest-manager.firebaseio.com/backupData/clients/${atomicState?.installationId}/automationApps.json", contentType:"application/json"], "getAutomationBackup", false)
}

def migrationInProgress() {
	return atomicState?.migrationInProgress == true ? true : false
}

/*
	NOTE: MIGRATION STEP 1
	This is the process that is called to kick off the backup/restore process.
	It set the state values of pollBlocked and migrationInProgress to true to prevent any polling
	PARENT METHOD
*/
void doAutoMigrationProcess() {
	LogAction("doAutoMigrationProcess...", "trace", true)
	if(atomicState?.migrationInProgress == true) { LogAction("Migration already in progress", "error", true) }
	atomicState?.pollBlocked = true
	atomicState?.migrationInProgress = true
	atomicState?.migrationState1 = "Step 1 Start"

	// This is to control the parent/child state to reset using fixState()
	atomicState?.resetAllData = false
	settingUpdate("resetAllData", "false")

	def cApps = getChildApps()
	if(cApps) {
		cApps?.each { ca ->
			def t0 = ca?.settings?.restoredFromBackup
			atomicState?.migrationState1 = "Step 1 Backup - ${ca.label}"
			if(t0 == null && backupAutomation(ca)) {
				LogAction("backed up ${ca?.label}", "debug", true)
			} else {
				if(t0) { LogAction("skipping backup of the new style automation ${ca.label}", "debug", true) }
				else { LogAction("backup failed of automation ${ca.label}", "warn", true) }
			}
		}
		atomicState?.migrationState1 = "Step 1 Finish - Restore Scheduled"
		runIn(15, "processAutoRestore", [overwrite:true])
		LogAction("Scheduled restore process for (15 seconds)...", "info", true)
	} else {
		atomicState?.migrationState1 = "Step 1 Finish - Nothing to Restore"
		LogAction("There are no automations to restore.", "warn", true)
		finishMigrationProcess(false)
	}
}

/*
	NOTE: MIGRATION STEP 2
	This is the process calls the backupConfigToFirebase method on every child
	PARENT METHOD
*/
def backupAutomation(child) {
	if(child?.backupConfigToFirebase()) {
		child?.stateUpdate("lastBackupDt", getDtNow())
		return true
	}
	return false
}

/*
	NOTE: MIGRATION STEP 3
	This process calls the automationRestore method with all of the backup data to restore
	PARENT METHOD
*/
void processAutoRestore() {
	LogAction("processAutoRestore...", "trace", true)
	atomicState?.migrationState3 = "Step 3 Start"
	def backupData = getAutomationBackupData()
	if(backupData instanceof List || backupData instanceof Map) {
		atomicState?.migrationState3 = "Step 3 Automation Restore"
		automationRestore(backupData)
	}
	atomicState?.migrationState3 = "Step 3 Finish"
}

/*
	NOTE: MIGRATION STEP 4
	This is the actual automation restore method for installing the automations from the backups
	It loops through each backed up automation id and creates the map to send and creates the new automation
	using the new file.
	PARENT METHOD
*/
def automationRestore(data, id=null) {
	LogAction("automationRestore... size: ${data?.size()}", "trace", true)
	try {
		if(data) {
			data?.each { bApp ->
				if(id && id.toString() != bApp?.key.toString()) { return }
				def appLbl = bApp?.value?.appLabel.toString()
				def setData = bApp?.value?.settingsData
				setData["restoreId"] = ["type":"text", "value":bApp?.key]
				setData["restoredFromBackup"] = ["type":"bool", "value":true]
				setData["restoreCompleted"] = ["type":"bool", "value":false]
				setData["automationTypeFlag"] = ["type":"text", "value":setData?.automationTypeFlag]

				atomicState?.migrationState4 = "Step 4 Automation Restore - Restoring [${setData?.automationTypeFlag?.value}] Automation Named: ($appLbl)...."
				LogAction("Restoring [${setData?.automationTypeFlag?.value}] Automation Named: ($appLbl)....", "info", true)
				// log.debug "setData: $setData"
				addChildApp(appNamespace(), autoAppName(), "${appLbl} (NST)", [settings:setData])
				postChildRestore(bApp?.key)
			}
			atomicState?.migrationState4 = "Step 4 Automation Restore - Finishing"
			runIn(25, "finishMigrationProcess", [overwrite:true])
			LogAction("Scheduling finishMigrationProcess for (25 seconds)...", "debug", true)
			return true
		}
	} catch (ex) {
		log.error "automationRestore Exception:", ex
	}
	return false
}

/*
	NOTE: MIGRATION STEP 5
	This is called by the child automations initAutoApp() method after the addChildApp()
	creates that app from backup.  On the first initialazation of the child it calls the
	parent to restore the stateData from backup.   BACKUP MUST STILL EXIST
	PARENT METHOD
*/
def callRestoreState(child, restId) {
	atomicState?.migrationState5 = "Step 5 callRestoreState - Start ${child.label}   RestoreID: ${restId}"
	LogAction("callRestoreState ${child.label}   RestoreID: ${restId}", "trace", true)
	//log.debug "child: [Name: ${child.label} || ID: ${child?.getId()} | RestoreID: $restId"
	if(restId) {
		def data = getAutomationBackupData()
		//log.debug "callRestoreState data: $data"
		def newData = data.find { it?.key?.toString() == restId?.toString() }
		if(newData?.value?.stateData) {
			atomicState?.migrationState5 = "Step 5 callRestoreState - restoring child ${child.label} state  RestoreID: ${restId}"
			newData?.value?.stateData?.each { sKey ->
				child?.stateUpdate(sKey?.key, sKey?.value)
			}
			return true
		} else {
			atomicState?.migrationState5 = "Step 5 callRestoreState - no backup data child ${child.label} RestoreID: ${restId}"
			LogAction("Backup Data not found: ${child.label}   RestoreID: ${restId}", "error", true)
		}
	}
	return false
}

/*
	NOTE: MIGRATION STEP 3  (Really part of STEP 3)
	This is called by the child once it's state data has been restored and it's purpose is
	finalize the restore setting values and disable/or remove the old automations.
	The removal is controlled by the method keepBackups().
	PARENT METHOD
*/
def postChildRestore(childId) {
	atomicState?.migrationState3A = "Step 3A Start postChildRestore(childId: $childId)"
	LogAction("postChildRestore(childId: $childId)", "trace", true)
	def cApp = getChildApps()
	cApp?.each { ca ->
		atomicState?.migrationState3A = "Step 3A postChildRestore Checking Automation (${ca?.label})..."
		LogAction("postChildRestore Checking Automation (${ca?.label})...", "info", true)
		if(ca?.getId() == childId) {
			if(keepBackups() == false) {
				atomicState?.migrationState3A = "Step 3A postChildRestore Removing Old Automation (${ca?.label})..."
				LogAction("postChildRestore Removing Old Automation (${ca?.label})...", "warn", true)
				deleteChildApp(ca)
			} else {
				ca?.settingUpdate("disableAutomationreq", "true", "bool")
				ca?.stateUpdate("disableAutomation", true)
				ca?.stateUpdate("disableAutomationDt", getDtNow())
				ca?.update()
			}
		} else {
			atomicState?.migrationState3A = "Step 3A postChildRestore No Match for Automation (${ca?.label})..."
			LogAction("postChildRestore No Match for Automation (${ca?.label})...", "info", true)
		}
	}
}

/*
	NOTE: MIGRATION 6
	This is the final part of the migration process. It's supposed to if Successful restore polling state,
	and mark the migration complete.  Otherwise it leaves them set so the migration will try again after the
	update is called.
	PARENT METHOD
*/
void finishMigrationProcess(result=true) {
	atomicState?.migrationState6 = "Step 6 start - finishMigrationProcess result: $result"
	LogAction("finishMigrationProcess result: $result", "trace", true)
	if(result) {
		LogAction("Auto Migration Process is complete...", "info", true)
		def t0 = atomicState?.installData
		t0["usingNewAutoFile"] = true
		atomicState?.installData = t0
	} else {
		LogAction("Auto Migration did not do anything...", "warn", true)
	}
	atomicState?.pollBlocked = false
	atomicState?.migrationInProgress = false
	atomicState?.autoMigrationComplete = true

	// This is to force the parent/child state to reset using fixState()
	atomicState?.resetAllData = false
	settingUpdate("resetAllData", "true")

	// This will perform a cleanup of any backup data that wasn't removed
	if(keepBackups() == false) { clearAllAutomationBackupData() }
	app.update()
}

def poll(force = false, type = null) {
	if(isPollAllowed()) {
		if(checkIfSwupdated()) { return }

		if(force == true) { forcedPoll(type); finishPoll(); return  }

		def pollTime = !settings?.pollValue ? 180 : settings?.pollValue.toInteger()
		if(settings?.restStreaming && atomicState?.restStreamingOn) {
			pollTime = 60*5
		}
		def pollTimeout = pollTime + 85

		if(getLastHeardFromNestSec() > pollTimeout) {
			if(settings?.restStreaming && atomicState?.restStreamingOn) {
				LogAction("Have not heard from Rest Stream - Sending restStreamHandler(Stop) Event to local node service", "warn", true)
				restStreamHandler(true)   // close the stream if we have not heard from it in a while
				atomicState?.restStreamingOn = false
			}
		}

		if(atomicState?.streamPolling && (!settings?.restStreaming || !atomicState?.restStreamingOn)) {	// return to normal polling
			unschedule("poll")
			atomicState.pollingOn = false
			setPollingState()		// will call poll
			return
		}

		if(settings?.restStreaming && atomicState?.restStreamingOn) {
			LogAction("Skipping Poll because Rest Streaming is ON", "info", false)
			if(!atomicState?.streamPolling) {	// set to stream polling
				unschedule("poll")
				atomicState.pollingOn = false
				setPollingState()		// will call poll
				return
			}
			restStreamCheck()
			finishPoll()
			return
		}
		startStopStream()

		//def pollStrTime = !settings?.pollStrValue ? 180 : settings?.pollStrValue.toInteger()
		//if(pollTime < 60 || pollStrTime < 60) {
		if(pollTime < 60 && inReview() && !atomicState?.apiRateLimited) {
			if(atomicState?.pollTock) {
				atomicState.pollTock = false
				runIn(30, "pollFollow", [overwrite: true])
			} else {
				atomicState.pollTock = true
			}
		}

		def okStruct = ok2PollStruct()
		def okDevice = ok2PollDevice()
		def okMeta = ok2PollMetaData()
		def meta = false
		def dev = false
		def str = false
		if(!okDevice && !okStruct  && !(getLastHeardFromNestSec() > pollTimeout*2)) {
			LogAction("No Device or Structure poll - Devices Last Updated: ${getLastDevicePollSec()} seconds ago | Structures Last Updated ${getLastStructPollSec()} seconds ago", "info", true)
		}
		else {
			def allowAsync = false
			def metstr = "sync"
			def sstr = ""
			if(atomicState?.appData && atomicState?.appData?.pollMethod?.allowAsync) {
				allowAsync = true
				metstr = "async"
			}
			if(okStruct) {
				sstr += "Updating Structure Data (Last Updated: ${getLastStructPollSec()} seconds ago)"
				if(allowAsync) {
					str = queueGetApiData("str")
				} else {
					str = getApiData("str")
				}
			}
			if(okDevice) {
				sstr += sstr != "" ? " | " : ""
				sstr += "Updating Device Data (Last Updated: ${getLastDevicePollSec()} seconds ago)"
				if(allowAsync) {
					dev = queueGetApiData("dev")
				} else {
					dev = getApiData("dev")
				}
			}
			if(okMeta) {
				sstr += sstr != "" ? " | " : ""
				sstr += "Updating Meta Data(Last Updated: ${getLastMetaPollSec()} seconds ago)"
				if(allowAsync) {
					meta = queueGetApiData("meta")
				} else {
					meta = getApiData("meta")
				}
			}
			if(sstr != "") { LogAction("${sstr} (${metstr})", "info", true) }
			if(allowAsync) { return }
		}
		finishPoll(str, dev)
	} else if(atomicState?.clientBlacklisted) {
		LogAction("Client poll is BLACKLISTED.  Please contact the Developer", "warn", true)
		finishPoll(false, true)
	}
}

def finishPoll(str=null, dev=null) {
	LogTrace("finishPoll($str, $dev) received")
	if(atomicState?.pollBlocked) { LogAction("Poll BLOCKED", "trace", true); schedNextWorkQ(null); return }
	if(dev || str || atomicState?.forceChildUpd || atomicState?.needChildUpd) { updateChildData() }
	updateWebStuff()
	notificationCheck() //Checks if a notification needs to be sent for a specific event
	broadcastCheck()
}

def finishPollHandler(data) {
	def dev = data?.dev
	finishPoll(false, dev)
}

def schedFinishPoll(devChg) {
	def curNow = now()
	atomicState?.lastFinishedPoll = curNow
	finishPoll(false, devChg)
	return
/*
	if(!atomicState?.lastFinishedPoll || curNow >= atomicState?.lastFinishedPoll + 3400) {
		def devFlg = [dev:devChg]
		runIn(4, "finishPollHandler", [overwrite: true, data: devFlg])
		atomicState?.lastFinishedPoll = curNow
	}
*/
}

def forcedPoll(type = null) {
	LogAction("forcedPoll($type) received", "warn", true)
	def lastFrcdPoll = getLastForcedPollSec()
	def pollWaitVal = !settings?.pollWaitVal ? 10 : settings?.pollWaitVal.toInteger()
	pollWaitVal = Math.max(pollWaitVal, 10)

	if(lastFrcdPoll > pollWaitVal) { // This limits manual forces to 10 seconds or more
		atomicState?.lastForcePoll = getDtNow()
		atomicState?.pollBlocked = false
		cmdProcState(false)

		LogAction("Last Forced Update was ${lastFrcdPoll} seconds ago.", "info", true)
		if(type == "dev" || !type) {
			LogAction("Update Device Data", "info", true)
			getApiData("dev")
		}
		if(type == "str" || !type) {
			LogAction("Update Structure Data", "info", true)
			getApiData("str")
		}
		if(type == "meta" || !type) {
			LogAction("Update Meta Data", "info", true)
			getApiData("meta")
		}
		atomicState?.lastWebUpdDt = null
		atomicState?.lastWeatherUpdDt = null
		atomicState?.lastForecastUpdDt = null
		schedNextWorkQ(null)
	} else {
		LogAction("Too Soon for Update; Elapsed (${lastFrcdPoll}) seconds; minimum (${settings?.pollWaitVal})", "debug", true)
		atomicState.needStrPoll = true
		atomicState.needDevPoll = true
	}
	atomicState.forceChildUpd = true
	updateChildData()
}

def postCmd() {
	//LogTrace("postCmd()")
	poll()
}

def getApiData(type = null) {
	LogTrace("getApiData($type)")
	LogAction("getApiData($type)", "info", false)
	def result = false
	if(!type || !atomicState?.authToken) { return result }

	def tPath = (type == "str") ? "/structures" : ((type == "dev") ? "/devices" : "/")
	try {
		def params = [
			uri: getNestApiUrl(),
			path: "$tPath",
			headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState?.authToken}"]
		]
		if(type == "str") {
			httpGet(params) { resp ->
				//def rCode = resp?.status ?: null
				//def errorMsg = resp?.errorMessage ?: null
				if(resp?.status == 200) {
					apiIssueEvent(false)
					atomicState?.apiRateLimited = false
					atomicState?.apiCmdFailData = null
					def t0 = resp?.data
					//LogTrace("API Structure Resp.Data: ${t0}")
					def chg = didChange(atomicState?.structData, t0, "str")
					if(chg) {
						LogAction("API Structure Data HAS Changed", "info", true)
						result = true
						atomicState.structName = atomicState?.structData && atomicState?.structures ? atomicState?.structData[atomicState?.structures]?.name : null
						locationPresNotify(getLocationPresence())
					}
					incApiStrReqCnt()
				} else {
					LogAction("getApiStructureData - Received: Resp (${resp?.status})", "error", true)
					apiRespHandler(resp?.status, resp?.data, "getApiData(str)")
				}
			}
		}
		else if(type == "dev") {
			httpGet(params) { resp ->
				//def rCode = resp?.status ?: null
				//def errorMsg = resp?.errorMessage ?: null
				if(resp?.status == 200) {
					atomicState?.lastHeardFromNestDt = getDtNow()
					apiIssueEvent(false)
					atomicState?.apiRateLimited = false
					atomicState?.apiCmdFailData = null
					def t0 = resp?.data
					//LogTrace("API Device Resp.Data: ${t0}")
					def chg = didChange(atomicState?.deviceData, t0, "dev")
					if(chg) {
						LogAction("API Device Data HAS Changed", "info", true)
						result = true
					}
					incApiDevReqCnt()
				} else {
					LogAction("getApiDeviceData - Received Resp (${resp?.status})", "error", true)
					apiRespHandler(resp?.status, resp?.data, "getApiData(dev)")
				}
			}
		}
		else if(type == "meta") {
			httpGet(params) { resp ->
				//def rCode = resp?.status ?: null
				//def errorMsg = resp?.errorMessage ?: null
				if(resp?.status == 200) {
					//LogTrace("API Metadata Resp.Data: ${resp?.data}")
					apiIssueEvent(false)
					atomicState?.apiRateLimited = false
					atomicState?.apiCmdFailData = null
					def nresp = resp?.data?.metadata
					def chg = didChange(atomicState?.metaData, nresp, "meta")
					if(chg) {
						LogAction("API Meta Data HAS Changed", "info", true)
						result = true
					}
					incApiMetaReqCnt()
				} else {
					LogAction("getApiMetaData - Received Resp (${resp?.status})", "error", true)
					apiRespHandler(resp?.status, resp?.data, "getApiData(meta)")
				}
			}
		}
	} catch (ex) {
		apiIssueEvent(true)
		atomicState?.apiRateLimited = false
		atomicState.forceChildUpd = true
		if(ex instanceof groovyx.net.http.HttpResponseException) {
			if(ex?.response) {
				apiRespHandler(ex?.response?.status, ex?.response?.data, "getApiData(ex catch)")
			}
			// if(ex.message.contains("Too Many Requests")) {
			// 	LogAction("Received '${ex.message}' response", "warn", true)
			// }
		} else {
			log.error "getApiData (type: $type) Exception:", ex
			if(type == "str") { atomicState.needStrPoll = true }
			else if(type == "dev") { atomicState?.needDevPoll = true }
			else if(type == "meta") { atomicState?.needMetaPoll = true }
			sendExceptionData(ex, "getApiData")
		}
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
			atomicState.qmetaRequested = true
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
			atomicState?.lastHeardFromNestDt = getDtNow()
			apiIssueEvent(false)
			atomicState?.apiRateLimited = false
			atomicState?.apiCmdFailData = null
			if(type == "str") {
				def t0 = resp?.json
				//LogTrace("API Structure Resp.Data: ${t0}")
				def chg = didChange(atomicState?.structData, t0, "str")
				if(chg) {
					LogAction("API Structure Data HAS Changed", "info", true)
					str = true
					atomicState.structName = atomicState?.structData && atomicState?.structures ? atomicState?.structData[atomicState?.structures]?.name : null
					locationPresNotify(getLocationPresence())
				}
				atomicState.qstrRequested = false
				incApiStrReqCnt()
			}
			if(type == "dev") {
				def t0 = resp?.json
				//LogTrace("API Device Resp.Data: ${t0}")
				def chg = didChange(atomicState?.deviceData, t0, "dev")
				if(chg) {
					LogAction("API Device Data HAS Changed", "info", true)
					dev = true

			//		atomicState.thermostats =  settings?.thermostats ? statState(settings?.thermostats) : null
			//		atomicState.protects = settings?.protects ? coState(settings?.protects) : null
			//		atomicState.cameras = settings?.cameras ? camState(settings?.cameras) : null

				}
				atomicState.qdevRequested = false
				incApiDevReqCnt()
			}
			if(type == "meta") {
				def nresp = resp?.json?.metadata
				//LogTrace("API Meta Resp.Data: ${resp?.json}")
				def chg = didChange(atomicState?.metaData, nresp, "meta")
				if(chg) {
					LogAction("API Meta Data HAS Changed", "info", true)
					meta = true
				}
				atomicState.qmetaRequested = false
				incApiMetaReqCnt()
			}
		} else {
			def tstr = (type == "str") ? "Structure" : ((type == "dev") ? "Device" : "Metadata")
			//LogAction("procNestResponse - Received $tstr poll: Resp (${resp?.status})", "error", true)
			if(resp?.hasError()) {
				def rCode = resp?.getStatus() ?: null
				def errJson = resp?.getErrorJson() ?: null
				//log.debug "rCode: $rCode | errJson: $errJson"
				apiRespHandler(rCode, errJson, "procNestResponse($type)")
			}
			apiIssueEvent(true)
			atomicState.forceChildUpd = true
			atomicState.qstrRequested = false
			atomicState.qdevRequested = false
			atomicState.qmetaRequested = false
		}
		if((atomicState?.qdevRequested == false && atomicState?.qstrRequested == false) && (dev || atomicState?.forceChildUpd || atomicState?.needChildUpd)) {
			finishPoll(true, true)
		}

	} catch (ex) {
		//log.error "procNestResponse (type: $type) Exception:", ex
		apiIssueEvent(true)
		atomicState?.apiRateLimited = false
		atomicState.forceChildUpd = true
		atomicState.qstrRequested = false
		atomicState.qdevRequested = false
		atomicState.qmetaRequested = false

		if(type == "str") { atomicState.needStrPoll = true }
		else if(type == "dev") { atomicState?.needDevPoll = true }
		else if(type == "meta") { atomicState?.needMetaPoll = true }
		sendExceptionData(ex, "procNestResponse_${type}")
	}
}

def receiveEventData() {
	def evtData = request?.JSON
	def devChgd = false
	def gotSomething = false
	if(evtData?.data && settings?.restStreaming) {

		//def t0 = atomicState?.aaOldStreamData
		//whatChanged(t0, evtData, "/")
		//atomicState.aaOldStreamData = evtData
		//state.remove("aaOldStreamData")

		if(evtData?.data?.devices) {
			//LogTrace("API Device Resp.Data: ${evtData?.data?.devices}")
			gotSomething = true
			def chg = didChange(atomicState?.deviceData, evtData?.data?.devices, "dev")
			if(chg) {
				devChgd = true
				LogAction("API Device Data HAS Changed (Stream)", "info", true)
			} else {
				LogTrace("got deviceData")
			}
		}
		if(evtData?.data?.structures) {
			//LogTrace("API Structure Resp.Data: ${evtData?.data?.structures}")
			gotSomething = true
			def chg = didChange(atomicState?.structData, evtData?.data?.structures, "str")
			if(chg) {
				LogAction("API Structure Data HAS Changed (Stream)", "info", true)
				atomicState.structName = atomicState?.structData && atomicState?.structures ? atomicState?.structData[atomicState?.structures]?.name : null
				locationPresNotify(getLocationPresence())
			} else {
				LogTrace("got structData")
			}
		}
		if(evtData?.data?.metadata) {
			//LogTrace("API Metadata Resp.Data: ${evtData?.data?.metadata}")
			gotSomething = true
			def chg = didChange(atomicState?.metaData, evtData?.data?.metadata, "meta")
			if(chg) {
				LogAction("API META Data HAS Changed (Stream)", "info", true)
			} else {
				LogTrace("got metaData")
			}
		}
	} else {
		LogAction("Sending restStreamHandler(Stop) Event to local node service", "debug", true)
		restStreamHandler(true)
	}
	if(gotSomething) {
		atomicState?.lastHeardFromNestDt = getDtNow()
		if(atomicState?.ssdpOn == true) {
			unsubscribe() //These were causing exceptions
			atomicState.ssdpOn = false
			subscriber()
		}
		apiIssueEvent(false)
		atomicState?.apiRateLimited = false
		atomicState?.apiCmdFailData = null
		incRestStrEvtCnt()
	}
	if(atomicState?.forceChildUpd || atomicState?.needChildUpd || devChgd) {
		schedFinishPoll(devChgd)
	}
	render contentType: 'text/html', data: "status received...ok", status: 200
}

def didChange(old, newer, type) {
	def result = false
	if(newer != null) {
		if(type == "str") {
			atomicState?.lastStrucDataUpd = getDtNow()
			atomicState.needStrPoll = false
		}
		if(type == "dev") {
			atomicState?.lastDevDataUpd = getDtNow()
			atomicState?.needDevPoll = false
		}
		if(type == "meta") {
			atomicState?.lastMetaDataUpd = getDtNow()
			atomicState.needMetaPoll = false
		}
		if(old != newer) {
			if(type == "str") {
				def t0 = atomicState?.structData && atomicState?.structures ? atomicState?.structData[atomicState?.structures] : null
				def t1 = newer && atomicState?.structures ? newer[atomicState?.structures] : null
				if(t1 && t0 != t1) {
					result = true
					atomicState?.forceChildUpd = true
					LogTrace("structure old newer not the same ${atomicState?.structures}")
					//whatChanged(t0, t1, "/structures")
				}
				atomicState?.structData = newer
			}
			else if(type == "dev") {
				def tstats = atomicState?.thermostats.collect { dni ->
					def t1 = dni.key
					if(t1 && old && old?.thermostats && newer?.thermostats &&
						old?.thermostats[t1] && newer?.thermostats[t1] && old?.thermostats[t1] == newer?.thermostats[t1]) {
						;
					} else {
						result = true
						atomicState.needChildUpd = true
						LogTrace("thermostat old newer not the same ${t1}")
						if(t1 && old && old?.thermostats && newer?.thermostats && old?.thermostats[t1] && newer?.thermostats[t1]) {
							//whatChanged(old?.thermostats[t1], newer?.thermostats[t1], "/devices/thermostats/${t1}")
						}
					}
				}

				def nProtects = atomicState?.protects.collect { dni ->
					def t1 = dni.key
					if(t1 && old && old?.smoke_co_alarms && newer?.smoke_co_alarms &&
						old?.smoke_co_alarms[t1] && newer?.smoke_co_alarms[t1] && old?.smoke_co_alarms[t1] == newer?.smoke_co_alarms[t1]) {
						;
					} else {
						result = true
						atomicState.needChildUpd = true
						LogTrace("protect old newer not the same ${t1}")
						if(t1 && old && old?.smoke_co_alarms && newer?.smoke_co_alarms && old?.smoke_co_alarms[t1] && newer?.smoke_co_alarms[t1]) {
							//whatChanged(old?.smoke_co_alarms[t1], newer?.smoke_co_alarms[t1], "/devices/smoke_co_alarms/${t1}")
						}
					}
				}

				def nCameras = atomicState?.cameras.collect { dni ->
					def t1 = dni.key
					if(t1 && old && old?.cameras && newer?.cameras &&
						old?.cameras[t1] && newer?.cameras[t1] && old?.cameras[t1] == newer?.cameras[t1]) {
						;
					} else {
						result = true
						atomicState.needChildUpd = true
						LogTrace("camera old newer not the same ${t1}")
						if(t1 && old && old?.cameras && newer?.cameras && old?.cameras[t1] && newer?.cameras[t1]) {
							//whatChanged(old?.cameras[t1], newer?.cameras[t1], "/devices/cameras/${t1}")
						}
					}
				}
				atomicState?.deviceData = newer

			}
			else if(type == "meta") {
				result = true
				atomicState.needChildUpd = true
				atomicState.metaData = newer
				//whatChanged(old, newer, "/metadata")
			}
		}
	}
	return result
}

def whatChanged(mapA, mapB, headstr) {
	def t0 = mapA
	def t1 = mapB
	def left = t0
	def right = t1

	if(left == null || right == null) {
		LogAction("Object: $headstr  NULL", "trace", true)
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
		atomicState?.lastChildUpdDt = getDtNow()
		def useMt = !useMilitaryTime ? false : true
		def dbg = !childDebug ? false : true
		def logNamePrefix = (settings?.debugAppendAppName || settings?.debugAppendAppName == null) ? true : false
		def remDiag = (atomicState?.appData?.database?.allowRemoteDiag && atomicState?.enRemDiagLogging) ? true: false
		def nestTz = getNestTimeZone()?.toString()
		def api = !apiIssues() ? false : true
		def htmlInfo = getHtmlInfo()
		def mobClientType = settings?.mobileClientType
		def vRprtPrefs = getVoiceRprtPrefs()
		def clientBl = atomicState?.clientBlacklisted == true ? true : false
		def hcCamTimeout = atomicState?.appData?.healthcheck?.camTimeout ?: 35
		def hcProtWireTimeout = atomicState?.appData?.healthcheck?.protWireTimeout ?: 35
		def hcProtBattTimeout = atomicState?.appData?.healthcheck?.protBattTimeout ?: 35
		def hcTstatTimeout = atomicState?.appData?.healthcheck?.tstatTimeout ?: 35
		def hcLongTimeout = atomicState?.appData?.healthcheck?.longTimeout ?: 3600
		def locPresence = getLocationPresence()
		def nPrefs = atomicState?.notificationPrefs
		def devBannerData = atomicState?.devBannerData ?: null
		def streamingActive = atomicState?.restStreamingOn == true ? true : false

		def curWeatherTemp
		if(atomicState?.thermostats && getWeatherDeviceInst()) {
			def cur = getWData()
			if(cur) {
				curWeatherTemp = getTemperatureScale() == "C" ? (cur?.current_observation?.temp_c ? Math.round(cur?.current_observation?.temp_c.toDouble()) : null) : (cur?.current_observation?.temp_f ? Math.round(cur?.current_observation?.temp_f).toInteger() : null)
			}
		}
		def showGraphs = settings?.tstatShowHistoryGraph == false ? false : true
		showGraphs = showGraphs && !inReview() ? true : false

		if(settings?.devNameOverride == null || atomicState?.devNameOverride == null) { // Upgrade force to on
			atomicState?.devNameOverride = true;
			settingUpdate("devNameOverride", "true", "bool")
		}
		def overRideNames = (atomicState?.devNameOverride) ? true : false

		def devices = app.getChildDevices(true)
		devices?.each {
			def devId = it?.deviceNetworkId
			if(atomicState?.thermostats && atomicState?.deviceData?.thermostats[devId]) {
				def defmin = fixTempSetting(atomicState?."${devId}_safety_temp_min" ?: null)
				def defmax = fixTempSetting(atomicState?."${devId}_safety_temp_max" ?: null)
				def safetyTemps = [ "min":defmin, "max":defmax ]

				def comfortDewpoint = fixTempSetting(settings?."${devId}_comfort_dewpoint_max" ?: null)
				if(comfortDewpoint == 0) {
					comfortDewpoint = fixTempSetting(settings?.locDesiredComfortDewpointMax ?: null)
				}
				def comfortHumidity = settings?."${devId}_comfort_humidity_max" ?: 80
				def autoSchedData = reqSchedInfoRprt(it, false) as Map
				def tData = ["data":atomicState?.deviceData?.thermostats[devId], "mt":useMt, "debug":dbg, "tz":nestTz, "apiIssues":api, "safetyTemps":safetyTemps, "comfortHumidity":comfortHumidity,
						"comfortDewpoint":comfortDewpoint, "pres":locPresence, "childWaitVal":getChildWaitVal().toInteger(), "htmlInfo":htmlInfo, "allowDbException":allowDbException,
						"latestVer":latestTstatVer()?.ver?.toString(), "vReportPrefs":vRprtPrefs, "clientBl":clientBl, "curExtTemp":curWeatherTemp, "logPrefix":logNamePrefix, "hcTimeout":hcTstatTimeout,
						"mobileClientType":mobClientType, "enRemDiagLogging":remDiag, "autoSchedData":autoSchedData, "healthNotify":nPrefs?.dev?.devHealth?.healthMsg, "showGraphs":showGraphs,
						"devBannerData":devBannerData, "restStreaming":streamingActive]
				def oldTstatData = atomicState?."oldTstatData${devId}"
				def tDataChecksum = generateMD5_A(tData.toString())
				atomicState."oldTstatData${devId}" = tDataChecksum
				tDataChecksum = atomicState."oldTstatData${devId}"
				if(force || nforce || (oldTstatData != tDataChecksum)) {
					physDevLblHandler("thermostat", devId, it?.label, "thermostats", tData?.data?.name.toString(), "tstat", overRideNames)
					def t1 = it?.currentState("devVer")?.value?.toString()
					atomicState?.tDevVer = t1 ?: ""
					if(atomicState?.tDevVer != "" && (versionStr2Int(atomicState?.tDevVer) >= minVersions()?.thermostat?.val)) {
						//LogTrace("UpdateChildData >> Thermostat id: ${devId} | data: ${tData}")
						LogTrace("updateChildData >> Thermostat id: ${devId} | oldTstatData: ${oldTstatData} tDataChecksum: ${tDataChecksum} force: $force  nforce: $nforce")
						it.generateEvent(tData)
						if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
					} else {
						if(atomicState?."lastUpdated${devId}Dt" == null) {
							atomicState."lastUpdated${devId}Dt" = getDtNow()
						} else {
							LogAction("NEED SOFTWARE UPDATE: Thermostat ${devId} (v${atomicState?.tDevVer}) REQUIRED: (v${minVersions()?.thermostat?.desc}) Update the Device to latest", "error", true)
							appUpdateNotify()
						}
						it.generateEvent(tData)
					}
				}
				return true
			}
			else if(atomicState?.protects && atomicState?.deviceData?.smoke_co_alarms[devId]) {
				def pData = ["data":atomicState?.deviceData?.smoke_co_alarms[devId], "mt":useMt, "debug":dbg, "showProtActEvts":(!showProtActEvts ? false : true), "logPrefix":logNamePrefix,
						"tz":nestTz, "htmlInfo":htmlInfo, "apiIssues":api, "allowDbException":allowDbException, "latestVer":latestProtVer()?.ver?.toString(), "clientBl":clientBl,
						"hcWireTimeout":hcProtWireTimeout, "hcBattTimeout":hcProtBattTimeout, "mobileClientType":mobClientType, "enRemDiagLogging":remDiag, "healthNotify":nPrefs?.dev?.devHealth?.healthMsg,
						"devBannerData":devBannerData, "restStreaming":streamingActive ]
				def oldProtData = atomicState?."oldProtData${devId}"
				def pDataChecksum = generateMD5_A(pData.toString())
				atomicState."oldProtData${devId}" = pDataChecksum
				pDataChecksum = atomicState."oldProtData${devId}"
				if(force || nforce || (oldProtData != pDataChecksum)) {
					physDevLblHandler("protect", devId, it?.label, "protects", pData?.data?.name.toString(), "prot", overRideNames)
					def t1 = it?.currentState("devVer")?.value?.toString()
					atomicState?.pDevVer = t1 ?: ""
					if(atomicState?.pDevVer != "" && (versionStr2Int(atomicState?.pDevVer) >= minVersions()?.protect?.val)) {
						//LogTrace("UpdateChildData >> Protect id: ${devId} | data: ${pData}")
						LogTrace("UpdateChildData >> Protect id: ${devId} | oldProtData: ${oldProtData} pDataChecksum: ${pDataChecksum} force: $force  nforce: $nforce")
						it.generateEvent(pData)
						if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
					} else {
						if(atomicState?."lastUpdated${devId}Dt" == null) {
							atomicState."lastUpdated${devId}Dt" = getDtNow()
						} else {
							LogAction("NEED SOFTWARE UPDATE: Protect ${devId} (v${atomicState?.pDevVer}) REQUIRED: (v${minVersions()?.protect?.desc}) Update the Device to latest", "error", true)
							appUpdateNotify()
						}
						it.generateEvent(pData)
					}
				}
				return true
			}
			else if(atomicState?.cameras && atomicState?.deviceData?.cameras[devId]) {
				def camData = ["data":atomicState?.deviceData?.cameras[devId], "mt":useMt, "debug":dbg, "logPrefix":logNamePrefix,
						"tz":nestTz, "htmlInfo":htmlInfo, "apiIssues":api, "allowDbException":allowDbException, "latestVer":latestCamVer()?.ver?.toString(), "clientBl":clientBl,
						"hcTimeout":hcCamTimeout, "mobileClientType":mobClientType, "enRemDiagLogging":remDiag, "healthNotify":nPrefs?.dev?.devHealth?.healthMsg,
						"streamNotify":nPrefs?.dev?.camera?.streamMsg, "devBannerData":devBannerData, "restStreaming":streamingActive, "motionSndChgWaitVal":motionSndChgWaitVal ]
				def oldCamData = atomicState?."oldCamData${devId}"
				def cDataChecksum = generateMD5_A(camData.toString())
				atomicState."oldCamData${devId}" = cDataChecksum
				cDataChecksum = atomicState."oldCamData${devId}"
				if(force || nforce || (oldCamData != cDataChecksum)) {
					physDevLblHandler("camera", devId, it?.label, "cameras", camData?.data?.name.toString(), "cam", overRideNames)
					def t1 = it?.currentState("devVer")?.value?.toString()
					atomicState?.camDevVer = t1 ?: ""
					if(atomicState?.camDevVer != "" && (versionStr2Int(atomicState?.camDevVer) >= minVersions()?.camera?.val)) {
						//LogTrace("UpdateChildData >> Camera id: ${devId} | data: ${camData}")
						LogTrace("UpdateChildData >> Camera id: ${devId} | oldCamData: ${oldCamData} cDataChecksum: ${cDataChecksum} force: $force  nforce: $nforce")
						it.generateEvent(camData)
						if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
					} else {
						if(atomicState?."lastUpdated${devId}Dt" == null) {
							atomicState."lastUpdated${devId}Dt" = getDtNow()
						} else {
							LogAction("NEED SOFTWARE UPDATE: Camera ${devId} (v${atomicState?.camDevVer}) REQUIRED: (v${minVersions()?.camera?.desc}) Update the Device to latest", "error", true)
							appUpdateNotify()
						}
						it.generateEvent(camData)
					}
				}
				return true
			}
			else if(atomicState?.presDevice && devId == getNestPresId()) {
				def pData = ["debug":dbg, "logPrefix":logNamePrefix, "tz":nestTz, "mt":useMt, "pres":locPresence, "apiIssues":api, "allowDbException":allowDbException,
							"latestVer":latestPresVer()?.ver?.toString(), "clientBl":clientBl, "hcTimeout":hcLongTimeout, "mobileClientType":mobClientType,
							"enRemDiagLogging":remDiag, "healthNotify":nPrefs?.dev?.devHealth?.healthMsg, "lastStrucDataUpd": atomicState?.lastStrucDataUpd ]
				def oldPresData = atomicState?."oldPresData${devId}"
				def pDataChecksum = generateMD5_A(pData.toString())
				atomicState."oldPresData${devId}" = pDataChecksum
				pDataChecksum = atomicState."oldPresData${devId}"
				if(force || nforce || (oldPresData != pDataChecksum)) {
					virtDevLblHandler(devId, it?.label, "pres", "pres", overRideNames)
					def t1 = it?.currentState("devVer")?.value?.toString()
					atomicState?.presDevVer = t1 ?: ""
					if(atomicState?.presDevVer != "" && (versionStr2Int(atomicState?.presDevVer) >= minVersions()?.presence?.val)) {
						LogTrace("UpdateChildData >> Presence id: ${devId} | oldPresData: ${oldPresData} pDataChecksum: ${pDataChecksum} force: $force  nforce: $nforce")
						it.generateEvent(pData)
						if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
					} else {
						if(atomicState?."lastUpdated${devId}Dt" == null) {
							atomicState."lastUpdated${devId}Dt" = getDtNow()
						} else {
							LogAction("NEED SOFTWARE UPDATE: Presence ${devId} (v${atomicState?.presDevVer}) REQUIRED: (v${minVersions()?.presence?.desc}) Update the Device to latest", "error", true)
							appUpdateNotify()
						}
						it.generateEvent(pData)
					}
				}
				return true
			}
			else if(atomicState?.weatherDevice && devId == getNestWeatherId()) {
				def wData1 = ["weatCond":getWData(), "weatForecast":getWForecastData(), "weatAstronomy":getWAstronomyData(), "weatAlerts":getWAlertsData()]
				def wData = ["data":wData1, "tz":nestTz, "mt":useMt, "debug":dbg, "logPrefix":logNamePrefix, "apiIssues":api, "htmlInfo":htmlInfo,
							"allowDbException":allowDbException, "weathAlertNotif":settings?.weathAlertNotif, "latestVer":latestWeathVer()?.ver?.toString(),
							"clientBl":clientBl, "hcTimeout":hcLongTimeout, "mobileClientType":mobClientType, "enRemDiagLogging":remDiag,
							"healthNotify":nPrefs?.dev?.devHealth?.healthMsg, "showGraphs":showGraphs, "devBannerData":devBannerData ]
				def oldWeatherData = atomicState?."oldWeatherData${devId}"
				def wDataChecksum = generateMD5_A(wData.toString())
				def showWGraphs = settings?.weatherShowGraph == false ? false : true
				atomicState."oldWeatherData${devId}" = wDataChecksum
				wDataChecksum = atomicState."oldWeatherData${devId}"
				if(force || nforce || (oldWeatherData != wDataChecksum)) {
					virtDevLblHandler(devId, it?.label, "weather", "weath", overRideNames)
					def t1 = it?.currentState("devVer")?.value?.toString()
					atomicState?.weatDevVer = t1 ?: ""
					if(atomicState?.weatDevVer != "" && (versionStr2Int(atomicState?.weatDevVer) >= minVersions()?.weather?.val)) {
						LogTrace("UpdateChildData >> Weather id: ${devId} oldWeatherData: ${oldWeatherData} wDataChecksum: ${wDataChecksum} force: $force  nforce: $nforce")
						it.generateEvent(wData)
						if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
					} else {
						if(atomicState?."lastUpdated${devId}Dt" == null) {
							atomicState."lastUpdated${devId}Dt" = getDtNow()
						} else {
							LogAction("NEED SOFTWARE UPDATE: Weather ${devId} (v${atomicState?.weatDevVer}) REQUIRED: (v${minVersions()?.weather?.desc}) Update the Device to latest", "error", true)
							appUpdateNotify()
						}
						it.generateEvent(wData)
					}
				}
				return true
			}

			else if(atomicState?.vThermostats && atomicState?."vThermostat${devId}") {
				def physdevId = atomicState?."vThermostatMirrorId${devId}"

				if(atomicState?.thermostats && atomicState?.deviceData?.thermostats[physdevId]) {
					def data = atomicState?.deviceData?.thermostats[physdevId]
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
						def tempC = 0.0
						def tempF = 0
						if(getTemperatureScale() == "C") {
							tempC = automationChildApp.getRemoteSenTemp()
							tempF = (tempC * 9/5 + 32) as Integer
						} else {
							tempF = automationChildApp.getRemoteSenTemp()
							tempC = (tempF - 32) * 5/9 as Double
						}
						data?.ambient_temperature_c = tempC
						data?.ambient_temperature_f = tempF

						def ctempC = 0.0
						def ctempF = 0
						if(getTemperatureScale() == "C") {
							ctempC = automationChildApp.getRemSenCoolSetTemp()
							ctempF = (ctempC * 9/5 + 32.0) as Integer
						} else {
							ctempF = automationChildApp.getRemSenCoolSetTemp()
							ctempC = (ctempF - 32.0) * 5/9 as Double
						}

						def htempC = 0.0
						def htempF = 0
						if(getTemperatureScale() == "C") {
							htempC = automationChildApp.getRemSenHeatSetTemp()
							htempF = (htempC * 9/5 + 32.0) as Integer
						} else {
							htempF = automationChildApp.getRemSenHeatSetTemp()
							htempC = (htempF - 32.0) * 5/9 as Double
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

					def autoSchedData = reqSchedInfoRprt(it, false) as Map
					def tData = ["data":data, "mt":useMt, "debug":dbg, "tz":nestTz, "apiIssues":api, "safetyTemps":safetyTemps, "comfortHumidity":comfortHumidity,
						"comfortDewpoint":comfortDewpoint, "pres":locPresence, "childWaitVal":getChildWaitVal().toInteger(), "htmlInfo":htmlInfo, "allowDbException":allowDbException,
						"latestVer":latestvStatVer()?.ver?.toString(), "vReportPrefs":vRprtPrefs, "clientBl":clientBl, "curExtTemp":curWeatherTemp, "logPrefix":logNamePrefix, "hcTimeout":hcTstatTimeout,
						"mobileClientType":mobClientType, "enRemDiagLogging":remDiag, "autoSchedData":autoSchedData, "healthNotify":nPrefs?.dev?.devHealth?.healthMsg, "showGraphs":showGraphs, "devBannerData":devBannerData]

					def oldTstatData = atomicState?."oldvStatData${devId}"
					def tDataChecksum = generateMD5_A(tData.toString())
					atomicState."oldvStatData${devId}" = tDataChecksum
					tDataChecksum = atomicState."oldvStatData${devId}"
					if(force || nforce || (oldTstatData != tDataChecksum)) {
						physDevLblHandler("vthermostat", devId, it?.label, "vThermostats", tData?.data?.name.toString(), "vtstat", overRideNames)
						def t1 = it?.currentState("devVer")?.value?.toString()
						atomicState?.vtDevVer = t1 ?: ""
						if(atomicState?.vtDevVer != "" && (versionStr2Int(atomicState?.vtDevVer) >= minVersions()?.thermostat?.val)) {
							LogTrace("UpdateChildData >> vThermostat id: ${devId} | oldvStatData: ${oldvStatData} tDataChecksum: ${tDataChecksum} force: $force  nforce: $nforce")
							it.generateEvent(tData)
							if(atomicState?."lastUpdated${devId}Dt" != null) { state.remove("lastUpdated${devId}Dt" as String) }
						} else {
							if(atomicState?."lastUpdated${devId}Dt" == null) {
								atomicState."lastUpdated${devId}Dt" = getDtNow()
							} else {
								LogAction("NEED SOFTWARE UPDATE: Thermostat ${devId} (v${atomicState?.tDevVer}) REQUIRED: (v${minVersions()?.thermostat?.desc}) Update the Device to latest", "error", true)
							}
							it.generateEvent(tData)
						}
					}
					return true
				}
			}

			else if(devId == getNestPresId()) {
				return true
			}
			else if(devId == getNestWeatherId()) {
				return true
			}
/* This causes NP exceptions depending if child has not finished being deleted or if items are removed from Nest
			else if(!atomicState?.deviceData?.thermostats[devId] && !atomicState?.deviceData?.smoke_co_alarms[devId] && !atomicState?.deviceData?.cameras[devId]) {
				LogAction("Device found ${devId} and connection removed", "warn", true)
				return null
			}
*/
			else {
				LogAction("updateChildData() Device ${devId} unclaimed", "warn", true)
				return true
			}
		}
	}
	catch (ex) {
		log.error "updateChildData Exception:", ex
		sendExceptionData(ex, "updateChildData")
		atomicState?.lastChildUpdDt = null
		return
	}
	atomicState.forceChildUpd = false
	atomicState.needChildUpd = false
}

def setNeedChildUpdate() {
	atomicState.needChildUpd = true
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
	LogTrace("physDevLblHandler | deflbl: ${deflbl} | curlbl: ${curlbl} | newlbl: ${newlbl} | deflblval: ${deflblval} || devId: ${devId}")
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
	if(!atomicState?.custLabelUsed && settings?."${abrevStr}_${devId}_lbl") { settingUpdate("${abrevStr}_${devId}_lbl", "") }
	if(settings?."${abrevStr}_${deflblval}_lbl") { settingUpdate("${abrevStr}_${deflblval}_lbl", "") } // clean up old stuff
}

void virtDevLblHandler(devId, devLbl, devMethAbrev, abrevStr, ovrRideNames) {
	def curlbl = devLbl?.toString()
	def newlbl = "getNest${devMethAbrev.capitalize()}Label"()
	LogTrace("virtDevLblHandler | curlbl: ${curlbl} | newlbl: ${newlbl} || devId: ${devId}")
	if(ovrRideNames && curlbl != newlbl) {
		LogAction("Changing name from ${curlbl} to ${newlbl}", "info", true)
		setDeviceLabel(devId, newlbl?.toString())
		curlbl = newlbl?.toString()
	}
	if(atomicState?.custLabelUsed && settings?."${abrevStr}Dev_lbl" != curlbl) {
		settingUpdate("${abrevStr}Dev_lbl", curlbl?.toString())
	}
	if(!atomicState?.custLabelUsed && settings?."${abrevStr}Dev_lbl") { settingUpdate("${abrevStr}Dev_lbl", "") }
}

def getLocationPresence() {
	def away = atomicState?.structData && atomicState?.structures ? atomicState?.structData[atomicState?.structures]?.away : null
	return (away != null) ? away.toString() : null
}

def apiIssues() {
	def t0 = atomicState?.apiIssuesList ?: [false, false, false, false, false, false, false]
	def result = t0[3..-1].every { it == true } ? true : false
	def dt = atomicState?.apiIssueDt
	if(result) {
		LogAction("Nest API Issues ${dt ? "may still be occurring. Status will clear when last updates are good (Last Updates: ${t0}) | Issues began at ($dt) " : "Detected (${getDtNow()})"}", "warn", true)
	}
	apiIssueType()
	atomicState?.apiIssueDt = (result ? (dt ?: getDtNow()) : null)
	return result
}

def apiIssueType() {
	def res = "none"
	//this looks at the last 3 items added and determines whether issue is sporadic or outage
	def t0 = atomicState?.apiIssuesList ?: [false, false, false, false, false, false, false]
	def items = t0[3..-1].findAll { it == true }
	if(items?.size() >= 1 && items?.size() <= 2) { res = "sporadic" }
	else if(items?.size() >= 3) { res = "outage" }
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
	if(list) { atomicState?.apiIssuesList = list }
}

def ok2PollMetaData() {
	if(atomicState?.pollBlocked) { return false }
	if(atomicState?.needMetaPoll) { return true }
	def pollTime = !settings?.pollMetaValue ? (3600 * 4) : settings?.pollMetaValue.toInteger()
	def val = pollTime/3
	if(val > 60) { val = 50 }
	return ( ((getLastMetaPollSec() + val) > pollTime) ? true : false )
}

def ok2PollDevice() {
	if(atomicState?.pollBlocked) { return false }
	if(atomicState?.needDevPoll) { return true }
	def pollTime = !settings?.pollValue ? 180 : settings?.pollValue.toInteger()
	def val = pollTime/3
	val = Math.max(Math.min(val.toInteger(), 50),25)
	//if(val > 60) { val = 50 }
	return ( ((getLastDevicePollSec() + val) > pollTime) ? true : false )
}

def ok2PollStruct() {
	if(atomicState?.pollBlocked) { return false }
	if(atomicState?.needStrPoll) { return true }
	def pollStrTime = !settings?.pollStrValue ? 180 : settings?.pollStrValue.toInteger()
	def val = pollStrTime/3
	val = Math.max(Math.min(val.toInteger(), 50),25)
	//if(val > 60) { val = 50 }
	return ( ((getLastStructPollSec() + val) > pollStrTime || !atomicState?.structData) ? true : false )
}


def isPollAllowed() {
	return (atomicState?.pollingOn &&
		!atomicState?.clientBlacklisted &&
		(atomicState?.thermostats || atomicState?.protects || atomicState?.weatherDevice || atomicState?.cameras)) ? true : false
}

def getLastMetaPollSec() { return !atomicState?.lastMetaDataUpd ? 100000 : GetTimeDiffSeconds(atomicState?.lastMetaDataUpd, null, "getLastMetaPollSec").toInteger() }
def getLastDevicePollSec() { return !atomicState?.lastDevDataUpd ? 840 : GetTimeDiffSeconds(atomicState?.lastDevDataUpd, null, "getLastDevicePollSec").toInteger() }
def getLastStructPollSec() { return !atomicState?.lastStrucDataUpd ? 1000 : GetTimeDiffSeconds(atomicState?.lastStrucDataUpd, null, "getLastStructPollSec").toInteger() }
def getLastForcedPollSec() { return !atomicState?.lastForcePoll ? 1000 : GetTimeDiffSeconds(atomicState?.lastForcePoll, null, "getLastForcedPollSec").toInteger() }
def getLastChildUpdSec() { return !atomicState?.lastChildUpdDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastChildUpdDt, null, "getLastChildUpdSec").toInteger() }
def getLastHeardFromNestSec() { return !atomicState?.lastHeardFromNestDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastHeardFromNestDt, null, "getLastHeardFromNestSec").toInteger() }

/************************************************************************************************
|										Nest API Commands										|
*************************************************************************************************/

private cmdProcState(Boolean value) { atomicState?.cmdIsProc = value }
private cmdIsProc() { return (!atomicState?.cmdIsProc) ? false : true }
private getLastProcSeconds() { return atomicState?.cmdLastProcDt ? GetTimeDiffSeconds(atomicState?.cmdLastProcDt, null, "getLastProcSeconds") : 0 }

def apiVar() {
	def api = [
		rootTypes: [
			struct:"structures", cos:"devices/smoke_co_alarms", tstat:"devices/thermostats", cam:"devices/cameras", meta:"metadata"
		],
		cmdObjs: [
			targetF:"target_temperature_f", targetC:"target_temperature_c", targetLowF:"target_temperature_low_f", setLabel:"label",
			targetLowC:"target_temperature_low_c", targetHighF:"target_temperature_high_f", targetHighC:"target_temperature_high_c",
			fanActive:"fan_timer_active", fanTimer:"fan_timer_timeout", fanDuration:"fan_timer_duration", hvacMode:"hvac_mode",
			away:"away", streaming:"is_streaming", setTscale:"temperature_scale"
		]
	]
	return api
}

def setCamStreaming(child, streamOn) {
	def devId = !child?.device?.deviceNetworkId ? child?.toString() : child?.device?.deviceNetworkId.toString()
	def val = streamOn.toBoolean() ? true : false
	LogAction("setCamStreaming: Camera${!devId ? "" : " ${devId}"} (${val ? "On" : "Off"})", "debug", true)
	return sendNestApiCmd(devId, apiVar().rootTypes.cam, apiVar().cmdObjs.streaming, val, devId)
}

def setCamLabel(child, label, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def val = label
	def virt = virtual.toBoolean()
//  This is not used anywhere.  A command to set label is not available in the dth for a callback
	LogAction("setCamLabel: Camera${!devId ? "" : " ${devId}"} Label: (${val})", "debug", true)
	return sendNestApiCmd(devId, apiVar().rootTypes.cam, apiVar().cmdObjs.setLabel, val, devId)
}

def setProtLabel(child, label, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def val = label
	def virt = virtual.toBoolean()
//  This is not used anywhere.  A command to set label is not available in the dth for a callback
	LogAction("setProtLabel: Protect${!devId ? "" : " ${devId}"} Label: (${val})", "debug", true)
	return sendNestApiCmd(devId, apiVar().rootTypes.cos, apiVar().cmdObjs.setLabel, val, devId)
}

def setStructureAway(child, value, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def val = value?.toBoolean()
	def virt = virtual.toBoolean()

	if(virt && atomicState?.vThermostats && devId) {
		if(atomicState?."vThermostat${devId}") {
			def pdevId = atomicState?."vThermostatMirrorId${devId}"
			def pChild
			if(pdevId) { pChild = getChildDevice(pdevId) }

			if(pChild) {
				if(val) {
					pChild.away()
				} else {
					pChild.present()
				}
			} else { LogAction("setStructureAway - CANNOT Set Thermostat${pdevId} Presence: (${val}) child ${pChild}", "warn", true) }
		}
	} else {
		LogAction("setStructureAway - Setting Nest Location: (${child?.device?.displayName})${!devId ? "" : " ${devId}"} (${val ? "Away" : "Home"})", "debug", true)
		if(val) {
			def ret = sendNestApiCmd(atomicState?.structures, apiVar().rootTypes.struct, apiVar().cmdObjs.away, "away", devId)
			// Below is to ensure automations read updated value even if queued
			if(ret && atomicState?.structData && atomicState?.structures && atomicState?.structData[atomicState?.structures]?.away) {
				def t0 = atomicState?.structData
				t0[atomicState?.structures].away = "away"
				atomicState?.structData = t0
			}
			return ret
		}
		else {
			def ret = sendNestApiCmd(atomicState?.structures, apiVar().rootTypes.struct, apiVar().cmdObjs.away, "home", devId)
			if(ret && atomicState?.structData && atomicState?.structures && atomicState?.structData[atomicState?.structures]?.away) {
				def t0 = atomicState?.structData
				t0[atomicState?.structures].away = "home"
				atomicState?.structData = t0
			}
			return ret
		}
	}
}

def setTstatTempScale(child, tScale, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def tempScale = tScale.toString()
	def virt = virtual.toBoolean()

//  INCOMPLETE: This is not used anywhere.  A command to set Temp Scale is not available in the dth for a callback

	LogAction("setTstatTempScale: INCOMPLETE Thermostat${!devId ? "" : " ${devId}"} tempScale: (${tempScale})", "debug", true)
	return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.setTscale, tempScale, devId)
}

def setTstatLabel(child, label, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def val = label
	def virt = virtual.toBoolean()

//  INCOMPLETE: This is not used anywhere.  A command to set label is not available in the dth for a callback

	LogAction("setTstatLabel: INCOMPLETE Thermostat${!devId ? "" : " ${devId}"} Label: (${val})", "debug", true)
	return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.setLabel, val, devId)
}

def setFanMode(child, fanOn, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def val = fanOn.toBoolean()
	def virt = virtual.toBoolean()

	if(virt && atomicState?.vThermostats && devId) {
		if(atomicState?."vThermostat${devId}") {
			def pdevId = atomicState?."vThermostatMirrorId${devId}"
			def pChild
			if(pdevId) { pChild = getChildDevice(pdevId) }

			if(pChild) {
				if(val) {
					pChild.fanOn()
				} else {
					pChild.fanAuto()
				}
			} else { LogAction("setFanMode - CANNOT Set Thermostat${pdevId} FanMode: (${fanOn}) child ${pChild}", "warn", true) }
		}
	} else {
		LogAction("setFanMode - Setting Thermostat${!devId ? "" : " ${devId}"} Fan Mode: (${val ? "On" : "Auto"})", "debug", true)
		return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.fanActive, val, devId)
	}
}

def setHvacMode(child, mode, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def virt = virtual.toBoolean()

	if(virt && atomicState?.vThermostats && devId) {
		if(atomicState?."vThermostat${devId}") {
			def pdevId = atomicState?."vThermostatMirrorId${devId}"
			def pChild
			if(pdevId) { pChild = getChildDevice(pdevId) }

			if(pChild) {
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
			} else { LogAction("setHvacMode - CANNOT Set Thermostat${pdevId} Mode: (${mode}) child ${pChild}", "warn", true) }
		}
	} else {
		LogAction("setHvacMode - Setting Thermostat (${child?.device?.displayName})${!devId ? "" : " ${devId}"} Mode: (${mode})", "debug", true)
		return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.hvacMode, mode.toString(), devId)
	}
}

def setTargetTemp(child, unit, temp, mode, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def virt = virtual.toBoolean()

	if(virt && atomicState?.vThermostats && devId) {
		if(atomicState?."vThermostat${devId}") {
			def pdevId = atomicState?."vThermostatMirrorId${devId}"
			def pChild
			if(pdevId) { pChild = getChildDevice(pdevId) }
			def appId = atomicState?."vThermostatChildAppId${devId}"
			def automationChildApp
			if(appId) { automationChildApp = getChildApps().find{ it.id == appId } }
			if(automationChildApp) {
				def res = automationChildApp.remSenTempUpdate(temp,mode)
				if(res) { return }
			}
			if(pChild) {
				if(mode == 'cool') {
					pChild.setCoolingSetpoint(temp)
				} else if(mode == 'heat') {
					pChild.setHeatingSetpoint(temp)
				} else { LogAction("setTargetTemp - UNKNOWN MODE (${mode}) child ${pChild}", "warn", true) }
			} else { LogAction("setTargetTemp - CANNOT Set Thermostat${pdevId} Temp: (${temp})${unit} Mode: (${mode}) child ${pChild}", "warn", true) }
		}
	} else {
		LogAction("setTargetTemp: (${child?.device?.displayName}) ${devId} | (${temp})${unit} | virtual ${virtual}", "debug", true)
		if(unit == "C") {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetC, temp, devId)
		}
		else {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetF, temp, devId)
		}
	}
}

def setTargetTempLow(child, unit, temp, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def virt = virtual.toBoolean()

	if(virt && atomicState?.vThermostats && devId) {
		if(atomicState?."vThermostat${devId}") {
			def pdevId = atomicState?."vThermostatMirrorId${devId}"
			def pChild
			if(pdevId) { pChild = getChildDevice(pdevId) }

			def appId = atomicState?."vThermostatChildAppId${devId}"
			def automationChildApp
			if(appId) { automationChildApp = getChildApps().find{ it.id == appId } }

			if(automationChildApp) {
				def res = automationChildApp.remSenTempUpdate(temp,"heat")
				if(res) { return }
			}

			if(pChild) {
					pChild.setHeatingSetpoint(temp)
			} else { LogAction("setTargetTemp - CANNOT Set Thermostat${pdevId} HEAT: (${temp})${unit} child ${pChild}", "warn", true) }
		}
	} else {
		LogAction("setTargetTempLow: (${child?.device?.displayName}) ${devId} | (${temp})${unit} | virtual ${virtual}", "debug", true)
		if(unit == "C") {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetLowC, temp, devId)
		}
		else {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetLowF, temp, devId)
		}
	}
}

def setTargetTempHigh(child, unit, temp, virtual=false) {
	def devId = !child?.device?.deviceNetworkId ? null : child?.device?.deviceNetworkId.toString()
	def virt = virtual.toBoolean()

	if(virt && atomicState?.vThermostats && devId) {
		if(atomicState?."vThermostat${devId}") {
			def pdevId = atomicState?."vThermostatMirrorId${devId}"
			def pChild
			if(pdevId) { pChild = getChildDevice(pdevId) }

			def appId = atomicState?."vThermostatChildAppId${devId}"
			def automationChildApp
			if(appId) { automationChildApp = getChildApps().find{ it.id == appId } }

			if(automationChildApp) {
				def res = automationChildApp.remSenTempUpdate(temp,"cool")
				if(res) { return }
			}

			if(pChild) {
				pChild.setCoolingSetpoint(temp)
			} else { LogAction("setTargetTemp - CANNOT Set Thermostat${pdevId} COOL: (${temp})${unit} child ${pChild}", "warn", true) }
		}
	} else {
		LogAction("setTargetTempHigh: (${child?.device?.displayName}) ${devId} | (${temp})${unit} | virtual ${virtual}", "debug", true)
		if(unit == "C") {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetHighC, temp, devId)
		}
		else {
			return sendNestApiCmd(devId, apiVar().rootTypes.tstat, apiVar().cmdObjs.targetHighF, temp, devId)
		}
	}
}

def sendNestApiCmd(cmdTypeId, cmdType, cmdObj, cmdObjVal, childId) {
	LogAction("sendNestApiCmd $cmdTypeId, $cmdType, $cmdObj, $cmdObjVal, $childId", "info", false)
	try {
		if(cmdTypeId) {
			def qnum = getQueueNumber(cmdTypeId, childId)
			if(qnum == -1 ) { return false }

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
						newCmd[2] != apiVar().cmdObjs.away && newCmd[2] != apiVar().cmdObjs.fanActive && newCmd[2] != apiVar().cmdObjs.fanTimer) {
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

			LogAction("${str} in Queue ${qnum} (qsize: ${tempQueue?.size()}): $cmdTypeId, $cmdType, $cmdObj, $cmdObjVal, $childId", "info", true)
			atomicState?.pollBlocked = true
			atomicState?.lastQcmd = cmdData
			schedNextWorkQ(childId)
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

private getQueueNumber(cmdTypeId, childId) {
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
		setRecentSendCmd(qnum, null)
	}
	qnum = cmdQueueList.indexOf(cmdTypeId)
	if(qnum == -1 ) { LogAction("getQueueNumber: NOT FOUND", "warn", true ) }
	return qnum
}

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
	return qnum
}


void schedNextWorkQ(childId) {
	def cmdDelay = getChildWaitVal()
	//
	// This is throttling the rate of commands to the Nest service for this access token.
	// If too many commands are sent Nest throttling could shut all write commands down for 1 hour to the device or structure
	// This allows up to 3 commands if none sent in the last hour, then only 1 per 60 seconds.  Nest could still
	// throttle this if the battery state on device is low.
	// https://nestdevelopers.io/t/user-receiving-exceeded-rate-limit-on-requests-please-try-again-later/354
	//

	def qnum = getQueueToWork()
	def timeVal = cmdDelay
	if(qnum != null) {
		if( !(getRecentSendCmd(qnum) > 0 || getLastCmdSentSeconds(qnum) > 60) ) {
			timeVal = (60 - getLastCmdSentSeconds(qnum) + cmdDelay)
		}
		def str = timeVal > cmdDelay ? "RATE LIMITING ON " : ""
		LogAction("schedNextWorkQ ${str}queue: ${qnum} | schedTime: ${timeVal} | recentSendCmd: ${getRecentSendCmd(qnum)} | last seconds: ${getLastCmdSentSeconds(qnum)} | cmdDelay: ${cmdDelay}", "info", true)
	}
	runIn(timeVal, "workQueue", [overwrite: true])
}

private getRecentSendCmd(qnum) {
	return atomicState?."recentSendCmd${qnum}"
}

private setRecentSendCmd(qnum, val) {
	atomicState."recentSendCmd${qnum}" = val
	return
}

def sendEcoActionDescToDevice(dev, desc) {
	if(dev && desc) {
		dev?.ecoDesc(desc)
	}
}

private getLastCmdSentSeconds(qnum) { return atomicState?."lastCmdSentDt${qnum}" ? GetTimeDiffSeconds(atomicState?."lastCmdSentDt${qnum}", null, "getLastCmdSentSeconds") : 3601 }

private setLastCmdSentSeconds(qnum, val) {
	atomicState."lastCmdSentDt${qnum}" = val
	atomicState.lastCmdSentDt = val
}

def storeLastCmdData(cmd, qnum) {
	if(cmd) {
		def newVal = ["qnum":qnum, "obj":cmd[2], "value":cmd[3], "date":getDtNow()]

		def list = atomicState?.cmdDetailHistory ?: []
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
		if(list) { atomicState?.cmdDetailHistory = list }
	}
}

void workQueue() {
	LogTrace("workQueue")
	def cmdDelay = getChildWaitVal()

	if(!atomicState?.cmdQlist) { atomicState?.cmdQlist = [] }
	def cmdQueueList = atomicState?.cmdQlist

	def qnum = getQueueToWork()
	if(qnum == null) { qnum = 0 }

	def allowAsync = false
	def metstr = "sync"
	if(atomicState?.appData && atomicState?.appData?.pollMethod?.allowAsync) {
		allowAsync = true
		metstr = "async"
	}

	if(!atomicState?."cmdQ${qnum}") { atomicState."cmdQ${qnum}" = [] }
	def cmdQueue = atomicState?."cmdQ${qnum}"

	try {
		if(cmdQueue?.size() > 0) {
			LogAction("workQueue Run queue: ${qnum} $metstr", "trace", true)
			runIn(60, "workQueue", [overwrite: true])  // lost schedule catchall

			if(!cmdIsProc()) {
				cmdProcState(true)
				atomicState?.pollBlocked = true
				cmdQueue = atomicState?."cmdQ${qnum}"
				def cmd = cmdQueue?.remove(0)
				atomicState?."cmdQ${qnum}" = cmdQueue
				def cmdres

				if(getLastCmdSentSeconds(qnum) > 3600) { setRecentSendCmd(qnum, 3) } // if nothing sent in last hour, reset 3 command limit

				storeLastCmdData(cmd, qnum)

				if(cmd[1] == "poll") {
					atomicState.needStrPoll = true
					atomicState.needDevPoll = true
					atomicState.forceChildUpd = true
					cmdres = true
				} else {
					if(allowAsync) {
						cmdres = queueProcNestApiCmd(getNestApiUrl(), cmd[0], cmd[1], cmd[2], cmd[3], qnum, cmd)
						return
					} else {
						cmdres = procNestApiCmd(getNestApiUrl(), cmd[0], cmd[1], cmd[2], cmd[3], qnum)
					}
				}
				finishWorkQ(cmd, cmdres)
			} else { LogAction("workQueue: busy processing command", "warn", true) }

		} else { atomicState.pollBlocked = false; cmdProcState(false) }
	}
	catch (ex) {
		log.error "workQueue Exception Error:", ex
		sendExceptionData(ex, "workQueue")
		cmdProcState(false)
		atomicState.needDevPoll = true
		atomicState.needStrPoll = true
		atomicState.forceChildUpd = true
		atomicState?.pollBlocked = false
		runIn(60, "workQueue", [overwrite: true])
		runIn((60 + 4), "postCmd", [overwrite: true])
		return
	}
}

def finishWorkQ(cmd, result) {
	LogTrace("finishWorkQ")
	def cmdDelay = getChildWaitVal()

	cmdProcState(false)
	if( !result ) {
		atomicState.forceChildUpd = true
		atomicState.pollBlocked = false
		runIn((cmdDelay * 3), "postCmd", [overwrite: true])
	}

	atomicState.needDevPoll = true
	if(cmd && cmd[1] == apiVar().rootTypes.struct.toString()) {
		atomicState.needStrPoll = true
		atomicState.forceChildUpd = true
	}

	def qnum = getQueueToWork()
	if(qnum == null) { qnum = 0 }

	if(!atomicState?."cmdQ${qnum}") { atomicState?."cmdQ${qnum}" = [] }
	def cmdQueue = atomicState?."cmdQ${qnum}"
	if(cmdQueue?.size() == 0) {
		atomicState.pollBlocked = false
		atomicState.needChildUpd = true
		cmdProcState(false)
		runIn(cmdDelay * 2, "postCmd", [overwrite: true])
	}
	else { schedNextWorkQ(null) }

	atomicState?.cmdLastProcDt = getDtNow()
	if(cmdQueue?.size() > 10) {
		sendMsg("Warning", "There is now ${cmdQueue?.size()} events in the Command Queue. Something must be wrong", false)
		LogAction("${cmdQueue?.size()} events in the Command Queue", "warn", true)
	}
	return
}

def queueProcNestApiCmd(uri, typeId, type, obj, objVal, qnum, cmd, redir = false) {
	LogTrace("queueProcNestApiCmd: typeId: ${typeId}, type: ${type}, obj: ${obj}, objVal: ${objVal}, qnum: ${qnum},  isRedirUri: ${redir}")
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
		LogAction("queueProcNestApiCmd Url: $uri | params: ${params}", "trace", true)
		atomicState?.lastCmdSent = "$type: (${obj}: ${objVal})"

		if(!redir && (getRecentSendCmd(qnum) > 0) && (getLastCmdSentSeconds(qnum) < 60)) {
			def val = getRecentSendCmd(qnum)
			val -= 1
			setRecentSendCmd(qnum, val)
		}
		setLastCmdSentSeconds(qnum, getDtNow())

		LogTrace("queueProcNestApiCmd time update recentSendCmd:  ${getRecentSendCmd(qnum)}  last seconds:${getLastCmdSentSeconds(qnum)} queue: ${qnum}")

		def asyncargs = [
			typeId: typeId,
			type: type,
			obj: obj,
			objVal: objVal,
			qnum: qnum,
			cmd: cmd ]

		asynchttp_v1.put(nestCmdResponse, params, asyncargs)

	} catch(ex) {
		log.error "queueProcNestApiCmd (command: $cmd) Exception:", ex
		sendExceptionData(ex, "queueProcNestApiCmd")
	}
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
			LogAction("nestCmdResponse Processed queue: ${qnum} ($type | ($obj:$objVal)) SUCCESSFULLY!", "info", true)
			apiIssueEvent(false)
			incCmdCnt()
			atomicState?.lastCmdSentStatus = "ok"
			atomicState?.apiRateLimited = false
			atomicState?.apiCmdFailData = null
			result = true
		} else {
			apiIssueEvent(true)
			atomicState?.lastCmdSentStatus = "failed"
			if(resp?.hasError()) {
				apiRespHandler((resp?.getStatus() ?: null), (resp?.getErrorJson() ?: null), "nestCmdResponse")
			}
		}
		finishWorkQ(command, result)

	} catch (ex) {
		log.error "nestCmdResponse (command: $command) Exception:", ex
		sendExceptionData(ex, "nestCmdResponse")
		apiIssueEvent(true)
		atomicState?.lastCmdSentStatus = "failed"
		cmdProcState(false)
	}
}

def procNestApiCmd(uri, typeId, type, obj, objVal, qnum, redir = false) {
	LogTrace("procNestApiCmd: typeId: ${typeId}, type: ${type}, obj: ${obj}, objVal: ${objVal}, qnum: ${qnum},  isRedirUri: ${redir}")
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
		LogAction("procNestApiCmd Url: $uri | params: ${params}", "trace", true)
		atomicState?.lastCmdSent = "$type: (${obj}: ${objVal})"

		if(!redir && (getRecentSendCmd(qnum) > 0) && (getLastCmdSentSeconds(qnum) < 60)) {
			def val = getRecentSendCmd(qnum)
			val -= 1
			setRecentSendCmd(qnum, val)
		}
		setLastCmdSentSeconds(qnum, getDtNow())

		LogTrace("procNestApiCmd time update recentSendCmd:  ${getRecentSendCmd(qnum)}  last seconds:${getLastCmdSentSeconds(qnum)} queue: ${qnum}")

		httpPutJson(params) { resp ->
			def rCode = resp?.status ?: null
			if(resp?.status == 307) {
				def newUrl = resp?.headers?.location?.split("\\?")
				LogTrace("NewUrl: ${newUrl[0]}")
				if( procNestApiCmd(newUrl[0], typeId, type, obj, objVal, qnum, true) ) {
					result = true
				}
			}
			else if(resp?.status == 200) {
				LogAction("nestCmdResponse Processed queue: ${qnum} ($type | ($obj:$objVal)) SUCCESSFULLY!", "info", true)
				apiIssueEvent(false)
				incCmdCnt()
				atomicState?.lastCmdSentStatus = "ok"
				atomicState?.apiRateLimited = false
				atomicState?.apiCmdFailData = null
				result = true
			}
			else {
				apiIssueEvent(true)
				atomicState?.lastCmdSentStatus = "failed"
				result = false
				apiRespHandler(resp?.status, resp?.data, "procNestApiCmd")
			}
		}
	} catch (ex) {
		apiIssueEvent(true)
		atomicState?.lastCmdSentStatus = "failed"
		cmdProcState(false)
		if (ex instanceof groovyx.net.http.HttpResponseException) {
			apiRespHandler(ex?.response?.status, ex?.response?.data, "procNestApiCmd")
		} else {
			log.error "procNestApiCmd Exception: ($type | $obj:$objVal)", ex
			sendExceptionData(ex, "procNestApiCmd")
		}
	}
	return result
}

def apiRespHandler(code, errJson, methodName) {
	LogAction("[$methodName] | Status: (${code}) | Error Message: ${errJson}", "warn", true)
	if (!(code?.toInteger() in [200, 307])) {
		def result = ""
		def errMsg = errJson?.message != null ? errJson?.message : null
		switch(code) {
			case 400:
				result = !errMsg ? "A Bad Request was made to the API..." : errMsg
				break
			case 401:
				result =  !errMsg ? "Authentication ERROR, Please try refreshing your login under Authentication settings..." : errMsg
				revokeNestToken()
				break
			case 403:
				result =  !errMsg ? "Forbidden: Your Login Credentials are Invalid..." : errMsg
				revokeNestToken()
				break
			case 429:
				result =  !errMsg ? "Requests are currently being blocked because of API Rate Limiting..." : errMsg
				atomicState?.apiRateLimited = true
				break
			case 500:
				result =  !errMsg ? "Internal Nest Error:" : errMsg
				break
			case 503:
				result =  !errMsg ? "There is currently a Nest Service Issue..." : errMsg
				break
			default:
				result =  !errMsg ? "Received Response..." : errMsg
				break
		}
		def failData = ["code":code, "msg":result, "method":methodName, "dt":getDtNow()]
		atomicState?.apiCmdFailData = failData
		failedCmdNotify(failData)
		LogAction("$methodName error - (Status: $code - $result) - [ErrorLink: ${errJson?.type}]", "error", true)
	}
}

def incApiStrReqCnt() {
	long reqCnt = atomicState?.apiStrReqCnt ?: 0
	reqCnt = reqCnt?.toLong()+1
	LogTrace("ApiStrReqCnt: $reqCnt")
	atomicState?.apiStrReqCnt = reqCnt?.toLong()
}

def incApiDevReqCnt() {
	long reqCnt = atomicState?.apiDevReqCnt ?: 0
	reqCnt = reqCnt?.toLong()+1
	LogTrace("ApiDevReqCnt: $reqCnt")
	atomicState?.apiDevReqCnt = reqCnt?.toLong()
}

def incApiMetaReqCnt() {
	long reqCnt = atomicState?.apiMetaReqCnt ?: 0
	reqCnt = reqCnt?.toLong()+1
	LogTrace("ApiMetaReqCnt: $reqCnt")
	atomicState?.apiMetaReqCnt = reqCnt?.toLong()
}

def incCmdCnt() {
	long cmdCnt = atomicState?.apiCommandCnt ?: 0
	cmdCnt = cmdCnt?.toLong()+1
	LogTrace("Api CmdCnt: $cmdCnt")
	atomicState?.apiCommandCnt = cmdCnt?.toLong()
}

def incRestStrEvtCnt() {
	long evtCnt = atomicState?.apiRestStrEvtCnt ?: 0
	evtCnt = evtCnt?.toLong()+1
	LogTrace("ApiRestStrEvtCnt: $evtCnt")
	atomicState?.apiRestStrEvtCnt = evtCnt?.toLong()
}

def incAppNotifSentCnt() {
	long notCnt = atomicState?.appNotifSentCnt ?: 0
	notCnt = notCnt?.toLong()+1
	LogTrace("AppNotifSentCnt: $notCnt")
	atomicState?.appNotifSentCnt = notCnt?.toLong()
}

/************************************************************************************************
|								Push Notification Functions										|
*************************************************************************************************/
def pushStatus() { return (settings?.recipients || settings?.phone || settings?.usePush) ? (settings?.usePush ? "Push Enabled" : "Enabled") : null }
def getLastMsgSec() { return !atomicState?.lastMsgDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastMsgDt, null, "getLastMsgSec").toInteger() }
def getLastUpdMsgSec() { return !atomicState?.lastUpdMsgDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastUpdMsgDt, null, "getLastUpdMsgSec").toInteger() }
def getLastMissPollMsgSec() { return !atomicState?.lastMisPollMsgDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastMisPollMsgDt, null, "getLastMissPollMsgSec").toInteger() }
def getLastApiIssueMsgSec() { return !atomicState?.lastApiIssueMsgDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastApiIssueMsgDt, null, "getLastApiIssueMsgSec").toInteger() }
def getLastLogRemindMsgSec() { return !atomicState?.lastLogRemindMsgDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastLogRemindMsgDt, null, "getLastLogRemindMsgSec").toInteger() }
def getLastFailedCmdMsgSec() { return !atomicState?.lastFailedCmdMsgDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastFailedCmdMsgDt, null, "getLastFailedCmdMsgSec").toInteger() }
def getLastDevHealthMsgSec() { return !atomicState?.lastDevHealthMsgData?.dt ? 100000 : GetTimeDiffSeconds(atomicState?.lastDevHealthMsgData?.dt, null, "getLastDevHealthMsgSec").toInteger() }
def getDebugLogsOnSec() { return !atomicState?.debugEnableDt ? 0 : GetTimeDiffSeconds(atomicState?.debugEnableDt, null, "getDebugLogsOnSec").toInteger() }

def getRecipientsSize() { return !settings.recipients ? 0 : settings?.recipients.size() }

def notificationCheck() {
	def nPrefs = atomicState?.notificationPrefs
	if(!getOk2Notify()) { return }
	apiIssueNotify(nPrefs?.app?.api?.issueMsg, nPrefs?.app?.api?.rateLimitMsg, nPrefs?.app?.api?.issueMsgWait)
	missPollNotify(nPrefs?.app?.poll?.missPollMsg, nPrefs?.app?.poll?.missPollMsgWait)
	loggingRemindNotify(nPrefs?.app?.remind?.logRemindMsg)
	if(!appDevType()) { appUpdateNotify() }
}

def cameraStreamNotify(child, Boolean streaming) {
	if(streaming == null || atomicState?.notificationPrefs?.dev?.camera?.streamMsg != true) { return }
	sendMsg("${child?.device?.displayName} Info", "Streaming is now '${streaming ? "ON" : "OFF"}'", false)
}

def deviceHealthNotify(child, Boolean isHealthy) {
	// log.trace "deviceHealthNotify(${child?.device?.displayName}, $isHealthy)"
	def nPrefs = atomicState?.notificationPrefs?.dev?.devHealth
	def devLbl = child?.device?.displayName
	def sameAsLastDev = (atomicState?.lastDevHealthMsgData?.device == devLbl)
	if(isHealthy == true || nPrefs?.healthMsg != true || (getLastDevHealthMsgSec() <= nPrefs?.healthMsgWait.toInteger() && sameAsLastDev) ) { return }
	sendMsg("$devLbl Health Warning", "\nDevice is currently OFFLINE. Please check your logs for possible issues.")
	atomicState?.lastDevHealthMsgData = ["device":"$devLbl", "dt":getDtNow()]
}

def locationPresNotify(pres) {
	if(!pres || atomicState?.notificationPrefs?.locationChg != true) { return }
	def lastStatus = atomicState?.nestLocStatus
	if(lastStatus && lastStatus != pres) {
		sendMsg("${app?.name} Nest Location Info", "\n(${atomicState?.structName}) location has been changed to [${pres.toString().capitalize()}]")
	}
	atomicState?.nestLocStatus = pres
}

def apiIssueNotify(msgOn, rateOn, wait) {
	if(!msgOn || !wait || !(getLastApiIssueMsgSec() > wait.toInteger())) { return }
	def apiIssue = apiIssues() ? true : false
	def rateLimit = (rateOn && atomicState?.apiRateLimited) ? true : false
	if((apiIssue && !atomicState?.apiIssueDt) || rateLimit) {
		def msg = ""
		msg += !rateLimit && apiIssue ? "\nThe Nest API appears to be having issues. This will effect the updating of device and location data.\nThe issues started at (${atomicState?.apiIssueDt})" : ""
		msg += rateLimit ? "${apiIssue ? "\n\n" : "\n"}Your API connection is currently being Rate-limited for excessive commands." : ""
		sendMsg("${app?.name} API Issue Warning", msg, false)
		LogAction(msg, (cmdFail ? "error" : "warn"), true)
		atomicState?.lastApiIssueMsgDt = getDtNow()
	}
}

def failedCmdNotify(failData) {
	if(!getOk2Notify() || !(getLastFailedCmdMsgSec() > 300)) { return }
	def nPrefs = atomicState?.notificationPrefs
	def cmdFail = (nPrefs?.app?.api?.cmdFailMsg && failData?.msg != null) ? true : false
	if(cmdFail) {
		def msg = "\nThe (${atomicState?.lastCmdSent}) CMD sent to the API has failed.\nStatus Code: ${failData?.code}\nErrorMsg: ${failData?.msg}\nDT: ${failData?.dt}"
		sendMsg("${app?.name} API CMD Failed", msg)
		atomicState?.lastFailedCmdMsgDt = getDtNow()
	}
}

def loggingRemindNotify(msgOn) {
	if(!msgOn || !(getLastLogRemindMsgSec() > 86400)) { return }
	def dbgAlert = (getDebugLogsOnSec() > 86400)
	if(sendOk && dbgAlert) {
		def msg = "Your debug logging has remained enabled for more than 24 hours please disable them to reduce resource usage on ST platform."
		sendMsg(("${app?.name} Debug Logging Reminder"), msg, false)
		atomicState?.lastLogRemindMsgDt = getDtNow()
	}
}

def missPollNotify(on, wait) {
	if(!on || !wait || !(getLastDevicePollSec() > atomicState?.notificationPrefs?.msgDefaultWait.toInteger())) { return }
	if(getLastMissPollMsgSec() > wait.toInteger()) {
		def msg = "\nThe app has not refreshed data in the last (${getLastDevicePollSec()}) seconds.\nPlease try refreshing data using device refresh button."
		sendMsg("${app.name} Polling Issue", msg)
		LogAction(msg, "error", true)
		atomicState?.lastMisPollMsgDt = getDtNow()
	}
}

def appUpdateNotify(force=false) {
	def on = atomicState?.notificationPrefs?.app?.updates?.updMsg
	def wait = atomicState?.notificationPrefs?.app?.updates?.updMsgWait
	if(!force && (!on || !wait)) { return }
	if(getLastUpdMsgSec() > wait.toInteger()) {
		def appUpd = isAppUpdateAvail()
		def autoappUpd = isAutoAppUpdateAvail()
		def protUpd = atomicState?.protects ? isProtUpdateAvail() : null
		def presUpd = atomicState?.presDevice ? isPresUpdateAvail() : null
		def tstatUpd = atomicState?.thermostats ? isTstatUpdateAvail() : null
		def weatherUpd = atomicState?.weatherDevice ? isWeatherUpdateAvail() : null
		def camUpd = atomicState?.cameras ? isCamUpdateAvail() : null
		if(appUpd || protUpd || presUpd || tstatUpd || weatherUpd || camUpd || vtstatUpd || force) {
			atomicState?.lastUpdMsgDt = getDtNow()
			def str = ""
			str += !force ? "" : "\nBAD AUTOMATIONS FILE, please REINSTALL automation file sources"
			str += !appUpd ? "" : "\nManager App: v${atomicState?.appData?.updater?.versions?.app?.ver?.toString()}"
			str += !autoappUpd ? "" : "\nAutomation App: v${atomicState?.appData?.updater?.versions?.autoapp?.ver?.toString()}"
			str += !protUpd ? "" : "\nProtect: v${atomicState?.appData?.updater?.versions?.protect?.ver?.toString()}"
			str += !camUpd ? "" : "\nCamera: v${atomicState?.appData?.updater?.versions?.camera?.ver?.toString()}"
			str += !presUpd ? "" : "\nPresence: v${atomicState?.appData?.updater?.versions?.presence?.ver?.toString()}"
			str += !tstatUpd ? "" : "\nThermostat: v${atomicState?.appData?.updater?.versions?.thermostat?.ver?.toString()}"
			str += !vtstatUpd ? "" : "\nVirtual Thermostat: v${atomicState?.appData?.updater?.versions?.thermostat?.ver?.toString()}"
			str += !weatherUpd ? "" : "\nWeather App: v${atomicState?.appData?.updater?.versions?.weather?.ver?.toString()}"
			sendMsg("Info", "${appName()} Update(s) are Available:${str} \n\nPlease visit the IDE to Update code", false)
		}
	}
}

def updateHandler() {
	LogTrace("updateHandler")
	if(atomicState?.isInstalled) {
		if(atomicState?.appData?.updater?.updateType.toString() == "critical" && atomicState?.lastCritUpdateInfo?.ver.toInteger() != atomicState?.appData?.updater?.updateVer.toInteger()) {
			sendMsg("Critical", "There are Critical Updates available for ${appName()}! Please visit the IDE and make sure to update the App and Devices Code")
			atomicState?.lastCritUpdateInfo = ["dt":getDtNow(), "ver":atomicState?.appData?.updater?.updateVer?.toInteger()]
		}
		if(atomicState?.appData?.updater?.updateMsg != null && atomicState?.appData?.updater?.updateMsg != atomicState?.lastUpdateMsg) {
			if(getLastUpdateMsgSec() > 86400) {
				sendMsg("Info", "${atomicState?.updater?.updateMsg}")
				atomicState.lastUpdateMsgDt = getDtNow()
				atomicState.lastUpdateMsg = atomicState?.appData?.updater?.updateMsg
			}
		}
	}
}

def getOk2Notify() { return (daysOk(settings?.quietDays) && notificationTimeOk() && modesOk(settings?.quietModes)) }

def sendMsg(msgType, msg, showEvt=true, people = null, sms = null, push = null, brdcast = null) {
	LogTrace("sendMsg")
	def sentstr = "Push"
	try {
		def newMsg = "${msgType}: ${msg}" as String
		def sent = false
		if(!getOk2Notify()) {
			LogAction("sendMsg: Skipping due to Quiet Time ($newMsg}", "info", true)
		} else {
			if(!brdcast) {
				def who = people ? people : settings?.recipients
				if(location.contactBookEnabled) {
					if(who) {
						sentstr = "Push Contacts to $who"
						sendNotificationToContacts(newMsg, who, [event: showEvt])
						sent = true
					}
				} else {
					LogAction("ContactBook is NOT Enabled on your SmartThings Account", "warn", true)
					if(push || settings?.usePush) {
						sendPush(newMsg)	// sends push and notification feed
						sent = true
					}
					def thephone = sms ? sms.toString() : settings?.phone ? settings?.phone?.toString() : ""
					if(thephone) {
						sentstr = "SMS to phone $thephone"
						def t0 = newMsg.take(140)
						sendSms(thephone as String, t0 as String)	// send SMS and notification feed
						sent = true
					}
				}
			} else {
				sentstr = "Broadcast"
				sendPush(newMsg)		// sends push and notification feed was  sendPushMessage(newMsg)  // push but no notification feed
				sent = true
			}
			if(sent) {
				atomicState?.lastMsg = newMsg
				atomicState?.lastMsgDt = getDtNow()
				LogAction("sendMsg: ${sentstr} Message Sent: ${newMsg} ${atomicState?.lastMsgDt}", "debug", true)
				incAppNotifSentCnt()
			}
		}
	} catch (ex) {
		log.error "sendMsg $sentstr Exception:", ex
		sendExceptionData(ex, "sendMsg")
	}
}

def getLastWebUpdSec() { return !atomicState?.lastWebUpdDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastWebUpdDt, null, "getLastWebUpdSec").toInteger() }
def getLastWeatherUpdSec() { return !atomicState?.lastWeatherUpdDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastWeatherUpdDt, null, "getLastWeatherUpdSec").toInteger() }
def getLastForecastUpdSec() { return !atomicState?.lastForecastUpdDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastForecastUpdDt, null, "getLastForecastUpdSec").toInteger() }
def getLastAnalyticUpdSec() { return !atomicState?.lastAnalyticUpdDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastAnalyticUpdDt, null, "getLastAnalyticUpdSec").toInteger() }
def getLastUpdateMsgSec() { return !atomicState?.lastUpdateMsgDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastUpdateMsgDt, null, "getLastUpdateMsgSec").toInteger() }

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

def updateWebStuff(now = false) {
	LogTrace("updateWebStuff")
	def nnow = now
	if(!atomicState?.appData) { nnow = true }
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
			if(canSchedule()) { runIn(20, "getWeatherConditions", [overwrite: true]) }
		}
	}
	if(atomicState?.isInstalled) {
		if(getLastAnalyticUpdSec() > (3600*24) && canSchedule()) { runIn(105, "sendInstallData", [overwrite: true]) }
	}
	if(atomicState?.feedbackPending) { sendFeedbackData() }
}

def getWeatherConditions(force = false) {
	LogTrace("getWeatherConditions")
	if(atomicState?.weatherDevice) {
		try {
			LogAction("Retrieving Local Weather Conditions", "info", false)
			def loc = ""
			def curWeather = ""
			def curForecast = ""
			def curAstronomy = ""
			def curAlerts = ""
			def err = false
			def custLoc = getCustWeatherLoc()
			if(custLoc) {
				loc = custLoc
				curWeather = getWeatherFeature("conditions", loc)
				curAlerts = getWeatherFeature("alerts", loc)
			} else {
				curWeather = getWeatherFeature("conditions")
				curAlerts = getWeatherFeature("alerts")
			}
			if(getLastForecastUpdSec() > (1800)) {
				if(custLoc) {
					loc = custLoc
					curForecast = getWeatherFeature("forecast", loc)
					curAstronomy = getWeatherFeature("astronomy", loc)
				} else {
					curForecast = getWeatherFeature("forecast")
					curAstronomy = getWeatherFeature("astronomy")
				}
				if(curForecast && curAstronomy) {
					atomicState?.curForecast = curForecast
					atomicState?.curAstronomy = curAstronomy
					atomicState?.lastForecastUpdDt = getDtNow()
				} else {
					LogAction("Could Not Retrieve Local Forecast or astronomy Conditions", "warn", true)
					err = true
				}
			}
			if(curWeather && curAlerts) {
				atomicState?.curWeather = curWeather
				atomicState?.curAlerts = curAlerts
				if(!err) { atomicState?.lastWeatherUpdDt = getDtNow() }
			} else {
				LogAction("Could Not Retrieve Local Weather Conditions or alerts", "warn", true)
				return false
			}
			if(curWeather || curAstronomy || curForecast || curAlerts) {
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

def getWData() {
	if(atomicState?.curWeather) {
		return atomicState?.curWeather
	} else {
		if(getWeatherConditions(true)) {
			return atomicState?.curWeather
		}
	}
	return null
}

def getWForecastData() {
	if(atomicState?.curForecast) {
		return atomicState?.curForecast
	} else {
		if(getWeatherConditions(true)) {
			return atomicState?.curForecast
		}
	}
	return null
}

def getWAstronomyData() {
	if(atomicState?.curAstronomy) {
		return atomicState?.curAstronomy
	} else {
		if(getWeatherConditions(true)) {
			return atomicState?.curAstronomy
		}
	}
	return null
}

def getWAlertsData() {
	if(atomicState?.curAlerts) {
		return atomicState?.curAlerts
	} else {
		if(getWeatherConditions(true)) {
			return atomicState?.curAlerts
		}
	}
	return null
}

def getWeatherDeviceInst() {
	return atomicState?.weatherDevice ? true : false
}

def getWebFileData(now = true) {
	//LogTrace("getWebFileData")
	def params = [ uri: "https://raw.githubusercontent.com/${gitPath()}/Data/appData.json", contentType: 'application/json' ]
	def result = false
	try {
		def allowAsync = false
		def metstr = "sync"
		if(!now && atomicState?.appData && atomicState?.appData?.pollMethod?.allowAsync) {
			allowAsync = true
			metstr = "async"
		}

		LogTrace("getWebFileData: Getting appData.json File(${metstr})")

		if(now || !allowAsync) {
			httpGet(params) { resp ->
				result = webResponse(resp, [type:null])
			}
		} else {
			asynchttp_v1.get(webResponse, params, [type:"async"])
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.HttpResponseException) {
			LogAction("appData.json file not found", "warn", true)
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
		LogTrace("webResponse Resp: ${newdata}")
		LogTrace("webResponse appData: ${t0}")
		if(newdata && t0 != newdata) {
			LogAction("appData.json File HAS Changed", "info", true)
			atomicState?.appData = newdata
			clientBlacklisted()
			updateHandler()
			helpHandler()
			setStateVar(true)
		} else { LogAction("appData.json did not change", "info", false) }
		getFbAppSettings(data?.type == "async" ? false : true )
		atomicState?.lastWebUpdDt = getDtNow()
		result = true
	} else {
		LogAction("Get failed appData.json status: ${resp?.status}", "warn", true)
	}
	return result
}

def getFbAppSettings(now = true) {
	def params = [ uri: "https://st-nest-manager.firebaseio.com/appSettings.json", contentType: 'application/json' ]
	def result = false
	try {
		def allowAsync = false
		def metstr = "sync"
		if(!now && atomicState?.appData && atomicState?.appData?.pollMethod?.allowAsync) {
			allowAsync = true
			metstr = "async"
		}
		LogAction("Getting appSettings.json File(${metstr})", "info", false)

		if(now || !allowAsync) {
			httpGet(params) { resp ->
				result = webFbResponse(resp, [type:null])
			}
		} else {
			asynchttp_v1.get(webFbResponse, params, [type:"async"])
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
	LogAction("webFbesponse(${data?.type})", "info", false)
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

def clientBlacklisted() {
	if(atomicState?.clientBlacklisted == null) { atomicState?.clientBlacklisted == false }
	def curBlState = atomicState?.clientBlacklisted
	if(atomicState?.isInstalled && atomicState?.appData?.clientBL) {
		def clientList = atomicState?.appData?.clientBL?.clients
		if(clientList != null || clientList != []) {
			def isBL = (atomicState?.installationId in clientList) ? true : false
			if(curBlState != isBL) {
				atomicState?.clientBlacklisted = isBL
			}
		} else { atomicState?.clientBlacklisted = false }
	} else { atomicState?.clientBlacklisted = false }
}

def broadcastCheck() {
	def bCastData = atomicState?.appData?.broadcast
	if(atomicState?.isInstalled && bCastData) {
		if(bCastData?.msgId != null && atomicState?.lastBroadcastId != bCastData?.msgId) {
			sendMsg(strCapitalize(bCastData?.type), bCastData?.message.toString(), false, null, null, null, true)
			atomicState?.lastBroadcastId = bCastData?.msgId
		}
		if(bCastData?.devBannerMsg != null && atomicState?.devBannerData?.msgId != bCastData?.devBannerMsg?.msgId) {
			if(bCastData?.devBannerMsg?.msgId && bCastData?.devBannerMsg?.message && bCastData?.devBannerMsg?.type && bCastData?.devBannerMsg?.expireDt) {
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

def helpHandler() {
	if(atomicState?.appData?.help) {
		atomicState.showHelp = (atomicState?.appData?.help?.showHelp == false) ? false : true
	}
}

def getHtmlInfo() {
	if(atomicState?.appData?.html?.cssUrl && atomicState?.appData?.html?.cssVer && atomicState?.appData?.html?.chartJsUrl && atomicState?.appData?.html?.chartJsVer ) {
		return ["cssUrl":atomicState?.appData?.html?.cssUrl, "cssVer":atomicState?.appData?.html?.cssVer, "chartJsUrl":atomicState?.appData?.html?.chartJsUrl, "chartJsVer":atomicState?.appData?.html?.chartJsVer]
	} else {
		if(getWebFileData()) {
			return ["cssUrl":atomicState?.appData?.html?.cssUrl, "cssVer":atomicState?.appData?.html?.cssVer, "chartJsUrl":atomicState?.appData?.html?.chartJsUrl, "chartJsVer":atomicState?.appData?.html?.chartJsVer]
		}
	}
}

def allowDbException() {
	if(atomicState?.appData?.database?.disableExceptions != null) {
		return atomicState?.appData?.database?.disableExceptions == true ? false : true
	} else {
		if(getWebFileData()) {
			return atomicState?.appData?.database?.disableExceptions == true ? false : true
		}
	}
}

def ver2IntArray(val) {
	def ver = val?.split("\\.")
	return [maj:"${ver[0]?.toInteger()}",min:"${ver[1]?.toInteger()}",rev:"${ver[2]?.toInteger()}"]
}
def versionStr2Int(str) { return str ? str.toString().replaceAll("\\.", "").toInteger() : null }

def getChildWaitVal() { return settings?.tempChgWaitVal ? settings?.tempChgWaitVal.toInteger() : 4 }

def getAskAlexaMQEn() {
	if(atomicState?.appData?.aaPrefs?.enAaMsgQueue == true) {
		return settings?.allowAskAlexaMQ == null ? true : setting?.allowAskAlexaMQ
	} else { return false }
}

def getAskAlexaMultiQueueEn() {
	return atomicState?.appData?.aaPrefs?.enMultiQueue == true ? true : false
}

def initAppMetricStore() {
	def items = ["mainLoadCnt", "devLocLoadCnt", "diagLoadCnt", "remDiagLoadCnt", "prefLoadCnt", "autoLoadCnt", "protTestLoadCnt", "helpLoadCnt", "infoLoadCnt", "chgLogLoadCnt", "nestLoginLoadCnt", "pollPrefLoadCnt", "devCustLoadCnt",
		"vRprtPrefLoadCnt", "notifPrefLoadCnt", "logPrefLoadCnt", "viewAutoSchedLoadCnt", "viewAutoStatLoadCnt", "autoGlobPrefLoadCnt", "devCustNameLoadCnt", "custWeathLoadCnt"]
	def data = atomicState?.usageMetricsStore ?: [:]
	items?.each { if(!data[it]) { data[it] = 0 } }
	atomicState?.usageMetricsStore = data
}
def incMetricCntVal(item) {
	def data = atomicState?.usageMetricsStore ?: [:]
	data[item] = (data[item] == null) ? 1 : data[item].toInteger()+1
	atomicState?.usageMetricsStore = data
}

def incMainLoadCnt() { incMetricCntVal("mainLoadCnt") }
def incDevLocLoadCnt() { incMetricCntVal("devLocLoadCnt") }
def incDiagLoadCnt() { incMetricCntVal("diagLoadCnt") }
def incRemDiagLoadCnt() { incMetricCntVal("remDiagLoadCnt") }
def incPrefLoadCnt() { incMetricCntVal("prefLoadCnt") }
def incInfoLoadCnt() { incMetricCntVal("infoLoadCnt") }
def incChgLogLoadCnt() { incMetricCntVal("chgLogLoadCnt") }
def incAutoLoadCnt() { incMetricCntVal("autoLoadCnt") }
def incProtTestLoadCnt() { incMetricCntVal("protTestLoadCnt") }
def incHelpLoadCnt() { incMetricCntVal("helpLoadCnt") }
def incNestLoginLoadCnt() { incMetricCntVal("nestLoginLoadCnt") }
def incDevCustNameLoadCnt() { incMetricCntVal("devCustNameLoadCnt") }
def incCustWeathLoadCnt() { incMetricCntVal("custWeathLoadCnt") }
def incPollPrefLoadCnt() { incMetricCntVal("pollPrefLoadCnt") }
def incDevCustLoadCnt() { incMetricCntVal("devCustLoadCnt") }
def incVrprtPrefLoadCnt() { incMetricCntVal("vRprtPrefLoadCnt") }
def incNotifPrefLoadCnt() { incMetricCntVal("notifPrefLoadCnt") }
def incAppNotifPrefLoadCnt() { incMetricCntVal("appNotifPrefLoadCnt") }
def incLogPrefLoadCnt() { incMetricCntVal("logPrefLoadCnt") }
def incViewAutoSchedLoadCnt() { incMetricCntVal("viewAutoSchedLoadCnt") }
def incViewAutoStatLoadCnt() { incMetricCntVal("viewAutoStatLoadCnt") }
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
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.app?.ver, appVersion(), "manager")) { return true }
	return false
}

def isAutoAppUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.autoapp?.ver, atomicState?.autoSaVer, "automation")) { return true }
	return false
}

def isPresUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.presence?.ver, atomicState?.presDevVer, "presence")) { return true }
	return false
}

def isProtUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.protect?.ver, atomicState?.pDevVer, "protect")) { return true }
	return false
}

def isCamUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.camera?.ver, atomicState?.camDevVer, "camera")) { return true }
	return false
}

def isTstatUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.thermostat?.ver, atomicState?.tDevVer, "thermostat")) { return true }
	return false
}

def isWeatherUpdateAvail() {
	if(isCodeUpdateAvailable(atomicState?.appData?.updater?.versions?.weather?.ver, atomicState?.weatDevVer, "weather")) { return true }
	return false
}

def reqSchedInfoRprt(child, report=true) {
	LogTrace("reqSchedInfoRprt: (${child.device.label})")
	def result = null
	if(!atomicState?.installData?.usingNewAutoFile) { return result }

	def tstat = getChildDevice(child.device.deviceNetworkId)
	if (tstat) {
		def str = ""
		def tstatAutoApp = getChildApps()?.find {
			//def aa = it.getAutomationType()
			//def bb = it.getTstatAutoDevId()
			//LogTrace("aa: ${aa} bb: ${bb} dni: ${tstat?.deviceNetworkId}")
			(it.getAutomationType() == "schMot" && it?.getTstatAutoDevId() == tstat?.deviceNetworkId)
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
				def canHeat = tstat?.currentCanHeat.toString() == "true" ? true : false
				def canCool = tstat?.currentCanCool.toString() == "true" ? true : false
				def curMode = tstat?.currentnestThermostatMode.toString()
				def curOper = tstat?.currentThermostatOperatingState.toString()
				def curHum = tstat?.currentHumidity.toString()
				def schedDesc = schedVoiceDesc(actSchedNum, schedData, schedMotionActive)
				str += schedDesc ?: " There are No Schedules currently Active. "

				if(getVoiceRprtPrefs()?.vRprtZone == true) {
					if(tempSrcStr && curZoneTemp) {
						def zTmp = curZoneTemp.toDouble()
						str += "The ${tempSrcStr} has an ambient temperature of "
						if(zTmp > adj_temp(78.0) && zTmp <= adj_temp(85.0)) { str += "a scorching " }
						else if(zTmp > adj_temp(76.0) && zTmp <= adj_temp(80.0)) { str += "a roasting " }
						else if(zTmp > adj_temp(74.0) && zTmp <= adj_temp(76.0)) { str += "a balmy " }
						else if(zTmp >= adj_temp(68.0) && zTmp <= adj_temp(74.0)) { str += "a comfortable " }
						else if(zTmp >= adj_temp(64.0) && zTmp <= adj_temp(68.0)) { str += "a breezy " }
						else if(zTmp >= adj_temp(60.0) && zTmp < adj_temp(64.0)) { str += "a chilly " }
						else if(zTmp < adj_temp(60.0)) { str += "a freezing " }
						str += "${curZoneTemp}${tempScaleStr}"
						str += curHum ? " with a humidity of ${curHum}%. " : ". "
						if(zTmp < adj_temp(60.0)) { str += " (Make sure to dress warmly.  " }
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
				str += canHeat && canCool && curMode == "auto" ? " and " : ". "
				str += canCool && curMode in ["auto", "cool", "eco"] ? "the cool set to ${reqSenCoolSetPoint}${tempScaleStr}.  " : ""

				if (str != "") {
					LogAction("reqSchedInfoRprt: Sending voice report for [$str] to (${tstat})", "info", false)
					result = str
				}
			}
		} else {
			//LogAction ("reqSchedInfoRprt: No Automation Schedules were found for ${tstat} device", "warn", false)
			if(report) {
				result = "No Thermostat Automation Schedules were found for ${tstat} device"
			}
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
	str += data?.lbl  ? " The automation schedule slot active is number ${num} and is labeled ${data?.lbl}. " : ""
	str += (!motion && (data?.ctemp || data?.htemp)) ? "The schedules desired temps" : ""
	str += (motion && (data?.mctemp || data?.mhtemp)) ? "The schedules desired motion triggered temps" : ""
	str += ((motion && data?.mhtemp) || (!motion && data?.htemp)) ? " are set to a heat temp of ${!motion ? fixTempSetting(data?.htemp) : fixTempSetting(data?.mhtemp)} degrees" : ""
	str += ((motion && data?.mctemp) || (!motion && data?.ctemp)) ? " and " : ". "
	str += ((motion && data?.mctemp) || (!motion && data?.ctemp)) ? " ${((!motion && !data?.htemp) || (motion && !data?.mhtemp)) ? "are" : ""} a cool temp of ${!motion ? fixTempSetting(data?.ctemp) : fixTempSetting(data?.mctemp)} degrees. " : ""
	return str != "" ? str : null
}

/************************************************************************************************
|			This Section Discovers all structures and devices on your Nest Account.				|
|			It also Adds/Removes Devices from ST												|
*************************************************************************************************/

def getNestStructures() {
	LogTrace("Getting Nest Structures")
	def struct = [:]
	def thisstruct = [:]
	try {
		if(ok2PollStruct()) { getApiData("str") }
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
			if(ok2PollDevice()) { getApiData("dev") }
		} else { LogAction("Missing: structData  ${atomicState?.structData}", "warn", true) }

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
}

def getProtectDisplayName(prot) {
	if(prot?.name) { return prot.name.toString() }
}

def getCameraDisplayName(cam) {
	if(cam?.name) { return cam.name.toString() }
}

def getNestDeviceDni(dni, type) {
	//LogTrace("getNestDeviceDni: $dni | $type")
	def d1 = getChildDevice(dni?.key.toString())
	if(d1) { return dni?.key.toString() }
	else { return "Nest${type}-${dni?.value.toString()}${appDevName()} | ${dni?.key.toString()}" }
	//LogAction("getNestDeviceDni ($type) Issue", "warn", true)
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
		def devt =  appDevName()
		if(settings?.structures) { retVal = "NestPres${devt} | ${settings?.structures}" }
		else if(atomicState?.structures) { retVal = "NestPres${devt} | ${atomicState?.structures}" }
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
		if(settings?.structures) { retVal = "NestWeather${devt} | ${settings?.structures}" }
		else if(atomicState?.structures) { retVal = "NestWeather${devt} | ${atomicState?.structures}" }
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
			def wLbl = getCustWeatherLoc() ? getCustWeatherLoc().toString() : "${getStZipCode()}"
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
	try {
		def devsInUse = []
		def tstats
		def nProtects
		def nCameras
		def nVstats
		def devsCrt = 0
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
					LogAction("Nest Presence Device may not be installed/published", "warn", true)
					retVal = false
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
						atomicState?.lastWeatherUpdDt = null
						atomicState?.lastForecastUpdDt = null
						devsCrt = devsCrt + 1
						LogAction("Created: ${d4.displayName} with (Id: ${dni})", "debug", true)
					} else {
						LogAction("Found: ${d4.displayName} with (Id: ${dni}) exists", "debug", true)
					}
					devsInUse += dni
				} catch (ex) {
					LogAction("Nest Weather Device Type may not be installed/published", "warn", true)
					retVal = false
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
					LogAction("atomicState.vThermostats: ${atomicState.vThermostats}  dni: ${dni}  dni.key: ${dni.key.toString()}  dni.value: ${dni.value.toString()}", "debug", true)
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
				LogAction("Created Devices;  Current Devices: (${tstats?.size()}) Thermostat(s), (${nVstats?.size() ?: 0}) Virtual Thermostat(s), (${nProtects?.size() ?: 0}) Protect(s), (${nCameras?.size() ?: 0}) Cameras(s), ${presCnt} Presence Device and ${weathCnt} Weather Device", "debug", true)
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
			atomicState?.curWeather = null
			atomicState?.curForecast = null
			atomicState?.curAstronomy = null
			atomicState?.curAlerts = null
		}

		def delete
		LogAction("devicesInUse: ${devsInUse}", "debug", false)
		delete = app.getChildDevices(true).findAll { !devsInUse?.toString()?.contains(it?.deviceNetworkId) }

		if(delete?.size() > 0) {
			LogAction("Removing ${delete.size()} devices: ${delete}", "debug", true)
			delete.each { deleteChildDevice(it.deviceNetworkId) }
		}
		retVal = true
		//currentDevMap(true)
	} catch (ex) {
		if(ex instanceof physicalgraph.exception.ConflictException) {
			def msg = "Error: Can't Remove Device.  One or more of them are still in use by other SmartApps or Routines.  Please remove them and try again!"
			sendPush(msg)
			LogAction("addRemoveDevices Exception | $msg", "warn", true)
		}
		else if(ex instanceof physicalgraph.app.exception.UnknownDeviceTypeException) {
			def msg = "Error: Device Handlers are Missing or Not Published.  Please verify all device handlers are present before continuing."
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

def setMyLockId(val) {
	if(atomicState?.myID == null && parent && val) {
		atomicState.myID = val
	}
}

def getMyLockId() {
	if(parent) { return atomicState?.myID } else { return null }
}

def addRemoveVthermostat(tstatdni, tval, myID) {
	def odevId = tstatdni
	LogAction("addRemoveVthermostat() tstat: ${tstatdni}   devid: ${odevId}   tval: ${tval}   myID: ${myID} vThermostats: ${atomicState?.vThermostats} ", "trace", true)

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

	def migrate = migrationInProgress()

	if(!migrate && atomicState?."vThermostat${devId}" && myID != atomicState?."vThermostatChildAppId${devId}") {
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
			if(!migrate) { runIn(10, "updated", [overwrite: true]) }  // create what is needed

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
			if(!migrate) { runIn(10, "updated", [overwrite: true]) }  // create what is needed
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
	try {
		def d1 = addChildDevice(app.namespace, getThermostatChildName(), "testNestThermostat-Install123", null, [label:"Nest Thermostat:InstallTest"])
		def d2 = addChildDevice(app.namespace, getPresenceChildName(), "testNestPresence-Install123", null, [label:"Nest Presence:InstallTest"])
		def d3 = addChildDevice(app.namespace, getProtectChildName(), "testNestProtect-Install123", null, [label:"Nest Protect:InstallTest"])
		def d4 = addChildDevice(app.namespace, getWeatherChildName(), "testNestWeather-Install123", null, [label:"Nest Weather:InstallTest"])
		def d5 = addChildDevice(app.namespace, getCameraChildName(), "testNestCamera-Install123", null, [label:"Nest Camera:InstallTest"])

		LogAction("d1: ${d1.label} | d2: ${d2.label} | d3: ${d3.label} | d4: ${d4.label} | d5: ${d5.label}", "debug", true)
		atomicState.devHandlersTested = true
		removeTestDevs()
		//runIn(4, "removeTestDevs")
		return true
	}
	catch (ex) {
		if(ex instanceof physicalgraph.app.exception.UnknownDeviceTypeException) {
			LogAction("Device Handlers are missing: ${getThermostatChildName()}, ${getPresenceChildName()}, and ${getProtectChildName()}, Verify the Device Handlers are installed and Published via the IDE", "error", true)
		} else {
			log.error "deviceHandlerTest Exception:", ex
			sendExceptionData(ex, "deviceHandlerTest")
		}
		atomicState.devHandlersTested = false
		return false
	}
}

def removeTestDevs() {
	try {
		def names = [ "testNestThermostat-Install123", "testNestPresence-Install123", "testNestProtect-Install123", "testNestWeather-Install123", "testNestCamera-Install123" ]
		names?.each { dev ->
			//log.debug "dev: $dev"
			def delete = app.getChildDevices(true).findAll { it?.deviceNetworkId == dev }
			//log.debug "delete: ${delete}"
			if(delete) {
				delete.each { deleteChildDevice(it.deviceNetworkId) }
			}
		}
	} catch (ex) {
		log.error "deviceHandlerTest Exception:", ex
		sendExceptionData(ex, "removeTestDevs")
	}
}

def preReqCheck() {
	//LogTrace("preReqCheckTest()")
	if(!atomicState?.installData) { atomicState?.installData = ["initVer":appVersion(), "dt":"Not Set", "updatedDt":"Not Set", "freshInstall":false, "shownDonation":false, "shownChgLog":true, "shownFeedback":false] }
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
		def msg = "Error: OAuth is not Enabled for ${appName()}!.  Please click remove and Enable Oauth under the SmartApp App Settings in the IDE"
		sendPush(msg)
		log.error "getAccessToken Exception", ex
		LogAction("getAccessToken Exception | $msg", "warn", true)
		sendExceptionData(ex, "getAccessToken")
		return false
	}
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
				atomicState.tokenExpires = resp?.data.expires_in
				atomicState.authToken = resp?.data.access_token
				if(atomicState?.authToken) { atomicState?.tokenCreatedDt = getDtNow() }
			}

			if(atomicState?.authToken) {
				LogAction("Nest AuthToken Generated SUCCESSFULLY", "info", true)
				success()
			} else {
				LogAction("Failure Generating Nest AuthToken", "error", true)
				fail()
			}
		}
		else { LogAction("callback() oauthState != atomicState.oauthInitState", "error", true) }
	}
	catch (ex) {
		log.error "Callback Exception:", ex
		sendExceptionData(ex, "callback")
	}
}

def revokeNestToken() {
	if(atomicState?.authToken) {
		def params = [
			uri: "https://api.home.nest.com",
			path: "/oauth2/access_tokens/${atomicState?.authToken}",
			contentType: 'application/json'
		]
		try {
			httpDelete(params) { resp ->
				atomicState.authToken = null
				if(resp?.status == 204) {
					LogAction("Nest Token revoked", "warn", true)
					return true
				}
			}
		}
		catch (ex) {
			if(ex?.message?.toString() == "Not Found") {
				return true
			} else {
				log.error "revokeNestToken Exception:", ex
				atomicState.authToken = null
				sendExceptionData(ex, "revokeNestToken")
				return false
			}
		}
	}
}

//HTML Connections Pages
def success() {
	def message = """
	<p>Your SmartThings Account is now connected to Nest!</p>
	<p>Click 'Done' to finish setup.</p>
	"""
	connectionStatus(message)
}

def fail() {
	def message = """
	<p>The connection could not be established!</p>
	<p>Click 'Done' to return to the menu.</p>
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
		<meta name="viewport" content="width=640">
		<title>SmartThings & Nest connection</title>
		<style type="text/css">
				@font-face {
						font-family: 'Swiss 721 W01 Thin';
						src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot');
						src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot?#iefix') format('embedded-opentype'),
								url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.woff') format('woff'),
								url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.ttf') format('truetype'),
								url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.svg#swis721_th_btthin') format('svg');
						font-weight: normal;
						font-style: normal;
				}
				@font-face {
						font-family: 'Swiss 721 W01 Light';
						src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot');
						src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot?#iefix') format('embedded-opentype'),
								url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.woff') format('woff'),
								url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.ttf') format('truetype'),
								url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.svg#swis721_lt_btlight') format('svg');
						font-weight: normal;
						font-style: normal;
				}
				.container {
						width: 90%;
						padding: 4%;
						/*background: #eee;*/
						text-align: center;
				}
				img {
						vertical-align: middle;
				}
				p {
						font-size: 2.2em;
						font-family: 'Swiss 721 W01 Thin';
						text-align: center;
						color: #666666;
						padding: 0 40px;
						margin-bottom: 0;
				}
				span {
						font-family: 'Swiss 721 W01 Light';
				}
		</style>
		</head>
		<body>
			<div class="container">
				<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
				<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
				<img src="${getAppImg("nst_manager_icon%402x.png")}" alt="nest icon" width="215" height="215"/>
				${message}
			</div>
		</body>
		</html>
		"""
	render contentType: 'text/html', data: html
}

def toJson(Map m) {
	return new org.json.JSONObject(m).toString()
}

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def clientId() {
	if(appSettings.clientId) {
		return appSettings.clientId
	} else {
		if(atomicState?.appData?.token?.id) {
			return atomicState?.appData?.token?.id
		} else { LogAction("clientId is missing and is required to generate your Nest Auth token.  Please verify you are running the latest software version", "error", true) }
	}
}

def clientSecret() {
	if(appSettings.clientSecret) {
		return appSettings.clientSecret
	} else {
		if(atomicState?.appData?.token?.secret) {
			return atomicState?.appData?.token?.secret
		} else { LogAction("clientSecret is missing and is required to generate your Nest Auth token.  Please verify you are running the latest software version", "error", true) }
	}
}

/************************************************************************************************
|									LOGGING AND Diagnostic										|
*************************************************************************************************/
def LogTrace(msg, logSrc=null) {
	def trOn = (appDebug && advAppDebug) ? true : false
	if(trOn) {
		def theLogSrc = (logSrc == null) ? (parent ? "Automation" : "NestManager") : logSrc
		Logger(msg, "trace", theLogSrc)
	}
}

def LogAction(msg, type="debug", showAlways=false, logSrc=null) {
	def isDbg = parent ? (showDebug  ? true : false) : (appDebug ? true : false)
	def theLogSrc = (logSrc == null) ? (parent ? "Automation" : "NestManager") : logSrc
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

def Logger(msg, type, logSrc=null) {
	if(msg && type) {
		def labelstr = ""
		if(atomicState?.debugAppendAppName == null) {
			def tval = parent ? parent?.settings?.debugAppendAppName : settings?.debugAppendAppName
			atomicState?.debugAppendAppName = (tval || tval == null) ? true : false
		}
		if(atomicState?.debugAppendAppName) { labelstr = "${app.label} | " }
		def themsg = tokenStrScrubber("${labelstr}${msg}")

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
		//log.debug "Logger remDiagTest: $msg | $type | $logSrc"
		if(!parent) { saveLogtoRemDiagStore(themsg, type, logSrc) }
		else {
			if(atomicState?.enRemDiagLogging == null) {
				atomicState?.enRemDiagLogging = parent?.state?.enRemDiagLogging
				if(atomicState?.enRemDiagLogging == null) {
					atomicState?.enRemDiagLogging = false
				}
				//log.debug "set enRemDiagLogging to ${atomicState?.enRemDiagLogging}"
			}
			if(atomicState?.enRemDiagLogging) {
				parent.saveLogtoRemDiagStore(themsg, type, logSrc)
			}
		}
	}
	else { log.error "${labelstr}Logger Error - type: ${type} | msg: ${msg} | logSrc: ${logSrc}" }
}

def fixState() {
	def result = false
	LogAction("fixState", "info", false)
	def before = getStateSizePerc()
	if(!parent) {
		if(!atomicState?.resetAllData && resetAllData) {
			def data = getState()?.findAll { !(it?.key in ["accessToken", "authToken", "enRemDiagLogging", "installationId", "remDiagLogActivatedDt", "remDiagLogDataStore", "remDiagDataSentDt", "remDiagLogSentCnt", "resetAllData", "pollingOn", "apiCommandCnt", "autoMigrationComplete" ]) }
			data.each { item ->
				state.remove(item?.key.toString())
			}
			unschedule()
			unsubscribe()
			atomicState.ssdpOn = false
			atomicState.pollingOn = false
			atomicState?.pollBlocked = true
			result = true
		} else if(atomicState?.resetAllData && !resetAllData) {
			LogAction("fixState: resetting ALL toggle", "info", true)
			atomicState.resetAllData = false
		}

	}
	if(result) {
		atomicState.resetAllData = true
		LogAction("fixState: State Data: before: $before  after: ${getStateSizePerc()}", "info", true)
		runIn(20, "finishFixState", [overwrite: true])
	}
	return result
}

void finishFixState() {
	LogAction("finishFixState", "info", false)
	if(!parent) {
		if(atomicState?.resetAllData) {
			atomicState.devNameOverride = settings?.devNameOverride ? true : false
			atomicState.useAltNames = settings?.useAltNames ? true : false
			atomicState.custLabelUsed = settings?.useCustDevNames ? true : false
			if(!atomicState?.installData) { atomicState?.installData = ["initVer":appVersion(), "dt":getDtNow().toString(), "updatedDt":"Not Set", "freshInstall":false, "shownDonation":false, "shownChgLog":true, "shownFeedback":false] }

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

// TODO ERS
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
	} else {
		LogAction("finishFixState called as CHILD", "error", true)
	}
}

void settingUpdate(name, value, type=null) {
	LogAction("settingUpdate($name, $value, $type)...", "trace", false)
//	try {
		//if(name && value && type) {
		if(name && type) {
			app?.updateSetting("$name", [type: "$type", value: value])
		}
		//else if (name && value && type == null){ app?.updateSetting(name.toString(), value) }
		else if (name && type == null){ app?.updateSetting(name.toString(), value) }
/*
	} catch(e) {
		log.error "settingUpdate Exception:", ex
	}
*/
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
	if(!atomicState?.stateVarUpd || frc || (stateVer < atomicState?.appData.state.stateVarVer.toInteger())) {
		if(!atomicState?.newSetupComplete)		{ atomicState.newSetupComplete = false }
		if(!atomicState?.setupVersion)			{ atomicState?.setupVersion = 0 }
		if(!atomicState?.custLabelUsed)			{ atomicState?.custLabelUsed = false }
		if(!atomicState?.useAltNames)			{ atomicState.useAltNames = false }
		if(!atomicState?.apiCommandCnt)			{ atomicState?.apiCommandCnt = 0L }
		atomicState?.stateVarUpd = true
		atomicState?.stateVarVer = atomicState?.appData?.state?.stateVarVer ? atomicState?.appData?.state?.stateVarVer?.toInteger() : 0
	}
}

//Things that I need to clear up on updates go here
def stateCleanup() {
	LogAction("stateCleanup", "trace", true)

	def data = [ "exLogs", "pollValue", "pollStrValue", "pollWaitVal", "tempChgWaitVal", "cmdDelayVal", "testedDhInst", "missedPollNotif", "updateMsgNotif", "updChildOnNewOnly", "disAppIcons",
		"showProtAlarmStateEvts", "showAwayAsAuto", "cmdQ", "recentSendCmd", "currentWeather", "altNames", "locstr", "custLocStr", "autoAppInstalled", "nestStructures", "lastSentExceptionDataDt",
		"tDevVer", "pDevVer", "camDevVer", "presDevVer", "weatDevVer", "vtDevVer", "dashSetup", "dashboardUrl", "apiIssues", "stateSize", "haveRun", "lastStMode", "lastPresSenAway", "automationsActive",
		"temperatures", "powers", "energies", "use24Time", "useMilitaryTime", "advAppDebug", "appDebug", "awayModes", "homeModes", "childDebug", "updNotifyWaitVal", "appApiIssuesWaitVal",
		"misPollNotifyWaitVal", "misPollNotifyMsgWaitVal", "devHealthMsgWaitVal", "nestLocAway", "heardFromRestDt", "autoSaVer", "lastAnalyticUpdDt"
 	]
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
	atomicState.forceChildUpd = true
	def sdata = [ "showAwayAsAuto", "temperatures", "powers", "energies", "childDevDataPageDev", "childDevDataRfsh", "childDevDataStateFilter", "childDevPageShowAttr", "childDevPageShowCapab", "childDevPageShowCmds" ]
	sdata.each { item ->
		if(settings?."${item}" != null) {
			settingUpdate("${item.toString()}", "")	// clear settings
		}
	}
}

/******************************************************************************
*								STATIC METHODS								  *
*******************************************************************************/
def getThermostatChildName()	{ return getChildName("Nest Thermostat") }
def getProtectChildName()	{ return getChildName("Nest Protect") }
def getPresenceChildName()	{ return getChildName("Nest Presence") }
def getWeatherChildName()	{ return getChildName("Nest Weather") }
def getCameraChildName()	{ return getChildName("Nest Camera") }

def getAutoAppChildName()	{ return getChildName(autoAppName()) }
def getWatDogAppChildName()	{ return getChildName("Nest Location ${location.name} Watchdog") }

def getChildName(str)		{ return "${str}${appDevName()}" }

def getServerUrl()			{ return "https://graph.api.smartthings.com" }
def getShardUrl()			{ return getApiServerUrl() }
def getCallbackUrl()		{ return "https://graph.api.smartthings.com/oauth/callback" }
def getBuildRedirectUrl()	{ return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState?.accessToken}&apiServerUrl=${shardUrl}" }
def getNestApiUrl()			{ return "https://developer-api.nest.com" }
def getAppEndpointUrl(subPath)	{ return "${apiServerUrl("/api/smartapps/installations/${app.id}/${subPath}?access_token=${atomicState.accessToken}")}" }
def getHelpPageUrl()		{ return "http://thingsthataresmart.wiki/index.php?title=NST_Manager" }
def getIssuePageUrl()		{ return "https://github.com/tonesto7/nest-manager/issues" }
def slackMsgWebHookUrl()	{ return "https://hooks.slack.com/services/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" }
def getAutoHelpPageUrl()	{ return "http://thingsthataresmart.wiki/index.php?title=NST_Manager#Nest_Automations" }
def weatherApiKey()			{ return "b82aba1bb9a9d7f1" }
def getFirebaseAppUrl() 	{ return "https://st-nest-manager.firebaseio.com" }
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
def getStateSize()	{ return state?.toString().length() }
def getStateSizePerc()  { return (int) ((stateSize/100000)*100).toDouble().round(0) }

def debugStatus() { return !appDebug ? "Off" : "On" }
def deviceDebugStatus() { return !childDebug ? "Off" : "On" }
def isAppDebug() { return !appDebug ? false : true }
def isChildDebug() { return !childDebug ? false : true }

def getLocationModes() {
	def result = []
	location?.modes.sort().each {
		if(it) { result.push("${it}") }
	}
	return result
}

def showDonationOk() {
	return (!atomicState?.installData?.shownDonation && getDaysSinceInstall() >= 30) ? true : false
}

def showFeedbackOk() {
	return (!atomicState?.installData?.shownFeedback && getDaysSinceInstall() >= 7) ? true : false
}

def showChgLogOk() {
	return (!atomicState?.installData?.shownChgLog && atomicState?.isInstalled) ? true : false
}

def getDaysSinceInstall() {
	def instDt = atomicState?.installData?.dt
	if(instDt == null || instDt == "Not Set") { return 0 }
	def start = Date.parse("E MMM dd HH:mm:ss z yyyy", instDt)
	def stop = new Date()
	if(start && stop) {
		return (stop - start)
	}
	return 0
}

def getObjType(obj, retType=false) {
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

def formatDt2(tm) {
	def formatVal = settings?.useMilitaryTime ? "MMM d, yyyy - HH:mm:ss" : "MMM d, yyyy - h:mm:ss a"
	def tf = new SimpleDateFormat(formatVal)
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	return tf.format(Date.parse("E MMM dd HH:mm:ss z yyyy", tm.toString()))
}

def GetTimeDiffSeconds(strtDate, stpDate=null, methName=null) {
	//LogTrace("[GetTimeDiffSeconds] StartDate: $strtDate | StopDate: ${stpDate ?: "Not Sent"} | MethodName: ${methName ?: "Not Sent"})")
	if((strtDate && !stpDate) || (strtDate && stpDate)) {
		//if(strtDate?.contains("dtNow")) { return 10000 }
		def now = new Date()
		def stopVal = stpDate ? stpDate.toString() : formatDt(now)
		def startDt = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate)
		def stopDt = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal)
		def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(startDt)).getTime()
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
//	try {
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
/*
	} catch (ex) {
		log.error "notificationTimeOk Exception:", ex
		sendExceptionData(ex, "notificationTimeOk")
	}
*/
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
	if(device && inReview()) { vals = [ 30:"30 Seconds" ] }
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
	if(input && enumName) {
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

/*
def b64Action(String str, dec=false) {
	if (str) {
		if(dec) {
			return (String) str?.bytes?.decodeBase64()
		} else {
			return (String) str?.bytes?.encodeBase64(true)
		}
	}
}
*/

/******************************************************************************
*					 	DIAGNOSTIC & NEST API INFO PAGES		  	  		  *
*******************************************************************************/
def nestInfoPage () {
	dynamicPage(name: "nestInfoPage", install: false) {
		if(atomicState?.protects) {
			section("Nest Protect Alarm Simulation:") {
				if(atomicState?.protects) {
					def dt = atomicState?.isAlarmCoTestActiveDt
					href "alarmTestPage", title: "Test Protect Automations\nBy Simulating Alarm Events", description: "${dt ? "Last Tested:\n$dt\n\n" : ""}Tap to Begin...", image: getAppImg("test_icon.png")
				}
			}
		}
		section("View All API Data Received from Nest:") {
			if(atomicState?.structures) {
				href "structInfoPage", title: "Nest Location(s) Info", description: "Tap to view", image: getAppImg("nest_structure_icon.png")
			}
			if(atomicState?.thermostats) {
				href "tstatInfoPage", title: "Nest Thermostat(s) Info", description: "Tap to view", image: getAppImg("nest_like.png")
			}
			if(atomicState?.protects) {
				href "protInfoPage", title: "Nest Protect(s) Info", description: "Tap to view", image: getAppImg("protect_icon.png")
			}
			if(atomicState?.cameras) {
				href "camInfoPage", title: "Nest Camera(s) Info", description: "Tap to view", image: getAppImg("camera_icon.png")
			}
			if(!atomicState?.structures && !atomicState?.thermostats && !atomicState?.protects && !atomicState?.cameras) {
				paragraph "There is nothing to show here", image: getAppImg("instruct_icon.png")
			}
		}
		section("Diagnostics") {
			href "diagPage", title: "View Diagnostic Info", description: null, image: getAppImg("diag_icon.png")
			href "remoteDiagPage", title: "Send Logs to Developer", description: "", image: getAppImg("diagnostic_icon.png")
		}
	}
}

def structInfoPage () {
	dynamicPage(name: "structInfoPage", refreshInterval: 30, install: false) {
		def noShow = [ "wheres", "cameras", "thermostats", "smoke_co_alarms", "structure_id" ]
		section("") {
			paragraph "Locations", state: "complete", image: getAppImg("nest_structure_icon.png")
		}
		atomicState?.structData?.each { struc ->
			if(struc?.key == atomicState?.structures) {
				def str = ""
				def cnt = 0
				section("Location Name: ${struc?.value?.name}") {
					def data = struc?.value.findAll { !(it.key in noShow) }
					data?.sort().each { item ->
						cnt = cnt+1
						str += "${(cnt <= 1) ? "" : "\n\n"}• ${item?.key?.toString()}: (${item?.value})"
					}
					paragraph "${str}"
				}
			}
		}
	}
}

def tstatInfoPage () {
	dynamicPage(name: "tstatInfoPage", refreshInterval: 30, install: false) {
		def noShow = [ "where_id", "device_id", "structure_id" ]
		section("") {
			paragraph "Thermostats", state: "complete", image: getAppImg("nest_like.png")
		}
		atomicState?.thermostats?.sort().each { tstat ->
			def str = ""
			def cnt = 0
			section("Thermostat Name: ${atomicState?.deviceData?.thermostats[tstat?.key]?.name}") {    // was ${tstat?.value}
				def data = atomicState?.deviceData?.thermostats[tstat?.key].findAll { !(it.key in noShow) }
				data?.sort().each { item ->
					cnt = cnt+1
					str += "${(cnt <= 1) ? "" : "\n\n"}• ${item?.key?.toString()}: (${item?.value})"
				}
				paragraph "${str}"
			}
		}
	}
}

def protInfoPage () {
	dynamicPage(name: "protInfoPage", refreshInterval: 30, install: false) {
		def noShow = [ "where_id", "device_id", "structure_id" ]
		section("") {
			paragraph "Protects", state: "complete", image: getAppImg("protect_icon.png")
		}
		atomicState?.protects.sort().each { prot ->
			def str = ""
			def cnt = 0
			section("Protect Name: ${atomicState?.deviceData?.smoke_co_alarms[prot?.key]?.name}") {   // was ${prot?.value}
				def data = atomicState?.deviceData?.smoke_co_alarms[prot?.key].findAll { !(it.key in noShow) }
				data?.sort().each { item ->
					cnt = cnt+1
					str += "${(cnt <= 1) ? "" : "\n\n"}• ${item?.key?.toString()}: (${item?.value})"
				}
				paragraph "${str}"
			}
		}
	}
}

def camInfoPage () {
	dynamicPage(name: "camInfoPage", refreshInterval: 30, install: false) {
		def noShow = [ "where_id", "device_id", "structure_id" ]
		section("") {
			paragraph "Cameras", state: "complete", image: getAppImg("camera_icon.png")
		}
		atomicState?.cameras.sort().each { cam ->
			def str = ""
			def evtStr = ""
			def cnt = 0
			def cnt2 = 0
			section("Camera Name: ${atomicState?.deviceData?.cameras[cam?.key]?.name}") {	// was ${cam?.value}
				def data = atomicState?.deviceData?.cameras[cam?.key].findAll { !(it.key in noShow) }
				data?.sort().each { item ->
					if(item?.key != "last_event") {
						if(item?.key in ["app_url", "web_url"]) {
							href url: item?.value, style:"external", required: false, title: item?.key.toString().replaceAll("\\_", " ").capitalize(), description:"Tap to view in Browser", state: "complete"
						} else {
							cnt = cnt+1
							str += "${(cnt <= 1) ? "" : "\n\n"}• ${item?.key?.toString()}: (${item?.value})"
						}
					} else {
						item?.value?.sort().each { item2 ->
							if(item2?.key in ["app_url", "web_url", "image_url", "animated_image_url"]) {
								href url: item2?.value, style:"external", required: false, title: "LastEvent: ${item2?.key.toString().replaceAll("\\_", " ").capitalize()}", description:"Tap to view in Browser", state: "complete"
							}
							else {
								cnt2 = cnt2+1
								evtStr += "${(cnt2 <= 1) ? "" : "\n\n"}  • (LastEvent) ${item2?.key?.toString()}: (${item2?.value})"
							}
						}
					}
				}
				paragraph "${str}"
				if(evtStr != "") {
					paragraph "Last Event Data:\n\n${evtStr}"
				}
			}
		}
	}
}

def alarmTestPage () {
	dynamicPage(name: "alarmTestPage", install: false, uninstall: false) {
		if(atomicState?.protects) {
			section("Select Carbon/Smoke Device to Test:") {
				input(name: "alarmCoTestDevice", title:"Select the Protect to Test", type: "enum", required: false, multiple: false, submitOnChange: true,
						metadata: [values:atomicState?.protects], image: getAppImg("protect_icon.png"))
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
	}
}

void resetAlarmTest() {
	LogAction("Resetting Protect Alarm Test back to the default.", "info", true)
	settingUpdate("alarmCoTestDevice", "")
	settingUpdate("alarmCoTestDeviceSimSmoke", "false")
	settingUpdate("alarmCoTestDeviceSimCo", "false")
	settingUpdate("alarmCoTestDeviceSimLowBatt", "false")
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

def diagPage () {
	dynamicPage(name: "diagPage", install: false) {
		section("") {
			paragraph "This page allows viewing diagnostic data for apps/devices to assist in troubleshooting", image: getAppImg("diag_icon.png")
		}
		section("State Size Info:") {
			paragraph "Current State Usage:\n${getStateSizePerc()}% (${getStateSize()} bytes)", required: true, state: (getStateSizePerc() <= 70 ? "complete" : null),
					image: getAppImg("progress_bar.png")
		}
		section("View App & Device Data") {
			href "managAppDataPage", title:"Manager App Data", description:"Tap to view", image: getAppImg("nest_manager.png")
			href "childAppDataPage", title:"Automation App Data", description:"Tap to view", image: getAppImg("automation_icon.png")
			href "childDevDataPage", title:"Device Data", description:"Tap to view", image: getAppImg("thermostat_icon.png")
			href "appParamsDataPage", title:"AppData File", description:"Tap to view", image: getAppImg("view_icon.png")
		}
		if(settings?.optInAppAnalytics || settings?.optInSendExceptions) {
			section("Analytics Data") {
				if(settings?.optInAppAnalytics) {
					href url: getAppEndpointUrl("renderInstallData"), style:"embedded", required: false, title:"View Shared Install Data", description:"Tap to view Data", image: getAppImg("app_analytics_icon.png")
				}
				href url: getAppEndpointUrl("renderInstallId"), style:"embedded", required: false, title:"View Installation ID", description:"Tap to view", image: getAppImg("view_icon.png")
			}
		}
		section("Recent Nest Command Details:") {
			def cmdDesc = ""
			cmdDesc += " • DateTime: "
			cmdDesc += atomicState?.lastCmdSentDt ? "\n └ (${atomicState?.lastCmdSentDt})" : "(Nothing found)"
			cmdDesc += "\n • Cmd Sent: "
			cmdDesc += atomicState?.lastCmdSent ? "\n └ (${atomicState?.lastCmdSent})" : "(Nothing found)"
			cmdDesc += "\n • Cmd Result: (${atomicState?.lastCmdSentStatus ?: "Nothing found"})"

			cmdDesc += "\n\n • Totals Commands Sent: (${!atomicState?.apiCommandCnt ? 0 : atomicState?.apiCommandCnt})"
			paragraph "${cmdDesc}"
		}
		section("Other Data:") {
			paragraph "API Token Client Version: ${atomicState?.metaData?.client_version ?: "Not Found"}"
			paragraph "Install Id:\n${atomicState?.installationId ?: "Not Found"}"
			paragraph "Token Number: ${atomicState?.appData?.token?.tokenNum ?: "Not Found"}"
		}
	}
}

def managAppDataPage() {
	def rVal = (settings?.managAppPageRfsh) ? (settings?.managAppDataRfshVal ? settings?.managAppDataRfshVal.toInteger() : 30) : null
	dynamicPage(name: "managAppDataPage", refreshInterval:rVal, install: false) {
		if(!atomicState?.diagManagAppStateFilters) { atomicState?.diagManagAppStateFilters = ["diagManagAppStateFilters"] }
		section("${app.label}:") {
			if(settings?.managAppPageShowSet == true || settings?.managAppPageShowSet == null) {
				paragraph title: "Settings Data", "${getMapDescStr(getSettings())}"
			}
			if(settings?.managAppPageShowState == true || settings?.managAppPageShowState == null) {
				def data = getState()?.findAll { !(it?.key in atomicState?.diagManagAppStateFilters) }
				paragraph title: "State Data", "${getMapDescStr(data)}"
			}
			if(settings?.managAppPageShowMeta == true || settings?.managAppPageShowMeta == null) {
				paragraph title: "MetaData", "${getMapDescStr(getMetadata())}"
			}
		}
		section("Data Filters:") {
			paragraph "Show the following items in the results:"
			input "managAppPageShowState", "bool", title: "State Data?", defaultValue: false, submitOnChange: true
			if(settings?.managAppPageShowState) {
				input(name: "managAppDataStateFilter", title: "Select Items to Ignore", type: "enum", required: false, multiple: true, submitOnChange: true, metadata: [values:getChildStateKeys()])
				atomicState?.diagManagAppStateFilters = settings?.managAppDataStateFilter ?: []
			}
			input "managAppPageShowSet", "bool", title: "Settings Data?", defaultValue: false, submitOnChange: true
			input "managAppPageShowMeta", "bool", title: "MetaData?", defaultValue: false, submitOnChange: true
		}
		section("Page Options:") {
			input "managAppPageRfsh", "bool", title: "Enable Auto-Refresh?", defaultValue: false, submitOnChange: true
			if(settings?.managAppPageRfsh) {
				input "managAppDataRfshVal", "number", title: "Refresh Every xx seconds?", defaultValue: 30, submitOnChange: true
			}
			paragraph "Changing this may require you to leave the page and come back"
		}
	}
}

def childAppDataPage() {
	def rVal = (settings?.childAppPageRfsh && settings?.childAppDataPageDev) ? (settings?.childAppDataRfshVal ? settings?.childAppDataRfshVal.toInteger() : 30) : null
	dynamicPage(name: "childAppDataPage", refreshInterval:rVal, install:false) {
		if(!atomicState?.diagChildAppStateFilters) { atomicState?.diagChildAppStateFilters = ["diagChildAppStateFilters"] }
		def apps = getAllChildApps()
		section("Child App Selection:") {
			input(name: "childAppDataPageApp", title: "Select Child App(s) to View", type: "enum", required: false, multiple: true, submitOnChange: true, metadata: [values:buildChildAppInputMap()])
			if(!settings?.childAppDataPageApp) { paragraph "Please select a child app to view!", required: true, state: null }
		}
		if(settings?.childAppDataPageApp) {
			apps?.each { cApp ->
				settings?.childAppDataPageApp?.each { selApp ->
					if(selApp == cApp?.getId()) {
						section("${strCapitalize(cApp?.getLabel())}:") {
							if(settings?.childAppPageShowState == true || settings?.childAppPageShowState == null) {
								def data = cApp?.getState()?.findAll { !(it?.key in atomicState?.diagChildAppStateFilters) }
								paragraph title: "State Data", "${getMapDescStr(data)}"
							}
							if(settings?.childAppPageShowSet == true || settings?.childAppPageShowSet == null) {
								paragraph title: "Settings Data", "${getMapDescStr(cApp?.getSettings())}"
							}
							if(settings?.childAppPageShowMeta == true || settings?.childAppPageShowMeta == null) {
								paragraph title: "MetaData", "${getMapDescStr(cApp?.getMetadata())}"
							}
						}
					}
				}
			}
		}
		if(settings?.childAppDataPageApp) {
			section("Data Filters:") {
				paragraph "Show the following items in the device results:"
				input "childAppPageShowState", "bool", title: "State Data?", defaultValue: false, submitOnChange: true
				if(settings?.childAppPageShowState) {
					input(name: "childAppDataStateFilter", title: "Select Items to Ignore", type: "enum", required: false, multiple: true, submitOnChange: true, metadata: [values:getChildStateKeys("childapp")])
					atomicState?.diagChildAppStateFilters = settings?.childAppDataStateFilter ?: []
				}
				input "childAppPageShowSet", "bool", title: "Settings Data?", defaultValue: false, submitOnChange: true
				input "childAppPageShowMeta", "bool", title: "MetaData?", defaultValue: false, submitOnChange: true
			}
		}
		section("Page Options:") {
			input "childAppPageRfsh", "bool", title: "Enable Auto-Refresh?", defaultValue: false, submitOnChange: true
			if(settings?.childAppPageRfsh) {
				input "childAppDataRfshVal", "number", title: "Refresh Every xx seconds?", defaultValue: 30, submitOnChange: true
			}
			paragraph "Changing this may require you to leave the page and come back"
		}
	}
}

def childDevDataPage() {
	def rVal = (settings?.childDevPageRfsh && settings?.childDevDataPageDev) ? (settings?.childDevDataRfshVal ? settings?.childDevDataRfshVal.toInteger() : 180) : null
	dynamicPage(name: "childDevDataPage", refreshInterval:rVal, install: false) {
		if(!atomicState?.diagDevStateFilters) { atomicState?.diagDevStateFilters = ["diagDevStateFilters"] }
		def devices = app.getChildDevices(true)
		section("Device Selection:") {
			input(name: "childDevDataPageDev", title: "Select Device(s) to View", type: "enum", required: false, multiple: true, submitOnChange: true, metadata: [values:buildDevInputMap()])
			if(!settings?.childDevDataPageDev) { paragraph "Please select a device to view!", required: true, state: null }
		}
		if(settings?.childDevDataPageDev) {
			devices?.each { dev ->
				settings?.childDevDataPageDev?.each { selDev ->
					if(selDev == dev?.deviceNetworkId) {
						section("${strCapitalize(dev?.displayName)}:") {
							if(settings?.childDevPageShowState == true || settings?.childDevPageShowState == null) {
								paragraph title: "State Data", "${getMapDescStr(dev?.getState())}"
							}
							if(settings?.childDevPageShowAttr == true || settings?.childDevPageShowAttr == null) {
								def str = ""; def cnt = 1
								def devData = dev?.supportedAttributes.collect { it as String }
								devData?.sort().each {
									str += "${cnt>1 ? "\n\n" : "\n"} • ${"$it" as String}: (${dev.currentValue("$it")})"
									cnt = cnt+1
								}
								paragraph title: "Supported Attributes\n", "${str}"
							}
							if(settings?.childDevPageShowCmds == true || settings?.childDevPageShowCmds == null) {
								def str = ""; def cnt = 1
								dev?.supportedCommands?.sort()?.each { cmd ->
									str += "${cnt>1 ? "\n\n" : "\n"} • ${cmd.name}(${!cmd?.arguments ? "" : cmd?.arguments.toString().toLowerCase().replaceAll("\\[|\\]", "")})"
									cnt = cnt+1
								}
								paragraph title: "Supported Commands", "${str}"
							}
							if(settings?.childDevPageShowCapab == true || settings?.childDevPageShowCapab == null) {
								def data = dev?.capabilities?.sort()?.collect {it as String}
								paragraph title: "Device Capabilities", "${getMapDescStr(data)}"
							}
						}
					}
				}
			}
		}
		if(settings?.childDevDataPageDev) {
			section("Data Filters:") {
				paragraph "Show the following items in the device results:"
				input "childDevPageShowState", "bool", title: "State Data?", defaultValue: true, submitOnChange: true
				if(settings?.childDevPageShowState) {
					input(name: "childDevDataStateFilter", title: "Select Items to Ignore", type: "enum", required: false, multiple: true, submitOnChange: true, metadata: [values:getChildStateKeys("device")])
					atomicState?.diagDevStateFilters = settings?.childDevDataStateFilter ?: []
				}
				input "childDevPageShowAttr", "bool", title: "Attributes?", defaultValue: false, submitOnChange: true
				input "childDevPageShowCmds", "bool", title: "Commands?", defaultValue: false, submitOnChange: true
				input "childDevPageShowCapab", "bool", title: "Capabilities?", defaultValue: false, submitOnChange: true
			}
		}
		section("Page Options:") {
			input "childDevPageRfsh", "bool", title: "Enable Auto-Refresh?", defaultValue: false, submitOnChange: true
			if(settings?.childDevPageRfsh) {
				input "childDevDataRfshVal", "number", title: "Refresh Every xx seconds?", defaultValue: 180, submitOnChange: true
			}
			paragraph "Changing this may require you to leave the page and come back"
		}
	}
}

def appParamsDataPage() {
	dynamicPage(name: "appParamsDataPage", refreshInterval: 0, install: false) {
		section() {
			def data = atomicState?.appData
			paragraph title: "AppData Contents", "${getMapDescStr(data)}"
		}
	}
}

def getMapDescStr(data) {
	def str = ""
	def cnt = 1
	data?.sort()?.each { par ->
		if(par?.value instanceof Map || par?.value instanceof List || par?.value instanceof ArrayList) {
			str += "${cnt>1 ? "\n\n" : ""} • ${par?.key.toString()}:"
			if(par?.value instanceof Map) {
				def map2 = par?.value
				def cnt2 = 1
				map2?.sort()?.each { par2 ->
					if(par2?.value instanceof Map) { //This handles second level maps
						def map3 = par2?.value
						def cnt3 = 1
						str += "\n   ${cnt2 < map2?.size() ? "├" : "└"} ${par2?.key.toString()}:"
						map3?.sort()?.each { par3 ->
							if(par3?.value instanceof Map) { //This handles third level maps
								def map4 = par3?.value
								def cnt4 = 1
								str += "\n   ${cnt2 < map2?.size() ? "│" : "    "}${cnt3 < map3?.size() ? "├" : "└"} ${par3?.key.toString()}:"
								map4?.sort()?.each { par4 ->
									if(par4?.value instanceof Map) { //This handles fourth level maps
										def map5 = par4?.value
										def cnt5 = 1
										str += "\n   ${cnt2 < map2?.size() ? "│" : "    "}${cnt3 < map3?.size() ? "│" : "    "}${cnt4 < map4?.size() ? "├" : "└"} ${par4?.key.toString()}:"
										map5?.sort()?.each { par5 ->
											str += "\n   ${cnt2 < map2?.size()  ? "│" : "    "}${cnt3 < map3?.size() ? "│" : "    "}${cnt4 < map4?.size() ? "│" : "    "}${cnt5 < map5?.size() ? "├" : "└"} ${par5}"
											cnt5 = cnt5+1
										}
									}
									else if(par4?.value instanceof List || par?.value instanceof ArrayList) { //This handles forth level lists
										def list4 = par4?.value?.collect {it}
										def cnt5 = 1
										str += "\n   ${cnt2 < map2?.size() ? "│" : "    "}${cnt3 < map3?.size() ? "│" : "    "}${cnt4 < map4?.size() ? "│" : "└"} ${par4?.key.toString()}:"
										list4?.each { par5 ->
											str += "\n   ${cnt2 < map2?.size()  ? "│" : "    "}${cnt3 < map3?.size() ? "│" : "    "}${cnt4 < map4?.size() ? "│" : "    "}${cnt5 < list4?.size() ? "├" : "└"} ${par5}"
											cnt5 = cnt5+1
										}
									} else {
										str += "\n   ${cnt2 < map2?.size()  ? "│" : "    "}${cnt3 < map3?.size()  ? "│" : "    "}${cnt4 < map4?.size() ? "├" : "└"} ${par4?.key.toString()}: (${par4?.value})"
									}
									cnt4 = cnt4+1
								}
							}
							else if(par3?.value instanceof List || par?.value instanceof ArrayList) { //This handles third level lists
								def list3 = par3?.value?.collect {it}
								def cnt4 = 1
								str += "\n   ${cnt2 < map2?.size() ? "│" : "    "}${cnt3 < map3?.size() ? "├" : "└"} ${par3?.key.toString()}:"
								list3?.each { par4 ->
									str += "\n   ${cnt2 < map2?.size()  ? "│" : "    "}${cnt3 < map3?.size() ? "│" : "    "}${cnt4 < list3?.size() ? "├" : "└"} ${par4}"
									cnt4 = cnt4+1
								}
							} else {
								str += "\n   ${cnt2 < map2?.size()  ? "│" : "    "}${cnt3 < map3?.size() ? "├" : "└"} ${par3?.key.toString()}: (${par3?.value})"
							}
							cnt3 = cnt3+1
						}
						cnt2 = cnt2+1
					} else {
						str += "\n   ${cnt2 < map2?.size() ? "├" : "└"} ${par2?.key.toString()}: (${par2?.value})"
						cnt2 = cnt2+1
					}
				}
			}
			if(par?.value instanceof List || par?.value instanceof ArrayList) {
				def list2 = par?.value?.collect {it}
				def cnt2 = 1
				list2?.each { par2 ->
					str += "\n   ${cnt2 < list2?.size() ? "├" : "└"} ${par2}"
					cnt2 = cnt2+1
				}
			}
		} //else {
		// 	str += "${cnt>1 ? "\n\n" : "\n"} • ${par?.key.toString()}: (${par?.value})"
		// }
		cnt = cnt+1
	}
	//log.debug "str: $str"
	return str != "" ? str : "No Data was returned"
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
		appMap[[it?.getId()].join('.')] = it?.getLabel()
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

def createManagerBackupDataJson() {
	def noShow = ["authToken", "accessToken", "curAlerts", "curAstronomy", "curForecast", "curWeather"]
	def sData = getSettings()?.sort()?.findAll { !(it.key in noShow) }
	def setData = [:]
	sData?.sort().each { item ->
		setData[item?.key] = item?.value
	}
	def stData = getState()?.sort()?.findAll { !(it.key in noShow) }
	def stateData = [:]
	stData?.sort().each { item ->
		stateData[item?.key] = item?.value
	}
	def result = ["settingsData":setData, "stateData":stateData, "backupDt":getDtNow().toString()]
	def resultJson = new groovy.json.JsonOutput().toJson(result)
	return resultJson
}

def getDeviceMetricCnts() {
	def data = [:]
	def devs = app.getChildDevices(true)
	if(devs?.size() >= 1) {
		devs?.each { dev ->
			def mData = dev?.getMetricCntData()
			if(mData != null) {
				//log.debug "mData: ${mData}"
				mData?.each { md ->
					def objKey = md?.key.toString()
					def objVal = md?.value.toInteger()
					if(data?.containsKey("${objKey}")) {
						def newVal = 0
						def prevVal = data?.get("${objKey}")
						newVal = prevVal?.toInteger()+objVal
						//log.debug "$objKey Data: [prevVal: $prevVal | objVal: $objVal | newVal: $newVal]"
						data << ["${objKey}":newVal]
					} else {
						data << ["${objKey}":objVal]
					}
				}
			}
		}
	}
	//log.debug "data: ${data}"
	return data
}
/******************************************************************************
*					Firebase Analytics Functions		  	  *
*******************************************************************************/
def createInstallDataJson() {
	try {
		generateInstallId()
		def autoDesc = getInstAutoTypesDesc()			// This is a hack to get installedAutomations data updated without waiting for user to hit done
		def tsVer = !atomicState?.tDevVer ? "Not Installed" : atomicState?.tDevVer
		def ptVer = !atomicState?.pDevVer ? "Not Installed" : atomicState?.pDevVer
		def cdVer = !atomicState?.camDevVer ? "Not Installed" : atomicState?.camDevVer
		def pdVer = !atomicState?.presDevVer ? "Not Installed" : atomicState?.presDevVer
		def wdVer = !atomicState?.weatDevVer ? "Not Installed" : atomicState?.weatDevVer
		def vtsVer = !atomicState?.vtDevVer ? "Not Installed" : atomicState?.vtDevVer
		def autoVer = !atomicState?.autoSaVer ? "Not Installed" : atomicState?.autoSaVer
		def restVer = !atomicState?.restServiceData?.version ? "Not Installed" : atomicState?.restServiceData?.version

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
				"guid":atomicState?.installationId, "versions":versions, "thermostats":tstatCnt, "protects":protCnt, "vthermostats":vstatCnt, "cameras":camCnt, "appErrorCnt":appErrCnt, "devErrorCnt":devErrCnt,
				"installDt": atomicState?.installData?.dt, "updatedDt": atomicState?.installData?.updatedDt, "automations":automations, "timeZone":tz, "apiCmdCnt":apiCmdCnt, "apiStrReqCnt":apiStrReqCnt,
				"apiDevReqCnt":apiDevReqCnt, "apiMetaReqCnt":apiMetaReqCnt, "appNotifSentCnt":appNotifSentCnt, "apiRestStrEvtCnt":apiRestStrEvtCnt, "appUseMetCnt":appUseMetCnt, "devUseMetCnt":devUseMetCnt,
				"stateUsage":"${getStateSizePerc()}%", "mobileClient":cltType, "datetime":getDtNow()?.toString(), "optOut":false
			]
		} else {
			data = [
				"guid":atomicState?.installationId, "versions":versions, "thermostats":tstatCnt, "protects":protCnt, "vthermostats":vstatCnt, "cameras":camCnt, "appErrorCnt":appErrCnt, "devErrorCnt":devErrCnt,
				"apiStrReqCnt":apiStrReqCnt, "apiDevReqCnt":apiDevReqCnt, "apiMetaReqCnt":apiMetaReqCnt, "installDt": atomicState?.installData?.dt,  "updatedDt": atomicState?.installData?.updatedDt,
				"automations":automations, "timeZone":tz, "apiCmdCnt":apiCmdCnt, "apiRestStrEvtCnt":apiRestStrEvtCnt, "stateUsage":"${getStateSizePerc()}%", "datetime":getDtNow()?.toString(), "optOut":true
			]
		}
		def resultJson = new groovy.json.JsonOutput().toJson(data)
		return resultJson

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

def renderInstallId() {
	try {
		def resultJson = new groovy.json.JsonOutput().toJson(atomicState?.installationId)
		render contentType: "application/json", data: resultJson
	} catch (ex) { log.error "renderInstallId Exception:", ex }
}

def sendInstallData() {
	sendFirebaseData(createInstallDataJson(), "installData/clients/${atomicState?.installationId}.json", null, "heartbeat")
}

def removeInstallData() {
	return removeFirebaseData("installData/clients/${atomicState?.installationId}.json")
}

def removeRemDiagData(childId) {
	LogAction("Removing Remote Diags", "info", true)
	removeFirebaseData("${getDbRemDiagPath()}/clients/${atomicState?.remDiagClientId}.json")
	return removeFirebaseData("${getDbRemDiagPath()}/clients/${atomicState?.remDiagClientId}/setandstate.json")
}

def sendInstallSlackNotif() {
	def cltType = !settings?.mobileClientType ? "Not Configured" : settings?.mobileClientType?.toString()
	def str = ""
	str += "New Client Installed:"
	str += "\n • DateTime: ${getDtNow()}"
	str += "\n • App Version: v${appVersion()}"
	str += "\n • TimeZone: ${getTimeZone()?.ID?.toString()}"
	str += "\n • Mobile Client: ${cltType}"
	def res = [:]
	res << ["username":"New User Notification"]
	res << ["icon_emoji":":spock-hand:"]
	res << ["channel": "#new_clients"]
	res << ["text":str]
	def json = new groovy.json.JsonOutput().toJson(res)
	sendDataToSlack(json, "", "post", "New Client Slack Notif")
}

def getDbExceptPath() { return atomicState?.appData?.database?.newexceptionPath ?: "newexceptionData" }
def getDbRemDiagPath() { return atomicState?.appData?.database?.remoteDiagPath ?: "remoteDiagLogs" }

def sendExceptionData(ex, methodName, isChild = false, autoType = null) {
	def labelstr = ""
	if(atomicState?.debugAppendAppName) { labelstr = "${app.label} | " }
	log.debug "${labelstr}sendExceptionData(method: $methodName, isChild: $isChild, autoType: $autoType)"
	if(atomicState?.appData?.database?.disableExceptions == true) {
		return
	} else {
		def exCnt = 0
		def exString
		if(ex instanceof java.lang.NullPointerException || ex instanceof java.lang.SecurityException) {
			//LogAction("sendExceptionData: NullPointerException was caught successfully", "info", true)
			return
		} else {
			//log.debug "ex: $ex"
			exString = ex?.message?.toString()
			//log.debug "sendExceptionData: Exception Message (${exString})"
		}
		exCnt = atomicState?.appExceptionCnt ? atomicState?.appExceptionCnt + 1 : 1
		atomicState?.appExceptionCnt = exCnt ?: 1
		if(settings?.optInSendExceptions || settings?.optInSendExceptions == null) {
			generateInstallId()
			def appType = isChild && autoType ? "automationApp/${autoType}" : "managerApp"
			def exData
			if(isChild) {
				exData = ["methodName":methodName, "automationType":autoType, "appVersion":(appVersion() ?: "Not Available"),"errorMsg":exString, "errorDt":getDtNow().toString()]
			} else {
				exData = ["methodName":methodName, "appVersion":(appVersion() ?: "Not Available"),"errorMsg":exString, "errorDt":getDtNow().toString()]
			}
			def results = new groovy.json.JsonOutput().toJson(exData)
			sendFirebaseData(results, "${getDbExceptPath()}/${appType}/${methodName}/${atomicState?.installationId}.json", "post", "Exception")
		}
	}
}

def sendChildExceptionData(devType, devVer, ex, methodName) {
	def exCnt = 0
	def exString
	if(ex instanceof java.lang.NullPointerException) {// || ex instanceof java.lang.SecurityException) {
		return
	} else {
		exString = ex.message.toString()
	}
	exCnt = atomicState?.childExceptionCnt ? atomicState?.childExceptionCnt + 1 : 1
	atomicState?.childExceptionCnt = exCnt ?: 1
	if(settings?.optInSendExceptions || settings?.optInSendExceptions == null) {
		generateInstallId()
		def exData = ["deviceType":devType, "devVersion":(devVer ?: "Not Available"), "methodName":methodName, "errorMsg":exString, "errorDt":getDtNow().toString()]
		def results = new groovy.json.JsonOutput().toJson(exData)
		sendFirebaseData(results, "${getDbExceptPath()}/${devType}/${methodName}/${atomicState?.installationId}.json", "post", "Exception")
	}
}

def sendFeedbackData(msg) {
	def cltId = atomicState?.installationId
	def exData = ["guid":atomicState?.installationId, "version":appVersion(), "feedbackMsg":(msg ? msg : (settings?.feedbackMsg ?: "No Text")), "msgDt":getDtNow().toString()]
	def results = new groovy.json.JsonOutput().toJson(exData)
	if(sendFirebaseData(results, "feedback/data.json", "post", "Feedback")) {
		atomicState?.feedbackPending = false
		if(!msg) { atomicState?.lastFeedbackData = ["lastMsg":settings?.feedbackMsg, "lastMsgDt":getDtNow().toString(), "lastAppVer":appVersion()] }
	}
}

def sendFirebaseData(data, pathVal, cmdType=null, type=null, noAsync=false) {
	LogTrace("sendFirebaseData(${data}, ${pathVal}, $cmdType, $type")

	def allowAsync = false
	def metstr = "sync"
	if(atomicState?.appData && atomicState?.appData?.pollMethod?.allowAsync) {
		allowAsync = true
		metstr = "async"
	}
	if(allowAsync && !noAsync) {
		return queueFirebaseData(data, pathVal, cmdType, type)
	} else {
		return syncSendFirebaseData(data, pathVal, cmdType, type)
	}
}

def queueFirebaseData(data, pathVal, cmdType=null, type=null) {
	LogTrace("queueFirebaseData(${data}, ${pathVal}, $cmdType, $type")
	def result = false
	def json = new groovy.json.JsonOutput().prettyPrint(data)
	def params = [ uri: "${getFirebaseAppUrl()}/${pathVal}", body: json.toString() ]
	def typeDesc = type ? "${type}" : "Data"
	try {
		atomicState.qFirebaseRequested = true
		if(!cmdType || cmdType == "put") {
			asynchttp_v1.put(processFirebaseResponse, params, [ type: "${typeDesc}"])
			result = true
		} else if (cmdType == "post") {
			asynchttp_v1.post(processFirebaseResponse, params, [ type: "${typeDesc}"])
			result = true
		} else { LogAction("queueFirebaseData UNKNOWN cmdType: ${cmdType}", warn, true) }

	} catch(ex) {
		log.error "queueFirebaseData (type: $typeDesc) Exception:", ex
		sendExceptionData(ex, "queueFirebaseData")
	}
	return result
}

def processFirebaseResponse(resp, data) {
	LogTrace("processFirebaseResponse(${data?.type})")
	def result = false
	def typeDesc = data?.type
	//log.debug "type: ${typeDesc}"
	try {
		if(resp?.status == 200) {
			LogAction("sendFirebaseData: ${typeDesc} Data Sent SUCCESSFULLY", "info", false)
			if(typeDesc?.toString() == "Remote Diag Logs") {

			} else {
				if(typeDesc?.toString() == "heartbeat") { atomicState?.lastAnalyticUpdDt = getDtNow() }
			}
			result = true
		}
		else if(resp?.status == 400) {
			LogAction("sendFirebaseData: 'Bad Request': ${resp?.status}", "error", true)
		}
		else {
			LogAction("sendFirebaseData: 'Unexpected' Response: ${resp?.status}", "warn", true)
		}
		if(resp.hasError()) {
			LogAction("errorData: $resp.errorData  errorMessage: $resp.errorMessage", "error", true)
		}
	} catch(ex) {
		log.error "processFirebaseResponse (type: $typeDesc) Exception:", ex
		sendExceptionData(ex, "processFirebaseResponse")
	}
	atomicState.qFirebaseRequested = false
}

def syncSendFirebaseData(data, pathVal, cmdType=null, type=null) {
	LogTrace("syncSendFirebaseData(${data}, ${pathVal}, $cmdType, $type")
	def result = false
	def json = new groovy.json.JsonOutput().prettyPrint(data)
	def params = [ uri: "${getFirebaseAppUrl()}/${pathVal}", body: json.toString() ]
	def typeDesc = type ? "${type}" : "Data"
	def respData
	try {
		if(!cmdType || cmdType == "put") {
			httpPutJson(params) { resp ->
				respData = resp
			}
		} else if (cmdType == "post") {
			httpPostJson(params) { resp ->
				respData = resp
			}
		}
		if(respData) {
			//log.debug "respData: ${respData}"
			if(respData?.status == 200) {
				LogAction("sendFirebaseData: ${typeDesc} Data Sent SUCCESSFULLY", "info", false)
				if(typeDesc.toString() == "Remote Diag Logs") {

				} else {
					if(typeDesc?.toString() == "heartbeat") { atomicState?.lastAnalyticUpdDt = getDtNow() }
				}
				result = true
			}
			else if(respData?.status == 400) {
				LogAction("sendFirebaseData: 'Bad Request': ${respData?.status}", "error", true)
			}
			else {
				LogAction("sendFirebaseData: 'Unexpected' Response: ${respData?.status}", "warn", true)
			}
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.HttpResponseException) {
			LogAction("sendFirebaseData: 'HttpResponseException': ${ex.message}", "error", true)
		}
		else { log.error "sendFirebaseData: ([$data, $pathVal, $cmdType, $type]) Exception:", ex }
		sendExceptionData(ex, "sendFirebaseData")
	}
	return result
}

def sendDataToSlack(data, pathVal, cmdType=null, type=null) {
	LogAction("sendDataToSlack(${data}, ${pathVal}, $cmdType, $type", "trace", false)
	def result = false
	def json = new groovy.json.JsonOutput().prettyPrint(data)
	def params = [ uri: "${slackMsgWebHookUrl()}", body: json.toString() ]
	def typeDesc = type ? "${type}" : "Data"
	def respData
	try {
		if(!cmdType || cmdType == "post") {
			httpPostJson(params) { resp ->
				respData = resp
			}
		}
		if(respData) {
			//log.debug "respData: ${respData}"
			if(respData?.status == 200) {
				LogAction("sendDataToSlack: ${typeDesc} Data Sent SUCCESSFULLY", "info", false)
				result = true
			}
			else if(respData?.status == 400) {
				LogAction("sendDataToSlack: 'Bad Request': ${respData?.status}", "error", true)
			}
			else {
				LogAction("sendDataToSlack: 'Unexpected' Response: ${respData?.status}", "warn", true)
			}
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.HttpResponseException) {
			LogAction("sendDataToSlack: 'HttpResponseException': ${ex.message}", "error", true)
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
		httpDelete(uri: "${getFirebaseAppUrl()}/${pathVal}") { resp ->
			LogAction("resp: ${resp?.status}", "info", true)
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.ResponseParseException) {
			LogAction("removeFirebaseData: Response: ${ex.message}", "info", true)
		} else {
			LogAction("removeFirebaseData: Exception: ${ex.message}", "error", true)
			sendExceptionData(ex, "removeFirebaseData")
			result = false
		}
	}
	return result
}

/////////////////////////////////////////////////////////////////////////////////////////////
/********************************************************************************************
|    						Application Name: Nest Automations								|
|    						Author: Anthony S. (@tonesto7) | Eric S. (@E_Sch)			    |
|********************************************************************************************/
/////////////////////////////////////////////////////////////////////////////////////////////

// Calls by Automation children
// parent only method
def automationNestModeEnabled(val=null) {
	LogAction("automationNestModeEnabled: val: $val", "info", false)
	if(val == null) {
		return atomicState?.automationNestModeEnabled ?: false
	} else {
		atomicState.automationNestModeEnabled = val.toBoolean()
	}
	return atomicState?.automationNestModeEnabled ?: false
}

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
	//def migrate = parent?.migrationInProgress()
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

// /********************************************************************************
// |		SCHEDULE, MODE, or MOTION CHANGES ADJUST THERMOSTAT SETPOINTS			|
// |		(AND THERMOSTAT MODE) AUTOMATION CODE									|
// *********************************************************************************/
//
def getTstatAutoDevId() {
	if(settings?.schMotTstat) { return settings?.schMotTstat.deviceNetworkId.toString() }
	return null
}

def isSchMotConfigured() {
	return settings?.schMotTstat ? true : false
}

//These are here to catch any events that occur before the migration occurs
def heartbeatAutomation() { return }
def runAutomationEval() { return }
def automationGenericEvt(evt) { return }
def automationSafetyTempEvt(evt) { return }
def nModeGenericEvt(evt) { return }
def leakWatSensorEvt(evt) { return }
def conWatContactEvt(evt) { return }
def extTmpGenericEvt(evt) { return }

def getAutomationType() {
	return atomicState?.automationType ?: null
}

def getIsAutomationDisabled() {
	def dis = atomicState?.disableAutomation
	return (dis != null && dis == true) ? true : false
}

def fixTempSetting(Double temp) {
	def newtemp = temp
	if(temp != null) {
		if(getTemperatureScale() == "C") {
			if(temp > 35) {    // setting was done in F
				newtemp = roundTemp( (newtemp - 32.0) * (5/9) as Double)
			}
		} else if(getTemperatureScale() == "F") {
			if(temp < 40) {    // setting was done in C
				newtemp = roundTemp( ((newtemp * (9/5) as Double) + 32.0) ).toInteger()
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

private getDayOfWeekName(date = null) {
	if (!date) {
		date = adjustTime(now())
	}
	switch (date.day) {
		case 0: return "Sunday"
		case 1: return "Monday"
		case 2: return "Tuesday"
		case 3: return "Wednesday"
		case 4: return "Thursday"
		case 5: return "Friday"
		case 6: return "Saturday"
	}
	return null
}

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

def getActiveScheduleState() {
	return atomicState?.activeSchedData ?: null
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
		}
	}
	return newtemp
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
|					     GLOBAL Code | Logging AND Diagnostic							    |
*************************************************************************************************/

def sendEventPushNotifications(message, type, pName) {
	//LogTrace("sendEventPushNotifications($message, $type, $pName)")
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
	def ok2Notify = getOk2Notify()

	LogAction("sendEventVoiceNotifications($vMsg, $pName)   ok2Notify: $ok2Notify", "trace", false)
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

/*
// obsolete
def getTstatCapabilities(tstat, autoType, dyn = false) {
	def canCool = true
	def canHeat = true
	def hasFan = true
	if(tstat?.currentCanCool) { canCool = tstat?.currentCanCool.toBoolean() }
	if(tstat?.currentCanHeat) { canHeat = tstat?.currentCanHeat.toBoolean() }
	if(tstat?.currentHasFan) { hasFan = tstat?.currentHasFan.toBoolean() }

	atomicState?."${autoType}${dyn ? "_${tstat?.deviceNetworkId}_" : ""}TstatCanCool" = canCool
	atomicState?."${autoType}${dyn ? "_${tstat?.deviceNetworkId}_" : ""}TstatCanHeat" = canHeat
	atomicState?."${autoType}${dyn ? "_${tstat?.deviceNetworkId}_" : ""}TstatHasFan" = hasFan
}
*/

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

/*
// obsolete
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

// obsolete
def getGlobalDesiredHeatTemp() {
	def t0 = parent?.settings?.locDesiredHeatTemp?.toDouble()
	return t0 ?: null
}

// obsolete
def getGlobalDesiredCoolTemp() {
	def t0 = parent?.settings?.locDesiredCoolTemp?.toDouble()
	return t0 ?: null
}

// obsolete
def getClosedContacts(contacts) {
	if(contacts) {
		def cnts = contacts?.findAll { it?.currentContact == "closed" }
		return cnts ?: null
	}
	return null
}

// obsolete
def getOpenContacts(contacts) {
	if(contacts) {
		def cnts = contacts?.findAll { it?.currentContact == "open" }
		return cnts ?: null
	}
	return null
}

// obsolete
def getDryWaterSensors(sensors) {
	if(sensors) {
		def cnts = sensors?.findAll { it?.currentWater == "dry" }
		return cnts ?: null
	}
	return null
}

// obsolete
def getWetWaterSensors(sensors) {
	if(sensors) {
		def cnts = sensors?.findAll { it?.currentWater == "wet" }
		return cnts ?: null
	}
	return null
}

// obsolete
def isContactOpen(con) {
	def res = false
	if(con) {
		if(con?.currentSwitch == "on") { res = true }
	}
	return res
}

// obsolete
def isSwitchOn(dev) {
	def res = false
	if(dev) {
		dev?.each { d ->
			if(d?.currentSwitch == "on") { res = true }
		}
	}
	return res
}

// obsolete
def isPresenceHome(presSensor) {
	def res = false
	if(presSensor) {
		presSensor?.each { d ->
			if(d?.currentPresence == "present") { res = true }
		}
	}
	return res
}

// obsolete
def isSomebodyHome(sensors) {
	if(sensors) {
		def cnts = sensors?.findAll { it?.currentPresence == "present" }
		return cnts ? true : false
	}
	return false
}

// obsolete
def getTstatPresence(tstat) {
	def pres = "not present"
	if(tstat) { pres = tstat?.currentPresence }
	return pres
}
*/

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

// obsolete
def longTimeSecEnum() {
	def vals = [
		0:"Off", 60:"1 Minute", 120:"2 Minutes", 180:"3 Minutes", 240:"4 Minutes", 300:"5 Minutes", 600:"10 Minutes", 900:"15 Minutes", 1200:"20 Minutes", 1500:"25 Minutes",
		1800:"30 Minutes", 2700:"45 Minutes", 3600:"1 Hour", 7200:"2 Hours", 14400:"4 Hours", 21600:"6 Hours", 43200:"12 Hours", 86400:"24 Hours", 10:"10 Seconds(Testing)"
	]
	return vals
}

// obsolete
def shortTimeEnum() {
	def vals = [
		1:"1 Second", 2:"2 Seconds", 3:"3 Seconds", 4:"4 Seconds", 5:"5 Seconds", 6:"6 Seconds", 7:"7 Seconds",
		8:"8 Seconds", 9:"9 Seconds", 10:"10 Seconds", 15:"15 Seconds", 30:"30 Seconds", 60:"60 Seconds"
	]
	return vals
}

def smallTempEnum() {
	def tempUnit = getTemperatureScale()
	def vals = [
		1:"1°${tempUnit}", 2:"2°${tempUnit}", 3:"3°${tempUnit}", 4:"4°${tempUnit}", 5:"5°${tempUnit}", 6:"6°${tempUnit}", 7:"7°${tempUnit}",
		8:"8°${tempUnit}", 9:"9°${tempUnit}", 10:"10°${tempUnit}"
	]
	return vals
}

// obsolete
def switchRunEnum() {
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
	return vals
}

// obsolete
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

// obsolete
def tModeHvacEnum(canHeat, canCool) {
	def vals = ["auto":"Auto", "cool":"Cool", "heat":"Heat", "eco":"Eco"]
	if(!canHeat) {
		vals = ["cool":"Cool", "eco":"Eco"]
	}
	if(!canCool) {
		vals = ["heat":"Heat", "eco":"Eco"]
	}
	return vals
}

// obsolete
def alarmActionsEnum() {
	def vals = ["siren":"Siren", "strobe":"Strobe", "both":"Both (Siren/Strobe)"]
	return vals
}

// obsolete
def getEnumValue(enumName, inputName) {
	def result = "unknown"
	if(enumName) {
		enumName?.each { item ->
			if(item?.key.toString() == inputName?.toString()) {
				result = item?.value
			}
		}
	}
	return result
}

// obsolete
def getSunTimeState() {
	def tz = TimeZone.getTimeZone(location.timeZone.ID)
	def sunsetTm = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", location?.currentValue('sunsetTime')).format('h:mm a', tz)
	def sunriseTm = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", location?.currentValue('sunriseTime')).format('h:mm a', tz)
	atomicState.sunsetTm = sunsetTm
	atomicState.sunriseTm = sunriseTm
}

// obsolete
def parseDt(format, dt) {
	def result
	def newDt = Date.parse("$format", dt)
	result = formatDt(newDt)
	//log.debug "result: $result"
	return result
}
*/

def askAlexaImgUrl() { return "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/AskAlexa512.png" }

///////////////////////////////////////////////////////////////////////////////
/******************************************************************************
|				Application Help and License Info Variables		  			  |
*******************************************************************************/
///////////////////////////////////////////////////////////////////////////////
def appName()		{ return "${parent ? "${autoAppName()}" : "${appLabel()}"}${appDevName()}" }
def appLabel()		{ return inReview() ? "NST Manager" : "Nest Manager" }
def appAuthor()		{ return "Anthony S." }
def appNamespace()	{ return "tonesto7" }
def useNewAutoFile()	{ return true }
def blockOldAuto()	{ return true }
def newAutoName()	{ return "NST Automations" }
def autoAppName()	{ return "NST Automations" }
def gitRepo()		{ return "tonesto7/nest-manager"}
def gitBranch()		{ return "master" }
def gitPath()		{ return "${gitRepo()}/${gitBranch()}"}
def developerVer()	{ return false }
def betaMarker()	{ return false }
def appDevType()	{ return false }
def inReview()		{ return false }
def keepBackups()	{ return false }
def allowMigration()	{ return true }
def appDevName()	{ return appDevType() ? " (Dev)" : "" }
def appInfoDesc()	{
	def cur = atomicState?.appData?.updater?.versions?.app?.ver.toString()
	def beta = betaMarker() ? "" : ""
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
def textDonateLink()	{ return "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2CJEVN439EAWS" }
def stIdeLink()		{ return "https://graph.api.smartthings.com" }
def textCopyright()	{ return "Copyright© 2017 - Anthony S." }
def textDesc()		{ return "This SmartApp is used to integrate your Nest devices with SmartThings and to enable built-in automations" }