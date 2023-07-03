/**
 *  Smart Motion Light
 *
 *  Copyright 2017 JANG JAEWON
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
    name: "Smart Motion Light",
    namespace: "ipse",
    author: "JANG JAEWON",
    description: "Activate motion sensor light only when specific lights are off. Light turns off in 1 minute.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section ("Where") {
		input "motion1", "capability.motionSensor", title: "Which Motion Sensor?",required:true
	}
	section ("When these lights are off...") {
		input "darkSwitches", "capability.switch", title: "Which?",required:true,multiple:true
	}
	section ("Turn on this light...") {
		input "onSwitch", "capability.switch", title: "Which?",required:true
	}
}

def installed()
{
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def motionActiveHandler(evt) {
	def currSwitches = darkSwitches.currentSwitch
    def countSwitches = currSwitches.findAll{it == "off"?true:false}
	if (darkSwitches.size() == countSwitches.size()) {
        onSwitch.on()
        runIn(60,turnOff)
    }
}

def turnOff(){
	onSwitch.off()
}