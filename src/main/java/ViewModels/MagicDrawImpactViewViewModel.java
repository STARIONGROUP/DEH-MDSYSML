/*
 * MagicDrawImpactViewViewModel.java
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

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.tree.TreeModel;

import org.eclipse.emf.ecore.EObject;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import DstController.IDstController;
import Enumerations.MappingDirection;
import Reactive.ObservableCollection;
import Services.MagicDrawSession.IMagicDrawSessionService;
import Services.MagicDrawTransaction.IMagicDrawTransactionService;
import Services.Stereotype.IStereotypeService;
import Utils.Ref;
import Utils.Stereotypes.Stereotypes;
import ViewModels.Interfaces.IMagicDrawImpactViewViewModel;
import ViewModels.MagicDrawObjectBrowser.MagicDrawObjectBrowserTreeRowViewModel;
import ViewModels.MagicDrawObjectBrowser.MagicDrawObjectBrowserTreeViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ClassRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.MagicDrawObjectBrowser.Rows.RootRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.ObjectBrowser.Interfaces.IThingRowViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.Iteration;
import io.reactivex.Observable;

/**
 * The {@linkplain MagicDrawImpactViewViewModel} is the main view model for the
 * requirement impact view in the impact view panel
 */
public class MagicDrawImpactViewViewModel extends MagicDrawObjectBrowserViewModel
		implements IMagicDrawImpactViewViewModel
{
	/**
	 * The {@linkplain IDstController}
	 */
	private final IDstController dstController;

	/**
	 * The {@linkplain IMagicDrawTransactionService}
	 */
	private final IMagicDrawTransactionService transactionService;

	/**
	 * The {@linkplain IStereotypeService}
	 */
	private final IStereotypeService stereotypeService;

	/**
	 * Initializes a new {@linkplain RequirementImpactViewViewModel}
	 * 
	 * @param dstController      the {@linkplain IDstController}
	 * @param sessionService     the {@linkplain IMagicDrawSessionService}
	 * @param transactionService the {@linkplain IMagicDrawTransactionService}
	 * @param stereotypeService  the {@linkplain IStereotypeService}
	 */
	public MagicDrawImpactViewViewModel(IDstController dstController, IMagicDrawSessionService sessionService,
			IMagicDrawTransactionService transactionService, IStereotypeService stereotypeService)
	{
		super(sessionService);
		this.dstController = dstController;
		this.transactionService = transactionService;
		this.stereotypeService = stereotypeService;
		this.InitializesObservables();
	}

	/**
	 * Initializes the needed subscription on {@linkplain Observable}
	 */
	private void InitializesObservables()
	{
		this.sessionService.HasAnyOpenSessionObservable().subscribe(this::UpdateBrowserTrees);

		this.sessionService.SessionUpdated().subscribe(hasBeenSaved -> {
			this.UpdateBrowserTrees(this.sessionService.HasAnyOpenSession());
		});

		this.dstController.GetHubMapResult().ItemsAdded().subscribe(
				x -> this.UpdateBrowserTrees(this.sessionService.HasAnyOpenSession()), this.logger::catching);

		this.dstController.GetHubMapResult().IsEmptyObservable().subscribe(isEmpty -> {
			if (isEmpty)
			{
				this.UpdateBrowserTrees(this.sessionService.HasAnyOpenSession());
			}
		});

		this.dstController.GetSelectedHubMapResultForTransfer().ItemsAdded().subscribe(x -> {
			for (Class thing : x)
			{
				this.SwitchIsSelected(thing, true);
			}

			this.shouldRefreshTree.Value(true);
		});

		this.dstController.GetSelectedHubMapResultForTransfer().ItemRemoved().subscribe(x -> {
			this.SwitchIsSelected(x, false);
			this.shouldRefreshTree.Value(true);
		});
	}

	/**
	 * Sets is selected property on the row view model that represents the provided
	 * {@linkplain Element}
	 * 
	 * @param element      The {@linkplain Element} to find the corresponding row
	 *                     view model
	 * @param shouldSelect A value indicating whether the row view model should set
	 *                     as selected
	 */
	private void SwitchIsSelected(Class element, boolean shouldSelect)
	{
		Ref<ElementRowViewModel<? extends Element>> refRowViewModel = new Ref<>(null);

		if (this.TryGetRowViewModelById(this.GetRootRowViewModel().GetContainedRows(), element, refRowViewModel))
		{
			this.SwitchIsSelected(refRowViewModel.Get(), shouldSelect);
		}
	}

	/**
	 * Gets the {@linkplain RootRowViewModel}
	 * 
	 * @return the {@linkplain RootRowViewModel}
	 */
	private RootRowViewModel GetRootRowViewModel()
	{
		return ((RootRowViewModel) this.browserTreeModel.Value().getRoot());
	}

	/**
	 * Sets is selected property on the row view model
	 * 
	 * @param shouldSelect a value indicating whether the row view model should set
	 *                     as selected
	 * @param rowViewModel the row view model to select or de-select
	 */
	private void SwitchIsSelected(IElementRowViewModel<? extends Element> rowViewModel, boolean shouldSelect)
	{
		if (!rowViewModel.GetIsSelected() && shouldSelect)
		{
			rowViewModel.SetIsSelected(true);
		} else if (rowViewModel.GetIsSelected() && !shouldSelect)
		{
			rowViewModel.SetIsSelected(false);
		}
	}

	/**
	 * Computes the difference for the provided {@linkplain Thing}
	 * 
	 * @return a {@linkplain RootRowViewModel}
	 */
	protected RootRowViewModel ComputeDifferences()
	{
		RootRowViewModel rootRowViewModel = (RootRowViewModel) this.browserTreeModel.Value().getRoot();

		try
		{
			for (MappedElementRowViewModel<? extends Thing, ? extends Class> mappedElementRowViewModel : this.dstController
					.GetHubMapResult())
			{
				Ref<ElementRowViewModel<? extends Element>> refRowViewModel = new Ref<>(null);

				if (this.TryGetRowViewModelById(rootRowViewModel.GetContainedRows(),
						mappedElementRowViewModel.GetDstElement(), refRowViewModel))
				{
					refRowViewModel.Get().UpdateElement(mappedElementRowViewModel.GetDstElement(), true);
					continue;
				}

				Ref<Element> refElementParentToUpdate = new Ref<>(Element.class);

				if (this.TryToFindParent(rootRowViewModel.GetContainedRows(),
						mappedElementRowViewModel.GetDstElement().eContainer(), refRowViewModel,
						refElementParentToUpdate))
				{
					refRowViewModel.Get().UpdateElement(refElementParentToUpdate.Get(), true);
				} else
				{
					rootRowViewModel = this.ComputeDifferences(rootRowViewModel, mappedElementRowViewModel);
				}
			}
		} catch (Exception exception)
		{
			this.logger.catching(exception);
		}

		return rootRowViewModel;
	}

	/**
	 * Computes the differences when none of the modifiable parent are contained in
	 * the tree already
	 * 
	 * @param rootRowViewModel          the {@linkplain RootRowViewModel}
	 * @param mappedElementRowViewModel the {@linkplain MappedElementRowViewModel}
	 */
	private RootRowViewModel ComputeDifferences(RootRowViewModel rootRowViewModel,
			MappedElementRowViewModel<? extends Thing, ? extends Element> mappedElementRowViewModel)
	{
		Element parent = this.FindUncontainedParent(mappedElementRowViewModel.GetDstElement());

		if (parent == null)
		{
			return rootRowViewModel;
		}

		List<Element> containedRows = rootRowViewModel.GetContainedRows().stream().map(x -> (Element) x.GetElement())
				.collect(Collectors.toList());
		containedRows.removeIf(x -> AreTheseEquals(x.getID(), parent.getID()));
		containedRows.add(parent);
		rootRowViewModel = new RootRowViewModel(rootRowViewModel.GetName(), containedRows);

		return rootRowViewModel;
	}

	/**
	 * Finds the parent of the provided {@linkplain Element} where its container is
	 * null
	 * 
	 * @param element the {@linkplain Element} from which to get the parent
	 * @return a {@linkplain Element}
	 */
	private Element FindUncontainedParent(Element element)
	{
		EObject parent = element;
		Element previousParent = (Element) parent;

		while (parent != null && parent instanceof Element)
		{
			previousParent = (Element) parent;
			parent = parent.eContainer();
		}

		return previousParent;
	}

	/**
	 * Tries to get the parent row view model
	 * 
	 * @param containedRows   the root children row view models
	 * @param parent          the direct parent
	 * @param refRowViewModel the {@linkplain Ref} of
	 *                        {@linkplain ElementRowViewModel} that can contain the
	 *                        searched parent row view model
	 * @param refContainer    the {@linkplain Ref} of {@linkplain Element} that can
	 *                        contain the {@linkplain Element} parent in case it is
	 *                        not the provided parent
	 * @return a value indicating the parent row view model has been found
	 */
	private boolean TryToFindParent(ObservableCollection<IElementRowViewModel<? extends Element>> containedRows,
			EObject parent, Ref<ElementRowViewModel<? extends Element>> refRowViewModel, Ref<Element> refContainer)
	{
		EObject container = parent;

		while (container instanceof Element && !this.TryGetRowViewModelById(containedRows, container, refRowViewModel))
		{
			container = container.eContainer();
		}

		refContainer.Set((Element) container);

		return refRowViewModel.HasValue();
	}

	/**
	 * Gets the {@linkplain Thing} by its Iid from the child row view models
	 * 
	 * @param childrenCollection the {@linkplain Collection} collection from the
	 *                           parent row view model
	 * @param element            the {@linkplain EObject} represented by the
	 *                           searched {@linkplain TElementRowViewModel}
	 * @param refElement         the {@linkplain Ref} of
	 *                           {@linkplain TElementRowViewModel} as ref parameter
	 */
	private boolean TryGetRowViewModelById(Collection<IElementRowViewModel<? extends Element>> childrenCollection,
			EObject element, Ref<ElementRowViewModel<? extends Element>> refElement)
	{
		return this.TryGetRowViewModelBy(childrenCollection,
				x -> AreTheseEquals(x.GetElement().getID(), ((Element) element).getID()), refElement);
	}

	/**
	 * Gets the {@linkplain Thing} by its Iid from the capella sessions
	 * 
	 * @param childrenCollection the {@linkplain Collection} collection from the
	 *                           parent row view model
	 * @param predicate          the {@linkplain Predicate} that test the view
	 *                           models against the specified check
	 * @param refElement         the {@linkplain Ref} of
	 *                           {@linkplain TElementRowViewModel} as ref parameter
	 */
	@SuppressWarnings("unchecked")
	private boolean TryGetRowViewModelBy(Collection<IElementRowViewModel<? extends Element>> childrenCollection,
			Predicate<IElementRowViewModel<? extends Element>> predicate,
			Ref<ElementRowViewModel<? extends Element>> refElement)
	{
		if (childrenCollection == null || childrenCollection.isEmpty())
		{
			return false;
		}

		for (IElementRowViewModel<? extends Element> childRowViewModel : childrenCollection)
		{
			if (childRowViewModel.GetElement() != null && predicate.test(childRowViewModel))
			{
				refElement.Set((ElementRowViewModel<? extends Element>) childRowViewModel);
				break;
			}

			if (childRowViewModel instanceof IHaveContainedRows)
			{
				if (this.TryGetRowViewModelBy(
						((IHaveContainedRows<IElementRowViewModel<? extends Element>>) childRowViewModel)
								.GetContainedRows(),
						predicate, refElement))
				{
					break;
				}
			}
		}

		return refElement.HasValue();
	}

	/**
	 * Updates this view model {@linkplain TreeModel}
	 * 
	 * @param isConnected a value indicating whether the session is open
	 */
	@Override
	protected void UpdateBrowserTrees(Boolean isConnected)
	{
		if (isConnected)
		{
			MagicDrawObjectBrowserTreeViewModel treeModel = this.dstController.GetHubMapResult().isEmpty()
					? new MagicDrawObjectBrowserTreeViewModel(this.sessionService.GetProjectName(),
							this.sessionService.GetProjectElements())
					: new MagicDrawObjectBrowserTreeViewModel(this.ComputeDifferences());

			this.SetOutlineModel(DefaultOutlineModel.createOutlineModel(treeModel,
					new MagicDrawObjectBrowserTreeRowViewModel(), true));
		}

		this.isTheTreeVisible.Value(isConnected);
	}

	/**
	 * Updates the {@linkplain browserTreeModel} based on the provided
	 * {@linkplain Iteration}
	 * 
	 * @param iteration the {@linkplain Iteration}
	 */
	protected void SetOutlineModel(OutlineModel model)
	{
		this.UpdateHighlightOnRows(model);
		this.browserTreeModel.Value(model);
	}

	/**
	 * Updates the <code>IsHighlighted</code> property on each row of the specified
	 * model
	 * 
	 * @param model the {@linkplain OutlineModel}
	 */
	private void UpdateHighlightOnRows(OutlineModel model)
	{
		Object rowViewModel = model.getRoot();

		if (rowViewModel instanceof IHaveContainedRows)
		{
			this.UpdateHiglightOnRows((IElementRowViewModel<?>) rowViewModel, false);
		}
	}

	/**
	 * Updates the <code>IsHighlighted</code> property on each row of the specified
	 * model
	 * 
	 * @param rowViewModel the {@linkplain IElementRowViewModel}
	 */
	@SuppressWarnings("unchecked")
	private void UpdateHiglightOnRows(IElementRowViewModel<?> rowViewModel, boolean shouldBeHighlighted)
	{
		if (rowViewModel instanceof IHaveContainedRows)
		{
			for (IElementRowViewModel<?> childRow : ((IHaveContainedRows<IElementRowViewModel<?>>) rowViewModel)
					.GetContainedRows())
			{
				if (childRow.GetElement() == null)
				{
					continue;
				}

				boolean isHighlighted = shouldBeHighlighted
						|| this.dstController.GetHubMapResult().stream()
								.anyMatch(r -> AreTheseEquals(r.GetDstElement().getID(), childRow.GetElement().getID()))
						|| this.transactionService.IsClonedOrNew(childRow.GetElement());

				childRow.SetIsHighlighted(isHighlighted);

				this.UpdateHiglightOnRows(childRow, isHighlighted);
			}
		}
	}

	/**
	 * Compute eligible rows where the represented {@linkplain Thing} can be
	 * transfered, and return the filtered collection for feedback application on
	 * the tree
	 * 
	 * @param selectedRow the collection of selected view model
	 *                    {@linkplain IThingRowViewModel}
	 */
	@Override
	public void OnSelectionChanged(ClassRowViewModel selectedRow)
	{
		if (selectedRow != null && selectedRow.GetElement() != null && this.dstController.GetHubMapResult().stream()
				.anyMatch(r -> AreTheseEquals(r.GetDstElement().getID(), selectedRow.GetElement().getID())))
		{
			this.AddOrRemoveSelectedRowToTransfer(selectedRow);
		}
	}

	/**
	 * Adds or remove the {@linkplain Thing} to/from the relevant collection
	 * depending on the {@linkplain MappingDirection}
	 * 
	 * @param thing the {@linkplain Thing} to add or remove
	 * @return a value indicating whether the row has been selected
	 */
	private void AddOrRemoveSelectedRowToTransfer(ClassRowViewModel rowViewModel)
	{
		this.AddOrRemoveSelectedRowToTransfer(rowViewModel, rowViewModel.SwitchIsSelectedValue());
		this.AddOrRemoveForTransfer(rowViewModel.GetIsSelected(), rowViewModel);
	}

	/**
	 * Adds or remove the {@linkplain Thing} to/from the relevant collection
	 * depending on the {@linkplain MappingDirection}
	 * 
	 * @param thing      the {@linkplain Thing} to add or remove
	 * @param isSelected a value indicating whether the row is selected
	 */
	private void AddOrRemoveSelectedRowToTransfer(ClassRowViewModel rowViewModel, boolean isSelected)
	{
		for (Property partProperty : rowViewModel.GetElement().getOwnedAttribute().stream()
				.filter(x -> this.stereotypeService.DoesItHaveTheStereotype(x, Stereotypes.PartProperty)
						|| x.getType() instanceof Class)
				.collect(Collectors.toList()))

		{
			Ref<ElementRowViewModel<? extends Element>> refRowViewModel = new Ref<>(null);

			if (this.TryGetRowViewModelBy(this.GetRootRowViewModel().GetContainedRows(),
					x -> AreTheseEquals(x.GetElement().getID(), partProperty.getType().getID()), refRowViewModel)
					&& refRowViewModel.Get() instanceof ClassRowViewModel
					&& this.dstController.GetHubMapResult().stream().anyMatch(
							r -> AreTheseEquals(r.GetDstElement().getID(), refRowViewModel.Get().GetElement().getID())))
			{
				ClassRowViewModel childRowViewModel = (ClassRowViewModel) refRowViewModel.Get();

				childRowViewModel.SetIsSelected(isSelected);
				this.AddOrRemoveForTransfer(isSelected, childRowViewModel);
				this.AddOrRemoveSelectedRowToTransfer(childRowViewModel, isSelected);
			}
		}
	}

	/**
	 * Adds or remove the {@linkplain Thing} to/from the relevant collection
	 * depending on the {@linkplain MappingDirection}
	 * 
	 * @param isSelected   a value indicating whether the row is selected
	 * @param rowViewModel the {@linkplain Thing} to add or remove
	 */
	private void AddOrRemoveForTransfer(boolean isSelected, ClassRowViewModel rowViewModel)
	{
		if (isSelected)
		{
			this.dstController.GetSelectedHubMapResultForTransfer().add(rowViewModel.GetElement());
		} else
		{
			this.dstController.GetSelectedHubMapResultForTransfer().RemoveOne(rowViewModel.GetElement());
		}
	}
}
