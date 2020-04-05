/* eslint no-undef: "off" */
const {expect} = require('chai')
const Page = require('../../lib/pages/page')

describe('pagebuilder', () => {
	it('should set page ID', () => {
		const page = new Page('mainPage')
		page.name('Page Builder Spec')
		const json = page.toJson()
		expect(json.pageId).to.equal('mainPage')
		expect(json.name).to.equal('Page Builder Spec')
	})

	it('should process one section', () => {
		const page = new Page('mainPage')
		page.defaultRequired(true)
		page.section('When this door opens and closes', section => {
			section.deviceSetting('contactSensor')
				.capabilities(['contactSensor'])
				.name('Select an open/close sensor')
		})

		const json = page.toJson()
		// Console.log(JSON.stringify(json, null, 2))
		expect(json.sections.length).to.equal(1)
		expect(json.sections[0].name).to.equal('When this door opens and closes')
		expect(json.sections[0].settings.length).to.equal(1)
		expect(json.sections[0].settings[0].id).to.equal('contactSensor')
		expect(json.sections[0].settings[0].name).to.equal('Select an open/close sensor')
		expect(json.sections[0].settings[0].description).to.equal('Tap to set')
		expect(json.sections[0].settings[0].required).to.equal(true)
		expect(json.sections[0].settings[0].multiple).to.equal(false)
		expect(json.sections[0].settings[0].type).to.equal('DEVICE')
		expect(json.sections[0].settings[0].permissions[0]).to.equal('r')
		expect(json.sections[0].settings[0].capabilities[0]).to.equal('contactSensor')
	})

	it('should process two sections', () => {
		const page = new Page('mainPage')
		page.defaultRequired(true)
		page.section('When this door opens and closes', section => {
			section.deviceSetting('contactSensor')
				.capabilities(['contactSensor'])
				.name('Select an open/close sensor')
		})

		page.section('Turn these lights on and off', section => {
			section.deviceSetting('lights')
				.capabilities(['switch'])
				.name('Select lights')
				.multiple(true)
				.permissions('rx')
		})

		const json = page.toJson()
		// Console.log(JSON.stringify(json, null, 2))
		expect(json.sections.length).to.equal(2)

		expect(json.sections[0].name).to.equal('When this door opens and closes')
		expect(json.sections[0].settings.length).to.equal(1)
		expect(json.sections[0].settings[0].id).to.equal('contactSensor')
		expect(json.sections[0].settings[0].name).to.equal('Select an open/close sensor')
		expect(json.sections[0].settings[0].description).to.equal('Tap to set')
		expect(json.sections[0].settings[0].required).to.equal(true)
		expect(json.sections[0].settings[0].multiple).to.equal(false)
		expect(json.sections[0].settings[0].type).to.equal('DEVICE')
		expect(json.sections[0].settings[0].permissions[0]).to.equal('r')
		expect(json.sections[0].settings[0].capabilities[0]).to.equal('contactSensor')

		expect(json.sections[1].name).to.equal('Turn these lights on and off')
		expect(json.sections[1].settings.length).to.equal(1)
		expect(json.sections[1].settings[0].id).to.equal('lights')
		expect(json.sections[1].settings[0].name).to.equal('Select lights')
		expect(json.sections[0].settings[0].description).to.equal('Tap to set')
		expect(json.sections[1].settings[0].required).to.equal(true)
		expect(json.sections[1].settings[0].multiple).to.equal(true)
		expect(json.sections[1].settings[0].type).to.equal('DEVICE')
		expect(json.sections[1].settings[0].permissions[0]).to.equal('r')
		expect(json.sections[1].settings[0].permissions[1]).to.equal('x')
		expect(json.sections[1].settings[0].capabilities[0]).to.equal('switch')
	})

	it('should process unnamed section', () => {
		const page = new Page('mainPage')
		page.defaultRequired(true)
		page.section(section => {
			section.deviceSetting('contactSensor')
				.capabilities(['contactSensor'])
				.name('Select an open/close sensor')
		})

		const json = page.toJson()
		// Console.log(JSON.stringify(json, null, 2))
		expect(json.sections.length).to.equal(1)
		expect(json.sections[0].name).to.equal(undefined)
		expect(json.sections[0].settings.length).to.equal(1)
		expect(json.sections[0].settings[0].id).to.equal('contactSensor')
		expect(json.sections[0].settings[0].name).to.equal('Select an open/close sensor')
		expect(json.sections[0].settings[0].required).to.equal(true)
		expect(json.sections[0].settings[0].multiple).to.equal(false)
		expect(json.sections[0].settings[0].type).to.equal('DEVICE')
		expect(json.sections[0].settings[0].permissions[0]).to.equal('r')
		expect(json.sections[0].settings[0].capabilities[0]).to.equal('contactSensor')
	})

	it('should detect duplicate setting ID', () => {
		const page = new Page('mainPage')
		let caughtError = false
		page.section('When this door opens and closes', section => {
			section.deviceSetting('sensor')
				.capabilities(['contactSensor'])
				.name('Select an open/close sensor')

			try {
				section.deviceSetting('sensor')
					.capabilities(['motionSensor'])
					.name('Select a motion sensor')
			} catch (error) {
				caughtError = true
			}
		})

		expect(caughtError).to.equal(true)
	})

	it('should honor default required', () => {
		const page = new Page('mainPage')
		page.defaultRequired(true)
		page.section('When this door opens and closes', section => {
			section.deviceSetting('contactSensor')
				.capabilities(['contactSensor'])
				.name('Select an open/close sensor')
			section.deviceSetting('motionSensor')
				.capabilities(['motionSensor'])
				.name('Select an open/close sensor')
				.required(false)
		})

		page.section('Turn these lights on and off', section => {
			section.deviceSetting('lights')
				.capabilities(['switch'])
				.name('Select lights')
				.multiple(true)
				.permissions('rx')
		})

		const json = page.toJson()
		// Console.log(JSON.stringify(json, null, 2))
		expect(json.sections[0].settings[0].required).to.equal(true)
		expect(json.sections[0].settings[1].required).to.equal(false)
		expect(json.sections[1].settings[0].required).to.equal(true)
	})

	it('options formats', () => {
		const page = new Page('mainPage')

		page.section(section => {
			section.enumSetting('standardOptions').options([{id: 'one', name: 'One'}, {id: 'two', name: 'Two'}])
			section.enumSetting('mapOptions').options({red: 'Red', green: 'Green', blue: 'Blue'})
			section.enumSetting('simpleOptions').options(['Vanilla', 'Chocolate', 'Strawberry'])
		})

		const json = page.toJson()
		// Console.log(JSON.stringify(json, null, 2))
		expect(json.sections[0].settings[0].options[0].id).to.equal('one')
		expect(json.sections[0].settings[0].options[0].name).to.equal('One')
		expect(json.sections[0].settings[0].options[1].id).to.equal('two')
		expect(json.sections[0].settings[0].options[1].name).to.equal('Two')

		expect(json.sections[0].settings[1].options[0].id).to.equal('red')
		expect(json.sections[0].settings[1].options[0].name).to.equal('Red')
		expect(json.sections[0].settings[1].options[1].id).to.equal('green')
		expect(json.sections[0].settings[1].options[1].name).to.equal('Green')
		expect(json.sections[0].settings[1].options[2].id).to.equal('blue')
		expect(json.sections[0].settings[1].options[2].name).to.equal('Blue')

		expect(json.sections[0].settings[2].options[0].id).to.equal('Vanilla')
		expect(json.sections[0].settings[2].options[0].name).to.equal('Vanilla')
		expect(json.sections[0].settings[2].options[1].id).to.equal('Chocolate')
		expect(json.sections[0].settings[2].options[1].name).to.equal('Chocolate')
		expect(json.sections[0].settings[2].options[2].id).to.equal('Strawberry')
		expect(json.sections[0].settings[2].options[2].name).to.equal('Strawberry')
	})

	it('groupedOptions formats', () => {
		const page = new Page('mainPage')

		page.section('none', section => {
			section.enumSetting('standardOptions').groupedOptions([
				{name: 'Group One', options: [{id: 'g1one', name: 'One'}, {id: 'g1two', name: 'Two'}]},
				{name: 'Group Two', options: [{id: 'g2one', name: 'One'}, {id: 'g2two', name: 'Two'}]}
			])
			section.enumSetting('mapOptions').groupedOptions({
				Colors: {red: 'Red', green: 'Green', blue: 'Blue'},
				Flavors: {vanilla: 'Vanilla', chocolate: 'Chocolate', strawberry: 'Strawberry'}
			})
			section.enumSetting('simpleOptions').groupedOptions({
				Colors: ['Red', 'Green', 'Blue'],
				Flavors: ['Vanilla', 'Chocolate', 'Strawberry']
			})
		})

		const json = page.toJson()
		// Standard Options - group 1
		expect(json.sections[0].settings[0].groupedOptions[0].name).to.equal('Group One')
		expect(json.sections[0].settings[0].groupedOptions[0].options[0].id).to.equal('g1one')
		expect(json.sections[0].settings[0].groupedOptions[0].options[0].name).to.equal('One')
		expect(json.sections[0].settings[0].groupedOptions[0].options[1].id).to.equal('g1two')
		expect(json.sections[0].settings[0].groupedOptions[0].options[1].name).to.equal('Two')
		// Standard Options - group 2
		expect(json.sections[0].settings[0].groupedOptions[1].name).to.equal('Group Two')
		expect(json.sections[0].settings[0].groupedOptions[1].options[0].id).to.equal('g2one')
		expect(json.sections[0].settings[0].groupedOptions[1].options[0].name).to.equal('One')
		expect(json.sections[0].settings[0].groupedOptions[1].options[1].id).to.equal('g2two')
		expect(json.sections[0].settings[0].groupedOptions[1].options[1].name).to.equal('Two')

		// Map Options - group 1
		expect(json.sections[0].settings[1].groupedOptions[0].name).to.equal('Colors')
		expect(json.sections[0].settings[1].groupedOptions[0].options[0].id).to.equal('red')
		expect(json.sections[0].settings[1].groupedOptions[0].options[0].name).to.equal('Red')
		expect(json.sections[0].settings[1].groupedOptions[0].options[1].id).to.equal('green')
		expect(json.sections[0].settings[1].groupedOptions[0].options[1].name).to.equal('Green')
		expect(json.sections[0].settings[1].groupedOptions[0].options[2].id).to.equal('blue')
		expect(json.sections[0].settings[1].groupedOptions[0].options[2].name).to.equal('Blue')
		// Map Options - group 2
		expect(json.sections[0].settings[1].groupedOptions[1].name).to.equal('Flavors')
		expect(json.sections[0].settings[1].groupedOptions[1].options[0].id).to.equal('vanilla')
		expect(json.sections[0].settings[1].groupedOptions[1].options[0].name).to.equal('Vanilla')
		expect(json.sections[0].settings[1].groupedOptions[1].options[1].id).to.equal('chocolate')
		expect(json.sections[0].settings[1].groupedOptions[1].options[1].name).to.equal('Chocolate')
		expect(json.sections[0].settings[1].groupedOptions[1].options[2].id).to.equal('strawberry')
		expect(json.sections[0].settings[1].groupedOptions[1].options[2].name).to.equal('Strawberry')

		// Simple Options - group 1
		expect(json.sections[0].settings[2].groupedOptions[0].name).to.equal('Colors')
		expect(json.sections[0].settings[2].groupedOptions[0].options[0].id).to.equal('Red')
		expect(json.sections[0].settings[2].groupedOptions[0].options[0].name).to.equal('Red')
		expect(json.sections[0].settings[2].groupedOptions[0].options[1].id).to.equal('Green')
		expect(json.sections[0].settings[2].groupedOptions[0].options[1].name).to.equal('Green')
		expect(json.sections[0].settings[2].groupedOptions[0].options[2].id).to.equal('Blue')
		expect(json.sections[0].settings[2].groupedOptions[0].options[2].name).to.equal('Blue')
		// Simple Options - group 2
		expect(json.sections[0].settings[2].groupedOptions[1].name).to.equal('Flavors')
		expect(json.sections[0].settings[2].groupedOptions[1].options[0].id).to.equal('Vanilla')
		expect(json.sections[0].settings[2].groupedOptions[1].options[0].name).to.equal('Vanilla')
		expect(json.sections[0].settings[2].groupedOptions[1].options[1].id).to.equal('Chocolate')
		expect(json.sections[0].settings[2].groupedOptions[1].options[1].name).to.equal('Chocolate')
		expect(json.sections[0].settings[2].groupedOptions[1].options[2].id).to.equal('Strawberry')
		expect(json.sections[0].settings[2].groupedOptions[1].options[2].name).to.equal('Strawberry')
	})

	it('page setting', () => {
		const page = new Page('mainPage')

		page.section(section => {
			section.pageSetting('anotherPage')
		})

		const json = page.toJson()
		// Console.log(JSON.stringify(json, null, 2))
		expect(json.sections[0].settings[0].id).to.equal('anotherPage')
		expect(json.sections[0].settings[0].page).to.equal('anotherPage')
		expect(json.sections[0].settings[0].required).to.equal(undefined)
	})

	it('page defaultRequired adds required:true', () => {
		const page = new Page('mainPage')
		page.defaultRequired(true)
		page.section(section => {
			section.paragraphSetting('mainParagraph')
		})

		const json = page.toJson()
		// Console.log(JSON.stringify(json, null, 2))
		expect(json.sections[0].settings[0].required).to.equal(true)
	})
})
