/*
 * MapAction.java
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
package Actions.Browser;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Utils.ImageLoader.ImageLoader;

import com.jidesoft.editor.selection.SelectionEvent;
import com.jidesoft.editor.selection.SelectionListener;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import DstController.IDstController;
import HubController.IHubController;
import Reactive.ObservableCollection;

/**
 * The {@link MapAction} is a {@link MDAction} that is to be added to the Cameo/Magic draw element browser context menu
 */
@SuppressWarnings("serial")
public class MapAction extends DefaultBrowserAction
{
    /**
     * The current class logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain IDstController}
     */
    private IDstController dstController;

    /**
     * the {@linkplain IHubController}
     */
    private IHubController hubController;
        
    /**
     * Initializes a new {@linkplain MapAction}
     * 
     * @param hubController the {@linkplain IHubController} 
     * @param dstController the {@linkplain IDstController}
     */
    public MapAction(IHubController hubController, IDstController dstController) 
    {
        super("Map Selection", "Map the current selection", KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK, true), null);
        this.setLargeIcon(ImageLoader.GetIcon("icon16.png"));
        this.dstController = dstController;
        this.hubController = hubController;
        this.SetIsEnabled(this.hubController.GetIsSessionOpen());
        this.hubController.GetIsSessionOpenObservable().subscribe(x -> this.SetIsEnabled(x));
    }

    /**
     * Sets a value indicating whether this action is enabled
     * 
     * @param shouldEnable a value switch that allows enabling this action
     */
    private void SetIsEnabled(boolean shouldEnable)
    {
        shouldEnable &= this.getTree() != null && this.getTree().getSelectedNodes().length > 0;
        this.logger.error(String.format("shouldEnable && this.tree != null && this.tree.getSelectedNodes().length > 0 =======> %s", shouldEnable));
        this.setEnabled(shouldEnable);
    }
    
    /**
    * Commands the {@link MDHubBrowserPanel} to show or hide
    * 
    * @param actionEvent The {@link ActionEvent} that originated the action performed. This parameter is unused.
    */
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {            
        try
        {
            ObservableCollection<Element> elements = this.SortSelectedElements();
            this.logger.error(String.format("this.dstController.Map(elements) ?? %s", this.dstController.Map(elements)));
        }
        catch (Exception exception) 
        {
            this.logger.error(String.format("MapAction actionPerformed has thrown an exception %s", exception));
            throw exception;
        }
    }

    /**
     * Sorts the selected element from the tree and return the correct sequence depending on what have been selected
     */
    private ObservableCollection<Element> SortSelectedElements()
    {
        ObservableCollection<Element> elements = new ObservableCollection<Element>(Element.class);
        
        Node[] nodes = this.getTree().getSelectedNodes();
                    
        for (Node node : nodes)
        {
            Object userObject = node.getUserObject();
            
            if (userObject instanceof Element)
            {
                Element element = (Element)userObject;
                
                if(element instanceof Model)
                {
                    elements.add(element);
                    break;
                }
                
                else if(element instanceof Package)
                {
                    if(elements.stream().allMatch(x -> x instanceof Package))
                    {
                        elements.add(element);
                    }
                }
                
                else if(element instanceof Class)
                {
                    if(elements.stream().allMatch(x -> x instanceof Class))
                    {
                        elements.add(element);
                    }
                }                
            }
        }
        
        return elements;
    }    
}

