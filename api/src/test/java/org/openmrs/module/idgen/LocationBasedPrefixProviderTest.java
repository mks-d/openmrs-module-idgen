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
	
	LocationBasedPrefixProvider locationPrefixProvider;
	UserContext userContext;
	// user's current location
	Location location1;
	// Locations with prefix attribute
	Location location2;
	Location location4;
	Location location7;
	
	@Before
	public void setup() {
		locationPrefixProvider = new LocationBasedPrefixProvider();
		createLocationTree();
		mockStatic(Context.class);
		userContext = mock(UserContext.class);
		when(Context.getUserContext()).thenReturn(userContext);
	}
	
	@Test
	public void getValue_shouldReturnPrefixDependingOnLocationInUserContext() {
		when(userContext.getLocation()).thenReturn(location1);
		Assert.assertThat(locationPrefixProvider.getValue(), is("REGD-"));
		// Change to location 3
		when(userContext.getLocation()).thenReturn(location2.getParentLocation());
		Assert.assertThat(locationPrefixProvider.getValue(), is("AFDEL-"));
		// Change to location 5
		when(userContext.getLocation()).thenReturn(location4.getParentLocation());
		Assert.assertThat(locationPrefixProvider.getValue(), is("KSUB-"));
	}
	
	@Test
	public void getLocationPrefix_shouldPickTheNearestValidPrefixUpTheTree() {
		Assert.assertEquals("REGD-", locationPrefixProvider.getLocationPrefix(location1));
	}
	
	@Test
	public void getLocationPrefix_shouldClimbToTopOfTheTreeAndPickValidPrefixIfOneIsSet() {
		LocationAttribute location4PrefixAtt = location4.getActiveAttributes().iterator().next();
		LocationAttribute location2PrefixAtt = location2.getActiveAttributes().iterator().next();
		// Invalidate prefix attributes for locations found in the middle of the tree
		location4PrefixAtt.setValue(" ");
		location2PrefixAtt.setValue(" ");
		Assert.assertEquals("KSUB-", locationPrefixProvider.getLocationPrefix(location1));
	}
	
	@Test(expected = RuntimeException.class)
	public void getLocationPrefix_throwAnExceptionIfNoValidPrefixIsFound() {
		// Invalidate valid prefixes
		LocationAttribute location2PrefixAtt = location2.getActiveAttributes().iterator().next();
		location2PrefixAtt.setValue(" ");
		LocationAttribute location4PrefixAtt = location4.getActiveAttributes().iterator().next();
		location4PrefixAtt.setValue(" ");
		LocationAttribute location7PrefixAtt = location7.getActiveAttributes().iterator().next();
		location7PrefixAtt.setValue(" ");
		
		locationPrefixProvider.getLocationPrefix(location1);
	}
	
	@Test
	public void getLocationPrefixRecursively_shouldPickThePrefixFromCurrentLocationIfOneIsSet() {
		LocationAttributeType prefixAttrType = createPrefixAttributeType();
		LocationAttribute prefixAtt = new LocationAttribute();
		prefixAtt.setAttributeType(prefixAttrType);
		prefixAtt.setValue("REGD");
		// add one at runtime
		location1.addAttribute(prefixAtt);
		Assert.assertEquals("REGD", locationPrefixProvider.getLocationPrefix(location1));
	}
	
	private void createLocationTree() {
		location1 = new Location();
		location1.setName("Location One");
		
		location2 = new Location();
		location2.setName("Location Two");
		location2.addChildLocation(location1);
		
		LocationAttribute location2PrefixAtt = new LocationAttribute();
		location2PrefixAtt.setAttributeType(createPrefixAttributeType());
		location2PrefixAtt.setValue("REGD-");
		location2.addAttribute(location2PrefixAtt);
		
		Location location3 = new Location();
		location3.setName("Location Three");
		location3.addChildLocation(location2);
			
		location4 = new Location();
		location4.setName("Location Four");
		location4.addChildLocation(location3);
		
		LocationAttribute location4PrefixAtt = new LocationAttribute();
		location4PrefixAtt.setAttributeType(createPrefixAttributeType());
		location4PrefixAtt.setValue("AFDEL-");
		location4.addAttribute(location4PrefixAtt);
		
		Location location5 = new Location();
		location5.setName("Location Five");
		location5.addChildLocation(location4);
		
		Location location6 = new Location();
		location6.setName("Location Six");
		location6.addChildLocation(location5);
		
		location7 = new Location();
		location7.setName("Location Seven");
		location7.addChildLocation(location6);
		
		LocationAttribute location7PrefixAtt = new LocationAttribute();
		location7PrefixAtt.setAttributeType(createPrefixAttributeType());
		location7PrefixAtt.setValue("KSUB-");
		location7.addAttribute(location7PrefixAtt);
		
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
