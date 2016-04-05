/**
 *  Energy Saver
 *
 *  Copyright 2014 SmartThings
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
    name: "에너지 세이버",
    namespace: "smartthings",
    author: "SmartThings",
	description: "에너지 소모가 많을 때, 기기의 전원을 차단시킴",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section {
		input(name: "meter", type: "capability.powerMeter", title: "다음의 전력계가 ...", required: true, multiple: false, description: null)
        input(name: "threshold", type: "number", title: "가리키는 에너지 소모량이 선택 값보다 클 때 ...", required: true, description: "in either watts or kw.")
	}
    section {
    	input(name: "switches", type: "capability.switch", title: "다음의 스위치를 끔", required: true, multiple: true, description: null)
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(meter, "power", meterHandler)
}

def meterHandler(evt) {
    def meterValue = evt.value as double
    def thresholdValue = threshold as int
    if (meterValue > thresholdValue) {
	    log.debug "${meter} reported energy consumption above ${threshold}. Turning of switches."
    	switches.off()
    }
}
