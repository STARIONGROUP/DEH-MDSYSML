/*
* MagicDrawImpactViewPanelViewModelTest.java
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableValue;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.NavigationService.INavigationService;
import ViewModels.Interfaces.IElementDefinitionImpactViewViewModel;
import ViewModels.Interfaces.IImpactViewContextMenuViewModel;
import ViewModels.Interfaces.IMagicDrawImpactViewViewModel;
import ViewModels.Interfaces.IRequirementImpactViewViewModel;
import ViewModels.Interfaces.ITransferControlViewModel;
import cdp4common.engineeringmodeldata.ExternalIdentifierMap;
import cdp4common.engineeringmodeldata.IdCorrespondence;

class MagicDrawImpactViewPanelViewModelTest
{
	MagicDrawImpactViewPanelViewModel viewModel;
	IHubController hubController;
	IDstController dstController;
	IElementDefinitionImpactViewViewModel elementDefinitionImpactViewModel;
	IRequirementImpactViewViewModel requirementImpactViewModel;
	ITransferControlViewModel transferControlViewModel;
	IImpactViewContextMenuViewModel contextMenuViewModel;
	IMagicDrawMappingConfigurationService mappingConfigurationService;
	IMagicDrawImpactViewViewModel magicDrawImpactViewViewModel;
	IMagicDrawUILogService logService;
	IMagicDrawSessionService sessionService;
	INavigationService navigationService;
	ObservableValue<Boolean> isSessionOpen;
	ObservableValue<Boolean> hasAnyOpenSession;

	@BeforeEach
	void setUp()
	{
		this.isSessionOpen = new ObservableValue<>(false, Boolean.class);
		this.hasAnyOpenSession = new ObservableValue<>(false, Boolean.class);

		this.hubController = mock(IHubController.class);
		when(this.hubController.GetIsSessionOpen()).thenReturn(false);
		when(this.hubController.GetIsSessionOpenObservable()).thenReturn(this.isSessionOpen.Observable());
		this.dstController = mock(IDstController.class);
		this.elementDefinitionImpactViewModel = mock(IElementDefinitionImpactViewViewModel.class);
		this.requirementImpactViewModel = mock(IRequirementImpactViewViewModel.class);
		this.transferControlViewModel = mock(ITransferControlViewModel.class);
		this.contextMenuViewModel = mock(IImpactViewContextMenuViewModel.class);
		this.mappingConfigurationService = mock(IMagicDrawMappingConfigurationService.class);
		this.magicDrawImpactViewViewModel = mock(IMagicDrawImpactViewViewModel.class);
		this.logService = mock(IMagicDrawUILogService.class);
		this.sessionService = mock(IMagicDrawSessionService.class);
		this.navigationService = mock(INavigationService.class);
		when(this.sessionService.HasAnyOpenSessionObservable()).thenReturn(this.hasAnyOpenSession.Observable());
		when(this.sessionService.GetProjectName()).thenReturn("Envision");

		this.viewModel = new MagicDrawImpactViewPanelViewModel(this.hubController, this.dstController,
				this.elementDefinitionImpactViewModel, this.requirementImpactViewModel, this.transferControlViewModel,
				this.contextMenuViewModel, this.mappingConfigurationService, this.magicDrawImpactViewViewModel,
				this.logService, this.sessionService, this.navigationService);
	}

	@Test
	void VerifyProperties()
	{
		assertEquals(this.magicDrawImpactViewViewModel, this.viewModel.GetMagicDrawImpactViewViewModel());
		assertEquals(this.elementDefinitionImpactViewModel, this.viewModel.GetElementDefinitionImpactViewViewModel());
		assertEquals(this.transferControlViewModel, this.viewModel.GetTransferControlViewModel());
		assertEquals(this.contextMenuViewModel, this.viewModel.GetContextMenuViewModel());
		assertEquals(this.requirementImpactViewModel, this.viewModel.GetRequirementDefinitionImpactViewViewModel());
		assertNotNull(this.viewModel.GetHasOneMagicDrawModelOpen());
		assertNotNull(this.viewModel.GetIsSessionOpen());
		assertFalse(this.viewModel.CanLoadMappingConfiguration());
	}

	@Test
	void VerifyGetSaveMappingConfigurationCollection()
	{
		assertEquals(0, this.viewModel.GetSavedMappingconfigurationCollection().size());
		when(this.hubController.GetIsSessionOpen()).thenReturn(true);
		ArrayList<ExternalIdentifierMap> maps = new ArrayList<>();
		when(this.hubController.GetAvailableExternalIdentifierMap(any(String.class))).thenReturn(maps);
		assertEquals(1, this.viewModel.GetSavedMappingconfigurationCollection().size());
		ExternalIdentifierMap map1 = new ExternalIdentifierMap();
		map1.setName("map1");

		ExternalIdentifierMap map2 = new ExternalIdentifierMap();
		map2.setName("Amap2");

		maps.add(map1);
		maps.add(map2);
		List<String> retrievedMaps = this.viewModel.GetSavedMappingconfigurationCollection();
		assertEquals(3, retrievedMaps.size());
		assertEquals(map2.getName(), retrievedMaps.get(1));
		assertEquals(map1.getName(), retrievedMaps.get(2));
	}

	@Test
	void VerifyGetOnChangeMappingConfiguration() throws Exception
	{
		when(this.dstController.ChangeMappingDirection()).thenReturn(MappingDirection.FromDstToHub,
				MappingDirection.FromHubToDst);
		assertEquals(MappingDirection.FromDstToHub, this.viewModel.GetOnChangeMappingDirectionCallable().call());
		assertEquals(MappingDirection.FromHubToDst, this.viewModel.GetOnChangeMappingDirectionCallable().call());
	}

	@Test
	void VerifyOnSaveLoadMappingConfiguration()
	{
		ExternalIdentifierMap map = new ExternalIdentifierMap();
		map.setName("cfg0");
		when(this.mappingConfigurationService.GetExternalIdentifierMap()).thenReturn(map);
		assertFalse(this.viewModel.OnSaveLoadMappingConfiguration(""));
		ArrayList<ExternalIdentifierMap> maps = new ArrayList<>();
		when(this.hubController.GetAvailableExternalIdentifierMap(any(String.class))).thenReturn(maps);
		when(this.mappingConfigurationService.IsTheCurrentIdentifierMapTemporary()).thenReturn(false, true, true);
		assertTrue(this.viewModel.OnSaveLoadMappingConfiguration("cfg"));
		maps.add(map);
		assertFalse(this.viewModel.OnSaveLoadMappingConfiguration(map.getName()));
		map.setRevisionNumber(45);
		map.getCorrespondence().add(new IdCorrespondence());
		when(this.navigationService.ShowConfirmDialog(any(String.class), any(String.class), any(Integer.class)))
				.thenReturn(JOptionPane.YES_OPTION, JOptionPane.NO_OPTION);
		assertFalse(this.viewModel.OnSaveLoadMappingConfiguration("cfg"));
		assertFalse(this.viewModel.OnSaveLoadMappingConfiguration("cfg"));
	}
}