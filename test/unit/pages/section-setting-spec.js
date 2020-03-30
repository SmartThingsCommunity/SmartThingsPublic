/* eslint no-undef: "off" */
const {expect} = require('chai')
const Page = require('../../../lib/pages/page')
const Section = require('../../../lib/pages/section')
const SectionSetting = require('../../../lib/pages/section-setting')

describe('section-setting', () => {
	let page = {}
	let section = {}
	let expected = {}

	beforeEach(() => {
		page = new Page('testPage')
		section = new Section(page, 'testSection')

		expected = {
			id: 'testSetting',
			name: 'myTestSectionSettingName',
			description: 'myTestSectionSettingDesc',
			defaultValue: 'defaultValue',
			disabled: true,
			required: true,
			submitOnChange: true
		}
	})

	it('should include `required` property', () => {
		let setting = new SectionSetting(section, 'testSection1')
		expect(setting.toJson()).to.have.ownProperty('required')

		setting = new SectionSetting(section, 'testSection2')
		setting.required(false)
		expect(setting.toJson()).to.have.ownProperty('required')
	})

	it('should default `required` to false', () => {
		const setting = new SectionSetting(section, 'testSection')
		expect(setting.toJson().required).to.equal(false)
	})

	it('should set `defaultValue`', () => {
		const setting = new SectionSetting(section, expected.id)
		setting.defaultValue(expected.defaultValue)
		expect(setting.toJson().defaultValue).to.equal(expected.defaultValue)
	})

	it('should set `submitOnChange`', () => {
		const setting = new SectionSetting(section, expected.id)
		setting.submitOnChange(expected.submitOnChange)
		expect(setting.toJson().submitOnChange).to.equal(expected.submitOnChange)
	})

	it('should omit `submitOnChange` by default', () => {
		const setting = new SectionSetting(section, expected.id)
		expect(setting.toJson()).to.not.have.ownProperty('submitOnChange')
	})

	it('should set `disabled`', () => {
		const setting = new SectionSetting(section, expected.id)
		setting.disabled(expected.disabled)
		expect(setting.toJson().disabled).to.equal(expected.disabled)
	})

	it('should omit `disabled` by default', () => {
		const setting = new SectionSetting(section, expected.id)
		expect(setting.toJson()).to.not.have.ownProperty('disabled')
	})

	it('should set name to a string value', () => {
		const setting = new SectionSetting(section, 'testSection')
		setting.name(() => expected.name)
		expect(setting.toJson()).to.have.ownProperty('name')
		expect(setting.toJson()).to.have.property('name', expected.name)
	})

	it('should default name to an i18n key', () => {
		const setting = new SectionSetting(section, 'testSection')
		expect(setting.toJson()).to.have.ownProperty('name')
		expect(setting.toJson()).to.have.property('name', 'pages.testPage.settings.testSection.name')
	})

	it('should set description to a string value', () => {
		const setting = new SectionSetting(section, 'testSection')
		setting.description(() => expected.description)
		expect(setting.toJson()).to.have.ownProperty('description')
		expect(setting.toJson()).to.have.property('description', expected.description)
	})

	it('should default description to an i18n key', () => {
		const setting = new SectionSetting(section, 'testSection')
		expect(setting.toJson()).to.have.ownProperty('description')
		expect(setting.toJson()).to.have.property('description', 'pages.testPage.settings.testSection.description')
	})
})
