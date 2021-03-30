# Usage of Dagger2

## steps

1. 引入依赖

```groovy
apply plugin: 'kotlin-kapt'
...
dependencies {
    ...
	implementation 'com.google.dagger:dagger:2.11'
	kapt 'com.google.dagger:dagger-compiler:2.11'
	compileOnly 'javax.annotation:jsr250-api:1.0'
}
```

2. 创建用于注入的Component和Module

```kotlin
@Singleton // 设置单例
@Component(modules = [AppModule::class, PresenterModule::class,NetworkModule::class, WikiModule::class])
interface AppComponent {
    fun inject(target: HomepageActivity)
    fun inject(target: SearchActivity)
}
```

```kotlin
@Module
class AppModule(private val app: Application) {
    @Provides
    @Singleton
    fun provideContext(): Context = app
}
```

```kotlin
@Module
class PresenterModule {
    @Provides
    @Singleton
    fun provideHomepagePresenter(homepage: Homepage): HomepagePresenter = HomepagePresenterImpl(homepage)

    @Provides
    @Singleton
    fun provideEntryPresenter(wiki: Wiki): EntryPresenter = EntryPresenterImpl(wiki)
}
```

```kotlin
@Module
class NetworkModule {

    companion object {
        private const val NAME_BASE_URL = "NAME_BASE_URL"
    }

    @Provides
    @Named(NAME_BASE_URL)
    fun provideBaseUrlString() = "${Const.PROTOCOL}://${Const.LANGUAGE}.${Const.BASE_URL}"

    @Provides
    @Singleton
    fun provideHttpClient() = OkHttpClient()

    @Provides
    @Singleton
    fun provideRequestBuilder(@Named(NAME_BASE_URL) baseUrl: String) = HttpUrl.parse(baseUrl)?.newBuilder()

    @Provides
    @Singleton
    fun provideWikiApi(client: OkHttpClient, requestBuilder: HttpUrl.Builder?) = WikiApi(client, requestBuilder)
}
```

```kotlin
@Module
class WikiModule {

    @Provides
    @Singleton
    fun provideHomepage(api: WikiApi) = Homepage(api)

    @Provides
    @Singleton
    fun provideWiki(api: WikiApi) = Wiki(api)
}
```

3. 在Application中进行初始化(先执行一次模块的`Build` - `Make Module'...'`)

```kotlin
class WikiApplication : Application() {
    
    // 确保AppComponent在应用中也是单例
    lateinit var wikiComponent: AppComponent
    
    private fun initDagger(app: WikiApplication): AppComponent =
            DaggerAppComponent.builder()
                    .appModule(AppModule(app))
                    .build()

    override fun onCreate() {
        super.onCreate()
        wikiComponent = initDagger(this)
        // 或者使用
        // wikiComponent = DaggerAppComponent.create()
    }
}
```

4. 使用注入

```kotlin
class HomepageActivity : Activity(), HomepageView {

    @Inject
    lateinit var presenter: HomepagePresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        (application as WikiApplication).wikiComponent.inject(this)
        ...
    }
}
```

```kotlin
class HomepagePresenterImpl @Inject constructor(private val homepage: Homepage) : HomepagePresenter {
	...
}
```