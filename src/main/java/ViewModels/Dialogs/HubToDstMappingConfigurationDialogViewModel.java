/*
 * HubToDstMappingConfigurationDialogViewModel.java
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
package ViewModels.Dialogs;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;

import DstController.IDstController;
import Enumerations.MappedElementRowStatus;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Utils.Ref;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Dialogs.Interfaces.IHubToDstMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IObjectBrowserBaseViewModel;
import ViewModels.Interfaces.IObjectBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IMagicDrawObjectBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.BlockRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.MappedElementListView.Interfaces.IMappedElementListViewViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Requirement;
import io.reactivex.Observable;

/**
 * The {@linkplain HubToDstMappingConfigurationDialogViewModel} is the main view
 * model for the {@linkplain CapellaHubToDstMappingConfigurationDialog}
 */
public class HubToDstMappingConfigurationDialogViewModel
		extends MappingConfigurationDialogViewModel<Thing, Class, ClassRowViewModel>
		implements IHubToDstMappingConfigurationDialogViewModel
{
	/**
	 * The {@linkplain IMagicDrawObjectBrowserViewModel}
	 */
	private final IMagicDrawObjectBrowserViewModel magicDrawObjectBrowser;

	/**
	 * The {@linkplain IMagicDrawTransactionService}
	 */
	private IMagicDrawTransactionService transactionService;

	/**
	 * Gets the DST {@linkplain IObjectBrowserBaseViewModel}
	 * 
	 * @return an {@linkplain IObjectBrowserViewModel}
	 */
	@Override
	public IObjectBrowserBaseViewModel<ClassRowViewModel> GetDstObjectBrowserViewModel()
	{
		return this.magicDrawObjectBrowser;
	}

	/**
	 * The {@linkplain IDstController}
	 */
	private final IDstController iDstController;

	/**
	 * Initializes a new {@linkplain HubToDstMappingConfigurationDialogViewModel}
	 * 
	 * @param dstController the {@linkplain IDstController}
	 * @param hubController the {@linkplain IHubController}
	 * @param elementDefinitionBrowserViewModel the {@linkplain IElementDefinitionBrowserViewModel}
	 * @param requirementBrowserViewModel the {@linkplain IRequirementBrowserViewModel}
	 * @param magicDrawObjectBrowserViewModel   the {@linkplain IMagicDrawObjectBrowserViewModel}
	 * @param transactionService the {@linkplain IMagicDrawTransactionService}
	 * @param mappedElementListViewViewModel the {@linkplain ICapellaMappedElementListViewViewModel}
	 */
	public HubToDstMappingConfigurationDialogViewModel(IDstController dstController, IHubController hubController,
			IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel,
			IRequirementBrowserViewModel requirementBrowserViewModel,
			IMagicDrawObjectBrowserViewModel magicDrawObjectBrowserViewModel,
			IMagicDrawTransactionService transactionService,
			IMappedElementListViewViewModel<Class> mappedElementListViewViewModel)
	{
		super(dstController, hubController, elementDefinitionBrowserViewModel, requirementBrowserViewModel,
				mappedElementListViewViewModel);

		this.magicDrawObjectBrowser = magicDrawObjectBrowserViewModel;
		this.iDstController = dstController;
		this.transactionService = transactionService;
		this.InitializeObservables();
	}

	/**
	 * Initializes the {@linkplain Observable}s of this view model
	 */
	@Override
	protected void InitializeObservables()
	{
		super.InitializeObservables();
		this.magicDrawObjectBrowser.GetSelectedElement().subscribe(x -> this.UpdateMappedElements(x));
	}

	/**
	 * Updates the mapped element collection
	 * 
	 * @param rowViewModel the {@linkplain IElementRowViewModel}
	 */
	@SuppressWarnings("unchecked")
	private void UpdateMappedElements(
			ViewModels.MagicDrawObjectBrowser.Rows.ElementRowViewModel<? extends Class> rowViewModel)
	{
		Optional<MappedElementRowViewModel<DefinedThing, Class>> optionalMappedElement = this.mappedElements.stream()
				.filter(x -> AreTheseEquals(x.GetDstElement().getID(), rowViewModel.GetElement().getID())).findFirst();

		if (!optionalMappedElement.isPresent())
		{
			MappedElementRowViewModel<? extends Thing, ? extends Class> mappedElement;

			if (rowViewModel instanceof BlockRowViewModel)
			{
				mappedElement = new MappedElementDefinitionRowViewModel(rowViewModel.GetElement(),
						MappingDirection.FromHubToDst);
			} else
			{
				mappedElement = new MappedRequirementRowViewModel(rowViewModel.GetElement(),
						MappingDirection.FromHubToDst);
			}

			this.mappedElements.add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement);
			this.SetSelectedMappedElement((MappedElementRowViewModel<DefinedThing, Class>) mappedElement);
		} else
		{
			this.SetSelectedMappedElement(optionalMappedElement.get());
		}
	}

	/**
	 * Updates this view model properties
	 */
	@Override
	protected void UpdateProperties()
	{
		this.UpdateProperties(this.iDstController.GetHubMapResult());
		this.magicDrawObjectBrowser.BuildTree();
	}

	/**
	 * Pre-map the selected elements
	 * 
	 * @param selectedElement the collection of {@linkplain #TElement}
	 */
	@Override
	protected void PreMap(Collection<Thing> selectedElements)
	{
		for (Thing thing : selectedElements)
		{
			this.PreMap(selectedElements, thing);
		}
	}

    /**
     * Pre-map the provided {@linkplain Thing} and its children 
     *  
     * @param selectedElement the collection of {@linkplain #TElement}
     * @param thing the {@linkplain Thing} to premap
     */
    private void PreMap(Collection<Thing> selectedElements, Thing thing)
    {
        MappedElementRowViewModel<DefinedThing, Class> mappedRowViewModel = this
        		.GetMappedElementRowViewModel(thing);
        
        if (mappedRowViewModel != null)
        {
        	this.mappedElements.add(mappedRowViewModel);
        }

        if(thing instanceof ElementDefinition)
        {
            for(ElementDefinition elementDefinition : ((ElementDefinition)thing).getContainedElement().stream()
                    .filter(x -> !selectedElements.contains(x.getElementDefinition())
                            && x.getElementDefinition().getParameter().stream().anyMatch(p -> p.getStateDependence() != null))
                    .map(x -> x.getElementDefinition())
                    .collect(Collectors.toList()))
            {
                this.PreMap(selectedElements, elementDefinition);
            }
        }
    }

	/**
	 * Get a {@linkplain MappedElementRowViewModel} that represents a pre-mapped
	 * {@linkplain Class}
	 * 
	 * @param thing the {@linkplain Class} element
	 * @return a {@linkplain MappedElementRowViewModel}
	 */
	@SuppressWarnings("unchecked")
	protected MappedElementRowViewModel<DefinedThing, Class> GetMappedElementRowViewModel(Thing thing)
	{
		Ref<Boolean> refShouldCreateNewTargetElement = new Ref<>(Boolean.class, false);
		MappedElementRowViewModel<? extends Thing, ? extends Class> mappedElementRowViewModel = null;

		if (thing instanceof ElementDefinition)
		{
			Ref<Class> refComponent = new Ref<>(Class.class);

			if (this.TryGetBlock((ElementDefinition) thing, refComponent, refShouldCreateNewTargetElement))
			{
				MappedElementDefinitionRowViewModel mappedElementDefinition = new MappedElementDefinitionRowViewModel(
						(ElementDefinition) thing, refComponent.Get(), MappingDirection.FromHubToDst);

				mappedElementRowViewModel = mappedElementDefinition;
			}
		} else if (thing instanceof cdp4common.engineeringmodeldata.Requirement)
		{
			Ref<Class> refRequirement = new Ref<>(Class.class);

			if (this.TryGetRequirement((cdp4common.engineeringmodeldata.Requirement) thing, refRequirement,
					refShouldCreateNewTargetElement))
			{
				mappedElementRowViewModel = new MappedRequirementRowViewModel(
						(cdp4common.engineeringmodeldata.Requirement) thing, refRequirement.Get(),
						MappingDirection.FromHubToDst);
			}
		}

		if (mappedElementRowViewModel != null)
		{
			mappedElementRowViewModel.SetShouldCreateNewTargetElement(refShouldCreateNewTargetElement.Get());
			mappedElementRowViewModel.SetRowStatus(
					Boolean.TRUE.equals(refShouldCreateNewTargetElement.Get()) ? MappedElementRowStatus.NewElement
							: MappedElementRowStatus.ExisitingElement);
			return (MappedElementRowViewModel<DefinedThing, Class>) mappedElementRowViewModel;
		}

		return null;
	}

	/**
	 * Gets or create an {@linkplain Component} that can be mapped to the provided
	 * {@linkplain ElementDefinition}, In the case the provided
	 * {@linkplain ElementDefinition} is already represented in the
	 * {@linkplain mappedElements} returns false
	 * 
	 * @param elementDefinition               the {@linkplain ElementDefinition}
	 *                                        element
	 * @param refBlock                        the {@linkplain Ref} of
	 *                                        {@linkplain Component}
	 * @param refShouldCreateNewTargetElement the {@linkplain Ref} of
	 *                                        {@linkplain Boolean} indicating
	 *                                        whether the target DST element will be
	 *                                        created
	 * @return a value indicating whether the method execution was successful in
	 *         getting a {@linkplain Component}
	 */
	private boolean TryGetBlock(ElementDefinition elementDefinition, Ref<Class> refBlock,
			Ref<Boolean> refShouldCreateNewTargetElement)
	{
		if (this.mappedElements.stream()
				.noneMatch(x -> AreTheseEquals(x.GetHubElement().getIid(), elementDefinition.getIid())
						&& x.GetDstElement() != null))
		{
			if (this.iDstController.TryGetElementByName(elementDefinition, refBlock))
			{
				refBlock.Set(this.transactionService.CloneElement(refBlock.Get()));
			} else
			{
				Class block = this.transactionService.Create(Stereotypes.Block, elementDefinition.getName());
				refBlock.Set(block);
				refShouldCreateNewTargetElement.Set(true);
			}
		}

		return refBlock.HasValue();
	}

	/**
	 * Gets or create an {@linkplain Requirement} that can be mapped from the
	 * specified {@linkplain cdp4common.engineeringmodeldata.Requirement}, In the
	 * case the provided {@linkplain cdp4common.engineeringmodeldata.Requirement} is
	 * already represented in the {@linkplain mappedElements} returns false
	 * 
	 * @param requirement                     the
	 *                                        {@linkplain cdp4common.engineeringmodeldata.Requirement}
	 *                                        element
	 * @param refRequirement                  the {@linkplain Ref} of
	 *                                        {@linkplain Requirement}
	 * @param refShouldCreateNewTargetElement the {@linkplain Ref} of
	 *                                        {@linkplain Boolean} indicating
	 *                                        whether the target DST element will be
	 *                                        created
	 * @param refArchitecture                 the {@linkplain Ref} of
	 *                                        {@linkplain CapellaArchitecture}
	 * @return a value indicating whether the method execution was successful in
	 *         getting a {@linkplain Class}
	 */
	private boolean TryGetRequirement(cdp4common.engineeringmodeldata.Requirement requirement,
			Ref<Class> refRequirement, Ref<Boolean> refShouldCreateNewTargetElement)
	{
		if (this.mappedElements.stream()
				.noneMatch(x -> AreTheseEquals(x.GetHubElement().getIid(), requirement.getIid())))
		{
			if (this.iDstController.TryGetElementByName(requirement, refRequirement))
			{
				Class original = refRequirement.Get();
				refRequirement.Set(this.transactionService.CloneElement(original));
			} else
			{
				Class newRequirement = this.transactionService.Create(Stereotypes.Requirement, requirement.getName());
				refRequirement.Set(newRequirement);
				refShouldCreateNewTargetElement.Set(true);
			}
		}

		return refRequirement.HasValue();
	}

	/**
	 * Occurs when the user sets the target element of the current mapped element to
	 * be a
	 * 
	 * @param selected the new {@linkplain boolean} value
	 */
	@Override
	public void WhenMapToNewElementCheckBoxChanged(boolean selected)
	{
		if (this.selectedMappedElement.Value() == null)
		{
			return;
		}

		if (selected && !this.selectedMappedElement.Value().GetShouldCreateNewTargetElementValue())
		{
			this.selectedMappedElement.Value().SetHubElement(null);
		}

		if (selected != this.selectedMappedElement.Value().GetShouldCreateNewTargetElementValue())
		{
			this.selectedMappedElement.Value().SetShouldCreateNewTargetElement(selected);
		}

		if (this.selectedMappedElement.Value().GetHubElement() instanceof ElementDefinition)
		{
			this.UpdateRowStatus(this.selectedMappedElement.Value(), ElementDefinition.class);
		} else if (this.selectedMappedElement.Value().GetHubElement() instanceof Requirement)
		{
			this.UpdateRowStatus(this.selectedMappedElement.Value(), Requirement.class);
		}
	}
}
