package net.eve0415.spigot.VelocityManager;

import static net.kyori.adventure.text.event.HoverEvent.showText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ServerCommand implements SimpleCommand {
    private final VelocityManager instance;
    public static final int MAX_SERVERS_TO_LIST = 50;

    public ServerCommand(final VelocityManager instance) {
        this.instance = instance;
    }

    @Override
    public void execute(final Invocation invocation) {
        final CommandSource source = invocation.source();
        final String[] ar = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("このコマンドはプレイヤーのみが実行できます", NamedTextColor.RED));
            return;
        }

        final ArrayList<String> args = new ArrayList<String>();
        final Player player = (Player) source;

        if (ar.length != 0) {
            args.add(ar[0]);

            if (ar.length == 2) {
                args.add(ar[1]);
            } else {
                args.add("@s");
            }
        }

        if (args.size() == 0) {
            outputServerInformation(player);
        } else {
            final String serverName = args.get(0);
            final String who = args.get(1);

            final Optional<RegisteredServer> toConnect = this.instance.getServer().getServer(serverName);
            if (!toConnect.isPresent()) {
                player.sendMessage(Component.text("サーバー名：" + serverName + " は存在しません。", NamedTextColor.RED));
                return;
            }

            if (!who.equals("@s")
                    && source.getPermissionValue("velocity.command.server.moveOtherPlayers") == Tristate.FALSE) {
                player.sendMessage(Component.text("あなたは、ほかのプレイヤーを移動させる権限がありません", NamedTextColor.RED));
                return;
            }

            if (who.equals("@a")) {
                final Optional<ServerConnection> s = player.getCurrentServer();
                final Collection<Player> currentPlayer = s.get().getServer().getPlayersConnected();

                for (final Player p : currentPlayer) {
                    p.createConnectionRequest(toConnect.get()).fireAndForget();
                }
            } else if (who.equals("@s")) {
                player.createConnectionRequest(toConnect.get()).fireAndForget();
            } else if (who.equals("@p")) {
                final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("nearbyPlayer");
                out.writeUTF(serverName);
                this.instance.getMessenger().sendOutgoingMessage(out, player);
            } else {
                final Optional<Player> p = this.instance.getServer().getPlayer(who);

                if (p.isPresent()) {
                    p.get().createConnectionRequest(toConnect.get()).fireAndForget();
                } else {
                    player.sendMessage(Component.text("プレイヤー名：" + who + " は見つかりませんでした。", NamedTextColor.RED));
                }
            }
        }
    }

    private void outputServerInformation(final Player executor) {
        final String currentServer = executor.getCurrentServer().map(ServerConnection::getServerInfo)
                .map(ServerInfo::getName).orElse("<不明>");
        executor.sendMessage(Component.text("あなたは現在 " + currentServer + " に接続しています。", NamedTextColor.YELLOW));

        final List<RegisteredServer> servers = sortedServerList(this.instance.getServer());
        if (servers.size() > MAX_SERVERS_TO_LIST) {
            executor.sendMessage(Component.text("サーバーの数が多すぎるため、リスト表示することができません。Tab キーを使用することですべてのサーバーリストを表示することができます。",
                    NamedTextColor.RED));
            return;
        }

        // Assemble the list of servers as components
        final TextComponent.Builder serverListBuilder = Component.text().content("サーバ一覧: ")
                .color(NamedTextColor.YELLOW);
        for (int i = 0; i < servers.size(); i++) {
            final RegisteredServer rs = servers.get(i);
            serverListBuilder.append(formatServerComponent(currentServer, rs));
            if (i != servers.size() - 1) {
                serverListBuilder.append(Component.text(", ", NamedTextColor.GRAY));
            }
        }

        executor.sendMessage(serverListBuilder.build());
    }

    private List<RegisteredServer> sortedServerList(final ProxyServer proxy) {
        final List<RegisteredServer> servers = new ArrayList<>(proxy.getAllServers());
        servers.sort(Comparator.comparing(RegisteredServer::getServerInfo));
        return Collections.unmodifiableList(servers);
    }

    private TextComponent formatServerComponent(final String currentPlayerServer, final RegisteredServer server) {
        final ServerInfo serverInfo = server.getServerInfo();
        TextComponent serverTextComponent = Component.text(serverInfo.getName());

        final String playersText = server.getPlayersConnected().size() + " プレイヤーがオンラインです";
        if (serverInfo.getName().equals(currentPlayerServer)) {
            serverTextComponent = serverTextComponent.color(NamedTextColor.GREEN)
                    .hoverEvent(showText(Component.text("現在このサーバーに接続されています\n" + playersText)));
        } else {
            serverTextComponent = serverTextComponent.color(NamedTextColor.GRAY)
                    .clickEvent(ClickEvent.runCommand("/server " + serverInfo.getName()))
                    .hoverEvent(showText(Component.text("クリックすることでこのサーバーに接続されます\n" + playersText)));
        }
        return serverTextComponent;
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        final String[] currentArgs = invocation.arguments();
        final Player player = (Player) invocation.source();
        final Optional<ServerConnection> s = player.getCurrentServer();
        final Collection<Player> currentPlayer = s.get().getServer().getPlayersConnected();
        final ArrayList<String> pos = new ArrayList<String>();
        for (final Player p : currentPlayer) {
            pos.add(p.getUsername());
        }
        pos.add("@a");
        pos.add("@s");
        pos.add("@p");

        final Stream<String> possibilities = pos.stream();
        final Stream<String> serverPossibilities = this.instance.getServer().getAllServers().stream()
                .map(rs -> rs.getServerInfo().getName());

        if (currentArgs.length == 0) {
            return serverPossibilities.collect(Collectors.toList());
        }

        if (currentArgs.length == 1) {
            return serverPossibilities
                    .filter(name -> name.regionMatches(true, 0, currentArgs[0], 0, currentArgs[0].length()))
                    .collect(Collectors.toList());
        }

        if (currentArgs.length == 2 && invocation.source()
                .getPermissionValue("velocity.command.server.moveOtherPlayers") == Tristate.TRUE) {
            return possibilities.filter(name -> name.regionMatches(true, 0, currentArgs[1], 0, currentArgs[1].length()))
                    .collect(Collectors.toList());
        }

        return ImmutableList.of();
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().getPermissionValue("velocity.command.server") != Tristate.FALSE;
    }
}
