/*
* MapCommandServiceTest.java
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
package Services.Mapping;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Services.MagicDrawSelection.IMagicDrawSelectionService;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import Services.NavigationService.INavigationService;
import Utils.Ref;
import ViewModels.Dialogs.Interfaces.IDstToHubMappingConfigurationDialogViewModel;
import ViewModels.Dialogs.Interfaces.IHubToDstMappingConfigurationDialogViewModel;
import io.reactivex.Observable;

class MapCommandServiceTest
{
    private IDstController dstController;
    private INavigationService navigationService;
    private IDstToHubMappingConfigurationDialogViewModel dstMappingDialog;
    private IMagicDrawUILogService logService;
    private IHubController hubController;
    private IMagicDrawSessionService sessionService;
    private IHubToDstMappingConfigurationDialogViewModel hubMappingDialog;
    private IMagicDrawSelectionService selectionService;
    private MapCommandService service;

    @BeforeEach
    void setUp() throws Exception
    {
        this.dstController = mock(IDstController.class);
        this.navigationService = mock(INavigationService.class);
        this.dstMappingDialog = mock(IDstToHubMappingConfigurationDialogViewModel.class);
        this.logService = mock(IMagicDrawUILogService.class);
        this.hubController = mock(IHubController.class);
        this.sessionService = mock(IMagicDrawSessionService.class);
        this.hubMappingDialog = mock(IHubToDstMappingConfigurationDialogViewModel.class);
        this.selectionService = mock(IMagicDrawSelectionService.class);
        
        this.InitializeMockSetups();
        
        this.service = new MapCommandService(dstController, navigationService, dstMappingDialog, logService, 
                hubController, sessionService, hubMappingDialog, selectionService);
    }

    private void InitializeMockSetups()
    {
        when(this.sessionService.HasAnyOpenSessionObservable()).thenReturn(Observable.fromArray(true));
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(Observable.fromArray(true));
        when(this.navigationService.ShowDialog(any(), any())).thenReturn(true);
        
        when(this.hubMappingDialog.GetMappedElementCollection()).thenReturn(new ObservableCollection<>());
        when(this.dstMappingDialog.GetMappedElementCollection()).thenReturn(new ObservableCollection<>());
    }

    @Test
    void VerifyProperties()
    {
        assertNotNull(this.service.CanExecuteObservable());
    }
    
    @Test
    void VerifyMapSelection()
    {
        assertDoesNotThrow(() -> this.service.MapSelection(null));
        assertDoesNotThrow(() -> this.service.MapSelection(MappingDirection.FromDstToHub));
        assertDoesNotThrow(() -> this.service.MapSelection(MappingDirection.FromHubToDst));
    }    
    
    @Test
    void VerifyMapTopElement()
    {
        assertDoesNotThrow(() -> this.service.MapTopElement(null, MappingDirection.FromHubToDst));
        assertDoesNotThrow(() -> this.service.MapTopElement(null, MappingDirection.FromDstToHub));
    }
    
    @Test
    void VerifyWhenDialogHasBeenClosed()
    {
        Ref<Boolean> dialogResult = new Ref<>(Boolean.class);
        assertDoesNotThrow(() -> this.service.WhenDialogHasBeenClosed(dialogResult, MappingDirection.FromDstToHub));
        dialogResult.Set(true);
        assertDoesNotThrow(() -> this.service.WhenDialogHasBeenClosed(dialogResult, MappingDirection.FromDstToHub));
        assertDoesNotThrow(() -> this.service.WhenDialogHasBeenClosed(dialogResult, MappingDirection.FromHubToDst));
    }
}
