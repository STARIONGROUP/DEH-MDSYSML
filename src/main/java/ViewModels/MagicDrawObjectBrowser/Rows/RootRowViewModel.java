/*
 * RootRowViewModel.java
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

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;

/**
 * The {@linkplain RootRowViewModel} represents the root element in one
 * containment tree
 */
public class RootRowViewModel extends PackageRowViewModel implements IHaveContainedRows<IElementRowViewModel<?>>
{
	/**
	 * The {@linkplain Collection} of {@linkplain Element} that the
	 * {@linkplain containedRows} should contain
	 */
	private Collection<Element> containedElements;

	/**
	 * Initializes a new {@linkplain RootRowViewModel}
	 * 
	 * @param name     the name of this row
	 * @param elements the children element that this row contains
     * @param transactionService the {@linkplain IMagicDrawTransactionService}
	 */
	public RootRowViewModel(String name, Collection<Element> elements, IMagicDrawTransactionService transactionService)
	{
		super(null, null, transactionService);

		this.containedElements = elements.stream().filter(x -> x instanceof NamedElement).map(x -> (NamedElement) x)
				.sorted(Comparator.comparing(NamedElement::getName)).collect(Collectors.toList());

		this.UpdateProperties(name);
	}

	/**
	 * Updates this view model properties
	 * 
	 * @param name     the name of this row
	 * @param elements the children element that this row contains
	 */
	@Override
	protected void UpdateProperties(String name)
	{
		super.UpdateProperties(name);
		this.ComputeContainedRows();
	}

	/**
	 * Computes the contained rows of this row view model
	 */
	@Override
	public void ComputeContainedRows()
	{
		if (this.containedElements == null)
		{
			return;
		}

		for (Element element : this.containedElements)
		{
			this.ComputeContainedRow(element);
		}
	}
}
