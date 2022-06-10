/*
 * PackageRowViewModel.java
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

import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import Services.Stereotype.StereotypeService;

import Reactive.ObservableCollection;
import Utils.Stereotypes.Stereotypes;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain PackageRowViewModel} is the row view model that represents a {@linkplain Package}
 */
public class PackageRowViewModel extends ElementRowViewModel<Package> implements IHaveContainedRows<IElementRowViewModel<?>>
{
    /**
     * The {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    private ObservableCollection<IElementRowViewModel<?>> containedRows = new ObservableCollection<>();

    /**
     * Gets the contained row the implementing view model has
     * 
     * @return An {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    @Override
    public ObservableCollection<IElementRowViewModel<?>> GetContainedRows()
    {
        return this.containedRows;
    }

    /**
     * Initializes a new {@linkplain PackageRowViewModel}
     * 
     * @param parent the {@linkplain IRowViewModel} parent of this view model
     * {@linkplain package}
     */
    public PackageRowViewModel(IElementRowViewModel<?> parent, Package element)
    {
        super(parent, element);
        this.ComputeContainedRows();
    }
    
    /**
     * Computes the contained rows of this row view model
     */
    @Override
    public void ComputeContainedRows() 
    {
        for (Element element : this.GetElement().getOwnedElement())
        {
            this.ComputeContainedRow(element);
        }
    }

    /**
     * Computes the contained row of this row view model based on the provided {@linkplain Element}
     * 
     * @param element the {@linkplain Element}
     */
    protected void ComputeContainedRow(BaseElement element)
    {
        if (element instanceof Class)
        {
            Class classElement = (Class)element;
            
            if(StereotypeService.Current.DoesItHaveTheStereotype(classElement, Stereotypes.Block))
            {
                this.containedRows.add(new BlockRowViewModel(this, classElement));
            }
            else if(StereotypeService.Current.DoesItHaveTheStereotype(classElement, Stereotypes.Requirement))
            {
                this.containedRows.add(new RequirementRowViewModel(this, classElement));
            }
        }
        else if(element instanceof Package)
        {
            this.containedRows.add(new PackageRowViewModel(this, (Package)element));
        }
    }
    
    /**
     * Gets the string representation of the type of thing represented
     * 
     * @return a {@linkplain Stereotypes}
     */
    @Override
    public Stereotypes GetClassKind()
    {
        return Stereotypes.Package;
    }
}
