/**
 *  Copyright 2015 SmartThings
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
 *	Wemo Switch
 *
 *	Author: superuser
 *	Date: 2013-10-11
 */
 metadata {
	definition (name: "Wemo Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "subscribe"
		command "resubscribe"
		command "unsubscribe"
	}

	// simulator metadata
	simulator {}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details (["switch", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

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
		def body = new XmlSlurper().parseText(bodyString)

		if (body?.property?.TimeSyncRequest?.text()) {
			log.trace "Got TimeSyncRequest"
			result << timeSyncResponse()
		} else if (body?.Body?.SetBinaryStateResponse?.BinaryState?.text()) {
			log.trace "Got SetBinaryStateResponse = ${body?.Body?.SetBinaryStateResponse?.BinaryState?.text()}"
		} else if (body?.property?.BinaryState?.text()) {
			def value = body?.property?.BinaryState?.text().toInteger() == 1 ? "on" : "off"
			log.trace "Notify: BinaryState = ${value}"
			result << createEvent(name: "switch", value: value)
		} else if (body?.property?.TimeZoneNotification?.text()) {
			log.debug "Notify: TimeZoneNotification = ${body?.property?.TimeZoneNotification?.text()}"
		} else if (body?.Body?.GetBinaryStateResponse?.BinaryState?.text()) {
			def value = body?.Body?.GetBinaryStateResponse?.BinaryState?.text().toInteger() == 1 ? "on" : "off"
			log.trace "GetBinaryResponse: BinaryState = ${value}"
			result << createEvent(name: "switch", value: value)
		}
	}

	result
}

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


def on() {
	log.debug "Executing 'on'"
    sendEvent(name: "switch", value: "on")
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
	log.debug "Executing 'off'"
	sendEvent(name: "switch", value: "off")
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

/*def refresh() {
	log.debug "Executing 'refresh'"
new physicalgraph.device.HubAction("""POST /upnp/control/basicevent1 HTTP/1.1
SOAPACTION: "urn:Belkin:service:basicevent:1#GetBinaryState"
Content-Length: 277
Content-Type: text/xml; charset="utf-8"
HOST: ${getHostAddress()}
User-Agent: CyberGarage-HTTP/1.0

<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<s:Body>
<u:GetBinaryState xmlns:u="urn:Belkin:service:basicevent:1">
</u:GetBinaryState>
</s:Body>
</s:Envelope>""", physicalgraph.device.Protocol.LAN)
}*/

def refresh() {
	log.debug "Executing WeMo Switch 'subscribe', then 'timeSyncResponse', then 'poll'"
	[subscribe(), timeSyncResponse(), poll()]
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

def subscribe(ip, port) {
	def existingIp = getDataValue("ip")
	def existingPort = getDataValue("port")
	if (ip && ip != existingIp) {
		log.debug "Updating ip from $existingIp to $ip"
		updateDataValue("ip", ip)
	}
	if (port && port != existingPort) {
		log.debug "Updating port from $existingPort to $port"
		updateDataValue("port", port)
	}

	subscribe("${ip}:${port}")
}

////////////////////////////
def resubscribe() {
log.debug "Executing 'resubscribe()'"

def sid = getDeviceDataByName("subscriptionId")

new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}
TIMEOUT: Second-5400


""", physicalgraph.device.Protocol.LAN)

}

////////////////////////////
def unsubscribe() {
def sid = getDeviceDataByName("subscriptionId")
new physicalgraph.device.HubAction("""UNSUBSCRIBE publisher path HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}


""", physicalgraph.device.Protocol.LAN)
}

////////////////////////////
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


def poll() {
log.debug "Executing 'poll'"
new physicalgraph.device.HubAction("""POST /upnp/control/basicevent1 HTTP/1.1
SOAPACTION: "urn:Belkin:service:basicevent:1#GetBinaryState"
Content-Length: 277
Content-Type: text/xml; charset="utf-8"
HOST: ${getHostAddress()}
User-Agent: CyberGarage-HTTP/1.0

<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<s:Body>
<u:GetBinaryState xmlns:u="urn:Belkin:service:basicevent:1">
</u:GetBinaryState>
</s:Body>
</s:Envelope>""", physicalgraph.device.Protocol.LAN)
}