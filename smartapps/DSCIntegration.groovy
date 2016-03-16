/*
 *  DSC Alarm Panel integration via REST API callbacks
 *
 *  Author: Kent Holloway <drizit@gmail.com>
 *  Modified by: Matt Martz <matt.martz@gmail.com>
 *  Modified by: Jordan <jordan@xeron.cc>
 */

definition(
    name: "DSC Integration",
    namespace: "dsc",
    author: "Jordan <jordan@xeron.cc>",
    description: "DSC Integration App",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2760581/dscpanel_small.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2760581/dscpanel_large.png",
    oauth: true,
    singleInstance: true
)

import groovy.json.JsonBuilder

preferences {
  section("Alarmserver Setup:") {
    input("ip", "text", title: "IP", description: "The IP of your alarmserver (required)", required: false)
    input("port", "text", title: "Port", description: "The port (required)", required: false)
  }
  section("XBMC Notifications (optional):") {
  	// TODO: put inputs here
    input "xbmcserver", "text", title: "XBMC IP", description: "IP Address", required: false
    input "xbmcport", "number", title: "XBMC Port", description: "Port", required: false
  }
  section("Notifications (optional)") {
    input "sendPush", "enum", title: "Push Notifiation", required: false,
      metadata: [
       values: ["Yes","No"]
      ]
    input "phone1", "phone", title: "Phone Number", required: false
  }
  section("Notification events (optional):") {
    input "notifyEvents", "enum", title: "Which Events?", description: "Events to notify on", required: false, multiple: true,
      options: [
        'all', 'partition alarm', 'partition armed', 'partition away', 'partition disarm', 'partition entrydelay',
        'partition exitdelay', 'partition forceready', 'partition instantaway', 'partition instantstay',
        'partition notready', 'partition ready', 'partition restore', 'partition stay', 'partition trouble',
        'ledbacklighton', 'ledbacklightoff', 'ledfireon', 'ledfireoff', 'ledprogramon', 'ledprogramoff',
        'ledtroubleon', 'ledtroubleoff', 'ledbypasson', 'ledbypassoff', 'ledmemoryon', 'ledmemoryoff',
        'ledarmedon', 'ledarmedoff', 'ledreadyon', 'ledreadyoff', 'zone alarm', 'zone clear', 'zone closed',
        'zone fault', 'zone open', 'zone restore', 'zone smoke', 'zone tamper'
      ]
  }
}

mappings {
  path("/panel/:eventcode/:zoneorpart") { action: [GET: "updateZoneOrPartition"] }
  path("/installzones")                 { action: [POST: "installzones"] }
  path("/installpartitions")            { action: [POST: "installpartitions"] }

}

def installzones() {
  def children = getChildDevices()
  def zones = request.JSON

  def zoneMap = [
    'contact':'DSC Zone Contact',
    'motion':'DSC Zone Motion',
    'smoke':'DSC Zone Smoke',
    'co':'DSC Zone CO',
  ]

  log.debug "children are ${children}"
  for (zone in zones) {
    def id = zone.key
    def padId = String.format("%02d", id.toInteger())
    def type = zone.value.'type'
    def device = zoneMap."${type}"
    def name = zone.value.'name'
    def networkId = "dsczone${id}"
    def zoneDevice = children.find { item -> item.device.deviceNetworkId == networkId }

    if (zoneDevice == null) {
      log.debug "add new child: id: ${id} type: ${type} name: ${name} ${networkId} ${padId}"
      zoneDevice = addChildDevice("dsc", "${device}", networkId, null, [name: "DSC Zone ${padId}", label:"DSC Zone ${padId} ${name}", completedSetup: true])
    } else {
      log.debug "zone device was ${zoneDevice}"
      try {
        log.debug "trying name update for ${padId}"
        zoneDevice.name = "DSC Zone ${padId}"
        log.debug "trying label update for ${padId}"
        zoneDevice.label = "DSC Zone ${padId} ${name}"
      } catch(IllegalArgumentException e) {
        log.debug "excepted for ${padId}"
         if ("${e}".contains('identifier required')) {
           log.debug "Attempted update but device didn't exist. Creating ${networkId}"
           zoneDevice = addChildDevice("dsc", "${device}", networkId, null, [name: "DSC Zone ${padId}", label:"DSC Zone ${padId} ${name}", completedSetup: true])
         } else {
           log.error "${e}"
         }
      }
    }
  }

  for (child in children) {
    if (child.device.deviceNetworkId.contains("dsczone}")) {
      def zone = child.device.deviceNetworkId.minus('dsczone')
      def jsonZone = zones.find { x -> "${x.key}" == "${zone}"}
      if (jsonZone == null) {
        try {
          log.debug "Deleting device ${child.device.deviceNetworkId} ${child.device.name} as it was not in the config"
          deleteChildDevice(child.device.deviceNetworkId)
        } catch(MissingMethodException e) {
          if ("${e}".contains('types: (null) values: [null]')) {
            log.debug "Device ${child.device.deviceNetworkId} was empty, likely deleted already."
          } else {
             log.error e
          }
        }
      }
    }
  }
}

def installpartitions() {
  def children = getChildDevices()
  def partitions = request.JSON

  def partMap = [
    'stay':'DSC Stay Panel',
    'away':'DSC Away Panel',
  ]

  log.debug "children are ${children}"
  for (part in partitions) {
    def id = part.key
    def padId = String.format("%02d", id.toInteger())
    def name = part.value

    for (p in ['stay', 'away']) {
      def networkId = "dsc${p}${id}"
      def partDevice = children.find { item -> item.device.deviceNetworkId == networkId }
      def device = partMap."${p}"

      if (partDevice == null) {
        log.debug "add new child: id: ${padId} name: ${name} nId: ${networkId} device: ${device}"
        partDevice = addChildDevice("dsc", "${device}", networkId, null, [name: "${device} ${padId}", label:"${device} ${padId} ${name}", completedSetup: true])
      } else {
        log.debug "part device was ${partDevice}"
        try {
          log.debug "trying name update for ${device} ${padId}"
          partDevice.name = "${device} ${padId}"
          log.debug "trying label update for ${device} ${padId} ${name}"
          partDevice.label = "${device} ${padId} ${name}"
        } catch(IllegalArgumentException e) {
          log.debug "excepted for ${device} ${padId}"
           if ("${e}".contains('identifier required')) {
             log.debug "Attempted update but device didn't exist. Creating ${networkId}"
             partDevice = addChildDevice("dsc", "${device}", networkId, null, [name: "${device} ${padId}", label:"${device} ${padId} ${name}", completedSetup: true])
           } else {
             log.error "${e}"
           }
        }
      }
    }
  }

  for (child in children) {
    for (p in ['stay', 'away']) {
        if (child.device.deviceNetworkId.contains("dsc${p}")) {
        def part = child.device.deviceNetworkId.minus("dsc${p}")
        def jsonPart = partitions.find { x -> "${x.key}" == "${part}"}
        if (jsonPart== null) {
          try {
            log.debug "Deleting device ${child.device.deviceNetworkId} ${child.device.name} as it was not in the config"
            deleteChildDevice(child.device.deviceNetworkId)
          } catch(MissingMethodException e) {
            if ("${e}".contains('types: (null) values: [null]')) {
              log.debug "Device ${child.device.deviceNetworkId} was empty, likely deleted already."
            } else {
              log.error e
            }
          }
        }
      }
    }
  }
}

def sendUrl(url) {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/alarm/${url}",
        headers: [
            HOST: "${settings.ip}:${settings.port}"
        ]
    )
	sendHubCommand(result)

	log.debug "response" : "Request to send url: ${url} received"
    return result
}


def installed() {
  log.debug "Installed!"
}

def updated() {
  log.debug "Updated!"
}

void updateZoneOrPartition() {
  update()
}

private update() {
  def zoneorpartition = params.zoneorpart

  // Add more events here as needed
  // Each event maps to a command in your "DSC Panel" device type
  def eventMap = [
    '510':"partition led",
    '511':"partition ledflash",
    '601':"zone alarm",
    '602':"zone closed",
    '603':"zone tamper",
    '604':"zone restore",
    '605':"zone fault",
    '606':"zone restore",
    '609':"zone open",
    '610':"zone closed",
    '631':"zone smoke",
    '632':"zone clear",
    '650':"partition ready",
    '651':"partition notready",
    '652':"partition armed",
    '653':"partition forceready",
    '654':"partition alarm",
    '655':"partition disarm",
    '656':"partition exitdelay",
    '657':"partition entrydelay",
    '663':"partition chime",
    '664':"partition nochime",
    '701':"partition armed",
    '702':"partition armed",
    '840':"partition trouble",
    '841':"partition restore",
    '6520':"partition away",
    '6521':"partition stay",
    '6522':"partition instantaway",
    '6523':"partition instantstay"
  ]

  // get our passed in eventcode
  def eventCode = params.eventcode
  if (eventCode) {
    // Lookup our eventCode in our eventMap
    def opts = eventMap."${eventCode}"?.tokenize()
    // log.debug "Options after lookup: ${opts}"
    // log.debug "Zone or partition: $zoneorpartition"
    if (opts[0]) {
      if (['510', '511'].contains(eventCode)) {
        def flash = (opts[1] == 'ledflash') ? 'flash ' : ''

        def ledMap = [
          '0':'backlight',
          '1':'fire',
          '2':'program',
          '3':'trouble',
          '4':'bypass',
          '5':'memory',
          '6':'armed',
          '7':'ready'
        ]

        for (def i = 0; i < 8; i++) {
          def name = ledMap."${i}"
          def status = (zoneorpartition[i] == '1') ? 'on' : 'off'
          if (notifyEvents && (notifyEvents.contains('all') || notifyEvents.contains('led'+name+status))) {
            sendMessage("${opts[0]} 1 ${name} led in ${flash}${status} state")
          }
        }
        updatePartitions('1',"${opts[1]}${zoneorpartition}")
      } else {
        if (notifyEvents && (notifyEvents.contains('all') || notifyEvents.contains(eventMap[eventCode]))) {
          sendMessage("${opts[0]} ${zoneorpartition} in ${opts[1]} state")
        }

        // We have some stuff to send to the device now
        // this looks something like panel.zone("open", "1")
        // log.debug "Test: ${opts[0]} and: ${opts[1]} for $zoneorpartition"
        if ("${opts[0]}" == 'zone') {
           //log.debug "It was a zone...  ${opts[1]}"
           updateZoneDevices("$zoneorpartition","${opts[1]}")
        }
        if ("${opts[0]}" == 'partition') {
           //log.debug "It was a zone...  ${opts[1]}"
           updatePartitions("$zoneorpartition","${opts[1]}")
        }
      }
    }
  }
}

private updateZoneDevices(zonenum,zonestatus) {
  def children = getChildDevices()
  log.debug "zone: ${zonenum} is ${zonestatus}"
  // log.debug "zonedevices.id are $zonedevices.id"
  // log.debug "zonedevices.displayName are $zonedevices.displayName"
  // log.debug "zonedevices.deviceNetworkId are $zonedevices.deviceNetworkId"
  def zonedevice = children.find { item -> item.device.deviceNetworkId == "dsczone${zonenum}"}
  //def zonedevice = zonedevices.find { it.deviceNetworkId == "dsczone${zonenum}" }
  if (zonedevice) {
      log.debug "Was True... Zone Device: $zonedevice.displayName at $zonedevice.deviceNetworkId is ${zonestatus}"
      //Was True... Zone Device: Front Door Sensor at zone1 is closed
      zonedevice.zone("${zonestatus}")
      if ("${settings.xbmcserver}" != "") {  //Note: I haven't tested this if statement, but it looks like it would work.
        def lanaddress = "${settings.xbmcserver}:${settings.xbmcport}"
        def deviceNetworkId = "1234"
        def json = new JsonBuilder()
        def messagetitle = "$zonedevice.displayName".replaceAll(' ','%20')
        log.debug "$messagetitle"
        json.call("jsonrpc":"2.0","method":"GUI.ShowNotification","params":[title: "$messagetitle",message: "${zonestatus}"],"id":1)
        def xbmcmessage = "/jsonrpc?request="+json.toString()
        def result = new physicalgraph.device.HubAction("""GET $xbmcmessage HTTP/1.1\r\nHOST: $lanaddress\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}")
        sendHubCommand(result)
      }
    }
}

private updatePartitions(partitionnum, partitionstatus) {
  def children = getChildDevices()
  log.debug "partition: ${partitionnum} is ${partitionstatus}"
  def paneldevice = children.find { item -> item.device.deviceNetworkId == "dscpanel${partitionnum}"}
  if (paneldevice) {
    log.debug "Was True... Panel device: $paneldevice.displayName at $paneldevice.deviceNetworkId is ${partitionstatus}"
    //Was True... Zone Device: Front Door Sensor at zone1 is closed
    paneldevice.partition("${partitionstatus}", "${partitionnum}")
  }
  def awayswitch = children.find { item -> item.device.deviceNetworkId == "dscaway${partitionnum}"}
  if (awayswitch) {
    log.debug "Was True... Away Switch device: $awayswitch.displayName at $awayswitch.deviceNetworkId is ${partitionstatus}"
    //Was True... Zone Device: Front Door Sensor at zone1 is closed
    awayswitch.partition("${partitionstatus}", "${partitionnum}")
  }
  def stayswitch = children.find { item -> item.device.deviceNetworkId == "dscstay${partitionnum}"}
  if (stayswitch) {
    log.debug "Was True... Stay Switch device: $stayswitch.displayName at $stayswitch.deviceNetworkId is ${partitionstatus}"
    //Was True... Zone Device: Front Door Sensor at zone1 is closed
    stayswitch.partition("${partitionstatus}", "${partitionnum}")
  }
}

private sendMessage(msg) {
    def newMsg = "Alarm Notification: $msg"
    if (phone1) {
        sendSms(phone1, newMsg)
    }
    if (sendPush == "Yes") {
        sendPush(newMsg)
    }
}
