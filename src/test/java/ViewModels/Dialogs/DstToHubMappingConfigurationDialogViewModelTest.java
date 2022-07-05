/*
* DstToHubMappingConfigurationDialogViewModelTest.java
*
* Copyright (c) 2020-2022 RHEA System S.A.
*
* Author: Sam Geren�, Alex Vorobiev, Nathanael Smiechowski, Antoine Th�ate
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
package ViewModels.Dialogs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import DstController.IDstController;
import Enumerations.MappedElementRowStatus;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Services.Stereotype.IStereotypeService;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Interfaces.IElementDefinitionBrowserViewModel;
import ViewModels.Interfaces.IRequirementBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.MagicDrawObjectBrowserTreeRowViewModel;
import ViewModels.MagicDrawObjectBrowser.MagicDrawObjectBrowserTreeViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IMagicDrawObjectBrowserViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.BlockRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.RequirementRowViewModel;
import ViewModels.MappedElementListView.Interfaces.IMappedElementListViewViewModel;
import ViewModels.ObjectBrowser.ElementDefinitionTree.Rows.ElementDefinitionRowViewModel;
import ViewModels.ObjectBrowser.Rows.ThingRowViewModel;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import ViewModels.Rows.MappedRequirementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.DomainOfExpertise;

class DstToHubMappingConfigurationDialogViewModelTest
{
	DstToHubMappingConfigurationDialogViewModel viewModel;
	IDstController dstController;
	IHubController hubController;
	IElementDefinitionBrowserViewModel elementDefinitionBrowserViewModel;
	IRequirementBrowserViewModel requirementBrowserViewModel;
	IMagicDrawObjectBrowserViewModel magicDrawObjectBrowserViewModel;
	IMappedElementListViewViewModel<Class> mappedElementListViewViewModel;
	IStereotypeService stereotypeService;
	ObservableValue<ClassRowViewModel> magicDrawSelectedElement;
	ObservableValue<ThingRowViewModel<Thing>> elementDefinitionSelectedElement;
	ObservableValue<ThingRowViewModel<Thing>> requirementSelectedElement;
	ObservableValue<MappedElementRowViewModel<DefinedThing, Class>> mappedSelectedElement;
	ObservableCollection<MappedElementRowViewModel<DefinedThing, Class>> dstMapResult;
	Iteration iteration;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp()
	{
		this.magicDrawSelectedElement = new ObservableValue<>();
		this.elementDefinitionSelectedElement = new ObservableValue<>();
		this.requirementSelectedElement = new ObservableValue<>();
		this.mappedSelectedElement = new ObservableValue<>();
		this.dstMapResult = new ObservableCollection<>();

		this.dstController = mock(IDstController.class);
		this.hubController = mock(IHubController.class);
		this.elementDefinitionBrowserViewModel = mock(IElementDefinitionBrowserViewModel.class);
		this.requirementBrowserViewModel = mock(IRequirementBrowserViewModel.class);
		this.magicDrawObjectBrowserViewModel = mock(IMagicDrawObjectBrowserViewModel.class);
		this.mappedElementListViewViewModel = mock(IMappedElementListViewViewModel.class);
		this.stereotypeService = mock(IStereotypeService.class);

		DomainOfExpertise domain = new DomainOfExpertise();
		domain.setName("THERMAL");

		this.iteration = new Iteration();

		when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
		when(this.hubController.GetCurrentDomainOfExpertise()).thenReturn(domain);
		when(this.dstController.GetDstMapResult()).thenReturn(this.dstMapResult);
		
		when(this.magicDrawObjectBrowserViewModel.GetSelectedElement())
				.thenReturn(this.magicDrawSelectedElement.Observable());
		
		when(this.elementDefinitionBrowserViewModel.GetSelectedElement())
				.thenReturn(this.elementDefinitionSelectedElement.Observable());
		
		when(this.requirementBrowserViewModel.GetSelectedElement())
				.thenReturn(this.requirementSelectedElement.Observable());
		
		when(this.mappedElementListViewViewModel.GetSelectedElement())
			.thenReturn(this.mappedSelectedElement.Observable());

		this.viewModel = new DstToHubMappingConfigurationDialogViewModel(this.dstController, this.hubController,
				this.elementDefinitionBrowserViewModel, this.requirementBrowserViewModel,
				this.magicDrawObjectBrowserViewModel, this.mappedElementListViewViewModel, this.stereotypeService);
	}

	@Test
	void VerifyProperties()
	{
		assertEquals(this.magicDrawObjectBrowserViewModel, this.viewModel.GetDstObjectBrowserViewModel());
		assertNotNull(this.viewModel.GetSelectedMappedElement());
		assertNotNull(this.viewModel.GetShouldMapToNewElementCheckBoxBeEnabled());
		assertEquals(0, this.viewModel.GetMappedElementCollection().size());
		assertEquals(this.mappedElementListViewViewModel, this.viewModel.GetMappedElementListViewViewModel());
	}

	@SuppressWarnings("unchecked")
	@Test
	void VerifyUpdateProperties()
	{
		assertDoesNotThrow(() -> this.viewModel.UpdateProperties());
		assertEquals(0, this.viewModel.GetMappedElementCollection().size());
		ArrayList<Element> mappedElements = new ArrayList<>();
		this.viewModel.SetMappedElement(mappedElements);
		assertDoesNotThrow(() -> this.viewModel.UpdateProperties());
		assertEquals(0, this.viewModel.GetMappedElementCollection().size());

		Class mappedBlock = mock(Class.class);
		when(mappedBlock.getName()).thenReturn("Accelerometer");
		when(mappedBlock.getID()).thenReturn(UUID.randomUUID().toString());
		mappedElements.add(mappedBlock);

		this.viewModel.SetMappedElement(mappedElements);
		assertDoesNotThrow(() -> this.viewModel.UpdateProperties());
		assertEquals(0, this.viewModel.GetMappedElementCollection().size());

		MappedElementRowViewModel<? extends DefinedThing, Class> mappedElement = new MappedElementDefinitionRowViewModel(
				new ElementDefinition(), mappedBlock, MappingDirection.FromDstToHub);
		this.dstMapResult.add((MappedElementRowViewModel<DefinedThing, Class>) mappedElement);

		assertDoesNotThrow(() -> this.viewModel.UpdateProperties());
		assertEquals(1, this.viewModel.GetMappedElementCollection().size());

		OutlineModel model = DefaultOutlineModel.createOutlineModel(
				new MagicDrawObjectBrowserTreeViewModel(("Envision"), new ArrayList<Element>()),
				new MagicDrawObjectBrowserTreeRowViewModel(), true);

		when(this.magicDrawObjectBrowserViewModel.GetBrowserTreeModel()).thenReturn(model);
		assertDoesNotThrow(() -> this.viewModel.ResetPreMappedThings());
		assertEquals(1, this.viewModel.GetMappedElementCollection().size());
	}

	@Test
	void VerifyPremap()
	{
		assertDoesNotThrow(() -> this.viewModel.PreMap(null));
		assertEquals(0, this.viewModel.mappedElements.size());

		ArrayList<Element> selectedElements = new ArrayList<>();
		ArrayList<Element> containedElements = new ArrayList<>();

		Package selectedPackage = mock(Package.class);
		when(selectedPackage.getOwnedElement()).thenReturn(containedElements);

		selectedElements.add(selectedPackage);
		selectedElements.add(mock(DataType.class));

		this.SetupElements(containedElements, selectedPackage);
		
		when(this.dstController.PreMap(any())).thenReturn(Arrays.asList());
		assertDoesNotThrow(() -> this.viewModel.PreMap(selectedElements));
		assertEquals(2, this.viewModel.mappedElements.size());
	}

	private void SetupElements(ArrayList<Element> containedElements, Package selectedPackage)
	{
		Class block = mock(Class.class);
		when(block.getID()).thenReturn(UUID.randomUUID().toString());
		when(block.getName()).thenReturn("Accelerometer");
		when(this.stereotypeService.DoesItHaveTheStereotype(block, Stereotypes.Block)).thenReturn(true);

		Class block2 = mock(Class.class);
		when(block2.getID()).thenReturn(UUID.randomUUID().toString());
		when(block2.getName()).thenReturn("Other Accelerometer");
		when(this.stereotypeService.DoesItHaveTheStereotype(block2, Stereotypes.Block)).thenReturn(true);

		Class requirement = mock(Class.class);
		when(requirement.getID()).thenReturn(UUID.randomUUID().toString());
		when(requirement.getName()).thenReturn("Launch Date");
		when(this.stereotypeService.DoesItHaveTheStereotype(requirement, Stereotypes.Requirement)).thenReturn(true);

		Package missionPackage = mock(Package.class);
		when(missionPackage.getName()).thenReturn("MissionRequirement");
		when(selectedPackage.getOwner()).thenReturn(missionPackage);

		Package parentMissionPackage = mock(Package.class);
		when(parentMissionPackage.getName()).thenReturn("MissionRequirements");
		when(missionPackage.getOwner()).thenReturn(parentMissionPackage);

		Class requirement2 = mock(Class.class);
		when(requirement2.getID()).thenReturn(UUID.randomUUID().toString());
		when(requirement2.getName()).thenReturn("Launcher mass");
		when(requirement2.getOwner()).thenReturn(selectedPackage);
		when(this.stereotypeService.DoesItHaveTheStereotype(requirement2, Stereotypes.Requirement)).thenReturn(true);

		Class requirement3 = mock(Class.class);
		when(requirement3.getID()).thenReturn(UUID.randomUUID().toString());
		when(requirement3.getName()).thenReturn("Launcher mass2");
		when(requirement3.getOwner()).thenReturn(missionPackage);
		when(this.stereotypeService.DoesItHaveTheStereotype(requirement3, Stereotypes.Requirement)).thenReturn(true);

		Class requirement4 = mock(Class.class);
		when(requirement4.getID()).thenReturn(UUID.randomUUID().toString());
		when(requirement4.getName()).thenReturn("Launcher mass3");
		when(this.stereotypeService.DoesItHaveTheStereotype(requirement4, Stereotypes.Requirement)).thenReturn(true);

		containedElements.add(block);
		containedElements.add(block2);
		containedElements.add(requirement);
		containedElements.add(requirement2);
		containedElements.add(requirement3);
		containedElements.add(requirement4);

		ElementDefinition definition = new ElementDefinition();
		definition.setIid(UUID.randomUUID());
		definition.setName(block.getName());
		this.iteration.getElement().add(definition);

		RequirementsSpecification requirementsSpecification = new RequirementsSpecification();
		requirementsSpecification.setName("MissionRequirement");
		requirementsSpecification.setIid(UUID.randomUUID());
		Requirement hubRequirement = new Requirement();
		hubRequirement.setName(requirement.getName());
		hubRequirement.setIid(UUID.randomUUID());
		requirementsSpecification.getRequirement().add(hubRequirement);

		this.iteration.getRequirementsSpecification().add(requirementsSpecification);
	}

	@SuppressWarnings("unchecked")
	@Test
	void VerifyWhenMapToNewElement()
	{
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		ElementDefinition elementDefinition = new ElementDefinition();
		elementDefinition.setOwner(this.hubController.GetCurrentDomainOfExpertise());
		ThingRowViewModel<? extends Thing> elementDefinitionRow = new ElementDefinitionRowViewModel(elementDefinition,
				null);

		Requirement requirement = new Requirement();
		requirement.setOwner(this.hubController.GetCurrentDomainOfExpertise());
		ThingRowViewModel<? extends Thing> requirementRow = new ViewModels.ObjectBrowser.RequirementTree.Rows.RequirementRowViewModel(
				requirement, null);

		this.requirementSelectedElement.Value((ThingRowViewModel<Thing>) requirementRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));
		this.elementDefinitionSelectedElement.Value((ThingRowViewModel<Thing>) elementDefinitionRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		Class newBlock = mock(Class.class);
		when(newBlock.getID()).thenReturn(UUID.randomUUID().toString());

		BlockRowViewModel newBlockRow = new BlockRowViewModel(null, newBlock);
		this.magicDrawSelectedElement.Value(newBlockRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		this.magicDrawSelectedElement.Value(newBlockRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		this.requirementSelectedElement.Value((ThingRowViewModel<Thing>) requirementRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));
		this.elementDefinitionSelectedElement.Value((ThingRowViewModel<Thing>) elementDefinitionRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		this.viewModel.selectedMappedElement.Value().SetRowStatus(MappedElementRowStatus.NewElement);
		this.viewModel.selectedMappedElement.Value().SetHubElement(elementDefinition);
		this.viewModel.WhenMapToNewElementCheckBoxChanged(true);

		this.requirementSelectedElement.Value((ThingRowViewModel<Thing>) requirementRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));
		this.elementDefinitionSelectedElement.Value((ThingRowViewModel<Thing>) elementDefinitionRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		Class newRequirement = mock(Class.class);
		when(newRequirement.getID()).thenReturn(UUID.randomUUID().toString());

		RequirementRowViewModel newRequirementRow = new RequirementRowViewModel(null, newRequirement);
		this.magicDrawSelectedElement.Value(newRequirementRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		this.magicDrawSelectedElement.Value(newRequirementRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));
		this.viewModel.selectedMappedElement.Value().SetRowStatus(MappedElementRowStatus.NewElement);

		this.requirementSelectedElement.Value((ThingRowViewModel<Thing>) requirementRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));
		this.elementDefinitionSelectedElement.Value((ThingRowViewModel<Thing>) elementDefinitionRow);
		assertDoesNotThrow(() -> this.viewModel.WhenMapToNewElementCheckBoxChanged(true));

		this.viewModel.selectedMappedElement.Value().SetHubElement(requirement);
		this.viewModel.WhenMapToNewElementCheckBoxChanged(true);
		this.viewModel.selectedMappedElement.Value().SetShouldCreateNewTargetElement(true);
		this.viewModel.WhenMapToNewElementCheckBoxChanged(true);
		this.viewModel.WhenMapToNewElementCheckBoxChanged(false);
	}
}
