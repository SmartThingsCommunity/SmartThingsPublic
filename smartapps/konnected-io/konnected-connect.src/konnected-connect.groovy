/**
 *  Konnected
 *
 *  Copyright 2017 konnected.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
public static String version() { return "2.2.6" }

definition(
  name:        "Konnected (Connect)",
  namespace:   "konnected-io",
  author:      "konnected.io",
  description: "Konnected devices bridge wired things with SmartThings",
  category:    "Safety & Security",
  iconUrl:     "https://raw.githubusercontent.com/konnected-io/docs/master/assets/images/KonnectedSecurity.png",
  iconX2Url:   "https://raw.githubusercontent.com/konnected-io/docs/master/assets/images/KonnectedSecurity@2x.png",
  iconX3Url:   "https://raw.githubusercontent.com/konnected-io/docs/master/assets/images/KonnectedSecurity@3x.png",
  singleInstance: true
)

preferences {
  page(name: "mainPage", title: "Konnected Devices", install: true, uninstall: true) {
    section {
      app(name: "childApps", appName: "Konnected Service Manager", namespace: "konnected-io", title: "Add a Konnected device", multiple: true)
      paragraph "Konnected (Connect) v${version()}"
    }
  }
}

def installed() {
  log.info "installed(): Installing Konnected Parent SmartApp"
  initialize()
}

def updated() {
  log.info "updated(): Updating Konnected SmartApp"
  unschedule()
  initialize()
}

def uninstalled() {
  log.info "uninstall(): Uninstalling Konnected SmartApp"
}

def initialize() {
  runEvery5Minutes(discoverySearch)
}

// Device Discovery : Send M-Search to multicast
def discoverySearch() {
  log.debug "Discovering Konnected devices on the network via SSDP"
  sendHubCommand(new physicalgraph.device.HubAction("lan discovery ${discoveryDeviceType()}", physicalgraph.device.Protocol.LAN))
}

def discoveryDeviceType() {
  return "urn:schemas-konnected-io:device:Security:1"
}

void registerKnownDevice(mac) {
  if (state.knownDevices == null) {
    state.knownDevices = [].toSet()
  }

  if (isNewDevice(mac)) {
    log.debug "Registering Konnected device ${mac}"
  	state.knownDevices.add(mac)
  }
}

void removeKnownDevice(mac) {
   state.knownDevices?.remove(mac)
}

Boolean isNewDevice(mac) {
  return !state.knownDevices?.contains(mac)
}