/**
 *  Reset Color on Close
 *
 *  Copyright 2015 mattrmiller
 *
 *  Licensed under the Apache License, Version 2.0 (the 'License'); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
/**
 * Definition
 */
definition(
    name: 'Set the Mood',
    namespace: 'mattrmiller',
    author: 'mattrmiller',
    description: 'Create moods out of the current state of your switches and bulbs, and save for later use.',
    category: 'Convenience',
    iconUrl: 'https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png',
    iconX2Url: 'https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png',
    iconX3Url: 'https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png'
) 
 
 /**
 * Preferences
 */
preferences {

	// Section for switches
	section('Select switches and bulbs to use for mood...') {
		input 'devices', 'capability.switch', multiple: true
	}
}

/**
 * Installed
 */
def installed() {

	// Subscribe to app run mode
	subscribe(app, runApp)
    
    // Save mood
    saveMood()
}

/**
 * Updated
 */
def updated() {

	// Unsubscribe
	unsubscribe()
    
	// Install again
	installed()
}

/**
 * Run app
 */
def runApp(evt) {

	// Set mood
	setMood()
}

/**
 * Set mood
 */
private setMood() {

	// Get mood values
    def mood = atomicState.mood ?: [:]
    
    // For each device
	devices.each { device ->
    
        // -- Set device if it exists
    	if (!mood.containsKey(device.getId())) {
	        return    
        }
        
        // -- Get values
        def values = mood[device.getId()]
        
		// -- Log
     	log.info "Settings mood with device: ${device.getId()} -> ${values['state']} - ${values['level']}"
        
        // -- Setup device
        // -- -- State
        if (values['state'] == 'on') {
        	device.on()
		}
        else {
	        device.off()
        }
        // -- -- Level
		if (values['level'] > 0) {
        	device.setLevel(dimmerValue)
        }
        // -- -- Color
        if (values['color'] != false) {
        	device.setColor(values['color'])
        }
	}
}

/**
 * Save mood
 */
private saveMood() {

	// Get mood values
    def mood = atomicState.mood ?: [:]
    
    // For each device
	devices.each { device ->
        
        // -- What capabilities do we have?
        def capRefresh = false
        def capColor = false
        device.getCapabilities().each { cap ->
        
        	// -- Log
            log.info "Device has capabilities: ${device.getId()} -> ${cap.name}"
            cap.commands.each {comm ->
     		   log.debug "-- Command name: ${comm.name}"
		    }
            
            // -- Save
            if (cap.name == 'Refresh') {
            	capRefresh = true
			}
            if (cap.name == 'Color Control') {
            	capColor = true
            }
            
        }
    
    	// -- Get values
        // -- -- Refresh
        if (capRefresh) {
        	device.refresh()
		}
        // -- -- Level
		if (device.currentValue('level') != null) {
        	def deviceLevel = device.currentValue('level').toInteger()
        }
        else {
        	def deviceLevel = 0
		}
        // -- -- Color
        def deviceColor = false;
        if (capColor) {
			deviceColor = [ level: device.currentValue('level') as Integer,
				hex: device.currentValue('color'),
				saturation: device.currentValue('saturation'),
				hue: device.currentValue('hue')]
        }
        
		// -- Create values
        def values = [:]
        values = [ state: device.currentValue('switch').toString(),
                   level: deviceLevel,
                   color: deviceColor]
                   
		// -- Log
     	log.info "Saving mood with device: ${device.getId()} -> ${values['state']} - ${values['level']}"
                   
		// -- Save
		mood[device.getId()] = values
	}
    
	// Save state
    atomicState.mood = mood
}