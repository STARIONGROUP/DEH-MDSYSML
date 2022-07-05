/*
 * MappingListViewSysMLRequirementRowViewModel.java
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
package ViewModels.MappingListView.Rows;


import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Services.Stereotype.StereotypeService;
import cdp4common.commondata.ClassKind;

/**
 * The {@linkplain MappingListViewSysMLRequirementRowViewModel} is the row view model that represents one {@linkplain Class} requirement in a {@linkplain MappingListView}
 */
public class MappingListViewSysMLRequirementRowViewModel extends MappingListViewContainerBaseRowViewModel<Class>
{    
    /**
     * Initializes a new {@linkplain MappingListViewSysMLRequirementRowViewModel}
     * 
     * @param requirement the represented {@linkplain Class} requirement
     */
    public MappingListViewSysMLRequirementRowViewModel(Class requirement)
    {
        super(requirement, requirement.getID(), String.format("%s-%s", StereotypeService.Current().GetRequirementId(requirement), 
                requirement.getName()), StereotypeService.Current().GetRequirementText(requirement), ClassKind.Requirement);
    }
    
    /**
     * Computes the contained rows
     */
    @Override
    public void ComputeContainedRows() 
    {
    	// Added comment to satisfy the code smell raised by the rule 1186.
    	// This method is empty because nothing has to be done there.
    }
}
