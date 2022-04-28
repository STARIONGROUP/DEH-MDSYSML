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

import static Utils.Stereotypes.StereotypeUtils.GetShortName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.javafmi.framework.FmiContainer.Var;

import com.nomagic.magicdraw.autoid.NumberingData;
import com.nomagic.magicdraw.autoid.NumberingInfo;
import com.nomagic.magicdraw.autoid.custom.AbstractionNumbering;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.foundation.MDExtension;
import com.nomagic.magicdraw.sysml.util.MDCustomizationForSysMLProfile;
import com.nomagic.magicdraw.sysml.util.SysMLConstants;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.requirements.util.MDCustomizationForRequirements;
import com.nomagic.requirements.util.RequirementUtilities;
import com.nomagic.requirements.util.RequirementsConstants;
import com.nomagic.uml2.UML2Constants;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.MappingEngineService.MappingRule;
import Utils.Ref;
import Utils.Stereotypes.MagicDrawBlockCollection;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import Utils.Stereotypes.RequirementType;
import Utils.Stereotypes.StereotypeUtils;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Definition;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import net.bytebuddy.asm.Advice.This;
import net.bytebuddy.dynamic.scaffold.MethodRegistry.Handler.ForAbstractMethod;

/**
 * The {@linkplain BlockToElementMappingRule} is the mapping rule implementation for transforming {@linkplain MagicDrawRequirementCollection} to {@linkplain RequirementsSpecification}
 */
public class DstRequirementToHubRequirementMappingRule extends DstToHubBaseMappingRule<MagicDrawRequirementCollection, ArrayList<MappedDstRequirementRowViewModel>>
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
     */
    public DstRequirementToHubRequirementMappingRule(IHubController hubController, IMagicDrawMappingConfigurationService mappingConfiguration, IMagicDrawTransactionService transactionService)
    {
        super(hubController, mappingConfiguration);
        this.transactionService = transactionService;
    }    
    
    /**
     * Transforms an {@linkplain MagicDrawRequirementCollection} of type {@linkplain Class} to an {@linkplain ArrayList} of {@linkplain RequirementsSpecification}
     * 
     * @param input the {@linkplain MagicDrawRequirementCollection} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedDstRequirementRowViewModel}
     */
    @Override
    public ArrayList<MappedDstRequirementRowViewModel> Transform(Object input)
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
            this.Logger.catching(exception);
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
        for (MappedDstRequirementRowViewModel mappedRequirement : mappedRequirements)
        {
            try
            {
                Element parentPackage = mappedRequirement.GetDstElement().getOwner();
                
                Ref<RequirementsSpecification> refRequirementsSpecification = new Ref<>(RequirementsSpecification.class);
                   
                if(!mappedRequirement.GetShouldCreateNewTargetElementValue() && mappedRequirement.GetHubElement() != null)
                {
                    refRequirementsSpecification.Set(mappedRequirement.GetHubElement());
                }
                else
                {
                    while (!(this.CanBeARequirementSpecification(parentPackage)
                            && parentPackage != null && parentPackage instanceof Package
                            && this.TryGetOrCreateRequirementSpecification((Package)parentPackage, refRequirementsSpecification)))
                    {
                        parentPackage = parentPackage.getOwner();
                    }
                }
                
                if(!refRequirementsSpecification.HasValue())
                {
                    this.Logger.error(
                            String.format("The mapping of the current requirement %s is no possible, because no eligible parent could be found current package name %s", 
                                    mappedRequirement.GetDstElement().getName(), mappedRequirement.GetDstElement().getOwner().getHumanName()));
                    
                    continue;
                }
    
                mappedRequirement.SetHubElement(refRequirementsSpecification.Get());
                
                Ref<RequirementsGroup> refRequirementsGroup = new Ref<>(RequirementsGroup.class);
                Ref<Requirement> refRequirement = new Ref<>(Requirement.class);
                
                Collection<Element> parentElements = parentPackage.getOwnedElement();
                
                if(!TryCreateRelevantGroupsAndTheRequirement(mappedRequirement.GetDstElement(), parentElements, refRequirementsSpecification, refRequirementsGroup, refRequirement))
                {
                    this.Logger.error(String.format("Could not map requirement %s", mappedRequirement.GetDstElement().getName()));
                }
                else
                {
                    this.UpdateOrCreateDefinition(mappedRequirement.GetDstElement(), refRequirement);
                    refRequirement.Get().setShortName(this.transactionService.GetRequirementId(mappedRequirement.GetDstElement()));
                }
            }
            catch(Exception exception)
            {
                this.Logger.catching(exception);
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
                        this.Logger.error(String.format("Could not create the requirement %s, because the creation/update of the requirement group %s failed", 
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
                .filter(x -> this.AreShortNamesEquals(x, GetShortName(element)))
                .findFirst();
        
        if(optionalRequirement.isPresent())
        {
            refRequirement.Set(optionalRequirement.get().clone(true));
        }
        else
        {
            Requirement requirement = new Requirement();
            requirement.setIid(UUID.randomUUID());
            requirement.setName(element.getName());
            requirement.setShortName(this.transactionService.GetRequirementId(element));
            requirement.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            this.MapCategory(element, requirement);
            requirement.setGroup(refRequirementsGroup.Get());
            refRequirementsSpecification.Get().getRequirement().add(requirement);
            refRequirement.Set(requirement);
        }

        refRequirementsSpecification.Get().getRequirement().removeIf(x -> x.getIid().equals(refRequirement.Get().getIid()));
        refRequirementsSpecification.Get().getRequirement().add(refRequirement.Get());
        
        return refRequirement.HasValue();
    }

    /**
     * Applies category onto the created {@linkplain Requirement}
     * 
     * @param element the {@linkplain Class} element
     * @param requirement the 10-25 requirement
     */
    private void MapCategory(Class element, Requirement requirement)
    {
        for(Stereotype stereotype : MDCustomizationForSysMLProfile.getInstance(element).getAllStereotypes())
        {
            this.Logger.debug("MAP CATEGORY %s, %s, %s", stereotype.getName(), stereotype.getHumanName(), stereotype.getHumanType());
            RequirementType requirementType = RequirementType.From(stereotype);
            
            if(requirementType != null)
            {
                this.MapCategory(requirement, requirementType.name(), ClassKind.Requirement);
            }
        }
    }

    /**
     * Tries to extract the value from the provided property and returns it as string
     * 
     * @return a value indicating whether the value has been extracted
     */
    private boolean TryGetValueFromProperty(Property property, Ref<String> refValue)
    {
        ValueSpecification valueSpecification = property.getDefaultValue();
        
        if(valueSpecification instanceof LiteralInteger)
        {
            refValue.Set(String.valueOf(((LiteralInteger)property.getDefaultValue()).getValue()));
        }
        else if(valueSpecification instanceof LiteralUnlimitedNatural)
        {
            refValue.Set(String.valueOf(((LiteralUnlimitedNatural)property.getDefaultValue()).getValue()));
        }
        else if(valueSpecification instanceof LiteralReal)
        {
            refValue.Set(String.valueOf(((LiteralReal)property.getDefaultValue()).getValue()));
        }
        else if(valueSpecification instanceof LiteralString)
        {
            refValue.Set(((LiteralString)property.getDefaultValue()).getValue());
        }
        else if(valueSpecification instanceof LiteralBoolean)
        {
            refValue.Set(String.valueOf(((LiteralBoolean)property.getDefaultValue()).isValue()));
        }
        
        return refValue.HasValue();
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

            definition.setContent(RequirementUtilities.getRequirementText(element));
            
            refRequirement.Get().getDefinition().removeIf(x -> x.getIid().equals(definition.getIid()));
            
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
        return !element.getOwnedElement().stream().anyMatch(x -> StereotypeUtils.DoesItHaveTheStereotype(x, Stereotypes.Requirement));
    }
}
