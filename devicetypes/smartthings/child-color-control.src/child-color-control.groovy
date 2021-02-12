/*	Copyright 2020 SmartThings
*
*	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*	in compliance with the License. You may obtain a copy of the License at:
*
*		http://www.apache.org/licenses/LICENSE-2.0
*
*	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*	for the specific language governing permissions and limitations under the License.
*
*	Child Color Selection
*
*	Copyright 2020 SmartThings
*
*/
metadata {
	definition(name: "Child Color Control", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings") {
		capability "Color Control"
		capability "Actuator"
	}

	tiles(scale: 2){
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
		}

		main(["switch"])
		details(["switch"])
	}
}

def setColor(value) {
	parent.childSetColor(value)
}
