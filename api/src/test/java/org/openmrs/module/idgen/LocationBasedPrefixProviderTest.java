package org.openmrs.module.idgen;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.hamcrest.core.Is.is;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class LocationBasedPrefixProviderTest {
	
	PrefixProvider locationPrefixProvider;
	UserContext userContext;
	
	@Before
	public void setup() {
		locationPrefixProvider = new LocationBasedPrefixProvider();
		mockStatic(Context.class);
		userContext = mock(UserContext.class);
		when(Context.getUserContext()).thenReturn(userContext);
	}
	
	@Test
	public void getValue_shouldReturnPrefixDependingOnLocationInUserContext() {
		when(userContext.getLocation()).thenReturn(createLocationTree());
		Assert.assertThat(locationPrefixProvider.getValue(), is("REGD-"));
		// Change location
		when(userContext.getLocation()).thenReturn(createLocationTree().getParentLocation());
		Assert.assertThat(locationPrefixProvider.getValue(), is("AFDEL-"));
		// replay
		when(userContext.getLocation()).thenReturn(createLocationTree().getParentLocation().getParentLocation());
		Assert.assertThat(locationPrefixProvider.getValue(), is("KSUB-"));
	}
	
	private Location createLocationTree() {
		LocationAttributeType prefixAttrType = new LocationAttributeType();
		prefixAttrType.setName(IdgenConstants.PREFIX_LOCATION_ATTRIBUTE_TYPE);
		prefixAttrType.setMinOccurs(0);
		prefixAttrType.setMaxOccurs(3);
		prefixAttrType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		
		Location mainReg = new Location();
		mainReg.setName("Main Registration");
		
		LocationAttribute mainRegPrefixAtt = new LocationAttribute();
		mainRegPrefixAtt.setAttributeType(prefixAttrType);
		mainRegPrefixAtt.setValue("REGD-");
		mainReg.addAttribute(mainRegPrefixAtt);
		
		Location kch = new Location();
		kch.setName("Kaboul Central Hospital");
		kch.addChildLocation(mainReg);
		
		LocationAttribute kchPrefixAtt = new LocationAttribute();
		kchPrefixAtt.setAttributeType(prefixAttrType);
		kchPrefixAtt.setValue("AFDEL-");
		kch.addAttribute(kchPrefixAtt);
		
		Location ks = new Location();
		ks.setName("Kaboul Subdelegation");
		ks.addChildLocation(kch);
		
		LocationAttribute ksPrefixAtt = new LocationAttribute();
		ksPrefixAtt.setAttributeType(prefixAttrType);
		ksPrefixAtt.setValue("KSUB-");
		ks.addAttribute(ksPrefixAtt);
		
		Location af = new Location();
		af.setName("Afghanistan Delegation");
		af.addChildLocation(ks);
		return mainReg;
	}

}
