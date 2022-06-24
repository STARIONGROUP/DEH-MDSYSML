/*
 * MagicDrawSelectionService.java
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
package Services.MagicDrawSelection;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain MagicDrawSelectionService} handles the DST browser selection
 */
public class MagicDrawSelectionService implements IMagicDrawSelectionService
{
    /**
     * The {@linkplain Tree} active browser
     */
    private Tree activeBrowser;

    /**
     * The {@linkplain IElementDefinitionBrowserViewModel}
     */
    private final IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel;
    
    /**
     * The {@linkplain IRequirementBrowserViewModel}
     */
    private final IRequirementBrowserViewModel requirementBrowserViewModel;

    /**
     * Initializes a new {@linkplain MagicDrawSelectionService} 
     * 
     * @param elementDefinitionBrowserViewModel the {@linkplain IElementDefinitionBrowserViewModel} instance
     * @param requirementBrowserViewModel the {@linkplain IRequirementBrowserViewModel} instance
     */
    public MagicDrawSelectionService(IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel, IRequirementBrowserViewModel requirementBrowserViewModel)
    {
        this.elementDefinitionBrowserViewModel = elementDefinitionBrowserViewModel;
        this.requirementBrowserViewModel = requirementBrowserViewModel;        
    }
    
    /**
     * Gets the selected items from the {@linkplain ISelectionService}
     * 
     * @param <TElement> the type of {@linkplain Element} to return
     * @param elementClass the {@linkplain java.lang.Class} of {@linkplain #TElement}
     * @return a {@linkplain Collection} of {@linkplain Class}
     */
    @Override
    @Annotations.ExludeFromCodeCoverageGeneratedReport
    public <TElement extends Element> Collection<TElement> GetDstSelection(java.lang.Class<TElement> elementClass)
    {
        if(this.activeBrowser == null)
        {
            return Collections.emptyList();
        }
        
        return Arrays.asList(this.activeBrowser.getSelectedNodes())
                .stream()
                .filter(x -> elementClass.isInstance(x.getUserObject()))
                .map(x -> elementClass.cast(x.getUserObject()))
                .collect(Collectors.toList());
    }

    /**
     * Sets the {@linkplain Tree} active browser in order to compute the selection when callnig {@linkplain #GetDstSelection()}
     * 
     * @param activeBrowserTree the {@linkplain Tree}
     */
    @Override
    @Annotations.ExludeFromCodeCoverageGeneratedReport
    public void SetActiveBrowser(Tree activeBrowserTree)
    {
        this.activeBrowser = activeBrowserTree;
    }
    
    /**
     * Sorts the provided collection of {@linkplain Thing}
     * 
     * @param elementsToSort the {@linkplain ArrayList} of {@linkplain Thing} to sort
     * @return a {@linkplain Collection}of mappable {@linkplain Thing}s
     */
    @Override
    public Collection<Thing> GetHubSelection()
    {
        ArrayList<Thing> elementsToSort = new ArrayList<>();
        
        elementsToSort.addAll(this.elementDefinitionBrowserViewModel.GetSelectedElements().stream()
                .map(x -> x.GetThing())
                .filter(x -> x instanceof ElementDefinition)
                .collect(Collectors.toList()));
        
        elementsToSort.addAll(this.requirementBrowserViewModel.GetSelectedElements().stream()
                .map(x -> x.GetThing()).collect(Collectors.toList()));
                
        ArrayList<Thing> mappableThings = new ArrayList<>();
        
        mappableThings.addAll(elementsToSort.stream()
                .filter(x -> x instanceof ElementDefinition)
                .map(x -> (ElementDefinition)x)
                .collect(Collectors.toList()));
        
        this.SortRequirements(elementsToSort, mappableThings);
                
        return mappableThings;
    }

    /**
     * Sorts the {@linkplain cdp4common.engineeringmodeldata.Requirement} that can be mapped based on whatever container was selected in the requirement browser
     * 
     * @param elements the base collection of selected elements
     * @param hubRequirements the {@linkplain HuRequirementCollection} to pass on to the mapping rule
     */
    private void SortRequirements(ArrayList<Thing> elements, ArrayList<Thing> hubRequirements)
    {
        hubRequirements.addAll(elements.stream()
                .filter(x -> x instanceof cdp4common.engineeringmodeldata.Requirement)
                .map(x -> (cdp4common.engineeringmodeldata.Requirement)x)
                .filter(x -> !x.isDeprecated())
                .collect(Collectors.toList()));        

        for (RequirementsSpecification requirementsSpecification : elements.stream()
                .filter(x -> x instanceof RequirementsSpecification)
                .map(x -> (RequirementsSpecification)x)
                .collect(Collectors.toList()))
        {
            SortRequirements(hubRequirements, requirementsSpecification, null);
        }
        
        for (RequirementsGroup requirementsGroup : elements.stream()
                .filter(x -> x instanceof RequirementsGroup)
                .map(x -> (RequirementsGroup)x)
                .collect(Collectors.toList()))
        {            
            SortRequirements(hubRequirements, requirementsGroup.getContainerOfType(RequirementsSpecification.class), 
                    x -> x.getGroup() != null && AreTheseEquals(x.getGroup().getIid(), requirementsGroup.getIid()));
        }
    }

    /**
     * Sorts the {@linkplain cdp4common.engineeringmodeldata.Requirement} that can be mapped based on whatever container was selected in the requirement browser
     * 
     * @param elements the base collection of selected elements
     * @param hubRequirements the {@linkplain HuRequirementCollection} to pass on to the mapping rule
     * @param requirementsSpecification the {@linkplain RequirementsSpecification} that contains all the requirements
     * @param filterOnGroup a {@linkplain Predicate} to test if only the requirements contained in a certain group should be selected
     */
    private void SortRequirements(ArrayList<Thing> hubRequirements, RequirementsSpecification requirementsSpecification,
            Predicate<cdp4common.engineeringmodeldata.Requirement> filterOnGroup)
    {
        for (Requirement requirement : requirementsSpecification.getRequirement()
                .stream()
                .filter(x -> !x.isDeprecated())
                .collect(Collectors.toList()))
        {
            if(filterOnGroup == null || filterOnGroup.test(requirement))
            {
                hubRequirements.add(requirement);
            }
        }
    }
}
