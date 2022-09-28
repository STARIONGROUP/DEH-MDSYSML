/*
 * AlertAcyclicDependencyDetectedDialogViewModel.java
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
package ViewModels.Dialogs;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import ViewModels.Dialogs.Interfaces.IAlertAcyclicDependencyDetectedDialogViewModel;
import ViewModels.Interfaces.IViewModel;

/**
 * The {@linkplain AlertAcyclicDependencyDetectedDialogViewModel} is the view model for {@linkplain AlertAcyclicDependencyDetectedDialog} 
 */
public class AlertAcyclicDependencyDetectedDialogViewModel implements IViewModel, IAlertAcyclicDependencyDetectedDialogViewModel
{
    /**
     * {@linkplain Map} where the key is a top node for each path where cyclic dependency was detected
     */
    private ArrayListMultimap<Class, Collection<NamedElement>> elements;
    
    /**
     * Gets the invalid elements to be displayed
     * 
     * @return a {@linkplain Map} where the key is a top node for each path where cyclic dependency was detected
     */
    @Override
    public ArrayListMultimap<Class, Collection<NamedElement>> GetInvalidElements()
    {
        return elements;
    }
    
    /**
     * Initializes a new {@linkplain AlertAcyclicDependencyDetectedDialogViewModel}
     * 
     * @param elements the {@linkplain Map} of invalid elements
     */
    public AlertAcyclicDependencyDetectedDialogViewModel(ArrayListMultimap<Class, Collection<NamedElement>> map)
    {
        this.elements = map;
    }
}
