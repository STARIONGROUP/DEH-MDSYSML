/*
 * MagicDrawMappingConfigurationService.java
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
package Services.MappingConfiguration;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Utils.Ref;
import Utils.Stereotypes.StereotypeUtils;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedHubRequirementRowViewModel;
import ViewModels.Rows.MappedRequirementBaseRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import cdp4common.commondata.NamedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ExternalIdentifierMap;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain MagicDrawMappingConfigurationService} is the implementation of {@linkplain MappingConfigurationService} for the MagicDraw adapter
 */
public class MagicDrawMappingConfigurationService extends MappingConfigurationService<Class, ExternalIdentifier> implements IMagicDrawMappingConfigurationService
{
    /**
     * The {@linkplain IMagicDrawTransactionService}
     */
    private final IMagicDrawTransactionService transactionService;
    
    /**
     * Initializes a new {@linkplain MagicDrawMappingConfigurationService}
     * 
     * @param HubController the {@linkplain IHubController}
     * @param transactionService the {@linkplain ICapellaTransactionService}
     */
    public MagicDrawMappingConfigurationService(IHubController hubController, IMagicDrawTransactionService transactionService)
    {
        super(hubController, ExternalIdentifier.class);
        this.transactionService = transactionService;

        this.HubController.GetIsSessionOpenObservable()
        .subscribe(x -> 
        {
            if(!x)
            {
                this.Correspondences.clear();
                this.SetExternalIdentifierMap(new ExternalIdentifierMap());
            }
        });
    }

    /**
     * Loads the mapping configuration and generates the map result respectively
     * 
     * @param elements a {@linkplain Collection} of {@code TDstElement}
     * @return a {@linkplain Collection} of {@linkplain IMappedElementRowViewModel}
     */
    @Override
    public Collection<IMappedElementRowViewModel> LoadMapping(Collection<Class> elements)
    {
        List<IMappedElementRowViewModel> mappedElements = new ArrayList<>();

        for (Class element : elements)
        {
            Ref<IMappedElementRowViewModel> refMappedElementRowViewModel = new Ref<>(IMappedElementRowViewModel.class);
            
            if(this.TryGetMappedElement(element, refMappedElementRowViewModel))
            {
                mappedElements.add(refMappedElementRowViewModel.Get());
            }
        }
                
        return mappedElements;
    }

    /**
     * Tries to get the {@linkplain IMappedElementRowViewModel} depending if the provided {@linkplain Class} 
     * has a mapping defined in the currently loaded externalIdentifier map and if the corresponding {@linkplain Thing} is present in the cache
     * 
     * @param element the {@linkplain Class} element
     * @return a {@linkplain Ref} of {@linkplain IMappedElementRowViewModel}
     */
    private boolean TryGetMappedElement(Class element, Ref<IMappedElementRowViewModel> refMappedElementRowViewModel)
    {
        Optional<ImmutableTriple<UUID, ExternalIdentifier, UUID>> optionalCorrespondence = this.Correspondences.stream()
                .filter(x -> AreTheseEquals(x.middle.Identifier, element.getID()))
                .findFirst();
        
        if(!optionalCorrespondence.isPresent())
        {
            return false;
        }
        
        MappingDirection mappingDirection = optionalCorrespondence.get().middle.MappingDirection;
        UUID internalId = optionalCorrespondence.get().right;
        
        if(StereotypeUtils.DoesItHaveTheStereotype(element, Stereotypes.Block))
        {
            Ref<ElementDefinition> refElementDefinition = new Ref<>(ElementDefinition.class);
            
            MappedElementDefinitionRowViewModel mappedElement = new MappedElementDefinitionRowViewModel(this.transactionService.Clone(element), mappingDirection);
            
            if(this.HubController.TryGetThingById(internalId, refElementDefinition))
            {
                mappedElement.SetHubElement(refElementDefinition.Get().clone(false));
            }
                        
            refMappedElementRowViewModel.Set(mappedElement);
        }
        else if(StereotypeUtils.DoesItHaveTheStereotype(element, Stereotypes.Requirement))
        {      
            if(mappingDirection == MappingDirection.FromHubToDst)
            {
                MappedHubRequirementRowViewModel mappedElement = new MappedHubRequirementRowViewModel(this.transactionService.Clone(element), mappingDirection);
                this.GetMappedRequirement(mappedElement, internalId, cdp4common.engineeringmodeldata.Requirement.class);
                
                refMappedElementRowViewModel.Set(mappedElement);
            }
            else
            {
                MappedDstRequirementRowViewModel mappedElement = new MappedDstRequirementRowViewModel(element, mappingDirection);
                this.GetMappedRequirement(mappedElement, internalId, RequirementsSpecification.class);
                refMappedElementRowViewModel.Set(mappedElement);
            }            
        }
        
        return refMappedElementRowViewModel.HasValue();
    }

    /**
     * Gets the mapped {@linkplain #TThing}
     * 
     * @param <TThing> the type of {@linkplain Thing} to query from the cache
     * @param mappedElement the {@linkplain MappedRequirementBaseRowViewModel}
     * @param internalId the internal id of the queried Thing
     * @param clazz the {@linkplain Class} of the queried Thing
     */
    @SuppressWarnings("unchecked")
    private <TThing extends Thing & NamedThing> void GetMappedRequirement(MappedRequirementBaseRowViewModel<TThing> mappedElement, UUID internalId, java.lang.Class<TThing> clazz)
    {
        Ref<TThing> refHubRequirement = new Ref<>(clazz);
        
        if(this.HubController.TryGetThingById(internalId, refHubRequirement))
        {
            mappedElement.SetHubElement((TThing) refHubRequirement.Get().clone(true));
        }
    }
    /**
     * Creates a new {@linkplain ExternalIdentifierMap} and sets the current as the new one
     * 
     * @param newName the {@linkplain String} name of the new configuration
     * @param addTheTemporyMapping a value indicating whether the current temporary {@linkplain ExternalIdentifierMap} 
     * contained correspondence should be transfered the new one
     * 
     * @return the new configuration {@linkplain ExternalIdentifierMap}
     */
    @Override
    public ExternalIdentifierMap CreateExternalIdentifierMap(String newName, String modelName, boolean addTheTemporyMapping)
    {
        return super.CreateExternalIdentifierMap(newName, modelName, DstController.DstController.THISTOOLNAME, addTheTemporyMapping);
    }
}
