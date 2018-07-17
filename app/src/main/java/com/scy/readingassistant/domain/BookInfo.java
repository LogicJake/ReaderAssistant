package com.scy.readingassistant.domain;

public class BookInfo implements Comparable<BookInfo>{
    private Long addTime;
    private String name;
    private String path ;
    private int currentPage ;
    private int totalPage ;
    private String author;

    public BookInfo(Long add_time, String name, String path, int currentPage, int totalPage, String author) {
        this.addTime = add_time;
        this.name = name;
        this.path = path;
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.author = author;
    }

    public Long getAddTime() {
        return addTime;
    }

    public void setAddTime(Long addTime) {
        this.addTime = addTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "BookInfo{" +
                "addTime=" + addTime +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", currentPage=" + currentPage +
                ", totalPage=" + totalPage +
                ", author='" + author + '\'' +
                '}';
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public int compareTo(BookInfo s) {
        //自定义比较方法，如果认为此实体本身大则返回1，否则返回-1
        if(this.addTime >= s.getAddTime()){
            return 1;
        }
        return -1;
    }
}