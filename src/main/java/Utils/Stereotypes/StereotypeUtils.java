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

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * The {@linkplain StereotypeUtils} provides useful methods to verify stereotypes used in MagicDraw/Cameo
 */
public final class StereotypeUtils
{
    /**
     * Verifies that the provided {@linkplain Element} has the specified stereotype
     * 
     * @param stereotype the expected {@linkplain Stereotypes}
     * @param element the {@linkplain Element} to test
     * @return a value indicating whether the {@linkplain Element} has the specified stereotype
     */
    public static boolean DoesItHaveTheStereotype(Element element, Stereotypes stereotype)
    {
        return element.getHumanType().toLowerCase().contains(stereotype.name().toLowerCase());
    }
}
