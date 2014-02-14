package com.lubin.rpc.server.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.Callable;

import com.lubin.rpc.server.http.message.FullDecodedRequest;
import com.lubin.rpc.server.http.message.Response;


public class DefaultHandler extends
		SimpleChannelInboundHandler<FullDecodedRequest> {

	private final EventExecutorGroup executor;

	public DefaultHandler(EventExecutorGroup executor) {
		super(false);
		this.executor = executor;
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx,
			final FullDecodedRequest decodedRequest) throws Exception {
		Callable<? extends Object> callable = new AsyncTask(
				decodedRequest.getPath(), decodedRequest.getValues());

		final Future<? extends Object> future = executor.submit(callable);

		future.addListener(new GenericFutureListener<Future<Object>>() {
			@Override
			public void operationComplete(Future<Object> future)
					throws Exception {
				if (future.isSuccess()) {
					ctx.writeAndFlush(new Response(decodedRequest.getRequest(),
							future.get()));
				} else {
					ctx.fireExceptionCaught(future.cause());
				}
			}
		});
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		// TODO(adolgarev): cancel submitted tasks,
		// that works only for not in progress tasks
		// if (future != null && !future.isDone()) {
		// future.cancel(true);
		// }
	}

}
