/**
 *  Water Sensor Child Device
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
	definition (name: "Water Sensor Child Device", namespace: "erocm123", author: "Eric Maycock", vid:"generic-leak") {
		capability "Water Sensor"
		capability "Sensor"
	}

	tiles() {
		multiAttributeTile(name:"water", type: "generic", width: 6, height: 4){
			tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
				attributeState("dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff")
            	attributeState("wet", icon:"st.alarm.water.wet", backgroundColor:"#00a0dc")
			}
		}
	}

}
