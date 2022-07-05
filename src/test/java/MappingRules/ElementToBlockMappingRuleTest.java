/*
 * ElementToBlockMappingRuleTestf.java
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import MappingRules.Interfaces.IStateMappingRule;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.HubElementCollection;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.InterfaceEndKind;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterOverride;
import cdp4common.engineeringmodeldata.ParameterOverrideValueSet;
import cdp4common.engineeringmodeldata.ParameterSwitchKind;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.sitedirectorydata.BooleanParameterType;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import cdp4common.sitedirectorydata.EnumerationParameterType;
import cdp4common.sitedirectorydata.EnumerationValueDefinition;
import cdp4common.sitedirectorydata.RatioScale;
import cdp4common.sitedirectorydata.SimpleQuantityKind;
import cdp4common.sitedirectorydata.SimpleUnit;
import cdp4common.sitedirectorydata.TextParameterType;
import cdp4common.types.ValueArray;

class ElementToBlockMappingRuleTest
{
    private static final String literalEnumerationValue = "valueDefinition2";
    private IHubController hubController;
    private IMagicDrawMappingConfigurationService mappingConfiguration;
    private IMagicDrawTransactionService transactionService;
    private ElementToBlockMappingRule mappingRule;
    private HubElementCollection elements;
    private IDstController dstController;
    private DomainOfExpertise domain;
    private Iteration iteration;
    private ElementDefinition elementDefinition1;
    private ElementDefinition elementDefinition0;
    private ElementDefinition elementDefinition2;
    private RatioScale scale;
    private SimpleQuantityKind quantityKind;
    private BooleanParameterType booleanParameterType;
    private ElementUsage elementUsage0;
    private ParameterOverride parameterOverride0;
    private SimpleUnit unit;
    private TextParameterType stringParameterType;
    private EnumerationParameterType enumParameterType;
    private BasicEList<EnumerationLiteral> literals;
    private IStateMappingRule stateMappingRule;
    private IStereotypeService stereotypeService;
    private ElementUsage elementUsage1;
    private ElementUsage elementUsage2;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void Setup()
    {
        this.hubController = mock(IHubController.class);
        this.mappingConfiguration = mock(IMagicDrawMappingConfigurationService.class);
        this.transactionService = mock(IMagicDrawTransactionService.class);
        this.dstController = mock(IDstController.class);
        this.stateMappingRule = mock(IStateMappingRule.class);
        this.stereotypeService = mock(IStereotypeService.class);
                
        when(this.transactionService.Create(any(Stereotypes.class), any(String.class)))
            .thenAnswer(x -> this.AnswerToTransactionServiceCreate(x));

        when(this.transactionService.Create(any(java.lang.Class.class), any(String.class)))
            .thenAnswer(x -> this.AnswerToTransactionServiceCreate(x));
        
        when(this.stereotypeService.GetStereotype(any(Enum.class)))
            .thenReturn(mock(Stereotype.class));
        
        
        this.mappingRule = new ElementToBlockMappingRule(this.hubController, this.mappingConfiguration, this.transactionService, this.stateMappingRule, this.stereotypeService);
        this.SetupElements();
        this.mappingRule.dstController = this.dstController;
    }
    
    private Object AnswerToTransactionServiceCreate(InvocationOnMock invocationData)
    {
        Stereotypes type = invocationData.getArgument(0, Stereotypes.class);
        
        if(invocationData.getArguments().length == 2)
        {
            return MockElement(invocationData.getArgument(1, String.class), type);
        }
        
        return this.MockElement("", type);
    }

    private Object MockElement(String elementName, Stereotypes type)
    {
        NamedElement mock = mock(type.GetType());
        when(mock.getName()).thenReturn(elementName);
        
        if(type == Stereotypes.Block)
        {
            when(((Class)mock).getOwnedAttribute()).thenReturn(new BasicEList<Property>());
            when(mock.eContents()).thenReturn(new BasicEList<EObject>());
        }

        if(type == Stereotypes.PartProperty)
        {
            doAnswer(invocation -> 
            {
                Class propertyType = invocation.getArgument(0, Class.class);
                when(((Property)mock).getType()).thenReturn(propertyType);
                return null;
            }).when((Property)mock)
            .setType(any(Class.class));
        }
                
        return mock;
    }

    @SuppressWarnings("unchecked")
    @Test
    void VerifyTransform()
    {
        assertDoesNotThrow(() -> this.mappingRule.Transform(null));
        assertDoesNotThrow(() -> this.mappingRule.Transform(mock(List.class)));
        assertEquals(3, this.mappingRule.Transform(this.elements).size());

        when(this.dstController.TryGetElementBy(any(), any(Ref.class))).thenAnswer(x -> 
        {
            Ref<EObject> refElement = x.getArgument(1, Ref.class);
            
            refElement.Set((Class)this.MockElement("", Stereotypes.Block));
            return true;
        });
        
        when(this.transactionService.CloneElement(any())).thenAnswer(x -> x.getArgument(0));
        
        assertEquals(3, this.mappingRule.Transform(this.elements).size());

        this.elements.clear();
        this.elements.add(new MappedElementDefinitionRowViewModel(elementDefinition2, null, MappingDirection.FromHubToDst));
        
        assertEquals(1, this.mappingRule.Transform(this.elements).size());
        
        verify(this.transactionService, times(29)).Create(any(Stereotypes.class), any(String.class));
        verify(this.transactionService, times(5)).AddReferenceDataToDataPackage(any(DataType.class));
    }
    
    private void SetupElements()
    {
        this.literals = new BasicEList<EnumerationLiteral>();
        EnumerationLiteral literal = mock(EnumerationLiteral.class);
        when(literal.getName()).thenReturn(literalEnumerationValue);
        this.literals.add(literal);
        
        this.elements = new HubElementCollection();
        
        this.domain = new DomainOfExpertise(UUID.randomUUID(), null, null);
        this.iteration = new Iteration(UUID.randomUUID(), null, null);
        
        this.scale = new RatioScale();
        this.scale.setName("scale");
        this.unit = new SimpleUnit();
        this.unit.setName("unit");
        this.unit.setShortName("unit");
        this.scale.setUnit(this.unit);
        
        this.quantityKind = new SimpleQuantityKind();
        this.quantityKind.setName("quantityKind");
        this.quantityKind.getPossibleScale().add(this.scale);
        this.quantityKind.setDefaultScale(this.scale);
        
        this.booleanParameterType = new BooleanParameterType();
        this.booleanParameterType.setName("booleanParameterType");
        
        this.stringParameterType = new TextParameterType();
        this.stringParameterType.setName("stringParameterType");

        this.enumParameterType = new EnumerationParameterType();
        this.enumParameterType.setName("enumParameterType");
        ArrayList<EnumerationValueDefinition> definitions = new ArrayList<>();
        EnumerationValueDefinition valueDefinition0 = new EnumerationValueDefinition();
        valueDefinition0.setName("valueDefinition0");
        valueDefinition0.setShortName("valueDefinition0");
        definitions.add(valueDefinition0);
        EnumerationValueDefinition valueDefinition1 = new EnumerationValueDefinition();
        valueDefinition1.setName("valueDefinition1");
        valueDefinition1.setShortName("valueDefinition1");
        definitions.add(valueDefinition1);
        EnumerationValueDefinition valueDefinition2 = new EnumerationValueDefinition();
        valueDefinition2.setName(literalEnumerationValue);
        valueDefinition2.setShortName(literalEnumerationValue);
        definitions.add(valueDefinition2);
        
        this.enumParameterType.getValueDefinition().addAll(definitions);
        
        Parameter parameter0 = new Parameter();
        parameter0.setParameterType(this.quantityKind);
        parameter0.setOwner(this.domain);
        parameter0.setScale(this.scale);
        ParameterValueSet parameterValueSet0 = new ParameterValueSet();
        parameterValueSet0.setManual(new ValueArray<String>(Arrays.asList("0.9883"), String.class));
        parameterValueSet0.setValueSwitch(ParameterSwitchKind.MANUAL);
        parameter0.getValueSet().add(parameterValueSet0);
        
        Parameter parameter1 = new Parameter();
        parameter1.setParameterType(this.booleanParameterType);
        parameter1.setOwner(this.domain);
        ParameterValueSet parameterValueSet1 = new ParameterValueSet();
        parameterValueSet1.setManual(new ValueArray<String>(Arrays.asList("true"), String.class));
        parameterValueSet1.setValueSwitch(ParameterSwitchKind.MANUAL);
        parameter1.getValueSet().add(parameterValueSet1);
        
        Parameter parameter2 = new Parameter();
        parameter2.setParameterType(this.stringParameterType);
        parameter2.setOwner(this.domain);
        ParameterValueSet parameterValueSet2 = new ParameterValueSet();
        parameterValueSet2.setManual(new ValueArray<String>(Arrays.asList("stringstring"), String.class));
        parameterValueSet2.setValueSwitch(ParameterSwitchKind.MANUAL);
        parameter2.getValueSet().add(parameterValueSet2);
        
        Parameter parameter3 = new Parameter();
        parameter3.setParameterType(this.enumParameterType);
        parameter3.setOwner(this.domain);
        ParameterValueSet parameterValueSet3 = new ParameterValueSet();
        parameterValueSet3.setManual(new ValueArray<String>(Arrays.asList(literalEnumerationValue), String.class));
        parameterValueSet3.setValueSwitch(ParameterSwitchKind.MANUAL);
        parameter3.getValueSet().add(parameterValueSet3);
        
        this.elementDefinition2 = new ElementDefinition();
        this.elementDefinition2.setName("elementDefinition2");
        this.elementDefinition2.setShortName("elementDefinition2");
        Category category0 = new Category();
        category0.setName("category0");
        Category category1 = new Category();
        category1.setName("category1");
        Category category2 = new Category();
        category2.setName("Logical Component");
        this.elementDefinition2.getCategory().addAll(Arrays.asList(category0, category1, category2));
        
        this.elementDefinition0 = new ElementDefinition();
        this.elementDefinition0.setName("elementDefinition0");
        this.elementDefinition0.setShortName("elementDefinition0");
        this.elementDefinition1 = new ElementDefinition();
        this.elementDefinition1.setName("elementDefinition1");
        this.elementDefinition1.setShortName("elementDefinition1");
        this.elementUsage0 = new ElementUsage();
        this.elementUsage0.setName("elementUsage0");
        this.elementUsage0.setShortName("elementUsage0");
        this.elementUsage0.setElementDefinition(this.elementDefinition0);
        this.elementUsage0.setInterfaceEnd(InterfaceEndKind.NONE);
        this.elementUsage1 = new ElementUsage();
        this.elementUsage1.setName("elementUsage1");
        this.elementUsage1.setShortName("elementUsage1");
        this.elementUsage1.setElementDefinition(this.elementDefinition2);
        this.elementUsage1.setInterfaceEnd(InterfaceEndKind.INPUT);
        this.elementUsage2 = new ElementUsage();
        this.elementUsage2.setName("elementUsage2");
        this.elementUsage2.setShortName("elementUsage2");
        this.elementUsage2.setElementDefinition(this.elementDefinition1);
        this.elementUsage2.setInterfaceEnd(InterfaceEndKind.OUTPUT);
        
        this.parameterOverride0 = new ParameterOverride();
        this.parameterOverride0.setParameter(parameter0);
        ParameterOverrideValueSet parameterOverrideValueSet0 = new ParameterOverrideValueSet();
        parameterOverrideValueSet0.setParameterValueSet(parameterValueSet0);
        parameterOverrideValueSet0.setManual(new ValueArray<String>(Arrays.asList("1.9585"), String.class));
        this.parameterOverride0.getValueSet().add(parameterOverrideValueSet0);

        this.elementDefinition0.getParameter().add(parameter0);
        this.elementDefinition0.getParameter().add(parameter1);
        this.elementDefinition0.getParameter().add(parameter2);
        this.elementDefinition0.getContainedElement().add(elementUsage1);
        this.elementDefinition2.getParameter().add(parameter3);
        this.elementDefinition2.getContainedElement().add(this.elementUsage2);
        this.elementDefinition1.getContainedElement().add(this.elementUsage0);
        
        BinaryRelationship relationship = new BinaryRelationship();
        relationship.setSource(this.elementUsage2);
        relationship.setTarget(this.elementUsage1);
        
        this.iteration.getRelationship().add(relationship);
        this.iteration.getElement().add(this.elementDefinition0);
        this.iteration.getElement().add(this.elementDefinition1);
        
        MappedElementDefinitionRowViewModel mappedElement0 = new MappedElementDefinitionRowViewModel(this.elementDefinition0, null, MappingDirection.FromHubToDst);
        MappedElementDefinitionRowViewModel mappedElement1 = new MappedElementDefinitionRowViewModel(this.elementDefinition1, null, MappingDirection.FromHubToDst);
        MappedElementDefinitionRowViewModel mappedElement2 = new MappedElementDefinitionRowViewModel(this.elementDefinition2, null, MappingDirection.FromHubToDst);
        
        this.elements.add(mappedElement0);
        this.elements.add(mappedElement1);
        this.elements.add(mappedElement2);
    }        
}
