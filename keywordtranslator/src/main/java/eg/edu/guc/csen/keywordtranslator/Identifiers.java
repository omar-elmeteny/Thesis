package eg.edu.guc.csen.keywordtranslator;

import java.util.HashSet;

class Identifiers {
    public static final HashSet<String> commonIdentifiers = initializeCommonIdenfiers();

    private static HashSet<String> initializeCommonIdenfiers() {
        HashSet<String> commonIdentifiers = new HashSet<>();
        commonIdentifiers.add("java");
        commonIdentifiers.add("lang");
        commonIdentifiers.add("io");
        commonIdentifiers.add("math");
        commonIdentifiers.add("util");
        commonIdentifiers.add("awt");
        commonIdentifiers.add("swing");
        commonIdentifiers.add("net");
        commonIdentifiers.add("String");
        commonIdentifiers.add("Integer");
        commonIdentifiers.add("Double");
        commonIdentifiers.add("Float");
        commonIdentifiers.add("Long");
        commonIdentifiers.add("Short");
        commonIdentifiers.add("Byte");
        commonIdentifiers.add("Boolean");
        commonIdentifiers.add("Character");
        commonIdentifiers.add("System");
        commonIdentifiers.add("out");
        commonIdentifiers.add("in");
        commonIdentifiers.add("err");
        commonIdentifiers.add("Math");
        commonIdentifiers.add("Random");
        commonIdentifiers.add("Scanner");
        commonIdentifiers.add("ArrayList");
        commonIdentifiers.add("LinkedList");
        commonIdentifiers.add("HashMap");
        commonIdentifiers.add("HashSet");
        commonIdentifiers.add("print");
        commonIdentifiers.add("println");
        commonIdentifiers.add("printf");
        commonIdentifiers.add("format");
        return commonIdentifiers;
    }
    
}
