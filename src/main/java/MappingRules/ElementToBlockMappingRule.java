/*
 * ElementToBlockMappingRule.java
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

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.ecore.xml.type.internal.DataValue;
import org.javafmi.modeldescription.v2.Unit;

import com.nomagic.magicdraw.sysml.util.MDCustomizationForSysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Enumeration;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import App.AppContainer;
import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Utils.Ref;
import Utils.ValueSetUtils;
import Utils.Stereotypes.HubElementCollection;
import Utils.Stereotypes.StereotypeUtils;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.engineeringmodeldata.ElementBase;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.InterfaceEndKind;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterOrOverrideBase;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.engineeringmodeldata.ValueSet;
import cdp4common.sitedirectorydata.BooleanParameterType;
import cdp4common.sitedirectorydata.EnumerationParameterType;
import cdp4common.sitedirectorydata.EnumerationValueDefinition;
import cdp4common.sitedirectorydata.MeasurementScale;
import cdp4common.sitedirectorydata.MeasurementUnit;
import cdp4common.sitedirectorydata.ParameterType;
import cdp4common.sitedirectorydata.PrefixedUnit;
import cdp4common.sitedirectorydata.QuantityKind;
import cdp4common.sitedirectorydata.TextParameterType;
import net.bytebuddy.asm.Advice.This;

/**
 * The {@linkplain ElementToClassMappingRule} is the mapping rule implementation for transforming Capella {@linkplain Class} to {@linkplain ElementDefinition}
 */
public class ElementToBlockMappingRule extends HubToDstBaseMappingRule<HubElementCollection, ArrayList<MappedElementDefinitionRowViewModel>>
{
    /**
     * The {@linkplain IMagicDrawSessionService}
     */
    private final IMagicDrawSessionService sessionService;

    /**
     * The {@linkplain HubElementCollection} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    private HubElementCollection elements;
    
    /**
     * The {@linkplain Collection} of {@linkplain DataType} that were created during this mapping
     */
    private Collection<DataType> temporaryDataTypes = new ArrayList<>();

    /**
     * The {@linkplain Collection} of {@linkplain Unit} that were created during this mapping
     */
    private Collection<InstanceSpecification> temporaryUnits = new ArrayList<>();

    /**
     * Initializes a new {@linkplain ElementToClassMappingRule}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain IMagicDrawMappingConfigurationService}
     * @param transactionService the {@linkplain IMagicDrawTransactionService}
     * @param sessionService the {@linkplain IMagicDrawSessionService}
     */
    public ElementToBlockMappingRule(IHubController hubController, IMagicDrawMappingConfigurationService mappingConfiguration,
            IMagicDrawTransactionService transactionService, IMagicDrawSessionService sessionService)
    {
        super(hubController, mappingConfiguration, transactionService);
        this.sessionService = sessionService;
    }
    
    /**
     * Transforms an {@linkplain HubElementCollection} of {@linkplain Class} to an {@linkplain ArrayList} of {@linkplain ElementDefinition}
     * 
     * @param input the {@linkplain HubElementCollection} of {@linkplain Class} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    @Override
    public ArrayList<MappedElementDefinitionRowViewModel> Transform(Object input)
    {
        try
        {
            if(this.dstController == null)
            {
                this.dstController = AppContainer.Container.getComponent(IDstController.class);
            }
            
            this.elements = this.CastInput(input);

            this.Map(this.elements);
            this.SaveMappingConfiguration(this.elements, MappingDirection.FromHubToDst);
            return new ArrayList<>(this.elements);
        }
        catch (Exception exception)
        {
            this.Logger.catching(exception);
            return new ArrayList<>();
        }
        finally
        {
            this.temporaryUnits.clear();
            this.temporaryDataTypes.clear();
        }
    }
    
    /**
     * Maps the provided collection of {@linkplain ElementBase}
     * 
     * @param mappedElementDefinitions the collection of {@linkplain Class} to map
     */
    private void Map(HubElementCollection mappedElementDefinitions)
    {        
        for (MappedElementDefinitionRowViewModel mappedElement : new ArrayList<MappedElementDefinitionRowViewModel>(mappedElementDefinitions))
        {
            if(mappedElement.GetDstElement() == null)
            {
                Class element = this.GetOrCreateElement(mappedElement.GetHubElement());
                mappedElement.SetDstElement(element);
            }
            
            this.MapContainedElement(mappedElement);
            this.MapProperties(mappedElement.GetHubElement(), mappedElement.GetDstElement());
            this.MapPort(mappedElement);
        }
    }
    
    /**
     * Updates the containment information of the provided parent and element
     * 
     * @param parent the {@linkplain Class} parent
     * @param element the {@linkplain Class} child
     */
    private void UpdateContainement(Class parent, Class element)
    {
        Property property = parent.getOwnedAttribute().stream()
                .filter(x -> AreTheseEquals(x.getType().getID(), element.getID()))
                .findFirst()
                .orElseGet(() -> 
                {
                    Property newPartProperty = this.transactionService.Create(Stereotypes.PartProperty, element.getName());
                    parent.getOwnedAttribute().add(newPartProperty);
                    return newPartProperty;
                });
        
        property.setType(element);
    }
    
    /**
     * Maps the properties of the provided {@linkplain ElementDefinition}
     * 
     * @param hubElement the {@linkplain ElementDefinition} from which the properties are to be mapped
     * @param element the target {@linkplain Class}
     */
    private void MapProperties(ElementDefinition hubElement, Class element)
    {
        this.MapProperties(hubElement.getParameter(), element);
    }

    /**
     * Maps the properties of the provided {@linkplain ElementUsage}
     * 
     * @param hubElement the {@linkplain ElementUsage} from which the properties are to be mapped
     * @param element the target {@linkplain Class}
     */
    private void MapProperties(ElementUsage hubElement, Class element)
    {
        List<ParameterOrOverrideBase> allParametersAndOverrides = hubElement.getElementDefinition().getParameter().stream()
                .filter(x -> hubElement.getParameterOverride().stream()
                        .noneMatch(o -> AreTheseEquals(x.getParameterType().getIid(), o.getParameterType().getIid())))
                .map(x -> (ParameterOrOverrideBase)x)
                .collect(Collectors.toList());
        
        allParametersAndOverrides.addAll(hubElement.getParameterOverride());
        
        this.MapProperties(allParametersAndOverrides, element);
    }
    
    /**
     * Maps the properties of the provided {@linkplain Collection} of {@linkplain ParameterOrOverrideBase}
     * 
     * @param parameters the {@linkplain Collection} of {@linkplain ParameterOrOverrideBase} to map
     * @param element the target {@linkplain Class}
     */
    private void MapProperties(Collection<? extends ParameterOrOverrideBase> parameters, Class element)
    {
        for (ParameterOrOverrideBase parameter : parameters)
        {
            Ref<DataType> refParameterType = new Ref<>(DataType.class);
            Ref<Property> refProperty = new Ref<>(Property.class);
            
            if(!TryGetExistingProperty(element, parameter, refProperty))
            {
                this.GetOrCreateDataType(parameter, refParameterType);                             
                this.CreateProperty(parameter, refProperty, refParameterType);
                element.getOwnedAttribute().add(refProperty.Get());
            }
            if(refProperty.Get().getType() != null)
            {
                refParameterType.Set((DataType)refProperty.Get().getType());
            }
            if(refProperty.Get().getAppliedStereotypeInstance() == null)
            {
                StereotypesHelper.addStereotype(refProperty.Get(), StereotypeUtils.GetStereotype(this.sessionService.GetProject(), Stereotypes.ValueProperty));
            }
            
            this.UpdateValue(parameter, refProperty, refParameterType);
        }
    }

    /**
     * Get or creates the {@linkplain DataType} that fits the provided {@linkplain Parameter}
     * 
     * @param parameter the {@linkplain MeasurementScale} to map
     * @param element the target {@linkplain Class}
     * @param refParameterType the {@linkplain Ref} of {@linkplain DataType} that will contains the output {@linkplain DataType}
     */
    private void GetOrCreateDataType(ParameterOrOverrideBase parameter, Ref<DataType> refParameterType)
    {
        if(parameter.getScale() != null && !AreTheseEquals(parameter.getScale().getUnit().getShortName(), "1"))
        {
            this.GetOrCreateDataType(parameter.getParameterType(), parameter.getScale(), refParameterType);
        }
        else
        {
            this.GetOrCreateDataType(parameter.getParameterType(), refParameterType);
        }
    }
    
    /**
     * Get or creates the {@linkplain DataType} that matches the provided {@linkplain ParameterType}
     * 
     * @param parameterType the {@linkplain ParameterType} to map
     * @param element the target {@linkplain Class}
     * @param refDataTypeType the {@linkplain Ref} of {@linkplain DataType} that will contains the output {@linkplain DataType}
     */
    private void GetOrCreateDataType(ParameterType parameterType, Ref<DataType> refDataTypeType)
    {
        this.QueryCollectionByNameAndShortName(parameterType, this.temporaryDataTypes, refDataTypeType);
        
        if(!refDataTypeType.HasValue() && !this.dstController.TryGetDataType(parameterType, null, refDataTypeType))
        {
            DataType newDataType = this.transactionService.Create(Stereotypes.ValueType, parameterType.getName());
            
            if(newDataType instanceof Enumeration)
            {
                this.CreateEnumerationLiterals((Enumeration)newDataType, (EnumerationParameterType)parameterType);
            }
            
            this.temporaryDataTypes.add(newDataType);
            this.transactionService.AddReferenceDataToDataPackage(newDataType);
            refDataTypeType.Set(newDataType);
        }
    }

    /**
     * Creates the possible {@linkplain EnumerationLiteral} for the provided {@linkplain Enumeration} based on the provided {@linkplain EnumerationParameterType}
     * 
     * @param enumerationDataType the {@linkplain Enumeration} data type
     * @param enumerationParameterType the {@linkplain EnumerationParameterType} parameter type
     */
    private void CreateEnumerationLiterals(Enumeration enumerationDataType, EnumerationParameterType enumerationParameterType)
    {
        for (EnumerationValueDefinition valueDefinition : enumerationParameterType.getValueDefinition())
        {
            enumerationDataType.getOwnedLiteral().add(this.transactionService.Create(EnumerationLiteral.class, valueDefinition.getName()));
        }
    }

    /**
     * Get or creates the {@linkplain DataType} that matches the provided {@linkplain MeasurementScale}
     * 
     * @param parameterType the {@linkplain ParameterType} to map
     * @param scale the {@linkplain MeasurementScale} to map
     * @param refDataType the {@linkplain Ref} of {@linkplain DataType} that will contains the output {@linkplain DataType}
     */
    private void GetOrCreateDataType(ParameterType parameterType, MeasurementScale scale, Ref<DataType> refDataType)
    {
        this.QueryCollectionByNameAndShortName(scale, this.temporaryDataTypes, refDataType);
        
        if(!refDataType.HasValue() && !this.dstController.TryGetDataType(parameterType, scale, refDataType))
        {
            DataType newDataType = this.transactionService.Create(Stereotypes.ValueType, parameterType.getName());
            
            if(scale.getUnit() != null)
            {
                InstanceSpecification newUnit = this.GetOrCreateUnit(scale.getUnit());

                if(scale.getUnit() instanceof PrefixedUnit)
                {
                    StereotypesHelper.setStereotypePropertyValue(newUnit, 
                            StereotypeUtils.GetStereotype(this.sessionService.GetProject(), Stereotypes.Unit), "prefix", ((PrefixedUnit)scale.getUnit()).getPrefix());
                }
                
                StereotypesHelper.setStereotypePropertyValue(newUnit, 
                        StereotypeUtils.GetStereotype(this.sessionService.GetProject(), Stereotypes.Unit), "symbol", scale.getUnit().getShortName());
                
                StereotypesHelper.setStereotypePropertyValue(newDataType, 
                        StereotypeUtils.GetStereotype(this.sessionService.GetProject(), Stereotypes.ValueType), "unit", newUnit);
                
                newDataType.setName(String.format("%s[%s]", newDataType.getName(), scale.getUnit().getShortName()));
            }
            
            this.temporaryDataTypes.add(newDataType);
            this.transactionService.AddReferenceDataToDataPackage(newDataType);
            refDataType.Set(newDataType);
        }
    }

    /**
     * Gets or creates the {@linkplain Unit} that matches the provided {@linkplain MeasurementUnit}
     * 
     * @param unit the {@linkplain MeasurementUnit} 
     * @return a matching {@linkplain Unit}
     */
    private InstanceSpecification GetOrCreateUnit(MeasurementUnit unit)
    {
        Ref<InstanceSpecification> refUnit = new Ref<>(InstanceSpecification.class);        

        this.QueryCollectionByNameAndShortName(unit, this.temporaryUnits, refUnit);
        
        if(!refUnit.HasValue() && !this.dstController.TryGetUnit(unit, refUnit))
        {
            InstanceSpecification newUnit = this.transactionService.Create(Stereotypes.Unit, unit.getName());
            refUnit.Set(newUnit);
            this.temporaryUnits.add(newUnit);
            this.transactionService.AddReferenceDataToDataPackage(newUnit);
        }        
        
        return refUnit.Get();
    }
    
    /**
     * Updates the value of the provided {@linkplain Property}
     * 
     * @param parameter the {@linkplain ParameterOrOverrideBase} that contains the values to transfer
     * @param refProperty the {@linkplain Ref} of {@linkplain Property}
     * @param refDataType the {@linkplain Ref} of {@linkplain DataType}
     */
    private void UpdateValue(ParameterOrOverrideBase parameter, Ref<Property> refProperty, Ref<DataType> refDataType)
    {
        Ref<ValueSpecification> refDataValue = new Ref<>(ValueSpecification.class);
        
        if (refProperty.Get().getDefaultValue() != null)
        {
            refDataValue.Set(refProperty.Get().getDefaultValue());
        }
        else
        {
            refDataValue.Set(CreateValueSpecification(parameter, refDataType));
            refProperty.Get().setDefaultValue(refDataValue.Get());
        }
        
        UpdateValue(refDataValue.Get(), parameter);
    }
    
    /**
     * Updates the value of the provided {@linkplain Property}
     * 
     * @param dataValue the {@linkplain DataValue}
     * @param parameter the {@linkplain ParameterOrOverrideBase} that contains the values to transfer
     * @param refProperty the {@linkplain Ref} of {@linkplain Property} 
     */
    private void UpdateValue(ValueSpecification valueSpecification, ParameterOrOverrideBase parameter)
    {
        String value = ValueSetUtils.QueryParameterBaseValueSet(parameter, null, null).getActualValue().get(0);
        Optional<String> valueString = "-".equals(value) || StringUtils.isBlank(value) ? Optional.empty() : Optional.of(value);
        
        if(valueSpecification instanceof LiteralInteger && valueString.isPresent())
        {
            ((LiteralInteger)valueSpecification).setValue(Integer.parseInt(valueString.get()));
        }
        else if(valueSpecification instanceof LiteralUnlimitedNatural && valueString.isPresent())
        {
            ((LiteralUnlimitedNatural)valueSpecification).setValue(Integer.parseInt(valueString.get()));
        }
        else if(valueSpecification instanceof LiteralReal && valueString.isPresent())
        {
            ((LiteralReal)valueSpecification).setValue(Double.parseDouble(valueString.get()));
        }
        else if(valueSpecification instanceof LiteralBoolean && valueString.isPresent())
        {
            ((LiteralBoolean)valueSpecification).setValue(Boolean.parseBoolean(valueString.get()));
        }
        else if(valueSpecification instanceof LiteralString)
        {
            ((LiteralString)valueSpecification).setValue(valueString.isPresent() ? valueString.get() : "-");
        }
    }

    /**
     * Gets the {@linkplain DataValue} class type
     * 
     * @param parameter the {@linkplain parameter}
     * @return a {@linkplain Class} of {@linkplain LiteralSpecification}
     */
    private java.lang.Class<? extends LiteralSpecification> GetValueSpecificationType(ParameterOrOverrideBase parameter)
    {
        if(parameter.getParameterType() instanceof QuantityKind)
        {
            return LiteralReal.class;
        }
        if(parameter.getParameterType() instanceof BooleanParameterType)
        {
            return LiteralBoolean.class;
        }
        if(parameter.getParameterType() instanceof TextParameterType)
        {
            return LiteralString.class;
        }
        
        return LiteralString.class;
    }
    
    /**
     * Creates the {@linkplain ValueSpecification} based on the provided {@linkplain ParameterOrOverrideBase}
     * 
     * @return a {@linkplain ValueSpecification}
     */
    private ValueSpecification CreateValueSpecification(ParameterOrOverrideBase parameter, Ref<DataType> refDataType)
    {
        java.lang.Class<? extends ValueSpecification> valueType = this.GetValueSpecificationType(parameter);
        
        ValueSpecification valueSpecification = null;
        
        if(valueType != null)
        {
            valueSpecification = this.transactionService.Create(valueType);
        }
        
        return valueSpecification;
    }

    /**
     * Creates a {@linkplain Property} based on the provided {@linkplain ParameterOrOverrideBase}
     * 
     * @param parameter the {@linkplain ParameterOrOverrideBase}
     * @param refProperty the {@linkplain Ref} {@linkplain Property}
     * @param dstDataType the {@linkplain Ref} of {@linkplain DataType}
     */
    private void CreateProperty(ParameterOrOverrideBase parameter, Ref<Property> refProperty, Ref<DataType> dstDataType)
    {
        Property newProperty = this.transactionService.Create(Stereotypes.ValueProperty, parameter.getParameterType().getName());
        
        if(dstDataType.HasValue())
        {
            newProperty.setDatatype(dstDataType.Get());
            newProperty.setType(dstDataType.Get());
        }
        
        refProperty.Set(newProperty);
    }

    /**
     * Tries to get an existing {@linkplain Property} that matches the provided {@linkplain ParameterOrOverrideBase}
     * 
     * @param dstElement the {@linkplain Class}
     * @param parameter the {@linkplain ParameterOrOverrideBase} 
     * @param refProperty the {@linkplain Ref} of {@linkplain Property}
     * @return a value indicating whether the {@linkplain Property} could be found
     */
    private boolean TryGetExistingProperty(Class dstElement, ParameterOrOverrideBase parameter, Ref<Property> refProperty)
    {
        Optional<Property> optionalProperty = dstElement.getOwnedAttribute().stream()
                .filter(x -> AreTheseEquals(x.getName(), parameter.getParameterType().getName(), true)
                        || AreTheseEquals(x.getName(), parameter.getParameterType().getShortName(), true))
                .findFirst();
        
        if(optionalProperty.isPresent())
        {
            refProperty.Set(optionalProperty.get());
        }
        
        return refProperty.HasValue();
    }

    /**
     * Maps the contained element of the provided {@linkplain MappedElementDefinitionRowViewModel} dst element
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void MapContainedElement(MappedElementDefinitionRowViewModel mappedElement)
    {
        for (ElementUsage containedUsage : mappedElement.GetHubElement().getContainedElement().stream()
                .filter(x -> x.getInterfaceEnd() == InterfaceEndKind.NONE).collect(Collectors.toList()))
        {
            MappedElementDefinitionRowViewModel usageDefinitionMappedElement = this.elements.stream()
                    .filter(x -> AreTheseEquals(x.GetDstElement().getName(), containedUsage.getElementDefinition().getName(), true))
                    .findFirst()
                    .orElseGet(() -> 
                    {
                        MappedElementDefinitionRowViewModel newMappedElement = new MappedElementDefinitionRowViewModel(containedUsage.getElementDefinition(),
                                this.GetOrCreateElement(containedUsage), MappingDirection.FromHubToDst);
                        
                        this.elements.add(newMappedElement);
                        return newMappedElement;
                    });
            
            this.MapProperties(containedUsage, usageDefinitionMappedElement.GetDstElement());
            this.MapPort(usageDefinitionMappedElement);
            this.UpdateContainement(mappedElement.GetDstElement(), usageDefinitionMappedElement.GetDstElement());
            this.MapContainedElement(usageDefinitionMappedElement);
        }        
    }

    /**
     * Maps the port for the specified {@linkplain MappedElementDefinitionRowViewModel}
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void MapPort(MappedElementDefinitionRowViewModel mappedElement)
    {
        for (ElementUsage containedUsage : mappedElement.GetHubElement().getContainedElement().stream()
                .filter(x -> x.getInterfaceEnd() != InterfaceEndKind.NONE).collect(Collectors.toList()))
        {
            Ref<Port> refPort = new Ref<>(Port.class);
            Ref<Class> refDefinition = new Ref<>(Class.class);
            
            if(!this.GetOrCreatePort(containedUsage, mappedElement.GetDstElement(), refPort, refDefinition))
            {
                continue;
            }
            
            mappedElement.GetDstElement().getOwnedElement().removeIf(x -> AreTheseEquals(x.getID(), refPort.Get().getID()));
            mappedElement.GetDstElement().getOwnedElement().add(refPort.Get());
            mappedElement.GetDstElement().getOwnedElement().removeIf(x -> AreTheseEquals(x.getID(), refDefinition.Get().getID()));
            mappedElement.GetDstElement().getOwnedElement().add(refDefinition.Get());
        }
    }

    /**
     * Gets or create the {@linkplain Port} based on the provided {@linkplain port}
     * 
     * @param port the {@linkplain ElementUsage} port 
     * @param parent the {@linkplain Class} parent
     * @param refPort the {@linkplain Ref} of {@linkplain Port}
     * @param refDefinition the {@linkplain Ref} of the definition block
     * @return a {@linkplain boolean}
     */
    private boolean GetOrCreatePort(ElementUsage port, Class parent, Ref<Port> refPort, Ref<Class> refDefinition)
    {
        parent.getOwnedPort().stream()
            .filter(x -> AreTheseEquals(x.getName(), port.getName()))
            .findFirst()
            .ifPresent(x -> refPort.Set(x));
                
        if(!refPort.HasValue() && !this.dstController.TryGetElementByName(port, refPort))
        {
            refPort.Set(this.transactionService.Create(Stereotypes.PortProperty, port.getName()));
        }

        if(refPort.Get().getType() == null && !this.dstController.TryGetElementByName(port.getElementDefinition(), refDefinition))
        {
            refDefinition.Set(this.transactionService.Create(Stereotypes.Block, port.getElementDefinition().getName()));
            refPort.Get().setType(parent);
            parent.getOwnedElement().add(refDefinition.Get());
        }
                
        return refPort.HasValue() && refDefinition.HasValue();
    }

    /**
     * Gets or creates a element based on an {@linkplain ElementDefinition}
     * 
     * @param elementBase the {@linkplain ElementBase}
     * @param targetArchitecture the {@linkplain CapellaArchitecture} that determines the type of the element
     * @return an existing or a new {@linkplain Class}
     */
    private Class GetOrCreateElement(ElementBase elementBase)
    {
        return this.GetOrCreateElement(elementBase.getName());
    }
    
    /**
     * Gets or creates the {@linkplain RequirementsPkg} that can represent the {@linkplain RequirementsSpecification}
     * 
     * @param <TClass> the type of {@linkplain Class}
     * @param hubElementName the {@linkplain String} element name in the HUB side
     * @return a {@linkplain RequirementsPkg}
     */
    private Class GetOrCreateElement(String hubElementName)
    {
        Ref<Class> refElement = new Ref<>(Class.class);
        
        if(!this.dstController.TryGetElementBy(x -> x instanceof NamedElement && 
                AreTheseEquals(((NamedElement) x).getName(), hubElementName, true), refElement))
        {
            refElement.Set(this.transactionService.Create(Stereotypes.Block, hubElementName));
        }
        else
        {
            refElement.Set(this.transactionService.Clone(refElement.Get()));
        }
        
        
        return refElement.Get();
    }
    
    /**
     * Searches for the provided {@linkplain Collection} for an {@linkplain #TElement} 
     * where the name could match either the name or short name of the provided {@linkplain DefinedThing}.
     * If found, it assigns the value to the provided {@linkplain Ref}
     * 
     * @param <TElement> the type of {@linkplain NamedElement} to get
     * @param definedThing the {@linkplain DefinedThing}
     * @param collection the {@linkplain Collection} of {@linkplain #TElement} to query
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     */
    private <TElement extends NamedElement> void QueryCollectionByNameAndShortName(DefinedThing definedThing, 
            Collection<? extends TElement> collection, Ref<TElement> refElement)
    {
        collection.stream()
                .filter(x -> AreTheseEquals(x.getName(), definedThing.getName(), true) 
                        || AreTheseEquals(x.getName(), definedThing.getShortName(), true))
                .findAny()
                .ifPresent(x -> refElement.Set(x));
    }
}
