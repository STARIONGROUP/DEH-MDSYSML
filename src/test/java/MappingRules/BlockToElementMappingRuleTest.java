/*
 * BlockToElementMappingRuleTest.java
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Predicate;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.InterfaceRealization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;

import Enumerations.MappingDirection;
import HubController.IHubController;
import MappingRules.Interfaces.IStateMappingRule;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.MagicDrawBlockCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.EngineeringModel;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import cdp4common.sitedirectorydata.EngineeringModelSetup;
import cdp4common.sitedirectorydata.MeasurementScale;
import cdp4common.sitedirectorydata.MeasurementUnit;
import cdp4common.sitedirectorydata.ModelReferenceDataLibrary;
import cdp4common.sitedirectorydata.QuantityKind;
import cdp4common.sitedirectorydata.RatioScale;
import cdp4common.sitedirectorydata.ScalarParameterType;
import cdp4common.sitedirectorydata.SimpleQuantityKind;
import cdp4common.sitedirectorydata.SimpleUnit;
import cdp4common.sitedirectorydata.SiteDirectory;
import cdp4common.sitedirectorydata.SiteReferenceDataLibrary;

class BlockToElementMappingRuleTest
{
    private IMagicDrawMappingConfigurationService mappingConfigurationService;
    private IHubController hubController;
    private BlockToElementMappingRule mappingRule;
    private MagicDrawBlockCollection elements;
    private ElementDefinition elementDefinition0;
    private ElementDefinition elementDefinition1;
    private DomainOfExpertise domain;
    private Iteration iteration;
    private Class component0;
    private Class component1;
    private Class component2;
    private ElementDefinition elementDefinition2;
    private IStateMappingRule stateMappingRule;
    private IStereotypeService stereotypeService;

    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings({ "resource", "unchecked" })
    @BeforeEach
    public void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.mappingConfigurationService = mock(IMagicDrawMappingConfigurationService.class);
        this.stateMappingRule = mock(IStateMappingRule.class);
        this.stereotypeService = mock(IStereotypeService.class);
        
        when(this.stereotypeService.IsValueProperty(any(Property.class))).thenAnswer(x -> 
        {
            Property property = x.getArgument(0, Property.class);
            return property.getType() instanceof DataType;
        });

        when(this.stereotypeService.IsPartProperty(any(Property.class))).thenAnswer(x -> 
        {
            Property property = x.getArgument(0, Property.class);
            return property.getType() instanceof Class;
        });
        
        when(this.stereotypeService.GetTypeRepresentation(any(Property.class))).thenAnswer(x -> 
        {
            Property property = x.getArgument(0, Property.class);
            Type type = property.getType();
            
            if(type != null)
            {
                return type.getName();
            }
            
            return " ";
        });
        
        when(this.stereotypeService.GetUnitRepresention(any(Property.class))).thenReturn("");
        
        this.SetupElements();
        
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        ModelReferenceDataLibrary modelReferenceDataLibrary = new ModelReferenceDataLibrary();
        SiteReferenceDataLibrary siteReferenceDataLibrary = new SiteReferenceDataLibrary();
        SiteDirectory siteDirectory = new SiteDirectory();
        EngineeringModelSetup engineeringModelSetup = new EngineeringModelSetup();
        EngineeringModel engineeringModel = new EngineeringModel();
        
        modelReferenceDataLibrary.setRequiredRdl(siteReferenceDataLibrary);
        siteDirectory.getModel().add(engineeringModelSetup);
        
        siteDirectory.getSiteReferenceDataLibrary().add(siteReferenceDataLibrary);
        engineeringModelSetup.getRequiredRdl().add(modelReferenceDataLibrary);

        engineeringModel.setEngineeringModelSetup(engineeringModelSetup);
        engineeringModel.getIteration().add(this.iteration);
        
        when(this.hubController.GetDehpOrModelReferenceDataLibrary()).thenReturn(modelReferenceDataLibrary);
        when(this.hubController.TryGetThingFromChainOfRdlBy(any(Predicate.class), any(Ref.class))).thenReturn(false);

        this.mappingRule = new BlockToElementMappingRule(this.hubController, this.mappingConfigurationService, this.stateMappingRule, this.stereotypeService);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void SetupElements()
    {
        this.domain = new DomainOfExpertise(UUID.randomUUID(), null, null);
        
        this.iteration = new Iteration(UUID.randomUUID(), null, null);
        
        this.elementDefinition0 = new ElementDefinition(UUID.randomUUID(), null, null);
        this.elementDefinition0.setOwner(this.domain);
        this.elementDefinition0.setName("elementDefinition0");
        this.elementDefinition0.setShortName("elementDefinition0");
        
        this.elementDefinition1 = new ElementDefinition(UUID.randomUUID(), null, null);
        this.elementDefinition1.setOwner(this.domain);
        this.elementDefinition1.setName("elementDefinition1");
        this.elementDefinition1.setShortName("elementDefinition1");
        
        this.elementDefinition2 = new ElementDefinition(UUID.randomUUID(), null, null);
        this.elementDefinition2.setOwner(this.domain);
        this.elementDefinition2.setName("element");
        this.elementDefinition2.setShortName("element");
        
        this.iteration.getElement().add(this.elementDefinition0);
        this.iteration.getElement().add(this.elementDefinition1);
        this.iteration.getElement().add(this.elementDefinition2);
        
        this.elements = new MagicDrawBlockCollection();
        
        this.component0 = mock(Class.class);
        when(this.component0.getName()).thenReturn("component0");
        when(this.component0.isAbstract()).thenReturn(true);
        this.component1 = mock(Class.class);
        when(this.component1.getName()).thenReturn("component1");
        when(this.component1.isAbstract()).thenReturn(true);
        when(this.component1.isLeaf()).thenReturn(true);
        when(this.component1.eContents()).thenReturn(new BasicEList());
        this.component2 = mock(Class.class);
        when(this.component2.getName()).thenReturn("element");
        Class component3 = mock(Class.class);
        when(component3.getName()).thenReturn("component3");
        when(component3.eContents()).thenReturn(new BasicEList<EObject>());
        when(component3.getOwnedAttribute()).thenReturn(new BasicEList<Property>());
        when(this.component2.eContents()).thenReturn(new BasicEList<EObject>(Arrays.asList(component3)));
        
        when(this.component0.eContents()).thenReturn(new BasicEList(Arrays.asList(this.component1, this.component2)));
        
        BasicEList<Property> properties = new BasicEList<Property>();
        
        Property property0 = mock(Property.class);
        when(property0.getName()).thenReturn("Property0");
        DataType stringDataType = mock(DataType.class);
        when(stringDataType.getName()).thenReturn("string");
        when(property0.getType()).thenReturn(stringDataType);
        LiteralString dataValue0 = mock(LiteralString.class);
        when(dataValue0.getValue()).thenReturn("label");
        when(property0.getDefaultValue()).thenReturn(dataValue0);
        
        Property property1 = mock(Property.class);
        when(property1.getName()).thenReturn("Property1");
        DataType realDataType = mock(DataType.class);
        when(realDataType.getName()).thenReturn("Mass[kg2]");
        when(property1.getType()).thenReturn(realDataType);
        LiteralReal dataValue1 = mock(LiteralReal.class);
        when(dataValue1.getValue()).thenReturn(53d);        
        when(property1.getDefaultValue()).thenReturn(dataValue1);
        
        Property property2 = mock(Property.class);
        when(property2.getName()).thenReturn("Property2");
        DataType boolDataType = mock(DataType.class);
        when(boolDataType.getName()).thenReturn("Boolean");
        when(property2.getType()).thenReturn(boolDataType);
        LiteralBoolean dataValue2 = mock(LiteralBoolean.class);
        when(dataValue2.isValue()).thenReturn(true);
        when(property2.getDefaultValue()).thenReturn(dataValue2);
                
        properties.add(property0);
        properties.add(property1);
        properties.add(property2);

        Property partProperty0 = mock(Property.class);
        when(partProperty0.getName()).thenReturn("partProperty0");
        when(boolDataType.getName()).thenReturn("Boolean");
        when(partProperty0.getType()).thenReturn(this.component0);
        
        BasicEList<Property> properties1 = new BasicEList<Property>();
        properties.add(partProperty0);
        
        when(this.component0.getOwnedAttribute()).thenReturn(properties1);
        when(this.component1.getOwnedAttribute()).thenReturn(properties);
        when(this.component2.getOwnedAttribute()).thenReturn(properties);
        
        Class implBlock = mock(Class.class);
        when(implBlock.getOwner()).thenReturn(this.component0);
                
        Interface interface0 = mock(Interface.class);
        when(interface0.getName()).thenReturn("interface0");
        InterfaceRealization interfaceRealization = mock(InterfaceRealization.class);
        when(interfaceRealization.getContract()).thenReturn(interface0);
        when(interfaceRealization.getOwner()).thenReturn(implBlock);
        
        when(interface0.get_interfaceRealizationOfContract()).thenReturn(Arrays.asList(interfaceRealization));
        
        Port port0 = mock(Port.class);
        when(port0.getName()).thenReturn("port0");
        when(port0.getProvided()).thenReturn(new BasicEList<Interface>(Arrays.asList(interface0)));
        when(port0.getRequired()).thenReturn(new BasicEList<Interface>());
        when(port0.getOwner()).thenReturn(this.component0);
        Port port1 = mock(Port.class);
        when(port1.getRequired()).thenReturn(new BasicEList<Interface>(Arrays.asList(interface0)));
        when(port1.getProvided()).thenReturn(new BasicEList<Interface>());
        when(port1.getOwner()).thenReturn(this.component1);
        Port port2 = mock(Port.class);
        when(port2.getRequired()).thenReturn(new BasicEList<Interface>(Arrays.asList(interface0, interface0)));
        when(port2.getProvided()).thenReturn(new BasicEList<Interface>());
        when(port2.getOwner()).thenReturn(this.component2);
        Port port3 = mock(Port.class);
        when(port3.getName()).thenReturn("port3");
        when(port3.getRequired()).thenReturn(new BasicEList<Interface>());
        when(port3.getProvided()).thenReturn(new BasicEList<Interface>(Arrays.asList(interface0)));
        when(port3.getOwner()).thenReturn(this.component2);
        
        when(this.component0.getOwnedPort()).thenReturn(new ArrayList<Port>(Arrays.asList(port0)));
        when(this.component1.getOwnedPort()).thenReturn(new ArrayList<Port>(Arrays.asList(port1)));
        when(this.component2.getOwnedPort()).thenReturn(new ArrayList<Port>(Arrays.asList(port2, port3)));
        
        this.elements.add(new MappedElementDefinitionRowViewModel(this.elementDefinition0, this.component0, MappingDirection.FromDstToHub));
        this.elements.add(new MappedElementDefinitionRowViewModel(this.elementDefinition1, this.component1, MappingDirection.FromDstToHub));
        this.elements.add(new MappedElementDefinitionRowViewModel(this.component2, MappingDirection.FromDstToHub));
    }

    @SuppressWarnings("unchecked")
    @Test
    void VerifyTransform()
    {
        assertDoesNotThrow(() -> this.mappingRule.Transform(this.elements));
        
        this.elements.clear();
        this.elements.add(new MappedElementDefinitionRowViewModel(this.elementDefinition0, this.component0, MappingDirection.FromDstToHub));
        this.elements.add(new MappedElementDefinitionRowViewModel(this.elementDefinition1, this.component1, MappingDirection.FromDstToHub));
        this.elements.add(new MappedElementDefinitionRowViewModel(this.component2, MappingDirection.FromDstToHub));
        
        when(this.hubController.TryGetThingFromChainOfRdlBy(any(Predicate.class), any(Ref.class)))
        .thenAnswer(new Answer<Boolean>() 
        {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable 
            {
                Object[] arguments = invocation.getArguments();
                
                Ref<?> ref = ((Ref<?>)arguments[1]);
                
                if(ref.GetType().isAssignableFrom(MeasurementScale.class))
                {
                    ((Ref<MeasurementScale>)ref).Set(new RatioScale());
                }
                if(ref.GetType().isAssignableFrom(Category.class))
                {
                    ((Ref<Category>)ref).Set(new Category());
                }
                else if(ref.GetType().isAssignableFrom(MeasurementUnit.class))
                {
                    ((Ref<MeasurementUnit>)ref).Set(new SimpleUnit());
                }
                else if(ref.GetType().isAssignableFrom(QuantityKind.class))
                {
                    SimpleQuantityKind parameterType = new SimpleQuantityKind();
                    parameterType.setShortName("Property0");
                    parameterType.getPossibleScale().add(new RatioScale());
                    ((Ref<ScalarParameterType>)ref).Set(parameterType);
                }
                
                return true;
            }
        });
        
        assertDoesNotThrow(() -> this.mappingRule.Transform(this.elements));
    }
}
