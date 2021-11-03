/*
 * MagicDrawImpactViewPanel.java
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
package Views;

import Utils.ImageLoader.ImageLoader;
import ViewModels.Interfaces.IMagicDrawImpactViewPanelViewModel;
/**
 * The {@linkplain MagicDrawImpactViewPanel} is the {@linkplain HubBrowserPanel} for the MagicDraw / Cameo software
 */
@SuppressWarnings("serial")
public class MagicDrawImpactViewPanel extends MagicDrawBasePanel<IMagicDrawImpactViewPanelViewModel, ImpactViewPanel>
{
    /**
     * Initializes a new {@linkplain MagicDrawImpactViewPanel}
     */
    public MagicDrawImpactViewPanel()
    {
        super("DEH MagicDraw Adapter - ImpactView");
        this.setTabTitle("Impact");
        this.setFrameIcon(ImageLoader.GetIcon("icon16.png"));
        this.setDefaultCloseAction(CLOSE_ACTION_TO_HIDE);
        this.View = new ImpactViewPanel();
        this.getRootPane().getContentPane().add(this.View);
    }

    /**
     * Binds the <code>TViewModel viewModel</code> to the implementing view
     * 
     * @param <code>viewModel</code> the view model to bind
     */
    @Override
    public void Bind()
    {
       this.DataContext.GetIsSessionOpen().subscribe(x -> 
       {     
           this.View.SetSavedMappingconfigurationCollection(this.DataContext.GetSavedMappingconfigurationCollection());
       });
       
       this.View.AttachOnChangeDirection(this.DataContext.GetOnChangeMappingDirectionCallable());
       
       this.View.GetElementDefinitionBrowser().SetDataContext(this.DataContext.GetElementDefinitionImpactViewViewModel());
       this.View.GetRequirementBrowser().SetDataContext(this.DataContext.GetRequirementDefinitionImpactViewViewModel());
       this.View.BindNumberOfSelectedThingToTransfer(this.DataContext.GetTransferControlViewModel().GetNumberOfSelectedThing());
       this.View.SetContextMenuDataContext(this.DataContext.GetContextMenuViewModel());
       this.View.AttachOnTransfer(this.DataContext.GetTransferControlViewModel().GetOnTransferCallable());
    }
}