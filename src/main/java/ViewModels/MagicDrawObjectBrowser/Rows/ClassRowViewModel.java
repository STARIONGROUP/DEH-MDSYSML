/*
 * ClassRowViewModel.java
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
package ViewModels.MagicDrawObjectBrowser.Rows;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Reactive.ObservableCollection;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;

/**
 * The {@linkplain ClassRowViewModel} is the row view model that represents either a block or a requirement, 
 * as in the {@linkplain BlockRowViewModel} or the {@linkplain RequirementRowViewModel}
 */
public abstract class ClassRowViewModel extends ElementRowViewModel<Class> implements IHaveContainedRows<IElementRowViewModel>
{
    /**
     * The {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    private ObservableCollection<IElementRowViewModel> containedRows = new ObservableCollection<IElementRowViewModel>();

    /**
     * Gets the contained row the implementing view model has
     * 
     * @return An {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    @Override
    public ObservableCollection<IElementRowViewModel> GetContainedRows()
    {
        return this.containedRows;
    }

    /**
     * @param parent the {@linkplain IElementRowViewModel} parent view model of this row view model
     * @param element the represented {@linkplain Class}
     */
    public ClassRowViewModel(IElementRowViewModel parent, Class element)
    {
        super(parent, element);
    }
    
    /**
     * Computes this row view model contained rows
     */
    @Override
    public void ComputeContainedRows()
    {
    }
}