/*
 * DstControllerTestFixture.java
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
package DstController;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.cache.Cache;

import Enumerations.MappingDirection;
import HubController.HubController;
import HubController.IHubController;
import Services.HistoryService.IMagicDrawLocalExchangeHistoryService;
import Services.LocalExchangeHistory.ILocalExchangeHistoryService;
import Services.MagicDrawSession.IMagicDrawProjectEventListener;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.MappingEngineService.IMappingEngineService;
import Utils.Ref;
import Utils.Stereotypes.MagicDrawBlockCollection;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Definition;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.EngineeringModel;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterSwitchKind;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import cdp4common.sitedirectorydata.ParameterType;
import cdp4common.sitedirectorydata.TextParameterType;
import cdp4common.types.CacheKey;
import cdp4common.types.ValueArray;
import cdp4dal.Assembler;
import cdp4dal.Session;
import cdp4dal.operations.ThingTransaction;
import io.reactivex.Observable;

class DstControllerTestFixture
{
    private IHubController hubController;
    private IMappingEngineService mappingEngine;
    private DstController controller;
    private DomainOfExpertise domain;
    private Iteration iteration;
    private ElementDefinition elementDefinition1;
    private ElementDefinition elementDefinition0;
    private RequirementsSpecification requirementsSpecification0;
    private RequirementsSpecification requirementsSpecification1;
    private Cache<CacheKey, Thing> cache;
    private Session session;
    private URI uri;
    private Assembler assembler;
    private Parameter parameter0;
    private Parameter parameter1;
    private IMagicDrawMappingConfigurationService mappingConfigurationService;
    private IMagicDrawUILogService logService;
    private IMagicDrawProjectEventListener projectEventListener;
    private IMagicDrawLocalExchangeHistoryService historyService;
    private IMagicDrawSessionService sessionService;
    private IMagicDrawTransactionService transactionService;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception
    {
        this.mappingEngine = mock(IMappingEngineService.class);
        this.hubController = mock(HubController.class);
        this.mappingConfigurationService = mock(IMagicDrawMappingConfigurationService.class);
        this.logService = mock(IMagicDrawUILogService.class);
        this.projectEventListener = mock(IMagicDrawProjectEventListener.class);
        this.historyService = mock(IMagicDrawLocalExchangeHistoryService.class);
        this.sessionService = mock(IMagicDrawSessionService.class);
        this.transactionService = mock(IMagicDrawTransactionService.class);

        when(this.sessionService.SessionUpdated()).thenReturn(Observable.fromArray(true, false));
        
        this.uri = URI.create("http://t.est");
        this.cache = com.google.common.cache.CacheBuilder.newBuilder().build();
        this.assembler = mock(Assembler.class);
        when(this.assembler.getDalUri()).thenReturn(uri);
        when(this.assembler.getCache()).thenReturn(this.cache);
        
        this.session = mock(Session.class);
        this.SetSession();

        when(this.session.getAssembler()).thenReturn(this.assembler);
        
        this.iteration = new Iteration(UUID.randomUUID(), this.cache, this.uri);
        this.iteration.setRevisionNumber(12);
        
        EngineeringModel model = new EngineeringModel(UUID.randomUUID(), this.cache, this.uri);
        model.getIteration().add(this.iteration);
        
        this.cache.put(new CacheKey(this.iteration.getIid(), this.iteration.getIid()), this.iteration);

        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(Observable.fromArray(true, false));
        when(this.hubController.GetSessionEventObservable()).thenReturn(Observable.fromArray(true, false));
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        when(this.hubController.GetIterationTransaction()).thenReturn(Pair.of(this.iteration.clone(false), mock(ThingTransaction.class)));
        when(this.hubController.Refresh()).thenReturn(true);

        this.controller = new DstController(this.mappingEngine, this.hubController, this.logService, 
                this.mappingConfigurationService, this.sessionService, this.historyService, this.transactionService);
    }

    private void SetSession() throws Exception
    {
        try
        {
            Field sessionField = HubController.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(this.hubController, this.session);     
        } 
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException exception)
        {
            exception.printStackTrace();
            throw exception;
        }
    }
    
    @Test
    void VerifyMap()
    {
        assertDoesNotThrow(() -> this.controller.Map(new MagicDrawBlockCollection(), MappingDirection.FromDstToHub));
        assertDoesNotThrow(() -> this.controller.Map(new MagicDrawRequirementCollection(), MappingDirection.FromDstToHub));
        assertEquals(0, this.controller.GetDstMapResult().size());

        when(this.mappingEngine.Map(any(MagicDrawBlockCollection.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(new ElementDefinition(), new ElementDefinition())));

        when(this.mappingEngine.Map(any(MagicDrawRequirementCollection.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(new RequirementsSpecification(), new RequirementsSpecification())));

        assertDoesNotThrow(() -> this.controller.Map(new MagicDrawBlockCollection(), MappingDirection.FromDstToHub));
        assertDoesNotThrow(() -> this.controller.Map(new MagicDrawRequirementCollection(), MappingDirection.FromDstToHub));
        assertEquals(2, this.controller.GetDstMapResult().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void VerifyTransfer() throws ExecutionException
    {
        assertDoesNotThrow(() -> this.controller.TransferToHub());

        this.domain = new DomainOfExpertise();
        this.domain.setName("THERMAL");
        this.domain.setShortName("THE");

        this.SetupElements();
        
        this.SetupRequirements();
        
        when(this.mappingEngine.Map(any(MagicDrawBlockCollection.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(elementDefinition0, elementDefinition1)));

        when(this.mappingEngine.Map(any(MagicDrawRequirementCollection.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(this.requirementsSpecification0, this.requirementsSpecification1)));

//        assertDoesNotThrow(() -> this.controller.Map(new MagicDrawBlockCollection(), MappingDirection.FromDstToHub));
//        assertDoesNotThrow(() -> this.controller.Map(new MagicDrawRequirementCollection(), MappingDirection.FromDstToHub));
//        assertEquals(4, this.controller.GetDstMapResult().size());
//        
//        when(this.hubController.TryGetThingById(eq(this.parameter0.getIid()), any(Ref.class)))
//            .thenAnswer(invocation -> 
//                {
//                    Object[] args = invocation.getArguments();
//                    ((Ref<Parameter>)args[1]).Set(this.parameter0);
//                    return true;
//                });
//        
//        when(this.hubController.TryGetThingById(eq(this.parameter1.getIid()), any(Ref.class)))
//            .thenAnswer(invocation -> 
//                {
//                        Object[] args = invocation.getArguments();
//                        ((Ref<Parameter>)args[1]).Set(this.parameter1);
//                        return true;
//                });
//        
//        assertTrue(this.controller.TransferToHub());
//        assertEquals(0, this.controller.GetDstMapResult().size());
    }

    @Test
    public void VerifyChangeMappingDirection()
    {
        Ref<Integer> numberOfTimeTheDirectionChanged = new Ref<Integer>(Integer.class, 0);
        Ref<MappingDirection> mappingDirection = new Ref<MappingDirection>(MappingDirection.class, MappingDirection.FromDstToHub);
        
        this.controller.GetMappingDirection().subscribe(x -> 
        {
            if(mappingDirection.Get() != x)
            {
                mappingDirection.Set(x);
                numberOfTimeTheDirectionChanged.Set(numberOfTimeTheDirectionChanged.Get() + 1);
            }
        });
        
        this.controller.ChangeMappingDirection();
        this.controller.ChangeMappingDirection();
        this.controller.ChangeMappingDirection();
        this.controller.ChangeMappingDirection();
        
        assertEquals(4, numberOfTimeTheDirectionChanged.Get());
    }
    
    @Test
    void testGetHubMapResult()
    {
        assertTrue(true);
    }

    @Test
    void testGetDstMapResult()
    {
        assertTrue(true);
    }

    @Test
    void testGetSelectedHubMapResultForTransfer()
    {
        assertTrue(true);
    }

    @Test
    void testGetSelectedDstMapResultForTransfer()
    {
        assertTrue(true);
    }

    @Test
    void testGetMappingDirection()
    {
        assertTrue(true);
    }

    @Test
    void testCurrentMappingDirection()
    {
        assertTrue(true);
    }

    @Test
    void testChangeMappingDirection()
    {
        assertTrue(true);
    }

    @Test
    void testLoadMapping()
    {
        assertTrue(true);
    }

    @Test
    void testMap()
    {
        assertTrue(true);
    }

    @Test
    void testTransfer()
    {
        assertTrue(true);
    }

    @Test
    void testTransferToDst()
    {
        assertTrue(true);
    }

    @Test
    void testTransferToHub()
    {
        assertTrue(true);
    }

    @Test
    void testUpdateParameterValueSets()
    {
        assertTrue(true);
    }

    @Test
    void testAddOrRemoveAllFromSelectedThingsToTransfer()
    {
        assertTrue(true);
    }

    @Test
    void testTryGetElementByName()
    {
        assertTrue(true);
    }

    @Test
    void testTryGetElementById()
    {
        assertTrue(true);
    }

    @Test
    void testTryGetElementBy()
    {
        assertTrue(true);
    }

    @Test
    void testTryGetUnit()
    {
        assertTrue(true);
    }

    @Test
    void testTryGetDataType()
    {
        assertTrue(true);
    }
    
    private void SetupRequirements()
    {
        RequirementsGroup group0 = new RequirementsGroup();
        group0.setName("group0");
        group0.setOwner(this.domain);
        group0.setIid(UUID.randomUUID());
        
        RequirementsGroup group1 = new RequirementsGroup();
        group1.setName("group1");
        group1.getGroup().add(group1);
        group1.setOwner(this.domain);
        group1.setIid(UUID.randomUUID());
        
        RequirementsGroup group2 = new RequirementsGroup();
        group2.setName("group1");
        group2.setOwner(this.domain);
        group2.setIid(UUID.randomUUID());
        
        Requirement requirement0 = new Requirement();
        requirement0.setName("requirement0");
        requirement0.setShortName("req0");
        requirement0.setGroup(group1);
        requirement0.setOwner(this.domain);
        requirement0.setIid(UUID.randomUUID());

        Requirement requirement1 = new Requirement();
        requirement1.setName("requirement1");
        requirement1.setShortName("req1");
        requirement1.setGroup(group0);
        requirement1.setOwner(this.domain);  
        requirement1.setIid(UUID.randomUUID());
        
        Requirement requirement2 = new Requirement();
        requirement2.setName("requirement2");
        requirement2.setShortName("req2");
        requirement1.setGroup(group2);
        requirement2.setOwner(this.domain);
        requirement2.setIid(UUID.randomUUID());
        
        Definition definition = new Definition();
        definition.setLanguageCode("en");
        definition.setContent("content");
        definition.setIid(UUID.randomUUID());
        
        requirement0.getDefinition().add(definition);
        requirement1.getDefinition().add(definition.clone(false));
        requirement2.getDefinition().add(definition.clone(false));

        this.requirementsSpecification0 = new RequirementsSpecification();
        this.requirementsSpecification0.getGroup().add(group0);
        this.requirementsSpecification0.getRequirement().add(requirement0);
        this.requirementsSpecification0.getRequirement().add(requirement1);
        this.requirementsSpecification0.setOwner(this.domain);
        this.requirementsSpecification0.setIid(UUID.randomUUID());
        
        this.requirementsSpecification1 = new RequirementsSpecification();
        this.requirementsSpecification1.getRequirement().add(requirement2);
        this.requirementsSpecification1.getGroup().add(group2);
        this.requirementsSpecification1.setOwner(this.domain);
        this.requirementsSpecification1.setIid(UUID.randomUUID());
    }
    
    private void SetupElements()
    {
        ParameterValueSet valueSet = new ParameterValueSet();
        valueSet.setIid(UUID.randomUUID());
        valueSet.setReference(new ValueArray<>(String.class));
        valueSet.setFormula(new ValueArray<>(String.class));
        valueSet.setPublished(new ValueArray<>(String.class));
        valueSet.setComputed(new ValueArray<>(String.class));
        valueSet.setManual(new ValueArray<>(String.class));
        valueSet.setValueSwitch(ParameterSwitchKind.MANUAL);
        valueSet.setIid(UUID.randomUUID());
        
        ParameterType parameterType = new TextParameterType();
        parameterType.setIid(UUID.randomUUID());
        
        this.parameter0 = new Parameter();
        this.parameter0.setOwner(this.domain);
        this.parameter0.setParameterType(parameterType);
        this.parameter0.getValueSet().add(valueSet);
        this.parameter0.setIid(UUID.randomUUID());

        this.parameter1 = new Parameter();
        this.parameter1.setOwner(this.domain);
        this.parameter1.setParameterType(parameterType);
        this.parameter1.getValueSet().add(valueSet.clone(true));
        this.parameter1.setIid(UUID.randomUUID());
        
        Category elementCategory = new Category();
        elementCategory.setName("elementCategory");
        elementCategory.setShortName("ec");
        elementCategory.getPermissibleClass().add(ClassKind.ElementDefinition);
        elementCategory.setIid(UUID.randomUUID());
        
        Category relationshipCategory = new Category();
        relationshipCategory.setName("relationshipCategory");
        relationshipCategory.setShortName("rc");
        relationshipCategory.getPermissibleClass().add(ClassKind.BinaryRelationship);
        relationshipCategory.setIid(UUID.randomUUID());
        
        BinaryRelationship relationship = new BinaryRelationship();
        relationship.setOwner(this.domain);
        relationship.setName("relationship");
        relationship.getCategory().add(relationshipCategory);
        relationship.setIid(UUID.randomUUID());
        
        parameter0.getRelationships().add(relationship);
        parameter1.getRelationships().add(relationship);
        
        this.elementDefinition0 = new ElementDefinition();
        this.elementDefinition0.setIid(UUID.randomUUID());
        this.elementDefinition0.setName("elementDefinition0");
        this.elementDefinition0.setShortName("ed");
        this.elementDefinition0.getParameter().add(parameter0);
        this.elementDefinition0.setOwner(this.domain);
        this.elementDefinition0.getCategory().add(elementCategory);

        this.elementDefinition1 = new ElementDefinition();
        this.elementDefinition1.setIid(UUID.randomUUID());
        this.elementDefinition0.setName("elementDefinition1");
        this.elementDefinition0.setShortName("ed");
        this.elementDefinition0.getParameter().add(parameter1);
        this.elementDefinition0.setOwner(this.domain);

        this.iteration.getElement().add(elementDefinition0);
        this.iteration.getElement().add(elementDefinition1);
    }
}
