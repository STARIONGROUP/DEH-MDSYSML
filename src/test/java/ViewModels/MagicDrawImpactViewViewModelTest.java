/*
* MagicDrawImpactViewViewModelTest.java
*
* Copyright (c) 2020-2022 RHEA System S.A.
*
* Author: Sam Geren�, Alex Vorobiev, Nathanael Smiechowski, Antoine Th�ate
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
package ViewModels;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import DstController.IDstController;
import Enumerations.MappingDirection;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.Stereotype.IStereotypeService;
import Services.Stereotype.StereotypeService;
import Utils.Stereotypes.Stereotypes;
import ViewModels.MagicDrawObjectBrowser.Rows.BlockRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.engineeringmodeldata.ElementDefinition;

class MagicDrawImpactViewViewModelTest
{
	MagicDrawImpactViewViewModel viewModel;
	IDstController dstController;
	IMagicDrawSessionService sessionService;
	IMagicDrawTransactionService transactionService;
	IStereotypeService stereotypeService;
	ObservableValue<Boolean> hasAnyOpenSession;
	ObservableValue<Boolean> sessionUpdated;
	ObservableCollection<MappedElementRowViewModel<DefinedThing, Class>> hubMapResult;
	ObservableCollection<Class> selectedHubMapResult;
	ArrayList<Element> projectElements;

	@BeforeEach
	void setUp()
	{
		this.dstController = mock(IDstController.class);
		this.sessionService = mock(IMagicDrawSessionService.class);
		this.transactionService = mock(IMagicDrawTransactionService.class);
		this.stereotypeService = mock(IStereotypeService.class);
		this.hasAnyOpenSession = new ObservableValue<>(false, Boolean.class);
		this.sessionUpdated = new ObservableValue<>(false, Boolean.class);
		this.hubMapResult = new ObservableCollection<>();
		this.selectedHubMapResult = new ObservableCollection<>();
		this.projectElements = new ArrayList<>();
		StereotypeService.SetCurrent(this.stereotypeService);

		when(this.sessionService.HasAnyOpenSessionObservable()).thenReturn(this.hasAnyOpenSession.Observable());
		when(this.sessionService.HasAnyOpenSession()).thenReturn(false);
		when(this.sessionService.SessionUpdated()).thenReturn(this.sessionUpdated.Observable());
		when(this.sessionService.GetProjectName()).thenReturn("Envision");
		when(this.sessionService.GetAllProjectElements()).thenReturn(this.projectElements);

		when(this.dstController.GetHubMapResult()).thenReturn(this.hubMapResult);
		when(this.dstController.GetSelectedHubMapResultForTransfer()).thenReturn(this.selectedHubMapResult);

		this.viewModel = new MagicDrawImpactViewViewModel(this.dstController, this.sessionService,
				this.transactionService, this.stereotypeService);
	}

	@Test
	void VerifySessionServiceObservables()
	{
		assertFalse(this.viewModel.isTheTreeVisible.Value());
		this.hasAnyOpenSession.Value(true);
		assertTrue(this.viewModel.isTheTreeVisible.Value());
		this.hasAnyOpenSession.Value(false);
		assertFalse(this.viewModel.isTheTreeVisible.Value());
		when(this.sessionService.HasAnyOpenSession()).thenReturn(true);
		this.sessionUpdated.Value(true);
		assertTrue(this.viewModel.isTheTreeVisible.Value());
		when(this.sessionService.HasAnyOpenSession()).thenReturn(false);
		this.sessionUpdated.Value(false);
		assertFalse(this.viewModel.isTheTreeVisible.Value());
	}

	@SuppressWarnings("unchecked")
	@Test
	void VerifyDstControllerObservables()
	{
		this.viewModel.UpdateBrowserTrees(true);

		Class block = mock(Class.class);
		when(block.getName()).thenReturn("Accelerometer");
		when(block.getID()).thenReturn(UUID.randomUUID().toString());
		when(this.stereotypeService.DoesItHaveTheStereotype(block, Stereotypes.Block)).thenReturn(true);

		Class block2 = mock(Class.class);
		when(block2.getName()).thenReturn("Other Accelerometer");
		when(block2.getID()).thenReturn(UUID.randomUUID().toString());
		when(this.stereotypeService.DoesItHaveTheStereotype(block2, Stereotypes.Block)).thenReturn(true);

		Class block3 = mock(Class.class);
		when(block3.getName()).thenReturn("New Accelerometer");
		when(block3.getID()).thenReturn(UUID.randomUUID().toString());
		when(this.stereotypeService.DoesItHaveTheStereotype(block3, Stereotypes.Block)).thenReturn(true);

		Property property1 = mock(Property.class);
		when(property1.getName()).thenReturn("property1");
		when(property1.getID()).thenReturn(UUID.randomUUID().toString());
		when(property1.getType()).thenReturn(block2);
		when(this.stereotypeService.DoesItHaveTheStereotype(property1, Stereotypes.PartProperty)).thenReturn(true);

		Property property2 = mock(Property.class);
		when(property2.getName()).thenReturn("property1");
		when(property2.getType()).thenReturn(block3);
		when(property2.getID()).thenReturn(UUID.randomUUID().toString());
		when(this.stereotypeService.DoesItHaveTheStereotype(property2, Stereotypes.PartProperty)).thenReturn(false);

		ArrayList<Property> properties = new ArrayList<>();
		properties.add(property1);
		properties.add(property2);
		when(block.getOwnedAttribute()).thenReturn(properties);

		this.projectElements.add(block2);
		this.projectElements.add(block);

		MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement = new MappedElementDefinitionRowViewModel(
				new ElementDefinition(), block, MappingDirection.FromHubToDst);
		MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement2 = new MappedElementDefinitionRowViewModel(
				new ElementDefinition(), block3, MappingDirection.FromHubToDst);
		ArrayList<MappedElementRowViewModel<DefinedThing, Class>> mappedElements = new ArrayList<>();
		mappedElements.add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement);
		mappedElements.add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement2);
		this.hubMapResult.addAll(mappedElements);
		this.hubMapResult.clear();
		when(this.sessionService.HasAnyOpenSession()).thenReturn(true);
		this.hubMapResult.addAll(mappedElements);
		assertTrue(this.viewModel.isTheTreeVisible.Value());
		this.selectedHubMapResult
				.addAll(this.hubMapResult.stream().map(x -> x.GetDstElement()).collect(Collectors.toList()));
		this.selectedHubMapResult.remove(block3);
		BlockRowViewModel blockRow = new BlockRowViewModel(null, block);
		assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged(null));
		assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged(blockRow));
		assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged(blockRow));
		this.hubMapResult.clear();
	}
}
