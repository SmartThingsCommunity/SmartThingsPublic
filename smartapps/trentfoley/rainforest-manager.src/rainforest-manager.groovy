/**
 *  Copyright 2020 Trent Foley
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
 *  Rainforest Manager
 *
 *  Author: Trent Foley
 *  Date: 2020-04-19
 */

// Automatically generated. Make future change here.
definition(
	name: "Rainforest Manager",
	namespace: "trentfoley",
	author: "Trent Foley",
	description: "Monitor your whole-house energy use by connecting to your Rainforest Cloud account",
	iconUrl: "https://scontent-dfw5-2.xx.fbcdn.net/v/t1.0-1/p200x200/579041_411717088857172_818823298_n.jpg?_nc_cat=108&_nc_sid=dbb9e7&_nc_ohc=ZfmfAwCyt1gAX9MvooX&_nc_ht=scontent-dfw5-2.xx&_nc_tp=6&oh=a52d69f3098469ea44a148eb6ce1f905&oe=5EC10D3E",
	iconX2Url: "https://scontent-dfw5-2.xx.fbcdn.net/v/t1.0-1/p200x200/579041_411717088857172_818823298_n.jpg?_nc_cat=108&_nc_sid=dbb9e7&_nc_ohc=ZfmfAwCyt1gAX9MvooX&_nc_ht=scontent-dfw5-2.xx&_nc_tp=6&oh=a52d69f3098469ea44a148eb6ce1f905&oe=5EC10D3E",
    iconX3Url: "https://scontent-dfw5-2.xx.fbcdn.net/v/t1.0-1/p200x200/579041_411717088857172_818823298_n.jpg?_nc_cat=108&_nc_sid=dbb9e7&_nc_ohc=ZfmfAwCyt1gAX9MvooX&_nc_ht=scontent-dfw5-2.xx&_nc_tp=6&oh=a52d69f3098469ea44a148eb6ce1f905&oe=5EC10D3E"
)

preferences {
	section("Rainforest Auth") {
        input "cloudid", "text", title: "Cloud ID"
        input "email", "text", title: "Username"
        input "password", "password", title: "Password"
    }
}

def getChildNamespace() { "trentfoley" }
def getChildName() { "Rainforest Eagle" }
def getServerUrl() { "https://rainforestcloud.com:9445/cgi-bin/post_manager" }

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	def device = addDevice(cloudid)
	device.initialize()
}

private def addDevice(dni) {
    def device = getChildDevice(dni)
    if(!device) {
        device = addChildDevice(childNamespace, childName, dni, null, [ label: "${childName}" ])
        log.debug "Created ${device.displayName} with device network id: ${dni}"
    } else {
        log.debug "Found already existing ${device.displayName} with device network id: ${dni}"
    }
    return device
}

def refreshChild(child) {
    def data = [:]

    httpPost(
        uri: serverUrl,
        headers: [
            "Cloud-Id": child.device.deviceNetworkId,
            "User": email,
            "Password": password
        ],
        body: "<Command><Name>get_instantaneous_demand</Name><Format>JSON</Format></Command>"
    ) { resp ->
        log.debug resp.data
        def respJson = parseJson(resp.data.text())
        def instDemand = respJson.InstantaneousDemand
        int demand = convertHexToInt(instDemand.Demand)
        int multiplier = convertHexToInt(instDemand.Multiplier)
        int divisor = convertHexToInt(instDemand.Divisor)

        if (multiplier == 0) { multiplier = 1 }
        if (divisor  == 0)   { divisor = 1 }
        demand = demand * multiplier / divisor * 1000

        data = [ power: demand, unit: "W" ]
    }
    
    return data
}

def parseJson(String s) {
	new groovy.json.JsonSlurper().parseText(s)
}

private Integer convertHexToInt(hex) {
    return Integer.decode(hex)
}