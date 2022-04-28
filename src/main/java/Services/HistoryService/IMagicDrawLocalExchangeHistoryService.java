/*
 * IMagicDrawLocalExchangeHistoryService.java
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
package Services.HistoryService;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import Services.LocalExchangeHistory.ILocalExchangeHistoryService;
import cdp4common.ChangeKind;

/**
* The {@linkplain ICapellaLocalExchangeHistoryService} is the interface definition for the {@linkplain CapellaLocalExchangeHistoryService}
*/
public interface IMagicDrawLocalExchangeHistoryService extends ILocalExchangeHistoryService
{
   /**
    * Appends a change in the log regarding the specified {@linkplain NamedElement}
    * 
    * @param element the {@linkplain NamedElement}
    * @param changeKind the {@linkplain ChangeKind}
    */
   void Append(NamedElement element, ChangeKind changeKind);

   /**
    * Appends a change in the log regarding the specified {@linkplain Property}s
    * 
    * @param clonedProperty the new {@linkplain Property}
    * @param originalProperty the old {@linkplain Property}
    */
   void Append(Property clonedProperty, Property originalProperty);
}
