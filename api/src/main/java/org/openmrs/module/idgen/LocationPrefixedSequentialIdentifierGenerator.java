package org.openmrs.module.idgen;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.context.Context;

public class LocationPrefixedSequentialIdentifierGenerator extends SequentialIdentifierGenerator {

	private final String DEFAULT_LOCATION_PREFIXED_IDENTIFIER_FORMAT = "[A-Z]{3,5}\\-[0]{3}\\-[0-9]{3,6}";  
	
	@Override
    public String getIdentifierForSeed(long seed) {
    	
		if (Context.getUserContext().getLocation() != null) {
			setPrefix(IdgenUtil.getLocationPrefixRecursively(Context.getUserContext().getLocation()));
		} else {
			throw new RuntimeException("No Location found in current UserContext");
		}
    	// Convert the next sequence integer into a String with the appropriate Base characters
		int seqLength = firstIdentifierBase == null ? 1 : firstIdentifierBase.length();

		String identifier = IdgenUtil.convertToBase(seed, baseCharacterSet.toCharArray(), seqLength);

		identifier = getPrefix()  + identifier;
		
		setPrefix(null); // ensure a stateless prefix behavior
		
    	if (getIdentifierType() != null && StringUtils.isNotEmpty(getIdentifierType().getFormat())) {
    		if (!identifier.matches(getIdentifierType().getFormat())) {
    			throw new InvalidIdentifierFormatException("Identifier " + identifier + " does not match : " + getIdentifierType().getFormat());
    		}
    	} else {
    		if (!identifier.matches(DEFAULT_LOCATION_PREFIXED_IDENTIFIER_FORMAT)) {
    			throw new InvalidIdentifierFormatException("Identifier " + identifier + " does not match : " + DEFAULT_LOCATION_PREFIXED_IDENTIFIER_FORMAT);
    		}
    	}
    	
		if (this.minLength != null && this.minLength > 0) {
			if (identifier.length() < this.minLength) {
				throw new RuntimeException("Invalid configuration for IdentifierSource. Length minimum set to " + this.minLength + " but generated " + identifier);
			}
		}

		if (this.maxLength != null && this.maxLength > 0) {
			if (identifier.length() > this.maxLength) {
				throw new RuntimeException("Invalid configuration for IdentifierSource. Length maximum set to " + this.maxLength + " but generated " + identifier);
			}
		}

    	return identifier;
    }

}
