/**
 *  Samsung Smart TV (Connect)
 *
 *  Copyright 2015 Min-kyu Park
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
    name: "Samsung Smart TV (Connect)",
    namespace: "smartthings",
    author: "Min-kyu Park",
    description: "Samsung Smart TV (Connect)",
    category: "SmartThings Labs",
    iconUrl: "http://www.samsung.com/global/microsite/tv/uhdtv/images/mount_stand_tv.png",
    iconX2Url: "http://www.samsung.com/global/microsite/tv/uhdtv/images/mount_stand_tv.png",
    iconX3Url: "http://www.samsung.com/global/microsite/tv/uhdtv/images/mount_stand_tv.png")

preferences {
	page(name:"televisionDiscovery", title:"Samsung Smart TV Setup", content:"televisionDiscovery", refreshInterval:3, refreshTimeout:5)
    page(name:"tvBtnPush", title:"Linking with your TV", content:"tvLinking", refreshInterval:3, refreshTimeout:5)
    page(name:"tvName", title:"Set name of Samsung Smart TV", content:"tvName")
}

def televisionDiscovery() {
    state.permission = false
    state.linkRefreshcount = -1
    
	def tvs = televisionsDiscovered()
	int tvRefreshCount = !state.tvRefreshCount ? 0 : state.tvRefreshCount as int
    
    state.tvRefreshCount = tvRefreshCount + 1

    def options = tvs ?: []
    def numFound = options.size() ?: 0

    if(!state.subscribe) {
        subscribe(location, null, locationHandler, [filterEvents:false])
        state.subscribe = true
    }

    if((tvRefreshCount % 5) == 0) {
        discoverTVs()
    }
    
    def unverified = getTVs().findAll { it?.value?.verified != true }
    
    if( (tvRefreshCount % 3) != 1 ) {
    	sendHubIPReq(unverified)
    }
        
    if( (tvRefreshCount % 3) != 2 ) {
    	verifyTVs(unverified)
    }
    
    return dynamicPage(name:"televisionDiscovery", title:"Discovery Started!", nextPage:"tvBtnPush", refreshInterval:refreshInterval, uninstall: true) {
        section("Please wait while we discover your Samsung Smart TV. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
            input "selectedTV", "enum", required:true, title:"Select Samsung Smart TV (${numFound} found)", multiple:false, options:options
        }
    }
}

def tvLinking() {
	int linkRefreshcount = state.linkRefreshcount
    
    state.linkRefreshcount = linkRefreshcount + 1
    
	def nextPage = ""
    def title = "Linking with your TV"
	def paragraphText = "Press the 'Yes' button on a message, which is going to be shown on TV screen, to setup a link."
    
    if (state.permission)
    {
    	nextPage = "tvName"
		title = "Success! - click 'Next'"
		paragraphText = "Linking to your hub was a success! Please click 'Next'!"
    }
    else if( !state.permission && ((linkRefreshcount % 5) == 0) )
    {
    	sendDeveloperReq()
    }
    
    return dynamicPage(name:"tvBtnPush", title:title, nextPage:nextPage, refreshInterval:refreshInterval) {
    	section("Button Press") {
        	paragraph """${paragraphText}"""
        }
    }
}

def tvName() {
    return dynamicPage(name:"tvName", title:"Set Name!", install:true) {
		section{
			input "TVname", "text", title:"Give your Samsung Smart TV a name", required:true
		}
    }
}

private discoverTVs() {
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:samsung.com:device:SmartThingsTvDevice:1", physicalgraph.device.Protocol.LAN))
}

private sendHubIPReq(devices) {
    devices.each {
       	def hubID = it.value.hubID
       	def vHub = location.hubs.find{it.id == hubID}
        def body = "{\"request\" : \"set\", \"type\" : \"hubIP\", \"value\" : \"${vHub.localIP + ":" + vHub.localSrvPortTCP}\"}"
        sendHubCommand(new physicalgraph.device.HubAction([
        method: "POST",
        path: "/smartthings/hub",
        headers: [
            HOST: ipAddressFromDni(it.value.ip + ":" + it.value.port),
            /Content-Type/: "application/json"
        ],
        body: "${body}\n"],
        selectedTV))
    }
}

private sendDeveloperReq() {
	def body = "{\"request\":\"set\",\"type\":\"notification\",\"value\":{\"notification id\":\"TVauthentication\",\"buttons\":[\"OK\"],\"title\":\"Inform\",\"icon\":\"\",\"message\":\"\",\"description\":\"Do you want to register your TV?\",\"device id\":\"\"}}"
    
	sendHubCommand(new physicalgraph.device.HubAction([
    	method: "POST",
		path: "/smartthings/tvfeature",
		headers: [
        	HOST: ipAddressFromDni(selectedTV),
            /Content-Type/: "application/json",
        ],
        body: "${body}\n"],
        selectedTV))
}

private verifyTV(deviceNetworkId) {
	sendHubCommand(new physicalgraph.device.HubAction([
    	method: "POST",
		path: "/smartthings/deviceinfo",
		headers:[
        	HOST: ipAddressFromDni(deviceNetworkId),
            /Content-Type/: "application/json",
            /Content-Length/: 0,
        ]],
        deviceNetworkId))
}

private verifyTVs(devices) {
    devices.each {
    	verifyTV(it?.value?.ip + ":" + it?.value?.port)
    }
}

Map televisionsDiscovered() {
    def vtelevisions = getVerifiedTVs()
    
    def map = [:]
    vtelevisions.each {
    	def value = "Samsung Smart TV (${convertHexToIP(it.value.ip)})"
        def key = it.value.ip + ":" + it.value.port
        map["${key}"] = value
    }
    map
}

def getTVs() {
	state.televisions = state.televisions ?: [:]
}

def getVerifiedTVs() {
	getTVs().findAll{ it?.value?.verified == true }
}

def installed() {
    initialize()
}

def updated() {
    if (state.subscribe)
    {
    	unsubscribe()
        state.subscribe = false
    }
	initialize()
}

def initialize() {
    unschedule()
    if (selectedTV)
    {
    	addTV()
    }
    runEvery5Minutes("scheduledActionsHandler")
    scheduledActionsHandler()
}

def scheduledActionsHandler() {
	doDeviceSync()
	refreshAll()
}

private refreshAll(){
    sendHubIPReq(getVerifiedTVs())
	childDevices*.refresh()
}

def addTV() {
    def vTVs = getVerifiedTVs()
    def vTV  = vTVs.find {(it.value.ip + ":" + it.value.port) == selectedTV}

    if(vTV)
    {
    	def d = getChildDevice(selectedTV)
        if(!d)
        {
        	d = addChildDevice("smartthings", "${vTV.value.productionYear} Samsung Smart TV", selectedTV, vTV.value.hubID, ["name":TVname, "label":TVname, "data":["mac": vTV.value.mac, "uuid": vTV.value.uuid, "ip": vTV.value.ip, "port": vTV.value.port, "productionYear": vTV.value.productionYear, "boardType": vTV.value.boardType]])
        }
        else
        {
        	TRACE("found ${d.displayName} with id ${selectedTV} already exists")
        }
    }
}

def locationHandler(evt) {
    def description = evt.description
    def hub = evt?.hubId
    
    def parsedEvent = parseLanMessage(description)
    
    if (parsedEvent?.ssdpTerm?.contains("urn:samsung.com:device:SmartThingsTvDevice:1"))
    {
        def uuid = parseUUID(parsedEvent.ssdpUSN)
        def TVs = getTVs()
        
        if (!(TVs."${parsedEvent.mac}"))
        {
            TRACE("Adding TV ${parsedEvent.mac}")
            TVs[parsedEvent.mac] = [
                mac : parsedEvent.mac,
                ip : parsedEvent.networkAddress,
                port : "1F90",
                ssdpPath : parsedEvent.ssdpPath,
                uuid : uuid,
                hubID : hub
                ]
        }
        else
        {
            TRACE("Device was already found in state...")
            def d = TVs."${parsedEvent.mac}"
            if(d.ip != parsedEvent.networkAddress)
            {
                TRACE("Device's ip changed...")
                d.ip = parsedEvent.networkAddress
                d.uuid = uuid
                
                def child = getChildDevice(parsedEvent.mac)
                if( child )
                {
                    TRACE("updating dni for device ${child} with mac ${parsedEvent.mac}")
                    child.updateDataValue("ip", upnpResult.ip)
                    child.updateDataValue("port", upnpResult.port)
                    child.updateDataValue("uuid", uuid)
                }
            }
        }
    }
    else if (parsedEvent.headers && parsedEvent.body)
    {
		def type = parsedEvent.headers."Content-Type"
		if(type?.contains("json"))
        {
            def parsedBody = parseJson(parsedEvent.body)
			if (parsedBody.type == "feedbackMessage")
            {
            	if (parsedBody.value == "TVauthentication,OK")
                {
                	state.permission = true
                }
            }
            else if (parsedBody.request == "update" && parsedBody.type == "deviceInfo")
            {
                def vTVs = getTVs()
                def vTV  = vTVs.find {it.key == parsedEvent.mac}
                vTV.value << [
                	boardType : parsedBody.value.boardType, 
                    productionYear : parsedBody.value.productionYear, 
                    verified : true
                    ]
            }
        }
    }
    else
    {
    	TRACE("non-TV event $evt.description")
    }
}

private parseUUID(ssdpUSN) {
	String uuid
    ssdpUSN = ssdpUSN.trim()
    if (ssdpUSN)
    {
        def splitedSSDPUSN = ssdpUSN.split('::')
        def frontValue = splitedSSDPUSN[0].trim()
        if (frontValue.startsWith('uuid:'))
        {
            frontValue -= "uuid:"
            frontValue = frontValue.trim()
            if (frontValue)
            {
                uuid = frontValue
            }
        }
    }
    uuid
}

def doDeviceSync(){
	if(!state.subscribe)
    {
    	subscribe(location, null, locationHandler, [filterEvents:false])
		state.subscribe = true
    }
	discoverTVs()
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private ipAddressFromDni(dni) {
	if (dni) {
    	def segs = dni.split(":")
		convertHexToIP(segs[0]) + ":" +  convertHexToInt(segs[1])
    } else { 
    	null
    }
}

private TRACE(message) {
    log.debug message
}