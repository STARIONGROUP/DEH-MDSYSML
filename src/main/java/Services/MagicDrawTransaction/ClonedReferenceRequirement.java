/*
 * ClonedReferenceRequirement.java
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

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;

/**
 * The ClonedReferenceElement is a POJO class that represents a cloned requirement with it's original reference
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class ClonedReferenceRequirement extends ClonedReferenceElement<Class>
{
    /**
     * Initializes a new {@linkplain ClonedReferenceElement}
     * 
     * @param original the {@linkplain Class} original reference
     */
    ClonedReferenceRequirement(Class original)
    {
        super(original, SysMLProfile.getInstance(original).getRequirement(), "Id", "Text");
    }
}
