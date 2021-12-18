/**
 *  Home Assistant Connect
 *
 *  Copyright 2017 Grace Mann
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
    name: "Home Assistant Connect",
    namespace: "gurase",
    author: "Grace Mann",
    description: "Connect your Home Assistant devices to SmartThings.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
    appSetting "hassUrl"
    appSetting "token"
    singleInstance: true
}

preferences {
    page(name: "setup", content: "setupPage")
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
	log.debug "initialize"
    
    addChildren(covers ?: [], state.entities["covers"], "Home Assistant Cover")
    addChildren(lights ?: [], state.entities["lights"], "Home Assistant Light")
    addChildren(scripts ?: [], state.entities["scripts"], "Home Assistant Switch")
    addChildren(switches ?: [], state.entities["switches"], "Home Assistant Switch")
    
    // Delete any that are no longer selected
    log.debug "selected devices: ${settings.collectMany { it.value }}"
    def delete = getChildDevices().findAll { !settings.collectMany { it.value }.contains(it.getDeviceNetworkId()) }
	log.warn "delete: ${delete}, deleting ${delete.size()} devices"
	delete.each { deleteChildDevice(it.getDeviceNetworkId()) }
    
    // Polling
    poll()
    runEvery5Minutes("poll")
}

def setupPage() {
	log.debug "setupPage"
	def options = getOptions()
    
	return dynamicPage(name: "setup", title: "Home Assistant", install: true, uninstall: true) {
    	section {
        	paragraph "Tap below to see the list of devices available in Home Assistant and select the ones you want to connect to SmartThings."
            input(name: "covers", type: "enum", required: false, title: "Covers", multiple: true, options: options.covers)
            input(name: "lights", type: "enum", required: false, title: "Lights", multiple: true, options: options.lights)
            input(name: "scripts", type: "enum", required: false, title: "Scripts", multiple: true, options: options.scripts)
            input(name: "switches", type: "enum", required: false, title: "Switches", multiple: true, options: options.switches)
        }
    }
}

// Get all cover, light, and switch entities from Home Assistant
def getEntities() {
	log.debug "getEntities"
    
	def params = [
        uri: appSettings.hassUrl,
        path: "/api/states",
        headers: ["Authorization": "Bearer " + appSettings.token],
        contentType: "application/json"
    ]
    
    def entities = [:]
	
    try {
        httpGet(params) { resp ->
        	// Covers
            def covers = [:]
            resp.data.findAll { 
            	it.entity_id.startsWith("cover.") 
            }.each {
            	covers["${it.entity_id}"] = it
            }
            entities["covers"] = covers
            
        	// Lights
        	def lights = [:]
            resp.data.findAll { 
            	it.entity_id.startsWith("light.") 
            }.each {
            	lights["${it.entity_id}"] = it
            }
            entities["lights"] = lights
            
            // Scripts
            def scripts = [:]
            resp.data.findAll { 
            	it.entity_id.startsWith("script.") 
            }.each {
            	scripts["${it.entity_id}"] = it
            }
            entities["scripts"] = scripts
            
            // Switches
            def switches = [:]
            resp.data.findAll { 
            	it.entity_id.startsWith("switch.") 
            }.each {
            	switches["${it.entity_id}"] = it
            }
            entities["switches"] = switches
            
            state.entities = entities
            return entities
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

// Populate Smartapp setup page with Home Assistant entities
def getOptions() {
	getEntities()
    def options = [:]
    
    state.entities.each { domain, domainEntities ->
    	def values = [:]
        
    	domainEntities.each { entityId, entity ->
        	values[entityId] = entity.attributes.smartthings_name ?: entity.attributes.friendly_name
        }
        
        values = values.sort { it.value }
        options["${domain}"] = values
    }
    
    return options
}

def addChildren(chosenEntities, domain, deviceType) {
	log.debug "addChildren"
    
    // Create devices for newly selected Home Assistant entities
    chosenEntities.each { entityId ->
		if (!getChildDevice(entityId)) {
			device = addChildDevice(app.namespace, deviceType, entityId, null, 
            	[name: "Device.${entityId}", label:"${domain[entityId].attributes.smartthings_name ?: domain[entityId].attributes.friendly_name}", completedSetup: true])
			log.debug "created ${device.displayName} with id ${device.getDeviceNetworkId()}"
		}
	}
}

// Poll child devices
def poll() {
	getEntities()
    def devices = getChildDevices()
    
    // Covers
    devices.findAll {
    	it.getTypeName() == "Home Assistant Cover"
    }.each { device ->
    	def entityId = device.getDeviceNetworkId()
    	def entity = state.entities.covers[entityId]
        
        device.sendEvent(name: "windowShade", value: entity.state)
        device.sendEvent(name: "level", value: entity.attributes.current_position)
        device.sendEvent(name: "label", value: entity.attributes.smartthings_name ?: entity.attributes.friendly_name)
    }
    
    // Lights
    devices.findAll {
    	it.getTypeName() == "Home Assistant Light"
    }.each { device ->
    	def entityId = device.getDeviceNetworkId()
    	def entity = state.entities.lights[entityId]
        
        if (entity.attributes.rgb_color) {
        	device.sendEvent(name: "color", value: colorUtil.rgbToHex(entity.attributes.rgb_color[0], entity.attributes.rgb_color[1], entity.attributes.rgb_color[2]))
        }
        
        if (entity.attributes.color_temp) {
        	device.sendEvent(name: "colorTemperature", value: (1000000).intdiv(entity.attributes.color_temp))
        }
        
        if (entity.attributes.brightness) {
        	device.sendEvent(name: "level", value: entity.attributes.brightness / 255 * 100)
            device.sendEvent(name: "switch.setLevel", value: entity.attributes.brightness / 255 * 100)
        }
        
        device.sendEvent(name: "switch", value: entity.state)
        device.sendEvent(name: "label", value: entity.attributes.smartthings_name ?: entity.attributes.friendly_name)
    }
    
    // Scripts, Switches
    devices.findAll {
    	it.getTypeName() == "Home Assistant Switch"
    }.each { device ->
    	def entityId = device.getDeviceNetworkId()
    	def entity = state.entities.subMap(["scripts", "switches"]).collectEntries { it.value }[entityId]
        
        device.sendEvent(name: "switch", value: entity.state)
        device.sendEvent(name: "label", value: entity.attributes.smartthings_name ?: entity.attributes.friendly_name)
    }
}

// Call Home Assistant services via HTTP POST request
def postService(service, data) {
	def params = [
        uri: appSettings.hassUrl,
        path: service,
        headers: ["Authorization": "Bearer " + appSettings.token],
        requestContentType: "application/json",
        body: data
    ]
    
    try {
        httpPost(params) { resp ->
        	return true
        }
    } catch (e) {
        log.error "something went wrong: $e"
        return false
    }
}