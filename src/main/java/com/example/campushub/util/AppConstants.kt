package com.example.campushub.util

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppConstants {

    @Provides
    fun provideFirebaseAuth() = Firebase.auth

    @Provides
    fun provideDatabaseReference() : DatabaseReference = Firebase.database.getReference()

    @Provides
    fun provideDatabaseInstance() : FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    fun provideStorageReference() : StorageReference = FirebaseStorage.getInstance().reference
}