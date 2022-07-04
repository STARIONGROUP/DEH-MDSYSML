/*
 * MappedElementDefinitionListViewCellRendererProvider.java
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
package Renderers;

import java.util.HashMap;
import java.util.UUID;

import Enumerations.MappingDirection;
import Utils.Ref;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;

/**
 * The {@linkplain MappedElementDefinitionListViewCellRendererProvider} provides an easyer way to cache and retrive views that should display
 * represented {@linkplain ElementDefinition} that holds state dependent {@linkplain Parameter}
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MappedElementDefinitionListViewCellRendererProvider extends MappedElementListViewBaseCellRendererProvider
{
    /**
     * Static final instance of this provider
     */
    public static final MappedElementDefinitionListViewCellRendererProvider Current = new MappedElementDefinitionListViewCellRendererProvider();
    
    /**
     * The dictionary of {@linkplain MappedElementDefinitionListViewCellDisplayTemplate} 
     * for each {@linkplain ElementDefinition} where the key is the {@linkplain ElementDefinition} id
     */
    private HashMap<UUID, MappedElementDefinitionListViewCellDisplayTemplate> cellDisplayTemplate = new HashMap<>();

    /**
     * Tries to get the {@linkplain MappedElementDefinitionListViewCellDisplayTemplate} view for the provided row view model
     * 
     * @param rowViewModel the {@linkplain Object} row view model
     * @param columnIndex the column index
     * @param refRow the {@linkplain Ref} of {@linkplain MappedElementDefinitionListViewCellDisplayTemplate}
     * @return a {@linkplain boolean}
     */
    public boolean TryGetComponent(Object rowViewModel, int columnIndex, Ref<MappedElementDefinitionListViewCellDisplayTemplate> refRow)
    {
        if(rowViewModel instanceof MappedElementDefinitionRowViewModel
                && this.ShouldDisplayParameters(columnIndex, (MappedElementDefinitionRowViewModel)rowViewModel))
        {
            refRow.Set(this.GetDisplayTemplate((MappedElementDefinitionRowViewModel) rowViewModel));
        }
        
        return refRow.HasValue();
    }

    /**
     * Gets a {@linkplain MappedElementDefinitionListViewCellDisplayTemplate} to represent the represented {@linkplain ElementDefinition} 
     * by the provided {@linkplain MappedElementDefinitionRowViewModel}
     * 
     * @param isSelected a value indicating whether the current row is selected
     * @param rowViewModel the {@linkplain MappedElementDefinitionRowViewModel}
     * @return a {@linkplain MappedElementDefinitionListViewCellDisplayTemplate}
     */
    private MappedElementDefinitionListViewCellDisplayTemplate GetDisplayTemplate(MappedElementDefinitionRowViewModel rowViewModel)
    {
        UUID elementDefinitionId = rowViewModel.GetHubElement().getIid();
        
        MappedElementDefinitionListViewCellDisplayTemplate displayTemplate = this.cellDisplayTemplate.getOrDefault(elementDefinitionId, null);
        
        if(displayTemplate == null)
        {
            displayTemplate = new MappedElementDefinitionListViewCellDisplayTemplate();
            this.cellDisplayTemplate.put(elementDefinitionId, displayTemplate);
        }
        
        displayTemplate.UpdateProperties(rowViewModel);
        return displayTemplate;
    }

    /**
     * Gets a value indicating whether a {@linkplain MappedElementDefinitionListViewCellDisplayTemplate} should be used to display the current cell
     * 
     * @param columnIndex the {@linkplain int} column index
     * @param rowViewModel the {@linkplain MappedElementDefinitionRowViewModel}
     * @return a {@linkplain boolean}
     */
    private boolean ShouldDisplayParameters(int columnIndex, MappedElementDefinitionRowViewModel rowViewModel)
    {
        return columnIndex == 1 && rowViewModel != null 
                && rowViewModel.GetMappingDirection() == MappingDirection.FromHubToDst
                && rowViewModel.GetHubElement().getParameter().stream().anyMatch(x -> x.getStateDependence() != null);
    }
    
    /**
     * Clears the cache of Components managed by this provider
     */
    public void ClearCache()
    {
        this.cellDisplayTemplate.clear();
    }
}
