package com.raywenderlich.android.droidwiki.dagger

import com.raywenderlich.android.droidwiki.ui.homepage.HomepageActivity
import com.raywenderlich.android.droidwiki.ui.search.SearchActivity
import dagger.Component
import javax.inject.Singleton

/**
 * AppComponent
 *
 * @author djzhao
 * @date 21/03/25
 */
@Singleton
@Component(modules = [AppModule::class, PresenterModule::class, NetworkModule::class, WikiModule::class])
interface AppComponent {
    fun inject(target: HomepageActivity)
    fun inject(target: SearchActivity)
}