/**
 *  SLEEPsense-fall asleep
 *
 *  Copyright 2015 Samsung Mobileapp
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
    name: "SLEEPsense-fall asleep",
    namespace: "samsung",
    author: "Samsung Mobileapp",
    description: "When you fall asleep, do this.",
    category: "Health & Wellness",
    iconUrl: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@2x.png")



preferences {
	page(name:"Mainpage", install:true, uninstall:true) {
        section("When you fall asleep, do this.") {
            input "sleepsensor", "capability.sleepSensor", title: "Sleep Sensor", multiple: false, required: true
        }
        section("Select DoorLocks") {
            input "locks", "capability.lock", multiple: true, required: false
        }
       section("Select lights/bulbs") {
            input "LightLevelController", "capability.switchLevel", title: "Select light", multiple: true, required: false
            input(name: "lightlevel", type: "enum", title: "level", options: ["off", "10%","20%","30%","40%","50%","60%","70%","80%","90%","100%"])
        }
    }
    

}


def installed()
{
	log.trace "installed()"

    subscribe(sleepsensor, "sleeping", sleepsensorHandler)
    subscribe(sleepsensor, "bedstate", sleepsensorHandler)
}

def updated()
{
	log.trace "updated()"
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}


def sleepsensorHandler(evt) {
	//log.debug "sleepsensorHandler: $evt.name"	
    log.debug "sleepsensorHandler: $evt.value"
    state.isOK = false;
    
    if(evt.value == "sleeping")
    {
    	log.debug "Sleeping actions will be excuted"
    	state.isOK = true;
    }
    else
    {
    	return
    }
    
    LightLevelConv()
    LightLevelController()
   
}

def LightLevelConv() {
	if (lightlevel == "off") {
    	state.lightlevel = 0;
    }
	else if (lightlevel == "10%") {
    	state.lightlevel = 10
    }
    else if (lightlevel == "20%") {
    	state.lightlevel = 20
    }
    else if (lightlevel == "30%") {
    	state.lightlevel = 30
    }
    else if (lightlevel == "40%") {
    	state.lightlevel = 40
    }
    else if (lightlevel == "50%") {
    	state.lightlevel = 50
    }
    else if (lightlevel == "60%") {
    	state.lightlevel = 60
    }
    else if (lightlevel == "70%") {
    	state.lightlevel = 70
    }
    else if (lightlevel == "80%") {
    	state.lightlevel = 80
    }
    else if (lightlevel == "90%") {
    	state.lightlevel = 90
    }
    else if (lightlevel == "100%") {
    	state.lightlevel = 100
    }
    
}

def LightLevelController() {
    log.debug "LightLevelController()"
    if(state.isOK == true)
    {    	
    	LightLevelController.each
        {
            log.debug it
            if(state.lightlevel == 0)
            {
	            log.debug "light off"
    	        it?.off()
            }
            else
            {
	            it?.setLevel(state.lightlevel)
            }
        }        
	}
	log.debug state.lightlevel
}

