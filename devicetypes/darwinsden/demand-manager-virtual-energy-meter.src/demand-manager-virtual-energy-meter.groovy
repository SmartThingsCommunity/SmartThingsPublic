/**
 *  Demand Manager Virtual Energy Meter
 *
 *  Author: Darwin@DarwinsDen.com
 *  Copyright 2018 - All rights reserved
 *  
 *  This software was developed in the hopes that it will be useful to others, however, 
 *  it is distributed on an "AS IS" BASIS, WITOUT WARRANTIES OR GUARANTEES OF ANY KIND, either express or implied. 
 * 
 *  The end user is free to modify this software for personal use. Re-distribution of this software in its original or 
 *  modified form requires explit written consent from the developer. 
 * 
 *  The developer retains all rights, title, copyright, and interest, including all copyright, patent rights, and trade secrets 
 *  associated with the algorthms, and technologies used herein. 
 * 
 */

metadata {
	definition (name: "Demand Manager Virtual Energy Meter", namespace: "darwinsden", author: "darwin@darwinsden.com") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		command "reset"	
        command "setPower"
        command "setEnergy"
        command "setDemandGoal"
    }
    
	simulator {
	}

	// tile definitions
    tiles(scale: 2) {
		multiAttributeTile(name:"power", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute("device.power", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'Power: ${currentValue} W',
                backgroundColors:[
                    [value: 0, color: "#44b621"],
					[value: 100, color: "#aaaaaa"]
                     ])

			}
			tileAttribute("timeSet", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue}',icon: "st.secondary.activity")
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
        
       valueTile("projStatus", "device.stat",  width: 2, height: 2) {
          state "1", label: 'Demand\nOK\n', backgroundColor:"#ffffff", action: ""
          state "2", label: 'Demand\nExceeded\n\n', backgroundColor:"#00a0dc", action: "" 
        }
        
		main "list-power"
		details(["power","energy","reset","refresh","projStatus"])
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
     sendEvent(name: 'timeSet', value: state.timeSet)
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
  state.timeSet = new Date(now()).format("EEE MMM yyyy '@' HH:mm z" , location.timeZone)
  sendData()
}

def setEnergy (value)
{
  state.energy = value
  sendData()
}

