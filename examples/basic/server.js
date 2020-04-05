const express = require('express')
const SmartApp = require('@smartthings/smartapp')

const server = express()
const PORT = 5555

server.use(express.json())

/* Define the SmartApp */
const smartapp = new SmartApp()
// Starting public key with '@' signifies to pull it from a file on local disk.
// If you do not have it yet, omit publicKey()
//	.publicKey('@smartthings_rsa.pub') // Optional until app verified
	.enableEventLogging(2) // Logs all lifecycle event requests and responses as pretty-printed JSON. Omit in production
	.configureI18n()
	.page('mainPage', (context, page, configData) => { // eslint-disable-line no-unused-vars
		page.section('sensors', section => {
			section
				.deviceSetting('contactSensor')
				.capabilities(['contactSensor'])
				.required(false)
		})
		page.section('lights', section => {
			section
				.deviceSetting('lights')
				.capabilities(['switch'])
				.multiple(true)
				.permissions('rx')
		})
	})
	.updated(async (context, updateData) => { // eslint-disable-line no-unused-vars
		// Called for both INSTALLED and UPDATED lifecycle events if there is no separate installed() handler
		await context.api.subscriptions.unsubscribeAll()
		return context.api.subscriptions.subscribeToDevices(context.config.contactSensor, 'contactSensor', 'contact', 'myDeviceEventHandler')
	})
	.subscribedEventHandler('myDeviceEventHandler', (context, event) => {
		const value = event.value === 'open' ? 'on' : 'off'
		context.api.devices.sendCommands(context.config.lights, 'switch', value)
	})

/* Handle POST requests */
server.post('/', (req, res, next) => { // eslint-disable-line no-unused-vars
	smartapp.handleHttpCallback(req, res)
})

/* Start listening at your defined PORT */
server.listen(PORT, () => console.log(`Server is up and running on port ${PORT}`))
