/*
 * IMagicDrawSelectionService.java
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
package Services.MagicDrawSelection;

import java.util.ArrayList;
import java.util.Collection;

import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import cdp4common.commondata.Thing;

/**
 * The {@linkplain IMagicDrawSelectionService} is the interface definition for the {@linkplain MagicDrawSelectionService}
 */
public interface IMagicDrawSelectionService
{
    /**
     * Gets the selected items from the {@linkplain ISelectionService}
     * 
     * @param <TElement> the type of {@linkplain Element} to return
     * @param elementClass the {@linkplain java.lang.Class} of {@linkplain #TElement}
     * @return a {@linkplain Collection} of {@linkplain Class}
     */
    <TElement extends Element> Collection<TElement> GetDstSelection(java.lang.Class<TElement> elementClass);

    /**
     * Sets the {@linkplain Tree} active browser in order to compute the selection when callnig {@linkplain #GetDstSelection()}
     * 
     * @param activeBrowserTree the {@linkplain Tree}
     */
    void SetActiveBrowser(Tree activeBrowserTree);

    /**
     * Sorts the provided collection of {@linkplain Thing}
     * 
     * @param elementsToSort the {@linkplain ArrayList} of {@linkplain Thing} to sort
     * @return a {@linkplain Collection}of mappable {@linkplain Thing}s
     */
    Collection<Thing> GetHubSelection();
}
