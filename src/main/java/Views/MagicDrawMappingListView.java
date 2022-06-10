/*
 * MagicDrawMappingListView.java
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

import ViewModels.MappingListView.Renderers.MappingListViewSysMLElementCellEditor;
import ViewModels.MappingListView.Renderers.MappingListViewSysMLElementCellRenderer;
import Views.MappingList.MappingListView;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

/**
 * The {@linkplain MagicDrawMappingListView} is the {@linkplain MappingListView} implementation for the MagicDraw adapter 
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MagicDrawMappingListView extends MappingListView
{
    /**
     * Initializes a new {@linkplain CapellaMappingListView}
     */
    public MagicDrawMappingListView()
    {
        super();
        this.objectBrowserTree.setDefaultRenderer(Class.class, new MappingListViewSysMLElementCellRenderer());
        this.objectBrowserTree.setDefaultEditor(Class.class, new MappingListViewSysMLElementCellEditor());
        this.objectBrowserTree.setCellSelectionEnabled(false);
    }
}
