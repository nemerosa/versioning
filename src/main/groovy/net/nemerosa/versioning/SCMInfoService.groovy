package net.nemerosa.versioning

import org.gradle.api.Project

interface SCMInfoService {

    SCMInfo getInfo(Project project)

}
