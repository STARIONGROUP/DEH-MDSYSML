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
import HubController.IHubController;
import Utils.Ref;
import ViewModels.ObjectBrowser.Interfaces.IThingRowViewModel;
import ViewModels.ObjectBrowser.Rows.OwnedDefinedThingRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.Iteration;

/**
 * The {@linkplain ImpactViewBaseViewModel} is the main abstract base view model for the impact views tab panel
 * such as the {@linkplain RequirementImpactViewViewModel} and the {@linkplain ElementDefinitionImpactViewViewModel}
 * 
 * @param <TThing> the type of {@linkplain Thing} the concrete class deals with
 * @param <TRootViewModel> the type of root view model the concrete class has
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
     * @param hubController the {@linkplain IHubController} instance
     * @param dstController the {@linkplain IDstController} instance
     * @param clazz the {@linkplain Class} of the {@linkplain TThing} for future check
     */
    public ImpactViewBaseViewModel(IHubController hubController, IDstController dstController, Class<TThing> clazz)
    {
        super(hubController);
        this.DstController = dstController;
        this.clazz = clazz;

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
        return thing1.getIid().equals(thing0.getIid());
    }

    /**
     * Verifies that the provided {@linkplain Thing} is new or modified
     * 
     * @param thing the thing to verify
     * @return a value indicating whether the thing is new or modified
     */
    protected <TCurrentThing extends Thing> boolean IsThingNewOrModified(Thing thing, Class<TCurrentThing> clazz)
    {
        boolean result = thing.getOriginal() != null || !this.hubController.TryGetThingById(thing.getIid(), new Ref<TCurrentThing>(clazz));
        
        this.logger.debug(String.format("IsThingNewOrModified = thing.getOriginal() != null || !this.hubController.TryGetThingById(thing.getIid(), new Ref<TCurrentThing>(clazz)) ? %s", result));
        
        return result;        
    }
        
    /**
     * Computes the differences between the current model and the elements present in the {@linkplain IDstController}.{@linkplain GetDstMapResult}
     */
    @SuppressWarnings("unchecked")
    protected void ComputeDifferences()
    {
        Iteration iteration = this.hubController.GetOpenIteration().clone(false);
        
        for (Thing thing : this.DstController.GetDstMapResult())
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
        this.browserTreeModel.Value(this.CreateNewModel(iteration));
    }
    
    /**
     * Handles changes in the row selections in the tree
     * 
     * @param parentRowViewModel
     * @param rowViewModel the view model {@linkplain IThingRowViewModel} of the selected row
     */
    @Override
    public void OnSelectionChanged(IThingRowViewModel<?> rowViewModel) { }
}
