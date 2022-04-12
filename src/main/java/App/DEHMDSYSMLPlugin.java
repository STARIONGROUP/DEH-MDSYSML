/*
 * DEHMDSYSMLPlugin.java
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

package App;

import static org.picocontainer.Characteristics.CACHE;

import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.picocontainer.Characteristics;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.actions.ActionsID;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.ui.browser.Tree;

import Actions.Browser.MapAction;
import Actions.ToolBar.LocalExchangeHistoryAction;
import Actions.ToolBar.MagicDrawAdapterRibbonActionCategory;
import Actions.ToolBar.OpenHubBrowserPanelAction;
import DstController.DstController;
import DstController.IDstController;
import DstController.IMagicDrawProjectEventListener;
import DstController.MagicDrawProjectEventListener;
import HubController.IHubController;
import MappingRules.BlockDefinitionMappingRule;
import MappingRules.RequirementMappingRule;
import Services.LocalExchangeHistory.ILocalExchangeHistoryService;
import Services.LocalExchangeHistory.LocalExchangeHistoryService;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import Services.MagicDrawUILog.MagicDrawUILogService;
import Services.MappingConfiguration.IMappingConfigurationService;
import Services.MappingConfiguration.MagicDrawMappingConfigurationService;
import Services.MappingEngineService.IMappingEngineService;
import Services.MappingEngineService.MappingEngineService;
import Services.VersionNumber.AdapterVersionNumberService;
import Services.VersionNumber.IAdapterVersionNumberService;
import Services.VersionNumber.MagicDrawAdapterVersionNumberService;
import Utils.ImageLoader.ImageLoader;
import ViewModels.ElementDefinitionImpactViewViewModel;
import ViewModels.HubBrowserPanelViewModel;
import ViewModels.MagicDrawImpactViewPanelViewModel;
import ViewModels.MagicDrawImpactViewViewModel;
import ViewModels.MagicDrawObjectBrowserViewModel;
import ViewModels.RequirementImpactViewViewModel;
import ViewModels.TransferControlViewModel;
import ViewModels.Dialogs.DstMappingConfigurationDialogViewModel;
import ViewModels.Dialogs.Interfaces.IDstMappingConfigurationDialogViewModel;
import ViewModels.Interfaces.IElementDefinitionImpactViewViewModel;
import ViewModels.Interfaces.IHubBrowserPanelViewModel;
import ViewModels.Interfaces.IMagicDrawImpactViewPanelViewModel;
import ViewModels.Interfaces.IMagicDrawImpactViewViewModel;
import ViewModels.Interfaces.IRequirementImpactViewViewModel;
import ViewModels.Interfaces.ITransferControlViewModel;
import ViewModels.MagicDrawObjectBrowser.Interfaces.IMagicDrawObjectBrowserViewModel;
import cdp4common.Version;

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
	                   
	                   if(found != null)
	                   {
	                        // find the category of the "New Project" action.
	                        ActionsCategory category = (ActionsCategory)manager.getActionParent(found);

	                        // Get all actions from this category (menu).
	                        List<NMAction> actionsInCategory = category.getActions();
	                        
	                        //Add the action after the "New Project" action.
	                        int indexOfFound = actionsInCategory.indexOf(found);
                            actionsInCategory.add(indexOfFound+1, new MagicDrawAdapterRibbonActionCategory());
	             
	                        // Set all actions.
	                        category.setActions(actionsInCategory);
	                    }
	                }
                };
	            
                ActionsConfiguratorsManager.getInstance().addMainToolbarConfigurator(configurator);
                
                MapAction mapAction = AppContainer.Container.getComponent(MapAction.class);
                
                BrowserContextAMConfigurator configuratorContext = new BrowserContextAMConfigurator()
                {
                    @Override
                    public int getPriority()
                    {
                        return 0;
                    }

                    @Override
                    public void configure(ActionsManager manager, Tree tree)
                    {
                        ActionsCategory category = new ActionsCategory(null, "DEH-MDSYSML action category");
                        category.setName("DEH-MDSYSML action category");
                        category.setSmallIcon(ImageLoader.GetIcon("icon16.png"));
                        manager.addCategory(manager.getCategories().size(), category);
                        List<NMAction> actionsInCategory = category.getActions();
                        actionsInCategory.add(actionsInCategory.size(), mapAction);
                        category.setActions(actionsInCategory);
                    }
                };
                
                ActionsConfiguratorsManager.getInstance().addContainmentBrowserContextConfigurator(configuratorContext);
                
                Application.getInstance().getGUILog().log(String.format("[MDSYSMLPlugin] %s Initialized with success!", AppContainer.Container.getComponent(IAdapterVersionNumberService.class).GetVersion()));
			}
	        catch (Exception exception)
	        {
				this.logger.error(String.format("MDSYSMLPlugin 'init' has thrown an exception %s %n %s", exception.toString(), exception.getStackTrace()));
				throw exception;
			}  
	    });
    }

    /**
     * Overridden method from {@linkplain Plugin}
     * Allows to perform operations when the {@linkplain Plugin} gets unloaded
     */
    @Override
    public boolean close()
    {
        AppContainer.Container.getComponent(IHubController.class).Close();
        AppContainer.Container.stop();
        return true;
    }
    
    /**
     * Overridden method from {@linkplain Plugin}
     * Allows to perform verification whether this {@linkplain Plugin} is supported by the Cameo or MagicDraw instance 
     * e.g. this {@linkplain Plugin} depends on another one
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
            AppContainer.Container.as(CACHE).addComponent(IDstController.class, DstController.class);
            AppContainer.Container.addComponent(IMagicDrawProjectEventListener.class, MagicDrawProjectEventListener.class);
            AppContainer.Container.addConfig(MappingEngineService.AssemblyParameterName, BlockDefinitionMappingRule.class.getPackage());
            AppContainer.Container.as(CACHE, Characteristics.USE_NAMES).addComponent(IMappingEngineService.class, MappingEngineService.class);
            AppContainer.Container.as(CACHE).addComponent(MapAction.class);
            AppContainer.Container.addComponent(BlockDefinitionMappingRule.class.getName(), BlockDefinitionMappingRule.class);
            AppContainer.Container.addComponent(RequirementMappingRule.class.getName(), RequirementMappingRule.class);

            AppContainer.Container.addComponent(IMappingConfigurationService.class, MagicDrawMappingConfigurationService.class);
            AppContainer.Container.addComponent(IMagicDrawUILogService.class, MagicDrawUILogService.class);
            AppContainer.Container.addComponent(IAdapterVersionNumberService.class, MagicDrawAdapterVersionNumberService.class);

            AppContainer.Container.addComponent(IElementDefinitionImpactViewViewModel.class, ElementDefinitionImpactViewViewModel.class);
            AppContainer.Container.addComponent(IRequirementImpactViewViewModel.class, RequirementImpactViewViewModel.class);
            AppContainer.Container.addComponent(IHubBrowserPanelViewModel.class, HubBrowserPanelViewModel.class);
            AppContainer.Container.addComponent(IMagicDrawImpactViewPanelViewModel.class, MagicDrawImpactViewPanelViewModel.class);
            AppContainer.Container.addComponent(ITransferControlViewModel.class, TransferControlViewModel.class);
            AppContainer.Container.addComponent(IDstMappingConfigurationDialogViewModel.class, DstMappingConfigurationDialogViewModel.class);
            AppContainer.Container.addComponent(IMagicDrawObjectBrowserViewModel.class, MagicDrawObjectBrowserViewModel.class);
            AppContainer.Container.addComponent(IMagicDrawImpactViewViewModel.class, MagicDrawImpactViewViewModel.class);
        }
        catch (Exception exception) 
        {
            this.logger.error(String.format("MDSYSMLPlugin register dependencies has thrown an exception with %s %n %s", exception.toString(), exception.getStackTrace()));
            throw exception;
        }
    }
}

