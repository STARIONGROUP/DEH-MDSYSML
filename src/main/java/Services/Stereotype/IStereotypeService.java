/*
 * IStereotypeService.java
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
package Services.Stereotype;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import java.util.Collection;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import Utils.Stereotypes.Stereotypes;
import cdp4common.sitedirectorydata.CategorizableThing;

/**
 * The {@linkplain IStereotypeService} is the interface definition for the {@linkplain StereotypeService}
 */
public interface IStereotypeService
{
    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain Stereotype} {@linkplain String} name
     * 
     * @param stereotype the {@linkplain Stereotype}
     * @return a {@linkplain Stereotype}
     */
    Stereotype GetStereotype(Enum<?> stereotype);

    /**
     * Sets the stereotype property value of the provided {@linkplain Element} with the provided {@linkplain Object} value
     *
     * @param element the stereotyped {@linkplain Element}
     * @param stereotype the {@linkplain Stereotypes} that applies to the {@linkplain Element} and which also defines the provided provided property name
     * @param propertyName the property name
     * @param value the value
     */
    void SetStereotypePropertyValue(Element element, Stereotypes stereotype, String propertyName, Object value);

    /**
     * Applies the {@linkplain Stereotype} specified by the provided {@linkplain Stereotypes} to the provided {@linkplain Element} 
     * 
     * @param element the {@linkplain Element}
     * @param stereotype the {@linkplain Stereotypes}
     */
    void ApplyStereotype(Element element, Stereotypes stereotype);

    /**
     * Gets a value indicating whether the provided {@linkplain Element} has the stereotype property isEncapsulated and returns the value
     * 
     * @param element the {@linkplain Element}
     * @return a {@linkplain boolean}
     */
    boolean IsEncapsulated(Element element);

    /**
     * Gets a value indicating whether the provided {@linkplain Property}
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain boolean}
     */
    boolean IsValueProperty(Property property);

    /**
     * Gets a value indicating whether the provided {@linkplain Property}
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain boolean}
     */
    boolean IsPartProperty(Property property);
    
    /**
     * Gets a value indicating whether the provided {@linkplain Property} is a reference property
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain boolean}
     */
	boolean IsReferenceProperty(Property property);

    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain Stereotype} {@linkplain String} name
     * 
     * @param namedElement the {@linkplain Element}
     * @param stereotype the {@linkplain Stereotype}
     * @return a {@linkplain Stereotype}
     */
    Stereotype GetStereotype(Element namedElement, Enum<?> stereotype);

    /**
     * Gets the {@linkplain Stereotype} that is applied to the provided property
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain Stereotype}
     */
    Stereotype GetPropertyStereotype(Property property);

    /**
     * Gets the {@linkplain Unit} as string
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain String}
     */
    String GetUnitRepresention(Property property);

    /**
     * Gets the type of the provided value as string
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain String}
     */
    String GetTypeRepresentation(Property property);

    /**
     * Gets the default value from the specified {@linkplain Property} as string
     * 
     * @param property the {@linkplain Property}
     * @return a {@linkplain String}
     */
    String GetValueFromProperty(Property property);

    /**
     * Gets value representation string out of the specified {@linkplain LiteralNumericValue}
     * 
     * @param value the {@linkplain LiteralNumericValue}
     * @return a {@linkplain String}
     */
    String GetValueRepresentation(Property property);

    /**
     * Verifies that the provided {@linkplain Element} has the specified stereotype
     * 
     * @param stereotype the expected {@linkplain Stereotypes}
     * @param element the {@linkplain Element} to test
     * @return a value indicating whether the {@linkplain Element} has the specified stereotype
     */
    boolean DoesItHaveTheStereotype(Element element, Stereotypes stereotype);

    /**
     * Gets the requirement text out of the provided SysML requirement
     * 
     * @param requirement the {@linkplain Element} requirement
     * @return a {@linkplain String}
     */
    String GetRequirementText(Element requirement);

    /**
     * Gets all the {@linkplain Stereotype} applied to the specified {@linkplain Element}
     * 
     * @param element the {@linkplain Element}
     * @return a {@linkplain Collection} of {@linkplain Stereotype}
     */
    Collection<Stereotype> GetAllStereotype(Element element);

    /**
     * Gets the DataTypes element from the {@linkplain #GetProject()}
     * 
     * @return a {@linkplain Collection} of {@linkplain #DataType}
     */
    Collection<InstanceSpecification> GetUnits();

    /**
     * Gets the DataTypes element from the {@linkplain #GetProject()}
     * 
     * @return a {@linkplain Collection} of {@linkplain #DataType}
     */
    Collection<DataType> GetDataTypes();

    /**
     * Gets the requirement text out of the provided SysML requirement
     * 
     * @param requirement the {@linkplain Class} requirement
     * @return a {@linkplain String}
     */
    String GetRequirementId(Class requirement);

    /**
     * Gets the {@linkplain Stereotype} that corresponds to the specified {@linkplain String}
     * 
     * @param stereotypeName the {@linkplain String} stereotype name
     * @return a {@linkplain Stereotype}
     */
    Stereotype GetStereotype(String stereotypeName);

    /**
     * Applies the provided {@linkplain Stereotype}  to the provided element {@linkplain Class}
     * 
     * @param element the {@linkplain Element}
     * @param stereotype the {@linkplain Class}
     */
    void ApplyStereotype(Element element, Stereotype stereotype);

    /**
     * Applies existing {@linkplain Stereotypes} represented by means of {@linkplain Categories} to the provided element {@linkplain Class}
     * 
     * @param thing the {@linkplain CategorizableThing}
     * @param element the {@linkplain Element}
     */
    void ApplyStereotypesFrom(CategorizableThing thing, Element element);
}
