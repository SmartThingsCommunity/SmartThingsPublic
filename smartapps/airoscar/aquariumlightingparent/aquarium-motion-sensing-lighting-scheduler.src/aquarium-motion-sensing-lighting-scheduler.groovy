/**
 *  Aquarium Light Scheduler 
 *
 *  Copyright 2016 Oscar Chen
 *	This is a parent app, for actual app functionality look up child app: airoscar/aquariumlightingautomation
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
    name: "Aquarium Motion Sensing Lighting Scheduler",
    namespace: "airoscar/aquariumlightingparent",
    author: "Oscar Chen",
    singleInstance: true,
    description: '''A smart aquarium lighting scheduler that conforms to a minimum and maximum set # of hours per day, 
    which also has the flexibility of triggering the lights on by motion sensor for user to view the aquarium, while 
    ensuring the maximum lighting threshold is not breached. The Smart Aquarium Lighting Scheduler behaves as the follow:
    (1) It will turn on the light in the morning at a set time ('Do not turn on before' setting) if desired;
    (2) It will turn on the light when there is motion during the day between the allowed time frame;
    (3) It will turn off the light after a certain set amount of time has lapsed since the motion has stopped;
    (4) It will monitor how many hours the light has been running today and yesterday;
    (5) It will turn on the light automatically close to end of the day if the light has not been running the desired number of hours today;
    (6) The desired number of hours of light is the average of the two settings : 'Minimum # of Hours' & 'Maximum # of Hours';
    (7) It will target today's run time based on yesterday's. Ie: if the light had ran closer to the 'Minimum # of Hours' yesterday, today it will try to be closer to 'maximum # of Hours';
    (8) However, the motion sensor will be able to turn on the light as long as 'Maximum # of Hours' is not breached, and the time is winthin the allowable time frame;
    (9) Finally, It will turn off the light in the evening at a set time ('Do not turn on after' setting); You can still manually toggle the light on/off in the after hour, but it will be turned off automatically shortly after if you leave it on;
    (10) Additional function: a notification setting can be turned on, to report at a set time once per day: run time today, and run time yesterday.
    (11) Additional function: you can toggle off the lights while the motion sensor is active, this would set off a bypass timer, during which the motion sensor would not turn on the light.
    
    ''',
    
    category: "Pets",
    iconUrl: "http://cdn.device-icons.smartthings.com/Outdoor/outdoor3-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Outdoor/outdoor3-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Outdoor/outdoor3-icn@2x.png")
    
preferences {
	page(name: "mainPage", title: "Automations", install: true, uninstall: true) {
        section {
            app(name: "Automations", appName: "Aquarium Motion Sensing Lighting Automation", namespace: "airoscar/aquariumlightingautomation", title: "New Automation", multiple: true)
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

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}

