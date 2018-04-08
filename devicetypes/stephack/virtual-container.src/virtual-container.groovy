/**
 *  Virtual Container (Device Handler)
 *
 *  Copyright 2017 Stephan Hackett
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
 *	
 */

metadata {
	definition (name: "Virtual Container", namespace: "stephack", author: "Stephan Hackett") {
	//capability "Switch"
	//capability "Refresh"
	//capability "Switch Level"
	//capability "Sensor"
	//capability "Actuator"
    
    attribute "noVMS", "number"
	}

	tiles(scale: 2) {		
        valueTile("noVMS","noVMS"){
        	state "default", label: '${currentValue}', icon: "https://cdn.rawgit.com/stephack/sonosVC/master/resources/images/sp.png"
        }
        
        childDeviceTiles("all", height: 1, width: 6)
        
		main "noVMS"
		details(["all"])
	}
}

def on(whichChild) {
    log.info "VMS was pushed sending from VC to SA"
	parent.childOn(whichChild)
}

def off(whichChild) {
	parent.childOff(whichChild)
}

def setLevel(val){
    log.info "VMS setLevel $val. Sending from VC to SA"
    //on()
    parent.childSetLevel(val)
    sendEvent(name:"level",value:val)
}

def createChildVMS(vmsNo,vmsName) {	
	   	def childDevice = getChildDevices()?.find {it.componentName == "VMS-${vmsNo}"}
        //log.debug childDevice
        if (!childDevice) {
           	log.info "Creating VMS ${vmsNo}:${vmsName}"
        	childDevice = addChildDevice("Virtual Momentary Switch", "VMS_${parent.app.id} ${vmsNo}", null,[completedSetup: true,
            label: "${vmsName}", name: "${vmsName}", isComponent: true, componentName: "VMS-${vmsNo}", componentLabel: "${vmsName}", "data":["vms":"${vmsNo}"]])
            //childDevice.refresh() 
         }
		
       	else {
        	log.info "VMS ${vmsNo} already exists"
            childDevice.label=vmsName
            childDevice.name=vmsName
            //childDevice.componentLabel = vmsName
            childDevice.updateComponentLabel("${vmsName}")
            //log.error childDevice.componentLabel
            //log.error vmsName
            log.info "VMS ${vmsNo} name is now [${vmsName}]"

		}
}

def deleteChildVMS(which) {
	log.info "start delete"
	if(which=="all"){ 
    log.debug "all"
    	def childDevice = getAllChildDevices()?.findAll{
        	it.deviceNetworkId.startsWith("VMS")
       	}
        if (childDevice) {
        	log.info "Deleting all Virtual Mometary Switches: ${childDevice}"
        	childDevice.each {child->
  				deleteChildDevice(child.deviceNetworkId)            
    		}
        }
    }
    else{
    	log.debug "dsome"
    	def childDevice = getChildDevices()?.find{
        	it.deviceNetworkId.startsWith("VMS") && it.deviceNetworkId.endsWith("${which}") 
       	}
        log.info "Deleting VMS: [${childDevice}]"
        childDevice?deleteChildDevice(childDevice.deviceNetworkId):""
    } 
}