/*
 * RequirementMappingRule.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import HubController.IHubController;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;

import Services.MappingEngineService.MappingRule;
import Utils.Ref;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import Utils.Stereotypes.StereotypeUtils;
import Utils.Stereotypes.Stereotypes;
import cdp4common.commondata.Definition;
import cdp4common.commondata.ShortNamedThing;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.types.ContainerList;

/**
 * The {@linkplain BlockDefinitionMappingRule} is the mapping rule implementation for transforming {@linkplain MagicDrawRequirementCollection} to {@linkplain RequirementsSpecification}
 */
public class RequirementMappingRule extends MappingRule<MagicDrawRequirementCollection, List<RequirementsSpecification>>
{
    /**
     * The {@linkplain IHubController}
     */
    private IHubController hubController;

    /**
     * The collection of {@linkplain RequirementsSpecification} that are being mapped
     */
    private ArrayList<RequirementsSpecification> requirementsSpecifications = new ArrayList<RequirementsSpecification>();

    /**
     * The collection of {@linkplain RequirementsGroup} that are being mapped
     */
    private ArrayList<RequirementsGroup> temporaryRequirementsGroups = new ArrayList<RequirementsGroup>();
    
    /**
     * Initializes a new {@linkplain RequirementMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     */
    public RequirementMappingRule(IHubController hubController)
    {
        this.hubController = hubController;
    }    
    
    /**
     * Transforms an {@linkplain MagicDrawRequirementCollection} of type {@linkplain Class} to an {@linkplain ArrayList} of {@linkplain RequirementsSpecification}
     * 
     * @param input the {@linkplain MagicDrawRequirementCollection} to transform
     * @return the {@linkplain ArrayList} of {@linkplain ElementDefinition}
     */
    @Override
    public List<RequirementsSpecification> Transform(Object input)
    {
        try
        {
            this.Map(this.CastInput(input));
            return this.requirementsSpecifications;
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
            return new ArrayList<RequirementsSpecification>();
        }
        finally
        {
            this.requirementsSpecifications.clear();
            this.temporaryRequirementsGroups.clear();
        }
    }
    
    /**
     * Maps the provided collection of block
     * 
     * @param allRequirements the collection of {@linkplain Class} or requirements to map
     */
    private void Map(MagicDrawRequirementCollection allRequirements)
    {
        for (Class requirement : allRequirements)
        {
            try
            {
                Element parentPackage = requirement.getOwner();
                
                Ref<RequirementsSpecification> refRequirementsSpecification = new Ref<RequirementsSpecification>(RequirementsSpecification.class);
                   
                while (!(this.CanBeARequirementSpecification(parentPackage)
                        && parentPackage != null && parentPackage instanceof Package
                        && this.TryGetOrCreateRequirementSpecification((Package)parentPackage, refRequirementsSpecification)))
                {
                    parentPackage = parentPackage.getOwner();
                }
                
                if(!refRequirementsSpecification.HasValue())
                {
                    this.logger.error(
                            String.format("The mapping of the current requirement %s is no possible, because no eligible parent could be found current package name %s", 
                                    requirement.getName(), requirement.getOwner().getHumanName()));
                    
                    continue;
                }
    
                Ref<RequirementsGroup> refRequirementsGroup = new Ref<RequirementsGroup>(RequirementsGroup.class);
                Ref<Requirement> refRequirement = new Ref<Requirement>(Requirement.class);
                
                Collection<Element> parentElements = parentPackage.getOwnedElement();
                
                if(!TryCreateRelevantGroupsAndTheRequirement(requirement, parentElements, refRequirementsSpecification, refRequirementsGroup, refRequirement))
                {
                    this.logger.error(String.format("Could not map requirement %s", requirement.getName()));
                }
            }
            catch(Exception exception)
            {
                this.logger.catching(exception);
            }
        }
    }
    
    /**
     * Tries to create the groups between the current {@linkplain RequirementsSpecification} and the current {@linkplain Requirement} to be created,
     * and creates the {@linkplain Requirement}. This method is called recursively until the methods reaches the {@linkplain Requirement}
     *
     * @param requirement the {@linkplain Class} requirement from MagicDraw
     * @param elements the children of the current {@linkplain Package} being processed
     * @param refRequirementsSpecification the {@linkplain Ref} of {@linkplain RequirementsSpecification}
     * @param refRequirementsGroup the {@linkplain Ref} of {@linkplain RequirementsGroup}, 
     * holds the last group that was created, also the closest to the {@linkplain Requirement}
     * @param refRequirement the {@linkplain Ref} of {@linkplain Requirement}
     * @return a value indicating whether the requirement has been created/updated
     * @throws UnsupportedOperationException in case the {@linkplain Requirement could not be created}
     */
    private boolean TryCreateRelevantGroupsAndTheRequirement(Class requirement, Collection<Element> elements,
            Ref<RequirementsSpecification> refRequirementsSpecification, Ref<RequirementsGroup> refRequirementsGroup,
            Ref<Requirement> refRequirement)
    {
        for (Element element : elements)
        {
            if(element instanceof Package)
            {
                if(element.isParentOf(requirement))
                {                
                    if(!this.TryGetOrCreateRequirementGroup((Package)element, refRequirementsSpecification, refRequirementsGroup))
                    {
                        this.logger.error(String.format("Could not create the requirement %s, because the creation/update of the requirement group %s failed", 
                                requirement.getName(), ((Package)element).getName()));
                        
                        break;
                    }
                }
            }
                        
            else if(element instanceof Class && element.getID().equals(requirement.getID()))
            {
                if(!this.TryGetOrCreateRequirement((Class)element, refRequirementsSpecification, refRequirementsGroup, refRequirement))
                {
                    throw new UnsupportedOperationException(
                            String.format("Could not create the requirement %s", requirement.getName()));
                }
            }
            
            if(this.TryCreateRelevantGroupsAndTheRequirement(requirement, element.getOwnedElement(), refRequirementsSpecification, refRequirementsGroup, refRequirement))
            {
                break;
            }
        }
        
        return refRequirement.HasValue();
    }

    /**
     * Tries to get from the current {@linkplain RequirementsSpecification} the represented {@linkplain Requirement} by the provided {@linkplain Class} element
     * 
     * @param element the {@linkplain Class} element
     * @param refRequirementsGroup the {@linkplain Ref} of {@linkplain RequirementsGroup}, the closest parent in the tree of the {@linkplain Requirement}
     * @param refRequirement the {@linkplain Ref} of {@linkplain Requirement}
     * @return a value indicating whether the {@linkplain Requirement} has been created or retrieved
     */
    private boolean TryGetOrCreateRequirement(Class element, Ref<RequirementsSpecification> refRequirementsSpecification, Ref<RequirementsGroup> refRequirementsGroup, Ref<Requirement> refRequirement)
    {
        Optional<Requirement> optionalRequirement = refRequirementsSpecification.Get()
                .getRequirement()
                .stream()
                .filter(x -> this.AreShortNamesEquals(x, element))
                .findFirst();
        
        if(optionalRequirement.isPresent())
        {
            refRequirement.Set(optionalRequirement.get());
        }
        else
        {
            Requirement requirement = new Requirement();
            requirement.setIid(UUID.randomUUID());
            requirement.setName(element.getName());
            requirement.setShortName(this.GetShortName(element));
            requirement.setOwner(this.hubController.GetCurrentDomainOfExpertise());

            Optional<Slot> optionalTextSlot = element.getAppliedStereotypeInstance()
                    .getSlot()
                    .stream()
                    .filter(x -> x.getDefiningFeature().getName().equals("Text"))
                    .findFirst();

            if(optionalTextSlot.isPresent() && optionalTextSlot.get().getValue().get(0) instanceof LiteralString)
            {
                Definition definition = requirement.getDefinition()
                        .stream()
                        .filter(x -> x.getLanguageCode().toLowerCase().contains("en"))
                        .findFirst()
                        .orElse(this.createDefinition(requirement));
                
                definition.setContent(((LiteralString)optionalTextSlot.get().getValue().get(0)).getValue());
            }
            
            requirement.setGroup(refRequirementsGroup.Get());
            refRequirementsSpecification.Get().getRequirement().add(requirement);
            refRequirement.Set(requirement);
        }
        
        return refRequirement.HasValue();
    }

    /**
     * Creates a {@linkplain Definition} to be added to a {@linkplain Requirement}
     * 
     * @param requirement the {@linkplain Requirement} owner of the definition
     * @return a {@linkplain Definition}
     */
    private Definition createDefinition(Requirement requirement)
    {
        Definition definition = new Definition();
        definition.setIid(UUID.randomUUID());
        definition.setLanguageCode("en-us");
        requirement.getDefinition().add(definition);
        return definition;
    }

    /**
     * Try to create the {@linkplain RequirementsSpecification} represented by the provided {@linkplain Package}
     * 
     * @param currentPackage the {@linkplain Package} to create or retrieve the {@linkplain RequirementsSpecification} that represents it
     * @param refRequirementsSpecification the {@linkplain Ref} parent {@linkplain RequirementsSpecification}
     * @param refRequirementsGroup the {@linkplain Ref} of {@linkplain RequirementsGroup}
     * @return a value indicating whether the {@linkplain RequirementsGroup} has been found or created
     */
    private boolean TryGetOrCreateRequirementGroup(Package currentPackage, Ref<RequirementsSpecification> refRequirementsSpecification, Ref<RequirementsGroup> refRequirementsGroup)
    {
        Optional<RequirementsGroup> optionalRequirementsSpecification = this.temporaryRequirementsGroups
                .stream()
                .filter(x -> this.AreShortNamesEquals(x, currentPackage))
                .findFirst();

        if(optionalRequirementsSpecification.isPresent())
        {
            refRequirementsGroup.Set(
                    refRequirementsSpecification.Get().getOriginal() != null 
                        ? optionalRequirementsSpecification.get().clone(false) 
                            : optionalRequirementsSpecification.get());
        }
        else
        {
            RequirementsGroup requirementsgroup = new RequirementsGroup();
            requirementsgroup.setName(currentPackage.getName());
            requirementsgroup.setShortName(this.GetShortName(currentPackage));
            requirementsgroup.setIid(UUID.randomUUID());
            requirementsgroup.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            
            if(refRequirementsGroup.HasValue())
            {
                refRequirementsGroup.Get().getGroup().add(requirementsgroup);
            }
            else
            {
                refRequirementsSpecification.Get().getGroup().add(requirementsgroup);                
            }
            
            refRequirementsGroup.Set(requirementsgroup);
            this.temporaryRequirementsGroups.add(requirementsgroup);
        }
        
        return refRequirementsGroup.HasValue();
    }
    
    /**
     * Try to create the {@linkplain RequirementsSpecification} represented by the provided {@linkplain Package}
     * 
     * @param currentPackage the {@linkplain Package} to create or retrieve the {@linkplain RequirementsSpecification} that represents it
     * @param refRequirementSpecification the {@linkplain Ref} of {@linkplain RequirementsSpecification}
     * @return a value indicating whether the {@linkplain RequirementsGroup} has been found or created
     */
    private boolean TryGetOrCreateRequirementSpecification(Package currentPackage, Ref<RequirementsSpecification> refRequirementSpecification)
    {
        Optional<RequirementsSpecification> optionalRequirementsSpecification = this.requirementsSpecifications
                .stream()
                .filter(x -> this.AreShortNamesEquals(x, currentPackage))
                .findFirst();

        if(optionalRequirementsSpecification.isPresent())
        {
            refRequirementSpecification.Set(optionalRequirementsSpecification.get());
        }
        else
        {
            optionalRequirementsSpecification = this.hubController.GetOpenIteration()
                    .getRequirementsSpecification()
                    .stream()
                    .filter(x -> this.AreShortNamesEquals(x, currentPackage))
                    .findFirst();
            
            if(optionalRequirementsSpecification.isPresent())
            {
                refRequirementSpecification.Set(optionalRequirementsSpecification.get().clone(false));
            }
            else
            {
                RequirementsSpecification requirementsSpecification = new RequirementsSpecification();
                requirementsSpecification.setName(currentPackage.getName());
                requirementsSpecification.setShortName(this.GetShortName(currentPackage));
                requirementsSpecification.setIid(UUID.randomUUID());
                requirementsSpecification.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                refRequirementSpecification.Set(requirementsSpecification);             
            }

            this.requirementsSpecifications.add(refRequirementSpecification.Get());
        }
        
        return refRequirementSpecification.HasValue();
    }
    
    /**
     * Compares the two specified things to determine if they have the same shortName
     * 
     * @param shortNamedThing the 10-25 {@linkplain ShortNamedThing}
     * @param parentPackage the MagicDraw {@linkplain NamedElement}
     * @return a value indicating whether the two element have the same short name
     */
    private boolean AreShortNamesEquals(ShortNamedThing shortNamedThing, NamedElement element)
    {
        return shortNamedThing.getShortName().compareToIgnoreCase(this.GetShortName(element)) == 0;
    }

    /**
     * Gets the short name of the specified {@linkplain NamedElement}
     * 
     * @param element the {@linkplain NamedElement} to get the short name from
     * @return a string containing the short name
     */
    private String GetShortName(NamedElement element)
    {
        return element.getName().replaceAll("[^a-zA-Z0-9]|\\s", "");
    }

    /**
     * Verifies if the provided {@linkplain Element} contains any requirement
     * 
     * @param element the current {@linkplain Element} to verify
     * @return a value indicating whether the {@linkplain Element} has any requirement as child
     */
    public boolean CanBeARequirementSpecification(Element element)
    {
        return !element.getOwnedElement().stream().anyMatch(x -> StereotypeUtils.DoesItHaveTheStereotype(x, Stereotypes.Requirement));
    }
}
