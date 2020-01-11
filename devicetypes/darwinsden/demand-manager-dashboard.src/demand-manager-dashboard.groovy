/**
 *  Demand Manager Dashboard
 *
 *  Author: Darwin@DarwinsDen.com
 *  Copyright 2018, 2020 - All rights reserved
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
 *	11-Jan-2020 >>> v0.1.2e.20200111 - Added attributes for current and projected demand
 *	10-Jan-2020 >>> v0.1.1e.20200110 - Updated for Hubitat compatibility
 * 
 */
metadata {
        definition (name: "Demand Manager Dashboard", namespace: "darwinsden", author: "darwin@darwinsden.com") {
        capability "Switch"
        capability "Refresh"  
        capability "Energy Meter"
		capability "Power Meter"
       
        command "setCurrentDemand"
        command "setProjectedDemand"
        command "setMode"
        command "setGoalDemand"
        command "setPeakDayDemand"
        command "setPeakMonthDemand"
        command "setMessage"
        command "setCycleMinutes"
        command "resetDay"
        command "resetMonth" 
  
        attribute "projectedStatus", "enum"
        attribute "mode", "string"
        attribute "status", "string"
        attribute "projectedStatus", "string"
        attribute "goal", "number"
        attribute "peakDayDemand", "number"
        attribute "peakMonthDemand", "number"
        attribute "peakMonthDemandStatus", "string"          
        attribute "currentDemand", "string"   
        attribute "projectedDemand", "string"       
        attribute "timeDaySet", "string"
        attribute "timeMonthSet", "string"
        attribute "message1", "string"
        attribute "message2", "string"
        attribute "message1Time", "string"
        attribute "message2Time", "string"
    }

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${currentValue}', action: "switch.off", icon: "https://raw.githubusercontent.com/DarwinsDen/SmartThingsPublic/3e63c07c/resources/icons/meterGray.png", backgroundColor: "#e86d13" 
				attributeState "off", label: '${currentValue}', action: "switch.on", icon: "https://raw.githubusercontent.com/DarwinsDen/SmartThingsPublic/3e63c07c/resources/icons/meterGray.png", backgroundColor: "#79b821"
			}
            tileAttribute("device.status", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}', unit:"")
            }
		}   

        valueTile("mode", "device.mode", width: 3, height: 1, decoration: "flat",inactiveLabel: false, canChangeIcon: true) {
			state "default", label: 'Manager Mode: ${currentValue}', backgroundColor: "#ffffff", action: "" 
		}
        
        valueTile("goal", "device.goal", width: 3, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
			state "default", label: 'Goal Demand:\n${currentValue}W', backgroundColor: "#ffffff", action: "" 
		}

       valueTile("currentDemand", "device.currentDemand", width: 4, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "" 
		}
        valueTile("projectedDemand", "device.projectedDemand", width: 4, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "" 
		}
        valueTile("peakDemandToday", "device.peakDayDemand", width: 3, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
			state "default", label: 'Today\u2019s Peak\nDemand: ${currentValue}W', backgroundColor: "#ffffff", action: "" 
		}
        valueTile("peakDemandMonth", "device.peakMonthDemandStatus", width: 3, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "" 
		}
        valueTile("timeMonthSet", "device.timeMonthSet", width: 2, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "" 
		}
        valueTile("timeDaySet", "device.timeDaySet", width: 2, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "" 
		}
       valueTile("message1", "device.message1", width: 4, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "" 
		}
        valueTile("message2", "device.message2", width: 4, height: 1, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "" 
		}
        valueTile("message1Time", "device.message1Time", width: 2, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "" 
		}
        valueTile("message2Time", "device.message2Time", width: 2, height: 1, decoration: "flat", inactiveLabel: false, canChangeIcon: true) {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff", action: "" 
		}
        standardTile("projStatus", "device.projectedStatus",   inactiveLabel: false, canChangeIcon: true, width: 2, height: 2) {
          state "Not Peak", label: 'Not Peak', icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png", backgroundColor: "#79b821"
          state "Demand OK", label: 'OK', icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png", backgroundColor: "#79b821"
          state "Demand Limit", label: 'At Limit', icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png",backgroundColor: "#b7a30b"
          state "Demand Exceeded", label: 'Exceeded', icon: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/wavex1-1.png", backgroundColor:  "#e86d13" 
        }
       

               valueTile("resetMonth", "device.energy", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label:'Reset', action:"resetMonth"
		}
               valueTile("resetDay", "device.energy", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label:'Reset', action:"resetDay"
		}
		main "switch"
		details(["switch","goal","mode","projectedDemand","currentDemand","projStatus","peakDemandToday","resetDay","timeDaySet","peakDemandMonth","resetMonth",
          "timeMonthSet","message1","message1Time","message2","message2Time"])
	}
}

def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on")
    sendEvent(name: "status", value: "In peak utility period")
    evaluateGoal()
}

def off() {
	sendEvent(name: "switch", value: "off")
    sendEvent(name: "status", value: "Not in peak utility period")
    evaluateGoal()
}

def setCurrentDemand (value) {
   //state.currentDemand = value
   sendEvent(name: "currentDemand", value: "Current ${state.cycleMinutes} minute\nDemand: ${value}W")
}

def setProjectedDemand (value) {
    state.projectedDemand = value
    sendEvent(name: "projectedDemand", value: "Projected ${state.cycleMinutes} minute\nDemand: ${value}W")
    evaluateGoal()
}

def setGoalDemand (value) {
    state.goalDemand = value
    sendEvent(name: "goal", value: value )
    evaluateGoal()
}

def setMode (value) {
    def mode
    switch (value) {
      case "fullControl":
        mode = "Full Control"
        break
      case "notifyOnly" :
        mode = "Notify Only"
        break
      case "monitorOnly" :
        mode = "Monitor Only"
        break
      default :
         mode = "Unknown"
         log.warn "Unexpected Demand Manager mode: ${value}"
         break
    }
    sendEvent(name: "mode", value: mode )
}

def setPeakDayDemand (value) {
    def peakDayDemand = value
    def timeDaySet = new Date(now()).format("dd MMM yyyy '@' HH:mm z" , location.timeZone)
    sendEvent(name: "peakDayDemand", value: peakDayDemand)
    sendEvent(name: "timeDaySet", value: timeDaySet)
}

def setPeakMonthDemand (value) {
    def mf = new java.text.SimpleDateFormat("MMMM")
    mf.setTimeZone(location.timeZone)
    def month = mf.format(new Date())
    def peakMonthDemand = "${month}\u2019s Peak\nDemand: ${value.toString()}W"
    def timeMonthSet = new Date(now()).format("dd MMM yyyy '@' HH:mm z" , location.timeZone)
    sendEvent(name: "peakMonthDemand", value: value)
    sendEvent(name: "peakMonthDemandStatus", value: peakMonthDemand)
    sendEvent(name: "timeMonthSet", value: timeMonthSet)
}

def setMessage (value) {

    def messageTime = new Date(now()).format("dd MMM yyyy '@' HH:mm:ss z" , location.timeZone)
    //state.message =  "${value} [${state.messageTime}]"
    def message1 = device.currentValue("message1")
    if (message1) {
       sendEvent(name: "message2", value: message1)
    }
    sendEvent(name: "message1", value: value)
   //  log.debug "oldmtime is ${device.currentValue("message1Time")}"
   def message1Time = device.currentValue("message1Time")
   if (message1Time) {
      sendEvent(name: "message2Time",value: message1Time) 
    }
    //log.debug "mtime is ${messageTime}"
    sendEvent(name: "message1Time", value: messageTime)   
}

def setCycleMinutes (value) {
    state.cycleMinutes = value
}

def reset() {
  setPeakMonthDemand (0)
  setPeakDayDemand (0)
}
def resetMonth() {
  setPeakMonthDemand (0)
}
def resetDay() {
  setPeakDayDemand (0)
}

def evaluateGoal() {
  def projectedStatus
  if (state.goalDemand) {
     if (device.currentValue("switch") == "off") {
        projectedStatus = "Not Peak"
     } else {
        if (state.projectedDemand < state.goalDemand * 0.8) {
           projectedStatus = "Demand OK" 
        } else if (state.projectedDemand < state.goalDemand * 1.1) {
           projectedStatus = "Demand Limit" 
        } else {
          projectedStatus = "Demand Exceeded"
        }
     }
   }
   sendEvent(name: "projectedStatus", value: projectedStatus)
}

//def sendData() {  
//     sendEvent(name: "peakMonthDemand", value: state.peakMonthDemand)
//}     
/*  if ((!state.lastSentPower || Math.abs(state.lastSentPower - state.power) > 10 ) ||
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
  */
  



