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
 *  Monitors a switch for ON condition and switches OFF after time delay.
 *
 *  Author: SmartThings modified by TC Hayes
 *
 *  This app detects the ON status of a switch, waits X seconds, then switches off.
 *  May be used anywhere a short on/off condition of a few seconds is needed, for example:
 *  --- An automatic doorbell where a motion sensor switches ON a buzzer. This app terminates the buzz after X seconds. 
 *  --- An automated pet/fish feeder where a separate timer app turns on the feed switch. This app turns off the flow after X seconds. Use along 
 *      with a delay timer between feeds eg (Home Monitor / Custom / New Monitoring Rule ...). 
 *  --- A garage door opener actuator where an on/off pulse of X seconds is needed to either open or close. Any other app can trigger
 *      the ON condition by detecting motion, button operation of a button, or elapsed time. This app will teminate the pulse with an OFF signal. Use
 *      ST Smart Home Monitor (Custom / New Monitoring Rule ... ) with this app to close a door left open.
 *  --- Use with app 'Switch ON After Delay' to create a continuing on/off flashing sequence. (Lighthouse.)
 **/
definition(
    name: "Switch OFF After Delay",
    namespace: "tchayes57",
    author: "tchayes57",
    description: "Monitor switch and respond to an ON condition with OFF after X seconds.",
    category: "Convenience",
    )

preferences {
	section("Switch to be monitored ...") {
		input "switch1", "capability.switch", required: true
	}
    
    section("Set Delay")  {
	    input "openTimer", "number", title: "Number of seconds delay", required: true
		}
}

def installed() {
	subscribe(app, appTouchHandler)
	subscribeToCommand(switch1, "on", onCommand)
}

def updated() {
	unsubscribe()
	subscribe(app, appTouchHandler)
	subscribeToCommand(switch1, "on", onCommand)
}
// identify transition to ON
def appTouch(evt) {
	log.debug "appTouch: $evt.value, $evt"
	switch1?.on()
}

// initiate OFF after delay
def onCommand(evt) {
	log.debug "onCommand: $evt.value, $evt"
	switch1?.off(delay: openTimer * 1000)
}