package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;


import play.Logger;
import play.Play;

public class IpTables {

  private ConcurrentHashMap<String, Table> tables = new ConcurrentHashMap<String, Table>();
  private class Table {
    public Table(String name) {
      this.name = name;
    }

    private final String name;
    private final List<String> entries = new LinkedList<String>();

    public String getName() {
      return name;
    }

    public List<String> getEntries() {
      return entries;
    }
  }
  
  private BlockingQueue<Command> cmdQueue = new LinkedBlockingQueue<Command>();
      
  private boolean deleteCommand(Table table, Command command) {
    for(String cmd: table.getEntries()) {
      if (cmd.equals(command.command)) {
        table.getEntries().remove(cmd);
        return true;
      }
    }
    return false;
  }
  
  private boolean findCommand(Table table, Command command) {
    for(String cmd: table.getEntries()) {
      if (cmd.equals(command.command)) {
        return true;
      }
    }
    return false;
  }
  
  public static class Command {
    public Command(Action action, String table, String command) {
      this.action = action;
      this.command = command;
      this.table = table;
    }
    public Command(String table, String command) {
      if (command.startsWith("-D")) {
        action = Action.DELETE;
      } else if (command.startsWith("-I")) {
        action = Action.INSERT;
      } else if (command.startsWith("-A")) {
        action = Action.APPEND;
      } else {
        throw new IllegalArgumentException(command);
      }
      this.table = table;
      this.command = command.substring("-X ".length());
    }
    public enum Action {
      DELETE, APPEND, INSERT, COMMIT
    };
    public final Action action;
    public final String command;
    public final String table;
    public String getLine() {
      if (action == Action.COMMIT) {
        return "COMMIT\n";
      } 
      if (action == Action.APPEND) {
        return "-A "+ command + "\n";
      }
      if (action == Action.INSERT) {
        return "-I "+ command + "\n";
      }
      if (action == Action.DELETE) {
        return "-D "+ command + "\n";
      }
      return null;
    }
  }

  public IpTables add(String tableName, String strCommand) {
    synchronized (tables) {
      Table table = tables.get(tableName);
      if (table == null) {
        table = new Table(tableName);
        tables.put(table.getName(), table);
      }
      final Command command = new Command(table.getName(), strCommand);
      if (command.action == Command.Action.DELETE) {
        if (deleteCommand(table, command)) {
          cmdQueue.add(command);
        }
      } else if (command.action == Command.Action.APPEND &&
                 command.action == Command.Action.INSERT) {
        if (!findCommand(table, command)) {
          if (command.action == Command.Action.APPEND) {
            table.getEntries().add(command.command);
          } else {
            table.getEntries().add(0, command.command);
          }
          cmdQueue.add(command);
        }
      }
    }
    return this;
  }

  public void start() {
    save.start(new IpTablesSave.Action() {
      public void done() {
        restore.start(new IpTablesRestore.Action() {
          public void done(Exception e) {
          }
        });
      }

      public void add(String table, String command) {
        IpTables.this.add(table, command);
      }
    });
  }

  private final IpTablesSave save = new IpTablesSave();
  private final IpTablesRestore restore = new IpTablesRestore(cmdQueue);

  public IpTables commit() {
    cmdQueue.add(new Command(Command.Action.COMMIT, null, null));
    return this;
  }
}
