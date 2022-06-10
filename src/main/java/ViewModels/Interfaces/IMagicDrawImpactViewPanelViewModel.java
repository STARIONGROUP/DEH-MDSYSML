/*
 * IMagicDrawImpactViewPanelViewModel.java
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
package ViewModels.Interfaces;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import Enumerations.MappingDirection;
import ViewModels.MagicDrawImpactViewPanelViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain IMagicDrawImpactViewPanelViewModel} is the main interface definition for the {@linkplain MagicDrawImpactViewPanelViewModel} 
 */
public interface IMagicDrawImpactViewPanelViewModel extends IViewModel
{
    /**
     * Executes when the Save/Load configuration is pressed.
     * Creates a new configuration or loads an existing one based on its name
     * 
     * @param configurationName the name of the configuration to load or create
     * @return a {@linkplain boolean} indicating whether the configuration is a new one
     */
    boolean OnSaveLoadMappingConfiguration(String configurationName);

    /**
     * Gets the {@linkplain Observable} of {@linkplain Boolean} indicating whether the session to the hub is open 
     * 
     * @return the {@linkplain Observable} of {@linkplain Boolean}
     */
    Observable<Boolean> GetIsSessionOpen();

    /**
     * Gets the {@linkplain Callable} of {@linkplain MappingDirection} to call when the user switch {@linkplain MappingDirection}
     * 
     * @return a {@linkplain Callable} of {@linkplain MappingDirection}
     */
    Callable<MappingDirection> GetOnChangeMappingDirectionCallable();

    /**
     * Gets the the saved mapping configurations names from the open {@linkplain Iteration}
     * 
     * @return a {@linkplain String} collection of the names of the available {@linkplain ExternalIdentifierMap}
     */
    List<String> GetSavedMappingconfigurationCollection();

    /**
     * Gets the {@linkplain IRequirementImpactViewViewModel} requirementDefinitionImpactViewViewModel
     * 
     * @return the {@linkplain IRequirementImpactViewViewModel}
     */
    IRequirementImpactViewViewModel GetRequirementDefinitionImpactViewViewModel();

    /**
     * Gets the {@linkplain IElementDefinitionImpactViewViewModel} elementDefinitionImpactViewViewModel
     * 
     * @return the {@linkplain IElementDefinitionImpactViewViewModel}
     */
    IElementDefinitionImpactViewViewModel GetElementDefinitionImpactViewViewModel();

    /**
     * Gets the {@linkplain ITransferControlViewModel}
     * 
     * @return a {@linkplain ITransferControlViewModel}
     */
    ITransferControlViewModel GetTransferControlViewModel();

    /**
     * Gets the {@linkplain IImpactViewContextMenuViewModel} view model for the context menus
     * 
     * @return a {@linkplain IImpactViewContextMenuViewModel}
     */
    IImpactViewContextMenuViewModel GetContextMenuViewModel();

    /**
     * Gets the {@linkplain Observable} of {@linkplain Boolean} indicating whether MagicDraw
     * 
     * @return the {@linkplain Observable} of {@linkplain Boolean}
     */
    Observable<Boolean> GetHasOneMagicDrawModelOpen();

    /**
     * Gets the {@linkplain IElementDefinitionImpactViewViewModel} elementDefinitionImpactViewViewModel
     * 
     * @return the {@linkplain IElementDefinitionImpactViewViewModel}
     */
    IMagicDrawImpactViewViewModel GetMagicDrawImpactViewViewModel();

    /**
     * Gets a value indicating whether the Impact view can load mapping configurations
     * 
     * @return a {@linkplain boolean}
     */
    boolean CanLoadMappingConfiguration();
}
