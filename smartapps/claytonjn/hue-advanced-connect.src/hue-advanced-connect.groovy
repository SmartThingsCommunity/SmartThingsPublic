/**
 *  Hue Advanced Service Manager
 *
 *  Author: Clayton (claytonjn)
 *
 *  Copyright 2015 SmartThings
 *  Copyright 2016 claytonjn
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
 */

definition(
	name: "Hue Advanced (Connect)",
	namespace: "claytonjn",
	author: "claytonjn",
	description: "Allows you to connect your Philips Hue lights with SmartThings and control them from your Things area or Dashboard in the SmartThings Mobile app. Adjust colors by going to the Thing detail screen for your Hue lights (tap the gear on Hue tiles).\n\nPlease update your Hue Bridge first, outside of the SmartThings app, using the Philips Hue app.",
	category: "My Apps",
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
	page(name:"groupDiscovery", title:"Hue Device Setup", content:"groupDiscovery", refreshTimeout:5)
	page(name:"advancedSettings", title:"Advanced Settings", content:"advancedSettings")
}

def mainPage() {
	def bridges = bridgesDiscovered()
	if (state.username && bridges) {
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
			input "selectedHue", "enum", required:false, title:"Select Hue Bridge (${numFound} found)", multiple:false, options:options
		}
	}
}

def bridgeDiscoveryFailed() {
	return dynamicPage(name:"bridgeDiscoveryFailed", title: "Bridge Discovery Failed", nextPage: "bridgeDiscovery") {
		section("Failed to discover any Hue Bridges. Please confirm that the Hue Bridge is connected to the same network as your SmartThings Hub, and that it has power.") {
		}
	}
}

def bridgeLinking()
{
	int linkRefreshcount = !state.linkRefreshcount ? 0 : state.linkRefreshcount as int
	state.linkRefreshcount = linkRefreshcount + 1
	def refreshInterval = 3

	def nextPage = ""
	def title = "Linking with your Hue"
    def paragraphText
	if (selectedHue) {
		paragraphText = "Press the button on your Hue Bridge to setup a link. "
    } else {
    	paragraphText = "You haven't selected a Hue Bridge, please Press \"Done\" and select one before clicking next."
    }
	if (state.username) { //if discovery worked
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
	def deviceType = "lights"
	int bulbRefreshCount = !state.bulbRefreshCount ? 0 : state.bulbRefreshCount as int
	state.bulbRefreshCount = bulbRefreshCount + 1
	def refreshInterval = 3
	state.inDeviceDiscovery = true
	def bridge = null
	if (selectedHue) {
        bridge = getChildDevice(selectedHue)
        subscribe(bridge, "lightsList", bulbListData)
	}
    state.bridgeRefreshCount = 0
	def bulboptions = devicesDiscovered(deviceType) ?: [:]
	def numFound = bulboptions.size() ?: 0
    if (numFound == 0)
    	app.updateSetting("selectedBulbs", "")

	if((bulbRefreshCount % 5) == 0) {
		discoverHueDevices(deviceType)
	}
	def selectedBridge = state.bridges.find { key, value -> value?.serialNumber?.equalsIgnoreCase(selectedHue) }
	def title = selectedBridge?.value?.name ?: "Find bridges"

	return dynamicPage(name:"bulbDiscovery", title:"Bulb Discovery Started!", nextPage:"groupDiscovery", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Hue Bulbs. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedBulbs", "enum", required:false, title:"Select Hue Bulbs (${numFound} found)", multiple:true, options:bulboptions
		}
		section {
			href "bridgeDiscovery", title: title, description: "", state: selectedHue ? "complete" : "incomplete", params: [override: true]

		}
	}
}

def groupDiscovery() {
	def deviceType = "groups"
	int groupRefreshCount = !state.groupRefreshCount ? 0 : state.groupRefreshCount as int
	state.groupRefreshCount = groupRefreshCount + 1
	def refreshInterval = 3
	state.inDeviceDiscovery = true
	def bridge = null
	if (selectedHue) {
	    bridge = getChildDevice(selectedHue)
        subscribe(bridge, "groupsList", groupListData)
	}
    state.bridgeRefreshCount = 0
	def groupoptions = devicesDiscovered(deviceType) ?: [:]
	def numFound = groupoptions.size() ?: 0
    if (numFound == 0)
    	app.updateSetting("selectedGroups", "")

	if((groupRefreshCount % 5) == 0) {
		discoverHueDevices(deviceType)
	}

	return dynamicPage(name:"groupDiscovery", title:"Group Discovery Started!", nextPage:"advancedSettings", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Hue Groups. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedGroups", "enum", required:false, title:"Select Hue Groups (${numFound} found)", multiple:true, options:groupoptions
		}
		section {
			def title = getBridgeIP() ? "Hue bridge (${getBridgeIP()})" : "Find bridges"
			href "bridgeDiscovery", title: title, description: "", state: selectedHue ? "complete" : "incomplete", params: [override: true]

		}
	}
}

def advancedSettings() {

	return dynamicPage(name:"advancedSettings", title:"Advanced Settings", nextPage:"", install:true, uninstall:true) {
		section("Default Transition") {
			paragraph	"Choose how long bulbs should take to transition between on/off and color changes by default. This can be modified per-device."
			input		"selectedTransition", "number", required:true, title:"Transition Time (seconds)", defaultValue:1
		}
		section("Circadian Daylight Integration") {
			paragraph	"Add buttons to each device page, allowing you to enable/disable automatic color changes and dynamic brightness per-device, on the fly."
			href(		name:        "circadianDaylightLink",
						title:       "More Info",
						required:    false,
						style:       "embeded", //TODO: change to "external" when that works on Android
						url:         "https://community.smartthings.com/t/circadian-daylight-smartthings-smart-bulbs/",
						description: "Tap to view more information about the Circadian Daylight SmartApp.",
						image:       "https://raw.githubusercontent.com/claytonjn/SmartThingsPublic/Circadian-Daylight/smartapp-icons/PNG/circadian-daylight@2x.png"	)
			input		name:"circadianDaylightIntegration", type:"bool", title:"Circadian Daylight Integration", defaultValue:false
		}
		section("Update Notifications") {
			paragraph 	"Get push notifications when an update is pushed to GitHub."
			input(		name: 			"updateNotifications",
						type:			"bool",
						title:			"Update Notifications",
						submitOnChange:	true	)
			if (updateNotifications) {
				input("recipients", "contact", title: "Send notifications to") {
					input "updatePush", "bool", title: "Send push notifications", required: false
				}
				input(		name:			"gitHubBranch",
							type:			"enum",
							title: 			"Branch",
							description:	"Get notifications for the stable or beta branch?",
							options:		["Stable", "Beta"],
							defaultValue:	"Stable",
							multiple:		true,
							required:		true	)
			}
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

private discoverHueDevices(deviceType) {
	def callbackFunction = (deviceType == "lights") ? "lightsHandler" : (deviceType == "groups") ? "groupsHandler" : null
	def host = getBridgeIP()
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/api/${state.username}/${deviceType}",
		headers: [
			HOST: host
		]], "${selectedHue}", [callback: callbackFunction]))
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

Map devicesDiscovered(deviceType) {
	def devices = (deviceType == "lights") ? getHueBulbs() : (deviceType == "groups") ? getHueGroups() : null
	def devicemap = [:]
	if (devices instanceof java.util.Map) {
		devices.each {
			def value = "${it.value.name}"
			def key = app.id +"/${deviceType}/"+ it.value.id
			devicemap["${key}"] = value
		}
	} else { //backwards compatable
		devices.each {
			def value = "${it.name}"
			def key = app.id +"/${deviceType}/"+ it.id
            logg += "$value - $key, "
			devicemap["${key}"] = value
		}
	}
	return devicemap
}

def bulbListData(evt) {
	state.bulbs = evt.jsonData
}

def groupListData(evt) {
	state.groups = evt.jsonData
}

Map getHueBulbs() {
	state.bulbs = state.bulbs ?: [:]
}

Map getHueGroups() {
	state.groups = state.groups ?: [:]
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
	if (state.circadianDaylightIntegration != settings.circadianDaylightIntegration) {
		selectedBulbs?.each { dni ->
			def d = getChildDevice(dni)
			d.setHADeviceHandler(settings.circadianDaylightIntegration)
		}
		selectedGroups?.each { dni ->
			def d = getChildDevice(dni)
			d.setHADeviceHandler(settings.circadianDaylightIntegration)
		}
		state.circadianDaylightIntegration = settings.circadianDaylightIntegration
	}
}

def initialize() {
	log.debug "Initializing"
    unsubscribe(bridge)
    state.inDeviceDiscovery = false
    state.bridgeRefreshCount = 0
    state.bulbRefreshCount = 0
	state.groupRefreshCount = 0
	if (selectedHue) {
		addBridge()
        addBulbs()
		addGroups()
        doDeviceSync()
        runEvery5Minutes("doDeviceSync")
	}
}

def manualRefresh() {
    unschedule()
	unsubscribe()
    doDeviceSync()
    runEvery5Minutes("doDeviceSync")
}

def uninstalled(){
	state.bridges = [:]
    state.username = null
}

// Handles events to add new devices
def deviceListHandler(hub, deviceType, data = "") {
	def msg = "${deviceType} list not processed. Only while in settings menu."
    def devices = [:]
	if (state.inDeviceDiscovery) {
        def logg = ""
        log.trace "Adding ${deviceType} to state..."
        state.bridgeProcessedDeviceList = true
        def object = new groovy.json.JsonSlurper().parseText(data)
        object.each { k,v ->
            if (v instanceof Map)
                devices[k] = [id: k, name: v.name, type: v.type, modelid: v.modelid, hub:hub]
        }
    }
    def bridge = null
	if (selectedHue) {
        bridge = getChildDevice(selectedHue)
	}
    bridge.sendEvent(name: "${deviceType}List", value: hub, data: devices, isStateChange: true, displayed: false)
    msg = "${devices.size()} ${deviceType} found. ${devices}"
	return msg
}

private upgradeDeviceType(device, newHueType) {
	def deviceType = getDeviceHandler(newHueType)

	// Automatically change users Hue devices to correct device types
	if (deviceType && !(device?.typeName?.equalsIgnoreCase(deviceType))) {
		log.debug "Update device type: \"$device.label\" ${device?.typeName}->$deviceType"
		device.setDeviceType(deviceType)
	}
}

private getDeviceHandler(hueType) {
	// Determine ST device type based on Hue classification of light
	if (hueType?.equalsIgnoreCase("Dimmable light"))
		return "Hue Advanced${handlerType} Lux Bulb"
	else if (hueType?.equalsIgnoreCase("Extended Color Light") || hueType?.equalsIgnoreCase("LightGroup") || hueType?.equalsIgnoreCase("Room"))
		return "Hue Advanced${handlerType} Bulb/Group"
	else if (hueType?.equalsIgnoreCase("Color Light"))
		return "Hue Advanced${handlerType} LivingColors"
	else if (hueType?.equalsIgnoreCase("Color Temperature Light"))
		return "Hue Advanced${handlerType} White Ambiance Bulb"
	else
		return null
}

private getDeviceType(hueType) {
	// Determine ST device type based on Hue classification of light
	if (hueType?.equalsIgnoreCase("Dimmable light") || hueType?.equalsIgnoreCase("Extended Color Light") || hueType?.equalsIgnoreCase("Color Light"))
		return "lights"
	else if (hueType?.equalsIgnoreCase("LightGroup") || hueType?.equalsIgnoreCase("Room"))
		return "groups"
	else
		return null
}

private addChild(dni, hueType, name, hub, update=false, device = null) {
	def deviceType = getDeviceHandler(hueType)

	if (deviceType) {
		return addChildDevice("claytonjn", deviceType, dni, hub, ["label": name])
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
				newHueBulb = bulbs.find { (app.id + "/lights/" + it.value.id) == dni }

				if (newHueBulb != null) {
                    d = addChild(dni, newHueBulb?.value?.type, newHueBulb?.value?.name, newHueBulb?.value?.hub)
					d.initialize("lights")
                    if (d) {
						log.debug "created ${d.displayName} with id $dni"
						d.completedSetup = true
						d.refresh()
					}
				} else {
					log.debug "$dni in not longer paired to the Hue Bridge or ID changed"
				}
			} else {
            	//backwards compatable
				newHueBulb = bulbs.find { (app.id + "/lights/" + it.id) == dni }
				d = addChild(dni, "Extended Color Light", newHueBulb?.value?.name, newHueBulb?.value?.hub)
				d?.initialize("lights")
				d?.completedSetup = true
                d?.refresh()
			}
		} else {
			log.debug "found ${d.displayName} with id $dni already exists, type: '$d.typeName'"
			if (bulbs instanceof java.util.Map) {
				// Update device type if incorrect
            	def newHueBulb = bulbs.find { (app.id + "/lights/" + it.value.id) == dni }
				upgradeDeviceType(d, newHueBulb?.value?.type)
			}
		}
	}
}

def addGroups() {
	def groups = getHueGroups()
	selectedGroups?.each { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			def newHueGroup
			if (groups instanceof java.util.Map) {
				newHueGroup = groups.find { (app.id + "/groups/" + it.value.id) == dni }
					 if (newHueGroup != null) {
						d = addChild(dni, newHueGroup?.value?.type, newHueGroup?.value?.name, newHueGroup?.value?.hub)
						d.initialize("groups")
						d.completedSetup = true
						log.debug "created ${d.displayName} with id $dni"
						d.refresh()
					 } else {
						log.debug "$dni in not longer paired to the Hue Bridge or ID changed"
					 }
			} else {
				//backwards compatable
				newHueGroup = groups.find { (app.id + "/groups/" + it.id) == dni }
				d = addChild(dni, "LightGroup", newHueGroup?.value?.name, newHueGroup?.value?.hub)
				d.initialize("groups")
				d.completedSetup = true
				d.refresh()
			}
		} else {
			log.debug "found ${d.displayName} with id $dni already exists, type: '$d.typeName'"
			if (groups instanceof java.util.Map) {
				// Update device type if incorrect
				def newHueGroup = groups.find { (app.id + "/groups/" + it.value.id) == dni }
				upgradeDeviceType(d, newHueGroup?.value?.type)
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
				d = addChildDevice("claytonjn", "Hue Advanced Bridge", selectedHue, vbridge.value.hub)
				d?.completedSetup = true
				log.debug "created ${d.displayName} with id ${d.deviceNetworkId}"
                def childDevice = getChildDevice(d.deviceNetworkId)
                childDevice.sendEvent(name: "serialNumber", value: vbridge.value.serialNumber)
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
		def dni = "${parsedEvent.mac}"
		def d = getChildDevice(dni)
		def networkAddress = null
		if (!d) {
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
						doDeviceSync()
					}
				}
			}
		} else {
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
		}
	}
}

void bridgeDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
	log.trace "description.xml response (application/xml)"
	def body = hubResponse.xml
	if (body?.device?.modelName?.text().startsWith("Philips hue bridge")) {
		def bridges = getHueBridges()
		def bridge = bridges.find {it?.key?.contains(body?.device?.UDN?.text())}
		if (bridge) {
			// serialNumber from API is in format of 0017882413ad (mac address), however on the actual bridge only last six
			// characters are printed on the back so using that to identify bridge
			def idNumber = body?.device?.serialNumber?.text()
			if (idNumber?.size() >= 6)
				idNumber = idNumber[-6..-1].toUpperCase()

			// usually in form of bridge name followed by (ip), i.e. defaults to Philips Hue (192.168.1.2)
			// replace IP with serial number to make it easier for user to identify
			def name = body?.device?.friendlyName?.text()
			def index = name?.indexOf('(')
			if (index != -1) {
				name = name.substring(0,index)
				name += " ($idNumber)"
			}
			bridge.value << [name:name, serialNumber:body?.device?.serialNumber?.text(), verified: true]
		} else {
			log.error "/description.xml returned a bridge that didn't exist"
		}
	}
}

void lightsHandler(physicalgraph.device.HubResponse hubResponse) {
	if (isValidSource(hubResponse.mac)) {
		def body = hubResponse.json
		if (!body?.state?.on) { //check if first time poll made it here by mistake
			def bulbs = getHueBulbs()
			log.debug "Adding bulbs to state!"
			body.each { k, v ->
				bulbs[k] = [id: k, name: v.name, type: v.type, modelid: v.modelid, hub: hubResponse.hubId]
			}
		}
	}
}

void groupsHandler(physicalgraph.device.HubResponse hubResponse) {
	if (isValidSource(hubResponse.mac)) {
		def body = hubResponse.json
		if (!body?.state?.on) { //check if first time poll made it here by mistake
			def groups = getHueGroups()
			log.debug "Adding groups to state!"
			body.each { k, v ->
				groups[k] = [id: k, name: v.name, type: v.type, modelid: v.modelid, hub: hubResponse.hubId]
			}
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
	}
	else if (parsedEvent.headers && parsedEvent.body) {
		log.trace "HUE ADVANCED BRIDGE RESPONSES"
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
					def bulbs = getHueBulbs()
					log.debug "Adding bulbs to state!"
					body.each { k,v ->
						bulbs[k] = [id: k, name: v.name, type: v.type, modelid: v.modelid, hub:parsedEvent.hub]
					}
					def groups = getHueGroups()
					log.debug "Adding groups to state!"
					body.each { k,v ->
						groups[k] = [id: k, name: v.name, type: v.type, modelid: v.modelid, hub:parsedEvent.hub]
					}
				}
			}
		}
	} else {
		log.trace "NON-HUE EVENT $evt.description"
	}
}

def doDeviceSync(){
	log.trace "Doing Hue Device Sync!"
	convertDeviceListToMap()
	poll()
	ssdpSubscribe()
	discoverBridges()
	if (updateNotifications == true) { checkForUpdates() }
}

def isValidSource(macAddress) {
	def vbridges = getVerifiedHueBridges()
	return (vbridges?.find {"${it.value.mac}" == macAddress}) != null
}

void checkForUpdates() {
	for (branch in settings.gitHubBranch) {
		def branchName
		if (branch == "Stable") { branchName = "Hue-Advanced" }
		if (branch == "Beta") { branchName = "Hue-Advanced-Development" }

		def url = "https://api.github.com/repos/claytonjn/SmartThingsPublic/branches/${branchName}"

		def result = null

		try {
			httpGet(uri: url) {response ->
				result = response
			}
			def latestCommitTime = result.data.commit.commit.author.date
			if (latestCommitTime != state."last${branch}Update") {
				def message = "Hue Advanced ${branch} branch updated with message: ${result.data.commit.commit.message}"
				// check that contact book is enabled and recipients selected
				if (location.contactBookEnabled && recipients) {
				    sendNotificationToContacts(message, recipients, [event: false])
				} else if (updatePush) { // check that the user did select a phone number
				    sendPushMessage(message)
				}
				state."last${branch}Update" = result.data.commit.commit.author.date
			}
		}
		catch (e) {
			log.warn e
		}
	}
}

/////////////////////////////////////
//CHILD DEVICE METHODS
/////////////////////////////////////

def parse(childDevice, description) {
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
            if (body instanceof java.util.HashMap || body instanceof groovy.json.internal.LazyMap) {
            	//poll response
                def devices = getChildDevices()
                for (device in body) {
                    def d = devices.find{it.deviceNetworkId == "${app.id}/${getDeviceType(device.value.type)}/${device.key}"}
                    if (d) {
						def deviceType = getDeviceType(device.value.type)
						def api = getApi(deviceType)
                        if (device.value.state?.reachable || deviceType == "groups") {
							sendEvent(d.deviceNetworkId, [name: "reachable", value: device.value[api].reachable])
							sendEvent(d.deviceNetworkId, [name: "switch", value: device.value[api].on ? "on" : "off"])
                            sendEvent(d.deviceNetworkId, [name: "level", value: hueBritoST(device.value[api].bri)])
                            if (device.value[api].sat) {
                                def hue = Math.min(Math.round(device.value[api].hue * 100 / 65535), 65535) as int
                                def sat = Math.round(device.value[api].sat * 100 / 255) as int
                                def hex = colorUtil.hslToHex(hue, sat)
                                sendEvent(d.deviceNetworkId, [name: "color", value: hex])
                                sendEvent(d.deviceNetworkId, [name: "hue", value: hue])
                                sendEvent(d.deviceNetworkId, [name: "saturation", value: sat])
								sendEvent(d.deviceNetworkId, [name: "xy", value: device.value[api].xy])
								sendEvent(d.deviceNetworkId, [name: "effect", value: device.value[api].effect])
								sendEvent(d.deviceNetworkId, [name: "colormode", value: device.value[api].colormode])
                            }
							if (device.value[api].ct) { sendEvent(d.deviceNetworkId, [name: "colorTemperature", value: Math.round(1000000 / device.value[api].ct)]) }
                        } else {
							sendEvent(d.deviceNetworkId, [name: "reachable", value: "false"])
							sendEvent(d.deviceNetworkId, [name: "switch", value: "off"])
                            sendEvent(d.deviceNetworkId, [name: "level", value: 100])
                            if (device.value[api].sat) {
                                def hue = 8
                                def sat = 18
                                def hex = colorUtil.hslToHex(8, 18)
                                sendEvent(d.deviceNetworkId, [name: "color", value: hex])
                                sendEvent(d.deviceNetworkId, [name: "hue", value: hue])
                                sendEvent(d.deviceNetworkId, [name: "saturation", value: sat])
								sendEvent(d.deviceNetworkId, [name: "effect", value: "none"])
                            }
							if (device.value[api].ct) { sendEvent(d.deviceNetworkId, [name: "colorTemperature", value: 2710 ]) }
                        }
                    }
                }
            }
            else
            { //put response
                def hsl = [:]
                body.each { payload ->
                    log.debug $payload
                    if (payload?.success)
                    {
                        def childDeviceNetworkId = app.id + "/"
                        def eventType
                        body?.success[0].each { k,v ->
                            childDeviceNetworkId += k.split("/")[1] + "/" + k.split("/")[2]
                            if (!hsl[childDeviceNetworkId]) hsl[childDeviceNetworkId] = [:]
                            eventType = k.split("/")[4]
                            log.debug "eventType: $eventType"
                            switch(eventType) {
                                case "on":
                                    sendEvent(childDeviceNetworkId, [name: "switch", value: (v == true) ? "on" : "off"])
                                    break
                                case "bri":
                                    sendEvent(childDeviceNetworkId, [name: "level", value: hueBritoST(v)])
                                    break
                                case "sat":
                                    hsl[childDeviceNetworkId].saturation = Math.round(v * 100 / 255) as int
                                    break
                                case "hue":
                                    hsl[childDeviceNetworkId].hue = Math.min(Math.round(v * 100 / 65535), 65535) as int
                                    break
								case "xy":
                                    sendEvent(childDeviceNetworkId, [name: "xy", value: v])
                                    break
								case "ct":
									sendEvent(childDeviceNetworkId, [name: "colorTemperature", value: Math.round(1000000 / v)])
									break
								case "effect":
                                    sendEvent(childDeviceNetworkId, [name: "effect", value: v])
                                    break
								case "colormode":
                                    sendEvent(childDeviceNetworkId, [name: "colormode", value: v])
                                    break
								case "reachable":
                                    sendEvent(childDeviceNetworkId, [name: "reachable", value: v])
                                    break
                            }
                        }

                    }
                    else if (payload.error)
                    {
                        log.debug "JSON error - ${body?.error}"
                    }

                }

                hsl.each { childDeviceNetworkId, hueSat ->
                    if (hueSat.hue && hueSat.saturation) {
                        def hex = colorUtil.hslToHex(hueSat.hue, hueSat.saturation)
                        log.debug "sending ${hueSat} for ${childDeviceNetworkId} as ${hex}"
                        sendEvent(hsl.childDeviceNetworkId, [name: "color", value: hex])
                    }
                }

            }
    	}
	} else {
		log.debug "parse - got something other than headers,body..."
		return []
	}
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

def on(childDevice, transitionTime, deviceType) {
	log.debug "Executing 'on'"
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [on: true, transitiontime: transitionTime * 10])
    return "Device is On"
}

def off(childDevice, transitionTime, deviceType) {
	log.debug "Executing 'off'"
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [on: false]) //, transitiontime: transitionTime * 10]) TODO: uncomment when bug resolved. See http://www.developers.meethue.com/content/using-transitiontime-onfalse-resets-bri-1
    return "Device is Off"
}

def setLevel(childDevice, percent, transitionTime, deviceType) {
	log.debug "Executing 'setLevel'"
    def level = stLeveltoHue(percent)
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [bri: level, on: percent > 0, transitiontime: transitionTime * 10])
}

def setSaturation(childDevice, percent, transitionTime, deviceType) {
	log.debug "Executing 'setSaturation($percent)'"
	def level = Math.min(Math.round(percent * 255 / 100), 255)
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [sat: level, transitiontime: transitionTime * 10])
}

def setHue(childDevice, percent, transitionTime, deviceType) {
	log.debug "Executing 'setHue($percent)'"
	def level =	Math.min(Math.round(percent * 65535 / 100), 65535)
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [hue: level, transitiontime: transitionTime * 10])
}

def setColorTemperature(childDevice, huesettings, transitionTime, deviceType) {
	log.debug "Executing 'setColorTemperature($huesettings)'"
	if(huesettings <= 100 && huesettings >= 0) { //Workaround for broken controlTile range parameter
		huesettings = (huesettings == 0) ? 1 : huesettings
		def stMax = 100
		def stMin = 0
		def stRange = (stMax - stMin)
		def hueMax = 6500
		def hueMin = 2000
		def hueRange = (hueMax - hueMin)
		huesettings = (((huesettings - stMin) * hueRange) / stRange) + hueMin
	}
	def ct = Math.round(1000000 / huesettings) as Integer
	def value = [ct: ct, on: true, transitiontime: transitionTime * 10]
	log.trace "sending command $value"
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", value)
}

def setColor(childDevice, huesettings, deviceType) {
	log.debug "Executing 'setColor($huesettings)'"

    def value = [:]
    def hue = null
    def sat = null
    def xy = null

    if (huesettings.hex != null) {
        value.xy = getHextoXY(huesettings.hex)
    } else {
        if (huesettings.hue != null)
            value.hue = Math.min(Math.round(huesettings.hue * 65535 / 100), 65535)
        if (huesettings.saturation != null)
            value.sat = Math.min(Math.round(huesettings.saturation * 255 / 100), 255)
		if (huesettings.xy != null)
			value.xy = huesettings.xy
    }

    // Default behavior is to turn light on
    value.on = true

    if (huesettings.level != null) {
        if (huesettings.level <= 0)
            value.on = false
        value.bri = stLeveltoHue(huesettings.level)
    }
    value.alert = huesettings.alert ? huesettings.alert : "none"
    value.transitiontime = huesettings.transitiontime ? huesettings.transitiontime : 10

    // Make sure to turn off light if requested
    if (huesettings.switch == "off")
        value.on = false

    log.debug "sending command $value"
    put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", value)
    return "Color set to $value"
}

def nextLevel(childDevice) {
    def level = device.latestValue("level") as Integer ?: 0
    if (level < 100) {
        level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
    } else {
        level = 25
    }
    setLevel(childDevice,level)
}

def setAlert(childDevice, alert, deviceType) {
	log.debug "Executing 'setAlert($alert)'"
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [alert: alert])
}

def setEffect(childDevice, effect, deviceType) {
	log.debug "Executing 'setEffect($effect)'"
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [effect: effect])
}

def bri_inc(childDevice, value, deviceType) {
	log.debug "Executing 'bri_inc($value)'"
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [bri_inc: value])
}

def sat_inc(childDevice, value, deviceType) {
	log.debug "Executing 'sat_inc($value)'"
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [sat_inc: value])
}

def hue_inc(childDevice, value, deviceType) {
	log.debug "Executing 'hue_inc($value)'"
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [hue_inc: value])
}

def ct_inc(childDevice, value, deviceType) {
	log.debug "Executing 'ct_inc($value)'"
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [ct_inc: value])
}

def xy_inc(childDevice, values, deviceType) {
	log.debug "Executing 'xy_inc($values)'"
	put("${deviceType}/${getId(childDevice)}/${getApi(deviceType)}", [xy_inc: values])
}

private getId(childDevice) {
	if (childDevice.device?.deviceNetworkId?.startsWith("HUE")) {
		return childDevice.device?.deviceNetworkId[3..-1]
	}
	else {
		return childDevice.device?.deviceNetworkId.split("/")[-1]
	}
}

private poll() {
	def host = getBridgeIP()
	def uris = ["/api/${state.username}/lights/", "/api/${state.username}/groups/"]
	for (uri in uris) {
		log.debug "GET: $host$uri"
		sendHubCommand(new physicalgraph.device.HubAction("""GET ${uri} HTTP/1.1
HOST: ${host}

""", physicalgraph.device.Protocol.LAN, selectedHue))
	}
}

private put(path, body) {
	def host = getBridgeIP()
	def uri = "/api/${state.username}/$path"
	def bodyJSON = new groovy.json.JsonBuilder(body).toString()
	def length = bodyJSON.getBytes().size().toString()

	log.debug "PUT:  $host$uri"
	log.debug "BODY: ${bodyJSON}"

	sendHubCommand(new physicalgraph.device.HubAction("""PUT $uri HTTP/1.1
HOST: ${host}
Content-Length: ${length}

${bodyJSON}
""", physicalgraph.device.Protocol.LAN, "${selectedHue}"))

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

def getHextoXY(String colorStr) {
    // For the hue bulb the corners of the triangle are:
    // -Red: 0.675, 0.322
    // -Green: 0.4091, 0.518
    // -Blue: 0.167, 0.04

    def cred = Integer.valueOf( colorStr.substring( 1, 3 ), 16 )
    def cgreen = Integer.valueOf( colorStr.substring( 3, 5 ), 16 )
    def cblue = Integer.valueOf( colorStr.substring( 5, 7 ), 16 )

    double[] normalizedToOne = new double[3];
    normalizedToOne[0] = (cred / 255);
    normalizedToOne[1] = (cgreen / 255);
    normalizedToOne[2] = (cblue / 255);
    float red, green, blue;

    // Make red more vivid
    if (normalizedToOne[0] > 0.04045) {
        red = (float) Math.pow(
                (normalizedToOne[0] + 0.055) / (1.0 + 0.055), 2.4);
    } else {
        red = (float) (normalizedToOne[0] / 12.92);
    }

    // Make green more vivid
    if (normalizedToOne[1] > 0.04045) {
        green = (float) Math.pow((normalizedToOne[1] + 0.055) / (1.0 + 0.055), 2.4);
    } else {
        green = (float) (normalizedToOne[1] / 12.92);
    }

    // Make blue more vivid
    if (normalizedToOne[2] > 0.04045) {
        blue = (float) Math.pow((normalizedToOne[2] + 0.055) / (1.0 + 0.055), 2.4);
    } else {
        blue = (float) (normalizedToOne[2] / 12.92);
    }

    float X = (float) (red * 0.649926 + green * 0.103455 + blue * 0.197109);
    float Y = (float) (red * 0.234327 + green * 0.743075 + blue * 0.022598);
    float Z = (float) (red * 0.0000000 + green * 0.053077 + blue * 1.035763);

    float x = (X != 0 ? X / (X + Y + Z) : 0);
    float y = (Y != 0 ? Y / (X + Y + Z) : 0);

    float[] xy = new float[2];
	xy[0] = x.round(4);
    xy[1] = y.round(4);
    return xy;
}

def getXYtoHex(xy, level) {
	float x = xy[0]
	float y = xy[1]
	float z = 1.0 - x - y
	float Y = level
	float X = (Y / y) * x
	float Z = (Y / y) * z

	float r = X * 1.612 - Y * 0.203 - Z * 0.302
	float g = -X * 0.509 + Y * 1.412 + Z * 0.066
	float b = X * 0.026 - Y * 0.072 + Z * 0.962

	r = r <= 0.0031308 ? (float) 12.92 * r : (float) (1.0 + 0.055) * Math.pow(r, (1.0 / 2.4)) - 0.055
	g = g <= 0.0031308 ? (float) 12.92 * g : (float) (1.0 + 0.055) * Math.pow(g, (1.0 / 2.4)) - 0.055
	b = b <= 0.0031308 ? (float) 12.92 * b : (float) (1.0 + 0.055) * Math.pow(b, (1.0 / 2.4)) - 0.055

	r = Math.round(r * 255)
	g = Math.round(g * 255)
	b = Math.round(b * 255)

	return "#" + Integer.toHexString(r).padLeft(2,'0') + Integer.toHexString(g).padLeft(2,'0') + Integer.toHexString(b).padLeft(2,'0')
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

def convertDeviceListToMap() {
	try {
		if (state.devices instanceof java.util.List) {
			def map = [:]
			state.devices.unique {it.id}.each { device ->
				map << ["${device.id}":["id":device.id, "name":device.name, "type": device.type, "modelid": device.modelid, "hub":device.hub]]
			}
			state.devices = map
		}
	}
	catch(Exception e) {
		log.error "Caught error attempting to convert device list to map: $e"
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

def getApi(deviceType) {
	if (deviceType == "lights") { return "state" }
	else if (deviceType == "groups") { return "action" }
}

def getSelectedTransition() {
	return settings.selectedTransition
}

def getHandlerType() {
	if(settings.circadianDaylightIntegration == true) { return " -CD-" }
	else { return "" }
}

def hueBritoST(bri) {
	def hueRange = (254 - 1)
	def stRange = (100 - 1)
	return Math.round((((bri - 1) * stRange) / hueRange) + 1)
}

def stLeveltoHue(level) {
	def stRange = (100 - 1)
	def hueRange = (254 - 1)
	return Math.round((((level - 1) * hueRange) / stRange) + 1)
}