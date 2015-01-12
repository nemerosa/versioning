package net.nemerosa.versioning.svn

class SVNInfoURLException extends RuntimeException {

    SVNInfoURLException(String url) {
        super("The SVN URL cannot be identified as a `trunk` or a branch: ${url}")
    }
}
