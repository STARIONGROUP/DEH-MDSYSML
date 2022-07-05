/*
* TransferControlViewModelTest.java
*
* Copyright (c) 2020-2022 RHEA System S.A.
*
* Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski, Antoine Théate
*
* This file is part of DEH-CommonJ
*
* The DEH-CommonJ is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 3 of the License, or (at your option) any later version.
*
* The DEH-CommonJ is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program; if not, write to the Free Software Foundation,
* Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/
package ViewModels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import DstController.IDstController;
import Enumerations.MappingDirection;
import Reactive.ObservableCollection;
import Reactive.ObservableValue;
import Services.HistoryService.IMagicDrawLocalExchangeHistoryService;
import Services.MagicDrawUILog.IMagicDrawUILogService;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.ElementDefinition;

class TransferControlViewModelTest
{
	TransferControlViewModel viewModel;
	IDstController dstController;
	IMagicDrawUILogService logService;
	IMagicDrawLocalExchangeHistoryService exchangeHistory;
	ObservableCollection<Thing> selectedDstMapResult;
	ObservableCollection<Class> selectedHubMapResult;
	ObservableValue<MappingDirection> mappingDirection;
	Integer currentSelectedTransferCount;

	@BeforeEach
	void setUp()
	{
		this.selectedDstMapResult = new ObservableCollection<>();
		this.selectedHubMapResult = new ObservableCollection<>();
		this.mappingDirection = new ObservableValue<>(MappingDirection.FromDstToHub, MappingDirection.class);
		this.dstController = mock(IDstController.class);
		when(this.dstController.GetSelectedDstMapResultForTransfer()).thenReturn(this.selectedDstMapResult);
		when(this.dstController.GetSelectedHubMapResultForTransfer()).thenReturn(this.selectedHubMapResult);
		when(this.dstController.GetMappingDirection()).thenReturn(this.mappingDirection.Observable());
		when(this.dstController.CurrentMappingDirection()).thenReturn(this.mappingDirection.Value());

		this.logService = mock(IMagicDrawUILogService.class);
		this.exchangeHistory = mock(IMagicDrawLocalExchangeHistoryService.class);

		this.viewModel = new TransferControlViewModel(this.dstController, this.logService, this.exchangeHistory);
	}

	@Test
	void VerifyProperties()
	{
		assertNotNull(this.viewModel.GetNumberOfSelectedThing());
	}

	@Test
	void VerifyObservables()
	{
		this.viewModel.GetNumberOfSelectedThing().subscribe(x -> this.currentSelectedTransferCount = x);
		this.selectedHubMapResult.add(mock(Class.class));
		assertEquals(0, this.currentSelectedTransferCount);
		this.selectedDstMapResult.add(new ElementDefinition());
		assertEquals(1, this.currentSelectedTransferCount);
		this.selectedDstMapResult.clear();
		assertEquals(0, this.currentSelectedTransferCount);
		this.mappingDirection.Value(MappingDirection.FromHubToDst);
		assertEquals(1, this.currentSelectedTransferCount);
	}

	@Test
	void VerifyCallable() throws Exception
	{
		when(this.dstController.Transfer()).thenReturn(false, true);

		assertFalse(this.viewModel.GetOnTransferCallable().call().booleanValue());
		assertTrue(this.viewModel.GetOnTransferCallable().call().booleanValue());

		verify(this.logService, times(2)).Append(any(String.class));
		verify(this.logService, times(1)).Append(any(String.class), eq(true));
		verify(this.logService, times(1)).Append(any(String.class), eq(false));
		verify(this.exchangeHistory, times(2)).Write();
	}
}
