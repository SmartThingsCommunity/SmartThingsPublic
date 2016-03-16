/**
 *  Simple Sync Connect
 *
 *  Copyright 2015 Roomie Remote, Inc.
 *
 *	Date: 2015-09-22
 */

definition(
    name: "Simple Sync Connect",
    namespace: "roomieremote-raconnect",
    author: "Roomie Remote, Inc.",
    description: "Integrate SmartThings with your Simple Control activities via Simple Sync.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/roomieuser/remotes/simplesync-60.png",
    iconX2Url: "https://s3.amazonaws.com/roomieuser/remotes/simplesync-120.png",
    iconX3Url: "https://s3.amazonaws.com/roomieuser/remotes/simplesync-120.png")

preferences()
{
	page(name: "mainPage", title: "Simple Sync Setup", content: "mainPage", refreshTimeout: 5)
    page(name:"agentDiscovery", title:"Simple Sync Discovery", content:"agentDiscovery", refreshTimeout:5)
    page(name:"manualAgentEntry")
    page(name:"verifyManualEntry")
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
            input "selectedAgent", "enum", required:true, title:"Select Simple Sync\n(${agentsDiscovered.size() ?: 0} found)", multiple:false, options:agentsDiscovered
        	href(name:"manualAgentEntry",
            	 title:"Manually Configure Simple Sync",
                 required:false,
                 page:"manualAgentEntry")
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