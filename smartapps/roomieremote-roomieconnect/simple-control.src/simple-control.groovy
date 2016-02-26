/**
 *  Simple Control
 *
 *  Copyright 2015 Roomie Remote, Inc.
 *
 *	Date: 2015-09-22
 */

definition(
    name: "Simple Control",
    namespace: "roomieremote-roomieconnect",
    author: "Roomie Remote, Inc.",
    description: "Integrate SmartThings with your Simple Control activities.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/roomieuser/remotes/simplesync-60.png",
    iconX2Url: "https://s3.amazonaws.com/roomieuser/remotes/simplesync-120.png",
    iconX3Url: "https://s3.amazonaws.com/roomieuser/remotes/simplesync-120.png")

preferences()
{
	section("Allow Simple Control to Monitor and Control These Things...")
    {
    	input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
  	}
	
	page(name: "mainPage", title: "Simple Control Setup", content: "mainPage", refreshTimeout: 5)
	page(name:"agentDiscovery", title:"Simple Sync Discovery", content:"agentDiscovery", refreshTimeout:5)
	page(name:"manualAgentEntry")
	page(name:"verifyManualEntry")
}

mappings {
	path("/devices") {
    	action: [
        	GET: "getDevices",
            POST: "handleDevicesWithIDs"
        ]
	}
    path("/device/:id") {
    	action: [
        	GET: "getDevice",
            POST: "updateDevice"
        ]
    }
	path("/subscriptions") {
		action: [
			GET: "listSubscriptions",
			POST: "addSubscription", // {"deviceId":"xxx", "attributeName":"xxx","callbackUrl":"http://..."}
            DELETE: "removeAllSubscriptions"
		]
	}
	path("/subscriptions/:id") {
		action: [
			DELETE: "removeSubscription"
		]
	}
}

private getAllDevices()
{
	//log.debug("getAllDevices()")
	([] + switches + locks + thermostats + imageCaptures + relaySwitches + doorControls + colorControls + musicPlayers + speechSynthesizers + switchLevels + indicators + mediaControllers + tones + tvs + alarms + valves + motionSensors + presenceSensors + beacons + pushButtons + smokeDetectors + coDetectors + contactSensors + accelerationSensors + energyMeters + powerMeters + lightSensors + humiditySensors + temperatureSensors + speechRecognizers + stepSensors + touchSensors)?.findAll()?.unique { it.id }
}

def getDevices()
{
	//log.debug("getDevices, params: ${params}")
    allDevices.collect {
    	//log.debug("device: ${it}")
		deviceItem(it)
	}
}

def getDevice()
{
	//log.debug("getDevice, params: ${params}")
    def device = allDevices.find { it.id == params.id }
    if (!device)
    {
    	render status: 404, data: '{"msg": "Device not found"}'
    }
    else
    {
    	deviceItem(device)
    }
}

def handleDevicesWithIDs()
{
	//log.debug("handleDevicesWithIDs, params: ${params}")
	def data = request.JSON
	def ids = data?.ids?.findAll()?.unique()
    //log.debug("ids: ${ids}")
    def command = data?.command
	def arguments = data?.arguments
    if (command)
    {
    	def success = false
	    //log.debug("command ${command}, arguments ${arguments}")
    	for (devId in ids)
        {
			def device = allDevices.find { it.id == devId }
			if (device) {
				if (arguments) {
					device."$command"(*arguments)
				} else {
					device."$command"()
				}
				success = true
			} else {
            	//log.debug("device not found ${devId}")
			}
		}
        
        if (success)
        {
        	render status: 200, data: "{}"
        }
        else
        {
        	render status: 404, data: '{"msg": "Device not found"}'
        }
    }
    else
    {
		ids.collect {
    		def currentId = it
    		def device = allDevices.find { it.id == currentId }
        	if (device)
        	{
        		deviceItem(device)
    	    }
   		}
	}
}

private deviceItem(device) {
	[
		id: device.id,
		label: device.displayName,
		currentState: device.currentStates,
		capabilities: device.capabilities?.collect {[
			name: it.name
		]},
		attributes: device.supportedAttributes?.collect {[
			name: it.name,
			dataType: it.dataType,
			values: it.values
		]},
		commands: device.supportedCommands?.collect {[
			name: it.name,
			arguments: it.arguments
		]},
		type: [
			name: device.typeName,
			author: device.typeAuthor
		]
	]
}

def updateDevice()
{
	//log.debug("updateDevice, params: ${params}")
	def data = request.JSON
	def command = data?.command
	def arguments = data?.arguments

	//log.debug("updateDevice, params: ${params}, request: ${data}")
	if (!command) {
		render status: 400, data: '{"msg": "command is required"}'
	} else {
		def device = allDevices.find { it.id == params.id }
		if (device) {
			if (arguments) {
				device."$command"(*arguments)
			} else {
				device."$command"()
			}
			render status: 204, data: "{}"
		} else {
			render status: 404, data: '{"msg": "Device not found"}'
		}
	}
}

def listSubscriptions()
{
	//log.debug "listSubscriptions()"
	app.subscriptions?.findAll { it.deviceId }?.collect {
		def deviceInfo = state[it.deviceId]
		def response = [
			id: it.id,
			deviceId: it.deviceId,
			attributeName: it.data,
			handler: it.handler
		]
		//if (!selectedAgent) {
			response.callbackUrl = deviceInfo?.callbackUrl
		//}
		response
	} ?: []
}

def addSubscription() {
	def data = request.JSON
	def attribute = data.attributeName
	def callbackUrl = data.callbackUrl

	//log.debug "addSubscription, params: ${params}, request: ${data}"
	if (!attribute) {
		render status: 400, data: '{"msg": "attributeName is required"}'
	} else {
		def device = allDevices.find { it.id == data.deviceId }
		if (device) {
			//if (!selectedAgent) {
				//log.debug "Adding callbackUrl: $callbackUrl"
				state[device.id] = [callbackUrl: callbackUrl]
			//}
			//log.debug "Adding subscription"
			def subscription = subscribe(device, attribute, deviceHandler)
			if (!subscription || !subscription.eventSubscription) {
            	//log.debug("subscriptions: ${app.subscriptions}")
                //for (sub in app.subscriptions)
                //{
                	//log.debug("subscription.id ${sub.id} subscription.handler ${sub.handler} subscription.deviceId ${sub.deviceId}")
                    //log.debug(sub.properties.collect{it}.join('\n'))
				//}
				subscription = app.subscriptions?.find { it.device.id == data.deviceId && it.data == attribute && it.handler == 'deviceHandler' }
			}

			def response = [
				id: subscription.id,
				deviceId: subscription.device?.id,
				attributeName: subscription.data,
				handler: subscription.handler
			]
			//if (!selectedAgent) {
				response.callbackUrl = callbackUrl
			//}
			response
		} else {
			render status: 400, data: '{"msg": "Device not found"}'
		}
	}
}

def removeSubscription()
{
	def subscription = app.subscriptions?.find { it.id == params.id }
	def device = subscription?.device

	//log.debug "removeSubscription, params: ${params}, subscription: ${subscription}, device: ${device}"
	if (device) {
		//log.debug "Removing subscription for device: ${device.id}"
		state.remove(device.id)
		unsubscribe(device)
	}
	render status: 204, data: "{}"
}

def removeAllSubscriptions()
{
	for (sub in app.subscriptions)
    {
    	//log.debug("Subscription: ${sub}")
        //log.debug(sub.properties.collect{it}.join('\n'))
        def handler = sub.handler
        def device = sub.device
        
    	if (device && handler == 'deviceHandler')
        {
	        //log.debug(device.properties.collect{it}.join('\n'))
        	//log.debug("Removing subscription for device: ${device}")
            state.remove(device.id)
            unsubscribe(device)
        }
    }
}

def deviceHandler(evt) {
	def deviceInfo = state[evt.deviceId]
	//if (selectedAgent) {
    //	sendToRoomie(evt, agentCallbackUrl)
	//} else if (deviceInfo) {
    if (deviceInfo)
    {
		if (deviceInfo.callbackUrl) {
			sendToRoomie(evt, deviceInfo.callbackUrl)
		} else {
			log.warn "No callbackUrl set for device: ${evt.deviceId}"
		}
	} else {
		log.warn "No subscribed device found for device: ${evt.deviceId}"
	}
}

def sendToRoomie(evt, String callbackUrl) {
	def callback = new URI(callbackUrl)
	def host = callback.port != -1 ? "${callback.host}:${callback.port}" : callback.host
	def path = callback.query ? "${callback.path}?${callback.query}".toString() : callback.path
	sendHubCommand(new physicalgraph.device.HubAction(
		method: "POST",
		path: path,
		headers: [
			"Host": host,
			"Content-Type": "application/json"
		],
		body: [evt: [deviceId: evt.deviceId, name: evt.name, value: evt.value]]
	))
}

def mainPage()
{
	if (canInstallLabs())
    {
       	return agentDiscovery()
    }
    else
    {
        def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

        return dynamicPage(name:"mainPage", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
            section("Upgrade")
            {
                paragraph "$upgradeNeeded"
            }
        }
    }
}

def agentDiscovery(params=[:])
{
	int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
    state.refreshCount = refreshCount + 1
    def refreshInterval = refreshCount == 0 ? 2 : 5
	
    if (!state.subscribe)
    {
        subscribe(location, null, locationHandler, [filterEvents:false])
        state.subscribe = true
    }
	
    //ssdp request every fifth refresh
    if ((refreshCount % 5) == 0)
    {
        discoverAgents()
    }
	
    def agentsDiscovered = agentsDiscovered()
    
    return dynamicPage(name:"agentDiscovery", title:"Pair with Simple Sync", nextPage:"", refreshInterval: refreshInterval, install:true, uninstall: true) {
        section("Pair with Simple Sync")
        {
            input "selectedAgent", "enum", required:false, title:"Select Simple Sync\n(${agentsDiscovered.size() ?: 0} found)", multiple:false, options:agentsDiscovered
        	href(name:"manualAgentEntry",
            	 title:"Manually Configure Simple Sync",
                 required:false,
                 page:"manualAgentEntry")
        }
        section("Allow Simple Control to Monitor and Control These Things...")
        {
	    	input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
	  	}
    }
}

def manualAgentEntry()
{
	dynamicPage(name:"manualAgentEntry", title:"Manually Configure Simple Sync", nextPage:"verifyManualEntry", install:false, uninstall:true) {
    	section("Manually Configure Simple Sync")
        {
        	paragraph "In the event that Simple Sync cannot be automatically discovered by your SmartThings hub, you may enter Simple Sync's IP address here."
            input(name: "manualIPAddress", type: "text", title: "IP Address", required: true)
        }
    }
}

def verifyManualEntry()
{
    def hexIP = convertIPToHexString(manualIPAddress)
    def hexPort = convertToHexString(47147)
    def uuid = "593C03D2-1DA9-4CDB-A335-6C6DC98E56C3"
    def hubId = ""
    
    for (hub in location.hubs)
    {
    	if (hub.localIP != null)
        {
        	hubId = hub.id
            break
        }
    }
    
    def manualAgent = [deviceType: "04",
    					mac: "unknown",
    					ip: hexIP,
                        port: hexPort,
                        ssdpPath: "/upnp/Roomie.xml",
                        ssdpUSN: "uuid:$uuid::urn:roomieremote-com:device:roomie:1",
                        hub: hubId,
                        verified: true,
                        name: "Simple Sync $manualIPAddress"]
	
    state.agents[uuid] = manualAgent
    
    addOrUpdateAgent(state.agents[uuid])
    
    dynamicPage(name: "verifyManualEntry", title: "Manual Configuration Complete", nextPage: "", install:true, uninstall:true) {
    	section("")
        {
        	paragraph("Tap Done to complete the installation process.")
        }
    }
}

def discoverAgents()
{
    def urn = getURN()
    
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery $urn", physicalgraph.device.Protocol.LAN))
}

def agentsDiscovered()
{
    def gAgents = getAgents()
    def agents = gAgents.findAll { it?.value?.verified == true }
    def map = [:]
    agents.each
    {
        map["${it.value.uuid}"] = it.value.name
    }
    map
}

def getAgents()
{
    if (!state.agents)
    {
    	state.agents = [:]
    }
    
    state.agents
}

def installed()
{
	initialize()
}

def updated()
{
	initialize()
}

def initialize()
{
	if (state.subscribe)
	{
    	unsubscribe()
		state.subscribe = false
	}
    
    if (selectedAgent)
    {
    	addOrUpdateAgent(state.agents[selectedAgent])
    }
}

def addOrUpdateAgent(agent)
{
	def children = getChildDevices()
	def dni = agent.ip + ":" + agent.port
    def found = false
	
	children.each
	{
		if ((it.getDeviceDataByName("mac") == agent.mac))
		{
        	found = true
            
            if (it.getDeviceNetworkId() != dni)
            {
				it.setDeviceNetworkId(dni)
			}
		}
        else if (it.getDeviceNetworkId() == dni)
        {
        	found = true
        }
	}
    
	if (!found)
	{
        addChildDevice("roomieremote-agent", "Simple Sync", dni, agent.hub, [label: "Simple Sync"])
	}
}

def locationHandler(evt)
{
    def description = evt?.description
    def urn = getURN()
    def hub = evt?.hubId
    def parsedEvent = parseEventMessage(description)
    
    parsedEvent?.putAt("hub", hub)
    
    //SSDP DISCOVERY EVENTS
	if (parsedEvent?.ssdpTerm?.contains(urn))
	{
        def agent = parsedEvent
        def ip = convertHexToIP(agent.ip)
        def agents = getAgents()
        
        agent.verified = true
        agent.name = "Simple Sync $ip"
        
        if (!agents[agent.uuid])
        {
        	state.agents[agent.uuid] = agent
        }
    }
}

private def parseEventMessage(String description)
{
	def event = [:]
	def parts = description.split(',')
    
	parts.each
    { part ->
		part = part.trim()
		if (part.startsWith('devicetype:'))
        {
			def valueString = part.split(":")[1].trim()
			event.devicetype = valueString
		}
		else if (part.startsWith('mac:'))
        {
			def valueString = part.split(":")[1].trim()
			if (valueString)
            {
				event.mac = valueString
			}
		}
		else if (part.startsWith('networkAddress:'))
        {
			def valueString = part.split(":")[1].trim()
			if (valueString)
            {
				event.ip = valueString
			}
		}
		else if (part.startsWith('deviceAddress:'))
        {
			def valueString = part.split(":")[1].trim()
			if (valueString)
            {
				event.port = valueString
			}
		}
		else if (part.startsWith('ssdpPath:'))
        {
			def valueString = part.split(":")[1].trim()
			if (valueString)
            {
				event.ssdpPath = valueString
			}
		}
		else if (part.startsWith('ssdpUSN:'))
        {
			part -= "ssdpUSN:"
			def valueString = part.trim()
			if (valueString)
            {
				event.ssdpUSN = valueString
                
                def uuid = getUUIDFromUSN(valueString)
                
                if (uuid)
                {
                	event.uuid = uuid
                }
			}
		}
		else if (part.startsWith('ssdpTerm:'))
        {
			part -= "ssdpTerm:"
			def valueString = part.trim()
			if (valueString)
            {
				event.ssdpTerm = valueString
			}
		}
		else if (part.startsWith('headers'))
        {
			part -= "headers:"
			def valueString = part.trim()
			if (valueString)
            {
				event.headers = valueString
			}
		}
		else if (part.startsWith('body'))
        {
			part -= "body:"
			def valueString = part.trim()
			if (valueString)
            {
				event.body = valueString
			}
		}
	}

	event
}

def getURN()
{
    return "urn:roomieremote-com:device:roomie:1"
}

def getUUIDFromUSN(usn)
{
	def parts = usn.split(":")
	
	for (int i = 0; i < parts.size(); ++i)
	{
		if (parts[i] == "uuid")
		{
			return parts[i + 1]
		}
	}
}

def String convertHexToIP(hex)
{
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

def Integer convertHexToInt(hex)
{
	Integer.parseInt(hex,16)
}

def String convertToHexString(n)
{
	String hex = String.format("%X", n.toInteger())
}

def String convertIPToHexString(ipString)
{
	String hex = ipString.tokenize(".").collect {
    	String.format("%02X", it.toInteger())
    }.join()
}

def Boolean canInstallLabs()
{
    return hasAllHubsOver("000.011.00603")
}

def Boolean hasAllHubsOver(String desiredFirmware)
{
    return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

def List getRealHubFirmwareVersions()
{
    return location.hubs*.firmwareVersionString.findAll { it }
}




