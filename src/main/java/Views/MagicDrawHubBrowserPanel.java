/*
 * MagicDrawHubBrowserPanel.java
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
package Views;

import Utils.ImageLoader.ImageLoader;
import ViewModels.Interfaces.IHubBrowserPanelViewModel;

/**
 * The {@linkplain MagicDrawHubBrowserPanel} is the {@linkplain HubBrowserPanel} for the MagicDraw / Cameo software
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MagicDrawHubBrowserPanel extends MagicDrawBasePanel<IHubBrowserPanelViewModel, HubBrowserPanel>
{
    /**
     * Initializes a new {@linkplain MagicDrawHubBrowserPanel}
     */
    public MagicDrawHubBrowserPanel()
    {
        super("DEH MagicDraw Adapter - HubBrowserPanel");
        setTabTitle("Hub Browser");
        setFrameIcon(ImageLoader.GetIcon("icon16.png"));
        this.setDefaultCloseAction(CLOSE_ACTION_TO_HIDE);
        this.view = new HubBrowserPanel();
        getRootPane().getContentPane().add(this.view);
    }

    /**
     * Binds the <code>TViewModel viewModel</code> to the implementing view
     * 
     * @param <code>viewModel</code> the view model to bind
     */
    @Override
    public void Bind()
    {
        this.view.GetElementDefinitionBrowser().GetContextMenu().SetDataContext(this.dataContext.GetElementDefinitionBrowserContextMenuViewModel());
        this.view.GetRequirementBrowser().GetContextMenu().SetDataContext(this.dataContext.GetRequirementBrowserContextMenuViewModel());
        this.view.GetSessionControlPanel().SetDataContext(this.dataContext.GetSessionControlViewModel());
        this.view.getHubBrowserHeader().SetDataContext(this.dataContext.GetHubBrowserHeaderViewModel());
        this.view.GetElementDefinitionBrowser().SetDataContext(this.dataContext.GetElementDefinitionBrowserViewModel());
        this.view.GetRequirementBrowser().SetDataContext(this.dataContext.GetRequirementBrowserViewModel());
    }
}
