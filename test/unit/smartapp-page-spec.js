/* eslint no-undef: "off" */
const assert = require('assert').strict
const SmartApp = require('../../lib/smart-app')

describe('smartapp-page-spec', () => {
	it('should set page ID', () => {
		const app = new SmartApp()
		app.appId('xxx')
		app.page('eaMainPage', (ctx, page) => {
			page.section('whenDoorOpensAndCloses', section => {
				section.deviceSetting('contactSensor')
					.capabilities(['contactSensor'])
					.required(true)
			})

			page.section('turnLightsOnAndOff', section => {
				section.deviceSetting('lights')
					.capabilities(['switch'])
					.multiple(true)
					.permissions('rx')
					.required(true)
			})
		})
		app.defaultPage((ctx, page, config) => {
			page.section(`Page ${config.pageId}`, section => {
				section.deviceSetting('contactSensor')
					.capabilities(['contactSensor'])
					.required(true)
			})
		})

		// Initialize configuration callback
		app.handleMockCallback({
			lifecycle: 'CONFIGURATION',
			executionId: 'e6903fe6-f88f-da69-4c12-e2802606ccbc',
			locale: 'en',
			version: '0.1.0',
			client: {
				os: 'ios',
				version: '0.0.0',
				language: 'en-US'
			},
			configurationData: {
				installedAppId: '7d7fa36d-0ad9-4893-985c-6b75858e38e4',
				phase: 'INITIALIZE',
				pageId: '',
				previousPageId: '',
				config: {}
			},
			settings: {}
		}).then(initResponse => {
			const expectedInitResponse = {initialize: {
				id: 'xxx',
				firstPageId: 'eaMainPage',
				permissions: [],
				disableCustomDisplayName: false,
				disableRemoveApp: false
			}}

			assert.deepStrictEqual(initResponse.configurationData, expectedInitResponse)
		})

		// Page configuration callback
		app.handleMockCallback({
			lifecycle: 'CONFIGURATION',
			executionId: 'abcf6e72-60f4-1f27-341b-449ad9e2192e',
			locale: 'en',
			version: '0.1.0',
			client: {
				os: 'ios',
				version: '0.0.0',
				language: 'fr'
			},
			configurationData: {
				installedAppId: '702d6539-cde1-4baf-9336-10110a0fd000',
				phase: 'PAGE',
				pageId: 'eaAnotherPage',
				previousPageId: '',
				config: {}
			},
			settings: {}
		}).then(pageResponse => {
			const expectedPageResponse = {
				page: {
					name: 'pages.eaAnotherPage.name',
					complete: true,
					pageId: 'eaAnotherPage',
					nextPageId: null,
					previousPageId: null,
					sections: [
						{
							name: 'Page eaAnotherPage',
							settings: [
								{
									id: 'contactSensor',
									name: 'pages.eaAnotherPage.settings.contactSensor.name',
									required: true,
									type: 'DEVICE',
									description: 'Tap to set',
									multiple: false,
									capabilities: [
										'contactSensor'
									],
									permissions: [
										'r'
									]
								}
							]
						}
					]
				}
			}
			assert.deepStrictEqual(pageResponse.configurationData, expectedPageResponse)
		})

		// Default page handler configuration callback
		app.handleMockCallback({
			lifecycle: 'CONFIGURATION',
			executionId: 'abcf6e72-60f4-1f27-341b-449ad9e2192e',
			locale: 'en',
			version: '0.1.0',
			client: {
				os: 'ios',
				version: '0.0.0',
				language: 'fr'
			},
			configurationData: {
				installedAppId: '702d6539-cde1-4baf-9336-10110a0fd000',
				phase: 'PAGE',
				pageId: '',
				previousPageId: '',
				config: {}
			},
			settings: {}
		}).then(pageResponse => {
			const expectedPageResponse = {
				page: {
					name: 'pages.eaMainPage.name',
					complete: true,
					pageId: 'eaMainPage',
					nextPageId: null,
					previousPageId: null,
					sections: [
						{
							name: 'whenDoorOpensAndCloses',
							settings: [
								{
									id: 'contactSensor',
									name: 'pages.eaMainPage.settings.contactSensor.name',
									required: true,
									type: 'DEVICE',
									description: 'Tap to set',
									multiple: false,
									capabilities: [
										'contactSensor'
									],
									permissions: [
										'r'
									]
								}
							]
						},
						{
							name: 'turnLightsOnAndOff',
							settings: [
								{
									id: 'lights',
									name: 'pages.eaMainPage.settings.lights.name',
									required: true,
									type: 'DEVICE',
									description: 'Tap to set',
									multiple: true,
									capabilities: [
										'switch'
									],
									permissions: [
										'r',
										'x'
									]
								}
							]
						}
					]
				}
			}
			assert.deepStrictEqual(pageResponse.configurationData, expectedPageResponse)
		})
	})

	it('should configure event logger', () => {
		const app = new SmartApp()
		app.appId('xxx')
		app.enableEventLogging(4)
		app.page('eaMainPage', (ctx, page) => {
			page.section('whenDoorOpensAndCloses', section => {
				section.deviceSetting('contactSensor')
					.capabilities(['contactSensor'])
			})
		})

		app.handleMockCallback({
			lifecycle: 'CONFIGURATION',
			executionId: 'e6903fe6-f88f-da69-4c12-e2802606ccbc',
			locale: 'en',
			version: '0.1.0',
			client: {
				os: 'ios',
				version: '0.0.0',
				language: 'en-US'
			},
			configurationData: {
				installedAppId: '7d7fa36d-0ad9-4893-985c-6b75858e38e4',
				phase: 'INITIALIZE',
				pageId: '',
				previousPageId: '',
				config: {}
			},
			settings: {}
		})
	})

	it('default page handler', () => {
		const app = new SmartApp()

		app.handleMockCallback({
			lifecycle: 'CONFIGURATION',
			executionId: 'abcf6e72-60f4-1f27-341b-449ad9e2192e',
			locale: 'en',
			version: '0.1.0',
			client: {
				os: 'ios',
				version: '0.0.0',
				language: 'fr'
			},
			configurationData: {
				installedAppId: '702d6539-cde1-4baf-9336-10110a0fd000',
				phase: 'PAGE',
				pageId: 'mainPage',
				previousPageId: '',
				config: {}
			},
			settings: {}
		}).then(pageResponse => {
			const expectedPageResponse = {
				page: {
					name: 'System Error!',
					complete: true,
					pageId: 'mainPage',
					nextPageId: null,
					previousPageId: null,
					sections: [
						{
							name: 'Configuration Page Error',
							settings: [
								{
									id: 'undefined_handler',
									required: false,
									name: 'Page Handler Missing',
									type: 'PARAGRAPH',
									description: 'No handler found for page \'mainPage\''
								}
							]
						}
					]
				}
			}
			assert.deepStrictEqual(pageResponse.configurationData, expectedPageResponse)
		})
	})

	it('default page handler without pageId', () => {
		const app = new SmartApp()

		app.handleMockCallback({
			lifecycle: 'CONFIGURATION',
			executionId: 'abcf6e72-60f4-1f27-341b-449ad9e2192e',
			locale: 'en',
			version: '0.1.0',
			client: {
				os: 'ios',
				version: '0.0.0',
				language: 'fr'
			},
			configurationData: {
				installedAppId: '702d6539-cde1-4baf-9336-10110a0fd000',
				phase: 'PAGE',
				pageId: '',
				previousPageId: '',
				config: {}
			},
			settings: {}
		}).then(pageResponse => {
			const expectedPageResponse = {
				page: {
					name: 'System Error!',
					complete: true,
					pageId: undefined,
					nextPageId: null,
					previousPageId: null,
					sections: [
						{
							name: 'Configuration Page Error',
							settings: [
								{
									id: 'undefined_handler',
									required: false,
									name: 'Page Handler Missing',
									type: 'PARAGRAPH',
									description: 'No page handlers were found'
								}
							]
						}
					]
				}
			}
			assert.deepStrictEqual(pageResponse.configurationData, expectedPageResponse)
		})
	})
})
