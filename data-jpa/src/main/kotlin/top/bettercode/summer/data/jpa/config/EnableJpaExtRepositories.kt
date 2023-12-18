package top.bettercode.summer.data.jpa.config

import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.repository.config.BootstrapMode
import org.springframework.data.repository.config.DefaultRepositoryBaseClass
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.transaction.PlatformTransactionManager
import top.bettercode.summer.data.jpa.support.JpaExtRepositoryFactoryBean
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * @author Peter Wu
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@Import(JpaExtRepositoriesRegistrar::class)
annotation class EnableJpaExtRepositories(
        /**
         * Alias for the [.basePackages] attribute. Allows for more concise annotation
         * declarations e.g.: `@EnableJpaRepositories("org.my.pkg")` instead of
         * `@EnableJpaExtRepositories(basePackages="org.my.pkg")`.
         *
         * @return value
         */
        vararg val value: String = [],
        /**
         * Base packages to scan for annotated components. [.value] is an alias for (and mutually
         * exclusive with) this attribute. Use [.basePackageClasses] for a type-safe alternative
         * to String-based package names.
         *
         * @return basePackages
         */
        val basePackages: Array<String> = [],
        /**
         * Type-safe alternative to [.basePackages] for specifying the packages to scan for
         * annotated components. The package of each class specified will be scanned. Consider creating a
         * special no-op marker class or interface in each package that serves no purpose other than being
         * referenced by this attribute.
         *
         * @return basePackageClasses
         */
        val basePackageClasses: Array<KClass<*>> = [],
        /**
         * Specifies which types are eligible for component scanning. Further narrows the set of candidate
         * components from everything in [.basePackages] to everything in the base packages that
         * matches the given filter or filters.
         *
         * @return includeFilters
         */
        val includeFilters: Array<ComponentScan.Filter> = [],
        /**
         * Specifies which types are not eligible for component scanning.
         *
         * @return excludeFilters
         */
        val excludeFilters: Array<ComponentScan.Filter> = [],
        /**
         * Returns the postfix to be used when looking up custom repository implementations. Defaults to
         * Impl. So for a repository named `PersonRepository` the corresponding
         * implementation class will be looked up scanning for `PersonRepositoryImpl`.
         *
         * @return repositoryImplementationPostfix
         */
        val repositoryImplementationPostfix: String = "Impl",
        /**
         * Configures the location of where to find the Spring Data named queries properties file. Will
         * default to `META-INF/jpa-named-queries.properties`.
         *
         * @return namedQueriesLocation
         */
        val namedQueriesLocation: String = "",
        /**
         * Returns the key of the [QueryLookupStrategy] to be used for lookup queries for query
         * methods. Defaults to [Key.CREATE_IF_NOT_FOUND].
         *
         * @return queryLookupStrategy
         */
        val queryLookupStrategy: QueryLookupStrategy.Key = QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND,
        /**
         * Returns the [FactoryBean] class to be used for each repository instance. Defaults to
         * [JpaExtRepositoryFactoryBean].
         *
         * @return repositoryFactoryBeanClass
         */
        val repositoryFactoryBeanClass: KClass<*> = JpaExtRepositoryFactoryBean::class,
        /**
         * Configure the repository base class to be used to create repository proxies for this particular
         * configuration.
         *
         * @return repositoryBaseClass
         * @since 1.9
         */
        val repositoryBaseClass: KClass<*> = DefaultRepositoryBaseClass::class,  // JPA specific configuration
        /**
         * Configures the name of the [EntityManagerFactory] bean definition to be used to create
         * repositories discovered through this annotation. Defaults to `entityManagerFactory`.
         *
         * @return entityManagerFactoryRef
         */
        val entityManagerFactoryRef: String = "entityManagerFactory",
        /**
         * Configures the name of the [PlatformTransactionManager] bean definition to be used to
         * create repositories discovered through this annotation. Defaults to
         * `transactionManager`.
         *
         * @return transactionManagerRef
         */
        val transactionManagerRef: String = "transactionManager",
        /**
         * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be
         * discovered by the repositories infrastructure.
         *
         * @return considerNestedRepositories
         */
        val considerNestedRepositories: Boolean = false,
        /**
         * Configures whether to enable default transactions for Spring Data JPA repositories. Defaults to
         * true. If disabled, repositories must be used behind a facade that's configuring
         * transactions (e.g. using Spring's annotation driven transaction facilities) or repository
         * methods have to be used to demarcate transactions.
         *
         * @return whether to enable default transactions, defaults to true.
         */
        val enableDefaultTransactions: Boolean = true,
        /**
         * Configures when the repositories are initialized in the bootstrap lifecycle.
         * [BootstrapMode.DEFAULT] (default) means eager initialization except all repository
         * interfaces annotated with [Lazy], [BootstrapMode.LAZY] means lazy by default
         * including injection of lazy-initialization proxies into client beans so that those can be
         * instantiated but will only trigger the initialization upon first repository usage (i.e a method
         * invocation on it). This means repositories can still be uninitialized when the application
         * context has completed its bootstrap. [BootstrapMode.DEFERRED] is fundamentally the same
         * as [BootstrapMode.LAZY], but triggers repository initialization when the application
         * context finishes its bootstrap.
         *
         * @return bootstrapMode
         * @since 2.1
         */
        val bootstrapMode: BootstrapMode = BootstrapMode.DEFAULT,
        /**
         * Configures what character is used to escape the wildcards _ and % in
         * derived queries with contains, startsWith or endsWith
         * clauses.
         *
         * @return a single character used for escaping.
         */
        val escapeCharacter: Char = '\\',
        /**
         * @return Configures the name of the [Configuration] bean definition to be used to create
         * repositories discovered through this annotation. Defaults to `mybatisConfiguration`.
         */
        val mybatisConfigurationRef: String = "mybatisConfiguration",
        /**
         * @return Configures the name of the [JpaExtProperties] bean definition to be used to
         * create repositories discovered through this annotation. Defaults to `jpaExtProperties`.
         */
        val jpaExtPropertiesRef: String = "jpaExtProperties",
        /**
         * Locations of MyBatis mapper files.
         */
        val mapperLocations: Array<String> = [],
        /**
         * key
         */
        val key: String = "primary"
)
