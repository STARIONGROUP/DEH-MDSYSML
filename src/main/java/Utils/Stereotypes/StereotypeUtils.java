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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import App.AppContainer;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Utils.Ref;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

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
     * Verifies that the provided {@linkplain Element} has the specified stereotype
     * 
     * @param stereotype the expected {@linkplain Stereotypes}
     * @param element the {@linkplain Element} to test
     * @return a value indicating whether the {@linkplain Element} has the specified stereotype
     */
    public static boolean DoesItHaveTheStereotype(Element element, Stereotypes stereotype)
    {
        String elementName = element.getHumanType().toLowerCase().replaceAll("\\s", "");
        return elementName.contains(stereotype.name().toLowerCase());
    }
        
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
      
    /**
     * Gets value representation string out of the specified {@linkplain LiteralNumericValue}
     * 
     * @param value the {@linkplain LiteralNumericValue}
     * @return a {@linkplain String}
     */
    public static String GetValueRepresentation(Property property)
    {         
        String unit = StereotypeUtils.GetUnitRepresention(property);
        
        String valueString = StereotypeUtils.GetValueFromProperty(property);
        
        return String.format("%s%s", valueString, unit == null ? String.format(" %s", StereotypeUtils.GetTypeRepresentation(property)) : String.format(" [%s]", unit));
    }
    
    /**
     * Tries to extract the value from the provided property and returns it as string
     * 
     * @return a value indicating whether the value has been extracted
     */
    public static boolean TryGetValueFromProperty(Property property, Ref<String> refValue)
    {
        refValue.Set(StereotypeUtils.GetValueFromProperty(property));
        return refValue.HasValue();
    }
    
    /**
     * Gets the default value from the specified {@linkplain Property} as string
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain String}
     */
    public static String GetValueFromProperty(Property property)
    {
        ValueSpecification value = property.getDefaultValue();
        String valueString = "";
        
        if(value instanceof LiteralInteger)
        {
            valueString = String.valueOf(((LiteralInteger)value).getValue());
        }
        else if(value instanceof LiteralReal)
        {
            valueString = String.valueOf(((LiteralReal)value).getValue());
        }
        else if(value instanceof LiteralUnlimitedNatural)
        {
            valueString = String.valueOf(((LiteralUnlimitedNatural)value).getValue());
        }
        else if(value instanceof LiteralBoolean)
        {
            valueString = String.valueOf(((LiteralBoolean)value).isValue());
        }
        else if(value instanceof LiteralString)
        {
            valueString = ((LiteralString)value).getValue();
        }
        
        return valueString;
    }
    
    /**
     * Gets the type of the provided value as string
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain String}
     */
    public static String GetTypeRepresentation(Property property)
    {
        Type type = property.getType();
        
        if(type != null)
        {
            return type.getName();
        }
        
        return " ";
    }

    /**
     * Gets the {@linkplain Unit} as string
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain String}
     */
    public static String GetUnitRepresention(Property property)
    {
        if(property.getType() == null)
        {
            return null;
        }
        
        Stereotype stereotype = StereotypeUtils.GetStereotype(property.getType(), "unit");

        if(stereotype == null)
        {
            return null;
        }
        
        List<String> unit = StereotypesHelper.getStereotypePropertyValueAsString(property.getType(), stereotype, "unit");
        
        if(unit != null && !unit.isEmpty())
        {
            return unit.get(0);
        }
        
        return null;
    }

    /**
     * Gets the {@linkplain Stereotype} that is applied to the provided property
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain Stereotype}
     */
    public static Stereotype GetPropertyStereotype(Property property)
    {
        if(StereotypeUtils.DoesItHaveTheStereotype(property, Stereotypes.PartProperty))
        {
            return StereotypeUtils.GetStereotype(property, Stereotypes.PartProperty);
        }
        if(StereotypeUtils.DoesItHaveTheStereotype(property, Stereotypes.ValueProperty))
        {
            return StereotypeUtils.GetStereotype(property, Stereotypes.ValueProperty);
        }
        if(StereotypeUtils.DoesItHaveTheStereotype(property, Stereotypes.ReferenceProperty))
        {
            return StereotypeUtils.GetStereotype(property, Stereotypes.ReferenceProperty);
        }
        
        return null;
    }
    
    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain Stereotype} {@linkplain String} name
     * 
     * @param project the {@linkplain Project}
     * @param stereotype the {@linkplain Stereotype}
     * @return a {@linkplain Stereotype}
     */
    public static Stereotype GetStereotype(Project project, Stereotypes stereotype)
    {
        return StereotypeUtils.GetStereotype(project, stereotype.name());
    }
    
    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain RequirementType} {@linkplain String} name
     * 
     * @param project the {@linkplain Project}
     * @param requirementType the {@linkplain RequirementType}
     * @return a {@linkplain Stereotype}
     */
    public static Stereotype GetStereotype(Project project, RequirementType requirementType)
    {
        return StereotypeUtils.GetStereotype(project, requirementType.name());
    }

    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain Stereotype} {@linkplain String} name
     * 
     * @param namedElement the {@linkplain NamedElement}
     * @param stereotype the {@linkplain Stereotype}
     * @return a {@linkplain Stereotype}
     */
    public static Stereotype GetStereotype(NamedElement namedElement, Stereotypes stereotype)
    {
        return StereotypeUtils.GetStereotype(namedElement, stereotype.name());
    }

    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain RequirementType} {@linkplain String} name
     * 
     * @param namedElement the {@linkplain NamedElement}
     * @param requirementType the {@linkplain RequirementType}
     * @return a {@linkplain Stereotype}
     */
    public static Stereotype GetStereotype(NamedElement namedElement, RequirementType requirementType)
    {
        return StereotypeUtils.GetStereotype(namedElement, requirementType.name());
    }

    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain String}
     * 
     * @param element the {@linkplain Element}
     * @param stereotypeName the {@linkplain String} stereotype name
     * @return a {@linkplain Stereotype}
     */
    private static Stereotype GetStereotype(NamedElement element, String stereotypeName)
    {
        return StereotypesHelper.getAppliedStereotypeByString(element, stereotypeName);
    }
    
    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain String}
     * 
     * @param project the {@linkplain Project}
     * @param stereotypeName the {@linkplain String} stereotype name
     * @return a {@linkplain Stereotype}
     */
    private static Stereotype GetStereotype(Project project, String stereotypeName)
    {
      Collection<Stereotype> allStereoType = StereotypesHelper.getAllStereotypes(project);
      Optional<Stereotype> optionalStereotype = allStereoType.stream().filter(x -> AreTheseEquals(x.getName(), stereotypeName, true)).findFirst();
      
      if(optionalStereotype.isPresent())
      {
          return optionalStereotype.get();
      }
      
      return null;
    }
}
