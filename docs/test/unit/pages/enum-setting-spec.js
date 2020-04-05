/* eslint no-undef: "off" */
const path = require('path')
const i18n = require('i18n')
const {expect} = require('chai')
const Page = require('../../../lib/pages/page')
const Section = require('../../../lib/pages/section')
const EnumSetting = require('../../../lib/pages/enum-setting')

i18n.configure({
	locales: ['en', 'fr'],
	directory: path.join(__dirname, '../locales')
})
describe('boolean-setting', () => {
	let page = {}
	let section = {}

	beforeEach(() => {
		page = new Page('enumTestPage', 'fr')
		section = new Section(page, 'testSection')
	})

	it('should set type to ENUM', () => {
		const setting = new EnumSetting(section, 'testSetting')
		const json = setting.toJson()
		expect(json.type).to.equal('ENUM')
	})

	it('should translate name', () => {
		const setting = new EnumSetting(section, 'testSetting')
		const json = setting.toJson()
		expect(json.name).to.equal('Test d\'énumération')
	})

	it('should translate options', () => {
		const setting = new EnumSetting(section, 'testSetting')
		setting.options(['red'])
		const json = setting.toJson()
		expect(json.options[0].name).to.equal('Rouge')
	})

	it('should not translate options when disabled', () => {
		const setting = new EnumSetting(section, 'testSetting')
		setting.translateOptions(false)
		setting.options(['red'])
		const json = setting.toJson()
		expect(json.options[0].name).to.equal('red')
	})
})
