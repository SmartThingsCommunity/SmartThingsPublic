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
 *  Foscam (connect)
 *
 *  Author: smartthings
 *  Date: 2014-03-10
 */

definition(
    name: "Foscam (Connect)",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Connect and take pictures using your Foscam camera from inside the Smartthings app.",
    category: "SmartThings Internal",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/foscam.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/foscam@2x.png",
    singleInstance: true
)

preferences {
	page(name: "cameraDiscovery", title:"Foscam Camera Setup", content:"cameraDiscovery")
	page(name: "loginToFoscam", title: "Foscam Login")
}

//PAGES
/////////////////////////////////////
def cameraDiscovery()
{
	if(canInstallLabs())
	{
		int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
		state.refreshCount = refreshCount + 1
		def refreshInterval = 3

		def options = camerasDiscovered() ?: []
		def numFound = options.size() ?: 0

		if(!state.subscribe) {
			subscribe(location, null, locationHandler, [filterEvents:false])
			state.subscribe = true
		}

		//bridge discovery request every
		if((refreshCount % 5) == 0) {
			discoverCameras()
		}

		return dynamicPage(name:"cameraDiscovery", title:"Discovery Started!", nextPage:"loginToFoscam", refreshInterval:refreshInterval, uninstall: true) {
			section("Please wait while we discover your Foscam. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
				input "selectedFoscam", "enum", required:false, title:"Select Foscam (${numFound} found)", multiple:true, options:options
			}
		}
	}
	else
	{
		def upgradeNeeded = """To use Foscam, your Hub should be completely up to date.

		To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

		return dynamicPage(name:"cameraDiscovery", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
			section("Upgrade") {
				paragraph "$upgradeNeeded"
			}
		}

	}
}

def loginToFoscam() {
	def showUninstall = username != null && password != null
	return dynamicPage(name: "loginToFoscam", title: "Foscam", uninstall:showUninstall, install:true,) {
		section("Log in to Foscam") {
			input "username", "text", title: "Username", required: true, autoCorrect:false
			input "password", "password", title: "Password", required: true, autoCorrect:false
		}
	}
}
//END PAGES

/////////////////////////////////////
private discoverCameras()
{
	//add type UDP_CLIENT
	def action = new physicalgraph.device.HubAction("0b4D4F5F490000000000000000000000040000000400000000000001", physicalgraph.device.Protocol.LAN, "FFFFFFFF:2710")
	action.options = [type:"LAN_TYPE_UDPCLIENT"]
	sendHubCommand(action)
}

def camerasDiscovered() {
	def cameras = getCameras()
	def map = [:]
	cameras.each {
		def value = it.value.name ?: "Foscam Camera"
		def key = it.value.ip + ":" + it.value.port
		map["${key}"] = value
	}
	map
}

/////////////////////////////////////
def getCameras()
{
	state.cameras = state.cameras ?: [:]
}

/////////////////////////////////////
def installed() {
	//log.debug "Installed with settings: ${settings}"
	initialize()

	runIn(300, "doDeviceSync" , [overwrite: false]) //setup ip:port syncing every 5 minutes

	//wait 5 seconds and get the deviceInfo
	//log.info "calling 'getDeviceInfo()'"
	//runIn(5, getDeviceInfo)
}

/////////////////////////////////////
def updated() {
	//log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

/////////////////////////////////////
def initialize() {
	// remove location subscription aftwards
	unsubscribe()
	state.subscribe = false

	if (selectedFoscam)
	{
		addCameras()
	}
}

def addCameras() {
	def cameras = getCameras()

	selectedFoscam.each { dni ->
		def d = getChildDevice(dni)

		if(!d)
		{
			def newFoscam = cameras.find { (it.value.ip + ":" + it.value.port) == dni }
			d = addChildDevice("smartthings", "Foscam", dni, newFoscam?.value?.hub, ["label":newFoscam?.value?.name ?: "Foscam Camera", "data":["mac": newFoscam?.value?.mac, "ip": newFoscam.value.ip, "port":newFoscam.value.port], "preferences":["username":username, "password":password]])

			log.debug "created ${d.displayName} with id $dni"
		}
		else
		{
			log.debug "found ${d.displayName} with id $dni already exists"
		}
	}
}

def getDeviceInfo() {
	def devices = getAllChildDevices()
	devices.each { d ->
		d.getDeviceInfo()
	}
}

/////////////////////////////////////
def locationHandler(evt) {
	/*
	FOSCAM EXAMPLE
	4D4F5F4901000000000000000000006200000000000000 (SOF) //46
	30303632364534443042344200 (mac) //26
	466F7363616D5F44617274684D61756C0000000000 (name) //42
	0A01652C (ip) //8
	FFFFFE00 (mask) //8
	00000000 (gateway ip) //8
	00000000 (dns) //8
	01005800 (reserve) //8
	01040108 (system software version) //8
	020B0106 (app software version) //8
	0058 (port) //4
	01 (dhcp enabled) //2
	*/
	def description = evt.description
	def hub = evt?.hubId

	log.debug "GOT LOCATION EVT: $description"

	def parsedEvent = stringToMap(description)

	//FOSCAM does a UDP response with camera operate protocol:“MO_I” i.e. "4D4F5F49"
	if (parsedEvent?.type == "LAN_TYPE_UDPCLIENT" && parsedEvent?.payload?.startsWith("4D4F5F49"))
	{
		def unpacked = [:]
		unpacked.mac = parsedEvent.mac.toString()
		unpacked.name = hexToString(parsedEvent.payload[72..113]).trim()
		unpacked.ip = parsedEvent.payload[114..121]
		unpacked.subnet = parsedEvent.payload[122..129]
		unpacked.gateway = parsedEvent.payload[130..137]
		unpacked.dns = parsedEvent.payload[138..145]
		unpacked.reserve = parsedEvent.payload[146..153]
		unpacked.sysVersion = parsedEvent.payload[154..161]
		unpacked.appVersion = parsedEvent.payload[162..169]
		unpacked.port = parsedEvent.payload[170..173]
		unpacked.dhcp = parsedEvent.payload[174..175]
		unpacked.hub = hub

		def cameras = getCameras()
		if (!(cameras."${parsedEvent.mac.toString()}"))
		{
			cameras << [("${parsedEvent.mac.toString()}"):unpacked]
		}
	}
}

/////////////////////////////////////
private Boolean canInstallLabs()
{
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}

private String hexToString(String txtInHex)
{
	byte [] txtInByte = new byte [txtInHex.length() / 2];
	int j = 0;
	for (int i = 0; i < txtInHex.length(); i += 2)
	{
			txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
	}
	return new String(txtInByte);
}
