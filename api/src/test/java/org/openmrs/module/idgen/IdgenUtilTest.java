/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.idgen;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.test.Verifies;

public class IdgenUtilTest {
	
	/**
	 * @see {@link IdgenUtil#convertFromBase(String,char[])}
	 */
	@Test
	@Verifies(value = "should convert from string in base character set to long", method = "convertFromBase(String,char[])")
	public void convertFromBase_shouldConvertFromStringInBaseCharacterSetToLong() throws Exception {
		char[] hexChars = "0123456789ABCDEF".toCharArray();
		long numericValue = 43804337214L;
		String hexValue = IdgenUtil.convertToBase(numericValue, hexChars, 0);
		Assert.assertEquals("A32F1243E", hexValue);
		long back = IdgenUtil.convertFromBase(hexValue, hexChars);
		Assert.assertEquals(numericValue, back);	
	}
	
	@Test
	public void getLocationPrefix_shouldPickTheNearestValidPrefixUpTheTree() {
		Assert.assertEquals("KBSD", IdgenUtil.getLocationPrefix(createLocationTree()));
	}
	
	@Test
	public void getLocationPrefix_shouldClimbToTopOfTheTreeAndPickValidPrefixIfOneIsSet() {
		Location registrationDesk = createLocationTree();
		LocationAttribute ksPrefixAtt = registrationDesk.getParentLocation().getParentLocation().getActiveAttributes().iterator().next();
		// Invalidate prefix attribute for a location found in the middle of the tree
		ksPrefixAtt.setValue(" ");
		Assert.assertEquals("AFDEL", IdgenUtil.getLocationPrefix(registrationDesk));
	}
	
	@Test(expected = RuntimeException.class)
	public void getLocationPrefix_throwAnExceptionIfNoValidPrefixIsFound() {
		Location location = createLocationTree();
		// Invalidate valid prefixes
		LocationAttribute ksPrefixAtt = location.getParentLocation().getParentLocation().getActiveAttributes().iterator().next();
		ksPrefixAtt.setValue(" ");
		LocationAttribute afPrefixAtt = location.getParentLocation().getParentLocation().getParentLocation().getActiveAttributes().iterator().next();
		afPrefixAtt.setValue(" ");
		
		IdgenUtil.getLocationPrefix(location);
	}
	
	@Test
	public void getLocationPrefixRecursively_shouldPickThePrefixFromCurrentLocationIfOneIsSet() {
		Location registrationDesk = createLocationTree();
		LocationAttributeType prefixAttrType = createPrefixAttributeType();
		LocationAttribute prefixAtt = new LocationAttribute();
		prefixAtt.setAttributeType(prefixAttrType);
		// Valid prefix 
		prefixAtt.setValue("REGD");
		registrationDesk.addAttribute(prefixAtt);
		Assert.assertEquals("REGD", IdgenUtil.getLocationPrefix(registrationDesk));
	}
	
	private Location createLocationTree() {
		Location mainReg = new Location();
		mainReg.setName("Registration Desk");
		
		Location kch = new Location();
		kch.setName("Kaboul Central Hospital");
		kch.addChildLocation(mainReg);
		
		LocationAttributeType prefixAttrType = createPrefixAttributeType();
		LocationAttribute kchPrefixAtt = new LocationAttribute();
		kchPrefixAtt.setAttributeType(prefixAttrType);
		// Invalid prefix 
		kchPrefixAtt.setValue(" ");
		kch.addAttribute(kchPrefixAtt);
		
		Location ks = new Location();
		ks.setName("Kaboul Subdelegation");
		ks.addChildLocation(kch);
		
		LocationAttribute ksPrefixAtt = new LocationAttribute();
		ksPrefixAtt.setAttributeType(prefixAttrType);
		// Valid prefix 
		ksPrefixAtt.setValue("KBSD");
		ks.addAttribute(ksPrefixAtt);
		
		Location af = new Location();
		af.setName("Afghanistan Delegation");
		af.addChildLocation(ks);
		
		LocationAttribute afPrefixAtt = new LocationAttribute();
		afPrefixAtt.setAttributeType(prefixAttrType);
		afPrefixAtt.setValue("AFDEL");
		af.addAttribute(afPrefixAtt);
		return mainReg;
	}
	
	private LocationAttributeType createPrefixAttributeType() {
		LocationAttributeType prefixAttrType = new LocationAttributeType();
		prefixAttrType.setName(IdgenConstants.PREFIX_LOCATION_ATTRIBUTE_TYPE);
		prefixAttrType.setMinOccurs(0);
		prefixAttrType.setMaxOccurs(5);
		prefixAttrType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		return prefixAttrType;
	}
}

