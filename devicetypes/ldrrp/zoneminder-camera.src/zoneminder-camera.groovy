/**
 *
 *  Zoneminder Camera
 *
*/

metadata {
	definition (name: "Zoneminder Camera", namespace: "ldrrp", author: "Luis Rodriguez", category: "Cameras") {
		capability "Image Capture"
		capability "Sensor"
		capability "Actuator"
		capability "Configuration"
		capability "Video Camera"
		capability "Video Capture"
		capability "Refresh"
		capability "Switch"

    command "start"

		attribute "hubactionMode", "string"
	}

    preferences {
	    input("ZMIP", "string", title:"Zoneminder internal IP Address", description: "Please enter your systems IP Address", required: true, displayDuringSetup: true)
			input("ZMPort", "string", title:"Zoneminder internal Port", defaultValue: '80', description: "Please enter your systems Port", required: true, displayDuringSetup: true)
    	input("ZMPATH", "string", title:"Path to Zoneminder CGI-BIN", defaultValue: '/', description: "Please enter the path to the CGI-BIN", required: true, displayDuringSetup: true)
			input("ZMID", "string", title:"Zoneminder Monitor ID", defaultValue: '19', description:"", required: true, displayDuringSetup: true)
			input("ZMVSCALE", "string", title:"Zoneminder Video Scale", defaultValue: '75', description:"What Scale to capture video at in percentage", required: true, displayDuringSetup: true)
			input("ZMISCALE", "string", title:"Zoneminder Image Scale", defaultValue: '100', description:"What Scale to capture images at in percentage", required: true, displayDuringSetup: true)
    	input("ZMAuth", "bool", title:"Does Zoneminder require User Auth?", defaultValue: true, description: "Please choose if the system requires authentication", required: false, displayDuringSetup: true)
    	input("ZMUser", "string", title:"Zoneminder User", defaultValue: '', description: "Please enter your systems username", required: false, displayDuringSetup: true)
    	input("ZMPass", "string", title:"Zoneminder Password", defaultValue: '', description: "Please enter your systems password", required: false, displayDuringSetup: true)
	}

	simulator {

	}

	tiles {
	multiAttributeTile(name: "videoPlayer", type: "videoPlayer", width: 6, height: 4) {
		tileAttribute("device.switch", key: "CAMERA_STATUS") {
			attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", action: "switch.off", backgroundColor: "#79b821", defaultState: true)
			attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", action: "switch.on", backgroundColor: "#ffffff")
			attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
			attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", action: "refresh.refresh", backgroundColor: "#F22000")
		}

		tileAttribute("device.errorMessage", key: "CAMERA_ERROR_MESSAGE") {
			attributeState("errorMessage", label: "", value: "", defaultState: true)
		}

		tileAttribute("device.camera", key: "PRIMARY_CONTROL") {
			attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", backgroundColor: "#79b821", defaultState: true)
			attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", backgroundColor: "#ffffff")
			attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
			attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", backgroundColor: "#F22000")
		}

		tileAttribute("device.startLive", key: "START_LIVE") {
			attributeState("live", action: "start", defaultState: true)
		}

		tileAttribute("device.stream", key: "STREAM_URL") {
			attributeState("activeURL", defaultState: true)
		}
	}


			standardTile("take", "device.image", width: 2, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
					state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
					state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
					state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
			}

			standardTile("refresh", "device.alarmStatus", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
					state "refresh", action:"polling.poll", icon:"st.secondary.refresh"
			}
			standardTile("blank", "device.image", width: 2, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
					state "blank", label: "", action: "", icon: "", backgroundColor: "#FFFFFF"
			}
			carouselTile("cameraDetails", "device.image", width: 6, height: 4) { }

			main "take"
			details(["videoPlayer", "take", "blank", "refresh", "cameraDetails"])
	}
}

//for photo
def parse(String description) {
    log.debug "Parsing '${description}'"
    def map = [:]
	def retResult = []
	def descMap = parseDescriptionAsMap(description)
	//Image
	def imageKey = descMap["tempImageKey"] ? descMap["tempImageKey"] : descMap["key"]
	if (imageKey) {
		storeTemporaryImage(imageKey, getPictureName())
	}
}

// handle commands
def take() {
    def hosthex = convertIPtoHex(ZMIP).toUpperCase()
    def porthex = convertPortToHex(ZMPort).toUpperCase()
    device.deviceNetworkId = "$hosthex:$porthex"

    def path = getPictureURL()

    def headers = [:]
    headers.put("HOST", "$ZMIP:$ZMPort")
    //headers.put("HOST", "$hosthex:$porthex")

    try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: path,
    	headers: headers
  	)

    hubAction.options = [outputMsgToS3:true]
    log.debug hubAction
    hubAction
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }

}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private getPictureName() {
	def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
    log.debug pictureUuid
    def picName = device.deviceNetworkId.replaceAll(':', '') + "_$pictureUuid" + ".jpg"
	return picName
}

private String convertIPtoHex(ipAddress) {
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug hexport
    return hexport
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}


private String convertHexToIP(hex) {
	log.debug("Convert hex to ip: $hex")
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
    log.debug device.deviceNetworkId
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

//for video
private getVideoURL() {
		def videoURL = ""
		if (ZMAuth) {
			videoURL = "http://${ZMIP}:${ZMPort}${ZMPATH}cgi-bin/nph-zms?mode=jpeg&monitor=${ZMID}&scale=${ZMVSCALE}&user=${ZMUser}&pass=${ZMPass}"
		} else {
			videoURL = "http://${ZMIP}:${ZMPort}${ZMPATH}cgi-bin/nph-zms?mode=jpeg&monitor=${ZMID}&scale=${ZMVSCALE}"
		}
		return videoURL
}

//for video
private getPictureURL() {
		def videoURL = ""
		if (ZMAuth) {
			videoURL = "http://${ZMIP}:${ZMPort}${ZMPATH}/cgi-bin/nph-zms?mode=single&monitor=${ZMID}&scale=${ZMISCALE}&user=${ZMUser}&pass=${ZMPass}"
		} else {
			videoURL = "http://${ZMIP}:${ZMPort}${ZMPATH}/cgi-bin/nph-zms?mode=single&monitor=${ZMID}&scale=${ZMISCALE}"
		}
		return videoURL
}

mappings {
   path("/getInHomeURL") {
       action:
       [GET: "getInHomeURL"]
   }
}

def installed() {
	configure()
}

def updated() {
	configure()
}

// handle commands
def configure() {
	log.debug "Executing 'configure'"
    sendEvent(name:"switch", value: "on")
}

def start() {
	log.trace "start()"
    def videoURL = getVideoURL()
    log.debug videoURL
	def dataLiveVideo = [
		OutHomeURL  : videoURL,
		InHomeURL   : videoURL,
		ThumbnailURL: "http://cdn.device-icons.smartthings.com/camera/dlink-indoor@2x.png",
		cookie      : [key: "key", value: "value"]
	]

	def event = [
		name           : "stream",
		value          : groovy.json.JsonOutput.toJson(dataLiveVideo).toString(),
		data		   : groovy.json.JsonOutput.toJson(dataLiveVideo),
		descriptionText: "Starting the livestream",
		eventType      : "VIDEO",
		displayed      : false,
		isStateChange  : true
	]
	sendEvent(event)
}

def getInHomeURL() {
	 [InHomeURL: getVideoURL()]
}
