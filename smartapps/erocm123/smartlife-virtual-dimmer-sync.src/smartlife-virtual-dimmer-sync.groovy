/**
 *  SmartLife Virtual Dimmer Sync
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
   name: "SmartLife Virtual Dimmer Sync",
   namespace: "erocm123",
   author: "Eric Maycock",
   description: "Creates 5 Virtual Dimmers and keeps them in sync with the selected SmartLife RGBW Channels (R,G,B,W1,W2)",
   category: "Convenience",
   iconUrl: "https://raw.githubusercontent.com/erocm123/SmartThingsPublic/master/smartapps/erocm123/smartlife-rgbw-light-connect.src/smartlife-rgbw-icon.png",
   iconX2Url: "https://raw.githubusercontent.com/erocm123/SmartThingsPublic/master/smartapps/erocm123/smartlife-rgbw-light-connect.src/smartlife-rgbw-icon-2x.png",
   iconX3Url: "https://raw.githubusercontent.com/erocm123/SmartThingsPublic/master/smartapps/erocm123/smartlife-rgbw-light-connect.src/smartlife-rgbw-icon-3x.png"
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
        
        input "physical", "capability.colorControl", title: "Which SmartLife RGBW Controller?", multiple: false, required: true
        if(!isVirtualConfigured()){
           href "createVirtual", title:"Create Virtual Devices", description:"Create virtual devices"
        }else{
           href "removeVirtual", title:"Remove Virtual Devices", description:"Remove virtual devices"
        }
    }
    section([title:"Available Options", mobileOnly:true]) {
			label title:"Assign a name for your app (optional)", required:false
		}
    }
}

def createVirtual(){
   dynamicPage(name: "createVirtual", title: "Associate your device's channels with virtual dimmers", nextPage: "createPage") {
		section {
            
			paragraph "This process will create virtual dimmers and associate them with the channels on your SmartLife Controller. There will be 5 dimmers (R,G,B,W1,W2)."

            def switchNames = ""
            for (int i = 1; i <= 5; i++){
               if(settings["${state.currentDeviceId}_programs_${i}_name"] == null || settings["${state.currentDeviceId}_programs_${i}_name"] == ""){
                  switchNames = switchNames + (settings["${state.currentDeviceId}_prefix"] != null ? settings["${state.currentDeviceId}_prefix"] : '') + "$physical.displayName - ${getColor(i, "upper")} Channel\r\n"
               }else{
                  switchNames = switchNames + (settings["${state.currentDeviceId}_prefix"] != null ? settings["${state.currentDeviceId}_prefix"] : '') + " " + settings["${state.currentDeviceId}_programs_${i}_name"] + "\r\n"
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
    for (int i = 1; i <= 5; i++){
       if(settings["${state.currentDeviceId}_programs_${i}_name"] == null || settings["${state.currentDeviceId}_programs_${i}_name"] == ""){
            switchName = (settings["${state.currentDeviceId}_prefix"] == null ? "" : settings["${state.currentDeviceId}_prefix"]) + "$physical.displayName - ${getColor(i, "upper")} Channel\r\n"
       }else{
            switchName = (settings["${state.currentDeviceId}_prefix"] == null ? "" : settings["${state.currentDeviceId}_prefix"]) + " " + settings["${state.currentDeviceId}_programs_${i}_name"]
       }  
       def child = addChildDevice("erocm123", "Simulated Dimmer", getDeviceID(i), null, [name: getDeviceID(i), label: switchName, completedSetup: true])
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
    }}
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
  unsubscribe()
  initialize()
}

def initialize() {
  log.debug "Initializing SmartLife Virtual Dimmer Sync"
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
             switch (evt.value){
                case "setLevel":
                   physical."set${getColor(switchNumber.toInteger()).capitalize()}Level"(it.currentValue("level"))
                break
                default:
                   physical."${getColor(switchNumber.toInteger())}${evt.value.capitalize()}"()
                break
             }
       }
    }
    
}

def physicalHandler(evt) {
  log.debug "physicalHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
  switch(evt.name){
     case ~/.*Level.*/:
        sendEvent(getChildDevice("${app.id}/${getSwitchNumber(evt.name)}"), [name:"level", value:"$evt.value", type:"physical"])
     break
     default:
        sendEvent(getChildDevice("${app.id}/${getSwitchNumber(evt.name)}"), [name:"switch", value:"$evt.value", type:"physical"])
     break
  
  }
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
         // Might want to change this to physical.supportedCommands and look for whiteOn, whiteOff, or setWhileLevel
         if(physical.typeName.toUpperCase().indexOf("FIBARO") >= 0){
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
   }
}
