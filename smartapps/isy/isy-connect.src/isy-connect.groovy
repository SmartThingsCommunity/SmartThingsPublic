/**
 *  ISY Connect
 *
 *  Copyright 2014 Richard L. Lynch <rich@richlynch.com>
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

definition(
    name: "ISY Connect",
    namespace: "isy",
    author: "Richard L. Lynch",
    description: "ISY Insteon Connection",
    category: "SmartThings Labs",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light99-hue-v2-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light99-hue-v2-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light99-hue-v2-icn@2x.png") {
}


preferences {
    page(name:"credPage", title:"ISY Setup 1", content:"credPage")
    page(name:"isyPage", title:"ISY Setup 2", content:"isyPage")
    page(name:"nodePage", title:"ISY Setup 3", content:"nodePage")
}

// Credentials preferences page - collect ISY username and password
def credPage() {
    state.nodes = [:]
    state.devices = [:]

    return dynamicPage(name:"credPage", title:"ISY Setup 1/3", nextPage:"isyPage", install:false, uninstall: true) {
        section("ISY Authentication") {
            input "admin", "text", title: "Username"
            input "admin", "password", title: "Password"
        }
    }
}

// ISY selection page - discover and choose which ISY to control
def isyPage() {
    def refreshInterval = 5

    if(!state.subscribed) {
        log.debug('Subscribing to updates')
        // subscribe to answers from HUB
        subscribe(location, null, locationHandler, [filterEvents:false])
        state.subscribed = true
    }

    log.debug('Performing discovery')
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:udi-com:device:X_Insteon_Lighting_Device:1", physicalgraph.device.Protocol.LAN))

    def devicesForDialog = getDevicesForDialog()

    return dynamicPage(name:"isyPage", title:"ISY Setup 2/3", nextPage:"nodePage", refreshInterval: refreshInterval, install:false, uninstall: true) {
        section("Select an ISY...") {
            input "selectedISY", "enum", required:false, title:"Select ISY \n(${devicesForDialog.size() ?: 0} found)", multiple:true, options:devicesForDialog
        }
    }
}

// Returns a map of ISYs for the preference page
def getDevicesForDialog() {
    def devices = getDevices()
    def map = [:]
    devices.each {
        def value = convertHexToIP(it.value.ip)
        def key = it.value.mac
        map["${key}"] = value
    }
    map
}

// Returns a map of ISYs for internal use
def getDevices() {
    if (!state.devices) { state.devices = [:] }
    log.debug("There are ${state.devices.size()} devices at this time")
    state.devices
}

// Node selection preference page - choose which Insteon devices to control
def nodePage() {
    def refreshInterval = 5;
    def selDev = getSelectedDevice()
    def path = "/rest/nodes"
    sendHubCommand(getRequest(selDev.value.ip, selDev.value.port, path))

    def nodes = getNodes()

    return dynamicPage(name:"nodePage", title:"Node Selection", nextPage:"", refreshInterval: refreshInterval, install:true, uninstall: true) {
        section("Select nodes...") {
            input "selectedNodes", "enum", required:false, title:"Select Nodes \n(${nodes.size() ?: 0} found)", multiple:true, options:nodes
        }
    }
}

// Returns a map of Insteon nodes for the preferences page
def getSelectedDevice() {
    def selDev
    selectedISY.each { dni ->
        def devices = getDevices()
        log.debug("Looking for ${dni}")
        selDev = devices.find { it.value.mac == dni }
    }
    selDev
}

// Returns a map of Insteon nodes for internal use
def getNodes() {
    if (!state.nodes) {
        state.nodes = [:]
    }

    log.debug("There are ${state.nodes.size()} nodes at this time")
    state.nodes
}

// Handle discovery answers from ISYs (via the ST hub)
def locationHandler(evt) {
    if(evt.name == "ping") {
        return ""
    }

    log.debug('Received Response: ' + evt.description)

    def description = evt.description
    def hub = evt?.hubId
    def parsedEvent = parseDiscoveryMessage(description)
    parsedEvent << ["hub":hub]

    // Force port 80 (0x50)
    parsedEvent.port = '0050'

    if (parsedEvent?.ssdpTerm?.contains("udi-com:device:X_Insteon_Lighting_Device:1")) {
        def devices = getDevices()

        if (!(devices."${parsedEvent.ssdpUSN.toString()}")) { //if it doesn't already exist
            //log.debug('Parsed Event: ' + parsedEvent)
            devices << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
        } else { // just update the values
            def d = devices."${parsedEvent.ssdpUSN.toString()}"
            boolean deviceChangedValues = false

            if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
                d.ip = parsedEvent.ip
                d.port = parsedEvent.port
                deviceChangedValues = true
            }

            if (deviceChangedValues) {
                def children = getChildDevices()
                children.each {
                    if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
                        //it.subscribe(parsedEvent.ip, parsedEvent.port)
                    }
                }
            }

        }
    }
    else if (parsedEvent.body != null) {
        def decodedBody = new String(parsedEvent.body.decodeBase64())
        //log.debug 'Decoded body: ' + decodedBody

        def xmlTop = new XmlSlurper().parseText(decodedBody)
        def xmlNodes = xmlTop.node
        //log.debug 'Nodes: ' + xmlNodes.size()
        xmlNodes.each {
            def addr = it.address.text()
            def name = it.name.text()
            //log.debug "${addr} => ${name}"
            state.nodes[addr] = name
        }
    }
}

// Called after the last preferences page is completed
def installed() {
    // remove location subscription
    unsubscribe()
    state.subscribed = false

    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

// One node needs to be selected to receive all the messages from the ISY
// This returns its Insteon address.
def getPrimaryNode(curNode) {
    if (!state.primaryNode) {
        state.primaryNode = curNode
    }
    return state.primaryNode
}

def initialize() {
    log.debug('Initializing')

    def selDev = getSelectedDevice()
    def nodes = getNodes()

    if (selDev) {
        selectedNodes.each { nodeAddr ->
            def dni

            /* First decide on the device network ID - assign one device
               the ISY address and give the other devices unique addresses */
            if (getPrimaryNode(nodeAddr) == nodeAddr) {
                // This device will receive all the updates from the ISY, and
                // will relay the updates to the other devices.
                dni = selDev.value.mac
            }
            else {
                // These devices will not directly receive any updates - the
                // primary node will have to relay updates.
                dni = selDev.value.mac + ':' + nodeAddr
            }

            def d
            d = getChildDevices()?.find {
                it.device.deviceNetworkId == dni
            }

            if (!d) {
                log.debug("Adding node ${nodeAddr} as ${dni}: ${nodes[nodeAddr]}")
                d = addChildDevice("isy", "ISY Controller", dni, selDev?.value.hub, [
                    "label": nodes[nodeAddr],
                    "data": [
                        "nodeAddr": nodeAddr,
                        "ip": selDev.value.ip,
                        "port": selDev.value.port,
                        "username": username,
                        "password": password
                    ]
                ])
            }
        }
    }
}

// Parse the various headers the ST hub adds into a map
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

// Helper function to convert hex number to integer
private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

// Helper function to convert hex IP address into decimal dotted quad format
private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

// Get decimal ip:port from hex ip and hex port
private getHostAddress(ip, port) {
    ip = convertHexToIP(ip)
    port = convertHexToInt(port)
    //log.debug "Using ip: ${ip} and port: ${port}"
    return ip + ":" + port
}

private getAuthorization() {
    def userpassascii = username + ":" + password
    "Basic " + userpassascii.encodeAsBase64().toString()
}

// Perform an HTTP GET request to the specified ip, port, and URL path
// Response will be received async in locationHandler assuming no devices
// have been created yet.
def getRequest(ip, port, path) {
    new physicalgraph.device.HubAction(
        'method': 'GET',
        'path': path,
        'headers': [
            'HOST': getHostAddress(ip, port),
            'Authorization': getAuthorization()
        ], null)
}
