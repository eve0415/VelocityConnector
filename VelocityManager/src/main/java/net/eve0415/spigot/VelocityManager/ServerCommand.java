package net.eve0415.spigot.VelocityManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.slf4j.Logger;

import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.format.TextColor;

public class ServerCommand implements Command {
    private final ProxyServer server;
    private Logger logger;

    public ServerCommand(ProxyServer server) {
        this.server = server;
    }

    // server name @a
    @Override
    public void execute(CommandSource source, String @NonNull [] ar) {
        if (!(source instanceof Player)) {
            source.sendMessage(TextComponent.of("Only players may run this command.", TextColor.RED));
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
                    .map(ServerInfo::getName).orElse("<unknown>");
            player.sendMessage(
                    TextComponent.of("You are currently connected to " + currentServer + ".", TextColor.YELLOW));

            // Assemble the list of servers as components
            TextComponent.Builder serverListBuilder = TextComponent.builder("Available servers: ")
                    .color(TextColor.YELLOW);
            List<RegisteredServer> infos = ImmutableList.copyOf(server.getAllServers());

            for (int i = 0; i < infos.size(); i++) {
                RegisteredServer rs = infos.get(i);
                TextComponent infoComponent = TextComponent.of(rs.getServerInfo().getName());
                String playersText = rs.getPlayersConnected().size() + " player(s) online";
                if (rs.getServerInfo().getName().equals(currentServer)) {
                    infoComponent = infoComponent.color(TextColor.GREEN).hoverEvent(HoverEvent
                            .showText(TextComponent.of("Currently connected to this server\n" + playersText)));
                } else {
                    infoComponent = infoComponent.color(TextColor.GRAY)
                            .clickEvent(ClickEvent.runCommand("/server " + rs.getServerInfo().getName()))
                            .hoverEvent(HoverEvent
                                    .showText(TextComponent.of("Click to connect to this server\n" + playersText)));
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

            Optional<RegisteredServer> toConnect = server.getServer(serverName);
            if (!toConnect.isPresent()) {
                player.sendMessage(TextComponent.of("Server " + serverName + " doesn't exist.", TextColor.RED));
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
            } else {
                Optional<Player> p = this.server.getPlayer(who);

                if (p.isPresent()) {
                    p.get().createConnectionRequest(toConnect.get()).fireAndForget();
                } else {
                    player.sendMessage(TextComponent.of("Cannot find player name " + who, TextColor.RED));
                }
            }
        }
    }

    @Override
    public List<String> suggest(CommandSource source, String @NonNull [] currentArgs) {
        Stream<String> possibilitieServer = Stream.concat(Stream.of("all"),
                server.getAllServers().stream().map(rs -> rs.getServerInfo().getName()));
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
            logger.info(possibilities.toString());
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