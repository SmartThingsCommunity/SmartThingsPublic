/**
 *  Lock Doors When Someone Leaves
 *
 *  Copyright 2016 Patrick O'Connor
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
    name: "Lock Doors When Someone Leaves",
    namespace: "RobertSheaO",
    author: "Patrick O'Connor",
    description: "Lock a door when someone leaves, not just when everyone leaves.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/ha-sst/lockdoorswhensomeoneleaves.png",
    iconX2Url: "https://s3.amazonaws.com/ha-sst/lockdoorswhensomeoneleaves@2x.png",
    iconX3Url: "https://s3.amazonaws.com/ha-sst/lockdoorswhensomeoneleaves@3x.png")


preferences {
	section("When these people leave...") {
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Lock these locks...") {
		input "lock1","capability.lock", multiple: true
        input("recipients", "contact", title: "Send notifications to") {
            input "pushNotification", "enum", title: "Send Me Notifications?", options: ["Yes", "No"]
        }
	}
}

def installed()
{
	subscribe(presence1, "presence", presence)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence", presence)
}

def presence(evt)
{
	if (evt.value != "present") {
		def nobodyHome = presence1.find{it.currentPresence == "present"} == null
		if (nobodyHome) {
            sendMessage("Doors locked after $evt.linkText departed")
			lock1.lock()
		}
	}
}

def sendMessage(msg) {
    if (pushNotification == "Yes") {
        sendPush msg
    }
}