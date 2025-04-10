package org.jrl.utils.aegis;

import org.jrl.tools.aegis.JrlAegisContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JrlAegisContextTest {

    @Test
    public void test() {
        JrlAegisContext.enter("k", "v");
        JrlAegisContext.getContext().enter("k2", "v2");
        Assertions.assertEquals("v", JrlAegisContext.getContext().getRequest().get("k"));
        Assertions.assertEquals("v2", JrlAegisContext.getContext().getRequest().get("k2"));
        JrlAegisContext.clear();
        Assertions.assertNull(JrlAegisContext.getContext().getRequest().get("k2"));
    }
}
