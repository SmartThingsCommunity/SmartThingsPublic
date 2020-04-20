## Polling & Timing Preferences

#### What is it For?
Polling preferences allows you to fine tune when data is refreshed from Nest.
There are two calls made: 

* One for the Location data
* The other is all Device data for the Location

##### *FWIW: As of version 2.0 we are now using Cron scheduling to perform Device and Location Polls.* 

#### If you are tinkering with the Devices modes and temps a lot Nest's API has rate limiting system in place.  Once you are rate limited it will block any calls for 60 seconds to 60 minutes.  In everyday use this should not be an issue.

For more info on Nest Manager and Nest's Rate Limiting visit [Nest Commands...](https://rawgit.com/tonesto7/nest-manager/master/Documents/help/nest-commands.html)

<img src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Screenshots/App/poll_prefs_page_1.png" width="281" height="500"><img src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Screenshots/App/poll_prefs_page_2.png" width="281" height="500"><img src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Screenshots/App/poll_prefs_page_3.png" width="281" height="500">

----------
#### 1. Device Poll Rate
This is the value used when scheduling the next refresh of Device data from the API.

* *The default value is 3 minutes*

----------

#### 2. Location Poll Rate
This is the value used when scheduling the next refresh of Location data from the API.

* *The default value is every 3 minutes*
* The lowest I would set this would be 2 minutes.  
	
----------

#### 3. Weather Poll Rate
This is the value used when scheduling the next refresh of Locations Weather data from the API.

* *The default value is every 15 minutes*
* It is not recommended to set at a value lower than 5 minutes  
	
----------
	
#### 4. Forced Refresh Limit
This is the value is the Delay used when using Refresh or App Touch button. Basically it's how much time needs to pass before it will force a refresh of the data.

* *The default value is every 10 seconds*

----------

#### 5. Manual Temp Change Delay
This is the the Delay used when changing temp manually from thermostat device. This allows you to hit the change buttons in succession and after the final tap the delay is used before sending the command to Nest.

* *The default value is 4 seconds*

----------

#### Only Update Children on New Data
There is a toggle that will allow you to set the poll to only update child devices when new/changed data is received from the Nest API.  This is here to reduce the load slightly on SmartThings.

* *The default value is On*

----------

#### Advanced Polling Options
This ***Should Only*** be used if are experiencing Polling issues.  The selected devices events are used to determine if a scheduled poll was missed and reschedule it.
##### Please select as FEW devices as possible! 
#####More devices will not necessarily make for better polling!

<img src="https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Screenshots/App/poll_prefs_page_3.png" width="281" height="500">
