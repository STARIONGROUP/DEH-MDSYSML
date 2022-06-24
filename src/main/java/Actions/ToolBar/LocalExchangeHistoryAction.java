/*
 * LocalExchangeHistoryAction.java
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
package Actions.ToolBar;

import java.awt.event.ActionEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.magicdraw.actions.MDAction;

import App.AppContainer;
import Services.NavigationService.INavigationService;
import Utils.ImageLoader.ImageLoader;
import Views.MagicDrawHubBrowserPanel;
import Views.ExchangeHistory.ExchangeHistoryDialog;

/**
 * The {@linkplain LocalExchangeHistoryAction} is the {@link MDAction} that shows the {@linkplain ExchangeHistoryDialog}
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class LocalExchangeHistoryAction extends MDAction
{
    /**
     * The current class logger
     */
    private transient Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain INavigationService}
     */
    private final transient INavigationService navigationService;
    
    /**
     * Initializes a new {@link LocalExchangeHistoryAction}
     */
    public LocalExchangeHistoryAction()
    {
         super("View Local Exchange History", "View Local Exchange History", null, null);
         this.setLargeIcon(ImageLoader.GetIcon("icon16.png"));
         this.navigationService = AppContainer.Container.getComponent(INavigationService.class);
    }
    
    /**
     * Commands the {@link MagicDrawHubBrowserPanel} to show or hide
     * 
     * @param actionEvent The {@link ActionEvent} that originated the action performed. This parameter is unused.
     */
     @Override
     public void actionPerformed(ActionEvent actionEvent)
     {            
         try
         {
             this.navigationService.ShowDialog(new ExchangeHistoryDialog());
         }
         catch (Exception exception) 
         {
             this.logger.error(String.format("LocalExchangeHistoryAction actionPerformed has thrown an exception %s %n %s", exception.toString(), exception.getStackTrace()));
             throw exception;
         }
     }
}
