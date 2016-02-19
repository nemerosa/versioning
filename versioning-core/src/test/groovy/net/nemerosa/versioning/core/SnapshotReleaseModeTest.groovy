package net.nemerosa.versioning.core

import org.junit.Before
import org.junit.Test

class SnapshotReleaseModeTest {

    VersioningConfig config

    @Before
    void 'Before'() {
        config = new VersioningConfig()
    }

    @Test
    void 'Snapshot release mode returns current tag'() {
        assert "current" == new SnapshotReleaseMode().getDisplayVersion("next", "last", "current", config)
    }

    @Test
    void 'Snapshot release mode returns snapshot of next tag'() {
        assert "next-SNAPSHOT" == new SnapshotReleaseMode().getDisplayVersion("next", "last", "", config)
    }

    @Test
    void 'Snapshot release mode returns custom snapshot of next tag'() {
        config.snapshot = '-DEV'
        assert "next-DEV" == new SnapshotReleaseMode().getDisplayVersion("next", "last", "", config)
    }

}
