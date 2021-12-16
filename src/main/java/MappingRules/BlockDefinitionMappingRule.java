/*
 * BlockDefinitionMappingRule.java
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.nomagic.magicdraw.sysml.util.MDCustomizationForSysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralNull;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;

import App.AppContainer;
import DstController.IDstController;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Services.MappingConfiguration.IMagicDrawMappingConfigurationService;
import Services.MappingEngineService.MappingRule;
import Utils.Ref;
import Utils.ValueSetUtils;
import Utils.Stereotypes.MagicDrawBlockCollection;
import static Utils.Stereotypes.StereotypeUtils.IsOwnedBy;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Definition;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.ElementUsage;
import cdp4common.engineeringmodeldata.InterfaceEndKind;
import cdp4common.engineeringmodeldata.Parameter;
import cdp4common.engineeringmodeldata.ParameterSwitchKind;
import cdp4common.engineeringmodeldata.ParameterValueSet;
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
import cdp4common.types.ContainerList;
import cdp4common.types.ValueArray;
import cdp4dal.operations.ThingTransaction;
import cdp4dal.operations.ThingTransactionImpl;
import cdp4dal.operations.TransactionContextResolver;

/**
 * The {@linkplain BlockDefinitionMappingRule} is the mapping rule implementation for transforming {@linkplain Element} to {@linkplain ElementDefinition}
 */
public class BlockDefinitionMappingRule extends MappingRule<MagicDrawBlockCollection, ArrayList<MappedElementDefinitionRowViewModel>>
{
    /**
     * The string that indicates the language code for the {@linkplain Definition} for {@linkplain ElementDefinition}s
     * That contains the MagicDraw Id of the mapped {@linkplain Block}
     */
    private static final String MDIID = "MDIID";    

    /**
     * The string that specifies the {@linkplain ElementDefinition} representing ports
     */
    private static final String PORTELEMENTDEFINITIONNAME = "Port";
    
    /**
     * The {@linkplain IHubController}
     */
    private IHubController hubController;

    /**
     * The {@linkplain IMagicDrawMappingConfigurationService} instance
     */
    private IMagicDrawMappingConfigurationService mappingConfiguration;
    
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
     * Initializes a new {@linkplain BlockDefinitionMappingRule}
     * 
     * @param HubController the {@linkplain IHubController}
     * @param mappingConfiguration the {@linkplain IMagicDrawMappingConfigurationService}
     */
    public BlockDefinitionMappingRule(IHubController hubController, IMagicDrawMappingConfigurationService mappingConfiguration)
    {
        this.hubController = hubController;
        this.mappingConfiguration = mappingConfiguration;
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
            this.Logger.catching(exception);
            return new ArrayList<>();
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
            this.MapPorts(mappedElement);
        }
        
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
                
                this.Logger.debug(String.format("BinaryRelationShip %s is linking element %s and element %s", relationship.getName(), portElementUsage.getRight().getUserFriendlyName(), elementUsage.getUserFriendlyName()));
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
            elementUsage.setShortName(this.GetShortName(portName));
            elementUsage.setIid(UUID.randomUUID());
            elementUsage.setOwner(this.hubController.GetCurrentDomainOfExpertise());
            elementUsage.setElementDefinition(this.GetPortElementDefinition());
            elementUsage.setInterfaceEnd(InterfaceEndKind.UNDIRECTED);
                        
            mappedElement.GetHubElement().getContainedElement().add(elementUsage);
            
            this.portsToConnect.add(Triple.of(port, mappedElement, elementUsage));
        }
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
        if(!StringUtils.isBlank(port.getName()))
        {
            return port.getName();
        }
        
        if(port.getType() != null)
        {
            String shortName = AppContainer.Container.getComponent(IDstController.class)
                    .GetProjectElements().stream()
                    .flatMap(x -> x.getOwnedElement().stream())
                    .filter(p -> MDCustomizationForSysMLProfile.isPartProperty(p)
                                && AreTheseEquals(((Property)p).getType().getID(), port.getOwner().getID()))
                    .map(x -> ((Property)x).getName())
                    .findFirst()
                    .orElse(mappedElement.GetHubElement().getName());
            
            return String.format("%s_%s", shortName, port.getType().getName());
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
        String shortName = this.GetShortName(dstElementName);
        
        ElementDefinition elementDefinition = this.elements.stream()
                .filter(x -> x.GetHubElement() != null && AreTheseEquals(x.GetHubElement().getShortName(), shortName))
                .map(x -> x.GetHubElement())
                .findFirst()
                .orElse(this.hubController.GetOpenIteration()
                    .getElement()
                    .stream()
                    .filter(x -> AreTheseEquals(x.getShortName(), shortName))
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

        return elementDefinition.clone(true);
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
        Hashtable<ConnectorEnd, BinaryRelationship> connectors = new Hashtable<>();
        
        for (Property property : block.getOwnedAttribute())
        {
            if (MDCustomizationForSysMLProfile.isPartProperty(property))
            {
                if(parentPartProperty != null && AreTheseEquals(parentPartProperty.getID(), property.getID()))
                {
                    continue;
                }
                    
                this.MapPartProperty(elementDefinition, property);
                continue;
            }
            
            Optional<Parameter> existingParameter = elementDefinition.getContainedParameter().stream()
                    .filter(x -> this.AreShortNamesEquals(x.getParameterType(), property.getName()))
                    .findAny();

            Ref<ParameterType> refParameterType = new Ref<>(ParameterType.class);
            Parameter parameter = null;
            
            Ref<String> refValue = new Ref<>(String.class, "");
            boolean hasValue = this.TryGetValueFromProperty(property, refValue);
            
            if(property.getAppliedStereotypeInstance() != null && property.getAppliedStereotypeInstance().getName().toLowerCase().contains("ConnectorProperty")
                    && property.getDefaultValue() instanceof ElementValue
                    && ((ElementValue)property.getDefaultValue()) != null)
            {
                this.connectedElements.add(Pair.of(property, elementDefinition));
                continue;
            }
            
            if(!existingParameter.isPresent() && hasValue)
            {
                if(this.TryCreateParameterType(property, refParameterType))
                {
                    parameter = new Parameter();
                    parameter.setIid(UUID.randomUUID());
                    parameter.setOwner(this.hubController.GetCurrentDomainOfExpertise());
                    parameter.setParameterType(refParameterType.Get());

                    elementDefinition.getParameter().add(parameter);
                    
                    if(refParameterType.Get() instanceof QuantityKind)
                    {
                        parameter.setScale(((QuantityKind)refParameterType.Get()).getDefaultScale());
                    }
                }
                else
                {
                    this.Logger.error(String.format("Coulnd create ParameterType %s", property.getName()));
                    continue;
                }
            }   
            else if (existingParameter.isPresent() && hasValue)
            {
                parameter = existingParameter.get().clone(true);
            }
            else
            {
                this.Logger.error(String.format("Could not map attribute %s for element definition %s", property.getName(), elementDefinition.getName()));
                continue;
            }

            this.ProcessBindingConnectors(connectors, property, parameter);
            this.UpdateValueSet(parameter, refValue);

            this.binaryRelationShips.addAll(Collections.list(connectors.elements()));
        }
        
        this.Logger.error(String.format("ElementDefinition has %s parameters", elementDefinition.getParameter().size()));
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
            this.Logger.error(String.format("The Part Property %s is not correctly typed", partProperty.getName()));
            return;
        }
        
        Class definitionBlock = (Class)partProperty.getType();
        
        MappedElementDefinitionRowViewModel mappedElement = this.elements.stream()
                .filter(x -> AreTheseEquals(x.GetDstElement().getID(), definitionBlock.getID()))
                .findFirst()
                .orElseGet(() -> 
                {
                    MappedElementDefinitionRowViewModel element = 
                            new MappedElementDefinitionRowViewModel(definitionBlock, MappingDirection.FromDstToHub);
                    
                    this.elements.add(element);                    
                    return element;
        
                });
        
        if(mappedElement.GetHubElement() == null)
        {
            mappedElement.SetHubElement(this.GetOrCreateElementDefinition(definitionBlock));
        }
        else
        {
            mappedElement.SetHubElement(mappedElement.GetHubElement().clone(true));
        }

        this.MapProperties(mappedElement.GetHubElement(), definitionBlock, partProperty);
        

        if(elementDefinition.getContainedElement()
                .stream().anyMatch(x -> AreTheseEquals(x.getName(), mappedElement.GetHubElement().getName())))
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
     * @param refValue the {@linkplain Ref} of {@linkplain String} holding the value to assign
     */
    private void UpdateValueSet(Parameter parameter, Ref<String> refValue)
    {
        ParameterValueSet valueSet = null;
        
        if(parameter.getOriginal() != null || !parameter.getValueSet().isEmpty())
        {
            valueSet = (ParameterValueSet) ValueSetUtils.QueryParameterBaseValueSet(parameter, null, null);    
        }
        else
        {
            valueSet = new ParameterValueSet();
            valueSet.setIid(UUID.randomUUID());
            valueSet.setReference(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setFormula(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setPublished(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setComputed(new ValueArray<>(Arrays.asList(""), String.class));
            valueSet.setValueSwitch(ParameterSwitchKind.MANUAL);
            parameter.getValueSet().add(valueSet);
        }

        valueSet.setManual(new ValueArray<>(Arrays.asList(refValue.Get()), String.class));
    }

    /**
     * Processes the binding connectors for the provided {@linkplain Property}
     * 
     * @param connectors the cached collection of {@linkplain ConnectorEnd} and {@linkplain BinaryRelationship}
     * @param property the current {@linkplain Property}
     * @param parameter the current {@linkplain Parameter}
     */
    private void ProcessBindingConnectors(Hashtable<ConnectorEnd, BinaryRelationship> connectors, Property property, Parameter parameter)
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
     * Tries to extract the value from the provided property and returns it as string
     * 
     * @return a value indicating whether the value has been extracted
     */
    private boolean TryGetValueFromProperty(Property property, Ref<String> refValue)
    {
        ValueSpecification valueSpecification = property.getDefaultValue();
        
        if(valueSpecification instanceof LiteralInteger)
        {
            refValue.Set(String.valueOf(((LiteralInteger)property.getDefaultValue()).getValue()));
        }
        else if(valueSpecification instanceof LiteralUnlimitedNatural)
        {
            refValue.Set(String.valueOf(((LiteralUnlimitedNatural)property.getDefaultValue()).getValue()));
        }
        else if(valueSpecification instanceof LiteralReal)
        {
            refValue.Set(String.valueOf(((LiteralReal)property.getDefaultValue()).getValue()));
        }
        else if(valueSpecification instanceof LiteralString)
        {
            refValue.Set(((LiteralString)property.getDefaultValue()).getValue());
        }
        else if(valueSpecification instanceof LiteralBoolean)
        {
            refValue.Set(String.valueOf(((LiteralBoolean)property.getDefaultValue()).isValue()));
        }
        
        return refValue.HasValue();
    }

    /**
     * Tries to create a {@linkplain ParameterType} if it doesn't exist yet in the chain of rdls
     * 
     * @param property the {@linkplain Property} to create the {@linkplain ParameterType} from
     * @param refParameterType the {@linkplain Ref} of {@linkplain ParameterType} 
     * @return a value indicating whether the {@linkplain ParameterType} has been successfully created or retrieved from the chain of rdls
     */
    @SuppressWarnings("resource")
    private boolean TryCreateParameterType(Property property, Ref<ParameterType> refParameterType)
    {
        try
        {
            String shortName = this.GetShortName(property.getName());
            
            if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> this.AreShortNamesEquals(x, shortName), refParameterType))
            {
                ParameterType parameterType = null;
                ValueSpecification valueSpecification = property.getDefaultValue();
                
                if(valueSpecification instanceof LiteralInteger || valueSpecification instanceof LiteralUnlimitedNatural || valueSpecification instanceof LiteralReal)
                {
                    parameterType = new SimpleQuantityKind();

                    SimpleQuantityKind quantityKind = ((SimpleQuantityKind)parameterType);
                    
                    Ref<MeasurementScale> refScale = new Ref<>(MeasurementScale.class);
                    
                    if(!this.TryCreateOrGetMeasurementScale(valueSpecification, property, refScale))
                    {
                        this.Logger.error(String.format("Could not map the property %s because no measurement scale could be found or created", shortName));
                        return false;
                    }
                    
                    quantityKind.getAllPossibleScale().add(refScale.Get());
                    quantityKind.setDefaultScale(refScale.Get());
                }
                
                else if(valueSpecification instanceof LiteralBoolean)
                {
                    parameterType = new BooleanParameterType();
                }

                else if(valueSpecification instanceof LiteralString)
                {
                    parameterType = new TextParameterType();
                }
                
                if(!(valueSpecification instanceof LiteralSpecification) || valueSpecification instanceof LiteralNull)
                {
                    this.Logger.error(String.format("The property %s value type isn't supported by the adapter because its not a LiteralSpecification ? %s or it is a LiteralNull ? %s", 
                            property.getName(), !(valueSpecification instanceof LiteralSpecification), valueSpecification instanceof LiteralNull));
                }
                                
                if(parameterType != null)
                {
                    parameterType.setIid(UUID.randomUUID());
                    parameterType.setName(property.getName());
                    parameterType.setShortName(shortName);
                    parameterType.setSymbol(shortName.substring(0, 1));
                    
                    ReferenceDataLibrary referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
                    referenceDataLibrary.getParameterType().add(parameterType);
                    return this.TryCreateReferenceDataLibraryThing(parameterType, referenceDataLibrary, refParameterType);
                }
                
                return false;
            }
            
            return true;
        }
        catch(Exception exception)
        {
            this.Logger.error(String.format("Could not create the parameter type with the shortname: %s, because %s", property.getName(), exception));
            this.Logger.catching(exception);
            return false;
        }
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
        Pair<Property, Property> scaleAndUnit = this.GetScaleAndUnit(property); 
        
        if(scaleAndUnit.getLeft() == null || scaleAndUnit.getRight() == null)
        {
            return false;
        }
        
        String scaleShortName = this.GetShortName(scaleAndUnit.getLeft().getName());
        
        if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getShortName().equals(scaleShortName), refScale))
        {
            MeasurementScale newScale = new RatioScale();
            newScale.setName(scaleAndUnit.getLeft().getName());
            
            if(valueSpecification instanceof LiteralInteger)
            {
                newScale.setNumberSet(NumberSetKind.INTEGER_NUMBER_SET);
            }
            else if(valueSpecification instanceof LiteralUnlimitedNatural)
            {
                newScale.setNumberSet(NumberSetKind.NATURAL_NUMBER_SET);                            
            }
            else if(valueSpecification instanceof LiteralReal)
            {
                newScale.setNumberSet(NumberSetKind.REAL_NUMBER_SET);                            
            }
            
            Ref<MeasurementUnit> refMeasurementUnit = new Ref<>(MeasurementUnit.class);
            
            if(!this.TryCreateOrGetMeasurementUnit(valueSpecification, scaleAndUnit.getRight().getName(), refMeasurementUnit))
            {   
                return false;
            }
            
            newScale.setUnit(refMeasurementUnit.Get());
            newScale.setShortName(refMeasurementUnit.Get().getShortName());
            
            ReferenceDataLibrary referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
            referenceDataLibrary.getScale().add(newScale);
            return this.TryCreateReferenceDataLibraryThing(newScale, referenceDataLibrary, refScale);
        }
        
        return true;
    }
    
    /**
     * Gets the scale and the unit from the specified property
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain Pair} of {@linkplain Property} where left is the scale and right is the unit
     */
    private Pair<Property, Property> GetScaleAndUnit(Property property)
    {
        Type dataType = property.getDatatype() != null ? property.getDatatype() : property.getType();
        
        if(dataType != null)
        {
            Property quantityKind = StereotypesHelper.findStereotypePropertyFor(dataType, "quantityKind");
            Property scale = null;
            
            if(quantityKind != null)
            {
                scale = StereotypesHelper.findStereotypePropertyFor(quantityKind, "scale");
            }
            
            Property unit = StereotypesHelper.findStereotypePropertyFor(dataType, "Unit");
            
            return Pair.of(scale, unit);
        }
        
        return Pair.of(null, null);
    }

    /**
     * Tries to create a new {@linkplain MeasurementScale} based on the provided {@linkplain valueSpecification}
     * or to retrieve it from the cache
     * 
     * @param valueSpecification the {@linkplain ValueSpecification}
     * @param unitName the unit name
     * @param refMeasurementUnit the {@linkplain Ref} of {@linkplain MeasurementUnit} as out parameter
     * @return a {@linkplain boolean} indicating whether the {@linkplain refMeasurementUnit} is not null
     */
    private boolean TryCreateOrGetMeasurementUnit(ValueSpecification valueSpecification, String unitName, Ref<MeasurementUnit> refMeasurementUnit)
    {
        String unitShortName = this.GetShortName(unitName);
        
        if(!this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getShortName().equals(unitShortName) || x.getName().equals(unitName), refMeasurementUnit))
        {
            SimpleUnit newMeasurementUnit = new SimpleUnit();
            newMeasurementUnit.setName(unitName);
            newMeasurementUnit.setShortName(unitShortName);

            ReferenceDataLibrary referenceDataLibrary = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
            referenceDataLibrary.getUnit().add(newMeasurementUnit);
            return this.TryCreateReferenceDataLibraryThing(newMeasurementUnit, referenceDataLibrary, refMeasurementUnit);
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
        
        Boolean isEncapsulated = null;

        Optional<String> optionalIsEncapsulated = 
                StereotypesHelper.getStereotypePropertyValueAsString(block, 
                        block.getAppliedStereotypeInstance().getName(), this.isEncapsulatedCategoryNames.getRight())
                .stream().findFirst();
                
        if(optionalIsEncapsulated.isPresent())
        {
            isEncapsulated = Boolean.valueOf(optionalIsEncapsulated.get());
        }
        
        this.MapCategory(elementDefinition, this.isLeafCategoryNames, block.isLeaf(), false);
        this.MapCategory(elementDefinition, this.isAbstractCategoryNames, block.isAbstract(), true);
        this.MapCategory(elementDefinition, this.isActiveCategoryNames, block.isActive(), false);
        this.MapCategory(elementDefinition, this.isEncapsulatedCategoryNames, isEncapsulated, true);
        
        this.Logger.error(String.format("ElementDefinition has %s Categories", elementDefinition.getCategory().size()));
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
    
            if(!(this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getShortName().equals(categoryNames.getLeft()), refCategory)))
            {
                if (shouldCreateTheCategory && !this.TryCreateCategory(categoryNames, refCategory, ClassKind.ElementDefinition, ClassKind.ElementUsage))
                {
                    return;
                }
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
                this.Logger.debug(String.format("The Category %s could not be found or created for the element %s", categoryNames.getLeft(), elementDefinition.getName()));
            }
        }
        catch(Exception exception)
        {
            this.Logger.catching(exception);
        }
    }
    
    /**
     * Tries to create the category with the specified {@linkplain categoryShortName}
     * 
     * @param categoryNames the {@linkplain Pair} of short name and name
     * @param refCategory the {@linkplain Ref} of Category
     * @param params of permissive classes
     * 
     * @return a value indicating whether the category has been successfully created and retrieved from the cache
     */
    private boolean TryCreateCategory(Pair<String, String> categoryNames, Ref<Category> refCategory, ClassKind... permissibleClass)
    {
        Category newCategory = new Category();
        newCategory.setName(categoryNames.getRight());
        newCategory.setShortName(categoryNames.getLeft());
        newCategory.setIid(UUID.randomUUID());
        newCategory.getPermissibleClass().addAll(Arrays.asList(permissibleClass)); 

        ReferenceDataLibrary rdl = this.hubController.GetDehpOrModelReferenceDataLibrary().clone(false);
        rdl.getDefinedCategory().add(newCategory);
        
        return TryCreateReferenceDataLibraryThing(newCategory, rdl, refCategory);        
    }

    /**
     * Tries to add the specified {@linkplain newThing} to the provided {@linkplain ContainerList} and retrieved the new reference from the cache after save
     * 
     * @param <TThing> the type of {@linkplain Thing}
     * @param newThing the new {@linkplain Thing}
     * @param clonedReferenceDataLibrary the cloned {@linkplain ReferenceDataLibrary} where the {@linkplain newThing} is contained
     * @param refThing the {@linkplain Ref} acting as an out parameter here
     * @return a value indicating whether the {@linkplain newThing} has been successfully created and retrieved from the cache
     */
    private <TThing extends Thing> boolean TryCreateReferenceDataLibraryThing(TThing newThing, ReferenceDataLibrary clonedReferenceDataLibrary, Ref<TThing> refThing)
    {
        try
        {
            ThingTransaction transaction = new ThingTransactionImpl(TransactionContextResolver.resolveContext(clonedReferenceDataLibrary), clonedReferenceDataLibrary);
            transaction.createOrUpdate(clonedReferenceDataLibrary);
            transaction.createOrUpdate(newThing);
            
            this.hubController.Write(transaction);
            this.hubController.RefreshReferenceDataLibrary(clonedReferenceDataLibrary);
            
            return this.hubController.TryGetThingFromChainOfRdlBy(x -> x.getIid().compareTo(newThing.getIid()) == 0, refThing);
        }
        catch(Exception exception)
        {
            this.Logger.error(String.format("Could not create the %s because %s", newThing.getClassKind(), exception));
            this.Logger.catching(exception);
            return false;
        }       
    }
}
