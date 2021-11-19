/*
 * MagicDrawObjectBrowserViewModel.java
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
package ViewModels;

import java.util.Collection;

import javax.swing.tree.TreeModel;

import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;

import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import Reactive.ObservableValue;
import ViewModels.MagicDrawObjectBrowser.MagicDrawObjectBrowserTreeRowViewModel;
import ViewModels.MagicDrawObjectBrowser.MagicDrawObjectBrowserTreeViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IMagicDrawObjectBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.BlockRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.RequirementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IThingRowViewModel;
import Views.MagicDrawObjectBrowser;
import cdp4common.commondata.Thing;
import io.reactivex.Observable;

/**
 * The {@linkplain MagicDrawObjectBrowserViewModel} is the view model for the MagicDraw object browser {@linkplain MagicDrawObjectBrowser}
 */
public class MagicDrawObjectBrowserViewModel extends ObjectBrowserBaseViewModel implements IMagicDrawObjectBrowserViewModel
{
    /**
     * Backing field for {@linkplain GetSelectedElement}
     */
    private ObservableValue<ClassRowViewModel> selectedElement = new ObservableValue<ClassRowViewModel>(ClassRowViewModel.class);
    
    /**
     * Gets the {@linkplain Observable} of {@linkplain ClassRowViewModel} that yields the selected element
     * 
     * @return an {@linkplain Observable} of {@linkplain ClassRowViewModel}
     */
    @Override
    public Observable<ClassRowViewModel> GetSelectedElement()
    {
        return this.selectedElement.Observable();
    }
    
    /**
     * Occurs when the selection changes
     * 
     * @param selectedRow the selected {@linkplain ClassRowViewModel}
     */
    @Override
    public void OnSelectionChanged(ClassRowViewModel selectedRow)
    {
        if(selectedRow instanceof BlockRowViewModel || selectedRow instanceof RequirementRowViewModel)
        {
            this.selectedElement.Value(selectedRow);
        }            
    }
            
    /**
     * Creates the {@linkplain OutlineModel} tree from the provided {@linkplain Collection} of {@linkplain Class}
     * 
     * @param name the name of the root element of the tree
     * @param elements the {@linkplain Collection} of {@linkplain Element}
     */
    @Override
    public void BuildTree(String name, Collection<Element> elements)
    {
        this.BrowserTreeModel.Value(DefaultOutlineModel.createOutlineModel(
                new MagicDrawObjectBrowserTreeViewModel(name, elements),
                new MagicDrawObjectBrowserTreeRowViewModel(), true));
    
        this.IsTheTreeVisible.Value(true);
    }


    /**
     * Updates this view model {@linkplain TreeModel}
     * 
     * @param isConnected a value indicating whether the session is open
     */
    protected void UpdateBrowserTrees(Boolean isConnected)
    {
    }
}
