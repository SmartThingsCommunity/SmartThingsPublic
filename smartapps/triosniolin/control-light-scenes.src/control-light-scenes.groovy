/**
 *  Switches Control Light Scenes
 *
 *  Copyright 2017 Will Cole
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
    name: "Control Light Scenes",
    namespace: "triosniolin",
    author: "Will Cole",
    description: "Using switches, control scenes for light(s).",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "page1")
	page(name: "page2")
	page(name: "page3")
	page(name: "page4")
    page(name: "page5")
    page(name: "page6")
}
def page1() {
	dynamicPage(name: "page1", title: "Select devices:", nextPage: "page2", uninstall:true) {
        section("Number of Scenes (such as on, off, movie, party)") {
            input "scenes", "number", 
                title: "Number of Scenes (1-5)", 
                range: "1..5",
                defaultValue: "3",
                required: true,
                submitOnChange: true
        }
        section("Device(s) to control") {
		input "colors", "capability.colorControl", 
                    multiple: true, 
                    title: "Slave Color bulb(s)...",
		    		required: false,
		    		hideWhenEmpty: true
                input "slaves", "capability.colorTemperature", 
                    multiple: true, 
                    title: "Slave Temp bulb(s)...", 
                    required: true
        }

    }
}

def page2() {
    def i = 1
	if (scenes > i) {
	dynamicPage(name: "page2", title: "Select Scene $i", nextPage: "page3") {
            section("Scene $i") {
                input "switch$i", "capability.switch", 
                    multiple: false, 
                    title: "Switch To Enable Scene $i", 
                    required: true
            }
            section("Scene $i Color Temp") {
                input "Temp$i", "number", 
                    title: "Scene $i Color Temp (Kelvin) (2700-6500)", 
                    range: "2700..6500",
                    defaultValue: "",
					required: false
            }
            section("Scene $i Level") {
                input "Dim$i", "number", 
                    title: "Scene $i Dim Level (0-100%)", 
                    range: "0..100",
                    defaultValue: "",
					required: false
            }
		if (colors) {
			section("Scene $i Hue") {
			input "Hue$i", "number", 
			    title: "Scene $i Hue (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
			section("Scene $i Saturation") {
			input "Sat$i", "number", 
			    title: "Scene $i Saturation (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
		}
        }
   } else {
    dynamicPage(name: "page2", title: "Select Scene $i", install:true) {
            section("Scene $i") {
                input "switch$i", "capability.switch", 
                    multiple: false, 
                    title: "Switch To Enable Scene $i", 
                    required: true
            }
            section("Scene $i Color Temp") {
                input "Temp$i", "number", 
                    title: "Scene $i Color Temp (Kelvin) (2700-6500)", 
                    range: "2700..6500",
                    defaultValue: "",
					required: false
            }
            section("Scene $i Level") {
                input "Dim$i", "number", 
                    title: "Scene $i Dim Level (0-100%)", 
                    range: "0..100",
                    defaultValue: "",
					required: false
            }
	    if (colors) {
			section("Scene $i Hue") {
			input "Hue$i", "number", 
			    title: "Scene $i Hue (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
			section("Scene $i Saturation") {
			input "Sat$i", "number", 
			    title: "Scene $i Saturation (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
		}
        }
    }
}
def page3() {
	def i = 2
if (scenes > i) {
	dynamicPage(name: "page3", title: "Select Scene $i", nextPage: "page4") {
            section("Scene $i") {
                input "switch$i", "capability.switch", 
                    multiple: false, 
                    title: "Switch To Enable Scene $i", 
                    required: true
            }
            section("Scene $i Color Temp") {
                input "Temp$i", "number", 
                    title: "Scene $i Color Temp (Kelvin) (2700-6500)", 
                    range: "2700..6500",
                    defaultValue: "",
					required: false
            }
            section("Scene $i Level") {
                input "Dim$i", "number", 
                    title: "Scene $i Dim Level (0-100%)", 
                    range: "0..100",
                    defaultValue: "",
					required: false
            }
		if (colors) {
			section("Scene $i Hue") {
			input "Hue$i", "number", 
			    title: "Scene $i Hue (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
			section("Scene $i Saturation") {
			input "Sat$i", "number", 
			    title: "Scene $i Saturation (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
		}
        }
   } else {
    dynamicPage(name: "page3", title: "Select Scene $i", install:true) {
            section("Scene $i") {
                input "switch$i", "capability.switch", 
                    multiple: false, 
                    title: "Switch To Enable Scene $i", 
                    required: true
            }
            section("Scene $i Color Temp") {
                input "Temp$i", "number", 
                    title: "Scene $i Color Temp (Kelvin) (2700-6500)", 
                    range: "2700..6500",
                    defaultValue: "",
					required: false
            }
            section("Scene $i Level") {
                input "Dim$i", "number", 
                    title: "Scene $i Dim Level (0-100%)", 
                    range: "0..100",
                    defaultValue: "",
					required: false
            }
	    if (colors) {
			section("Scene $i Hue") {
			input "Hue$i", "number", 
			    title: "Scene $i Hue (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
			section("Scene $i Saturation") {
			input "Sat$i", "number", 
			    title: "Scene $i Saturation (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
		}
        }
    }
}
def page4() {
	def i = 3
if (scenes > i) {
	dynamicPage(name: "page4", title: "Select Scene $i", nextPage: "page5") {
            section("Scene $i") {
                input "switch$i", "capability.switch", 
                    multiple: false, 
                    title: "Switch To Enable Scene $i", 
                    required: true
            }
            section("Scene $i Color Temp") {
                input "Temp$i", "number", 
                    title: "Scene $i Color Temp (Kelvin) (2700-6500)", 
                    range: "2700..6500",
                    defaultValue: "",
					required: false
            }
            section("Scene $i Level") {
                input "Dim$i", "number", 
                    title: "Scene $i Dim Level (0-100%)", 
                    range: "0..100",
                    defaultValue: "",
					required: false
            }
		if (colors) {
			section("Scene $i Hue") {
			input "Hue$i", "number", 
			    title: "Scene $i Hue (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
			section("Scene $i Saturation") {
			input "Sat$i", "number", 
			    title: "Scene $i Saturation (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
		}
        }
   } else {
        dynamicPage(name: "page4", title: "Select Scene $i", install:true) {
            section("Scene $i") {
                input "switch$i", "capability.switch", 
                    multiple: false, 
                    title: "Switch To Enable Scene $i", 
                    required: true
            }
            section("Scene $i Color Temp") {
                input "Temp$i", "number", 
                    title: "Scene $i Color Temp (Kelvin) (2700-6500)", 
                    range: "2700..6500",
                    defaultValue: "",
					required: false
            }
            section("Scene $i Level") {
                input "Dim$i", "number", 
                    title: "Scene $i Dim Level (0-100%)", 
                    range: "0..100",
                    defaultValue: "",
					required: false
            }
		if (colors) {
			section("Scene $i Hue") {
			input "Hue$i", "number", 
			    title: "Scene $i Hue (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
			section("Scene $i Saturation") {
			input "Sat$i", "number", 
			    title: "Scene $i Saturation (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
		}
        }
    }
}
def page5() {
	def i = 4
	if (scenes > i) {
        dynamicPage(name: "page5", title: "Select Scene $i", nextPage: "page6") {
            section("Scene $i") {
                input "switch$i", "capability.switch", 
                    multiple: false, 
                    title: "Switch To Enable Scene $i", 
                    required: true
            }
            section("Scene $i Color Temp") {
                input "Temp$i", "number", 
                    title: "Scene $i Color Temp (Kelvin) (2700-6500)", 
                    range: "2700..6500",
                    defaultValue: "",
					required: false
            }
            section("Scene $i Level") {
                input "Dim$i", "number", 
                    title: "Scene $i Dim Level (0-100%)", 
                    range: "0..100",
                    defaultValue: "",
					required: false
            }
		if (colors) {
			section("Scene $i Hue") {
			input "Hue$i", "number", 
			    title: "Scene $i Hue (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
			section("Scene $i Saturation") {
			input "Sat$i", "number", 
			    title: "Scene $i Saturation (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
		}
        }
       } else {
        dynamicPage(name: "page5", title: "Select Scene $i", install:true) {
                section("Scene $i") {
                    input "switch$i", "capability.switch", 
                        multiple: false, 
                        title: "Switch To Enable Scene $i", 
                        required: true
                }
                section("Scene $i Color Temp") {
                    input "Temp$i", "number", 
                        title: "Scene $i Color Temp (Kelvin) (2700-6500)", 
                        range: "2700..6500",
                        defaultValue: "",
					required: false
                }
                section("Scene $i Level") {
                    input "Dim$i", "number", 
                        title: "Scene $i Dim Level (0-100%)", 
                        range: "0..100",
                        defaultValue: "",
					required: false
                }
		if (colors) {
			section("Scene $i Hue") {
			input "Hue$i", "number", 
			    title: "Scene $i Hue (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
			section("Scene $i Saturation") {
			input "Sat$i", "number", 
			    title: "Scene $i Saturation (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
		}
            }
    }
}
def page6() {
	def i = 5
        dynamicPage(name: "page6", title: "Select Scene $i", install:true) {
            section("Scene $i") {
                input "switch$i", "capability.switch", 
                    multiple: false, 
                    title: "Switch To Enable Scene $i", 
                    required: true
            }
            section("Scene $i Color Temp") {
                input "Temp$i", "number", 
                    title: "Scene $i Color Temp (Kelvin) (2700-6500)", 
                    range: "2700..6500",
                    defaultValue: "",
					required: false
            }
            section("Scene $i Level") {
                input "Dim$i", "number", 
                    title: "Scene $i Dim Level (0-100%)", 
                    range: "0..100",
                    defaultValue: "",
					required: false
            }
		if (colors) {
			section("Scene $i Hue") {
			input "Hue$i", "number", 
			    title: "Scene $i Hue (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
			section("Scene $i Saturation") {
			input "Sat$i", "number", 
			    title: "Scene $i Saturation (0-100)", 
			    range: "0..100",
			    defaultValue: "",
					required: false
			}
		}
        
    }
}
def installed()
{
if (switch1) {
subscribe (switch1, "switch.on", switch1Handler)
}
if (switch2) {
subscribe (switch2, "switch.on", switch2Handler)
}
if (switch3) {
subscribe (switch3, "switch.on", switch3Handler)
}
if (switch4) {
subscribe (switch4, "switch.on", switch4Handler)
}
if (switch5) {
subscribe (switch5, "switch.on", switch5Handler)
}
}

def updated()
{
	unsubscribe()
if (switch1) {
subscribe (switch1, "switch.on", switch1Handler)
}
if (switch2) {
subscribe (switch2, "switch.on", switch2Handler)
}
if (switch3) {
subscribe (switch3, "switch.on", switch3Handler)
}
if (switch4) {
subscribe (switch4, "switch.on", switch4Handler)
}
if (switch5) {
subscribe (switch5, "switch.on", switch5Handler)
}
	log.info "subscribed to all of switches events"
}



def switch1Handler(evt){	

	log.info "switch1Handler Event: ${evt.value}"
	if (colors) {
		
		if (Temp1) { slaves?.setColorTemperature(Temp1)
			     colors?.setColorTemperature(Temp1) }
		if (Dim1) { slaves?.setLevel(Dim1) 
			    colors?.setLevel(Dim1) }
		if (Hue1) { colors?.setHue(Hue1) }
		if (Sat1) { colors?.setSaturation(Sat1) }
	} else {
		if (Temp1) { slaves?.setColorTemperature(Temp1) }
		if (Dim1) { slaves?.setLevel(Dim1) }
	}
}
def switch2Handler(evt){	
	log.info "switch2Handler Event: ${evt.value}"
	if (colors) {
		
		if (Temp2) { slaves?.setColorTemperature(Temp2)
			     colors?.setColorTemperature(Temp2) }
		if (Dim2) { slaves?.setLevel(Dim2) 
			    colors?.setLevel(Dim2) }
		if (Hue2) { colors?.setHue(Hue2) }
		if (Sat2) { colors?.setSaturation(Sat2) }
	} else {
		if (Temp2) { slaves?.setColorTemperature(Temp2) }
		if (Dim2) { slaves?.setLevel(Dim2) }
	}
}
def switch3Handler(evt){	
	log.info "switch3Handler Event: ${evt.value}"
	if (colors) {
		
		if (Temp3) { slaves?.setColorTemperature(Temp3)
			     colors?.setColorTemperature(Temp3) }
		if (Dim3) { slaves?.setLevel(Dim3) 
			    colors?.setLevel(Dim3) }
		if (Hue3) { colors?.setHue(Hue3) }
		if (Sat3) { colors?.setSaturation(Sat3) }
	} else {
		if (Temp3) { slaves?.setColorTemperature(Temp3) }
		if (Dim3) { slaves?.setLevel(Dim3) }
	}
}
def switch4Handler(evt){	
	log.info "switch4Handler Event: ${evt.value}"
	if (colors) {
		
		if (Temp4) { slaves?.setColorTemperature(Temp4)
			     colors?.setColorTemperature(Temp4) }
		if (Dim4) { slaves?.setLevel(Dim4) 
			    colors?.setLevel(Dim4) }
		if (Hue4) { colors?.setHue(Hue4) }
		if (Sat4) { colors?.setSaturation(Sat4) }
	} else {
		if (Temp4) { slaves?.setColorTemperature(Temp4) }
		if (Dim4) { slaves?.setLevel(Dim4) }
	}
}
def switch5Handler(evt){	
	log.info "switch5Handler Event: ${evt.value}"
	if (colors) {
		
		if (Temp5) { slaves?.setColorTemperature(Temp5)
			     colors?.setColorTemperature(Temp5) }
		if (Dim5) { slaves?.setLevel(Dim5) 
			    colors?.setLevel(Dim5) }
		if (Hue5) { colors?.setHue(Hue5) }
		if (Sat5) { colors?.setSaturation(Sat5) }
	} else {
		if (Temp5) { slaves?.setColorTemperature(Temp5) }
		if (Dim5) { slaves?.setLevel(Dim5) }
	}
}