/**
 *  Cuchen Wifi Cooker PA-10
 *
 *  Copyright 2015 Kim DoHyeong
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
metadata {
	definition (name: "Cuchen Wifi Cooker PA-10", namespace: "cuchen.wifi.cooker.pa-10", author: "Kim DoHyeong") {
    capability "polling"
    
    command "refreshStatus"
    command "cookRice"
    command "cookRiceRapid"
    command "cookMixed"
    command "cookMixedRapid"
    command "warm"
    command "reheat"
    command "autoClean"
    command "cancel"

    attribute "status", "enum", ["stand by", "warm", "cooking", "carry over cooking", "reserved", "auto cleaning", "reheat", "slow cooking", "steam", "error"]
    attribute "model", "string"
    attribute "refreshing", "enum", ["doing", "done"]
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		// TODO: define your main and details tiles here
        
    	multiAttributeTile(name: "statusTile", type: "thermostat", width: 6, height: 4) {
      		tileAttribute("device.status", key: "PRIMARY_CONTROL") {
        		attributeState "stand by", label: "대기중", action: "refreshStatus", backgroundColor: "#EB6D86", icon: "http://lihom.jc-square.com/st_stand_by.png"
                attributeState "cooking", label: "취사중", action: "refreshStatus", backgroundColor: "#E73C4D", icon: "http://lihom.jc-square.com/st_cooking.png"
        		attributeState "warm", label: "보온중", action: "refreshStatus", backgroundColor: "#A54090", icon: "http://lihom.jc-square.com/st_warm.png"
                attributeState "carry over cooking", label: "뜸들이는중", action: "refreshStatus", backgroundColor: "#89C35D", icon: "http://lihom.jc-square.com/st_carry_over_cooking.png"
                attributeState "reserved", label: "예약중", action: "refreshStatus", backgroundColor: "#00A9B4", icon: "http://lihom.jc-square.com/st_reserved.png"
                attributeState "auto cleaning", label: "세척중", action: "refreshStatus", backgroundColor: "#3076BB", icon: "http://lihom.jc-square.com/st_auto_cleaning.png"
                attributeState "reheat", label: "재가열중", action: "refreshStatus", backgroundColor: "#EC6D3B", icon: "http://lihom.jc-square.com/st_reheat.png"
                attributeState "slow cooking", label: "요리중", action: "refreshStatus", backgroundColor: "#C79F62", icon: "http://lihom.jc-square.com/st_slow_cooking.png"
                attributeState "steam", label: "찜요리중", action: "refreshStatus", backgroundColor: "#804F21", icon: "http://lihom.jc-square.com/st_steam.png"
                attributeState "error1", label: "뚜껑 손잡이 열림", action: "refreshStatus", backgroundColor: "#E2002E", icon: "http://lihom.jc-square.com/st_error.png"
                attributeState "error2", label: "내솥 없음", action: "refreshStatus", backgroundColor: "#E2002E", icon: "http://lihom.jc-square.com/st_error.png"
                attributeState "error3", label: "취사중", action: "refreshStatus", backgroundColor: "#E2002E", icon: "http://lihom.jc-square.com/st_error.png"
                attributeState "error4", label: "보온중", action: "refreshStatus", backgroundColor: "#E2002E", icon: "http://lihom.jc-square.com/st_error.png"
                attributeState "error5", label: "예약중", action: "refreshStatus", backgroundColor: "#E2002E", icon: "http://lihom.jc-square.com/st_error.png"
                attributeState "error6", label: "자동취사중", action: "refreshStatus", backgroundColor: "#E2002E", icon: "http://lihom.jc-square.com/st_error.png"
                attributeState "error7", label: "재가열", action: "refreshStatus", backgroundColor: "#E2002E", icon: "http://lihom.jc-square.com/st_error.png"
                attributeState "error8", label: "통신장애", action: "refreshStatus", backgroundColor: "#E2002E", icon: "http://lihom.jc-square.com/st_error.png"
                attributeState "error", label: "통신장애", action: "refreshStatus", backgroundColor: "#E2002E", icon: "http://lihom.jc-square.com/st_error.png"
      		}
            /*
            tileAttribute("device.status", key: "SECONDARY_CONTROL") {
        		attributeState "stand by", label: "대기중", backgroundColor: "#EB6D86"
                attributeState "cooking", label: "취사중", backgroundColor: "#E73C4D"
        		attributeState "warm", label: "보온중", backgroundColor: "#A54090"
                attributeState "carry over cooking", label: "뜸 들이는 중", backgroundColor: "#89C35D"
                attributeState "reserved", label: "예약중", backgroundColor: "#00A9B4"
                attributeState "auto cleaning", label: "자동세척중", backgroundColor: "#3076BB"
                attributeState "reheat", label: "재가열중", backgroundColor: "#EC6D3B"
                attributeState "slow cooking", label: "죽 요리중", backgroundColor: "#C79F62"
                attributeState "steam", label: "찜 요리중", backgroundColor: "#804F21"
      		}
            */
    	}
        standardTile("cookRiceTile", "device.status", width: 2, height: 2, canChangeIcon: false, decoration: "flat") {
			state "stand by", label: "백미찰진밥", action: "cookRice", icon: "http://lihom.jc-square.com/cmd_rice.png"
            state "default", label: "백미찰진밥", action: "", icon: "http://lihom.jc-square.com/cmd_rice_disable.png"
		}
        standardTile("cookMixedTile", "device.status", width: 2, height: 2, canChangeIcon: false, decoration: "flat") {
       		state "stand by", label: '잡곡', action: "cookMixed", icon: "http://lihom.jc-square.com/cmd_mixed.png"
            state "default", label: '잡곡', action: "", icon: "http://lihom.jc-square.com/cmd_mixed_disable.png"
		}
        
        standardTile("refreshTile", "device.refreshing", width: 2, height: 2, canChangeIcon: false, decoration: "flat") {
            state "default", label: "상태갱신", action: "refreshStatus", icon: "http://lihom.jc-square.com/cmd_refresh.png"
            state "doing", label: "상태갱신", action: "", icon: "http://lihom.jc-square.com/cmd_refresh_disable.png"
		}
        
        standardTile("cookRiceRapidTile", "device.status", width: 2, height: 2, canChangeIcon: false, decoration: "flat") {
			state "stand by", label: "백미쾌속", action: "cookRiceRapid", icon: "http://lihom.jc-square.com/cmd_rice_rapid.png"
            state "default", label: "백미쾌속", action: "", icon: "http://lihom.jc-square.com/cmd_rice_rapid_disable.png"
		}
        standardTile("cookMixedRapidTile", "device.status", width: 2, height: 2, canChangeIcon: false, decoration: "flat") {
			state "stand by", label: '잡곡쾌속', action: "cookMixedRapid", icon: "http://lihom.jc-square.com/cmd_mixed_rapid.png"
            state "default", label: '잡곡쾌속', action: "", icon: "http://lihom.jc-square.com/cmd_mixed_rapid_disable.png"
		}
        
        standardTile("cancelTile", "device.status", width: 2, height: 2, canChangeIcon: false, decoration: "flat") {
			state "default", label: '취소', action: "cancel", icon: "http://lihom.jc-square.com/cmd_cancel.png"
		}
        
        main(["statusTile"])
        details(["statusTile", "cookRiceTile", "cookMixedTile", "refreshTile", "cookRiceRapidTile", "cookMixedRapidTile", "cancelTile"])
        //details(["valueTile"])
	}
}

//def translations = [
//  rice : [english: "Rice", korean: "백미"]
//]

// parse events into attributes
def parse(String description) {
  log.debug "Parsing '${description}'"
  // TODO: handle 'model' attribute
  // TODO: handle 'status' attribute
  // TODO: handle 'statusTime' attribute
  // TODO: handle 'statusMin' attribute
  // TODO: handle 'lang' attribute
  // TODO: handle 'vol' attribute
  // TODO: handle 'autoCleanDay' attribute
  // TODO: handle 'autoCleanMonth' attribute
  // TODO: handle 'autoCleanYear' attribute
  // TODO: handle 'isRsvAm' attribute
  // TODO: handle 'isError' attribute
  // TODO: handle 'errorNo' attribute
  // TODO: handle 'isPowerOff' attribute
  // TODO: handle 'isBs130' attribute
  // TODO: handle 'menu' attribute
  // TODO: handle 'level' attribute
}

// handle commands
def poll() {
  log.debug "Executing 'poll'"
  parent.poll(this)
}

def refreshStatus() {
  log.debug "Executing 'refreshStatus'"
  if (device.currentValue("refreshing") == "done") {
      sendEvent(name: "refreshing", value: "doing") 
      parent.refreshStatus(this, true)
  }
}

def cookRice() {
  log.debug "Executing 'cookRice'"
  //sendEvent(name: "status", value: "cooking")
  log.debug "status = " + device.currentValue("status")
  parent.cooking(this, [menu: 0, level: 0, hour: 0, min: 0])
}

def cookRiceRapid() {
  log.debug "Executing 'cookRiceRapid'"
  //sendEvent(name: "status", value: "cooking")
  log.debug "status = " + device.currentValue("status")
  parent.cooking(this, [menu: 1, level: 0, hour: 0, min: 0])
}

def cookMixed() {
  log.debug "Executing 'cookMixed'"
  //sendEvent(name: "status", value: "cooking")
  log.debug "status = " + device.currentValue("status")
  
  parent.cooking(this, [menu: 2, level: 0, hour: 0, min: 0])
}

def cookMixedRapid() {
  log.debug "Executing 'cookMixedRapid'"
  //sendEvent(name: "status", value: "cooking")
  log.debug "status = " + device.currentValue("status")
  
  parent.cooking(this, [menu: 3, level: 0, hour: 0, min: 0])
}

def warm() {
  log.debug "Executing 'warm'"
  parent.warm(this)
}

def reheat() {
  log.debug "Executing 'reheat'"
  parent.reheat(this)
}

def autoClean() {
  log.debug "Executing 'autoClean'"
  parent.autoClean(this)
}

def cancel() {
  log.debug "Executing 'cancel'"
  // TODO: handle 'cancel' command
  //sendEvent(name: "status", value: "stand by")
  log.debug "status = " + device.currentValue("status")
  
  parent.cancel(this)
}

def toStatus(statusNo) {
  switch (statusNo) {
    case 2 :
      return "warm"
    case 3 :
      return "cooking"
    case 4 :
      return "carry over cooking"
    case 5 :
      return "reserved"
    case 6 :
      return "auto cleaning"
    case 7 :
      return "reheat"
    case 8 :
      return "slow cooking"
    case 9 :
      return "steam"
    default : // 0 or 1
      return "stand by"
  }
}

def getResultMessage(resultCd) {
  switch (resultCd) {
    case 0 :
      //return "Send command success!"
      return "명령 전달 완료."
    case 1 : 
      return "뚜껑 손잡이 열림"
    case 2 :
      return "내솥 없음"
    case 3 :
      return "취사중"
    case 4 :
      return "보온중"
    case 5 :
      return "예약중"
    case 6 :
      return "자동취사중"
    case 7 :
      return "재가열"
    default : // over 8
      return "서버 연결 실패"
  }
}

def toLanguage(langNo) {
  switch(langNo) {
    case 3 :
      return "chinese"
    default :
      return "korean"
  }
}

def updateStatus(stateList) {
//  log.debug "refreshStatus - " + stateList
  sendEvent(name: "refreshing", value: "done")
  for (elem in stateList) {
  	switch (elem.apiName) {
      case "model" :
        sendEvent(name: "model", value: elem.value, displayed : false)
        //log.debug "model - " + device.currentValue("model")
        break
      case "status" :
        sendEvent(name: "status", value: toStatus(elem.value), displayed : false)
        //log.debug "status - " + device.currentValue("status")
        break
      case "stTime" :
        sendEvent(name: "statusTime", value: elem.value, displayed : false)
        //log.debug "statusTime - " + device.currentValue("statusTime")
        break
      case "stMinute" :
        sendEvent(name: "statusMin", value: elem.value, displayed : false)
        //log.debug "statusMin - " + device.currentValue("statusMin")
        break
      case "lang" :
        sendEvent(name: "lang", value: toLanguage(elem.value), displayed : false)
        //log.debug "lang - " + device.currentValue("lang")
        break
      case "vol" :
      case "autoCleanDay" :
      case "autoCleanMonth" :
      case "autoCleanYear" :
      case "isRsvAm" :
      case "isError" :
      case "errorNo" :
      case "isPowerOff" :
      case "isBs" :
      case "menu" :
      case "level" :
	      break
    }
  }
}