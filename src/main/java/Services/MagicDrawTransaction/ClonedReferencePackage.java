/*
 * ClonedReferencePackage.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import Services.Stereotype.IStereotypeService;

/**
 * The ClonedReferencePackage is a POJO class that represents a cloned {@linkplain Package} with it's original reference
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class ClonedReferencePackage extends ClonedReferenceElement<Package>
{
    /**
     * The {@linkplain HashMap} of existing clones
     */
    private Map<String, ClonedReferenceElement<? extends Element>> existingClones;
    
    /**
     * Backing field for {@linkplain #ContainedElements()}
     */
    private Collection<ClonedReferenceElement<? extends Element>> containedElements = new ArrayList<>();
    
    /**
     * Gets the {@linkplain Collection} of {@linkplain ClonedReferenceElement} contained in the current cloned {@linkplain Package}
     * 
     * @return
     */
    public  Collection<ClonedReferenceElement<? extends Element>> ContainedElements()
    {
        return this.containedElements;
    }
    
    /**
     * Initializes a new {@linkplain ClonedReferencePackage}
     * 
     * @param stereotypeService the {@linkplain IStereotypeService}
     * @param original the {@linkplain Package} original reference
     * @param existingClones the {@linkplain HashMap} of existing clones
     */
    ClonedReferencePackage(IStereotypeService stereotypeService, Package original, Map<String, ClonedReferenceElement<? extends Element>> existingClones)
    {
        super(original, null, stereotypeService); 
        this.existingClones = existingClones;
        this.logger.debug(String.format("Created a clonedReference package for %s", original.getName()));
        this.CloneContainedElements();
    }

    /**
     * Clones recursively the contained element
     */
    private void CloneContainedElements()
    {
        for (Element containedElement : this.GetOriginal().getOwnedElement().stream().collect(Collectors.toList()))
        {            
            ClonedReferenceElement<? extends Element> clonedContainedElement;
            
            if(existingClones.containsKey(containedElement.getID()))
            {
                clonedContainedElement = existingClones.get(containedElement.getID());
            }
            else
            {
                clonedContainedElement = ClonedReferenceElement.Create(containedElement, this.stereotypeService, this.existingClones);
            }
            
            this.containedElements.add(clonedContainedElement);
        }
    }
}
