/*
 * AlertMoreThanOneCapellaModelOpenDialog.java
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
package Views.Dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import Utils.ImageLoader.ImageLoader;
import ViewModels.Dialogs.Interfaces.IAlertAcyclicDependencyDetectedDialogViewModel;
import ViewModels.Interfaces.IViewModel;
import Views.Interfaces.IDialog;

/**
 * The {@linkplain AlertAcyclicDependencyDetectedDialog} is the dialog where the user gets notify when more than one Capella model is open
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class AlertAcyclicDependencyDetectedDialog extends BaseDialog<Boolean> implements IDialog<IAlertAcyclicDependencyDetectedDialogViewModel, Boolean>
{
    /**
     * This view attached {@linkplain AlertMoreThanOneCapellaModelOpenDialogViewModel} view model
     */
    private transient IAlertAcyclicDependencyDetectedDialogViewModel dataContext;
    
    /**
     * The base message that needs to be formatted
     */
    private String baseMessage = "<html><p style=\"text-align: center;\">The DEH MagicDraw SysML / Cameo System Modeler Adapter detected cyclic dependencies in the current SysML model.</p>\r\n<p style=\"text-align: center;\">The following elements will be ignored from mapping as they won't be accepted in the Hub.</p><p style=\"text-align: center;\">&nbsp;</p><p style=\"text-align: center;\">%s</p></html>";
    
    /**
     * View components declaration
     */
    private JButton okButton;
    private JLabel message;

    /**
     * Initializes a new {@linkplain AlertAcyclicDependencyDetectedDialog}
     */
    public AlertAcyclicDependencyDetectedDialog() 
    {
        this.Initialize();
    }

    /**
     * Initializes this view components 
     */
    private void Initialize()
    {
        this.setTitle("DEH MagicDraw SysML / Cameo System Modeler Adapter warning");
        this.setSize(600, 448);
        this.setLocationRelativeTo(null);
        this.setIconImage(ImageLoader.GetIcon().getImage());
        this.setType(Type.POPUP);
        this.setModal(true);
        
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 150, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
        this.getContentPane().setLayout(gridBagLayout);
        
        this.message = new JLabel();
        
        JScrollPane scrollableTextArea = new JScrollPane(this.message);
  
        GridBagConstraints gbc_message = new GridBagConstraints();
        gbc_message.fill = GridBagConstraints.BOTH;
        gbc_message.gridwidth = 2;
        gbc_message.insets = new Insets(5, 5, 5, 5);
        gbc_message.gridx = 0;
        gbc_message.gridy = 0;
        this.getContentPane().add(scrollableTextArea, gbc_message);
                
        this.okButton = new JButton("OK");
        GridBagConstraints gbc_okButton = new GridBagConstraints();
        gbc_okButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_okButton.insets = new Insets(5, 5, 5, 5);
        gbc_okButton.gridx = 1;
        gbc_okButton.gridy = 2;
        this.getContentPane().add(this.okButton, gbc_okButton);
    }
    
    /**
     * Generates a HTML list of faulty elements
     * 
     * @return a HTML string
     */
    private String GenerateListOfFaultyElements()
    {
        StringBuilder faultyElementsList = new StringBuilder();
        
        for (Entry<Class, Collection<NamedElement>> entry : this.dataContext.GetInvalidElements().entries())
        {
            faultyElementsList.append("<p>");
            this.AppendToFaultyElementList(faultyElementsList, entry.getKey());
            
            for (NamedElement element : entry.getValue())
            {
                this.AppendToFaultyElementList(faultyElementsList, element);
            }
            
            this.CloseList(faultyElementsList, entry.getValue().size() + 1);
        }
        
        return faultyElementsList.toString();
    }

    /**
     * Closes the HTML formatted string with the correct amount of closing tags
     * 
     * @param faultyElementsList the {@linkplain StringBuilder}
     * @param numberOfentry the number of entry
     */
    private void CloseList(StringBuilder faultyElementsList, int numberOfEntry)
    {
        for (int entryNumber = 0; entryNumber < numberOfEntry; entryNumber++)
        {
            faultyElementsList.append("</li></ul>");
        }

        faultyElementsList.append("</p>");
    }

    /**
     * Appends to the {@linkplain StringBuilder} the provided {@linkplain NamedElement} name
     * 
     * @param faultyElementsList the {@linkplain StringBuilder}
     * @param element the {@linkplain NamedElement}
     */
    private void AppendToFaultyElementList(StringBuilder faultyElementsList, NamedElement element)
    {
        faultyElementsList.append(String.format("<ul style=\"list-style-type:%s\">", element instanceof Class ? "square" : "circle"));
        faultyElementsList.append(String.format("<li>%s", element.getName()));
    }

    /**
     * Binds the {@linkplain #dataContext} viewModel to this view
     * 
     * @param viewModel the view model to bind
     */
    @Override
    public void Bind()
    {
        this.okButton.addActionListener(x -> this.CloseDialog(true));
        this.message.setText(String.format(baseMessage, this.GenerateListOfFaultyElements()));
    }

    /**
     * Closes the dialog and sets the {@link dialogResult}
     * 
     * @param result the {@linkplain TResult} to set
     */
    @Override
    public void CloseDialog(Boolean result)
    {
        super.CloseDialog(result);
    }
    
    /**
     * Sets the DataContext
     */
     @Override
    public void SetDataContext(IAlertAcyclicDependencyDetectedDialogViewModel viewModel)
    {
        this.dataContext = viewModel;
        this.Bind();        
    }

    /**
     * Gets the DataContext
     * 
     * @return an {@link IViewModel}
     */
    @Override
    public IAlertAcyclicDependencyDetectedDialogViewModel GetDataContext()
    {
        return this.dataContext;
    }
}
