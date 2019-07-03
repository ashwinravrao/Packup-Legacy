package com.ashwinrao.locrate.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface BoxDao {

    /**
     * @return An observable collection of all boxes from {@link AppDatabase}.
     */

    @Query("SELECT * FROM boxes ORDER BY created DESC")
    LiveData<List<Box>> getBoxes();


    /**
     * @param moveId The id representing the parent move column
     * @return an observable collection of boxes that belong to the specified move
     */

    @Query("select * from boxes where move_id = :moveId order by created desc")
    LiveData<List<Box>> getBoxesByMove(int moveId);


    /**
     * @return An observable box (with specified name field) from {@link AppDatabase}.
     */

    @Query("SELECT * FROM boxes WHERE name = :name")
    LiveData<Box> getBoxByName(String name);


    /**
     * @return An observable box (with specified id field) from {@link AppDatabase}.
     */

    @Query("SELECT * FROM boxes WHERE id = :id")
    LiveData<Box> getBoxById(String id);

    /**
     * InsertBox/save a box to the {@link AppDatabase}.
     *
     * Overwrites boxes that have the same primary key.
     */

    @Insert(onConflict = REPLACE)
    void insert(Box...boxes);

    @Update(onConflict = REPLACE)
    void update(Box...boxes);

    /**
     * DeleteBox a box from {@link AppDatabase}.
     */

    @Delete
    void delete(Box...boxes);
}
