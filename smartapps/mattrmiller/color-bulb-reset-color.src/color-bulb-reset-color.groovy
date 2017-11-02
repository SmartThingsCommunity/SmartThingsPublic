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
definition (
    name: 'Color Bulb Reset Color',
    namespace: 'mattrmiller',
    author: 'mattrmiller',
    description: 'Return color bulbs to previous setting on closure of contact sensor(s).',
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
	section('When these lights turn on...') {
		input 'lights', 'capability.switch'
	}
    
    // Section for bulbs
	section('Return these bulbs to the color of last turned on...') {
		input 'bulbs', 'capability.colorControl', multiple: true
	}
}

/**
 * Installed
 */
def installed() {

    // Subscribe to lights turning on and off
	subscribe(lights, 'switch.on', lightTurnsOn)
    subscribe(lights, 'switch.off', lightTurnsOff)
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
 * Lights turn on
 */
def lightTurnsOn(evt) {

	// Get bulb values
    def bulbValues = atomicState.bulbValues ?: [:]

	// For each bulb
	bulbs.each { bulb ->
    
    	// -- Set color if it exists
    	if (bulbValues.containsKey(bulb.getId())) {
        
  	    	// -- -- Log
			log.info "Setting values for bulb: ${bulb.getId()} -> {$bulb.currentColor} -> ${bulbValues[bulb.getId()]['hex']}"
        
        	// -- -- Set color
			bulb.setColor(bulbValues[bulb.getId()])
		}
	}
    
	// Save state
    atomicState.bulbValues = bulbValues
}

/**
 * Lights turn off
 */
def lightTurnsOff(evt) {

	// Get bulb values
	def bulbValues = atomicState.bulbValues ?: [:]

	// For each bulb
	bulbs.each { bulb ->
    
    	// -- Refresh to get latest values
        bulb.refresh()
    
    	// -- Create values
        def values = [:]
        values = [ level: bulb.currentValue('level') as Integer,
                   hex: bulb.currentValue('color'),
                   saturation: bulb.currentValue('saturation'),
                   hue: bulb.currentValue('hue')]

        // -- Log
     	log.info "Saving values for bulb: ${bulb.getId()} -> ${values['hex']}"

		// -- Save
		bulbValues[bulb.getId()] = values
	}
    
	// Save state
    atomicState.bulbValues = bulbValues
}
