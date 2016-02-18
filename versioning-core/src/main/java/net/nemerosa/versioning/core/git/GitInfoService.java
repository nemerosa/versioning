package net.nemerosa.versioning.core.git;


import net.nemerosa.versioning.core.ProjectIntf;
import net.nemerosa.versioning.core.SCMInfo;
import net.nemerosa.versioning.core.SCMInfoService;
import net.nemerosa.versioning.core.VersioningConfig;
import net.nemerosa.versioning.core.support.ProcessExitException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.nemerosa.versioning.core.support.Utils.processOk;
import static net.nemerosa.versioning.core.support.Utils.run;

public class GitInfoService implements SCMInfoService {

    @Override
    public SCMInfo getInfo(ProjectIntf project, VersioningConfig config) {
        // Is Git enabled?
        boolean hasGit = project.getFile(".git").exists();
        // No Git information
        if (!hasGit) {
            return SCMInfo.NONE;
        }
        // Git information available
        else {
            // Gets the branch info
            String branch = run(project.getRootDir(), "git", "rev-parse", "--abbrev-ref", "HEAD");
            // Gets the commit info (full hash)
            String commit = run(project.getRootDir(), "git", "log", "-1", "--format=%H");
            // Gets the current commit (short hash)
            String abbreviated = run(project.getRootDir(), "git", "log", "-1", "--format=%h");
            // Gets the current tag, if any
            String tag;
            try {
                tag = run(project.getRootDir(), "git", "describe", "--tags", "--exact-match", "--always", "HEAD");
            } catch (ProcessExitException ex) {
                if (ex.getExit() == 128) {
                    tag = null;
                } else {
                    throw ex;
                }
            }
            // Returns the information
            return new SCMInfo(
                    branch,
                    commit,
                    abbreviated,
                    tag,
                    isGitTreeDirty(project.getRootDir())
            );
        }
    }

    public static boolean isGitTreeDirty(File dir) {
        return !processOk(dir, "git", "update-index", "-q", "--ignore-submodules", "--refresh") ||
                !processOk(dir, "git", "diff-files", "--quiet", "--ignore-submodules", "--") ||
                !processOk(dir, "git", "diff-index", "--cached", "--quiet", "HEAD", "--ignore-submodules", "--");
    }

    @Override
    public List<String> getBaseTags(ProjectIntf project, VersioningConfig config, String base) {
        String output = run(project.getRootDir(), "git", "log", "HEAD", "--pretty=oneline", "--decorate");
        List<String> tags = Arrays.asList(StringUtils.split(output, "\n"));
        return selectBaseTags(base, tags);
    }

    protected static List<String> selectBaseTags(String base, List<String> tags) {
        Pattern baseTagPattern = Pattern.compile(String.format("tag: (%s\\.(\\d+))", base));
        List<String> selection = new ArrayList<>();
        for (String tag : tags) {
            Matcher m = baseTagPattern.matcher(tag);
            if (m.find()) {
                String match = m.group(1);
                if (StringUtils.isNotBlank(match)) {
                    selection.add(match);
                }
            }
        }
        return selection;
    }

    @Override
    public String getBranchTypeSeparator() {
        return "/";
    }
}
