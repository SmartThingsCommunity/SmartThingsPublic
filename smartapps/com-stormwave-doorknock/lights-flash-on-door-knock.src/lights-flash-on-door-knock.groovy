/**
 *  Lights Flash on Door Knock
 *
 *  Copyright 2016 Andrew Cornforth
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
    name: "Lights Flash on Door Knock",
    namespace: "com.stormwave.doorknock",
    author: "Andrew Cornforth",
    description: "Flashes lights when activity is noticed at the door.",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light10-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light10-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light10-icn@2x.png")


preferences {
	section("Things") 
    {
		input "doorsensor", "capability.accelerationSensor", title: "Door Sensor", required: true, multiple: true
        input "lights", "capability.switch", title:"Lights", required:true, multiple:true
	}
    section("Settings")
    {
    	input "numFlashes", "number", title:"Number of Flashes", required:true, defaultValue:"5"
    	input "onFor", "number", title:"Time On (ms)", required:true, defaultValue:"1000"
    	input "offFor", "number", title:"Time Off (ms)", required:true, defaultValue:"1000"
	}
    section("Only If")
    {
    	input "switcheson", "capability.switch", title:"Switches Are On", required:false, multiple:true
    	input "switchesoff", "capability.switch", title:"Switches Are Off", required:false, multiple:true
    	input "sleepon", "capability.sleepSensor", title:"Thing Is Asleep", required:false, multiple:true
    	input "sleepoff", "capability.sleepSensor", title:"Thing Is Awake", required:false, multiple:true
    }
    section("Or If")
    {
    	input "or_switcheson", "capability.switch", title:"Switches Are On", required:false, multiple:true
    	input "or_switchesoff", "capability.switch", title:"Switches Are Off", required:false, multiple:true
    	input "or_sleepon", "capability.sleepSensor", title:"Thing Is Asleep", required:false, multiple:true
    	input "or_sleepoff", "capability.sleepSensor", title:"Thing Is Awake", required:false, multiple:true
    }
}

def flashLights()
{
    lights.each 
    { 
        light ->
            def delay = 0L
            if (light.latestValue("switch")=="on")
            {
                numFlashes.times 
                {
                    light.off(delay: delay)
                    delay += offFor
                    light.on(delay: delay)
                    delay += onFor
                }
            }
	        else
            {
                numFlashes.times 
                {
                    light.on(delay: delay)
                    delay += onFor
                    light.off(delay: delay)
                    delay += offFor
                }
			}
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def unsubcribe()
{
	unsubscribe(doorsensor)
}

def initialize() 
{
	subscribe(doorsensor, "acceleration", activityHandler)
}

def activityHandler(evt) 
{
	if (evt.value=="active")
    {
    	// Check conditions
        def valid = true
        switcheson.each 
        { 
            device ->
            	if (device.latestValue("switch")=="off") { valid = false; }
        }
        if (valid)
            switchesoff.each 
            { 
                device ->
                    if (device.latestValue("switch")=="on") { valid = false; }
            }
        if (valid)
            sleepon.each 
            { 
                device ->
                    if (device.latestValue("sleeping")=="not sleeping") { valid = false; }
            }
        if (valid)
            sleepoff.each 
            { 
                device ->
                    if (device.latestValue("sleeping")=="sleeping") { valid = false; }
            }
            
        // Check for "or" values if failed first step
		if (!valid)
        {
        	valid = true
            or_switcheson.each 
            { 
                device ->
                    if (device.latestValue("switch")=="off") { valid = false; }
            }
            if (valid)
                or_switchesoff.each 
                { 
                    device ->
                        if (device.latestValue("switch")=="on") { valid = false; }
                }
            if (valid)
                or_sleepon.each 
                { 
                    device ->
                        if (device.latestValue("sleeping")=="not sleeping") { valid = false; }
                }
            if (valid)
                or_sleepoff.each 
                { 
                    device ->
                        if (device.latestValue("sleeping")=="sleeping") { valid = false; }
                }
		}
        if (valid)
        {
    		flashLights()
        }
	}
}