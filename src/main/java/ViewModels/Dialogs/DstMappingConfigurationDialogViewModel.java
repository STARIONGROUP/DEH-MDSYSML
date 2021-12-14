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

import java.util.Collection;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import DstController.IDstController;
import Enumerations.MappingDirection;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Services.MappingEngineService.IMappableThingCollection;
import ViewModels.Dialogs.Interfaces.IDstMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IMagicDrawObjectBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.BlockRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementsSpecificationRowViewModel;
import Views.Dialogs.DstMappingConfigurationDialog;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
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
     * Initializes a new {@linkplain DstMappingConfigurationDialogViewModel}
     * 
     * @param dstController the {@linkplain IDstController}
     * @param elementDefinitionBrowserViewModel the {@linkplain IElementDefinitionBrowserViewModel}
     * @param requirementBrowserViewModel the {@linkplain IRequirementBrowserViewModel}
     * @param magicDrawObjectBrowserViewModel the {@linkplain IMagicDrawObjectBrowserViewModel}
     */
    public DstMappingConfigurationDialogViewModel(IDstController dstController, IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel,
            IRequirementBrowserViewModel requirementBrowserViewModel, IMagicDrawObjectBrowserViewModel magicDrawObjectBrowserViewModel)
    {
        this.elementDefinitionBrowserViewModel = elementDefinitionBrowserViewModel;
        this.requirementBrowserViewModel = requirementBrowserViewModel; 
        this.dstController = dstController;
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
    }

    /**
     * Sets the Hub element on the selected element if the element is compatible
     * 
     * @param thing the {@linkplain Thing} to assign
     */
    @SuppressWarnings("unchecked")
    private void SetHubElement(Thing thing)
    {
        if(this.selectedMappedElement.Value() == null)
        {
            return;
        }
        
        if(thing instanceof ElementDefinition 
                && this.selectedMappedElement.Value().GetTThingClass().isAssignableFrom(ElementDefinition.class))
        {
            ((MappedElementRowViewModel<ElementDefinition, Class>)this.selectedMappedElement.Value())
                .SetHubElement((ElementDefinition)thing.clone(true));
        }
        else if(thing instanceof RequirementsSpecification 
                && this.selectedMappedElement.Value().GetTThingClass().isAssignableFrom(RequirementsSpecification.class))
        {
            ((MappedElementRowViewModel<RequirementsSpecification, Class>)this.selectedMappedElement.Value())
                .SetHubElement((RequirementsSpecification)thing.clone(true));
        }
        else
        {
            this.logger.debug("Thing is not compatible with the current selected mapped element!");
        }
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
    }
    
    /**
     * Occurs when the user sets the target element of the current mapped element to be a
     * 
     * @param selected the new {@linkplain boolean} value
     */
    @Override
    public void WhenMapToNewHubElementCheckBoxChanged(boolean selected)
    {
        this.selectedMappedElement.Value().SetShouldCreateNewTargetElement(selected);
    }
}
