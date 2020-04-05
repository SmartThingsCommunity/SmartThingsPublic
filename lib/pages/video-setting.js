'use strict'

const SectionSetting = require('./section-setting.js')

module.exports = class VideoSetting extends SectionSetting {
	constructor(section, id) {
		super(section, id)
		this._type = 'VIDEO'
	}

	/**
	 * Configure your video source
	 *
	 * @param {String} source HTTPS url. Max length 2048 characters.
	 * @returns {VideoSetting} Video Setting instance
	 */
	video(source) {
		this._video = source
		return this
	}

	/**
	 * Configure your video source
	 *
	 * @param {String} source HTTPS url. Max length 2048 characters.
	 * @returns {VideoSetting} Video Setting instance
	 */
	image(source) {
		this._image = source
		return this
	}

	toJson() {
		const result = super.toJson()
		if (this._video) {
			result.video = this._video
		}

		if (this._image) {
			result.image = this._image
		}

		return result
	}
}
