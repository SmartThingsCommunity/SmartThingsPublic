import grails.converters.JSON

/**
*  JSON API Access App
*/

definition(
  name: "Bling_SmartApps",
  namespace: "BlingSwitch",
  author: "Bling",
  description: "Return things as JSON",
  category: "My Apps",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
  oauth: [displayName: "JSON API", displayLink: ""]
)


preferences {
  section("Allow these things to be exposed via JSON...") {
    input "switches", "capability.switch", title: "Switches", multiple: true, required: true, hideWhenEmpty: true
  }
}

mappings {
  path("/things") {
    action: [
      GET: "listThings"
    ]
  }
}

def listThings() {
  [
    switches: switches.collect{device(it, "switch")}
  ]
}

private device(it, type) {
  def device_state = [label: it.label, type: type, id: it.id, devtest: it]

  for (attribute in it.supportedAttributes) {
    device_state."${attribute}" = it.currentValue("${attribute}")
  }

  device_state ? device_state : null
}

def installed() {}

def updated() {}