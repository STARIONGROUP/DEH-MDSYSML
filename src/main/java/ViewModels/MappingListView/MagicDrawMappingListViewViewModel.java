/*
 * MagicDrawMappingListViewViewModel.java
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
package ViewModels.MappingListView;

import ViewModels.MappingListView.MappingListViewViewModel;
import ViewModels.MappingListView.Interfaces.IMappingListViewViewModel;
import ViewModels.Rows.MappedElementRowViewModel;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.tree.TreeModel;

import org.apache.commons.lang3.tuple.Triple;
import org.netbeans.swing.outline.DefaultOutlineModel;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;

/**
 * The {@linkplain MagicDrawMappingListViewViewModel} is the main view model for the {@linkplain MagicDrawMappingListView}
 */
public class MagicDrawMappingListViewViewModel extends MappingListViewViewModel<IDstController> implements IMappingListViewViewModel
{
    /**
     * The {@linkplain IMagicDrawTransactionService}
     */
    private IMagicDrawTransactionService transactionService;

    /**
     * Initializes a new {@linkplain MagicDrawMappingListViewViewModel}
     * 
     * @param dstController the {@linkplain IDstController}
     * @param hubController the {@linkplain IHubController}
     * @param transactionService the {@linkplain IMagicDrawTransactionService}
     */
    public MagicDrawMappingListViewViewModel(IDstController dstController, IHubController hubController, IMagicDrawTransactionService transactionService)
    {
        super(dstController, hubController);
        this.transactionService = transactionService;
        this.InitializeObservable();
    }

    /**
     * Updates this view model {@linkplain TreeModel}
     * 
     * @param shouldDisplayTree a value indicating whether the tree should be made visible
     */
    @Override
    protected void UpdateBrowserTrees(Boolean shouldDisplayTree)
    {
        this.isTheTreeVisible.Value(shouldDisplayTree);
        
        if(shouldDisplayTree)
        {
            this.browserTreeModel.Value(DefaultOutlineModel.createOutlineModel(
                    new MappingListViewTreeViewModel<Class>(this.SortMappedElements()), 
                    new MappingListViewTreeRowViewModel<Class>(Class.class), true));
        }
    }

    /**
     * Sorts the mapped elements
     * 
     * @return a {@linkplain Collection} of {@linkplain Triple} of the {@linkplain #TDstElement} the {@linkplain MappingDirection} and the {@linkplain Thing}
     */
    private Collection<Triple<? extends Class, MappingDirection, ? extends Thing>> SortMappedElements()
    {
        ArrayList<MappedElementRowViewModel<? extends DefinedThing, ? extends Class>> allElements = new ArrayList<>(this.dstController.GetHubMapResult());            
        allElements.addAll(this.dstController.GetDstMapResult());
        
        ArrayList<Triple<? extends Class, MappingDirection, ? extends Thing>> result = new ArrayList<>();
        
        for (MappedElementRowViewModel<? extends DefinedThing, ? extends Class> mappedElement : allElements)
        {
            if(mappedElement.GetMappingDirection() == MappingDirection.FromDstToHub)
            {
                result.add(Triple.of(mappedElement.GetDstElement(), mappedElement.GetMappingDirection(), 
                        mappedElement.GetHubElement().getOriginal() == null 
                            ? mappedElement.GetHubElement() 
                            : mappedElement.GetHubElement().getOriginal()));
            }
            else
            {
                result.add(Triple.of(this.transactionService.IsCloned(mappedElement.GetDstElement())
                        ? this.transactionService.GetClone(mappedElement.GetDstElement()).GetOriginal()
                        : mappedElement.GetDstElement(), mappedElement.GetMappingDirection(), 
                        mappedElement.GetHubElement()));
            }
        }
        
        return result;
    }
}
