
package com.github.erasmux.AndLibUtils;

public class CommandLine {

    static String Command = "AndLibUtils";
    
    static String Version = "1.0";

    public static void main(String args[]) {
        if (args.length < 1)
            usage();
        else if (args[0].equals("help")) {
            if (!Prelinked.Help(args))
                usage();
        }
        else if (args[0].equals("version")) {
            System.out.println(Command + " v"+Version);
        }
        else if (Prelinked.CheckArgs(args))
            Prelinked.Run(args);
        else
            usage();
    }

    static void usage() {
        System.out.println("usage: "+Command+" <option> [<flags>]");
        System.out.println("Availble options are:");
        System.out.println("   "+Prelinked.Usage());
        System.out.println("   version");
        System.out.println("   help <option>");
    }
}
