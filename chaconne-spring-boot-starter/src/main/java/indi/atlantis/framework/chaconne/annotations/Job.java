package indi.atlantis.framework.chaconne.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 
 * Job
 * 
 * @author Fred Feng
 *
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface Job {

	String name();

	String description();

	int retries();

	int weight();

	long timeout();

	String email();

}