/*
 * MappedHubRequirementRowViewModel.java
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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

/**
 * The {@linkplain MappedHubRequirementRowViewModel} is the row view model that represents a mapping between 
 * a {@linkplain cdp4common.engineeringmodeldata.Requirement} and a {@linkplain Requirement}
 */
public class MappedHubRequirementRowViewModel extends MappedRequirementBaseRowViewModel<cdp4common.engineeringmodeldata.Requirement>
{    
    /**
     * Initializes a new {@linkplain MappedHubRequirementRowViewModel}
     * 
     * @param thing the {@linkplain TThing} that is at one end of the mapping
     * @param dstElement the {@linkplain TDstElement} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedHubRequirementRowViewModel(cdp4common.engineeringmodeldata.Requirement thing, Class dstElement, MappingDirection mappingDirection)
    {
        super(thing, dstElement, mappingDirection, cdp4common.engineeringmodeldata.Requirement.class);
    }

    /**
     * Initializes a new {@linkplain MappedHubRequirementRowViewModel} with {@linkplain MappingDirection}.{@code FromDstToHub}
     * 
     * @param dstElement the {@linkplain TDstElement} that is at one end of the mapping
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedHubRequirementRowViewModel(Class dstElement, MappingDirection mappingDirection)
    {
        super(dstElement, mappingDirection, cdp4common.engineeringmodeldata.Requirement.class);
    }
    
    /**
     * Initializes a new {@linkplain MappedHubRequirementRowViewModel} with {@linkplain MappingDirection}.{@code FromHubToDst}
     * 
     * @param hubElement the {@linkplain Requirement} that is at one end of the mapping
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedHubRequirementRowViewModel(cdp4common.engineeringmodeldata.Requirement hubElement, MappingDirection mappingDirection)
    {
        super(hubElement, null, mappingDirection, cdp4common.engineeringmodeldata.Requirement.class);
    }
}
