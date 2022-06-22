/*
 * ValuePropertyRowViewModel.java
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

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import Services.Stereotype.StereotypeService;
import Utils.Stereotypes.Stereotypes;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;

/**
 * The {@linkplain ValuePropertyRowViewModel} is a concrete row view model of {@linkplain PropertyRowViewModel}
 * that represents a reference property
 */
public class ValuePropertyRowViewModel extends PropertyRowViewModel
{
    /**
     * Initializes a new {@linkplain ReferencePropertyRowViewModel}
     * 
     * @param parent the parent {@linkplain IElementRowViewModel} view model
     * @param property the represented {@linkplain Property}
     */
    public ValuePropertyRowViewModel(IElementRowViewModel<?> parent, Property property)
    {
        super(parent, property);
        this.SetName(String.format("%s = %s", this.GetName(), StereotypeService.Current().GetValueRepresentation(property)));
    }

    /**
     * Gets the string representation of the type of thing represented
     * 
     * @return a {@linkplain Stereotypes}
     */
    @Override
    public Stereotypes GetClassKind()
    {
        return Stereotypes.ValueProperty;
    }
}
