package com.javacc.ls.settings;

public class JavaCCCodeLensSettings {

	private boolean enabled = true;

	/**
	 * Returns true if codelens service is enabled and false otherwise.
	 * 
	 * @return true if codelens service is enabled and false otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set true if codelens service is enabled and false otherwise.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
