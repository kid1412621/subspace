package me.nanova.subspace.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.nanova.subspace.data.db.AccountDao
import me.nanova.subspace.data.db.AppDatabase
import me.nanova.subspace.data.db.AppDatabase.Companion.DATABASE_NAME
import me.nanova.subspace.data.db.AppDatabase.Companion.MIGRATION_1_2
import me.nanova.subspace.data.db.AppDatabase.Companion.MIGRATION_2_3
import me.nanova.subspace.data.db.AppDatabase.Companion.MIGRATION_3_4
import me.nanova.subspace.data.db.RemoteKeyDao
import me.nanova.subspace.data.db.TorrentDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room
            .databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .build()
    }

    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    fun provideRemoteKeyDao(database: AppDatabase): RemoteKeyDao {
        return database.remoteKeyDao()
    }

    @Provides
    fun provideTorrentDao(database: AppDatabase): TorrentDao {
        return database.torrentDao()
    }
}
