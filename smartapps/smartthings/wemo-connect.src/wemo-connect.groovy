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
 *  Wemo Service Manager
 *
 *  Author: superuser
 *  Date: 2013-09-06
 */
definition(
	name: "Wemo (Connect)",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Allows you to integrate your WeMo Switch and Wemo Motion sensor with SmartThings.",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/wemo.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/wemo@2x.png",
	singleInstance: true
)

preferences {
	page(name:"firstPage", title:"Wemo Device Setup", content:"firstPage")
}

private discoverAllWemoTypes()
{
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:Belkin:device:insight:1/urn:Belkin:device:controllee:1/urn:Belkin:device:sensor:1/urn:Belkin:device:lightswitch:1", physicalgraph.device.Protocol.LAN))
}

private getFriendlyName(String deviceNetworkId) {
	sendHubCommand(new physicalgraph.device.HubAction("""GET /setup.xml HTTP/1.1
HOST: ${deviceNetworkId}

""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}", [callback: "setupHandler"]))
}

private verifyDevices() {
	def switches = getWemoSwitches().findAll { it?.value?.verified != true }
	def motions = getWemoMotions().findAll { it?.value?.verified != true }
	def lightSwitches = getWemoLightSwitches().findAll { it?.value?.verified != true }
	def devices = switches + motions + lightSwitches
	devices.each {
		getFriendlyName((it.value.ip + ":" + it.value.port))
	}
}

void ssdpSubscribe() {
	subscribe(location, "ssdpTerm.urn:Belkin:device:insight:1", ssdpSwitchHandler)
	subscribe(location, "ssdpTerm.urn:Belkin:device:controllee:1", ssdpSwitchHandler)
	subscribe(location, "ssdpTerm.urn:Belkin:device:sensor:1", ssdpMotionHandler)
	subscribe(location, "ssdpTerm.urn:Belkin:device:lightswitch:1", ssdpLightSwitchHandler)
}

def firstPage()
{
	if(canInstallLabs())
	{
		int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
		state.refreshCount = refreshCount + 1
		def refreshInterval = 5

		log.debug "REFRESH COUNT :: ${refreshCount}"

		ssdpSubscribe()

		//ssdp request every 25 seconds
		if((refreshCount % 5) == 0) {
			discoverAllWemoTypes()
		}

		//setup.xml request every 5 seconds except on discoveries
		if(((refreshCount % 1) == 0) && ((refreshCount % 5) != 0)) {
			verifyDevices()
		}

		def switchesDiscovered = switchesDiscovered()
		def motionsDiscovered = motionsDiscovered()
		def lightSwitchesDiscovered = lightSwitchesDiscovered()

		return dynamicPage(name:"firstPage", title:"Discovery Started!", nextPage:"", refreshInterval: refreshInterval, install:true, uninstall: true) {
			section { paragraph title: "Note:", "This device has not been officially tested and certified to “Work with SmartThings”. You can connect it to your SmartThings home but performance may vary and we will not be able to provide support or assistance." }
			section("Select a device...") {
				input "selectedSwitches", "enum", required:false, title:"Select Wemo Switches \n(${switchesDiscovered.size() ?: 0} found)", multiple:true, options:switchesDiscovered
				input "selectedMotions", "enum", required:false, title:"Select Wemo Motions \n(${motionsDiscovered.size() ?: 0} found)", multiple:true, options:motionsDiscovered
				input "selectedLightSwitches", "enum", required:false, title:"Select Wemo Light Switches \n(${lightSwitchesDiscovered.size() ?: 0} found)", multiple:true, options:lightSwitchesDiscovered
			}
		}
	}
	else
	{
		def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

		return dynamicPage(name:"firstPage", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
			section("Upgrade") {
				paragraph "$upgradeNeeded"
			}
		}
	}
}

def devicesDiscovered() {
	def switches = getWemoSwitches()
	def motions = getWemoMotions()
	def lightSwitches = getWemoLightSwitches()
	def devices = switches + motions + lightSwitches
	devices?.collect{ [app.id, it.ssdpUSN].join('.') }
}

def switchesDiscovered() {
	def switches = getWemoSwitches().findAll { it?.value?.verified == true }
	def map = [:]
	switches.each {
		def value = it.value.name ?: "WeMo Switch ${it.value.ssdpUSN.split(':')[1][-3..-1]}"
		def key = it.value.mac
		map["${key}"] = value
	}
	map
}

def motionsDiscovered() {
	def motions = getWemoMotions().findAll { it?.value?.verified == true }
	def map = [:]
	motions.each {
		def value = it.value.name ?: "WeMo Motion ${it.value.ssdpUSN.split(':')[1][-3..-1]}"
		def key = it.value.mac
		map["${key}"] = value
	}
	map
}

def lightSwitchesDiscovered() {
	//def vmotions = switches.findAll { it?.verified == true }
	//log.trace "MOTIONS HERE: ${vmotions}"
	def lightSwitches = getWemoLightSwitches().findAll { it?.value?.verified == true }
	def map = [:]
	lightSwitches.each {
		def value = it.value.name ?: "WeMo Light Switch ${it.value.ssdpUSN.split(':')[1][-3..-1]}"
		def key = it.value.mac
		map["${key}"] = value
	}
	map
}

def getWemoSwitches()
{
	if (!state.switches) { state.switches = [:] }
	state.switches
}

def getWemoMotions()
{
	if (!state.motions) { state.motions = [:] }
	state.motions
}

def getWemoLightSwitches()
{
	if (!state.lightSwitches) { state.lightSwitches = [:] }
	state.lightSwitches
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}

def initialize() {
	unsubscribe()
	unschedule()

	ssdpSubscribe()

	if (selectedSwitches)
		addSwitches()

	if (selectedMotions)
		addMotions()

	if (selectedLightSwitches)
		addLightSwitches()

	runIn(5, "subscribeToDevices") //initial subscriptions delayed by 5 seconds
	runIn(10, "refreshDevices") //refresh devices, delayed by 10 seconds
	runEvery5Minutes("refresh")
}

def resubscribe() {
	log.debug "Resubscribe called, delegating to refresh()"
	refresh()
}

def refresh() {
	log.debug "refresh() called"
	doDeviceSync()
	refreshDevices()
}

def refreshDevices() {
	log.debug "refreshDevices() called"
	def devices = getAllChildDevices()
	devices.each { d ->
		log.debug "Calling refresh() on device: ${d.id}"
		d.refresh()
	}
}

def subscribeToDevices() {
	log.debug "subscribeToDevices() called"
	def devices = getAllChildDevices()
	devices.each { d ->
		d.subscribe()
	}
}

def addSwitches() {
	def switches = getWemoSwitches()

	selectedSwitches.each { dni ->
		def selectedSwitch = switches.find { it.value.mac == dni } ?: switches.find { "${it.value.ip}:${it.value.port}" == dni }
		def d
		if (selectedSwitch) {
			d = getChildDevices()?.find {
				it.deviceNetworkId == selectedSwitch.value.mac || it.device.getDataValue("mac") == selectedSwitch.value.mac
			}
			if (!d) {
				log.debug "Creating WeMo Switch with dni: ${selectedSwitch.value.mac}"
				d = addChildDevice("smartthings", "Wemo Switch", selectedSwitch.value.mac, selectedSwitch?.value.hub, [
					"label": selectedSwitch?.value?.name ?: "Wemo Switch",
					"data": [
						"mac": selectedSwitch.value.mac,
						"ip": selectedSwitch.value.ip,
						"port": selectedSwitch.value.port
					]
				])
				def ipvalue = convertHexToIP(selectedSwitch.value.ip)
				d.sendEvent(name: "currentIP", value: ipvalue, descriptionText: "IP is ${ipvalue}")
				log.debug "Created ${d.displayName} with id: ${d.id}, dni: ${d.deviceNetworkId}"
			} else {
				log.debug "found ${d.displayName} with id $dni already exists"
			}
		}
	}
}

def addMotions() {
	def motions = getWemoMotions()

	selectedMotions.each { dni ->
		def selectedMotion = motions.find { it.value.mac == dni } ?: motions.find { "${it.value.ip}:${it.value.port}" == dni }
		def d
		if (selectedMotion) {
			d = getChildDevices()?.find {
				it.deviceNetworkId == selectedMotion.value.mac || it.device.getDataValue("mac") == selectedMotion.value.mac
			}
			if (!d) {
				log.debug "Creating WeMo Motion with dni: ${selectedMotion.value.mac}"
				d = addChildDevice("smartthings", "Wemo Motion", selectedMotion.value.mac, selectedMotion?.value.hub, [
					"label": selectedMotion?.value?.name ?: "Wemo Motion",
					"data": [
						"mac": selectedMotion.value.mac,
						"ip": selectedMotion.value.ip,
						"port": selectedMotion.value.port
					]
				])
				def ipvalue = convertHexToIP(selectedMotion.value.ip)
				d.sendEvent(name: "currentIP", value: ipvalue, descriptionText: "IP is ${ipvalue}")
				log.debug "Created ${d.displayName} with id: ${d.id}, dni: ${d.deviceNetworkId}"
			} else {
				log.debug "found ${d.displayName} with id $dni already exists"
			}
		}
	}
}

def addLightSwitches() {
	def lightSwitches = getWemoLightSwitches()

	selectedLightSwitches.each { dni ->
		def selectedLightSwitch = lightSwitches.find { it.value.mac == dni } ?: lightSwitches.find { "${it.value.ip}:${it.value.port}" == dni }
		def d
		if (selectedLightSwitch) {
			d = getChildDevices()?.find {
				it.deviceNetworkId == selectedLightSwitch.value.mac || it.device.getDataValue("mac") == selectedLightSwitch.value.mac
			}
			if (!d) {
				log.debug "Creating WeMo Light Switch with dni: ${selectedLightSwitch.value.mac}"
				d = addChildDevice("smartthings", "Wemo Light Switch", selectedLightSwitch.value.mac, selectedLightSwitch?.value.hub, [
					"label": selectedLightSwitch?.value?.name ?: "Wemo Light Switch",
					"data": [
						"mac": selectedLightSwitch.value.mac,
						"ip": selectedLightSwitch.value.ip,
						"port": selectedLightSwitch.value.port
					]
				])
				def ipvalue = convertHexToIP(selectedLightSwitch.value.ip)
				d.sendEvent(name: "currentIP", value: ipvalue, descriptionText: "IP is ${ipvalue}")
				log.debug "created ${d.displayName} with id $dni"
			} else {
				log.debug "found ${d.displayName} with id $dni already exists"
			}
		}
	}
}

def ssdpSwitchHandler(evt) {
	def description = evt.description
	def hub = evt?.hubId
	def parsedEvent = parseDiscoveryMessage(description)
	parsedEvent << ["hub":hub]
	log.debug parsedEvent

	def switches = getWemoSwitches()
	if (!(switches."${parsedEvent.ssdpUSN.toString()}")) {
		//if it doesn't already exist
		switches << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
	} else {
		log.debug "Device was already found in state..."
		def d = switches."${parsedEvent.ssdpUSN.toString()}"
		boolean deviceChangedValues = false
		log.debug "$d.ip <==> $parsedEvent.ip"
		if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
			d.ip = parsedEvent.ip
			d.port = parsedEvent.port
			deviceChangedValues = true
			log.debug "Device's port or ip changed..."
			def child = getChildDevice(parsedEvent.mac)
			if (child) {
				child.subscribe(parsedEvent.ip, parsedEvent.port)
				child.poll()
			} else {
				log.debug "Device with mac $parsedEvent.mac not found"
			}
		}
	}
}

def ssdpMotionHandler(evt) {
	log.info("ssdpMotionHandler")
	def description = evt.description
	def hub = evt?.hubId
	def parsedEvent = parseDiscoveryMessage(description)
	parsedEvent << ["hub":hub]
	log.debug parsedEvent

	def motions = getWemoMotions()
	if (!(motions."${parsedEvent.ssdpUSN.toString()}")) {
		//if it doesn't already exist
		motions << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
	} else { // just update the values
		log.debug "Device was already found in state..."

		def d = motions."${parsedEvent.ssdpUSN.toString()}"
		boolean deviceChangedValues = false

		if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
			d.ip = parsedEvent.ip
			d.port = parsedEvent.port
			deviceChangedValues = true
			log.debug "Device's port or ip changed..."
		}

		if (deviceChangedValues) {
			def children = getChildDevices()
			log.debug "Found children ${children}"
			children.each {
				if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
					log.debug "updating ip and port, and resubscribing, for device ${it} with mac ${parsedEvent.mac}"
					it.subscribe(parsedEvent.ip, parsedEvent.port)
				}
			}
		}
	}
}

def ssdpLightSwitchHandler(evt) {
	log.info("ssdpLightSwitchHandler")
	def description = evt.description
	def hub = evt?.hubId
	def parsedEvent = parseDiscoveryMessage(description)
	parsedEvent << ["hub":hub]
	log.debug parsedEvent

	def lightSwitches = getWemoLightSwitches()

	if (!(lightSwitches."${parsedEvent.ssdpUSN.toString()}")) {
		//if it doesn't already exist
		lightSwitches << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
	} else {
		log.debug "Device was already found in state..."

		def d = lightSwitches."${parsedEvent.ssdpUSN.toString()}"
		boolean deviceChangedValues = false

		if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
			d.ip = parsedEvent.ip
			d.port = parsedEvent.port
			deviceChangedValues = true
			log.debug "Device's port or ip changed..."
			def child = getChildDevice(parsedEvent.mac)
			if (child) {
				log.debug "updating ip and port, and resubscribing, for device with mac ${parsedEvent.mac}"
				child.subscribe(parsedEvent.ip, parsedEvent.port)
			} else {
				log.debug "Device with mac $parsedEvent.mac not found"
			}
		}
	}
}

void setupHandler(hubResponse) {
	String contentType = hubResponse?.headers['Content-Type']
	if (contentType != null && contentType == 'text/xml') {
		def body = hubResponse.xml
		def wemoDevices = []
		String deviceType = body?.device?.deviceType?.text() ?: ""
		if (deviceType.startsWith("urn:Belkin:device:controllee:1") || deviceType.startsWith("urn:Belkin:device:insight:1")) {
			wemoDevices = getWemoSwitches()
		} else if (deviceType.startsWith("urn:Belkin:device:sensor")) {
			wemoDevices = getWemoMotions()
		} else if (deviceType.startsWith("urn:Belkin:device:lightswitch")) {
			wemoDevices = getWemoLightSwitches()
		}

		def wemoDevice = wemoDevices.find {it?.key?.contains(body?.device?.UDN?.text())}
		if (wemoDevice) {
			wemoDevice.value << [name:body?.device?.friendlyName?.text(), verified: true]
		} else {
			log.error "/setup.xml returned a wemo device that didn't exist"
		}
	}
}

@Deprecated
def locationHandler(evt) {
	def description = evt.description
	def hub = evt?.hubId
	def parsedEvent = parseDiscoveryMessage(description)
	parsedEvent << ["hub":hub]
	log.debug parsedEvent

	if (parsedEvent?.ssdpTerm?.contains("Belkin:device:controllee") || parsedEvent?.ssdpTerm?.contains("Belkin:device:insight")) {
		def switches = getWemoSwitches()
		if (!(switches."${parsedEvent.ssdpUSN.toString()}")) {
			//if it doesn't already exist
			switches << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
		} else {
			log.debug "Device was already found in state..."
			def d = switches."${parsedEvent.ssdpUSN.toString()}"
			boolean deviceChangedValues = false
			log.debug "$d.ip <==> $parsedEvent.ip"
			if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
				d.ip = parsedEvent.ip
				d.port = parsedEvent.port
				deviceChangedValues = true
				log.debug "Device's port or ip changed..."
				def child = getChildDevice(parsedEvent.mac)
				if (child) {
					child.subscribe(parsedEvent.ip, parsedEvent.port)
					child.poll()
				} else {
					log.debug "Device with mac $parsedEvent.mac not found"
				}
			}
		}
	}
	else if (parsedEvent?.ssdpTerm?.contains("Belkin:device:sensor")) {
		def motions = getWemoMotions()
		if (!(motions."${parsedEvent.ssdpUSN.toString()}")) {
			//if it doesn't already exist
			motions << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
		} else { // just update the values
			log.debug "Device was already found in state..."

			def d = motions."${parsedEvent.ssdpUSN.toString()}"
			boolean deviceChangedValues = false

			if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
				d.ip = parsedEvent.ip
				d.port = parsedEvent.port
				deviceChangedValues = true
				log.debug "Device's port or ip changed..."
			}

			if (deviceChangedValues) {
				def children = getChildDevices()
				log.debug "Found children ${children}"
				children.each {
					if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
						log.debug "updating ip and port, and resubscribing, for device ${it} with mac ${parsedEvent.mac}"
						it.subscribe(parsedEvent.ip, parsedEvent.port)
					}
				}
			}
		}

	}
	else if (parsedEvent?.ssdpTerm?.contains("Belkin:device:lightswitch")) {

		def lightSwitches = getWemoLightSwitches()

		if (!(lightSwitches."${parsedEvent.ssdpUSN.toString()}"))
		{ //if it doesn't already exist
			lightSwitches << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
		} else {
			log.debug "Device was already found in state..."

			def d = lightSwitches."${parsedEvent.ssdpUSN.toString()}"
			boolean deviceChangedValues = false

			if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
				d.ip = parsedEvent.ip
				d.port = parsedEvent.port
				deviceChangedValues = true
				log.debug "Device's port or ip changed..."
				def child = getChildDevice(parsedEvent.mac)
				if (child) {
					log.debug "updating ip and port, and resubscribing, for device with mac ${parsedEvent.mac}"
					child.subscribe(parsedEvent.ip, parsedEvent.port)
				} else {
					log.debug "Device with mac $parsedEvent.mac not found"
				}
			}
		}
	}
	else if (parsedEvent.headers && parsedEvent.body) {
		String headerString = new String(parsedEvent.headers.decodeBase64())?.toLowerCase()
		if (headerString != null && (headerString.contains('text/xml') || headerString.contains('application/xml'))) {
			def body = parseXmlBody(parsedEvent.body)
			if (body?.device?.deviceType?.text().startsWith("urn:Belkin:device:controllee:1"))
			{
				def switches = getWemoSwitches()
				def wemoSwitch = switches.find {it?.key?.contains(body?.device?.UDN?.text())}
				if (wemoSwitch)
				{
					wemoSwitch.value << [name:body?.device?.friendlyName?.text(), verified: true]
				}
				else
				{
					log.error "/setup.xml returned a wemo device that didn't exist"
				}
			}

			if (body?.device?.deviceType?.text().startsWith("urn:Belkin:device:insight:1"))
			{
				def switches = getWemoSwitches()
				def wemoSwitch = switches.find {it?.key?.contains(body?.device?.UDN?.text())}
				if (wemoSwitch)
				{
					wemoSwitch.value << [name:body?.device?.friendlyName?.text(), verified: true]
				}
				else
				{
					log.error "/setup.xml returned a wemo device that didn't exist"
				}
			}

			if (body?.device?.deviceType?.text().startsWith("urn:Belkin:device:sensor")) //?:1
			{
				def motions = getWemoMotions()
				def wemoMotion = motions.find {it?.key?.contains(body?.device?.UDN?.text())}
				if (wemoMotion)
				{
					wemoMotion.value << [name:body?.device?.friendlyName?.text(), verified: true]
				}
				else
				{
					log.error "/setup.xml returned a wemo device that didn't exist"
				}
			}

			if (body?.device?.deviceType?.text().startsWith("urn:Belkin:device:lightswitch")) //?:1
			{
				def lightSwitches = getWemoLightSwitches()
				def wemoLightSwitch = lightSwitches.find {it?.key?.contains(body?.device?.UDN?.text())}
				if (wemoLightSwitch)
				{
					wemoLightSwitch.value << [name:body?.device?.friendlyName?.text(), verified: true]
				}
				else
				{
					log.error "/setup.xml returned a wemo device that didn't exist"
				}
			}
		}
	}
}

@Deprecated
private def parseXmlBody(def body) {
	def decodedBytes = body.decodeBase64()
	def bodyString
	try {
		bodyString = new String(decodedBytes)
	} catch (Exception e) {
		// Keep this log for debugging StringIndexOutOfBoundsException issue
		log.error("Exception decoding bytes in sonos connect: ${decodedBytes}")
		throw e
	}
	return new XmlSlurper().parseText(bodyString)
}

private def parseDiscoveryMessage(String description) {
	def event = [:]
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('devicetype:')) {
			part -= "devicetype:"
			event.devicetype = part.trim()
		}
		else if (part.startsWith('mac:')) {
			part -= "mac:"
			event.mac = part.trim()
		}
		else if (part.startsWith('networkAddress:')) {
			part -= "networkAddress:"
			event.ip = part.trim()
		}
		else if (part.startsWith('deviceAddress:')) {
			part -= "deviceAddress:"
			event.port = part.trim()
		}
		else if (part.startsWith('ssdpPath:')) {
			part -= "ssdpPath:"
			event.ssdpPath = part.trim()
		}
		else if (part.startsWith('ssdpUSN:')) {
			part -= "ssdpUSN:"
			event.ssdpUSN = part.trim()
		}
		else if (part.startsWith('ssdpTerm:')) {
			part -= "ssdpTerm:"
			event.ssdpTerm = part.trim()
		}
		else if (part.startsWith('headers')) {
			part -= "headers:"
			event.headers = part.trim()
		}
		else if (part.startsWith('body')) {
			part -= "body:"
			event.body = part.trim()
		}
	}
	event
}

def doDeviceSync(){
	log.debug "Doing Device Sync!"
	discoverAllWemoTypes()
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private Boolean canInstallLabs() {
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware) {
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions() {
	return location.hubs*.firmwareVersionString.findAll { it }
}
