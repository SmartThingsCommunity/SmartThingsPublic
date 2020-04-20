## Notification Preferences
Notifications allows you to receive a message when a certain action occurs.

#####Currently there are only two events that will trigger a notification: 

* When a scheduled poll hasn't occurred after a set amount of seconds.
* When there is a SmartApp or Device Type code update available.

<img src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Screenshots/App/notif_prefs_page_1.png" width="281" height="500"><img src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Screenshots/App/notif_prefs_page_2.png" width="281" height="500">

**The __Send Notifications to__ input will not be shown for users whose accounts do not have contact book enabled.  If you don't have it you can't enable it until SmartThings activates the feature again. If the Contact book is still not available.  You will see a toggle that will allow sending Push Messages instead of selecting individual users.**
_________
#### Enable Contact-Book Option on SmartThings Account
This is not the official method and is consider a hack.  This will not cause any harm.

* Log into the IDE 
* Browse to [https://graph.api.smartthings.com/account/list](https://graph.api.smartthings.com/account/list)
* Click on the link for the Name of your account
* In you're web browsers URL bar you will see `https://graph.api.smartthings.com/account/show/%My_Unique_ID%`
* Just replace the `account/show` section with `contact`
* So it looks like this `https://graph.api.smartthings.com/contact/%My_Unique_ID%`
* Select +New Contact or Either of the Import Buttons to proceed.
* All that you need to do is add one contact to your account for it to be enabled. From there you can then managed the contacts from the Mobile App under the Slide out settings menu.


----------
#### Silence Notifications Time
If notifications are active.  It will not send them during the times and days selected.

<img src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Screenshots/App/notif_quiet_page.png" width="281" height="500">

* You can also use Modes to activate quiet time
* *The Start/Stop Time options available are:* 
	* A Specific Time
	* Sunrise
	* Sunset
* As well as Days of the Week

----------

#### Missed Poll Notifications
This will send a notification after the set number of seconds passed the last poll.
The wait before sending another value is the time that must pass before being notified for this event again.

* *The default time passed value is every 15 minutes(900 seconds)*
* *The default wait value is 30 minutes(1800 seconds)*  
* ***The custom values allowed are between (30-84600) seconds***
	
----------
	
#### Code Update Notifications
When there is a SmartApp or Device Type code update available.

* *The default value is every 2 Hours(7200 seconds)*  
* ***The custom values allowed are between (30-84600) seconds***

----------

#### Custom Automation App Notifications

As of v1.1.0 of the Automations app you can select individual recipients for the different automations.