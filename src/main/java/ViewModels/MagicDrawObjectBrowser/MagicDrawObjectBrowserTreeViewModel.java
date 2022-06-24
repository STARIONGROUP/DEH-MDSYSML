/*
 * MagicDrawObjectBrowserTreeViewModel.java
 *
 * Copyright (c) 2020-2021 RHEA System S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
 *
 * This file is part of DEH-CommonJ
 *
 * The DEH-CommonJ is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * The DEH-CommonJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ViewModels.MagicDrawObjectBrowser;

import java.util.Collection;

import javax.swing.tree.TreeModel;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import ViewModels.MagicDrawObjectBrowser.Rows.RootRowViewModel;
import ViewModels.ObjectBrowser.BrowserTreeBaseViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.ElementDefinitionBrowserTreeRowViewModel;

/**
 * The {@linkplain ElementDefinitionBrowserTreeRowViewModel} is the {@linkplain TreeModel} for the element definition browser
 */
public class MagicDrawObjectBrowserTreeViewModel extends BrowserTreeBaseViewModel
{
    /**
     * Gets the root element of the tree
     * 
     * @return an {@linkplain Object}
     */
    @Override
    public Object getRoot()
    {
        return this.root;
    }
    
    /**
     * Initializes a new {@linkplain MagicDrawObjectBrowserTreeRowViewModel}
     * 
     * @param elements the {@linkplain Collection} {@linkplain Class} that composes the tree
     */
    public MagicDrawObjectBrowserTreeViewModel(String modelName, Collection<Element> elements)
    {
        this.root = new RootRowViewModel(modelName, elements);
    }
    
    /**
     * Initializes a new {@linkplain MagicDrawObjectBrowserTreeRowViewModel}
     * 
     * @param rootRowViewModel the {@linkplain RootRowViewModel}
     */
    public MagicDrawObjectBrowserTreeViewModel(RootRowViewModel rootRowViewModel)
    {
        this.root = rootRowViewModel;
    }
}
