package org.openmrs.module.idgen;

import org.openmrs.api.context.Context;

public class LocationBasedPrefixProvider implements PrefixProvider {

	@Override
	public String getValue() {
		if (Context.getUserContext().getLocation() != null) {
			return IdgenUtil.getLocationPrefix(Context.getUserContext().getLocation());
		} else {
			throw new RuntimeException("No Location found in current UserContext");
		}
		
	}

}
