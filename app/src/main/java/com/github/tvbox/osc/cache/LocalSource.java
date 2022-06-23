package com.github.tvbox.osc.cache;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * @author pj567
 * @date :2021/1/7
 * @description:
 */
@Entity(tableName = "localSource")
public class LocalSource implements Serializable {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String name;
    @NonNull
    public String api;
    @ColumnInfo(defaultValue = "0")
    public int type;
    public String playerUrl;
}