/*
 * MagicDrawAdapterVersionNumberService.java
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
package Services.VersionNumber;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdp4common.Version;

/**
 * The {@linkplain MagicDrawAdapterVersionNumberService} is the MagicDraw specific implementation of the {@linkplain IAdapterVersionNumberService}
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class MagicDrawAdapterVersionNumberService extends AdapterVersionNumberService implements IAdapterVersionNumberService
{
    /**
     * Initializes a new {@linkplain AdapterVersionNumberService}
     */
    public MagicDrawAdapterVersionNumberService()
    {
        super(GetMagicDrawAdapterVersion());
    }

    /**
     * Gets the current version of the MagicDraw adapter
     * 
     * @return the {@linkplain Version}
     */
    private static Version GetMagicDrawAdapterVersion()
    {
        Logger logger = LogManager.getLogger();
        
        try
        {
            Properties properties = new Properties();
            
            properties.load(MagicDrawAdapterVersionNumberService.class
                    .getResourceAsStream("/META-INF/maven/com.rheagroup/DEHMDSYSMLPlugin/pom.properties"));
            
            return Version.parseVersion(properties.getProperty("version"));
        } 
        catch (Exception exception)
        {
            logger.catching(exception);
        }
        
        return Version.emptyVersion;
    }
}
