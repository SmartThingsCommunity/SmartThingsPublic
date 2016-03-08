/**
 *  Virtual / Physical Switch Sync (i.e. Enerwave ZWN-RSM2 Adapter, Monoprice Dual Relay, Philio PAN04, Aeon SmartStrip)
 *
 *  Copyright 2016 Eric Maycock (erocm123)
 * 
 *  Note: Use a "Simulated Switch" from the IDE for best results
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
   name: "Virtual / Physical Switch Sync",
   namespace: "erocm123",
   author: "Eric Maycock",
   description: "Keeps multi switch devices like the Aeon Smartstrip, Monoprice Dual Relay, and Philio PAN04 in sync with their virtual switches",
   category: "My Apps",
   iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
   iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "numberPage", nextPage: "setupPage")
    page(name: "setupPage")
}

def numberPage() {
    dynamicPage(name: "numberPage", install: false, uninstall: true) {
        section {
            input "vNumber", "number", title:"Number of virtual switches", description: 2, defaultValue: 2 , required: true
        }
        section([title:"Available Options", mobileOnly:true]) {
			label title:"Assign a name for your app (optional)", required:false
		}
    }

}

def setupPage() {
    dynamicPage(name: "setupPage", install: true, uninstall: true) {
    section {
        input "physical", "capability.switch", title: "Which Physical Switch?", multiple: false, required: true
        for (int i = 1; i <= vNumber; i++){
            input "virtual${i}", "capability.switch", title: "Virtual Switch to link to Switch ${i}?", multiple: false, required: true        
        }
    }
    }
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
  log.debug "Initializing Virtual / Physical Switch Sync v 1.0"
  for (int i = 1; i <= vNumber; i++){
     subscribe(physical, "switch${i}", physicalHandler)
     subscribeToCommand(settings["virtual${i}"], "on", virtualHandler)
     subscribeToCommand(settings["virtual${i}"], "off", virtualHandler)
     subscribe(physical, "power${i}", powerHandler)
     subscribe(physical, "energy${i}", energyHandler)
  }
}

def virtualHandler(evt) {
  log.debug "virtualHandler called with event: deviceId ${evt.deviceId} name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
  
    for (int i = 1; i <= vNumber; i++){
       if (evt.deviceId == settings["virtual${i}"].id) {
          switch (evt.value) {
             case 'on':
             //log.debug "switch ${i} on"
             physical."on${i}"()
             //eval(physical.on+${i}())
             break
             case 'off':
             //log.debug "switch ${i} off"
             physical."off${i}"()
             break
          }
       }
    }
}

def physicalHandler(evt) {
  log.debug "physicalHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
  
  for (int i = 1; i <= vNumber; i++){
       if (evt.name == "switch${i}") {
          switch (evt.value) {
             case 'on':
             settings["virtual${i}"].onPhysical()
             break
             case 'off':
             settings["virtual${i}"].offPhysical()
             break
          }
       }
    }
}

def powerHandler(evt) {
   log.debug "powerHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
   for (int i = 1; i <= vNumber; i++){
       if (evt.name == "power${i}") {
          settings["virtual${i}"].power(evt.value)
       }
   }
}

def energyHandler(evt) {
   log.debug "energyHandler called with event:  name:${evt.name} source:${evt.source} value:${evt.value} isStateChange: ${evt.isStateChange()} isPhysical: ${evt.isPhysical()} isDigital: ${evt.isDigital()} data: ${evt.data} device: ${evt.device}"
   for (int i = 1; i <= vNumber; i++){
       if (evt.name == "energy${i}") {
          settings["virtual${i}"].energy(evt.value)
       }
   }
}