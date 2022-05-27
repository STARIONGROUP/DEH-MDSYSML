/*
 * DirectedRelationshipType.java
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

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;

import cdp4common.commondata.NamedThing;

import static Utils.Operators.Operators.AreTheseEquals;

/**
 * The {@linkplain DirectedRelationshipType} all the {@linkplain DirectedRelationship} supported by the adapter
 */
public enum DirectedRelationshipType
{
    /**
     * Represents the base concrete class name that can be stereotyped with the next represented types
     */
    Abstraction,
    
    /**
     * Represents the SysML abstract relationship type <code>Trace</code>
     */
    Trace,
    
    /**
     * Represents the concrete relationship type <code>verify</code>, this stereotype has 2 constraints
     * - the supplier must be stereotyped by the stereotype <code>Requirement</code>
     * - the client must be stereotyped by the stereotype <code>TestCase</code>
     */
    Verify,

    /**
     * Represents the concrete relationship type <code>Satisfy</code>, this stereotype has 1 constraint
     * - the supplier must be stereotyped by the stereotype <code>Requirement</code>
     */
    Satisfy,
    
    /**
     * Represents the concrete relationship type <code>Derive</code>, this stereotype has 2 constraints
     * - the supplier must be stereotyped by the stereotype <code>Requirement</code>
     * - the client must be stereotyped by the stereotype <code>Requirement</code>
     */
    DeriveReqt;
    
    /**
     * Initializes a new {@linkplain DirectedRelationshipType}
     * Hides the default public constructor
     */
    private DirectedRelationshipType() { }

    /**
     * Gets the {@linkplain DirectedRelationshipType} instance value that matches the specified {@linkplain NamedThing}
     * 
     * @param namedThing the {@linkplain NamedThing}
     * @return a {@linkplain DirectedRelationshipType}
     */
    public static DirectedRelationshipType From(NamedThing namedThing)
    {
        return From(namedThing.getName());        
    }
    
    /**
     * Gets the {@linkplain DirectedRelationshipType} instance value that matches the specified {@linkplain DirectedRelationship} instnce applied stereotype
     * 
     * @param relationship the {@linkplain DirectedRelationship}
     * @return a {@linkplain DirectedRelationshipType}
     */
    public static DirectedRelationshipType From(DirectedRelationship relationship)
    {
        if(relationship.getAppliedStereotypeInstance() != null)
        {
            return DirectedRelationshipType.From(relationship.getAppliedStereotypeInstance().getName());
        }
        
        return null;
    }
    
    /**
     * Gets the {@linkplain DirectedRelationshipType} instance value that matches the specified {@linkplain String}
     * 
     * @param name the {@linkplain String} name that can match one of these enum values name
     * @return a {@linkplain DirectedRelationshipType}
     */
    public static DirectedRelationshipType From(String name)
    {
        for (DirectedRelationshipType directedRelationshipType : DirectedRelationshipType.values())
        {
            if(AreTheseEquals(directedRelationshipType.name(), name, true))
            {
                return directedRelationshipType;
            }
        }
        
        return null;
    }
}
    