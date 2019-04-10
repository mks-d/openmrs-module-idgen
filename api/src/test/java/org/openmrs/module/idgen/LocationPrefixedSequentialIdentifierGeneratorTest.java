package org.openmrs.module.idgen;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class LocationPrefixedSequentialIdentifierGeneratorTest {

private IdentifierSourceService identifierSourceService;
	
	@Before
	public void setup() {
		mockStatic(Context.class);
		identifierSourceService = mock(IdentifierSourceService.class);		
	}
	
	@Test
	public void getIdentifierForSeed_shouldGenerateLocationPrefixedIdFromUnInitializedSource() {
		LocationPrefixedSequentialIdentifierGenerator generator = new LocationPrefixedSequentialIdentifierGenerator();
		generator.setBaseCharacterSet("0123456789");
		generator.setFirstIdentifierBase("000000");
		generator.setName("Location Prefixed Sequential Identifier Source");
		
		UserContext userContext = mock(UserContext.class);
		when(userContext.getLocation()).thenReturn(createLocationTree());
		when(Context.getUserContext()).thenReturn(userContext);
		when(Context.getService(IdentifierSourceService.class)).thenReturn(identifierSourceService);
		
		assertThat(generator.getIdentifierForSeed(1L), is("AFDEL-000-000001"));
	}
	
	private Location createLocationTree() {
		LocationAttributeType prefixAttrType = new LocationAttributeType();
		prefixAttrType.setName("Prefix");
		prefixAttrType.setMinOccurs(0);
		prefixAttrType.setMaxOccurs(1);
		prefixAttrType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		
		Location mainReg = new Location();
		mainReg.setName("Main Registration");
		
		Location kch = new Location();
		kch.setName("Kaboul Central Hospital");
		kch.addChildLocation(mainReg);
		
		LocationAttribute kchPrefixAtt = new LocationAttribute();
		kchPrefixAtt.setAttributeType(prefixAttrType);
		kchPrefixAtt.setValue("AFDEL-000-");
		kch.addAttribute(kchPrefixAtt);
		
		Location ks = new Location();
		ks.setName("Kaboul Subdelegation");
		ks.addChildLocation(kch);
		
		Location af = new Location();
		af.setName("Afghanistan Delegation");
		af.addChildLocation(ks);
		return mainReg;
	}

}
