package com.hxh.component.basicore.ui.mrecycleview;

import java.util.List;

/**
 * 标题: ANetResultBean.java
 * 作者: hxh
 * 日期: 2018/2/27 14:53
 * 描述: 如果使用MRecycleView，那么你的返回类型必须是继承了这个类
 * 并且，请求方法需要加上@UseMRecycleView
 *
 */
public abstract class AbsNetResultBean<T> {


    public abstract int getItemCount();

    public abstract List<T> getItems();

    public abstract Object getSource();
}
