package com.github.tvbox.osc.cache;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * @author pj567
 * @date :2021/1/7
 * @description:
 */
@Entity(tableName = "localLive")
public class LocalLive implements Serializable {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String name;
    @NonNull
    public String url;
}