/*
 * RequirementType.java
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

import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * The {@linkplain RequirementType} enumeration enumerates all the SysML requirement types
 */
public enum RequirementType
{
    /**
     * The {@linkplain Requirement} enumeration value represents a basic requirement
     */
    Requirement("Requirement"),

    /**
     * The {@linkplain BusinessRequirement} enumeration value represents a Business Requirement
     */
    BusinessRequirement("Business Requirement"),

    /**
     * The {@linkplain DesignConstraint} enumeration value represents a Design Constraint
     */
    DesignConstraint("Design Constraint"),

    /**
     * The {@linkplain DesignConstraint} enumeration value represents a Extended Requirement
     */
    ExtendedRequirement("Extended Requirement"),

    /**
     * The {@linkplain DesignConstraint} enumeration value represents a Functional Requirement
     */
    FunctionalRequirement("Functional Requirement"),

    /**
     * The {@linkplain DesignConstraint} enumeration value represents a Interface Requirement
     */
    InterfaceRequirement("Interface Requirement"),

    /**
     * The {@linkplain PerformanceRequirement} enumeration value represents a Performance Requirement
     */
    PerformanceRequirement("Performance Requirement"),

    /**
     * The {@linkplain PhysicalRequirement} enumeration value represents a Physical Requirement
     */
    PhysicalRequirement("Physical Requirement"),

    /**
     * The {@linkplain UsabilityRequirement} enumeration value represents a Usability Requirement
     */
    UsabilityRequirement("Usability Requirement");

    /**
     * Gets the label as {@linkplain String} that corresponds to this {@linkplain RequirementType} instance
     */
    private final String label;

    /**
     * Initializes a new {@linkplain RequirementType}
     * 
     * @param label the {@linkplain String} label
     */
    private RequirementType(String label)
    {
        this.label = label;
    }

    /**
     * Returns the name of this Enum constant, as contained in the declaration
     * 
     * @return a {@linkplain String}
     */
    @Override
    public String toString()
    {
        return this.label;
    }

    /**
     * Gets the {@linkplain RequirementType} instance value that matches the specified {@linkplain String} name by {@linkplain #label} or value
     * 
     * @param name the {@linkplain String} name
     * @return a {@linkplain RequirementType}
     */
    public static RequirementType From(String name)
    {
        for (RequirementType requirementType : RequirementType.values())
        {
            if(AreTheseEquals(requirementType.name(), name, true) || AreTheseEquals(requirementType.label, name, true))
            {
                return requirementType;
            }
        }
        
        return null;
    }

    /**
     * Gets the {@linkplain RequirementType} instance value that matches the specified {@linkplain Stereotype} name by {@linkplain #label} or value
     * 
     * @param stereotype the {@linkplain Stereotype}
     * @return a {@linkplain RequirementType}
     */
    public static RequirementType From(Stereotype stereotype)
    {
        return RequirementType.From(stereotype.getName());
    }
}
