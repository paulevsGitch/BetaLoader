package paulevs.betaloader.utilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {
	public static <T> T callMethod(Class<T> castClass, Object instance, String methodName, Object... args) {
		T result = null;
		try {
			Class<?>[] methodArgs = new Class[args.length];
			Method method = instance.getClass().getDeclaredMethod(methodName, methodArgs);
			method.setAccessible(true);
			result = (T) method.invoke(instance, args);
		}
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}
}
