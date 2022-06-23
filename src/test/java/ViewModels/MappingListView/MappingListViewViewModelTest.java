/*
* MappingListViewViewModelTest.java
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
package ViewModels.MappingListView;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MagicDrawTransaction.Clones.ClonedReferenceElement;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.engineeringmodeldata.ElementDefinition;

class MappingListViewViewModelTest
{
	MagicDrawMappingListViewViewModel viewModel;
	IDstController dstController;
	IHubController hubController;
	IMagicDrawTransactionService transactionService;
	ObservableCollection<MappedElementRowViewModel<DefinedThing, Class>> dstMapResult;
	ObservableCollection<MappedElementRowViewModel<DefinedThing, Class>> hubMapResult;
	boolean isTreeVisible;

	@BeforeEach
	void setUp() throws Exception
	{
		this.dstMapResult = new ObservableCollection<>();
		this.hubMapResult = new ObservableCollection<>();
		this.dstController = mock(IDstController.class);
		when(this.dstController.GetDstMapResult()).thenReturn(this.dstMapResult);
		when(this.dstController.GetHubMapResult()).thenReturn(this.hubMapResult);
		this.hubController = mock(IHubController.class);
		when(this.hubController.GetIsSessionOpen()).thenReturn(true);
		this.transactionService = mock(IMagicDrawTransactionService.class);
		this.viewModel = new MagicDrawMappingListViewViewModel(this.dstController, this.hubController,
				this.transactionService);
		this.viewModel.IsTheTreeVisible().subscribe(x -> this.isTreeVisible = x);
	}

	@SuppressWarnings("unchecked")
	@Test
	void UpdateBrowserTrees()
	{
		this.viewModel.UpdateBrowserTrees(false);
		assertFalse(this.isTreeVisible);

		ArrayList<MappedElementRowViewModel<DefinedThing, Class>> mappedElements = new ArrayList<>();

		Class block = mock(Class.class);
		ElementDefinition definition = new ElementDefinition();

		MappedElementRowViewModel<? extends DefinedThing, Class> dstMappedElement = new MappedElementDefinitionRowViewModel(
				definition, block, MappingDirection.FromDstToHub);
		MappedElementRowViewModel<? extends DefinedThing, Class> hubMappedElement = new MappedElementDefinitionRowViewModel(
				definition, block, MappingDirection.FromHubToDst);

		mappedElements.add((MappedElementRowViewModel<DefinedThing, Class>) dstMappedElement);
		mappedElements.add((MappedElementRowViewModel<DefinedThing, Class>) hubMappedElement);

		this.dstMapResult.addAll(
				mappedElements.stream().filter(x -> x.GetMappingDirection().equals(MappingDirection.FromDstToHub))
						.collect(Collectors.toList()));
		
		this.hubMapResult.addAll(
				mappedElements.stream().filter(x -> x.GetMappingDirection().equals(MappingDirection.FromHubToDst))
						.collect(Collectors.toList()));
		
		assertTrue(this.isTreeVisible);

		when(this.transactionService.IsCloned(block)).thenReturn(true);
		ClonedReferenceElement<Class> cloned = mock(ClonedReferenceElement.class);
		when(cloned.GetOriginal()).thenReturn(mock(Class.class));
		when(this.transactionService.GetClone(block)).thenReturn(cloned);

		ElementDefinition cloneDefinition = definition.clone(false);
		this.dstMapResult.get(0).SetHubElement(cloneDefinition);
		this.viewModel.UpdateBrowserTrees(true);
	}

}
