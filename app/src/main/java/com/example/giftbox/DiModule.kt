package com.example.giftbox

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat.getString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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
    fun provideLoginRepository(auth: FirebaseAuth) : LoginRepository {
        return LoginRepository(auth)
    }

// SharedPref
//    @Singleton
//    @Provides
//    fun provideSharedPref(@ApplicationContext context: Context): SharedPreferences {
//        return context.getSharedPreferences(getString(context, R.string.preference_file_key), Context.MODE_PRIVATE)
//    }
}