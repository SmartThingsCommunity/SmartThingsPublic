/**
 *  Notify When Left Open and take photos
 *  Based on original code by olson.lukas@gmail.com (2013-06-24)
 *  8/14/2014 - Brian Lowrance - brian@rayzurbock.com - Photo code added
 *  11/8/2014 - Brian Lowrance - brian@rayzurbock.com - Prevent false alarms added
 *  11/8/2014 - Brian Lowrance - brian@rayzurbock.com - Option to repeat alert if door remains open
 *  11/9/2014 - Brian Lowrance - brian@rayzurbock.com - Modified repeat alert with a max of 10 per occurrance.
 *  12/3/2014 - Brian Lowrance - brian@rayzurbock.com - Added Dynamic Menus (Status Page, Configure Page). Added Hello Home action option on first alert.
 *  12/9/2014 - Brian Lowrance - brian@rayzurbock.com - Added SpeechSynthesis to speak alert if desired (Sonos or VLC Thing).
 *
 * For the latest version visit: https://github.com/rayzurbock/SmartThings-DoorLeftOpen
 * Version: 1.3.3
 */

definition(
    name: "Door left open (with Photo Burst, Hello Home, & Speech)",
    namespace: "rayzurbock",
    author: "Brian Lowrance",
    category: "Safety & Security",
    description: "Notify when something is left open, optionally: take photos, run hello home action",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
    
preferences {
	page(name: "pageStatus")
    page(name: "pageConfigure")
    page(name: "pageHHAction")
}

def pageStatus() {
    dynamicPage(name: "pageStatus", install: false, uninstall: false){
        def statusmsg = ""
        def appinfo = ""
        appinfo = "Door Left Open (with Photo Burst & Hello Home)\n"
        if (!(state.appversion == null)){appinfo += "Version: ${state.appversion}\n"}
        appinfo += "http://github.com/rayzurbock"
        section ("Status"){
            if (settings.contactSensor){
                href "pageConfigure", title:"Configure", description:"Tap to open"
                statusmsg = "Contact Sensor:\n${settings.contactSensor} (Currently ${settings.contactSensor.latestValue('contact')})\n"
                statusmsg += "Left Open Threshold: ${settings.numMinutes} minute(s)\n\n"
                statusmsg += "Alert Message:\n  '${settings.messageText}'\n"
                if (settings.speechSynth) {
                    statusmsg += "Announce with these speech synthesis devices:\n"
                    settings.speechSynth.each() {
                        statusmsg += "  - ${it.displayName}\n"
                    }
                }
                if (settings.repeatpush)  {
                    statusmsg += "Repeating alerts up to 10 times\n"
                } else {
                    statusmsg += "Not repeating alerts\n"
                }
                if (settings.phoneNumber) {
                    statusmsg += "SMS Number: ${settings.phoneNumber}\n\n"
                } else {
                    statusmsg += "Not sending SMS Alerts\n\n"
                }
                if (settings.runHHAction) {
                    statusmsg += "Hello Home on Alert:\n${settings.hhactionOnAlert}\n"
                    if (!(settings.hhactionOnAlertClear == null)) { statusmsg += "Hello Home on Clear:\n${settings.hhactionOnAlertClear}\n\n" }
                    if (settings.hhactionOnAlertClear == null) { statusmsg += "Hello Home on Clear:\nNot configured\n\n" }
                } else {
                    statusmsg += "Not running Hello Home Actions\n\n"
                }
                if (settings.camera) {
                   statusmsg += "Photo Burst with camera: ${settings.camera}\n"
                   statusmsg += "Photo Burst Count: ${settings.burstCount}"
                } else {
                   statusmsg += "Not using camera burst"
                }
                paragraph statusmsg
                paragraph appinfo
            } else {
                href "pageConfigure", title:"Configure", description:"Tap to open"
                paragraph appinfo
            }
        }
    }
}

def pageConfigure() {
    dynamicPage(name: "pageConfigure", title: "Configure", install: true, uninstall: true) {
        section("When . . .") {
            input "contactSensor", "capability.contactSensor", title: "This is left open"
            input "numMinutes", "number", title: "For how many minutes", required: true
        }
        section("Alert . . .") {
            input "messageText", "text", title: "Send notification that says", required: true
            input "speechSynth", "capability.speechSynthesis", title: "Announce with these text-to-speech devices", multiple: true, required: false
            input "phoneNumber", "phone", title: "Send SMS message to", required: false
            input "repeatpush", "bool", title: "Repeat notification until resolved (up to 10x)?", required: true
        }
        section ("Run hello home action on alert (optional)"){
            href "pageHHAction", title:"Run Hello Home", description:"Tap to open"
        }
        section("Take photos on alert (optional)"){
            input "camera", "capability.imageCapture", title: "Which Camera", required: false
            input "burstCount", "number", title: "How many snapshots? (default 5)", defaultValue:5, required: false
        }
        section([mobileOnly:true]){
            label title: "SmartApp Name (Front Door Left Open)", required: true
            mode title: "Set for specific mode(s)", required: false
        }
    }
}

def pageHHAction(){
    dynamicPage(name:"pageHHAction", title: "Run Hello Home Action"){
        section (){
            paragraph "Caution:\nTo prevent conflicting hello home actions by multiple doors, please configure this for only one door/app install at this time"
        }
        def hhphrases = location.helloHome?.getPhrases()*.label
        if (hhphrases) { 
            hhphrases.sort()
            section("When door is left open, change mode on first alert to:"){
                input "runHHAction", "bool", title: "Run Hello Home Action?", required: false, refreshAfterSelection:true
                input "hhactionOnAlert", "enum", title: "Action:", options: hhphrases, required: settings.runHHAction
            }
            section("When door is closed, change mode to:"){
                input "hhactionOnAlertClear", "enum", title: "Action:", options: hhphrases , required: false
            }
        } else {
          section("No Hello Home Actions found in your SmartThings configuration")
        }
    }
}

def installed() {
    subscribe(contactSensor, "contact", onContactChange);
    state.appversion = "1.3.2-beta1"
    state.count = 0;
    state.maxrepeat = 10;
    state.alertmsg = "";
}

def updated() {
    unsubscribe()
    state.appversion = "1.3.3"
    subscribe(contactSensor, "contact", onContactChange);
    state.count = 0;
    state.maxrepeat = 10;
    state.alertmsg = "";
}

def onContactChange(evt) {
    log.debug "onContactChange";
    if (evt.value == "open") {
        state.count = 0;
        state.maxrepeat = 10;
        runIn(numMinutes * 60, onContactLeftOpenHandler);
    } else {
        //Door closed
        unschedule(onContactLeftOpenHandler);
        if (runHHAction && (state.count > 0) && (!(settings.hhactionOnAlertClear == null))){
            //Run Hello Home Action on Alert
            location.helloHome.execute(settings.hhactionOnAlertClear)
        }
        state.count = 0
    }
}

def onContactLeftOpenHandler() {
    log.debug "onContactLeftOpenHandler";
    if (contactSensor.latestValue("contact") == "open") {
        state.count = state.count + 1
        log.debug "Door still open, alert! (Alert #${state.count})"
        if (state.count == 1) {
            //Run the following only on the first alert trigger
            state.alertmsg = messageText
            if (runHHAction){
              //Run Hello Home Action on Alert
              location.helloHome.execute(settings.hhactionOnAlert)
            }
        }
        if (state.count > 1 && state.count < state.maxrepeat) {state.alertmsg = "${messageText}. Repeat #${state.count}."}
        if (state.count == state.maxrepeat) {state.alertmsg = "${messageText}. Last notice."}
        sendPush(state.alertmsg);
        sendSms(phoneNumber, state.alertmsg);
        if (settings.speechSynth) {settings.speechSynth*.speak("Door Left Open Alert! ! ! ${state.alertmsg}")}
        if (repeatpush) {
            if (state.count < state.maxrepeat) {
                log.debug "Rescheduling repeat alert";
                unschedule();
                runIn(numMinutes * 60, onContactLeftOpenHandler);
            }
        }
        if (camera) {
            camera.take()
            log.debug "Camera: Snap"
            (1..((burstCount ?: 10) - 1)).each {
                log.debug "Camera: Snap"
                camera.take(delay: (500 * it))
                pause(1500)
        }
    }
    } else {
        log.debug "Door closed, cancel alert"
    }
}

private def notifyVoice(msg) {
    if (!settings.speechSynth) {
        return
    }

    def phrase = null
    phrase = settings.speechText ?: getStatusPhrase()
    settings.speechSynth*.speak(phrase)
}