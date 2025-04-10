package org.jrl.tools.aegis.core.chain;

import org.jrl.tools.aegis.JrlAegisContext;
import org.jrl.tools.aegis.core.JrlAegisEntry;
import org.jrl.tools.aegis.core.JrlAegisExecutor;
import org.jrl.tools.aegis.exception.JrlAegisException;
import org.jrl.tools.condition.JrlCondition;
import org.jrl.tools.condition.JrlConditionMatchHelper;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

/**
 * 神盾链路执行器
 *
 * @author JerryLong
 */
public class JrlAegisExecutorChain extends JrlChain.AbstractZeusChain<JrlAegisExecutor<?, ?>, JrlAegisChainEntry> {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlAegisExecutorChain.class);

    public JrlAegisExecutorChain(int index, JrlAegisExecutor<?, ?> executor, JrlAegisExecutorChain next) {
        super(index, executor, next);
    }

    @Override
    public ZeusChainStatus status() {
        if (this.status != ZeusChainStatus.ENABLE) {
            return this.status;
        }
        final JrlAegisExecutor<?, ?> executor = this.getExecutor();
        if (null == executor || executor.isExpired()) {
            return ZeusChainStatus.DISABLE;
        }
        if (!executor.inTime()) {
            return ZeusChainStatus.DISABLE;
        }
        // 匹配条件
        return matchCondition();
    }

    private ZeusChainStatus matchCondition() {
        final JrlCondition condition = this.getExecutor().getRule().condition();
        if (null == condition) {
            return ZeusChainStatus.ENABLE;
        }
        final JrlConditionMatchHelper.MatchResult match = JrlConditionMatchHelper.match(condition, JrlAegisContext.getContext().getRequest());
        if (!match.isMatch()) {
            return ZeusChainStatus.DISABLE;
        }
        return ZeusChainStatus.ENABLE;
    }

    /**
     * 执行链路
     *
     * @return 结果entry，拿到entry必须执行end
     */
    @Override
    public JrlAegisChainEntry execute() {
        JrlAegisEntry entry = tryAcquire();
        // 如果当前节点没有获取到，则执行下一个节点
        //如果想执行整条链路的所有节点，把这个null == entry去掉就行
        if (null == entry && null != getNext()) {
            return nextTryAcquire(entry);
        }
        return new JrlAegisChainEntry(entry, null);
    }

    @Override
    public void add(JrlAegisExecutor<?, ?> executor, int index) {
        if (index > this.getIndex()) {
            final JrlAegisExecutor<?, ?> thisExecutor = this.getExecutor();
            //创建next节点（是原本当前节点）
            final JrlAegisExecutorChain next = new JrlAegisExecutorChain(this.getIndex(), thisExecutor, (JrlAegisExecutorChain) this.getNext());
            //指向新节点
            this.setNext(next);
            //替换当前节点
            this.setIndex(index);
            this.setExecutor(executor);
        } else {
            this.getNext().add(executor, index);
        }
    }

    @Override
    public int addLast(JrlAegisExecutor<?, ?> executor) {
        if (this.getNext() == null) {
            final int index = this.getIndex() - 1;
            this.setNext(new JrlAegisExecutorChain(index, executor, null));
            return index;
        } else {
            return this.getNext().addLast(executor);
        }
    }

    @Override
    public JrlAegisExecutorChain addFirst(JrlAegisExecutor<?, ?> executor) {
        return new JrlAegisExecutorChain(this.getIndex() + 1, executor, this);
    }

    /**
     * 尝试获取许可
     *
     * @return 是否获取成功
     */
    private JrlAegisEntry tryAcquire() {
        final JrlAegisExecutor<?, ?> executor = this.getExecutor();
        if (null == executor || status() != ZeusChainStatus.ENABLE) {
            return null;
        }
        return executor.tryAcquire();
    }

    private JrlAegisChainEntry nextTryAcquire(JrlAegisEntry entry) {
        final JrlChain<JrlAegisExecutor<?, ?>, JrlAegisChainEntry> next = getNext();
        // 执行下一个限流链路
        try {
            return new JrlAegisChainEntry(entry, next.execute());
        } catch (JrlAegisException e) {
            //如果下一个被阻断抛出异常，则结束当前链路
            JrlAegisChainEntry.end(entry);
            throw e;
        }
    }
}