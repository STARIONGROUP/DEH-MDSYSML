/*
 * MapCommandService.java
 *
 * Copyright (c) 2020-2021 RHEA System S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
 *
 * This file is part of DEH-MDSYSML
 *
 * The DEH-MDSYSML is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * The DEH-MDSYSML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Services.Mapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableValue;
import Services.MagicDrawSelection.IMagicDrawSelectionService;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import Services.NavigationService.INavigationService;
import Utils.Ref;
import Utils.Stereotypes.HubElementCollection;
import Utils.Stereotypes.HubRequirementCollection;
import Utils.Stereotypes.MagicDrawBlockCollection;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import Utils.Tasks.Task;
import Utils.Tasks.TaskStatus;
import ViewModels.Dialogs.Interfaces.IDstToHubMappingConfigurationDialogViewModel;
import ViewModels.Dialogs.Interfaces.IHubToDstMappingConfigurationDialogViewModel;
import ViewModels.Dialogs.Interfaces.IMappingConfigurationDialogViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import Views.Dialogs.MagicDrawDstToHubMappingConfigurationDialog;
import Views.Dialogs.MagicDrawHubToDstMappingConfigurationDialog;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import io.reactivex.Observable;

/**
 * The {@linkplain MapCommandService} provides a simplest way to prepare and map elements from DST and HUB, also provides an abstraction level 
 * on {@linkplain AbstractHandler}, so the action behind {@linkplain MapToHubCommand} can be properly tested since it has many dependencies
 */
public class MapCommandService implements IMapCommandService
{
    /**
     * The current class logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IDstToHubMappingConfigurationDialogViewModel} instance
     */
    private final IDstToHubMappingConfigurationDialogViewModel dstMappingDialogViewModel;

    /**
     * The {@linkplain IDstController} instance
     */
    private final IDstController dstController;

    /**
     * The {@linkplain INavigationService} instance
     */
    private final INavigationService navigationService;
    
    /**
     * The {@linkplain ICapellaSelectionService} instance
     */
    private final IMagicDrawSelectionService selectionService;
    
    /**
     * The {@linkplain ICapellaLogService} instance
     */
    private final IMagicDrawUILogService logService;

    /**
     * The {@linkplain IHubController} instance
     */
    private final IHubController hubController;

    /**
     * The {@linkplain IMagicDrawSessionService} 
     */
    private final IMagicDrawSessionService sessionService;
    
    /**
     * The {@linkplain IHubToDstMappingConfigurationDialogViewModel} instance
     */
    private final IHubToDstMappingConfigurationDialogViewModel hubMappingDialogViewModel;
    
    /**
     * Backing field for {@linkplain CanExecuteObservable}
     */
    private ObservableValue<Boolean> canExecute = new ObservableValue<>();
    
    /**
     * Initializes a new {@linkplain MapCommandService}
     * 
     * @param dstController the {@linkplain IDstController} instance
     * @param navigationService the {@linkplain INavigationService} instance
     * @param dstMappingDialog the {@linkplain IDstToHubMappingConfigurationDialogViewModel} instance
     * @param logService the {@linkplain IMagicDrawUILogService} instance
     * @param hubController the {@linkplain IHubController} instance
     * @param sessionService the {@linkplain ICapellaSessionService} instance
     * @param hubMappingDialog the {@linkplain IHubToDstMappingConfigurationDialogViewModel} instance
     * @param selectionService the {@linkplain IMagicDrawSelectionService} instance
     */
    public MapCommandService(IDstController dstController,
            INavigationService navigationService, IDstToHubMappingConfigurationDialogViewModel dstMappingDialog,
            IMagicDrawUILogService logService, IHubController hubController, IMagicDrawSessionService sessionService,
            IHubToDstMappingConfigurationDialogViewModel hubMappingDialog, IMagicDrawSelectionService selectionService)
    {
        this.dstController = dstController;
        this.navigationService = navigationService;
        this.dstMappingDialogViewModel = dstMappingDialog;
        this.logService = logService;
        this.hubController = hubController;
        this.sessionService = sessionService;
        this.hubMappingDialogViewModel = hubMappingDialog;
        this.selectionService = selectionService;
        this.Initialize();
    }
    
    /**
     * Initializes this {@linkplain MapCommandService} {@linkplain CanExecuteObservable} 
     * for later use by the context menu map command
     */
    @Override
    public void Initialize()
    {
        Observable.combineLatest(this.sessionService.HasAnyOpenSessionObservable().startWith(this.sessionService.HasAnyOpenSession()), 
                    this.hubController.GetIsSessionOpenObservable().startWith(this.hubController.GetIsSessionOpen()),
                (hasAnyOpenSession, isHubSessionOpen) -> hasAnyOpenSession && isHubSessionOpen)
        .subscribe(x -> this.canExecute.Value(x));
    }
    
    /**
     * Gets an {@linkplain Observable} of {@linkplain Boolean} indicating whether the map action can be executed
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> CanExecuteObservable()
    {
        return this.canExecute.Observable();
    }

    /**
     * Gets a value indicating whether the map action can be executed
     * 
     * @return a {@linkplain Boolean} value
     */
    @Override
    public boolean CanExecute()
    {
        return this.sessionService.HasAnyOpenSession() && this.hubController.GetIsSessionOpen();
    }

    /**
     * Maps the selection from the {@linkplain ISelectionService} to the specified {@linkplain MappingDirection}
     * 
     * @param mappingDirection the {@linkplain MappingDirection}
     */
    @Override
    public void MapSelection(MappingDirection mappingDirection)
    {
        switch (mappingDirection)
        {
            case FromHubToDst:
                this.MapFromHubToDst(null);
                break;
        
            case FromDstToHub:
                this.MapFromDstToHub();
                break;

            default:
                break;
        }
    }

    /**
     * Maps the specified top element to the specified {@linkplain MappingDirection}
     * 
     * @param <TElement> the type of the top element
     * @param topElement the {@linkplain #TElement} top element
     * @param mappingDirection the {@linkplain MappingDirection}
     */
    @Override
    public <TElement> void MapTopElement(TElement topElement, MappingDirection mappingDirection)
    {        
        if (mappingDirection == MappingDirection.FromHubToDst && topElement instanceof ElementDefinition)
        {
            this.MapFromHubToDst((ElementDefinition)topElement);
        }
    }
        
    /**
     * Maps either the specified top element {@linkplain ElementDefinition} or the currently selected elements
     * 
     * @param topElement the {@linkplain ElementDefinition} optional
     */
    private void MapFromHubToDst(ElementDefinition topElement)
    {        
        try 
        {
            this.hubMappingDialogViewModel.SetMappedElement(topElement != null ? Arrays.asList(topElement) : this.selectionService.GetHubSelection());
            
            Ref<Boolean> dialogResult = new Ref<>(Boolean.class, false);
            
            Task.Run(() -> dialogResult.Set(this.navigationService.ShowDialog(new MagicDrawHubToDstMappingConfigurationDialog(), this.hubMappingDialogViewModel)))
                .Observable()
                .subscribe(x -> WhenDialogHasBeenClosed(dialogResult, MappingDirection.FromHubToDst), x -> this.logger.catching(x));
        }
        catch (Exception exception) 
        {
            this.logger.catching(exception);
        }
    }

    /**
     * Maps the selected element from the {@linkplain ICapellaSelectionService} through the {@linkplain CapellaDstToHubMappingConfigurationDialog}
     * ShowDialog needs to be called asynchronously because of swing/AWT incompatibility, otherwise awaiting for the dialog result blocks the AWT UI Thread
     * 
     * @param elements the {@linkplain Collection} of {@linkplain Class} element to map
     */
    private void MapFromDstToHub()
    {
        try 
        {
            this.dstMappingDialogViewModel.SetMappedElement(this.selectionService.GetDstSelection(Element.class));
            
            Ref<Boolean> dialogResult = new Ref<>(Boolean.class, false);
            
            Task.Run(() -> dialogResult.Set(this.navigationService.ShowDialog(new MagicDrawDstToHubMappingConfigurationDialog(), this.dstMappingDialogViewModel)))
                .Observable()
                .subscribe(x -> WhenDialogHasBeenClosed(dialogResult, MappingDirection.FromDstToHub), x -> this.logger.catching(x));
        }
        catch (Exception exception) 
        {
            this.logger.catching(exception);
        }
    }
    
    /**
     * Occurs when the {@linkplain CapellaDstToHubMappingConfigurationDialog} is closed by the user. 
     * It asynchronously calls {@linkplain MapSelectedElements} with the valid mapped element resulting from the {@linkplain CapellaDstToHubMappingConfigurationDialog}
     * 
     * @param dialogResult the {@linkplain Ref} carrying the dialog result {@linkplain Boolean}
     */
    void WhenDialogHasBeenClosed(Ref<Boolean> dialogResult, MappingDirection mappingDirection)
    {
        if(!Boolean.TRUE.equals(dialogResult.Get()))
        {
            return;
        }   
        
        Collection<MappedElementRowViewModel<? extends Thing, ?>> validMappedElements;        
        IMappingConfigurationDialogViewModel<?, ?, ?> dialogViewModel;
        
        if(mappingDirection == MappingDirection.FromHubToDst)
        {
            dialogViewModel = this.hubMappingDialogViewModel;
        }
        else
        {
            dialogViewModel = this.dstMappingDialogViewModel;
        }
        
        validMappedElements = dialogViewModel.GetMappedElementCollection()
                .stream()
                .filter(m -> m.GetIsValid())
                .collect(Collectors.toList());
                    
        StopWatch timer = StopWatch.createStarted();
        
        Task.Run(() -> this.MapSelectedElements(validMappedElements, mappingDirection), boolean.class)
            .Observable()
            .subscribe(t -> 
            {
                if(timer.isStarted())
                {
                    timer.stop();
                }

                if(t.GetStatus() == TaskStatus.Faulted)
                {
                    this.logger.catching(t.GetException());
                }
                
                this.logService.Append(String.format("Mapping action is done in %s ms", timer.getTime(TimeUnit.MILLISECONDS)), t.GetResult() == true);
                
            }, t -> this.logger.catching(t));
    }
        
    /**
     * Maps the selected elements from the current tree
     * 
     * @param mappedElements the collection of {@linkplain MappedElementRowViewModel}
     * @param mappingDirection the applicable {@linkplain MappingDirection}
     * @return a value indicating whether the mapping operation succeeded
     */
    private boolean MapSelectedElements(Collection<MappedElementRowViewModel<? extends Thing, ?>> mappableElements, MappingDirection mappingDirection)
    {
        if(mappingDirection == MappingDirection.FromDstToHub)
        {
            return MapSelectedElementsFromDstToHub(mappableElements);
        }
        else if(mappingDirection == MappingDirection.FromHubToDst)
        {
            return MapSelectedElementFromHubToDst(mappableElements);
        }
        
        return false;
    }

    /**
     * Calls the {@linkplain IDstController} to map the provided {@linkplain MappedElementRowViewModel}s towards the DST
     * 
     * @param mappableElements the {@linkplain Collection} of {@linkplain MappedElementRowViewModel} to be mapped
     * @return a value indicating whether the mapping operation went ok
     */
    private boolean MapSelectedElementFromHubToDst(Collection<MappedElementRowViewModel<? extends Thing, ?>> mappableElements)
    {
        boolean result = true;
        
        HubRequirementCollection mappedHubRequirements = new HubRequirementCollection();
        HubElementCollection mappedElementDefinitions = new HubElementCollection();
        
        mappedHubRequirements.addAll(mappableElements.stream()
                .filter(x -> x instanceof MappedRequirementRowViewModel)
                .map(x -> (MappedRequirementRowViewModel)x)
                .collect(Collectors.toList()));
        
        mappedElementDefinitions.addAll(mappableElements.stream()
                .filter(x -> x instanceof MappedElementDefinitionRowViewModel)
                .map(x -> (MappedElementDefinitionRowViewModel)x)
                .collect(Collectors.toList()));

        
        if(!mappedHubRequirements.isEmpty())
        {
            this.logService.Append("Mapping of %s Requirements in progress...", mappedHubRequirements.size());
            result &= this.dstController.Map(mappedHubRequirements, MappingDirection.FromHubToDst);
        }        

        if(!mappedElementDefinitions.isEmpty())
        {
            this.logService.Append("Mapping of %s Elements in progress...", mappedElementDefinitions.size());
            result &= this.dstController.Map(mappedElementDefinitions, MappingDirection.FromHubToDst);
        }
        
        return result;
    }

    /**
     * Calls the {@linkplain IDstController} to map the provided {@linkplain MappedElementRowViewModel}s towards the HUB
     * 
     * @param mappableElements the {@linkplain Collection} of {@linkplain MappedElementRowViewModel} to be mapped
     * @return a value indicating whether the mapping operation went ok
     */
    private boolean MapSelectedElementsFromDstToHub(Collection<MappedElementRowViewModel<? extends Thing, ?>> mappableElements)
    {
        boolean result = true;
        MagicDrawBlockCollection mappedComponents = new MagicDrawBlockCollection();
        MagicDrawRequirementCollection mappedDstRequirements = new MagicDrawRequirementCollection();
        
        mappedDstRequirements.addAll(mappableElements.stream()
                .filter(x -> x.GetTThingClass().isAssignableFrom(cdp4common.engineeringmodeldata.Requirement.class))
                .map(x -> (MappedRequirementRowViewModel)x)
                .collect(Collectors.toList()));

        mappedComponents.addAll(mappableElements.stream()
                .filter(x -> x.GetTThingClass().isAssignableFrom(ElementDefinition.class))
                .map(x -> (MappedElementDefinitionRowViewModel)x)
                .collect(Collectors.toList()));
        
        if(!mappedDstRequirements.isEmpty())
        {
            this.logService.Append("Mapping of %s Requirements in progress...", mappedDstRequirements.size());
            result &= this.dstController.Map(mappedDstRequirements, MappingDirection.FromDstToHub);
        }
        
        if(!mappedComponents.isEmpty())
        {
            this.logService.Append("Mapping of %s Blocks in progress...", mappedComponents.size());
            result &= this.dstController.Map(mappedComponents, MappingDirection.FromDstToHub);
        }
        
        return result;
    }
}
