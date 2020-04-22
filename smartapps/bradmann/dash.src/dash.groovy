/**
 *  dash
 *
 *  Copyright 2016 Brad Mann
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
    name: "dash",
    namespace: "bradmann",
    author: "Brad Mann",
    description: "Home Dashboard",
    category: "Convenience",
    iconUrl: "https://nodejs-entropy6.rhcloud.com/favicomatic/apple-touch-icon-60x60.png",
    iconX2Url: "https://nodejs-entropy6.rhcloud.com/favicomatic/apple-touch-icon-120x120.png",
    iconX3Url: "https://nodejs-entropy6.rhcloud.com/favicomatic/apple-touch-icon-152x152.png",
    oauth: [displayName: "dash [Home Dashboard]", displayLink: "https://nodejs-entropy6.rhcloud.com/"],
    uuid: "f17fbeca-ee14-4f8b-8f82-be4898286b26"
)

preferences {    
    section("Devices") {
    	paragraph "Select the devices you want DASH to control and monitor."
    	input(name: "sensors", type: "capability.sensor", required: false, multiple: true)
        input(name: "switches", type: "capability.switch", required: false, multiple: true)
    }
}

mappings { 
  path("/devices") {
  	action: [
    	GET: "listDevices"
    ]
  }
  
  path("/devices/:deviceID/:command") {
  	action: [
    	POST: "updateDevice"
    ]
  }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	initialize()
}

def initialize() {
	def devs = getChildDevices()
    log.debug "Devices: ${devs}"
    switches.each {
    	log.debug "SWITCH OBJECT: ${it}"
    }
}

/*
* Helper function for outputting the device data in JSON.
*/
private deviceJSON(device) {
    def caps = []
    def attributeValues = []
    device.capabilities.each {
        cap ->
        def attributes = []
        def commands = []
        cap.attributes.each {
            att ->
            attributes << [dataType: att.dataType, name: att.name, values: att.values]
            attributeValues << [capability: cap.name, attribute: att.name, value: device.currentValue(att.name)]
        }
        cap.commands.each {
            cmd ->
            commands << [name: cmd.name, arguments: cmd.arguments]
        }

        caps << [name: cap.name, attributes: attributes, commands: commands]
    }
    return [capabilities: caps, name: device.displayName, id: device.id, currentValues: attributeValues]
}

/* REST enpoints */
/*
*  Get the list of devices and their current states.
*/
def listDevices() {
    def resp = []
    def devices = sensors + switches
    def uniqueDevs = devices.unique {a,b -> a['id'] <=> b['id']}
    
    uniqueDevs.each {
    	device ->
    	resp << deviceJSON(device)
    }
    return resp
}

/*
*  Run a command on a device. Inputs are device ID and command to run.
*/
def updateDevice() {
	// Use the built-in request object to get the command parameter
    def command = params.command
    def deviceID = params.deviceID

	log.debug "updateDevice(${command}, ${deviceID}) called"

	def devices = sensors + switches
    def uniqueDevs = devices.unique {a,b -> a['id'] <=> b['id']}

    if (command) {
        // Check that the device supports the specified command
        // If not, return an error using httpError, providing a HTTP status code.
        def existingDevice = null
        uniqueDevs.find {
            if (it.id == deviceID) {
                existingDevice = it
                return true
            }
            return false
        }
        
        if (!existingDevice) {
        	httpError(501, "Device ${deviceID} not found.")
        } else if (!existingDevice.hasCommand(command)) {
        	httpError(501, "$command is not a valid command for all devices specified.")
        }

        // All devices have the command
        // Execute the command on all devices.
        existingDevice."$command"()
    }
    return [success: true]
}