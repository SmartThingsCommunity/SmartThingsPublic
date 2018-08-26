/**
 *  Leviosa Shades Smart App
 *
 */
definition(
		name: "Leviosa WiShadeController Service Manager",
		namespace: "atlascoder",
		author: "Leviosashades",
		description: "A Controller that can manage motorized shade from mobile App in Home or via Internet",
		category: "My apps",
		iconUrl: "https://s3.us-east-2.amazonaws.com/leviosa-pub/Icon-48.png",
		iconX2Url: "https://s3.us-east-2.amazonaws.com/leviosa-pub/Icon-88.png",
		iconX3Url: "https://s3.us-east-2.amazonaws.com/leviosa-pub/Icon-172.png"
	)


preferences {
	page(name: "searchTargetSelection", title: "Shade Groups Setup", nextPage: "deviceDiscovery") {
		section("You are starting to connect your Leviosa Motor Shades managed through wiSHadeControllers. Your wiShadesController(s) must be setup correctly before to start to discover and connect them.") {}
		section("Your wiShadesController(s) must be setup correctly before to start to discover and connect them.")
	}
	page(name: "deviceDiscovery", title: "Shade Groups Discovery", content: "deviceDiscovery")
}

def getSelectedDevices() {
	def list = []
	def devices = getVerifiedDevices()
	devices.each {
		list << it.value.ssdpUSN.split(':')[1]
	}
    list
}

def deviceDiscovery() {

	state.searchTarget = "urn:schemas-upnp-org:service:TwoWayMotor:1"
    
	log.debug "Discovery start for ${state.searchTarget}"

	ssdpSubscribe()

	ssdpDiscover()
	verifyDevices()

	return dynamicPage(name: "deviceDiscovery", title: "Discovery Started!", nextPage: "", refreshInterval: 5, install: true, uninstall: true) {
		section("Please wait while we discover your shade groups. Discovery can take five minutes or more, so sit back and relax!") {
        	def l = getSelectedDevices().size()
            if (l == 0) {
        		paragraph "No controllers discovered yet.."
            }
        	else if (l == 1) {
        		paragraph "1 shade group discovered."
            paragraph "Every controller has 6 groups, please wait.."
            }
            else if (l < 6) {
        		paragraph "${l} shade groups discovered."
	            paragraph "Every controller has 6 groups, please wait.."
            }
            else if (l == 6) {
        		paragraph "6 shade groups discovered."
	            paragraph "If you have only 1 controller - you have to finish setup pressing Save. Otherwise - please wait.."
            }
            else {
        		paragraph "${l} shade groups discovered."
	            paragraph "Every controller has 6 groups, please, decide yourself when stop to discover."
            }
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {

	unsubscribe()
	unschedule()

	ssdpSubscribe()
    
    def selectedDevices = getSelectedDevices()

	log.debug "Selected devices: ${selectedDevices}"

	if (selectedDevices.size() > 0) {
		addDevices()
	}

	runEvery5Minutes("ssdpDiscover")
}

void ssdpDiscover() {
	log.debug "lan discovery ${state.searchTarget}"
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery ${state.searchTarget}", physicalgraph.device.Protocol.LAN))
}

void ssdpSubscribe() {
	log.debug "subscribing to ${state.searchTarget}"
	subscribe(location, "ssdpTerm.${state.searchTarget}", ssdpHandler)
}

Map verifiedDevices() {
	def devices = getVerifiedDevices()
	def map = [:]
	devices.each {
		def key = it.value.ssdpUSN.split(':')[1]
		def value = it.value.name ?: "Group ${key}"
		map["${key}"] = value
	}
	map
}

void verifyDevices() {
	def devices = getDevices().findAll { it?.value?.verified != true }
	devices.each {
		int port = convertHexToInt(it.value.deviceAddress)
		String ip = convertHexToIP(it.value.networkAddress)
		String host = "${ip}:${port}"
        log.debug "Requesting http://${host}${it.value.ssdpPath}"
		sendHubCommand(new physicalgraph.device.HubAction("GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n", physicalgraph.device.Protocol.LAN, host, [callback: deviceDescriptionHandler]))
	}
}

def getVerifiedDevices() {
	getDevices().findAll{ it.value.verified == true }
}

def getDevices() {
	if (!state.devices) {
		state.devices = [:]
	}
	state.devices
}

def addDevices() {
	def devices = getDevices()
    
    log.debug "Current devices: ${devices.toString()}"
    log.debug "Selected devices: ${selectedDevices}"
    
    def selDevs = []
    
    def selectedDevices = getSelectedDevices()
    
    if (selectedDevices instanceof String) {
        selDevs << selectedDevices
    }
    else {
    	selDevs += selectedDevices
    }

	log.debug "Selected devices: ${selDevs}"

	selDevs.each { dni ->
    	log.debug "dni = ${dni}"
		def selectedDevice = devices.find { it.value.ssdpUSN.split(':')[1] == dni }
		def d
		if (selectedDevice) {
			d = getChildDevices()?.find {
				it.deviceNetworkId == selectedDevice.value.ssdpUSN.split(':')[1]
			}
            
		}

		log.debug "selectedDevice => ${selectedDevice}"

		if (!d) {
			log.debug "Creating Window Blind Device with dni: ${dni}"
			addChildDevice("atlascoder", "LeviosaController", dni, selectedDevice?.value.hub, [
				"label": selectedDevice?.value?.name ?: "LeviosaController",
				"data": [
					"mac": selectedDevice.value.mac,
					"ip": selectedDevice.value.networkAddress,
					"port": selectedDevice.value.deviceAddress,
                    "dni": dni
				]
			])
		}
	}
}

def ssdpHandler(evt) {
	def description = evt.description
	def hub = evt?.hubId

	def parsedEvent = parseLanMessage(description)
	parsedEvent << ["hub":hub]

	def devices = getDevices()
	String ssdpUSN = parsedEvent.ssdpUSN.toString()
    def uuid = ssdpUSN.split(':')[1]
    log.debug "Response from uuid:${uuid}"
	if (devices."${uuid}") {
		def d = devices."${uuid}"
        log.debug "Update uuid:${uuid} => ${d}"
		if (d.networkAddress != parsedEvent.networkAddress || d.deviceAddress != parsedEvent.deviceAddress) {
			d.networkAddress = parsedEvent.networkAddress
			d.deviceAddress = parsedEvent.deviceAddress
			def child = getChildDevice(uuid)
			if (child) {
				child.sync(parsedEvent.networkAddress, parsedEvent.deviceAddress)
			}
		}
	} else {
        log.debug "Add uuid:${uuid} => ${parsedEvent}"
		devices << ["${uuid}" : parsedEvent]
	}
}

void deviceDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
	def body = hubResponse.xml
	def devices = getDevices()
	def device = devices.find { it?.key == body?.device?.UDN?.toString().split(':')[1] }
    log.debug "Device by ${body?.device?.UDN?.toString().split(':')[1]} = > ${device}"
	if (device) {
    	log.debug "Update device with name: ${body?.device?.friendlyName?.text()}"
		device.value << [name: body?.device?.friendlyName?.text(), model: body?.device?.modelName?.text(), serialNumber: body?.device?.serialNum?.text(), verified: true]
	}
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}