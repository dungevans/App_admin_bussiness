package com.lethanh.ql_com_dao_bk.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductResponse {
    @SerializedName("content")
    private List<Product> content;

    @SerializedName("total_elements")
    private int totalElements;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("number")
    private int pageNumber;

    @SerializedName("size")
    private int pageSize;

    @SerializedName("number_of_elements")
    private int numberOfElements;

    public List<Product> getContent() {
        return content;
    }

    public void setContent(List<Product> content) {
        this.content = content;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }
}
