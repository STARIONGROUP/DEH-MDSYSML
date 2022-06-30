/*
 * MappedElementDefinitionRowViewModel.java
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
package ViewModels.Rows;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import Utils.Operators.Operators;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Enumerations.MappingDirection;
import cdp4common.engineeringmodeldata.ActualFiniteState;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Parameter;

/**
 * The {@linkplain MappedElementDefinitionRowViewModel} is the row view model that represents a mapping between an {@linkplain ElementDefinition }
 */
public class MappedElementDefinitionRowViewModel extends MappedElementRowViewModel<ElementDefinition, Class>
{
    /**
     * Backing field for {@linkplain #GetSelectedActualFiniteState()}
     */
    private HashMap<UUID, ActualFiniteState> selectedActualFiniteStates = new HashMap<>();
        
    /**
     * Initializes a new {@linkplain MappedElementDefinitionRowViewModel}
     * 
     * @param thing the {@linkplain TThing} that is at one end of the mapping
     * @param dstElement the {@linkplain TDstElement} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedElementDefinitionRowViewModel(ElementDefinition thing, Class dstElement, MappingDirection mappingDirection)
    {
        super(thing, ElementDefinition.class, dstElement, mappingDirection);
    }

    /**
     * Initializes a new {@linkplain MappedElementDefinitionRowViewModel} with {@linkplain MappingDirection}.{@code FromDstToHub}
     * 
     * @param dstElement the {@linkplain TDstElement}
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedElementDefinitionRowViewModel(Class dstElement, MappingDirection mappingDirection)
    {
        super(ElementDefinition.class, dstElement, mappingDirection);
    }

    /**
     * Gets the string representation of the represented DST element
     * 
     * @return a {@linkplain String}
     */
    @Override
    public String GetDstElementRepresentation()
    {
        return this.GetElementRepresentation(this.GetDstElement() == null ? "-" : this.GetDstElement().getName(),
                "Block", MappingDirection.FromHubToDst);
    }
    
    /**
     * Gets the string representation of the represented DST element
     * 
     * @return a {@linkplain String}
     */
    @Override
    public String GetHubElementRepresentation()
    {
        return this.GetHubElementRepresentation(ElementDefinition.class);
    }
    
    /**
     * Updates the {@linkplain ActualFiniteState} selected for the state dependent {@linkplain Parameter} where the id is the one provided
     * 
     * @param parameterId the {@linkplain UUID} of the target {@linkplain Parameter}
     * @param actualFiniteState the selected {@linkplain ActualFiniteState}
     */
    public void SetActualFiniteStateFor(UUID parameterId, ActualFiniteState actualFiniteState)
    {
        Parameter parameter = this.GetHubElement() != null 
                ? this.GetHubElement().getParameter().stream().filter(x -> Operators.AreTheseEquals(x.getIid(), parameterId)).findFirst().orElse(null) 
                : null;
        
        if(parameter != null && parameter.getStateDependence() != null 
                && parameter.getStateDependence().getActualState().stream()
                        .anyMatch(x -> Operators.AreTheseEquals(actualFiniteState.getIid(), x.getIid())))
        {
            this.selectedActualFiniteStates.put(parameterId, actualFiniteState);
        }   
    }
    
    /**
     * Gets the {@linkplain ActualFiniteState} for the {@linkplain Parameter} that has the specified {@linkplain UUID}
     * 
     * @param parameterId the parameter id
     * @return an {@linkplain ActualFiniteState}
     */
    public ActualFiniteState GetSelectedActualFiniteStateFor(UUID parameterId)
    {
        return this.selectedActualFiniteStates.get(parameterId);
    }
    
    /**
     * Gets all selected {@linkplain ActualFiniteStates} as a {@linkplain Collection}
     * 
     * @return a {@linkplain Collection} of {@linkplain Entry} of {@linkplain UUID} and {@linkplain ActualFiniteState}
     */
    public Collection<Entry<UUID, ActualFiniteState>> GetSelectedActualFiniteState()
    {
        return this.selectedActualFiniteStates.entrySet();
    }
}
