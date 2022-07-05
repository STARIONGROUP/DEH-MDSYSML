/*
 * ICloneReferenceService.java
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
package Services.MagicDrawTransaction.Clones;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import Services.Stereotype.IStereotypeService;
import Utils.Stereotypes.Stereotypes;

/**
 * The {@linkplain ICloneReferenceService} is the interface definition service for the {@linkplain CloneReferenceService}
 */
public interface ICloneReferenceService
{

    /**
     * Clears the collection of {@linkplain ClonedReferenceElement} this service manages
     */
    void Reset();

    /**
     * Verifies that the provided {@linkplain #TElement} is a clone
     * 
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    <TElement extends Element> boolean IsCloned(TElement element);

    /**
     * Initializes a new {@linkplain ClonedReferenceElement} based on the {@linkplain #T} stereotype
     * 
     * @param <T> the type of the {@linkplain Element}
     * @param original the {@linkplain Element} original
     * @param stereotypeService the {@linkplain IStereotypeService}
     * @param existingClones the {@linkplain HashMap} of existing clones
     * @return a {@linkplain ClonedReferenceElement}
     */
    <T extends Element> ClonedReferenceElement<T> Create(T original);

    /**
     * Clones the original and returns the clone or returns the clone if it already exist
     * 
     * @param <TElement> the type of the original {@linkplain Element}
     * @param original the original {@linkplain #TElement}
     * @return a clone of the {@linkplain #original}
     */
    <TElement extends Element> TElement CloneElement(TElement original);

    /**
     * Gets the {@linkplain ClonedReferenceElement} where the element id == the provided {@linkplain #TElement} id
     * 
     * @param <TElement> the type of the element
     * @param element the element
     * @return a {@linkplain ClonedReferenceElement} of type {@linkplain #TElement}
     */
    <TElement extends Element> ClonedReferenceElement<TElement> GetClone(TElement element);

    /**
     * Gets a read only {@linkplain Collection} of the clones reference of type {@linkplain #TElement}
     *  
     * @param stereotype the {@linkplain Stereotypes} type of element 
     * @return a {@linkplain Collection} of {@linkplain ClonedReferenceElement}
     */
    Collection<ClonedReferenceElement<? extends Element>> GetClones(Stereotypes stereotype);

    /**
     * Gets a read only {@linkplain Collection} of the clones reference
     * 
     * @return a {@linkplain Collection} of {@linkplain ClonedReferenceElement}
     */
    Map<String, ClonedReferenceElement<? extends Element>> GetClones();
}
