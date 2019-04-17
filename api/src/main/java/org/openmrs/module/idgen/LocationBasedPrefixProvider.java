package org.openmrs.module.idgen;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

public class LocationBasedPrefixProvider implements PrefixProvider {

	@Override
	public String getValue() {
		if (Context.getUserContext().getLocation() != null) {
			return getLocationPrefix(Context.getUserContext().getLocation());
		} else {
			throw new RuntimeException("No Location found in current UserContext");
		}
		
	}

	/**
	 * Convenience method for getting a valid prefix from a parent location with a valid prefix attribute up the tree
	 * 
	 * @param location The starting point location
	 * @return prefix
	 */
	public String getLocationPrefix(Location location) {
		if (location != null) {
			for (Object ob : location.getActiveAttributes().toArray()) {
				LocationAttribute att = (LocationAttribute) ob;
				if (att.getAttributeType().getName().equalsIgnoreCase(IdgenConstants.PREFIX_LOCATION_ATTRIBUTE_TYPE)) {
					String prefix = (String) att.getValue();
					if (StringUtils.isNotBlank(prefix)) {
						return prefix;
					}
				}
			}
		} else {
			// This means we either reached the top of the location without a valid prefix found or there is no parent Location 
			// up the tree with a prefix set
			throw new APIException("No location prefix could be found up the location tree.");
		}
		return getLocationPrefix(location.getParentLocation());
	}
}
