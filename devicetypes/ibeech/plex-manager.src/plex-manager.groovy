definition(
    name: "Plex Manager",
    namespace: "ibeech",
    author: "ibeech & Ph4r",
    description: "Add and Manage Plex Home Theatre endpoints",
    category: "Safety & Security",
    iconUrl: "http://download.easyicon.net/png/1126483/64/",
    iconX2Url: "http://download.easyicon.net/png/1126483/128/",
    iconX3Url: "http://download.easyicon.net/png/1126483/128/",
    oauth: [displayName: "PlexManager", displayLink: ""])

preferences {
	page(name: "startPage")
	page(name: "authPage")
    page(name: "clientPage")
}

def startPage() {
    if (atomicState?.authenticationToken) { return clientPage() }
    else { return authPage() }
}

/* Auth Page */
def authPage() {
    return dynamicPage(name: "authPage", nextPage: clientPage, install: false) {
    	section("Plex Media Server") {
        	input "plexUserName", "text", "title": "Plex Username", multiple: false, required: true
    		input "plexPassword", "password", "title": "Plex Password", multiple: false, required: true
            input "plexServerIP", "text", "title": "Server IP", multiple: false, required: true
            input "theHub", "hub", title: "On which hub?", multiple: false, required: true
		}
    }
}

def clientPage() {
	if (!atomicState.authenticationToken) { getAuthenticationToken() }
	def showUninstall = atomicState.appInstalled
    def clnts  = getClientList() ? getClientList() : getAuthenticationToken()
	def clntDesc = clnts.size() ? "Found (${clnts.size()}) Clients..." : "Tap to Choose" 
    return dynamicPage(name: "clientPage", uninstall: true, install: true) {
		section("Client Selection Page") {
        	input "clients", "enum", title: "Select Your Clients...", description: clntDesc, metadata: [values:clnts], multiple: true, required: false, submitOnChange: true
            href "authPage", title:"Go Back to Auth Page", description: "Tap to edit..."
            input "showAllDevs", "bool", title: "Show All Devices Regardless of Capability", defaultValue: "false", submitOnChange: true
  		}
    }
}

def clientListOpt() {
	getClientList().collect{[(it.key): it.value]}
}

def getClientList() {
	def devs = [:]
	log.debug "Executing 'getClientList'"
    
    try{
        def params = [
            uri: "https://plex.tv/devices.xml",
            contentType: 'application/xml',
            headers: [
                  'X-Plex-Token': atomicState.authenticationToken
            ]
        ]
        // GET 3rd level IP of Plex server
        def plexServerIPShort = settings.plexServerIP.substring(0 , plexServerIP.lastIndexOf("."))

        httpGet(params) { resp ->
            log.debug "Parsing plex.tv/devices.xml"
            def devices = resp.data.Device

            def deviceNames = []
            devices.each { thing ->

                def capabilities = thing.@provides.text()

                // If these capabilities
                if(capabilities.contains("player")||capabilities.contains("client")||settings.showAllDevs){ 

                     //Define name based on name unless blank then use device name
                     def whatToCallMe = "Unknown"
                     if(thing.@name.text() != "") 		{whatToCallMe = "${thing.@name.text()}-${thing.@product.text()}"}
                     else if(thing.@device.text()!="")	{whatToCallMe = "${thing.@device.text()}-${thing.@product.text()}"}

                     // Create alternative name if same name   
                     def tempName = whatToCallMe
                     for (int i = 2; i < 100; i++) {
                        if(deviceNames.contains(tempName)){
                            tempName = "${whatToCallMe} #$i"
                        }else{
                            whatToCallMe = tempName
                            break
                        }
                    }

                    deviceNames << whatToCallMe

                    def addressVal = "0.0.0.0"
                    def portVal = "0"
                    def listName = whatToCallMe

                    // Get IP Address for those with an IP in the same range as your Plex Server if connection IP available (will only return a single entry for the local device)
                    thing.Connection.each { con ->

                        def uri = con.@uri.text()
                        def address = (uri =~ 'https?://([^:]+)')[0][1]
                        def port = uri.split(":")[2].replaceAll("/","")

                        //Check if IP on same range
                        if(plexServerIPShort == address.substring(0 , address.lastIndexOf("."))){
                            addressVal = address
                            portVal = port
                        }
                    }


                    // Add to list
                    if(devs.findIndexValues { it =~ /${thing.@clientIdentifier.text()}/ } == []){
                        if(portVal == "0"){ listName = listName + "*" }
                        devs << ["${whatToCallMe}|${thing.@clientIdentifier.text()}|${addressVal}|${portVal}": "$listName"]
                    } 
                }
            }   
        }
        return devs.sort { a, b -> a.value.toLowerCase() <=> b.value.toLowerCase() }
    }
    catch (ex) {
        if (ex instanceof groovyx.net.http.HttpResponseException) {
        	if(ex.message.contains("unauthorized")) {
            	log.debug "The current Authentication Token has expired... Re-authenicating..."
            	return null
            }
        }
    	else { 
        	log.warn "getClientList Exception: ${ex}"
            return null
        }
   	}
}

def installed() {
	atomicState.appInstalled = true
	log.debug "Installed with settings: ${settings}"
	initialize()   
}

def initialize() {
	atomicState.commandID = -1;
    atomicState.stateCommandSent = now();
    schedule("9 0/1 * 1/1 * ?", regularPolling);
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    atomicState.accessToken = createAccessToken()
    log.debug "URL FOR USE IN PLEX WEBHOOK:\n${getApiServerUrl()}/api/smartapps/installations/${app.id}/pwh?access_token=${atomicState.accessToken}"

	unsubscribe()

    if(clients) {
    
    	def children = getChildDevices()
        def clnts = clients.collect { client ->
        	def item = client.tokenize('|')
            def phtName = item[0]
            def phtIP = item[1]
            def phtIdentifier = item[2]
            def port = item[3]
            log.info "Updating PHT: " + phtName + " with IP: " + phtIP + ":" + port + " and machine identifier: " + phtIdentifier

            def child_deviceNetworkID = childDeviceID(phtIP, phtIdentifier, port);

            //def pht = getChildDevice(client)
            def pht = children.find{ d -> d.deviceNetworkId.contains(phtIP) }
            
            if(!pht) { 
                // The PHT does not exist, create it
                log.debug "creating ${phtName} with id $client"
                pht = addChildDevice("ibeech", "Plex Home Theatre", child_deviceNetworkID, theHub.id, [label:phtName, name:phtName])
                pht.take();
                pht.setPlaybackState("stopped");
                log.debug "created ${pht.displayName} with id $client"
            } 
            else {
				log.debug "found ${pht.displayName} with id $client already exists"
                // Update the network device ID
                if(pht.deviceNetworkId != child_deviceNetworkID) {
                    log.trace "Updating this devices network ID, so that it is consistant"
                    pht.deviceNetworkId = childDeviceID(phtIP, phtIdentifier, port);
                }
            }

            return pht
        }
        
        log.debug "created ${clnts?.size()} clients"
        
        unschedule()
        
        def deleter  // Delete any that are no longer in settings
        if(!clnts) {
            deleter = getAllChildDevices()
            log.warn "found empty clnts list"
        } 
        else { //delete unselected clients
            deleter = getChildDevices().findAll { !clnts.contains(it.deviceNetworkId) }
        }
        def listC = deleter - clnts.intersect(deleter) //Text looks good, but intersect of the lists returns empty set?
        log.warn "clients: ${clients}, clnts: ${clnts}, deleter: ${deleter}, listC: ${listC}, deleting ${listC.size()} devices like ${listC[0]}"
		//deleter.each { deleteChildDevice(it.deviceNetworkId) } //inherits from SmartApp (data-management)
		//listC.each { deleteChildDevice(it) } //inherits from SmartApp (data-management)
        
        
        initialize()
        
    }

    atomicState.commandID = -1;
    
    if (!atomicState.authenticationToken) {
    	getAuthenticationToken()
    }
    
    regularPolling();
    
    // Renew the subscriptions
    subscribe(location, null, response, [filterEvents:false])
    subscribe(location, "playbackDuration", response)
    subscribe(location, "switch", switchChange)
    //subscribe(app, onAppTouch)
}

def uninstalled() {
	removeChildDevices(getChildDevices())
    //unschedule()  //This is called automatically by the Cloud
}

def onAppTouch(event) {
	regularPolling()
}

private removeChildDevices(delete) {
	try {
    	delete.each {
        	def phtIP = getPHTAddress(it.deviceNetworkId);
            def phtPort = getPHTPort(it.deviceNetworkId);
            def phtID = getPHTIdentifier(it.deviceNetworkId);
        	executeClientRequest("/timeline/unsubscribe", phtID, phtIP, phtPort , "GET");
            deleteChildDevice(it.deviceNetworkId)
            log.info "Successfully Removed Child Device: ${it.displayName} (${it.deviceNetworkId})"
    		}
   		}
    catch (e) { log.error "There was an error (${e}) when trying to delete the child device" }
}

mappings {
  path("/pwh") 					{ action: [ POST: "plexWebHookHandler", GET: "plexWebHookHandler", PUT: "plexWebHookHandler"] }
}

def plexWebHookHandler() {	 
	def jsonSlurper = new groovy.json.JsonSlurper()
	def plexJSON = jsonSlurper.parseText(params.payload)
    //log.debug "WebHooks data\nServer ${plexJSON.Server}\nPlayer: ${plexJSON.Player}\nFull: ${plexJSON}"
    
    def command = ""
    def playerID = plexJSON.Player.uuid
    def mediaType = plexJSON.Metadata.type
    // change command to right format
    switch(plexJSON.event) {
		case ["media.play","media.resume"]:		command = "playing"; 	break;
        case "media.pause":						command = "paused"; 	break;
        case "media.stop":						command = "stopped"; 	break;
        return
    }
    def children = getChildDevices()
    def pht = children.find{ d -> d.deviceNetworkId.contains(playerID) }
    def timelineStatus = "Subscribe" 
    if (pht?.settings?.TimelineStatus.toString() != "") {
        timelineStatus = pht.settings.TimelineStatus.toString()
    }
    if (timelineStatus != "None") { return }        
    pht.setPlaybackState(command);
    pht.playbackType(mediaType);
    pht.setPlaybackTitle(plexJSON.Metadata.title);

    def playingTime		= 0L
    if (plexJSON.Metadata?.viewOffset) {playingTime = plexJSON.Metadata?.viewOffset.toLong();}
    def playingDuration	= playingTime.toLong();
    if (plexJSON.Metadata?.duration) {playingDuration = plexJSON.Metadata?.duration.toLong();}
    def playingPosition = (( playingTime / playingDuration ) * 100).toLong()

    log.trace "Determined that $pht Media is: $command is $playingPosition% Complete @ $playingTime/$playingDuration"

    pht.playbackPositionIn(playingPosition);
    pht.playbackDurationIn(playingDuration);
}

def response(evt) {	 

    def msg = parseLanMessage(evt.description);
    if (msg?.body && msg?.body.contains("HTTPError")) { log.debug msg }
    //if (msg && msg?.body) { log.debug msg } // For true debugging only
    if (msg?.body && msg.body.contains("<Media") && !msg.body.contains("HTTPError")){
    	
    	def mediaContainer = new XmlSlurper().parseText(msg.body)
        
       if (atomicState?.commandID == null || (msg.body.contains("commandID") && mediaContainer?.@commandID.text().toInteger() >= 0)) {
        	if (mediaContainer?.@commandID.text().toInteger() >= atomicState?.commandID.toInteger()) { atomicState.commandID = mediaContainer.@commandID.text().toInteger(); }
        
            def children = getChildDevices()

            def thisID = msg.headers["X-Plex-Client-Identifier"]

            def pht = children.find{ d -> d.deviceNetworkId.contains(thisID) }

            if (msg.body.contains("Timeline") && (now() >= (atomicState.stateCommandSent + 900L) )){
                def playbackState = "stopped";
                def playingType = "NONE";
                def playingLevel = 0;
                def playinglocation = "none";
                def indexName = "nav";
                def mediaLocation;

				// location=[navigation,fullScreenVideo,fullScreenPhoto,fullScreenMusic]
				playinglocation = mediaContainer?.@location.text();
                
                switch(playinglocation) {
                    case "navigation":
                    	indexName = "nav";
                    break; 
                    case "fullScreenVideo":
                    	indexName = "video";
                    break; 
                    case "fullScreenPhoto":
                    	indexName = "photo";
                    break; 
                    case "fullScreenMusic":
                    	indexName = "music";
                    break; 
                }
                
                if (indexName != "nav")
                {
                    mediaLocation = mediaContainer.'*'.find { node-> node.@type == indexName }
                    playbackState = mediaLocation.@state.text();
                    playingType   = mediaLocation.@type.text();
                    playingLevel  = mediaLocation.@volume.text();

                    log.trace "Determined that $pht is: $playbackState of type $playingType @ $playinglocation:$playinglevel"

                    pht.setPlaybackState(playbackState);
                    pht.playbackType(playingType);
                    pht.volumeLevelIn(playingLevel);
                    pht.setPlaybackTitle(playinglocation);
                    
                    if (indexName != "photo")
                    {
                        def playingDuration	= mediaLocation.@duration.text().toLong();
                        def playingTime		= mediaLocation.@time.text().toLong();
                        def playingPosition = (( playingTime / playingDuration ) * 100).toLong()
                        
                        log.trace "Determined that $pht Media is: $playbackState is $playingPosition% Complete @ $playingTime/$playingDuration"
                        
                        pht.playbackPositionIn(playingPosition);
                        pht.playbackDurationIn(playingDuration);
                    }
/*                                        
                    def inKey 				= mediaLocation.@key.text();
                    def inMachineIdentifier = mediaLocation.@machineIdentifier.text();
                    def inAddress		    = mediaLocation.@address.text();
                    def inPort 				= mediaLocation.@port.text(); 
                    def inProtocol 			= mediaLocation.@protocol.text();
                    
                    def phtIP = getPHTAddress(pht.deviceNetworkId);
                    def phtPort = getPHTPort(pht.deviceNetworkId);
                    def phtID = getPHTIdentifier(pht.deviceNetworkId);
        
					
                    executeClientRequest("/mirror/details?key=$inKey&machineIdentifier=$inMachineIdentifier&address=$inAddress&port=$inPort&protocol=$inProtocol", phtID, phtIP, phtPort, "GET");
*/                    
                }
                else {
                	log.trace "No status available in $playinglocation message"
                    pht.setPlaybackState(playbackState);
                    pht.playbackType(playingType);
                    pht.volumeLevelIn(playinglevel);
                    pht.setPlaybackTitle(playinglocation);
                }
            }
        }      
	}
}

def String childDeviceID(phtIP, identifier, port) {

	def id = "pht." + settings.plexServerIP + "." + phtIP + "." + identifier + "." + port
    //log.trace "childDeviceID: $id";
    return id;
}
def String getPHTAddress(deviceNetworkId) {

	def parts = deviceNetworkId.tokenize('.');
	def part = parts[6] + "." + parts[7] + "." + parts[8] + "." + parts[9];
    //log.trace "PHTAddress: $part"
    
    return part;
}
def String getPHTIdentifier(deviceNetworkId) {

	def parts = deviceNetworkId.tokenize('.');
    def part = parts[5];    
    //log.trace "PHTIdentifier: $part"
    
	return part;
}
def String getPHTCommand(deviceNetworkId) {
	
	def parts = deviceNetworkId.tokenize('.');
    def part = parts[11];
    //log.trace "PHTCommand: $part"
    
	return part
}
def String getPHTAttribute(deviceNetworkId) {

	def parts = deviceNetworkId.tokenize('.');
    def part = parts[12];
    //log.trace "PHTAttribute: $part"
    
	return part;
}
def String getPHTPort(deviceNetworkId) {
	
	def parts = deviceNetworkId.tokenize('.');
    def part = parts[10];
    //log.trace "PHTPort: $part"
    
	return part
}

def switchChange(evt) {
    
    // We are only interested in event data which contains 
    if(evt.value == "on" || evt.value == "off") return;   
    
	log.debug "Plex Home Theatre event received: " + evt.value;
    
    def parts = evt.value.tokenize('.');
    
    // Parse out the PHT IP address from the event data
    def phtIP = getPHTAddress(evt.value);
    
    // Parse out the new switch state from the event data
    def command = getPHTCommand(evt.value);
    
    // Parse out the PHT clientIdentifier from the event data
    def phtID = getPHTIdentifier(evt.value);
    
    // Parse out the PHT IP address from the event data
    def phtPort = getPHTPort(evt.value);
    
    //log.debug "phtIP: " + phtIP
    log.debug "Command: $command"
    
    switch(command) {
    	case "next":
        	log.debug "Executing 'next'"
			executeClientRequest("/playback/skipNext", phtID, phtIP, phtPort , "GET");
        break;
        
        case "previous":
        	log.debug "Executing 'next'"
			executeClientRequest("/playback/skipPrevious", phtID, phtIP, phtPort , "GET");
        break;
        
        case "play":
			log.debug "Executing 'play'"
            atomicState.stateCommandSent = now();
			executeClientRequest("/playback/play", phtID, phtIP, phtPort , "GET"); 
        break;
        
        case "pause":
			log.debug "Executing 'pause'"
            atomicState.stateCommandSent = now();
			executeClientRequest("/playback/pause", phtID, phtIP, phtPort , "GET"); 
        break;
            
        case "stop": 
			log.debug "Executing 'stop'"
            atomicState.stateCommandSent = now();
			executeClientRequest("/playback/stop", phtID, phtIP, phtPort , "GET"); 
        break;
        
        case "scanNewClients":
        	getClientList() ? getClientList() : getAuthenticationToken()
        break;
            
        case "setVolume":
        	setVolume(phtID, phtIP, phtPort, getPHTAttribute(evt.value));
        break;
            
        case "setPosition":
        	setPosition(phtID, phtIP, phtPort, getPHTAttribute(evt.value));
        break;
            
        case "stepBack": 
			log.debug "Executing 'stepBack'"
			executeClientRequest("/playback/stepBack", phtID, phtIP, phtPort , "GET"); 
        break;
            
        case "stepForward": 
			log.debug "Executing 'stepForward'"
			executeClientRequest("/playback/stepForward", phtID, phtIP, phtPort , "GET"); 
        break;
            
        case "moveLeft": 
			log.debug "Executing 'moveLeft'"
			executeClientRequest("/navigation/moveLeft", phtID, phtIP, phtPort , "GET"); 
        break;
            
        case "moveRight": 
			log.debug "Executing 'moveRight'"
			executeClientRequest("/navigation/moveRight", phtID, phtIP, phtPort , "GET"); 
        break;
            
        case "moveDown": 
			log.debug "Executing 'moveDown'"
			executeClientRequest("/navigation/moveDown", phtID, phtIP, phtPort , "GET"); 
        break;
            
        case "moveUp": 
			log.debug "Executing 'moveUp'"
			executeClientRequest("/navigation/moveUp", phtID, phtIP, phtPort , "GET"); 
        break;
            
        case "select": 
			log.debug "Executing 'select'"
			executeClientRequest("/navigation/select", phtID, phtIP, phtPort , "GET"); 
        break;
            
        case "back": 
			log.debug "Executing 'moveRight'"
			executeClientRequest("/navigation/back", phtID, phtIP, phtPort , "GET"); 
        break;
            
        case "home": 
			log.debug "Executing 'home'"
			executeClientRequest("/navigation/home", phtID, phtIP, phtPort , "GET"); 
        break;
            
        case "music": 
			log.debug "Executing 'music'"
			executeClientRequest("/navigation/music", phtID, phtIP, phtPort , "GET"); 
        break;
		
    }
    
    return;
}

def setVolume(phtID, phtIP, phtPort, level) {
	log.debug "Executing 'setVolume'"
	
	executeClientRequest("/playback/setParameters?volume=$level", phtID, phtIP, phtPort, "GET");
}

def setPosition(phtID, phtIP, phtPort, level) {
	log.debug "Executing 'setPosition'"
	def children = getChildDevices()
    def pht = children.find{ d -> d.deviceNetworkId.contains(phtIP) }
    
    if (pht) {
        def duration = pht.currentValue("playbackDuration")
        def selectedLevel = ( duration.toLong() * (level.toLong() / 100L) ).toLong()

		executeClientRequest("/playback/seekTo?offset=$selectedLevel", phtID, phtIP, phtPort, "GET");
    }
}

def regularPolling() { 

	initiateClientRequest()
    
    runOnce( new Date(now() + 30000L), initiateClientRequest);
}

def executeClientRequest(Path, phtID, phtIP, phtPort, method) {
	// We don't have an authentication token
    if(!atomicState.authenticationToken) {
    	getAuthenticationToken()
    }
	
    // We don't have an active subscription
    if(atomicState?.commandID.toInteger() < 0 || atomicState?.commandID.toInteger() == null) {
    	initiateClientRequest()
    }
	
    atomicState.commandID = atomicState.commandID.toInteger() + 1;   
    if ( atomicState.commandID.toInteger() == Integer.MAX_VALUE ) atomicState.commandID = 0;
    
	def connStyle = "Client" 
    def children = getChildDevices()
    def pht = children.find{ d -> d.deviceNetworkId.contains(phtIP) }
    if (pht?.settings.CommandTarget.toString() != "") {
    	connStyle = pht.settings.CommandTarget.toString()
    }
     
    log.trace "ExecuteClientRequest - The $method path is: $Path and the ID is: $atomicState.commandID from: $phtID|$phtIP:$phtPort to $connStyle";
     
    def sendPath = Path
    if (!Path.contains("?")) { sendPath = Path + "?" } else { sendPath = Path + "&" }
    def headers = [:]
    def pathTarget =""
    if (connStyle == "Server") {
        headers.put("HOST", "$settings.plexServerIP:32400")
        //headers.put("X-Plex-Token", atomicState.authenticationToken)
        //headers.put("X-Plex-Target-Client-Identifier", phtID)
        pathTarget = "/system/players/" + phtIP + sendPath + "X-Plex-Token=$atomicState.authenticationToken&X-Plex-Target-Client-Identifier=$phtID"
    }
    else if (connStyle == "ServerProxy") {
    	headers.put("HOST", "$settings.plexServerIP:32400")
        headers.put("X-Plex-Device-Name", "STHub")
        headers.put("X-Plex-Client-Identifier", "PlexManager")
        headers.put("X-Plex-Client-Platform", "SmartThings")
        headers.put("X-Plex-Version", "5.3.4.759")
        headers.put("X-Plex-Platform", "SmartThings")
        headers.put("X-Plex-Platform-Version", "4.4.4")
        headers.put("X-Plex-Provides", "controller,sync-target")
        headers.put("X-Plex-Product", "PlexManager for ST")
        headers.put("X-Plex-Device", "STHub")
        headers.put("X-Plex-Model", "v2")
        headers.put("X-Plex-Device-Vendor", "Samsung")
        headers.put("X-Plex-Device-Screen-Resolution", "1024x768 (Mobile)")
        headers.put("X-Plex-Device-Screen-Density", "160")
        headers.put("X-Plex-Username", atomicState.tokenUserName)
        headers.put("Accept-Language", "en-us")
        headers.put("Connection", "Keep-Alive")
        headers.put("Accept-Encoding", "gzip")
        headers.put("X-Plex-Target-Client-Identifier", phtID)
        pathTarget = "/player" + sendPath + "X-Plex-Token=$atomicState.authenticationToken&commandID=$atomicState.commandID"
    }
    else {
    	headers.put("HOST", "$phtIP:$phtPort")
        headers.put("X-Plex-Device-Name", "STHub")
        headers.put("X-Plex-Client-Identifier", "PlexManager")
        headers.put("X-Plex-Target-Client-Identifier", phtID)
        pathTarget = "/player" + sendPath + "X-Plex-Client-Identifier=PlexManager&X-Plex-Device-Name=STHub&X-Plex-Token=$atomicState.authenticationToken&X-Plex-Target-Client-Identifier=$phtID&commandID=$atomicState.commandID"
    }
    
    try {    
		def actualAction = new physicalgraph.device.HubAction(
		    method: method,
		    path: pathTarget,   
		    headers: headers)
		
        log.debug"${actualAction}"
        
		sendHubCommand(actualAction)        
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
}

def initiateClientRequest() {
	def hub = location.hubs[0]
    def localPort =  hub.localSrvPortTCP
    def localIP = hub.localIP
	log.trace "initiateClientRequest";
     
    // We don't have an authentication token
    if(!atomicState.authenticationToken) {
    	getAuthenticationToken()
    }
	
    getChildDevices().each { pht ->
	
    	def timelineStatus = "Subscribe" 
        if (pht?.settings?.TimelineStatus && pht?.settings?.TimelineStatus.toString() != "") {
            timelineStatus = pht.settings.TimelineStatus.toString()
        }
        log.trace "Initiating ${pht.deviceNetworkId} with $timelineStatus"
    	if (timelineStatus == "None") { return }
    
        def phtIP = getPHTAddress(pht.deviceNetworkId);
        def phtPort = getPHTPort(pht.deviceNetworkId);
        def phtID = getPHTIdentifier(pht.deviceNetworkId);
        def headers = [:] 
        headers.put("X-Plex-Device-Name", "STHub")
        headers.put("X-Plex-Client-Identifier", "PlexManager")
        
        headers.put("X-Plex-Client-Platform", "SmartThings")
        headers.put("X-Plex-Version", "5.3.4.759")
        headers.put("X-Plex-Platform", "SmartThings")
        headers.put("X-Plex-Platform-Version", "4.4.4")
        headers.put("X-Plex-Provides", "controller,sync-target")
        headers.put("X-Plex-Product", "PlexManager for ST")
        headers.put("X-Plex-Device", "STHub")
        headers.put("X-Plex-Model", "v2")
        headers.put("X-Plex-Device-Vendor", "Samsung")
        headers.put("X-Plex-Device-Screen-Resolution", "1024x768 (Mobile)")
        headers.put("X-Plex-Device-Screen-Density", "160")
        headers.put("X-Plex-Username", atomicState.tokenUserName)
        headers.put("Accept-Language", "en-us")
        headers.put("Connection", "Keep-Alive")
        headers.put("Accept-Encoding", "gzip")
        headers.put("X-Plex-Target-Client-Identifier", phtID)
        //headers.put("X-Plex-Token", atomicState.authenticationToken)
        def sendCommandID = 0
        def pathToSend = ""
        
        if (timelineStatus == "Poll") {
        	headers.put("HOST", "$phtIP:$phtPort")
            if (atomicState.commandID > sendCommandID) { 
            	atomicState.commandID = atomicState.commandID.toInteger() + 1;
                sendCommandID = atomicState.commandID
            }
        	pathToSend = "/player/timeline/poll?wait=0&commandID=$sendCommandID&X-Plex-Token=$atomicState.authenticationToken"
        }
        else if (timelineStatus == "ServerSubscribe") {
            headers.put("HOST", "$settings.plexServerIP:32400")
        	if (atomicState.commandID > sendCommandID) { sendCommandID = atomicState.commandID }
        	pathToSend = "/player/timeline/subscribe?port=$localPort&protocol=http&commandID=$sendCommandID&X-Plex-Token=$atomicState.authenticationToken"
        }
		else {
            headers.put("HOST", "$phtIP:$phtPort")
        	if (atomicState.commandID > sendCommandID) { sendCommandID = atomicState.commandID }
        	pathToSend = "/player/timeline/subscribe?port=$localPort&protocol=http&commandID=$sendCommandID&X-Plex-Token=$atomicState.authenticationToken"
        }

        try {    
            def actualAction = new physicalgraph.device.HubAction(
                method: "GET",
                path: pathToSend,
                headers: headers)

            //log.debug"${actualAction}"
        
		    sendHubCommand(actualAction)        
        }
        catch (Exception e) {
            log.debug "Hit Exception $e on $hubAction"
        }	
    }
}

def getAuthenticationToken() {
	      
    log.debug "Getting authentication token for Plex Server " + settings.plexServerIP      

    def params = [
    	uri: "https://plex.tv/users/sign_in.json?user%5Blogin%5D=" + settings.plexUserName + "&user%5Bpassword%5D=" + URLEncoder.encode(settings.plexPassword),
        headers: [
            'X-Plex-Client-Identifier': 'PlexManager',
			'X-Plex-Product': 'PlexManager for ST',
			'X-Plex-Version': '5.3.4.759',
            'X-Plex-Device-Name': 'STHub',
            'X-Plex-Client-Platform': 'SmartThings',
            'X-Plex-Platform': 'SmartThings',
            'X-Plex-Platform-Version': '4.4.4',
            'X-Plex-Provides': 'controller,sync-target',
            'X-Plex-Device': 'STHub',
            'X-Plex-Model': 'v2',
            'X-Plex-Device-Vendor': 'Samsung',
            'X-Plex-Device-Screen-Resolution': '1024x768 (Mobile)',
            'X-Plex-Device-Screen-Density': '160'
        ]
   	]
    
	try {    
		httpPostJson(params) { resp ->
        	atomicState.tokenUserName = resp.data.user.username;            
        	atomicState.authenticationToken = resp.data.user.authentication_token;
        	log.debug "Token is: " + atomicState.authenticationToken + " UserName is: " + atomicState.tokenUserName //+ " data is: " + resp.data
        }
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $params"
	}
}

/* Helper functions to get the network device ID */
private String NetworkDeviceId(){
    def iphex = convertIPtoHex(settings.piIP).toUpperCase()
    def porthex = convertPortToHex(settings.piPort)
    return "$iphex:$porthex" 
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    //log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    //log.debug hexport
    return hexport
}