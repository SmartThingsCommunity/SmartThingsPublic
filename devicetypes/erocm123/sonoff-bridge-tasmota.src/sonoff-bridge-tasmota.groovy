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
 *  Sonoff Bridge - Tasmota
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2019-04-30
 *
 *
 */
 
import groovy.json.JsonSlurper

metadata {
	definition (name: "Sonoff Bridge - Tasmota", namespace: "erocm123", author: "Eric Maycock", vid:"generic-switch") {
        capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Health Check"
        capability "Button"
        capability "Holdable Button"
        
        command "reboot"
                
        attribute   "needUpdate", "string"
        attribute   "noise", "string"
	}

	simulator {
	}
    
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input name: "cdt01", type: "enum", required: false,
					options: [["Disabled" : "Disabled"],
        ["Home Security; Motion Detection" : "Motion Sensor Child Device"],
        ["Carbon Monoxide; Carbon Monoxide detected" : "Carbon Monoxide Detector Child Device"],
        ["Carbon Dioxide; Carbon Dioxide detected" : "Carbon Dioxide Detector Child Device"],
        ["Water Alarm; Water Leak detected" : "Water Sensor Child Device"],
        ["Smoke Alarm; Smoke detected" : "Smoke Detector Child Device"],
        ["Sensor Binary" : "Contact Sensor Child Device"]],
					title: "Child Device 1 Type"
        input name: "cdi01", type: "number", range: "0..100000", required: false,
					title: "Child Device 1 Identifier"
        input name: "cdr01", type: "number", range: "0..100000", required: false,
					title: "Child Device 1 Reset Time"
              
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
        valueTile("temperature","device.temperature", inactiveLabel: false, width: 2, height: 2) {
            	state "temperature",label:'${currentValue}°', backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
				    [value: 74, color: "#44b621"],
				    [value: 84, color: "#f1d801"],
				    [value: 95, color: "#d04e00"],
				    [value: 96, color: "#bc2323"]
			    ]
		}
		valueTile("humidity","device.humidity", inactiveLabel: false, width: 2, height: 2) {
           	state "humidity",label:'RH ${currentValue} %'
		}
        valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
           state "luminosity", label:'${currentValue} LUX', unit:"%", 
                backgroundColors:[
                	[value: 0, color: "#000000"],
                    [value: 1, color: "#060053"],
                    [value: 10, color: "#3E3900"],
                    [value: 30, color: "#8E8400"],
					[value: 50, color: "#C5C08B"],
					[value: 70, color: "#DAD7B6"],
					[value: 90, color: "#F3F2E9"],
                    [value: 100, color: "#FFFFFF"]
				]
		}
        valueTile("noise","device.noise", inactiveLabel: false, width: 2, height: 2) {
           	state "noise",label:'Noise ${currentValue} %'
		}
        valueTile("airQuality","device.airQuality", inactiveLabel: false, width: 2, height: 2) {
           	state "airQuality",label:'Air Quality ${currentValue} %'
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
	details(["switch", "temperature", "humidity","illuminance", "noise", "air",
             "refresh","configure","reboot",
             "ip", "uptime"])
}

private void createChildDevices() {
    if (cdt01 && cdt01 != "Disabled" && !childExists("ep01")) {
        addChildDevice(cdt01, "${device.deviceNetworkId}-ep01", null, [completedSetup: true, label: "${device.label} - Child Device 1",
            isComponent: false, componentName: "ep01", componentLabel: "Child Device 1"
        ])
    } else if ((!cdt01 || cdt01 == "Disabled") && childExists("ep01")) {
        log.debug "Trying to delete child device ep01. If this fails it is likely that there is an App using the child device in question."
        def children = childDevices
        def childDevice = children.find{it.deviceNetworkId.endsWith("ep01")}
            if(childDevice) deleteChildDevice(childDevice.deviceNetworkId)
    }

    childDevices.each {
        if (it.label == "${state.oldLabel} (CH${channelNumber(it.deviceNetworkId)})") {
            def newLabel = "${device.displayName} (CH${channelNumber(it.deviceNetworkId)})"
            it.setLabel(newLabel)
        }
    }
    state.oldLabel = device.label
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
    createChildDevices()
    def cmds = [] 
    cmds = update_needed_settings()
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    if (state.realTemperature != null) sendEvent(name:"temperature", value: getAdjustedTemp(state.realTemperature))
    if (state.realHumidity != null) sendEvent(name:"humidity", value: getAdjustedHumidity(state.realHumidity))
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

def buttonEvent(button, value, type = "digital") {
    createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true, type: type)
}

def parse(description) {
	//log.debug "Parsing: ${description}"
    def events = []
    def descMap = parseDescriptionAsMap(description)
    def body
    //log.debug "descMap: ${descMap}"
    log.debug "========== Parsing Report =========="
    
    //{"Time":"2019-04-04T04:09:33","Uptime":"0T00:05:14","Vcc":3.486,"SleepMode":"Dynamic","Sleep":50,"LoadAvg":19,"Wifi":{"AP":1,"SSId":"cashew","BSSId":"08:62:66:C0:47:10","Channel":11,"RSSI":100,"LinkCount":1,"Downtime":"0T00:00:04"}}
//04:09:34 RSL: SENSOR = {"Time":"2019-04-04T04:09:33","SonoffSC":{"Temperature":20.0,"Humidity":21.0,"Light":30,"Noise":40,"AirQuality":100},"TempUnit":"C"}

    if (!state.mac || state.mac != descMap["mac"]) {
		log.debug "Mac address of device found ${descMap["mac"]}"
        updateDataValue("mac", descMap["mac"])
	}
    
    if (state.mac != null && state.dni != state.mac) state.dni = setDeviceNetworkId(state.mac)
    if (descMap["body"]) body = new String(descMap["body"].decodeBase64())

    if (body && body != "") {
    //log.debug body

    if(body.startsWith("{") || body.startsWith("[")) {
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    //log.debug "result: ${result}"
    
    if (result.containsKey("StatusSTS")) result = result.StatusSTS
    
    if (result.containsKey("RfReceived")) {
        if (result.RfReceived.containsKey("Data"))
            log.debug "Data: $result.RfReceived.Data"
            switch (result.RfReceived.Data) {
                case "321222":
                    events << buttonEvent(1, "pushed", "physical")
                break;
                case "321228":
                    events << buttonEvent(2, "pushed", "physical")
                break;
                case "321221":
                    events << buttonEvent(3, "pushed", "physical")
                break;
                case "321224":
                    events << buttonEvent(4, "pushed", "physical")
                break;
            }
        if (result.RfReceived.containsKey("High"))
            log.debug "High: $result.RfReceived.High"
        if (result.RfReceived.containsKey("Low"))
            log.debug "Low: $result.RfReceived.Low"
        if (result.RfReceived.containsKey("RfKey"))
            log.debug "RfKey: $result.RfReceived.RfKey"
        if (result.RfReceived.containsKey("Sync"))
            log.debug "Sync: $result.RfReceived.Sync"
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

    if (result.containsKey("Uptime")) {
        log.debug "Uptime: $result.Uptime"
        events << createEvent(name: 'uptime', value: result.Uptime, displayed: false)
    }
    } else {
        //log.debug "Response is not JSON: $body"
    }
    }
    
    if (!device.currentValue("ip") || (device.currentValue("ip") != getDataValue("ip"))) events << createEvent(name: 'ip', value: getDataValue("ip"))
    
    return events
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
    cmds << getAction(getCommandString("Status", "0"))
    return cmds
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

 /*  Code has elements from other community source @CyrilPeponnet (Z-Wave Parameter Sync). */

def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    currentProperties."${cmd.name}" = cmd.value

    if (state.settings?."${cmd.name}" != null)
    {
        if (state.settings."${cmd.name}".toString() == cmd.value)
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
                  cmds << getAction("/configSet?name=${it.@index}&value=${it.@value}")
               } else {
                  isUpdateNeeded = "YES"
                  logging("Current value of setting ${it.@index} is unknown", 2)
                  cmds << getAction("/configGet?name=${it.@index}")
               }
            }
            else if ((settings."${it.@index}" != null || it.@hidden == "true") && currentProperties."${it.@index}" != (settings."${it.@index}" != null? settings."${it.@index}".toString() : "${it.@value}"))
            { 
                isUpdateNeeded = "YES"
                logging("Setting ${it.@index} will be updated to ${settings."${it.@index}"}", 2)
                cmds << getAction("/configSet?name=${it.@index}&value=${settings."${it.@index}"}")
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
</Value><Value type="number" byteSize="1" index="tempOffset" label="Temperature Offset" min="-99" max="99" value="0" setting_type="preference" fw="">
<Help>
Range: -99 to 99
Default: 0
</Help>
</Value>
<Value type="number" byteSize="1" index="humidityOffset" label="Humidity Offset" min="-50" max="50" value="0" setting_type="preference" fw="">
<Help>
Range: -50 to 50
Default: 0
</Help>
</Value>
<Value type="list" index="logLevel" label="Debug Logging Level?" value="0" setting_type="preference" fw="">
<Help>
</Help>
    <Item label="None" value="0" />
    <Item label="Reports" value="1" />
    <Item label="All" value="99" />
</Value>
</configuration>
'''
}