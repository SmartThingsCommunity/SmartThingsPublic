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
 *  Samsung TV Service Manager
 *
 *  Author: SmartThings (Juan Risso)
 */
 
definition(
	name: "Samsung TV (Connect)",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Allows you to control your Samsung TV from the SmartThings app. Perform basic functions like power Off, source, volume, channels and other remote control functions.",
	category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Samsung/samsung-remote%402x.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Samsung/samsung-remote%403x.png",
    singleInstance: true
)

preferences {
	page(name:"samsungDiscovery", title:"Samsung TV Setup", content:"samsungDiscovery", refreshTimeout:5)
}

def getDeviceType() {
	return "urn:samsung.com:device:RemoteControlReceiver:1"
}

//PAGES
def samsungDiscovery()
{
	if(canInstallLabs())
	{	
		int samsungRefreshCount = !state.samsungRefreshCount ? 0 : state.samsungRefreshCount as int
		state.samsungRefreshCount = samsungRefreshCount + 1
		def refreshInterval = 3

		def options = samsungesDiscovered() ?: []

		def numFound = options.size() ?: 0

		if(!state.subscribe) {
			log.trace "subscribe to location"
			subscribe(location, null, locationHandler, [filterEvents:false])
			state.subscribe = true
		}

		//samsung discovery request every 5 //25 seconds
		if((samsungRefreshCount % 5) == 0) {
        	log.trace "Discovering..."
			discoversamsunges()
		}

		//setup.xml request every 3 seconds except on discoveries
		if(((samsungRefreshCount % 1) == 0) && ((samsungRefreshCount % 8) != 0)) {
            log.trace "Verifing..."
			verifysamsungPlayer()
		}

		return dynamicPage(name:"samsungDiscovery", title:"Discovery Started!", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
			section("Please wait while we discover your Samsung TV. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
				input "selectedsamsung", "enum", required:false, title:"Select Samsung TV (${numFound} found)", multiple:true, options:options
			}
		}
	}
	else
	{
		def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

		return dynamicPage(name:"samsungDiscovery", title:"Upgrade needed!", nextPage:"", install:true, uninstall: true) {
			section("Upgrade") {
				paragraph "$upgradeNeeded"
			}
		}
	}
}

def installed() {
	log.trace "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"
	unschedule()
	initialize()
}

def uninstalled() {
	def devices = getChildDevices()
	log.trace "deleting ${devices.size()} samsung"
	devices.each {
		deleteChildDevice(it.deviceNetworkId)
	}
}

def initialize() {
	// remove location subscription afterwards
	if (selectedsamsung) {
		addsamsung()
	}
    //Check every 5 minutes for IP change
	runEvery5Minutes("discoversamsunges")
}

//CHILD DEVICE METHODS
def addsamsung() {
	def players = getVerifiedsamsungPlayer()
    log.trace "Adding childs" 
	selectedsamsung.each { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			def newPlayer = players.find { (it.value.ip + ":" + it.value.port) == dni }
			log.trace "newPlayer = $newPlayer"
			log.trace "dni = $dni"
			d = addChildDevice("smartthings", "Samsung Smart TV", dni, newPlayer?.value.hub, [label:"${newPlayer?.value.name}"])
			log.trace "created ${d.displayName} with id $dni"

			d.setModel(newPlayer?.value.model)
			log.trace "setModel to ${newPlayer?.value.model}"
		} else {
			log.trace "found ${d.displayName} with id $dni already exists"
		}
	}
}

private tvAction(key,deviceNetworkId) {
    log.debug "Executing ${tvCommand}"
    
	def tvs = getVerifiedsamsungPlayer()
	def thetv = tvs.find { (it.value.ip + ":" + it.value.port) == deviceNetworkId }
    
    // Standard Connection Data
    def appString = "iphone..iapp.samsung"
    def appStringLength = appString.getBytes().size()

    def tvAppString = "iphone.UN60ES8000.iapp.samsung"
    def tvAppStringLength = tvAppString.getBytes().size()

    def remoteName = "SmartThings".encodeAsBase64().toString()
    def remoteNameLength = remoteName.getBytes().size()

    // Device Connection Data
    def ipAddress = convertHexToIP(thetv?.value.ip).encodeAsBase64().toString()
    def ipAddressHex = deviceNetworkId.substring(0,8)
    def ipAddressLength = ipAddress.getBytes().size()
    
    def macAddress = thetv?.value.mac.encodeAsBase64().toString()
    def macAddressLength = macAddress.getBytes().size()

    // The Authentication Message
    def authenticationMessage = "${(char)0x64}${(char)0x00}${(char)ipAddressLength}${(char)0x00}${ipAddress}${(char)macAddressLength}${(char)0x00}${macAddress}${(char)remoteNameLength}${(char)0x00}${remoteName}"
    def authenticationMessageLength = authenticationMessage.getBytes().size()
    
    def authenticationPacket = "${(char)0x00}${(char)appStringLength}${(char)0x00}${appString}${(char)authenticationMessageLength}${(char)0x00}${authenticationMessage}"

    // If our initial run, just send the authentication packet so the prompt appears on screen
    if (key == "AUTHENTICATE") {
	    sendHubCommand(new physicalgraph.device.HubAction(authenticationPacket, physicalgraph.device.Protocol.LAN, "${ipAddressHex}:D6D8"))
    } else {
        // Build the command we will send to the Samsung TV
        def command = "KEY_${key}".encodeAsBase64().toString()
        def commandLength = command.getBytes().size()

        def actionMessage = "${(char)0x00}${(char)0x00}${(char)0x00}${(char)commandLength}${(char)0x00}${command}"
        def actionMessageLength = actionMessage.getBytes().size()

        def actionPacket = "${(char)0x00}${(char)tvAppStringLength}${(char)0x00}${tvAppString}${(char)actionMessageLength}${(char)0x00}${actionMessage}"

        // Send both the authentication and action at the same time
        sendHubCommand(new physicalgraph.device.HubAction(authenticationPacket + actionPacket, physicalgraph.device.Protocol.LAN, "${ipAddressHex}:D6D8"))
    }
}

private discoversamsunges()
{
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery ${getDeviceType()}", physicalgraph.device.Protocol.LAN))
}


private verifysamsungPlayer() {
	def devices = getsamsungPlayer().findAll { it?.value?.verified != true }

	if(devices) {
		log.warn "UNVERIFIED PLAYERS!: $devices"
	}

	devices.each {
		verifysamsung((it?.value?.ip + ":" + it?.value?.port), it?.value?.ssdpPath)
	}
}

private verifysamsung(String deviceNetworkId, String devicessdpPath) {
	log.trace "dni: $deviceNetworkId, ssdpPath: $devicessdpPath"
	String ip = getHostAddress(deviceNetworkId)
	log.trace "ip:" + ip
	sendHubCommand(new physicalgraph.device.HubAction("""GET ${devicessdpPath} HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
}

Map samsungesDiscovered() {
	def vsamsunges = getVerifiedsamsungPlayer()
	def map = [:]
	vsamsunges.each {
		def value = "${it.value.name}"
		def key = it.value.ip + ":" + it.value.port
		map["${key}"] = value
	}
    log.trace "Devices discovered $map"
	map
}

def getsamsungPlayer()
{
	state.samsunges = state.samsunges ?: [:]
}

def getVerifiedsamsungPlayer()
{
	getsamsungPlayer().findAll{ it?.value?.verified == true }
}

def locationHandler(evt) {
	def description = evt.description
	def hub = evt?.hubId
	def parsedEvent = parseEventMessage(description)
	parsedEvent << ["hub":hub] 
    log.trace "${parsedEvent}"
    log.trace "${getDeviceType()} - ${parsedEvent.ssdpTerm}"
	if (parsedEvent?.ssdpTerm?.contains(getDeviceType()))
	{ //SSDP DISCOVERY EVENTS

		log.trace "TV found"
		def samsunges = getsamsungPlayer()

		if (!(samsunges."${parsedEvent.ssdpUSN.toString()}"))
		{ //samsung does not exist
        	log.trace "Adding Device to state..."
			samsunges << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
		}
		else
		{ // update the values

			log.trace "Device was already found in state..."

			def d = samsunges."${parsedEvent.ssdpUSN.toString()}"
			boolean deviceChangedValues = false

			if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
				d.ip = parsedEvent.ip
				d.port = parsedEvent.port
				deviceChangedValues = true
				log.trace "Device's port or ip changed..."
			}

			if (deviceChangedValues) {
				def children = getChildDevices()
				children.each {
					if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
						log.trace "updating dni for device ${it} with mac ${parsedEvent.mac}"
						it.setDeviceNetworkId((parsedEvent.ip + ":" + parsedEvent.port)) //could error if device with same dni already exists
					}
				}
			}
		}
	}
	else if (parsedEvent.headers && parsedEvent.body)
	{ // samsung RESPONSES
    	def deviceHeaders = parseLanMessage(description, false)
		def type = deviceHeaders.headers."content-type" 
		def body
		log.trace "REPONSE TYPE: $type"
		if (type?.contains("xml"))
		{ // description.xml response (application/xml)
			body = new XmlSlurper().parseText(deviceHeaders.body)
			log.debug body.device.deviceType.text()
			if (body?.device?.deviceType?.text().contains(getDeviceType()))
			{
				def samsunges = getsamsungPlayer()
				def player = samsunges.find {it?.key?.contains(body?.device?.UDN?.text())}
				if (player)
				{
					player.value << [name:body?.device?.friendlyName?.text(),model:body?.device?.modelName?.text(), serialNumber:body?.device?.serialNum?.text(), verified: true]
				}
				else
				{
					log.error "The xml file returned a device that didn't exist"
				}
			}
		}
		else if(type?.contains("json"))
		{ //(application/json)
			body = new groovy.json.JsonSlurper().parseText(bodyString)
			log.trace "GOT JSON $body"
		}

	}
	else {
		log.trace "TV not found..."
		//log.trace description
	}
}

private def parseEventMessage(String description) {
	def event = [:]
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('devicetype:')) {
			def valueString = part.split(":")[1].trim()
			event.devicetype = valueString
		}
		else if (part.startsWith('mac:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.mac = valueString
			}
		}
		else if (part.startsWith('networkAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ip = valueString
			}
		}
		else if (part.startsWith('deviceAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.port = valueString
			}
		}
		else if (part.startsWith('ssdpPath:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ssdpPath = valueString
			}
		}
		else if (part.startsWith('ssdpUSN:')) {
			part -= "ssdpUSN:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpUSN = valueString
			}
		}
		else if (part.startsWith('ssdpTerm:')) {
			part -= "ssdpTerm:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpTerm = valueString
			}
		}
		else if (part.startsWith('headers')) {
			part -= "headers:"
			def valueString = part.trim()
			if (valueString) {
				event.headers = valueString
			}
		}
		else if (part.startsWith('body')) {
			part -= "body:"
			def valueString = part.trim()
			if (valueString) {
				event.body = valueString
			}
		}
	}
	event
}

def parse(childDevice, description) {
	def parsedEvent = parseEventMessage(description)

	if (parsedEvent.headers && parsedEvent.body) {
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())
		log.trace "parse() - ${bodyString}"

		def body = new groovy.json.JsonSlurper().parseText(bodyString)
	} else {
		log.trace "parse - got something other than headers,body..."
		return []
	}
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress(d) {
	def parts = d.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

private Boolean canInstallLabs()
{
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}