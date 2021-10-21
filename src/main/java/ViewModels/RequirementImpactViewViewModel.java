/*
 * RequirementImpactViewViewModel.java
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

import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;

import DstController.IDstController;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Utils.Ref;
import ViewModels.Interfaces.IRequirementImpactViewViewModel;
import ViewModels.ObjectBrowser.RequirementTree.RequirementBrowserTreeRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.RequirementBrowserTreeViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.IterationRequirementRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementBaseTreeElementViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementGroupRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementRowViewModel;
import ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementSpecificationRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain RequirementImpactViewViewModel} is the main view model for the requirement impact view in the impact view panel
 */
public class RequirementImpactViewViewModel extends ImpactViewBaseViewModel<RequirementsSpecification> implements IRequirementImpactViewViewModel
{
    /**
     * Initializes a new {@linkplain RequirementImpactViewViewModel}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param dstControllern the {@linkplain IDstController}
     */
    public RequirementImpactViewViewModel(IHubController hubController, IDstController dstController)
    {
        super(hubController, dstController, RequirementsSpecification.class);
    }

    /**
     * Computes the difference for the provided {@linkplain Thing}
     * 
     * @param iteration the cloned iteration for display purpose
     * @param thing the current {@linkplain Thing} thing
     */
    @Override
    protected void ComputeDifferences(Iteration iteration, RequirementsSpecification thing)
    {
        if(thing.getOriginal() == null)
        {            
            iteration.getRequirementsSpecification().add(thing);
        }
        else
        {
            Ref<Integer> index = new Ref<Integer>(Integer.class, null);
            
            iteration.getRequirementsSpecification()
                    .stream()
                    .filter(x -> this.DoTheseThingsRepresentTheSameThing(x, thing))
                    .findFirst()
                    .ifPresent(x -> index.Set(iteration.getRequirementsSpecification().indexOf(x)));
            
            if(index.HasValue() && iteration.getRequirementsSpecification().removeIf(x -> this.DoTheseThingsRepresentTheSameThing(thing, x)))
            {
                iteration.getRequirementsSpecification().add(index.Get(), thing);
            }
        }
    }
    
    /**
     * Creates a new {@linkplain OutlineModel} based on the provided {@linkplain Iteration}
     * 
     * @param iteration the {@linkplain Iteration}
     */
    @Override
    protected OutlineModel CreateNewModel(Iteration iteration)
    {
        return DefaultOutlineModel.createOutlineModel(
            new RequirementBrowserTreeViewModel(iteration),
            new RequirementBrowserTreeRowViewModel(), true);
    }
}
