/*
 * MagicDrawLocalExchangeHistoryService.java
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

import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import HubController.IHubController;
import Services.LocalExchangeHistory.ILocalExchangeHistoryService;
import Services.LocalExchangeHistory.LocalExchangeHistoryService;
import Services.VersionNumber.IAdapterVersionNumberService;
import Utils.Stereotypes.DirectedRelationshipType;
import Utils.Stereotypes.StereotypeUtils;
import cdp4common.ChangeKind;

/**
 * The {@linkplain MagicDrawLocalExchangeHistoryService} is the MagicDraw Adpater implementation of {@linkplain ILocalExchangeHistoryService}
 */
public class MagicDrawLocalExchangeHistoryService extends LocalExchangeHistoryService implements IMagicDrawLocalExchangeHistoryService
{
    /**
     * Initializes a new {@linkplain MagicDrawLocalExchangeHistoryService}
     * 
     * @param hubController the {@linkplain IHubController}
     * @param versionService the {@linkplain IAdapterVersionNumberService}
     */
    public MagicDrawLocalExchangeHistoryService(IHubController hubController, IAdapterVersionNumberService versionService)
    {
        super(hubController, versionService);
    }

    /**
     * Appends a change in the log regarding the specified {@linkplain DirectedRelationship}
     * 
     * @param relationship the {@linkplain DirectedRelationship}
     * @param changeKind the {@linkplain ChangeKind}
     */
    @Override
    public void Append(Abstraction relationship, ChangeKind changeKind)
    {
        DirectedRelationshipType type = DirectedRelationshipType.From(relationship);
        String elementType = type != null ? type.name() : relationship.getClass().getSimpleName().replace("Impl", "");

        this.Append(String.format("%s [%s] has been %sD", elementType, relationship.getName(), changeKind));
    }
    
    /**
     * Appends a change in the log regarding the specified {@linkplain CapellaElement}
     * 
     * @param element the {@linkplain CapellaElement}
     * @param changeKind the {@linkplain ChangeKind}
     */
    @Override
    public void Append(NamedElement element, ChangeKind changeKind)
    {
        String modelCode = element.eContainer() instanceof NamedElement 
                ? String.format("%s.%s", ((NamedElement)element.eContainer()).getName(), element.getName())
                        : element.getName();

        String elementType = element.getClass().getSimpleName().replace("Impl", "");

        this.Append(String.format("%s [%s] has been %sD", elementType, modelCode, changeKind));
    }

    /**
     * Appends a change in the log regarding the specified {@linkplain Property}s
     * 
     * @param clonedProperty the new {@linkplain Property}
     * @param originalProperty the old {@linkplain Property}
     */
    @Override
    public void Append(Property clonedProperty, Property originalProperty)
    {
        String valueToUpdateString = StereotypeUtils.GetValueRepresentation(originalProperty);
        String newValueString = StereotypeUtils.GetValueRepresentation(clonedProperty);
        
        String propertyName = String.format("%s.%s", 
                originalProperty.eContainer() instanceof NamedElement 
                    ? ((NamedElement) originalProperty.eContainer()).getName() 
                    : "", originalProperty.getName());
        
        this.Append(String.format("Value: [%s] from Property [%s] has been updated to [%s]", valueToUpdateString, propertyName, newValueString));
    }
}
