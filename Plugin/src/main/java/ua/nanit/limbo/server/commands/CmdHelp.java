package ua.nanit.limbo.server.commands;

import java.util.Collection;

import ua.nanit.limbo.server.Command;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public class CmdHelp implements Command {

    private final LimboServer server;

    public CmdHelp(LimboServer server) {
        this.server = server;
    }

    @Override
    public void execute() {
        Collection<Command> commands = server.getCommandManager().getCommands();

        Log.info("Available commands:");

        for (Command command : commands) {
            Log.info("%s - %s", command.name(), command.description());
        }
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return "Show this message";
    }
}
