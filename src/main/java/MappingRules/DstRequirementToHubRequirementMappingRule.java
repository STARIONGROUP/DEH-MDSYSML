/*
 * DstRequirementToHubRequirementMappingRule.java
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
import static Utils.Stereotypes.StereotypeUtils.GetShortName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.nomagic.magicdraw.sysml.util.MDCustomizationForSysMLProfile;
import com.nomagic.requirements.util.RequirementUtilities;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import Utils.Stereotypes.RequirementType;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Definition;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain BlockToElementMappingRule} is the mapping rule implementation for transforming {@linkplain MagicDrawRequirementCollection} to {@linkplain RequirementsSpecification}
 */
public class DstRequirementToHubRequirementMappingRule extends DstToHubBaseMappingRule<MagicDrawRequirementCollection, ArrayList<MappedRequirementRowViewModel>>
{
    /**
     * The collection of {@linkplain RequirementsSpecification} that are being mapped
     */
    private ArrayList<RequirementsSpecification> requirementsSpecifications = new ArrayList<>();

    /**
     * The collection of {@linkplain RequirementsGroup} that are being mapped
     */
    private ArrayList<RequirementsGroup> temporaryRequirementsGroups = new ArrayList<>();

    /**
     * The {@linkplain IMagicDrawTransactionService}
     */
    private final IMagicDrawTransactionService transactionService;

    /**
     * Initializes a new {@linkplain DstRequirementToHubRequirementMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain IMagicDrawMappingConfigurationService}
     * @param mappingConfiguration the {@linkplain IMagicDrawTransactionService}
     * @param stereotypeService the {@linkplain IStereotypeService}
     */
    public DstRequirementToHubRequirementMappingRule(IHubController hubController, IMagicDrawMappingConfigurationService mappingConfiguration,
            IMagicDrawTransactionService transactionService, IStereotypeService stereotypeService)
    {
        super(hubController, mappingConfiguration, stereotypeService);
        this.transactionService = transactionService;
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
            MagicDrawRequirementCollection mappedElements = this.CastInput(input);
            this.Map(mappedElements);
            this.SaveMappingConfiguration(mappedElements, MappingDirection.FromDstToHub);
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
            this.temporaryRequirementsGroups.clear();
        }
    }
    
    /**
     * Maps the provided collection of block
     * 
     * @param mappedRequirements the collection of {@linkplain Class} or requirements to map
     */
    private void Map(MagicDrawRequirementCollection mappedRequirements)
    {
        for (MappedRequirementRowViewModel mappedRequirement : mappedRequirements)
        {
            try
            {
                Element parentPackage = mappedRequirement.GetDstElement().getOwner();
                
                Ref<RequirementsSpecification> refRequirementsSpecification = new Ref<>(RequirementsSpecification.class);
                   
                if(!mappedRequirement.GetShouldCreateNewTargetElementValue() && mappedRequirement.GetHubElement() != null)
                {
                    refRequirementsSpecification.Set(mappedRequirement.GetHubElement().getContainerOfType(RequirementsSpecification.class));
                }
                else
                {
                    while (parentPackage != null 
                            && !(this.CanBeARequirementSpecification(parentPackage)
                            && parentPackage instanceof Package
                            && this.TryGetOrCreateRequirementSpecification((Package)parentPackage, refRequirementsSpecification)))
                    {
                        parentPackage = parentPackage.getOwner();
                    }
                }
                
                if(!refRequirementsSpecification.HasValue())
                {
                    this.logger.error(
                            String.format("The mapping of the current requirement %s is not possible, because no eligible parent could be found current package name %s", 
                                    mappedRequirement.GetDstElement().getName(), mappedRequirement.GetDstElement().getOwner().getHumanName()));
                    
                    continue;
                }
    
                Ref<RequirementsGroup> refRequirementsGroup = new Ref<>(RequirementsGroup.class);
                Ref<Requirement> refRequirement = new Ref<>(Requirement.class);
                
                Collection<Element> parentElements = parentPackage.getOwnedElement();
                
                if(!TryCreateRelevantGroupsAndTheRequirement(mappedRequirement.GetDstElement(), parentElements, refRequirementsSpecification, refRequirementsGroup, refRequirement))
                {
                    this.logger.error(String.format("Could not map requirement %s", mappedRequirement.GetDstElement().getName()));
                }
                
                if(refRequirement.HasValue())
                {
                    this.UpdateProperties(mappedRequirement.GetDstElement(), refRequirementsSpecification, refRequirement);
                }

                mappedRequirement.SetHubElement(refRequirement.Get());
            }
            catch(Exception exception)
            {
                this.logger.catching(exception);
            }
        }
    }
    
   /**
    * Updates the target {@linkplain cdp4common.engineeringmodeldata.Requirement} properties
    * 
    * @param dstRequirement the source {@linkplain Requirement}
    * @param refRequirementsSpecification the {@linkplain Ref} of {@linkplain RequirementsSpecification} container
    * @param refRequirement the {@linkplain Ref} of {@linkplain cdp4common.engineeringmodeldata.Requirement} target
    */
   private void UpdateProperties(Class dstRequirement,
           Ref<RequirementsSpecification> refRequirementsSpecification,
           Ref<cdp4common.engineeringmodeldata.Requirement> refRequirement)
   {
       this.UpdateOrCreateDefinition(dstRequirement, refRequirement);
       refRequirement.Get().setName(dstRequirement.getName());
       refRequirement.Get().setShortName(this.transactionService.GetRequirementId(dstRequirement));

       refRequirementsSpecification.Get().getRequirement().removeIf(x -> AreTheseEquals(x.getIid(), refRequirement.Get().getIid()));
       refRequirementsSpecification.Get().getRequirement().add(refRequirement.Get());

       this.MapCategories(dstRequirement, refRequirement.Get());
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
                        
            else if(element instanceof Class && AreTheseEquals(element.getID(), requirement.getID()))
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
                .filter(x -> this.AreShortNamesEquals(x, GetShortName(element)) && !x.isDeprecated())
                .findFirst();
        
        if(optionalRequirement.isPresent())
        {
            refRequirement.Set(optionalRequirement.get().clone(true));
        }
        else
        {
            Requirement requirement = new Requirement();
            requirement.setIid(UUID.randomUUID());
            requirement.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            requirement.setGroup(refRequirementsGroup.Get());
            refRequirement.Set(requirement);
        }

        refRequirementsSpecification.Get().getRequirement().removeIf(x -> AreTheseEquals(x.getIid(), refRequirement.Get().getIid()));
        refRequirementsSpecification.Get().getRequirement().add(refRequirement.Get());
        
        return refRequirement.HasValue();
    }

    /**
     * Applies category onto the created {@linkplain Requirement}
     * 
     * @param element the {@linkplain Class} element
     * @param requirement the 10-25 requirement
     */
    private void MapCategories(Class element, Requirement requirement)
    {
        for(Stereotype stereotype : this.stereotypeService.GetAllStereotype(element))
        {
            RequirementType requirementType = RequirementType.From(stereotype);
            
            if(requirementType != null)
            {
                this.logger.debug(String.format("MAP CATEGORY %s, %s, %s", stereotype.getName(), stereotype.getHumanName(), stereotype.getHumanType()));
                this.MapCategory(requirement, requirementType.name(), ClassKind.Requirement);
            }
        }
    }

    /**
     * Updates or creates the definition according to the provided {@linkplain Class} assignable to the {@linkplain Requirement}  
     * 
     * @param element the {@linkplain Class} element that represents the requirement in MagicDraw
     * @param refRequirement the {@linkplain Ref} of {@linkplain Requirement} to update
     */
    private void UpdateOrCreateDefinition(Class element, Ref<Requirement> refRequirement)
    {
        if(refRequirement.HasValue())
        {            
            Definition definition = refRequirement.Get().getDefinition()
                    .stream()
                    .filter(x -> x.getLanguageCode().equalsIgnoreCase("en"))
                    .findFirst()
                    .map(x -> x.clone(true))
                    .orElse(this.createDefinition());

            String requirementText  = this.stereotypeService.GetRequirementText(element);
            definition.setContent(StringUtils.isBlank(requirementText) ? "-" : requirementText);
            
            refRequirement.Get().getDefinition().removeIf(x -> AreTheseEquals(x.getIid(), definition.getIid()));
            
            refRequirement.Get().getDefinition().add(definition);
        }
    }
    
    /**
     * Creates a {@linkplain Definition} to be added to a {@linkplain Requirement}
     * 
     * @return a {@linkplain Definition}
     */
    private Definition createDefinition()
    {
        Definition definition = new Definition();
        definition.setIid(UUID.randomUUID());
        definition.setLanguageCode("en");
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
        Ref<RequirementsGroup> refCurrentRequirementsGroup = new Ref<>(RequirementsGroup.class);
        
        if(this.TryToFindGroup(currentPackage, refRequirementsSpecification, refCurrentRequirementsGroup))
        {
            refRequirementsGroup.Set(refCurrentRequirementsGroup.Get());
        }
        else
        {
            RequirementsGroup requirementsgroup = new RequirementsGroup();
            requirementsgroup.setName(currentPackage.getName());
            requirementsgroup.setShortName(GetShortName(currentPackage));
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
     * Tries to find the group represented by / representing the provided {@linkplain Package}
     * 
     * @param currentPackage the {@linkplain Package}
     * @param refRequirementsSpecification the {@linkplain Ref} of the current {@linkplain RequirementsSpecification}
     * @param refRequirementsGroup the {@linkplain Ref} of {@linkplain RequirementsGroup}
     * @return a value indicating whether the {@linkplain RequirementsGroup} has been found
     */
    private boolean TryToFindGroup(Package currentPackage, Ref<RequirementsSpecification> refRequirementsSpecification, Ref<RequirementsGroup> refRequirementsGroup)
    {
        Optional<RequirementsGroup> optionalRequirementsGroup = Stream.concat(this.temporaryRequirementsGroups.stream(), 
                refRequirementsSpecification.Get().getAllContainedGroups().stream())
            .filter(x -> this.AreShortNamesEquals(x, GetShortName(currentPackage)))
            .findFirst();
        
        if(optionalRequirementsGroup.isPresent())
        {
            refRequirementsGroup.Set(optionalRequirementsGroup.get().getRevisionNumber() > 0 
                    ? optionalRequirementsGroup.get().clone(true)
                            : optionalRequirementsGroup.get());
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
                .filter(x -> this.AreShortNamesEquals(x, GetShortName(currentPackage)))
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
                    .filter(x -> this.AreShortNamesEquals(x, GetShortName(currentPackage)))
                    .findFirst();
            
            if(optionalRequirementsSpecification.isPresent())
            {
                refRequirementSpecification.Set(optionalRequirementsSpecification.get().clone(true));
            }
            else
            {
                RequirementsSpecification requirementsSpecification = new RequirementsSpecification();
                requirementsSpecification.setName(currentPackage.getName());
                requirementsSpecification.setShortName(GetShortName(currentPackage));
                requirementsSpecification.setIid(UUID.randomUUID());
                requirementsSpecification.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                refRequirementSpecification.Set(requirementsSpecification);             
            }
        
            this.requirementsSpecifications.add(refRequirementSpecification.Get());
        }
        
        return refRequirementSpecification.HasValue();
    }
    
    /**
     * Verifies if the provided {@linkplain Element} contains any requirement
     * 
     * @param element the current {@linkplain Element} to verify
     * @return a value indicating whether the {@linkplain Element} has any requirement as child
     */
    public boolean CanBeARequirementSpecification(Element element)
    {
        return !element.getOwnedElement().stream().anyMatch(x -> this.stereotypeService.DoesItHaveTheStereotype(x, Stereotypes.Requirement));
    }
}
