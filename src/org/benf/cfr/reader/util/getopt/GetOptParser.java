package org.benf.cfr.reader.util.getopt;

import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.MiscConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: lee
 * Date: 01/02/2013
 * Time: 16:27
 */
public class GetOptParser {

    private static class OptData {
        private final boolean isFlag;
        private final String name;
        private final PermittedOptionProvider.ArgumentParam<?, ?> argument;

        private OptData(String name) {
            this.name = name;
            this.isFlag = true;
            this.argument = null;
        }

        private OptData(PermittedOptionProvider.ArgumentParam<?, ?> argument) {
            this.argument = argument;
            this.isFlag = false;
            this.name = argument.getName();
        }

        public boolean isFlag() {
            return isFlag;
        }

        public String getName() {
            return name;
        }

        public PermittedOptionProvider.ArgumentParam<?, ?> getArgument() {
            return argument;
        }
    }

    public static String getHelp(PermittedOptionProvider permittedOptionProvider) {
        StringBuilder sb = new StringBuilder();
        for (String flag : permittedOptionProvider.getFlags()) {
            sb.append("   [ --").append(flag).append(" ]\n");
        }
        for (PermittedOptionProvider.ArgumentParam param : permittedOptionProvider.getArguments()) {
            sb.append("   [ --").append(param.getName()).append(param.shortDescribe()).append(" ]\n");
        }
        return sb.toString();
    }

    private static Map<String, OptData> buildOptTypeMap(PermittedOptionProvider optionProvider) {
        Map<String, OptData> optTypeMap = MapFactory.newMap();
        for (String flagName : optionProvider.getFlags()) {
            optTypeMap.put(flagName, new OptData(flagName));
        }
        for (PermittedOptionProvider.ArgumentParam arg : optionProvider.getArguments()) {
            optTypeMap.put(arg.getName(), new OptData(arg));
        }
        return optTypeMap;
    }

    public <T> T parse(String[] args, GetOptSinkFactory<T> getOptSinkFactory) {

        List<String> unFlagged = ListFactory.newList();
        int start = 0;
        for (; start < args.length; ++start) {
            String arg = args[start];
            if (arg.startsWith("-")) break;
            unFlagged.add(arg);
        }

        Map<String, String> processed = process(Arrays.copyOfRange(args, start, args.length), getOptSinkFactory);
        return getOptSinkFactory.create(unFlagged, processed);
    }

    public <T> void showHelp(PermittedOptionProvider permittedOptionProvider) {
        System.err.println("CFR " + MiscConstants.CFR_VERSION + "\n");
        System.err.println(getHelp(permittedOptionProvider));
    }

    public <T> void showHelp(PermittedOptionProvider permittedOptionProvider, Options options, PermittedOptionProvider.ArgumentParam<String, Void> helpArg) {
        System.err.println("CFR " + MiscConstants.CFR_VERSION + "\n");
        String relevantOption = options.getOption(helpArg);
        List<? extends PermittedOptionProvider.ArgumentParam<?, ?>> possible = permittedOptionProvider.getArguments();
        for (PermittedOptionProvider.ArgumentParam<?, ?> opt : possible) {
            if (opt.getName().equals(relevantOption)) {
                System.err.println(opt.describe());
                return;
            }
        }
        System.err.println(getHelp(permittedOptionProvider));
        System.err.println("No such argument '" + relevantOption + "'");
    }

    private Map<String, String> process(String[] in, PermittedOptionProvider optionProvider) {
        Map<String, OptData> optTypeMap = buildOptTypeMap(optionProvider);
        Map<String, String> res = MapFactory.newMap();
        for (int x = 0; x < in.length; ++x) {
            if (in[x].startsWith("--")) {
                String name = in[x].substring(2);
                OptData optData = optTypeMap.get(name);
                if (optData == null) {
                    throw new BadParametersException("Unknown argument " + name, optionProvider);
                }
                if (optData.isFlag()) {
                    res.put(name, null);
                } else {
                    if (x >= in.length - 1)
                        throw new BadParametersException("parameter " + name + " requires argument", optionProvider);
                    res.put(name, in[++x]);
                    optData.getArgument().getFn().invoke(res.get(name), null);
                }
            } else {
                throw new BadParametersException("Unexpected argument " + in[x], optionProvider);
            }
        }
        return res;
    }
}
