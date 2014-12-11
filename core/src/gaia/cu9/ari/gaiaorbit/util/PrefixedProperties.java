package gaia.cu9.ari.gaiaorbit.util;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Selects the subset of properties from a {@link Properties} object
 * that have the same prefix and exposes them.
 * @author Toni Sagrista
 *
 */
public class PrefixedProperties extends Properties {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PrefixedProperties(Properties props, String prefix) {
	if (props == null) {
	    return;
	}

	@SuppressWarnings("unchecked")
	Enumeration<String> en = (Enumeration<String>) props.propertyNames();
	while (en.hasMoreElements()) {
	    String propName = en.nextElement();
	    String propValue = props.getProperty(propName);

	    if (propName.startsWith(prefix)) {
		String key = propName.substring(prefix.length());
		setProperty(key, propValue);
	    }
	}
    }
}
