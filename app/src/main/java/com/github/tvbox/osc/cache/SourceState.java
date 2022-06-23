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
@Entity(tableName = "sourceState")
public class SourceState implements Serializable {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String sourceKey;
    public boolean home;
    public boolean active;
    public String tidSort;
}