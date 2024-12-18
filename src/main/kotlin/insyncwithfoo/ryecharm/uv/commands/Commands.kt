package insyncwithfoo.ryecharm.uv.commands

import insyncwithfoo.ryecharm.Command
import insyncwithfoo.ryecharm.configurations.uv.UVTimeouts
import insyncwithfoo.ryecharm.message


internal class InitCommand : Command(), UVCommand {
    
    override val subcommand = "init"
    override val timeoutKey = UVTimeouts.INIT.key
    
    override val runningMessage: String
        get() = message("progresses.command.uv.init")
    
}


internal class AddCommand : Command(), UVCommand {
    
    override val subcommand = "add"
    override val timeoutKey = UVTimeouts.ADD.key
    
    override val runningMessage: String
        get() = message("progresses.command.uv.add")
    
}


internal class RemoveCommand : Command(), UVCommand {
    
    override val subcommand = "remove"
    override val timeoutKey = UVTimeouts.REMOVE.key
    
    override val runningMessage: String
        get() = message("progresses.command.uv.remove")
    
}


internal class UpgradeCommand : Command(), UVCommand {
    
    override val subcommand = "add"
    override val timeoutKey = UVTimeouts.ADD.key
    
    override val runningMessage: String
        get() = message("progresses.command.uv.upgrade")
    
}


internal class SyncCommand : Command(), UVCommand {
    
    override val subcommand = "sync"
    override val timeoutKey = UVTimeouts.SYNC.key
    
    override val runningMessage: String
        get() = message("progresses.command.uv.sync")
    
}


internal class VenvCommand : Command(), UVCommand {
    
    override val subcommand = "venv"
    override val timeoutKey = UVTimeouts.VENV.key
    
    override val runningMessage: String
        get() = message("progresses.command.uv.venv")
    
}


internal class VersionCommand : Command(), UVCommand {
    
    override val subcommand = "version"
    override val timeoutKey = UVTimeouts.VERSION.key
    
    override val runningMessage: String
        get() = message("progresses.command.uv.version")
    
}
