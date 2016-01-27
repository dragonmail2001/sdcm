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

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class CServerInitializer extends ChannelInitializer<SocketChannel> {

	private CServer server;
	
	public CServerInitializer(CServer server) {
		this.server = server;
	}
	
	public void addChannel(Channel channel) {
		this.server.addChannel(channel);
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		// create a default pipeline implementation
		ChannelPipeline pipeline = ch.pipeline();
		/**
		 * uncomment the following line if you want HTTPS SSLEngine engine =
		 * SecureChatSslContextFactory.getServerContext().createSSLEngine();
		 * engine.setUseClientMode(false); pipeline.addLast("ssl",new
		 * SslHandler(engine));
		 */
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpObjectAggregator(server.aggregator()));
		pipeline.addLast("encoder", new HttpResponseEncoder());

		/**
		 * Remove the following line if you don't want automatic content
		 * compression. pipeline.addLast("deflater", new
		 * HttpContentCompressor());
		 */
		pipeline.addLast("handler", new CServerHandler(this));

	}
}
