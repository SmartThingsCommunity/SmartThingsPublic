/**
 *  SmartTiles 5.3.3
 *
 *  Visit Home Page for more information:
 *  http://SmartTiles.click
 *
 *  If you like this app, please support the developer via PayPal: donate@SmartTiles.click
 *
 *  This software if free for Private Use. You may use and modify the software without distributing it.
 *  
 *  This software and derivatives may not be used for commercial purposes.
 *  You may not modify, distribute or sublicense this software.
 *  You may not grant a sublicense to modify and distribute this software to third parties not included in the license.
 *
 *  Software is provided without warranty and the software author/license owner cannot be held liable for damages.
 *
 *  Copyright © 2014 Alex Malikov
 *
 */
definition(
    name: "SmartTiles Tablet ${appVersion()}-${appStream()}",
    namespace: "625alex",
    author: "Alex Malikov",
    description: "SmartTiles Dashboard, Tablet.",
    category: "SmartThings Labs",
    iconUrl: "https://625alex.github.io/SmartTiles/prod/icon.png",
    iconX2Url: "https://625alex.github.io/SmartTiles/prod/icon.png",
    oauth: true)

def appVersion() {"5.3.3"}
def appStream() {"M"}

preferences {
	page(name: "selectDevices", install: false, uninstall: true, nextPage: "nextPage") {
        section("About") {
            paragraph "SmartTiles Dashboard, a SmartThings web client.\n\nYour home has a Home Page!™"
            paragraph "Version ${appVersion()}-${appStream()}\n\n" +
            "If you like this app, please support the developer via PayPal:\n\ndonate@SmartTiles.click\n\n" +
            "Copyright © 2014 Alex Malikov"
			href url:"http://SmartTiles.click", style:"embedded", required:false, title:"More information...", description:"www.SmartTiles.click"
        }
		
		section() {
			href "controlThings", title:"Things"
		}
		
        section() {
			href "videos", title:"Video Streams"
		}
		
		section() {
			href "shortcuts", title:"Shortcuts"
		}
		
		section() {
			href "moreTiles", title: "Other Tiles"
		}
		
		section() {
			href "prefs", title: "Preferences"
		}
    }
	
	page(name: "controlThings", title: "Things", install: false) {
		section("Control lights...") {
			input "lights", "capability.switch", title: "Lights...", multiple: true, required: false
			input "dimmerLights", "capability.switchLevel", title: "Dimmable Lights...", multiple: true, required: false
			input "switches", "capability.switch", title: "Switches...", multiple: true, required: false
			input "dimmers", "capability.switchLevel", title: "Dimmable Switches...", multiple: true, required: false
			input "momentaries", "capability.momentary", title: "Momentary Switches...", multiple: true, required: false
			input "themeLights", "capability.switch", title: "Theme Lights...", multiple: true, required: false
		}
		
		section("Control thermostats...") {
			input "thermostatsHeat", "capability.thermostat", title: "Heating Thermostats...", multiple: true, required: false
			input "thermostatsCool", "capability.thermostat", title: "Cooling Thermostats...", multiple: true, required: false
		}
		
		section("Control things...") {
			input "locks", "capability.lock", title: "Locks...", multiple: true, required: false
			input "music", "capability.musicPlayer", title: "Music Players...", multiple: true, required: false
			input "camera", "capability.imageCapture", title: "Cameras (Image Capture)...", multiple: true, required: false
		}
		
		section("View state of things...") {
            input "presence", "capability.presenceSensor", title: "Presence Sensors...", multiple: true, required: false
            input "contacts", "capability.contactSensor", title: "Contact Sensors...", multiple: true, required: false
            input "motion", "capability.motionSensor", title: "Motion Sensors...", multiple: true, required: false
            input "temperature", "capability.temperatureMeasurement", title: "Temperature...", multiple: true, required: false
            input "humidity", "capability.relativeHumidityMeasurement", title: "Hygrometer...", multiple: true, required: false
            input "water", "capability.waterSensor", title: "Water Sensors...", multiple: true, required: false
            input "battery", "capability.battery", title: "Battery Status...", multiple: true, required: false
            input "energy", "capability.energyMeter", title: "Energy Meters...", multiple: true, required: false
            input "power", "capability.powerMeter", title: "Power Meters...", multiple: true, required: false
            input "acceleration", "capability.accelerationSensor", title: "Vibration Sensors...", multiple: true, required: false
            input "luminosity", "capability.illuminanceMeasurement", title: "Luminosity Sensors...", multiple: true, required: false
            input "weather", "device.smartweatherStationTile", title: "Weather...", multiple: true, required: false
        }
	}
	
	page(name: "videos")
	page(name: "videoStreams")
	page(name: "videoStreamsMJPEG")
	page(name: "shortcuts")
	page(name: "dashboards")
	page(name: "links")
	page(name: "prefs")
	page(name: "moreTiles")
	page(name: "authenticationPreferences")
	page(name: "resetOauth")
	page(name: "viewURL")
	page(name: "nextPage")
}

def videos() {
	dynamicPage(name: "videos", title: "Video Streams", install: false) {
		section() {
			href url:"http://www.smarttiles.click/info/#video", style:"embedded", required:false, title:"More information...", description:"www.smarttiles.click/info/#video"
		}
		
		section() {
			href "videoStreams", title:"Dropcam video streams"
		}
		
		section() {
			href "videoStreamsMJPEG", title:"Generic MJPEG video streams", description: "Foscam, Blue Iris, etc"
		}
	}
}

def videoStreams() {
	dynamicPage(name: "videoStreams", title: "Video Streams", install: false) {
		section("About") {
			paragraph "Enter absolute URL of the stream starting with http..."
		}
		
		(1..10).each{
			def vsTitle = "dropcamStreamT$it"
			def vsLink = "dropcamStreamUrl$it"
			section("Dropcam Video Stream $it") {
				input vsTitle, "text", title:"Title", required: false
				input vsLink, "text", title:"URL", required: false
			}
		}
	}
}

def videoStreamsMJPEG() {
	dynamicPage(name: "videoStreamsMJPEG", title: "Generic MJPEG Video Streams", install: false) {
		section("About") {
			paragraph "Enter absolute URL starting with http..."
			paragraph "For Foscam cameras use http://DOMAIN:PORT/videostream.cgi?&user=USERNAME&pwd=PASSWORD"
			paragraph "For BlueIris cameras use http://blueirisserver/mjpg/CAMERASHORTNAME/video.mjpeg"
			paragraph "Feel free to try other links for MJPEG Video Streams, your experience may vary.\n\nThere may be issues displaying these video streams using Chrome in iOS."
		}
		
		(1..10).each{
			def gvsTitle = "mjpegStreamTitile$it"
			def gvsLink = "mjpegStreamUrl$it"
			section("Generic MJPEG Video Stream $it") {
				input gvsTitle, "text", title:"Title", required: false
				input gvsLink, "text", title:"URL", required: false
			}
		}
	}
}

def shortcuts() {
	dynamicPage(name: "links", title: "Shortcuts", install: false) {
		section() {
			href "dashboards", title: "Links to other dashboards"
		}
		
		section() {
			href "links", title: "Links to other websites"
		}
	}
}

def links() {
	dynamicPage(name: "links", title: "Links", install: false) {
		section() {
			paragraph "Enter absolute URL starting with http..."
		}
		
		(1..10).each{
			def lTitle = "linkTitle$it"
			def lLink = "linkUrl$it"
			log.debug "t: $t, l: $l"
			section("Link $it") {
				input lTitle, "text", title:"Title", required: false
				input lLink, "text", title:"URL", required: false
			}
		}
	}
}

def dashboards() {
	dynamicPage(name: "dashboards", title: "Dashboards", install: false) {
		section() {
			paragraph "Enter absolute URL starting with https..."
		}
		(1..10).each{
			def dTitle = "dashboardTitle$it"
			def dLink = "dashboardUrl$it"
			log.debug "t: $t, l: $l"
			section("Dashboard $it") {
				input dTitle, "text", title:"Title", required: false
				input dLink, "text", title:"URL", required: false
			}
		}
	}
}

def moreTiles() {
	dynamicPage(name: "moreTiles", title: "More Tiles", install: false) {
		section() {
			input "showMode", title: "Mode", "bool", required: true, defaultValue: true
			input "showHelloHome", title: "Hello, Home!", "bool", required: true, defaultValue: true
			input "showRefresh", title: "Refresh", "bool", required: true, defaultValue: true
			input "showHistory", title: "Event History", "bool", required: true, defaultValue: true
			input "showClock", title: "Clock", "enum", multiple: false, required: true, defaultValue: "Small Analog", options: ["Small Analog", "Small Digital", "Large Analog", "Large Digital", "None"]
		}
	}
}

def prefs() {
	dynamicPage(name: "prefs", title: "Preferences", install: false) {
		section() {
			label title: "Title", required: false, defaultValue: "$location SmartTiles"
		}
		
		section() {
			input "theme", title: "Theme", "enum", multiple: false, required: true, defaultValue: "default", options: [default: "Metro (default)", slate: "Slate", quartz: "Quartz", onyx: "Onyx", cobalt: "Cobalt"]
			input "tileSize", title: "Tile Size", "enum", multiple: false, required: true, defaultValue: "Medium", options: ["Small", "Medium", "Large"]
			input "fontSize", title: "Font Size", "enum", multiple: false, required: true, defaultValue: "Normal", options: ["Normal", "Larger", "Largest"]
			input "dropShadow", title: "Drop Shadow", "bool", required: true, defaultValue: false
		}
		
		section() {
			input "roundNumbers", title: "Round Off Decimals", "bool", required: true, defaultValue:true
		}
		
		section() {
			input "themeLightType", title: "Theme Lights", "enum", multiple: false, required: true, defaultValue: "Default", options: ["Default", "Christmas", "Valentine's Day"]
		}
		
		if (state) {
			section() {
				href url:"${generateURL("list").join()}", style:"embedded", required:false, title:"Device Order", description: "Tap to change, then click \"Done\""
			}
			
			section() {
				href url:"${generateURL("css").join()}", style:"embedded", required:false, title:"Custom CSS", description: "Tap to change, then click \"Done\""
			}
		}
		
		section() {
			href "authenticationPreferences", title:"Access and Authentication"
		}
	}
}

def authenticationPreferences() {
	dynamicPage(name: "authenticationPreferences", title: "Access and Authentication", install: false) {
		section() {
			input "disableDashboard", "bool", title: "Disable temporarily (hide all tiles)?", defaultValue: false, required:false
			input "readOnlyMode", "bool", title: "View only mode?", defaultValue: false, required:false
		}
		section("Reset Access Token...") {
        	paragraph "Activating this option will invalidate access token. Access to all authenticated instances of this dashboard will be permanently revoked."
        	input "resetOauth", "bool", title: "Reset access token?", defaultValue: false
        }
	}
}

def resetOauth() {
	dynamicPage(name: "resetOauth", title: "Reset Access Token", install:false) {
		generateURL(null)
		
		section() {
			paragraph "You chose to reset Access Token in SmartTiles preferences."
			href "authenticationPreferences", title:"Reset Access Token", description: "Tap to set this option to \"OFF\""
		}
	}
}

def viewURL() {
	dynamicPage(name: "viewURL", title: "${title ?: location.name} SmartTiles URL", install:true, nextPage: null) {
		section() {
			paragraph "Copy the URL below to any modern browser to view ${title ?: location.name} SmartTiles. Add a shortcut to home screen of your mobile device to run as a native app."
			href url:"${generateURL("link").join()}", style:"embedded", required:false, title:"URL", description:"Tap to view, then click \"Done\""
		}
		
		section() {
			paragraph "Optionally, send SMS containing the URL of ${title ?: location.name} SmartTiles to a phone number. The URL will be sent in two parts because it's too long."
			input "phone", "phone", title: "Which phone?", required: false
		}
		
		section() {
			href "selectDevices", title:"Return to settings"
		}
	}
}

def nextPage() {
	if (state?.appVersionT != appVersion()) {
		log.debug "nextPage moreTiles"
		state.appVersionT = appVersion()
		moreTiles()
	} else if (state?.appVersionP != appVersion()) {
		log.debug "nextPage prefs"
		state.appVersionP = appVersion()
		prefs()
	} else if (settings.resetOauth) {
		log.debug "nextPage resetOauth"
		resetOauth()
	} else {
		log.debug "nextPage viewURL"
		viewURL()
    }
}

mappings {
	if (params.access_token && params.access_token != state.accessToken) {
		def oauthError = [GET: "oauthError"]
        path("/ui") {action: oauthError}
        path("/command") {action: oauthError}
        path("/data") {action: oauthError}
        path("/ping") {action: oauthError}
        path("/link") {action: oauthError}
        path("/list") {action: oauthError}
        path("/history") {action: oauthError}
        path("/position") {action: oauthError}
        path("/css") {action: [GET: "oauthError", POST: "oauthError"]}
	} else {
        path("/ui") {action: [GET: "html"]}
        path("/command") {action: [GET: "command"]}
        path("/data") {action: [GET: "allDeviceData"]}
        path("/ping") {action: [GET: "ping"]}
        path("/link") {action: [GET: "link"]}
		path("/list") {action: [GET: "list"]}
		path("/history") {action: [GET: "history"]}
		path("/position") {action: [GET: "position"]}
		path("/css") {action: [GET: "css", POST: "saveCSS"]}
    }
}

def oauthError() {[error: "OAuth token is invalid or access has been revoked"]}

def getMinTemp() {getTemperatureScale() == "F" ? 45 : 7}
def getMaxTemp() {getTemperatureScale() == "F" ? 90 : 30}

def command() {
	log.debug "command received with params $params"
	if (disableDashboard || readOnlyMode) return [status: "disabled"]

    def id = params.device
    def type = params.type
    def command = params.command
	def value = params.value

	def device

	if (type == "thermostatHeat" || type == "thermostatCool") {
		def deviceSet = (type == "thermostatHeat" ? thermostatsHeat : thermostatsCool)
		device = deviceSet?.find{it.id == id}
		value = value.toInteger()
		if (device) {
			if (value < getMinTemp()) value = getMinTemp()
			else if (value > getMaxTemp()) value = getMaxTemp()
			if (type == "thermostatHeat") device.setHeatingSetpoint(value)
			else device.setCoolingSetpoint(value)
		}
	} else if (type == "switch" || type == "light" || type == "themeLight") {
		def deviceSet = (type == "switch" ? switches : (type == "light" ? lights : themeLights))
		device = deviceSet?.find{it.id == id}
		if (device) {
			if(device.currentValue('switch') == "on") {
				device.off()
			} else {
				device.on()
			}
		}
	} else if (type == "lock") {
		device = locks?.find{it.id == id}
		if (device) {
			if(device.currentValue('lock') == "locked") {
                device.unlock()
            } else {
                device.lock()
            }
		}
	} else if (type == "dimmer" || type == "dimmerLight") {
		def deviceSet = (type == "dimmer" ? dimmers : dimmerLights)
		device = deviceSet?.find{it.id == id}
		if (device) {
			if (command == "toggle") {
				if(device.currentValue('switch') == "on") {
					device.off()
				} else {
					device.setLevel(Math.min((value as Integer) * 10, 99))
				}
			} else if (command == "level") {
				device.setLevel(Math.min((value as Integer) * 10, 99))
			}
		}
    } else if (type == "mode") {
		setLocationMode(command)
	} else if (type == "helloHome") {
        log.debug "executing Hello Home '$value'"
    	location.helloHome.execute(command)
    } else if (type == "momentary") {
    	momentaries?.find{it.id == id}?.push()
    } else if (type == "camera") {
    	camera?.find{it.id == id}.take()
    } else if (type == "music") {
		device = music?.find{it.id == id}
		if (device) {
			if (command == "level") {
				device.setLevel(Math.min((value as Integer) * 10, 99))
			} else {
				device."$command"()
			}
		}
	}
    
	[status:"ok"]
}

def position() {
	log.debug "command received with params $params"
	def map = [:]
	params?.list?.split("\\|~\\|").eachWithIndex{o, i -> map[o] = i}
	state.sortOrder = map
	log.debug "state.sortOrder: $state.sortOrder"
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	
	initialize()
}

def initialize() {
    weatherRefresh()
	runEvery15Minutes(updateStateTS)
	runEvery30Minutes(weatherRefresh)
    
	sendURL_SMS("ui")
	
	updateStateTS()
	
	subscribe(location, handler)
	subscribe(themeLights, "switch.on", handler, [filterEvents: false])
	subscribe(themeLights, "switch.off", handler, [filterEvents: false])
	subscribe(themeLights, "switch", handler, [filterEvents: false])
	subscribe(themeLights, "level", handler, [filterEvents: false])
	subscribe(lights, "switch.on", handler, [filterEvents: false])
	subscribe(lights, "switch.off", handler, [filterEvents: false])
	subscribe(lights, "switch", handler, [filterEvents: false])
	subscribe(lights, "level", handler, [filterEvents: false])
    subscribe(switches, "switch", handler, [filterEvents: false])
    subscribe(dimmers, "level", handler, [filterEvents: false])
	subscribe(dimmers, "switch", handler, [filterEvents: false])
    subscribe(dimmerLights, "level", handler, [filterEvents: false])
	subscribe(dimmerLights, "switch", handler, [filterEvents: false])
	subscribe(momentaries, "switch.on", handler, [filterEvents: false])
    subscribe(locks, "lock", handler, [filterEvents: false])
    subscribe(contacts, "contact", handler, [filterEvents: false])
    subscribe(presence, "presence", handler, [filterEvents: false])
    subscribe(temperature, "temperature", handler, [filterEvents: false])
    subscribe(humidity, "humidity", handler, [filterEvents: false])
    subscribe(luminosity, "luminosity", handler, [filterEvents: false])
    subscribe(motion, "motion", handler, [filterEvents: false])
    subscribe(acceleration, "acceleration", handler, [filterEvents: false])
    subscribe(water, "water", handler, [filterEvents: false])
    subscribe(battery, "battery", handler, [filterEvents: false])
    subscribe(energy, "energy", handler, [filterEvents: false])
    subscribe(power, "power", handler, [filterEvents: false])

	subscribe(music, "status", handler, [filterEvents: false])
	subscribe(music, "level", handler, [filterEvents: false])
	subscribe(music, "trackDescription", handler, [filterEvents: false])
	subscribe(music, "mute", handler, [filterEvents: false])
	
	subscribe(thermostatsHeat, "temperature", handler, [filterEvents: false])
	subscribe(thermostatsHeat, "heatingSetpoint", handler, [filterEvents: false])
	subscribe(thermostatsHeat, "thermostatFanMode", handler, [filterEvents: false])
	subscribe(thermostatsHeat, "thermostatOperatingState", handler, [filterEvents: false])
	
	subscribe(thermostatsCool, "temperature", handler, [filterEvents: false])
	subscribe(thermostatsCool, "coolingSetpoint", handler, [filterEvents: false])
	subscribe(thermostatsCool, "thermostatFanMode", handler, [filterEvents: false])
	subscribe(thermostatsCool, "thermostatOperatingState", handler, [filterEvents: false])
	
	state.appVersion = appVersion()
}

def weatherRefresh() {
	log.debug "refreshing weather"
	weather?.refresh()
}

def sendURL_SMS(path) {
	generateURL(path)
	if (state.accessToken) {
		log.info "${title ?: location.name} SmartTiles URL: ${generateURL("ui").join()}"
		if (phone) {
			sendSmsMessage(phone, generateURL(path)[0])
			sendSmsMessage(phone, generateURL(path)[1])
		}
	}
}

def generateURL(path) {
	log.debug "resetOauth: $settings.resetOauth, $resetOauth, $settings.resetOauth"
	if (settings.resetOauth) {
		log.debug "Reseting Access Token"
		state.accessToken = null
	}
	
	if (settings.resetOauth || !state.accessToken) {
		try {
			createAccessToken()
			log.debug "Creating new Access Token: $state.accessToken"
		} catch (ex) {
			log.error "Did you forget to enable OAuth in SmartApp IDE settings for SmartTiles?"
			log.error ex
		}
	}
	
	["https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/$path", "?access_token=${state.accessToken}"]
}

def head() {
"""
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/>
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="apple-mobile-web-app-status-bar-style" content="black" />
<link rel="icon" sizes="192x192" href="https://625alex.github.io/SmartTiles/prod/icon.png">
<link rel="apple-touch-icon" href="https://625alex.github.io/SmartTiles/prod/icon.png">
<meta name="mobile-web-app-capable" content="yes">
<title>${app.label ?: location.name}</title>

<link rel="stylesheet" href="https://code.jquery.com/mobile/1.4.4/jquery.mobile-1.4.4.min.css" />
<link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/weather-icons/1.3.2/css/weather-icons.min.css" />
<link href="https://625alex.github.io/SmartTiles/prod/style.${appVersion()}.min.css?u=0" rel="stylesheet">
<link href='https://fonts.googleapis.com/css?family=Mallanna' rel='stylesheet' type='text/css'>

<script>
window.location.hash = "";
var stateTS = ${getStateTS()};
var tileSize = ${getTSize()};
var readOnlyMode = ${readOnlyMode ?: false};
var icons = ${getTileIcons().encodeAsJSON()};
var appVersion = "${appVersion()}";
var minTemp = ${getMinTemp()};
var maxTemp = ${getMaxTemp()};
var theme = "$theme";
</script>

<script src="https://code.jquery.com/jquery-2.1.1.min.js" type="text/javascript"></script>
<script src="https://code.jquery.com/mobile/1.4.4/jquery.mobile-1.4.4.min.js" type="text/javascript"></script>
<script src="https://625alex.github.io/SmartTiles/prod/script.${appVersion()}.min.js?u=0" type="text/javascript"></script>

<style>
.tile {width: ${getTSize()}px; height: ${getTSize()}px;}
.w2 {width: ${getTSize() * 2}px;}
.h2 {height: ${getTSize() * 2}px;}
${!dropShadow ? ".icon, .icon * {text-shadow: none;} .ui-slider-handle.ui-btn.ui-shadow {box-shadow: none; -webkit-box-shadow: none; -moz-box-shadow: none;}" : ""}
body {font-size: ${getFSize()}%;}
${readOnlyMode ? """.tile, .music i {cursor: default} .clock, .refresh{cursor: pointer}""" : ""}
${getThemeLightIcon().css}
</style>
"""
}                                                              

def footer() {
"""<script>
\$(function() {
  var wall = new freewall(".tiles");
  wall.fitWidth();
  
  wall.reset({
			draggable: false,
			selector: '.tile',
		animate: true,
		gutterX:cellGutter,
		gutterY:cellGutter,
		cellW:cellSize,
		cellH:cellSize,
		fixSize:null,
		onResize: function() {
			wall.fitWidth();
			wall.refresh();
		}
	});
	wall.fitWidth();
	// for scroll bar appear;
	\$(window).trigger("resize");
});
</script>"""
}

def headHistory() {
"""
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/>
<title>${app.label ?: location.name} Event History</title>
<link rel="stylesheet" href="https://code.jquery.com/mobile/1.4.4/jquery.mobile-1.4.4.min.css" />
<link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/weather-icons/1.3.2/css/weather-icons.min.css" />
<link href="https://625alex.github.io/SmartTiles/prod/style.${appVersion()}.min.css?u=0" rel="stylesheet">
<link href='https://fonts.googleapis.com/css?family=Mallanna' rel='stylesheet' type='text/css'>
<script src="https://code.jquery.com/jquery-2.1.1.min.js" type="text/javascript"></script>
<script src="https://code.jquery.com/mobile/1.4.4/jquery.mobile-1.4.4.min.js" type="text/javascript"></script>
<script src="https://625alex.github.io/SmartTiles/jquery.ui.touch-punch.min.js" type="text/javascript"></script>
<style>
.batt {background-size: 20px 20px;}
${getThemeLightIcon().css}
</style>
"""
}

def headList() {
"""
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/>
<title>${app.label ?: location.name} Device Order</title>

<link rel="stylesheet" href="https://code.jquery.com/mobile/1.4.4/jquery.mobile-1.4.4.min.css" />
<link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/weather-icons/1.3.2/css/weather-icons.min.css" />
<link href="https://625alex.github.io/SmartTiles/prod/style.${appVersion()}.min.css?u=0" rel="stylesheet">
<link href='https://fonts.googleapis.com/css?family=Mallanna' rel='stylesheet' type='text/css'>

<script src="https://code.jquery.com/jquery-2.1.1.min.js" type="text/javascript"></script>
<script src="https://code.jquery.com/ui/1.11.2/jquery-ui.min.js" type="text/javascript"></script>
<script src="https://625alex.github.io/SmartTiles/jquery.ui.touch-punch.min.js" type="text/javascript"></script>

<script>
	\$(function() {
		\$( ".list" ).sortable({
			stop: function( event, ui ) {changeOrder();}
		});
		\$( ".list" ).disableSelection();
	});

	function changeOrder() {
		var l = "";
		\$( ".list li" ).each(function(index) {
			l = l + \$(this).data("type") + "-" + \$(this).data("device") + "|~|";
		});
		var access_token = getUrlParameter("access_token");
		var request = {list: l};
		if (access_token) request["access_token"] = access_token;
		
		\$.get("position", request).done(function(data) {
			if (data.status == "ok") {}
		}).fail(function() {alert("error, please refresh")});
	}
	
	function getUrlParameter(sParam)
	{
		var sPageURL = window.location.search.substring(1);
		var sURLVariables = sPageURL.split('&');
		for (var i = 0; i < sURLVariables.length; i++) 
		{
			var sParameterName = sURLVariables[i].split('=');
			if (sParameterName[0] == sParam) 
			{
				return sParameterName[1];
			}
		}
	}
</script>
<style>
.batt {background-size: 20px 20px;}
${getThemeLightIcon().css}
</style>
"""
}  

def getTSize() {
	if (tileSize == "Medium") return 120
	else if (tileSize == "Large") return 150
	105
}

def getFSize() {
	if (fontSize == "Larger") return 120
	if (fontSize == "Largest") return 150
	100
}

def getTS() {
	def tf = new java.text.SimpleDateFormat("h:mm a")
    if (location?.timeZone) tf.setTimeZone(location.timeZone)
    "${tf.format(new Date())}"
}

def getDate() {
	def tf = new java.text.SimpleDateFormat("MMMMM d")
    if (location?.timeZone) tf.setTimeZone(location.timeZone)
    "${tf.format(new Date())}"
}

def formatDate(date) {
	def tf = new java.text.SimpleDateFormat("h:mm:ss a, dd MMMMM")
    if (location?.timeZone) tf.setTimeZone(location.timeZone)
    return tf.format(date)
}

def getDOW() {
	def tf = new java.text.SimpleDateFormat("EEEE")
    if (location?.timeZone) tf.setTimeZone(location.timeZone)
    "${tf.format(new Date())}"
}

def renderModeTile(data) {
"""<div class="mode tile w2 menu ${data.isStandardMode ? data.mode : ""}" data-mode="$data.mode" data-popup="mode-popup">
	<div class="title">Mode</div>
	<div data-role="popup" id="mode-popup" data-overlay-theme="b">
		<ul data-role="listview" data-inset="true" style="min-width:210px;">
			${data.modes.collect{"""<li data-icon="false">$it</li>"""}.join("\n")}
		</ul>
    </div>
	<div class="icon Home"><i class="fa fa-home"></i></div>
	<div class="icon Night"><i class="fa fa-moon-o"></i></div>
	<div class="icon Away"><i class="fa fa-sign-out"></i></div>
	<div class="icon small text mode-name" id="mode-name">$data.mode</div>
</div>"""
}

def renderHelloHomeTile(data) {
"""
<div class="hello-home tile menu" data-rel="popup" data-popup="hello-home-popup">
	<div class="title">Hello, Home!</div>
	<div data-role="popup" id="hello-home-popup" data-overlay-theme="b">
		<ul data-role="listview" data-inset="true" style="min-width:210px;">
			${data.phrases.collect{"""<li data-icon="false">$it</li>"""}.join("\n")}
		</ul>
	</div>
</div>
"""
}

def roundNumber(num) {
	if (!roundNumbers || !"$num".isNumber()) return num
	if (num == null || num == "") return "n/a"
	else {
    	try {
            return "$num".toDouble().round()
        } catch (e) {return num}
    }
}

def getWeatherData(device) {
	def data = [tile:"device", active:"inactive", type: "weather", device: device.id, name: device.displayName]
    ["city", "weather", "feelsLike", "temperature", "localSunrise", "localSunset", "percentPrecip", "humidity", "weatherIcon"].each{data["$it"] = device?.currentValue("$it")}
    data.icon = ["chanceflurries":"wi-snow","chancerain":"wi-rain","chancesleet":"wi-rain-mix","chancesnow":"wi-snow","chancetstorms":"wi-storm-showers","clear":"wi-day-sunny","cloudy":"wi-cloudy","flurries":"wi-snow","fog":"wi-fog","hazy":"wi-dust","mostlycloudy":"wi-cloudy","mostlysunny":"wi-day-sunny","partlycloudy":"wi-day-cloudy","partlysunny":"wi-day-cloudy","rain":"wi-rain","sleet":"wi-rain-mix","snow":"wi-snow","sunny":"wi-day-sunny","tstorms":"wi-storm-showers","nt_chanceflurries":"wi-snow","nt_chancerain":"wi-rain","nt_chancesleet":"wi-rain-mix","nt_chancesnow":"wi-snow","nt_chancetstorms":"wi-storm-showers","nt_clear":"wi-stars","nt_cloudy":"wi-cloudy","nt_flurries":"wi-snow","nt_fog":"wi-fog","nt_hazy":"wi-dust","nt_mostlycloudy":"wi-night-cloudy","nt_mostlysunny":"wi-night-cloudy","nt_partlycloudy":"wi-night-cloudy","nt_partlysunny":"wi-night-cloudy","nt_sleet":"wi-rain-mix","nt_rain":"wi-rain","nt_snow":"wi-snow","nt_sunny":"wi-night-clear","nt_tstorms":"wi-storm-showers","wi-horizon":"wi-horizon"][data.weatherIcon]
	data
}

def getThermostatData(device, type) {
	def deviceData = [:]
	device?.supportedAttributes?.each{
		try {
			deviceData << [("$it" as String): device.currentValue("$it")]
		} catch (e) {}
	}
	[tile: "device", type: type, device: device.id, name: device.displayName, humidity: deviceData.humidity, temperature: deviceData.temperature, thermostatFanMode: deviceData.thermostatFanMode, thermostatOperatingState: deviceData.thermostatOperatingState, setpoint: type == "thermostatHeat" ? deviceData.heatingSetpoint : deviceData.coolingSetpoint]
}

def renderTile(data) {
	if (data.type == "thermostatHeat" || data.type == "thermostatCool") {
		return  """<div class="$data.type tile h2" data-type="$data.type" data-device="$data.device" data-setpoint="$data.setpoint"><div class="title">$data.name ${getTileIcons()[data.type]}<br/><span class="title2">${data.temperature}&deg;, $data.thermostatOperatingState</span></div><div class="icon setpoint">$data.setpoint&deg;</div><div class="icon up"><i class="fa fa-fw fa-chevron-up"></i></div><div class="icon down"><i class="fa fa-fw fa-chevron-down"></i></div><div class="footer">&#10044; $data.thermostatFanMode ${data.humidity ? ",<i class='fa fa-fw wi wi-sprinkles'></i>" + data.humidity  + "%" : ""}</div></div>"""
	} else if (data.type == "weather"){
		return """<div class="weather tile w2" data-type="weather" data-device="$data.device" data-weather="$data.weatherIcon"><div class="title">$data.city<br/><span class="title2">$data.weather, feels like $data.feelsLike&deg;</span></div><div class="icon"><span class="text">$data.temperature&deg;</span><i class="wi $data.icon"></i></span></div><div class="footer">$data.localSunrise <i class="fa fa-fw wi wi-horizon-alt"></i> $data.localSunset</div><div class="footer right">$data.percentPrecip%<i class="fa fa-fw fa-umbrella"></i><br>$data.humidity%<i class="fa fa-fw wi wi-sprinkles"></i></div></div>"""
	} else if (data.type == "music") {
		return """
		<div class="music tile w2 $data.active ${data.mute ? "muted" : ""}" data-type="music" data-device="$data.device" data-level="$data.level" data-track-description="$data.trackDescription" data-mute="$data.mute">
			<div class="title"><span class="name">$data.name</span><br/><span class='title2 track'>$data.trackDescription</span></div>
			<div class="icon text"><i class="fa fa-fw fa-backward back"></i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class="fa fa-fw fa-pause pause"></i><i class="fa fa-fw fa-play play"></i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class="fa fa-fw fa-forward forward"></i></div>
			<div class="footer"><i class='fa fa-fw fa-volume-down unmuted'></i><i class='fa fa-fw fa-volume-off muted'></i></div>
		</div>
		"""
	} else if (data.tile == "device") {
		return """<div class="$data.type tile $data.active" data-active="$data.active" data-type="$data.type" data-device="$data.device" data-value="$data.value" data-level="$data.level" data-is-value="$data.isValue"><div class="title">$data.name</div></div>"""
	} else if (data.tile == "link") {
		return """<div class="link tile" data-link-i="$data.i"><div class="title">$data.name</div><div class="icon"><a href="$data.link" data-ajax="false" style="color:white"><i class="fa fa-th"></i></a></div></div>"""
	} else if (data.tile == "dashboard") {
		return """<div class="dashboard tile" data-link-i="$data.i"><div class="title">$data.name</div><div class="icon"><a href="$data.link" data-ajax="false" style="color:white"><i class="fa fa-link"></i></a></div></div>"""
	} else if (data.tile == "video") {
		return """<div class="video tile h2 w2" data-link-i="$data.i"><div class="title">$data.name</div><div class="icon" style="margin-top:-82px;"><object width="240" height="164"><param name="movie" value="$data.link"></param><param name="allowFullScreen" value="true"></param><param name="allowscriptaccess" value="always"></param><param name="wmode" value="opaque"></param><embed src="$data.link" type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="true" width="240" height="164" wmode="opaque"></embed></object></div></div>"""
	} else if (data.tile == "genericMJPEGvideo") {
		return """<div class="video tile h2 w2" data-link-i="$data.i"><div class="title">$data.name</div><div class="icon" style="margin-top:-82px;"><object width="240" height="164"><img src="$data.link" width="240" height="164"></object></div></div>"""
	} else if (data.tile == "refresh") {
		return """<div class="refresh tile clickable"><div class="title">Refresh</div><div class="footer">Updated $data.ts</div></div>"""
	} else if (data.tile == "history") {
		return """<div class="history tile"><div class="title">Event History</div></div>"""
	} else if (data.tile == "mode") {
		return renderModeTile(data)
	} else if (data.tile == "clock") {
		if (data.style == "a") {
			return """<div id="analog-clock" class="clock tile clickable h$data.size w$data.size"><div class="title">$data.date</div><div class="icon" style="margin-top:-${data.size * 45}px;"><canvas id="clockid" class="CoolClock:st:${45 * data.size}"></canvas></div><div class="footer">$data.dow</div></div>"""
		} else {
			return """<div id="digital-clock" class="clock tile clickable w$data.size"><div class="title">$data.date</div><div class="icon ${data.size == 2 ? "" : "text"}" id="clock">*</div><div class="footer">$data.dow</div></div>"""
		}
	} else if (data.tile == "helloHome") {
		return renderHelloHomeTile(data)
	}
	
	return ""
}

def getTileIcons() {
	[
		dimmer : [off : "<i class='inactive fa fa-fw fa-toggle-off st-switch-off'></i>", on : "<i class='active fa fa-fw fa-toggle-on st-switch-on'></i>"],
		dimmerLight : [off : "<i class='inactive fa fa-fw fa-lightbulb-o st-light-off'></i>", on : "<i class='active fa fa-fw fa-lightbulb-o st-light-on'></i>"],
		switch : [off : "<i class='inactive fa fa-fw fa-toggle-off st-switch-off'></i>", on : "<i class='active fa fa-fw fa-fw fa-toggle-on st-switch-on'></i>"],
		light : [off : "<i class='inactive fa fa-fw fa-lightbulb-o st-light'></i>", on : "<i class='active fa fa-fw fa-lightbulb-o st-light-on'></i>"],
		lock : [locked : "<i class='inactive fa fa-fw fa-lock st-lock'></i>", unlocked : "<i class='active fa fa-fw fa-unlock-alt st-unlock'></i>"],
		motion : [active : "<i class='active fa fa-fw fa-exchange st-motion-active'></i>", inactive: "<i class='inactive fa fa-fw fa-exchange st-motion-inactive'></i>"],
		acceleration : [active : "<i class='active fa fa-fw st-acceleration-active'>&#8779</i>", inactive: "<i class='inactive fa fa-fw st-acceleration-inactive'>&#8779</i>"],
		presence : [present : "<i class='active fa fa-fw fa-map-marker st-present'></i>", notPresent: "<i class='inactive fa fa-fw fa-map-marker st-not-present'></i>", "not present": "<i class='inactive fa fa-fw fa-map-marker st-not-present'></i>"],
		contact : [open : "<i class='active r45 fa fa-fw fa-expand st-opened'></i>", closed: "<i class='inactive r45 fa fa-fw fa-compress st-closed'></i>"],
		water : [dry : "<i class='inactive fa fa-fw fa-tint st-dry'></i>", wet: "<i class='active fa fa-fw fa-tint st-wet'></i>"],
		momentary : "<i class='fa fa-fw fa-circle-o st-momentary'></i>",
		camera : "<i class='fa fa-fw fa-camera st-camera'></i>",
		refresh : "<i class='fa fa-fw fa-refresh st-refresh'></i>",
        history : "<i class='fa fa-fw fa-history st-history'></i>",		
		humidity : "<i class='fa fa-fw wi wi-sprinkles st-humidity'></i>",
		luminosity : "<i class='fa fa-fw st-luminosity'>&#9728;</i>",
		temperature : "<i class='fa fa-fw wi wi-thermometer st-temperature'></i>",
		energy : "<i class='fa fa-fw wi wi-lightning st-energy'></i>",
		power : "<i class='fa fa-fw fa-bolt st-power'></i>",
		battery : "<i class='fa fa-fw fa-fw batt st-battery'></i>",
        "hello-home" : "<i class='fa fa-fw fa-comment-o st-hello-home'></i>",
        link : "<i class='fa fa-fw fa-link st-link'></i>",
        dashboard : "<i class='fa fa-fw fa-th st-dashboard'></i>",
        thermostatHeat : "<i class='fa fa-fw fa-fire st-heat'></i>",
        thermostatCool : "<i class='fa fa-fw wi wi-snowflake-cold st-cool'></i>",
		themeLight: getThemeLightIcon(),
		clock: """<i class="fa fa-fw fa-clock-o st-clock"></i>""",
		mode: """<i class="fa fa-fw fa-gear st-mode"></i>""",
		weather: """<i class="fa fa-fw wi wi-day-rain-mix st-weather"></i>""",
		music: """<i class="fa fa-fw fa-music st-music"></i>""",
		video: """<i class="fa fa-fw fa-video-camera st-video"></i>""",
		"?": """<i class="fa fa-fw fa-question st-unknown"></i>""",
	]
}

def getListIcon(type) {
	def icons = [
		lock: getTileIcons().lock.locked,
		switch: getTileIcons().switch.on,
		light: getTileIcons().light.on,
		themeLight: getTileIcons().themeLight.on,
		dimmer: getTileIcons().dimmer.on,
		dimmerLight: getTileIcons().dimmerLight.on,
		momentary: getTileIcons().momentary,
		contact: getTileIcons().contact.open,
		presence: getTileIcons().presence.present,
		motion: getTileIcons().motion.active,
		acceleration: getTileIcons().acceleration.active,
		water: getTileIcons().water.wet,
	]
	
	icons[type] ?: getTileIcons()[type]
}

def getEventIcon(event) {
	if (event.name == "level" && (event.deviceType == "dimmerLight" || event.deviceType == "dimmer")) return (getTileIcons()["light"]).on
	def eventValues = getTileIcons()[event.deviceType]

	if (!eventValues) return getTileIcons()["?"]
	
	if (eventValues instanceof String) return eventValues
	
	eventValues[event.value] ?: getTileIcons()["?"]
}

def getThemeLightIcon() {
	def icons = [
	"Valentine's Day" : [on : "<i class='active fa fa-fw fa-heart st-valentines-on'></i>", off : "<i class='inactive fa fa-fw fa-heart-o st-valentines-off'></i>", css: ".themeLight {background-color: #FF82B2;} /*pink*/ .themeLight.active {background-color: #A90000} .themeLight.active .icon i {color:#EA001F}"],
	"Christmas" : [on: "<i class='active fa fa-fw fa-tree st-christmas-on'></i>", off: "<i class='inactive fa fa-fw fa-tree st-christmas-off'></i>", css: ".themeLight {background-color: #11772D;} /*green*/ .themeLight.active {background-color: #AB0F0B} .themeLight.active .icon i {color:#11772D}"],
    ]
	icons[themeLightType] ?: [off : "<i class='inactive fa fa-fw fa-lightbulb-o st-light-off'></i>", on : "<i class='active fa fa-fw fa-lightbulb-o st-light-on'></i>", css : ""]
}

def renderListItem(data) {return """<li class="item tile $data.type" data-type="$data.type" data-device="$data.device" id="$data.type|$data.device">${getListIcon(data.type)}$data.name</li>"""}

def renderEvent(data) {return """<li class="item tile $data.deviceType" data-name="$data.name" data-value="$data.value"><div class="event-icon">${getEventIcon(data)}</div><div class="event">$data.displayName &nbsp;<i class="fa fa-long-arrow-right"></i> $data.value${data.unit ?: ""}</div><div class="date">${formatDate(data.date)}</div></li>"""}

def getMusicPlayerData(device) {[tile: "device", type: "music", device: device.id, name: device.displayName, status: device.currentValue("status"), level: getDeviceLevel(device, "music"), trackDescription: device.currentValue("trackDescription"), mute: device.currentValue("mute") == "muted", active: device.currentValue("status") == "playing" ? "active" : ""]}

def getDeviceData(device, type) {[tile: "device",  active: isActive(device, type), type: type, device: device.id, name: device.displayName, value: getDeviceValue(device, type), level: getDeviceLevel(device, type), isValue: isValue(device, type)]}

def getDeviceFieldMap() {[lock: "lock", themeLight: "switch", light: "switch", "switch": "switch", dimmer: "switch", dimmerLight: "switch", contact: "contact", presence: "presence", temperature: "temperature", humidity: "humidity", luminosity: "illuminance", motion: "motion", acceleration: "acceleration", water: "water", power: "power", energy: "energy", battery: "battery"]}

def getActiveDeviceMap() {[lock: "unlocked", themeLight: "on", light: "on", "switch": "on", dimmer: "on", dimmerLight: "on", contact: "open", presence: "present", motion: "active", acceleration: "active", water: "wet"]}

def isValue(device, type) {!(["momentary", "camera"] << getActiveDeviceMap().keySet()).flatten().contains(type)}

def isActive(device, type) {
	def field = getDeviceFieldMap()[type]
	def value = "n/a"
	try {
		value = device.respondsTo("currentValue") ? device.currentValue(field) : device.value
	} catch (e) {
		log.error "Device $device ($type) does not report $field properly. This is probably due to numerical value returned as text"
	}
	value == getActiveDeviceMap()[type] ? "active" : "inactive"
}

def getDeviceValue(device, type) {
	def unitMap = [temperature: "°", humidity: "%", luminosity: "lx", battery: "%", power: "W", energy: "kWh"]
	def field = getDeviceFieldMap()[type]
	def value = "n/a"
	try {
		value = device.respondsTo("currentValue") ? device.currentValue(field) : device.value
	} catch (e) {
		log.error "Device $device ($type) does not report $field properly. This is probably due to numerical value returned as text"
	}
	if (!isValue(device, type)) return value
	else return "${roundNumber(value)}${unitMap[type] ?: ""}"
}

def getDeviceLevel(device, type) {if (type == "dimmer" || type == "dimmerLight" || type == "music") return "${(device.currentValue("level") ?: 0) / 10.0}".toDouble().round() ?: 1}

def handler(e) {
	log.debug "event from: $e.displayName, value: $e.value, source: $e.source, description: $e.description"
	updateStateTS()
}

def updateStateTS() {
	log.debug "updating TS"
	state.ts = now()
}

def getStateTS() {state.ts}

def ping() {
	if ("$params.ts" == "${getStateTS()}") [status: "noop", updated: getTS(), ts: getStateTS()]
	else [status: "update", updated: getTS(), ts: getStateTS(), data: allDeviceData()]
}

def saveCSS() {
	state.customCSS = params.css
	css()
}

def allDeviceData() {
	def refresh = [tile: "refresh", ts: getTS(), name: "Refresh", type: "refresh"]
	if (disableDashboard) return [refresh]
	
	def data = []
	
	if (showClock == "Small Analog") data << [tile: "clock", size: 1, style: "a", date: getDate(), dow: getDOW(), name: "Clock", type: "clock"]
	else if (showClock == "Large Analog") data << [tile: "clock", size: 2, style: "a", date: getDate(), dow: getDOW(), name: "Clock", type: "clock"]
    else if (showClock == "Small Digital") data << [tile: "clock", size: 1, style: "d", date: getDate(), dow: getDOW(), name: "Clock", type: "clock"]
	else if (showClock == "Large Digital") data << [tile: "clock", size: 2, style: "d", date: getDate(), dow: getDOW(), name: "Clock", type: "clock"]
	
	if (showMode && location.modes) data << [tile: "mode", mode: "$location.mode", isStandardMode: ("$location.mode" == "Home" || "$location.mode" == "Away" || "$location.mode" == "Night"), modes: location?.modes?.name?.sort(), name: "Mode", type: "mode"]
	
	def phrases = location?.helloHome?.getPhrases() ? location?.helloHome?.getPhrases()*.label?.sort() : []
	if (showHelloHome && phrases) data << [tile: "helloHome", phrases: phrases, name: "Hello, Home!", type: "hello-home"]
	
	weather?.each{data << getWeatherData(it)}
	
	locks?.each{data << getDeviceData(it, "lock")}
	thermostatsHeat?.each{data << getThermostatData(it, "thermostatHeat")}
	thermostatsCool?.each{data << getThermostatData(it, "thermostatCool")}
	music?.each{data << getMusicPlayerData(it)}
	switches?.each{data << getDeviceData(it, "switch")}
	lights?.each{data << getDeviceData(it, "light")}
	themeLights?.each{data << getDeviceData(it, "themeLight")}
	dimmers?.each{data << getDeviceData(it, "dimmer")}
	dimmerLights?.each{data << getDeviceData(it, "dimmerLight")}
	momentaries?.each{data << getDeviceData(it, "momentary")}
	contacts?.each{data << getDeviceData(it, "contact")}
	presence?.each{data << getDeviceData(it, "presence")}
	motion?.each{data << getDeviceData(it, "motion")}
	acceleration?.each{data << getDeviceData(it, "acceleration")}
	camera?.each{data << getDeviceData(it, "camera")}
	(1..10).each{if (settings["dropcamStreamUrl$it"]) {data << [tile: "video", device: "$it", link: settings["dropcamStreamUrl$it"], name: settings["dropcamStreamT$it"] ?: "Stream $it", i: it, type: "video"]}}
	(1..10).each{if (settings["mjpegStreamUrl$it"]) {data << [tile: "genericMJPEGvideo", device: "$it", link: settings["mjpegStreamUrl$it"], name: settings["mjpegStreamTitile$it"] ?: "Stream $it", i: it, type: "video"]}}
	temperature?.each{data << getDeviceData(it, "temperature")}
	humidity?.each{data << getDeviceData(it, "humidity")}
	luminosity?.each{data << getDeviceData(it, "luminosity")}
	water?.each{data << getDeviceData(it, "water")}
	energy?.each{data << getDeviceData(it, "energy")}
	power?.each{data << getDeviceData(it, "power")}
	battery?.each{data << getDeviceData(it, "battery")}
	
	(1..10).each{if (settings["linkUrl$it"]) {data << [tile: "link", device: "$it", link: settings["linkUrl$it"], name: settings["linkTitle$it"] ?: "Link $it", i: it, type: "link"]}}
	(1..10).each{if (settings["dashboardUrl$it"]) {data << [tile: "dashboard", device: "$it", link: settings["dashboardUrl$it"], name: settings["dashboardTitle$it"] ?: "Dashboard $it", i: it, type: "dashboard"]}}
	
	if (showRefresh) data << refresh
	if (showHistory) data << [tile: "history", name: "Event History", type: "history"]
	
	data.sort{state?.sortOrder?."$it.type-$it.device"}
}

def getEventsOfDevice(device) {
	def today = new Date()
	def then = timeToday(today.format("HH:mm"), TimeZone.getTimeZone('UTC')) - 1
	device.eventsBetween(then, today, [max: 200])?.findAll{"$it.source" == "DEVICE"}?.collect{[description: it.description, descriptionText: it.descriptionText, displayName: it.displayName, date: it.date, name: it.name, unit: it.unit, source: it.source, value: it.value]}
}

def filterEventsPerCapability(events, deviceType) {
	def acceptableEventsPerCapability = [
		light           : ["switch"],
		dimmerLight     : ["switch", "level"],
		switch          : ["switch"],
		dimmer          : ["switch", "level"],
		momentary       : ["switch"],
		themeLight      : ["switch"],
		thermostatHeat  : ["temperature", "heatingSetpoint", "thermostatFanMode", "thermostatOperatingState",],
		thermostatCool  : ["temperature", "coolingSetpoint", "thermostatFanMode", "thermostatOperatingState",],
		lock            : ["lock"],
		music           : ["status", "level", "trackDescription", "mute"],
		camera          : [],
		presence        : ["presence"],
		contact         : ["contact"],
		motion          : ["motion"],
		temperature     : ["temperature"],
		humidity        : ["humidity"],
		water           : ["water"],
		battery         : ["battery"],
		energy          : ["energy"],
		power           : ["power"],
		acceleration    : ["acceleration"],
		luminosity      : ["illuminance"],
		weather         : ["temperature", "weather"],
	]
	
	if (events) events*.deviceType = deviceType
	events?.findAll{it.name in acceptableEventsPerCapability[deviceType]}
}

def getAllDeviceEvents() {
	def eventsPerCapability = [
		light           : lights                ?.collect{getEventsOfDevice(it)},
		dimmerLight     : dimmerLights          ?.collect{getEventsOfDevice(it)},
		switch          : switches              ?.collect{getEventsOfDevice(it)},
		dimmer          : dimmers               ?.collect{getEventsOfDevice(it)},
		momentary       : momentaries           ?.collect{getEventsOfDevice(it)},
		themeLight      : themeLights           ?.collect{getEventsOfDevice(it)},
		thermostatHeat  : thermostatsHeat       ?.collect{getEventsOfDevice(it)},
		thermostatCool  : thermostatsCool       ?.collect{getEventsOfDevice(it)},
		lock            : locks                 ?.collect{getEventsOfDevice(it)},
		music           : music                 ?.collect{getEventsOfDevice(it)},
		camera          : camera                ?.collect{getEventsOfDevice(it)},
		presence        : presence              ?.collect{getEventsOfDevice(it)},
		contact         : contacts              ?.collect{getEventsOfDevice(it)},
		motion          : motion                ?.collect{getEventsOfDevice(it)},
		temperature     : temperature           ?.collect{getEventsOfDevice(it)},
		humidity        : humidity              ?.collect{getEventsOfDevice(it)},
		water           : water                 ?.collect{getEventsOfDevice(it)},
		battery         : battery               ?.collect{getEventsOfDevice(it)},
		energy          : energy                ?.collect{getEventsOfDevice(it)},
		power           : power                 ?.collect{getEventsOfDevice(it)},
		acceleration    : acceleration          ?.collect{getEventsOfDevice(it)},
		luminosity      : luminosity            ?.collect{getEventsOfDevice(it)},
		weather         : weather               ?.collect{getEventsOfDevice(it)},
	]
	
	def filteredEvents = [:]
	
	eventsPerCapability.each {deviceType, events ->
		filteredEvents[deviceType] = filterEventsPerCapability(events?.flatten(), deviceType)
	}
	filteredEvents.values()?.flatten()?.findAll{it}?.sort{"$it.date.time" + "$it.deviceType"}.reverse()
}

def html() {render contentType: "text/html", data: "<!DOCTYPE html><html><head>${head()}${customCSS()} \n<style>${state.customCSS ?: ""}</style></head><body class='theme-$theme'>\n${renderTiles()}\n${renderWTFCloud()}${footer()}</body></html>"}
def renderTiles() {"""<div class="tiles">\n${allDeviceData()?.collect{renderTile(it)}.join("\n")}<div class="blank tile"></div></div>"""}

def renderWTFCloud() {"""<div data-role="popup" id="wtfcloud-popup" data-overlay-theme="b" class="wtfcloud"><div class="icon cloud" onclick="clearWTFCloud()"><i class="fa fa-cloud"></i></div><div class="icon message" onclick="clearWTFCloud()"><i class="fa fa-question"></i><i class="fa fa-exclamation"></i><i class='fa fa-refresh'></i></div></div>"""}

def link() {
	if (!params.access_token) return ["You are not authorized to view OAuth access token"]
	render contentType: "text/html", data: """<!DOCTYPE html><html><head><meta charset="UTF-8"/><meta name="viewport" content="user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width, height=device-height, target-densitydpi=device-dpi" /></head><body style="margin: 0;"><div style="padding:10px">${title ?: location.name} SmartTiles URL:</div><textarea rows="9" cols="30" style="font-size:10px; width: 100%">${generateURL("ui").join()}</textarea><div style="padding:10px">Copy the URL above and tap Done.</div></body></html>"""
}

def css() {render contentType: "text/html", data: """<!DOCTYPE html><html><head><meta charset="UTF-8"/><meta name="viewport" content="user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width, height=device-height, target-densitydpi=device-dpi" /></head><body style="margin: 0;"><form action="css?access_token=$state.accessToken" method="post"><textarea rows="10" cols="30" style="font-size:12pt; width: 100%;" name="css">${state.customCSS ?: "/*enter custom css here*/"}</textarea><br/><input type="submit" value="Save" style="margin-left:10px"></form><br/><div style="padding:10px">Enter custom CSS and tap "Save", then tap "Done".<br/><br/>Please note that invalid CSS may break the dashboard. Use at your discretion.</div></body></html>"""}

def list() {render contentType: "text/html", data: """<!DOCTYPE html><html><head>${headList()}</head><body class='theme-$theme'><ul class="list">\n${allDeviceData()?.collect{renderListItem(it)}.join("\n")}</ul></body></html>"""}

def historyNav() {
"""
<div style="" class="historyNav">
<i class="fa fa-fw fa-arrow-left" onclick="window.history.back();"></i>
<i class="fa fa-fw fa-refresh" onclick="this.className = this.className + ' fa-spin'; location.reload();"></i>
<i class="fa fa-fw fa-chevron-up" onclick="window.scrollTo(0, 0);"></i>
</div>
"""
}

def history() {
	if (!showHistory || disableDashboard) return ["history disabled"]
	render contentType: "text/html", data: """<!DOCTYPE html><html><head>${headHistory()}</head><body class='theme-$theme'>${historyNav()}<ul class="history-list list">\n${getAllDeviceEvents()?.collect{renderEvent(it)}.join("\n")}</ul></body></html>"""
}

def customCSS() {
"""
<style>
/*** Custonm CSS Start ***/

/*** Custonm CSS End *****/
</style>
"""
}