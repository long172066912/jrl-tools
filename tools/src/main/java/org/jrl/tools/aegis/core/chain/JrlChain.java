package org.jrl.tools.aegis.core.chain;

/**
 * 链路接口，index越大越优先
 *
 * @author JerryLong
 */
interface JrlChain<E, R> {
    /**
     * 获取索引
     *
     * @return 索引
     */
    int getIndex();

    /**
     * 执行
     */
    R execute();

    /**
     * 添加节点
     *
     * @param e     节点
     * @param index 位置
     */
    void add(E e, int index);

    /**
     * 添加节点到最后一个
     *
     * @param e 节点
     * @return 位置
     */
    int addLast(E e);

    /**
     * 添加节点到第一个
     *
     * @param e 节点
     * @return 位置
     */
    JrlChain<E, R> addFirst(E e);

    /**
     * 移除节点
     *
     * @param index 位置
     */
    void remove(int index);

    /**
     * 获取节点
     *
     * @param index 位置
     * @return 节点
     */
    E get(int index);

    /**
     * 节点状态
     *
     * @return 状态
     */
    JrlChainStatus status();

    /**
     * 修改节点
     *
     * @param e     节点
     * @param index 位置
     */
    void change(E e, int index);

    enum JrlChainStatus {
        /**
         * 启用
         */
        ENABLE,
        /**
         * 禁用
         */
        DISABLE
    }

    /**
     * 对工具链的抽象处理
     *
     * @author JerryLong
     */
    abstract class AbstractJrlChain<E, R> implements JrlChain<E, R> {
        private int index;
        private E executor;
        private JrlChain<E, R> next;
        protected JrlChainStatus status = JrlChainStatus.ENABLE;

        public AbstractJrlChain(int index, E executor, AbstractJrlChain<E, R> next) {
            this.index = index;
            this.executor = executor;
            this.next = next;
        }

        public JrlChain<E, R> getNext() {
            return next;
        }

        protected void setNext(JrlChain<E, R> next) {
            this.next = next;
        }

        public E getExecutor() {
            return executor;
        }

        public void setExecutor(E executor) {
            this.executor = executor;
        }

        @Override
        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        @Override
        public void remove(int index) {
            if (this.getIndex() == index) {
                this.status = JrlChainStatus.DISABLE;
            } else {
                if (null == this.getNext()) {
                    return;
                }
                this.getNext().remove(index);
            }
        }

        @Override
        public void change(E e, int index) {
            if (this.getIndex() == index) {
                this.setExecutor(e);
            } else {
                if (null == this.getNext()) {
                    throw new IllegalArgumentException("The index does not exist." + index);
                }
                this.getNext().change(e, index);
            }
        }

        @Override
        public E get(int index) {
            if (this.getIndex() == index) {
                return this.getExecutor();
            } else {
                if (null == this.getNext()) {
                    return null;
                }
                return this.getNext().get(index);
            }
        }
    }
}
