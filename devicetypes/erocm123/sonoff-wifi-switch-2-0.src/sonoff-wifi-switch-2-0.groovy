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
    if (state.MAC != null) state.dni = setDeviceNetworkId(state.MAC)
    else 
    if (ip != null) state.dni = setDeviceNetworkId(ip, "80")
    state.hubIP = device.hub.getDataValue("localIP")
    response(setupDevices() + setupRules() + refresh())
}

def setupDevices() {
    def cmds = []
    cmds << postAction("/devices?index=1&page=1", "taskdevicenumber=1&edit=1&page=1")
    cmds << postAction("/devices?index=1&page=1", "taskdevicenumber=1&taskdevicename=BUTTON&taskdevicetimer=0&taskdeviceid=1&taskdevicepin1=0&taskdevicepin1pullup=on&plugin_001_type=1&plugin_001_button=2&taskdevicevaluename1=Switch&edit=1&page=1")
    cmds << postAction("/devices?index=2&page=1", "taskdevicenumber=1&edit=1&page=1")
    cmds << postAction("/devices?index=2&page=1", "taskdevicenumber=1&taskdevicename=SWITCH&taskdevicetimer=0&taskdeviceid=2&taskdevicepin1=12&taskdevicepin1pullup=on&plugin_001_type=1&plugin_001_button=0&taskdevicesenddata=on&taskdevicevaluename1=Switch&edit=1&page=1")
    return delayBetween(cmds, 1000)
}

def setupRules() {
    def cmds = []
    cmds << postAction("/advanced", "mqttsubscribe=&mqttpublish=&messagedelay=1000&ip=0&ntphost=&timezone=0&syslogip=0.0.0.0&sysloglevel=0&udpport=0&useserial=on&serialloglevel=0&webloglevel=0&baudrate=115200&wdi2caddress=0&wireclockstretchlimit=0&userules=on&edit=1")
    cmds << postAction("/rules", "rules=On+BUTTON%23Switch+do%0D%0A++if+%5BSWITCH%23Switch%5D%3D0%0D%0A++++gpio%2C12%2C1%0D%0A++else%0D%0A++++gpio%2C12%2C0%0D%0A++endif%0D%0Aendon%0D%0A%0D%0AOn+SWITCH%23Switch+do%0D%0A+if+%5BSWITCH%23Switch%5D%3D1%0D%0A++++gpio%2C13%2C0%0D%0A++else%0D%0A++++gpio%2C13%2C1%0D%0A++endif%0D%0Aendon")
    return delayBetween(cmds, 1000)
}

def setupConfig() {
    // Automatic config page submit not working at this time. Has to be done manually
    /*def hubIP = device.hub.getDataValue("localIP")
    log.debug "Hub IP: ${hubIP}"
    def cmds = []
    //cmds << postAction("/config", "protocol=1&usedns=0&controllerip=$hubIP&controllerport=39500&controlleruser=&controllerpassword=&delay=60")
    return delayBetween(cmds, 1000)*/
}

def parse(description) {
	//log.debug "Parsing: ${description}"
    def events = []
    def cmds
    def descMap = parseDescriptionAsMap(description)
    def body
    log.debug "descMap: ${descMap}"
    

    if (!state.MAC || state.MAC != descMap["mac"]) {
		log.debug "Mac address of device found ${descMap["mac"]}"
        updateDataValue("MAC", descMap["mac"])
	}
    
    if (state.MAC != null && state.dni != state.MAC) state.dni = setDeviceNetworkId(state.MAC)
    if (descMap["body"]) body = new String(descMap["body"].decodeBase64())

    if (body && body != "") {

    
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

    if (result.containsKey("System")) {
        if (result.System.containsKey("Uptime")) log.debug "System has been up ${result.System.Uptime.toInteger() / 60} hours"
    }
    } else {
        //log.debug "Response is not JSON: $body"
        def ruleSearch = "OnBUTTONSwitchdoifSWITCHSwitch0gpio121elsegpio120endifendonOnSWITCHSwitchdoifSWITCHSwitch1gpio130elsegpio131endifendon"
        if (body.replaceAll("\\W", "").indexOf(ruleSearch) > 0) state.ruleConfigured = true

    }
    } else {
        cmds = refresh()
    }
    
    if (settings.ip) {
        //log.debug "switch: $state.switchConfigured, button: $state.buttonConfigured, rule: $state.ruleConfigured"
        if (state.switchConfigured == true && state.buttonConfigured == true && state.ruleConfigured == true) {
            events << createEvent(name:"hubInfo", value:"For instant status updates, configure switch at http://$settings.ip/config | Hub Info - IP: ${device.hub.getDataValue("localIP")}, Port: 39500")
        } else {
            events << createEvent(name:"hubInfo", value:"Sonoff switch still being configured")
        }
    }
    else { 
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

private parseHTML(html) {
   //log.debug html
   def myHtml = html.split("<")
   def result = []
   switch (myHtml[0]) {
      case "GPIO 12 Set to 0":
      result = [name: "switch", value: "off"]
      break
      case "GPIO 12 Set to 1":
      result = [name: "switch", value: "on"]
      break
      case "GPIO 13 Set to 0":
      log.debug "LED is on"
      break
      case "GPIO 13 Set to 1":
      log.debug "LED is off"
      break
      default:
      break
   }
   return result
}

private ledOn() {
   return getAction("/control?cmd=GPIO,13,0")
}

private ledOff() {
   return getAction("/control?cmd=GPIO,13,1")
}

def on() {
	log.debug "on()"
    def cmds = []
    cmds << getAction("/control?cmd=GPIO,12,1")
    return cmds
}

def off() {
    log.debug "off()"
	def cmds = []
    cmds << getAction("/control?cmd=GPIO,12,0")
    return cmds
}

def refresh() {
	log.debug "refresh()"
    def cmds = []
    cmds << getAction("/json")
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
    def uri = "/?cmd=reboot"
    getAction(uri)
}