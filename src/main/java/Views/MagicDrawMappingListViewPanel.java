/*
 * MagicDrawMappingListViewPanel.java
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
import ViewModels.MappingListView.Interfaces.IMappingListViewViewModel;
import Views.MappingList.MappingListView;

/**
 * The {@linkplain MagicDrawHubBrowserPanel} is the {@linkplain MappingListView} for the MagicDraw / Cameo software
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MagicDrawMappingListViewPanel extends MagicDrawBasePanel<IMappingListViewViewModel, MagicDrawMappingListView>
{
    /**
     * Initializes a new {@linkplain MagicDrawMappingListViewPanel}
     */
    public MagicDrawMappingListViewPanel()
    {
        super("DEH MagicDraw Adapter - MappingListView");
        setTabTitle("Mapping List View");
        setFrameIcon(ImageLoader.GetIcon("icon16.png"));
        this.setDefaultCloseAction(CLOSE_ACTION_TO_HIDE);
        this.view = new MagicDrawMappingListView();
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
        this.view.SetDataContext(this.dataContext);
    }
}
