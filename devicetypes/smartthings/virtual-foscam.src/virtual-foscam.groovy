import static java.util.UUID.randomUUID 
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import java.security.SignatureException

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Virtual Foscam", namespace: "smartthings", author: "sidoh") {
		capability "Actuator"
		capability "Image Capture"
        capability "Switch"
        
        command "enableRecording"
        command "disableRecording"
        
        attribute "preset", "enum", ["Door", "Couch", "Table"]
        command "setPresetDoor"
        command "setPresetCouch"
        command "setPresetTable"
	}
    
    preferences {
    	input "camera", "text", title: "Camera Identifier", displayDuringSetup: true
    }

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("camera", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: true) {
			state "default", label: "", action: "", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
		}

		carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

		standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
			state "taking", label:'Taking', action: "", icon: "st.camera.dropcam", backgroundColor: "#53a7c0"
			state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
		}
        
        standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.Entertainment.entertainment9", backgroundColor:"#79b821", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.Entertainment.entertainment9", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', icon:"st.Entertainment.entertainment9", backgroundColor:"#79b821"
            state "turningOff", label:'${name}', icon:"st.Entertainment.entertainment9", backgroundColor:"#ffffff"
		}
        
        standardTile("presetDoor", "device.preset", width: 1, height: 1, canChangeIcon: true) {
        	state "Door", label: '${name}', backgroundColor:"#79b821", action: "setPresetDoor"
        }
        
        standardTile("presetCouch", "device.preset", width: 1, height: 1, canChangeIcon: true) {
        	state "Couch", label: '${name}', backgroundColor:"#79b821", action: "setPresetCouch"
        }
        
        standardTile("presetTable", "device.preset", width: 1, height: 1, canChangeIcon: true) {
        	state "Table", label: '${name}', backgroundColor:"#79b821", action: "setPresetTable"
        }

		main "switch"
		details(["cameraDetails", "take", "switch", "presetDoor", "presetCouch", "presetTable"])
	}
}

def setPresetDoor() {
	setPreset("Door")
}

def setPresetCouch() {
	setPreset("Couch")
}

def setPresetTable() {
	setPreset("Table")
}

def on() {
	sendEvent(name: "switch", value: "on")
    gwPost('', [recording: true])
}

def off() {
	sendEvent(name: "switch", value: "off")
    gwPost('', [recording: false])
}

def take() {
	log.info "Executing 'take'"

	gwGet('/snapshot.jpg') {
    	final def imageName = "${randomUUID() as String}.jpg"
		storeImage(imageName, it.data)
    }
}

def hmac(String data, String key) throws SignatureException {
  final Mac hmacSha1;
  try {
     hmacSha1 = Mac.getInstance("HmacSHA1");
  } catch (Exception nsae) {
     hmacSha1 = Mac.getInstance("HMAC-SHA-1");         
  }
  
  final SecretKeySpec macKey = new SecretKeySpec(key.getBytes(), "RAW");
  hmacSha1.init(macKey);
  
  final byte[] signature =  hmacSha1.doFinal(data.getBytes());
  
  return signature.encodeHex()
}

def getHmacHeaders() {
    final def payload = randomUUID() as String
    long time = new Date().getTime() 
    time /= 1000L
    
    final String signature = hmac(payload + time, '<security_token>')

    [
        'X-Signature-Timestamp': time,
        'X-Signature-Payload': payload,
        'X-Signature': signature
    ]
}

def setPreset(preset) {
	gwPost('', [preset: preset])
    runIn(2, take)
    sendEvent(name: 'preset', value: preset)
}

def gwPost(path, params, success = {}) {
    httpPost(
    	[
            uri: ("http://<ha_gateway_url>/camera/${settings.camera}${path}"),
            body: params,
            headers: getHmacHeaders(),
            success: success
        ]
    )
}

def gwGet(path, success = {}) {
    httpGet(
    	[
            uri: ("http://<ha_gateway_url>/camera/${settings.camera}${path}"),
            headers: getHmacHeaders(),
            success: success
        ]
    )
}