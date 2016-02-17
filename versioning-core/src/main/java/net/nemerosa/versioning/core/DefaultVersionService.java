package net.nemerosa.versioning.core;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class DefaultVersionService implements VersionService {

    /**
     * Registry of SCM info services
     */
    private static final Map<String, SCMInfoService> scmInfoServices = ImmutableMap.<String, SCMInfoService>builder()
            // .put("git", new ...)
            .build();

    @Override
    public VersionInfo computeVersionInfo(ProjectIntf project, VersioningConfig config) {

        // Gets the SCM info service
        SCMInfoService scmInfoService = getSCMInfoService(config.getScm());

        // Gets the version source
        SCMInfo scmInfo = scmInfoService.getInfo(project, config);

        // No info?
        if (scmInfo.isEmpty()) {
            return VersionInfo.NONE;
        }

        // Version source
        String versionBranch = scmInfo.getBranch();

        // Branch parsing
        BranchInfo branchInfo = config.getBranchParser().parse(versionBranch, scmInfoService.getBranchTypeSeparator());
        String versionBranchType = branchInfo.getType();
        String versionBase = branchInfo.getBase();

        // Branch identifier
        String versionBranchId = normalise(versionBranch);

        // Full version
        String versionFull = config.getFullVersionBuilder().build(versionBranchId, scmInfo.getAbbreviated());

        // FIXME Method net.nemerosa.versioning.core.DefaultVersionService.computeVersionInfo
        return null;
    }

    private String normalise(String value) {
        return value.replaceAll("[^A-Za-z0-9\\.\\-_]", "-");
    }

    private static SCMInfoService getSCMInfoService(String type) {
        SCMInfoService scmInfoService = scmInfoServices.get(type);
        if (scmInfoService != null) {
            return scmInfoService;
        } else {
            throw new VersioningException("Unknown SCM info service: %s", type);
        }
    }

}
