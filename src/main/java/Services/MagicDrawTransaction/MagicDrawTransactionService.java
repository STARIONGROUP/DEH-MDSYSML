/*
 * MagicDrawTransactionService.java
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
package Services.MagicDrawTransaction;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.ecore.EObject;

import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Usage;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.InterfaceRealization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Region;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;
import com.nomagic.uml2.impl.ElementsFactory;

import App.AppContainer;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Utils.Stereotypes.DirectedRelationshipType;
import Utils.Stereotypes.RequirementType;
import Utils.Stereotypes.StereotypeUtils;
import Utils.Stereotypes.Stereotypes;
import cdp4common.ChangeKind;

/**
 * The MagicDrawTransactionService is a service that takes care of clones and transactions in MagicDraw
 */
public class MagicDrawTransactionService implements IMagicDrawTransactionService
{
    /**
     * The {@linkplain String} that defines the requirement id in the tags value of a MagicDraw requirement
     */
    public static final String RequirementIdStereotypeString = "Id";
    
    /**
     * The {@linkplain String} that defines the requirement id in the tags value of a MagicDraw requirement
     */
    public static final String RequirementTextStereotypeString = "Text";

    /**
     * The name of the package to store mapped parameter types
     */
    private static final String dataPackageName = "Comet DataTypes";
            
    /**
     * The current class logger
     */
    private final Logger logger = LogManager.getLogger();

    /**
     * The {@linkplain IMagicDrawSessionService} 
     */
    private final IMagicDrawSessionService sessionService;
    
    /**
     * Backing field for {@linkplain #GetClones(Class)} and {@linkplain #GetClones()}
     */
    private HashMap<String, ClonedReferenceElement<? extends Element>> cloneReferences = new HashMap<>();
    
    /**
     * Holds the newly created {@linkplain Element} for future reference such as in {@linkplain #IsClonedOrNew(EObject)}, {@linkplain #GetNew(String, Class)}
     */
    private HashMap<String, Element> newReferences = new HashMap<>();

    /**
     * The dictionary of changed {@linkplain Region} for all present {@linkplain State}
     */
    private HashMap<State, List<Pair<Region, ChangeKind>>> stateModifiedRegions = new HashMap<>();
    
    /**
     * The {@linkplain ElementsFactory} that allows to create new SysML element
     */
    private ElementsFactory elementFactory;

    /**
     * The reference to the {@linkplain #dataPackageName}
     */
    private Package dataPackage;
    
    /**
     * Gets a read only {@linkplain Collection} of the clones reference
     * 
     * @return a {@linkplain Collection} of {@linkplain ClonedReferenceElement}
     */    
    @Override
    public Map<String, ClonedReferenceElement<? extends Element>> GetClones()
    {
        return Collections.unmodifiableMap(cloneReferences);
    }
    
    /**
     * Gets a read only {@linkplain Collection} of the clones reference of type {@linkplain #TElement}
     *  
     * @param stereotype the {@linkplain Stereotypes} type of element 
     * @return a {@linkplain Collection} of {@linkplain ClonedReferenceElement}
     */    
    @Override
    public Collection<ClonedReferenceElement<? extends Element>> GetClones(Stereotypes stereotype)
    {
        return Collections.unmodifiableCollection(cloneReferences.values().stream()
                .filter(x -> StereotypesHelper.hasStereotype(x.GetOriginal(), StereotypeUtils.GetStereotype(this.sessionService.GetProject() ,stereotype)))
                .collect(Collectors.toList()));
    }
    
    /**
     * Initializes a new {@linkplain CapellaTransactionService}
     * 
     * @param sessionService the {@linkplain IMagicDrawSessionService}
     */
    public MagicDrawTransactionService(IMagicDrawSessionService sessionService)
    {
        this.sessionService = sessionService;
        
        this.sessionService.HasAnyOpenSessionObservable().subscribe(x -> 
                            this.elementFactory = x.booleanValue() ? this.sessionService.GetProject().getElementsFactory() : null);
    }
    
    /**
     * Gets the {@linkplain ClonedReferenceElement} where the element id == the provided {@linkplain #TElement} id
     * 
     * @param <TElement> the type of the element
     * @param element the element
     * @return a {@linkplain ClonedReferenceElement} of type {@linkplain #TElement}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <TElement extends Element> ClonedReferenceElement<TElement> GetClone(TElement element)
    {
        return (ClonedReferenceElement<TElement>) this.cloneReferences.get(element.getID());
    }

    /**
     * Gets the {@linkplain CapellaElement} where the element id == the provided id
     * 
     * @param <TElement> the type of the element
     * @param id the {@linkplain String} id of the queried element
     * @param elementType the {@linkplain Class} of the queried element
     * @return a {@linkplain ClonedReferenceElement} of type {@linkplain #TElement}
     */    
    @Override
    @SuppressWarnings("unchecked")
    public <TElement extends Element> TElement GetNew(String id, java.lang.Class<TElement> elementType)
    {
        return (TElement) this.newReferences.get(id);
    }
    /**
     * Clones the original and returns the clone or returns the clone if it already exist
     * 
     * @param <TElement> the type of the original {@linkplain Element}
     * @param original the original {@linkplain #TElement}
     * @return a clone of the {@linkplain #original}
     */    
    @Override
    @SuppressWarnings("unchecked")
    public <TElement extends Element> TElement Clone(TElement original)
    {
        if(original == null)
        {
            return null;
        }
        
        if(this.cloneReferences.containsKey(original.getID()))
        {
            return (TElement) this.cloneReferences.get(original.getID()).GetClone();
        }
        
        ClonedReferenceElement<TElement> clonedReference = ClonedReferenceElement.Create(original);
                
        this.cloneReferences.put(original.getID(), clonedReference); 
        return clonedReference.GetClone();
    }
    
    /**
     * Verifies that the provided {@linkplain #TElement} is a clone
     * 
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */    
    @Override
    public <TElement extends Element> boolean IsCloned(TElement element)
    {
        if(!(element instanceof Element))
        {
            return false;
        }
        
        return this.cloneReferences.containsKey(((Element)element).getID()) 
                && this.cloneReferences.get(((Element)element).getID()).GetClone() == element;
    }    

    /**
     *  Verifies that the provided {@linkplain #TElement} is a new element
     *  
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    @Override
    public <TElement extends Element> boolean IsNew(TElement element)
    {
        return this.newReferences.containsKey(((Element)element).getID())
                && this.newReferences.get(((Element)element).getID()) == element;
    }
    
    /**
     * Verifies that the provided {@linkplain #TElement} is a clone or a new element
     * 
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    @Override
    public <TElement extends Element> boolean IsClonedOrNew(TElement element)
    {
        if(!(element instanceof Element))
        {
            return false;
        }
        
        if(this.IsCloned(element)) 
        {
            return true;
        }

        return this.newReferences.containsKey(((Element)element).getID())
                && this.newReferences.get(((Element)element).getID()) == element;
    }

    /**
     * Creates a {@linkplain ValueSpecification}
     * 
     * @param valueType the {@linkplain java.lang.Class} of {@linkplain ValueSpecification}
     * @return the newly created {@linkplain ValueSpecification}
     */
    @Override
    public ValueSpecification Create(java.lang.Class<? extends ValueSpecification> valueType)
    {
        if(valueType == LiteralReal.class)
        {
            return this.elementFactory.createLiteralRealInstance();
        }
        if(valueType == LiteralBoolean.class)
        {
            return this.elementFactory.createLiteralBooleanInstance();
        }
        if(valueType == LiteralString.class)
        {
            return this.elementFactory.createLiteralStringInstance();
        }
        
        return null;
    }    

    /**
     * Creates a new {@linkplain Abstraction} relationship based on the provided {@linkplain DirectedRelationshipType} stereotype
     * 
     * @param relationshipType the {@linkplain DirectedRelationshipType}
     * @return an {@linkplain Abstraction}
     */
    @Override
    public Abstraction Create(DirectedRelationshipType relationshipType)
    {
        Abstraction relationship = this.Create(
                StereotypeUtils.GetStereotype(this.sessionService.GetProject(), relationshipType), 
                () -> this.elementFactory.createAbstractionInstance());
        
        this.newReferences.put(relationship.getID(), relationship);
        return relationship;
    }
        
    /**
     * Initializes a new {@linkplain Class} from the specified {@linkplain #Class}
     * 
     * @param <TInstance> the {@linkplain Type} of {@linkplain Element}
     * @param <TEnum> the type of {@linkplain Enum} of the enumeration value
     * @param enumerationValue the {@linkplain #TEnum} that represents a stereotype to apply to the newly created {@linkplain Class}
     * @param name the name of the newly created {@linkplain Class}, used to query the {@linkplain #newReferences} collection
     * @return an instance of a {@linkplain Class}
     */
    @SuppressWarnings("unchecked")
    private <TEnum extends Enum<?>, TInstance extends NamedElement> TInstance CreateFromEnum(TEnum enumerationValue, String name)
    {        
        TInstance newElement = null;
        
        if(enumerationValue instanceof RequirementType)
        {
            newElement = (TInstance) this.Create((RequirementType)enumerationValue);
        }
        else if(enumerationValue instanceof Stereotypes)
        {
            newElement = (TInstance) this.Create((Stereotypes)enumerationValue);
        }
        
        if(newElement != null)
        {
            newElement.setName(name);
        }
        
        return newElement;
    }

    /**
     * Initializes a new {@linkplain Class} from the specified {@linkplain RequirementType}
     * 
     * @param stereotype the {@linkplain RequirementType} stereotype to apply to the newly created {@linkplain Class}
     * @param name the name of the newly created {@linkplain Class}, used to query the {@linkplain #newReferences} collection
     * @return an instance of a {@linkplain Class}
     */    
    @Override
    public Class Create(RequirementType stereotype, String name)
    {
        return this.CreateFromEnum(stereotype, name);
    }
    
    /**
     * Initializes a new {@linkplain Class} from the specified {@linkplain Stereotypes}
     * 
     * @param stereotype the {@linkplain Stereotypes} stereotype to apply to the newly created {@linkplain Class}
     * @param name the name of the newly created {@linkplain Class}, used to query the {@linkplain #newReferences} collection
     * @return an instance of a {@linkplain Class}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <TElement extends Element> TElement Create(Stereotypes stereotype, String name)
    {
        return (TElement) this.CreateFromEnum(stereotype, name);
    }
    
    /**
     * Initializes a new {@linkplain Class} from the specified {@linkplain Stereotypes}
     * 
     * @param <TElement> the type of element to return
     * @param stereotype the {@linkplain Stereotypes} stereotype to apply to the newly created {@linkplain Class}
     * @return an instance of a {@linkplain TElement}
     */
    @Override
    public <TElement extends Element> TElement Create(Stereotypes stereotype)
    {
        Supplier<TElement> createFunction = this.GetCreateFunctionFromElementFactory(stereotype);
        
        if(createFunction == null)
        {
            return null;
        }
        
        return this.Create(StereotypeUtils.GetStereotype(this.sessionService.GetProject(), stereotype), createFunction);
    }

    /**
     * Gets the create function depending on the provided {@linkplain Stereotypes}
     * 
     * @param <TElement> the type of element the {@linkplain Supplier} returns
     * @param stereotype the {@linkplain Stereotypes} to create
     * @return a {@linkplain Supplier} of {@linkplain #TElement}
     */
    @SuppressWarnings("unchecked")
    private <TElement extends Element> Supplier<TElement> GetCreateFunctionFromElementFactory(Stereotypes stereotype)
    {
        switch (stereotype)
        {
            case ReferenceProperty:
            case PartProperty:
            case ValueProperty: return () -> (TElement)this.elementFactory.createPropertyInstance();
            case Requirement: 
            case Block: return () -> (TElement)this.elementFactory.createClassInstance();
            case Package: return () ->  (TElement)this.elementFactory.createPackageInstance();
            case PortProperty: return () ->  (TElement)this.elementFactory.createPortInstance();
            case Unit: return () ->  (TElement)this.elementFactory.createInstanceSpecificationInstance();
            case ValueType: return () ->  (TElement)this.elementFactory.createDataTypeInstance();
            default:
            break;
        }
        
        return null;
    }

    /**
     * Initializes a new {@linkplain Class} from the specified {@linkplain RequirementType}
     * 
     * @param requirementType the {@linkplain RequirementType} to apply to the newly created {@linkplain Class}
     * @return an instance of a {@linkplain Class}
     */    
    @Override
    public Class Create(RequirementType requirementType)
    {        
        return this.Create(StereotypeUtils.GetStereotype(this.sessionService.GetProject(), requirementType), () -> this.elementFactory.createClassInstance());
    }   

    /**
     * Creates an {@linkplain #TElement} with the specified name
     * 
     * @param <TElement> the type of element to create
     * @param elementClass the {@linkplain java.lang.Class} of {@linkplain #TElement}
     * @param name the {@linkplain String} name of the new element
     * @return a new instance of a TElement
     */
    @SuppressWarnings("unchecked")
    @Override
    public <TElement extends Element> TElement Create(java.lang.Class<TElement> elementClass, String name)
    {
        NamedElement newElement = null;
        
        if(elementClass == DataType.class)
        {
            newElement = this.elementFactory.createDataTypeInstance();
        }
        else if(elementClass == Package.class)
        {
            newElement = this.elementFactory.createPackageInstance();         
        }
        else if(elementClass == Port.class)
        {
            newElement = this.elementFactory.createPortInstance();         
        }
        else if(elementClass == Interface.class)
        {
            newElement = this.elementFactory.createInterfaceInstance();         
        }
        else if(elementClass == Usage.class)
        {
            newElement = this.elementFactory.createUsageInstance();         
        }
        else if(elementClass == InterfaceRealization.class)
        {
            newElement = this.elementFactory.createInterfaceRealizationInstance();         
        }
        else if(elementClass == Dependency.class)
        {
            newElement = this.elementFactory.createDependencyInstance();         
        }
        else if(elementClass == State.class)
        {
            newElement = this.elementFactory.createStateInstance();         
        }
        else if(elementClass == StateMachine.class)
        {
            newElement = this.elementFactory.createStateMachineInstance();         
        }
        else if(elementClass == Region.class)
        {
            newElement = this.elementFactory.createRegionInstance();         
        }
        
        if(newElement != null)
        {
            newElement.setName(name);
            this.newReferences.put(newElement.getID(), newElement);
            return (TElement)newElement;
        }
        
        return null;
    }
    
    /**
     * Initializes a new {@linkplain Class} from the specified {@linkplain RequirementType}
     * 
     * @param <TElement> the type of {@linkplain Element} to create
     * @param magicDrawStereotype the {@linkplain Stereotype}
     * @param createFunction the {@linkplain Supplier} of {@linkplain #TElement}
     * @return a {@linkplain #TElement}
     */
    private <TElement extends Element> TElement Create(Stereotype magicDrawStereotype, Supplier<TElement> createFunction)
    {
        TElement reference = createFunction.get();
        
        if(magicDrawStereotype != null)
        {
            StereotypesHelper.addStereotype(reference, magicDrawStereotype);
        }
        
        this.newReferences.put(reference.getID(), reference);
        return reference;
    }    
    
    /**
     * Gets the existing or create the {@linkplain Package} that is meant to hold parameter types created from COMET
     * 
     * @return a {@linkplain Package}
     */
    private void SetDataPackage()
    {
        if(this.dataPackage == null)
        {
            this.dataPackage = this.GetOrCreateDataPackage(MagicDrawTransactionService.dataPackageName);            
        }
    }

    /**
     * Gets the existing or create the {@linkplain Package} that is meant to hold parameter types created from COMET
     * 
     * @param dataPackageName the name of the package to get or create
     * @return a {@linkplain Package}
     */
    private Package GetOrCreateDataPackage(String dataPackageName)
    {
        return this.sessionService.GetProject().getPrimaryModel().getOwnedElement().stream()
                                .filter(x -> x instanceof Package)
                                .map(x -> (Package)x)
                                .filter(x -> AreTheseEquals( x.getName(), dataPackageName))
                                .findAny()
                                .orElseGet(() ->
                                {
                                    Package newDataPackage = this.Create(Package.class, dataPackageName);
                                    this.Commit(() -> this.sessionService.GetProject().getPrimaryModel().getOwnedElement().add(newDataPackage));
                                    return newDataPackage;
                                });
    }

    /**
     * Adds the provided {@linkplain DataType} to the {@linkplain DataPackage} of the current project
     * 
     * @param newDataType the new {@linkplain DataType}
     */
    @Override
    public void AddReferenceDataToDataPackage(DataType newDataType)
    {
        this.SetDataPackage();
        this.Commit(() -> this.dataPackage.getOwnedElement().add(newDataType));
    }

    /**
     * Adds the provided {@linkplain InstanceSpecification} to the {@linkplain DataPackage} of the current project
     * 
     * @param newDataType the new {@linkplain InstanceSpecification} unit
     */
    @Override
    public void AddReferenceDataToDataPackage(InstanceSpecification unit)
    {   
        this.SetDataPackage();
        this.Commit(() -> this.dataPackage.getOwnedElement().add(unit));        
    }

    /**
     * Reset the clones references, by means of finalizing the transaction
     */    
    @Override
    public void Finalize()
    {
        this.cloneReferences.clear();
        this.newReferences.clear();
        this.stateModifiedRegions.clear();
    }

    /**
     * Commits the provided transaction
     * 
     * @param transactionMethod the {@linkplain Runnable} to execute inside the transaction
     * @return a value indicating whether the operation succeed
     */    
    @Override
    public boolean Commit(Runnable transactionMethod)
    {
        try
        {
            this.logger.info("Begin commiting transaction to MagicDraw");
            SessionManager.getInstance().createSession(this.sessionService.GetProject(), "DEH SysML adapter transfer from HUB session");
            transactionMethod.run();
            return true;
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
            return false;
        }
        finally
        {
            SessionManager.getInstance().closeSession(this.sessionService.GetProject());
            this.Finalize();
            this.logger.info("End commiting transaction to MagicDraw");
        }
    }

    /**
     * Gets the provided {@linkplain Class} requirement Id
     * 
     * @param requirement the {@linkplain Class} requirement
     * @return a {@linkplain String}
     */
    public static String GetRequirementId(Element requirement)
    {
        return AppContainer.Container.getComponent(IMagicDrawTransactionService.class).GetRequirementId((Class)requirement);
    }
    
    /**
     * Gets the provided {@linkplain Class} requirement Id
     * 
     * @param requirement the {@linkplain Class} requirement
     * @return a {@linkplain String}
     */
    @Override
    public String GetRequirementId(Class requirement)
    {
        return GetStereotypePropertyValue(requirement, this.GetRequirementStereotype(requirement), RequirementIdStereotypeString);
    }

    /**
     * Gets the provided {@linkplain Class} requirement Text
     * 
     * @param requirement the {@linkplain Class} requirement
     * @return a {@linkplain String}
     */
    @Override
    public String GetRequirementText(Class requirement)
    {
        return GetStereotypePropertyValue(requirement, this.GetRequirementStereotype(requirement), RequirementTextStereotypeString);
    }

    /**
     * Get Stereotype property value
     * 
     * @param element the {@linkplain Class} element
     * @param stereotype the {@linkplain Stereotype}
     * @param propertyName the property name
     * @return a {@linkplain String}
     */
    private String GetStereotypePropertyValue(Class element, Stereotype stereotype, String propertyName)
    {
        Optional<String> optionalPropertyValue = 
                StereotypesHelper.getStereotypePropertyValueAsString(element, 
                        stereotype, propertyName)
                .stream().findFirst();
                
        if(optionalPropertyValue.isPresent())
        {
            return optionalPropertyValue.get();
        }
        
        return "";
    }

    /**
     * Gets the requirement {@linkplain Stereotype}
     * 
     * @param requirement the {@linkplain Class} requirement
     * @return a {@linkplain Stereotype}
     */
    private Stereotype GetRequirementStereotype(Class requirement)
    {
        Class originalRequirement = requirement;
        
        if(this.IsCloned(requirement))
        {
            originalRequirement = this.GetClone(requirement).GetOriginal();
        }
        
        return SysMLProfile.getInstance(originalRequirement).getRequirement();
    }

    /** 
     * Sets the provided {@linkplain Class} requirement Id
     * 
     * @param requirement the {@linkplain Class} requirement
     * @param requirementId the {@linkplain String} requirement Id
     */
    @Override
    public void SetRequirementId(Class requirement, String requirementId)
    {
        StereotypesHelper.setStereotypePropertyValue(requirement, this.GetRequirementStereotype(requirement), RequirementIdStereotypeString, requirementId);
    }
    
    /** 
     * Sets the provided {@linkplain Class} requirement Id
     * 
     * @param targetRequirement the target {@linkplain Class} requirement
     * @param sourceRequirement the source {@linkplain Class} requirement
     */
    @Override
    public void SetRequirementId(Class targetRequirement, Class sourceRequirement)
    {
        this.SetRequirementId(targetRequirement, this.GetRequirementId(sourceRequirement));
    }

    /** 
     * Sets the provided {@linkplain Class} requirement Text
     * 
     * @param requirement the {@linkplain Class} requirement
     * @param requirementText the {@linkplain String} requirement Text
     */
    @Override
    public void SetRequirementText(Class requirement, String requirementText)
    {
        StereotypesHelper.setStereotypePropertyValue(requirement, this.GetRequirementStereotype(requirement), RequirementTextStereotypeString, requirementText);
    }
    
    /** 
     * Sets the provided {@linkplain Class} requirement Text
     * 
     * @param targetRequirement the target {@linkplain Class} requirement
     * @param sourceRequirement the source {@linkplain Class} requirement
     */
    @Override
    public void SetRequirementText(Class targetRequirement, Class sourceRequirement)
    {
        this.SetRequirementText(targetRequirement, this.GetRequirementText(sourceRequirement));
    }
    
    /**
     * Gets all the modified {@linkplain Regions} associated to their {@linkplain ChangeKind}
     * 
     * @param state the {@linkplain State}
     * @return a {@linkplain List} of {@linkplain Pair} of {@linkplain Region} and {@linkplain ChangeKind}
     */
    public List<Pair<Region, ChangeKind>> GetModifiedRegions(State state)
    {
        if(this.stateModifiedRegions.getOrDefault(state, null) == null)
        {
            this.stateModifiedRegions.put(state, new ArrayList<>());
        }
        
        return this.stateModifiedRegions.get(state);
    }

    /**
     * Gets all the collection of entries with their state the modified {@linkplain Regions} associated to their {@linkplain ChangeKind}
     * 
     * @return a {@linkplain List} of {@linkplain Pair} of {@linkplain Region} and {@linkplain ChangeKind}
     */
    @Override
    public Set<Entry<State, List<Pair<Region, ChangeKind>>>> GetStatesModifiedRegions()
    {        
        return this.stateModifiedRegions.entrySet();
    }
}
