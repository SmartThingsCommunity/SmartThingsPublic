/**
 *  Device Tile Controller
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
definition(
    name: "Device Tile Controller",
    namespace: "smartthings/tile-ux",
    author: "SmartThings",
    description: "A controller SmartApp to install virtual devices into your location in order to simulate various native Device Tiles.",
    category: "SmartThings Internal",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    singleInstance: true)


preferences {
    // landing page
    page(name: "defaultPage")
}

def defaultPage() {
    dynamicPage(name: "defaultPage", install: true, uninstall: true) {
        section {
            paragraph "Select on Unselect the devices that you want to install"
        }
        section(title: "Multi Attribute Tile Types") {
            input(type: "bool", name: "genericDeviceTile", title: "generic", description: "A device that showcases the various use of generic multi-attribute-tiles.", defaultValue: "false")
            input(type: "bool", name: "lightingDeviceTile", title: "lighting", description: "A device that showcases the various use of lighting multi-attribute-tiles.", defaultValue: "false")
            input(type: "bool", name: "thermostatDeviceTile", title: "thermostat", description: "A device that showcases the various use of thermostat multi-attribute-tiles.", defaultValue: "true")
            input(type: "bool", name: "mediaPlayerDeviceTile", title: "media player", description: "A device that showcases the various use of mediaPlayer multi-attribute-tiles.", defaultValue: "false")
            input(type: "bool", name: "videoPlayerDeviceTile", title: "video player", description: "A device that showcases the various use of videoPlayer multi-attribute-tiles.", defaultValue: "false")
        }
        section(title: "Device Tile Types") {
            input(type: "bool", name: "standardDeviceTile", title: "standard device tiles", description: "A device that showcases the various use of standard device tiles.", defaultValue: "false")
            input(type: "bool", name: "valueDeviceTile", title: "value device tiles", description: "A device that showcases the various use of value device tiles.", defaultValue: "false")
            input(type: "bool", name: "presenceDeviceTile", title: "presence device tiles", description: "A device that showcases the various use of color control device tile.", defaultValue: "false")
        }
        section(title: "Other Tile Types") {
            input(type: "bool", name: "carouselDeviceTile", title: "image carousel", description: "A device that showcases the various use of carousel device tile.", defaultValue: "false")
            input(type: "bool", name: "sliderDeviceTile", title: "slider", description: "A device that showcases the various use of slider device tile.", defaultValue: "false")
            input(type: "bool", name: "colorWheelDeviceTile", title: "color wheel", description: "A device that showcases the various use of color wheel device tile.", defaultValue: "false")
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
}

def uninstalled() {
    getChildDevices().each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initializeDevices()
}

def initializeDevices() {
    settings.each { key, value ->
        log.debug "$key : $value"
        def existingDevice = getChildDevices().find { it.name == key }
        log.debug "$existingDevice"
        if (existingDevice && !value) {
            deleteChildDevice(existingDevice.deviceNetworkId)
        } else if (!existingDevice && value) {
            String dni = UUID.randomUUID()
            log.debug "$dni"
            addChildDevice(app.namespace, key, dni, null, [
                label: labelMap()[key] ?: key,
                completedSetup: true
            ])
        }
    }
}

// Map the name of the Device to a proper Label
def labelMap() {
    [
        genericDeviceTile: "Tile Multiattribute Generic",
        lightingDeviceTile: "Tile Multiattribute Lighting",
        thermostatDeviceTile: "Tile Multiattribute Thermostat",
        mediaPlayerDeviceTile: "Tile Multiattribute Media Player",
        videoPlayerDeviceTile: "Tile Multiattribute Video Player",
        standardDeviceTile: "Tile Device Standard",
        valueDeviceTile: "Tile Device Value",
        presenceDeviceTile: "Tile Device Presence",
        carouselDeviceTile: "Tile Device Carousel",
        sliderDeviceTile: "Tile Device Slider",
        colorWheelDeviceTile: "Tile Device Color Wheel"
    ]
}
