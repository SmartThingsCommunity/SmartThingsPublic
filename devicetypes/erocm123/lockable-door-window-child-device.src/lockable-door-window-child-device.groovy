/**
 *  Lockable Door/Window Child Device
 *
 *  Copyright 2017 Eric Maycock
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
metadata {
	definition (name: "Lockable Door/Window Child Device", namespace: "erocm123", author: "Eric Maycock", vid:"generic-lock") {
		capability "Contact Sensor"
        capability "Lock"
		capability "Sensor"
	}

	tiles() {
		tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
                attributeState "closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00a0dc"
				attributeState "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13"
			}
            tileAttribute ("lock", key: "SECONDARY_CONTROL") {
                attributeState "locked", label:'locked', icon:"st.locks.lock.locked", backgroundColor:"#00A0DC", nextState:"unlocking"
			    attributeState "unlocked", label:'unlocked', icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff", nextState:"locking"
            }
		}
        

		main "contact"
		details(["contact", childDeviceTiles("all")])
	}
	}

}
