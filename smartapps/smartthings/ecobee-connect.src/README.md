Ecobee Thermostat SmartApp(s) and related Device Types for use with the SmartThings Hub
======================================================

## Introduction

This SmartApp and the related Device types are intended to be used with the [Ecobee thermostats](http://www.ecobee.com/). 

Here are links to the working version of the repository being developed and maintained by Sean Schneyer [(on GitHub)](https://github.com/StrykerSKS) [(on SmartThings Community)](https://community.smartthings.com/users/strykersks/).

## Motivation

The intent is to provide an Open Source Licensed implementation that can be used by the SmartThings community of users. This will help ensure accessibility for all users and provide for an easy mechanism for the community to help maintain/drive the functionality.

The ultimate goal would be to have these capabilities become part of the stock drivers on the "master" branch. But until that time they will be maintained as a fork providing good visibility to any changes upstream.

## Quick Links
- README.md (this file): <https://github.com/StrykerSKS/SmartThingsPublic/blob/StrykerSKS-Ecobee3/smartapps/smartthings/ecobee-connect.src/README.md>
- Ecobee (Connect) SmartApp: <https://github.com/StrykerSKS/SmartThingsPublic/tree/StrykerSKS-Ecobee3/smartapps/smartthings/ecobee-connect.src>
- Ecobee Thermostat Device: <https://github.com/StrykerSKS/SmartThingsPublic/tree/StrykerSKS-Ecobee3/devicetypes/smartthings/ecobee-thermostat.src>
- Ecobee Sensor Device: <https://github.com/StrykerSKS/SmartThingsPublic/tree/StrykerSKS-Ecobee3/devicetypes/smartthings/ecobee-sensor.src>
- SmartThings IDE: <https://graph.api.smartthings.com>

-----------------------------
# Installation

## General
> **NOTE**: While I have tested this on my system and believe it to be working. I have not run a full battery of tests that can include all possible corner cases or configurations. It is possible, perhaps even _likely_, that there are still bugs in this code. If you do run into an issue, the best option is to report it using the [Issues](https://github.com/StrykerSKS/SmartThingsPublic/issues) tab within the GitHub repository.

It is highly recommended that you use the GitHub Integration that SmartThings offers with their IDE. This will make it **much** easier to keep up to date with changes over time. For the general steps needed for setting up GitHub IDE integration, please visit <http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html> and follow the steps for performing the setup.

## Install Preparation
The first step is to ensure that you delete any existing Ecobee related devices and SmartApps. They are likely not compatible with this codebase and are almost certain to cause problems down the road. 

If you are not familiar with adding your own custom devices, then be sure to familiarize yourself with the [SmartThings IDE](https://graph.api.smartthings.com/) before you begin the installation process.

You will also need to make sure that you remember your Ecobee username and password. You should login to <http://www.ecobee.com/> now to ensure you have your credentials.

## Install Device Types
Here we will install two (2) different Device Types:
- `Ecobee Thermostat`
- `Ecobee Sensor`

Follow the steps for _either_ the GitHub Integration or the Manual method below. Do **not** try to do both methods.

### Install Using GitHub Integration
Follow these steps (all within the SmartThings IDE):
- Click on the `My Device Types` tab
- Click `Settings`
- Click `Add new repository` and use the following parameters:
  - Owner: `StrykerSKS`
  - Name: `SmartThingsPublic`
  - Branch: `StrykerSKS-Ecobee3`
- Click `Save`
- Click `Update from Repo` and select the repository we just added above
- Find and Select `ecobee-sensor.groovy` and `ecobee-thermostat.groovy`
- Select `Publish`(bottom right of screen near the `Cancel` button)
- Click `Execute Update`
- Note the response at the top. It should be something like "`Updated 0 devices and created 2 new devices, 2 published`"
- Verify that the two devices show up in the list and are marked with Status `Published`



### Install Manually from Code
For this method you will need to have one browser window open on GitHub and another on the IDE.

Follow these steps to install the Ecobee Sensor:
- [IDE] Click on the `My Device Types` tab
- [IDE] Click `New Device Type` (top right corner)
- [IDE] Click `From Code`
- [GitHub] Go to the respository for the Ecobee Sensor: <https://github.com/StrykerSKS/SmartThingsPublic/blob/StrykerSKS-Ecobee3/devicetypes/smartthings/ecobee-sensor.src/ecobee-sensor.groovy>
- [GitHub] Click `Raw`
- [GitHub] Select all of the text in the window (use Ctrl-A if using Windows)
- [GitHub] Copy all of the selected text to the Clipboard (use Ctrl-C if using Windows)
- [IDE] Click inside the text box
- [IDE] Paste all of the previously copied text (use Ctrl-V if using Windows)
- [IDE] Click `Create`
- [IDE] Click `Save`
- [IDE] Click `Publish` --> `For Me`

Follow these steps to install the Ecobee Thermostat:
- [IDE] Click on the `My Device Types` tab
- [IDE] Click `New Device Type` (top right corner)
- [IDE] Click `From Code`
- [GitHub] Go to the respository for the Ecobee Sensor: <https://github.com/StrykerSKS/SmartThingsPublic/blob/StrykerSKS-Ecobee3/devicetypes/smartthings/ecobee-thermostat.src/ecobee-thermostat.groovy>
- [GitHub] Click `Raw`
- [GitHub] Select all of the text in the window (use Ctrl-A if using Windows)
- [GitHub] Copy all of the selected text to the Clipboard (use Ctrl-C if using Windows)
- [IDE] Click inside the text box
- [IDE] Paste all of the previously copied text (use Ctrl-V if using Windows)
- [IDE] Click `Create`
- [IDE] Click `Save`
- [IDE] Click `Publish` --> `For Me`


## Install SmartApp
Here we will install the following SmartApp:
- `Ecobee (Connect)`

Follow the steps for _either_ the GitHub Integration or the Manual method below. Do **not** try to do both methods.

### Install Using GitHub Integration
Follow these steps (all within the SmartThings IDE):
- Click on the `My SmartApps` tab
- Click `Settings`
- Click `Add new repository` and use the following parameters:
  - Owner: `StrykerSKS`
  - Name: `SmartThingsPublic`
  - Branch: `StrykerSKS-Ecobee3`
- Click `Save`
- Click `Update from Repo` and select the repository we just added above
- Find and Select `ecobee-connect.groovy`
- Select `Publish`(bottom right of screen near the `Cancel` button)
- Click `Execute Update`
- Note the response at the top. It should be something like "`Updated 0 and created 1 SmartApps, 1 published`"
- Verify that the SmartApp shows up in the list and is marked with Status `Published`
- Click on the `Edit Properties` button to the left of the SmartApp that we just added (looks like pencil on a paper)
- Click on the `OAuth` tab
- Click `Enable OAuth in Smart App`
- Click `Update` (bottom left of screen)
- Verify that `Updated SmartApp` appears at the top of the screen


-------------------------

## Open Items / To Dos

There is still plenty of work left to do and items to work on. Here is a list of some of the items. This may also serve as a possible wishlist in the future:
- Celsius: So far I have only tested this with Farhenheit. I plan to also test Celsius but haven't had a chance to do so. From working with the code, I suspect that there **will** be issues found here.
- Additional SmartApps for things like Mode changes, etc.


## Contributors

The easiest way to track the contributors to the project will be to check the contributors listed on GitHub.

## License

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:
      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.






