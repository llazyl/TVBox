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
public interface LocalParseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LocalParse bean);

    @Query("select * from localParse")
    List<LocalParse> getAll();

    @Delete
    int delete(LocalParse bean);
}