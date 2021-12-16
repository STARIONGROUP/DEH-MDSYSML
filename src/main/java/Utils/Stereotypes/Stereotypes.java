/*
 * Stereotypes.java
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

/**
 * The {@linkplain Stereotype} {@linkplain enum} provides base names for MgicDraw/Cameo stereotypes
 */
public enum Stereotypes
{
    /**
     * The block identifier used in MagicDraw HumanType on {@linkplain Class}
     */
    Block,
    
    /**
     * The requirement identifier used in MagicDraw HumanType on {@linkplain Class}
     */
    Requirement,
    
    /**
     * The package identifier used in MagicDraw for {@linkplain Package}
     */
    Package,
    
    /**
     * The ReferenceProperty represents a reference property, that references other properties
     */
    ReferenceProperty,
    
    /**
     * The ValueProperty represents a value property
     */
    ValueProperty,
    
    /**
     * The PartProperty represents a part property
     */
    PartProperty,
    
    /**
     * The PortProperty represents a port property
     */
    PortProperty
}
