/**
 *  Hue B Smart
 *
 *  Copyright 2016 Anthony Pastor
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
 *	(beta) version .9
 *	(beta) version .9a - added submitOnChange() to bulb, group, and scene selection pages
 *  (beta) version .9b - added Hue Ambience bulbs (thanks @tmleafs!); fixed scaleLevel; conformed DTHs 
 *
 */
 
definition(
        name: "Hue B Smart",
        namespace: "info_fiend",
        author: "anthony pastor",
        description: "The Smartest Hue Control App for SmartThings - total control of bulbs, scenes, groups, and schedules",
        category: "My Apps",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png",
        singleInstance: true
)

preferences {
    page(name:"Bridges", content: "bridges")
    page(name:"linkButton", content: "linkButton")
    page(name:"linkBridge", content: "linkBridge")
    page(name:"manageBridge", content: "manageBridge")
	page(name:"chooseBulbs", content: "chooseBulbs")
 	page(name:"chooseScenes", content: "chooseScenes")
 	page(name:"chooseGroups", content: "chooseGroups")
    page(name:"chooseSchedules", content: "chooseSchedules")
    page(name:"createQuickfixSch", content: "createQuickfixSch")
    page(name:"enableQF", content: "enableQF")
//    page(name:"deleteQuickfixSch", content: "deleteQuickfixSch")
    page(name:"deleteBridge", content: "deleteBridge")
    
    page(name:"defaultTransition", title:"Default Transition", content:"defaultTransition", refreshTimeout:5)
}

def manageBridge(params) {

/**    state.selectedScene = []
	state.selectedGroup = []
	state.availableScenes = []
	state.availableGroups = []        	
**/
	state.newSchedule = [:]

    if (params.mac) {
        state.params = params;
    } else {
        params = state.params;
    }

    def bridge = getBridge(params.mac)
    def ip = convertHexToIP(bridge.value.networkAddress)
    def mac = params.mac
    def bridgeDevice = getChildDevice(mac)
    def title = "${bridgeDevice.label} ${ip}"
    def refreshInterval = 2

    if (!bridgeDevice) {
        log.debug("Bridge device not found?")
        /* Error, bridge device doesn't exist? */
        return
    }
    
	if (params.refreshItems) {
    	params.refreshItems = false
		bridge.value.itemsDiscovered = false
    	state.itemDiscoveryComplete = false        
    }
    
    int itemRefreshCount = !state.itemRefreshCount ? 0 : state.itemRefreshCount as int
    if (!state.itemDiscoveryComplete) {
        state.itemRefreshCount = itemRefreshCount + 1
    }

    // resend request if we haven't received a response in 10 seconds 
    if (!bridge.value.itemsDiscovered && ((!state.inItemDiscovery && !state.itemDiscoveryComplete) || (state.itemRefreshCount == 6))) {
		unschedule() 
        state.itemDiscoveryComplete = false
        state.inItemDiscovery = mac
        bridgeDevice.discoverItems()
        state.itemRefreshCount = 0
        return dynamicPage(name:"manageBridge", title: "Manage bridge ${ip}", refreshInterval: refreshInterval, install: false) {
        	section("Discovering bulbs, scenes, schedules, and groups...") {
				href(name: "Delete Bridge", page:"deleteBridge", title:"", description:"Delete bridge ${ip} (and devices)", params: [mac: mac])
			}
		}
    } else if (state.inItemDiscovery) {
        return dynamicPage(name:"manageBridge", title: "Manage bridge ${ip}", refreshInterval: refreshInterval, install: false) {
            section("Discovering bulbs, scenes, schedules, and groups...") {
				href(name: "Delete Bridge", page:"deleteBridge", title:"", description:"Delete bridge ${ip} (and devices)", params: [mac: mac])
            }
        }
    }
	/* discovery complete, re-enable device sync */
	runEvery5Minutes(doDeviceSync)
    
    def numBulbs = bridge.value.bulbs.size() ?: 0
    def numScenes = bridge.value.scenes.size() ?: 0
    def numGroups = bridge.value.groups.size() ?: 0
    def numSchedules = bridge.value.schedules?.size() ?: 0

    dynamicPage(name:"manageBridge", install: true) {
        section("Manage Bridge ${ip}") {
			href(name:"Refresh items", page:"manageBridge", title:"Refresh discovered items", description: "", params: [mac: mac, refreshItems: true])
            paragraph ""
			href(name:"Choose Bulbs", page:"chooseBulbs", description:"", title: "Choose Bulbs (${numBulbs} found)", params: [mac: mac])
            href(name:"Choose Scenes", page:"chooseScenes", description:"", title: "Choose Scenes (${numScenes} found)", params: [mac: mac])
			href(name:"Choose Groups", page:"chooseGroups", description:"", title: "Choose Groups (${numGroups} found)", params: [mac: mac])
            paragraph ""
            href(name:"Create Quick Fixes", page:"chooseSchedules", description:"", title: "Choose and Modify Quick Fixes (${numSchedules} found)", params: [mac: mac])
            paragraph ""
            href(name: "Delete Bridge", page:"deleteBridge", title:"Delete bridge ${ip}", description: "", params: [mac: mac])
            href(name:"Back", page:"Bridges", title:"Back to main page", description: "")
		}
    }
}

def linkBridge() {
    state.params.done = true
    log.debug "linkBridge"
    dynamicPage(name:"linkBridge") {
        section() {
            getLinkedBridges() << state.params.mac
            paragraph "Linked! Please tap Done."
        }
    }
}

def linkButton(params) {
    /* if the user hit the back button, use saved parameters as the passed ones no longer good
     * also uses state.params to pass these on to the next page
     */
    if (params.mac) {
        state.params = params;
    } else {
        params = state.params;
    }

    int linkRefreshcount = !state.linkRefreshcount ? 0 : state.linkRefreshcount as int
    state.linkRefreshcount = linkRefreshcount + 1
    def refreshInterval = 3

    params.linkingBridge = true
    if (!params.linkDone) {
        if ((linkRefreshcount % 2) == 0) {
            sendDeveloperReq("${params.ip}:80", params.mac)
        }
        log.debug "linkButton ${params}"
        dynamicPage(name: "linkButton", refreshInterval: refreshInterval, nextPage: "linkButton") {
            section("Hue Bridge ${params.ip}") {
                paragraph "Please press the link button on your Hue bridge."
                image "http://www.developers.meethue.com/sites/default/files/smartbridge.jpg"
            }
            section() {
                href(name:"Cancel", page:"Bridges", title: "", description: "Cancel")
            }
        }
    } else {
        /* link success! create bridge device */
        log.debug "Bridge linked!"
        log.debug("ssdp ${params.ssdpUSN}")
        log.debug("username ${params.username}")
        
        def bridge = getUnlinkedBridges().find{it?.key?.contains(params.ssdpUSN)}
        log.debug("bridge ${bridge}")
        def d = addChildDevice("info_fiend", "Hue B Smart Bridge", bridge.value.mac, bridge.value.hub, [label: "Hue B Smart Bridge (${params.ip})"])
		
        d.sendEvent(name: "networkAddress", value: params.ip)
        d.sendEvent(name: "serialNumber", value: bridge.value.serialNumber)
        d.sendEvent(name: "username", value: params.username)
		state.user = params.username
        subscribe(d, "itemDiscovery", itemDiscoveryHandler)

        params.linkDone = false
        params.linkingBridge = false

        bridge.value << ["bulbs" : [:], "groups" : [:], "scenes" : [:], "schedules" : [:]]
        getLinkedBridges() << bridge
        log.debug "Bridge added to linked list."
        getUnlinkedBridges().remove(params.ssdpUSN)
        log.debug "Removed bridge from unlinked list."

        dynamicPage(name: "linkButton", nextPage: "Bridges") {
            section("Hue Bridge ${params.ip}") {
                paragraph "Successfully linked Hue Bridge! Please tap Next."
            }
        }
    }
}

def getLinkedBridges() {
    state.linked_bridges = state.linked_bridges ?: [:]
}

def getUnlinkedBridges() {
    state.unlinked_bridges = state.unlinked_bridges ?: [:]
}

def getVerifiedBridges() {
    getUnlinkedBridges().findAll{it?.value?.verified == true}
}

def getBridgeBySerialNumber(serialNumber) {
    def b = getUnlinkedBridges().find{it?.value?.serialNumber == serialNumber}
    if (!b) {
        return getLinkedBridges().find{it?.value?.serialNumber == serialNumber}
    } else {
        return b
    }
}

def getBridge(mac) {
    def b = getUnlinkedBridges().find{it?.value?.mac == mac}
    if (!b) {
        return getLinkedBridges().find{it?.value?.mac == mac}
    } else {
        return b
    }
}

def bridges() {
    /* Prevent "Unexpected Error has occurred" if the user hits the back button before actually finishing an install.
     * Weird SmartThings bug
     */
    if (!state.installed) {
        return dynamicPage(name:"Bridges", title: "Initial installation", install:true, uninstall:true) {
            section() {
                paragraph "For initial installation, please tap Done, then proceed to Menu -> SmartApps -> Hue B Smart."
            }
        }
    }

    /* clear temporary stuff from other pages */
    state.params = [:]
    state.inItemDiscovery = null
    state.itemDiscoveryComplete = false
    state.numDiscoveryResponses = 0
    state.creatingDevices = false

    int bridgeRefreshCount = !state.bridgeRefreshCount ? 0 : state.bridgeRefreshCount as int
    state.bridgeRefreshCount = bridgeRefreshCount + 1
    def refreshInterval = 3

    if (!state.subscribed) {
        subscribe(location, null, locationHandler, [filterEvents:false])
        state.subscribed = true
    }

    // Send bridge discovery request every 15 seconds
    if ((state.bridgeRefreshCount % 5) == 1) {
        discoverHueBridges()
        log.debug "Bridge discovery sent."
    } else {
        // if we're not sending bridge discovery, verify bridges instead
        verifyHueBridges()
    }

    dynamicPage(name:"Bridges", refreshInterval: refreshInterval, install: true, uninstall: true) {
        section("Linked Bridges") {
            getLinkedBridges().sort { it.value.name }.each {
                def ip = convertHexToIP(it.value.networkAddress)
                def mac = "${it.value.mac}"
                state.mac = mac
                def title = "Hue Bridge ${ip}"
                href(name:"manageBridge ${mac}", page:"manageBridge", title: title, description: "", params: [mac: mac])
            }
        }
        section("Unlinked Bridges") {
            paragraph "Searching for Hue bridges. They will appear here when found. Please wait."
            getVerifiedBridges().sort { it.value.name }.each {
                def ip = convertHexToIP(it.value.networkAddress)
                def mac = "${it.value.mac}"
                def title = "Hue Bridge ${ip}"
                href(name:"linkBridge ${mac}", page:"linkButton", title: title, description: "", params: [mac: mac, ip: ip, ssdpUSN: it.value.ssdpUSN])
            }
        }
    }
}

def deleteBridge(params) {

    if (params.mac) {
        state.params = params;
    } else {
        params = state.params;
    }
	
	def bridge = getBridge(params.mac)
    def d = getChildDevice(params.mac)
    log.debug "Deleting bridge ${d.currentValue('networkAddress')} (${params.mac})"
    
	def success = true
	def devices = getChildDevices()
    def text = ""
	devices.each {
    	def devId = it.deviceNetworkId
        if (devId.contains(params.mac) && devId != params.mac) {
        	log.debug "Removing ${devId}"
			try {
    	    	deleteChildDevice(devId)
			} catch (physicalgraph.exception.NotFoundException e) {
	        	log.debug("${devId} already deleted?")
			} catch (physicalgraph.exception.ConflictException e) {
	        	log.debug("${devId} still in use.")
				text = text + "${it.label} is still in use. Remove from any SmartApps or Dashboards, then try again.\n"
		        success = false
			}
        }
	}
    if (success) {
		try {
        	unsubscribe(d)
    		deleteChildDevice(params.mac)
		} catch (physicalgraph.exception.NotFoundException e) {
	    	log.debug("${params.mac} already deleted?")
		} catch (physicalgraph.exception.ConflictException e) {
	    	log.debug("${params.mac} still in use.")
			text = text + "${params.mac} is still in use. Remove from any SmartApps or Dashboards, then try again.\n"
			success = false
		}
	}
    if (success) {
        getLinkedBridges().remove(bridge.key)
        return dynamicPage(name:"deleteBridge", title: "Delete Bridge", install:false, uninstall:false, nexdtPage: "Bridges") {
            section() {
                paragraph "Bridge ${d.currentValue('networkAddress')} and devices successfully deleted."
            	href(name:"Back", page:"Bridges", title:"", description: "Back to main page")
            }
        }    
    } else {
        return dynamicPage(name:"deleteBridge", title: "Delete Bridge", install:false, uninstall:false, nextPage: "Bridges") {
            section() {
                paragraph "Bridge deletion (${d.currentValue('networkAddress')}) failed.\n${text}"
				href(name:"Back", page:"Bridges", title:"", description: "Back to main page")                
            }
        }    
    }
}

def chooseBulbs(params) {

    if (params.mac) {
        state.params = params;
    } else {
        params = state.params;
    }

	def bridge = getBridge(params.mac)
	def addedBulbs = [:]
    def availableBulbs = [:]
    def user = state.username
    
    bridge.value.bulbs.each {
		def devId = "${params.mac}/BULB${it.key}"
		def name = it.value.name
        
		def d = getChildDevice(devId) 
        if (d) {
        	addedBulbs << it
        } else {
        	availableBulbs << it
        }
    }

	if (params.add) {
	    log.debug("Adding ${params.add}")
        def bulbId = params.add
		params.add = null
        def b = bridge.value.bulbs[bulbId]
		def devId = "${params.mac}/BULB${bulbId}"
        if (b.type.equalsIgnoreCase("Dimmable light")) {
			try {
	            def d = addChildDevice("info_fiend", "Hue B Smart Lux Bulb", devId, bridge.value.hub, ["label": b.name])	
				["bri", "reachable", "on"].each { p -> 
					d.updateStatus("state", p, b.state[p])
				}
                d.updateStatus("state", "transitiontime", 4)
                d.updateStatus("state", "colormode", "HS")                
                d.configure()
                addedBulbs[bulbId] = b
                availableBulbs.remove(bulbId)
			} catch (grails.validation.ValidationException e) {
            	log.debug "${devId} already created."
			}    
	    }
		else if (b.type.equalsIgnoreCase("Color Temperature Light")) {
			 try {
                    def d = addChildDevice("info_fiend", "Hue B Smart White Ambiance", devId, bridge.value.hub, ["label": b.name])
				["ct", "bri", "reachable", "on"].each { p ->
                        		d.updateStatus("state", p, b.state[p])
                		}
                d.updateStatus("state", "transitiontime", 4)
		d.configure()
                addedBulbs[bulbId] = b
                availableBulbs.remove(bulbId)
           		} catch (grails.validation.ValidationException e) {
                log.debug "${devId} already created."
            		}
		}
		else {
			try {
            	def d = addChildDevice("info_fiend", "Hue B Smart Bulb", devId, bridge.value.hub, ["label": b.name])
                ["bri", "sat", "reachable", "hue", "on", "xy", "ct", "effect"].each { p ->
                	d.updateStatus("state", p, b.state[p])
                    
				}
                d.updateStatus("state", "colormode", "HS")
                d.updateStatus("state", "transitiontime", 4)
                d.configure()
                addedBulbs[bulbId] = b
                availableBulbs.remove(bulbId)
			} catch (grails.validation.ValidationException e) {
	            log.debug "${devId} already created."
			}
		}
	}
    
    if (params.remove) {
    	log.debug "Removing ${params.remove}"
		def devId = params.remove
        params.remove = null
		def bulbId = devId.split("BULB")[1]
		try {
        	deleteChildDevice(devId)
            addedBulbs.remove(bulbId)
            availableBulbs[bulbId] = bridge.value.bulbs[bulbId]
		} catch (physicalgraph.exception.NotFoundException e) {
        	log.debug("${devId} already deleted.")
            addedBulbs.remove(bulbId)
            availableBulbs[bulbId] = bridge.value.bulbs[bulbId]
		} catch (physicalgraph.exception.ConflictException e) {
        	log.debug("${devId} still in use.")
            errorText = "Bulb ${bridge.value.bulbs[bulbId].name} is still in use. Remove from any SmartApps or Dashboards, then try again."
        }     
    }
    
    dynamicPage(name:"chooseBulbs", title: "", install: true) {
    	section("") {
        	href(name: "manageBridge", page: "manageBridge", title: "Back to Bridge", description: "", params: [mac: params.mac])
	}
    	section("Added Bulbs") {
			addedBulbs.sort{it.value.name}.each { 
				def devId = "${params.mac}/BULB${it.key}"
				def name = it.value.name
				href(name:"${devId}", page:"chooseBulbs", description:"", title:"Remove ${name}", params: [mac: params.mac, remove: devId], submitOnChange: true )
			}
		}
        section("Available Bulbs") {
			availableBulbs.sort{it.value.name}.each { 
				def devId = "${params.mac}/BULB${it.key}"
				def name = it.value.name
				href(name:"${devId}", page:"chooseBulbs", description:"", title:"Add ${name}", params: [mac: params.mac, add: it.key], submitOnChange: true )
			}
        }
    }
}

def chooseScenes(params) {
 
    if (params.mac) {
        state.params = params;
    } else {
        params = state.params;
    }

	def bridge = getBridge(params.mac)
	def addedScenes = [:]
    def availableScenes = [:]
 //   def availableSchedules = [:]
    def user = state.username
    
    bridge.value.scenes.each {
		def devId = "${params.mac}/SCENE${it.key}"
        
		def d = getChildDevice(devId) 
        if (d) {
        	addedScenes << it
        } else {
        	availableScenes << it
        }
    }

    
	if (params.add) {
	    log.debug("Adding ${params.add}")
        def sceneId = params.add
		params.add = null
        def s = bridge.value.scenes[sceneId]
        log.debug "adding scene ${s}.  Are lights assigned? lights = ${s.lights}"
		log.debug "Does scene ${s} have a schedule using it?  scheduleId = ${s.scheduleId}"
        
         
		def devId = "${params.mac}/SCENE${sceneId}"
		try { 
			def d = addChildDevice("info_fiend", "Hue B Smart Scene", devId, bridge.value.hub, ["label": s.name, "type": "scene"])
            d.updateStatus("scene", "lights", s.lights )
            d.updateStatus("scene", "lightDevId", s.sceneLightDevIds )
            if (d.scheduleId) {
	            d.updateStatus("scene", "scheduleId", s.scheduleId )
            }    
            d.configure()
			addedScenes[sceneId] = s
			availableScenes.remove(sceneId)
		} catch (grails.validation.ValidationException e) {
            	log.debug "${devId} already created."
	    }
	}
    
    if (params.remove) {
    	log.debug "Removing ${params.remove}"
		def devId = params.remove
        params.remove = null
		def sceneId = devId.split("SCENE")[1]
        try {
        	deleteChildDevice(devId)
            addedScenes.remove(sceneId)
            availableScenes[sceneId] = bridge.value.scenes[sceneId]
            
		} catch (physicalgraph.exception.NotFoundException e) {
        	log.debug("${devId} already deleted.")
			addedScenes.remove(sceneId)
            availableScenes[sceneId] = bridge.value.scenes[sceneId]
		} catch (physicalgraph.exception.ConflictException e) {
        	log.debug("${devId} still in use.")
            errorText = "Scene ${bridge.value.scenes[sceneId].name} is still in use. Remove from any SmartApps or Dashboards, then try again."
        }
    }
    
    dynamicPage(name:"chooseScenes", title: "", install: true) {
		section("") { 
			href(name: "manageBridge", page: "manageBridge", description: "", title: "Back to Bridge", params: [mac: params.mac])
        }
    	section("Added Scenes") {
			addedScenes.sort{it.value.name}.each { 
				def devId = "${params.mac}/SCENE${it.key}"
				def name = it.value.name
				href(name:"${devId}", page:"chooseScenes", description:"", title:"Remove ${name}", params: [mac: params.mac, remove: devId], submitOnChange: true )
			}
		}
        section("Available Scenes") {
			availableScenes.sort{it.value.name}.each { 
				def devId = "${params.mac}/SCENE${it.key}"
				def name = it.value.name
				href(name:"${devId}", page:"chooseScenes", description:"", title:"Add ${name}", params: [mac: params.mac, add: it.key], submitOnChange: true )
			}
        }
    }
}

def chooseGroups(params) {
	state.groupLights = []
    state.selectedGroup = []
	state.selectedBulbs = []
    state.availableGroups = []
	state.availableBulbs = []

    
    if (params.mac) {
        state.params = params;
    } else {
        params = state.params;
    }

    def errorText = ""

	def bridge = getBridge(params.mac)
	def addedGroups = [:]
    def availableGroups = [:]
    def user = state.user
    log.debug "=================== ${state.user} ================"
    bridge.value.groups.each {
		def devId = "${params.mac}/GROUP${it.key}"
		def name = it.value.name
        
		def d = getChildDevice(devId) 
        if (d) {
        	addedGroups << it
        } else {
        	availableGroups << it
        }
    }

	if (params.add) {
	    log.debug("Adding ${params.add}")
        def groupId = params.add
        log.debug "ADDING GROUP: params.mac = ${params.mac} / groupId = ${groupId}"
		params.add = null
        def g = bridge.value.groups[groupId]
        log.debug "ADDING GROUP: g / bridge.value.groups[groupId] = ${g}.  Are lights assigned? lights = ${g.lights}"
		def devId = "${params.mac}/GROUP${groupId}"
        
        if (g.action.hue) {
			try { 
				def d = addChildDevice("info_fiend", "Hue B Smart Group", devId, bridge.value.hub, ["label": g.name, "type": g.type, "groupType": "Color Group", "allOn": g.all_on, "anyOn": g.any_on])
	    	    log.debug "adding group ${d}.  Are lights assigned? lights = ${g.lights}"     
            	["bri", "sat", "hue", "on", "xy", "ct", "colormode", "effect"].each { p ->
                		d.updateStatus("action", p, g.action[p])                    
				}
    	        d.updateStatus("action", "transitiontime", 4)
        	    d.updateStatus("action", "lights", "${g.lights}")
				d.updateStatus("scene", "lightDevId", "{g.groupLightDevIds}")
	            d.configure()
				addedGroups[groupId] = g
				availableGroups.remove(groupId)
			} catch (grails.validation.ValidationException e) {
    	        	log.debug "${devId} already created."
	    	}
		} else {
			try { 
				def d = addChildDevice("info_fiend", "Hue B Smart Lux Group", devId, bridge.value.hub, ["label": g.name, "type": g.type, "groupType": "Lux Group", "allOn": g.all_on, "anyOn": g.any_on])
	    	    log.debug "adding group ${d}.  Are lights assigned? lights = ${g.lights}"     
            	["bri", "on", "effect"].each { p ->
                		d.updateStatus("action", p, g.action[p])                    
				}
    	        d.updateStatus("action", "transitiontime", 4)
        	    d.updateStatus("action", "lights", "${g.lights}")
				d.updateStatus("scene", "lightDevId", "{g.groupLightDevIds}")
	            d.configure()
				addedGroups[groupId] = g
				availableGroups.remove(groupId)
			} catch (grails.validation.ValidationException e) {
    	        	log.debug "${devId} already created."
	    	}
		}        
    }
    
    if (params.remove) {
    	log.debug "Removing ${params.remove}"
		def devId = params.remove
        params.remove = null
		def groupId = devId.split("GROUP")[1]
		try {
        	deleteChildDevice(devId)
            addedGroups.remove(groupId)
            availableGroups[groupId] = bridge.value.groups[groupId]
		} catch (physicalgraph.exception.NotFoundException e) {
        	log.debug("${devId} already deleted.")
            addedGroups.remove(groupId)
            availableGroups[groupId] = bridge.value.groups[groupId]
		} catch (physicalgraph.exception.ConflictException e) {
        	log.debug("${devId} still in use.")
            errorText = "Group ${bridge.value.groups[groupId].name} is still in use. Remove from any SmartApps or Dashboards, then try again."
        }
    }

    return dynamicPage(name:"chooseGroups", title: "", install:false, uninstall:false, nextPage: "chooseSchedules") {
	    section("") { 
        		href(name: "manageBridge", page: "manageBridge", description: "", title: "Back to Bridge", params: [mac: params.mac], submitOnChange: true )
		}
	    section("Hue Groups Added to SmartThings") {
			addedGroups.sort{it.value.name}.each { 
				def devId = "${params.mac}/GROUP${it.key}"
				def name = it.value.name
				href(name:"${devId}", page:"chooseGroups", description:"", title:"Remove ${name}", params: [mac: params.mac, remove: devId], submitOnChange: true )
			}
		}
        section("Available Hue Groups") {
			availableGroups.sort{it.value.name}.each { 
				def devId = "${params.mac}/GROUP${it.key}"
				def name = it.value.name
				href(name:"${devId}", page:"chooseGroups", description:"", title:"Add ${name}", params: [mac: params.mac, add: it.key])
			}
        }
        
	}
}

def chooseSchedules(params) {
	log.trace "+++++++++++++++chooseSchedules ( ${params} ):"    
    state.addedSchedules = []
	state.selectedSchedule = []

    if (params.mac) {
        state.params = params;
    } else {
        params = state.params;
    }

    def errorText = ""

	def bridge = getBridge(params.mac)
	def addedSchedules = [:]
    def availableSchedules = [:]
    def user = state.user
    log.debug "=================== ${state.user} ================"
    bridge.value.schedules.each {
		def devId = "${params.mac}/SCHEDULE${it.key}"
		def name = it.value.name
        
		def d = getChildDevice(devId) 
        if (d) {
        	addedSchedules << it
        } else {
        	availableSchedules << it
        }
    }

	log.trace "availableSchedules = ${availableSchedules} / addedSchedules = ${addedSchedules}"
	
	if (params.add) {
	    log.debug("Adding ${params.add}")
        def schId = params.add
        log.debug "ADDING SCHEDULE: params.mac = ${params.mac} / schId = ${schId}"
		params.add = null
        def sch = bridge.value.schedules[schId]
        log.debug "ADDING SCHEDULE: sch / bridge.value.schedules[schId] = ${sch}."
		def devId = "${params.mac}/SCHEDULE${schId}"
        
        if (sch.type == "schedule") {
			try { 
				def qf = addChildDevice("info_fiend", "Hue B Smart QuickFix", devId, bridge.value.hub, ["name": sch.name, "type": sch.type,"sceneId": sch.sceneId, "status": sch.status])
	    	    log.debug "adding schedule ${qf}." 
/**          		["status", "groupId", "sceneId"].each { p ->
                	sch.updateStatus("schedule", p, sch.[p])                    
				}   	       
**/	            
				qf.configure()
				addedSchedules[schId] = sch
				availableSchedules.remove(schId)
			} catch (grails.validation.ValidationException e) {
    	        	log.debug "${devId} already created."
	    	}
		} 
        log.debug "addedSchedules == ${addedSchedules} / availableSchedules == ${availableSchedules}"
    }
    
    if (params.remove) {
    	log.debug "Removing ${params.remove}"
		def devId = params.remove
        params.remove = null
		def schId = devId.split("SCHEDULE")[1]
        log.debug "schId == ${schId}"
		try {
        	deleteChildDevice(devId)
            addedSchedules.remove(schId)
            availableSchedules[schId] = bridge.value.schedules[schId]
		} catch (physicalgraph.exception.NotFoundException e) {
        	log.debug("${devId} already deleted.")
            addedSchedules.remove(schId)
            availableSchedules[schId] = bridge.value.schedules[schId]
		} catch (physicalgraph.exception.ConflictException e) {
        	log.debug("${devId} still in use.")
            errorText = "QuickFix ${bridge.value.schedules[schId].name} is still in use. Remove from any SmartApps or Dashboards, then try again."
        }
    }
    
    return dynamicPage(name:"chooseSchedules", title: "", install:false, uninstall:false, nextPage: "manageBridge") {
	    section("") { 
			href(name: "manageBridge", page: "manageBridge", description: "", title: "Back to Bridge", params: [mac: params.mac])
        }
        
        section("QuickFixes Added to SmartThings") {
			addedSchedules.sort{it.value.name}.each { 
				def devId = "${params.mac}/SCHEDULE${it.key}"
				def name = it.value.name
				href(name:"${devId}", page:"chooseSchedules", description:"", title:"Remove ${name}", params: [mac: params.mac, remove: devId])
			}
		}
        section("Available QuickFixes") {
			availableSchedules.sort{it.value.name}.each { 
				def devId = "${params.mac}/SCHEDULE${it.key}"
				def name = it.value.name
				href(name:"${devId}", page:"chooseSchedules", description:"", title:"Add ${name}", params: [mac: params.mac, add: it.key])
			}
        }
        
	    section("QuickFixes") {
			href(name:"createQuickfixSch", page:"createQuickfixSch", title: "Create a QuickFix", description: "Create A Schedule on the Hue Hub that quickly applies a scene to any light that was just physically turned on.", params: [mac: params.mac]) 
			href(name:"enableQF", page:"enableQF", title: "Enable / Disable QuickFixes", description: "Enable / disable existing QuickFixes on Hue Hub.", params: [mac: params.mac])                 	
		}
		
        
	}
}

def createQuickfixSch(params) {
	log.trace "createQuickfixSch ( ${params} )"
    
    if (params.mac) {
        state.params = params;
    } else {
        params = state.params;
    }

    def errorText = ""

	def bridge = getBridge(params.mac)
	def user = state.user

	if (params.qfName == settings.quickFixName) {        
       	log.debug "Haz params.qfName = ${params.qfName}"        
		state.newSchedule << [qfName: params.qfName]
        params.qfName = null       
    } 
    
	state.availableScenes = bridge.value.scenes
    
	if (params.scDevId) {        
       	log.debug "Haz params.scDevId = ${params.scDevId}"        
	    bridge.value.scenes.each { ss->
        	if (params.scDevId == ss.key ) {      
                state.newSchedule << [sceneId: ss.value.id, sceneName: ss.value.name]                
                state.availableScenes.remove(ss)
				log.trace "Found scene ${state.newSchedule.sceneName} !!!"
                params.scDevId = null
        	}    
		}
    } 
    
    state.availableGroups = bridge.value.groups
    state.availableGroups["0"] = [id: "0", name: "All Lights"]
    
	if (params.grDevId) {        
       	log.debug "Haz params.grDevId = ${params.grDevId}"        
	    bridge.value.groups.each { sg ->        
        	if (params.grDevId == sg.key ) {      
        		state.newSchedule << [groupId: sg.value.id, groupName: sg.value.name]    
				state.availableGroups.remove(sg)
                log.trace "Found group ${state.newSchedule.groupName} !!!"
                params.grDevId = null
	    	}  
		}
    }

    log.debug "selectedScene = ${state.selectedScene}, selectedGroup = ${state.selectedGroup}"
    log.debug "selectedScene.id = ${state.selectedScene.value.id}, selectedGroup.id = ${state.selectedGroup.value.id}"
	if (params.confirm) { log.debug "params.confirm = ${params.confirm}" }
    
	if ( params.confirm == true) {
	    log.debug("createQuickfixSch: CONFIRMED creation of ${state.newSchedule.qfName} ( ${state.newSchedule.groupId} , ${state.newSchedule.sceneId} )")
		def host = state.host
		log.debug "createNewGroup: host = ${host} / username = ${state.user}"
		def groupId = state.newSchedule.groupId 
		def sceneId = state.newSchedule.sceneId         
        
		def uri = "/api/${state.user}/schedules/"
		log.debug "uri = ${uri}"
        def body = ["name": "${state.newSchedule.qfName}", 
        			"command": ["address": "/api/${state.user}/groups/" + groupId + "/action", "method": "PUT",
			            			"body": ["scene": sceneId], "method": "PUT"],
				    "localtime": "R/PT00:00:01",
				    "status": "disabled"
					]

		def bodyJSON = new groovy.json.JsonBuilder(body).toString()
		log.debug "body = ${body} / bodyJSON = ${bodyJSON}"
        try {		
			sendHubCommand(new physicalgraph.device.HubAction([
				method: "POST",
				path: uri,
				headers: [
					HOST: host
				], 
    	        body: bodyJSON
        	    
           	],"${selectedHue}"))	
        } catch (e) {
    		log.debug "something went wrong: $e"
		}
        
		state.newSchedule = []
		setttings.quickFixName = null
        params.confirm = null
        
        chooseSchedules(params.mac)
        
	}
    
    dynamicPage(name:"createQuickfixSch", title: "Steps: 1. Enter name of new QuickFix. \n" + "2. Choose the scene." +  "3. Choose the group." + "4. Press confirm.", install:false, nextPage: "chooseSchedules") {	    	
                
      	if (state.newSchedule.qfName) {
	    	section() {
   	            paragraph "Name of QuickFix is " + state.newSchedule.qfName + "."
    	    }
            
		} else {
			section("Choose Name for New QuickFix (hit enter to go to step 2)") {
//            	settings.quickFixName = 
	      	    input "quickFixName", "text", title: "QuickFix Name: ", required: true, submitOnChange: true 
		        if (quickFixName != "") {
	            	href(name:"${quickFixName}", page:"createQuickfixSch", multiple: false, description:"", title:"Click Here to confirm name: ", params: [mac: params.mac, qfName: quickFixName], submitOnChange: true )    
                }
        	}        
        }
        
        if (state.newSchedule.sceneId) { 
	    	section() {
    	       	log.debug "selectedScene = ${state.newSchedule.sceneName}"
//                def selSName = state.selectedScene.value.name
   	            paragraph "Scene " + state.newSchedule.sceneName + " selected."				                        
    	    } 
        } else {
            
	       	section("What scene do you want to apply in this QuickFix?") {
	//			log.debug "availableScenes = ${availableScenes}"
				state.availableScenes.sort{it.value.name}.each {
					def scDevId = "${it.key}" 	// {params.mac}/SCENE$
					def scName = it.value.name
					href(name:"${scDevId}", page:"createQuickfixSch", multiple: false, description:"", title:"Scene ${scName}", params: [mac: params.mac, scDevId: scDevId], submitOnChange: true )    
				}
			}            
		}         			                       
            
		if ( state.newSchedule.groupId ) {
            section() {
   	           	log.debug "selectedGroup = ${state.newSchedule.groupName}" 
//       	       	def selGName = state.selectedGroup.value.name
        	   	paragraph "" + state.newSchedule.groupName + " selected."				                        
   	        }            	    	
   		} else { 
			section("To what Group should that scene be applied?") {
//				log.debug "availableGroups = ${availableGroups}"
				state.availableGroups.sort{it.value.name}.each {
					def grDevId = "${it.key}"
					def grName = it.value.name
                    def gTitle 
                    if (it.key == "0") {
                    	gTitle = "${grName}"
                    } else {
                    	gTitle = "Group ${grName}"
					}                        
					href(name:"${grDevId}", page:"createQuickfixSch", description:"", title: gTitle, params: [mac: params.mac, grDevId: grDevId], submitOnChange: true)
				}
			}       		
        }    
		
        
	    if ( state.newSchedule.qfName && state.newSchedule.groupId && state.newSchedule.sceneId ) {
   	       	section("Confirm creation of this QuickFix Schedule on Hue Hub.") {
       	       	paragraph "ATTENTION: Clicking Confirm below will IMMEDIATELY create a new schedule on the Hue Hub called ${state.newSchedule.qfName} using the selected scene and group." 
				href(name:"creGroupConfirmed", page:"createQuickfixSch", description:"", title:"Click HERE to Confirm", params: [mac: params.mac, confirm: true], submitOnChange: true) 	//, options: ["Yes", "No"], defaultValue: "No", 
//				input "confirmQF", "enum", description:"", title:"Click 'YES' to Confirm", required: false, options: ["Yes", "No"], defaultValue: "No" , submitOnChange: true
            }       
   	    }				
	}
}


def enableQF(params) {
	log.trace "enableQF: ( ${params} )"
    
    if (params.mac) {
        state.params = params;
    } else {
        params = state.params;
    }

    def errorText = ""

	def bridge = getBridge(params.mac)
	def user = state.user

	state.availableSchedules = bridge.value.schedules
	log.debug "state.availableSchedules == ${state.availableSchedules}"
    
	if (params.schDevId) {        
       	log.debug "Haz params.schDevId = ${params.schDevId}"        
	    state.availableSchedules?.each { sch->
        	if (params.schDevId == sch.key ) {      
                state.schedule << [schId: sch.value.id, schName: sch.name, schStatus: sch.status]                
//                state.availableSchedules.remove(sch)
				log.trace "Found schedule ${state.schedule.schName} !!!"
                params.schDevId = null
        	}    
		}
    } 

    log.debug "selectedSchedule = ${state.schedule}, selectedSchedule.id = ${state.schedule.id}, selectedSchedule.status = ${selectedSchedule.status}"

	if (params.status) { log.debug "params.status = ${params.status}" }
    
	if ( params.confirm == true) {
		def host = state.host
		log.debug "createNewGroup: host = ${host} / username = ${state.user}"
		def schId = state.schedule.schId         
		def schStatus = state.schedule.newStatus
        
		log.debug "Changing ${state.schedule.shcName} ( ${schId} ) to ${newStatus}."
			       
		def uri = "/api/${state.user}/schedules/${schId}"
		log.debug "uri = ${uri}"
        def body = [ "status": newStatus ]

		def bodyJSON = new groovy.json.JsonBuilder(body).toString()
		log.debug "body = ${body} / bodyJSON = ${bodyJSON}"
        try {		
			sendHubCommand(new physicalgraph.device.HubAction([
				method: "PUT",
				path: uri,
				headers: [
					HOST: host
				], 
    	        body: bodyJSON
        	    
           	],"${selectedHue}"))	
        } catch (e) {
    		log.debug "something went wrong: $e"
		}
        
		state.schedule = []
//		setttings.quickFixName = null
        params.confirm = null
        params.newStatus = null
        
        chooseSchedules(params.mac)
        
	}
    
    dynamicPage(name:"enableQF", title: "Enable / Disable your existing QuickFixes.", install:false, nextPage: "chooseSchedules") {	    	
                
      	if (state.schedule.schName) {
	    	section() {
   	            paragraph "QuickFix ${state.schedule.schName} is currently ${state.schedule.schStatus}."
    	    }
            if (state.schedule.schStatus == "enabled") {
         		def changeStatus = "disabled"   	
            } else {
         		def changeStatus = "enabled"   	            
            }
            section("Change to ${changeStatus}?") {
				href(name:"${newStatus}", page:"enableQF", multiple: false, description:"", title:"Click HERE to change status of ${schName}.", params: [mac: params.mac, confirm: true, newStatus: changeStatus], submitOnChange: true )                
			}
		} else {       
	       	section("What QuickFix do you want to modify?") {
 				state.availableSchedules.sort{it.value.name}.each {
					def schDevId = "${it.key}" 	// {params.mac}/SCENE$
					def schName = it.value.name
                    def schStatus = it.value.status
					href(name:"${schDevId}", page:"createQuickfixSch", multiple: false, description:"", title:"Scene ${schName} (${schStatus})", params: [mac: params.mac, schDevId: schDevId], submitOnChange: true )    
				}
			}            
		}   			                                  		
	}
}


/**
def deleteQuickfixSch(params) {
    state.selectedScene = []
	state.selectedGroup = []
    state.availableGroups = []
	state.availableScenes = []
    
    if (params.mac) {
        state.params = params;
    } else {
        params = state.params;
    }

    def errorText = ""

	def bridge = getBridge(params.mac)
	def addedGroups = [:]
    def availableGroups = [:]
	def addedScenes = [:]
    def availableScenes = [:]

	def user = state.user
    log.debug "=================== ${state.user} ================"
    bridge.value.groups.each {
		def groupsDevId = "${params.mac}/GROUP${it.key}"
		def groupName = it.value.name
        
		def d = getChildDevice(groupDevId) 
        if (d) {
        	addedGroups << it
        } else {
        	availableGroups << it
        }
    }
    
    bridge.value.scenes.each {
		def scenesDevId = "${params.mac}/SCENE${it.key}"
		def sceneName = it.value.name
        
		def e = getChildDevice(scenesDevId) 
        if (e) {
        	addedScenes << it
        } else {
        	availableGroups << it
        }
    }

	
    
    if (params.remove) {
    	log.debug "Removing ${params.remove}"
		def devId = params.remove
        params.remove = null
		def groupId = devId.split("GROUP")[1]
		try {
        	deleteChildDevice(devId)
            addedGroups.remove(groupId)
            availableGroups[groupId] = bridge.value.groups[groupId]
		} catch (physicalgraph.exception.NotFoundException e) {
        	log.debug("${devId} already deleted.")
            addedGroups.remove(groupId)
            availableGroups[groupId] = bridge.value.groups[groupId]
		} catch (physicalgraph.exception.ConflictException e) {
        	log.debug("${devId} still in use.")
            errorText = "Group ${bridge.value.groups[groupId].name} is still in use. Remove from any SmartApps or Dashboards, then try again."
        }
    }



}
**/

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def uninstalled() {
    log.debug "Uninstalling"
    state.installed = false
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    initialize()
}

def initialize() {
    log.debug "Initialize."
    unsubscribe()
    unschedule()
    state.schedules = []
    state.subscribed = false
    state.unlinked_bridges = [:]
    state.bridgeRefreshCount = 0
    state.installed = true
	state.limitation = "None"
    state.scheduleEnabled = [:]
    
	doDeviceSync()
	runEvery5Minutes(doDeviceSync)

	state.linked_bridges.each {
        def d = getChildDevice(it.value.mac)
        subscribe(d, "itemDiscovery", itemDiscoveryHandler)
    }
    subscribe(location, null, locationHandler, [filterEvents:false])
}

def itemDiscoveryHandler(evt) {

	log.trace "evt = ${evt}"
    def bulbs = evt.jsonData[0]
 //   log.debug "bulbs from evt.jsonData[0] = ${bulbs}"
    def scenes = evt.jsonData[1]
//	log.debug "scenes from evt.jsonData[1] = ${scenes}"
    def groups = evt.jsonData[2]
//	log.debug "groups from evt.jsonData[2] = ${groups}"
    def schedules = evt.jsonData[3]
//	log.debug "schedules from evt.jsonData[3] = ${schedules}"
    def mac = evt.jsonData[4]
//	log.debug "mac from evt.jsonData[4] = ${mac}"


	def bridge = getBridge(mac)
    state.bridge = bridge
    def host = bridge.value.networkAddress
    host = "${convertHexToIP(host)}" + ":80"
    state.host = host
    def username = state.user

    
	bridge.value.bulbs = bulbs
    bridge.value.groups = groups
    log.debug "Groups = ${groups}"
   	bridge.value.scenes = scenes
    bridge.value.schedules = schedules
    
	if (state.inItemDiscovery) {
	    state.inItemDiscovery = false
        state.itemDiscoveryComplete = true
        bridge.value.itemsDiscovered = true
	}
    
    /* update existing devices */
	def devices = getChildDevices()
    log.trace "devices = ${devices}"
	devices.each {
    	def devId = it.deviceNetworkId
        
	    if (devId.contains(mac) && devId.contains("/")) {
    		if (it.deviceNetworkId.contains("BULB")) {
	            log.trace "contains BULB / DNI = ${it.deviceNetworkId}"
   	            def bulbId = it.deviceNetworkId.split("/")[1] - "BULB"
       	        log.debug "bulbId = ${bulbId}" 
				def type = bridge.value.bulbs[bulbId].type
               	if (type.equalsIgnoreCase("Dimmable light")) {
					["reachable", "on", "bri"].each { p -> 
   	                	it.updateStatus("state", p, bridge.value.bulbs[bulbId].state[p])
					}
           	    }
			else if (type.equalsIgnoreCase("Color Temperature Light")) {
					 ["bri", "ct", "reachable", "on"].each { p ->
                            	it.updateStatus("state", p, bridge.value.bulbs[bulbId].state[p])
                    			}
		    }
			else {
					["reachable", "on", "bri", "hue", "sat", "ct", "xy","effect", "colormode"].each { p -> 
                   		it.updateStatus("state", p, bridge.value.bulbs[bulbId].state[p])                        
					}
   	            }
            }
            if (it.deviceNetworkId.contains("GROUP")) {
   	            def groupId = it.deviceNetworkId.split("/")[1] - "GROUP"
           	    def g = bridge.value.groups[groupId]
				def groupFromBridge = bridge.value.groups[groupId]
                
                def gLights = groupFromBridge.lights
                def test
					["on", "bri", "sat", "ct", "xy", "effect", "hue", "colormode"].each { p -> 
       	            	test = bridge.value.groups[groupId].action[p]
                   	    it.updateStatus("action", p, bridge.value.groups[groupId].action[p])                        
        			}	
            }
            
            if (it.deviceNetworkId.contains("SCENE")) {
            	log.trace "it.deviceNetworkId contains SCENE = ${it.deviceNetworkId}"

				log.trace "contains SCENE / DNI = ${it.deviceNetworkId}"
    	        def sceneId = it.deviceNetworkId.split("/")[1] - "SCENE"
        	    log.debug "sceneId = ${sceneId}"     
                def sceneFromBridge = bridge.value.scenes[sceneId]
                log.trace "sceneFromBridge = ${sceneFromBridge}"
                def sceneLights = []
                sceneLights = sceneFromBridge.lights
                def sceneSchedule = sceneFromBridge.scheduleId
	            log.trace "bridge.value.scenes[${sceneId}].lights = ${sceneLights}"                    
				log.trace "bridge.value.scenes[${sceneId}].scheduleId = ${sceneSchedule}"                    

            	if (bridge.value.scenes[sceneId].lights) {	
					it.updateStatus("scene", "lights", bridge.value.scenes[sceneId].lights)
                }
                if (sceneSchedule) {	
					it.updateStatus("scene", "scheduleId", sceneSchedule)
					it.updateStatus("scene", "schedule", "off")                    
                }
        	}
		}   		 	
	}
}

def locationHandler(evt) {
    def description = evt.description
    def hub = evt?.hubId

    def parsedEvent = parseLanMessage(description)
    parsedEvent << ["hub":hub]

    if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:basic:1")) {
        /* SSDP response */
        processDiscoveryResponse(parsedEvent)
    } else if (parsedEvent.headers && parsedEvent.body) {
        /* Hue bridge HTTP reply */
        def headerString = parsedEvent.headers.toString()
        if (headerString.contains("xml")) {
            /* description.xml reply, verifying bridge */
            processVerifyResponse(parsedEvent.body)
        } else if (headerString?.contains("json")) {
            def body = new groovy.json.JsonSlurper().parseText(parsedEvent.body)
            if (body.success != null && body.success[0] != null && body.success[0].username) {
                /* got username from bridge */
                state.params.linkDone = true
                state.params.username = body.success[0].username
            } else if (body.error && body.error[0] && body.error[0].description) {
                log.debug "error: ${body.error[0].description}"
            } else {
                log.debug "unknown response: ${headerString}"
                log.debug "unknown response: ${body}"
            }
        }
    }
}

/**
 * HUE BRIDGE COMMANDS
 **/
private discoverHueBridges() {
    log.debug("Sending bridge discovery.")
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:basic:1", physicalgraph.device.Protocol.LAN))
}

private verifyHueBridges() {
    def devices = getUnlinkedBridges().findAll { it?.value?.verified != true }
    devices.each {
        def ip = convertHexToIP(it.value.networkAddress)
        def port = convertHexToInt(it.value.deviceAddress)
        verifyHueBridge("${it.value.mac}", (ip + ":" + port))
    }
}

private verifyHueBridge(String deviceNetworkId, String host) {
    log.debug("Sending verify request for ${deviceNetworkId} (${host})")
    sendHubCommand(new physicalgraph.device.HubAction([
            method: "GET",
            path: "/description.xml",
            headers: [
                    HOST: host
            ]]))
}

/**
 * HUE BRIDGE RESPONSES
 **/
private processDiscoveryResponse(parsedEvent) {
	log.debug("Discovery Response is ${parsedEvent}.")
    log.debug("Discovered bridge ${parsedEvent.mac} (${convertHexToIP(parsedEvent.networkAddress)})")

    def bridge = getUnlinkedBridges().find{it?.key?.contains(parsedEvent.ssdpUSN)} 
    if (!bridge) { bridge = getLinkedBridges().find{it?.key?.contains(parsedEvent.ssdpUSN)} }
    if (bridge) {
        /* have already discovered this bridge */
        log.debug("Previously found bridge discovered")
        /* update IP address */
        if (parsedEvent.networkAddress != bridge.value.networkAddress) {
        	bridge.value.networkAddress = parsedEvent.networkAddress
        	def bridgeDev = getChildDevice(parsedEvent.mac)
            if (bridgeDev) {
            	bridgeDev.sendEvent(name: "networkAddress", value: convertHexToIP(bridge.value.networkAddress))
            }
        }
    } else { 
    
        log.debug("Found new bridge.")
        state.unlinked_bridges << ["${parsedEvent.ssdpUSN}":parsedEvent]
   }
}

private processVerifyResponse(eventBody) {
    log.debug("Processing verify response.")
    def body = new XmlSlurper().parseText(eventBody)
    if (body?.device?.modelName?.text().startsWith("Philips hue bridge")) {
        log.debug(body?.device?.UDN?.text())
        def bridge = getUnlinkedBridges().find({it?.key?.contains(body?.device?.UDN?.text())})
        if (bridge) {
            log.debug("found bridge!")
            bridge.value << [name:body?.device?.friendlyName?.text(), serialNumber:body?.device?.serialNumber?.text(), verified: true, itemsDiscovered: false]
        } else {
            log.error "/description.xml returned a bridge that didn't exist"
        }
    }
}

private sendDeveloperReq(ip, mac) {
    log.debug("Sending developer request to ${ip} (${mac})")
    def token = app.id
    sendHubCommand(new physicalgraph.device.HubAction([
            method: "POST",
            path: "/api",
            headers: [
                    HOST: ip
            ],
            body: [devicetype: "$token-0"]]))
}

/**
 * UTILITY FUNCTIONS
 **/
def getCommandData(id) {
    def ids = id.split("/")
    def bridge = getBridge(ids[0])
    def bridgeDev = getChildDevice(ids[0])

    def result = [ip: "${bridgeDev.currentValue("networkAddress")}:80",
                  username: "${bridgeDev.currentValue("username")}",
                  deviceId: "${ids[1] - "BULB" - "GROUP" - "SCENE" - "SCHEDULE"}",
    ]
    return result
}

def curSchEnabled() {

	def result
	if (state.enabledSchedule) {
    	 result = state.scheduleEnabled
	}

	return result
}

def quickFixON(devId, scId, schId) {

	log.trace "quickFixON: (${devId}, ${scId}, ${schId})"
    
    def curScheduled = state.scheduleEnabled    
    log.debug "curScheduled = ${curScheduled}"
    def hostIP = state.host
     
    def curId = curScheduled.scheduleId
	if (curId) {
   		log.debug "Disabling currently-enabled schedule ${curId}."
    	sendHubCommand(new physicalgraph.device.HubAction([
			method: "PUT",
			path: "/api/${state.user}/schedules/${curId}/",
			headers: [
	    	   	host: hostIP
			],
		    body: [status: "disabled"]
		]))		
        
        
		def oldDevId = curScheduled.deviceId
        def oldScene = getChildDevice(oldDevId)
        
        log.debug "oldDevId = ${oldDevId} & oldScene = ${oldScene}"
        if (oldDevId) {
			oldScene.updateStatus("scene", "schedule", "off")
    	    state.scheduleEnabled = [scheduleId: null]
        }    
    }         
	    
   	log.debug "Enabling schedule: ${schId} from ${scId}."    	
	sendHubCommand(new physicalgraph.device.HubAction([
        method: "PUT",
		path: "/api/${state.user}/schedules/${schId}/",
		headers: [
	       	host: hostIP
		],
		body: [status: "enabled"]
	]))
    
    def scene = getChildDevice(devId)
    state.scheduleEnabled = [deviceId: devId, sceneId: scId, scheduleId: schId]
    scene.updateStatus("scene", "schedule", "on")
}

def noFix(devId, scId, schId) {
	log.trace "quickFixOFF: "
    
    def curScheduled = state.scheduleEnabled    
    log.debug "curScheduled = ${curScheduled}"
    def hostIP = state.host
     
    def curId = curScheduled.scheduleId
	if (curId) {
   		log.debug "Disabling currently-enabled schedule ${curId}."
    	sendHubCommand(new physicalgraph.device.HubAction([
			method: "PUT",
			path: "/api/${state.user}/schedules/${curId}/",
			headers: [
	    	   	host: hostIP
			],
		    body: [status: "disabled"]
		]))		
        
        
		def oldDevId = curScheduled.deviceId
        def oldScene = getChildDevice(oldDevId)
        
        log.debug "oldDevId = ${oldDevId} & oldScene = ${oldScene}"
        if (oldDevId) {
			oldScene.updateStatus("scene", "schedule", "off")
    	    state.scheduleEnabled = [scheduleId: null]
        }    
    } 
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

def scaleLevel(level, fromST = false, max = 254) {
	log.trace "scaleLevel( ${level}, ${fromST}, ${max} )"
    /* scale level from 0-254 to 0-100 */
    
    if (fromST) {
        return Math.round( level * max / 100 )
    } else {
    	if (max == 0) {
    		return 0
		} else { 	
        	return Math.round( level * 100 / max )
		}
    }    
    log.trace "scaleLevel returned ${scaled}."
    
}

def parse(desc) {
    log.debug("parse")
}

def doDeviceSync(inItems = null) {
	state.limitation = inItems
	log.debug "Doing Hue Device Sync!  inItems = ${inItems}"
    state.doingSync = true
    try {
		subscribe(location, null, locationHandler, [filterEvents:false])
    } catch (e) {
 	}
	state.linked_bridges.each {
		def bridgeDev = getChildDevice(it.value.mac)
        if (bridgeDev) {
			bridgeDev.discoverItems(inItems)
        }
	}
	discoverHueBridges()
    state.doingSync = false
}