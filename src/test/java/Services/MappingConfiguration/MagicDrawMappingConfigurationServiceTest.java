/*
* MagicDrawMappingConfigurationServiceTest.java
*
* Copyright (c) 2020-2022 RHEA System S.A.
*
* Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski, Antoine Théate
*
* This file is part of DEH-CommonJ
*
* The DEH-CommonJ is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 3 of the License, or (at your option) any later version.
*
* The DEH-CommonJ is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program; if not, write to the Free Software Foundation,
* Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/
package Services.MappingConfiguration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.Stereotypes;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Requirement;
import io.reactivex.Observable;

class MagicDrawMappingConfigurationServiceTest
{
    private IHubController hubController;
    private IMagicDrawTransactionService transactionService;
    private IStereotypeService stereotypeService;
    private MagicDrawMappingConfigurationService service;
    private Class block0;
    private ElementDefinition elementDefinition;
    private Requirement requirement;
    private Class dstRequirement;

    @BeforeEach
    void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.stereotypeService = mock(IStereotypeService.class);
        this.transactionService = mock(IMagicDrawTransactionService.class);
        
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(Observable.fromArray(true, false));
        
        this.service = new MagicDrawMappingConfigurationService(this.hubController, this.transactionService, this.stereotypeService);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void VerifyLoadMapping()
    {
        assertDoesNotThrow(() -> this.service.LoadMapping(Collections.emptyList()));
        
        this.block0 = mock(Class.class);
        when(this.block0.getID()).thenReturn(UUID.randomUUID().toString());
        
        this.dstRequirement = mock(Class.class);
        when(this.dstRequirement.getID()).thenReturn(UUID.randomUUID().toString());
        
        this.elementDefinition = new ElementDefinition();
        this.elementDefinition.setIid(UUID.randomUUID());
        
        this.requirement = new Requirement();
        this.requirement.setIid(UUID.randomUUID());
        
        ExternalIdentifier externalIdentifier0 = new ExternalIdentifier();
        externalIdentifier0.Identifier = this.block0.getID();
        externalIdentifier0.MappingDirection = MappingDirection.FromDstToHub;
        ExternalIdentifier externalIdentifier1 = new ExternalIdentifier();
        externalIdentifier1.Identifier = this.block0.getID();
        externalIdentifier1.MappingDirection = MappingDirection.FromDstToHub;
        ExternalIdentifier externalIdentifier2 = new ExternalIdentifier();
        externalIdentifier0.Identifier = this.dstRequirement.getID();
        externalIdentifier0.MappingDirection = MappingDirection.FromDstToHub;
        ExternalIdentifier externalIdentifier3 = new ExternalIdentifier();
        externalIdentifier1.Identifier = this.dstRequirement.getID();
        externalIdentifier1.MappingDirection = MappingDirection.FromDstToHub;

        this.service.correspondences.add(ImmutableTriple.of(UUID.randomUUID(), externalIdentifier0, this.elementDefinition.getIid()));
        this.service.correspondences.add(ImmutableTriple.of(UUID.randomUUID(), externalIdentifier1, this.elementDefinition.getIid()));
        this.service.correspondences.add(ImmutableTriple.of(UUID.randomUUID(), externalIdentifier2, this.requirement.getIid()));
        this.service.correspondences.add(ImmutableTriple.of(UUID.randomUUID(), externalIdentifier3, this.requirement.getIid()));
        
        assertEquals(0, this.service.LoadMapping(Arrays.asList(block0, dstRequirement)).size());
        when(this.stereotypeService.DoesItHaveTheStereotype(any(), same(Stereotypes.Block))).thenReturn(true);
        
        when(this.hubController.TryGetThingById(any(UUID.class), any(Ref.class))).thenAnswer(x ->
        {
            Ref<?> ref = x.getArgument(1, Ref.class);
            
            if(ref.GetType() == Requirement.class)
            {
                ((Ref<Requirement>)ref).Set(this.requirement);
            }
            else
            {
                ((Ref<ElementDefinition>)ref).Set(this.elementDefinition);
            }
            
            return true;
        });
        
        assertNotEquals(0, this.service.LoadMapping(Arrays.asList(block0, dstRequirement)).size());
        
        when(this.stereotypeService.DoesItHaveTheStereotype(any(), same(Stereotypes.Block))).thenReturn(false);
        when(this.stereotypeService.DoesItHaveTheStereotype(any(), same(Stereotypes.Requirement))).thenReturn(true);

        assertNotEquals(0, this.service.LoadMapping(Arrays.asList(dstRequirement)).size());
    }

    @Test
    void VerifyCreateExternalIdentifierMapStringStringBoolean()
    {
        assertNotNull(this.service.CreateExternalIdentifierMap("name", "model", false));
    }
}
