package com.github.tvbox.osc.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class AbsSortJson implements Serializable {

    @SerializedName(value = "class")
    public ArrayList<AbsJsonClass> classList;

    public AbsSortXml toAbsSortXml() {
        AbsSortXml absSortXml = new AbsSortXml();
        MovieSort movieSort = new MovieSort();
        movieSort.sortList = new ArrayList<>();
        for (AbsJsonClass cls : classList) {
            MovieSort.SortData sortData = new MovieSort.SortData();
            sortData.id = cls.type_id;
            sortData.name = cls.type_name;
            movieSort.sortList.add(sortData);
        }
        absSortXml.movieSort = movieSort;
        return absSortXml;
    }

    public class AbsJsonClass implements Serializable {
        public String type_id;
        public String type_name;
    }

}
