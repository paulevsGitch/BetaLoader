package modloader;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.FIELD})
public @interface MLProp {
	/**
	 * Overrides the field name for property key.
	 * @return
	 */
	String name() default "";
	
	/**
	 * Adds additional help to top of configuration file.
	 * @return
	 */
	String info() default "";
	
	/**
	 * Minimum value allowed if field is a number.
	 * @return
	 */
	double min() default Double.NEGATIVE_INFINITY;
	
	/**
	 * Maximum value allowed if field is a number.
	 * @return
	 */
	double max() default Double.POSITIVE_INFINITY;
}
