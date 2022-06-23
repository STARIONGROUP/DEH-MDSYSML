/*
* MappingListViewBlockRowViewModelTest.java
*
* Copyright (c) 2020-2022 RHEA System S.A.
*
* Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski, Antoine Théate
*
* This file is part of DEH-CommonJ
*
* The DEH-CommonJ is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 3 of the License, or (at your option) any later version.
*
* The DEH-CommonJ is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program; if not, write to the Free Software Foundation,
* Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/
package ViewModels.MappingListView.Rows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import Services.Stereotype.IStereotypeService;
import Services.Stereotype.StereotypeService;
import Utils.Stereotypes.Stereotypes;
import cdp4common.commondata.ClassKind;

class MappingListViewBlockRowViewModelTest
{
	MappingListViewBlockRowViewModel rowViewModel;
	IStereotypeService stereotypeService;
	Class block;

	@BeforeEach
	void setUp() throws Exception
	{
		this.stereotypeService = mock(IStereotypeService.class);
		StereotypeService.SetCurrent(this.stereotypeService);

		this.block = mock(Class.class);
		when(this.block.getID()).thenReturn(UUID.randomUUID().toString());
		when(this.block.getName()).thenReturn("envision");

		ArrayList<Property> containedAttributes = new ArrayList<>();

		Property valueProperty = mock(Property.class);
		when(valueProperty.getName()).thenReturn("mass");
		when(this.stereotypeService.DoesItHaveTheStereotype(valueProperty, Stereotypes.ValueProperty)).thenReturn(true);
		when(this.stereotypeService.GetValueRepresentation(valueProperty)).thenReturn("45");

		containedAttributes.add(valueProperty);

		Property partProperty = mock(Property.class);
		when(this.stereotypeService.DoesItHaveTheStereotype(partProperty, Stereotypes.ValueProperty)).thenReturn(false);

		containedAttributes.add(partProperty);
		when(this.block.getOwnedAttribute()).thenReturn(containedAttributes);

		this.rowViewModel = new MappingListViewBlockRowViewModel(this.block);
	}

	@Test
	void VerifyProperties()
	{
		assertEquals(1, this.rowViewModel.GetContainedRows().size());
		assertEquals(this.block.getID(), this.rowViewModel.GetId());
		assertEquals(this.block.getName(), this.rowViewModel.GetName());
		
		assertEquals(this.stereotypeService.GetValueRepresentation(this.block.getOwnedAttribute().get(0)),
				this.rowViewModel.GetContainedRows().get(0).GetValue());
		
		assertEquals(ClassKind.ElementDefinition, this.rowViewModel.GetClassKind());
	}
}
