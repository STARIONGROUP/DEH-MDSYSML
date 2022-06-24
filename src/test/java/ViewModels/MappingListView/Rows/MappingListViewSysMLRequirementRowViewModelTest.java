/*
* MappingListViewSysMLRequirementRowViewModelTest.java
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

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Services.Stereotype.IStereotypeService;
import Services.Stereotype.StereotypeService;

class MappingListViewSysMLRequirementRowViewModelTest
{
	MappingListViewSysMLRequirementRowViewModel rowViewModel;
	IStereotypeService stereotypeService;
	Class requirement;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp()
	{
		this.stereotypeService = mock(IStereotypeService.class);
		StereotypeService.SetCurrent(this.stereotypeService);

		this.requirement = mock(Class.class);
		when(this.requirement.getID()).thenReturn(UUID.randomUUID().toString());
		when(this.requirement.getName()).thenReturn("Launcher mass");

		when(this.stereotypeService.GetRequirementId(this.requirement)).thenReturn("M010");
		when(this.stereotypeService.GetRequirementText(this.requirement)).thenReturn("A text");
		this.rowViewModel = new MappingListViewSysMLRequirementRowViewModel(this.requirement);
	}

	@Test
	void VerifyProperties()
	{
		assertEquals(String.format("%s-%s", "M010",this.requirement.getName()), this.rowViewModel.GetName());
		assertEquals("A text", this.rowViewModel.GetValue());
	}
}
