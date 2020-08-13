/**
 *  HomeSeer HS-WS200+
 *
 *  Copyright 2018 HomeSeer
 *
 *  Modified from the work by DarwinsDen device handler for the WD100 version 1.03
 *
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
 *	Author: HomeSeer
 *	Date: 12/2017
 *
 *	Changelog:
 *
 *	1.0	Initial Version
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
 *   4 taps up         9        pressed
 *   4 taps down       10       pressed
 *   5 taps up         11       pressed
 *   5 taps down       12       pressed
 *
 */
 
metadata {
	definition (name: "WS200+ Switch", namespace: "homeseer", author: "support@homeseer.com") {
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
        command "tapUp4"
        command "tapDown4"
        command "tapUp5"
        command "tapDown5"
        command "holdUp"
        command "holdDown"
        command "setStatusLed"
        command "setSwitchModeNormal"
        command "setSwitchModeStatus"
        command "setDefaultColor"
        
        fingerprint mfr: "000C", prod: "4447", model: "3035"
}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"		

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"		
	}

    preferences {      
       input "doubleTapToFullBright", "bool", title: "Double-Tap Up sets to full brightness",  defaultValue: false,  displayDuringSetup: true, required: false	       
       input "singleTapToFullBright", "bool", title: "Single-Tap Up sets to full brightness",  defaultValue: false,  displayDuringSetup: true, required: false	
       input "doubleTapDownToDim",    "bool", title: "Double-Tap Down sets to 25% level",      defaultValue: false,  displayDuringSetup: true, required: false	       
       input "reverseSwitch", "bool", title: "Reverse Switch",  defaultValue: false,  displayDuringSetup: true, required: false
       input "bottomled", "bool", title: "Bottom LED On if Load is Off",  defaultValue: false,  displayDuringSetup: true, required: false              
       input ( "color", "enum", title: "Default LED Color", options: ["White", "Red", "Green", "Blue", "Magenta", "Yellow", "Cyan"], description: "Select Color", required: false)
    }
    
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"				
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
        
        standardTile("tapUp4", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Tap ▲▲▲▲", backgroundColor: "#ffffff", action: "tapUp4", icon: "st.Home.home30"
		} 

        standardTile("tapDown4", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Tap ▼▼▼▼", backgroundColor: "#ffffff", action: "tapDown4", icon: "st.Home.home30"
		} 
        
        standardTile("tapUp5", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Tap ▲▲▲▲▲", backgroundColor: "#ffffff", action: "tapUp5", icon: "st.Home.home30"
		} 

        standardTile("tapDown5", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Tap ▼▼▼▼▼", backgroundColor: "#ffffff", action: "tapDown5", icon: "st.Home.home30"
		} 

        standardTile("holdUp", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Hold ▲", backgroundColor: "#ffffff", action: "holdUp", icon: "st.Home.home30"
		} 

        standardTile("holdDown", "device.button", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Hold ▼", backgroundColor: "#ffffff", action: "holdDown", icon: "st.Home.home30"
        }
        
		main(["switch"])
        
		details(["switch","tapUp2","tapUp3","tapUp4","tapUp5","holdUp","tapDown2","tapDown3","tapDown4","tapDown5","holdDown","firmwareVersion", "refresh"])
	}
}

def parse(String description) {
	def result = null
    log.debug (description)
    if (description != "updated") {
	    def cmd = zwave.parse(description, [0x20: 1, 0x70: 1])	
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
	createEvent([name: "switch", value: cmd.value ? "on" : "off", type: "physical"])
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	createEvent([name: "switch", value: cmd.value ? "on" : "off", type: "physical"])
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	createEvent([name: "switch", value: cmd.value ? "on" : "off", type: "digital"])
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
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def off() {
	sendEvent(tapDown1Response("digital"))
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}


/*
 *  Set dimmer to status mode, then set the color of the individual LED
 *
 *  led = 1-7
 *  color = 0=0ff
 *          1=red
 *          2=green
 *          3=blue
 *          4=magenta
 *          5=yellow
 *          6=cyan
 *          7=white
 */
def setStatusLed (led,color,blink) {    
    def cmds= []
    
    if(state.statusled1==null) {    	
    	state.statusled1=0        
        state.blinkval=0
    }
    
    /* set led # and color */
    switch(led) {
    	case 1:
        	state.statusled1=color
            break
        
    }
    
    
    if(state.statusled1==0)
    {
    	// no LEDS are set, put back to NORMAL mode
        cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 13, size: 1).format() 
    }
    else
    {
    	// at least one LED is set, put to status mode
        cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 13, size: 1).format()
        // set the LED to color
        cmds << zwave.configurationV2.configurationSet(configurationValue: [color], parameterNumber: 21, size: 1).format()
        // check if LED should be blinking
        def blinkval = state.blinkval
        if(blink)
        {
            // set blink speed, also enables blink, 1=100ms blink frequency
            cmds << zwave.configurationV2.configurationSet(configurationValue: [5], parameterNumber: 31, size: 1).format()
            state.blinkval = blinkval
        }
        else
        {
            cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 31, size: 1).format()
            state.blinkval = blinkval
        }
    }
 	delayBetween(cmds, 500)
}

/*
 * Set switch to Normal mode (exit status mode)
 *
 */
def setSwitchModeNormal() {
	def cmds= []
    cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 13, size: 1).format()
    delayBetween(cmds, 500)
}

/*
 * Set switch to Status mode (exit normal mode)
 *
 */
def setSwitchModeStatus() {
	def cmds= []
    cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 13, size: 1).format()
    delayBetween(cmds, 500)
}

/*
 * Set the color of the LEDS for normal dimming mode, shows the current dim level
 */
def setDefaultColor(color) {
	def cmds= []
    cmds << zwave.configurationV2.configurationSet(configurationValue: [color], parameterNumber: 14, size: 1).format()
    delayBetween(cmds, 500)
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
                   // Press Once
                  result += createEvent(tapUp1Response("physical"))  
                  result += createEvent([name: "switch", value: "on", type: "physical"])   
                  break
              case 1:
                  result=createEvent([name: "switch", value: "on", type: "physical"])
                  break
              case 2:
                  // Hold
                  result += createEvent(holdUpResponse("physical"))  
                  result += createEvent([name: "switch", value: "on", type: "physical"])    
                  break
              case 3: 
                  // 2 Times
                  result +=createEvent(tapUp2Response("physical"))                                  
                  break
              case 4:
                  // 3 times
                  result=createEvent(tapUp3Response("physical"))
                  break
              case 5:
                  // 4 times
                  result=createEvent(tapUp4Response("physical"))
                  break
              case 6:
                  // 5 times
                  result=createEvent(tapUp5Response("physical"))
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
                  result += createEvent(tapDown1Response("physical"))
                  result += createEvent([name: "switch", value: "off", type: "physical"]) 
                  break
              case 1:
                  result=createEvent([name: "switch", value: "off", type: "physical"])
                  break
              case 2:
                  // Hold
                  result += createEvent(holdDownResponse("physical"))
                  result += createEvent([name: "switch", value: "off", type: "physical"]) 
                  break
              case 3: 
                  // 2 Times
                  result+=createEvent(tapDown2Response("physical"))
                  if (doubleTapDownToDim)
                  {
                     result += setLevel(25)
                     result += response("delay 5000")
                     result += response(zwave.switchMultilevelV1.switchMultilevelGet())
                  }  
                  break
              case 4:
                  // 3 Times
                  result=createEvent(tapDown3Response("physical"))
                  break
              case 5:
                  // 4 Times
                  result=createEvent(tapDown4Response("physical"))
                  break
              case 6:
                  // 5 Times
                  result=createEvent(tapDown5Response("physical"))
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

def tapUp1Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▲")
	[name: "button", value: "pushed", data: [buttonNumber: "7"], descriptionText: "$device.displayName Tap-Up-1 (button 7) pressed", 
       isStateChange: true, type: "$buttonType"]
}

def tapDown1Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▼")
	[name: "button", value: "pushed", data: [buttonNumber: "8"], descriptionText: "$device.displayName Tap-Down-1 (button 8) pressed", 
      isStateChange: true, type: "$buttonType"]
}

def tapUp2Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▲▲")
	[name: "button", value: "pushed", data: [buttonNumber: "1"], descriptionText: "$device.displayName Tap-Up-2 (button 1) pressed", 
       isStateChange: true, type: "$buttonType"]
}

def tapDown2Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▼▼")
	[name: "button", value: "pushed", data: [buttonNumber: "2"], descriptionText: "$device.displayName Tap-Down-2 (button 2) pressed", 
      isStateChange: true, type: "$buttonType"]
}

def tapUp3Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▲▲▲")
	[name: "button", value: "pushed", data: [buttonNumber: "3"], descriptionText: "$device.displayName Tap-Up-3 (button 3) pressed", 
    isStateChange: true, type: "$buttonType"]
}

def tapUp4Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▲▲▲▲")
	[name: "button", value: "pushed", data: [buttonNumber: "9"], descriptionText: "$device.displayName Tap-Up-4 (button 9) pressed", 
    isStateChange: true, type: "$buttonType"]
}

def tapUp5Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▲▲▲▲▲")
	[name: "button", value: "pushed", data: [buttonNumber: "11"], descriptionText: "$device.displayName Tap-Up-5 (button 11) pressed", 
    isStateChange: true, type: "$buttonType"]
}

def tapDown3Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▼▼▼")
	[name: "button", value: "pushed", data: [buttonNumber: "4"], descriptionText: "$device.displayName Tap-Down-3 (button 4) pressed", 
    isStateChange: true, type: "$buttonType"]
}

def tapDown4Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▼▼▼▼")
	[name: "button", value: "pushed", data: [buttonNumber: "10"], descriptionText: "$device.displayName Tap-Down-3 (button 10) pressed", 
    isStateChange: true, type: "$buttonType"]
}

def tapDown5Response(String buttonType) {
    sendEvent(name: "status" , value: "Tap ▼▼▼▼▼")
	[name: "button", value: "pushed", data: [buttonNumber: "12"], descriptionText: "$device.displayName Tap-Down-3 (button 12) pressed", 
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

def tapUp4() {
	sendEvent(tapUp4Response("digital"))
}

def tapDown4() {
	sendEvent(tapDown4Response("digital"))
}

def tapUp5() {
	sendEvent(tapUp5Response("digital"))
}

def tapDown5() {
	sendEvent(tapDown5Response("digital"))
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
   sendEvent(name: "firmwareVersion",  value: versionInfo, isStateChange: true, displayed: false)
}

def configure() {
   log.debug ("configure() called")
 
   sendEvent(name: "numberOfButtons", value: 12, displayed: false)
   def commands = []
   commands << setDimRatePrefs()   
   commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
   commands << zwave.versionV1.versionGet().format()
   delayBetween(commands,500)
}

def setDimRatePrefs() 
{
   log.debug ("set prefs")
   def cmds = []

	if (color)
    {
        switch (color) {
        	case "White":
            	cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 14, size: 1).format()
                break
      		case "Red":
            	cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 14, size: 1).format()
                break
            case "Green":
            	cmds << zwave.configurationV2.configurationSet(configurationValue: [2], parameterNumber: 14, size: 1).format()
                break
            case "Blue":
            	cmds << zwave.configurationV2.configurationSet(configurationValue: [3], parameterNumber: 14, size: 1).format()
                break
            case "Magenta":
            	cmds << zwave.configurationV2.configurationSet(configurationValue: [4], parameterNumber: 14, size: 1).format()
                break
            case "Yellow":
            	cmds << zwave.configurationV2.configurationSet(configurationValue: [5], parameterNumber: 14, size: 1).format()
                break
            case "Cyan":
            	cmds << zwave.configurationV2.configurationSet(configurationValue: [6], parameterNumber: 14, size: 1).format()
                break
            
            
      	}
    }    
   
      
   if (reverseSwitch)
   {
       cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
   }
   else
   {
      cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
   }
   
   if (bottomled)
   {
       cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()
   }
   else
   {
      cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
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