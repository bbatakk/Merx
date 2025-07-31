package com.rokobanana.merx.core.di

import com.rokobanana.merx.data.repository.AuthRepositoryImpl
import com.rokobanana.merx.data.repository.MembresRepositoryImpl
import com.rokobanana.merx.data.repository.ProductesRepositoryImpl
import com.rokobanana.merx.domain.repository.AuthRepository
import com.rokobanana.merx.domain.repository.MembresRepository
import com.rokobanana.merx.domain.repository.ProductesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductesRepository(
        impl: ProductesRepositoryImpl
    ): ProductesRepository

    @Binds
    abstract fun bindMembresRepository(
        impl: MembresRepositoryImpl
    ): MembresRepository
}