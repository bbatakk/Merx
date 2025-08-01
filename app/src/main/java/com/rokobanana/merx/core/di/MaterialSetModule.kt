package com.rokobanana.merx.core.di

import com.rokobanana.merx.data.repository.MaterialSetRepositoryImpl
import com.rokobanana.merx.domain.repository.MaterialSetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MaterialSetModule {
    @Binds
    @Singleton
    abstract fun bindMaterialSetRepository(
        impl: MaterialSetRepositoryImpl
    ): MaterialSetRepository
}