package top.bettercode.summer.util.test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;
import top.bettercode.autodoc.gen.Autodoc;
import top.bettercode.autodoc.gen.AutodocSetting;
import top.bettercode.logging.RequestLoggingFilter;
import top.bettercode.logging.RequestLoggingProperties;

/**
 * mockMvc 基础测试类
 *
 * @author Peter Wu
 */
@ExtendWith(value = {SpringExtension.class, AutodocSetting.class})
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude="
        + "top.bettercode.simpleframework.security.config.SecurityConfiguration,"
        + "top.bettercode.environment.WritableEnvironmentEndpointAutoConfiguration,"
        + "top.bettercode.simpleframework.config.CorsConfiguration,"
        + "top.bettercode.logging.websocket.WebsocketConfiguration,"
        + "top.bettercode.config.EndpointAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.websocket.servlet.WebSocketMessagingAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.amqp.RabbitHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.audit.AuditAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.audit.AuditEventsEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.availability.AvailabilityHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.availability.AvailabilityProbesAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.cache.CachesEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.cassandra.CassandraHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.cassandra.CassandraReactiveHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.cloudfoundry.servlet.CloudFoundryActuatorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.cloudfoundry.reactive.ReactiveCloudFoundryActuatorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.context.properties.ConfigurationPropertiesReportEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.context.ShutdownEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.couchbase.CouchbaseHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.couchbase.CouchbaseReactiveHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticSearchReactiveHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticSearchRestHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.endpoint.jmx.JmxEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.flyway.FlywayEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.hazelcast.HazelcastHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.influx.InfluxDbHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.integration.IntegrationGraphEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.jms.JmsHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.jolokia.JolokiaEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.ldap.LdapHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.liquibase.LiquibaseEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.logging.LogFileWebEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.logging.LoggersEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.mail.MailHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.management.HeapDumpWebEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.management.ThreadDumpEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.KafkaMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.Log4J2MetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.LogbackMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.amqp.RabbitMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.cache.CacheMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.data.RepositoryMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.appoptics.AppOpticsMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.atlas.AtlasMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.datadog.DatadogMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.dynatrace.DynatraceMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.elastic.ElasticMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.ganglia.GangliaMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.graphite.GraphiteMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.humio.HumioMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.influx.InfluxMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.jmx.JmxMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.kairos.KairosMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.newrelic.NewRelicMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.signalfx.SignalFxMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.stackdriver.StackdriverMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.statsd.StatsdMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.export.wavefront.WavefrontMetricsExportAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.integration.IntegrationMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.jersey.JerseyServerMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.mongo.MongoMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.orm.jpa.HibernateMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.r2dbc.ConnectionPoolMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.web.client.HttpClientMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.web.jetty.JettyMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.metrics.web.tomcat.TomcatMetricsAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.mongo.MongoHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.mongo.MongoReactiveHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.neo4j.Neo4jHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.quartz.QuartzEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.r2dbc.ConnectionFactoryHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.redis.RedisHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.scheduling.ScheduledTasksEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.session.SessionsEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.solr.SolrHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.startup.StartupEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.system.DiskSpaceHealthContributorAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.web.mappings.MappingsEndpointAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementContextAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration,"
        + "org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementContextAutoConfiguration"
})
public abstract class BaseWebNoAuthTest {

  @Autowired
  private WebApplicationContext context;
  protected MockMvc mockMvc;
  @Autowired
  private RequestLoggingFilter requestLoggingFilter;
  @Autowired(required = false)
  private AutoDocFilter autoDocFilter;
  @Autowired
  protected RequestLoggingProperties requestLoggingProperties;

  protected final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() throws Exception {
    //--------------------------------------------
    requestLoggingProperties.setForceRecord(true);
    requestLoggingProperties.setIncludeRequestBody(true);
    requestLoggingProperties.setIncludeResponseBody(true);
    requestLoggingProperties.setFormat(true);
    mockMvc = webAppContextSetup(context)
        .addFilter(autoDocFilter)
        .addFilter(requestLoggingFilter)
        .build();
  }

  private String getFileName(MvcResult result) throws UnsupportedEncodingException {
    String contentDisposition = result.getResponse().getHeader("Content-Disposition");
    contentDisposition = URLDecoder
        .decode(contentDisposition.replaceAll(".*filename\\*=UTF-8''(.*?)", "$1"), "UTF-8");
    return "build/" + contentDisposition;
  }

  protected void download(ResultActions perform) throws Exception {
    MvcResult result = perform.andExpect(status().isOk()).andReturn();
    String fileName = getFileName(result);
    StreamUtils.copy(result.getResponse().getContentAsByteArray(),
        new FileOutputStream(fileName));
    try {
      String filePath = System.getProperty("user.dir") + File.separator + fileName;
      if (System.getProperties().getProperty("os.name").toLowerCase().startsWith("win")) {
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + filePath);
      } else {
        Runtime.getRuntime().exec("xdg-open " + filePath);
      }
    } catch (Exception ignored) {
    }
  }

  protected String nonnullJson(Object object) throws JsonProcessingException {
    return objectMapper.setSerializationInclusion(
        Include.NON_NULL).writeValueAsString(object);
  }

  protected void requires(String... require) {
    Autodoc.requiredParameters(require);
  }

  protected void tableNames(String... tableName) {
    Autodoc.tableNames(tableName);
  }
}
