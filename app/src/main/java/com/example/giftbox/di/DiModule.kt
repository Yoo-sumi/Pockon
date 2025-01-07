package com.example.giftbox.di

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat.getString
import androidx.room.Room
import com.example.giftbox.AppDatabase
import com.example.giftbox.BrandsDao
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.data.LoginRepository
import com.example.giftbox.R
import com.example.giftbox.data.remote.BrandSearchDataSource
import com.example.giftbox.data.BrandSearchRepository
import com.example.giftbox.data.local.BrandDataSource
import com.example.giftbox.data.remote.GiftDataSource
import com.example.giftbox.data.remote.GiftPhotoDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DiModule {
    @Singleton
    @Provides
    fun provideFirebaseAuth() : FirebaseAuth {
        return Firebase.auth
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestore() : FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideLoginRepository(auth: FirebaseAuth) : LoginRepository {
        return LoginRepository(auth)
    }

    @Singleton
    @Provides
    fun provideGiftRepository(giftDataSource: GiftDataSource, giftPhotoDataSource: GiftPhotoDataSource) : GiftRepository {
        return GiftRepository(giftDataSource, giftPhotoDataSource)
    }

    @Singleton
    @Provides
    fun provideGiftDataSource(firestore: FirebaseFirestore) : GiftDataSource {
        return GiftDataSource(firestore)
    }

    @Singleton
    @Provides
    fun provideBrandSearchRepository(brandSearchDataSource: BrandSearchDataSource, giftPlaceDataSource: BrandDataSource) : BrandSearchRepository {
        return BrandSearchRepository(brandSearchDataSource, giftPlaceDataSource)
    }

    @Provides
    fun provideBrandSearchDataSource() : BrandSearchDataSource {
        return BrandSearchDataSource()
    }

    @Provides
    fun provideBrandDataSource(brandsDao: BrandsDao) : BrandDataSource {
        return BrandDataSource(brandsDao)
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestorage() : StorageReference {
        return FirebaseStorage.getInstance().reference
    }

    @Singleton
    @Provides
    fun provideGiftPhotoDataSource(storageRef: StorageReference) : GiftPhotoDataSource {
        return GiftPhotoDataSource(storageRef)
    }

    @Singleton
    @Provides
    fun provideSharedPref(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(getString(context, R.string.preference_file_key), Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room
        .databaseBuilder(context, AppDatabase::class.java, "gift_box.db")
        .build()

    @Singleton
    @Provides
    fun provideGiftPlaceDao(appDatabase: AppDatabase): BrandsDao = appDatabase.brandsDao()
}