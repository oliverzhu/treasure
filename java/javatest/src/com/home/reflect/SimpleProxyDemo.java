package com.home.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SimpleProxyDemo {
	
}

/**
 * 动态处理工具
 * @author Administrator
 *
 */
class DynamicProxyHandler implements InvocationHandler
{
	//被代理的类(即委托类用于执行自己的代码)
	private Object proxied;
	
	public DynamicProxyHandler(Object proxied) {
		this.proxied = proxied;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		//委托类执行之前，插入要做的事，比如事务
		System.out.println("**** proxy:" + proxy.getClass() + 
				", method : " + method + ", args:" + args);
		if(args != null)
		{
			for(Object arg: args)
			{
				System.out.println("  " + arg);
			}
		}
		
		//委托类执行自己的方法
		method.invoke(proxied, args);
		
		
		//委托类执行之后
		System.out.println("done...");
		return null;
	}
	
}

/**
 * 消费代理
 * @author Administrator
 *
 */
class SimpleDynamicProxy
{
	public static void consumer(Interface iface)
	{
		iface.doSomething();
		iface.somethingElse("bonobo");
	}
	
	public static void main(String[] args) {
		RealObject real = new RealObject();
//		consumer(real);
		
		
		//在运行时创建的动态对象，在编译期间并没有它的字节码信息
		Interface proxy = (Interface) Proxy.newProxyInstance(
				Interface.class.getClassLoader(), 
				new Class[]{Interface.class}, 
				new DynamicProxyHandler(real));
		consumer(proxy);
	}
}

