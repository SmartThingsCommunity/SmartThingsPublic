##Nest command rate limits

Nest rates limits commands to their APIs based on devices and structures. This is done to ensure availability of their services, and to not impact the operation of the devices.

Writes (changes of temps or modes) to Nest devices requires the device to wake up and synchronize its state. This can impact battery life so Nest implemented rate limits. If a device’s battery is low, nest will reject all changes until the battery is recharged.

Nest describes this in: [https://developer.nest.com/documentation/cloud/data-rate-limits/](https://developer.nest.com/documentation/cloud/data-rate-limits/)

### How commands work with Nest Manager:

It is important when designing your automation's using Nest, that your automation's do not require more changes than Nest will allow in a period of time.

The Nest Manager for SmartThings implements throttling to keep requests within Nest’s rate limits. ***If commands are received for a device that exceeds the Nest rate limits, the Nest Manager will slow the commands to 1 per minute for the device that is over the limit.***

**Nest may reject commands if the battery state of the Nest device is low. If this occurs, Nest will not accept any commands for the device.**