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

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import Reactive.ObservableValue;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import ViewModels.MagicDrawObjectBrowser.MagicDrawObjectBrowserTreeRowViewModel;
import ViewModels.MagicDrawObjectBrowser.MagicDrawObjectBrowserTreeViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IMagicDrawObjectBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ElementRowViewModel;
import Views.MagicDrawObjectBrowser;
import io.reactivex.Observable;

/**
 * The {@linkplain MagicDrawObjectBrowserViewModel} is the view model for the MagicDraw object browser {@linkplain MagicDrawObjectBrowser}
 */
public class MagicDrawObjectBrowserViewModel extends ObjectBrowserBaseViewModel<ElementRowViewModel<?>> implements IMagicDrawObjectBrowserViewModel
{
    /**
     * The {@linkplain IMagicDrawSessionService}
     */
    protected final IMagicDrawSessionService sessionService;

    /**
     * The {@linkplain IMagicDrawTransactionService}
     */
    protected final IMagicDrawTransactionService transactionService;
    
    /**
     * Backing field for {@linkplain GetSelectedElement}
     */
    private ObservableValue<ElementRowViewModel<?>> selectedElement = new ObservableValue<>();
        
    /**
     * Gets the {@linkplain Observable} of {@linkplain ClassRowViewModel} that yields the selected element
     * 
     * @return an {@linkplain Observable} of {@linkplain ClassRowViewModel}
     */
    @Override
    public Observable<ElementRowViewModel<?>> GetSelectedElement()
    {
        return this.selectedElement.Observable();
    }
    
    /**
     * Initializes a new {@linkplain MagicDrawObjectBrowser}
     * 
     * @param sessionService the {@linkplain IMagicDrawSessionService}
     * @param transactionService the {@linkplain IMagicDrawTransactionService}
     */
    public MagicDrawObjectBrowserViewModel(IMagicDrawSessionService sessionService, IMagicDrawTransactionService transactionService)
    {
        this.sessionService = sessionService;
        this.transactionService = transactionService;
    }
    
    /**
     * Occurs when the selection changes
     * 
     * @param selectedRow the selected {@linkplain ElementRowViewModel}
     */
    @Override
    public void OnSelectionChanged(ElementRowViewModel<?> selectedRow)
    {
        this.selectedElement.Value(selectedRow);
    }
            
    /**
     * Creates the {@linkplain OutlineModel} tree from the provided {@linkplain Collection} of {@linkplain Class}
     * 
     * @param elements the {@linkplain Collection} of {@linkplain Element}
     */
    @Override
    public void BuildTree(Collection<Element> elements)
    {
        this.browserTreeModel.Value(DefaultOutlineModel.createOutlineModel(
                new MagicDrawObjectBrowserTreeViewModel(this.sessionService.GetProjectName(), elements, this.transactionService),
                new MagicDrawObjectBrowserTreeRowViewModel(), true));
    
        this.isTheTreeVisible.Value(true);
    }

    /**
     * Creates the {@linkplain OutlineModel} tree
     */
    public void BuildTree()
    {
        this.BuildTree(this.sessionService.GetProjectElements());
    }
    
    /**
     * Updates this view model {@linkplain TreeModel}
     * 
     * @param isConnected a value indicating whether the session is open
     */
    protected void UpdateBrowserTrees(Boolean isConnected)
    {
    	// Added comment to satisfy the code smell raised by the rule 1186.
    	// This method is empty because nothing has to be done there.
    }
}
