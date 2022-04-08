/*
 * MagicDrawAdapterRibbonActionCategory.java
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
package Actions.ToolBar;

import java.util.Arrays;

import com.nomagic.actions.ActionsCategory;

import Utils.ImageLoader.ImageLoader;

/**
 * The {@linkplain MagicDrawAdapterRibbonActionCategory} is the {@linkplain ActionsCategory} that groups all visual entry points for the MagicDraw adapter 
 */
@SuppressWarnings("serial")
public class MagicDrawAdapterRibbonActionCategory extends ActionsCategory
{
    /**
     * Initializes a new {@linkplain MagicDrawAdapterRibbonActionCategory}
     */
    public MagicDrawAdapterRibbonActionCategory()
    {
        super("DEH Magic Draw Adapter", "DEH Magic Draw Adapter Menu", null, null);
        this.setLargeIcon(ImageLoader.GetIcon("icon16.png"));
        this.setNested(true);
        this.addActions(Arrays.asList(new OpenHubBrowserPanelAction(), new LocalExchangeHistoryAction()));
    }
}
