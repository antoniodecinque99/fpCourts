package com.example.mainactivity


import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mainactivity.models.Booking
import com.example.mainactivity.models.BookingDao
import com.example.mainactivity.models.Court
import com.example.mainactivity.models.CourtDao
import com.example.mainactivity.models.CourtReview
import com.example.mainactivity.models.CourtReviewDao
import com.example.mainactivity.models.Person
import com.example.mainactivity.models.PersonDao
import com.example.mainactivity.models.PersonSports
import com.example.mainactivity.models.PersonSportsDao


@Database(entities = [Booking::class, Court::class, Person::class, CourtReview::class, PersonSports::class], version = 2)
abstract class BookingDatabase: RoomDatabase() {

    abstract fun bookingDao(): BookingDao
    abstract fun personDao(): PersonDao
    abstract fun courtDao(): CourtDao
    abstract fun courtReviewDao(): CourtReviewDao
    abstract fun personSportsDao(): PersonSportsDao

    companion object {
        @Volatile
        private var INSTANCE: BookingDatabase? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `person_sports` (\n" +
                        "\t`matches_played` INTEGER NOT NULL,\n" +
                        "\t`seen_users` INTEGER NOT NULL,\n" +
                        "\t`sport_name` TEXT NOT NULL,\n" +
                        "\t`matches_lost` INTEGER NOT NULL,\n" +
                        "\t`matches_organized` INTEGER  NOT NULL,\n" +
                        "\t`level` INTEGER  NOT NULL,\n" +
                        "\t`matches_won` INTEGER  NOT NULL,\n" +
                        "\t`sports_id` INTEGER NOT NULL,\n" +
                        "\t`person_id` INTEGER NOT NULL,\n" +
                        "\t`matches_planned` INTEGER NOT NULL,\n" +
                        "\tPRIMARY KEY (`person_id`,`sports_id`)\n" +
                        ");")
            }
        }

        fun getDatabase(context: Context): BookingDatabase =
            (INSTANCE ?: synchronized(this) {
                val i = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BookingDatabase::class.java, "booking_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            //prefill the db
                            Thread {
                                val database = getDatabase(context)

                                val bookingDao = database.bookingDao()
                                val courtDao = database.courtDao()
                                val personDao = database.personDao()
                                val courtReviewDao = database.courtReviewDao()
                                val personSportsDao = database.personSportsDao()

                                for (booking in prepopulated_bookings) {
                                    bookingDao.save(booking)
                                }

                                for (court in prepopulated_court) {
                                    courtDao.save(court)
                                }

                                for (person in prepopulated_person) {
                                    personDao.save(person)
                                }

                                for (courtReview in prepopulater_courtReviews) {
                                    courtReviewDao.save(courtReview)
                                }

                                for (personSports in prepopulated_personSports) {
                                    personSportsDao.save(personSports)
                                }
                            }.start()
                        }
                    })
                    .build()
                INSTANCE = i
                INSTANCE
            })!!

        val prepopulated_person = listOf<Person>(
            Person(0, "Simone", "Paniati", "SP"),
            Person(0, "Flavio", "Patti", "FP"),
            Person(0, "Antonio", "De Cinque", "AD"),
            Person(0, "Federico", "Boscolo", "FB")
        )

        val prepopulated_court = listOf<Court>(
            Court(0, "CIT Turin", "Football", "Via Orvieto, 1/20/A", 10.5, favorite = false),
            Court(0, "Robilant", "Football", "P.za di Robilant, 16", 13.5, favorite = false),
            Court(0, "Turin Sport Center", "Basket", "Via Filadelfia, 142", 8.0, favorite = false),
            Court(0, "Le Pleiadi", "Tennis", "Via Rosta, 103", 20.0, favorite = false),
            Court(0, "Skyline Volley Club", "Volley", "Via Livorno, 8", 15.0, favorite = false),
            Court(0, "Padel Club Turin", "Padel", "Via Salgari, 25", 18.0, favorite = false)
        )

        val prepopulated_bookings = listOf(
            Booking(0, 1, "Basket court for 3 people", 3, "2023-05-01", 2),
            Booking(0, 1, "Tennis court for 2 people", 4, "2023-05-31", 3),
            Booking(0, 1, "Football field for 5 people", 1, "2023-05-02", 1),
            Booking(0, 1, "Tennis court for 2 people", 4, "2023-05-02", 3),
            Booking(0, 1, "Volley court for 4 people", 5, "2023-05-02", 4),
            Booking(0, 1, "Padel court for 2 people", 6, "2023-05-05", 5),
            Booking(0, 1, "Basket court for 3 people", 3, "2023-05-05", 2),
            Booking(0, 1, "Football field for 5 people", 1, "2023-05-07", 1),
            Booking(0, 1, "Tennis court for 2 people", 4, "2023-05-07", 3),
            Booking(0, 1, "Volley court for 4 people", 5, "2023-05-07", 4),
            Booking(0, 1, "Padel court for 2 people", 6, "2023-05-07", 5),
            Booking(0, 1, "Volley court for 4 people", 5, "2023-05-07", 4),
            Booking(0, 1, "Padel court for 2 people", 6, "2023-05-07", 5),
            Booking(0, 1, "Volley court for 4 people", 5, "2023-05-07", 4),
            Booking(0, 1, "Padel court for 2 people", 6, "2023-05-07", 5),
            Booking(0, 1, "Basket court for 3 people", 3, "2023-05-11", 2),
            Booking(0, 1, "Football field for 5 people", 1, "2023-05-12", 1),
            Booking(0, 1, "Tennis court for 2 people", 4, "2023-05-12", 3),
            Booking(0, 1, "Volley court for 4 people", 5, "2023-05-18", 4),
            Booking(0, 1, "Padel court for 2 people", 6, "2023-05-18", 5),
            Booking(0, 1, "Basket court for 3 people", 3, "2023-05-19", 2),
            Booking(0, 1, "Football field for 5 people", 1, "2023-05-19", 1),
            Booking(0, 1, "Tennis court for 2 people", 4, "2023-05-23", 3),
            Booking(0, 1, "Volley court for 4 people", 5, "2023-05-23", 4),
            Booking(0, 1, "Padel court for 2 people", 6, "2023-05-28", 5),
            Booking(0, 1, "Basket court for 3 people", 3, "2023-05-28", 2),
            Booking(0, 1, "Football field for 5 people", 1, "2023-05-28", 1)
        )

        val prepopulater_courtReviews = listOf<CourtReview>(
            // Court 1
            CourtReview(0, 1, 1, 3, "More or less", "2023-05-01"),
            CourtReview(0, 2, 1, 4, "Great court with excellent facilities!", "2023-05-02"),
            CourtReview(0, 3, 1, 5, "Highly recommended for sports enthusiasts.", "2023-05-03"),
            CourtReview(0, 4, 1, 2, "Could be better maintained.", "2023-05-04"),

            // Court 2
            CourtReview(0, 1, 2, 1, "Bad", "2023-05-05"),
            CourtReview(0, 2, 2, 3, "Decent court, but needs improvement.", "2023-05-06"),
            CourtReview(0, 3, 2, 4, "Good court for casual games.", "2023-05-07"),

            // Court 3
            CourtReview(0, 1, 3, 5, "Top-notch facilities and great atmosphere!", "2023-05-08"),
            CourtReview(0, 2, 3, 4, "One of the best courts in town.", "2023-05-09"),
            CourtReview(0, 3, 3, 2, "Not very impressed with the court.", "2023-05-10"),

            // Court 4
            CourtReview(0, 1, 4, 4, "Almost TOP", "2023-05-11"),
            CourtReview(0, 2, 4, 5, "Superb court with excellent amenities.", "2023-05-12"),
            CourtReview(0, 3, 4, 3, "Could use some improvements, but overall good.", "2023-05-13"),

            // Court 5
            CourtReview(0, 1, 5, 5, "Mhhh", "2023-05-14"),
            CourtReview(0, 2, 5, 4, "This court offers a fantastic playing experience.", "2023-05-15"),
            CourtReview(0, 3, 5, 3, "Average court, but good enough for casual games.", "2023-05-16"),

            // Court 6
            CourtReview(0, 1, 6, 3, "mHHHHH", "2023-05-17"),
            CourtReview(0, 2, 6, 2, "mHHHHH", "2023-05-18"),
            CourtReview(0, 3, 6, 1, "Not recommended. Poorly maintained court.", "2023-05-19")
        )




        val prepopulated_personSports = listOf(
            PersonSports(0, 0, "Basket", 17, 2, 1, 1, 10, 1, 0),
            PersonSports(0, 1, "Football", 36, 10, 6, 4, 27, 4, 2),
            PersonSports(0, 2, "Tennis", 74, 54, 40, 14, 14, 37, 1),
            PersonSports(0, 3, "Volley", 21, 7, 3, 4, 15, 1, 0),
            PersonSports(0, 4, "Padel", 92, 150, 129, 21, 102, 147, 3),
        )
    }
}