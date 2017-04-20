/**
 *  Simulated Refrigerator
 *
 *  Example composite device handler that simulates a refrigerator with a freezer compartment and a main compartment.
 *  Each of these compartments has its own door, temperature, and temperature setpoint. Each compartment modeled
 *  as a child device of the main refrigerator device so that temperature-based SmartApps can be used with each
 *  compartment
 *
 *  Copyright 2017 SmartThings
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
metadata {
	definition (name: "Simulated Refrigerator", namespace: "smartthings/testing", author: "SmartThings") {
		capability "Contact Sensor"
	}

	tiles(scale: 2) {
    	// Row 1: Refrigerator door open/close display
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state("closed", label:'${name}', icon:"st.fridge.fridge-closed", backgroundColor:"#79b821")
			state("open", label:'${name}', icon:"st.fridge.fridge-open", backgroundColor:"#ffa81e")
		}
        // Row 1: Fridge and Freezer Door open/close displays
		childDeviceTile("mainDoor", "mainDoor", height: 2, width: 2, childTileName: "mainDoor")
		childDeviceTile("freezerDoor", "freezerDoor", height: 2, width: 2, childTileName: "freezerDoor")
        
        // Row 2: Label for Door open/close controls
        valueTile("doorControlLabel", "device.contact", width: 2, height: 2, decoration: "flat") {
            state "contact", label:'Door Control'
    	}
        // Row 2: Door open/close controls from "Simulated Refrigerator Door" child 
        childDeviceTile("mainDoorControl", "mainDoor", height: 2, width: 2, childTileName: "control")
        childDeviceTile("freezerDoorControl", "freezerDoor", height: 2, width: 2, childTileName: "control")
		
        // Row 3: Refrigerator and freezer temperature displays from "Simulated Refrigerator Temperature Control" child
		childDeviceTile("refrigerator", "refrigerator", height: 2, width: 3) //, childTileName: "refrigerator")
		childDeviceTile("freezer", "freezer", height: 2, width: 3, childTileName: "freezer")
        
        // Row 4: Label for up/down temp controls for refrigerator
        valueTile("fridgUpDownLabel", "device.contact", width: 1, height: 1, decoration: "flat") {
            state "contact", label:'Fridge'
    	}
        // Row 4: Up/down temp controls for the refrigerator
        childDeviceTile("refrigeratorUp", "refrigerator", height: 1, width: 1, childTileName: "tempUp")
		childDeviceTile("refrigeratorDown", "refrigerator", height: 1, width: 1, childTileName: "tempDown")
        // Row 4: Label for up/down temp controls for the freezer
        valueTile("freezerUpDown", "device.contact", width: 1, height: 1, decoration: "flat") {
            state "contact", label:'Freezer'
    	}
        // Row 4: Up/down temp controls for the freezer
		childDeviceTile("freezerUp", "freezer", height: 1, width: 1, childTileName: "tempUp")
		childDeviceTile("freezerDown", "freezer", height: 1, width: 1, childTileName: "tempDown")
       
       	// Row 5: "Set Fridge:" label and up/down controls display from "Simulated Refrigerator Temperature Control" child
		childDeviceTile("refrigeratorSetpoint", "refrigerator", height: 1, width: 1, childTileName: "refrigeratorSetpoint")  
        childDeviceTile("refrigeratorSetpointUp", "refrigerator", height: 1, width: 1, childTileName: "setpointUp")
		childDeviceTile("refrigeratorSetpointDown", "refrigerator", height: 1, width: 1, childTileName: "setpointDown")
       
        // Row 5: "Set Freezer:" label and up/down controls display from "Simulated Refrigerator Temperature Control" child
		childDeviceTile("freezerSetpoint", "freezer", height: 1, width: 1, childTileName: "freezerSetpoint")
        childDeviceTile("freezerSetpointUp", "freezer", height: 1, width: 1, childTileName: "setpointUp")
		childDeviceTile("freezerSetpointDown", "freezer", height: 1, width: 1, childTileName: "setpointDown")
        
        main "contact"
	}
}

def installed() {
	state.counter = state.counter ? state.counter + 1 : 1
	if (state.counter == 1) {
		addChildDevice(
        		// Explicitly calling the child Device Handler with the name "Simulated Refrigerator Door" is how it is associated with this parent Device Handler.
				"Simulated Refrigerator Door",
				"${device.deviceNetworkId}.1",
				null,
				[completedSetup: true, label: "${device.label} (Freezer Door)", componentName: "freezerDoor", componentLabel: "Freezer Door"])

		addChildDevice(
				"Simulated Refrigerator Door",
				"${device.deviceNetworkId}.2",
				null,
				[completedSetup: true, label: "${device.label} (Main Door)", componentName: "mainDoor", componentLabel: "Main Door"])

		addChildDevice(
				"Simulated Refrigerator Temperature Control",
				"${device.deviceNetworkId}.3",
				null,
				[completedSetup: true, label: "${device.label} (Freezer)", componentName: "freezer", componentLabel: "Freezer"])

		addChildDevice(
				"Simulated Refrigerator Temperature Control",
				"${device.deviceNetworkId}.3",
				null,
				[completedSetup: true, label: "${device.label} (Fridge)", componentName: "refrigerator", componentLabel: "Fridge"])
	}
}

def doorOpen(dni) {
	// If any door opens, then the refrigerator is considered to be open
	sendEvent(name: "contact", value: "open")
}

def doorClosed(dni) {
	// Both doors must be closed for the refrigerator to be considered closed
	if (!childDevices.find{it.deviceNetworkId != dni && it.currentValue("contact") == "open"}) {
		sendEvent(name: "contact", value: "closed")
	}
}