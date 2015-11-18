/**
 *  ObyThing Music Connect
 *
 *  Copyright 2015 obycode
 *
 */
definition(
    name: "obything Connect",
    namespace: "com.obycode",
    author: "obycode",
    description: "Smart home, smart Mac. With obything.",
    category: "Fun & Social",
    iconUrl: "http://obything.obycode.com/icons/icon60.png",
    iconX2Url: "http://obything.obycode.com/icons/icon120.png",
    iconX3Url: "http://obything.obycode.com/icons/icon120.png",
    oauth: [displayName: "obything", displayLink: "http://obything.obycode.com"])


preferences {
	section("Smart home, smart Mac. With obything.") {
    app(name: "childApps", appName: "obything Notify with Sound", namespace: "com.obycode", title: "Notify with Sound", multiple: true)
    app(name: "childApps", appName: "obything Music Control", namespace: "com.obycode", title: "Music Control", multiple: true)
    app(name: "childApps", appName: "obything Trigger Playlist", namespace: "com.obycode", title: "Trigger Playlists", multiple: true)
    app(name: "childApps", appName: "obything Weather Forecast", namespace: "com.obycode", title: "Weather Forecast", multiple: true)
	}
}

mappings {
  path("/setup") {
    action: [
      POST: "setup",
    ]
  }
  path("/:uuid/:kind") {
    action: [
      POST: "createChild",
      PUT: "updateChild",
    ]
  }
}

def installed() {
	log.debug "Installed"
}

def updated() {
	log.debug "Updated"

	unsubscribe()
}

def uninstalled() {
  logout()
}

def logout() {
  revokeAccessToken()
}

// mapping handlers

// /setup POST
def setup() {
  def ip = request.JSON?.ip
  if (ip == null) {
    return httpError(400, "IP not specified")
  }
  def port = request.JSON?.port
  if (port == null) {
    return httpError(400, "Port not specified")
  }
  def name = request.JSON?.name
  if (name == null) {
    return httpError(400, "Name not specified")
  }
  def uuid = request.JSON?.uuid
  if (uuid == null) {
    return httpError(400, "UUID not specified")
  }

  // If machines is not initialized yet, set it up
  if (state.machines == null) {
    state.machines = [:]
  }

  // If this machine has already been initialized, just update it
  if (state.machines[uuid]) {
    state.machines[uuid]["ip"] = ip
    state.machines[uuid]["port"] = port
    state.machines[uuid]["name"] = name
    log.debug "Updated machine"

    def dead = []
    // Update the child devices
    def newHexIP = convertIPtoHex(ip)
    state.machines[uuid]["children"].keySet().each {
      def ids = state.machines[uuid]["children"][it]
      def child = getChildDevice(ids.dni)
      if (!child) {
        dead.add(it)
      }
      else {
        // Only change the IP; the label could've been manually changed and I'm
        // not sure how to handle the port changing (its not allowed now anyway).
        def oldHexPort = child.deviceNetworkId.split(':')[1]
        def newDNI = "$newHexIP:$oldHexPort"
        child.deviceNetworkId = newDNI
        state.machines[uuid]["children"][it]["dni"] = newDNI
      }
    }
    dead.each {
      state.machines[uuid]["children"].remove(it)
    }
  }
  // Otherwise, just create a new machine
  else {
    def machine = [ip:ip, port:port, name:name, children:[:]]
    state.machines[uuid] = machine
    log.debug "Added new machine"
  }

  sendCommand(state.machines[uuid], "/ping")
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

// /:uuid/:kind POST
def createChild() {
  // Constants for the port offsets
  final int iTunesService = 1
  final int pandoraService = 2
  final int spotifyService = 3

  def machine = state.machines[params.uuid]
  if (machine == null) {
    return httpError(404, "Machine not found")
  }

  def childName = machine["name"]
  def portNum = machine["port"].toInteger()
  switch (params.kind) {
    case "itunes":
      childName = childName + " iTunes"
      portNum += iTunesService
      break
    case "pandora":
      childName = childName + " Pandora"
      portNum += pandoraService
      break
    case "spotify":
      childName = childName + " Spotify"
      portNum += spotifyService
      break
    default:
      return httpError(400, "Unknown or unspecified device type")
  }

  def hexIP = convertIPtoHex(machine["ip"])
  def hexPort = convertPortToHex(portNum.toString())
  def childId = "$hexIP:$hexPort"

  // If this child already exists, re-associate with it
  def existing = getChildDevice(childId)
  if (existing) {
    log.debug "Found existing device: $existing"
    state.machines[params.uuid]["children"][params.kind] = [id:existing.id, dni:childId]
  }
  // otherwise, create it
  else {
    def d = addChildDevice("com.obycode", "obything Music Player", childId, location.hubs[0].id, [name:"obything Music Player", label:childName, completedSetup:true])
    log.debug "Created child device: $d"
    state.machines[params.uuid]["children"][params.kind] = [id:d.id, dni:d.deviceNetworkId]
  }

  return [dni:childId]
}

// /:uuid/:kind PUT
def updateChild() {
  def machine = state.machines[params.uuid]
  if (machine == null) {
    return httpError(404, "Machine not found")
  }

  def child = machine["children"][params.kind]
  if (child == null) {
    return httpError(404, "Device not found")
  }

  def device = getChildDevice(child.dni)
  if (device == null) {
    return httpError(404, "Device not found")
  }

  device.update(request.JSON)
}

private String convertIPtoHex(ipAddress) {
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

private void sendCommand(machine, path, command = null) {
  def fullPath = path
  if (command) {
    fullPath = fullPath + "?" + command
  }
  sendHubCommand(new physicalgraph.device.HubAction("GET " + fullPath + """ HTTP/1.1\r\nHOST: """ + machine["ip"] + ":" + machine["port"] + """\r\n\r\n""", physicalgraph.device.Protocol.LAN))
}
