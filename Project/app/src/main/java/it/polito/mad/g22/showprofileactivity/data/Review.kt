package it.polito.mad.g22.showprofileactivity.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "review", primaryKeys = ["user", "field"], foreignKeys = [
    ForeignKey(entity = User::class, parentColumns = ["email"], childColumns = ["user" ],onUpdate = ForeignKey.CASCADE),
    ForeignKey(entity = SportField::class, parentColumns = ["id"], childColumns = ["field" ])
])
data class Review(
    val user: String,
    val field:Int,
    val star: Int?,
    val description: String?,
)

@Dao
abstract class ReviewDao {
    @Query("SELECT user, field, star, description FROM review WHERE user=:email")
    abstract fun getReviews(email: String): LiveData<List<Review>>

    @Query("SELECT DISTINCT user, field FROM booking b, timeslot t " +
            "WHERE b.timeslot = t.id AND user=:email AND strftime('%s', starts_at) < strftime('%s', 'now','localtime') AND NOT EXISTS ( " +
            " SELECT 1 from review r where r.user = b.user AND r.field = b.field )")
    abstract fun getPendingReviews(email: String): LiveData<List<Review>>

    @Query("INSERT INTO review VALUES(:user, :field, :star, :description)")
    abstract fun insertReview(user: String, field: Int, star: Int, description: String?)

    @Query("UPDATE review SET star=:star, description=:description WHERE user=:user AND field=:field")
    abstract fun updateReview(user: String, field: Int, star: Int, description: String?)

    @Query("DELETE FROM review WHERE user=:user AND field = :field")
    abstract fun deleteReview(user: String, field: Int)

}

