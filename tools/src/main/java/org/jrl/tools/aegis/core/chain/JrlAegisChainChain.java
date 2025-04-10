package org.jrl.tools.aegis.core.chain;

import org.jrl.tools.aegis.exception.JrlAegisException;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.Callable;

/**
 * 链路本身的链路
 *
 * @author JerryLong
 */
public class JrlAegisChainChain extends JrlChain.AbstractZeusChain<JrlAegisExecutorChain, JrlAegisChainEntry> {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlAegisChainChain.class);


    public JrlAegisChainChain(int index, JrlAegisExecutorChain executor, JrlAegisChainChain next) {
        super(index, executor, next);
    }

    @Override
    public JrlAegisChainEntry execute() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("zeus-aegis ZeusAegisChainChain execute !");
        }
        JrlAegisChainEntry entry = null;
        //执行当前链路
        final JrlAegisExecutorChain chain = getExecutor();
        if (null != chain) {
            //执行链路，如果被拒绝，则抛出异常
            entry = chain.execute();
            //如果next不为空，继续执行
            final JrlChain<JrlAegisExecutorChain, JrlAegisChainEntry> next = getNext();
            if (null != next) {
                try {
                    return new JrlAegisChainEntry(entry, next.execute());
                } catch (JrlAegisException e) {
                    JrlAegisChainEntry.end(entry);
                    throw e;
                }
            }
        }
        return entry;
    }

    public <V> V execute(Callable<V> callable) {
        JrlAegisChainEntry entry = null;
        Throwable error = null;
        try {
            entry = this.execute();
            try {
                return callable.call();
            } catch (Throwable e) {
                error = e;
                throw new RuntimeException(e);
            }
        } finally {
            JrlAegisChainEntry.end(entry, error);
        }
    }

    @Override
    public ZeusChainStatus status() {
        return status;
    }

    @Override
    public void add(JrlAegisExecutorChain zeusAegisExecutorChain, int index) {
        if (index > this.getIndex()) {
            final JrlAegisExecutorChain thisExecutor = this.getExecutor();
            //创建next节点（是原本当前节点）
            final JrlAegisChainChain next = new JrlAegisChainChain(index, thisExecutor, (JrlAegisChainChain) this.getNext());
            //指向新节点
            this.setNext(next);
            //替换当前节点
            this.setIndex(index);
            this.setExecutor(zeusAegisExecutorChain);
        } else {
            this.getNext().add(zeusAegisExecutorChain, index);
        }
    }

    @Override
    public int addLast(JrlAegisExecutorChain zeusAegisExecutorChain) {
        if (this.getNext() == null) {
            final int index = this.getIndex() - 1;
            this.setNext(new JrlAegisChainChain(index, zeusAegisExecutorChain, null));
            return index;
        } else {
            return this.getNext().addLast(zeusAegisExecutorChain);
        }
    }

    @Override
    public JrlAegisChainChain addFirst(JrlAegisExecutorChain zeusAegisExecutorChain) {
        return new JrlAegisChainChain(this.getIndex() + 1, zeusAegisExecutorChain, this);
    }
}