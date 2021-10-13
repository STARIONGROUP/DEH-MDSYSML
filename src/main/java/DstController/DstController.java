/*
 * DstController.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListener;

import HubController.IHubController;
import Reactive.ObservableCollection;
import Services.MappingEngineService.IMappableThingCollection;
import Services.MappingEngineService.IMappingEngineService;
import Utils.Ref;
import cdp4common.commondata.Definition;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.engineeringmodeldata.ParameterValueSetBase;
import cdp4common.engineeringmodeldata.Relationship;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.engineeringmodeldata.ValueSet;
import cdp4common.types.ContainerList;
import cdp4dal.exceptions.TransactionException;
import cdp4dal.operations.ThingTransaction;
import cdp4dal.operations.ThingTransactionImpl;
import cdp4dal.operations.TransactionContextResolver;
import io.reactivex.Observable;
import io.reactivex.internal.observers.ForEachWhileObserver;

/**
 * The {@linkplain DstController} is a class that manage transfer and connection to attached running instance of Cameo/MagicDraw
 */
public final class DstController implements IDstController
{
    /**
     * The current class logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain ProjectEventListener} to monitor project open and closed in Cameo/MagicDraw
     */
    private final MDSysMLProjectEventListener projectEventListener = new MDSysMLProjectEventListener();

    /**
     * The {@linkplain IMappingEngine} instance
     */
    private final IMappingEngineService mappingEngine;

    /**
     * The {@linkplain IHubController} instance
     */
    private final IHubController hubController;

    /**
     * Gets the open Document ({@linkplain Project}) from the running instance of Cameo/MagicDraw
     * 
     * @return the {@linkplain Project}
     */
    @Override
    public Project OpenDocument() 
    {
        return this.projectEventListener.OpenDocumentObservable.Value();
    }

    /**
     * Gets an {@linkplain Observable} of {@linkplain Boolean} indicating if Cameo/MagicDraw has an open document
     * 
     * @return a {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> HasOneDocumentOpenObservable()
    {
        return this.projectEventListener.HasOneDocumentOpenObservable.Observable();
    }
    
    /**
     * Gets a value indicating if Cameo/MagicDraw has an open document
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean HasOneDocumentOpen()
    {
        return this.projectEventListener.HasOneDocumentOpenObservable.Value().booleanValue();
    }
    
    /**
     * Backing field for {@linkplain GetDstMapResult}
     */
    private ObservableCollection<Thing> dstMapResult = new ObservableCollection<Thing>(Thing.class);

    /**
     * Gets The {@linkplain ObservableCollection} of dst map result
     */
    @Override
    public ObservableCollection<Thing> GetDstMapResult()
    {
        return this.dstMapResult;
    }
    
    /**
     * Initializes a new {@linkplain DstController}
     * 
     * @param mappingEngine the {@linkplain IMappingEngine} instance
     * @param hubController the {@linkplain IHubController} instance
     * @param shouldListenForProjectChanges indicates whether the {@linkplain DstController} should watch projects
     */
    public DstController(IMappingEngineService mappingEngine, IHubController hubController, boolean shouldListenForProjectChanges)
    {
        this.mappingEngine = mappingEngine;
        this.hubController = hubController;
        
        if(shouldListenForProjectChanges)
        {
            Application applicationInstance = Application.getInstance();
            applicationInstance.addProjectEventListener(this.projectEventListener);
        }
    }
    
    /**
     * Maps the {@linkplain input} by calling the {@linkplain IMappingEngine}
     * and assign the map result to the dstMapResult or the hubMapResult
     * 
     * @param input the {@linkplain IMappableThingCollection} in other words the  {@linkplain Collection} of {@linkplain Object} to map
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    @Override
    public boolean Map(IMappableThingCollection input)
    {        
        Object resultAsObject = this.mappingEngine.Map(input);

        if(resultAsObject instanceof ArrayList<?>)
        {
            @SuppressWarnings("unchecked")
            ArrayList<? extends Thing> resultAsCollection = (ArrayList<? extends Thing>) resultAsObject;

            if(!resultAsCollection.isEmpty())
            {
                return this.dstMapResult.addAll(resultAsCollection);
            }
        }
        
        return false;
    }
    
    /**
     * Transfers all the {@linkplain Thing} contained in the {@linkplain dstMapResult} to the Hub
     * 
     * @return a value indicating that all transfer could be completed
     */
    @Override
    public boolean TransferToHub()
    {
        try
        {
            Pair<Iteration, ThingTransaction> iterationTransaction = this.hubController.GetIterationTransaction();
            Iteration iterationClone = iterationTransaction.getLeft();
            ThingTransaction transaction = iterationTransaction.getRight();

            this.PrepareThingsForTransfer(iterationClone, transaction);
            
            transaction.createOrUpdate(iterationClone);
            boolean result = this.hubController.TryWrite(transaction);
            result &= this.hubController.Refresh();
            result &= this.UpdateParameterValueSets();
            return result && this.hubController.Refresh();
        } 
        catch (Exception exception)
        {
            this.logger.error(exception);
            return false;
        }
        finally
        {
            this.dstMapResult.clear();
        }
    }
    
    /**
     * Updates the {@linkplain ValueSet} with the new values
     * 
     * @return a value indicating whether the operation went OK
     * @throws TransactionException
     */
    public boolean UpdateParameterValueSets() throws TransactionException
    {
        Pair<Iteration, ThingTransaction> iterationTransaction = this.hubController.GetIterationTransaction();
        Iteration iterationClone = iterationTransaction.getLeft();
        ThingTransaction transaction = iterationTransaction.getRight();

        List<Parameter> allParameters = this.dstMapResult.stream()
                .filter(x -> x instanceof ElementDefinition)
                .flatMap(x -> ((ElementDefinition)x).getParameter().stream())
                .collect(Collectors.toList());
        
        for(Parameter parameter : allParameters)
        {
            Ref<Parameter> refNewParameter = new Ref<Parameter>(Parameter.class);
            
            if(this.hubController.TryGetThingById(parameter.getIid(), refNewParameter))
            {
                Parameter newParameterCloned = refNewParameter.Get().clone(false);
    
                for (int index = 0; index < parameter.getValueSet().size(); index++)
                {
                    ParameterValueSet clone = newParameterCloned.getValueSet().get(index).clone(false);
                    this.UpdateValueSet(clone, parameter.getValueSet().get(index));
                    transaction.createOrUpdate(clone);
                }
    
                transaction.createOrUpdate(newParameterCloned);
            }
        }
        
        transaction.createOrUpdate(iterationClone);
        return this.hubController.TryWrite(transaction);
    }    

    /**
     * Updates the specified {@linkplain ParameterValueSetBase}
     * 
     * @param clone the {@linkplain ParameterValueSetBase} to update
     * @param valueSet the {@linkplain ValueSet} that contains the new values
     */
    private void UpdateValueSet(ParameterValueSetBase clone, ValueSet valueSet)
    {
        clone.setManual(valueSet.getManual());
        clone.setValueSwitch(valueSet.getValueSwitch());
    }

    /**
     * Prepares all the {@linkplain Thing}s that are to be updated or created
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @throws TransactionException
     */
    private void PrepareThingsForTransfer(Iteration iterationClone, ThingTransaction transaction) throws TransactionException
    {
        for (Thing thing : this.dstMapResult)
        {
            switch(thing.getClassKind())
            {
                case ElementDefinition:
                    this.PrepareElementDefinitionForTransfer(iterationClone, transaction, (ElementDefinition)thing);
                    break;
                case RequirementsSpecification:
                    this.PrepareRequirementForTransfer(iterationClone, transaction, (RequirementsSpecification)thing);
                    break;
                default:
                    break;
            }            
        }
    }

    /**
     * Prepares the provided {@linkplain ElementDefinition} for transfer
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @param elementDefinition the {@linkplain ElementDefinition} to prepare
     * @throws TransactionException
     */
    private void PrepareElementDefinitionForTransfer(Iteration iterationClone, ThingTransaction transaction, 
            ElementDefinition elementDefinition) throws TransactionException
    {
        this.AddOrUpdateIterationAndTransaction(elementDefinition, iterationClone.getElement(), transaction);
        
        for(Parameter parameter : elementDefinition.getParameter())
        {
            transaction.createOrUpdate(parameter);
            
            for (Relationship relationship : parameter.getRelationships())
            {
                transaction.createOrUpdate(relationship);
            }
        }
    }

    /**
     * Prepares the provided {@linkplain ElementDefinition} for transfer
     * 
     * @param iterationClone the {@linkplain Iteration} clone
     * @param transaction the {@linkplain ThingTransaction}
     * @param requirementsSpecification the {@linkplain RequirementsSpecification} to prepare
     * @throws TransactionException
     */
    private void PrepareRequirementForTransfer(Iteration iterationClone, ThingTransaction transaction, 
            RequirementsSpecification requirementsSpecification) throws TransactionException
    {        
        this.AddOrUpdateIterationAndTransaction(requirementsSpecification, iterationClone.getRequirementsSpecification(), transaction);
        
        ContainerList<RequirementsGroup> groups = requirementsSpecification.getGroup();
        
        this.RegisterRequirementsGroups(transaction, groups);
        
        for(Requirement requirement : requirementsSpecification.getRequirement())
        {
            transaction.createOrUpdate(requirement);
            
            for (Definition definition : requirement.getDefinition())
            {
                transaction.createOrUpdate(definition);
            }
        }
    }

    /**
     * Registers the {@linkplain RequirementsGroup} to be created or updated
     * 
     * @param transaction
     * @param groups
     * @throws TransactionException
     */
    private void RegisterRequirementsGroups(ThingTransaction transaction, ContainerList<RequirementsGroup> groups) throws TransactionException
    {
        for(RequirementsGroup requirementsGroup : groups)
        {
            transaction.createOrUpdate(requirementsGroup);
            
            if(!requirementsGroup.getGroup().isEmpty())
            {
                this.RegisterRequirementsGroups(transaction, requirementsGroup.getGroup());
            }
        }
    }

    /**
     * Updates the {@linkplain ThingTransaction} and the {@linkplain ContainerList} with the provided {@linkplain Thing}
     * 
     * @param <T> the Type of the current {@linkplain Thing}
     * @param thing the {@linkplain Thing}
     * @param containerList the {@linkplain ContainerList} of {@linkplain Thing} typed as T
     * @param transaction the {@linkplain ThingTransaction}
     * @throws TransactionException
     */
    private <T extends Thing> void AddOrUpdateIterationAndTransaction(T thing, ContainerList<T> containerList, ThingTransaction transaction) throws TransactionException
    {
        if(!containerList.stream().anyMatch(x -> x.getIid().equals(thing.getIid())))
        {
            containerList.add(thing);
        }
        
        transaction.createOrUpdate(thing);
    }
}
