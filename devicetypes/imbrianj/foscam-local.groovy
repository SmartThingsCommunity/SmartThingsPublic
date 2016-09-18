/**
 *  Foscam
 *
 *  Author: SmartThings
 *  Date: 2014-02-04
 */

metadata {
  definition (name: "Foscam", namespace: "smartthings", author: "SmartThings") {
    capability "Actuator"
    capability "Sensor"
    capability "Image Capture"

    attribute "alarmStatus", "string"

    command "alarmOn"
    command "alarmOff"
    command "left"
    command "right"
    command "up"
    command "down"
    command "pause"
    command "preset"
    command "preset1"
    command "preset2"
    command "preset3"
  }

  //TODO:encrypt these settings and make them required:true
  preferences {
    input "username", "text", title: "Username", description: "Your Foscam Username", required: false
    input "password", "password", title: "Password", description: "Your Foscam Password", required: false
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

    standardTile("up", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "up", label: "up", action: "up", icon: ""
    }

    standardTile("blank", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "blank", label: "", action: "pause", icon: ""
    }

    standardTile("left", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "left", label: "left", action: "left", icon: ""
    }

    standardTile("pause", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "pause", label: "pause", action: "pause", icon: ""
    }
    
    standardTile("right", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "right", label: "right", action: "right", icon: ""
    }

    standardTile("alarmOn", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "alarmOn", label: "Alarm On", action: "alarmOn", icon: "st.camera.dropcam-centered"
    }

    standardTile("down", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "down", label: "down", action: "down", icon: ""
    }

    standardTile("alarmOff", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "alarmOff", label: "Alarm Off", action: "alarmOff", icon: "st.camera.dropcam-centered"
    }

    standardTile("preset1", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "preset1", label: "preset 1", action: "preset1", icon: ""
    }

    standardTile("preset2", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "preset2", label: "preset 2", action: "preset2", icon: ""
    }

    standardTile("preset3", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "preset3", label: "preset 3", action: "preset3", icon: ""
    }

    main "camera"

    details(["cameraDetails", "take", "up", "blank", "left", "pause", "right", "alarmOn", "down", "alarmOff", "preset1", "preset2", "preset3", "refresh"])
  }
}

// parse events into attributes
def parse(String description) {
  log.debug("Parsing '${description}'")

  def map = stringToMap(description)
  log.debug map

  def result = []

  if (map.bucket && map.key) { //got a s3 pointer
    putImageInS3(map)
  }

  else if (map.headers && map.body) { //got device info response

    /*
    TODO:need to figure out a way to reliably know which end the snapshot should be taken at.
    Current theory is that 8xxx series cameras are at /snapshot.cgi and 9xxx series are at /cgi-bin/CGIProxy.fcgi
    */

    def headerString = new String(map.headers.decodeBase64())
    if (headerString.contains("404 Not Found")) {
      state.snapshot = "/snapshot.cgi"
    }

    if (map.body) {
      def bodyString = new String(map.body.decodeBase64())
      def body = new XmlSlurper().parseText(bodyString)
      def productName = body?.productName?.text()
      if (productName) {
        log.trace "Got Foscam Product Name: $productName"
        state.snapshot = "/cgi-bin/CGIProxy.fcgi"
      }
    }
  }

  result
}

def putImageInS3(map) {
  def s3ObjectContent
  try {
    def imageBytes = getS3Object(map.bucket, map.key + ".jpg")

    if(imageBytes) {
      s3ObjectContent = imageBytes.getObjectContent()
      def bytes = new ByteArrayInputStream(s3ObjectContent.bytes)
      storeImage(getPictureName(), bytes)
    }
  }
  catch(Exception e) {
    log.error e
  }

  finally {
    //explicitly close the stream
    if (s3ObjectContent) { s3ObjectContent.close() }
  }
}

// handle commands
def take() {
  api('snapshot')
}

def alarmOn() {
  api("set_alarm", "motion_armed=1")
}

def alarmOff() {
  api("set_alarm", "motion_armed=0")
}

def left() {
  api("decoder_control", "command=6")
}

def right() {
  api("decoder_control", "command=4")
}

def up() {
  api("decoder_control", "command=0")
}

def down() {
  api("decoder_control", "command=2")
}

def pause() {
  api("decoder_control", "command=1")
}

def preset1() {
  preset(1)
}

def preset2() {
  preset(2)
}

def preset3() {
  preset(3)
}

//go to a preset location
def preset(def num) {
  if(num == null) return

  //1 is 31, 2 is 33, 3 is 35
  def cmd = 30 + (num * 2) - 1

  api("decoder_control", "command=${cmd}")
}

private api(method, args = []) {
  log.debug("Executing ${method}")

  def methods = [
    "decoder_control": [uri: "/decoder_control.cgi${getLogin()}&${args}"],
    "snapshot":        [uri: "/snapshot.cgi${getLogin()}&${args}"],
    "set_alarm":       [uri: "/set_alarm.cgi${getLogin()}&${args}"],
    "reboot":          [uri: "/reboot.cgi${getLogin()}&${args}"],
    "camera_control":  [uri: "/camera_control.cgi${getLogin()}&${args}"],
    "get_params":      [uri: "/get_params.cgi${getLogin()}"],
    "videostream":     [uri: "/videostream.cgi${getLogin()}"]
  ]

  def request = methods.getAt(method)

  action(request.uri)
}

private action(uri) {
  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: [HOST:getHostAddress()]
  )

  hubAction.options = [outputMsgToS3:true]
  hubAction
}

//helper methods
private getPictureName() {
  def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
  return device.deviceNetworkId + "_$pictureUuid" + ".jpg"
}

private getLogin() {
  def username = getUsername()
  def password = getPassword()
  return "?user=${username}&pwd=${password}"
}

private getUsername() {
  settings.username
}

private getPassword() {
  settings.password
}

private Integer convertHexToInt(hex) {
  Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
  [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
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

private calcDigestAuth(String method, String uri) {
  def HA1 =  hashMD5("${getUsername}::${getPassword}")
  def HA2 = hashMD5("${method}:${uri}")
  def response = hashMD5("${HA1}::::auth:${HA2}")

  'Digest username="'+ getUsername() + '", realm="", nonce="", uri="'+ uri +'", qop=auth, nc=, cnonce="", response="' + response + '", opaque=""'
}
