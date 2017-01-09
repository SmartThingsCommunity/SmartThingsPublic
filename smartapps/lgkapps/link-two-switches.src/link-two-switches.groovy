/**
 *  Copyright 2015 SmartThings
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
 *  Links Two Switchs
 *
 *  Author: kahn@lgk.com
 *
 *  Date: 2015-11-08
 */
definition(
	name: "Link Two Switches",
	namespace: "lgkapps",
	author: "kahn@lgk.com",
	description: "Based on the big switch but links two switches so one either one changes the other follows suit.  You can two this with the new smart lighting, but you would need two separate smartapps instead of one.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("Master Switch?") {
		input "master", "capability.switch", title: "Where?"
	}
    section("Linked Switch?") {
		input "linked", "capability.switch", title: "Where?"
	}
}

def installed()
{   
	subscribe(master, "switch.on", onHandler)
	subscribe(master, "switch.off", offHandler)
	//subscribe(master, "level", dimHandler)
    
    subscribe(linked, "switch.on", linkedOnHandler)
	subscribe(linked, "switch.off", linkedOffHandler)
	//subscribe(linked, "level", linkedDimHandler)   
    
}

def updated()
{
	unsubscribe()
	subscribe(master, "switch.on", onHandler)
	subscribe(master, "switch.off", offHandler)
	
    subscribe(linked, "switch.on", linkedOnHandler)
	subscribe(linked, "switch.off", linkedOffHandler)
	
	log.debug "in updated ... master state=  $master.currentSwitch"
	log.debug "in updated ... linked state= $linked.currentSwitch"
}

def logHandler(evt) {
	log.debug evt.value
}

def onHandler(evt) {
	log.debug "In Master on handler"
	log.debug evt.value	
    if (linked.currentSwitch == "off")
     {	 log.debug "Master turned On, (Linked was off) turning it on"
   		 linked.on()
     }
}

def linkedOnHandler(evt) {
	log.debug "In Linked on handler"
	log.debug evt.value
     if (master.currentSwitch == "off")
     {	 log.debug "Linked turned On, (Master was off) turning it on"
   		 master.on()
     }
}

def offHandler(evt) {
	log.debug evt.value
    log.debug "In Master off handler"
	  if (linked.currentSwitch == "on")
     {	 log.debug "Master turned Off, (Linked was on) turning it off"
   		 linked.off()
     }
}

def linkedOffHandler(evt) {
	log.debug evt.value
    log.debug "In Linked off handler"
	  if (master.currentSwitch == "on")
     {	 log.debug "Linked turned Off, (Master was on) turning it off"
   		master.off()
     }
}

def dimHandler(evt) {
	log.debug "In Master Dim Handler"
	log.debug "Dim level: $evt.value"
	linked.setLevel(evt.value)
}


def LinkedDimHandler(evt) {
	log.debug "In Linked Dim Handler"
	log.debug "Dim level: $evt.value"
	// do nothing here as we would go in a loop. I dont have a dimmer on linked switch anyeaymaster.setLevel(evt.value)
}
