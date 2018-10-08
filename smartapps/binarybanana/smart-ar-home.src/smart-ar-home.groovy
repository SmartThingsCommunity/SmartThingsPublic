/**
 *  Smart AR Home
 *
 *  Copyright 2017 Binary Banana LLC
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
 *  Publish the app
 *   Sign in into your SmartThings portal: https://graph.api.smartthings.com/
 *   Navigate to 'My SmartApps' and click on 'New SmartApp'
 *   Switch the tab to "From Code"
 *   Open Smart AR Home app code: https://github.com/dzak83/SmartThingsPublic/blob/master/smartapps/com-smartarhome/smartarhome.src/smart-ar-home.groovy
 *   Copy and paste it into your new app on SmartThings portal. Create and then publish it using "For Me" option.
 *  
 *  Authenticate
 *   In the SmartThings portal, open Smart AR Home app and click on 'App Settings'
 *   Scroll down to OAuth and enable it. Click Update to confirm.
 *   Page should refresh and you should now see values for 'OAuth Client ID' and 'OAuth Client Secret'
 *   Copy and paste the values into the Smart AR Home app to continue setup process.
 */
definition(
    name: "Smart AR Home",
    namespace: "BinaryBanana",
    author: "Binary Banana LLC",
    description: "Your gateway into augmented reality in your home",
    category: "Convenience",
    iconUrl: "http://smartarhome.com/SmartThingsIcon/1024pxLogo.png",
    iconX2Url: "http://smartarhome.com/SmartThingsIcon/1024pxLogo.png",
    iconX3Url: "http://smartarhome.com/SmartThingsIcon/1024pxLogo.png")


preferences {	
    section("Control these switch levels...") {
        input "switchlevels", "capability.switchLevel", multiple:true, required:false
    }
	section("Control these switches...") {
        input "switches", "capability.switch", multiple:true, required:false
    }    
    section("Control these window shades...") {
        input "shades", "capability.windowShade", multiple:true, required:false
    }    
	section("Control these outlets...") {
    	input "outlets", "capability.outlet", multiple:true, required:false
    }    
    section("Control these locks...") {
    	input "locks", "capability.lock", multiple:true, required:false
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
}

private device(it, type) {
	it ? [id: it.id, label: it.label, type: type] : null
}

//API Mapping
mappings {
   path("/getalldevices") {
    action: [
      	GET: "getAllDevices"
   ]
  }    
  path("/light/dim/:id/:dim") {
    action: [
      GET: "setLevelStatus"
    ]
  }
  path("/light/on/:id") {
    action: [
      GET: "turnOnLight"
    ]
  }
  path("/light/off/:id") {
    action: [
      GET: "turnOffLight"
    ]
  }
  path("/doorlocks/lock/:id") {
    action: [
      GET: "lockDoorLock"
    ]
  }
  path("/doorlocks/unlock/:id") {
    action: [
      GET: "unlockDoorLock"
    ]
  }
  path("/doorlocks/:id") {
    action: [
      GET: "getDoorLockStatus"
    ]
  }  
    path("/shades/open/:id") {
    action: [
      GET: "openShade"
    ]
  }
  path("/shades/preset/:id") {
    action: [
      GET: "presetShade"
    ]
  }
  path("/shades/close/:id") {
    action: [
      GET: "closeShade"
    ]
  }
  	path("/shades/:id") {
    action: [
	GET: "getShadeStatus"
	]
}
   
  	path("/outlets/:id") {
    action: [
      GET: "getOutletStatus"
    ]
  }
  	path("/outlets/turnon/:id") {
    action: [
      GET: "turnOnOutlet"
    ]
  }
  path("/outlets/turnoff/:id") {
    action: [
      GET: "turnOffOutlet"
    ]
  }
  path("/switches/turnon/:id") {
    action: [
      GET: "turnOnSwitch"
    ]
  }
  path("/switches/turnoff/:id") {
    action: [
      GET: "turnOffSwitch"
    ]
  }
  path("/switches/:id") {
    action: [
      GET: "getSwitchStatus"
    ]
  }
}

//API Methods
def getAllDevices() {
    def locks_list = locks.collect{device(it,"Lock")}
    def shades_list = shades.collect{device(it,"Window Shade")}    
	def outlets_list = outlets.collect{device(it,"Outlet")}
    def switches_list = switches.collect{device(it,"Switch")}
    def switchlevels_list = switchlevels.collect{device(it,"Switch Level")}   
    return switchlevels_list + switches_list + shades_list + locks_list + outlets_list
}

//switch level
def getLevelStatus() {
	def device = switchlevels.find { it.id == params.id }
    if (!device) {
            [Level: "No dimmer"]
        } else {
        	return [Level: device.currentValue('level')]
        }
}

def getLevelStatus(id) {
	def device = switchlevels.find { it.id == id }
    if (!device) {
            [Level: "No dimmer"]
        } else {
        	return [Level: device.currentValue('level')]
        }
}


def setLevelStatus() {
	def device = switchlevels.find { it.id == params.id }
    def level = params.dim
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	device.setLevel(level.toInteger())
        	return [result_action: "200", Level: device.currentValue('level')]
        }
}

//light
def turnOnLight() {
    def device = switchlevels.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
            device.on();
                  
            return [Device_id: params.id, result_action: "200"]
        }
    }

def turnOffLight() {
    def device = switchlevels.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
            device.off();
                  
            return [Device_id: params.id, result_action: "200"]
        }
}

//shades
def getShadeStatus() 
{
	def device = shades.find { it.id == params.id }
    if (!device) 
    {
        httpError(404, "Device not found")
    }
    else
    {
        return [Device_state: device.currentValue('windowShade')]
    }
}

def openShade() 
{
    def device = shades.find { it.id == params.id }
    if (!device) 
    {
            httpError(404, "Device not found")
    }
    else 
    {            
    	device.open();
                  
        return [Device_id: params.id, result_action: "200"]                              
    }
}
    
def presetShade() {
    def device = shades.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.presetPosition();
                  
            return [Device_id: params.id, result_action: "200"]                              
            }
    }

def closeShade() {
	def device = shades.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.close();
                  
            return [Device_id: params.id, result_action: "200"]                              
            }
    }

//LOCKS
def getDoorLockStatus() {
	def device = locks.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	def bat = getBatteryStatus(device.id)
        	return [Device_state: device.currentValue('lock')] + bat
        }
}

def lockDoorLock() {
    def device = locks.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.lock();
                  
            return [Device_id: params.id, result_action: "200"]                              
            }
    }

def unlockDoorLock() {
	def device = locks.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.unlock();
                  
            return [Device_id: params.id, result_action: "200"]                              
            }
    }

//OUTLET
def getOutletStatus() {

    def device = outlets.find { it.id == params.id }
    if (!device) {
            device = switches.find { it.id == params.id }
            if(!device) {
            	httpError(404, "Device not found")
        	}
     }
     def watt = getMeterStatus(device.id)
       	
     return [Device_state: device.currentValue('switch')] + watt
}

def turnOnOutlet() {
    def device = outlets.find { it.id == params.id }
    if (!device) {
            device = switches.find { it.id == params.id }
            if(!device) {
            	httpError(404, "Device not found")
        	}
     }
     
     device.on();
                  
     return [Device_id: params.id, result_action: "200"]
}

def turnOffOutlet() {
    def device = outlets.find { it.id == params.id }
    if (!device) {
            device = switches.find { it.id == params.id }
            if(!device) {
            	httpError(404, "Device not found")
        	}
     }
            
     device.off();
                  
     return [Device_id: params.id, result_action: "200"]
}

//SWITCH
def getSwitchStatus() {
	def device = switches.find { it.id == params.id }
    if (!device) {
            httpError(404, "Device not found")
        } else {
        	return [Device_state: device.currentValue('switch'), Dim: getLevelStatus(params.id)]
	}
}

def turnOnSwitch() {
    def device = switches.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.on();
                  
            return [Device_id: params.id, result_action: "200"]
        }
}

def turnOffSwitch() {
    def device = switches.find { it.id == params.id }
        if (!device) {
            httpError(404, "Device not found")
        } else {
            
            device.off();
            return [Device_id: params.id, result_action: "200"]
        }
}