/*
 * MappingListViewBlockRowViewModel.java
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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import Services.Stereotype.StereotypeService;
import Utils.Stereotypes.Stereotypes;
import cdp4common.commondata.ClassKind;

/**
 * The MappingListViewBlockRowViewModel is the row view model that represents one {@linkplain Class} block in a {@linkplain MappingListView}
 */
public class MappingListViewBlockRowViewModel extends MappingListViewContainerBaseRowViewModel<Class>
{    
    /**
     * Initializes a new {@linkplain MappingListViewBlockRowViewModel}
     * 
     * @param block the represented {@linkplain Class}
     */
    public MappingListViewBlockRowViewModel(Class block)
    {
        super(block, block.getID(), block.getName(), null, ClassKind.ElementDefinition);
    }
    
    /**
     * Computes the contained rows
     */
    @Override
    public void ComputeContainedRows() 
    {
        for (Property property : this.element.getOwnedAttribute())
        {
            if(StereotypeService.Current.DoesItHaveTheStereotype(property, Stereotypes.ValueProperty))
            {
                this.containedRows.add(new MappingListViewBaseRowViewModel(property.getID(),
                        property.getName(), StereotypeService.Current.GetValueRepresentation(property), ClassKind.Parameter));
            }
        }
    }
}