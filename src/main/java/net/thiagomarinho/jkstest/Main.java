package net.thiagomarinho.jkstest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.JCommander.Builder;
import com.beust.jcommander.Parameter;

import net.thiagomarinho.jkstest.commands.ReadUnsignedMessage;
import net.thiagomarinho.jkstest.commands.SignMessage;
import net.thiagomarinho.jkstest.commands.ValidateSignature;
import net.thiagomarinho.jkstest.commands.WriteSignedMessage;
import net.thiagomarinho.jkstest.commands.ReadSignedMessage;
import net.thiagomarinho.jkstest.commands.WriteUnsignedMessage;

public class Main {

	private static final String PROGRAM_NAME = "java -jar jks-test.jar";

    @Parameter(names = "--help", help = true)
    private boolean help;

    private Map<String, Command> commands;

    public interface Command {
        void run();
    }

    public Main() {
        commands = new HashMap<>();
    }

    public static void main(String[] args) {
		new Main()
		        .addCommand("write-unsigned", WriteUnsignedMessage::new)
				.addCommand("read-unsigned", ReadUnsignedMessage::new)
				.addCommand("write-signed", WriteSignedMessage::new)
				.addCommand("read-signed", ReadSignedMessage::new)
				.addCommand("sign", SignMessage::new)
				.addCommand("validate-signature", ValidateSignature::new)
				.run(args);
	}

    private <T extends Command> Main addCommand(String commandName, Supplier<T> constructor) {
        T command = constructor.get();

        this.commands.put(commandName, command);

        return this;
    }

    private void run(String[] args) {
        JCommander commander = commander();

        commander.parse(args);

        commands.getOrDefault(commander.getParsedCommand(), () -> {
            commander.usage();
        }).run();
    }

    private JCommander commander() {
    	Builder builder = JCommander.newBuilder()
                .addObject(this)
                .programName(PROGRAM_NAME);

        commands.entrySet().forEach(entry -> builder.addCommand(entry.getKey(), entry.getValue()));

        return builder.build();
    }
}
