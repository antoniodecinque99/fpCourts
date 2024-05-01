package com.example.mainactivity.models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PersonSportsDao {
    @Query("SELECT * FROM person_sports")
    fun getAllPersonSports() : List<PersonSports>

    @Query("SELECT * FROM person_sports WHERE person_id LIKE :personId")
    fun getAllSportsPerPerson(personId: Int): List<PersonSports>

    @Query("SELECT * FROM person_sports WHERE person_id LIKE :personId AND sports_id LIKE :sportId")
    fun getSportPerPersonById(personId: Int, sportId: Int): PersonSports

    @Query("SELECT * FROM person_sports WHERE person_id LIKE :personId AND sport_name LIKE :sportName")
    fun getSportPerPersonByName(personId: Int, sportName: String): PersonSports

    @Query("DELETE FROM person_sports WHERE person_id = :personId AND sports_id = :sportId")
    fun deleteSportForPerson(personId: Int, sportId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(personSport: PersonSports)
}