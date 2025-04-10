package org.jrl.tools.aegis.core;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jrl.tools.aegis.JrlAegis;
import org.jrl.tools.aegis.JrlAegisRule;
import org.jrl.tools.aegis.core.chain.JrlAegisChainChain;
import org.jrl.tools.aegis.core.chain.JrlAegisChainEntry;
import org.jrl.tools.aegis.core.chain.JrlAegisExecutorChain;
import org.jrl.tools.aegis.exception.JrlAegisException;
import org.jrl.tools.aegis.model.JrlAegisScope;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.mock.JrlMock;
import org.jrl.tools.mock.JrlMockUtil;
import org.jrl.tools.mock.model.MockResponse;
import org.jrl.tools.utils.JrlClassUtil;
import org.jrl.tools.utils.JrlCollectionUtil;
import org.jrl.tools.utils.function.AbstractJrlCallable;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 神盾处理抽象类，内部转换成chain矩阵处理
 *
 * @author JerryLong
 */
public abstract class AbstractJrlAegis<E extends JrlAegisExecutor<?, R>, R extends JrlAegisRule> implements JrlAegis, JrlAegisControl<R> {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(AbstractJrlAegis.class);

    private final String name;
    private Map<JrlAegisScope, List<R>> ruleInfo;
    private JrlAegisChainChain aegisChainChain;
    private boolean openBlockMock = false;

    public AbstractJrlAegis(String name, List<R> rules) {
        this.name = name;
        changeRules(rules);
    }

    /**
     * 获取规则信息
     *
     * @return
     */
    @Override
    public Map<JrlAegisScope, List<R>> getRules() {
        return ruleInfo;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void openBlockMock() {
        this.openBlockMock = true;
        JrlMock.open();
    }

    @Override
    public void closeBlockMock() {
        this.openBlockMock = false;
    }

    @Override
    public boolean tryAcquire() {
        JrlAegisChainEntry entry = null;
        try {
            entry = aegisChainChain.execute();
            return true;
        } catch (JrlAegisException e) {
            return false;
        } finally {
            JrlAegisChainEntry.end(entry);
        }
    }

    @Override
    public JrlAegisEntry tryEntry() throws JrlAegisException {
        return aegisChainChain.execute();
    }

    @Override
    public <V> V tryAcquire(AbstractJrlCallable<V> runnable) throws JrlAegisException {
        JrlAegisRunnableEntry<V> entry = this.tryEntry(runnable);
        if (null != entry) {
            try {
                return entry.getData();
            } finally {
                entry.end();
            }
        }
        // 未获取到令牌，打印错误日志
        LOGGER.error("zeus-aegis tryAcquire fail , not found entry !");
        return null;
    }

    @Override
    public <V> JrlAegisRunnableEntry<V> tryEntry(AbstractJrlCallable<V> callable) throws JrlAegisException {
        Throwable error = null;
        V data = null;
        final JrlAegisChainEntry realEntry;
        try {
            //尝试获取令牌
            realEntry = aegisChainChain.execute();
        } catch (JrlAegisException e) {
            //触发Aegis异常，判断是否开启mock
            if (this.openBlockMock) {
                final MockResponse<V> mock = JrlMockUtil.mock(JrlAegis.class, this.getName());
                if (null != mock && mock.isMock()) {
                    try {
                        data = mock.getResult();
                        //匹配类型
                        //1 如果返回是非基本类型，并且data是字符串，则用json反序列化
                        if (!JrlClassUtil.isWrapClass(callable.getValueType().getClass()) && data instanceof String) {
                            data = JrlJsonNoExpUtil.fromJson((String) data, new TypeReference<V>() {
                                @Override
                                public Type getType() {
                                    return callable.getValueType();
                                }
                            });
                        }
                    } catch (Throwable ex) {
                        error = ex;
                    }
                    return new JrlAegisRunnableEntry<>(null, data, error);
                }
            }
            throw e;
        }
        try {
            data = callable.call();
        } catch (Throwable e) {
            error = e;
        }
        return new JrlAegisRunnableEntry<>(realEntry, data, error);
    }

    /**
     * 构建执行器
     *
     * @return
     */
    protected abstract E buildExecutor(R rule);


    @Override
    public void deleteRule(R rule) {
        LOGGER.info("zeus-aegis delete rule : {}", rule);
        final JrlAegisChainChain chain = ZeusAegisChainUtil.getChain(rule.scope().getPriority());
        if (chain == null) {
            LOGGER.error("zeus-aegis delete rule fail , not found chain");
            return;
        }
        chain.getExecutor().remove(rule.id());
        LOGGER.info("zeus-aegis delete rule success !");
    }

    /**
     * 更新规则
     *
     * @param rule
     */
    @Override
    public void changeRule(R rule) {
        LOGGER.info("zeus-aegis change rule : {}", rule);
        final JrlAegisChainChain chain = ZeusAegisChainUtil.getChain(rule.scope().getPriority());
        if (chain == null) {
            LOGGER.error("zeus-aegis change rule fail , not found chain");
            return;
        }
        final JrlAegisExecutor<?, JrlAegisRule> zeusAegisExecutor = (JrlAegisExecutor<?, JrlAegisRule>) chain.getExecutor().get(rule.id());
        if (null == zeusAegisExecutor) {
            LOGGER.error("zeus-aegis change rule fail , not found executor");
            return;
        }
        zeusAegisExecutor.changeRule(rule);
        LOGGER.info("zeus-aegis change rule success !");
    }


    @Override
    public void addRule(R rule) {
        LOGGER.info("zeus-aegis change rule : {}", rule);
        final JrlAegisChainChain chain = ZeusAegisChainUtil.getChain(rule.scope().getPriority());
        if (chain == null) {
            LOGGER.error("zeus-aegis add rule fail , not found chain");
            return;
        }
        //判断id是否已存在
        if (chain.get(rule.id()) != null) {
            LOGGER.warn("zeus-aegis add rule fail , rule id already exist ! scope : {} , id : {}", rule.scope().getScope(), rule.id());
            return;
        }
        chain.getExecutor().add(buildExecutor(rule), rule.id());
        LOGGER.info("zeus-aegis change rule success !");
    }

    /**
     * 获取执行器（是一个四方形矩阵）
     *
     * @return
     */
    private JrlAegisChainChain getAegisChainChain() {
        if (ruleInfo == null || ruleInfo.isEmpty()) {
            return null;
        }
        //构建链矩阵
        List<JrlAegisScope> scopes = new ArrayList<>();
        for (Map.Entry<JrlAegisScope, List<R>> entry : ruleInfo.entrySet()) {
            scopes.add(entry.getKey());
        }
        //优先级越高优先
        scopes.sort(Comparator.comparingInt(JrlAegisScope::getPriority).reversed());
        return ZeusAegisChainUtil.buildChain(scopes, 0, (scope) ->
                ZeusAegisChainUtil.buildExecutorChain(ruleInfo.get(scope), 0, this::buildExecutor)
        );
    }

    @Override
    public void changeRules(List<R> rules) {
        //对条件进行过滤，已过期的删除
        final long now = System.currentTimeMillis();
        //rules根据scope分组，priority排序，rule根据id排序，id大的优先
        Map<JrlAegisScope, List<R>> ruleMap = JrlCollectionUtil.
                groupAndSort(rules, R::scope, Comparator.comparingInt(R::id).reversed());
        this.ruleInfo = JrlCollectionUtil.filter(ruleMap, rule -> rule.startTime() <= now && rule.endTime() >= now);
        if (null == this.ruleInfo) {
            LOGGER.warn("zeus-aegis load rules fail , rules is empty !");
            throw new IllegalArgumentException("zeus-aegis load rules fail , rules is empty !");
        }
        //构建神盾执行链
        if (null != this.aegisChainChain) {
            LOGGER.info("zeus-aegis change rules start ! scope size : {}", ruleInfo.size());
            this.aegisChainChain = getAegisChainChain();
            LOGGER.info("zeus-aegis change rules success !");
        } else {
            LOGGER.info("zeus-aegis load rules start ! scope size : {}", ruleInfo.size());
            this.aegisChainChain = getAegisChainChain();
            LOGGER.info("zeus-aegis load rules success !");
        }
    }

    protected static class ZeusAegisChainUtil {

        private static final Map<Integer, JrlAegisChainChain> CHAIN_MAP = new ConcurrentHashMap<>();

        /**
         * 构建链路
         *
         * @param scopes        作用域列表
         * @param index         索引
         * @param chainFunction 链创建方法
         * @return ZeusLimiterChainChain
         */
        public static JrlAegisChainChain buildChain(List<JrlAegisScope> scopes, int index, Function<JrlAegisScope, JrlAegisExecutorChain> chainFunction) {
            if (scopes == null || scopes.isEmpty()) {
                return null;
            }
            if (index >= scopes.size()) {
                return null;
            }
            final JrlAegisScope zeusAegisScope = scopes.get(index);
            final JrlAegisChainChain zeusAegisChainChain = new JrlAegisChainChain(zeusAegisScope.getPriority(), chainFunction.apply(zeusAegisScope), buildChain(scopes, ++index, chainFunction));
            CHAIN_MAP.put(zeusAegisScope.getPriority(), zeusAegisChainChain);
            return zeusAegisChainChain;
        }

        public static JrlAegisChainChain getChain(int priority) {
            return CHAIN_MAP.get(priority);
        }

        /**
         * 构建链路
         *
         * @param rules            规则列表
         * @param index            当前索引
         * @param executorFunction 执行器构造函数
         * @param <E>              执行器
         * @return 链路
         */
        public static <R extends JrlAegisRule, E extends JrlAegisExecutor<?, R>> JrlAegisExecutorChain buildExecutorChain(List<R> rules, int index, Function<R, E> executorFunction) {
            if (rules == null || rules.isEmpty()) {
                return null;
            }
            if (index >= rules.size()) {
                return null;
            }
            return new JrlAegisExecutorChain(rules.get(index).id(), executorFunction.apply(rules.get(index)), buildExecutorChain(rules, ++index, executorFunction));
        }
    }
}
