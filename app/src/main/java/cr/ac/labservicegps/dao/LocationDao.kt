package cr.ac.labservicegps.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cr.ac.labservicegps.entity.Location

@Dao
interface LocationDao {

    @Insert
    fun insert(location: Location)

    @Query("select * from location_table")
    fun query():List<Location>

}