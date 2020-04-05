'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class ImagesSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'IMAGES'
	}

	/**
	 * Configure your image array sources
	 *
	 * @param {Array.<String>} sources The images to display. HTTPS urls. Max length 2048 characters.
	 * @returns {ImagesSetting} ImagesSetting instance
	 */
	images(sources) {
		this._images = sources
		return this
	}

	toJson() {
		const result = super.toJson()
		if (this._images) {
			result.images = this._images
		}

		return result
	}
}
