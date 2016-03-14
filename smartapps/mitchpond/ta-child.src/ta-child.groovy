/**
 *  TA Child
 *
 *  Copyright 2016 Mitch Pond
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
    name: "TA Child",
    namespace: "mitchpond",
    author: "Mitch Pond",
    description: "Child app for Temperature Averager",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "namePage", nextPage: "devicePage")
    page(name: "devicePage")
}

def devicePage() {
    dynamicPage(name: "devicePage", title: "New Virtual Temp Sensor", install: true, uninstall: childCreated()) {
        section { 
            input "sensors", "capability.temperatureMeasurement", title: "Temperature Sensors", required: true, multiple: true
        }
    }
}

def namePage() {
    dynamicPage(name: "namePage", title: "New Virtual Temp Sensor", install: false, uninstall: childCreated()) {
        section {
            label title: "Device Label:", required: true
        }
    }

}

def installed() {
    spawnChildDevice(app.label, "Simulated Temperature Sensor")
	initialize()
}


def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}


def initialize() {
	subscribe(sensors, "temperature", getAverageTemp)
    getAverageTemp()
}

def spawnChildDevice(deviceLabel, deviceType) {
    app.updateLabel(deviceLabel)
    if (!childCreated()) {
        def child = addChildDevice("mitchpond", deviceType, getDeviceID(), null, [name: getDeviceID(), label: deviceLabel, completedSetup: true])
    }
}

def uninstalled() {
    deleteChildDevice(getChildDevice(getDeviceID()).deviceNetworkId)
}

private childCreated() {
    if (getChildDevice(getDeviceID())) {
        return true
    } else {
        return false
    }
}

def getAverageTemp(evt){
	def values = sensors.collect {it.currentTemperature}
    def averageTemp = values.sum() / values.size()
    log.debug "Values: ${values}  Average: ${averageTemp}"
    getChildDevice(getDeviceID()).setTemperature(averageTemp)
}

private getDeviceID() {
    return "TA_${app.id}"
}
