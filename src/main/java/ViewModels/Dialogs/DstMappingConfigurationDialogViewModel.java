/*
 * DstMappingConfigurationDialogViewModel.java
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import DstController.IDstController;
import Enumerations.MappedElementRowStatus;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Utils.Ref;
import Utils.Stereotypes.StereotypeUtils;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Dialogs.Interfaces.IDstMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IMagicDrawObjectBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.BlockRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.RootRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementsSpecificationRowViewModel;
import Views.Dialogs.DstMappingConfigurationDialog;
import cdp4common.commondata.NamedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import io.reactivex.Observable;

/**
 * The {@linkplain DstMappingConfigurationDialogViewModel} is the main view model for the {@linkplain DstMappingConfigurationDialog}
 */
public class DstMappingConfigurationDialogViewModel implements IDstMappingConfigurationDialogViewModel
{
    /**
     * This view model logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IDstController}
     */
    private IDstController dstController;
    
    /**
     * The {@linkplain IHubController}
     */
    private IHubController hubController;
    
    /**
     * Backing field for {@linkplain GetElementDefinitionBrowserViewModel}
     */
    private IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel;
    
    /**
     * Gets the {@linkplain IElementDefinitionBrowserViewModel}
     * 
     * @return an {@linkplain IElementDefinitionBrowserViewModel}
     */
    @Override
    public IElementDefinitionBrowserViewModel GetElementDefinitionBrowserViewModel()
    {
        return this.elementDefinitionBrowserViewModel;
    }
    
    /**
     * Backing field for {@linkplain GetRequirementBrowserViewModel}
     */
    private IRequirementBrowserViewModel requirementBrowserViewModel;
    
    /**
     * Gets the {@linkplain IRequirementBrowserViewModel}
     * 
     * @return an {@linkplain IRequirementBrowserViewModel}
     */
    @Override
    public IRequirementBrowserViewModel GetRequirementBrowserViewModel()
    {
        return this.requirementBrowserViewModel;
    }

    /**
     * Backing field for {@linkplain GetMagicDrawObjectBrowserViewModel}
     */
    private IMagicDrawObjectBrowserViewModel magicDrawObjectBrowserViewModel;

    /**
     * Gets the {@linkplain IMagicDrawObjectBrowserViewModel}
     * 
     * @return an {@linkplain IMagicDrawObjectBrowserViewModel}
     */
    @Override
    public IMagicDrawObjectBrowserViewModel GetMagicDrawObjectBrowserViewModel()
    {
        return this.magicDrawObjectBrowserViewModel;
    }
    
    /**
     * The {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel} that represents all the mapped elements
     */
    private ObservableCollection<MappedElementRowViewModel<? extends Thing, Class>> mappedElements = new ObservableCollection<>();
    
    /**
     * Gets the collection of mapped element
     * 
     * @return {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel}
     */
    @Override
    public ObservableCollection<MappedElementRowViewModel<? extends Thing, Class>> GetMappedElementCollection()
    {
        return this.mappedElements;
    }

    /**
     * Backing field for {@linkplain GetSelectedMappedElement}
     */
    private ObservableValue<MappedElementRowViewModel<? extends Thing, Class>> selectedMappedElement = new ObservableValue<>(null);
    
    /**
     * The selected {@linkplain MappedElementRowViewModel}
     * 
     * @return a {@linkplain Observable} of {@linkplain MappedElementRowViewModel}
     */
    @Override
    public Observable<MappedElementRowViewModel<? extends Thing, Class>> GetSelectedMappedElement()
    {
        return this.selectedMappedElement.Observable();
    }

    /**
     * Sets the selectedMappedElement
     * 
     * @param mappedElement the {@linkplain MappedElementRowViewModel} that is to be selected
     */
    @Override
    public void SetSelectedMappedElement(MappedElementRowViewModel<? extends Thing, Class> mappedElement)
    {
        mappedElement.SetIsSelected(true);
        this.selectedMappedElement.Value(mappedElement);
    }
    
    /**
     * Backing field for {@linkplain GetShouldMapToNewHubElementCheckBoxBeEnabled}
     */
    private ObservableValue<Boolean> shouldMapToNewHubElementCheckBoxBeEnabled = new ObservableValue<>(true, Boolean.class);

    /**
     * Gets an {@linkplain Observable} value indicating whether the mapToNewHubElementCheckBox should be enabled 
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    public Observable<Boolean> GetShouldMapToNewHubElementCheckBoxBeEnabled()
    {
        return this.shouldMapToNewHubElementCheckBoxBeEnabled.Observable();
    }
    
    /**
     * Initializes a new {@linkplain DstMappingConfigurationDialogViewModel}
     * 
     * @param dstController the {@linkplain IDstController}
     * @param hubController the {@linkplain IHubController}
     * @param elementDefinitionBrowserViewModel the {@linkplain IElementDefinitionBrowserViewModel}
     * @param requirementBrowserViewModel the {@linkplain IRequirementBrowserViewModel}
     * @param magicDrawObjectBrowserViewModel the {@linkplain IMagicDrawObjectBrowserViewModel}
     */
    public DstMappingConfigurationDialogViewModel(IDstController dstController, IHubController hubController, 
            IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel, IRequirementBrowserViewModel requirementBrowserViewModel,
            IMagicDrawObjectBrowserViewModel magicDrawObjectBrowserViewModel)
    {
        this.elementDefinitionBrowserViewModel = elementDefinitionBrowserViewModel;
        this.requirementBrowserViewModel = requirementBrowserViewModel; 
        this.dstController = dstController;
        this.hubController = hubController;
        this.magicDrawObjectBrowserViewModel = magicDrawObjectBrowserViewModel;
        
        this.InitializeObservables();
        this.UpdateProperties();
    }
    
    /**
     * Initializes the {@linkplain Observable}s of this view model
     */
    private void InitializeObservables()
    {
        this.magicDrawObjectBrowserViewModel.GetSelectedElement()
            .subscribe(x -> this.UpdateMappedElements(x));
        
        this.elementDefinitionBrowserViewModel.GetSelectedElement()
            .subscribe(x -> this.SetHubElement(x.GetThing()));

        this.requirementBrowserViewModel.GetSelectedElement()
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
    private <TThing extends Thing & NamedThing> void SetHubElement(Thing thing, java.lang.Class<TThing> clazz)
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
        Optional<MappedElementRowViewModel<? extends Thing, Class>> optionalMappedElement = this.mappedElements.stream()
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
                mappedElement = new MappedRequirementsSpecificationRowViewModel(rowViewModel.GetElement(), MappingDirection.FromDstToHub);
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
    private void UpdateProperties()
    {
        this.mappedElements.clear();
                
        for (MappedElementRowViewModel<? extends Thing, Class> mappedElementRowViewModel : this.dstController.GetDstMapResult())
        {
            this.mappedElements.add(mappedElementRowViewModel);
        }
    }
    
    /**
     * Sets the mappedElement picked to open this dialog and sets the DST tree
     * 
     * @param selectedElement the collection of {@linkplain Element}
     */
    @Override
    public void SetMappedElement(Collection<Element> selectedElement)
    {
        this.magicDrawObjectBrowserViewModel.BuildTree(this.dstController.OpenDocument().getName(), selectedElement);
        this.UpdateProperties();
        this.PreMap(selectedElement);
    }
    
    /**
     * Pre-map the selected elements
     * 
     * @param selectedElement the collection of {@linkplain Element}
     */
    private void PreMap(Collection<Element> selectedElement)
    {
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
        
        if(StereotypeUtils.DoesItHaveTheStereotype(classElement, Stereotypes.Block))
        {
            Ref<ElementDefinition> refElementDefinition = new Ref<>(ElementDefinition.class);
            
            if(this.TryGetElementDefinition(classElement, refElementDefinition, refShouldCreateNewTargetElement))
            {
                mappedElementRowViewModel = 
                        new MappedElementDefinitionRowViewModel(refElementDefinition.Get(), classElement, MappingDirection.FromDstToHub);
            }
        }
        else if(StereotypeUtils.DoesItHaveTheStereotype(classElement, Stereotypes.Requirement))
        {
            Ref<RequirementsSpecification> refRequirementSpecification = new Ref<>(RequirementsSpecification.class);
            
            if(this.TryGetRequirementSpecification(classElement, refRequirementSpecification, refShouldCreateNewTargetElement))
            {
                mappedElementRowViewModel = 
                        new MappedRequirementsSpecificationRowViewModel(refRequirementSpecification.Get(), classElement, MappingDirection.FromDstToHub);
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
    private <TThing extends Thing> void UpdateRowStatus(MappedElementRowViewModel<? extends Thing, Class> mappedElementRowViewModel, java.lang.Class<TThing> clazz)
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
    private boolean TryGetRequirementSpecification(Class classElement, Ref<RequirementsSpecification> refRequirementSpecification, Ref<Boolean> refShouldCreateNewTargetElement)
    {
        Optional<RequirementsSpecification> optionalRequirementsSpecification = 
              this.hubController.GetOpenIteration().getRequirementsSpecification().stream()
              .flatMap(x -> x.getRequirement().stream())
              .filter(x -> AreTheseEquals(x.getName(), classElement.getName()))
              .map(x -> x.getContainerOfType(RequirementsSpecification.class))
              .findFirst();

        if(optionalRequirementsSpecification.isPresent())
        {
            UUID optionalRequirementsSpecificationIid = optionalRequirementsSpecification.get().getIid();
            
            if(this.mappedElements.stream().anyMatch(x -> AreTheseEquals(x.GetHubElement().getIid(), optionalRequirementsSpecificationIid)
                    && AreTheseEquals(x.GetDstElement().getID(), classElement.getID())))
            {
                return false;
            }
            
            refRequirementSpecification.Set(optionalRequirementsSpecification.get().clone(true));
        }
        else
        {
            refShouldCreateNewTargetElement.Set(true);
            
            Ref<String> possibleParentName = new Ref<>(String.class);
            
            if(this.TryGetPossibleRequirementsSpecificationName(classElement, possibleParentName))
            {
                optionalRequirementsSpecification = this.hubController.GetOpenIteration().getRequirementsSpecification().stream()
                    .filter(x -> AreTheseEquals(possibleParentName, x.getName()))
                    .findFirst();
                
                if(optionalRequirementsSpecification.isPresent())
                {
                    refRequirementSpecification.Set(optionalRequirementsSpecification.get().clone(true));
                }
            }
            
            RequirementsSpecification requirementsSpecification = new RequirementsSpecification();
            requirementsSpecification.setName(possibleParentName.HasValue() ? possibleParentName.Get() : "new RequirementsSpecification");
            requirementsSpecification.setShortName(GetShortName("-"));
            requirementsSpecification.setIid(UUID.randomUUID());
            requirementsSpecification.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            refRequirementSpecification.Set(requirementsSpecification);            
        }

        return refRequirementSpecification.HasValue();
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
        List<Element> elements = ((RootRowViewModel)this.magicDrawObjectBrowserViewModel.GetBrowserTreeModel().getRoot())
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
    public void WhenMapToNewHubElementCheckBoxChanged(boolean selected)
    {
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
