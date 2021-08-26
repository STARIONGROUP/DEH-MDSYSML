/*
 * HubBrowserPanelViewModelTestFixture.java
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
package ViewModels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import HubController.IHubController;
import Service.NavigationService.INavigationService;
import ViewModels.Interfaces.IHubBrowserHeaderViewModel;
import Views.HubLogin;

class HubBrowserPanelViewModelTestFixture
{

    private INavigationService navigationService;
    private IHubController hubController;
    private IHubBrowserHeaderViewModel hubBrowserHeaderViewModel;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception
    {
        this.navigationService = mock(INavigationService.class);
        this.hubController = mock(IHubController.class);
        this.hubBrowserHeaderViewModel = mock(IHubBrowserHeaderViewModel.class);
    }

    @Test
    void VerifyConnectButtonAction()
    {
        HubBrowserPanelViewModel viewModel = new HubBrowserPanelViewModel(this.navigationService, this.hubController, this.hubBrowserHeaderViewModel);
        assertDoesNotThrow(() -> viewModel.Connect());
        verify(this.navigationService, times(1)).ShowDialog(any(HubLogin.class));
    }    
}
