/*
* StateMappingRuleTest.java
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
package MappingRules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Region;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Utils.StreamExtensions;
import cdp4common.ChangeKind;
import cdp4common.engineeringmodeldata.ActualFiniteState;
import cdp4common.engineeringmodeldata.ActualFiniteStateList;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterSwitchKind;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.engineeringmodeldata.PossibleFiniteState;
import cdp4common.engineeringmodeldata.PossibleFiniteStateList;
import cdp4common.types.ValueArray;

class StateMappingRuleTest
{
    private IMagicDrawTransactionService transactionService;
    private IMagicDrawSessionService sessionService;
    private IHubController hubController;
    private StateMappingRule mappingRule;
    private ActualFiniteStateList actualFiniteStateList;
    private Parameter nonStateDependentParameter;
    private Parameter dependentParameter;
    private Property stateDependentProperty;
    private Property nonStateDependentProperty;
    private StateMachine stateMachine0;
    private State state0;
    private State state1;
    private Dependency dependency;
    private Iteration iteration;

    @BeforeEach
    void SetUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.transactionService = mock(IMagicDrawTransactionService.class);
        this.sessionService = mock(IMagicDrawSessionService.class);
        this.SetupHubElements();
        this.SetupDstElements();
        
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        
        when(this.sessionService.GetProjectElements()).thenReturn(Arrays.asList(this.stateMachine0));

        when(this.transactionService.Create(Dependency.class, "")).thenAnswer(x -> 
        {
            return mock(Dependency.class);
        });
        
        when(this.transactionService.Create(eq(Region.class), any(String.class))).thenAnswer(x -> 
        {
            String regionName = x.getArgument(1, String.class);
            Region region = mock(Region.class);
            when(region.getName()).thenReturn(regionName);
            return region;
        });
        
        when(this.transactionService.Create(eq(State.class), any(String.class))).thenAnswer(x ->
        {
            String stateName = x.getArgument(1, String.class);
            
            return StreamExtensions.OfType(this.stateMachine0.getOwnedElement().stream(), NamedElement.class)
                    .filter(s -> Utils.Operators.Operators.AreTheseEquals(s.getName(), stateName))
                    .findFirst()
                    .orElseGet(() -> 
                    {
                        State newState = mock(State.class);
                        when(newState.getName()).thenReturn(stateName);
                        return newState;
                    });
        });

        when(this.transactionService.GetModifiedRegions(any(State.class))).thenReturn(new ArrayList<Pair<Region, ChangeKind>>());
        
        this.mappingRule = new StateMappingRule(this.hubController, transactionService, sessionService);
    }

    private void SetupDstElements()
    {
        this.nonStateDependentProperty = mock(Property.class);
        this.stateDependentProperty = mock(Property.class);
        this.dependency = mock(Dependency.class);        
        
        this.state0 = mock(State.class);
        when(this.state0.getName()).thenReturn("state0");
        this.state1 = mock(State.class);
        when(this.state1.getName()).thenReturn("state1");

        when(this.dependency.getTarget()).thenReturn(Arrays.asList(this.state0));
        when(this.dependency.getSource()).thenReturn(Arrays.asList(this.stateDependentProperty));
        
        this.stateMachine0 = mock(StateMachine.class);
        when(this.stateMachine0.getOwnedElement()).thenReturn(Arrays.asList(state0, state1));
    }

    private void SetupHubElements()
    {
        this.iteration = new Iteration();
        
        PossibleFiniteState possibleFiniteState0 = new PossibleFiniteState();
        possibleFiniteState0.setName("state0");
        possibleFiniteState0.setShortName("state0");
        PossibleFiniteState possibleFiniteState1 = new PossibleFiniteState();
        possibleFiniteState1.setName("state1");
        possibleFiniteState1.setShortName("state1");
        PossibleFiniteState possibleFiniteState2 = new PossibleFiniteState();
        possibleFiniteState2.setName("state2");
        possibleFiniteState2.setShortName("state2");
        
        PossibleFiniteStateList possibleFiniteStateList = new PossibleFiniteStateList();
        possibleFiniteStateList.setName("PossibleStateList");
        possibleFiniteStateList.setShortName("PossibleStateList");
        possibleFiniteStateList.getPossibleState().add(possibleFiniteState0);
        possibleFiniteStateList.getPossibleState().add(possibleFiniteState1);
        possibleFiniteStateList.getPossibleState().add(possibleFiniteState2);
        this.iteration.getPossibleFiniteStateList().add(possibleFiniteStateList);
        
        ActualFiniteState actualFiniteState0 = new ActualFiniteState();
        actualFiniteState0.getPossibleState().add(possibleFiniteState0);
        ActualFiniteState actualFiniteState1 = new ActualFiniteState();
        actualFiniteState0.getPossibleState().add(possibleFiniteState1);
        ActualFiniteState actualFiniteState2 = new ActualFiniteState();
        actualFiniteState0.getPossibleState().add(possibleFiniteState2);
        
        this.actualFiniteStateList = new ActualFiniteStateList();
        this.actualFiniteStateList.getPossibleFiniteStateList().add(possibleFiniteStateList);
        this.actualFiniteStateList.getActualState().add(actualFiniteState0);
        this.actualFiniteStateList.getActualState().add(actualFiniteState1);
        this.actualFiniteStateList.getActualState().add(actualFiniteState2);
        
        this.nonStateDependentParameter = new Parameter();
        this.AddValueSet(this.nonStateDependentParameter);
        
        this.dependentParameter = new Parameter();
        this.dependentParameter.setStateDependence(this.actualFiniteStateList);
        this.AddValueSet(this.dependentParameter, actualFiniteState0, actualFiniteState1, actualFiniteState2);
        
    }

    private void AddValueSet(Parameter parameter, ActualFiniteState ... actualFiniteStates)
    {
        for (ActualFiniteState actualFiniteState : actualFiniteStates)
        {
            ParameterValueSet parameterValueSet = new ParameterValueSet();
            parameterValueSet.setValueSwitch(ParameterSwitchKind.MANUAL);
            parameterValueSet.setActualState(actualFiniteState);
            parameterValueSet.setManual(new ValueArray<String>(Arrays.asList("-"), String.class));
            parameter.getValueSet().add(parameterValueSet);
        }        
    }

    @Test
    void VerifyMapStateDependencies()
    {
        assertDoesNotThrow(() -> this.mappingRule.MapStateDependencies(
                this.dependentParameter, this.nonStateDependentProperty, MappingDirection.FromHubToDst));
        
        assertDoesNotThrow(() -> this.mappingRule.MapStateDependencies(
                this.nonStateDependentParameter, this.stateDependentProperty, MappingDirection.FromDstToHub));

        when(this.stateDependentProperty.get_directedRelationshipOfSource()).thenReturn(Arrays.asList(this.dependency));
        
        assertDoesNotThrow(() -> this.mappingRule.MapStateDependencies(
                this.nonStateDependentParameter, this.stateDependentProperty, MappingDirection.FromDstToHub));
        
        assertDoesNotThrow(() -> this.mappingRule.MapStateDependencies(
                this.dependentParameter, this.nonStateDependentProperty, MappingDirection.FromDstToHub));
        
        assertDoesNotThrow(() -> this.mappingRule.MapStateDependencies(
                this.nonStateDependentParameter, this.stateDependentProperty, MappingDirection.FromHubToDst));
        
        assertDoesNotThrow(() -> this.mappingRule.MapStateDependencies(
                this.nonStateDependentParameter, this.nonStateDependentProperty, MappingDirection.FromDstToHub));
                
        assertDoesNotThrow(() -> this.mappingRule.MapStateDependencies(
                this.nonStateDependentParameter, this.nonStateDependentProperty, MappingDirection.FromHubToDst));
        
        assertDoesNotThrow(() -> this.mappingRule.MapStateDependencies(
                this.dependentParameter, this.stateDependentProperty, MappingDirection.FromHubToDst));
                
        verify(this.transactionService, times(5)).GetModifiedRegions(any(State.class));
        
        assertDoesNotThrow(() -> this.mappingRule.Clear());
    }
}
