/**
 *  Alarm.com Service Manager
 *
 *  Author: Schwark Satyavolu
 *
 */

definition(
    name: "Alarm.com",
    namespace: "schwark",
    author: "Schwark Satyavolu",
    description: "Allows you to connect your Alarm.com alarm system with SmartThings and control them from your Things area or Dashboard in the SmartThings Mobile app.",
    category: "SmartThings Labs",
    iconUrl: "https://images-na.ssl-images-amazon.com/images/I/71yQ11GAAiL.png",
    iconX2Url: "https://images-na.ssl-images-amazon.com/images/I/71yQ11GAAiL.png",
    singleInstance: true
) 

preferences {
	input("username", "string", title:"Username", description: "Please enter your Alarm.com username", required: true, displayDuringSetup: true)
	input("password", "password", title:"Password", description: "Please enter your Alarm.com password", required: true, displayDuringSetup: true)
	input("disarm", "bool", title:"Add Disarm Switch as well", description: "Disarm button is only added if this is set to on", required: false, displayDuringSetup: true, defaultValue: false )

}

/////////////////////////////////////
def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def uninstalled() {
	log.debug("Uninstalling with settings: ${settings}")
	unschedule()

	removeChildDevices(getChildDevices())
}

/////////////////////////////////////
def updated() {
	//log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

/////////////////////////////////////
def initialize() {
	// remove location subscription aftwards
	unsubscribe()
	state.subscribe = false

	runIn(60*5, doDeviceSync)
}

def getHubId() {
	return state.hubId ? state.hubId : location.hubs[0].id
}

/////////////////////////////////////
def locationHandler(evt) {
	//log.debug "$locationHandler(evt.description)"
	def description = evt.description
	def hub = evt?.hubId
	state.hubId = hub
	//log.debug("location handler: event description is ${description}")
}

def getCookieValue(browserSession, name) {
	def value = ""
	if(browserSession && browserSession.cookies) {
		browserSession.cookies.each {cookie ->
			def cookieList = cookie.split('=', 2)
			def cookieName = cookieList[0]
			def cookieValue = cookieList[1]
			//log.debug("looking for ${name}: current cookie is ${cookieName} with value ${cookieValue}")
			if(name == cookieName) {
				value = cookieValue
			}
		}
	}

	return value
}

/////////////////////////////////////
private def parseEventMessage(Map event) {
	//handles gateway attribute events
	return event
}

private def parseEventMessage(String description) {
}


private def getCommand(command=null, silent=true, nodelay=false, bypass=false) {
	log.debug("getCommand got command ${command} with silent ${silent} and nodelay of ${nodelay}")
	def COMMANDS = [
					'ARMSTAY': ['params': ['command':'/armStay', 'silent': silent?'true':'false', 'nodelay': nodelay?'true':'false'], 'name': 'Arm Stay', button: true],
					'ARMAWAY': ['params': ['command':'/armAway', 'silent': silent?'true':'false', 'nodelay': nodelay?'true':'false'], 'name': 'Arm Away', button: true],
					'DISARM': ['params': ['command':'/disarm', 'silent': silent?'true':'false', 'nodelay': nodelay?'true':'false'], 'name': 'Disarm', button: settings.disarm],
					'STATUS': ['params': ['command':'', 'silent': silent?'true':'false', 'nodelay': nodelay?'true':'false'], 'name': 'Status', button: false]
				   ]

	return command ? COMMANDS[command] : COMMANDS
}

private def toQueryString(Map m)
{
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

private def getRecipe(command, silent=true, nodelay=false, bypass=false) {
	def COMMAND = getCommand(command, silent, nodelay, bypass)
	log.debug("getRecipe got command: silent is ${silent} and nodelay is ${nodelay} and bypass is ${bypass} and command is ${command} and COMMAND is ${COMMAND} and panelid is ${state.panelid}")
	def apiMethod = 'post'
	def postBody = '{"forceBypass":'+bypass+',"noEntryDelay":'+nodelay+',"silentArming":'+silent+',"statePollOnly":false}'
	if('STATUS' == command) {
		apiMethod ='get'
		postBody = ''
	}
	def urlext = (state.panelid ? state.panelid : '${dataunit}${extension}')
	def STEPS = [
			['name': 'initlogin', 'uri': 'https://www.alarm.com/login.aspx'],
			['name': 'login', 'uri': 'https://www.alarm.com/web/Default.aspx', 'method':'post', 'variables':[
			  	'__VIEWSTATE':'',
			  	'__VIEWSTATEGENERATOR':'',
			  	'__EVENTVALIDATION':'',
			  	'IsFromNewSite': '1',
			  	'JavaScriptTest':  '1',
			  	'ctl00$ContentPlaceHolder1$loginform$hidLoginID': '',
				'ctl00$ContentPlaceHolder1$loginform$txtUserName': settings.username,
			  	'ctl00$ContentPlaceHolder1$loginform$txtPassword': settings.password,
			  	'ctl00$ContentPlaceHolder1$loginform$signInButton':'Logging In...',
			  	'ctl00$bottom_footer3$ucCLS_ZIP$txtZip': 'Zip Code'
			 ], 'state': ['afg':'cookie:afg'], 'nofollow': true ],
		     //['name': 'userextract', 'requestContentType': 'application/json; charset=UTF-8', 'contentType': 'application/vnd.api+json', 'uri': 'https://www.alarm.com/web/api/systems/availableSystemItems', 'headers': ['ajaxrequestuniquekey': '${afg}'], 'body': '', 'state': ['userid': 'json:data.0.id']],
		     //['name': 'panelextract', 'requestContentType': 'application/json; charset=UTF-8', 'contentType': 'application/vnd.api+json', 'uri': 'https://www.alarm.com/web/api/systems/systems/${userid}', 'headers': ['ajaxrequestuniquekey': '${afg}'], 'body': '', 'state': ['panelid': 'json:data.relationships.partitions.data.0.id']],
			 ['name': 'idextract', 'uri': 'https://www.alarm.com/web/History/EventHistory.aspx', 'state': ['dataunit': 'ctl00__page_html.data-unit-id', 'extension': 'ctl00_phBody_ddlDevice.optionvalue#Panel', 'afg':'cookie:afg']],			 
		     ['name': command, 'method': apiMethod, 'requestContentType': 'application/json; charset=UTF-8', 'contentType': 'application/vnd.api+json', 'uri': 'https://www.alarm.com/web/api/devices/partitions/'+urlext+COMMAND.params.command, 'headers': ['ajaxrequestuniquekey': '${afg}'], 'body': postBody, 'state': ['status': ~/(?ms)"state": (\d)\,/]]
	]
	if(state.panelid) {
		STEPS.remove(2)
		//STEPS.remove(2)
	}
	return STEPS.reverse()
}

private def updateStatus(command, status, browserSession=null) {
	def on = null
	def id = ['0': 'UNKNOWN', '1': 'DISARM', '2': 'ARMSTAY', '3': 'ARMAWAY']
	if('STATUS' == command && status) {
		on = id[status] 
	} else {
		on = command
	}
	log.debug("updating status to ${on} on command ${command}")

	if(!state.panelid && browserSession && browserSession.state.dataunit && browserSession.state.extension) {
		state.panelid = browserSession.state.dataunit+browserSession.state.extension
		log.debug("setting panel id to ${state.panelid}")
	}

	if(on && 'STATUS' != on) {
		def PREFIX = getPrefix()
		def COMMANDS = getCommand()
		COMMANDS.each { key, map ->
			def device = getChildDevice("${PREFIX}${key}")
			if(device) {
				log.debug("trying to update status of child device ${key}")
				if( on == key) {
					device.sendEvent(name:"switch", value:"on")
					log.debug("sending event on to ${key}")
				} else {
					device.sendEvent(name:"switch", value:"off")
					log.debug("sending event off to ${key}")
				}
			}			
		}
	}
}

def runCommand(command, silent, nodelay, bypass=false, browserSession=[:]) {
	browserSession.vars = ['__VIEWSTATEGENERATOR':'','__EVENTVALIDATION':'','__VIEWSTATE':'']

	log.debug("runCommand got command ${command} with silent ${silent} and nodelay of ${nodelay} and bypass of ${bypass}")
	def recipes = getRecipe(command, silent, nodelay, bypass)
	navigateUrl(recipes, browserSession)
	updateStatus(command, (browserSession.state && browserSession.state.status) ? browserSession.state.status : null, browserSession)

	return browserSession
}

private def getPatternValue(html, browserSession, kind, variable, pattern=null) {
	if(!pattern) {
        pattern = ~/(?ms)name="${variable}".*?value="([^"]*)"/
    }
	log.debug("looking for values with pattern ${pattern} for ${variable}")
	def value = null
	if(html) {
		html = "${html}"
		if(!browserSession[kind]) browserSession[kind] = [:]
		def group = (html =~ pattern)
		if(group) {
			log.debug "found variable value ${group[0][1]} for ${variable}"
			value = group[0][1]
			browserSession[kind][variable] = value
		}
	}
	return value
}

private def visitNodes(node, processor) {
	def stack = [node]
	
    def current = null
	while(stack && (current = stack.pop())) {
		if(processor) {
			if(processor(current)) return
		}
        if(current instanceof groovy.util.slurpersupport.Node) {
			current.children().each {
				stack.add(0,it)
			}
        }
	}
}

private def getIdAttr(html, id, attr) {
	def result = null
	if(html && id && attr) {
	   visitNodes(html[0]) {
	    	if(it instanceof groovy.util.slurpersupport.Node) {
		        def attributes = it.attributes()
		        if(attributes && attributes['id'] == id) {
		        	if(attr.startsWith('optionvalue#')) {
			        	def text = attr.split('#')[1]
		        		it.children.each { child ->
			        		//log.debug("checking option with text ${child.text()} and ${child.attributes()} for ${attr} and ${id}")
		        			if(child.name == 'OPTION' && child.text() == text) {
				        		result = child.attributes()['value']
		        			}
		        		}
		        	} else if('#text' == attr) 
		        		result = it.text()
		        	else {
		        		result = attributes ? attributes[attr] : null
		        	}

	        	} 
	        }
	        return result
	    }
	}
    return result
}

private def getJsonValue(html, browserSession, jsonPath) {
	def obj = html
	if(!(obj instanceof java.util.Map)) {
		def jsonSlurper = new groovy.json.JsonSlurper()
		obj = jsonSlurper.parseText("${html}")		
	}
	def parts = jsonPath.tokenize('.')
	parts.each { 
		if(it.isInteger()) it = it.toInteger()
		obj = obj[it]
	}

	return obj
}

private def extractSession(params, response, browserSession) {
	//log.debug("extracting session variables..")
	def count = 1
	def html = response.data
    
    if(params.state) {
    	params.state.each { name, pattern ->
    		if(pattern instanceof java.util.regex.Pattern) {
		    	getPatternValue(html, browserSession, 'state', name, pattern)
    			log.debug("state variable ${name} set to ${browserSession.state[name]} from regex ${pattern} with content ${html}")
    		} else if(pattern instanceof String) {
    			if(pattern.startsWith('cookie:')) {
    				def cookieName = pattern.minus('cookie:')
    				browserSession.state[name] = getCookieValue(browserSession,cookieName)
    				log.debug("state variable ${name} set to ${browserSession.state[name]} from cookie")
    			} else if(pattern.startsWith('json:')) {
    				def jsonPath = pattern.minus('json:')
    				browserSession.state[name] = getJsonValue(html, browserSession,jsonPath)
    				log.debug("state variable ${name} set to ${browserSession.state[name]} from json")
    			} else {
		    		def parts = pattern.tokenize('.')
		    		browserSession.state[name] = parts.size() == 2 ? getIdAttr(html, parts[0], parts[1]) : null
    				log.debug("state variable ${name} set to ${browserSession.state[name]} from html parse with pattern ${pattern}")
		    	}
		    } 
    	}
	}

    browserSession.vars.each() { n, v ->
    	browserSession.vars[n] = ''
    }
        
    visitNodes(html[0]) {
    	if(it instanceof groovy.util.slurpersupport.Node) {
    		if(it.name == 'INPUT') {
        	def attr = it.attributes()
            if(attr) {
            	def name = attr['name']
                if(name) {
					def value = attr['value'] ? attr['value'] : ''
						if(browserSession.vars.containsKey(name)) {
							browserSession.vars[name] = value
							//log.debug "found form value ${value} for ${name}"
						}
					}
           		}
        	}
    	}
        return false
    }
    
	return browserSession
}

private def fillTemplate(template, map) {
	if(!map) return template
	def result = template.replaceAll(/\$\{(\w+)\}/) { k -> map[k[1]] ?: k[0] }
	return result
}

private def navigateUrl(recipes, browserSession) {
	if(!recipes.size()) return

    def params = recipes.pop()
    log.debug("processing recipe ${params.name}")

	def success = { response ->
    	log.trace("response status is ${response.status} for ${params.name} and uri:${params.uri}")

    	browserSession.cookies = !browserSession.get('cookies') ? [] : browserSession.cookies
    	response.headerIterator('Set-Cookie').each {
    		//log.debug "adding cookie to cookie jar: ${it}"
      		browserSession.cookies.add(it.value.split(';')[0])
    	}
		extractSession(params, response, browserSession)
	    if(params.processor) params.processor(response, browserSession)

    	if(response.status == 200) {
    		def text = "${response.data}"
    		browserSession.completedUrl = params.uri
	    	if(params.expect) {
	    		if(params.expect.content) {
	    			log.debug((text =~ params.expect.content) ? "${params.name} is successful by content" : "${params.name} has failed by content")
	    		} else if(params.expect.location) {
	    			log.debug((browserSession.completedUrl =~ params.expect.location) ? "${params.name} is successful by location" : "${params.name} has failed by location at ${browserSession.completedUrl} - expecting ${params.expect.location}")
				}
	    	}
	    } else if(response.status == 302 && !params.nofollow) {
	    	response.headerIterator('Location').each {
    			def location = params.uri.toURI().resolve(it.value).toString()
    			log.debug "redirecting on 302 to: ${location}"
    			recipes.push(['name': "${params.name} redirect", 'uri': location, 'expect': params.expect, 'processor': params.processor, 'state': params.state])
    		}
	    }

		if(recipes) {
			if(!recipes[-1].referer) recipes[-1].referer = params.uri
			navigateUrl(recipes, browserSession)
		}

	    //return browserSession
    }

	if(params.uri) {
		if(!browserSession.state) browserSession.state = [:]
		params.uri = fillTemplate(params.uri, browserSession.vars + browserSession.state)
        if(!params.headers) params.headers = [:]
		if(!params.headers['Origin']) params.headers['Host'] = params.uri.toURI().host
		params.headers['User-Agent'] = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.110 Safari/537.36'
		if(!params.contentType) params.headers['Accept'] = 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
		params.headers['Accept-Language'] = 'en-US,en;q=0.5'

		//if(params.referer == 'self') params.referer = params.uri
		//if(params.referer) params.headers['Referer'] = params.referer
		if(browserSession.cookies) {
			params.headers['Cookie'] = browserSession.cookies.join(";")
		}
		if(browserSession.vars) {
			params.variables = (params.variables ? params.variables : [:])
			params.variables.each {name, value ->
				if(!value && browserSession.vars[name]) params.variables[name] = browserSession.vars[name]
			}
		}
        params.headers.each {name, value ->
        	params.headers[name] = fillTemplate(value, browserSession.vars + browserSession.state)
        }
		try {
			log.debug("navigation:${params}")
			if(!params.method) params.method = 'get'
			log.debug("navigation::${params.method} ${params.uri}")
			//params.headers.each{key, value ->
			//	log.debug("header:${params.uri}::${key}:${value}")
			//}
			if(params.method == 'post' && (params.variables || params.body)) {				
				if(params.variables) {
					params.body = toQueryString(params.variables)
				}
				//log.debug("postbody:${params.uri}::${params.body}")
				httpPost(params, success)
			} else {
				if(params.variables) params.query = params.variables
				//log.debug("getquery:${params.uri}::${params.query}")
	    		httpGet(params,success)
   			}
		} catch (e) {
    			log.error "something went wrong: $e"
		}
	}

	return browserSession
}

/////////////////////////////////////
def doDeviceSync(){
	log.debug "Doing Alarm.com Device Sync!"

	if(!state.subscribe) {
		subscribe(location, null, locationHandler, [filterEvents:false])
		state.subscribe = true
	}

	createSwitches()
}


////////////////////////////////////////////
//CHILD DEVICE METHODS
/////////////////////////////////////
def parse(childDevice, description) {
	def parsedEvent = parseEventMessage(description)

	if (parsedEvent.headers && parsedEvent.body) {
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())
		log.debug "parse() - ${bodyString}"
	} else {
		log.debug "parse - got something other than headers,body..."
		return []
	}
}

def getPrefix() {
	return "ALARMCOM"
}

def createSwitches() {
	log.debug("Creating Alarm.com Switches...")

	def PREFIX = getPrefix()
	def COMMANDS = getCommand()
	
	// add missing devices
	COMMANDS.each() { id, map ->
		def name = map['name']
		log.debug("processing switch ${id} with name ${name}")
		def hubId = getHubId()
		def device = getChildDevice("${PREFIX}${id}")
		if(map.button && !device) {
			def alarmSwitch = addChildDevice("schwark", "Alarm.com Switch", "${PREFIX}${id}", hubId, ["name": "AlarmCom.${id}", "label": "${name}", "completedSetup": true])
			log.debug("created child device ${PREFIX}${id} with name ${name} and hub ${hubId}")
			alarmSwitch.setCommand(id)
			device = alarmSwitch
			if(id == 'DISARM' && device) { // reverse icon states
				device.setIcon("on", "switch", "st.security.alarm.off")
				device.setIcon("off", "switch", "st.security.alarm.on")
				device.save()
			}
		}
	}

	// remove disabled devices
	def children = getChildDevices()
	children.each {
		if(it && it.deviceNetworkId) {
			def id = it.deviceNetworkId
			if(id.startsWith(PREFIX)) {
				id = id - PREFIX
				def button = COMMANDS[id] ? COMMANDS[id].button : false
				if(!button) deleteChildDevice(it.deviceNetworkId)
			}
		}
	}
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Boolean canInstallLabs()
{
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}

private removeChildDevices(data) {
    data.delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}