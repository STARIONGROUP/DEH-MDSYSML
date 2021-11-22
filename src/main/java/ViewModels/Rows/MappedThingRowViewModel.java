/*
 * MappedThingRowViewModel.java
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

import Enumerations.MappingDirection;

/**
 * The {@linkplain MappedThingRowViewModel} is the row view model that represents a mapping done between the Hub and MagicDraw
 */
public class MappedThingRowViewModel
{
    /**
     * Backing field for {@linkplain GetMappingDirection} and {@linkplain SetMappingDirection}
     */
    private MappingDirection mappingDirection;
    
    /**
     * Gets the {@linkplain MappingDirection} that defines the mapping represented by this row
     * 
     * @return a {@linkplain MappingDirection}
     */
    public MappingDirection GetMappingDirection()
    {
        return this.mappingDirection;
    }
    
    /**
     * Sets the {@linkplain MappingDirection} that defines the mapping represented by this row
     * 
     * @param mappingDirection the {@linkplain MappingDirection} to set
     */
    public void SetMappingDirection(MappingDirection mappingDirection)
    {
        this.mappingDirection = mappingDirection;
    }
    
    /**
     * Backing field for {@linkplain GetHubThingName} and {@linkplain SetHubThingName}
     */
    private String hubThingName;
    
    /**
     * Gets the name of the Hub thing
     * 
     * @return a {@linkplain String}
     */
    public String GetHubThingName()
    {
        return this.hubThingName;
    }
    
    /**
     * Sets the name of the Hub thing
     * 
     * @param hubThingName the name to set
     */
    public void SetHubThingName(String hubThingName)
    {
        this.hubThingName = hubThingName;
    }
    /**
     * Backing field for {@linkplain GetDstThingName} and {@linkplain SetDstThingName}
     */
    private String dstThingName;
    
    /**
     * Gets the name of the DST thing
     * 
     * @return a {@linkplain String}
     */
    public String GetDstThingName()
    {
        return this.dstThingName;
    }
    
    /**
     * Sets the name of the DST thing
     * 
     * @param dstThingName the name to set
     */
    public void SetDstThingName(String dstThingName)
    {
        this.dstThingName = dstThingName;
    }
    
    /**
     * Initializes a new MappedThingRowViewModel 
     * 
     * @param dstThingName the name of the DST thing
     * @param HubThingName the name of the Hub thing
     * @param mappingDirection the mapping direction represented by this row
     */
    public MappedThingRowViewModel(String dstThingName, String HubThingName, MappingDirection mappingDirection)
    {
        this.dstThingName = dstThingName;
        this.hubThingName = HubThingName;
        this.mappingDirection = mappingDirection;        
    }
}
