/*
 * TransferControlViewModel.java
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
package ViewModels;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

import DstController.IDstController;
import Enumerations.MappingDirection;
import Reactive.ObservableValue;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import ViewModels.Interfaces.ITransferControlViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain TransferControlViewModel} is the base abstract view model for the transfer control from the impact view panel
 */
public class TransferControlViewModel implements ITransferControlViewModel
{
    /**
     * The number of selected things to transfer
     */
    private ObservableValue<Integer> numberOfSelectedThings = new ObservableValue<Integer>(0, Integer.class);
    
    /**
     * Gets the number of selected things to transfer
     * 
     * @return an {@linkplain Observable} of {@linkplain Integer}
     */
    @Override
    public Observable<Integer> GetNumberOfSelectedThing()
    {
        return this.numberOfSelectedThings.Observable();
    }
    
    /**
     * The {@linkplain IDstController}
     */
    private IDstController dstController;
    
    /**
     * The {@linkplain IMagicDrawUILogService}
     */
    private IMagicDrawUILogService logService;

    /**
     * Initializes a new {@linkplain TransferControlViewModel}
     * 
     * @param dstController the {@linkplain IDstController}
     * @param logService the {@linkplain IMagicDrawUILogService}
     */
    public TransferControlViewModel(IDstController dstController, IMagicDrawUILogService logService)
    {
        this.dstController = dstController;
        this.logService = logService;
        
        this.dstController.GetSelectedDstMapResultForTransfer()
            .Changed()
            .subscribe(x -> this.UpdateNumberOfSelectedThing(this.dstController.CurrentMappingDirection()));
        
        this.dstController.GetMappingDirection()
            .subscribe(mappingDirection ->
            {
                this.UpdateNumberOfSelectedThing(mappingDirection);
            });
    }

    /**
     * Updates the {@linkplain numberOfSelectedThing} based on a given {@linkplain MappingDirection}
     * 
     * @param mappingDirection the {@linkplain MappingDirection}
     */
    private void UpdateNumberOfSelectedThing(MappingDirection mappingDirection)
    {
        this.numberOfSelectedThings.Value(mappingDirection == MappingDirection.FromDstToHub 
                ? this.dstController.GetSelectedDstMapResultForTransfer().size()
                : this.dstController.GetSelectedHubMapResultForTransfer().size());
    }
    
    /**
     * Gets a {@linkplain Callable} of {@linkplain Boolean} to call when the transfer button is pressed
     * 
     * @return a {@linkplain Callable} of {@linkplain Boolean}
     */
    @Override
    public Callable<Boolean> GetOnTransferCallable()
    {
       return () -> 
       {
           StopWatch timer = StopWatch.createStarted();
           
           this.logService.Append("Transfer in progress...");
           
           boolean result = this.dstController.Transfer();

           if(timer.isStarted())
           {
               timer.stop();
           }
           
           this.logService.Append(String.format("Transfer done in %s ms", timer.getTime(TimeUnit.MILLISECONDS)), result);
           
           return result;
       };
    }
}
