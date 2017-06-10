/**
 *  App Name:   Wireless 3-Way
 *
 *  Author: 	chrisb 
 *  Date: 		2013-07-16
 *  
 *  This app "groups" a set of switches together so that if any one is turned on
 *	or off, they all will go on or off.  This allows them to act as three-way
 *	without needing wiring between the switches.
 *	
 *  This program is Public Domain.
 */

// This section is what the user will see and asks him or her to input the data we need.

// Automatically generated. Make future change here.
definition(
    name: "Wireless 3-Way",
    namespace: "sirhcb",
    author: "seateabee@gmail.com",
    description: "Set up any two (or more) switches to turn on or off together.  This allows for a wireless three way setup.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
    section("Turn on which switches or outlets?"){						// Whatever is in the quotes will be displayed to the user.
		input "switches", "capability.switch", title: "Which?", multiple: true	
        
        // input means we're getting data from teh user.  The first item "switches" is the nickname we're giving to this data.
        
        // The second item "capability.switch" is asking what type of device we're looking for.  We're looking for a device that
        // can turn on and off.  That can "switch."  
        
        // The third item, Title, is optional.  This will display in the field that the user sees.  You don't need to include this. 
        
        // Finally, we're indicating "multiple: true".  This lets the user select multiple devices that fit the profile (ie, devices 
        // that can 'switch').  This is also optional.  Obviously we need multiple devices for this program but if you don't for
        // future programs, don't include this.     
    }
}


// This section tells the program what to do when it's first installed.
def installed()
{
	subscribe(switches, "switch.on", switchOnHandler)			
    subscribe(switches, "switch.off", switchOffHandler)
    
    // The first line here tells the program to associate "switches" (our nickname for the devices) we picked above with the procedure
    // 'switchOnHandler.' That's just a name for the procedure we're do later.  It also associates an action or event ("switch.on") with that
    // procedure.
        
    // As I'm sure you guessed the second line tells the program to associate any of our 'switches' and the event ("switch.off") 
    // with the switchOffHandler procedure.
}

// This section tells the program what to do if a user who has installed the program updates it.
def updated()
{
	unsubscribe()
	subscribe(switches, "switch.on", switchOnHandler)
    subscribe(switches, "switch.off", switchOffHandler)

	// The first line here unsubscribes to any action that the program previous subscribed too.  It's a sort of "reset" of the program to
    // clear out any old data.  After this we re-subscribe to the same two things we did when the program was first installed.
    
    // If this wasn't done we'd still be looking at old data and the program wouldn't operate right.  For example, let's say the user installed
    // this program for switch A and switch B.  Later, he changed it to switch B and switch C.  If we didn't unsubscribe, the program would still
    // be looking at switch A and B for actions.  If we didn't re-subscribe the program would never start look at switch C.

}

// This procedure tells the program what to do if it sees a switch turned on.
def switchOnHandler(evt) {
	log.debug "I see a switch was turned on... I'm turning them all on!"
 	switches.on()

	// def = definition.  We're defining this procedure.  The name of this procedure is the same as the what we told the program to 
    // run when it sees a switch turned on.  The (evt) is saying: When you see this event happen (ie: "switch.on") do the actions in
    // this procedure.
    
    // The first line, log.debug "whatever" is for the simulator.  It will display this line when the program comes to this point.  
    // These lines are very useful for long complex programs that aren't working right.  They are a sort of sign that the app
    // holds up to say: This is where I am in the program.  These are optional.  You do not need to put these in, but again, very
    // useful for debugging programs.
    
    // The final line is the real meat of the program.  We are telling all the devices we selected in the preferences section above 
    // (nicknamed "swtiches") that they need to turn on.  The the () at the end allows us to include some optional things.  
    // For example, let's say for some silly reason we want the rest of the lights to come on a few seconds after a switch is pressed.  
    // We can say: switch.on(delay: 3000) instead of just switch.on().  This will delay the command by 3000 milliseconds (or 3 seconds).

}

// Thie procedure tells the program what to do if it sees a switch turned off.
def switchOffHandler(evt) {
	log.debug "I see a switch was turned off... I'm turning them all off!"
 	switches.off()

	// Obviously this procedure is virtually identical to the previous.  The only difference here is that instead of telling the switches
    // to turn on (switches.on) we're telling them to turn off (switches.off).  Again, remember that 'switches' is just the nickname we
    // picked above.  You can use pretty much any nickname you want.  We could have called them daves instead of switches.  Then the 
    // commands would be daves.on() or daves.off()
}