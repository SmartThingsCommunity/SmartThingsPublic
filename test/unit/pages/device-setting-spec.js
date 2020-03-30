/* eslint no-undef: "off" */
const {expect} = require('chai')
const Page = require('../../../lib/pages/page')
const Section = require('../../../lib/pages/section')
const DeviceSetting = require('../../../lib/pages/device-setting')

describe('device-setting', () => {
	let page = {}
	let section = {}
	const expected = {
		// Super
		id: 'testSetting',
		name: 'myTestDeviceSettingName',
		description: 'myTestDviceSettingDesc',
		disabled: true,
		required: true,
		defaultValue: 'defaultValue',
		//  End Super
		submitOnChange: true,
		multiple: true,
		closeOnSelection: true,
		preselect: true,
		capabilities: ['switch'],
		excludeCapabilities: ['light'],
		permissions: 'rwx'
	}

	beforeEach(() => {
		page = new Page('testPage')
		section = new Section(page, 'testSection')
	})

	it('should set `multiple`', () => {
		const setting = new DeviceSetting(section, 'testDevice')
		expect(setting.toJson()).to.have.ownProperty('multiple')
		expect(setting.toJson().multiple).to.equal(false)
	})

	it('should set `closeOnSelection`', () => {
		const setting = new DeviceSetting(section, expected.id)
		setting.closeOnSelection(expected.closeOnSelection)
		expect(setting.toJson()).to.have.ownProperty('closeOnSelection')
		expect(setting.toJson().closeOnSelection).to.equal(expected.closeOnSelection)
	})

	it('should set `preselect`', () => {
		const setting = new DeviceSetting(section, expected.id)
		setting.preselect(expected.preselect)
		expect(setting.toJson()).to.have.ownProperty('preselect')
		expect(setting.toJson().preselect).to.equal(expected.preselect)
	})

	it('should set `capabilities`', () => {
		const setting = new DeviceSetting(section, expected.id)
		setting.capabilities(expected.capabilities)
		expect(setting.toJson()).to.have.ownProperty('capabilities')
		expect(setting.toJson().capabilities).to.have.members(expected.capabilities)
	})

	it('should set `capability`', () => {
		const setting = new DeviceSetting(section, expected.id)
		setting.capability(expected.capabilities[0])
		expect(setting.toJson()).to.have.ownProperty('capabilities')
		expect(setting.toJson().capabilities).to.have.members(expected.capabilities)
	})

	it('should set `excludeCapabilities`', () => {
		const setting = new DeviceSetting(section, expected.id)
		setting.excludeCapabilities(expected.excludeCapabilities)
		expect(setting.toJson()).to.have.ownProperty('excludeCapabilities')
		expect(setting.toJson().excludeCapabilities).to.have.members(expected.excludeCapabilities)
	})

	it('should set `excludeCapability`', () => {
		const setting = new DeviceSetting(section, expected.id)
		setting.excludeCapability(expected.excludeCapabilities[0])
		expect(setting.toJson()).to.have.ownProperty('excludeCapabilities')
		expect(setting.toJson().excludeCapabilities).to.have.members(expected.excludeCapabilities)
	})

	it('should set `super.submitOnChange`', () => {
		const setting = new DeviceSetting(section, expected.id)
		setting.submitOnChange(expected.submitOnChange)
		expect(setting.toJson()).to.have.ownProperty('submitOnChange')
		expect(setting.toJson().submitOnChange).to.equal(expected.submitOnChange)
	})

	it('should set `permissions` correctly with `rwx`', () => {
		const setting = new DeviceSetting(section, expected.id)
		setting.permissions(expected.permissions)
		expect(setting.toJson()).to.have.ownProperty('permissions')
		expect(setting.toJson().permissions).to.have.members(['r', 'w', 'x'])
	})

	it('should set `permissions` correctly with `r`', () => {
		const setting = new DeviceSetting(section, expected.id)
		setting.permissions(expected.permissions[0])
		expect(setting.toJson()).to.have.ownProperty('permissions')
		expect(setting.toJson().permissions).to.have.members(['r'])
	})

	it('should set `permissions` correctly with `[r,w,x]`', () => {
		const setting = new DeviceSetting(section, expected.id)
		setting.permissions(expected.permissions.split(''))
		expect(setting.toJson()).to.have.ownProperty('permissions')
		expect(setting.toJson().permissions).to.have.members(['r', 'w', 'x'])
	})

	it('should default `permissions` to [r]', () => {
		const setting = new DeviceSetting(section, expected.id)
		expect(setting.toJson()).to.have.ownProperty('permissions')
		expect(setting.toJson().permissions).to.have.members(['r'])
	})

	it('`toJson()` to return super properties at least', () => {
		const setting = new DeviceSetting(section, expected.id)
		expect(setting.toJson()).to.have.ownProperty('id')
		expect(setting.toJson()).to.have.ownProperty('name')
		expect(setting.toJson()).to.have.ownProperty('description')
		expect(setting.toJson()).to.have.ownProperty('required')
	})

	it('`toJson()` to return super properties at least', () => {
		const setting = new DeviceSetting(section, expected.id)
		expect(setting.toJson()).to.have.ownProperty('type')
		expect(setting.toJson().type).to.equal('DEVICE')

		expect(setting.toJson()).to.have.ownProperty('description')
		expect(setting.toJson().description).to.equal('Tap to set')

		expect(setting.toJson()).to.have.ownProperty('multiple')
		expect(setting.toJson().multiple).to.equal(false)
	})
})
