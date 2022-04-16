package com.example.dlg;

import com.example.dlg.AuthdlgApp;
import com.example.dlg.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { AuthdlgApp.class, TestSecurityConfiguration.class })
public @interface IntegrationTest {
}
