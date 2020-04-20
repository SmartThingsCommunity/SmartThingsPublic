/**
 *  Xandem Home Integration
 *
 *  Copyright 2017 Derek Twaddle
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
 * README
 *
 * Hello, after purchsing Xandem Home for a rental property I noticed there were no SmartApps yet developed.  This is my
 * first attempt at integrating Xandem with SmartThings out of personal need and decided to share with the community.
 *
 * Depending on your ISP and setup, there are some preliminary steps that need to be performed before installing this app.  Xandem is initially
 * accessible only via local network so you will need to make some minor rule changes to your firewall / router.
 *
 * If you have a static IP assignment from your ISP, you may forgo the following step.  If your ISP assigns an IP Address to you Dynamically
 * which is what most do at Resedential installations, you will need a Dynamic DNS service.  Dynamic DNS will map your randomly assigned IP
 * address to a static name like (Ex: myhome.dyndns.com). No matter what your IP changes to, it will always be resolved using the same name you chose.
 *
 * Once the above is completed, you will need to configure your router to pass or port forward to your internal Xandem Hub.  You will
 * want to statically assign a local IP to the mac address of the Xandem Hub.  This way you will always assign the same local IP to the Xandem Hub which can be mapped
 * and used in port forwarding rules.  After the IP has been assigned (Ex: 192.168.1.25) add a rule on your rounter to pass incoming Port 80 request
 * to your Xandem Hub IP address on the same port.  This effectively lets your router listen for and pass data from SmarthThings to your Xandem Hub.
 * 
 * Once the above networking steps are completed, create a Xandem API key on your local Xandem Hub.  Instructions can
 * found here: http://documentation.xandem.com/api/#api-keys-and-authorization-header
 *
 * This SmartApp is a personal project you are free to use.
*/

definition(
    name: "Xandem Home Integration",
    namespace: "Xandem",
    author: "Derek Twaddle",
    description: "Integrates Xandem Home to SmartThings. Detects motion, if above a user set threshold, turn on light.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
    )

preferences {
    section("Network Configuration") {
        input "ddns", "text", title: " External Hostname or Static IP", required: true
        paragraph "Example: mynetwork.dyndns.org or 64.102.0.1"
        input "apikey", "text", title: "API Key", required: true
        paragraph "Enter the Xandem API Key generated on your local Hub"
    }
    section("Turn on when motion detected and at or above level:") {
    	input "thelevel", "text", title: "1 - 10", required: true
        paragraph "Motion Level: 1 Minimal Motion - 10 High Motion"
    }
    section("Turn on this light") {
        input "theswitch", "capability.switch", required: true
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
    runEvery5Minutes(updateStatus)
}

def updateStatus() {
    def params = [
        uri: "http://${ddns}/v1/data",
        headers: [Authorization: "${apikey}"],
        body: [data_fields: ["motion_score", "is_motion"]]
	]

	try {
    	httpPostJson(params) { resp ->
        	resp.headers.each {
       	 }
        
        // If motion detected and motion level is at or above chosen level, Light On
        if (resp.data.is_motion != 0 && resp.data.motion_score >= thelevel){
            theswitch.on()
            log.debug "Motion detected, light on"
        } 
   	 }
	} catch (e) {
    	log.debug "something went wrong: $e"
	}
}