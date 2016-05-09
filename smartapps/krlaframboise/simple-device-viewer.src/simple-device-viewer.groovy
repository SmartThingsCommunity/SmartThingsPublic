/**
 *  Simple Device Viewer v 1.8
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    https://community.smartthings.com/t/release-simple-device-viewer/42481?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.8 (05/08/2016)
 *      - Fixed bug with new feature that caused duplicates
 *        on the last event screen.
 *      - Added option to poll devices supporting poll command.
 *      - Added icons for Switches, Smoke/Carbon, Water,
 *        Alarms, Normal Battery, Normal Last Event
 *      - Added Light Device Type so that switches selected
 *        from that preference will appear on the Lights
 *        screen instead of the Switches screen.
 *
 *    1.7.1 (05/04/2016)
 *      - Added optional check for last event time that
 *        detects activity from hidden events.
 *      - Fixed bug with new feature that caused last event
 *        to display N/A when they fall outside the threshold.
 *
 *    1.6.3 (04/26/2016)
 *      - Fixed condensed view bug introduced in version 1.5.
 *      - Fixed duplicate notifications in message list bug.
 *      - Documented public methods for publication.
 *      - Accidentally removed modes option in one of the last
 *        test versions so reverting back to version before those
 *        changes.
 *
 *    1.5.1 (03/27/2016)
 *      - Added Icons for Contact Sensors, Motion Sensors, 
 *        Presence Sensors, Locks, and Switches.
 *      - Added Exclude Device options for Battery, Temp,
 *        and Last Event Notifications.
 *      - Bug fix for random N/A notifications.
 *
 *    1.4.4 (03/22/2016)
 *      - Added Temp, Battery, and Last Event notifications.
 *      - Added Condensed View option.
 *      - Created Custom Icon
 *      - Changed title formatting of capability screens.
 *      - Turned off unnecessary logging
 *      - Fixed bug caused by decimals in numeric fields.
 *      - Fixed bug caused by settings object that has 
 *        the property ID, but is not a device. (3/22)
 *
 *    1.3 (03/19/2016)
 *      - Added "Setup Thresholds" section that allows you
 *        to specify battery low level, temp high/low, and
 *        last event time.
 *      - Added threshold icons and value sorting for screens
 *        Temp, Battery, and Last Events.
 *      - Added "Other Settings" section that allows you to
 *        enable/disable icons and value sorting.
 *
 *    1.2 (03/17/2016)
 *      - Added page headings
 *      - Added ability to toggle switches from Switches screen.
 *      - Added "Turn Off All Switches" link to Switches page.
 *
 *    1.1 (03/17/2016)
 *      - Initial Release
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
		iconUrl: "https://raw.githubusercontent.com/krlaframboise/SmartThingsPublic/master/smartapps/krlaframboise/simple-device-viewer.src/simple-device-viewer-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/krlaframboise/SmartThingsPublic/master/smartapps/krlaframboise/simple-device-viewer.src/simple-device-viewer-icon-2x.png",
    iconX3Url: "https://raw.githubusercontent.com/krlaframboise/SmartThingsPublic/master/smartapps/krlaframboise/simple-device-viewer.src/simple-device-viewer-icon-3x.png")

 preferences {
	page(name:"mainPage")
  page(name:"capabilityPage")
	page(name:"lastEventPage")
	page(name:"toggleSwitchPage")
	page(name:"devicesPage")
	page(name:"thresholdsPage")
	page(name:"notificationsPage")
	page(name:"otherSettingsPage")
}

// Main Menu Page
def mainPage() {
	if (!state.capabilitySettings || (state.lastRequiredVersion != requiredVersion())) {
		storeCapabilitySettings()
	}
	dynamicPage(name:"mainPage", uninstall:true, install:true) {				
		section() {	
			if (getAllDevices().size() != 0) {
				state.lastCapabilitySetting = null
				href(
					name: "lastEventLink", 
					title: "All Devices - Last Event",
					description: "",
					page: "lastEventPage",
					required: false
				)
				getCapabilityPageLink(null)			
			}		
			getSelectedCapabilitySettings().each {
				if (devicesHaveCapability(getCapabilityName(it))) {
					getCapabilityPageLink(it)
				}
			}
		}
		section("Settings") {			
			href(
				name: "devicesLink", 
				title: "Choose Devices & Capabilities",
				description: "",
				page: "devicesPage", 
				required: false
			)			
			href(
				name: "thresholdsLink", 
				title: "Threshold Settings",
				description: "",
				page: "thresholdsPage", 
				required: false
			)
			href(
				name: "notificationsLink", 
				title: "Notification Settings",
				description: "",
				page: "notificationsPage", 
				required: false
			)
			href(
				name: "otherSettingsLink", 
				title: "Other Settings",
				description: "",
				page: "otherSettingsPage",
				required: false
			)
		}
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
				required: false
			input "sensors", "capability.sensor",
				title: "Which Sensors?",
				multiple: true,
				required: false			
			state.capabilitySettings.each {				
				input "${getPrefName(it)}Devices",
					"capability.${getPrefType(it)}",
					title: "Which ${getPluralName(it)}?",
					multiple: true,
					required: false
			}			
		}
		section ("Display Options") {
			paragraph "All the capabilities supported by the selected devices are shown on the main screen by default, but this field allows you to limit the list to specific capabilities." 
			input "selectedCapabilities", "enum",
				title: "Display Which Capabilities?",
				multiple: true,
				options: getCapabilitySettingNames(),
				required: false
		}
	}
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
				defaultValue: 25			
		}
		section("Temperature Thresholds") {
			input "lowTempThreshold", "number",
				title: "Enter Low Temperature:",
				required: false,
				defaultValue: 63
			input "highTempThreshold", "number",
				title: "Enter High Temperature:",
				required: false,
				defaultValue: 73			
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
		}
	}
}

// Page for SMS and Push notification settings
def notificationsPage() {
	dynamicPage(name:"notificationsPage") {
		section ("Notification Settings") {
			paragraph "When notifications are enabled, notifications will be sent when the device value goes above or below the threshold specified in the Threshold Settings."				
			
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
			input "lastEventByStateEnabled", "bool",
				title: "Advanced Last Event Check Enabled?\n(When enabled, an additional check is performed on devices that have no displayed events within the threshold.  The check can determine if there's been activity within the threshold, but it can't determine the time of that activity so if it finds activity, it sets the last event time to the threshold.)",
				defaultValue: false,
				required: false
			input "debugLogEnabled", "bool",
				title: "Debug Logging Enabled?",
				defaultValue: false,
				required: false
		}
		section ("Sorting") {
			input "batterySortByValue", "bool",
				title: "Sort by Battery Value?",
				defaultValue: false,
				required: false
			input "tempSortByValue", "bool",
				title: "Sort by Temperature Value?",
				defaultValue: false,
				required: false
			input "lastEventSortByValue", "bool",
				title: "Sort by Last Event Value?",
				defaultValue: false,
				required: false			
		}
		section ("Device Polling") {
			paragraph "If you enable the polling feature, the devices that support the Polling Capability will be polled at a regular interval."  
			input "pollingEnabled", "bool",
				title: "Poll Devices?",
				defaultValue: false,
				required: false
			input "pollingInterval", "number",
				title: "Polling Interval in Minutes:\n(Must be between 5 and ${6 * 24 * 60})",
				defaultValue: 5,
				range: "5..${6 * 24 * 60}",
				required: false		
		}
		section ("Scheduling") {
			paragraph "Leave this field empty unless you're using an external timer to turn on a switch at regular intervals.  If you select a switch, the application will check to see if notifications need to be sent when its turned on instead of using SmartThings scheduler to check every 5 minutes."

			input "timerSwitch", "capability.switch",
				title: "Select timer switch:",
				required: false
		}
	}
}

// Lists all devices and their last event times.
def lastEventPage() {
	dynamicPage(name:"lastEventPage") {		
		section ("Time Since Last Event") {
			getParagraphs(getAllDeviceLastEventListItems()?.unique())			
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
				toggleSwitch(device, device?.currentSwitch == "off" ? "on" : "off")
			}
			else {
				getDevicesByCapability("Switch").each {
					toggleSwitch(it, "off")
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
		paragraph "Turned ${device.displayName} ${newState.toUpperCase()}"
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
			section("All Selected Capabilities") {
				getParagraphs(getAllDevices().collect { 
					getDeviceAllCapabilitiesListItem(it) 
				})
			}
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

private getDevicesByCapability(name) {
	return getAllDevices()
		.findAll { it.hasCapability(name.toString()) }
		.sort() { it.displayName.toLowerCase() }		
}

private getDeviceAllCapabilitiesListItem(device) {
	def listItem = [
		sortValue: device.displayName
	]	
	getSelectedCapabilitySettings().each {
		if (device.hasCapability(getCapabilityName(it))) {
			listItem.status = (listItem.status ? "${listItem.status}, " : "").concat(getDeviceCapabilityStatusItem(device, it).status)
		}
	}
	listItem.title = getDeviceStatusTitle(device, listItem.status)
	return listItem
}

private getDeviceCapabilityListItems(cap) {
	def items = []
	getDevicesByCapability(getCapabilityName(cap))?.each { 
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

private getCapabilitySettingByName(name) {
	state.capabilitySettings.find { it.name == name }
}

private getAllDeviceLastEventListItems() {
	getAllDevices().collect {
		getDeviceLastEventListItem(it)		
	}
}

private getDeviceLastEventListItem(device) {
	def now = new Date().time
	def lastEventTime = getDeviceLastEventTime(device)
	
	def listItem = [
		value: lastEventTime ? now - lastEventTime : Long.MAX_VALUE,
		status: lastEventTime ? "${getTimeSinceLastEvent(now - lastEventTime)}" : "N/A"
	]
	
	listItem.title = getDeviceStatusTitle(device, listItem.status)
	listItem.sortValue = lastEventSortByValue ? listItem.value : device.displayName
	listItem.image = getLastEventImage(lastEventTime)
	return listItem
}

private getDeviceLastEventTime(device) {
	def lastEvents = device.events(max: 1)?.date?.time
	def lastEventTime
	def lastStateTime
	
	if (lastEvents && lastEvents.size() > 0) {
		lastEventTime = lastEvents[0]
	}	
	
	if (lastEventIsOld(lastEventTime) && settings.lastEventByStateEnabled) {		
		lastStateTime = getDeviceLastEventTimeByState(device)
	}
	return lastStateTime ? lastStateTime : lastEventTime
}

private getDeviceLastEventTimeByState(device) {
	def lastEventTime = null
	def lastEventCutoff = new Date((new Date().time - getLastEventThresholdMS())+1000)
	
	try {		
		def found = device.supportedAttributes.find { attr ->
			device.statesSince("$attr", lastEventCutoff, [max:1])?.size() > 0
		}
		if (found) {
			lastEventTime = lastEventCutoff.time
		}
	}
	catch (e) {
		log.warn "${device.displayName} Error: $e"
	}
	return lastEventTime
}

private getTimeSinceLastEvent(ms) {
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

private long msSecond() {
	return 1000
}

private long msMinute() {
	return msSecond() * 60
}

private long msHour() {
	return msMinute() * 60
}

private long msDay() {
	return msHour() * 24
}

private calculateTimeSince(ms, divisor) {
	return "${((float)(ms / divisor)).round()}"
}

private String getDeviceStatusTitle(device, status) {
	if (!status || status == "null") {
		status = "N/A"
	}
	return "${status?.toUpperCase()} -- ${device.displayName}"
}

private getDeviceCapabilityStatusItem(device, cap) {
	try {
		def item = [
			image: "",
			sortValue: device.displayName,
			value: device.currentValue(getAttributeName(cap)).toString()
		]
		item.status = item.value
		if ("${item.status}" != "null") {
		
			if (item.status == getActiveState(cap)) {
				item.status = "*${item.status}"
			}
			
			switch (cap.name) {
				case "Battery":			
					item.status = "${item.status}%"
					item.image = getBatteryImage(item.value)
					if (batterySortByValue) {
						item.sortValue = safeToInteger(item.value)
					}				
					break
				case "Temperature Measurement":
					item.status = "${item.status}°${location.temperatureScale}"
					item.image = getTemperatureImage(item.value)
					if (tempSortByValue) {
						item.sortValue = safeToInteger(item.value)
					}
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
		}
		else {
			item.status = "N/A"
		}
		return item
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

private int safeToInteger(val, defaultVal=0) {
	try {
		if (val) {
			return val.toFloat().round().toInteger()
		}
		else if (defaultVal != 0){
			return safeToInteger(defaultVal, 0)
		}
		else {
			return defaultVal
		}
	}
	catch (e) {
		logDebug "safeToInteger($val, $defaultVal) failed with error $e"
		return 0
	}
}

private getSelectedCapabilitySettings() {
	if (!selectedCapabilities) {
		return state.capabilitySettings
	}
	else {
		return state.capabilitySettings.findAll { it.name in selectedCapabilities }		
	}
}

private getCapabilitySettingNames() {
	state.capabilitySettings.collect { it.name }?.unique()
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

// private getAllDeviceCapabilities() {
	// def allCapabilities = getAllDevices()?.collect { it.capabilities }.flatten()
	// return allCapabilities.collect { it.toString() }.unique().sort()	
// }

private getAllDevices() {
	return settings.collectMany {k, device -> isDevice(device) ? device : []}?.flatten()?.unique { it.deviceNetworkId }
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

private String getLastEventImage(lastEventTime) {
	def status = lastEventIsOld(lastEventTime) ? "warning" : "ok"
	return getImagePath("${status}.png")
}

private boolean lastEventIsOld(lastEventTime) {	
	try {
		if (!lastEventTime) {
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

private long getLastEventThresholdMS() {
def threshold = lastEventThreshold ? lastEventThreshold : 7
	def unitMS
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
	def status = batteryIsLow(batteryLevel) ? "low" : "normal"
	return  getImagePath("${status}-battery.png")	
}

private boolean batteryIsLow(batteryLevel) {
	return isBelowThreshold(batteryLevel, lowBatteryThreshold, 25)
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

private boolean tempIsHigh(val) {
	return isAboveThreshold(val, highTempThreshold, 73)
}

private boolean tempIsLow(val) {
	return isBelowThreshold(val, lowTempThreshold, 63)
}

private String getImagePath(imageName) {
	if (iconsAreEnabled()) {
		return "https://raw.githubusercontent.com/krlaframboise/SmartThingsPublic/master/smartapps/krlaframboise/simple-device-viewer.src/$imageName"
	}
}

private boolean iconsAreEnabled() {
	return (iconsEnabled || iconsEnabled == null)
}

private boolean isAboveThreshold(val, threshold, int defaultThreshold) {
	return safeToInteger(val) > safeToInteger(threshold, defaultThreshold)	
}

private boolean isBelowThreshold(val, threshold, int defaultThreshold) {
	return safeToInteger(val) < safeToInteger(threshold,defaultThreshold)	
}

// Subscribes to events, starts schedules and initializes all settings.
def installed() {
	initialize()
}

// Resets subscriptions, scheduling and ensures all settings are initialized.
def updated() {
	unsubscribe()
	unschedule()
	initialize()
}

private initialize() {
	logDebug "Initializing"
	state.nextPollTime = null
	state.nextCheckTime = null

	if (!state.sentNotifications) {
		state.sentNotifications = []
	}
	
	storeCapabilitySettings()
	state.lastRequiredVersion = requiredVersion()
	
	if (timerSwitch) {
		subscribe(timerSwitch, "switch.on", timerSwitchEventHandler)
	}
	else {
		runEvery5Minutes(checkAllDeviceThresholds)
		if (canPollDevices()) {
			runEvery5Minutes(pollDevices)
		}
	}
}

private void storeCapabilitySettings() {
	state.capabilitySettings = []
	
	state.capabilitySettings << [
		name: "Switch",
		pluralName: "Switches",		
		activeState: "on",
		imageOnly: true
	]
	state.capabilitySettings << [
		name: "Light",
		prefName: "light",
		prefType: "switch",
		capabilityName: "Switch",
		attributeName: "switch",
		activeState: "on",		
		imageOnly: true
	]
	state.capabilitySettings << [
		name: "Motion Sensor", 
		prefType: "motionSensor",
		attributeName: "motion",
		activeState: "active",
		imageOnly: true
	]
	state.capabilitySettings << [
		name: "Contact Sensor",
		prefType: "contactSensor",
		attributeName: "contact",
		activeState: "open",
		imageOnly: true
	]
	state.capabilitySettings << [
		name: "Presence Sensor",
		prefType: "presenceSensor",
		attributeName: "presence",
		activeState: "present",
		imageOnly: true
	]
	state.capabilitySettings << [
		name: "Battery",
		pluralName: "Batteries"
	]
	state.capabilitySettings << [
		name: "Water Sensor",
		prefType: "waterSensor",
		attributeName: "water",
		activeState: "wet",
		imageOnly: true
	]
	state.capabilitySettings << [
		name: "Alarm",
		activeState: "off",
		imageOnly: true
	]
	state.capabilitySettings << [
		name: "Lock",
		activeState: "locked",
		imageOnly: true
	]
	state.capabilitySettings << [
		name: "Temperature Measurement",
		pluralName: "Temperature Sensors",
		prefType: "temperatureMeasurement",
		attributeName: "temperature"
	]
	state.capabilitySettings << [
		name: "Smoke Detector",
		prefType: "smokeDetector",
		attributeName: "smoke",
		activeState: "detected",
		imageOnly: true
	]
	state.capabilitySettings << [
		name: "Carbon Monoxide Detector",
		prefType: "carbonMonoxideDetector",
		attributeName: "carbonMonoxide",
		activeState: "detected",
		imageOnly: true
	]	
	state.capabilitySettings.sort { it.name }
}

// Used to genrate notifications when external timer is being used instead of relying on SmartThings scheduler. 
def timerSwitchEventHandler(evt) {
	if (canPollDevices()) {
		runIn(15000, pollDevices, [overwrite: false])
	}
	checkAllDeviceThresholds()
}

// Generates notifications if device attributes fall outside of specified thresholds and ensures that notifications are spaced at least 5 minutes apart.
def checkAllDeviceThresholds() {
	if (timeElapsed(state.nextCheckTime, true)) {
		log.debug "Checking All Device Thresholds"
		state.nextCheckTime = addMinutesToCurrentTime(4)		
		state.currentCheckSent = 0
		
		if (batteryNotificationsEnabled) {
			checkBatteries()
		}			
		if (temperatureNotificationsEnabled) {
			checkTemperatures()
		}			
		if (lastEventNotificationsEnabled) {
			checkLastEvents()
		}
	}
	else {
		log.debug "Skipped - Checking All Device Thresholds"
	}
}

def pollDevices() {
	if (canPollDevices()) {
		logDebug "Polling Devices"
		getDevicesByCapability("Polling")*.poll()
		state.nextPollTime = addMinutesToCurrentTime(safeToInteger(settings.pollingInterval)-1)
	}
}

private canPollDevices() {
	return settings.pollingEnabled &&
		safeToInteger(settings.pollingInterval) &&
		timeElapsed(state.nextPollTime, true)
}

private checkTemperatures() {
	logDebug "Checking Temperatures"
	def cap = getCapabilitySettingByName("Temperature Measurement")
	
	removeExcludedDevices(getDevicesByCapability("Temperature Measurement"), temperatureNotificationsExcluded)?.each {	
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

private checkBatteries() {
	logDebug "Checking Batteries"
	def cap = getCapabilitySettingByName("Battery")

	removeExcludedDevices(getDevicesByCapability("Battery"), batteryNotificationsExcluded)?.each {
		def item = getDeviceCapabilityStatusItem(it, cap)
		
		def message = batteryIsLow(item.value) ? "Low Battery Alert - ${getDeviceStatusTitle(it, item.status)}" : null
		
		handleDeviceNotification(it, message, "battery", batteryNotificationsRepeat)
	}
}

private checkLastEvents() {
	logDebug "Checking Last Events"
	removeExcludedDevices(getAllDevices(), lastEventNotificationsExcluded)?.each {
		def item = getDeviceLastEventListItem(it)
		def message = item.value > getLastEventThresholdMS() ? "Last Event Alert - ${getDeviceStatusTitle(it, item.status)}" : null
		
		handleDeviceNotification(it, message, "lastEvent", lastEventNotificationsRepeat)
	}
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
	def repeatMS = notificationRepeat ? (notificationRepeat * msHour()) : 0	
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

private long addMinutesToCurrentTime(minutes) {
	def currentTime = new Date().time
	return (currentTime + (minutes * msMinute()))	
}

private boolean timeElapsed(timeValue, nullResult=false) {
	if (timeValue != null) {
		def currentTime = new Date().time
		return (timeValue <= currentTime)
	} else {
		return nullResult
	}
}

private requiredVersion() {
	//v 1.8
	return 10800 
}

private logDebug(msg) {
	if (debugLogEnabled) {
		log.debug msg
	}
}

private logInfo(msg) {
	log.info msg
}