/*
 * ClonedReferenceElement.java
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import Services.Stereotype.IStereotypeService;
import Services.Stereotype.StereotypeService;
import Utils.Stereotypes.Stereotypes;

/**
 * The ClonedReferenceElement is a POJO class that represents a cloned element with it's original reference
 * 
 * @param <TElement> the type of the cloned {@linkplain CapellaElement}
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class ClonedReferenceElement<TElement extends Element>
{
    /**
     * The {@linkplain Log4J} logger
     */
    protected Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IStereotypeService}
     */
    protected IStereotypeService stereotypeService;
    
    /**
     *  Backing field for {@linkplain #GetClone()}
     */
    private TElement clone;

    /**
     * Gets the cloned reference to the {@linkplain #TElement}
     * 
     * @return the {@linkplain #TElement}
     */
    public TElement GetClone()
    {
        return this.clone;
    }

    /**
     * Backing field for {@linkplain #GetOriginal()}
     */
    private TElement original;
    
    /**
     * Gets the original reference to the {@linkplain #TElement}
     * 
     * @return the {@linkplain #TElement}
     */
    public TElement GetOriginal()
    {
        return this.original;
    }
    
    private final Map<String, String> slots = new HashMap<>();
    
    /**
     * Holds the stereotype used to clone the stereotype properties
     */
    private Stereotype stereotype;
        
    /**
     * Initializes a new {@linkplain ClonedReferenceElement}
     * 
     * @param original the {@linkplain #TElement} original reference
     */
    ClonedReferenceElement(TElement original) 
    {
        this.original = original;
        this.SetClone();
    }
    
    /**
     * Initializes a new {@linkplain ClonedReferenceElement}
     * 
     * @param original the {@linkplain #TElement} original reference
     * @param stereotype the {@linkplain Stereotype} that applies to the original
     * @param stereotypeService the {@linkplain IStereotypeService}
     * @param stereotypesParameterNames a params of String that list all the stereotype parameter names 
     */
    ClonedReferenceElement(TElement original, Stereotype stereotype, IStereotypeService stereotypeService, String... stereotypesParameterNames) 
    {
        this.original = original;
        this.stereotype = stereotype;
        this.stereotypeService = stereotypeService;
        
        this.GetOriginalStereotypePropertyValues();
        this.SetClone();
    }

    /**
     * Clones the original
     */
    void SetClone()
    {
        this.clone = EcoreUtil.copy(this.original);
        
        if(this.clone != null)
        {
            this.clone.setAppliedStereotypeInstance((InstanceSpecification) new EcoreUtil.Copier().copy(this.original.getAppliedStereotypeInstance()));
        }
        
        if(this.stereotype != null && this.stereotypeService != null)
        {
            this.stereotypeService.ApplyStereotype(this.clone, this.stereotype);
        }
        
        this.CloneOriginalStereotypePropertyValues();
    }

    /**
     * Clones the stereotype properties
     */
    private void CloneOriginalStereotypePropertyValues()
    {
        for (Entry<String, String> slot : this.slots.entrySet().stream().filter(x -> StringUtils.isNotEmpty( x.getValue())).collect(Collectors.toList()))
        {
            StereotypesHelper.setStereotypePropertyValue(this.clone, this.stereotype, slot.getKey(), slot.getValue());
        }
    }

    /**
     * Get the original stereotype property values
     * 
     * @param stereotypesParameterNames a params of String that list all the stereotype parameter names 
     */
    private void GetOriginalStereotypePropertyValues(String... stereotypesParameterNames)
    {
        if(this.original.getAppliedStereotypeInstance() == null)
        {
            return;
        }

        for (String name : stereotypesParameterNames)
        {

            this.slots.put(name, this.GetOriginalPropertyValue(name));
        }
    }

    /**
     * Get the original stereotype property value where the name is the one provided
     * 
     * @param string the {@linkplain String} property name
     * @return a {@linkplain String} value
     */
    private String GetOriginalPropertyValue(String propertyName)
    {
        Optional<String> optionalPropertyValue = 
                StereotypesHelper.getStereotypePropertyValueAsString(this.original, this.stereotype, propertyName)
                .stream().findFirst();
        
        if(optionalPropertyValue.isPresent())
        {
            return optionalPropertyValue.get();
        }
        
        return null;
    }
}
