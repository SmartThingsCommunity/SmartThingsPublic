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

def uninstalled() {
  removeChildDevices(getChildDevices())
}

mappings {
  path("/beacons") {
    action: [
    DELETE: "clearBeacons",
    POST:   "addBeacon"
    ]
  }

  path("/beacons/:id") {
    action: [
    PUT: "updateBeacon",
    DELETE: "deleteBeacon"
    ]
  }
}

void clearBeacons() {
  removeChildDevices(getChildDevices())
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
    log.debug "adding beacon $beaconId"
    def d = addChildDevice("com.obycode", "BeaconThing", beaconId,  null, [label:beacon.name, name:"BeaconThing", completedSetup: true])
    log.debug "addChildDevice returned $d"

    if (beacon.present) {
      d.arrive(beacon.present)
    }
    else if (beacon.presence) {
      d.setPresence(beacon.presence)
    }
  }
}

void updateBeacon() {
  log.debug "updating beacon ${params.id}"
  def beaconDevice = getChildDevice(params.id)
  // def children = getChildDevices()
  // def beaconDevice = children.find{ d -> d.deviceNetworkId == "${params.id}" }
  if (!beaconDevice) {
    log.debug "Beacon not found directly"
    def children = getChildDevices()
    beaconDevice = children.find{ d -> d.deviceNetworkId == "${params.id}" }
    if (!beaconDevice) {
      log.debug "Beacon not found in list either"
      return
    }
  }

  // This could be just updating the presence
  def presence = request.JSON?.presence
  if (presence) {
    log.debug "Setting ${beaconDevice.label} to $presence"
    beaconDevice.setPresence(presence)
  }

  // It could be someone arriving
  def arrived = request.JSON?.arrived
  if (arrived) {
    log.debug "$arrived arrived at ${beaconDevice.label}"
    beaconDevice.arrived(arrived)
  }

  // It could be someone left
  def left = request.JSON?.left
  if (left) {
    log.debug "$left left ${beaconDevice.label}"
    beaconDevice.left(left)
  }

  // or it could be updating the name
  def beacon = request.JSON?.beacon
  if (beacon) {
    beaconDevice.label = beacon.name
  }
}

void deleteBeacon() {
  log.debug "deleting beacon ${params.id}"
  deleteChildDevice(params.id)
  // def children = getChildDevices()
  // def beaconDevice = children.find{ d -> d.deviceNetworkId == "${params.id}" }
  // if (beaconDevice) {
  //   deleteChildDevice(beaconDevice.deviceNetworkId)
  // }
}

private removeChildDevices(delete) {
  delete.each {
    deleteChildDevice(it.deviceNetworkId)
  }
}
