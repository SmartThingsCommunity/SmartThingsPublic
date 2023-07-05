/**
 *  HomeSeer HS-WD100+
 *
 *  Copyright 2016, 2017 DarwinsDen.com
 *
 *  For button mappings, device parameter information and images, questions or to provide feedback on this device handler, 
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
 *  2.00 (03/25/2021) - Quick test update of button mappings for new ST app
 *  1.07 (02/07/2020) - Corrected git pull merge error for setLev command rate update
 *  1.06 (04/19/2019) - Added rate argument for setLevel for cloud integrations
 *  1.05 (05/06/2018) - Request dim level on hold to improve dim level status. Removed call to set switch status off on hold release.
 *  1.04 (05/04/2018) - Remove call to set switch to off when held down
 *  1.03 (11/14/2017) - Turn off firmware event log, correct physical button setting for some presses, remove 100ms delay in instant status
 *  1.02 (06/25/2017) - Pulled in @stephack's changes to include button 7/8 events when triggered remotely
 *  1.01 (01/16/2017) - Corrected advertised number of buttons (8)
 *  1.00 (01/14/2017) - Added button 7 (single tap up) and button 8 (single tap down). Added double down to 25% dim level option. 
 *  0.17 (10/05/2016) - Added single-tap-up to full brightness option.
 *  0.16 (09/24/2016) - Added double-tap-up to full brightness option and support for firmware dim rate configuration parameters.
 *  0.15 (09/06/2016) - Added Firmware version info. Removed unused lit indicator button.
 *  0.14 (08/01/2016) - Corrected 60% dim rate limit test that was inadvertently pulled into repository
 *  0.13 (06/13/2016) - Added dim level ramp-up option for remote dim commands
 *  0.12 (06/03/2016) - Added press type indicator to display last tap/hold press status
 *  0.11 (05/28/2016) - Set numberOfButtons attribute for ease of use with CoRE and other SmartApps. Corrected physical/digital states.
 *	0.10 (04/10/2016) -	Initial 0.1 Beta.
 *
 *
 *   Button Mappings:
 *
 *   ACTION          BUTTON#    BUTTON ACTION
 *   Double-Tap Up     1        pressed
 *   Double-Tap Down   2        pressed
 *   Triple-Tap Up     3        pressed
 *   Triple-Tap Down   4        pressed
 *   Hold Up           5 	    pressed
 *   Hold Down         6 	    pressed
 *   Single-Tap Up     7        pressed
 *   Single-Tap Down   8        pressed
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
       input "doubleTapToFullBright", "bool", title: "Double-Tap Up sets to full brightness",  defaultValue: false,  displayDuringSetup: true, required: false	       
       input "singleTapToFullBright", "bool", title: "Single-Tap Up sets to full brightness",  defaultValue: false,  displayDuringSetup: true, required: false	
       input "doubleTapDownToDim",    "bool", title: "Double-Tap Down sets to 25% level",      defaultValue: false,  displayDuringSetup: true, required: false	       
       input "reverseSwitch", "bool", title: "Reverse Switch",  defaultValue: false,  displayDuringSetup: true, required: false	       
        
       input ( "localStepDuration", "number", title: "Press Configuration button after entering ramp rate preferences\n\nLocal Ramp Rate: Duration of each level (1-22)(1=10ms) [default: 3]", defaultValue: 3,range: "1..22", required: false)
       input ( "localStepSize", "number", title: "Local Ramp Rate: Dim level % to change each duration (1-99) [default: 1]", defaultValue: 1, range: "1..99", required: false)
       input ( "remoteStepDuration", "number", title: "Remote Ramp Rate: Duration of each level (1-22)(1=10ms) [default: 3]", defaultValue: 3,range: "1..22", required: false)
       input ( "remoteStepSize", "number", title: "Remote Ramp Rate: Dim level % to change each duration (1-99) [default: 1]", defaultValue: 1, range: "1..99", required: false)
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
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.configure"
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
        
		details(["switch","tapUp2","tapUp3","holdUp","tapDown2","tapDown3","holdDown","level","firmwareVersion", "refresh"])
	}
}

def parse(String description) {
	def result = null
    log.debug (description)
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
	sendEvent(tapUp1Response("digital"))
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	sendEvent(tapDown1Response("digital"))
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def setLevel (value, rate=null) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
    def result = []
 
    result += response(zwave.basicV1.basicSet(value: level))
    result += response("delay 5000")
    result += response(zwave.switchMultilevelV1.switchMultilevelGet())
    result += response("delay 5000")
    result += response(zwave.switchMultilevelV1.switchMultilevelGet())
}

// Add rate argument for setLevel for cloud integrations
//def setLevel(value, duration) {
//	setLevel (value)
//}

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
                   // Press Once
                  result += createEvent(btnResponse("up", "physical"))
                  result += createEvent([name: "switch", value: "on", type: "physical"])
       
                  if (singleTapToFullBright)
                  {
                     result += setLevel(99)
                     result += response("delay 5000")
                     result += response(zwave.switchMultilevelV1.switchMultilevelGet())
                  } 
                  break
              case 1:
                  //Hold release? (inconsistent) 
                  break
              case 2:
                  // Hold
                  result += createEvent(btnResponse("up_hold", "physical")) 
                  result += response(["delay 5000", zwave.switchMultilevelV1.switchMultilevelGet().format()])
                  break
              case 3: 
                  // 2 Times
                  result += createEvent(btnResponse("up_2x", "physical"))
                  if (doubleTapToFullBright)
                  {
                     result += setLevel(99)
                     result += response("delay 5000")
                     result += response(zwave.switchMultilevelV1.switchMultilevelGet())
                  }                    
                  break
              case 4:
                  // 3 Three times
                  result = createEvent(btnResponse("up_3x", "physical"))
                  break
              default:
                  log.debug ("unexpected up press keyAttribute: $cmd.keyAttributes")
          }
          break
          
      case 2:
          // Down
          switch (cmd.keyAttributes) {
              case 0:
                  // Press Once
                  result += createEvent(btnResponse("down", "physical"))
                  result += createEvent([name: "switch", value: "off", type: "physical"]) 
                  break
              case 1:
                  //Hold release? (inconsistent) 
                  break
              case 2:
                  // Hold
                  result += createEvent(btnResponse("down_hold", "physical"))
                  result += response(["delay 5000", zwave.switchMultilevelV1.switchMultilevelGet().format()])
                  break
              case 3: 
                  // 2 Times
                  result += createEvent(btnResponse("down_2x", "physical"))
                  if (doubleTapDownToDim)
                  {
                     result += setLevel(25)
                     result += response("delay 5000")
                     result += response(zwave.switchMultilevelV1.switchMultilevelGet())
                  }  
                  break
              case 4:
                  // 3 Times
                  result = createEvent(btnResponse("down_3x", "physical"))
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

def btnResponse(action, buttonType) {
    sendEvent(name: "status", value: action)
    def eventBtn = 1 
    def eventAction = action
    [name: "button", value: eventAction, data: [buttonNumber: eventBtn], descriptionText: action, displayed: false, isStateChange: true, type: buttonType]
}

def tapUp1() {
    sendEvent(btnResponse("up", "digital"))
}

def tapDown1() {
    sendEvent(btnResponse("down", "digital"))
}

def tapUp2() {
    sendEvent(btnResponse("up_2x", "digital"))
}

def tapDown2() {
    sendEvent(btnResponse("down_2x", "digital"))
}

def tapUp3() {
    sendEvent(btnResponse("up_3x", "digital"))
}

def tapDown3() {
    sendEvent(btnResponse("down_3x", "digital"))
}

def holdUp() {
    sendEvent(btnResponse("up_hold", "digital"))
}

def holdDown() {
    sendEvent(btnResponse("down_hold", "digital"))
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
   sendEvent(name: "firmwareVersion",  value: versionInfo, isStateChange: true, displayed: false)
}

def configure() {
   log.debug ("configure() called")
   btnValues = ["down", "down_hold", "down_2x", "down_3x", "up", "up_hold", "up_2x", "up_3x"].encodeAsJSON()
   sendEvent(name: "numberOfButtons", value: 1, displayed: false)
   sendEvent(name: "supportedButtonValues", value: btnValues, displayed: false)

   sendEvent(name: "numberOfButtons", value: 8, displayed: false)
   def commands = []
   commands << setDimRatePrefs()
   commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
   commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
   commands << zwave.versionV1.versionGet().format()
   delayBetween(commands,500)
    
    
}

def setDimRatePrefs() 
{
   log.debug ("set prefs")
   def cmds = []

   if (remoteStepSize)
   {
      def remoteStepSize = Math.max(Math.min(remoteStepSize, 99), 1)
      cmds << zwave.configurationV2.configurationSet(configurationValue: [remoteStepSize], parameterNumber: 7, size: 1).format()
   }
   
   if (remoteStepDuration)
   {
       def remoteStepDuration = Math.max(Math.min(remoteStepDuration, 22), 2)
       cmds << zwave.configurationV2.configurationSet(configurationValue: [0, remoteStepDuration], parameterNumber: 8, size: 2).format()
   }
   
   
   if (localStepSize)
   {
      def localStepSize = Math.max(Math.min(localStepSize, 99), 1)
      cmds << zwave.configurationV2.configurationSet(configurationValue: [localStepSize], parameterNumber: 9, size: 1).format()
   }
   
   if (localStepDuration)
   {
      def localStepDuration = Math.max(Math.min(localStepDuration, 22), 2)
      cmds << zwave.configurationV2.configurationSet(configurationValue: [0,localStepDuration], parameterNumber: 10, size: 2).format()
   }
   
   if (reverseSwitch)
   {
       cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
   }
   else
   {
      cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
   }
   
   //Enable the following configuration gets to verify configuration in the logs
   //cmds << zwave.configurationV1.configurationGet(parameterNumber: 7).format()
   //cmds << zwave.configurationV1.configurationGet(parameterNumber: 8).format()
   //cmds << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
   //cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format()
   
   return cmds
}
 
def updated()
{
 def cmds= []
 cmds << setDimRatePrefs
 delayBetween(cmds, 500)
}
