/**
 *  Copyright 2019 SmartThings
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
    definition (name: "Simulated Window Shade", namespace: "bflorian", author: "SmartThings", ocfDeviceType: "oic.d.blind") {
        capability "Window Shade"
        
        command "resume"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"windowShade", type: "lighting", width: 6, height: 4){
            tileAttribute ("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", label:'Open', action:"close", icon:"st.shades.shade-open", backgroundColor:"#00A0DC"
                attributeState "closed", label:'Closed', action:"open", icon:"st.shades.shade-closed", backgroundColor:"#ffffff"
                attributeState "partially open", label:'Partially Open', action:"resume", icon:"st.shades.shade-open", backgroundColor:"#00A0DC"
                attributeState "opening", label:'Opening', action:"pause", icon:"st.shades.shade-opening", backgroundColor:"#00A0DC"
                attributeState "closing", label:'Closing', action:"pause", icon:"st.shades.shade-closing", backgroundColor:"#ffffff"
            }
        }
    }
}

def installed() {
    sendEvent(name: "windowShade", value: "closed")
}

def updated() {
}

def open() {
    sendEvent(name: "windowShade", value: "opening")
    state.direction = "opening"
	runIn(10, opened)
}

def close() {
    sendEvent(name: "windowShade", value: "closing")
	state.direction = "closing"
	runIn(10, closed)
}

def pause() {
    sendEvent(name: "windowShade", value: "partially open")
	unschedule()
}

def resume() {
	if (state.direction == "closing") {
    	close()
    }
    else {
    	open()
    }
}

def opened() {
	sendEvent(name: "windowShade", value: "open")
}

def closed() {
	sendEvent(name: "windowShade", value: "closed")
}