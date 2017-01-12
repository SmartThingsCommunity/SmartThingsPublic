/**
 *  Neato BotVac Connected
 *
 *	Smartthings Devicetype
 *
 *  Copyright 2015 Sidney Johnson
 *  If you like this device, please support the developer via PayPal: https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XKDRYZ3RUNR9Y
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
 *	Version: 1.0 - Initial Version
 *	Version: 1.1 - Fixed installed and updated functions
 *	Version: 1.2 - Added error tracking, and better icons, link state
 *	Version: 1.3 - Better error tracking, error correction and the ability to change the default port (thx to sidhartha100), fix a bug that prevented auto population of deviceNetworkId
 *	Version: 1.4 - Added bin status and code clean up
 *	Version: 1.4.1 - Added poll on inilizition, better error handling, inproved clean button
 *	Version: 1.4.2 - Fixed poll not working, changed battery icon, and code clean up
 *
 *  Modified 2016 by Alex Lee Yuk Cheung for Neato BotVac Compatibility. Requires https://github.com/kangguru/botvac web server running. 
 *  Neato Version: 1.0 - Initial Version
 *	Neato Version: 1.0.1 - Improved Botvac connection detection
 *	Neato Version: 1.0.2 - Added Please Clear My Path Error message
 *  Neato Version: 1.0.3 - Added Navigation No Progress Error message
 *	Neato Version: 1.0.4 - Added Neato icons
 *	Neato Version: 1.0.4b - Display error message when serial/secret on Pi is incorrect.
 *	Neato Version: 1.0.4c - Stop continuous error message on bad link.
 */
import groovy.json.JsonSlurper

metadata {
	definition (name: "Neato Botvac Connected", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		capability "Battery"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
        
		command "refresh"
        command "dock"
        command "enableSchedule"
        command "disableSchedule"

		attribute "network","string"
		attribute "bin","string"
	}
    
	preferences {
		input("ip", "text", title: "IP Address", description: "Your Botvac API Server Address", required: true, displayDuringSetup: true)
		input("port", "number", title: "Port Number", description: "YourBotvac API Server Port Number", required: true, displayDuringSetup: true)
	}

	tiles(scale: 2) {
    	multiAttributeTile(name: "clean", width: 6, height: 4, type:"lighting") {
			tileAttribute("device.switch", key:"PRIMARY_CONTROL", canChangeBackground: true){
				attributeState("off", label: 'STOPPED', action: "on", icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png", backgroundColor: "#ffffff")
				attributeState("on", label: 'CLEANING', action: "off", icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/best-pet-hair-cleaning.png", backgroundColor: "#79b821")
			}
            tileAttribute ("statusMsg", key: "SECONDARY_CONTROL") {
				attributeState "statusMsg", label:'${currentValue}'
			}
		}
    
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        standardTile("charging", "device.charging", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
         	state ("true", label:'Charging', icon: "st.samsung.da.RC_ic_charge", backgroundColor: "#E5E500")
			state ("false", label:'', icon: "st.samsung.da.RC_ic_charge")
		}
		standardTile("bin", "device.bin", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
			state ("default", label:'unknown', icon: "st.unknown.unknown.unknown")
			state ("empty", label:'Bin Empty', icon: "st.Office.office10",backgroundColor: "#79b821")
			state ("full", label:'Bin Full', icon: "st.Office.office10", backgroundColor: "#bc2323")
		}
		/*standardTile("clean", "device.switch", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
			state("on", label: 'dock', action: "switch.off", icon: "st.Appliances.appliances13", backgroundColor: "#79b821", nextState:"off")
			state("off", label: 'clean', action: "switch.on", icon: "st.Appliances.appliances13", backgroundColor: "#79b821", nextState:"on")
		}*/
		standardTile("network", "device.network", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
			state ("default", label:'unknown', icon: "st.unknown.unknown.unknown")
			state ("Connected", label:'Link Good', icon: "st.Health & Wellness.health9", backgroundColor: "#79b821")
			state ("Not Connected", label:'Link Bad', icon: "st.Health & Wellness.health9", backgroundColor: "#bc2323")
		}
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
			state("default", label:'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon")
		}
        standardTile("status", "device.status", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
			state ("unknown", label:'${currentValue}', icon: "st.unknown.unknown.unknown")
			state ("cleaning", label:'${currentValue}', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/best-pet-hair-cleaning.png")
			state ("ready", label:'${currentValue}', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png")
			state ("error", label:'${currentValue}', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png", backgroundColor: "#bc2323")
			state ("paused", label:'${currentValue}', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png")
		}
        
        standardTile("dockStatus", "device.dockStatus", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
         	state ("docked", label:'DOCKED', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/auto-charge-resume.png")
			state ("dockable", label:'DOCK', action: "dock", icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_staub.png")
            state ("undocked", label:'UNDOCKED', icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png")
		}
        
        standardTile("scheduled", "device.scheduled", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
         	state ("true", label:'Sched', action: "disableSchedule", icon:"st.Office.office7")
			state ("false", label:'Manual', action: "enableSchedule", icon: "st.Appliances.appliances13")
		}
        
        standardTile("dockHasBeenSeen", "device.dockHasBeenSeen", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
         	state ("true", label:'SEEN', backgroundColor: "#79b821", icon:"st.Transportation.transportation13")
			state ("false", label:'SEARCHING', backgroundColor: "#E5E500", icon:"st.Transportation.transportation13")
		}
        
		main("clean")
			details(["clean","status","battery","bin","network", "dockStatus", "charging", "dockHasBeenSeen", "scheduled", "refresh"])
		}
}

def parse(String description) {
	def map
	def headerString
	def bodyString
	def slurper
	def result
	def statusMsg = ""
    def binFullFlag = false
    unschedule('setOffline')
	map = stringToMap(description)
	headerString = new String(map.headers.decodeBase64())    
	if (headerString.contains("200 OK")) {	
		bodyString = new String(map.body.decodeBase64())
		slurper = new JsonSlurper()
		result = slurper.parseText(bodyString)
        log.debug result
        //Show error if the Pi app cannot find robot with provided serial/secret credentials
        if (result.containsKey("message")) {
        	switch (result.message) {
            	case "Could not find robot_serial for specified vendor_name":
                	setOffline()
        			sendEvent(name: 'status', value: "error" as String)
        			statusMsg += 'Robot serial and/or secret is not correct'
					log.debug headerString
                break;
            }
        }
        if (result.containsKey("state")) {
        	sendEvent(name: 'network', value: "Connected" as String)
        	//state 1 - Ready to clean
        	//state 2 - Cleaning
        	//state 3 - Paused
       		//state 4 - Error
            switch (result.state) {
        		case "1":
            		sendEvent(name: 'status', value: "ready" as String)
                	sendEvent(name: 'switch', value: "off" as String)
                	statusMsg += 'READY TO CLEAN'
				break;
				case "2":
					sendEvent(name: 'status', value: "cleaning" as String)
                	sendEvent(name: 'switch', value: "on" as String)
                	statusMsg += 'CURRENTLY CLEANING'
				break;
            	case "3":
					sendEvent(name: 'status', value: "paused" as String)
                	sendEvent(name: 'switch', value: "off" as String)
                	statusMsg += 'PAUSED'
				break;
            	case "4":
					sendEvent(name: 'status', value: "error" as String)
                	statusMsg += 'HAS A PROBLEM'
				break;
            	default:
               		sendEvent(name: 'status', value: "unknown" as String)
                	statusMsg += 'UNKNOWN'
				break;
        	}
        }
        if (result.containsKey("error")) {
        	switch (result.error) {
            	case "ui_alert_dust_bin_full":
					binFullFlag = true
				break;
            	case "ui_error_picked_up":
					statusMsg += ' - Picked Up!'
				break;
                case "ui_error_brush_stuck":
                	statusMsg += ' - Brush Stuck!'
                break;
                case "ui_error_stuck":
                	statusMsg += ' - I\'m Stuck!'
                break;
                case "ui_error_dust_bin_missing":
                	statusMsg += ' - Dust Bin Is Missing!'
                break
                case "ui_error_navigation_falling":
                	statusMsg += ' - Please Clear My Path!'
                break
                case "ui_error_navigation_noprogress":
                	statusMsg += ' - Please Clear My Path!'
                break
                
                //More error detail messages here as discovered
				
			}
        }
        if (result.containsKey("details")) {
        	if (result.details.isDocked) {
            	sendEvent(name: 'dockStatus', value: "docked" as String)
            } else {
            	sendEvent(name: 'dockStatus', value: "undocked" as String)
            }
        	sendEvent(name: 'charging', value: result.details.isCharging as String)
        	sendEvent(name: 'scheduled', value: result.details.isScheduleEnabled as String)
        	sendEvent(name: 'dockHasBeenSeen', value: result.details.dockHasBeenSeen as String)
        	sendEvent(name: 'battery', value: result.details.charge as Integer)
        }
        if (result.containsKey("availableCommands")) {
        	if (result.availableCommands.goToBase) {
        		sendEvent(name: 'dockStatus', value: "dockable")
            }
        }
        
        if (binFullFlag) {
        	sendEvent(name: 'bin', value: "full" as String)
        } else {
        	sendEvent(name: 'bin', value: "empty" as String)
        }
        
	}
	else {
		setOffline()
        sendEvent(name: 'status', value: "error" as String)
        statusMsg += 'Not Connected To Neato'
		log.debug headerString
	}
    sendEvent(name: 'statusMsg', value: statusMsg, displayed: false)
}

// handle commands

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}

def initialize() {
	log.info "Neato Botvac Connected ${textVersion()} ${textCopyright()}"
	poll()
}

def on() {
	log.debug "Executing 'on'"
    if (device.latestState('status').stringValue == 'paused') {
    	api('resume')
    }
    else {
		api('on')
    }
}

def off() {
	log.debug "Executing 'off'"
	api('pause')
}

def dock() {
	log.debug "Executing 'dock'"
    if (device.latestState('status').stringValue == 'paused') {
    	api('dock')
    }
}

def enableSchedule() {
	log.debug "Executing 'enableSchedule'"
	api('auto')
}

def disableSchedule() {
	log.debug "Executing 'disableSchedule'"
	api('manual')
}

def setOffline() {
	sendEvent(name: 'network', value: "Not Connected" as String)
}

def poll() {
	log.debug "Executing 'poll'"
    
	if (device.deviceNetworkId != null) {
		api('refresh')
	}
	else {
		setOffline()
        sendEvent(name: 'status', value: "error" as String)
		log.debug "DNI: Not set"
	}
}

def refresh() {
	log.debug "Executing 'refresh'"
	ipSetup()
	api('refresh')
}

def api(method, args = [], success = {}) {
	log.debug "Executing 'api'"
    
	def methods = [
		'on': [uri: "/start_cleaning", type: 'get'],
		'pause': [uri: "/pause_cleaning", type: 'get'],
        'resume': [uri: "/resume_cleaning", type: 'get'],
        'dock': [uri: "/send_to_base", type: 'get'],
        'refresh' : [uri: "/get_robot_state", type: 'get'],
        'manual': [uri: "/disable_schedule", type: 'get'],
        'auto': [uri: "/enable_schedule", type: 'get']
	]
	
	def request = methods.getAt(method)
	
	log.debug "Starting $method : $args"
	postAction(request.uri)
}

private postAction(uri){
  ipSetup()  
  
  runIn(30, setOffline)
  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  ) //, delayAction(1000), refresh()]
  log.debug("Executing hubAction on " + getHostAddress())
  log.debug hubAction
  hubAction    
}

def ipSetup() {
	def hosthex
	def porthex
	if (settings.ip) {
		hosthex = convertIPtoHex(settings.ip)
	}
	if (settings.port) {
		porthex = convertPortToHex(settings.port)
	}
	if (settings.ip && settings.port) {
		device.deviceNetworkId = "$hosthex:$porthex"
	}
}

private getHostAddress() {
	return "${ip}:${port}"
}

private String convertIPtoHex(ip) { 
	String hexip = ip.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	return hexip
}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
	return hexport
}
private delayAction(long time) {
	new physicalgraph.device.HubAction("delay $time")
}

private def textVersion() {
	def text = "Version 1.4.2. Neato Version 1.0"
}

private def textCopyright() {
	def text = "Copyright © 2015 Sidjohn1. Modified by Alex Lee Yuk Cheung"
}