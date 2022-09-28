/*
 * IAlertAcyclicDependencyDetectedDialogViewModel.java
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
package ViewModels.Dialogs.Interfaces;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import ViewModels.Dialogs.AlertAcyclicDependencyDetectedDialogViewModel;
import ViewModels.Interfaces.IViewModel;

/**
 * The {@linkplain IAlertAcyclicDependencyDetectedDialogViewModel} is the interface definition for {@linkplain AlertAcyclicDependencyDetectedDialogViewModel}
 */
public interface IAlertAcyclicDependencyDetectedDialogViewModel extends IViewModel
{

    /**
     * Gets the invalid elements to be displayed
     * 
     * @return a {@linkplain Map} where the key is a top node for each path where cyclic dependency was detected
     */
    ArrayListMultimap<Class, Collection<NamedElement>> GetInvalidElements();
}
