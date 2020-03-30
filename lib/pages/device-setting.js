'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class DeviceSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'DEVICE'
		this._description = 'Tap to set'
		this._multiple = false
	}

	/**
	 * Indicates if this device setting can have multiple values.
	 *
	 * @param {Boolean} value
	 * @default false
	 * @returns {DeviceSetting} Device Setting instance
	 */
	multiple(value) {
		this._multiple = value
		return this
	}

	/**
	 * Indicates if this input should close on selection.
	 *
	 * @param {Boolean} value
	 * @default true
	 * @returns {DeviceSetting} Device Setting instance
	 */
	closeOnSelection(value) {
		this._closeOnSelection = value
		return this
	}

	/**
	 * Indicates if the first device in the list of options should be pre selected.
	 *
	 * @param {Boolean} value
	 * @default true
	 * @returns {DeviceSetting} Device Setting instance
	 */
	preselect(value) {
		this._preselect = value
		return this
	}

	/**
	 * An array of required capabilities for the device(s) options.
	 *
	 * @param {Array.<String>} items Each string has a max length of 128 characters
	 * @returns {DeviceSetting} Device Setting instance
	 */
	capabilities(items) {
		this._capabilities = items
		return this
	}

	/**
	 * Set a singular required capabilities for the device(s) options.
	 *
	 * @param {String} capability Capability string has a max length of 128 characters
	 * @returns {DeviceSetting} Device Setting instance
	 */
	capability(capability) {
		this._capabilities = [capability]
		return this
	}

	/**
	 * The excluded capabilities for the device(s) options.
	 *
	 * @param {String|Array.<String>} capabilities Each string has a max length of 128 characters
	 * @returns {DeviceSetting} Device Setting instance
	 */
	excludeCapabilities(capabilities) {
		this._excludeCapabilities = typeof capabilities === 'string' ?
			capabilities.split(' ') :
			capabilities
		return this
	}

	/**
	 * Set a singular excluded capability for the device(s) options.
	 *
	 * @param {String} value Capability string has a max length of 128 characters
	 * @returns {DeviceSetting} Device Setting instance
	 */
	excludeCapability(value) {
		// TODO -- need to be able to OR capabilities
		this._excludeCapabilities = [value]
		return this
	}

	/**
	 * The required permissions for the selected device(s).
	 *
	 * @param {Array.<String> | String} permissions Accepts a single string permission or an array
	 * @returns {DeviceSetting} Device Setting instance
	 * @example
	 * // Separated with no delimiter
	 * section.deviceSetting().permissions('rwx')
	 *
	 * @example
	 * // Separated with space delimiter
	 * section.deviceSetting().permissions('r w x')
	 *
	 * @example
	 * // Separated into an array
	 * section.deviceSetting().permissions(['r', 'w', 'x'])
	 */
	permissions(permissions) {
		this._permissions = typeof permissions === 'string' ? permissions.match(/[rwx]/ig) : permissions
		return this
	}

	toJson() {
		const result = super.toJson()
		result.multiple = this._multiple

		if (this._closeOnSelection) {
			result.closeOnSelection = this._closeOnSelection
		}

		if (this._submitOnChange) {
			result.submitOnChange = this._submitOnChange
		}

		if (this._preselect) {
			result.preselect = this._preselect
		}

		if (this._capabilities) {
			result.capabilities = this._capabilities
		}

		if (this._excludeCapabilities) {
			result.excludeCapabilities = this._excludeCapabilities
		}

		if (this._permissions) {
			result.permissions = this._permissions
		} else {
			result.permissions = ['r']
		}

		if (this._style) {
			result.style = this._style
		}

		return result
	}
}
