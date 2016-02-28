/**
 *  j64Partition
 *
 *  Copyright 2016 Joe Jarvis
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
	definition (name: "j64 Partition", namespace: "j64", author: "Joe Jarvis") {
		capability "Actuator"
		capability "Refresh"
		capability "Polling"
    
        attribute "alarm", "string"
        attribute "mode", "string"
        
        command "awayArmPartition"
        command "stayArmPartition"
        command "disarmPartition"
        command "soundAlarm"
    }

	simulator {
	}

  tiles {
    // Main Row
    standardTile("alarm", "device.alarm", width: 2, height: 2, canChangeBackground: true, canChangeIcon: true, decoration: "flat") {
      state "alarm",    label: '${name}', icon: "st.contact.contact.open",   backgroundColor: "#b82121"
      state "armed",    label: '${name}', icon: "st.contact.contact.open",   backgroundColor: "#2147b8"
      state "disarmed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#6cb821"
    }
       
    standardTile("awayArm", "device.alarm", decoration: "flat") {
      state("disarmed", label:'Away Arm', action:"awayArmPartition", icon:"st.Transportation.transportation6", defaultState: true)
    }
        
    standardTile("stayArm", "device.alarm", decoration: "flat") {
      state("disarmed", label:'Stay Arm', action:"stayArmPartition", icon:"st.Bedroom.bedroom2", defaultState: true)
    }
        
    standardTile("disarm", "device.alarm", decoration: "flat") {
      state("default", label:'Dis Arm', action:"disarmPartition", icon:"st.Electronics.electronics6", defaultState: true)
    }
    
    standardTile("soundAlarm", "device.alarm", decoration: "flat", width: 2, height: 1) {
      state("default", label:'Sound Alarm', action:"soundAlarm", icon:"st.Electronics.electronics19", defaultState: true)
    }

    standardTile("refresh", "device.alarm", decoration: "flat") {
      state("default", label:'', action:"refresh", icon:"st.secondary.refresh", defaultState: true)
    }
    
    valueTile("mode", "device.mode", width: 2, height: 1, canChangeBackground: true, canChangeIcon: true, decoration: "flat") {
      state "Exit Delay",      label: '${currentValue}', backgroundColor: "#ffffff"
      state "Entry Delay",     label: '${currentValue}', backgroundColor: "#ffffff"
	  state "Ready To Arm",    label: '${currentValue}', backgroundColor: "#ffffff"
      state "Zone Not Ready",  label: '${currentValue}', backgroundColor: "#ffffff"
      state "Stay Mode",       label: '${currentValue}', backgroundColor: "#ffffff"
      state "Away Mode",       label: '${currentValue}', backgroundColor: "#ffffff"
      state "Tamper",          label: '${currentValue}', backgroundColor: "#ffffff"
      state "Fault",           label: '${currentValue}', backgroundColor: "#ffffff"
    }

    // This tile will be the tile that is displayed on the Hub page.
    main "alarm"

    // These tiles will be displayed when clicked on the device, in the order listed here.
    details(["alarm", "awayArm", "stayArm", "mode", "disarm", "soundAlarm", "refresh"])
    }
}

def setAlarm(alarm, armed) {
	if ("${alarm}".toLowerCase() == "true") {
	  	sendEvent (name: "alarm", value: "alarm")
	} else {
		if ("${armed}".toLowerCase() == "true")
		  	sendEvent (name: "alarm", value: "armed")
		else
			sendEvent (name: "alarm", value: "disarmed")
	}
}

def setMode(mode, readyToArm) {
	if (mode == "ExitDelayInProgress")
		sendEvent (name: "mode", value: "Exit Delay")
    
	if (mode == "EntryDelayInProgress")
		sendEvent (name: "mode", value: "Entry Delay")

	if (mode == "Away")
  		sendEvent (name: "mode", value: "Away Mode")

	if (mode == "Stay")
  		sendEvent (name: "mode", value: "Stay Mode")

	if (mode == "Tamper")
  		sendEvent (name: "mode", value: "Tamper")

	if (mode == "Fault")
  		sendEvent (name: "mode", value: "Fault")

	if (mode == "NotArmed") {
        if ("${readyToArm}".toLowerCase() == "true")
	  		sendEvent (name: "mode", value: "Ready To Arm") 
		else
        	sendEvent (name: "mode", value: "Zone Not Ready")
	}
}

def awayArmPartition(val) {
	log.debug "Away arm requested"
	parent.armPartition(device.deviceNetworkId.replaceAll("partition",""), "away")
}

def stayArmPartition(val) {
	log.debug "Stay arm requested"
	parent.armPartition(device.deviceNetworkId.replaceAll("partition",""), "stay")
}

def disarmPartition() {
	log.debug "Disarm requested"
	parent.disarmPartition(device.deviceNetworkId.replaceAll("partition",""))
}

def soundAlarm() {
	log.debug "Sound alarm requested"
	parent.soundAlarm(device.deviceNetworkId.replaceAll("partition",""))
}

def poll() {
}

def refresh() {
	parent.refreshPartition(device.deviceNetworkId.replaceAll("partition",""))
}

