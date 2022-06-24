/*
 * CloneReferenceService.java
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import Services.Stereotype.IStereotypeService;
import Utils.Stereotypes.Stereotypes;

/**
 * The {@linkplain CloneReferenceService} exposes ways to get any supported element cloned
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class CloneReferenceService implements ICloneReferenceService
{
    /**
     * Backing field for {@linkplain #GetClones(Class)} and {@linkplain #GetClones()}
     */
    private HashMap<String, ClonedReferenceElement<? extends Element>> cloneReferences = new HashMap<>();
    
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
                .filter(x -> this.stereotypeService.DoesItHaveTheStereotype(x.GetOriginal(), stereotype))
                .collect(Collectors.toList()));
    }
    
    /**
     * The {@linkplain IStereotypeService}
     */
    private final IStereotypeService stereotypeService;

    /**
     * Initializes a new {@linkplain CloneReferenceService}
     * 
     * @param stereotypeService the {@linkplain IStereotypeService}
     */
    public CloneReferenceService(IStereotypeService stereotypeService)
    {
        this.stereotypeService = stereotypeService;
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
     * Clones the original and returns the clone or returns the clone if it already exist
     * 
     * @param <TElement> the type of the original {@linkplain Element}
     * @param original the original {@linkplain #TElement}
     * @return a clone of the {@linkplain #original}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <TElement extends Element> TElement CloneElement(TElement original)
    {
        if(original == null)
        {
            return null;
        }
        
        if(this.cloneReferences.containsKey(original.getID()))
        {
            return (TElement) this.cloneReferences.get(original.getID()).GetClone();
        }
        
        ClonedReferenceElement<TElement> clonedReference = this.Create(original);
        this.cloneReferences.put(original.getID(), clonedReference); 
        return clonedReference.GetClone();
    }

    /**
     * Initializes a new {@linkplain ClonedReferenceElement} based on the {@linkplain #T} stereotype
     * 
     * @param <T> the type of the {@linkplain Element}
     * @param original the {@linkplain Element} original
     * @param stereotypeService the {@linkplain IStereotypeService}
     * @param existingClones the {@linkplain HashMap} of existing clones
     * @return a {@linkplain ClonedReferenceElement}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Element> ClonedReferenceElement<T> Create(T original)
    {
        if(this.stereotypeService.DoesItHaveTheStereotype(original, Stereotypes.Requirement))
        {
            return (ClonedReferenceElement<T>) new ClonedReferenceRequirement(this.stereotypeService, (Class) original);
        }
        else if(this.stereotypeService.DoesItHaveTheStereotype(original, Stereotypes.Block))
        {
            return (ClonedReferenceElement<T>) new ClonedReferenceBlock(this.stereotypeService, (Class) original);
        }
        else if(original instanceof Package)
        {
            ClonedReferencePackage clonedReferencePackage = new ClonedReferencePackage(this.stereotypeService, (Package) original);
            this.CloneContainedElements(clonedReferencePackage);
            return (ClonedReferenceElement<T>)clonedReferencePackage;
        }
        
        return new ClonedReferenceElement<>(original);
    }
    
    /**
     * Clones recursively the contained element
     * 
     * @param clonedReferencePackage the freshly cloned {@linkplain Package} as {@linkplain ClonedReferencePackage}
     * @param existingClones the {@linkplain HashMap} of existing clones
     */
    private void CloneContainedElements(ClonedReferencePackage clonedReferencePackage)
    {
        for (Element containedElement : clonedReferencePackage.GetOriginal().getOwnedElement().stream().collect(Collectors.toList()))
        {            
            ClonedReferenceElement<? extends Element> clonedContainedElement;
            
            if(this.cloneReferences.containsKey(containedElement.getID()))
            {
                clonedContainedElement = this.cloneReferences.get(containedElement.getID());
            }
            else
            {
                clonedContainedElement = this.Create(containedElement);
            }
                        
            if(clonedContainedElement instanceof ClonedReferencePackage)
            {
                this.cloneReferences.put(clonedContainedElement.GetClone().getID(), clonedContainedElement);
            }
        }
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
     * Clears the collection of {@linkplain ClonedReferenceElement} this service manages
     */
    @Override
    public void Reset()
    {
        this.cloneReferences.clear();
    }
}
