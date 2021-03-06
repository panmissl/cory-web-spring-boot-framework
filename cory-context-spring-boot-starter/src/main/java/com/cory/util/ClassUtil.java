package com.cory.util;

import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 对Class和Package的一些处理工具方法
 * 
 * @author Cory
 * 
 */
@Slf4j
public class ClassUtil {

	/**
	 * 判断类及其父类（递归）是否有某个注解
	 * @param cls
	 * @param annotation
	 * @return
	 */
	public static boolean classHasAnnotationWithParent(Class cls, Class<? extends Annotation> annotation) {
		return null != AnnotationUtils.findAnnotation(cls, annotation);
	}

	/**
	 * 解析对象属性
	 * @param object 需要解析的对象
	 * @param beanClass 对象的类型
	 * @param filterAnnotationType 属性过滤，可以为空。如果传入，则过滤只有此注解的属性
	 * @return 值可能为空，调用方需要自己处理空值
	 */
	public static Map<String, Object> fetchProperties(Object object, Class<?> beanClass, Class<? extends Annotation> filterAnnotationType) {
		try {
			PropertyDescriptor[] arr = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
			if (null == arr || arr.length == 0) {
				return null;
			}

			Map<String, Object> map = new HashMap<>();
			for (PropertyDescriptor descriptor : arr) {
				String name = descriptor.getName();
				if (name.equals("class")) {
					continue;
				}
				if (null != filterAnnotationType) {
					//只取Field注解过的
					java.lang.reflect.Field javaField = getJavaFieldWithParent(object.getClass(), descriptor.getName());
					if (null == javaField || !javaField.isAnnotationPresent(filterAnnotationType)) {
						continue;
					}
				}
				Object value = descriptor.getReadMethod().invoke(object);
				////不能判断空，如果为空则设置为空，否则插入或更新的时候，null值就更新不到db了
				//if (null != value) {
				//	map.put(name, value);
				//}
				map.put(name, value);
			}
			return map;
		} catch (Throwable e) {
			log.error("fetch properties fail", e);
			throw new CoryException(ErrorCode.GENERIC_ERROR, "解析对象属性失败");
		}
	}

	private static java.lang.reflect.Field getJavaFieldWithParent(Class cls, String fieldName) {
		try {
			return cls.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			//已经到了Object，看还是没有则返回null
			if (cls.equals(Object.class)) {
				try {
					return cls.getDeclaredField(fieldName);
				} catch (NoSuchFieldException e1) {
					return null;
				}
			}
			return getJavaFieldWithParent(cls.getSuperclass(), fieldName);
		}
	}

	/**
	 * 解析类的泛型，比如：List<String>，返回String。如果有多个泛型，比如Map，则返回第0个，需要返回第N个请调用重载方法
	 * @param cls
	 * @return
	 */
	public static Class parseGenericType(Class cls) {
		return parseGenericType(cls, 0);
	}

	/**
	 * 解析类的泛型，比如：Map<String, Integer>，则index = 0 时返回String，index = 1 时返回Integer
	 * @param cls
	 * @param index
	 * @return
	 */
	public static Class parseGenericType(Class cls, int index) {
		Type type = cls.getGenericSuperclass();
		if (null == type) {
			return null;
		}
		if (!(type instanceof ParameterizedType)) {
			return null;
		}
		ParameterizedType parameterizedType = (ParameterizedType) type;
		Type[] arr = parameterizedType.getActualTypeArguments();
		if (null == arr || arr.length < index + 1) {
			return null;
		}
		return (Class) arr[index];
	}

	/**
	 * 从包package中获取所有的Class
	 * 
	 * @param pack
	 * @return
	 */
	public static Set<Class<?>> getClasses(String pack) {
		// 第一个class类的集合
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		// 是否循环迭代
		boolean recursive = true;
		// 获取包的名字 并进行替换
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');
		// 定义一个枚举的集合 并进行循环来处理这个目录下的things
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			// 循环迭代下去
			while (dirs.hasMoreElements()) {
				// 获取下一个元素
				URL url = dirs.nextElement();
				// 得到协议的名称
				String protocol = url.getProtocol();
				// 如果是以文件的形式保存在服务器上
				if ("file".equals(protocol)) {
					System.err.println("file类型的扫描");
					// 获取包的物理路径
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					// 以文件的方式扫描整个包下的文件 并添加到集合中
					findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
				} else if ("jar".equals(protocol)) {
					// 如果是jar包文件
					// 定义一个JarFile
					System.err.println("jar类型的扫描");
					JarFile jar;
					try {
						// 获取jar
						jar = ((JarURLConnection) url.openConnection()).getJarFile();
						// 从此jar包 得到一个枚举类
						Enumeration<JarEntry> entries = jar.entries();
						// 同样的进行循环迭代
						while (entries.hasMoreElements()) {
							// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							// 如果是以/开头的
							if (name.charAt(0) == '/') {
								// 获取后面的字符串
								name = name.substring(1);
							}
							// 如果前半部分和定义的包名相同
							if (name.startsWith(packageDirName)) {
								int idx = name.lastIndexOf('/');
								// 如果以"/"结尾 是一个包
								if (idx != -1) {
									// 获取包名 把"/"替换成"."
									packageName = name.substring(0, idx)
											.replace('/', '.');
								}
								// 如果可以迭代下去 并且是一个包
								if ((idx != -1) || recursive) {
									// 如果是一个.class文件 而且不是目录
									if (name.endsWith(".class") && !entry.isDirectory()) {
										// 去掉后面的".class" 获取真正的类名
										String className = name.substring(packageName.length() + 1, name.length() - 6);
										try {
											// 添加到classes
											classes.add(Class.forName(packageName + '.' + className));
										} catch (ClassNotFoundException e) {
											// log
											// .error("添加用户自定义视图类错误 找不到此类的.class文件");
											e.printStackTrace();
										}
									}
								}
							}
						}
					} catch (IOException e) {
						// log.error("在扫描用户定义视图时从jar包获取文件出错");
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return classes;
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 * 
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	public static void findAndAddClassesInPackageByFile(String packageName,
			String packagePath, final boolean recursive, Set<Class<?>> classes) {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory()) {
			// log.warn("用户定义包名 " + packageName + " 下没有任何文件");
			return;
		}
		// 如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file) {
				return (recursive && file.isDirectory())
						|| (file.getName().endsWith(".class"));
			}
		});
		// 循环所有文件
		for (File file : dirfiles) {
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
			} else {
				// 如果是java类文件 去掉后面的.class 只留下类名
				String className = file.getName().substring(0, file.getName().length() - 6);
				try {
					// 添加到集合中去
					//classes.add(Class.forName(packageName + '.' + className));
					//经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));  
				} catch (ClassNotFoundException e) {
					// log.error("添加用户自定义视图类错误 找不到此类的.class文件");
					e.printStackTrace();
				}
			}
		}
	}
}
