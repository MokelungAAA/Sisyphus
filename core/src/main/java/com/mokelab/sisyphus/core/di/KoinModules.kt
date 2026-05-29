package com.mokelab.sisyphus.core.di

import org.koin.core.module.Module

/**
 * Koin模块接口
 * 各feature模块实现此接口提供自己的Koin模块
 */
interface KoinModules {
    /** 提供该模块的所有Koin模块 */
    fun modules(): List<Module>
}
