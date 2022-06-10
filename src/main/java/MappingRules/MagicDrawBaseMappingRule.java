/*
 * MagicDrawBaseMappingRule.java
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

import java.util.Collection;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.MappingEngineService.MappingRule;
import Services.Stereotype.IStereotypeService;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;

/**
 * The CapellaBaseMappingRule is the base mapping rule for all the Capella adapter rules
 *  
 * @param <TInput> the input type the rule will process
 * @param <TOutput> the output type the rule will return
 */
public abstract class MagicDrawBaseMappingRule<TInput extends Object, TOutput> extends MappingRule<TInput, TOutput>
{
    /**
     * The {@linkplain IHubController}
     */
    protected final IHubController hubController;

    /**
     * The {@linkplain IStereotypeService}
     */
    protected final IStereotypeService stereotypeService;

    /**
     * The {@linkplain IMagicDrawMappingConfigurationService} instance
     */
    protected final IMagicDrawMappingConfigurationService mappingConfiguration;
    
    /**
     * Initializes a new {@linkplain DstToHubBaseMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain IMagicDrawMappingConfigurationService}
     * @param stereotypeService the {@linkplain IStereotypeService}
     */
    protected MagicDrawBaseMappingRule(IHubController hubController, IMagicDrawMappingConfigurationService mappingConfiguration, IStereotypeService stereotypeService)
    {
        this.hubController = hubController;
        this.mappingConfiguration = mappingConfiguration;
        this.stereotypeService = stereotypeService;
    }
    
    /**
     * Saves the mapping configuration
     * 
     * @param elements the {@linkplain Collection} of {@linkplain MappedElementRowViewModel} from which the DST element extends {@linkplain Class}
     * @param mappingDirection the {@linkplain MappingDirection} that applies to the provided mapped element 
     */
    protected void SaveMappingConfiguration(Collection<? extends MappedElementRowViewModel<? extends Thing, ? extends Class>> elements, MappingDirection mappingDirection)
    {
        for (MappedElementRowViewModel<? extends Thing, ? extends Class> element : elements)
        {
            this.mappingConfiguration.AddToExternalIdentifierMap(
                    element.GetHubElement().getIid(), element.GetDstElement().getID(), mappingDirection);
        }
    }
}
