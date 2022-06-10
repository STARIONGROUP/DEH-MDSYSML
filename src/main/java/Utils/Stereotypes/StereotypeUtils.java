/*
 * StereotypeUtils.java
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
package Utils.Stereotypes;

import static Utils.Operators.Operators.AreTheseEquals;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

/**
 * The {@linkplain StereotypeUtils} provides useful methods to verify stereotypes used in MagicDraw/Cameo
 */
public final class StereotypeUtils
{    
    /**
     * Initializes a new {@linkplain StereotypeUtils} and
     * Prevents the {@linkplain StereotypeUtils} to initialized because static classes don't exist out of the box in java
     */
    private StereotypeUtils() { }
    
    /**
     * Verifies the provided {@linkplain Element} element is owned by the provided {@linkplain Element} parent
     * 
     * @param element the {@linkplain Element} to verify
     * @param parent the {@linkplain Element} parent
     * @return a value indicating whether the element is owned by the specified parent
     */
    public static boolean IsOwnedBy(Element element, Element parent)
    {
        if(!parent.hasOwnedElement())
        {
            return false;
        }
        
        for (Element child : parent.getOwnedElement())
        {
            if(AreTheseEquals(child.getID(), element.getID()))
            {
                return true;
            }
            
            if(child.hasOwnedElement() && IsOwnedBy(element, child))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets a 10-25 compliant short name from the provided stereotype name
     * 
     * @param name the {@linkplain String} name to base the short name on
     * @return a {@linkplain string}
     */
    public static String GetShortName(String name)
    {
        return name.replaceAll("[^a-zA-Z0-9]|\\s", "");
    }

    /**
     * Gets a 10-25 compliant short name from the provided stereotype name
     * 
     * @param name the {@linkplain NamedElement} to base the short name on its name
     * @return a {@linkplain string}
     */
    public static String GetShortName(NamedElement name)
    {
        return name.getName().replaceAll("[^a-zA-Z0-9]|\\s", "");
    }
}
