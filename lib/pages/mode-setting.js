'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class ModeSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'MODE'
		this._description = 'Tap to set'
	}

	/**
	 * Indicates if this mode setting can have multiple values.
	 *
	 * @param {Boolean} value
	 * @default false
	 * @returns {ModeSetting} Mode Setting instance
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
	 * @returns {ModeSetting} Mode Setting instance
	 */
	closeOnSelection(value) {
		this._closeOnSelection = value
		return this
	}

	/**
	 * Indicates if this input should refresh configs after a change in value.
	 *
	 * @param {Boolean} value
	 * @default false
	 * @returns {ModeSetting} Mode Setting instance
	 */
	submitOnChange(value) {
		this._submitOnChange = value
		return this
	}

	/**
	 * Style of the setting.
	 *
	 * @param {('COMPLETE', 'ERROR', 'DEFAULT')} value
	 * @returns {ModeSetting} Mode Setting instance
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

		if (this._submitOnChange) {
			result.submitOnChange = this._submitOnChange
		}

		if (this._style) {
			result.style = this._style
		}

		return result
	}
}
