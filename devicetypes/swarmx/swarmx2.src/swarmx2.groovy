/**
 *  swarmx2
 *
 *  Copyright 2016 Badrinarayanan Rangarajan
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
metadata {
	definition (name: "swarmx2", namespace: "swarmx", author: "Badrinarayanan Rangarajan") {
    capability "Actuator"
		capability "Sensor"
		capability "Image Capture"
        
        command "setAuthToken"
        command "removeAuthToken"
	}

        preferences {
            input("CameraIP", "string", title:"Camera IP Address", description: "Please enter your camera's IP Address", required: true, displayDuringSetup: true)
            input("CameraPort", "string", title:"Camera Port", description: "Please enter your camera's Port", defaultValue: 80 , required: true, displayDuringSetup: true)
            input("CameraPath", "string", title:"Camera Path to Image", description: "Please enter the path to the image (default: /cgi-bin/video.cgi?msubmenu=jpg&resolution=2)", defaultValue: "/cgi-bin/video.cgi?msubmenu=jpg&resolution=2", required: true, displayDuringSetup: true)
            input("CameraPostGet", "string", title:"Does Camera use a Post or Get, normally Get?", description: "Please choose if the camera uses a POST or a GET command to retreive the image", defaultValue: "GET", displayDuringSetup: true)
            input("CameraUser", "string", title:"Camera User", description: "Please enter your camera's username (default: admin)", defaultValue: "admin", required: false, displayDuringSetup: true)
            input("CameraPassword", "string", title:"Camera Password", description: "Please enter your camera's password", required: false, displayDuringSetup: true)
	}
    
	simulator {
	}

	tiles {
		standardTile("camera", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: true) {
			state "default", label: "", action: "", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
		}

		carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

		standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
			state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
			state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
		}
        
        standardTile("authenticate", "device.button", width: 1, height: 1, canChangeIcon: true) {
            state "DeAuth", label: '${name}', action: "removeAuthToken", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "Auth", label: '${name}', action: "setAuthToken", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        
        main "camera"
		details(["cameraDetails", "take", "error", "authenticate"])
	}
}

// method to set digest token
def setAuthToken() {
	trace("setAuth")
	state.auth = "empty"
	take()
}

// method to set remove token (a.k.a. logout)
def removeAuthToken() {
	trace("removeAuth")
	sendEvent(name: "authenticate", value: "Auth")
	state.auth = "empty"
}

// method called after touching the take button
def take() {
	def porthex = convertPortToHex(CameraPort)
	def hosthex = convertIPtoHex(CameraIP)
	def path = CameraPath.trim()
	def request = ""

	// set a proper network Id of the device
	device.deviceNetworkId = "$hosthex:$porthex"

	trace("The device id configured is: $device.deviceNetworkId")
	trace("state: " + state)

	if (!state.auth || state.auth == "empty") {
		// empty request to get nonce token
		request = """GET ${path} HTTP/1.1\r\nAccept: */*\r\nHost: ${getHostAddress()}\r\n\r\n"""
	} else {
		// got nonce token, parsing headers and calculating digest header
		def auth_headers = calcDigestAuth(state.auth)
		request = """GET ${path} HTTP/1.1\r\nAccept: */*\r\nHost: ${getHostAddress()}\r\nAuthorization: ${auth_headers}\r\n\r\n"""
	}

	try {
		def hubAction = new physicalgraph.device.HubAction(request, physicalgraph.device.Protocol.LAN, "${device.deviceNetworkId}")
		if (state.auth && state.auth != "empty") {
			// upload image/jpg output to S3 
			hubAction.options = [outputMsgToS3: true]
		}
		return hubAction
	} catch (Exception e) {
		trace("Hit Exception $e on $hubAction")
	}
}

// method to parse output from the camera
def parse(String output) {
	trace("Parsing output: '${output}'")
	def headers = ""
	def parsedHeaders = ""
	def map = stringToMap(output)

	if (map.headers) {
		headers = new String(map.headers.decodeBase64())
		parsedHeaders = parseHttpHeaders(headers)

		if (parsedHeaders.auth) {
			// set required tokens in the special state variable (see description above)
			state.auth = parsedHeaders.auth
			trace("Got 401, send request again (click on 'take' one more time): " + state.auth)
			sendEvent(name: "authenticate", value: "DeAuth")
			return result
		}
	}

	if (map.body != null) {
		def bodyString = new String(map.body.decodeBase64())
		trace(bodyString)
	}

	if (map.bucket && map.key) {
		trace("Uploading the picture to amazon S3")
		putImageInS3(map)
	}

	return result
}


// parse headers that are returned from the camera
private parseHttpHeaders(String headers) {
	def lines = headers.readLines()
	def status = lines[0].split()

	def result = [
	  protocol: status[0],
	  status: status[1].toInteger(),
	  reason: status[2]
	]

	if (result.status == 401) {
		result.auth = stringToMap(lines[1].replaceAll("WWW-Authenticate: Digest ", "").replaceAll("=", ":").replaceAll("\"", ""))
		trace("It's ok. Press take again" + result.auth)
	}

	if (result.status == 200) {
		trace("Authentication successful! :" + result)
	}

	return result
}


// calculate digest token, more details: http://en.wikipedia.org/wiki/Digest_access_authentication#Overview
private String calcDigestAuth(headers) {
	def HA1 = new String("${CameraUser}:" + headers.realm.trim() + ":${CameraPassword}").trim().encodeAsMD5()
	def HA2 = new String("${CameraPostGet}:${CameraPath}").trim().encodeAsMD5()

	// increase nc every request by one
	if (!state.nc) {
		state.nc = 1
	} else {
		state.nc = state.nc + 1
	}

	def cnonce = java.util.UUID.randomUUID().toString().replaceAll('-', '').substring(0, 8)
	def response = new String("${HA1}:" + headers.nonce.trim() + ":" + state.nc + ":" + cnonce + ":" + "auth" + ":${HA2}")
	def response_enc = response.encodeAsMD5()

	trace("HA1: " + HA1 + " ===== org:" + "${CameraUser}:" + headers.realm.trim() + ":${CameraPassword}")
	trace("HA2: " + HA2 + " ===== org:" + "${CameraPostGet}:${CameraPath}")
	trace("Response: " + response_enc + " =====   org:" + response)
	
	def eol = " "
        
    return 'Digest username="' + CameraUser.trim() + '",' + eol +
           'realm="' + headers.realm.trim() + '",' + eol +
           'qop="' + headers.qop.trim() + '",' + eol +
           'algorithm="MD5",' + eol +
           'uri="'+ CameraPath.trim() + '",' +  eol +
           'nonce="' + headers.nonce.trim() + '",' + eol +
           'cnonce="' + cnonce.trim() + '",'.trim() + eol +
           'opaque="",' + eol +
           'nc=' + state.nc + ',' + eol +
           'response="' + response_enc.trim() + '"'
}

private getPictureName() {
	def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
	return device.deviceNetworkId + "_$pictureUuid" + ".jpg"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    trace("IP address entered is $ipAddress and the converted hex code is $hex")
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex, 16)
}


private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]), convertHexToInt(hex[2..3]), convertHexToInt(hex[4..5]), convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

private hashMD5(String somethingToHash) {
	java.security.MessageDigest.getInstance("MD5").digest(somethingToHash.getBytes("UTF-8")).encodeHex().toString()
}

// store image on S3. Hint: if you use your bucket and key maybe you can upload it to your cloud? Never tested, but possible it will work.
def putImageInS3(map) {
	def s3ObjectContent
    
	try {
		def imageBytes = getS3Object(map.bucket, map.key + ".jpg")

		if (imageBytes) {
			s3ObjectContent = imageBytes.getObjectContent()
			def bytes = new ByteArrayInputStream(s3ObjectContent.bytes)
			storeImage(getPictureName(), bytes)
		}
	} catch (Exception e) {
		log.error e
	} finally {
		if (s3ObjectContent) {
			s3ObjectContent.close()
		}
	}
}

private def delayHubAction(ms) {
	return new physicalgraph.device.HubAction("delay ${ms}")
}

private getCallBackAddress() {
	device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private trace(message) {
  log.debug message
}
