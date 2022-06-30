/*
 * BlockToElementMappingRule.java
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
import static Utils.Stereotypes.StereotypeUtils.GetShortName;
import static Utils.Stereotypes.StereotypeUtils.IsOwnedBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import Enumerations.MappingDirection;
import HubController.IHubController;
import MappingRules.Interfaces.IStateMappingRule;
import Reactive.ObservableCollection;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.MagicDrawBlockCollection;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Definition;
import cdp4common.dto.ParameterValueSetBase;
import cdp4common.engineeringmodeldata.ActualFiniteState;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.InterfaceEndKind;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterSwitchKind;
import cdp4common.engineeringmodeldata.ParameterValueSet;
import cdp4common.engineeringmodeldata.ParameterValueSetBase;
import cdp4common.engineeringmodeldata.Relationship;
import cdp4common.sitedirectorydata.BooleanParameterType;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.MeasurementScale;
import cdp4common.sitedirectorydata.MeasurementUnit;
import cdp4common.sitedirectorydata.NumberSetKind;
import cdp4common.sitedirectorydata.ParameterType;
import cdp4common.sitedirectorydata.QuantityKind;
import cdp4common.sitedirectorydata.RatioScale;
import cdp4common.sitedirectorydata.ReferenceDataLibrary;
import cdp4common.sitedirectorydata.SimpleQuantityKind;
import cdp4common.sitedirectorydata.SimpleUnit;
import cdp4common.sitedirectorydata.TextParameterType;
import cdp4common.types.ValueArray;
import javassist.bytecode.analysis.ControlFlow.Block;

/**
 * The {@linkplain BlockToElementMappingRule} is the mapping rule implementation for transforming {@linkplain Element} to {@linkplain ElementDefinition}
 */
public class BlockToElementMappingRule extends DstToHubBaseMappingRule<MagicDrawBlockCollection, ArrayList<MappedElementDefinitionRowViewModel>>
{
    /**
     * A string that is used when retrieving scale and unit
     */
    private static final String scaleAndUnitSeparator = "~";

    /**
     * The string that indicates the language code for the {@linkplain Definition} for {@linkplain ElementDefinition}s
     * That contains the MagicDraw Id of the mapped {@linkplain Block}
     */
    public static final String MDIID = "MDIID";    

    /**
     * The string that specifies the {@linkplain ElementDefinition} representing ports
     */
    private static final String PORTELEMENTDEFINITIONNAME = "Port";
        
    /**
     * The {@linkplain IStateMappingRule} instance
     */
    private final IStateMappingRule stateMappingRule;
    
    /**
     * The isLeaf category names where pair.item0 is the short name
     */
    private final Pair<String, String> isLeafCategoryNames = Pair.of("LEA", "isLeaf");
    
    /**
     * The isAbstract category names where pair.item0 is the short name
     */
    private final Pair<String, String> isAbstractCategoryNames = Pair.of("ABS", "isAbstract");
    
    /**
     * The isActive category names where pair.item0 is the short name
     */
    private final Pair<String, String> isActiveCategoryNames = Pair.of("ACT", "isActive");
    
    /**
     * The isEncapsulated category names where pair.item0 is the short name
     */
    private final Pair<String, String> isEncapsulatedCategoryNames = Pair.of("ENC", "isEncapsulated");
    
    /**
     * The connector category and BinaryRelationship names where pair.item0 is the short name
     */
    private final Pair<String, String> connectorPropertyNames = Pair.of("connectorProperty", "Connector Property");
    
    /**
     * The binding connectors category and BinaryRelationship names where pair.item0 is the short name
     */
    private final Pair<String, String> bindingConnectorNames = Pair.of("bindingConnector", "Binding Connector");
    
    /**
     * The category for interface/BinaryRelationship names where pair.item0 is the short name
     */
    private final Pair<String, String> interfaceCategoryNames = Pair.of("interface", "interface");

    /**
     * Holds the {@linkplain BinaryRelationship} that are to be updated or created
     */
    public final List<BinaryRelationship> binaryRelationShips = new ArrayList<>();

    /**
     * Holds the collection of {@linkplain Pair} of {@linkplain Property} representing a connected property and {@linkplain ElementDefinition} for future relationship creation
     */
    private List<Pair<Property, ElementDefinition>> connectedElements = new ArrayList<>();
    
    /**
     * The collection of of {@linkplain Pair} of {@linkplain Port} representing a connected port and the {@linkplain MappedElementDefinitionRowViewModel} for future relationship creation
     */
    private List<Triple<Port, MappedElementDefinitionRowViewModel, ElementUsage>> portsToConnect = new ArrayList<>();

    /**
     * The {@linkplain MagicDrawBlockCollection} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    private MagicDrawBlockCollection elements;
    
    /**
     * The {@linkplain ElementDefinition} that represents the ports
     */
    private ElementDefinition portElementDefinition;
    
    /**
     * Initializes a new {@linkplain BlockToElementMappingRule}
     * 
     * @param HubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain IMagicDrawMappingConfigurationService}
     * @param stateMappingRule the {@linkplain IStateMappingRule}
     * @param stereotypeService the {@linkplain IStereotypeService}
     */
    public BlockToElementMappingRule(IHubController hubController, IMagicDrawMappingConfigurationService mappingConfiguration, IStateMappingRule stateMappingRule, IStereotypeService stereotypeService)
    {
        super(hubController, mappingConfiguration, stereotypeService);
        this.stateMappingRule = stateMappingRule;
    }
    
    /**
     * Transforms an {@linkplain MagicDrawBlockCollection} of type {@linkplain Class} to an {@linkplain ArrayList} of {@linkplain ElementDefinition}
     * 
     * @param input the {@linkplain ObservableCollection} of type {@linkplain Element} to transform
     * @return the {@linkplain ArrayList} of {@linkplain MappedElementDefinitionRowViewModel}
     */
    @Override
    public ArrayList<MappedElementDefinitionRowViewModel> Transform(Object input)
    {
        try
        {
            this.elements = this.CastInput(input);
            this.Map(this.elements);
            this.SaveMappingConfiguration(this.elements);
            return new ArrayList<>(this.elements);
        }
        catch (Exception exception)
        {
            this.logger.catching(exception);
            return new ArrayList<>();
        }
        finally
        {
            this.elements.clear();
            this.portsToConnect.clear();
            this.connectedElements.clear();
            this.binaryRelationShips.clear();
            this.stateMappingRule.Clear();
        }
    }
    
    /**
     * Saves the mapping configuration
     * 
     * @param elements the {@linkplain MagicDrawBlockCollection}
     */
    private void SaveMappingConfiguration(MagicDrawBlockCollection elements)
    {
        for (MappedElementDefinitionRowViewModel mappedElement : elements)
        {
            this.mappingConfiguration.AddToExternalIdentifierMap(
                    mappedElement.GetHubElement().getIid(), mappedElement.GetDstElement().getID(), MappingDirection.FromDstToHub);
        }
    }

    /**
     * Maps the provided collection of block
     * 
     * @param mappedElementDefinitions the collection of {@linkplain Class} or block to map
     */
    private void Map(MagicDrawBlockCollection mappedElementDefinitions)
    {        
        for (MappedElementDefinitionRowViewModel mappedElement : new ArrayList<MappedElementDefinitionRowViewModel>(mappedElementDefinitions))
        {
            if(mappedElement.GetHubElement() == null)
            {
                mappedElement.SetHubElement(this.GetOrCreateElementDefinition(mappedElement.GetDstElement()));
            }
            
            this.MapCategories(mappedElement.GetHubElement(), mappedElement.GetDstElement());
            this.MapProperties(mappedElement.GetHubElement(), mappedElement.GetDstElement(), null);
        }
        
        this.MapPorts();
        this.ProcessInterfaces();
        this.ProcessConnectorProperties();
    }
    
    /**
     * Creates the {@linkplain BinaryRelationShip} that connects ports between each others
     * 
     * @implSpec the interface is retrived the following way
     * - for each interface connected
     * - Select the realizations of each interface
     * - filter out the realizations that belongs to the current port owner (block)
     * - select then the interface and the block that owns the realization of the  interface 
     */
    private void ProcessInterfaces()
    {
        for (Triple<Port, MappedElementDefinitionRowViewModel, ElementUsage> portElementUsage : this.portsToConnect)
        {            
            Port port = portElementUsage.getLeft();
            
            if(port.getRequired().isEmpty())
            {
                continue;
            }
            
            List<Pair<Interface, Class>> interfaceRealizationOfInterfaceBlocks = port.getRequired().stream()
                    .flatMap(x -> x.get_interfaceRealizationOfContract().stream())
                    .filter(x ->  !IsOwnedBy(x, port.getOwner()))
                    .map(x -> Pair.of(x.getContract(), (Class)x.getOwner().getOwner()))
                    .collect(Collectors.toList());

            if(interfaceRealizationOfInterfaceBlocks.isEmpty())
            {
                continue;
            }
            
            for (Pair<Interface, Class> interfaceRealizationOfInterfaceBlock : interfaceRealizationOfInterfaceBlocks)
            {
                String realizationOfInterfaceBlockName = interfaceRealizationOfInterfaceBlock.getRight().getName();
                                
                ElementUsage elementUsage = this.portsToConnect.stream()
                        .filter(x -> AreTheseEquals(x.getMiddle().GetDstElement().getName(), realizationOfInterfaceBlockName))
                        .map(x -> x.getRight())
                        .findFirst()
                        .orElse(this.hubController.GetOpenIteration().getElement().stream()
                                .flatMap(x -> x.getContainedElement().stream())
                                .filter(x -> AreTheseEquals(x.getName(), realizationOfInterfaceBlockName))
                        .findFirst()
                        .orElse(null));
                
                if(elementUsage == null)
                {
                    continue;
                }
                
                BinaryRelationship relationship = this.hubController.GetOpenIteration()
                        .getRelationship()
                        .stream()
                        .filter(BinaryRelationship.class::isInstance)
                        .map(BinaryRelationship.class::cast)
                        .filter(x -> AreTheseEquals(interfaceRealizationOfInterfaceBlock.getLeft().getName(), x.getName())
                                && AreTheseEquals(x.getTarget().getIid(), elementUsage.getIid())
                                && AreTheseEquals(x.getTarget().getIid(), portElementUsage.getRight().getIid()))
                        .findFirst()    
                        .map(x -> x.clone(false))
                        .orElseGet(this.CreateBinaryRelationship(interfaceRealizationOfInterfaceBlock.getLeft(), portElementUsage.getRight(), elementUsage));
                
                this.logger.debug(String.format("BinaryRelationShip %s is linking element %s and element %s", relationship.getName(), portElementUsage.getRight().getUserFriendlyName(), elementUsage.getUserFriendlyName()));
                portElementUsage.getMiddle().GetRelationships().add(relationship);
            }
        }
    }

    /**
     * Creates a {@linkplain BinaryRelationship} based on the specified {@linkplain Interface}
     *  
     * @return a {@linkplain Supplier} of {@linkplain Relationship}
     */
    @SuppressWarnings("resource")    
    private Supplier<? extends BinaryRelationship> CreateBinaryRelationship(Interface portInterface, ElementUsage source, ElementUsage target)
    {
        BinaryRelationship relationship = new BinaryRelationship();
        relationship.setIid(UUID.randomUUID());
        relationship.setOwner(this.hubController.GetCurrentDomainOfExpertise());
        relationship.setName(portInterface.getName());
        relationship.setSource(source);
        relationship.setTarget(target);
        
        Ref<Category> refCategory = new Ref<>(Category.class);

        if(this.hubController.TryGetThingFromChainOfRdlBy(x -> AreTheseEquals(x.getName(), this.interfaceCategoryNames.getRight()), refCategory)
                   || this.TryCreateCategory(this.interfaceCategoryNames, refCategory, ClassKind.BinaryRelationship))
        {
            relationship.getCategory().add(refCategory.Get());            
        }

        return () -> relationship;
    }
    
    /**
     * Maps the attached ports of all the mapped {@linkplain Class}
     */
    private void MapPorts()
    {
        for (MappedElementDefinitionRowViewModel mappedElement : this.elements)
        {
            this.MapPorts(mappedElement);
        }
    }
    
    /**
     * Maps the attached ports of the {@linkplain Class} mapped in the specified {@linkplain MappedElementDefinitionRowViewModel}
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void MapPorts(MappedElementDefinitionRowViewModel mappedElement)
    {
        for (Port port : mappedElement.GetDstElement().getOwnedPort())
        {
            String portName = this.GetPortName(mappedElement, port);
            
            if(mappedElement.GetHubElement().getContainedElement().stream()
                    .anyMatch(x -> AreTheseEquals(x.getName(), portName)))
            {
                continue;
            }
            
            ElementUsage elementUsage = new ElementUsage();
            
            elementUsage.setName(portName);
            elementUsage.setShortName(GetShortName(portName));
            elementUsage.setIid(UUID.randomUUID());
            elementUsage.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            elementUsage.setElementDefinition(this.GetPortElementDefinition());
            
            elementUsage.setInterfaceEnd(this.GetInterfaceEndKind(port));
                        
            mappedElement.GetHubElement().getContainedElement().add(elementUsage);
            
            this.portsToConnect.add(Triple.of(port, mappedElement, elementUsage));
        }
    }

    /**
     * Gets the {@linkplain InterfaceEndKind} of the specified {@linkplain Port}
     * 
     * @param port the {@linkplain Port}
     * @return the {@linkplain InterfaceEndKind}
     */
    private InterfaceEndKind GetInterfaceEndKind(Port port)
    {
        if (port.getProvided().isEmpty() && !port.getRequired().isEmpty())
        {
            return InterfaceEndKind.INPUT;
        }
        else if(port.getRequired().isEmpty() && !port.getProvided().isEmpty())
        {
            return InterfaceEndKind.OUTPUT;
        }
        
        return InterfaceEndKind.UNDIRECTED;
    }

    /**
     * Gets the {@linkplain ElementDefinition} that represents all ports 
     * 
     * @return the {@linkplain ElementDefinition} port
     */
    private ElementDefinition GetPortElementDefinition()
    {
        if(this.portElementDefinition != null)
        {
            return this.portElementDefinition;
        }
        
        this.portElementDefinition = this.GetOrCreateElementDefinition(PORTELEMENTDEFINITIONNAME, null);
        return this.portElementDefinition;
    }

    /**
     * Computes the port name
     * 
     * @param mappedElement the {@linkplain MappedElementDefinitionRowViewModel}
     * @param port the {@linkplain Port}
     * @return the port name as a string
     */
    private String GetPortName(MappedElementDefinitionRowViewModel mappedElement, Port port)
    {        
    	Type portType = port.getType();
    	
        if(portType != null)
        {
            return String.format("%s_%s", port.getName(), portType.getName());
        }
        
        long portNumber = mappedElement.GetHubElement().getContainedElement().stream().filter(x -> x.getInterfaceEnd() != InterfaceEndKind.NONE).count();
        
        String nameAfterContainer = String.format("%s_port", mappedElement.GetHubElement().getName());
        
        if(portNumber > 0)
        {
            nameAfterContainer = String.format("%s%s", nameAfterContainer, portNumber);
        }
        
        return  nameAfterContainer;
    }
    
    /**
     * Gets an existing or creates an {@linkplain ElementDefinition} that will be mapped to the {@linkplain Class} 
     * represented in the provided {@linkplain MappedElementDefinitionRowViewModel}
     * 
     * @param dstElement the {@linkplain Class} that will be mapped to the output {@linkplain ElementDefinition}
     * @return an {@linkplain ElementDefinition}
     */
    private ElementDefinition GetOrCreateElementDefinition(Class dstElement)
    {
        return this.GetOrCreateElementDefinition(dstElement.getName(), dstElement.getID());
    }
    
    /**
     * Gets an existing or creates an {@linkplain ElementDefinition} that will be mapped to the {@linkplain Class} 
     * represented in the provided {@linkplain MappedElementDefinitionRowViewModel}
     *
     * @param dstElementName the name of the DST element
     * @param dstElementId the id of the DST element
     * @return an {@linkplain ElementDefinition}
     */
    private ElementDefinition GetOrCreateElementDefinition(String dstElementName, String dstElementId)
    {
        String shortName = GetShortName(dstElementName);
        
        Predicate<ElementDefinition> verifyNames = x -> AreTheseEquals(x.getShortName(), shortName, true) || AreTheseEquals(x.getName(), dstElementName, true);
        
        ElementDefinition elementDefinition = this.elements.stream()
                .filter(x -> x.GetHubElement() != null && verifyNames.test(x.GetHubElement()))
                .map(x -> x.GetHubElement())
                .findFirst()
                .orElse(this.hubController.GetOpenIteration()
                    .getElement()
                    .stream()
                    .filter(x -> verifyNames.test(x))
                    .map(x -> x.clone(true))
                    .findFirst()
                    .orElse(null));
        
        if(elementDefinition == null)
        {
            elementDefinition = new ElementDefinition();
            elementDefinition.setIid(UUID.randomUUID());
            elementDefinition.setName(dstElementName);
            elementDefinition.setShortName(shortName);
            elementDefinition.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            
            if(dstElementId != null)
            {
                Definition definition = new Definition();
                
                definition.setIid(UUID.randomUUID());
                definition.setContent(dstElementId);
                definition.setLanguageCode(MDIID);
                elementDefinition.getDefinition().add(definition);
            }
            
            return elementDefinition;
        }

        return elementDefinition;
    }
    
    /**
     * Creates the {@linkplain BinaryRelationship} specified by {@linkplain connectedElement} 
     */
    private void ProcessConnectorProperties()
    {
        for (Pair<Property, ElementDefinition> element : this.connectedElements)
        {
            Optional<Definition> definition = element.getRight()
                    .getDefinition()
                    .stream()
                    .filter(x-> x.getLanguageCode().equals(MDIID))
                    .findFirst();
            
            if(definition.isPresent())
            {
                Optional<ElementDefinition> otherEnd = this.connectedElements.stream()
                        .filter(x -> ((ElementValue)x.getLeft().getDefaultValue()).getElement().getID()
                                .equalsIgnoreCase(definition.get().getContent()))
                        .map(x -> x.getRight()).findFirst();
                
                if(otherEnd.isPresent())
                {
                    BinaryRelationship relationship = this.hubController.GetOpenIteration()
                            .getRelationship()
                            .stream()
                            .filter(x -> x.getName().equals(this.connectorPropertyNames.getRight())
                                    && x instanceof BinaryRelationship
                                    && VerifyBinaryRelationshipIsTheOne((BinaryRelationship)x, otherEnd.get(), element.getRight()))
                            .findFirst()
                            .map(x -> (BinaryRelationship)x.clone(false))
                            .orElseGet(this.CreateBinaryRelationshipFromConnectorProperty(otherEnd.get(), element.getRight()));
                            
                    this.binaryRelationShips.add(relationship);
                }
            }
        }
    }

    /**
     * Verifies that the relationship is assigned to the specified {@linkplain ElementDefinitions}
     * 
     * @param relationship the {@linkplain BinaryRelationship} to verify
     * @param elementDefinition0 one of the {@linkplain ElementDefinitions} that should be linked to the other
     * @param elementDefinition1 the other {@linkplain ElementDefinitions} that should be linked to the other
     * @return a value indicating whether the provided relationship meets the requirements
     */
    private boolean VerifyBinaryRelationshipIsTheOne(BinaryRelationship relationship, ElementDefinition elementDefinition0, ElementDefinition elementDefinition1)
    {
        return (relationship.getTarget().getIid().compareTo(elementDefinition0.getIid()) == 0
                && relationship.getSource().getIid().compareTo(elementDefinition1.getIid()) == 0)
                || (relationship.getTarget().getIid().compareTo(elementDefinition1.getIid()) == 0
                && relationship.getSource().getIid().compareTo(elementDefinition0.getIid()) == 0);
    }

    /**
     * Creates the relationship for the two specified {@linkplain ElementDefinition}s or verify that the two of them already own such relationship
     * 
     * @param elementDefinition0 one of the {@linkplain ElementDefinition} 
     * @param elementDefinition1 the other {@linkplain ElementDefinition}
     * @return a {@linkplain Supplier} of {@linkplain Relationship}
     */
    private Supplier<? extends BinaryRelationship> CreateBinaryRelationshipFromConnectorProperty(ElementDefinition elementDefinition0, ElementDefinition elementDefinition1)
    {        
        BinaryRelationship relationship = new BinaryRelationship();
        relationship.setIid(UUID.randomUUID());
        relationship.setOwner(this.hubController.GetCurrentDomainOfExpertise());
        relationship.setName(this.connectorPropertyNames.getRight());
        relationship.setSource(elementDefinition0);
        relationship.setTarget(elementDefinition1);
        
        elementDefinition0.getRelationships().add(relationship);
        elementDefinition1.getRelationships().add(relationship);
        
        Ref<Category> refCategory = new Ref<>(Category.class);

        if(this.TryCreateCategory(this.connectorPropertyNames, refCategory, ClassKind.BinaryRelationship))
        {
            relationship.getCategory().add(refCategory.Get());            
        }

        return () -> relationship;
    }

    /**
     * Maps the properties of the specified block
     * 
     * @param elementDefinition the {@linkplain ElementDefinition} that represents the block
     * @param block the source {@linkplain Class} block
     */
    private void MapProperties(ElementDefinition elementDefinition, Class block, Property parentPartProperty)
    {
        HashMap<ConnectorEnd, BinaryRelationship> connectors = new HashMap<>();
        
        for (Property property : block.getOwnedAttribute())
        {
            if (this.stereotypeService.IsPartProperty(property))
            {
                if(parentPartProperty == null || !AreTheseEquals(parentPartProperty.getID(), property.getID()))
                {
                    this.MapPartProperty(elementDefinition, property);
                }
                
                continue;
            }
            
            if (!this.stereotypeService.IsValueProperty(property))
            {
                this.logger.error(String.format("Coulnd map property %s, since it is not a value property", property.getName()));
                continue;
            }
            
            Ref<ParameterType> refParameterType = new Ref<>(ParameterType.class);
            Ref<Parameter> refParameter = new Ref<>(Parameter.class);
            
            if(!this.TryGetOrCreateParameter(elementDefinition, property, refParameter,refParameterType )) 
            {
            	continue;
            }
            
            this.stateMappingRule.MapStateDependencies(refParameter.Get(), property, MappingDirection.FromDstToHub);
            this.ProcessBindingConnectors(connectors, property, refParameter.Get());
            this.UpdateScale(refParameter.Get(), property);
            this.UpdateValueSet(refParameter.Get(), property);

            elementDefinition.getParameter().removeIf(x -> AreTheseEquals(x.getIid(), refParameter.Get().getIid()));
            elementDefinition.getParameter().add(refParameter.Get());
            
            this.binaryRelationShips.addAll(connectors.values());
        }
        
        this.logger.error(String.format("ElementDefinition has %s parameters", elementDefinition.getParameter().size()));
    }

    /**
     * Tries to get or create a {@linkplain Parameter} based on the property
     * @param elementDefinition The {@linkplain ElementDefinition} of the {@linkplain Parameter}
     * @param property The based {@linkplain Property}
     * @param refParameter The {@linkplain Ref<Parameter>}
     * @param refParameterType The {@linkplain Ref<ParameterType>}
     * @return Value indicating if the {@linkplain Parameter} has correctly been get or created
     */
    private boolean TryGetOrCreateParameter(ElementDefinition elementDefinition, Property property, Ref<Parameter> refParameter, Ref<ParameterType> refParameterType) 
    {
        Predicate<Parameter> areParameterParameterTypeShortNameEqualsPredicate = 
                x -> this.AreShortNamesEquals(x.getParameterType(), GetShortName(property)) 
                || x.getParameterType().getName().compareToIgnoreCase(property.getName()) == 0;
        
        Optional<Parameter> existingParameter = elementDefinition.getContainedParameter().stream()
                .filter(areParameterParameterTypeShortNameEqualsPredicate)
                .findAny();

        
        InstanceSpecification appliedStereotype = property.getAppliedStereotypeInstance();
        
        if(appliedStereotype != null && appliedStereotype.getName().toLowerCase().contains("ConnectorProperty")
                && property.getDefaultValue() instanceof ElementValue
                && ((ElementValue)property.getDefaultValue()) != null)
        {
            this.connectedElements.add(Pair.of(property, elementDefinition));
            return false;
        }
        if(!existingParameter.isPresent())
        {
            if(this.TryCreateParameterType(property, refParameterType))
            {
                Parameter newParameter = new Parameter();
                newParameter.setIid(UUID.randomUUID());
                newParameter.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                newParameter.setParameterType(refParameterType.Get());
                refParameter.Set(newParameter);
            }
            else
            {
                this.logger.error(String.format("Could create ParameterType %s", property.getName()));
                return false;
            }
        }   
        else
        {
            refParameter.Set(existingParameter.get().clone(true));
        }
        
        return true;
    }
    
    /**
     * Sets the correct actual scale for the specified {@linkplain Parameter}
     * 
     * @param parameter the {@linkplain Parameter}
     * @param property the {@linkplain Property}
     */
    private void UpdateScale(Parameter parameter, Property property)
    {
        if(parameter.getParameterType() instanceof QuantityKind)
        {
            QuantityKind parameterType = (QuantityKind)parameter.getParameterType();
            Pair<String, String> scaleAndUnit = this.GetScaleAndUnit(property);
            String unitName = scaleAndUnit.getRight();

            this.logger.debug(String.format("Got scaleAnUnit [%s] [%s]", scaleAndUnit.getLeft(), scaleAndUnit.getRight()));
            
            if(unitName == null)
            {
                parameter.setScale(parameterType.getDefaultScale());
            }
            else
            {
                Function<QuantityKind, MeasurementScale> findScaleFunction = x -> x.getPossibleScale().stream()
                    .filter(q -> AreTheseEquals(q.getName(), unitName, true) || AreTheseEquals(q.getShortName(), unitName, true))
                    .findFirst()
                    .orElse(null);
                
                MeasurementScale scale = findScaleFunction.apply(parameterType);
                
                if(scale != null) 
                {
                    this.logger.debug(String.format("Parameter [%s] Scale has changed for [%s]", parameter.getParameterType().getName(), scale.getName()));
                    
                    parameter.setScale(scale);
                }
                else
                {
                    Ref<ParameterType> refParameterType = new Ref<>(ParameterType.class, parameterType);
                    
                    if(this.TryUpdateQuantityKindScale(property, refParameterType))
                    {
                        MeasurementScale newScale = findScaleFunction.apply((QuantityKind)refParameterType.Get());

                        this.logger.debug(String.format("Parameter [%s] Scale has changed for [%s]", 
                                parameter.getParameterType().getName(), newScale.getName()));
                        
                        parameter.setScale(newScale);
                    }
                }
            }
        }        
    }

    /**
     * Maps the specified part property
     * 
     * @param elementDefinition the {@linkplain ElementDefinition} that represents the block
     * @param partProperty the part Property
     */
    private void MapPartProperty(ElementDefinition elementDefinition, Property partProperty)
    {
        if(!(partProperty.getType() instanceof Class))
        {
            this.logger.error(String.format("The Part Property %s is not correctly typed", partProperty.getName()));
            return;
        }
        
        Class definitionBlock = (Class)partProperty.getType();
        
        MappedElementDefinitionRowViewModel mappedElement = this.elements.stream()
                .filter(x -> AreTheseEquals(x.GetDstElement().getID(), definitionBlock.getID()))
                .findFirst()
                .orElseGet(() -> 
                {
                    MappedElementDefinitionRowViewModel element = 
                            new MappedElementDefinitionRowViewModel(this.GetOrCreateElementDefinition(definitionBlock), definitionBlock, MappingDirection.FromDstToHub);
                    
                    this.elements.add(element);                    
                    return element;
                });
        
        this.MapCategories(mappedElement.GetHubElement(), definitionBlock);
        this.MapProperties(mappedElement.GetHubElement(), definitionBlock, partProperty);

        if(elementDefinition.getContainedElement()
                .stream().anyMatch(x -> AreTheseEquals(x.getElementDefinition().getIid(), mappedElement.GetHubElement().getIid())))
        {
            return;
        }
        
        ElementUsage elementUsage = new ElementUsage();
        elementUsage.setName(mappedElement.GetHubElement().getName());
        elementUsage.setShortName(mappedElement.GetHubElement().getShortName());
        elementUsage.setIid(UUID.randomUUID());
        elementUsage.setOwner(this.hubController.GetCurrentDomainOfExpertise());
        elementUsage.setElementDefinition(mappedElement.GetHubElement());
        
        elementDefinition.getContainedElement().add(elementUsage);
    }

    /**
     * Updates the correct value set depending on the selected @Link
     * 
     * @param parameter the {@linkplain Parameter}
     * @param property the {@linkplain Property}
     */
    private void UpdateValueSet(Parameter parameter, Property property)
    {
        if (parameter.getStateDependence() == null)
        {
            this.CreateOrUpdateParameterValueSet(parameter, null);
        }
        else
        {
            for (ActualFiniteState actualFiniteState : parameter.getStateDependence().getActualState())
            {
                this.CreateOrUpdateParameterValueSet(parameter, actualFiniteState);
            }

            if (parameter.getValueSet().stream().noneMatch(x -> x.getActualState() == null))
            {
                ParameterValueSet valueSet = new ParameterValueSet();
                valueSet.setIid(UUID.randomUUID());
                valueSet.setReference(new ValueArray<>(Arrays.asList(""), String.class));
                valueSet.setFormula(new ValueArray<>(Arrays.asList(""), String.class));
                valueSet.setPublished(new ValueArray<>(Arrays.asList(""), String.class));
                valueSet.setComputed(new ValueArray<>(Arrays.asList(""), String.class));
                valueSet.setValueSwitch(ParameterSwitchKind.MANUAL);
                parameter.getValueSet().add(valueSet);
            }
        }
                
        String value = this.stereotypeService.GetValueFromProperty(property);
        
        for (ParameterValueSet valueSet : parameter.getValueSet())
        {
            this.logger.debug(String.format("Parameter [%s] value is being updated with [%s]", parameter.getParameterType().getName(), StringUtils.isBlank(value) ? "-" : value));
            valueSet.setManual(new ValueArray<>(Arrays.asList(StringUtils.isBlank(value) ? "-" : value), String.class));
        }
    }
    
    /**
     * Creates or update a {@linkplain ParameterValueSetBase} for the given {@linkplain Parameter} and {@linkplain ActualFiniteState}
     * 
     * @param parameter the {@linkplain Parameter}
     * @param actualFiniteState the {@linkplain ActualFiniteState}
     */
    private void CreateOrUpdateParameterValueSet(Parameter parameter, ActualFiniteState actualFiniteState)
    {
        if (!((parameter.getOriginal() != null || !parameter.getValueSet().isEmpty()) && actualFiniteState == null))
        {
            ParameterValueSet valueSet = new ParameterValueSet();
            valueSet.setIid(UUID.randomUUID());
            valueSet.setReference(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setFormula(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setPublished(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setComputed(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setValueSwitch(ParameterSwitchKind.MANUAL);
            valueSet.setActualState(actualFiniteState);

            parameter.getValueSet().add(valueSet);
        }
    }
    
    /**
     * Processes the binding connectors for the provided {@linkplain Property}
     * 
     * @param connectors the cached collection of {@linkplain ConnectorEnd} and {@linkplain BinaryRelationship}
     * @param property the current {@linkplain Property}
     * @param parameter the current {@linkplain Parameter}
     */
    private void ProcessBindingConnectors(HashMap<ConnectorEnd, BinaryRelationship> connectors, Property property, Parameter parameter)
    {        
        if(property.has_connectorEndOfPartWithPort())
        {
            for (ConnectorEnd connector : property.get_connectorEndOfPartWithPort())
            {
                if(connectors.containsKey(connector))
                {
                    parameter.getRelationships().add(connectors.get(connector));
                    connectors.get(connector).setTarget(parameter);
                }
                else
                {
                    BinaryRelationship binaryRelationship = new BinaryRelationship();
                    binaryRelationship.setIid(UUID.randomUUID());
                    binaryRelationship.setName(this.bindingConnectorNames.getRight());
                    binaryRelationship.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                    binaryRelationship.setSource(parameter);
                    
                    Ref<Category> refCategory = new Ref<>(Category.class);
                    
                    if(this.TryCreateCategory(this.bindingConnectorNames, refCategory, ClassKind.BinaryRelationship, ClassKind.BinaryRelationshipRule))
                    {
                        binaryRelationship.getCategory().add(refCategory.Get());
                    }
                    
                    parameter.getRelationships().add(binaryRelationship);
                    connectors.put(connector, binaryRelationship);
                }
            }                
        }
    }

    /**
     * Tries to create a {@linkplain ParameterType} if it doesn't exist yet in the chain of rdls
     * 
     * @param property the {@linkplain Property} to create the {@linkplain ParameterType} from
     * @param refParameterType the {@linkplain Ref} of {@linkplain ParameterType} 
     * @return a value indicating whether the {@linkplain ParameterType} has been successfully created or retrieved from the chain of rdls
     */
    private boolean TryCreateParameterType(Property property, Ref<ParameterType> refParameterType)
    {
        try
        {
            String shortName = GetShortName(property.getName());
            
            if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> this.AreShortNamesEquals(x, shortName) 
                    || x.getName().compareToIgnoreCase(property.getName()) == 0 
                    || (property.getType() != null && AreTheseEquals(property.getType().getName(), x.getName(), true)), refParameterType))
            {
                ParameterType parameterType = this.CreateParameterType(property);
                                
                if(parameterType != null)
                {
                    parameterType.setIid(UUID.randomUUID());
                    parameterType.setName(property.getName());
                    parameterType.setShortName(shortName);
                    parameterType.setSymbol(property.getName().substring(0, 1));
                    
                    ReferenceDataLibrary referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
                    referenceDataLibrary.getParameterType().add(parameterType);
                    return this.TryCreateOrUpdateReferenceDataLibraryThing(parameterType, referenceDataLibrary, refParameterType);
                }
            }
            else
            {
                return this.TryUpdateQuantityKindScale(property, refParameterType);
            }
            
            return refParameterType.HasValue();
        }
        catch(Exception exception)
        {
            this.logger.error(String.format("Could not create the parameter type with the name: %s, because %s", property.getName(), exception));
            this.logger.catching(exception);
            return false;
        }
    }

	/**
	 * Create a {@linkplain ParameterType} based on the {@linkplain Property}
	 * 
	 * @param property The {@linkplain Property}
	 * @return The created {@linkplain ParameterType}
	 */
	private ParameterType CreateParameterType(Property property)
	{
		ParameterType parameterType = null;
		ValueSpecification valueSpecification = property.getDefaultValue();

		if(valueSpecification == null || valueSpecification instanceof LiteralInteger || valueSpecification instanceof LiteralUnlimitedNatural || valueSpecification instanceof LiteralReal)
		{
		    parameterType = this.CreateQuantityKind(property);
		}                
		if(parameterType == null && (valueSpecification == null || valueSpecification instanceof LiteralBoolean))
		{
		    parameterType = new BooleanParameterType();
		}
		if(parameterType == null && valueSpecification instanceof LiteralString)
		{
		    parameterType = new TextParameterType();
		}
		else if(valueSpecification != null)
		{
		    this.logger.error(String.format("The property %s isn't supported by the adapter because it is %s or %s", property.getName(), valueSpecification.getHumanType(), valueSpecification.getHumanName()));
		}
		return parameterType;
	}
    
    /**
     * Tries to update the provided parameter based on the specified {@linkplain Property} valu type if it is a {@linkplain QuantityKind}
     * 
     * @param property the {@linkplain Property} to create the {@linkplain ParameterType} from
     * @param refParameterType the {@linkplain Ref} of {@linkplain ParameterType} 
     * @return an assert
     */
    private boolean TryUpdateQuantityKindScale(Property property, Ref<ParameterType> refParameterType)
    { 
        if(!(refParameterType.Get() instanceof QuantityKind))
        {
            return true;
        }
        
        QuantityKind quantityKind = ((QuantityKind)refParameterType.Get());
        String scaleName = this.GetScaleName(property);
        
        if(quantityKind.getPossibleScale().stream().noneMatch(x -> AreTheseEquals(scaleName, x.getName(), true)
                    || AreTheseEquals(scaleName, x.getShortName(), true)))
        {
            Ref<MeasurementScale> refScale = new Ref<>(MeasurementScale.class);
            
            if(!this.TryCreateOrGetMeasurementScale(property.getDefaultValue(), property, refScale))
            {
                this.logger.error(String.format("Could not map the property %s because no measurement scale could be found or created", property.getName()));
                return false;
            }
            
            QuantityKind clone = quantityKind.clone(false);
            clone.getPossibleScale().add(refScale.Get());

            ReferenceDataLibrary referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
            return this.TryCreateOrUpdateReferenceDataLibraryThing(clone, referenceDataLibrary, refParameterType);
        }
        
        return true;
    }

    /**
     * Creates a parameter type base on the measurement scale and unit
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain QuantityKind}
     */
    private QuantityKind CreateQuantityKind(Property property)
    {
        Ref<MeasurementScale> refScale = new Ref<>(MeasurementScale.class);
        
        if(!this.TryCreateOrGetMeasurementScale(property.getDefaultValue(), property, refScale))
        {
            this.logger.error(String.format("Could not map the property %s because no measurement scale could be found or created", property.getName()));
            return null;
        }

        SimpleQuantityKind quantityKind = new SimpleQuantityKind();
        
        quantityKind.getAllPossibleScale().add(refScale.Get());
        quantityKind.setDefaultScale(refScale.Get());
        return quantityKind;
    }

    /**
     * Tries to create a new {@linkplain MeasurementScale} based on the provided {@linkplain valueSpecification}
     * 
     * @param valueSpecification the {@linkplain ValueSpecification}
     * @param property the typed property
     * @param refScale the {@linkplain Ref} of {@linkplain MeasurementScale} as out parameter
     * @return a {@linkplain boolean} indicating whether the {@linkplain refScale} is not null
     */
    @SuppressWarnings("resource")
    private boolean TryCreateOrGetMeasurementScale(ValueSpecification valueSpecification, Property property, Ref<MeasurementScale> refScale)
    {
        String scaleName = this.GetScaleName(property);
        
        if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> AreTheseEquals(x.getShortName(), scaleName, true) 
                ||  AreTheseEquals(x.getName(), scaleName, true) , refScale))
        {
            MeasurementScale newScale = new RatioScale();
            newScale.setName(scaleName);
            
            if(valueSpecification instanceof LiteralInteger)
            {
                newScale.setNumberSet(NumberSetKind.INTEGER_NUMBER_SET);
            }
            else if(valueSpecification instanceof LiteralUnlimitedNatural)
            {
                newScale.setNumberSet(NumberSetKind.NATURAL_NUMBER_SET);                            
            }
            else
            {
                newScale.setNumberSet(NumberSetKind.REAL_NUMBER_SET);                            
            }
            
            Ref<MeasurementUnit> refMeasurementUnit = new Ref<>(MeasurementUnit.class);
            
            if(!this.TryCreateOrGetMeasurementUnit(scaleName, refMeasurementUnit))
            {   
                return false;
            }
            
            newScale.setUnit(refMeasurementUnit.Get());
            newScale.setShortName(refMeasurementUnit.Get().getShortName());
            
            ReferenceDataLibrary referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
            referenceDataLibrary.getScale().add(newScale);
            return this.TryCreateOrUpdateReferenceDataLibraryThing(newScale, referenceDataLibrary, refScale);
        }
        
        return true;
    }

    /**
     * Gets the possible scale name
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain String}
     */
    private String GetScaleName(Property property)
    {
        Pair<String, String> scaleAndUnit = this.GetScaleAndUnit(property);
        String scaleName = scaleAndUnit.getRight();

        this.logger.debug(String.format("Got scaleAnUnit [%s] [%s]", scaleAndUnit.getLeft(), scaleAndUnit.getRight()));
        
        if(scaleAndUnit.getRight() == null)
        {
            scaleName = "one";
        }
        
        return scaleName;
    }
    
    /**
     * Gets the scale and the unit from the specified property
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain Pair} of {@linkplain Property} where left is the scale and right is the unit
     */
    private Pair<String, String> GetScaleAndUnit(Property property)
    {
        String type = this.stereotypeService.GetTypeRepresentation(property);

        String[] scaleAndUnit = type.replaceAll("\\[|\\]", scaleAndUnitSeparator).split(scaleAndUnitSeparator);
        
        if(scaleAndUnit.length == 1)
        {
            return Pair.of(scaleAndUnit[0], this.stereotypeService.GetUnitRepresention(property));
        }
        else if(scaleAndUnit.length == 2)
        {
            return Pair.of(scaleAndUnit[0], scaleAndUnit[1].replace(scaleAndUnitSeparator, ""));
        }
        
        return Pair.of(null, null);
    }

    /**
     * Tries to create a new {@linkplain MeasurementScale} based on the provided {@linkplain valueSpecification}
     * or to retrieve it from the cache
     * 
     * @param unitName the unit name
     * @param refMeasurementUnit the {@linkplain Ref} of {@linkplain MeasurementUnit} as out parameter
     * @return a {@linkplain boolean} indicating whether the {@linkplain refMeasurementUnit} is not null
     */
    private boolean TryCreateOrGetMeasurementUnit(String unitName, Ref<MeasurementUnit> refMeasurementUnit)
    {
        if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getShortName().equals(unitName) || x.getName().equals(unitName) || x.getShortName().equals("-"), refMeasurementUnit))
        {
            SimpleUnit newMeasurementUnit = new SimpleUnit();
            newMeasurementUnit.setName(unitName);
            newMeasurementUnit.setShortName(unitName);

            ReferenceDataLibrary referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
            referenceDataLibrary.getUnit().add(newMeasurementUnit);
            return this.TryCreateOrUpdateReferenceDataLibraryThing(newMeasurementUnit, referenceDataLibrary, refMeasurementUnit);
        }
        
        return true;
    }

    /**
     * Maps the applied categories to the block to the specified {@linkplain ElementDefinition}
     * 
     * @param elementDefinition the target {@linkplain ElementDefinition}
     * @param block the SysML block ({@linkplain Class}) instance
     */
    private void MapCategories(ElementDefinition elementDefinition, Class block)
    {
        if(elementDefinition == null)
        {
            return;
        }
        
        this.MapCategory(elementDefinition, this.isLeafCategoryNames, block.isLeaf(), false);
        this.MapCategory(elementDefinition, this.isAbstractCategoryNames, block.isAbstract(), true);
        this.MapCategory(elementDefinition, this.isActiveCategoryNames, block.isActive(), false);
        this.MapCategory(elementDefinition, this.isEncapsulatedCategoryNames, this.stereotypeService.IsEncapsulated(block), true);
              
        for (Stereotype stereotype : this.stereotypeService.GetAllStereotype(block))
        {
            this.MapCategory(elementDefinition, stereotype.getName(), ClassKind.ElementDefinition, ClassKind.ElementUsage);
        }
    }

    /**
     * Maps the specified by short name {@linkplain Category} to the provided {@linkplain ElementDefinition}
     * 
     * @param elementDefinition the {@linkplain ElementDefinition} to update
     * @param categoryNames the {@linkplain Pair} of short name and name
     * @param value the {@linkplain Boolean} value from the current SysML block
     * @param shouldCreateTheCategory a value indicating whether the {@linkplain Category} should be created if it doesn't exist yet
     */
    private void MapCategory(ElementDefinition elementDefinition, Pair<String, String> categoryNames, Boolean value, boolean shouldCreateTheCategory)
    {
        try
        {         
            Ref<Category> refCategory = new Ref<>(Category.class);
    
            if (!(this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getShortName().equals(categoryNames.getLeft()), refCategory)) 
            		&& shouldCreateTheCategory && !this.TryCreateCategory(categoryNames, refCategory, ClassKind.ElementDefinition, ClassKind.ElementUsage))
			{
			    return;
			}
    
            if(refCategory.HasValue())
            {
                if(Boolean.TRUE.equals(value))
                {
                    elementDefinition.getCategory().add(refCategory.Get());
                }
                else
                {
                    elementDefinition.getCategory().remove(refCategory.Get());
                }
            }
            else
            {
                this.logger.debug(String.format("The Category %s could not be found or created for the element %s", categoryNames.getLeft(), elementDefinition.getName()));
            }
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
        }
    }
}
