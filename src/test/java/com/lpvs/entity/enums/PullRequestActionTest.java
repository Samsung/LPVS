package com.lpvs.entity.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class PullRequestActionTest {

    @Test
    public void testConvertFrom() {
        assertEquals(PullRequestAction.convertFrom("opened"), PullRequestAction.OPEN);
        assertEquals(PullRequestAction.convertFrom("reopened"), PullRequestAction.REOPEN);
        assertEquals(PullRequestAction.convertFrom("closed"), PullRequestAction.CLOSE);
        assertEquals(PullRequestAction.convertFrom("synchronize"), PullRequestAction.UPDATE);
        assertEquals(PullRequestAction.convertFrom("rescan"), PullRequestAction.RESCAN);

        assertNotEquals(PullRequestAction.convertFrom("random_name"), PullRequestAction.OPEN);
        assertNotEquals(PullRequestAction.convertFrom("random_name"), PullRequestAction.REOPEN);
        assertNotEquals(PullRequestAction.convertFrom("random_name"), PullRequestAction.CLOSE);
        assertNotEquals(PullRequestAction.convertFrom("random_name"), PullRequestAction.UPDATE);
        assertNotEquals(PullRequestAction.convertFrom("random_name"), PullRequestAction.RESCAN);

        assertNull(PullRequestAction.convertFrom("random_name"));

    }

    @Test
    public void testGetPullRequestAction() {
        assertEquals(PullRequestAction.OPEN.getPullRequestAction(), "opened");
        assertEquals(PullRequestAction.REOPEN.getPullRequestAction(), "reopened");
        assertEquals(PullRequestAction.CLOSE.getPullRequestAction(), "closed");
        assertEquals(PullRequestAction.UPDATE.getPullRequestAction(), "synchronize");
        assertEquals(PullRequestAction.RESCAN.getPullRequestAction(), "rescan");
    }
}
