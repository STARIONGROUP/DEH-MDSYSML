/*
 * IMagicDrawObjectBrowserViewModel.java
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

import java.util.Collection;

import org.netbeans.swing.outline.OutlineModel;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import ViewModels.Interfaces.IObjectBrowserBaseViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain IMagicDrawObjectBrowserViewModel} is the interface definition for the {@linkplain MagicDrawObjectBrowserViewModel}
 */
public interface IMagicDrawObjectBrowserViewModel extends IObjectBrowserBaseViewModel
{
    /**
     * Compute eligible rows where the represented {@linkplain Class} can be transfered,
     * and return the filtered collection for feedback application on the tree
     * 
     * @param selectedRow the collection of selected view model {@linkplain ClassRowViewModel}
     */
    void OnSelectionChanged(ClassRowViewModel selectedRow);

    /**
     * Creates the {@linkplain OutlineModel} tree from the provided {@linkplain Collection} of {@linkplain Class}
     * 
     * @param name the name of the root element of the tree
     * @param elements the {@linkplain Collection} of {@linkplain Element}
     */
    void BuildTree(String name, Collection<Element> elements);

    /**
     * Gets the {@linkplain Observable} of {@linkplain ClassRowViewModel} that yields the selected element
     * 
     * @return an {@linkplain Observable} of {@linkplain ClassRowViewModel}
     */
    Observable<ClassRowViewModel> GetSelectedElement();
}
