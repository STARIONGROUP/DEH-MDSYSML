/*
* HubToDstMappingConfigurationDialogViewModelTest.java
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
package ViewModels.Dialogs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Utils.Ref;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IMagicDrawObjectBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.BlockRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.RequirementRowViewModel;
import ViewModels.MappedElementListView.Interfaces.IMappedElementListViewViewModel;
import ViewModels.ObjectBrowser.Rows.ThingRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

class HubToDstMappingConfigurationDialogViewModelTest
{
	HubToDstMappingConfigurationDialogViewModel viewModel;
	IDstController dstController;
	IHubController hubController;
	IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel;
	IRequirementBrowserViewModel requirementBrowserViewModel;
	IMagicDrawObjectBrowserViewModel magicDrawObjectBrowserViewModel;
	IMagicDrawTransactionService transactionService;
	IMappedElementListViewViewModel<Class> mappedElementListViewViewModel;
	ObservableValue<ElementRowViewModel<?>> magicDrawSelectedElement;
	ObservableValue<ThingRowViewModel<Thing>> elementDefinitionSelectedElement;
	ObservableValue<ThingRowViewModel<Thing>> requirementSelectedElement;
	ObservableValue<MappedElementRowViewModel<DefinedThing, Class>> mappedElementSelectedElement;
	ObservableCollection<MappedElementRowViewModel<DefinedThing, Class>> hubMapResult;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp()
	{
		this.magicDrawSelectedElement = new ObservableValue<>();
		this.elementDefinitionSelectedElement = new ObservableValue<>();
		this.requirementSelectedElement = new ObservableValue<>();
		this.mappedElementSelectedElement = new ObservableValue<>();
		this.hubMapResult = new ObservableCollection<>();

		this.dstController = mock(IDstController.class);
		this.hubController = mock(IHubController.class);
		this.elementDefinitionBrowserViewModel = mock(IElementDefinitionBrowserViewModel.class);
		this.requirementBrowserViewModel = mock(IRequirementBrowserViewModel.class);
		this.magicDrawObjectBrowserViewModel = mock(IMagicDrawObjectBrowserViewModel.class);
		this.transactionService = mock(IMagicDrawTransactionService.class);
		this.mappedElementListViewViewModel = mock(IMappedElementListViewViewModel.class);

		when(this.dstController.GetHubMapResult()).thenReturn(this.hubMapResult);
		when(this.magicDrawObjectBrowserViewModel.GetSelectedElement())
				.thenReturn(this.magicDrawSelectedElement.Observable());

		when(this.elementDefinitionBrowserViewModel.GetSelectedElement())
				.thenReturn(this.elementDefinitionSelectedElement.Observable());

		when(this.requirementBrowserViewModel.GetSelectedElement())
				.thenReturn(this.requirementSelectedElement.Observable());

		when(this.mappedElementListViewViewModel.GetSelectedElement())
				.thenReturn(this.mappedElementSelectedElement.Observable());

		this.viewModel = new HubToDstMappingConfigurationDialogViewModel(this.dstController, this.hubController,
				this.elementDefinitionBrowserViewModel, this.requirementBrowserViewModel,
				this.magicDrawObjectBrowserViewModel, this.transactionService, this.mappedElementListViewViewModel);
	}

	@Test
	void VerifyProperties()
	{
		assertEquals(this.magicDrawObjectBrowserViewModel, this.viewModel.GetDstObjectBrowserViewModel());
	}

	@Test
	void VerifyUpdateProperties()
	{
		assertDoesNotThrow(() -> this.viewModel.UpdateProperties());
		verify(this.magicDrawObjectBrowserViewModel, times(1)).BuildTree();
	}

	@SuppressWarnings("unchecked")
	@Test
	void VerifyPremap()
	{
		ArrayList<Thing> elements = new ArrayList<>();
		assertDoesNotThrow(() -> this.viewModel.PreMap(elements));

		ElementDefinition elementDefinition = new ElementDefinition();
		elementDefinition.setName("Accelerometer");
		elementDefinition.setIid(UUID.randomUUID());

		Requirement requirement = new Requirement();
		requirement.setName("Launcher mass");

		when(this.dstController.TryGetElementByName(any(Requirement.class), any(Ref.class))).thenReturn(false, false,
				true);
		when(this.dstController.TryGetElementByName(any(ElementDefinition.class), any(Ref.class))).thenReturn(false,
				false, true);

		when(this.transactionService.Create(Stereotypes.Block, elementDefinition.getName())).thenReturn(null,
				mock(Class.class));

		when(this.transactionService.Create(Stereotypes.Requirement, requirement.getName())).thenReturn(null,
				mock(Class.class));

		when(this.transactionService.CloneElement(any(Class.class))).thenReturn(mock(Class.class));

		elements.add(elementDefinition);
		elements.add(requirement);
		elements.add(new RequirementsSpecification());

		assertDoesNotThrow(() -> this.viewModel.PreMap(elements));
		this.viewModel.mappedElements.clear();
		assertDoesNotThrow(() -> this.viewModel.PreMap(elements));
		this.viewModel.mappedElements.clear();
		assertDoesNotThrow(() -> this.viewModel.PreMap(elements));
	}

	@SuppressWarnings("unchecked")
	@Test
	void VerifyWhenMapToNewElement()
	{
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(false));

		MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement = new MappedElementDefinitionRowViewModel(
				null, MappingDirection.FromHubToDst);
		this.mappedElementSelectedElement.Value((MappedElementRowViewModel<DefinedThing, Class>) mappedElement);
		this.mappedElementSelectedElement.Value().SetShouldCreateNewTargetElement(false);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(false));

		this.mappedElementSelectedElement.Value().SetShouldCreateNewTargetElement(false);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));
		this.mappedElementSelectedElement.Value().SetShouldCreateNewTargetElement(true);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		this.mappedElementSelectedElement.Value().SetHubElement(new ElementDefinition());
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		this.mappedElementSelectedElement.Value().SetShouldCreateNewTargetElement(false);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		assertNotNull(mappedElement.GetDstElementRepresentation());
		assertNotNull(mappedElement.GetHubElementRepresentation());
		
		mappedElement = new MappedRequirementRowViewModel(null, MappingDirection.FromHubToDst);
		this.mappedElementSelectedElement.Value((MappedElementRowViewModel<DefinedThing, Class>) mappedElement);

		this.mappedElementSelectedElement.Value().SetShouldCreateNewTargetElement(false);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(false));

		this.mappedElementSelectedElement.Value().SetShouldCreateNewTargetElement(false);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		this.mappedElementSelectedElement.Value().SetShouldCreateNewTargetElement(true);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		this.mappedElementSelectedElement.Value().SetShouldCreateNewTargetElement(false);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		this.mappedElementSelectedElement.Value().SetHubElement(new Requirement());
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));
		
		assertNotNull(mappedElement.GetDstElementRepresentation());
		assertNotNull(mappedElement.GetHubElementRepresentation());
	}

	@Test
	void VerifyMagicDrawObjectBrowserObservable()
	{
		Class block = mock(Class.class);
		when(block.getID()).thenReturn(UUID.randomUUID().toString());
		when(block.getName()).thenReturn("Accelerometer");

		ClassRowViewModel classRow = new BlockRowViewModel(null, block);
		this.magicDrawSelectedElement.Value(classRow);

		assertEquals(1, this.viewModel.mappedElements.size());
		this.magicDrawSelectedElement.Value(classRow);
		assertEquals(1, this.viewModel.mappedElements.size());

		classRow = new RequirementRowViewModel(null, mock(Class.class));
		this.magicDrawSelectedElement.Value(classRow);
		assertEquals(2, this.viewModel.mappedElements.size());
	}
}
