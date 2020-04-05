'use strict'

const i18n = require('i18n')
const fs = require('fs-extra')
const {Mutex} = require('async-mutex')
const {SmartThingsOAuthClient} = require('@smartthings/core-sdk')

const Authorizer = require('./util/authorizer')
const responders = require('./util/responders')
const SmartAppContext = require('./util/smart-app-context')
const Page = require('./pages/page')
const Log = require('./util/log')
const ConfigurationError = require('./util/configuration-error')

module.exports = class SmartApp {
	/**
	 * @typedef {Object} SmartAppOptions
	 * @prop {String=} clientId The app's `clientId`, used to refresh expired tokens
	 * @prop {String=} clientSecret The app's `clientSecret`, used to refresh expired tokens
	 * @prop {String=} redirectUri The app's `redirectUri`, used for OAuth2 integrations to SmartThings
	 * @prop {String=} appId Your app name in `snake-case`, required for persisting configuration values
	 * @prop {Boolean=} disableCustomDisplayName Disallows the user's ability to set a custom name
	 * @prop {Array.<String>|String=} permissions The app's requested permissions configured in Developer Workspace
	 * @prop {Boolean=} disableRemoveApp Disallows removal of the app during the configuration flow
	 * @prop {String=} apiUrl Override the default SmartThings API host
	 * @prop {String=} refreshUrl Override the default SmartThings Auth API host
	 * @prop {String=} keyApiHost Override the default SmartThings Key API host
	 * @prop {number=} [keyCacheTTL=86400000] Override the SmartThings key cache TTL. Default 24 hours.
	 * @prop {any=} logger Override the Winston event and error logger
	 * @prop {number=} [jsonSpace=0] Basic formatting option for indentation
	 * @prop {Boolean=} enableEventLogging Log incoming lifecycle event requests and responses
	 * @prop {String=} publicKey Public key to verify the authenticity of requests
	 * @prop {Boolean=} logUnhandledRejections
	 */
	/**
	 * Create a SmartApp instance
	 * @param {SmartAppOptions} [options] Optionally, pass in a configuration object
	 */
	constructor(options = {}) {
		this._clientId = options.clientId
		this._clientSecret = options.clientSecret
		this._redirectUri = options.redirectUri
		this._id = options.appId
		this._log = new Log(options.logger, options.jsonSpace, options.enableEventLogging)
		this._permissions = options.permissions ? options.permissions : []
		this._disableCustomDisplayName = options.disableCustomDisplayName === undefined ? false : options.disableCustomDisplayName
		this._disableRemoveApp = options.disableRemoveApp === undefined ? false : options.disableRemoveApp
		this._subscribedEventHandlers = {}
		this._eventTypeHandlers = {}
		this._scheduledEventHandlers = {}
		this._pages = {}
		this._defaultPage = ((ctx, page, configurationData) => {
			page.name('System Error!')
			const msg = configurationData.pageId ?
				`No handler found for page '${configurationData.pageId}'` :
				'No page handlers were found'

			page.section('error', section => {
				section.name('Configuration Page Error')
				section.paragraphSetting('undefined_handler')
					.name('Page Handler Missing')
					.description(msg)
			})
			this._log.warn(msg)
		})
		this._installedHandler = ((ctx, installData) => {
			this._updatedHandler(ctx, installData)
		})
		this._updatedHandler = (() => { })
		this._uninstalledHandler = (() => {})
		this._oauthHandler = (() => {})
		this._deviceCommandHandler = null
		this._defaultDeviceCommandHandler = ((ctx, deviceId, cmd) => {
			this._log.warn(`No command handler for ${JSON.stringify(cmd)} of device ${deviceId}`)
		})
		this._deviceCommands = {}
		this._executeHandler = (() => {})
		this._localizationEnabled = false
		this._apiUrl = options.apiUrl
		this._refreshUrl = options.refreshUrl

		this._authorizer = new Authorizer({
			logger: this._log,
			keyApiHost: options.keyApiHost,
			publicKey: options.publicKey,
			keyCacheTTL: options.keyCacheTTL
		})
		this._unhandledRejectionHandler = reason => {
			this._log.exception(reason)
		}

		if (options.logUnhandledRejections !== false) {
			process.on('unhandledRejection', this._unhandledRejectionHandler)
		}
	}

	/// /////////////////////////////
	// App Initialization Options //
	/// /////////////////////////////

	/**
	 * Set your app identifier for use elsewhere in the app
	 * @param {String} id A globally unique, developer-defined identifier
	 * for an app. It is alpha-numeric, may contain dashes, underscores,
	 * periods, and must be less then 250 characters long.
	 * @returns {SmartApp} SmartApp instance
	 */
	appId(id) {
		this._id = id
		return this
	}

	/**
	 * Manually set the SmartThings API URL
	 * @param {String} url The host URL
	 * @default https://api.smartthings.com
	 * @returns {SmartApp} SmartApp instance
	 */
	apiUrl(url) {
		this._apiUrl = url
		return this
	}

	/**
	 * Manually set the refresh token URL
	 * @param {String} url The host URL
	 * @default https://auth-global.api.smartthings.com/oauth/token
	 * @returns {SmartApp} SmartApp instance
	 */
	refreshUrl(url) {
		this._refreshUrl = url
		return this
	}

	/**
	 * Manually set the SmartThings Key API host
	 * @param {String} url The host URL
	 * @default https://key.smartthings.com
	 * @returns {SmartApp} SmartApp instance
	 */
	keyApiHost(url) {
		this._authorizer._keyResolver._httpKeyResolve._keyApiHost = url
		return this
	}

	/**
	 * Set your smartapp automation's client id. Cannot be
	 * acquired until your app has been created through the
	 * Developer Workspace.
	 * @param {String} id The clientId
	 * @returns {SmartApp} SmartApp instance
	 */
	clientId(id) {
		this._clientId = id
		return this
	}

	/**
	 * Set your smartapp automation's client secret. Cannot be
	 * acquired until your app has been created through the
	 * Developer Workspace. This secret should never be shared
	 * or committed into a public repository.
	 * @param {String} secret The clientSecret
	 * @returns {SmartApp} SmartApp instance
	 */
	clientSecret(secret) {
		this._clientSecret = secret
		return this
	}

	/**
	 * Sets the redirect URI for API apps (i.e. OAuth-In)
	 * @param {String} uri
	 * @returns {SmartApp} SmartApp instance
	 */
	redirectUri(uri) {
		this._redirectUri = uri
		return this
	}

	/**
	 * Add your public key so that our WebHook endpoint can verify
	 * requests from SmartThings.
	 *
	 * https://smartthings.developer.samsung.com/docs/how-to/using-public-key-webhook-endpoint.html
	 * @param {String} key public key to identify your app with SmartThings
	 * @returns {SmartApp} SmartApp instance
	 */
	publicKey(key) {
		this._authorizer.setPublicKey(key)
		return this
	}

	/**
	 * Configure the event and error logger. Default behavior is to log errors but not events.
	 * @param {any} logger Override the Winston logger
	 * @param {number} [jsonSpace=null] Override the JSON formatter indentation
	 * @param {Boolean} [enableEvents=false] Logs lifecycle event requests and responses
	 * @example
	 * smartapp.configureLogger(console, 2, true)
	 */
	configureLogger(logger, jsonSpace = null, enableEvents = false) {
		this._log = new Log(logger, jsonSpace, enableEvents)
		return this
	}

	/**
	 * Enable or disable lifecycle event requests and responses
	 * without overriding the default {@see Winston} logger.
	 * @param {number} [jsonSpace=null]  Override the JSON formatter indentation
	 * @param {Boolean} [enableEvents=true] Logs lifecycle event requests and responses
	 * @example
	 * smartapp.enableEventLogging(2, true)
	 */
	enableEventLogging(jsonSpace = null, enableEvents = true) {
		this._log.enableEvents(jsonSpace, enableEvents)
		return this
	}

	/**
	 * Set app permissions as a string or array of strings.
	 *
	 * @example
	 * // sets single permission
	 * smartapp.permissions('r:devices:*')
	 * @example
	 * // sets multiple permissions
	 * smartapp.permissions('r:devices:* r:locations:*')
	 * @example
	 * // sets multiple permissions
	 * smartapp.permissions(['r:devices:*', 'r:locations:*'])
	 * @param {Array<String> | String} value
	 * @returns {SmartApp} SmartApp instance
	 */
	permissions(value) {
		this._permissions = value
		return this
	}

	/**
	 * Disable the ability for the user to customize the display name.
	 *
	 * @param {Boolean} [value=true] Value
	 * @default true
	 * @returns {SmartApp} SmartApp instance
	 */
	disableCustomDisplayName(value = true) {
		this._disableCustomDisplayName = value
		return this
	}

	/**
	 * Disable the ability to remove the app from the configuration flow.
	 *
	 * @param {Boolean} [value=true] Value
	 * @default true
	 * @returns {SmartApp} SmartApp instance
	 */
	disableRemoveApp(value = true) {
		this._disableRemoveApp = value
		return this
	}

	/**
	 * Provide a custom context store used for storing in-flight credentials
	 * for each installed instance of the app.
	 *
	 * @param {any} value ContextStore instance
	 * @example Use the AWS DynamoDB plugin
	 * smartapp.contextStore(new DynamoDBContextStore('aws-region', 'app-table-name'))
	 * @example
	 * // Use Firebase Cloud Firestore
	 * smartapp.contextStore(new FirestoreDBContextStore(firebaseServiceAccount, 'app-table-name'))
	 * @returns {SmartApp} SmartApp instance
	 */
	contextStore(value) {
		this._contextStore = value
		return this
	}

	/**
	 * Replaces the default unhandled rejection handler. If you don't want to have a default handler at
	 * all then instantiate the app with new SmartApp({logUnhandledRejections: false})
	 *
	 * @param {Function} callback when a promise rejection is not handled
	 * @returns {SmartApp} SmartApp instance */
	unhandledRejectionHandler(callback) {
		this._unhandledRejectionHandler = callback
		return this
	}

	/// ///////////////////////////
	// Configuration/Initialize //
	/// ///////////////////////////

	configureI18n(options = {}) {
		const opts = {directory: './locales', updateFiles: false, ...options}
		if (opts.updateFiles) {
			fs.ensureDirSync(opts.directory)
		}

		i18n.configure(opts)
		this._localizationEnabled = true
		return this
	}

	/**
	 * The first page users will see when they open configuration.
	 *
	 * @param {String} pageId A developer defined page ID. Must
	 * be URL safe characters.
	 * @returns {SmartApp} SmartApp instance
	 */
	firstPageId(pageId) {
		this._firstPageId = pageId
		return this
	}

	/// //////////////////////
	// Configuration/Page   //
	/// //////////////////////

	/**
	 * @typedef ConfigurationData
	 * @property {String=} installedAppId The id of the installed app.
	 * @property {String} phase Denotes the current installation phase.
	 * @property {String=} pageId A developer defined page ID. Must be URL
	 * safe characters.
	 * @property {String=} previousPageId The previous page the user
	 * completed. Must be URL safe characters.
	 * @property {Object=} config A map of configurations for an Installed App.
	 * The map 'key' is the configuration name and the 'value' is an array
	 * of strings.
	 */

	/**
	 * @typedef InstallData
	 * @property {String} authToken
	 * @property {String} refreshToken
	 * @property {InstalledApp} installedApp
	 */

	/**
	 * @typedef UninstallData
	 * @property {InstalledApp} installedApp
	 */

	/**
	 * @typedef UpdateData
	 * @property {String} authToken
	 * @property {String} refreshToken
	 * @property {InstalledApp} installedApp
	 * @property {String=} previousConfig
	 * @property {Object=} previousPermissions
	 */

	/**
	 * @callback PageCallback
	 * @param context { import("./util/smart-app-context") } SmartAppContext to
	 * access config values associated to the installed app
	 * @param page { import("./pages/page") } Chainable page instance
	 * @param {ConfigurationData} data Optionally access the raw configuration
	 * data event object
	 */

	/**
	 * @callback UpdatedCallback
	 * @param context { import("./util/smart-app-context") } SmartAppContext to
	 * access config values associated to the installed app
	 * @param {UpdateData} data Update data
	 */

	/**
	 * @callback InstalledCallback
	 * @param context { import("./util/smart-app-context") } SmartAppContext to
	 * access config values associated to the installed app
	 * @param {InstallData} data Install data
	 */

	/**
	 * @callback UninstalledCallback
	 * @param context { import("./util/smart-app-context") } SmartAppContext to
	 * access config values associated to the installed app
	 * @param {UninstallData} data Uninstall data
	 */

	/**
	 * Define a configuration page – you may chain as many pages as is
	 * necessary to satisfy your configuration needs. Please see the
	 * the documentation on how to design pages for your automation.
	 *
	 * https://smartthings.developer.samsung.com/docs/how-to/design-pages-smartapp.html
	 * @param {String} id Identify your page with a unique snake_case or
	 * camelCase identifier, used for i18n keys
	 * @param {PageCallback} callback Allows you to define config page
	 * characteristics and access config values
	 * @returns {SmartApp} SmartApp instance
	 */
	page(id, callback) {
		if (!this._firstPageId) {
			this._firstPageId = id
		}

		this._pages[id] = callback
		return this
	}

	/**
	 * Define a default handler for pages that don't have specific name-based handlers. Useful for encoding data in
	 * page name for use in controlling of the rendering of the page from different links
	 *
	 * @param {PageCallback} callback Allows you to define config page
	 * characteristics and access config values
	 * @returns {SmartApp} SmartApp instance
	 */
	defaultPage(callback) {
		this._defaultPage = callback
		return this
	}

	/// ///////////
	// Install  //
	/// ///////////

	/**
	 * App installed lifecycle event
	 * @param {InstalledCallback} callback
	 * @returns {SmartApp} SmartApp instance
	 */
	installed(callback) {
		this._installedHandler = callback
		return this
	}

	/// ///////////
	// Update   //
	/// ///////////

	/**
	 * Installed app updated lifecycle event
	 * @param {UpdatedCallback} callback
	 * @returns {SmartApp} SmartApp instance
	 */
	updated(callback) {
		this._updatedHandler = callback
		return this
	}

	/// /////////////
	// Uninstall  //
	/// /////////////

	/**
	 * Uninstalled app lifecycle event
	 * @param {UninstalledCallback} callback
	 * @returns {SmartApp} SmartApp instance
	 */
	uninstalled(callback) {
		this._uninstalledHandler = callback
		return this
	}

	/// ///////////
	// Events   //
	/// ///////////

	oauthHandler(callback) {
		this._oauthHandler = callback
		return this
	}

	/**
	 * @typedef SimpleValue A simple value
	 * @property {('NULL_VALUE'|'INT_VALUE'|'DOUBLE_VALUE'|'STRING_VALUE'|'BOOLEAN_VALUE')} valueType The type of the value.
	 * @property {number=} intValue
	 * @property {number=} doubleValue
	 * @property {String} stringValue
	 * @property {Boolean} boolValue
	 */

	/**
	 * @typedef {Object} SecurityArmStateEvent
	 * @property {String} eventId The id of the event
	 * @property {String} locationId The id of the location in which the event was triggered.
	 * @property {('UNKNOWN'|'ARMED_STAY'|'ARMED_AWAY'|'DISARMED')} armState The arm state of a security statem.
	 * @property {SimpleValue} optionalArguments A set of key / value pairs useful for passing any optional arguments
	 */
	/**
	 * @typedef {Object} ModeEvent
	 * @property {String} eventId The id of the event
	 * @property {String} locationId The id of the location in which the event was triggered.
	 * @property {String} modeId The ID of the mode associated with a MODE_EVENT.
	 */

	/**
	 * @typedef HubHealthEvent
	 * @property {String} eventId The id of the event.
	 * @property {String} locationId The id of the location in which the event was triggered.
	 * @property {String} hubId The id of the hub.
	 * @property {('OFFLINE'|'ONLINE'|'ZWAVE_OFFLINE'|'ZWAVE_ONLINE'|'ZIGBEE_OFFLINE'|'ZIGBEE_ONLINE'|'BLUETOOTH_OFFLINE'|'BLUETOOTH_ONLINE')} status The status of the hub.
	 * @property {('NONE'|'DISCONNECTED'|'INACTIVE')} reason The reason the hub is offline.
	 */

	/**
	 * @typedef DeviceHealthEvent
	 * @property {String} eventId The id of the event.
	 * @property {String} locationId The id of the location in which the event was triggered.
	 * @property {String} deviceId The id of the device.
	 * @property {String} hubId The id of the hub.
	 * @property {('OFFLINE'|'ONLINE'|'UNHEALTHY')} status The status of the device.
	 * @property {('NONE'|'SERVICE_UNAVAILABLE'|'HUB_OFFLINE'|'ZWAVE_OFFLINE'|'ZIGBEE_OFFLINE'|'BLUETOOTH_OFFLINE'|'HUB_DISCONNECTED')} reason The reason the device is offline.
	 */

	/**
	 * @typedef DeviceLifecycleMove Move device lifecycle
	 * @property {String} locationId
	 */

	/**
	 * @typedef {Object} DeviceLifecycleEvent An event on a device that matched a subscription for this app.
	 * @property {('CREATE'|'DELETE'|'UPDATE'|'MOVE_FROM'|'MOVE_TO')} lifecycle
	  * The device lifecycle. The lifecycle will be one of:
      *  - **`CREATE`** - Invoked when a device is created.
      *  - **`DELETE`**  - Invoked when a device is deleted.
      *  - **`UPDATE`**  - Invoked when a device is updated.
      *  - **`MOVE_FROM`**  - Invoked when a device is moved from a location.
      *  - **`MOVE_TO`**  - Invoked when a device is moved to a location.
	 * @property {String} eventId The ID of the event.
	 * @property {String} locationId The ID of the location in which the event was triggered.
	 * @property {String} deviceId The ID of the location in which the event was triggered.
	 * @property {String} deviceName The name of the device.
	 * @property {String} principal The principal that made the change
	 * @property {Object} create Invoked when a device is created.
	 * @property {Object} delete Invoked when a device is deleted.
	 * @property {Object} update Invoked when a device is updated.
	 * @property {DeviceLifecycleMove} moveFrom Invoked when a device is moved from a location.
	 * @property {DeviceLifecycleMove} moveTo Invoked when a device is moved to a location.
	 */

	/**
	 * @typedef {Object} DeviceEvent An event on a device that matched a subscription for this app.
	 * @property {String} eventId The ID of the event.
	 * @property {String} locationId The ID of the location in which the event was triggered.
	 * @property {String} deviceId The ID of the location in which the event was triggered.
	 * @property {String} componentId The name of the component on the device that the event is associated with.
	 * @property {String} capability The name of the capability associated with the DEVICE_EVENT.
	 * @property {String} attribute The name of the DEVICE_EVENT. This typically corresponds to an attribute name of the device-handler’s capabilities.
	 * @property {Object} value The value of the event. The type of the value is dependent on the capability's attribute type.
	 * @property {String} valueType The root level data type of the value field. The data types are representitive of standard JSON data types.
	 * @property {Boolean} stateChange Whether or not the state of the device has changed as a result of the DEVICE_EVENT.
	 * @property {Map} data json map as defined by capability data schema
	 * @property {String} subscriptionName The name of subscription that caused delivery.
	 */

	/**
	 * @typedef {Object} TimerEvent
	 * @property {String} eventId The ID of the event.
	 * @property {String} name The name of the schedule that caused this event.
	 * @property {Object} type
	 * @property {String} time The IS0-8601 date time strings in UTC that this event was scheduled for.
	 * @property {String} expression The CRON expression if the schedule was of type CRON.
	 */

	/**
	 * @callback ModeEventCallback
	 * @param context { import('./util/smart-app-context') }
	 * @param {ModeEvent} modeEvent
	 */

	/**
	 * @callback DeviceEventCallback
	 * @param context { import('./util/smart-app-context') }
	 * @param {DeviceEvent} deviceEvent
	 */

	/**
	 * @callback TimerEventCallback
	 * @param context { import('./util/smart-app-context') }
	 * @param {TimerEvent} timerEvent
	 */

	/**
	 * @callback HubHealthEventCallback
	 * @param context { import('./util/smart-app-context') }
	 * @param {HubHealthEvent} hubHealthEvent
	 */

	/**
	 * @callback DeviceHealthEventCallback
	 * @param context { import('./util/smart-app-context') }
	 * @param {DeviceHealthEvent} deviceHealthEvent
	 */

	/**
	 * @callback DeviceLifecycleEventCallback
	 * @param context { import('./util/smart-app-context') }
	 * @param {DeviceLifecycleEvent} deviceLifecycleEvent
	 */

	/**
	 * @callback SecurityArmStateEventCallback
	 * @param context { import('./util/smart-app-context') }
	 * @param {SecurityArmStateEvent} securityArmStateEvent
	 */

	/**
	 * Internal handler for named subscriptions to events
	 *
	 * @param {String} name Provide the name matching a created subscription
	 * @param {*} callback Callback handler object
	 * @param {String} [eventType=undefined] The type of event
	 * @returns {SmartApp} SmartApp instance
	 */
	subscribedEventHandler(name, callback, eventType = undefined) {
		if (eventType) {
			if (this._eventTypeHandlers[eventType] && this._eventTypeHandlers[eventType] !== name) {
				throw new ConfigurationError(`Event type ${eventType} already assigned to handler ${this._eventTypeHandlers[eventType]}`)
			}

			this._eventTypeHandlers[eventType] = name
		}

		this._subscribedEventHandlers[name] = callback
		return this
	}

	/**
	 * Mode event handler. Only one can be used at a time.
	 * @param {String} name Subscription name
	 * @param {ModeEventCallback} callback `MODE_EVENT` callback
	 * @returns {SmartApp} instance
	 */
	subscribedModeEventHandler(name, callback) {
		const eventType = 'MODE_EVENT'
		this.subscribedEventHandler(name, callback, eventType)
		return this
	}

	/**
	 * Device event handler. Only one can be used at a time.
	 * @param {String} name
	 * @param {DeviceEventCallback} callback
	 */
	subscribedDeviceEventHandler(name, callback) {
		const eventType = 'DEVICE_EVENT'
		this.subscribedEventHandler(name, callback, eventType)
		return this
	}

	/**
	 * Timer event handler. Only one can be used at a time.
	 * @param {String} name
	 * @param {TimerEventCallback} callback
	 */
	subscribedTimerEventHandler(name, callback) {
		const eventType = 'TIMER_EVENT'
		this.subscribedEventHandler(name, callback, eventType)
		return this
	}

	/**
	 * Hub health event handler. Only one can be used at a time.
	 * @param {String} name
	 * @param {HubHealthEventCallback} callback
	 */
	subscribedHubHealthEventHandler(name, callback) {
		const eventType = 'HUB_HEALTH_EVENT'
		this.subscribedEventHandler(name, callback, eventType)
		return this
	}

	/**
	 * Device lifecycle event handler. Only one can be used at a time.
	 * @param {String} name
	 * @param {DeviceLifecycleEventCallback} callback
	 */
	subscribedDeviceLifecycleEventHandler(name, callback) {
		const eventType = 'DEVICE_LIFECYCLE_EVENT'
		this.subscribedEventHandler(name, callback, eventType)
		return this
	}

	/**
	 * Device health event handler. Only one can be used at a time.
	 * @param {String} name
	 * @param {DeviceHealthEventCallback} callback
	 */
	subscribedDeviceHealthEventHandler(name, callback) {
		const eventType = 'DEVICE_HEALTH_EVENT'
		this.subscribedEventHandler(name, callback, eventType)
		return this
	}

	/**
	 * Security arm state event handler. Only one can be used at a time.
	 * @param {String} name
	 * @param {SecurityArmStateEventCallback} callback
	 */
	subscribedSecurityArmStateEventHandler(name, callback) {
		const eventType = 'SECURITY_ARM_STATE_EVENT'
		this.subscribedEventHandler(name, callback, eventType)
		return this
	}

	/**
	 * Handler for named subscriptions to **scheduled** events
	 *
	 * @param {String} name Provide the name matching a created subscription
	 * @param {Object} callback Callback handler object
	 * @returns {SmartApp} SmartApp instance
	 */
	scheduledEventHandler(name, callback) {
		this._scheduledEventHandlers[name] = callback
		return this
	}

	/**
	 * Handler for device commands
	 *
	 * @param {Object} callback Callback handler object
	 * @returns {SmartApp} SmartApp instance
	 */
	deviceCommandHandler(callback) {
		this._deviceCommandHandler = callback
		return this
	}

	/**
	 * Handler for device commands
	 *
	 * @param {Object} callback Callback handler object
	 * @returns {SmartApp} SmartApp instance
	 */
	defaultDeviceCommandHandler(callback) {
		this._defaultDeviceCommandHandler = callback
		return this
	}

	/**
	 * Device command and callback
	 *
	 * @param {String} command Device command
	 * @param {Object} callback Callback handler object
	 * @returns {SmartApp} SmartApp instance
	 */
	deviceCommand(command, callback) {
		this._deviceCommands[command] = callback
		return this
	}

	/**
	 * Handler for execute events
	 * @param {Object} callback handler
	 * @returns {SmartApp} SmartApp instance
	 */
	executeHandler(callback) {
		this._executeHandler = callback
		return this
	}

	/// //////////////
	// Utilities   //
	/// //////////////

	translate(...args) {
		if (this._localizationEnabled) {
			return this.__(...args)
		}

		return args[0]
	}

	/**
	 * Use with an AWS Lambda function. No signature verification is required.
	 *
	 * @param {*} event
	 * @param {*} context
	 * @param {*} callback
	 */
	handleLambdaCallback(event, context, callback) {
		this._handleCallback(event, responders.lambdaResponse(callback, this._log))
	}

	/**
	 * Use with a standard HTTP webhook endpoint app. Signature verification is required.
	 *
	 * @param {*} request
	 * @param {*} response
	 */
	async handleHttpCallback(request, response) {
		if (request.body && request.body.lifecycle === 'PING') {
			return this._handleCallback(request.body, responders.httpResponder(response, this._log))
		}

		const isAuthorized = await this._authorizer.isAuthorized(request)
		if (isAuthorized) {
			return this._handleCallback(request.body, responders.httpResponder(response, this._log))
		}

		this._log.error('Unauthorized')
		response.status(401).send('Forbidden')
	}

	/**
	 * Use with a standard HTTP webhook endpoint app, but
	 * disregard the HTTP verification process.
	 *
	 * @param {*} request
	 * @param {*} response
	 */
	handleHttpCallbackUnverified(request, response) {
		this._handleCallback(request.body, responders.httpResponder(response, this._log))
	}

	/**
	 * Used for internal unit testing.
	 *
	 * @param {Object} body
	 */
	async handleMockCallback(body) {
		const responder = responders.mockResponder(this._log)
		await this._handleCallback(body, responder)
		return responder.response
	}

	async handleOAuthCallback(request) {
		const oauthClient = new SmartThingsOAuthClient(this._clientId, this._clientSecret, this._redirectUri)
		const auth = await oauthClient.redeemCode(request.query.code)
		const ctx = await this.withContext({
			installedAppId: auth.installed_app_id,
			authToken: auth.access_token,
			refreshToken: auth.refresh_token
		})

		const isa = await ctx.api.installedApps.get(auth.installed_app_id)
		ctx.setLocationId(isa.locationId)

		if (this._contextStore) {
			this._contextStore.put({
				installedAppId: ctx.installedAppId,
				locationId: ctx.locationId,
				authToken: auth.access_token,
				refreshToken: auth.refresh_token,
				clientId: this._clientId,
				clientSecret: this._clientSecret,
				config: ctx.config
			})
		}

		return ctx
	}

	/// ///////////////////////////////////////////////////////////
	// Proactive API calls (not in response to lifecycle events //
	/// ///////////////////////////////////////////////////////////

	withContext(installedAppIdOrObject) {
		const app = this
		if (typeof installedAppIdOrObject === 'object') {
			return new Promise(resolve => {
				resolve(new SmartAppContext(app, installedAppIdOrObject, new Mutex()))
			})
		}

		if (this._contextStore) {
			return new Promise((resolve, reject) => {
				this._contextStore.get(installedAppIdOrObject).then(data => {
					resolve(new SmartAppContext(app, data, new Mutex()))
				}).catch(error => {
					reject(error)
				})
			})
		}

		return Promise.reject(new Error('Context not available. No context store defined'))
	}

	/// /////////////////////
	// Event Dispatching  //
	/// /////////////////////

	async _handleCallback(evt, responder) {
		const context = new SmartAppContext(this, evt)
		try {
			const {messageType, lifecycle} = evt
			switch (lifecycle || messageType) {
				case 'PING': {
					this._log.event(evt)
					responder.respond({statusCode: 200, pingData: {challenge: evt.pingData.challenge}})
					break
				}

				case 'CONFIGURATION': {
					const {configurationData} = evt

					// Inject whether or not the request was a resubmitted page
					configurationData.isResubmit = configurationData.pageId === configurationData.previousPageId

					switch (configurationData.phase) {
						case 'INITIALIZE': {
							this._log.event(evt, configurationData.phase)
							responder.respond({
								statusCode: 200, configurationData: {
									initialize: {
										id: this._id,
										firstPageId: this._firstPageId,
										permissions: this._permissions,
										disableCustomDisplayName: this._disableCustomDisplayName,
										disableRemoveApp: this._disableRemoveApp
									}
								}
							})
							break
						}

						case 'PAGE': {
							await context.retrieveTokens()
							this._log.event(evt, configurationData.phase)
							const pageId = configurationData.pageId ? configurationData.pageId : this._firstPageId
							const pageHandler = this._pages[pageId]
							if (pageHandler) {
								const page = this._localizationEnabled ? new Page(pageId, context.locale) : new Page(pageId)
								await pageHandler(context, page, configurationData)
								responder.respond({statusCode: 200, configurationData: {page: page.toJson()}})
							} else {
								const page = this._localizationEnabled ? new Page(pageId, context.locale) : new Page(pageId)
								await this._defaultPage(context, page, configurationData)
								responder.respond({statusCode: 200, configurationData: {page: page.toJson()}})
							}

							break
						}

						default:
							throw new Error(`Unsupported config phase: ${configurationData.phase}`)
					}

					break
				}

				case 'OAUTH_CALLBACK': {
					this._log.event(evt)
					await this._oauthHandler(context, evt.oauthCallbackData)
					responder.respond({statusCode: 200, oAuthCallbackData: {}})
					break
				}

				case 'INSTALL': {
					this._log.event(evt)
					await this._installedHandler(context, evt.installData)
					if (this._contextStore) {
						this._contextStore.put({
							installedAppId: context.installedAppId,
							locationId: context.locationId,
							authToken: context.authToken,
							refreshToken: context.refreshToken,
							clientId: this._clientId,
							clientSecret: this._clientSecret,
							config: context.config
						})
					}

					responder.respond({statusCode: 200, installData: {}})
					break
				}

				case 'UPDATE': {
					this._log.event(evt)
					await this._updatedHandler(context, evt.updateData)
					if (this._contextStore) {
						this._contextStore.put({
							installedAppId: context.installedAppId,
							locationId: context.locationId,
							authToken: context.authToken,
							refreshToken: context.refreshToken,
							clientId: this._clientId,
							clientSecret: this._clientSecret,
							config: context.config
						})
					}

					responder.respond({statusCode: 200, updateData: {}})
					break
				}

				case 'UNINSTALL': {
					this._log.event(evt)
					await this._uninstalledHandler(context, evt.uninstallData)
					if (this._contextStore) {
						this._contextStore.delete(context.installedAppId)
					}

					responder.respond({statusCode: 200, uninstallData: {}})
					break
				}

				case 'EVENT': {
					this._log.event(evt)
					const results = []
					for (const event of evt.eventData.events) {
						switch (event.eventType) {
							case 'DEVICE_EVENT': {
								const handlerName = event.deviceEvent.subscriptionName.split('_')[0]
								const handler = this._subscribedEventHandlers[handlerName]
								results.push(handler(context, event.deviceEvent, event.eventTime))
								break
							}

							case 'TIMER_EVENT': {
								const handlerName = event.timerEvent.name
								const handler = this._scheduledEventHandlers[handlerName]
								results.push(handler(context, event.timerEvent, event.eventTime))
								break
							}

							case 'DEVICE_COMMANDS_EVENT': {
								if (this._deviceCommandHandler) {
									results.push(this._deviceCommandHandler(context, event.deviceCommandsEvent))
								} else {
									const {deviceCommandsEvent} = event
									for (const cmd of deviceCommandsEvent.commands) {
										const compKey = `${cmd.componentId}/${cmd.capability}/${cmd.command}`
										const capKey = `${cmd.capability}/${cmd.command}`
										let handler = this._deviceCommands[compKey]
										if (!handler) {
											handler = this._deviceCommands[capKey]
										}

										if (handler) {
											results.push(handler(context, deviceCommandsEvent.deviceId, cmd, deviceCommandsEvent, event.eventTime))
										} else {
											this._defaultDeviceCommandHandler(context, deviceCommandsEvent.deviceId, cmd, event.eventTime)
										}
									}
								}

								break
							}

							case 'DEVICE_LIFECYCLE_EVENT': {
								// TODO - remove when handler name is returned in event
								const handlerName = this._eventTypeHandlers[event.eventType]
								const handler = this._subscribedEventHandlers[handlerName]
								results.push(handler(context, event.deviceLifecycleEvent, event.eventTime))
								break
							}

							case 'DEVICE_HEALTH_EVENT': {
								// TODO - remove when handler name is returned in event
								const handlerName = this._eventTypeHandlers[event.eventType]
								const handler = this._subscribedEventHandlers[handlerName]
								results.push(handler(context, event.deviceHealthEvent, event.eventTime))
								break
							}

							case 'HUB_HEALTH_EVENT': {
								// TODO - remove when handler name is returned in event
								const handlerName = this._eventTypeHandlers[event.eventType]
								const handler = this._subscribedEventHandlers[handlerName]
								results.push(handler(context, event.hubHealthEvent, event.eventTime))
								break
							}

							case 'MODE_EVENT': {
								// TODO - remove when handler name is returned in event
								const handlerName = this._eventTypeHandlers[event.eventType]
								const handler = this._subscribedEventHandlers[handlerName]
								results.push(handler(context, event.modeEvent, event.eventTime))
								break
							}

							case 'SECURITY_ARM_STATE_EVENT': {
								// TODO - remove when handler name is returned in event
								const handlerName = this._eventTypeHandlers[event.eventType]
								const handler = this._subscribedEventHandlers[handlerName]
								results.push(handler(context, event.securityArmStateEvent, event.eventTime))
								break
							}

							case 'INSTALLED_APP_LIFECYCLE_EVENT': {
								// TODO - remove when handler name is returned in event
								const handlerName = this._eventTypeHandlers[event.eventType]
								const handler = this._subscribedEventHandlers[handlerName]
								if (handler) {
									results.push(handler(context, event.installedAppLifecycleEvent, event.eventTime))
								} else {
									const {installedAppLifecycleEvent} = event
									if (installedAppLifecycleEvent.lifecycle === 'DELETE' && installedAppLifecycleEvent.installedAppId === context.installedAppId) {
										this._uninstalledHandler(context, {installedApp: evt.eventData.installedApp})
										if (this._contextStore) {
											this._contextStore.delete(installedAppLifecycleEvent.installedAppId)
										}
									}
								}

								break
							}

							default: {
								this._log.warn(`Unhandled event of type ${event.eventType}`)
							}
						}
					}

					await Promise.all(results)
					responder.respond({statusCode: 200, eventData: {}})
					break
				}

				case 'EXECUTE': {
					this._log.event(evt)
					const executeData = (await this._executeHandler(context, evt.executeData)) || {}
					responder.respond({statusCode: 200, executeData})
					break
				}

				case 'CONFIRMATION': {
					if (evt.confirmationData && evt.confirmationData.appId && evt.confirmationData.confirmationUrl) {
						if (this._id) {
							if (evt.confirmationData.appId === this._id) {
								this._log.info(`CONFIRMATION request for app ${evt.confirmationData.appId}, to enable events visit ${evt.confirmationData.confirmationUrl}`)
							} else {
								this._log.warn(`Unexpected CONFIRMATION request for app ${evt.confirmationData.appId}, received ${JSON.stringify(evt)}`)
							}
						} else {
							this._log.info(`CONFIRMATION request for app ${evt.confirmationData.appId}, to enable events visit ${evt.confirmationData.confirmationUrl}`)
						}
					} else {
						this._log.warn(`Invalid CONFIRMATION request ${JSON.stringify(evt)}`)
					}

					break
				}

				default: {
					this._log.warn(`Lifecycle ${evt.lifecycle} not supported`)
				}
			}
		} catch (error) {
			this._log.exception(error)
			responder.respond({statusCode: 500, message: `Server error: '${error.toString()}'`})
		}
	}
}
