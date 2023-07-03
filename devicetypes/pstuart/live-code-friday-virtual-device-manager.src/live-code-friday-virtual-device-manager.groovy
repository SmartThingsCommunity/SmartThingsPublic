/**
 *  Live Code Friday Virtual Device Manager
 *
 *  Copyright 2015 Patrick Stuart
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
 *  Tasks
 	Create a dynamic pages interface
    Pick a device type
    add/delete that child 
    
 */
definition(
    name: "Live Code Friday Virtual Device Manager",
    namespace: "pstuart",
    author: "Patrick Stuart",
    description: "Live Code Friday Virtual Device Manager",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    singleInstance: true)


preferences {
	page(name: "firstPage")
    page(name: "inputPage")
    page(name: "devicePage")
    page(name: "addDevicePage")
    page(name: "viewDevicePage")
    page(name: "deletePage")
}

def firstPage() {
	dynamicPage(name: "firstPage", title: "Where to first?", install: true, uninstall: true) {
		section("Main Menu") {
        	paragraph "Version 1.2"
        	href(page: "inputPage", title: "Let's Add Devices!")
        	
        }
        /*
        section("Later") {
        	paragraph "More to come..."
        }
        */
        
   }
}

def inputPage() {
	dynamicPage(name: "inputPage", title: "Choose What Device Type You Want To Add", nextPage:"firstPage") {
		section("Device Types") {
        	href(page: "devicePage", title: "Switches", params: [device: "Generic Switch"])
            href(page: "devicePage", title: "Dimmers", params: [device: "Generic Dimmer"])
            href(page: "devicePage", title: "Contact Sensor", params: [device: "Generic Contact"])
            
        }
        section("Later") {
        	paragraph "more devices coming soon"
        }
        section("Navigation") {
        	href(page: "firstPage", title: "Main Menu")
        }
	}
}

def devicePage(params) {
	dynamicPage(name: "devicePage", title: "Devices", nextPage:"inputPage") {
    	// Loop childDevices based on type
        // match up types
	def device = params.device
    log.debug "Hit Device Page with the selector device type $device"
    def deviceTitle = device + "s"
    if (device?.endsWith('ch') || device?.endsWith('s') || device?.endsWith('x')) {
    	deviceTitle = device + "es"
    }
    log.debug "Device Title is $deviceTitle"
    
		section("Installed ${deviceTitle}") {
        	 def childDevices = getChildDevices()
             
             	log.debug "The Child Devices are $childDevices"
             if (childDevices) {
                def devices = "Switches Installed:\r\nThis is a second line\r\n"
                log.debug "Inside childDevices if statement"
                
             	childDevices.findAll { it.typeName == device }
                .each {
                	log.debug "The child device id is $it.deviceNetworkId and the type is $it"
                    def test = it.typeName
                    log.debug "Testing $test"
                    //def tempDevice = getChildDevice(it)
                    //log.debug tempDevice
                    //devices = devices + "test\r\n"
                    
                    
                    
                    switch(it.typeName) {
                    	case "Generic Switch" : 
                        	href(page: "viewDevicePage", title: it.name, params: [dni: it.deviceNetworkId])
                            break
                        case "Generic Dimmer" :
                        	href(page: "viewDevicePage", title: it.name, params: [dni: it.deviceNetworkId])
                            break
                        case "Generic Contact" :
                        	href(page: "viewDevicePage", title: it.name, params: [dni: it.deviceNetworkId])
                            break
                        default : break
                        
                        
                    }
                	
                }
             } else {
             paragraph "No Virtual Generic Devices are Installed"
             }
        }
        
        section("Add A ${params.device}") { //${params.device}
        	// List Switches getChildDevices()
            
            // Add A Switch addChildDevice()
            // View A Switch / Delete that switch go to switch view
            input("DeviceName", "text")
            href(page: "addDevicePage", title: "New $device", params: [type: device])
        }
        
        section("Navigation") {
        	href(page: "firstPage", title: "Main Menu")
        }
	}
}

def addDevicePage(params) {
	dynamicPage(name: "addDevicePage", title: "New $params.type", nextPage:"devicePage") {
		section("New $params.type Add Result") {
        	//add new virtual switch
            log.debug "Add Device Page hit with params $params and $settings.DeviceName"
            def newDeviceName = params.type
            if (settings.DeviceName) {
            	newDeviceName = settings.DeviceName
            }
            def result = addChildDevice(params.type, newDeviceName)
        	paragraph "$params.type Added ${result}"
            
            href(page: "devicePage", title: "Devices")
        }
        
        section("Navigation") {
        	href(page: "firstPage", title: "Main Menu")
        }
	}
}

def viewDevicePage(params) {
	dynamicPage(name: "viewDevicePage", title: "Switch", nextPage:"devicePage") {
    	def viewSwitch = getChildDevice(params.dni)
    	section("$viewSwitch.name") {
        	paragraph "Switch Details \r\nName: $viewSwitch.name\r\nType: $viewSwitch.typeName\r\nNetwork ID: $viewSwitch.deviceNetworkId\r\nStates\r\nSwitch: ${viewSwitch.currentState('switch').value}\r\n" // Create info about switch / child device
            log.debug viewSwitch.currentState('switch').value
            href(page: "deletePage", title: "Delete", params: [dni: params.dni])
        }
        
        section("Navigation") {
        	href(page: "firstPage", title: "Main Menu")
        }
    }
}

def deletePage(params) {
	dynamicPage(name: "deletePage", title: "Delete", nextPage:"devicePage") {
    	section("switch") {
        	paragraph "Deleted Switch with DNI of $params.dni"
            log.debug "Deleting $params.dni"
            //def delete = getChildDevices().findAll { it?.contains(params.dni) }
			//log.debug delete
            def delete = getChildDevice(params.dni)
            //removeChildDevices(delete)
            deleteChildDevice(delete.deviceNetworkId)
            
            href(page: "switchPage", title: "Switches")
        }
        
        section("Navigation") {
        	href(page: "firstPage", title: "Main Menu")
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
	// TODO: subscribe to attributes, devices, locations, etc.
    def switches = getChildDevices()
    switches.each {
    	if (!it.currentValue('switch') ) {
        	it.off()
        }
    }
}


def addChildDevice(params, deviceName) {
	//Get all devices installed as children
    def childDevices = getChildDevices() //.findAll{ it -> it.type == params } //Find device of type params.type
    //def collectDevices = childDevices.collect{ getChildDevice(it).type ?: 0}
    //log.debug "The result of collectDevices is $collectDevices"
    
    def gTypes = genericTypes()
    log.debug gTypes
    def subChildDevices = childDevices?.findAll { it -> it.typeName == params }
    log.debug "The subset of child devices is $subChildDevices based on type $params"
    def counter = subChildDevices?.size() + 1
    log.debug "$subChildDevices and counter is $counter"
    
    /*
    def counters = [:]
    childDevices.each {
    	def childDevice = getChildDevice(it)
        log.debug "Child Device type is $childDevice.type"
        gTypes.each {
        	if (it == childDevice.type) {
            	def counter = ["name" : params.type, "counter" : counter++ ]
            }
        }
    } */
    
    
    
    
                    
    //def counter = childDevices.size() + 1 //TODO Fix counter for each type
    def dni = "pstuartDevice_$counter" // TODO create random string /guid
    def newDeviceName = "$params $counter"
    if (deviceName != params) {
    	newDeviceName = deviceName
    }
    log.debug newDeviceName
    log.debug dni
    log.debug params
    log.trace "just about to add childe device"
    def childDevice = addChildDevice("pstuart", params, dni, null, [name:newDeviceName])
    log.debug childDevice
    childDevice.off()
    return childDevice
    //return dni
    
    
}

def genericTypes() {
	def gTypes = [
    	"Generic Switch",
        "Generic Contact",
        "Generic Dimmer",
    ]
	return gTypes
}