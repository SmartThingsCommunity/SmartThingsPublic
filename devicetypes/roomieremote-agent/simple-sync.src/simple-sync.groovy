/**
 *  Simple Sync
 *
 *  Copyright 2015 Roomie Remote, Inc.
 *
 *	Date: 2015-09-22
 */
metadata
{
	definition (name: "Simple Sync", namespace: "roomieremote-agent", author: "Roomie Remote, Inc.")
    {
		capability "Media Controller"
	}

	// simulator metadata
	simulator
    {
	}

	// UI tile definitions
	tiles
    {
		standardTile("mainTile", "device.status", width: 1, height: 1, icon: "st.Entertainment.entertainment11")
        {
        	state "default", label: "Simple Sync", icon: "st.Home.home2", backgroundColor: "#00a0dc"
		}
        
    	def detailTiles = ["mainTile"]
		
		main "mainTile"
		details(detailTiles)
	}
}

def parse(String description)
{
	def results = []
	
	try
	{
		def msg = parseLanMessage(description)
		
		if (msg.headers && msg.body)
		{
        	switch (msg.headers["X-Roomie-Echo"])
			{
            	case "getAllActivities":
                    handleGetAllActivitiesResponse(msg)
                	break
			}
    	}
	}
    catch (Throwable t)
    {
	    sendEvent(name: "parseError", value: "$t", description: description)
    	throw t
    }
    
    results
}

def handleGetAllActivitiesResponse(response)
{
	def body = parseJson(response.body)
    
    if (body.status == "success")
    {
	    def json = new groovy.json.JsonBuilder()
	    def root = json activities: body.data
	    def data = json.toString()
    	
	    sendEvent(name: "activities", value: data)
    }
}

def getAllActivities(evt)
{
    def host = getHostAddress(device.deviceNetworkId)
	
	def action = new physicalgraph.device.HubAction(method: "GET",
													path: "/api/v1/activities",
													headers: [HOST: host, "X-Roomie-Echo": "getAllActivities"])
	
	action
}

def startActivity(evt)
{
    def uuid = evt
    def host = getHostAddress(device.deviceNetworkId)
    def activity = new groovy.json.JsonSlurper().parseText(device.currentValue('activities') ?: "{ 'activities' : [] }").activities.find { it.uuid == uuid }
    def toggle = activity["toggle"]
    def jsonMap = ["activity_uuid": uuid]
    
    if (toggle != null)
    {
        jsonMap << ["toggle_state": toggle ? "on" : "off"]
    }
    
    def json = new groovy.json.JsonBuilder(jsonMap)
    def jsonBody = json.toString()
    def headers = [HOST: host, "Content-Type": "application/json"]
    
    def action = new physicalgraph.device.HubAction(method: "POST",
													path: "/api/v1/runactivity",
													body: jsonBody,
													headers: headers)
	
    action
}

def getHostAddress(d)
{
	def parts = d.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

def String convertHexToIP(hex)
{
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

def Integer convertHexToInt(hex)
{
	Integer.parseInt(hex,16)
}
