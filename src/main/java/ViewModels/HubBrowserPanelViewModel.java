/*
 * HubBrowserPanelViewModel.java
 *
 * Copyright (c) 2015-2019 RHEA System S.A.
 *
 * Author: Sam Geren�, Alex Vorobiev, Nathanael Smiechowski 
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

import HubController.IHubController;
import Service.NavigationService.INavigationService;
import ViewModels.Interfaces.IHubBrowserPanelViewModel;
import Views.HubLogin;

/**
 * The {@link HubBrowserPanelViewModel} is the main viewModel for the {@link MDHubBrowserPanel}
 */
public class HubBrowserPanelViewModel implements IHubBrowserPanelViewModel
{
    /**
     * The {@linkplain INavigationService}
     */
    private INavigationService navigationService;    

    /**
     * The {@linkplain IHubController}
     */
    private IHubController hubController;

    /**
     * Initializes a new {@link HubBrowserPanelViewModel}
     * @param navigationService the {@linkplain INavigationService}
     * @param hubController the {@linkplain IHubController}
     */
    public HubBrowserPanelViewModel(INavigationService navigationService, IHubController hubController)
    {
        this.navigationService = navigationService;
        this.hubController = hubController;
    }

    /**
     * Action to be taken when the Connect button is clicked
     * 
     * @return a {@linkplain Boolean} as the dialog result
     */
    @Override
    public Boolean ConnectButtonAction()
    {
        HubLogin view = new HubLogin();
        return this.navigationService.ShowDialog(view);
    }    
}
