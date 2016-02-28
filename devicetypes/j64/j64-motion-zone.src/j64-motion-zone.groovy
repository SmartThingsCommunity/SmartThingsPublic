/*
 *  DSC Zone Device - Open/Close contact sensor
 *
 *  Author: Joe Jarvis
 *  Date: 2015-12-28
 *
 */

// for the UI
metadata {
  definition (name: "j64 Motion Zone", namespace: "j64", author: "joejarvis64@gmail.com") {
    capability "Motion Sensor"
    capability "Refresh"
    capability "Polling"

    attribute "motion", "string"

    command "toggleBypass"
	}

  simulator {
  }

  tiles {
    // Main Row
    standardTile("zone", "device.motion", width: 2, height: 2, canChangeBackground: true, canChangeIcon: true) {
      state "active",   label: '${name}', icon: "st.contact.contact.open",   backgroundColor: "#ffa81e"
      state "inactive", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
      state "alarm",  label: '${name}', icon: "st.contact.contact.open",   backgroundColor: "#ff0000"
      state "tamper", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#ff3399"
      state "fault",  label: '${name}', icon: "st.contact.contact.closed",   backgroundColor: "#cc0099"
    }
    
    standardTile("refresh", "device.contact", inactiveLabel: false, decoration: "flat") {
      state("default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh")
    }

    standardTile("toggleBypass", "device.contact", inactiveLabel: false, decoration: "flat") {
      state("refresh",  label:'Toggle Bypass',  action:"toggleBypass", icon:"st.Weather.weather1")
    }
    
    // This tile will be the tile that is displayed on the Hub page.
    main "zone"

    // These tiles will be displayed when clicked on the device, in the order listed here.
    details(["zone", "refresh", "toggleBypass"])
  }
}

def setState(String newState) {
  def ns = newState.toLowerCase()
  if (ns == "closed") {
  	sendEvent (name: "motion", value: "inactive")
  } else {
    	sendEvent (name: "motion", value: "active")
  }
}

def toggleBypass() {
	parent.bypassZone(device.deviceNetworkId.replaceAll("zone",""))
}

def poll() {
}

def refresh() {
	parent.refreshZone(device.deviceNetworkId.replaceAll("zone",""))
}