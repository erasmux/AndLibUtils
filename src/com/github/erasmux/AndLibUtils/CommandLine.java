
package com.github.erasmux.AndLibUtils;

public class CommandLine {

    static String Command = "AndLibUtils";
    
    static String Version = "1.0";

    public static void main(String args[]) {
        int status = 1;

        if (args.length < 1)
            usage();
        else if (args[0].equals("help")) {
            if (!Prelinked.Help(args) &&
                !JNIRenamer.Help(args))
                usage();
        }
        else if (args[0].equals("version")) {
            System.out.println(Command + " v"+Version);
        }
        else if (Prelinked.CheckArgs(args))
            status = Prelinked.Run(args);
        else if (JNIRenamer.CheckArgs(args))
            status = JNIRenamer.Run(args);
        else
            usage();

        if (status != 0)
            System.exit(status);
    }

    static void usage() {
        System.out.println("usage: "+Command+" <option> [<flags>]");
        System.out.println("Availble options are:");
        System.out.println("   "+JNIRenamer.Usage());
        System.out.println("   "+Prelinked.Usage());
        System.out.println("   version");
        System.out.println("   help <option>");
    }
}
