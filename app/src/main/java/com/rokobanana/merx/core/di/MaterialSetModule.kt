package com.rokobanana.merx.core.di

import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.data.remote.MaterialSetFirestoreDataSource
import com.rokobanana.merx.data.repository.MaterialSetRepositoryImpl
import com.rokobanana.merx.data.source.MaterialSetDataSource
import com.rokobanana.merx.domain.repository.MaterialSetRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MaterialSetModule {

    companion object {
        @Provides
        @Singleton
        fun provideMaterialSetDataSource(
            firestore: FirebaseFirestore
        ): MaterialSetDataSource = MaterialSetFirestoreDataSource(firestore)
    }

    @Binds
    @Singleton
    abstract fun bindMaterialSetRepository(
        impl: MaterialSetRepositoryImpl
    ): MaterialSetRepository
}