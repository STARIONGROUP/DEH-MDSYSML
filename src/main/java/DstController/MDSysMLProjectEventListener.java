/*
 * MDSysMLProjectEventListener.java
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
import com.nomagic.magicdraw.core.project.ProjectEventListener;

import Reactive.ObservableValue;

public final class MDSysMLProjectEventListener implements ProjectEventListener
{
    /**
     * Gets a reactive value indicating if Cameo/MagicDraw has an open document
     */
    protected final ObservableValue<Boolean> HasOneDocumentOpenObservable = new ObservableValue<Boolean>(false, Boolean.class);
    
    /**
     * Gets a reactive {@linkplain Project}
     */
    protected final ObservableValue<Project> OpenDocumentObservable = new ObservableValue<Project>(Project.class);
    
    @Override
    public void projectClosed(Project project)
    {
        this.HasOneDocumentOpenObservable.Value(false);
        this.OpenDocumentObservable.Value(null);
    }

    @Override
    public void projectCreated(Project project)
    {
        this.HasOneDocumentOpenObservable.Value(true);
        this.OpenDocumentObservable.Value(project);
    }

    @Override
    public void projectOpened(Project project)
    {
        this.HasOneDocumentOpenObservable.Value(true);
        this.OpenDocumentObservable.Value(project);
    }

    @Override
    public void projectOpenedFromGUI(Project project)
    {
        this.HasOneDocumentOpenObservable.Value(true);
        this.OpenDocumentObservable.Value(project);
    }

    @Override
    public void projectReplaced(Project project, Project project2)
    {
        this.HasOneDocumentOpenObservable.Value(true);
        this.OpenDocumentObservable.Value(project2);    
    }

    @Override
    public void projectDeActivated(Project project) { }
    
    @Override
    public void projectActivated(Project project) { }

    @Override
    public void projectActivatedFromGUI(Project project) { }
    
    @Override
    public void projectPreActivated(Project project) { }

    @Override
    public void projectPreClosed(Project project) { }

    @Override
    public void projectPreClosedFinal(Project project) { }

    @Override
    public void projectPreDeActivated(Project project) { }

    @Override
    public void projectPreReplaced(Project project, Project project2) { }

    @Override
    public void projectPreSaved(Project project, boolean isSaved) { }

    @Override
    public void projectSaved(Project project, boolean isSaved) { }
}
