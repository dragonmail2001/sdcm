/*
 * Copyright 2015-2115 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @email   dragonmail2001@163.com
 * @author  jinglong.zhaijl
 * @date    2015-10-24
 *
 */
package org.cloudy.dscm.publisher;

import java.util.Properties;

import org.cloudy.dscm.common.CConf;
import org.cloudy.dscm.common.CLogger;
import org.cloudy.dscm.context.CContextLoaderListener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class CServer {
	private CLogger logger;
	private int port = 5520;
	private int backlog = 10240;
	private int rcvbuf = 1024*256;
	private int sndbuf = 1024*256;
	private int aggregator = 1024*256;
	private boolean keepalive = true;
	private boolean debug = true;

	private static CServer httpserver;
	private CContextLoaderListener loader;
	private ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	public void addChannel(Channel channel) {
		channels.add(channel);
	}	
	
	public static CServer getInstance(CContextLoaderListener loader) {
		if(httpserver == null && loader != null) {
			httpserver = new CServer(loader);
		}
		return httpserver;
	}
	
	public static CLogger logger() {
		return getInstance().logger;
	}
	
	static CServer getInstance() {
		return httpserver;
	}
	
	public CServer(CContextLoaderListener loader) {
		this.loader = loader;
	}
	
	public Object getBean(String name) throws Exception {
		if(name == null || name.length() <= 0) {
			throw new Exception("bean name is null");
		}
		return loader.classByName(name);
	}

	private ServerBootstrap bootstrap;
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workGroup;
	
	public void stopHttpserver() {
		channels.close().awaitUninterruptibly();  
	}
	
	public boolean debug() {
		return debug;
	}
	
	public int aggregator() {
		return aggregator;
	}
	
	public void startHttpserver(Properties properties) throws Exception{
		
		int capaticy = CConf.toInt(properties.getProperty("capaticy"));
		long delay = CConf.toLong(properties.getProperty("delay"));
		int fmax = CConf.toInt(properties.getProperty("fmax"));
		int lmax = CConf.toInt(properties.getProperty("lmax"));
		int pool = CConf.toInt(properties.getProperty("pool"));
		String path = properties.getProperty("path");
		
		
		port = CConf.toInt(properties.getProperty("port"));
		backlog = CConf.toInt(properties.getProperty("backlog"));
		rcvbuf = CConf.toInt(properties.getProperty("rcvbuf"));
		sndbuf = CConf.toInt(properties.getProperty("sndbuf"));
		keepalive = CConf.toBool(properties.getProperty("keepalive"));
		debug = CConf.toBool(properties.getProperty("debug")); 
		
		aggregator = CConf.toInt(properties.getProperty("aggregator")); 
		
		
		logger = new CLogger(capaticy, delay, fmax,lmax, pool, path, "svr.");
		
		bootstrap = new ServerBootstrap();
		bossGroup = new NioEventLoopGroup();
		workGroup = new NioEventLoopGroup();
		
		bootstrap.group(bossGroup, workGroup);
		bootstrap.option(ChannelOption.SO_BACKLOG, backlog);
	    bootstrap.option(ChannelOption.SO_RCVBUF, rcvbuf);
	    bootstrap.option(ChannelOption.SO_SNDBUF, sndbuf);
	    bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);  
	    bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
	    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, keepalive);			
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childHandler(new CServerInitializer(this));
		
		logger.log(new StringBuilder().append("|backlog:").append(backlog).append("|rcvbuf:").append(rcvbuf).
				append("|sndbuf:").append(sndbuf).append("|keepalive:").append(keepalive).append("|port:").append(port).toString());

		final Channel channel;
		try {
			channel = bootstrap.bind(port).sync().channel();
		} catch (InterruptedException exc) {
			logger.log(exc.getMessage());
			throw exc;
		}
		new Thread(new Runnable() {
			public void run() {
				try {
					channel.closeFuture().sync();
				} catch (InterruptedException exc) {
					exc.printStackTrace();
				} finally {
					workGroup.shutdownGracefully();
					workGroup.shutdownGracefully();
				}
			}
		}).start();
 
	}
}