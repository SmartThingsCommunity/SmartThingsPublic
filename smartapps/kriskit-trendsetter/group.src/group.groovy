/**
 *  Trend Setter - Group
 *
 *  Copyright 2015 Chris Kitch
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
    name: "Group",
    namespace: "kriskit.trendSetter",
    author: "Chris Kitch",
    description: "A child SmartApp for Trend Setter for handling a group of devices.",
    category: "My Apps",
    iconUrl: "https://cdn.rawgit.com/Kriskit/SmartThingsPublic/master/smartapps/kriskit/trendsetter/icon.png",
    iconX2Url: "https://cdn.rawgit.com/Kriskit/SmartThingsPublic/master/smartapps/kriskit/trendsetter/icon@2x.png",
    iconX3Url: "https://cdn.rawgit.com/Kriskit/SmartThingsPublic/master/smartapps/kriskit/trendsetter/icon@3x.png")
    
def version() {
	return "1.0"
}

def typeDefinitions() {
	return [
        [
        	type: "switch", 
            singular: "Switch", 
            plural: "Switches", 
            deviceType: "Switch Group Device",
            attributes: [
            		[name: "switch"]
            	]
        ],
        [
        	type: "switchLevel", 
            singular: "Dimmer", 
            plural: "Dimmers", 
            deviceType: "Dimmer Group Device",
            inherits: "switch",
            attributes: [
            	[name: "level"]
            ]
        ],
        [
        	type: "colorControl",
            singular: "Colorful Light",
            plural: "Colorful Lights",
            deviceType: "Colorful Light Group Device",
            inherits: "switchLevel",
            attributes: [
            	[name: "hue"],
                [name: "saturation"],
                [name: "color"]
            ]
        ],
        [
        	type: "powerMeter",
            singular: "Power Meter",
            plural: "Power Meters",
            deviceType: "Power Meter Group Device",
            attributes: [
            	[name: "power"]
            ]
        ]
    ]
}

// Setup
preferences {
	page(name: "configure")
}

def configure() {
	atomicState.typeDefinitions = null
	def controller = getControllerDevice();

	dynamicPage(name: "configure", uninstall: controller != null, install: true) {   
        if (!controller) {
           section {              
				input "deviceType", "enum", title: "Device Type", required: true, submitOnChange: true, options: getDeviceTypeOptions()
                paragraph "This cannot be changed once the group is created.", color: "#ffcc00"
            }
        }
        
        if (deviceType) {
           	def definition = getTypeDefinition(deviceType)
        
            section(title: controller == null ? "Grouping" : null) {
        		label title: "Group Name", required: true
            
                input "devices", "capability.${deviceType}", title: "${definition.plural}", multiple: true, required: true, submitOnChange: controller != null

                if (selectedDevicesContainsController()) {
                    paragraph "WARNING: You have selected the controller ${definition.singular.toLowerCase()} for this group. This will likely cause unexpected behaviour.\n\nPlease uncheck the '${controller.displayName}' from the selected ${definition.plural.toLowerCase()}.", 
                        image: "https://cdn2.iconfinder.com/data/icons/freecns-cumulus/32/519791-101_Warning-512.png"
                }
            }
            
			if (controller == null) {
                section(title: "Controller") {                        
                    input "deviceName", "text", title: "${definition.singular} Name", required: true, description: "For the controlling virtual ${definition.singular.toLowerCase()} to be created"
                }
            }
            
            if (definition.advanced) {
            	section(title: "Advanced", hidden: true, hideable: true) {
                }
            }
        }
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    
	installControllerDevice()
	initialize()
}

def installControllerDevice() {
	def definition = getTypeDefinition()

	log.debug "Installing switch group controller device..."
	addChildDevice("kriskit.trendSetter", definition.deviceType, UUID.randomUUID().toString(), null, ["name": deviceName, "label": deviceName, completedSetup: true])
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	def definition = getTypeDefinition()
	addSubscriptions(definition)
    def namesToCheck = definition.attributes?.collect { it.name }
    updateControllerState(namesToCheck)
}

def addSubscriptions(definition) {
	def controller = getControllerDevice()

    definition.attributes?.each {
    	log.debug "Subscribing to ${it.name}..."
    	subscribe(devices, it.name, onDeviceAttributeChange)
    }
}

// Subscription Handlers
def onDeviceAttributeChange(evt) {
	def namesToCheck = atomicState.namesToCheck  ?: []

	log.debug "Device state change: ${evt.device.displayName} -> ${evt.name} = ${evt.value}"

    if (!namesToCheck.any { it == evt.name })
    	namesToCheck.push(evt.name)
        
	atomicState.namesToCheck = namesToCheck
    runIn(1, "updateControllerState")
}

def updateControllerState() {
	def namesToCheck = atomicState.namesToCheck
    updateControllerState(namesToCheck)
    atomicState.namesToCheck = null
}

def updateControllerState(namesToCheck) {
	if (!namesToCheck)
    	return

	def controller = getControllerDevice()
    namesToCheck?.each { name ->
    	def values = devices?.currentValue(name)
        values?.removeAll([null])
        log.debug "Updating Controller State: $name -> $values"
        controller.groupSync(name, values)
    }
}

def performGroupCommand(command, arguments = null) {
	runCommand(devices, command, arguments ?: []) 
}

def runCommand(target, command, args) {
    log.debug "Running command '${command}' with arguments ${args} on ${target}..."
    $performCommand(target, command, args)
}

def getGroupCurrentValues(name) {
	return devices?.currentValue(name)
}

// Utilities
def getTypeDefinitions() {
	if (atomicState.version != version()) {
    	atomicState.typeDefinitions = null
        atomicState.version = version()
	}

	if (atomicState.typeDefinitions)
    	return atomicState.typeDefinitions

	log.debug "Building type definitions..."

	def result = []
	def definitions = typeDefinitions()
    
    definitions?.each { definition ->
    	if (definition.inherits)
        	definition = mergeAttributes(definition, definitions.find { it.type == definition.inherits })
    
    	result.push(definition)
    }
    
    atomicState.typeDefinitions = result
    
    return result
}

def mergeAttributes(definition, inheritedDefinition) {
    inheritedDefinition.attributes?.each { attr ->
    	if (!definition.attributes?.any { it.name == attr.name })
        	definition.attributes.push(attr)
    }
    
    if (inheritedDefinition.inherits) {
    	def definitions = typeDefinitions()
    	definition = mergeAttributes(definition, definitions.find { it.type == inheritedDefinition.inherits })
	}
    
    return definition
}

def getControllerDevice() {
	return getChildDevices()?.find { true }
}

def getTypeDefinition() {
	return getTypeDefinition(deviceType)
}

def getTypeDefinition(type) {
	return getTypeDefinitions().find {
    	it.type == type
    }
}

def getDeviceTypeOptions() {
	return getTypeDefinitions().collect {
    	["${it.type}": it.singular]
    }
}

def selectedDevicesContainsController() {
	def controller = getControllerDevice()
	return devices?.any { 
    	it.deviceNetworkId == controller.deviceNetworkId 
    }
}

private $performCommand(target, command, args) {
    switch(args?.size()) {
    	default: 
        	target?."$command"()
        break
    
    	case 1: 
        	target?."$command"(args[0])
        break
        
		case 2: 
        	target?."$command"(args[0], args[1])
        break
        
		case 3: 
        	target?."$command"(args[0], args[1], args[2])
        break
        
		case 4: 
        	target?."$command"(args[0], args[1], args[2], args[3])
        break
        
		case 5: 
        	target?."$command"(args[0], args[1], args[2], args[3], args[4], args[5])
        break
        
		case 6: 
        	target?."$command"(args[0], args[1], args[2], args[3], args[4], args[5], args[6])
        break
        
        case 7: 
        	target?."$command"(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7])
        break
        
        case 8: 
        	target?."$command"(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8])
        break
        
        case 9: 
        	target?."$command"(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9])
        break
    }
}