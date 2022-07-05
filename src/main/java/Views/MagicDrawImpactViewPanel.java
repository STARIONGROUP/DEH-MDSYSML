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
import Views.ContextMenu.ImpactViewContextMenu;
import io.reactivex.Observable;

/**
 * The {@linkplain MagicDrawImpactViewPanel} is the {@linkplain HubBrowserPanel} for the MagicDraw / Cameo software
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MagicDrawImpactViewPanel extends MagicDrawBasePanel<IMagicDrawImpactViewPanelViewModel, ImpactViewPanel>
{
    /**
     * The {@linkplain MagicDrawObjectBrowser} to display impact on SysML model
     */
    private MagicDrawObjectBrowser magicDrawObjectBrowser;
    
    /**
     * The {@linkplain ImpactViewContextMenu} context menu view for the MagicDraw impact view
     */
    private ImpactViewContextMenu magicDrawContextMenu;

    /**
     * Initializes a new {@linkplain MagicDrawImpactViewPanel}
     */
    public MagicDrawImpactViewPanel()
    {
        super("DEH MagicDraw Adapter - ImpactView");
        this.setTabTitle("Impact");
        this.setFrameIcon(ImageLoader.GetIcon("icon16.png"));
        this.setDefaultCloseAction(CLOSE_ACTION_TO_HIDE);
        this.view = new ImpactViewPanel();
        this.getRootPane().getContentPane().add(this.view);
        this.magicDrawObjectBrowser = new MagicDrawObjectBrowser();
        this.view.SetDstImpactViewView(this.magicDrawObjectBrowser);
        this.magicDrawContextMenu = new ImpactViewContextMenu();
    }

    /**
     * Binds the <code>TViewModel viewModel</code> to the implementing view
     * 
     * @param <code>viewModel</code> the view model to bind
     */
    @Override
    public void Bind()
    {
        this.view.SetLoadMappingControlsIsEnable(this.dataContext.CanLoadMappingConfiguration());
        
        if(this.dataContext.CanLoadMappingConfiguration())
        {
            this.view.SetSavedMappingconfigurationCollection(this.dataContext.GetSavedMappingconfigurationCollection());
        }
        
        this.dataContext.GetIsSessionOpen().subscribe(x -> this.view.SetSavedMappingconfigurationCollection(this.dataContext.GetSavedMappingconfigurationCollection()));
        
        Observable.combineLatest(this.dataContext.GetHasOneMagicDrawModelOpen(), this.dataContext.GetIsSessionOpen(),
                (hasOneMagicDrawModelOpen, isHubSessionOpen) -> hasOneMagicDrawModelOpen && isHubSessionOpen)
               .subscribe(x -> this.view.SetLoadMappingControlsIsEnable(x));
       
        this.view.AttachOnSaveLoadMappingConfiguration(x -> this.dataContext.OnSaveLoadMappingConfiguration(x));
        
        this.view.AttachOnChangeDirection(this.dataContext.GetOnChangeMappingDirectionCallable());

        this.view.GetElementDefinitionBrowser().SetDataContext(this.dataContext.GetElementDefinitionImpactViewViewModel());
        this.view.GetRequirementBrowser().SetDataContext(this.dataContext.GetRequirementDefinitionImpactViewViewModel());
        this.magicDrawObjectBrowser.SetDataContext(this.dataContext.GetMagicDrawImpactViewViewModel());
        this.magicDrawContextMenu.SetDataContext(this.dataContext.GetContextMenuViewModel());
        this.magicDrawObjectBrowser.SetContextMenu(this.magicDrawContextMenu);
        this.view.BindNumberOfSelectedThingToTransfer(this.dataContext.GetTransferControlViewModel().GetNumberOfSelectedThing());
        this.view.SetContextMenuDataContext(this.dataContext.GetContextMenuViewModel());
        this.view.AttachOnTransfer(this.dataContext.GetTransferControlViewModel().GetOnTransferCallable());
    }
}
