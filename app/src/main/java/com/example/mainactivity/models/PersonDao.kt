package com.example.mainactivity.models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons")
    fun getAllPersons() : LiveData<List<Person>>

    @Query("SELECT * FROM persons WHERE person_id LIKE :personId")
    fun getPersonById(personId: Int): Person

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(person: Person)
}