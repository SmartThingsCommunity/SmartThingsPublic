'use strict'

module.exports = class SectionSetting {
	constructor(section, id) {
		this._section = section
		this._id = id
		this._name = this.i18nKey('name')
		this._description = this.i18nKey('description')
		this._required = section._defaultRequired
		if (section._page._settingIds.includes(id)) {
			throw new Error(`Setting ID '${id}' has already been used.`)
		} else if (id) {
			section._page._settingIds.push(id)
		}

		this._translateDefaultValue = false
	}

	name(value) {
		this._name = value
		return this
	}

	description(value) {
		if (value === undefined || value === null) {
			this._description = ''
		} else {
			this._description = value
		}

		return this
	}

	translateDefaultValue(value) {
		this._translateDefaultValue = value
		if (value === true) {
			this._defaultValue = this.i18nKey('defaultValue')
		}

		return this
	}

	defaultValue(value) {
		this._defaultValue = String(value)
		return this
	}

	required(value) {
		this._required = value
		return this
	}

	disabled(value) {
		this._disabled = value
		return this
	}

	submitOnChange(value) {
		this._submitOnChange = value
		return this
	}

	i18nKey(property) {
		return this._section._page.i18nKey(`settings.${this._id}.${property}`)
	}

	translate(...args) {
		return this._section._page.translate(...args)
	}

	toJson() {
		const result = {
			id: this._id,
			required: this._required,
			type: this._type
		}

		if (this._name instanceof Function) {
			result.name = this._name()
		} else if (typeof (this._name) === 'string' || this._name instanceof String) {
			result.name = this.translate(this._name)
		}

		if (this._description instanceof Function) {
			result.description = this._description()
		} else if (typeof (this._name) === 'string' || this._description instanceof String) {
			result.description = this.translate(this._description)
		}

		if (this._translateDefaultValue && (typeof (this._defaultValue) === 'string' || this._defaultValue instanceof String)) {
			result.defaultValue = this.translate(this._defaultValue)
		} else if (this._defaultValue !== undefined) {
			result.defaultValue = this._defaultValue
		}

		if (this._disabled) {
			result.disabled = this._disabled
		}

		if (this._submitOnChange) {
			result.submitOnChange = this._submitOnChange
		}

		return result
	}
}
