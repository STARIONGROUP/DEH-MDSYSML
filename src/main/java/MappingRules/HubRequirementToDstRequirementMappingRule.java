/*
 * HubRequirementToDstRequirementMappingRule.java
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
package MappingRules;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Optional;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import App.AppContainer;
import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.HubRequirementCollection;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import Utils.Stereotypes.RequirementType;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.commondata.Definition;
import cdp4common.commondata.NamedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.Category;

/**
 * The {@linkplain HubRequirementToDstRequirementMappingRule} is the mapping rule implementation for transforming {@linkplain HubRequirementCollection} to Magic Draw {@linkplain Requirement}
 */
public class HubRequirementToDstRequirementMappingRule extends HubToDstBaseMappingRule<HubRequirementCollection, ArrayList<MappedRequirementRowViewModel>>
{
    /**
     * The collection of {@linkplain RequirementsSpecification} that are being mapped
     */
    private ArrayList<RequirementsSpecification> requirementsSpecifications = new ArrayList<>();

    /**
     * The temporary collection of {@linkplain RequirementsPkg} that were created during this mapping
     */
    private ArrayList<Package> temporaryRequirementsContainer = new ArrayList<>();

    /**
     * Initializes a new {@linkplain HubRequirementToDstRequirementMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain IMagicDrawMappingConfigurationService}
     * @param transactionService the {@linkplain IMagicDrawTransactionService}
     * @param stereotypeService the {@linkplain IStereotypeService}
     */
    public HubRequirementToDstRequirementMappingRule(IHubController hubController, IMagicDrawMappingConfigurationService mappingConfiguration, 
            IMagicDrawTransactionService transactionService, IStereotypeService stereotypeService)
    {
        super(hubController, mappingConfiguration, transactionService, stereotypeService);
    }
    
    /**
     * Transforms an {@linkplain MagicDrawRequirementCollection} of type {@linkplain Class} to an {@linkplain ArrayList} of {@linkplain RequirementsSpecification}
     * 
     * @param input the {@linkplain MagicDrawRequirementCollection} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedDstRequirementRowViewModel}
     */
    @Override
    public ArrayList<MappedRequirementRowViewModel> Transform(Object input)
    {
        try
        {
            if(this.dstController == null)
            {
                this.dstController = AppContainer.Container.getComponent(IDstController.class);
            }
            
            HubRequirementCollection mappedElements = this.CastInput(input);
            this.Map(mappedElements);
            
            this.SaveMappingConfiguration(mappedElements, MappingDirection.FromHubToDst);
            return new ArrayList<>(mappedElements);
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
            return new ArrayList<>();
        }
        finally
        {
            this.requirementsSpecifications.clear();
            this.temporaryRequirementsContainer.clear();
        }
    }
    
    /**
     * Maps the provided collection of requirements
     * 
     * @param mappedRequirements the collection of {@linkplain Requirement} to map
     */
    private void Map(HubRequirementCollection mappedRequirements)
    {
        for (MappedRequirementRowViewModel mappedRequirementRowViewModel : mappedRequirements)
        {
            if(mappedRequirementRowViewModel.GetDstElement() == null)
            {
                mappedRequirementRowViewModel.SetDstElement(this.GetOrCreateRequirement(mappedRequirementRowViewModel));
            }
            
            this.UpdateProperties(mappedRequirementRowViewModel.GetHubElement(), mappedRequirementRowViewModel.GetDstElement());            
            this.UpdateOrCreateRequirementPackages(mappedRequirementRowViewModel.GetHubElement(), mappedRequirementRowViewModel.GetDstElement());
        }
    }

    /**
     * Updates the target requirement properties
     * 
     * @param hubRequirement the {@linkplain cdp4common.engineeringmodeldata.Requirement} element that represents the requirement in the Hub
     * @param requirement the {@linkplain Class} requirement to update
     */
    private void UpdateProperties(cdp4common.engineeringmodeldata.Requirement hubRequirement, Class dstRequirement)
    {
        this.logger.debug(String.format("%s - %s %s", this.transactionService.GetRequirementId(dstRequirement), dstRequirement.getName(), 
                this.transactionService.GetRequirementText(dstRequirement)));
        
        this.transactionService.SetRequirementId(dstRequirement, hubRequirement.getShortName());
        dstRequirement.setName(hubRequirement.getName());
        this.UpdateOrCreateDefinition(hubRequirement, dstRequirement);
        
        this.logger.debug(String.format("%s - %s %s", this.transactionService.GetRequirementId(dstRequirement), dstRequirement.getName(), 
                this.transactionService.GetRequirementText(dstRequirement)));
    }

    /**
     * Gets or creates the {@linkplain RequirementsPkg} that can represent the {@linkplain RequirementsSpecification}
     * 
     * @param mappedRequirementRowViewModel the {@linkplain MappedDstRequirementRowViewModel}
     * @return a {@linkplain RequirementsPkg}
     */
    private Class GetOrCreateRequirement(MappedRequirementRowViewModel mappedRequirementRowViewModel)
    {
        Ref<RequirementType> refRequirementType = new Ref<>(RequirementType.class);
        
        if(!this.TryGetRequirementType(mappedRequirementRowViewModel.GetHubElement(), refRequirementType))
        {
            refRequirementType.Set(RequirementType.Requirement);
        }
        
        return this.GetOrCreateRequirement(mappedRequirementRowViewModel.GetHubElement());
    }

    /**
     * Gets or creates the {@linkplain RequirementsPkg} that can represent the {@linkplain RequirementsSpecification}
     * 
     * @param hubRequirement the {@linkplain cdp4common.engineeringmodeldata.Requirement}
     * @param targetArchitecture the {@linkplain CapellaArchitecture}
     * @param requirementType the {@linkplain Class} of {@linkplain Requirement}
     * @return a {@linkplain Class} requirement
     */
    private Class GetOrCreateRequirement(cdp4common.engineeringmodeldata.Requirement hubRequirement)
    {
        Ref<Class> refElement = new Ref<>(Class.class);
        
        if(!this.dstController.TryGetElementBy(x -> x instanceof NamedElement && 
                AreTheseEquals(((NamedElement) x).getName(), hubRequirement.getName(), true), refElement))
        {        
            Class newRequirement = this.transactionService.Create(Stereotypes.Requirement, hubRequirement.getName());
            refElement.Set(newRequirement);
        }
        else
        {
            refElement.Set(this.transactionService.Clone(refElement.Get()));
        }
        
        return refElement.Get();
    }
    
    /**
     * Updates or creates the definition according to the provided {@linkplain Requirement} assignable to the {@linkplain Requirement}  
     * 
     * @param hubRequirement the {@linkplain cdp4common.engineeringmodeldata.Requirement} element that represents the requirement in the Hub
     * @param requirement the {@linkplain Requirement} to update
     */
    private void UpdateOrCreateDefinition(cdp4common.engineeringmodeldata.Requirement hubRequirement, Class requirement)
    {
        if(!hubRequirement.getDefinition().isEmpty())
        {
            Definition definition = hubRequirement.getDefinition()
                    .stream()
                    .filter(x -> x.getLanguageCode().equalsIgnoreCase("en"))
                    .findFirst()
                    .orElse(hubRequirement.getDefinition().get(0));            

            this.transactionService.SetRequirementText(requirement, definition.getContent());
        }
        else
        {
            this.transactionService.SetRequirementText(requirement, "");
        }
    }
    
    /**
     * Gets the {@linkplain RequirementType} based on the {@linkplain Category} applied to the provided {@linkplain cdp4common.engineeringmodeldata.Requirement}
     * 
     * @param requirement the {@linkplain cdp4common.engineeringmodeldata.Requirement}
     * @param refRequirementType the {@linkplain Ref} of {@linkplain RequirementType}
     * @return a {@linkplain boolean} indicating whether the {@linkplain RequirementType} is different than the default value
     */
    private boolean TryGetRequirementType(cdp4common.engineeringmodeldata.Requirement requirement, Ref<RequirementType> refRequirementType)
    {
        for (Category category : requirement.getCategory())
        {
            RequirementType requirementType = RequirementType.From(category.getName());
            
            if(requirementType != null)
            {
                refRequirementType.Set(requirementType);
                break;
            }
        }
        
        return refRequirementType.HasValue();
    }
    
    /**
     * Gets or creates the {@linkplain RequirementsPkg} that can represent the {@linkplain RequirementsSpecification} or one of its {@linkplain RequirementsGroup}
     * 
     * @param hubRequirement the {@linkplain cdp4common.engineeringmodeldata.Requirement} element that represents the requirement in the Hub
     * @param requirement the {@linkplain Class} requirement to update
     */
    private void UpdateOrCreateRequirementPackages(cdp4common.engineeringmodeldata.Requirement hubRequirement, Class requirement)
    {
        RequirementsSpecification hubRequirementSpecification = hubRequirement.getContainerOfType(RequirementsSpecification.class);
        
        Package requirementsSpecification = this.GetOrCreateRequirementContainer(hubRequirementSpecification);
                
        RequirementsGroup container = hubRequirement.getGroup();
        NamedElement lastChild = requirement;
        
        while(container != null && !AreTheseEquals(container.getIid(), hubRequirementSpecification.getIid()))
        {
            Package newGroup = this.GetOrCreateRequirementContainer(container);

            this.AddOrUpdateContainement(lastChild, newGroup);
            
            Thing upperContainer = container.getContainer();            
            container = upperContainer instanceof RequirementsGroup ? (RequirementsGroup)upperContainer : null;
            lastChild = newGroup;
        }

        this.AddOrUpdateContainement(lastChild, requirementsSpecification);
    }

    /**
     * Adds or update the containedElement to the container
     * 
     * @param containedElement the {@linkplain CapellaElement} contained element
     * @param container the {@linkplain RequirementsPkg} container
     */
    private <TElement extends Element> void AddOrUpdateContainement(TElement containedElement, Package container)
    {
        container.getOwnedElement().removeIf(x -> AreTheseEquals(x.getID(), containedElement.getID()));
        container.getOwnedElement().add(containedElement);
    }
    
    /**
     * Gets or creates the {@linkplain RequirementsPkg} that can represent the {@linkplain RequirementsSpecification} or one of its {@linkplain RequirementsGroup} 
     * 
     * @param thingContainer the {@linkplain cdp4common.engineeringmodeldata.Requirement} element that represents the requirement in the Hub
     * @param targetArchitecture the {@linkplain CapellaArchitecture}
     * @return a {@linkplain RequirementsPkg}
     */
    private <TThing extends NamedThing> Package GetOrCreateRequirementContainer(TThing thingContainer)
    {
        Ref<Package> refElement = new Ref<>(Package.class);
        
        Optional<Package> existingContainer = this.temporaryRequirementsContainer.stream()
                .filter(x -> AreTheseEquals(((NamedElement) x).getName(), thingContainer.getName(), true))
                .findFirst();
        
        if(existingContainer.isPresent())
        {
            refElement.Set(existingContainer.get());
        }
        else
        {
            if(!this.dstController.TryGetElementBy(x -> x instanceof NamedElement && 
                    AreTheseEquals(((NamedElement) x).getName(), thingContainer.getName(), true), refElement))
            {        
                Package newRequirementsPackage = this.transactionService.Create(Package.class, thingContainer.getName());
                this.temporaryRequirementsContainer.add(newRequirementsPackage);
                refElement.Set(newRequirementsPackage);
            }
            else
            {
                refElement.Set(this.transactionService.Clone(refElement.Get()));
            }
        }
        
        return refElement.Get();
    }    
}
