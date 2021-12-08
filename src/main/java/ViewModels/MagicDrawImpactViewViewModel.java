/*
 * MagicDrawImpactViewViewModel.java
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

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Utils.Ref;
import ViewModels.Interfaces.IMagicDrawImpactViewViewModel;
import ViewModels.MagicDrawObjectBrowser.MagicDrawObjectBrowserTreeRowViewModel;
import ViewModels.MagicDrawObjectBrowser.MagicDrawObjectBrowserTreeViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.RootRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.ObjectBrowser.Interfaces.IThingRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.Iteration;

/**
 * The {@linkplain MagicDrawImpactViewViewModel} is the main view model for the requirement impact view in the impact view panel
 */
public class MagicDrawImpactViewViewModel extends MagicDrawObjectBrowserViewModel implements IMagicDrawImpactViewViewModel
{
    /**
     * The {@linkplain IDstController}
     */
    private IDstController dstController;

    /**
     * Initializes a new {@linkplain RequirementImpactViewViewModel}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param dstControllern the {@linkplain IDstController}
     */
    public MagicDrawImpactViewViewModel(IDstController dstController)
    {
        this.dstController = dstController;

        this.InitializesObservables();
    }

    /**
     * Initializes the needed subscription on {@linkplain Observable}
     */
    private void InitializesObservables()
    {
        this.dstController.HasOneDocumentOpenObservable()
            .subscribe(this::UpdateBrowserTrees);
                
        this.dstController.OpenDocumentHasBeenSaved().subscribe(hasBeenSaved -> 
            {
                if(hasBeenSaved)
                {
                    this.UpdateBrowserTrees(this.dstController.HasOneDocumentOpen());
                }
            });
        
        this.dstController.GetHubMapResult()
            .ItemsAdded()
            .subscribe(x -> this.ComputeDifferences(), this.Logger::catching);
        
        this.dstController.GetHubMapResult()
            .IsEmpty()
            .subscribe(isEmpty ->
            {
                if(isEmpty)
                {
                    this.UpdateBrowserTrees(this.dstController.HasOneDocumentOpen());
                }
            });

        this.dstController.GetSelectedHubMapResultForTransfer()
            .ItemsAdded()
            .subscribe(x ->
            {
                for(Class thing : x)
                {
                    this.SwitchIsSelected(thing, true);
                }
                
                this.shouldRefreshTree.Value(true);
            });

        this.dstController.GetSelectedHubMapResultForTransfer()
            .ItemRemoved()
            .subscribe(x -> 
            {
                this.SwitchIsSelected(x, false);
                this.shouldRefreshTree.Value(true);
            });
    }


    /**
     * Sets is selected property on the row view model that represents the provided {@linkplain Class}
     * 
     * @param thing The {@linkplain Class} to find the corresponding row view model
     * @param shouldSelect A value indicating whether the row view model should set as selected
     */
    private void SwitchIsSelected(Class thing, boolean shouldSelect)
    {
        IElementRowViewModel<?> viewModel = this.GetRowViewModelFromClass(thing);
        
        if(!viewModel.GetIsSelected() && shouldSelect)
        {
            viewModel.SetIsSelected(true);
        }
        else if(viewModel.GetIsSelected() && !shouldSelect)
        {
            viewModel.SetIsSelected(false);            
        }
    }

    
    /**
     * Computes the difference for the provided {@linkplain Thing}
     */
    protected Collection<Element> ComputeDifferences()
    {
        try
        {
            ArrayList<Element> elements = new ArrayList<Element>(this.dstController.GetProjectElements());
            
            for(MappedElementRowViewModel<? extends Thing, Class> mappedElementRowViewModel : this.dstController.GetHubMapResult())
            {
                    Ref<Integer> index = new Ref<Integer>(Integer.class, null);
                    
                    elements.stream()
                            .filter(x -> AreTheseEquals(mappedElementRowViewModel.GetDstElement().getID(), x.getID()))
                            .findFirst()
                            .ifPresent(x -> index.Set(elements.indexOf(x)));
                    
                    if(index.HasValue() && elements.remove(index.Get().intValue()) != null)
                    {
                        elements.add(index.Get(), mappedElementRowViewModel.GetDstElement());
                    }
                    else
                    {
                        elements.add(mappedElementRowViewModel.GetDstElement());
                    }
            }
            
            return elements;
        }
        catch(Exception exception)
        {
            this.Logger.catching(exception);
            return Arrays.asList();
        }
    }
    
    /**
     * Gets the {@linkplain IElementRowViewModel} that represent the {@linkplain Class}
     * 
     * @param element the {@linkplain Class}
     * @return the {@linkplain IElementRowViewModel}
     */
    protected IElementRowViewModel<?> GetRowViewModelFromClass(Class element)
    {
        RootRowViewModel rootRowViewModel = (RootRowViewModel) this.BrowserTreeModel.Value().getRoot();
        
        Optional<IElementRowViewModel<?>> optionalDefinition = rootRowViewModel.GetContainedRows().stream()
            .filter(x -> AreTheseEquals(x.GetElement().getID(), element.getID()))
            .findFirst();
        
        if(optionalDefinition.isPresent())
        {
            return optionalDefinition.get();
        }
        
        return null;
    }
    
    /**
     * Updates this view model {@linkplain TreeModel}
     * 
     * @param isConnected a value indicating whether the session is open
     */
    @Override
    protected void UpdateBrowserTrees(Boolean isConnected)
    {
        if(isConnected)
        {
            MagicDrawObjectBrowserTreeViewModel treeModel = this.dstController.GetHubMapResult().isEmpty() 
                    ? new MagicDrawObjectBrowserTreeViewModel(this.dstController.OpenDocument().getName(), this.dstController.GetProjectElements())
                    : new MagicDrawObjectBrowserTreeViewModel(this.dstController.OpenDocument().getName(), this.ComputeDifferences());
                        
            this.SetOutlineModel(DefaultOutlineModel.createOutlineModel(treeModel, new MagicDrawObjectBrowserTreeRowViewModel(), true));
        }
    
        this.IsTheTreeVisible.Value(isConnected);
    }
    
    /**
     * Updates the {@linkplain browserTreeModel} based on the provided {@linkplain Iteration}
     * 
     * @param iteration the {@linkplain Iteration}
     */
    protected void SetOutlineModel(OutlineModel model)
    {
        this.UpdateHighlightOnRows(model);
        this.BrowserTreeModel.Value(model);
    }

    /**
     * Updates the <code>IsHighlighted</code> property on each row of the specified model
     * 
     * @param model the {@linkplain OutlineModel}
     */
    @SuppressWarnings("unchecked")
    private void UpdateHighlightOnRows(OutlineModel model)
    {
        Object root = model.getRoot();
        
        if(root instanceof IHaveContainedRows)
        {
            for (IElementRowViewModel<?> rowViewModel : ((IHaveContainedRows<IElementRowViewModel<?>>)root).GetContainedRows())
            {
                    boolean isHighlighted = this.dstController.GetHubMapResult().stream()
                            .anyMatch(r -> AreTheseEquals(r.GetDstElement().getID(), rowViewModel.GetElement().getID()));
                    
                    rowViewModel.SetIsHighlighted(isHighlighted);
            }
        }
    }

    /**
     * Compute eligible rows where the represented {@linkplain Thing} can be transfered,
     * and return the filtered collection for feedback application on the tree
     * 
     * @param selectedRow the collection of selected view model {@linkplain IThingRowViewModel}
     */
    @Override
    public void OnSelectionChanged(ClassRowViewModel selectedRow) 
    {
        if(selectedRow != null && selectedRow.GetElement() != null && this.dstController.GetHubMapResult().stream()
                .anyMatch(r -> AreTheseEquals(r.GetDstElement().getID(), selectedRow.GetElement().getID())))
        {
            this.AddOrRemoveSelectedRowToTransfer(selectedRow);
        }
    }

    /**
     * Adds or remove the {@linkplain Thing} to/from the relevant collection depending on the {@linkplain MappingDirection}
     * 
     * @param thing the {@linkplain Thing} to add or remove
     */
    private void AddOrRemoveSelectedRowToTransfer(ClassRowViewModel x)
    {
        if(x.SwitchIsSelectedValue())
        {
            this.dstController.GetSelectedHubMapResultForTransfer().add(x.GetElement());
        }
        else
        {
            this.dstController.GetSelectedHubMapResultForTransfer().Remove(x.GetElement());
        }
    }
}
