/*
 * MagicDrawUILogService.java
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

import java.util.Arrays;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.ui.notification.Notification;
import com.nomagic.magicdraw.ui.notification.NotificationSeverity;

/**
 * The {@linkplain MagicDrawUILogService} provides easy way to report status messages to the MagicDraw UI log
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MagicDrawUILogService implements IMagicDrawUILogService
{
    /**
     * The current class {@linkplain Logger}
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * Appends a string message with the default severity {@linkplain NotificationSeverity.INFO}
     * 
     * @param message the {@linkplain String} message to display
     */
    @Override
    public void Append(String message)
    {
        this.Append(message,  NotificationSeverity.INFO);
    }
    
    /**
    * Appends a format-able string message using {@linkplain String.format(String, args)} with the default severity {@linkplain NotificationSeverity.INFO}
    * 
    * @param message the {@linkplain String} message to display
    * @param args Arguments referenced by the format specifiers in the format
    * string. If there are more arguments than format specifiers, the
    * extra arguments are ignored. The number of arguments is
    * variable and may be zero
    */
    @Override
    public void Append(String message, Object... args)
    {
        this.Append(String.format(message, args),  NotificationSeverity.INFO);
    }
    
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
    @Override
    public void Append(String message, NotificationSeverity messageSeverity, Object... args)
    {
        this.Append(String.format(message, args),  messageSeverity);
    }
    
    /**
     * Appends a string message with the default severity {@linkplain NotificationSeverity.INFO}
     * 
     * @param message the {@linkplain String} message to display
     * @param successStatus a value indicating the result of an action which the message describes
     */
    @Override
    public void Append(String message, boolean successStatus)
    {
        if(successStatus)
        {
            this.Append(String.format("%s%s", message, " with success"), NotificationSeverity.INFO);
        }
        else
        {
            this.Append(String.format("%s %s", message, " with errors, check the MagicDraw log file for more details."), NotificationSeverity.ERROR);
        }
    }

    /**
     * Appends a string message with the specified {@linkplain NotificationSeverity}
     * 
     * @param message the {@linkplain String} message to display
     * @param messageSeverity the {@linkplain NotificationSeverity}
     */
    @Override
    public void Append(String message, NotificationSeverity messageSeverity)
    {
        Application.getInstance().getGUILog().logNotification(
                String.format("[MDSYSMLPlugin] [%s] %s", messageSeverity.getText(), message), messageSeverity, Notification.Context.ENVIRONMENT);
        
        String logMessage = String.format("%s %s ", this.GetCaller(), message);
        
        if(messageSeverity.equals(NotificationSeverity.INFO))
        {
            this.logger.info(logMessage);
        }
        else if(messageSeverity.equals(NotificationSeverity.WARNING))
        {
            this.logger.warn(logMessage);
        }
        else if(messageSeverity.equals(NotificationSeverity.ERROR))
        {
            this.logger.error(logMessage);
        }        
        
    }

    /**
     * Gets the caller class and line number as a string
     * 
     * @return a {@linkplain String}
     */
    private Object GetCaller()
    {
        try
        {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            
            Optional<StackTraceElement> stackElement = Arrays.asList(stackTrace).stream().skip(1).filter(x -> !x.getFileName().contains(this.getClass().getSimpleName())).findFirst();
            
            if(stackElement.isPresent())
            {
                return String.format("%s {Line %s} -", stackElement.get().getClassName(), 
                        stackElement.get().getLineNumber());
            }
        } 
        catch (Exception exception)
        {
            this.logger.catching(exception);
        }        
        
        return "";
    }
}
