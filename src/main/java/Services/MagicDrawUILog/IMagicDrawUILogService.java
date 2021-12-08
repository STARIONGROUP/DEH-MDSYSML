/*
 * IMagicDrawUILogService.java
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
package Services.MagicDrawUILog;

import com.nomagic.magicdraw.ui.notification.NotificationSeverity;

/**
 * The {@linkplain IMagicDrawUILogService} is the interface definition for the service {@linkplain MagicDrawUILogService}
 */
public interface IMagicDrawUILogService
{

    /**
     * Appends a string message with the specified {@linkplain NotificationSeverity}
     * 
     * @param message the {@linkplain String} message to display
     * @param messageSeverity the {@linkplain NotificationSeverity}
     */
    void Append(String message, NotificationSeverity messageSeverity);

    /**
     * Appends a string message with the default severity {@linkplain NotificationSeverity.INFO}
     * 
     * @param message the {@linkplain String} message to display
     */
    void Append(String message);

    /**
     * Appends a string message with the default severity {@linkplain NotificationSeverity.INFO}
     * 
     * @param message the {@linkplain String} message to display
     * @param successStatus a value indicating the result of an action which the message describes
     */
    void Append(String message, boolean successStatus);

    /**
    * Appends a format-able string message using {@linkplain String.format(String, args)} with the specified {@linkplain NotificationSeverity}
    * 
    * @param message the {@linkplain String} message to display
    * @param messageSeverity the {@linkplain NotificationSeverity}
    * @param args Arguments referenced by the format specifiers in the format
    * string. If there are more arguments than format specifiers, the
    * extra arguments are ignored. The number of arguments is
    * variable and may be zero
    */
    void Append(String message, NotificationSeverity messageSeverity, Object... args);

    /**
    * Appends a format-able string message using {@linkplain String.format(String, args)} with the default severity {@linkplain NotificationSeverity.INFO}
    * 
    * @param message the {@linkplain String} message to display
    * @param args Arguments referenced by the format specifiers in the format
    * string. If there are more arguments than format specifiers, the
    * extra arguments are ignored. The number of arguments is
    * variable and may be zero
    */
    void Append(String message, Object... args);
}
