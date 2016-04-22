/**
 *  Copyright 2015 Chris Kitch
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
 * Wemo Maker
 *
 * Author: Chris Kitch
 * Date: 21-05-2016
 */ 
 // ---- METADATA ---- //
 metadata {
 	definition (name: "Wemo Maker", namespace: "kriskit", author: "Chris Kitch") {
        capability "Actuator"
        capability "Switch"
        capability "Contact Sensor"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"

        attribute "currentIP", "string"
        attribute "switchMode", "string"
        attribute "sensorPresent", "string"

        command "subscribe"
        command "resubscribe"
        command "unsubscribe"
        command "setOffline"
     }
 
 	preferences {
		section {
            input "sensorInvert", "bool", title: "Invert Sensor",
                description: "Inverts the sensor input so it's reported as its opposite value", 
                defaultValue: false,
                required: true, 
                displayDuringSetup: true
    	}
    }

     // simulator metadata
     simulator {}

     // UI tile definitions
    tiles(scale: 2) {
		multiAttributeTile(name: "main", type: "generic", width: 6, height: 4, canChangeIcon: true) {
        	tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
            	attributeState "momentary", label: "Activate", action: "switch.on", icon: "st.switches.switch.on", backgroundColor: "#9bceff", nextState: "activating"
                attributeState "activating", label:'Activating', icon:"st.switches.switch.on", backgroundColor:"#c4e2ff"
                attributeState "on", label:'On', action:"switch.off", icon:"st.switches.switch.off", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'Off', action:"switch.on", icon:"st.switches.switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'Turning On', action:"switch.off", icon:"st.switches.switch.off", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'Turning Off', action:"switch.on", icon:"st.switches.switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "offline", label:'Offline', icon:"st.switches.switch.off", backgroundColor:"#ff0000"
            }
            
             tileAttribute ("device.contact", key: "SECONDARY_CONTROL") {
	            attributeState "contact", label: 'Contact ${currentValue}'
			}
		}
        
        standardTile("contact", "device.contact", width: 3, height: 2, canChangeIcon: true, decoration: "flat") {
            state "open", label:'Open', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
            state "closed", label:'Closed', icon:"st.contact.contact.closed", backgroundColor:"#ffffff"
            state "disabled", label:'Disabled', backgroundColor:"#cccccc"
			state "offline", label:'Offline', backgroundColor:"#ff0000"
        }
        
        valueTile("switchModeTitle", "switchModeTitle", width: 3, height: 1, canChangeIcon: true, decoration: "flat") {
            state "default", label:'Switch Mode', backgroundColor:"#ffffff"
        }
        
        valueTile("switchMode", "switchMode", width: 3, height: 1, canChangeIcon: true, decoration: "flat") {
            state "toggle", label:'Toggle', backgroundColor:"#ffffff"
            state "momentary", label:'Momentary', backgroundColor:"#ffffff"
			state "offline", label:'Offline', backgroundColor:"#ff0000"
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["main"])
        details(["main", "contact", "switchModeTitle", "switchMode", "refresh"])
    }
}
// ---- /METADATA ---- //

// ---- SETUP ---- //
def installed() {
    poll()
}

def updated() {
    poll()
}
// ---- /SETUP ---- //

// ---- COMMANDS ---- //
def on() {
	if (device.currentValue("switchMode") == "momentary")
        sendEvent(name: "switch", value: "momentary", descriptionText: "Switch was triggered", displayed: true, isStateChange: true) 
        
    trigger()
}

def trigger() {
def turnOn = new physicalgraph.device.HubAction("""POST /upnp/control/basicevent1 HTTP/1.1
SOAPAction: "urn:Belkin:service:basicevent:1#SetBinaryState"
Host: ${getHostAddress()}
Content-Type: text/xml
Content-Length: 333

<?xml version="1.0"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<SOAP-ENV:Body>
 <m:SetBinaryState xmlns:m="urn:Belkin:service:basicevent:1">
<BinaryState>1</BinaryState>
 </m:SetBinaryState>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>""", physicalgraph.device.Protocol.LAN)
}

def off() {
	if (device.currentValue("switchMode") != "toggle")
    	return
    
log.debug "Executing 'off'"
def turnOff = new physicalgraph.device.HubAction("""POST /upnp/control/basicevent1 HTTP/1.1
SOAPAction: "urn:Belkin:service:basicevent:1#SetBinaryState"
Host: ${getHostAddress()}
Content-Type: text/xml
Content-Length: 333

<?xml version="1.0"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<SOAP-ENV:Body>
 <m:SetBinaryState xmlns:m="urn:Belkin:service:basicevent:1">
<BinaryState>0</BinaryState>
 </m:SetBinaryState>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>""", physicalgraph.device.Protocol.LAN)
}

def subscribe(hostAddress) {
log.debug "Executing 'subscribe()'"
def address = getCallBackAddress()
new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${hostAddress}
CALLBACK: <http://${address}/>
NT: upnp:event
TIMEOUT: Second-5400
User-Agent: CyberGarage-HTTP/1.0


""", physicalgraph.device.Protocol.LAN)
}

def subscribe() {
	subscribe(getHostAddress())
}

def refresh() {
 	log.debug "Executing WeMo Switch 'subscribe', then 'timeSyncResponse', then 'poll'"
 	[subscribe(), timeSyncResponse(), poll()]
}

def subscribe(ip, port) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    if (ip && ip != existingIp) {
         log.debug "Updating ip from $existingIp to $ip"    
    	 updateDataValue("ip", ip)
    	 def ipvalue = convertHexToIP(getDataValue("ip"))
         sendEvent(name: "currentIP", value: ipvalue, descriptionText: "IP changed to ${ipvalue}")
    }
 	if (port && port != existingPort) {
 		log.debug "Updating port from $existingPort to $port"
 		updateDataValue("port", port)
	}
	subscribe("${ip}:${port}")
}

def resubscribe() {
    log.debug "Executing 'resubscribe()'"
    def sid = getDeviceDataByName("subscriptionId")
new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}
TIMEOUT: Second-5400


""", physicalgraph.device.Protocol.LAN)
}


def unsubscribe() {
    def sid = getDeviceDataByName("subscriptionId")
new physicalgraph.device.HubAction("""UNSUBSCRIBE publisher path HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}


""", physicalgraph.device.Protocol.LAN)
}


//TODO: Use UTC Timezone
def timeSyncResponse() {
log.debug "Executing 'timeSyncResponse()'"
new physicalgraph.device.HubAction("""POST /upnp/control/timesync1 HTTP/1.1
Content-Type: text/xml; charset="utf-8"
SOAPACTION: "urn:Belkin:service:timesync:1#TimeSync"
Content-Length: 376
HOST: ${getHostAddress()}
User-Agent: CyberGarage-HTTP/1.0

<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
 <s:Body>
  <u:TimeSync xmlns:u="urn:Belkin:service:timesync:1">
   <UTC>${getTime()}</UTC>
   <TimeZone>-05.00</TimeZone>
   <dst>1</dst>
   <DstSupported>1</DstSupported>
  </u:TimeSync>
 </s:Body>
</s:Envelope>
""", physicalgraph.device.Protocol.LAN)
}

def setOffline() {
	//sendEvent(name: "currentIP", value: "Offline", displayed: false)
    sendEvent(name: "switch", value: "offline", descriptionText: "The device is offline")
    sendEvent(name: "contact", value: "offline", descriptionText: "The device is offline", displayed: false)
    sendEvent(name: "switchMode", value: "offline", descriptionText: "The device is offline", displayed: false)
}

def poll() {
log.debug "Executing 'poll'"
if (device.currentValue("currentIP") != "Offline")
    runIn(30, setOffline)
new physicalgraph.device.HubAction("""POST /upnp/control/deviceevent1 HTTP/1.1
SOAPACTION: "urn:Belkin:service:deviceevent:1#GetAttributes"
Content-Length: 279
Content-Type: text/xml; charset="utf-8"
HOST: ${getHostAddress()}
User-Agent: CyberGarage-HTTP/1.0

<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<s:Body>
<u:GetAttributes xmlns:u="urn:Belkin:service:deviceevent:1">null</u:GetAttributes>
</s:Body>
</s:Envelope>""", physicalgraph.device.Protocol.LAN)
}

// ---- /COMMANDS ---- //

// ---- EVENTS ---- //
// parse events into attributes
def parse(String description) {
	log.debug "Event received."

    def msg = parseLanMessage(description)
    def headerString = msg.header

    if (headerString?.contains("SID: uuid:")) {
        def sid = (headerString =~ /SID: uuid:.*/) ? ( headerString =~ /SID: uuid:.*/)[0] : "0"
        sid -= "SID: uuid:".trim()
        updateDataValue("subscriptionId", sid)
 	}

    def result = []
    def bodyString = msg.body
    
    if (bodyString) {
    	unschedule("setOffline")
        def body = new XmlSlurper().parseText(bodyString)
        
        handleTimeSyncRequest(body, result)
        handleSetBinaryStateResponse(body, result)
        handleBinaryState(body, result)
        handlePropertySet(body, result)
        handleTimeZoneNotification(body, result)
        handleGetAttributesResponse(body, result)
 	}
    
 return result
}

private handleTimeSyncRequest(body, result) {
	if (!body?.property?.TimeSyncRequest?.text())
    	return
        
	log.debug "Got TimeSyncRequest"
	result << timeSyncResponse()
}

private handleSetBinaryStateResponse(body, result) {
	if (!body?.Body?.SetBinaryStateResponse?.BinaryState?.text())
    	return
        
	log.debug "Got SetBinaryStateResponse = ${body?.Body?.SetBinaryStateResponse?.BinaryState?.text()}"
}

private handleBinaryState(body, result) {
	if (device.currentValue("switchMode") != "toggle")
    	return

	if (body?.property?.BinaryState?.text()) {
        def value = body?.property?.BinaryState?.text().substring(0, 1).toInteger() == 0 ? "off" : "on"
        log.debug "Notify: BinaryState = ${value}, ${body.property.BinaryState}"
        def dispaux = device.currentValue("switch") != value
        result << createEvent(name: "switch", value: value, descriptionText: "Switch is ${value}", displayed: dispaux)
    }
    
    if (body?.Body?.GetBinaryStateResponse?.BinaryState?.text()) {
        def value = body?.Body?.GetBinaryStateResponse?.BinaryState?.text().substring(0, 1).toInteger() == 0 ? "off" : "on"
        log.debug "GetBinaryResponse: BinaryState = ${value}, ${body.property.BinaryState}"
        if (device.currentValue("currentIP") == "Offline") {
            def ipvalue = convertHexToIP(getDataValue("ip"))
            sendEvent(name: "IP", value: ipvalue, descriptionText: "IP is ${ipvalue}")
        }
        def dispaux2 = device.currentValue("switch") != value
        result << createEvent(name: "switch", value: value, descriptionText: "Switch is ${value}", displayed: dispaux2)    
    }
}

private handlePropertySet(body, result) {
	if (!body?.property?.attributeList?.text())
    	return
        
	log.debug "Got propertySet"
        
    def attrListString = body?.text()
    parseAndProcessAttributeList(attrListString, result)
}

private handleTimeZoneNotification(body, result) {
	if (!body?.property?.TimeZoneNotification?.text())
    	return
        
	log.debug "Notify: TimeZoneNotification = ${body?.property?.TimeZoneNotification?.text()}"	
}

private handleGetAttributesResponse(body, result) {
	if (!body?.Body?.GetAttributesResponse?.text())
    	return
    log.debug "Got GetAttributesResponse"
    def attrListString = body?.Body?.GetAttributesResponse?.text()
    attrListString = attrListString.replaceAll(/&lt;/, '<')
        .replaceAll(/&gt;/, '>')
        .replaceAll(/&quot;/, '"')
        .replaceAll(/&apos;/, "'")
        .replaceAll(/&amp;/, '&')
    
    parseAndProcessAttributeList(attrListString, result)
}

private parseAndProcessAttributeList(listString, result) {      
    log.debug "Processing attribute list..."

    def attrList = new XmlSlurper().parseText("<attributeList>" + listString + "</attributeList>")
	processAttributeList(attrList, result)
}

private processAttributeList(list, result) {
	def values = [:]
    
    list?.attribute.each {
    	if (it.name?.text())
    		values[it.name.text()] = it.value.text()
    }
    
    def sensorPresent = device.currentValue("sensorPresent") == "on"
    
    if (values["SensorPresent"]) {
    	log.debug "SensorPresent = ${values['SensorPresent']}"
    	sensorPresent =	values["SensorPresent"] == "1"
    	result << updateSensorPresent(sensorPresent ? "on" : "off")
	}
    
    if (!sensorPresent)
    	result << updateSensor("disabled")
    else if (values["Sensor"]) {
    	log.debug "Sensor = ${values['Sensor']}"
    	def checkValue = sensorInvert ? "1" : "0"
    	def sensorValue = values["Sensor"] == checkValue ? "closed" : "open"
        result << updateSensor(sensorValue)
    }
    
    def switchMode = device.currentValue("switchMode")
    
    if (values["SwitchMode"]) {
    	log.debug "SwitchMode = ${values['SwitchMode']}"
    	switchMode = values["SwitchMode"] == "0" ? "toggle" : "momentary"
        result << updateSwitchMode(switchMode)
        
        if (switchMode == "momentary" && device.currentValue("switch") != "momentary")
        	result << updateSwitch("momentary")
    }
    
    if (values["Switch"]) {
    	log.debug "Switch = ${values['Switch']}"
        def switchValue
        
        if (switchMode == "toggle")
        	switchValue = values["Switch"] == "0" ? "off" : "on"
        else if (values["Switch"] == "0")
        	switchValue = "momentary"
        
        if (switchValue)
        	result << updateSwitch(switchValue)
    }
}

private updateSwitch(value) {
	def current = device.currentValue("switch")
	log.debug "Updating switch value: ${current} => ${value}"
	def dispaux = device.currentValue("switch") != value
    return createEvent(name: "switch", value: value, descriptionText: "Switch is ${value}", displayed: dispaux) 
}

private updateSensorPresent(value) {
	def current = device.currentValue("sensorPresent")
	log.debug "Updating sensor presence value: ${current} => ${value}"
	def descState = value ? "enabled" : "disabled"
    def dispaux = device.currentValue("sensorPresent") != value
    return createEvent(name: "sensorPresent", value: value, descriptionText: "Sensor is ${descState}", displayed: dispaux)  
}

private updateSensor(value) {
	def current = device.currentValue("contact")
	log.debug "Updating sensor value: ${current} => ${value}"
    def dispaux = device.currentValue("contact") != value
    return createEvent(name: "contact", value: value, descriptionText: "Contact is ${value}", displayed: dispaux) 
}

private updateSwitchMode(value) {
	def current = device.currentValue("switchMode")
	log.debug "Updating switch mode value: ${current} => ${value}"
    def dispaux = device.currentValue("switchMode") != value
    return createEvent(name: "switchMode", value: value, descriptionText: "Switch mode is ${value}", displayed: dispaux) 
}

// ---- /EVENTS ---- //

// ---- UTILS ---- //
private getTime() {
    // This is essentially System.currentTimeMillis()/1000, but System is disallowed by the sandbox.
    ((new GregorianCalendar().time.time / 1000l).toInteger()).toString()
}

private getCallBackAddress() {
 	device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private Integer convertHexToInt(hex) {
 	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
 	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
 	def ip = getDataValue("ip")
 	def port = getDataValue("port")
 	if (!ip || !port) {
 		def parts = device.deviceNetworkId.split(":")
 		if (parts.length == 2) {
 			ip = parts[0]
 			port = parts[1]
 		} else {
 			log.warn "Can't figure out ip and port for device: ${device.id}"
		 }
 	}
 	log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
 	return convertHexToIP(ip) + ":" + convertHexToInt(port)
}
// ---- /UTILS ---- //
