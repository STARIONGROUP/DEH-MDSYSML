/*
 * IMagicDrawProjectEventListener.java
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
package DstController;

import com.nomagic.magicdraw.core.Project;

import Reactive.ObservableValue;
import io.reactivex.Observable;

/**
 * The {@linkplain IMagicDrawProjectEventListener} is the interface definition for {@linkplain MagicDrawProjectEventListener}
 */
public interface IMagicDrawProjectEventListener
{
    /**
     * Gets a reactive value indicating that the open document has been saved
     */
    Observable<Boolean> ProjectSaved();

    /**
     * Gets a reactive value indicating if Cameo/MagicDraw has an open document
     */
    ObservableValue<Boolean> HasOneDocumentOpen();

    /**
     * Gets an {@linkplain ObservableValue} of type {@linkplain Project}
     */
    ObservableValue<Project> OpenDocument();
}
