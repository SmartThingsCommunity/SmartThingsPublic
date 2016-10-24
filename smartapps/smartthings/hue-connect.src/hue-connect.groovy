/**
 *  Hue Service Manager
 *
 *  Author: Juan Risso (juan@smartthings.com)
 *
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

definition(
	name: "Hue (Connect)",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Allows you to connect your Philips Hue lights with SmartThings and control them from your Things area or Dashboard in the SmartThings Mobile app. Adjust colors by going to the Thing detail screen for your Hue lights (tap the gear on Hue tiles).\n\nPlease update your Hue Bridge first, outside of the SmartThings app, using the Philips Hue app.",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png",
	singleInstance: true
)

preferences {
	page(name:"mainPage", title:"Hue Device Setup", content:"mainPage", refreshTimeout:5)
	page(name:"bridgeDiscovery", title:"Hue Bridge Discovery", content:"bridgeDiscovery", refreshTimeout:5)
	page(name:"bridgeDiscoveryFailed", title:"Bridge Discovery Failed", content:"bridgeDiscoveryFailed", refreshTimeout:0)
	page(name:"bridgeBtnPush", title:"Linking with your Hue", content:"bridgeLinking", refreshTimeout:5)
	page(name:"bulbDiscovery", title:"Hue Device Setup", content:"bulbDiscovery", refreshTimeout:5)
}

def mainPage() {
	def bridges = bridgesDiscovered()

	if (state.refreshUsernameNeeded) {
		return bridgeLinking()
	} else if (state.username && bridges) {
		return bulbDiscovery()
	} else {
		return bridgeDiscovery()
	}
}

def bridgeDiscovery(params=[:])
{
	def bridges = bridgesDiscovered()
	int bridgeRefreshCount = !state.bridgeRefreshCount ? 0 : state.bridgeRefreshCount as int
	state.bridgeRefreshCount = bridgeRefreshCount + 1
	def refreshInterval = 3

	def options = bridges ?: []
	def numFound = options.size() ?: 0
	if (numFound == 0) {
		if (state.bridgeRefreshCount == 25) {
			log.trace "Cleaning old bridges memory"
			state.bridges = [:]
			app.updateSetting("selectedHue", "")
		} else if (state.bridgeRefreshCount > 100) {
			// five minutes have passed, give up
			// there seems to be a problem going back from discovey failed page in some instances (compared to pressing next)
			// however it is probably a SmartThings settings issue
			state.bridges = [:]
			app.updateSetting("selectedHue", "")
			state.bridgeRefreshCount = 0
			return bridgeDiscoveryFailed()
		}
	}

	ssdpSubscribe()

	//bridge discovery request every 15 //25 seconds
	if((bridgeRefreshCount % 5) == 0) {
		discoverBridges()
	}

	//setup.xml request every 3 seconds except on discoveries
	if(((bridgeRefreshCount % 3) == 0) && ((bridgeRefreshCount % 5) != 0)) {
		verifyHueBridges()
	}

	return dynamicPage(name:"bridgeDiscovery", title:"Discovery Started!", nextPage:"bridgeBtnPush", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Hue Bridge. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedHue", "enum", required:false, title:"Select Hue Bridge (${numFound} found)", multiple:false, options:options, submitOnChange: true
		}
	}
}

def bridgeDiscoveryFailed() {
	return dynamicPage(name:"bridgeDiscoveryFailed", title: "Bridge Discovery Failed", nextPage: "bridgeDiscovery") {
		section("Failed to discover any Hue Bridges. Please confirm that the Hue Bridge is connected to the same network as your SmartThings Hub, and that it has power.") {
		}
	}
}

def bridgeLinking() {
	int linkRefreshcount = !state.linkRefreshcount ? 0 : state.linkRefreshcount as int
	state.linkRefreshcount = linkRefreshcount + 1
	def refreshInterval = 3

	def nextPage = ""
	def title = "Linking with your Hue"
	def paragraphText
	if (selectedHue) {
		if (state.refreshUsernameNeeded) {
			paragraphText = "The current Hue username is invalid.\n\nPlease press the button on your Hue Bridge to re-link. "
		} else {
			paragraphText = "Press the button on your Hue Bridge to setup a link. "
		}
	} else {
		paragraphText = "You haven't selected a Hue Bridge, please Press \"Done\" and select one before clicking next."
	}
	if (state.username) { //if discovery worked
		if (state.refreshUsernameNeeded) {
			state.refreshUsernameNeeded = false
			// Issue one poll with new username to cancel local polling with old username
			poll()
		}
		nextPage = "bulbDiscovery"
		title = "Success!"
		paragraphText = "Linking to your hub was a success! Please click 'Next'!"
	}

	if((linkRefreshcount % 2) == 0 && !state.username) {
		sendDeveloperReq()
	}

	return dynamicPage(name:"bridgeBtnPush", title:title, nextPage:nextPage, refreshInterval:refreshInterval) {
		section("") {
			paragraph """${paragraphText}"""
		}
	}
}

def bulbDiscovery() {
	int bulbRefreshCount = !state.bulbRefreshCount ? 0 : state.bulbRefreshCount as int
	state.bulbRefreshCount = bulbRefreshCount + 1
	def refreshInterval = 3
	state.inBulbDiscovery = true
	def bridge = null

	state.bridgeRefreshCount = 0
	def allLightsFound = bulbsDiscovered() ?: [:]

	// List lights currently not added to the user (editable)
	def newLights = allLightsFound.findAll {getChildDevice(it.key) == null} ?: [:]
	newLights = newLights.sort {it.value.toLowerCase()}

	// List lights already added to the user (not editable)
	def existingLights = allLightsFound.findAll {getChildDevice(it.key) != null} ?: [:]
	existingLights = existingLights.sort {it.value.toLowerCase()}

	def numFound = newLights.size() ?: 0
	if (numFound == 0)
		app.updateSetting("selectedBulbs", "")

	if((bulbRefreshCount % 5) == 0) {
		discoverHueBulbs()
	}
	def selectedBridge = state.bridges.find { key, value -> value?.serialNumber?.equalsIgnoreCase(selectedHue) }
	def title = selectedBridge?.value?.name ?: "Find bridges"

	// List of all lights previously added shown to user
	def existingLightsDescription = ""
	if (existingLights) {
		existingLights.each {
			if (existingLightsDescription.isEmpty()) {
				existingLightsDescription += it.value
			} else {
				 existingLightsDescription += ", ${it.value}"
			}
		}
	}

	return dynamicPage(name:"bulbDiscovery", title:"Light Discovery Started!", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
		section("Please wait while we discover your Hue Lights. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedBulbs", "enum", required:false, title:"Select Hue Lights to add (${numFound} found)", multiple:true, submitOnChange: true, options:newLights
			paragraph title: "Previously added Hue Lights (${existingLights.size()} added)", existingLightsDescription
		}
		section {
			href "bridgeDiscovery", title: title, description: "", state: selectedHue ? "complete" : "incomplete", params: [override: true]
		}
	}
}

private discoverBridges() {
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:basic:1", physicalgraph.device.Protocol.LAN))
}

void ssdpSubscribe() {
	subscribe(location, "ssdpTerm.urn:schemas-upnp-org:device:basic:1", ssdpBridgeHandler)
}

private sendDeveloperReq() {
	def token = app.id
	def host = getBridgeIP()
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "POST",
		path: "/api",
		headers: [
			HOST: host
		],
		body: [devicetype: "$token-0"]], "${selectedHue}", [callback: "usernameHandler"]))
}

private discoverHueBulbs() {
	def host = getBridgeIP()
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/api/${state.username}/lights",
		headers: [
			HOST: host
		]], "${selectedHue}", [callback: "lightsHandler"]))
}

private verifyHueBridge(String deviceNetworkId, String host) {
	log.trace "Verify Hue Bridge $deviceNetworkId"
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/description.xml",
		headers: [
			HOST: host
		]], deviceNetworkId, [callback: "bridgeDescriptionHandler"]))
}

private verifyHueBridges() {
	def devices = getHueBridges().findAll { it?.value?.verified != true }
	devices.each {
		def ip = convertHexToIP(it.value.networkAddress)
		def port = convertHexToInt(it.value.deviceAddress)
		verifyHueBridge("${it.value.mac}", (ip + ":" + port))
	}
}

Map bridgesDiscovered() {
	def vbridges = getVerifiedHueBridges()
	def map = [:]
	vbridges.each {
		def value = "${it.value.name}"
		def key = "${it.value.mac}"
		map["${key}"] = value
	}
	map
}

Map bulbsDiscovered() {
	def bulbs =  getHueBulbs()
	def bulbmap = [:]
	if (bulbs instanceof java.util.Map) {
		bulbs.each {
			def value = "${it.value.name}"
			def key = app.id +"/"+ it.value.id
			bulbmap["${key}"] = value
		}
	} else { //backwards compatable
		bulbs.each {
			def value = "${it.name}"
			def key = app.id +"/"+ it.id
			logg += "$value - $key, "
			bulbmap["${key}"] = value
		}
	}
	return bulbmap
}

Map getHueBulbs() {
	state.bulbs = state.bulbs ?: [:]
}

def getHueBridges() {
	state.bridges = state.bridges ?: [:]
}

def getVerifiedHueBridges() {
	getHueBridges().findAll{ it?.value?.verified == true }
}

def installed() {
	log.trace "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	log.debug "Initializing"
	unsubscribe(bridge)
	state.inBulbDiscovery = false
	state.bridgeRefreshCount = 0
	state.bulbRefreshCount = 0
	state.updating = false
	setupDeviceWatch()
	if (selectedHue) {
		addBridge()
		addBulbs()
		doDeviceSync()
		runEvery5Minutes("doDeviceSync")
	}
}

def manualRefresh() {
	checkBridgeStatus()
	state.updating = false
	poll()
}

def uninstalled(){
	// Remove bridgedevice connection to allow uninstall of smartapp even though bridge is listed
	// as user of smartapp
	app.updateSetting("bridgeDevice", null)
	state.bridges = [:]
	state.username = null
}

private setupDeviceWatch() {
	def hub = location.hubs[0]
	// Make sure that all child devices are enrolled in device watch
	getChildDevices().each {
		it.sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"LAN\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${hub?.hub?.hardwareID}\"}")
	}
}

private upgradeDeviceType(device, newHueType) {
	def deviceType = getDeviceType(newHueType)

	// Automatically change users Hue bulbs to correct device types
	if (deviceType && !(device?.typeName?.equalsIgnoreCase(deviceType))) {
		log.debug "Update device type: \"$device.label\" ${device?.typeName}->$deviceType"
		device.setDeviceType(deviceType)
	}
}

private getDeviceType(hueType) {
	// Determine ST device type based on Hue classification of light
	if (hueType?.equalsIgnoreCase("Dimmable light"))
		return "Hue Lux Bulb"
	else if (hueType?.equalsIgnoreCase("Extended Color Light"))
		return "Hue Bulb"
	else if (hueType?.equalsIgnoreCase("Color Light"))
		return "Hue Bloom"
	else if (hueType?.equalsIgnoreCase("Color Temperature Light"))
		return "Hue White Ambiance Bulb"
	else
		return null
}

private addChildBulb(dni, hueType, name, hub, update=false, device = null) {
	def deviceType = getDeviceType(hueType)

	if (deviceType) {
		return addChildDevice("smartthings", deviceType, dni, hub, ["label": name])
	} else {
		log.warn "Device type $hueType not supported"
		return null
	}
}

def addBulbs() {
	def bulbs = getHueBulbs()
	selectedBulbs?.each { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			def newHueBulb
			if (bulbs instanceof java.util.Map) {
				newHueBulb = bulbs.find { (app.id + "/" + it.value.id) == dni }
				if (newHueBulb != null) {
					d = addChildBulb(dni, newHueBulb?.value?.type, newHueBulb?.value?.name, newHueBulb?.value?.hub)
					if (d) {
						log.debug "created ${d.displayName} with id $dni"
						d.completedSetup = true
					}
				} else {
					log.debug "$dni in not longer paired to the Hue Bridge or ID changed"
				}
			} else {
			 	//backwards compatable
				newHueBulb = bulbs.find { (app.id + "/" + it.id) == dni }
				d = addChildBulb(dni, "Extended Color Light", newHueBulb?.value?.name, newHueBulb?.value?.hub)
				d?.completedSetup = true
				d?.refresh()
			}
		} else {
			log.debug "found ${d.displayName} with id $dni already exists, type: '$d.typeName'"
			if (bulbs instanceof java.util.Map) {
				// Update device type if incorrect
				def newHueBulb = bulbs.find { (app.id + "/" + it.value.id) == dni }
				upgradeDeviceType(d, newHueBulb?.value?.type)
			}
		}
	}
}

def addBridge() {
	def vbridges = getVerifiedHueBridges()
	def vbridge = vbridges.find {"${it.value.mac}" == selectedHue}

	if(vbridge) {
		def d = getChildDevice(selectedHue)
		if(!d) {
	 		// compatibility with old devices
			def newbridge = true
			childDevices.each {
				if (it.getDeviceDataByName("mac")) {
					def newDNI = "${it.getDeviceDataByName("mac")}"
					if (newDNI != it.deviceNetworkId) {
						def oldDNI = it.deviceNetworkId
						log.debug "updating dni for device ${it} with $newDNI - previous DNI = ${it.deviceNetworkId}"
						it.setDeviceNetworkId("${newDNI}")
						if (oldDNI == selectedHue) {
							app.updateSetting("selectedHue", newDNI)
						}
						newbridge = false
					}
				}
			}
			if (newbridge) {
				// Hue uses last 6 digits of MAC address as ID number, this number is shown on the bottom of the bridge
				def idNumber = getBridgeIdNumber(selectedHue)
				d = addChildDevice("smartthings", "Hue Bridge", selectedHue, vbridge.value.hub, ["label": "Hue Bridge ($idNumber)"])
				if (d) {
					// Associate smartapp to bridge so user will be warned if trying to delete bridge
					app.updateSetting("bridgeDevice", [type: "device.hueBridge", value: d.id])

					d.completedSetup = true
					log.debug "created ${d.displayName} with id ${d.deviceNetworkId}"
					def childDevice = getChildDevice(d.deviceNetworkId)
					childDevice?.sendEvent(name: "status", value: "Online")
					childDevice?.sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
					updateBridgeStatus(childDevice)

					childDevice?.sendEvent(name: "idNumber", value: idNumber)
					if (vbridge.value.ip && vbridge.value.port) {
						if (vbridge.value.ip.contains(".")) {
							childDevice.sendEvent(name: "networkAddress", value: vbridge.value.ip + ":" +  vbridge.value.port)
							childDevice.updateDataValue("networkAddress", vbridge.value.ip + ":" +  vbridge.value.port)
						} else {
							childDevice.sendEvent(name: "networkAddress", value: convertHexToIP(vbridge.value.ip) + ":" +  convertHexToInt(vbridge.value.port))
							childDevice.updateDataValue("networkAddress", convertHexToIP(vbridge.value.ip) + ":" +  convertHexToInt(vbridge.value.port))
						}
					} else {
						childDevice.sendEvent(name: "networkAddress", value: convertHexToIP(vbridge.value.networkAddress) + ":" +  convertHexToInt(vbridge.value.deviceAddress))
						childDevice.updateDataValue("networkAddress", convertHexToIP(vbridge.value.networkAddress) + ":" +  convertHexToInt(vbridge.value.deviceAddress))
					}
				} else {
					log.error "Failed to create Hue Bridge device"
				}
			}
		} else {
			log.debug "found ${d.displayName} with id $selectedHue already exists"
		}
	}
}

def ssdpBridgeHandler(evt) {
	def description = evt.description
	log.trace "Location: $description"

	def hub = evt?.hubId
	def parsedEvent = parseLanMessage(description)
	parsedEvent << ["hub":hub]

	def bridges = getHueBridges()
	log.trace bridges.toString()
	if (!(bridges."${parsedEvent.ssdpUSN.toString()}")) {
		//bridge does not exist
		log.trace "Adding bridge ${parsedEvent.ssdpUSN}"
		bridges << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
	} else {
		// update the values
		def ip = convertHexToIP(parsedEvent.networkAddress)
		def port = convertHexToInt(parsedEvent.deviceAddress)
		def host = ip + ":" + port
		log.debug "Device ($parsedEvent.mac) was already found in state with ip = $host."
		def dstate = bridges."${parsedEvent.ssdpUSN.toString()}"
		def dniReceived = "${parsedEvent.mac}"
		def currentDni = dstate.mac
		def d = getChildDevice(dniReceived)
		def networkAddress = null
		if (!d) {
			// There might be a mismatch between bridge DNI and the actual bridge mac address, correct that
			log.debug "Bridge with $dniReceived not found"
			def bridge = childDevices.find { it.deviceNetworkId == currentDni }
			if (bridge != null) {
				log.warn "Bridge is set to ${bridge.deviceNetworkId}, updating to $dniReceived"
				bridge.setDeviceNetworkId("${dniReceived}")
				dstate.mac = dniReceived
				// Check to see if selectedHue is a valid bridge, otherwise update it
				def isSelectedValid = bridges?.find {it.value?.mac == selectedHue}
				if (isSelectedValid == null) {
					log.warn "Correcting selectedHue in state"
					app.updateSetting("selectedHue", dniReceived)
				}
				doDeviceSync()
			}
		} else {
			updateBridgeStatus(d)
			if (d.getDeviceDataByName("networkAddress")) {
				networkAddress = d.getDeviceDataByName("networkAddress")
			} else {
				networkAddress = d.latestState('networkAddress').stringValue
			}
			log.trace "Host: $host - $networkAddress"
			if (host != networkAddress) {
				log.debug "Device's port or ip changed for device $d..."
				dstate.ip = ip
				dstate.port = port
				dstate.name = "Philips hue ($ip)"
				d.sendEvent(name:"networkAddress", value: host)
				d.updateDataValue("networkAddress", host)
			}
			if (dstate.mac != dniReceived) {
				log.warn "Correcting bridge mac address in state"
				dstate.mac = dniReceived
			}
			if (selectedHue != dniReceived) {
				// Check to see if selectedHue is a valid bridge, otherwise update it
				def isSelectedValid = bridges?.find {it.value?.mac == selectedHue}
				if (isSelectedValid == null) {
					log.warn "Correcting selectedHue in state"
					app.updateSetting("selectedHue", dniReceived)
				}
			}
		}
	}
}

void bridgeDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
	log.trace "description.xml response (application/xml)"
	def body = hubResponse.xml
	if (body?.device?.modelName?.text()?.startsWith("Philips hue bridge")) {
		def bridges = getHueBridges()
		def bridge = bridges.find {it?.key?.contains(body?.device?.UDN?.text())}
		if (bridge) {
			def idNumber = getBridgeIdNumber(body?.device?.serialNumber?.text())

			// usually in form of bridge name followed by (ip), i.e. defaults to Philips Hue (192.168.1.2)
			// replace IP with id number to make it easier for user to identify
			def name = body?.device?.friendlyName?.text()
			def index = name?.indexOf('(')
			if (index != -1) {
				name = name.substring(0,index)
				name += " ($idNumber)"
			}
			bridge.value << [name:name, serialNumber:body?.device?.serialNumber?.text(), idNumber: idNumber, verified: true]
		} else {
			log.error "/description.xml returned a bridge that didn't exist"
		}
	}
}

void lightsHandler(physicalgraph.device.HubResponse hubResponse) {
	if (isValidSource(hubResponse.mac)) {
		def body = hubResponse.json
		if (!body?.state?.on) { //check if first time poll made it here by mistake
			log.debug "Adding bulbs to state!"
			updateBulbState(body, hubResponse.hubId)
		}
	}
}

void usernameHandler(physicalgraph.device.HubResponse hubResponse) {
	if (isValidSource(hubResponse.mac)) {
		def body = hubResponse.json
		if (body.success != null) {
			if (body.success[0] != null) {
				if (body.success[0].username)
					state.username = body.success[0].username
			}
		} else if (body.error != null) {
			//TODO: handle retries...
			log.error "ERROR: application/json ${body.error}"
		}
	}
}

/**
 * @deprecated This has been replaced by the combination of {@link #ssdpBridgeHandler()}, {@link #bridgeDescriptionHandler()},
 * {@link #lightsHandler()}, and {@link #usernameHandler()}. After a pending event subscription migration, it can be removed.
 */
@Deprecated
def locationHandler(evt) {
	def description = evt.description
	log.trace "Location: $description"

	def hub = evt?.hubId
 	def parsedEvent = parseLanMessage(description)
	parsedEvent << ["hub":hub]

	if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:basic:1")) {
		//SSDP DISCOVERY EVENTS
		log.trace "SSDP DISCOVERY EVENTS"
		def bridges = getHueBridges()
		log.trace bridges.toString()
		if (!(bridges."${parsedEvent.ssdpUSN.toString()}")) {
			//bridge does not exist
			log.trace "Adding bridge ${parsedEvent.ssdpUSN}"
			bridges << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
		} else {
			// update the values
			def ip = convertHexToIP(parsedEvent.networkAddress)
			def port = convertHexToInt(parsedEvent.deviceAddress)
			def host = ip + ":" + port
			log.debug "Device ($parsedEvent.mac) was already found in state with ip = $host."
			def dstate = bridges."${parsedEvent.ssdpUSN.toString()}"
			def dni = "${parsedEvent.mac}"
			def d = getChildDevice(dni)
			def networkAddress = null
			if (!d) {
				childDevices.each {
					if (it.getDeviceDataByName("mac")) {
						def newDNI = "${it.getDeviceDataByName("mac")}"
						d = it
						if (newDNI != it.deviceNetworkId) {
							def oldDNI = it.deviceNetworkId
							log.debug "updating dni for device ${it} with $newDNI - previous DNI = ${it.deviceNetworkId}"
							it.setDeviceNetworkId("${newDNI}")
							if (oldDNI == selectedHue) {
								app.updateSetting("selectedHue", newDNI)
							}
							doDeviceSync()
						}
					}
				}
			} else {
				updateBridgeStatus(d)
				if (d.getDeviceDataByName("networkAddress")) {
					networkAddress = d.getDeviceDataByName("networkAddress")
				} else {
					networkAddress = d.latestState('networkAddress').stringValue
				}
				log.trace "Host: $host - $networkAddress"
				if(host != networkAddress) {
					log.debug "Device's port or ip changed for device $d..."
					dstate.ip = ip
					dstate.port = port
					dstate.name = "Philips hue ($ip)"
					d.sendEvent(name:"networkAddress", value: host)
					d.updateDataValue("networkAddress", host)
				}
			}
		}
	} else if (parsedEvent.headers && parsedEvent.body) {
		log.trace "HUE BRIDGE RESPONSES"
		def headerString = parsedEvent.headers.toString()
		if (headerString?.contains("xml")) {
			log.trace "description.xml response (application/xml)"
			def body = new XmlSlurper().parseText(parsedEvent.body)
			if (body?.device?.modelName?.text().startsWith("Philips hue bridge")) {
				def bridges = getHueBridges()
				def bridge = bridges.find {it?.key?.contains(body?.device?.UDN?.text())}
				if (bridge) {
					bridge.value << [name:body?.device?.friendlyName?.text(), serialNumber:body?.device?.serialNumber?.text(), verified: true]
				} else {
					log.error "/description.xml returned a bridge that didn't exist"
				}
			}
		} else if(headerString?.contains("json") && isValidSource(parsedEvent.mac)) {
			log.trace "description.xml response (application/json)"
			def body = new groovy.json.JsonSlurper().parseText(parsedEvent.body)
			if (body.success != null) {
				if (body.success[0] != null) {
					if (body.success[0].username) {
						state.username = body.success[0].username
					}
				}
			} else if (body.error != null) {
				//TODO: handle retries...
				log.error "ERROR: application/json ${body.error}"
			} else {
				//GET /api/${state.username}/lights response (application/json)
				if (!body?.state?.on) { //check if first time poll made it here by mistake
					log.debug "Adding bulbs to state!"
					updateBulbState(body, parsedEvent.hub)
				}
			}
		}
	} else {
		log.trace "NON-HUE EVENT $evt.description"
	}
}

def doDeviceSync(){
	log.trace "Doing Hue Device Sync!"

	// Check if state.updating failed to clear
	if (state.lastUpdateStarted < (now() - 20 * 1000) && state.updating) {
		state.updating = false
		log.warn "state.updating failed to clear"
	}

	convertBulbListToMap()
	poll()
	ssdpSubscribe()
	discoverBridges()
	checkBridgeStatus()
}

/**
 * Called when data is received from the Hue bridge, this will update the lastActivity() that
 * is used to keep track of online/offline status of the bridge. Bridge is considered offline
 * if not heard from in 16 minutes
 *
 * @param childDevice Hue Bridge child device
 */
private void updateBridgeStatus(childDevice) {
	// Update activity timestamp if child device is a valid bridge
	def vbridges = getVerifiedHueBridges()
	def vbridge = vbridges.find {
		"${it.value.mac}".toUpperCase() == childDevice?.device?.deviceNetworkId?.toUpperCase()
	}
	vbridge?.value?.lastActivity = now()
	if (vbridge && childDevice?.device?.currentValue("status") == "Offline") {
		log.debug "$childDevice is back Online"
		childDevice?.sendEvent(name: "status", value: "Online")
		childDevice?.sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
	}
}

/**
 * Check if all Hue bridges have been heard from in the last 11 minutes, if not an Offline event will be sent
 * for the bridge and all connected lights. Also, set ID number on bridge if not done previously.
 */
private void checkBridgeStatus() {
	def bridges = getHueBridges()
	// Check if each bridge has been heard from within the last 11 minutes (2 poll intervals times 5 minutes plus buffer)
	def time = now() - (1000 * 60 * 11)
	bridges.each {
		def d = getChildDevice(it.value.mac)
		if (d) {
			// Set id number on bridge if not done
			if (it.value.idNumber == null) {
				it.value.idNumber = getBridgeIdNumber(it.value.serialNumber)
				d.sendEvent(name: "idNumber", value: it.value.idNumber)
			}

			if (it.value.lastActivity < time) { // it.value.lastActivity != null &&
				if (d.currentStatus == "Online") {
					log.warn "$d is Offline"
					d.sendEvent(name: "status", value: "Offline")
					d.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)

					Calendar currentTime = Calendar.getInstance()
					getChildDevices().each {
						def id = getId(it)
						if (state.bulbs[id]?.online == true) {
							state.bulbs[id]?.online = false
							state.bulbs[id]?.unreachableSince = currentTime.getTimeInMillis()
							it.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
						}
					}
				}
			} else if (d.currentStatus == "Offline") {
				log.debug "$d is back Online"
				d.sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
				d.sendEvent(name: "status", value: "Online")//setOnline(false)
			}
		}
	}
}

def isValidSource(macAddress) {
	def vbridges = getVerifiedHueBridges()
	return (vbridges?.find {"${it.value.mac}" == macAddress}) != null
}

def isInBulbDiscovery() {
	return state.inBulbDiscovery
}

private updateBulbState(messageBody, hub) {
	def bulbs = getHueBulbs()

	// Copy of bulbs used to locate old lights in state that are no longer on bridge
	def toRemove = [:]
	toRemove << bulbs

	messageBody.each { k,v ->

		if (v instanceof Map) {
			if (bulbs[k] == null) {
				bulbs[k] = [:]
			}
			bulbs[k] << [id: k, name: v.name, type: v.type, modelid: v.modelid, hub:hub, remove: false]
			toRemove.remove(k)
		}
	}

	// Remove bulbs from state that are no longer discovered
	toRemove.each { k,v ->
		log.warn "${bulbs[k].name} no longer exists on bridge, removing"
		bulbs.remove(k)
	}
}

/////////////////////////////////////
//CHILD DEVICE METHODS
/////////////////////////////////////

def parse(childDevice, description) {
	// Update activity timestamp if child device is a valid bridge
	updateBridgeStatus(childDevice)

	def parsedEvent = parseLanMessage(description)
	if (parsedEvent.headers && parsedEvent.body) {
		def headerString = parsedEvent.headers.toString()
		def bodyString = parsedEvent.body.toString()
		if (headerString?.contains("json")) {
			def body
			try {
				body = new groovy.json.JsonSlurper().parseText(bodyString)
			} catch (all) {
				log.warn "Parsing Body failed - trying again..."
				poll()
			}
			if (body instanceof java.util.Map) {
				// get (poll) reponse
				return handlePoll(body)
			} else {
				//put response
				return handleCommandResponse(body)
			}
		}
	} else {
		log.debug "parse - got something other than headers,body..."
		return []
	}
}

// Philips Hue priority for color is  xy > ct > hs
// For SmartThings, try to always send hue, sat and hex
private sendColorEvents(device, xy, hue, sat, ct, colormode = null) {
	if (device == null || (xy == null && hue == null && sat == null && ct == null))
		return

	def events = [:]
	// For now, only care about changing color temperature if requested by user
	if (ct != null && (colormode == "ct" || (xy == null && hue == null && sat == null))) {
		// for some reason setting Hue to their specified minimum off 153 yields 154, dealt with below
		// 153 (6500K) to 500 (2000K)
		def temp = (ct == 154) ? 6500 : Math.round(1000000 / ct)
		device.sendEvent([name: "colorTemperature", value: temp, descriptionText: "Color temperature has changed"])
		// Return because color temperature change is not counted as a color change in SmartThings so no hex update necessary
		return
	}

	if (hue != null) {
		// 0-65535
		def value = Math.min(Math.round(hue * 100 / 65535), 65535) as int
		events["hue"] = [name: "hue", value: value, descriptionText: "Color has changed", displayed: false]
	}

	if (sat != null) {
		// 0-254
		def value = Math.round(sat * 100 / 254) as int
		events["saturation"] = [name: "saturation", value: value, descriptionText: "Color has changed", displayed: false]
	}

	// Following is used to decide what to base hex calculations on since it is preferred to return a colorchange in hex
	if (xy != null && colormode != "hs") {
		// If xy is present and color mode is not specified to hs, pick xy because of priority

		// [0.0-1.0, 0.0-1.0]
		def id = device.deviceNetworkId?.split("/")[1]
		def model = state.bulbs[id]?.modelid
		def hex = colorFromXY(xy, model)

		// Create Hue and Saturation events if not previously existing
		def hsv = hexToHsv(hex)
		if (events["hue"] == null)
			events["hue"] = [name: "hue", value: hsv[0], descriptionText: "Color has changed", displayed: false]
		if (events["saturation"] == null)
			events["saturation"] = [name: "saturation", value: hsv[1], descriptionText: "Color has changed", displayed: false]

		events["color"] = [name: "color", value: hex.toUpperCase(), descriptionText: "Color has changed", displayed: true]
	} else if (colormode == "hs" || colormode == null) {
		// colormode is "hs" or "xy" is missing, default to follow hue/sat which is already handled above
		def hueValue = (hue != null) ? events["hue"].value : Integer.parseInt("$device.currentHue")
		def satValue = (sat != null) ? events["saturation"].value : Integer.parseInt("$device.currentSaturation")


		def hex = hsvToHex(hueValue, satValue)
		events["color"] = [name: "color", value: hex.toUpperCase(), descriptionText: "Color has changed", displayed: true]
	}

	boolean sendColorChanged = false
	events.each {
		device.sendEvent(it.value)
	}
}

private sendBasicEvents(device, param, value) {
	if (device == null || value == null || param == null)
		return

	switch (param) {
		case "on":
			device.sendEvent(name: "switch", value: (value == true) ? "on" : "off")
			break
		case "bri":
			// 1-254
			def level = Math.max(1, Math.round(value * 100 / 254)) as int
			device.sendEvent(name: "level", value: level, descriptionText: "Level has changed to ${level}%")
			break
	}
}

/**
 * Handles a response to a command (PUT) sent to the Hue Bridge.
 *
 * Will send appropriate events depending on values changed.
 *
 * 	Example payload
 * [,
 * {"success":{"/lights/5/state/bri":87}},
 * {"success":{"/lights/5/state/transitiontime":4}},
 * {"success":{"/lights/5/state/on":true}},
 * {"success":{"/lights/5/state/xy":[0.4152,0.5336]}},
 * {"success":{"/lights/5/state/alert":"none"}}
 * ]
 *
 * @param body a data structure of lists and maps based on a JSON data
 * @return empty array
 */
private handleCommandResponse(body) {
	// scan entire response before sending events to make sure they are always in the same order
	def updates = [:]

	body.each { payload ->
		if (payload?.success) {
			def childDeviceNetworkId = app.id + "/"
			def eventType
			payload.success.each { k, v ->
				def data = k.split("/")
				if (data.length == 5) {
					childDeviceNetworkId = app.id + "/" + k.split("/")[2]
					if (!updates[childDeviceNetworkId])
						updates[childDeviceNetworkId] = [:]
					eventType = k.split("/")[4]
					updates[childDeviceNetworkId]."$eventType" = v
				}
			}
		} else if (payload?.error) {
			log.warn "Error returned from Hue bridge, error = ${payload?.error}"
			// Check for unauthorized user
			if (payload?.error?.type?.value == 1) {
				log.error "Hue username is not valid"
				state.refreshUsernameNeeded = true
				state.username = null
			}
			return []
		}
	}

	// send events for each update found above (order of events should be same as handlePoll())
	updates.each { childDeviceNetworkId, params ->
		def device = getChildDevice(childDeviceNetworkId)
		def id = getId(device)
		sendBasicEvents(device, "on", params.on)
		sendBasicEvents(device, "bri", params.bri)
		sendColorEvents(device, params.xy, params.hue, params.sat, params.ct)
	}
	return []
}

/**
 * Handles a response to a poll (GET) sent to the Hue Bridge.
 *
 * Will send appropriate events depending on values changed.
 *
 * 	Example payload
 *
 * {"5":{"state": {"on":true,"bri":102,"hue":25600,"sat":254,"effect":"none","xy":[0.1700,0.7000],"ct":153,"alert":"none",
 * "colormode":"xy","reachable":true}, "type": "Extended color light", "name": "Go", "modelid": "LLC020", "manufacturername": "Philips",
 * "uniqueid":"00:17:88:01:01:13:d5:11-0b", "swversion": "5.38.1.14378"},
 * "6":{"state": {"on":true,"bri":103,"hue":14910,"sat":144,"effect":"none","xy":[0.4596,0.4105],"ct":370,"alert":"none",
 * "colormode":"ct","reachable":true}, "type": "Extended color light", "name": "Upstairs Light", "modelid": "LCT007", "manufacturername": "Philips",
 * "uniqueid":"00:17:88:01:10:56:ba:2c-0b", "swversion": "5.38.1.14919"},
 *
 * @param body a data structure of lists and maps based on a JSON data
 * @return empty array
 */
private handlePoll(body) {
	// Used to track "unreachable" time
	// Device is considered "offline" if it has been in the "unreachable" state for
	// 11 minutes (e.g. two poll intervals)
	// Note, Hue Bridge marks devices as "unreachable" often even when they accept commands
	Calendar time11 = Calendar.getInstance()
	time11.add(Calendar.MINUTE, -11)
	Calendar currentTime = Calendar.getInstance()

	def bulbs = getChildDevices()
	for (bulb in body) {
		def device = bulbs.find { it.deviceNetworkId == "${app.id}/${bulb.key}" }
		if (device) {
			if (bulb.value.state?.reachable) {
				if (state.bulbs[bulb.key]?.online == false || state.bulbs[bulb.key]?.online == null) {
					// light just came back online, notify device watch
					device.sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
					log.debug "$device is Online"
				}
				// Mark light as "online"
				state.bulbs[bulb.key]?.unreachableSince = null
				state.bulbs[bulb.key]?.online = true
			} else {
				if (state.bulbs[bulb.key]?.unreachableSince == null) {
					// Store the first time where device was reported as "unreachable"
					state.bulbs[bulb.key]?.unreachableSince = currentTime.getTimeInMillis()
				}
				if (state.bulbs[bulb.key]?.online || state.bulbs[bulb.key]?.online == null) {
					// Check if device was "unreachable" for more than 11 minutes and mark "offline" if necessary
					if (state.bulbs[bulb.key]?.unreachableSince < time11.getTimeInMillis() || state.bulbs[bulb.key]?.online == null) {
						log.warn "$device went Offline"
						state.bulbs[bulb.key]?.online = false
						device.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
					}
				}
				log.warn "$device may not reachable by Hue bridge"
			}
			// If user just executed commands, then do not send events to avoid confusing the turning on/off state
			if (!state.updating) {
				sendBasicEvents(device, "on", bulb.value?.state?.on)
				sendBasicEvents(device, "bri", bulb.value?.state?.bri)
				sendColorEvents(device, bulb.value?.state?.xy, bulb.value?.state?.hue, bulb.value?.state?.sat, bulb.value?.state?.ct, bulb.value?.state?.colormode)
			}
		}
	}
	return []
}

private updateInProgress() {
	state.updating = true
	state.lastUpdateStarted = now()
	runIn(20, updateHandler)
}

def updateHandler() {
	state.updating = false
	poll()
}

def hubVerification(bodytext) {
	log.trace "Bridge sent back description.xml for verification"
	def body = new XmlSlurper().parseText(bodytext)
	if (body?.device?.modelName?.text().startsWith("Philips hue bridge")) {
		def bridges = getHueBridges()
		def bridge = bridges.find {it?.key?.contains(body?.device?.UDN?.text())}
		if (bridge) {
			bridge.value << [name:body?.device?.friendlyName?.text(), serialNumber:body?.device?.serialNumber?.text(), verified: true]
		} else {
			log.error "/description.xml returned a bridge that didn't exist"
		}
	}
}

def on(childDevice) {
	log.debug "Executing 'on'"
	def id = getId(childDevice)
	updateInProgress()
	createSwitchEvent(childDevice, "on")
	put("lights/$id/state", [on: true])
	return "Bulb is turning On"
}

def off(childDevice) {
	log.debug "Executing 'off'"
	def id = getId(childDevice)
	updateInProgress()
	createSwitchEvent(childDevice, "off")
	put("lights/$id/state", [on: false])
	return "Bulb is turning Off"
}

def setLevel(childDevice, percent) {
	log.debug "Executing 'setLevel'"
	def id = getId(childDevice)
	updateInProgress()
	// 1 - 254
	def level
	if (percent == 1)
		level = 1
	else
		level = Math.min(Math.round(percent * 254 / 100), 254)

	createSwitchEvent(childDevice, level > 0 ,percent)

	// For Zigbee lights, if level is set to 0 ST just turns them off without changing level
	// that means that the light will still be on when on is called next time
	// Lets emulate that here
	if (percent > 0) {
		put("lights/$id/state", [bri: level, on: true])
	} else {
		put("lights/$id/state", [on: false])
	}
	return "Setting level to $percent"
}

def setSaturation(childDevice, percent) {
	log.debug "Executing 'setSaturation($percent)'"
	def id = getId(childDevice)
	updateInProgress()
	// 0 - 254
	def level = Math.min(Math.round(percent * 254 / 100), 254)
	// TODO should this be done by app only or should we default to on?
	createSwitchEvent(childDevice, "on")
	put("lights/$id/state", [sat: level, on: true])
	return "Setting saturation to $percent"
}

def setHue(childDevice, percent) {
	log.debug "Executing 'setHue($percent)'"
	def id = getId(childDevice)
	updateInProgress()
	// 0 - 65535
	def level =	Math.min(Math.round(percent * 65535 / 100), 65535)
	// TODO should this be done by app only or should we default to on?
	createSwitchEvent(childDevice, "on")
	put("lights/$id/state", [hue: level, on: true])
	return "Setting hue to $percent"
}

def setColorTemperature(childDevice, huesettings) {
	log.debug "Executing 'setColorTemperature($huesettings)'"
	def id = getId(childDevice)
	updateInProgress()
	// 153 (6500K) to 500 (2000K)
	def ct = hueSettings == 6500 ? 153 : Math.round(1000000/huesettings)
	createSwitchEvent(childDevice, "on")
	put("lights/$id/state", [ct: ct, on: true])
	return "Setting color temperature to $percent"
}

def setColor(childDevice, huesettings) {
	log.debug "Executing 'setColor($huesettings)'"
	def id = getId(childDevice)
	updateInProgress()

	def value = [:]
	def hue = null
	def sat = null
	def xy = null

	// Prefer hue/sat over hex to make sure it works with the majority of the smartapps
	if (huesettings.hue != null || huesettings.sat != null) {
		// If both hex and hue/sat are set, send all values to bridge to get hue/sat in response from bridge to
		// generate hue/sat events even though bridge will prioritize XY when setting color
		if (huesettings.hue != null)
			value.hue = Math.min(Math.round(huesettings.hue * 65535 / 100), 65535)
		if (huesettings.saturation != null)
			value.sat = Math.min(Math.round(huesettings.saturation * 254 / 100), 254)
	} else if (huesettings.hex != null) {
		// For now ignore model to get a consistent color if same color is set across multiple devices
		// def model = state.bulbs[getId(childDevice)]?.modelid
		// value.xy = calculateXY(huesettings.hex, model)
		// Once groups, or scenes are introduced it might be a good idea to use unique models again
		value.xy = calculateXY(huesettings.hex)
	}

/* Disabled for now due to bad behavior via Lightning Wizard
	if (!value.xy) {
		// Below will translate values to hex->XY to take into account the color support of the different hue types
		def hex = colorUtil.hslToHex((int) huesettings.hue, (int) huesettings.saturation)
		// value.xy = calculateXY(hex, model)
		// Once groups, or scenes are introduced it might be a good idea to use unique models again
		value.xy = calculateXY(hex)
	}
*/

	// Default behavior is to turn light on
	value.on = true

	if (huesettings.level != null) {
		if (huesettings.level <= 0)
			value.on = false
		else if (huesettings.level == 1)
			value.bri = 1
		else
			value.bri = Math.min(Math.round(huesettings.level * 254 / 100), 254)
	}
	value.alert = huesettings.alert ? huesettings.alert : "none"
	value.transitiontime = huesettings.transitiontime ? huesettings.transitiontime : 4

	// Make sure to turn off light if requested
	if (huesettings.switch == "off")
		value.on = false

	createSwitchEvent(childDevice, value.on ? "on" : "off")
	put("lights/$id/state", value)
	return "Setting color to $value"
}

private getId(childDevice) {
	if (childDevice.device?.deviceNetworkId?.startsWith("HUE")) {
		return childDevice.device?.deviceNetworkId[3..-1]
	} else {
		return childDevice.device?.deviceNetworkId.split("/")[-1]
	}
}

private poll() {
	def host = getBridgeIP()
	def uri = "/api/${state.username}/lights/"
	log.debug "GET: $host$uri"
	sendHubCommand(new physicalgraph.device.HubAction("GET ${uri} HTTP/1.1\r\n" +
		"HOST: ${host}\r\n\r\n", physicalgraph.device.Protocol.LAN, selectedHue))
}

private isOnline(id) {
	return (state.bulbs[id]?.online != null && state.bulbs[id]?.online) || state.bulbs[id]?.online == null
}

private put(path, body) {
	def host = getBridgeIP()
	def uri = "/api/${state.username}/$path"
	def bodyJSON = new groovy.json.JsonBuilder(body).toString()
	def length = bodyJSON.getBytes().size().toString()

	log.debug "PUT:  $host$uri"
	log.debug "BODY: ${bodyJSON}"

	sendHubCommand(new physicalgraph.device.HubAction("PUT $uri HTTP/1.1\r\n" +
		"HOST: ${host}\r\n" +
		"Content-Length: ${length}\r\n" +
		"\r\n" +
		"${bodyJSON}", physicalgraph.device.Protocol.LAN, "${selectedHue}"))
}

/*
 * Bridge serial number from Hue API is in format of 0017882413ad (mac address), however on the actual bridge hardware
 * the id is printed as only last six characters so using that to identify bridge to users
 */
private getBridgeIdNumber(serialNumber) {
	def idNumber = serialNumber ?: ""
	if (idNumber?.size() >= 6)
		idNumber = idNumber[-6..-1].toUpperCase()
	return idNumber
}

private getBridgeIP() {
	def host = null
	if (selectedHue) {
		def d = getChildDevice(selectedHue)
		if (d) {
			if (d.getDeviceDataByName("networkAddress"))
				host =  d.getDeviceDataByName("networkAddress")
		else
				host = d.latestState('networkAddress').stringValue
		}
		if (host == null || host == "") {
			def serialNumber = selectedHue
			def bridge = getHueBridges().find { it?.value?.serialNumber?.equalsIgnoreCase(serialNumber) }?.value
			if (!bridge) {
				bridge = getHueBridges().find { it?.value?.mac?.equalsIgnoreCase(serialNumber) }?.value
			}
			if (bridge?.ip && bridge?.port) {
				if (bridge?.ip.contains("."))
					host = "${bridge?.ip}:${bridge?.port}"
				else
					host = "${convertHexToIP(bridge?.ip)}:${convertHexToInt(bridge?.port)}"
			} else if (bridge?.networkAddress && bridge?.deviceAddress)
				host = "${convertHexToIP(bridge?.networkAddress)}:${convertHexToInt(bridge?.deviceAddress)}"
		}
		log.trace "Bridge: $selectedHue - Host: $host"
	}
	return host
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

def convertBulbListToMap() {
	try {
		if (state.bulbs instanceof java.util.List) {
			def map = [:]
			state.bulbs?.unique {it.id}.each { bulb ->
				map << ["${bulb.id}":["id":bulb.id, "name":bulb.name, "type": bulb.type, "modelid": bulb.modelid, "hub":bulb.hub, "online": bulb.online]]
			}
			state.bulbs = map
		}
	}
	catch(Exception e) {
		log.error "Caught error attempting to convert bulb list to map: $e"
	}
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Boolean hasAllHubsOver(String desiredFirmware) {
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions() {
	return location.hubs*.firmwareVersionString.findAll { it }
}

/**
 * Sends appropriate turningOn/turningOff state events depending on switch or level changes.
 *
 * @param childDevice device to send event for
 * @param setSwitch The new switch state, "on" or "off"
 * @param setLevel Optional, switchLevel between 0-100, used if you set level to 0 for example since
 *				  that should generate "off" instead of level change
 */
private void createSwitchEvent(childDevice, setSwitch, setLevel = null) {

	if (setLevel == null) {
		setLevel = childDevice.device?.currentValue("level")
	}
	// Create on, off, turningOn or turningOff event as necessary
	def currentState = childDevice.device?.currentValue("switch")
	if ((currentState == "off" || currentState == "turningOff")) {
		if (setSwitch == "on" || setLevel > 0) {
			childDevice.sendEvent(name: "switch", value: "turningOn", displayed: false)
		}
	} else if ((currentState == "on" || currentState == "turningOn")) {
		if (setSwitch == "off" || setLevel == 0) {
			childDevice.sendEvent(name: "switch", value: "turningOff", displayed: false)
		}
	}
}

/**
 * Return the supported color range for different Hue lights. If model is not specified
 * it defaults to the smallest Gamut (B) to ensure that colors set on a mix of devices
 * will be consistent.
 *
 * @param model Philips model number, e.g. LCT001
 * @return a map with x,y coordinates for red, green and blue according to CIE color space
 */
private colorPointsForModel(model = null) {
	def result = null
	switch (model) {
		// Gamut A
		case "LLC001": /* Monet, Renoir, Mondriaan (gen II) */
		case "LLC005": /* Bloom (gen II) */
		case "LLC006": /* Iris (gen III) */
		case "LLC007": /* Bloom, Aura (gen III) */
		case "LLC011": /* Hue Bloom */
		case "LLC012": /* Hue Bloom */
		case "LLC013": /* Storylight */
		case "LST001": /* Light Strips */
		case "LLC010": /* Hue Living Colors Iris + */
			result = [r:[x: 0.704f, y: 0.296f], g:[x: 0.2151f, y: 0.7106f], b:[x: 0.138f, y: 0.08f]];
			break
		// Gamut C
		case "LLC020": /* Hue Go */
		case "LST002": /* Hue LightStrips Plus */
			result = [r:[x: 0.692f, y: 0.308f], g:[x: 0.17f, y: 0.7f], b:[x: 0.153f, y: 0.048f]];
			break
		// Gamut B
		case "LCT001": /* Hue A19 */
		case "LCT002": /* Hue BR30 */
		case "LCT003": /* Hue GU10 */
		case "LCT007": /* Hue A19 + */
		case "LLM001": /* Color Light Module + */
		default:
			result = [r:[x: 0.675f, y: 0.322f], g:[x: 0.4091f, y: 0.518f], b:[x: 0.167f, y: 0.04f]];

	}
	return result;
}

/**
 * Return x, y  value from android color and light model Id.
 * Please note: This method does not incorporate brightness values. Get/set it from/to your light seperately.
 *
 * From Philips Hue SDK modified for Groovy
 *
 * @param color the color value in hex like // #ffa013
 * @param model the model Id of Light (or null to get default Gamut B)
 * @return the float array of length 2, where index 0 and 1 gives  x and y values respectively.
 */
private float[] calculateXY(colorStr, model = null) {

	// #ffa013
	def cred = Integer.valueOf( colorStr.substring( 1, 3 ), 16 )
	def cgreen = Integer.valueOf( colorStr.substring( 3, 5 ), 16 )
	def cblue = Integer.valueOf( colorStr.substring( 5, 7 ), 16 )

	float red = cred / 255.0f;
	float green = cgreen / 255.0f;
	float blue = cblue / 255.0f;

	// Wide gamut conversion D65
	float r = ((red > 0.04045f) ? (float) Math.pow((red + 0.055f) / (1.0f + 0.055f), 2.4f) : (red / 12.92f));
	float g = (green > 0.04045f) ? (float) Math.pow((green + 0.055f) / (1.0f + 0.055f), 2.4f) : (green / 12.92f);
	float b = (blue > 0.04045f) ? (float) Math.pow((blue + 0.055f) / (1.0f + 0.055f), 2.4f) : (blue / 12.92f);

	// Why values are different in ios and android , IOS is considered
	// Modified conversion from RGB -> XYZ with better results on colors for
	// the lights
	float x = r * 0.664511f + g * 0.154324f + b * 0.162028f;
	float y = r * 0.283881f + g * 0.668433f + b * 0.047685f;
	float z = r * 0.000088f + g * 0.072310f + b * 0.986039f;

	float[] xy = new float[2];

	xy[0] = (x / (x + y + z));
	xy[1] = (y / (x + y + z));
	if (Float.isNaN(xy[0])) {
		xy[0] = 0.0f;
	}
	if (Float.isNaN(xy[1])) {
		xy[1] = 0.0f;
	}
	/*if(true)
		return [0.0f,0.0f]*/

	// Check if the given XY value is within the colourreach of our lamps.
	def xyPoint = [x: xy[0], y: xy[1]];
	def colorPoints = colorPointsForModel(model);
	boolean inReachOfLamps = checkPointInLampsReach(xyPoint, colorPoints);
	if (!inReachOfLamps) {
		// It seems the colour is out of reach
		// let's find the closes colour we can produce with our lamp and
		// send this XY value out.

		// Find the closest point on each line in the triangle.
		def pAB = getClosestPointToPoints(colorPoints.r, colorPoints.g, xyPoint);
		def pAC = getClosestPointToPoints(colorPoints.b, colorPoints.r, xyPoint);
		def pBC = getClosestPointToPoints(colorPoints.g, colorPoints.b, xyPoint);

		// Get the distances per point and see which point is closer to our
		// Point.
		float dAB = getDistanceBetweenTwoPoints(xyPoint, pAB);
		float dAC = getDistanceBetweenTwoPoints(xyPoint, pAC);
		float dBC = getDistanceBetweenTwoPoints(xyPoint, pBC);

		float lowest = dAB;
		def closestPoint = pAB;
		if (dAC < lowest) {
			lowest = dAC;
			closestPoint = pAC;
		}
		if (dBC < lowest) {
			lowest = dBC;
			closestPoint = pBC;
		}

		// Change the xy value to a value which is within the reach of the
		// lamp.
		xy[0] = closestPoint.x;
		xy[1] = closestPoint.y;
	}
	//		xy[0] = PHHueHelper.precision(4, xy[0]);
	//		xy[1] = PHHueHelper.precision(4, xy[1]);


	// TODO needed, assume it just sets number of decimals?
	//xy[0] = PHHueHelper.precision(xy[0]);
	//xy[1] = PHHueHelper.precision(xy[1]);
	return xy;
}

/**
 * Generates the color for the given XY values and light model.  Model can be null if it is not known.
 * Note: When the exact values cannot be represented, it will return the closest match.
 * Note 2: This method does not incorporate brightness values. Get/Set it from/to your light seperately.
 *
 * From Philips Hue SDK modified for Groovy
 *
 * @param points the float array contain x and the y value. [x,y]
 * @param model the model of the lamp, example: "LCT001" for hue bulb. Used to calculate the color gamut.
 *		If this value is empty the default gamut values are used.
 * @return the color value in hex (#ff03d3). If xy is null OR xy is not an array of size 2, Color. BLACK will be returned
 */
private String colorFromXY(points, model ) {

	if (points == null || model == null) {
		log.warn "Input color missing"
		return "#000000"
	}

	def xy = [x: points[0], y: points[1]];

	def colorPoints = colorPointsForModel(model);
	boolean inReachOfLamps = checkPointInLampsReach(xy, colorPoints);

	if (!inReachOfLamps) {
		// It seems the colour is out of reach
		// let's find the closest colour we can produce with our lamp and
		// send this XY value out.
		// Find the closest point on each line in the triangle.
		def pAB = getClosestPointToPoints(colorPoints.r, colorPoints.g, xy);
		def pAC = getClosestPointToPoints(colorPoints.b, colorPoints.r, xy);
		def pBC = getClosestPointToPoints(colorPoints.g, colorPoints.b, xy);

		// Get the distances per point and see which point is closer to our
		// Point.
		float dAB = getDistanceBetweenTwoPoints(xy, pAB);
		float dAC = getDistanceBetweenTwoPoints(xy, pAC);
		float dBC = getDistanceBetweenTwoPoints(xy, pBC);
		float lowest = dAB;
		def closestPoint = pAB;
		if (dAC < lowest) {
			lowest = dAC;
			closestPoint = pAC;
		}
		if (dBC < lowest) {
			lowest = dBC;
			closestPoint = pBC;
		}
		// Change the xy value to a value which is within the reach of the
		// lamp.
		xy.x = closestPoint.x;
		xy.y = closestPoint.y;
	}
	float x = xy.x;
	float y = xy.y;
	float z = 1.0f - x - y;
	float y2 = 1.0f;
	float x2 = (y2 / y) * x;
	float z2 = (y2 / y) * z;
	/*
	 * // Wide gamut conversion float r = X * 1.612f - Y * 0.203f - Z *
	 * 0.302f; float g = -X * 0.509f + Y * 1.412f + Z * 0.066f; float b = X
	 * * 0.026f - Y * 0.072f + Z * 0.962f;
	 */
	// sRGB conversion
	// float r = X * 3.2410f - Y * 1.5374f - Z * 0.4986f;
	// float g = -X * 0.9692f + Y * 1.8760f + Z * 0.0416f;
	// float b = X * 0.0556f - Y * 0.2040f + Z * 1.0570f;

	// sRGB D65 conversion
	float r =  x2 * 1.656492f  - y2 * 0.354851f - z2 * 0.255038f;
	float g = -x2 * 0.707196f  + y2 * 1.655397f + z2 * 0.036152f;
	float b =  x2 *  0.051713f - y2 * 0.121364f + z2 * 1.011530f;

	if (r > b && r > g && r > 1.0f) {
		// red is too big
		g = g / r;
		b = b / r;
		r = 1.0f;
	} else if (g > b && g > r && g > 1.0f) {
		// green is too big
		r = r / g;
		b = b / g;
		g = 1.0f;
	} else if (b > r && b > g && b > 1.0f) {
		// blue is too big
		r = r / b;
		g = g / b;
		b = 1.0f;
	}
	// Apply gamma correction
	r = r <= 0.0031308f ? 12.92f * r : (1.0f + 0.055f) * (float) Math.pow(r, (1.0f / 2.4f)) - 0.055f;
	g = g <= 0.0031308f ? 12.92f * g : (1.0f + 0.055f) * (float) Math.pow(g, (1.0f / 2.4f)) - 0.055f;
	b = b <= 0.0031308f ? 12.92f * b : (1.0f + 0.055f) * (float) Math.pow(b, (1.0f / 2.4f)) - 0.055f;

	if (r > b && r > g) {
		// red is biggest
		if (r > 1.0f) {
			g = g / r;
			b = b / r;
			r = 1.0f;
		}
	} else if (g > b && g > r) {
		// green is biggest
		if (g > 1.0f) {
			r = r / g;
			b = b / g;
			g = 1.0f;
		}
	} else if (b > r && b > g && b > 1.0f) {
		r = r / b;
		g = g / b;
		b = 1.0f;
	}

	// neglecting if the value is negative.
	if (r < 0.0f) {
		r = 0.0f;
	}
	if (g < 0.0f) {
		g = 0.0f;
	}
	if (b < 0.0f) {
		b = 0.0f;
	}

	// Converting float components to int components.
	def r1 = String.format("%02X", (int) (r * 255.0f));
	def g1 = String.format("%02X", (int) (g * 255.0f));
	def b1 = String.format("%02X", (int) (b * 255.0f));

	return "#$r1$g1$b1"
}


/**
 * Calculates crossProduct of two 2D vectors / points.
 *
 * From Philips Hue SDK modified for Groovy
 *
 * @param p1 first point used as vector [x: 0.0f, y: 0.0f]
 * @param p2 second point used as vector [x: 0.0f, y: 0.0f]
 * @return crossProduct of vectors
 */
private float crossProduct(p1, p2) {
	return (p1.x * p2.y - p1.y * p2.x);
}

/**
 * Find the closest point on a line.
 * This point will be within reach of the lamp.
 *
 * From Philips Hue SDK modified for Groovy
 *
 * @param A the point where the line starts [x:..,y:..]
 * @param B the point where the line ends [x:..,y:..]
 * @param P the point which is close to a line. [x:..,y:..]
 * @return the point which is on the line. [x:..,y:..]
 */
private getClosestPointToPoints(A, B, P) {
	def AP = [x: (P.x - A.x), y: (P.y - A.y)];
	def AB = [x: (B.x - A.x), y: (B.y - A.y)];

	float ab2 = AB.x*AB.x + AB.y*AB.y;
	float ap_ab = AP.x*AB.x + AP.y*AB.y;

	float t = ap_ab / ab2;

	if (t < 0.0f)
		t = 0.0f;
	else if (t > 1.0f)
		t = 1.0f;

	def newPoint = [x: (A.x + AB.x * t), y: (A.y + AB.y * t)];
	return newPoint;
}

/**
 * Find the distance between two points.
 *
 * From Philips Hue SDK modified for Groovy
 *
 * @param one [x:..,y:..]
 * @param two [x:..,y:..]
 * @return the distance between point one and two
 */
private float getDistanceBetweenTwoPoints(one, two) {
	float dx = one.x - two.x; // horizontal difference
	float dy = one.y - two.y; // vertical difference
	float dist = Math.sqrt(dx * dx + dy * dy);

	return dist;
}

/**
 * Method to see if the given XY value is within the reach of the lamps.
 *
 * From Philips Hue SDK modified for Groovy
 *
 * @param p the point containing the X,Y value [x:..,y:..]
 * @return true if within reach, false otherwise.
 */
private boolean checkPointInLampsReach(p, colorPoints) {

	def red =   colorPoints.r;
	def green = colorPoints.g;
	def blue =  colorPoints.b;

	def v1 = [x: (green.x - red.x), y: (green.y - red.y)];
	def v2 = [x: (blue.x - red.x), y: (blue.y - red.y)];

	def q = [x: (p.x - red.x), y: (p.y - red.y)];

	float s = crossProduct(q, v2) / crossProduct(v1, v2);
	float t = crossProduct(v1, q) / crossProduct(v1, v2);

	if ( (s >= 0.0f) && (t >= 0.0f) && (s + t <= 1.0f))
	{
		return true;
	}
	else
	{
		return false;
	}
}

/**
 * Converts an RGB color in hex to HSV/HSB.
 * Algorithm based on http://en.wikipedia.org/wiki/HSV_color_space.
 *
 * @param colorStr color value in hex (#ff03d3)
 *
 * @return HSV representation in an array (0-100) [hue, sat, value]
 */
def hexToHsv(colorStr){
	def r = Integer.valueOf( colorStr.substring( 1, 3 ), 16 ) / 255
	def g = Integer.valueOf( colorStr.substring( 3, 5 ), 16 ) / 255
	def b = Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) / 255

	def max = Math.max(Math.max(r, g), b)
	def min = Math.min(Math.min(r, g), b)

	def h, s, v = max

	def d = max - min
	s = max == 0 ? 0 : d / max

	if(max == min){
		h = 0
	}else{
		switch(max){
			case r: h = (g - b) / d + (g < b ? 6 : 0); break
			case g: h = (b - r) / d + 2; break
			case b: h = (r - g) / d + 4; break
		}
		h /= 6;
	}

	return [Math.round(h * 100), Math.round(s * 100), Math.round(v * 100)]
}

/**
 * Converts HSV/HSB color to RGB in hex.
 * Algorithm based on http://en.wikipedia.org/wiki/HSV_color_space.
 *
 * @param  hue hue 0-100
 * @param  sat saturation 0-100
 * @param  value value 0-100 (defaults to 100)

 * @return the color in hex (#ff03d3)
 */
def hsvToHex(hue, sat, value = 100){
	def r, g, b;
	def h = hue / 100
	def s = sat / 100
	def v = value / 100

	def i = Math.floor(h * 6)
	def f = h * 6 - i
	def p = v * (1 - s)
	def q = v * (1 - f * s)
	def t = v * (1 - (1 - f) * s)

	switch (i % 6) {
		case 0:
			r = v
			g = t
			b = p
			break
		case 1:
			r = q
			g = v
			b = p
			break
		case 2:
			r = p
			g = v
			b = t
			break
		case 3:
			r = p
			g = q
			b = v
			break
		case 4:
			r = t
			g = p
			b = v
			break
		case 5:
			r = v
			g = p
			b = q
			break
	}

	// Converting float components to int components.
	def r1 = String.format("%02X", (int) (r * 255.0f))
	def g1 = String.format("%02X", (int) (g * 255.0f))
	def b1 = String.format("%02X", (int) (b * 255.0f))

	return "#$r1$g1$b1"
}

