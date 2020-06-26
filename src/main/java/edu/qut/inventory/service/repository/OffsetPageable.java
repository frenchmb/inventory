package edu.qut.inventory.service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

public class OffsetPageable  implements Pageable, Serializable {
    private int limit;
    private int offset;
    private final Sort sort;

    public OffsetPageable(int limit, int offset, Sort sort) {
        this.limit = limit;
        this.offset = offset;
        this.sort = sort;
    }

    public OffsetPageable(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
        sort = new Sort(Sort.Direction.ASC, "id");
    }

    @Override
    public boolean isPaged() {
        return true;
    }

    @Override
    public boolean isUnpaged() {
        return false;
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }


    @Override
    public Pageable next() {
        return new OffsetPageable((int) (getOffset() + getPageSize()), getPageSize(), getSort());
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? new OffsetPageable((int) getOffset() - getPageSize(), getPageSize(), getSort()) : this;
    }


    @Override
    public Pageable first() {
        return new OffsetPageable(0, getPageSize(), getSort());
    }

    @Override
    public boolean hasPrevious() {
        return offset > limit;
    }
}
