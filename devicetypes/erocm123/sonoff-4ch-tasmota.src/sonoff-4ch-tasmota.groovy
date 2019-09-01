/**
 *  Copyright 2019 Eric Maycock
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
 *  Sonoff POW Wifi Switch
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2019-03-29
 *
 */
 
import groovy.json.JsonSlurper

metadata {
	definition (name: "Sonoff 4Ch - Tasmota", namespace: "erocm123", author: "Eric Maycock", vid:"generic-switch") {
        capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Health Check"
        
        attribute   "amperage", "number"
        attribute   "needUpdate", "string"
        command "reboot"
	}

	simulator {
	}
    
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())
	}

	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", backgroundColor:"#00a0dc", icon: "st.switches.switch.on", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.off", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", backgroundColor:"#00a0dc", icon: "st.switches.switch.off", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.on", nextState:"turningOn"
			}
        }
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }
        standardTile("reboot", "device.reboot", decoration: "flat", height: 2, width: 2, inactiveLabel: false) {
            state "default", label:"Reboot", action:"reboot", icon:"", backgroundColor:"#FFFFFF"
        }
        valueTile("ip", "ip", width: 2, height: 1) {
    		state "ip", label:'IP Address\r\n${currentValue}'
		}
        valueTile("uptime", "uptime", width: 2, height: 1) {
    		state "uptime", label:'Uptime ${currentValue}'
		}   
    }

	main(["switch"])
	details(["switch", 
             "refresh","configure","reboot",
             "ip", "uptime"])
}

def installed() {
	log.debug "installed()"
	configure()
}

def configure() {
    logging("configure()", 1)
    def cmds = []
    cmds = update_needed_settings()
    if (cmds != []) cmds
}

def updated()
{
    logging("updated()", 1)
    if (!childDevices) {
		createChildDevices()
	}
	else if (device.label != state.oldLabel) {
		childDevices.each {
            if (it.label == "${state.oldLabel} (R${channelNumber(it.deviceNetworkId)})") {
			    def newLabel = "${device.displayName} (R${channelNumber(it.deviceNetworkId)})"
			    it.setLabel(newLabel)
            }
		}
		state.oldLabel = device.label
	}
    def cmds = [] 
    cmds = update_needed_settings()
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    if (cmds != []) response(cmds)
}

private def logging(message, level) {
    if (logLevel != "0"){
    switch (logLevel) {
       case "1":
          if (level > 1)
             log.debug "$message"
       break
       case "99":
          log.debug "$message"
       break
    }
    }
}

def parse(description) {
	//log.debug "Parsing: ${description}"
    def events = []
    def descMap = parseDescriptionAsMap(description)
    def body
    //log.debug "descMap: ${descMap}"

    if (!state.mac || state.mac != descMap["mac"]) {
		log.debug "Mac address of device found ${descMap["mac"]}"
        updateDataValue("mac", descMap["mac"])
	}
    
    if (state.mac != null && state.dni != state.mac) state.dni = setDeviceNetworkId(state.mac)
    if (descMap["body"] && descMap["body"] != "T04=") body = new String(descMap["body"].decodeBase64())

    if (body && body != "") {
    
    if(body.startsWith("{") || body.startsWith("[")) {
    log.debug "========== Parsing Report =========="
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    //log.debug "result: ${result}"
    
    if (result.containsKey("StatusSTS")) result = result.StatusSTS
    
    if (result.containsKey("POWER1") || result.containsKey("POWER2") || result.containsKey("POWER3") || result.containsKey("POWER4")) {
    if (result.containsKey("POWER1")) {
        log.debug "POWER1: $result.POWER1"
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep1"}
        if (childDevice) {         
            childDevice.sendEvent(name: "switch", value: result.POWER1)
        }
    }
    if (result.containsKey("POWER2")) {
        log.debug "POWER2: $result.POWER2"
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep2"}
        if (childDevice) {         
            childDevice.sendEvent(name: "switch", value: result.POWER2)
        }
    }
    if (result.containsKey("POWER3")) {
        log.debug "POWER3: $result.POWER3"
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep3"}
        if (childDevice) {         
            childDevice.sendEvent(name: "switch", value: result.POWER3)
        }
    }
    if (result.containsKey("POWER4")) {
        log.debug "POWER4: $result.POWER4"
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep4"}
        if (childDevice) {         
            childDevice.sendEvent(name: "switch", value: result.POWER4)
        }
    }

    def allOff = true
    childDevices.each {
        childDevice ->
        if (childDevice.currentState("switch").value != "OFF") allOff = false
    }

    if (allOff) {
        events << createEvent([name: "switch", value: "off"])
    } else {
        events << createEvent([name: "switch", value: "on"])
    }
    }
    if (result.containsKey("LoadAvg")) {
        log.debug "LoadAvg: $result.LoadAvg"
    }
    if (result.containsKey("Sleep")) {
        log.debug "Sleep: $result.Sleep"
    }
    if (result.containsKey("SleepMode")) {
        log.debug "SleepMode: $result.SleepMode"
    }
    if (result.containsKey("Vcc")) {
        log.debug "Vcc: $result.Vcc"
    }
    if (result.containsKey("Wifi")) {
        if (result.Wifi.containsKey("AP")) {
            log.debug "AP: $result.Wifi.AP"
        }
        if (result.Wifi.containsKey("BSSId")) {
            log.debug "BSSId: $result.Wifi.BSSId"
        }
        if (result.Wifi.containsKey("Channel")) {
            log.debug "Channel: $result.Wifi.Channel"
        }
        if (result.Wifi.containsKey("RSSI")) {
            log.debug "RSSI: $result.Wifi.RSSI"
        }
        if (result.Wifi.containsKey("SSId")) {
            log.debug "SSId: $result.Wifi.SSId"
        }
    }
    if (result.containsKey("Hostname")) {
        log.debug "Hostname: $result.Hostname"
    }
    if (result.containsKey("IPAddress")) {
        log.debug "IPAddress: $result.IPAddress"
    }
    if (result.containsKey("WebServerMode")) {
        log.debug "WebServerMode: $result.WebServerMode"
    }
    if (result.containsKey("Version")) {
        log.debug "Version: $result.Version"
    }
    if (result.containsKey("Module")) {
        log.debug "Module: $result.Module"
    }
    if (result.containsKey("RestartReason")) {
        log.debug "RestartReason: $result.RestartReason"
    }
    if (result.containsKey("SetOption81")) {
        log.debug "SetOption81: $result.SetOption81"
    }
    if (result.containsKey("Uptime")) {
        log.debug "Uptime: $result.Uptime"
        events << createEvent(name: 'uptime', value: result.Uptime, displayed: false)
    }
    if (result.containsKey("LedPower")) {
        log.debug "LedPower: $result.LedPower"
        processSetting("LedPower", result.LedPower == "ON"? 1 : 0)
    }
    if (result.containsKey("LedState")) {
        log.debug "LedState: $result.LedState"
        processSetting("LedState", result.LedState)
    }
    if (result.containsKey("HubitatHost")) {
        log.debug "HubitatHost: $result.HubitatHost"
    }
    if (result.containsKey("HubitatPort")) {
        log.debug "HubitatPort: $result.HubitatPort"
    }
    if (result.containsKey("PowerOnState")) {
        log.debug "PowerOnState: $result.PowerOnState"
        processSetting("PowerOnState", result.PowerOnState)
    }
    if (result.containsKey("SetOption81")) {
        log.debug "SetOption81: $result.SetOption81"
        processSetting("SetOption81", result.SetOption81 == "ON"? 1 : 0)
    }
    } else {
        //log.debug "Response is not JSON: $body"
    }
    }
    
    if (!device.currentValue("ip") || (device.currentValue("ip") != getDataValue("ip"))) events << createEvent(name: 'ip', value: getDataValue("ip"))
    
    return events
}

def processSetting(name, value){
    def currentProperties = state.currentProperties ?: [:]
    currentProperties."${name}" = "${value}"
    
    if (state.settings?."${name}" != null)
    {
        if (state.settings."${name}".toString() == "${value}")
        {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
        }
    }
    state.currentProperties = currentProperties
}

def getCommandString(command, value) {
    def uri = "/cm?"
    if (password) {
        uri += "user=admin&password=${password}&"
    }
	if (value) {
		uri += "cmnd=${command}%20${value}"
	}
	else {
		uri += "cmnd=${command}"
	}
    return uri
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
    cmds << getAction(getCommandString("Power1", "On"))
    cmds << getAction(getCommandString("Power2", "On"))
    cmds << getAction(getCommandString("Power3", "On"))
    cmds << getAction(getCommandString("Power4", "On"))
    return cmds
}

def off() {
    log.debug "off()"
	def cmds = []
    cmds << getAction(getCommandString("Power1", "Off"))
    cmds << getAction(getCommandString("Power2", "Off"))
    cmds << getAction(getCommandString("Power3", "Off"))
    cmds << getAction(getCommandString("Power4", "Off"))
    return cmds
}

def refresh() {
	log.debug "refresh()"
    def cmds = []
    cmds << getAction(getCommandString("Status", "0"))
    return cmds
}

void childOn(String dni) {
    log.debug "childOn($dni)"
    def cmds = []
    cmds << getAction(getCommandString("Power${channelNumber(dni)}", "On"))
	sendHubCommand(cmds)
}

void childOff(String dni) {
    log.debug "childOff($dni)"
	def cmds = []
    cmds << getAction(getCommandString("Power${channelNumber(dni)}", "Off"))
	sendHubCommand(cmds)
}

void childRefresh(String dni) {
    log.debug "childRefresh($dni)"
	def cmds = []
    cmds << getAction(getCommandString("Power${channelNumber(dni)}", ""))
    sendHubCommand(cmds)
}

def ping() {
    log.debug "ping()"
    refresh()
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
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
       device.deviceNetworkId = state.dni
    }
}

private getHostAddress() {
    if (override == true && ipAddress != null && port != null){
        return "${ipAddress}:${port}"
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

private getHeader(){
    def headers = [:]
    //log.debug getHostAddress()
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    return headers
}

def reboot() {
	log.debug "reboot()"
    getAction(getCommandString("Restart", "1"))
}

def sync(ip, port) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    if (ip && ip != existingIp) {
        updateDataValue("ip", ip)
        sendEvent(name: 'ip', value: ip)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
    }
}

private channelNumber(String dni) {
	dni.split("-ep")[-1] as Integer
}

private void createChildDevices() {
	state.oldLabel = device.label
    if ( device.deviceNetworkId =~ /^([0-9A-F]{2}){6}$/) {
     try {
        for (i in 1..4) {
	       addChildDevice("Switch Child Device", "${device.deviceNetworkId}-ep${i}", null,
		      [completedSetup: true, label: "${device.displayName} (R${i})",
		      isComponent: false, componentName: "ep$i", componentLabel: "Relay $i"])
        }
    } catch (e) {
        state.alertMessage = "Child device creation failed. Please make sure that the \"Switch Child Device\" is installed and published."
	    runIn(2, "sendAlert")
    }
    } else {
        state.alertMessage = "Device has not yet been fully configured. Hit the configure button device tile and try again."
        runIn(2, "sendAlert")
    
    }
}

private sendAlert() {
   sendEvent(
      descriptionText: state.alertMessage,
	  eventType: "ALERT",
	  name: "childDeviceCreation",
	  value: "failed",
	  displayed: true,
   )
}


def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
   
    configuration.Value.each
    {
        if(it.@hidden != "true" && it.@disabled != "true"){
        switch(it.@type)
        {   
            case ["number"]:
                input "${it.@index}", "number",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items
            break
            case ["password"]:
                input "${it.@index}", "password",
                    title:"${it.@label}\n" + "${it.Help}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
        }
        }
    }
}

def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
    
    state.settings = settings
     
    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    

    cmds << getAction(getCommandString("HubitatHost", device.hub.getDataValue("localIP")))
    cmds << getAction(getCommandString("HubitatPort", device.hub.getDataValue("localSrvPortTCP")))
    
    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "lan" && it.@disabled != "true"){
            if (currentProperties."${it.@index}" == null)
            {
               if (it.@setonly == "true"){
                  logging("Setting ${it.@index} will be updated to ${it.@value}", 2)
                  cmds << getAction(getCommandString(it.@index, it.@value))
               } else {
                  isUpdateNeeded = "YES"
                  logging("Current value of setting ${it.@index} is unknown", 2)
                  cmds << getAction(getCommandString(it.@index, ""))
               }
            }
            else if ((settings."${it.@index}" != null || it.@hidden == "true") && currentProperties."${it.@index}" != (settings."${it.@index}" != null? settings."${it.@index}".toString() : "${it.@value}"))
            { 
                isUpdateNeeded = "YES"
                logging("Setting ${it.@index} will be updated to ${settings."${it.@index}"}", 2)
                cmds << getAction(getCommandString(it.@index, it.@value))
            } 
        }
    }
    
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

def configuration_model()
{
'''
<configuration>
<Value type="password" byteSize="1" index="password" label="Password" min="" max="" value="" setting_type="preference" fw="">
<Help>
</Help>
</Value>
<Value type="list" byteSize="1" index="PowerOnState" label="Power On State" min="0" max="3" value="3" setting_type="lan" fw="">
<Help>
Default: Previous
</Help>
    <Item label="Off" value="0" />
    <Item label="On" value="1" />
    <Item label="Previous" value="3" />
</Value>
<Value type="byte" byteSize="1" index="SetOption81" label="Enable SmartThings Support" min="0" max="1" value="1" setting_type="lan" fw="" hidden="true">
<Help>
</Help>
</Value>
<Value type="list" byteSize="1" index="LedPower" label="LED Power" min="0" max="1" value="1" setting_type="lan" fw="" hidden="true">
<Help>
</Help>
    <Item label="OFF" value="0" />
    <Item label="ON" value="1" />
</Value>
<Value type="byte" byteSize="1" index="LedState" label="LED State" min="0" max="8" value="8" setting_type="lan" fw="" hidden="true">
<Help>
</Help>
</Value>
<Value type="list" index="logLevel" label="Debug Logging Level?" value="1" setting_type="preference" fw="">
<Help>
</Help>
    <Item label="None" value="0" />
    <Item label="Reports" value="1" />
    <Item label="All" value="99" />
</Value>
</configuration>
'''
}