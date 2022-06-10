/*
 * BinaryRelationshipsToDirectedRelationshipsMappingRule.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import App.AppContainer;
import DstController.IDstController;
import HubController.IHubController;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.MappingEngineService.MappingRule;
import Services.Stereotype.IStereotypeService;
import Utils.Operators.Operators;
import Utils.Stereotypes.DirectedRelationshipType;
import Utils.Stereotypes.HubRelationshipElementsCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.sitedirectorydata.Category;

/**
 * The {@linkplain BinaryRelationshipsToDirectedRelationshipsMappingRule} is the {@linkplain MappingRule} that maps {@linkplain BinaryRelationShip} to {@linkplain DirectedRelationships}
 */
public class BinaryRelationshipsToDirectedRelationshipsMappingRule extends HubToDstBaseMappingRule<HubRelationshipElementsCollection, ArrayList<Abstraction>>
{
    /**
     * The result of this mapping rule
     */
    private ArrayList<Abstraction> result = new ArrayList<>();

    /**
     * The {@linkplain IDstController}
     */
    IDstController dstController;
    
    /**
     * Initializes a new {@linkplain ComponentToElementMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain IMagicDrawMappingConfigurationService}
     * @param transactionService the {@linkplain IMagicDrawTransactionService}
     * @param stereotypeService the {@linkplain IStereotypeService}
     */
    public BinaryRelationshipsToDirectedRelationshipsMappingRule(IHubController hubController, IMagicDrawMappingConfigurationService mappingConfiguration, 
            IMagicDrawTransactionService transactionService, IStereotypeService stereotypeService)
    {
        super(hubController, mappingConfiguration, transactionService, stereotypeService);
    }
    
    /**
     * Transforms an {@linkplain SysMLComponentCollection} of {@linkplain Component} to an {@linkplain ArrayList} of {@linkplain ElementDefinition}
     * 
     * @param input the {@linkplain SysMLComponentCollection} of {@linkplain Component} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    @Override
    public ArrayList<Abstraction> Transform(Object input)
    {
        try
        {
            if(this.dstController == null)
            {
                this.dstController = AppContainer.Container.getComponent(IDstController.class);
            }
            
            this.Map(this.CastInput(input));
            return new ArrayList<>(this.result);
        }
        catch (Exception exception)
        {
            this.Logger.catching(exception);
            return new ArrayList<>();
        }
        finally
        {
            this.result.clear();
        }
    }
    
    /**
     * Maps the provided collection of {@linkplain MappedElementRowViewModel}
     * 
     * @param mappedElementDefinitions the collection of {@linkplain MappedElementRowViewModel} to map
     */
    private void Map(HubRelationshipElementsCollection elements)
    {
        for (Entry<BinaryRelationship, Pair<MappedElementRowViewModel<? extends Thing, ? extends Class>, MappedElementRowViewModel<? extends Thing, ? extends Class>>> 
                                                                relationshipAndPairs : this.GetMappableBinaryRelationships(elements).entrySet())
        {
            if(this.DoesThisRelationshipAlreadyExist(relationshipAndPairs))
            {
                continue;
            }
            
            this.result.add(this.CreateTrace(relationshipAndPairs));
        }
    }
    
    /**
     * Creates a new SysML {@linkplain Trace}
     * 
     * @param relationshipAndPairs a {@linkplain HashMap} of {@linkplain BinaryRelationship} and a {@linkplain Pair} of {@linkplain MappedElementRowViewModel}
     * @return a new {@linkplain Trace}
     */
    private Abstraction CreateTrace(
            Entry<BinaryRelationship, Pair<MappedElementRowViewModel<? extends Thing, ? extends Class>, MappedElementRowViewModel<? extends Thing, ? extends Class>>> relationshipAndPairs)
    {
        Abstraction newTrace = this.transactionService.Create(this.GetRelationshipType(relationshipAndPairs.getKey()));
        
        Class sourceElement = relationshipAndPairs.getValue().getLeft().GetDstElement();
        
        newTrace.getSource().add(this.transactionService.IsCloned(sourceElement) 
                ? this.transactionService.GetClone(sourceElement).GetOriginal()
                : sourceElement);

        Class targetElement = relationshipAndPairs.getValue().getRight().GetDstElement();
        
        newTrace.getTarget().add(this.transactionService.IsCloned(targetElement) 
                ? this.transactionService.GetClone(targetElement).GetOriginal()
                : targetElement);
                
        this.Logger.debug(String.format("Relationship being mapped : [%s] => [%s]", sourceElement.getName(), targetElement.getName()));
        
        return newTrace;
    }

    /**
     * Get the class of {@linkplain DirectedRelationship} that matches one category applied to the provided {@linkplain BinaryRelationship}
     * 
     * @param binaryRelationship the {@linkplain BinaryRelationship} 
     * @return a {@linkplain DirectedRelationshipType}
     */
    private DirectedRelationshipType GetRelationshipType(BinaryRelationship binaryRelationship)
    {
        for (Category category : binaryRelationship.getCategory())
        {
            DirectedRelationshipType relationshipType = DirectedRelationshipType.From(category);
            
            if(relationshipType != null)
            {
                return relationshipType;
            }
        }
        
        return DirectedRelationshipType.Trace;
    }

    /**
     * Verifies that the trace already exist in the capella model
     * 
     * @param relationshipAndPairs a {@linkplain HashMap} of {@linkplain BinaryRelationship} and a {@linkplain Pair} of {@linkplain MappedElementRowViewModel}
     * @return an assert
     */
    private boolean DoesThisRelationshipAlreadyExist(
            Entry<BinaryRelationship, Pair<MappedElementRowViewModel<? extends Thing, ? extends Class>, MappedElementRowViewModel<? extends Thing, ? extends Class>>> relationshipAndPairs)
    {
        boolean result = relationshipAndPairs.getValue().getLeft().GetDstElement().get_directedRelationshipOfSource().stream()
                    .anyMatch(x -> x.getTarget().stream().anyMatch(r -> Operators.AreTheseEquals(r.getID(), relationshipAndPairs.getValue().getRight().GetDstElement().getID())))
                || this.dstController.GetMappedBinaryRelationshipsToDirectedRelationships().stream().anyMatch(x -> 
                        !x.getTarget().isEmpty() && !x.getSource().isEmpty() 
                        && x.getSource().stream().anyMatch(r -> Operators.AreTheseEquals(r.getID(), relationshipAndPairs.getValue().getLeft().GetDstElement().getID()))
                        && x.getTarget().stream().anyMatch(r -> Operators.AreTheseEquals(r.getID(), relationshipAndPairs.getValue().getRight().GetDstElement().getID())));
        
        return result;
    }

    /**
     * Gets the mappable {@linkplain BinaryRelationship} and its target and source {@linkplain MappedElementRowViewModel}
     * 
     * @param elements the {@linkplain HubRelationshipElementsCollection}
     * @return a {@linkplain HashMap} of {@linkplain BinaryRelationship} and a {@linkplain Pair} of {@linkplain MappedElementRowViewModel}
     */
    private HashMap<BinaryRelationship, Pair<MappedElementRowViewModel<? extends Thing, ? extends Class>, MappedElementRowViewModel<? extends Thing, ? extends Class>>> 
        GetMappableBinaryRelationships(HubRelationshipElementsCollection elements)
    {
        HashMap<BinaryRelationship, Pair<MappedElementRowViewModel<? extends Thing, ? extends Class>, MappedElementRowViewModel<? extends Thing, ? extends Class>>> relatedThings = 
                new HashMap<>();
        
        for (MappedElementRowViewModel<? extends Thing, ? extends Class> mappedElementRowViewModel : elements)
        {
            for(BinaryRelationship relationship : mappedElementRowViewModel.GetHubElement().getRelationships().stream()
                    .filter(x -> x instanceof BinaryRelationship)
                    .map(x -> (BinaryRelationship)x).collect(Collectors.toList()))
            {
                boolean isTarget = Operators.AreTheseEquals(relationship.getTarget().getIid(), mappedElementRowViewModel.GetHubElement().getIid());
                
                Optional<MappedElementRowViewModel<? extends Thing, ? extends Class>> otherElement = elements.stream().filter(x -> isTarget 
                        ? Operators.AreTheseEquals(x.GetHubElement().getIid(), relationship.getSource().getIid())
                        : Operators.AreTheseEquals(x.GetHubElement().getIid(), relationship.getTarget().getIid()))
                .findFirst();
                
                if(otherElement.isPresent())
                {
                    if(isTarget)
                    {
                        relatedThings.put(relationship, Pair.of(otherElement.get(), mappedElementRowViewModel));
                    }
                    else
                    {
                        relatedThings.put(relationship, Pair.of(mappedElementRowViewModel, otherElement.get()));  
                    }
                }
            }
        }
        
        return relatedThings;
    }
}
