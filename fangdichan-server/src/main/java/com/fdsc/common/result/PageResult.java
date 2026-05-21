package com.fdsc.common.result;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private long page;
    private long size;
    private long total;
    private long pages;

    public static <T> PageResult<T> of(List<T> list, long page, long size, long total) {
        PageResult<T> r = new PageResult<>();
        r.setList(list);
        r.setPage(page);
        r.setSize(size);
        r.setTotal(total);
        r.setPages((long) Math.ceil((double) total / size));
        return r;
    }
}
