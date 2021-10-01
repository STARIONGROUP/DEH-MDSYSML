/*
 * BlockDefinitionMappingRuleTestFixture.java
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
package MappingRules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import HubController.IHubController;
import Reactive.ObservableCollection;

class BlockDefinitionMappingRuleTestFixture
{
    private IHubController hubController;
    private BlockDefinitionMappingRule rule;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.rule = new BlockDefinitionMappingRule(this.hubController);
    }

    @Test
    void VerifyTransform()
    {
        assertDoesNotThrow(() -> this.rule.Transform(hubController));
        assertEquals(0, this.rule.Transform(hubController).size());

        Class element0 = mock(Class.class);
        Class element1 = mock(Class.class);
        Class element2 = mock(Class.class);
        ObservableCollection<Class> elements = new ObservableCollection<Class>(Arrays.asList(element0, element1, element2), Class.class);
        
        assertDoesNotThrow(() -> this.rule.Transform(elements));
    }
}
