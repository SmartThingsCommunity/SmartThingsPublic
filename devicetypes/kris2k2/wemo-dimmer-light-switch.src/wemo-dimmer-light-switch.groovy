/**
 *  WeMo Dimmer Light Switch
 *  Source: https://github.com/kris2k2/SmartThingsPublic/devicetypes/kris2k2/wemo-dimmer-light-switch.src
 * 
 *  Code derived from Nicolas Cerveaux/zzarbi work on the WeMo Insight Switch
 *  Source: https://github.com/zzarbi/smartthings
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
 */
metadata {
    // Automatically generated. Make future change here.
    definition (name: "WeMo Dimmer Light Switch", namespace: "kris2k2", author: "Kristopher Lalletti") {
		capability "Switch Level"
        capability "Actuator"
        capability "Switch"
        capability "Polling"
        capability "Refresh"

        command "subscribe"
        command "resubscribe"
        command "unsubscribe"
    }

    // simulator metadata
    simulator {}

    // UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"rich-control", type: "switch", canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "offline", label:'${name}', icon:"st.Home.home30", backgroundColor:"#cccccc"
 			}
            tileAttribute ("currentIP", key: "SECONDARY_CONTROL") {
				attributeState "currentIP", label: ''
 			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
			state "offline", label:'${name}', icon:"st.Home.home30", backgroundColor:"#cccccc"
		}

		standardTile("refresh", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}

        main(["switch"])
        details(["rich-control", "level", "refresh"])
    }
}

private debug(data){
    if(parent.appSettings.debug == "true"){
        log.debug(data)
    }
}

// parse events into attributes
def parse(String description) {
	debug("Executing parse: ${description}")

    def map = stringToMap(description)

    def headerString = ""
    if (map.headers) {
        headerString = new String(map.headers.decodeBase64())
        debug("- Headers: ${headerString}")
    }
    def result = []

    // update subscriptionId
    if (headerString.contains("SID: uuid:")) {
        def sid = (headerString =~ /SID: uuid:.*/) ? ( headerString =~ /SID: uuid:.*/)[0] : "0"
        sid -= "SID: uuid:".trim()
        debug('Update subscriptionID: '+ sid)
        updateDataValue("subscriptionId", sid)
    }

    // parse the rest of the message
    if (map.body) {
        def bodyString = new String(map.body.decodeBase64())
        def value = "off"
        def level = 0
        
        debug("- Body: ${bodyString}")

		if (bodyString.contains('<BinaryState>') == true) {
            def xmlBinaryState = (bodyString =~ /<BinaryState>(.+)<\/BinaryState>/)[ 0 ][ 1 ]
			value = xmlBinaryState.toInteger() == 1 ? "on" : "off"
            debug("- Switch Status: $value")
            result << createEvent(name: "switch", value: value)
            
		}
		if (bodyString.contains('<brightness>') == true) {
			def xmlBrightness = (bodyString =~ /<brightness>(.+)<\/brightness>/)[ 0 ][ 1 ]
			level = xmlBrightness.toInteger()
            debug("- Switch Level: $level")
            result << createEvent(name: "level", value: level)
		}
		if (bodyString.contains('<Brightness>') == true) {
			def xmlBrightness = (bodyString =~ /<Brightness>(.+)<\/Brightness>/)[ 0 ][ 1 ]
			level = xmlBrightness.toInteger()
            debug("- Switch Level: $level")
            result << createEvent(name: "level", value: level)
		}
    }

    result
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
            debug("Can't figure out ip and port for device: ${device.id}")
        }
    }

    //convert IP/port
    ip = convertHexToIP(ip)
    port = convertHexToInt(port)
    debug("Using ip: ${ip} and port: ${port} for device: ${device.id}")
    return ip + ":" + port
}

private postRequest(path, SOAPaction, body) {
    // Send  a post request
    new physicalgraph.device.HubAction([
        'method': 'POST',
        'path': path,
        'body': body,
        'headers': [
            'HOST': getHostAddress(),
            'Content-type': 'text/xml; charset=utf-8',
            'SOAPAction': "\"${SOAPaction}\""
        ]
    ], device.deviceNetworkId)
}

def poll() {
    def body = """
<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
	<s:Body>
		<u:GetBinaryState xmlns:u="urn:Belkin:service:basicevent:1">
		</u:GetBinaryState>
	</s:Body>
</s:Envelope>
"""
    postRequest('/upnp/control/basicevent1', 'urn:Belkin:service:basicevent:1#GetBinaryState', body)
}

def on() {
    def body = """
<?xml version="1.0" encoding="utf-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <SOAP-ENV:Body>
        <m:SetBinaryState xmlns:m="urn:Belkin:service:basicevent:1">
            <BinaryState>1</BinaryState>
        </m:SetBinaryState>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
"""
    postRequest('/upnp/control/basicevent1', 'urn:Belkin:service:basicevent:1#SetBinaryState', body)
}

def off() {
    def body = """
<?xml version="1.0" encoding="utf-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <SOAP-ENV:Body>
        <m:SetBinaryState xmlns:m="urn:Belkin:service:basicevent:1">
            <BinaryState>0</BinaryState>
        </m:SetBinaryState>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
"""
    postRequest('/upnp/control/basicevent1', 'urn:Belkin:service:basicevent:1#SetBinaryState', body)
}

def brightness(value) {

    def body = """
<?xml version="1.0" encoding="utf-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <SOAP-ENV:Body>
        <m:SetBinaryState xmlns:m="urn:Belkin:service:basicevent:1">
            <BinaryState>1</BinaryState>
            <brightness>${value}</brightness>
        </m:SetBinaryState>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
"""
    postRequest('/upnp/control/basicevent1', 'urn:Belkin:service:basicevent:1#SetBinaryState', body)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
    brightness(value)
}

def refresh() {
    debug("Executing WeMo Switch 'subscribe', then 'timeSyncResponse', then 'poll'")
    poll()
}

def subscribe(hostAddress) {
    debug("Executing 'subscribe()'")
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
        debug("Updating ip from $existingIp to $ip")
        updateDataValue("ip", ip)
    }
    if (port && port != existingPort) {
        debug("Updating port from $existingPort to $port")
        updateDataValue("port", port)
    }

    subscribe("${ip}:${port}")
}

def resubscribe() {
    debug("Executing 'resubscribe()'")
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