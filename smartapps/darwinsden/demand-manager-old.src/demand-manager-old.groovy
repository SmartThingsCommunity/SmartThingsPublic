/**
 *  Demand Manager
 *
 *  Author: Darwin@DarwinsDen.com
 *  Copyright 2018, 2019 - All rights reserved
 *  
 *  ****** WARNING ******
 *  Installation and configuration of this software will grant this application control of your home thermostat and other devices. 
 *  Unexpectedly high and low home temperatures and unexpected high utility usage & costs may result due to both the planned 
 *  and unplanned nature of the algorithms and technologies involved, unreliability of devices and networks, and un-anticipated 
 *  software defects including those in this software application and its dependencies. By installing this software, you are accepting
 *  the risks to people, pets, and personal property and agree to not hold the developer liable.
 *  
 *  This software was developed in the hopes that it will be useful to others, however, 
 *  it is distributed on an "AS IS" BASIS, WITOUT WARRANTIES OR GUARANTEES OF ANY KIND, either express or implied. 
 * 
 *  The end user is free to modify this software for personal use. Re-distribution of this software in its original or 
 *  modified form requires explicit written consent from the developer. 
 * 
 *  The developer retains all rights, title, copyright, and interest, including all copyright, patent rights, and trade secrets 
 *  associated with the algorthms, and technologies used herein. 
 *
 */
 
def version() { return "v0.1.4e.20190530" }
/*   
*	30-May-2019 >>> v0.1.4e.20190530 - Resolve new install/init issue
 *	28-May-2019 >>> v0.1.3e.20190528 - Added option to persist off-peak indication display, improved threading, additional watchdog logic
 *	07-May-2019 >>> v0.1.2e.20190507 - Added additional exception handling and watchdog processes, logic updates
 */
 
definition(
    name: "Demand Manager - Old", namespace: "darwinsden", author: "Darwin", description: "Control Demand Management.",
    category: "My Apps", iconUrl: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/meterColor.png",
    iconX2Url: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/meterColor.png"
)

preferences {
    page(name: "pageMain")
}

private pageMain() {
    dynamicPage(name: "pageMain", title: "", install: true, uninstall: true) {
       // section("Remove Demand Manager") {
       //     href "pageRemove", title: "", description: "Remove Demand Manager", required: false
       // }
   }
}

def pageRemove() {
    dynamicPage(name: "pageRemove", title: "", install: false, uninstall: true) {
        section() {
            paragraph parent ? "CAUTION: You are about to remove the. This action is irreversible. If you are sure you want to do this, please tap on the Remove button below." :
                "CAUTION: You are about to completely remove Demand Manager and all of its settings. This action is irreversible. If you are sure you want to do this, please tap on the Remove button below.",
                required: true, state: null
        }
    }
}


def installed() {
    log.debug("installed called")
    //initialize()
}

def uninstalled() {
   // removeChildDevices(getChildDevices())
}

def updated() {
    log.debug("updated called")
    unsubscribe()
    unschedule()
}

def initialize() {
    log.debug "Initializing Demand Manager"
}

