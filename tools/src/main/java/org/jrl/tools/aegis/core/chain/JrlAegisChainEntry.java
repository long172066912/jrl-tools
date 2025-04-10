package org.jrl.tools.aegis.core.chain;

import org.jrl.tools.aegis.core.JrlAegisEntry;

/**
 * 链路entry
 *
 * @author JerryLong
 */
public class JrlAegisChainEntry implements JrlAegisEntry {
    private final JrlAegisEntry entry;
    private final JrlAegisEntry next;

    public JrlAegisChainEntry(JrlAegisEntry entry, JrlAegisEntry next) {
        this.entry = entry;
        this.next = next;
    }

    @Override
    public void end() {
        if (null != this.entry) {
            this.entry.end();
        }
        if (null != this.next) {
            this.next.end();
        }
    }

    @Override
    public void end(Throwable error) {
        if (null != this.entry) {
            this.entry.end(error);
        }
        if (null != this.next) {
            this.next.end(error);
        }
    }

    public static void end(JrlAegisEntry entry) {
        if (null != entry) {
            entry.end();
        }
    }

    public static void end(JrlAegisEntry entry, Throwable error) {
        if (null != entry) {
            if (null != error) {
                entry.end(error);
            } else {
                entry.end();
            }
        }
    }
}