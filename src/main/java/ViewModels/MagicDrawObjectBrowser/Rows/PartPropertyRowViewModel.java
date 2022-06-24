/*
 * PartPropertyRowViewModel.java
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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;

import Reactive.ObservableCollection;
import Services.Stereotype.StereotypeService;
import Utils.Stereotypes.Stereotypes;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;

public class PartPropertyRowViewModel extends PropertyRowViewModel implements IHaveContainedRows<PropertyRowViewModel>
{
	/**
	 * The collection of contained rows
	 */
	private ObservableCollection<PropertyRowViewModel> containedRows = new ObservableCollection<>(
			PropertyRowViewModel.class);

	/**
	 * Gets the contained row the implementing view model has
	 * 
	 * @return An {@linkplain ObservableCollection} of
	 *         {@linkplain PropertyRowViewModel}
	 */
	@Override
	public ObservableCollection<PropertyRowViewModel> GetContainedRows()
	{
		return this.containedRows;
	}

	/**
	 * Initializes a new {@linkplain ReferencePropertyRowViewModel}
	 * 
	 * @param parent   the parent {@linkplain IElementRowViewModel} view model
	 * @param property the represented {@linkplain Property}
	 */
	public PartPropertyRowViewModel(IElementRowViewModel<?> parent, Property property)
	{
		super(parent, property);
		this.ComputeContainedRows();
	}

	/***
	 * Updates this view model properties
	 */
	@Override
	public void UpdateProperties()
	{
		Type type = this.GetElement().getType();
		this.SetName(String.format("%s : %s", this.GetElement().getName(), type != null ? type.getName() : ""));
	}

	/**
	 * Computes the contained rows
	 */
	@Override
	public void ComputeContainedRows()
	{
		this.containedRows.clear();

		if (!(this.GetElement().getType() instanceof Class))
		{
			this.Logger.error(String.format("The Part Property %s is not correctly typed", this.GetName()));
			return;
		}

		for (Property property : ((Class) this.GetElement().getType()).getOwnedAttribute())
		{
			if (StereotypeService.Current().IsReferenceProperty(property))
			{
				this.containedRows.add(new ReferencePropertyRowViewModel(this, property));
			} else if (StereotypeService.Current().IsValueProperty(property))
			{
				this.containedRows.add(new ValuePropertyRowViewModel(this, property));
			} else if (StereotypeService.Current().IsPartProperty(property)
					&& property.getID() != this.GetElement().getID())
			{
				this.containedRows.add(new PartPropertyRowViewModel(this, property));
			} else
			{
				this.containedRows.add(new ValuePropertyRowViewModel(this, property));
			}
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
		return Stereotypes.PartProperty;
	}
}
