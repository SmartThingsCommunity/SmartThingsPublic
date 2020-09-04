/**
 *  Simple Device Viewer v 2.5.2
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Contributors:
 *    Tim Larson (codethug)
 *
 *  URL to documentation:
 *    https://community.smartthings.com/t/release-simple-device-viewer/42481?u=krlaframboise
 *
 *  Changelog:
 *
 *    2.5.2 (07/11/2017)
 *      - Added Threshold and Notification Settings for Power Meter devices (added by codethug)
 *      - Reduced timeouts by changing main screen to display capability page links regardless of whether or not there are devices with that capability.
 *      - Added timeout checking to ensure user is always able to open the application.
 *      - Added ranges for thresholds which will hopefull cause the negative sign to be displayed on iOS.
 *      - Fixed issue with not being able to set the thresholds to 0.
 *      - Removed default values from threshold settings and made application skip over thresholds that are empty.
 *
 *    2.5.1 (02/21/2017)
 *      - Optionally display device's online/offline status in the dashboard.
 *      - Optionally override last even threshold and send notifications for offline devices.
 *      - Fixed timeout problem with Display Settings screen.
 *
 *    2.4.1 (01/21/2017)
 *      - Switched to SmartThings last activity field and only use the device's event history and state history if the device activity is null.
 *      - Added Acceleration Sensor Capability
 *      - Added Dashboard Layout options for condensed and multiple columns.
 *      - Added setting that allows you to choose which one is used.
 *
 *    2.3.2 (01/09/2017)
 *      - Fixed attribute for valve capability.
 *
 *    2.3.1 (01/07/2017)
 *      - Fixed problem with All Devices - States screen.
 *
 *    2.3.0 (01/06/2017)
 *      - Added support for Energy Meter, Illuminance Measurement, Power Meter, Relative Humidity Measurement, Valve
 *      - Added abort to "All Device - States" to prevent timeout errors.
 *
 *    2.2.3 (09/21/2016)
 *      - Still having occassional timeouts so made it abort sooner,
 *        but run more often when it's not successful.
 *
 *    2.2.1 (09/19/2016)
 *      - Made the program detect potential timeout errors and
 *        abort before it times out and then pickup where it left
 *        off the next time it runs.
 *
 *    2.1 (09/13/2016)
 *      - Added Ask Alexa Notification Option.
 *      - Reversed order of Events screen.
 *      - Changed Sort By Value default settings to true.
 *      - Added 3 more icons for battery levels.
 *
 *    2.0.1 (09/02/2016)
 *      - Bug fix due to platform issue.
 *
 *    2.0 (08/09/2016)
 *      - Added Dashboard
 *      - Added exclude device option for all capabilities.
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of
 *  the License at:
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the License is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 *  OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
definition(
    name: "Simple Device Viewer",
    namespace: "krlaframboise",
    author: "Kevin LaFramboise",
    description: "Provides information about the state of the specified devices.",
    category: "My Apps",
		iconUrl: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-device-viewer/simple-device-viewer-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-device-viewer/simple-device-viewer-icon-2x.png",
    iconX3Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-device-viewer/simple-device-viewer-icon-3x.png")

 preferences {
	page(name:"mainPage")
  page(name:"capabilityPage")
	page(name:"lastEventPage")
	page(name:"refreshLastEventPage")
	page(name:"toggleSwitchPage")
	page(name:"devicesPage")
	page(name:"displaySettingsPage")
	page(name:"thresholdsPage")
	page(name:"notificationsPage")
	page(name:"pollingPage")
	page(name:"otherSettingsPage")
	page(name:"dashboardSettingsPage")
	page(name:"enableDashboardPage")
	page(name:"disableDashboardPage")
}

// Main Menu Page
def mainPage() {	
	dynamicPage(name:"mainPage", uninstall:true, install:true) {			
	
		if (getAllDevices().size() != 0) {				
			section() {
				getDashboardHref()
			}
		}
				
		section() {				
			if (getAllDevices().size() != 0) {				
				state.lastCapabilitySetting = null
				getPageLink("lastEventLink",
					"All Devices - Last Event",
					"lastEventPage")
				getCapabilityPageLink(null)			
			}		
			getSelectedCapabilitiesPageLinks()			
		}
		section("Settings") {			
			getPageLink("devicesLink",
				"Choose Devices",
				"devicesPage")
			getPageLink("displaySettingsLink",
				"Display Settings",
				"displaySettingsPage")
			getPageLink("thresholdsLink",
				"Threshold Settings",
				"thresholdsPage")
			getPageLink("notificationsLink",
				"Notification Settings",
				"notificationsPage")
			getPageLink("pollingLink",
				"Polling Settings",
				"pollingPage")
			getPageLink("otherSettingsLink",
				"Other Settings",
				"otherSettingsPage")
			getPageLink("dashboardSettingsPageLink",
					"Dashboard Settings",
					"dashboardSettingsPage")
		}
	}
}

private getSelectedCapabilitiesPageLinks() {
	def startTime = new Date().time
	def aborted = false
	def timeout = 5000
	def selectedCaps = getSelectedCapabilitySettings(timeout)
	
	if (new Date().time - startTime > timeout) {
		aborted = true
	}
	selectedCaps.each {		
		getCapabilityPageLink(it)
	}
	if (aborted) {
		paragraph "Unable to load items within the allowed time.  Check the Choose Devices screen to make sure each device is only selected once.  If that doesn't eliminate this message, try reducing the number of Capabilities selected in the Display Settings screen."
	}
}

private getDashboardHref() {
	if (!state.endpoint) {
		href "enableDashboardPage", title: "Enable Dashboard", description: ""
	} 
	else {
		href "", title: "View Dashboard", style: "external", url: api_dashboardUrl()
	}
}

// Page for choosing devices and which capabilities to use.
def devicesPage() {
	dynamicPage(name:"devicesPage") {		
		section ("Choose Devices") {
			paragraph "Select all the devices that you want to be able to view in this application.\n\nYou can use any of the fields below to select a device, but you only need to select each device once.  Duplicates are automatically removed so selecting a device more than once won't hurt anything."
			input "actuators", "capability.actuator",
				title: "Which Actuators?",
				multiple: true,
				hideWhenEmpty: true,
				required: false
			input "sensors", "capability.sensor",
				title: "Which Sensors?",
				multiple: true,
				hideWhenEmpty: true,
				required: false			
			capabilitySettings().each {				
				input "${getPrefName(it)}Devices",
					"capability.${getPrefType(it)}",
					title: "Which ${getPluralName(it)}?",
					multiple: true,
					hideWhenEmpty: true,
					required: false
			}			
		}		
	}
}

def displaySettingsPage() {
	dynamicPage(name:"displaySettingsPage") {
		section ("Display Options") {
			paragraph "All the capabilities supported by the selected devices are shown on the main screen by default, but this field allows you to limit the list to specific capabilities." 
			input "enabledCapabilities", "enum",
				title: "Display Which Capabilities?",
				multiple: true,
				options: getCapabilitySettingNames(false),
				required: false,
				submitOnChange: true
		}		
		section ("Device Capability Exclusions") {
			paragraph "The capability pages display all the devices that support the capability by default, but these fields allow you to exclude devices from each page."
			
			input "enabledExclusions", "enum",
				title: "Enable Device Exclusions for Which Capabilities?",
				multiple: true,
				options: getCapabilitySettingNames(true),
				required: false,
				submitOnChange: true
			
			if (settings?.enabledExclusions?.find { it == "Events" }) {
				input "lastEventExcludedDevices",
					"enum",
					title: "Exclude these devices from the Last Events page:",
					multiple: true,
					required: false,
					options:getExcludedDeviceOptions(null)
			}
			capabilitySettings().each { cap ->
				if (settings?.enabledExclusions?.find { it == getPluralName(cap) }) {
					input "${getPrefName(cap)}ExcludedDevices",
						"enum",
						title: "Exclude these devices from the ${getPluralName(cap)} page:",
						multiple: true,
						required: false,
						options: getDisplayExcludedDeviceOptions(cap)
				}
			}	
		}
	}
}

private getDisplayExcludedDeviceOptions(cap) {
	def devices = []	
	getDevicesByCapability(getCapabilityName(cap)).each { 
		if (deviceMatchesSharedCapability(it, cap)) {
			devices << it.displayName
		}
	}	
	return devices?.sort()
}

// Page for defining thresholds used for icons and notifications
def thresholdsPage() {
	dynamicPage(name:"thresholdsPage") {		
		section () {
			paragraph "The thresholds specified on this page are used to determine icons in the SmartApp and when to send notifications."			
		}
		section("Battery Thresholds") {
			input "lowBatteryThreshold", "number",
				title: "Enter Low Battery %:",
				multiple: false,
				range:"0..100"
		}
		section("Temperature Thresholds") {
			input "lowTempThreshold", "number",
				title: "Enter Low Temperature:",
				required: false,
				range:"-200..200"
			input "highTempThreshold", "number",
				title: "Enter High Temperature:",
				required: false,
				range:"-200..200"
		}
		section("Power Thresholds") {
			input "lowPowerThreshold", "number",
				title: "Enter Low Power in Watts:",
				required: false,
				range:"0..100000"
			input "highPowerThreshold", "number",
				title: "Enter High Power in Watts:",
				required: false,
				range:"0..100000"
		}
		section("Last Event Thresholds") {
			input "lastEventThreshold", "number",
				title: "Last event should be within:",
				required: false,
				defaultValue: 7
			input "lastEventThresholdUnit", "enum",
				title: "Choose unit of time:",
				required: false,
				defaultValue: "days",
				options: ["seconds", "minutes", "hours", "days"]
			input "lastEventThresholdOverride", "bool",
				title: "Override Last Event Threshold for Offline Devices?",
				required: false,
				defaultValue: false
		}
	}
}

// Page for SMS and Push notification settings
def notificationsPage() {
	dynamicPage(name:"notificationsPage") {
		section ("Notification Settings") {
			paragraph "When notifications are enabled, notifications will be sent when the device value goes above or below the threshold specified in the Threshold Settings."				
			
			input "createAskAlexaMsg", "bool",
				title: "Create Ask Alexa Message?", 
				required: false,
				defaultValue: false			
			input "sendPush", "bool",
				title: "Send Push Notifications?", 
				required: false
			input("recipients", "contact", title: "Send notifications to") {
				input "phone", "phone", 
					title: "Send text message to",
					description: "Phone Number", 
					required: false
      }
			mode title: "Only send Notifications for specific mode(s)",
				required: false
			input "maxNotifications", "number",
				title: "Enter maximum number of notifications to receive within 5 minutes:",
				required: false
		}
		section ("Battery Notifications") {			
			input "batteryNotificationsEnabled", "bool",
				title: "Send battery notifications?",
				defaultValue: false,
				required: false
			input "batteryNotificationsRepeat", "number",
				title: "Send repeat notifications every: (hours)",
				defaultValue: 0,
				required: false
			input "batteryNotificationsExcluded", "enum",
				title: "Exclude these devices from battery notifications:",
				multiple: true,
				required: false,
				options: getExcludedDeviceOptions("Battery")
		}
		section ("Temperature Notifications") {
			input "temperatureNotificationsEnabled", "bool",
				title: "Send Temperature Notifications?",
				defaultValue: false,
				required: false
			input "temperatureNotificationsRepeat", "number",
				title: "Send repeat notifications every: (hours)",
				defaultValue: 0,
				required: false
			input "temperatureNotificationsExcluded", "enum",
				title: "Exclude these devices from temperature notifications:",
				multiple: true,
				required: false,
				options: getExcludedDeviceOptions("Temperature Measurement")
		}
		section ("Power Notifications") {
			input "powerNotificationsEnabled", "bool",
				title: "Send Power Notifications?",
				defaultValue: false,
				required: false
			input "powerNotificationsRepeat", "number",
				title: "Send repeat notifications every: (hours)",
				defaultValue: 0,
				required: false
			input "powerNotificationsExcluded", "enum",
				title: "Exclude these devices from power notifications:",
				multiple: true,
				required: false,
				options: getExcludedDeviceOptions("Power Meter")
		}
		section ("Last Event Notifications") {
			input "lastEventNotificationsEnabled", "bool",
				title: "Send Last Event notification?",
				defaultValue: false,
				required: false
			input "lastEventNotificationsRepeat", "number",
				title: "Send repeat notifications every: (hours)",
				defaultValue: 0,
				required: false
			input "lastEventNotificationsExcluded", "enum",
				title: "Exclude these devices from last event notifications:",
				multiple: true,
				required: false,
				options: getExcludedDeviceOptions(null)
		}
	}
}

// Page for Polling settings
def pollingPage() {
	dynamicPage(name:"pollingPage") {
		section ("Polling Settings") {
			paragraph "If you enable the polling feature, the devices that support the Polling Capability will be polled at a regular interval."
			paragraph "Polling your devices too frequently can cause them to stop responding or miss other commands that get sent to it."
			input "pollingEnabled", "bool",
				title: "Polling Enabled",
				defaultValue: false,
				required: false
			input "pollingInterval", "number",
				title: "How often should the devices be polled? (minutes)\n(Must be between 5 and ${6 * 24 * 60})",
				defaultValue: (4 * 60),
				range: "5..${6 * 24 * 60}",
				required: false
		}
		section("Polling Restrictions") {
			input "pollingExcluded", "enum",
				title: "Exclude these devices from Polling",
				multiple: true,
				required: false,
				options: getExcludedDeviceOptions("Polling")
		}
	}
}

private getExcludedDeviceOptions(capabilityName) {
	if (capabilityName) {
		getDevicesByCapability(capabilityName).collect { it.displayName }?.sort()
	}
	else {
		getAllDevices().collect { it.displayName }?.sort()
	}
}

// Page for misc preferences.
def otherSettingsPage() {
	dynamicPage(name:"otherSettingsPage") {		
		section ("Other Settings") {
			label(name: "label",
				title: "Assign a name",
				required: false)
			input "iconsEnabled", "bool",
				title: "Display Device State Icons?",
				defaultValue: true,
				required: false
			input "condensedViewEnabled", "bool",
				title: "Condensed View Enabled?",
				defaultValue: false,
				required: false				
		}
		section ("Sorting") {
			input "batterySortByValue", "bool",
				title: "Sort by Battery Value?",
				defaultValue: true,
				required: false
			input "tempSortByValue", "bool",
				title: "Sort by Measurement Value?",
				defaultValue: true,
				required: false
			input "lastEventSortByValue", "bool",
				title: "Sort by Last Event Value?",
				defaultValue: true,
				required: false			
		}	
		section ("Last Event Accuracy") {
			input "checkHistoryThreshold", "number",
				title: "Check History Threshold: (Hours)\n(This SmartApp uses the device's last activity field, but this setting allows you to specify the number of hours the last activity has to be behind before manually retrieving the last event from the device history.)",
				defaultValue: 12,
				range: "1..168",
				required: false
			input "lastEventAccuracy", "number",
				title: "Accuracy Level (1-25)\n(Setting this to a higher number will improve the accuracy for devices that generate a lot of events, but if you're seeing timeout errors in Live Logging, you should set this to a lower number.)",
				defaultValue: 8,
				range: "1..25",
				required: false		
			input "lastEventByStateEnabled", "bool",
				title: "Advanced Last Event Check Enabled?\n(When enabled, the devices events and state changes are used to determine the most recent activity.)",
				defaultValue: true,
				required: false
		}
		section ("Logging") {
			input "logging", "enum",
				title: "Types of messages to log:",
				multiple: true,
				required: false,
				defaultValue: ["debug", "info"],
				options: ["debug", "info", "trace"]
		}
		section ("Resources") {			
			paragraph "If you want to be able to use different icons, fork krlaframboise's GitHub Resources repository and change this url to the forked path.  If you do change this setting, make sure that the new location contains all the Required Files."
			href "", title: "View Required Resource List", 
				style: "external", 
				url: 			"http://htmlpreview.github.com/?https://github.com/krlaframboise/Resources/blob/master/simple-device-viewer/required-resources.html"
			input "resourcesUrl", "text",
				title: "Resources Url:",
				required: false,
				defaultValue: getResourcesUrl()
		}
		section ("Scheduling") {
			paragraph "Leave this field empty unless you're using an external timer to turn on a switch at regular intervals.  If you select a switch, the application will check to see if notifications need to be sent when its turned on instead of using SmartThings scheduler to check every 5 minutes."

			input "timerSwitch", "capability.switch",
				title: "Select timer switch:",
				required: false
		}		
	}
}

def dashboardSettingsPage() {
	dynamicPage(name:"dashboardSettingsPage") {
		section ("Dashboard Settings") {
			if (state.endpoint) {
				log.info "Dashboard Url: ${api_dashboardUrl()}"
				input "dashboardRefreshInterval", "number", 
					title: "Dashboard Refresh Interval: (60-86400 seconds)",
					range: "60..86400",
					defaultValue: 300,
					required: false
				input "dashboardDefaultView", "enum",
					title: "Default View:",
					required: false,
					options: getCapabilitySettingNames(true)
				input "dashboardMenuPosition", "enum", 
					title: "Menu Position:", 
					defaultValue: "Top of Page",
					required: false,
					options: ["Top of Page", "Bottom of Page"]
				input "dashboardLayout", "enum", 
					title: "Layout:", 
					defaultValue: "Normal",
					required: false,
					options: ["Normal", "Condensed - 1 Column", "Condensed - 2 Column", "Condensed - 3 Column"]
				input "displayOnlineOfflineStatus", "bool",
					title: "Display Online/Offline Status:",
					defaultValue: false,
					required: false
				input "customCSS", "text",
					title:"Enter CSS rules that should be appended to the dashboard's CSS file.",
					required: false
				getPageLink("disableDashboardPageLink",
					"Disable Dashboard",
					"disableDashboardPage")
			}
			else {
				getPageLink("enableDashboardPageLink",
					"Enable Dashboard",
					"enableDashboardPage")
			}
		}
	}
}

private getPageLink(linkName, linkText, pageName, args=null) {
	def map = [
		name: "$linkName", 
		title: "$linkText",
		description: "",
		page: "$pageName",
		required: false
	]
	if (args) {
		map.params = args
	}
	href(map)
}

private disableDashboardPage() {	
	dynamicPage(name: "disableDashboardPage", title: "") {
		section() {
			if (state.endpoint) {
				try {
					revokeAccessToken()
				}
				catch (e) {
					logDebug "Unable to revoke access token: $e"
				}
				state.endpoint = null
			}	
			paragraph "The Dashboard has been disabled! Tap Done to continue"	
		}
	}
}

private enableDashboardPage() {
	dynamicPage(name: "enableDashboardPage", title: "") {
		section() {
			if (initializeAppEndpoint()) {
				paragraph "The Dashboard is now enabled. Tap Done to continue"
			} 
			else {
				paragraph "Please go to your SmartThings IDE, select the My SmartApps section, click the 'Edit Properties' button of the Simple Device Viewer app, open the OAuth section and click the 'Enable OAuth in Smart App' button. Click the Update button to finish.\n\nOnce finished, tap Done and try again.", title: "Please enable OAuth for Simple Device Viewer", required: true, state: null
			}
		}
	}
}



// Lists all devices and their last event times.
def lastEventPage() {
	dynamicPage(name:"lastEventPage") {		
		section ("Time Since Last Event") {
			href(
				name: "refreshLastEventLink", 
				title: "Refresh Data",
				description: "${getRefreshLastEventLinkDescription()}",
				page: "refreshLastEventPage",
				required: false
			)
			
			def items = getAllDeviceLastEventListItems()?.unique()
			if (settings.lastEventSortByValue != false) {
				items?.each { it.sortValue = (it.sortValue * -1) }
			}				
			getParagraphs(items)
		}		
	}
}

private getRefreshLastEventLinkDescription() {
	def stateRefreshed = (state.stateCachedTime) ? getTimeSinceLastActivity(new Date().time - state.stateCachedTime) : "?"
	def eventsRefreshed = (state.eventCachedTime) ? getTimeSinceLastActivity(new Date().time - state.eventCachedTime) : "?"
	return "Events refreshed ${eventsRefreshed.toLowerCase()} ago.\nState refreshed ${stateRefreshed.toLowerCase()} ago."
}

def refreshLastEventPage() {
	dynamicPage(name:"refreshLastEventPage") {		
		section () {
			refreshDeviceActivityCache()
			paragraph "Started refreshing last events, but this process could take up to a minute."
		}		
	}
}

// Lists all devices supporting switch capability as links that can be used to toggle their state
def toggleSwitchPage(params) {
	dynamicPage(name:"toggleSwitchPage") {		
		section () {
			paragraph "Wait a few seconds before pressing Done to ensure that the previous page refreshes correctly."
			if (params.deviceId) {
				def device = params.deviceId ? getAllDevices().find { it.id == params.deviceId } : null
				def newState = device?.currentSwitch == "off" ? "on" : "off"
				paragraph toggleSwitch(device, newState)
			}
			else {
				getDevicesByCapability("Switch").each {
					paragraph toggleSwitch(it, "off")
				}
			}			
		}		
	}
}

private toggleSwitch(device, newState) {
	if (device) {	
		if (newState == "on") {
			device.on()
		}
		else {
			device.off()
		}		
		return "Turned ${device.displayName} ${newState.toUpperCase()}"
	}
}

// Lists all devices and all the state of all their capabilities
def capabilityPage(params) {
	dynamicPage(name:"capabilityPage") {	
		def capSetting = params.capabilitySetting ? params.capabilitySetting : state.lastCapabilitySetting
		
		if (capSetting) {
			state.lastCapabilitySetting = capSetting
			section("${getPluralName(capSetting)}") {
				if (capSetting.name in ["Switch","Light"]) {
					href(
						name: "allOffSwitchLink", 
						title: "Turn Off All ${getPluralName(capSetting)}",
						description: "",
						page: "toggleSwitchPage",
						required: false
					)
					getSwitchToggleLinks(getDeviceCapabilityListItems(capSetting))
				}
				else {				
					getParagraphs(getDeviceCapabilityListItems(capSetting))
				}
			}
		}
		else {
			getAllSelectedCapabilitiesSection()
		}			
	}
}

private getAllSelectedCapabilitiesSection() {
	section("All Selected Capabilities") {
		def startTime = new Date().time
		def timeout = 15000
		def aborted = false
		def capListItems = []
		
		def selectedCapSettings = getSelectedCapabilitySettings(timeout)
		
		getAllDevices().each {
			if (new Date().time - startTime > timeout) {
				aborted = true
			}
			else {
				capListItems << getDeviceAllCapabilitiesListItem(selectedCapSettings, it)
			}
		}
		
		if (aborted) {
			paragraph "Unable to load the states of all devices within the allowed time.  If you've selected a lot of devices and capabilities, you might not be able to use the 'All Devices - States' view."
		}
		if (capListItems) {
			getParagraphs(capListItems)
		}
	}
}

private getSwitchToggleLinks(listItems) {
	listItems.sort { it.sortValue }	
	return listItems.unique().each {
		href(
			image: it.image ? it.image : "",
			name: "switchLink${it.deviceId}", 
			title: "${it.title}",
			description: "",
			page: "toggleSwitchPage", 
			required: false,
			params: [deviceId: it.deviceId]
		)
	}
}

private getParagraphs(listItems) {
	listItems.sort { it.sortValue }
	if (!condensedViewEnabled) {
		return listItems.unique().each { 
			it.image = it.image ? it.image : ""
			paragraph image: "${it.image}",	"${it.title}"
		}
	}
	else {
		def content = null
		listItems.unique().each { 
			content = content ? content.concat("\n${it.title}") : "${it.title}"
		}
		if (content) {
			paragraph "$content"
		}
	}
}

private getCapabilityPageLink(cap) {
	return href(
		name: cap ? "${getPrefName(cap)}Link" : "allDevicesLink", 
		title: cap ? "${getPluralName(cap)}" : "All Devices - States",
		description: "",
		page: "capabilityPage",
		required: false,
		params: [capabilitySetting: cap]
	)	
}

// Checks if any devices have the specificed capability
private devicesHaveCapability(name) {	
	return getAllDevices().find { it.hasCapability(name) } ? true : false
}

private getDevicesByCapability(name, excludeList=null) {
	removeExcludedDevices(getAllDevices()
		.findAll { it.hasCapability(name.toString()) }
		.sort() { it.displayName.toLowerCase() }, excludeList)	
}

private getDeviceAllCapabilitiesListItem(selectedCapSettings, device) {
	def listItem = [
		sortValue: device.displayName
	]		
	selectedCapSettings.each {
		if (device.hasCapability(getCapabilityName(it))) {
			listItem.status = (listItem.status ? "${listItem.status}, " : "").concat(getDeviceCapabilityStatusItem(device, it).status)
		}
	}
	listItem.title = getDeviceStatusTitle(device, listItem.status)
	return listItem
}

private getDeviceCapabilityListItems(cap) {
	def items = []
	getDevicesByCapability(getCapabilityName(cap), settings["${getPrefName(cap)}ExcludedDevices"])?.each { 
		if (deviceMatchesSharedCapability(it, cap)) {
			items << getDeviceCapabilityListItem(it, cap)
		}
	}
	return items
}

private deviceMatchesSharedCapability(device, cap) {
	if (cap.name in ["Switch", "Light"]) {
		def isLight = (lightDevices?.find { it.id == device.id }) ? true : false				
		return ((cap.name == "Light") == isLight)
	}
	else {
		return true
	}
}

private getDeviceCapabilityListItem(device, cap) {
	def listItem = getDeviceCapabilityStatusItem(device, cap)
	listItem.deviceId = "${device.id}"
	if (listItem.image && cap.imageOnly && !condensedViewEnabled) {
		listItem.title = "${device.displayName}"
	}
	else {
		listItem.title = "${getDeviceStatusTitle(device, listItem.status)}"
	}
	listItem
}

private getCapabilitySettingByPrefName(prefName) {
	capabilitySettings().find { getPrefName(it) == prefName }
}

private getCapabilitySettingByPluralName(pluralName) {
	capabilitySettings().find { getPluralName(it)?.toLowerCase() == pluralName?.toLowerCase()}
}

private getCapabilitySettingByName(name) {
	capabilitySettings().find { it.name == name }
}

private getAllDeviceLastEventListItems() {
	removeExcludedDevices(getAllDevices(), lastEventExcludedDevices)?.collect {
		getDeviceLastEventListItem(it)		
	}
}

private getDeviceLastEventListItem(device) {
	def now = new Date().time
	def lastActivity = getDeviceLastActivity(device)
	def lastEventTime = lastActivity?.time ?: 0
	
	def listItem = [
		value: lastEventTime ? now - lastEventTime : Long.MAX_VALUE,
		status: lastEventTime ? "${getTimeSinceLastActivity(now - lastEventTime)}" : "N/A",
		deviceId: device.deviceNetworkId
	]
	
	listItem.title = getDeviceStatusTitle(device, listItem.status)
	listItem.sortValue = settings.lastEventSortByValue != false ? listItem.value : device.displayName
	listItem.image = getLastEventImage(lastEventTime, device.status)
	return listItem
}

private getDeviceLastActivity(device) {
	def activity = getDeviceCache(device.deviceNetworkId)?.activity
	if (activity?.size()) {
		return activity.sort { it.time }.last()
	}
}

/*There's currently a bug that limits the number
of events returned to 50 so this method loops
through the list until it finds one that has 
a source containing "DEVICE".*/
private getDeviceLastDeviceEvent(device) {
	def totalLoops = safeToInteger(settings.lastEventAccuracy, 1)
	def startDate = new Date() - 7
	def endDate = new Date()
	def lastEvent
	
	totalLoops = (totalLoops > 3) ? 3 : totalLoops  // Limit to 3 due to event timeout problem.
	
	for (int index= 0; index < totalLoops; index++) {
		def events = device.eventsBetween(startDate, endDate, [max:50]).flatten()
		
		if (events) {			
			lastEvent = events?.find { "${it.source}".startsWith("DEVICE") }
		
			if (lastEvent?.date?.time) {
				// Found an event with the correct source so stop checking.
				index = totalLoops
			}
			else {
				// Haven't found an event with the correct so move the
				// end date so the next 50 events will be retrieved.
				endDate = events.last()?.date
			}
		}
		else {
			// Checked all the events so stop checking.
			index = totalLoops
		}
	}
	if (lastEvent) {		
		return [
			name: lastEvent.name,
			value: lastEvent.value,			
			time: lastEvent.date.time,
			type: "event"
		]
	}
}

private getDeviceLastStateChange(device) {
	if (settings.lastEventByStateEnabled != false) {
		
		def lastState
		device.supportedAttributes.each {
			def attributeState = device.currentState("$it")
			if (attributeState) {
				if (!lastState || lastState.date.time < attributeState.date.time) {
					lastState = attributeState
				}
			}
		}
		
		if (lastState) {
			return [
				name: lastState.name,
				value: lastState.value,
				time: lastState.date?.time,
				type: "state"
			]				
		}		
	}	
}

private getTimeSinceLastActivity(ms) {
	if (ms < msSecond()) {
		return "$ms MS"
	}
	else if (ms < msMinute()) {
		return "${calculateTimeSince(ms, msSecond())} SECS"
	}
	else if (ms < msHour()) {
		return "${calculateTimeSince(ms, msMinute())} MINS"
	}
	else if (ms < msDay()) {
		return "${calculateTimeSince(ms, msHour())} HRS"
	}
	else {
		return "${calculateTimeSince(ms, msDay())} DAYS"
	}		
}

private calculateTimeSince(ms, divisor) {
	return "${((float)(ms / divisor)).round()}"
}

private String getDeviceStatusTitle(device, status) {
	if (!status || status == "null") {
		status = "N/A"
	}
	if (state.refreshingDashboard) {
		return "${device.displayName}${getOnlineOfflineStatus(device.status)}"
	}
	else {
		return "${status} -- ${device.displayName}"
	}	
}

private getOnlineOfflineStatus(deviceStatus) {
	if (settings?.displayOnlineOfflineStatus && deviceStatus?.toLowerCase() in ["online", "offline"]) {
		return " (${deviceStatus.toLowerCase()})"
	}
	else {
		return ""
	}
}

private getDeviceCapabilityStatusItem(device, cap) {
	try {
		return getCapabilityStatusItem(cap, device.displayName, device.currentValue(getAttributeName(cap)).toString())		
	}
	catch (e) {
		log.error "Device: ${device?.displayName} - Capability: $cap - Error: $e"
		return [
			image: "",
			sortValue: device?.displayName,
			value: "",
			status: "N/A"
		]
	}
}

private getCapabilityStatusItem(cap, sortValue, value) {
	def item = [
		image: "",
		sortValue: sortValue,
		value: value
	]
	item.status = item.value
	if ("${item.status}" != "null") {
	
		if (item.status == getActiveState(cap) && !state.refreshingDashboard) {
			item.status = "*${item.status}"
		}
			
		switch (cap.name) {
			case "Acceleration Sensor":
				item.image = getAccelerationImage(item.value)
				break
			case "Battery":			
				item.status = "${item.status}%"
				item.image = getBatteryImage(item.value)
				if (batterySortByValue != false) {
					item.sortValue = safeToInteger(item.value)
				}				
				break
			case "Temperature Measurement":
				item.image = getTemperatureImage(item.value)
				break
			case "Alarm":
				item.image = getAlarmImage(item.value)
				break
			case "Contact Sensor":
				item.image = getContactImage(item.value)
				break
			case "Lock":
				item.image = getLockImage(item.value)
				break
			case "Motion Sensor":
				item.image = getMotionImage(item.value)
				break
			case "Power Meter":
				item.image = getPowerImage(item.value)
				break
			case "Presence Sensor":
				item.image = getPresenceImage(item.value)
				break
			case ["Smoke Detector", "Carbon Monoxide Detector"]:
				item.image = getSmokeCO2Image(item.value)
				break
			case "Switch":
				item.image = getSwitchImage(item.value)
				break
			case "Light":
				item.image = getLightImage(item.value)
				break
			case "Water Sensor":
				item.image = getWaterImage(item.value)
				break
		}
		
		if (cap?.units != null) {
			item.status = "${item.status}${cap.units}"
			if (tempSortByValue != false) {
				item.sortValue = safeToFloat(item.value)
			}
		}
		else {
			item.status = "${item.status}".toUpperCase()
		}
	}
	else {
		item.status = "N/A"
	}
	return item
}

private getSelectedCapabilitySettings(timeout) {	
	if (!settings.enabledCapabilities) {
		return capabilitySettings().findAll { devicesHaveCapability(getCapabilityName(it)) }
	}
	else {
		def startTime = new Date().time
		return capabilitySettings().findAll {	
			if (new Date().time - startTime <= timeout) {
				(getPluralName(it) in settings.enabledCapabilities)
			}
		}
	}
}

private getAllDNIs() {
	return getAllDevices().collect { it.deviceNetworkId }
}

private getAllDevices() {
	def devices = []
	getDeviceInputs().each { deviceInput ->
		settings["$deviceInput"].each { device ->
			if (!devices.find { it.deviceNetworkId == device.deviceNetworkId }) {
				devices << device
			}
		}
	}
	return devices
}

private getDeviceInputs() {
	def deviceInputs = capabilitySettings().collect {
		"${getPrefName(it)}Devices" 
	}
	deviceInputs << "sensors"
	deviceInputs << "actuators"
	return deviceInputs
}

private boolean isDevice(obj) {
	try {
		if (obj?.id) {
			// This isn't a device if the following line throws an exception.
			obj.hasCapability("") 
			return true
		}
		else {
			return false
		}
	}
	catch (e) {
		return false
	}
}

private String getLastEventImage(lastEventTime, deviceStatus) {
	def status = lastEventIsOld(lastEventTime, deviceStatus) ? "warning" : "ok"
	return getImagePath("${status}.png")
}

private boolean lastEventIsOld(lastEventTime, deviceStatus) {	
	try {
		if (!lastEventTime || offlineOverride(deviceStatus)) {
			return true
		}
		else {
			return ((new Date().time - getLastEventThresholdMS()) > lastEventTime)
		}
	}
	catch (e) {
		return true
	}
}

private boolean offlineOverride(deviceStatus) {
	return (settings?.lastEventThresholdOverride && (deviceStatus?.toLowerCase() == "offline"))
}

private String getAccelerationImage(currentState) {
	def status = (currentState == "active") ? "active" : "inactive"
	return getImagePath("acceleration-${status}.png")
}

private String getPresenceImage(currentState) {
	def status = (currentState == "present") ? "present" : "not-present"
	return getImagePath("${status}.png")
}

private String getContactImage(currentState) {
	return  getImagePath("${currentState}.png")	
}

private String getLockImage(currentState) {
	return  getImagePath("${currentState}.png")	
}

private String getMotionImage(currentState) {
	def status = (currentState == "active") ? "motion" : "no-motion"
	return  getImagePath("${status}.png")	
}

private String getSwitchImage(currentState) {
	return  getImagePath("switch-${currentState}.png")	
}

private String getLightImage(currentState) {
	return  getImagePath("light-${currentState}.png")
}

private String getAlarmImage(currentState) {
	return  getImagePath("alarm-${currentState}.png")	
}

private String getWaterImage(currentState) {
	return  getImagePath("${currentState}.png")	
}

private String getSmokeCO2Image(currentState) {
	def status = (currentState == "detected") ? "detected" : "clear"
	return getImagePath("smoke-${status}.png")	
}

private String getBatteryImage(batteryLevel) {
	def status 
	if (batteryIsLow(batteryLevel)) {
		status = "low"
	}
	else {
		switch (safeToInteger(batteryLevel, 100)) {
			case { it == 100}:
				status = "normal"
				break
			case { it >= 75 }:
				status = "normal-75"
				break
			case { it >= 50 }:
				status = "normal-50"
				break
			case { it >= 25 }:
				status = "normal-25"
				break
		}	
	}
	return  getImagePath("${status}-battery.png")	
}

private String getTemperatureImage(tempVal) {		
	def status = "normal"
	if (tempIsHigh(tempVal)) {
		status = "high"
	}
	else if (tempIsLow(tempVal)) {
		status = "low"
	}	
	return getImagePath("${status}-temp.png")
}

private String getPowerImage(powerVal) {		
	def status = "ok"
	if (powerIsHigh(powerVal)) {
		status = "warning"
	}
	else if (powerIsLow(powerVal)) {
		status = "warning"
	}	
	return getImagePath("${status}.png")
}

private String getImagePath(imageName) {
	if (iconsAreEnabled()) {
		if (state.refreshingDashboard) {
			return imageName
		}
		else {
			return "${getResourcesUrl()}/$imageName"
		}
	}
}

private boolean iconsAreEnabled() {
	return (iconsEnabled || iconsEnabled == null || state.refreshingDashboard)
}

private getResourcesUrl() {
	def url = "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-device-viewer"

	if (settings?.resourcesUrl) {
		url = settings.resourcesUrl
	}
	
	return url
}

// Revokes the dashboard access token, if applicable.
def uninstalled() {
	if (state.endpoint) {
		try {
			logDebug "Revoking dashboard access token"
			revokeAccessToken()
		}
		catch (e) {
			log.warn "Unable to revoke dashboard access token: $e"
		}
	}
}

// Subscribes to events, starts schedules and initializes all settings.
def installed() {
	initialize()
}

// Resets subscriptions, scheduling and ensures all settings are initialized.
def updated() {
	unsubscribe()
	unschedule()
	state.refreshingDashboard = false
	
	if (state.capabilitySettings) {
		cleanState()
	}
	
	initialize()
	
	logDebug "State Used: ${(state.toString().length() / 100000)*100}%"
}

private initialize() {
	if (!state.sentNotifications) {
		state.sentNotifications = []
	}
	
	if (settings.timerSwitch) {
		subscribe(timerSwitch, "switch.on", timerSwitchEventHandler)
	}
	else {		
		runEvery5Minutes(performScheduledTasks)
	}
	
	initializeDevicesCache()
}

// Starting with version 1.9, the capabilitySettings are
// no longer stored in state so this cleans up the old data.
private cleanState() {
	def sentNotifications = state.sentNotifications
	def devicesCache = state.devicesCache
	state.clear()
	state.sentNotifications = sentNotifications
	state.devicesCache = devicesCache
}

// Remove cached data for devices no longer selected and
// add cached data for newly selected devices.
void initializeDevicesCache() {
	def dnis = getAllDNIs()
	
	state.devicesCache?.removeAll { cache ->
		!dnis?.find { dni -> cache.dni == dni }
	}	
}

// Used to generate notifications when external timer is being used instead of relying on SmartThings scheduler. 
def timerSwitchEventHandler(evt) {
	performScheduledTasks()	
}

def performScheduledTasks() {
	state.lastPerformedScheduledTasks = new Date().time
	if (canCheckDevices(state.lastDeviceCheck)) {
		checkDevices()		
	}
	if (canPollDevices(state.lastDevicePoll)) {
		runIn(61, refreshDeviceActivityCache)
		pollDevices()
	}
	else {
		refreshDeviceActivityCache()
	}
}

void pollDevices() {
	log.warn "Polling Devices"
	logDebug "Polling Devices"
	state.lastDevicePoll = new Date().time	
	getDevicesByCapability("Polling", pollingExcluded)*.poll()
}

private canPollDevices(lastPoll) {
	return settings.pollingEnabled &&
		timeElapsed((lastPoll ?: 0) + msMinute(safeToInteger(settings.pollingInterval, 5)), true)
}

void refreshDeviceActivityCache() {
	runIn(30, refreshDeviceStateCache)
	refreshDeviceEventCache()		
}

void refreshDeviceStateCache() {
	refreshDeviceActivityTypeCache("state")	
}

void refreshDeviceEventCache() {
	refreshDeviceActivityTypeCache("event")	
}

void refreshDeviceActivityTypeCache(activityType) {
	def devices = getAllDevices()
	def deviceCount = devices?.size()
	if (deviceCount) {
		def cachedTime = new Date().time
		
		def deviceIndex = (safeToInteger(state."${activityType}DeviceIndex", -1))
		
		def checkHistoryCutoff = (new Date().time - (safeToInteger(settings?.checkHistoryThreshold, 12) * 60 * 60 * 1000))
		
		for (int i= 0; i < deviceCount; i++) {
			
			deviceIndex += 1
			deviceIndex = (deviceIndex >= deviceCount) ? 0 : deviceIndex
			def device = devices[deviceIndex]
			
			def lastActivity
			def lastActivityDate = device.getLastActivity()
			if (lastActivityDate && (lastActivityDate.time >= checkHistoryCutoff)) {
				lastActivity = [
					name: "unknown",
					value: "",
					time: lastActivityDate.time,
					type: activityType
				]
			}
			else {
				if (activityType == "event") {
					if (!lastActivity) {
						lastActivity = getDeviceLastDeviceEvent(device)
					}
				}
				else {
					if (!lastActivity) {
						lastActivity = getDeviceLastStateChange(device)
					}
				}
			}
			
			if (lastActivity) {
				lastActivity.cachedTime = cachedTime
				saveLastActivityToDeviceCache(device.deviceNetworkId, lastActivity)
			}
			
			if (((new Date().time) - cachedTime) > 10000) {
				logTrace "Aborted refreshing ${activityType} cache after device ${devices[deviceIndex]?.displayName} [${deviceIndex}]. (Refreshed ${i} of the ${deviceCount} devices)"
				
				if ((new Date().time - (state.lastPerformedScheduledTasks ?: 0)) < 190000) {
					// Start the refresh again in 1 minute as long as it won't overlap with the refresh that occurs every 5 minutes.
					if (activityType == "event") {
						runIn(61, refreshDeviceEventCache)
					}
					else {
						runIn(61, refreshDeviceStateCache)
					}
				}				
				i = deviceCount
			}
		}	
		state."${activityType}CachedTime" = cachedTime
		state."${activityType}DeviceIndex" = deviceIndex
	}	
}

void saveLastActivityToDeviceCache(dni, lastActivity) {
	def found = false
	def activity = getDeviceCache(dni).activity.collect {
		if (it.type == lastActivity.type) {
			found = true
			return (it.time < lastActivity.time) ? lastActivity : it
		}
		else {
			return it
		}
	}

	if (!found) {
		activity << lastActivity
	}
		
	getDeviceCache(dni).activity = activity
}

private getDeviceCache(dni) {
	if (!state.devicesCache) {
		state.devicesCache = []
	}
	
	def deviceCache = state.devicesCache.find { cache -> "$dni" == "${cache.dni}" }
	if (!deviceCache) {
		deviceCache = [dni: "$dni", activity: [ ]]
		state.devicesCache << deviceCache
	}
	return deviceCache
}


// Generates notifications if device attributes fall outside of specified thresholds and ensures that notifications are spaced at least 5 minutes apart.
def checkDevices() {
	logDebug "Checking Device Thresholds"
	
	state.lastDeviceCheck = new Date().time
	state.currentCheckSent = 0
		
	if (settings.batteryNotificationsEnabled) {
		runIn(90, checkBatteries)
	}			
	if (settings.temperatureNotificationsEnabled) {
		runIn(61, checkTemperatures)
	}			
	if (settings.powerNotificationsEnabled) {
		runIn(30, checkPowers)
	}			
	if (settings.lastEventNotificationsEnabled) {
		checkLastEvents()
	}	
}

private canCheckDevices(lastCheck) {	
	return (settings.batteryNotificationsEnabled ||
		settings.temperatureNotificationsEnabled ||
		settings.powerNotificationsEnabled ||
		settings.lastEventNotificationsEnabled) &&
		timeElapsed((lastCheck ?: 0) + msMinute(5), true)
}

def checkTemperatures() {
	logDebug "Checking Temperatures"
	def cap = getCapabilitySettingByName("Temperature Measurement")
	
	getDevicesByCapability("Temperature Measurement", temperatureNotificationsExcluded)?.each {	
		def item = getDeviceCapabilityStatusItem(it, cap)
		
		def message = null
		if (tempIsHigh(item.value)) {
			message = "High Temperature Alert - ${getDeviceStatusTitle(it, item.status)}"			
		}
		else if (tempIsLow(item.value)) {			
			message = "Low Temperature Alert - ${getDeviceStatusTitle(it, item.status)}"			
		}
		
		handleDeviceNotification(it, message, "temperature", temperatureNotificationsRepeat)
	}
}

private boolean tempIsHigh(val) {
	isAboveThreshold(val, highTempThreshold, 73)
}

private boolean tempIsLow(val) {
	isBelowThreshold(val, lowTempThreshold, 63)	
}

def checkPowers() {
	logDebug "Checking Powers"
	def cap = getCapabilitySettingByName("Power Meter")
	
	getDevicesByCapability("Power Meter", powerNotificationsExcluded)?.each {	
		def item = getDeviceCapabilityStatusItem(it, cap)
		
		def message = null
		if (powerIsHigh(item.value)) {
			message = "High Power Alert - ${getDeviceStatusTitle(it, item.status)}"			
		}
		else if (powerIsLow(item.value)) {			
			message = "Low Power Alert - ${getDeviceStatusTitle(it, item.status)}"			
		}
		
		handleDeviceNotification(it, message, "power", powerNotificationsRepeat)
	}
}

private boolean powerIsHigh(val) {
	isAboveThreshold(val, highPowerThreshold, 500)
}

private boolean powerIsLow(val) {
	isBelowThreshold(val, lowPowerThreshold, 50)
}

def checkBatteries() {
	logDebug "Checking Batteries"
	def cap = getCapabilitySettingByName("Battery")

	getDevicesByCapability("Battery", batteryNotificationsExcluded)?.each {
		def item = getDeviceCapabilityStatusItem(it, cap)
		
		def message = batteryIsLow(item.value) ? "Low Battery Alert - ${getDeviceStatusTitle(it, item.status)}" : null
		
		handleDeviceNotification(it, message, "battery", batteryNotificationsRepeat)
	}
}

private boolean batteryIsLow(batteryLevel) {
	isBelowThreshold(batteryLevel, lowBatteryThreshold, 25)
}

private boolean isAboveThreshold(val, threshold, int defaultThreshold) {
	if (threshold == null) {
		return false
	}
	else {
		return safeToInteger(val) > safeToInteger(threshold, defaultThreshold)
	}
}

private boolean isBelowThreshold(val, threshold, int defaultThreshold) {
	if (threshold == null) {
		return false
	}
	else {
		safeToInteger(val) < safeToInteger(threshold,defaultThreshold)
	}
}

private int safeToInteger(val, defaultVal=0) {
	try {
		if (val != null && "$val".isNumber()) {
			if ("$val".isInteger()) {
				return "$val".toInteger()
			}
			else if ("$val".isFloat()) {
				return "$val".toFloat().round().toInteger()
			}
			else if ("$val".isDouble()) {
				return "$val".toDouble().round().toInteger()
			}
			else {
				logDebug "Unable to parse $val to Integer so returning 0"
				return 0
			}
		}
		else {
			return safeToInteger(defaultVal, 0)
		}		
	}
	catch (e) {
		logDebug "safeToInteger($val, $defaultVal) failed with error $e"
		return 0
	}
}

private int safeToFloat(val, defaultVal=0) {
	try {
		if (val && "$val".isNumber()) {
			if ("$val".isInteger() || "$val".isFloat() || "$val".isDouble()) {
				return "$val".toFloat()
			}			
			else {
				logDebug "Unable to parse $val to Integer so returning 0"
				return 0
			}
		}
		else {
			return safeToFloat(defaultVal, 0)
		}		
	}
	catch (e) {
		logDebug "safeToFloat($val, $defaultVal) failed with error $e"
		return 0
	}
}

def checkLastEvents() {
	logDebug "Checking Last Events"
	removeExcludedDevices(getAllDevices(), lastEventNotificationsExcluded)?.each {
		
		def item = getDeviceLastEventListItem(it)

		def isOld = (item.value > getLastEventThresholdMS() || offlineOverride(it.status))
		
		def message = null
		if (isOld) {
			message = "Last Event Alert - ${getDeviceStatusTitle(it, item.status)}"
			if (offlineOverride(it.status)) {
				message = "${message} (OFFLINE)"
			}
		}
		   		
		handleDeviceNotification(it, message, "lastEvent", lastEventNotificationsRepeat)
	}
}

private long getLastEventThresholdMS() {
	long threshold = lastEventThreshold ? lastEventThreshold : 7
	long unitMS
	switch (lastEventThresholdUnit) {
		case "seconds":
			unitMS = msSecond()
			break
		case "minutes":
			unitMS = msMinute()
			break
		case "hours":
			unitMS = msHour()
			break
		default:
			unitMS = msDay()
	}
	return (threshold * unitMS)
}

private long msSecond(multiplier=1) {
	return (1000 * multiplier)
}

private long msMinute(multiplier=1) {
	return (msSecond(60) * multiplier)
}

private long msHour(multiplier=1) {
	return (msMinute(60) * multiplier)
}

private long msDay(multiplier=1) {
	return (msHour(24) * multiplier)
}


private removeExcludedDevices(deviceList, excludeList) {
	if (excludeList) {
		def result = []
		deviceList.each {
			def displayName = "${it.displayName}"
			if (!excludeList.find { it == "$displayName" }) {
				result << it
			}
		}
		return result
	}
	else {
		return deviceList
	}
}

private handleDeviceNotification(device, message, notificationType, notificationRepeat) {
	def id = "$notificationType${device.id}"
	def lastSentMap = state.sentNotifications.find { it.id == id }
	def lastSent = lastSentMap?.lastSent
	def repeatMS = notificationRepeat ? msHour(notificationRepeat) : 0	
	def unknownStatus = message?.contains("- N/A --") ? true : false
			
	if (message && !unknownStatus) {
		if (canSendNotification(lastSent, repeatMS)){
			if (lastSent) {
				lastSentMap.lastSent = new Date().time
			}
			else {
				state.sentNotifications << [id: "$id", lastSent: new Date().time]				
			}			
			sendNotificationMessage(message)
		}
	}
	else if (unknownStatus) {
		// Do nothing because occassionally null is returned for
		// battery or last event when it really has a value causing
		// false notifications to be sent out.
	}
	else if (lastSent) {
		state.sentNotifications.remove(lastSentMap)
	}
}

private boolean canSendNotification(lastSent, repeatMS) {	
	def sendLimitExceeded = state.currentCheckSent >= (maxNotifications ? maxNotifications : 1000)
	
	if (!lastSent && !sendLimitExceeded) {
		return true
	}
	else {
		return (!sendLimitExceeded && repeatMS > 0 && timeElapsed(lastSent + repeatMS))
	}
}

private sendNotificationMessage(message) {	
	if (sendPush || recipients || phone) {
		state.currentCheckSent = state.currentCheckSent + 1
		logInfo "Sending $message"
		if (settings.createAskAlexaMsg) {
			sendLocationEvent(name: "AskAlexaMsgQueue", value: "Simple Device Viewer", isStateChange: true, descriptionText: "${message.replace(' - ', ' ')}", unit: "SDV")
		}
		if (sendPush) {
			sendPush(message)
		}
		if (location.contactBookEnabled && recipients) {
			sendNotificationToContacts(message, recipients)
		} else {
			if (phone && sendPush) {
				sendSmsMessage(phone, message)
			}
			else if (phone) {
				sendSms(phone, message)
			}
		}
	}
	else {
		logInfo "Could not send message because notifications have not been configured.\nMessage: $message"
	}
}

private boolean timeElapsed(timeValue, nullResult=false) {
	if (timeValue != null) {
		def currentTime = new Date().time
		return (timeValue <= currentTime)
	} else {
		return nullResult
	}
}

private getCapabilitySettingNames(includeEvents) {
	def items = []
	if (includeEvents) {
		items << "Events"
	}
	items += capabilitySettings().collect { getPluralName(it) }?.unique()
	return items.sort()
}

private getCapabilityName(capabilitySetting) {
	capabilitySetting.capabilityName ?: capabilitySetting.name
}

private String getAttributeName(capabilitySetting) {
	capabilitySetting.attributeName ?: capabilitySetting.name.toLowerCase()
}

private String getActiveState(capabilitySetting) {
	capabilitySetting.activeState ?: capabilitySetting.name.toLowerCase()
}

private String getPrefName(capabilitySetting) {
	capabilitySetting.prefName ?: getPrefType(capabilitySetting)
}

private String getPrefType(capabilitySetting) {
	capabilitySetting.prefType ?: capabilitySetting.name.toLowerCase()
}

private String getPluralName(capabilitySetting) {
	capabilitySetting.pluralName ?: "${capabilitySetting.name}s"
}


private capabilitySettings() {
	[		
		[
			name: "Acceleration Sensor",
			prefType: "accelerationSensor",
			attributeName: "acceleration",
			activeState: "active",
			imageOnly: true
		],
		[
			name: "Alarm",
			activeState: "off",
			imageOnly: true
		],
		[
			name: "Battery",
			pluralName: "Batteries"
		],
		[
			name: "Carbon Monoxide Detector",
			prefType: "carbonMonoxideDetector",
			attributeName: "carbonMonoxide",
			activeState: "detected",
			imageOnly: true
		],
		[
			name: "Contact Sensor",
			prefType: "contactSensor",
			attributeName: "contact",
			activeState: "open",
			imageOnly: true
		],
		[
			name: "Energy Meter",
			prefType: "energyMeter",
			attributeName: "energy",
			units: " kWh"
		],
		[
			name: "Illuminance Measurement",
			pluralName: "Illuminance Sensors",
			prefType: "illuminanceMeasurement",
			attributeName: "illuminance",
			units: " lx"
		],
		[
			name: "Light",
			prefName: "light",
			prefType: "switch",
			capabilityName: "Switch",
			attributeName: "switch",
			activeState: "on",		
			imageOnly: true
		],
		[
			name: "Lock",
			activeState: "locked",
			imageOnly: true
		],		
		[
			name: "Motion Sensor", 
			prefType: "motionSensor",
			attributeName: "motion",
			activeState: "active",
			imageOnly: true
		],
		[
			name: "Power Meter",
			prefType: "powerMeter",
			attributeName: "power",
			units: " W"
		],
		[
			name: "Presence Sensor",
			prefType: "presenceSensor",
			attributeName: "presence",
			activeState: "present",
			imageOnly: true
		],
		[
			name: "Relative Humidity Measurement",
			pluralName: "Relative Humidity Sensors",
			prefType: "relativeHumidityMeasurement",
			attributeName: "humidity",
			units: "%"
		],
		[
			name: "Smoke Detector",
			prefType: "smokeDetector",
			attributeName: "smoke",
			activeState: "detected",
			imageOnly: true
		],
		[
			name: "Switch",
			pluralName: "Switches",		
			activeState: "on",
			imageOnly: true
		],		
		[
			name: "Temperature Measurement",
			pluralName: "Temperature Sensors",
			prefType: "temperatureMeasurement",
			attributeName: "temperature",
			units: "°${location.temperatureScale}"
		],
		[
			name: "Valve",
			activeState: "open",
			attributeName: "contact"
		],
		[
			name: "Water Sensor",
			prefType: "waterSensor",
			attributeName: "water",
			activeState: "wet",
			imageOnly: true
		]
	]
}



/********************************************
*    Dashboard
********************************************/
private initializeAppEndpoint() {	
	if (!state.endpoint) {
		try {
			def accessToken = createAccessToken()
			if (accessToken) {
				state.endpoint = apiServerUrl("/api/token/${accessToken}/smartapps/installations/${app.id}/")				
			}
		} 
		catch(e) {
			state.endpoint = null
		}
	}
	logDebug "Dashboard Url: ${api_dashboardUrl()}"	
	return state.endpoint
}

mappings {
	path("/dashboard") {action: [GET: "api_dashboard"]}
	path("/dashboard/:capability") {action: [GET: "api_dashboard"]}	
	path("/dashboard/:capability/:cmd") {action: [GET: "api_dashboard"]}
	path("/dashboard/:capability/:cmd/:deviceId") {action: [GET: "api_dashboard"]}	
}

private api_dashboardUrl(capName=null) {	
	def pageName
	capName = capName ?: api_getDefaultCapabilityName()
	if (capName?.toLowerCase() == "events") {
		pageName = "events"
	}
	else {		
		def cap = getCapabilitySettingByPluralName(capName)
		pageName = (cap ? getPluralName(cap)?.toLowerCase()?.replace(" ", "-") : "") ?: "lights"		
	}
	return "${state.endpoint}dashboard/${pageName}"
}

private api_getDefaultCapabilityName() {
	if (settings?.dashboardDefaultView) {
		return settings.dashboardDefaultView
	}
	else {
		return "events"
	}	
}

def api_dashboard() {
	def cap
	def currentUrl
	def menu = ""
	def header = ""
	def footer = ""
	def refreshInterval = 300
	def html = ""

	try {
		state.refreshingDashboard = true
		header = api_getPageHeader()		
			
		if (params.capability == "events") {
			currentUrl = api_dashboardUrl("events")
			header = api_getPageHeader("Events")
		}
		else if (params.capability) {			
			cap = params.capability ? getCapabilitySettingByPluralName(params.capability?.replace("-", " ")) : null
		
			currentUrl = api_dashboardUrl(getPluralName(cap))
			header = api_getPageHeader("${getPluralName(cap)}")
		}	
		
		if (!params.capability && state.normalRefreshInterval) {
			currentUrl = api_dashboardUrl(null)
			state.normalRefreshInterval = false	// Prevents fast refresh loop
			refreshInterval = 0			
		}
		else {
			refreshInterval = api_getRefreshInterval(params.cmd)
		}
		
		menu = api_getMenuHtml(currentUrl)
		footer = api_getPageFooter(null, currentUrl)
		
		if (params.capability == "events") {
			def items = getAllDeviceLastEventListItems()?.unique()
			if (settings.lastEventSortByValue != false) {
				items?.each { it.sortValue = (it.sortValue * -1) }
			}
			html = api_getItemsHtml(items)
		}
		else if (cap) {
			html = api_getCapabilityHtml(cap, currentUrl, params.deviceId, params.cmd)
		}
		
		html = "<section>$html</section>"
	}
	catch(e) {
		log.error "Unable to load dashboard:\n$e"
		html = api_getPageErrorHtml(e)
	}
	state.refreshingDashboard = false
	return api_renderHtmlPage(api_getPageBody(header, html, menu, footer), currentUrl, refreshInterval)
}

private api_getRefreshInterval(cmd) {
	if (api_isToggleSwitchCmd(cmd) && state.normalRefreshInterval) {
		state.normalRefreshInterval = false // Prevents fast refresh loop
		return 3
	}
	else {
		state.normalRefreshInterval = true
		return settings.dashboardRefreshInterval ?: 300
	}
}

private api_getCapabilityHtml(cap, currentUrl, deviceId, cmd) {	
	def html = ""
	if (api_isToggleSwitchCmd(cmd)) {		
		if (deviceId) {
			html = "<h1>${api_toggleSwitch(cap, deviceId, cmd)}</h1>"
		}
		else {
			html = api_toggleSwitches(cap, cmd)
		}
	
		html = "<div class=\"command-results\">$html</div>"		
	}			
	
	if (cap.name in ["Switch","Light", "Alarm"]) {
		html += api_getToggleItemsHtml(currentUrl, getDeviceCapabilityListItems(cap))
	}
	else {
		html += api_getItemsHtml(getDeviceCapabilityListItems(cap))
	}
	return html
}

private api_isToggleSwitchCmd(cmd) {
	return (cmd in ["on", "off", "toggle"])
}

private api_getMenuHtml(currentUrl) {
	def className = api_menuAtTop() ? "top" : "bottom"
	def html = "<nav class=\"$className\">"
	
	html += api_getMenuItemHtml("Refresh", "refresh", currentUrl)
	
	html += api_getMenuItemHtml("Events", "warning", api_dashboardUrl("events"))
	
	getSelectedCapabilitySettings(15000).each {
		html += api_getMenuItemHtml(getPluralName(it), getPrefName(it), api_dashboardUrl(getPluralName(it)))
	}
	
	html += "</nav>"
	return html
}

private api_getMenuItemHtml(linkText, className, url) {
	return "<div class=\"menu-item\"><a href=\"$url\" ${api_getWaitOnClickAttr()} class=\"item-image $className\"><span>${linkText}</span></a></div>"
}

private api_toggleSwitches(cap, cmd) {
	def html = ""	
	
	getDeviceCapabilityListItems(cap).each {
		html += "<li>${api_toggleSwitch(cap, it.deviceId, cmd)}</li>"
	}
	
	if (html) {
		return "<h1>The following changes were made:</h1><ul>$html</ul>"
	}
	else {
		return "<h1>No Changes Were Made</h1>"
	}	
}

private api_toggleSwitch(cap, deviceId, cmd) {
	def device = deviceId ? getAllDevices().find { it.id == deviceId } : null
		
	if (device) {
		def newState = api_getNewSwitchState(device, cmd)
		if (newState) {
			return toggleSwitch(device, newState)
		}		
		else {
			return "Unable to determine new switch state for ${device.displayName}"
		}
	}
	else {
		return "Unable to find a device with id ${deviceId}"
	}		
}

private api_getNewSwitchState(device, cmd) {
	if (cmd in ["on", "off"]) {
		return cmd
	}
	else if (cmd == "toggle") {
		return device?.currentSwitch == "off" ? "on" : "off"
	}
	else {
		return ""
	}
}

private api_getToggleItemsHtml(currentUrl, listItems) {
	def html = ""
			
	listItems.unique().each {		
		html += api_getItemHtml(it.title, it.image, "${currentUrl}/toggle/${it.deviceId}", it.deviceId, it.status)
	}
	
	def pluralName
	def imageName	
	if (listItems) {
		pluralName = listItems[0] ? getPluralName(listItems[0]) : ""
		imageName = listItems[0]?.image?.replace(".png","")
		imageName = imageName?.replace("-on", "")?.replace("-off", "")
	}	
	
	if (imageName in ["light", "switch"]) {
		html += api_getItemHtml("Turn All Off", "${imageName}-off all-command", "${currentUrl}/off", "", "")	
		
		html += api_getItemHtml("Turn All On", "${imageName}-on all-command", "${currentUrl}/on", "", "")
	}
	return html
}

private api_getItemsHtml(listItems) {
	def html = ""		
	
	listItems.sort { it.sortValue }
	
	listItems.unique().each {				
		html += api_getItemHtml(it.title, it.image, null, it.deviceId, it.status)
	}
	return html
}

private api_getItemHtml(text, imageName, url, deviceId, status) {
	def imageClass = imageName ? imageName?.replace(".png", "") : ""
	def deviceClass = deviceId ? deviceId?.replace(" ", "-") : "none"
	def html 
	
	if (url) {
		html = "<a class=\"item-text\" href=\"$url\" ${api_getWaitOnClickAttr()}><span class=\"label\">$text</span></a>"
	}
	else {
		html = "<div class=\"item-text\"><span class=\"label\">$text</span></div>"		
	}
	
	html = "<div class=\"item-image-text\"><div class=\"item-image $imageClass\"><span class=\"item-status\">$status</span></div>$html</div>"
	
	return "<div class=\"device-item device-id-$deviceClass\">$html</div>"
}

private api_getWaitOnClickAttr() {
	return "onclick=\"displayWaitMsg(this)\""
}

private api_getPageBody(header, content, menu, footer) {
	if (api_menuAtTop()) {
		return "$header$menu$content$footer"
	}
	else {
		return "$header$content$menu$footer"		
	}
}

private api_menuAtTop() {
	return (settings.dashboardMenuPosition != "Bottom of Page")
}

private api_getPageHeader(html=null) {
	def header = "Simple Device Viewer"
	header += html ? " - $html" : ""
	return "<header>$header</header>"
}

private api_getPageFooter(html, currentUrl) {
	html = html ?: ""
	return "<footer>$html<textarea class=\"dashboard-url\" rows=\"2\">${currentUrl}</textarea></footer>"
}

private api_getPageErrorHtml(e) {
	return "<div class=\"error-message\"><h1>Unable to Load Dashboard</h1><h2>Error Message:</h2><p>$e</p><p><a href=\"${api_dashboardUrl()}\">Back to Default Dashboard</a></p></div>"		
}

private api_renderHtmlPage(html, url, refreshInterval) {
	render contentType: "text/html", 
		data: "<!DOCTYPE html><html lang=\"en\"><head><title>Simple Device Viewer - Dashboard</title><meta charset=\"utf-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><meta http-equiv=\"refresh\" content=\"${refreshInterval}; URL=${url}\">${api_getCSS()}</head><body>${html}${api_getJS()}</body></html>"
}

private api_getJS() {	
	return "<script>function displayWaitMsg(link) { link.className += \" wait\"; }</script>"
}

private api_getCSS() {
	// return "<link rel=\"stylesheet\" href=\"${getResourcesUrl()}/dashboard.css\">"
	
	def css = "body {	font-size: 100%;	text-align:center;	font-family:Helvetica,arial,sans-serif;	margin:0 0 10px 0;	background-color: #000000;}header, nav, section, footer {	display: block;	text-align:center;}header {	margin: 0 0 0 0;	padding: 4px 0 4px 0;	width: 100%;		font-weight: bold;	font-size: 100%;	background-color:#808080;	color:#ffffff;}nav.top{	padding-top: 0;}nav.bottom{	padding: 4px 4px 4px 4px;}section {	padding: 10px 20px 40px 20px;}.command-results {	background-color: #d6e9c6;	margin: 0 20px 20px 20px;	padding: 10px 20px 10px 20px;	border-radius: 100px;}.command-results h1 {	margin: 0 0 0 0;}.command-results ul {	list-style: none;}.command-results li {	line-height: 1.5;	font-size: 120%;}.dashboard-url {	display:block;	width:100%;	font-size: 80%;}.device-id-none{	background-color: #d6e9c6 !important;}.refresh {	background-image: url('refresh.png');}.acceleration-active {	background-image: url('acceleration-active.png');}.acceleration-inactive{	background-image: url('acceleration-inactive.png');}.alarm, .alarm-both {	background-image: url('alarm-both.png');}.alarm-siren {	background-image: url('alarm-siren.png');}.alarm-strobe {	background-image: url('alarm-strobe.png');}.alarm-off {	background-image: url('alarm-off.png');}.battery, .normal-battery {	background-image: url('normal-battery.png');}.normal-75-battery {	background-image: url('normal-75-battery.png');}.normal-50-battery {	background-image: url('normal-50-battery.png');}.normal-25-battery {	background-image: url('normal-25-battery.png');}.low-battery {	background-image: url('low-battery.png');}.open {	background-image: url('open.png');}.contactSensor, .closed {	background-image: url('closed.png');}.light, .light-on {	background-image: url('light-on.png');}.light-off {	background-image: url('light-off.png');}.lock, .locked{	background-image: url('locked.png');}.unlocked {	background-image: url('unlocked.png');}.motionSensor, .motion {	background-image: url('motion.png');}.no-motion {	background-image: url('no-motion.png');}.presenceSensor, .present {	background-image: url('present.png');}.not-present {	background-image: url('not-present.png');}.smokeDetector, .smoke-detected {	background-image: url('smoke-detected.png');}.smoke-clear {	background-image: url('smoke-clear.png');}.switch, .switch-on {	background-image: url('switch-on.png');}.switch-off {	background-image: url('switch-off.png');}.temperatureMeasurement, .normal-temp {	background-image: url('normal-temp.png');}.low-temp {	background-image: url('low-temp.png');}.high-temp {	background-image: url('high-temp.png');}.waterSensor, .dry {	background-image: url('dry.png');}.wet {	background-image: url('wet.png');}.ok {	background-image: url('ok.png');}.warning {	background-image: url('warning.png');}.device-item {	width: 200px;	display: inline-block;	background-color: #ffffff;	margin: 2px 2px 2px 2px;	padding: 4px 4px 4px 4px;	border-radius: 5px;}.item-image-text {	position: relative;	height: 75px;	width:100%;	display: table;}.item-image {	display: table-cell;	position: relative;	width: 35%;	border: 1px solid #cccccc;	border-radius: 5px;	background-repeat:no-repeat;	background-size:auto 70%;	background-position: center bottom;}.item-status {	width: 100%;	font-size:75%;	display:inline-block;}.item-text {	display: table-cell;	width: 65%;	position: relative;	vertical-align: middle;}a.item-text {	color:#000000;}.item-text.wait, .menu-item a.wait{	color:#ffffff;	background-image:url('wait.gif');	background-repeat:no-repeat;	background-position: center bottom;}.item-text.wait{	background-size:auto 100%;}.label {	display:inline-block;	vertical-align: middle;	line-height:1.4;	font-weight: bold;	padding-left:4px;}.menu-item {	display: inline-block;	background-color:#808080;	padding:4px 4px 4px 4px;	border:1px solid #000000;	border-radius: 5px;	font-weight:bold;}.menu-item .item-image{	display:table-cell;	background-size:auto 45%;	height:50px;	width:75px;	border:0;	border-radius:0;}.menu-item .item-image.switch,.menu-item .item-image.light,.menu-item .item-image.battery,.menu-item .item-image.alarm,.menu-item .item-image.refresh {	background-size:auto 60%;}.menu-item a, .menu-item a:link, .menu-item a:hover, .menu-item a:active,.menu-item a:visited {	color: #ffffff;		text-decoration:none;}.menu-item:hover, .menu-item:hover a, .menu-item a:hover { 	background-color:#ffffff;	color:#000000 !important;}.menu-item span {	width: 100%;	font-size:75%;	display:inline-block;}@media (max-width: 639px){	.device-item {		width:125px;	}	.item-image-text {		height: 65px;	}	.item-image {		background-size: auto 60%;	}	.item-text .label {		font-size: 80%;		line-height: 1.2;	}}"
	
	css = css.replace("url('", "url('${getResourcesUrl()}/")
	css += api_getLayoutCSS()
	
	if (settings?.customCSS) {
		css += settings.customCSS
	}
	return "<style>$css</style>"
}

private api_getLayoutCSS() {
	def layout = settings?.dashboardLayout ?: "1 Column"
	def css = ""
	if (layout?.toLowerCase()?.contains("condensed")) {
		css = "section{padding:4px 0 0 0;}.device-item{width: 98%;padding:0 0 0 0;margin:0 0 0 0;border:0;border-radius:0;}.item-image-text{height:auto;padding-left:4px;}.item-image{background-position:left center;width:20%;border:0;border-radius:0;}.item-status{line-height:1.4;}.item-text{text-align:left;}"
	}
	if (layout?.contains("2")) {
		css += ".device-item{width: 45%;margin: 0 2px;}"
	}
	if (layout?.contains("3")) {
		css += ".device-item{width: 30%; margin: 0 1px;}"
	}
	return css ?: ""
}

private logDebug(msg) {
	if (loggingTypeEnabled("debug")) {
		log.debug msg
	}
}

private logTrace(msg) {
	if (loggingTypeEnabled("trace")) {
		log.trace msg
	}
}

private logInfo(msg) {
	if (loggingTypeEnabled("info")) {
		log.info msg
	}
}

private loggingTypeEnabled(loggingType) {
	return (!settings?.logging || settings?.logging?.contains(loggingType))
}