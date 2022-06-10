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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import DstController.IDstController;
import Enumerations.MappedElementRowStatus;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.StereotypeUtils;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Dialogs.Interfaces.IDstToHubMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IObjectBrowserBaseViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IMagicDrawObjectBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.BlockRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.RootRowViewModel;
import ViewModels.MappedElementListView.Interfaces.IMappedElementListViewViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.NamedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import io.reactivex.Observable;

/**
 * The {@linkplain DstToHubMappingConfigurationDialogViewModel} is the main view model for the {@linkplain DstMappingConfigurationDialog}
 */
public class DstToHubMappingConfigurationDialogViewModel extends MappingConfigurationDialogViewModel<Element, Class, ClassRowViewModel> implements IDstToHubMappingConfigurationDialogViewModel
{
    /**
     * The {@linkplain IDstController}
     */
    private final IDstController dstController;

    /**
     * The {@linkplain IMagicDrawObjectBrowserViewModel}
     */
    private final IMagicDrawObjectBrowserViewModel magicDrawObjectBrowser;

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
    public IObjectBrowserBaseViewModel<ClassRowViewModel> GetDstObjectBrowserViewModel()
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
     */
    public DstToHubMappingConfigurationDialogViewModel(IDstController dstController, IHubController hubController, 
            IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel, IRequirementBrowserViewModel requirementBrowserViewModel,
            IMagicDrawObjectBrowserViewModel magicDrawObjectBrowserViewModel, IMappedElementListViewViewModel<Class> mappedElementListViewViewModel,
            IStereotypeService stereotypeService)
    {
        super(dstController, hubController, elementDefinitionBrowserViewModel, requirementBrowserViewModel, mappedElementListViewViewModel);
        
        this.dstController = dstController;
        this.magicDrawObjectBrowser = magicDrawObjectBrowserViewModel;
        this.stereotypeService = stereotypeService;
        
        this.InitializeObservables();
        this.UpdateProperties();
    }
    
    /**
     * Initializes the {@linkplain Observable}s of this view model
     */
    @Override
    protected void InitializeObservables()
    {
        this.magicDrawObjectBrowser.GetSelectedElement()
            .subscribe(x -> this.UpdateMappedElements(x));
        
        this.GetElementDefinitionBrowserViewModel().GetSelectedElement()
            .subscribe(x -> this.SetHubElement(x.GetThing()));

        this.GetRequirementBrowserViewModel().GetSelectedElement()
            .subscribe(x -> this.SetHubElement(x.GetThing()));
        
        this.selectedMappedElement.Observable().subscribe(
                x -> this.shouldMapToNewHubElementCheckBoxBeEnabled.Value(
                        x != null && x.GetRowStatus() != MappedElementRowStatus.ExistingMapping),
                x -> this.logger.catching(x));        
    }

    /**
     * Sets the Hub element on the selected element if the element is compatible
     * 
     * @param thing the {@linkplain Thing} to assign
     */
    private void SetHubElement(Thing thing)
    {
        if(this.selectedMappedElement.Value() == null || this.selectedMappedElement.Value().GetRowStatus() == MappedElementRowStatus.ExistingMapping)
        {
            return;
        }
        
        if(thing instanceof ElementDefinition
                && this.selectedMappedElement.Value().GetTThingClass().isAssignableFrom(ElementDefinition.class))
        {
            this.SetHubElement(thing, ElementDefinition.class);
        }
        else if(thing instanceof RequirementsSpecification
                && this.selectedMappedElement.Value().GetTThingClass().isAssignableFrom(RequirementsSpecification.class))
        {
            this.SetHubElement(thing, RequirementsSpecification.class);
        }
        else
        {
            this.logger.warn("Thing is not compatible with the current selected mapped element!");
        }
    }

    /**
     * Sets the Hub element on the selected element
     * 
     * @param thing the {@linkplain Thing} to assign
     * @param clazz the class of the {@linkplain Thing}
     */
    @SuppressWarnings("unchecked")
    private <TThing extends DefinedThing> void SetHubElement(Thing thing, java.lang.Class<TThing> clazz)
    {
        MappedElementRowViewModel<TThing, Class> mappedElementRowViewModel = (MappedElementRowViewModel<TThing, Class>)this.selectedMappedElement.Value();
        
        mappedElementRowViewModel.SetHubElement((TThing)thing.clone(true));
        
        this.shouldMapToNewHubElementCheckBoxBeEnabled.Value(false);
        mappedElementRowViewModel.SetShouldCreateNewTargetElement(false);
        
        this.UpdateRowStatus(this.selectedMappedElement.Value(), clazz);
    }

    /**
     * Updates the mapped element collection 
     * 
     * @param rowViewModel the {@linkplain IElementRowViewModel}
     */
    private void UpdateMappedElements(ClassRowViewModel rowViewModel)
    {
        Optional<MappedElementRowViewModel<? extends DefinedThing, ? extends Class>> optionalMappedElement = this.mappedElements.stream()
            .filter(x -> AreTheseEquals(x.GetDstElement().getID(), rowViewModel.GetElement().getID()))
            .findFirst();
        
        if(!optionalMappedElement.isPresent())
        {
            MappedElementRowViewModel<? extends Thing, Class> mappedElement;
            
            if(rowViewModel instanceof BlockRowViewModel)
            {
                mappedElement = new MappedElementDefinitionRowViewModel(rowViewModel.GetElement(), MappingDirection.FromDstToHub);
            }
            else
            {
                mappedElement = new MappedRequirementRowViewModel(rowViewModel.GetElement(), MappingDirection.FromDstToHub);
            }

            this.mappedElements.add(mappedElement);
            this.SetSelectedMappedElement(mappedElement);
        }
        else
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
        if(this.originalSelection == null || this.originalSelection.isEmpty())
        {
            return;
        }
        
        this.UpdateProperties(this.dstController.GetDstMapResult());
        this.magicDrawObjectBrowser.BuildTree(this.originalSelection);
    }
    
    /**
     * Pre-map the selected elements
     * 
     * @param selectedElement the collection of {@linkplain Element}
     */
    protected void PreMap(Collection<Element> selectedElement)
    {
        if(selectedElement == null)
        {
            return;
        }
        
        for (Element element : selectedElement)
        {            
            if (element instanceof Class)
            {
                MappedElementRowViewModel<? extends Thing, Class> mappedElement = this.GetMappedElementRowViewModel((Class)element);
         
                if(mappedElement != null)
                {
                    this.mappedElements.add(mappedElement);
                }
            }
            else if(element instanceof Package)
            {
                this.PreMap(element.getOwnedElement());
            }             
        }
    }    

    /**
     * Get a {@linkplain MappedElementRowViewModel} that represents a pre-mapped {@linkplain Class}
     * 
     * @param classElement the {@linkplain Class} element
     * @return a {@linkplain MappedElementRowViewModel}
     */
    private MappedElementRowViewModel<? extends Thing, Class> GetMappedElementRowViewModel(Class classElement)
    {
        Ref<Boolean> refShouldCreateNewTargetElement = new Ref<>(Boolean.class, false);
        MappedElementRowViewModel<? extends Thing, Class> mappedElementRowViewModel = null;
        
        if(this.stereotypeService.DoesItHaveTheStereotype(classElement, Stereotypes.Block))
        {
            Ref<ElementDefinition> refElementDefinition = new Ref<>(ElementDefinition.class);
            
            if(this.TryGetElementDefinition(classElement, refElementDefinition, refShouldCreateNewTargetElement))
            {
                mappedElementRowViewModel = 
                        new MappedElementDefinitionRowViewModel(refElementDefinition.Get(), classElement, MappingDirection.FromDstToHub);
            }
        }
        else if(this.stereotypeService.DoesItHaveTheStereotype(classElement, Stereotypes.Requirement))
        {
            Ref<cdp4common.engineeringmodeldata.Requirement> refRequirement = new Ref<>(cdp4common.engineeringmodeldata.Requirement.class);
            
            if(this.TryGetRequirement(classElement, refRequirement, refShouldCreateNewTargetElement))
            {
                mappedElementRowViewModel = 
                        new MappedRequirementRowViewModel(refRequirement.Get(), classElement, MappingDirection.FromDstToHub);
            }
        }
        
        if(mappedElementRowViewModel != null)
        {
            mappedElementRowViewModel.SetShouldCreateNewTargetElement(refShouldCreateNewTargetElement.Get());
            mappedElementRowViewModel.SetRowStatus(Boolean.TRUE.equals(refShouldCreateNewTargetElement.Get()) ? MappedElementRowStatus.NewElement : MappedElementRowStatus.ExisitingElement);
            return mappedElementRowViewModel;
        }
        
        this.logger.warn(String.format("Impossible to map the provided class %s as it doesn't have a supported stereotype or is already present in the mapped rows", classElement.getName()));
        return null;
    }

    /**
     * Updates the {@linkplain MappedElementRowStatus} of the provided {@linkplain MappedElementRowViewModel}
     * 
     * @param mappedElementRowViewModel the {@linkplain MappedElementRowViewModel} of which to update the row status
     * @param clazz the {@linkplain java.lang.Class} of the {@linkplain Thing} represented in the {@linkplain MappedElementRowViewModel}
     */
    @Override
    protected <TThing extends Thing> void UpdateRowStatus(MappedElementRowViewModel<? extends Thing, ? extends Class> mappedElementRowViewModel, java.lang.Class<TThing> clazz)
    {
        Ref<TThing> refThing = new Ref<>(clazz);
        mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.None);
        
        if(mappedElementRowViewModel.GetShouldCreateNewTargetElementValue())
        {
            mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.NewElement);
        }
        else if(mappedElementRowViewModel.GetHubElement() != null)
        {            
            if(this.dstController.GetDstMapResult().stream()
                    .filter(x -> x.GetHubElement().getClass() == clazz)
                    .anyMatch(x -> AreTheseEquals(x.GetHubElement().getIid(), mappedElementRowViewModel.GetHubElement().getIid())))
            {
                mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.ExistingMapping);
            }
            else if(this.hubController.TryGetThingById(mappedElementRowViewModel.GetHubElement().getIid(), refThing))
            {
                mappedElementRowViewModel.SetRowStatus(MappedElementRowStatus.ExisitingElement);
            }
        }
    }

    /**
     * Gets or create an {@linkplain ElementDefinition} that can be mapped to the provided {@linkplain Class},
     * In the case the provided {@linkplain Class} is already represented in the {@linkplain mappedElements} returns false
     * 
     * @param classElement the {@linkplain Class} element
     * @param refElementDefinition the {@linkplain Ref} of {@linkplain ElementDefinition}
     * @param refShouldCreateNewTargetElement the {@linkplain Ref} of {@linkplain Boolean} indicating whether the target Hub element will be created
     * @return a value indicating whether the method execution was successful in getting a {@linkplain ElementDefinition}
     */
    private boolean TryGetElementDefinition(Class classElement, Ref<ElementDefinition> refElementDefinition, Ref<Boolean> refShouldCreateNewTargetElement)
    {
        if(this.mappedElements.stream().noneMatch(x-> AreTheseEquals(x.GetDstElement().getID(), classElement.getID())))
        {
            Optional<ElementDefinition> optionalElementDefinition =
                    this.hubController.GetOpenIteration().getElement().stream()
                    .filter(x -> AreTheseEquals(x.getName(), classElement.getName())).findFirst();
            
            if(optionalElementDefinition.isPresent())
            {
                refElementDefinition.Set(optionalElementDefinition.get().clone(true));
            }
            else
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
     * Gets or create an {@linkplain RequirementsSpecification} that can contained a {@linkplain Requirement} that will represent the provided {@linkplain Class},
     * In the case the provided {@linkplain Class} is already represented in the {@linkplain mappedElements} returns false
     * 
     * @param classElement the {@linkplain Class} element
     * @param refRequirementSpecification the {@linkplain Ref} of {@linkplain RequirementsSecification}
     * @param refShouldCreateNewTargetElement the {@linkplain Ref} of {@linkplain Boolean} indicating whether the target Hub element will be created
     * @return a value indicating whether the method execution was successful in getting a {@linkplain RequirementSpecification}
     */
    private boolean TryGetRequirement(Class classElement, Ref<cdp4common.engineeringmodeldata.Requirement> refRequirement, Ref<Boolean> refShouldCreateNewTargetElement)
    {
        Optional<cdp4common.engineeringmodeldata.Requirement> optionalRequirement = 
                this.hubController.GetOpenIteration().getRequirementsSpecification().stream()
                .flatMap(x -> x.getRequirement().stream())
                .filter(x -> AreTheseEquals(x.getName(), classElement.getName()))
                .findFirst();

        if(optionalRequirement.isPresent())
        {
            if(this.mappedElements.stream().anyMatch(x -> AreTheseEquals(x.GetHubElement().getIid(), optionalRequirement.get().getIid())
                    && AreTheseEquals(x.GetDstElement().getID(), classElement.getID())))
            {
                return false;
            }
            
            RequirementsSpecification requirementSpecification = optionalRequirement.get().getContainerOfType(RequirementsSpecification.class).clone(true);            
            refRequirement.Set(requirementSpecification.getRequirement().stream()
                    .filter(x -> AreTheseEquals(x.getIid(), optionalRequirement.get().getIid()))
                    .findFirst()
                    .get());
        }
        else
        {
            refShouldCreateNewTargetElement.Set(true);
            
            Ref<String> possibleParentName = new Ref<>(String.class);
            
            if(this.TryGetPossibleRequirementsSpecificationName(classElement, possibleParentName))
            {
                RequirementsSpecification requirementSpecification = this.hubController.GetOpenIteration().getRequirementsSpecification().stream()
                        .filter(x -> AreTheseEquals(possibleParentName.Get(), x.getName(), true))
                        .map(x -> x.clone(true))
                        .findFirst()
                        .orElseGet(() ->
                    {
                        RequirementsSpecification newRequirementsSpecification = new RequirementsSpecification();
                        newRequirementsSpecification.setName(possibleParentName.HasValue() ? possibleParentName.Get() : "new RequirementsSpecification");
                        newRequirementsSpecification.setShortName(GetShortName(possibleParentName.HasValue() ? possibleParentName.Get() : newRequirementsSpecification.getName()));
                        newRequirementsSpecification.setIid(UUID.randomUUID());
                        newRequirementsSpecification.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                        return newRequirementsSpecification;
                    });

                Requirement newRequirement = new cdp4common.engineeringmodeldata.Requirement();
                newRequirement.setName(classElement.getName());
                requirementSpecification.getRequirement().add(newRequirement);
                
                refRequirement.Set(newRequirement);
            }
        }

        return refRequirement.HasValue();
    }

    /**
     * Attempts to retrieve the parent of parent of the provided {@linkplain Class} element. 
     * Hence this is not always possible if the user decides to structure its SysML project differently.
     * However, this feature is only a nice to have.
     *  
     * @param classElement the {@linkplain Class} element to get the parent from
     * @return a value indicating whether the name of the parent was retrieved with success
     */
    private boolean TryGetPossibleRequirementsSpecificationName(Class classElement, Ref<String> possibleParentName)
    {
        try
        {
            possibleParentName.Set(((NamedElement)classElement.getOwner().getOwner()).getName());
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
            return false;
        }
        
        return possibleParentName.HasValue();
    }

    /**
     * Resets the pre-mapped things to the default way 
     */
    public void ResetPreMappedThings()
    {
        List<Element> elements = ((RootRowViewModel)this.magicDrawObjectBrowser.GetBrowserTreeModel().getRoot())
                .GetContainedRows()
                .stream()
                .map(x -> (Element)x.GetElement())
                .collect(Collectors.toList());
        
        this.UpdateProperties();
        this.PreMap(elements);
    }
    
    /**
     * Occurs when the user sets the target element of the current mapped element to be a
     * 
     * @param selected the new {@linkplain boolean} value
     */
    @Override
    public void WhenMapToNewElementCheckBoxChanged(boolean selected)
    {
        if(this.selectedMappedElement.Value() == null)
        {
            return;
        }
        
        if(selected && !this.selectedMappedElement.Value().GetShouldCreateNewTargetElementValue())
        {
            this.selectedMappedElement.Value().SetHubElement(null);
        }
        
        if(selected != this.selectedMappedElement.Value().GetShouldCreateNewTargetElementValue())
        {
            this.selectedMappedElement.Value().SetShouldCreateNewTargetElement(selected);
        }
        
        if(this.selectedMappedElement.Value().GetHubElement() instanceof ElementDefinition)
        {
            this.UpdateRowStatus(this.selectedMappedElement.Value(), ElementDefinition.class);
        }
        else if(this.selectedMappedElement.Value().GetHubElement() instanceof RequirementsSpecification)
        {
            this.UpdateRowStatus(this.selectedMappedElement.Value(), RequirementsSpecification.class);
        }
    }
}
