package it.polito.mad.g22.showprofileactivity.data

import androidx.room.*

@Entity(
    tableName = "userSportEvaluation",
    primaryKeys = ["email","sportId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["email"], childColumns = ["email"],onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = Sport::class, parentColumns = ["id"], childColumns = ["sportId"])
    ]
)
data class sportEvaluation (
    var email: String,
    val sportId: Int,
    var evaluation: Int
    )

data class SportEvaluationWithSport(
    @Embedded val sportEvaluation: sportEvaluation,
    @Relation(
        parentColumn = "sportId",
        entityColumn = "id"
    )
    val Sport: Sport
)


