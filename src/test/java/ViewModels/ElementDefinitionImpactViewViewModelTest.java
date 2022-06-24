/*
* ElementDefinitionImpactViewViewModelTest.java
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
package ViewModels;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.IterationElementDefinitionRowViewModel;
import ViewModels.ObjectBrowser.Rows.ThingRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.sitedirectorydata.DomainOfExpertise;

class ElementDefinitionImpactViewViewModelTest
{
	ElementDefinitionImpactViewViewModel viewModel;
	IHubController hubController;
	IDstController dstController;
	ObservableCollection<MappedElementRowViewModel<DefinedThing, Class>> dstMapResult;
	ObservableCollection<Thing> selectedDstMapResult;
	Iteration iteration;
	ElementDefinition elementDefinition;
	ObservableValue<Boolean> isSessionOpen;
	ObservableValue<Boolean> sessionEvent;

	@BeforeEach
	void setUp()
	{
		this.hubController = mock(IHubController.class);
		this.dstController = mock(IDstController.class);

		this.dstMapResult = new ObservableCollection<>();
		this.selectedDstMapResult = new ObservableCollection<>();
		when(this.dstController.GetDstMapResult()).thenReturn(this.dstMapResult);
		when(this.dstController.GetSelectedDstMapResultForTransfer()).thenReturn(this.selectedDstMapResult);
		DomainOfExpertise owner = new DomainOfExpertise();
		owner.setName("THERMAL");

		this.iteration = new Iteration();
		this.elementDefinition = new ElementDefinition();
		this.elementDefinition.setIid(UUID.randomUUID());
		this.elementDefinition.setName("Adapter Ring");
		this.elementDefinition.setOwner(owner);

		this.isSessionOpen = new ObservableValue<>(false, Boolean.class);
		this.sessionEvent = new ObservableValue<>(false, Boolean.class);
		when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
		when(this.hubController.GetIsSessionOpen()).thenReturn(false);
		when(this.hubController.GetIsSessionOpenObservable()).thenReturn(this.isSessionOpen.Observable());
		when(this.hubController.GetSessionEventObservable()).thenReturn(this.sessionEvent.Observable());

		this.viewModel = new ElementDefinitionImpactViewViewModel(this.hubController, this.dstController);
	}

	@Test
	void VerifyPropertiesAndInitialization()
	{
		assertNotNull(this.viewModel.dstController);
		assertFalse(this.viewModel.isTheTreeVisible.Value());
		assertNotNull(this.viewModel.IsTheTreeVisible());
		when(this.hubController.GetIsSessionOpen()).thenReturn(true);
		this.isSessionOpen.Value(true);
		this.sessionEvent.Value(true);
		assertDoesNotThrow(() -> this.viewModel = new ElementDefinitionImpactViewViewModel(this.hubController,
				this.dstController));
	}

	@SuppressWarnings("unchecked")
	@Test
	void VerifyDstMapResultObservables()
	{
		when(this.hubController.GetIsSessionOpen()).thenReturn(false);
		this.isSessionOpen.Value(false);
		this.sessionEvent.Value(false);
		this.iteration.getElement().add(this.elementDefinition);

		ElementDefinition otherDefinition = new ElementDefinition();
		otherDefinition.setIid(UUID.randomUUID());
		otherDefinition.setOwner(this.elementDefinition.getOwner());
		otherDefinition.setName("Adapter Rind Mounting");

		this.iteration.getElement().add(otherDefinition);

		when(this.hubController.GetIsSessionOpen()).thenReturn(true);
		this.isSessionOpen.Value(true);
		this.sessionEvent.Value(true);
		IterationElementDefinitionRowViewModel root = (IterationElementDefinitionRowViewModel) (this.viewModel
				.GetBrowserTreeModel().getRoot());
		assertEquals(2, root.GetContainedRows().size());

		Class block0 = mock(Class.class);
		when(block0.getName()).thenReturn("block0");
		when(block0.getID()).thenReturn(UUID.randomUUID().toString());

		MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement = new MappedElementDefinitionRowViewModel(
				this.elementDefinition.clone(false), block0, MappingDirection.FromDstToHub);
		ArrayList<MappedElementRowViewModel<DefinedThing, Class>> mappedElements = new ArrayList<>();
		mappedElements.add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement);
		this.dstMapResult.addAll(mappedElements);

		assertTrue(this.viewModel.GetRowViewModelFromThing(this.elementDefinition).GetIsHighlighted());
		assertFalse(this.viewModel.GetRowViewModelFromThing(otherDefinition).GetIsHighlighted());

		this.dstMapResult.clear();
		assertFalse(this.viewModel.GetRowViewModelFromThing(this.elementDefinition).GetIsHighlighted());

		ElementDefinition newElement = new ElementDefinition();
		newElement.setIid(UUID.randomUUID());
		newElement.setOwner(this.elementDefinition.getOwner());
		newElement.setName("Adapter Rind Mounting2");

		MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement2 = new MappedElementDefinitionRowViewModel(
				newElement, block0, MappingDirection.FromDstToHub);
		MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement3 = new MappedElementDefinitionRowViewModel(
				otherDefinition, block0, MappingDirection.FromDstToHub);
		mappedElements.add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement2);
		mappedElements.add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement3);
		this.dstMapResult.addAll(mappedElements);

		root = (IterationElementDefinitionRowViewModel) (this.viewModel.GetBrowserTreeModel().getRoot());
		assertEquals(3, root.GetThing().getElement().size());

		assertNull(this.viewModel.GetRowViewModelFromThing(new ElementDefinition()));
	}

	@SuppressWarnings("unchecked")
	@Test
	void VerifySelectedDstMapResultForTransferObservables()
	{
		this.iteration.getElement().add(this.elementDefinition);
		Class block0 = mock(Class.class);
		when(block0.getName()).thenReturn("block0");
		when(block0.getID()).thenReturn(UUID.randomUUID().toString());

		MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement = new MappedElementDefinitionRowViewModel(
				this.elementDefinition.clone(false), block0, MappingDirection.FromDstToHub);
		ArrayList<MappedElementRowViewModel<DefinedThing, Class>> mappedElements = new ArrayList<>();
		mappedElements.add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement);
		this.dstMapResult.addAll(mappedElements);
		when(this.hubController.GetIsSessionOpen()).thenReturn(true);
		this.isSessionOpen.Value(true);

		ArrayList<Thing> selectedThings = new ArrayList<>();
		selectedThings.add(this.elementDefinition);

		this.selectedDstMapResult.addAll(selectedThings);
		assertTrue(this.viewModel.GetRowViewModelFromThing(this.elementDefinition).GetIsSelected());

		this.selectedDstMapResult.removeAll(selectedThings);
		assertFalse(this.viewModel.GetRowViewModelFromThing(this.elementDefinition).GetIsSelected());

		assertDoesNotThrow(() -> this.viewModel.OnSelectionChanged((ThingRowViewModel<Thing>) null));

		ThingRowViewModel<? extends Thing> thingRow = (ThingRowViewModel<? extends Thing>) this.viewModel
				.GetRowViewModelFromThing(this.elementDefinition);
		this.viewModel.OnSelectionChanged((ThingRowViewModel<Thing>) thingRow);
		assertTrue(thingRow.GetIsSelected());
		this.viewModel.OnSelectionChanged((ThingRowViewModel<Thing>) thingRow);
		assertFalse(thingRow.GetIsSelected());
	}
}
