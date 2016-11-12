/**
 *  Low Battery Alert
 *
 *  Author: Steve Meyers
 *  Date: 2015-02-06
 *    This app will poll selected devices that use a battery and send an alert when the level reaches a specified threshold.
 *    As written, this will:
 *        Poll on the 1st & 15th every month at 10AM
 *        Alert when the batteries are lower than 13%.
 */
definition(
    name: "Low Battery Alert",
    namespace: "jfurtner",
    author: "Steve Meyers",
    description: "Alert if low battery",
    category: "Convenience",
    iconUrl: "https://www.furtner.ca/smartthings/battery-60px.png",
    iconX2Url: "https://www.furtner.ca/smartthings/battery-120px.png",
)

preferences {
    section("About") {
        paragraph "This app will poll selected devices that use a battery and send an alert when the level reaches a specified threshold."
        paragraph "You may configure up to four groups with different thresholds."
    }
    for (int i = 1; i <= 4; i++) {
        section("Monitoring group ${i}") {
            input "group_${i}", "capability.battery", title: "Select devices to monitor", multiple: true, required: false
            input "threshold_${i}", "number", title: "Notify if battery is below", defaultValue: 25
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unschedule()
    initialize()
}

def initialize() {
    //Second Minute Hour DayOfMonth Month DayOfWeek Year
    schedule("0 0 10am 1-31 * ?", check_batteries)
    check_batteries()
}

def check_batteries() {
    def size, batteries, device, threshold, value;

    for (int i = 1; i <= 4; i++) {
        size = settings["group_${i}"]?.size() ?: 0
        if (size > 0) {
            threshold = settings."threshold_${i}".toInteger()
            log.debug "Checking batteries for group ${i} (threshold ${threshold})"
            
            batteries = settings."group_${i}".currentValue("battery")
            for (int j = 0; j < size; j++) {
                  device = settings["group_${i}"][j]
                if (device != null) {
                    value = batteries[j]
                    if (value < threshold) {
                        log.debug "The $device battery is at ${value}, below threshold (${threshold})"
                        sendPush("The $device battery is at ${value}, below threshold (${threshold})")
                    } else {
                        log.debug "The $device battery is at ${value}"
                    }
                }
            }
        } else {
            log.debug "Group ${i} has no devices (${size})"
        }
    } 
}