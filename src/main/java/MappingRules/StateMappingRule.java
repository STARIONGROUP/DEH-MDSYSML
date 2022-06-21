/*
 * ElementToBlockStateMappingRule.java
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Region;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;

import Enumerations.MappingDirection;
import HubController.IHubController;
import MappingRules.Interfaces.IStateMappingRule;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Utils.Ref;
import Utils.StreamExtensions;
import Utils.Operators.Operators;
import Utils.Stereotypes.StereotypeUtils;
import cdp4common.ChangeKind;
import cdp4common.engineeringmodeldata.ActualFiniteState;
import cdp4common.engineeringmodeldata.ActualFiniteStateList;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterOrOverrideBase;
import cdp4common.engineeringmodeldata.PossibleFiniteState;
import cdp4common.engineeringmodeldata.PossibleFiniteStateList;
import cdp4common.types.OrderedItemList;

/**
 * The {@linkplain StateMappingRule} is the rules to process states from states
 * machine diagram to allow creation of state dependent {@linkplain Parameter}
 */
public class StateMappingRule implements IStateMappingRule
{
    /**
     * The current class logger
     */
    private Logger logger = LogManager.getLogger();

    /**
     * The {@linkplain IhubController}
     */
    private final IHubController hubController;
    /**
     * The {@linkplain IMagicDrawTransactionService}
     */
    private final IMagicDrawTransactionService transactionService;

    /**
     * The {@linkplain IMagicDrawSessionService}
     */
    private final IMagicDrawSessionService sessionService;

    /**
     * The collection of {@linkplain PossibleFiniteStateList} created
     */
    private ArrayList<PossibleFiniteStateList> createdPossibleFiniteStateLists = new ArrayList<>();

    /**
     * The collection of {@linkplain PossibleFiniteStateList} created
     */
    private ArrayList<ActualFiniteStateList> createdActualFiniteStateLists = new ArrayList<>();

    /**
     * The collection of {@linkplain State} created
     */
    private ArrayList<State> createdStates = new ArrayList<>();

    /**
     * Initializes a new {@linkplain StateMappingRule}
     * 
     * @param hubController the {@linkplain IhubController}
     * @param transactionService the {@linkplain IMagicDrawTransactionService}
     * @param sessionService the {@linkplain IMagicDrawSessionService}
     */
    public StateMappingRule(IHubController hubController, IMagicDrawTransactionService transactionService,
            IMagicDrawSessionService sessionService)
    {
        this.hubController = hubController;
        this.transactionService = transactionService;
        this.sessionService = sessionService;
    }

    /**
     * Clears the collection of created things during one mapping process
     */
    @Override
    public void Clear()
    {
        this.createdActualFiniteStateLists.clear();
        this.createdPossibleFiniteStateLists.clear();
        this.createdStates.clear();
    }

    /**
     * Maps the state dependencies of the provided
     * {@linkplain ParameterOrOverrideBase} or the provided {@linkplain Property},
     * depending on the {@linkplain MappingDirection}
     * 
     * @param parameter The {@linkplain ParameterOrOverrideBase}
     * @param property The {@linkplain Property} property
     * @param mappinDirection the {@linkplain MappingDirection} that applies
     */
    @Override
    public void MapStateDependencies(ParameterOrOverrideBase parameter, Property property,
            MappingDirection mappinDirection)
    {
        switch (mappinDirection)
        {
        case FromDstToHub:
            this.MapStateDependenciesFromDstToHub(parameter, property);
            break;
        case FromHubToDst:
            this.MapStateDependenciesFromHubToDst(parameter, property);
            break;
        default:
            return;
        }
    }

    /**
     * Maps the state dependencies of the provided
     * {@linkplain ParameterOrOverrideBase} of the provided {@linkplain Property}
     * 
     * @param parameter The {@linkplain ParameterOrOverrideBase}
     * @param property The {@linkplain Property} property
     */
    private void MapStateDependenciesFromDstToHub(ParameterOrOverrideBase parameter, Property property)
    {
        List<Dependency> dependencies = StreamExtensions
                .OfType(property.get_directedRelationshipOfSource().stream(), Dependency.class)
                .filter(x -> x.getTarget().stream()
                        .anyMatch(t -> t instanceof State)).collect(Collectors.toList());

        if (dependencies.isEmpty())
        {
            parameter.setStateDependence(null);
            return;
        }

        ArrayList<PossibleFiniteStateList> possibleFiniteStateList = new ArrayList<>();

        for (Dependency dependency : dependencies)
        {
            StreamExtensions.OfType(dependency.getTarget().stream(), State.class).findFirst().ifPresent(x ->
                possibleFiniteStateList.add(this.GetOrCreatePossibleFiniteState(x)));
        }

        if (possibleFiniteStateList.isEmpty())
        {
            parameter.setStateDependence(null);
            return;
        }

        parameter.setStateDependence(this.GetOrCreateActualFiniteStateList(possibleFiniteStateList));
    }

    /**
     * Gets or create the {@linkplain ActualFiniteStateList}
     * 
     * @param possibleFiniteStateListCollection A collection of {@linkplain PossibleFiniteStateList} that the {@linkplain ActualFiniteStateList} should contains
     * @return The created or retrieved {@linkplain ActualFiniteStateList}
     */
    private ActualFiniteStateList GetOrCreateActualFiniteStateList(
            ArrayList<PossibleFiniteStateList> possibleFiniteStateListCollection)
    {
        Ref<ActualFiniteStateList> refActualFiniteStateList = new Ref<>(ActualFiniteStateList.class);

        if (this.TryGetActualFiniteStateListFromCollection(
                this.hubController.GetOpenIteration().getActualFiniteStateList(), possibleFiniteStateListCollection,
                refActualFiniteStateList))
        {
            refActualFiniteStateList.Set(refActualFiniteStateList.Get().clone(true));
        } else if (!this.TryGetActualFiniteStateListFromCollection(this.createdActualFiniteStateLists,
                possibleFiniteStateListCollection, refActualFiniteStateList))
        {
            refActualFiniteStateList.Set(this.CreateActualFiniteStateList(possibleFiniteStateListCollection));
        }

        refActualFiniteStateList.Get().getPossibleFiniteStateList().clear();

        for (PossibleFiniteStateList possibleFiniteStateList : possibleFiniteStateListCollection)
        {
            refActualFiniteStateList.Get().getPossibleFiniteStateList().add(possibleFiniteStateList);
        }

        this.UpdateActualFiniteStateList(refActualFiniteStateList.Get());
        return refActualFiniteStateList.Get();
    }

    /**
     * Update the {@linkplain ActualFiniteStateList} to apply change on the
     * NetChangePreview.
     * 
     * @param actualFiniteStateList The {@linkplain ActualFiniteStateList} to update
     */
    private void UpdateActualFiniteStateList(ActualFiniteStateList actualFiniteStateList)
    {
        List<ArrayList<PossibleFiniteState>> combinations = this
                .GetAllPossibleCombinations(actualFiniteStateList.getPossibleFiniteStateList());

        actualFiniteStateList.getActualState().clear();

        for (Collection<PossibleFiniteState> combination : combinations)
        {
            ActualFiniteState actualFiniteState = new ActualFiniteState();
            actualFiniteState.setIid(UUID.randomUUID());

            for (PossibleFiniteState possibleFiniteState : combination)
            {
                actualFiniteState.getPossibleState().add(possibleFiniteState);
            }

            actualFiniteStateList.getActualState().add(actualFiniteState);
        }
    }

    /**
     * Generates all possible combination crossing all {@linkplain PossibleFiniteState} from all {@linkplain PossibleFiniteStateList}
     * 
     * @param possibleFiniteStateLists A collection of {@linkplain PossibleFiniteStateList}
     * @return A collection of {@linkplain PossibleFiniteState}
     */
    private List<ArrayList<PossibleFiniteState>> GetAllPossibleCombinations(
            Collection<PossibleFiniteStateList> possibleFiniteStateLists)
    {
        ArrayList<Collection<PossibleFiniteState>> allPossibleFiniteState = new ArrayList<>();

        for (PossibleFiniteStateList possibleFiniteStateList : possibleFiniteStateLists)
        {
            allPossibleFiniteState.add(possibleFiniteStateList.getPossibleState());
        }

        List<ArrayList<PossibleFiniteState>> combinations = new ArrayList<>();
        combinations.add(new ArrayList<>());

        for (Collection<PossibleFiniteState> possibleFiniteStates : allPossibleFiniteState)
        {
            combinations = combinations.stream().flatMap(x -> possibleFiniteStates.stream().map(p -> {
                @SuppressWarnings("unchecked")
                ArrayList<PossibleFiniteState> newList = (ArrayList<PossibleFiniteState>) x.clone();
                newList.add(p);
                return newList;
            })).collect(Collectors.toList());
        }

        return combinations;
    }

    /**
     * Creates a {@linkplain ActualFiniteStateList} based on a collection of {@linkplain PossibleFiniteStateList}
     *
     * @param possibleFiniteStateListCollection The collection of {@linkplain possibleFiniteStateListCollection}
     * @return an {@linkplain ActualFiniteStateList}
     */
    private ActualFiniteStateList CreateActualFiniteStateList(
            ArrayList<PossibleFiniteStateList> possibleFiniteStateListCollection)
    {
        ActualFiniteStateList actualFiniteStateList = new ActualFiniteStateList();
        actualFiniteStateList.setIid(UUID.randomUUID());
        actualFiniteStateList.setOwner(this.hubController.GetCurrentDomainOfExpertise());

        for (PossibleFiniteStateList possibleFiniteStateList : possibleFiniteStateListCollection)
        {
            actualFiniteStateList.getPossibleFiniteStateList().add(possibleFiniteStateList);
        }

        this.createdActualFiniteStateLists.add(actualFiniteStateList);

        return actualFiniteStateList;
    }

    /**
     * Gets an {@linkplain ActualFiniteStateList} contained inside a collection
     * 
     * @param actualFiniteStateLists a collection to look into
     * @param possibleFiniteStateListCollection the {@linkplain PossibleFiniteStateList} that compose the {@linkplain ActualFiniteStateList}
     * @param refActualFiniteStateList the {@linkplain Ref} holding the {@linkplain ActualFiniteStateList} that was found
     * 
     * @return A {@linkplain ActualFiniteStateList} if found, null if not present inside the collection
     */
    private boolean TryGetActualFiniteStateListFromCollection(Collection<ActualFiniteStateList> actualFiniteStateLists,
            ArrayList<PossibleFiniteStateList> possibleFiniteStateListCollection,
            Ref<ActualFiniteStateList> refActualFiniteStateList)
    {
        actualFiniteStateLists.stream()
                .filter(x -> x.getPossibleFiniteStateList().stream()
                        .allMatch(p -> possibleFiniteStateListCollection.stream()
                                .anyMatch(s -> Operators.AreTheseEquals(s.getIid(), p.getIid()))))
                .findFirst().ifPresent(x -> refActualFiniteStateList.Set(x));

        return refActualFiniteStateList.HasValue();
    }

    /**
     * Gets or creates a {@linkplain PossibleFiniteStateList} based on a State
     * {@linkplain Element}
     * 
     * @param state The state {@linkplain Element}
     * @return The retrieved or created {@linkplain PossibleFiniteStateList}>
     */
    private PossibleFiniteStateList GetOrCreatePossibleFiniteState(State state)
    {
        List<String> partitions = state.getRegion().stream().map(x -> x.getName()).collect(Collectors.toList());

        String stateShortName = StereotypeUtils.GetShortName(state);

        if (partitions.isEmpty())
        {
            partitions.add(state.getName());
        }

        PossibleFiniteStateList possibleFiniteStateList = null;

        Optional<PossibleFiniteStateList> optionalPossibleFiniteStateList = this.hubController.GetOpenIteration()
                .getPossibleFiniteStateList().stream()
                .filter(x -> Operators.AreTheseEquals(x.getName(), state.getName(), true)
                        || Operators.AreTheseEquals(x.getShortName(), stateShortName, true))
                .findFirst();

        if (optionalPossibleFiniteStateList.isPresent())
        {
            possibleFiniteStateList = optionalPossibleFiniteStateList.get().clone(true);
        }
        else
        {
            possibleFiniteStateList = this.createdPossibleFiniteStateLists.stream()
                    .filter(x -> Operators.AreTheseEquals(x.getName(), state.getName(), true)
                            || Operators.AreTheseEquals(x.getShortName(), stateShortName, true))
                    .findFirst().orElseGet(() -> this.CreatePossibleFiniteStateList(state.getName(), stateShortName));
        }

        this.UpdatePossibleFiniteStateList(possibleFiniteStateList, partitions);

        return possibleFiniteStateList;
    }

    /**
     * Update the current {@linkplain PossibleFiniteStateList} based on the name of partitions
     * 
     * @param possibleFiniteStateList The {@linkplain PossibleFiniteStateList}
     * @param partitions A collection of name to populate the {@linkplain PossibleFiniteStateList.PossibleState}
     */
    private void UpdatePossibleFiniteStateList(PossibleFiniteStateList possibleFiniteStateList, List<String> partitions)
    {
        OrderedItemList<PossibleFiniteState> finiteStates = possibleFiniteStateList.getPossibleState();

        for (int finiteStateIndex = partitions.size(); finiteStateIndex < finiteStates.size(); finiteStateIndex++)
        {
            finiteStates.remove(finiteStates.get(finiteStateIndex));
        }

        for (int namesIndex = 0; namesIndex < partitions.size(); namesIndex++)
        {
            if (finiteStates.size() > namesIndex)
            {
                this.UpdatePossibleFiniteState(finiteStates.get(namesIndex), partitions.get(namesIndex));
            } else
            {
                possibleFiniteStateList.getPossibleState()
                        .add(this.CreatePossibleFiniteState(partitions.get(namesIndex)));
            }
        }
    }

    /**
     * Creates a {@linkplain PossibleFiniteState} based on a named
     * 
     * @param name The name of the {@linkplain PossibleFiniteState}
     * @return The created {@linkplain PossibleFiniteState}
     */
    private PossibleFiniteState CreatePossibleFiniteState(String name)
    {
        PossibleFiniteState possibleFiniteState = new PossibleFiniteState();
        possibleFiniteState.setIid(UUID.randomUUID());
        possibleFiniteState.setName(name);
        possibleFiniteState.setShortName(StereotypeUtils.GetShortName(name));
        return possibleFiniteState;
    }

    /**
     * Updates a {@linkplain PossibleFiniteState} based on a name
     * 
     * @param finiteState The {@linkplain PossibleFiniteState}
     * @param name        The name of the state
     */
    private void UpdatePossibleFiniteState(PossibleFiniteState finiteState, String name)
    {
        String shortName = StereotypeUtils.GetShortName(name);

        if (!Operators.AreTheseEquals(name, finiteState.getName())
                || !Operators.AreTheseEquals(shortName, finiteState.getShortName()))
        {
            finiteState.setName(name);
            finiteState.setShortName(shortName);
        }
    }

    /**
     * Creates a new {@linkplain PossibleFiniteStateList}
     * 
     * @param name The name of the {@linkplain PossibleFiniteStateList}
     * @return A new {@linkplain PossibleFiniteStateList}
     */
    private PossibleFiniteStateList CreatePossibleFiniteStateList(String name, String shortName)
    {
        PossibleFiniteStateList possibleFiniteStateList = new PossibleFiniteStateList();
        possibleFiniteStateList.setIid(UUID.randomUUID());
        possibleFiniteStateList.setName(name);
        possibleFiniteStateList.setShortName(shortName);
        possibleFiniteStateList.setOwner(this.hubController.GetCurrentDomainOfExpertise());

        this.createdPossibleFiniteStateLists.add(possibleFiniteStateList);
        return possibleFiniteStateList;
    }

    /**
     * Maps the state dependencies of the provided {@linkplain ParameterOrOverrideBase} of the provided {@linkplain Property}
     * 
     * @param parameter The {@linkplain ParameterOrOverrideBase}
     * @param property The {@linkplain Property} property
     */
    private void MapStateDependenciesFromHubToDst(ParameterOrOverrideBase parameter, Property property)
    {
        if (parameter.getStateDependence() == null)
        {
            return;
        }

        List<State> existingDependentStates = StreamExtensions
                .OfType(property.get_directedRelationshipOfSource().stream(), Dependency.class)
                .map(x -> StreamExtensions.OfType(x.getTarget().stream(), State.class).findFirst().orElse(null))
                .filter(x -> x != null).collect(Collectors.toList());

        for (PossibleFiniteStateList possibleFiniteState : parameter.getStateDependence().getPossibleFiniteStateList())
        {
            State state = this.GetOrCreateStateAndDependency(property, existingDependentStates, possibleFiniteState);

            this.UpdateState(state, possibleFiniteState);
            this.logger.debug(String.format("State [%s] was created or updated", state.getName()));
        }
    }

    /**
     * Gets or creates a {@linkplain State} that corresponds to the provided {@linkplain possibleFiniteState}
     * 
     * @param property the {@linkplain Property}
     * @param existingDependentStates the {@linkplain List} of existing {@linkplain State}
     * @param possibleFiniteState the {@linkplain PossibleFiniteState}
     * @return a {@linkplain State}
     */
    private State GetOrCreateStateAndDependency(Property property, List<State> existingDependentStates,
            PossibleFiniteStateList possibleFiniteState)
    {
        Predicate<NamedElement> namePredicate = x -> Operators.AreTheseEquals(x.getName(), possibleFiniteState.getName(), true)
                    || Operators.AreTheseEquals(x.getName(), possibleFiniteState.getShortName(), true);

        Optional<State> optionalState = existingDependentStates.stream()
                .filter(x -> namePredicate.test(x))
                .findFirst();
        
        if(!optionalState.isPresent())
        {        
            State state = StreamExtensions.OfType(this.sessionService.GetProjectElements().stream(), StateMachine.class)
                            .flatMap(x -> x.getRegion().stream())
                            .flatMap(x -> StreamExtensions.OfType(x.getOwnedElement().stream(), State.class))
                            .filter(x -> namePredicate.test(x))
                            .findFirst()
                            .orElseGet(() -> this.createdStates.stream()
                                    .filter(x -> namePredicate.test(x))
                                    .findFirst()
                                    .orElseGet(() ->
                                    {
                                        State newState = this.transactionService.Create(State.class, possibleFiniteState.getName());
                                        this.createdStates.add(newState);
                                        return newState;
                                    }));
            
            Dependency dependency = this.transactionService.Create(Dependency.class, "");
            dependency.getSupplier().add(state);
            dependency.getClient().add(property);
            state.get_relationshipOfRelatedElement().add(dependency);
            return state;
        }
        
        return optionalState.get();
    }

    /**
     * Update the {@linkplain State} based on the {@linkplain PossibleFiniteState}
     * 
     * @param state the {@linkplain State} to update
     * @param possibleFiniteStateList the {@linkplain PossibleFiniteStateList}
     */
    private void UpdateState(State state, PossibleFiniteStateList possibleFiniteStateList)
    {
        List<Pair<String, String>> possibleStateNames = possibleFiniteStateList.getPossibleState().stream()
                .map(x -> Pair.of(x.getShortName(), x.getName())).collect(Collectors.toList());

        if (possibleStateNames.size() == 1)
        {
            if (!possibleStateNames.get(0).getRight().equals(state.getName()))
            {
                this.RemovesNonMatchingRegions(state, possibleStateNames);

                if (state.getRegion().isEmpty())
                {
                    this.CreateNewRegion(state, possibleStateNames.get(0).getRight());
                }
            } 
            else
            {
                this.DeleteAllRegionsOfState(state);
            }
        } 
        else
        {
            this.RemovesNonMatchingRegions(state, possibleStateNames);

            List<String> presentNames = state.getRegion().stream().map(x -> x.getName()).collect(Collectors.toList());

            List<String> missingNames = possibleStateNames.stream()
                    .filter(x -> presentNames.stream()
                            .noneMatch(p -> Operators.AreTheseEquals(p, x.getRight(), true)
                                    || Operators.AreTheseEquals(p, x.getLeft(), true)))
                    .map(x -> x.getRight()).collect(Collectors.toList());

            this.logger.info(String.format("Updating states present names = %s, missing names = %s => [Δ = %s]" , presentNames, missingNames, presentNames.size() - missingNames.size()));

            missingNames.forEach(x -> this.CreateNewRegion(state, x));
        }
    }

    /**
     * Adds a new {@linkplain Region} and adds it to the transaction for future
     * process
     * 
     * @param state the {@linkplain State}
     * @param name the name
     */
    private void CreateNewRegion(State state, String name)
    {
        Region newRegion = this.transactionService.Create(Region.class, name);
        this.transactionService.GetModifiedRegions(state).add(Pair.of(newRegion, ChangeKind.CREATE));
    }

    /**
     * Removes all {@linkplain Partition} of a State {@linkplain Element} where the name does not to any {@linkplain PossibleFiniteState}
     * 
     * @param state The State {@linkplain State}
     * @param possibleStateNames A collection containing the short name and name of all {@linkplain PossibleFiniteState}
     */
    private void RemovesNonMatchingRegions(State state, List<Pair<String, String>> possibleStateNames)
    {
        this.transactionService.GetModifiedRegions(state)
                .addAll(state.getRegion().stream()
                        .filter(x -> possibleStateNames.stream()
                                .noneMatch(r -> Operators.AreTheseEquals(x.getName(), r.getRight(), true)))
                        .map(x -> Pair.of(x, ChangeKind.DELETE)).collect(Collectors.toList()));
    }

    /**
     * Deletes all {@linkplain Partition} representing Region of a State {@linkplain Element}
     * 
     * @param state The state {@linkplain Element}
     */
    private void DeleteAllRegionsOfState(State state)
    {
        this.transactionService.GetModifiedRegions(state).addAll(
                state.getRegion().stream().map(x -> Pair.of(x, ChangeKind.DELETE)).collect(Collectors.toList()));
    }
}
