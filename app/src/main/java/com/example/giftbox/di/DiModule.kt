package com.example.giftbox.di

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat.getString
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.data.LoginRepository
import com.example.giftbox.R
import com.example.giftbox.data.GiftDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
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
    fun provideGiftRepository(giftDataSource: GiftDataSource) : GiftRepository {
        return GiftRepository(giftDataSource)
    }

    @Singleton
    @Provides
    fun provideGiftDataSource(firestore: FirebaseFirestore) : GiftDataSource {
        return GiftDataSource(firestore)
    }

    @Singleton
    @Provides
    fun provideSharedPref(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(getString(context, R.string.preference_file_key), Context.MODE_PRIVATE)
    }
}