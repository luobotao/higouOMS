import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate3.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.libs.F.Promise;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.RequestHeader;
import play.mvc.WebSocket;
import scala.concurrent.duration.Duration;
import play.mvc.Result;
import utils.Constants;
import actor.FreshTaskTrigger;
import actor.MyWebSocketActor;
import actor.RefundToBBT;
import actor.RestorProductStock;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import forms.DateBetween;
import forms.DateSimpleBetween;

/**
 * Application wide behaviour. We establish a Spring application context for the dependency injection system and
 * configure Spring Data.
 */
public class Global extends GlobalSettings {

    private static Logger.ALogger LOGGER = Logger.of(Global.class);

    /**
     * Declare the application context to be used - a Java annotation based application context requiring no XML.
     */
    final private AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

    /**
     * Sync the context lifecycle with Play's.
     */
    @Override
    public void onStart(final Application app) {
        super.onStart(app);

        configJson();

        // AnnotationConfigApplicationContext can only be refreshed once, but we do it here even though this method
        // can be called multiple times. The reason for doing during startup is so that the Play configuration is
        // entirely available to this application context.
        LOGGER.info("spring register {}", SpringDataJpaConfiguration.class);
        ctx.register(SpringDataJpaConfiguration.class);
        LOGGER.info("spring scan controllers,models,services,utils,repositories");
        ctx.scan("controllers", "models", "services", "utils","repositories");
        ctx.refresh();

        // This will construct the beans and call any construction lifecycle methods e.g. @PostConstruct
        ctx.start();
        //DateBetween 注册
        LOGGER.info("register DateBetween");
        DateBetween.register();
        DateSimpleBetween.register();
        boolean doactor = Play.application().configuration().getBoolean("doactor", false);
		if(doactor){
			LOGGER.info("start to doactor====================");
			Akka.system().scheduler().schedule(
	    	        Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
	    	        Duration.create(5, TimeUnit.MINUTES),     //Frequency 5 minutes
	    	        RefundToBBT.myActor,
	    	        "ACT",
	    	        Akka.system().dispatcher(),
	    	        null
	    	);
			Akka.system().scheduler().schedule(
	    	        Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
	    	        Duration.create(5, TimeUnit.MINUTES),     //Frequency 5 minutes
	    	        RestorProductStock.myActor,
	    	        "ACT",
	    	        Akka.system().dispatcher(),
	    	        null
	    	);
		}
    }

    public static WebSocket<String> socket() {
        return WebSocket.withActor(MyWebSocketActor::props);
    }
    
    @Override
    public Action<?> onRequest(final Http.Request request, Method actionMethod) {
        LOGGER.info("request {} and request method is {}", request.uri(),request);
        return super.onRequest(request, actionMethod);
    }

    private boolean checkUserAgent(String userAgent) {
        for (String s : Constants.USER_AGENTS) {
            if (userAgent.toLowerCase().contains(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean needCheckReferer(String path) {
        for (Pattern p : Constants.NEED_CHECK_REFERER_URL_PATTERN) {
            if (p.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkReferer(Http.Request request) {
        if (!needCheckReferer(request.path())) {
            return true;
        }
        String referer = request.getHeader("Referer");
        if (StringUtils.isNotBlank(referer) && referer.contains(request.host())) {
            return true;
        }
        return false;
    }

    /**
     * Sync the context lifecycle with Play's.
     */
    @Override
    public void onStop(final Application app) {
        // This will call any destruction lifecycle methods and then release the beans e.g. @PreDestroy
        ctx.close();
        super.onStop(app);
    }

    /**
     * Controllers must be resolved through the application context. There is a special method of GlobalSettings
     * that we can override to resolve a given controller. This resolution is required by the Play router.
     */
    @Override
    public <A> A getControllerInstance(Class<A> aClass) {
        return ctx.getBean(aClass);
    }

    
    
    
    @Override
	public Promise<Result> onBadRequest(RequestHeader paramRequestHeader,
			String paramString) {
		// TODO Auto-generated method stub
		return super.onBadRequest(paramRequestHeader, paramString);
	}

	/*@Override
	public Promise<Result> onError(Http.RequestHeader request, Throwable t) {
		LOGGER.error("error request " + request.uri(), t);
        if (request.uri().startsWith("/api")) {
            return F.Promise.promise(new F.Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    DefaultServiceVO vo = new DefaultServiceVO();
                    vo.error("global.systemError");
                    return Results.ok(vo.toJson());
                }
            });
        } else {
            return F.Promise.pure(Action.redirect("/500"));
        }
	}*/

	@Override
	public Promise<Result> onHandlerNotFound(RequestHeader paramRequestHeader) {
		// TODO Auto-generated method stub
		return super.onHandlerNotFound(paramRequestHeader);
	}

	private void configJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.writerWithDefaultPrettyPrinter();
        Json.setObjectMapper(objectMapper);
    }

    /**
     * This configuration establishes Spring Data concerns including those of JPA.
     */
    @Configuration
    @EnableJpaRepositories("repositories")
    @EnableTransactionManagement
    public static class SpringDataJpaConfiguration {

        @Bean
        public EntityManagerFactory entityManagerFactory() {
            LocalContainerEntityManagerFactoryBean managerFactory = new LocalContainerEntityManagerFactoryBean();
            managerFactory.setDataSource(dataSource());
            managerFactory.setPackagesToScan("models");

            HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
            managerFactory.setJpaVendorAdapter(adapter);

            Properties properties = new Properties();
            properties.setProperty("hibernate.hbm2ddl.auto", "update");
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            properties.setProperty("hibernate.show_sql", "true");
            managerFactory.setJpaProperties(properties);

            managerFactory.afterPropertiesSet();
            return managerFactory.getObject();
        }

        @Bean
        public HibernateExceptionTranslator hibernateExceptionTranslator() {
            return new HibernateExceptionTranslator();
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
            jpaTransactionManager.setEntityManagerFactory(entityManagerFactory());
            return jpaTransactionManager;
        }

        @Bean(initMethod = "init", destroyMethod = "close")
        public DataSource dataSource() {
            DruidDataSource druidDataSource = new DruidDataSource();
            String dataUrl = Play.application().configuration().getString("druid.url.dev");
            String password = Play.application().configuration().getString("druid.password.dev");
            if(Play.application().configuration().getBoolean("production", false)){
            	dataUrl = Play.application().configuration().getString("druid.url.product");
            	password = Play.application().configuration().getString("druid.password.product");
            }
            druidDataSource.setUrl(dataUrl);
            druidDataSource.setUsername(Play.application().configuration().getString("druid.username"));
            druidDataSource.setPassword(password);
            druidDataSource.setMaxActive(Play.application().configuration().getInt("druid.maxActive"));
            druidDataSource.setInitialSize(Play.application().configuration().getInt("druid.initialSize"));
            druidDataSource.setMaxWait(Play.application().configuration().getLong("druid.maxWait"));
            druidDataSource.setMinIdle(Play.application().configuration().getInt("druid.minIdle"));
            druidDataSource.setTimeBetweenEvictionRunsMillis(Play.application().configuration().getLong("druid.timeBetweenEvictionRunsMillis"));
            druidDataSource.setMinEvictableIdleTimeMillis(Play.application().configuration().getLong("druid.minEvictableIdleTimeMillis"));
            druidDataSource.setValidationQuery(Play.application().configuration().getString("druid.validationQuery"));
            druidDataSource.setTestWhileIdle(Play.application().configuration().getBoolean("druid.testWhileIdle"));
            druidDataSource.setTestOnBorrow(Play.application().configuration().getBoolean("druid.testOnBorrow"));
            druidDataSource.setTestOnReturn(Play.application().configuration().getBoolean("druid.testOnReturn"));

            try {
                druidDataSource.setFilters("config");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

//            druidDataSource.setConnectionProperties("config.decrypt=true");

            return druidDataSource;
        }
    }
}
