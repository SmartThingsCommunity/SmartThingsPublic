/**
 *  TCP Bulbs (Connect)
 *
 *  Copyright 2014 Todd Wackford
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

import java.security.MessageDigest;

private apiUrl() { "https://tcp.greenwavereality.com/gwr/gop.php?" }

definition(
	name: "Tcp Bulbs (Connect)",
	namespace: "wackford",
	author: "SmartThings",
	description: "Connect your TCP bulbs to SmartThings using Cloud to Cloud integration. You must create a remote login acct on TCP Mobile App.",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/tcp.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/tcp@2x.png",
	singleInstance: true
)


preferences {
	def msg = """Tap 'Next' after you have entered in your TCP Mobile remote credentials.

Once your credentials are accepted, SmartThings will scan your TCP installation for Bulbs."""

	page(name: "selectDevices", title: "Connect Your TCP Lights to SmartThings", install: false, uninstall: true, nextPage: "chooseBulbs") {
		section("TCP Connected Remote Credentials") {
			input "username", "text", title: "Enter TCP Remote Email/UserName", required: true
			input "password", "password", title: "Enter TCP Remote Password", required: true
			paragraph msg
		}
	}

	page(name: "chooseBulbs", title: "Choose Bulbs to Control With SmartThings", content: "initialize")
}

def installed() {
	debugOut "Installed with settings: ${settings}"

	unschedule()
	unsubscribe()

	setupBulbs()

	def cron = "0 11 23 * * ?"
	log.debug "schedule('$cron', syncronizeDevices)"
	schedule(cron, syncronizeDevices)
}

def updated() {
	debugOut "Updated with settings: ${settings}"

	unschedule()

	setupBulbs()

	def cron = "0 11 23 * * ?"
	log.debug "schedule('$cron', syncronizeDevices)"
	schedule(cron, syncronizeDevices)
}

def uninstalled()
{
	unschedule() //in case we have hanging runIn()'s
}

private removeChildDevices(delete)
{
	debugOut "deleting ${delete.size()} bulbs"
	debugOut "deleting ${delete}"
	delete.each {
		deleteChildDevice(it.device.deviceNetworkId)
	}
}

def uninstallFromChildDevice(childDevice)
{
	def errorMsg = "uninstallFromChildDevice was called and "
	if (!settings.selectedBulbs) {
		debugOut errorMsg += "had empty list passed in"
		return
	}

	def dni = childDevice.device.deviceNetworkId

	if ( !dni ) {
		debugOut errorMsg += "could not find dni of device"
		return
	}

	def newDeviceList = settings.selectedBulbs - dni
	app.updateSetting("selectedBulbs", newDeviceList)

	debugOut errorMsg += "completed succesfully"
}


def setupBulbs() {
	debugOut "In setupBulbs"

	def bulbs = state.devices
	def deviceFile = "TCP Bulb"

	selectedBulbs.each { did ->
		//see if this is a selected bulb and install it if not already
		def d = getChildDevice(did)

		if(!d) {
			def newBulb = bulbs.find { (it.did) == did }
			d = addChildDevice("wackford", deviceFile, did, null, [name: "${newBulb?.name}", label: "${newBulb?.name}", completedSetup: true])

			/*if ( isRoom(did) ) { //change to the multi light group icon for a room device
				d.setIcon("switch", "on",  "st.lights.multi-light-bulb-on")
				d.setIcon("switch", "off",  "st.lights.multi-light-bulb-off")
				d.save()
			}*/

		} else {
			debugOut "We already added this device"
		}
	}

	// Delete any that are no longer in settings
	def delete = getChildDevices().findAll { !selectedBulbs?.contains(it.deviceNetworkId) }
	removeChildDevices(delete)

	//we want to ensure syncronization between rooms and bulbs
	//syncronizeDevices()
}

def initialize() {

	atomicState.token = ""

	getToken()

	if ( atomicState.token == "error" ) {
		return dynamicPage(name:"chooseBulbs", title:"TCP Login Failed!\r\nTap 'Done' to try again", nextPage:"", install:false, uninstall: false) {
			section("") {}
		}
	} else {
		"we're good to go"
		debugOut "We have Token."
	}

	//getGatewayData() //we really don't need anything from the gateway

	deviceDiscovery()

	def options = devicesDiscovered() ?: []

	def msg = """Tap 'Done' after you have selected the desired devices."""

	return dynamicPage(name:"chooseBulbs", title:"TCP and SmartThings Connected!", nextPage:"", install:true, uninstall: true) {
		section("Tap Below to View Device List") {
			input "selectedBulbs", "enum", required:false, title:"Select Bulb/Fixture", multiple:true, options:options
			paragraph msg
		}
	}
}

def deviceDiscovery() {
	def data = "<gip><version>1</version><token>${atomicState.token}</token></gip>"

	def Params = [
		cmd: "RoomGetCarousel",
		data: "${data}",
		fmt: "json"
	]

	def cmd = toQueryString(Params)

	def rooms = ""

	apiPost(cmd) { response ->
		rooms = response.data.gip.room
	}

	debugOut "rooms data = ${rooms}"

	def devices = []
	def bulbIndex = 1
	def lastRoomName = null
	def deviceList = []

	if ( rooms[1] == null ) {
		def roomId = rooms.rid
		def roomName = rooms.name
		devices  = rooms.device
		if ( devices[1] != null ) {
			debugOut "Room Device Data: did:${roomId} roomName:${roomName}"
			//deviceList += ["name" : "${roomName}", "did" : "${roomId}", "type" : "room"]
			devices.each({
				debugOut "Bulb Device Data: did:${it?.did} room:${roomName} BulbName:${it?.name}"
				deviceList += ["name" : "${roomName} ${it?.name}", "did" : "${it?.did}", "type" : "bulb"]
			})
		} else {
			debugOut "Bulb Device Data: did:${it?.did} room:${roomName} BulbName:${it?.name}"
			deviceList += ["name" : "${roomName} ${it?.name}", "did" : "${it?.did}", "type" : "bulb"]
		}
	} else {
		rooms.each({
			devices  = it.device
			def roomName = it.name
			if ( devices[1] != null ) {
				def roomId = it?.rid
				debugOut "Room Device Data: did:${roomId} roomName:${roomName}"
				//deviceList += ["name" : "${roomName}", "did" : "${roomId}", "type" : "room"]
				devices.each({
					debugOut "Bulb Device Data: did:${it?.did} room:${roomName} BulbName:${it?.name}"
					deviceList += ["name" : "${roomName} ${it?.name}", "did" : "${it?.did}", "type" : "bulb"]
				})
			} else {
				debugOut "Bulb Device Data: did:${devices?.did} room:${roomName} BulbName:${devices?.name}"
				deviceList += ["name" : "${roomName} ${devices?.name}", "did" : "${devices?.did}", "type" : "bulb"]
			}
		})
	}
	devices = ["devices" : deviceList]
	state.devices = devices.devices
}

Map devicesDiscovered() {
	def devices =  state.devices
	def map = [:]
	if (devices instanceof java.util.Map) {
		devices.each {
			def value = "${it?.name}"
			def key = it?.did
			map["${key}"] = value
		}
	} else { //backwards compatable
		devices.each {
			def value = "${it?.name}"
			def key = it?.did
			map["${key}"] = value
		}
	}
	map
}

def getGatewayData() {
	debugOut "In getGatewayData"

	def data = "<gip><version>1</version><token>${atomicState.token}</token></gip>"

	def qParams = [
		cmd: "GatewayGetInfo",
		data: "${data}",
		fmt: "json"
	]

	def cmd = toQueryString(qParams)

	apiPost(cmd) { response ->
		debugOut "the gateway reponse is ${response.data.gip.gateway}"
	}

}

def getToken() {

	atomicState.token = ""

	if (password) {
		def hashedPassword = generateMD5(password)

		def data = "<gip><version>1</version><email>${username}</email><password>${hashedPassword}</password></gip>"

		def qParams = [
			cmd : "GWRLogin",
			data: "${data}",
			fmt : "json"
		]

		def cmd = toQueryString(qParams)

		apiPost(cmd) { response ->
			def status = response.data.gip.rc

			//sendNotificationEvent("Get token status ${status}")

			if (status != "200") {//success code = 200
				def errorText = response.data.gip.error
				debugOut "Error logging into TCP Gateway. Error = ${errorText}"
				atomicState.token = "error"
			} else {
				atomicState.token = response.data.gip.token
			}
		}
	} else {
		log.warn "Unable to log into TCP Gateway. Error = Password is null"
		atomicState.token = "error"
	}
}

def apiPost(String data, Closure callback) {
	//debugOut "In apiPost with data: ${data}"
	def params = [
		uri: apiUrl(),
		body: data
	]

	httpPost(params) {
		response ->
			def rc = response.data.gip.rc

			if ( rc == "200" ) {
				debugOut ("Return Code = ${rc} = Command Succeeded.")
				callback.call(response)

			} else if ( rc == "401" ) {
				debugOut "Return Code = ${rc} = Error: User not logged in!" //Error code from gateway
				log.debug "Refreshing Token"
				getToken()
				//callback.call(response) //stubbed out so getToken works (we had race issue)

			} else {
				log.error "Return Code = ${rc} = Error!" //Error code from gateway
				sendNotificationEvent("TCP Lighting is having Communication Errors. Error code = ${rc}. Check that TCP Gateway is online")
				callback.call(response)
			}
	}
}


//this is not working. TCP power reporting is broken. Leave it here for future fix
def calculateCurrentPowerUse(deviceCapability, usePercentage) {
	debugOut "In calculateCurrentPowerUse()"

	debugOut "deviceCapability: ${deviceCapability}"
	debugOut "usePercentage: ${usePercentage}"

	def calcPower = usePercentage * 1000
	def reportPower = calcPower.round(1) as String

	debugOut "report power = ${reportPower}"

	return reportPower
}

def generateSha256(String s) {

	MessageDigest digest = MessageDigest.getInstance("SHA-256")
	digest.update(s.bytes)
	new BigInteger(1, digest.digest()).toString(16).padLeft(40, '0')
}

def generateMD5(String s) {
	MessageDigest digest = MessageDigest.getInstance("MD5")
	digest.update(s.bytes);
	new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def checkDevicesOnline(bulbs) {
	debugOut "In checkDevicesOnline()"

	def onlineBulbs = []
	def thisBulb = []

	bulbs.each {
		def dni = it?.did
		thisBulb = it

		def data = "<gip><version>1</version><token>${atomicState.token}</token><did>${dni}</did></gip>"

		def qParams = [
			cmd: "DeviceGetInfo",
			data: "${data}",
			fmt: "json"
		]

		def cmd = toQueryString(qParams)

		def bulbData = []

		apiPost(cmd) { response ->
			bulbData = response.data.gip
		}

		if ( bulbData?.offline == "1" ) {
			debugOut "${it?.name} is offline with offline value of ${bulbData?.offline}"

		} else {
			debugOut "${it?.name} is online with offline value of ${bulbData?.offline}"
			onlineBulbs += thisBulb
		}
	}
	return onlineBulbs
}

def syncronizeDevices() {
	debugOut "In syncronizeDevices"

	def update = getChildDevices().findAll { selectedBulbs?.contains(it.deviceNetworkId) }

	update.each {
		def dni = getChildDevice( it.deviceNetworkId )
		debugOut "dni = ${dni}"

		if (isRoom(dni)) {
			pollRoom(dni)
		} else {
			poll(dni)
		}
	}
}

boolean isRoom(dni) {
	def device = state.devices.find() {(( it.type == 'room') && (it.did == "${dni}"))}
}

boolean isBulb(dni) {
	def device = state.devices.find() {(( it.type == 'bulb') && (it.did == "${dni}"))}
}

def debugEvent(message, displayEvent) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)

}

def debugOut(msg) {
	//log.debug msg
	//sendNotificationEvent(msg) //Uncomment this for troubleshooting only
}


/**************************************************************************
 Child Device Call In Methods
 **************************************************************************/
def on(childDevice) {
	debugOut "On request from child device"

	def dni = childDevice.device.deviceNetworkId
	def data = ""
	def cmd = ""

	if ( isRoom(dni) ) { // this is a room, not a bulb
		data = "<gip><version>1</version><token>$atomicState.token</token><rid>${dni}</rid><type>power</type><value>1</value></gip>"
		cmd = "RoomSendCommand"
	} else {
		data = "<gip><version>1</version><token>$atomicState.token</token><did>${dni}</did><type>power</type><value>1</value></gip>"
		cmd = "DeviceSendCommand"
	}

	def qParams = [
		cmd: cmd,
		data: "${data}",
		fmt: "json"
	]

	cmd = toQueryString(qParams)

	apiPost(cmd) { response ->
		debugOut "ON result: ${response.data}"
	}

	//we want to ensure syncronization between rooms and bulbs
	//runIn(2, "syncronizeDevices")
}

def off(childDevice) {
	debugOut "Off request from child device"

	def dni = childDevice.device.deviceNetworkId
	def data = ""
	def cmd = ""

	if ( isRoom(dni) ) { // this is a room, not a bulb
		data = "<gip><version>1</version><token>$atomicState.token</token><rid>${dni}</rid><type>power</type><value>0</value></gip>"
		cmd = "RoomSendCommand"
	} else {
		data = "<gip><version>1</version><token>$atomicState.token</token><did>${dni}</did><type>power</type><value>0</value></gip>"
		cmd = "DeviceSendCommand"
	}

	def qParams = [
		cmd: cmd,
		data: "${data}",
		fmt: "json"
	]

	cmd = toQueryString(qParams)

	apiPost(cmd) { response ->
		debugOut "${response.data}"
	}

	//we want to ensure syncronization between rooms and bulbs
	//runIn(2, "syncronizeDevices")
}

def setLevel(childDevice, value) {
	debugOut "setLevel request from child device"

	def dni = childDevice.device.deviceNetworkId
	def data = ""
	def cmd = ""

	if ( isRoom(dni) ) { // this is a room, not a bulb
		data = "<gip><version>1</version><token>${atomicState.token}</token><rid>${dni}</rid><type>level</type><value>${value}</value></gip>"
		cmd = "RoomSendCommand"
	} else {
		data = "<gip><version>1</version><token>${atomicState.token}</token><did>${dni}</did><type>level</type><value>${value}</value></gip>"
		cmd = "DeviceSendCommand"
	}

	def qParams = [
		cmd: cmd,
		data: "${data}",
		fmt: "json"
	]

	cmd = toQueryString(qParams)

	apiPost(cmd) { response ->
		debugOut "${response.data}"
	}

	//we want to ensure syncronization between rooms and bulbs
	//runIn(2, "syncronizeDevices")
}

// Really not called from child, but called from poll() if it is a room
def pollRoom(dni) {
	debugOut "In pollRoom"
	def data = ""
	def cmd = ""
	def roomDeviceData = []

	data = "<gip><version>1</version><token>${atomicState.token}</token><rid>${dni}</rid><fields>name,power,control,status,state</fields></gip>"
	cmd = "RoomGetDevices"

	def qParams = [
		cmd: cmd,
		data: "${data}",
		fmt: "json"
	]

	cmd = toQueryString(qParams)

	apiPost(cmd) { response ->
		roomDeviceData = response.data.gip
	}

	debugOut "Room Data: ${roomDeviceData}"

	def totalPower = 0
	def totalLevel = 0
	def cnt = 0
	def onCnt = 0 //used to tally on/off states

	roomDeviceData.device.each({
		if ( getChildDevice(it.did) ) {
			totalPower += it.other.bulbpower.toInteger()
			totalLevel += it.level.toInteger()
			onCnt += it.state.toInteger()
			cnt += 1
		}
	})

	def avgLevel = totalLevel/cnt
	def usingPower = totalPower * (avgLevel / 100) as float
	def room = getChildDevice( dni )

	//the device is a room but we use same type file
	sendEvent( dni, [name: "setBulbPower",value:"${totalPower}"] ) //used in child device calcs

	//if all devices in room are on, room is on
	if ( cnt == onCnt ) { // all devices are on
		sendEvent( dni, [name: "switch",value:"on"] )
		sendEvent( dni, [name: "power",value:usingPower.round(1)] )

	} else { //if any device in room is off, room is off
		sendEvent( dni, [name: "switch",value:"off"] )
		sendEvent( dni, [name: "power",value:0.0] )
	}

	debugOut "Room Using Power: ${usingPower.round(1)}"
}

def poll(childDevice) {
	debugOut "In poll() with ${childDevice}"


	def dni = childDevice.device.deviceNetworkId

	def bulbData = []
	def data = ""
	def cmd = ""

	if ( isRoom(dni) ) { // this is a room, not a bulb
		pollRoom(dni)
		return
	}

	data = "<gip><version>1</version><token>${atomicState.token}</token><did>${dni}</did></gip>"
	cmd = "DeviceGetInfo"

	def qParams = [
		cmd: cmd,
		data: "${data}",
		fmt: "json"
	]

	cmd = toQueryString(qParams)

	apiPost(cmd) { response ->
		bulbData = response.data.gip
	}

	debugOut "This Bulbs Data Return = ${bulbData}"

	def bulb = getChildDevice( dni )

	//set the devices power max setting to do calcs within the device type
	if ( bulbData.other.bulbpower )
		sendEvent( dni, [name: "setBulbPower",value:"${bulbData.other.bulbpower}"] )

	if (( bulbData.state == "1" ) && ( bulb?.currentValue("switch") != "on" ))
		sendEvent( dni, [name: "switch",value:"on"] )

	if (( bulbData.state == "0" ) && ( bulb?.currentValue("switch") != "off" ))
		sendEvent( dni, [name: "switch",value:"off"] )

	//if ( bulbData.level != bulb?.currentValue("level")) {
	//	sendEvent( dni, [name: "level",value: "${bulbData.level}"] )
	//    sendEvent( dni, [name: "setLevel",value: "${bulbData.level}"] )
	//}

	if (( bulbData.state == "1" ) && ( bulbData.other.bulbpower )) {
		def levelSetting = bulbData.level as float
		def bulbPowerMax = bulbData.other.bulbpower as float
		def calculatedPower = bulbPowerMax * (levelSetting / 100)
		sendEvent( dni, [name: "power", value: calculatedPower.round(1)] )
	}

	if (( bulbData.state == "0" ) && ( bulbData.other.bulbpower ))
		sendEvent( dni, [name: "power", value: 0.0] )
}
