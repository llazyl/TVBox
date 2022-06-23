package com.github.tvbox.osc.cache;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * @author pj567
 * @date :2021/1/7
 * @description:
 */
@Dao
public interface LocalLiveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LocalLive bean);

    @Query("select * from localLive")
    List<LocalLive> getAll();

    @Delete
    int delete(LocalLive bean);
}