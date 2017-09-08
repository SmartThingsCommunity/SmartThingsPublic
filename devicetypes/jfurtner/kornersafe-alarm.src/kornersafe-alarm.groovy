/**
 *  KornerSafe
 *
 *  Copyright 2016 Jamie Furtner
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
include 'asynchttp_v1'

metadata {
definition (name: "KornerSafe Alarm", namespace: "jfurtner", author: "Jamie Furtner") {
		capability "polling"
		capability "switch"
        capability "bridge"
        capability "sensor"
        capability "Contact Sensor"
        capability "refresh"
        
        command "setOff"
        command "setOn"
        command "setOpen"
        command "setClosed"
        //attribute "message", "string"
        command "setAPIEndpoints", ["string", "string"]
	}
    

	simulator {		
	}
    
    preferences{
    	input("deviceIP", "string", title:"IP Address", required:true, displayDuringSetup:true)
        input("devicePort", "number", title:"HTTP Port (i.e. 80 - HTTP only supported)", required:true, displayDuringSetup:true)
        input("devicePage", "string", title:"HTTP endpoint (i.e. '/kornersafe.php')", required:true, displayDuringSetup:true)
        input("debugOutput", "bool", title: "Enable debug logging?", defaultValue: true, required: false)
        input("traceOutput", "bool", title: "Enable trace logging?", defaultValue: false, required: false)
    }

	tiles(scale:2) {
    	standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
        	state 'off', label: 'Disarmed', action: 'on', icon: 'st.locks.lock.unlocked', backgroundColor: '#ffffff',nextState:"turningOn"
            state "turningOn", label:'Arming', icon:'st,locks.lock.locking', backgroundColor: '#62bbdb', nextState:'turningOff' // 55% of on
            state 'on', label: 'Armed', action: 'off', icon: 'st.locks.lock.locked', backgroundColor: '#00a0dc',nextState:'turningOff'
            state "turningOff", label:'Disarming', icon:'st.locks.lock.unlocking',backgroundColor: '#92c8db',nextState: 'turningOn' // 33% of on
        }
        standardTile('refresh', 'device.refresh', width:2, height:2) {
        	state 'refresh', label: 'Refresh', action: 'refresh', icon: 'st.secondary.refresh'
        }        
        valueTile('contactSensor', "device.contact", width:2, height:2){
            state 'closed', label: 'Ok', icon: 'st.alarm.beep.beep', backgroundColor: '#d1ffe7'
        	state 'open', label: 'Alarm', icon: 'st.alarm.alarm.alarm', backgroundColor: '#e86d13'
        }
/*        valueTile('message', 'device.message', width:6, height:2) {
        	state 'message', label:'${currentValue}'
        }
*/
	}
}

def updated() {
	logDebug("Updated with settings: ${settings}")
	setDeviceNetworkId(deviceIP, devicePort)
	logDebug("Network ID: ${device.networkId}")
}

def logState()
{
	logDebug "NetworkId: ${device.networkId}"
    logDebug "appUrl: ${state.appUrl}"
    logDebug "appToken: ${state.appToken}"
}

def setAPIEndpoints(String newAppUrl, String newAppToken)
{
	logTrace('INIT setAPIEndpoints')
	state.appUrl = newAppUrl
    state.appToken = newAppToken
	logTrace("URL: ${state.appUrl}")
    logTrace("Token: ${state.appToken}")
}

def parse(msg) {
	logTrace 'INIT parse'
	def lanMsg = parseLanMessage(msg)
    
    logTrace(lanMsg.json)
    
    return result
}

// handle commands
def poll() {
	logTrace "INIT 'poll'"
	return hubAction('status')
}

def on() {
	logTrace "INIT 'on'"
	return hubAction('arm')
}

def off() {
	logTrace "INIT 'off'"
	return hubAction('disarm')
}

def refresh() {
	logTrace "INIT refresh"    
    return hubAction('status')
}

// handle smartapp commands
def setOff() {
	logTrace('INIT setOff')
    setSwitch('off')
}

def setOn() {
	logTrace('INIT setOn')
    setSwitch('on')
}

def setOpen()
{
	logTrace('INIT setOpen')
    setContact('open')
}

def setClosed()
{
	logTrace('INIT setClosed')
    setContact('closed')
}

/*private def sendMessage(msg)
{
	logTrace('INIT sendMessage')
	def dt = new Date()
    def completeMessage = "$dt $msg"
    logTrace("Message: $completeMessage")
	sendEvent(createEvent(name:"message", value:completeMessage))
}*/


private def setContact(String openClosed)
{
	logTrace('INIT setContact')
    if (currentContact != openClosed)
		sendEvent(getContactEvent(openClosed))
}

private def getContactEvent(String openClosed)
{
	logTrace('INIT getContactEvent')
    return createEvent(name: "contact", value: openClosed)
}

private def setSwitch(String onOff)
{
	logTrace('INIT setSwitch')
    if (currentSwitch != onOff)
		sendEvent(createEvent(name: "switch", value: onOff))
}

def hubAction(String action) {
	logTrace('INIT hubAction')
    setDeviceNetworkId(deviceIP, devicePort)
    
    def jsonBody = new groovy.json.JsonBuilder([
    	"appUrl":"${state.appUrl}",
        "appToken":"${state.appToken}"
        ])
    
	def hubAction = new physicalgraph.device.HubAction(
    	method: "POST",
        path: "$devicePage?$action",
        headers: [
        	HOST: "$deviceIP:$devicePort",
            'Content-Type':'application/json'
        ],
        body: jsonBody
    )
    logTrace jsonBody
    logTrace hubAction
    return hubAction
}

def logDebug(msg)
{
	if (debugOutput)
        log.debug msg
}

def logTrace(msg)
{
	if (traceOutput)
		log.trace msg
}

private updateDNI() { 
if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
   device.deviceNetworkId = state.dni
}
}
private setDeviceNetworkId(ip,port){
	logTrace('INIT setDeviceNetworkId')
    logTrace("IP: $ip port:$port")
  	def iphex = convertIPtoHex(ip)
  	def porthex = convertPortToHex(port)
    if (device.deviceNetworkId != "$iphex:$porthex")
    {
        device.deviceNetworkId = "$iphex:$porthex"
        logDebug "Device Network Id set to ${iphex}:${porthex}"
    }
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    logTrace "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    logTrace "Port entered is $port and the converted hex code is $hexport"
    return hexport
}