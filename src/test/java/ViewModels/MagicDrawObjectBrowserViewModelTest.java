/*
* MagicDrawObjectBrowserViewModelTest.java
*
* Copyright (c) 2020-2022 RHEA System S.A.
*
* Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski, Antoine Théate
*
* This file is part of DEH-CommonJ
*
* The DEH-CommonJ is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 3 of the License, or (at your option) any later version.
*
* The DEH-CommonJ is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program; if not, write to the Free Software Foundation,
* Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/
package ViewModels;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;

import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.Stereotype.IStereotypeService;
import Services.Stereotype.StereotypeService;
import Utils.Stereotypes.Stereotypes;
import ViewModels.MagicDrawObjectBrowser.Rows.BlockRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.PackageRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.RequirementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.RootRowViewModel;

class MagicDrawObjectBrowserViewModelTest
{
	MagicDrawObjectBrowserViewModel viewModel;
	IMagicDrawSessionService sessionService;
	ArrayList<Element> elements;
	IStereotypeService stereotypeService;

	@BeforeEach
	void setUp()
	{
		this.elements = new ArrayList<>();
		this.stereotypeService = mock(IStereotypeService.class);
		StereotypeService.SetCurrent(this.stereotypeService);

		this.sessionService = mock(IMagicDrawSessionService.class);
		when(this.sessionService.GetProjectName()).thenReturn("MODEL");
		when(this.sessionService.GetProjectElements()).thenReturn(this.elements);

		this.viewModel = new MagicDrawObjectBrowserViewModel(this.sessionService);
	}

	@Test
	void VerifyBuildEmptyTree()
	{
		assertDoesNotThrow(() -> this.viewModel.BuildTree());
		RootRowViewModel root = (RootRowViewModel) this.viewModel.GetBrowserTreeModel().getRoot();
		assertEquals(0, root.GetContainedRows().size());
		assertDoesNotThrow(() -> this.viewModel.BuildTree());
		assertNotNull(this.viewModel.GetSelectedElement());
	}

	@Test
	void VerifyUpdateBrowserTrees()
	{
		assertDoesNotThrow(() -> this.viewModel.UpdateBrowserTrees(true));
		assertDoesNotThrow(() -> this.viewModel.UpdateBrowserTrees(false));
	}

	@Test
	void VerifyBuildTree()
	{
		Package mainPackage = mock(Package.class);
		when(mainPackage.getName()).thenReturn("Package");

		this.elements.add(mainPackage);
		ArrayList<Element> containedElements = new ArrayList<>();
		when(mainPackage.getOwnedElement()).thenReturn(containedElements);

		Class block = mock(Class.class);
		when(block.getName()).thenReturn("block");
		when(this.stereotypeService.DoesItHaveTheStereotype(block, Stereotypes.Block)).thenReturn(true);

		ArrayList<Property> properties = new ArrayList<>();
		Property referenceProperty = mock(Property.class);
		when(this.stereotypeService.IsReferenceProperty(referenceProperty)).thenReturn(true);
		properties.add(referenceProperty);

		Property valuePropertyDataType = mock(Property.class);
		when(valuePropertyDataType.getType()).thenReturn(mock(DataType.class));
		when(this.stereotypeService.IsValueProperty(valuePropertyDataType)).thenReturn(false);
		properties.add(valuePropertyDataType);

		Property partProperty = mock(Property.class);
		when(this.stereotypeService.IsPartProperty(partProperty)).thenReturn(true);
		properties.add(partProperty);

		Property valueProperty = mock(Property.class);
		when(this.stereotypeService.IsValueProperty(valuePropertyDataType)).thenReturn(true, false);
		properties.add(valueProperty);
		properties.add(valueProperty);

		Property partProperty2 = mock(Property.class);
		when(partProperty2.getType()).thenReturn(mock(Class.class));
		when(this.stereotypeService.IsValueProperty(partProperty2)).thenReturn(false);
		properties.add(partProperty2);

		when(block.getOwnedAttribute()).thenReturn(properties);

		Port port = mock(Port.class);
		ArrayList<Port> ports = new ArrayList<>();
		ports.add(port);
		when(block.getOwnedPort()).thenReturn(ports);

		Class requirement = mock(Class.class);
		when(requirement.getName()).thenReturn("requirement");
		when(this.stereotypeService.DoesItHaveTheStereotype(requirement, Stereotypes.Block)).thenReturn(false);

		containedElements.add(block);
		containedElements.add(requirement);
		containedElements.add(mock(Element.class));

		this.elements.add(mainPackage);

		assertDoesNotThrow(() -> this.viewModel.BuildTree());
		RootRowViewModel root = (RootRowViewModel) this.viewModel.GetBrowserTreeModel().getRoot();

		PackageRowViewModel packageRow = (PackageRowViewModel) root.GetContainedRows().get(1);
		assertEquals(Stereotypes.Package, packageRow.GetClassKind());
		assertEquals(2, root.GetContainedRows().size());

		BlockRowViewModel blockRow = (BlockRowViewModel) packageRow.GetContainedRows().get(0);
		assertEquals(Stereotypes.Block, blockRow.GetClassKind());

		RequirementRowViewModel requirementRow = (RequirementRowViewModel) packageRow.GetContainedRows().get(1);
		assertEquals(Stereotypes.Requirement, requirementRow.GetClassKind());
		assertDoesNotThrow(() -> requirementRow.ComputeContainedRows());

		this.viewModel.OnSelectionChanged(blockRow);
		this.viewModel.OnSelectionChanged(requirementRow);
		this.viewModel.OnSelectionChanged(mock(ClassRowViewModel.class));

		assertEquals(7, blockRow.GetContainedRows().size());
		assertEquals(Stereotypes.PortProperty, blockRow.GetContainedRows().get(6).GetClassKind());
	}
}
