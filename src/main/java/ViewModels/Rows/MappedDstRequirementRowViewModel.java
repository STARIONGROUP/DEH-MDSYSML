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

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Enumerations.MappingDirection;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain MappedDstRequirementRowViewModel} is the row view model that represents a mapping between an {@linkplain RequirementsSpecification}
 */
public class MappedDstRequirementRowViewModel extends MappedRequirementBaseRowViewModel<RequirementsSpecification>
{
    /**
     * Initializes a new {@linkplain MappedDstRequirementRowViewModel}
     * 
     * @param thing the {@linkplain TThing} that is at one end of the mapping
     * @param dstElement the {@linkplain TDstElement} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedDstRequirementRowViewModel(RequirementsSpecification thing, Class dstElement, MappingDirection mappingDirection)
    {
        super(thing, dstElement, mappingDirection, RequirementsSpecification.class);
    }

    /**
     * Initializes a new {@linkplain MappedRequirementsSpecificationRowViewModel} with {@linkplain MappingDirection}.{@code FromDstToHub}
     * 
     * @param dstElement the {@linkplain TDstElement} that is at the other end
     * @param mappingDirection the {@linkplain MappingDirection} to which this mapping applies to
     */
    public MappedDstRequirementRowViewModel(Class dstElement, MappingDirection mappingDirection)
    {
        super(dstElement, mappingDirection, RequirementsSpecification.class);
    }
}
