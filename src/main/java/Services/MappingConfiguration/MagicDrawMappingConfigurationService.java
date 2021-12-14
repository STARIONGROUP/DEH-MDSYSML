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

import HubController.IHubController;
import Utils.Ref;
import Utils.Stereotypes.StereotypeUtils;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedRequirementsSpecificationRowViewModel;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ExternalIdentifierMap;
import cdp4common.engineeringmodeldata.RequirementsSpecification;

/**
 * The {@linkplain MagicDrawMappingConfigurationService} is the implementation of {@linkplain MappingConfigurationService} for the MagicDraw adapter
 */
public class MagicDrawMappingConfigurationService extends MappingConfigurationService<Class> implements IMagicDrawMappingConfigurationService
{
    /**
     * Initializes a new {@linkplain MagicDrawMappingConfigurationService}
     * 
     * @param HubController the {@linkplain IHubController}
     */
    public MagicDrawMappingConfigurationService(IHubController hubController)
    {
        super(hubController);
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
        
        this.Logger.error(String.format("mappedElements contains %s not cloned thing", 
                mappedElements.stream().filter(x -> ((MappedElementDefinitionRowViewModel)x).GetHubElement().getOriginal() == null).count()));
        
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
        
        if(StereotypeUtils.DoesItHaveTheStereotype(element, Stereotypes.Block))
        {
            Ref<ElementDefinition> refElementDefinition = new Ref<>(ElementDefinition.class);
                        
            MappedElementDefinitionRowViewModel mappedElement = new MappedElementDefinitionRowViewModel(element, optionalCorrespondence.get().middle.MappingDirection);
            
            if(this.HubController.TryGetThingById(optionalCorrespondence.get().right, refElementDefinition))
            {
                mappedElement.SetHubElement(refElementDefinition.Get().clone(true));
                
                if(!mappedElement.GetHubElement().getContainedElement().isEmpty())
                {
                    this.Logger.debug(String.format("Current element definition contains %s non cloned out of %s usages", 
                            mappedElement.GetHubElement().getContainedElement().stream().filter(x -> x.getOriginal() == null).count(),
                            mappedElement.GetHubElement().getContainedElement().size()));
                }
            }
            
            refMappedElementRowViewModel.Set(mappedElement);
        }
        else if(StereotypeUtils.DoesItHaveTheStereotype(element, Stereotypes.Requirement))
        {      
            Ref<RequirementsSpecification> refRequirementsSpecification = new Ref<>(RequirementsSpecification.class);
            
            MappedRequirementsSpecificationRowViewModel mappedElement = new MappedRequirementsSpecificationRowViewModel(element, optionalCorrespondence.get().middle.MappingDirection);
            
            if(this.HubController.TryGetThingById(optionalCorrespondence.get().right, refRequirementsSpecification))
            {
                mappedElement.SetHubElement(refRequirementsSpecification.Get().clone(true));
            }
            
            refMappedElementRowViewModel.Set(mappedElement);
        }
        
        return refMappedElementRowViewModel.HasValue();
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
