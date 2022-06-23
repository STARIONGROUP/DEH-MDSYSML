/*
* MappedThingRowViewModelTest.java
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
package ViewModels.Rows;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Enumerations.MappingDirection;

class MappedThingRowViewModelTest
{
	MappedThingRowViewModel rowViewModel;
	String dstThingName;
	String hubThingName;
	MappingDirection mappingDirection;

	@BeforeEach
	void setUp()
	{
		this.dstThingName = "Envision";
		this.hubThingName = "TopElement";
		this.mappingDirection = MappingDirection.FromDstToHub;

		this.rowViewModel = new MappedThingRowViewModel(this.dstThingName, this.hubThingName, this.mappingDirection);
	}

	@Test
	void VerifyProperties()
	{
		assertEquals(this.dstThingName, this.rowViewModel.GetDstThingName());
		assertEquals(this.hubThingName, this.rowViewModel.GetHubThingName());
		assertEquals(this.mappingDirection, this.rowViewModel.GetMappingDirection());
		assertDoesNotThrow(() -> this.rowViewModel.SetDstThingName(this.hubThingName));
		assertDoesNotThrow(() -> this.rowViewModel.SetHubThingName(this.dstThingName));
		assertDoesNotThrow(() -> this.rowViewModel.SetMappingDirection(MappingDirection.FromHubToDst));
	}
}
