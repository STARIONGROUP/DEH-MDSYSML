/*
 * DstToHubMappingConfigurationDialogViewModel.java
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
import static Utils.Stereotypes.StereotypeUtils.GetShortName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ArrayListMultimap;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import DstController.IDstController;
import Enumerations.MappedElementRowStatus;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.ModelConsistency.ICircularDependencyValidationService;
import Services.NavigationService.INavigationService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Dialogs.Interfaces.IDstToHubMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IObjectBrowserBaseViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IMagicDrawObjectBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.BlockRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.RootRowViewModel;
import ViewModels.MappedElementListView.Interfaces.IMappedElementListViewViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import Views.Dialogs.AlertAcyclicDependencyDetectedDialog;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import io.reactivex.Observable;

/**
 * The {@linkplain DstToHubMappingConfigurationDialogViewModel} is the main view
 * model for the {@linkplain DstMappingConfigurationDialog}
 */
public class DstToHubMappingConfigurationDialogViewModel
		extends MappingConfigurationDialogViewModel<Element, Class, ElementRowViewModel<?>>
		implements IDstToHubMappingConfigurationDialogViewModel
{
	/**
	 * The {@linkplain IDstController}
	 */
	private final IDstController iDstController;
	
    /**
     * The {@linkplain ICircularDependencyValidationService}}
     */
    private final ICircularDependencyValidationService circularDependencyService;
    
    /**
     * The {@linkplain IMagicDrawObjectBrowserViewModel}
     */
    private final IMagicDrawObjectBrowserViewModel magicDrawObjectBrowser;

	/**
	 * The {@linkplain INavigationService}
	 */
	private final INavigationService navigationService;

	/**
	 * The {@linkplain IStereotypeService}
	 */
	private final IStereotypeService stereotypeService;

	/**
	 * Gets the DST {@linkplain IObjectBrowserBaseViewModel}
	 * 
	 * @return an {@linkplain IObjectBrowserBaseViewModel}
	 */
	@Override
	public IObjectBrowserBaseViewModel<ElementRowViewModel<?>> GetDstObjectBrowserViewModel()
	{
		return this.magicDrawObjectBrowser;
	}

	/**
	 * Initializes a new {@linkplain DstToHubMappingConfigurationDialogViewModel}
	 * 
	 * @param dstController the {@linkplain IDstController}
	 * @param hubController the {@linkplain IHubController}
	 * @param elementDefinitionBrowserViewModel the {@linkplain IElementDefinitionBrowserViewModel}
	 * @param requirementBrowserViewModel the {@linkplain IRequirementBrowserViewModel}
	 * @param magicDrawObjectBrowserViewModel the {@linkplain IMagicDrawObjectBrowserViewModel}
	 * @param mappedElementListViewViewModel the {@linkplain ICapellaMappedElementListViewViewModel}
	 * @param stereotypeService the {@linkplain IStereotypeService}
	 * @param circularDependencyService the {@linkplain ICircularDependencyValidationService}
	 * @param navigationService the {@linkplain INavigationService}
	 */
	public DstToHubMappingConfigurationDialogViewModel(IDstController dstController, IHubController hubController,
			IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel,
			IRequirementBrowserViewModel requirementBrowserViewModel,
			IMagicDrawObjectBrowserViewModel magicDrawObjectBrowserViewModel,
			IMappedElementListViewViewModel<Class> mappedElementListViewViewModel, 
			IStereotypeService stereotypeService,
			ICircularDependencyValidationService circularDependencyService,
			INavigationService navigationService)
	{
		super(dstController, hubController, elementDefinitionBrowserViewModel, requirementBrowserViewModel,
				mappedElementListViewViewModel);

		this.iDstController = dstController;
		this.magicDrawObjectBrowser = magicDrawObjectBrowserViewModel;
		this.stereotypeService = stereotypeService;
		this.circularDependencyService = circularDependencyService;
		this.navigationService = navigationService;

		this.InitializeObservables();
		this.UpdateProperties();
	}

	/**
	 * Initializes the {@linkplain Observable}s of this view model
	 */
	@Override
	protected void InitializeObservables()
	{
		super.InitializeObservables();
		
		this.magicDrawObjectBrowser.GetSelectedElement()
		        .filter(x -> x instanceof ClassRowViewModel)
		        .map(x -> (ClassRowViewModel)x)
		        .subscribe(x -> this.UpdateMappedElements(x));
	}

	/**
	 * Updates the mapped element collection
	 * 
	 * @param rowViewModel the {@linkplain IElementRowViewModel}
	 */
	@SuppressWarnings("unchecked")
	private void UpdateMappedElements(ClassRowViewModel rowViewModel)
	{
		Optional<MappedElementRowViewModel<DefinedThing, Class>> optionalMappedElement = this.mappedElements.stream()
				.filter(x -> AreTheseEquals(x.GetDstElement().getID(), rowViewModel.GetElement().getID())).findFirst();

		if (!optionalMappedElement.isPresent())
		{
			MappedElementRowViewModel<? extends Thing, Class> mappedElement;

			if (rowViewModel instanceof BlockRowViewModel)
			{
				mappedElement = new MappedElementDefinitionRowViewModel(rowViewModel.GetElement(),
						MappingDirection.FromDstToHub);
			} 
			else
			{
				mappedElement = new MappedRequirementRowViewModel(rowViewModel.GetElement(),
						MappingDirection.FromDstToHub);
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
		if (this.originalSelection == null || this.originalSelection.isEmpty())
		{
			return;
		}

		this.UpdateProperties(this.iDstController.GetDstMapResult());
		this.magicDrawObjectBrowser.BuildTree(this.originalSelection);
	}

    /**
     * Pre-map the selected elements
     * 
     * @param selectedElement the collection of {@linkplain Element}
     * @param requirements the collection of {@linkplain Element}
     */
    @Override
	protected void PreMap(Collection<Element> selectedElement)
	{
	    ArrayList<Class> requirements = new ArrayList<>();
        this.PreMap(selectedElement, requirements);
        
        Collection<MappedElementRowViewModel<DefinedThing, Class>> preMapRequirements = 
                ((IDstController)this.dstController).PreMap(new MagicDrawRequirementCollection(false, requirements.stream()
                    .map(x -> new MappedRequirementRowViewModel(x, MappingDirection.FromDstToHub))
                    .collect(Collectors.toList())));
        
        for (MappedElementRowViewModel<DefinedThing, Class> mappedElementRowViewModel : preMapRequirements)
        {
            this.UpdateRowStatus(mappedElementRowViewModel, Requirement.class);
        }
        
        this.mappedElements.addAll(preMapRequirements);
	}
	
	/**
	 * Pre-map the selected elements
	 * 
	 * @param selectedElements the collection of {@linkplain Element}
	 * @param requirements the collection of {@linkplain Class}
	 */
	protected void PreMap(Collection<Element> selectedElements, Collection<Class> requirements)
	{
		if (selectedElements == null)
		{
			return;
		}

		for (Element element : this.FilterInvalidElements(selectedElements))
		{
		    if(element instanceof Class)
		    {		    
    			if (this.stereotypeService.DoesItHaveTheStereotype(element, Stereotypes.Block))
    			{
    				MappedElementRowViewModel<DefinedThing, Class> mappedElement = this.GetBlockMappedElementRowViewModel((Class) element);
    
    				if (mappedElement != null)
    				{
    					this.mappedElements.add(mappedElement);
    				}
    			}
    			else if (this.stereotypeService.DoesItHaveTheStereotype(element, Stereotypes.Requirement)
    			        && this.dstController.GetDstMapResult().stream()
    			                .noneMatch(x -> AreTheseEquals(x.GetDstElement().getID(), element.getID())))
    			{
    			    requirements.add((Class) element);
    			}
			}
			else if (element instanceof Package)
			{
				this.PreMap(element.getOwnedElement());
			}
		}
	}

	/**
	 * Filters out un-mappable {@linkplain Element}s and warn the user
	 * 
     * @param selectedElements the original selection of {@linkplain Element}
     * @return the filtered collection of {@linkplain Element}
     */
    private Collection<Element> FilterInvalidElements(Collection<Element> selectedElements)
    {
        Pair<ArrayListMultimap<Class, Collection<NamedElement>>, Collection<Element>> elements = this.circularDependencyService.FiltersInvalidElements(selectedElements);
        
        if(!elements.getLeft().isEmpty())
        {
            this.navigationService.ShowDialog(new AlertAcyclicDependencyDetectedDialog(), new AlertAcyclicDependencyDetectedDialogViewModel(elements.getLeft()));
        }
        
        return elements.getRight();
    }

    /**
	 * Get a {@linkplain MappedElementRowViewModel} that represents a pre-mapped
	 * {@linkplain Class}
	 * 
	 * @param classElement the {@linkplain Class} element
	 * @return a {@linkplain MappedElementRowViewModel}
	 */
	@SuppressWarnings("unchecked")
	private MappedElementRowViewModel<DefinedThing, Class> GetBlockMappedElementRowViewModel(Class classElement)
	{
		Ref<Boolean> refShouldCreateNewTargetElement = new Ref<>(Boolean.class, false);
		MappedElementRowViewModel<? extends Thing, Class> mappedElementRowViewModel = null;

		Ref<ElementDefinition> refElementDefinition = new Ref<>(ElementDefinition.class);

		if (this.TryGetElementDefinition(classElement, refElementDefinition, refShouldCreateNewTargetElement))
		{
			mappedElementRowViewModel = new MappedElementDefinitionRowViewModel(refElementDefinition.Get(),
					classElement, MappingDirection.FromDstToHub);
		}
		
		if (mappedElementRowViewModel != null)
		{
			this.UpdateRowStatus(refShouldCreateNewTargetElement, mappedElementRowViewModel);
			return (MappedElementRowViewModel<DefinedThing, Class>) mappedElementRowViewModel;
		}

		this.logger.warn(String.format(
				"Impossible to map the provided class %s as it doesn't have a supported stereotype or is already present in the mapped rows",
				classElement.getName()));
		
		return null;
	}

    private void UpdateRowStatus(Ref<Boolean> refShouldCreateNewTargetElement,
            MappedElementRowViewModel<? extends Thing, Class> mappedElementRowViewModel)
    {
        mappedElementRowViewModel.SetShouldCreateNewTargetElement(refShouldCreateNewTargetElement.Get());
        mappedElementRowViewModel.SetRowStatus(
        		Boolean.TRUE.equals(refShouldCreateNewTargetElement.Get()) ? MappedElementRowStatus.NewElement
        				: MappedElementRowStatus.ExisitingElement);
    }

	/**
	 * Updates the {@linkplain MappedElementRowStatus} of the provided {@linkplain MappedElementRowViewModel}
	 * 
	 * @param mappedElementRowViewModel the {@linkplain MappedElementRowViewModel} of which to update the row status
	 * @param clazz the {@linkplain java.lang.Class} of the {@linkplain Thing} represented in the {@linkplain MappedElementRowViewModel}
	 */
	@Override
	protected <TThing extends Thing> void UpdateRowStatus(MappedElementRowViewModel<? extends Thing, ? extends Class> mappedElementRowViewModel,
			java.lang.Class<TThing> clazz)
	{
		Ref<TThing> refThing = new Ref<>(clazz);
		mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.None);

		if (mappedElementRowViewModel.GetShouldCreateNewTargetElementValue())
		{
			mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.NewElement);
		} 
		else if (mappedElementRowViewModel.GetHubElement() != null)
		{
			if (this.iDstController.GetDstMapResult().stream().filter(x -> x.GetHubElement().getClass() == clazz)
					.anyMatch(x -> AreTheseEquals(x.GetHubElement().getIid(),
							mappedElementRowViewModel.GetHubElement().getIid())))
			{
				mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.ExistingMapping);
			}
			else if (this.hubController.TryGetThingById(mappedElementRowViewModel.GetHubElement().getIid(), refThing))
			{
				mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.ExisitingElement);
			}
		}
	}

	/**
	 * Gets or create an {@linkplain ElementDefinition} that can be mapped to the
	 * provided {@linkplain Class}, In the case the provided {@linkplain Class} is
	 * already represented in the {@linkplain mappedElements} returns false
	 * 
	 * @param classElement the {@linkplain Class} element
	 * @param refElementDefinition the {@linkplain Ref} of {@linkplain ElementDefinition}
	 * @param refShouldCreateNewTargetElement the {@linkplain Ref} of {@linkplain Boolean} indicating whether the target Hub element will be created
	 * @return a value indicating whether the method execution was successful in getting a {@linkplain ElementDefinition}
	 */
	private boolean TryGetElementDefinition(Class classElement, Ref<ElementDefinition> refElementDefinition,
			Ref<Boolean> refShouldCreateNewTargetElement)
	{
		if (this.mappedElements.stream()
				.noneMatch(x -> AreTheseEquals(x.GetDstElement().getID(), classElement.getID())))
		{
			Optional<ElementDefinition> optionalElementDefinition = this.hubController.GetOpenIteration().getElement()
					.stream().filter(x -> AreTheseEquals(x.getName(), classElement.getName())).findFirst();

			if (optionalElementDefinition.isPresent())
			{
				refElementDefinition.Set(optionalElementDefinition.get().clone(true));
			} else
			{
				ElementDefinition elementDefinition = new ElementDefinition();
				elementDefinition.setIid(UUID.randomUUID());
				elementDefinition.setName(classElement.getName());
				elementDefinition.setShortName(GetShortName(classElement));
				elementDefinition.setOwner(this.hubController.GetCurrentDomainOfExpertise());

				refElementDefinition.Set(elementDefinition);
				refShouldCreateNewTargetElement.Set(true);
			}
		}

		return refElementDefinition.HasValue();
	}

	/**
	 * Resets the pre-mapped things to the default way
	 */
	@Override
	public void ResetPreMappedThings()
	{
		List<Element> elements = ((RootRowViewModel) this.magicDrawObjectBrowser.GetBrowserTreeModel().getRoot())
				.GetContainedRows().stream().map(x -> (Element) x.GetElement()).collect(Collectors.toList());

		this.UpdateProperties();
		this.PreMap(elements);
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
		} 
		else if (this.selectedMappedElement.Value().GetHubElement() instanceof Requirement)
		{
			this.UpdateRowStatus(this.selectedMappedElement.Value(), Requirement.class);
		}
	}
}
