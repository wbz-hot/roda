package org.roda.wui.client.common.search;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;

public interface EntitySearch<T extends IsIndexed> {

    BasicAsyncTableCell<T> getList();

    void setFilter(Filter filter);

    void refresh();
}
