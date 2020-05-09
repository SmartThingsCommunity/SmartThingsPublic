//DEPRECATED. INTEGRATION MOVED TO SUPER LAN CONNECT

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
 *  Wemo Motion
 *
 *  Author: superuser
 *  Date: 2013-10-11
 */
 metadata {
	definition (name: "Wemo Motion", namespace: "smartthings", author: "SmartThings") {
		capability "Motion Sensor"
		capability "Refresh"
		capability "Sensor"

    	attribute "currentIP", "string"

		command "subscribe"
		command "resubscribe"
		command "unsubscribe"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	// UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"rich-control", type: "generic", canChangeIcon: true){
            tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
                 attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC"
                 attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc"
                 attributeState "offline", label:'${name}', icon:"st.motion.motion.active", backgroundColor:"#cccccc"
 			}
            tileAttribute ("currentIP", key: "SECONDARY_CONTROL") {
             	 attributeState "currentIP", label: ''
 			}
        }

		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#CCCCCC")
      		state("offline", label:'${name}', icon:"st.motion.motion.inactive", backgroundColor:"#ff0000")
		}

        standardTile("refresh", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

		main "motion"
		details (["rich-control", "refresh"])
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
    	unschedule("setOffline")
        def body = new XmlSlurper().parseText(bodyString.replaceAll("[^\\x20-\\x7e]", ""))
		if (body?.property?.TimeSyncRequest?.text()) {
			log.trace "Got TimeSyncRequest"
			result << timeSyncResponse()
		} else if (body?.Body?.SetBinaryStateResponse?.BinaryState?.text()) {
			log.trace "Got SetBinaryStateResponse = ${body?.Body?.SetBinaryStateResponse?.BinaryState?.text()}"
		} else if (body?.property?.BinaryState?.text()) {
			def value = body?.property?.BinaryState?.text().toInteger() == 1 ? "active" : "inactive"
			log.debug "Notify - BinaryState = ${value}"
      result << createEvent(name: "motion", value: value, descriptionText: "Motion is ${value}")
		} else if (body?.property?.TimeZoneNotification?.text()) {
			log.debug "Notify: TimeZoneNotification = ${body?.property?.TimeZoneNotification?.text()}"
		}
	}

	result
}

////////////////////////////
private getTime() {
	// This is essentially System.currentTimeMillis()/1000, but System is disallowed by the sandbox.
	((new GregorianCalendar().time.time / 1000l).toInteger()).toString()
}

private getCallBackAddress() {
	device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
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

////////////////////////////
def refresh() {
	log.debug "Executing WeMo Motion 'subscribe', then 'timeSyncResponse', then 'getStatus'"
	[subscribe(), timeSyncResponse(), getStatus()]
}

////////////////////////////
def getStatus() {
log.debug "Executing WeMo Motion 'getStatus'"
if (device.currentValue("currentIP") != "Offline")
    runIn(30, setOffline)
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

////////////////////////////
def subscribe(hostAddress) {
log.debug "Executing 'subscribe()'"
def address = getCallBackAddress()
new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${hostAddress}
CALLBACK: <http://${address}/>
NT: upnp:event
TIMEOUT: Second-4200
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
    	def ipvalue = convertHexToIP(getDataValue("ip"))
    	sendEvent(name: "currentIP", value: ipvalue, descriptionText: "IP changed to ${ipvalue}")
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
TIMEOUT: Second-4200


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

def setOffline() {
    sendEvent(name: "motion", value: "offline", descriptionText: "The device is offline")
}

private Integer convertHexToInt(hex) {
 	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
 	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
