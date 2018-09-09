/**
 *  Copyright 2018 DarwinsDen
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
 *  Sisumulated Energy Meter+
 *
 *  Author: DarwinsDen
 *
 *  Date: 2018-08-10
 */

metadata {
	definition (name: "Simulated Energy Meter+", namespace: "darwinsden", author: "Darwin") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		command "reset"	
        command "setPower"
        command "setEnergy"
    }
    
	simulator {
	}

	// tile definitions
    tiles(scale: 2) {
		multiAttributeTile(name:"power", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute("device.power", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'Power: ${currentValue} W')
			}
			tileAttribute("device.energy", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'Energy: ${currentValue} Kilowatt Hours',icon: "st.secondary.activity")
			}
		}
     
		// This tile is not displayed on the tile screen for this device but rather in the Things list.
		valueTile("list-power", "device.power") {
			state "default", label:'${currentValue} W', icon: "st.secondary.activity", canChangeIcon: true
		}

		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset', action:"reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "list-power"
		details(["power","energy","reset","refresh"])
	}

	preferences {
	}

}

def updated(){
}

def installed() {
 reset()
}


def parse(String description){
}

def refresh() {
     sendEvent(name: 'power', value: state.power, unit: "W")
     sendEvent(name: 'energy', value: state.energy, unit: "kWh")
}

def reset() {
  state.energy = 0
  state.power = 0
  state.lastSentPower=state.power
  state.lastSentEnergy=state.energy
  sendEvent(name: 'power', value: state.power, unit: "W")
  sendEvent(name: 'energy', value: state.energy, unit: "kWh")
}

def sendData() {  
  
  if ((!state.lastSentPower || Math.abs(state.lastSentPower - state.power) > 10 ) ||
      (!state.lastPowerSendTime || (now()-state.lastPowerSendTime)/1000 > 120))
  {
     sendEvent(name: 'power', value: state.power, unit: "W")
     state.lastSentPower=state.power
     state.lastPowerSendTime = now()
  }
  if ((!state.lastSentEnergy || Math.abs(state.lastSentEnergy - state.energy) > 0.0001) ||
      (!state.lastEnergySendTime || (now()-state.lastPowerSendTime)/1000 > 120))
  {
     sendEvent(name: 'energy', value: state.energy, unit: "kWh")
     state.lastSentEnergy=state.energy
     state.lastEnergySendTime = now()
  }
}

def setPower (value)
{
  state.power = value
  sendData()
}

def setEnergy (value)
{
  state.energy = value
  sendData()
}