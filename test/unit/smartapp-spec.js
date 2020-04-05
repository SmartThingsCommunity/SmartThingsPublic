/* eslint no-undef: "off" */
const assert = require('assert').strict
const SmartApp = require('../../lib/smart-app')

describe('smartapp-spec', () => {
	let app
	let expectedData
	let receivedData

	beforeEach(() => {
		app = new SmartApp({logUnhandledRejections: false})
		expectedData = {
			authToken: 'string',
			refreshToken: 'string',
			installedApp: {
				installedAppId: 'd692699d-e7a6-400d-a0b7-d5be96e7a564',
				locationId: 'e675a3d9-2499-406c-86dc-8a492a886494',
				config: {}
			}
		}
	})

	it('should handle INSTALL event', () => {
		app.installed((_, installData) => {
			receivedData = installData
		})
		app.handleMockCallback({
			lifecycle: 'INSTALL',
			executionId: 'e6903fe6-f88f-da69-4c12-e2802606ccbc',
			locale: 'en',
			version: '0.1.0',
			client: {
				os: 'ios',
				version: '0.0.0',
				language: 'en-US'
			},
			installData: expectedData,
			settings: {}
		})
		assert.strictEqual(receivedData, expectedData)
	})

	it('should handle UNINSTALL event', () => {
		app.uninstalled((_, uninstallData) => {
			receivedData = uninstallData
		})
		app.handleMockCallback({
			lifecycle: 'UNINSTALL',
			executionId: 'e6903fe6-f88f-da69-4c12-e2802606ccbc',
			locale: 'en',
			version: '0.1.0',
			client: {
				os: 'ios',
				version: '0.0.0',
				language: 'en-US'
			},
			uninstallData: expectedData,
			settings: {}
		})
		assert.strictEqual(receivedData, expectedData)
	})

	it('should handle UPDATE event', () => {
		app.updated((_, updateData) => {
			receivedData = updateData
		})
		app.handleMockCallback({
			lifecycle: 'UPDATE',
			executionId: 'e6903fe6-f88f-da69-4c12-e2802606ccbc',
			locale: 'en',
			version: '0.1.0',
			client: {
				os: 'ios',
				version: '0.0.0',
				language: 'en-US'
			},
			updateData: expectedData,
			settings: {}
		})
		assert.strictEqual(receivedData, expectedData)
	})
})
