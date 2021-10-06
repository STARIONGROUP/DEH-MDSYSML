/*
 * MapActionTestFixture.java
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
package Actions.Browser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.event.ActionEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.nomagic.magicdraw.tests.MagicDrawTestRunner;
import com.nomagic.magicdraw.ui.browser.Tree;

import DstController.IDstController;
import HubController.IHubController;
import Reactive.ObservableValue;

@RunWith(MagicDrawTestRunner.class)
public class MapActionTestFixture
{
    private IDstController dstController;
    private IHubController hubController;

    @Before
    public void setup()
    {
        this.hubController = mock(IHubController.class);
        this.dstController = mock(IDstController.class);
        ObservableValue<Boolean> isSessionOpen = new ObservableValue<Boolean>(true, Boolean.class);
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(isSessionOpen.Observable());
    }
    
    @Test
    public void VerifyActionPerformed()
    {
        MapAction action = new MapAction(this.hubController, this.dstController);
        action.setTree(new Tree());
        assertDoesNotThrow(() -> action.actionPerformed(any(ActionEvent.class)));
    }
}
