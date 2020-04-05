/* eslint-disable no-undef, no-unused-expressions */
const chai = require('chai')
const EndpointContext = require('../../../lib/util/smart-app-context')
const SmartApp = require('../../../lib/smart-app')

const {expect} = chai
chai.use(require('chai-datetime'))

describe('smart-app-context-spec', () => {
	let app
	let event
	let date

	beforeEach(() => {
		app = new SmartApp({logUnhandledRejections: false})
		date = new Date()
		event = {
			lifecycle: 'UPDATE',
			executionId: 'b328f242-c602-4204-8d73-33c48ae180af',
			locale: 'en',
			version: '1.0.0',
			client: {
				os: 'string',
				version: 'string',
				language: 'string',
				supportedTemplates: [
					'BASIC_V1'
				]
			},
			updateData: {
				authToken: 'AUTH_TOKEN',
				refreshToken: 'REFRESH_TOKEN',
				installedApp: {
					installedAppId: 'd692699d-e7a6-400d-a0b7-d5be96e7a564',
					locationId: 'e675a3d9-2499-406c-86dc-8a492a886494',
					config: {
						lightSwitch: [
							{
								valueType: 'DEVICE',
								deviceConfig: {
									deviceId: '74aac3bb-91f2-4a88-8c49-ae5e0a234d76',
									componentId: 'main'
								}
							}
						],
						minutes: [
							{
								valueType: 'STRING',
								stringConfig: {
									value: '5'
								}
							}
						],
						modes: [
							{
								valueType: 'MODE',
								modeConfig: {
									modeId: '0ec5488d-6fe7-4a52-ad61-8e9699948d87'
								}
							}
						],
						truthiness: [
							{
								valueType: 'STRING',
								stringConfig: {
									value: 'true'
								}
							}
						],
						mydate: [
							{
								valueType: 'STRING',
								stringConfig: {
									value: date
								}
							}
						]
					},
					permissions: [
						'r:devices:e457978e-5e37-43e6-979d-18112e12c961',
						'r:devices:74aac3bb-91f2-4a88-8c49-ae5e0a234d76',
						'x:devices:74aac3bb-91f2-4a88-8c49-ae5e0a234d76'
					]
				},
				previousConfig: {
					property1: [
						{
							valueType: 'DEVICE',
							deviceConfig: {
								deviceId: '31192dc9-eb45-4d90-b606-21e9b66d8c2b',
								componentId: 'main'
							}
						}
					],
					property2: [
						{
							valueType: 'DEVICE',
							deviceConfig: {
								deviceId: '31192dc9-eb45-4d90-b606-21e9b66d8c2b',
								componentId: 'main'
							}
						}
					]
				},
				previousPermissions: [
					'string'
				]
			},
			settings: {}
		}
	})

	it('should return a date config value', () => {
		const context = new EndpointContext(app, event, undefined)
		let value = context.configDateValue('mydate')
		expect(value).to.equalDate(date)

		value = context.configDateValue('bad-key')
		expect(value).to.be.undefined
	})

	it('should return a locale time config value', () => {
		const context = new EndpointContext(app, event, undefined)
		let value = context.configTimeString('mydate')
		expect(value).to.equal(date.toLocaleTimeString('en-US', {hour: '2-digit', minute: '2-digit'}))

		value = context.configTimeString('mydate', {hour: 'numeric', minute: 'numeric'})
		expect(value).to.equal(date.toLocaleTimeString('en-US', {hour: 'numeric', minute: 'numeric'}))

		value = context.configTimeString('bad-key')
		expect(value).to.be.undefined
	})

	it('should return a boolean config value', () => {
		const context = new EndpointContext(app, event, undefined)
		let value = context.configBooleanValue('truthiness')
		expect(value).to.equal(true)

		value = context.configBooleanValue('bad-key')
		expect(value).to.be.false
	})

	it('should return a string config value', () => {
		const context = new EndpointContext(app, event, undefined)
		let value = context.configStringValue('minutes')
		expect(value).to.equal('5')

		value = context.configStringValue('bad-key')
		expect(value).to.be.undefined
	})

	it('should return a number config value', () => {
		const context = new EndpointContext(app, event, undefined)
		let value = context.configNumberValue('minutes')
		expect(value).to.equal(5)

		value = context.configNumberValue('bad-key')
		expect(value).to.be.undefined
	})

	it('should return a mode config value array', () => {
		const context = new EndpointContext(app, event, undefined)
		let value = context.configModeIds('modes')
		expect(value).to.be.an.instanceof(Array)
		expect(value).to.include('0ec5488d-6fe7-4a52-ad61-8e9699948d87')

		value = context.configModeIds('bad-key')
		expect(value).to.be.undefined
	})
})
