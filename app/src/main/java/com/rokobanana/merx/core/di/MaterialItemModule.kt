package com.rokobanana.merx.core.di

import com.rokobanana.merx.data.remote.MaterialItemFirestoreDataSource
import com.rokobanana.merx.domain.repository.MaterialItemRepository
import com.rokobanana.merx.data.repository.MaterialItemRepositoryImpl
import com.rokobanana.merx.data.source.MaterialItemDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MaterialItemModule {
    @Provides
    @Singleton
    fun provideMaterialItemDataSource(): MaterialItemDataSource = MaterialItemFirestoreDataSource()

    @Provides
    @Singleton
    fun provideMaterialItemRepository(dataSource: MaterialItemDataSource): MaterialItemRepository =
        MaterialItemRepositoryImpl(dataSource)
}