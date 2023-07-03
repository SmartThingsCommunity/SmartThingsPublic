/**
 *  Zw Multichannel
 *
 *  Copyright 2016 SmartThings
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
	definition (name: "Zw Multichannel", namespace: "capabilities", author: "SmartThings") {
		capability "Zw Multichannel"
	}

	simulator {
		status "ZWEvent":""
        status "ZWInfo":""
	}

	tiles {
		valueTile("zwEvent", "device.epEvent", label:"${name}", width:2, height:2) {}
        valueTile("zwInfo", "device.epInfo", label:"${name}", width:2, height:2) {}
        main("zwEvent")
        details(["zwEvent","zwInfo"])        
	}
}

// parse events into attributes
def parse(String description) {
	def pair = description.split(":")
    createEvent(name: pair[0].trim(), value: pair[1].trim())
}

// handle commands
def enableEpEvents(data) {
	'[enableEpEvents]${data}'
}

def epCmd(num, str) {
	'[epCmd]${num}:${str}'
}