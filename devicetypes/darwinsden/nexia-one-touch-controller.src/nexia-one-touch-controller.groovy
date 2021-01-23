/**
 *  Nexia NX1000 One Touch Controller
 *
 *  For device information and images, questions or to provide feedback on this device handler, 
 *  please visit: 
 *
 *      darwinsden.com/nexia-nx1000
 *
 *  Copyright 2016 DarwinsDen.com
 *
 *  This device handler configures all of the One Touch buttons as momentary buttons
 *  Both pushed and held states are supported.
 *  Naming of the buttons is not currently supported
 * 
 *  Press and hold the middle page button on the bottom of the Nexia One Touch controller to access Z-Wave include/exclude options
 * 
 *  Note: If button presses are not working after pairing the One Touch with the Smarthings Hub, access the Z-Wave page on the One Touch
 *        again after pairing. This usually corrects the issue.
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
 *	Date: 2016-05-22
 *
 *	Changelog:
 *
 *	0.10 (05/22/2016) -	Initial 0.1 Beta
 *  0.11 (05/28/2016) - Set numberOfButtons attribute for ease of use with CoRE and other SmartApps
 *  0.12 (12/15/2016) - Incorporated C&E T's changes as-is to set labels on device. See below: 
 *    C&E T: 
 *     - implemented methods to set the labels on the device 
 *         -> Labels are set when you press configure while the device is awake.
 *            Aftern pressing the configure button BE PATIENT! DON'T touch the device buttons for about 20-30 seconds
 *     - added ", data: [buttonNumber: button]" to button released event
 *
 */
 metadata {
  
  definition (name: "Nexia One Touch Controller", namespace: "darwinsden", author: "darwin@darwinsden.com") {
        capability "Actuator"
        capability "Button"
        capability "Configuration"
        capability "Indicator"
        capability "Sensor"
        
        command "button1"
        command "button2"
        command "button3"
        command "button4"
        command "button5"
        command "button6"
        command "button7"
        command "button8"
        command "button9"
        command "button10"
        command "button11"
        command "button12"
        command "button13"
        command "button14"
        command "button15"

        fingerprint deviceId: "0x1801", inClusters: "0x5E, 0x85, 0x59, 0x80, 0x5B, 0x70, 0x5A, 0x7A, 0x72, 0x8F, 0x73, 0x2D, 0x93, 0x92, 0x86, 0x84"
  }

  simulator {
    status "button 1 pushed":   "command: 5B03, payload: 00 00 01"    
    status "button 2 pushed":   "command: 5B03, payload: 00 00 02" 
    status "button 3 pushed":   "command: 5B03, payload: 00 00 03" 
    status "button 4 pushed":   "command: 5B03, payload: 00 00 04" 
    status "button 5 pushed":   "command: 5B03, payload: 00 00 05" 
    status "button 6 pushed":   "command: 5B03, payload: 00 00 06" 
    status "button 7 pushed":   "command: 5B03, payload: 00 00 07" 
    status "button 8 pushed":   "command: 5B03, payload: 00 00 08" 
    status "button 9 pushed":   "command: 5B03, payload: 00 00 09" 
    status "button 10 pushed":  "command: 5B03, payload: 00 00 0A" 
    status "button 11 pushed":  "command: 5B03, payload: 00 00 0B"  
    status "button 12 pushed":  "command: 5B03, payload: 00 00 0C" 
    status "button 13 pushed":  "command: 5B03, payload: 00 00 0D"
    status "button 14 pushed":  "command: 5B03, payload: 00 00 0E"
    status "button 15 pushed":  "command: 5B03, payload: 00 00 0F"
  }

  preferences {
		input "buttonLabel1",  "string", title: "Button 1 Label",  defaultValue: "Button 1",  displayDuringSetup: true, required: false	
        input "buttonLabel2",  "string", title: "Button 2 Label",  defaultValue: "Button 2",  displayDuringSetup: true, required: false	
        input "buttonLabel3",  "string", title: "Button 3 Label",  defaultValue: "Button 3",  displayDuringSetup: true, required: false	
        input "buttonLabel4",  "string", title: "Button 4 Label",  defaultValue: "Button 4",  displayDuringSetup: true, required: false	
        input "buttonLabel5",  "string", title: "Button 5 Label",  defaultValue: "Button 5",  displayDuringSetup: true, required: false	
		input "buttonLabel6",  "string", title: "Button 6 Label",  defaultValue: "Button 6",  displayDuringSetup: true, required: false	
        input "buttonLabel7",  "string", title: "Button 7 Label",  defaultValue: "Button 7",  displayDuringSetup: true, required: false	
        input "buttonLabel8",  "string", title: "Button 8 Label",  defaultValue: "Button 8",  displayDuringSetup: true, required: false	
        input "buttonLabel9",  "string", title: "Button 9 Label",  defaultValue: "Button 9",  displayDuringSetup: true, required: false	
        input "buttonLabel10", "string", title: "Button 10 Label", defaultValue: "Button 10", displayDuringSetup: true, required: false	
		input "buttonLabel11", "string", title: "Button 11 Label", defaultValue: "Button 11", displayDuringSetup: true, required: false	
        input "buttonLabel12", "string", title: "Button 12 Label", defaultValue: "Button 12", displayDuringSetup: true, required: false	
        input "buttonLabel13", "string", title: "Button 13 Label", defaultValue: "Button 13", displayDuringSetup: true, required: false	
        input "buttonLabel14", "string", title: "Button 14 Label", defaultValue: "Button 14", displayDuringSetup: true, required: false	
        input "buttonLabel15", "string", title: "Button 15 Label", defaultValue: "Button 15", displayDuringSetup: true, required: false	
  }	

  tiles (scale: 2) {
  
  
    	valueTile("ok", "device.buttonNum", width: 2, height: 2) {
			state("", label:'${currentValue}',
				backgroundColor: "#79b821"
			)
		}

   valueTile("btn1", "device.label1", width:2, height: 1, decoration: "flat", inactiveLabel: false) {
			state "default", label: '${currentValue}', action: "button1"
		}
   valueTile("btn2", "device.label2", width:2, height: 1, decoration: "flat", inactiveLabel: false) {
			state "default", label: '${currentValue}', action: "button2"
		}
   valueTile("btn3", "device.label3", width:2, height: 1, decoration: "flat", inactiveLabel: false) {
			state "default", label: '${currentValue}', action: "button3"
		}        
   valueTile("btn4", "device.label4", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button4"
		}     
   valueTile("btn5", "device.label5", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button5"
		}       
   valueTile("btn6", "device.label6", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button6"
		} 
   valueTile("btn7", "device.label7", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button7"
		}     
   valueTile("btn8", "device.label8", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button8"
		}  
   valueTile("btn9", "device.label9", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button9"
		} 
   valueTile("btn10", "device.label10", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button10"
		} 
   valueTile("btn11", "device.label11", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button11"
		} 
   valueTile("btn12", "device.label12", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button12"
		} 
   valueTile("btn13", "device.label13", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button13"
		} 
   valueTile("btn14", "device.label14", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button14"
		} 
   valueTile("btn15", "device.label15", width: 2, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "button15"
		} 

   valueTile("battery1", "device.battery", width: 2, height: 2, decoration: "flat", inactiveLabel: false) {
			state "default", label:'${currentValue}% battery', unit:""
		}

    standardTile("configure", "device.configure", width: 2, height: 2 ,inactiveLabel: false, decoration: "flat") {
      state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
    }

    main "ok"
    details (["ok", "battery1", "configure", "btn1", "btn2", "btn3","btn4","btn5","btn6","btn7","btn8","btn9","btn10",
              "btn11","btn12","btn13","btn14","btn15"])
  }
}

// parse events into attributes
def parse(String description) {
  log.debug "Parsing '${description}'"

  def result = null
  def cmd = zwave.parse(description, [0x92: 1, 0x93: 1])
  if (cmd) {
    result = zwaveEvent(cmd)
  }
  return result
}

// Handle a button being pressed
def buttonEvent(button, pressType) {
   button = button as Integer
   def result = null
  
   switch (pressType) {
      case 0:
          // Pressed
          log.debug ("button: $button pressed")
          result = [name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true]
          break
          
      case 1:
          // Released (inconsistently received)
          log.debug ("button: $button released")
          // C&E T: added ", data: [buttonNumber: button]"
          result = [name: "button", value: "default", data: [buttonNumber: button], descriptionText: "$device.displayName button was released", isStateChange: true] 
          break
          
      case 2: 
          // held (hand potential multiple signals) 
          if (state.lastButton == button && (state.repeatCount < 4) && (now() - state.repeatStart < 2000)) {
              log.debug "Ignoring button ${button} hold repeat ${state.repeatCount}x ${now()}"
              state.repeatCount = state.repeatCount + 1
              //result = [:]
          }
          else {
              // This is the first detected hold. 
              log.debug ("button: $button held")
              state.lastButton = button
              state.repeatCount = 0
              state.repeatStart = now()
              result = [name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true]
          }
          break

      default:
           // unexpected case
           log.debug ("unexpected press type: $pressType")
   }   
   result
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
  log.debug ("Unexpected scene activationset received")
  log.debug (cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactuatorconfv1.SceneActuatorConfGet cmd) {
  log.debug ("Unexpected scene actuator config command received")
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    def result = []
    //Set the button number in the device handler GUI
    result << createEvent(name: "buttonNum", value: "$cmd.sceneNumber", data: [buttonNumber: cmd.sceneNumber], isStateChange: true)
    result << createEvent(buttonEvent(cmd.sceneNumber, cmd.keyAttributes))
}

// The controller sent a scene activation report.  Log it, but it really shouldn't happen.
def zwaveEvent(physicalgraph.zwave.commands.sceneactuatorconfv1.SceneActuatorConfReport cmd) {
   log.debug "Unexpected Scene activation report Received"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
  log.debug ("Unexpected config report Received")
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
  log.debug ("Unexpected hail received")
}

def zwaveEvent (physicalgraph.zwave.commands.screenattributesv1.ScreenAttributesReport cmd) {
  log.debug ("Unexpected Screen Attribute V1 Report Received")
}

def zwaveEvent (physicalgraph.zwave.commands.screenmdv1.ScreenMdReport cmd) {
  log.debug ("Unexpected Screen MD Report V1 Received")
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    log.debug ("Received Wakeup notification")
    def result = []
    result << createEvent(descriptionText: "${device.displayName} woke up", displayed: false)
	if (!isDuplicateCall(state.lastWokeUp, 1)) {
		state.lastwokeUp = new Date().time		
		result << response(configureOld()) // C&E T -> see comment on method configureOld
	}
    result << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format()) 
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
       def result = null
       state.lastBatteryReport = new Date().time
       def batteryLevel = cmd.batteryLevel
       log.info("Battery: $batteryLevel")
       result = createEvent(name: "battery", value: batteryLevel, unit: "%", descriptionText: "Battery%: $batteryLevel", isStateChange: true)   
       return result
}

// Update manufacturer information when it is reported
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
   log.debug ("manufacturer specific report recieved")
   log.debug (cmd)
   if (state.manufacturer != cmd.manufacturerName) {
     updateDataValue("manufacturer", cmd.manufacturerName)
   }
     createEvent([:])
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
   def response = []
   log.debug ("Unexpected association Groupings Report Received")
}

// Log unexpected Z-Wave commands
def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug ("unknown Command Recieved")
    log.debug (cmd)
    createEvent([:])
}

def setButtonLabels() {

    //Initialize button labels 
    if (!state.buttonLabelsInitialized)
    {
       state.buttonLabelsInitialized=true
       state.buttonLabels=["Button 1", "Button 2",  "Button 3",  "Button 4",  "Button 5",  "Button 6",  "Button 7", "Button 8", 
                           "Button 9", "Button 10", "Button 11", "Button 12", "Button 13", "Button 14", "Button 15"]
    }
 
    // set the button labels based on preferences
    if (buttonLabel1 != null) {
       state.buttonLabels[0]=buttonLabel1
    }
    if (buttonLabel2 != null) {
       state.buttonLabels[1]=buttonLabel2
    }
    if (buttonLabel3 != null) {
       state.buttonLabels[2]=buttonLabel3
    }
    if (buttonLabel4 != null) {
       state.buttonLabels[3]=buttonLabel4
    }
    if (buttonLabel5 != null) {
       state.buttonLabels[4]=buttonLabel5
    }
    if (buttonLabel6 != null) {
       state.buttonLabels[5]=buttonLabel6
    }
    if (buttonLabel7 != null) {
       state.buttonLabels[6]=buttonLabel7
    }
    if (buttonLabel8 != null) {
       state.buttonLabels[7]=buttonLabel8
    }
    if (buttonLabel9 != null) {
       state.buttonLabels[8]=buttonLabel9
    }
    if (buttonLabel7 != null) {
       state.buttonLabels[6]=buttonLabel7
    }
    if (buttonLabel8 != null) {
       state.buttonLabels[7]=buttonLabel8
    }
    if (buttonLabel9 != null) {
       state.buttonLabels[8]=buttonLabel9
    }
    if (buttonLabel10 != null) {
       state.buttonLabels[9]=buttonLabel10
    }
    if (buttonLabel11 != null) {
       state.buttonLabels[10]=buttonLabel11
    }
    if (buttonLabel12 != null) {
       state.buttonLabels[11]=buttonLabel12
    }
    if (buttonLabel13 != null) {
       state.buttonLabels[12]=buttonLabel13
    }
    if (buttonLabel14 != null) {
       state.buttonLabels[13]=buttonLabel14
    }
    if (buttonLabel15 != null) {
       state.buttonLabels[14]=buttonLabel15
    }

    //send the button labels
    sendEvent(name: "label1",  value: state.buttonLabels[0],  isStateChange: true)
    sendEvent(name: "label2",  value: state.buttonLabels[1],  isStateChange: true)
    sendEvent(name: "label3",  value: state.buttonLabels[2],  isStateChange: true)
    sendEvent(name: "label4",  value: state.buttonLabels[3],  isStateChange: true)
    sendEvent(name: "label5",  value: state.buttonLabels[4],  isStateChange: true)
    sendEvent(name: "label6",  value: state.buttonLabels[5],  isStateChange: true)
    sendEvent(name: "label7",  value: state.buttonLabels[6],  isStateChange: true)
    sendEvent(name: "label8",  value: state.buttonLabels[7],  isStateChange: true)
    sendEvent(name: "label9",  value: state.buttonLabels[8],  isStateChange: true)
    sendEvent(name: "label10", value: state.buttonLabels[9],  isStateChange: true)
    sendEvent(name: "label11", value: state.buttonLabels[10], isStateChange: true)
    sendEvent(name: "label12", value: state.buttonLabels[11], isStateChange: true)
    sendEvent(name: "label13", value: state.buttonLabels[12], isStateChange: true)
    sendEvent(name: "label14", value: state.buttonLabels[13], isStateChange: true)
    sendEvent(name: "label15", value: state.buttonLabels[14], isStateChange: true)
    
    sendEvent(name: "numberOfButtons", value: 15, displayed: false)
}

// Configure the device button types and corresponding scene numbers
def configurationCmds() {
    def commands = []
  
    // Loop through all the buttons on the controller
    for (def buttonNum = 1; buttonNum <= 15; buttonNum++) {  
       // set a unique corresponding scene for each button
       commands << zwave.sceneControllerConfV1.sceneControllerConfSet(groupId: buttonNum, sceneId: buttonNum).format()
       // set configuration for each button to zero (scene control momontary)
       commands << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: buttonNum + 1, size: 1).format()
    }

    commands << zwave.batteryV1.batteryGet().format()
    commands << associateHub()
       
    setButtonLabels()
    
    delayBetween(commands)
}

// Configure the device
def configure() {
    def cmd=configurationCmds()
    //cmd << "delay 100"
    
    // C&E T: set button texts on device
    for (def buttonNum = 1; buttonNum <= 15; buttonNum++) {  
        cmd << createCommandToSetButtonLabel(buttonNum-1, device.currentValue("label"+buttonNum))
        cmd << "delay 1200" // using such a long delay seems to be the safest way to go
    }
    
    log.debug("Sending configuration: ${cmd}")
    return cmd
}

// C&E T
// Old configure() method to use in wakeUp call 
//   It seems to be a bad idea to send all label texts to the device on every wake up notification -> crashed our hub
def configureOld() {
    def cmd=configurationCmds()
    log.debug("Sending configuration: ${cmd}")
    return cmd
}

// Associate the hub with each button on the device
def associateHub() {
    def commands = []

    // Loop through all the buttons on the controller
    for (def buttonNum = 16; buttonNum <= 31; buttonNum++) {  //2-15 needed?

          // Associate the hub with the button so we will get status updates
          commands << zwave.associationV1.associationSet(groupingIdentifier: buttonNum, nodeId: zwaveHubNodeId).format() 
    }
    return commands
}

private isDuplicateCall(lastRun, allowedEverySeconds) {
	def result = false
	if (lastRun) {
		result =((new Date().time) - lastRun) < (allowedEverySeconds * 1000)
	}
	result
}

def updated() {
	if (!isDuplicateCall(state.lastUpdated, 1)) {
		state.lastUpdated = new Date().time		
		return response(configure())
	}
}

def sendVirtualButtonEvent(button) {
	sendEvent(buttonEvent(button, 0))
    sendEvent(name: "buttonNum", value: "$button", data: [buttonNumber: button], isStateChange: true)
}

def button1() {
	sendVirtualButtonEvent(1)
}

def button2() {
	sendVirtualButtonEvent(2)
}

def button3() {
	sendVirtualButtonEvent(3)
}

def button4() {
	sendVirtualButtonEvent(4)
}

def button5() {
	sendVirtualButtonEvent(5)
}

def button6() {
	sendVirtualButtonEvent(6)
}

def button7() {
	sendVirtualButtonEvent(7)
}

def button8() {
	sendVirtualButtonEvent(8)
}

def button9() {
	sendVirtualButtonEvent(9)
}

def button10() {
	sendVirtualButtonEvent(10)
}

def button11() {
	sendVirtualButtonEvent(11)
}

def button12() {
	sendVirtualButtonEvent(12)
}

def button13() {
	sendVirtualButtonEvent(13)
}

def button14() {
	sendVirtualButtonEvent(14)
}

def button15() {
	sendVirtualButtonEvent(15)
}

// C&E T
// Creates a Screen Meta Data Report command for one button label
//   lineNumber = the number of the line 0..14
//   text = the text to put on the button
// Some default settings are applied (normal font, clear line, ASCII+OEM chars, charPos=0)
def createCommandToSetButtonLabel(lineNumber, text) {
	def command = ""

    if (null == text) {
    	log.error "createCommandToSetButtonLabel: text == null"
    }
    else if (lineNumber < 0) {
    	log.error "createCommandToSetButtonLabel: lineNumber < 0"
    }
    else if (lineNumber > 14) {
    	log.error "createCommandToSetButtonLabel: lineNumber > 14"
    }
    else {
        // Command structure - see http://z-wave.sigmadesigns.com/wp-content/uploads/2016/08/SDS12652-13-Z-Wave-Command-Class-Specification-N-Z.pdf
        //   "92" // COMMAND_CLASS_SCREEN_MD
        //   "02" // SCREEN_MD_REPORT
        //   "3B" // MoreData=0, Reserved=0, ScreenSettings=7 (3 bit, 7=keep current content), CharacterEncoding=1 (3 bit, ASCII+OEM)
        //   "1"  // LineSettings=0 (3 bit, 0=selected font), Clear=1 (1 bit, 1=clear line) 
        //   "X"  // LineNumber (hex, 4 bit)
        //   "00" // CharacterPosition (hex 8 bit)
        //   "XX" // NumberOfCharacters (hex 8 bit)
        //   "XX" // Character (hex ASCII)
        //   "XX" // Character (hex ASCII)
        // NOTE: Theoretically you can send more than one line in one screenMdReport command.
        //   Hoever, the zwave documentation says that the size of the payload SHOULD NOT be bigger than 48 bytes.
        //   That basically limits us to about 2 words at once. 
        //   So, it's easier to send one button label at a time.

        def screenReportHeader = "92"+"02"+"3B"
        def lineSettings = "1"
        def lineNumberString = Integer.toHexString(lineNumber)
        //log.debug "createCommandToSetButtonLabel: lineNumberString: ${lineNumberString}"
        def characterPositionString = "00"
        def label = convertTextToHexCodeSequence(text)
        def numOfChars = label.length()/2 // after conversion to hex each character is represented by two characters
        def numOfCharString = Integer.toHexString(numOfChars.intValue())
        // numOfCharString could have one char only but we need 2
        if (numOfCharString.length() == 1) {
            numOfCharString = "0" + numOfCharString
        }
        //log.debug "createCommandToSetButtonLabel: numOfCharString: ${numOfCharString}"

        command = screenReportHeader + lineSettings + lineNumberString + characterPositionString + numOfCharString + label
        //log.debug "createCommandToSetButtonLabel: command: ${command}"
    }
    
    return command
}

// C&E T
// Converts a text to a string with hex codes for the button label command
// includes a CR ofter the 8th character and cuts after the 16th
// e.g. "ABCDEFGHIJKLMNOPQRST" -> "ABCDEFGH\rIJKLMNOP" -> "41424344454647480D494A4B4C4D4E4F50"
def convertTextToHexCodeSequence(String text) {
    //log.debug "convertTextToHexCodeSequence: Input: ${text}"
    def textLength = text.length()
    
    if (textLength > 8) {
    	// cut to max 16 characters
    	if (textLength > 16) {
    		text = text.substring(0, 16)
            textLength = text.length()
        }
        // add CR sign
        text = text.substring(0, 8) + "\r" + text.substring(8, textLength);
    }
    
    // convert to hexCode string
    text = text.encodeAsHex().toString()
    
    //log.debug "convertTextToHexCodeSequence: Output: ${text}"
    
    return text
}
