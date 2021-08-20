/*
 * DEHMDSYSMLPlugin.java
 *
 * Copyright (c) 2015-2019 RHEA System S.A.
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

package App;
import java.util.List;

import javax.swing.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ActionsID;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;

import App.AppContainer;
import ViewModels.HubBrowserPanelViewModel;
import ViewModels.Interfaces.IHubBrowserPanelViewModel;
import Actions.ToolBar.*;

public class DEHMDSYSMLPlugin extends Plugin
{
    /**
     * The current class logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * Main entry for the {@link DEH-MDSYSML} plugin
     */
    @Override
    public void init()
    {
        this.RegisterDependencies();

	    SwingUtilities.invokeLater(() -> 
	    {
	        try 
	        {
	            AMConfigurator configurator = new AMConfigurator()
                {
	                public void configure(ActionsManager manager)
	                {
	                   NMAction found = manager.getActionFor(ActionsID.NEW_PROJECT);
	                   
	                   if( found != null )
	                   {
	                        // find the category of the "New Project" action.
	                        ActionsCategory category = (ActionsCategory)manager.getActionParent(found);
	             	             
	                        // Get all actions from this category (menu).
	                        List<NMAction> actionsInCategory = category.getActions();
	                        
	                        //Add the action after the "New Project" action.
	                        int indexOfFound = actionsInCategory.indexOf(found);
	                        actionsInCategory.add(indexOfFound+1, new OpenHubBrowserPanelAction());
	             
	                        // Set all actions.
	                        category.setActions(actionsInCategory);
	                    }
	                };
                };
	            
                ActionsConfiguratorsManager.getInstance().addMainToolbarConfigurator(configurator);
               
			}
	        catch (Exception exception) 
	        {
				this.logger.error(String.format("MDSYSMLPlugin 'init' has thrown an exception %s \n\r %s", exception.toString(), exception.getStackTrace()));
				throw exception;
			}  
	    });
    }

    /**
     * Overriden method from {@linkplain Plugin}
     * Allows to perform operations when the plugin gets unloaded
     */
    @Override
    public boolean close()
    {
        return true;
    }
    
    /**
     * Overriden method from {@linkplain Plugin}
     * Allows to perform verification whether this plugin is supported by the Cameo or MagicDraw instance e.g. this plugin depends on another one
     */
    @Override
    public boolean isSupported()
    {
        return true;
    }
    
    /**
     * Registers the dependencies of the DEH-MDSYSML plugin into the {@linkplain AppContainer.Container}
     */
    public void RegisterDependencies()
    {
        try
        {
            AppContainer.Container.addComponent(IHubBrowserPanelViewModel.class, HubBrowserPanelViewModel.class);
        }
        catch (Exception exception) 
        {
            this.logger.error(String.format("MDSYSMLPlugin register dependencies has thrown an exception with %s \n\r %s", exception.toString(), exception.getStackTrace()));
            throw exception;
        }    
    }
}

