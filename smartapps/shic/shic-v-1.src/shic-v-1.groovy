/**
 *  SHICREST
 *
 *  Copyright 2017 Unitedthings Inc.
 *
 */
definition(
    name: "SHIC v.1",
    namespace: "SHIC",
    author: "Unitedthings Inc. (incorporated in Delaware)",
    description: "SHIC REST gate",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Allow SHIC to access these things") {
		 input "waterSensor", "capability.waterSensor", multiple: true
         input "smokeDetector", "capability.smokeDetector", multiple: true
	}
}

mappings {
  path("/waterSensor") {
    action: [
      GET: "listwaterSensor"
    ]
  }
  
}
mappings {
  path("/smokeDetector") {
    action: [
      GET: "listsmokeDetector"
    ]
  }
  
}
def listwaterSensor() {   def resp = []
    waterSensor.each {
      resp << [name: it.getDisplayName(),  lastactivity: it.getLastActivity(), status: it.getStatus(),  value: it.currentState("water"), bat: it.currentState("battery")  ]
    }
    return resp}
    
    def listsmokeDetector() {   def resp = []
    smokeDetector.each {
      resp << [name: it.getDisplayName(),  lastactivity: it.getLastActivity(), status: it.getStatus(), value: it.currentState("smoke"), bat: it.currentState("battery") ]
    }
    return resp}

// TODO: implement event handlers