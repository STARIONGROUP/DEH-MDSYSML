/*
 * MappingListViewSysMLElementCellRenderer.java
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
package ViewModels.MappingListView.Renderers;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import ViewModels.MappingListView.Rows.*;
import Services.Stereotype.StereotypeService;
import Utils.Stereotypes.Stereotypes;

/**
 * The {@linkplain MappingListViewSysMLElementCellRenderer} is the {@linkplain DefaultTableCellEditor} for the 
 * {@linkplain MappedElementListView} where the represented element is an {@linkplain DefinedThing}
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MappingListViewSysMLElementCellRenderer extends MappingListViewElementBaseCellRenderer<Class>
{
    /**
     * Initializes a new {@linkplain MappingListViewSysMLElementCellRenderer}
     */
    public MappingListViewSysMLElementCellRenderer()
    {
        super(x -> StereotypeService.Current.DoesItHaveTheStereotype(x, Stereotypes.Block) 
                ? new MappingListViewBlockRowViewModel(x)
                : new MappingListViewSysMLRequirementRowViewModel(x));
    }
}
