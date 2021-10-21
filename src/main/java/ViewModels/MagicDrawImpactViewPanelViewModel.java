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

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import ViewModels.Interfaces.IElementDefinitionImpactViewViewModel;
import ViewModels.Interfaces.IMagicDrawImpactViewPanelViewModel;
import ViewModels.Interfaces.IRequirementImpactViewViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain MagicDrawImpactViewPanelViewModel} is the dst adapter implementation of the 
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
     */
    public MagicDrawImpactViewPanelViewModel(IHubController hubController, IDstController dstController, 
            IElementDefinitionImpactViewViewModel elementDefinitionImpactViewModel, IRequirementImpactViewViewModel requirementImpactViewModel)
    {
        super(hubController);
        this.dstController = dstController;
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
            return new ArrayList<String>();
        }
        
        List<String> externalIdentifierMaps = this.HubController.GetOpenIteration()
                .getExternalIdentifierMap()
                .stream()
                .map(x -> x.getName())
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
}
