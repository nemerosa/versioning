package net.nemerosa.versioning.core.svn;

public class SVNInfoURLException extends RuntimeException {

    public SVNInfoURLException(String url) {
        super(String.format("The SVN URL cannot be identified as a `trunk` or a branch: %s", url));
    }
}
