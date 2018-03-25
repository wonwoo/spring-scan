package ml.wonwoo.springscan;

import ml.wonwoo.springscan.test.Bar;
import ml.wonwoo.springscan.test.Foo;
import ml.wonwoo.springscan.test.TestController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;


@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class SpringScanApplicationTests {

  @Autowired
  private ApplicationContext context;

  @Autowired
  private DefaultListableBeanFactory bf;

  @Test
  public void BarTest() {
    ClassPathBeanDefinitionScanner provider = new ClassPathBeanDefinitionScanner(bf, false);
    AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(
        Foo.class);
    provider.addIncludeFilter(annotationTypeFilter);
    beanRegister(provider);
    assertThat(context.getBean(Bar.class)).isNotNull();
  }

  @Test
  public void isEmpty() {
    ClassPathBeanDefinitionScanner provider = new ClassPathBeanDefinitionScanner(bf, false);
    beanRegister(provider);
    assertThat(context.getBeansOfType(Bar.class)).isEmpty();
  }

  @Test
  public void useDefaultFilters() {
    ClassPathBeanDefinitionScanner provider = new ClassPathBeanDefinitionScanner(bf);
    beanRegister(provider);
    assertThat(context.getBeansOfType(TestController.class)).isNotEmpty();
  }

  private void beanRegister(ClassPathBeanDefinitionScanner provider) {
    Set<String> basePackages = new HashSet<>(AutoConfigurationPackages.get(context));
    for (String basePackage : basePackages) {
      Set<BeanDefinition> candidateComponents = provider
          .findCandidateComponents(basePackage);
      for (BeanDefinition candidateComponent : candidateComponents) {
        if (candidateComponent instanceof AnnotatedBeanDefinition) {
          AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
          AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
          String className = annotationMetadata.getClassName();
          BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className);
          BeanDefinitionReaderUtils.registerBeanDefinition(holder, bf);
        }
      }
    }
  }
}
