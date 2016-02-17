package net.nemerosa.versioning.core;

import com.google.common.collect.ImmutableMap;
import net.nemerosa.versioning.core.git.GitInfoService;
import net.nemerosa.versioning.core.svn.SVNInfoService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultVersionService implements VersionService {

    /**
     * Registry of SCM info services
     */
    private static final Map<String, SCMInfoService> scmInfoServices = ImmutableMap.<String, SCMInfoService>builder()
            .put("git", new GitInfoService())
            .put("svn", new SVNInfoService())
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

        // Display version
        String versionDisplay;
        if (config.getReleases().contains(versionBranchType)) {
            List<String> baseTags = scmInfoService.getBaseTags(project, config, versionBase);
            versionDisplay = getDisplayVersion(config, scmInfo, branchInfo, baseTags);
        } else {
            // Adjusting the base
            String base = StringUtils.isNotBlank(versionBase) ? versionBase : versionBranchId;
            // Display mode
            versionDisplay = config.getDisplayMode().getDisplayVersion(
                    versionBranchType,
                    versionBranchId,
                    base,
                    scmInfo.getAbbreviated(),
                    versionFull,
                    config
            );
        }

        // OK
        return new VersionInfo(
                config.getScm(),
                scmInfo.getBranch(),
                versionBranchType,
                versionBranchId,
                scmInfo.getCommit(),
                versionDisplay,
                versionFull,
                versionBase,
                scmInfo.getAbbreviated()
        );
    }

    private String getDisplayVersion(VersioningConfig config, SCMInfo scmInfo, BranchInfo branchInfo, List<String> baseTags) {
        String currentTag = scmInfo.getTag();
        String lastTag;
        String nextTag;
        if (baseTags.isEmpty()) {
            lastTag = "";
            nextTag = branchInfo.getBase() + ".0";
        } else {
            lastTag = baseTags.get(0).trim();
            String regex = branchInfo.getBase() + "\\.(\\d+)";
            Matcher m = Pattern.compile(regex).matcher(lastTag);
            if (m.matches()) {
                int lastNumber = Integer.parseInt(m.group(1), 10);
                int newNumber = lastNumber + 1;
                nextTag = branchInfo.getBase() + "." + newNumber;
            } else {
                throw new VersioningException("Cannot parse last tag to get last version number: %s", lastTag);
            }
        }
        return config.getReleaseMode().getDisplayVersion(
                nextTag,
                lastTag,
                currentTag,
                config
        );
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
