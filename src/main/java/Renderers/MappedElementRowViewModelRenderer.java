/*
 * MappedElementRowViewModelRenderer.java
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
package Renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;

/**
 * The {@linkplain MappedElementRowViewModelRenderer} is the custom renderer that allows to display {@linkplain MappedElementRowViewModel} in a {@linkplain JList}
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MappedElementRowViewModelRenderer extends JPanel
		implements ListCellRenderer<MappedElementRowViewModel<? extends Thing, ?>>
{
	private JLabel dstElement;
	private JLabel hubElement;

	/**
	 * Initializes a new {@linkplain MappedElementRowViewModelRenderer}
	 */
	public MappedElementRowViewModelRenderer()
	{
		this.Initialize();
	}

	/**
	 * Initializes this view components
	 */
	private void Initialize()
	{
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]
		{ 400, 0, 0 };
		gridBagLayout.rowHeights = new int[]
		{ 0, 0 };
		gridBagLayout.columnWeights = new double[]
		{ 0.0, 0.0, 1.0 };
		gridBagLayout.rowWeights = new double[]
		{ 1.0, Double.MIN_VALUE };
		this.setLayout(gridBagLayout);

		this.dstElement = new JLabel("");
		this.dstElement.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbcDstElement = new GridBagConstraints();
		gbcDstElement.anchor = GridBagConstraints.WEST;
		gbcDstElement.fill = GridBagConstraints.VERTICAL;
		gbcDstElement.insets = new Insets(5, 5, 0, 5);
		gbcDstElement.gridx = 0;
		gbcDstElement.gridy = 0;
		this.add(this.dstElement, gbcDstElement);

		JLabel lblArrow = new JLabel("<html><body>&#x1F872;</body></html>");
		lblArrow.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbcLblArrow = new GridBagConstraints();
		gbcLblArrow.fill = GridBagConstraints.VERTICAL;
		gbcLblArrow.insets = new Insets(5, 5, 0, 5);
		gbcLblArrow.gridx = 1;
		gbcLblArrow.gridy = 0;
		this.add(lblArrow, gbcLblArrow);

		this.hubElement = new JLabel("");
		this.hubElement.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbcHubElement = new GridBagConstraints();
		gbcHubElement.insets = new Insets(5, 5, 0, 5);
		gbcHubElement.fill = GridBagConstraints.BOTH;
		gbcHubElement.gridx = 2;
		gbcHubElement.gridy = 0;
		this.add(this.hubElement, gbcHubElement);
	}

	/**
	 * Returns a component that has been configured to display the specified value. That component's paint method is then called to"render" the cell. 
	 * If it is necessary to compute the dimensions of a list because the list cells do not have a fixed size, 
	 * this method is called to generate a component on which getPreferredSizecan be invoked.
	 * 
	 * @param list The JList we're painting.
	 * @param value The value returned by list.getModel().getElementAt(index).
	 * @param index The cells index.
	 * @param isSelected True if the specified cell was selected.
	 * @param cellHasFocus True if the specified cell has the focus.
	 * @return A component whose paint() method will render the specified value.
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends MappedElementRowViewModel<? extends Thing, ?>> list,
			MappedElementRowViewModel<? extends Thing, ?> value, int index, boolean isSelected, boolean cellHasFocus)
	{
		this.dstElement.setText(value.GetDstElementRepresentation());
		this.hubElement.setText(value.GetHubElementRepresentation());

		this.UpdateRowStatus(value);

		value.GetShouldCreateNewTargetElement().subscribe(x -> this.UpdateLabelsAndStatus(value));

		value.GetIsSelectedObservable().subscribe(x -> this.SetIsSelected(list, x));

		this.SetIsSelected(list, isSelected);

		return this;
	}

	/**
	 * Updates the provided {@linkplain MappedElementRowViewModel} row status
	 * 
	 * @param rowViewModel the {@linkplain MappedElementRowViewModel} row view model
	 */
	private void UpdateRowStatus(MappedElementRowViewModel<? extends Thing, ?> rowViewModel)
	{
		if (rowViewModel.GetRowStatus() != null)
		{
			switch (rowViewModel.GetRowStatus())
			{
			case ExisitingElement:
				this.dstElement.setForeground(Color.decode("#17418f"));
				this.hubElement.setForeground(Color.decode("#17418f"));
				break;
			case ExistingMapping:
				this.dstElement.setForeground(Color.decode("#a8601d"));
				this.hubElement.setForeground(Color.decode("#a8601d"));
				break;
			case NewElement:
				this.dstElement.setForeground(Color.decode("#226b1e"));
				this.hubElement.setForeground(Color.decode("#226b1e"));
				break;
			default:
				this.dstElement.setForeground(Color.BLACK);
				this.hubElement.setForeground(Color.BLACK);
				break;
			}
		}
	}

	/**
	 * Updates the labels and status of this row
	 * 
	 * @param the {@linkplain MappedElementRowViewModel}
	 */
	private void UpdateLabelsAndStatus(MappedElementRowViewModel<? extends Thing, ?> rowViewModel)
	{
		SwingUtilities.invokeLater(() -> {
			this.dstElement.setText(rowViewModel.GetDstElementRepresentation());
			this.hubElement.setText(rowViewModel.GetHubElementRepresentation());

			this.UpdateRowStatus(rowViewModel);
		});
	}

	/**
	 * Updates the selection highlights colors of this row
	 * 
	 * @param list The JList we're painting.
	 * @param isSelected True if the specified cell was selected.
	 */
	private void SetIsSelected(JList<? extends MappedElementRowViewModel<? extends Thing, ?>> list, boolean isSelected)
	{
		if (isSelected)
		{
			this.setBackground(list.getSelectionBackground());
			this.setForeground(list.getSelectionForeground());
		} else
		{
			this.setBackground(list.getBackground());
			this.setForeground(list.getForeground());
		}
	}
}
