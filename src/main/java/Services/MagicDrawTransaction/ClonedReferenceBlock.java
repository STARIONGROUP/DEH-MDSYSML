/*
 * ClonedReferenceBlock.java
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

import java.util.stream.Collectors;

import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import Services.Stereotype.IStereotypeService;
import Utils.Stereotypes.StereotypeUtils;

/**
 * The ClonedReferenceElement is a POJO class that represents a cloned Block with it's original reference
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class ClonedReferenceBlock extends ClonedReferenceElement<Class>
{    
    /**
     * Initializes a new {@linkplain ClonedReferenceElement}
     * 
     * @param stereotypeService the {@linkplain IStereotypeService}
     * @param original the {@linkplain Class} original reference
     */
    ClonedReferenceBlock(IStereotypeService stereotypeService, Class original)
    {
        super(original, SysMLProfile.getInstance(original).getBlock(), stereotypeService);
        this.CloneProperties();
    }

    /**
     * Clones each property of the block
     */
    private void CloneProperties()
    {
        for (Property property : this.GetOriginal().getOwnedAttribute().stream().collect(Collectors.toList()))
        {
            Stereotype stereotype = this.stereotypeService.GetPropertyStereotype(property);
            Property propertyFromClone = this.GetClone().getOwnedAttribute().stream().filter(x -> AreTheseEquals(x.getID(), property.getID())).findFirst().orElse(null);
            
            if(stereotype == null || propertyFromClone == null)
            {
                this.logger.debug(String.format("Could not clone the property %s because stereotype %s or the cloned property %s was not found", 
                        property.getName(), stereotype != null ? stereotype.getName() : "NULL", propertyFromClone != null ? propertyFromClone.getName() : "NULL"));
                
                continue;
            }
            
            Property clonedProperty = propertyFromClone;
            StereotypesHelper.addStereotype(clonedProperty, stereotype);
            clonedProperty.setType(property.getType());
        }
    }
}
