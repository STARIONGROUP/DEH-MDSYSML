/*
 * DstMappingConfigurationDialog.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import Renderers.MappedElementRowViewModelRenderer;
import Utils.ImageLoader.ImageLoader;
import ViewModels.Dialogs.Interfaces.IDstMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.IViewModel;
import ViewModels.Rows.MappedElementRowViewModel;
import Views.MagicDrawObjectBrowser;
import Views.Interfaces.IDialog;
import Views.ObjectBrowser.ObjectBrowser;
import cdp4common.commondata.ClassKind;
import cdp4common.commondata.Thing;

/**
 * The {@linkplain DstMappingConfigurationDialog} is the dialog view to allow to configure a mapping 
 * to be defined between a selection of DST elements and the hub element
 */
@SuppressWarnings("serial")
public class DstMappingConfigurationDialog extends JDialog implements IDialog<IDstMappingConfigurationDialogViewModel, Boolean>
{
    /**
     * The current class log4J {@linkplain Logger}
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * Backing field for {@linkplain GetDialogResult()}
     */
    private Boolean dialogResult;

    /**
     * This view attached {@linkplain IViewModel}
     */
    private IDstMappingConfigurationDialogViewModel dataContext;
    
    /**
     * The {@linkplain ObjectBrowser} view for {@linkplain ElementDefinition}
     */
    private ObjectBrowser elementDefinitionBrowser;

    /**
     * The {@linkplain ObjectBrowser} view for {@linkplain RequirementsSpecification}
     */
    private ObjectBrowser requirementBrowser;

    /**
     * View components declarations
     */
    private final JPanel contentPanel = new JPanel();
    private JButton okButton;
    private JButton cancelButton;
    private JSplitPane browserSplitPane;
    private MagicDrawObjectBrowser magicDrawObjectBrowser;
    private JList<MappedElementRowViewModel<? extends Thing, Class>> mappedElementListView;
    private DefaultListModel<MappedElementRowViewModel<? extends Thing, Class>> mappedElementSource;
    private boolean hasBeenPaintedOnce;
    private JSplitPane mainSplitPane;
    private JPanel panel;
    private JTabbedPane hubBrowserTreeViewsContainer;
    private JCheckBox mapToNewHubElementCheckBox;
    private JScrollPane scrollPane;
    private JButton resetButton;
    
    /**
     * Initializes a new {@linkplain DstMappingConfigurationDialog}
     */
    public DstMappingConfigurationDialog()
    {
        this.Initialize();
    }

    /**
     * Initializes this view visual components and properties
     */
    private void Initialize()
    {
        setTitle("SysML Mapping Configuration");
        setType(Type.POPUP);
        setModal(true);
        setBounds(100, 100, 549, 504);
        setMinimumSize(new Dimension(800, 600));
        this.setIconImage(ImageLoader.GetIcon("icon16.png").getImage());
        getContentPane().setLayout(new BorderLayout());
        this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(this.contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[] {};
        gbl_contentPanel.rowHeights = new int[] {0};
        gbl_contentPanel.columnWeights = new double[]{1.0};
        gbl_contentPanel.rowWeights = new double[]{1.0};
        contentPanel.setLayout(gbl_contentPanel);
        
        this.mainSplitPane = new JSplitPane();
        this.mainSplitPane.setDividerLocation(0.5);
        this.mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        
        this.browserSplitPane = new JSplitPane();
        this.browserSplitPane.setContinuousLayout(true);
        this.browserSplitPane.setDividerLocation(0.5);
        this.mainSplitPane.setLeftComponent(this.browserSplitPane);

        this.magicDrawObjectBrowser = new MagicDrawObjectBrowser();
        this.browserSplitPane.setLeftComponent(this.magicDrawObjectBrowser);
        
        panel = new JPanel();
        browserSplitPane.setRightComponent(panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{0, 0};
        gbl_panel.rowHeights = new int[]{0, 0, 0};
        gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);
        
        
        this.hubBrowserTreeViewsContainer = new JTabbedPane(SwingConstants.TOP);
        GridBagConstraints gbc_hubBrowserTreeViewsContainer = new GridBagConstraints();
        gbc_hubBrowserTreeViewsContainer.insets = new Insets(0, 0, 5, 0);
        gbc_hubBrowserTreeViewsContainer.fill = GridBagConstraints.BOTH;
        gbc_hubBrowserTreeViewsContainer.gridx = 0;
        gbc_hubBrowserTreeViewsContainer.gridy = 0;
        
        this.elementDefinitionBrowser = new ObjectBrowser();
        elementDefinitionBrowser.setBackground(Color.WHITE);
        hubBrowserTreeViewsContainer.addTab("Element Definitions", ImageLoader.GetIcon(ClassKind.Iteration), this.elementDefinitionBrowser, null);
        
        this.requirementBrowser = new ObjectBrowser();
        hubBrowserTreeViewsContainer.addTab("Requirements", ImageLoader.GetIcon(ClassKind.RequirementsSpecification), this.requirementBrowser, null);
        
        panel.add(this.hubBrowserTreeViewsContainer, gbc_hubBrowserTreeViewsContainer);
        
        mapToNewHubElementCheckBox = new JCheckBox("Map the current selected row to a new Hub element");
        mapToNewHubElementCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_mapToNewHubElementCheckBox = new GridBagConstraints();
        gbc_mapToNewHubElementCheckBox.anchor = GridBagConstraints.WEST;
        gbc_mapToNewHubElementCheckBox.gridx = 0;
        gbc_mapToNewHubElementCheckBox.gridy = 1;
        panel.add(mapToNewHubElementCheckBox, gbc_mapToNewHubElementCheckBox);
        
        JPanel mappedElementsPanel = new JPanel();
        mainSplitPane.setRightComponent(mappedElementsPanel);
        
        this.mappedElementSource = new DefaultListModel<>();
        GridBagLayout gbl_mappedElementsPanel = new GridBagLayout();
        gbl_mappedElementsPanel.columnWidths = new int[] {0};
        gbl_mappedElementsPanel.rowHeights = new int[] {0};
        gbl_mappedElementsPanel.columnWeights = new double[]{1.0};
        gbl_mappedElementsPanel.rowWeights = new double[]{1.0};
        mappedElementsPanel.setLayout(gbl_mappedElementsPanel);
        
        this.mappedElementListView = new JList<>(mappedElementSource);
        mappedElementListView.setToolTipText("<html><p>The collection of previously mapped elements (<span style=\"color: #E37814;\">Orange</span>),<br>as well as pre-mapped element to existing Hub element (<span style=\"color: #0E5AE8;\">Blue</span>),<br>and element mapped to \"to be created\" Hub element (<span style=\"color: #20B818;\">Green</span>)</P></html>");
        this.mappedElementListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.mappedElementListView.setSelectionBackground(new Color(104, 143, 184));
        this.mappedElementListView.setCellRenderer(new MappedElementRowViewModelRenderer());
        
        scrollPane = new JScrollPane(this.mappedElementListView);
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 0;
        mappedElementsPanel.add(scrollPane, gbc_scrollPane);
        
        GridBagConstraints gbc_splitPane = new GridBagConstraints();
        gbc_splitPane.fill = GridBagConstraints.BOTH;
        gbc_splitPane.gridx = 0;
        gbc_splitPane.gridy = 0;
        contentPanel.add(mainSplitPane, gbc_splitPane);
        
        JPanel buttonPane = new JPanel();
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        GridBagLayout gbl_buttonPane = new GridBagLayout();
        gbl_buttonPane.columnWidths = new int[] {1, 588, 1, 1, 1, 1};
        gbl_buttonPane.rowHeights = new int[]{23, 0};
        gbl_buttonPane.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_buttonPane.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        buttonPane.setLayout(gbl_buttonPane);
        
        resetButton = new JButton("Reset");
        resetButton.setToolTipText("Reset  to the default mapping.");
        GridBagConstraints gbc_resetButton = new GridBagConstraints();
        gbc_resetButton.insets = new Insets(0, 5, 10, 5);
        gbc_resetButton.gridx = 0;
        gbc_resetButton.gridy = 0;
        buttonPane.add(resetButton, gbc_resetButton);
        
        this.okButton = new JButton("Next");
        this.okButton.setToolTipText("Map the current elements");
        this.okButton.setActionCommand("OK");
        GridBagConstraints gbc_okButton = new GridBagConstraints();
        gbc_okButton.insets = new Insets(0, 5, 10, 5);
        gbc_okButton.gridx = 2;
        gbc_okButton.gridy = 0;
        buttonPane.add(this.okButton, gbc_okButton);
        
        this.cancelButton = new JButton("Cancel");
        this.cancelButton.setToolTipText("Close this dialog and abort the mapping");
        this.cancelButton.setActionCommand("Cancel");
        GridBagConstraints gbc_cancelButton = new GridBagConstraints();
        gbc_cancelButton.insets = new Insets(0, 5, 10, 5);
        gbc_cancelButton.gridx = 3;
        gbc_cancelButton.gridy = 0;
        buttonPane.add(this.cancelButton, gbc_cancelButton);
        
        this.addComponentListener(new ComponentAdapter() 
        {
            /**
             * Invoked when the component's size changes
             * 
             * @param componentEvent the {@linkplain ComponentEvent}
             */
            @Override
            public void componentResized(ComponentEvent componentEvent) 
            {
                super.componentResized(componentEvent);
                mainSplitPane.setDividerLocation(0.5);
                browserSplitPane.setDividerLocation(0.5);
            }
        });
    }
    
    /**
     * Binds the <code>TViewModel viewModel</code> to the implementing view
     * 
     * @param <code>viewModel</code> the view model to bind
     */
    public void Bind()
    {
        this.elementDefinitionBrowser.SetDataContext(this.dataContext.GetElementDefinitionBrowserViewModel());
        this.requirementBrowser.SetDataContext(this.dataContext.GetRequirementBrowserViewModel());
        this.magicDrawObjectBrowser.SetDataContext(this.dataContext.GetMagicDrawObjectBrowserViewModel());
        
        for (MappedElementRowViewModel<? extends Thing, Class> mappedElement : this.dataContext.GetMappedElementCollection())
        {
            this.mappedElementSource.addElement(mappedElement);
        }
        
        this.mappedElementListView.addListSelectionListener(x -> 
        {
            if(this.mappedElementListView.getSelectedValue() != null)
            {
                this.dataContext.SetSelectedMappedElement(this.mappedElementListView.getSelectedValue());
            }
        });
        
        this.dataContext.GetSelectedMappedElement().subscribe(x -> 
        {
            if(x != null)
            {
                this.mappedElementListView.setSelectedValue(x, true);
                this.RefreshMappedElementListView();
                this.UpdateMapToNewHubElementCheckBoxState(x.GetShouldCreateNewTargetElementValue());
            }
        });
        
        this.dataContext.GetMappedElementCollection().ItemAdded()
            .subscribe(x -> 
            {
                this.mappedElementSource.addElement(x);
                this.RefreshMappedElementListView();
            });
        
        this.dataContext.GetMappedElementCollection().ItemRemoved()
            .subscribe(x -> 
            {
                this.mappedElementSource.removeElement(x);
                this.RefreshMappedElementListView();
            });
        
        this.dataContext.GetMappedElementCollection().IsEmpty()
            .filter(x -> x)
            .subscribe(x -> 
            {
                this.mappedElementSource.clear();
                this.RefreshMappedElementListView();
            });
        
        this.mapToNewHubElementCheckBox.addActionListener(x -> 
        {
            this.dataContext.WhenMapToNewHubElementCheckBoxChanged(this.mapToNewHubElementCheckBox.isSelected());
        });
        
        this.dataContext.GetShouldMapToNewHubElementCheckBoxBeEnabled()
            .subscribe(x -> this.mapToNewHubElementCheckBox.setEnabled(x));
        
        this.okButton.addActionListener(x -> this.CloseDialog(true));        
        this.cancelButton.addActionListener(x -> this.CloseDialog(false));
        this.resetButton.addActionListener(x -> this.dataContext.ResetPreMappedThings());
    }

    /**
     * Refreshes the mapped element list view
     */
    private void RefreshMappedElementListView()
    {
        this.mappedElementListView.ensureIndexIsVisible(this.mappedElementSource.getSize());
    }
    
    /**
     * Updates the visual state of the {@linkplain mapToNewHubElementCheckBox} according to the selected mapped element
     * 
     * @param shouldCreateNewTargetElement the new value
     */
    private void UpdateMapToNewHubElementCheckBoxState(boolean shouldCreateNewTargetElement)
    {
        SwingUtilities.invokeLater(() -> this.mapToNewHubElementCheckBox.setSelected(shouldCreateNewTargetElement));        
    }

    /**
     * Sets the DataContext
     * 
     * @param viewModel the {@link IViewModel} to assign
     */
    public void SetDataContext(IViewModel viewModel)
    {
        this.dataContext = (IDstMappingConfigurationDialogViewModel) viewModel;
        this.Bind();
    }
    
    /**
    * Gets the DataContext
    * 
    * @return an {@link IViewModel}
    */
    @Override
    public IDstMappingConfigurationDialogViewModel GetDataContext()
    {
        return this.dataContext;
    }

    /**
     * Shows the dialog and return the result
     * 
     * @return a {@linkplain TResult}
     */
    @Override
    public Boolean ShowDialog()
    {
        this.setVisible(true);
        return this.dialogResult;
    }
    
    /**
     * Closes the dialog and sets the {@link dialogResult}
     * 
     * @param result the {@linkplain TResult} to set
     */
    @Override
    public void CloseDialog(Boolean result)
    {
        this.dialogResult = result;
        setVisible(false);
        dispose();
    }
    
    /**
     * Gets the {@linkplain dialogResult}
     * 
     * @return a {@linkplain Boolean}
     */
    public Boolean GetDialogResult()
    {
        return this.dialogResult;
    }

    /**
     * Paints the container. This forwards the paint to any lightweight components that are children of this container. 
     * If this method is re-implemented, super.paint(g) should be called so that lightweight components 
     * are properly rendered. If a child component is entirely clipped by the current clipping setting in g, 
     * paint() will not be forwarded to that child.
     * 
     * @param graphics the specified Graphics window
     */
    @Override
    public void paint(Graphics graphics) 
    {
        super.paint(graphics);

        if (!this.hasBeenPaintedOnce) 
        {
            this.hasBeenPaintedOnce = true;
            this.mainSplitPane.setDividerLocation(0.5);
            this.browserSplitPane.setDividerLocation(0.5);
        }
    }
}

