/*
 * MappedElementDefinitionParameterListViewCellDisplayTemplate.java
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

import Utils.ValueSetUtils;
import ViewModels.Rows.MappedElementDefinitionRowViewModel;
import cdp4common.engineeringmodeldata.ActualFiniteState;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.Option;
import cdp4common.engineeringmodeldata.Parameter;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import java.awt.Insets;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * The {@linkplain MappedElementDefinitionParameterListViewCellDisplayTemplate} is the cell display template for {@linkplain Parameter}
 *  when the {@linkplain ElementDefinition} container has state dependent {@linkplain Parameter}
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MappedElementDefinitionParameterListViewCellDisplayTemplate extends JPanel
{
    /**
     * The dictionary of index in the comboBox and {@linkplain ActualFiniteState}
     */
    private HashMap<Integer, ActualFiniteState> indexedStates = new HashMap<>();
    
    /**
     * View components declaration
     */
    private JComboBox<String> statesValueSetComboBox;
    private JLabel parameterName;
    private GridBagConstraints valueConstraints;

    /**
     * Initializes a new {@linkplain MappedElementDefinitionListViewCellDisplayTemplate}
     * 
     * @param parameter the represented {@linkplain Parameter}
     * @param rowViewModel a {@linkplain BiConsumer} to run when the {@linkplain #statesValueSetComboBox} selected index changes
     */
    public MappedElementDefinitionParameterListViewCellDisplayTemplate(Parameter parameter, MappedElementDefinitionRowViewModel rowViewModel)
    {
        this.Initialize();
        this.Bind(parameter, rowViewModel);
    }

    /**
     * Binds this view components values to the provided {@linkplain Parameter}
     * 
     * @param parameter the {@linkplain Parameter}
     * @param rowViewModel the {@linkplain MappedElementDefinitionRowViewModel}
     */
    private void Bind(Parameter parameter, MappedElementDefinitionRowViewModel rowViewModel)
    {
        Option defaultOption = parameter.isOptionDependent() ? parameter.getContainerOfType(Iteration.class).getDefaultOption() : null;
     
        Component component;
        
        if(parameter.getStateDependence() == null)
        {
            component = new JLabel(ValueSetUtils.GetParameterValue(parameter, defaultOption, null));
        }
        else
        {
            this.statesValueSetComboBox = new JComboBox<>();
            
            ActualFiniteState stateToSelect = rowViewModel.GetSelectedActualFiniteStateFor(parameter.getIid());
            
            int indexToSelect = 0;
            
            for(ActualFiniteState state : parameter.getStateDependence().getActualState())
            {
                this.statesValueSetComboBox.addItem(String.format("%s %s", state.getName(), ValueSetUtils.GetParameterValue(parameter, defaultOption, state)));
                
                int stateIndex = this.statesValueSetComboBox.getItemCount() -1;
                
                this.indexedStates.put(stateIndex, state);
                
                if(stateToSelect != null && Utils.Operators.Operators.AreTheseEquals(stateToSelect.getIid(), state.getIid()) 
                        || stateToSelect == null && state.isDefault())
                {
                    indexToSelect = stateIndex;
                }        
            }

            this.statesValueSetComboBox.addActionListener(e -> 
                rowViewModel.SetActualFiniteStateFor(parameter.getIid(), indexedStates.get(statesValueSetComboBox.getSelectedIndex())));
            
            this.statesValueSetComboBox.setSelectedIndex(indexToSelect);
            
            component = this.statesValueSetComboBox;
        }
        
        this.parameterName.setText(parameter.getParameterType().getName());
        this.add(component, valueConstraints);
    }

    /**
     * Initializes this view components
     */
    private void Initialize()
    {
        this.setBackground(Color.WHITE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        this.setLayout(gridBagLayout);
        
        this.parameterName = new JLabel("New label");
        GridBagConstraints parameterNameConstraints = new GridBagConstraints();
        parameterNameConstraints.insets = new Insets(3, 3, 3, 3);
        parameterNameConstraints.anchor = GridBagConstraints.WEST;
        parameterNameConstraints.gridx = 0;
        parameterNameConstraints.gridy = 0;
        this.add(parameterName, parameterNameConstraints);
        
        this.valueConstraints = new GridBagConstraints();
        this.valueConstraints.insets = new Insets(3, 3, 3, 3);
        this.valueConstraints.fill = GridBagConstraints.HORIZONTAL;
        this.valueConstraints.gridx = 1;
        this.valueConstraints.gridy = 0;
    }    
}
