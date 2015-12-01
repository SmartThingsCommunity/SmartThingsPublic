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
 *      http://www.apache.org/licenses/LICENSE-2.0
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
	page(name:"bridgeBtnPush", title:"Linking with your Hue", content:"bridgeLinking", refreshTimeout:5)
	page(name:"bulbDiscovery", title:"Hue Device Setup", content:"bulbDiscovery", refreshTimeout:5)
}

def mainPage() {
	if(canInstallLabs()) {
		def bridges = bridgesDiscovered()
		if (state.username && bridges) {
			return bulbDiscovery()
		} else {
			return bridgeDiscovery()
		}
	} else {
		def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

		return dynamicPage(name:"bridgeDiscovery", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
			section("Upgrade") {
				paragraph "$upgradeNeeded"
			}
		}
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

	if (numFound == 0 && state.bridgeRefreshCount > 25) {
    	log.trace "Cleaning old bridges memory"
    	state.bridges = [:]
        state.bridgeRefreshCount = 0
        app.updateSetting("selectedHue", "")
    }    

	subscribe(location, null, locationHandler, [filterEvents:false])

	//bridge discovery request every 15 //25 seconds
	if((bridgeRefreshCount % 5) == 0) {
		discoverBridges()
	}

	//setup.xml request every 3 seconds except on discoveries
	if(((bridgeRefreshCount % 1) == 0) && ((bridgeRefreshCount % 5) != 0)) {
		verifyHueBridges()
	}

	return dynamicPage(name:"bridgeDiscovery", title:"Discovery Started!", nextPage:"bridgeBtnPush", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Hue Bridge. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedHue", "enum", required:false, title:"Select Hue Bridge (${numFound} found)", multiple:false, options:options
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
    def hueimage = null
	if (selectedHue) {
		paragraphText = "Press the button on your Hue Bridge to setup a link. "
        hueimage = "http://huedisco.mediavibe.nl/wp-content/uploads/2013/09/pair-bridge.png"
    } else {
    	paragraphText = "You haven't selected a Hue Bridge, please Press \"Done\" and select one before clicking next."
        hueimage = null
    }
	if (state.username) { //if discovery worked
		nextPage = "bulbDiscovery"
		title = "Success!"
		paragraphText = "Linking to your hub was a success! Please click 'Next'!"
        hueimage = null
	}

	if((linkRefreshcount % 2) == 0 && !state.username) {
		sendDeveloperReq()
	}

	return dynamicPage(name:"bridgeBtnPush", title:title, nextPage:nextPage, refreshInterval:refreshInterval) {
		section("") {
			paragraph """${paragraphText}"""
            if (hueimage != null)
            	image "${hueimage}"
		}
	}
}

def bulbDiscovery() {
	int bulbRefreshCount = !state.bulbRefreshCount ? 0 : state.bulbRefreshCount as int
	state.bulbRefreshCount = bulbRefreshCount + 1
	def refreshInterval = 3
	state.inBulbDiscovery = true
	def bridge = null
	if (selectedHue) {
        bridge = getChildDevice(selectedHue)
        subscribe(bridge, "bulbList", bulbListData)
	}
    state.bridgeRefreshCount = 0
	def bulboptions = bulbsDiscovered() ?: [:]
	def numFound = bulboptions.size() ?: 0
    if (numFound == 0)
    	app.updateSetting("selectedBulbs", "") 
        
	if((bulbRefreshCount % 5) == 0) {
		discoverHueBulbs()
	}

	return dynamicPage(name:"bulbDiscovery", title:"Bulb Discovery Started!", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
		section("Please wait while we discover your Hue Bulbs. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedBulbs", "enum", required:false, title:"Select Hue Bulbs (${numFound} found)", multiple:true, options:bulboptions
		}
		section {				
			def title = getBridgeIP() ? "Hue bridge (${getBridgeIP()})" : "Find bridges"
			href "bridgeDiscovery", title: title, description: "", state: selectedHue ? "complete" : "incomplete", params: [override: true]

		}
	}
}

private discoverBridges() {
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:basic:1", physicalgraph.device.Protocol.LAN))
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
		body: [devicetype: "$token-0", username: "$token-0"]], "${selectedHue}"))
}

private discoverHueBulbs() {
    def host = getBridgeIP()
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/api/${state.username}/lights",
		headers: [
			HOST: host
		]], "${selectedHue}"))
}

private verifyHueBridge(String deviceNetworkId, String host) {
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/description.xml",
		headers: [
			HOST: host
		]], deviceNetworkId))
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

def bulbListData(evt) {
	state.bulbs = evt.jsonData
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
    unschedule()
	unsubscribe() 
	initialize()
}

def initialize() {
	log.debug "Initializing"  
    unsubscribe(bridge)
    state.inBulbDiscovery = false
    state.bridgeRefreshCount = 0
    state.bulbRefreshCount = 0
	if (selectedHue) {
   		addBridge()
        addBulbs()
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

// Handles events to add new bulbs
def bulbListHandler(hub, data = "") {
	def msg = "Bulbs list not processed. Only while in settings menu."
    def bulbs = [:]
	if (state.inBulbDiscovery) {       
        def logg = ""
        log.trace "Adding bulbs to state..."
        state.bridgeProcessedLightList = true
        def object = new groovy.json.JsonSlurper().parseText(data)   
        object.each { k,v ->
            if (v instanceof Map) 
                bulbs[k] = [id: k, name: v.name, type: v.type, hub:hub]
        }
    }  
    def bridge = null
	if (selectedHue) 
        bridge = getChildDevice(selectedHue)
    bridge.sendEvent(name: "bulbList", value: hub, data: bulbs, isStateChange: true, displayed: false)
    msg = "${bulbs.size()} bulbs found. ${bulbs}"
	return msg
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
                    if (newHueBulb?.value?.type?.equalsIgnoreCase("Dimmable light") ) {
                        d = addChildDevice("smartthings", "Hue Lux Bulb", dni, newHueBulb?.value.hub, ["label":newHueBulb?.value.name])
                    } else {
                        d = addChildDevice("smartthings", "Hue Bulb", dni, newHueBulb?.value.hub, ["label":newHueBulb?.value.name])
                    }
                } else {
                	log.debug "$dni in not longer paired to the Hue Bridge or ID changed"
                }
			} else { 
            	//backwards compatable
				newHueBulb = bulbs.find { (app.id + "/" + it.id) == dni }
				d = addChildDevice("smartthings", "Hue Bulb", dni, newHueBulb?.hub, ["label":newHueBulb?.name])
			}

			log.debug "created ${d.displayName} with id $dni"
			d.refresh()
		} else {
			log.debug "found ${d.displayName} with id $dni already exists, type: '$d.typeName'"
			if (bulbs instanceof java.util.Map) {
            	def newHueBulb = bulbs.find { (app.id + "/" + it.value.id) == dni }
				if (newHueBulb?.value?.type?.equalsIgnoreCase("Dimmable light") && d.typeName == "Hue Bulb") {
					d.setDeviceType("Hue Lux Bulb")
				}
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
                        if (oldDNI == selectedHue)
                        	app.updateSetting("selectedHue", newDNI)
                        newbridge = false 
                    }
                }    
            }  
        	if (newbridge) {
				d = addChildDevice("smartthings", "Hue Bridge", selectedHue, vbridge.value.hub)
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
                            if (oldDNI == selectedHue)
                                app.updateSetting("selectedHue", newDNI)
                            doDeviceSync()
                        }
                    }
                }
			} else {
            	if (d.getDeviceDataByName("networkAddress"))
                	networkAddress = d.getDeviceDataByName("networkAddress")
            	else
                	networkAddress = d.latestState('networkAddress').stringValue
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
		} else if(headerString?.contains("json")) {
            log.trace "description.xml response (application/json)"
			def body = new groovy.json.JsonSlurper().parseText(parsedEvent.body)
			if (body.success != null) {
				if (body.success[0] != null) {
					if (body.success[0].username)
						state.username = body.success[0].username
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
						bulbs[k] = [id: k, name: v.name, type: v.type, hub:parsedEvent.hub]
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
	convertBulbListToMap()
	poll()
    try {
		subscribe(location, null, locationHandler, [filterEvents:false])
    } catch (all) {
    	log.trace "Subscription already exist"
 	}
	discoverBridges()
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
            if (body instanceof java.util.HashMap) { 
            	//poll response
                def bulbs = getChildDevices()
                for (bulb in body) {
                    def d = bulbs.find{it.deviceNetworkId == "${app.id}/${bulb.key}"}    
                    if (d) {
                        if (bulb.value.state?.reachable) {
                            sendEvent(d.deviceNetworkId, [name: "switch", value: bulb.value?.state?.on ? "on" : "off"])
                            sendEvent(d.deviceNetworkId, [name: "level", value: Math.round(bulb.value.state.bri * 100 / 255)])
                            if (bulb.value.state.sat) {
                                def hue = Math.min(Math.round(bulb.value.state.hue * 100 / 65535), 65535) as int
                                def sat = Math.round(bulb.value.state.sat * 100 / 255) as int
                                def hex = colorUtil.hslToHex(hue, sat)
                                sendEvent(d.deviceNetworkId, [name: "color", value: hex])
                                sendEvent(d.deviceNetworkId, [name: "hue", value: hue])
                                sendEvent(d.deviceNetworkId, [name: "saturation", value: sat])
                            }
                        } else {
                            sendEvent(d.deviceNetworkId, [name: "switch", value: "off"])
                            sendEvent(d.deviceNetworkId, [name: "level", value: 100])                     
                            if (bulb.value.state.sat) {
                                def hue = 23
                                def sat = 56
                                def hex = colorUtil.hslToHex(23, 56)
                                sendEvent(d.deviceNetworkId, [name: "color", value: hex])
                                sendEvent(d.deviceNetworkId, [name: "hue", value: hue])
                                sendEvent(d.deviceNetworkId, [name: "saturation", value: sat])                               
                            }    
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
                            childDeviceNetworkId += k.split("/")[2]
                            if (!hsl[childDeviceNetworkId]) hsl[childDeviceNetworkId] = [:]
                            eventType = k.split("/")[4]
                            log.debug "eventType: $eventType"
                            switch(eventType) {
                                case "on":
                                    sendEvent(childDeviceNetworkId, [name: "switch", value: (v == true) ? "on" : "off"])
                                    break
                                case "bri":
                                    sendEvent(childDeviceNetworkId, [name: "level", value: Math.round(v * 100 / 255)])
                                    break
                                case "sat":
                                    hsl[childDeviceNetworkId].saturation = Math.round(v * 100 / 255) as int
                                    break
                                case "hue":
                                    hsl[childDeviceNetworkId].hue = Math.min(Math.round(v * 100 / 65535), 65535) as int
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

def on(childDevice) {
	log.debug "Executing 'on'"
	put("lights/${getId(childDevice)}/state", [on: true])
    return "Bulb is On"
}

def off(childDevice) {
	log.debug "Executing 'off'"
	put("lights/${getId(childDevice)}/state", [on: false])
    return "Bulb is Off"
}

def setLevel(childDevice, percent) {
	log.debug "Executing 'setLevel'"
    def level 
    if (percent == 1) level = 1 else level = Math.min(Math.round(percent * 255 / 100), 255)
	put("lights/${getId(childDevice)}/state", [bri: level, on: percent > 0])
}

def setSaturation(childDevice, percent) {
	log.debug "Executing 'setSaturation($percent)'"
	def level = Math.min(Math.round(percent * 255 / 100), 255)
	put("lights/${getId(childDevice)}/state", [sat: level])
}

def setHue(childDevice, percent) {
	log.debug "Executing 'setHue($percent)'"
	def level =	Math.min(Math.round(percent * 65535 / 100), 65535)
	put("lights/${getId(childDevice)}/state", [hue: level])
}

def setColor(childDevice, huesettings) {
	log.debug "Executing 'setColor($huesettings)'"
	def hue = Math.min(Math.round(huesettings.hue * 65535 / 100), 65535)
	def sat = Math.min(Math.round(huesettings.saturation * 255 / 100), 255)
    def alert = huesettings.alert ? huesettings.alert : "none"
    def transition = huesettings.transition ? huesettings.transition : 4

	def value = [sat: sat, hue: hue, alert: alert, transitiontime: transition]
	if (huesettings.level != null) {
        if (huesettings.level == 1) value.bri = 1 else value.bri = Math.min(Math.round(huesettings.level * 255 / 100), 255)
		value.on = value.bri > 0
	}

	if (huesettings.switch) {
		value.on = huesettings.switch == "on"
	}

	log.debug "sending command $value"
	put("lights/${getId(childDevice)}/state", value)
}

def nextLevel(childDevice) {
	def level = device.latestValue("level") as Integer ?: 0
	if (level < 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(childDevice,level)
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
	def uri = "/api/${state.username}/lights/"
    try {
		sendHubCommand(new physicalgraph.device.HubAction("""GET ${uri} HTTP/1.1
HOST: ${host}

""", physicalgraph.device.Protocol.LAN, selectedHue))
	} catch (all) {
        log.warn "Parsing Body failed - trying again..."
        doDeviceSync()
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

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

def convertBulbListToMap() {
	try {
		if (state.bulbs instanceof java.util.List) {
			def map = [:]
			state.bulbs.unique {it.id}.each { bulb ->
				map << ["${bulb.id}":["id":bulb.id, "name":bulb.name, "hub":bulb.hub]]
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

private Boolean canInstallLabs() {
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware) {
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions() {
	return location.hubs*.firmwareVersionString.findAll { it }
}
