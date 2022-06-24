/*
 * IMagicDrawSessionService.java
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
package Services.MagicDrawSession;

import java.util.Collection;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import io.reactivex.Observable;

/**
 * The {@linkplain IMagicDrawSessionService} is the main interface definition
 * for {@linkplain MagicDrawSessionService}
 */
public interface IMagicDrawSessionService
{
	/**
	 * Gets an {@linkplain Observable} of value indicating whether there is any
	 * session open
	 * 
	 * @return an {@linkplain Observable} of {@linkplain Boolean}
	 */
	Observable<Boolean> HasAnyOpenSessionObservable();

	/**
	 * Gets the value emitted by {@linkplain HasAnyOpenSessionObservable} indicating
	 * whether there is any session open
	 * 
	 * @return a {@linkplain Boolean} value
	 */
	boolean HasAnyOpenSession();

	/**
	 * Gets the {@linkplain Observable} of {@linkplain Boolean} that indicates when
	 * the emitted Project gets saved
	 * 
	 * @return an {@linkplain Observable} of {@linkplain Boolean}
	 */
	Observable<Boolean> SessionUpdated();

	/**
	 * Gets the {@linkplain Project} from the {@linkplain Session}
	 * 
	 * @return a {@linkplain Project}
	 */
	Project GetProject();

	/**
	 * Gets the {@linkplain Project} name from the {@linkplain Session}
	 * 
	 * @return a name of the {@linkplain Project}
	 */
	String GetProjectName();

	/**
	 * Gets the open project element
	 * 
	 * @return a {@linkplain Collection} of {@linkplain Element}
	 */
	Collection<Element> GetProjectElements();

	/**
	 * Gets the project root package
	 * 
	 * @return a {@linkplain Package}
	 */
	Package GetModel();
}
