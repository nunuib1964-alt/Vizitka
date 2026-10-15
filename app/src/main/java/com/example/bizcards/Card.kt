package com.example.bizcards

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val note: String = ""
)

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY name COLLATE NOCASE")
    fun getAll(): Flow<List<Card>>

    @Query(
        "SELECT * FROM cards WHERE " +
        "name LIKE '%' || :q || '%' OR company LIKE '%' || :q || '%' OR " +
        "phone LIKE '%' || :q || '%' OR email LIKE '%' || :q || '%' " +
        "ORDER BY name COLLATE NOCASE"
    )
    fun search(q: String): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getById(id: Int): Flow<Card?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: Card): Long

    @Delete
    suspend fun delete(card: Card)
}

@Database(entities = [Card::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile private var inst: AppDb? = null
        fun get(ctx: Context): AppDb =
            inst ?: synchronized(this) {
                inst ?: Room.databaseBuilder(ctx, AppDb::class.java, "cards.db").build().also { inst = it }
            }
    }
}
