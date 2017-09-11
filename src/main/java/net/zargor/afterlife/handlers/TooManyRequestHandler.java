package net.zargor.afterlife.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class TooManyRequestHandler {

	private static final int REQUESTLIMIT_GET = 100;
	private static final int REQUESTLIMIT_POST = 50;

	@Data
	@AllArgsConstructor
	class ConnectionRequestAmount {

		private String ip;
		private long time;
		private int amount;
	}

	private final List<ConnectionRequestAmount> tooManyRequestsListGet = new ArrayList<>();
	private final List<ConnectionRequestAmount> tooManyRequestsListOther = new ArrayList<>();

	public TooManyRequestHandler() {
		new Thread(() -> {
			while (true) {
				synchronized (tooManyRequestsListGet) {
					tooManyRequestsListGet.removeAll(tooManyRequestsListGet.stream().filter(cra -> cra.time <= System.currentTimeMillis()).collect(Collectors.toList()));
				}
				synchronized (tooManyRequestsListOther) {
					tooManyRequestsListOther.removeAll(tooManyRequestsListOther.stream().filter(cra -> cra.time <= System.currentTimeMillis()).collect(Collectors.toList()));
				}
				try {
					Thread.sleep(30000);
				} catch (InterruptedException exe) {
					exe.printStackTrace();
				}
			}
		}, "RequestLimitThread");
	}


	/**
	 * @return Returns true if user has made too many requests
	 */
	ConnectionRequestAmount tooManyRequests(ChannelHandlerContext ctx, HttpMethod method) {
		String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostString();
		if (method == HttpMethod.GET) {
			ConnectionRequestAmount con = tooManyRequestsListGet.stream().filter(cra -> Objects.equals(cra.getIp(), ip)).findFirst().orElse(null);
			if (con == null) {
				con = new ConnectionRequestAmount(ip, System.currentTimeMillis() + 1000 * 60, 0);
				tooManyRequestsListGet.add(con);
			}
			con.amount++;
			if (con.time <= System.currentTimeMillis()) {
				tooManyRequestsListGet.remove(con);
				return null;
			}
			return con.amount >= REQUESTLIMIT_GET ? con : null;
		} else {
			ConnectionRequestAmount con = tooManyRequestsListOther.stream().filter(cra -> Objects.equals(cra.getIp(), ip)).findFirst().orElse(null);
			if (con == null) {
				con = new ConnectionRequestAmount(ip, System.currentTimeMillis() + 1000 * 60, 0);
				tooManyRequestsListOther.add(con);
			}
			con.amount++;
			if (con.time <= System.currentTimeMillis()) {
				tooManyRequestsListOther.remove(con);
				return null;
			}
			return con.amount >= REQUESTLIMIT_POST ? con : null;
		}
	}

}
