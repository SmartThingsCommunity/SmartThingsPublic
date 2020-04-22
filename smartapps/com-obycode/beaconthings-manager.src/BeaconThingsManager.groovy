/**
 *  BeaconThing Manager
 *
 *  Copyright 2015 obycode
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
    name: "BeaconThings Manager",
    namespace: "com.obycode",
    author: "obycode",
    description: "SmartApp to interact with the BeaconThings iOS app. Use this app to integrate iBeacons into your smart home.",
    category: "Convenience",
    iconUrl: "http://beaconthingsapp.com/images/Icon-60.png",
    iconX2Url: "http://beaconthingsapp.com/images/Icon-60@2x.png",
    iconX3Url: "http://beaconthingsapp.com/images/Icon-60@3x.png",
    oauth: true)


preferences {
	section("Allow BeaconThings to talk to your home") {

	}
}

def installed() {
  log.debug "Installed with settings: ${settings}"

  initialize()
}

def initialize() {
}

def updated() {
}

def uninstalled() {
  logout()
  removeChildDevices(getAllChildDevices())
}

def logout() {
  removeChildDevices(getAllChildDevices())
  revokeAccessToken()
}

mappings {
  path("/beacons") {
    action: [
    GET:    "getBeacons",
    DELETE: "clearBeacons",
    POST:   "addBeacon"
    ]
  }

  path("/beacons/:id") {
    action: [
    PUT:    "updateBeacon",
    DELETE: "deleteBeacon"
    ]
  }

  path("/logout") {
    action: [
    POST: "logout"
    ]
  }
}

def getBeacons() {
  def children = getAllChildDevices()
  def childList = ['places':[],'areas':[]]
  children.each {
    def parts = it.deviceNetworkId.split('-')
    def beacon = [:]
    if (parts.size() < 2) {
      log.debug "invalid dni: ${it.deviceNetworkId}"
      return
    }
    beacon['name'] = it.label
    beacon['major'] = parts[1]
    if (parts.size() == 3) {
      beacon['minor'] = parts[2]
      childList['areas'].add(beacon)
    }
    else {
      childList['places'].add(beacon)
    }
  }
  log.debug "childList is $childList"
  childList
}

void clearBeacons() {
  removeChildDevices(getAllChildDevices())
}

void addBeacon() {
  def beacon = request.JSON?.beacon
  if (beacon) {
    def beaconId = "BeaconThings"
    if (beacon.major) {
      beaconId = "$beaconId-${beacon.major}"
      if (beacon.minor) {
        beaconId = "$beaconId-${beacon.minor}"
      }
    }

    // If for some reason this beacon is already registered, delete it
    try {
      def existingDevice = getChildDevice(beaconId)
      if(!existingDevice) {
        deleteChildDevice(beaconId)
      }
    } catch (e) {
      log.debug "couldn't delete existing child"
    }

    log.debug "Adding beacon $beaconId"
    def d = addChildDevice("com.obycode", "BeaconThing", beaconId,  null, [label:beacon.name, name:"BeaconThing", completedSetup: true])

    if (beacon.presence) {
      d.setPresence(beacon.presence)
    }
  }
}

void updateBeacon() {
  log.debug "updating beacon ${params.id}"
  def children = getAllChildDevices()
  def beaconDevice = children.find{ d -> d.deviceNetworkId == "${params.id}" }
  if (!beaconDevice) {
    log.debug "Beacon not found: ${params.id}"
    return
  }

  // This could be just updating the presence
  def presence = request.JSON?.presence
  if (presence) {
    log.debug "Setting ${beaconDevice.label} to $presence"
    beaconDevice.setPresence(presence)
  }

  // or it could be updating the name
  def beacon = request.JSON?.beacon
  if (beacon) {
    beaconDevice.label = beacon.name
  }
}

void deleteBeacon() {
  log.debug "deleting beacon ${params.id}"
  // def children = getChildDevices()
  // def beaconDevice = children.find{ d -> d.deviceNetworkId == "${params.id}" }
  // deleteChildDevice(beaconDevice.deviceNetworkId)
  deleteChildDevice(params.id)
}

private removeChildDevices(delete) {
  delete.each {
    deleteChildDevice(it.deviceNetworkId)
  }
}
