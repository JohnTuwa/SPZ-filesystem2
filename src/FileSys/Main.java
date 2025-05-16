package FileSys;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        FileManager fileManager = new FileManager();
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("Welcome to the Filesystem");
        System.out.println("Type 'help' to see available commands");

        fileManager.createFile("test");
        fileManager.truncate("test", 80);

        while (true) {
            System.out.print(">>> ");
            input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting...");
                break;
            }

            String[] tokens = input.split("\\s+");
            if (tokens.length == 0) continue;

            String command = tokens[0];

            switch (command.toLowerCase()) {
                case "help":
                    System.out.println("Available commands:");
                    System.out.println("  create <filename>             - Create a new file");
                    System.out.println("  ls                            - List all files");
                    System.out.println("  link <existingFile> <newFile> - Create a new name for an existing file");
                    System.out.println("  unlink <filename>             - Remove a file name");
                    System.out.println("  truncate <file> <size>        - Change file size");
                    System.out.println("  open <filename>               - Open a file and return fd");
                    System.out.println("  close <fd>                    - Close an open file");
                    System.out.println("  seek <fd> <offset>            - Move the read/write pointer in an open file");
                    System.out.println("  read <fd> <size>              - Read data from file of given size");
                    System.out.println("  write <fd> <data>             - Write data to file at current pointer position");
                    System.out.println("  stat <filename>               - Display file descriptor information");
                    System.out.println("  exit                          - Exit the terminal");
                    break;

                case "create":
                    if (tokens.length < 2) {
                        System.out.println("Invalid argument. Type 'help' to see available commands.");
                    } else {
                        fileManager.createFile(tokens[1]);
                    }
                    break;

                case "ls":
                    fileManager.ls();
                    break;

                case "link":
                    if (tokens.length < 3) {
                        System.out.println("Invalid arguments. Type 'help' to see available commands.");
                    } else {
                        fileManager.link(tokens[1], tokens[2]);
                    }
                    break;

                case "unlink":
                    if (tokens.length < 2) {
                        System.out.println("Invalid argument. Type 'help' to see available commands.");
                    } else {
                        fileManager.unlink(tokens[1]);
                    }
                    break;

                case "truncate":
                    if (tokens.length < 3) {
                        System.out.println("Invalid argument. Type 'help' to see available commands.");
                    } else {
                        try {
                            int size = Integer.parseInt(tokens[2]);
                            fileManager.truncate(tokens[1], size);
                        } catch (NumberFormatException e) {
                            System.out.println("Size argument should be a number.");
                        }
                    }
                    break;

                case "seek":
                    if (tokens.length < 3) {
                        System.out.println("Invalid argument. Type 'help' to see available commands.");
                    } else {
                        try {
                            int fd = Integer.parseInt(tokens[1]);
                            int offset = Integer.parseInt(tokens[2]);
                            fileManager.seek(fd, offset);
                        } catch (NumberFormatException e) {
                            System.out.println("Argument should be a number.");
                        }
                    }
                    break;

                case "open":
                    if (tokens.length < 2) {
                        System.out.println("Invalid argument. Type 'help' to see available commands.");
                    } else {
                        fileManager.open(tokens[1]);
                    }
                    break;

                case "close":
                    if (tokens.length < 2) {
                        System.out.println("Invalid argument. Type 'help' to see available commands.");
                    } else {
                        try {
                            int fd = Integer.parseInt(tokens[1]);
                            fileManager.close(fd);
                        } catch (NumberFormatException e) {
                            System.out.println("fd should be a number.");
                        }
                    }
                    break;

                case "read":
                    if (tokens.length < 3) {
                        System.out.println("Invalid argument. Type 'help' to see available commands.");
                    } else {
                        try {
                            int fd = Integer.parseInt(tokens[1]);
                            int size = Integer.parseInt(tokens[2]);
                            fileManager.read(fd, size);
                        } catch (NumberFormatException e) {
                            System.out.println("Argument should be a number.");
                        }
                    }
                    break;

                case "write":
                    if (tokens.length < 3) {
                        System.out.println("Invalid argument. Type 'help' to see available commands.");
                    } else {
                        try {
                            int fd = Integer.parseInt(tokens[1]);
                            fileManager.write(fd, tokens[2]);
                        } catch (NumberFormatException e) {
                            System.out.println("fd should be a number.");
                        }
                    }
                    break;

                case "stat":
                    if (tokens.length < 2) {
                        System.out.println("Invalid argument. Type 'help' to see available commands.");
                    } else {
                        fileManager.stat(tokens[1]);
                    }
                    break;

                default:
                    System.out.println("Unknown command. Type 'help' to see available commands.");
            }
        }
        scanner.close();
    }
}