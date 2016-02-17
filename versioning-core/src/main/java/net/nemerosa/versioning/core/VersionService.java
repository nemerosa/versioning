package net.nemerosa.versioning.core;

public interface VersionService {

    /**
     * Computes the information
     */
    VersionInfo computeVersionInfo(ProjectIntf project, VersioningConfig config);

}
