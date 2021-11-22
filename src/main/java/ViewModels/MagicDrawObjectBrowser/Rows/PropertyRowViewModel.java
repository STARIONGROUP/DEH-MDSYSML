/*
 * PropertyRowViewModel.java
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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;

/**
 * The {@linkplain PropertyRowViewModel} is the abstract row view model for the {@linkplain ReferencePropertyRowViewModel} and the {@linkplain ValuePropertyRowViewModel}
 */
public abstract class PropertyRowViewModel extends ElementRowViewModel<Property>
{
    /**
     * Initializes a new {@linkplain ReferencePropertyRowViewModel}
     * 
     * @param parent the parent {@linkplain IElementRowViewModel} view model
     * @param property the represented {@linkplain Property}
     */
    public PropertyRowViewModel(IElementRowViewModel<Class> parent, Property property)
    {
        super(parent, property);
    }
}
