/*
 * MagicDrawImpactViewPanelViewModel.java
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
package ViewModels;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import com.nomagic.magicdraw.ui.notification.NotificationSeverity;

import DstController.DstController;
import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Utils.Tasks.Task;
import ViewModels.Interfaces.IElementDefinitionImpactViewViewModel;
import ViewModels.Interfaces.IImpactViewContextMenuViewModel;
import ViewModels.Interfaces.IMagicDrawImpactViewPanelViewModel;
import ViewModels.Interfaces.IMagicDrawImpactViewViewModel;
import ViewModels.Interfaces.IRequirementImpactViewViewModel;
import ViewModels.Interfaces.ITransferControlViewModel;
import cdp4common.engineeringmodeldata.ExternalIdentifierMap;

import static Utils.Operators.Operators.AreTheseEquals;

import io.reactivex.Observable;

/**
 * The {@linkplain MagicDrawImpactViewPanelViewModel} is the DST adapter implementation of the 
 * {@linkplain ImpactViewPanelViewModel} main view model for the {@linkplain MagicDrawImpactViewPanel}
 */
public class MagicDrawImpactViewPanelViewModel extends ImpactViewPanelViewModel implements IMagicDrawImpactViewPanelViewModel
{
    /**
     * A value indicating whether the session to the hub is open
     */
    private Observable<Boolean> isSessionOpen;
    
    /**
     * The {@linkplain IDstController} instance
     */
    private IDstController dstController;    

    /**
     * The {@linkplain IMagicDrawUILogService}
     */
    private IMagicDrawUILogService logService;
    
    /**
     * The {@linkplain IMagicDrawMappingConfigurationService}
     */
    private IMagicDrawMappingConfigurationService mappingConfigurationService;

    /**
     * The {@linkplain IElementDefinitionImpactViewViewModel}
     */
    private IMagicDrawImpactViewViewModel magicDrawImpactViewViewModel;

    /**
     * Gets the {@linkplain IElementDefinitionImpactViewViewModel} elementDefinitionImpactViewViewModel
     * 
     * @return the {@linkplain IElementDefinitionImpactViewViewModel}
     */
    @Override
    public IMagicDrawImpactViewViewModel GetMagicDrawImpactViewViewModel()
    {
        return magicDrawImpactViewViewModel;
    }
    
    /**
     * The {@linkplain IElementDefinitionImpactViewViewModel}
     */
    private IElementDefinitionImpactViewViewModel elementDefinitionImpactViewViewModel;

    /**
     * Gets the {@linkplain IElementDefinitionImpactViewViewModel} elementDefinitionImpactViewViewModel
     * 
     * @return the {@linkplain IElementDefinitionImpactViewViewModel}
     */
    @Override
    public IElementDefinitionImpactViewViewModel GetElementDefinitionImpactViewViewModel()
    {
        return elementDefinitionImpactViewViewModel;
    }

    /**
     * The {@linkplain ITransferControlViewModel}
     */
    private ITransferControlViewModel transferControlViewModel;

    /**
     * Gets the {@linkplain ITransferControlViewModel}
     * 
     * @return a {@linkplain ITransferControlViewModel}
     */
    @Override
    public ITransferControlViewModel GetTransferControlViewModel()
    {
        return this.transferControlViewModel;
    }

    /**
     * The {@linkplain IImpactViewContextMenuViewModel}
     */
    private IImpactViewContextMenuViewModel contextMenuViewModel;    
    
    /**
     * Gets the {@linkplain IImpactViewContextMenuViewModel} view model for the context menus
     * 
     * @return a {@linkplain IImpactViewContextMenuViewModel}
     */
    @Override
    public IImpactViewContextMenuViewModel GetContextMenuViewModel()
    {
        return this.contextMenuViewModel;
    }
    
    /**
     * The {@linkplain IRequirementImpactViewViewModel}
     */
    private IRequirementImpactViewViewModel requirementDefinitionImpactViewViewModel;

    /**
     * Gets the {@linkplain IRequirementImpactViewViewModel} requirementDefinitionImpactViewViewModel
     * 
     * @return the {@linkplain IRequirementImpactViewViewModel}
     */
    @Override
    public IRequirementImpactViewViewModel GetRequirementDefinitionImpactViewViewModel()
    {
        return requirementDefinitionImpactViewViewModel;
    }
    
    /**
     * Gets the {@linkplain Observable} of {@linkplain Boolean} indicating whether MagicDraw
     * 
     * @return the {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> GetHasOneMagicDrawModelOpen()
    {
        return this.dstController.HasOneDocumentOpenObservable();
    }

    /**
     * Gets the {@linkplain Observable} of {@linkplain Boolean} indicating whether the session to the hub is open 
     * 
     * @return the {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> GetIsSessionOpen()
    {
        return this.isSessionOpen;
    }

    /**
     * Initializes a new {@linkplain MagicDrawImpactViewPanelViewModel}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param dstController the {@linkplain IDstController}
     * @param elementDefinitionImpactViewModel the {@linkplain IElementDefinitionImpactViewViewModel}
     * @param requirementImpactViewModel the {@linkplain IRequirementImpactViewViewModel}
     * @param transferControlViewModel the {@linkplain ITransferControlViewModel}
     * @param contextMenuViewModel the {@linkplain IImpactViewContextMenuViewModel}
     * @param mappingConfigurationService the {@linkplain IMagicDrawMappingConfigurationService}
     * @param magicDrawImpactViewViewModel the {@linkplain IMagicDrawImpactViewViewModel}
     * @param logService the {@linkplain IMagicDrawUILogService}
     */
    public MagicDrawImpactViewPanelViewModel(IHubController hubController, IDstController dstController, 
            IElementDefinitionImpactViewViewModel elementDefinitionImpactViewModel, IRequirementImpactViewViewModel requirementImpactViewModel,
            ITransferControlViewModel transferControlViewModel, IImpactViewContextMenuViewModel contextMenuViewModel,
            IMagicDrawMappingConfigurationService mappingConfigurationService, IMagicDrawImpactViewViewModel magicDrawImpactViewViewModel,
            IMagicDrawUILogService logService)
    {
        super(hubController);
        this.dstController = dstController;
        this.transferControlViewModel = transferControlViewModel;
        this.contextMenuViewModel = contextMenuViewModel;
        this.mappingConfigurationService = mappingConfigurationService;
        this.magicDrawImpactViewViewModel = magicDrawImpactViewViewModel;
        this.logService = logService;
        this.isSessionOpen = this.HubController.GetIsSessionOpenObservable();
        this.elementDefinitionImpactViewViewModel = elementDefinitionImpactViewModel;
        this.requirementDefinitionImpactViewViewModel = requirementImpactViewModel;
    }

    /**
     * Gets the the saved mapping configurations names from the open {@linkplain Iteration}
     * 
     * @return a {@linkplain String} collection of the names of the available {@linkplain ExternalIdentifierMap}
     */
    @Override
    public List<String> GetSavedMappingconfigurationCollection()
    {
        if(!this.HubController.GetIsSessionOpen())
        {
            return new ArrayList<>();
        }
        
        List<String> externalIdentifierMaps = this.HubController.GetAvailableExternalIdentifierMap(DstController.THISTOOLNAME)
                .stream()
                .map(x -> x.getName())
                .sorted()
                .collect(Collectors.toList());

        externalIdentifierMaps.add(0, "");
        return externalIdentifierMaps;
    }

    /**
     * Gets the {@linkplain Callable} of {@linkplain MappingDirection} to call when the user switch {@linkplain MappingDirection}
     * 
     * @return a {@linkplain Callable} of {@linkplain MappingDirection}
     */
    @Override
    public Callable<MappingDirection> GetOnChangeMappingDirectionCallable()
    {
        return () -> this.dstController.ChangeMappingDirection();
    }

    /**
     * Executes when the Save/Load configuration is pressed.
     * Creates a new configuration or loads an existing one based on its name
     * 
     * @param configurationName the name of the configuration to load or create
     * @return a {@linkplain boolean} indicating whether the configuration is a new one
     */
    @Override
    public boolean OnSaveLoadMappingConfiguration(String configurationName)
    {
        boolean isNew = !AreTheseEquals(configurationName, this.mappingConfigurationService.GetExternalIdentifierMap().getName());
        
        if(configurationName.isEmpty())
        {
            return false;
        }

        this.mappingConfigurationService.SetExternalIdentifierMap(this.HubController.GetAvailableExternalIdentifierMap(DstController.THISTOOLNAME)
                .stream().filter(x -> AreTheseEquals(x.getName(), configurationName))
                .findFirst()
                .orElse(this.CreateNewMappingConfiguration(configurationName)));
        
        Task.Run(() -> this.dstController.LoadMapping());
        
        this.logService.Append("The configuration %s is %s ...", configurationName, !isNew ? "reloading" : "loading");
        
        return isNew && this.mappingConfigurationService.GetExternalIdentifierMap().getRevisionNumber() < 1;
    }

    /**
     * Creates a new ExternalIdentifierMap based on the specified name
     * 
     * @param configurationName the String name of the new configuration
     * @return the newly created {@linkplain ExternalIdentifierMap}
     */
    private ExternalIdentifierMap CreateNewMappingConfiguration(String configurationName)
    {
        return this.mappingConfigurationService
                .CreateExternalIdentifierMap(configurationName, this.dstController.OpenDocument().getName()
                        , this.CheckForExistingTemporaryMapping());
    }

    /**
     * Verifies that there is some existing mapping defined in the possibly temporary {@linkplain ExternalIdentifierMap} and asks the user if it wants to keep it
     * 
     * @return a {@linkplain boolean}
     */
    private boolean CheckForExistingTemporaryMapping()
    {
        return this.mappingConfigurationService.IsTheCurrentIdentifierMapTemporary() 
                && !this.mappingConfigurationService.GetExternalIdentifierMap().getCorrespondence().isEmpty()
                && JOptionPane.showConfirmDialog(null, "You have some mapping defined already, do you want to keep it?", "", JOptionPane.YES_NO_OPTION) 
                    == JOptionPane.YES_OPTION;
    }
}
