/*
 * IMagicDrawTransactionService.java
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

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Region;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;

import Services.MagicDrawTransaction.Clones.ClonedReferenceElement;
import Utils.Stereotypes.DirectedRelationshipType;
import Utils.Stereotypes.RequirementType;
import Utils.Stereotypes.Stereotypes;
import cdp4common.ChangeKind;

/**
* The {@linkplain IMagicDrawTransactionService} is the interface definition for {@linkplain MagicDrawTransactionService}
*/
public interface IMagicDrawTransactionService
{
    /**
     * Gets all the modified {@linkplain Regions} associated to their {@linkplain ChangeKind}
     * 
     * @param state the {@linkplain State}
     * @return a {@linkplain List} of {@linkplain Pair} of {@linkplain Region} and {@linkplain ChangeKind}
     */
    List<Pair<Region, ChangeKind>> GetModifiedRegions(State state);

    /**
     * Commits the provided transaction
     * 
     * @param transactionMethod the {@linkplain Runnable} to execute inside the transaction
     * @return a value indicating whether the operation succeed
     */
    boolean Commit(Runnable transactionMethod);

    /**
     * Clears the clones references and new references
     */
    void Clear();

    /**
     * Initializes a new {@linkplain Class} from the specified {@linkplain RequirementType}
     * 
     * @param requirementType the {@linkplain RequirementType} to apply to the newly created {@linkplain Class}
     * @return an instance of a {@linkplain Class}
     */
    Class Create(RequirementType requirementType);

    /**
     * Initializes a new {@linkplain Class} from the specified {@linkplain Stereotypes}
     * 
     * @param <TElement> the type of element to return
     * @param stereotype the {@linkplain Stereotypes} stereotype to apply to the newly created {@linkplain Class}
     * @return an instance of a {@linkplain TElement}
     */
    <TElement extends Element> TElement Create(Stereotypes stereotype);

    /**
     * Initializes a new {@linkplain Class} from the specified {@linkplain Stereotypes}
     * 
     * @param stereotype the {@linkplain Stereotypes} stereotype to apply to the newly created {@linkplain Class}
     * @param name the name of the newly created {@linkplain Class}, used to query the {@linkplain #newReferences} collection
     * @return an instance of a {@linkplain Class}
     */
    <TElement extends Element> TElement Create(Stereotypes stereotype, String name);
   
    /**
     * Initializes a new {@linkplain Class} from the specified {@linkplain Stereotypes}
     * 
     * @param elementClass the {@linkplain java.lang.Class} type of the element to create
     * @param name the name of the newly created {@linkplain Class}, used to query the {@linkplain #newReferences} collection
     * @return an instance of a {@linkplain Class}
     */
    <TElement extends Element> TElement Create(java.lang.Class<TElement> elementClass, String name);

    /**
     * Initializes a new {@linkplain Class} from the specified {@linkplain RequirementType}
     * 
     * @param stereotype the {@linkplain RequirementType} stereotype to apply to the newly created {@linkplain Class}
     * @param name the name of the newly created {@linkplain Class}, used to query the {@linkplain #newReferences} collection
     * @return an instance of a {@linkplain Class}
     */
    Class Create(RequirementType stereotype, String name);

    /**
     * Verifies that the provided {@linkplain #TElement} is a clone or a new element
     * 
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    <TElement extends Element> boolean IsClonedOrNew(TElement element);

    /**
     * Verifies that the provided {@linkplain #TElement} is a clone
     * 
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    <TElement extends Element> boolean IsCloned(TElement element);

    /**
     *  Verifies that the provided {@linkplain #TElement} is a new element
     *  
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    <TElement extends Element> boolean IsNew(TElement element);

    /**
     * Clones the original and returns the clone or returns the clone if it already exist
     * 
     * @param <TElement> the type of the original {@linkplain Element}
     * @param original the original {@linkplain #TElement}
     * @return a clone of the {@linkplain #original}
     */
    <TElement extends Element> TElement CloneElement(TElement original);

    /**
     * Gets the {@linkplain CapellaElement} where the element id == the provided id
     * 
     * @param <TElement> the type of the element
     * @param id the {@linkplain String} id of the queried element
     * @param elementType the {@linkplain Class} of the queried element
     * @return a {@linkplain ClonedReferenceElement} of type {@linkplain #TElement}
     */
    <TElement extends Element> TElement GetNew(String id, java.lang.Class<TElement> elementType);

    /**
     * Gets the {@linkplain ClonedReferenceElement} where the element id == the provided {@linkplain #TElement} id
     * 
     * @param <TElement> the type of the element
     * @param element the element
     * @return a {@linkplain ClonedReferenceElement} of type {@linkplain #TElement}
     */
    <TElement extends Element> ClonedReferenceElement<TElement> GetClone(TElement element);

    /**
     * Adds the provided {@linkplain DataType} to the {@linkplain DataPackage} of the current project
     * 
     * @param newDataType the new {@linkplain DataType}
     */
    void AddReferenceDataToDataPackage(DataType newDataType);

    /** 
     * Sets the provided {@linkplain Class} requirement Id
     * 
     * @param requirement the {@linkplain Class} requirement
     * @param requirementId the {@linkplain String} requirement Id
     */
    void SetRequirementId(Class requirement, String requirementId);

    /**
     * Gets the provided {@linkplain Class} requirement Id
     * 
     * @param requirement the {@linkplain Element} requirement
     * @return a {@linkplain String}
     */
    String GetRequirementId(Class requirement);

    /** 
     * Sets the provided {@linkplain Class} requirement Id
     * 
     * @param targetRequirement the target {@linkplain Class} requirement
     * @param sourceRequirement the source {@linkplain Class} requirement
     */
    void SetRequirementId(Class targetRequirement, Class sourceRequirement);

    /** 
     * Sets the provided {@linkplain Class} requirement Text
     * 
     * @param targetRequirement the target {@linkplain Class} requirement
     * @param sourceRequirement the source {@linkplain Class} requirement
     */
    void SetRequirementText(Class targetRequirement, Class sourceRequirement);

    /** 
     * Sets the provided {@linkplain Class} requirement Text
     * 
     * @param requirement the {@linkplain Class} requirement
     * @param requirementText the {@linkplain String} requirement text
     */
    void SetRequirementText(Class requirement, String requirementText);

    /**
     * Gets the provided {@linkplain Class} requirement Text
     * 
     * @param requirement the {@linkplain Class} requirement
     * @return a {@linkplain String}
     */
    String GetRequirementText(Class requirement);

    /**
     * Creates a {@linkplain ValueSpecification}
     * 
     * @param valueType the {@linkplain java.lang.Class} of {@linkplain ValueSpecification}
     * @return the newly created {@linkplain ValueSpecification}
     */
    ValueSpecification Create(java.lang.Class<? extends ValueSpecification> valueType);

    /**
     * Adds the provided {@linkplain InstanceSpecification} to the {@linkplain DataPackage} of the current project
     * 
     * @param newDataType the new {@linkplain InstanceSpecification} unit
     */
    void AddReferenceDataToDataPackage(InstanceSpecification unit);

    /**
     * Creates a new {@linkplain Abstraction} relationship based on the provided {@linkplain DirectedRelationshipType} stereotype
     * 
     * @param relationshipType the {@linkplain DirectedRelationshipType}
     * @return an {@linkplain Abstraction}
     */
    Abstraction Create(DirectedRelationshipType relationshipType);

    /**
     * Gets all the collection of entries with their state the modified {@linkplain Regions} associated to their {@linkplain ChangeKind}
     * 
     * @return a {@linkplain List} of {@linkplain Pair} of {@linkplain Region} and {@linkplain ChangeKind}
     */
    Set<Entry<State, List<Pair<Region, ChangeKind>>>> GetStatesModifiedRegions();

    /**
     * Deletes the specified {@linkplain Element} from the model
     * 
     * @param element the {@linkplain Element}
     */
    void Delete(Element element);
}
