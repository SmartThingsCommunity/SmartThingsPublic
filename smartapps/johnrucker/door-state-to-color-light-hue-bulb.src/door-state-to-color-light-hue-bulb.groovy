/**
 *  CoopBoss Door Status to color
 *
 *  Copyright 2015 John Rucker
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
    name: "Door State to Color Light (Hue Bulb)",
    namespace: "JohnRucker",
    author: "John Rucker",
    description: "Change the color of your Hue bulbs based on your coop's door status.",
    category: "My Apps",
    iconUrl: "http://coopboss.com/images/SmartThingsIcons/coopbossLogo.png",
    iconX2Url: "http://coopboss.com/images/SmartThingsIcons/coopbossLogo2x.png",
    iconX3Url: "http://coopboss.com/images/SmartThingsIcons/coopbossLogo3x.png")


preferences {
	section("When the door opens/closese...") {
    	paragraph "Sets a Hue bulb or bulbs to a color based on your coop's door status:\r  unknown = white\r  open = blue\r  opening = purple\r  closed = green\r  closing = pink\r  jammed = red\r  forced close = orange."
		input "doorSensor", "capability.doorControl", title: "Select CoopBoss", required: true, multiple: false
		input "bulbs", "capability.colorControl", title: "pick a bulb", required: true, multiple: true
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
	subscribe(doorSensor, "doorState", coopDoorStateHandler)
}

def coopDoorStateHandler(evt) {
    log.debug "${evt.descriptionText}, $evt.value"
	def color = "White"
    def hueColor = 100
    def saturation = 100
    Map hClr = [:]
    hClr.hex = "#FFFFFF"

	switch(evt.value) {
    	case "open":
        	color = "Blue"
            break;
        case "opening":
        	color = "Purple"
            break;
        case "closed":
        	color = "Green"
            break;
    	case "closing":
        	color = "Pink"
            break;
        case "jammed":
        	color = "Red"
            break;
        case "forced close":
        	color = "Orange"
            break;
        case "unknown":
        	color = "White"
            break;        
    }   
       
	switch(color) {
		case "White":
			hueColor = 52
			saturation = 19
			break;
		case "Daylight":
			hueColor = 53
			saturation = 91
			break;
		case "Soft White":
			hueColor = 23
			saturation = 56
			break;
		case "Warm White":
			hueColor = 20
			saturation = 80 //83
			break;
		case "Blue":
			hueColor = 70
            hClr.hex = "#0000FF"
			break;
		case "Green":
			hueColor = 39
            hClr.hex = "#00FF00"
			break;
		case "Yellow":
			hueColor = 25
            hClr.hex = "#FFFF00"            
			break;
		case "Orange":
			hueColor = 10
            hClr.hex = "#FF6000"
			break;
		case "Purple":
			hueColor = 75
            hClr.hex = "#BF7FBF"
			break;
		case "Pink":
			hueColor = 83
            hClr.hex = "#FF5F5F"
			break;
		case "Red":
			hueColor = 100
            hClr.hex = "#FF0000"
			break;
	}    
    
    //bulbs*.on()
    bulbs*.setHue(hueColor)
	bulbs*.setSaturation(saturation)   
    bulbs*.setColor(hClr)
    
    //bulbs.each{
    	//it.on()  // Turn the bulb on when open (this method does not come directly from the colorControl capability)
    	//it.setLevel(100)  // Make sure the light brightness is 100%       
    	//it.setHue(hueColor)
		//it.setSaturation(saturation) 
        //}        
}