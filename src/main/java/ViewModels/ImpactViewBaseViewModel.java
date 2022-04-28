/*
 * ImpactViewBaseViewModel.java
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

import org.netbeans.swing.outline.OutlineModel;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Utils.Ref;
import static Utils.Operators.Operators.AreTheseEquals;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IThingRowViewModel;
import ViewModels.ObjectBrowser.Rows.ThingRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.Iteration;

/**
 * The {@linkplain ImpactViewBaseViewModel} is the main abstract base view model for the impact views tab panel
 * such as the {@linkplain RequirementImpactViewViewModel} and the {@linkplain ElementDefinitionImpactViewViewModel}
 * 
 * @param <TThing> the type of {@linkplain Thing} the concrete class deals with
 */
public abstract class ImpactViewBaseViewModel<TThing extends Thing> extends ObjectBrowserViewModel
{
    /**
     * The {@linkplain IDstController}
     */
    protected IDstController DstController;
    
    /**
     * The {@linkplain Class} of the {@linkplain TThing} for future check
     */
    private Class<TThing> clazz;

    /**
     * Initializes a new {@linkplain ImpactViewBaseViewModel}
     * 
     * @param HubController the {@linkplain IHubController} instance
     * @param dstController the {@linkplain IDstController} instance
     * @param clazz the {@linkplain Class} of the {@linkplain TThing} for future check
     */
    protected ImpactViewBaseViewModel(IHubController hubController, IDstController dstController, Class<TThing> clazz)
    {
        super(hubController);
        this.DstController = dstController;
        this.clazz = clazz;

        this.InitializesObservables();
    }

    /**
     * Initializes the needed subscription on {@linkplain Observable}
     */
    @SuppressWarnings("unchecked")
    private void InitializesObservables()
    {
        this.DstController.GetDstMapResult()
            .ItemsAdded()
            .subscribe(x -> this.ComputeDifferences(), e -> this.logger.catching(e));
        
        this.DstController.GetDstMapResult()
            .IsEmpty()
            .subscribe(isEmpty ->
            {
                if(isEmpty)
                {
                    this.UpdateBrowserTrees(this.hubController.GetIsSessionOpen());
                }
            });

        this.DstController.GetSelectedDstMapResultForTransfer()
            .ItemsAdded()
            .filter(x -> x.stream().allMatch(t -> this.clazz.isInstance(t)))
            .subscribe(x ->
            {
                for(Thing thing : x)
                {
                    this.SwitchIsSelected((TThing)thing, true);
                }
                
                this.shouldRefreshTree.Value(true);
            });

        this.DstController.GetSelectedDstMapResultForTransfer()
            .ItemRemoved()
            .filter(x -> this.clazz.isInstance(x))
            .subscribe(x -> 
            {
                this.SwitchIsSelected((TThing)x, false);
                this.shouldRefreshTree.Value(true);
            });
    }

    /**
     * Sets is selected property on the row view model that represents the provided {@linkplain Thing}
     * 
     * @param thing The {@linkplain Thing} to find the corresponding row view model
     * @param shouldSelect A value indicating whether the row view model should set as selected
     */
    private void SwitchIsSelected(TThing thing, boolean shouldSelect)
    {
        IThingRowViewModel<TThing> viewModel = this.GetRowViewModelFromThing((TThing)thing);
        
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
     * Gets the {@linkplain IThingRowViewModel} that represent the {@linkplain Thing}
     * 
     * @param thing the {@linkplain TThing} 
     * @return the {@linkplain IThingRowViewModel} of {@linkplain TThing}
     */
    protected abstract IThingRowViewModel<TThing> GetRowViewModelFromThing(TThing thing);
    
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
            this.SetOutlineModel(this.hubController.GetOpenIteration());
        }
    
        this.isTheTreeVisible.Value(isConnected);
    }
    
    /**
     * Verifies that the two provided {@linkplain Thing} have the same id
     * 
     * @param thing0 the first {@linkplain Thing} to compare
     * @param thing1 the second {@linkplain Thing} to compare
     * @return a {@linkplain boolean}
     */
    protected boolean DoTheseThingsRepresentTheSameThing(Thing thing0, Thing thing1)
    {
        return AreTheseEquals(thing0.getIid(), thing1.getIid());
    }

    /**
     * Verifies that the provided {@linkplain Thing} is new or modified
     * 
     * @param thing the thing to verify
     * @return a value indicating whether the thing is new or modified
     */
    protected <TCurrentThing extends Thing> boolean IsThingNewOrModified(Thing thing, Class<TCurrentThing> clazz)
    {
        return thing.getOriginal() != null || !this.hubController.TryGetThingById(thing.getIid(), new Ref<TCurrentThing>(clazz));
    }

    /**
     * Computes the differences between the current model and the elements present in the {@linkplain IDstController}.{@linkplain GetDstMapResult}
     */
    @SuppressWarnings("unchecked")
    protected void ComputeDifferences()
    {
        Iteration iteration = this.hubController.GetOpenIteration().clone(false);
        
        for (Thing thing : this.DstController.GetDstMapResult()
                .stream().map(x -> x.GetHubElement()).collect(Collectors.toList()))
        {
            if(this.clazz.isInstance(thing))
            {
                this.ComputeDifferences(iteration, (TThing)thing);
            }
        }
        
        this.SetOutlineModel(iteration);
    }

    /**
     * Creates a new {@linkplain OutlineModel} based on the provided {@linkplain Iteration}
     * 
     * @param iteration the {@linkplain Iteration}
     */
    protected abstract OutlineModel CreateNewModel(Iteration iteration);
    
    /**
     * Computes the difference for the provided {@linkplain Thing}
     * 
     * @param iteration the cloned iteration for display purpose
     * @param thing the current {@linkplain Thing} thing
     */
    protected abstract void ComputeDifferences(Iteration iteration, TThing thing);
    
    /**
     * Updates the {@linkplain browserTreeModel} based on the provided {@linkplain Iteration}
     * 
     * @param iteration the {@linkplain Iteration}
     */
    protected void SetOutlineModel(Iteration iteration)
    {
        OutlineModel model = this.CreateNewModel(iteration);
        this.UpdateHighlightOnRows(model);
        this.browserTreeModel.Value(model);
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
            for (IRowViewModel row : ((IHaveContainedRows<IRowViewModel>)root).GetContainedRows())
            {
                if(row instanceof IThingRowViewModel)
                {
                    IThingRowViewModel<?> thingRowViewModel = (IThingRowViewModel<?>)row;
                    
                    boolean isHighlighted = this.DstController.GetDstMapResult().stream()
                            .anyMatch(r -> AreTheseEquals(r.GetHubElement().getIid(), thingRowViewModel.GetThing().getIid()));
                    
                    thingRowViewModel.SetIsHighlighted(isHighlighted);
                }
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
    public void OnSelectionChanged(ThingRowViewModel<?> selectedRow) 
    {
        if(selectedRow != null && selectedRow.GetThing() != null)
        {
            Optional<MappedElementRowViewModel<? extends Thing, ? extends com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class>> optionalMappedElement = 
                    this.DstController.GetDstMapResult().stream()
                        .filter(r -> AreTheseEquals(r.GetHubElement().getIid(), selectedRow.GetThing().getIid()))
                        .findFirst();
            
            if(optionalMappedElement.isPresent())
            {
                this.AddOrRemoveSelectedRowToTransfer(selectedRow, optionalMappedElement.get().GetHubElement());
            }            
        }
    }

    /**
     * Adds or remove the {@linkplain Thing} to/from the relevant collection depending on the {@linkplain MappingDirection}
     * 
     * @param x the {@linkplain IThingRowViewModel} that represents the {@linkplain Thing}
     * @param thing the {@linkplain Thing} to add or remove
     */
    private void AddOrRemoveSelectedRowToTransfer(IThingRowViewModel<?> x, Thing thing)
    {
        if(x.SwitchIsSelectedValue())
        {
            this.DstController.GetSelectedDstMapResultForTransfer().add(thing);
        }
        else
        {
            this.DstController.GetSelectedDstMapResultForTransfer().Remove(thing);
        }
    }
}
