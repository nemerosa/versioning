package net.nemerosa.versioning.core;

import java.io.File;

/**
 * Interface to the project to get the version from.
 */
public interface ProjectIntf {

    /**
     * Gets the root directory
     */
    File getRootDir();

    /**
     * Gets a file
     */
    File getFile(String relativePath);

}
