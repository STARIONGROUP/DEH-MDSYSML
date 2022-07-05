/*
* MagicDrawLocalExchangeHistoryServiceTest.java
*
* Copyright (c) 2020-2022 RHEA System S.A.
*
* Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski, Antoine Théate
*
* This file is part of DEH-CommonJ
*
* The DEH-CommonJ is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 3 of the License, or (at your option) any later version.
*
* The DEH-CommonJ is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program; if not, write to the Free Software Foundation,
* Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/
package Services.HistoryService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import HubController.IHubController;
import Services.AdapterInfo.IAdapterInfoService;
import Services.Stereotype.IStereotypeService;
import cdp4common.ChangeKind;
import cdp4common.sitedirectorydata.DomainOfExpertise;
import cdp4common.sitedirectorydata.Person;

class MagicDrawLocalExchangeHistoryServiceTest
{
    private IHubController hubController;
    private MagicDrawLocalExchangeHistoryService service;
    private IAdapterInfoService adapterInfoService;
    private IStereotypeService stereotypeService;
    private DomainOfExpertise domain;
    private Person person;

    @BeforeEach
    void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.adapterInfoService = mock(IAdapterInfoService.class);
        this.stereotypeService = mock(IStereotypeService.class);
        
        this.domain = new DomainOfExpertise();
        this.person = new Person();
        this.domain.setName("domain");
        this.person.setGivenName("per");
        this.person.setSurname("son");
        
        when(this.hubController.GetCurrentDomainOfExpertise()).thenReturn(this.domain);
        when(this.hubController.GetActivePerson()).thenReturn(this.person);
        
        this.service = new MagicDrawLocalExchangeHistoryService(this.hubController, this.adapterInfoService, this.stereotypeService);
    }

    @Test
    void VerifyAppend()
    {
        Class block0 = mock(Class.class);
        when(block0.getName()).thenReturn("block");
        Class block1 = mock(Class.class);
        when(block1.getName()).thenReturn("block");
        Property clonedProperty = mock(Property.class);
        Property originalProperty = mock(Property.class);
        when(originalProperty.eContainer()).thenReturn(block1);
        when(block1.eContainer()).thenReturn(block0);
        
        assertDoesNotThrow(() -> this.service.Append(clonedProperty, originalProperty));
        assertDoesNotThrow(() -> this.service.Append(clonedProperty, ChangeKind.NONE));
        assertDoesNotThrow(() -> this.service.Append(block1, ChangeKind.NONE));
        
        Abstraction relationship = mock(Abstraction.class);
        when(relationship.getSource()).thenReturn(Arrays.asList(block0));
        
        assertDoesNotThrow(() -> this.service.Append(relationship, ChangeKind.CREATE));
        when(relationship.getTarget()).thenReturn(Arrays.asList(originalProperty));
        assertDoesNotThrow(() -> this.service.Append(relationship, ChangeKind.UPDATE));
    }
}
