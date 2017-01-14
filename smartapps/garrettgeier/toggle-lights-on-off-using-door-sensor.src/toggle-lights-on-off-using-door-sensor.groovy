/**
 *  Door Sensor Light Toggle
 *
 *  Copyright 2017 Garrett Geier
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
    name: "Toggle lights on/off using Door Sensor",
    namespace: "GarrettGeier",
    author: "Garrett Geier",
    description: "Toggle lights on/off with a door sensor. When the door is opened, the lights will toggle based on the master switch.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

//main screen options
preferences
{
     //choose the door/contact sensor here.
	section("When this door is opened")
    {
		input "doorToggler", "capability.contactSensor", title: "Select Door Sensor", required: true, multiple: false
    }
    
    // this is the master switch. If this switch is ON, then the lights will turn OFF when the door is opened.
    // If this switch is OFF, then the lights will turn ON when the door is opened.
    // Don't care about if the door is closed, this is designed for a normal door which will eventually be shut. :)
    section("Select the Master switch")
    {
    	input "masterToggle", "capability.switch", title: "Master switch", required: true, multiple: false
    }
    
    //based on the master switch, these will turn on or off.
    section("Stuff you want to turn on/off")
    {
	    input "switchesToToggle", "capability.switch", title: "If master is ON, these will turn OFF. When the master is OFF, these will turn ON.", required: true, multiple: true
	}
}

//required
def installed()
{
	initialize()
}

//required
def updated()
{
	unsubscribe()
	initialize()
}

// the event we care about, when the door is opened.
// see http://docs.smartthings.com/en/latest/capabilities-reference.html#contactsensor for device capabilities
def initialize()
{
    subscribe(doorToggler, "contact.open", toggleSwitches)
}

def toggleSwitches(evt)
{
	//log.debug "$evt.value"
  
	if (evt.value == "open" && masterToggle.currentSwitch == "off")
    {
        //log.debug "Light! Yay!"
		switchesToToggle.on()
        masterToggle.on()
    }
    
    else if (evt.value == "open" && masterToggle.currentSwitch == "on")
    {
        //log.debug "Kill the lights"
		switchesToToggle.off()
        masterToggle.off()
    }
}