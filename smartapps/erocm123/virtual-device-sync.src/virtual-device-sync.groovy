/**
 *  Virtual Device Sync
 *
 *  Copyright 2016 Eric Maycock (erocm123)
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
 */
 
definition(
   name: "Virtual Device Sync",
   namespace: "erocm123",
   author: "Eric Maycock",
   description: "Creates virtual devices and keeps them in sync with the selected physical device. Inteded to be used with devices with multiple relays, SmartLife RGBW, and Fibaro RGBW.",
   category: "Convenience",
   iconUrl: "https://github.com/erocm123/SmartThingsPublic/raw/master/smartapps/erocm123/virtual-device-sync.src/virtual-device-sync.png",
   iconX2Url: "https://github.com/erocm123/SmartThingsPublic/raw/master/smartapps/erocm123/virtual-device-sync.src/virtual-device-sync-2x.png",
   iconX3Url: "https://github.com/erocm123/SmartThingsPublic/raw/master/smartapps/erocm123/virtual-device-sync.src/virtual-device-sync-3x.png"
)

preferences {
    page(name: "setupPage")
    page(name: "createVirtual")
    page(name: "removeVirtual")
    page(name: "removalPage")
    page(name: "createPage")
}

def setupPage() {
    dynamicPage(name: "setupPage", install: true, uninstall: true) {
    section { 
        if(!isVirtualConfigured()){
           input "physical", "capability.switch", title: "Which Physical Switch", multiple: false, required: true, submitOnChange: true
           if(physical != null){
              paragraph "Device Handler: $physical.typeName\r\n\r\nDetected Number of Endpoints: ${getEndpoints()}\r\n\r\nRecommended Type: ${getType()}"
              input "virtualSwitchType", "enum", title: "Virtual Switch Type", value: getType() , multiple: false, required: true, options: ["Switch","Energy Switch","Dimmer"]
              app.updateSetting("virtualSwitchType", getType())
           }
           href "createVirtual", title:"Create Virtual Devices", description:"Create virtual devices"
        }else{
           def switchNames = ""
           getChildDevices().each {
               switchNames = switchNames + it.displayName + "\r\n"
           }
           paragraph "Chosen Device: $physical\r\n\r\nTo change to a different device, please remove the virtual devices below."
           paragraph "Device Handler: $physical.typeName\r\n\r\nDetected Number of Endpoints: ${getEndpoints()}\r\n\r\nRecommended Type: ${getType()}\r\n\r\nVirtual Switches have been created. They will be kept in sync with the physical switch chosen above\r\n\r\n$switchNames"
           href "removeVirtual", title:"Remove Virtual Devices", description:"Remove virtual devices"
        }
    }
    section([title:"Available Options", mobileOnly:true]) {
            input "setLabel", "boolean", title: "Change the default name of the app?", required: false, submitOnChange: true, value: false
            if (settings.setLabel != null && setLabel.toBoolean() == true) {
               label title:"Assign a name for your app (optional)", required:false
            } 
		}
    }
}

def createVirtual(){
   dynamicPage(name: "createVirtual", title: "Associate your device's endpoints with virtual devices", nextPage: "createPage") {
		section {
			paragraph "This process will create virtual devices and associate them with the endpoints on your physical device."
            def switchNames = ""
            for (int i = 1; i <= getEndpoints(); i++){
               
               if ((physical.typeName.toUpperCase().indexOf("FIBARO") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0) ||
               (physical.typeName.toUpperCase().indexOf("SMARTLIFE") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0)){
                  switchNames = switchNames + "$physical.displayName - ${getColor(i, "upper")} Channel\r\n"
               } else {
                  switchNames = switchNames + "$physical.displayName - ${i}\r\n"
               }
            }
            paragraph "The following switches will be created:\r\n\r\n" + switchNames
		}
    }
}

def createPage(){
   dynamicPage(name: "createPage", title: "Devices have been created", nextPage: "setupPage", createVirtualDevice())
}

def removeVirtual(){
   def switchNames = ""
   dynamicPage(name: "removeVirtual", title: "Remove the virtual switches created by this app", nextPage: "removalPage") {
		section {
			paragraph "This process will remove the virtual switches created by this program. Press next to continue"
            getChildDevices().each {
               switchNames = switchNames + it.displayName + "\r\n"
            }
            paragraph "The following virtual switches will be removed:\r\n\r\n" + switchNames
		}
    }
}

def removalPage(){
   dynamicPage(name: "removalPage", title: "Devices have been removed", nextPage: "setupPage", removeVirtualDevice()) 
}

def createVirtualDevice() {
    if(!isVirtualConfigured()){
       def switchName
       try {
          for (int i = 1; i <= getEndpoints(); i++){
             if ((physical.typeName.toUpperCase().indexOf("FIBARO") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0) ||
             (physical.typeName.toUpperCase().indexOf("SMARTLIFE") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0)){
                switchName = "$physical.displayName - ${getColor(i, "upper")} Channel\r\n"
             } else {
                switchName = "$physical.displayName - ${i}\r\n"
             }
             def switchType = ""
             if (virtualSwitchType != null && virtualSwitchType == "Switch"){
                switchType = "Simulated Switch"
             } else if (virtualSwitchType != null && virtualSwitchType == "Energy Switch") {
                switchType = "Simulated Energy Switch"
             } else {
                switchType = "Simulated Dimmer"
             }
             def child = addChildDevice("erocm123", switchType, getDeviceID(i), null, [name: getDeviceID(i), label: switchName, completedSetup: true])
          }   
       } catch (e) {
          return {
		   section {
			   paragraph "Error when creating the virtual devices. Make sure that you have all of the \"Simulated\" device handlers installed."
		   }
       }
    }
    return {
	   section {
	      paragraph "Devices have been configured. Press next to go to the main page."
	   }
    }
    }else{
       return {
	      section {
		     paragraph "Devices have already been configured."
		  }
       }
    }
}

def isVirtualConfigured(){ 
    def foundDevice = false
    getChildDevices().each {
       foundDevice = true
    }
    return foundDevice
}

def removeVirtualDevice() {
    try {
       unsubscribe()
       getChildDevices().each {
          deleteChildDevice(it.deviceNetworkId)
       }
       return {
          section {
	         paragraph "Devices have been removed. Press next to go to the main page."
	      }
       }
	} catch (e) {
       return {
          section {
			paragraph "Error: ${(e as String).split(":")[1]}."
	      }
       }
    }
}

private getDeviceID(number) {
    return "${app.id}/${number}"
}

def installed() {
  log.debug "Installed with settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
  if(physical != null && setLabel != null && setLabel.toBoolean() != true){
     app.updateLabel("Virtual Device Sync - ${physical.label ? physical.label : physical.name}")
  }
  unsubscribe()
  initialize()
}

def initialize() {
  log.debug "Initializing Virtual Device Sync"
  if ((physical.typeName.toUpperCase().indexOf("FIBARO") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0) ||
  (physical.typeName.toUpperCase().indexOf("SMARTLIFE") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0)){
     subscribe(physical, "red", physicalHandler)
     subscribe(physical, "blue", physicalHandler)
     subscribe(physical, "green", physicalHandler)
     subscribe(physical, "white", physicalHandler) // Added for Fibaro RGBW Controller
     subscribe(physical, "white1", physicalHandler)
     subscribe(physical, "white2", physicalHandler)
     subscribe(physical, "redLevel", physicalHandler)
     subscribe(physical, "blueLevel", physicalHandler)
     subscribe(physical, "greenLevel", physicalHandler)
     subscribe(physical, "whiteLevel", physicalHandler) // Added for Fibaro RGBW Controller
     subscribe(physical, "white1Level", physicalHandler)
     subscribe(physical, "white2Level", physicalHandler)
  } else {
     for (int i = 1; i <= getEndpoints(); i++){
        subscribe(physical, "switch${i}", physicalHandler)
        subscribe(physical, "power${i}", powerHandler)
        subscribe(physical, "energy${i}", energyHandler)
     }
  }
  getChildDevices().each {
     subscribeToCommand(it, "on", virtualHandler)
     subscribeToCommand(it, "off", virtualHandler)
     subscribeToCommand(it, "setLevel", virtualHandler)
  }
}

def virtualHandler(evt) {
  log.debug "virtualHandler called with event: deviceId ${evt.deviceId} name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
    getChildDevices().each {
       if (evt.deviceId == it.id) {
          def switchNumber = it.deviceNetworkId.split("/")[1]
          if ((physical.typeName.toUpperCase().indexOf("FIBARO") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0) ||
          (physical.typeName.toUpperCase().indexOf("SMARTLIFE") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0)){
             switch (evt.value){
                case "setLevel":
                   physical."set${getColor(switchNumber.toInteger()).capitalize()}Level"(it.currentValue("level"))
                break
                default:
                   physical."${getColor(switchNumber.toInteger())}${evt.value.capitalize()}"()
                break
             }
          } else {
             switch (evt.value){
                case "setLevel":
                   physical."setLevel${switchNumber}"(it.currentValue("level"))
                break
                default:
                   physical."${evt.value}${switchNumber}"()
                break
             }
          }
       }
    }
}

def physicalHandler(evt) {
  log.debug "physicalHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
  if ((physical.typeName.toUpperCase().indexOf("FIBARO") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0) ||
  (physical.typeName.toUpperCase().indexOf("SMARTLIFE") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0)){
     switch(evt.name){
        case ~/.*Level.*/:
           sendEvent(getChildDevice("${app.id}/${getSwitchNumber(evt.name)}"), [name:"level", value:"$evt.value", type:"physical"])
        break
        default:
           sendEvent(getChildDevice("${app.id}/${getSwitchNumber(evt.name)}"), [name:"switch", value:"$evt.value", type:"physical"])
        break
  }
  } else {
     switch(evt.name){
        case ~/.*Level.*/:
           sendEvent(getChildDevice("${app.id}/${getSwitchNumber(evt.name)}"), [name:"level", value:"$evt.value", type:"physical"])
        break
        default:
           sendEvent(getChildDevice("${app.id}/${getSwitchNumber(evt.name)}"), [name:"switch", value:"$evt.value", type:"physical"])
        break
     }
  }
}

def powerHandler(evt) {
   log.debug "powerHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
   sendEvent(getChildDevice("${app.id}/${getSwitchNumber(evt.name)}"), [name:"power", value:"$evt.value"])
}

def energyHandler(evt) {
   log.debug "energyHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
   sendEvent(getChildDevice("${app.id}/${getSwitchNumber(evt.name)}"), [name:"energy", value:"$evt.value"])
}


private getColor(number, format = null){
   switch (number) {
      case 1:
         if(format == "upper") return "R" else if(format == "lower") return "r" else return "red"
      break
      case 2:
         if(format == "upper") return "G" else if(format == "lower") return "g"  else return "green"
      break
      case 3:
         if(format == "upper") return "B" else if(format == "lower") return "b"  else return "blue"
      break
      case 4:
         if (physical.typeName.toUpperCase().indexOf("FIBARO") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0){
            if(format == "upper") return "W" else if(format == "lower") return "w"  else return "white"
         } else {
            if(format == "upper") return "W1" else if(format == "lower") return "w1"  else return "white1"
         }
      break
      case 5:
         if(format == "upper") return "W2" else if(format == "lower") return "w2"  else return "white2"
      break
   }
}

private getSwitchNumber(value){
   switch (value) {
      case ~/.*red.*/:
         return 1
      break
      case ~/.*green.*/:
         return 2
      break
      case ~/.*blue.*/:
         return 3
      break
      case ~/.*white1.*/:
         return 4
      break
      case ~/.*white2.*/:
         return 5
      break
      case ~/.*white.*/:
         return 4
      break
      case ~/.*switch.*/:
         return value.substring(6).toInteger()
      break
      case ~/.*energy.*/:
         return value.substring(6).toInteger()
      break
      case ~/.*power.*/:
         return value.substring(5).toInteger()
      break
   }
}

private getEndpoints() {
   def endpoints = 0
   if (physical.typeName.toUpperCase().indexOf("FIBARO") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0){
      endpoints = 4
   } else if (physical.typeName.toUpperCase().indexOf("SMARTLIFE") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0){
      endpoints = 5
   } else {
      physical.supportedCommands.each {
         switch (it) {     
            case ~/.*on.*/:
               for (int i = 1; i <= 10; i++){
                  if (it.toString().indexOf("$i") >= 0) if (i > endpoints) endpoints = i
               }
            break
         }
      }
   } 
   return endpoints
}

private getType() {
   String hasCapability = ""
   
   if (physical.hasCapability("Switch")) {
      hasCapability = "Switch"
   }
   if ((physical.hasCapability("Power Meter")) || (physical.hasCapability("Energy Meter"))) {
      hasCapability = "Energy Switch"
   }
   if (physical.hasCapability("Switch Level")) {
      hasCapability = "Dimmer"
   }
   if ((physical.typeName.toUpperCase().indexOf("FIBARO") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0) ||
   (physical.typeName.toUpperCase().indexOf("SMARTLIFE") >= 0 && physical.typeName.toUpperCase().indexOf("RGBW") >= 0)) {
      hasCapability = "Dimmer"
   }
   
   
   return hasCapability
}