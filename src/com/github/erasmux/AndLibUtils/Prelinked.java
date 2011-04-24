
package com.github.erasmux.AndLibUtils;

import java.io.*;
import java.util.*;

public class Prelinked {

    private File file_;
    private long addr_;

    public Prelinked(File file) throws IOException, FileNotFoundException {
        file_ = file;
        addr_ = GetPrelinkAddr(new RandomAccessFile(file_, "r"));
    }

    public File file() {
        return file_;
    }

    public String filename() {
        return file_.getName();
    }

    public boolean prelinked() {
        return addr_ >= 0;
    }

    /// prelinked address, valid only if prelinked() is true.
    public long address() {
        return addr_;
    }

    /// returns prelinked address of the given input or -1 if not prelinked.
    static public long GetPrelinkAddr(RandomAccessFile in) throws IOException {
        long length = in.length();
        if (length < 8)
            return -1;

        in.seek(length-8);
        // read little unendian unsigned int:
        long addr = in.readUnsignedByte() |
            (in.readUnsignedByte() << 8) |
            (in.readUnsignedByte() << 16) |
            ((long)in.readUnsignedByte() << 24);
        
        // check magic:
        if (in.readByte()!='P' || in.readByte()!='R' || in.readByte()!='E' || in.readByte()!=' ')
            return -1;

        return addr;
    }

    // Functions for Command line interface:

    public static String CommandName = "prelink";

    public static String Usage() {
        return CommandName+" map [-o outfile] <files>";
    }

    static void PrintUsage() {
        System.out.println("usage: "+CommandLine.Command+" "+Usage());
    }

    public static boolean CheckArgs(String args[]) {
        return args.length > 2 &&
            args[0].equals(CommandName) &&
            args[1].equals("map");
    }

    public static boolean Help(String args[]) {
        if (args.length >= 2 && args[1].equals(CommandName)) {
            PrintUsage();
            System.out.println();
            System.out.println("Checks the prelinked address of the specificed files.");
            System.out.println(" -o outfile : logs output to given file");
            return true;
        }
        return false;
    }

    public static int Run(String args[]) {
        if (args.length < 3 || !CheckArgs(args)) {
            PrintUsage();
            return -1;
        }

        String outfile = null;
        List<String> files = new LinkedList<String>();
        for(int ii=2; ii<args.length; ++ii) {
            if (args[ii].equals("-o") && (ii+1)<args.length) {
                outfile = args[++ii];
            }
            else files.add(args[ii]);
        }
        
        PrintStream out = System.out;
        if (outfile != null) 
            try {
                out = new PrintStream(new File(outfile));
            } catch (IOException e) {
                System.err.println("Error opening output file: "+e.getMessage());
                return -3;
            }


        // sort files in order of their prelinked address:
        SortedSet<Prelinked> plmap = new TreeSet<Prelinked>(new Comparator<Prelinked>() {
                public int compare(Prelinked pl1, Prelinked pl2) {
                    if (pl1.prelinked() && pl2.prelinked()) {
                        if (pl1.address() < pl2.address())
                            return -1;
                        if (pl1.address() > pl2.address())
                            return 1;
                    }
                    else if (pl1.prelinked() && !pl2.prelinked())
                        return -1;
                    else if (!pl1.prelinked() && pl2.prelinked()) 
                        return 1;
                    // otherwise fallback to order by filename:
                    return pl1.filename().compareTo(pl2.filename());
                }
            });
        // check actual prelinked status of each file:
        int count=0, errors=0;
        for (Iterator<String> iter = files.iterator(); iter.hasNext(); ++count) {
            String fname = iter.next();
            try {
                plmap.add(new Prelinked(new File(fname)));
            } catch (Exception e) {
                System.err.println("Error processing file "+fname+": "+e.getMessage());
                errors++;
            }
        }

        // print results:
        for (Iterator<Prelinked> iter = plmap.iterator(); iter.hasNext();) {
            Prelinked prelink = iter.next();
            if (prelink.prelinked())
                out.println("prelinked @ 0x"+String.format("%8X",prelink.address())+": "+prelink.filename());
            else
                out.println("not prelinked:          "+prelink.filename());
        }

        System.out.println("Processed "+Integer.toString(count)+" files"
                           +(errors>0 ? String.format(" (%d errors).",errors) : "."));
        return errors>0 ? -5 : 0;
    }

}
