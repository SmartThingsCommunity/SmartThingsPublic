/**
 *  HomeSeer HS-WD100+
 *
 *  Copyright 2016 DarwinsDen.com
 *
 *  For device parameter information and images, questions or to provide feedback on this device handler, 
 *  please visit: 
 *
 *      darwinsden.com/homeseer100plus/
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
 *	Author: Darwin@DarwinsDen.com
 *	Date: 2016-04-10
 *
 *	Changelog:
 *
 *	0.10 (04/10/2016) -	Initial 0.1 Beta.
 *  0.11 (05/28/2016) - Set numberOfButtons attribute for ease of use with CoRE and other SmartApps. Corrected physical/digital states.
 *  0.12 (06/03/2016) - Added press type indicator to display last tap/hold press status
 *  0.13 (06/13/2016) - Added dim level ramp-up option for remote dim commands
 *  0.14 (08/01/2016) - Corrected 60% dim rate limit test that was inadvertently pulled into repository
 *  0.15 (09/06/2016) - Added Firmware version info. Removed unused lit indicator button.
 *
 */
 
metadata {
	definition (name: "WD100+ Dimmer", namespace: "darwinsden", author: "darwin@darwinsden.com") {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Button"
        capability "Configuration"
        
        command "tapUp2"
        command "tapDown2"
        command "tapUp3"
        command "tapDown3"
        command "holdUp"
        command "holdDown"

        fingerprint deviceId: "0x1101", inClusters: "0x5E, 0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x26, 0x27, 0x70, 0x2C, 0x2B, 0x5B, 0x7A", outClusters: "0x5B"
}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

    preferences {
        input "remoteDimFadeUpEnabled", "bool", title: "Enable remote dim fade-up",  defaultValue: false,  displayDuringSetup: true, required: false	
        input "remoteDimDurationPerLevel", "number", title: "Remote dim rate ms duration per level (default is 20) [1-1000]",  defaultValue: 20,  displayDuringSetup: true, required: false	
		input "remoteSizeOfDimLevels", "number", title: "Remote dim rate % size of each level (default is 5) [1-50]",  defaultValue: 5,  displayDuringSetup: true, required: false	
    }
    
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
            tileAttribute("device.status", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}', unit:"")
            }
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        valueTile("firmwareVersion", "device.firmwareVersion", width:2, height: 2, decoration: "flat", inactiveLabel: false) {
			state "default", label: '${currentValue}'
		}
        
		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}

        standardTile("tapUp2", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Tap ▲▲", backgroundColor: "#ffffff", action: "tapUp2", icon: "st.Home.home30"
		}     
 
        standardTile("tapDown2", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Tap ▼▼", backgroundColor: "#ffffff", action: "tapDown2", icon: "st.Home.home30"
		} 

        standardTile("tapUp3", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Tap ▲▲▲", backgroundColor: "#ffffff", action: "tapUp3", icon: "st.Home.home30"
		} 

        standardTile("tapDown3", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Tap ▼▼▼", backgroundColor: "#ffffff", action: "tapDown3", icon: "st.Home.home30"
		} 

        standardTile("holdUp", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Hold ▲", backgroundColor: "#ffffff", action: "holdUp", icon: "st.Home.home30"
		} 

        standardTile("holdDown", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Hold ▼", backgroundColor: "#ffffff", action: "holdDown", icon: "st.Home.home30"
		} 

		main(["switch"])
        
		details(["switch","tapUp2","tapUp3","holdUp","tapDown2","tapDown3","holdDown","level","firmwareVersion", "refresh",])
	}
}

def parse(String description) {
	def result = null
    if (description != "updated") {
	    def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])	
        if (cmd) {
		    result = zwaveEvent(cmd)
	    }
    }
    if (!result){
        log.debug "Parse returned ${result} for command ${cmd}"
    }
    else {
		log.debug "Parse returned ${result}"
    }   
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
 	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
    dimmerEvents(cmd) 
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
    state.lastLevel = cmd.value
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")   
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "ConfigurationReport $cmd"
	def value = "when off"
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	createEvent([name: "indicatorStatus", value: value])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
    state.manufacturer=cmd.manufacturerName
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)	
    setFirmwareVersion()
    createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
    //updateDataValue("applicationVersion", "${cmd.applicationVersion}")
    log.debug ("received Version Report")
    log.debug "applicationVersion:      ${cmd.applicationVersion}"
    log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    state.firmwareVersion=cmd.applicationVersion+'.'+cmd.applicationSubVersion
    log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
    setFirmwareVersion()
    createEvent([descriptionText: "Firmware V"+state.firmwareVersion, isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd) { 
    log.debug ("received Firmware Report")
    log.debug "checksum:       ${cmd.checksum}"
    log.debug "firmwareId:     ${cmd.firmwareId}"
    log.debug "manufacturerId: ${cmd.manufacturerId}"
    [:]
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
    def level = Math.max(Math.min(valueaux, 99), 0)
    def result = []
    def statusDelay = 5000
	if (remoteDimFadeUpEnabled && state.lastLevel != null && level > state.lastLevel) 
    {
         //Workaround for HS-D100+ current lack of support for remote dim level rate configuration
         for (def i = state.lastLevel + state.remoteSizeOfDimLevels; i < level; i=i+state.remoteSizeOfDimLevels) {  
            result << zwave.basicV1.basicSet(value: i).format()
            result << "delay $state.remoteDimDurationPerLevel"
         }
         statusDelay = state.remoteDimDurationPerLevel * state.remoteSizeOfDimLevels + 3000
    }

    result << zwave.basicV1.basicSet(value: level).format()
   
    if (level > 0) {
		sendEvent(name: "switch", value: "on")
    } else {
		sendEvent(name: "switch", value: "off")
	}
    state.lastLevel = level
    sendEvent (name: "level", value: level, unit: "%")
    result << "delay $statusDelay"
	result << zwave.switchMultilevelV1.switchMultilevelGet().format()
    result << "delay 3000"
	result << zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	log.debug "refresh() called"
    configure()
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    log.debug("sceneNumber: ${cmd.sceneNumber} keyAttributes: ${cmd.keyAttributes}")
    def result = []
    
    switch (cmd.sceneNumber) {
      case 1:
          // Up
          switch (cmd.keyAttributes) {
              case 0:
                  result=createEvent([name: "switch", value: "on", type: "physical"])
                  break
 
              case 1:
                  result=createEvent([name: "switch", value: "on", type: "physical"])
                  break
              case 2:
                  // Hold
                  result += createEvent(holdUpResponse("physical"))  
                  result += response("delay 100")
                  result += createEvent([name: "switch", value: "on", type: "physical"])    

                  break
              case 3: 
                  // 2 Times
                  result=createEvent(tapUp2Response("physical"))
                  break
              case 4:
                  // 3 Three times
                  result=createEvent(tapUp3Response("physical"))
                  break
              default:
                  log.debug ("unexpected up press keyAttribute: $cmd.keyAttributes")
          }
          break
          
      case 2:
          // Down
          switch (cmd.keyAttributes) {
              case 0:
                  result=createEvent([name: "switch", value: "off", type: "physical"])
                  break

              case 1:
                  result=createEvent([name: "switch", value: "off", type: "physical"])
                  break
              case 2:
                  // Hold
                  result += createEvent(holdDownResponse("physical"))
                  result += response("delay 100")
                  result += createEvent([name: "switch", value: "off", type: "physical"]) 
                  break
              case 3: 
                  // 2 Times
                  result=createEvent(tapDown2Response("physical"))
                  break
              case 4:
                  // 3 Times
                  result=createEvent(tapDown3Response("physical"))
                  break
              default:
                  log.debug ("unexpected down press keyAttribute: $cmd.keyAttributes")
           } 
           break
           
      default:
           // unexpected case
           log.debug ("unexpected scene: $cmd.sceneNumber")
   }  
   return result
}

def tapUp2Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▲▲")
	[name: "button", value: "pushed", data: [buttonNumber: "1"], descriptionText: "$device.displayName Tap-Up-2 (button 1) pressed", 
       isStateChange: true, type: "$buttonType"]
}

def tapDown2Response(String buttontype) {
    sendEvent(name: "status" , value: "Tap ▼▼")
	[name: "button", value: "pushed", data: [buttonNumber: "2"], descriptionText: "$device.displayName Tap-Down-2 (button 2) pressed", 
      isStateChange: true, type: "$buttonType"]
}

def tapUp3Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▲▲▲")
	[name: "button", value: "pushed", data: [buttonNumber: "3"], descriptionText: "$device.displayName Tap-Up-3 (button 3) pressed", 
    isStateChange: true, type: "$buttonType"]
}

def tapDown3Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▼▼▼")
	[name: "button", value: "pushed", data: [buttonNumber: "4"], descriptionText: "$device.displayName Tap-Down-3 (button 4) pressed", 
    isStateChange: true, type: "$buttonType"]
}

def holdUpResponse(String buttonType) {
    sendEvent(name: "status" , value: "Hold ▲")
	[name: "button", value: "pushed", data: [buttonNumber: "5"], descriptionText: "$device.displayName Hold-Up (button 5) pressed", 
    isStateChange: true, type: "$buttonType"]
}

def holdDownResponse(String buttonType) {
    sendEvent(name: "status" , value: "Hold ▼")
	[name: "button", value: "pushed", data: [buttonNumber: "6"], descriptionText: "$device.displayName Hold-Down (button 6) pressed", 
    isStateChange: true, type: "$buttonType"]
}

def tapUp2() {
	sendEvent(tapUp2Response("digital"))
}

def tapDown2() {
	sendEvent(tapDown2Response("digital"))
}

def tapUp3() {
	sendEvent(tapUp3Response("digital"))
}

def tapDown3() {
	sendEvent(tapDown3Response("digital"))
}

def holdUp() {
	sendEvent(holdUpResponse("digital"))
}

def holdDown() {
	sendEvent(holdDownResponse("digital"))
} 

def setFirmwareVersion() {
   def versionInfo = ''
   if (state.manufacturer)
   {
      versionInfo=state.manufacturer+' '
   }
   if (state.firmwareVersion)
   {
      versionInfo=versionInfo+"Firmware V"+state.firmwareVersion
   }
   else 
   {
     versionInfo=versionInfo+"Firmware unknown"
   }   
   sendEvent(name: "firmwareVersion",  value: versionInfo, isStateChange: true)
}

def configure() {
   log.debug ("configure() called")
   setDimRatePrefs()
   sendEvent(name: "numberOfButtons", value: 6, displayed: false)
   def commands = []
   commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
   commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
   commands << zwave.versionV1.versionGet().format()
   //commands << zwave.firmwareUpdateMdV2.firmwareMdGet().format()
   delayBetween(commands,300)
}

def setDimRatePrefs() 
{
   log.debug ("set prefs")
   if (remoteSizeOfDimLevels == null)
   {
      state.remoteSizeOfDimLevels = 5
   }
   else if (remoteSizeOfDimLevels < 1)
   {
      state.remoteSizeOfDimLevels = 1
   }
   else if (remoteSizeOfDimLevels > 50)
   {
      state.remoteSizeOfDimLevels = 50
   }
   else
   {
      state.remoteSizeOfDimLevels = remoteSizeOfDimLevels
   }
   
   if (remoteDimDurationPerLevel == null)
   {
      state.remoteDimDurationPerLevel = 20
   }
   else if (remoteDimDurationPerLevel < 1)
   {
      state.remoteDimDurationPerLevel = 1
   }
   else if (remoteDimDurationPerLevel > 1000)
   {
      state.remoteDimDurationPerLevel = 1000
   }
   else
   {
     state.remoteDimDurationPerLevel = remoteDimDurationPerLevel
   }  
}

def updated()
{
   log.debug ("updated called")
   setDimRatePrefs()
}