/*
* HubBrowserContextMenuViewModelTest.java
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
package ViewModels.ContextMenu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableValue;
import Services.Mapping.IMapCommandService;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;

class HubBrowserContextMenuViewModelTest
{
	HubBrowserContextMenuViewModel viewModel;
	IMapCommandService mapCommandService;
	IHubController hubController;
	ObservableValue<Boolean> canExecute;
	ObservableValue<Boolean> sessionEvent;
	Iteration iteration;
	boolean currentCanMapTopElement;
	
    @BeforeEach
    void setUp() 
    {
    	this.canExecute = new ObservableValue<>(false, Boolean.class);
    	this.sessionEvent = new ObservableValue<>(false, Boolean.class);
    	this.iteration = new Iteration();
    	
    	this.hubController = mock(IHubController.class);
    	when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
    	when(this.hubController.GetSessionEventObservable()).thenReturn(this.sessionEvent.Observable());
    	this.mapCommandService = mock(IMapCommandService.class);
    	when(this.mapCommandService.CanExecute()).thenReturn(false);
    	when(this.mapCommandService.CanExecuteObservable()).thenReturn(canExecute.Observable());
    	
    	this.viewModel = new HubBrowserContextMenuViewModel(mapCommandService, hubController);
    	this.viewModel.CanMapTopElement().subscribe(x -> this.currentCanMapTopElement = x);
    }
    
    @Test
    void VerifyProperties() 
    {
    	assertNotNull(this.viewModel.CanMapTopElement());
    	assertNotNull(this.viewModel.CanMapSelection());
    	assertFalse(this.currentCanMapTopElement);
    }
    
     @Test
     void VerifyObservables() 
     {
     	this.viewModel.SetBrowserType(IRequirementBrowserViewModel.class);
     	this.canExecute.Value(false);
    	assertFalse(this.currentCanMapTopElement);
     	
    	this.viewModel.SetBrowserType(IElementDefinitionBrowserViewModel.class);
     	this.canExecute.Value(false);
    	assertFalse(this.currentCanMapTopElement);
    	
    	when(this.mapCommandService.CanExecute()).thenReturn(true);
     	this.canExecute.Value(true);
    	assertFalse(this.currentCanMapTopElement);
    	
    	this.viewModel.SetBrowserType(IRequirementBrowserViewModel.class);
     	this.canExecute.Value(true);
    	assertFalse(this.currentCanMapTopElement);
     	
    	this.viewModel.SetBrowserType(IElementDefinitionBrowserViewModel.class);
     	this.canExecute.Value(true);
    	assertFalse(this.currentCanMapTopElement);
    	
    	this.iteration.setTopElement(new ElementDefinition());
    	this.viewModel.SetBrowserType(IRequirementBrowserViewModel.class);
    	this.sessionEvent.Value(true);
    	assertFalse(this.currentCanMapTopElement);

    	this.viewModel.SetBrowserType(IElementDefinitionBrowserViewModel.class);
     	this.sessionEvent.Value(true);
    	assertTrue(this.currentCanMapTopElement);
    	
    	when(this.mapCommandService.CanExecute()).thenReturn(false);
     	this.canExecute.Value(false);
    	assertFalse(this.currentCanMapTopElement);
     }
     
     @Test
     void VerifyMapCommands()
     {
     	this.iteration.setTopElement(new ElementDefinition());
     	assertDoesNotThrow(() -> this.viewModel.MapTopElement());
     	assertDoesNotThrow(() -> this.viewModel.MapSelection());
     	verify(this.mapCommandService, times(1)).MapTopElement(any(ElementDefinition.class), eq(MappingDirection.FromHubToDst));
     	verify(this.mapCommandService, times(0)).MapTopElement(any(ElementDefinition.class), eq(MappingDirection.FromDstToHub));
     	verify(this.mapCommandService, times(1)).MapSelection(MappingDirection.FromHubToDst);
     	verify(this.mapCommandService, times(0)).MapSelection(MappingDirection.FromDstToHub);
     }
}
