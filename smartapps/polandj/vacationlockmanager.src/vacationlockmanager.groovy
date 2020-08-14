/**
 *  VacationLockManager
 *
 *  Copyright 2018-2020 Jonathan Poland
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
    name: "VacationLockManager",
    namespace: "polandj",
    author: "Jonathan Poland",
    description: "Exposes a web API for calling from zapier to automatically add and remove user lock codes to a zwave/zigbee lock based on the users checkin and checkout dates.",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home3-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@3x.png")

preferences {
    page(name: "pageOne", title: "API and locks", nextPage: "pageTwo", uninstall: true) {
		section("Info") {
    		paragraph title: "API ID", app.getId()
    	}
		section("Lock") {
    		input "lock","capability.lockCodes", title: "Lock", multiple: false
        }
    }
    page(name: "pageTwo", title: "Notifications", nextPage: "pageThree") {
    	section() {
        	input "ownersms", "phone", title: "Owner SMS Number", required: false
            input "cleanersms", "phone", title: "Cleaners SMS Number", required: false
        }
        section("Twilio") {
        	input "twacct", "text", title: "Account", required: false
            input "twsid", "text", title: "SID", required: false
            input "twtok", "text", title: "Token", required: false
            input "twphone", "phone", title: "Phone", required: false
        }
    }
    page(name: "pageThree", title: "Options", nextPage: "selectRoutines") {
    	section("Check in/out") {
        	input "checkinhour", "number", title: "Check in time (hour of day)", defaultValue: 17, range: "0..23", required: true
        	input "checkouthour", "number", title: "Check out time (hour of day)", defaultValue: 11, range: "0..23", required: true
        }
        section("Code lifetime") {
        	input "hoursbefore", "number", title: "Add code this many hours before checkin", defaultValue: 23, range: "1..48", required: true
        	input "hoursafter", "number", title: "Delete code this many hours after checkout", defaultValue: 6, range: "1..48", required: true
        }
    }
    page(name: "selectRoutines", install: true)
}

def selectRoutines() {
    dynamicPage(name: "selectRoutines", title: "Select Routines to Execute") {
        // get the available actions
        def actions = location.helloHome?.getPhrases()*.label
        if (actions) {
        	// sort them alphabetically
            actions.sort()
            section("Rented Routine") {
                input "rentedroutine", "enum", title: "Rented routine", options: actions
            }
            section("Vacant Routine") {
                input "vacantroutine", "enum", title: "Vacant routine", options: actions
           	}
		}
    }
}

mappings {
  path("/reservation") {
    action: [
      POST: "addReservation"
    ]
  }
  path("/cancel") {
    action: [
      POST: "delReservation"
    ]
  }
  path("/who") {
    action: [
      GET: "listReservations"
    ]
  }
  path("/edit") {
  	action: [
      POST: "editReservation"
    ]
  }
  path("/test") {
  	action: [
      POST: "test"
    ]
  }
}

import groovy.json.JsonSlurper

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(lock, "lock", codeUsed)
    subscribe(location, "routineExecuted", routineRan)
    runEvery1Hour(checkCodes)
    log.debug "VacationLockManager Initialized with url https://graph-na04-useast2.api.smartthings.com/api/smartapps/installations/${app.getId()}/reservation"
}


/*
 * Called whenever the lock is locked or unlocked.
 * We use this to optionally monitor for unlocking by a specified user and then notifying.
 */
def codeUsed(evt) {
    if(evt.value == "unlocked" && evt.data) {
        def codeData = new JsonSlurper().parseText(evt.data)
        def username = findNameForSlot(codeData.usedCode)
        if(username && state[username] && !state[username].welcomed) {
        	def phone = state[username].phone
        	twilio_sms(phone, "Welcome to ${location.name}, ${fname(username)}! Please let me know if you need anything as you get settled in.")
            sendPush("$username has checked in")
            state[username].welcomed = true
        }
    }
}

/*
 * Tries to set the code and name on the lock.
 * We'll confirm the change in the periodic check.
 */
def addCode(data) {
	def name = data?.name
    def phone = data?.phone
	def code = phone[-7..-1]

	def slot = findSlotNamed(name)
    if (!slot) {
    	slot = findEmptySlot()
        lock.setCode(slot, code, name)
        state[name].slot = slot
		log.debug "Setting code $name = $code in slot $slot"
    }
    // Run rented routine if now occupied
    def sz = state.size()
    if (sz == 1) {
    	runIn(1, runRentedRoutine)
    }
}

/*
 * Tries to remove the code from the lock.
 */
def delCode(data) {
	def name = data?.name

	def slot = findSlotNamed(name)
    if (slot) {
    	lock.deleteCode(slot)
        log.debug "Deleting code for $name in slot $slot"
        // Run vacant routine if this is last one
        if (state.size() == 1) {
        	runIn(1, runVacantRoutine)
        }
    } else {
    	// We delete from state the second time around,
        // once we know it's really gone from the lock
        state.remove(name)
    }
}

/*
 * Sends a notification to cleaners and notes it in state
 */
 def notifyCleaners(data) {
	def name = data?.name
    def guests = data?.guests
    def checkout = data?.checkout

	twilio_sms(cleanersms, "Upcoming cleaning reminder for ${location.name}: ${guests} check out on ${checkout}")
    state[name].cleaners_notified = true
}

/* 
 * Get current lock codes from the lock as a map
 */
def getLockCodes() {
	def lockCodes = lock.currentValue("lockCodes")
    def codeData = new JsonSlurper().parseText(lockCodes)
    return codeData
}

/* Given a user, find the slot with that name.
 * Returns 0 on not found, since the slots are 1-indexed (1-30)
 */
def findSlotNamed(user) {
	def lockCodes = getLockCodes()
	def x = lockCodes.find{ it.value == user }?.key
    if (x) {
    	log.debug "User $user is in slot $x"
   	}
    return x as Integer
}

/*
 * Find the user associated with a given slot
 * Returns the name or null if no slot or name
 */
def findNameForSlot(slot) {
	def lockCodes = getLockCodes()
    def x = lockCodes.find{ it.key == slot as String}?.value
    if (x) {
    	log.debug "User $x is in slot $slot"
   	}
    return x
}

/*
 * Finds an empty slot
 * We use this when we're adding a new code to find where to put it.
 * We start at the max code ID (30) and work backwards.
 */
def findEmptySlot() {
	def lockCodes = getLockCodes()
    def maxCodes = lock.currentValue("maxCodes").toInteger()
    def emptySlot = null
    for (def i = maxCodes; i > 0; i--) {
    	if (!lockCodes.get("$i")) {
        	emptySlot = i
            break
        }
    }
    log.debug "Next empty slot is $emptySlot"
    return emptySlot
}

/*
 * Return the number of milliseconds in the given number of hours
 */
def millis(hours) {
	return (hours * 3600000)
}

/*
 * Convert string date to a date object, if possible.  Null if not.
 */
def extractDate(date){
    final List<String> dateFormats = ["MMM dd, yyyy", "MMM dd,yyyy"]
    if (date instanceof Date) {
        return date
    }
    for(String format: dateFormats) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format)
        sdf.setTimeZone(location.getTimeZone())
        try {             
            return sdf.parse(date)      
        } 
        catch (java.text.ParseException e) { }
        catch (java.lang.NullPointerException e) { }
    }
}

/*
 * Make sure phone numbers are complete
 * (will not work correctly for non-US numbers)
 */
def normalizePhone(phone) {
    try {
        return ("+1" + phone.replaceAll("[^\\d]", "")[-10..-1])
    } 
    catch (StringIndexOutOfBoundsException) { }
}

/*
 * Normalize how dates are output.
 */
def fmtDate(date) {
    try {
        return new java.text.SimpleDateFormat("MMMMM dd, yyyy").format(date)
    }
    catch (IllegalArgumentException) {
        return date
    }
}

/*
 * Print a first name from First Last
 */
def fname(name) {
    try {
        return name.trim().split(" ")[0]
    } catch (Exception e) {
        return name
    }
}

/*
 * Called every hour, checks that the state of the lock matches our desired state.
 * Sometimes set/delete operations need to be retried on the lock, this does that.
 */
def checkCodes() {
	log.debug "Periodic check of users and codes.."
    def ltf = new java.text.SimpleDateFormat ("yyyy-MM-dd@HH:mm")
    ltf.setTimeZone(location.getTimeZone())
	Date now = new Date();
    state.each { key, value ->
        def addOnDate = extractDate(value.checkin)
        addOnDate.setTime(addOnDate.getTime() + millis(checkinhour) - millis(hoursbefore))
        def warnOnDate = extractDate(value.checkin)
        warnOnDate.setTime(warnOnDate.getTime() + millis(checkinhour) - millis(3))
        def delOnDate =  extractDate(value.checkout)
        delOnDate.setTime(delOnDate.getTime() + millis(checkouthour) + millis(hoursafter))
        def cleanerNotifyDate = extractDate(value.checkout)
        cleanerNotifyDate.setTime(cleanerNotifyDate.getTime() + millis(checkouthour) - millis(48))
        if (now < addOnDate) {
        	log.debug "${key}: Early (Now: ${ltf.format(now)} < Add: ${ltf.format(addOnDate)})"
        } else if (now > addOnDate && now < delOnDate) {
        	log.debug "${key}: Active (Add: ${ltf.format(addOnDate)} < Now: ${ltf.format(now)} < Del: ${ltf.format(delOnDate)})"
            if (!findSlotNamed(value.name)) {
            	// Can't call directly because it manipulates state (which we're iterating)
            	runIn(10, addCode, [data: value])
            	// Notify if it's getting close to checkin and still not added
                if (now > warnOnDate) {
               		sendPush("${value.name} is checking in soon, but lock code hasn't been added yet!")
                }
            }
            // Remind cleaners a couple days before guests check out
            if (now > cleanerNotifyDate && !value.cleaners_notified) {
            	// Can't call directly because it manipulates state (which we're iterating)
            	runIn(20, notifyCleaners, [data: value])
            }
        } else {
        	log.debug "${key}: Expired (Del: ${ltf.format(delOnDate)} < Now: ${ltf.format(now)})"
            // Can't call directly because it manipulates state (which we're iterating)
            runIn(1, delCode, [data: value])
        }
    }
}

/*
 * The callback for our API endpoint to add reservations.  This is called (from zapier) to inform 
 * us of a new reservation.
 * Request MUST specify name, phone, checkin, checkout, guests params
 */
def addReservation() {
	def name = request.JSON?.name
    def phone = request.JSON?.phone
    def checkin = request.JSON?.checkin
    def checkout = request.JSON?.checkout
    def guests = request.JSON?.guests
    
    if (!name || !phone || !checkin || !checkout || !guests) {
    	httpError(400, "Must specify name, phone, checkin, checkout, AND guests parameters")
    }
    phone = normalizePhone(phone)
    if (!phone) {
    	httpError(400, "Invalid phone number")
    }
    checkin = extractDate(checkin)
    if (!checkin) {
    	httpError(400, "Invalid check-in date")
    }
    checkout = extractDate(checkout)
    if (!checkout) {
    	httpError(400, "Invalid check-out date")
    }
    state[name] = [name: name, phone: phone, guests: guests,
    			   checkin: fmtDate(checkin), checkout: fmtDate(checkout),
                   slot: 0, welcomed: false, cleaners_notified: false]

	log.info "Lock code scheduled for $name, $guests staying $checkin to $checkout"
    //twilio_sms(cleanersms, "Cleaning reminder for ${location.name} on ${fmtDate(checkout)}. There are ${guests} staying ${fmtDate(checkin)} to ${fmtDate(checkout)}")
    checkCodes()
}

def test() {
	
}

def delReservation() {
    def phone = request.JSON?.phone
    def retval = "No such number"
    
    if (!phone) {
    	httpError(400, "Must specify phone parameter")
    }
    phone = normalizePhone(phone)
    if (!phone) {
    	httpError(400, "Invalid phone number")
    }
    state.each { key, value ->
    	if (value.phone == phone) {
        	sendPush("${value.name} manually deleted")
            //twilio_sms(cleanersms, "Please cancel the cleaning scheduled for ${location.name} on ${fmtDate(value.checkout)}.  The guests cancelled.")
    		runIn(1, delCode, [data: value])
            retval = "Deleted ${value.name}"
        }
    }
    return [response: retval]
}

def listReservations() {
  return state
}

def editReservation() {
	def name = request.JSON?.name
    def phone = normalizePhone(request.JSON?.phone)
    def checkin = extractDate(request.JSON?.checkin)
    def checkout = extractDate(request.JSON?.checkout)
    def guests = request.JSON?.guests
    def retval = "No such name"
    
    if (!name) {
    	httpError(400, "Must specify a valid name parameter")
    }
    
    def keyval = state[name]
    if (keyval) {
    	retval = "Edited ${name}: "
    	if (phone && phone != keyval.phone) {
        	if (slot) {
            	httpError(400, "Cannot change phone because code already on lock")
            } else {
            	state[name].phone = phone
            	retval = retval + " phone (${keyval.phone} -> ${phone})"
            }
        }
        if (checkin && fmtDate(checkin) != keyval.checkin) {
        	state[name].checkin = fmtDate(checkin)
        	retval = retval + " checkin (${keyval.checkin} -> ${fmtDate(checkin)})"
        }
        if (checkout && fmtDate(checkout) != keyval.checkout) {
        	state[name].checkout = fmtDate(checkout)
        	retval = retval + " checkout (${keyval.checkout} -> ${fmtDate(checkout)})"
        }
        if (guests && guests != keyval.guests) {
        	state[name].guests = guests
        	retval = retval + " guests (${keyval.guests} -> ${guests})"
        }
    }
    return [response: retval]
}
/*
 * Actually sends the SMS, if a sms is configured
 */
def notify(sms, msg) {
    if (sms) {
        sendSms(sms, msg)
    }
}

/*
 * Send SMS using Twilio API
 */
def twilio_sms(sms, msg) {
	if (sms) {
    	String phone = normalizePhone(sms)
    	String charset = "UTF-8"
		String url = String.format("https://%s:%s@api.twilio.com/2010-04-01/Accounts/%s/Messages.json",
        						   URLEncoder.encode(twsid, charset),
                                   URLEncoder.encode(twtok, charset),
                                   URLEncoder.encode(twacct, charset))
    	String query = String.format("To=%s&Body=%s&From=%s", 
 	    							 URLEncoder.encode(phone, charset),
	     							 URLEncoder.encode(msg, charset),
                                     URLEncoder.encode(twphone, charset))
                         
		try {
			httpPost(url, query) { resp ->
        		log.debug "response data: ${resp.data}"
    		}
        } catch (e) {
        	sendPush("Problem sending twilio sms to $sms: $e")
		}
    }
}

def routineRan(evt) {
    sendPush("${location.name} ran routine ${evt.displayName}")
}

def runRentedRoutine() {
	if(rentedroutine) {
		location.helloHome?.execute(rentedroutine)
    }
}

def runVacantRoutine() {
	if (vacantroutine) {
	 	location.helloHome?.execute(vacantroutine)
    }
}