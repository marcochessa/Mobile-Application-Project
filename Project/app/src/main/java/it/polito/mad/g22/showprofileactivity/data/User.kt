package it.polito.mad.g22.showprofileactivity.data

import android.provider.ContactsContract.CommonDataKinds.Email
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "user")
data class User(
    @PrimaryKey val email: String,
    val fullname: String?,
    val nickname: String?,
    val tel: String?,
    val birthDate: String? = null,
    val bio: String? = null,
    val image: String? = null

)

@Entity(tableName = "language")
data class Language(
    @PrimaryKey val acronym : String
)

@Entity(
    tableName = "userLanguageLink",
    primaryKeys = ["email","acronym"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["email"], childColumns = ["email"],onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = Language::class, parentColumns = ["acronym"], childColumns = ["acronym"])
    ])
data class UserLanguageLink(
    val email : String,
    val acronym: String
)


data class UserWithLanguagesAndEvaluations(
    @Embedded val user: User,
    @Relation(
        parentColumn = "email",
        entityColumn = "acronym",
        associateBy = Junction(UserLanguageLink::class)
    )
    val languages: List<Language>,


    @Relation(
        parentColumn = "email",
        entityColumn = "email"
    )
    val evaluations: List<sportEvaluation>

)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Transaction
    @Query("SELECT * FROM user WHERE user.email = :userEmail")
    fun getUsersWithLanguages(userEmail: String): LiveData<UserWithLanguagesAndEvaluations>

    @Query("Select acronym FROM language")
    fun getLanguages() : LiveData<List<Language>>

    @Query("DELETE FROM userLanguageLink WHERE email = :userEmail")
    fun deleteAllLanguagesForUser(userEmail:String)

    @Insert
    fun insertUserLanguageLink(links: List<UserLanguageLink>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("DELETE FROM userSportEvaluation WHERE email = :userEmail")
    fun deleteAllEvaluationByEmail(userEmail: String)

    @Insert
    fun insertEvaluationList(evaluations: List<sportEvaluation>)

    @Query("UPDATE user SET email = :newEmail WHERE email= :oldEmail")
    fun updateUserEmail(newEmail: String, oldEmail: String)

    @Transaction
    fun insertUserWithLanguages(u: UserWithLanguagesAndEvaluations){
        deleteAllLanguagesForUser(u.user.email)
        deleteAllEvaluationByEmail(u.user.email)
        insertUserLanguageLink(u.languages.map { UserLanguageLink(u.user.email,it.acronym) })
        insertEvaluationList(u.evaluations)
        insertUser(u.user)
    }

    @Transaction
    fun insertUserWithLanguages(u: UserWithLanguagesAndEvaluations, oldEmail: String){
        updateUserEmail(u.user.email,oldEmail)
        deleteAllLanguagesForUser(u.user.email)
        deleteAllEvaluationByEmail(u.user.email)
        insertUserLanguageLink(u.languages.map { UserLanguageLink(u.user.email,it.acronym) })
        insertEvaluationList(u.evaluations)
        insertUser(u.user)
    }
}

