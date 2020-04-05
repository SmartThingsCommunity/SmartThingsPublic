'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class SecuritySetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'ENUM'
		this._description = 'Tap to set'
		this._options = [
			{id: 'DISARMED', name: 'Disarmed'},
			{id: 'ARMED_STAY', name: 'Armed/Stay'},
			{id: 'ARMED_AWAY', name: 'Armed/Away'}
		]
	}

	/**
	 * Indicates if this security setting can have multiple values.
	 *
	 * @param {Boolean} value
	 * @default false
	 * @returns {SecuritySetting} Security Setting instance
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
	 * @returns {SecuritySetting} Security Setting instance
	 */
	closeOnSelection(value) {
		this._closeOnSelection = value
		return this
	}

	/**
	 * Indicates if this input should refresh configs after a change in value.
	 *
	 * @param {Boolean} value
	 * @returns {SecuritySetting} Security Setting instance
	 */
	submitOnChange(value) {
		this._submitOnChange = value
		return this
	}

	/**
	 * Style of the setting.
	 *
	 * @param {('COMPLETE','ERROR','DEFAULT','DROPDOWN')} value
	 * @returns {SecuritySetting} Security Setting instance
	 */
	style(value) {
		this._style = value
		return this
	}

	toJson() {
		const result = super.toJson()
		if (this._multiple) {
			result.multiple = this._multiple
		}

		if (this._closeOnSelection) {
			result.closeOnSelection = this._closeOnSelection
		}

		if (this._options) {
			result.options = this._options
		}

		if (this._submitOnChange) {
			result.submitOnChange = this._submitOnChange
		}

		if (this._style) {
			result.style = this._style
		}

		return result
	}
}
