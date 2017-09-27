/*
 *  DSC Alarm Panel integration via REST API callbacks
 *
 *  Author: Kent Holloway <drizit@gmail.com>
 *  Modified by: Matt Martz <matt.martz@gmail.com>
 *  Modified by: Jordan <jordan@xeron.cc>
 */

definition(
    name: 'DSC Integration',
    namespace: 'dsc',
    author: 'Jordan <jordan@xeron.cc>',
    description: 'DSC Integration App',
    category: 'My Apps',
    iconUrl: 'https://dl.dropboxusercontent.com/u/2760581/dscpanel_small.png',
    iconX2Url: 'https://dl.dropboxusercontent.com/u/2760581/dscpanel_large.png',
    oauth: true,
    singleInstance: true
)

import groovy.json.JsonBuilder

preferences {
	page(name: "MainSetup")
}

def MainSetup() {
    if (!state.accessToken) {
        createAccessToken()
    }
	dynamicPage(name: "MainSetup", title: "DSC Setup", install:true, uninstall:true) {
	  section('Alarmserver Setup:') {
		input('ip', 'text', title: 'IP', description: 'The IP of your alarmserver (required)', required: false)
		input('port', 'text', title: 'Port', description: 'The port (required)', required: false)
		input 'shmSync', 'enum', title: 'Smart Home Monitor Sync', required: false,
		  metadata: [
		   values: ['Yes','No']
		  ]
		input 'shmBypass', 'enum', title: 'SHM Stay/Away Bypass', required: false,
		  metadata: [
		   values: ['Yes','No']
		  ]
	  }
	  section('XBMC Notifications (optional):') {
		// TODO: put inputs here
		input 'xbmcserver', 'text', title: 'XBMC IP', description: 'IP Address', required: false
		input 'xbmcport', 'number', title: 'XBMC Port', description: 'Port', required: false
	  }
	  section('Notifications (optional)') {
		input 'sendPush', 'enum', title: 'Push Notifiation', required: false,
		  metadata: [
		   values: ['Yes','No']
		  ]
		input 'phone1', 'phone', title: 'Phone Number', required: false
	  }
	  section('Notification events (optional):') {
		input 'notifyEvents', 'enum', title: 'Which Events?', description: 'Events to notify on', required: false, multiple: true,
		  options: [
			'all', 'partition alarm', 'partition armed', 'partition away', 'partition chime', 'partition disarm',
			'partition duress', 'partition entrydelay', 'partition exitdelay', 'partition forceready',
			'partition instantaway', 'partition instantstay', 'partition nochime', 'partition notready', 'partition ready',
			'partition restore', 'partition stay', 'partition trouble', 'partition keyfirealarm', 'partition keyfirerestore',
			'partition keyauxalarm', 'partition keyauxrestore', 'partition keypanicalarm', 'partition keypanicrestore',
			'led backlight on', 'led backlight off', 'led fire on', 'led fire off', 'led program on', 'led program off',
			'led trouble on', 'led trouble off', 'led bypass on', 'led bypass off', 'led memory on', 'led memory off',
			'led armed on', 'led armed off', 'led ready on', 'led ready off', 'zone alarm', 'zone clear', 'zone closed',
			'zone fault', 'zone open', 'zone restore', 'zone smoke', 'zone tamper'
		  ]
	  }
	  section() {
		paragraph "View this SmartApp's API and Token Configuration to use it in the Alarmserver config."
		href url:"${apiServerUrl("/api/smartapps/installations/${app.id}/config?access_token=${state.accessToken}")}", style:"embedded", required:false, title:"Show Smartapp Token Info", description:"Tap, select, copy, then click \"Done\""
      }
    }
}
mappings {
  path('/update')            { action: [POST: 'update'] }
  path('/installzones')      { action: [POST: 'installzones'] }
  path('/installpartitions') { action: [POST: 'installpartitions'] }
if (!params.access_token || (params.access_token && params.access_token != state.accessToken)) {
  path("/config")                         { action: [GET: "authError"] }
	
} else {
  path("/config")                         { action: [GET: "renderConfig"]  }
}
}

def initialize() {
  if (settings.shmSync == 'Yes') {
    subscribe(location, 'alarmSystemStatus', shmHandler)
  }
  if(!state.accessToken) {
	createAccessToken()
  }
}

def shmHandler(evt) {
  if (settings.shmSync == 'Yes') {
    log.debug "shmHandler: shm changed state to: ${evt.value}"
    def children = getChildDevices()
    def child = children.find { item -> item.device.deviceNetworkId in ['dscstay1', 'dscaway1'] }
    if (child != null) {
      log.debug "shmHandler: using panel: ${child.device.deviceNetworkId} state: ${child.currentStatus}"
      //map DSC states to simplified values for comparison
      def dscMap = [
        'alarm': 'on',
        'away':'away',
        'entrydelay': 'on',
        'exitdelay': 'on',
        'forceready':'off',
        'instantaway':'away',
        'instantstay':'stay',
        'ready':'off',
        'stay':'stay',
      ]

      if (dscMap[child.currentStatus] && evt.value != dscMap[child.currentStatus]) {
        if (evt.value == 'off' && dscMap[child.currentStatus] in ['stay', 'away', 'on'] ) {
          sendUrl('disarm')
          log.debug "shmHandler: ${evt.value} is valid action for ${child.currentStatus}, disarm sent"
        } else if (evt.value == 'away' && dscMap[child.currentStatus] in ['stay', 'off'] ) {
          sendUrl('arm')
          log.debug "shmHandler: ${evt.value} is valid action for ${child.currentStatus}, arm sent"
        } else if (evt.value == 'stay' && dscMap[child.currentStatus] in ['away', 'off'] ) {
          sendUrl('stayarm')
          log.debug "shmHandler: ${evt.value} is valid action for ${child.currentStatus}, stayarm sent"
        }
      }
    }
  }
}

def installzones() {
  def children = getChildDevices()
  def zones = request.JSON

  def zoneMap = [
    'contact':'DSC Zone Contact',
    'motion':'DSC Zone Motion',
    'smoke':'DSC Zone Smoke',
    'co':'DSC Zone CO',
    'flood':'DSC Zone Flood',
  ]

  log.debug "children are ${children}"
  for (zone in zones) {
    def id = zone.key
    def type = zone.value.'type'
    def device = zoneMap."${type}"
    def name = zone.value.'name'
    def networkId = "dsczone${id}"
    def zoneDevice = children.find { item -> item.device.deviceNetworkId == networkId }

    if (zoneDevice == null) {
      log.debug "add new child: device: ${device} networkId: ${networkId} name: ${name}"
      zoneDevice = addChildDevice('dsc', "${device}", networkId, null, [name: "${name}", label:"${name}", completedSetup: true])
    } else {
      log.debug "zone device was ${zoneDevice}"
      try {
        log.debug "trying name update for ${networkId}"
        zoneDevice.name = "${name}"
        log.debug "trying label update for ${networkId}"
        zoneDevice.label = "${name}"
      } catch(IllegalArgumentException e) {
        log.debug "excepted for ${networkId}"
         if ("${e}".contains('identifier required')) {
           log.debug "Attempted update but device didn't exist. Creating ${networkId}"
           zoneDevice = addChildDevice("dsc", "${device}", networkId, null, [name: "${name}", label:"${name}", completedSetup: true])
         } else {
           log.error "${e}"
         }
      }
    }
  }

  for (child in children) {
    if (child.device.deviceNetworkId.contains('dsczone')) {
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
    'simplestay':'DSC Simple Stay Panel',
    'simpleaway':'DSC Simple Away Panel',
  ]

  log.debug "children are ${children}"
  for (part in partitions) {
    def id = part.key

    for (p in part.value) {
      def type = p.key
      def name = p.value
      def networkId = "dsc${type}${id}"
      def partDevice = children.find { item -> item.device.deviceNetworkId == networkId }
      def device = partMap."${type}"

      if (partDevice == null) {
        log.debug "add new child: device: ${device} networkId: ${networkId} name: ${name}"
        partDevice = addChildDevice('dsc', "${device}", networkId, null, [name: "${name}", label:"${name}", completedSetup: true])
      } else {
        log.debug "part device was ${partDevice}"
        try {
          log.debug "trying name update for ${networkId}"
          partDevice.name = "${name}"
          log.debug "trying label update for ${networkId}"
          partDevice.label = "${name}"
        } catch(IllegalArgumentException e) {
          log.debug "excepted for ${networkId}"
           if ("${e}".contains('identifier required')) {
             log.debug "Attempted update but device didn't exist. Creating ${networkId}"
             partDevice = addChildDevice('dsc', "${device}", networkId, null, [name: "${name}", label:"${name}", completedSetup: true])
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
        def jsonPart = partitions.find { x -> x.value."${p}" }
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

def autoBypass() {
  def closedList = ['clear','closed','dry','inactive']
  def deviceList = ['carbonMonoxide','contact','motion','smoke','water']
  def children = getChildDevices()
  def zones = children.findAll { it.device.deviceNetworkId.startsWith("dsczone") }
  def bypassList = []

  for (zone in zones) {
    for (device in deviceList) {
      if (zone.currentValue(device)) {
        if (!closedList.contains(zone.currentValue(device))) {
          def bypass = zone.deviceNetworkId.minus('dsczone')
          bypassList.add(bypass)
        }
      }
    }
  }
  if (bypassList) {
    sendUrl("bypass?zone=${bypassList.sort().unique().join(',')}")
  }
}

def sendUrl(url) {
    def result = new physicalgraph.device.HubAction(
        method: 'GET',
        path: "/api/alarm/${url}",
        headers: [
            HOST: "${settings.ip}:${settings.port}"
        ]
    )
	sendHubCommand(result)
	log.debug 'response' : "Request to send url: ${url} received"
    return result
}


def installed() {
  log.debug 'Installed!'
  initialize()
}

def updated() {
  unsubscribe()
  unschedule()
  initialize()
  log.debug 'Updated!'
}

def authError() {
    [error: "Permission denied"]
}

def renderConfig() {
    def configJson = new groovy.json.JsonOutput().toJson([
        description: "SmartApp Token/AppID",
        platforms: [
            [
                app_url: apiServerUrl("/api/smartapps/installations/"),
                app_id: app.id,
                access_token:  state.accessToken
            ]
        ],
    ])

    def configString = new groovy.json.JsonOutput().prettyPrint(configJson)
    render contentType: "text/plain", data: configString
}

private update() {
  def update = request.JSON

  if (update.'parameters' && "${update.'type'}" == 'partition') {
    for (p in update.'parameters') {
      if (notifyEvents && (notifyEvents.contains('all') || notifyEvents.contains("led ${p.key} ${p.value}".toString()))) {
        def flash = (update.'status'.startsWith('ledflash')) ? 'flashing ' : ''
        sendMessage("Keypad LED ${p.key.capitalize()}: ${flash}${p.value}")
      }
    }
  } else {
    if (notifyEvents && (notifyEvents.contains('all') || notifyEvents.contains("${update.'type'} ${update.'status'}".toString()))) {
      def messageMap = [
        'alarm':'ALARMING!',
        'armed':'armed',
        'away':'armed away',
        'bypass':'zone bypass refresh',
        'clear':'sensor cleared',
        'closed':'closed',
        'chime':'chime enabled',
        'disarm':'disarmed',
        'duress':'DURESS ALARM!',
        'entrydelay':'entry delay in progress',
        'exitdelay':'exit delay in progress',
        'fault': 'faulted!',
        'forceready':'forced ready',
        'instantaway':'armed instant away',
        'instantstay':'armed instant stay',
        'nochime':'chime disabled',
        'notready':'not ready',
        'open': 'opened',
        'ready':'ready',
        'restore':'restored',
        'smoke': 'SMOKE DETECTED!',
        'stay':'armed stay',
        'tamper': 'tampered!',
        'trouble':'in trouble state!',
        'keyfirealarm':'Fire key ALARMING!',
        'keyfirerestore':'Fire key restored',
        'keyauxalarm':'Auxiliary key ALARMING!',
        'keyauxrestore':'Auxiliary key restored',
        'keypanicalarm':'Panic key ALARMING!',
        'keypanicrestore':'Panic key restored',
      ]

      def messageBody = messageMap[update.'status']

      if (update.'name') {
        sendMessage("${update.'type'.capitalize()} ${update.'name'} ${messageBody}")
      } else {
        sendMessage(messageBody)
      }
    }
  }
  if ("${update.'type'}" == 'zone') {
    updateZoneDevices(update.'value', update.'status')
  } else if ("${update.'type'}" == 'partition') {
    if (settings.shmSync == 'Yes') {
      // Map DSC states to SHM modes, only using absolute states for now, no exit/entry delay
      def shmMap = [
        'away':'away',
        'forceready':'off',
        'instantaway':'away',
        'instantstay':'stay',
        'notready':'off',
        'ready':'off',
        'stay':'stay',
      ]

      if (shmMap[update.'status']) {
        if (settings.shmBypass != 'Yes' || !(location.currentState("alarmSystemStatus")?.value in ['away','stay'] && shmMap[update.'status'] in ['away','stay'])) {
          log.debug "sending smart home monitor: ${shmMap[update.'status']} for status: ${update.'status'}"
          sendLocationEvent(name: "alarmSystemStatus", value: shmMap[update.'status'])
        }
      }
    }
    updatePartitions(update.'value', update.'status', update.'parameters')
  } else if ("${update.'type'}" == 'bypass') {
    for (p in update.'parameters') {
      updateZoneDevices(p.key, p.value)
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
    log.debug "zone: device $zonedevice.displayName at $zonedevice.deviceNetworkId is ${zonestatus}"
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

private updatePartitions(partitionnum, partitionstatus, partitionparams) {
  def children = getChildDevices()
  log.debug "partition: ${partitionnum} is ${partitionstatus}"

  def panelList = ['stay', 'away', 'simplestay', 'simpleaway']
  
  for (paneltype in panelList) {
    def panel = children.find { item -> item.device.deviceNetworkId == "dsc${paneltype}${partitionnum}"}
    if (panel) {
      log.debug "partition: ${paneltype.capitalize()} Panel device: $panel.displayName at $panel.deviceNetworkId is ${partitionstatus}"
      panel.partition("${partitionstatus}", "${partitionnum}", partitionparams)
    }
  }
}

private sendMessage(msg) {
  def newMsg = "Alarm Notification: $msg"
  if (phone1) {
    sendSms(phone1, newMsg)
  }
  if (sendPush == 'Yes') {
    sendPush(newMsg)
  }
}
