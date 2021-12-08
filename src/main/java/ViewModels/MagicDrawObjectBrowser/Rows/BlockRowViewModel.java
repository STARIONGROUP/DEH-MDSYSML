/*
 * BlockRowViewModel.java
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
package ViewModels.MagicDrawObjectBrowser.Rows;

import com.nomagic.magicdraw.sysml.util.MDCustomizationForSysMLProfile;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import Utils.Stereotypes.Stereotypes;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;

/**
 * The {@linkplain BlockRowViewModel} is the row view model that represents a SysML block
 */
public class BlockRowViewModel extends ClassRowViewModel
{
    /**
     * Initializes a new {@linkplain BlockRowViewModel}
     * 
     * @param parent the {@linkplain IElementRowViewModel} parent view model of this row view model
     * @param element the {@linkplain Class} represented
     */
    public BlockRowViewModel(IElementRowViewModel<?> parent, Class element)
    {
        super(parent, element);
        this.ComputeContainedRows();
    }

    /**
     * Gets the string representation of the type of thing represented
     * 
     * @return a {@linkplain Stereotypes}
     */
    @Override
    public Stereotypes GetClassKind()
    {
        return Stereotypes.Block;
    }
        
    /**
     * Computes this row view model contained rows
     */
    @Override
    public void ComputeContainedRows()
    {
        for (Property property : this.GetElement().getOwnedAttribute())
        {
            if(MDCustomizationForSysMLProfile.isReferenceProperty(property))
            {
                this.GetContainedRows().add(new ReferencePropertyRowViewModel(this, property));
            }
            else if(MDCustomizationForSysMLProfile.isValueProperty(property))
            {
                this.GetContainedRows().add(new ValuePropertyRowViewModel(this, property));
            }
            else if(MDCustomizationForSysMLProfile.isPartProperty(property))
            {
                this.GetContainedRows().add(new PartPropertyRowViewModel(this, property));
            }
        }        
    }
}
