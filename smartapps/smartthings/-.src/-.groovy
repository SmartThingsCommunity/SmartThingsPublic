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
 *  Lights Off, When Closed
 *
 *  Author: SmartThings
 */
definition(
    name: "불을 꺼라",
    namespace: "smartthings 불을꺼라",
    author: "LEE HYE JIN",
    description: "문이 닫히면 불을 꺼라",
    category: "Convenience",
    iconUrl: "http://postfiles15.naver.net/20110419_110/fm20_1303196773441csi0D_JPEG/%C0%FC%B1%B8.jpg?type=w2",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

preferences {
	section ("When the door closes...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section ("Turn off a light...") {
		input "switch1", "capability.switch"
	}
}

def installed()
{
	subscribe(contact1, "contact.closed", contactClosedHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.closed", contactClosedHandler)
}

def contactClosedHandler(evt) {
	switch1.off()
}