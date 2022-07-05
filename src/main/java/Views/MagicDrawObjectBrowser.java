/*
 * MagicDrawObjectBrowser.java
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

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import org.apache.commons.lang3.tuple.Pair;

import Renderers.MagicDrawObjectBrowserRenderDataProvider;
import ViewModels.Interfaces.IImpactViewContextMenuViewModel;
import ViewModels.Interfaces.IViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IMagicDrawObjectBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import Views.ObjectBrowser.ObjectBrowserBase;

/**
 * The {@linkplain MagicDrawObjectBrowser}
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MagicDrawObjectBrowser extends ObjectBrowserBase<IMagicDrawObjectBrowserViewModel, IImpactViewContextMenuViewModel>
{
    /**
     * Initializes a new {@linkplain MagicDrawObjectBrowser}
     */
    public MagicDrawObjectBrowser()
    {
        super();
        this.objectBrowserTree.setRenderDataProvider(new MagicDrawObjectBrowserRenderDataProvider());
    }

    /**
     * Sets the DataContext
     * 
     * @param viewModel the {@link IViewModel} to assign
     */
    @Override
    public void SetDataContext(IMagicDrawObjectBrowserViewModel viewModel)
    {
        this.dataContext = viewModel;
        this.Bind();
    }

    /**
     * Gets the DataContext
     * 
     * @return An {@link IViewModel}
     */
    @Override
    public IMagicDrawObjectBrowserViewModel GetDataContext()
    {
        return this.dataContext;
    }

    /**
     * Handles the selection when the user changes it
     */
    @Override
    protected void OnSelectionChanged()
    {
        int selectedRowIndex = objectBrowserTree.getSelectedRow();

        Pair<Integer, IElementRowViewModel<?>> row = Pair.of(selectedRowIndex,
                (IElementRowViewModel<?>) objectBrowserTree.getValueAt(selectedRowIndex, 0));

        if (!(row.getRight() instanceof ClassRowViewModel))
        {
            return;
        }

        dataContext.OnSelectionChanged((ClassRowViewModel) row.getRight());

        SwingUtilities.invokeLater(() -> 
            objectBrowserTree.tableChanged(new TableModelEvent(objectBrowserTree.getOutlineModel(), row.getLeft())));
    }
}
