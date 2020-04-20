/**
 *  FortrezZ MIMO2+ B-Side
 *
 *  Copyright 2016 FortrezZ, LLC
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
    name: "FortrezZ MIMO2+ B-Side",
    namespace: "fortrezz",
    author: "FortrezZ, LLC",
    description: "Breaks the MIMO2 into two separate devices to allow automation on SIG2 and Relay 2.",
    category: "Convenience",
    iconUrl: "http://swiftlet.technology/wp-content/uploads/2016/05/logo-square-200-1.png",
    iconX2Url: "http://swiftlet.technology/wp-content/uploads/2016/05/logo-square-500.png",
    iconX3Url: "http://swiftlet.technology/wp-content/uploads/2016/05/logo-square.png",
    singleInstance: true)


preferences {
	section("Title") {
		input(name: "devices", type: "capability.voltageMeasurement", title: "MIMO2 devices", description: null, required: true, submitOnChange: true, multiple: true)
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

def initialize(){
	log.debug("Devices: ${settings.devices}")
    settings.devices.each {//deviceId ->
    	subscribe(it, "powered", events)
    	subscribe(it, "switch2", events)
    	subscribe(it, "contact2", events)
    	subscribe(it, "voltage2", events)
    	subscribe(it, "relay2", events)
    	subscribe(it, "anaDig2", events)
        
        try {
            def existingDevice = getChildDevice(it.id)
            if(!existingDevice) {
	            log.debug("Device ID: ${existingDevice}")
                def childDevice = addChildDevice("fortrezz", "FortrezZ MIMO2+ B-Side", it.id, null, [name: "Device.${it.id}", label: "${it.name} B-Side", completedSetup: true])
            }
        } catch (e) {
            log.error "Error creating device: ${e}"
        }
    }
    
    getChildDevices().each {
    	def test = it
        def search = settings.devices.find { getChildDevice(it.id).id == test.id }
        if(!search) {
        	removeChildDevices(test)
        }
    }
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def on2(child) {
	log.debug("on2")
    def ret = child
    settings.devices.each {//deviceId ->
        def ch = getChildDevice(it.id)
        if(child == ch.id) {
        	ret = "${child}, ${it.id}"
        	it.on2()
        }
    }
    return ret
}

def off2(child) {
	log.debug("off2")
    def ret = child
    settings.devices.each {//deviceId ->
        def ch = getChildDevice(it.id)
        if(child == ch.id) {
        	ret = "${child}, ${it.id}"
        	it.off2()
        }
    }
    return ret
}

def refresh2(child) {
	log.debug("refresh2")
    def ret = child
    settings.devices.each {//deviceId ->
        def ch = getChildDevice(it.id)
        if(child == ch.id) {
        	ret = "${child}, ${it.id}"
        	it.refresh()
        }
    }
    return ret
}

def events(evt) {
	def ch = getChildDevice(evt.device.id)
    ch.eventParse(evt);
	log.debug("${evt.device.id} triggered ${evt.name}")
}
// TODO: implement event handlers