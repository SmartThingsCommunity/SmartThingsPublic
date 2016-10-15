/**
 *  Copyright 2016 Eric Maycock
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
 *  Sonoff TH Wifi Switch
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2016-06-02
 */
 
import groovy.json.JsonSlurper

metadata {
	definition (name: "Sonoff TH Wifi Switch", namespace: "erocm123", author: "Eric Maycock") {
        capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
        
        command "reboot"
	}

	simulator {
	}
    
    preferences {
        input("powerOnState", "enum", title:"Boot Up State", description: "State of the relay when it boots up", required: false, displayDuringSetup: false, options: [[0:"Off"],[1:"On"],[2:"Previous State"]])
        input("scale", "enum", title:"Temperature Scale", description: "Choose the temperature scale", required: false, displayDuringSetup: false, options: [[true:"Fahrenheit"],[false:"Celsius"]])
        input("tempOffset", "number", title:"Temperature Offset", description: "Range: -99..99", range: "-99..99", required: false, displayDuringSetup: false)
        input("humidityOffset", "number", title:"Humidity Offset", description: "Range: -50..50", range: "-50..50", required: false, displayDuringSetup: false)
        input("password", "password", title:"Password", required:false, displayDuringSetup:true)
        input("override", "boolean", title:"Override detected IP Address", required: false, displayDuringSetup: false)
        input("ip", "string", title:"IP Address", description: "192.168.1.150", required: false, displayDuringSetup: false)
        input("port", "string", title:"Port", description: "80", required: false, displayDuringSetup: false)
	}

	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.switches.switch.on", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.off", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.switches.switch.off", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.on", nextState:"turningOn"
			}
        }
        valueTile("temperature","device.temperature", inactiveLabel: false, width: 2, height: 2) {
            	state "temperature",label:'${currentValue}°', backgroundColors:[
                	[value: 32, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
				]
		}
		valueTile("humidity","device.humidity", inactiveLabel: false, width: 2, height: 2) {
           	state "humidity",label:'RH ${currentValue} %'
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("configure", "device.configure", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        valueTile("reboot", "device.reboot", decoration: "flat", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "default", label:"Reboot", action:"reboot", icon:"", backgroundColor:"#FFFFFF"
        }
        valueTile("hubInfo", "device.hubInfo", decoration: "flat", height: 2, width: 2, inactiveLabel: false, canChangeIcon: false) {
            state "hubInfo", label:'${currentValue}' //backgroundColor:"#FFFFFF"
        }
        
    }

	main(["switch"])
	details(["switch", "temperature", "humidity",
             "refresh","configure","reboot",
             "hubInfo"])
}

def installed() {
	log.debug "installed()"
	configure()
}

def updated() {
	log.debug "updated()"
    
    if (state.realTemperature != null) sendEvent(name:"temperature", value: getAdjustedTemp(state.realTemperature))
    if (state.realHumidity != null) sendEvent(name:"humidity", value: getAdjustedHumidity(state.realHumidity))
    
    configure()
}

def configure() {
	log.debug "configure()"
	log.debug "Configuring Device For SmartThings Use"
    sendEvent(name:"hubInfo", value:"Sonoff switch still being configured", displayed:false) 
    if (ip != null && ip != "" && override == "true") state.dni = setDeviceNetworkId(ip, "80")
    state.hubIP = device.hub.getDataValue("localIP")
    state.hubPort = device.hub.getDataValue("localSrvPortTCP")
    def responses = []
    responses << getAction("/config?haip=${state.hubIP}&haport=${state.hubPort}&pos=${pos}&fahrenheit=${scale}")
    return response(responses)
}

def parse(description) {
	//log.debug "Parsing: ${description}"
    def events = []
    def cmds
    def descMap = parseDescriptionAsMap(description)
    def body
    //log.debug "descMap: ${descMap}"

    if (!state.mac || state.mac != descMap["mac"]) {
		log.debug "Mac address of device found ${descMap["mac"]}"
        updateDataValue("mac", descMap["mac"])
	}
    
    if (state.mac != null && state.dni != state.mac) state.dni = setDeviceNetworkId(state.mac)
    if (descMap["body"]) body = new String(descMap["body"].decodeBase64())

    if (body && body != "") {
    
    if(body.startsWith("{") || body.startsWith("[")) {
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    log.debug "result: ${result}"
    
    if (result.containsKey("Sensors")) {
        def mySwitch = result.Sensors.find { it.TaskName == "SWITCH" }
        def myButton = result.Sensors.find { it.TaskName == "BUTTON" }
        def myLED = result.Sensors.find { it.TaskName == "LED" }
        if (mySwitch) { 
            events << createEvent(name:"switch", value: (mySwitch.Switch.toInteger() == 0 ? 'off' : 'on'))
            state.switchConfigured = true
        }
        if (myButton) state.buttonConfigured = true
        //if (myLED) log.debug "LED is ${(myLED.Switch.toInteger() == 0 ? 'off' : 'on')}"
    }
    if (result.containsKey("pin")) {
        if (result.pin == 12) events << createEvent(name:"switch", value: (result.state.toInteger() == 0 ? 'off' : 'on'))
    }
    if (result.containsKey("power")) {
        events << createEvent(name: "switch", value: result.power)
    }
    if (result.containsKey("success")) {
        if (result.success == "true") state.configured = true
    }
    if (result.containsKey("uptime")) {
        state.uptime = result.uptime
    }
    if (result.containsKey("temperature")) {
        if (result.temperature != "nan") {
            state.realTemperature = convertTemperatureIfNeeded(result.temperature.toFloat(), result.scale)
            events << createEvent(name:"temperature", value:"${getAdjustedTemp(state.realTemperature)}", unit:"${unit}")
        } else {
            log.debug "The temperature sensor is reporting \"nan\""
        }
    }
    if (result.containsKey("humidity")) {
        if (result.temperature != "nan") {
            state.realHumidity = Math.round((result.humidity as Double) * 100) / 100
            events << createEvent(name: "humidity", value:"${getAdjustedHumidity(state.realHumidity)}", unit:"%")
        } else {
            log.debug "The humidity sensor is reporting \"nan\""
        }
    }
    } else {
        //log.debug "Response is not JSON: $body"
    }
    } else {
        cmds = refresh()
    }
    
    def hubInfoText = ""
    if (getDeviceDataByName("ip") != null && getDeviceDataByName("ip") != "") {
        hubInfoText = "IP Address: ${getDeviceDataByName("ip")} - "
    }
    if (override == "true" && ip != null && ip != "") {
        hubInfoText = "IP Address: $ip - "
    }
    if (state.uptime) {
        hubInfoText = hubInfoText + "Uptime: " + state.uptime
    }
    if (state.configured == true) {
        hubInfoText = hubInfoText + "\r\n - Configured: Yes"
    } else {
        hubInfoText = hubInfoText + "\r\n - Configured: NO"
    }
    
    events << createEvent(name:"hubInfo", value:hubInfoText, displayed:false)
    
    if (cmds) return cmds else return events

}

private getAdjustedTemp(value) {
    value = Math.round((value as Double) * 100) / 100

	if (tempOffset) {
	   return value =  value + Math.round(tempOffset * 100) /100
	} else {
       return value
    }
    
}

private getAdjustedHumidity(value) {
    value = Math.round((value as Double) * 100) / 100

	if (humidityOffset) {
	   return value =  value + Math.round(humidityOffset * 100) /100
	} else {
       return value
    }
    
}

def configureInstant(ip, port, pos){
    return getAction("/config?haip=${ip}&haport=${port}&pos=${pos}")
}



def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
        
        if (nameAndValue.length == 2) map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        else map += [(nameAndValue[0].trim()):""]
	}
}


def on() {
	log.debug "on()"
    def cmds = []
    cmds << getAction("/on")
    return cmds
}

def off() {
    log.debug "off()"
	def cmds = []
    cmds << getAction("/off")
    return cmds
}

def refresh() {
	log.debug "refresh()"
    def cmds = []
    cmds << getAction("/status")
    return cmds
}

private getAction(uri){ 
  updateDNI()
  
  def userpass
  
  if(password != null && password != "") 
    userpass = encodeCredentials("admin", password)
    
  def headers = getHeader(userpass)

  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  )
  return hubAction    
}

private postAction(uri, data){ 
  updateDNI()
  
  def userpass
  
  if(password != null && password != "") 
    userpass = encodeCredentials("admin", password)
  
  def headers = getHeader(userpass)
  
  def hubAction = new physicalgraph.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  return hubAction    
}

private setDeviceNetworkId(ip, port = null){
    def myDNI
    if (port == null) {
        myDNI = ip
    } else {
  	    def iphex = convertIPtoHex(ip)
  	    def porthex = convertPortToHex(port)
        myDNI = "$iphex:$porthex"
    }
    log.debug "Device Network Id set to ${myDNI}"
    return myDNI
}

private updateDNI() { 
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
       device.deviceNetworkId = state.dni
    }
}

private getHostAddress() {
    if (override == "true" && ip != null && ip != ""){
        return "${ip}:80"
    }
    else if(getDeviceDataByName("ip") && getDeviceDataByName("port")){
        return "${getDeviceDataByName("ip")}:${getDeviceDataByName("port")}"
    }else{
	    return "${ip}:80"
    }
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

private encodeCredentials(username, password){
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    return userpass
}

private getHeader(userpass = null){
    def headers = [:]
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
    return headers
}

def reboot() {
	log.debug "reboot()"
    def uri = "/reboot"
    getAction(uri)
}

def sync(ip, port) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    if (ip && ip != existingIp) {
        updateDataValue("ip", ip)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
    }
}