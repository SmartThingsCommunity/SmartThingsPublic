/**
 *  Neato Botvac Connected Series
 *
 *  Copyright 2016 Alex Lee Yuk Cheung
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
 *  VERSION HISTORY
 *	14-10-2016: 1.0 - Initial Version
 *
 */
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;

metadata {
	definition (name: "Neato Botvac Connected Series", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
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


	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
    	multiAttributeTile(name: "clean", width: 6, height: 4, type:"lighting") {
			tileAttribute("device.switch", key:"PRIMARY_CONTROL", canChangeBackground: true){
				attributeState("off", label: 'STOPPED', action: "switch.on", icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/laser-guided-navigation.png", backgroundColor: "#ffffff", nextState:"on")
				attributeState("on", label: 'CLEANING', action: "switch.off", icon: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/best-pet-hair-cleaning.png", backgroundColor: "#79b821", nextState:"off")
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
	poll()
}

def on() {
	log.debug "Executing 'on'"
    if (device.latestState('status').stringValue == 'paused') {
    	nucleoPOST("/messages", '{"reqId":"1", "cmd":"resumeCleaning"}')
    }
    else {
		nucleoPOST("/messages", '{"reqId":"1", "cmd":"startCleaning", "params":{"category": 2, "mode": 2, "modifier": 2}}') 
    }
    runIn(2, refresh)
}

def off() {
	log.debug "Executing 'off'"
    def currentState = device.latestState('status').stringValue
    if (currentState == 'cleaning' || currentState == 'error') {
    	nucleoPOST("/messages", '{"reqId":"1", "cmd":"pauseCleaning"}')
    }
    runIn(2, refresh)
}

def dock() {
	log.debug "Executing 'dock'"
    if (device.latestState('status').stringValue == 'paused') {
    	nucleoPOST("/messages", '{"reqId":"1", "cmd":"sendToBase"}')
    }
    runIn(2, refresh)
}

def enableSchedule() {
	log.debug "Executing 'enableSchedule'"
	nucleoPOST("/messages", '{"reqId":"1", "cmd":"enableSchedule"}')
    runIn(2, refresh)
}

def disableSchedule() {
	log.debug "Executing 'disableSchedule'"
	nucleoPOST("/messages", '{"reqId":"1", "cmd":"disableSchedule"}')
    runIn(2, refresh)
}

def setOffline() {
	sendEvent(name: 'network', value: "Not Connected" as String)
}

def poll() {
	log.debug "Executing 'poll'"
    def resp = nucleoPOST("/messages", '{"reqId":"1", "cmd":"getRobotState"}')
    def result = resp.data
    def statusMsg = ""
    def binFullFlag = false
	if (resp.status != 200) {
    	if (result.containsKey("message")) {
        	switch (result.message) {
            	case "Could not find robot_serial for specified vendor_name":
                	statusMsg += 'Robot serial and/or secret is not correct'
                break;
            }
        }
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
        setOffline()
        sendEvent(name: 'status', value: "error" as String)
        statusMsg += 'Not Connected To Neato'
		log.debug headerString
	}
    else {
        if (result.containsKey("state")) {
        	sendEvent(name: 'network', value: "Connected" as String)
        	//state 1 - Ready to clean
        	//state 2 - Cleaning
        	//state 3 - Paused
       		//state 4 - Error
            switch (result.state) {
        		case "1":
            		sendEvent(name: "status", value: "ready")
                	sendEvent(name: "switch", value: "off")
                	statusMsg += 'READY TO CLEAN'
                    log.debug device.latestState('switch').stringValue
				break;
				case "2":
					sendEvent(name: "status", value: "cleaning")
                	sendEvent(name: "switch", value: "on")
                	statusMsg += 'CURRENTLY CLEANING'
				break;
            	case "3":
					sendEvent(name: "status", value: "paused")
                	sendEvent(name: "switch", value: "off")
                	statusMsg += 'PAUSED'
				break;
            	case "4":
					sendEvent(name: "status", value: "error")
                	statusMsg += 'HAS A PROBLEM'
				break;
            	default:
               		sendEvent(name: "status", value: "unknown")
                	statusMsg += 'UNKNOWN'
				break;
        	}
        }
        if (result.containsKey("error")) {
        	switch (result.error) {
            	case "ui_alert_dust_bin_full":
					binFullFlag = true
				break;
                case "ui_alert_return_to_base":
					statusMsg += ' - Returning to base'
				break;
                case "ui_alert_return_to_start":
					statusMsg += ' - Returning to start'
				break;
                case "ui_alert_return_to_charge":
					statusMsg += ' - Returning to charge'
				break;
                case "ui_alert_busy_charging":
					statusMsg += ' - Busy charging'
				break;
                case "ui_alert_recovering_location":
					statusMsg += ' - Recovering Location'
				break;
                case "ui_error_dust_bin_full":
					binFullFlag = true
                    statusMsg += ' - Dust bin full!'
				break;
            	case "ui_error_picked_up":
					statusMsg += ' - Picked up!'
				break;
                case "ui_error_brush_stuck":
                	statusMsg += ' - Brush stuck!'
                break;
                case "ui_error_stuck":
                	statusMsg += ' - I\'m stuck!'
                break;
                case "ui_error_dust_bin_missing":
                	statusMsg += ' - Dust Bin is missing!'
                break
                case "ui_error_navigation_falling":
                	statusMsg += ' - Please clear my path!'
                break
                case "ui_error_navigation_noprogress":
                	statusMsg += ' - Please clear my path!'
                break
                case "ui_error_battery_overtemp":
                	statusMsg += ' - Battery is overheating!'
                break
                case "ui_error_unable_to_return_to_base":
                	statusMsg += ' - Unable to return to base!'
                break
                case "ui_error_bumper_stuck":
                	statusMsg += ' - Bumper stuck!'
                break
                case "ui_error_lwheel_stuck":
                	statusMsg += ' - Left wheel stuck!'
                break
                case "ui_error_rwheel_stuck":
                	statusMsg += ' - Right wheel stuck!'
                break
                case "ui_error_lds_jammed":
                	statusMsg += ' - LIDAR jammed!'
                break
                case "ui_error_brush_overload":
                	statusMsg += ' - Brush overloaded!'
                break
                case "ui_error_hardware_failure":
                	statusMsg += ' - Hardware Failure!'
                break
                case "ui_error_unable_to_see":
                	statusMsg += ' - Unable to see!'
                break
                case "ui_error_rdrop_stuck":
                	statusMsg += ' - Right drop stuck!'
                break
                case "ui_error_ldrop_stuck":
                	statusMsg += ' - Left drop stuck!'
                break
                default:
                	if ("ui_alert_invalid" != result.error) {
                		statusMsg += ' - ' + result.error.replaceAll('ui_error_', '').replaceAll('ui_alert_', '').replaceAll('_',' ').capitalize()
                    }
				break;
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
    sendEvent(name: 'statusMsg', value: statusMsg, displayed: false)
    
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}

def nucleoPOST(path, body) {
	try {
		log.debug("Beginning API POST: ${nucleoURL(path)}, ${body}")
		def date = new Date().format("EEE, dd MMM yyyy HH:mm:ss z", TimeZone.getTimeZone('GMT'))
        log.debug getHMACSignature(date, body)
		httpPostJson(uri: nucleoURL(path), body: body, headers: nucleoRequestHeaders(date, getHMACSignature(date, body)) ) {response ->
			parent.logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		parent.logResponse(e.response)
		return e.response
	}
}

def getHMACSignature(date, body) {
	//request params
	def robot_serial = device.deviceNetworkId.tokenize("|")[0]
    //Format date should be "Fri, 03 Apr 2015 09:12:31 GMT"
	
	def robot_secret_key = device.deviceNetworkId.tokenize("|")[1]

	// build string to be signed
	def string_to_sign = "${robot_serial.toLowerCase()}\n${date}\n${body}"

	// create signature with SHA256
	//signature = OpenSSL::HMAC.hexdigest('sha256', robot_secret_key, string_to_sign)
    try {
    	Mac mac = Mac.getInstance("HmacSHA256")
    	SecretKeySpec secretKeySpec = new SecretKeySpec(robot_secret_key.getBytes(), "HmacSHA256")
    	mac.init(secretKeySpec)
    	byte[] digest = mac.doFinal(string_to_sign.getBytes())
    	return digest.encodeHex()
   	} catch (InvalidKeyException e) {
    	throw new RuntimeException("Invalid key exception while converting to HMac SHA256")
  	}
}

Map nucleoRequestHeaders(date, HMACsignature) {
	return [
        'X-Date': "${date}",
        'Accept': 'application/vnd.neato.nucleo.v1',
        'Content-Type': 'application/*+json',
        'X-Agent': '0.11.3-142',
        'Authorization': "NEATOAPP ${HMACsignature}"
    ]
}

def nucleoURL(path = '/') 			 { return "https://nucleo.neatocloud.com:4443/vendors/neato/robots/${device.deviceNetworkId.tokenize("|")[0]}${path}" }