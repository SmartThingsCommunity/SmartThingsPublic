/**
 *  Coffee After Shower
 *
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
    name: "Coffee After Shower",
    namespace: "hwustrack",
    author: "Hans Wustrack",
    description: "This app is designed simply to turn on your coffee machine while you are taking a shower.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("About") {
        paragraph "This app is designed simply to turn on your coffee machine " +
            "while you are taking a shower."
    }
	section("Bathroom humidity sensor") {
		input "bathroom", "capability.relativeHumidityMeasurement", title: "Which humidity sensor?"
	}
    section("Coffee maker to turn on") {
    	input "coffee", "capability.switch", title: "Which switch?"
    }
    section("Humidity level to switch coffee on at") {
    	input "relHum", "number", title: "Humidity level?", defaultValue: 50
    }
}

def installed() {
	subscribe(bathroom, "humidity", coffeeMaker)
}

def updated() {
	unsubscribe()
	subscribe(bathroom, "humidity", coffeeMaker)
}

def coffeeMaker(shower) {
	log.info "Humidity value: $shower.value"
	if (shower.value.toInteger() > relHum) {
		coffee.on()
    } 
}
