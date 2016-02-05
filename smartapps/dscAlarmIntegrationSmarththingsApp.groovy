/*
 *  DSC Alarm Panel integration via REST API callbacks
 *
 *  Author: Kent Holloway <drizit@gmail.com>
 *  Modified by: Matt Martz <matt.martz@gmail.com>
 *  Modified by: Jordan <jordan@xeron.cc>
 */

definition(
    name: "DSC Integration",
    namespace: "",
    author: "Jordan <jordan@xeron.cc>",
    description: "DSC Integration App",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2760581/dscpanel_small.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2760581/dscpanel_large.png",
    oauth: true
)

import groovy.json.JsonBuilder

preferences {

  section("Alarm Panel:") {
    input "paneldevices", "capability.switch", title: "Alarm Panel (required)", multiple: true, required: false
  }
  section("Zone Devices:") {
    input "zonedevices", "capability.sensor", title: "DSC Zone Devices (required)", multiple: true, required: false
  }
  section("XBMC Notifications (optional):") {
  	// TODO: put inputs here
    input "xbmcserver", "text", title: "XBMC IP", description: "IP Address", required: false
    input "xbmcport", "number", title: "XBMC Port", description: "Port", required: false
  }
  section("Notifications (optional) - NOT WORKING:") {
    input "sendPush", "enum", title: "Push Notifiation", required: false,
      metadata: [
       values: ["Yes","No"]
      ]
    input "phone1", "phone", title: "Phone Number", required: false
  }
  section("Notification events (optional):") {
    input "notifyEvents", "enum", title: "Which Events?", description: "default (none)", required: false, multiple: false,
     options:
      ['all','alarm','closed','open','closed','partitionready',
       'partitionnotready','partitionarmed','partitionalarm',
       'partitionexitdelay','partitionentrydelay', 'partitionaway',
       'partitionstay', 'partitioninstantaway', 'partitioninstantstay'
      ]
  }
}

mappings {
  path("/panel/:eventcode/:zoneorpart") {
    action: [
      GET: "updateZoneOrPartition"
    ]
  }
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
      '601':"zone alarm",
      '602':"zone closed",
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
      '701':"partition armed",
      '702':"partition armed",
      '6520':"partition away",
      '6521':"partition stay",
      '6522':"partition instantaway",
      '6523':"partition instantstay"
    ]

    // get our passed in eventcode
    def eventCode = params.eventcode
    if (eventCode)
    {
      // Lookup our eventCode in our eventMap
      def opts = eventMap."${eventCode}"?.tokenize()
      // log.debug "Options after lookup: ${opts}"
      // log.debug "Zone or partition: $zoneorpartition"
      if (opts[0])
      {
        // We have some stuff to send to the device now
        // this looks something like panel.zone("open", "1")
        // log.debug "Test: ${opts[0]} and: ${opts[1]} for $zoneorpartition"
        if ("${opts[0]}" == 'zone') {
           //log.debug "It was a zone...  ${opts[1]}"
           updateZoneDevices(zonedevices,"$zoneorpartition","${opts[1]}")
        }
        if ("${opts[0]}" == 'partition') {
           //log.debug "It was a zone...  ${opts[1]}"
           updatePartitions(paneldevices, "$zoneorpartition","${opts[1]}")
        }
      }
    }
}

private updateZoneDevices(zonedevices,zonenum,zonestatus) {
  log.debug "zonedevices: $zonedevices - ${zonenum} is ${zonestatus}"
  // log.debug "zonedevices.id are $zonedevices.id"
  // log.debug "zonedevices.displayName are $zonedevices.displayName"
  // log.debug "zonedevices.deviceNetworkId are $zonedevices.deviceNetworkId"
  def zonedevice = zonedevices.find { it.deviceNetworkId == "dsczone${zonenum}" }
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

private updatePartitions(paneldevices, partitionnum, partitionstatus) {
  log.debug "paneldevices: $paneldevices - ${partitionnum} is ${partitionstatus}"
  def paneldevice = paneldevices.find { it.deviceNetworkId == "dscpanel${partitionnum}" }
  if (paneldevice) {
    log.debug "Was True... Panel device: $paneldevice.displayName at $paneldevice.deviceNetworkId is ${partitionstatus}"
    //Was True... Zone Device: Front Door Sensor at zone1 is closed
    paneldevice.partition("${partitionstatus}", "${partitionnum}")
  }
  def awayswitch = paneldevices.find { it.deviceNetworkId == "dscaway${partitionnum}" }
  if (awayswitch) {
    log.debug "Was True... Away Switch device: $awayswitch.displayName at $awayswitch.deviceNetworkId is ${partitionstatus}"
    //Was True... Zone Device: Front Door Sensor at zone1 is closed
    awayswitch.partition("${partitionstatus}", "${partitionnum}")
  }
  def stayswitch = paneldevices.find { it.deviceNetworkId == "dscstay${partitionnum}" }
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
