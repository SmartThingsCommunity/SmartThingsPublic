/*
 *  DSC Zone Device - Open/Close contact sensor
 *
 *  Author: Joe Jarvis
 *  Date: 2015-12-28
 *
 */

// for the UI
metadata {
  definition (name: "j64 Alarm", namespace: "j64", author: "joejarvis64@gmail.com") {
    capability "Alarm"
    capability "Refresh"
    capability "Polling"
	}

  simulator {
  }

  tiles {
    // Main Row
    standardTile("alarm", "device.alarm", width: 2, height: 2, canChangeBackground: true, canChangeIcon: true) {
      state "off",    label: '${name}', action: siren,   icon: "st.contact.contact.open",   backgroundColor: "#ffa81e"
      state "siren",  label: '${name}', action: off, icon: "st.office.office6", backgroundColor: "#79b821"
      state "strobe", label: '${name}', action: off, icon: "st.office.office6",   backgroundColor: "#79b821"
      state "both",   label: '${name}', action: off, icon: "st.office.office6", backgroundColor: "#79b821"
    }
    
    standardTile("refresh", "device.contact", inactiveLabel: false, decoration: "flat") {
      state("default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh")
    }
    
    // This tile will be the tile that is displayed on the Hub page.
    main "alarm"

    // These tiles will be displayed when clicked on the device, in the order listed here.
    details(["alarm", "refresh"])
  }
}

def setAlarm(String alarmOn) {
  def ns = alarmOn?.toLowerCase()
  if (ns == "true") {
		sendEvent (name: "alarm", value: "siren")
  } else {
    	sendEvent (name: "alarm", value: "off")
  }
}

def off() {
	log.debug "Turn off alarm"
    parent.disarmPartition(device.deviceNetworkId.replaceAll("alarm",""))
}

def siren() {
	log.debug "Turn on alarm"
    parent.soundAlarm(device.deviceNetworkId.replaceAll("alarm",""))
}

def poll() {
}

def refresh() {
    // alarms are only on the 1st partition
	parent.refreshPartition("1")
}