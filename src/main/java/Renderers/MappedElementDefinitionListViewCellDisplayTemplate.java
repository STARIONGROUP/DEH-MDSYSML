/*
 * MappedElementDefinitionListViewCellDisplayTemplate.java
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

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.engineeringmodeldata.ActualFiniteState;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.Parameter;

import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.awt.GridLayout;

/**
 * The {@linkplain MappedElementDefinitionListViewCellDisplayTemplate} is the cell display template for the {@linkplain MappedElementDefinitionListViewCellRenderer}
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MappedElementDefinitionListViewCellDisplayTemplate extends JPanel
{
    /**
     * View elements declarations
     */
    private JPanel parameterContainer;
    private JLabel elemenDefinitionName;
    
    /**
     * Initializes a new {@linkplain MappedElementDefinitionListViewCellDisplayTemplate}
     */
    public MappedElementDefinitionListViewCellDisplayTemplate()
    {
        this.Initialize();
    }
    
    /**
     * Sets the view components value after the provided {@linkplain ElementDefinition} 
     * 
     * @param elementDefinition the {@linkplain ElementDefinition}
     * @param whenActualFiniteStateIsSelected a {@linkplain Runnable} to run when a {@linkplain ActualFiniteState} 
     * is selected for one of the {@linkplain ElementDefinition} {@linkplain Parameter}
     */
    public void UpdateProperties(MappedElementDefinitionRowViewModel rowViewModel)
    {
        if(rowViewModel.GetHubElement() == null)
        {
            return;
        }
        
        this.elemenDefinitionName.setText(rowViewModel.GetHubElement().getName());
        this.parameterContainer.removeAll();
        
        for (Parameter parameter : rowViewModel.GetHubElement().getParameter())
        {
            this.parameterContainer.add(new MappedElementDefinitionParameterListViewCellDisplayTemplate(parameter, rowViewModel));
        }        
    }

    /**
     * Initializes this view components
     */
    private void Initialize()
    {
        this.setBackground(Color.WHITE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        this.setLayout(gridBagLayout);
        
        this.elemenDefinitionName = new JLabel("New label");
        GridBagConstraints elemenDefinitionNameConstraints = new GridBagConstraints();
        elemenDefinitionNameConstraints.gridwidth = 2;
        elemenDefinitionNameConstraints.anchor = GridBagConstraints.WEST;
        elemenDefinitionNameConstraints.insets = new Insets(3, 3, 3, 0);
        elemenDefinitionNameConstraints.gridx = 0;
        elemenDefinitionNameConstraints.gridy = 0;
        add(elemenDefinitionName, elemenDefinitionNameConstraints);
        
        this.parameterContainer = new JPanel();
        this.parameterContainer.setLayout(new GridLayout(0, 1, 0, 5));
        this.parameterContainer.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints gbcList = new GridBagConstraints();
        gbcList.anchor = GridBagConstraints.NORTHEAST;
        gbcList.insets = new Insets(0, 0, 5, 0);
        gbcList.fill = GridBagConstraints.BOTH;
        gbcList.gridx = 1;
        gbcList.gridy = 1;
                
        JPanel flow = new JPanel();
        flow.setBackground(Color.WHITE);
        FlowLayout flFlow = new FlowLayout();
        flFlow.setAlignment(FlowLayout.LEFT);
        flow.setLayout(flFlow);
        flow.add(this.parameterContainer);
        scrollPane.add(flow);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setViewportView(flow);
        
        this.add(scrollPane, gbcList);
    }
}
