package net.eve0415.spigot.VelocityManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.format.TextColor;

public class ServerCommand implements Command {
    private final VelocityManager instance;

    public ServerCommand(VelocityManager instance) {
        this.instance = instance;
    }

    @Override
    public void execute(CommandSource source, String @NonNull [] ar) {
        if (!(source instanceof Player)) {
            source.sendMessage(TextComponent.of("このコマンドはプレイヤーのみが実行できます", TextColor.RED));
            return;
        }

        ArrayList<String> args = new ArrayList<String>();
        Player player = (Player) source;

        if (ar.length != 0) {
            args.add(ar[0]);

            if (ar.length == 2) {
                args.add(ar[1]);
            } else {
                args.add("@s");
            }
        }

        if (args.size() == 0) {
            String currentServer = player.getCurrentServer().map(ServerConnection::getServerInfo)
                    .map(ServerInfo::getName).orElse("<不明>");
            player.sendMessage(TextComponent.of("あなたは現在 " + currentServer + "に接続しています。", TextColor.YELLOW));

            // Assemble the list of servers as components
            TextComponent.Builder serverListBuilder = TextComponent.builder("サーバ一覧: ").color(TextColor.YELLOW);
            List<RegisteredServer> infos = ImmutableList.copyOf(this.instance.server.getAllServers());

            for (int i = 0; i < infos.size(); i++) {
                RegisteredServer rs = infos.get(i);
                TextComponent infoComponent = TextComponent.of(rs.getServerInfo().getName());
                String playersText = rs.getPlayersConnected().size() + " プレイヤーがオンラインです";
                if (rs.getServerInfo().getName().equals(currentServer)) {
                    infoComponent = infoComponent.color(TextColor.GREEN)
                            .hoverEvent(HoverEvent.showText(TextComponent.of("現在このサーバーに接続されています\n" + playersText)));
                } else {
                    infoComponent = infoComponent.color(TextColor.GRAY)
                            .clickEvent(ClickEvent.runCommand("/server " + rs.getServerInfo().getName()))
                            .hoverEvent(HoverEvent.showText(TextComponent.of("クリックするとこのサーバーに接続されます\n" + playersText)));
                }
                serverListBuilder.append(infoComponent);

                if (i != infos.size() - 1) {
                    serverListBuilder.append(TextComponent.of(", ", TextColor.GRAY));
                }
            }

            player.sendMessage(serverListBuilder.build());
        } else {
            String serverName = args.get(0);
            String who = args.get(1);

            Optional<RegisteredServer> toConnect = this.instance.server.getServer(serverName);
            if (!toConnect.isPresent()) {
                player.sendMessage(TextComponent.of("サーバー名：" + serverName + " は存在しません。", TextColor.RED));
                return;
            }

            if (!who.equals("@s")
                    && source.getPermissionValue("velocity.command.server.moveOtherPlayers") == Tristate.TRUE) {
                player.sendMessage(TextComponent.of("あなたは、ほかのプレイヤーを移動させる権限がありません", TextColor.RED));
                return;
            }

            if (who.equals("@a")) {
                Optional<ServerConnection> s = player.getCurrentServer();
                Collection<Player> currentPlayer = s.get().getServer().getPlayersConnected();

                for (Player p : currentPlayer) {
                    p.createConnectionRequest(toConnect.get()).fireAndForget();
                }
            } else if (who.equals("@s")) {
                player.createConnectionRequest(toConnect.get()).fireAndForget();
            } else if (who.equals("@p")) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("nearbyPlayer");
                out.writeUTF(serverName);
                this.instance.messenger.sendOutgoingMessage(out, player);
            } else {
                Optional<Player> p = this.instance.server.getPlayer(who);

                if (p.isPresent()) {
                    p.get().createConnectionRequest(toConnect.get()).fireAndForget();
                } else {
                    player.sendMessage(TextComponent.of("プレイヤー名：" + who + " は見つかりませんでした。", TextColor.RED));
                }
            }
        }
    }

    @Override
    public List<String> suggest(CommandSource source, String @NonNull [] currentArgs) {
        Stream<String> possibilitieServer = Stream.concat(Stream.of("all"),
                this.instance.server.getAllServers().stream().map(rs -> rs.getServerInfo().getName()));
        Player player = (Player) source;
        player.sendMessage(TextComponent.of(currentArgs.length));

        if (currentArgs.length == 0) {
            return possibilitieServer.collect(Collectors.toList());
        } else if (currentArgs.length == 1) {
            return possibilitieServer
                    .filter(name -> name.regionMatches(true, 0, currentArgs[0], 0, currentArgs[0].length()))
                    .collect(Collectors.toList());
        } else if (currentArgs.length == 2) {
            if (source.getPermissionValue("velocity.command.server.moveOtherPlayers") == Tristate.FALSE)
                return ImmutableList.of();

            Optional<ServerConnection> s = player.getCurrentServer();
            Collection<Player> currentPlayer = s.get().getServer().getPlayersConnected();
            ArrayList<String> possibilities = new ArrayList<String>();

            for (Player p : currentPlayer) {
                possibilities.add(p.getUsername());
            }
            possibilities.add("@a");
            possibilities.add("@s");
            this.instance.logger.info(possibilities.toString());
            return possibilities.stream()
                    .filter(name -> name.regionMatches(true, 0, currentArgs[0], 0, currentArgs[0].length()))
                    .collect(Collectors.toList());
        } else {
            return ImmutableList.of();
        }
    }

    @Override
    public boolean hasPermission(CommandSource source, String @NonNull [] args) {
        return source.getPermissionValue("velocity.command.server") != Tristate.FALSE;
    }
}