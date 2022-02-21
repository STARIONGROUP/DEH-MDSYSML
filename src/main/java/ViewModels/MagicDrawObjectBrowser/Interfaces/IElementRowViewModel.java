/*
 * IClassRowViewModel.java
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
package ViewModels.MagicDrawObjectBrowser.Interfaces;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import Utils.Stereotypes.Stereotypes;
import ViewModels.MagicDrawObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain IElementRowViewModel} is the interface definition for all {@linkplain ElementRowViewModel}
 * 
 * @param TElement the type of {@linkplain Element} this row view model represents
 */
public interface IElementRowViewModel<TElement extends Element> extends IRowViewModel
{
    /**
     * Gets the string representation of the type of thing represented
     * 
     * @return a {@linkplain Stereotypes}
     */
    Stereotypes GetClassKind();

    /**
     * Gets the name of the {@linkplain Element} represented by this row view model
     * 
     * @return the represented {@linkplain Element}
     */
    TElement GetElement();
}
