package com.rokobanana.merx.core.di

import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.data.remote.MaterialCollectionFirestoreDataSource
import com.rokobanana.merx.data.repository.MaterialCollectionRepositoryImpl
import com.rokobanana.merx.data.source.MaterialCollectionDataSource
import com.rokobanana.merx.domain.repository.MaterialCollectionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MaterialCollectionModule {

    companion object {
        @Provides
        @Singleton
        fun provideMaterialCollectionDataSource(
            firestore: FirebaseFirestore
        ): MaterialCollectionDataSource = MaterialCollectionFirestoreDataSource(firestore)
    }

    @Binds
    @Singleton
    abstract fun bindMaterialCollectionRepository(
        impl: MaterialCollectionRepositoryImpl
    ): MaterialCollectionRepository
}