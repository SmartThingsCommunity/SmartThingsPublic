/**
 *  Jawbone Service Manager
 *
 *  Author: Juan Risso
 *  Date: 2013-12-19
 */
definition(
	name: "Jawbone UP (Connect)",
	namespace: "juano2310",
	author: "Juan Pablo Risso",
	description: "Connect your Jawbone UP to SmartThings",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/jawbone-up.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/jawbone-up@2x.png",
	oauth: true,
    usePreferencesForAuthorization: false,
    singleInstance: true
) {
	appSetting "clientId"
	appSetting "clientSecret"
}

preferences {
    page(name: "Credentials", title: "Jawbone UP", content: "authPage", install: false)
}

mappings {
	path("/receivedToken") { action: [ POST: "receivedToken", GET: "receivedToken"] }
	path("/receiveToken") { action: [ POST: "receiveToken", GET: "receiveToken"] }
	path("/hookCallback") { action: [ POST: "hookEventHandler", GET: "hookEventHandler"] }
    path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}    
	path("/oauth/callback") { action: [ GET: "callback" ] }
}

def getServerUrl() { return "https://graph.api.smartthings.com" }
def getBuildRedirectUrl() { "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${apiServerUrl}" }
def buildRedirectUrl(page) { return buildActionUrl(page) }

def callback() {
	def redirectUrl = null
	if (params.authQueryString) {
		redirectUrl = URLDecoder.decode(params.authQueryString.replaceAll(".+&redirect_url=", ""))
		log.debug "redirectUrl: ${redirectUrl}"
	} else {
		log.warn "No authQueryString"
	}
	
	if (state.JawboneAccessToken) {
		log.debug "Access token already exists"
		setup()
		success()
	} else {
		def code = params.code
		if (code) {
			if (code.size() > 6) {
				// Jawbone code
				log.debug "Exchanging code for access token"
				receiveToken(redirectUrl)
			} else {
				// SmartThings code, which we ignore, as we don't need to exchange for an access token.
				// Instead, go initiate the Jawbone OAuth flow.
				log.debug "Executing callback redirect to auth page"
			    state.oauthInitState = UUID.randomUUID().toString()
			    def oauthParams = [response_type: "code", client_id: appSettings.clientId, scope: "move_read sleep_read", redirect_uri: "${serverUrl}/oauth/callback"]
				redirect(location: "https://jawbone.com/auth/oauth2/auth?${toQueryString(oauthParams)}")
			}
		} else {
			log.debug "This code should be unreachable"
			success()
		}
	}
}

def authPage() {
    log.debug "authPage"
    def description = null          
    if (state.JawboneAccessToken == null) {
		if (!state.accessToken) {
			log.debug "About to create access token"
			createAccessToken()
		}
        description = "Click to enter Jawbone Credentials"
        def redirectUrl = buildRedirectUrl
        log.debug "RedirectURL = ${redirectUrl}"
        def donebutton= state.JawboneAccessToken != null 
        return dynamicPage(name: "Credentials", title: "Jawbone UP", nextPage: null, uninstall: true, install: donebutton) {
               section { href url:redirectUrl, style:"embedded", required:true, title:"Jawbone UP", state: hast ,description:description }
        }
    } else {
        description = "Jawbone Credentials Already Entered." 
        return dynamicPage(name: "Credentials", title: "Jawbone UP", uninstall: true, install:true) {
               section { href url: buildRedirectUrl("receivedToken"), style:"embedded", state: "complete", title:"Jawbone UP", description:description }
        }
    }
}

def oauthInitUrl() {
    log.debug "oauthInitUrl"
    state.oauthInitState = UUID.randomUUID().toString()
    def oauthParams = [ response_type: "code", client_id: appSettings.clientId, scope: "move_read sleep_read", redirect_uri: "${serverUrl}/oauth/callback" ]
	redirect(location: "https://jawbone.com/auth/oauth2/auth?${toQueryString(oauthParams)}")
}

def receiveToken(redirectUrl = null) {
	log.debug "receiveToken"
    def oauthParams = [ client_id: appSettings.clientId, client_secret: appSettings.clientSecret, grant_type: "authorization_code", code: params.code ]
    def params = [
      uri: "https://jawbone.com/auth/oauth2/token?${toQueryString(oauthParams)}",
    ]
    httpGet(params) { response -> 
    	log.debug "${response.data}"
		log.debug "Setting access token to ${response.data.access_token}, refresh token to ${response.data.refresh_token}"
    	state.JawboneAccessToken = response.data.access_token
		state.refreshToken = response.data.refresh_token
    }

	setup()
	if (state.JawboneAccessToken) {
		success()
	} else {
		def message = """
			<p>The connection could not be established!</p>
			<p>Click 'Done' to return to the menu.</p>
		"""
		connectionStatus(message)
	}
}

def success() {
	def message = """
		<p>Your Jawbone Account is now connected to SmartThings!</p>
		<p>Click 'Done' to finish setup.</p>
	"""
	connectionStatus(message)
}

def receivedToken() {
	def message = """
		<p>Your Jawbone Account is already connected to SmartThings!</p>
		<p>Click 'Done' to finish setup.</p>
	"""
	connectionStatus(message)
}

def connectionStatus(message, redirectUrl = null) {
	def redirectHtml = ""
	if (redirectUrl) {
		redirectHtml = """
			<meta http-equiv="refresh" content="3; url=${redirectUrl}" />
		"""
	}
	
    def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=640">
        <title>SmartThings Connection</title>
        <style type="text/css">
            @font-face {
                font-family: 'Swiss 721 W01 Thin';
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot');
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot?#iefix') format('embedded-opentype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.woff') format('woff'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.ttf') format('truetype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.svg#swis721_th_btthin') format('svg');
                font-weight: normal;
                font-style: normal;
            }
            @font-face {
                font-family: 'Swiss 721 W01 Light';
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot');
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot?#iefix') format('embedded-opentype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.woff') format('woff'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.ttf') format('truetype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.svg#swis721_lt_btlight') format('svg');
                font-weight: normal;
                font-style: normal;
            }
            .container {
                width: 560px;
                padding: 40px;
                /*background: #eee;*/
                text-align: center;
            }
            img {
                vertical-align: middle;
            }
            img:nth-child(2) {
                margin: 0 30px;
            }
            p {
                font-size: 2.2em;
                font-family: 'Swiss 721 W01 Thin';
                text-align: center;
                color: #666666;
                padding: 0 40px;
                margin-bottom: 0;
            }
        /*
            p:last-child {
                margin-top: 0px;
            }
        */
            span {
                font-family: 'Swiss 721 W01 Light';
            }
        </style>
		${redirectHtml}
        </head>
        <body>
            <div class="container">
                <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSMuoEIQ7gQhFtc02vXkybwmH0o7L1cs5mtbcJye0mgNqop_LOZbg" alt="Jawbone UP icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                ${message}
            </div>
        </body>
        </html>
	"""
	render contentType: 'text/html', data: html
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def validateCurrentToken() {
	log.debug "validateCurrentToken"
    def url = "https://jawbone.com/nudge/api/v.1.1/users/@me/refreshToken"
    def requestBody = "secret=${appSettings.clientSecret}"
	
	try {
		httpPost(uri: url, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ],  body: requestBody) {response ->
	    	if (response.status == 200) {
				log.debug "${response.data}"
				log.debug "Setting refresh token to ${response.data.data.refresh_token}"
	     		state.refreshToken = response.data.data.refresh_token
	        }
	    }
	} catch (groovyx.net.http.HttpResponseException e) {
        if (e.statusCode == 401) { // token is expired
        	log.debug "Access token is expired"
        	if (state.refreshToken) { // if we have this we are okay
    			def oauthParams = [client_id: appSettings.clientId, client_secret: appSettings.clientSecret, grant_type: "refresh_token", refresh_token: state.refreshToken]
        		def tokenUrl = "https://jawbone.com/auth/oauth2/token?${toQueryString(oauthParams)}"
        		def params = [
          			uri: tokenUrl
        		]
        		httpGet(params) { refreshResponse ->
					def data = refreshResponse.data
					log.debug "Status: ${refreshResponse.status}, data: ${data}"
					if (data.error) {
						if (data.error == "access_denied") {
							// User has removed authorization (probably)
							log.warn "Access denied, because: ${data.error_description}"
							state.remove("JawboneAccessToken")
							state.remove("refreshToken")
						}
					} else {
						log.debug "Setting access token to ${data.access_token}, refresh token to ${data.refresh_token}"
						state.JawboneAccessToken = data.access_token
						state.refreshToken = data.refresh_token
					}
				}
            }
        }
	} catch (java.net.SocketTimeoutException e) {
		log.warn "Connection timed out, not much we can do here"
	}
}

def initialize() {
    log.debug "Callback URL - Webhook"  
	def localServerUrl = getApiServerUrl() 
	def hookUrl = "${localServerUrl}/api/token/${state.accessToken}/smartapps/installations/${app.id}/hookCallback"
    def webhook = "https://jawbone.com/nudge/api/v.1.1/users/@me/pubsub?webhook=$hookUrl"      
	httpPost(uri: webhook, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ])
}

def setup() {
	// make sure this is going to work
	validateCurrentToken()

	if (state.JawboneAccessToken) {
		def urlmember = "https://jawbone.com/nudge/api/users/@me/"
		def member = null    
		httpGet(uri: urlmember, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
		    member = response.data.data
		}
	
		if (member) {
			state.member = member
			def externalId = "${app.id}.${member.xid}"

			// find the appropriate child device based on my app id and the device network id 
			def deviceWrapper = getChildDevice("${externalId}")

			// invoke the generatePresenceEvent method on the child device
			log.debug "Device $externalId: $deviceWrapper"
			if (!deviceWrapper) {
			  	def childDevice = addChildDevice('juano2310', "Jawbone User", "${app.id}.${member.xid}",null,[name:"Jawbone UP - " + member.first, completedSetup: true])
			    if (childDevice) {
			       	log.debug "Child Device Successfully Created"
			        generateInitialEvent (member, childDevice)
			    }
			}
		}

		initialize()
	}
}

def installed() {
	
	if (!state.accessToken) {
		log.debug "About to create access token"
		createAccessToken()
	}

	if (state.JawboneAccessToken) {
		setup()
	}
}

def updated() {
	
	if (!state.accessToken) {
		log.debug "About to create access token"
		createAccessToken()
	}

	if (state.JawboneAccessToken) {
		setup()
	}
}

def uninstalled() {
	if (state.JawboneAccessToken) {
		try {
			httpDelete(uri: "https://jawbone.com/nudge/api/v.1.0/users/@me/PartnerAppMembership", headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) { response ->
				log.debug "Success disconnecting Jawbone from SmartThings"
			}
		} catch (groovyx.net.http.HttpResponseException e) {
			log.error "Error disconnecting Jawbone from SmartThings: ${e.statusCode}"
		}
	}
}

def pollChild(childDevice) {
    def member = state.member 
    generatePollingEvents (member, childDevice)   
}

def generatePollingEvents (member, childDevice) {
    // lets figure out if the member is currently "home" (At the place)
    def urlgoals = "https://jawbone.com/nudge/api/users/@me/goals" 
    def urlmoves = "https://jawbone.com/nudge/api/users/@me/moves"  
    def urlsleeps = "https://jawbone.com/nudge/api/users/@me/sleeps"     
    def goals = null
    def moves = null
 	def sleeps = null   
    httpGet(uri: urlgoals, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
        goals = response.data.data
    }   
    httpGet(uri: urlmoves, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
        moves = response.data.data.items[0]
    }    
   
    try { // we are going to just ignore any errors
        log.debug "Member = ${member.first}"
        log.debug "Moves Goal = ${goals.move_steps} Steps"
        log.debug "Moves = ${moves.details.steps} Steps" 

        childDevice?.sendEvent(name:"steps", value: moves.details.steps)
        childDevice?.sendEvent(name:"goal", value: goals.move_steps)
    	//setColor(moves.details.steps,goals.move_steps,childDevice)
    }
    catch (e) {
            // eat it
    }       
}

def generateInitialEvent (member, childDevice) {
    // lets figure out if the member is currently "home" (At the place)
    def urlgoals = "https://jawbone.com/nudge/api/users/@me/goals" 
    def urlmoves = "https://jawbone.com/nudge/api/users/@me/moves"  
    def urlsleeps = "https://jawbone.com/nudge/api/users/@me/sleeps"     
    def goals = null
    def moves = null
 	def sleeps = null   
    httpGet(uri: urlgoals, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
        goals = response.data.data
    }   
    httpGet(uri: urlmoves, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
        moves = response.data.data.items[0]
    }    
    
    try { // we are going to just ignore any errors
        log.debug "Member = ${member.first}"
        log.debug "Moves Goal = ${goals.move_steps} Steps"
        log.debug "Moves = ${moves.details.steps} Steps"
        log.debug "Sleeping state = false"   
        childDevice?.generateSleepingEvent(false)
        childDevice?.sendEvent(name:"steps", value: moves.details.steps)
        childDevice?.sendEvent(name:"goal", value: goals.move_steps)
    	//setColor(moves.details.steps,goals.move_steps,childDevice)
    }
    catch (e) {
            // eat it
    }       
}

def setColor (steps,goal,childDevice) {
    def result = steps * 100 / goal
    if (result < 25) 
    	childDevice?.sendEvent(name:"steps", value: "steps", label: steps)
    else if ((result >= 25) && (result < 50)) 
        childDevice?.sendEvent(name:"steps", value: "steps1", label: steps)
    else if ((result >= 50) && (result < 75)) 
        childDevice?.sendEvent(name:"steps", value: "steps1", label: steps)
    else if (result >= 75)     
        childDevice?.sendEvent(name:"steps", value: "stepsgoal", label: steps)   
}

def hookEventHandler() {
    // log.debug "In hookEventHandler method."
    log.debug "request = ${request}"
    
    def json = request.JSON 
    
    // get some stuff we need
    def userId = json.events.user_xid[0]
    def	json_type = json.events.type[0]
	def json_action = json.events.action[0]

    //log.debug json
    log.debug "Userid = ${userId}"
    log.debug "Notification Type: " + json_type
    log.debug "Notification Action: " + json_action 
    
    // find the appropriate child device based on my app id and the device network id
    def externalId = "${app.id}.${userId}"
    def childDevice = getChildDevice("${externalId}")
            
    if (childDevice) {
    	switch (json_action) {   
	        case "enter_sleep_mode":      
            	childDevice?.generateSleepingEvent(true)           
                break           
            case "exit_sleep_mode":       
            	childDevice?.generateSleepingEvent(false) 
                break 
            case "creation": 
                childDevice?.sendEvent(name:"steps", value: 0)
          		break
            case "updation":
                def urlgoals = "https://jawbone.com/nudge/api/users/@me/goals"     
                def urlmoves = "https://jawbone.com/nudge/api/users/@me/moves"       
                def goals = null
                def moves = null    
                httpGet(uri: urlgoals, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
 	               goals = response.data.data
                }       
                httpGet(uri: urlmoves, headers: ["Authorization": "Bearer ${state.JawboneAccessToken}" ]) {response -> 
                   moves = response.data.data.items[0]
                }        
                log.debug "Goal = ${goals.move_steps} Steps"
        		log.debug "Steps = ${moves.details.steps} Steps"
                childDevice?.sendEvent(name:"steps", value: moves.details.steps)
                childDevice?.sendEvent(name:"goal", value: goals.move_steps)       
                //setColor(moves.details.steps,goals.move_steps,childDevice)   
                break
			case "deletion":
				app.delete()
				break
		}
    }
    else {
            log.debug "Couldn't find child device associated with Jawbone."
    }

	def html = """{"code":200,"message":"OK"}"""
	render contentType: 'application/json', data: html
}
