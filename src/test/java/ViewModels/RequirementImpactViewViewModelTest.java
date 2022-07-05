/*
 * RequirementImpactViewViewModelTest.java
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
package ViewModels;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import DstController.IDstController;
import HubController.IHubController;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Utils.Ref;
import ViewModels.ObjectBrowser.RequirementTree.Rows.IterationRequirementRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Requirement;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.DomainOfExpertise;

class RequirementImpactViewViewModelTest
{
	private IHubController hubController;
	private IDstController dstController;
	private RequirementImpactViewViewModel viewModel;
	private ObservableCollection<MappedElementRowViewModel<DefinedThing, Class>> dstMapResult;
	private ObservableCollection<Thing> selectedDstMapResultForTransfer;
	private ObservableValue<Boolean> isSessionOpen;
	private ObservableValue<Boolean> sessionEvent;
	private Iteration iteration;

	@BeforeEach
	void setUp()
	{
		this.hubController = mock(IHubController.class);
		this.dstController = mock(IDstController.class);

		this.isSessionOpen = new ObservableValue<>(false, Boolean.class);
		this.sessionEvent = new ObservableValue<>(false, Boolean.class);
		this.iteration = this.GetIteration();

		when(this.hubController.GetIsSessionOpenObservable()).thenReturn(this.isSessionOpen.Observable());
		when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
		when(this.hubController.GetSessionEventObservable()).thenReturn(this.sessionEvent.Observable());

		this.dstMapResult = new ObservableCollection<>();
		when(this.dstController.GetDstMapResult()).thenReturn(this.dstMapResult);
		this.selectedDstMapResultForTransfer = new ObservableCollection<>(Thing.class);
		when(this.dstController.GetSelectedDstMapResultForTransfer()).thenReturn(this.selectedDstMapResultForTransfer);

		this.viewModel = new RequirementImpactViewViewModel(this.hubController, this.dstController);
	}

	@SuppressWarnings("resource")
	@Test
	void VerifyComputeDifferences()
	{
		assertEquals(null, this.viewModel.browserTreeModel.Value());
		this.isSessionOpen.Value(true);
		this.viewModel.SetOutlineModel(this.hubController.GetOpenIteration());
		assertNotNull(this.viewModel.browserTreeModel);
		assertNotNull(this.viewModel.browserTreeModel.Value());

		assertEquals(IterationRequirementRowViewModel.class,
				this.viewModel.browserTreeModel.Value().getRoot().getClass());

		assertEquals(1, ((IterationRequirementRowViewModel) this.viewModel.browserTreeModel.Value().getRoot())
				.GetContainedRows().size());

		assertNull(this.viewModel.GetRowViewModelFromThing(new Requirement()));

		Requirement requirement = this.iteration.getRequirementsSpecification().get(0).getRequirement().get(0);

		assertNotNull(this.viewModel.GetRowViewModelFromThing(requirement));
		assertDoesNotThrow(() -> this.viewModel.ComputeDifferences(this.iteration, requirement));
		assertDoesNotThrow(() -> this.viewModel.ComputeDifferences(this.iteration, requirement.clone(false)));
		RequirementsSpecification newSpecification = new RequirementsSpecification();
		Requirement newRequirement = new Requirement();
		newSpecification.getRequirement().add(newRequirement);
		assertDoesNotThrow(() -> this.viewModel.ComputeDifferences(this.iteration, newRequirement));
	}

	@Test
	void VerifyAddAllToSelectedDstMapResultForTransfer()
	{
		Ref<Boolean> refreshHasBeenTriggered = new Ref<>(Boolean.class, false);
		this.viewModel.GetShouldRefreshTree().subscribe(x -> refreshHasBeenTriggered.Set(x));

		this.viewModel.SetOutlineModel(this.hubController.GetOpenIteration());

		this.selectedDstMapResultForTransfer
				.addAll(this.dstMapResult.stream().map(x -> x.GetHubElement()).collect(Collectors.toList()));

		assertTrue(refreshHasBeenTriggered.Get());
	}

	private Iteration GetIteration()
	{
		Iteration iteration = new Iteration(UUID.randomUUID(), null, null);
		DomainOfExpertise owner = new DomainOfExpertise(UUID.randomUUID(), null, null);

		RequirementsSpecification requirementSpecification0 = new RequirementsSpecification(UUID.randomUUID(), null,
				null);

		requirementSpecification0.setOwner(owner);
		Requirement requirement0 = new Requirement(UUID.randomUUID(), null, null);
		requirement0.setOwner(owner);
		Requirement requirement1 = new Requirement(UUID.randomUUID(), null, null);
		requirement1.setOwner(owner);
		RequirementsGroup requirementGroup0 = new RequirementsGroup(UUID.randomUUID(), null, null);
		requirementGroup0.setOwner(owner);
		RequirementsGroup requirementGroup1 = new RequirementsGroup(UUID.randomUUID(), null, null);
		requirementGroup1.setOwner(owner);

		requirement1.setGroup(requirementGroup1);
		requirementSpecification0.getRequirement().add(requirement0);
		requirementSpecification0.getRequirement().add(requirement1);
		requirementSpecification0.getGroup().add(requirementGroup0);
		requirementGroup0.getGroup().add(requirementGroup1);
		iteration.getRequirementsSpecification().add(requirementSpecification0);

		return iteration;
	}
}
