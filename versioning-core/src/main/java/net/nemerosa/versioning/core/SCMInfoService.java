package net.nemerosa.versioning.core;

import java.util.List;

public interface SCMInfoService {

    SCMInfo getInfo(ProjectIntf project, VersioningConfig config);

    List<String> getBaseTags(ProjectIntf project, VersioningConfig config, String base);

    String getBranchTypeSeparator();
}
