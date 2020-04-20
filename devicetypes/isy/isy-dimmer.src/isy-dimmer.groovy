/**
 *  ISY Controller - Dimmer devicetype
 *
 *  Copyright 2016 Richard L. Lynch <rich@richlynch.com>
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
    definition (name: "ISY Dimmer", namespace: "isy", author: "Richard L. Lynch") {
        capability "Switch"
        capability "Switch Level"
        capability "Polling"
        capability "Refresh"
        capability "Actuator"
    }

    simulator {
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"off"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"on"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "switch"
        details (["switch", "refresh"])
    }
}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing Dev ${device.deviceNetworkId} '${description}'"

    def parsedEvent = parseDiscoveryMessage(description)
    //log.debug "Parsed event: " + parsedEvent
    //log.debug "Body: " + parsedEvent['body']
    if (parsedEvent['body'] != null) {
        def xmlText = new String(parsedEvent.body.decodeBase64())
        //log.debug 'Device Type Decoded body: ' + xmlText

        def xmlTop = new XmlSlurper().parseText(xmlText)
        def nodes = xmlTop.node
        //log.debug 'Nodes: ' + nodes.size()

        def childMap = [:]
        parent.getAllChildDevices().each { child ->
            def childNodeAddr = child.getDataValue("nodeAddr")
            childMap[childNodeAddr] = child
        }

        nodes.each { node ->
            def nodeAddr = node.attributes().id
            def status = ''

            node.property.each { prop ->
                if (prop.attributes().id == 'ST') {
                    status = prop.attributes().value
                }
            }

            if (status != '' && childMap[nodeAddr]) {
                def child = childMap[nodeAddr]

                if (child.getDataValue("nodeAddr") == nodeAddr) {
                    def value = 'on'
                    if (status == '0') {
                        value = 'off'
                    }
                    try {
                        status = status.toFloat() * 99.0 / 255.0
                        status = status.toInteger()
                    } catch (NumberFormatException ex) {
                        log.debug "Exception parsing ${status}: ${ex} (will assume device is off)"
                        status = '0'
                        value = 'off'
                    }
                    log.debug "Updating ${child.label} ${nodeAddr} to ${value}/${status}"
                    child.sendEvent(name: 'switch', value: value)

                    if (status != 0) {
                        child.sendEvent(name: 'level', value: status)
                    }
                }
            }
        }
    }
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

    //convert IP/port
    ip = convertHexToIP(ip)
    port = convertHexToInt(port)
    log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}

private getAuthorization() {
    def userpassascii = getDataValue("username") + ":" + getDataValue("password")
    "Basic " + userpassascii.encodeAsBase64().toString()
}

def getRequest(path) {
    log.debug "Sending request for ${path} from ${device.deviceNetworkId}"

    new physicalgraph.device.HubAction(
        'method': 'GET',
        'path': path,
        'headers': [
            'HOST': getHostAddress(),
            'Authorization': getAuthorization()
        ], device.deviceNetworkId)
}

// handle commands
def on() {
    def level = device.latestValue('level')
    log.debug "Executing 'on' ${level}"

    level = level.toFloat() * 255.0 / 99.0
    level = level.toInteger()
    sendEvent(name: 'switch', value: 'on')
    def node = getDataValue("nodeAddr").replaceAll(" ", "%20")
    def path = "/rest/nodes/${node}/cmd/DON/${level}"
    getRequest(path)
}

def off() {
    log.debug "Executing 'off'"

    sendEvent(name: 'switch', value: 'off')
    def node = getDataValue("nodeAddr").replaceAll(" ", "%20")
    def path = "/rest/nodes/${node}/cmd/DOF"
    getRequest(path)
}

def setLevel(value) {
    log.debug "Executing dim ${value}"

    sendEvent(name: 'switch', value: 'on')
    sendEvent(name: 'level', value: value)

    value = value.toFloat() * 255.0 / 99.0
    value = value.toInteger()
    if (value > 255) {
        value = 255
    }
    def node = getDataValue("nodeAddr").replaceAll(" ", "%20")
    def path = "/rest/nodes/${node}/set/DON/${value}"
    getRequest(path)
}

def poll() {
    if (!device.deviceNetworkId.contains(':')) {
        log.debug "Executing 'poll' from ${device.deviceNetworkId}"

        def path = "/rest/status"
        getRequest(path)
    }
    else {
        log.debug "Ignoring poll request for ${device.deviceNetworkId}"
    }
}

def refresh() {
    log.debug "Executing 'refresh'"

    def path = "/rest/status"
    getRequest(path)
}

private def parseDiscoveryMessage(String description) {
    def device = [:]
    def parts = description.split(',')
    parts.each { part ->
        part = part.trim()
        if (part.startsWith('devicetype:')) {
            def valueString = part.split(":")[1].trim()
            device.devicetype = valueString
        } else if (part.startsWith('mac:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.mac = valueString
            }
        } else if (part.startsWith('networkAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.ip = valueString
            }
        } else if (part.startsWith('deviceAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.port = valueString
            }
        } else if (part.startsWith('ssdpPath:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.ssdpPath = valueString
            }
        } else if (part.startsWith('ssdpUSN:')) {
            part -= "ssdpUSN:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpUSN = valueString
            }
        } else if (part.startsWith('ssdpTerm:')) {
            part -= "ssdpTerm:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpTerm = valueString
            }
        } else if (part.startsWith('headers')) {
            part -= "headers:"
            def valueString = part.trim()
            if (valueString) {
                device.headers = valueString
            }
        } else if (part.startsWith('body')) {
            part -= "body:"
            def valueString = part.trim()
            if (valueString) {
                device.body = valueString
            }
        }
    }

    device
}