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

 */



definition(

	name: "Tcp Bulbs (Connect)",

	namespace: "mujica",

	author: "SmartThings-Ule",

	description: "Connect your TCP bulbs to SmartThings using local integration. You must have a geteway with firmware ver 2",

	category: "SmartThings Labs",

	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/tcp.png",

	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/tcp@2x.png",

	singleInstance: true

)





preferences {

	page(name: "iniSettings", title: "Connect Your TCP Lights to SmartThings", content: "iniSettings")

	page(name: "chooseBulbs", title: "Choose Bulbs to Control With SmartThings", content: "bulbDiscovery")

}



def iniSettings(){

	state.loadStatus = "Inactive"

    log.trace "state.loadStatus ${state.loadStatus}"

    return dynamicPage(name:"iniSettings", title:"Connect Your TCP Lights to SmartThings", nextPage:"chooseBulbs", install:false, uninstall: true) {

       section("TCP Connected Remote Credentials") {

			input "ipGateway", "text", title: "Enter TCP Gateway IP", required: true

			paragraph "Tap 'Next' after you have entered the ip of your TCP Connected Gateway.\r\n\r\nOnce your ip are accepted, SmartThings will scan your TCP installation for Bulbs."

		}

    }

}



def bulbDiscovery() {

	debugOut "bulbDiscovery()"

	//getToken()

	state.token = "1234567890"

    

    if (state.loadStatus == "Inactive"){

    	state.count = 0

    	state.loadStatus = "Loading"

        log.trace "state.loadStatus ${state.loadStatus}"

    	deviceDiscovery()

    }

    log.trace "state.count ${state.count}"

    state.count = state.count + 1 

    log.trace "state.count ${state.count}"

    if(state.loadStatus == "Loaded" ){

        def options = devicesDiscovered() ?: []

		log.trace "state.loadStatus ${state.loadStatus}"

        return dynamicPage(name:"chooseBulbs", title:"", nextPage:"", install:true, uninstall: true) {

            section("Tap Below to View Device List") {

                input "selectedBulbs", "enum", required:false, title:"Select Bulb", multiple:true, options:options

                paragraph """Tap 'Done' after you have selected the desired devices."""

            }

        }

    }else{

    	if (state.count)

    

    	log.trace "state.loadStatus ${state.loadStatus}"

        def msg = state.count >= 3 ? "The TCP Gateway is not responding, please verify the ip address" : "Please wait while we discover your devices. Discovery can take some minutes or more, so sit back and relax! Select your device below once discovered."

        return dynamicPage(name:"chooseBulbs", title:"", nextPage:"", refreshInterval:5) {

            section(msg) {}

        }

    }

}





def installed() {

	debugOut "Installed with settings: ${settings}"



	unschedule()

	unsubscribe()



	setupBulbs()



	def cron = "0 0/1 * * * ?"

	log.debug "schedule('$cron', syncronizeDevices)"

	schedule(cron, syncronizeDevices)

}



def updated() {

	debugOut "Updated with settings: ${settings}"

	unschedule()

	setupBulbs()

	def cron = "0 0/1 * * * ?"

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

	debugOut "setupBulbs()"

	def bulbs = state.devices

	def deviceFile = "TCP Bulb"



	selectedBulbs.each { did ->

		//see if this is a selected bulb and install it if not already

		def d = getChildDevice(did)



		if(!d) {

			def newBulb = bulbs.find { (it.did) == did }

			d = addChildDevice("mujica", deviceFile, did, null, [name: "${newBulb?.name}", label: "${newBulb?.name}", completedSetup: true,"data":["model":newBulb?.model,"nodetype":newBulb?.nodetype,"node":newBulb?.node,"dni":did]])

		} else {

			infoOut "Avoid add existent device ${did}"

		}

	}

	def delete = getChildDevices().findAll { !selectedBulbs?.contains(it.deviceNetworkId) }

	removeChildDevices(delete)

}





def deviceDiscovery() {

	log.trace "deviceDiscovery()"

	def data = "<gip><version>1</version><token>${state.token}</token></gip>"



	def Params = [

		cmd: "RoomGetCarousel",

		data: "${data}",

		fmt: "json"

	]



	def cmd = toQueryString(Params)



	debugOut "deviceDiscovery()"



	apiGet(cmd,"RoomGetCarouselHandler")

}



def apiGet(String data, String calledBackHandler) {

	debugOut "apiGet($data, $calledBackHandler) $ipGateway"

    sendHubCommand(new physicalgraph.device.HubAction([

		method: "GET",

		path: "/gwr/gop.php?$data",

		headers: [

			HOST: "$ipGateway:80"

		]], getNetworkId("$ipGateway","80"), [callback: calledBackHandler]))

}





void SendCommandHandler(physicalgraph.device.HubResponse hubResponse){

	debugOut "SendCommandHandler($hubResponse)"

    debugOut "hubResponse.body ${hubResponse.body}"

}

void RoomGetCarouselHandler(physicalgraph.device.HubResponse hubResponse){

	debugOut "RoomGetCarouselHandler($hubResponse)"

    def bodyXml



    if (hubResponse?.body?.contains("<gip>"))

    { // description.xml response (application/xml)

        debugOut "body contains xml"

        bodyXml = new XmlSlurper().parseText(hubResponse.body)

        debugOut "bodyXml $bodyXml"

    }

        



	def rooms = ""

	def devices = []

	def deviceList = []





	rooms = bodyXml.room

    

    

    debugOut "rooms ${rooms[1]}"



    rooms.each({

        devices  = it.device

        debugOut "it.device ${it.device}"

        def roomName = it.name

        debugOut "roomName = ${it.name}"

        debugOut "devices[1] ${devices[1]}"

        debugOut "devices[1] != null"

        def roomId = it?.rid

        debugOut "Room Device Data: did:${roomId} roomName:${roomName}"

        devices.each({

            debugOut "Bulb Device Data: did:${it?.did} room:${roomName} BulbName:${it?.name}"

            deviceList += ["name" : "${roomName} ${it?.name}", "did" : "${it?.did}", "type" : "${devices?.type}", "node" : "${devices?.node}", "nodetype" : "${devices?.nodetype}", "model" : "${devices?.prodmodel}"]

        })

    })



	devices = ["devices" : deviceList]

    debugOut "devices $devices"

	state.devices = devices.devices

    state.loadStatus = "Loaded"

}



def getDevices()

{

	state.devices = state.devices ?: [:]

}



void RoomGetCarouselUpdateHandler(physicalgraph.device.HubResponse hubResponse){

	debugOut "RoomGetCarouselUpdateHandler($hubResponse)"

    

   

    debugOut "msg ${parseLanMessage(hubResponse.body)}"

    

    def bodyXml



    if (hubResponse?.body?.contains("<gip>"))

    { 

        debugOut "body contains xml"

        bodyXml = new XmlSlurper().parseText(hubResponse.body)

    }

        



	def rooms = ""

	def devices = []

	def deviceList = []



	rooms = bodyXml.room



    rooms.each({

        devices  = it.device

        devices.each({

        	def dni = it.did.text()

        	def bulb = getChildDevice(dni)

            if ( bulb ){

                def power = it.power ? it.power.text() as float :0

                sendEvent( dni, [name: "power", value: power*1000] )

                if (( it.state.text() == "1" ) && ( bulb?.currentValue("switch") != "on" ))

                    sendEvent( dni, [name: "switch",value:"on"] )



                if (( it.state.text() == "0" ) && ( bulb?.currentValue("switch") != "off" ))

                    sendEvent( dni, [name: "switch",value:"off"] )



                if ( it.level.text() != bulb?.currentValue("level")) {

                	sendEvent( dni, [name: "level",value: "${it.level.text()}"] )

                    sendEvent( dni, [name: "setLevel",value: "${it.level.text()}"] )

                }

        	}

        })

    })

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



def getToken() {



	state.token = "1234567890"

}



String toQueryString(Map m) {

	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")

}



def syncronizeDevices() {

    poll(null)

}



def getNetworkId(ipaddr, port) {

    "${ipaddr.tokenize('.').collect {String.format('%02X', it.toInteger())}.join()}:${String.format('%04X', port.toInteger())}"

}



/**************************************************************************

 Child Device Call In Methods

 **************************************************************************/

def on(childDevice) {



	def dni = childDevice.device.deviceNetworkId

	def data = ""

	def cmd = ""



    data = "<gip><version>1</version><token>$state.token</token><did>${dni}</did><type>power</type><value>1</value></gip>"

    cmd = "DeviceSendCommand"



	def qParams = [

		cmd: cmd,

		data: "${data}",

		fmt: "json"

	]



	cmd = toQueryString(qParams)

	apiGet(cmd,"SendCommandHandler" )

}



def off(childDevice) {



	def dni = childDevice.device.deviceNetworkId

	def data = ""

	def cmd = ""



    data = "<gip><version>1</version><token>$state.token</token><did>${dni}</did><type>power</type><value>0</value></gip>"

    cmd = "DeviceSendCommand"



	def qParams = [

		cmd: cmd,

		data: "${data}",

		fmt: "json"

	]



	cmd = toQueryString(qParams)

	apiGet(cmd,"SendCommandHandler") 



}



def setLevel(childDevice, value) {

	debugOut "setLevel request from child device"



	def dni = childDevice.device.deviceNetworkId

	def data = ""

	def cmd = ""



    data = "<gip><version>1</version><token>${state.token}</token><did>${dni}</did><type>level</type><value>${value}</value></gip>"

    cmd = "DeviceSendCommand"





	def qParams = [

		cmd: cmd,

		data: "${data}",

		fmt: "json"

	]



	cmd = toQueryString(qParams)



	apiGet(cmd,"SendCommandHandler") 

}



def poll(childDevice) {

	infoOut "poll()"

	def eventTime = new Date().time

    if ((state.lastPollTime ?:0) + 10000  <=  eventTime ){

    	state.lastPollTime = new Date().time

        def Params = [

            cmd: "RoomGetCarousel",

            data: "<gip><version>1</version><token>${state.token}</token></gip>",

            fmt: "json"

        ]

        def cmd = toQueryString(Params)

        apiGet(cmd,"RoomGetCarouselUpdateHandler")

    }else{

    	infoOut "Multiple poll requests avoided"

    }

}





/**************************************************************************

 Msg Methods

 **************************************************************************/



def debugOut(msg) {

	//log.debug msg

}



def traceOut(msg) {

	log.trace msg

}



def infoOut(msg) {

	log.info msg

}