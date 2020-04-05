/* eslint no-undef: "off" */
const {expect} = require('chai')
const Page = require('../../../lib/pages/page')
const Section = require('../../../lib/pages/section')
const BooleanSetting = require('../../../lib/pages/boolean-setting')

describe('boolean-setting', () => {
	let page = {}
	let section = {}

	beforeEach(() => {
		page = new Page('testPage')
		section = new Section(page, 'testSection')
	})

	it('should set type to BOOLEAN', () => {
		const setting = new BooleanSetting(section, 'testSetting')
		setting.name('testBoolean')
		const json = setting.toJson()
		expect(json.type).to.equal('BOOLEAN')
		expect(json.name).to.equal('testBoolean')
	})

	it('should set an image url when set', () => {
		const setting = new BooleanSetting(section, 'id')
		setting.image('https://example.com/image.png')
		const json = setting.toJson()
		expect(json.image).to.equal('https://example.com/image.png')
	})

	it('should not include image in json when unset', () => {
		const setting = new BooleanSetting(section, 'id')
		const json = setting.toJson()
		expect(json).to.not.have.property('image')
	})
})
