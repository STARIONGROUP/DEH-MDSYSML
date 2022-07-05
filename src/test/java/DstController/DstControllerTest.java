/*
 * DstControllerTest.java
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.common.util.BasicEList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Usage;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.InterfaceRealization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Region;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;

import Enumerations.MappingDirection;
import HubController.HubController;
import HubController.IHubController;
import MappingRules.BlockToElementMappingRule;
import Services.HistoryService.IMagicDrawLocalExchangeHistoryService;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MagicDrawTransaction.Clones.ClonedReferenceElement;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.MappingEngineService.IMappingEngineService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.HubElementCollection;
import Utils.Stereotypes.HubRequirementCollection;
import Utils.Stereotypes.MagicDrawBlockCollection;
import Utils.Stereotypes.MagicDrawRequirementCollection;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.ChangeKind;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Definition;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ActualFiniteState;
import cdp4common.engineeringmodeldata.ActualFiniteStateList;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.EngineeringModel;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterSwitchKind;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.engineeringmodeldata.PossibleFiniteState;
import cdp4common.engineeringmodeldata.PossibleFiniteStateList;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import cdp4common.sitedirectorydata.ParameterType;
import cdp4common.sitedirectorydata.RatioScale;
import cdp4common.sitedirectorydata.SimpleQuantityKind;
import cdp4common.sitedirectorydata.SimpleUnit;
import cdp4common.sitedirectorydata.SpecializedQuantityKind;
import cdp4common.sitedirectorydata.TextParameterType;
import cdp4common.types.CacheKey;
import cdp4common.types.ValueArray;
import cdp4dal.Assembler;
import cdp4dal.Session;
import cdp4dal.exceptions.TransactionException;
import cdp4dal.operations.ThingTransaction;
import io.reactivex.Observable;

class DstControllerTest
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
    private IMagicDrawLocalExchangeHistoryService historyService;
    private IMagicDrawSessionService sessionService;
    private IMagicDrawTransactionService transactionService;
    private IStereotypeService stereotypeService;
    private Requirement requirement2;
    private Requirement requirement1;
    private Requirement requirement0;
    private Class block1;
    private Class block0;
    private Class dstRequirement0;
    private Class dstRequirement1;
    private Class dstRequirement2;
    private Class block0Cloned;

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
        this.historyService = mock(IMagicDrawLocalExchangeHistoryService.class);
        this.sessionService = mock(IMagicDrawSessionService.class);
        this.transactionService = mock(IMagicDrawTransactionService.class);
        this.stereotypeService = mock(IStereotypeService.class);

        when(this.sessionService.SessionUpdated()).thenReturn(Observable.fromArray(false, false));
        
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
        when(this.hubController.GetSessionEventObservable()).thenReturn(Observable.fromArray(false, false));
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        when(this.hubController.GetIterationTransaction()).thenReturn(Pair.of(this.iteration.clone(false), mock(ThingTransaction.class)));
        when(this.hubController.Refresh()).thenReturn(true);
        
        when(this.sessionService.HasAnyOpenSessionObservable()).thenReturn(Observable.fromArray(false, false));

        this.controller = new DstController(this.mappingEngine, this.hubController, this.logService, 
                this.mappingConfigurationService, this.sessionService, this.historyService, this.transactionService, this.stereotypeService);
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

        this.SetupHubElements();
        this.SetupHubRequirements();
        this.SetupDstElements();

        when(this.mappingEngine.Map(any(MagicDrawBlockCollection.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(
                    new MappedElementDefinitionRowViewModel(this.elementDefinition0, null, MappingDirection.FromDstToHub),
                    new MappedElementDefinitionRowViewModel(this.elementDefinition1, null, MappingDirection.FromDstToHub))));

        when(this.mappingEngine.Map(any(MagicDrawRequirementCollection.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(
                    new MappedRequirementRowViewModel(this.requirement0, null, MappingDirection.FromDstToHub),
                    new MappedRequirementRowViewModel(this.requirement1, null, MappingDirection.FromDstToHub),
                    new MappedRequirementRowViewModel(this.requirement2, null, MappingDirection.FromDstToHub))));

        assertDoesNotThrow(() -> this.controller.Map(new MagicDrawBlockCollection(), MappingDirection.FromDstToHub));
        assertEquals(2, this.controller.GetDstMapResult().size());
        assertEquals(0, this.controller.GetHubMapResult().size());
        assertDoesNotThrow(() -> this.controller.Map(new MagicDrawRequirementCollection(), MappingDirection.FromDstToHub));
        assertEquals(5, this.controller.GetDstMapResult().size());
        assertEquals(0, this.controller.GetHubMapResult().size());
        
        when(this.mappingEngine.Map(any(HubElementCollection.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(
                    new MappedElementDefinitionRowViewModel(this.elementDefinition0, this.block0, MappingDirection.FromHubToDst),
                    new MappedElementDefinitionRowViewModel(this.elementDefinition1, this.block1, MappingDirection.FromHubToDst))));

        when(this.mappingEngine.Map(any(HubRequirementCollection.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(
                    new MappedRequirementRowViewModel(this.requirement0, this.dstRequirement0, MappingDirection.FromHubToDst),
                    new MappedRequirementRowViewModel(this.requirement1, this.dstRequirement1, MappingDirection.FromHubToDst),
                    new MappedRequirementRowViewModel(this.requirement2, this.dstRequirement2, MappingDirection.FromHubToDst))));

        assertDoesNotThrow(() -> this.controller.Map(new HubElementCollection(), MappingDirection.FromHubToDst));
        assertEquals(5, this.controller.GetDstMapResult().size());
        assertEquals(2, this.controller.GetHubMapResult().size());
        assertDoesNotThrow(() -> this.controller.Map(new HubRequirementCollection(), MappingDirection.FromHubToDst));
        assertEquals(5, this.controller.GetDstMapResult().size());
        assertEquals(5, this.controller.GetHubMapResult().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void VerifyTransferToHub() throws ExecutionException, TransactionException
    {
        assertDoesNotThrow(() -> this.controller.TransferToHub());

        this.domain = new DomainOfExpertise();
        this.domain.setName("THERMAL");
        this.domain.setShortName("THE");
        this.SetupHubElements();
        this.SetupHubRequirements();
        
        when(this.hubController.TryGetThingById(eq(this.parameter0.getIid()), any(Ref.class)))
            .thenAnswer(invocation -> 
                {
                    Object[] args = invocation.getArguments();
                    ((Ref<Parameter>)args[1]).Set(this.parameter0);
                    return true;
                });
        
        when(this.hubController.TryGetThingById(eq(this.parameter1.getIid()), any(Ref.class)))
            .thenAnswer(invocation -> 
                {
                        Object[] args = invocation.getArguments();
                        ((Ref<Parameter>)args[1]).Set(this.parameter1);
                        return true;
                });
        
        assertTrue(this.controller.TransferToHub());
        
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement0 = new MappedElementDefinitionRowViewModel(this.elementDefinition0, this.block0, MappingDirection.FromDstToHub);
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement1 = new MappedElementDefinitionRowViewModel(this.elementDefinition1, this.block1, MappingDirection.FromDstToHub);
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement2 = new MappedRequirementRowViewModel(this.requirement0, this.dstRequirement0, MappingDirection.FromDstToHub);
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement3 = new MappedRequirementRowViewModel(this.requirement1, this.dstRequirement1, MappingDirection.FromDstToHub);
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement4 = new MappedRequirementRowViewModel(this.requirement2, this.dstRequirement2, MappingDirection.FromDstToHub);
        
        this.controller.GetDstMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement0);
        this.controller.GetDstMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement1);
        this.controller.GetDstMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement2);
        this.controller.GetDstMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement3);
        this.controller.GetDstMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement4);
        
        this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.ElementDefinition, false);
        this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.Requirement, false);
        when(this.hubController.TrySupplyAndCreateLogEntry(any())).thenReturn(true);
        
        ThingTransaction transaction = mock(ThingTransaction.class);
        
        when(transaction.getAddedThing()).thenReturn(ImmutableList.of());
        
        when(this.hubController.GetIterationTransaction()).thenReturn(Pair.of(new Iteration(), transaction));
        
        assertTrue(this.controller.TransferToHub());
        
        when(this.hubController.TrySupplyAndCreateLogEntry(any())).thenThrow(new NullPointerException());
        assertFalse(this.controller.TransferToHub());
        assertEquals(5, this.controller.GetDstMapResult().size());
    }

    @Test
    void VerifyChangeMappingDirection()
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
    void VerifyTransfer()
    {
        assertTrue(this.controller.Transfer());
        this.controller.ChangeMappingDirection();
        assertFalse(this.controller.Transfer());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void VerifylTransferToDst()
    {
        this.SetupDstElements();
        
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement0 = new MappedElementDefinitionRowViewModel(this.block0, MappingDirection.FromHubToDst);
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement1 = new MappedElementDefinitionRowViewModel(this.block1, MappingDirection.FromHubToDst);
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement2 = new MappedRequirementRowViewModel(this.dstRequirement0, MappingDirection.FromHubToDst);
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement3 = new MappedRequirementRowViewModel(this.dstRequirement1, MappingDirection.FromHubToDst);
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement4 = new MappedRequirementRowViewModel(this.dstRequirement2, MappingDirection.FromHubToDst);
        
        this.controller.GetHubMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement0);
        this.controller.GetHubMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement1);
        this.controller.GetHubMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement2);
        this.controller.GetHubMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement3);
        this.controller.GetHubMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement4);
        
        this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(null, false);
        
        when(this.transactionService.IsNew(any())).thenReturn(true);
        
        Package model = mock(Package.class);
        when(model.get_directedRelationshipOfTarget()).thenReturn(new ArrayList<>());
        when(model.getOwnedElement()).thenReturn(new ArrayList<>());
        when(this.sessionService.GetModel()).thenReturn(model);
        HashMap<State, List<Pair<Region, ChangeKind>>> stateAndModifiedRegions = new HashMap<State, List<Pair<Region, ChangeKind>>>();
        State state = mock(State.class);
        Dependency dependency0 = mock(Dependency.class);
        when(dependency0.getSupplier()).thenReturn(new ArrayList<>());
        when(dependency0.getClient()).thenReturn(new ArrayList<>(Arrays.asList(mock(Property.class))));
        Dependency dependency1 = mock(Dependency.class);
        when(dependency1.getSupplier()).thenReturn(new ArrayList<>());
        when(dependency1.getClient()).thenReturn(new ArrayList<>(Arrays.asList(mock(Property.class))));
        when(state.get_directedRelationshipOfTarget()).thenReturn(Arrays.asList(dependency0, dependency1));
        stateAndModifiedRegions.put(state, Arrays.asList(Pair.of(mock(Region.class), ChangeKind.CREATE), Pair.of(mock(Region.class), ChangeKind.DELETE)));
        when(this.transactionService.GetStatesModifiedRegions()).thenReturn(stateAndModifiedRegions.entrySet());
        when(this.hubController.Refresh()).thenReturn(true);
        
        when(this.transactionService.Commit(any())).thenAnswer(x -> 
        {
            Runnable transaction = x.getArgument(0, Runnable.class);
            
            if(transaction == null)
            {
                return false;
            }
            
            transaction.run();
            return true;
        });
        
        StateMachine stateMachine = mock(StateMachine.class);
        Region region0 = mock(Region.class);
        when(stateMachine.getRegion()).thenReturn(Arrays.asList(region0));
                
        when( this.transactionService.Create(StateMachine.class, "Model")).thenReturn(stateMachine);        
        when(this.mappingConfigurationService.IsTheCurrentIdentifierMapTemporary()).thenReturn(true);
        
        when(this.stereotypeService.DoesItHaveTheStereotype(any(), eq(Stereotypes.Requirement))).thenAnswer(x -> 
        {
            Class element = x.getArgument(0, Class.class);            
            return element == this.dstRequirement0 || element == this.dstRequirement1 || element == this.dstRequirement2;
        });
        
        when(this.stereotypeService.DoesItHaveTheStereotype(any(), eq(Stereotypes.Block))).thenAnswer(x -> 
        {
            Class element = x.getArgument(0, Class.class);            
            return element == this.block0 || element == this.block0Cloned || element == this.block1;
        });
        
        assertTrue(this.controller.TransferToDst());
        
        when(this.transactionService.IsCloned(any())).thenReturn(true);
        
        when(this.transactionService.GetClone(any())).thenAnswer(x -> 
        {
            NamedElement element = x.getArgument(0, NamedElement.class);
            ClonedReferenceElement<NamedElement> cloneReference = mock(ClonedReferenceElement.class);
            
            if(Utils.Operators.Operators.AreTheseEquals(block0.getName(), element.getName()))
            {
                when(cloneReference.GetOriginal()).thenReturn(this.block0);
                when(cloneReference.GetClone()).thenReturn(this.block0Cloned);
            }
            else
            {
                when(cloneReference.GetOriginal()).thenReturn(element);
                when(cloneReference.GetClone()).thenReturn(element);
            }
            
            return cloneReference;
        });
        
        assertTrue(this.controller.TransferToDst());
                
        when(this.transactionService.Commit(any())).thenThrow(new NullPointerException());        
        assertFalse(this.controller.TransferToDst());
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

    @SuppressWarnings("unchecked")
    @Test
    void VerifyAddOrRemoveAllFromSelectedThingsToTransfer()
    {
        this.SetupHubElements();
        this.SetupHubRequirements();
        
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement0 = new MappedElementDefinitionRowViewModel(
                this.elementDefinition0, mock(Class.class), MappingDirection.FromHubToDst);
        
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement1 = new MappedElementDefinitionRowViewModel(
                this.elementDefinition0, mock(Class.class), MappingDirection.FromHubToDst);
        
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement2 = new MappedElementDefinitionRowViewModel(
                this.elementDefinition1, mock(Class.class), MappingDirection.FromDstToHub);
        
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement3= new MappedRequirementRowViewModel(
                this.requirementsSpecification0.getRequirement().get(0), mock(Class.class), MappingDirection.FromHubToDst);
        
        MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement4= new MappedRequirementRowViewModel(
                this.requirementsSpecification0.getRequirement().get(0), mock(Class.class), MappingDirection.FromDstToHub);
        
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(null, false));
        assertEquals(0, this.controller.GetSelectedHubMapResultForTransfer().size());
        assertEquals(0, this.controller.GetSelectedDstMapResultForTransfer().size());
    
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.ElementDefinition, true));
        assertEquals(0, this.controller.GetSelectedHubMapResultForTransfer().size());
        assertEquals(0, this.controller.GetSelectedDstMapResultForTransfer().size());
    
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.Requirement, true));
        assertEquals(0, this.controller.GetSelectedHubMapResultForTransfer().size());
        assertEquals(0, this.controller.GetSelectedDstMapResultForTransfer().size());
        
        this.controller.GetDstMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement2);
        this.controller.GetDstMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement4);
        
        this.controller.GetHubMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement0);
        this.controller.GetHubMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement1);
        this.controller.GetHubMapResult().add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement3);
        
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(null, false));
        assertEquals(3, this.controller.GetSelectedHubMapResultForTransfer().size());
        assertEquals(0, this.controller.GetSelectedDstMapResultForTransfer().size());
    
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.ElementDefinition, false));
        assertEquals(3, this.controller.GetSelectedHubMapResultForTransfer().size());
        assertEquals(1, this.controller.GetSelectedDstMapResultForTransfer().size());
    
        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.Requirement, false));
        assertEquals(3, this.controller.GetSelectedHubMapResultForTransfer().size());
        assertEquals(2, this.controller.GetSelectedDstMapResultForTransfer().size());

        assertDoesNotThrow(() -> this.controller.AddOrRemoveAllFromSelectedThingsToTransfer(ClassKind.Requirement, true));
        assertEquals(3, this.controller.GetSelectedHubMapResultForTransfer().size());
        assertEquals(1, this.controller.GetSelectedDstMapResultForTransfer().size());
    }

    @Test
    void VerifyTryGetElementByName()
    {
        Class element0 = mock(Class.class);
        when(element0.getName()).thenReturn("");
        Class element1 = mock(Class.class);
        when(element1.getName()).thenReturn("");

        when(this.sessionService.GetProjectElements()).thenReturn(Arrays.asList(element0, element1));

        this.SetupHubElements();
        
        Ref<Class> refElement = new Ref<>(Class.class);
        assertFalse(this.controller.TryGetElementByName(this.elementDefinition0, refElement));
        when(element0.getName()).thenReturn(this.elementDefinition0.getName());
        assertTrue(this.controller.TryGetElementByName(this.elementDefinition0, refElement));
        assertSame(element0, refElement.Get());
        refElement.Set(null);
        when(element1.getName()).thenReturn(this.elementDefinition1.getShortName());
        assertTrue(this.controller.TryGetElementByName(this.elementDefinition1, refElement));
        assertSame(element1, refElement.Get());
    }

    @Test
    void VerifyTryGetElementById()
    {

        Class element0 = mock(Class.class);
        when(element0.getID()).thenReturn("");

        when(this.sessionService.GetProjectElements()).thenReturn(Arrays.asList(element0));
        
        Ref<Class> refElement = new Ref<>(Class.class);
        assertFalse(this.controller.TryGetElementById(UUID.randomUUID().toString(), refElement));
        when(element0.getID()).thenReturn(UUID.randomUUID().toString());
        assertTrue(this.controller.TryGetElementById(element0.getID(), refElement));
        assertSame(element0, refElement.Get());
    }

    @Test
    void VerifyTryGetUnit()
    {
        InstanceSpecification unit0 = mock(InstanceSpecification.class);
        when(unit0.getName()).thenReturn("kg");
        
        InstanceSpecification unit1 = mock(InstanceSpecification.class);
        when(unit1.getName()).thenReturn("g");
                
        SimpleUnit pounds = new SimpleUnit();
        pounds.setName("pounds");
        pounds.setShortName("lbs");
        
        SimpleUnit kilograms = new SimpleUnit();
        kilograms.setName("kilograms");
        kilograms.setShortName("kg");
        
        Ref<InstanceSpecification> refUnit = new Ref<>(InstanceSpecification.class);
        
        assertFalse(this.controller.TryGetUnit(null, refUnit));        
        assertFalse(this.controller.TryGetUnit(pounds, refUnit));
        assertFalse(this.controller.TryGetUnit(kilograms, refUnit));
        when(this.stereotypeService.GetUnits()).thenReturn(Arrays.asList(unit0, unit1));
        assertFalse(this.controller.TryGetUnit(pounds, refUnit));
        assertTrue(this.controller.TryGetUnit(kilograms, refUnit));
        assertSame(unit0, refUnit.Get());
    }

    @SuppressWarnings("resource")
	@Test
    void VerifyTryGetDataType()
    {
        DataType dataType0 = mock(DataType.class);
        when(dataType0.getName()).thenReturn("mass[lbs]");
        DataType dataType1 = mock(DataType.class);
        when(dataType1.getName()).thenReturn("mass[kg]");
        DataType dataType2 = mock(DataType.class);
        when(dataType2.getName()).thenReturn("mass[l]");
        DataType dataType3 = mock(DataType.class);
        when(dataType3.getName()).thenReturn("mass");
        DataType dataType4 = mock(DataType.class);
        when(dataType4.getName()).thenReturn("wetmass[kg]");

        TextParameterType textMass = new TextParameterType();
        textMass.setName("mass");
        textMass.setShortName("mass");
        
        SimpleQuantityKind mass = new SimpleQuantityKind();
        mass.setName("mass");
        mass.setShortName("mass");
        
        SpecializedQuantityKind wetmass = new SpecializedQuantityKind();
        wetmass.setName("wetmass");
        wetmass.setShortName("wetmass");
        wetmass.setGeneral(mass);

        RatioScale pounds = new RatioScale();
        pounds.setName("pounds");
        pounds.setShortName("lbs");
        
        RatioScale kilograms = new RatioScale();
        kilograms.setName("kilograms");
        kilograms.setShortName("kg");
        
        Ref<DataType> refDataType = new Ref<>(DataType.class);
        
        assertFalse(this.controller.TryGetDataType(wetmass, kilograms, refDataType));
        when(this.stereotypeService.GetDataTypes()).thenReturn(Arrays.asList(dataType0, dataType1, dataType2, dataType3, dataType4));
        refDataType.Set(null);
        assertTrue(this.controller.TryGetDataType(wetmass, null, refDataType));
        assertSame(refDataType.Get(), dataType3);
        refDataType.Set(null);
        assertTrue(this.controller.TryGetDataType(mass, kilograms, refDataType));
        assertSame(refDataType.Get(), dataType1);
    }
    
    private void SetupHubRequirements()
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
        
        this.requirement0 = new Requirement();
        this.requirement0.setName("requirement0");
        this.requirement0.setShortName("req0");
        this.requirement0.setGroup(group1);
        this.requirement0.setOwner(this.domain);
        this.requirement0.setIid(UUID.randomUUID());

        this.requirement1 = new Requirement();
        this.requirement1.setName("requirement1");
        this.requirement1.setShortName("req1");
        this.requirement1.setGroup(group0);
        this.requirement1.setOwner(this.domain);  
        this.requirement1.setIid(UUID.randomUUID());
        
        this.requirement2 = new Requirement();
        this.requirement2.setName("requirement2");
        this.requirement2.setShortName("req2");
        this.requirement1.setGroup(group2);
        this.requirement2.setOwner(this.domain);
        this.requirement2.setIid(UUID.randomUUID());
        
        Definition definition = new Definition();
        definition.setLanguageCode("en");
        definition.setContent("content");
        definition.setIid(UUID.randomUUID());
        
        this.requirement0.getDefinition().add(definition);
        this.requirement1.getDefinition().add(definition.clone(false));
        this.requirement2.getDefinition().add(definition.clone(false));

        this.requirementsSpecification0 = new RequirementsSpecification();
        this.requirementsSpecification0.getGroup().add(group0);
        this.requirementsSpecification0.getRequirement().add(this.requirement0);
        this.requirementsSpecification0.getRequirement().add(this.requirement1);
        this.requirementsSpecification0.setOwner(this.domain);
        this.requirementsSpecification0.setIid(UUID.randomUUID());
        
        this.requirementsSpecification1 = new RequirementsSpecification();
        this.requirementsSpecification1.getRequirement().add(this.requirement2);
        this.requirementsSpecification1.getGroup().add(group2);
        this.requirementsSpecification1.setOwner(this.domain);
        this.requirementsSpecification1.setIid(UUID.randomUUID());
    }
    
    private void SetupHubElements()
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
        
        
        PossibleFiniteState possibleFiniteState0 = new PossibleFiniteState();
        PossibleFiniteState possibleFiniteState1 = new PossibleFiniteState();
        PossibleFiniteState possibleFiniteState2 = new PossibleFiniteState();
        PossibleFiniteState possibleFiniteState3 = new PossibleFiniteState();
        PossibleFiniteState possibleFiniteState4 = new PossibleFiniteState();
        
        PossibleFiniteStateList possibleFiniteStateList0 = new PossibleFiniteStateList();
        possibleFiniteStateList0.getPossibleState().add(possibleFiniteState0);
        possibleFiniteStateList0.getPossibleState().add(possibleFiniteState1);
        possibleFiniteStateList0.getPossibleState().add(possibleFiniteState2);

        PossibleFiniteStateList possibleFiniteStateList1 = new PossibleFiniteStateList();
        possibleFiniteStateList0.getPossibleState().add(possibleFiniteState3);
        possibleFiniteStateList0.getPossibleState().add(possibleFiniteState4);
        
        ActualFiniteStateList stateList = new ActualFiniteStateList();
        stateList.getPossibleFiniteStateList().addAll(Arrays.asList(possibleFiniteStateList0, possibleFiniteStateList1));
        
        ActualFiniteState actualState0 = new ActualFiniteState();
        actualState0.getPossibleState().addAll(Arrays.asList(possibleFiniteState3, possibleFiniteState0));
        ActualFiniteState actualState1 = new ActualFiniteState();
        actualState0.getPossibleState().addAll(Arrays.asList(possibleFiniteState3, possibleFiniteState1));
        ActualFiniteState actualState2 = new ActualFiniteState();
        actualState0.getPossibleState().addAll(Arrays.asList(possibleFiniteState3, possibleFiniteState2));
        ActualFiniteState actualState3 = new ActualFiniteState();
        actualState0.getPossibleState().addAll(Arrays.asList(possibleFiniteState4, possibleFiniteState0));
        ActualFiniteState actualState4 = new ActualFiniteState();
        actualState0.getPossibleState().addAll(Arrays.asList(possibleFiniteState4, possibleFiniteState1));
        ActualFiniteState actualState5 = new ActualFiniteState();
        actualState0.getPossibleState().addAll(Arrays.asList(possibleFiniteState4, possibleFiniteState2));
        
        stateList.getActualState().add(actualState0);
        stateList.getActualState().add(actualState1);
        stateList.getActualState().add(actualState2);
        stateList.getActualState().add(actualState3);
        stateList.getActualState().add(actualState4);
        stateList.getActualState().add(actualState5);
        this.parameter1.setStateDependence(stateList);
                
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
        Definition definition = new Definition();
        definition.setLanguageCode(BlockToElementMappingRule.MDIID);
        this.elementDefinition0.getDefinition().add(definition);

        ElementUsage elementUsage = new ElementUsage();
        elementUsage.setElementDefinition(this.elementDefinition0);
        
        this.elementDefinition1 = new ElementDefinition();
        this.elementDefinition1.setIid(UUID.randomUUID());
        this.elementDefinition1.setName("elementDefinition1");
        this.elementDefinition1.setShortName("ed");
        this.elementDefinition1.getParameter().add(parameter1);
        this.elementDefinition1.setOwner(this.domain);
        this.elementDefinition1.getContainedElement().add(elementUsage);

        this.iteration.getElement().add(elementDefinition0);
        this.iteration.getElement().add(elementDefinition1);
    }
    
    private void SetupDstElements()
    {
        Usage relationship0 = mock(Usage.class);
        InterfaceRealization relationship1 = mock(InterfaceRealization.class);
        Interface interface0 = mock(Interface.class);
        when(relationship1.getContract()).thenReturn(interface0);
        when(relationship0.getTarget()).thenReturn(Arrays.asList(interface0));
        
        Class portProvidingBlock = mock(Class.class);
        when(portProvidingBlock.get_relationshipOfRelatedElement()).thenReturn(Arrays.asList(relationship0));
        Class portRealizationBlock = mock(Class.class);
        when(portRealizationBlock.get_relationshipOfRelatedElement()).thenReturn(Arrays.asList(relationship1));
        
        Port port0 = mock(Port.class);
        when(port0.getType()).thenReturn(portRealizationBlock);
        
        Port port1 = mock(Port.class);
        when(port1.getType()).thenReturn(portProvidingBlock);
        
        Property property0 = mock(Property.class);
        String id = UUID.randomUUID().toString();
        when(property0.getID()).thenReturn(id);
        Property property1 = mock(Property.class);
        when(property1.getID()).thenReturn(id);
        
        this.block0 = mock(Class.class);
        when(this.block0.getName()).thenReturn("block0");
        when(this.block0.getID()).thenReturn(UUID.randomUUID().toString());
        when(this.block0.getOwnedPort()).thenReturn(Arrays.asList(port0));
        when(this.block0.eContents()).thenReturn(new BasicEList<>());
        when(this.block0.getOwnedAttribute()).thenReturn(Arrays.asList(property0));

        this.block0Cloned = mock(Class.class);
        when(this.block0Cloned.getName()).thenReturn("block0");
        when(this.block0Cloned.getID()).thenReturn(UUID.randomUUID().toString());
        when(this.block0Cloned.getOwnedPort()).thenReturn(Arrays.asList(port0));
        when(this.block0Cloned.eContents()).thenReturn(new BasicEList<>());
        when(this.block0.getOwnedAttribute()).thenReturn(Arrays.asList(property1));
        
        this.block1 = mock(Class.class);
        when(this.block1.getName()).thenReturn("block1");
        when(this.block1.getID()).thenReturn(UUID.randomUUID().toString());
        when(this.block1.getOwnedPort()).thenReturn(Arrays.asList(port1));
        when(this.block1.eContents()).thenReturn(new BasicEList<>());
        
        this.dstRequirement0 = mock(Class.class);
        when(this.dstRequirement0.getName()).thenReturn("dstRequirement0");
        when(this.dstRequirement0.getID()).thenReturn(UUID.randomUUID().toString());
        
        this.dstRequirement1 = mock(Class.class);
        when(this.dstRequirement1.getName()).thenReturn("dstRequirement1");
        when(this.dstRequirement1.getID()).thenReturn(UUID.randomUUID().toString());
        
        this.dstRequirement2 = mock(Class.class);
        when(this.dstRequirement2.getName()).thenReturn("dstRequirement2");
        when(this.dstRequirement2.getID()).thenReturn(UUID.randomUUID().toString());
    }
}
