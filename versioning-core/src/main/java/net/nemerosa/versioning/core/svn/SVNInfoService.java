package net.nemerosa.versioning.core.svn;

import net.nemerosa.versioning.core.ProjectIntf;
import net.nemerosa.versioning.core.SCMInfo;
import net.nemerosa.versioning.core.SCMInfoService;
import net.nemerosa.versioning.core.VersioningConfig;
import net.nemerosa.versioning.core.support.ProcessExitException;
import net.nemerosa.versioning.core.support.XMLElement;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.nemerosa.versioning.core.support.Utils.run;

public class SVNInfoService implements SCMInfoService {

    private static final Pattern TRUNK_PATTERN = Pattern.compile(".*/trunk$");
    private static final Pattern BRANCH_PATTERN = Pattern.compile(".*/branches/([^/]+)$");

    @Override
    public SCMInfo getInfo(ProjectIntf project, VersioningConfig config) {
        // Is SVN enabled?
        boolean hasSvn = project.getFile(".svn").exists();
        // No SVN information
        if (!hasSvn) {
            return SCMInfo.NONE;
        } else {
            // Gets the SVN raw info as XML
            String xmlInfo = run(project.getRootDir(), "svn", "info", "--xml");
            // Parsing
            XMLElement info = XMLElement.parse(xmlInfo);
            XMLElement entry = info.get("entry");
            // URL
            String url = entry.get("url").getText();
            // Branch parsing
            String branch = parseBranch(url);
            // Revision
            String revision = entry.get("commit").getAttribute("revision");
            // OK
            return new SCMInfo(
                    branch,
                    revision,
                    revision,
                    "",
                    isWorkingCopyDirty(project.getRootDir())
            );
        }
    }

    public static boolean isWorkingCopyDirty(File dir) {
        // Gets the status as XML
        String xmlStatus = run(dir, "svn", "status", "--xml");
        // Parsing
        XMLElement status = XMLElement.parse(xmlStatus);
        // List of entries
        List<XMLElement> entries = status.get("target").getList("entry");
        if (entries.size() == 0) return false;
        // Checks every entry
        for (XMLElement entry : entries) {
            String path = entry.getAttribute("path");
            if (!StringUtils.equals("userHome", path)) {
                XMLElement wcStatus = entry.get("wc-status");
                String item = wcStatus.getAttribute("item");
                String props = wcStatus.getAttribute("props");
                if ((!StringUtils.equals(item, "none") && !StringUtils.equals(item, "external")) ||
                        !StringUtils.equals(props, "none")) {
                    return true;
                }
            }
        }
        return false;
    }

    static String parseBranch(String url) {
        if (TRUNK_PATTERN.matcher(url).matches()) {
            return "trunk";
        } else {
            Matcher m = BRANCH_PATTERN.matcher(url);
            if (m.matches()) {
                return m.group(1);
            } else {
                throw new SVNInfoURLException(url);
            }
        }
    }

    @Override
    public List<String> getBaseTags(ProjectIntf project, VersioningConfig config, String base) {
        // Gets the SVN raw info as XML
        String xmlInfo = run(project.getRootDir(), "svn", "info", "--xml");
        // Parsing
        XMLElement info = XMLElement.parse(xmlInfo);
        // URL
        String url = info.get("entry").get("url").getText();
        // Branch parsing
        String branch = parseBranch(url);
        // Gets the base URL by removing the branch
        String baseUrl;
        if (Objects.equals(branch, "trunk")) {
            baseUrl = StringUtils.difference(url, branch);
        } else {
            baseUrl = StringUtils.difference(url, "branches/" + branch);
        }
        // Gets the list of tags
        try {
            String tagsUrl = baseUrl + "/tags";
            project.log("Getting list of tags from %s...", tagsUrl);
            // Command arguments
            List<String> args = new ArrayList<>();
            args.add("list");
            args.add("--xml");
            args.add("--non-interactive");
            // Credentials
            if (StringUtils.isNotBlank(config.getUser())) {
                project.log("Authenticating with %s", config.getUser());
                args.add("--no-auth-cache");
                args.add("--username");
                args.add(config.getUser());
                args.add("--password");
                args.add(config.getPassword());
            }
            // Certificate
            if (config.isTrustServerCert()) {
                project.log("Trusting certificate by default");
                args.add("--trust-server-cert");
            }
            // Tags folder
            args.add(tagsUrl);
            // Command
            String xmlTags = run(project.getRootDir(), "svn", args);
            // Parsing
            XMLElement lists = XMLElement.parse(xmlTags);
            // Lists of tags, order from the most recent to the oldest
            List<XMLElement> entries = lists.get("list").getList("entry");
            Collections.sort(entries, new Comparator<XMLElement>() {
                @Override
                public int compare(XMLElement o1, XMLElement o2) {
                    long r1 = Long.parseLong(o1.get("commit").getAttribute("revision"), 10);
                    long r2 = Long.parseLong(o2.get("commit").getAttribute("revision"), 10);
                    return Long.compare(r2, r1);
                }
            });
            List<String> tags = new ArrayList<>();
            Pattern baseTagPattern = Pattern.compile("(" + base + "\\.(\\d+))");
            for (XMLElement entry : entries) {
                String tag = entry.get("name").getText();
                // Keeping only tags which fit the release pattern
                Matcher m = baseTagPattern.matcher(tag);
                if (m.find()) {
                    String match = m.group(1);
                    if (StringUtils.isNotBlank(match)) {
                        tags.add(match);
                    }
                }
            }
            // OK
            return tags;
        } catch (ProcessExitException ex) {
            if (ex.getExit() == 1 && ex.getMessage().contains("E200009")) {
                project.log("The tags/ folder does not exist yet");
                return Collections.emptyList();
            } else {
                throw ex;
            }
        }
    }

    @Override
    public String getBranchTypeSeparator() {
        return "-";
    }

}
