/*
 * Copyright (C) 2019 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.repo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import loci.formats.IFormatReader;

import ome.api.IQuery;
import ome.conditions.SecurityViolation;
import ome.io.nio.ReaderSecurityCheck;
import ome.parameters.Parameters;
import ome.services.blitz.repo.path.FsFile;
import ome.services.blitz.repo.path.ServerFilePathTransformer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide OMERO permissions checking for Bio-Formats readers applied to managed repository filesets.
 * Cf. 2019-SV1.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.5.6
 */
public class ManagedReaderSecurityCheck implements ReaderSecurityCheck  {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedReaderSecurityCheck.class);

    private static final int BATCH_SIZE = 256;

    private final IQuery iQuery;
    private final Set<String> repoUuids;
    private final Map<String, ServerFilePathTransformer> repoServerPaths = new ConcurrentHashMap<>();

    /**
     * Construct a new thread-safe used-file security checker.
     * @param iQuery a query service for checking repository roots and image file accessibility
     * @param managedRepoUuids the UUIDs of the managed repositories; will not be taken as final
     */
    public ManagedReaderSecurityCheck(IQuery iQuery, Set<String> managedRepoUuids) {
       this.iQuery = iQuery;
       this.repoUuids = managedRepoUuids;
    }

    @Override
    public void assertUsedFilesReadable(IFormatReader reader) throws SecurityViolation {
        /* Check that we know all the managed repository roots.*/
        final Set<String> missingRepos = Sets.difference(repoUuids, repoServerPaths.keySet());
        if (!missingRepos.isEmpty()) {
            final String hql = "SELECT hash, path || name FROM OriginalFile " +
                    "WHERE hash IN (:repos) AND repo IS NULL and mimetype = 'Repository'";
            final Parameters parameters = new Parameters().addSet("repos", missingRepos);
            for (final Object[] result : iQuery.projection(hql, parameters)) {
                final String repo = (String) result[0];
                final String path = (String) result[1];
                final ServerFilePathTransformer serverPaths = new ServerFilePathTransformer();
                serverPaths.setBaseDirFile(new File(path));
                repoServerPaths.put(repo, serverPaths);
                LOGGER.info("noted managed repository {}", repo);
            }
        }
        LOGGER.debug("checking used files for " + reader.getCurrentFile());
        final String[] usedFiles = reader.getUsedFiles();
        final SetMultimap<String, String> repoPaths = HashMultimap.create();
        final List<String> badPaths = new ArrayList<>();
        /* Check that all this batch of used files are in a managed repository. */
        for (final String usedFile : usedFiles) {
            boolean isWithinRepo = false;
            for (final Map.Entry<String, ServerFilePathTransformer> repoServerPath : repoServerPaths.entrySet()) {
                final String repo = repoServerPath.getKey();
                final ServerFilePathTransformer serverPaths = repoServerPath.getValue();
                final FsFile serverPath;
                try {
                    serverPath = serverPaths.getFsFileFromServerFile(new File(usedFile).getAbsoluteFile());
                } catch (IllegalArgumentException iae) {
                    continue;
                }
                repoPaths.put(repo, serverPath.toString());
                isWithinRepo = true;
                break;
            }
            if (!isWithinRepo) {
                badPaths.add(usedFile);
            }
        }
        if (!badPaths.isEmpty()) {
            final StringBuilder errorBuilder = new StringBuilder();
            errorBuilder.append("reader for " + reader.getCurrentFile() + " accesses data outside managed repository:");
            for (final String badPath : badPaths) {
                errorBuilder.append("\n\t");
                errorBuilder.append(badPath);
            }
            final String error = errorBuilder.toString();
            LOGGER.warn(error);
            throw new SecurityViolation(error);
        }
        /* Check that all the used files' OMERO permissions have them readable by the current user. */
        final String hql = "SELECT COUNT(*) FROM OriginalFile WHERE repo = :repo AND path || name IN (:paths)";
        for (final Map.Entry<String, Collection<String>> pathsOneRepo : repoPaths.asMap().entrySet()) {
            final String repo = pathsOneRepo.getKey();
            for (final List<String> paths : Iterables.partition(pathsOneRepo.getValue(), BATCH_SIZE)) {
                final Parameters parameters = new Parameters().addString("repo", repo).addList("paths", new ArrayList<>(paths));
                final Object[] result = iQuery.projection(hql, parameters).get(0);
                if (paths.size() != (Long) result[0]) {
                    final String error = "reader for " + reader.getCurrentFile() + " uses file from repository " + repo +
                            " that is not readable from database";
                    LOGGER.warn(error);
                    throw new SecurityViolation(error);
                }
            }
        }
    }
}
