/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.util;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.PagedIterator;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class FileUtilTest {
    @Test
    public void test() {
        GHPullRequestFileDetail detail = new GHPullRequestFileDetail();
        ReflectionTestUtils.setField(detail, "filename", "I_am_a_file");
        FileUtil.saveFiles(new ArrayList<GHPullRequestFileDetail>(){{
            add(detail);
        }}, "", "", 1);
        ReflectionTestUtils.setField(detail, "patch", "+ a\n- b\n@@ -8,7 +8,6 @@\n c");
        Assertions.assertEquals("Projects//:::::Projects//delete",
                FileUtil.saveFiles(new ArrayList<GHPullRequestFileDetail>(){{
                    add(detail);
                }}, "", "", 1));
    }
}
