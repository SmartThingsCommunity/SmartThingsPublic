/**
 *  iHomeSmartPlug iSP5
 *
 *  Copyright 2016 EVRYTHNG LTD
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
 * Last reviewed:20.07.2017
 *   - Added capabilities: Health Check, Outlet, Light
 *   - Added ocfDeviceType
 *   - Changed background colour of tiles
 *   - Added lifecycle functions
 *   - Added ping method
 */
import groovy.json.JsonOutput

metadata {
	definition (name: "iHomeSmartPlug-iSP5", namespace: "ihome_devices", author: "iHome", ocfDeviceType: "oic.d.smartplug") {
		capability "Actuator" 	//The device is an actuator (provides actions)
        capability "Sensor"		//The device s a sensor (provides properties)
   		capability "Refresh"    //Enable the refresh by the user

		capability "Switch"     //Device complies to the SmartThings switch capability

		capability "Health Check"
		capability "Outlet"     //Needed for Google Home
		capability "Light"      //Needed for Google Home

        attribute "firmware","string" //Mapping the custom property firmware
        attribute "model","string"    //Mapping the custom property model (model of the plug)
        attribute "status", "string" //Mapping the status of the last call to the cloud in a message to the user
	}

	tiles(scale: 2){

    	multiAttributeTile(name:"control", type:"generic", width:6, height:4) {
        	tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        	    attributeState( "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "off")
        	    attributeState( "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "on")
			}
			tileAttribute ("device.status", key: "SECONDARY_CONTROL") {
				attributeState "status", label:'${currentValue}'
			}
        }

        standardTile("refresh", "device.refresh", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state ("default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh")
		}
        valueTile("firmware", "device.firmware", width:2, height:2, decoration: "flat") {
        	state "firmware", label:'Firmware v${currentValue}'
    	}
        valueTile("model", "device.model", width:2, height:2, decoration: "flat") {
        	state "model", label:'${currentValue}'
    	}
		main (["control"])
		details (["control","refresh","firmware","model"])
	}
}

def initialize() {
	sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "cloud", scheme:"untracked"]), displayed: false)
}

def installed() {
	log.debug "installed()"
	initialize()
}

def updated() {
	log.debug "updated()"
	initialize()
}

/*
* This method creates the internal SmartThings events to handle changes in the properties of the plug
*/
def updateProperties(Map properties) {

	log.debug "Updating plug's properties: ${properties}"

    def connected = (properties["~connected"]?.value == true)
    if (connected == true){ //only update if plug is connected

        //update status message
  	    sendEvent(name: "status", value: parent.getConnectedMessage())
       	log.info "Updating ${device.displayName}: property status set to: ${parent.getConnectedMessage()}"

		//update currentpowerstate1
        def currentpowerstate1 = properties["currentpowerstate1"].value
        if (currentpowerstate1 != null){
        	log.info "Updating ${device.displayName}: property currentpowerstate1 set to value: ${currentpowerstate1}"
            currentpowerstate1 = "${currentpowerstate1}"
        	if (currentpowerstate1 == "1") {
    			sendEvent(name: "switch", value: "on")
        	}
        	else if (currentpowerstate1 == "0") {
   				sendEvent(name: "switch", value: "off")
        	}
        }

        //update firmware version
        def appfwversion = properties["appfwversion"].value
        if (appfwversion != null){
        	log.info "Updating ${device.displayName}: property appfwversion set to value: ${appfwversion}"
            appfwversion = "${appfwversion}"
   			sendEvent(name: "firmware", value: appfwversion)
        }

		//update model
    	log.info "Updating ${device.displayName}: property model set to value: iSP5"
    	sendEvent(name:"model", value:"iSP5")

    } else { //the plug is not connected

        //update status message
      	sendEvent(name: "status", value: parent.getPlugNotConnectedMessage())
      	log.info "Updating ${device.displayName}: property status set to: ${parent.getPlugNotConnectedMessage()}"
    }
}

// Process the polling error, changing the status message
def pollError(){
  	log.info "Error retrieving info from the cloud"
	sendEvent(name: "status", value: parent.getConnectionErrorMessage())
}

/*
* This method handles the switch.on function by updating the corresponding property in the cloud
*/
def on() {

    //update the status of the plug before attempting to change it
	refresh()

	if (device.currentState("status")?.value == parent.getConnectedMessage()) {//only update if the plug is connected
		//Turn on if the device is off
		if (device.currentState("switch")?.value.toLowerCase().startsWith("off")){
       		log.info "Updating ${device.displayName} in the cloud: property targetpowerstate1 set to value: 1"

			def propertyUpdateJSON = "[{\"key\":\"targetpowerstate1\", \"value\":\"1\"}]"
    		def success = parent.propertyUpdate(device.deviceNetworkId, propertyUpdateJSON)

        	if(success){
        		log.info "Updating ${device.displayName}: sending switch.on command"
 				sendEvent(name: "switch", value: "on")
            	sendEvent(name: "status", value: parent.getConnectedMessage())
        	} else {
        		log.info "Cloud property update error, skipping event"
				sendEvent(name: "status", value: parent.getConnectionErrorMessage())
        	}
    	}
    }
}

/*
* This method handles the switch.off function by updating the corresponding property in the cloud
*/
def off() {
    //update the status of the plug before attempting to change it
	refresh()

	if (device.currentState("status")?.value == parent.getConnectedMessage()) {//only update if the plug is connected
		//Turn off if the device is on
		if (device.currentState("switch")?.value.toLowerCase().startsWith("on")){
        	log.info "Updating ${device.displayName} in the cloud: property targetpowerstate1 set to value: 0"
    		def propertyUpdateJSON = "[{\"key\":\"targetpowerstate1\", \"value\":\"0\"}]"
			def success = parent.propertyUpdate(device.deviceNetworkId, propertyUpdateJSON)

        	if (success){
	    		log.info "Updating ${device.displayName}: sending switch.off command"
    			sendEvent(name: "switch", value: "off")
            	sendEvent(name: "status", value: parent.getConnectedMessage())
        	} else {
        		log.info "Cloud property update error, skipping event"
            	sendEvent(name: "status", value: parent.getConnectionErrorMessage())
        	}
   		}
   }
}

/*
* This method handles the refresh capability
*/
def refresh() {
    parent.pollChildren(device.deviceNetworkId)
}

def ping() {
	refresh()
}