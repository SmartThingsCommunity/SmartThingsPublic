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
 *  Sonoff Wifi Switch 2.0
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2016-01-27
 */
 
import groovy.json.JsonSlurper
import groovy.util.XmlSlurper

metadata {
	definition (name: "Sonoff Wifi Switch 2.0", namespace: "erocm123", author: "Eric Maycock") {
        capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        
        command "reboot"
	}

	simulator {
	}
    
    preferences {
        input("ip", "string", title:"IP Address", description: "192.168.1.150" ,required: true, displayDuringSetup: true)
        // Port should always be 80
        //input("port", "string", title:"Port", description: "80" , required: true, displayDuringSetup: true)
	}

	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
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
        valueTile("hubInfo", "device.hubInfo", decoration: "flat", height: 2, width: 6, inactiveLabel: false, canChangeIcon: false) {
            state "hubInfo", label:'${currentValue}' //backgroundColor:"#FFFFFF"
        }
        
    }

	main(["switch"])
	details(["switch",
             "refresh","configure","reboot",
             "hubInfo"])
}

def installed() {
	log.debug "installed()"
	configure()
}

def updated() {
	log.debug "updated()"
    configure()
}

def configure() {
	log.debug "configure()"
	log.debug "Configuring Device For SmartThings Use"
    state.ruleConfigured = false
    state.switchConfigured = false
    state.buttonConfigured = false
    sendEvent(name:"hubInfo", value:"Sonoff switch still being configured")
    //if (state.MAC != null) state.dni = setDeviceNetworkId(state.MAC)
    //else 
    if (ip != null) state.dni = setDeviceNetworkId(ip, "80")
    state.hubIP = device.hub.getDataValue("localIP")
    response(configureInstant(state.hubIP, "39500"))
}

def configureInstant(ip, port){
    return [getAction("/config?haip=${ip}&haport=${port}")]
}

def parse(description) {
	//log.debug "Parsing: ${description}"
    def events = []
    def cmds
    def descMap = parseDescriptionAsMap(description)
    def body
    //log.debug "descMap: ${descMap}"
    

    if (!state.MAC || state.MAC != descMap["mac"]) {
		log.debug "Mac address of device found ${descMap["mac"]}"
        updateDataValue("MAC", descMap["mac"])
	}
    
    if (state.MAC != null && state.dni != state.MAC) state.dni = setDeviceNetworkId(state.MAC)
    if (descMap["body"]) body = new String(descMap["body"].decodeBase64())

    if (body && body != "") {
    log.debug body

    
    if(body.startsWith("{") || body.startsWith("[")) {
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    //log.debug "result: ${result}"
    
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
    if (result.containsKey("System")) {
        if (result.System.containsKey("Uptime")) log.debug "System has been up ${result.System.Uptime.toInteger() / 60} hours"
    }
    if (result.containsKey("success")) {
        if (result.success == "true") events << createEvent(name:"hubInfo", value:"Switch has been configured.")
    }
    } else {
        //log.debug "Response is not JSON: $body"
        def ruleSearch = "OnBUTTONSwitchdoifSWITCHSwitch0gpio121elsegpio120endifendonOnSWITCHSwitchdoifSWITCHSwitch1gpio130elsegpio131endifendon"
        if (body.replaceAll("\\W", "").indexOf(ruleSearch) > 0) state.ruleConfigured = true

    }
    } else {
        cmds = refresh()
    }
    
    
    
    if (settings.ip == null || settings.ip == "") {
        events << createEvent(name:"hubInfo", value:"IP address of the switch not entered. Please do so in device preferences.") 
    }

    if (cmds) return cmds else return events

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
  def headers = getHeader()
  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  )
  return hubAction    
}

private postAction(uri, data){ 
  updateDNI()
  def headers = getHeader()
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
    if (device.deviceNetworkId != state.dni) {
        device.deviceNetworkId = state.dni
    }
}

private getHostAddress() {
	return "${ip}:80"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

private getHeader(){
    def headers = [:]
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    return headers
}

def reboot() {
	log.debug "reboot()"
    def uri = "/reboot"
    getAction(uri)
}