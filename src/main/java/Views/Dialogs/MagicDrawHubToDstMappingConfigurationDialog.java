/*
 * MagicDrawHubToDstMappingConfigurationDialog.java
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
package Views.Dialogs;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Enumerations.MappingDirection;
import Renderers.MappedElementDefinitionListViewCellEditor;
import Renderers.MappedElementDefinitionListViewCellRenderer;
import Renderers.MappedElementDefinitionListViewCellRendererProvider;
import ViewModels.Dialogs.Interfaces.IHubToDstMappingConfigurationDialogViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import Views.MagicDrawObjectBrowser;
import cdp4common.commondata.Thing;

/**
 * The {@linkplain MagicDrawHubToDstMappingConfigurationDialog} is the dialog view to allow to configure a mapping 
 * to be defined between a selection of DST elements and the hub element
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MagicDrawHubToDstMappingConfigurationDialog extends MappingConfigurationDialog<IHubToDstMappingConfigurationDialogViewModel, Thing, Class, ClassRowViewModel>
{
    /**
     * Initializes a new {@linkplain MagicDrawHubToDstMappingConfigurationDialog}
     */
    public MagicDrawHubToDstMappingConfigurationDialog()
    {
        super(MappingDirection.FromHubToDst, new MagicDrawObjectBrowser());
        this.mappedElementListView.GetObjectBrowser().setDefaultRenderer(String.class, new MappedElementDefinitionListViewCellRenderer());
        this.mappedElementListView.GetObjectBrowser().setDefaultEditor(String.class, new MappedElementDefinitionListViewCellEditor());
        MappedElementDefinitionListViewCellRendererProvider.Current.ClearCache();
        this.mappedElementListView.GetObjectBrowser().setRowHeight(100);
    }
}
