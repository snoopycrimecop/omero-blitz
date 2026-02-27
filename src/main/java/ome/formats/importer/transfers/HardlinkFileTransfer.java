/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.formats.importer.transfers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Local-only file transfer mechanism which makes use of hard-linking.
 *
 * @since 5.0
 */
public class HardlinkFileTransfer extends AbstractExecFileTransfer {

    /**
     * Creates a hard link; returns null.
     *
     * @param file File to be copied.
     * @param location Location to copy to.
     * @return null
     */
    protected ProcessBuilder createProcessBuilder(File file, File location) {
        try {
            Files.createLink(Paths.get(location.getAbsolutePath()), Paths.get(file.getAbsolutePath()));
        }
        catch (IOException e) {
        }
        return null;
    }

    /**
     * No cleanup action is taken.
     */
    public void afterTransfer(int errors, List<String> srcFiles) throws CleanupFailure {
        // no-op
    }
}
