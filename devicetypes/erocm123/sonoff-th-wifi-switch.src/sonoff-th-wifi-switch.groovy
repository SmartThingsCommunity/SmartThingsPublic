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
 *  Date: 2017-06-14
 *
 *  2017-06-14: Added option to set frequency of temperature and humidity reports.
 *              Added option to use an external connected switch in place of a temperature/humidity sensor.
 *              Added option to adjust uptime report frequency. Made uptime events not show up in "Recently" tab.
 *              Fixed bug that was making settings appear as if they were not synced even when they were.
 *              Fixed bug that prevented zero values (0) from being entered into preferences.
 *
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
        capability "Health Check"
        
        command "reboot"
                
        attribute   "needUpdate", "string"
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
	details(["switch", "temperature", "humidity",
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
    if (descMap["body"]) body = new String(descMap["body"].decodeBase64())

    if (body && body != "") {
    //log.debug body

    if(body.startsWith("{") || body.startsWith("[")) {
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    log.debug "result: ${result}"

    if (result.containsKey("type")) {
        if (result.type == "configuration") {
            events << update_current_properties(result)
        }
    }
    if (result.containsKey("power")) {
        events << createEvent(name: "switch", value: result.power)
    }
    if (result.containsKey("uptime")) {
        events << createEvent(name: 'uptime', value: result.uptime, displayed: false)
    }
    if (result.containsKey("temperature")) {
        if (result.temperature != "nan") {
            state.realTemperature = convertTemperatureIfNeeded(result.temperature.toFloat(), result.scale)
            events << createEvent(name:"temperature", value:"${getAdjustedTemp(state.realTemperature)}", unit:"${location.temperatureScale}")
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
    cmds << getAction("/status")
    return cmds
}

def ping() {
    log.debug "ping()"
    refresh()
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
    
    cmds << getAction("/configSet?name=haip&value=${device.hub.getDataValue("localIP")}")
    cmds << getAction("/configSet?name=haport&value=${device.hub.getDataValue("localSrvPortTCP")}")
    
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
<Value type="list" byteSize="1" index="pos" label="Boot Up State" min="0" max="2" value="0" setting_type="lan" fw="">
<Help>
Default: Off
</Help>
    <Item label="Off" value="0" />
    <Item label="On" value="1" />
    <Item label="Previous" value="2" />
</Value>
<Value type="number" byteSize="1" index="autooff1" label="Auto Off" min="0" max="65536" value="0" setting_type="lan" fw="">
<Help>
Automatically turn the switch off after this many seconds.
Range: 0 to 65536
Default: 0 (Disabled)
</Help>
</Value>
<Value type="list" byteSize="1" index="externaltype" label="External Device Type" min="0" max="4" value="0" setting_type="lan" fw="">
<Help>
Default: Disabled
</Help>
    <Item label="Disabled" value="0" />
    <Item label="Temperature - AM2301" value="1" />
    <Item label="Temperature - DS18B20" value="2" />
    <Item label="Switch - Momentary" value="3" />
    <Item label="Switch - Toggle" value="4" />
</Value>
<Value type="number" byteSize="1" index="treport" label="Temperature Report Interval" min="0" max="65536" value="300" setting_type="lan" fw="">
<Help>
Send temperature reports at this interval (in seconds).
Range: 0 (Disabled) to 65536
Default: 300
</Help>
</Value>
<Value type="number" byteSize="1" index="hreport" label="Humidity Report Interval" min="0" max="65536" value="300" setting_type="lan" fw="">
<Help>
Send humidity reports at this interval (in seconds).
Range: 0 (Disabled) to 65536
Default: 300
</Help>
</Value>
<Value type="number" byteSize="1" index="ureport" label="Uptime Report Interval" min="0" max="65536" value="300" setting_type="lan" fw="">
<Help>
Send uptime reports at this interval (in seconds).
Range: 0 (Disabled) to 65536
Default: 300
</Help>
</Value>
<Value type="number" byteSize="1" index="tempOffset" label="Temperature Offset" min="-99" max="99" value="0" setting_type="preference" fw="">
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