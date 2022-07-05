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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Definition;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsContainer;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain BlockToElementMappingRule} is the mapping rule implementation for transforming {@linkplain MagicDrawRequirementCollection} to {@linkplain RequirementsSpecification}
 */
public class DstRequirementToHubRequirementMappingRule extends DstToHubBaseMappingRule<MagicDrawRequirementCollection, ArrayList<MappedRequirementRowViewModel>>
{
    /**
     * A collection of <see cref="RequirementsSpecification" />
     */
    private final ArrayList<RequirementsSpecification> requirementsSpecifications = new ArrayList<>();

    /**
     *A collection of <see cref="RequirementsGroup" />
     */
    private final ArrayList<RequirementsGroup> requirementsGroups = new ArrayList<>();

    /**
     * The {@linkplain IMagicDrawTransactionService}
     */
    private final IMagicDrawTransactionService transactionService;

    /**
     * The {@linkplain Collection} of {@linkplain MappedRequirementRowViewModel}
     */
    private ArrayList<MappedRequirementRowViewModel> mappedElements;

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
            MagicDrawRequirementCollection mappedElementsAndMappingType = this.CastInput(input);

            for (RequirementsSpecification requirementsSpecification : this.hubController.GetOpenIteration().getRequirementsSpecification()
                    .stream().filter(x -> !x.isDeprecated()).collect(Collectors.toList()))
            {
                RequirementsSpecification requirementsSpecificationsClone = requirementsSpecification.clone(true);
                this.requirementsSpecifications.add(requirementsSpecificationsClone);
                this.PopulateRequirementsGroupCollection(requirementsSpecificationsClone);
            }

            this.mappedElements = new ArrayList<>(mappedElementsAndMappingType.getRight());

            for (MappedRequirementRowViewModel mappedElement : this.mappedElements)
            {
                this.MapRequirement(mappedElement);
            }

            if (mappedElementsAndMappingType.getLeft())
            {
                this.MapCategories();
                this.SaveMappingConfiguration(this.mappedElements, MappingDirection.FromDstToHub);
            }

            return this.mappedElements;
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
            return new ArrayList<>();
        }
        finally
        {
            this.requirementsSpecifications.clear();
            this.requirementsGroups.clear();
        }
    }

    /**
     * Maps the provided {@linkplain MappedRequirementRowViewModel}
     *
     * @param mappedElement the {@linkplain MappedRequirementRowViewModel}
     */
    private void MapRequirement(MappedRequirementRowViewModel mappedElement)
    {
        if (!mappedElement.GetShouldCreateNewTargetElementValue() && mappedElement.GetHubElement() != null)
        {
            this.UpdateProperties(mappedElement.GetDstElement(), mappedElement.GetHubElement());
            RequirementsSpecification requirementSpecification = mappedElement.GetHubElement().getContainerOfType(RequirementsSpecification.class).clone(true);
            
            requirementSpecification.getRequirement().removeIf(x -> x.getIid() == mappedElement.GetHubElement().getIid());
            requirementSpecification.getRequirement().add(mappedElement.GetHubElement());
            return;
        }

        Ref<Requirement> refRequirement = new Ref<>(Requirement.class);
        Package packageParent = mappedElement.GetDstElement().getOwningPackage();

        if (packageParent.getOwningPackage() == null)
        {
            if (!this.TryGetOrCreateRequirementsSpecificationAndRequirement(mappedElement.GetDstElement(), refRequirement))
            {
                this.logger.error(String.format("Error during creation of the RequirementsSpecification for %s requirement", mappedElement.GetDstElement().getName()));
            }
        }
        else
        {
            if (!this.TryGetOrCreateRequirement(mappedElement.GetDstElement(), refRequirement))
            {
                this.logger.error(String.format("Error during creation of the Requirement for %s package", mappedElement.GetDstElement().getName()));
                return;
            }
            
            Package packageGrandParent = packageParent.getOwningPackage();

            Ref<RequirementsSpecification> refRequirementsSpecification = new Ref<>(RequirementsSpecification.class);

            if (packageGrandParent.getOwningPackage() == null)
            {
                if (!this.TryGetOrCreateRequirementsSpecification(packageParent, refRequirementsSpecification))
                {
                    this.logger.error(String.format("Error during creation of the RequirementsSpecification for package %s", packageParent.getName()));
                    return;
                }
            }
            else
            {
                if (!this.ProcessPackageHierarchy(packageParent, refRequirement.Get(), refRequirementsSpecification))
                {
                    this.logger.error(String.format("Error during creation of the RequirementsSpecification for %s requirement during the Process hierarchy",
                            refRequirement.Get().getName()));

                    return;
                }
            }

            refRequirementsSpecification.Get().getRequirement().add(refRequirement.Get());
        }

        mappedElement.SetHubElement(refRequirement.Get());
        mappedElement.SetShouldCreateNewTargetElement(mappedElement.GetHubElement().getOriginal() == null);
    }
    
    /**
     * Tries to get a existing <see cref="Requirement" /> or created one based on the <see cref="Element" />
     * It also creates the <see cref="Requirement" /> corresponding to the <see cref="Element" />
     * 
     * @param requirementElement The <see cref="Class" />
     * @param refRequirement The <see cref="Requirement" />
     * @return A value indicating whether the <see cref="RequirementsSpecification" /> has been created or retrieved
     */
    private boolean TryGetOrCreateRequirementsSpecificationAndRequirement(Class requirementElement, Ref<Requirement> refRequirement)
    {
        Ref<RequirementsSpecification> refRequirementsSpecification = new Ref<>(RequirementsSpecification.class);
        
        if (!this.TryGetOrCreateRequirement(requirementElement, refRequirement) || !this.TryGetOrCreateRequirementsSpecification(requirementElement.getName(), refRequirementsSpecification))
        {
            return false;
        }
        
        refRequirementsSpecification.Get().getRequirement().add(refRequirement.Get());
        return true;
    }

    /**
     * Process the whole hierachy from the <see cref="Package" /> containing the Requirement to the root of 
     * the project to create <see cref="RequirementsGroup" /> and <see cref="RequirementsSpecification" />
     * 
     * @param packageParent The <see cref="Package" />
     * @param requirement The <see cref="Requirement" />
     * @param refRequirementsSpecification The <see cref="RequirementsSpecification" />
     * @return Value representing if the whole hierarchy has been processed
     */
    private boolean ProcessPackageHierarchy(Package packageParent, Requirement requirement, Ref<RequirementsSpecification> refRequirementsSpecification)
   {
       Package packageGrandParent = packageParent.getOwningPackage();

       Ref<RequirementsGroup> refParentRequirementsGroup = new Ref<>(RequirementsGroup.class);

       if (!this.TryGetOrCreateRequirementsGroup(packageParent, refParentRequirementsGroup))
       {
           this.logger.error(String.format("Error during creation of the RequirementsGroup for %s package", packageParent.getName()));
           return false;
       }

       requirement.setGroup(refParentRequirementsGroup.Get());

       while (packageGrandParent.getOwningPackage() != null)
       {
           packageParent = packageGrandParent;
           packageGrandParent = packageParent.getOwningPackage();

           if (packageGrandParent.getOwningPackage() == null)
           {
               break;
           }

           Ref<RequirementsGroup> refNewestRequirementsGroup = new Ref<>(RequirementsGroup.class);

           if (!this.TryGetOrCreateRequirementsGroup(packageParent, refNewestRequirementsGroup))
           {
               this.logger.error(String.format("Error during creation of the RequirementsGroup for %s package", packageParent.getName()));
               return false;
           }

           refNewestRequirementsGroup.Get().getGroup().removeIf(x -> AreTheseEquals(x.getIid(), refParentRequirementsGroup.Get().getIid()));
           refNewestRequirementsGroup.Get().getGroup().add(refParentRequirementsGroup.Get());
           refParentRequirementsGroup.Set(refNewestRequirementsGroup.Get());
       }

       if (!this.TryGetOrCreateRequirementsSpecification(packageParent, refRequirementsSpecification))
       {
           this.logger.error(String.format("Error during creation of the RequirementsSpecification for %s package", packageGrandParent.getName()));
           return false;
       }

       refRequirementsSpecification.Get().getGroup().removeIf(x -> AreTheseEquals(x.getIid(), refParentRequirementsGroup.Get().getIid()));
       refRequirementsSpecification.Get().getGroup().add(refParentRequirementsGroup.Get());

       return true;
   }
   
   /**
    * Tries to get a existing <see cref="RequirementsGroup" /> or created one based on the <see cref="Package" />
    *
    * @param package The <see cref="Package" />
    * @param refRequirementsGroup The <see cref="RequirementsGroup" />
    * @return A value indicating whether the <see cref="RequirementsGroup" /> has been created or retrieved
    */
    private boolean TryGetOrCreateRequirementsGroup(Package requirementPackage, Ref<RequirementsGroup> refRequirementsGroup)
    {
        String shortName = GetShortName(requirementPackage.getName());
       
        Optional<RequirementsGroup> optionalRequirementsGroup = this.requirementsGroups.stream()
                .filter(x -> AreTheseEquals(x.getShortName(), shortName, true))
                .findFirst();
       
        if (optionalRequirementsGroup.isPresent())
        {
            refRequirementsGroup.Set(optionalRequirementsGroup.get());
        }
        else
        {
            RequirementsGroup requirementsGroup = new RequirementsGroup();
            requirementsGroup.setName(requirementPackage.getName());
            requirementsGroup.setShortName(shortName);
            requirementsGroup.setIid(UUID.randomUUID());
            requirementsGroup.setOwner(this.hubController.GetCurrentDomainOfExpertise());

            refRequirementsGroup.Set(requirementsGroup);
            this.requirementsGroups.add(requirementsGroup);
        }
       
        return refRequirementsGroup.HasValue();
    }

   /**
    * Tries to get a existing <see cref="RequirementsGroup" /> or created one based on the <see cref="Package" />
    *
    * @param requirementPackage The <see cref="Package" />
    * @param requirementsSpecification The <see cref="RequirementsSpecification" />
    * @return A value indicating whether the <see cref="RequirementsSpecification" /> has been created or retrieved
    */
   private boolean TryGetOrCreateRequirementsSpecification(Package requirementPackage, Ref<RequirementsSpecification> requirementsSpecification)
   {
       return this.TryGetOrCreateRequirementsSpecification(requirementPackage.getName(), requirementsSpecification);
   }

   /**
    * Tries to get a existing <see cref="RequirementsSpecification" /> or created one based on the name
    *
    * @param packageName The name
    * @param refRequirementsSpecification The <see cref="RequirementsSpecification" />
    * @return A value indicating whether the <see cref="RequirementsSpecification" /> has been created or retrieved
    */
    private boolean TryGetOrCreateRequirementsSpecification(String packageName, Ref<RequirementsSpecification> refRequirementsSpecification)
    {
        String shortName = GetShortName(packageName);

        Optional<RequirementsSpecification> optionalRequirementsSpecification = this.requirementsSpecifications.stream()
                .filter(x -> AreTheseEquals(x.getShortName(), shortName, true))
                .findFirst();

        if (!optionalRequirementsSpecification.isPresent())
        {
            RequirementsSpecification newRequirementSpecification = new RequirementsSpecification();
            newRequirementSpecification.setIid(UUID.randomUUID());
            newRequirementSpecification.setName(packageName);
            newRequirementSpecification.setShortName(shortName);
            newRequirementSpecification.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            refRequirementsSpecification.Set(newRequirementSpecification);
            this.requirementsSpecifications.add(refRequirementsSpecification.Get());
        }
        else
        {
            refRequirementsSpecification.Set(optionalRequirementsSpecification.get());
        }

        this.requirementsGroups.addAll(refRequirementsSpecification.Get().getAllContainedGroups());

        return refRequirementsSpecification.HasValue();
    }

    /**
     * Tries to get a existing <see cref="Requirement" /> or created one based on the <see cref="Element" />
     *
     * @param dstRequirement the {@linkplain Class} requirement
     * @param refRequirement the {@linkplain Ref} of {@linkplain Requirement}
     * @return a value indicating whether the {@linkplain Requirement} was either found or created
     */
    private boolean TryGetOrCreateRequirement(Class dstRequirement, Ref<Requirement> refRequirement)
    {
        if (this.TryGetRequirement(dstRequirement, refRequirement))
        {
            RequirementsSpecification requirementsSpecification = refRequirement.Get().getContainerOfType(RequirementsSpecification.class);
            requirementsSpecification.getRequirement().removeIf(x -> AreTheseEquals(x.getIid(), refRequirement.Get().getIid()));
        }
        else
        {
            Requirement requirement = new Requirement();
            requirement.setIid(UUID.randomUUID());
            requirement.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            refRequirement.Set(requirement);
        }

        this.UpdateProperties(dstRequirement, refRequirement.Get());
        return refRequirement.HasValue();
    }

    /**
     * Try to get an existing {@linkplain Requirement}
     *
     * @param dstRequirement the {@linkplain Class} requirement
     * @param refRequirement the {@linkplain Ref} of {@linkplain Requirement}
     * @return a value indicating whether the {@linkplain Requirement} was either found
     */
    private boolean TryGetRequirement(Class dstRequirement, Ref<Requirement> refRequirement)
    {
        String requirementShortname = this.transactionService.GetRequirementId(dstRequirement);

        refRequirement.Set(this.requirementsSpecifications.stream()
                .flatMap(x -> x.getRequirement().stream())
                .filter(x -> !x.isDeprecated() && AreTheseEquals(x.getShortName(), requirementShortname, true))
                .findFirst()
                .orElse(null));

        return refRequirement.HasValue();
    }

    /**
     * Updates the target {@linkplain cdp4common.engineeringmodeldata.Requirement} properties
     *
     * @param dstRequirement the source {@linkplain Requirement}
     * @param requirement the {@linkplain cdp4common.engineeringmodeldata.Requirement} target
     */
    private void UpdateProperties(Class dstRequirement, Requirement requirement)
    {
        this.UpdateOrCreateDefinition(dstRequirement, requirement);
        requirement.setName(dstRequirement.getName());
        requirement.setShortName(this.transactionService.GetRequirementId(dstRequirement));
    }

    /**
     * Applies categories for each {@linkplain #mappedElements}
     */
    private void MapCategories()
    {
        for (MappedRequirementRowViewModel mappedRequirementRowViewModel : this.mappedElements)
        {
            for(Stereotype stereotype : this.stereotypeService.GetAllStereotype(mappedRequirementRowViewModel.GetDstElement()))
            {
                this.MapCategory(mappedRequirementRowViewModel.GetHubElement(), stereotype.getName(), ClassKind.Requirement);
            }
        }
    }

    /**
     * Updates or creates the definition according to the provided {@linkplain Class} assignable to the {@linkplain Requirement}
     *
     * @param element the {@linkplain Class} element that represents the requirement in MagicDraw
     * @param requirement the {@linkplain Requirement} to update
     */
    private void UpdateOrCreateDefinition(Class element, Requirement requirement)
    {
            Definition definition = requirement.getDefinition()
                    .stream()
                    .filter(x -> x.getLanguageCode().equalsIgnoreCase("en"))
                    .findFirst()
                    .map(x -> x.clone(true))
                    .orElse(this.CreateDefinition());

            String requirementText  = this.stereotypeService.GetRequirementText(element);
            definition.setContent(StringUtils.isBlank(requirementText) ? "-" : requirementText);

            requirement.getDefinition().removeIf(x -> AreTheseEquals(x.getIid(), definition.getIid()));
            requirement.getDefinition().add(definition);
    }

    /**
     * Creates a {@linkplain Definition} to be added to a {@linkplain Requirement}
     *
     * @return a {@linkplain Definition}
     */
    private Definition CreateDefinition()
    {
        Definition definition = new Definition();
        definition.setIid(UUID.randomUUID());
        definition.setLanguageCode("en");
        return definition;
    }

    /**
     * Populate the <see cref="requirementsGroups"/> collection
     *
     *@param container The <see cref="RequirementsContainer"/>
     */
    private void PopulateRequirementsGroupCollection(RequirementsContainer container)
    {
        this.requirementsGroups.addAll(container.getGroup());

        for (RequirementsGroup requirementsGroup : container.getGroup())
        {
            this.PopulateRequirementsGroupCollection(requirementsGroup);
        }
    }

}
