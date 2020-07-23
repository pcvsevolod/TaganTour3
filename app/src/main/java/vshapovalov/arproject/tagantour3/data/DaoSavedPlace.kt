package vshapovalov.arproject.tagantour3.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DaoSavedPlace {
    @Query("select * from savedPlaces")
    fun getAllSavedPlaces(): List<SavedPlace>

    @Query("delete from savedPlaces")
    fun deleteAllSavedPlaces()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSavedPlace(savedPlace: SavedPlace)

    @Update
    fun updateSavedPlace(savedPlace: SavedPlace)

    @Delete
    fun deleteSavedPlace(savedPlace: SavedPlace)
}