/*
 * MappedRequirementRowViewModel.java
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

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Enumerations.MappingDirection;
/**
 * The MappedRequirementBaseRowViewModel represents a mapping between a {@linkplain cdp4common.engineeringmodeldata.Requirement} a stereotyped {@linkplain Class} as requirement
 */
public class MappedRequirementRowViewModel extends MappedElementRowViewModel<cdp4common.engineeringmodeldata.Requirement, Class>
{
    /**
     * Initializes a new {@linkplain MappedRequirementRowViewModel}
     * 
     * @param thing the {@linkplain cdp4common.engineeringmodeldata.Requirement} that is at one end of the mapping
     * @param dstElement the {@linkplain Requirement} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedRequirementRowViewModel(cdp4common.engineeringmodeldata.Requirement thing, Class dstElement, MappingDirection mappingDirection)
    {
        super(thing, cdp4common.engineeringmodeldata.Requirement.class, dstElement, mappingDirection);
    }

    /**
     * Initializes a new {@linkplain MappedRequirementRowViewModel} with {@linkplain MappingDirection}.{@code FromDstToHub}
     * 
     * @param dstElement the {@linkplain Class} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedRequirementRowViewModel(Class dstElement, MappingDirection mappingDirection)
    {
        super(cdp4common.engineeringmodeldata.Requirement.class, dstElement, mappingDirection);
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
                "Requirement", MappingDirection.FromHubToDst);
    }
    
    /**
     * Gets the string representation of the represented DST element
     * 
     * @return a {@linkplain String}
     */
    @Override
    public String GetHubElementRepresentation()
    {
        return this.GetHubElementRepresentation(this.GetTThingClass());
    }    
}
