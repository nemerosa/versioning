package net.nemerosa.versioning.support

import java.util.regex.Matcher

class TagSupport {

    static int tagOrder(String tagPattern, String tagName) {
        Matcher m = tagName =~ tagPattern
        if (m.find()) {
            int ngroups = m.groupCount()
            if (ngroups < 1) {
                throw new IllegalArgumentException("Tag pattern is expected to have at least one number grouping instruction: $tagPattern")
            } else {
                return m.group(1) as int
            }
        } else {
            throw new IllegalStateException("Tag $tagName should have matched $tagPattern")
        }
    }

}
